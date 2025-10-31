/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.utils;

import androidx.annotation.Nullable;

public class UIWhatsNew {

    private final String mMessage;

    @Nullable
    private String mImage;

    UIWhatsNew(String message, @Nullable String image) {

        mMessage = message;
        mImage = image;
    }

    public String getMessage() {

        return mMessage;
    }

    @Nullable
    public String getImage() {

        return mImage;
    }

    public void setImage(String image) {

        mImage = image;
    }

}
