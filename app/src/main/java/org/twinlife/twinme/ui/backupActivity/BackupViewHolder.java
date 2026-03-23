/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.backupActivity;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class BackupViewHolder extends RecyclerView.ViewHolder {
    private static final String LOG_TAG = "BackupViewHolder";
    private static final boolean DEBUG = false;

    private static final int DESIGN_INFO_MARGIN = 34;

    private final TextView mInfoView;
    private final View mSeparatorView;

    BackupViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = Design.ITEM_VIEW_HEIGHT;
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Design.WHITE_COLOR);

        mInfoView = view.findViewById(R.id.backup_item_info_view);
        mInfoView.setTypeface(Design.FONT_REGULAR32.typeface);
        mInfoView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_REGULAR32.size);
        mInfoView.setTextColor(Design.FONT_COLOR_DEFAULT);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mInfoView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_INFO_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_INFO_MARGIN * Design.WIDTH_RATIO);

        mSeparatorView  = view.findViewById(R.id.backup_item_separator_view);
        mSeparatorView.setBackgroundColor(Design.SEPARATOR_COLOR);
    }

    public void onBind(UIBackupInfo backupInfo, boolean hideSeparator) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBind");
        }

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(backupInfo.getId().toString());
        spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_DEFAULT), 0, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        spannableStringBuilder.append("\n");
        int startDate = spannableStringBuilder.length();
        spannableStringBuilder.append(backupInfo.formatDate(itemView.getContext()));
        spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_GREY), startDate, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mInfoView.setText(spannableStringBuilder);

        if (hideSeparator) {
            mSeparatorView.setVisibility(View.GONE);
        } else {
            mSeparatorView.setVisibility(View.VISIBLE);
        }
    }
}
