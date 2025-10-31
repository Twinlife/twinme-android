/*
 *  Copyright (c) 2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.cleanupActivity;

import android.content.Context;
import android.graphics.Color;
import android.text.format.Formatter;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class UIStorage {

    public enum StorageType {
        TOTAL,
        USED,
        FREE,
        APP,
        CONVERSATION
    }

    private final StorageType mStorageType;
    private long mSize;
    private String mName;

    public UIStorage(StorageType storageType, long size) {

        mStorageType = storageType;
        mSize = size;
    }

    public StorageType getStorageType() {

        return mStorageType;
    }

    public long getSize() {

        return mSize;
    }

    public void setSize(long size) {

        mSize = size;
    }

    public void setName(String name) {

        mName = name;
    }

    public String getTitle(Context context) {

        String  title = "";
        switch (mStorageType) {
            case TOTAL:
                title = context.getString(R.string.cleanup_activity_total);
                break;

            case USED:
                title = context.getString(R.string.cleanup_activity_used);
                break;

            case FREE:
                title = context.getString(R.string.cleanup_activity_free);
                break;

            case APP:
                title = context.getString(R.string.application_name);
                break;

            case CONVERSATION:
                if (mName != null) {
                    title = mName;
                } else {
                    title = context.getString(R.string.conversations_fragment_title);
                }
                break;
        }

        return title;
    }

    public String getSize(Context context) {

        return Formatter.formatFileSize(context, mSize);
    }

    public int getBackgroundColor() {

        int backgroundColor = Color.TRANSPARENT;

        switch (mStorageType) {
            case USED:
                backgroundColor = Color.rgb(0,174,255);
                break;

            case TOTAL:
            case FREE:
                backgroundColor = Color.rgb(222,232,255);
                break;

            case APP:
                backgroundColor = Color.rgb(253,96,93);
                break;

            case CONVERSATION:
                backgroundColor = Design.WHITE_COLOR;
                break;
        }

        return backgroundColor;
    }

    public int getBorderColor() {

        if (mStorageType == StorageType.CONVERSATION) {
            return Color.rgb(151,151,151);
        }

        return Color.TRANSPARENT;
    }
}
