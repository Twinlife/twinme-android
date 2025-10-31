/*
 *  Copyright (c) 2017-2021 twinlife SA & Telefun SAS.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Florian Fossa (ffossa@skyrock.com)
 *   Fabrice Trescartes (fabrice.trescartes@twin.life)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import androidx.annotation.NonNull;

public class SecurityInfoItem extends Item {

    public SecurityInfoItem() {

        super(ItemType.SECURITY_INFO, DEFAULT_DESCRIPTOR_ID, 0);
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
        stringBuilder.append("SecurityInfoItem\n");
        appendTo(stringBuilder);

        return stringBuilder.toString();
    }
}
