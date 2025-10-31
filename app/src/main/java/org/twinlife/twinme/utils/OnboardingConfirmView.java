/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.graphics.text.LineBreaker;
import android.os.Build;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import androidx.percentlayout.widget.PercentRelativeLayout;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class OnboardingConfirmView extends AbstractConfirmView {

    private static final String LOG_TAG = "OnboardingConfirmView";
    private static final boolean DEBUG = false;

    private static final int DESIGN_IMAGE_WIDTH = 400;
    private static final int DESIGN_IMAGE_HEIGHT = 240;
    private static final int DESIGN_TITLE_TOP_MARGIN = 40;
    private static final int DESIGN_TITLE_BOTTOM_MARGIN = 40;
    private static final int DESIGN_MESSAGE_HORIZONTAL_MARGIN = 52;
    private static final int DESIGN_MIN_HEIGHT = 618;

    protected View mRootView;
    private ImageView mImageView;


    public OnboardingConfirmView(Context context) {
        super(context);
    }

    public OnboardingConfirmView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (DEBUG) {
            Log.d(LOG_TAG, "create");
        }

        try {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            mRootView = inflater.inflate(R.layout.onboarding_confirm_view, null);
            addView(mRootView);

            initViews();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setImage(Drawable drawable) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setImage: " + drawable);
        }

        if (mImageView != null) {
            if (drawable != null) {
                mImageView.setImageDrawable(drawable);
            } else {
                mImageView.setVisibility(GONE);
            }
        }
    }

    public void hideCancelView() {
        if (DEBUG) {
            Log.d(LOG_TAG, "hideCancelView");
        }

        if (mCancelView != null) {
            mCancelView.setVisibility(GONE);

            MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mConfirmView.getLayoutParams();
            marginLayoutParams.bottomMargin = (int) (DESIGN_CONFIRM_MARGIN * Design.HEIGHT_RATIO);
        }
    }

    @Override
    protected void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        mOverlayView = mRootView.findViewById(R.id.onboarding_confirm_view_overlay_view);
        mActionView = mRootView.findViewById(R.id.onboarding_confirm_view_action_view);
        mSlideMarkView = mRootView.findViewById(R.id.onboarding_confirm_view_slide_mark_view);
        mTitleView = mRootView.findViewById(R.id.onboarding_confirm_view_title_view);
        mImageView = mRootView.findViewById(R.id.onboarding_confirm_view_feature_image_view);
        mMessageView = mRootView.findViewById(R.id.onboarding_confirm_view_message_view);
        mConfirmView = mRootView.findViewById(R.id.onboarding_confirm_view_confirm_view);
        mConfirmTextView = mRootView.findViewById(R.id.onboarding_confirm_view_confirm_text_view);
        mCancelView = mRootView.findViewById(R.id.onboarding_confirm_view_cancel_view);
        mCancelTextView = mRootView.findViewById(R.id.onboarding_confirm_view_cancel_text_view);

        super.initViews();

        Design.updateTextFont(mTitleView, Design.FONT_MEDIUM36);

        ViewTreeObserver viewTreeObserver = mTitleView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewTreeObserver viewTreeObserver = mTitleView.getViewTreeObserver();
                viewTreeObserver.removeGlobalOnLayoutListener(this);

                float messageMaxHeight = mOverlayView.getHeight() - (DESIGN_MIN_HEIGHT * Design.HEIGHT_RATIO) - mTitleView.getHeight();
                mMessageView.setMaxHeight((int) messageMaxHeight);
            }
        });

        MarginLayoutParams marginLayoutParams = (MarginLayoutParams) mTitleView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_TITLE_TOP_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.leftMargin = (int) (DESIGN_MESSAGE_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_MESSAGE_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);

        mMessageView.setMovementMethod(new ScrollingMovementMethod());

        float messageMaxHeight = Design.DISPLAY_HEIGHT - (DESIGN_MIN_HEIGHT * Design.HEIGHT_RATIO);
        mMessageView.setMaxHeight((int) messageMaxHeight);

        Design.updateTextFont(mMessageView, Design.FONT_MEDIUM32);
        mMessageView.setTextColor(Design.FONT_COLOR_DEFAULT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mMessageView.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
        }

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mMessageView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_MESSAGE_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_MESSAGE_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);

        ViewGroup.LayoutParams layoutParams = mImageView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_IMAGE_WIDTH * Design.WIDTH_RATIO);
        layoutParams.height = (int) (DESIGN_IMAGE_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (MarginLayoutParams) mImageView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_TITLE_BOTTOM_MARGIN * Design.HEIGHT_RATIO);

        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable confirmViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        confirmViewBackground.getPaint().setColor(Design.getMainStyle());
        mConfirmView.setBackground(confirmViewBackground);
    }
}
