/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (fabrice.trescartes@twin.life)
 */

package org.twinlife.twinme.ui.conversationActivity.poll;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class PollResultItem {

    public enum PollResultItemType {
        POLL_RESULT_CHOICE,
        POLL_RESULT_VOTER
    }

    private static int sItemId = 0;

    private final long mItemId;
    private final PollResultItemType mPollResultItemType;
    private final String mTitle;
    private final Bitmap mAvatar;

    public PollResultItem(PollResultItemType pollResultItemType, @NonNull String title, @Nullable Bitmap avatar) {

        mPollResultItemType = pollResultItemType;
        mTitle = title;
        mAvatar = avatar;
        mItemId = sItemId++;
    }

    public PollResultItemType getPollResultItemType() {

        return mPollResultItemType;
    }

    public String getTitle() {

        return mTitle;
    }

    public Bitmap getAvatar() {

        return mAvatar;
    }

    public long getItemId() {

        return mItemId;
    }
}
