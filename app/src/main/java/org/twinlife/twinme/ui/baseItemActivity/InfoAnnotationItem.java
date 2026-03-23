/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import androidx.annotation.NonNull;

import org.twinlife.twinme.ui.conversationActivity.UIAnnotation;

public class InfoAnnotationItem extends Item {

    private final Item mItem;

    private final UIAnnotation mAnnotation;

    public InfoAnnotationItem(Item item, UIAnnotation annotation) {

        super(ItemType.INFO_ANNOTATION, DEFAULT_DESCRIPTOR_ID, 0);

        mItem = item;
        mAnnotation = annotation;
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

    public UIAnnotation getAnnotation() {

        return mAnnotation;
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
