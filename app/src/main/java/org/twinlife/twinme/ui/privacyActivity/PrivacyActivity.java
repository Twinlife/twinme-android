/*
 *  Copyright (c) 2021-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.privacyActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.graphics.ColorUtils;
import androidx.percentlayout.widget.PercentRelativeLayout;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.premiumServicesActivity.PremiumFeatureConfirmView;
import org.twinlife.twinme.ui.premiumServicesActivity.PremiumServicesActivity;
import org.twinlife.twinme.ui.premiumServicesActivity.UIPremiumFeature;
import org.twinlife.twinme.utils.AbstractConfirmView;
import org.twinlife.twinme.utils.SwitchView;

public class PrivacyActivity extends AbstractTwinmeActivity {
    private static final String LOG_TAG = "PrivacyActivity";
    private static final boolean DEBUG = false;

    private static final float DESIGN_TEXT_INFORMATION_TOP_MARGIN = 17f;
    private static final float DESIGN_SECTION_TOP_MARGIN = 80f;
    private static int SECTION_VIEW_HEIGHT;
    private static int TEXT_INFORMATION_TOP_MARGIN;
    private static int SECTION_TOP_MARGIN;

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

    //
    // Private methods
    //

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.privacy_activity);

        View contentView = findViewById(R.id.privacy_activity_content_view);
        contentView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        setStatusBarColor();
        setToolBar(R.id.privacy_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);

        setTitle(getString(R.string.privacy_activity_title));
        applyInsets(R.id.privacy_activity_content_view, R.id.privacy_activity_tool_bar,R.id.privacy_activity_screen_container_view, Design.TOOLBAR_COLOR, false);

        View containerView = findViewById(R.id.privacy_activity_screen_container_view);
        containerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        View screenLockView = findViewById(R.id.privacy_activity_screen_lock_view);
        ViewGroup.LayoutParams sectionLayoutParams = screenLockView.getLayoutParams();
        sectionLayoutParams.height = SECTION_VIEW_HEIGHT;
        screenLockView.setLayoutParams(sectionLayoutParams);
        screenLockView.setOnClickListener(view -> onPremiumFeatureClick());

        SwitchView screenLockSwitchView = findViewById(R.id.privacy_activity_screen_lock_checkbox);
        Design.updateTextFont(screenLockSwitchView, Design.FONT_REGULAR34);
        screenLockSwitchView.setEnabled(false);
        screenLockSwitchView.setChecked(false);
        screenLockSwitchView.setClickable(false);
        screenLockSwitchView.setTextColor(Design.FONT_COLOR_DEFAULT);

        TextView screenLockInformationView = findViewById(R.id.privacy_activity_screen_lock_information_text_view);
        Design.updateTextFont(screenLockInformationView, Design.FONT_REGULAR28);
        screenLockInformationView.setTextColor(Design.FONT_COLOR_GREY);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) screenLockInformationView.getLayoutParams();
        marginLayoutParams.topMargin = TEXT_INFORMATION_TOP_MARGIN;
        marginLayoutParams.bottomMargin = SECTION_TOP_MARGIN;

        View hideLastScreenView = findViewById(R.id.privacy_activity_hide_last_screen_view);
        sectionLayoutParams = hideLastScreenView.getLayoutParams();
        sectionLayoutParams.height = SECTION_VIEW_HEIGHT;
        hideLastScreenView.setLayoutParams(sectionLayoutParams);
        hideLastScreenView.setOnClickListener(view -> onPremiumFeatureClick());

        SwitchView hideLastScreenSwitchView = findViewById(R.id.privacy_activity_hide_last_screen_checkbox);
        Design.updateTextFont(hideLastScreenSwitchView, Design.FONT_REGULAR34);
        hideLastScreenSwitchView.setEnabled(false);
        hideLastScreenSwitchView.setChecked(false);
        hideLastScreenSwitchView.setClickable(false);
        hideLastScreenSwitchView.setTextColor(Design.FONT_COLOR_DEFAULT);

        TextView hideLastScreenInformationView = findViewById(R.id.privacy_activity_hide_last_screen_information_text_view);
        Design.updateTextFont(hideLastScreenInformationView, Design.FONT_REGULAR28);
        hideLastScreenInformationView.setTextColor(Design.FONT_COLOR_GREY);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) hideLastScreenInformationView.getLayoutParams();
        marginLayoutParams.topMargin = TEXT_INFORMATION_TOP_MARGIN;
    }

    private void onPremiumFeatureClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPremiumFeatureClick");
        }

        PercentRelativeLayout percentRelativeLayout = findViewById(R.id.privacy_activity_content_view);

        PremiumFeatureConfirmView premiumFeatureConfirmView = new PremiumFeatureConfirmView(this, null);
        PercentRelativeLayout.LayoutParams layoutParams = new PercentRelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        premiumFeatureConfirmView.setLayoutParams(layoutParams);
        premiumFeatureConfirmView.initWithPremiumFeature(new UIPremiumFeature(this, UIPremiumFeature.FeatureType.PRIVACY));

        AbstractConfirmView.Observer observer = new AbstractConfirmView.Observer() {
            @Override
            public void onConfirmClick() {
                premiumFeatureConfirmView.redirectStore();
            }

            @Override
            public void onCancelClick() {
                premiumFeatureConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onDismissClick() {
                premiumFeatureConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                percentRelativeLayout.removeView(premiumFeatureConfirmView);
                setStatusBarColor();
            }
        };
        premiumFeatureConfirmView.setObserver(observer);

        percentRelativeLayout.addView(premiumFeatureConfirmView);
        premiumFeatureConfirmView.show();

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }

    @Override
    public void setupDesign() {
        if (DEBUG) {
            Log.d(LOG_TAG, "setupDesign");
        }

        SECTION_VIEW_HEIGHT = Design.SECTION_HEIGHT;
        TEXT_INFORMATION_TOP_MARGIN = (int) (DESIGN_TEXT_INFORMATION_TOP_MARGIN * Design.HEIGHT_RATIO);
        SECTION_TOP_MARGIN = (int) (DESIGN_SECTION_TOP_MARGIN * Design.HEIGHT_RATIO);
    }
}
