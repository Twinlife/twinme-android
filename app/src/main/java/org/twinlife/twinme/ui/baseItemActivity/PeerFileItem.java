/*
 *  Copyright (c) 2018-2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Yannis Le Gal (Yannis.LeGal@twin.life)
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import android.content.Context;
import android.text.format.Formatter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.twinlife.twinlife.ConversationService.Descriptor;
import org.twinlife.twinlife.ConversationService.NamedFileDescriptor;

import java.util.UUID;

public class PeerFileItem extends Item {

    private final NamedFileDescriptor mNamedFileDescriptor;
    private final UUID mPeerTwincodeOutboundId;

    public PeerFileItem(NamedFileDescriptor namedFileDescriptor, Descriptor replyToDescriptor) {

        super(ItemType.PEER_FILE, namedFileDescriptor, replyToDescriptor);

        mNamedFileDescriptor = namedFileDescriptor;
        mPeerTwincodeOutboundId = namedFileDescriptor.getTwincodeOutboundId();
        setCopyAllowed(mNamedFileDescriptor.isCopyAllowed());
        setCanReply(true);
    }

    public NamedFileDescriptor getNamedFileDescriptor() {

        return mNamedFileDescriptor;
    }

    //
    // Override Item methods
    //

    @Override
    public boolean isPeerItem() {

        return true;
    }

    @Override
    public boolean isAvailableItem() {

        return mNamedFileDescriptor.isAvailable();
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

    @Override
    public String getPath() {

        return mNamedFileDescriptor.getPath();
    }

    @Override
    public boolean isSameObject(@Nullable Descriptor descriptor) {

        return mNamedFileDescriptor == descriptor;
    }

    @Override
    String getInformation(Context context) {

        StringBuilder stringBuilder = new StringBuilder();
        if (mNamedFileDescriptor.getExtension() != null) {
            stringBuilder.append(mNamedFileDescriptor.getExtension().toUpperCase());
            stringBuilder.append("\n");
        }

        if (mNamedFileDescriptor.getLength() > 0) {
            stringBuilder.append(Formatter.formatFileSize(context, mNamedFileDescriptor.getLength()));
        }

        return stringBuilder.toString();
    }

    //
    // Override Object methods
    //

    @Override
    @NonNull
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("PeerFileItem\n");
        appendTo(stringBuilder);
        stringBuilder.append(" namedFileDescriptor: ");
        stringBuilder.append(mNamedFileDescriptor);
        stringBuilder.append("\n");

        return stringBuilder.toString();
    }
}
