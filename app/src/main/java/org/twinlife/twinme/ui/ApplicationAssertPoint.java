/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui;

import org.twinlife.twinlife.AssertPoint;

public enum ApplicationAssertPoint implements AssertPoint {
    INVALID_DESCRIPTOR,
    INVALID_POSITION,
    UNEXPECTED_EXCEPTION,
    AUDIO_RECORD_NULL,
    AUDIO_RECORD_ERROR,
    POST_NOTIFICATION_ERROR,
    POST_NOTIFICATION_SECURITY;

    public int getIdentifier() {

        return this.ordinal() + BASE_VALUE;
    }

    private static final int BASE_VALUE = 5000;
}