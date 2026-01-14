/*
 *  Copyright (c) 2021-2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.privacyActivity;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.Settings;
import org.twinlife.twinme.ui.settingsActivity.AbstractSettingsActivity;
import org.twinlife.twinme.ui.settingsActivity.MenuSelectValueView;
import org.twinlife.twinme.ui.settingsActivity.UISetting;

public class PrivacyActivity extends AbstractSettingsActivity {
    private static final String LOG_TAG = "PrivacyActivity";
    private static final boolean DEBUG = false;


    private PrivacyAdapter mPrivacyAdapter;

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
    public void onSettingClick(Settings.IntConfig intConfig) {

        if (intConfig == Settings.privacyScreenLockTimeout) {
            onScreenLockTimeoutClick();
        }
    }

    @Override
    public void onRingToneClick(UISetting<String> setting) {

    }

    @Override
    public void onSettingChangeValue(Settings.BooleanConfig booleanConfig, boolean value) {

        if (booleanConfig == Settings.privacyHideLastScreen) {
            getTwinmeApplication().setLastScreenHidden(value);
        } else if (booleanConfig == Settings.privacyActivityScreenLock) {
            getTwinmeApplication().setScreenLocked(value);
        }

        mPrivacyAdapter.updateSettings();
    }

    public boolean isDeviceSecure() {
        if (DEBUG) {
            Log.d(LOG_TAG, "isDeviceSecure");
        }

        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if (keyguardManager != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                return keyguardManager.isDeviceSecure();
            }
            return keyguardManager.isKeyguardSecure();
        }

        return false;
    }

    public void onDeviceSecureMessage() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeviceSecureMessage");
        }

        showAlertMessageView(R.id.privacy_activity_layout, getString(R.string.deleted_account_activity_warning), getString(R.string.lock_screen_activity_passcode_not_set), false, null);
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

        mPrivacyAdapter = new PrivacyAdapter(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        RecyclerView settingsRecyclerView = findViewById(R.id.privacy_activity_list_view);
        settingsRecyclerView.setLayoutManager(linearLayoutManager);
        settingsRecyclerView.setAdapter(mPrivacyAdapter);
        settingsRecyclerView.setItemAnimator(null);
        settingsRecyclerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
    }

    private void onScreenLockTimeoutClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onScreenLockTimeoutClick");
        }

        ViewGroup viewGroup = findViewById(R.id.privacy_activity_layout);

        MenuSelectValueView menuSelectValueView = new MenuSelectValueView(this, null);
        menuSelectValueView.setActivity(this);

        menuSelectValueView.setObserver(new MenuSelectValueView.Observer() {
            @Override
            public void onCloseMenuAnimationEnd() {
                viewGroup.removeView(menuSelectValueView);
                setStatusBarColor();
            }

            @Override
            public void onSelectValue(int value) {

                menuSelectValueView.animationCloseMenu();
            }

            @Override
            public void onSelectTimeout(UITimeout timeout) {
                if (DEBUG) {
                    Log.d(LOG_TAG, "onSelectTimeout: " + timeout);
                }

                menuSelectValueView.animationCloseMenu();
                getTwinmeApplication().updateScreenLockTimeout(timeout.getDelay());
                mPrivacyAdapter.updateTimeout();
            }
        });

        viewGroup.addView(menuSelectValueView);
        menuSelectValueView.openMenu(MenuSelectValueView.MenuType.LOCKSCREEN);

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }
}
