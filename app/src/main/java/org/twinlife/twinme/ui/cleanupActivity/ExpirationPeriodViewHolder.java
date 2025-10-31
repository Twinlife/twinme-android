/*
 *  Copyright (c) 2023-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.cleanupActivity;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class ExpirationPeriodViewHolder extends RecyclerView.ViewHolder {

    private static final float DESIGN_SELECTED_SIZE = 44f;
    private static final float DESIGN_MARGIN = 34f;

    private final TextView mTitleView;
    private final View mSelectedView;
    private final View mSeparatorView;

    public ExpirationPeriodViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = MenuCleanUpExpirationView.ITEM_VIEW_HEIGHT;
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Color.TRANSPARENT);

        mTitleView = view.findViewById(R.id.menu_expiration_period_item_title_view);
        Design.updateTextFont(mTitleView, Design.FONT_REGULAR32);
        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mTitleView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_MARGIN * Design.WIDTH_RATIO);

        mSelectedView = view.findViewById(R.id.menu_expiration_period_item_selected_view);

        layoutParams = mSelectedView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_SELECTED_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_SELECTED_SIZE * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mSelectedView.getLayoutParams();
        marginLayoutParams.rightMargin = (int) (DESIGN_MARGIN * Design.WIDTH_RATIO);

        ImageView selectedImageView = view.findViewById(R.id.menu_expiration_period_item_selected_image);
        selectedImageView.setColorFilter(Design.getMainStyle());

        mSeparatorView = view.findViewById(R.id.menu_expiration_period_item_separator_view);
        mSeparatorView.setBackgroundColor(Design.SEPARATOR_COLOR);
    }

    public void onBind(UICleanUpExpiration cleanUpExpiration, boolean displayValue, boolean checked, boolean hideSeparator) {

        if (displayValue) {
            mTitleView.setText(cleanUpExpiration.getValue(itemView.getContext()));
        } else {
            mTitleView.setText(cleanUpExpiration.getTitle(itemView.getContext()));
        }

        if (checked) {
            mSelectedView.setVisibility(View.VISIBLE);
        } else {
            mSelectedView.setVisibility(View.GONE);
        }

        if (hideSeparator) {
            mSeparatorView.setVisibility(View.GONE);
        } else {
            mSeparatorView.setVisibility(View.VISIBLE);
        }

        updateFont();
        updateColor();
    }

    public void onViewRecycled() {

    }

    private void updateFont() {

        Design.updateTextFont(mTitleView, Design.FONT_REGULAR32);
    }

    private void updateColor() {

        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mSeparatorView.setBackgroundColor(Design.SEPARATOR_COLOR);
    }
}