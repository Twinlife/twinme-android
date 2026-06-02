/*
 *  Copyright (c) 2020-2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.CircularImageDescriptor;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.CircularImageView;

import java.util.ArrayList;
import java.util.List;

class PeerInvitationContactItemViewHolder extends PeerItemViewHolder {

    private final View mInvitationContainer;
    private final TextView mNameView;
    private final TextView mInvitationView;
    private final GradientDrawable mGradientDrawable;
    private final CircularImageView mAvatarView;

    PeerInvitationContactItemViewHolder(BaseItemActivity baseItemActivity, View view, boolean allowClick, boolean allowLongClick) {

        super(baseItemActivity, view,
                R.id.base_item_activity_peer_invitation_contact_item_layout_container,
                R.id.base_item_activity_peer_invitation_contact_item_avatar,
                R.id.base_item_activity_peer_invitation_contact_item_overlay_view,
                R.id.base_item_activity_peer_invitation_contact_item_selected_view,
                R.id.base_item_activity_peer_invitation_contact_item_selected_image_view);

        mInvitationContainer = view.findViewById(R.id.base_item_activity_peer_invitation_contact_item_view);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mInvitationContainer.getLayoutParams();
        if (baseItemActivity.displayPeerItemAvatar()) {
            marginLayoutParams.setMarginStart(Design.PEER_CONTENT_CONVERSATION_MARGIN + Design.PEER_AVATAR_CONVERSATION_MARGIN + BaseItemActivity.AVATAR_HEIGHT);
        } else {
            marginLayoutParams.setMarginStart(Design.PEER_AVATAR_CONVERSATION_MARGIN);
        }
        mInvitationContainer.setLayoutParams(marginLayoutParams);

        mGradientDrawable = new GradientDrawable();
        mGradientDrawable.mutate();
        mGradientDrawable.setColor(Design.GREY_ITEM_COLOR);
        mGradientDrawable.setShape(GradientDrawable.RECTANGLE);
        mInvitationContainer.setBackground(mGradientDrawable);
        mGradientDrawable.setStroke(Design.BORDER_WIDTH, Color.TRANSPARENT);
        mInvitationContainer.setClickable(false);

        mNameView = view.findViewById(R.id.base_item_activity_peer_invitation_contact_item_name);
        Design.updateTextFont(mNameView, Design.FONT_MEDIUM26);
        mNameView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mAvatarView = view.findViewById(R.id.base_item_activity_peer_invitation_contact_item_avatar_view);

        mInvitationView = view.findViewById(R.id.base_item_activity_peer_invitation_contact_item_invitation_view);
        Design.updateTextFont(mInvitationView, Design.FONT_REGULAR26);
        mInvitationView.setTextColor(Design.FONT_COLOR_DEFAULT);

        if (allowClick) {
            mInvitationContainer.setOnClickListener(v -> {

                if (getBaseItemActivity().isSelectItemMode()) {
                    onContainerClick();
                    return;
                }

                PeerInvitationContactItem invitation = (PeerInvitationContactItem) getItem();
                invitation.onClickInvitation();
            });
        }
        if (allowLongClick) {
            mInvitationContainer.setOnLongClickListener(v -> {
                baseItemActivity.onItemLongPress(getItem());
                return true;
            });
        }
    }

    @Override
    void onBind(Item item) {

        if (!(item instanceof PeerInvitationContactItem)) {
            return;
        }
        super.onBind(item);

        PeerInvitationContactItem invitation = (PeerInvitationContactItem) item;

        // Get a possible avatar image that depends on the peer twincode.
        Bitmap avatar = invitation.getAvatar();
        if (avatar != null) {
            mAvatarView.setImage(mAvatarView.getContext(), null,
                    new CircularImageDescriptor(avatar, 0.5f, 0.5f, 0.5f));
        }

        mGradientDrawable.setCornerRadii(getCornerRadii());

        mNameView.setText(invitation.getName());
        mInvitationView.setText(String.format(getString(R.string.accept_invitation_view_message), invitation.getName()));

        if (!getBaseItemActivity().displayPeerItemAvatar()) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mInvitationContainer.getLayoutParams();
            int leftMargin = Design.PEER_AVATAR_CONVERSATION_MARGIN;
            if (getBaseItemActivity().isSelectItemMode()) {
                marginLayoutParams.setMarginStart(leftMargin + BaseItemViewHolder.CHECKBOX_MARGIN + BaseItemViewHolder.CHECKBOX_HEIGHT);
            } else {
                marginLayoutParams.setMarginStart(leftMargin);
            }
            mInvitationContainer.setLayoutParams(marginLayoutParams);
        }
    }

    @Override
    List<View> clickableViews() {

        return new ArrayList<View>() {
            {
                add(getContainer());
            }
        };
    }

    @Override
    void onViewRecycled() {

        mNameView.setText(null);
    }
}
