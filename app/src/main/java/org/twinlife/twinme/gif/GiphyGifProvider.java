/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Giphy GIF provider. Uses the Giphy v1 REST API.
 *  Docs: https://developers.giphy.com/docs/api/endpoint
 */

package org.twinlife.twinme.gif;

import android.net.Uri;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.Executor;

public class GiphyGifProvider extends AbstractHttpGifProvider {

    private static final String BASE_URL = "https://api.giphy.com/v1/gifs";
    private static final String RATING = "g";   // strictest content rating

    @Nullable
    private final String mApiKey;

    public GiphyGifProvider(@Nullable String apiKey, @NonNull Executor executor, @NonNull Handler mainHandler) {
        super(executor, mainHandler);
        mApiKey = apiKey;
    }

    @NonNull
    @Override
    public String getDisplayName() {
        return "GIPHY";
    }

    @Override
    public boolean isConfigured() {
        return mApiKey != null && !mApiKey.isEmpty();
    }

    @NonNull
    @Override
    protected String buildUrl(boolean trending, @Nullable String query, int limit, @Nullable String position) {
        int offset = 0;
        if (position != null && !position.isEmpty()) {
            try {
                offset = Integer.parseInt(position);
            } catch (NumberFormatException ignored) {
            }
        }
        Uri.Builder builder = Uri.parse(BASE_URL + (trending ? "/trending" : "/search")).buildUpon();
        builder.appendQueryParameter("api_key", mApiKey == null ? "" : mApiKey);
        builder.appendQueryParameter("limit", Integer.toString(limit));
        builder.appendQueryParameter("offset", Integer.toString(offset));
        builder.appendQueryParameter("rating", RATING);
        builder.appendQueryParameter("bundle", "messaging_non_clips");
        if (!trending && query != null) {
            builder.appendQueryParameter("q", query);
        }
        return builder.build().toString();
    }

    @Override
    protected void parse(@NonNull JSONObject root, int requestLimit, @Nullable String requestPosition,
                         @NonNull List<GifItem> out, @NonNull String[] nextHolder) throws JSONException {
        JSONArray data = root.optJSONArray("data");
        if (data != null) {
            for (int i = 0; i < data.length(); i++) {
                GifItem item = parseResult(data.optJSONObject(i));
                if (item != null) {
                    out.add(item);
                }
            }
        }
        int offset = 0;
        if (requestPosition != null && !requestPosition.isEmpty()) {
            try {
                offset = Integer.parseInt(requestPosition);
            } catch (NumberFormatException ignored) {
            }
        }
        int returned = (data != null) ? data.length() : 0;
        // No further page once a short page comes back.
        nextHolder[0] = (returned >= requestLimit) ? Integer.toString(offset + requestLimit) : null;
    }

    @Nullable
    private GifItem parseResult(@Nullable JSONObject result) {
        if (result == null) {
            return null;
        }
        JSONObject images = result.optJSONObject("images");
        if (images == null) {
            return null;
        }
        JSONObject preview = firstNonNull(images.optJSONObject("fixed_width"),
                images.optJSONObject("preview_gif"), images.optJSONObject("downsized"));
        JSONObject content = firstNonNull(images.optJSONObject("downsized_medium"),
                images.optJSONObject("downsized"), images.optJSONObject("original"));
        if (preview == null || content == null) {
            return null;
        }
        String previewUrl = preview.optString("url", "");
        String contentUrl = content.optString("url", "");
        if (previewUrl.isEmpty() || contentUrl.isEmpty()) {
            return null;
        }
        int width = preview.optInt("width", 0);
        int height = preview.optInt("height", 0);
        String id = result.optString("id", contentUrl);
        String desc = result.has("title") ? result.optString("title", null) : null;
        return new GifItem(id, previewUrl, contentUrl, width, height, desc);
    }

    @Nullable
    private static JSONObject firstNonNull(@Nullable JSONObject... candidates) {
        for (JSONObject candidate : candidates) {
            if (candidate != null) {
                return candidate;
            }
        }
        return null;
    }
}
