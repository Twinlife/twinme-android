/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (fabrice.trescartes@twin.life)
 *   Romain Kolb (romain.kolb@skyrock.com)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinlife.ErrorCode;
import org.twinlife.twinme.actions.GetTwincodeAction;

import java.util.UUID;

public class PeerShareContactItem extends Item implements GetTwincodeAction.Consumer {

    private BaseItemActivity mBaseItemActivity;
    private BaseItemActivity.InvitationItemObserver mInvitationItemObserver;

    private Bitmap mAvatar;
    private String mName;

    @Nullable
    private ConversationService.ContactShareDescriptor mContactShareDescriptor;

    @Nullable
    private ConversationService.TwincodeDescriptor mTwincodeDescriptor;

    private final UUID mPeerTwincodeOutboundId;

    public PeerShareContactItem(@NonNull ConversationService.ContactShareDescriptor descriptor) {
        super(ItemType.PEER_SHARE_CONTACT, descriptor, null);

        mContactShareDescriptor = descriptor;
        mPeerTwincodeOutboundId = descriptor.getTwincodeOutboundId();
        setCopyAllowed(false);
        setCanReply(false);
    }

    public PeerShareContactItem(BaseItemActivity baseItemActivity, BaseItemActivity.InvitationItemObserver invitationItemObserver, @NonNull ConversationService.TwincodeDescriptor descriptor) {
        super(ItemType.PEER_SHARE_CONTACT, descriptor, null);

        mPeerTwincodeOutboundId = descriptor.getTwincodeOutboundId();
        mBaseItemActivity = baseItemActivity;
        mInvitationItemObserver = invitationItemObserver;
        mTwincodeDescriptor = descriptor;
        baseItemActivity.getTwincodeOutbound(descriptor.getTwincodeId(), this);
        setCopyAllowed(false);
        setCanReply(false);
    }

    @Override
    public void onGetTwincodeAction(@NonNull ErrorCode errorCode, @Nullable String name, @Nullable Bitmap avatar) {

        mName = name;
        mAvatar = avatar;

        if (mAvatar == null) {
            mAvatar = mBaseItemActivity.getDefaultAvatar();
        }

        if (mInvitationItemObserver != null && mTwincodeDescriptor != null) {
            if (errorCode == ErrorCode.EXPIRED) {
                mBaseItemActivity.runOnUiThread(() -> mInvitationItemObserver.onDeleteInvitationItem(this));
            } else {
                mBaseItemActivity.runOnUiThread(() -> {
                    mInvitationItemObserver.onUpdateDescriptor(mTwincodeDescriptor, ConversationService.UpdateType.TIMESTAMPS);
                });
            }
        }
    }

    @Nullable
    public ConversationService.ContactShareDescriptor getContactShareDescriptor() {

        return mContactShareDescriptor;
    }

    @Nullable
    public ConversationService.TwincodeDescriptor getTwincodeDescriptor() {

        return mTwincodeDescriptor;
    }

    @Nullable
    public Bitmap getAvatar() {

        return mAvatar;
    }

    @Nullable
    public String getName() {

        return mName;
    }

    @Nullable
    public String getContactName() {

        if (mContactShareDescriptor == null) {
            return null;
        }
        return mContactShareDescriptor.getName();
    }

    //
    // Override Item methods
    //

    public boolean isPeerItem() {

        return true;
    }

    @Override
    public long getTimestamp() {

        return getCreatedTimestamp();
    }

    @Override
    public UUID getPeerTwincodeOutboundId() {

        return mPeerTwincodeOutboundId;
    }

    @Override
    public boolean isSamePeer(Item item) {

        return mPeerTwincodeOutboundId.equals(item.getPeerTwincodeOutboundId());
    }

    //
    // Override Object methods
    //

    @Override
    @NonNull
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("ShareContactItem\n");
        appendTo(stringBuilder);
        stringBuilder.append(" peer: ");
        stringBuilder.append(mPeerTwincodeOutboundId);
        if (mTwincodeDescriptor != null) {
            stringBuilder.append(" twincodeDescriptor: ");
            stringBuilder.append(mTwincodeDescriptor);
        } else {
            stringBuilder.append(" contactShareDescriptor: ");
            stringBuilder.append(mContactShareDescriptor);
        }

        stringBuilder.append("\n");

        return stringBuilder.toString();
    }
}
