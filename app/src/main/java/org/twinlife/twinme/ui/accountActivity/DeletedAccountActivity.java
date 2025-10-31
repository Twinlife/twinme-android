/*
 *  Copyright (c) 2018-2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.accountActivity;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.TwinmeApplication;

public class DeletedAccountActivity extends Activity {
    private static final String LOG_TAG = "DeletedAccountActivity";
    private static final boolean DEBUG = false;

    //
    // Override Activity methods
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
    protected void onPause() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPause");
        }

        super.onPause();

        // Stop the twinme service to stop the application.
        TwinmeApplication twinmeApplication = (TwinmeApplication) getApplication();
        twinmeApplication.stop();
    }

    //
    // Private methods
    //

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        setContentView(R.layout.deleted_account_activity);

        TextView messageView = findViewById(R.id.deleted_account_activity_message_view);
        Design.updateTextFont(messageView, Design.FONT_REGULAR36);
        messageView.setTextColor(Design.FONT_COLOR_DEFAULT);
        messageView.setText(Html.fromHtml(getString(R.string.deleted_account_activity_message)));
    }
}
