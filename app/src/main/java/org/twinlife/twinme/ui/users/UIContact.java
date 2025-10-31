/*
 *  Copyright (c) 2017-2020 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui.users;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.twinlife.twinme.TwinmeApplication;
import org.twinlife.twinme.models.CertificationLevel;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Originator;
import org.twinlife.twinme.ui.mainActivity.UIConversation;

public class UIContact extends UIOriginator {

    private UIConversation mUIConversation;

    private UIContactTag mUIContactTag;

    public UIContact(@NonNull TwinmeApplication twinmeApplication, @NonNull Originator contact, @Nullable Bitmap avatar) {
        super(twinmeApplication, contact, avatar);

        mUIContactTag = null;
    }

    public UIConversation getUIConversation() {

        return mUIConversation;
    }

    public void setUIConversation(UIConversation uiConversation) {

        mUIConversation = uiConversation;
    }

    @Override
    public UIContactTag getUIContactTag() {

        return mUIContactTag;
    }

    @Override
    public boolean isCertified() {

        if (getContact() instanceof Contact) {
            Contact contact = (Contact) getContact();

            return contact.getCertificationLevel() == CertificationLevel.LEVEL_4;
        }

        return false;
    }

    public void updateContactTag(Context context) {

        if (getContact() instanceof Contact) {
            Contact contact = (Contact) getContact();
            if (!contact.hasPeer()) {
                mUIContactTag = new UIContactTag(context, UIContactTag.ContactTag.REVOKED);
            } else if (!contact.hasPrivatePeer()) {
                mUIContactTag = new UIContactTag(context, UIContactTag.ContactTag.PENDING);
            } else {
                mUIContactTag = null;
            }
        } else {
            mUIContactTag = null;
        }
    }
}
