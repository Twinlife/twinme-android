/*
 *  Copyright (c) 2020-2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.spaces;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class SubSectionViewHolder extends RecyclerView.ViewHolder {

    private static final float DESIGN_ITEM_VIEW_HEIGHT = 80f;
    private static final int ITEM_VIEW_HEIGHT;

    static {
        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
    }

    private TextView mTitleView;
    private View mSeparatorView;

    SubSectionViewHolder(@NonNull View view) {
        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = ITEM_VIEW_HEIGHT;
        view.setLayoutParams(layoutParams);

        initViews(view);
    }

    SubSectionViewHolder(@NonNull View view, int height) {
        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = height;
        view.setLayoutParams(layoutParams);

        initViews(view);
    }

    public void onBind(String title) {

        mTitleView.setText(title);

        updateColor();
        updateFont();
    }

    public void onBind(String title, boolean hideSeparator) {

        mTitleView.setText(title);

        if (hideSeparator) {
            mSeparatorView.setVisibility(View.GONE);
        } else {
            mSeparatorView.setVisibility(View.VISIBLE);
        }

        updateColor();
        updateFont();
    }

    public void onViewRecycled() {

    }

    private void initViews(View view) {

        mTitleView = view.findViewById(R.id.space_appearance_activity_subsection_item_title_view);
        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);
        Design.updateTextFont(mTitleView, Design.FONT_BOLD26);

        mSeparatorView = view.findViewById(R.id.space_appearance_activity_subsection_item_separator_view);
    }

    private void updateColor() {

        itemView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);
    }

    private void updateFont() {

        Design.updateTextFont(mTitleView, Design.FONT_BOLD26);
    }
}