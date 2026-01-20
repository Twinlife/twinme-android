/*
 *  Copyright (c) 2021-2026 twinlife SA.
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

import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.premiumServicesActivity.PremiumFeatureConfirmView;
import org.twinlife.twinme.ui.premiumServicesActivity.UIPremiumFeature;
import org.twinlife.twinme.ui.settingsActivity.AbstractSettingsActivity;
import org.twinlife.twinme.ui.settingsActivity.UISetting;
import org.twinlife.twinme.utils.AbstractBottomSheetView;

public class PrivacyActivity extends AbstractSettingsActivity {
    private static final String LOG_TAG = "PrivacyActivity";
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
    public void onSettingClick(UISetting<?> setting) {

    }

    @Override
    public void onRingToneClick(UISetting<String> setting) {

    }

    public void onPremiumFeatureClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPremiumFeatureClick");
        }

        ViewGroup viewGroup = findViewById(R.id.privacy_activity_layout);

        PremiumFeatureConfirmView premiumFeatureConfirmView = new PremiumFeatureConfirmView(this, null);
        premiumFeatureConfirmView.initWithPremiumFeature(new UIPremiumFeature(this, UIPremiumFeature.FeatureType.PRIVACY));

        AbstractBottomSheetView.Observer observer = new AbstractBottomSheetView.Observer() {
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
                viewGroup.removeView(premiumFeatureConfirmView);
                setStatusBarColor();
            }
        };
        premiumFeatureConfirmView.setObserver(observer);
        viewGroup.addView(premiumFeatureConfirmView);
        premiumFeatureConfirmView.show();

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
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

        View contentView = findViewById(R.id.privacy_activity_layout);
        contentView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        setStatusBarColor();
        setToolBar(R.id.privacy_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);

        setTitle(getString(R.string.privacy_activity_title));
        applyInsets(R.id.privacy_activity_layout, R.id.privacy_activity_tool_bar,R.id.privacy_activity_list_view, Design.TOOLBAR_COLOR, false);

        PrivacyAdapter privacyAdapter = new PrivacyAdapter(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        RecyclerView settingsRecyclerView = findViewById(R.id.privacy_activity_list_view);
        settingsRecyclerView.setLayoutManager(linearLayoutManager);
        settingsRecyclerView.setAdapter(privacyAdapter);
        settingsRecyclerView.setItemAnimator(null);
        settingsRecyclerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
    }
}
