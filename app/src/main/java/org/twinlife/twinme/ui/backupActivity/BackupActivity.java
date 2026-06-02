/*
 *  Copyright (c) 2024-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.backupActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.BackupService.BackupState;
import org.twinlife.twinlife.BaseService;
import org.twinlife.twinlife.util.Logger;
import org.twinlife.twinme.services.BackupService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.utils.AbstractBottomSheetView;
import org.twinlife.twinme.utils.BackupStats;
import org.twinlife.twinme.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class BackupActivity extends AbstractTwinmeActivity {
    private static final String LOG_TAG = "BackupActivity";
    private static final boolean DEBUG = false;

    private class BackupServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent == null || intent.getExtras() == null) {

                return;
            }

            final String event = intent.getExtras().getString(BackupService.BACKUP_SERVICE_EVENT);
            if (event == null) {
                return;
            }

            // Catch exception in case an external app succeeds in sending a message.
            try {
                if (DEBUG) {
                    Log.d(LOG_TAG, "Received event=" + event);
                }
                switch (event) {
                    case BackupService.MESSAGE_WORDS_GENERATED:
                        onMessageWordsGenerated(intent);
                        break;

                    case BackupService.MESSAGE_BACKUP_STATE:
                        onMessageBackupState(intent);
                        break;

                    case BackupService.MESSAGE_BACKUP_ERROR:
                        onMessageBackupError(intent);
                        break;

                    default:
                        Log.w(LOG_TAG, "Backup event " + event + " not handled");
                        break;
                }
            } catch (Exception exception) {
                if (Logger.WARN) {
                    Log.w(LOG_TAG, "Invalid message", exception);
                }
            }
        }
    }

    private View mOverlayView;

    private BackupAdapter mBackupAdapter;

    private final List<UIBackupWord> mBackupWords = new ArrayList<>();
    private String mBackupFilePath;

    private BackupServiceReceiver mBackupServiceReceiver;
    private boolean mConfirmBackup = false;

    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Listen to the backupService messages.
        IntentFilter filter = new IntentFilter(Intents.INTENT_BACKUP_SERVICE_MESSAGE);
        mBackupServiceReceiver = new BackupServiceReceiver();

        // Register and avoid exporting the backup receiver.
        ContextCompat.registerReceiver(getBaseContext(), mBackupServiceReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);

        initViews();

        //TODO BPK: call only if we're not reusing the previous password
        Intent intent = new Intent();
        intent.setClass(this, BackupService.class);
        intent.setAction(BackupService.ACTION_GENERATE_WORDS);

        startService(intent);
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        super.onDestroy();

        runOnUiThread(() -> {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        });

        Intent intent = new Intent();
        intent.setClass(this, BackupService.class);
        intent.setAction(BackupService.ACTION_STOP);
        startService(intent);

        unregisterReceiver(mBackupServiceReceiver);
    }

    public boolean isConfirmBackup() {
        if (DEBUG) {
            Log.d(LOG_TAG, "isConfirmBackup");
        }

        return mConfirmBackup;
    }

    public void onConfirmBackupClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onConfirmBackupClick");
        }

        mConfirmBackup = !mConfirmBackup;
        mBackupAdapter.updateConfirmBackup();
    }

    public void onGenerateWordsClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGenerateWordsClick");
        }

        Intent intent = new Intent();
        intent.setClass(this, BackupService.class);
        intent.setAction(BackupService.ACTION_GENERATE_WORDS);
        startService(intent);
    }

    public void onCopyWordsClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCopyWordsClick");
        }

        Utils.setClipboard(this, getWordsList());
        Toast.makeText(this, R.string.conversation_view_menu_item_view_copy_message, Toast.LENGTH_SHORT).show();
    }

    private void onMessageWordsGenerated(@NonNull Intent intent) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onMessageWordsGenerated: intent=" + intent);
        }

        mBackupWords.clear();

        List<String> passwordWords = intent.getStringArrayListExtra(BackupService.BACKUP_SERVICE_PASSWORD_WORDS);
        if (passwordWords != null) {
            for (int i = 0; i < passwordWords.size(); i++) {
                UIBackupWord uiBackupWord = new UIBackupWord(passwordWords.get(i), i);
                mBackupWords.add(uiBackupWord);
            }
        }

        mBackupAdapter.updateWords(mBackupWords);
    }

    public void onMessageBackupState(@NonNull Intent intent) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onMessageBackupState");
        }

        BackupState backupState = (BackupState) intent.getSerializableExtra(BackupService.BACKUP_SERVICE_BACKUP_STATE);
        if (backupState != null) {

            switch (backupState) {
                case STARTING:
                    mOverlayView.setVisibility(View.VISIBLE);
                    mProgressBarView.setVisibility(View.VISIBLE);
                    break;

                case GENERATE_KEY:
                case CREATE_FILE:
                    break;

                case TERMINATED: {
                    mBackupFilePath = intent.getStringExtra(BackupService.BACKUP_SERVICE_FILE_PATH);

                    mOverlayView.setVisibility(View.GONE);
                    mProgressBarView.setVisibility(View.GONE);

                    BackupStats backupStats = (BackupStats) intent.getSerializableExtra(BackupService.BACKUP_SERVICE_STATS);
                    showBackupContent(backupStats);
                    break;
                }

                default:
                    break;
            }
        }
    }

    public void onMessageBackupError(@NonNull Intent intent) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onMessageBackupError");
        }

        org.twinlife.twinlife.BackupService.ErrorCode errorCode = (org.twinlife.twinlife.BackupService.ErrorCode) intent.getSerializableExtra(BackupService.BACKUP_SERVICE_ERROR_CODE);
        BaseService.ErrorCode baseErrorCode = (BaseService.ErrorCode) intent.getSerializableExtra(BackupService.BACKUP_SERVICE_BASE_ERROR_CODE);

        mOverlayView.setVisibility(View.GONE);
        mProgressBarView.setVisibility(View.GONE);

        showErrorMessage(errorCode, baseErrorCode);
    }

    //
    // Private methods
    //

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.backup_activity);

        setStatusBarColor();
        setToolBar(R.id.backup_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);
        setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        setTitle(getString(R.string.backup_view_title));

        applyInsets(R.id.backup_activity_layout, R.id.backup_activity_tool_bar, R.id.backup_activity_list_view, Design.TOOLBAR_COLOR, false);

        ViewGroup layout = findViewById(R.id.backup_activity_layout);
        layout.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        BackupAdapter.OnCreateBackupClickListener onCreateBackupClickListener = this::onBackupClick;

        mBackupAdapter = new BackupAdapter(this, onCreateBackupClickListener);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        RecyclerView recyclerView = findViewById(R.id.backup_activity_list_view);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(mBackupAdapter);
        recyclerView.setItemAnimator(null);
        recyclerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        mOverlayView = findViewById(R.id.backup_activity_overlay_view);
        mOverlayView.setBackgroundColor(Design.OVERLAY_VIEW_COLOR);

        mProgressBarView = findViewById(R.id.backup_activity_progress_bar);
    }

    private void onBackupClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBackupClick");
        }
        
        if (mConfirmBackup) {
            Intent intent = new Intent();
            intent.setClass(this, BackupService.class);
            intent.setAction(BackupService.ACTION_START_BACKUP);

            startService(intent);
        }
    }

    private void showBackupContent(BackupStats backupStats) {
        if (DEBUG) {
            Log.d(LOG_TAG, "showBackupContent");
        }

        ViewGroup viewGroup = findViewById(R.id.backup_activity_layout);

        BackupContentConfirmView backupContentConfirmView = new BackupContentConfirmView(this, null);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        backupContentConfirmView.setLayoutParams(layoutParams);

        backupContentConfirmView.initStats(backupStats);

        AbstractBottomSheetView.Observer observer = new AbstractBottomSheetView.Observer() {
            @Override
            public void onConfirmClick() {

                onBackupSuccess();
            }

            @Override
            public void onCancelClick() {
                backupContentConfirmView.animationCloseConfirmView();
                onBackupCancel();
            }

            @Override
            public void onDismissClick() {
                backupContentConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                viewGroup.removeView(backupContentConfirmView);
                setStatusBarColor();
            }
        };
        backupContentConfirmView.setObserver(observer);

        viewGroup.addView(backupContentConfirmView);
        backupContentConfirmView.show();

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }

    private void onBackupSuccess() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBackupSuccess");
        }

        Intent intent = new Intent();
        intent.setClass(this, SuccessBackupActivity.class);
        intent.putExtra(BackupService.BACKUP_SERVICE_FILE_PATH, mBackupFilePath);
        intent.putExtra(BackupService.BACKUP_SERVICE_PASSWORD_WORDS, getWordsList());
        startActivity(intent);

        finish();
    }

    private void onBackupCancel() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBackupCancel");
        }

        Intent intent = new Intent();
        intent.setClass(this, BackupService.class);
        intent.setAction(BackupService.ACTION_CANCEL_BACKUP);
        startService(intent);
    }

    private String getWordsList() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getWordsList");
        }

        StringBuilder stringBuilder = new StringBuilder();

        for (UIBackupWord backupWord : mBackupWords) {
            stringBuilder.append(backupWord.getWord());
            stringBuilder.append(" ");
        }
        return stringBuilder.toString();
    }

    private void showErrorMessage(org.twinlife.twinlife.BackupService.ErrorCode errorCode, BaseService.ErrorCode baseErrorCode) {
        if (DEBUG) {
            Log.d(LOG_TAG, "showErrorMessage");
        }

        String message;
        if (errorCode == org.twinlife.twinlife.BackupService.ErrorCode.NO_SPACE_LEFT) {
            message = getString(R.string.application_error_no_storage_space);
        } else if (errorCode == org.twinlife.twinlife.BackupService.ErrorCode.KEY_GEN_FAILED && baseErrorCode == BaseService.ErrorCode.TWINLIFE_OFFLINE) {
            message = getString(R.string.application_connection_status_no_network_message);
        } else {
            message = getString(R.string.cleanup_view_error);
        }

        showAlertMessageView(R.id.backup_activity_layout, getString(R.string.deleted_account_view_warning), message, false, this::finish);
    }
}
