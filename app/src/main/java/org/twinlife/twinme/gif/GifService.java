/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  GifService: single entry point for the GIF feature.
 *
 *  - Reads the Tenor / Giphy API keys from string resources
 *    (R.string.tenor_api_key / R.string.giphy_api_key). Either or both may be
 *    set; the picker uses whichever providers are configured.
 *  - Exposes the configured providers and the active one (persisted).
 *  - Downloads the chosen GIF to the cache dir, ready to be sent.
 *  - Keeps a small list of recently sent GIFs (in SharedPreferences).
 */

package org.twinlife.twinme.gif;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.twinlife.device.android.twinme.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GifService {

    private static final String PREFS_NAME = "gif_service";
    private static final String KEY_ACTIVE_PROVIDER = "active_provider";
    private static final String KEY_RECENTS = "recents";
    private static final int MAX_RECENTS = 24;
    private static final String CLIENT_KEY = "twinme-android";

    public interface DownloadCallback {
        void onDownloaded(@NonNull File file);

        void onError(@NonNull Exception error);
    }

    @SuppressWarnings("StaticFieldLeak")
    private static volatile GifService sInstance;

    @NonNull
    private final Context mAppContext;
    @NonNull
    private final ExecutorService mExecutor;
    @NonNull
    private final Handler mMainHandler;
    @NonNull
    private final List<GifProvider> mProviders;
    @NonNull
    private final SharedPreferences mPrefs;
    @Nullable
    private GifProvider mActiveProvider;

    @NonNull
    public static GifService getInstance(@NonNull Context context) {
        if (sInstance == null) {
            synchronized (GifService.class) {
                if (sInstance == null) {
                    sInstance = new GifService(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    private GifService(@NonNull Context appContext) {
        mAppContext = appContext;
        mExecutor = Executors.newFixedThreadPool(2);
        mMainHandler = new Handler(Looper.getMainLooper());
        mPrefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        mProviders = new ArrayList<>();

        String tenorKey = trim(appContext.getString(R.string.tenor_api_key));
        String giphyKey = trim(appContext.getString(R.string.giphy_api_key));
        // Tenor is listed first so it is the default when both keys are present.
        if (!tenorKey.isEmpty()) {
            mProviders.add(new TenorGifProvider(tenorKey, CLIENT_KEY, mExecutor, mMainHandler));
        }
        if (!giphyKey.isEmpty()) {
            mProviders.add(new GiphyGifProvider(giphyKey, mExecutor, mMainHandler));
        }
        restoreActiveProvider();
    }

    @NonNull
    public List<GifProvider> getAvailableProviders() {
        return new ArrayList<>(mProviders);
    }

    public boolean isConfigured() {
        return !mProviders.isEmpty();
    }

    @Nullable
    public GifProvider getActiveProvider() {
        return mActiveProvider;
    }

    public void setActiveProvider(@NonNull GifProvider provider) {
        mActiveProvider = provider;
        mPrefs.edit().putString(KEY_ACTIVE_PROVIDER, provider.getDisplayName()).apply();
    }

    private void restoreActiveProvider() {
        String stored = mPrefs.getString(KEY_ACTIVE_PROVIDER, null);
        if (stored != null) {
            for (GifProvider provider : mProviders) {
                if (provider.getDisplayName().equals(stored)) {
                    mActiveProvider = provider;
                    break;
                }
            }
        }
        if (mActiveProvider == null && !mProviders.isEmpty()) {
            mActiveProvider = mProviders.get(0);
        }
    }

    public void download(@NonNull GifItem item, @NonNull DownloadCallback callback) {
        mExecutor.execute(() -> {
            HttpURLConnection connection = null;
            try {
                File directory = new File(mAppContext.getCacheDir(), "gifs");
                if (!directory.exists() && !directory.mkdirs()) {
                    throw new IOException("Could not create cache directory");
                }
                File destination = new File(directory, "gif_" + UUID.randomUUID() + ".gif");
                connection = (HttpURLConnection) new URL(item.getContentUrl()).openConnection();
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(20000);
                int code = connection.getResponseCode();
                if (code < 200 || code >= 400) {
                    throw new IOException("HTTP " + code + " while downloading GIF");
                }
                try (InputStream input = connection.getInputStream();
                     OutputStream output = new FileOutputStream(destination)) {
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = input.read(buffer)) != -1) {
                        output.write(buffer, 0, read);
                    }
                }
                mMainHandler.post(() -> callback.onDownloaded(destination));
            } catch (Exception error) {
                mMainHandler.post(() -> callback.onError(error));
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    @NonNull
    public List<GifItem> getRecentGifs() {
        List<GifItem> recents = new ArrayList<>();
        String stored = mPrefs.getString(KEY_RECENTS, null);
        if (stored != null) {
            try {
                JSONArray array = new JSONArray(stored);
                for (int i = 0; i < array.length(); i++) {
                    GifItem item = GifItem.fromJson(array.getJSONObject(i));
                    if (item != null) {
                        recents.add(item);
                    }
                }
            } catch (JSONException ignored) {
            }
        }
        return recents;
    }

    public void addRecentGif(@NonNull GifItem item) {
        List<GifItem> recents = getRecentGifs();
        recents.remove(item); // de-duplicate (GifItem.equals uses the content URL)
        recents.add(0, item);
        while (recents.size() > MAX_RECENTS) {
            recents.remove(recents.size() - 1);
        }
        JSONArray array = new JSONArray();
        for (GifItem recent : recents) {
            try {
                array.put(recent.toJson());
            } catch (JSONException ignored) {
            }
        }
        mPrefs.edit().putString(KEY_RECENTS, array.toString()).apply();
    }

    @NonNull
    private static String trim(@Nullable String value) {
        return value == null ? "" : value.trim();
    }
}
