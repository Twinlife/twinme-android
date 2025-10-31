/*
 *  Copyright (c) 2017-2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Houssem Temanni (Houssem.Temanni@twinlife-systems.com)
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 */

package org.twinlife.twinme.ui.conversationActivity;

import org.twinlife.twinme.utils.MediaMetaData;

public class MusicItem {
    private static int sItemId = 0;

    private final long mItemId;

    private final String mPath;
    private final MediaMetaData mMediaMetaData;

    public MusicItem(String path, MediaMetaData mediaMetaData) {

        mItemId = sItemId++;
        mPath = path;
        mMediaMetaData = mediaMetaData;
    }

    public long getItemId() {

        return mItemId;
    }

    public String getPath() {

        return mPath;
    }

    public MediaMetaData getMediaMetaData() {

        return mMediaMetaData;
    }

    public boolean isMediaMetaDataContains(String text) {

        return (mMediaMetaData.title != null && mMediaMetaData.title.toLowerCase().contains(text)) || (mMediaMetaData.artist != null && mMediaMetaData.artist.toLowerCase().contains(text)) ||  (mMediaMetaData.album != null && mMediaMetaData.album.toLowerCase().contains(text));
    }
}