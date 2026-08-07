/*
 *  Copyright (c) 2013-2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import org.twinlife.twinlife.AndroidTwinlifeImpl;

public class BootReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = "BootReceiver";

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            if (!AndroidTwinlifeImpl.isStarted()) {
                Log.e(LOG_TAG, "AndroidTwinlifeImpl is not started");
            } else {
                Log.e(LOG_TAG, "AndroidTwinlifeImpl is started");
            }
        }
    }
}
