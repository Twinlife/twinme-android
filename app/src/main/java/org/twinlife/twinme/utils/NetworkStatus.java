/*
 *  Copyright (c) 2018-2020 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.util.Log;

import org.twinlife.device.android.twinme.R;

import java.util.ArrayList;
import java.util.List;

public class NetworkStatus {
    private static final String LOG_TAG = "NetworkStatus";
    private static final boolean DEBUG = false;

    private static final int OFFLINE_ERROR_DELAY = 0; // 0s, immediate error
    private static final int CONNECTED_ERROR_DELAY = 10; // 10s
    private static final int CONNECTING_ERROR_DELAY = 15; // 15s, give more time for the network to scan and setup.

    private NetworkInfo.State mMobileNetworkStatus;
    private NetworkInfo.State mWifiNetworkStatus;
    private int mPersistDelay;
    private int mMessage;
    private int mResolution;
    private long mDeadline;

    public NetworkStatus() {

    }

    /**
     * Check if we are trying to connect to at least one network.
     *
     * @return True if we are trying to connect to a network.
     */
    private boolean isConnecting() {

        return mMobileNetworkStatus == NetworkInfo.State.CONNECTING || mWifiNetworkStatus == NetworkInfo.State.CONNECTING;
    }

    /**
     * Check if we are connected to at least one network.
     *
     * @return True if we are connected to at least one network.
     */
    private boolean isConnected() {

        return mMobileNetworkStatus == NetworkInfo.State.CONNECTED || mWifiNetworkStatus == NetworkInfo.State.CONNECTED;
    }

    /**
     * Get the main diagnostic message.
     *
     * @return the network diagnostic message.
     */
    public int getMessage() {
        return mMessage;
    }

    /**
     * Get the possible resolution advice to the network connectivity issue.
     *
     * @return the resolution message.
     */
    public int getResolution() {

        return mResolution;
    }

    /**
     * Get the delay in seconds after which we can consider there is a persistent network issue.
     *
     * @return the delay in seconds.
     */
    public int getPersistentDelay() {

        return mPersistDelay;
    }

    /**
     * Get the deadline after which we can report an alert.
     *
     * @return the deadline.
     */
    public long getProbeDeadline() {

        return mDeadline;
    }

    /**
     * Get the network diagnostic messages to explain why there is no network connectivity.
     */
    public void getNetworkDiagnostic(Context context) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getNetworkDiagnostic");
        }

        boolean isAirplane = isAirplaneOn(context);
        if (isAirplane) {
            mMobileNetworkStatus = NetworkInfo.State.DISCONNECTED;
            mWifiNetworkStatus = NetworkInfo.State.DISCONNECTED;
            // "Airplane mode is activated."
            mMessage = R.string.application_network_status_airplane_message;
            mResolution = R.string.application_network_status_airplane_resolution;
            mPersistDelay = OFFLINE_ERROR_DELAY;
            return;
        }
        boolean isWifiEnabled = isWifiEnabled(context);

        // Get the available networks.
        final List<NetworkInfo> result = getNetworks(context);

        // Analyze what networks are available and look at their state.
        mMobileNetworkStatus = NetworkInfo.State.UNKNOWN;
        mWifiNetworkStatus = NetworkInfo.State.UNKNOWN;
        for (NetworkInfo net : result) {
            switch (net.getType()) {
                case ConnectivityManager.TYPE_MOBILE:
                    mMobileNetworkStatus = net.getState();
                    break;

                case ConnectivityManager.TYPE_WIFI:
                case ConnectivityManager.TYPE_ETHERNET:
                    mWifiNetworkStatus = net.getState();
                    break;

                default:
                    // Other network types are not meaningful because they concern VPNs, Bluetooth
                    break;
            }
        }
        if (isConnected()) {
            mPersistDelay = CONNECTED_ERROR_DELAY;
            // "The network is connected but the Internet connectivity is not available.";
            mMessage = R.string.application_network_status_connected_no_internet;
            mResolution = R.string.application_network_status_connected_resolution;

        } else if (isConnecting()) {
            mPersistDelay = CONNECTING_ERROR_DELAY;
            mMessage = R.string.application_network_status_connection_timeout;
            mResolution = 0;

        } else {
            mPersistDelay = OFFLINE_ERROR_DELAY;
            mResolution = 0;
            if (!isWifiEnabled) {
                // mResolution = "Please, turn on the Wi-Fi to get an Internet connection.";
                if (mMobileNetworkStatus == NetworkInfo.State.UNKNOWN) {
                    // mMessage = "Wi-Fi is disabled.";
                    mMessage = R.string.application_network_status_wifi_disabled_no_mobile;
                    mResolution = R.string.application_network_status_wifi_disabled_no_mobile_resolution;
                } else {
                    // mMessage = "Wi-Fi is disabled and there is no mobile Internet connection";
                    mMessage = R.string.application_network_status_wifi_disabled;
                    mResolution = R.string.application_network_status_wifi_disabled_resolution;
                }
            } else {
                // mMessage = "There is no Internet connection";
                mMessage = R.string.application_network_status_no_internet;
            }
        }
        if (mDeadline == 0) {
            mDeadline = System.currentTimeMillis() + mPersistDelay * 1000L;
        }
    }

    private static boolean isWifiEnabled(final Context context) {
        if (DEBUG) {
            Log.d(LOG_TAG, "isWifiEnabled");
        }

        final WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return wifiManager != null && wifiManager.isWifiEnabled();
    }

    /**
     * Get a network diagnostic message to explain why there is no network connectivity.
     *
     * @return the network diagnostic message.
     */
    private static List<NetworkInfo> getNetworks(Context context) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getNetworkDiagnostic");
        }

        // Get the available networks.
        final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final List<NetworkInfo> result = new ArrayList<>();

        if (connectivityManager != null) {
            final Network[] networks = connectivityManager.getAllNetworks();
            for (Network net : networks) {
                final NetworkInfo network = connectivityManager.getNetworkInfo(net);

                if (network != null) {
                    result.add(network);
                }
            }
        }
        return result;
    }

    private static boolean isAirplaneOn(final Context context) {
        if (DEBUG) {
            Log.d(LOG_TAG, "isAirplaneOn");
        }

        return Settings.Global.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
    }
}
