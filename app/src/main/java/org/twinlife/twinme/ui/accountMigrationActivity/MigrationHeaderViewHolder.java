/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.accountMigrationActivity;

import android.graphics.Color;
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

public class MigrationHeaderViewHolder extends RecyclerView.ViewHolder {

    private static final float DESIGN_HORIZONTAL_MARGIN = 20f;
    private static final float DESIGN_VERTICAL_MARGIN = 10f;

    private final TextView mMessageView;

    public MigrationHeaderViewHolder(@NonNull View view) {

        super(view);

        view.setBackgroundColor(Color.TRANSPARENT);

        mMessageView = view.findViewById(R.id.account_migration_header_item_text_view);
        Design.updateTextFont(mMessageView, Design.FONT_REGULAR32);
        mMessageView.setTextColor(Design.FONT_COLOR_DEFAULT);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mMessageView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.topMargin = (int) (DESIGN_VERTICAL_MARGIN* Design.WIDTH_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_VERTICAL_MARGIN * Design.WIDTH_RATIO);
    }

    public void onBind(UIMigrationStateItem item) {

        if (item.getInfo() != null && !item.getInfo().isEmpty()) {
            Design.updateTextFont(mMessageView, Design.FONT_MEDIUM32);
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
            spannableStringBuilder.append(item.getTitle());
            spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_DEFAULT), 0, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableStringBuilder.append("\n");
            int startSubTitle = spannableStringBuilder.length();
            spannableStringBuilder.append(item.getInfo());
            spannableStringBuilder.setSpan(new RelativeSizeSpan(0.8f), startSubTitle, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_GREY), startSubTitle, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            mMessageView.setText(spannableStringBuilder);
        } else {
            Design.updateTextFont(mMessageView, Design.FONT_MEDIUM36);
            mMessageView.setTextColor(Design.FONT_COLOR_DEFAULT);
            mMessageView.setText(item.getTitle());
        }
    }
}
