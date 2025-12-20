/*
 *  Copyright (c) 2023-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui.exportActivity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.text.format.Formatter;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.util.Logger;
import org.twinlife.twinlife.util.Utils;
import org.twinlife.twinme.export.ExportService;
import org.twinlife.twinme.export.ExportState;
import org.twinlife.twinme.export.ExportStats;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.Settings;
import org.twinlife.twinme.ui.premiumServicesActivity.PremiumFeatureConfirmView;
import org.twinlife.twinme.ui.premiumServicesActivity.UIPremiumFeature;
import org.twinlife.twinme.ui.settingsActivity.UISetting;
import org.twinlife.twinme.utils.AbstractBottomSheetView;
import org.twinlife.twinme.utils.FileInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ExportActivity extends AbstractTwinmeActivity {
    private static final String LOG_TAG = "ExportActivity";
    private static final boolean DEBUG = false;

    private static final int REQUEST_PICK_DOCUMENT = 1;

    private class ExportServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent == null || intent.getExtras() == null) {

                return;
            }

            final String event = intent.getStringExtra(ExportService.MESSAGE_EVENT);
            if (event != null) {
                if (DEBUG) {
                    Log.d(LOG_TAG, "Received event=" + event);
                }

                // Catch exception in case an external app succeeds in sending a message.
                try {
                    ExportState state = (ExportState) intent.getSerializableExtra(ExportService.MESSAGE_STATE);
                    ExportStats stats = (ExportStats) intent.getSerializableExtra(ExportService.MESSAGE_STATS);
                    String exportName = intent.getStringExtra(ExportService.MESSAGE_EXPORT_NAME);
                    int progress = intent.getIntExtra(ExportService.MESSAGE_PROGRESS, 0);
                    ExportActivity.this.onProgress(state, stats, progress, exportName);
                } catch (Exception exception) {
                    if (Logger.WARN) {
                        Log.w(LOG_TAG, "Exception", exception);
                    }
                }
            }

            final String message = intent.getStringExtra(ExportService.MESSAGE_ERROR);
            if (message != null) {
                ExportActivity.this.onError(message);
            }
        }
    }

    private ExportAdapter mExportAdapter;

    private final List<UIExport> mExports = new ArrayList<>();

    private boolean mUIInitialized = false;
    private boolean mUIPostInitialized = false;

    private boolean mIsContentToExport = false;
    private boolean mIsExportInProgress = false;
    private boolean mIsExportOneConversation = true;
    private ExportServiceReceiver mExportReceiver;
    private Uri mExportUri;
    @Nullable
    private String mExportName;

    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        initViews();

        // Listen to the Exportervice messages.
        IntentFilter filter = new IntentFilter(Intents.INTENT_EXPORT_SERVICE_MESSAGE);
        mExportReceiver = new ExportServiceReceiver();

        // Register and avoid exporting the export receiver.
        ContextCompat.registerReceiver(getBaseContext(), mExportReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);

        Intent intent = getIntent();
        UUID contactId = Utils.UUIDFromString(intent.getStringExtra(Intents.INTENT_CONTACT_ID));
        UUID groupId = Utils.UUIDFromString(intent.getStringExtra(Intents.INTENT_GROUP_ID));
        UUID spaceId = Utils.UUIDFromString(intent.getStringExtra(Intents.INTENT_SPACE_ID));

        Intent serviceIntent = new Intent(this, ExportService.class);
        if (spaceId != null) {
            serviceIntent.putExtra(ExportService.PARAM_SPACE_ID, spaceId);
            mIsExportOneConversation = false;
        }
        if (groupId != null) {
            serviceIntent.putExtra(ExportService.PARAM_GROUP_ID, groupId);
        }
        if (contactId != null) {
            serviceIntent.putExtra(ExportService.PARAM_CONTACT_ID, contactId);
        }
        serviceIntent.setAction(ExportService.ACTION_SCAN);
        startService(serviceIntent);
    }

    //
    // Override Activity methods
    //

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onWindowFocusChanged: hasFocus=" + hasFocus);
        }

        if (hasFocus && mUIInitialized && !mUIPostInitialized) {
            postInitViews();
        }
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        // Ask the service to stop (but don't stop the export if it is running).
        Intent serviceIntent = new Intent(this, ExportService.class);
        serviceIntent.setAction(ExportService.ACTION_STOP);
        startService(serviceIntent);

        unregisterReceiver(mExportReceiver);

        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onActivityResult: requestCode=" + requestCode + " requestCode=" + resultCode + " intent=" + intent);
        }

        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_PICK_DOCUMENT) {
                Uri uri = intent.getData();

                // Save the selected URI as is (do not modify or store something else).
                if (uri != null) {
                    Settings.defaultDirectoryToExport.setString(uri.toString()).save();
                }
                mExportUri = uri;
                Intent serviceIntent = new Intent(this, ExportService.class);

                serviceIntent.putExtra(ExportService.PARAM_EXPORT_URI, mExportUri);
                serviceIntent.setAction(ExportService.ACTION_EXPORT);
                startService(serviceIntent);
            }
        }
    }

    public void onEditSetting(@NonNull UISetting<String> setting, String value) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onEditSetting: setting=" + setting + " value=" + value);
        }
    }

    public void onActionClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onActionClick");
        }

        ViewGroup viewGroup = findViewById(R.id.export_activity_layout);

        PremiumFeatureConfirmView premiumFeatureConfirmView = new PremiumFeatureConfirmView(this, null);
        premiumFeatureConfirmView.initWithPremiumFeature(new UIPremiumFeature(this, UIPremiumFeature.FeatureType.CONVERSATION));

        AbstractBottomSheetView.Observer observer = new AbstractBottomSheetView.Observer() {
            @Override
            public void onConfirmClick() {
                premiumFeatureConfirmView.redirectStore();
            }

            @Override
            public void onCancelClick() {
                premiumFeatureConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onDismissClick() {
                premiumFeatureConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                viewGroup.removeView(premiumFeatureConfirmView);
                setStatusBarColor();
            }
        };
        premiumFeatureConfirmView.setObserver(observer);
        viewGroup.addView(premiumFeatureConfirmView);
        premiumFeatureConfirmView.show();

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }

    public boolean isExportInProgress() {
        if (DEBUG) {
            Log.d(LOG_TAG, "isExportInProgress");
        }

        return mIsExportInProgress;
    }

    public boolean canExport() {
        if (DEBUG) {
            Log.d(LOG_TAG, "canExport");
        }

        for (UIExport export : mExports) {
            if (export.isChecked() && export.getCount() > 0) {
                return true;
            }
        }

        return false;
    }

    @SuppressLint("DefaultLocale")
    public String getExportInformation() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getExportInformation");
        }

        if (!mIsContentToExport) {
            return getString(R.string.export_activity_no_content_to_export);
        }

        boolean isOneContentToExportIsChecked = false;

        long totalSize = 0;
        long totalCountFile = 0;
        long totalCountMessage = 0;

        for (UIExport export : mExports) {

            if (export.isChecked() && export.getCount() > 0) {
                isOneContentToExportIsChecked = true;
                if (export.getExportContentType() == UIExport.ExportContentType.MESSAGE) {
                    totalCountMessage = export.getCount();
                } else {
                    totalSize += export.getSize();
                    totalCountFile += export.getCount();
                }
            }
        }

        StringBuilder builder = new StringBuilder();

        if (mIsExportOneConversation) {
            builder.append(getString(R.string.export_activity_one_conversation_zip_file));
        } else {
            builder.append(getString(R.string.export_activity_all_conversations_zip_file));
        }

        if (isOneContentToExportIsChecked) {
            builder.append("\n");
            builder.append(getString(R.string.export_activity_content_to_export));
            builder.append(" : ");

            if (totalCountMessage > 0) {
                String messageTitle;
                if (totalCountMessage > 1) {
                    messageTitle = getString(R.string.settings_activity_chat_category_title);
                } else {
                    messageTitle = getString(R.string.feedback_activity_message);
                }

                builder.append(String.format("%d %s%s", totalCountMessage, messageTitle, totalCountFile > 0 ? " - " : ""));
            }

            if (totalCountFile > 0) {
                builder.append(Formatter.formatFileSize(this, totalSize));
            }
        }
        return builder.toString();
    }

    public void onProgress(@NonNull ExportState state, @NonNull ExportStats stats, int progress, @Nullable String exportName) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onProgress: " + state + " stats=" + stats + " progress=" + progress + " exportName=" + exportName);
        }

        if (state == ExportState.EXPORT_DONE) {
            Toast.makeText(this, R.string.export_activity_success, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mIsExportInProgress = state == ExportState.EXPORT_EXPORTING;
        if (mIsExportInProgress) {
            // We are exporting now, show the progress bar.
            mExportAdapter.setExportProgress(progress);
        }
        
        if (state == ExportState.EXPORT_WAIT && mExportName == null) {
            prepareExport();
        }

        if (exportName != null) {
            mExportName = exportName;
        }
        updateContent(stats);
    }

    public void onError(@NonNull String message) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onError: " + message);
        }

        Log.e(LOG_TAG, "OOPS: " + message);
    }

    //
    // Private methods
    //

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.export_activity);

        setStatusBarColor();
        setToolBar(R.id.export_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);
        setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        setTitle(getString(R.string.export_activity_title));
        applyInsets(R.id.export_activity_layout, R.id.export_activity_tool_bar, R.id.export_activity_list_view, Design.TOOLBAR_COLOR, false);

        initExport();

        @SuppressLint("NotifyDataSetChanged")
        ExportAdapter.OnExportClickListener exportClickListener = export -> {
                if (export.getCount() > 0) {
                    export.setChecked(!export.isChecked());
                    mExportAdapter.notifyDataSetChanged();
                prepareExport();
            }
        };

        mExportAdapter = new ExportAdapter(this, mExports, exportClickListener);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        RecyclerView recyclerView = findViewById(R.id.export_activity_list_view);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(mExportAdapter);
        recyclerView.setItemAnimator(null);
        recyclerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        mUIInitialized = true;
    }

    private void postInitViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "postInitViews");
        }

        mUIPostInitialized = true;
    }

    private void initExport() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initExport");
        }

        mExports.clear();
        mExports.add(new UIExport(UIExport.ExportContentType.MESSAGE, R.drawable.tab_bar_chat_grey, true));
        mExports.add(new UIExport(UIExport.ExportContentType.IMAGE, R.drawable.toolbar_picture_grey, true));
        mExports.add(new UIExport(UIExport.ExportContentType.VIDEO, R.drawable.history_video_call, true));
        mExports.add(new UIExport(UIExport.ExportContentType.AUDIO, R.drawable.toolbar_microphone_grey, true));
        mExports.add(new UIExport(UIExport.ExportContentType.FILE, R.drawable.toolbar_file_grey, true));
    }

    private void updateContent(ExportStats stats) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateContent: " + stats);
        }

        for (UIExport export : mExports) {
            switch (export.getExportContentType()) {
                case MESSAGE:
                    export.setCount(stats.msgCount);
                    export.setSize(0);
                    break;

                case IMAGE:
                    export.setCount(stats.imageCount);
                    export.setSize(stats.imageSize);
                    break;

                case VIDEO:
                    export.setCount(stats.videoCount);
                    export.setSize(stats.videoSize);
                    break;

                case AUDIO:
                    export.setCount(stats.audioCount);
                    export.setSize(stats.audioSize);
                    break;

                case FILE:
                    export.setCount(stats.fileCount);
                    export.setSize(stats.fileSize);
                    break;
            }

            if (export.getCount() > 0) {
                mIsContentToExport = true;
            }
        }

        mExportAdapter.setExports(mExports);
    }

    /**
     * Get the user printable name of the current export directory.
     *
     * Note: for Android >= 5.0, we cannot provide a default export directory because
     * we must ask the user to choose a directory and grant the write permission.
     * For Android 4.x, we can use the Download directory as default.
     *
     * @return the export directory printable name or null.
     */
    public String getExportDirectoryName() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getExportDirectoryName");
        }

        if (mExportUri == null) {

            return null;
        }

        String name = null;
        try {
            Uri documentUri = DocumentsContract.buildDocumentUriUsingTree(mExportUri, DocumentsContract.getTreeDocumentId(mExportUri));
            String[] projection = {DocumentsContract.Document.COLUMN_DISPLAY_NAME};

            Cursor cursor = getContentResolver().query(documentUri, projection, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    if (name == null) {
                        name = FileInfo.getColumnString(cursor, DocumentsContract.Document.COLUMN_DISPLAY_NAME);
                    }
                }
                cursor.close();
            }
        } catch (Exception exception) {
            // This directory is no longer valid or something went wrong, choose another one.
            Log.e(LOG_TAG, "Exception: ", exception);
            name = null;
        }
        if (name == null) {
            mExportUri = null;
        }
        return name;
    }

    /**
     * Prepare the export by telling the ExportService which contents are selected.
     */
    private void prepareExport() {
        if (DEBUG) {
            Log.d(LOG_TAG, "prepareExport");
        }

        Intent serviceIntent = new Intent(this, ExportService.class);

        StringBuilder types = new StringBuilder();
        for (UIExport export : mExports) {

            if (export.isChecked() && export.getCount() > 0) {
                if (types.length() > 0) {
                    types.append(",");
                }
                switch (export.getExportContentType()) {
                    case MESSAGE:
                        types.append("message");
                        break;

                    case IMAGE:
                        types.append("image");
                        break;

                    case VIDEO:
                        types.append("video");
                        break;

                    case AUDIO:
                        types.append("audio");
                        break;

                    case FILE:
                        types.append("file");
                        break;
                }
            }
        }
        serviceIntent.putExtra(ExportService.PARAM_FILTER_TYPES, types.toString());
        serviceIntent.setAction(ExportService.ACTION_PREPARE);
        startService(serviceIntent);
    }
}
