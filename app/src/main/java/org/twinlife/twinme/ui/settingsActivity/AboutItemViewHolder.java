/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.settingsActivity;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class AboutItemViewHolder extends RecyclerView.ViewHolder {

    private static final float DESIGN_VERTICAL_MARGIN = 20f;
    private static final float DESIGN_HORIZONTAL_MARGIN = 34f;

    private final TextView mInformationView;

    public AboutItemViewHolder(@NonNull View view) {

        super(view);

        mInformationView = view.findViewById(R.id.about_activity_item_text_view);
        Design.updateTextFont(mInformationView, Design.FONT_REGULAR34);
        mInformationView.setTextColor(Design.FONT_COLOR_DEFAULT);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mInformationView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_VERTICAL_MARGIN * Design.HEIGHT_RATIO);;
        marginLayoutParams.bottomMargin = (int) (DESIGN_VERTICAL_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.leftMargin = (int) (DESIGN_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
    }

    public void onBind(boolean isCopyright) {

        if (isCopyright) {
            mInformationView.setText(R.string.about_view_copyright);
            mInformationView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        } else {
            mInformationView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        }

        updateFont();
        updateColor();
    }

    private void updateFont() {

        Design.updateTextFont(mInformationView, Design.FONT_REGULAR34);
    }

    private void updateColor() {

        itemView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        mInformationView.setTextColor(Design.FONT_COLOR_DEFAULT);
    }
}
