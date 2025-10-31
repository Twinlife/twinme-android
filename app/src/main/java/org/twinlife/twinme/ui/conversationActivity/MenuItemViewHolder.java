/*
 *  Copyright (c) 2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.conversationActivity;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class MenuItemViewHolder extends RecyclerView.ViewHolder {
    private static final String LOG_TAG = "MenuItemViewHolder";
    private static final boolean DEBUG = false;

    private static final float DESIGN_ITEM_VIEW_HEIGHT = 90f;
    private static final int ITEM_VIEW_HEIGHT;

    static {
        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
    }

    private final TextView mTitleView;
    private final ImageView mImageView;
    private final View mSeparatorView;

    MenuItemViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = ITEM_VIEW_HEIGHT;
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Color.TRANSPARENT);

        mTitleView = view.findViewById(R.id.menu_item_child_title);
        Design.updateTextFont(mTitleView, Design.FONT_REGULAR34);
        mTitleView.setTextColor(Design.BLACK_COLOR);

        mImageView = view.findViewById(R.id.menu_item_child_image);

        mSeparatorView = view.findViewById(R.id.menu_item_child_separator);
        mSeparatorView.setBackgroundColor(Design.SEPARATOR_COLOR);
    }

    public void onBind(String title, Drawable image, int colorFilter, boolean isEnabled, boolean hideSeparator) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBind: title=" + title);
        }

        mTitleView.setText(title);
        mImageView.setImageDrawable(image);
        mImageView.setColorFilter(colorFilter);

        if (isEnabled) {
            mTitleView.setAlpha(1.0f);
            mImageView.setAlpha(1.0f);
        } else {
            mTitleView.setAlpha(0.5f);
            mImageView.setAlpha(0.5f);
        }

        if (hideSeparator) {
            mSeparatorView.setVisibility(View.GONE);
        } else {
            mSeparatorView.setVisibility(View.VISIBLE);
        }
    }

    public void onViewRecycled() {
    }
}
