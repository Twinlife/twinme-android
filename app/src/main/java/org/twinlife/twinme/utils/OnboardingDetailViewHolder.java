/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.utils;

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
import org.twinlife.twinme.ui.premiumServicesActivity.UIPremiumFeatureDetail;

public class OnboardingDetailViewHolder extends RecyclerView.ViewHolder {
    private static final String LOG_TAG = "OnboardingDetail...";
    private static final boolean DEBUG = false;

    private static final float DESIGN_ITEM_VIEW_HEIGHT = 100;
    private static final float DESIGN_ICON_MARGIN = 34;
    private static final float DESIGN_ICON_HEIGHT= 48;
    private static final int ITEM_VIEW_HEIGHT;
    private static final int ICON_MARGIN;

    private static final int ICON_SIZE;

    static {
        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
        ICON_MARGIN = (int) (DESIGN_ICON_MARGIN * Design.WIDTH_RATIO);
        ICON_SIZE = (int) (DESIGN_ICON_HEIGHT * Design.HEIGHT_RATIO);
    }

    private final ImageView mIconView;
    private final TextView mTitleView;

    public OnboardingDetailViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = ITEM_VIEW_HEIGHT;
        view.setLayoutParams(layoutParams);

        mIconView = view.findViewById(R.id.onboarding_detail_view_item_image_view);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mIconView.getLayoutParams();
        marginLayoutParams.leftMargin = ICON_MARGIN;
        marginLayoutParams.rightMargin = ICON_MARGIN;

        layoutParams = mIconView.getLayoutParams();
        layoutParams.width = ICON_SIZE;
        layoutParams.height = ICON_SIZE;

        mTitleView = view.findViewById(R.id.onboarding_detail_view_item_title_view);
        Design.updateTextFont(mTitleView, Design.FONT_MEDIUM30);
        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mTitleView.getLayoutParams();
        marginLayoutParams.rightMargin = ICON_MARGIN;
    }

    public void onBind(UIPremiumFeatureDetail premiumFeatureDetail, boolean forceDarkMode) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBind: " + premiumFeatureDetail);
        }

        itemView.setBackgroundColor(Design.POPUP_BACKGROUND_COLOR);
        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mTitleView.setText(premiumFeatureDetail.getMessage());
        mIconView.setImageDrawable(ResourcesCompat.getDrawable(itemView.getResources(), premiumFeatureDetail.getImageId(), null));
    }

    public void onViewRecycled() {

    }
}
