/*
 *  Copyright (c) 2022-2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.AvatarView;
import org.twinlife.twinme.utils.CommonUtils;

import java.util.Collections;
import java.util.List;

public class PeerClearItemViewHolder extends PeerItemViewHolder {

    private static final float DESIGN_AVATAR_VIEW_HEIGHT = 60f;
    private static final float DESIGN_AVATAR_VIEW_RIGHT_MARGIN = 20f;
    private static final float DESIGN_AVATAR_VIEW_LEFT_MARGIN = 36f;
    private static final int AVATAR_VIEW_HEIGHT;
    private static final int AVATAR_VIEW_RIGHT_MARGIN;
    private static final int AVATAR_VIEW_LEFT_MARGIN;

    static {
        AVATAR_VIEW_HEIGHT = (int) (DESIGN_AVATAR_VIEW_HEIGHT * Design.HEIGHT_RATIO);
        AVATAR_VIEW_RIGHT_MARGIN = (int) (DESIGN_AVATAR_VIEW_RIGHT_MARGIN * Design.WIDTH_RATIO);
        AVATAR_VIEW_LEFT_MARGIN = (int) (DESIGN_AVATAR_VIEW_LEFT_MARGIN * Design.WIDTH_RATIO);
    }

    private final View mInfoView;
    private final TextView mResetView;
    private final AvatarView mAvatarImageView;

    PeerClearItemViewHolder(BaseItemActivity baseItemActivity, View view, boolean allowLongClick) {

        super(baseItemActivity, view,
                R.id.base_item_activity_peer_clear_item_container,
                R.id.base_item_activity_peer_clear_item_avatar,
                R.id.base_item_activity_peer_clear_item_overlay_view,
                R.id.base_item_activity_peer_clear_item_selected_view,
                R.id.base_item_activity_peer_clear_item_selected_image_view);

        mAvatarImageView = view.findViewById(R.id.base_item_activity_peer_clear_item_avatar);
        ViewGroup.LayoutParams layoutParams = mAvatarImageView.getLayoutParams();
        layoutParams.height = AVATAR_VIEW_HEIGHT;

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mAvatarImageView.getLayoutParams();
        if (CommonUtils.isLayoutDirectionRTL()) {
            marginLayoutParams.leftMargin = AVATAR_VIEW_RIGHT_MARGIN;
            marginLayoutParams.setMarginStart(AVATAR_VIEW_RIGHT_MARGIN);
        } else {
            marginLayoutParams.rightMargin = AVATAR_VIEW_RIGHT_MARGIN;
            marginLayoutParams.setMarginEnd(AVATAR_VIEW_RIGHT_MARGIN);
        }

        mInfoView = view.findViewById(R.id.base_item_activity_peer_clear_info);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mInfoView.getLayoutParams();
        marginLayoutParams.leftMargin = AVATAR_VIEW_LEFT_MARGIN;
        marginLayoutParams.setMarginStart(AVATAR_VIEW_LEFT_MARGIN);

        mResetView = view.findViewById(R.id.base_item_activity_peer_clear_item_reset_view);
        Design.updateTextFont(mResetView, Design.FONT_ITALIC_28);
        mResetView.setTextColor(Design.DELETE_COLOR_RED);

        View clearContainer = view.findViewById(R.id.base_item_activity_peer_clear_item_container);

        clearContainer.setOnClickListener(v -> {
            if (getBaseItemActivity().isSelectItemMode()) {
                onContainerClick();
            }
        });

        if (allowLongClick) {
            clearContainer.setOnLongClickListener(v -> {
                baseItemActivity.onItemLongPress(getItem());
                return true;
            });
        }
    }

    @Override
    void onBind(Item item) {

        if (!(item instanceof PeerClearItem)) {
            return;
        }
        super.onBind(item);

        PeerClearItem peerClearItem = (PeerClearItem) item;
        mResetView.setText(String.format(getString(R.string.conversation_activity_cleanup_conversation_by_peer), peerClearItem.getName()));

        getBaseItemActivity().getContactAvatar(null, (Bitmap avatar) -> {
            if (avatar != null) {
                mAvatarImageView.setImageBitmap(avatar);
                mAvatarImageView.setVisibility(View.VISIBLE);
            }
        });

        int avatarMargin = AVATAR_VIEW_LEFT_MARGIN;
        if (getBaseItemActivity().isSelectItemMode()) {
           avatarMargin = CHECKBOX_HEIGHT + CHECKBOX_MARGIN * 2;
        }

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mInfoView.getLayoutParams();
        if (CommonUtils.isLayoutDirectionRTL()) {
            marginLayoutParams.rightMargin = avatarMargin;
            marginLayoutParams.setMarginEnd(avatarMargin);
        } else {
            marginLayoutParams.leftMargin = avatarMargin;
            marginLayoutParams.setMarginStart(avatarMargin);
        }

    }

    @Override
    List<View> clickableViews() {
        return Collections.singletonList(getContainer());
    }
}
