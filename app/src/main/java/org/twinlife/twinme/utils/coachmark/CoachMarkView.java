/*
 *  Copyright (c) 2022 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.utils.coachmark;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.percentlayout.widget.PercentRelativeLayout;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

import java.util.ArrayList;
import java.util.List;


public class CoachMarkView extends PercentRelativeLayout {

    private static final String LOG_TAG = "CoachMarkView";
    private static final boolean DEBUG = false;

    public interface OnCoachMarkViewListener {

        void onCloseCoachMark();

        void onTapCoachMarkFeature();

        void onLongPressCoachMarkFeature();
    }

    private static final long ANIMATION_DURATION = 100;

    private static final float DESIGN_TEXT_HEIGHT_PADDING = 12f;
    private static final float DESIGN_TEXT_WIDTH_PADDING = 20f;
    private static final float DESIGN_MESSAGE_VIEW_VERTICAL_MARGIN = 16f;
    private static final float DESIGN_MESSAGE_VIEW_HORIZONTAL_MARGIN = 16f;

    static final int TEXT_HEIGHT_PADDING;
    static final int TEXT_WIDTH_PADDING;
    static final int MESSAGE_VIEW_VERTICAL_MARGIN;
    static final int MESSAGE_VIEW_HORIZONTAL_MARGIN;

    static {
        TEXT_HEIGHT_PADDING = (int) (DESIGN_TEXT_HEIGHT_PADDING * Design.HEIGHT_RATIO);
        TEXT_WIDTH_PADDING = (int) (DESIGN_TEXT_WIDTH_PADDING * Design.WIDTH_RATIO);
        MESSAGE_VIEW_VERTICAL_MARGIN = (int) (DESIGN_MESSAGE_VIEW_VERTICAL_MARGIN * Design.HEIGHT_RATIO);
        MESSAGE_VIEW_HORIZONTAL_MARGIN = (int) (DESIGN_MESSAGE_VIEW_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
    }

    private CoachMarkOverlayView mCoachMarkOverlayView;
    private TextView mMessageView;
    private View mFeatureView;

    private OnCoachMarkViewListener mOnCoachMarkViewListener;

    private CoachMark mCoachMark;

    private final List<View> animationList = new ArrayList<>();

    private boolean mIsAnimationEnded = false;

    public CoachMarkView(Context context) {
        super(context);
    }

    public CoachMarkView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (DEBUG) {
            Log.d(LOG_TAG, "create");
        }

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            View view = inflater.inflate(R.layout.coach_mark_view, (ViewGroup) getParent());
            view.setLayoutParams(new PercentRelativeLayout.LayoutParams(PercentRelativeLayout.LayoutParams.MATCH_PARENT, PercentRelativeLayout.LayoutParams.MATCH_PARENT));
            addView(view);
        }

        mCoachMarkOverlayView = findViewById(R.id.coach_mark_view_overlay_view);
        mCoachMarkOverlayView.setOnClickListener(view -> mOnCoachMarkViewListener.onCloseCoachMark());
        mCoachMarkOverlayView.setBackgroundColor(Color.TRANSPARENT);

        mMessageView = findViewById(R.id.coach_mark_view_message_view);
        Design.updateTextFont(mMessageView, Design.FONT_MEDIUM34);
        mMessageView.setTextColor(Color.BLACK);
        mMessageView.setPadding(TEXT_WIDTH_PADDING, TEXT_HEIGHT_PADDING, TEXT_WIDTH_PADDING, TEXT_HEIGHT_PADDING);

        MarginLayoutParams marginLayoutParams = (MarginLayoutParams) mMessageView.getLayoutParams();
        marginLayoutParams.leftMargin = MESSAGE_VIEW_HORIZONTAL_MARGIN;
        marginLayoutParams.rightMargin = MESSAGE_VIEW_HORIZONTAL_MARGIN;
        marginLayoutParams.setMarginStart(MESSAGE_VIEW_HORIZONTAL_MARGIN);
        marginLayoutParams.setMarginEnd(MESSAGE_VIEW_HORIZONTAL_MARGIN);

        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.mutate();
        gradientDrawable.setColor(Color.WHITE);
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        gradientDrawable.setCornerRadius(radius);
        mMessageView.setBackground(gradientDrawable);

        mFeatureView = findViewById(R.id.coach_mark_view_feature_view);
        mFeatureView.setOnClickListener(view -> mOnCoachMarkViewListener.onTapCoachMarkFeature());
        mFeatureView.setOnLongClickListener(view -> {
            mOnCoachMarkViewListener.onLongPressCoachMarkFeature();
            return true;
        });
    }

    public void setOnCoachMarkViewListener(OnCoachMarkViewListener onCoachMarkViewListener) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setOnCoachMarkViewListener: onCoachMarkViewListener=" + onCoachMarkViewListener);
        }

        mOnCoachMarkViewListener = onCoachMarkViewListener;
    }

    public void openCoachMark(CoachMark coachMark) {
        if (DEBUG) {
            Log.d(LOG_TAG, "openCoachMark: coachMark=" + coachMark);
        }

        mCoachMark = coachMark;
        mIsAnimationEnded = false;

        mCoachMarkOverlayView.setAlpha((float) 0.0);
        mMessageView.setAlpha((float) 0.0);

        mMessageView.setText(coachMark.getMessage());

        ViewGroup.LayoutParams layoutParams = mFeatureView.getLayoutParams();
        layoutParams.width = coachMark.getFeatureWidth();
        layoutParams.height = coachMark.getFeatureHeight();

        mFeatureView.setX(coachMark.getFeaturePosition().x);
        mFeatureView.setY(coachMark.getFeaturePosition().y);

        ViewTreeObserver viewTreeObserver = mMessageView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewTreeObserver viewTreeObserver = mFeatureView.getViewTreeObserver();
                viewTreeObserver.removeGlobalOnLayoutListener(this);

                if (coachMark.isOnTop()) {
                    mMessageView.setY(mFeatureView.getY() - MESSAGE_VIEW_VERTICAL_MARGIN - mMessageView.getHeight());
                } else {
                    mMessageView.setY(mFeatureView.getY() + mFeatureView.getHeight() + MESSAGE_VIEW_VERTICAL_MARGIN);
                }
            }
        });

        mCoachMarkOverlayView.setClipRect(new RectF(coachMark.getFeaturePosition().x, coachMark.getFeaturePosition().y, coachMark.getFeaturePosition().x + coachMark.getFeatureWidth(), coachMark.getFeaturePosition().y + coachMark.getFeatureHeight()), coachMark.getFeatureRadius());

        animationList.clear();

        animationList.add(mCoachMarkOverlayView);
        animationList.add(mMessageView);

        animationCoachMark();
    }

    public CoachMark getCoachMark() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getCoachMark");
        }

        return mCoachMark;
    }

    public void animationCoachMark() {
        if (DEBUG) {
            Log.d(LOG_TAG, "animationCoachMark");
        }

        if (mIsAnimationEnded) {
            return;
        }

        PropertyValuesHolder propertyValuesHolderAlpha = PropertyValuesHolder.ofFloat(View.ALPHA, 0.0f, 1.0f);

        List<Animator> animators = new ArrayList<>();

        for (View view : animationList) {
            ObjectAnimator alphaViewAnimator = ObjectAnimator.ofPropertyValuesHolder(view, propertyValuesHolderAlpha);
            alphaViewAnimator.setDuration(ANIMATION_DURATION);
            animators.add(alphaViewAnimator);
        }

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(animators);
        animatorSet.start();
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animator) {

            }

            @Override
            public void onAnimationEnd(@NonNull Animator animator) {

                mIsAnimationEnded = true;
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animator) {

            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animator) {

            }
        });
    }
}
