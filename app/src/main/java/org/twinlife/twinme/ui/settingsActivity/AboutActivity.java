/*
 *  Copyright (c) 2021-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.settingsActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.core.graphics.ColorUtils;

import org.twinlife.device.android.twinme.BuildConfig;
import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.WebViewActivity;
import org.twinlife.twinme.ui.premiumServicesActivity.PremiumServicesActivity;
import org.twinlife.twinme.utils.CommonUtils;
import org.twinlife.twinme.utils.WhatsNewDialog;
import org.twinlife.twinme.utils.update.LastVersion;

public class AboutActivity extends AbstractTwinmeActivity {
    private static final String LOG_TAG = "AboutActivity";
    private static final boolean DEBUG = false;

    private static final float DESIGN_UPDATE_WIDTH = 504f;
    private static final float DESIGN_UPDATE_HEIGHT = 100f;
    private static final float DESIGN_UPDATE_TOP = 30f;
    private static final float DESIGN_LEGAL_TITLE_TOP = 80f;
    private static final float DESIGN_LEGAL_TITLE_LEFT = 34f;
    private static final float DESIGN_LEGAL_TITLE_BOTTOM = 14f;
    private static final float DESIGN_INFO_TOP = 14f;
    private static final float DESIGN_INFO_BOTTOM = 60f;
    private static int SECTION_VIEW_HEIGHT;
    private static int LEGAL_TITLE_TOP;

    private static int INFO_TOP;
    private static int INFO_BOTTOM;
    private static int UPDATE_WIDTH;
    private static int UPDATE_HEIGHT;
    private static int UPDATE_TOP;
    private static int LEGAL_TITLE_BOTTOM;
    private static int LEGAL_TITLE_LEFT;

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

        setTitle(getString(R.string.navigation_activity_about_twinme));
        setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        applyInsets(R.id.about_activity_layout, R.id.about_activity_tool_bar, R.id.about_activity_container_view, Design.TOOLBAR_COLOR, false);

        ScrollView scrollView = findViewById(R.id.about_activity_scroll_view);
        scrollView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        View containerView = findViewById(R.id.about_activity_container_view);
        containerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        TextView messageTextView = findViewById(R.id.about_activity_message_view);
        Design.updateTextFont(messageTextView, Design.FONT_REGULAR34);
        messageTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        View versionView = findViewById(R.id.about_activity_version_view);
        versionView.setOnClickListener(view -> onVersionClick());
        ViewGroup.LayoutParams sectionLayoutParams = versionView.getLayoutParams();
        sectionLayoutParams.height = SECTION_VIEW_HEIGHT;

        TextView versionTitleView = findViewById(R.id.about_activity_version_title);
        Design.updateTextFont(versionTitleView, Design.FONT_REGULAR32);
        versionTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        TextView versionTextView = findViewById(R.id.about_activity_version_text);
        Design.updateTextFont(versionTextView, Design.FONT_REGULAR32);
        versionTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        versionTextView.setText(BuildConfig.VERSION_NAME);

        ImageView accessoryImageView = findViewById(R.id.about_activity_version_image);
        LastVersion version = getTwinmeApplication().getLastVersion();
        if (version != null && version.isCurrentVersion()) {
            accessoryImageView.setVisibility(View.VISIBLE);
        } else {
            accessoryImageView.setVisibility(View.INVISIBLE);
        }

        TextView legalTitleTextView = findViewById(R.id.about_activity_legal_title);
        Design.updateTextFont(legalTitleTextView, Design.FONT_BOLD26);
        legalTitleTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        View updateView = findViewById(R.id.about_activity_update_view);
        updateView.setOnClickListener(v -> onUpdateVersionClick());

        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable updateViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        updateViewBackground.getPaint().setColor(Design.getMainStyle());
        updateView.setBackground(updateViewBackground);

        ViewGroup.LayoutParams layoutParams = updateView.getLayoutParams();
        layoutParams.width = UPDATE_WIDTH;
        layoutParams.height = UPDATE_HEIGHT;

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) updateView.getLayoutParams();
        marginLayoutParams.topMargin = UPDATE_TOP;
        marginLayoutParams.bottomMargin = UPDATE_TOP;

        TextView updateVersionTextView = findViewById(R.id.about_activity_update_title_view);
        Design.updateTextFont(updateVersionTextView, Design.FONT_REGULAR34);
        updateVersionTextView.setTextColor(Color.WHITE);

        if (getTwinmeApplication().hasNewVersion()) {
            updateView.setVisibility(View.VISIBLE);
        } else {
            updateView.setVisibility(View.GONE);
        }

        marginLayoutParams = (ViewGroup.MarginLayoutParams) legalTitleTextView.getLayoutParams();
        marginLayoutParams.topMargin = LEGAL_TITLE_TOP;
        if (CommonUtils.isLayoutDirectionRTL()) {
            marginLayoutParams.rightMargin = LEGAL_TITLE_LEFT;
        } else {
            marginLayoutParams.leftMargin = LEGAL_TITLE_LEFT;
        }
        marginLayoutParams.bottomMargin = LEGAL_TITLE_BOTTOM;

        View termsOfUseView = findViewById(R.id.about_activity_terms_of_use_view);
        termsOfUseView.setOnClickListener(v -> onTermsOfUseClick());

        sectionLayoutParams = termsOfUseView.getLayoutParams();
        sectionLayoutParams.height = SECTION_VIEW_HEIGHT;

        TextView termsOfUseTextView = findViewById(R.id.about_activity_terms_of_use_text);
        Design.updateTextFont(termsOfUseTextView, Design.FONT_REGULAR32);
        termsOfUseTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        View privacyPolicyView = findViewById(R.id.about_activity_privacy_policy_view);
        privacyPolicyView.setOnClickListener(v -> onPrivacyPolicyClick());

        sectionLayoutParams = privacyPolicyView.getLayoutParams();
        sectionLayoutParams.height = SECTION_VIEW_HEIGHT;

        TextView privacyPolicyTextView = findViewById(R.id.about_activity_privacy_policy_text);
        Design.updateTextFont(privacyPolicyTextView, Design.FONT_REGULAR32);
        privacyPolicyTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        TextView openSourceTitleTextView = findViewById(R.id.about_activity_open_source_title);
        Design.updateTextFont(openSourceTitleTextView, Design.FONT_BOLD26);
        openSourceTitleTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) openSourceTitleTextView.getLayoutParams();
        marginLayoutParams.topMargin = LEGAL_TITLE_TOP;
        if (CommonUtils.isLayoutDirectionRTL()) {
            marginLayoutParams.rightMargin = LEGAL_TITLE_LEFT;
        } else {
            marginLayoutParams.leftMargin = LEGAL_TITLE_LEFT;
        }
        marginLayoutParams.bottomMargin = LEGAL_TITLE_BOTTOM;

        View applicationCodeView = findViewById(R.id.about_activity_application_code_view);
        applicationCodeView.setOnClickListener(v -> onApplicationCodeClick());

        sectionLayoutParams = applicationCodeView.getLayoutParams();
        sectionLayoutParams.height = SECTION_VIEW_HEIGHT;

        TextView applicationCodeTextView = findViewById(R.id.about_activity_application_code_text);
        Design.updateTextFont(applicationCodeTextView, Design.FONT_REGULAR32);
        applicationCodeTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        TextView applicationCodeInfoView = findViewById(R.id.about_activity_open_source_information);
        Design.updateTextFont(applicationCodeInfoView, Design.FONT_REGULAR28);
        applicationCodeInfoView.setTextColor(Design.FONT_COLOR_GREY);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) applicationCodeInfoView.getLayoutParams();
        marginLayoutParams.bottomMargin = INFO_BOTTOM;
        if (CommonUtils.isLayoutDirectionRTL()) {
            marginLayoutParams.rightMargin = LEGAL_TITLE_LEFT;
        } else {
            marginLayoutParams.leftMargin = LEGAL_TITLE_LEFT;
        }
        marginLayoutParams.topMargin = INFO_TOP;

        View licencesView = findViewById(R.id.about_activity_licences_view);
        licencesView.setOnClickListener(v -> onLicencesClick());

        sectionLayoutParams = licencesView.getLayoutParams();
        sectionLayoutParams.height = SECTION_VIEW_HEIGHT;

        TextView licencesTextView = findViewById(R.id.about_activity_licences_text);
        Design.updateTextFont(licencesTextView, Design.FONT_REGULAR32);
        licencesTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        TextView copyrightTextView = findViewById(R.id.about_activity_copyright_view);
        Design.updateTextFont(copyrightTextView, Design.FONT_REGULAR32);
        copyrightTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
    }

    private void onTermsOfUseClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onTermsOfUseClick");
        }

        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra(WebViewActivity.INTENT_WEB_VIEW_ACTIVITY_URL, "file:///android_res/raw/terms_of_service.html");
        intent.putExtra(Intents.INTENT_TITLE, getString(R.string.about_activity_terms_of_use));
        startActivity(intent);
    }

    private void onPrivacyPolicyClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPrivacyPolicyClick");
        }

        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra(WebViewActivity.INTENT_WEB_VIEW_ACTIVITY_URL, "file:///android_res/raw/privacy_policy.html");
        intent.putExtra(Intents.INTENT_TITLE, getString(R.string.about_activity_privacy_policy));
        startActivity(intent);
    }

    private void onApplicationCodeClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onApplicationCodeClick");
        }

        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra(WebViewActivity.INTENT_WEB_VIEW_ACTIVITY_URL, "file:///android_res/raw/opensource.html");
        intent.putExtra(Intents.INTENT_TITLE, getString(R.string.about_activity_application_code));
        startActivity(intent);
    }

    private void onLicencesClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onLicencesClick");
        }

        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra(WebViewActivity.INTENT_WEB_VIEW_ACTIVITY_URL, "file:///android_res/raw/licenses.html");
        intent.putExtra(Intents.INTENT_TITLE, getString(R.string.about_activity_open_sources_licences));
        startActivity(intent);
    }

    private void onUpdateVersionClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateVersionClick");
        }

        if (getTwinmeApplication().getLastVersion() != null) {
            WhatsNewDialog whatsNewDialog = new WhatsNewDialog(this, true);
            DialogInterface.OnCancelListener dialogCancelListener = dialog -> {};
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

    private void onVersionClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onVersionClick");
        }

        LastVersion version = getTwinmeApplication().getLastVersion();
        if (version != null && version.isCurrentVersion()) {
            WhatsNewDialog whatsNewDialog = new WhatsNewDialog(this, true);
            DialogInterface.OnCancelListener dialogCancelListener = dialog -> {};
            DialogInterface.OnDismissListener dismissListener = dialogInterface -> setStatusBarColor();
            whatsNewDialog.setOnCancelListener(dialogCancelListener);
            whatsNewDialog.setOnDismissListener(dismissListener);
            whatsNewDialog.setup(getTwinmeApplication().getLastVersion(), () -> {
                whatsNewDialog.dismiss();
            }, false);
            whatsNewDialog.show();

            Window window = getWindow();
            window.setNavigationBarColor(Design.POPUP_BACKGROUND_COLOR);

            int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
            setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
        }
    }

    @Override
    public void setupDesign() {
        if (DEBUG) {
            Log.d(LOG_TAG, "setupDesign");
        }

        SECTION_VIEW_HEIGHT = Design.SECTION_HEIGHT;
        UPDATE_WIDTH = (int) (DESIGN_UPDATE_WIDTH * Design.WIDTH_RATIO);
        UPDATE_HEIGHT = (int) (DESIGN_UPDATE_HEIGHT * Design.HEIGHT_RATIO);
        UPDATE_TOP = (int) (DESIGN_UPDATE_TOP * Design.HEIGHT_RATIO);
        LEGAL_TITLE_TOP = (int) (DESIGN_LEGAL_TITLE_TOP * Design.HEIGHT_RATIO);
        LEGAL_TITLE_BOTTOM = (int) (DESIGN_LEGAL_TITLE_BOTTOM * Design.HEIGHT_RATIO);
        LEGAL_TITLE_LEFT = (int) (DESIGN_LEGAL_TITLE_LEFT * Design.WIDTH_RATIO);
        INFO_TOP = (int) (DESIGN_INFO_TOP * Design.HEIGHT_RATIO);
        INFO_BOTTOM = (int) (DESIGN_INFO_BOTTOM * Design.HEIGHT_RATIO);
    }
}
