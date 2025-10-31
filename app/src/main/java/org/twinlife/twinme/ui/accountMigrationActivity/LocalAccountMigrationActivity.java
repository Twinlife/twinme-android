/*
 *  Copyright (c) 2015-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.accountMigrationActivity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import org.twinlife.device.android.twinme.BuildConfig;
import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.AccountMigrationService;
import org.twinlife.twinlife.TwincodeOutbound;
import org.twinlife.twinlife.TwincodeURI;
import org.twinlife.twinlife.util.Version;
import org.twinlife.twinme.models.AccountMigration;
import org.twinlife.twinme.models.Profile;
import org.twinlife.twinme.services.AccountMigrationScannerService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.skin.DisplayMode;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.Settings;
import org.twinlife.twinme.ui.TwinmeApplication;
import org.twinlife.twinme.util.TwinmeAttributes;

import java.util.UUID;

/**
 * Local account migration activity launched by Twinme+ to migrate Twinme Lite account into Twinme+.
 */
public class LocalAccountMigrationActivity extends AbstractTwinmeActivity implements AccountMigrationScannerService.Observer {
    private static final String LOG_TAG = "LocalAccountMigr..";
    private static final boolean DEBUG = false;

    private static final float DESIGN_IMAGE_TOP_MARGIN = 40;
    private static final float DESIGN_IMAGE_BOTTOM_MARGIN = 20;
    private static final float DESIGN_IMAGE_HEIGHT = 520;

    private static final String TWINME_PLUS_ACTIVITY = "org.twinlife.device.android.twinme.plus" + BuildConfig.APPLICATION_ID_SUFFIX;

    private AccountMigrationScannerService mAccountMigrationScannerService;
    private UUID mAccountMigrationPeerTwincodeId;
    private UUID mAccountMigrationId;

    private View mAcceptButton;
    private TextView mInformationTextView;

    protected class AcceptListener implements View.OnClickListener {

        private boolean disabled = false;

        @Override
        public void onClick(View view) {
            if (DEBUG) {
                Log.d(LOG_TAG, "AcceptListener.onClick: view=" + view);
            }

            if (disabled) {

                return;
            }
            disabled = true;

            onAcceptClick();
        }
    }

    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    protected void onCreate(@NonNull Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Check that the caller is Twinme+
        ComponentName caller = this.getCallingActivity();
        if (caller == null || !TWINME_PLUS_ACTIVITY.equals(caller.getPackageName())) {
            setResult(Activity.RESULT_CANCELED);
            finish();
            return;
        }

        // Check that the incoming parameter is a valid UUID.
        Intent intent = getIntent();
        Object value = intent.getSerializableExtra(Intents.INTENT_TWINCODE_ID);
        if (!(value instanceof UUID)) {
            setResult(Activity.RESULT_CANCELED);
            finish();
            return;
        }
        mAccountMigrationPeerTwincodeId = (UUID) value;

        initViews();
        mAccountMigrationScannerService = new AccountMigrationScannerService(this, getTwinmeContext(), mAccountMigrationPeerTwincodeId, this);
    }

    //
    // Override Activity methods
    //

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        if (mAccountMigrationScannerService != null) {
            mAccountMigrationScannerService.dispose();
            mAccountMigrationScannerService = null;
        }

