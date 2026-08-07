/*
 *  Copyright (c) 2021-2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.settingsActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.WebViewActivity;
import org.twinlife.twinme.ui.premiumServicesActivity.PremiumServicesActivity;
import org.twinlife.twinme.utils.WhatsNewDialog;
import org.twinlife.twinme.utils.update.LastVersion;

public class AboutActivity extends AbstractTwinmeActivity {
    private static final String LOG_TAG = "AboutActivity";
    private static final boolean DEBUG = false;

    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        initViews();
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        super.onDestroy();
    }

    protected void onAboutItemClick(UIAboutItem.AboutItemType aboutItemType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onAboutItemClick: " + aboutItemType);
        }

        switch (aboutItemType) {
            case TERMS_OF_USE:
                onTermsOfUseClick();
                break;

            case PRIVACY_POLICY:
                onPrivacyPolicyClick();
                break;

            case SOURCE_CODE:
                onApplicationCodeClick();
                break;

            case OPEN_SOURCE_LICENSES:
                onLicencesClick();
                break;

            case CURRENT_VERSION:
                onVersionClick();
                break;

            case UPDATE_AVAILABLE:
                onUpdateVersionClick();
                break;
        }
    }

    //
    // Private methods
    //

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.about_activity);

        setStatusBarColor();
        setToolBar(R.id.about_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);

        setTitle(getString(R.string.navigation_view_about_twinme));
        setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        applyInsets(R.id.about_activity_layout, R.id.about_activity_tool_bar, R.id.about_activity_list_view, Design.TOOLBAR_COLOR, false);

        AboutAdapter aboutAdapter = new AboutAdapter(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        RecyclerView recyclerView = findViewById(R.id.about_activity_list_view);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(aboutAdapter);
        recyclerView.setItemAnimator(null);
        recyclerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
    }

    private void onTermsOfUseClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onTermsOfUseClick");
        }

        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra(WebViewActivity.INTENT_WEB_VIEW_RESOURCE_ID, R.raw.terms_of_service);
        intent.putExtra(Intents.INTENT_TITLE, getString(R.string.about_view_terms_of_use));
        startActivity(intent);
    }

    private void onPrivacyPolicyClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPrivacyPolicyClick");
        }

        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra(WebViewActivity.INTENT_WEB_VIEW_RESOURCE_ID, R.raw.privacy_policy);
        intent.putExtra(Intents.INTENT_TITLE, getString(R.string.about_view_privacy_policy));
        startActivity(intent);
    }

    private void onApplicationCodeClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onApplicationCodeClick");
        }

        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra(WebViewActivity.INTENT_WEB_VIEW_RESOURCE_ID, R.raw.opensource);
        intent.putExtra(Intents.INTENT_TITLE, getString(R.string.about_view_application_code));
        startActivity(intent);
    }

    private void onLicencesClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onLicencesClick");
        }

        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra(WebViewActivity.INTENT_WEB_VIEW_RESOURCE_ID,  R.raw.licenses);
        intent.putExtra(Intents.INTENT_TITLE, getString(R.string.about_view_open_sources_licences));
        startActivity(intent);
    }

    private void onUpdateVersionClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateVersionClick");
        }

        if (getTwinmeApplication().getLastVersion() != null) {
            WhatsNewDialog whatsNewDialog = new WhatsNewDialog(this, true);
            DialogInterface.OnCancelListener dialogCancelListener = dialog -> {
            };
            DialogInterface.OnDismissListener dismissListener = dialogInterface -> setStatusBarColor();
            whatsNewDialog.setOnCancelListener(dialogCancelListener);
            whatsNewDialog.setOnDismissListener(dismissListener);
            whatsNewDialog.setup(getTwinmeApplication().getLastVersion(), () -> {
                PremiumServicesActivity.redirectStore(this);
                whatsNewDialog.dismiss();
            }, true);
            whatsNewDialog.show();

            Window window = getWindow();
            window.setNavigationBarColor(Design.POPUP_BACKGROUND_COLOR);

            int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
            setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
        }
    }

    protected void onVersionClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onVersionClick");
        }

        LastVersion version = getTwinmeApplication().getLastVersion();
        if (version != null && version.isCurrentVersion()) {
            WhatsNewDialog whatsNewDialog = new WhatsNewDialog(this, true);
            DialogInterface.OnCancelListener dialogCancelListener = dialog -> {
            };
            DialogInterface.OnDismissListener dismissListener = dialogInterface -> setStatusBarColor();
            whatsNewDialog.setOnCancelListener(dialogCancelListener);
            whatsNewDialog.setOnDismissListener(dismissListener);
            whatsNewDialog.setup(getTwinmeApplication().getLastVersion(), whatsNewDialog::dismiss, false);
            whatsNewDialog.show();

            Window window = getWindow();
            window.setNavigationBarColor(Design.POPUP_BACKGROUND_COLOR);

            int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
            setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
        }
    }
}
