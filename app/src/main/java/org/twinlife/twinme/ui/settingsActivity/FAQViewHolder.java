/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.settingsActivity;

import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.faq.UIFAQItem;

public class FAQViewHolder extends RecyclerView.ViewHolder {

    private final static int DESIGN_HORIZONTAL_MARGIN = 34;

    private final TextView mFAQView;
    private final View mSeparatorView;

    public FAQViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = Design.SECTION_HEIGHT;
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Design.WHITE_COLOR);

        mFAQView = view.findViewById(R.id.faq_item_question_view);
        mFAQView.setTypeface(Design.FONT_REGULAR32.typeface);
        mFAQView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_REGULAR32.size);
        mFAQView.setTextColor(Design.FONT_COLOR_DEFAULT);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mFAQView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);

        mSeparatorView  = view.findViewById(R.id.faq_item_separator_view);
        mSeparatorView.setBackgroundColor(Design.SEPARATOR_COLOR);
    }

    public void onBind(UIFAQItem uifaqItem, boolean hideSeparator) {

        mFAQView.setText(uifaqItem.getTitle());

        if (hideSeparator) {
            mSeparatorView.setVisibility(View.GONE);
        } else {
            mSeparatorView.setVisibility(View.VISIBLE);
        }

        updateFont();
        updateColor();
    }

    private void updateFont() {

        mFAQView.setTypeface(Design.FONT_REGULAR32.typeface);
        mFAQView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_REGULAR32.size);
    }

    private void updateColor() {

        mFAQView.setTextColor(Design.FONT_COLOR_DEFAULT);
        itemView.setBackgroundColor(Design.WHITE_COLOR);
    }
}