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

import androidx.annotation.NonNull;

import org.twinlife.twinlife.ConversationService.Descriptor;
import org.twinlife.twinlife.ConversationService.ObjectDescriptor;

public class MessageItem extends Item {

    private final String mContent;
    private final ObjectDescriptor mObjectDescriptor;

    public MessageItem(ObjectDescriptor objectDescriptor, Descriptor replyToDescriptor) {

        super(ItemType.MESSAGE, objectDescriptor, replyToDescriptor);

        mContent = objectDescriptor.getMessage();
        mObjectDescriptor = objectDescriptor;
        setCopyAllowed(objectDescriptor.isCopyAllowed());
        setCanReply(true);
    }

    public String getContent() {

        return mContent;
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

    public boolean isEdited() {

        return mObjectDescriptor.isEdited();
    }

    //
    // Override Object methods
    //

    @Override
    @NonNull
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("MessageItem\n");
        appendTo(stringBuilder);
        stringBuilder.append(" content: ");
        stringBuilder.append(mContent);
        stringBuilder.append("\n");

        return stringBuilder.toString();
    }
}
