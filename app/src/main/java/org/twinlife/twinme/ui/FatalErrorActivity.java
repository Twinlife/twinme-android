/*
 *  Copyright (c) 2018-2020 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.graphics.Color;
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
import org.twinlife.twinlife.BaseService;
import org.twinlife.twinlife.BaseService.ErrorCode;
import org.twinlife.twinme.skin.Design;

/**
 * Fatal error activity to display a specific screen when some fatal error occured during the startup.
 * <ul>
 *   <li>We must not use TwinmeActivity because the twinlife library is not initialized completely.</li>
 *   <li>We must not use TwinmeApplication instance because it may not exists</li>
 * </p>
 */

public class FatalErrorActivity extends Activity {
    private static final String LOG_TAG = "FatalErrorActivity";
    private static final boolean DEBUG = false;

    private static final int DESIGN_MESSAGE_COLOR = Color.rgb(52, 54, 55);

    private String mMessage;
    private boolean mCustomMessage = false;

    //
    // Override Activity methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        Application application = getApplication();

        Intent intent = getIntent();
        String value = intent.getStringExtra(Intents.INTENT_ERROR_ID);
        boolean databaseUpgraded = intent.getBooleanExtra(Intents.INTENT_DATABASE_UPGRADED, false);
        if (value != null) {
            BaseService.ErrorCode errorCode = ErrorCode.valueOf(value);
            if (errorCode == ErrorCode.NO_STORAGE_SPACE && !databaseUpgraded) {
                mCustomMessage = true;
                mMessage = application.getString(R.string.application_migration_no_storage_space);
            } else {
                mMessage = application.getString(TwinmeApplicationImpl.errorToMessageId(ErrorCode.valueOf(value)));

                if (mMessage.equals(application.getString(R.string.fatal_error_activity_error_code_message))) {
                    mCustomMessage = true;
                    mMessage = String.format(mMessage, errorCode.ordinal());
                }
            }
        } else {
            mCustomMessage = true;
            mMessage = String.format(application.getString(R.string.fatal_error_activity_error_code_message), ErrorCode.LIBRARY_ERROR.ordinal());
        }

        initViews();
    }

    @Override
    protected void onPause() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPause");
        }

        super.onPause();

        System.exit(2);
    }

    //
    // Private methods
    //

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        setContentView(R.layout.fatal_error_activity);

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

        ImageView twinmeView = findViewById(R.id.fatal_error_activity_twinme_view);
        twinmeView.setColorFilter(Design.BLACK_COLOR);

        // Message to introduce the error.
        TextView messageView = findViewById(R.id.fatal_error_activity_internal_error_message_view);

        // If the TwinmeApplication is not there, the Design is not initialized.
        if (Design.FONT_REGULAR36 != null) {
            Design.updateTextFont(messageView, Design.FONT_REGULAR36);
        }
        messageView.setTextColor(Design.FONT_COLOR_DEFAULT);

        if (mCustomMessage) {
            messageView.setText(mMessage);
        } else {
            messageView.setText(Html.fromHtml(String.format(getString(R.string.fatal_error_activity_error_message), mMessage)));
        }
    }
}
