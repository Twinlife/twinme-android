/*
 *  Copyright (c) 2018-2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui.users;

import android.graphics.Bitmap;

import androidx.annotation.Nullable;

import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinlife.ConversationService.InvitationDescriptor;
import org.twinlife.twinme.TwinmeApplication;
import org.twinlife.twinme.models.Originator;

public class UIInvitation extends UIContact {

    @Nullable
    private InvitationDescriptor mInvitation;

    public UIInvitation(TwinmeApplication twinmeApplication, Originator contact, Bitmap avatar) {
        super(twinmeApplication, contact, avatar);
    }

    public void setInvitation(InvitationDescriptor invitation) {

        mInvitation = invitation;
    }

    public InvitationDescriptor.Status getStatus() {

        return mInvitation.getStatus();
    }

    public ConversationService.InvitationDescriptor getInvitationDescriptor() {

        return mInvitation;
    }

    public boolean peerFailure() {

        return mInvitation.getReceivedTimestamp() < 0;
    }
}
