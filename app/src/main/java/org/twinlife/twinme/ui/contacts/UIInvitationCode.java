/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.contacts;

import org.twinlife.twinme.models.Invitation;

import java.util.Calendar;
import java.util.UUID;

public class UIInvitationCode {

    private final Invitation mInvitation;
    private final String mCode;
    private final long mExpirationDate;

    public UIInvitationCode(Invitation invitation, String code, long expirationDate) {

        mInvitation = invitation;
        mCode = code;
        mExpirationDate = expirationDate;
    }

    public UUID getInvitationId() {

        return mInvitation.getId();
    }

    public Invitation getInvitation() {

        return mInvitation;
    }

    public String getCode() {

        return mCode;
    }

    public long getExpirationDate() {

        return mExpirationDate;
    }

    public boolean hasExpired() {

        Calendar calendarExpiration = Calendar.getInstance();
        calendarExpiration.setTimeInMillis(mExpirationDate * 1000L);

        return calendarExpiration.before(Calendar.getInstance());
    }
}

