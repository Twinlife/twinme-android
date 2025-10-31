/*
 *  Copyright (c) 2022-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.inAppSubscriptionActivity;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.percentlayout.widget.PercentRelativeLayout;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class CustomProgressBarView extends PercentRelativeLayout {

    private static final int ANIMATION_DURATION = 5000;

    private static final int DESIGN_BACKGROUND_COLOR = Color.argb(76, 255, 32, 80);
    private static final int DESIGN_BACKGROUND_DARK_COLOR = Color.argb(76, 255, 255, 255);
    private static final int DESIGN_PROGRESS_COLOR = Color.argb(255, 255, 32, 80);

    public interface Observer {
        void onCustomProgressBarEndAnmation();
    }

    private final Runnable mEndAnimation = this::endAnimation;
    private View mProgressView;
    private int mWidth = 0;
    private int mHeight = 0;

    private ValueAnimator mValueAnimator;

    private Observer mObserver;

    public CustomProgressBarView(Context context) {

        super(context);
    }

    public CustomProgressBarView(Context context, AttributeSet attrs) {

        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            View view;
            view = inflater.inflate(R.layout.custom_progress_bar, (ViewGroup) getParent());
            view.setLayoutParams(new PercentRelativeLayout.LayoutParams(PercentRelativeLayout.LayoutParams.MATCH_PARENT, PercentRelativeLayout.LayoutParams.MATCH_PARENT));
            addView(view);
        }
    }

    public CustomProgressBarView(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs, defStyle);
    }

    public void setObserver(Observer observer) {

        mObserver = observer;
    }

    public void startAnimation() {

        final long started = System.currentTimeMillis();
        mValueAnimator = ValueAnimator.ofInt(0, mWidth);
        mValueAnimator.addUpdateListener(valueAnimator -> {
            int value = (Integer) valueAnimator.getAnimatedValue();
            ViewGroup.LayoutParams layoutParams = mProgressView.getLayoutParams();
            layoutParams.width = value;
            mProgressView.setLayoutParams(layoutParams);

            if (mWidth == value && mObserver != null) {
                // If the user disable the Android animation, we are called immediately.
                // Make sure we don't switch to the next panel before the 5s animation duration.
                long delay = started + ANIMATION_DURATION - System.currentTimeMillis();
                if (delay > 0) {
                    postDelayed(mEndAnimation, delay);
                } else {
                    endAnimation();
                }
            }
        });
        mValueAnimator.setDuration(ANIMATION_DURATION);
        mValueAnimator.start();
    }

    public void stopAnimation() {

        if (mValueAnimator != null && mValueAnimator.isRunning()) {
            mValueAnimator.cancel();
        }
        removeCallbacks(mEndAnimation);

        if (mProgressView != null) {
            ViewGroup.LayoutParams layoutParams = mProgressView.getLayoutParams();
            layoutParams.width = mWidth;
            mProgressView.setLayoutParams(layoutParams);
        }
    }

    public void resetAnimation() {

        if (mProgressView != null) {
            ViewGroup.LayoutParams layoutParams = mProgressView.getLayoutParams();
            layoutParams.width = 0;
            mProgressView.setLayoutParams(layoutParams);
        }
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {

        mWidth = width;
        mHeight = height;

        if (mProgressView == null) {
            initViews();
        }

        super.onSizeChanged(width, height, oldWidth, oldHeight);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(@NonNull Canvas canvas) {

        super.onDraw(canvas);
    }

    //
    // Private Methods
    //

    private void initViews() {

        View backgroundView = findViewById(R.id.custom_progress_bar_background);

        float radius = mHeight * 0.5f;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable backgroundViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        if (Design.isDarkMode(backgroundView.getContext())) {
            backgroundViewBackground.getPaint().setColor(DESIGN_BACKGROUND_DARK_COLOR);
        } else {
            backgroundViewBackground.getPaint().setColor(DESIGN_BACKGROUND_COLOR);
        }
        backgroundView.setBackground(backgroundViewBackground);

        mProgressView = findViewById(R.id.custom_progress_bar_progress);

        ShapeDrawable progressViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        progressViewBackground.getPaint().setColor(DESIGN_PROGRESS_COLOR);
        mProgressView.setBackground(progressViewBackground);
    }

    private void endAnimation() {

        if (mObserver != null) {
            mObserver.onCustomProgressBarEndAnmation();
        }
    }
}