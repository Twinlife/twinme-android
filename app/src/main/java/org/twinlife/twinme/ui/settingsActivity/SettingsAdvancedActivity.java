/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.settingsActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ConnectivityService;
import org.twinlife.twinlife.PeerConnectionService;
import org.twinlife.twinlife.ProxyDescriptor;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.Settings;
import org.twinlife.twinme.ui.privacyActivity.UITimeout;
import org.twinlife.twinme.utils.AppStateInfo;
import org.twinlife.twinme.utils.UIAppInfo;

import java.util.List;

public class SettingsAdvancedActivity extends AbstractTwinmeActivity {
    private static final String LOG_TAG = "SettingsAdvanced...";
    private static final boolean DEBUG = false;

    private SettingsAdvancedAdapter mSettingsAdvancedAdapter;

    private Handler mHandler = new Handler(Looper.getMainLooper());

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

    @Override
    protected void onPause() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        super.onPause();

        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    protected void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        super.onResume();

        List<ProxyDescriptor> userProxies = getTwinmeContext().getConnectivityService().getUserProxies();
        mSettingsAdvancedAdapter.updateProxies(userProxies);

        if (userProxies.isEmpty() && getTwinmeContext().getConnectivityService().isUserProxyEnabled()) {
            getTwinmeContext().getConnectivityService().setUserProxyEnabled(false);
        }

        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        } else {
            mHandler = new Handler(Looper.getMainLooper());
        }

        mHandler.postDelayed(this::updateConnexionStatus, 5000);
    }

    public void onAddProxyClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onAddProxyClick");
        }

        if (getTwinmeContext().getConnectivityService().getUserProxies().size() >= ConnectivityService.MAX_PROXIES) {
            showAlertMessageView(R.id.settings_advanced_activity_layout, getString(R.string.deleted_account_view_warning), String.format(getString(R.string.proxy_view_limit), ConnectivityService.MAX_PROXIES), false, null);
            return;
        }
        startActivity(AddProxyActivity.class);
    }

    public void onDevelopersSettingsClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDevelopersSettingsClick");
        }

        startActivity(DebugSettingsActivity.class);
    }

    public void onSecurityLevelClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSecurityLevelClick");
        }

        openMenuSecurityLevel();
    }

    public void onSettingsClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSettingsClick");
        }

        showAlertMessageView(R.id.settings_advanced_activity_layout, getString(R.string.settings_advanced_view_security_title), getString(R.string.settings_advanced_view_security_unauthorized_update), false, null);
    }

    public void onProxySettingChangeValue(boolean value) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onProxySettingChangeValue");
        }

        getTwinmeContext().getConnectivityService().setUserProxyEnabled(value);
    }

    public void onTelecomSettingChangeValue(boolean value) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onTelecomSettingChangeValue");
        }

        getTwinmeApplication().setUseTelecom(value);
    }

    public void onSettingChangeValue(@NonNull UISetting<Boolean> setting, boolean value) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSettingChangeValue: setting=" + setting + " value=" + value);
        }

        if (setting.getTypeSetting() == UISetting.TypeSetting.CHECKBOX) {
            setting.setBoolean(value);
        }
    }

    public boolean isTelecomEnable() {
        if (DEBUG) {
            Log.d(LOG_TAG, "isTelecomEnable");
        }

        return getTwinmeApplication().isTelecomEnable();
    }

    public boolean isProxyEnable() {
        if (DEBUG) {
            Log.d(LOG_TAG, "isProxyEnable");
        }

        return getTwinmeContext().getConnectivityService().isUserProxyEnabled();
    }

    public UIAppInfo getAppInfo() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getAppInfo");
        }

        AppStateInfo appInfo = getTwinmeApplication().appInfo();
        if (appInfo != null && appInfo.getType() != null) {
            UIAppInfo uiAppInfo = new UIAppInfo(getApplicationContext(), appInfo.getType());
            if (getTwinmeContext().getConnectivityService().getProxyDescriptor() != null) {
                ProxyDescriptor proxyDescriptor = getTwinmeContext().getConnectivityService().getProxyDescriptor();
                if (proxyDescriptor.isUserProxy()) {
                    uiAppInfo.setProxy(proxyDescriptor.getAddress());
                }
            }
            return uiAppInfo;
        }

        return null;
    }

    public void onProxyClick(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onProxyClick position=" + position);
        }

        Intent intent = new Intent();
        intent.putExtra(Intents.INTENT_PROXY, position);
        startActivity(ProxyActivity.class, intent);
    }

    //
    // Private methods
    //

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        setContentView(R.layout.settings_advanced_activity);

        setStatusBarColor();
        setToolBar(R.id.settings_advanced_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);
        setTitle(getString(R.string.settings_advanced_view_title));
        setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        applyInsets(R.id.settings_advanced_activity_layout, R.id.settings_advanced_activity_tool_bar, R.id.settings_advanced_activity_list_view, Design.TOOLBAR_COLOR, false);

        mSettingsAdvancedAdapter = new SettingsAdvancedAdapter(this, getTwinmeContext().getConnectivityService().getUserProxies());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        RecyclerView settingsRecyclerView = findViewById(R.id.settings_advanced_activity_list_view);
        settingsRecyclerView.setLayoutManager(linearLayoutManager);
        settingsRecyclerView.setAdapter(mSettingsAdvancedAdapter);
        settingsRecyclerView.setItemAnimator(null);
        settingsRecyclerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

    }

    private void updateConnexionStatus() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateConnexionStatus");
        }

        mSettingsAdvancedAdapter.updateConnexionStatus();
        mHandler.postDelayed(this::updateConnexionStatus, 5000);
    }

    private void openMenuSecurityLevel() {
        if (DEBUG) {
            Log.d(LOG_TAG, "openMenuSecurityLevel");
        }

        ViewGroup viewGroup = findViewById(R.id.settings_advanced_activity_layout);

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

                if (value == PeerConnectionService.IceTransportMode.ALL.toInteger()) {
                    getTwinmeApplication().setIceTransportMode(PeerConnectionService.IceTransportMode.ALL);
                } else if (value == PeerConnectionService.IceTransportMode.RELAY.toInteger()) {
                    getTwinmeApplication().setIceTransportMode(PeerConnectionService.IceTransportMode.RELAY);
                    Settings.visualizationLink.setBoolean(false).save();
                    Settings.visualizationMap.setBoolean(false).save();
                } else {
                    getTwinmeApplication().setIceTransportMode(PeerConnectionService.IceTransportMode.TURNS);
                }

                getTwinmeContext().getPeerConnectionService().setIceTransportMode(getTwinmeApplication().getIceTransportMode());
                mSettingsAdvancedAdapter.updateSecurityLevel();
            }

            @Override
            public void onSelectTimeout(UITimeout timeout) {

            }
        });

        viewGroup.addView(menuSelectValueView);
        menuSelectValueView.openMenu(MenuSelectValueView.MenuType.SECURITY_LEVEL, getTwinmeApplication().getIceTransportMode().toInteger());

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }
}
