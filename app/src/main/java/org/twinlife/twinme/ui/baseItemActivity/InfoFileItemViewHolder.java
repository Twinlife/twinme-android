/*
 *  Copyright (c) 2019-2020 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

class InfoFileItemViewHolder extends BaseItemViewHolder {

    private static final float DESIGN_TEXT_TOP_MARGIN = 16f;
    private static final int TEXT_TOP_MARGIN;

    static {
        TEXT_TOP_MARGIN = (int) (DESIGN_TEXT_TOP_MARGIN * Design.HEIGHT_RATIO);
    }

    private final TextView mInfoFileTextView;

    InfoFileItemViewHolder(BaseItemActivity baseItemActivity, View view) {
        super(baseItemActivity, view);

        mInfoFileTextView = view.findViewById(R.id.base_info_activity_info_file_item_text_view);
        Design.updateTextFont(mInfoFileTextView, Design.FONT_REGULAR32);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mInfoFileTextView.getLayoutParams();
        marginLayoutParams.topMargin = TEXT_TOP_MARGIN;
        marginLayoutParams.bottomMargin = TEXT_TOP_MARGIN;
    }

    @Override
    void onBind(Item item) {

        if (!(item instanceof InfoFileItem)) {
            return;
        }

        InfoFileItem infoItem = (InfoFileItem) item;

        if (infoItem.getItem().getType() == Item.ItemType.CALL) {
            CallItem callItem = (CallItem) infoItem.getItem();
            if (getBaseItemActivity().getContact() != null) {
                mInfoFileTextView.setText(callItem.getInformation(getBaseItemActivity(), getBaseItemActivity().getContact().getName()));
            }
        } else if (infoItem.getItem().getType() == Item.ItemType.PEER_CALL) {
            PeerCallItem peerCallItem = (PeerCallItem) infoItem.getItem();
            if (getBaseItemActivity().getContact() != null) {
                mInfoFileTextView.setText(peerCallItem.getInformation(getBaseItemActivity(), getBaseItemActivity().getContact().getName()));
            }
        } else {
            mInfoFileTextView.setText(infoItem.getItem().getInformation(getBaseItemActivity()));
        }
    }
}
