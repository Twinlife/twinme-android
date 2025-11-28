/*
 *  Copyright (c) 2020-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.accountMigrationActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.ColorUtils;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.BaseService.ErrorCode;
import org.twinlife.twinlife.TwincodeOutbound;
import org.twinlife.twinlife.TwincodeURI;
import org.twinlife.twinlife.util.Logger;
import org.twinlife.twinlife.util.Version;
import org.twinlife.twinme.models.AccountMigration;
import org.twinlife.twinme.models.Profile;
import org.twinlife.twinme.services.AccountMigrationScannerService;
import org.twinlife.twinme.services.AccountMigrationService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.skin.DisplayMode;
import org.twinlife.twinme.ui.AbstractScannerActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.Settings;
import org.twinlife.twinme.ui.TwinmeApplication;
import org.twinlife.twinme.util.TwinmeAttributes;
import org.twinlife.twinme.utils.AbstractConfirmView;
import org.twinlife.twinme.utils.DefaultConfirmView;
import org.twinlife.twinme.utils.OnboardingConfirmView;
import org.twinlife.twinme.utils.RoundedView;
import org.twinlife.twinme.utils.camera.CameraManager;

import java.util.Arrays;
import java.util.UUID;

public class AccountMigrationScannerActivity extends AbstractScannerActivity implements AccountMigrationScannerService.Observer {
    private static final String LOG_TAG = "AccountMigrationScan...";
    private static final boolean DEBUG = false;

    private static final int SHOW_ONBOARDING = 3;

    private static final float DESIGN_ZOOM_MARGIN = 21f;
    private static final float DESIGN_SHARE_PADDING = 20f;
    private static final float DESIGN_SHARE_ICON_SIZE = 42f;
    private static final float DESIGN_SHARE_ICON_PADDING = 27f;

    private class MigrationServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent == null || intent.getExtras() == null) {

                return;
            }

            final String event = intent.getExtras().getString(AccountMigrationService.MIGRATION_SERVICE_EVENT);
            if (event == null) {
                return;

            }

            // Catch exception in case an external app succeeds in sending a message.
            try {
                if (DEBUG) {
                    Log.d(LOG_TAG, "Received event=" + event);
                }
                switch (event) {
                    case AccountMigrationService.MESSAGE_STATE:
                        AccountMigrationScannerActivity.this.onMessageState(intent);
                        break;

                    case AccountMigrationService.MESSAGE_INCOMING:
                        AccountMigrationScannerActivity.this.onMessageIncoming(intent);
                        break;

                    case AccountMigrationService.MESSAGE_ERROR:
                        // Error occurred, CallService is stopping.
                        AccountMigrationScannerActivity.this.onMessageError(intent);
                        break;

                    default:
                        Log.w(LOG_TAG, "Migration event " + event + " not handled");
                        break;
                }
            } catch (Exception exception) {
                if (Logger.WARN) {
                    Log.w(LOG_TAG, "Invalid message", exception);
                }
            }
        }
    }

    private View mQrcodeContainerView;
    protected View mInfoScanView;
    protected TextView mInfoTextView;
    protected TextView mScanTitleView;

    private Profile mProfile;
    private boolean mHasRelations;
    private boolean mFromCurrentDevice = false;
    private boolean mShowOnboarding = false;

    @Nullable
    private AccountMigration mAccountMigration;
    @Nullable
    private TwincodeURI mAccountMigrationLink;
    private AccountMigrationScannerService mAccountMigrationScannerService;
    private MigrationServiceReceiver mMigrationReceiver;

    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        mHasRelations = false;
        mFromCurrentDevice = getIntent().getBooleanExtra(Intents.INTENT_MIGRATION_FROM_CURRENT_DEVICE, false);
        mAccountMigrationScannerService = new AccountMigrationScannerService(this, getTwinmeContext(), this);

        // Listen to the MigrationService messages.
        IntentFilter filter = new IntentFilter(Intents.INTENT_MIGRATION_SERVICE_MESSAGE);
        mMigrationReceiver = new MigrationServiceReceiver();

        // Register and avoid exporting the export receiver.
        ContextCompat.registerReceiver(getBaseContext(), mMigrationReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);

        updateViews();
    }

    //
    // Override Activity methods
    //

    @Override
    protected void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        super.onResume();

        if (!mShowOnboarding && getTwinmeApplication().startOnboarding(TwinmeApplication.OnboardingType.TRANSFER)) {
            mShowOnboarding = true;
            showOnboarding();
        }

        // Update again the QR-code because the twincode could change.
        updateQRCode();

        if (mAccountMigrationScannerService != null && !mAccountMigrationScannerService.isConnected()) {
            showNetworkDisconnect(R.string.account_activity_migration_title, this::finish);
        }
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        mAccountMigrationScannerService.dispose();
        unregisterReceiver(mMigrationReceiver);

        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onActivityResult requestCode=" + requestCode + " resultCode=" + resultCode + " data=" + data);
        }

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SHOW_ONBOARDING && resultCode == RESULT_CANCELED) {
            finish();
        }
    }

    @Override
    protected void onError(String message) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onError: message=" + message);
        }

        showAlertMessageView(R.id.account_migration_scanner_activity_layout, getString(R.string.deleted_account_activity_warning), message, false, this::finish);
    }

    @Override
    public void onRequestPermissions(@NonNull Permission[] grantedPermissions) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRequestPermissions grantedPermissions=" + Arrays.toString(grantedPermissions));
        }

        super.onRequestPermissions(grantedPermissions);
    }
    //
    // Implement AccountMigrationScannerService.Observer methods
    //

    @Override
    public void onGetDefaultProfile(@NonNull Profile profile) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetDefaultProfile profile=" + profile);
        }

        mProfile = profile;
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
            Log.d(LOG_TAG, "onCreateDeviceMigration deviceMigration=" + accountMigration);
        }

        mAccountMigration = accountMigration;
        mAccountMigrationLink = twincodeURI;
        updateQRCode();
    }

    @Override
    public void onHasRelations() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onHasRelations");
        }

        mHasRelations = true;
    }

    @Override
    public void onGetTwincode(@NonNull TwincodeOutbound twincodeOutbound, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetTwincode twincodeOutbound=" + twincodeOutbound);
        }

        // Close the scanner view
        stopScan();

        final Pair<Version, Boolean> version = TwinmeAttributes.getTwincodeAttributeAccountMigration(twincodeOutbound);
        checkVersion(version, () -> mAccountMigrationScannerService.bindAccountMigration(twincodeOutbound));
    }

    @Override
    public void onGetTwincodeNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetTwincodeNotFound");
        }

        incorrectQRCode(getString(R.string.capture_activity_incorrect_qrcode));
    }

    @Override
    public void onAccountMigrationConnected(@NonNull UUID accountMigrationId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeviceMigrationConnected accountMigrationId=" + accountMigrationId);
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.putExtra(Intents.INTENT_ACCOUNT_MIGRATION_ID, accountMigrationId);
        intent.setClass(this, AccountMigrationActivity.class);
        startActivity(intent);

        finish();
    }

    //
    // Private methods
    //

    @Override
    protected void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.account_migration_scanner_activity);

        setStatusBarColor();
        setToolBar(R.id.account_migration_scanner_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);
        setBackgroundColor(Design.GREY_BACKGROUND_COLOR);
        setTitle(getString(R.string.account_activity_migration_title));

        applyInsets(R.id.account_migration_scanner_activity_layout, R.id.account_migration_scanner_activity_tool_bar, R.id.account_migration_scanner_activity_content_view, Design.TOOLBAR_COLOR, false);

        View contentView = findViewById(R.id.account_migration_scanner_activity_content_view);
        contentView.setBackgroundColor(Design.GREY_BACKGROUND_COLOR);

        mQrcodeContainerView = findViewById(R.id.account_migration_scanner_activity_qrcode_container_view);
        float radius = Design.POPUP_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable containerViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        containerViewBackground.getPaint().setColor(Design.POPUP_BACKGROUND_COLOR);
        mQrcodeContainerView.setBackground(containerViewBackground);

        mQRCodeView = findViewById(R.id.account_migration_scanner_activity_qrcode_view);
        mQRCodeView.setOnClickListener(view -> onQRCodeClick());

        View zoomView = findViewById(R.id.account_migration_scanner_activity_zoom_view);
        zoomView.setVisibility(View.GONE);
        zoomView.setOnClickListener(v -> onQRCodeClick());

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) zoomView.getLayoutParams();
        marginLayoutParams.topMargin = - (int) (DESIGN_ZOOM_MARGIN * Design.HEIGHT_RATIO);

        RoundedView zoomRoundedView = findViewById(R.id.account_migration_scanner_activity_zoom_rounded_view);
        zoomRoundedView.setBorder(1, Design.GREY_COLOR);
        zoomRoundedView.setColor(Design.WHITE_COLOR);

        ImageView zoomIconView = findViewById(R.id.account_migration_scanner_activity_zoom_icon_view);
        zoomIconView.setColorFilter(Design.BLACK_COLOR);

        mCameraView = findViewById(R.id.account_migration_scanner_activity_camera_view);
        mCameraView.setOnClickListener(view -> {
            if (!mCameraGranted) {
                onSettingsClick();
            }
        });

        radius = Design.POPUP_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable cameraViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        if (mCameraGranted) {
            cameraViewBackground.getPaint().setColor(Design.GREY_BACKGROUND_COLOR);
        } else {
            cameraViewBackground.getPaint().setColor(Design.BLACK_COLOR);
        }
        mCameraView.setBackground(cameraViewBackground);

        mInfoScanView = findViewById(R.id.account_migration_scanner_activity_scan_info_view);

        ImageView scanIconView = findViewById(R.id.account_migration_scanner_activity_scan_icon_view);
        scanIconView.setColorFilter(Color.WHITE);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) scanIconView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO);
        marginLayoutParams.topMargin = (int) (DESIGN_SHARE_ICON_PADDING * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_SHARE_ICON_PADDING * Design.HEIGHT_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO));

        ViewGroup.LayoutParams layoutParams = scanIconView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_SHARE_ICON_SIZE * Design.HEIGHT_RATIO);
        layoutParams.width = (int) (DESIGN_SHARE_ICON_SIZE * Design.HEIGHT_RATIO);

        mScanTitleView = findViewById(R.id.account_migration_scanner_activity_scan_message_view);
        Design.updateTextFont(mScanTitleView, Design.FONT_MEDIUM32);
        mScanTitleView.setTextColor(Color.WHITE);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mScanTitleView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO));
        marginLayoutParams.setMarginEnd((int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO));

        mInfoTextView = findViewById(R.id.account_migration_scanner_activity_message_view);
        Design.updateTextFont(mInfoTextView, Design.FONT_REGULAR34);
        mInfoTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mMessageView = findViewById(R.id.account_migration_scanner_activity_camera_message_view);
        Design.updateTextFont(mMessageView, Design.FONT_REGULAR34);
        mMessageView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mTextureView = findViewById(R.id.account_migration_scanner_activity_texture_view);
        mTextureView.setSurfaceTextureListener(this);

        mViewFinder = findViewById(R.id.account_migration_scanner_activity_view_finder_view);
        mViewFinder.setDrawCorner(false);

        updateQRCode();
    }

    private void updateQRCode() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateQRCode");
        }

        if (mAccountMigration == null || mAccountMigrationLink == null) {
            return;
        }

        updateQRCode(mAccountMigrationLink.uri);
    }

    private void updateViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateViews");
        }

        if (mFromCurrentDevice) {
            mScanSelect = true;
            if (mCameraManager == null) {
                mCameraManager = createCameraManager(mTextureView, this, CameraManager.Mode.QRCODE);
            }

            if (mAmbientLightManager == null) {
                mAmbientLightManager = new AmbientLightManager();
            }

            if (!mCameraGranted && !mDeferedOnCreateInternal) {
                mDeferedOnCreateInternal = true;
            }
            checkCameraPermission();

            mQrcodeContainerView.setVisibility(View.GONE);
            mQRCodeView.setVisibility(View.INVISIBLE);
            mCameraView.setVisibility(View.VISIBLE);
            mInfoTextView.setText(getString(R.string.account_migration_scanner_activity_migration_start_from_current_device_message));
        } else {
            mQrcodeContainerView.setVisibility(View.VISIBLE);
            mQRCodeView.setVisibility(View.VISIBLE);
            mCameraView.setVisibility(View.GONE);
            mInfoTextView.setText(getString(R.string.account_migration_scanner_activity_migration_start_from_another_device_message));
        }
    }

    @Override
    protected void handleDecode(@NonNull Uri uri) {
        if (DEBUG) {
            Log.d(LOG_TAG, "handleDecode: uri=" + uri);
        }

        mAccountMigrationScannerService.parseURI(uri, this::onParseTwincodeURI);
    }

    private void onParseTwincodeURI(@NonNull ErrorCode errorCode, @Nullable TwincodeURI twincodeURI) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onParseTwincodeURI: errorCode=" + errorCode + " twincodeURI=" + twincodeURI);
        }

        // @todo Handle errors and report an accurate message:
        // ErrorCode.BAD_REQUEST: link is not well formed or not one of our link
        // ErrorCode.FEATURE_NOT_IMPLEMENTED: link does not target our application or domain.
        // ErrorCode.ITEM_NOT_FOUND: link targets the application but it is not compatible with the version.
        if (errorCode == ErrorCode.SUCCESS && twincodeURI != null) {
            if (twincodeURI.kind == TwincodeURI.Kind.AccountMigration && twincodeURI.twincodeId != null) {

                mAccountMigrationScannerService.getTwincodeOutbound(twincodeURI.twincodeId);
            } else {
                // The QR-code is correct but corresponds to an invitation or a call.
                incorrectQRCode(getLinkError(errorCode, R.string.capture_activity_incorrect_qrcode));
            }
        } else {
            incorrectQRCode(getLinkError(errorCode, R.string.capture_activity_incorrect_qrcode));
        }
    }

    @Override
    protected void incorrectQRCode(String message) {
        if (DEBUG) {
            Log.d(LOG_TAG, "incorrectQRCode");
        }

        showAlertMessageView(R.id.account_migration_scanner_activity_layout, getString(R.string.deleted_account_activity_warning), message, false, this::finish);
    }

    @Override
    protected void permissionCameraResult() {
        if (DEBUG) {
            Log.d(LOG_TAG, "permissionCameraResult");
        }

        super.permissionCameraResult();

        if (mCameraGranted) {
            mMessageView.setVisibility(View.GONE);
            mInfoScanView.setVisibility(View.VISIBLE);
            mInfoScanView.postDelayed(() -> mInfoScanView.setVisibility(View.GONE), 5000);
        } else {
            mInfoScanView.setVisibility(View.GONE);
            mMessageView.setVisibility(View.VISIBLE);
            mMessageView.setText(getResources().getString(R.string.capture_activity_no_camera));
        }
    }

    private void onQRCodeClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onQRCodeClick");
        }
    }

    private void stopScan() {
        if (DEBUG) {
            Log.d(LOG_TAG, "stopScan");
        }

        mScanSelect = false;

        // Release the camera and any resource while we are not active.
        if (mCameraManager != null) {
            mCameraManager.close();
            mCameraManager = null;
        }

        // Turn off the ambient light manager if there is one.
        if (mAmbientLightManager != null) {
            mAmbientLightManager.stop();
            mAmbientLightManager = null;
        }
    }

    private void checkVersion(@NonNull Pair<Version, Boolean> peerVersion, @NonNull Runnable runMigration) {
        if (DEBUG) {
            Log.d(LOG_TAG, "checkVersion peerVersion=" + peerVersion);
        }

        // If the peer version is too old, there is a strong risk to loose data: if we send our database
        // it has a new format that is not compatible with the peer device application.
        // - if version match, we can proceed,
        // - if our version is newer and there is no relation, we can proceed,
        // - if our version is older and the peer has no relation, we can proceed.
        final Version supportedVersion = new Version(org.twinlife.twinlife.AccountMigrationService.VERSION);
        if (peerVersion.first.major == supportedVersion.major
                || (peerVersion.first.major < supportedVersion.major && !mHasRelations)
                || (peerVersion.first.major > supportedVersion.major && Boolean.FALSE.equals(peerVersion.second))) {
            runMigration.run();
        } else {
            // Ask confirmation here to issue the bindMigration()
            Log.e(LOG_TAG, "AccountMigration is stopped because the peer device is old!");
            String message;
            if (peerVersion.first.major < supportedVersion.major) {
                message = getString(R.string.account_migration_scanner_activity_message_older_version_target);
            } else {
                message = getString(R.string.account_migration_scanner_activity_message_older_version);
            }

            ViewGroup viewGroup = findViewById(R.id.account_migration_scanner_activity_layout);

            DefaultConfirmView defaultConfirmView = new DefaultConfirmView(this, null);
            defaultConfirmView.setTitle(getString(R.string.deleted_account_activity_warning));
            defaultConfirmView.setMessage(message);

            boolean darkMode = false;
            int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            int displayMode = Settings.displayMode.getInt();
            if ((currentNightMode == Configuration.UI_MODE_NIGHT_YES && displayMode == DisplayMode.SYSTEM.ordinal())  || displayMode == DisplayMode.DARK.ordinal()) {
                darkMode = true;
            }

            defaultConfirmView.setImage(ResourcesCompat.getDrawable(getResources(), darkMode ? R.drawable.onboarding_migration_dark : R.drawable.onboarding_migration, null));
            defaultConfirmView.setConfirmTitle(getString(R.string.account_migration_activity_start));

            AbstractConfirmView.Observer observer = new AbstractConfirmView.Observer() {
                @Override
                public void onConfirmClick() {
                    defaultConfirmView.animationCloseConfirmView();
                }

                @Override
                public void onCancelClick() {
                    defaultConfirmView.animationCloseConfirmView();
                }

                @Override
                public void onDismissClick() {
                    defaultConfirmView.animationCloseConfirmView();
                }

                @Override
                public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                    viewGroup.removeView(defaultConfirmView);
                    setStatusBarColor();

                    if (fromConfirmAction) {
                        runMigration.run();
                    } else {
                        finish();
                    }
                }
            };
            defaultConfirmView.setObserver(observer);
            viewGroup.addView(defaultConfirmView);
            defaultConfirmView.show();

            Window window = getWindow();
            window.setNavigationBarColor(Design.POPUP_BACKGROUND_COLOR);
        }
    }

    private void onMessageIncoming(@NonNull Intent intent) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onMessageIncoming: intent=" + intent);
        }

        UUID accountMigrationId = (UUID) intent.getSerializableExtra(AccountMigrationService.MIGRATION_DEVICE_ID);
        if (accountMigrationId == null) {
            return;
        }
        String version = intent.getStringExtra(AccountMigrationService.MIGRATION_PEER_VERSION);
        boolean peerHasContacts = intent.getBooleanExtra(AccountMigrationService.MIGRATION_PEER_HAS_CONTACTS, true);
        if (version == null) {
            return;
        }

        checkVersion(new Pair<>(new Version(version), peerHasContacts), () -> {
            Intent serviceIntent = new Intent(this, AccountMigrationService.class);
            serviceIntent.setAction(AccountMigrationService.ACTION_ACCEPT_MIGRATION);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
            onAccountMigrationConnected(accountMigrationId);
        });
    }

    private void onMessageState(@NonNull Intent intent) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onMessageState: intent=" + intent);
        }
    }

    private void onMessageError(@NonNull Intent intent) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onMessageError: intent=" + intent);
        }
    }

    private void showOnboarding() {
        if (DEBUG) {
            Log.d(LOG_TAG, "showOnboarding");
        }

        ViewGroup viewGroup = findViewById(R.id.account_migration_scanner_activity_layout);

        OnboardingConfirmView onboardingConfirmView = new OnboardingConfirmView(this, null);
        onboardingConfirmView.setTitle(getString(R.string.account_activity_migration_title));

        boolean darkMode = false;
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        int displayMode = Settings.displayMode.getInt();
        if ((currentNightMode == Configuration.UI_MODE_NIGHT_YES && displayMode == DisplayMode.SYSTEM.ordinal())  || displayMode == DisplayMode.DARK.ordinal()) {
            darkMode = true;
        }

        onboardingConfirmView.setImage(ResourcesCompat.getDrawable(getResources(), darkMode ? R.drawable.onboarding_migration_dark : R.drawable.onboarding_migration, null));
        onboardingConfirmView.setMessage(getString(R.string.account_activity_migration_message));
        onboardingConfirmView.setConfirmTitle(getString(R.string.application_ok));
        onboardingConfirmView.setCancelTitle(getString(R.string.application_do_not_display));

        AbstractConfirmView.Observer observer = new AbstractConfirmView.Observer() {
            @Override
            public void onConfirmClick() {
                onboardingConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCancelClick() {
                getTwinmeApplication().setShowOnboardingType(TwinmeApplication.OnboardingType.TRANSFER, false);
                onboardingConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onDismissClick() {
                onboardingConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                viewGroup.removeView(onboardingConfirmView);
                setStatusBarColor();
            }
        };
        onboardingConfirmView.setObserver(observer);
        viewGroup.addView(onboardingConfirmView);
        onboardingConfirmView.show();

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }

    @Override
    public void updateFont() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateFont");
        }

        super.updateFont();

        Design.updateTextFont(mScanTitleView, Design.FONT_MEDIUM32);
        Design.updateTextFont(mInfoTextView, Design.FONT_REGULAR34);
        Design.updateTextFont(mMessageView, Design.FONT_REGULAR34);
    }

    @Override
    public void updateColor() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateColor");
        }

        super.updateColor();

        mInfoTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mMessageView.setTextColor(Design.FONT_COLOR_DEFAULT);
    }
}
