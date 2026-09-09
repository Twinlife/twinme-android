/*
 *  Copyright (c) 2018-2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 *   Romain Kolb (romain.kolb@skyrock.com)
 */

package org.twinlife.twinme.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.twinlife.device.android.twinme.R;

public class NetworkStatus {
    private static final String LOG_TAG = "NetworkStatus";
    private static final boolean DEBUG = false;

    public interface NetworkStatusListener {
        void onNetworkStatusChanged(boolean connected, boolean connecting);
    }

    private boolean mIsWifiConnected = false;
    private boolean mIsMobileConnected = false;
    private boolean mIsConnecting = false;

    private int mMessage;
    private int mResolution;

    @Nullable
    private ConnectivityManager.NetworkCallback mNetworkCallback = null;

    public void startMonitoring(@NonNull Context context, @NonNull NetworkStatusListener listener) {
        if (DEBUG) {
            Log.d(LOG_TAG, "startMonitoring: context=" + context + " listener=" + listener);
        }

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return;

        NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

        mNetworkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                updateAndNotify(context, listener);
            }

            @Override
            public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities cap) {
                updateAndNotify(context, listener);
            }

            @Override
            public void onLost(@NonNull Network network) {
                updateAndNotify(context, listener);
            }
        };

        cm.registerNetworkCallback(request, mNetworkCallback);

        updateAndNotify(context, listener);
    }

    private void updateAndNotify(Context context, NetworkStatusListener listener) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateAndNotify: context=" + context + " listener=" + listener);
        }

        computeStatus(context);
        listener.onNetworkStatusChanged(isConnected(), mIsConnecting);
    }

    private void computeStatus(Context context) {
        if (DEBUG) {
            Log.d(LOG_TAG, "computeStatus: context=" + context);
        }

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        mIsWifiConnected = false;
        mIsMobileConnected = false;
        mIsConnecting = false;

        if (isAirplaneOn(context)) {
            mMessage = R.string.application_network_status_airplane_message;
            mResolution = R.string.application_network_status_airplane_resolution;
            return;
        }

        if (cm != null) {
            Network activeNetwork = cm.getActiveNetwork();
            NetworkCapabilities caps = cm.getNetworkCapabilities(activeNetwork);

            if (caps != null) {
                boolean validated = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);

                if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    if (validated) {
                        mIsWifiConnected = true;
                    } else {
                        mIsConnecting = true;
                    }
                } else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    if (validated) {
                        mIsMobileConnected = true;
                    } else {
                        mIsConnecting = true;
                    }
                }
            }
        }

        if (isConnected()) {
            mMessage = R.string.application_network_status_connected_no_internet;
            mResolution = R.string.application_network_status_connected_resolution;
        } else if (mIsConnecting) {
            mMessage = R.string.application_network_status_connection_timeout;
            mResolution = 0;
        } else {
            mResolution = 0;
            if (!isWifiEnabled(context)) {
                // Mobile is down or we don't have the info
                if (!mIsMobileConnected) {
                    mMessage = R.string.application_network_status_wifi_disabled_no_mobile;
                    mResolution = R.string.application_network_status_wifi_disabled_no_mobile_resolution;
                } else {
                    mMessage = R.string.application_network_status_wifi_disabled;
                    mResolution = R.string.application_network_status_wifi_disabled_resolution;
                }
            } else {
                mMessage = R.string.application_network_status_no_internet;
            }
        }
    }

    public void stopMonitoring(@NonNull Context context) {
        if (DEBUG) {
            Log.d(LOG_TAG, "stopMonitoring: context=" + context);
        }

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null && mNetworkCallback != null) {
            cm.unregisterNetworkCallback(mNetworkCallback);
            mNetworkCallback = null;
        }
    }

    public int getMessage() {
        return mMessage;
    }

    public int getResolution() {
        return mResolution;
    }

    public boolean isMonitoring() {
        return mNetworkCallback != null;
    }

    public boolean isWifiConnected() {

        return mIsWifiConnected;
    }

    public boolean isMobileConnected() {

        return mIsMobileConnected;
    }

    private boolean isConnected() {
        return mIsWifiConnected || mIsMobileConnected;
    }

    private boolean isWifiEnabled(Context context) {
        WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return wm != null && wm.isWifiEnabled();
    }

    private boolean isAirplaneOn(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
    }
}