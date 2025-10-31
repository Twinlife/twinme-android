/*
 *  Copyright (c) 2018-2019 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 *   Romain Kolb (romain.kolb@skyrock.com)
 */

package org.twinlife.twinme.ui;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.twinlife.twinlife.debug.DebugTwinlifeImpl;
import org.twinlife.twinme.FeatureUtils;

import java.util.ArrayList;
import java.util.List;

import leakcanary.LeakCanary;
import shark.AndroidReferenceMatchers;
import shark.ReferenceMatcher;

/**
 * The Debug Twinme application.
 * <p>
 * This class is used to be able to install the DebugService factory before Twinlife is started.
 */
public class TwinmeDebugApplicationImpl extends TwinmeApplicationImpl {
    private static final String LOG_TAG = "TwinmeDebugApplication";
    private static final boolean DEBUG = false;

    private static final String USE_WEBRTC_AEC = "useWebRtcAEC";
    private static final String USE_WEBRTC_NOISE_SUPPRESSOR = "useWebRtcNoiseSuppressor";
    private static final String BLACK_LIST_FOR_OPENSLES = "blacklistDeviceForOpenSLESUsage";
    //
    // Override TwinmeApplicationImpl methods
    //

    @Override
    public void onCreate() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate");
        }

        // Install the DebugService factory.
        DebugTwinlifeImpl.initialize();

        super.onCreate();

        // Configure WebRTC on startup (we can do this before the WebRTC initialisation).
        //WebRtcAudioUtils.setWebRtcBasedAcousticEchoCanceler(getUseWebRtcAEC());
        //WebRtcAudioUtils.setWebRtcBasedNoiseSuppressor(getUseWebRtcNoiseSuppressor());
        //WebRtcAudioManager.setBlacklistDeviceForOpenSLESUsage(getBlacklistDeviceForOpenSLESUsage());

        List<ReferenceMatcher> referenceMatchers = new ArrayList<>(AndroidReferenceMatchers.getAppDefaults());
        referenceMatchers.add(AndroidReferenceMatchers.nativeGlobalVariableLeak(
                "android.app.KeyguardManager$3",
                "Known issue with Keyguard: https://issuetracker.google.com/issues/111158463",
                androidBuildMirror -> true
        ));
        referenceMatchers.add(AndroidReferenceMatchers.nativeGlobalVariableLeak(
                "android.telecom.ConnectionService$1",
                "Internal issue, we clean up all our own references in TelecomConnectionService.onDestroy()",
                androidBuildMirror -> FeatureUtils.isTelecomSupported(this)
        ));

        LeakCanary.Config config = LeakCanary.getConfig().newBuilder()
                .retainedVisibleThreshold(10) // Give native code (WebRTC) more time to release its references.
                .referenceMatchers(referenceMatchers) //Mark known 3rd-party issues as Library Leaks
                .build();

        LeakCanary.setConfig(config);

    }

    public boolean getUseWebRtcAEC() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        return sharedPreferences.getBoolean(USE_WEBRTC_AEC, false /* WebRtcAudioUtils.useWebRtcBasedAcousticEchoCanceler()*/);
    }

    public boolean getUseWebRtcNoiseSuppressor() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        return sharedPreferences.getBoolean(USE_WEBRTC_NOISE_SUPPRESSOR, false /*WebRtcAudioUtils.useWebRtcBasedNoiseSuppressor()*/);
    }

    public boolean getBlacklistDeviceForOpenSLESUsage() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        return sharedPreferences.getBoolean(BLACK_LIST_FOR_OPENSLES, false);
    }

    public void setUseWebRtcAEC(boolean value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(USE_WEBRTC_AEC, value);
        editor.apply();
        //WebRtcAudioUtils.setWebRtcBasedAcousticEchoCanceler(value);
    }

    public void setUseWebRtcNoiseSuppressor(boolean value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(USE_WEBRTC_NOISE_SUPPRESSOR, value);
        editor.apply();
        //WebRtcAudioUtils.setWebRtcBasedNoiseSuppressor(value);
    }

    public void setBlacklistDeviceForOpenSLESUsage(boolean value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(BLACK_LIST_FOR_OPENSLES, value);
        editor.apply();
        //WebRtcAudioManager.setBlacklistDeviceForOpenSLESUsage(value);
    }

}
