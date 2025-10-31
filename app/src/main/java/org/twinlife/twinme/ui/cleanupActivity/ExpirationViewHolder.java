/*
 *  Copyright (c) 2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.cleanupActivity;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class ExpirationViewHolder extends RecyclerView.ViewHolder {

    private final TextView mTextView;
    private final TextView mValueView;

    public ExpirationViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = Design.EXPORT_VIEW_HEIGHT;
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Design.WHITE_COLOR);

        mTextView = view.findViewById(R.id.cleanup_activity_value_item_title_view);
        Design.updateTextFont(mTextView, Design.FONT_REGULAR32);
        mTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mValueView = view.findViewById(R.id.cleanup_activity_value_item_value_view);
        Design.updateTextFont(mValueView, Design.FONT_REGULAR32);
        mValueView.setTextColor(Design.FONT_COLOR_DEFAULT);
    }

    public void onBind(String title, String value) {

        mTextView.setText(title);
        mValueView.setText(value);

        updateFont();
        updateColor();
    }

    private void updateFont() {

        Design.updateTextFont(mTextView, Design.FONT_REGULAR32);
        Design.updateTextFont(mValueView, Design.FONT_REGULAR32);
    }

    private void updateColor() {

        itemView.setBackgroundColor(Design.WHITE_COLOR);
        mTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mValueView.setTextColor(Design.FONT_COLOR_DEFAULT);
    }
}