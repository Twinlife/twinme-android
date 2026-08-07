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
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class SectionTitleViewHolder extends RecyclerView.ViewHolder {

    private static final float DESIGN_ITEM_VIEW_HEIGHT = 110f;
    private static final int DESIGN_LEFT_MARGIN = 34;
    private static final int DESIGN_RIGHT_MARGIN = 32;
    private static final int DESIGN_BOTTOM_MARGIN = 14;
    private static final int ITEM_VIEW_HEIGHT;

    static {
        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
    }

    private final TextView mSectionTitleView;
    private final TextView mBadgeView;
    private final View mSeparatorView;

    private int mBackgroundColor = Design.LIGHT_GREY_BACKGROUND_COLOR;

    public SectionTitleViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = ITEM_VIEW_HEIGHT;
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        mSectionTitleView = view.findViewById(R.id.section_title_item_title_view);
        Design.updateTextFont(mSectionTitleView, Design.FONT_BOLD26);
        mSectionTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mSectionTitleView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_LEFT_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_RIGHT_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_BOTTOM_MARGIN * Design.HEIGHT_RATIO);

        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        mBadgeView = view.findViewById(R.id.section_title_item_badge_view);
        Design.updateTextFont(mBadgeView, Design.FONT_MEDIUM30);
        mBadgeView.setTextColor(Color.WHITE);
        mBadgeView.setPadding(Design.NEW_FEATURE_PADDING, 0, Design.NEW_FEATURE_PADDING, 0);
        mBadgeView.setVisibility(View.GONE);

        layoutParams = mBadgeView.getLayoutParams();
        layoutParams.height = Design.NEW_FEATURE_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mBadgeView.getLayoutParams();
        marginLayoutParams.topMargin = - (int) (Design.NEW_FEATURE_HEIGHT * 0.5f);
        marginLayoutParams.leftMargin = - Design.NEW_FEATURE_MARGIN;
        marginLayoutParams.rightMargin = - Design.NEW_FEATURE_MARGIN;

        ShapeDrawable newFeatureTitleViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        newFeatureTitleViewBackground.getPaint().setColor(Design.getMainStyle());
        mBadgeView.setBackground(newFeatureTitleViewBackground);

        mSeparatorView = view.findViewById(R.id.section_title_item_item_separator_view);
        mSeparatorView.setBackgroundColor(Design.SEPARATOR_COLOR);
    }

    public void resetMargins() {

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mSectionTitleView.getLayoutParams();
        marginLayoutParams.leftMargin = 0;
        marginLayoutParams.rightMargin = 0;
    }

    public void onBind(String title, boolean hideSeparator) {

        mBackgroundColor = Design.LIGHT_GREY_BACKGROUND_COLOR;
        updateViews(title, hideSeparator, null);
    }


    public void onBind(String title, boolean hideSeparator, @Nullable String badgeTitle, Runnable runnable) {

        updateViews(title, hideSeparator, badgeTitle);

        if (runnable != null) {
            mBadgeView.setOnClickListener(view -> runnable.run());
        }
    }

    private void updateViews(String title, boolean hideSeparator, @Nullable String badgeTitle) {

        mSectionTitleView.setText(Utils.capitalizeString(title));

        if (hideSeparator) {
            mSeparatorView.setVisibility(View.GONE);
        } else {
            mSeparatorView.setVisibility(View.VISIBLE);
        }

        if (badgeTitle != null) {
            mBadgeView.setText(badgeTitle);
            mBadgeView.setVisibility(View.VISIBLE);
        } else {
            mBadgeView.setVisibility(View.GONE);
        }

        updateFont();
        updateColor();
    }

    public void onBind(String title, int backgroundColor, boolean hideSeparator) {

        mBackgroundColor = backgroundColor;
        updateViews(title, hideSeparator, null);
    }

    public void onViewRecycled() {

    }

    private void updateFont() {

        Design.updateTextFont(mSectionTitleView, Design.FONT_BOLD26);
    }

    private void updateColor() {

        itemView.setBackgroundColor(mBackgroundColor);
        mSectionTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mSeparatorView.setBackgroundColor(Design.SEPARATOR_COLOR);
    }
}
