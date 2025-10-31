/*
 *  Copyright (c) 2020 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.settingsActivity;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.RoundedView;

public class AppearanceColorViewHolder extends RecyclerView.ViewHolder {
    private static final String LOG_TAG = "AppareanceColorViewH...";
    private static final boolean DEBUG = false;

    private static final float DESIGN_ITEM_VIEW_HEIGHT = 120f;
    private static final int ITEM_VIEW_HEIGHT;

    static {
        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
    }

    private final TextView mColorNameView;
    private final RoundedView mColorView;
    private final ImageView mNoColorImageView;
    private final View mSeparatorView;

    AppearanceColorViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = ITEM_VIEW_HEIGHT;
        view.setLayoutParams(layoutParams);

        mColorNameView = view.findViewById(R.id.personalization_activity_appearance_color_item_color_name_view);
        Design.updateTextFont(mColorNameView, Design.FONT_REGULAR32);
        mColorNameView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mColorView = view.findViewById(R.id.personalization_activity_appearance_color_item_color_view);
        mColorView.setBorder(Design.BORDER_WIDTH, Design.ITEM_BORDER_COLOR);

        mNoColorImageView = view.findViewById(R.id.personalization_activity_appearance_color_item_no_color_content_view);

        mSeparatorView = view.findViewById(R.id.personalization_activity_appearance_color_item_separator_view);
        mSeparatorView.setBackgroundColor(Design.SEPARATOR_COLOR);
    }

    public void onBind(int color, String nameColor, Bitmap bitmap, boolean hideSeparator) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBind: color=" + color + " nameColor " + nameColor);
        }

        mColorNameView.setText(nameColor);

        if (bitmap != null) {
            mColorView.setVisibility(View.GONE);
            mNoColorImageView.setVisibility(View.VISIBLE);
            mNoColorImageView.setImageBitmap(bitmap);
        } else {
            mColorView.setColor(color);
            mColorView.setVisibility(View.VISIBLE);
            mNoColorImageView.setVisibility(View.GONE);
            mColorView.invalidate();
        }

        if (hideSeparator) {
            mSeparatorView.setVisibility(View.GONE);
        } else {
            mSeparatorView.setVisibility(View.VISIBLE);
        }

        updateColor();
        updateFont();
    }

    public void onViewRecycled() {

    }

    private void updateFont() {

        Design.updateTextFont(mColorNameView, Design.FONT_REGULAR32);
    }

    private void updateColor() {

        mColorNameView.setTextColor(Design.FONT_COLOR_DEFAULT);
        itemView.setBackgroundColor(Design.WHITE_COLOR);
    }
}
