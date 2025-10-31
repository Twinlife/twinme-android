/*
 *  Copyright (c) 2016-2019 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import androidx.annotation.NonNull;

public class TimeItem extends Item {

    public TimeItem(long timestamp) {

        super(ItemType.TIME, DEFAULT_DESCRIPTOR_ID, timestamp);
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
        stringBuilder.append("TimeItem\n");
        appendTo(stringBuilder);

        return stringBuilder.toString();
    }
}