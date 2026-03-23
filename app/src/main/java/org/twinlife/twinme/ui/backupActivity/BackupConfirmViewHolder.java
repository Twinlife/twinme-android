/*
 *  Copyright (c) 2024-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.backupActivity;

import android.graphics.Color;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class BackupConfirmViewHolder extends RecyclerView.ViewHolder {
    private static final String LOG_TAG = "BackupConfirmViewHolder";
    private static final boolean DEBUG = false;

    private static final float DESIGN_ICON_HEIGHT = 42;
    private static final float DESIGN_MARGIN = 32;
    private static final float DESIGN_CONTAINER_MARGIN = 60;

    private final ImageView mSelectedView;
    private final TextView mMessageView;

    public BackupConfirmViewHolder(@NonNull View view) {

        super(view);

        view.setBackgroundColor(Color.TRANSPARENT);

        View containerSelectedView = view.findViewById(R.id.create_backup_activity_confirm_item_layout_image);

        ViewGroup.LayoutParams layoutParams = containerSelectedView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_ICON_HEIGHT * Design.HEIGHT_RATIO);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) containerSelectedView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.topMargin = (int) (DESIGN_CONTAINER_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_CONTAINER_MARGIN * Design.HEIGHT_RATIO);

        mSelectedView = view.findViewById(R.id.create_backup_activity_confirm_item_selected_image);
        mSelectedView.setColorFilter(Design.getMainStyle());

        mMessageView = view.findViewById(R.id.create_backup_activity_confirm_item_message_view);
        mMessageView.setTypeface(Design.FONT_REGULAR32.typeface);
        mMessageView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_REGULAR32.size);
        mMessageView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mMessageView.getLayoutParams();
        marginLayoutParams.rightMargin = (int) (DESIGN_MARGIN * Design.WIDTH_RATIO);
    }

    public void onBind(boolean isSelected) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBind: " + isSelected);
        }

        if (isSelected) {
            mSelectedView.setVisibility(View.VISIBLE);
        } else {
            mSelectedView.setVisibility(View.INVISIBLE);
        }

        updateColor();
        updateFont();
    }

    private void updateColor() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateColor");
        }

        mMessageView.setTextColor(Design.FONT_COLOR_DEFAULT);
    }

    private void updateFont() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateFont");
        }

        mMessageView.setTypeface(Design.FONT_REGULAR32.typeface);
        mMessageView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_REGULAR32.size);
    }
}
