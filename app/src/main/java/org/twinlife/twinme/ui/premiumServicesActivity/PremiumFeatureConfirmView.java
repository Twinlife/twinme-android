/*
 *  Copyright (c) 2024-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.premiumServicesActivity;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.AbstractBottomSheetView;

public class PremiumFeatureConfirmView extends AbstractBottomSheetView {
    private static final String LOG_TAG = "PremiumFeatureCon...";
    private static final boolean DEBUG = false;

    private static final int DESIGN_IMAGE_WIDTH = 400;
    private static final int DESIGN_IMAGE_HEIGHT = 240;

    private ImageView mPremiumFeatureImageView;

    public PremiumFeatureConfirmView(Context context) {
        super(context);
    }

    public PremiumFeatureConfirmView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (DEBUG) {
            Log.d(LOG_TAG, "create");
        }

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.premium_feature_confirm_view, this, true);
        initViews();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mPremiumFeatureImageView.setVisibility(GONE);
        } else {
            mPremiumFeatureImageView.setVisibility(VISIBLE);
        }

        show();
    }

    public void initWithPremiumFeature(UIPremiumFeature uiPremiumFeature) {
        if (DEBUG) {
            Log.d(LOG_TAG, "initWithPremiumFeature: " + uiPremiumFeature);
        }

        if (mTitleView != null) {
            mTitleView.setText(uiPremiumFeature.getTitle());
        }

        if (mPremiumFeatureImageView != null) {
            mPremiumFeatureImageView.setImageDrawable(ResourcesCompat.getDrawable(getContext().getResources(), uiPremiumFeature.getImageId(), null));
        }
    }

    public void redirectStore() {

        PremiumServicesActivity.redirectStore(getContext());
        animationCloseConfirmView();
    }

    public void hideOverlay() {
        if (DEBUG) {
            Log.d(LOG_TAG, "hideOverlay");
        }

        if (mOverlayView != null) {
            mOverlayView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    @Override
    protected void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        mOverlayView = findViewById(R.id.premium_feature_confirm_view_overlay_view);
        mActionView = findViewById(R.id.premium_feature_confirm_view_action_view);
        mSlideMarkView = findViewById(R.id.premium_feature_confirm_view_slide_mark_view);
        mPremiumFeatureImageView = findViewById(R.id.premium_feature_confirm_view_feature_image_view);
        mTitleView = findViewById(R.id.premium_feature_confirm_view_title_view);
        mMessageView = findViewById(R.id.premium_feature_confirm_view_message_view);
        mConfirmView = findViewById(R.id.premium_feature_confirm_view_confirm_view);
        mConfirmTextView = findViewById(R.id.premium_feature_confirm_view_confirm_text_view);
        mCancelView = findViewById(R.id.premium_feature_confirm_view_cancel_view);
        mCancelTextView = findViewById(R.id.premium_feature_confirm_view_cancel_text_view);

        super.initViews();

        mPremiumFeatureImageView.setMaxWidth((int) (DESIGN_IMAGE_WIDTH * Design.WIDTH_RATIO));
        mPremiumFeatureImageView.setMaxHeight((int) (DESIGN_IMAGE_HEIGHT * Design.HEIGHT_RATIO));

        int orientation = getContext().getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mPremiumFeatureImageView.setVisibility(GONE);
        } else {
            mPremiumFeatureImageView.setVisibility(VISIBLE);
        }

        MarginLayoutParams marginLayoutParams = (MarginLayoutParams) mPremiumFeatureImageView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_TITLE_MARGIN * Design.HEIGHT_RATIO);

        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable confirmViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        confirmViewBackground.getPaint().setColor(Design.getMainStyle());
        mConfirmView.setBackground(confirmViewBackground);

        TextView linkTextView = findViewById(R.id.premium_feature_confirm_view_link_view);
        Design.updateTextFont(linkTextView, Design.FONT_MEDIUM34);
        linkTextView.setTextColor(Design.getMainStyle());
        linkTextView.setPaintFlags(linkTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        linkTextView.setOnClickListener(view -> onPremiumServicesClick());

        ViewGroup.LayoutParams layoutParams = linkTextView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = Design.BUTTON_HEIGHT;

        marginLayoutParams = (MarginLayoutParams) linkTextView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_MESSAGE_MARGIN * Design.HEIGHT_RATIO);
    }

    private void onPremiumServicesClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPremiumServicesClick");
        }

        PremiumServicesActivity.redirectToPremiumServices(getContext());
        animationCloseConfirmView();
    }
}
