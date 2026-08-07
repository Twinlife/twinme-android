/*
 *  Copyright (c) 2013-2020 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class WebViewActivity extends AbstractTwinmeActivity {
    private static final String LOG_TAG = "WebViewActivity";
    private static final boolean DEBUG = false;

    public static final String INTENT_WEB_VIEW_ACTIVITY_URL = "org.twinlife.device.android.twinlife.Url";
    public static final String INTENT_WEB_VIEW_RESOURCE_ID = "org.twinlife.device.android.twinlife.ResourceId";

    private String mTitle;
    private WebView mWebView;

    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String value = intent.getStringExtra(Intents.INTENT_TITLE);
        if (value != null) {
            mTitle = value;
        }

        initViews();
    }

    @Override
    protected void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        super.onResume();
    }

    @Override
    public void onApplyInsetsFinish() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onApplyInsetsFinish");
        }

        super.onApplyInsetsFinish();

        if (mWebView != null) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mWebView.getLayoutParams();
            marginLayoutParams.bottomMargin = getBarBottomInset();
        }
    }

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.web_view_activity);

        setStatusBarColor();
        setToolBar(R.id.web_view_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);
        setBackgroundColor(Design.WHITE_COLOR);
        applyInsets(R.id.web_view_activity_content_view, R.id.web_view_activity_tool_bar, R.id.web_view_activity_background_view, Design.TOOLBAR_COLOR, false);

        if (mTitle != null) {
            setTitle(mTitle);
        } else {
            setTitle(getString(R.string.application_name));
        }

        View backgroundView = findViewById(R.id.web_view_activity_background_view);
        backgroundView.setBackgroundColor(Design.WHITE_COLOR);

        View contentView = findViewById(R.id.web_view_activity_content_view);
        contentView.setBackgroundColor(Design.WHITE_COLOR);

        mWebView = findViewById(R.id.web_view_activity_web_view);

        // Chromium crash on Android 5.0 (SDK 21) .. Android 7.1 (SDK 25) and a workarround to set the opacity at 0.99%
        // probably fix the crash.  Apply this strange hack only for targets that crash.
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
            mWebView.setAlpha(0.99f);
        }
        mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        mWebView.setBackgroundColor(Color.WHITE);
        mWebView.setWebViewClient(new WebViewClient());

        int resourceId = getIntent().getIntExtra(INTENT_WEB_VIEW_RESOURCE_ID, 0);
        String url = "";
        if (resourceId == R.raw.terms_of_service) {
            url = "file:///android_res/raw/terms_of_service.html";
        } else if (resourceId == R.raw.privacy_policy) {
            url = "file:///android_res/raw/privacy_policy.html";
        } else if (resourceId == R.raw.opensource) {
            url = "file:///android_res/raw/opensource.html";
        } else if (resourceId == R.raw.licenses) {
            url = "file:///android_res/raw/licenses.html";
        } else if (resourceId == R.raw.help) {
            url = "file:///android_res/raw/help.html";
        }

        if (!url.isEmpty()) {
            mWebView.loadUrl(url);
        } else {
            finish();
        }
    }
}
