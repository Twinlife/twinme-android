/*
 *  Copyright (c) 2023-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.cleanupActivity;

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

public class ExpirationDateViewHolder extends RecyclerView.ViewHolder {

    private final TextView mTitleView;

    public ExpirationDateViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = MenuCleanUpExpirationView.ITEM_VIEW_HEIGHT;
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Color.TRANSPARENT);

        View dateContainerView = view.findViewById(R.id.menu_expiration_date_container_view);
        layoutParams = dateContainerView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = Design.BUTTON_HEIGHT;

        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable confirmViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        confirmViewBackground.getPaint().setColor(Design.GREY_BACKGROUND_COLOR);
        dateContainerView.setBackground(confirmViewBackground);

        mTitleView = view.findViewById(R.id.menu_expiration_date_view);
        Design.updateTextFont(mTitleView, Design.FONT_REGULAR32);
        mTitleView.setTextColor(Color.BLACK);

    }

    public void onBind(String date) {

        mTitleView.setText(date);

        updateFont();
    }

    public void onViewRecycled() {

    }

    private void updateFont() {

        Design.updateTextFont(mTitleView, Design.FONT_REGULAR32);
    }
}