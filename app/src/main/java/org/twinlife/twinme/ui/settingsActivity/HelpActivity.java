/*
 *  Copyright (c) 2021-2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.settingsActivity;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.skin.DisplayMode;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.FeedbackActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.Settings;
import org.twinlife.twinme.ui.WebViewActivity;
import org.twinlife.twinme.ui.externalCallActivity.OnboardingExternalCallActivity;
import org.twinlife.twinme.ui.premiumServicesActivity.PremiumServicesActivity;
import org.twinlife.twinme.ui.spaces.OnboardingSpaceActivity;
import org.twinlife.twinme.ui.welcomeActivity.WelcomeHelpActivity;
import org.twinlife.twinme.utils.AbstractBottomSheetView;
import org.twinlife.twinme.utils.OnboardingConfirmView;

public class HelpActivity extends AbstractTwinmeActivity {
    private static final String LOG_TAG = "HelpActivity";
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

    public void onSubSectionClick(UIHelpSubSection.HelpSubSectionType type) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSubSectionClick");
        }

        switch (type) {
            case GETTING_STARTED:
                onHelpClick();
                break;

            case FAQ:
                onFAQClick();
                break;

            case BLOG:
                onBlogClick();
                break;

            case FEEDBACK:
                onFeedbackClick();
                break;

            case WELCOME:
                onWelcomeClick();
                break;

            case QUALITY_OF_SERVICES:
                onQualityOfServiceClick();
                break;

            case ADDITIONAL_FUNCTIONS:
                onPremiumServicesClick();
                break;

            case SPACES:
                onSpacesClick();
                break;

            case PROFILE:
                onProfileClick();
                break;

            case CLICK_TO_CALL:
                onClickToCallClick();
                break;

            case CERTIFIED_RELATION:
                onCertifiedRelationClick();
                break;

            case ACCOUNT_TRANSFER:
                onTransferClick();
                break;

            case BACKUP:
                onBackupClick();
                break;

            case PROXY:
                onProxyClick();
                break;

            default:
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
        setContentView(R.layout.help_activity);

        setStatusBarColor();
        setToolBar(R.id.help_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);

        setTitle(getString(R.string.navigation_view_help));
        setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        applyInsets(R.id.help_activity_layout, R.id.help_activity_tool_bar, R.id.help_activity_list_view, Design.TOOLBAR_COLOR, false);

        HelpAdapter helpAdapter = new HelpAdapter(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        RecyclerView settingsRecyclerView = findViewById(R.id.help_activity_list_view);
        settingsRecyclerView.setLayoutManager(linearLayoutManager);
        settingsRecyclerView.setAdapter(helpAdapter);
        settingsRecyclerView.setItemAnimator(null);
        settingsRecyclerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
    }

    private void onHelpClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onHelpClick");
        }

        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra(WebViewActivity.INTENT_WEB_VIEW_RESOURCE_ID, R.raw.help);
        intent.putExtra(Intents.INTENT_TITLE, getString(R.string.navigation_view_help));
        startActivity(intent);
    }

    private void onFAQClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onFAQClick");
        }

        startActivity(FAQActivity.class);
    }

    private void onBlogClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBlogClick");
        }

        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(this, Uri.parse(getString(R.string.twinme_blog)));
    }

    private void onWelcomeClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onWelcomeClick");
        }

        Intent intent = new Intent(this, WelcomeHelpActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private void onQualityOfServiceClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onQualityOfServiceClick");
        }

        Intent intent = new Intent(this, QualityOfServiceActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private void onPremiumServicesClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPremiumServicesClick");
        }

        Intent intent = new Intent(this, PremiumServicesActivity.class);
        intent.putExtra(Intents.INTENT_FROM_SIDE_MENU, true);
        startActivity(intent);
    }

    private void onSpacesClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSpacesClick");
        }

        Intent intent = new Intent(this, OnboardingSpaceActivity.class);
        intent.putExtra(Intents.INTENT_FROM_SIDE_MENU, true);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private void onProfileClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onProfileClick");
        }

        String message = getString(R.string.create_profile_view_onboarding_message_part_1) +
                "\n\n" +
                getString(R.string.create_profile_view_onboarding_message_part_2) +
                "\n\n" +
                getString(R.string.create_profile_view_onboarding_message_part_3) +
                "\n\n" +
                getString(R.string.create_profile_view_onboarding_message_part_4);

        showOnboardingView(getString(R.string.application_profile), message, isDarkMode() ? R.drawable.onboarding_add_profile_dark : R.drawable.onboarding_add_profile);
    }

    private void onClickToCallClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onClickToCallClick");
        }

        Intent intent = new Intent(this, OnboardingExternalCallActivity.class);
        intent.putExtra(Intents.INTENT_FROM_SIDE_MENU, true);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private void onTransferClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onTransferClick");
        }

        showOnboardingView(getString(R.string.account_view_migration_title), getString(R.string.account_view_migration_message), isDarkMode() ? R.drawable.onboarding_migration_dark : R.drawable.onboarding_migration);
    }

    private void onCertifiedRelationClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCertifiedRelationClick");
        }

        showOnboardingView(getString(R.string.authentified_relation_view_to_be_certified_title), getString(R.string.authentified_relation_view_onboarding_message), isDarkMode() ? R.drawable.onboarding_authentified_relation_dark : R.drawable.onboarding_authentified_relation);
    }

    private void onFeedbackClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onFeedbackClick");
        }

        startActivity(FeedbackActivity.class);
    }

    private void onProxyClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onProxyClick");
        }

        showOnboardingView(getString(R.string.proxy_view_title), getString(R.string.proxy_view_onboarding), R.drawable.onboarding_proxy);
    }

    private void onBackupClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBackupClick");
        }

        String message = getString(R.string.backup_view_beta_message_part_1) +
                "\n\n" +
                getString(R.string.backup_view_beta_message_part_2) +
                "\n\n" +
                getString(R.string.backup_view_beta_message_part_3);

        showOnboardingView(getString(R.string.account_view_backup_restore), message, R.drawable.onboarding_backup);
    }

    private void showOnboardingView(String title, String message, int image) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onProfileClick");
        }

        ViewGroup viewGroup = findViewById(R.id.help_activity_layout);

        OnboardingConfirmView onboardingConfirmView = new OnboardingConfirmView(this, null);
        onboardingConfirmView.setTitle(title);
        onboardingConfirmView.setImage(ResourcesCompat.getDrawable(getResources(),image, null));
        onboardingConfirmView.setMessage(message);
        onboardingConfirmView.setConfirmTitle(getString(R.string.application_ok));
        onboardingConfirmView.hideCancelView();

        AbstractBottomSheetView.Observer observer = new AbstractBottomSheetView.Observer() {
            @Override
            public void onConfirmClick() {
                onboardingConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCancelClick() {
                onboardingConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onDismissClick() {
                onboardingConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                viewGroup.removeView(onboardingConfirmView);
                setStatusBarColor();
            }
        };
        onboardingConfirmView.setObserver(observer);
        viewGroup.addView(onboardingConfirmView);
        onboardingConfirmView.show();

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }

    private boolean isDarkMode() {
        if (DEBUG) {
            Log.d(LOG_TAG, "isDarkMode");
        }

        boolean darkMode = false;
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        int displayMode = Settings.displayMode.getInt();
        if ((currentNightMode == Configuration.UI_MODE_NIGHT_YES && displayMode == DisplayMode.SYSTEM.ordinal())  || displayMode == DisplayMode.DARK.ordinal()) {
            darkMode = true;
        }

        return darkMode;
    }
}
