/*
 *  Copyright (c) 2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.conversationActivity;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.twinlife.twinlife.ConversationService;

public class UIAnnotation {

    private static int sItemId = 0;
    private final long mItemId;

    private final ConversationService.AnnotationType mAnnotationType;
    @Nullable
    private final UIReaction mReaction;
    @NonNull
    private final String mName;
    @Nullable
    private final Bitmap mAvatar;

    private final long mTimestamp;

    public UIAnnotation(@Nullable UIReaction reaction, @NonNull String name, @Nullable Bitmap avatar, long timestamp, ConversationService.AnnotationType annotationType) {

        mItemId = sItemId++;

        mAnnotationType = annotationType;
        mReaction = reaction;
        mName = name;
        mAvatar = avatar;
        mTimestamp = timestamp;
    }

    public long getItemId() {

        return mItemId;
    }

    public String getName() {

        return mName;
    }

    public Bitmap getAvatar() {

        return mAvatar;
    }

    public ConversationService.AnnotationType getAnnotationType() {

        return mAnnotationType;
    }

    public int getOrderPriority() {

        if (mAnnotationType == ConversationService.AnnotationType.LIKE) {
            return 3;
        }
        else if (mAnnotationType == ConversationService.AnnotationType.READ) {
            return 2;
        }
        else if (mAnnotationType == ConversationService.AnnotationType.RECEIVED) {
            return 1;
        }
        else {
            return 0;
        }
    }

    public long getTimestamp() {

        return mTimestamp;
    }

    public UIReaction getReaction() {

        return mReaction;
    }
}