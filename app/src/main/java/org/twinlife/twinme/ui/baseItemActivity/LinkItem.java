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

public class LinkItem extends Item {

    private final String mContent;
    private final URL mUrl;

    @NonNull
    private final ConversationService.ObjectDescriptor mObjectDescriptor;

    public LinkItem(@NonNull ObjectDescriptor objectDescriptor, Descriptor replyToDescriptor, URL url) {

        super(ItemType.LINK, objectDescriptor, replyToDescriptor);

        mContent = objectDescriptor.getMessage();
        mObjectDescriptor = objectDescriptor;

        mUrl = url;

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

    //
    // Override Object methods
    //

    @Override
    @NonNull
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("LinkItem\n");
        appendTo(stringBuilder);
        stringBuilder.append(" content: ");
        stringBuilder.append(mContent);
        stringBuilder.append("\n");

        return stringBuilder.toString();
    }
}
