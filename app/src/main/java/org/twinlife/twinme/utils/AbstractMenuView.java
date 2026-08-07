/*
 *  Copyright (c) 2026 twinlife SA.
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
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import org.twinlife.twinme.skin.Design;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMenuView extends RelativeLayout {
    private static final String LOG_TAG = "AbstractMenuView";
    private static final boolean DEBUG = false;

    public interface AbstractMenuViewObserver {

        void onCloseAbstractMenuViewAnimationEnd();
    }

    protected View mOverlayView;
    protected View mActionView;

    private int mRootHeight = 0;
    private int mActionHeight = 0;

    private boolean isOpenAnimationEnded = false;
    private boolean isCloseAnimationEnded = false;

    public AbstractMenuView(Context context) {
        super(context);
    }

    public AbstractMenuView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void updateHeight(int height) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateHeight");
        }

        mActionHeight = height;
        mActionView.setY(mRootHeight - mActionHeight);
    }

    public void openMenu() {
        if (DEBUG) {
            Log.d(LOG_TAG, "openMenu");
        }

        isOpenAnimationEnded = false;
        isCloseAnimationEnded = false;

        ViewTreeObserver viewTreeObserver = mActionView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewTreeObserver viewTreeObserver = mActionView.getViewTreeObserver();
                viewTreeObserver.removeOnGlobalLayoutListener(this);

                mRootHeight = mOverlayView.getHeight();
                mActionHeight = getActionViewHeight();

                mActionView.setY(Design.DISPLAY_HEIGHT);
                mActionView.invalidate();
                animationOpenMenu();
            }
        });
    }

    public void animationOpenMenu() {
        if (DEBUG) {
            Log.d(LOG_TAG, "animationOpenMenu");
        }

        if (isOpenAnimationEnded) {
            return;
        }

        mOverlayView.setAlpha(1.0f);

        int startValue = mRootHeight;
        int endValue = mRootHeight - mActionHeight;

        PropertyValuesHolder propertyValuesHolder = PropertyValuesHolder.ofFloat(View.Y, startValue, endValue);

        List<Animator> animators = new ArrayList<>();
        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(mActionView, propertyValuesHolder);
        objectAnimator.setDuration(Design.ANIMATION_VIEW_DURATION);
        animators.add(objectAnimator);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(animators);
        animatorSet.start();
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animator) {

            }

            @Override
            public void onAnimationEnd(@NonNull Animator animator) {

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

    public void dismiss() {
        if (DEBUG) {
            Log.d(LOG_TAG, "dismiss");
        }

        onDismissClick();
    }

    public void animationCloseMenu() {
        if (DEBUG) {
            Log.d(LOG_TAG, "animationCloseMenu");
        }

        if (isCloseAnimationEnded) {
            return;
        }

        int startValue = mRootHeight - mActionHeight;
        int endValue = mRootHeight;

        PropertyValuesHolder propertyValuesHolder = PropertyValuesHolder.ofFloat(View.Y, startValue, endValue);

        List<Animator> animators = new ArrayList<>();
        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(mActionView, propertyValuesHolder);
        objectAnimator.setDuration(Design.ANIMATION_VIEW_DURATION);
        animators.add(objectAnimator);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(animators);
        animatorSet.start();
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animator) {

            }

            @Override
            public void onAnimationEnd(@NonNull Animator animator) {

                mOverlayView.setAlpha(0f);

                isCloseAnimationEnded = true;
                getObserver().onCloseAbstractMenuViewAnimationEnd();
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animator) {

            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animator) {

            }
        });
    }

    public abstract int getActionViewHeight();

    public abstract AbstractMenuViewObserver getObserver();

    protected abstract void initViews();

    protected void onDismissClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDismissClick");
        }

        animationCloseMenu();
    }
}
