/*
 *  Copyright (c) 2015-2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.ShapeDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.ResultPointCallback;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.QRCodeWriter;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.camera.CameraManager;
import org.webrtc.Size;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract scanner activity.
 * <p>
 * This is the base class of scanner activities to take into account various common QR-code scanning operations.
 * It provides:
 * <p>
 * - the initialisation and setting of the CameraManager to access the camera,
 * - the ViewFinderView class to draw the camera image,
 * - the AmbientLightManager class to control the ambient light,
 * - the BeepManager class to emit the beep once the QR-code is scanned,
 * <p>
 * To use this activity:
 * <p>
 * - inherit from the AbstractScannerActivity class,
 * - setup mTextureView, mMessageView, mQRCodeView, mProgressBarView
 * - override void handleDecode(@NonNull Uri uri) to be called when the QR-code is scanned,
 * - override void incorrectQRCode() to be notified when the QR-code is invalid,
 * <p>
 * When the activity goes in background the camera is released by onSuspend() and any processing is suspended.
 * The camera manager is re-opened by onResume() when we are restored.
 * <p>
 * The ViewFinderView class has its onDraw() method that is called periodically to refresh the view
 * and display the QR-code result points (3 points in most cases).  This processing must also be
 * stopped while we are paused and then it must be restarted (see onDraw() and setupCamera()).
 */
public abstract class AbstractScannerActivity extends AbstractTwinmeActivity implements TextureView.SurfaceTextureListener, CameraManager.CameraCallback {
    private static final String LOG_TAG = "AbstractScannerActivity";
    private static final boolean DEBUG = false;

    private static final int MAX_RESULT_POINTS = 20;
    private static final long ANIMATION_DELAY = 80L;

    private static final int QRCODE_PIXEL_WIDTH = 295;
    private static final int QRCODE_PIXEL_HEIGHT = 295;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;

    private static final float DESIGN_FRAMING_HEIGHT_RATIO = 574f / 974f;
    private static float DESIGN_FRAMING_WIDTH_RATIO = (Design.DISPLAY_WIDTH - 64f) / Design.DISPLAY_WIDTH;
    private static float DESIGN_FRAMING_LEFT_RATIO = (1f - DESIGN_FRAMING_WIDTH_RATIO) / 2f;
    private static float DESIGN_FRAMING_TOP_RATIO = (1f - DESIGN_FRAMING_HEIGHT_RATIO) / 2f;
    private static float DESIGN_FRAMING_RIGHT_RATIO = (1f + DESIGN_FRAMING_WIDTH_RATIO) / 2f;
    private static float DESIGN_FRAMING_BOTTOM_RATIO = (1f + DESIGN_FRAMING_HEIGHT_RATIO) / 2f;
    private static float DESIGN_FRAMING_LINE_THICKNESS_RATIO = 4f / Design.DISPLAY_WIDTH;
    private static float DESIGN_FRAMING_LINE_LENGTH_X_RATIO = 28f / Design.DISPLAY_WIDTH;
    private static float DESIGN_FRAMING_LINE_LENGTH_Y_RATIO = 28f / 974f;
    private static int POINT_SIZE = 6;

    public static class ViewFinderView extends View {
        private static final int[] SCANNER_ALPHA = {0, 64, 128, 192, 255, 192, 128, 64};
        private static final int CURRENT_POINT_OPACITY = 0xA0;

        private AbstractScannerActivity mCaptureActivity;
        private final Paint mPaint;
        private final float[] mPoints = new float[2];
        private final int mLaserColor;
        private final int mResultPointColor;
        private final Rect mRectArea = new Rect();
        private final Matrix mFromPreview = new Matrix();
        private int mWidth;
        private int mHeight;
        private int scannerAlpha;
        private List<ResultPoint> mCurrentResultPoints = new ArrayList<>();
        private List<ResultPoint> mLastResultPoints = new ArrayList<>();

        private boolean mDrawCorner = true;

