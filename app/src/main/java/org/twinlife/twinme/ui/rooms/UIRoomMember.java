/*
 *  Copyright (c) 2020 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.rooms;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.twinlife.twinlife.TwincodeOutbound;

import java.util.UUID;

public class UIRoomMember {
    private static int sItemId = 0;

    private final long mItemId;

    @NonNull
    private TwincodeOutbound mTwincodeOutbound;
    private String mName;
    @Nullable
    private Bitmap mAvatar;

    public UIRoomMember(@NonNull TwincodeOutbound twincodeOutbound, @Nullable Bitmap avatar) {

        mItemId = sItemId++;
        mTwincodeOutbound = twincodeOutbound;
        mName = twincodeOutbound.getName();
        mAvatar = avatar;
    }

    public long getItemId() {

        return mItemId;
    }

    public TwincodeOutbound getTwincodeOutbound() {

        return mTwincodeOutbound;
    }

    public UUID getId() {

        return mTwincodeOutbound.getId();
    }

    public String getName() {

        return mName;
    }

    public Bitmap getAvatar() {

        return mAvatar;
    }

    public void setAvatar(Bitmap avatar) {

        mAvatar = avatar;
    }

    public void update(@NonNull TwincodeOutbound twincodeOutbound, Bitmap avatar) {

        mTwincodeOutbound = twincodeOutbound;
        mName = twincodeOutbound.getName();
        mAvatar = avatar;
    }
}


