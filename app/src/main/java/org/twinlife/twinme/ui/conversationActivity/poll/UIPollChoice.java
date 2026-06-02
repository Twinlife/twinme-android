/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (fabrice.trescartes@twin.life)
 */

package org.twinlife.twinme.ui.conversationActivity.poll;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import org.twinlife.device.android.twinme.R;

import java.util.ArrayList;
import java.util.List;

public class UIPollChoice {

    private final int mPosition;
    @NonNull
    private String mChoice;
    private int mCount;
    private boolean mIsSelected;
    @NonNull
    private List<Bitmap> mAvatars;

    public UIPollChoice(int position, @NonNull String choice) {

        mPosition = position;
        mChoice = choice;
        mCount = 0;
        mIsSelected = false;
        mAvatars = new ArrayList<>();
    }

    public int getPosition() {

        return mPosition;
    }

    @NonNull
    public String getChoice() {

        return mChoice;
    }

    public void setChoice(@NonNull String choice) {

        mChoice = choice;
    }

    @NonNull
    public String getChoicePosition(Context context) {

        return String.format(context.getString(R.string.poll_view_choice), mPosition + 1);
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
    public List<Bitmap> getAvatars() {

        return mAvatars;
    }

    public void setAvatars(@NonNull List<Bitmap> avatars) {

        mAvatars = avatars;
    }
}
