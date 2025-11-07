/*
 *  Copyright (c) 2020-2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.utils;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class SectionTitleViewHolder extends RecyclerView.ViewHolder {

    private static final float DESIGN_ITEM_VIEW_HEIGHT = 110f;
    private static final int ITEM_VIEW_HEIGHT;

    static {
        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
    }

    private final TextView mSectionTitleView;
    private final TextView mNewFeatureView;
    private final View mSeparatorView;

    public SectionTitleViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = ITEM_VIEW_HEIGHT;
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        mSectionTitleView = view.findViewById(R.id.section_title_item_title_view);
        Design.updateTextFont(mSectionTitleView, Design.FONT_BOLD26);
        mSectionTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        mNewFeatureView = view.findViewById(R.id.section_title_item_new_feature);
        Design.updateTextFont(mNewFeatureView, Design.FONT_MEDIUM30);
        mNewFeatureView.setTextColor(Color.WHITE);
        mNewFeatureView.setPadding(Design.NEW_FEATURE_PADDING, 0, Design.NEW_FEATURE_PADDING, 0);
        mNewFeatureView.setVisibility(View.GONE);

        layoutParams = mNewFeatureView.getLayoutParams();
        layoutParams.height = Design.NEW_FEATURE_HEIGHT;

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mNewFeatureView.getLayoutParams();
        marginLayoutParams.topMargin = - (int) (Design.NEW_FEATURE_HEIGHT * 0.5f);
        marginLayoutParams.leftMargin = - Design.NEW_FEATURE_MARGIN;
        marginLayoutParams.rightMargin = - Design.NEW_FEATURE_MARGIN;

        ShapeDrawable newFeatureTitleViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        newFeatureTitleViewBackground.getPaint().setColor(Design.getMainStyle());
        mNewFeatureView.setBackground(newFeatureTitleViewBackground);

        mSeparatorView = view.findViewById(R.id.section_title_item_item_separator_view);
        mSeparatorView.setBackgroundColor(Design.SEPARATOR_COLOR);
    }

    public void onBind(String title, boolean hideSeparator) {

        updateViews(title, hideSeparator, false);
    }

    public void onBind(String title, boolean hideSeparator, boolean isNewFeature, Runnable runnable) {

        updateViews(title, hideSeparator, isNewFeature);

        if (runnable != null) {
            mNewFeatureView.setOnClickListener(view -> runnable.run());
        }
    }

    private void updateViews(String title, boolean hideSeparator, boolean isNewFeature) {

        mSectionTitleView.setText(Utils.capitalizeString(title));

        if (hideSeparator) {
            mSeparatorView.setVisibility(View.GONE);
        } else {
            mSeparatorView.setVisibility(View.VISIBLE);
        }

        if (isNewFeature) {
            mNewFeatureView.setVisibility(View.VISIBLE);
        } else {
            mNewFeatureView.setVisibility(View.GONE);
        }

        updateFont();
        updateColor();
    }

    public void onViewRecycled() {

    }

    private void updateFont() {

        Design.updateTextFont(mSectionTitleView, Design.FONT_BOLD26);
    }

    private void updateColor() {

        itemView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        mSectionTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mSeparatorView.setBackgroundColor(Design.SEPARATOR_COLOR);
    }
}
