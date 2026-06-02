/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.utils.faq;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.twinlife.device.android.twinme.BuildConfig;
import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.util.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FAQImpl implements Serializable, FAQ {
    private static final String LOG_TAG = "FAQImpl";
    private static final boolean DEBUG = false;

    private static final String SAVE_NAME = "faq.json";

    private static final String TITLE_KEY = "title";
    private static final String ARTICLES_KEY = "articles";
    private static final String ID_KEY = "id";
    private static final String DESCRIPTION_KEY = "description";
    private static final String TAGS_KEY = "tags";
    private static final String IMAGE_KEY = "images_dark";
    private static final String VIDEO_KEY = "changes";
    private static final String MIN_VERSION_KEY = "min_version";
    private static final String MAX_VERSION_KEY = "max_version";

    @Nullable
    private String mJsonString;

    public FAQImpl() {

    }

    @Override
    public List<UIFAQCategory> getCategories(Context context) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getCategories");
        }

        String jsonToParse = mJsonString;

        if (jsonToParse == null) {
            jsonToParse = loadFromResources(context);
        }

        if (jsonToParse == null) {
            return new ArrayList<>();
        }

        List<UIFAQCategory> parsed = parseJSON(jsonToParse);
        return parsed != null ? parsed : new ArrayList<>();
    }

    @Override
    public void setJson(String json) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setJson: json=" + json);
        }

        mJsonString = json;
    }

    @NonNull
    public static FAQ load(@NonNull Context context) {
        if (DEBUG) {
            Log.d(LOG_TAG, "load");
        }

        FAQ faq;
        File file = new File(context.getCacheDir(), SAVE_NAME);
        try (FileInputStream fis = new FileInputStream(file)) {
            try (ObjectInputStream is = new ObjectInputStream(fis)) {
                faq = (FAQ) is.readObject();
            }
        } catch (Exception exception) {
            faq = new FAQImpl();
            Utils.deleteFile("", file);
        }
        return faq;
    }

    @Override
    public void save(@NonNull File cacheDir) {
        if (DEBUG) {
            Log.d(LOG_TAG, "save");
        }

        File file = new File(cacheDir, SAVE_NAME);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(this);
        } catch (Exception ignored) {
        }
    }

    private String loadFromResources(Context context) {
        if (DEBUG) {
            Log.d(LOG_TAG, "loadFromResources");
        }

        try (InputStream inputStream = context.getResources().openRawResource(R.raw.faq)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            return stringBuilder.toString();
        } catch (IOException e) {
            return null;
        }
    }

    private List<UIFAQCategory> parseJSON(String jsonString) {
        if (DEBUG) {
            Log.d(LOG_TAG, "parseJSON: jsonString=" + jsonString);
        }

        List<UIFAQCategory> uiFAQCategories = new ArrayList<>();
        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(jsonString);

            for (int indexCategory = 0; indexCategory < jsonArray.length(); indexCategory++) {
                JSONObject jsonObject = jsonArray.getJSONObject(indexCategory);

                String title = null;
                List<UIFAQArticle> articles = new ArrayList<>();

                if (jsonObject.has(TITLE_KEY)) {
                    title = jsonObject.getString(TITLE_KEY);
                }

                if (jsonObject.has(ARTICLES_KEY)) {
                    JSONArray articlesArray = jsonObject.getJSONArray(ARTICLES_KEY);
                    for (int indexArticle = 0; indexArticle < articlesArray.length(); indexArticle++) {
                        JSONObject articleObject = articlesArray.getJSONObject(indexArticle);

                        int idArticle = -1;
                        String question = null;
                        String answer = null;
                        String image = null;
                        String video = null;
                        String minVersion = null;
                        String maxVersion = null;
                        List<String> tags = new ArrayList<>();

                        if (articleObject.has(MIN_VERSION_KEY)) {
                            minVersion = articleObject.getString(MIN_VERSION_KEY);
                        }

                        if (articleObject.has(MAX_VERSION_KEY)) {
                            maxVersion = articleObject.getString(MAX_VERSION_KEY);
                        }

                        String currentVersion = BuildConfig.VERSION_NAME;

                        if (minVersion != null && !minVersion.isEmpty() && compareVersions(currentVersion, minVersion) == -1) {
                            continue;
                        }

                        if (maxVersion != null && !maxVersion.isEmpty() && compareVersions(currentVersion, maxVersion) == 1) {
                            continue;
                        }

                        if (articleObject.has(ID_KEY)) {
                            idArticle = articleObject.getInt(ID_KEY);
                        }

                        if (articleObject.has(TITLE_KEY)) {
                            question = articleObject.getString(TITLE_KEY);
                        }

                        if (articleObject.has(DESCRIPTION_KEY)) {
                            answer = articleObject.getString(DESCRIPTION_KEY);
                        }

                        if (articleObject.has(IMAGE_KEY)) {
                            image = articleObject.getString(IMAGE_KEY);
                        }

                        if (articleObject.has(VIDEO_KEY)) {
                            video = articleObject.getString(VIDEO_KEY);
                        }

                        if (articleObject.has(TAGS_KEY)) {
                            JSONArray tagsArray = articleObject.getJSONArray(TAGS_KEY);
                            for (int i = 0; i < tagsArray.length(); i++) {
                                tags.add(tagsArray.getString(i));
                            }
                        }

                        if (question != null && answer != null) {
                            UIFAQArticle uiFAQArticle = new UIFAQArticle(idArticle, question, answer, image, video, tags);
                            articles.add(uiFAQArticle);
                        }
                    }
                }

                if (title != null && !articles.isEmpty()) {
                    UIFAQCategory uiFAQCategory = new UIFAQCategory(title, articles);
                    uiFAQCategories.add(uiFAQCategory);
                }
            }

            return uiFAQCategories;

        } catch (JSONException e) {
            return null;
        }
    }

    private int compareVersions(String version1, String version2) {
        if (DEBUG) {
            Log.d(LOG_TAG, "compareVersions version1=" + version1 + " version2=" + version2);
        }

        String[] versionNumbers1 = version1.split("\\.");
        String[] versionNumbers2 = version2.split("\\.");

        int length = Math.max(versionNumbers1.length, versionNumbers2.length);

        for (int i = 0; i < length; i++) {
            int v1 = i < versionNumbers1.length ? Integer.parseInt(versionNumbers1[i]) : 0;
            int v2 = i < versionNumbers2.length ? Integer.parseInt(versionNumbers2[i]) : 0;

            if (v1 < v2) {
                return -1;
            } else if (v1 > v2) {
                return 1;
            }
        }

        return 0;
    }
}

