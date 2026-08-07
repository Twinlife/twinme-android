/*
 *  Copyright (c) 2020-2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.settingsActivity;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.SwitchView;

public class SettingSwitchViewHolder extends RecyclerView.ViewHolder {

    private static final int DESIGN_LEFT_MARGIN = 34;
    private static final int DESIGN_RIGHT_MARGIN = 32;

    private static final int DESIGN_VERTICAL_MARGIN = 3;

    private final SwitchView mSwitchView;

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

        if (uiSetting.getSubTitle() == null) {
            mSwitchView.setText(uiSetting.getTitle());
        } else {
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
            spannableStringBuilder.append(uiSetting.getTitle());
            spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_DEFAULT), 0, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableStringBuilder.append("\n");

            int startSubTitle = spannableStringBuilder.length();
            spannableStringBuilder.append(uiSetting.getSubTitle());
            spannableStringBuilder.setSpan(new RelativeSizeSpan(0.9f), startSubTitle, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_GREY), startSubTitle, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            mSwitchView.setText(spannableStringBuilder);
        }

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