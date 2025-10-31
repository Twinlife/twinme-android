/*
 *  Copyright (c) 2022 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.spaces;

import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.SwitchView;

public class SettingSpaceViewHolder extends RecyclerView.ViewHolder {

    public interface Observer {

        void onSettingChangeValue(String spaceSettingProperty, boolean value);
    }

    private static final float DESIGN_ITEM_VIEW_HEIGHT = 120f;
    private static final int ITEM_VIEW_HEIGHT;

    static {
        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
    }

    private final Observer mObserver;

    private final SwitchView mSwitchView;

    private String mSpaceSettingProperty;

    private final CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener;

    public SettingSpaceViewHolder(@NonNull View view, Observer observer) {

        super(view);

        mObserver = observer;

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = ITEM_VIEW_HEIGHT;
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Design.WHITE_COLOR);

        mSwitchView = view.findViewById(R.id.settings_activity_item_checkbox);
        mSwitchView.setTypeface(Design.FONT_REGULAR32.typeface);
        mSwitchView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_REGULAR32.size);
        mSwitchView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mOnCheckedChangeListener = (compoundButton, value) -> mObserver.onSettingChangeValue(mSpaceSettingProperty, value);
        mSwitchView.setOnCheckedChangeListener(mOnCheckedChangeListener);
    }

    public void onBind(String spaceSettingProperty, String title, boolean isSelected, boolean isEnable) {

        mSpaceSettingProperty = spaceSettingProperty;

        mSwitchView.setText(title);

        mSwitchView.setEnabled(isEnable);
        mSwitchView.setOnCheckedChangeListener(null);
        mSwitchView.setChecked(isSelected);
        if (isEnable) {
            mSwitchView.setOnCheckedChangeListener(mOnCheckedChangeListener);
            itemView.setOnClickListener(null);
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