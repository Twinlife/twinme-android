/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (fabrice.trescartes@twin.life)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import androidx.annotation.NonNull;

import org.twinlife.twinlife.ConversationService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PeerPollItem extends Item {

    @NonNull
    private final ConversationService.PollDescriptor mPollDescriptor;

    private final UUID mPeerTwincodeOutboundId;

    @NonNull
    private final Map<UUID, List<ConversationService.PollDescriptor.Choice>> mVotes;

    public PeerPollItem(@NonNull ConversationService.PollDescriptor pollDescriptor) {

        super(ItemType.PEER_POLL, pollDescriptor, null);

        mPollDescriptor = pollDescriptor;
        mPeerTwincodeOutboundId = pollDescriptor.getTwincodeOutboundId();
        mVotes = pollDescriptor.getVotes();
        setCopyAllowed(false);
        setCanReply(false);
    }

    public ConversationService.PollDescriptor getPollDescriptor() {

        return mPollDescriptor;
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
    public UUID getPeerTwincodeOutboundId() {

        return mPeerTwincodeOutboundId;
    }

    @Override
    public boolean isSamePeer(Item item) {

        return mPeerTwincodeOutboundId.equals(item.getPeerTwincodeOutboundId());
    }

    public void updateVotes(ConversationService.PollDescriptor pollDescriptor) {

        mVotes.clear();
        mVotes.putAll(pollDescriptor.getVotes());
    }

    public Map<UUID, List<ConversationService.PollDescriptor.Choice>> getVotes() {

        return mVotes;
    }

    //
    // Override Object methods
    //

    @Override
    @NonNull
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("PeerPollItem\n");
        appendTo(stringBuilder);
        stringBuilder.append(" peer: ");
        stringBuilder.append(mPeerTwincodeOutboundId);
        stringBuilder.append(" question: ");
        stringBuilder.append(mPollDescriptor.getQuestion());
        stringBuilder.append("\n");

        return stringBuilder.toString();
    }
}