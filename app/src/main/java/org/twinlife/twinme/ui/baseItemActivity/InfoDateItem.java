/*
 *  Copyright (c) 2019 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

public class InfoDateItem extends Item {

    public enum InfoDateItemType {
        SENT,
        RECEIVED,
        SEEN,
        DELETED,
        UPDATED,
        EPHEMERAL
    }

    private final InfoDateItemType mInfoDateItemType;
    private final Item mItem;
    private final Bitmap mAvatar;

    public InfoDateItem(InfoDateItemType infoDateItemType, Item item, Bitmap avatar) {

        super(ItemType.INFO_DATE, DEFAULT_DESCRIPTOR_ID, 0);

        mInfoDateItemType = infoDateItemType;
        mItem = item;
        mAvatar = avatar;
    }

    public Bitmap getAvatar() {

        return mAvatar;
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

    InfoDateItemType getInfoDateItemType() {

        return mInfoDateItemType;
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
        stringBuilder.append("InfoDateItem\n");
        appendTo(stringBuilder);

        return stringBuilder.toString();
    }
}
