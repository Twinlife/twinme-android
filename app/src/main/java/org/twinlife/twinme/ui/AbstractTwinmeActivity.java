/*
 *  Copyright (c) 2018-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Romain Kolb (romain.kolb@skyrock.com)
 */

package org.twinlife.twinme.ui;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.percentlayout.widget.PercentRelativeLayout;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.BaseService;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Originator;
import org.twinlife.twinme.models.Space;
import org.twinlife.twinme.services.AbstractTwinmeService;
import org.twinlife.twinme.services.AccountMigrationService;
import org.twinlife.twinme.calls.CallService;
import org.twinlife.twinme.calls.CallStatus;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.accountMigrationActivity.AccountMigrationActivity;
import org.twinlife.twinme.ui.callActivity.CallActivity;
import org.twinlife.twinme.ui.externalCallActivity.ShowExternalCallActivity;
import org.twinlife.twinme.ui.groups.ShowGroupActivity;
import org.twinlife.twinme.ui.rooms.ShowRoomActivity;
import org.twinlife.twinme.utils.AlertMessageView;
import org.twinlife.twinme.utils.AppStateInfo;
import org.twinlife.twinme.utils.CallFloatingView;
import org.twinlife.twinme.utils.InCallInfo;
import org.twinlife.twinme.utils.InfoFloatingView;
import org.twinlife.twinme.utils.TwinmeActivityImpl;

import java.util.UUID;

/**
 * Base class of Twinme UI activity.
 *
 * @todo: we should try to merge it into TwinmeActivityImpl.
 */

@SuppressLint("Registered")
public class AbstractTwinmeActivity extends TwinmeActivityImpl implements AbstractTwinmeService.Observer {
    private static final String LOG_TAG = "AbstractTwinmeActivity";
    private static final boolean DEBUG = false;

    private static final int DESIGN_CALL_FLOATING_SIZE = 180;
    private static final int DESIGN_TOAST_IMAGE_SIZE = 104;

    protected ProgressBar mProgressBarView;
    @Nullable
    protected CallFloatingView mCallFloatingView;
    @Nullable
    protected InfoFloatingView mInfoFloatingView;

    private CallServiceReceiver mCallReceiver;

    private Observer mObserver;
    private int mBarTopInset = 0;
    private int mBarBottomInset = 0;

    public interface Observer {

        void onToolBarClick();
    }

