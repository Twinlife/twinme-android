/*
 *  Copyright (c) 2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.exportActivity;

import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class ExportContentViewHolder extends RecyclerView.ViewHolder {

    private final TextView mTitleView;
    private final ImageView mImageView;
    private final ImageView mSelectedView;

    public ExportContentViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = Design.EXPORT_VIEW_HEIGHT;
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Design.WHITE_COLOR);

        mImageView = view.findViewById(R.id.export_activity_content_item_image);
        mImageView.setColorFilter(Design.SHOW_ICON_COLOR);

        mTitleView = view.findViewById(R.id.export_activity_content_item_title);
        Design.updateTextFont(mTitleView, Design.FONT_REGULAR32);
        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mSelectedView = view.findViewById(R.id.export_activity_content_item_selected_image);
        mSelectedView.setColorFilter(Design.getMainStyle());
    }

    public void onBind(UIExport export, Drawable drawable) {

        mImageView.setImageDrawable(drawable);

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(export.getTitle(itemView.getContext()));
        spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_DEFAULT), 0, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.append("\n");
        int startInfo = spannableStringBuilder.length();
        spannableStringBuilder.append(export.getInformation(itemView.getContext()));
        spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_GREY), startInfo, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mTitleView.setText(spannableStringBuilder);

        if (export.isChecked() && export.getCount() > 0) {
            mSelectedView.setVisibility(View.VISIBLE);
        } else {
            mSelectedView.setVisibility(View.INVISIBLE);
        }

        if (export.getCount() == 0) {
            mImageView.setAlpha(0.5f);
            mSelectedView.setAlpha(0.5f);
            mTitleView.setAlpha(0.5f);
        } else {
            mImageView.setAlpha(1f);
            mSelectedView.setAlpha(1f);
            mTitleView.setAlpha(1f);
        }

        updateColor();
    }

    private void updateColor() {

        itemView.setBackgroundColor(Design.WHITE_COLOR);
        mSelectedView.setColorFilter(Design.getMainStyle());
    }
}