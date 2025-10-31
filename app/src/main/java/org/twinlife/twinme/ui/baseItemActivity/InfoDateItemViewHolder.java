/*
 *  Copyright (c) 2019-2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.AvatarView;
import org.twinlife.twinme.utils.CommonUtils;
import org.twinlife.twinme.utils.RoundedView;

class InfoDateItemViewHolder extends BaseItemViewHolder {

    private final TextView mTitleTextView;
    private final TextView mDateTextView;
    private final ImageView mStateImageView;
    private final AvatarView mStateAvatarView;
    private final RoundedView mStateBubbleView;

    private static final float DESIGN_ITEM_VIEW_HEIGHT = 120f;
    private static final int ITEM_VIEW_HEIGHT;

    static {
        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
    }

    InfoDateItemViewHolder(BaseItemActivity baseItemActivity, View view) {
        super(baseItemActivity, view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = ITEM_VIEW_HEIGHT;
        view.setLayoutParams(layoutParams);

        mStateBubbleView = view.findViewById(R.id.base_item_activity_info_date_item_state_bubble_view);
        mStateBubbleView.setColor(Design.BLUE_NORMAL);
        mStateBubbleView.setAlpha((float) 0.32);

        mStateImageView = view.findViewById(R.id.base_item_activity_info_date_item_state_image_view);

        mTitleTextView = view.findViewById(R.id.base_item_activity_info_date_item_title_text_view);
        Design.updateTextFont(mTitleTextView, Design.FONT_REGULAR32);

        mDateTextView = view.findViewById(R.id.base_item_activity_info_date_item_date_text_view);
        Design.updateTextFont(mDateTextView, Design.FONT_REGULAR32);

        mStateAvatarView = view.findViewById(R.id.base_item_activity_info_date_item_state_avatar_view);
    }

    @Override
    void onBind(Item item) {

        if (!(item instanceof InfoDateItem)) {
            return;
        }

        mStateBubbleView.setVisibility(View.VISIBLE);
        mStateImageView.setVisibility(View.INVISIBLE);
        mStateAvatarView.setVisibility(View.INVISIBLE);

        mDateTextView.setText("");

        InfoDateItem infoDateItem = (InfoDateItem) item;
        switch (infoDateItem.getInfoDateItemType()) {
            case SENT:
                mTitleTextView.setText(getString(R.string.info_item_activity_sent));
                long createdTimestamp = infoDateItem.getItem().getCreatedTimestamp();
                if (createdTimestamp > 0) {
                    mDateTextView.setText(CommonUtils.formatItemInterval(getBaseItemActivity(), createdTimestamp));
                    mStateBubbleView.setVisibility(View.INVISIBLE);
                    mStateImageView.setVisibility(View.VISIBLE);
                    mStateImageView.setImageDrawable(ResourcesCompat.getDrawable(itemView.getResources(), R.drawable.sending_state, null));

                }
                break;

            case RECEIVED:
                mTitleTextView.setText(getString(R.string.info_item_activity_received));
                long receivedTimestamp = infoDateItem.getItem().getReceivedTimestamp();
                if (receivedTimestamp > 0) {
                    mDateTextView.setText(CommonUtils.formatItemInterval(getBaseItemActivity(), receivedTimestamp));
                    mStateBubbleView.setVisibility(View.INVISIBLE);
                    mStateImageView.setVisibility(View.VISIBLE);
                    mStateImageView.setImageDrawable(ResourcesCompat.getDrawable(itemView.getResources(), R.drawable.received_state, null));
                }
                break;

            case SEEN:
                mTitleTextView.setText(getString(R.string.info_item_activity_seen));
                long readTimestamp = infoDateItem.getItem().getReadTimestamp();
                if (readTimestamp > 0) {
                    mDateTextView.setText(CommonUtils.formatItemInterval(getBaseItemActivity(), readTimestamp));
                    mStateBubbleView.setVisibility(View.INVISIBLE);
                    Bitmap avatar = infoDateItem.getAvatar();
                    if (avatar != null) {
                        mStateAvatarView.setImageBitmap(avatar);
                        mStateAvatarView.setVisibility(View.VISIBLE);
                    }
                }
                break;

            case DELETED:
                mTitleTextView.setText(getString(R.string.info_item_activity_deleted));
                long peerDeletedTimestamp = infoDateItem.getItem().getPeerDeletedTimestamp();
                if (peerDeletedTimestamp > 0) {
                    mDateTextView.setText(CommonUtils.formatItemInterval(getBaseItemActivity(), peerDeletedTimestamp));
                    mStateBubbleView.setVisibility(View.INVISIBLE);
                    mStateImageView.setVisibility(View.VISIBLE);
                    mStateImageView.setImageDrawable(ResourcesCompat.getDrawable(itemView.getResources(), R.drawable.deleted_state, null));
                }
                break;

            case UPDATED:
                mTitleTextView.setText(String.format("%s : ", getString(R.string.info_item_activity_updated)));
                long updatedTimestamp = infoDateItem.getItem().getUpdatedTimestamp();
                if (updatedTimestamp > 0) {
                    mDateTextView.setText(CommonUtils.formatItemInterval(getBaseItemActivity(), updatedTimestamp));
                    mStateBubbleView.setVisibility(View.INVISIBLE);
                    mStateImageView.setVisibility(View.VISIBLE);
                    mStateImageView.setImageDrawable(ResourcesCompat.getDrawable(itemView.getResources(), R.drawable.edit_style, null));
                }
                break;

            case EPHEMERAL:
                mTitleTextView.setText(getString(R.string.application_timeout));
                long timeInterval = infoDateItem.getItem().getReadTimestamp() + infoDateItem.getItem().getExpireTimeout();
                if (timeInterval > 0) {
                    mDateTextView.setText(CommonUtils.formatItemInterval(getBaseItemActivity(), timeInterval));
                    mStateBubbleView.setVisibility(View.INVISIBLE);
                    mStateImageView.setVisibility(View.VISIBLE);
                    mStateImageView.setImageDrawable(ResourcesCompat.getDrawable(itemView.getResources(), R.drawable.ephemeral_icon, null));
                    mStateImageView.setColorFilter(Design.BLACK_COLOR);
                }

                break;
        }
    }
}
