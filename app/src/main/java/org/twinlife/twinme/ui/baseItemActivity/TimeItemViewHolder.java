/*
 *  Copyright (c) 2016-2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.skin.TextStyle;
import org.twinlife.twinme.utils.CommonUtils;

class TimeItemViewHolder extends BaseItemViewHolder {

    private final TextView mTextView;

    TimeItemViewHolder(BaseItemActivity baseItemActivity, View view) {

        super(baseItemActivity, view, R.id.base_item_activity_time_item_overlay_view);

        LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = TIME_ITEM_HEIGHT;
        mTextView = view.findViewById(R.id.base_item_activity_time_item_text);
        TextStyle textStyle = Design.FONT_MEDIUM24;
        mTextView.setTypeface(textStyle.typeface);
        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textStyle.size);
        mTextView.setTextColor(getBaseItemActivity().getCustomAppearance().getConversationBackgroundText());
    }

    @Override
    void onBind(Item item) {

        super.onBind(item);

        mTextView.setText(CommonUtils.formatItemInterval(getBaseItemActivity(), item.getTimestamp()));

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
