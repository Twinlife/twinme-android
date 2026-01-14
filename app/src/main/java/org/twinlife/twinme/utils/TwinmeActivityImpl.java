/*
 *  Copyright (c) 2013-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Romain Kolb (romain.kolb@skyrock.com)
 */

package org.twinlife.twinme.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.BaseService.ErrorCode;
import org.twinlife.twinlife.ConnectionStatus;
import org.twinlife.twinlife.JobService;
import org.twinlife.twinlife.TwincodeURI;
import org.twinlife.twinme.TwinmeContext;
import org.twinlife.twinme.TwinmeApplication.Feature;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.skin.DisplayMode;
import org.twinlife.twinme.ui.Settings;
import org.twinlife.twinme.ui.TwinmeActivity;
import org.twinlife.twinme.ui.TwinmeApplication;
import org.twinlife.twinme.ui.TwinmeApplicationImpl;
import org.twinlife.twinme.ui.callActivity.CallActivity;
import org.twinlife.twinme.ui.conversationActivity.AbstractPreviewActivity;
import org.twinlife.twinme.utils.camera.Camera1ManagerImpl;
import org.twinlife.twinme.utils.camera.Camera2ManagerImpl;
import org.twinlife.twinme.utils.camera.CameraManager;
import org.webrtc.Camera2Enumerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ScheduledFuture;

@SuppressLint("Registered")
public class TwinmeActivityImpl extends AppCompatActivity implements TwinmeActivity, OnRequestPermissionsResultCallback, InfoFloatingView.Observer {
    private static final String LOG_TAG = "TwinmeActivityImpl";
    private static final boolean DEBUG = false;

    private static final int PERMISSIONS_REQUEST_CODE = 0xFF;

    public static final float DESIGN_TOAST_RADIUS = 22f;
    private static final int DESIGN_INFO_FLOATING_SIZE = 60;

    private TwinmeApplicationImpl mTwinmeApplication;
    private TwinmeContext mTwinmeContext;

    private final Object mDialogLock = new Object();
    private Toast mToast;
    private AlertDialog mAlertDialog;
    private CountDownTimer mCountDownTimer;
    private ScheduledFuture<?> mProbeTimer;
    private NetworkStatus mNetworkStatus;
    protected boolean mResumed;
    private boolean mAddBackgroundDrawable = false;
    protected boolean mPaused = false;
    private boolean mShowAlert = false;
    protected int mBackgroundColor = Design.WHITE_COLOR;
    @Nullable
    protected InfoFloatingView mInfoFloatingView;

    @SuppressLint("NewApi")
    @NonNull
    static String toSystemPermission(Permission permission) {

        switch (permission) {
            case ACCESS_COARSE_LOCATION:
                return Manifest.permission.ACCESS_COARSE_LOCATION;

            case ACCESS_FINE_LOCATION:
                return Manifest.permission.ACCESS_FINE_LOCATION;

            case ACCESS_BACKGROUND_LOCATION:
                return Manifest.permission.ACCESS_BACKGROUND_LOCATION;

            case CAMERA:
                return Manifest.permission.CAMERA;

            case RECORD_AUDIO:
                return Manifest.permission.RECORD_AUDIO;

            case READ_EXTERNAL_STORAGE:
                return Manifest.permission.READ_EXTERNAL_STORAGE;

            case BLUETOOTH_CONNECT:
                return Manifest.permission.BLUETOOTH_CONNECT;

            case READ_MEDIA_AUDIO:
                return Manifest.permission.READ_MEDIA_AUDIO;

            case POST_NOTIFICATIONS:
                return Manifest.permission.POST_NOTIFICATIONS;

            case WRITE_EXTERNAL_STORAGE:
            default: // not reached
                return Manifest.permission.WRITE_EXTERNAL_STORAGE;
        }
    }

