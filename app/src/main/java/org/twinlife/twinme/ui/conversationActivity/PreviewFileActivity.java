/*
 *  Copyright (c) 2024-2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Romain Kolb (romain.kolb@skyrock.com)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui.conversationActivity;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.core.content.FileProvider;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import org.twinlife.device.android.twinme.BuildConfig;
import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.BaseService.ErrorCode;
import org.twinlife.twinlife.ImageService;
import org.twinlife.twinlife.Twinlife;
import org.twinlife.twinme.models.CertificationLevel;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Group;
import org.twinlife.twinme.models.Originator;
import org.twinlife.twinme.skin.CircularImageDescriptor;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.TwinmeApplication;
import org.twinlife.twinme.utils.AlertMessageView;
import org.twinlife.twinme.utils.FileInfo;
import org.twinlife.twinme.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PreviewFileActivity extends AbstractPreviewActivity {
    private static final String LOG_TAG = "PreviewMediaActivity";
    private static final boolean DEBUG = false;

    private static final int REQUEST_GET_FILE = 1;

    private class MediaLinearLayoutManager extends LinearLayoutManager {

        public MediaLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        @Override
        public boolean canScrollHorizontally() {

            return mCanScroll;
        }
    }

    @Nullable
    private PreviewFileAdapter mPreviewFileAdapter;
    private PreviewThumbnailAdapter mPreviewThumbnailAdapter;
    private int mCurrentPosition = 0;

    private boolean mCanScroll = true;

    private LinearLayoutManager mLinearLayoutManager;

    private final ArrayList<FileInfo> mFiles = new ArrayList<>();
    private final ArrayList<FileInfo> mReducedFiles = new ArrayList<>();

    private TwinmeContextObserver mObserver;

    private ActivityResultLauncher<PickVisualMediaRequest> mMediaPicker;

    private int mCountFiles = 0;
    private String mApplicationFileUri;

    //
    // Override TwinmeActivityImpl methods
    //

    @UnstableApi
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mAllowCopy = intent.getBooleanExtra(Intents.INTENT_ALLOW_COPY_FILE, getTwinmeApplication().fileCopyAllowed());
        mAllowEphemeralMessage = intent.getBooleanExtra(Intents.INTENT_ALLOW_EPHEMERAL, false);
        mExpireTimeout = intent.getLongExtra(Intents.INTENT_EXPIRE_TIMEOUT, 0);
        mPreviewStartWithMedia = intent.getBooleanExtra(Intents.INTENT_PREVIEW_START_WITH_MEDIA, false);

        mOriginatorId = Utils.UUIDFromString(intent.getStringExtra(Intents.INTENT_CONTACT_ID));
        mInitMessage = intent.getStringExtra(Intents.INTENT_TEXT_MESSAGE);

        mIsQualityMediaOriginal = getTwinmeApplication().qualityMedia() == TwinmeApplication.QualityMedia.ORIGINAL.ordinal();

        // Get our application URI prefix so that `importFile()` can recognize our file and avoid a copy by using the direct file access.
        // This only concerns media files created by `takePhoto()` and `takeVideo()` from ConversationActivity.
        File dir = new File(getFilesDir(), Twinlife.TMP_DIR + "/t.jpg");
        mApplicationFileUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", dir).toString();
        mApplicationFileUri = mApplicationFileUri.substring(0, mApplicationFileUri.lastIndexOf('/'));

        initViews();

        ArrayList<String> urisToString = intent.getStringArrayListExtra(Intents.INTENT_SELECTED_URI);

        if (urisToString != null) {
            for (String uri : urisToString) {
                // Import the file locally immediately to avoid a SecurityException later.
                getTwinmeContext().execute(() -> importFile(Uri.parse(uri)));
            }
        }

        mObserver = new TwinmeContextObserver();
        getTwinmeContext().setObserver(mObserver);

        mMediaPicker = registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(10), uris -> {
            if (!uris.isEmpty()) {
                addPreviewMedia(uris);
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        super.onDestroy();

        // Make sure we don't leave a file of a picture/video when we leave.
        cleanupMediaInfo();

        if (mObserver != null) {
            getTwinmeContext().removeObserver(mObserver);
        }
    }

    @Override
    protected void onPause() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPause");
        }

        super.onPause();

        if (mPreviewFileAdapter != null) {
            mPreviewFileAdapter.pausePlayer();
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onConfigurationChanged: newConfig=" + newConfig);
        }

        super.onConfigurationChanged(newConfig);

        if (mPreviewFileAdapter != null) {
            mPreviewFileAdapter.updateFiles();
        }
    }

    @UnstableApi
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onActivityResult: requestCode=" + requestCode + " resultCode=" + resultCode + " intent=" + intent);
        }

        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_OK && requestCode == REQUEST_GET_FILE) {
            ClipData clipData = (intent == null) ? null : intent.getClipData();
            if (clipData != null && clipData.getItemCount() > 0) {
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    addFiles(clipData.getItemAt(i).getUri());
                }
            } else {
                // single selection or old android
                Uri uri = (intent == null) ? null : intent.getData();
                if (uri != null) {
                    addFiles(uri);
                }
            }
        }
    }

    public void onImageScaleStateChanged(boolean isScale) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onImageScaleStateChanged");
        }

        mCanScroll = !isScale;
    }

    public void onVideoSeekBarUpdate(boolean touch) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onVideoSeekBarUpdate");
        }

        mCanScroll = !touch;
    }

    public int getCurrentPosition() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getCurrentPosition");
        }

        return mCurrentPosition;
    }

    @Override
    public void finish() {
        if (DEBUG) {
            Log.d(LOG_TAG, "finish");
        }

        super.finish();

        if (mPreviewFileAdapter != null) {
            mPreviewFileAdapter.stopPlayer();
        }
    }

    @UnstableApi
    @Override
    public void send() {
        if (DEBUG) {
            Log.d(LOG_TAG, "send");
        }

        if (mPreviewStartWithMedia && !mIsQualityMediaOriginal) {
            prepareMediaBeforeSend();
        } else {
            sendIntent();
        }
    }

    @Override
    protected long totalFilesSize() {
        if (DEBUG) {
            Log.d(LOG_TAG, "totalFilesSize");
        }

        long totalSize = 0;

        for (FileInfo fileInfo : mFiles) {
            if (fileInfo.getSize() > 0) {
                totalSize += fileInfo.getSize();
            }
        }

        return totalSize;
    }

    //
    // Private methods
    //

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());

        setContentView(R.layout.preview_media_activity);

        setStatusBarColor(Color.BLACK);
        setBackgroundColor(Color.BLACK);

        Window window = getWindow();
        window.setNavigationBarColor(Color.BLACK);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        applyInsets(R.id.preview_activity_layout, -1, R.id.preview_activity_content_send_view, Color.BLACK, false);

        super.initViews();

        final LinearLayoutManager thumbnailLinearLayoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);

        mPreviewFileAdapter = new PreviewFileAdapter(this, mFiles);
        mLinearLayoutManager = new  MediaLinearLayoutManager(this, RecyclerView.HORIZONTAL, false);

        RecyclerView mediaListView = findViewById(R.id.preview_activity_list_view);
        mediaListView.setLayoutManager(mLinearLayoutManager);
        mediaListView.setAdapter(mPreviewFileAdapter);
        mediaListView.setItemAnimator(null);

        SnapHelper pagerSnapHelper = new PagerSnapHelper();
        pagerSnapHelper.attachToRecyclerView(mediaListView);

        mediaListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    View centerView = pagerSnapHelper.findSnapView(mLinearLayoutManager);
                    if (centerView != null) {
                        int prevPosition = mCurrentPosition;
                        mCurrentPosition = mLinearLayoutManager.getPosition(centerView);
                        mPreviewThumbnailAdapter.notifyItemChanged(prevPosition);
                        mPreviewThumbnailAdapter.notifyItemChanged(mCurrentPosition);

                        thumbnailLinearLayoutManager.scrollToPosition(mCurrentPosition);
                    }
                }
            }
        });

        PreviewThumbnailAdapter.OnPreviewThumbnailListener onPreviewThumbnailListener = new PreviewThumbnailAdapter.OnPreviewThumbnailListener() {
            @Override
            public void onAddFileClick() {
                onAddClick();
            }

            @Override
            public void onSelectFileClick(int position) {

                if (position >= 0 && position < mFiles.size()) {
                    onFileClick(position);
                }
            }
        };

        mPreviewThumbnailAdapter = new PreviewThumbnailAdapter(this, mFiles, onPreviewThumbnailListener);

        RecyclerView thumbnailListView = findViewById(R.id.preview_activity_thumbnail_list_view);
        thumbnailListView.setLayoutManager(thumbnailLinearLayoutManager);
        thumbnailListView.setAdapter(mPreviewThumbnailAdapter);
        thumbnailListView.setItemAnimator(null);

        ViewGroup.LayoutParams layoutParams = thumbnailListView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_THUMBNAIL_HEIGHT * Design.HEIGHT_RATIO);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) thumbnailListView.getLayoutParams();
        marginLayoutParams.bottomMargin = (int) (DESIGN_THUMBNAIL_MARGIN * Design.HEIGHT_RATIO);

        if (mPreviewStartWithMedia) {
            mQualityView.setVisibility(View.VISIBLE);
        } else {
            mQualityView.setVisibility(View.GONE);
        }

        mProgressBarView = findViewById(R.id.preview_activity_progress_bar);
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    protected void updateViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateViews");
        }

        if (!mFiles.isEmpty()) {
            mEditText.setVisibility(View.VISIBLE);
            mSendView.setVisibility(View.VISIBLE);
        } else {
            mEditText.setVisibility(View.GONE);
            mSendView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onTwinlifeReady() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onTwinlifeReady");
        }

        if (mOriginatorId != null && mOriginator == null) {
            getTwinmeContext().getOriginator(mOriginatorId, (ErrorCode error, Originator originator) -> {

                if (error != ErrorCode.SUCCESS || originator == null) {
                    Log.w(LOG_TAG, "Couldn't get originator " + mOriginatorId + ": " + error);
                    return;
                }

                mOriginator = originator;

                if (mOriginator.getAvatarId() != null) {
                    mContactAvatar = getTwinmeContext().getImageService().getImage(mOriginator.getAvatarId(), ImageService.Kind.THUMBNAIL);

                    if (mContactAvatar == null) {
                        if (mOriginator instanceof Group) {
                            mContactAvatar = getTwinmeApplication().getDefaultGroupAvatar();
                        } else {
                            mContactAvatar  = getTwinmeApplication().getDefaultAvatar();
                        }
                    }
                }

                mContactName = mOriginator.getName();

                mIsCertified = mOriginator instanceof Contact &&
                        ((Contact) mOriginator).getCertificationLevel() == CertificationLevel.LEVEL_4;

                runOnUiThread(() -> {
                    mAvatarView.setImage(this, null,
                            new CircularImageDescriptor(mContactAvatar, 0.5f, 0.5f, 0.5f));

                    mNameView.setText(mContactName);

                    if (mIsCertified) {
                        mCertifiedImageView.setVisibility(View.VISIBLE);
                    } else {
                        mCertifiedImageView.setVisibility(View.GONE);
                    }
                });
            });
        }
    }

    /**
     * Cleanup the media info file to make sure we don't use and keep unused files on the filesystem when we leave.
     */
    protected void cleanupMediaInfo() {
        if (DEBUG) {
            Log.d(LOG_TAG, "cleanupMediaInfo");
        }

        if (!mFiles.isEmpty()) {
            cleanupFiles(mFiles);
        }

        if (!mReducedFiles.isEmpty()) {
            cleanupFiles(mReducedFiles);
        }
    }

    protected void cleanupFiles(List<FileInfo> files) {
        if (DEBUG) {
            Log.d(LOG_TAG, "cleanupFiles");
        }

        for (FileInfo fileInfo : files) {
            // If there is a previous picture file, remove it to cleanup the file system.
            if (fileInfo.isFile()) {
                fileInfo.removeFile();
            }
        }

    }

    @UnstableApi
    @WorkerThread
    private void importFile(@NonNull Uri uri) {
        if (DEBUG) {
            Log.d(LOG_TAG, "importFile uri=" + uri);
        }

        mCountFiles++;
        runOnUiThread(this::allFilesCopied);

        final Context context = getApplicationContext();

        // If the file is provided by our own file provider, it is located in the tmp directory and we can access it directly.
        // We can avoid an expensive copy if the media is a video for example.
        if (uri.toString().startsWith(mApplicationFileUri)) {
            uri = Uri.fromFile(new File(new File(context.getFilesDir(), Twinlife.TMP_DIR), uri.toString().substring(mApplicationFileUri.length())));
        }
        final FileInfo fileInfo = new FileInfo(context, uri);

        final FileInfo copy;
        if (fileInfo.isFile()) {
            // File already copied, see ShareActivity.importFiles() or file was saved in TMP_DIR,
            // see ConversationActivity.takePhoto() and takeVideo(), for the video we avoid a big file copy!
            copy = fileInfo;
        } else if (fileInfo.isImage() || fileInfo.isVideo()) {
            copy = fileInfo.saveMedia(context, TwinmeApplication.QualityMedia.ORIGINAL.ordinal());
        } else  {
            copy = fileInfo.saveFile(context);
        }
        endImportFile(copy);
    }

    private void endImportFile(@Nullable FileInfo copy) {
        if (DEBUG) {
            Log.d(LOG_TAG, "endImportFile fileInfo=" + copy);
        }

        if (copy != null) {
            runOnUiThread(() -> {
                final int prevSize = mFiles.size();
                mFiles.add(copy);
                if (mPreviewFileAdapter !=  null) {
                    mPreviewFileAdapter.notifyItemRangeInserted(prevSize, 1);
                    mPreviewThumbnailAdapter.notifyItemRangeInserted(prevSize, 1);

                    int lastCurrentPosition = mCurrentPosition;
                    mCurrentPosition = mFiles.size() - 1;
                    mLinearLayoutManager.scrollToPosition(mCurrentPosition);
                    mPreviewThumbnailAdapter.notifyItemChanged(lastCurrentPosition);
                    mPreviewThumbnailAdapter.notifyItemChanged(mCurrentPosition);
                }
                updateViews();
                mCountFiles--;
                allFilesCopied();
            });
        } else {
            runOnUiThread(() -> {
                updateViews();
                showError();
                mCountFiles--;
                allFilesCopied();
            });
        }
    }

    private void showError() {
        if (DEBUG) {
            Log.d(LOG_TAG, "showError");
        }

        hideKeyboard();

        ViewGroup viewGroup = findViewById(R.id.preview_activity_layout);

        AlertMessageView alertMessageView = new AlertMessageView(this, null);
        alertMessageView.setForceDarkMode(true);
        alertMessageView.setTitle(getString(R.string.deleted_account_activity_warning));
        alertMessageView.setMessage(getString(R.string.application_error_file_not_found));

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
                setStatusBarColor(Color.BLACK);
            }
        };
        alertMessageView.setObserver(observer);
        viewGroup.addView(alertMessageView);
        alertMessageView.show();

        setStatusBarColor(Color.BLACK, Color.rgb(72,72,72));
    }

    private void onAddClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGalleryClick");
        }

        hapticFeedback();

        if (mPreviewStartWithMedia) {
            if (mMediaPicker != null) {
                launch(mMediaPicker, new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageAndVideo.INSTANCE)
                        .build());
            }
        } else {
            Intent chooseFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
            chooseFileIntent.setType("*/*");
            chooseFileIntent.addCategory(Intent.CATEGORY_OPENABLE);
            chooseFileIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

            Permission[] permissions = new Permission[]{
                    Permission.READ_EXTERNAL_STORAGE,
                    Permission.READ_MEDIA_AUDIO
            };

            if (checkPermissions(permissions)) {
                startActivityForResult(chooseFileIntent, REQUEST_GET_FILE);
            }
        }
    }

    @UnstableApi
    private void addPreviewMedia(List<Uri> uris) {
        if (DEBUG) {
            Log.d(LOG_TAG, "addPreviewMedia: " + uris);
        }

        for (Uri uri : uris) {
            getTwinmeContext().execute(() -> importFile(uri));
        }
    }

    @UnstableApi
    private void addFiles(Uri uri) {
        if (DEBUG) {
            Log.d(LOG_TAG, "addFiles: " + uri);
        }

        getTwinmeContext().execute(() -> importFile(uri));
    }

    private void onFileClick(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onMediaClick");
        }

        if (mPreviewFileAdapter == null) {
            return;
        }

        if (position == mCurrentPosition && mFiles.size() > 1) {
            mFiles.remove(position);
            mPreviewFileAdapter.notifyItemRemoved(position);
            mPreviewThumbnailAdapter.notifyItemRemoved(position);
            if (!mFiles.isEmpty()) {
                mCurrentPosition = Math.min(position, mFiles.size() - 1);
                mPreviewThumbnailAdapter.notifyItemChanged(mCurrentPosition);
                mPreviewFileAdapter.notifyItemChanged(mCurrentPosition);
                mLinearLayoutManager.scrollToPosition(position);
            }
        } else {
            mPreviewFileAdapter.notifyItemChanged(position);
            mLinearLayoutManager.scrollToPosition(position);
            mPreviewThumbnailAdapter.notifyItemChanged(position);
            mPreviewThumbnailAdapter.notifyItemChanged(mCurrentPosition);
            mCurrentPosition = position;
        }
    }

    private void allFilesCopied() {
        if (DEBUG) {
            Log.d(LOG_TAG, "allFilesCopied");
        }

        if (mCountFiles == 0) {
            mOverlayView.setVisibility(View.GONE);
            mProgressBarView.setVisibility(View.GONE);

            mPreviewStartWithMedia = true;

            for (FileInfo fileInfo : mFiles) {
                if (!fileInfo.isImage() && !fileInfo.isVideo()) {
                    mPreviewStartWithMedia = false;
                    break;
                }
            }

            if (mPreviewStartWithMedia) {
                mQualityView.setVisibility(View.VISIBLE);
            } else {
                mQualityView.setVisibility(View.GONE);
            }
        } else {
            mOverlayView.setVisibility(View.VISIBLE);
            mProgressBarView.setVisibility(View.VISIBLE);
        }
    }

    @UnstableApi
    private void prepareMediaBeforeSend() {
        if (DEBUG) {
            Log.d(LOG_TAG, "prepareMediaBeforeSend");
        }

        List<Uri> filesToReduce = new ArrayList<>();
        for (FileInfo fileInfo : mFiles) {
            filesToReduce.add(fileInfo.getUri());
        }

        for (Uri fileUri : filesToReduce) {
            // Increment count of files only from the main UI thread and before executing the reduce.
            mCountFiles++;
            getTwinmeContext().execute(() -> reduceFile(fileUri));
        }
    }

    @UnstableApi
    @WorkerThread
    private void reduceFile(@NonNull Uri uri) {
        if (DEBUG) {
            Log.d(LOG_TAG, "reduceFile uri=" + uri);
        }

        runOnUiThread(this::allFilesReduced);

        final Context context = getApplicationContext();
        final FileInfo fileInfo = new FileInfo(context, uri);

        if (fileInfo.isVideo()) {
            FileInfo.ReduceVideoObserver reduceVideoObserver = file -> {
                if (file != null) {
                    FileInfo videoFileInfo = new FileInfo(fileInfo, file);
                    endReduceFile(videoFileInfo);
                    // Remove original file because it was imported in the application and is no longer necessary.
                    fileInfo.removeFile();
                } else {
                    endReduceFile(fileInfo);
                }
            };
            fileInfo.reduceVideo(context, reduceVideoObserver);
        } else {
            FileInfo copy;
            if (fileInfo.isImage()) {
                copy = fileInfo.saveMedia(context, TwinmeApplication.QualityMedia.STANDARD.ordinal());
                if (copy != null) {
                    if (fileInfo.getPath() != null && !fileInfo.getPath().equals(copy.getPath())) {
                        fileInfo.removeFile();
                    }
                } else {
                    copy = fileInfo;
                }
            } else  {
                copy = fileInfo;
            }
            endReduceFile(copy);
        }
    }

    private void endReduceFile(@Nullable FileInfo copy) {
        if (DEBUG) {
            Log.d(LOG_TAG, "endReduceFile fileInfo=" + copy);
        }

        if (copy != null) {
            runOnUiThread(() -> {
                mReducedFiles.add(copy);
                mCountFiles--;
                allFilesReduced();
            });
        } else {
            runOnUiThread(() -> {
                mCountFiles--;
                allFilesReduced();
            });
        }
    }

    private void allFilesReduced() {
        if (DEBUG) {
            Log.d(LOG_TAG, "allFilesReduced");
        }

        if (mCountFiles == 0) {
            mOverlayView.setVisibility(View.GONE);
            mProgressBarView.setVisibility(View.GONE);
            sendIntent();
        } else {
            mOverlayView.setVisibility(View.VISIBLE);
            mProgressBarView.setVisibility(View.VISIBLE);
        }
    }

    private void sendIntent() {
        if (DEBUG) {
            Log.d(LOG_TAG, "sendIntent");
        }

        Intent data = new Intent();
        data.putExtra(Intents.INTENT_TEXT_MESSAGE, mEditText.getText().toString());
        data.putExtra(Intents.INTENT_ALLOW_COPY_FILE, mAllowCopy);
        data.putExtra(Intents.INTENT_ALLOW_COPY_TEXT, mAllowCopy);
        data.putExtra(Intents.INTENT_ALLOW_EPHEMERAL, mAllowEphemeralMessage);
        data.putExtra(Intents.INTENT_EXPIRE_TIMEOUT, mExpireTimeout);

        if (!mReducedFiles.isEmpty() && !mIsQualityMediaOriginal) {
            data.putParcelableArrayListExtra(Intents.INTENT_CAPTURED_FILE, new ArrayList<>(mReducedFiles));
        } else {
            data.putParcelableArrayListExtra(Intents.INTENT_CAPTURED_FILE, new ArrayList<>(mFiles));
        }

        // Clean the media info: we don't want to erase the file.
        mFiles.clear();
        mReducedFiles.clear();
        setResult(RESULT_OK, data);
        finish();
    }
}
