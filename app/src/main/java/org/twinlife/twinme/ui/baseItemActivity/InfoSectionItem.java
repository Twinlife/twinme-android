/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import androidx.annotation.NonNull;

public class InfoSectionItem extends Item {

    private final Item mItem;

    private final String mTitle;

    public InfoSectionItem(Item item, String title) {

        super(ItemType.INFO_SECTION, DEFAULT_DESCRIPTOR_ID, 0);

        mItem = item;
        mTitle = title;
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

    public String getTitle() {

        return mTitle;
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
        stringBuilder.append("InfoSectionItem\n");
        appendTo(stringBuilder);

        return stringBuilder.toString();
    }
}