        public ViewFinderView(Context context, AttributeSet attrs) {
            super(context, attrs);

            if (DEBUG) {
                Log.d(LOG_TAG, "ViewFinderView.ViewFinderView: context=" + context + "attrs=" + attrs);
            }

            scannerAlpha = 0;
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

            Resources resources = getResources();
            mLaserColor = resources.getColor(R.color.qrcode_laser);
            mResultPointColor = resources.getColor(R.color.qrcode_result_points);
        }

        public void setDrawCorner(boolean drawCorner) {

            mDrawCorner = drawCorner;
        }

        @NonNull
        public Rect setCaptureActivity(@NonNull AbstractScannerActivity captureActivity, @NonNull CameraManager cameraManager) {

            // This must be executed from the CameraThread!
            mCaptureActivity = captureActivity;
            mWidth = mCaptureActivity.mTextureView.getWidth();
            mHeight = mCaptureActivity.mTextureView.getHeight();

            final Size cameraResolution = cameraManager.getCameraResolution();
            final boolean facingFront = cameraManager.isCameraFacingFront();

            int videoWidth;
            int videoHeight;
            int cameraOrientation = cameraManager.getDisplayOrientation();
            if (cameraOrientation != 90 && cameraOrientation != 270) {
                videoWidth = cameraResolution.width;
                videoHeight = cameraResolution.height;
            } else {
                //noinspection SuspiciousNameCombination
                videoWidth = cameraResolution.height;
                //noinspection SuspiciousNameCombination
                videoHeight = cameraResolution.width;
            }
            double aspectRatio = (double) videoHeight / videoWidth;
            int newWidth;
            int newHeight;
            if (mHeight > (int) (mWidth * aspectRatio)) {
                newWidth = (int) (mHeight / aspectRatio);
                newHeight = mHeight;
            } else {
                newWidth = mWidth;
                newHeight = (int) (mWidth * aspectRatio);
            }

            float width = mWidth;
            float height = mHeight;

            float left = width * DESIGN_FRAMING_LEFT_RATIO;
            float top = height * DESIGN_FRAMING_TOP_RATIO;
            float right = width * DESIGN_FRAMING_RIGHT_RATIO;
            float bottom = height * DESIGN_FRAMING_BOTTOM_RATIO;

            mRectArea.set((int) left, (int) top, (int) right, (int) bottom);
            scheduleRefresh();

            float scaleX = (float) newWidth / videoWidth;
            float scaleY = (float) newHeight / videoHeight;

            mFromPreview.reset();

            // Crop the region to analyse within the camera image: keep a square representing the height or width
            // of the camera (which is almost what the user sees in the preview).  This region is a little bit bigger
            // that the preview and it allows to detect QR-code in some corner cases.
            float dw;
            if (cameraResolution.width > cameraResolution.height) {
                dw = cameraResolution.width - cameraResolution.height;
                top = 0;
                left = dw / 2;
                bottom = cameraResolution.height;
                right = left + cameraResolution.height;
            } else {
                dw = videoHeight - cameraResolution.width;
                left = 0;
                top = dw / 2;
                bottom = top + cameraResolution.width;
                right = cameraResolution.width;
            }

            // Translate camera coordinates:
            // - [left, top] correction is necessary because the QR-code scanner gives relative position within the cropped area
            // - [-vH/2, -vW/2] correction moves the rotation position within the center of the camera
            mFromPreview.setTranslate(left - (videoHeight / 2f), top -(videoWidth / 2f));

            // Rotate and scale to map the camera coordinate to the view.
            mFromPreview.postScale(facingFront ? -1 : 1, 1);
            mFromPreview.postRotate(cameraOrientation);
            mFromPreview.postScale(scaleX, scaleY);

            // Due to rotation and [-vH/2, -vW/2] translation, the coordinates are centered in the middle of the view area,
            // translate back to use the top left corner.
            mFromPreview.postTranslate(width / 2f, height / 2f);

            if (DEBUG) {
                mPoints[0] = left;
                mPoints[1] = top;
                mFromPreview.mapPoints(mPoints);
                Log.e(LOG_TAG, "Draw [" + left + ", " + top + "] -> [" + mPoints[0] + " " + mPoints[1] + "]");

                mPoints[0] = right;
                mPoints[1] = top;
                mFromPreview.mapPoints(mPoints);
                Log.e(LOG_TAG, "Draw [" + right + ", " + top + "] -> [" + mPoints[0] + " " + mPoints[1] + "]");

                mPoints[0] = left;
                mPoints[1] = bottom;
                mFromPreview.mapPoints(mPoints);
                Log.e(LOG_TAG, "Draw [" + left + ", " + bottom + "] -> [" + mPoints[0] + " " + mPoints[1] + "]");
                Log.e(LOG_TAG, "Camera region analysed [" + left + ", " + top + ", " + right + ", " + bottom + "]");
            }
            return new Rect((int) left, (int) top, (int) right, (int) bottom);
        }