        super.onDestroy();
    }

    //
    // Implement AccountMigrationScannerService.Observer methods
    //

    public void onGetDefaultProfile(@NonNull Profile profile) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetDefaultProfile profile=" + profile);
        }
    }

    @Override
    public void onGetDefaultProfileNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetDefaultProfileNotFound");
        }
    }

    @Override
    public void onCreateAccountMigration(@Nullable AccountMigration accountMigration,
                                         @Nullable TwincodeURI twincodeURI) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateAccountMigration deviceMigration=" + accountMigration);
        }

        if (accountMigration == null) {
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
    }

    @Override
    public void onHasRelations() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onHasRelations");
        }

    }

    @Override
    public void onGetTwincode(@NonNull TwincodeOutbound twincodeOutbound, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetTwincode twincodeOutbound=" + twincodeOutbound);
        }

        // Refuse to start the local migration twinme -> twinme+ if twinme+ is too old: it will not work!
        // If twinme is too old but twinme+ is more recent, this is fine.
        final Pair<Version, Boolean> version = TwinmeAttributes.getTwincodeAttributeAccountMigration(twincodeOutbound);
        final Version supportedVersion = new Version(AccountMigrationService.VERSION);
        if (version.first.major >= supportedVersion.major) {
            mAccountMigrationScannerService.createAccountMigration();
        } else {
            mInformationTextView.setText(getString(R.string.local_account_migration_activity_message_older_version));
        }
    }

    @Override
    public void onGetTwincodeNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetTwincodeNotFound");
        }

        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    @Override
    public void onAccountMigrationConnected(@NonNull UUID accountMigrationId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onAccountMigrationConnected accountMigrationId=" + accountMigrationId);
        }

        // Dispose the account migration service now because it will receive several events that we don't need.
        if (mAccountMigrationScannerService != null) {
            mAccountMigrationScannerService.dispose();
            mAccountMigrationScannerService = null;
        }

        hideProgressIndicator();

        mAccountMigrationId = accountMigrationId;

        mAcceptButton.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (DEBUG) {
            Log.d(LOG_TAG, "onActivityResult requestCode=" + requestCode + " resultCode=" + resultCode + " data=" + data);
        }

        if (resultCode == Activity.RESULT_OK) {
            setResult(Activity.RESULT_OK);
        } else {
            setResult(Activity.RESULT_CANCELED);
        }

        finish();

        // Stop the twinme service to stop the application: it is now migrated to Twinme+ (we don't need to launch TwinmeLite).
        if (resultCode == Activity.RESULT_OK) {
            TwinmeApplication twinmeApplication = (TwinmeApplication) getApplication();
            twinmeApplication.stop();
        }
    }

    //
    // Private methods
    //

    protected void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.local_account_migration_activity);

        setStatusBarColor();
        setToolBar(R.id.local_account_migration_activity_tool_bar);
        showToolBar(true);
        showBackButton(false);

        setTitle(getString(R.string.account_activity_migration_title));

        ImageView imageView = findViewById(R.id.local_account_migration_activity_image_view);

        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_IMAGE_HEIGHT * Design.HEIGHT_RATIO);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) imageView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_IMAGE_TOP_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_IMAGE_BOTTOM_MARGIN * Design.HEIGHT_RATIO);

        boolean darkMode = false;
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        int displayMode = Settings.displayMode.getInt();
        if ((currentNightMode == Configuration.UI_MODE_NIGHT_YES && displayMode == DisplayMode.SYSTEM.ordinal())  || displayMode == DisplayMode.DARK.ordinal()) {
            darkMode = true;
        }

        imageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), darkMode ? R.drawable.onboarding_migration_dark : R.drawable.onboarding_migration, null));

        mInformationTextView = findViewById(R.id.local_account_migration_activity_information_view);
        Design.updateTextFont(mInformationTextView, Design.FONT_BOLD28);
        mInformationTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        AcceptListener acceptListener = new AcceptListener();
        mAcceptButton = findViewById(R.id.local_account_migration_activity_accept_view);
        mAcceptButton.setOnClickListener(acceptListener);
        mAcceptButton.setVisibility(View.GONE);

        layoutParams = mAcceptButton.getLayoutParams();
        layoutParams.height = Design.BUTTON_HEIGHT;

        float radius = 7f * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable acceptViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        acceptViewBackground.getPaint().setColor(Design.BLUE_NORMAL);
        mAcceptButton.setBackground(acceptViewBackground);

        TextView acceptTextView = findViewById(R.id.local_account_migration_activity_accept_title_view);
        Design.updateTextFont(acceptTextView, Design.FONT_BOLD28);
        acceptTextView.setTextColor(Color.WHITE);

        mProgressBarView = findViewById(R.id.local_account_migration_activity_progress_bar);
    }

    private void onAcceptClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onAcceptClick");
        }

        if (mAccountMigrationId != null) {
            mAcceptButton.setVisibility(View.GONE);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.putExtra(Intents.INTENT_ACCOUNT_MIGRATION_ID, mAccountMigrationId);
            intent.putExtra(Intents.INTENT_ACCOUNT_MIGRATION_TWINCODE, mAccountMigrationPeerTwincodeId);
            intent.setClass(this, AccountMigrationActivity.class);
            startActivityForResult(intent, 0);
        }
    }
}
