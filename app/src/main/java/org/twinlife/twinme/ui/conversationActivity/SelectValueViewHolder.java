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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class SelectValueViewHolder extends RecyclerView.ViewHolder {

    private static final int DESIGN_MARGIN = 34;
    private static final int DESIGN_ACCESSORY_WIDTH = 22;
    private static final int DESIGN_ACCESSORY_HEIGHT = 34;
    private static final int DESIGN_ICON_SIZE = 36;

    private final TextView mTextView;

    private final ImageView mIconView;
    private final ImageView mAccessoryView;

    private boolean mForceDarkMode = false;

    public SelectValueViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = Design.SECTION_HEIGHT;
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Design.POPUP_BACKGROUND_COLOR);

        mIconView = view.findViewById(R.id.select_value_item_icon_view);
        mIconView.setVisibility(View.GONE);
        layoutParams = mIconView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_ICON_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_ICON_SIZE * Design.HEIGHT_RATIO);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mIconView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_MARGIN * Design.WIDTH_RATIO);

        mTextView = view.findViewById(R.id.select_value_item_title);
        Design.updateTextFont(mTextView, Design.FONT_REGULAR32);
        mTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mTextView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) ((DESIGN_MARGIN + DESIGN_ACCESSORY_WIDTH) * Design.WIDTH_RATIO);

        mAccessoryView = view.findViewById(R.id.select_value_item_accessory_view);
        layoutParams = mAccessoryView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_ACCESSORY_WIDTH * Design.WIDTH_RATIO);
        layoutParams.height = (int) (DESIGN_ACCESSORY_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mAccessoryView.getLayoutParams();
        marginLayoutParams.rightMargin = (int) (DESIGN_MARGIN * Design.WIDTH_RATIO);
    }

    public void resetMargins() {

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mTextView.getLayoutParams();
        marginLayoutParams.leftMargin = 0;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mAccessoryView.getLayoutParams();
        marginLayoutParams.rightMargin = 0;
    }

    public void onBind(@Nullable String title, String value, boolean forceDarkMode, int backgroundColor) {

        mForceDarkMode = forceDarkMode;

        int colorTitle = Design.FONT_COLOR_DEFAULT;
        if (forceDarkMode) {
            colorTitle = Color.WHITE;
        }

        mIconView.setVisibility(View.GONE);
        updateViews(title, value, colorTitle, backgroundColor);
    }

    public void onBind(@Nullable String title, String value, int icon, int colorTitle, int backgroundColor) {

        mIconView.setVisibility(View.VISIBLE);
        mIconView.setImageDrawable(ResourcesCompat.getDrawable(itemView.getResources(), icon, null));
        mIconView.setColorFilter(Design.BLACK_COLOR);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mTextView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_MARGIN * Design.WIDTH_RATIO * 2) +  (int) (DESIGN_ICON_SIZE * Design.HEIGHT_RATIO);

        updateViews(title, value, colorTitle, backgroundColor);
    }

    private void updateViews(@Nullable String title, String value, int colorTitle, int backgroundColor) {

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