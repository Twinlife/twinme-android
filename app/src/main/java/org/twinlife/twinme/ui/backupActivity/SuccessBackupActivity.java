/*
 *  Copyright (c) 2024-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.backupActivity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.services.BackupService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.conversationActivity.NamedFileProvider;
import org.twinlife.twinme.utils.AbstractBottomSheetView;
import org.twinlife.twinme.utils.OnboardingConfirmView;
import org.twinlife.twinme.utils.SaveAsyncTask;
import org.twinlife.twinme.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SuccessBackupActivity extends AbstractTwinmeActivity {
    private static final String LOG_TAG = "SuccessBackupActivity";
    private static final boolean DEBUG = false;

    private static final int REQUEST_SAVE_BACKUP = 1;

    private final List<UIBackupWord> mBackupWords = new ArrayList<>();
    private String mBackupFilePath;

    private boolean mShowSuccessView = false;

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
        mBackupFilePath = intent.getStringExtra(BackupService.BACKUP_SERVICE_FILE_PATH);

        if (mBackupFilePath == null) {
            finish();
        }

        String words = intent.getStringExtra(BackupService.BACKUP_SERVICE_PASSWORD_WORDS);

        if (words != null) {
            initBackupWords(words);
        }

        initViews();
    }

    @Override
    public void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        super.onResume();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            if (!mShowSuccessView) {
                getTwinmeApplication().setLastBackupDate();
                mShowSuccessView = true;
                showSuccessView();
            }
        }
    }

    @Override
    public void onApplyInsetsFinish() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onApplyInsetsFinish");
        }

        super.onApplyInsetsFinish();

        if (!mShowSuccessView) {
            getTwinmeApplication().setLastBackupDate();
            mShowSuccessView = true;
            showSuccessView();
        }
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        File file = new File(mBackupFilePath);
        runOnTwinlifeThread(() -> {
            if (file.exists() && !file.delete()) {
                Log.w(LOG_TAG, "Cannot remove backup");
            }
        });

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateOptionsMenu: menu=" + menu);
        }

        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.onboarding_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.info_action);
        ImageView imageView = (ImageView) menuItem.getActionView();

        if (imageView != null) {
            imageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.onboarding_info_icon, null));
            imageView.setColorFilter(Color.WHITE);
            imageView.setPadding(Design.TOOLBAR_IMAGE_ITEM_PADDING, 0, Design.TOOLBAR_IMAGE_ITEM_PADDING, 0);
            imageView.setOnClickListener(view -> showSuccessView());
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onActivityResult: requestCode=" + requestCode + " resultCode=" + resultCode + " intent=" + intent);
        }

        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_OK && requestCode == REQUEST_SAVE_BACKUP) {
            Uri fileUri = (intent == null) ? null : intent.getData();
            if (fileUri != null) {
                saveFileToUri(fileUri);
            }
        }
    }

    public void onShareClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onShareClick");
        }

        File file = new File(mBackupFilePath);
        Uri uri = NamedFileProvider.getInstance().getUriForFile(this, file, file.getName());

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("application/octet-stream");

        if (mBackupFilePath != null) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
        }

        startActivity(Intent.createChooser(intent, null));
    }

    public void onCopyClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCopyClick");
        }

        Utils.setClipboard(this, getWordsList());
        Toast.makeText(this, R.string.conversation_activity_menu_item_view_copy_message, Toast.LENGTH_SHORT).show();
    }

    public void onSaveFileClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSaveFileClick");
        }

        File fileToSave = new File(mBackupFilePath);
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_TITLE, fileToSave.getName());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, getTwinmeApplication().defaultUriToSaveFiles());
        }

        startActivityForResult(intent, REQUEST_SAVE_BACKUP);
    }

    public String getBackupFileName() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getBackupFileName");
        }

        Uri uri = Uri.parse(mBackupFilePath);
        return uri.getLastPathSegment();
    }

    //
    // Private methods
    //

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.success_backup_activity);

        setStatusBarColor();
        setToolBar(R.id.success_backup_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);
        setBackgroundColor(Design.WHITE_COLOR);

        setTitle(getString(R.string.backup_activity_title));

        applyInsets(R.id.success_backup_activity_layout, R.id.success_backup_activity_tool_bar, R.id.success_backup_activity_list_view, Design.TOOLBAR_COLOR, false);

        SuccessBackupAdapter successBackupAdapter = new SuccessBackupAdapter(this, mBackupWords);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        RecyclerView recyclerView = findViewById(R.id.success_backup_activity_list_view);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(successBackupAdapter);
        recyclerView.setItemAnimator(null);
        recyclerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
    }

    private void initBackupWords(@NonNull String words) {
        if (DEBUG) {
            Log.d(LOG_TAG, "initBackupWords");
        }

        String[] backupWords = words.split(" ");
        int position = 0;
        for (String word : backupWords) {
            UIBackupWord backupWord = new UIBackupWord(word, position);
            mBackupWords.add(backupWord);
            position++;
        }
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

    private void showSuccessView() {
        if (DEBUG) {
            Log.d(LOG_TAG, "showSuccessView");
        }

        ViewGroup viewGroup = findViewById(R.id.success_backup_activity_layout);

        OnboardingConfirmView onboardingConfirmView = new OnboardingConfirmView(this, null);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        onboardingConfirmView.setLayoutParams(layoutParams);

        String title = getString(R.string.backup_activity_success);
        String message = getString(R.string.backup_activity_save_file_message)
                + "\n\n" + getString(R.string.backup_activity_verify_message);

        onboardingConfirmView.setTitle(title);
        onboardingConfirmView.setImage(ResourcesCompat.getDrawable(getResources(), R.drawable.onboarding_backup, null));
        onboardingConfirmView.setMessage(message);
        onboardingConfirmView.setConfirmTitle(getString(R.string.application_save));

        AbstractBottomSheetView.Observer observer = new AbstractBottomSheetView.Observer() {
            @Override
            public void onConfirmClick() {
                onboardingConfirmView.animationCloseConfirmView();
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

                if (fromConfirmAction) {
                    onSaveFileClick();
                }
            }
        };
        onboardingConfirmView.setObserver(observer);
        viewGroup.addView(onboardingConfirmView);
        onboardingConfirmView.show();

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }

    private void saveFileToUri(Uri uri) {
        if (DEBUG) {
            Log.d(LOG_TAG, "saveFileToUri " + uri);
        }

        File path = new File(mBackupFilePath);
        SaveAsyncTask save = new SaveAsyncTask(this, path, uri);
        save.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}