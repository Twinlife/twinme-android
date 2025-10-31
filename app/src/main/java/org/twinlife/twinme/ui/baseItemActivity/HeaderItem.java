/*
 *  Copyright (c) 2016-2019 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import androidx.annotation.NonNull;

class HeaderItem extends Item {

    HeaderItem() {

        super(ItemType.HEADER, DEFAULT_DESCRIPTOR_ID, 0);
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

        return 0;
    }

    //
    // Override Object methods
    //

    @Override
    @NonNull
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("HeaderItem\n");
        appendTo(stringBuilder);

        return stringBuilder.toString();
    }
}