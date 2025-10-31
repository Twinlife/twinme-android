/*
 *  Copyright (c) 2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.contacts;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.percentlayout.widget.PercentRelativeLayout;

import com.airbnb.lottie.LottieAnimationView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.CircularImageDescriptor;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.CircularImageView;

import java.util.ArrayList;
import java.util.List;

public class SuccessAuthentifiedRelationView extends PercentRelativeLayout {
    private static final String LOG_TAG = "SuccessAuthentified...";
    private static final boolean DEBUG = false;

    public interface SuccessAuthentifiedRelationListener {

        void onCloseSuccessAuthentifiedRelationView();
    }

    private static final int DESIGN_CONTENT_VIEW_WIDTH = 680;
    private static final int DESIGN_CONTENT_VIEW_HEIGHT = 540;
    private static final int DESIGN_CONTENT_VIEW_MARGIN = 80;
    private static final int DESIGN_ANIMATION_VIEW_MARGIN = 40;
    private static final int DESIGN_AVATAR_MARGIN = 88;
    private static final int DESIGN_AVATAR_SIZE = 104;
    private static final int DESIGN_ICON_SIZE = 34;
    private static final int DESIGN_ICON_MARGIN = 20;
    private static final int DESIGN_ACTION_MARGIN = 34;
    private static final int DESIGN_ACTION_BOTTOM_MARGIN = 20;
    private static final int DESIGN_CLOSE_HEIGHT = 52;
    private static final int DESIGN_CLOSE_TOP_MARGIN = 18;

    private TextView mNameView;
    private TextView mMessageView;
    private CircularImageView mAvatarView;
    private LottieAnimationView mAnimationView;

    private SuccessAuthentifiedRelationListener mSuccessAuthentifiedRelationListener;

    public SuccessAuthentifiedRelationView(Context context) {

        super(context);
    }

    public SuccessAuthentifiedRelationView(Context context, AttributeSet attrs) {

        super(context, attrs);

        try {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = inflater.inflate(R.layout.success_authentified_relation_view, (ViewGroup) getParent());
            //noinspection deprecation
            view.setLayoutParams(new PercentRelativeLayout.LayoutParams(PercentRelativeLayout.LayoutParams.MATCH_PARENT, PercentRelativeLayout.LayoutParams.MATCH_PARENT));
            addView(view);
            initViews();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SuccessAuthentifiedRelationView(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs, defStyle);
    }

    public void setInfo(Context context, String name, Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setInfo");
        }

        if (avatar != null) {
            mAvatarView.setImage(context, null,
                    new CircularImageDescriptor(avatar, 0.5f, 0.5f, 0.5f));
        }

        if (name != null) {
            mNameView.setText(name);
            mMessageView.setText(String.format(context.getString(R.string.authentified_relation_activity_certified_message), name));
        }
    }

    public void setSuccessAuthentifiedRelationListener(SuccessAuthentifiedRelationListener successAuthentifiedRelationListener) {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        mSuccessAuthentifiedRelationListener = successAuthentifiedRelationListener;
    }

    public void startSuccessAnimation() {
        if (DEBUG) {
            Log.d(LOG_TAG, "startSuccessAnimation");
        }

        mAnimationView.setVisibility(VISIBLE);
        if (!mAnimationView.isAnimating()) {
            mAnimationView.playAnimation();
        }
    }

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        setBackgroundColor(Design.OVERLAY_VIEW_COLOR);
        setOnClickListener(v -> {});

        View contentView = findViewById(R.id.success_authentified_relation_view_content_view);

        MarginLayoutParams marginLayoutParams = (MarginLayoutParams) contentView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_CONTENT_VIEW_MARGIN * Design.HEIGHT_RATIO);

        ViewGroup.LayoutParams layoutParams = contentView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_CONTENT_VIEW_WIDTH * Design.WIDTH_RATIO);
        layoutParams.height = (int) (DESIGN_CONTENT_VIEW_HEIGHT * Design.HEIGHT_RATIO);

        float radius = Design.POPUP_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable popupViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        popupViewBackground.getPaint().setColor(Design.POPUP_BACKGROUND_COLOR);
        contentView.setBackground(popupViewBackground);

        mAvatarView = findViewById(R.id.success_authentified_relation_view_avatar_view);

        layoutParams = mAvatarView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_AVATAR_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_AVATAR_SIZE * Design.HEIGHT_RATIO);

        marginLayoutParams = (MarginLayoutParams) mAvatarView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_AVATAR_MARGIN * Design.HEIGHT_RATIO);

        mNameView = findViewById(R.id.success_authentified_relation_view_name_view);
        Design.updateTextFont(mNameView, Design.FONT_MEDIUM34);
        mNameView.setTextColor(Design.FONT_COLOR_DEFAULT);

        ImageView iconCertifiedView = findViewById(R.id.success_authentified_relation_view_certified_image_view);

        layoutParams = iconCertifiedView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_ICON_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_ICON_SIZE * Design.HEIGHT_RATIO);

        marginLayoutParams = (MarginLayoutParams) iconCertifiedView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_ICON_MARGIN * Design.WIDTH_RATIO);

        mMessageView = findViewById(R.id.success_authentified_relation_view_message_view);
        Design.updateTextFont(mMessageView, Design.FONT_REGULAR32);
        mMessageView.setTextColor(Design.FONT_COLOR_DESCRIPTION);

        View confirmClickableView = findViewById(R.id.success_authentified_relation_view_confirm_view);
        confirmClickableView.setOnClickListener(v -> onCloseClick());

        marginLayoutParams = (MarginLayoutParams) confirmClickableView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_ACTION_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_ACTION_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_ACTION_BOTTOM_MARGIN * Design.HEIGHT_RATIO);

        ShapeDrawable sendViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        sendViewBackground.getPaint().setColor(Design.BLUE_NORMAL);
        confirmClickableView.setBackground(sendViewBackground);

        layoutParams = confirmClickableView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = Design.BUTTON_HEIGHT;

        TextView sendTextView = findViewById(R.id.success_authentified_relation_view_confirm_text_view);
        Design.updateTextFont(sendTextView, Design.FONT_BOLD28);
        sendTextView.setTextColor(Color.WHITE);

        View closeView = findViewById(R.id.success_authentified_relation_view_close_view);
        closeView.setOnClickListener(view -> onCloseClick());

        layoutParams = closeView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_CLOSE_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) closeView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_CLOSE_TOP_MARGIN * Design.HEIGHT_RATIO);

        mAnimationView = findViewById(R.id.success_authentified_relation_view_animation_view);
        mAnimationView.setVisibility(INVISIBLE);

        layoutParams = mAnimationView.getLayoutParams();
        layoutParams.width = Design.DISPLAY_WIDTH;
        layoutParams.height = (int) (Design.DISPLAY_HEIGHT * 0.5);

        marginLayoutParams = (MarginLayoutParams) mAnimationView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_ANIMATION_VIEW_MARGIN * Design.HEIGHT_RATIO);

        mAnimationView.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animation) {
                mAnimationView.setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationEnd(@NonNull Animator animation) {

                PropertyValuesHolder propertyValuesHolderAlpha = PropertyValuesHolder.ofFloat(View.ALPHA, 1, 0);
                ObjectAnimator alphaAnimator = ObjectAnimator.ofPropertyValuesHolder(mAnimationView, propertyValuesHolderAlpha);

                List<Animator> animators = new ArrayList<>();
                animators.add(alphaAnimator);

                AnimatorSet animationSet = new AnimatorSet();
                animationSet.setDuration(2000);
                animationSet.setInterpolator(new DecelerateInterpolator());
                animationSet.playTogether(animators);
                animationSet.start();
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animation) {

            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animation) {

            }
        });
    }

    private void onCloseClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCloseClick");
        }

        mSuccessAuthentifiedRelationListener.onCloseSuccessAuthentifiedRelationView();
    }
}