        @Override
        public void onDraw(@NonNull Canvas canvas) {
            if (DEBUG) {
                Log.d(LOG_TAG, "ViewFinderView.onDraw: canvas=" + canvas);
            }

            if (mCaptureActivity == null || !mCaptureActivity.isResuming()) {

                return;
            }

            float lengthX = mWidth * DESIGN_FRAMING_LINE_LENGTH_X_RATIO;
            float lengthY = mHeight * DESIGN_FRAMING_LINE_LENGTH_Y_RATIO;
            float thickness = mWidth * DESIGN_FRAMING_LINE_THICKNESS_RATIO;

            mPaint.setColor(Color.WHITE);

            if (mDrawCorner) {
                canvas.drawRect(mRectArea.left, mRectArea.top, mRectArea.left + thickness, mRectArea.top + lengthY, mPaint);
                canvas.drawRect(mRectArea.left, mRectArea.top, mRectArea.left + lengthX, mRectArea.top + thickness, mPaint);
                canvas.drawRect(mRectArea.right - thickness, mRectArea.bottom - lengthY, mRectArea.right, mRectArea.bottom, mPaint);
                canvas.drawRect(mRectArea.right - lengthX, mRectArea.bottom - thickness, mRectArea.right, mRectArea.bottom, mPaint);
            }

            // Draw a red "laser scanner" line through the middle to show decoding is active
            mPaint.setColor(mLaserColor);
            mPaint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
            scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;
            int middle = (mRectArea.top + mRectArea.bottom) / 2;
            canvas.drawRect(mRectArea.left, middle - 1, mRectArea.right, middle + 2, mPaint);

            List<ResultPoint> resultPoints = mLastResultPoints;
            if (!resultPoints.isEmpty()) {
                mPaint.setAlpha(CURRENT_POINT_OPACITY);
                mPaint.setColor(mResultPointColor);

                for (ResultPoint resultPoint : resultPoints) {
                    mPoints[0] = resultPoint.getX();
                    mPoints[1] = resultPoint.getY();
                    mFromPreview.mapPoints(mPoints);
                    canvas.drawCircle(mPoints[0], mPoints[1], POINT_SIZE, mPaint);
                    if (DEBUG) {
                        Log.e(LOG_TAG, "Draw [" + resultPoint.getX() + " " + resultPoint.getY() + "] -> [" + mPoints[0] + " " + mPoints[1] + "]"
                                + " w=" + mWidth + " h=" + mHeight + " PT=" + POINT_SIZE);
                    }
                }
                resultPoints.clear();
            }
            synchronized (this) {
                mLastResultPoints = mCurrentResultPoints;
                mCurrentResultPoints = resultPoints;
            }

            scheduleRefresh();
        }

        void addPossibleResultPoint(ResultPoint resultPoint) {
            if (DEBUG) {
                Log.d(LOG_TAG, "ViewFinderView.addPossibleResultPoint: resultPoint=" + resultPoint);
            }

            synchronized (this) {
                mCurrentResultPoints.add(resultPoint);
                int size = mCurrentResultPoints.size();
                if (size > MAX_RESULT_POINTS) {
                    // trim it
                    mCurrentResultPoints.subList(0, size - MAX_RESULT_POINTS / 2).clear();
                }
            }
        }

        private void scheduleRefresh() {
            if (DEBUG) {
                Log.d(LOG_TAG, "ViewFinderView.scheduleRefresh");
            }

            postInvalidateDelayed(ANIMATION_DELAY, 0, 0, mWidth, mHeight);
        }
    }

