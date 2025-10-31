/*
 *  Copyright (c) 2016-2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import android.content.Context;
import android.text.format.Formatter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ConversationService.Descriptor;
import org.twinlife.twinlife.ConversationService.ImageDescriptor;

import java.util.UUID;

public class PeerImageItem extends Item {

    @NonNull
    private final ImageDescriptor mImageDescriptor;

    public PeerImageItem(@NonNull ImageDescriptor imageDescriptor, @Nullable Descriptor replyToDescriptor) {

        super(ItemType.PEER_IMAGE, imageDescriptor, replyToDescriptor);

        mImageDescriptor = imageDescriptor;
        setCopyAllowed(mImageDescriptor.isCopyAllowed());
        setCanReply(true);
    }

    @NonNull
    public ImageDescriptor getImageDescriptor() {

        return mImageDescriptor;
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

        return mImageDescriptor.isAvailable();
    }

    @Override
    public boolean isClearLocalItem() {

        return mImageDescriptor.getLength() == 0 && isAvailableItem();
    }

    @Override
    public long getTimestamp() {

        return getCreatedTimestamp();
    }

    @Override
    public String getPath() {

        return mImageDescriptor.getPath();
    }

    @Override
    public boolean isSameObject(@Nullable Descriptor descriptor) {

        return mImageDescriptor == descriptor;
    }

    @Override
    @NonNull
    String getInformation(@NonNull Context context) {

        if (isClearLocalItem()) {
            return context.getString(R.string.conversation_activity_local_cleanup);
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            if (mImageDescriptor.getExtension() != null) {
                stringBuilder.append(mImageDescriptor.getExtension().toUpperCase());
                stringBuilder.append("\n");
            }

            if (mImageDescriptor.getLength() > 0) {
                stringBuilder.append(Formatter.formatFileSize(context, mImageDescriptor.getLength()));
            }

            if (mImageDescriptor.getHeight() != 0 && mImageDescriptor.getWidth() != 0) {
                stringBuilder.append("\n");
                stringBuilder.append(mImageDescriptor.getWidth());
                stringBuilder.append(" x ");
                stringBuilder.append(mImageDescriptor.getHeight());
            }

            return stringBuilder.toString();
        }
    }

    @Override
    public UUID getPeerTwincodeOutboundId() {

        return mImageDescriptor.getTwincodeOutboundId();
    }

    @Override
    public boolean isSamePeer(@NonNull Item item) {

        return mImageDescriptor.getTwincodeOutboundId().equals(item.getPeerTwincodeOutboundId());
    }

    //
    // Override Object methods
    //

    @Override
    @NonNull
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("PeerImageItem\n");
        appendTo(stringBuilder);
        stringBuilder.append(" imageDescriptor: ");
        stringBuilder.append(mImageDescriptor);
        stringBuilder.append("\n");

        return stringBuilder.toString();
    }
}
