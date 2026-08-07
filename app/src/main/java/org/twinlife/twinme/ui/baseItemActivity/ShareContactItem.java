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

public class ShareContactItem extends Item implements GetTwincodeAction.Consumer {

    @Nullable
    private ConversationService.ContactShareDescriptor mContactShareDescriptor;

    @Nullable
    private ConversationService.TwincodeDescriptor mTwincodeDescriptor;

    private BaseItemActivity mBaseItemActivity;
    private BaseItemActivity.InvitationItemObserver mInvitationItemObserver;
    private Bitmap mAvatar;
    private String mName;


    public ShareContactItem(@NonNull ConversationService.ContactShareDescriptor descriptor) {
        super(ItemType.SHARE_CONTACT, descriptor, null);

        mContactShareDescriptor = descriptor;
        setCopyAllowed(false);
        setCanReply(false);
    }

    public ShareContactItem(BaseItemActivity baseItemActivity, BaseItemActivity.InvitationItemObserver invitationItemObserver, @NonNull ConversationService.TwincodeDescriptor twincodeDescriptor) {

        super(ItemType.SHARE_CONTACT, twincodeDescriptor, null);

        mBaseItemActivity = baseItemActivity;
        mInvitationItemObserver = invitationItemObserver;
        mTwincodeDescriptor = twincodeDescriptor;
        if (twincodeDescriptor.getSendTo() != null) {
            baseItemActivity.getTwincodeOutbound(twincodeDescriptor.getSendTo(), this);
        } else {
            baseItemActivity.getTwincodeOutbound(twincodeDescriptor.getTwincodeId(), this);
        }
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
                mBaseItemActivity.runOnUiThread(() -> mInvitationItemObserver.onUpdateDescriptor(mTwincodeDescriptor, ConversationService.UpdateType.TIMESTAMPS));
            }
        }
    }

    String getName() {

        return mName;
    }

    Bitmap getAvatar() {

        return mAvatar;
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
    public String getContactName() {

        if (mContactShareDescriptor == null) {
            return null;
        }
        return mContactShareDescriptor.getName();
    }


    @Override
    public boolean isPeerItem() {
        return false;
    }


    @Override
    public long getTimestamp() {

        return getCreatedTimestamp();
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
        stringBuilder.append(" contactShareDescriptor: ");
        stringBuilder.append(mContactShareDescriptor);
        stringBuilder.append("\n");

        return stringBuilder.toString();
    }
}