    protected static class ViewFinderResultPointCallback implements ResultPointCallback {

        private final ViewFinderView mViewFinderView;

        public ViewFinderResultPointCallback(ViewFinderView viewFinderView) {

            mViewFinderView = viewFinderView;
        }

        @Override
        public void foundPossibleResultPoint(ResultPoint resultPoint) {
            if (DEBUG) {
                Log.d(LOG_TAG, "ViewFinderResultPointCallback.foundPossibleResultPoint: resultPoint=" + resultPoint);
            }

            mViewFinderView.addPossibleResultPoint(resultPoint);
        }
    }

    //
    // Based on:
    // android/src/com/google/zxing/client/android/AmbientLightManager.java
    //

    public class AmbientLightManager implements SensorEventListener {

        private static final float TOO_DARK_LUX = 45.0f;
        private static final float BRIGHT_ENOUGH_LUX = 450.0f;

        private Sensor mLightSensor;

        void start() {
            if (DEBUG) {
                Log.d(LOG_TAG, "AmbientLightManager.start");
            }

            if (mLightSensor != null) {

                return;
            }

            SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            if (sensorManager != null) {
                mLightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
                if (mLightSensor != null) {
                    sensorManager.registerListener(this, mLightSensor, SensorManager.SENSOR_DELAY_NORMAL);
                }
            }
        }

        public void stop() {
            if (DEBUG) {
                Log.d(LOG_TAG, "AmbientLightManager.stop");
            }

            if (mLightSensor != null) {
                SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
                if (sensorManager != null) {
                    sensorManager.unregisterListener(this);
                }
                mLightSensor = null;
            }
        }

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            if (DEBUG) {
                Log.d(LOG_TAG, "AmbientLightManager.onSensorChanged: sensorEvent.light=" + sensorEvent.values[0]);
            }

            if (mCameraManager != null) {
                float ambientLightLux = sensorEvent.values[0];

                if (ambientLightLux <= TOO_DARK_LUX) {
                    mCameraManager.setTorch(CameraManager.FlashMode.SINGLE);
                } else if (ambientLightLux >= BRIGHT_ENOUGH_LUX) {
                    mCameraManager.setTorch(CameraManager.FlashMode.OFF);
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            if (DEBUG) {
                Log.d(LOG_TAG, "AmbientLightManager.onAccuracyChanged: sensor=" + sensor + " accuracy=" + accuracy);
            }
        }
    }

    //
    // Based on: android/src/com/google/zxing/client/android/BeepManager.java
    //

    private class BeepManager implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

        private static final float BEEP_VOLUME = 0.10f;
        private static final long VIBRATE_DURATION = 200L;

        private MediaPlayer mMediaPlayer;
        private boolean mPlayBeep;
        private boolean mPlayerReady;
        private boolean mClose;

        BeepManager() {
            if (DEBUG) {
                Log.d(LOG_TAG, "BeepManager.BeepManager");
            }

            mPlayBeep = true;
            mPlayerReady = false;
            mClose = false;
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null) {
                if (audioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
                    mPlayBeep = false;
                }
            }

            if (mPlayBeep && mMediaPlayer == null) {
                setVolumeControlStream(AudioManager.STREAM_MUSIC);
                mMediaPlayer = new MediaPlayer();
                AudioAttributes.Builder builder = new AudioAttributes.Builder();
                builder.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC);
                builder.setUsage(AudioAttributes.USAGE_NOTIFICATION);
                mMediaPlayer.setAudioAttributes(builder.build());
                mMediaPlayer.setOnPreparedListener(this);
                mMediaPlayer.setOnCompletionListener(this);

