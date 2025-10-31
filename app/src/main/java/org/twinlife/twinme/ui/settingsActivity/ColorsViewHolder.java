/*
 *  Copyright (c) 2020 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.settingsActivity;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class ColorsViewHolder extends RecyclerView.ViewHolder {

    private static final float DESIGN_ITEM_VIEW_HEIGHT = 120f;
    private static final float DESIGN_COLOR_VIEW_WIDTH = 70f;
    private static final int ITEM_VIEW_HEIGHT;
    private static final int COLOR_VIEW_WIDTH;

    static {
        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
        COLOR_VIEW_WIDTH = (int) (DESIGN_COLOR_VIEW_WIDTH * Design.WIDTH_RATIO);
    }

    private final ColorsAdapter mColorsAdapter;

    public ColorsViewHolder(@NonNull View view, PersonalizationActivity personalizationActivity, ColorsAdapter.OnColorClickListener onColorClickListener) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = ITEM_VIEW_HEIGHT;
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Design.WHITE_COLOR);

        mColorsAdapter = new ColorsAdapter(personalizationActivity, Design.mainColors(), onColorClickListener, COLOR_VIEW_WIDTH);
        LinearLayoutManager uiColorsLinearLayoutManager = new LinearLayoutManager(personalizationActivity, RecyclerView.HORIZONTAL, false);
        RecyclerView colorsRecyclerView = view.findViewById(R.id.personalization_activity_color_list_view);
        colorsRecyclerView.setLayoutManager(uiColorsLinearLayoutManager);
        colorsRecyclerView.setAdapter(mColorsAdapter);
        colorsRecyclerView.setItemAnimator(null);
    }

    public void onBind() {

        mColorsAdapter.notifyDataSetChanged();

        updateColor();
    }

    private void updateColor() {

        itemView.setBackgroundColor(Design.WHITE_COLOR);
    }
}