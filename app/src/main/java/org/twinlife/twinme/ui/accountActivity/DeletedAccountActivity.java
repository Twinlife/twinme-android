/*
 *  Copyright (c) 2018-2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.accountActivity;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
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

        Window window = getWindow();
        window.setNavigationBarColor(Design.WHITE_COLOR);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Design.WHITE_COLOR);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }

        getWindow().getDecorView().setBackgroundColor(Design.WHITE_COLOR);

        ImageView twinmeView = findViewById(R.id.deleted_account_activity_twinme_view);
        twinmeView.setColorFilter(Design.BLACK_COLOR);

        TextView messageView = findViewById(R.id.deleted_account_activity_message_view);
        Design.updateTextFont(messageView, Design.FONT_REGULAR36);
        messageView.setTextColor(Design.FONT_COLOR_DEFAULT);
        messageView.setText(Html.fromHtml(getString(R.string.deleted_account_activity_message)));
    }
}
