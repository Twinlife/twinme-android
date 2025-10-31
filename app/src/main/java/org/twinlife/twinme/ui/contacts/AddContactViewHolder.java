/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.contacts;

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
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.RoundedView;

public class AddContactViewHolder extends RecyclerView.ViewHolder {

    private static final float DESIGN_ADD_VIEW_SIZE = 86f;
    private static final float DESIGN_ADD_VIEW_MARGIN = 20f;
    private static final float DESIGN_ADD_ICON_SIZE = 74f;
    private static final float DESIGN_TEXT_MARGIN = 8f;
    private static final int ADD_VIEW_SIZE;
    private static final int ADD_VIEW_MARGIN;
    private static final int ADD_ICON_SIZE;
    private static final int TEXT_MARGIN;

    static {
        ADD_VIEW_SIZE = (int) (DESIGN_ADD_VIEW_SIZE * Design.HEIGHT_RATIO);
        ADD_VIEW_MARGIN = (int) (DESIGN_ADD_VIEW_MARGIN * Design.HEIGHT_RATIO);
        ADD_ICON_SIZE = (int) (DESIGN_ADD_ICON_SIZE * Design.HEIGHT_RATIO);
        TEXT_MARGIN = (int) (DESIGN_TEXT_MARGIN * Design.HEIGHT_RATIO);
    }

    private final TextView mTitleView;

    public AddContactViewHolder(@NonNull View view) {

        super(view);

        view.setBackgroundColor(Design.WHITE_COLOR);

        RoundedView roundedView = view.findViewById(R.id.contacts_fragment_add_contact_item_rounded_view);
        roundedView.setColor(Design.getMainStyle());

        ViewGroup.LayoutParams layoutParams = roundedView.getLayoutParams();
        layoutParams.width = ADD_VIEW_SIZE;
        layoutParams.height = ADD_VIEW_SIZE;

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) roundedView.getLayoutParams();
        marginLayoutParams.topMargin = ADD_VIEW_MARGIN;
        marginLayoutParams.bottomMargin = ADD_VIEW_MARGIN;

        ImageView imageView = view.findViewById(R.id.contacts_fragment_add_contact_item_image_view);

        layoutParams = imageView.getLayoutParams();
        layoutParams.width = ADD_ICON_SIZE;
        layoutParams.height = ADD_ICON_SIZE;

        mTitleView = view.findViewById(R.id.contacts_fragment_add_contact_item_title_view);
        Design.updateTextFont(mTitleView, Design.FONT_MEDIUM32);
        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mTitleView.getLayoutParams();
        marginLayoutParams.topMargin = TEXT_MARGIN;
        marginLayoutParams.bottomMargin = TEXT_MARGIN;
    }

    public void onBind(String title, String subTitle) {

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(title);
        spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_DEFAULT), 0, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.append("\n");
        int startInfo = spannableStringBuilder.length();
        spannableStringBuilder.append(subTitle);
        spannableStringBuilder.setSpan(new RelativeSizeSpan(0.87f), startInfo, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_GREY), startInfo, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mTitleView.setText(spannableStringBuilder);

        updateColor();
    }

    public void onViewRecycled() {

        updateColor();
    }

    private void updateColor() {

        itemView.setBackgroundColor(Design.WHITE_COLOR);
    }
}
