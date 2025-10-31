/*
 *  Copyright (c) 2017-2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui.users;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.twinlife.twinme.TwinmeApplication;
import org.twinlife.twinme.models.Originator;

import java.util.UUID;

public class UIOriginator implements Comparable<UIOriginator> {
    private static int sItemId = 0;

    private final long mItemId;

    @NonNull
    private Originator mContact;
    @NonNull
    private String mName;
    @Nullable
    private Bitmap mAvatar;
    private boolean mAvatarUnkown;

    public UIOriginator(@NonNull TwinmeApplication twinmeApplication, @NonNull Originator contact, @Nullable Bitmap avatar) {

        mItemId = sItemId++;
        mAvatarUnkown = true;
        mContact = contact;

        String name = contact.getName();
        if (name == null) {
            mName = twinmeApplication.getAnonymousName();
        } else {
            mName = name;
        }

        mAvatar = avatar;
        if (mAvatar == null) {
            mAvatar = twinmeApplication.getAnonymousAvatar();
        } else {
            mAvatarUnkown = false;
        }
    }

    public long getItemId() {

        return mItemId;
    }

    public double getUsageScore() {

        return mContact.getUsageScore();
    }

    public long getLastMessageDate() {

        return mContact.getLastMessageDate();
    }

    public Originator getContact() {

        return mContact;
    }

    public UUID getId() {

        return mContact.getId();
    }

    public String getName() {

        return mName;
    }

    public Bitmap getAvatar() {

        return mAvatar;
    }

    public void setAvatar(Bitmap avatar) {

        mAvatar = avatar;
        mAvatarUnkown = false;
    }

    public UIContactTag getUIContactTag() {

        return null;
    }

    public boolean isScheduleEnable() {

        return false;
    }

    public boolean isCertified() {

        return false;
    }

    public void update(@NonNull TwinmeApplication twinmeApplication, @NonNull Originator contact, Bitmap avatar) {

        mContact = contact;
        String name = contact.getName();
        if (name == null) {
            mName = twinmeApplication.getAnonymousName();
        } else {
            mName = name;
        }

        mAvatar = avatar;
        if (mAvatar == null) {
            mAvatar = twinmeApplication.getAnonymousAvatar();
            mAvatarUnkown = true;
        } else {
            mAvatarUnkown = false;
        }
    }

    @Override
    public int compareTo(@NonNull UIOriginator originator) {

        return mName.compareToIgnoreCase(originator.getName());
    }
}
