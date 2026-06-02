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

import org.twinlife.twinlife.ConversationService;

import java.util.ArrayList;
import java.util.List;

public class UIPollResult {

    public static class UIPollResultVoter {
        @Nullable
        private final String mName;
        @Nullable
        private final Bitmap mAvatar;

        public UIPollResultVoter(@Nullable String name, @Nullable Bitmap avatar) {

            mName = name;
            mAvatar = avatar;
        }

        @Nullable
        public String getName() {

            return mName;
        }

        @Nullable
        public Bitmap getAvatar() {

            return mAvatar;
        }
    }

    @NonNull
    private ConversationService.PollDescriptor.Choice mChoice;
    private int mCount;
    private boolean mIsSelected;
    @NonNull
    private final List<UIPollResultVoter> mPollResultVoters;

    public UIPollResult(@NonNull ConversationService.PollDescriptor.Choice choice) {

        mChoice = choice;
        mCount = 0;
        mIsSelected = false;
        mPollResultVoters = new ArrayList<>();
    }

    @NonNull
    public ConversationService.PollDescriptor.Choice getChoice() {

        return mChoice;
    }

    public void setChoice(@NonNull ConversationService.PollDescriptor.Choice choice) {

        mChoice = choice;
    }

    public int getCount() {

        return mCount;
    }

    public void setCount(int count) {

        mCount = count;
    }

    public boolean isSelected() {

        return mIsSelected;
    }

    public void setSelected(boolean selected) {

        mIsSelected = selected;
    }

    @NonNull
    public List<UIPollResultVoter> getPollResultVoters() {

        return mPollResultVoters;
    }

    public void addPollResultVoter(@NonNull UIPollResultVoter voter) {

        mPollResultVoters.add(voter);
    }
}
