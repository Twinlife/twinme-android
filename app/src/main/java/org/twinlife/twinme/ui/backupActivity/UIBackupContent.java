/*
 *  Copyright (c) 2024-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.backupActivity;

import android.content.Context;

import org.twinlife.device.android.twinme.R;

public class UIBackupContent {

    public enum BackupContentType {
        CONTACTS,
        GROUPS
    }

    private final BackupContentType mBackupContentType;
    private int mCount;

    public UIBackupContent(BackupContentType backupContentType, int count) {

        mBackupContentType = backupContentType;
        mCount = count;
    }

    public String getTitle(Context context) {

        String  title = "";
        switch (mBackupContentType) {
            case CONTACTS:
                title = context.getString(R.string.share_view_contact_list);
                break;

            case GROUPS:
                title = context.getString(R.string.share_view_group_list);
                break;
        }

        return title;
    }

    public int getIcon(Context context) {

        int iconId = -1;
        switch (mBackupContentType) {
            case CONTACTS:
                iconId = R.drawable.contacts_icon;
                break;

            case GROUPS:
                iconId = R.drawable.groups_icon;
                break;
        }

        return iconId;
    }

    public int getContentCount() {

        return mCount;
    }

    public void setContentCount(int count) {

        mCount = count;
    }
}
