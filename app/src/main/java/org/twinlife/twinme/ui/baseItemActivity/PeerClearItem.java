/*
 *  Copyright (c) 2022 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.twinlife.twinlife.ConversationService.ClearDescriptor;
import org.twinlife.twinlife.ConversationService.Descriptor;

import java.util.UUID;

public class PeerClearItem extends Item {

    @NonNull
    private final ClearDescriptor mClearDescriptor;
    private String mName;

    public PeerClearItem(@NonNull ClearDescriptor clearDescriptor) {

        super(ItemType.PEER_CLEAR, clearDescriptor, null);

        mClearDescriptor = clearDescriptor;
        setCanReply(false);
    }

    @NonNull
    ClearDescriptor getClearDescriptor() {

        return mClearDescriptor;
    }

    public String getName() {

        return mName;
    }

    public void setName(String name) {

        mName = name;
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
    public boolean isSameObject(@Nullable Descriptor descriptor) {

        return mClearDescriptor == descriptor;
    }

    @Override
    public UUID getPeerTwincodeOutboundId() {

        return mClearDescriptor.getTwincodeOutboundId();
    }

    @Override
    public boolean isSamePeer(Item item) {

        return mClearDescriptor.getTwincodeOutboundId().equals(item.getPeerTwincodeOutboundId());
    }

    //
    // Override Object methods
    //

    @Override
    @NonNull
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("PeerClearItem\n");
        appendTo(stringBuilder);
        stringBuilder.append(" clearDescriptor: ");
        stringBuilder.append(mClearDescriptor);
        stringBuilder.append("\n");

        return stringBuilder.toString();
    }
}
