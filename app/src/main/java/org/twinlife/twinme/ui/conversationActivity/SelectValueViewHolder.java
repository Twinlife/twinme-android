/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.conversationActivity;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class SelectValueViewHolder extends RecyclerView.ViewHolder {

    private final TextView mTextView;

    public SelectValueViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = Design.SECTION_HEIGHT;
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Design.POPUP_BACKGROUND_COLOR);

        mTextView = view.findViewById(R.id.select_value_item_title);
        Design.updateTextFont(mTextView, Design.FONT_REGULAR32);
        mTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
    }

    public void onBind(@Nullable String title, String value, boolean isEnable, int backgroundColor) {

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();

        if (title != null) {
            spannableStringBuilder.append(title);
            spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_DEFAULT), 0, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableStringBuilder.append("\n");
        }

        int startSubTitle = spannableStringBuilder.length();
        spannableStringBuilder.append(value);
        spannableStringBuilder.setSpan(new ForegroundColorSpan(title != null ? Design.FONT_COLOR_GREY : Design.FONT_COLOR_DEFAULT), startSubTitle, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        mTextView.setText(spannableStringBuilder);

        if (isEnable) {
            mTextView.setAlpha(1.0f);
        } else {
            mTextView.setAlpha(0.5f);
        }

        itemView.setBackgroundColor(backgroundColor);

        updateFont();
        updateColor();
    }

    private void updateFont() {

        Design.updateTextFont(mTextView, Design.FONT_REGULAR32);
    }

    private void updateColor() {

        mTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
    }
}