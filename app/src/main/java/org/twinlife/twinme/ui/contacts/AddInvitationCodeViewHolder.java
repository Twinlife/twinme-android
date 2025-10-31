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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.RoundedView;

public class AddInvitationCodeViewHolder extends RecyclerView.ViewHolder {

    private static final float DESIGN_ITEM_VIEW_SIZE = 124f;
    private static final int ITEM_VIEW_SIZE;

    static {
        ITEM_VIEW_SIZE = (int) (DESIGN_ITEM_VIEW_SIZE * Design.HEIGHT_RATIO);
    }

    private final TextView mTitleView;

    public AddInvitationCodeViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = ITEM_VIEW_SIZE;
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Design.WHITE_COLOR);

        RoundedView roundedView = view.findViewById(R.id.invitation_code_activity_add_code_item_rounded_view);
        roundedView.setColor(Design.getMainStyle());

        mTitleView = view.findViewById(R.id.invitation_code_activity_add_code_item_title_view);
        Design.updateTextFont(mTitleView, Design.FONT_MEDIUM32);
        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);
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
