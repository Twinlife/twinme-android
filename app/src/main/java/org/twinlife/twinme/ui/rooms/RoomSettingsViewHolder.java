/*
 *  Copyright (c) 2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.rooms;

import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.SwitchView;

public class RoomSettingsViewHolder extends RecyclerView.ViewHolder {
    protected static final float DESIGN_ITEM_VIEW_HEIGHT = 120f;
    protected static final int ITEM_VIEW_HEIGHT;

    static {
        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
    }

    private final SwitchView mSwitchView;

    private int mSwitchTag;
    private final CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener;

    public RoomSettingsViewHolder(@NonNull View view, SettingsRoomActivity settingsActivity) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = ITEM_VIEW_HEIGHT;
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Design.WHITE_COLOR);

        mSwitchView = view.findViewById(R.id.settings_room_activity_item_checkbox);
        Design.updateTextFont(mSwitchView, Design.FONT_REGULAR32);
        mSwitchView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mOnCheckedChangeListener = (compoundButton, value) -> settingsActivity.onSettingChangeValue(mSwitchTag, value);
        mSwitchView.setOnCheckedChangeListener(mOnCheckedChangeListener);
    }

    public void onBind(String title, int switchTag, boolean isSelected) {

        mSwitchTag = switchTag;

        mSwitchView.setText(title);

        mSwitchView.setOnCheckedChangeListener(null);
        mSwitchView.setChecked(isSelected);
        mSwitchView.setOnCheckedChangeListener(mOnCheckedChangeListener);

        updateFont();
        updateColor();
    }

    public void onViewRecycled() {

    }

    private void updateFont() {

        Design.updateTextFont(mSwitchView, Design.FONT_REGULAR32);
    }

    private void updateColor() {

        itemView.setBackgroundColor(Design.WHITE_COLOR);
        mSwitchView.setTextColor(Design.FONT_COLOR_DEFAULT);
    }
}