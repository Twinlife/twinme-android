/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.contacts;

import androidx.annotation.Nullable;

import org.twinlife.twinme.ui.users.UIContact;

public class ShareContactItem {

    public enum ShareContactType {
        SHARE_CONTACT,
        SECTION,
        CONTACT
    }

    private final ShareContactType mType;

    @Nullable
    private final UIContact mUIContact;

    public ShareContactItem(ShareContactType type, @Nullable UIContact uiContact) {

        mType = type;
        mUIContact = uiContact;
    }

    @Nullable
    public UIContact getContact() {

        return mUIContact;
    }

    public ShareContactType getType() {

        return mType;
    }

}
