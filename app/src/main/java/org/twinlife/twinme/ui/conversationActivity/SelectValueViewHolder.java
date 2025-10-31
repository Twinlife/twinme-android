/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.conversationActivity;

import android.graphics.Color;
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

    private boolean mForceDarkMode = false;

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

    public void onBind(@Nullable String title, String value, boolean forceDarkMode, int backgroundColor) {

        mForceDarkMode = forceDarkMode;

        int colorTitle = Design.FONT_COLOR_DEFAULT;
        if (forceDarkMode) {
            colorTitle = Color.WHITE;
        }

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();

        if (title != null) {
            spannableStringBuilder.append(title);
            spannableStringBuilder.setSpan(new ForegroundColorSpan(colorTitle), 0, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableStringBuilder.append("\n");
        }

        int startSubTitle = spannableStringBuilder.length();
        spannableStringBuilder.append(value);
        spannableStringBuilder.setSpan(new ForegroundColorSpan(title != null ? Design.FONT_COLOR_GREY : colorTitle), startSubTitle, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        mTextView.setText(spannableStringBuilder);

        itemView.setBackgroundColor(backgroundColor);

        if (mForceDarkMode) {
            itemView.setBackgroundColor(Color.rgb(72,72,72));
        } else {
            itemView.setBackgroundColor(backgroundColor);
        }

        updateFont();
        updateColor();
    }

    private void updateFont() {

        Design.updateTextFont(mTextView, Design.FONT_REGULAR32);
    }

    private void updateColor() {

        if (mForceDarkMode) {
            mTextView.setTextColor(Color.WHITE);
        } else {
            mTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        }
    }
}