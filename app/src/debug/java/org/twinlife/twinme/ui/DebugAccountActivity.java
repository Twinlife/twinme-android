/*
 *  Copyright (c) 2012-2019 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Zhuoyu Ma (Zhuoyu.Ma@twinlife-systems.com)
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Denis Campredon (Denis.Campredon@twinlife-systems.com)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.ui.accountActivity.AccountActivity;

public class DebugAccountActivity extends AccountActivity {
    private static final String LOG_TAG = "DebugSettingsActivity";
    private static final boolean DEBUG = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);
        initViews();
    }

    @Override
    public void onRestart() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRestart");
        }

        super.onRestart();
        initViews();
    }

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        final Activity mActivity = this;
        final View debugLinkView = findViewById(R.id.account_activity_debug_title_view);
        debugLinkView.setOnClickListener(v -> {
            Intent intent = new Intent(mActivity, DebugActivity.class);
            mActivity.startActivity(intent);
        });

        final View logLinkView = findViewById(R.id.account_activity_logs_title_view);
        logLinkView.setOnClickListener(v -> {
            Intent intent = new Intent(mActivity, LogActivity.class);
            mActivity.startActivity(intent);
        });

        final View testLinkView = findViewById(R.id.account_activity_tests_title_view);
        testLinkView.setOnClickListener(v -> {
            Intent intent = new Intent(mActivity, TestActivity.class);
            mActivity.startActivity(intent);
        });

        final View configLinkView = findViewById(R.id.account_activity_config_title_view);
        configLinkView.setOnClickListener(v -> {
            Intent intent = new Intent(mActivity, DebugConfigActivity.class);
            mActivity.startActivity(intent);
        });
    }
}
