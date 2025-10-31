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

public class RingtoneViewHolder extends RecyclerView.ViewHolder {

    private static final float DESIGN_ITEM_VIEW_HEIGHT = 110f;
    private static final int ITEM_VIEW_HEIGHT;

    static {
        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
    }

    private final TextView mTitleView;
    private final TextView mRingToneTextView;

    private UISetting<String> mUISetting;

    public RingtoneViewHolder(@NonNull View view, AbstractSettingsActivity settingsActivity) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = ITEM_VIEW_HEIGHT;
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Design.WHITE_COLOR);

        mTitleView = view.findViewById(R.id.settings_activity_item_ringtone_title_view);
        Design.updateTextFont(mTitleView, Design.FONT_REGULAR32);
        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mRingToneTextView = view.findViewById(R.id.settings_activity_item_ringtone_name_view);
        Design.updateTextFont(mRingToneTextView, Design.FONT_REGULAR28);
        mRingToneTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        itemView.setOnClickListener(v -> settingsActivity.onRingToneClick(mUISetting));
    }

    public void onBind(UISetting<String> uiSetting, String sound) {

        mUISetting = uiSetting;

        mTitleView.setText(uiSetting.getTitle());
        mRingToneTextView.setText(sound);

        updateFont();
        updateColor();
    }

    private void updateFont() {

        Design.updateTextFont(mRingToneTextView, Design.FONT_REGULAR28);
        Design.updateTextFont(mTitleView, Design.FONT_REGULAR32);
    }

    private void updateColor() {

        itemView.setBackgroundColor(Design.WHITE_COLOR);
        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mRingToneTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
    }
}