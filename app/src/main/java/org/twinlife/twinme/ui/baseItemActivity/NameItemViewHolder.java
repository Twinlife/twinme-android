/*
 *  Copyright (c) 2018-2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

class NameItemViewHolder extends BaseItemViewHolder {

    private final TextView mTextView;

    NameItemViewHolder(BaseItemActivity baseItemActivity, View view) {

        super(baseItemActivity, view, R.id.base_item_activity_name_item_overlay_view);

        LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = NAME_ITEM_HEIGHT;
        mTextView = view.findViewById(R.id.base_item_activity_name_item_text);
        Design.updateTextFont(mTextView, Design.FONT_REGULAR24);
        mTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
    }

    @Override
    void onBind(Item item) {

        super.onBind(item);

        mTextView.setText(((NameItem) item).getName());
        mTextView.setTextColor(getBaseItemActivity().getCustomAppearance().getConversationBackgroundText());

        if (isMenuOpen()) {
            getOverlayView().setVisibility(View.VISIBLE);
        } else {
            getOverlayView().setVisibility(View.INVISIBLE);
        }
    }

    @Override
    void onViewRecycled() {

        mTextView.setText(null);
    }
}
