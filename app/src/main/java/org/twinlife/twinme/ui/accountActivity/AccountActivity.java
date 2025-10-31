/*
 *  Copyright (c) 2020-2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.accountActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.accountMigrationActivity.AccountMigrationScannerActivity;
import org.twinlife.twinme.ui.cleanupActivity.TypeCleanUpActivity;
import org.twinlife.twinme.ui.exportActivity.ExportActivity;

public class AccountActivity extends AbstractTwinmeActivity {
    private static final String LOG_TAG = "AccountActivity";
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

    //
    // Override Activity methods
    //

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        super.onResume();
    }


    public void onTransferClick(boolean fromCurrentDevice) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onTransferClick");
        }

        Intent intent = new Intent(this, AccountMigrationScannerActivity.class);
        intent.putExtra(Intents.INTENT_MIGRATION_FROM_CURRENT_DEVICE, fromCurrentDevice);
        startActivity(intent);
    }

    public void onDeleteAccountClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteAccountClick");
        }

        startActivity(DeleteAccountActivity.class);
    }

    public void onExportClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onExportClick");
        }

        startActivity(ExportActivity.class);
    }

    public void onCleanupClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCleanupClick");
        }

        startActivity(TypeCleanUpActivity.class);
    }

    //
    // Private methods
    //

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.account_activity);

        setStatusBarColor();
        setToolBar(R.id.account_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);
        setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        setTitle(getString(R.string.account_activity_title));

        applyInsets(R.id.account_activity_layout, R.id.account_activity_tool_bar, R.id.account_activity_list_view, Design.TOOLBAR_COLOR, false);

        AccountAdapter accountAdapter = new AccountAdapter(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        RecyclerView settingsRecyclerView = findViewById(R.id.account_activity_list_view);
        settingsRecyclerView.setLayoutManager(linearLayoutManager);
        settingsRecyclerView.setAdapter(accountAdapter);
        settingsRecyclerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        settingsRecyclerView.setItemAnimator(null);

        mProgressBarView = findViewById(R.id.account_activity_progress_bar);
    }
}
