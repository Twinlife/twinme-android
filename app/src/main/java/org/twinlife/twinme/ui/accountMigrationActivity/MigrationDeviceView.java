/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.accountMigrationActivity;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.android.material.card.MaterialCardView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class MigrationDeviceView extends RelativeLayout {
    private static final String LOG_TAG = "MigrationDeviceView";
    private static final boolean DEBUG = false;

    private static final float DESIGN_NOTCH_WIDTH = 40f;
    private static final float DESIGN_NOTCH_HEIGHT = 6f;
    private static final float DESIGN_NOTCH_MARGIN = 20f;

    private GradientDrawable mGradientProgressDrawable;
    private View mProgressView;
    private MaterialCardView mDeviceView;

    private boolean mInitColors = false;

    public MigrationDeviceView(Context context) {

        super(context);
    }

    public MigrationDeviceView(Context context, AttributeSet attrs) {

        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.account_migration_device_view, this, true);
        initViews();
    }

    public MigrationDeviceView(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs, defStyle);
    }

    public void updateColors(int backgroundColor, int progressColor) {

        if (!mInitColors && mDeviceView != null && mProgressView != null) {
            mInitColors = true;
            mDeviceView.setCardBackgroundColor(backgroundColor);
            mGradientProgressDrawable.setColor(progressColor);

            ViewGroup.LayoutParams layoutParams = mProgressView.getLayoutParams();
            layoutParams.height = getHeight();
        }
    }

    public void updateProgress(float percent) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateProgress: percent=" + percent);
        }

        animateProgressViewSize(1 - percent);
    }

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        mDeviceView = findViewById(R.id.account_migration_device_view_device);
        mDeviceView.setCardBackgroundColor(Design.GREY_BACKGROUND_COLOR);
        mDeviceView.setStrokeColor(Design.BLACK_COLOR);
        mDeviceView.setStrokeWidth(4);
        mDeviceView.setRadius(Design.POPUP_RADIUS);
        mDeviceView.setElevation(1);

        View notchView = findViewById(R.id.account_migration_device_view_notch_view);
        notchView.setElevation(2);

        ViewGroup.LayoutParams layoutParams = notchView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_NOTCH_WIDTH * Design.WIDTH_RATIO);
        layoutParams.height = (int) (DESIGN_NOTCH_HEIGHT * Design.HEIGHT_RATIO);

        MarginLayoutParams marginLayoutParams = (MarginLayoutParams) notchView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_NOTCH_MARGIN * Design.HEIGHT_RATIO);

        float corner = ((float)Design.SLIDE_MARK_HEIGHT / 2) * Resources.getSystem().getDisplayMetrics().density;
        GradientDrawable notchGradientDrawable = new GradientDrawable();
        notchGradientDrawable.setColor(Design.BLACK_COLOR);
        notchGradientDrawable.setCornerRadius(corner);
        notchView.setBackground(notchGradientDrawable);

        mProgressView = findViewById(R.id.account_migration_device_view_progress_view);

        mGradientProgressDrawable = new GradientDrawable();
        mGradientProgressDrawable.setColor(Color.TRANSPARENT);
        mProgressView.setBackground(mGradientProgressDrawable);
    }

    private void animateProgressViewSize(float percent) {

        int targetSize = (int) (getHeight() * percent);

        ViewGroup.LayoutParams layoutParams = mProgressView.getLayoutParams();
        int startSize = layoutParams.height;

        ValueAnimator animator = ValueAnimator.ofInt(startSize, targetSize);
        animator.setDuration(200);
        animator.addUpdateListener(animation -> {
            int size = (int) animation.getAnimatedValue();
            ViewGroup.LayoutParams params = mProgressView.getLayoutParams();
            params.height = size;
            mProgressView.setLayoutParams(params);
        });
        animator.start();
    }
}