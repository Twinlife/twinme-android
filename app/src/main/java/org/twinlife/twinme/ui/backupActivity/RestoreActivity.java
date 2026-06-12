/*
 *  Copyright (c) 2024-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.backupActivity;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.BackupService.RestoreState;
import org.twinlife.twinlife.BackupService.TerminateReason;
import org.twinlife.twinlife.backup.BackupHeaderInfo;
import org.twinlife.twinlife.util.Logger;
import org.twinlife.twinme.services.BackupService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.SplashScreenActivity;
import org.twinlife.twinme.ui.TwinmeApplication;
import org.twinlife.twinme.utils.AbstractBottomSheetView;
import org.twinlife.twinme.utils.DefaultConfirmView;
import org.twinlife.twinme.utils.FileInfo;
import org.twinlife.twinme.utils.MnemonicCodeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RestoreActivity extends AbstractTwinmeActivity {
    private static final String LOG_TAG = "RestoreActivity";
    private static final boolean DEBUG = false;

    private static final float DESIGN_PROGRESS_MARGIN = 80;

    private static final int COUNT_WORDS = 12;

    private static class CustomLayoutManager extends LinearLayoutManager {

        private boolean mIsScrollEnabled = true;

        public CustomLayoutManager(Context context) {
            super(context);
        }

        public void setScrollEnabled(boolean scrollEnabled) {
            mIsScrollEnabled = scrollEnabled;
        }

        @Override
        public boolean canScrollVertically() {
            return mIsScrollEnabled && super.canScrollVertically();
        }
    }

    private class RestoreServiceReceiver extends BroadcastReceiver {

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
                    case BackupService.MESSAGE_RESTORE_HEADER_INFO:
                        onMessageRestoreHeaderInfo(intent);
                        break;

                    case BackupService.MESSAGE_RESTORE_STATE:
                        onMessageRestoreState(intent);
                        break;

                    case BackupService.MESSAGE_RESTORE_ERROR:
                        onMessageRestoreError(intent);
                        break;

                    case BackupService.MESSAGE_VERIFY_REPORT:
                        onMessageVerifyReport(intent);
                        break;

                    case BackupService.MESSAGE_CHECK_FILE_COMPATIBILITY_RESULT:
                        onMessageCheckFileCompatibilityResult(intent);
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

    private View mCancelView;
    private RestoreAdapter mRestoreAdapter;
    private CustomLayoutManager mCustomLayoutManager;
    private final List<UIBackupWord> mBackupWords = new ArrayList<>();

    private boolean mCanRestore = false;
    private boolean mIsBackupHeaderInfoOK = false;
    private boolean mIsLastBackup = false;
    private String mBackupFilePath;
    private String mBackupFileName;
    private boolean mVerifyBackupMode = false;
    private boolean mIsVerifyBackupTerminated = false;

    private RestoreServiceReceiver mRestoreServiceReceiver;

    @Nullable
    private MnemonicCodeUtils mMnemonicCodeUtils;

    private RestoreState mRestoreState;
    private TerminateReason mTerminateReason;
    private BackupService.RestoreReport mRestoreReport;

    private int mCurrentWord = -1;

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

        // Listen to the BackupService messages.
        IntentFilter filter = new IntentFilter(Intents.INTENT_BACKUP_SERVICE_MESSAGE);
        mRestoreServiceReceiver = new RestoreServiceReceiver();

        // Register and avoid exporting the backup receiver.
        ContextCompat.registerReceiver(getBaseContext(), mRestoreServiceReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);

        if (savedInstanceState != null) {
            mBackupFilePath = savedInstanceState.getString(BackupService.BACKUP_SERVICE_FILE_PATH);
            mBackupFileName = savedInstanceState.getString(BackupService.BACKUP_SERVICE_FILE_NAME);
            mVerifyBackupMode = savedInstanceState.getBoolean(Intents.INTENT_BACKUP_VERIFY_MODE);
        } else {
            Intent intent = getIntent();
            mBackupFilePath = intent.getStringExtra(BackupService.BACKUP_SERVICE_FILE_PATH);
            mBackupFileName = intent.getStringExtra(BackupService.BACKUP_SERVICE_FILE_NAME);
            mVerifyBackupMode = intent.getBooleanExtra(Intents.INTENT_BACKUP_VERIFY_MODE, false);
        }

        if (mBackupFilePath == null) {
            finish();
            return;
        }

        checkFileCompatibility();

        initViews();
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
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

        unregisterReceiver(mRestoreServiceReceiver);
    }

    @Override
    public void onApplyInsetsFinish() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onApplyInsetsFinish");
        }

        super.onApplyInsetsFinish();

        if (mCancelView != null) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mCancelView.getLayoutParams();
            marginLayoutParams.bottomMargin = (int) (DESIGN_PROGRESS_MARGIN * Design.HEIGHT_RATIO) + getBarBottomInset();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSaveInstanceState: outState=" + outState);
        }

        super.onSaveInstanceState(outState);

        outState.putString(BackupService.BACKUP_SERVICE_FILE_PATH, mBackupFilePath);
        outState.putString(BackupService.BACKUP_SERVICE_FILE_NAME, mBackupFileName);
        outState.putBoolean(Intents.INTENT_BACKUP_VERIFY_MODE, mVerifyBackupMode);
    }

    public List<UIBackupWord> getBackupWords() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getBackupWords");
        }

        return mBackupWords;
    }

    public int getCurrentWord() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getCurrentWord");
        }

        return mCurrentWord;
    }

    public void initCurrentWord() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initCurrentWord");
        }

        if (mCurrentWord == -1) {
            mCurrentWord = 0;
        }
    }

    public String getBackupFileName() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getBackupFileName");
        }

        if (mBackupFileName != null) {
            return mBackupFileName;
        }

        Uri uri = Uri.parse(mBackupFilePath);
        FileInfo fileInfo = new FileInfo(this, uri);

        return fileInfo.getFilename();
    }

    public void onPasteWordsClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPasteWordsClick");
        }

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard.hasPrimaryClip() && clipboard.getPrimaryClipDescription() != null && (clipboard.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) || clipboard.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML))) {
            if (clipboard.getPrimaryClip() != null) {
                ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
                String content = item.getText().toString();

                final String[] words = content.split(" ");
                mBackupWords.clear();

                if (mMnemonicCodeUtils == null) {
                    mMnemonicCodeUtils = new MnemonicCodeUtils(this);
                }

                boolean incorrectWordToPaste = false;
                for (int index = 0; index < COUNT_WORDS; index++) {
                    if (index < words.length) {
                        String word = words[index];

                        List<String> search = mMnemonicCodeUtils.getSuggestions(word);
                        if (!search.isEmpty()) {
                            UIBackupWord backupWord = new UIBackupWord(words[index], index);
                            mBackupWords.add(backupWord);
                        } else {
                            incorrectWordToPaste = true;
                            UIBackupWord backupWord = new UIBackupWord(null, index);
                            mBackupWords.add(backupWord);
                        }
                    } else {
                        UIBackupWord backupWord = new UIBackupWord(null, index);
                        mBackupWords.add(backupWord);
                    }
                }

                if (incorrectWordToPaste) {
                    toast(getString(R.string.restore_view_paste_error));
                }
            }
        }

        mCanRestore = isAllWordsCompleted();
        mRestoreAdapter.notifyItemRangeChanged(1, 2);
    }

    public RestoreState getRestoreState() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getRestoreState");
        }

        return mRestoreState;
    }

    public BackupService.RestoreReport getRestoreReport() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getRestoreReport");
        }

        return mRestoreReport;
    }

    public SpannableStringBuilder getRestoreMessage() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getRestoreMessage");
        }

        if (mRestoreState == null) {
            return new SpannableStringBuilder( mVerifyBackupMode ? getString(R.string.restore_view_error_message_verify_backup) : getString(R.string.restore_view_error_message));
        }

        if (mTerminateReason != null && mTerminateReason == TerminateReason.ERROR) {
            return new SpannableStringBuilder();
        }

        String title = null;
        String message = "";
        switch (mRestoreState) {
            case STARTING:
                message = mVerifyBackupMode ? getString(R.string.restore_view_state_restore_verify_starting) : getString(R.string.restore_view_state_restore_starting);
                break;

            case RESTORE_ACCOUNT:
                message = mVerifyBackupMode ? getString(R.string.restore_view_state_restore_verify_account) : getString(R.string.restore_view_state_restore_account);
                break;

            case RESTORE_DATA:
                message = mVerifyBackupMode ? getString(R.string.restore_view_state_restore_verify_data) : getString(R.string.restore_view_state_restore_data);
                break;

            case WAIT_CONFIRM:
                message = getString(R.string.restore_view_state_restore_wait_confirm);
                break;

            case TERMINATED:
                if (mVerifyBackupMode) {
                    title = getString(R.string.backup_view_verify_completed);
                    if (mRestoreReport != null && mRestoreReport.isRestoreUpToDate()) {
                        if (mIsLastBackup) {
                            message = getString(R.string.backup_view_verify_up_to_date);
                        } else {
                            message = getString(R.string.restore_view_more_recent_backup) + "\n\n" + getString(R.string.backup_view_verify_up_to_date);
                        }
                    } else if (mRestoreReport != null) {
                        if (mIsLastBackup) {
                            message = getString(R.string.backup_view_verify_not_up_to_date) + "\n\n" + getString(R.string.backup_view_content_diff_message);
                        } else {
                            message = getString(R.string.restore_view_more_recent_backup) + "\n\n" + getString(R.string.backup_view_content_diff_message);
                        }
                    } else {
                        title = getString(R.string.application_canceled_operation);
                    }
                } else {
                    title = getString(R.string.restore_view_success);
                    message = getString(R.string.restore_view_success_message);
                }

                break;

            case CANCEL:
                if (!mVerifyBackupMode) {
                    title = getString(R.string.application_canceled_operation);
                    message = getString(R.string.account_migration_view_cancel_message);
                }

            default:
                break;
        }

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        if (title != null) {
            spannableStringBuilder.append(title);
            spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_DEFAULT), 0, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableStringBuilder.append("\n\n");

        }
        int startSubTitle = spannableStringBuilder.length();
        spannableStringBuilder.append(message);
        spannableStringBuilder.setSpan(new RelativeSizeSpan(0.94f), startSubTitle, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_GREY), startSubTitle, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return spannableStringBuilder;
    }

    public List<String> searchWords(String search) {
        if (DEBUG) {
            Log.d(LOG_TAG, "searchWords: " + search);
        }

        if (mMnemonicCodeUtils == null) {
            mMnemonicCodeUtils = new MnemonicCodeUtils(this);
        }

        return mMnemonicCodeUtils.getSuggestions(search);
    }

    public void selectWordSuggestions(String word) {
        if (DEBUG) {
            Log.d(LOG_TAG, "selectWordSuggestions: " + word);
        }

        if (mCurrentWord >= 0 && mCurrentWord < COUNT_WORDS) {
            UIBackupWord backupWord = mBackupWords.get(mCurrentWord);
            backupWord.setWord(word);
            nextCurrentWord();
            mRestoreAdapter.notifyItemChanged(1);
        }

        mCanRestore = isAllWordsCompleted();
        mRestoreAdapter.notifyItemChanged(2);
    }

    public void onWordClick(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onWordClick: " + position);
        }

        mCurrentWord = position;
        mRestoreAdapter.selectedWord();
    }

    public boolean canRestore() {
        if (DEBUG) {
            Log.d(LOG_TAG, "canRestore");
        }

        return mCanRestore;
    }

    public void onFooterActionClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onFooterActionClick");
        }

        if (mVerifyBackupMode && mIsVerifyBackupTerminated) {
            Intent intent = new Intent();
            intent.setClass(this, BackupActivity.class);
            startActivity(intent);
            finish();
        } else if (mCanRestore) {
            ViewGroup viewGroup = findViewById(R.id.restore_activity_layout);

            DefaultConfirmView defaultConfirmView = new DefaultConfirmView(this, null);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            defaultConfirmView.setLayoutParams(layoutParams);

            String title;
            String message;
            String actionTitle;
            if (mVerifyBackupMode) {
                title = getString(R.string.account_view_backup_verify);
                message = getString(R.string.restore_view_onboarding_verify);
                actionTitle = getString(R.string.application_confirm);
            } else {
                title = getString(R.string.deleted_account_view_warning);
                message = getString(R.string.restore_view_warning);
                actionTitle = getString(R.string.restore_view_restore);
            }

            defaultConfirmView.setTitle(title);
            defaultConfirmView.setMessage(message);

            defaultConfirmView.setImage(null);
            defaultConfirmView.setConfirmTitle(actionTitle);

            AbstractBottomSheetView.Observer observer = new AbstractBottomSheetView.Observer() {
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
                        startRestore();
                    }
                }
            };

            defaultConfirmView.setObserver(observer);
            viewGroup.addView(defaultConfirmView);
            defaultConfirmView.show();

            int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
            setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
        }
    }

    public void updateScroll(boolean canScroll) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateScroll");
        }

        mCustomLayoutManager.setScrollEnabled(canScroll);
    }

    protected boolean isVerifyBackupMode() {
        if (DEBUG) {
            Log.d(LOG_TAG, "isVerifyBackupMode");
        }

        return mVerifyBackupMode;
    }

    protected boolean isVerifyBackupTerminated() {
        if (DEBUG) {
            Log.d(LOG_TAG, "isVerifyBackupTerminated");
        }

        return mIsVerifyBackupTerminated;
    }


    //
    // Private methods
    //

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.restore_activity);

        setStatusBarColor();
        setToolBar(R.id.restore_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);
        setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        applyInsets(R.id.restore_activity_layout, R.id.restore_activity_tool_bar, R.id.restore_activity_background, Design.TOOLBAR_COLOR, false);

        if (mVerifyBackupMode) {
            setTitle(getString(R.string.account_view_backup_verify));
        } else {
            setTitle(getString(R.string.restore_view_title));
        }

        initBackupWords();

        View backgroundView = findViewById(R.id.restore_activity_background);
        backgroundView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        mRestoreAdapter = new RestoreAdapter(this);
        mCustomLayoutManager = new CustomLayoutManager(this);

        RecyclerView recyclerView = findViewById(R.id.restore_activity_list_view);
        recyclerView.setLayoutManager(mCustomLayoutManager);
        recyclerView.setAdapter(mRestoreAdapter);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setItemAnimator(null);
        recyclerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        mCancelView = findViewById(R.id.restore_activity_cancel_view);
        mCancelView.setOnClickListener(v -> onCancelClick());
        mCancelView.setVisibility(View.GONE);

        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable cancelViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        cancelViewBackground.getPaint().setColor(Design.BUTTON_RED_COLOR);
        mCancelView.setBackground(cancelViewBackground);

        ViewGroup.LayoutParams layoutParams = mCancelView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = Design.BUTTON_HEIGHT;

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mCancelView.getLayoutParams();
        marginLayoutParams.bottomMargin = (int) (DESIGN_PROGRESS_MARGIN * Design.HEIGHT_RATIO);

        TextView cancelTextView = findViewById(R.id.restore_activity_cancel_title_view);
        cancelTextView.setTypeface(Design.FONT_BOLD28.typeface);
        cancelTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_BOLD28.size);
        cancelTextView.setTextColor(Color.WHITE);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (DEBUG) {
                    Log.d(LOG_TAG, "handleOnBackPressed");
                }

                if (dismissBottomSheet(R.id.restore_activity_layout)) {
                    return;
                }

                if (mRestoreState == RestoreState.TERMINATED || mRestoreState == RestoreState.CANCEL) {
                    if (mVerifyBackupMode) {
                        finish();
                    } else {
                        terminateActivity();
                    }
                } else {
                    onCancelClick();
                }
            }
        });
    }

    private void initBackupWords() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initBackupWords");
        }

        for (int i = 0; i < COUNT_WORDS; i++) {
            mBackupWords.add(new UIBackupWord(null, i));
        }
    }

    private void onCancelClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCancelClick");
        }

        rollbackRestore();
    }

    private void onMessageRestoreHeaderInfo(@NonNull Intent intent) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onMessageRestoreHeaderInfo=" + intent);
        }

        BackupHeaderInfo backupHeaderInfo = (BackupHeaderInfo) intent.getSerializableExtra(BackupService.BACKUP_SERVICE_HEADER_INFO);
        UUID lastBackupId = (UUID) intent.getSerializableExtra(BackupService.BACKUP_SERVICE_LAST_BACKUP_ID);
        mIsBackupHeaderInfoOK = backupHeaderInfo != null && backupHeaderInfo.applicationName.equals("skred");
        mIsLastBackup = lastBackupId != null && backupHeaderInfo != null && lastBackupId.equals(backupHeaderInfo.backupId);
    }

    private void onMessageRestoreState(@NonNull Intent intent) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onMessageRestoreState=" + intent);
        }

        mRestoreState = (RestoreState) intent.getSerializableExtra(BackupService.BACKUP_SERVICE_RESTORE_STATE);
        if (mRestoreState == RestoreState.WAIT_CONFIRM) {
            mRestoreReport = (BackupService.RestoreReport) intent.getSerializableExtra(BackupService.BACKUP_SERVICE_RESTORE_REPORT);
        } else if (mRestoreState == RestoreState.TERMINATED) {
            TerminateReason terminateReason = (TerminateReason) intent.getSerializableExtra(BackupService.BACKUP_SERVICE_TERMINATE_REASON);
            mTerminateReason = terminateReason;
            if (terminateReason != TerminateReason.ERROR) {
                if (terminateReason == TerminateReason.CANCEL && !mVerifyBackupMode) {
                    mRestoreState = RestoreState.CANCEL;
                } else {
                    mRestoreState = RestoreState.TERMINATED;
                }
            }
        }

        updateRestoreState();
    }

    private void onMessageRestoreError(@NonNull Intent intent) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onMessageRestoreError=" + intent);
        }

        if (mTerminateReason != null && mTerminateReason == TerminateReason.ERROR) {
            return;
        }

        org.twinlife.twinlife.BackupService.ErrorCode errorCode = (org.twinlife.twinlife.BackupService.ErrorCode) intent.getSerializableExtra(BackupService.BACKUP_SERVICE_ERROR_CODE);
        if (errorCode == org.twinlife.twinlife.BackupService.ErrorCode.SYNC_FAILED) {
            return;
        }

        mTerminateReason = TerminateReason.ERROR;
        showRestoreError(errorCode);
    }

    private void onMessageVerifyReport(@NonNull Intent intent) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onMessageVerifyReport: intent=" + intent);
        }

        mRestoreState = RestoreState.TERMINATED;
        mRestoreReport = (BackupService.RestoreReport) intent.getSerializableExtra(BackupService.BACKUP_SERVICE_RESTORE_REPORT);

        updateRestoreState();
    }

    private void onMessageCheckFileCompatibilityResult(@NonNull Intent intent) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onMessageCheckFileCompatibilityResult: intent=" + intent);
        }

        org.twinlife.twinlife.BackupService.ErrorCode result = (org.twinlife.twinlife.BackupService.ErrorCode) intent.getSerializableExtra(BackupService.BACKUP_SERVICE_CHECK_FILE_COMPATIBILITY_RESULT);

        if (result != org.twinlife.twinlife.BackupService.ErrorCode.SUCCESS) {
            showRestoreError(org.twinlife.twinlife.BackupService.ErrorCode.INVALID_FILE);
        }
    }

    private void nextCurrentWord() {
        if (DEBUG) {
            Log.d(LOG_TAG, "nextCurrentWord");
        }

        for (int index = 0; index < mBackupWords.size(); index++) {
            UIBackupWord backupWord = mBackupWords.get(index);
            if (backupWord.getWord() == null) {
                mCurrentWord = index;
                break;
            }
        }
    }

    private void updateRestoreState() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateRestoreState");
        }

        if (mRestoreState == RestoreState.STARTING) {
            showBackButton(false);
            mCancelView.setVisibility(View.VISIBLE);
        } else if (mRestoreState == RestoreState.WAIT_CONFIRM) {
            if (!mVerifyBackupMode) {
                confirmRestore();
            }
        } else if (mRestoreState == RestoreState.TERMINATED) {
            if (mVerifyBackupMode) {
                mIsVerifyBackupTerminated = true;
                showBackButton(true);
            }

            mCancelView.setVisibility(View.INVISIBLE);
        }

        mRestoreAdapter.refreshContent();
    }

    private void showRestoreError(org.twinlife.twinlife.BackupService.ErrorCode errorCode) {
        if (DEBUG) {
            Log.d(LOG_TAG, "showRestoreError: " + errorCode);
        }

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);

        String message;
        Runnable runnable = () -> {
            if (mRestoreState == RestoreState.TERMINATED || mRestoreState == RestoreState.CANCEL) {
                if (mVerifyBackupMode) {
                    finish();
                } else {
                    terminateActivity();
                }
            }
        };

        if (errorCode == org.twinlife.twinlife.BackupService.ErrorCode.NO_SPACE_LEFT) {
            message = getString(R.string.application_error_no_storage_space);
        } else if (errorCode == org.twinlife.twinlife.BackupService.ErrorCode.INVALID_KEY) {
            message = getString(R.string.backup_view_error_words);
        } else if (errorCode == org.twinlife.twinlife.BackupService.ErrorCode.DIFFERENT_ACCOUNT) {
            message = getString(R.string.restore_view_verify_same_account);
        } else if (errorCode == org.twinlife.twinlife.BackupService.ErrorCode.INVALID_FILE && !mIsBackupHeaderInfoOK) {
            message = getString(R.string.restore_view_file_not_supported);
            runnable = this::finish;
        } else {
            message = mVerifyBackupMode ? getString(R.string.restore_view_error_message_verify_backup) : getString(R.string.restore_view_error_message);
        }

        showAlertMessageView(R.id.restore_activity_layout, getString(R.string.deleted_account_view_warning), message, false, runnable);
    }

    private boolean isAllWordsCompleted() {
        if (DEBUG) {
            Log.d(LOG_TAG, "isAllWordsCompleted");
        }

        if (mBackupWords == null) {
            return false;
        }

        for (UIBackupWord backupWord : mBackupWords) {
            if (backupWord.getWord() == null) {
                return false;
            }
        }

        return true;
    }

    private List<String> getWordsList() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getWordsList");
        }

        List<String> words = new ArrayList<>();

        for (UIBackupWord backupWord : mBackupWords) {
            if (backupWord.getWord() != null) {
                words.add(backupWord.getWord());
            }
        }

        return words;
    }

    private void checkFileCompatibility() {
        if (DEBUG) {
            Log.d(LOG_TAG, "checkFileCompatibility");
        }

        Intent intent = new Intent(BackupService.ACTION_CHECK_FILE_COMPATIBILITY);
        intent.setClass(this, BackupService.class);
        intent.putExtra(BackupService.PARAM_FILE_PATH, mBackupFilePath);

        startService(intent);
    }

    private void startRestore() {
        if (DEBUG) {
            Log.d(LOG_TAG, "startRestore");
        }

        mTerminateReason = null;

        Intent intent = new Intent();
        intent.setClass(this, BackupService.class);
        if (mVerifyBackupMode) {
            intent.setAction(BackupService.ACTION_VERIFY_BACKUP);
        } else {
            intent.setAction(BackupService.ACTION_START_RESTORE);
        }

        intent.putExtra(BackupService.PARAM_FILE_PATH, mBackupFilePath);

        if (mMnemonicCodeUtils == null) {
            mMnemonicCodeUtils = new MnemonicCodeUtils(this);
        }

        intent.putExtra(BackupService.PARAM_PASSWORD, mMnemonicCodeUtils.toEntropy(getWordsList()));

        startService(intent);
    }

    private void commitRestore() {
        if (DEBUG) {
            Log.d(LOG_TAG, "commitRestore");
        }

        Intent intent = new Intent();
        intent.setClass(this, BackupService.class);
        intent.setAction(BackupService.ACTION_COMMIT_RESTORE);
        startService(intent);
    }

    private void rollbackRestore() {
        if (DEBUG) {
            Log.d(LOG_TAG, "rollbackRestore");
        }

        Intent intent = new Intent();
        intent.setClass(this, BackupService.class);
        intent.setAction(BackupService.ACTION_CANCEL_RESTORE);
        startService(intent);
    }

    private void confirmRestore() {
        if (DEBUG) {
            Log.d(LOG_TAG, "confirmRestore");
        }

        ViewGroup viewGroup = findViewById(R.id.restore_activity_layout);

        BackupContentConfirmView backupContentConfirmView = new BackupContentConfirmView(this, null);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        backupContentConfirmView.setLayoutParams(layoutParams);
        backupContentConfirmView.setTitle(getString(R.string.deleted_account_view_warning));
        backupContentConfirmView.setConfirmTitle(getString(R.string.application_confirm));
        backupContentConfirmView.initRestoreReport(mRestoreReport, mIsLastBackup);

        AbstractBottomSheetView.Observer observer = new AbstractBottomSheetView.Observer() {
            @Override
            public void onConfirmClick() {

                commitRestore();
                backupContentConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCancelClick() {
                rollbackRestore();
                backupContentConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onDismissClick() {
                rollbackRestore();
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

    private void showBackupHeaderInfoError() {
        if (DEBUG) {
            Log.d(LOG_TAG, "showBackupHeaderInfoError");
        }

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
        showAlertMessageView(R.id.restore_activity_layout, getString(R.string.deleted_account_view_warning), getString(R.string.restore_view_application_error), false, this::rollbackRestore);
    }

    private void terminateActivity() {
        if (DEBUG) {
            Log.d(LOG_TAG, "terminateActivity");
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.setClass(this, SplashScreenActivity.class);
        startActivity(intent);
        finish();

        TwinmeApplication twinmeApplication = (TwinmeApplication) getApplication();
        twinmeApplication.stop();
    }
}
