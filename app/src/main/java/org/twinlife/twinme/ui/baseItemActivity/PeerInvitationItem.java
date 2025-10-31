/*
 *  Copyright (c) 2018-2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import android.content.Intent;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.twinlife.twinlife.BaseService.ErrorCode;
import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinlife.ConversationService.GroupConversation;
import org.twinlife.twinlife.ConversationService.InvitationDescriptor;
import org.twinlife.twinme.actions.GetTwincodeAction;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.conversationActivity.ConversationActivity;
import org.twinlife.twinme.ui.groups.AcceptGroupInvitationActivity;

public class PeerInvitationItem extends Item implements GetTwincodeAction.Consumer {

    private final BaseItemActivity mBaseItemActivity;
    private final BaseItemActivity.InvitationItemObserver mInvitationItemObserver;
    private final String mGroupName;
    private InvitationDescriptor.Status mInvitationStatus;
    private final InvitationDescriptor mInvitation;
    private Bitmap mAvatar;

    public PeerInvitationItem(BaseItemActivity baseItemActivity, BaseItemActivity.InvitationItemObserver invitationItemObserver, InvitationDescriptor invitationDescriptor) {

        super(ItemType.PEER_INVITATION, invitationDescriptor, null);

        mBaseItemActivity = baseItemActivity;
        mInvitationItemObserver = invitationItemObserver;
        mInvitation = invitationDescriptor;
        mGroupName = invitationDescriptor.getName();
        mInvitationStatus = invitationDescriptor.getStatus();
        baseItemActivity.getTwincodeOutbound(invitationDescriptor.getGroupTwincodeId(), this);
        mAvatar = baseItemActivity.getTwinmeContext().getDefaultGroupAvatar();
    }

    String getGroupName() {

        return mGroupName;
    }

    InvitationDescriptor.Status getStatus() {

        return mInvitationStatus;
    }

    Bitmap getGroupAvatar() {

        return mAvatar;
    }

    void onClickInvitation() {

        final ConversationService service = mBaseItemActivity.getTwinmeContext().getConversationService();

        GroupConversation group = service.getGroupConversationWithGroupTwincodeId(mInvitation.getGroupTwincodeId());
        if (group != null && group.getState() == GroupConversation.State.JOINED) {
            mBaseItemActivity.startActivity(ConversationActivity.class, Intents.INTENT_GROUP_ID, group.getContactId());
        } else {

            if (mBaseItemActivity.getContact() != null) {
                Intent intent = new Intent();

                intent.setClass(mBaseItemActivity, AcceptGroupInvitationActivity.class);
                intent.putExtra(Intents.INTENT_CONTACT_ID, mBaseItemActivity.getContact().getId().toString());
                intent.putExtra(Intents.INTENT_INVITATION_ID, mInvitation.getDescriptorId().toString());
                mBaseItemActivity.startActivity(intent);
                mBaseItemActivity.overridePendingTransition(0, 0);
            }
        }
    }

    //
    // Override Item methods
    //

    @Override
    public boolean isPeerItem() {

        return true;
    }

    @Override
    public long getTimestamp() {

        return getCreatedTimestamp();
    }

    @Override
    public void updateTimestamps(ConversationService.Descriptor descriptor) {
        super.updateTimestamps(descriptor);

        // Update the invitation status because it could have changed.
        mInvitationStatus = ((InvitationDescriptor) descriptor).getStatus();
    }

    @Override
    public void onGetTwincodeAction(@NonNull ErrorCode errorCode, @Nullable String name, @Nullable Bitmap avatar) {

        mAvatar = avatar;

        if (mAvatar == null) {
            mAvatar = mBaseItemActivity.getTwinmeApplication().getDefaultGroupAvatar();
        }

        if (mInvitationItemObserver != null) {
            mBaseItemActivity.runOnUiThread(() -> {
                mInvitationItemObserver.onUpdateDescriptor(mInvitation, ConversationService.UpdateType.TIMESTAMPS);
            });
        }
    }

    //
    // Override Object methods
    //

    @Override
    @NonNull
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("PeerInvitationItem\n");
        appendTo(stringBuilder);
        stringBuilder.append(" groupName: ");
        stringBuilder.append(mGroupName);
        stringBuilder.append(" invitationStatus: ");
        stringBuilder.append(mInvitationStatus);
        stringBuilder.append("\n");

        return stringBuilder.toString();
    }
}
