/*
 *  Copyright (c) 2022 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import androidx.annotation.NonNull;

import org.twinlife.twinlife.ConversationService.ClearDescriptor;

public class ClearItem extends Item {

    private final ClearDescriptor mClearDescriptor;

    public ClearItem(ClearDescriptor clearDescriptor) {

        super(ItemType.CLEAR, clearDescriptor, null);

        mClearDescriptor = clearDescriptor;
    }

    ClearDescriptor getClearDescriptor() {

        return mClearDescriptor;
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
        stringBuilder.append("ClearItem:\n");
        appendTo(stringBuilder);
        stringBuilder.append(" clearDescriptor=");
        stringBuilder.append(mClearDescriptor);
        stringBuilder.append("\n");

        return stringBuilder.toString();
    }
}
