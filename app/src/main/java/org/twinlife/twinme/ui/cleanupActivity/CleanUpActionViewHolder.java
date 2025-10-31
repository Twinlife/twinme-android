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

public class CleanUpActionViewHolder extends RecyclerView.ViewHolder {

    private final View mActionView;
    private final TextView mTitleView;

    public CleanUpActionViewHolder(@NonNull View view, CleanUpActivity cleanUpActivity) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = Design.EXPORT_VIEW_HEIGHT;
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Color.TRANSPARENT);

        mActionView = view.findViewById(R.id.cleanup_activity_action_item_action_view);
        mActionView.setOnClickListener(v -> cleanUpActivity.onActionClick());

        layoutParams = mActionView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = Design.BUTTON_HEIGHT;

        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable actionViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        actionViewBackground.getPaint().setColor(Design.DELETE_COLOR_RED);
        mActionView.setBackground(actionViewBackground);

        mTitleView = view.findViewById(R.id.cleanup_activity_action_item_text_view);
        Design.updateTextFont(mTitleView, Design.FONT_BOLD34);
        mTitleView.setTextColor(Color.WHITE);
    }

    public void onBind(boolean enable) {

        if (enable) {
            mActionView.setAlpha(1f);
        } else {
            mActionView.setAlpha(0.5f);
        }

        updateFont();
    }

    private void updateFont() {

        Design.updateTextFont(mTitleView, Design.FONT_BOLD34);
    }
}