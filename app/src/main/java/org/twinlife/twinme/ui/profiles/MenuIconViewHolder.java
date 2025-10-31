/*
 *  Copyright (c) 2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.profiles;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.UIMenuSelectAction;

public class MenuIconViewHolder extends RecyclerView.ViewHolder {
    private static final String LOG_TAG = "MenuIconViewHolder";
    private static final boolean DEBUG = false;

    private static final float DESIGN_ITEM_VIEW_HEIGHT = 124;
    private static final int ITEM_VIEW_HEIGHT;

    static {
        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
    }

    private final ImageView mIconView;
    private final TextView mTitleView;
    private final View mSeparatorView;

    public MenuIconViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = ITEM_VIEW_HEIGHT;
        view.setLayoutParams(layoutParams);

        mIconView = view.findViewById(R.id.menu_icon_item_image_view);
        mIconView.setColorFilter(Design.BLACK_COLOR);

        mTitleView = view.findViewById(R.id.menu_icon_item_title_view);
        Design.updateTextFont(mTitleView, Design.FONT_REGULAR34);
        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mSeparatorView = view.findViewById(R.id.menu_icon_item_separator_view);
        mSeparatorView.setBackgroundColor(Design.SEPARATOR_COLOR);
    }

    public void onBind(UIMenuSelectAction uiMenuSelectAction, boolean hideSeparator) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBind: title=" + uiMenuSelectAction.getTitle());
        }

        itemView.setBackgroundColor(Design.POPUP_BACKGROUND_COLOR);
        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mTitleView.setText(uiMenuSelectAction.getTitle());

        mIconView.setImageDrawable(ResourcesCompat.getDrawable(itemView.getResources(), uiMenuSelectAction.getIcon(), null));

        if (hideSeparator) {
            mSeparatorView.setVisibility(View.GONE);
        } else {
            mSeparatorView.setVisibility(View.VISIBLE);
        }
    }

    public void onViewRecycled() {

    }
}
