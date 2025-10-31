/*
 *  Copyright (c) 2022 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.spaces;

import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.Utils;

public class SettingValueSpaceViewHolder extends RecyclerView.ViewHolder {

    public interface Observer {

        void onSettingClick(String spaceSettingProperty);
    }

    private static final float DESIGN_ITEM_VIEW_HEIGHT = 120f;
    private static final int ITEM_VIEW_HEIGHT;

    static {
        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
    }

    private final TextView mTextView;
    private final TextView mValueView;

    private String mSpaceSettingProperty;

    private final Observer mObserver;

    public SettingValueSpaceViewHolder(@NonNull View view, Observer observer) {

        super(view);

        mObserver = observer;

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = ITEM_VIEW_HEIGHT;
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Design.WHITE_COLOR);

        mTextView = view.findViewById(R.id.settings_activity_item_title_view);
        mTextView.setTypeface(Design.FONT_REGULAR32.typeface);
        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_REGULAR32.size);
        mTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mValueView = view.findViewById(R.id.settings_activity_item_value_view);
        mValueView.setTypeface(Design.FONT_REGULAR32.typeface);
        mValueView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_REGULAR32.size);
        mValueView.setTextColor(Design.FONT_COLOR_DEFAULT);

        itemView.setOnClickListener(v -> {
            mObserver.onSettingClick(mSpaceSettingProperty);
        });
    }

    public void onBind(String spaceSettingProperty, String title, long value, boolean visible) {

        mSpaceSettingProperty = spaceSettingProperty;

        mTextView.setText(title);
        mValueView.setText(Utils.formatTimeout(mValueView.getContext(), value));

        ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
        if (visible) {
            layoutParams.height = ITEM_VIEW_HEIGHT;
        } else {
            layoutParams.height = 0;
        }
        updateFont();
        updateColor();
    }

    private void updateFont() {

        mTextView.setTypeface(Design.FONT_REGULAR32.typeface);
        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_REGULAR32.size);
    }

    private void updateColor() {

        itemView.setBackgroundColor(Design.WHITE_COLOR);
        mTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
    }
}