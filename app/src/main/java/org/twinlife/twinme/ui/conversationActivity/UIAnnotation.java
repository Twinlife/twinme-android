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

public class UIAnnotation {

    private static int sItemId = 0;
    private final long mItemId;
    @NonNull
    private final UIReaction mReaction;
    @NonNull
    private final String mName;
    @Nullable
    private final Bitmap mAvatar;

    public UIAnnotation(@NonNull UIReaction reaction, @NonNull String name, @Nullable Bitmap avatar) {

        mItemId = sItemId++;

        mReaction = reaction;
        mName = name;
        mAvatar = avatar;
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

    public UIReaction getReaction() {

        return mReaction;
    }
}