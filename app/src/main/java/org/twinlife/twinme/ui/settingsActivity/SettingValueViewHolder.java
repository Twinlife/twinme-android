/*
 *  Copyright (c) 2020-2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.settingsActivity;

import android.annotation.SuppressLint;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.DisplayCallsMode;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.Settings;
import org.twinlife.twinme.ui.TwinmeApplication;

public class SettingValueViewHolder extends RecyclerView.ViewHolder {

    private static final float DESIGN_TEXT_LARGE_WIDTH_PERCENT = 0.8267f;

    private final TextView mTextView;
    private final TextView mValueView;

    private final ImageView mSelectImageView;

    private UISetting<?> mUISetting;

    public SettingValueViewHolder(@NonNull View view, AbstractSettingsActivity settingsActivity) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = Design.SECTION_HEIGHT;
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Design.WHITE_COLOR);

        mTextView = view.findViewById(R.id.settings_activity_item_title_view);
        Design.updateTextFont(mTextView, Design.FONT_REGULAR32);
        mTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mValueView = view.findViewById(R.id.settings_activity_item_value_view);
        Design.updateTextFont(mValueView, Design.FONT_REGULAR32);
        mValueView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mSelectImageView = view.findViewById(R.id.settings_activity_item_image_view);

        itemView.setOnClickListener(v -> settingsActivity.onSettingClick(mUISetting));
    }

    @SuppressLint("SetTextI18n")
    public void onBind(@NonNull UISetting<?> uiSetting, boolean visible) {

        mUISetting = uiSetting;

        mTextView.setText(uiSetting.getTitle());
        mValueView.setVisibility(View.GONE);
        mSelectImageView.setVisibility(View.VISIBLE);

        String title = uiSetting.getTitle();
        String value = "";
        if (uiSetting.isSetting(Settings.qualityMedia)) {
            if (uiSetting.getInteger() == TwinmeApplication.QualityMedia.STANDARD.ordinal()) {
                title = itemView.getContext().getString(R.string.conversation_activity_media_quality_standard);
                value = itemView.getContext().getString(R.string.conversation_activity_media_quality_standard_subtitle);
            } else {
                title = itemView.getContext().getString(R.string.conversation_activity_media_quality_original);
                value = itemView.getContext().getString(R.string.conversation_activity_media_quality_original_subtitle);
            }
        } else if (uiSetting.isSetting(Settings.displayCallsMode)) {
            if (uiSetting.getInteger() == DisplayCallsMode.NONE.ordinal()) {
                value = itemView.getContext().getString(R.string.settings_activity_display_call_none);
            } else if (uiSetting.getInteger() == DisplayCallsMode.MISSED.ordinal()) {
                value = itemView.getContext().getString(R.string.calls_fragment_missed_call_segmented_control);
            } else {
                value = itemView.getContext().getString(R.string.calls_fragment_all_call_segmented_control);
            }
        }

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();

        if (!title.isEmpty()) {
            spannableStringBuilder.append(title);
            spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_DEFAULT), 0, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableStringBuilder.append("\n");
        }

        int startSubTitle = spannableStringBuilder.length();
        spannableStringBuilder.append(value);
        spannableStringBuilder.setSpan(new RelativeSizeSpan(0.9f), startSubTitle, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.setSpan(new ForegroundColorSpan(!title.isEmpty() ? Design.FONT_COLOR_GREY : Design.FONT_COLOR_DEFAULT), startSubTitle, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        mTextView.setText(spannableStringBuilder);

        ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
        if (visible) {
            layoutParams.height = Design.SECTION_HEIGHT;
        } else {
            layoutParams.height = 0;
        }

        float textViewWidth = Design.DISPLAY_WIDTH * DESIGN_TEXT_LARGE_WIDTH_PERCENT;
        layoutParams = mTextView.getLayoutParams();
        layoutParams.width = (int) textViewWidth;

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