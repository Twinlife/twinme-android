/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.settingsActivity;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.UIAppInfo;

public class ConnexionStatusViewHolder extends RecyclerView.ViewHolder {

    private final TextView mTitleView;
    private final ImageView mStatusImageView;

    public ConnexionStatusViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = Design.SECTION_HEIGHT;
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Design.WHITE_COLOR);

        mTitleView = view.findViewById(R.id.connexion_status_item_title_view);
        mTitleView.setTypeface(Design.FONT_REGULAR32.typeface);
        mTitleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_REGULAR32.size);
        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mStatusImageView = view.findViewById(R.id.connexion_status_item_image_view);
    }

    public void onBind(UIAppInfo uiAppInfo) {

        if (uiAppInfo != null) {
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
            spannableStringBuilder.append(uiAppInfo.getTitle());
            spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_DEFAULT), 0, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            if (uiAppInfo.getProxy() != null) {
                spannableStringBuilder.append("\n");
                int startInfo = spannableStringBuilder.length();
                spannableStringBuilder.append(uiAppInfo.getProxy());
                spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_GREY), startInfo, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            mTitleView.setText(spannableStringBuilder);

            mStatusImageView.setImageDrawable(ResourcesCompat.getDrawable(itemView.getResources(), uiAppInfo.getImageId(), null));
        } else {
            mTitleView.setText(itemView.getContext().getString(R.string.application_connection_status_no_services));
            mStatusImageView.setImageDrawable(ResourcesCompat.getDrawable(itemView.getResources(), R.drawable.no_access_services, null));
        }

        updateFont();
        updateColor();
    }

    private void updateFont() {

        mTitleView.setTypeface(Design.FONT_REGULAR32.typeface);
        mTitleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_REGULAR32.size);
    }

    private void updateColor() {

        itemView.setBackgroundColor(Design.WHITE_COLOR);
    }
}