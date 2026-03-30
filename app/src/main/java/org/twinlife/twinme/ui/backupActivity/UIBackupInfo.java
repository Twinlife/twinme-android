/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.backupActivity;

import android.content.Context;

import androidx.annotation.NonNull;

import org.twinlife.twinme.utils.CommonUtils;

import java.util.UUID;

public class UIBackupInfo {

    private final UUID mBackupId;

    private final long mBackupDate;

    public UIBackupInfo(@NonNull UUID backupId, long backupDate) {

        mBackupId = backupId;
        mBackupDate = backupDate;
    }

    public String getId () {

        return mBackupId.toString().length() >= 8  ? mBackupId.toString().substring(0, 8) : mBackupId.toString();
    }

    public long getDate() {

        return mBackupDate;
    }

    public String formatDate(Context context) {

        return CommonUtils.formatBackupInterval(context, mBackupDate, false);
    }
}
