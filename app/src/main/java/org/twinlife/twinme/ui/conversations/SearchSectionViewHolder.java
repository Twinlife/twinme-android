/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.conversations;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class SearchSectionViewHolder extends RecyclerView.ViewHolder {

    private static final float DESIGN_TITLE_MARGIN = 24f;
    private static final float DESIGN_ITEM_VIEW_HEIGHT = 90f;
    private static final int ITEM_VIEW_HEIGHT;

    static {
        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
    }

    private final TextView mSectionTitleView;
    private final View mRightView;
    private final TextView mRightTitleView;

    public SearchSectionViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = ITEM_VIEW_HEIGHT;
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Design.WHITE_COLOR);

        mSectionTitleView = view.findViewById(R.id.search_section_item_title_view);
        Design.updateTextFont(mSectionTitleView, Design.FONT_BOLD34);
        mSectionTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mSectionTitleView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_TITLE_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_TITLE_MARGIN * Design.WIDTH_RATIO));

        mRightView = view.findViewById(R.id.search_section_item_right_view);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mRightView.getLayoutParams();
        marginLayoutParams.rightMargin = (int) (DESIGN_TITLE_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginEnd((int) (DESIGN_TITLE_MARGIN * Design.WIDTH_RATIO));

        mRightTitleView = view.findViewById(R.id.search_section_item_right_title_view);
        Design.updateTextFont(mRightTitleView, Design.FONT_BOLD26);
        mRightTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);
    }

    public void onBind(String title, boolean showRightView, @Nullable Runnable runnable) {

        if (runnable != null) {
            mRightView.setOnClickListener(v -> runnable.run());
        }

        mSectionTitleView.setText(title);

        if (showRightView) {
            mRightView.setVisibility(View.VISIBLE);
            mRightTitleView.setVisibility(View.VISIBLE);
        } else {
            mRightView.setVisibility(View.GONE);
            mRightTitleView.setVisibility(View.GONE);
        }

        updateFont();
        updateColor();
    }

    public void onViewRecycled() {

    }

    private void updateFont() {

        Design.updateTextFont(mSectionTitleView, Design.FONT_BOLD34);
        Design.updateTextFont(mRightTitleView, Design.FONT_BOLD26);
    }

    private void updateColor() {

        itemView.setBackgroundColor(Design.WHITE_COLOR);
        mSectionTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mRightTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);
    }
}
