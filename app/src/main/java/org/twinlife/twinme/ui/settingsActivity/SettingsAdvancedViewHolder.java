/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */


package org.twinlife.twinme.ui.settingsActivity;

import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.SwitchView;

public class SettingsAdvancedViewHolder extends RecyclerView.ViewHolder {

    private final SwitchView mSwitchView;

    public SettingsAdvancedViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = Design.SECTION_HEIGHT;
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Design.WHITE_COLOR);

        mSwitchView = view.findViewById(R.id.settings_advanced_item_checkbox);
        mSwitchView.setTypeface(Design.FONT_REGULAR32.typeface);
        mSwitchView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_REGULAR32.size);
        mSwitchView.setTextColor(Design.FONT_COLOR_DEFAULT);
    }

    public void onBind(@NonNull String title, boolean isSelected, boolean isEnable, @Nullable CompoundButton.OnCheckedChangeListener onCheckedChangeListener) {

        mSwitchView.setText(title);

        mSwitchView.setOnCheckedChangeListener(null);
        mSwitchView.setChecked(isSelected);

        if (isEnable) {
            mSwitchView.setEnabled(true);
            mSwitchView.setClickable(true);
            mSwitchView.setOnCheckedChangeListener(onCheckedChangeListener);
            mSwitchView.setAlpha(1.0f);
        } else {
            mSwitchView.setEnabled(false);
            mSwitchView.setClickable(false);
            mSwitchView.setAlpha(0.5f);
        }

        updateFont();
        updateColor();
    }

    private void updateFont() {

        mSwitchView.setTypeface(Design.FONT_REGULAR32.typeface);
        mSwitchView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_REGULAR32.size);
    }

    private void updateColor() {

        itemView.setBackgroundColor(Design.WHITE_COLOR);
        mSwitchView.setTextColor(Design.FONT_COLOR_DEFAULT);
    }
}