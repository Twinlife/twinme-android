/*
 *  Copyright (c) 2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.privacyActivity;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.accountMigrationActivity.AccountMigrationActivity;
import org.twinlife.twinme.ui.mainActivity.MainActivity;
import org.twinlife.twinme.ui.welcomeActivity.WelcomeActivity;

import java.util.UUID;

public class LockScreenActivity extends AbstractTwinmeActivity {
    private static final String LOG_TAG = "LockScreenActivity";
    private static final boolean DEBUG = false;

    private static final int UNLOCK_REQUEST = 1;

    private UUID mDeviceMigrationId;
    private boolean mIsMigration = false;
    private boolean mHasConversations = false;
    private boolean mFromSplashScreen = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        mCanShowLockScreen = false;

        Intent intent = getIntent();
        mHasConversations = intent.getBooleanExtra(Intents.INTENT_HAS_CONVERSATIONS, false);
        mIsMigration = intent.getBooleanExtra(Intents.INTENT_IS_MIGRATION, false);
        mFromSplashScreen = intent.getBooleanExtra(Intents.INTENT_FROM_SPLASHSCREEN, false);
        mDeviceMigrationId = (UUID) intent.getSerializableExtra(Intents.INTENT_ACCOUNT_MIGRATION_ID);

        initViews();
    }

    @Override
    protected void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        super.onResume();
    }

    @Override
    public void onBackPressed() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBackPressed");
        }

        moveTaskToBack(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (UNLOCK_REQUEST == requestCode) {
            if (resultCode == RESULT_OK) {
                InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputManager != null) {
                    inputManager.hideSoftInputFromWindow(getWindow().getDecorView().getApplicationWindowToken(), 0);
                }
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

                Handler handler = new Handler();
                handler.postDelayed(this::unlockSuccess, 100);
            }
        }
    }

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        setContentView(R.layout.lock_screen_activity);

        setStatusBarColor(Design.WHITE_COLOR);

        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        View unlockClickableView = findViewById(R.id.lock_screen_activity_unlock_view);
        unlockClickableView.setOnClickListener(v -> onUnlockClick());

        ShapeDrawable saveViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        saveViewBackground.getPaint().setColor(Color.WHITE);
        ViewCompat.setBackground(unlockClickableView, saveViewBackground);

        ViewGroup.LayoutParams layoutParams = unlockClickableView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = Design.BUTTON_HEIGHT;

        TextView saveTextView = findViewById(R.id.lock_screen_activity_unlock_title_view);
        saveTextView.setTypeface(Design.FONT_BOLD28.typeface);
        saveTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_BOLD28.size);
        saveTextView.setTextColor(Color.BLACK);

        onUnlockClick();
    }

    private void onUnlockClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUnlockClick");
        }

        if (!isDeviceSecure()) {
            showAlertMessageView(R.id.lock_screen_activity_content_view, getString(R.string.deleted_account_activity_warning), getString(R.string.lock_screen_activity_passcode_not_set), false, null);
            return;
        }

        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        if (keyguardManager != null) {
            Intent screenLockIntent = keyguardManager.createConfirmDeviceCredentialIntent(getString(R.string.lock_screen_activity_unlock), getString(R.string.lock_screen_activity_local_authentication));
            startActivityForResult(screenLockIntent, UNLOCK_REQUEST);
        }
    }

    private boolean isDeviceSecure() {
        if (DEBUG) {
            Log.d(LOG_TAG, "isDeviceSecure");
        }

        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if (keyguardManager != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                return keyguardManager.isDeviceSecure();
            }
            return keyguardManager.isKeyguardSecure();
        }

        return false;
    }

    private void unlockSuccess() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUnlockClick");
        }

        getTwinmeApplication().unlockScreen();

        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_NO_ANIMATION);

        intent.putExtra(Intents.INTENT_HAS_CONVERSATIONS, mHasConversations);
        if (mIsMigration) {
            intent.putExtra(Intents.INTENT_ACCOUNT_MIGRATION_ID, mDeviceMigrationId);
            intent.setClass(this, AccountMigrationActivity.class);
            startActivity(intent);
        } else if (mFromSplashScreen) {
            if (!getTwinmeApplication().showWelcomeScreen()) {
                intent.setClass(this, MainActivity.class);
            } else {
                intent.setClass(this, WelcomeActivity.class);
            }
            startActivity(intent);
        }

        finish();
        overridePendingTransition(0, 0);
    }
}
