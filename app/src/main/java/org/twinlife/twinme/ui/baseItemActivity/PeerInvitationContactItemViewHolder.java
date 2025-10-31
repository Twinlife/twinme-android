/*
 *  Copyright (c) 2020-2025 twinlife SA.
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
import android.widget.TextView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.CircularImageDescriptor;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.CircularImageView;

import java.util.ArrayList;
import java.util.List;

class PeerInvitationContactItemViewHolder extends PeerItemViewHolder {

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

        View invitationContainer = view.findViewById(R.id.base_item_activity_peer_invitation_contact_item_view);

        mGradientDrawable = new GradientDrawable();
        mGradientDrawable.mutate();
        mGradientDrawable.setColor(Design.GREY_ITEM_COLOR);
        mGradientDrawable.setShape(GradientDrawable.RECTANGLE);
        invitationContainer.setBackground(mGradientDrawable);
        mGradientDrawable.setStroke(Design.BORDER_WIDTH, Color.TRANSPARENT);
        invitationContainer.setClickable(false);

        mNameView = view.findViewById(R.id.base_item_activity_peer_invitation_contact_item_name);
        Design.updateTextFont(mNameView, Design.FONT_MEDIUM26);
        mNameView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mAvatarView = view.findViewById(R.id.base_item_activity_peer_invitation_contact_item_avatar_view);

        mInvitationView = view.findViewById(R.id.base_item_activity_peer_invitation_contact_item_invitation_view);
        Design.updateTextFont(mInvitationView, Design.FONT_REGULAR26);
        mInvitationView.setTextColor(Design.FONT_COLOR_DEFAULT);

        if (allowClick) {
            invitationContainer.setOnClickListener(v -> {

                if (getBaseItemActivity().isSelectItemMode()) {
                    onContainerClick();
                    return;
                }

                PeerInvitationContactItem invitation = (PeerInvitationContactItem) getItem();
                invitation.onClickInvitation();
            });
        }
        if (allowLongClick) {
            invitationContainer.setOnLongClickListener(v -> {
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
        mInvitationView.setText(String.format(getString(R.string.accept_invitation_activity_message), invitation.getName()));
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
