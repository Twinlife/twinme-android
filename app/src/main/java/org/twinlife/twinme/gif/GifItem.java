/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  GifItem: provider-agnostic representation of a single GIF returned by a
 *  GifProvider (Tenor, Giphy, ...). Keeps only what the picker grid and the
 *  send path need: a small animated preview URL, a full URL to download and
 *  send, and the natural pixel size used to lay out the grid.
 */

package org.twinlife.twinme.gif;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

public class GifItem {

    @NonNull
    private final String mId;
    @NonNull
    private final String mPreviewUrl;
    @NonNull
    private final String mContentUrl;
    private final int mWidth;
    private final int mHeight;
    @Nullable
    private final String mDescription;

    public GifItem(@NonNull String id, @NonNull String previewUrl, @NonNull String contentUrl,
                   int width, int height, @Nullable String description) {
        mId = id;
        mPreviewUrl = previewUrl;
        mContentUrl = contentUrl;
        mWidth = width;
        mHeight = height;
        mDescription = description;
    }

    @NonNull
    public String getId() {
        return mId;
    }

    @NonNull
    public String getPreviewUrl() {
        return mPreviewUrl;
    }

    @NonNull
    public String getContentUrl() {
        return mContentUrl;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    @Nullable
    public String getDescription() {
        return mDescription;
    }

    @NonNull
    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", mId);
        json.put("preview", mPreviewUrl);
        json.put("content", mContentUrl);
        json.put("w", mWidth);
        json.put("h", mHeight);
        if (mDescription != null) {
            json.put("desc", mDescription);
        }
        return json;
    }

    @Nullable
    public static GifItem fromJson(@NonNull JSONObject json) {
        String id = json.optString("id", "");
        String preview = json.optString("preview", "");
        String content = json.optString("content", "");
        if (preview.isEmpty() || content.isEmpty()) {
            return null;
        }
        String desc = json.has("desc") ? json.optString("desc", null) : null;
        return new GifItem(id, preview, content, json.optInt("w", 0), json.optInt("h", 0), desc);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GifItem)) {
            return false;
        }
        GifItem other = (GifItem) o;
        return mContentUrl.equals(other.mContentUrl);
    }

    @Override
    public int hashCode() {
        return mContentUrl.hashCode();
    }
}
