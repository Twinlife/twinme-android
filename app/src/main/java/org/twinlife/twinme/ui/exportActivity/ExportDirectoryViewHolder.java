/*
 *  Copyright (c) 2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.exportActivity;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.settingsActivity.UISetting;

public class ExportDirectoryViewHolder extends RecyclerView.ViewHolder {

    private final EditText mEditTextView;
    private final TextView mDirectoryView;
    private UISetting<String> mUISetting;

    public ExportDirectoryViewHolder(@NonNull View view, ExportActivity exportActivity) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = Design.EXPORT_VIEW_HEIGHT;
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Color.TRANSPARENT);

        mEditTextView = view.findViewById(R.id.export_activity_directory_item_edit_text);
        Design.updateTextFont(mEditTextView, Design.FONT_REGULAR32);
        mEditTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mDirectoryView = view.findViewById(R.id.export_activity_directory_item_text);
        Design.updateTextFont(mDirectoryView, Design.FONT_REGULAR32);
        mDirectoryView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mEditTextView.setVisibility(View.GONE);
        mDirectoryView.setVisibility(View.VISIBLE);
    }

    public void onBind(@NonNull UISetting<String> uiSetting) {

        mUISetting = uiSetting;

        mEditTextView.setText(uiSetting.getTitle());
        mDirectoryView.setText(uiSetting.getTitle());

        if (uiSetting.getTitle().equals(itemView.getContext().getString(R.string.settings_activity_default_directory_title))) {
            mDirectoryView.setTextColor(Design.GREY_COLOR);
        } else {
            mDirectoryView.setTextColor(Design.FONT_COLOR_DEFAULT);
        }

        updateFont();
        updateColor();
    }

    private void updateFont() {

        Design.updateTextFont(mEditTextView, Design.FONT_REGULAR32);
        Design.updateTextFont(mDirectoryView, Design.FONT_REGULAR32);
    }

    private void updateColor() {

        itemView.setBackgroundColor(Design.WHITE_COLOR);
        mEditTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
    }
}
