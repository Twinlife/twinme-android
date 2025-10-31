/*
 *  Copyright (c) 2014-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Thibaud David (contact@thibauddavid.com)
 *   Yannis Le Gal (Yannis.LeGal@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.twinlife.device.android.twinme.BuildConfig;
import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.Twinlife;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.TwinmeActivity.Permission;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class EditableView {
    private static final String LOG_TAG = "EditableView";
    private static final boolean DEBUG = false;

    private static final int MIN_REQUEST_CODE = 1024;
    private static final int SELECT_IMAGE = MIN_REQUEST_CODE;

    private static final String DEFAULT_PREFIX_ID = "org.twinlife.device.android.twinme.util.EditableView.";
    private static final String CAPTURE_URI_ID = "CaptureUriId";
    private static final String CROPPED_URI_ID = "CroppedUriId";
    private static final String FROM_MEDIA_PICKER = "FromMediaPicker";

    private final AbstractTwinmeActivity mActivity;
    private final String mPrefixId;
    private final int mRequestId;

    private Uri mCaptureUri;
    private Uri mCroppedUri;

    private boolean mPreserveFiles;

    private ActivityResultLauncher<PickVisualMediaRequest> mMediaPicker;
    private boolean mEditableAvatar;
    private boolean mMediaPickFromGallery;

    public interface ImageResult {
        void onGetImage(@NonNull String path, @NonNull Bitmap image, @Nullable Bitmap largeImage);
    }

    public EditableView(@NonNull AbstractTwinmeActivity activity) {
        if (DEBUG) {
            Log.d(LOG_TAG, "EditableView: activity=" + activity);
        }

        mActivity = activity;
        mPrefixId = DEFAULT_PREFIX_ID;
        mPreserveFiles = false;
        mMediaPickFromGallery = false;

        mMediaPicker = activity.registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                onPickMediaInGallery(uri);
            }
        });
        mRequestId = SELECT_IMAGE;
        mEditableAvatar = true;
    }

    public EditableView(@NonNull AbstractTwinmeActivity activity, @NonNull String prefix, int requestId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "EditableView: activity=" + activity + " prefix=" + prefix + " requestId=" + requestId);
        }

        mActivity = activity;
        mPrefixId = prefix;
        mPreserveFiles = false;
        mEditableAvatar = true;
        mRequestId = SELECT_IMAGE + requestId;
    }

    public void setEditableAvatar(boolean editableAvatar) {

        mEditableAvatar = editableAvatar;
    }

    /**
     * Click action to update the image either with the camera or with an external file.
     *
     * We handle the permissions here to simplify and provide some homogeneous behavior.
     */
    public void onClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "EditableView.onClick");
        }

        final Permission[] permissions = new Permission[] {
                Permission.CAMERA,
                Permission.READ_EXTERNAL_STORAGE
        };

        if (mActivity.checkPermissions(permissions)) {
            onClick(true, true);
        }
    }

    /**
     * Click action to update the image either with the camera
     *
     * We handle the permissions here to simplify and provide some homogeneous behavior.
     */
    public void onCameraClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "EditableView.onCameraClick");
        }

        final Permission[] permissions = new Permission[] {
                Permission.CAMERA,
        };

        if (mActivity.checkPermissions(permissions)) {
            onCameraClick(true);
        }
    }

    /**
     * Click action to update the image either with an external file.
     * We handle the permissions here to simplify and provide some homogeneous behavior.
     */
    public void onGalleryClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "EditableView.onGalleryClick");
        }

        // This is tricky: we will get the media picker response from the onPickMediaInGallery() but
        // we will also get called from the onActivityResult().  On some devices, onPickMediaInGallery()
        // is called first, but on some others this is the opposite.  We have to trigger the cropImage()
        // when we get the result and we do this from onPickMediaInGallery().  We must mark mMediaPickFromGallery
        // so that the second call on onActivityResult() knows that the cropImage() was processed or will
        // be executed AND we must not dispose() the result (if we dispose(), we loose the selected media after cropping).
        if (mMediaPicker != null && mActivity.launch(mMediaPicker, new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build())) {
            mMediaPickFromGallery = true;
        }
    }

    private void onPickMediaInGallery(Uri uri) {
        if (DEBUG) {
            Log.d(LOG_TAG, "EditableView.onPickMediaInGallery: uri = " + uri);
        }

        mCaptureUri = uri;
        mActivity.runOnTwinlifeThread(this::cropImage);
    }

    /**
     * Handle the permission request result when `checkPermissions` returned false.
     *
     * @param grantedPermissions the list of granted permissions.
     * @return true if the operation is handled.
     */
    public boolean onRequestPermissions(@NonNull Permission[] grantedPermissions) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRequestPermissions grantedPermissions=" + Arrays.toString(grantedPermissions));
        }

        boolean cameraGranted = false;
        for (Permission grantedPermission : grantedPermissions) {
            switch (grantedPermission) {
                case CAMERA:
                    cameraGranted = true;
                    break;
            }
        }
        if (cameraGranted) {
            onCameraClick(true);
            return true;
        } else {
            return false;
        }
    }

    private boolean onCameraClick(boolean cameraGranted) {
        if (DEBUG) {
            Log.d(LOG_TAG, "EditableView.onCameraClick: cameraGranted=" + cameraGranted);
        }

        Intent cameraIntent = null;
        if (cameraGranted) {
            File directory = new File(mActivity.getFilesDir(), Twinlife.TMP_DIR);
            if (!directory.isDirectory()) {
                if (!directory.mkdirs() || !directory.isDirectory()) {

                    return false;
                }
            }

            File captureFile = new File(directory, "twinlife_" + System.currentTimeMillis() + ".jpg");
            try {
                //noinspection ResultOfMethodCallIgnored
                captureFile.createNewFile();
            } catch (IOException exception) {
                Log.d(LOG_TAG, "EditableView.onClick: cameraGranted=" + cameraGranted + " exception" + exception);

                return false;
            }

            mCaptureUri = FileProvider.getUriForFile(mActivity, BuildConfig.APPLICATION_ID + ".fileprovider", captureFile);
            cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            List<ResolveInfo> resolvedIntentActivities = mActivity.getPackageManager().queryIntentActivities(cameraIntent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolvedIntentInfo : resolvedIntentActivities) {
                String packageName = resolvedIntentInfo.activityInfo.packageName;
                mActivity.grantUriPermission(packageName, mCaptureUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }

            // There is no standard way to select the front camera through the intent.
            // The following works for Google Pixel, Wiko but does not work on Huawei tablet, Samsung S2.
            cameraIntent.putExtra("android.intent.extras.CAMERA_FACING", android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT);
            cameraIntent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1);
            cameraIntent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true);
            cameraIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
            cameraIntent.putExtra("return-data", false);
            cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mCaptureUri);
            cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }

        if (cameraIntent != null) {
            try {
                mActivity.startActivityForResult(cameraIntent, mRequestId);
                return true;

            } catch (ActivityNotFoundException exception) {
                Log.d(LOG_TAG, "Exception: " + exception);
            }
        }

        return false;
    }

    private boolean onClick(boolean cameraGranted, boolean storageAccessGranted) {
        if (DEBUG) {
            Log.d(LOG_TAG, "EditableView.onClick: cameraGranted=" + cameraGranted + " storageAccessGranted=" + storageAccessGranted);
        }

        Intent cameraIntent = null;
        if (cameraGranted) {
            File directory = new File(mActivity.getFilesDir(), Twinlife.TMP_DIR);
            if (!directory.isDirectory()) {
                if (!directory.mkdirs() || !directory.isDirectory()) {

                    return false;
                }
            }

            File captureFile = new File(directory, "twinlife_" + System.currentTimeMillis() + ".jpg");
            try {
                //noinspection ResultOfMethodCallIgnored
                captureFile.createNewFile();
            } catch (IOException exception) {
                //noinspection ConstantConditions
                Log.d(LOG_TAG, "EditableView.onClick: cameraGranted=" + cameraGranted + " storageAccessGranted=" + storageAccessGranted + " exception" + exception);

                return false;
            }

            mCaptureUri = FileProvider.getUriForFile(mActivity, BuildConfig.APPLICATION_ID + ".fileprovider", captureFile);
            cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            List<ResolveInfo> resolvedIntentActivities = mActivity.getPackageManager().queryIntentActivities(cameraIntent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolvedIntentInfo : resolvedIntentActivities) {
                String packageName = resolvedIntentInfo.activityInfo.packageName;
                mActivity.grantUriPermission(packageName, mCaptureUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }

            // There is no standard way to select the front camera through the intent.
            // The following works for Google Pixel, Wiko but does not work on Huawei tablet, Samsung S2.
            cameraIntent.putExtra("android.intent.extras.CAMERA_FACING", android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT);
            cameraIntent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1);
            cameraIntent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true);
            cameraIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
            cameraIntent.putExtra("return-data", false);
            cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mCaptureUri);
            cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }

        Intent getContentIntent = null;
        if (storageAccessGranted) {
            getContentIntent = new Intent(Intent.ACTION_GET_CONTENT);
            getContentIntent.addCategory(Intent.CATEGORY_OPENABLE);
            getContentIntent.setType("image/*");
        }

        Intent intent = null;
        if (cameraIntent != null && getContentIntent != null) {
            intent = new Intent(Intent.ACTION_CHOOSER);
            Parcelable[] intents = {cameraIntent};
            intent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents);
            intent.putExtra(Intent.EXTRA_TITLE, mActivity.getString(R.string.application_select_application));
            intent.putExtra(Intent.EXTRA_INTENT, getContentIntent);
        } else if (cameraIntent != null) {
            intent = cameraIntent;
        } else if (getContentIntent != null) {
            intent = getContentIntent;
        }

        if (intent != null) {
            try {
                mActivity.startActivityForResult(intent, mRequestId);
                return true;

            } catch (ActivityNotFoundException exception) {
                Log.d(LOG_TAG, "Exception: " + exception);
            }

            // Chooser activity not found, fallback to the camera.
            if (cameraIntent != null && getContentIntent != null) {
                try {
                    mActivity.startActivityForResult(cameraIntent, mRequestId);
                    return true;

                } catch (ActivityNotFoundException exception) {
                    Log.d(LOG_TAG, "Exception: " + exception);
                }
            }
        }

        return false;
    }

    public void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        String value = savedInstanceState.getString(mPrefixId + CAPTURE_URI_ID);
        if (value != null) {
            mCaptureUri = Uri.parse(value);
        }

        value = savedInstanceState.getString(mPrefixId + CROPPED_URI_ID);
        if (value != null) {
            mCroppedUri = Uri.parse(value);
        }
        mMediaPickFromGallery = savedInstanceState.getBoolean(mPrefixId + FROM_MEDIA_PICKER, false);
    }

    public void onSaveInstanceState(Bundle outState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSaveInstanceState: outState=" + outState);
        }

        // If we save the capture or cropped Uri, we must invalidate them because
        // onDestroy() will probably be called and it will delete the file.
        // When doing so, the CropImage will not be able to access the file!
        if (mCaptureUri != null) {
            outState.putString(mPrefixId + CAPTURE_URI_ID, mCaptureUri.toString());
            mPreserveFiles = true;
        }

        if (mCroppedUri != null) {
            outState.putString(mPrefixId + CROPPED_URI_ID, mCroppedUri.toString());
            mPreserveFiles = true;
        }
        if (mMediaPickFromGallery) {
            outState.putBoolean(mPrefixId + FROM_MEDIA_PICKER, true);
        }
    }

    public Uri onActivityResult(int requestCode, int resultCode, Intent data) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onActivityResult: requestCode=" + requestCode + " resultCode=" + resultCode + " data=" + data);
        }

        if (resultCode != Activity.RESULT_OK) {
            dispose();

            return null;
        }

        if (requestCode == mRequestId) {
            if (data != null && data.getData() != null) {
                mCaptureUri = data.getData();
            }
            mActivity.runOnTwinlifeThread(this::cropImage);
            return null;
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && mCaptureUri != null) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            // Remove the previous cropped image if there was one.
            if (mCroppedUri != null && mCroppedUri.getPath() != null) {
                File file = new File(mCroppedUri.getPath());
                if (file.exists() && !file.delete()) {
                    Log.w(LOG_TAG, "Cannot remove previous cropped image");
                }
            }
            mCroppedUri = result.getUri();
            mPreserveFiles = false;
            mMediaPickFromGallery = false;
        }
        // cropImage() was called OR will be called from onPickMediaGallery().
        if (mMediaPickFromGallery) {
            return null;
        }

        // Now we must release the capture file: we have the final cropped image or the operation was canceled.
        dispose();
        if (mCroppedUri == null) {

            return null;
        }

        return mCroppedUri;
    }

    public void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        // The activity can be destroyed but we may have to keep the file because it will resurrect through another activity.
        // When this happens, the URI was saved by onSaveInstanceState().
        if (mCroppedUri != null && mCroppedUri.getPath() != null && !mPreserveFiles) {
            // Cleanup from the twinlife thread to avoid blocking the UI.
            mActivity.runOnTwinlifeThread(() -> {
                File file = new File(mCroppedUri.getPath());
                if (file.exists() && !file.delete()) {
                    Log.w(LOG_TAG, "Cannot remove cropped image");
                }
            });
        }

        if (mCaptureUri != null && mCaptureUri.getPath() != null && !mPreserveFiles) {
            mActivity.runOnTwinlifeThread(() -> {
                File file = new File(mActivity.getFilesDir(), mCaptureUri.getPath());
                if (file.exists() && !file.delete()) {
                    Log.w(LOG_TAG, "Cannot remove capture image");
                }
            });
        }
    }

    /**
     * If there is a valid selected & cropped image, get the path, thunbmail image and large image.
     * Image reading and decoding is made from the twinlife executor's thread to avoid blocking the UI.
     * The image result will be called only if we have some valid path and thumbnail and from the main UI thread.
     * @param result the result method to invoke.
     */
    public void getSelectedImage(@NonNull ImageResult result) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getSelectedImage");
        }

        final Uri uri = mCroppedUri;
        if (uri != null && uri.getPath() != null) {
            mActivity.runOnTwinlifeThread(() -> {
                final String path = uri.getPath();
                final Bitmap bitmap = Utils.getScaledAvatar(uri);
                if (bitmap != null && path != null) {
                    BitmapDrawable drawable = Utils.getBitmapDrawable(mActivity, path, Design.AVATAR_MAX_WIDTH, Design.AVATAR_MAX_HEIGHT);

                    mActivity.runOnUiThread(() -> result.onGetImage(path, bitmap, drawable == null ? null : drawable.getBitmap()));
                }
            });
        }
    }

    public Uri getUri() {

        return mCroppedUri;
    }

    //
    // Private Methods
    //

    private void cropImage() {
        if (DEBUG) {
            Log.d(LOG_TAG, "cropImpage");
        }

        // Get the mime type of the image so that we can choose between PNG or JPEG.
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        try (InputStream is = mActivity.getContentResolver().openInputStream(mCaptureUri)) {

            BitmapFactory.decodeStream(is, null, bmOptions);

        } catch (Exception ex) {
            Log.e(LOG_TAG, "Error " + ex);
        }

        CropImage.ActivityBuilder activityBuilder = CropImage.activity(mCaptureUri);

        if (mEditableAvatar) {
            activityBuilder.setCropShape(CropImageView.CropShape.RECTANGLE);
            activityBuilder.setAspectRatio(1, 1);
            activityBuilder.setFixAspectRatio(true);
        } else {
            activityBuilder.setCropShape(CropImageView.CropShape.RECTANGLE);
            activityBuilder.setScaleType(CropImageView.ScaleType.FIT_CENTER);
            activityBuilder.setInitialCropWindowPaddingRatio(0);
        }

        activityBuilder.setGuidelines(CropImageView.Guidelines.ON);
        if ("image/png".equals(bmOptions.outMimeType)) {
            activityBuilder.setOutputCompressFormat(Bitmap.CompressFormat.PNG);
        } else {
            activityBuilder.setOutputCompressFormat(Bitmap.CompressFormat.JPEG);
        }
        activityBuilder.start(mActivity);
    }

    private void dispose() {
        if (DEBUG) {
            Log.d(LOG_TAG, "dispose");
        }

        final Uri captureUri = mCaptureUri;
        if (captureUri != null) {
            mCaptureUri = null;
            mPreserveFiles = false;
            try {
                mActivity.revokeUriPermission(captureUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (Exception ignore) {
            }

            // Delete the file from the executor's thread to avoid blocking the UI.
            if (captureUri.getPath() != null) {
                mActivity.runOnTwinlifeThread(() -> {
                    File captureFile = new File(mActivity.getFilesDir(), captureUri.getPath());
                    if (captureFile.exists() && !captureFile.delete()) {
                        Log.w(LOG_TAG, "Cannot remove capture file");
                    }
                });
            }
        }
    }
}
