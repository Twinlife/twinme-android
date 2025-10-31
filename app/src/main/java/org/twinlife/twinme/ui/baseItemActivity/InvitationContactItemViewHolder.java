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

import java.util.ArrayList;
import java.util.List;

class InvitationContactItemViewHolder extends ItemViewHolder {

    private final TextView mNameView;
    private final TextView mInvitationView;
    private final GradientDrawable mGradientDrawable;
    private final CircularImageView mAvatarView;
    private final DeleteProgressView mDeleteView;
    private final View mInvitationContainer;

    InvitationContactItemViewHolder(BaseItemActivity baseItemActivity, View view, boolean allowClick, boolean allowLongClick) {

        super(baseItemActivity, view,
                R.id.base_item_activity_invitation_contact_item_layout_container,
                R.id.base_item_activity_invitation_contact_item_state_view,
                R.id.base_item_activity_invitation_contact_item_state_avatar_view,
                R.id.base_item_activity_invitation_contact_item_overlay_view,
                R.id.base_item_activity_invitation_contact_item_selected_view,
                R.id.base_item_activity_invitation_contact_item_selected_image_view);

        mInvitationContainer = view.findViewById(R.id.base_item_activity_invitation_contact_item_view);

        mInvitationContainer.setOnClickListener(v -> {
            if (getBaseItemActivity().isSelectItemMode()) {
                onContainerClick();
            }
        });

        mGradientDrawable = new GradientDrawable();
        mGradientDrawable.mutate();
        mGradientDrawable.setColor(Design.getMainStyle());
        mGradientDrawable.setShape(GradientDrawable.RECTANGLE);
        mInvitationContainer.setBackground(mGradientDrawable);
        mInvitationContainer.setClickable(false);

        mNameView = view.findViewById(R.id.base_item_activity_invitation_contact_item_name);
        Design.updateTextFont(mNameView, Design.FONT_MEDIUM26);

        mAvatarView = view.findViewById(R.id.base_item_activity_invitation_contact_avatar_view);

        mInvitationView = view.findViewById(R.id.base_item_activity_invitation_contact_item_invitation_view);
        Design.updateTextFont(mInvitationView, Design.FONT_REGULAR26);

        mDeleteView = view.findViewById(R.id.base_item_activity_invitation_contact_item_delete_view);

        if (allowLongClick) {
            mInvitationContainer.setOnLongClickListener(v -> {
                baseItemActivity.onItemLongPress(getItem());
                return true;
            });
        }
    }

    @Override
    void onBind(Item item) {

        if (!(item instanceof InvitationContactItem)) {
            return;
        }
        super.onBind(item);

        InvitationContactItem invitation = (InvitationContactItem) item;

        // Get a possible avatar image that depends on the peer twincode.
        Bitmap avatar = invitation.getAvatar();
        if (avatar != null) {
            mAvatarView.setImage(mAvatarView.getContext(), null,
                    new CircularImageDescriptor(avatar, 0.5f, 0.5f, 0.5f));
        }

        mGradientDrawable.setCornerRadii(getCornerRadii());

        mNameView.setText(invitation.getName());
        mInvitationView.setText(getString(R.string.conversation_activity_invitation_pending));
    }

    @Override
    void onViewRecycled() {

        super.onViewRecycled();

        mNameView.setText(null);
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
