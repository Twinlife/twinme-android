/*
 *  Copyright (c) 2023-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.conversationFilesActivity;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class CustomTabViewHolder extends RecyclerView.ViewHolder {

    private static final float DESIGN_CONTAINER_MARGIN = 8f;
    private static final float DESIGN_TITLE_HEIGHT = 58f;
    private static final float DESIGN_TITLE_MARGIN = 14f;

    private final View mContainerView;
    private final TextView mTitleView;

    CustomTabViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int) (CustomTabView.DESIGN_CUSTOM_TAB_VIEW_HEIGHT * Design.HEIGHT_RATIO);
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Color.TRANSPARENT);

        mContainerView = view.findViewById(R.id.custom_tab_view_item_container_view);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mContainerView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_CONTAINER_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_CONTAINER_MARGIN * Design.WIDTH_RATIO);

        mTitleView = view.findViewById(R.id.custom_tab_view_item_title);
        Design.updateTextFont(mTitleView, Design.FONT_REGULAR34);
        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        layoutParams = mTitleView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_TITLE_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mTitleView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_TITLE_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_TITLE_MARGIN * Design.WIDTH_RATIO);
    }

    public void onBind(UICustomTab customTab, int mainColor, int textSelectedColor) {

        mTitleView.setText(customTab.getTitle());

        float radius = CustomTabView.DESIGN_CUSTOM_TAB_VIEW_HEIGHT * Design.HEIGHT_RATIO * 0.5f * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable containerViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        if (customTab.isSelected()) {
            containerViewBackground.getPaint().setColor(mainColor);
            mTitleView.setTextColor(textSelectedColor);
        } else {
            containerViewBackground.getPaint().setColor(Color.TRANSPARENT);
            mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);
        }

        mContainerView.setBackground(containerViewBackground);
    }
}
