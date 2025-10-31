/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.conversationActivity;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.SwitchView;

public class MenuSendOptionViewHolder extends RecyclerView.ViewHolder {

    private static final float DESIGN_ICON_SIZE = 42f;
    private static final float DESIGN_ICON_MARGIN = 34f;

    private final SwitchView mSwitchView;
    private final ImageView mIconView;
    private final View mSeparatorView;

    private boolean mForceDarkMode = false;
    private int mBackgroundColor =  Design.POPUP_BACKGROUND_COLOR;

    public MenuSendOptionViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = Design.SECTION_HEIGHT;
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Design.POPUP_BACKGROUND_COLOR);

        mIconView = view.findViewById(R.id.menu_send_option_item_icon);
        mIconView.setColorFilter(Design.BLACK_COLOR);

        layoutParams = mIconView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_ICON_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_ICON_SIZE * Design.HEIGHT_RATIO);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mIconView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_ICON_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_ICON_MARGIN * Design.WIDTH_RATIO);

        mSwitchView = view.findViewById(R.id.menu_send_option_item_checkbox);
        Design.updateTextFont(mSwitchView, Design.FONT_REGULAR32);
        mSwitchView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mSwitchView.getLayoutParams();
        marginLayoutParams.rightMargin = (int) (DESIGN_ICON_MARGIN * Design.WIDTH_RATIO);

        mSeparatorView = view.findViewById(R.id.menu_send_option_item_separator_view);
    }

    public void onBind(@NonNull String title, int icon, int tag, boolean isSelected, boolean isEnable, boolean forceDarkMode, int backgroundColor, boolean hideSeparator, CompoundButton.OnCheckedChangeListener onCheckedChangeListener) {

        mBackgroundColor = backgroundColor;
        mForceDarkMode = forceDarkMode;

        mIconView.setImageDrawable(ResourcesCompat.getDrawable(itemView.getResources(), icon, null));

        mSwitchView.setText(title);
        mSwitchView.setOnCheckedChangeListener(null);
        mSwitchView.setChecked(isSelected);

        if (isEnable) {
            mSwitchView.setEnabled(true);
            mSwitchView.setOnCheckedChangeListener(onCheckedChangeListener);
        } else {
            mSwitchView.setEnabled(false);
            mSwitchView.setClickable(false);
        }

        if (hideSeparator) {
            mSeparatorView.setVisibility(View.GONE);
        } else {
            mSeparatorView.setVisibility(View.VISIBLE);
        }

        updateFont();
        updateColor();
    }

    private void updateFont() {

        Design.updateTextFont(mSwitchView, Design.FONT_REGULAR32);
    }

    private void updateColor() {

        if (mForceDarkMode) {
            itemView.setBackgroundColor(Color.rgb(72,72,72));
            mSwitchView.setTextColor(Color.WHITE);
            mIconView.setColorFilter(Color.WHITE);
        } else {
            itemView.setBackgroundColor(mBackgroundColor);
            mSwitchView.setTextColor(Design.FONT_COLOR_DEFAULT);
            mIconView.setColorFilter(Design.BLACK_COLOR);
        }
    }
}