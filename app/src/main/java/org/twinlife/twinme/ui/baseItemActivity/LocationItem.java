/*
 *  Copyright (c) 2019 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.twinlife.twinlife.ConversationService.Descriptor;
import org.twinlife.twinlife.ConversationService.GeolocationDescriptor;

public class LocationItem extends Item {

    @NonNull
    private final GeolocationDescriptor mLocationDescriptor;

    public LocationItem(@NonNull GeolocationDescriptor locationDescriptor, @Nullable Descriptor replyToDescriptor) {

        super(Item.ItemType.LOCATION, locationDescriptor, replyToDescriptor);

        mLocationDescriptor = locationDescriptor;
    }

    GeolocationDescriptor getGeolocationDescriptor() {

        return mLocationDescriptor;
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
        stringBuilder.append("LocationItem\n");
        appendTo(stringBuilder);
        stringBuilder.append(" locationDescriptor: ");
        stringBuilder.append(mLocationDescriptor);
        stringBuilder.append("\n");

        return stringBuilder.toString();
    }
}
