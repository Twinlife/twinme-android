/*
 *  Copyright (c) 2022 twinlife SA.
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

public class SettingSectionViewHolder extends RecyclerView.ViewHolder {

    private final TextView mTextView;
    private final ImageView mAccessoryView;

    public SettingSectionViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = Design.SECTION_HEIGHT;
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Design.WHITE_COLOR);

        mTextView = view.findViewById(R.id.settings_activity_item_section_title_view);
        Design.updateTextFont(mTextView, Design.FONT_REGULAR32);
        mTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mAccessoryView = view.findViewById(R.id.settings_activity_item_section_accessory_view);
    }

    public void onBind(String title, boolean premiumSection) {

        mTextView.setText(title);
        mTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mAccessoryView.setVisibility(View.VISIBLE);

        if (premiumSection) {
            itemView.setAlpha(0.5f);
        } else {
            itemView.setAlpha(1.0f);
        }

        updateFont();
        updateColor();
    }

    public void onBind(String title, int color, boolean hideAccessory) {

        mTextView.setText(title);
        mTextView.setTextColor(color);

        if (hideAccessory) {
            mAccessoryView.setVisibility(View.GONE);
        } else {
            mAccessoryView.setVisibility(View.VISIBLE);
        }

        itemView.setAlpha(1.0f);

        updateFont();
        updateColor();
    }

    private void updateFont() {

        Design.updateTextFont(mTextView, Design.FONT_REGULAR32);
    }

    private void updateColor() {

        itemView.setBackgroundColor(Design.WHITE_COLOR);
    }
}