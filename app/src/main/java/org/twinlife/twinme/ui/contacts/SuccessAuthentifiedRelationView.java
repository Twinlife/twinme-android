/*
 *  Copyright (c) 2024-2025 twinlife SA.
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
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.airbnb.lottie.LottieAnimationView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.AbstractBottomSheetView;

import java.util.ArrayList;
import java.util.List;

public class SuccessAuthentifiedRelationView extends AbstractBottomSheetView {
    private static final String LOG_TAG = "SuccessAuthentified...";
    private static final boolean DEBUG = false;

    private static final int ANIMATION_DURATION = 2000;
    private static final int DESIGN_ANIMATION_VIEW_MARGIN = 40;
    private static final int DESIGN_ICON_SIZE = 34;
    private static final int DESIGN_ICON_MARGIN = 20;

    private LottieAnimationView mAnimationView;

    public SuccessAuthentifiedRelationView(Context context) {

        super(context);
    }

    public SuccessAuthentifiedRelationView(Context context, AttributeSet attrs) {

        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.success_authentified_relation_view, this, true);
        initViews();
    }

    @Override
    protected void onFinishOpenAnimation() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onFinishOpenAnimation");
        }

        startSuccessAnimation();
    }

    @Override
    protected void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        mOverlayView = findViewById(R.id.success_authentified_relation_view_overlay_view);
        mActionView = findViewById(R.id.success_authentified_relation_view_action_view);
        mSlideMarkView = findViewById(R.id.success_authentified_relation_view_slide_mark_view);
        mAvatarView = findViewById(R.id.success_authentified_relation_view_avatar_view);
        mTitleView = findViewById(R.id.success_authentified_relation_view_name_view);
        mMessageView = findViewById(R.id.success_authentified_relation_view_message_view);
        mConfirmView = findViewById(R.id.success_authentified_relation_view_confirm_view);
        mConfirmTextView = findViewById(R.id.success_authentified_relation_view_confirm_text_view);

        super.initViews();

        ImageView iconCertifiedView = findViewById(R.id.success_authentified_relation_view_certified_image_view);

        ViewGroup.LayoutParams layoutParams = iconCertifiedView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_ICON_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_ICON_SIZE * Design.HEIGHT_RATIO);

        MarginLayoutParams marginLayoutParams = (MarginLayoutParams) iconCertifiedView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_ICON_MARGIN * Design.WIDTH_RATIO);

        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable confirmViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        confirmViewBackground.getPaint().setColor(Design.getMainStyle());
        mConfirmView.setBackground(confirmViewBackground);

        marginLayoutParams = (MarginLayoutParams) mConfirmView.getLayoutParams();
        marginLayoutParams.bottomMargin = (int) (DESIGN_CONFIRM_MARGIN * Design.HEIGHT_RATIO);

        marginLayoutParams = (MarginLayoutParams) iconCertifiedView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_ICON_MARGIN * Design.WIDTH_RATIO);

        mAnimationView = findViewById(R.id.success_authentified_relation_view_animation_view);
        mAnimationView.setVisibility(INVISIBLE);

        layoutParams = mAnimationView.getLayoutParams();
        layoutParams.width = Design.DISPLAY_WIDTH;
        layoutParams.height = (int) (Design.DISPLAY_HEIGHT * 0.5);

        marginLayoutParams = (MarginLayoutParams) mAnimationView.getLayoutParams();
        marginLayoutParams.bottomMargin = (int) (DESIGN_ANIMATION_VIEW_MARGIN * Design.HEIGHT_RATIO);

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
                animationSet.setDuration(ANIMATION_DURATION);
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

    private void startSuccessAnimation() {
        if (DEBUG) {
            Log.d(LOG_TAG, "startSuccessAnimation");
        }

        mAnimationView.setVisibility(VISIBLE);
        if (!mAnimationView.isAnimating()) {
            mAnimationView.playAnimation();
        }
    }
}