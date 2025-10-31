/*
 *  Copyright (c) 2016-2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Houssem Temanni (Houssem.Temanni@twinlife-systems.com)
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import android.content.Context;
import android.text.format.DateUtils;
import android.text.format.Formatter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.twinlife.twinlife.ConversationService.AudioDescriptor;
import org.twinlife.twinlife.ConversationService.Descriptor;

public class AudioItem extends Item {

    @NonNull
    private final AudioDescriptor mAudioDescriptor;

    public AudioItem(@NonNull AudioDescriptor audioDescriptor, @Nullable Descriptor replyToDescriptor) {

        super(ItemType.AUDIO, audioDescriptor, replyToDescriptor);

        mAudioDescriptor = audioDescriptor;
        setCopyAllowed(mAudioDescriptor.isCopyAllowed());
        setCanReply(true);
    }

    @NonNull
    AudioDescriptor getAudioDescriptor() {

        return mAudioDescriptor;
    }

    //
    // Override Item methods
    //

    @Override
    public boolean isPeerItem() {

        return false;
    }

    @Override
    public long getTimestamp() {

        return getCreatedTimestamp();
    }

    @Override
    public String getPath() {

        return mAudioDescriptor.getPath();
    }

    @Override
    public boolean isAvailableItem() {

        return mAudioDescriptor.isAvailable();
    }

    @Override
    String getInformation(Context context) {

        StringBuilder stringBuilder = new StringBuilder();
        if (getAudioDescriptor().getExtension() != null) {
            stringBuilder.append(mAudioDescriptor.getExtension().toUpperCase());
            stringBuilder.append("\n");
        }

        if (getAudioDescriptor().getLength() > 0) {
            stringBuilder.append(Formatter.formatFileSize(context, mAudioDescriptor.getLength()));
        }

        if (getAudioDescriptor().getDuration() > 0) {
            stringBuilder.append("\n");
            stringBuilder.append(DateUtils.formatElapsedTime(mAudioDescriptor.getDuration()));
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
        stringBuilder.append("AudioItem:\n");
        appendTo(stringBuilder);
        stringBuilder.append(" audioDescriptor=");
        stringBuilder.append(mAudioDescriptor);
        stringBuilder.append("\n");

        return stringBuilder.toString();
    }
}
