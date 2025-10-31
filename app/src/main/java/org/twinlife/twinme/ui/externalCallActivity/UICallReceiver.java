/*
 *  Copyright (c) 2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.externalCallActivity;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.twinlife.twinme.TwinmeApplication;
import org.twinlife.twinme.models.CallReceiver;
import org.twinlife.twinme.models.Originator;
import org.twinlife.twinme.ui.users.UIOriginator;

public class UICallReceiver extends UIOriginator {

    public UICallReceiver(@NonNull TwinmeApplication twinmeApplication, @NonNull Originator contact, @Nullable Bitmap avatar) {
        super(twinmeApplication, contact, avatar);
    }

    @Override
    public boolean isScheduleEnable() {

        if (getContact() instanceof CallReceiver) {
            CallReceiver callReceiver = (CallReceiver) getContact();
            if ( callReceiver.getCapabilities().getSchedule() != null) {
                return callReceiver.getCapabilities().getSchedule().isEnabled();
            }
        }

        return false;
    }
}
