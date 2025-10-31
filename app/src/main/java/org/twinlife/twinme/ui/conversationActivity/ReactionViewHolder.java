/*
 *  Copyright (c) 2023 twinlife SA.
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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class ReactionViewHolder extends RecyclerView.ViewHolder {
    private static final String LOG_TAG = "ReactionViewHolder";
    private static final boolean DEBUG = false;

    private static final float DESIGN_ITEM_VIEW_WIDTH = 76f;
    private static final float DESIGN_ITEM_VIEW_HEIGHT = 92f;
    private static final int ITEM_VIEW_WIDTH;
    private static final int ITEM_VIEW_HEIGHT;

    static {
        ITEM_VIEW_WIDTH = (int) (DESIGN_ITEM_VIEW_WIDTH * Design.WIDTH_RATIO);
        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
    }

    private final ImageView mImageView;

    ReactionViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = ITEM_VIEW_WIDTH;
        layoutParams.height = ITEM_VIEW_HEIGHT;
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Color.TRANSPARENT);

        mImageView = view.findViewById(R.id.menu_reaction_child_image);
    }

    public void onBind(Drawable image) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBind: image=" + image);
        }

        mImageView.setImageDrawable(image);
    }

    public void onViewRecycled() {
    }
}
