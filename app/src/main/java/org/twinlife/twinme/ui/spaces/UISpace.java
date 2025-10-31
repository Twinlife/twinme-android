/*
 *  Copyright (c) 2019-2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.spaces;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.twinlife.twinme.TwinmeApplication;
import org.twinlife.twinme.models.Profile;
import org.twinlife.twinme.models.Space;
import org.twinlife.twinme.models.SpaceSettings;

import java.util.UUID;

public class UISpace {

    private static int sItemId = 0;

    private final long mItemId;

    @NonNull
    private Space mSpace;
    private SpaceSettings mSpaceSettings;
    @Nullable
    private String mNameSpace;
    private String mNameProfile;
    private Bitmap mAvatar;
    private Bitmap mAvatarSpace;

    private boolean mIsCurrentSpace;
    private boolean mHasNotification;

    public UISpace(@NonNull TwinmeApplication twinmeApplication, @NonNull Space space, @Nullable Bitmap avatar, @Nullable Bitmap profileAvatar, @NonNull SpaceSettings spaceSettings) {

        mItemId = sItemId++;

        mSpace = space;
        mSpaceSettings = spaceSettings;
        mNameSpace = space.getName();
        mAvatarSpace = avatar;

        Profile profile = space.getProfile();
        if (profile != null) {
            mNameProfile = profile.getName();
            mAvatar = profileAvatar;
        }
        if (mNameProfile == null) {
            mNameProfile = twinmeApplication.getAnonymousName();
        }
        if (mAvatar == null) {
            mAvatar = twinmeApplication.getAnonymousAvatar();
        }

        mHasNotification = false;
        mIsCurrentSpace = false;
    }

    public long getItemId() {

        return mItemId;
    }

    public Space getSpace() {

        return mSpace;
    }

    public SpaceSettings getSpaceSettings() {

        return mSpaceSettings;
    }

    public UUID getId() {

        return mSpace.getId();
    }

    public String getNameSpace() {

        return mNameSpace;
    }

    public String getNameProfile() {

        return mNameProfile;
    }

    public Bitmap getAvatar() {

        return mAvatar;
    }

    public Bitmap getAvatarSpace() {

        return mAvatarSpace;
    }

    public boolean hasNotification() {

        return mHasNotification;
    }

    public boolean hasProfile() {

        return mSpace.getProfile() != null;
    }

    public void setHasNotification(boolean hasNotification) {

        mHasNotification = hasNotification;
    }

    public boolean isCurrentSpace() {

        return mIsCurrentSpace;
    }

    public void setIsCurrentSpace(boolean isCurrentSpace) {

        mIsCurrentSpace = isCurrentSpace;
    }

    public void update(@NonNull TwinmeApplication twinmeApplication, @NonNull Space space, @Nullable Bitmap avatar, @Nullable Bitmap profileAvatar, @NonNull SpaceSettings spaceSettings) {

        mSpace = space;
        mSpaceSettings = spaceSettings;
        mNameSpace = space.getName();
        mAvatarSpace = avatar;

        Profile profile = space.getProfile();
        if (profile != null) {
            mNameProfile = profile.getName();
            mAvatar = profileAvatar;
        }
        if (mNameProfile == null) {
            mNameProfile = twinmeApplication.getAnonymousName();
        }
        if (mAvatar == null) {
            mAvatar = twinmeApplication.getAnonymousAvatar();
        }
    }

}
