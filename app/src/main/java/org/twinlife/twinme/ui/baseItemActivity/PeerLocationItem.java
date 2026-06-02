/*
 *  Copyright (c) 2019-2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Romain Kolb (romain.kolb@skyrock.com)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.twinlife.twinlife.ConversationService.Descriptor;
import org.twinlife.twinlife.ConversationService.GeolocationDescriptor;

import java.util.UUID;

public class PeerLocationItem extends Item {

    @NonNull
    private final GeolocationDescriptor mGeolocationDescriptor;
    private final UUID mPeerTwincodeOutboundId;

    public PeerLocationItem(@NonNull GeolocationDescriptor geolocationDescriptor, @Nullable Descriptor replyToDescriptor) {

        super(ItemType.PEER_LOCATION, geolocationDescriptor, replyToDescriptor);

        mGeolocationDescriptor = geolocationDescriptor;
        mPeerTwincodeOutboundId = geolocationDescriptor.getTwincodeOutboundId();
        setCopyAllowed(mGeolocationDescriptor.isCopyAllowed());
    }

    GeolocationDescriptor getGeolocationDescriptor() {

        return mGeolocationDescriptor;
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
    public String getInformation(Context context) {

        return mGeolocationDescriptor.getLatitude() + "," + mGeolocationDescriptor.getLongitude();
    }

    //
    // Override Object methods
    //

    @Override
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("PeerLocationItem\n");
        appendTo(stringBuilder);
        stringBuilder.append(" peer: ");
        stringBuilder.append(mPeerTwincodeOutboundId);
        stringBuilder.append("\n");

        return stringBuilder.toString();
    }
}
