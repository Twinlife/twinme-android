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
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class PreviewAddViewHolder extends RecyclerView.ViewHolder {

    private static final float DESIGN_ITEM_VIEW = 120f;
    private static final float DESIGN_CONTAINER_MARGIN = 10f;
    private static final float DESIGN_ICON_SIZE = 50f;

    public PreviewAddViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = (int) (DESIGN_ITEM_VIEW * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_ITEM_VIEW * Design.HEIGHT_RATIO);
        view.setLayoutParams(layoutParams);

        view.setBackgroundColor(Color.TRANSPARENT);

        View containerView = view.findViewById(R.id.preview_add_item_container);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) containerView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_CONTAINER_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_CONTAINER_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.topMargin = (int) (DESIGN_CONTAINER_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_CONTAINER_MARGIN * Design.HEIGHT_RATIO);

        ImageView iconView = view.findViewById(R.id.preview_add_item_icon);

        layoutParams = iconView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_ICON_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_ICON_SIZE * Design.HEIGHT_RATIO);


    }

    public void onViewRecycled() {

    }
}