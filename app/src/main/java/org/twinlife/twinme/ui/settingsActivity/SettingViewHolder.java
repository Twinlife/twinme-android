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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class SettingViewHolder extends RecyclerView.ViewHolder {

    private final TextView mTextView;

    private UISetting<?> mUISetting;

    public SettingViewHolder(@NonNull View view, AbstractSettingsActivity settingsActivity) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = Design.SECTION_HEIGHT;
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Design.WHITE_COLOR);

        mTextView = view.findViewById(R.id.settings_activity_item_title_view);
        Design.updateTextFont(mTextView, Design.FONT_REGULAR32);
        mTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        itemView.setOnClickListener(v -> settingsActivity.onSettingClick(mUISetting));
    }

    public void onBind(@NonNull UISetting<?> uiSetting) {

        mUISetting = uiSetting;

        mTextView.setText(uiSetting.getTitle());

        updateFont();
        updateColor();
    }

    private void updateFont() {

        Design.updateTextFont(mTextView, Design.FONT_REGULAR32);
    }

    private void updateColor() {

        itemView.setBackgroundColor(Design.WHITE_COLOR);
        mTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
    }
}