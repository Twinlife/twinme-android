/*
 *  Copyright (c) 2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.exportActivity;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class ExportProgressViewHolder extends RecyclerView.ViewHolder {

    private final ProgressBar mProgressBar;
    private final TextView mProgressView;
    private final TextView mMessageView;

    public ExportProgressViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = Design.EXPORT_VIEW_HEIGHT;
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Color.TRANSPARENT);

        mProgressBar = view.findViewById(R.id.export_activity_progress_item_progress_bar);
        mProgressBar.setProgressTintList(ColorStateList.valueOf(Design.getMainStyle()));
        mProgressBar.setProgressBackgroundTintList(ColorStateList.valueOf(Color.WHITE));

        mProgressView = view.findViewById(R.id.export_activity_progress_item_progress_view);
        Design.updateTextFont(mProgressView, Design.FONT_BOLD34);
        mProgressView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mMessageView = view.findViewById(R.id.export_activity_progress_item_message_view);
        Design.updateTextFont(mMessageView, Design.FONT_REGULAR34);
        mMessageView.setTextColor(Design.FONT_COLOR_DEFAULT);
    }

    public void onBind(int progress, String title) {

        mProgressBar.setProgress(progress);

        String progressValue = progress + "%";
        mProgressView.setText(progressValue);

        mMessageView.setText(title);

        updateFont();
        updateColor();
    }

    private void updateFont() {

        Design.updateTextFont(mProgressView, Design.FONT_BOLD34);
        Design.updateTextFont(mMessageView, Design.FONT_REGULAR34);
    }

    private void updateColor() {

        mProgressBar.setProgressTintList(ColorStateList.valueOf(Design.getMainStyle()));
        mProgressBar.setProgressBackgroundTintList(ColorStateList.valueOf(Color.WHITE));

        mProgressView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mMessageView.setTextColor(Design.FONT_COLOR_DEFAULT);
    }
}