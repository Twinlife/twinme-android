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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

class InfoFileItemViewHolder extends BaseItemViewHolder {

    private static final float DESIGN_MINIMUM_HEIGHT = 120f;
    private static final float DESIGN_ICON_SIZE = 36f;
    private static final float DESIGN_ICON_MARGIN = 34f;
    private static final float DESIGN_TEXT_TOP_MARGIN = 16f;
    private static final int TEXT_TOP_MARGIN;

    static {
        TEXT_TOP_MARGIN = (int) (DESIGN_TEXT_TOP_MARGIN * Design.HEIGHT_RATIO);
    }

    private final ImageView mIconView;
    private final TextView mInfoFileTextView;

    InfoFileItemViewHolder(BaseItemActivity baseItemActivity, View view) {
        super(baseItemActivity, view);

        view.setMinimumHeight((int)(DESIGN_MINIMUM_HEIGHT * Design.HEIGHT_RATIO));

        mInfoFileTextView = view.findViewById(R.id.base_info_activity_info_file_item_text_view);
        Design.updateTextFont(mInfoFileTextView, Design.FONT_REGULAR32);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mInfoFileTextView.getLayoutParams();
        marginLayoutParams.topMargin = TEXT_TOP_MARGIN;
        marginLayoutParams.bottomMargin = TEXT_TOP_MARGIN;

        mIconView = view.findViewById(R.id.base_info_activity_info_file_item_icon_view);
        mIconView.setColorFilter(Design.BLACK_COLOR);

        ViewGroup.LayoutParams layoutParams = mIconView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_ICON_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_ICON_SIZE * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mIconView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_ICON_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_ICON_MARGIN * Design.WIDTH_RATIO);
    }

    @Override
    void onBind(Item item) {

        if (!(item instanceof InfoFileItem)) {
            return;
        }

        InfoFileItem infoItem = (InfoFileItem) item;

        if (infoItem.getItem().getType() == Item.ItemType.CALL) {
            CallItem callItem = (CallItem) infoItem.getItem();
            int iconName = callItem.getCallDescriptor().isVideo() ? R.drawable.video_call : R.drawable.audio_call;
            mIconView.setImageDrawable(ResourcesCompat.getDrawable(itemView.getResources(), iconName, null));
            if (getBaseItemActivity().getContact() != null) {
                mInfoFileTextView.setText(callItem.getInformation(getBaseItemActivity(), getBaseItemActivity().getContact().getName()));
            }
        } else if (infoItem.getItem().getType() == Item.ItemType.PEER_CALL) {
            PeerCallItem peerCallItem = (PeerCallItem) infoItem.getItem();
            int iconName = peerCallItem.getCallDescriptor().isVideo() ? R.drawable.video_call : R.drawable.audio_call;
            mIconView.setImageDrawable(ResourcesCompat.getDrawable(itemView.getResources(), iconName, null));
            if (getBaseItemActivity().getContact() != null) {
                mInfoFileTextView.setText(peerCallItem.getInformation(getBaseItemActivity(), getBaseItemActivity().getContact().getName()));
            }
        } else if (infoItem.getItem().getType() == Item.ItemType.LOCATION || infoItem.getItem().getType() == Item.ItemType.PEER_LOCATION) {
            mIconView.setImageDrawable(ResourcesCompat.getDrawable(itemView.getResources(), R.drawable.call_location_icon, null));
            mInfoFileTextView.setText(infoItem.getItem().getInformation(getBaseItemActivity()));
        } else {
            mIconView.setImageDrawable(ResourcesCompat.getDrawable(itemView.getResources(), R.drawable.notification_file_message, null));
            mInfoFileTextView.setText(infoItem.getItem().getInformation(getBaseItemActivity()));
        }
    }
}
