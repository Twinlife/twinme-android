/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.settingsActivity;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.RoundedView;

public class ColorViewHolder extends RecyclerView.ViewHolder {

    private static final float DESIGN_ITEM_VIEW_HEIGHT = 80f;
    private static final int ITEM_VIEW_HEIGHT;

    static {
        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
    }

    private final RoundedView mColorView;
    private final RoundedView mContentColorView;
    private final ImageView mContentNoColorView;
    private final View mSeparatorView;

    ColorViewHolder(View view, int width) {
        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = ITEM_VIEW_HEIGHT;
        layoutParams.width = width;
        view.setLayoutParams(layoutParams);

        mColorView = view.findViewById(R.id.personalization_activity_color_item_color_view);
        mColorView.setBorder(2.0f, Design.BLACK_COLOR);
        mColorView.setColor(Design.WHITE_COLOR);

        mContentColorView = view.findViewById(R.id.personalization_activity_color_item_color_content_view);

        mContentNoColorView = view.findViewById(R.id.personalization_activity_color_item_no_color_content_view);

        mSeparatorView = view.findViewById(R.id.personalization_activity_color_item_separator_view);
        mSeparatorView.setBackgroundColor(Color.argb(255, 35, 42, 69));
    }

    public void onBind(UICustomColor customColor) {

        if (customColor.getColor() != null) {
            mContentColorView.setColor(Color.parseColor(customColor.getColor()));
            mContentColorView.setVisibility(View.VISIBLE);
            mSeparatorView.setVisibility(View.INVISIBLE);
            mContentNoColorView.setVisibility(View.INVISIBLE);
        } else {
            mContentColorView.setVisibility(View.INVISIBLE);
            mSeparatorView.setVisibility(View.VISIBLE);
            mContentNoColorView.setVisibility(View.VISIBLE);
            mContentNoColorView.setImageDrawable(ResourcesCompat.getDrawable(itemView.getResources(), R.drawable.no_style_space, null));
        }

        if (customColor.isSelected()) {
            mColorView.setVisibility(View.VISIBLE);
            mColorView.setBorder(2.0f, Design.BLACK_COLOR);
            mColorView.setColor(Design.WHITE_COLOR);
        } else {
            mColorView.setVisibility(View.INVISIBLE);
        }
    }

    public void onBindEditStyle(boolean isSelected) {

        mContentColorView.setVisibility(View.INVISIBLE);
        mSeparatorView.setVisibility(View.INVISIBLE);
        mContentNoColorView.setVisibility(View.VISIBLE);
        mContentNoColorView.setImageDrawable(ResourcesCompat.getDrawable(itemView.getResources(), R.drawable.edit_style, null));

        if (isSelected) {
            mColorView.setVisibility(View.VISIBLE);
            mColorView.setBorder(2.0f, Design.BLACK_COLOR);
            mColorView.setColor(Design.WHITE_COLOR);
        } else {
            mColorView.setVisibility(View.INVISIBLE);
        }
    }
}
