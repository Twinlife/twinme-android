package org.twinlife.twinme.ui.baseItemActivity;

import androidx.annotation.NonNull;

public class InfoDeleteItem extends Item {

    private final Item mItem;

    public InfoDeleteItem(Item item) {

        super(ItemType.INFO_DELETED, DEFAULT_DESCRIPTOR_ID, 0);

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
        stringBuilder.append("InfoDeleteItem\n");
        appendTo(stringBuilder);

        return stringBuilder.toString();
    }
}