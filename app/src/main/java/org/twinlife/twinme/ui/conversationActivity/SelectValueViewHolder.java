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

    public void onBind(@Nullable String title, String value, boolean isEnable, int backgroundColor) {

        mIconView.setVisibility(View.GONE);
        updateViews(title, value, isEnable, backgroundColor);
    }

    public void onBind(@Nullable String title, String value, int icon, boolean isEnable, int backgroundColor) {

        mIconView.setVisibility(View.VISIBLE);
        mIconView.setImageDrawable(ResourcesCompat.getDrawable(itemView.getResources(), icon, null));
        mIconView.setColorFilter(Design.BLACK_COLOR);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mTextView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_MARGIN * Design.WIDTH_RATIO * 2) +  (int) (DESIGN_ICON_SIZE * Design.HEIGHT_RATIO);

        updateViews(title, value, isEnable, backgroundColor);
    }

    private void updateViews(@Nullable String title, String value, boolean isEnable, int backgroundColor) {

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