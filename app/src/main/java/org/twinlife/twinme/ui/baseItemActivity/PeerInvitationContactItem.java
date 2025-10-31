/*
 *  Copyright (c) 2020-2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import android.content.Intent;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.twinlife.twinlife.BaseService.ErrorCode;
import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinlife.ConversationService.TwincodeDescriptor;
import org.twinlife.twinme.actions.GetTwincodeAction;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.ui.AcceptInvitationActivity;
import org.twinlife.twinme.ui.Intents;

public class PeerInvitationContactItem extends Item implements GetTwincodeAction.Consumer {

    private final BaseItemActivity mBaseItemActivity;
    private final BaseItemActivity.InvitationItemObserver mInvitationItemObserver;
    private final ConversationService.TwincodeDescriptor mTwincodeDescriptor;
    private Bitmap mAvatar;
    private String mName;

    public PeerInvitationContactItem(BaseItemActivity baseItemActivity, BaseItemActivity.InvitationItemObserver invitationItemObserver, TwincodeDescriptor twincodeDescriptor) {

        super(ItemType.PEER_INVITATION_CONTACT, twincodeDescriptor, null);

        mBaseItemActivity = baseItemActivity;
        mInvitationItemObserver = invitationItemObserver;
        mTwincodeDescriptor = twincodeDescriptor;
        baseItemActivity.getTwincodeOutbound(twincodeDescriptor.getTwincodeId(), this);
    }

    @Override
    public void onGetTwincodeAction(@NonNull ErrorCode errorCode, @Nullable String name, @Nullable Bitmap avatar) {

        mName = name;
        mAvatar = avatar;

        if (mAvatar == null) {
            mAvatar = mBaseItemActivity.getDefaultAvatar();
        }

        if (mInvitationItemObserver != null) {
            mBaseItemActivity.runOnUiThread(() -> {
                mInvitationItemObserver.onUpdateDescriptor(mTwincodeDescriptor, ConversationService.UpdateType.TIMESTAMPS);
            });
        }
    }

    String getName() {

        return mName;
    }

    Bitmap getAvatar() {

        return mAvatar;
    }

    void onClickInvitation() {

        Intent intent = new Intent();
        intent.setClass(mBaseItemActivity, AcceptInvitationActivity.class);
        intent.putExtra(Intents.INTENT_DESCRIPTOR_ID, mTwincodeDescriptor.getDescriptorId().toString());
        Contact contact = mBaseItemActivity.getContact();
        if (contact != null) {
            intent.putExtra(Intents.INTENT_CONTACT_ID, contact.getId().toString());
        } else if (mBaseItemActivity.getGroup() != null){
            intent.putExtra(Intents.INTENT_GROUP_ID, mBaseItemActivity.getGroup().getId().toString());
        }
        mBaseItemActivity.startActivity(intent);
        mBaseItemActivity.overridePendingTransition(0, 0);
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

    }

    //
    // Override Object methods
    //

    @Override
    @NonNull
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("PeerInvitationContactItem\n");
        appendTo(stringBuilder);

        return stringBuilder.toString();
    }
}
