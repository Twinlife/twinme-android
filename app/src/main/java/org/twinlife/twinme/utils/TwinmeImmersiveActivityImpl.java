/*
 *  Copyright (c) 2013-2017 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 */

package org.twinlife.twinme.utils;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

@SuppressLint("Registered")
public class TwinmeImmersiveActivityImpl extends TwinmeActivityImpl {
    private static final String LOG_TAG = "TwinmeImmersiveActiv...";
    private static final boolean DEBUG = false;

    private class OnSystemUiVisibilityChangeListener implements View.OnSystemUiVisibilityChangeListener {

        @Override
        public void onSystemUiVisibilityChange(int visibility) {
            if (DEBUG) {
                Log.d(LOG_TAG, "OnSystemUiVisibilityChangeListener.onSystemUiVisibilityChange visibility=" + visibility);
            }

            TwinmeImmersiveActivityImpl.this.onSystemUiVisibilityChange(visibility);
        }
    }

    //
    // Override Activity methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        setSystemUIVisibility(false);
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new OnSystemUiVisibilityChangeListener());
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onWindowFocusChanged: hasFocus=" + hasFocus);
        }

        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            setSystemUIVisibility(false);
        }
    }

    @SuppressWarnings("SameParameterValue")
    @SuppressLint("InlinedApi")
    private void setSystemUIVisibility(boolean visible) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setSystemUIVisibility visible=" + visible);
        }

        int visibility = getWindow().getDecorView().getSystemUiVisibility();
        if (visible == ((visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0)) {
            return;
        }

        if (visible) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    private void onSystemUiVisibilityChange(int visibility) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSystemUiVisibilityChange: visibility=" + visibility);
        }

        setSystemUIVisibility(false);
    }
}
