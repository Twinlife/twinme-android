/*
 *  Copyright (c) 2019 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */
package org.twinlife.twinme.ui;

import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Switch;
import android.widget.TextView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

/**
 * Activity to configure some advanced application settings.
 */
public class DebugConfigActivity extends AbstractTwinmeActivity {
    private static final boolean DEBUG = true;
    private static final String LOG_TAG = "DebugConfigActivity";
    private TwinmeDebugApplicationImpl mTwinmeDebugApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTwinmeDebugApplication = (TwinmeDebugApplicationImpl) getApplication();
        initViews();
    }

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        setContentView(R.layout.debug_config_activity);

        TextView titleView = findViewById(R.id.debug_config_activity_title_view);
        Design.updateTextFont(titleView, Design.FONT_REGULAR44);
        titleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        setBackAction(R.id.debug_config_activity_back_label_view,
                R.id.debug_config_activity_back_clickable_view,
                v -> onBackClick());

        Switch enableAECView = findViewById(R.id.debug_config_enable_hardware_aec);
        //if (!WebRtcAudioEffects.isAcousticEchoCancelerSupported()) {
        //    enableAECView.setVisibility(GONE);
        //}
        enableAECView.setOnCheckedChangeListener((v, isChecked) -> configureAEC(isChecked));
        enableAECView.setTypeface(Design.FONT_MEDIUM32.typeface);
        enableAECView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_MEDIUM32.size);
        enableAECView.setChecked(!mTwinmeDebugApplication.getUseWebRtcAEC());

        Switch enableOpenSLESView = findViewById(R.id.debug_config_enable_opensl_es);
        enableOpenSLESView.setOnCheckedChangeListener((v, isChecked) -> configureOpenSLES(isChecked));
        enableOpenSLESView.setTypeface(Design.FONT_MEDIUM32.typeface);
        enableOpenSLESView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_MEDIUM32.size);
        enableOpenSLESView.setChecked(!mTwinmeDebugApplication.getBlacklistDeviceForOpenSLESUsage());

        Switch enableNoiseSuppressorView = findViewById(R.id.debug_config_enable_noise_suppressor);
        //if (!WebRtcAudioEffects.isNoiseSuppressorSupported()) {
        //    enableNoiseSuppressorView.setVisibility(GONE);
        //}
        enableNoiseSuppressorView.setOnCheckedChangeListener((v, isChecked) -> configureNoiseSuppressor(isChecked));
        enableNoiseSuppressorView.setTypeface(Design.FONT_MEDIUM32.typeface);
        enableNoiseSuppressorView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_MEDIUM32.size);
        enableNoiseSuppressorView.setChecked(!mTwinmeDebugApplication.getUseWebRtcNoiseSuppressor());
    }

    private void configureAEC(boolean value) {
        if (DEBUG) {
            Log.d(LOG_TAG, "configureAEC value=" + value);
        }

        // Enable software and disable hardware
        mTwinmeDebugApplication.setUseWebRtcAEC(!value);
    }

    private void configureOpenSLES(boolean value) {
        if (DEBUG) {
            Log.d(LOG_TAG, "configureOpenSLES value=" + value);
        }

        mTwinmeDebugApplication.setBlacklistDeviceForOpenSLESUsage(!value);
    }

    private void configureNoiseSuppressor(boolean value) {
        if (DEBUG) {
            Log.d(LOG_TAG, "configureNoiseSuppressor value=" + value);
        }

        // Enable software noise suppressor and disable hardware
        mTwinmeDebugApplication.setUseWebRtcNoiseSuppressor(!value);
    }
}
