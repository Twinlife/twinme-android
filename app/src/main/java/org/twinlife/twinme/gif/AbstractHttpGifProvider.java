/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Shared HTTP/threading plumbing for GIF providers: runs the request on a
 *  background Executor, performs a simple HTTPS GET, parses the JSON body in
 *  the subclass, and delivers the result on the main thread.
 */

package org.twinlife.twinme.gif;

import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

abstract class AbstractHttpGifProvider implements GifProvider {

    private static final int CONNECT_TIMEOUT_MS = 15000;
    private static final int READ_TIMEOUT_MS = 20000;

    @NonNull
    private final Executor mExecutor;
    @NonNull
    private final Handler mMainHandler;

    AbstractHttpGifProvider(@NonNull Executor executor, @NonNull Handler mainHandler) {
        mExecutor = executor;
        mMainHandler = mainHandler;
    }

    /** Build the fully-formed request URL for a trending or search query. */
    @NonNull
    protected abstract String buildUrl(boolean trending, @Nullable String query, int limit, @Nullable String position);

    /**
     * Parse the JSON response. Append GifItems to {@code out} and put the next
     * pagination cursor (or null when finished) into {@code nextHolder[0]}.
     */
    protected abstract void parse(@NonNull JSONObject root, int requestLimit, @Nullable String requestPosition,
                                  @NonNull List<GifItem> out, @NonNull String[] nextHolder) throws JSONException;

    @Override
    public void fetchTrending(int limit, @Nullable String position, @NonNull Callback callback) {
        request(true, null, limit, position, callback);
    }

    @Override
    public void search(@NonNull String query, int limit, @Nullable String position, @NonNull Callback callback) {
        request(false, query, limit, position, callback);
    }

    private void request(boolean trending, @Nullable String query, int limit,
                         @Nullable String position, @NonNull Callback callback) {
        if (!isConfigured()) {
            mMainHandler.post(() -> callback.onError(new IllegalStateException("Missing API key for " + getDisplayName())));
            return;
        }
        mExecutor.execute(() -> {
            try {
                String url = buildUrl(trending, query, limit, position);
                String body = httpGet(url);
                JSONObject root = new JSONObject(body);
                List<GifItem> items = new ArrayList<>();
                String[] next = new String[1];
                parse(root, limit, position, items, next);
                mMainHandler.post(() -> callback.onResult(items, next[0]));
            } catch (Exception error) {
                mMainHandler.post(() -> callback.onError(error));
            }
        });
    }

    @NonNull
    private static String httpGet(@NonNull String urlString) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
        try {
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setRequestProperty("Accept", "application/json");
            int code = connection.getResponseCode();
            InputStream stream = (code >= 200 && code < 400) ? connection.getInputStream() : connection.getErrorStream();
            String body = readAll(stream);
            if (code < 200 || code >= 400) {
                throw new IOException("HTTP " + code + ": " + body);
            }
            return body;
        } finally {
            connection.disconnect();
        }
    }

    @NonNull
    private static String readAll(@Nullable InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            char[] buffer = new char[4096];
            int read;
            while ((read = reader.read(buffer)) != -1) {
                builder.append(buffer, 0, read);
            }
        }
        return builder.toString();
    }
}
