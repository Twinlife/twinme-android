/*
 *  Copyright (c) 2020 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.settingsActivity;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class PersonalizationViewHolder extends RecyclerView.ViewHolder {

    private final TextView mTitleView;
    private final ImageView mSelectedView;

    public PersonalizationViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = Design.SECTION_HEIGHT;
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Design.WHITE_COLOR);

        mTitleView = view.findViewById(R.id.personalization_activity_item_name_view);
        Design.updateTextFont(mTitleView, Design.FONT_REGULAR32);
        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mSelectedView = view.findViewById(R.id.personalization_activity_item_selected_image);
        mSelectedView.setColorFilter(Design.getMainStyle());
    }

    public void onBind(String title, boolean isSelected) {

        mTitleView.setText(title);

        if (isSelected) {
            mSelectedView.setVisibility(View.VISIBLE);
        } else {
            mSelectedView.setVisibility(View.INVISIBLE);
        }

        updateFont();
        updateColor();
    }

    private void updateFont() {

        Design.updateTextFont(mTitleView, Design.FONT_REGULAR32);
    }

    private void updateColor() {

        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);
        itemView.setBackgroundColor(Design.WHITE_COLOR);
        mSelectedView.setColorFilter(Design.getMainStyle());
    }
}