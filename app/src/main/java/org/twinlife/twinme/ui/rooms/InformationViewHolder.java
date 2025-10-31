/*
 *  Copyright (c) 2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.rooms;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class InformationViewHolder extends RecyclerView.ViewHolder {

    private static final float DESIGN_TEXT_TOP_MARGIN = 20f;
    private static final int TEXT_TOP_MARGIN;

    static {
        TEXT_TOP_MARGIN = (int) (DESIGN_TEXT_TOP_MARGIN * Design.HEIGHT_RATIO);
    }

    private final TextView mInformationView;

    public InformationViewHolder(@NonNull View view) {

        super(view);

        mInformationView = view.findViewById(R.id.settings_room_activity_information_item_title_view);
        Design.updateTextFont(mInformationView, Design.FONT_REGULAR28);
        mInformationView.setTextColor(Design.FONT_COLOR_GREY);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mInformationView.getLayoutParams();
        marginLayoutParams.topMargin = TEXT_TOP_MARGIN;
    }

    public void onBind(String information, boolean isSubTitle) {

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mInformationView.getLayoutParams();
        if (isSubTitle) {
            marginLayoutParams.topMargin = 0;
            marginLayoutParams.bottomMargin = TEXT_TOP_MARGIN;
        } else {
            marginLayoutParams.topMargin = TEXT_TOP_MARGIN;
            marginLayoutParams.bottomMargin = 0;
        }

        mInformationView.setText(information);
        updateFont();
        updateColor();
    }

    public void onViewRecycled() {

    }

    private void updateFont() {

        Design.updateTextFont(mInformationView, Design.FONT_REGULAR28);
    }

    private void updateColor() {

        itemView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        mInformationView.setTextColor(Design.FONT_COLOR_GREY);
    }
}
