/*
 *  Copyright (c) 2019-2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.CircularImageDescriptor;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.CircularImageView;
import org.twinlife.twinme.utils.CommonUtils;

class InfoDateItemViewHolder extends BaseItemViewHolder {

    private static final int DATE_COLOR = Color.argb(255, 119, 138, 159);

    private static final float DESIGN_AVATAR_SIZE = 84;
    private static final float DESIGN_DATE_WIDTH = 200;
    private static final float DESIGN_HORIZONTAL_MARGIN = 32;

    private static final float DESIGN_ITEM_VIEW_HEIGHT = 120f;
    private static final int ITEM_VIEW_HEIGHT;

    static {
        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
    }

    private final CircularImageView mAvatarView;
    private final TextView mNameView;
    private final TextView mDateTextView;

    InfoDateItemViewHolder(BaseItemActivity baseItemActivity, View view) {
        super(baseItemActivity, view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = ITEM_VIEW_HEIGHT;
        view.setLayoutParams(layoutParams);

        mAvatarView = view.findViewById(R.id.base_item_activity_info_date_item_avatar_view);

        layoutParams = mAvatarView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_AVATAR_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_AVATAR_SIZE * Design.HEIGHT_RATIO);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mAvatarView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);

        mNameView = view.findViewById(R.id.base_item_activity_info_date_item_name_view);
        Design.updateTextFont(mNameView, Design.FONT_REGULAR30);
        mNameView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mNameView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);

        mDateTextView = view.findViewById(R.id.base_item_activity_info_date_item_date_text_view);
        Design.updateTextFont(mDateTextView, Design.FONT_MEDIUM28);
        mDateTextView.setTextColor(DATE_COLOR);

        layoutParams = mDateTextView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_DATE_WIDTH * Design.WIDTH_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mDateTextView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
    }

    @Override
    void onBind(Item item) {

        if (!(item instanceof InfoDateItem)) {
            return;
        }

        InfoDateItem infoDateItem = (InfoDateItem) item;

        mNameView.setText(infoDateItem.getName());

        if (infoDateItem.getAvatar() != null) {
            mAvatarView.setImage(itemView.getContext(), null,
                    new CircularImageDescriptor(infoDateItem.getAvatar(), 0.5f, 0.5f, 0.5f));
        }

        mDateTextView.setText("-");

        switch (infoDateItem.getInfoDateItemType()) {
            case SENT:
                long createdTimestamp = infoDateItem.getItem().getCreatedTimestamp();
                if (createdTimestamp > 0) {
                    mDateTextView.setText(CommonUtils.formatItemInterval(getBaseItemActivity(), createdTimestamp));
                }
                break;

            case RECEIVED:
                long receivedTimestamp = infoDateItem.getItem().getReceivedTimestamp();
                if (receivedTimestamp > 0) {
                    mDateTextView.setText(CommonUtils.formatItemInterval(getBaseItemActivity(), receivedTimestamp));
                }
                break;

            case SEEN:
                long readTimestamp = infoDateItem.getItem().getReadTimestamp();
                if (readTimestamp > 0) {
                    mDateTextView.setText(CommonUtils.formatItemInterval(getBaseItemActivity(), readTimestamp));
                }
                break;

            case DELETED:
                long peerDeletedTimestamp = infoDateItem.getItem().getPeerDeletedTimestamp();
                if (peerDeletedTimestamp > 0) {
                    mDateTextView.setText(CommonUtils.formatItemInterval(getBaseItemActivity(), peerDeletedTimestamp));
                }
                break;

            case UPDATED:
                long updatedTimestamp = infoDateItem.getItem().getUpdatedTimestamp();
                if (updatedTimestamp > 0) {
                    mDateTextView.setText(CommonUtils.formatItemInterval(getBaseItemActivity(), updatedTimestamp));
                }
                break;

            case EPHEMERAL:
                long timeInterval = infoDateItem.getItem().getReadTimestamp() + infoDateItem.getItem().getExpireTimeout();
                if (timeInterval > 0) {
                    mDateTextView.setText(CommonUtils.formatItemInterval(getBaseItemActivity(), timeInterval));
                }

                break;
        }
    }
}
