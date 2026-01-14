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

    private final SwitchView mSwitchView;

    private UISetting<Boolean> mUISetting;
    private Settings.BooleanConfig mBooleanConfig;
    private final CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener;

    public SettingSwitchViewHolder(@NonNull View view, AbstractSettingsActivity settingsActivity) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = Design.SECTION_HEIGHT;
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Design.WHITE_COLOR);

        mSwitchView = view.findViewById(R.id.settings_activity_item_checkbox);
        Design.updateTextFont(mSwitchView, Design.FONT_REGULAR32);
        mSwitchView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mOnCheckedChangeListener = (compoundButton, value) -> {
            if (mUISetting != null) {
                settingsActivity.onSettingChangeValue(mUISetting, value);
            } else {
                settingsActivity.onSettingChangeValue(mBooleanConfig, value);
            }
        };
        mSwitchView.setOnCheckedChangeListener(mOnCheckedChangeListener);
    }

    public void onBind(@NonNull UISetting<Boolean> uiSetting, boolean isSelected, boolean isEnable) {

        mUISetting = uiSetting;
        mBooleanConfig = null;

        mSwitchView.setText(uiSetting.getTitle());

        mSwitchView.setOnCheckedChangeListener(null);
        mSwitchView.setChecked(isSelected);

        if (isEnable) {
            mSwitchView.setEnabled(true);
            mSwitchView.setOnCheckedChangeListener(mOnCheckedChangeListener);
        } else {
            mSwitchView.setEnabled(false);
            mSwitchView.setClickable(false);
        }

        updateFont();
        updateColor();
    }

    public void onBind(@NonNull String title, boolean isSelected, Settings.BooleanConfig booleanConfig) {

        mBooleanConfig = booleanConfig;
        mUISetting = null;

        mSwitchView.setText(title);

        mSwitchView.setOnCheckedChangeListener(null);
        mSwitchView.setChecked(isSelected);
        mSwitchView.setOnCheckedChangeListener(mOnCheckedChangeListener);

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