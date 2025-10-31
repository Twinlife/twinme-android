/*
 *  Copyright (c) 2016-2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import androidx.annotation.NonNull;

import org.twinlife.twinlife.ConversationService.Descriptor;
import org.twinlife.twinlife.ConversationService.ObjectDescriptor;

import java.util.UUID;

public class PeerMessageItem extends Item {

    private final String mContent;
    private final UUID mPeerTwincodeOutboundId;
    private final ObjectDescriptor mObjectDescriptor;

    public PeerMessageItem(ObjectDescriptor objectDescriptor, Descriptor replyToDescriptor) {

        super(ItemType.PEER_MESSAGE, objectDescriptor, replyToDescriptor);

        mContent = objectDescriptor.getMessage();
        mObjectDescriptor = objectDescriptor;
        mPeerTwincodeOutboundId = objectDescriptor.getTwincodeOutboundId();
        setCopyAllowed(objectDescriptor.isCopyAllowed());
        setCanReply(true);
    }

    public String getContent() {

        return mContent;
    }

    @Override
    public UUID getPeerTwincodeOutboundId() {

        return mPeerTwincodeOutboundId;
    }

    @Override
    public boolean isSamePeer(Item item) {

        return mPeerTwincodeOutboundId.equals(item.getPeerTwincodeOutboundId());
    }

    public boolean isEdited() {

        return mObjectDescriptor.isEdited();
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

    //
    // Override Object methods
    //

    @Override
    @NonNull
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("PeerMessageItem\n");
        appendTo(stringBuilder);
        stringBuilder.append(" peer: ");
        stringBuilder.append(mPeerTwincodeOutboundId);
        stringBuilder.append(" content: ");
        stringBuilder.append(mContent);
        stringBuilder.append("\n");

        return stringBuilder.toString();
    }
}
