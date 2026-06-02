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

public class PollItem extends Item {

    private final ConversationService.PollDescriptor mPollDescriptor;

    @NonNull
    private final Map<UUID, List<ConversationService.PollDescriptor.Choice>> mVotes;

    public PollItem(ConversationService.PollDescriptor pollDescriptor) {

        super(ItemType.POLL, pollDescriptor, null);

        mPollDescriptor = pollDescriptor;
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

    @Override
    public boolean isPeerItem() {

        return false;
    }


    @Override
    public long getTimestamp() {

        return getCreatedTimestamp();
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
        stringBuilder.append("PolLItem\n");
        appendTo(stringBuilder);
        stringBuilder.append(" question: ");
        stringBuilder.append(mPollDescriptor.getQuestion());
        stringBuilder.append("\n");

        return stringBuilder.toString();
    }
}