/*
 *  Copyright (c) 2020-2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.settingsActivity;

import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.Settings;
import org.twinlife.twinme.utils.SwitchView;

public class SettingSwitchViewHolder extends RecyclerView.ViewHolder {

    private static final int DESIGN_LEFT_MARGIN = 34;
    private static final int DESIGN_RIGHT_MARGIN = 32;

    private static final int DESIGN_VERTICAL_MARGIN = 3;

    private final SwitchView mSwitchView;

    private UISetting<Boolean> mUISetting;
    private Settings.BooleanConfig mBooleanConfig;

    public SettingSwitchViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = Design.SECTION_HEIGHT;
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Design.WHITE_COLOR);

        mSwitchView = view.findViewById(R.id.settings_activity_item_checkbox);
        Design.updateTextFont(mSwitchView, Design.FONT_REGULAR32);
        mSwitchView.setTextColor(Design.FONT_COLOR_DEFAULT);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mSwitchView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_LEFT_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_RIGHT_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.topMargin = (int) (DESIGN_VERTICAL_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_VERTICAL_MARGIN * Design.HEIGHT_RATIO);
    }

    public void resetMargins() {

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mSwitchView.getLayoutParams();
        marginLayoutParams.leftMargin = 0;
        marginLayoutParams.rightMargin = 0;
    }

    public void onBind(@NonNull UISetting<Boolean> uiSetting, boolean isSelected, boolean isEnable, CompoundButton.OnCheckedChangeListener onCheckedChangeListener) {

        mUISetting = uiSetting;
        mBooleanConfig = null;

        mSwitchView.setText(uiSetting.getTitle());

        mSwitchView.setOnCheckedChangeListener(null);
        mSwitchView.setChecked(isSelected);

        if (isEnable) {
            mSwitchView.setEnabled(true);
            mSwitchView.setOnCheckedChangeListener(onCheckedChangeListener);
        } else {
            mSwitchView.setEnabled(false);
            mSwitchView.setClickable(false);
        }

        updateFont();
        updateColor();
    }

    public void onBind(@NonNull String title, boolean isSelected, Settings.BooleanConfig booleanConfig, CompoundButton.OnCheckedChangeListener onCheckedChangeListener) {

        mBooleanConfig = booleanConfig;
        mUISetting = null;

        mSwitchView.setText(title);

        mSwitchView.setOnCheckedChangeListener(null);
        mSwitchView.setChecked(isSelected);

        if (onCheckedChangeListener != null) {
            mSwitchView.setEnabled(true);
            mSwitchView.setOnCheckedChangeListener(onCheckedChangeListener);
        } else {
            mSwitchView.setEnabled(false);
            mSwitchView.setClickable(false);
        }

        updateFont();
        updateColor();
    }

    public void onBind(@NonNull String title, boolean isSelected, boolean isEnable, CompoundButton.OnCheckedChangeListener onCheckedChangeListener) {

        mSwitchView.setText(title);

        mSwitchView.setOnCheckedChangeListener(null);
        mSwitchView.setChecked(isSelected);

        if (isEnable) {
            mSwitchView.setEnabled(true);
            mSwitchView.setOnCheckedChangeListener(onCheckedChangeListener);
        } else {
            mSwitchView.setEnabled(false);
            mSwitchView.setClickable(false);
        }

        updateFont();
        updateColor();
    }

    private void updateFont() {

        Design.updateTextFont(mSwitchView, Design.FONT_REGULAR32);
    }

    private void updateColor() {

        itemView.setBackgroundColor(Design.WHITE_COLOR);
        mSwitchView.setTextColor(Design.FONT_COLOR_DEFAULT);
    }
}