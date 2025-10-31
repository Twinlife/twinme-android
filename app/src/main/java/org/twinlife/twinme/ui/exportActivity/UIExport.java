/*
 *  Copyright (c) 2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui.exportActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.format.Formatter;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.utils.Utils;

public class UIExport {

    public enum ExportContentType {
        MESSAGE,
        IMAGE,
        VIDEO,
        AUDIO,
        FILE,
        MEDIA_AND_FILE,
        ALL
    }

    private final ExportContentType mExportContentType;
    private final int mImage;
    private long mCount = 0;
    private long mSize = 0;
    private boolean mChecked;

    public UIExport(ExportContentType exportContentType, int image, boolean checked) {

        mExportContentType = exportContentType;
        mImage = image;
        mChecked = checked;
    }

    public ExportContentType getExportContentType() {

        return mExportContentType;
    }

    public int getImage() {

        return mImage;
    }

    public void setCount(long count) {

        mCount = count;
    }

    public long getCount() {

        return mCount;
    }

    public void setSize(long size) {

        mSize = size;
    }

    public long getSize() {

        return mSize;
    }

    public void setChecked(boolean checked) {

        mChecked = checked;
    }

    public boolean isChecked() {

        return mChecked;
    }

    public String getTitle(Context context) {

        String  title = "";

        switch (mExportContentType) {
            case MESSAGE:
                title = context.getString(R.string.settings_activity_chat_category_title);
                break;

            case IMAGE:
                title = context.getString(R.string.export_activity_images);
                break;

            case VIDEO:
                title = context.getString(R.string.export_activity_videos);
                break;

            case AUDIO:
                title = context.getString(R.string.export_activity_voice_messages);
                break;

            case FILE:
                title = context.getString(R.string.export_activity_files);
                break;

            case MEDIA_AND_FILE:
                title = context.getString(R.string.cleanup_activity_medias_and_files);
                break;

            case ALL:
                title = context.getString(R.string.cleanup_activity_messages);
                break;
        }

        return Utils.capitalizeString(title);
    }
    
    @SuppressLint("DefaultLocale")
    public String getInformation(Context context) {

        String information;
        String contentType = getContentType(context);

        if (mSize > 0) {
            information = String.format("%d %s - %s", mCount, contentType, Formatter.formatFileSize(context, mSize));
        } else {
            information = String.format("%d %s", mCount, contentType);
        }

        return information;
    }

    private String getContentType(Context context) {

        final String contentType;

        if (mExportContentType == ExportContentType.MESSAGE) {
            if (mCount > 1) {
                contentType = context.getString(R.string.settings_activity_chat_category_title);
            } else {
                contentType = context.getString(R.string.feedback_activity_message);
            }
        } else {
            if (mCount > 1) {
                contentType = context.getString(R.string.export_activity_files);
            } else {
                contentType = context.getString(R.string.export_activity_file);
            }
        }

        return contentType;
    }
}
