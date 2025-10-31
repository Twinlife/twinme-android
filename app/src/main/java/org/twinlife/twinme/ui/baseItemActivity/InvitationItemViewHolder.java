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
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.CircularImageDescriptor;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.CircularImageView;
import org.twinlife.twinme.utils.RoundedView;

import java.util.ArrayList;
import java.util.List;

class InvitationItemViewHolder extends ItemViewHolder {

    private final TextView mGroupNameView;
    private final TextView mInvitationView;
    private final GradientDrawable mGradientDrawable;
    private final RoundedView mNoAvatarView;
    private final CircularImageView mInvitationAvatarView;
    private final DeleteProgressView mDeleteView;
    private final View mInvitationContainer;

    InvitationItemViewHolder(BaseItemActivity baseItemActivity, View view, boolean allowClick, boolean allowLongClick) {

        super(baseItemActivity, view,
                R.id.base_item_activity_invitation_item_layout_container,
                R.id.base_item_activity_invitation_item_state_view,
                R.id.base_item_activity_invitation_item_state_avatar_view,
                R.id.base_item_activity_invitation_item_overlay_view,
                R.id.base_item_activity_invitation_item_selected_view,
                R.id.base_item_activity_invitation_item_selected_image_view);

        mInvitationContainer = view.findViewById(R.id.base_item_activity_invitation_item_view);

        mGradientDrawable = new GradientDrawable();
        mGradientDrawable.mutate();
        mGradientDrawable.setColor(Design.getMainStyle());
        mGradientDrawable.setShape(GradientDrawable.RECTANGLE);
        mInvitationContainer.setBackground(mGradientDrawable);
        mInvitationContainer.setClickable(false);

        mInvitationView = view.findViewById(R.id.base_item_activity_invitation_item_invitation_view);
        Design.updateTextFont(mInvitationView, Design.FONT_REGULAR26);

        mGroupNameView = view.findViewById(R.id.base_item_activity_invitation_item_group_name);
        Design.updateTextFont(mGroupNameView, Design.FONT_MEDIUM26);

        mInvitationAvatarView = view.findViewById(R.id.base_item_activity_invitation_avatar_view);

        mNoAvatarView = view.findViewById(R.id.base_item_activity_invitation_no_avatar_view);
        mNoAvatarView.setColor(Design.GREY_ITEM_COLOR);

        mDeleteView = view.findViewById(R.id.base_item_activity_invitation_item_delete_view);

        if (allowClick) {


            mInvitationContainer.setOnClickListener(v -> {
                if (getBaseItemActivity().isSelectItemMode()) {
                    onContainerClick();
                    return;
                }

                InvitationItem invitation = (InvitationItem) getItem();

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

        if (!(item instanceof InvitationItem)) {
            return;
        }
        super.onBind(item);

        mNoAvatarView.setVisibility(View.GONE);
        InvitationItem invitation = (InvitationItem) item;

        // Get a possible avatar image that depends on the peer twincode.
        Bitmap avatar = invitation.getGroupAvatar();
        if (avatar != null) {
            if (avatar.equals(getBaseItemActivity().getTwinmeApplication().getDefaultGroupAvatar())) {
                mNoAvatarView.setVisibility(View.VISIBLE);
            }
            mInvitationAvatarView.setImage(mInvitationAvatarView.getContext(), null,
                    new CircularImageDescriptor(avatar, 0.5f, 0.5f, 0.5f));
        }

        mGradientDrawable.setCornerRadii(getCornerRadii());

        mGroupNameView.setText(invitation.getGroupName());

        if (invitation.getState() == Item.ItemState.NOT_SENT) {
            mInvitationView.setText(getString(R.string.conversation_activity_invitation_failed));
        } else if (invitation.getState() != Item.ItemState.PEER_DELETED && invitation.getState() != Item.ItemState.BOTH_DELETED) {
            switch (invitation.getStatus()) {
                case PENDING:
                    mInvitationView.setText(getString(R.string.conversation_activity_invitation_pending));
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
    }

    @Override
    void onViewRecycled() {

        super.onViewRecycled();

        mGroupNameView.setText(null);
        mInvitationView.setText(null);
        mDeleteView.setVisibility(View.GONE);
        mDeleteView.setOnDeleteProgressListener(null);
        setDeleteAnimationStarted(false);
    }

    @Override
    void startDeletedAnimation() {

        if (isDeleteAnimationStarted()) {
            return;
        }

        setDeleteAnimationStarted(true);
        mDeleteView.setVisibility(View.VISIBLE);

        ViewGroup.MarginLayoutParams deleteLayoutParams = (ViewGroup.MarginLayoutParams) mDeleteView.getLayoutParams();
        deleteLayoutParams.width = mInvitationContainer.getWidth();
        deleteLayoutParams.height = mInvitationContainer.getHeight();
        mDeleteView.setLayoutParams(deleteLayoutParams);
        mDeleteView.setCornerRadii(getCornerRadii());
        mDeleteView.setOnDeleteProgressListener(() -> deleteItem(getItem()));

        float progress = 0;
        int animationDuration = DESIGN_DELETE_ANIMATION_DURATION;
        if (getItem().getDeleteProgress() > 0) {
            progress = getItem().getDeleteProgress() / 100.0f;
            animationDuration = (int) (BaseItemViewHolder.DESIGN_DELETE_ANIMATION_DURATION - ((getItem().getDeleteProgress() * BaseItemViewHolder.DESIGN_DELETE_ANIMATION_DURATION) / 100.0));
        }

        mDeleteView.startAnimation(animationDuration, progress);
    }

    @Override
    List<View> clickableViews() {

        return new ArrayList<View>() {
            {
                add(mInvitationContainer);
                add(getContainer());
            }
        };
    }
}