                try (AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.beep)) {
                    mMediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
                    mMediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                    mMediaPlayer.prepareAsync();
                } catch (Exception exception) {
                    Log.w(LOG_TAG, exception);
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                }
            }
        }

        @Override
        public void onPrepared(@NonNull MediaPlayer player) {
            if (DEBUG) {
                Log.d(LOG_TAG, "onPrepared");
            }

            mPlayerReady = true;
        }

        @Override
        public boolean onError(@NonNull MediaPlayer player, int what, int extra) {
            if (DEBUG) {
                Log.d(LOG_TAG, "onError");
            }

            mMediaPlayer.release();
            mMediaPlayer = null;
            return true;
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            if (DEBUG) {
                Log.d(LOG_TAG, "BeepManager.onCompletion");
            }

            if (mClose) {
                mMediaPlayer.release();
                mMediaPlayer = null;
            } else {
                mMediaPlayer.seekTo(0);
            }
        }

        synchronized void playBeepSoundAndVibrate() {
            if (DEBUG) {
                Log.d(LOG_TAG, "BeepManager.playBeepSoundAndVibrate");
            }

            if (mPlayBeep && mMediaPlayer != null && mPlayerReady) {
                mMediaPlayer.start();
            }

            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null) {
                vibrator.vibrate(VIBRATE_DURATION);
            }
        }

        synchronized void close() {
            if (DEBUG) {
                Log.d(LOG_TAG, "BeepManager.close");
            }

            mClose = true;
            if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
        }
    }

    protected volatile CameraManager mCameraManager;
    protected BeepManager mBeepManager;
    protected AmbientLightManager mAmbientLightManager;

    protected View mCameraView;
    protected TextureView mTextureView;
    protected TextView mMessageView;
    protected ImageView mQRCodeView;

    private SurfaceTexture mSurfaceTexture;
    protected ViewFinderView mViewFinder;
    protected ViewFinderResultPointCallback mViewFinderResultPointCallback;
    private final QRCodeReader mQRCodeReader = new QRCodeReader();
    private final EnumMap<DecodeHintType, Object> mHints = new EnumMap<>(DecodeHintType.class);
    private Rect mFramingRectInPreview;
    protected Bitmap mQRCodeBitmap;

    protected boolean mDeferedOnCreateInternal = false;
    private boolean mDeferredReadTwincode = false;
    private boolean mQRCodeScanned = false;
    protected boolean mCameraGranted = false;
    private boolean mProblemSent = false;
    protected boolean mScanSelect = true;

    // On some old devices, the image analysis of the first frame received takes a huge amount of time
    // and the preview appears frozen during that first analysis (between 1 to 15 seconds).  A work around
    // is to reduce the image size that we give to the QR-code analyser only for that first frame.
    // This appears to give good results (seen on Samsung GT-I9195, Sansung SM-A320FL, Huawei DRA LR21).
    private boolean mSkipFirstFrame = true;

    private boolean mShowAlertOnResume = false;

    private ActivityResultLauncher<PickVisualMediaRequest> mMediaPicker;

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

        mDeferedOnCreateInternal = true;

        initViews();

        mViewFinderResultPointCallback = new ViewFinderResultPointCallback(mViewFinder);

        mHints.put(DecodeHintType.NEED_RESULT_POINT_CALLBACK, mViewFinderResultPointCallback);

        mMediaPicker = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                onPickMediaInGallery(uri);
            }
        });
    }

    @Override
    protected void onPause() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPause");
        }

        super.onPause();

        // Restore the light.
        if (mAmbientLightManager != null) {
            mAmbientLightManager.stop();
            mAmbientLightManager = null;
        }

        // Release the camera and any resource while we are not active.
        if (mCameraManager != null) {
            mCameraManager.close();
            mCameraManager = null;
        }

        // Release the beep manager to avoid consuming the battery (see release()).
        if (mBeepManager != null) {
            mBeepManager.close();
            mBeepManager = null;
        }
    }

    @Override
    protected void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        super.onResume();

        if (mBeepManager == null) {
            mBeepManager = new BeepManager();
        }

        // Activate the camera if the scan is enabled.
        if (mScanSelect) {
            mCameraManager = createCameraManager(mTextureView, this, CameraManager.Mode.QRCODE);

            mAmbientLightManager = new AmbientLightManager();
            setupCamera();
        }

        if (mShowAlertOnResume) {
            mShowAlertOnResume = false;
            incorrectQRCode(getString(R.string.capture_activity_incorrect_qrcode));
        }
    }

    //
    // Override Activity methods
    //

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onKeyDown: keyCode=" + keyCode + " keyEvent=" + keyEvent);
        }

        switch (keyCode) {
            case KeyEvent.KEYCODE_FOCUS:
            case KeyEvent.KEYCODE_CAMERA:

                return true;

            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (mCameraManager != null) {
                    mCameraManager.setTorch(CameraManager.FlashMode.OFF);
                }
                if (mAmbientLightManager != null) {
                    mAmbientLightManager.stop();
                }

                return true;

            case KeyEvent.KEYCODE_VOLUME_UP:
                if (mCameraManager != null) {
                    mCameraManager.setTorch(CameraManager.FlashMode.SINGLE);
                }
                if (mAmbientLightManager != null) {
                    mAmbientLightManager.start();
                }

                return true;
        }

        return super.onKeyDown(keyCode, keyEvent);
    }

    //
    // Implements TextureView.SurfaceTextureListener methods
    //

    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int width, int height) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSurfaceTextureAvailable: surface=" + surfaceTexture + " width=" + width + " height" + height);
        }

        mSurfaceTexture = surfaceTexture;
        if (mCameraManager == null && mTextureView != null) {
            mCameraManager = createCameraManager(mTextureView, this, CameraManager.Mode.QRCODE);
        }
        setupCamera();
    }

    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSurfaceTextureUpdated: surface=" + surface);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int width, int height) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSurfaceTextureSizeChanged: surfaceTexture=" + surfaceTexture + " width=" + width + "height=" + height);
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSurfaceTextureDestroyed: surfaceTexture=" + surfaceTexture);
        }

        if (mCameraManager != null) {
            mCameraManager.close();
            mCameraManager = null;
        }

        if (mAmbientLightManager != null) {
            mAmbientLightManager.stop();
        }
        mSurfaceTexture = null;

        return true;
    }

    //
    // Implements CameraManager.CameraCallback methods
    //

    @Override
    public void onCameraReady() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCameraReady");
        }

        // 1/ Don't run this from the main UI thread: it must be executed from the Camera Thread
        //    to make sure the mFramingRectInPreview is initialized after we return.
        // 2/ Because we run from the Camera Thread, if opening the camera is very slow, the camera manager
        //    instance could be released (ie, a Camera close is queued and will be handled by Camera Thread).
        final CameraManager cameraManager = mCameraManager;
        if (cameraManager != null && cameraManager.isOpened()) {
            mFramingRectInPreview = mViewFinder.setCaptureActivity(this, cameraManager);
        }
    }

    @Override
    public boolean onPicture(@NonNull byte[] data, int width, int height) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPicture width=" + width + " height=" + height);
        }

        // We run from the Camera Thread and it is possible that the Camera manager instance is fully released.
        final CameraManager cameraManager = mCameraManager;
        if (cameraManager == null || !cameraManager.isOpened() || mFramingRectInPreview == null) {

            // Stop sending pictures.
            return false;
        }

        try {
            PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(data, width, height,
                    mFramingRectInPreview.left, mFramingRectInPreview.top,
                    mSkipFirstFrame ? 100 : mFramingRectInPreview.width(),
                    mSkipFirstFrame ? 100 : mFramingRectInPreview.height(), false);
            mSkipFirstFrame = false;
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            Result result = mQRCodeReader.decode(bitmap, mHints);
            runOnUiThread(() -> handleDecode(result));
            return false;

        } catch (ReaderException | OutOfMemoryError exception) {
            // continue
        } catch (Exception otherException) {
            if (!mProblemSent) {
                mProblemSent = true;

                String pb = "QR-code exception: " +
                        otherException +
                        "\n" +
                        "Data length=" + data.length + "\n" +
                        "Camera x=" + width + " y=" + height + "\n" +
                        "Camera orientation " + cameraManager.getDisplayOrientation() + "\n" +
                        "Camera facing " + cameraManager.isCameraFacingFront() + "\n" +
                        "Frame rect left=" + mFramingRectInPreview.left + " top=" + mFramingRectInPreview.top +
                        " width=" + mFramingRectInPreview.width() + " height=" + mFramingRectInPreview.height() +
                        "\nView area left=" + mViewFinder.mRectArea.left + " top=" + mViewFinder.mRectArea.top +
                        " width=" + mViewFinder.mRectArea.width() + " height=" + mViewFinder.mRectArea.height();
                // SCz getTwinmeContext().sendProblemReport(LOG_TAG, pb);
            }
            return false;

        } finally {
            mQRCodeReader.reset();
        }

        // Send pictures only if we are in foreground (stop if we are paused).
        return isResuming();
    }

    @Override
    public void onRecordVideoStart() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRecordVideoStart");
        }
    }

    @Override
    public void onRecordVideoStop(File videoFile) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRecordVideoStop: videoFile=" + videoFile);
        }
    }

    @Override
    public void onCameraError(@NonNull CameraManager.ErrorCode errorCode) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCameraReady");
        }

        runOnUiThread(() -> {
            switch (errorCode) {
                case NO_CAMERA:
                    onError(getString(R.string.capture_activity_no_camera));
                    break;

                case NO_PERMISSION:
                    break;

                case CAMERA_ERROR:
                case CAMERA_IN_USE:
                    onError(getString(R.string.capture_activity_create_camera_error));
                    break;
            }
        });
    }

    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    public void onRequestPermissions(@NonNull Permission[] grantedPermissions) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRequestPermissions grantedPermissions=" + Arrays.toString(grantedPermissions));
        }

        boolean grantedCamera = false;
        boolean storageReadAccessGranted = false;
        for (Permission grantedPermission : grantedPermissions) {
            switch (grantedPermission) {
                case CAMERA:
                    grantedCamera = true;
                    break;

                case READ_EXTERNAL_STORAGE:
                    storageReadAccessGranted = true;
                    break;
            }
        }

        mCameraGranted = grantedCamera;

        if (mDeferedOnCreateInternal) {
            mDeferedOnCreateInternal = false;
            if (grantedCamera) {
                setupCamera();
            }
        }
        permissionCameraResult();

        if (mDeferredReadTwincode) {
            mDeferredReadTwincode = false;
            if (storageReadAccessGranted) {
                openGallery();
            } else {
                message(getString(R.string.application_denied_permissions), 0L, new DefaultMessageCallback(R.string.application_ok) {
                });
            }
        }
    }

    @Override
    public void setupDesign() {
        if (DEBUG) {
            Log.d(LOG_TAG, "setupDesign");
        }

        DESIGN_FRAMING_WIDTH_RATIO = (Design.DISPLAY_WIDTH - 64f) / Design.DISPLAY_WIDTH;
        DESIGN_FRAMING_LEFT_RATIO = (1f - DESIGN_FRAMING_WIDTH_RATIO) / 2f;
        DESIGN_FRAMING_TOP_RATIO = (1f - DESIGN_FRAMING_HEIGHT_RATIO) / 2f;
        DESIGN_FRAMING_RIGHT_RATIO = (1f + DESIGN_FRAMING_WIDTH_RATIO) / 2f;
        DESIGN_FRAMING_BOTTOM_RATIO = (1f + DESIGN_FRAMING_HEIGHT_RATIO) / 2f;
        DESIGN_FRAMING_LINE_THICKNESS_RATIO = 4f / Design.DISPLAY_WIDTH;
        DESIGN_FRAMING_LINE_LENGTH_X_RATIO = 28f / Design.DISPLAY_WIDTH;
        DESIGN_FRAMING_LINE_LENGTH_Y_RATIO = 28f / 974f;
        POINT_SIZE = (int) (6 * Design.WIDTH_RATIO);
    }

    //
    // Private Methods
    //

    protected void checkCameraPermission() {
        if (DEBUG) {
            Log.d(LOG_TAG, "checkCameraPermission");
        }

        Permission[] permissions = new Permission[]{Permission.CAMERA};
        if (checkPermissions(permissions)) {
            mDeferedOnCreateInternal = false;
            mCameraGranted = true;
            setupCamera();
            permissionCameraResult();
        }
    }

    protected void setupCamera() {
        if (DEBUG) {
            Log.d(LOG_TAG, "setupCamera");
        }

        // If the permission is not yet granted, the camera manager is not yet initialized.
        if (mCameraGranted && mCameraManager != null && mSurfaceTexture != null) {
            mCameraManager.open(mSurfaceTexture, true);
        }
    }

    //
    // Private methods
    //

    protected abstract void initViews();

    protected abstract void incorrectQRCode(String message);

    protected void onImportFromGalleryClick() {

        openGallery();
    }

    private void openGallery() {
        if (DEBUG) {
            Log.d(LOG_TAG, "openGallery");
        }

        if (mMediaPicker != null) {
            launch(mMediaPicker, new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        }
    }

    private void onPickMediaInGallery(Uri uri) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPickMediaInGallery: " + uri);
        }

        if (uri != null) {
            boolean found = false;

            // When we are back from image selection, the beep manager is gone and it will be created by onRestore().
            // We need it in case the image is a valid QR-code since handleDecode() will use it!.
            if (mBeepManager == null) {
                mBeepManager = new BeepManager();
            }
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                if (bitmap != null) {
                    LuminanceSource source = new RGBLuminanceSource(bitmap);
                    BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
                    QRCodeReader reader = new QRCodeReader();
                    try {
                        Result result = reader.decode(binaryBitmap);
                        handleDecode(result);
                        found = true;
                    } catch (NotFoundException e) {
                        Log.d(LOG_TAG, "NotFoundException: " + e);
                    } catch (ChecksumException e) {
                        Log.d(LOG_TAG, "ChecksumException: " + e);
                    } catch (FormatException e) {
                        Log.d(LOG_TAG, "FormatException: " + e);
                    }
                }
            } catch (IOException e) {
                Log.d(LOG_TAG, "FormatException: " + e);
            } catch (OutOfMemoryError e) {
                Log.e(LOG_TAG, "OutOfMemoryError: " + e);
            }

            if (!found) {
                if (mPaused) {
                    mShowAlertOnResume = true;
                } else {
                    incorrectQRCode(getString(R.string.capture_activity_incorrect_qrcode));
                }
            }
        }
    }

    protected void updateQRCode(@Nullable String url) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateQRCode");
        }

        if (mQRCodeView == null || url == null) {

            return;
        }

        BitMatrix result;
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.MARGIN, 0);
            result = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, QRCODE_PIXEL_WIDTH, QRCODE_PIXEL_HEIGHT, hints);
        } catch (Exception exception) {
            Log.e(LOG_TAG, "updateQrcode: exception=" + exception);

            return;
        }

        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }

        mQRCodeBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mQRCodeBitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        mQRCodeView.setImageBitmap(mQRCodeBitmap);
    }

    protected void onSettingsClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSettingsClick");
        }

        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    protected abstract void handleDecode(Uri uri);

    protected abstract void onError(String message);

    protected void permissionCameraResult() {
        if (DEBUG) {
            Log.d(LOG_TAG, "permissionCameraResult");
        }

        if (mCameraGranted) {
            mMessageView.setText(getResources().getString(R.string.capture_activity_message));
            mMessageView.postDelayed(() -> mMessageView.setVisibility(View.GONE), 5000);
        } else {
            mMessageView.setText(getResources().getString(R.string.application_permission_scan_code));
        }

        ShapeDrawable cameraViewBackground = new ShapeDrawable();
        cameraViewBackground.getPaint().setColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        mCameraView.setBackground(cameraViewBackground);
    }

    private void handleDecode(Result rawResult) {
        if (DEBUG) {
            Log.d(LOG_TAG, "handleDecode: rawResult=" + rawResult);
        }

        // It is possible that the activity is paused or terminated while we decoded a valid QR-code.
        if (mBeepManager == null || mQRCodeScanned) {

            return;
        }

        // The handleDecode can be called several times since several QR-code pictures can be decoded
        // before we stop the preview.
        mQRCodeScanned = true;
        mBeepManager.playBeepSoundAndVibrate();

        Uri uri = Uri.parse(rawResult.getText());
        handleDecode(uri);
    }
}
