/*
 *  Copyright (c) 2018-2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SaveTwincodeAsyncTask {
    private static final String LOG_TAG = "SaveTwincodeAsyncTask";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private final WeakReference<AbstractTwinmeActivity> mActivityWeakReference;
    private final Bitmap mQRCodeBitmap;

    private final boolean mIsTransferCode;

    public SaveTwincodeAsyncTask(AbstractTwinmeActivity activity, Bitmap QRCodeBitmap) {

        mActivityWeakReference = new WeakReference<>(activity);
        mQRCodeBitmap = QRCodeBitmap;
        mIsTransferCode = false;
    }

    public SaveTwincodeAsyncTask(AbstractTwinmeActivity activity, Bitmap QRCodeBitmap, boolean isTransferCode) {

        mActivityWeakReference = new WeakReference<>(activity);
        mQRCodeBitmap = QRCodeBitmap;
        mIsTransferCode = isTransferCode;
    }

    public void execute() {
        executor.execute(this::saveQrCode);
    }

    private void saveQrCode() {
        AbstractTwinmeActivity activity = mActivityWeakReference.get();
        if (activity != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyyHHmmss", Locale.US);
            String fileName = "twincode_" + dateFormat.format(new Date());

            ContentValues values = new ContentValues();
            Uri base = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            values.put(MediaStore.Images.Media.TITLE, fileName);
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
            values.put(MediaStore.Images.Media.DATE_MODIFIED, System.currentTimeMillis() / 1000);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.put(MediaStore.Images.Media.IS_PENDING, 1);
                base = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
            }

            ContentResolver resolver = activity.getContentResolver();
            Uri item = resolver.insert(base, values);
            if (item != null) {
                try (ParcelFileDescriptor parcelFileDescriptor = resolver.openFileDescriptor(item, "w")) {
                    if (parcelFileDescriptor != null) {
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        mQRCodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                        try (InputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
                             OutputStream outputStream = new FileOutputStream(parcelFileDescriptor.getFileDescriptor())) {
                            byte[] buffer = new byte[1024];
                            int length;
                            while ((length = inputStream.read(buffer)) > 0) {
                                outputStream.write(buffer, 0, length);
                            }
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            values.put(MediaStore.Images.Media.IS_PENDING, 0);
                        }
                        resolver.update(item, values, null, null);

                        values.clear();
                    }
                } catch (Exception exception) {
                    Log.e(LOG_TAG, "Error occurred while saving QRcode", exception);
                }
            }
        }

        uiHandler.post(this::notifySaved);
    }

    private void notifySaved() {

        AbstractTwinmeActivity activity = mActivityWeakReference.get();
        if (activity != null) {
            if (mIsTransferCode) {
                Toast.makeText(activity, R.string.transfert_call_activity_saved_message, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity, R.string.capture_activity_qrcode_saved, Toast.LENGTH_SHORT).show();
            }

        }
    }
}
