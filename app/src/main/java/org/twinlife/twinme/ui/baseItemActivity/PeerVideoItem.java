/*
 *  Copyright (c) 2018-2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (fabrice.trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import android.content.Context;
import android.text.format.DateUtils;
import android.text.format.Formatter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ConversationService.Descriptor;
import org.twinlife.twinlife.ConversationService.VideoDescriptor;

import java.util.UUID;

public class PeerVideoItem extends Item {

    @NonNull
    private final VideoDescriptor mVideoDescriptor;
    private final UUID mPeerTwincodeOutboundId;

    public PeerVideoItem(@NonNull VideoDescriptor videoDescriptor, @Nullable Descriptor replyToDescriptor) {

        super(ItemType.PEER_VIDEO, videoDescriptor, replyToDescriptor);

        mVideoDescriptor = videoDescriptor;
        mPeerTwincodeOutboundId = videoDescriptor.getTwincodeOutboundId();
        setCopyAllowed(mVideoDescriptor.isCopyAllowed());
        setCanReply(true);
    }

    @NonNull
    public VideoDescriptor getVideoDescriptor() {

        return mVideoDescriptor;
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

        return mVideoDescriptor.isAvailable();
    }

    @Override
    public boolean isClearLocalItem() {

        return mVideoDescriptor.getLength() == 0 && isAvailableItem();
    }

    @Override
    public long getTimestamp() {

        return getCreatedTimestamp();
    }

    @Override
    public String getPath() {

        return mVideoDescriptor.getPath();
    }

    @Override
    public boolean isSameObject(@Nullable Descriptor descriptor) {

        return mVideoDescriptor == descriptor;
    }

    @Override
    String getInformation(Context context) {

        if (isClearLocalItem()) {
            return context.getString(R.string.conversation_activity_local_cleanup);
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            if (mVideoDescriptor.getExtension() != null) {
                stringBuilder.append(mVideoDescriptor.getExtension().toUpperCase());
                stringBuilder.append("\n");
            }

            if (mVideoDescriptor.getLength() > 0) {
                stringBuilder.append(Formatter.formatFileSize(context, mVideoDescriptor.getLength()));
            }

            if (mVideoDescriptor.getHeight() != 0 && mVideoDescriptor.getWidth() != 0) {
                stringBuilder.append("\n");
                stringBuilder.append(mVideoDescriptor.getWidth());
                stringBuilder.append(" x ");
                stringBuilder.append(mVideoDescriptor.getHeight());
            }

            if (mVideoDescriptor.getDuration() > 0) {
                stringBuilder.append("\n");
                stringBuilder.append(DateUtils.formatElapsedTime(mVideoDescriptor.getDuration()));
            }

            return stringBuilder.toString();
        }
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
    // Override Object methods
    //

    @Override
    @NonNull
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("PeerVideoItem\n");
        appendTo(stringBuilder);
        stringBuilder.append(" videoDescriptor: ");
        stringBuilder.append(mVideoDescriptor);
        stringBuilder.append("\n");

        return stringBuilder.toString();
    }

}


