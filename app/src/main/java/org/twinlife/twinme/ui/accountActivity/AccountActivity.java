/*
 *  Copyright (c) 2020-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.accountActivity;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.services.BackupService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.Permission;
import org.twinlife.twinme.ui.TwinmeApplication;
import org.twinlife.twinme.ui.accountMigrationActivity.AccountMigrationScannerActivity;
import org.twinlife.twinme.ui.backupActivity.BackupActivity;
import org.twinlife.twinme.ui.backupActivity.BackupsActivity;
import org.twinlife.twinme.ui.backupActivity.RestoreActivity;
import org.twinlife.twinme.ui.cleanupActivity.TypeCleanUpActivity;
import org.twinlife.twinme.ui.exportActivity.ExportActivity;
import org.twinlife.twinme.utils.AbstractBottomSheetView;
import org.twinlife.twinme.utils.AlertMessageView;
import org.twinlife.twinme.utils.FileInfo;
import org.twinlife.twinme.utils.OnboardingConfirmView;

import java.util.Arrays;
import java.util.Objects;

public class AccountActivity extends AbstractTwinmeActivity {
    private static final String LOG_TAG = "AccountActivity";
    private static final boolean DEBUG = false;

    private enum OnboardingBackupType {
        BACKUP,
        RESTORE,
        VERIFY,
        BETA
    }

    private static final int REQUEST_GET_FILE = 1;

    private boolean mVerifyBackupMode = false;

    private boolean mStartOnboardingOnResume = false;

    private Uri mBackupUri;

    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mStartOnboardingOnResume = intent.getBooleanExtra(Intents.INTENT_SHOW_ONBOARDING, false);

        if (savedInstanceState != null) {
            mVerifyBackupMode = savedInstanceState.getBoolean(Intents.INTENT_BACKUP_VERIFY_MODE);
        }

        initViews();
    }

    //
    // Override Activity methods
    //

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSaveInstanceState: outState=" + outState);
        }

        super.onSaveInstanceState(outState);

        outState.putBoolean(Intents.INTENT_BACKUP_VERIFY_MODE, mVerifyBackupMode);
    }

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

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            if (getTwinmeApplication().showBackupWarning()) {
                getTwinmeApplication().setLastBackupAlertDate();
                showBackupWarning();
            } else if (mStartOnboardingOnResume && getTwinmeApplication().startOnboarding(TwinmeApplication.OnboardingType.BACKUP_BETA)) {
                mStartOnboardingOnResume = false;
                showBackupOnboarding(OnboardingBackupType.BETA, true);
            }
        }
    }

    @Override
    public void onApplyInsetsFinish() {
        super.onApplyInsetsFinish();

        if (getTwinmeApplication().showBackupWarning()) {
            getTwinmeApplication().setLastBackupAlertDate();
            showBackupWarning();
        } else if (mStartOnboardingOnResume && getTwinmeApplication().startOnboarding(TwinmeApplication.OnboardingType.BACKUP_BETA)) {
            mStartOnboardingOnResume = false;
            showBackupOnboarding(OnboardingBackupType.BETA, true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onActivityResult: requestCode=" + requestCode + " resultCode=" + resultCode + " intent=" + intent);
        }

        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_OK && requestCode == REQUEST_GET_FILE) {
            ClipData clipData = (intent == null) ? null : intent.getClipData();
            if (clipData != null && clipData.getItemCount() > 0) {
                mBackupUri = clipData.getItemAt(0).getUri();
            } else {
                // single selection or old android
                Uri uri = (intent == null) ? null : intent.getData();
                if (uri != null) {
                    mBackupUri = uri;
                }
            }

            if (mBackupUri != null) {
                getTwinmeContext().execute(this::openBackup);
            }
        }


    }

    @Override
    public void onRequestPermissions(@NonNull Permission[] grantedPermissions) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRequestPermissions grantedPermissions=" + Arrays.toString(grantedPermissions));
        }

        boolean storageReadAccessGranted = false;
        for (Permission grantedPermission : grantedPermissions) {
            if (Objects.requireNonNull(grantedPermission) == Permission.READ_EXTERNAL_STORAGE) {
                storageReadAccessGranted = true;
            }
        }

        if (storageReadAccessGranted) {
            openFileIntent();
        } else {
            message(getString(R.string.application_denied_permissions), 0L, new DefaultMessageCallback(R.string.application_ok) {
            });
        }
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

    public void onBackupClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBackupClick");
        }

        showBackupOnboarding(OnboardingBackupType.BACKUP, false);
    }

    public void onRestoreClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRestoreClick");
        }

        mVerifyBackupMode = false;
        showRestoreWarning();
    }

    public void onVerifyBackupClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onVerifyBackupClick");
        }

        mVerifyBackupMode = true;
        showBackupOnboarding(OnboardingBackupType.VERIFY, false);
    }

    public void onBackupsClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBackupsClick");
        }

        startActivity(BackupsActivity.class);
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

    public void onBetaInfoClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBetaInfoClick");
        }

        showBackupOnboarding(OnboardingBackupType.BETA, false);
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
        setTitle(getString(R.string.account_view_title));

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

    private void showBackupOnboarding(OnboardingBackupType onboardingBackupType, boolean fromBadge) {
        if (DEBUG) {
            Log.d(LOG_TAG, "showBackupOnboarding: onboardingBackupType " + onboardingBackupType);
        }

        boolean startOnboarding = true;
        if (onboardingBackupType == OnboardingBackupType.BACKUP) {
            startOnboarding = getTwinmeApplication().startOnboarding(TwinmeApplication.OnboardingType.BACKUP);
        } else if (onboardingBackupType == OnboardingBackupType.RESTORE) {
            startOnboarding = getTwinmeApplication().startOnboarding(TwinmeApplication.OnboardingType.RESTORE);
        } else if (onboardingBackupType == OnboardingBackupType.VERIFY) {
            startOnboarding = getTwinmeApplication().startOnboarding(TwinmeApplication.OnboardingType.VERIFY_BACKUP);
        }

        if (startOnboarding) {
            ViewGroup viewGroup = findViewById(R.id.account_activity_layout);

            OnboardingConfirmView onboardingConfirmView = new OnboardingConfirmView(this, null);

            String title;
            String message;
            String action;
            if (onboardingBackupType == OnboardingBackupType.BACKUP) {
                title = getString(R.string.account_view_backup);
                message = getString(R.string.backup_view_onboarding);
                action = getString(R.string.backup_view_backup);
            } else if (onboardingBackupType == OnboardingBackupType.RESTORE) {
                title = getString(R.string.account_view_restore);
                message = getResources().getString(R.string.restore_view_onboarding) +
                        "\n\n" + getResources().getString(R.string.restore_view_onboarding_words);
                action = getString(R.string.restore_view_restore);
            } else if (onboardingBackupType == OnboardingBackupType.VERIFY) {
                title = getString(R.string.account_view_backup_verify);
                message = getResources().getString(R.string.restore_view_onboarding_verify) +
                        "\n\n" + getResources().getString(R.string.backup_view_verify_words);
                action = getString(R.string.restore_view_select_backup);
            } else {
                title = getString(R.string.account_view_backup_restore);
                message = getString(R.string.backup_view_onboarding);
                action = getString(R.string.application_ok);
            }

            onboardingConfirmView.setTitle(title);
            onboardingConfirmView.setImage(ResourcesCompat.getDrawable(getResources(), R.drawable.onboarding_backup, null));
            onboardingConfirmView.setMessage(message);
            onboardingConfirmView.setConfirmTitle(action);

            if (onboardingBackupType == OnboardingBackupType.BETA && !fromBadge) {
                onboardingConfirmView.hideCancelView();
            } else {
                onboardingConfirmView.setCancelTitle(getString(R.string.application_do_not_display));
            }

            AbstractBottomSheetView.Observer observer = new AbstractBottomSheetView.Observer() {
                @Override
                public void onConfirmClick() {
                    onboardingConfirmView.animationCloseConfirmView();

                    if (onboardingBackupType != OnboardingBackupType.BETA) {
                        startBackupIntent(onboardingBackupType);
                    }
                }

                @Override
                public void onCancelClick() {
                    onboardingConfirmView.animationCloseConfirmView();

                    if (onboardingBackupType == OnboardingBackupType.BACKUP) {
                        getTwinmeApplication().setShowOnboardingType(TwinmeApplication.OnboardingType.BACKUP, false);
                    } else if (onboardingBackupType == OnboardingBackupType.RESTORE) {
                        getTwinmeApplication().setShowOnboardingType(TwinmeApplication.OnboardingType.RESTORE, false);
                    } else if (onboardingBackupType == OnboardingBackupType.VERIFY) {
                        getTwinmeApplication().setShowOnboardingType(TwinmeApplication.OnboardingType.VERIFY_BACKUP, false);
                    } else if (onboardingBackupType == OnboardingBackupType.BETA && fromBadge) {
                        getTwinmeApplication().setShowOnboardingType(TwinmeApplication.OnboardingType.BACKUP_BETA, false);
                    }

                    if (onboardingBackupType != OnboardingBackupType.BETA) {
                        startBackupIntent(onboardingBackupType);
                    }
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
        } else {
            startBackupIntent(onboardingBackupType);
        }
    }

    private void showBackupWarning() {
        if (DEBUG) {
            Log.d(LOG_TAG, "showBackupWarning");
        }

        ViewGroup viewGroup = findViewById(R.id.account_activity_layout);

        OnboardingConfirmView onboardingConfirmView = new OnboardingConfirmView(this, null);
        onboardingConfirmView.setTitle(getString(R.string.account_view_backup));
        onboardingConfirmView.setImage(ResourcesCompat.getDrawable(getResources(), R.drawable.onboarding_backup, null));
        onboardingConfirmView.setMessage(getString(R.string.backup_view_reminder));
        onboardingConfirmView.setConfirmTitle(getString(R.string.backup_view_new_backup));
        onboardingConfirmView.setCancelTitle(getString(R.string.application_later));

        AbstractBottomSheetView.Observer observer = new AbstractBottomSheetView.Observer() {
            @Override
            public void onConfirmClick() {
                onboardingConfirmView.animationCloseConfirmView();
                startBackupIntent(OnboardingBackupType.BACKUP);
            }

            @Override
            public void onCancelClick() {
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
    private void showRestoreWarning() {
        if (DEBUG) {
            Log.d(LOG_TAG, "showRestoreWarning");
        }

        ViewGroup viewGroup = findViewById(R.id.account_activity_layout);

        OnboardingConfirmView onboardingConfirmView = new OnboardingConfirmView(this, null);
        onboardingConfirmView.setTitle(getString(R.string.deleted_account_view_warning));
        onboardingConfirmView.setMessage(getString(R.string.restore_view_backup_device_always_signed_in_part_three));
        onboardingConfirmView.setConfirmTitle( getString(R.string.account_view_transfer_from_another_device));
        onboardingConfirmView.setCancelTitle(getString(R.string.account_view_restore));

        AbstractBottomSheetView.Observer observer = new AbstractBottomSheetView.Observer() {
            @Override
            public void onConfirmClick() {
                onboardingConfirmView.animationCloseConfirmView();
                onTransferClick(false);
            }

            @Override
            public void onCancelClick() {

                onboardingConfirmView.animationCloseConfirmView();
                showBackupOnboarding(OnboardingBackupType.RESTORE, false);
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

    private void startBackupIntent(OnboardingBackupType  onboardingBackupType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "startBackupIntent: onboardingBackupType " + onboardingBackupType);
        }

        if (onboardingBackupType == OnboardingBackupType.BACKUP) {
            Intent intent = new Intent();
            intent.setClass(this, BackupActivity.class);
            startActivity(intent);
        } else {
            onSelectBackupClick();
        }
    }

    private void onSelectBackupClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSelectBackupClick");
        }

        Permission[] permissions = new Permission[]{
                Permission.READ_EXTERNAL_STORAGE};

        if (checkPermissions(permissions)) {
            openFileIntent();
        }
    }

    private void openFileIntent() {
        if (DEBUG) {
            Log.d(LOG_TAG, "openFileIntent");
        }

        Intent chooseFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFileIntent.setType("*/*");
        chooseFileIntent.addCategory(Intent.CATEGORY_OPENABLE);

        startActivityForResult(chooseFileIntent, REQUEST_GET_FILE);
    }

    @WorkerThread
    private void openBackup() {
        if (DEBUG) {
            Log.d(LOG_TAG, "openBackup");
        }

        FileInfo fileInfo = new FileInfo(this, mBackupUri);
        final Context context = getApplicationContext();
        final FileInfo copy;
        if (mBackupUri.getPath() != null && mBackupUri.getPath().startsWith(getApplicationContext().getCacheDir().getAbsolutePath())) {
            copy = fileInfo;
        } else {
            copy = fileInfo.saveFile(context);
        }

        if (copy != null) {
            runOnUiThread(() -> {
                Intent intent = new Intent();
                intent.putExtra(BackupService.BACKUP_SERVICE_FILE_NAME, fileInfo.getFilename());
                intent.putExtra(BackupService.BACKUP_SERVICE_FILE_PATH, copy.getUri().toString());
                intent.putExtra(Intents.INTENT_BACKUP_VERIFY_MODE, mVerifyBackupMode);
                intent.setClass(this, RestoreActivity.class);
                startActivity(intent);
            });
        }
    }

    private void fileNotSupported() {
        if (DEBUG) {
            Log.d(LOG_TAG, "fileNotSupported");
        }

        ViewGroup viewGroup = findViewById(R.id.account_activity_layout);

        AlertMessageView alertMessageView = new AlertMessageView(this, null);
        alertMessageView.setMessage(getString(R.string.restore_view_file_not_supported));

        AlertMessageView.Observer observer = new AlertMessageView.Observer() {

            @Override
            public void onConfirmClick() {
                alertMessageView.animationCloseConfirmView();
            }

            @Override
            public void onDismissClick() {
                alertMessageView.animationCloseConfirmView();
            }

            @Override
            public void onCloseViewAnimationEnd() {
                viewGroup.removeView(alertMessageView);
                setStatusBarColor();
            }
        };
        alertMessageView.setObserver(observer);

        viewGroup.addView(alertMessageView);
        alertMessageView.show();

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }
}
