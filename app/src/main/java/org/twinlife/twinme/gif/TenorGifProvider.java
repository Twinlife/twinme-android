/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Tenor (Google) GIF provider. Uses the Tenor v2 REST API.
 *  Docs: https://developers.google.com/tenor/guides/quickstart
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

public class TenorGifProvider extends AbstractHttpGifProvider {

    private static final String BASE_URL = "https://tenor.googleapis.com/v2";
    private static final String CONTENT_FILTER = "high";          // strictest content rating
    private static final String MEDIA_FILTER = "tinygif,gif,nanogif";

    @Nullable
    private final String mApiKey;
    @NonNull
    private final String mClientKey;

    public TenorGifProvider(@Nullable String apiKey, @NonNull String clientKey,
                            @NonNull Executor executor, @NonNull Handler mainHandler) {
        super(executor, mainHandler);
        mApiKey = apiKey;
        mClientKey = clientKey;
    }

    @NonNull
    @Override
    public String getDisplayName() {
        return "Tenor";
    }

    @Override
    public boolean isConfigured() {
        return mApiKey != null && !mApiKey.isEmpty();
    }

    @NonNull
    @Override
    protected String buildUrl(boolean trending, @Nullable String query, int limit, @Nullable String position) {
        Uri.Builder builder = Uri.parse(BASE_URL + (trending ? "/featured" : "/search")).buildUpon();
        builder.appendQueryParameter("key", mApiKey == null ? "" : mApiKey);
        builder.appendQueryParameter("client_key", mClientKey);
        builder.appendQueryParameter("limit", Integer.toString(limit));
        builder.appendQueryParameter("media_filter", MEDIA_FILTER);
        builder.appendQueryParameter("contentfilter", CONTENT_FILTER);
        if (position != null && !position.isEmpty()) {
            builder.appendQueryParameter("pos", position);
        }
        if (!trending && query != null) {
            builder.appendQueryParameter("q", query);
        }
        return builder.build().toString();
    }

    @Override
    protected void parse(@NonNull JSONObject root, int requestLimit, @Nullable String requestPosition,
                         @NonNull List<GifItem> out, @NonNull String[] nextHolder) throws JSONException {
        JSONArray results = root.optJSONArray("results");
        if (results != null) {
            for (int i = 0; i < results.length(); i++) {
                GifItem item = parseResult(results.optJSONObject(i));
                if (item != null) {
                    out.add(item);
                }
            }
        }
        String next = root.optString("next", "");
        // Tenor returns "" or "0" when there is no further page.
        nextHolder[0] = (next.isEmpty() || "0".equals(next)) ? null : next;
    }

    @Nullable
    private GifItem parseResult(@Nullable JSONObject result) {
        if (result == null) {
            return null;
        }
        JSONObject formats = result.optJSONObject("media_formats");
        if (formats == null) {
            return null;
        }
        JSONObject preview = firstNonNull(formats.optJSONObject("tinygif"),
                formats.optJSONObject("nanogif"), formats.optJSONObject("gif"));
        JSONObject content = firstNonNull(formats.optJSONObject("gif"), formats.optJSONObject("tinygif"));
        if (preview == null || content == null) {
            return null;
        }
        String previewUrl = preview.optString("url", "");
        String contentUrl = content.optString("url", "");
        if (previewUrl.isEmpty() || contentUrl.isEmpty()) {
            return null;
        }
        int width = 0;
        int height = 0;
        JSONArray dims = preview.optJSONArray("dims");
        if (dims != null && dims.length() == 2) {
            width = dims.optInt(0, 0);
            height = dims.optInt(1, 0);
        }
        String id = result.optString("id", contentUrl);
        String desc = result.has("content_description") ? result.optString("content_description", null) : null;
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
