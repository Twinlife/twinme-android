/*
 *  Copyright (c) 2020-2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.BaseService.ErrorCode;
import org.twinlife.twinlife.util.Utils;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;

/**
 * Save a file asynchronously to avoid blocking the UI thread.
 * <p>
 * The SaveAsyncTask can save only one file at a time.
 * - if the operation succeeds, report the toast("Saved!")
 * - if the operation failed, report the onError() with storage error.
 */
public class SaveAsyncTask extends AsyncTask<Void, Void, Void> {

    private final WeakReference<AbstractTwinmeActivity> mActivityWeakReference;
    private final ContentResolver mContentResolver;
    private final FileInfo mMedia;
    private final File mFile;
    private final Uri mUri;
    private ErrorCode mResult;

    public SaveAsyncTask(@NonNull AbstractTwinmeActivity activity, @NonNull File file, @NonNull Uri uri) {

        mActivityWeakReference = new WeakReference<>(activity);
        mContentResolver = activity.getContentResolver();
        mFile = file;
        mUri = uri;
        mMedia = new FileInfo(activity.getApplicationContext(), Uri.fromFile(mFile));
        mResult = ErrorCode.LIBRARY_ERROR;
    }

    public SaveAsyncTask(@NonNull AbstractTwinmeActivity activity, @NonNull File file, @NonNull File target) {

        mActivityWeakReference = new WeakReference<>(activity);
        mContentResolver = activity.getContentResolver();
        mFile = file;
        mUri = Uri.fromFile(target);
        mMedia = new FileInfo(activity.getApplicationContext(), Uri.fromFile(mFile));
        mResult = ErrorCode.LIBRARY_ERROR;
    }

    @Override
    protected Void doInBackground(Void... params) {

        try {
            Uri target;
            if (mMedia.isImage() || mMedia.isVideo()) {
                ContentValues values = new ContentValues();
                Uri base;
                if (mMedia.isVideo()) {
                    base = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    values.put(MediaStore.Video.Media.TITLE, mMedia.getFilename());
                    values.put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
                    values.put(MediaStore.Video.Media.DATE_MODIFIED, System.currentTimeMillis() / 1000);
                    values.put(MediaStore.Video.Media.MIME_TYPE, mMedia.getMimeType());
                } else {
                    base = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    values.put(MediaStore.Images.Media.TITLE, mMedia.getFilename());
                    values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
                    values.put(MediaStore.Images.Media.DATE_MODIFIED, System.currentTimeMillis() / 1000);
                    values.put(MediaStore.Images.Media.MIME_TYPE, mMedia.getMimeType());
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (mMedia.isVideo()) {
                        values.put(MediaStore.Video.Media.IS_PENDING, 1);
                        base = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
                    } else {
                        values.put(MediaStore.Images.Media.IS_PENDING, 1);
                        base = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
                    }
                }

                target = mContentResolver.insert(base, values);
            } else {
                target = mUri;
            }

            if (target != null) {
                try (ParcelFileDescriptor parcelFileDescriptor = mContentResolver.openFileDescriptor(target, "w")) {
                    if (parcelFileDescriptor != null) {

                        try (InputStream inputStream = new FileInputStream(mFile);
                             FileOutputStream outputStream = new FileOutputStream(parcelFileDescriptor.getFileDescriptor())) {
                            if (Utils.copyStream(inputStream, outputStream)) {
                                mResult = ErrorCode.SUCCESS;

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    ContentValues values = new ContentValues();
                                    if (mMedia.isVideo()) {
                                        values.put(MediaStore.Video.Media.IS_PENDING, 0);
                                    } else {
                                        values.put(MediaStore.Images.Media.IS_PENDING, 0);
                                    }
                                    mContentResolver.update(target, values, null, null);
                                }
                            } else {
                                mResult = ErrorCode.NO_STORAGE_SPACE;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {

        AbstractTwinmeActivity activity = mActivityWeakReference.get();
        if (activity != null) {
            if (mResult == ErrorCode.SUCCESS) {
                Toast.makeText(activity, R.string.conversation_activity_menu_item_view_save_message, Toast.LENGTH_SHORT).show();
            } else {
                activity.onExecutionError(mResult);
            }
        }
    }
}
