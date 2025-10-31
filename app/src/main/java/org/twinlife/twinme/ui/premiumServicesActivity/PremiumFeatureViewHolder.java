/*
 *  Copyright (c) 2023-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.premiumServicesActivity;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class PremiumFeatureViewHolder extends RecyclerView.ViewHolder {
    private static final String LOG_TAG = "PremiumFeatureVi...";
    private static final boolean DEBUG = false;

    private static final float DESIGN_ITEM_HEIGHT = 1108f;

    private static final float DESIGN_CONTAINER_BORDER = 4f;

    private static final float DESIGN_IMAGE_WIDTH = 38f;
    private static final float DESIGN_IMAGE_HEIGHT = 34f;
    private static final float DESIGN_ICON_LEFT_MARGIN = 52f;
    private static final float DESIGN_ICON_RIGHT_MARGIN = 18f;

    private static final float DESIGN_PREMIUM_WIDTH = 410f;
    private static final float DESIGN_PREMIUM_HEIGHT = 320f;

    private static final float DESIGN_CONTAINER_MARGIN = 40f;
    private static final float DESIGN_CONTAINER_BOTTOM_MARGIN = 20f;

    private final ImageView mImageView;
    private final TextView mTitleView;
    private final TextView mSubTitleView;

    private final TextView mFeatureDetailOneTextView;
    private final TextView mFeatureDetailTwoTextView;
    private final TextView mFeatureDetailThreeTextView;
    private final TextView mFeatureDetailFourTextView;

    private final ImageView mFeatureDetailOneImageView;
    private final ImageView mFeatureDetailTwoImageView;
    private final ImageView mFeatureDetailThreeImageView;
    private final ImageView mFeatureDetailFourImageView;

    PremiumFeatureViewHolder(@NonNull View view, boolean showBorder) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int) (DESIGN_ITEM_HEIGHT * Design.HEIGHT_RATIO);
        view.setLayoutParams(layoutParams);

        View containerView = view.findViewById(R.id.premium_services_activity_item_container_view);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) containerView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_CONTAINER_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_CONTAINER_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_CONTAINER_BOTTOM_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_CONTAINER_MARGIN * Design.WIDTH_RATIO));

        if (showBorder) {
            float radius = 28 * Resources.getSystem().getDisplayMetrics().density;
            float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

            ShapeDrawable containerViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
            containerViewBackground.getPaint().setColor(Design.WHITE_COLOR);

            ShapeDrawable containerViewBorder = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
            containerViewBorder.getPaint().setColor(Design.BLACK_COLOR);
            containerViewBorder.getPaint().setStyle(Paint.Style.STROKE);
            containerViewBorder.getPaint().setStrokeWidth(DESIGN_CONTAINER_BORDER);

            LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{containerViewBackground, containerViewBorder});
            containerView.setBackground(layerDrawable);

            view.setBackgroundColor(Color.BLACK);
        } else {
            view.setBackgroundColor(Design.WHITE_COLOR);
        }

        mTitleView = view.findViewById(R.id.premium_services_activity_item_title_view);
        Design.updateTextFont(mTitleView, Design.FONT_MEDIUM34);
        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mSubTitleView = view.findViewById(R.id.premium_services_activity_item_subtitle_view);
        Design.updateTextFont(mSubTitleView, Design.FONT_MEDIUM32);
        mSubTitleView.setTextColor(Design.FONT_COLOR_DESCRIPTION);

        mImageView = view.findViewById(R.id.premium_services_activity_item_image_view);
        layoutParams = mImageView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_PREMIUM_WIDTH * Design.WIDTH_RATIO);
        layoutParams.height = (int) (DESIGN_PREMIUM_HEIGHT * Design.HEIGHT_RATIO);

        mFeatureDetailOneImageView = view.findViewById(R.id.premium_services_activity_item_description_part_one_image_view);
        layoutParams = mFeatureDetailOneImageView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_IMAGE_WIDTH * Design.WIDTH_RATIO);
        layoutParams.height = (int) (DESIGN_IMAGE_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mFeatureDetailOneImageView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_ICON_LEFT_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_ICON_RIGHT_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_ICON_LEFT_MARGIN * Design.WIDTH_RATIO));
        marginLayoutParams.setMarginEnd((int) (DESIGN_ICON_RIGHT_MARGIN * Design.WIDTH_RATIO));

        mFeatureDetailOneTextView = view.findViewById(R.id.premium_services_activity_item_description_part_one_text_view);
        Design.updateTextFont(mFeatureDetailOneTextView, Design.FONT_MEDIUM30);
        mFeatureDetailOneTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mFeatureDetailTwoImageView = view.findViewById(R.id.premium_services_activity_item_description_part_two_image_view);
        layoutParams = mFeatureDetailTwoImageView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_IMAGE_WIDTH * Design.WIDTH_RATIO);
        layoutParams.height = (int) (DESIGN_IMAGE_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mFeatureDetailTwoImageView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_ICON_LEFT_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_ICON_RIGHT_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_ICON_LEFT_MARGIN * Design.WIDTH_RATIO));
        marginLayoutParams.setMarginEnd((int) (DESIGN_ICON_RIGHT_MARGIN * Design.WIDTH_RATIO));

        mFeatureDetailTwoTextView = view.findViewById(R.id.premium_services_activity_item_description_part_two_text_view);
        Design.updateTextFont(mFeatureDetailTwoTextView, Design.FONT_MEDIUM30);
        mFeatureDetailTwoTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mFeatureDetailThreeImageView = view.findViewById(R.id.premium_services_activity_item_description_part_three_image_view);
        layoutParams = mFeatureDetailThreeImageView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_IMAGE_WIDTH * Design.WIDTH_RATIO);
        layoutParams.height = (int) (DESIGN_IMAGE_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mFeatureDetailThreeImageView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_ICON_LEFT_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_ICON_RIGHT_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_ICON_LEFT_MARGIN * Design.WIDTH_RATIO));
        marginLayoutParams.setMarginEnd((int) (DESIGN_ICON_RIGHT_MARGIN * Design.WIDTH_RATIO));

        mFeatureDetailThreeTextView = view.findViewById(R.id.premium_services_activity_item_description_part_three_text_view);
        Design.updateTextFont(mFeatureDetailThreeTextView, Design.FONT_MEDIUM30);
        mFeatureDetailThreeTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mFeatureDetailFourImageView = view.findViewById(R.id.premium_services_activity_item_description_part_four_image_view);
        layoutParams = mFeatureDetailFourImageView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_IMAGE_WIDTH * Design.WIDTH_RATIO);
        layoutParams.height = (int) (DESIGN_IMAGE_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mFeatureDetailFourImageView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_ICON_LEFT_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_ICON_RIGHT_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_ICON_LEFT_MARGIN * Design.WIDTH_RATIO));
        marginLayoutParams.setMarginEnd((int) (DESIGN_ICON_RIGHT_MARGIN * Design.WIDTH_RATIO));

        mFeatureDetailFourTextView = view.findViewById(R.id.premium_services_activity_item_description_part_four_text_view);
        Design.updateTextFont(mFeatureDetailFourTextView, Design.FONT_MEDIUM30);
        mFeatureDetailFourTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
    }

    public void onBind(Context context, UIPremiumFeature uiPremiumFeature) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBind: feature=" + uiPremiumFeature);
        }

        mTitleView.setText(uiPremiumFeature.getTitle());
        mSubTitleView.setText(uiPremiumFeature.getSubTitle());
        mImageView.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), uiPremiumFeature.getImageId(), null));

        if (uiPremiumFeature.getFeatureDetails().size() > 3) {
            UIPremiumFeatureDetail premiumFeatureDetail1 = uiPremiumFeature.getFeatureDetails().get(0);
            UIPremiumFeatureDetail premiumFeatureDetail2 = uiPremiumFeature.getFeatureDetails().get(1);
            UIPremiumFeatureDetail premiumFeatureDetail3 = uiPremiumFeature.getFeatureDetails().get(2);
            UIPremiumFeatureDetail premiumFeatureDetail4 = uiPremiumFeature.getFeatureDetails().get(3);

            mFeatureDetailOneTextView.setText(premiumFeatureDetail1.getMessage());
            mFeatureDetailTwoTextView.setText(premiumFeatureDetail2.getMessage());
            mFeatureDetailThreeTextView.setText(premiumFeatureDetail3.getMessage());
            mFeatureDetailFourTextView.setText(premiumFeatureDetail4.getMessage());

            mFeatureDetailOneImageView.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), premiumFeatureDetail1.getImageId(), null));
            mFeatureDetailTwoImageView.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), premiumFeatureDetail2.getImageId(), null));
            mFeatureDetailThreeImageView.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), premiumFeatureDetail3.getImageId(), null));
            mFeatureDetailFourImageView.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), premiumFeatureDetail4.getImageId(), null));
        }
    }

    public void onViewRecycled() {

    }
}
