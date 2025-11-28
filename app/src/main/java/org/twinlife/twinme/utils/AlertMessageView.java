/*
 *  Copyright (c) 2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.utils;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.percentlayout.widget.PercentRelativeLayout;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

import java.util.ArrayList;
import java.util.List;

public class AlertMessageView extends PercentRelativeLayout {
    private static final String LOG_TAG = "AlertMessageView";
    private static final boolean DEBUG = false;

    public interface Observer {

        void onConfirmClick();

        void onDismissClick();

        void onCloseViewAnimationEnd();
    }

    protected static final int DESIGN_TITLE_MARGIN = 60;
    private static final int DESIGN_MESSAGE_MARGIN = 30;
    private static final int DESIGN_CONFIRM_TOP_MARGIN = 70;
    private static final int DESIGN_CONFIRM_BOTTOM_MARGIN = 80;
    private static final int DESIGN_CONFIRM_VERTICAL_MARGIN = 10;
    private static final int DESIGN_CONFIRM_HORIZONTAL_MARGIN = 20;

    protected View mOverlayView;
    protected View mActionView;
    protected View mSlideMarkView;
    protected TextView mTitleView;
    protected TextView mMessageView;
    protected View mConfirmView;
    protected TextView mConfirmTextView;

    private int mRootHeight = 0;
    private int mActionHeight = 0;

    private boolean isOpenAnimationEnded = false;
    private boolean isCloseAnimationEnded = false;

    private String mTitle;
    private String mMessage;
    private String mConfirmTitle;
    private boolean mForceDarkMode = false;
    private int mWindowHeight = 0;

    @Nullable
    private Observer mObserver;

    public AlertMessageView(Context context) {
        super(context);
    }

    public AlertMessageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (DEBUG) {
            Log.d(LOG_TAG, "create");
        }

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.alert_message_view, this, true);
        initViews();
    }

    public void setObserver(Observer observer) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setObserver: " + observer);
        }

        mObserver = observer;
    }

    public void setWindowHeight(int windowHeight) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setWindowHeight: " + windowHeight);
        }

        mWindowHeight = windowHeight;
    }

    public void setConfirmTitle(String title) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setConfirmTitle: " + title);
        }

        mConfirmTitle = title;
    }

    public void setTitle(String title) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setTitle: " + title);
        }

        mTitle = title;
    }

    public void setMessage(String message) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setMessage: " + message);
        }

        mMessage = message;
    }

    public void setForceDarkMode(boolean forceDarkMode) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setForceDarkMode: " + forceDarkMode);
        }

        mForceDarkMode = forceDarkMode;
    }

    public void show() {
        if (DEBUG) {
            Log.d(LOG_TAG, "show");
        }

        updateViews();

        ViewTreeObserver viewTreeObserver = mActionView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewTreeObserver viewTreeObserver = mActionView.getViewTreeObserver();
                viewTreeObserver.removeOnGlobalLayoutListener(this);

                mRootHeight = mOverlayView.getHeight();

                if (mWindowHeight != 0) {
                    mRootHeight = mWindowHeight;
                }

                mActionHeight = mActionView.getHeight();
                showAlertView();
            }
        });
    }

    public void animationOpenConfirmView() {
        if (DEBUG) {
            Log.d(LOG_TAG, "animationOpenConfirmView");
        }

        if (isOpenAnimationEnded) {
            return;
        }

        mOverlayView.setAlpha(1.0f);

        int startValue = mRootHeight;
        int endValue = mRootHeight - mActionHeight;

        List<Animator> animators = new ArrayList<>();

        PropertyValuesHolder propertyValuesHolder = PropertyValuesHolder.ofFloat(View.Y, startValue, endValue);

        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(mActionView, propertyValuesHolder);
        objectAnimator.setDuration(Design.ANIMATION_VIEW_DURATION);
        animators.add(objectAnimator);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animators);
        animatorSet.start();
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animator) {

            }

            @Override
            public void onAnimationEnd(@NonNull Animator animator) {

                if (mRootHeight != mOverlayView.getHeight()) {
                    mRootHeight = mOverlayView.getHeight();
                    mActionView.setY(mRootHeight - mActionHeight);
                }

                isOpenAnimationEnded = true;
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animator) {

            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animator) {

            }
        });
    }

    public void animationCloseConfirmView() {
        if (DEBUG) {
            Log.d(LOG_TAG, "animationCloseConfirmView");
        }

        if (isCloseAnimationEnded) {
            return;
        }

        int startValue = mRootHeight - mActionHeight;
        int endValue = mRootHeight;

        List<Animator> animators = new ArrayList<>();

        PropertyValuesHolder propertyValuesHolder = PropertyValuesHolder.ofFloat(View.Y, startValue, endValue);

        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(mActionView, propertyValuesHolder);
        objectAnimator.setDuration(Design.ANIMATION_VIEW_DURATION);
        animators.add(objectAnimator);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animators);
        animatorSet.start();
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animator) {

            }

            @Override
            public void onAnimationEnd(@NonNull Animator animator) {

                isCloseAnimationEnded = true;
                mOverlayView.setAlpha(0f);

                if (mObserver != null) {
                    mObserver.onCloseViewAnimationEnd();
                }
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animator) {

            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animator) {

            }
        });
    }

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        mOverlayView = findViewById(R.id.alert_message_view_overlay_view);
        mActionView = findViewById(R.id.alert_message_view_action_view);
        mSlideMarkView = findViewById(R.id.alert_message_view_slide_mark_view);
        mTitleView = findViewById(R.id.alert_message_view_title_view);
        mMessageView = findViewById(R.id.alert_message_view_message_view);
        mConfirmView = findViewById(R.id.alert_message_view_confirm_view);
        mConfirmTextView = findViewById(R.id.alert_message_view_confirm_text_view);

        mOverlayView.setBackgroundColor(Design.OVERLAY_VIEW_COLOR);
        mOverlayView.setAlpha(0);
        mOverlayView.setOnClickListener(v -> onDismissClick());

        mActionView.setY(Design.DISPLAY_HEIGHT);

        float radius = Design.ACTION_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, 0, 0, 0, 0};

        ShapeDrawable scrollIndicatorBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        scrollIndicatorBackground.getPaint().setColor(Design.POPUP_BACKGROUND_COLOR);
        mActionView.setBackground(scrollIndicatorBackground);

        ViewGroup.LayoutParams layoutParams = mSlideMarkView.getLayoutParams();
        layoutParams.height = Design.SLIDE_MARK_HEIGHT;

        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.mutate();
        gradientDrawable.setColor(Color.rgb(244, 244, 244));
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        mSlideMarkView.setBackground(gradientDrawable);

        float corner = ((float)Design.SLIDE_MARK_HEIGHT / 2) * Resources.getSystem().getDisplayMetrics().density;
        gradientDrawable.setCornerRadius(corner);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mSlideMarkView.getLayoutParams();
        marginLayoutParams.topMargin = Design.SLIDE_MARK_TOP_MARGIN;

        Design.updateTextFont(mTitleView, Design.FONT_BOLD44);
        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mTitleView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_TITLE_MARGIN * Design.HEIGHT_RATIO);

        Design.updateTextFont(mMessageView, Design.FONT_MEDIUM40);
        mMessageView.setTextColor(Design.FONT_COLOR_GREY);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mMessageView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_MESSAGE_MARGIN * Design.HEIGHT_RATIO);

        mConfirmView.setOnClickListener(v -> onConfirmClick());

        radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable confirmViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        confirmViewBackground.getPaint().setColor(Design.getMainStyle());
        mConfirmView.setBackground(confirmViewBackground);

        layoutParams = mConfirmView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;

        mConfirmView.setMinimumHeight(Design.BUTTON_HEIGHT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mConfirmView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_CONFIRM_TOP_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_CONFIRM_BOTTOM_MARGIN * Design.HEIGHT_RATIO);

        Design.updateTextFont(mConfirmTextView, Design.FONT_BOLD36);
        mConfirmTextView.setTextColor(Color.WHITE);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mConfirmTextView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_CONFIRM_VERTICAL_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_CONFIRM_VERTICAL_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.leftMargin = (int) (DESIGN_CONFIRM_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_CONFIRM_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
    }

    private void updateViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateViews");
        }

        if (mTitle != null && mTitleView != null) {
            mTitleView.setText(mTitle);
        }

        if (mMessage != null && mMessageView != null) {
            mMessageView.setText(mMessage);
        }

        if (mConfirmTitle != null && mConfirmTextView != null) {
            mConfirmTextView.setText(mConfirmTitle);
        }

        float radius = Design.ACTION_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, 0, 0, 0, 0};
        ShapeDrawable scrollIndicatorBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));

        if (mForceDarkMode) {
            scrollIndicatorBackground.getPaint().setColor(Color.rgb(72,72,72));
            mTitleView.setTextColor(Color.WHITE);
        } else {
            scrollIndicatorBackground.getPaint().setColor(Design.POPUP_BACKGROUND_COLOR);
            mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);
        }

        mActionView.setBackground(scrollIndicatorBackground);

    }

    private void onConfirmClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onConfirmClick");
        }

        if (mObserver != null) {
            mObserver.onConfirmClick();
        }
    }

    private void onDismissClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDismissClick");
        }

        if (mObserver != null) {
            mObserver.onDismissClick();
        }
    }

    private void showAlertView() {
        if (DEBUG) {
            Log.d(LOG_TAG, "showAlertView");
        }

        isOpenAnimationEnded = false;
        isCloseAnimationEnded = false;

        mActionView.setY(Design.DISPLAY_HEIGHT);
        mActionView.invalidate();
        animationOpenConfirmView();
    }
}
