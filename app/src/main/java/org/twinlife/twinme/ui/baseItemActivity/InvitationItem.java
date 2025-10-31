/*
 *  Copyright (c) 2018-2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.baseItemActivity;

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

import java.util.UUID;

public class InvitationItem extends Item implements GetTwincodeAction.Consumer {

    private final BaseItemActivity mBaseItemActivity;
    private final BaseItemActivity.InvitationItemObserver mInvitationItemObserver;
    private final String mGroupName;
    private InvitationDescriptor.Status mInvitationStatus;
    private final UUID mGroupTwincodeId;
    private final InvitationDescriptor mInvitation;
    private Bitmap mAvatar;

    public InvitationItem(BaseItemActivity baseItemActivity, BaseItemActivity.InvitationItemObserver invitationItemObserver, InvitationDescriptor invitationDescriptor) {

        super(ItemType.INVITATION, invitationDescriptor, null);

        mBaseItemActivity = baseItemActivity;
        mInvitationItemObserver = invitationItemObserver;
        mGroupName = invitationDescriptor.getName();
        mInvitation = invitationDescriptor;
        mInvitationStatus = invitationDescriptor.getStatus();
        mGroupTwincodeId = invitationDescriptor.getGroupTwincodeId();
        baseItemActivity.getTwincodeOutbound(invitationDescriptor.getGroupTwincodeId(), this);
        mAvatar = baseItemActivity.getTwinmeContext().getDefaultGroupAvatar();
    }

    String getGroupName() {

        return mGroupName;
    }

    Bitmap getGroupAvatar() {

        return mAvatar;
    }

    InvitationDescriptor.Status getStatus() {

        return mInvitationStatus;
    }

    //
    // Override Item methods
    //

    @Override
    public boolean isPeerItem() {

        return false;
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
            mBaseItemActivity.runOnUiThread(() -> mInvitationItemObserver.onUpdateDescriptor(mInvitation, ConversationService.UpdateType.TIMESTAMPS));
        }
    }

    void onClickInvitation() {

        // If the peer joined the group and this group is still available, redirect to the group conversation.
        final ConversationService service = mBaseItemActivity.getTwinmeContext().getConversationService();

        GroupConversation group = service.getGroupConversationWithGroupTwincodeId(mGroupTwincodeId);
        if (group != null && group.getState() == GroupConversation.State.JOINED) {
            mBaseItemActivity.startActivity(ConversationActivity.class, Intents.INTENT_GROUP_ID, group.getContactId());
        }
    }

    //
    // Override Object methods
    //

    @Override
    @NonNull
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("InvitationItem\n");
        appendTo(stringBuilder);
        stringBuilder.append(" groupName: ");
        stringBuilder.append(mGroupName);
        stringBuilder.append(" invitationStatus: ");
        stringBuilder.append(mInvitationStatus);
        stringBuilder.append("\n");

        return stringBuilder.toString();
    }
}
