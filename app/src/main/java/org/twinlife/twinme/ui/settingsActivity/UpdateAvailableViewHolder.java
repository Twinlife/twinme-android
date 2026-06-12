/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.settingsActivity;

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

public class UpdateAvailableViewHolder extends RecyclerView.ViewHolder {

    private static final float DESIGN_ICON_MARGIN = 32f;
    private static final float DESIGN_NOTIFICATION_MARGIN = 9f;
    private static final float DESIGN_TEXT_MARGIN = 34f;

    private static final float DESIGN_NOTIFICATION_SIZE = 16f;
    private static final float DESIGN_ICON_SIZE = 60f;

    private final TextView mInformationView;

    public UpdateAvailableViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = Design.SECTION_HEIGHT;
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Design.WHITE_COLOR);

        RoundedView notificationView = view.findViewById(R.id.update_available_notification_rounded_view);

        layoutParams = notificationView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_NOTIFICATION_SIZE * Design.WIDTH_RATIO);
        layoutParams.height = (int) (DESIGN_NOTIFICATION_SIZE * Design.HEIGHT_RATIO);
        notificationView.setLayoutParams(layoutParams);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) notificationView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_NOTIFICATION_MARGIN * Design.WIDTH_RATIO);

        notificationView.setColor(Design.DELETE_COLOR_RED);

        mInformationView = view.findViewById(R.id.update_available_item_title_view);
        Design.updateTextFont(mInformationView, Design.FONT_REGULAR34);
        mInformationView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mInformationView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_TEXT_MARGIN * Design.WIDTH_RATIO);
        mInformationView.setLayoutParams(marginLayoutParams);

        ImageView iconView = view.findViewById(R.id.update_available_item_image_view);

        layoutParams = iconView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_ICON_SIZE * Design.WIDTH_RATIO);
        layoutParams.height = (int) (DESIGN_ICON_SIZE * Design.HEIGHT_RATIO);
        iconView.setLayoutParams(layoutParams);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) iconView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_ICON_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_ICON_MARGIN * Design.WIDTH_RATIO);
        iconView.setLayoutParams(marginLayoutParams);
    }

    public void onBind(String version) {

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(itemView.getContext().getString(R.string.update_app_view_update_available));
        spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_DEFAULT), 0, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        if (!version.isEmpty()) {
            spannableStringBuilder.append("\n");
            int startSubTitle = spannableStringBuilder.length();
            spannableStringBuilder.append(version);
            spannableStringBuilder.setSpan(new RelativeSizeSpan(0.9f), startSubTitle, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_GREY), startSubTitle, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        mInformationView.setText(spannableStringBuilder);

        updateColor();
    }

    private void updateColor() {

        itemView.setBackgroundColor(Design.WHITE_COLOR);
    }
}
