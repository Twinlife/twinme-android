/*
 *  Copyright (c) 2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.premiumServicesActivity;

public class UIPremiumFeatureDetail {

    private final String mMessage;
    private final int mImageId;

    UIPremiumFeatureDetail(String message, int imageId) {

        mMessage = message;
        mImageId = imageId;
    }

    public String getMessage() {

        return mMessage;
    }

    public int getImageId() {

        return mImageId;
    }
}