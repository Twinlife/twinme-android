/*
 *  Copyright (c) 2020-2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.twinlife.twinlife.BaseService.ErrorCode;
import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinlife.ConversationService.TwincodeDescriptor;
import org.twinlife.twinme.actions.GetTwincodeAction;

public class InvitationContactItem extends Item implements GetTwincodeAction.Consumer {

    private final BaseItemActivity mBaseItemActivity;
    private final BaseItemActivity.InvitationItemObserver mInvitationItemObserver;
    private final ConversationService.TwincodeDescriptor mTwincodeDescriptor;
    private Bitmap mAvatar;
    private String mName;

    public InvitationContactItem(BaseItemActivity baseItemActivity, BaseItemActivity.InvitationItemObserver invitationItemObserver, TwincodeDescriptor twincodeDescriptor) {

        super(ItemType.INVITATION_CONTACT, twincodeDescriptor, null);

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

        if (mInvitationItemObserver != null) {
            mBaseItemActivity.runOnUiThread(() -> mInvitationItemObserver.onUpdateDescriptor(mTwincodeDescriptor, ConversationService.UpdateType.TIMESTAMPS));
        }
    }

    String getName() {

        return mName;
    }

    Bitmap getAvatar() {

        return mAvatar;
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

    }

    //
    // Override Object methods
    //

    @Override
    @NonNull
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("InvitationContactItem\n");
        appendTo(stringBuilder);
        stringBuilder.append("\n");

        return stringBuilder.toString();
    }
}
