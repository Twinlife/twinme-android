/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import androidx.annotation.NonNull;

public class InfoEphemeralItem extends Item {

    private final Item mItem;

    public InfoEphemeralItem(Item item) {

        super(ItemType.INFO_EPHEMERAL, DEFAULT_DESCRIPTOR_ID, 0);

        mItem = item;
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

    Item getItem() {

        return mItem;
    }

    //
    // Override Object methods
    //

    @Override
    @NonNull
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("InfoEphemeralItem\n");
        appendTo(stringBuilder);

        return stringBuilder.toString();
    }
}