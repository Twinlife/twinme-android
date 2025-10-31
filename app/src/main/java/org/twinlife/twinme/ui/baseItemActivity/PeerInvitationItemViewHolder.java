/*
 *  Copyright (c) 2018-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
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
import org.twinlife.twinme.utils.RoundedView;

import java.util.ArrayList;
import java.util.List;

class PeerInvitationItemViewHolder extends PeerItemViewHolder {

    private final TextView mGroupNameView;
    private final TextView mInvitationView;
    private final GradientDrawable mGradientDrawable;
    private final CircularImageView mInvitationAvatarView;
    private final RoundedView mNoAvatarView;

    PeerInvitationItemViewHolder(BaseItemActivity baseItemActivity, View view, boolean allowClick, boolean allowLongClick) {

        super(baseItemActivity, view,
                R.id.base_item_activity_peer_invitation_item_layout_container,
                R.id.base_item_activity_peer_invitation_item_avatar,
                R.id.base_item_activity_peer_invitation_item_overlay_view,
                R.id.base_item_activity_peer_invitation_item_selected_view,
                R.id.base_item_activity_peer_invitation_item_selected_image_view);

        View invitationContainer = view.findViewById(R.id.base_item_activity_peer_invitation_item_view);

        mGradientDrawable = new GradientDrawable();
        mGradientDrawable.mutate();
        mGradientDrawable.setColor(Design.GREY_ITEM_COLOR);
        mGradientDrawable.setShape(GradientDrawable.RECTANGLE);
        invitationContainer.setBackground(mGradientDrawable);
        invitationContainer.setClickable(false);

        mGroupNameView = view.findViewById(R.id.base_item_activity_peer_invitation_item_group_name);
        mGroupNameView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mGroupNameView.setTextColor(getBaseItemActivity().getCustomAppearance().getPeerMessageTextColor());

        mInvitationAvatarView = view.findViewById(R.id.base_item_activity_peer_invitation_avatar_view);

        mNoAvatarView = view.findViewById(R.id.base_item_activity_peer_invitation_no_avatar_view);
        mNoAvatarView.setColor(Color.parseColor(Design.DEFAULT_COLOR));

        mInvitationView = view.findViewById(R.id.base_item_activity_peer_invitation_item_invitation_view);
        Design.updateTextFont(mInvitationView, Design.FONT_REGULAR26);
        mInvitationView.setTextColor(getBaseItemActivity().getCustomAppearance().getPeerMessageTextColor());

        if (allowClick) {
            invitationContainer.setOnClickListener(v -> {

                if (getBaseItemActivity().isSelectItemMode()) {
                    onContainerClick();
                    return;
                }

                PeerInvitationItem invitation = (PeerInvitationItem) getItem();
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

        if (!(item instanceof PeerInvitationItem)) {
            return;
        }
        super.onBind(item);

        PeerInvitationItem invitation = (PeerInvitationItem) item;

        // Get a possible avatar image that depends on the peer twincode.
        Bitmap avatar = invitation.getGroupAvatar();
        if (avatar != null) {
            if (avatar.equals(getBaseItemActivity().getTwinmeApplication().getDefaultGroupAvatar())) {
                mNoAvatarView.setVisibility(View.VISIBLE);
                mInvitationAvatarView.setColorFilter(Color.WHITE);
            } else {
                mNoAvatarView.setVisibility(View.GONE);
                mInvitationAvatarView.setColorFilter(Color.TRANSPARENT);
            }
            mInvitationAvatarView.setImage(mInvitationAvatarView.getContext(), null,
                    new CircularImageDescriptor(avatar, 0.5f, 0.5f, 0.5f));
        }

        mGradientDrawable.setCornerRadii(getCornerRadii());
        mGradientDrawable.setColor(getBaseItemActivity().getCustomAppearance().getPeerMessageBackgroundColor());
        if (getBaseItemActivity().getCustomAppearance().getPeerMessageBorderColor() != Color.TRANSPARENT) {
            mGradientDrawable.setStroke(Design.BORDER_WIDTH, getBaseItemActivity().getCustomAppearance().getPeerMessageBorderColor());
        }

        mGroupNameView.setText(invitation.getGroupName());
        switch (invitation.getStatus()) {
            case PENDING:
                mInvitationView.setText(getString(R.string.conversation_activity_invitation_title));
                break;

            case ACCEPTED:
                mInvitationView.setText(getString(R.string.conversation_activity_invitation_accepted));
                break;

            case JOINED:
                mInvitationView.setText(getString(R.string.conversation_activity_invitation_joined));
                break;

            case REFUSED:
            case WITHDRAWN:
                mInvitationView.setText(getString(R.string.conversation_activity_invitation_refused));
                break;
        }
    }

    @Override
    List<View> clickableViews() {

        return new ArrayList<View>() {
            {
                add(mInvitationView);
                add(getContainer());
            }
        };
    }

    @Override
    void onViewRecycled() {

        super.onViewRecycled();

        mInvitationView.setText(null);
        mGroupNameView.setText(null);
    }
}
