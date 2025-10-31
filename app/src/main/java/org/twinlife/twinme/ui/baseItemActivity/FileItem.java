/*
 *  Copyright (c) 2018-2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Yannis Le Gal (Yannis.LeGal@twin.life)
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import android.content.Context;
import android.text.format.Formatter;

import androidx.annotation.NonNull;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ConversationService.Descriptor;
import org.twinlife.twinlife.ConversationService.NamedFileDescriptor;

public class FileItem extends Item {

    public static int getFileIcon(String path) {

        int iconRes = R.drawable.file_grey;
        if (path.endsWith(".doc") || path.endsWith(".docx")) {
            iconRes = R.drawable.file_word;
        } else if (path.endsWith(".xls") || path.endsWith(".xlsx")) {
            iconRes = R.drawable.file_excel;
        } else if (path.endsWith(".ppt") || path.endsWith(".pptx")) {
            iconRes = R.drawable.file_powerpoint;
        } else if (path.endsWith(".pdf")) {
            iconRes = R.drawable.file_pdf;
        }

        return iconRes;
    }

    private final NamedFileDescriptor mNamedFileDescriptor;

    public FileItem(NamedFileDescriptor namedFileDescriptor, Descriptor replyToDescriptor) {

        super(ItemType.FILE, namedFileDescriptor, replyToDescriptor);

        mNamedFileDescriptor = namedFileDescriptor;
        setCopyAllowed(mNamedFileDescriptor.isCopyAllowed());
        setCanReply(true);
    }

    public NamedFileDescriptor getNamedFileDescriptor() {

        return mNamedFileDescriptor;
    }

    //
    // Override Item methods
    //

    @Override
    public boolean isPeerItem() {

        return false;
    }

    @Override
    public boolean isAvailableItem() {

        return mNamedFileDescriptor.isAvailable();
    }

    @Override
    public long getTimestamp() {

        return getCreatedTimestamp();
    }

    @Override
    public String getPath() {

        return mNamedFileDescriptor.getPath();
    }

    @Override
    String getInformation(Context context) {

        StringBuilder stringBuilder = new StringBuilder();
        if (mNamedFileDescriptor.getExtension() != null) {
            stringBuilder.append(mNamedFileDescriptor.getExtension().toUpperCase());
            stringBuilder.append("\n");
        }

        if (mNamedFileDescriptor.getLength() > 0) {
            stringBuilder.append(Formatter.formatFileSize(context, mNamedFileDescriptor.getLength()));
        }

        return stringBuilder.toString();
    }

    //
    // Override Object methods
    //

    @Override
    @NonNull
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("FileItem\n");
        appendTo(stringBuilder);
        stringBuilder.append(" namedFileDescriptor: ");
        stringBuilder.append(mNamedFileDescriptor);
        stringBuilder.append("\n");

        return stringBuilder.toString();
    }
}
