/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.settingsActivity;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.AndroidDeviceInfo;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.Permission;
import org.twinlife.twinme.utils.AppStateInfo;
import org.twinlife.twinme.utils.NetworkStatus;

public class DiagnosticsActivity extends AbstractTwinmeActivity {
    private static final String LOG_TAG = "DiagnosticsActivity";
    private static final boolean DEBUG = false;

    private DiagnosticsAdapter mDiagnosticsAdapter;

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

        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        } else {
            mHandler = new Handler(Looper.getMainLooper());
        }

        updateItems();
    }

    //
    // Private methods
    //

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.diagnostics_activity);

        setStatusBarColor();
        setToolBar(R.id.diagnostics_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);

        setTitle(getString(R.string.diagnostics_view_title));
        setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        applyInsets(R.id.diagnostics_activity_layout, R.id.diagnostics_activity_tool_bar, R.id.diagnostics_activity_list_view, Design.TOOLBAR_COLOR, false);

        mDiagnosticsAdapter = new DiagnosticsAdapter(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        RecyclerView recyclerView = findViewById(R.id.diagnostics_activity_list_view);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(mDiagnosticsAdapter);
        recyclerView.setItemAnimator(null);
        recyclerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
    }

    private void updateItems() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateItems");
        }

        final AndroidDeviceInfo androidDeviceInfo = new AndroidDeviceInfo(this);

        boolean postNotificationEnable = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            postNotificationEnable = checkPermissionsWithoutRequest(new Permission[]{Permission.POST_NOTIFICATIONS});
        }

        boolean microphoneEnable = checkPermissionsWithoutRequest(new Permission[]{Permission.RECORD_AUDIO});
        boolean cameraEnable = checkPermissionsWithoutRequest(new Permission[]{Permission.CAMERA});
        boolean backgroundRestrictedEnable = androidDeviceInfo.isBackgroundRestricted();
        boolean hasPushNotification = getTwinmeContext().getManagementService().hasPushNotification();

        mDiagnosticsAdapter.updatePermissions(UIDiagnosticSubsection.DiagnosticSubSectionType.PERMISSION_NOTIFICATION, postNotificationEnable ? UIDiagnosticSubsection.DiagnosticState.OK : UIDiagnosticSubsection.DiagnosticState.KO);
        mDiagnosticsAdapter.updatePermissions(UIDiagnosticSubsection.DiagnosticSubSectionType.PERMISSION_MICRO, microphoneEnable ? UIDiagnosticSubsection.DiagnosticState.OK : UIDiagnosticSubsection.DiagnosticState.KO);
        mDiagnosticsAdapter.updatePermissions(UIDiagnosticSubsection.DiagnosticSubSectionType.PERMISSION_CAMERA, cameraEnable ? UIDiagnosticSubsection.DiagnosticState.OK : UIDiagnosticSubsection.DiagnosticState.KO);
        mDiagnosticsAdapter.updatePermissions(UIDiagnosticSubsection.DiagnosticSubSectionType.PERMISSION_BACKGROUND, backgroundRestrictedEnable ? UIDiagnosticSubsection.DiagnosticState.KO : UIDiagnosticSubsection.DiagnosticState.OK);
        mDiagnosticsAdapter.updatePushState(hasPushNotification ? UIDiagnosticSubsection.DiagnosticState.OK : UIDiagnosticSubsection.DiagnosticState.KO);

        String secureMethod = null;
        switch (getTwinmeContext().getConfigurationService().getSecuredMethod()) {
            case KEYSTORE_AES_GCM:
                secureMethod = "AES/GCM";
                break;

            case KEYSTORE_AES_CBC:
                secureMethod = "AES/CBC";
                break;

            case KEYSTORE_RSA_4096_AES_CBC:
                secureMethod = "RSA 4096 AES/CBC";
                break;

            case KEYSTORE_RSA_3072_AES_CBC:
                secureMethod = "RSA 3072 AES/CBC";
                break;

            case KEYSTORE_RSA_2048_AES_CBC:
                secureMethod = "RSA 2048 AES/CBC";
                break;

            case KEYSTORE_RSA_1024_AES_CBC:
                secureMethod = "RSA 1014 AES/CBC";
                break;
        }

        mDiagnosticsAdapter.updateKeystore(secureMethod);
        updateConnexionStatus();
    }

    private void updateConnexionStatus() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateConnexionStatus");
        }

        AppStateInfo appInfo = getTwinmeApplication().appInfo();
        if (appInfo != null && appInfo.getType() != null) {
            String title = "";
            String subtitle = null;
            UIDiagnosticSubsection.DiagnosticState state = UIDiagnosticSubsection.DiagnosticState.UNKNOWN;
            switch (appInfo.getType()) {
                case CONNECTED:
                    title = getString(R.string.application_connected);
                    state = UIDiagnosticSubsection.DiagnosticState.OK;
                    break;

                case CONNECTION_IN_PROGRESS:
                    title = getString(R.string.application_not_connected);
                    break;

                case NO_SERVICES:
                    title = getString(R.string.application_connection_status_no_services);
                    subtitle = getString(R.string.application_connection_status_no_services_message);
                    state = UIDiagnosticSubsection.DiagnosticState.KO;
                    break;

                case OFFLINE:
                    title = getString(R.string.application_connection_status_no_network);
                    subtitle = getString(R.string.application_connection_status_no_network_message);
                    state = UIDiagnosticSubsection.DiagnosticState.KO;
                    break;
            }

            mDiagnosticsAdapter.updateConnectionState(title, subtitle, state);
        }

        final NetworkStatus net = new NetworkStatus();
        net.startMonitoring(this, (connected, connecting) -> {

            if (connected) {
                String subtitle = "";
                if (net.isMobileConnected()) {
                    subtitle = getString(R.string.diagnostics_view_cellular_network);
                } else if (net.isWifiConnected()) {
                    subtitle = getString(R.string.diagnostics_view_wifi);
                }

                mDiagnosticsAdapter.updateConnectionState(getString(R.string.application_connected), subtitle, UIDiagnosticSubsection.DiagnosticState.OK);
            }


            net.stopMonitoring(this);
        });

        mHandler.postDelayed(this::updateConnexionStatus, 5000);
    }
}
