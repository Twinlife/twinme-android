/*
 *  Copyright (c) 2020 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.profiles;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.twinlife.twinme.models.Profile;

public class UIProfile {

    private static int sItemId = 0;

    private final long mItemId;

    @NonNull
    private Profile mProfile;
    @NonNull
    private String mName;
    private Bitmap mAvatar;

    public UIProfile(@NonNull Profile profile, @Nullable Bitmap avatar) {

        mItemId = sItemId++;

        mProfile = profile;
        mName = profile.getName();
        mAvatar = avatar;
    }

    public long getItemId() {

        return mItemId;
    }

    public Profile getProfile() {

        return mProfile;
    }

    public String getName() {

        return mName;
    }

    public Bitmap getAvatar() {

        return mAvatar;
    }

    public void update(@NonNull Profile profile, @Nullable Bitmap avatar) {

        mProfile = profile;
        mName = profile.getName();
        mAvatar = avatar;
    }
}
