/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.conversationActivity;

import android.content.Context;
import android.net.Uri;
import android.text.format.Formatter;

import androidx.annotation.NonNull;

import org.twinlife.device.android.twinme.R;

public class UIPreviewFile {

    private final Uri mUri;
    private final String mTitle;
    private int mIcon;
    private String mSize;

    public UIPreviewFile(Context context, String title, @NonNull Uri uri, String size) {

        mUri = uri;
        mTitle = title;
        initInfo(context, size);
    }

    public String getTitle() {

        return mTitle;
    }

    public int getIcon() {

        return mIcon;
    }

    public String getSize() {

        return mSize;
    }

    public Uri getUri() {

        return mUri;
    }

    private void initInfo(Context context, String size) {

        mIcon = R.drawable.file_grey;

        if (mTitle != null) {
            if (mTitle.endsWith(".doc") || mTitle.endsWith(".docx")) {
                mIcon = R.drawable.file_word;
            } else if (mTitle.endsWith(".xls") || mTitle.endsWith(".xlsx")) {
                mIcon = R.drawable.file_excel;
            } else if (mTitle.endsWith(".ppt") || mTitle.endsWith(".pptx")) {
                mIcon = R.drawable.file_powerpoint;
            } else if (mTitle.endsWith(".pdf")) {
                mIcon = R.drawable.file_pdf;
            }
        }

        if (size != null && !size.isEmpty()){
            mSize = Formatter.formatFileSize(context, Integer.parseInt(size));
        } else {
            mSize = "";
        }
    }
}