    private class CallServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent == null || intent.getExtras() == null) {

                return;
            }

            String event = intent.getExtras().getString(CallService.CALL_SERVICE_EVENT);
            if (event != null) {
                if (DEBUG) {
                    Log.d(LOG_TAG, "Received event=" + event);
                }

                if (event.equals(CallService.MESSAGE_TERMINATE_CALL) || event.equals(CallService.MESSAGE_ERROR)) {
                    hideCallFloatingView();
                }
            }
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

        setupDesign();
    }

    public void setObserver(Observer observer) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setObserver: " + observer);
        }

        mObserver = observer;
    }

    public void updateFont() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateFont");
        }
    }

    public void updateColor() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateColor");
        }

        Design.updateValues(this, getTwinmeApplication());
    }

    public void updateInCall() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateInCall");
        }

    }

    public void setupDesign() {
        if (DEBUG) {
            Log.d(LOG_TAG, "setupDesign");
        }

    }

    public int getBarTopInset() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getBarTopInset");
        }

        return mBarTopInset;
    }
    
    public int getBarBottomInset() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getBarBottomInset");
        }

        return mBarBottomInset;
    }

    @Override
    protected void onPause() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPause");
        }

        super.onPause();

        if (mCallReceiver != null) {
            unregisterReceiver(mCallReceiver);
            mCallReceiver = null;
        }

        if (mCallFloatingView != null) {
            // Beware that inCallInfo can be changed between two consecutive calls!!!
            InCallInfo info = getTwinmeApplication().inCallInfo();
            if (info != null) {
                Point position = new Point((int) mCallFloatingView.getX(), (int) mCallFloatingView.getY());
                info.setPosition(position);
            }
        }

        if (mInfoFloatingView != null) {
            AppStateInfo appStateInfo = getTwinmeApplication().appInfo();
            if (appStateInfo != null) {
                Point position = new Point((int) mInfoFloatingView.getX(), (int) mInfoFloatingView.getY());
                appStateInfo.setPosition(position);
                appStateInfo.setInfoFloatingViewState(AppStateInfo.InfoFloatingViewState.DEFAULT);
            }
        }
    }

    @Override
    protected void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        super.onResume();

        // If an account migration service is running, redirect to the activity.
        // (check also SplashScreenActivity for another case).
        if (AccountMigrationService.isRunning()) {
            startActivity(AccountMigrationActivity.class);

            return;
        }

        // If an incoming call is in progress, redirect to the Audio/Video call activity so
        // that the user can accept/reject the call.
        final CallStatus callStatus = CallService.getCurrentMode();
        if (CallStatus.isIncoming(callStatus)) {
            startActivity(CallActivity.class);

            return;
        }

        // Listen to the CallService messages.
        IntentFilter filter = new IntentFilter(Intents.INTENT_CALL_SERVICE_MESSAGE);
        mCallReceiver = new CallServiceReceiver();
        ContextCompat.registerReceiver(this, mCallReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);

        updateFont();
        updateColor();
        updateInCall();

        final TwinmeApplication twinmeApplication = getTwinmeApplication();
        InCallInfo info = twinmeApplication.inCallInfo();
        if (info != null && canShowInfoFloatingView()) {
            showCallFloatingView(info);
        } else {
            hideCallFloatingView();
        }

        onConnectionStatusChange(twinmeApplication.getConnectionStatus());
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        updateFont();
        updateColor();
    }

    public void applyInsets(int rootLayout, int toolBarLayout, int bottomLayout, int backgroundColor, boolean isFullScreen) {
        if (DEBUG) {
            Log.d(LOG_TAG, "applyInsets rootLayout=" + rootLayout + " bottomLayout=" + bottomLayout + " backgroundColor=" + backgroundColor);
        }

        View rootView = findViewById(rootLayout);

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                            | WindowInsetsCompat.Type.displayCutout()
                            | WindowInsetsCompat.Type.ime()
            );

            mBarTopInset = bars.top;
            mBarBottomInset = bars.bottom;

            v.setBackgroundColor(backgroundColor);

            int topPadding = isFullScreen ? 0 : bars.top;
            int bottomPadding = bars.bottom;

            if (toolBarLayout != -1) {
                topPadding = 0;
                View toolBarView = findViewById(toolBarLayout);
                if (toolBarView != null) {
                    ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams)toolBarView.getLayoutParams();
                    marginLayoutParams.topMargin = bars.top;
                }
            }

            if (bottomLayout != -1) {
                bottomPadding = 0;
                View bottomView = findViewById(bottomLayout);
                if (bottomView != null) {
                    bottomView.setPadding(0, 0, 0, bars.bottom);
                }
            }

            v.setPadding(bars.left, topPadding, bars.right, bottomPadding);

            return WindowInsetsCompat.CONSUMED;
        });
    }

    /**
     * Run the haptic feedback according to user's settings.
     */
    public void hapticFeedback() {
        if (DEBUG) {
            Log.d(LOG_TAG, "hapticFeedback");
        }

        if (!mResumed) {
            // ignore haptic feedback if the activity is not visible.
            return;
        }

        final TwinmeApplication twinmeApplication = getTwinmeApplication();
        final int hapticFeedbackMode = twinmeApplication.hapticFeedbackMode();

        if (hapticFeedbackMode == TwinmeApplication.HapticFeedbackMode.SYSTEM.ordinal()) {
            getWindow().getDecorView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        } else if (hapticFeedbackMode == TwinmeApplication.HapticFeedbackMode.ON.ordinal()) {
            getWindow().getDecorView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        }
    }

    public void showAlertMessageView(int layout, String title, String message, boolean isFullScreen, @Nullable Runnable callBack) {
        if (DEBUG) {
            Log.d(LOG_TAG, "showAlertMessageView");
        }

        if (isNotRunning()) {
            return;
        }

        PercentRelativeLayout percentRelativeLayout = findViewById(layout);

        AlertMessageView alertMessageView = new AlertMessageView(this, null);
        PercentRelativeLayout.LayoutParams layoutParams = new PercentRelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        alertMessageView.setLayoutParams(layoutParams);
        alertMessageView.setTitle(title);
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
                percentRelativeLayout.removeView(alertMessageView);
                if (isFullScreen) {
                    setFullscreen();
                } else {
                    setStatusBarColor();
                }

                if (callBack != null) {
                    callBack.run();
                }
            }
        };
        alertMessageView.setObserver(observer);

        percentRelativeLayout.addView(alertMessageView);
        alertMessageView.show();

        if (isFullScreen) {
            Window window = getWindow();
            window.setNavigationBarColor(Design.POPUP_BACKGROUND_COLOR);
        } else {
            int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
            setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
        }
    }

    //
    // Implement AbstractTwinmeService.Observer methods
    //

    @Override
    public void showProgressIndicator() {
        if (DEBUG) {
            Log.d(LOG_TAG, "showProgressIndicator");
        }

        if (mProgressBarView != null && mProgressBarView.getVisibility() != View.VISIBLE) {
            mProgressBarView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void hideProgressIndicator() {
        if (DEBUG) {
            Log.d(LOG_TAG, "hideProgressIndicator");
        }

        if (mProgressBarView != null && mProgressBarView.getVisibility() == View.VISIBLE) {
            mProgressBarView.setVisibility(View.GONE);
        }
    }

    public void onExecutionError(BaseService.ErrorCode errorCode) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onExecutionError: errorCode=" + errorCode);
        }

        if (errorCode == BaseService.ErrorCode.NO_STORAGE_SPACE) {
            LayoutInflater inflater = getLayoutInflater();
            View toastLayout = inflater.inflate(R.layout.toast_error, findViewById(R.id.toast_layout));

            View roundedView = toastLayout.findViewById(R.id.toast_content);
            float radius = 14f * Resources.getSystem().getDisplayMetrics().density;
            float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
            ShapeDrawable popupViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
            popupViewBackground.getPaint().setColor(Design.POPUP_BACKGROUND_COLOR);
            roundedView.setBackground(popupViewBackground);

            ImageView imageView = toastLayout.findViewById(R.id.toast_image);
            ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
            layoutParams.height = layoutParams.width = Design.getHeight(DESIGN_TOAST_IMAGE_SIZE);

            TextView toastTitleView = toastLayout.findViewById(R.id.toast_title);
            Design.updateTextFont(toastTitleView, Design.FONT_BOLD28);
            toastTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

            TextView toastTextView = toastLayout.findViewById(R.id.toast_text);
            toastTextView.setText(getString(R.string.application_error_no_storage_space));
            Design.updateTextFont(toastTextView, Design.FONT_REGULAR32);
            toastTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

            Toast toast = new Toast(this);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(toastLayout);
            toast.setGravity(Gravity.FILL_HORIZONTAL | Gravity.TOP, 0, 0);
            toast.show();
        }
    }

    public void onSetCurrentSpace(@NonNull Space space) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSetCurrentSpace");
        }

    }

    protected void onBackClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBackClick");
        }

        finish();
    }

    protected void setBackground(@NonNull View view) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setBackground");
        }

    }

    protected void setToolBar(int toolbarId) {

        updateToolBar(toolbarId, Design.TOOLBAR_COLOR);
    }

    protected void setToolBar(int toolbarId, int backgroundColor) {

        updateToolBar(toolbarId, backgroundColor);
    }

    private void updateToolBar(int toolbarId, int backgroundColor) {

        Toolbar toolbar = findViewById(toolbarId);

        if (toolbar != null) {

            toolbar.setTitle("");
            toolbar.setOnClickListener(view -> {
                if (mObserver != null) {
                    mObserver.onToolBarClick();
                }
            });

            toolbar.setBackgroundColor(backgroundColor);

            setSupportActionBar(toolbar);

            TextView titleView = findViewById(R.id.toolbar_title);
            if (titleView != null) {
                Design.updateTextFont(titleView, Design.FONT_BOLD34);
                titleView.setTextColor(Color.WHITE);
            }

            TextView subTitleView = findViewById(R.id.toolbar_subtitle);
            if (subTitleView != null) {
                Design.updateTextFont(subTitleView, Design.FONT_REGULAR24);
                subTitleView.setTextColor(Color.WHITE);
                subTitleView.setVisibility(View.GONE);
            }
        }
    }

    protected void hideToolBar() {

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }


    public <I> boolean launch(@NonNull ActivityResultLauncher<I> launcher, I input) {
        if (DEBUG) {
            Log.d(LOG_TAG, "launch launcher=" + launcher + " input=" + input);
        }

        try {
            launcher.launch(input);
            return true;

        } catch (ActivityNotFoundException exception) {

            // The external activity operation does not exist.  This happens frequently on some Android devices.
            // Raise a toast but not immediately because it will not be displayed: we must schedule the toast in some post().
            Handler handler = new Handler();
            handler.post(() -> toast(getString(R.string.application_operation_failure)));
            return false;
        }
    }

    @Override
    public void startActivity(@NonNull Intent intent) {
        if (DEBUG) {
            Log.d(LOG_TAG, "startActivity intent=" + intent);
        }

        try {
            // startActivity is similar to calling startActivityForResult.
            super.startActivityForResult(intent, -1, null);

        } catch (ActivityNotFoundException exception) {

            // The external activity operation does not exist.  This happens frequently on some Android devices.
            // Raise a toast but not immediately because it will not be displayed: we must schedule the toast in some post().
            Handler handler = new Handler();
            handler.post(() -> toast(getString(R.string.application_operation_failure)));
        } catch (SecurityException exception) {

            // Execution of external activity is forbidden due to some security constraints.
            // This happens from time to time on some Android devices.
            // Raise a toast but not immediately because it will not be displayed: we must schedule the toast in some post().
            Handler handler = new Handler();
            handler.post(() -> toast(getString(R.string.application_not_authorized_operation)));
        }
    }

    @Override
    public void startActivityForResult(@NonNull Intent intent, int requestCode) {
        if (DEBUG) {
            Log.d(LOG_TAG, "startActivityForResult intent=" + intent + " requestCode=" + requestCode);
        }

        try {
            super.startActivityForResult(intent, requestCode);

        } catch (ActivityNotFoundException exception) {

            // The external activity operation does not exist.  This happens frequently on some Android devices.
            // Raise a toast but not immediately because it will not be displayed: we must schedule the toast in some post().
            Handler handler = new Handler();
            handler.post(() -> toast(getString(R.string.application_operation_failure)));
        } catch (SecurityException exception) {

            // Execution of external activity is forbidden due to some security constraints.
            // This happens from time to time on some Android devices.
            // Raise a toast but not immediately because it will not be displayed: we must schedule the toast in some post().
            Handler handler = new Handler();
            handler.post(() -> toast(getString(R.string.application_not_authorized_operation)));
        }
    }

    public void startActivity(@NonNull Class<?> clazz, @NonNull Intent intent) {
        if (DEBUG) {
            Log.d(LOG_TAG, "startActivity clazz=" + clazz + " intent=" + intent);
        }

        intent.setClass(this, clazz);
        startActivity(intent);
    }

    public void startActivity(@NonNull Class<?> clazz, @NonNull String name, @NonNull UUID param) {
        if (DEBUG) {
            Log.d(LOG_TAG, "startActivity clazz=" + clazz + " name=" + name + " param=" + param);
        }

        Intent intent = new Intent();
        intent.putExtra(name, param.toString());
        intent.setClass(this, clazz);
        startActivity(intent);
    }

    public void startActivity(@NonNull Class<?> clazz) {
        if (DEBUG) {
            Log.d(LOG_TAG, "startActivity clazz=" + clazz);
        }

        Intent intent = new Intent(this, clazz);
        startActivity(intent);
    }

    public void showContactActivity(@NonNull Originator subject) {
        if (DEBUG) {
            Log.d(LOG_TAG, "showContactActivity subject=" + subject);
        }

        switch (subject.getType()) {
            case GROUP:
                startActivity(ShowGroupActivity.class, Intents.INTENT_GROUP_ID, subject.getId());
                break;

            case CONTACT:
                if (((Contact) subject).isTwinroom()) {
                    startActivity(ShowRoomActivity.class, Intents.INTENT_CONTACT_ID, subject.getId());
                } else {
                    startActivity(ShowContactActivity.class, Intents.INTENT_CONTACT_ID, subject.getId());
                }
                break;

            case CALL_RECEIVER:
                startActivity(ShowExternalCallActivity.class, Intents.INTENT_CALL_RECEIVER_ID, subject.getId());
                break;

            default:
                break;
        }
    }

    private void showCallFloatingView(@NonNull InCallInfo info) {
        if (DEBUG) {
            Log.d(LOG_TAG, "showCallFloatingView info=" + info);
        }

        if (mCallFloatingView == null) {
            mCallFloatingView = new CallFloatingView(this);
            mCallFloatingView.setOnInCallClickListener(this::onCallFloatingViewClick);

            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.height = layoutParams.width = Design.getHeight(DESIGN_CALL_FLOATING_SIZE);
            addContentView(mCallFloatingView, layoutParams);

            ViewTreeObserver viewTreeObserver = mCallFloatingView.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (mCallFloatingView != null) {
                        ViewTreeObserver viewTreeObserver = mCallFloatingView.getViewTreeObserver();
                        viewTreeObserver.removeOnGlobalLayoutListener(this);
                        if (info.position() != null) {
                            mCallFloatingView.setX(info.position().x);
                            mCallFloatingView.setY(info.position().y);
                        } else {
                            mCallFloatingView.moveToTopRight();
                        }
                    }
                }
            });
        }

        mCallFloatingView.setVisibility(View.VISIBLE);
        mCallFloatingView.setInCallInfo(info);
    }

    private void hideCallFloatingView() {
        if (DEBUG) {
            Log.d(LOG_TAG, "hideCallFloatingView");
        }

        updateInCall();

        if (mCallFloatingView != null) {
            mCallFloatingView.setVisibility(View.GONE);
        }
    }

    private void onCallFloatingViewClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCallFloatingViewClick");
        }

        // Beware that inCallInfo can be changed between two consecutive calls!!!
        // It can be cleared after the user clicks on the item but before we handle the click!
        InCallInfo inCallInfo = getTwinmeApplication().inCallInfo();
        if (inCallInfo == null) {
            return;
        }

        hideCallFloatingView();

        Intent intent = new Intent();
        if (inCallInfo.getContactId() != null) {
            intent.putExtra(Intents.INTENT_CONTACT_ID, inCallInfo.getContactId().toString());
        }

        if (inCallInfo.getGroupId() != null) {
            intent.putExtra(Intents.INTENT_CONTACT_ID, inCallInfo.getGroupId().toString());
        }

        CallStatus callStatus = inCallInfo.getCallMode();

        intent.putExtra(Intents.INTENT_CALL_MODE, callStatus);
        intent.setClass(this, CallActivity.class);
        startActivity(intent);
    }
}
