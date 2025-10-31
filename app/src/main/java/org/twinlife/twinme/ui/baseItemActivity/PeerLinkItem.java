/*
 *  Copyright (c) 2022 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import androidx.annotation.NonNull;

import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinlife.ConversationService.Descriptor;
import org.twinlife.twinlife.ConversationService.ObjectDescriptor;

import java.net.URL;
import java.util.UUID;

public class PeerLinkItem extends Item {

    private final String mContent;
    private final URL mUrl;
    private final UUID mPeerTwincodeOutboundId;

    @NonNull
    private final ConversationService.ObjectDescriptor mObjectDescriptor;

    public PeerLinkItem(@NonNull ObjectDescriptor objectDescriptor, Descriptor replyToDescriptor, URL url) {

        super(ItemType.PEER_LINK, objectDescriptor, replyToDescriptor);

        mObjectDescriptor = objectDescriptor;

        mContent = objectDescriptor.getMessage();
        mUrl = url;
        mPeerTwincodeOutboundId = objectDescriptor.getTwincodeOutboundId();
        setCopyAllowed(objectDescriptor.isCopyAllowed());
        setCanReply(true);
    }

    public String getContent() {

        return mContent;
    }

    public URL getUrl() {

        return mUrl;
    }

    public ObjectDescriptor getObjectDescriptor() {

        return mObjectDescriptor;
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

    //
    // Override Object methods
    //

    @Override
    @NonNull
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("PeerLinkItem\n");
        appendTo(stringBuilder);
        stringBuilder.append(" peer: ");
        stringBuilder.append(mPeerTwincodeOutboundId);
        stringBuilder.append(" content: ");
        stringBuilder.append(mContent);
        stringBuilder.append("\n");

        return stringBuilder.toString();
    }
}
