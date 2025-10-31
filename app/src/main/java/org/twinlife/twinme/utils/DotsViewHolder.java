/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.utils;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class DotsViewHolder extends RecyclerView.ViewHolder {

    private static final int DESIGN_DOT_INACTIVE_COLOR = Color.rgb(244, 244, 244);

    final RoundedView mDotView;

    DotsViewHolder(View view) {
        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = Design.DOT_SIZE;
        layoutParams.height = Design.DOT_SIZE;
        view.setLayoutParams(layoutParams);

        mDotView = view.findViewById(R.id.dot_item_rounded_view);
        mDotView.setColor(DESIGN_DOT_INACTIVE_COLOR);
    }

    public void onBind(boolean isCurrentPosition) {

        mDotView.invalidate();
        if (isCurrentPosition) {
            mDotView.setColor(Design.getMainStyle());
        } else {
            mDotView.setColor(DESIGN_DOT_INACTIVE_COLOR);
        }
    }
}