    @NonNull
    static Permission toPermission(String permission) {

        switch (permission) {
            case Manifest.permission.ACCESS_COARSE_LOCATION:
                return Permission.ACCESS_COARSE_LOCATION;

            case Manifest.permission.ACCESS_FINE_LOCATION:
                return Permission.ACCESS_FINE_LOCATION;

            case Manifest.permission.ACCESS_BACKGROUND_LOCATION:
                return Permission.ACCESS_BACKGROUND_LOCATION;

            case Manifest.permission.CAMERA:
                return Permission.CAMERA;

            case Manifest.permission.RECORD_AUDIO:
                return Permission.RECORD_AUDIO;

            case Manifest.permission.READ_EXTERNAL_STORAGE:
                return Permission.READ_EXTERNAL_STORAGE;

            case Manifest.permission.BLUETOOTH_CONNECT:
                return Permission.BLUETOOTH_CONNECT;

            case Manifest.permission.READ_MEDIA_AUDIO:
                return Permission.READ_MEDIA_AUDIO;

            case Manifest.permission.POST_NOTIFICATIONS:
                return Permission.POST_NOTIFICATIONS;

            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
            default: // not reached
                return Permission.WRITE_EXTERNAL_STORAGE;
        }
    }

    //
    // Override Activity methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        mTwinmeApplication = TwinmeApplicationImpl.getInstance(this);
        if (mTwinmeApplication != null) {
            mTwinmeContext = mTwinmeApplication.getTwinmeContext();
            Design.init(this, mTwinmeApplication);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onOptionsItemSelected: item=" + item);
        }

        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return true;
    }

    @Override
    protected void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        super.onResume();

        mResumed = true;
        mPaused = false;
        if (!mTwinmeApplication.isRunning()) {
            finish();
        }

        if (!mAddBackgroundDrawable) {
            getWindow().getDecorView().setBackgroundColor(mBackgroundColor);
        }
        mTwinmeApplication.setCurrentActivity(this);

        if (getTwinmeApplication().lastScreenHidden()) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
    }

    @Override
    protected void onPause() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPause");
        }

        super.onPause();

        // Hide the toast if we go in background, otherwise it remains on the screen.
        synchronized (mDialogLock) {
            if (mToast != null) {
                mToast.cancel();
                mToast = null;
            }
            if (mAlertDialog != null) {
                mAlertDialog.dismiss();
                mAlertDialog = null;
            }
        }

        // Protect concurrent access with onConnect()/onDisconnect().
        synchronized (this) {
            // If a connectivity monitor is running, stop it.
            if (mProbeTimer != null) {
                mProbeTimer.cancel(false);
                mProbeTimer = null;
            }
            mNetworkStatus = null;
            mResumed = false;
            mPaused = true;
        }
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        // Make sure the info floating view is destroyed because it schedules some work.
        if (mInfoFloatingView != null) {
            mInfoFloatingView.hideView();
            mInfoFloatingView = null;
        }

        super.onDestroy();
    }

    protected boolean isResuming() {
        if (DEBUG) {
            Log.d(LOG_TAG, "isResuming");
        }

        return mResumed;
    }

    /**
     * Check if a message() is being displayed.
     *
     * @return true if a message dialog is active.
     */
    protected boolean isMessageDisplayed() {
        if (DEBUG) {
            Log.d(LOG_TAG, "isMessageDisplayed");
        }

        return mAlertDialog != null || mShowAlert;
    }

    /**
     * Check if the feature is enabled for the application.
     *
     * @param feature the feature identification.
     * @return true if the feature is enabled.
     */
    public boolean isFeatureSubscribed(@NonNull Feature feature) {

        return mTwinmeApplication.isFeatureSubscribed(feature);
    }

    //
    // Implementation of TwinlifeActivity interface
    //

    @NonNull
    @Override
    public final TwinmeApplication getTwinmeApplication() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getTwinmeApplication");
        }

        return mTwinmeApplication;
    }

    @NonNull
    @Override
    public final TwinmeContext getTwinmeContext() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getTwinmeApplication");
        }

        return mTwinmeContext;
    }

    @NonNull
    public final Bitmap getDefaultAvatar() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getDefaultAvatar");
        }

        return mTwinmeApplication.getDefaultAvatar();
    }

    @NonNull
    public final Bitmap getAnonymousAvatar() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getAnonymousAvatar");
        }

        return mTwinmeApplication.getAnonymousAvatar();
    }

    @NonNull
    public String getAnonymousName() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getAnonymousName");
        }

        return mTwinmeApplication.getAnonymousName();
    }

    @Override
    public final boolean checkPermissions(@NonNull Permission[] permissions) {
        if (DEBUG) {
            Log.d(LOG_TAG, "checkPermissions permissions=" + Arrays.toString(permissions));
        }

        return requestPermissions(permissions, true);
    }

    @Override
    public final boolean checkPermissionsWithoutRequest(@NonNull Permission[] permissions) {
        if (DEBUG) {
            Log.d(LOG_TAG, "checkPermissionsWithoutRequest=" + Arrays.toString(permissions));
        }

        return requestPermissions(permissions, false);
    }

    public void setBackgroundColor(int backgroundColor) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setBackgroundColor: " + backgroundColor);
        }

        mBackgroundColor = backgroundColor;

        if (mResumed) {

            getWindow().getDecorView().setBackgroundColor(mBackgroundColor);
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setTitle= " + title);
        }

        TextView titleView = findViewById(R.id.toolbar_title);

        if (titleView != null) {
            super.setTitle("");
            titleView.setText(title);
        } else {
            super.setTitle(title);
        }
    }

    public void runOnTwinlifeThread(@NonNull Runnable runnable) {

        mTwinmeContext.execute(runnable);
    }

    public void setSubTitle(CharSequence subTitle) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setSubTitle= " + subTitle);
        }

        TextView subTitleView = findViewById(R.id.toolbar_subtitle);
        subTitleView.setText(subTitle);
    }

    public void showSubTitle() {
        if (DEBUG) {
            Log.d(LOG_TAG, "showSubTitle");
        }

        TextView subTitleView = findViewById(R.id.toolbar_subtitle);
        subTitleView.setVisibility(View.VISIBLE);
    }

    public void hideSubTitle() {
        if (DEBUG) {
            Log.d(LOG_TAG, "hideSubTitle");
        }

        TextView subTitleView = findViewById(R.id.toolbar_subtitle);
        subTitleView.setVisibility(View.GONE);
    }

    public boolean canShowInfoFloatingView() {
        if (DEBUG) {
            Log.d(LOG_TAG, "canShowInfoFloatingView");
        }

        return true;
    }

    protected void showBackButton(boolean show) {
        if (DEBUG) {
            Log.d(LOG_TAG, "showBackButton: show=" + show);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(show);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.arrow_back);
            getSupportActionBar().setHomeActionContentDescription(getString(R.string.application_back));
        }
    }

    protected void showToolBar(boolean show) {
        if (DEBUG) {
            Log.d(LOG_TAG, "showToolBar: show=" + show);
        }

        if (getSupportActionBar() != null) {
            if (show) {
                getSupportActionBar().show();
            } else {
                getSupportActionBar().hide();
            }
        }
    }

    protected void setStatusBarColor(int color) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setStatusBarColor");
        }

        Window window = getWindow();
        window.setNavigationBarColor(mBackgroundColor);

        updateStatusBarColor(color);
    }

    public void setStatusBarColor(int statusBarColor, int navigationBarColor) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setStatusBarColor");
        }

        Window window = getWindow();
        window.setNavigationBarColor(navigationBarColor);
        updateStatusBarColor(statusBarColor);
    }

    public void setStatusBarDrawable(Drawable drawable, int navigationBarColor) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setStatusBarDrawable");
        }

        Window window = getWindow();
        window.setNavigationBarColor(navigationBarColor);

        updateStatusBar(drawable);
    }

    public void setStatusBarColor() {
        if (DEBUG) {
            Log.d(LOG_TAG, "setStatusBarColor");
        }

        Window window = getWindow();
        window.setNavigationBarColor(mBackgroundColor);

        updateStatusBarColor(Design.TOOLBAR_COLOR);
    }

    protected void openMenuColor() {
        if (DEBUG) {
            Log.d(LOG_TAG, "openMenuColor");
        }

        Window window = getWindow();
        window.setNavigationBarColor(Design.OVERLAY_VIEW_COLOR);

        boolean darkMode = false;
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        int displayMode = Settings.displayMode.getInt();
        if ((currentNightMode == Configuration.UI_MODE_NIGHT_YES && displayMode == DisplayMode.SYSTEM.ordinal())  || displayMode == DisplayMode.DARK.ordinal()) {
            darkMode = true;
        }

        if (darkMode) {
            updateStatusBarColor(Design.TOOLBAR_COLOR);
        } else {
            updateStatusBarColor(Design.mixColors(Design.getMainStyle(), Design.OVERLAY_VIEW_COLOR));
        }
    }

    private boolean isFullScreen() {
        if (DEBUG) {
            Log.d(LOG_TAG, "isFullScreen");
        }

        Window window = getWindow();
        return window.getDecorView().getSystemUiVisibility() == View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
    }

    private void updateStatusBarColor(int color) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateStatusBarColor: color=" + color);
        }

        mAddBackgroundDrawable = false;

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(color);
        window.setBackgroundDrawable(null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }
    }

    private void updateStatusBar(Drawable drawable) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateStatusBar: drawable=" + drawable);
        }

        mAddBackgroundDrawable = true;

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setBackgroundDrawable(drawable);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }
    }

    /**
     * Create a camera manager for the current activity.  The camera manager uses either the Camera 1 or Camera 2 API.
     * Depending on the mode, the picture callback onPicture() is called on a regular basis (Mode.QR_CODE) or when
     * a picture is asked with takePicture().
     *
     * @param textureView    the texture view where the camera will be displayed.
     * @param cameraCallback the picture callback.
     * @param mode           the camera mode configuration.
     * @return the camera manager.
     */
    protected CameraManager createCameraManager(@NonNull TextureView textureView,
                                                @NonNull CameraManager.CameraCallback cameraCallback,
                                                @NonNull CameraManager.Mode mode) {
        if (DEBUG) {
            Log.d(LOG_TAG, "createCameraManager textureView=" + textureView + " previewCallback=" + cameraCallback + " mode=" + mode);
        }

        // Force the use of Camera 1 when taking a picture (orientation issue with Camera2).
        if (mode == CameraManager.Mode.PHOTO) {

            return new Camera1ManagerImpl(this, textureView, cameraCallback, mode);
        }

        // For Android >= 9, use Camera2 as it provides better results and solves issues on Samsung devices.
        // Device examples: Xiaomi Redmi Android 10, Huawei Y2019 Android 10, Pixel 3 Android 11, Samsung A51.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

            return new Camera2ManagerImpl(this, textureView, cameraCallback, mode);
        }

        // For Android >= 5, also check using WebRTC isSupported() because the device is old and Camera2 may not be correct.
        if (Camera2Enumerator.isSupported(this)) {

            return new Camera2ManagerImpl(this, textureView, cameraCallback, mode);
        }

        return new Camera1ManagerImpl(this, textureView, cameraCallback, mode);
    }

    @Override
    public void onRequestPermissions(@NonNull Permission[] grantedPermissions) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRequestPermissions grantedPermissions=" + Arrays.toString(grantedPermissions));
        }
    }

    @Override
    public void toast(@NonNull String text) {
        if (DEBUG) {
            Log.d(LOG_TAG, "toast: text=" + text);
        }

        if (isNotRunning()) {

            return;
        }

        synchronized (mDialogLock) {
            if (mToast != null) {
                mToast.cancel();
            }

            LayoutInflater inflater = getLayoutInflater();
            View toastLayout = inflater.inflate(R.layout.toast, findViewById(R.id.toast_layout));

            View roundedView = toastLayout.findViewById(R.id.toast_content);
            float radius = DESIGN_TOAST_RADIUS * Resources.getSystem().getDisplayMetrics().density;
            float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
            ShapeDrawable popupViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
            popupViewBackground.getPaint().setColor(Design.POPUP_BACKGROUND_COLOR);
            roundedView.setBackground(popupViewBackground);

            TextView toastTextView = toastLayout.findViewById(R.id.toast_text);
            toastTextView.setText(text);
            Design.updateTextFont(toastTextView, Design.FONT_MEDIUM30);
            toastTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

            mToast = new Toast(this);
            mToast.setDuration(Toast.LENGTH_LONG);
            mToast.setView(toastLayout);
            mToast.setGravity(Gravity.TOP, 0, 0);
            mToast.show();
        }
    }

    @Override
    public void message(@NonNull String message, long timeout, MessageCallback messageCallback) {
        if (DEBUG) {
            Log.d(LOG_TAG, "message: message=" + message + " timeout=" + timeout + " messageCallback=" + messageCallback);
        }

        if (isNotRunning()) {

            return;
        }

        mShowAlert = true;

        ViewGroup rootView = (ViewGroup) ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);

        AlertMessageView alertMessageView = new AlertMessageView(this, null);

        if (isFullScreen()) {
            alertMessageView.setWindowHeight(getWindow().getDecorView().getHeight());
        }

        if (this instanceof CallActivity || this instanceof AbstractPreviewActivity) {
            alertMessageView.setForceDarkMode(true);
        }

        alertMessageView.setTitle(getString(R.string.conversation_activity_menu_item_view_info_title));
        alertMessageView.setMessage(Html.fromHtml(message).toString());

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
                rootView.removeView(alertMessageView);
                mShowAlert = false;
                if (isFullScreen()) {
                    setFullscreen();
                } else {
                    setStatusBarColor();
                }

                if (messageCallback != null) {
                    messageCallback.onClick();
                }
            }
        };
        alertMessageView.setObserver(observer);

        rootView.addView(alertMessageView);
        alertMessageView.show();

        if (isFullScreen()) {
            Window window = getWindow();
            window.setNavigationBarColor(Design.POPUP_BACKGROUND_COLOR);
        } else {
            int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
            setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
        }
    }

    @Override
    public void messageSettings(@NonNull String message, long timeout, SettingsMessageCallback settingsMessageCallback) {
        if (DEBUG) {
            Log.d(LOG_TAG, "messageSettings: message=" + message + " timeout=" + timeout + " settingsMessageCallback=" + settingsMessageCallback);
        }

        if (isNotRunning()) {

            return;
        }

        mShowAlert = true;

        ViewGroup rootView = (ViewGroup) ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);

        DefaultConfirmView defaultConfirmView = new DefaultConfirmView(this, null);
        defaultConfirmView.setTitle(getString(R.string.deleted_account_activity_warning));
        defaultConfirmView.setMessage(message);
        defaultConfirmView.setImage(null);
        defaultConfirmView.setConfirmTitle(getString(R.string.application_authorization_go_settings));

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
                rootView.removeView(defaultConfirmView);
                mShowAlert = false;
                setFullscreen();

                if (settingsMessageCallback != null) {
                    if (fromConfirmAction) {
                        settingsMessageCallback.onSettingsClick();
                    } else {
                        settingsMessageCallback.onCancelClick();
                    }
                }

            }
        };
        defaultConfirmView.setObserver(observer);
        rootView.addView(defaultConfirmView);
        defaultConfirmView.show();

        if (isFullScreen()) {
            Window window = getWindow();
            window.setNavigationBarColor(Design.POPUP_BACKGROUND_COLOR);
        } else {
            int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
            setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
        }
    }

    /**
     * Set the layout for fullscreen display.
     */
    protected void setFullscreen() {
        if (DEBUG) {
            Log.d(LOG_TAG, "setFullscreen");
        }

        final Window window = getWindow();
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Design.WHITE_COLOR);
    }

    @Override
    public void error(@NonNull String message, @Nullable Runnable errorCallback) {
        if (DEBUG) {
            Log.d(LOG_TAG, "error: message=" + message + "errorCallback=" + errorCallback);
        }

        if (isNotRunning()) {

            return;
        }

        mShowAlert = true;

        ViewGroup rootView = (ViewGroup) ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);

        AlertMessageView alertMessageView = new AlertMessageView(this, null);

        if (this instanceof CallActivity || this instanceof AbstractPreviewActivity) {
            alertMessageView.setForceDarkMode(true);
        }

        alertMessageView.setTitle(getString(R.string.deleted_account_activity_warning));
        alertMessageView.setMessage(message);

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
                rootView.removeView(alertMessageView);
                mShowAlert = false;
                if (isFullScreen()) {
                    setFullscreen();
                } else {
                    setStatusBarColor();
                }

                if (errorCallback != null) {
                    errorCallback.run();
                }
            }
        };
        alertMessageView.setObserver(observer);

        rootView.addView(alertMessageView);
        alertMessageView.show();

        if (isFullScreen()) {
            Window window = getWindow();
            window.setNavigationBarColor(Design.POPUP_BACKGROUND_COLOR);
        } else {
            int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
            setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
        }
    }

    @Override
    public void onError(ErrorCode errorCode, @Nullable String message, @Nullable Runnable errorCallback) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onError: errorCode=" + errorCode + " message=" + message + " errorCallback=" + errorCallback);
        }

        mTwinmeApplication.onError(this, errorCode, message, errorCallback);
    }

    public String getLinkError(@NonNull ErrorCode errorCode, @StringRes int defaultMessage) {

        if (errorCode == ErrorCode.BAD_REQUEST) {
            return getString(R.string.add_contact_activity_scan_error_incorect_link);
        } else if (errorCode == ErrorCode.FEATURE_NOT_IMPLEMENTED) {
            return getString(R.string.add_contact_activity_scan_error_not_managed_link);
        } else if (errorCode == ErrorCode.ITEM_NOT_FOUND) {
            return getString(R.string.add_contact_activity_scan_error_corrupt_link);
        } else {
            return getString(defaultMessage);
        }
    }

    public String getLinkError(@NonNull TwincodeURI.Kind kind, @StringRes int defaultMessage) {

        if (kind == TwincodeURI.Kind.Call) {
            return getString(R.string.add_contact_activity_scan_message_call_link);
        } else if (kind == TwincodeURI.Kind.AccountMigration) {
            return getString(R.string.add_contact_activity_scan_message_migration_link);
        } else if (kind == TwincodeURI.Kind.Transfer) {
            return getString(R.string.add_contact_activity_scan_message_transfer_link);
        } else {
            return getString(defaultMessage);
        }
    }

    //
    // Override OnRequestPermissionsResultCallback methods
    //

    @Override
    public final void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRequestPermissionsResult requestCode=" + requestCode + " permissions=" + Arrays.toString(permissions) + " grantResults=" +
                    Arrays.toString(grantResults));
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != PERMISSIONS_REQUEST_CODE) {

            return;
        }


        ArrayList<Permission> grantedPermissions = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                grantedPermissions.add(toPermission(permissions[i]));
            }
        }

        // onRequestPermissionsResult() is called before onResume() and if the permission was denied
        // we want to display the permission denied dialog message.  However, that dialog is conditionally
        // displayed by mPaused.  onResume() will be called soon after if necessary so it does not matter to set it here.
        mPaused = false;
        onRequestPermissions(grantedPermissions.toArray(new Permission[0]));
    }

    /**
     * Set the back label display and install the action to navigate back.
     *
     * @param labelId the Back label id.
     * @param clickId the Back clickable view.
     * @param l       the onClickListener to invoke.
     * @todo SCz we should rename this method: setTitleAction?
     */
    public void setBackAction(@IdRes int labelId, @IdRes int clickId, @Nullable View.OnClickListener l) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setBackAction labelId=" + labelId + " clickId=" + clickId);
        }

        TextView backLabelView = findViewById(labelId);
        Design.updateTextFont(backLabelView, Design.FONT_REGULAR36);
        backLabelView.setTextColor(Design.FONT_COLOR_DEFAULT);

        View backClickableView = findViewById(clickId);
        backClickableView.setOnClickListener(l);
    }

    public void onConnectionStatusChange(@NonNull ConnectionStatus connectionStatus) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onConnectionStatusChange " + connectionStatus);
        }

        if (mResumed) {
            final TwinmeApplication twinmeApplication = getTwinmeApplication();

            AppStateInfo.InfoFloatingViewType infoFloatingViewType;

            switch (connectionStatus) {
                case CONNECTED:
                    infoFloatingViewType = AppStateInfo.InfoFloatingViewType.CONNECTED;
                    break;

                case CONNECTING:
                    infoFloatingViewType = AppStateInfo.InfoFloatingViewType.CONNECTION_IN_PROGRESS;
                    break;

                case NO_SERVICE:
                    infoFloatingViewType = AppStateInfo.InfoFloatingViewType.NO_SERVICES;
                    break;

                case NO_INTERNET:
                default:
                    infoFloatingViewType = AppStateInfo.InfoFloatingViewType.OFFLINE;
                    break;
            }

            if (connectionStatus == ConnectionStatus.CONNECTED) {
                if (twinmeApplication.showConnectedMessage()) {
                    AppStateInfo appInfo = twinmeApplication.appInfo();
                    if (appInfo == null) {
                        appInfo = new AppStateInfo(AppStateInfo.InfoFloatingViewState.DEFAULT, infoFloatingViewType, null);
                        twinmeApplication.setAppInfo(appInfo);
                    } else {
                        appInfo.setInfoFloatingViewType(infoFloatingViewType);
                    }
                    showInfoFloatingView(appInfo);
                }
            } else {
                twinmeApplication.setShowConnectedMessage(true);

                AppStateInfo appInfo = twinmeApplication.appInfo();
                if (appInfo == null) {
                    appInfo = new AppStateInfo(AppStateInfo.InfoFloatingViewState.DEFAULT, infoFloatingViewType, null);
                    twinmeApplication.setAppInfo(appInfo);
                } else {
                    appInfo.setInfoFloatingViewType(infoFloatingViewType);
                }

                showInfoFloatingView(appInfo);
            }
        }

        if (connectionStatus != ConnectionStatus.CONNECTED) {
            return;
        }

        synchronized (mDialogLock) {
            if (mAlertDialog != null) {
                mAlertDialog.dismiss();
                mAlertDialog = null;
            }
        }

        // Protect concurrent access with onPause()/showNetworkDisconnect()/probeNetwork().
        synchronized (this) {
            // If a connectivity monitor is running, stop it.
            if (mProbeTimer != null) {
                mProbeTimer.cancel(false);
                mProbeTimer = null;
            }
            if (mNetworkStatus != null) {
                mNetworkStatus = null;
            }
        }
    }

    public void showNetworkDisconnect(int actionMessage, @NonNull Runnable errorCallback) {
        if (DEBUG) {
            Log.d(LOG_TAG, "showNetworkDisconnect: errorCallback=" + errorCallback);
        }

        NetworkStatus status;
        synchronized (this) {
            // A network status is already monitoring the connection, nothing to do.
            if (mNetworkStatus != null) {

                return;
            }

            mNetworkStatus = new NetworkStatus();
            status = mNetworkStatus;

            // Launch a probe network task to check the network status periodically.
            final TwinmeActivityImpl activity = this;
            final JobService jobService = getTwinmeContext().getJobService();
            mProbeTimer = jobService.scheduleAtFixedRate(() -> runOnUiThread(() -> activity.probeNetwork(actionMessage, errorCallback)), 1, 1);
        }
        status.getNetworkDiagnostic(getApplicationContext());

        final int delay = status.getPersistentDelay();
        if (delay > 0) {
            toast(getString(R.string.application_network_connecting));

            return;
        }
        reportNetworkProblem(status, actionMessage, errorCallback);
    }

    //
    // Implement InfoFloatingView.Observer methods
    //

    @Override
    public void onHideInfoFloatingView() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onHideInfoFloatingView");
        }

        hideInfoFloatingView();
    }

    @Override
    public void onTouchInfoFloatingView() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onTouchInfoFloatingView");
        }

        final AppStateInfo appInfo = getTwinmeApplication().appInfo();
        if (appInfo != null && mInfoFloatingView != null) {
            appInfo.updateExpirationTime();
            mInfoFloatingView.setAppInfo(appInfo);
        }
    }

    private void showInfoFloatingView(@NonNull AppStateInfo info) {
        if (DEBUG) {
            Log.d(LOG_TAG, "showInfoFloatingView info=" + info);
        }

        if (!canShowInfoFloatingView()) {
            return;
        }

        if (mInfoFloatingView == null) {
            mInfoFloatingView = new InfoFloatingView(this);
            mInfoFloatingView.setObserver(this);
            mInfoFloatingView.setOnInfoClickListener(new InfoFloatingView.OnInfoClickListener() {
                @Override
                public void onInfoClick() {
                    onInfoFloatingViewClick();
                }

                @Override
                public void onInfoLongPress() {

                }
            });

            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.height = layoutParams.width = Design.getHeight(DESIGN_INFO_FLOATING_SIZE);
            addContentView(mInfoFloatingView, layoutParams);

            ViewTreeObserver viewTreeObserver = mInfoFloatingView.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    // Be careful that the view could now be null.
                    if (mInfoFloatingView != null) {
                        ViewTreeObserver viewTreeObserver = mInfoFloatingView.getViewTreeObserver();
                        viewTreeObserver.removeOnGlobalLayoutListener(this);
                        if (info.position() != null) {
                            mInfoFloatingView.setX(info.position().x);
                            mInfoFloatingView.setY(info.position().y);
                            mInfoFloatingView.setVisibility(View.VISIBLE);
                        } else {
                            mInfoFloatingView.moveToTopRight();
                        }
                    }
                }
            });
        } else {
            mInfoFloatingView.setVisibility(View.VISIBLE);
        }

        mInfoFloatingView.setAppInfo(info);
    }

    protected void hideInfoFloatingView() {
        if (DEBUG) {
            Log.d(LOG_TAG, "hideCallFloatingView");
        }

        getTwinmeApplication().setShowConnectedMessage(false);

        if (mInfoFloatingView != null) {
            mInfoFloatingView.hideView();
            mInfoFloatingView = null;
        }
    }

    private void onInfoFloatingViewClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onInfoFloatingViewClick");
        }

        final AppStateInfo appInfo = getTwinmeApplication().appInfo();
        if (appInfo != null) {
            if (appInfo.getState() == AppStateInfo.InfoFloatingViewState.DEFAULT) {
                appInfo.setInfoFloatingViewState(AppStateInfo.InfoFloatingViewState.EXTEND);
            } else {
                appInfo.setInfoFloatingViewState(AppStateInfo.InfoFloatingViewState.DEFAULT);
            }

            if (mInfoFloatingView != null) {
                mInfoFloatingView.setAppInfo(appInfo);
                mInfoFloatingView.tapAction();
            }
        }
    }

    private void probeNetwork(int actionMessage, @NonNull Runnable errorCallback) {
        if (DEBUG) {
            Log.d(LOG_TAG, "probeNetwork: actionMessage=" + actionMessage + " errorCallback=" + errorCallback);
        }

        final NetworkStatus status;
        synchronized (this) {
            if (mNetworkStatus == null || isNotRunning()) {
                return;
            }
            status = mNetworkStatus;
        }
        status.getNetworkDiagnostic(getApplicationContext());
        synchronized (mDialogLock) {
            if (mToast != null) {
                mToast.show();
            }
        }

        // If the probe deadline has passed, raise the alert message to the user.
        final long deadline = status.getProbeDeadline();
        if (deadline < System.currentTimeMillis()) {
            reportNetworkProblem(status, actionMessage, errorCallback);
        }
    }

    private void reportNetworkProblem(NetworkStatus status, int actionMessage, @NonNull Runnable errorCallback) {
        if (DEBUG) {
            Log.d(LOG_TAG, "reportNetworkProblem: actionMessage=" + actionMessage + " errorCallback=" + errorCallback);
        }

        TextView messageText;
        TextView resolutionText;
        synchronized (mDialogLock) {
            if (mProbeTimer != null) {
                mProbeTimer.cancel(false);
                mProbeTimer = null;
            }
            if (mAlertDialog == null) {
                AlertDialog alertDialog = new AlertDialog(this);
                alertDialog.setCancelable(false);
                alertDialog.setup("", Html.fromHtml("???"),
                        getString(R.string.application_ok),
                        () -> {
                            alertDialog.dismiss();
                            synchronized (mDialogLock) {
                                mAlertDialog = null;
                                if (mCountDownTimer != null) {
                                    mCountDownTimer.cancel();
                                    mCountDownTimer = null;
                                }
                            }
                            errorCallback.run();
                        }
                );

                mAlertDialog = alertDialog;
                mAlertDialog.show();
            }

            if (mToast != null) {
                mToast.cancel();
                mToast = null;
            }
            messageText = mAlertDialog.findViewById(R.id.alert_dialog_title);
            resolutionText = mAlertDialog.findViewById(R.id.alert_dialog_message);
        }
        messageText.setText(status.getMessage());
        if (status.getResolution() != 0) {
            resolutionText.setText(status.getResolution());
            resolutionText.setVisibility(View.VISIBLE);
        } else {
            resolutionText.setVisibility(View.GONE);
        }
    }

    private boolean requestPermissions(@NonNull Permission[] permissions, boolean requestPermissions) {

        boolean granted = true;
        ArrayList<String> requestedPermissions = new ArrayList<>();
        for (Permission permission : permissions) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                // On Android < 13, don't ask for these permissions (assume they are granted).
                if (permission == Permission.READ_MEDIA_AUDIO) {
                    continue;
                }
            } else {
                // Don't ask the READ_EXTERNAL_STORAGE on Android >= 13.
                // It is replaced by READ_MEDIA_AUDIO, READ_MEDIA_IMAGES, READ_MEDIA_VIDEO.
                if (permission == Permission.READ_EXTERNAL_STORAGE) {
                    continue;
                }
            }

            // On Android 10+, we must not ask for the WRITE_EXTERNAL_STORAGE permission.
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P || permission != Permission.WRITE_EXTERNAL_STORAGE) {
                String requestedPermission = toSystemPermission(permission);
                requestedPermissions.add(requestedPermission);
                if (ContextCompat.checkSelfPermission(this, requestedPermission) != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                }
            }
        }
        if (granted) {

            return true;
        }

        if (requestPermissions) {
            ActivityCompat.requestPermissions(this, requestedPermissions.toArray(new String[0]), PERMISSIONS_REQUEST_CODE);
        }

        return false;
    }

    //
    // Private Methods
    //

    @SuppressLint("NewApi")
    public boolean isNotRunning() {
        if (DEBUG) {
            Log.d(LOG_TAG, "isNotRunning");
        }

        return mPaused || isFinishing() || isDestroyed();
    }
}
