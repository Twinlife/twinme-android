/*
 *  Copyright (c) 2020-2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.settingsActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class SettingEditViewHolder extends RecyclerView.ViewHolder {

    private final EditText mEditTextView;

    private UISetting<String> mUISetting;

    public SettingEditViewHolder(@NonNull View view, AbstractSettingsActivity settingsActivity) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = Design.SECTION_HEIGHT;
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Design.WHITE_COLOR);

        mEditTextView = view.findViewById(R.id.settings_activity_item_edit_title_view);
        Design.updateTextFont(mEditTextView, Design.FONT_REGULAR32);
        mEditTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mEditTextView.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {

                settingsActivity.onEditSetting(mUISetting, mEditTextView.getText().toString().trim());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
        });
    }

    public void onBind(@NonNull UISetting<String> uiSetting) {

        mUISetting = uiSetting;

        mEditTextView.setText(uiSetting.getTitle());

        updateFont();
        updateColor();
    }

    private void updateFont() {

        Design.updateTextFont(mEditTextView, Design.FONT_REGULAR32);
    }

    private void updateColor() {

        itemView.setBackgroundColor(Design.WHITE_COLOR);
        mEditTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
    }
}
