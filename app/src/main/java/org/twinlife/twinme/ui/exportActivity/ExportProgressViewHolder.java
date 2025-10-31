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

    private static final float DESIGN_PROGRESS_VIEW_HEIGHT = 8f;
    private static final float DESIGN_PROGRESS_VIEW_MARGIN = 30f;
    private static final float DESIGN_TEXT_MARGIN = 10f;

    private final ProgressBar mProgressBar;
    private final TextView mProgressView;
    private final TextView mMessageView;

    public ExportProgressViewHolder(@NonNull View view) {

        super(view);

        view.setBackgroundColor(Color.TRANSPARENT);

        mProgressBar = view.findViewById(R.id.export_activity_progress_item_progress_bar);
        mProgressBar.setProgressTintList(ColorStateList.valueOf(Design.getMainStyle()));
        mProgressBar.setProgressBackgroundTintList(ColorStateList.valueOf(Color.WHITE));

        ViewGroup.LayoutParams layoutParams = mProgressBar.getLayoutParams();
        layoutParams.height = (int) (DESIGN_PROGRESS_VIEW_HEIGHT * Design.HEIGHT_RATIO);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mProgressBar.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_PROGRESS_VIEW_MARGIN * Design.HEIGHT_RATIO);

        mProgressView = view.findViewById(R.id.export_activity_progress_item_progress_view);
        Design.updateTextFont(mProgressView, Design.FONT_BOLD34);
        mProgressView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mProgressView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_TEXT_MARGIN * Design.HEIGHT_RATIO);

        mMessageView = view.findViewById(R.id.export_activity_progress_item_message_view);
        Design.updateTextFont(mMessageView, Design.FONT_REGULAR34);
        mMessageView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mMessageView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_TEXT_MARGIN * Design.HEIGHT_RATIO);
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