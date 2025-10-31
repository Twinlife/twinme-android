/*
 *  Copyright (c) 2020 twinlife SA.
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
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.percentlayout.widget.PercentRelativeLayout;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

@SuppressWarnings("deprecation")
public class CallAnimationView extends PercentRelativeLayout {
    private static final String LOG_TAG = "CallAnimationView";
    private static final boolean DEBUG = false;

    private static final long ANIMATION_DURATION = 1000;
    private static final long ANIMATION_RESTART_DELAY = 1100;
    private static final long ANIMATION_RED_CIRCLE_DELAY = 800;
    private static final long ANIMATION_GREEN_CIRCLE_DELAY = 1000;
    private static final long ANIMATION_BLUE_CIRCLE_DELAY = 1100;
    private static final int DESIGN_RED_CIRCLE_COLOR = Color.argb(255, 255, 30, 92);
    private static final int DESIGN_GREEN_CIRCLE_COLOR = Color.argb(255, 105, 221, 198);
    private static final int DESIGN_BLUE_CIRCLE_COLOR = Color.argb(255, 14, 178, 254);
    private static final float BORDER_WIDTH = 2.f;

    private RoundedView mRedRoundedView;
    private RoundedView mGreenRoundedView;
    private RoundedView mBlueRoundedView;

    private AnimatorSet mAnimatorSet;

    public CallAnimationView(Context context) {
        super(context);
    }

    public CallAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (DEBUG) {
            Log.d(LOG_TAG, "create");
        }

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            View view = inflater.inflate(R.layout.abstract_call_activity_animation_view, (ViewGroup) getParent());
            view.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
            addView(view);
            view.setBackgroundColor(Color.TRANSPARENT);
        }

        int roundSize = Design.DISPLAY_WIDTH;

        mRedRoundedView = findViewById(R.id.abstract_call_activity_red_rounded_view);
        ViewGroup.LayoutParams layoutParams = mRedRoundedView.getLayoutParams();
        layoutParams.width = roundSize;
        layoutParams.height = roundSize;

        mRedRoundedView.setBorder(BORDER_WIDTH, DESIGN_RED_CIRCLE_COLOR);
        mRedRoundedView.setColor(Color.TRANSPARENT);
        mRedRoundedView.setScaleX(0.0f);
        mRedRoundedView.setScaleY(0.0f);

        mGreenRoundedView = findViewById(R.id.abstract_call_activity_green_rounded_view);
        layoutParams = mGreenRoundedView.getLayoutParams();
        layoutParams.width = roundSize;
        layoutParams.height = roundSize;

        mGreenRoundedView.setBorder(BORDER_WIDTH, DESIGN_GREEN_CIRCLE_COLOR);
        mGreenRoundedView.setColor(Color.TRANSPARENT);
        mGreenRoundedView.setScaleX(0.0f);
        mGreenRoundedView.setScaleY(0.0f);

        mBlueRoundedView = findViewById(R.id.abstract_call_activity_blue_rounded_view);
        layoutParams = mBlueRoundedView.getLayoutParams();
        layoutParams.width = roundSize;
        layoutParams.height = roundSize;

        mBlueRoundedView.setBorder(BORDER_WIDTH, DESIGN_BLUE_CIRCLE_COLOR);
        mBlueRoundedView.setColor(Color.TRANSPARENT);
        mBlueRoundedView.setScaleX(0.0f);
        mBlueRoundedView.setScaleY(0.0f);
    }

    public void animationRound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "animationRound");
        }

        PropertyValuesHolder propertyValuesHolderX = PropertyValuesHolder.ofFloat(View.SCALE_X, 0.0f, 2.0f);
        PropertyValuesHolder propertyValuesHolderY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.0f, 2.0f);
        PropertyValuesHolder propertyValuesAlpha = PropertyValuesHolder.ofFloat(View.ALPHA, 1.0f, 0.0f);

        ObjectAnimator scaleRedViewAnimator = ObjectAnimator.ofPropertyValuesHolder(mRedRoundedView, propertyValuesHolderX, propertyValuesHolderY, propertyValuesAlpha);
        scaleRedViewAnimator.setDuration(ANIMATION_DURATION);
        scaleRedViewAnimator.setRepeatCount(0);
        scaleRedViewAnimator.setInterpolator(new DecelerateInterpolator());
        scaleRedViewAnimator.setStartDelay(ANIMATION_RED_CIRCLE_DELAY);

        ObjectAnimator scaleGreenViewAnimator = ObjectAnimator.ofPropertyValuesHolder(mGreenRoundedView, propertyValuesHolderX, propertyValuesHolderY, propertyValuesAlpha);
        scaleGreenViewAnimator.setDuration(ANIMATION_DURATION);
        scaleGreenViewAnimator.setRepeatCount(0);
        scaleGreenViewAnimator.setInterpolator(new DecelerateInterpolator());
        scaleGreenViewAnimator.setStartDelay(ANIMATION_GREEN_CIRCLE_DELAY);

        ObjectAnimator scaleBlueViewAnimator = ObjectAnimator.ofPropertyValuesHolder(mBlueRoundedView, propertyValuesHolderX, propertyValuesHolderY, propertyValuesAlpha);
        scaleBlueViewAnimator.setDuration(ANIMATION_DURATION);
        scaleBlueViewAnimator.setRepeatCount(0);
        scaleBlueViewAnimator.setInterpolator(new DecelerateInterpolator());
        scaleBlueViewAnimator.setStartDelay(ANIMATION_BLUE_CIRCLE_DELAY);

        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.playTogether(scaleRedViewAnimator, scaleGreenViewAnimator, scaleBlueViewAnimator);
        mAnimatorSet.start();

        mAnimatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animator) {
                if (DEBUG) {
                    Log.d(LOG_TAG, "onAnimationStart animator=" + animator);
                }
            }

            @Override
            public void onAnimationEnd(@NonNull Animator animator) {
                if (DEBUG) {
                    Log.d(LOG_TAG, "onAnimationEnd animator=" + animator);
                }

                if (mAnimatorSet != null) {
                    mAnimatorSet.setStartDelay(ANIMATION_RESTART_DELAY);
                    mAnimatorSet.start();
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

    public void stopAnimation() {
        if (DEBUG) {
            Log.d(LOG_TAG, "stopAnimation");
        }

        if (mAnimatorSet != null) {
            mAnimatorSet.end();
            mAnimatorSet.cancel();
            mAnimatorSet = null;
            mRedRoundedView.clearAnimation();
            mGreenRoundedView.clearAnimation();
            mBlueRoundedView.clearAnimation();
            mRedRoundedView.setVisibility(GONE);
            mGreenRoundedView.setVisibility(GONE);
            mBlueRoundedView.setVisibility(GONE);
        }
    }
}
