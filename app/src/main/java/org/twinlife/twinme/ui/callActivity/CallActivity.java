/*
 *  Copyright (c) 2014-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Tristan Garaud (Tristan.Garaud@twinlife-systems.com)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Romain Kolb (romain.kolb@skyrock.com)
 */

package org.twinlife.twinme.ui.callActivity;

import static org.twinlife.twinme.ui.Intents.INTENT_AUDIO_METADATA;
import static org.twinlife.twinme.ui.Intents.INTENT_AUDIO_SELECTION;
import static org.twinlife.twinme.ui.Intents.INTENT_CONTACT_SELECTION;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.percentlayout.widget.PercentRelativeLayout;

import org.twinlife.device.android.twinme.BuildConfig;
import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.AndroidDeviceInfo;
import org.twinlife.twinlife.BaseService;
import org.twinlife.twinlife.BaseService.ErrorCode;
import org.twinlife.twinlife.ConnectionStatus;
import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinlife.PeerConnectionService;
import org.twinlife.twinlife.PeerConnectionService.ConnectionState;
import org.twinlife.twinlife.TerminateReason;
import org.twinlife.twinlife.TwincodeURI;
import org.twinlife.twinlife.util.Logger;
import org.twinlife.twinlife.util.Utils;
import org.twinlife.twinme.audio.AudioDevice;
import org.twinlife.twinme.audio.ProximitySensor;
import org.twinlife.twinme.calls.CallAudioManager;
import org.twinlife.twinme.calls.CallParticipant;
import org.twinlife.twinme.calls.CallParticipantEvent;
import org.twinlife.twinme.calls.CallParticipantObserver;
import org.twinlife.twinme.calls.CallService;
import org.twinlife.twinme.calls.CallState;
import org.twinlife.twinme.calls.CallStatus;
import org.twinlife.twinme.calls.ErrorType;
import org.twinlife.twinme.calls.keycheck.WordCheckChallenge;
import org.twinlife.twinme.calls.streaming.StreamPlayer;
import org.twinlife.twinme.calls.streaming.StreamingEvent;
import org.twinlife.twinme.calls.streaming.StreamingStatus;
import org.twinlife.twinme.models.CallReceiver;
import org.twinlife.twinme.models.CertificationLevel;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.GroupMember;
import org.twinlife.twinme.models.Originator;
import org.twinlife.twinme.models.Profile;
import org.twinlife.twinme.models.Zoomable;
import org.twinlife.twinme.models.schedule.DateTime;
import org.twinlife.twinme.models.schedule.DateTimeRange;
import org.twinlife.twinme.models.schedule.Schedule;
import org.twinlife.twinme.services.AbstractTwinmeService;
import org.twinlife.twinme.services.AudioCallService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.skin.DisplayMode;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.TwinmeApplication;
import org.twinlife.twinme.ui.contacts.InvitationCodeConfirmView;
import org.twinlife.twinme.ui.premiumServicesActivity.PremiumFeatureConfirmView;
import org.twinlife.twinme.ui.premiumServicesActivity.UIPremiumFeature;
import org.twinlife.twinme.utils.AbstractConfirmView;
import org.twinlife.twinme.utils.AlertMessageView;
import org.twinlife.twinme.utils.AppStateInfo;
import org.twinlife.twinme.utils.DefaultConfirmView;
import org.twinlife.twinme.utils.InfoFloatingView;
import org.twinlife.twinme.utils.OnboardingConfirmView;
import org.twinlife.twinme.utils.TwinmeImmersiveActivityImpl;
import org.twinlife.twinme.utils.coachmark.CoachMark;
import org.twinlife.twinme.utils.coachmark.CoachMarkView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Abstract Audio/Video Call Activity.
 * <p>
 * This is the root of either AudioCallActivity or VideoCallActivity to provide a common behavior.
 * <p>
 * The XXXCallActivity interacts with the CallService by sending intents and receiving broadcast intents sent by the service.
 * All the audio/video call with audio settings, audio manager, audio player, notifications, sound, video surfaces,
 * P2P connection are handled by the CallService.  The CallService is also responsible for handling connection timeouts.
 * <p>
 * The XXXCallActivity can be started and stopped several times during the same audio/video call.
 * When it is restarted, the CallService is asked to send us the current state and we get:
 * - the current call state,
 * - the optional start time of the call,
 * - the mute and speaker state,
 * - the camera mute and front/back state.
 * <p>
 * We also listen to other messages sent by the CallService when it changes the audio/speaker state (because someone asked it
 * to change through a notification action).
 * <p>
 * This defines the following abstract operations:
 * <p>
 * - initViews() to setup the presentation view,
 * - isCallReady() to check that all the permissions are granted before starting the call.
 */

public class CallActivity extends TwinmeImmersiveActivityImpl implements AudioCallService.Observer, InfoFloatingView.Observer, ViewTreeObserver.OnGlobalLayoutListener, CallParticipantObserver {
    private static final String LOG_TAG = "CallActivity";
    private static final boolean DEBUG = false;

    private static final String WAKELOCK_TAG = BuildConfig.APPLICATION_ID + ":AudioCall";
    private static final int PROXIMITY_DELAY = 750; // 0.75s (must not be too short nor too long)
    private static final long TURN_OFF_TO_PAUSE_DELAY = 1000; // Samsung proximity hack, delay between lock and call to onPause().f

    private static final int ANIMATE_MENU_DURATION = 100;

    private static final int REQUEST_ADD_PARTICIPANT = 1;
    private static final int REQUEST_ADD_STREAMING_AUDIO = 2;

    private static final long MESSAGE_TIMEOUT = 30 * 1000; // 30s
    private static final long CLOSE_ACTIVITY_TIMEOUT = 3 * 1000; // 3s
    private static final int COACH_MARK_DELAY = 500;
    private static final int CERTIFY_DELAY = 500;
    private static final int HIDE_MENU_VIDEO_CALL_DELAY = 3000;

    private static final int SCALE_ANIMATION_DURATION = 400;
    private static final int SCALE_ANIMATION_REPEAT_DELAY = 7000;

    private static final float DESIGN_MAX_NAME_WIDTH = 340f;
    private static final float DESIGN_BUTTON_HEIGHT = 100f;
    private static final float DESIGN_VIEW_BUTTON_HEIGHT = 148f;
    private static final float DESIGN_ACTION_BUTTON_HEIGHT = 136f;
    private static final float DESIGN_MENU_VIEW_HEIGHT = 342f;
    private static final float DESIGN_DEFAULT_MENU_MARGIN = 150f;
    private static final float DESIGN_DEFAULT_CONTAINER_HEIGHT = 136f;
    private static final float DESIGN_MENU_VIEW_BOTTOM_MARGIN = 0;
    private static final float DESIGN_PARTICIPANTS_VIEW_BOTTOM_MARGIN = 36;
    private static final float DESIGN_CONVERSATION_VIEW_BOTTOM_MARGIN = 20;
    private static final float DESIGN_INFO_FLOATING_SIZE = 60;
    private static final float DESIGN_SIDE_MARGIN = 34f;
    private static final float DESIGN_BACK_RIGHT_MARGIN = 74f;
    // private static final int NO_PARTICIPANT_RADIUS = 24;

    private static int DEFAULT_CONTAINER_HEIGHT;
    private static int MENU_VIEW_HEIGHT;
    private static int MENU_VIEW_WIDTH;
    private static int PARTICIPANTS_BOTTOM_MARGIN;
    private static int MENU_VIEW_BOTTOM_MARGIN;
    private static int CONVERSATION_VIEW_BOTTOM_MARGIN;
    private static int SIDE_MARGIN;
    private static int BACK_RIGHT_MARGIN;
    private static int DEFAULT_MENU_VIEW_BOTTOM_MARGIN;

    private static int BUTTON_HEIGHT;
    private static int VIEW_BUTTON_HEIGHT;
    private static int ACTION_BUTTON_HEIGHT;

    private class AcceptListener implements OnClickListener {

        private boolean disabled = false;

        @Override
        public void onClick(View view) {
            if (DEBUG) {
                Log.d(LOG_TAG, "AcceptOnListener.onClick: view=" + view);
            }

            if (disabled) {

                return;
            }
            disabled = true;

            onAcceptClick();
        }
    }

    private class SwipeGesture extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onFling(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {

            boolean isDownGesture = velocityY > 0;
            detectSwipe(isDownGesture);

            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }

    private class DeclineListener implements OnClickListener {

        private boolean disabled = false;

        @Override
        public void onClick(View view) {
            if (DEBUG) {
                Log.d(LOG_TAG, "DeclineListener.onClick: view=" + view);
            }

            if (disabled) {

                return;
            }
            disabled = true;

            onDeclineClick();
        }
    }

    private class CallServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent == null || intent.getExtras() == null) {

                return;
            }

            final String event = intent.getExtras().getString(CallService.CALL_SERVICE_EVENT);
            if (event == null) {
                return;
            }

            // Catch exception in case an external app succeeds in sending a message.
            try {
                if (DEBUG) {
                    Log.d(LOG_TAG, "Received event=" + event);
                }
                switch (event) {
                    case CallService.MESSAGE_ACCEPTED_CALL:
                        // Call was accepted by the peer.
                        CallActivity.this.onMessageAcceptedCall(intent);
                        break;

                    case CallService.MESSAGE_CONNECTION_STATE:
                        // Connection state was changed.
                        CallActivity.this.onMessageChangeConnectionState(intent);
                        break;

                    case CallService.MESSAGE_CREATE_INCOMING_CALL:
                        // Started an incoming call, ignore.
                        break;

                    case CallService.MESSAGE_CREATE_OUTGOING_CALL:
                    case CallService.MESSAGE_CALLS_MERGED:
                            // Started an outgoing call
                        CallActivity.this.updateViews();
                        break;

                    case CallService.MESSAGE_TERMINATE_CALL:
                        // Call is terminated.
                        CallActivity.this.onMessageTerminatePeerConnection(intent);
                        break;

                    case CallService.MESSAGE_STATE:
                        // Response for ACTION_CHECK_STATE.
                        CallActivity.this.onMessageState(intent);
                        break;

                    case CallService.MESSAGE_AUDIO_SINK_UPDATE:
                        // The speaker mode was changed.
                        CallActivity.this.onMessageAudioSinkUpdate(intent);
                        break;

                    case CallService.MESSAGE_AUDIO_MUTE_UPDATE:
                        // The audio mute was changed.
                        CallActivity.this.onMessageAudioMuteUpdate(intent);
                        break;

                    case CallService.MESSAGE_VIDEO_UPDATE:
                        // The video tracks are updated.
                        CallActivity.this.onMessageVideoUpdate(intent);
                        break;

                    case CallService.MESSAGE_CAMERA_SWITCH:
                        // The audio mute was changed.
                        CallActivity.this.onMessageCameraSwitch(intent);
                        break;

                    case CallService.MESSAGE_CAMERA_MUTE_UPDATE:
                        // The audio mute was changed.
                        CallActivity.this.onMessageCameraMuteUpdate(intent);
                        break;

                    case CallService.MESSAGE_TRANSFER_REQUEST:
                        // Incoming call transfer request from a Call Receiver
                        CallActivity.this.onTransferRequest(intent);
                        break;

                    case CallService.MESSAGE_CALL_ON_HOLD:
                    case CallService.MESSAGE_CALL_RESUMED:
                        //Call was put on hold, either by tapping the Pause button or because we switched calls (double call).
                        //Call was resumed, either by the peer or because we switched calls (double call)
                        CallActivity.this.onUpdateHoldState(intent);
                        break;

                    case CallService.MESSAGE_ERROR:
                        // Error occurred, CallService is stopping.
                        CallActivity.this.onMessageError(intent);
                        break;

                    default:
                        Log.w(LOG_TAG, "Call event " + event + " not handled");
                        break;
                }
            } catch (Exception exception) {
                if (Logger.WARN) {
                    Log.w(LOG_TAG, "Invalid message", exception);
                }
            }
        }
    }

    private Handler mCloseHandler = null;
    private boolean mResumed = false;
    private boolean mAskStateOnResume = false;
    private boolean mTerminating = false;
    private boolean mVideo = false;

    private ViewGroup mRootView;
    private View mBackgroundView;
    private ProgressBar mProgressBarView;
    private View mContentView;
    private View mHeaderView;
    private CallMenuView mCallMenuView;
    private CallStreamingAudioView mCallStreamingAudioView;
    private CallConversationView mCallConversationView;
    private CallCertifyView mCallCertifyView;
    private SelectAudioSourceView mSelectAudioSourceView;
    private CallHoldView mCallHoldView;
    private TextView mNameView;
    private View mCertifiedImageView;
    private Chronometer mChronometerView;
    private TextView mMessageView;
    private TextView mTransferView;
    private ParticipantImageView mNoParticipantView;
    private FrameLayout mParticipantsView;
    @Nullable
    private CallParticipantLocaleView mCallParticipantLocaleView;
    private AbstractCallParticipantView.CallParticipantViewMode mCallParticipantViewMode = AbstractCallParticipantView.CallParticipantViewMode.SMALL_LOCALE_VIDEO;
    private View mParticipantsOverlayView;

    private View mAnswerCallView;
    private View mAddParticipantView;
    private View mAddParticipantImageView;
    private View mBackClickableView;
    private TextView mTerminatedView;
    private CoachMarkView mCoachMarkView;
    private View mUnreadMessageView;
    private ImageView mUnreadMessageImageView;
    private View mCameraControlView;
    private boolean mIsAudioMute = false;
    private boolean mIsSpeakerOn = false;
    private boolean mHasCamera = false;
    @Nullable
    private Originator mOriginator;
    private UUID mOriginatorId;

    private UUID mGroupId;
    private String mOriginatorName;
    private Bitmap mOriginatorAvatar;
    private Bitmap mOriginatorIdentityAvatar;
    private Bitmap mButtonBackground;
    private boolean mTerminated = false;
    private boolean mConnected = false;
    private boolean mStarted = false;
    private boolean mIsCallStartedInVideo = false;
    private boolean mIsCallReceiver = false;
    private boolean mShowCertifyView = false;
    private boolean mHideMenuOnVideoCall = false;
    private boolean mShowRemoteCameraOnboardingView = false;

    @Nullable
    private CallStatus mMode;
    private long mStartTime;

    private boolean mRemoteAudioMute = false;
    private boolean mRemoteVideoMute = true;

    private AnimatorSet mAvatarAnimatorSet;

    @Nullable
    private AudioCallService mAudioCallService;
    private CallServiceReceiver mCallReceiver;
    private AudioDevice mCurrentAudioDevice = AudioDevice.NONE;

    private boolean mShowCallQuality = false;
    private boolean mAskCallQuality = false;

    private boolean mShowCallGroupAnimation = false;
    private boolean mShowBackgroundRestriction = false;

    private StreamPlayer mStreamPlayer;

    private final Handler mUiThreadHandler = new Handler(Looper.getMainLooper());

    private boolean mUIInitialized = false;
    private boolean mRootViewInitialized = false;
    private boolean mNoParticipantViewInitialized = false;
    private boolean mGetDescriptorDone = false;
    private boolean mMenuVisibility = true;
    private boolean mIsCameraMute = false;
    private boolean mIsCameraReady = false;
    private boolean mIsLandscape = false;
    private boolean mCameraGranted = false;
    private boolean mAudioGranted = false;
    private int mRootViewWidth = 0;
    private int mRootViewHeight = 0;
    private int mBarTopInset = 0;

    private final List<AbstractCallParticipantView> mCallParticipantViewList = new ArrayList<>();
    private boolean mIsSpeakerOnBeforeProximityUpdate = false;

    @Nullable
    private ProximitySensor mProximitySensor;
    @Nullable
    private PowerManager.WakeLock mScreenOffWakeLock;
    @Nullable
    private Handler mProximmityHandler;

    private GestureDetector mGestureDetector;

    private long mTurnOffTime;

    private final List<Integer> mCallParticipantColors = new ArrayList<>();
    private WordCheckChallenge mWordCheckChallenge;

    private final List<AnimatorSet> mAnimatorSets = new ArrayList<>();

    //
    // Override TwinlifeActivity methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        initCallParticipantColor();

        // Listen to the CallService messages.
        IntentFilter filter = new IntentFilter(Intents.INTENT_CALL_SERVICE_MESSAGE);
        mCallReceiver = new CallServiceReceiver();

        // Register and avoid exporting the call receiver.
        ContextCompat.registerReceiver(getBaseContext(), mCallReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);

        // We don't need a big image for the button background.
        mButtonBackground = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888);
        mButtonBackground.eraseColor(Design.ACTION_CALL_COLOR);

        BUTTON_HEIGHT = (int) (DESIGN_BUTTON_HEIGHT * Design.HEIGHT_RATIO);
        VIEW_BUTTON_HEIGHT = (int) (DESIGN_VIEW_BUTTON_HEIGHT * Design.HEIGHT_RATIO);
        ACTION_BUTTON_HEIGHT = (int) (DESIGN_ACTION_BUTTON_HEIGHT * Design.HEIGHT_RATIO);
        MENU_VIEW_HEIGHT = (int) (DESIGN_MENU_VIEW_HEIGHT * Design.HEIGHT_RATIO);
        MENU_VIEW_WIDTH = Design.DISPLAY_WIDTH;
        MENU_VIEW_BOTTOM_MARGIN = (int) (DESIGN_MENU_VIEW_BOTTOM_MARGIN * Design.HEIGHT_RATIO);
        PARTICIPANTS_BOTTOM_MARGIN = (int) (DESIGN_PARTICIPANTS_VIEW_BOTTOM_MARGIN * Design.HEIGHT_RATIO);
        DEFAULT_CONTAINER_HEIGHT = (int) (DESIGN_DEFAULT_CONTAINER_HEIGHT * Design.HEIGHT_RATIO);
        DEFAULT_MENU_VIEW_BOTTOM_MARGIN = (int) (DESIGN_DEFAULT_MENU_MARGIN * Design.HEIGHT_RATIO);
        SIDE_MARGIN = (int) (DESIGN_SIDE_MARGIN * Design.WIDTH_RATIO);
        BACK_RIGHT_MARGIN = (int) (DESIGN_BACK_RIGHT_MARGIN * Design.WIDTH_RATIO);
        CONVERSATION_VIEW_BOTTOM_MARGIN = (int) (DESIGN_CONVERSATION_VIEW_BOTTOM_MARGIN * Design.HEIGHT_RATIO);

        initViews();

        // The Audio/Video activity is now singleTask activity.  It becomes possible that the same activity is
        // used for two Audio/Video calls: when a call terminates and a new call is started before the activity finishes.
        // When this happens, onNewIntent() is called with the new intent, this may be the same contact or another one.
        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onNewIntent: activity=" + this + " intent=" + intent);
        }

        super.onNewIntent(intent);

        mTerminated = false;
        mStarted = false;
        mOriginatorId = Utils.UUIDFromString(intent.getStringExtra(Intents.INTENT_CONTACT_ID));

        mGroupId = Utils.UUIDFromString(intent.getStringExtra(Intents.INTENT_GROUP_ID));

        mShowCertifyView = intent.getBooleanExtra(Intents.INTENT_CERTIFY_BY_VIDEO_CALL, false);

        CallState callState = CallService.getState();
        CallStatus currentMode;
        if (callState != null) {
            currentMode = callState.getStatus();
        } else {
            currentMode = null;
        }

        mMode = (CallStatus) intent.getSerializableExtra(Intents.INTENT_CALL_MODE);

        boolean isIncoming = CallStatus.isIncoming(mMode);

        // The CallService is not running and we have an incoming call: ignore because the call is finished.
        if (currentMode == null && (mMode == null || isIncoming)) {

            finish();
            return;
        }

        // The incoming audio/video call is terminated, we are done.
        if (currentMode == CallStatus.TERMINATED && isIncoming) {

            finish();
            return;
        }

        if (mCloseHandler != null) {
            mCloseHandler.removeCallbacksAndMessages(null);
            mCloseHandler = null;
        }

        if (mMode == null) {
            mMode = CallStatus.FALLBACK;
        } else {
            mVideo = mMode.isVideo();
        }

        mIsCallStartedInVideo = mVideo;

        // Start in a mode where we display the peer's contact avatar and name.
        mRemoteVideoMute = true;
        mRemoteAudioMute = false;

        boolean isAccepted = Intents.INTENT_ACCEPTED.equals(intent.getAction());

        // If the call is accepted through the intent, send the accept action to the CallService immediately.
        // This is tricky because when onResume() is called we have to trigger the ACTION_CHECK_STATE
        // but the accept can be made only when the contact is known probably from onGetOriginator().
        // The startCall() will trigger the accept but it is called only when isCallReady() returns true.
        // Meanwhile, we must not override the mMode (see onMessageChangeConnectionState()).
        if (isAccepted && CallStatus.isIncoming(currentMode)) {
            mMode = CallStatus.ACCEPTED_INCOMING_CALL;
        }

        switch (mMode) {
            case OUTGOING_CALL:
            case OUTGOING_VIDEO_CALL:
            case OUTGOING_VIDEO_BELL:
            case INCOMING_CALL:
            case INCOMING_VIDEO_CALL:
            case INCOMING_VIDEO_BELL:
            case ACCEPTED_INCOMING_CALL:
                break;

            default:
            case IN_CALL:
            case IN_VIDEO_CALL:
                mMode = CallStatus.FALLBACK;
                break;
        }

        updateViews();

        if (mAudioCallService != null) {
            mAudioCallService.dispose();
            mAudioCallService = null;

            Intent queryIntent = new Intent(this, CallService.class);
            if (CallService.isDoubleCall() && isAccepted) {
                // Double call: the new call is accepted
                queryIntent.setAction(CallService.ACTION_ACCEPT_CALL);
            } else {
                queryIntent.setAction(CallService.ACTION_CHECK_STATE);
            }
            startService(queryIntent);

        } else if ((mOriginatorId == null && mGroupId == null) || (mMode == CallStatus.FALLBACK || mMode == CallStatus.ACCEPTED_INCOMING_CALL)) {
            mAskStateOnResume = true;

        } else {

            if (mOriginatorId == null) {
                //We're initiating a group call, so the originator is the group itself.
                mOriginatorId = mGroupId;
                mGroupId = null;
                mAudioCallService = new AudioCallService(this, getTwinmeContext(), this, mOriginatorId, null);
            } else {
                mAudioCallService = new AudioCallService(this, getTwinmeContext(), this, mOriginatorId, mGroupId);
            }

            mConnected = mAudioCallService.isConnected();
            mAskStateOnResume = false;
        }
    }

    //
    // Override Activity methods
    //

    @Override
    public void onGlobalLayout() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGlobalLayout");
        }

        if (!mIsCameraReady) {
            mIsCameraReady = true;
            final List<Permission> permissions = new ArrayList<>();

            permissions.add(Permission.CAMERA);
            permissions.add(Permission.RECORD_AUDIO);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                permissions.add(Permission.BLUETOOTH_CONNECT);
            }

            if (checkPermissions(permissions.toArray(new Permission[]{}))) {
                mCameraGranted = true;
                mAudioGranted = true;
                if (isCallReady()) {
                    // Continue the video/audio settings since we can access the camera.
                    startCall();
                }
            }
        }

        if (!mRootViewInitialized) {
            mRootViewInitialized = true;
            mRootViewWidth = mRootView.getWidth();
            mRootViewHeight = mRootView.getHeight();
            mIsLandscape = mRootViewWidth > mRootViewHeight;
            updateViews();
        }

        mRootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        mIsLandscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;

        resetParticipantsView();
        updateParticipantsView(CallService.getState());

        for (AbstractCallParticipantView cpv : mCallParticipantViewList) {
            if (cpv.isScreenSharing() && cpv.getCallParticipantViewAspect() == AbstractCallParticipantView.CallParticipantViewAspect.FULLSCREEN) {
                cpv.minZoom();
                mParticipantsView.bringToFront();
                mParticipantsOverlayView.bringToFront();
                mParticipantsView.bringChildToFront(cpv);
                break;
            }
        }

        bringAbstractConfirmViewToFront();
    }

    @Override
    protected void onPause() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPause");
        }

        super.onPause();

        CallService.setObserver(null);

        // Release the proximity sensor but keep the screen off wakelock because on Samsung devices this triggers a call to onResume().
        if (mProximitySensor != null) {
            mProximitySensor.stop();
            mProximitySensor = null;
        }

        if (mProximmityHandler != null) {
            mProximmityHandler.removeCallbacksAndMessages(null);
            mProximmityHandler = null;
        }

        final CallState call = CallService.getState();

        // Release the screen wake lock if the activity is paused, or the call is terminated.
        // On Samsung device, getting the PROXIMITY_SCREEN_OFF_WAKE_LOCK will trigger a call to onPause().
        // We must not release the lock because this will trigger onResume(): the proximity sensor will trigger another lock.
        // The TURN_OFF_TO_PAUSE_DELAY is here to avoid releasing the lock in that strange situation.
        // On other devices, if we don't release the lock, the screen will turn OFF or ON according to the proximity sensor
        // which is very annoying when you are outside of the Audio call.
        if (mScreenOffWakeLock != null && mScreenOffWakeLock.isHeld()
                && (mTurnOffTime + TURN_OFF_TO_PAUSE_DELAY < System.currentTimeMillis()
                || call == null || CallStatus.isTerminated(call.getStatus()))) {
            mScreenOffWakeLock.release();
            mScreenOffWakeLock = null;
        }

        if (call != null) {
            getTwinmeApplication().setInCallInfo(call.getInCallInfo());
        }

        mResumed = false;

        // If we have not started the outgoing call yet, don't ask the state if we resume because the
        // call service is not yet started and this will terminate the outgoing call.  The onPause()
        // occurs due to the permissions request dialog for the Audio and Camera.
        mAskStateOnResume = !(CallStatus.isOutgoing(mMode) && !mStarted);
    }

    @Override
    protected void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        // Setup the call participant observer before getting the new call service state.
        CallService.setObserver(this);

        super.onResume();

        // If the call is finished, terminate the activity (unless a message() dialog is pending).
        CallStatus currentMode = CallService.getCurrentMode();
        if (mTerminated && !isMessageDisplayed()) {
            finish();
            return;
        }
        if (!mTerminating && (currentMode == null && !CallStatus.isOutgoing(mMode))) {
            finish();
            return;
        }

        // After we resume the call state can be changed, ask the CallService.
        if (mAskStateOnResume) {
            try {
                Intent intent = new Intent(this, CallService.class);
                intent.setAction(CallService.ACTION_CHECK_STATE);
                startService(intent);

            } catch (IllegalStateException exception) {
                // We are in fact in background and can't start the CallService.
                finish();
                return;
            }
        }

        mResumed = true;

        if (mAudioCallService != null && !mAudioCallService.isConnected()) {
            showNetworkDisconnect(mVideo ? R.string.video_call_activity_cannot_call : R.string.audio_call_activity_cannot_call, this::finish);

        } else if (isCallReady()) {
            startCall();
        }

        if (CallService.isKeyCheckRunning()) {
            resumeCertifyView();
        }

        AppStateInfo appStateInfo = getTwinmeApplication().appInfo();
        if (appStateInfo != null) {
            appStateInfo.setInfoFloatingViewState(AppStateInfo.InfoFloatingViewState.DEFAULT);
            showInfoFloatingView(appStateInfo);
        } else {
            hideInfoFloatingView();
        }

        // Notify CallService that the app is in foreground, after a delay to try to prevent race conditions
        // where Android considers the app is still in the background and refuses the "microphone" foreground service type.
        mUiThreadHandler.postDelayed(() -> {
            Intent intent = new Intent(this, CallService.class);
            intent.setAction(CallService.ACTION_ACTIVITY_RESUMED);
            startService(intent);
        }, 1000);

        mProximitySensor = ProximitySensor.create(getApplicationContext(), this::onProximitySensorChangedState);
        mProximitySensor.start();
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        if (mProximitySensor != null) {
            mProximitySensor.stop();
            mProximitySensor = null;
        }

        if (mProximmityHandler != null) {
            mProximmityHandler.removeCallbacksAndMessages(null);
            mProximmityHandler = null;
        }

        if (mScreenOffWakeLock != null && mScreenOffWakeLock.isHeld()) {
            mScreenOffWakeLock.release();
            mScreenOffWakeLock = null;
        }

        if (mAudioCallService != null) {
            mAudioCallService.dispose();
            mAudioCallService = null;
        }

        if (mCloseHandler != null) {
            mCloseHandler.removeCallbacksAndMessages(null);
            mCloseHandler = null;
        }

        unregisterReceiver(mCallReceiver);

        for (AnimatorSet animatorSet : mAnimatorSets) {
            animatorSet.removeAllListeners();
        }

        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onActivityResult requestCode=" + requestCode + " resultCode=" + resultCode + " data=" + data);
        }

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ADD_PARTICIPANT) {
            String selection = data != null ? data.getStringExtra(INTENT_CONTACT_SELECTION) : null;
            if (selection != null) {

                String[] list = selection.split(",");
                for (String item : list) {
                    UUID contactId = Utils.UUIDFromString(item);

                    if (contactId != null) {
                        // Now we have the contact, trigger the outgoing call in the service.
                        Intent intent = new Intent(this, CallService.class);
                        intent.setAction(CallService.ACTION_OUTGOING_CALL);
                        intent.putExtra(CallService.PARAM_CONTACT_ID, contactId);
                        intent.putExtra(CallService.PARAM_CALL_MODE, CallStatus.OUTGOING_CALL);
                        intent.putExtra(CallService.PARAM_CALL_ADD_PARTICIPANT, true);
                        startService(intent);
                    }
                }
            }
        } else if (requestCode == REQUEST_ADD_STREAMING_AUDIO) {
            String selection = data != null ? data.getStringExtra(INTENT_AUDIO_SELECTION) : null;
            if (selection != null) {
                Intent intent = new Intent(this, CallService.class);
                intent.setAction(CallService.ACTION_START_STREAMING);
                intent.putExtra(CallService.PARAM_STREAMING_PATH, selection);
                Parcelable audioMetadata = data.getParcelableExtra(INTENT_AUDIO_METADATA);
                if (audioMetadata != null) {
                    intent.putExtra(CallService.PARAM_STREAMING_INFO, audioMetadata);
                }
                startService(intent);
            }
        }
    }

    @Override
    public void onRequestPermissions(@NonNull Permission[] grantedPermissions) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRequestPermissions: grantedPermissions=" + Arrays.toString(grantedPermissions));
        }

        for (Permission grantedPermission : grantedPermissions) {
            switch (grantedPermission) {
                case CAMERA:
                    mCameraGranted = true;
                    break;

                case RECORD_AUDIO:
                    mAudioGranted = true;
                    audioRecordGranted();
                    break;
            }
        }
        if (mAudioGranted && (mCameraGranted || (mMode != null && !mMode.isVideo()))) {
            if (isCallReady()) {
                startCall();
            }
        } else {
            terminateCall(TerminateReason.NOT_AUTHORIZED, false);

            String message;
            if (!mAudioGranted && (!mCameraGranted && (mMode != null && mMode.isVideo()))) {
                message = getString(R.string.application_authorization_microphone_camera);
            } else if (!mAudioGranted) {
                message = getString(R.string.application_authorization_microphone);
            } else {
                message = getString(R.string.application_authorization_camera);
            }

            messageSettings(message, 0L, new SettingsMessageCallback() {
                @Override
                public void onCancelClick() {
                    finish();
                }

                @Override
                public void onSettingsClick() {

                    openAppSettings();
                    finish();
                }

                @Override
                public void onTimeout() {

                }
            });
        }
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

    //
    // Implement AudioCallService.Observer methods
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

    @Override
    public void onConnectionStatusChange(@NonNull ConnectionStatus connectionStatus) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onConnectionStatusChange");
        }

        super.onConnectionStatusChange(connectionStatus);

        mConnected = connectionStatus == ConnectionStatus.CONNECTED;

        if (mResumed) {
            final TwinmeApplication twinmeApplication = getTwinmeApplication();
            if (mConnected) {
                if (twinmeApplication.showConnectedMessage()) {
                    twinmeApplication.setShowConnectedMessage(false);
                    AppStateInfo appInfo = twinmeApplication.appInfo();
                    if (appInfo == null) {
                        appInfo = new AppStateInfo(AppStateInfo.InfoFloatingViewState.DEFAULT, AppStateInfo.InfoFloatingViewType.CONNECTED, null);
                        twinmeApplication.setAppInfo(appInfo);
                    } else {
                        appInfo.setInfoFloatingViewType(AppStateInfo.InfoFloatingViewType.CONNECTED);
                    }
                    showInfoFloatingView(appInfo);
                }
            } else {

                twinmeApplication.setShowConnectedMessage(true);
                AppStateInfo appInfo = twinmeApplication.appInfo();

                if (appInfo == null) {
                    appInfo = new AppStateInfo(AppStateInfo.InfoFloatingViewState.DEFAULT, AppStateInfo.InfoFloatingViewType.OFFLINE, null);
                    twinmeApplication.setAppInfo(appInfo);
                } else {
                    appInfo.setInfoFloatingViewType(AppStateInfo.InfoFloatingViewType.OFFLINE);
                }

                showInfoFloatingView(appInfo);
            }
        }
    }

    @Override
    public void onTwinlifeOnline() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onTwinlifeOnline");
        }

        if (mResumed) {

            // Start the call if the activity is in foreground and we are ready to make it.
            if (isCallReady()) {
                startCall();
            }
        }
    }

    @Override
    public void onGetOriginator(@NonNull Originator originator, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetOriginator: originator=" + originator);
        }

        mOriginator = originator;
        mOriginatorAvatar = avatar;
        if (!mOriginator.hasPeer()) {
            terminateCall(TerminateReason.REVOKED, false);
            
            ViewGroup viewGroup = findViewById(R.id.call_activity_layout);

            AlertMessageView alertMessageView = new AlertMessageView(this, null);
            alertMessageView.setWindowHeight(getWindow().getDecorView().getHeight());
            alertMessageView.setForceDarkMode(true);
            alertMessageView.setTitle(getString(R.string.audio_call_activity_terminate));
            alertMessageView.setMessage(getString(R.string.application_contact_not_found));

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
                    setStatusBarColor();
                    finish();
                }
            };
            alertMessageView.setObserver(observer);

            viewGroup.addView(alertMessageView);
            alertMessageView.show();

            int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
            setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);

            return;
        }

        mContentView.setVisibility(View.VISIBLE);

        updateOriginator();

        if (isCallReady()) {
            startCall();
        }
    }

    @Override
    public void onUpdateOriginator(@NonNull Originator originator, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateOriginator: originator=" + originator);
        }

        mOriginator = originator;
        mOriginatorAvatar = avatar;
        updateOriginator();

        if (isCallReady() && !mStarted) {
            startCall();
        }
    }

    @Override
    public void onGetOriginatorNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetOriginatorNotFound");
        }

        finish();
    }

    private void onMessageAcceptedCall(@NonNull Intent intent) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onMessageAcceptedCall: intent=" + intent);
        }

        mMode = (CallStatus) intent.getSerializableExtra(CallService.CALL_SERVICE_STATE);
        mHasCamera = intent.getBooleanExtra(CallService.CALL_HAS_CAMERA, false);

        onMessageCameraMuteUpdate(intent);
        updateViews();
    }

    private void onMessageChangeConnectionState(@NonNull Intent intent) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onMessageChangeConnectionState: intent=" + intent);
        }

        CallState callState = CallService.getState();
        if (callState != null && callState.getTransferDirection() == CallState.TransferDirection.TO_BROWSER) {
            callIsTransfered();
            return;
        }

        ConnectionState state = (ConnectionState) intent.getSerializableExtra(CallService.CALL_SERVICE_CONNECTION_STATE);
        mHasCamera = intent.getBooleanExtra(CallService.CALL_HAS_CAMERA, false);
        CallStatus mode = (CallStatus) intent.getSerializableExtra(CallService.CALL_SERVICE_STATE);
        if (CallStatus.isActive(mode)) {

            mMode = mode;
            mStartTime = intent.getLongExtra(CallService.CALL_SERVICE_CONNECTION_START_TIME, 0);

            // Start the timer only when we are connected with the peer.
            if (state == PeerConnectionService.ConnectionState.CONNECTED) {

                if (mAvatarAnimatorSet != null) {
                    mAvatarAnimatorSet.end();
                    mAvatarAnimatorSet.cancel();
                    mAvatarAnimatorSet = null;
                }

                mChronometerView.setVisibility(View.VISIBLE);
                mChronometerView.setBase(mStartTime);
                addCallParticipantAnimation();
                mChronometerView.start();

                if (mShowCertifyView && mCallCertifyView == null) {
                    Handler certifyHandler = new Handler();
                    certifyHandler.postDelayed(this::startCertifyVideoCall, CERTIFY_DELAY);
                }

                if (mIsCallStartedInVideo && !mHideMenuOnVideoCall) {
                    mHideMenuOnVideoCall = true;
                    Handler hideMenuHandler = new Handler();
                    hideMenuHandler.postDelayed(() -> setMenuVisibility(false), HIDE_MENU_VIDEO_CALL_DELAY);
                }

                updateViews();
            }

            // We must stay in the ACCEPTED_INCOMING_CALL state until isCallReady() returns true and startCall()
            // is called otherwise we never accept the incoming call from the intent (see also onNewIntent()).
        } else if (mode != mMode && (!CallStatus.isAccepted(mMode) || mStarted)) {
            mMode = mode;
        }
    }

    private void onMessageTerminatePeerConnection(@NonNull Intent intent) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onMessageTerminatePeerConnection: intent=" + intent);
        }

        TerminateReason terminateReason = (TerminateReason) intent.getSerializableExtra(CallService.CALL_SERVICE_TERMINATE_REASON);

        boolean isHoldCall = intent.getBooleanExtra(CallService.CALL_IS_HOLD_CALL, false);

        if (isHoldCall) {
            mCallHoldView.setVisibility(View.GONE);
            animateMenu();
            return;
        }

        mChronometerView.stop();

        if (mAvatarAnimatorSet != null) {
            mAvatarAnimatorSet.cancel();
        }

        if (mShowCallQuality) {
            mMode = CallStatus.TERMINATED;
            updateViews();
            return;
        }

        // The peer closed the connection, display a message on the view and prepare to close it.
        if (terminateReason == TerminateReason.SUCCESS) {
            mMode = CallStatus.TERMINATED;
            updateViews();
            if (mOriginator != null && mOriginator.getIdentityCapabilities().hasDiscreet()) {
                mTerminatedView.setText(getString(R.string.audio_call_activity_terminate));
            } else {
                mTerminatedView.setText(Html.fromHtml(String.format(getString(R.string.audio_call_activity_terminate_success), mOriginatorName)));
            }

            mContentView.setOnClickListener(view -> onCloseClick());

            if (!mAskCallQuality && mStartTime > 0) {
                mAskCallQuality = true;
                long duration = (SystemClock.elapsedRealtime() - mStartTime) / 1000;
                mShowCallQuality = getTwinmeApplication().askCallQualityWithCallDuration(duration);

                if (mShowCallQuality) {
                    showCallQualityView();
                } else {
                    mCloseHandler = new Handler();
                    mCloseHandler.postDelayed(this::closeTimeout, CLOSE_ACTIVITY_TIMEOUT);
                }
            } else {
                mCloseHandler = new Handler();
                mCloseHandler.postDelayed(this::closeTimeout, CLOSE_ACTIVITY_TIMEOUT);
            }

            return;
        }

        // The incoming call was not accepted, we must not display the popup and terminate the activity now.
        if (CallStatus.isIncoming(mMode)) {
            finish();
            return;
        }

        // A message is already being displayed.
        if (mTerminating || terminateReason == null) {
            return;
        }

        if(terminateReason == TerminateReason.TRANSFER_DONE){
            callIsTransfered();
            mCloseHandler = new Handler();
            mCloseHandler.postDelayed(this::closeTimeout, CLOSE_ACTIVITY_TIMEOUT);
            return;
        }

        message(terminateReason(terminateReason), MESSAGE_TIMEOUT, new DefaultMessageCallback(R.string.application_ok) {

            @Override
            public void onClick() {

                finish();
            }

            @Override
            public void onTimeout() {

                // TBD
                finish();
            }
        });
    }

    private void closeTimeout() {
        if (DEBUG) {
            Log.d(LOG_TAG, "closeTimeout");
        }

        if (!mShowCallQuality) {
            finish();
        }
    }

    private void onMessageAudioMuteUpdate(@NonNull Intent intent) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onMessageAudioMuteUpdate: intent=" + intent);
        }

        boolean audioMute = intent.getBooleanExtra(CallService.CALL_MUTE_STATE, false);
        if (audioMute != mIsAudioMute) {
            mIsAudioMute = audioMute;
            mCallMenuView.setIsAudioMuted(mIsAudioMute);
            mCallMenuView.updateMenu();
        }
    }

    private void onMessageAudioSinkUpdate(@NonNull Intent intent) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onMessageAudioSinkUpdate: intent=" + intent);
        }

        CallAudioManager audioManager = CallService.getAudioManager();

        if (audioManager == null) {
            return;
        }

        // if selectedAudioDevice == NONE => default sink, i.e. EARPIECE for audio calls and SPEAKER_PHONE for video calls.
        AudioDevice selectedAudioDevice = audioManager.getSelectedAudioDevice();

        if (mCurrentAudioDevice == selectedAudioDevice) {
            return;
        }

        mCurrentAudioDevice = selectedAudioDevice;

        String updateAudioMessage = "";
        switch (mCurrentAudioDevice) {
            case BLUETOOTH:
                updateAudioMessage = getString(R.string.call_activity_connected_bluetooth);
                break;

            case SPEAKER_PHONE:
                updateAudioMessage = getString(R.string.call_activity_connected_speaker);
                break;

            default:
                break;
        }

        CallStatus mode = (CallStatus) intent.getSerializableExtra(CallService.CALL_SERVICE_STATE);

        boolean speakerOn = selectedAudioDevice == AudioDevice.SPEAKER_PHONE ||
                (selectedAudioDevice == AudioDevice.NONE && mode != null && mode.isVideo());
        if (speakerOn != mIsSpeakerOn) {
            mIsSpeakerOn = speakerOn;
            mCallMenuView.setIsInSpeakerOn(mIsSpeakerOn);
        }

        mCallMenuView.setAudioDevice(selectedAudioDevice, audioManager.isHeadsetAvailable());
        mCallMenuView.updateMenu();

        if (CallStatus.isActive(mode) && !updateAudioMessage.isEmpty()) {
            toast(updateAudioMessage);
        }
    }

    private void onMessageVideoUpdate(@NonNull Intent intent) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onMessageVideoUpdate: intent=" + intent);
        }

        updateViews();
    }

    private void onMessageCameraSwitch(@NonNull Intent intent) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onMessageCameraSwitch: intent=" + intent);
        }

        if (!mUIInitialized) {

            return;
        }

        if (mCallParticipantLocaleView != null) {
            mCallParticipantLocaleView.setVideoZoom(1.0f);
            mCallParticipantLocaleView.updateViews();
        }
    }

    private void onMessageCameraMuteUpdate(@NonNull Intent intent) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onMessageCameraMuteUpdate: intent=" + intent);
        }

        mHasCamera = intent.getBooleanExtra(CallService.CALL_HAS_CAMERA, false);
        boolean cameraMute = intent.getBooleanExtra(CallService.CALL_CAMERA_MUTE_STATE, false);

        if (!mUIInitialized) {
            mIsCameraMute = cameraMute;
            return;
        }

        CallStatus mode = (CallStatus) intent.getSerializableExtra(CallService.CALL_SERVICE_STATE);
        if (mode != null && mode != mMode && !(CallStatus.isAccepted(mMode) && CallStatus.isIncoming(mode))) {
            mMode = mode;
            mVideo = mMode.isVideo();

            updateViews();
        }

        if (cameraMute != mIsCameraMute) {
            mIsCameraMute = cameraMute;

            mCallMenuView.setIsCameraMuted(mIsCameraMute);
            mCallMenuView.updateMenu();
        }

        updateModeInCall();
    }

    private void onUpdateHoldState(@NonNull Intent intent) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateHoldState");
        }

        CallStatus mode = (CallStatus) intent.getSerializableExtra(CallService.CALL_SERVICE_STATE);
        if (mode != null && mode != mMode && !(CallStatus.isAccepted(mMode) && CallStatus.isIncoming(mode))) {
            mMode = mode;
            updateViews();
        }
    }

    private void onTransferRequest(@NonNull Intent intent) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onTransferRequest: intent=" + intent);
        }
        final UUID callReceiverId = (UUID) intent.getSerializableExtra(CallService.CALL_CONTACT_ID);
        final UUID peerConnectionId = (UUID) intent.getSerializableExtra(CallService.PARAM_PEER_CONNECTION_ID);

        //TODO: user confirmation

        Intent result = new Intent(this, CallService.class);
        result.setAction(CallService.ACTION_ACCEPT_TRANSFER);
        result.putExtra(CallService.CALL_CONTACT_ID, callReceiverId);
        result.putExtra(CallService.PARAM_PEER_CONNECTION_ID, peerConnectionId);

        //TODO: ACTION_DENY_TRANSFER

        startService(result);
    }

    private void onBackClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBackClick");
        }

        finish();
    }

    private void onMessageState(@NonNull Intent intent) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onMessageState: intent=" + intent);
        }

        if (mTerminating) {

            return;
        }

        CallStatus mode = (CallStatus) intent.getSerializableExtra(CallService.CALL_SERVICE_STATE);

        // If the CallService does not have a state, it means there is no call in progress because it was finished.
        // It happens if the current activity is called with an intent that refers to a past incoming/outgoing call.
        if (mode == null) {

            finish();
            return;
        }

        // If we are started without known the originator, we must get it from the CallService.
        // If the CallService does not know the originator, we can terminate: it means the call is finished.
        if (mOriginatorId == null) {
            mOriginatorId = (UUID) intent.getSerializableExtra(CallService.CALL_CONTACT_ID);
            if (mOriginatorId == null) {

                finish();
                return;
            }
            mGroupId = (UUID) intent.getSerializableExtra(CallService.CALL_GROUP_ID);
        }

        // Now we can start the AudioCallService and get the contact information!
        if (mAudioCallService == null) {
            mAudioCallService = new AudioCallService(this, getTwinmeContext(), this, mOriginatorId, mGroupId);
            mConnected = mAudioCallService.isConnected();
            if (mConnected && mMode == CallStatus.ACCEPTED_INCOMING_CALL && !mStarted) {
                acceptCall();
            }
        }

        mHasCamera = intent.getBooleanExtra(CallService.CALL_HAS_CAMERA, false);

        onMessageAudioMuteUpdate(intent);
        onMessageAudioSinkUpdate(intent);
        onMessageCameraMuteUpdate(intent);
        onMessageCameraSwitch(intent);

        mStartTime = intent.getLongExtra(CallService.CALL_SERVICE_CONNECTION_START_TIME, 0);
        if (mode != mMode && !(CallStatus.isAccepted(mMode) && CallStatus.isIncoming(mode))) {
            mMode = mode;
            mVideo = mMode.isVideo();

            updateViews();
        }

        ConnectionState state = (ConnectionState) intent.getSerializableExtra(CallService.CALL_SERVICE_CONNECTION_STATE);
        if (state != null && state != ConnectionState.INIT) {
            onMessageChangeConnectionState(intent);
        }

        TerminateReason terminateReason = (TerminateReason) intent.getSerializableExtra(CallService.CALL_SERVICE_TERMINATE_REASON);
        if (terminateReason != null) {
            onMessageTerminatePeerConnection(intent);
        }
    }

    private void onMessageError(@NonNull Intent intent) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onMessageError: intent=" + intent);
        }

        // We have to be careful: we can receive an error message but it does not concern our activity.
        ErrorType type = (ErrorType) intent.getSerializableExtra(CallService.CALL_ERROR_STATUS);
        if (type == null) {
            onError(ErrorCode.LIBRARY_ERROR, null);
            return;
        }

        switch (type) {
            case INTERNAL_ERROR:
                onError(ErrorCode.SERVICE_UNAVAILABLE, null);
                break;

            case CONTACT_NOT_FOUND:
                onError(ErrorCode.ITEM_NOT_FOUND, null);
                break;

            case CONNECTION_NOT_FOUND:
                message(terminateReason(TerminateReason.CANCEL), MESSAGE_TIMEOUT, new DefaultMessageCallback(R.string.application_ok) {

                    @Override
                    public void onClick() {

                        finish();
                    }

                    @Override
                    public void onTimeout() {

                        // TBD
                        finish();
                    }
                });
                break;

            case CAMERA_ERROR:
                terminateCall(TerminateReason.GENERAL_ERROR, false);
                error(getString(R.string.capture_activity_create_camera_error), this::finish);
                break;

            case CALL_IN_PROGRESS:
                UUID originatorId = (UUID) intent.getSerializableExtra(CallService.CALL_CONTACT_ID);
                if (originatorId != null && originatorId.equals(mOriginatorId)) {
                    // If the Audio service is already running, get the current state.
                    mMode = CallStatus.FALLBACK;

                    intent = new Intent(this, CallService.class);
                    intent.setAction(CallService.ACTION_CHECK_STATE);
                    startService(intent);
                }
                break;
        }
    }

    @Override
    public void onError(ErrorCode errorCode, String errorParameter) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onError: errorCode=" + errorCode + " errorParameter=" + errorParameter);
        }

        mChronometerView.stop();

        if (errorCode == ErrorCode.ITEM_NOT_FOUND) {
            terminateCall(TerminateReason.REVOKED, false);

            ViewGroup viewGroup = findViewById(R.id.call_activity_layout);

            AlertMessageView alertMessageView = new AlertMessageView(this, null);
            alertMessageView.setWindowHeight(getWindow().getDecorView().getHeight());
            alertMessageView.setForceDarkMode(true);
            alertMessageView.setTitle(getString(R.string.audio_call_activity_terminate));
            alertMessageView.setMessage(getString(R.string.application_contact_not_found));

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
                    setStatusBarColor();
                    finish();
                }
            };
            alertMessageView.setObserver(observer);
            viewGroup.addView(alertMessageView);
            alertMessageView.show();

            int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
            setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);

            return;
        }

        onError(errorCode, null, this::finish);
    }

    @Override
    public void onGetIdentityAvatar(@NonNull Originator originator, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetIdentityAvatar: originator=" + originator);
        }

        mOriginatorIdentityAvatar = avatar;
        if (mCallParticipantLocaleView != null) {
            mCallParticipantLocaleView.setAvatar(mOriginatorIdentityAvatar);
            mCallParticipantLocaleView.updateViews();
        }
    }

    @Override
    public void onUpdateIdentityAvatar(@NonNull Originator originator, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateIdentityAvatar: originator=" + originator);
        }

        mOriginatorIdentityAvatar = avatar;
        if (mCallParticipantLocaleView != null) {
            mCallParticipantLocaleView.setAvatar(mOriginatorIdentityAvatar);
            mCallParticipantLocaleView.updateViews();
        }
    }

    /**
     * A new participant is added to the call group.
     *
     * @param participant the participant.
     */
    @Override
    public void onAddParticipant(@NonNull CallParticipant participant) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onAddParticipant: participant=" + participant);
        }
    }

    /**
     * One or several participants are removed from the call.
     *
     * @param participants the list of participants being removed.
     */
    @Override
    public void onRemoveParticipants(@NonNull List<CallParticipant> participants) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRemoveParticipants: participants=" + participants);
        }

        updateParticipantsView(CallService.getState());

        for (AbstractCallParticipantView cpv : mCallParticipantViewList) {
            cpv.bringVideoToFront();
        }
    }

    /**
     * An event occurred for the participant and its state was changed.
     *
     * @param participant the participant.
     * @param event the event that occurred.
     */
    @Override
    public void onEventParticipant(@NonNull CallParticipant participant, @NonNull CallParticipantEvent event) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onEventParticipant: participant=" + participant + " event=" + event);
        }

        if (event == CallParticipantEvent.EVENT_CONNECTED) {
            updateViews();
            return;
        }

        if (event == CallParticipantEvent.EVENT_IDENTITY && mMode == CallStatus.TERMINATED) {
            finish();
            return;
        }

        if (isStreamEvent(event)) {
            updateAudioStreaming(participant, event);
            return;
        }

        if (isKeyCheckEvent(event)) {
            handleKeyCheckEvent(event);
            return;
        }

        final AbstractCallParticipantView callParticipantView = getParticipantView(participant);
        if (callParticipantView == null) {
            return;
        }

        if (callParticipantView.isRemoteParticipant()) {
            CallParticipantRemoteView callParticipantRemoteView = (CallParticipantRemoteView)callParticipantView;
            callParticipantRemoteView.setParticipant(participant);
            callParticipantRemoteView.updateViews();
        }

        if (isRemoteCameraControlEvent(event)) {
            updateCameraControl(participant, event);
            return;
        }

        if (event == CallParticipantEvent.EVENT_VIDEO_ON
                || event == CallParticipantEvent.EVENT_VIDEO_OFF
                || event == CallParticipantEvent.EVENT_HOLD
                || event == CallParticipantEvent.EVENT_RESUME) {

            CallState callState = CallService.getState();
            if (callState != null && callState.getStatus() != mMode) {
                mMode = callState.getStatus();
                updateViews();
            }

            if (mCallParticipantViewList.size() == 2) {
                updateParticipantsView(CallService.getState());
            }
        }

        if (event == CallParticipantEvent.EVENT_SCREEN_SHARING_ON) {
            CallParticipantRemoteView callParticipantRemoteView = (CallParticipantRemoteView)callParticipantView;
            onFullScreenTapCallParticipantView(callParticipantRemoteView);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        } else if (event == CallParticipantEvent.EVENT_SCREEN_SHARING_OFF) {
            CallParticipantRemoteView callParticipantRemoteView = (CallParticipantRemoteView)callParticipantView;
            onMinimizeTapCallParticipantView(callParticipantRemoteView);
        }
    }

    @Override
    public void onEventStreaming(@Nullable CallParticipant streamerParticipant, @NonNull StreamingEvent event) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onEventStreamer: streamerParticipant=" + streamerParticipant + " event=" + event);
        }

        switch (event) {
            case EVENT_START:
            case EVENT_PLAYING:
                mCallStreamingAudioView.resumeStreaming();
                break;

            case EVENT_PAUSED:
                mCallStreamingAudioView.pauseStreaming();
                break;

            case EVENT_COMPLETED:
            case EVENT_STOP:
                mStreamPlayer = null;
                updateParticipantsView(CallService.getState());
                mCallStreamingAudioView.stopStreaming();
                break;

            case EVENT_ERROR:
                mStreamPlayer = null;
                updateParticipantsView(CallService.getState());
                mCallStreamingAudioView.stopStreaming();
                toast(getString(R.string.streaming_audio_activity_error_message));
                break;

            case EVENT_UNSUPPORTED:
                mStreamPlayer = null;
                updateParticipantsView(CallService.getState());
                mCallStreamingAudioView.stopStreaming();
                toast(String.format(getString(R.string.streaming_audio_activity_unsupported_message), mOriginatorName));
                break;

            default:
                break;
        }

        if (mStreamPlayer != null) {
            mCallStreamingAudioView.setVisibility(View.VISIBLE);
            mCallStreamingAudioView.setSound(mStreamPlayer.getMediaInfo());
        } else {
            mCallStreamingAudioView.setVisibility(View.GONE);
        }

        animateMenu();
    }
    /**
     * The participant has sent us a descriptor.
     *
     * @param participant the participant.
     * @param descriptor the descriptor that was sent.
     */
    public void onPopDescriptor(@Nullable CallParticipant participant, @NonNull ConversationService.Descriptor descriptor) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPopDescriptor: participant=" + participant + " descriptor=" + descriptor);
        }

        mUnreadMessageView.setVisibility(View.VISIBLE);
        if (mCallConversationView.getVisibility() == View.GONE) {
            hapticFeedback();
            mUnreadMessageImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.call_new_message_icon, null));
        }

        String name = "";
        if (participant != null) {
            name = participant.getName();
        }

        mCallConversationView.addDescriptor(descriptor, false, true, name);
    }

    /**
     * The participant has updated its geolocation.
     *
     * @param participant the participant.
     * @param descriptor the geolocation descriptor being updated.
     */
    @Override
    public void onUpdateGeolocation(@Nullable CallParticipant participant, @NonNull ConversationService.GeolocationDescriptor descriptor) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateGeolocation: participant=" + participant + " descriptor=" + descriptor);
        }

        Log.e(LOG_TAG, "onUpdateGeolocation: participant=" + participant + " descriptor=" + descriptor);
    }

    /**
     * The participant has deleted its descriptor.
     *
     * @param participant the participant.
     * @param descriptorId the descriptor that was deleted.
     */
    public void onDeleteDescriptor(@Nullable CallParticipant participant, @NonNull ConversationService.DescriptorId descriptorId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteDescriptor: participant=" + participant + " descriptorId=" + descriptorId);
        }

        Log.e(LOG_TAG, "onDeleteDescriptor: participant=" + participant + " descriptorId=" + descriptorId);
    }

    private void startCall() {
        if (DEBUG) {
            Log.d(LOG_TAG, "startCall");
        }

        if (CallStatus.isOutgoing(mMode) && !mStarted) {

            // Now we have the contact, trigger the outgoing call in the service.
            Intent intent = new Intent(this, CallService.class);
            intent.setAction(CallService.ACTION_OUTGOING_CALL);
            intent.putExtra(CallService.PARAM_CALL_MODE, mMode);

            if (mOriginator != null && mOriginator.getType() == Originator.Type.GROUP) {
                intent.putExtra(CallService.PARAM_GROUP_ID, mOriginatorId);
                intent.putExtra(CallService.PARAM_CONTACT_ID, mOriginator.getTwincodeOutboundId());
            } else {
                intent.putExtra(CallService.PARAM_CONTACT_ID, mOriginatorId);
            }

            startService(intent);
            mStarted = true;

        } else if (mMode == CallStatus.ACCEPTED_INCOMING_CALL && !mStarted) {

            acceptCall();
        }
    }

    private void acceptCall() {
        if (DEBUG) {
            Log.d(LOG_TAG, "acceptCall");
        }

        Intent intent = new Intent(this, CallService.class);
        intent.setAction(CallService.ACTION_ACCEPT_CALL);
        startService(intent);
        mStarted = true;
    }

    @SuppressLint({"InlinedApi", "ClickableViewAccessibility"})
    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        final Window window = getWindow();
        final WindowManager.LayoutParams layoutParams;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setTurnScreenOn(true);
            setShowWhenLocked(true);
            KeyguardManager keyguardManager = (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
            keyguardManager.requestDismissKeyguard(this, null);
            layoutParams = window.getAttributes();
            layoutParams.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        } else {
            layoutParams = window.getAttributes();
            layoutParams.flags |= WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
        }
        window.setAttributes(layoutParams);

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.call_activity);

        setBackgroundColor(Color.BLACK);

        applyInsets(R.id.call_activity_view, Color.BLACK);

        mGestureDetector = new GestureDetector(this, new SwipeGesture());

        mRootView = findViewById(R.id.call_activity_view);

        mRootView.setOnTouchListener((v, event) -> {
            mGestureDetector.onTouchEvent(event);
            return true;
        });

        ViewTreeObserver viewTreeObserver = mRootView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(this);

        mProgressBarView = findViewById(R.id.call_activity_progress_bar);

        mHeaderView = findViewById(R.id.call_activity_header_view);
        ViewGroup.LayoutParams viewLayoutParams = mHeaderView.getLayoutParams();
        viewLayoutParams.height = BUTTON_HEIGHT;

        mBackClickableView = findViewById(R.id.call_activity_back_clickable_view);
        mBackClickableView.setOnClickListener(v -> onBackClick());

        ImageView backImageView = findViewById(R.id.call_activity_back_image_view);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) backImageView.getLayoutParams();
        marginLayoutParams.leftMargin = SIDE_MARGIN;
        marginLayoutParams.rightMargin = SIDE_MARGIN + BACK_RIGHT_MARGIN;
        marginLayoutParams.setMarginStart(SIDE_MARGIN);
        marginLayoutParams.setMarginEnd(SIDE_MARGIN + BACK_RIGHT_MARGIN);

        mBackgroundView = findViewById(R.id.call_activity_background_view);
        mBackgroundView.setBackgroundColor(Color.BLACK);

        mParticipantsView = findViewById(R.id.call_activity_participants_view);
        mParticipantsView.setBackgroundColor(Color.BLACK);
        marginLayoutParams = (ViewGroup.MarginLayoutParams) mParticipantsView.getLayoutParams();
        marginLayoutParams.topMargin = BUTTON_HEIGHT;
        marginLayoutParams.bottomMargin = PARTICIPANTS_BOTTOM_MARGIN + (MENU_VIEW_HEIGHT - DEFAULT_MENU_VIEW_BOTTOM_MARGIN);

        mParticipantsOverlayView = findViewById(R.id.call_activity_participants_overlay_view);
        mParticipantsOverlayView.setAlpha(0f);
        mParticipantsOverlayView.setBackgroundColor(Design.OVERLAY_VIEW_COLOR);
        mParticipantsOverlayView.setVisibility(View.GONE);

        mNoParticipantView = findViewById(R.id.call_activity_participants_avatar_view);

        mContentView = findViewById(R.id.call_activity_content_view);

        mNameView = findViewById(R.id.call_activity_name_view);
        Design.updateTextFont(mNameView, Design.FONT_REGULAR34);
        mNameView.setTextColor(Color.WHITE);

        mNameView.setMaxWidth((int)(DESIGN_MAX_NAME_WIDTH * Design.WIDTH_RATIO));

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mNameView.getLayoutParams();
        marginLayoutParams.leftMargin = -BACK_RIGHT_MARGIN;
        marginLayoutParams.setMarginStart(-BACK_RIGHT_MARGIN);

        mCertifiedImageView = findViewById(R.id.call_activity_certified_image_view);
        mCertifiedImageView.setVisibility(View.GONE);
        viewLayoutParams = mCertifiedImageView.getLayoutParams();
        viewLayoutParams.height = Design.CERTIFIED_HEIGHT;

        mMessageView = findViewById(R.id.call_activity_message_view);
        Design.updateTextFont(mMessageView, Design.FONT_REGULAR34);
        mMessageView.setTextColor(Color.WHITE);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mMessageView.getLayoutParams();
        marginLayoutParams.rightMargin = SIDE_MARGIN;
        marginLayoutParams.setMarginEnd(SIDE_MARGIN);

        mTransferView = findViewById(R.id.call_activity_transfer_view);
        Design.updateTextFont(mTransferView, Design.FONT_REGULAR34);
        mTransferView.setTextColor(Color.WHITE);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mTransferView.getLayoutParams();
        marginLayoutParams.leftMargin = SIDE_MARGIN;
        marginLayoutParams.rightMargin = SIDE_MARGIN;
        marginLayoutParams.setMarginStart(SIDE_MARGIN);
        marginLayoutParams.setMarginEnd(SIDE_MARGIN);

        mChronometerView = findViewById(R.id.call_activity_chronometer_view);
        Design.updateTextFont(mChronometerView, Design.FONT_REGULAR34);
        mChronometerView.setTextColor(Color.WHITE);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mChronometerView.getLayoutParams();
        marginLayoutParams.rightMargin = SIDE_MARGIN;
        marginLayoutParams.setMarginEnd(SIDE_MARGIN);

        mCallMenuView = findViewById(R.id.call_activity_menu_view);
        // Use same layout parameters in portrait/landscape modes
        @SuppressWarnings("deprecation")
        PercentRelativeLayout.LayoutParams menuViewLayoutParams = new PercentRelativeLayout.LayoutParams(MENU_VIEW_WIDTH,
                MENU_VIEW_HEIGHT);
        menuViewLayoutParams.bottomMargin = -DEFAULT_MENU_VIEW_BOTTOM_MARGIN;
        menuViewLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        menuViewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        mCallMenuView.setLayoutParams(menuViewLayoutParams);
        mCallMenuView.bringToFront();

        CallMenuView.CallMenuListener callMenuListener = new CallMenuView.CallMenuListener() {

            @Override
            public void onCallPause() {

                onCallPauseClick();
            }

            @Override
            public void onOpenConversation() {

                onCallConversationClick();
            }

            @Override
            public void onSelectStreamingAudio() {

                onAddStreamingAudio();
            }

            @Override
            public void onShareInvitation() {

                onShareInvitationClick();
            }

            @Override
            public void onMicroMute() {

                onMicroMuteClick();
            }

            @Override
            public void onCameraMute() {

                onCameraMuteClick();
            }

            @Override
            public void onControlCamera() {

                onControlCameraClick();
            }

            @Override
            public void onSpeakerOn() {

                onSpeakerOnClick();
            }

            @Override
            public void onSpeakerLongClick() {

                selectAudioSource();
            }

            @Override
            public void onHangup() {

                onHangupClick();
            }

            @Override
            public void onCertifyRelation() {

                onCertifyRelationClick();
            }

            @Override
            public void onMenuStateUpdated(CallMenuView.CallMenuViewState state) {

                updateMenuState(state);
            }
        };

        mCallMenuView.setCallMenuListener(callMenuListener);

        mCallStreamingAudioView = findViewById(R.id.call_activity_streaming_audio_view);

        CallStreamingAudioView.StreamingAudioListener streamingAudioListener = new CallStreamingAudioView.StreamingAudioListener() {

            @Override
            public void onStreamingPlayPause() {

                playPauseStreaming();
            }

            @Override
            public void onStreamingStop() {

                stopStreaming();
            }
        };

        mCallStreamingAudioView.setStreamingAudioListener(streamingAudioListener);

        menuViewLayoutParams = new PercentRelativeLayout.LayoutParams(MENU_VIEW_WIDTH,
                DEFAULT_CONTAINER_HEIGHT);
        menuViewLayoutParams.bottomMargin = PARTICIPANTS_BOTTOM_MARGIN + (MENU_VIEW_HEIGHT - DEFAULT_MENU_VIEW_BOTTOM_MARGIN);
        menuViewLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        menuViewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        mCallStreamingAudioView.setLayoutParams(menuViewLayoutParams);
        mCallStreamingAudioView.bringToFront();

        mCallConversationView = findViewById(R.id.call_activity_call_conversation_view);

        menuViewLayoutParams = new PercentRelativeLayout.LayoutParams(Design.DISPLAY_WIDTH - (SIDE_MARGIN * 2),
                ViewGroup.LayoutParams.MATCH_PARENT);
        menuViewLayoutParams.topMargin = BUTTON_HEIGHT;
        menuViewLayoutParams.bottomMargin = PARTICIPANTS_BOTTOM_MARGIN + (MENU_VIEW_HEIGHT - DEFAULT_MENU_VIEW_BOTTOM_MARGIN);
        menuViewLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        menuViewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        mCallConversationView.setLayoutParams(menuViewLayoutParams);
        mCallConversationView.bringToFront();

        CallConversationView.CallConversationListener callConversationListener = new CallConversationView.CallConversationListener() {
            @Override
            public void onCloseConversation() {

                ViewGroup.MarginLayoutParams marginLayout = (ViewGroup.MarginLayoutParams) mCallConversationView.getLayoutParams();
                marginLayout.bottomMargin = PARTICIPANTS_BOTTOM_MARGIN + (MENU_VIEW_HEIGHT - DEFAULT_MENU_VIEW_BOTTOM_MARGIN);
                mCallConversationView.setVisibility(View.GONE);
            }

            @Override
            public void onSendMessage(String message) {

                sendMessage(message);
            }
        };

        mCallConversationView.initCallConversationListener(callConversationListener, this);

        mCallHoldView = findViewById(R.id.call_activity_call_hold_view);

        CallHoldView.CallHoldListener callHoldListener = new CallHoldView.CallHoldListener() {
            @Override
            public void onAddToCall() {

                onMergeCallClick();
            }

            @Override
            public void onHangup() {

                onHangupHoldCallClick();
            }

            @Override
            public void onSwapCall() {

                onSwapCallClick();
            }
        };

        mCallHoldView.setCallHoldListener(callHoldListener);

        menuViewLayoutParams = new PercentRelativeLayout.LayoutParams(MENU_VIEW_WIDTH,
                DEFAULT_CONTAINER_HEIGHT);
        menuViewLayoutParams.bottomMargin = PARTICIPANTS_BOTTOM_MARGIN + (MENU_VIEW_HEIGHT - DEFAULT_MENU_VIEW_BOTTOM_MARGIN);
        menuViewLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        menuViewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        mCallHoldView.setLayoutParams(menuViewLayoutParams);
        mCallHoldView.bringToFront();

        mAddParticipantView = findViewById(R.id.call_activity_add_participant_view);
        mAddParticipantView.setOnClickListener(v -> onAddParticipantClick());
        mAddParticipantView.setVisibility(View.GONE);

        mAddParticipantImageView = findViewById(R.id.call_activity_add_participant_image_view);
        mAddParticipantImageView.setAlpha(1f);

        viewLayoutParams = mAddParticipantView.getLayoutParams();
        viewLayoutParams.height = BUTTON_HEIGHT;

        mUnreadMessageView = findViewById(R.id.call_activity_unread_message_view);
        mUnreadMessageView.setOnClickListener(v -> onCallConversationClick());
        mUnreadMessageView.setVisibility(View.GONE);

        viewLayoutParams = mUnreadMessageView.getLayoutParams();
        viewLayoutParams.height = BUTTON_HEIGHT;

        mUnreadMessageImageView = findViewById(R.id.call_activity_unread_message_image_view);

        mCameraControlView = findViewById(R.id.call_activity_control_camera_view);
        mCameraControlView.setOnClickListener(v -> onControlCameraClick());
        mCameraControlView.setVisibility(View.GONE);

        viewLayoutParams = mCameraControlView.getLayoutParams();
        viewLayoutParams.height = BUTTON_HEIGHT;

        ImageView cameraControlImageView = findViewById(R.id.call_activity_control_camera_image_view);
        cameraControlImageView.setColorFilter(Design.DELETE_COLOR_RED);

        mAnswerCallView = findViewById(R.id.call_activity_answer_call_view);
        viewLayoutParams = mAnswerCallView.getLayoutParams();
        viewLayoutParams.height = VIEW_BUTTON_HEIGHT;

        View acceptButtonView = findViewById(R.id.call_activity_accept_button_view);
        acceptButtonView.setOnClickListener(new AcceptListener());

        GradientDrawable acceptGradientDrawable = new GradientDrawable();
        acceptGradientDrawable.mutate();
        acceptGradientDrawable.setColor(Design.BUTTON_GREEN_COLOR);
        acceptGradientDrawable.setShape(GradientDrawable.RECTANGLE);
        acceptButtonView.setBackground(acceptGradientDrawable);
        float corner = ((float) BUTTON_HEIGHT / 2.0f) * Resources.getSystem().getDisplayMetrics().density;
        acceptGradientDrawable.setCornerRadius(corner);

        View declineButtonView = findViewById(R.id.call_activity_decline_button_view);
        declineButtonView.setOnClickListener(new DeclineListener());

        GradientDrawable declineGradientDrawable = new GradientDrawable();
        declineGradientDrawable.mutate();
        declineGradientDrawable.setColor(Design.BUTTON_RED_COLOR);
        declineGradientDrawable.setShape(GradientDrawable.RECTANGLE);
        declineButtonView.setBackground(declineGradientDrawable);
        declineGradientDrawable.setCornerRadius(corner);

        mTerminatedView = findViewById(R.id.call_activity_terminated_view);
        Design.updateTextFont(mTerminatedView, Design.FONT_REGULAR34);
        mTerminatedView.setTextColor(Color.WHITE);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mTerminatedView.getLayoutParams();
        marginLayoutParams.rightMargin = SIDE_MARGIN;
        marginLayoutParams.setMarginEnd(SIDE_MARGIN);

        mCoachMarkView = findViewById(R.id.call_activity_coach_mark_view);
        CoachMarkView.OnCoachMarkViewListener onCoachMarkViewListener = new CoachMarkView.OnCoachMarkViewListener() {
            @Override
            public void onCloseCoachMark() {

                mCoachMarkView.setVisibility(View.GONE);
            }

            @Override
            public void onTapCoachMarkFeature() {

                mCoachMarkView.setVisibility(View.GONE);
                getTwinmeApplication().hideCoachMark(CoachMark.CoachMarkTag.ADD_PARTICIPANT_TO_CALL);
                onAddParticipantClick();
            }

            @Override
            public void onLongPressCoachMarkFeature() {

            }
        };

        mCoachMarkView.setOnCoachMarkViewListener(onCoachMarkViewListener);

        ViewCompat.setOnApplyWindowInsetsListener(getWindow().getDecorView(), (v, insets) -> {
            boolean isKeyboardVisible = insets.isVisible(WindowInsetsCompat.Type.ime());
            int keyboardHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;

            ViewGroup.MarginLayoutParams marginLayout = (ViewGroup.MarginLayoutParams) mCallConversationView.getLayoutParams();

            if (isKeyboardVisible && mCallConversationView.getVisibility() == View.VISIBLE) {
                marginLayout.bottomMargin = keyboardHeight + CONVERSATION_VIEW_BOTTOM_MARGIN;
            } else {
                marginLayout.bottomMargin = PARTICIPANTS_BOTTOM_MARGIN + (MENU_VIEW_HEIGHT - DEFAULT_MENU_VIEW_BOTTOM_MARGIN);
            }

            return insets;
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (DEBUG) {
                    Log.d(LOG_TAG, "handleOnBackPressed");
                }

                backPressed();
            }
        });

        mUIInitialized = true;
    }

    private void animateMenu() {
        if (DEBUG) {
            Log.d(LOG_TAG, "animateMenu");
        }

        if (!mUIInitialized) {
            return;
        }   

        float alpha = 1f;
        float alphaHeader = 1f;
        mCallMenuView.setCallMenuViewState(CallMenuView.CallMenuViewState.DEFAULT);
        if (mMenuVisibility) {
            if (mCallCertifyView != null) {
                alphaHeader = 0.5f;
            }

            mParticipantsView.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mParticipantsView.getLayoutParams();
            marginLayoutParams.topMargin = BUTTON_HEIGHT;

            int playerHeight = 0;
            if (mStreamPlayer != null) {
                playerHeight = DEFAULT_CONTAINER_HEIGHT + PARTICIPANTS_BOTTOM_MARGIN;
            }

            int callHoldHeight = 0;
            if (CallService.isDoubleCall()) {

                callHoldHeight =  DEFAULT_CONTAINER_HEIGHT + PARTICIPANTS_BOTTOM_MARGIN;
            }

            marginLayoutParams.bottomMargin = PARTICIPANTS_BOTTOM_MARGIN + (MENU_VIEW_HEIGHT - DEFAULT_MENU_VIEW_BOTTOM_MARGIN + playerHeight + callHoldHeight);

            ((ViewGroup) mHeaderView).getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
            marginLayoutParams = (ViewGroup.MarginLayoutParams) mHeaderView.getLayoutParams();
            marginLayoutParams.topMargin = 0;

            mCallStreamingAudioView.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
            marginLayoutParams = (ViewGroup.MarginLayoutParams) mCallStreamingAudioView.getLayoutParams();
            marginLayoutParams.bottomMargin = (MENU_VIEW_HEIGHT - DEFAULT_MENU_VIEW_BOTTOM_MARGIN) + PARTICIPANTS_BOTTOM_MARGIN + callHoldHeight;

            mCallHoldView.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
            marginLayoutParams = (ViewGroup.MarginLayoutParams) mCallHoldView.getLayoutParams();
            marginLayoutParams.bottomMargin = (MENU_VIEW_HEIGHT - DEFAULT_MENU_VIEW_BOTTOM_MARGIN) + PARTICIPANTS_BOTTOM_MARGIN;

            mCallMenuView.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
            marginLayoutParams = (ViewGroup.MarginLayoutParams) mCallMenuView.getLayoutParams();
            marginLayoutParams.bottomMargin = -DEFAULT_MENU_VIEW_BOTTOM_MARGIN;

            updateCallParticipantView();
        } else {
            alpha = 0f;
            alphaHeader = 0f;

            mParticipantsView.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mParticipantsView.getLayoutParams();
            marginLayoutParams.topMargin = 0;

            int playerHeight = 0;
            if (mStreamPlayer != null) {
                playerHeight = DEFAULT_CONTAINER_HEIGHT + PARTICIPANTS_BOTTOM_MARGIN;
            }

            int callHoldHeight = 0;
            if (CallService.isDoubleCall()) {
                callHoldHeight =  DEFAULT_CONTAINER_HEIGHT + PARTICIPANTS_BOTTOM_MARGIN;
            }

            marginLayoutParams.bottomMargin = playerHeight + callHoldHeight;

            ((ViewGroup) mHeaderView).getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
            marginLayoutParams = (ViewGroup.MarginLayoutParams) mHeaderView.getLayoutParams();
            marginLayoutParams.topMargin = -BUTTON_HEIGHT;

            mCallStreamingAudioView.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
            marginLayoutParams = (ViewGroup.MarginLayoutParams) mCallStreamingAudioView.getLayoutParams();
            marginLayoutParams.bottomMargin = PARTICIPANTS_BOTTOM_MARGIN + callHoldHeight;

            mCallHoldView.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
            marginLayoutParams = (ViewGroup.MarginLayoutParams) mCallHoldView.getLayoutParams();
            marginLayoutParams.bottomMargin = PARTICIPANTS_BOTTOM_MARGIN;

            mCallMenuView.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
            marginLayoutParams = (ViewGroup.MarginLayoutParams) mCallMenuView.getLayoutParams();
            marginLayoutParams.bottomMargin = - (MENU_VIEW_HEIGHT + MENU_VIEW_BOTTOM_MARGIN);

            updateCallParticipantView();
        }

        mCallMenuView.animate()
                .alpha(alpha)
                .setDuration(ANIMATE_MENU_DURATION);

        mHeaderView.animate()
                .alpha(alphaHeader)
                .setDuration(ANIMATE_MENU_DURATION);
    }

    /**
     * Must return true if the audio/video permissions are granted to make the call.
     *
     * @return true if the call can be started and false if still waiting for permissions.
     */
    private boolean isCallReady() {
        if (DEBUG) {
            Log.d(LOG_TAG, "isCallReady");
        }

        // To start the outgoing call, we must:
        // - be connected and in foreground,
        // - have a valid contact,
        // - have the audio permission,
        // - have the video permission if this is a video call (if not, we allow an audio call).
        return mConnected && mResumed && mOriginator != null
                && (mOriginator.getType() != Originator.Type.CONTACT || mOriginator.hasPrivatePeer())
                && mAudioGranted && (mCameraGranted || (mMode != null && !mMode.isVideo()));
    }

    public void markDescriptorRead(ConversationService.DescriptorId descriptorId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "markDescriptorRead: " + descriptorId);
        }

        CallState callState = CallService.getState();
        if (callState != null) {
            for (ConversationService.Descriptor descriptor : callState.getDescriptors()) {
                if (descriptor.getDescriptorId().equals(descriptorId)) {
                    callState.markDescriptorRead(descriptor);
                    break;
                }
            }
        }
    }

    private void callIsTransfered() {
        if (DEBUG) {
            Log.d(LOG_TAG, "callIsTransfered");
        }

        mParticipantsView.setVisibility(View.GONE);
        mTransferView.setVisibility(View.VISIBLE);
    }

    private void startCertifyVideoCall() {
        if (DEBUG) {
            Log.d(LOG_TAG, "startCertifyVideoCall");
        }

        mShowCertifyView = false;
        startCertifyView(true, true);
        mCallMenuView.setIsCertifyRunning(true);
        mCallMenuView.updateMenu();
    }

    private void resumeCertifyView() {
        if (DEBUG) {
            Log.d(LOG_TAG, "resumeCertifyView");
        }

        startCertifyView(false, false);
        mWordCheckChallenge = CallService.getKeyCheckCurrentWord();
        mCallCertifyView.setCurrentWord(mWordCheckChallenge);
    }

    private void updateViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateViews");
        }

        if (!mUIInitialized || mMode == null) {

            return;
        }


        switch (mMode) {
            case ACCEPTED_INCOMING_CALL:
            case ACCEPTED_OUTGOING_CALL:
                mCallMenuView.setVisibility(View.VISIBLE);
                mMessageView.setText(getString(R.string.audio_call_activity_connecting));
                mAnswerCallView.setVisibility(View.GONE);
                break;

            case INCOMING_CALL:
            case INCOMING_VIDEO_CALL:
            case INCOMING_VIDEO_BELL:
                if (mMode == CallStatus.INCOMING_CALL) {
                    mMessageView.setText(getString(R.string.audio_call_activity_calling));
                } else if (mMode == CallStatus.INCOMING_VIDEO_CALL) {
                    mMessageView.setText(getString(R.string.video_call_activity_calling));
                }
                mCallMenuView.setVisibility(View.GONE);
                mMessageView.setVisibility(View.VISIBLE);
                mAnswerCallView.setVisibility(View.VISIBLE);
                break;

            case OUTGOING_CALL:
            case OUTGOING_VIDEO_CALL:
            case OUTGOING_VIDEO_BELL:
            case IN_VIDEO_BELL:
                mCallMenuView.setVisibility(View.VISIBLE);
                mMessageView.setVisibility(View.VISIBLE);
                break;

            case IN_CALL:
            case IN_VIDEO_CALL:
            case ON_HOLD:
            case PEER_ON_HOLD:
                CallState callState = CallService.getState();
                if (callState != null) {
                    CallParticipant participant = callState.getMainParticipant();
                    if (participant != null) {
                        mRemoteAudioMute = participant.isAudioMute();
                        mRemoteVideoMute = participant.isCameraMute();
                        StreamingStatus status = participant.getStreamingStatus();
                        mCallMenuView.setIsStreamingAudioSupported(StreamingStatus.isSupported(status));
                    }
                }

                mCallMenuView.setVisibility(View.VISIBLE);

                CallAudioManager audioManager = CallService.getAudioManager();
                if (audioManager != null) {
                    AudioDevice selectedAudioDevice = audioManager.getSelectedAudioDevice();
                    mCallMenuView.setAudioDevice(selectedAudioDevice, audioManager.isHeadsetAvailable());
                }

                mCallMenuView.updateMenu();
                mAddParticipantView.setVisibility(View.VISIBLE);
                showCoachMark();

                mBackClickableView.setVisibility(View.VISIBLE);
                mMessageView.setVisibility(View.GONE);
                mAnswerCallView.setVisibility(View.GONE);

                if (mChronometerView.getVisibility() != View.VISIBLE) {
                    if (mAvatarAnimatorSet != null) {
                        mAvatarAnimatorSet.end();
                        mAvatarAnimatorSet.cancel();
                        mAvatarAnimatorSet = null;
                    }
                }

                mChronometerView.stop();
                mChronometerView.setVisibility(View.VISIBLE);
                mChronometerView.setBase(mStartTime);
                addCallParticipantAnimation();
                mChronometerView.start();

                if (CallService.isDoubleCall()) {
                    CallState holdCallState = CallService.getHoldState();
                    if (holdCallState != null && holdCallState.getOriginator() != null) {
                        mCallHoldView.setVisibility(View.VISIBLE);
                        mCallHoldView.setCallInfo(holdCallState.getOriginator().getName(), holdCallState.getAvatar());
                    }
                } else {
                    mCallHoldView.setVisibility(View.GONE);
                }

                checkBatteryOptimizations();

                break;

            case TERMINATED:
                mCallMenuView.setVisibility(View.GONE);
                mCallStreamingAudioView.setVisibility(View.GONE);
                mNameView.setVisibility(View.GONE);
                mCertifiedImageView.setVisibility(View.GONE);
                mChronometerView.setVisibility(View.GONE);
                mAddParticipantView.setVisibility(View.GONE);
                mUnreadMessageView.setVisibility(View.GONE);
                mCameraControlView.setVisibility(View.GONE);
                mTerminatedView.setVisibility(View.VISIBLE);

                if (mCallCertifyView != null) {
                    mCallCertifyView.setVisibility(View.GONE);
                    mHeaderView.setAlpha(1.f);
                }
                break;

            default:
                break;
        }

        if (!mCallParticipantViewList.isEmpty()) {
            mNoParticipantView.setVisibility(View.GONE);
        } else if (mOriginatorAvatar != null) {
            initNoParticipantImageView();
            mNoParticipantView.setVisibility(View.VISIBLE);
        }

        mCallMenuView.setIsInCall(CallStatus.isActive(mMode));
        if (!mVideo && !CallStatus.isActive(mMode)) {
            mIsCameraMute = true;
            mCallMenuView.setIsCameraMuted(true);
        } else {
            mCallMenuView.setIsCameraMuted(mIsCameraMute);
        }

        updateCallParticipantView();

        boolean hideCertify = true;
        if (mOriginator != null && mOriginator.getType() == Originator.Type.CONTACT && mCallParticipantViewList.size() == 2) {
            Contact contact = (Contact) mOriginator;
            CertificationLevel level = contact.getCertificationLevel();
            if (level == CertificationLevel.LEVEL_1 || level == CertificationLevel.LEVEL_2 || level == CertificationLevel.LEVEL_3) {
                hideCertify = false;
            }
        }

        boolean isVideoAllowed = mCameraGranted && (mIsCallStartedInVideo || (mOriginator != null && mOriginator.getCapabilities().hasVideo() && mOriginator.getIdentityCapabilities().hasVideo()));
        mCallMenuView.setIsVideoAllowed(isVideoAllowed);
        mCallMenuView.setIsCameraControlAllowed(isVideoAllowed && mCallParticipantViewList.size() == 2 && isRemoteCameraControlSupported() && mOriginator != null && mOriginator.getCapabilities().getZoomable() != Zoomable.NEVER);
        mCallMenuView.setIsRemoteCameraControl(isRemoteCameraControl());
        mCallMenuView.setIsInPause(mMode == CallStatus.ON_HOLD);
        mCallMenuView.setIsCertifyRunning(CallService.isKeyCheckRunning());
        mCallMenuView.setHideCertifyRelation(hideCertify);
        mCallMenuView.updateMenu();

        if (mMode == null) {

            return;
        }

        switch (mMode) {
            case INCOMING_VIDEO_CALL:
            case IN_VIDEO_BELL:
            case IN_CALL:
            case IN_VIDEO_CALL:
                if (CallStatus.isActive(mMode) && mCallCertifyView == null && !mHideMenuOnVideoCall) {
                    setMenuVisibility(true);
                }

                // This is an audio call, the camera is not active.
                if (mMode == CallStatus.IN_CALL) {
                    mIsCameraMute = true;
                    mCallMenuView.setIsCameraMuted(true);
                }

                mCallMenuView.setIsConversationAllowed(isMessageSupported());
                mCallMenuView.updateMenu();

                updateModeInCall();
                break;

            case TERMINATED:
                setMenuVisibility(true);
                mBackgroundView.setVisibility(View.VISIBLE);
                mContentView.setVisibility(View.VISIBLE);
                mParticipantsView.animate().alpha(0f).setDuration(CLOSE_ACTIVITY_TIMEOUT);
                break;
            default:
                break;
        }
    }

    private void updateModeInCall() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateModeInCall");
        }

        if (!mUIInitialized || !CallStatus.isActive(mMode)) {

            return;
        }

        if (mRemoteVideoMute) {
            mCallMenuView.setVisibility(View.VISIBLE);
            mBackgroundView.setVisibility(View.VISIBLE);
            mContentView.setVisibility(View.VISIBLE);
            setMenuVisibility(true);
        } else {
            mBackgroundView.setVisibility(View.GONE);
            mContentView.setVisibility(View.GONE);
        }

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

        if (isRemoteCameraControl()) {
            mCameraControlView.setVisibility(View.VISIBLE);
        } else {
            mCameraControlView.setVisibility(View.GONE);
        }

        if (!mGetDescriptorDone) {
            mGetDescriptorDone = true;

            CallState callState = CallService.getState();
            if (callState != null) {

                Map<UUID, String> participantsName = new HashMap<>();
                final List<CallParticipant> participants = callState.getParticipants();
                for (CallParticipant callParticipant : participants) {
                    if (callParticipant.getSenderId() != null) {
                        participantsName.put(callParticipant.getSenderId(), callParticipant.getName());
                    }
                }

                boolean unreadMessage = false;

                for (ConversationService.Descriptor descriptor : callState.getDescriptors()) {
                    boolean isLocal = !callState.isPeerDescriptor(descriptor);

                    String name = "";

                    if (isLocal && mOriginator != null) {
                        name = mOriginator.getIdentityName();
                    } else if (participantsName.get(descriptor.getDescriptorId().twincodeOutboundId) != null) {
                        name = participantsName.get(descriptor.getDescriptorId().twincodeOutboundId);
                    }

                    if (!isLocal && descriptor.getReadTimestamp() == 0) {
                        unreadMessage = true;
                    }

                    mCallConversationView.addDescriptor(descriptor, isLocal, false, name);
                }

                if (mCallConversationView.hasDescriptors()) {
                    mUnreadMessageView.setVisibility(View.VISIBLE);
                } else {
                    mUnreadMessageView.setVisibility(View.GONE);
                }

                if (unreadMessage) {
                    mUnreadMessageImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.call_new_message_icon, null));
                } else {
                    mUnreadMessageImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.call_message_icon, null));
                }
            }
        }
    }

    private void onMicroMuteClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onMicroMuteClick");
        }

        mIsAudioMute = !mIsAudioMute;

        mCallMenuView.setIsAudioMuted(mIsAudioMute);
        mCallMenuView.updateMenu();

        Intent intent = new Intent(this, CallService.class);
        intent.setAction(CallService.ACTION_AUDIO_MUTE);
        intent.putExtra(CallService.PARAM_AUDIO_MUTE, mIsAudioMute);
        startService(intent);

        if (mCallParticipantLocaleView != null) {
            mCallParticipantLocaleView.setMicroMute(mIsAudioMute);
            mCallParticipantLocaleView.updateViews();
        }
    }

    private void onSpeakerOnClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSpeakerOnClick");
        }

        CallAudioManager twinmeAudioManager = CallService.getAudioManager();
        if (twinmeAudioManager != null && twinmeAudioManager.isHeadsetAvailable()) {
            openSelectAudioSourceView();
        } else {
            mIsSpeakerOn = !mIsSpeakerOn;

            mCallMenuView.setIsInSpeakerOn(mIsSpeakerOn);
            mCallMenuView.updateMenu();

            Intent intent = new Intent(this, CallService.class);
            intent.setAction(CallService.ACTION_SPEAKER_MODE);
            intent.putExtra(CallService.PARAM_AUDIO_SPEAKER, mIsSpeakerOn);
            startService(intent);
        }
    }

    private void selectAudioSource() {
        if (DEBUG) {
            Log.d(LOG_TAG, "selectAudioSource");
        }

        CallAudioManager callAudioManager = CallService.getAudioManager();
        if (callAudioManager != null) {
            openSelectAudioSourceView();
        }
    }

    private void onControlCameraClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onControlControlClick");
        }

        mCallMenuView.setCallMenuViewState(CallMenuView.CallMenuViewState.DEFAULT);

        CallState callState = CallService.getState();
        if (callState != null && callState.getMainParticipant() != null) {
            CallParticipant participant = callState.getMainParticipant();

            if (isRemoteCameraControl()) {
                DefaultConfirmView defaultConfirmView = new DefaultConfirmView(this, null);
                defaultConfirmView.setForceDarkMode(true);
                defaultConfirmView.setTitle(getString(R.string.call_activity_camera_control));
                String message;
                if (participant.getRemoteActiveCamera() > 0) {
                    message = String.format(getString(R.string.call_activity_camera_control_message), mOriginatorName);
                } else {
                    message = String.format(getString(R.string.call_activity_camera_control_remotely), mOriginatorName);
                }

                defaultConfirmView.setMessage(message);
                defaultConfirmView.setImage(null);
                defaultConfirmView.setConfirmColor(Design.DELETE_COLOR_RED);
                defaultConfirmView.setConfirmTitle(getString(R.string.call_activity_camera_control_stop));
                defaultConfirmView.setCancelTitle(getString(R.string.application_cancel));

                AbstractConfirmView.Observer observer = new AbstractConfirmView.Observer() {
                    @Override
                    public void onConfirmClick() {
                        defaultConfirmView.animationCloseConfirmView();
                        participant.remoteStopControl();
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
                        mRootView.removeView(defaultConfirmView);
                        setStatusBarColor();
                    }
                };
                defaultConfirmView.setObserver(observer);
                mRootView.addView(defaultConfirmView);
                defaultConfirmView.bringToFront();
                defaultConfirmView.show();

                Window window = getWindow();
                window.setNavigationBarColor(Design.POPUP_BACKGROUND_COLOR);
            } else {
                if (getTwinmeApplication().startOnboarding(TwinmeApplication.OnboardingType.REMOTE_CAMERA) && !mShowRemoteCameraOnboardingView) {
                    mShowRemoteCameraOnboardingView = true;
                    OnboardingConfirmView onboardingConfirmView = new OnboardingConfirmView(this, null);
                    onboardingConfirmView.setForceDarkMode(true);
                    onboardingConfirmView.setImage(ResourcesCompat.getDrawable(getResources(), R.drawable.onboarding_control_camera, null));
                    onboardingConfirmView.setTitle(getString(R.string.call_activity_camera_control_needs_help));
                    onboardingConfirmView.setMessage(getString(R.string.call_activity_camera_control_onboarding_part_2));
                    onboardingConfirmView.setConfirmTitle(getString(R.string.application_ok));
                    onboardingConfirmView.setCancelTitle(getString(R.string.application_do_not_display));

                    AbstractConfirmView.Observer observer = new AbstractConfirmView.Observer() {
                        @Override
                        public void onConfirmClick() {
                            onboardingConfirmView.animationCloseConfirmView();
                        }

                        @Override
                        public void onCancelClick() {
                            onboardingConfirmView.animationCloseConfirmView();
                            getTwinmeApplication().setShowOnboardingType(TwinmeApplication.OnboardingType.REMOTE_CAMERA, false);
                            onControlCameraClick();
                        }

                        @Override
                        public void onDismissClick() {
                            onboardingConfirmView.animationCloseConfirmView();
                        }

                        @Override
                        public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                            mRootView.removeView(onboardingConfirmView);

                            if (fromConfirmAction) {
                                onControlCameraClick();
                            }

                            setStatusBarColor();
                        }
                    };
                    onboardingConfirmView.setObserver(observer);
                    mRootView.addView(onboardingConfirmView);
                    onboardingConfirmView.show();

                    int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
                    setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
                    return;
                }
                showPremiumFeature(UIPremiumFeature.FeatureType.CAMERA_CONTROL);
            }
        }
    }

    private void onCameraMuteClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCameraMuteClick");
        }

        boolean isVideoAllowed = mCameraGranted && (mIsCallStartedInVideo || (mOriginator != null && mOriginator.getCapabilities().hasVideo() && mOriginator.getIdentityCapabilities().hasVideo()));
        if (!isVideoAllowed) {
            String message = getString(R.string.application_not_authorized_operation);
            if (mOriginator != null && !mOriginator.getCapabilities().hasVideo()) {
                message = getString(R.string.application_not_authorized_operation_by_your_contact);
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            return;
        }

        CallState callState = CallService.getState();
        if (callState != null && callState.getMainParticipant() != null) {
            CallParticipant participant = callState.getMainParticipant();

            if (participant.getCallConnection().isRemoteControlGranted()) {
                onControlCameraClick();
                return;
            }
        }

        mIsCameraMute = !mIsCameraMute;

        mCallMenuView.setIsCameraMuted(mIsCameraMute);
        mCallMenuView.updateMenu();
        updateCallParticipantView();

        Intent intent = new Intent(this, CallService.class);
        intent.setAction(CallService.ACTION_CAMERA_MUTE);
        intent.putExtra(CallService.PARAM_CAMERA_MUTE, mIsCameraMute);
        startService(intent);
    }

    private void onAddParticipantClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onAddParticipantClick");
        }

        getTwinmeApplication().hideGroupCallAnimation();

        showPremiumFeature(UIPremiumFeature.FeatureType.GROUP_CALL);
    }

    private void onShareInvitationClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onShareInvitationClick");
        }

        mCallMenuView.setCallMenuViewState(CallMenuView.CallMenuViewState.DEFAULT);

        if (getTwinmeApplication().getCurrentSpace().getProfile() != null) {
            AbstractTwinmeService service = new AbstractTwinmeService("AbstractTwinmeService", this, getTwinmeContext(), null);
            service.getProfileImage(getTwinmeApplication().getCurrentSpace().getProfile(), (Bitmap avatar) -> {
                service.dispose();
                InvitationCodeConfirmView invitationCodeConfirmView = new InvitationCodeConfirmView(this, null);
                invitationCodeConfirmView.setForceDarkMode(true);

                invitationCodeConfirmView.setAvatar(avatar, false);
                invitationCodeConfirmView.setTitle(getTwinmeApplication().getCurrentSpace().getProfile().getName());
                invitationCodeConfirmView.setConfirmTitle(getString(R.string.add_contact_activity_invite));
                invitationCodeConfirmView.setMessage(getString(R.string.group_member_activity_invite_personnal_relation));

                AbstractConfirmView.Observer observer = new AbstractConfirmView.Observer() {
                    @Override
                    public void onConfirmClick() {
                        sendInvitation();
                        invitationCodeConfirmView.animationCloseConfirmView();
                    }

                    @Override
                    public void onCancelClick() {
                        invitationCodeConfirmView.animationCloseConfirmView();
                    }

                    @Override
                    public void onDismissClick() {
                        invitationCodeConfirmView.animationCloseConfirmView();
                    }

                    @Override
                    public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                        mRootView.removeView(invitationCodeConfirmView);
                        setStatusBarColor();
                    }
                };
                invitationCodeConfirmView.setObserver(observer);

                mRootView.addView(invitationCodeConfirmView);
                invitationCodeConfirmView.show();

                int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
                setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
            });
        }
    }

    private void onCallPauseClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCallPauseClick");
        }

        Intent intent = new Intent(this, CallService.class);

        if (CallService.isDoubleCall()) {
            intent.setAction(CallService.ACTION_SWAP_CALLS);
        } else {
            CallState call = CallService.getState();
            if (call == null) {
                return;
            }

            if (call.getStatus() == CallStatus.ON_HOLD) {
                intent.setAction(CallService.ACTION_RESUME_CALL);
            } else {
                intent.setAction(CallService.ACTION_HOLD_CALL);
            }
        }

        startService(intent);

        mCallMenuView.setCallMenuViewState(CallMenuView.CallMenuViewState.DEFAULT);
    }

    private void onCertifyRelationClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCertifyRelationClick");
        }

        mCallMenuView.setCallMenuViewState(CallMenuView.CallMenuViewState.DEFAULT);

        CallState call = CallService.getState();
        if (call != null && !call.isVideo()) {
            toast(getString(R.string.call_activity_certify_video_message));
            return;
        }

        startCertifyVideoCall();
    }

    private void onCallConversationClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCallConversationClick");
        }

        mCallConversationView.reloadData();
        mCallConversationView.setVisibility(View.VISIBLE);
        mUnreadMessageImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.call_message_icon, null));

        mCallConversationView.bringToFront();

        mCallMenuView.setCallMenuViewState(CallMenuView.CallMenuViewState.DEFAULT);
    }

    private void onAddStreamingAudio() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onAddStreamingAudio");
        }

        mCallMenuView.setCallMenuViewState(CallMenuView.CallMenuViewState.DEFAULT);
        showPremiumFeature(UIPremiumFeature.FeatureType.STREAMING);
    }

    private void onAcceptClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onAcceptClick");
        }

        acceptCall();
    }

    private void onDeclineClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeclineClick");
        }

        terminateCall(TerminateReason.DECLINE, true);
    }

    private void onHangupClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onHangupClick");
        }

        if (CallStatus.isActive(mMode) || (mMode != null && mMode.isOnHold())) {
            terminateCall(TerminateReason.SUCCESS, true);
        } else {
            terminateCall(TerminateReason.CANCEL, true);
        }
    }

    private void onCloseClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCloseClick");
        }

        finish();
    }

    private void updateOriginator() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateOriginator");
        }

        boolean hideCertifyRelation = true;

        if (mOriginator != null) {
            Originator caller = mOriginator;

            if (mOriginator.getType() == Originator.Type.GROUP_MEMBER) {
                caller = ((GroupMember) mOriginator).getGroup();
            } else if (mOriginator.getType() == Originator.Type.CONTACT) {
                Contact contact = (Contact) mOriginator;
                CertificationLevel level = contact.getCertificationLevel();
                if (level == CertificationLevel.LEVEL_1 || level == CertificationLevel.LEVEL_2 || level == CertificationLevel.LEVEL_3) {
                    hideCertifyRelation = false;
                }
            }

            mOriginatorName = caller.getName();

            if (caller instanceof CallReceiver) {
                mIsCallReceiver = true;
            }
        }

        if (mNameView != null && mOriginatorName != null) {
            mNameView.setText(mOriginatorName);
        }

        if (mCallCertifyView != null) {
            mCallCertifyView.setName(mOriginatorName);
            mCallCertifyView.setAvatar(this, mOriginatorAvatar);
        }

        mCallMenuView.setHideCertifyRelation(hideCertifyRelation);
        mCallMenuView.setIsConversationAllowed(mIsCallReceiver);
        mCallMenuView.setIsShareInvitationAllowed(mIsCallReceiver);

        if (mUIInitialized && mOriginator != null) {
            boolean isInCall = CallStatus.isActive(mMode);

            if (!isInCall) {
                mMessageView.setVisibility(View.VISIBLE);
                mNameView.setVisibility(View.VISIBLE);
            }

            if (mOriginatorName != null) {
                mNameView.setText(mOriginatorName);
            }

            mNameView.setMaxWidth((int)(DESIGN_MAX_NAME_WIDTH * Design.WIDTH_RATIO));
            mCertifiedImageView.setVisibility(View.GONE);

            if (mOriginator.getType() == Originator.Type.CONTACT) {
                Contact contact = (Contact) mOriginator;
                if (contact.getCertificationLevel() == CertificationLevel.LEVEL_4) {
                    mCertifiedImageView.setVisibility(View.VISIBLE);
                    mNameView.setMaxWidth((int)(DESIGN_MAX_NAME_WIDTH * Design.WIDTH_RATIO) - Design.CERTIFIED_HEIGHT);
                }
            }

            mContentView.setVisibility(View.VISIBLE);

            // Update everything to make sure we also refresh the authorized menus.
            updateViews();
        }
    }

    private void terminateCall(@NonNull TerminateReason terminateReason, boolean finish) {
        if (DEBUG) {
            Log.d(LOG_TAG, "terminateCall: terminateReason=" + terminateReason + " finish=" + finish);
        }

        if (mTerminated) {

            return;
        }
        mTerminated = true;

        if (mAvatarAnimatorSet != null) {
            mAvatarAnimatorSet.cancel();
        }

        mChronometerView.stop();

        // Compute the call duration and check for optional quality only if the call was started.
        if (mStartTime > 0) {
            long duration = (SystemClock.elapsedRealtime() - mStartTime) / 1000;
            mShowCallQuality = getTwinmeApplication().askCallQualityWithCallDuration(duration);
            mAskCallQuality = true;
        }

        if (mStreamPlayer != null && mStreamPlayer.getStreamer() != null) {
            Intent stopStreamingIntent = new Intent(this, CallService.class);
            stopStreamingIntent.setAction(CallService.ACTION_STOP_STREAMING);
            startService(stopStreamingIntent);
        }

        Intent intent = new Intent(this, CallService.class);
        intent.setAction(CallService.ACTION_TERMINATE_CALL);
        intent.putExtra(CallService.PARAM_TERMINATE_REASON, terminateReason);
        startService(intent);

        if (terminateReason == TerminateReason.SUCCESS) {
            mTerminatedView.setText(getString(R.string.audio_call_activity_terminate));
        } else {
            mTerminatedView.setText("");
        }

        if (mShowCallQuality) {
            showCallQualityView();
        } else if (finish) {
            finish();
        } else {
            mTerminating = true;
        }
    }

    private String terminateReason(TerminateReason terminateReason) {
        if (DEBUG) {
            Log.d(LOG_TAG, "terminateReason: reason=" + terminateReason);
        }

        if (mOriginatorName == null) {

            return getString(R.string.audio_call_activity_terminate);
        }

        switch (terminateReason) {
            case BUSY:
                return String.format(getString(R.string.audio_call_activity_terminate_busy), mOriginatorName);

            case CANCEL:
                return String.format(getString(R.string.audio_call_activity_terminate_cancel), mOriginatorName);

            case CONNECTIVITY_ERROR:
                return getString(R.string.audio_call_activity_terminate_connectivity_error);

            case DECLINE:
                return String.format(getString(R.string.audio_call_activity_terminate_decline), mOriginatorName);

            case DISCONNECTED:
                return getString(R.string.video_call_activity_terminate_disconnected);

            case NOT_AUTHORIZED:
                return getString(R.string.audio_call_activity_terminate_not_authorized);

            case GONE:
                if (mStartTime > 0) {
                    return  String.format(getString(R.string.call_activity_error_call_interrupted), terminateReason.ordinal());
                } else {
                    return String.format(getString(R.string.audio_call_activity_terminate_gone), mOriginatorName);
                }

            case REVOKED:
                return String.format(getString(R.string.audio_call_activity_terminate_revoked), mOriginatorName);

            case SUCCESS:
                return String.format(getString(R.string.audio_call_activity_terminate_success), mOriginatorName);

            case TIMEOUT:
                if (mMode == CallStatus.OUTGOING_CALL || mMode == CallStatus.OUTGOING_VIDEO_CALL || mMode == CallStatus.OUTGOING_VIDEO_BELL) {

                    return String.format(getString(R.string.audio_call_activity_terminate_timeout), mOriginatorName);
                }

                return getString(R.string.audio_call_activity_terminate);

            case SCHEDULE:

                String message = getString(R.string.show_call_activity_schedule_message);
                if (mOriginator != null) {
                    Schedule schedule = mOriginator.getCapabilities().getSchedule();

                    if (schedule != null && !schedule.getTimeRanges().isEmpty()) {

                        DateTimeRange dateTimeRange = (DateTimeRange) schedule.getTimeRanges().get(0);
                        DateTime start = dateTimeRange.start;
                        DateTime end = dateTimeRange.end;

                        if (start.date.equals(end.date)) {
                            message = String.format(getString(R.string.show_call_activity_schedule_from_to), start.formatDate(), start.formatTime(this), end.formatTime(this));
                        } else {
                            message = String.format("%1$s %2$s", start.formatDateTime(this), end.formatDateTime(this)) ;
                        }
                    }
                }
                return  getString(R.string.show_call_activity_schedule_call) + " : " + message;
                
            default:
                String reason;
                if (mStartTime > 0) {
                    reason = String.format(getString(R.string.call_activity_error_call_interrupted), terminateReason.ordinal());
                } else {
                    reason = String.format(getString(R.string.call_activity_error_call_not_go_thru), terminateReason.ordinal());
                }

                return reason + "<br></br>" + getString(R.string.call_activity_try_to_call_back);
        }
    }

    private void onSendCallQualityClick(int quality) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSendCallQualityClick: quality=" + quality);
        }

        Intent intent = new Intent(this, CallService.class);
        intent.setAction(CallService.ACTION_QUALITY_CALL);
        intent.putExtra(CallService.PARAM_CALL_QUALITY, quality);
        startService(intent);
    }

    private void showCoachMark() {
        if (DEBUG) {
            Log.d(LOG_TAG, "showCoachMark");
        }

        if (getTwinmeApplication().showCoachMark(CoachMark.CoachMarkTag.ADD_PARTICIPANT_TO_CALL)) {
            mCoachMarkView.postDelayed(() -> {
                mCoachMarkView.setVisibility(View.VISIBLE);
                CoachMark coachMark = new CoachMark(getString(R.string.call_activity_coach_mark), CoachMark.CoachMarkTag.ADD_PARTICIPANT_TO_CALL, false, false, new Point((int) mAddParticipantView.getX(), (int) mAddParticipantView.getY()), mAddParticipantView.getHeight(), mAddParticipantView.getHeight(), mAddParticipantView.getHeight() * 0.5f);
                mCoachMarkView.openCoachMark(coachMark);
            }, COACH_MARK_DELAY);
        }
    }

    private void addCallParticipantAnimation() {
        if (DEBUG) {
            Log.d(LOG_TAG, "addCallParticipantAnimation");
        }

        if (!mShowCallGroupAnimation && getTwinmeApplication().showGroupCallAnimation()) {
            mShowCallGroupAnimation = true;
            PropertyValuesHolder propertyValuesHolderX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0f, 1.4f);
            PropertyValuesHolder propertyValuesHolderY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0f, 1.4f);

            ObjectAnimator scaleViewAnimator = ObjectAnimator.ofPropertyValuesHolder(mAddParticipantImageView, propertyValuesHolderX, propertyValuesHolderY);
            scaleViewAnimator.setRepeatMode(ValueAnimator.REVERSE);
            scaleViewAnimator.setRepeatCount(1);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playSequentially(scaleViewAnimator);
            animatorSet.setStartDelay(SCALE_ANIMATION_REPEAT_DELAY);
            animatorSet.setDuration(SCALE_ANIMATION_DURATION);
            animatorSet.start();

            animatorSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(@NonNull Animator animator) {
                }

                @Override
                public void onAnimationEnd(@NonNull Animator animator) {
                    if (getTwinmeApplication().showGroupCallAnimation()) {
                        animatorSet.start();
                    }
                }

                @Override
                public void onAnimationCancel(@NonNull Animator animator) {
                }

                @Override
                public void onAnimationRepeat(@NonNull Animator animator) {
                }
            });

            mAnimatorSets.add(animatorSet);
        }
    }

    private void showInfoFloatingView(@NonNull AppStateInfo info) {
        if (DEBUG) {
            Log.d(LOG_TAG, "showInfoFloatingView info=" + info);
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
            layoutParams.height = layoutParams.width = Design.getHeight((int)DESIGN_INFO_FLOATING_SIZE);
            addContentView(mInfoFloatingView, layoutParams);

            ViewTreeObserver viewTreeObserver = mInfoFloatingView.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
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

    private void onInfoFloatingViewClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onInfoFloatingViewClick");
        }

        final AppStateInfo appInfo = getTwinmeApplication().appInfo();
        if (appInfo == null) {
            return;
        }
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

    private void checkBatteryOptimizations() {
        if (DEBUG) {
            Log.d(LOG_TAG, "checkBatteryOptimizations");
        }

        if (mShowBackgroundRestriction) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            final AndroidDeviceInfo androidDeviceInfo = new AndroidDeviceInfo(this);
            if (androidDeviceInfo.isBackgroundRestricted() && getTwinmeApplication().startCallRestrictionMessage()) {
                mShowBackgroundRestriction = true;

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setClass(this, CallRestrictionActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        }
    }

    private void setMenuVisibility(boolean visible) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setMenuVisibility: visible=" + visible);
        }

        if (mCallParticipantLocaleView != null && mCallParticipantLocaleView.getVisibility() == View.GONE) {
            if (mMenuVisibility) {
                return;
            }
            visible = true;
        }

        mMenuVisibility = visible;

        animateMenu();
    }

    private void onSimpleTapCallParticipantView(AbstractCallParticipantView callParticipantView) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSimpleTapCallParticipantView: " + callParticipantView);
        }

        if (callParticipantView.isScreenSharing() && callParticipantView.getCallParticipantViewAspect() == AbstractCallParticipantView.CallParticipantViewAspect.FULLSCREEN) {
            return;
        }

        if (mCallParticipantViewList.size() > 2) {
            setMenuVisibility(!mMenuVisibility);
        } else {

            for (AbstractCallParticipantView cpv : mCallParticipantViewList) {
                if (cpv.isScreenSharing()) {
                    return;
                }
            }
            if (mCallParticipantViewMode == AbstractCallParticipantView.CallParticipantViewMode.SMALL_LOCALE_VIDEO && !callParticipantView.isRemoteParticipant()) {
                resetParticipantsView();
                mCallParticipantViewMode = AbstractCallParticipantView.CallParticipantViewMode.SMALL_REMOTE_VIDEO;
                updateParticipantsView(CallService.getState());
            } else if (mCallParticipantViewMode == AbstractCallParticipantView.CallParticipantViewMode.SMALL_REMOTE_VIDEO && callParticipantView.isRemoteParticipant()) {
                mCallParticipantViewMode = AbstractCallParticipantView.CallParticipantViewMode.SMALL_LOCALE_VIDEO;
                updateParticipantsView(CallService.getState());
            } else {
                setMenuVisibility(!mMenuVisibility);
            }

            if (mCallParticipantViewMode != AbstractCallParticipantView.CallParticipantViewMode.SPLIT_SCREEN) {
                for (AbstractCallParticipantView cpv : mCallParticipantViewList) {
                    cpv.bringVideoToFront();
                    if (!cpv.isMainParticipant()) {
                        mParticipantsView.bringChildToFront(cpv);
                        break;
                    }
                }
            }
        }
    }

    private void onDoubleTapCallParticipantView(AbstractCallParticipantView callParticipantView) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDoubleTapCallParticipantView: " + callParticipantView);
        }

        if (mCallParticipantViewList.size() > 2) {
            mParticipantsView.bringToFront();
            mParticipantsOverlayView.bringToFront();
            callParticipantView.bringToFront();
            callParticipantView.updateCallParticipantViewAspect();

            mParticipantsOverlayView.setVisibility(View.VISIBLE);

            float alpha = 0f;

            if (callParticipantView.getCallParticipantViewAspect() == AbstractCallParticipantView.CallParticipantViewAspect.FULLSCREEN) {
                alpha = 1f;
            }

            mParticipantsOverlayView.animate()
                    .alpha(alpha)
                    .setDuration(500)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (callParticipantView.getCallParticipantViewAspect() == AbstractCallParticipantView.CallParticipantViewAspect.FIT) {
                                mParticipantsOverlayView.setVisibility(View.GONE);
                            } else {
                                mParticipantsOverlayView.setVisibility(View.VISIBLE);
                            }
                        }
                    });

            for (AbstractCallParticipantView cpv : mCallParticipantViewList) {
                cpv.bringVideoToFront();
            }

            mParticipantsView.bringChildToFront(callParticipantView);
        } else {
            if (mCallParticipantViewMode == AbstractCallParticipantView.CallParticipantViewMode.SPLIT_SCREEN) {
                mCallParticipantViewMode = AbstractCallParticipantView.CallParticipantViewMode.SMALL_LOCALE_VIDEO;
            } else {
                mCallParticipantViewMode = AbstractCallParticipantView.CallParticipantViewMode.SPLIT_SCREEN;
            }

            updateParticipantsView(CallService.getState());

            for (AbstractCallParticipantView cpv : mCallParticipantViewList) {
                cpv.bringVideoToFront();
            }
        }
    }

    private void onFullScreenTapCallParticipantView(AbstractCallParticipantView callParticipantView) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onFullScreenTapCallParticipantView: " + callParticipantView);
        }

        if (mCallParticipantViewList.size() > 2) {
            setMenuVisibility(false);
        }

        callParticipantView.bringToFront();
        callParticipantView.updateCallParticipantViewAspect();
        mParticipantsOverlayView.setVisibility(View.VISIBLE);

        float alpha = 0f;
        if (callParticipantView.getCallParticipantViewAspect() == AbstractCallParticipantView.CallParticipantViewAspect.FULLSCREEN) {
            alpha = 1f;
        }

        mParticipantsOverlayView.animate()
                .alpha(alpha)
                .setDuration(500)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (callParticipantView.getCallParticipantViewAspect() == AbstractCallParticipantView.CallParticipantViewAspect.FIT) {
                            mParticipantsOverlayView.setVisibility(View.GONE);
                        } else {
                            mParticipantsOverlayView.setVisibility(View.VISIBLE);
                        }

                        if (mCallParticipantViewList.size() == 2) {
                            setMenuVisibility(false);
                        }

                        callParticipantView.minZoom();
                        mParticipantsView.bringToFront();
                        mParticipantsOverlayView.bringToFront();
                        mParticipantsView.bringChildToFront(callParticipantView);
                    }
                });

        for (AbstractCallParticipantView cpv : mCallParticipantViewList) {
            cpv.bringVideoToFront();
        }
    }

    private void onMinimizeTapCallParticipantView(AbstractCallParticipantView callParticipantView) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onMinimizeTapCallParticipantView: " + callParticipantView);
        }

        if (mCallParticipantViewList.size() == 2) {
            mParticipantsView.bringToFront();
            callParticipantView.updateCallParticipantViewAspect();
            callParticipantView.resetView();

            for (AbstractCallParticipantView cpv : mCallParticipantViewList) {
                cpv.bringVideoToFront();
            }

            mCallParticipantViewMode = AbstractCallParticipantView.CallParticipantViewMode.SMALL_LOCALE_VIDEO;
            updateParticipantsView(CallService.getState());

            mParticipantsOverlayView.setVisibility(View.GONE);
            setMenuVisibility(true);

        } else {
            if (callParticipantView.getCallParticipantViewAspect() == AbstractCallParticipantView.CallParticipantViewAspect.FULLSCREEN) {
                callParticipantView.updateCallParticipantViewAspect();
                callParticipantView.resetView();
                updateParticipantsView(CallService.getState());
                mParticipantsOverlayView.setVisibility(View.GONE);
            } else {
                setMenuVisibility(!mMenuVisibility);
            }
        }
    }

    private void onSwitchCameraTapCallParticipant(AbstractCallParticipantView callParticipantView) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSwitchCameraTapCallParticipant: " + callParticipantView);
        }

        if (!callParticipantView.isRemoteParticipant()) {

            CallState callState = CallService.getState();
            if (callState != null && callState.getMainParticipant() != null) {
                CallParticipant participant = callState.getMainParticipant();

                if (participant.getCallConnection().isRemoteControlGranted()) {
                    onControlCameraClick();
                    return;
                }
            }

            boolean isVideoAllowed = mCameraGranted && (mIsCallStartedInVideo || (mOriginator != null && mOriginator.getCapabilities().hasVideo() && mOriginator.getIdentityCapabilities().hasVideo()));

            if (isVideoAllowed && !mIsCameraMute) {
                Intent intent = new Intent(this, CallService.class);
                intent.setAction(CallService.ACTION_SWITCH_CAMERA);
                startService(intent);
            }
        } else {
            CallParticipant callParticipant = callParticipantView.getCallParticipant();
            callParticipant.remoteSwitchCamera(callParticipant.getRemoteActiveCamera() == 2);
        }
    }

    private void updateCallParticipantView() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateCallParticipantView");
        }

        if (!mUIInitialized) {
            return;
        }

        CallState callState = CallService.getState();
        if (callState != null) {
            final List<CallParticipant> participants = callState.getParticipants();
            for (CallParticipant callParticipant : participants) {

                if(callParticipant.getTransferredToParticipantId() != null){
                    // This participant has been transferred, nothing to do
                    continue;
                }

                StreamingStatus streamingStatus = callParticipant.getStreamingStatus();
                if (mStreamPlayer == null && (streamingStatus == StreamingStatus.PLAYING || callParticipant.getStreamPlayer() != null)) {
                    updateAudioStreaming(callParticipant, CallParticipantEvent.EVENT_STREAM_STATUS);
                }

                AbstractCallParticipantView callParticipantView = getParticipantView(callParticipant);
                if (callParticipantView == null) {
                    CallParticipantRemoteView callParticipantRemoteView = new CallParticipantRemoteView(this);
                    callParticipantRemoteView.setParticipant(callParticipant);
                    callParticipantRemoteView.setColor(getRandomColor());
                    callParticipantRemoteView.setInitVideoInFitMode(getTwinmeApplication().isVideoInFitMode());

                    AbstractCallParticipantView.OnCallParticipantClickListener onCallParticipantClickListener = new AbstractCallParticipantView.OnCallParticipantClickListener() {
                        @Override
                        public void onSimpleTap() {
                            onSimpleTapCallParticipantView(callParticipantRemoteView);
                        }

                        @Override
                        public void onDoubleTap() {
                            onDoubleTapCallParticipantView(callParticipantRemoteView);
                        }

                        @Override
                        public void onInfoTap() {
                            onInfoCallParticipantClick(callParticipantRemoteView.getName());
                        }

                        @Override
                        public void onCancelTap() {
                            onCancelCallParticipantClick(callParticipantRemoteView);
                        }

                        @Override
                        public void onSwipeDown() {
                            detectSwipe(true);
                        }

                        @Override
                        public void onSwipeUp() {
                            detectSwipe(false);
                        }

                        @Override
                        public void onFullScreenTap() {
                            onFullScreenTapCallParticipantView(callParticipantRemoteView);
                        }

                        @Override
                        public void onMinimizeTap() {
                            onMinimizeTapCallParticipantView(callParticipantRemoteView);
                        }

                        @Override
                        public void onSwitchCameraTap() {
                            onSwitchCameraTapCallParticipant(callParticipantRemoteView);
                        }
                    };

                    callParticipantRemoteView.setOnCallParticipantClickListener(onCallParticipantClickListener);

                    AbstractCallParticipantView.OnCallParticipantScaleListener onCallParticipantScaleListener = new AbstractCallParticipantView.OnCallParticipantScaleListener() {
                        @Override
                        public void onZoomChanged(float zoom) {

                            if (callParticipantRemoteView.getCallParticipant() != null && callParticipantRemoteView.getCallParticipant().getRemoteActiveCamera() > 0) {
                                callParticipantRemoteView.getCallParticipant().remoteSetZoom((int) zoom);
                            }
                        }

                        @Override
                        public void onSaveFitMode(boolean fitMode) {
                            getTwinmeApplication().setVideoInFitMode(fitMode);
                        }
                    };

                    callParticipantRemoteView.setOnCallParticipantScaleListener(onCallParticipantScaleListener);

                    mParticipantsView.addView(callParticipantRemoteView);
                    mCallParticipantViewList.add(callParticipantRemoteView);
                } else {
                    CallParticipantRemoteView callParticipantRemoteView = (CallParticipantRemoteView) callParticipantView;
                    callParticipantRemoteView.setParticipant(callParticipant);
                }
            }

            //add locale video
            if (mOriginator != null && mCallParticipantLocaleView == null) {
                mCallParticipantLocaleView = new CallParticipantLocaleView(this);
                AbstractCallParticipantView.OnCallParticipantClickListener onCallParticipantClickListener = new AbstractCallParticipantView.OnCallParticipantClickListener() {
                    @Override
                    public void onSimpleTap() {
                        onSimpleTapCallParticipantView(mCallParticipantLocaleView);
                    }

                    @Override
                    public void onDoubleTap() {
                        onDoubleTapCallParticipantView(mCallParticipantLocaleView);
                    }

                    @Override
                    public void onInfoTap() {

                    }

                    @Override
                    public void onCancelTap() {

                    }

                    @Override
                    public void onSwipeDown() {
                        detectSwipe(true);
                    }

                    @Override
                    public void onSwipeUp() {
                        detectSwipe(false);
                    }

                    @Override
                    public void onFullScreenTap() {

                    }

                    @Override
                    public void onMinimizeTap() {

                    }

                    @Override
                    public void onSwitchCameraTap() {

                        onSwitchCameraTapCallParticipant(mCallParticipantLocaleView);
                    }
                };

                mCallParticipantLocaleView.setOnCallParticipantClickListener(onCallParticipantClickListener);

                AbstractCallParticipantView.OnCallParticipantScaleListener onCallParticipantScaleListener = new AbstractCallParticipantView.OnCallParticipantScaleListener() {
                    @Override
                    public void onZoomChanged(float zoom) {
                        if (mHasCamera) {
                            getTwinmeContext().getPeerConnectionService().setZoom((int) zoom);
                        }
                    }

                    @Override
                    public void onSaveFitMode(boolean fitMode) {

                    }
                };

                mCallParticipantLocaleView.setOnCallParticipantScaleListener(onCallParticipantScaleListener);

                mCallParticipantLocaleView.setName(mOriginator.getIdentityName());
                mCallParticipantLocaleView.setAvatar(mOriginatorIdentityAvatar);
                mCallParticipantLocaleView.setMicroMute(mIsAudioMute);

                if (CallStatus.isActive(mMode)) {
                    mCallParticipantLocaleView.setCameraMute(mIsCameraMute);
                } else {
                    mCallParticipantLocaleView.setCameraMute(true);
                }

                mCallParticipantLocaleView.updateViews();

                mParticipantsView.addView(mCallParticipantLocaleView);
                mCallParticipantViewList.add(mCallParticipantLocaleView);
            } else if (mOriginator != null) {
                mCallParticipantLocaleView.setAvatar(mOriginatorIdentityAvatar);
                mCallParticipantLocaleView.setMicroMute(mIsAudioMute);
                mCallParticipantLocaleView.setCameraMute(mIsCameraMute);
                mCallParticipantLocaleView.updateViews();
            }

            if (!CallStatus.isTerminated(mMode)) {
                // To remove a participant, start from the end of the list
                // (don't iterate to avoid a ConcurrentModificationException if we remove at the same time)
                for (int i = mCallParticipantViewList.size(); i > 0; ) {
                    i--;
                    AbstractCallParticipantView callParticipantView = mCallParticipantViewList.get(i);
                    if (callParticipantView.isRemoteParticipant() && !isParticipantInCall(participants, callParticipantView.getCallParticipant())) {
                        mParticipantsView.removeView(callParticipantView);
                        mCallParticipantViewList.remove(i);
                    }
                }
            }
        }

        updateParticipantsView(callState);
    }

    private AbstractCallParticipantView[] sortViews(List<AbstractCallParticipantView> callParticipantViewList, CallParticipant mainParticipant) {
        int numberParticipant = callParticipantViewList.size();
        AbstractCallParticipantView[] sortedViews = new AbstractCallParticipantView[numberParticipant];

        if (numberParticipant == 1) {
            sortedViews[0] = callParticipantViewList.get(0);
        } else {
            int i = 1;
            for (AbstractCallParticipantView callParticipantView : callParticipantViewList) {
                if (callParticipantView.getCallParticipant() == mainParticipant) {
                    sortedViews[0] = callParticipantView;
                } else {
                    if (i == numberParticipant) {
                        Log.w(LOG_TAG, "sortViews: could not find main participant");
                        sortedViews[0] = callParticipantView;
                    }else {
                        sortedViews[i++] = callParticipantView;
                    }
                }
            }
        }
        return sortedViews;
    }

    private void updateParticipantsView(CallState callState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateParticipantsView");
        }

        CallStatus currentStatus = callState != null ? callState.getStatus() : mMode;

        // Get the main participant and views only once because they can change between two calls (due to multi-threading).
        final CallParticipant mainParticipant = (callState != null ? callState.getMainParticipant() : null);
        final List<AbstractCallParticipantView> callParticipantViewList = new ArrayList<>(mCallParticipantViewList);

        int position = 1;
        StringBuilder participantsName = new StringBuilder();
        int numberParticipant = callParticipantViewList.size();
        boolean isOneCameraEnableInCall = isOneCameraEnableInCall();

        // Sort the views: main participant must be in 1st position for the positioning algorithm to work correctly.
        AbstractCallParticipantView[] sortedViews = sortViews(callParticipantViewList, mainParticipant);

        for (AbstractCallParticipantView callParticipantView : sortedViews) {
            if (callParticipantView.isRemoteParticipant()) {
                participantsName.append(callParticipantView.getName());
            }
            boolean isMainParticipant = false;
            if (callState != null) {
                isMainParticipant = callParticipantView.getCallParticipant() == mainParticipant;
            }
            callParticipantView.setCallParticipantViewMode(mCallParticipantViewMode);
            int width;
            if (mIsLandscape) {
                width = Math.max(mRootViewWidth, mRootViewHeight);
            } else {
                width = Math.min(mRootViewWidth, mRootViewHeight);
            }

            callParticipantView.setPosition(isMainParticipant, width, getParticipantViewHeight(), numberParticipant, position, !mMenuVisibility, currentStatus, isOneCameraEnableInCall, mIsCallReceiver, mIsLandscape);
            position++;

            if (position < numberParticipant) {
                participantsName.append(", ");
            }
        }

        if (mIsCallReceiver || (mOriginator != null && mOriginator.isGroup())) {
            mNameView.setText(mOriginatorName);
        } else {
            mNameView.setText(participantsName.toString());
        }

        mNameView.setMaxWidth((int)(DESIGN_MAX_NAME_WIDTH * Design.WIDTH_RATIO));

        if ((numberParticipant == 2 || mIsCallReceiver || (mOriginator != null && mOriginator.isGroup())) && callState != null ) {
            mNameView.setVisibility(View.VISIBLE);
            mCertifiedImageView.setVisibility(View.GONE);
            if (mOriginator != null && mOriginator.getType() == Originator.Type.CONTACT) {
                Contact contact = (Contact) mOriginator;
                if (contact.getCertificationLevel() == CertificationLevel.LEVEL_4) {
                    mCertifiedImageView.setVisibility(View.VISIBLE);
                    mNameView.setMaxWidth((int)(DESIGN_MAX_NAME_WIDTH * Design.WIDTH_RATIO) - Design.CERTIFIED_HEIGHT);
                }
            }
        } else {
            mNameView.setVisibility(View.GONE);
            mCertifiedImageView.setVisibility(View.GONE);
        }

        if (!mCallParticipantViewList.isEmpty()) {
            mNoParticipantView.setVisibility(View.GONE);
        } else {
            mNoParticipantView.setVisibility(View.VISIBLE);
        }
    }

    private void resetParticipantsView() {
        if (DEBUG) {
            Log.d(LOG_TAG, "resetParticipantsView");
        }

        for (AbstractCallParticipantView callParticipantView : mCallParticipantViewList) {
            callParticipantView.resetView();
        }
    }

    private AbstractCallParticipantView getParticipantView(CallParticipant callParticipant) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getParticipantView");
        }

        final Integer transferredId = callParticipant.getTransferredFromParticipantId();
        if (transferredId != null) {
            // This participant is a transfer target.
            // If we've got a view for the transferred participant,
            // it will become the transfer target's view.
            for (AbstractCallParticipantView callParticipantView : mCallParticipantViewList) {
                if (callParticipantView.getParticipantId() == transferredId) {
                    return callParticipantView;
                }
            }
            // No view found => updateCallParticipantView has already reassigned the view
            // to the transfer target (which updates the view's participantId).
        }

        final int id = callParticipant.getParticipantId();
        for (AbstractCallParticipantView callParticipantView : mCallParticipantViewList) {
            if (callParticipantView.getParticipantId() == id) {
                return callParticipantView;
            }
        }

        return null;
    }

    private boolean isParticipantInCall(List<CallParticipant> participants, CallParticipant callParticipant) {
        if (DEBUG) {
            Log.d(LOG_TAG, "isParticipantInCall");
        }

        final int id = callParticipant.getParticipantId();
        for (CallParticipant participant : participants) {
            if (participant.getParticipantId() == id) {
                return true;
            }
        }

        return false;
    }

    private boolean isOneCameraEnableInCall() {
        if (DEBUG) {
            Log.d(LOG_TAG, "isOneCameraEnableInCall");
        }

        for (AbstractCallParticipantView callParticipantView : mCallParticipantViewList) {
            if (!callParticipantView.isCameraMute() || callParticipantView.isScreenSharing()) {
                return true;
            }
        }

        return false;
    }

    private boolean isRemoteCameraControl() {
        if (DEBUG) {
            Log.d(LOG_TAG, "isRemoteCameraControl");
        }

        for (AbstractCallParticipantView callParticipantView : mCallParticipantViewList) {
            if (callParticipantView.isRemoteCameraControl()) {
                return true;
            }
        }

        return false;
    }

    private boolean isRemoteCameraControlSupported() {
        if (DEBUG) {
            Log.d(LOG_TAG, "isRemoteCameraControlSupported");
        }

        for (AbstractCallParticipantView callParticipantView : mCallParticipantViewList) {
            if (callParticipantView.isRemoteCameraControlSupported()) {
                return true;
            }
        }

        return false;
    }

    private boolean isMessageSupported() {
        if (DEBUG) {
            Log.d(LOG_TAG, "isMessageSupported");
        }

        for (AbstractCallParticipantView callParticipantView : mCallParticipantViewList) {
            if (callParticipantView.isMessageSupported()) {
                return true;
            }
        }

        return false;
    }

    private void updateCameraControl(@NonNull CallParticipant participant, @NonNull CallParticipantEvent event) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateCameraControl: " + participant + " event=" + event);
        }

        updateViews();

        switch (event) {
            case EVENT_ASK_CAMERA_CONTROL: {
                if (!mCameraGranted) {
                    participant.remoteAnswerControl(false);
                    return;
                }
                DefaultConfirmView defaultConfirmView = new DefaultConfirmView(this, null);
                defaultConfirmView.setForceDarkMode(true);
                defaultConfirmView.setTitle(getString(R.string.call_activity_camera_control));
                String message = String.format(getString(R.string.call_activity_camera_control_confirm_message), mOriginatorName);
                defaultConfirmView.setMessage(message);
                defaultConfirmView.setImage(null);
                defaultConfirmView.setConfirmTitle(getString(R.string.application_accept));
                defaultConfirmView.setCancelTitle(getString(R.string.application_cancel));

                AbstractConfirmView.Observer observer = new AbstractConfirmView.Observer() {
                    @Override
                    public void onConfirmClick() {
                        defaultConfirmView.animationCloseConfirmView();
                        participant.remoteAnswerControl(true);
                        updateViews();
                    }

                    @Override
                    public void onCancelClick() {
                        defaultConfirmView.animationCloseConfirmView();
                        participant.remoteAnswerControl(false);
                    }

                    @Override
                    public void onDismissClick() {
                        defaultConfirmView.animationCloseConfirmView();
                        participant.remoteAnswerControl(false);
                    }

                    @Override
                    public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                        mRootView.removeView(defaultConfirmView);
                        setStatusBarColor();
                    }
                };
                defaultConfirmView.setObserver(observer);
                mRootView.addView(defaultConfirmView);
                defaultConfirmView.bringToFront();
                defaultConfirmView.show();

                Window window = getWindow();
                window.setNavigationBarColor(Design.POPUP_BACKGROUND_COLOR);
                break;
            }

            case EVENT_CAMERA_CONTROL_GRANTED:
                participant.remoteCameraMute(false);
                break;

            case EVENT_CAMERA_CONTROL_DENIED: {
                AlertMessageView alertMessageView = new AlertMessageView(this, null);
                alertMessageView.setWindowHeight(getWindow().getDecorView().getHeight());
                alertMessageView.setForceDarkMode(true);
                alertMessageView.setTitle(getString(R.string.call_activity_camera_control));
                alertMessageView.setMessage(String.format(getString(R.string.call_activity_camera_control_denied), mOriginatorName));

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
                        mRootView.removeView(alertMessageView);
                        setStatusBarColor();
                    }
                };
                alertMessageView.setObserver(observer);

                mRootView.addView(alertMessageView);
                alertMessageView.show();

                int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
                setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);

                break;
            }

            case EVENT_CAMERA_CONTROL_DONE:
                break;

            default:
                break;
        }
    }

    private void updateAudioStreaming(@NonNull CallParticipant participant, @NonNull CallParticipantEvent event) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateAudioStreaming: " + participant + " event=" + event);
        }

        switch (event) {
            case EVENT_STREAM_START:
                if (participant.getStreamPlayer() != null) {
                    mStreamPlayer = participant.getStreamPlayer();
                    updateParticipantsView(CallService.getState());
                }
                break;

            case EVENT_STREAM_STATUS:
                CallState callState = CallService.getState();
                StreamingStatus streamingStatus = participant.getStreamingStatus();
                if (streamingStatus == StreamingStatus.PLAYING && callState != null && callState.getCurrentStreamer() != null && callState.getCurrentStreamer().getPlayer() != null) {
                    mStreamPlayer = callState.getCurrentStreamer().getPlayer();
                    updateParticipantsView(CallService.getState());
                } else if (participant.getStreamPlayer() != null) {
                    mStreamPlayer = participant.getStreamPlayer();
                    updateParticipantsView(CallService.getState());
                } else if (streamingStatus == StreamingStatus.READY) {
                    mStreamPlayer = null;
                    updateParticipantsView(CallService.getState());
                } else if (streamingStatus == StreamingStatus.ERROR) {
                    Intent intent = new Intent(this, CallService.class);
                    intent.setAction(CallService.ACTION_STOP_STREAMING);
                    startService(intent);

                    mStreamPlayer = null;
                    updateParticipantsView(CallService.getState());
                    toast(getString(R.string.streaming_audio_activity_error_message));
                } else if (streamingStatus == StreamingStatus.UNSUPPORTED) {
                    Intent intent = new Intent(this, CallService.class);
                    intent.setAction(CallService.ACTION_STOP_STREAMING);
                    startService(intent);

                    mStreamPlayer = null;
                    updateParticipantsView(CallService.getState());
                    toast(getString(R.string.streaming_audio_activity_error_message));
                }
                break;

            case EVENT_STREAM_STOP:
                mStreamPlayer = null;
                updateParticipantsView(CallService.getState());
                mCallStreamingAudioView.stopStreaming();
                break;

            case EVENT_STREAM_PAUSE:
                mCallStreamingAudioView.pauseStreaming();
                break;

            case EVENT_STREAM_RESUME:
                mCallStreamingAudioView.resumeStreaming();
                break;

            case EVENT_STREAM_INFO:
                if (participant.getStreamPlayer() != null) {
                    mStreamPlayer = participant.getStreamPlayer();
                }
                break;

            default:
                break;
        }

        if (mStreamPlayer != null) {
            mCallStreamingAudioView.setVisibility(View.VISIBLE);
            mCallStreamingAudioView.setSound(mStreamPlayer.getMediaInfo());
        } else {
            mCallStreamingAudioView.setVisibility(View.GONE);
        }
    }

    private void playPauseStreaming() {
        if (DEBUG) {
            Log.d(LOG_TAG, "playPauseStreaming");
        }

        if (mStreamPlayer.getStreamer() != null) {
            if (mStreamPlayer.isPause()) {
                mStreamPlayer.getStreamer().resumeStreaming();
            } else {
                mStreamPlayer.getStreamer().pauseStreaming();
            }
        } else {
            if (mStreamPlayer.isPause()) {
                mStreamPlayer.askResume();
            } else {
                mStreamPlayer.askPause();
            }
        }
    }

    private void stopStreaming() {
        if (DEBUG) {
            Log.d(LOG_TAG, "stopStreaming");
        }

        if (mStreamPlayer.getStreamer() != null) {
            Intent intent = new Intent(this, CallService.class);
            intent.setAction(CallService.ACTION_STOP_STREAMING);
            startService(intent);

            mStreamPlayer = null;
            mCallStreamingAudioView.setVisibility(View.GONE);
            updateParticipantsView(CallService.getState());
        } else {
            mStreamPlayer.askStop();
        }

        animateMenu();
    }

    private void sendInvitation() {
        if (DEBUG) {
            Log.d(LOG_TAG, "sendInvitation");
        }

        CallState callState = CallService.getState();
        if (callState != null && getTwinmeApplication().getCurrentSpace().getProfile() != null) {
            Profile profile = getTwinmeApplication().getCurrentSpace().getProfile();
            if (profile.getTwincodeOutbound() != null && mAudioCallService != null) {
                mAudioCallService.createURI(TwincodeURI.Kind.Invitation, profile.getTwincodeOutbound(), (BaseService.ErrorCode errorCode, TwincodeURI twincodeURI) -> {
                    if (twincodeURI != null) {
                        ConversationService.Descriptor descriptor = callState.createTwincodeDescriptor(twincodeURI.twincodeId, Profile.SCHEMA_ID, twincodeURI.pubKey, null, true);
                        if (!callState.sendDescriptor(descriptor)) {
                            // Descriptor was not sent: no active participant accepts receiving messages.
                        }
                    }
                });
            }
        }
    }

    private void sendMessage(String message) {
        if (DEBUG) {
            Log.d(LOG_TAG, "sendMessage: " + message);
        }

        CallState callState = CallService.getState();
        if (callState != null) {
            ConversationService.Descriptor descriptor = callState.createMessage(message, null, true);
            if (callState.sendDescriptor(descriptor)) {
                String name = "";
                if (mOriginator != null) {
                    name = mOriginator.getIdentityName();
                }
                mUnreadMessageView.setVisibility(View.VISIBLE);
                mUnreadMessageImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.call_message_icon, null));
                mCallConversationView.addDescriptor(descriptor, true, true, name);
            }
        }
    }

    //
    // Key check
    //

    private void handleKeyCheckEvent(@NonNull CallParticipantEvent event) {
        if (DEBUG) {
            Log.d(LOG_TAG, "handleKeyCheckEvent: " + event);
        }

        boolean updateWord = false;

        switch (event) {
            case EVENT_KEY_CHECK_INITIATE:
                // Peer has requested a key check session.
                // For now we automatically accept the request.
                // CallService.getCurrentKeyCheckWord() will return the first word.
                startCertifyView(true, false);
                mWordCheckChallenge = CallService.getKeyCheckCurrentWord();
                updateWord = true;
                break;

            case EVENT_ON_KEY_CHECK_INITIATE:
                // Peer has accepted our key check session request.
                // CallService.getCurrentKeyCheckWord() will return the first word.
                mWordCheckChallenge = CallService.getKeyCheckCurrentWord();
                updateWord = true;
                break;

            case EVENT_CURRENT_WORD_CHANGED:
                // Peer has confirmed a word. If we've also validated this word,
                // CallService.getCurrentKeyCheckWord() will return the next word.
                mWordCheckChallenge = CallService.getKeyCheckCurrentWord();
                updateWord = true;
                break;

            case EVENT_WORD_CHECK_RESULT_KO:
                // Peer has marked a word as invalid. We can get the failed challenge through
                // CallService.getKeyCheckPeerError()
                WordCheckChallenge failedChallenge = CallService.getKeyCheckPeerError();
                mCallCertifyView.certifyRelationFailed();
                stopKeyCheckSession();
                break;

            case EVENT_TERMINATE_KEY_CHECK:
                // Both us and the peer have finished the checking process.
                // At this point CallService.isKeyCheckOK() returns a non-null value:
                // true if every local and peer checks were OK, false otherwise
                // NB: the actual verify/trust process is not done yet, and it may fail. Maybe add another event to indicate we're completely finished?
                Boolean ok = CallService.isKeyCheckOK();
                mCallCertifyView.certifyRelationSuccess();
                stopKeyCheckSession();
                break;


            default:
                Log.e(LOG_TAG, "event " + event + " is not a key check event");
                break;
        }

        if (updateWord) {
            mCallCertifyView.setCurrentWord(mWordCheckChallenge);
        }
    }

    /**
     * Start a key check session. Does nothing if we're not in a 1-on-1 call, or the peer's twincode is not signed.
     *
     * @param language the language of the wordlist. Defaults to the current locale if null. We only support english for now so this parameter has no effect.
     */
    private void startKeyCheckSession(@Nullable Locale language) {
        if (DEBUG) {
            Log.d(LOG_TAG, "startKeyCheckSession: language=" + language);
        }

        Intent intent = new Intent(this, CallService.class);
        intent.setAction(CallService.ACTION_START_KEY_CHECK);

        // If PARAM_KEY_CHECK_LANGUAGE is not set we'll use Locale.getDefault().
        // At the moment we only have the english wordlist, so this extra has no effect.
        if (language != null) {
            // Locale is serializable so we can just use putExtra().
            intent.putExtra(CallService.PARAM_KEY_CHECK_LANGUAGE, language);
        }
        startService(intent);
    }

    private void stopKeyCheckSession() {
        if (DEBUG) {
            Log.d(LOG_TAG, "stopKeyCheckSession");
        }

        Intent intent = new Intent(this, CallService.class).setAction(CallService.ACTION_STOP_KEY_CHECK);
        startService(intent);
    }

    /**
     * Save the result locally and send it to the peer.
     *
     * @param index the index of the verified word.
     * @param ok    true if the word is correct, false otherwise.
     */
    private void sendKeyCheckResult(int index, boolean ok) {
        if (DEBUG) {
            Log.d(LOG_TAG, "sendKeyCheckResult: index=" + index + " ok=" + ok);
        }

        Intent intent = new Intent(this, CallService.class);
        intent.setAction(CallService.ACTION_WORD_CHECK_RESULT);
        // The index comes from the WordCheckChallenge returned by CallService.getKeyCheckCurrentWord().
        intent.putExtra(CallService.PARAM_WORD_CHECK_INDEX, index);
        intent.putExtra(CallService.PARAM_WORD_CHECK_RESULT, ok);
        startService(intent);

        // After this event is processed and the peer has sent us his result for this word,
        // CallService.getKeyCheckCurrentWord() will return the next word.
        // CallService.isKeyCheckDone() will tell us if both sides have checked all words.
        // CallService.isKeyCheckOK() will tell us if all words were confirmed OK by both sides.
    }

    private int getParticipantViewHeight() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getParticipantViewHeight");
        }

        int height;
        if (mIsLandscape) {
            height = Math.min(mRootViewWidth, mRootViewHeight);
        } else {
            height = Math.max(mRootViewWidth, mRootViewHeight) - mBarTopInset;
        }

        int playerHeight = 0;
        if (mStreamPlayer != null) {
            playerHeight =  DEFAULT_CONTAINER_HEIGHT + PARTICIPANTS_BOTTOM_MARGIN;
        }

        int callHoldHeight = 0;
        if (CallService.isDoubleCall()) {
            callHoldHeight =  DEFAULT_CONTAINER_HEIGHT + PARTICIPANTS_BOTTOM_MARGIN;
        }

        if (mMenuVisibility) {
            return height - (BUTTON_HEIGHT + (MENU_VIEW_HEIGHT - DEFAULT_MENU_VIEW_BOTTOM_MARGIN) + PARTICIPANTS_BOTTOM_MARGIN + playerHeight + callHoldHeight);
        } else {
            return height - SIDE_MARGIN - playerHeight - callHoldHeight;
        }
    }

    /**
     * Called 1s after we detect a change in the proximity and no other change occurred: the proximity is stabilized.
     */
    private void updateProximityLock() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateProximityLock");
        }

        if (mProximitySensor != null && mProximitySensor.sensorReportsNearState()) {
            if (mIsSpeakerOn) {
                mIsSpeakerOnBeforeProximityUpdate = true;
                onSpeakerOnClick();
            }
            turnOffScreen();
        } else {
            if (mIsSpeakerOnBeforeProximityUpdate) {
                mIsSpeakerOnBeforeProximityUpdate = false;
                onSpeakerOnClick();
            }
            turnOnScreen();
        }
    }

    /**
     * This method is called when the proximity sensor reports a state change,
     * e.g. from "NEAR to FAR" or from "FAR to NEAR".
     */
    private void onProximitySensorChangedState() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onProximitySensorChangedState");
        }

        if (!CallStatus.isActive(mMode) || !mRemoteVideoMute) {
            turnOnScreen();
            return;
        }

        // Don't take into account the proximity change immediately but leave 750ms
        // to make sure it stabilized: cancel a previous post and schedule a new one.
        if (mProximmityHandler != null) {
            mProximmityHandler.removeCallbacksAndMessages(null);
        } else {
            mProximmityHandler = new Handler();
        }

        if (mProximitySensor != null) {
            mProximmityHandler.postDelayed(this::updateProximityLock, PROXIMITY_DELAY);
        }
    }

    private void turnOnScreen() {
        if (DEBUG) {
            Log.d(LOG_TAG, "turnOnScreen");
        }

        if (mScreenOffWakeLock != null && mScreenOffWakeLock.isHeld()) {
            mScreenOffWakeLock.release();
        }
    }

    private void turnOffScreen() {
        if (DEBUG) {
            Log.d(LOG_TAG, "turnOffScreen");
        }

        if (mScreenOffWakeLock == null) {
            PowerManager powerManager = (PowerManager) this.getSystemService(POWER_SERVICE);
            if (powerManager != null && powerManager.isWakeLockLevelSupported(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK)) {
                mScreenOffWakeLock = powerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, WAKELOCK_TAG);
            }
        }
        if (mScreenOffWakeLock != null) {
            mScreenOffWakeLock.acquire(DateUtils.HOUR_IN_MILLIS * 2);
            mTurnOffTime = System.currentTimeMillis();
        }
    }

    private void onInfoCallParticipantClick(String name) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onInfoCallParticipantClick: " + name);
        }

        ViewGroup viewGroup = findViewById(R.id.call_activity_layout);

        AlertMessageView alertMessageView = new AlertMessageView(this, null);
        alertMessageView.setWindowHeight(getWindow().getDecorView().getHeight());
        alertMessageView.setForceDarkMode(true);
        alertMessageView.setMessage(String.format(getString(R.string.call_activity_not_supported_group_call_message), name));

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
                setStatusBarColor();
            }
        };
        alertMessageView.setObserver(observer);

        viewGroup.addView(alertMessageView);
        alertMessageView.show();

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }

    private void onCancelCallParticipantClick(AbstractCallParticipantView callParticipantView) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCancelCallParticipantClick: " + callParticipantView);
        }

        Intent intent = new Intent(this, CallService.class);
        intent.setAction(CallService.ACTION_TERMINATE_CALL);
        intent.putExtra(CallService.PARAM_TERMINATE_REASON, TerminateReason.CANCEL);
        intent.putExtra(CallService.PARAM_PEER_CONNECTION_ID, callParticipantView.getCallParticipant().getPeerConnectionId());
        startService(intent);
    }

    private void onMergeCallClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onMergeCallClick");
        }

        showPremiumFeature(UIPremiumFeature.FeatureType.GROUP_CALL);
    }

    private void onHangupHoldCallClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onHangupHoldCallClick");
        }

        CallState holdCall = CallService.getHoldState();

        if(holdCall == null){
            return;
        }

        Intent intent = new Intent(this, CallService.class);
        intent.setAction(CallService.ACTION_TERMINATE_CALL);
        intent.putExtra(CallService.PARAM_TERMINATE_REASON, TerminateReason.SUCCESS);
        intent.putExtra(CallService.PARAM_CALL_ID, holdCall.getId());
        startService(intent);

        mCallHoldView.setVisibility(View.GONE);
        animateMenu();
    }

    private void onSwapCallClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSwapCallClick");
        }

        Intent intent = new Intent(this, CallService.class);
        intent.setAction(CallService.ACTION_SWAP_CALLS);
        startService(intent);
    }

    private boolean isRemoteCameraControlEvent(CallParticipantEvent event) {
        if (DEBUG) {
            Log.d(LOG_TAG, "isRemoteCameraControlEvent: " + event);
        }

        return event == CallParticipantEvent.EVENT_ASK_CAMERA_CONTROL
                || event == CallParticipantEvent.EVENT_CAMERA_CONTROL_DENIED
                || event == CallParticipantEvent.EVENT_CAMERA_CONTROL_GRANTED
                || event == CallParticipantEvent.EVENT_CAMERA_CONTROL_DONE;
    }

    private boolean isStreamEvent(CallParticipantEvent event) {
        if (DEBUG) {
            Log.d(LOG_TAG, "isStreamEvent: " + event);
        }

        return event == CallParticipantEvent.EVENT_STREAM_INFO
                || event == CallParticipantEvent.EVENT_STREAM_PAUSE
                || event == CallParticipantEvent.EVENT_STREAM_RESUME
                || event == CallParticipantEvent.EVENT_STREAM_START
                || event == CallParticipantEvent.EVENT_STREAM_STOP
                || event == CallParticipantEvent.EVENT_STREAM_STATUS;
    }

    private boolean isKeyCheckEvent(CallParticipantEvent event) {
        if (DEBUG) {
            Log.d(LOG_TAG, "isKeyCheckEvent: " + event);
        }

        return event == CallParticipantEvent.EVENT_KEY_CHECK_INITIATE
                || event == CallParticipantEvent.EVENT_ON_KEY_CHECK_INITIATE
                || event == CallParticipantEvent.EVENT_CURRENT_WORD_CHANGED
                || event == CallParticipantEvent.EVENT_WORD_CHECK_RESULT_KO
                || event == CallParticipantEvent.EVENT_TERMINATE_KEY_CHECK;
    }

    private void detectSwipe(boolean isDownGesture) {
        if (DEBUG) {
            Log.d(LOG_TAG, "detectSwipe: " + isDownGesture);
        }

        boolean needsUpdate = false;

        if (isDownGesture && mCallMenuView.getCallMenuViewState() == CallMenuView.CallMenuViewState.EXTEND) {
            needsUpdate = true;
            mCallMenuView.setCallMenuViewState(CallMenuView.CallMenuViewState.DEFAULT);
        } else if (!isDownGesture && mCallMenuView.getCallMenuViewState() == CallMenuView.CallMenuViewState.DEFAULT) {
            needsUpdate = true;
            mCallMenuView.setCallMenuViewState(CallMenuView.CallMenuViewState.EXTEND);
        }

        if (needsUpdate) {
            updateMenuState(mCallMenuView.getCallMenuViewState());
        }
    }

    private void updateMenuState(CallMenuView.CallMenuViewState state) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateMenuState");
        }

        if (mCallCertifyView != null && mCallCertifyView.getVisibility() == View.VISIBLE) {
            mCallCertifyView.bringToFront();
        }

        mCallMenuView.bringToFront();

        bringAbstractConfirmViewToFront();

        mCallMenuView.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        mCallMenuView.getLayoutTransition().setDuration(ANIMATE_MENU_DURATION);
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mCallMenuView.getLayoutParams();
        if (state == CallMenuView.CallMenuViewState.EXTEND) {
            marginLayoutParams.bottomMargin = MENU_VIEW_BOTTOM_MARGIN;
        } else {
            marginLayoutParams.bottomMargin = -DEFAULT_MENU_VIEW_BOTTOM_MARGIN;
        }

        mCallMenuView.animate()
                .setDuration(ANIMATE_MENU_DURATION);
    }

    private void initCallParticipantColor() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initCallParticipantColor");
        }

        mCallParticipantColors.add(Color.parseColor("#F5B000"));
        mCallParticipantColors.add(Color.parseColor("#85CE79"));
        mCallParticipantColors.add(Color.parseColor("#6DB8C2"));
        mCallParticipantColors.add(Color.parseColor("#4CD0D9"));
        mCallParticipantColors.add(Color.parseColor("#4C8DD9"));
        mCallParticipantColors.add(Color.parseColor("#704CD9"));
        mCallParticipantColors.add(Color.parseColor("#E36F04"));
        mCallParticipantColors.add(Color.parseColor("#7991CE"));
        mCallParticipantColors.add(Color.parseColor("#F53B00"));
        mCallParticipantColors.add(Color.parseColor("#E15A5A"));
        mCallParticipantColors.add(Color.parseColor("#96A655"));
    }

    private int getRandomColor() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getRandomColor");
        }

        if (!mCallParticipantColors.isEmpty()) {
            int random = new Random().nextInt(mCallParticipantColors.size());
            int randomColor = mCallParticipantColors.get(random);
            mCallParticipantColors.remove(random);
            return randomColor;
        }

        return Design.GREY_BACKGROUND_COLOR;
    }

    private void hapticFeedback() {
        if (DEBUG) {
            Log.d(LOG_TAG, "hapticFeedback");
        }

        final TwinmeApplication twinmeApplication = getTwinmeApplication();
        final int hapticFeedbackMode = twinmeApplication.hapticFeedbackMode();

        if (hapticFeedbackMode == TwinmeApplication.HapticFeedbackMode.SYSTEM.ordinal()) {
            getWindow().getDecorView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        } else if (hapticFeedbackMode == TwinmeApplication.HapticFeedbackMode.ON.ordinal()) {
            getWindow().getDecorView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        }
    }

    private void openAppSettings() {
        if (DEBUG) {
            Log.d(LOG_TAG, "openAppSettings");
        }

        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + BuildConfig.APPLICATION_ID));
        startActivity(intent);
    }

    private void audioRecordGranted() {
        if (DEBUG) {
            Log.d(LOG_TAG, "audioRecordGranted");
        }

        Intent intentMute = new Intent(this, CallService.class);
        intentMute.setAction(CallService.ACTION_AUDIO_MUTE);
        intentMute.putExtra(CallService.PARAM_AUDIO_MUTE, true);
        startService(intentMute);

        Intent intentUnmute = new Intent(this, CallService.class);
        intentUnmute.setAction(CallService.ACTION_AUDIO_MUTE);
        intentUnmute.putExtra(CallService.PARAM_AUDIO_MUTE, false);
        startService(intentUnmute);
    }

    private void initNoParticipantImageView() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initNoParticipantImageView");
        }

        if (!mRootViewInitialized) {
            return;
        }

        float avatarHeight =  (int) ((getParticipantViewHeight() - SIDE_MARGIN) * 0.5f);

        if (mNoParticipantViewInitialized) {
            mNoParticipantView.setX(SIDE_MARGIN);
            mNoParticipantView.setY(getParticipantViewHeight() * 0.5f - (avatarHeight * 0.5f));
            mNoParticipantView.setImageBitmap(mOriginatorAvatar);
            mNoParticipantView.requestLayout();
            return;
        }

        ViewGroup.LayoutParams avatarLayoutParams = mNoParticipantView.getLayoutParams();
        avatarLayoutParams.width = Design.DISPLAY_WIDTH - (SIDE_MARGIN * 2);
        avatarLayoutParams.height = (int) avatarHeight;
        mNoParticipantView.setLayoutParams(avatarLayoutParams);

        mNoParticipantView.setX(SIDE_MARGIN);
        mNoParticipantView.setY(getParticipantViewHeight() * 0.5f - (avatarHeight * 0.5f));

        mNoParticipantView.setImageBitmap(mOriginatorAvatar);

        mNoParticipantView.requestLayout();

        mNoParticipantViewInitialized = true;
    }

    private void applyInsets(int rootLayout, int backgroundColor) {
        if (DEBUG) {
            Log.d(LOG_TAG, "applyInsets rootLayout=" + rootLayout + " backgroundColor=" + backgroundColor);
        }

        View rootView = findViewById(rootLayout);

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                            | WindowInsetsCompat.Type.displayCutout()
                            | WindowInsetsCompat.Type.ime()
            );

            v.setBackgroundColor(backgroundColor);

            int topPadding = bars.top;
            int bottomPadding = 0;

            if (!mIsLandscape) {
                mBarTopInset = topPadding;
            }

            v.setPadding(0, topPadding, 0, bottomPadding);

            return WindowInsetsCompat.CONSUMED;
        });
    }

    private void startCertifyView(boolean showOnboarding, boolean startKeySession) {
        if (DEBUG) {
            Log.d(LOG_TAG, "startCertifyView");
        }

        if (mCallCertifyView == null) {
            mCallCertifyView = new CallCertifyView(this, null);

            // Use same layout parameters in portrait/landscape modes
            @SuppressWarnings("deprecation")
            PercentRelativeLayout.LayoutParams layoutParams = new PercentRelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            mCallCertifyView.setLayoutParams(layoutParams);
            mRootView.addView(mCallCertifyView);

            CallCertifyView.CallCertifyListener callCertifyListener = new CallCertifyView.CallCertifyListener() {
                @Override
                public void onCancelWord() {

                    hapticFeedback();

                    if (mWordCheckChallenge != null) {
                        sendKeyCheckResult(mWordCheckChallenge.index, false);
                    }
                }

                @Override
                public void onConfirmWord() {

                    hapticFeedback();

                    if (mWordCheckChallenge != null) {
                        sendKeyCheckResult(mWordCheckChallenge.index, true);
                    }
                }

                @Override
                public void onCertifyViewFinish() {

                    mCallCertifyView.setVisibility(View.GONE);
                    mHeaderView.setAlpha(1.f);
                    mCallMenuView.setHideCertifyRelation(true);
                    mCallMenuView.setIsCertifyRunning(false);
                    mCallMenuView.updateMenu();
                    if (!mMenuVisibility) {
                        setMenuVisibility(true);
                    }
                }

                @Override
                public void onCertifyViewSingleTap() {

                    setMenuVisibility(!mMenuVisibility);
                }
            };

            mCallCertifyView.setCallCertifyListener(callCertifyListener);
        }

        if (mOriginatorName != null) {
            mCallCertifyView.setName(mOriginatorName);
        }

        if (mOriginatorAvatar != null) {
            mCallCertifyView.setAvatar(this, mOriginatorAvatar);
        }

        mCallMenuView.setCallMenuViewState(CallMenuView.CallMenuViewState.DEFAULT);

        mCallCertifyView.setVisibility(View.VISIBLE);
        mHeaderView.setAlpha(0.5f);
        mCallCertifyView.bringToFront();
        mCallMenuView.bringToFront();

        if (startKeySession) {
            startKeyCheckSession(null);
        }

        if (mMenuVisibility) {
            setMenuVisibility(false);
        }

        if (showOnboarding) {
            showCertifyByVideoCallOnboarding();
        }
    }

    private void showCertifyByVideoCallOnboarding() {
        if (DEBUG) {
            Log.d(LOG_TAG, "showCertifyByVideoCallOnboarding");
        }

        OnboardingConfirmView onboardingConfirmView = new OnboardingConfirmView(this, null);
        onboardingConfirmView.setForceDarkMode(true);
        onboardingConfirmView.setTitle(getString(R.string.authentified_relation_activity_to_be_certified_title));

        boolean darkMode = false;
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        int displayMode = getTwinmeApplication().displayMode();
        if ((currentNightMode == Configuration.UI_MODE_NIGHT_YES && displayMode == DisplayMode.SYSTEM.ordinal())  || displayMode == DisplayMode.DARK.ordinal()) {
            darkMode = true;
        }

        onboardingConfirmView.setImage(ResourcesCompat.getDrawable(getResources(), darkMode ? R.drawable.onboarding_authentified_relation_dark : R.drawable.onboarding_authentified_relation, null));

        onboardingConfirmView.setMessage(String.format(getString(R.string.call_activity_certify_onboarding_start_message), mOriginatorName));
        onboardingConfirmView.setConfirmTitle(getString(R.string.authentified_relation_activity_start));
        onboardingConfirmView.hideCancelView();

        AbstractConfirmView.Observer observer = new AbstractConfirmView.Observer() {
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
                mRootView.removeView(onboardingConfirmView);
                setStatusBarColor();
            }
        };
        onboardingConfirmView.setObserver(observer);
        mRootView.addView(onboardingConfirmView);
        onboardingConfirmView.show();

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }

    private void showPremiumFeature(UIPremiumFeature.FeatureType featureType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "showPremiumFeature");
        }

        PremiumFeatureConfirmView premiumFeatureConfirmView = new PremiumFeatureConfirmView(this, null);
        premiumFeatureConfirmView.setForceDarkMode(true);
        premiumFeatureConfirmView.initWithPremiumFeature(new UIPremiumFeature(this, featureType));

        AbstractConfirmView.Observer observer = new AbstractConfirmView.Observer() {
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
                mRootView.removeView(premiumFeatureConfirmView);
                setStatusBarColor();
            }
        };
        premiumFeatureConfirmView.setObserver(observer);

        mRootView.addView(premiumFeatureConfirmView);
        premiumFeatureConfirmView.show();

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }

    private void showCallQualityView() {
        if (DEBUG) {
            Log.d(LOG_TAG, "showCallQualityView");
        }

        CallQualityView callQualityView = new CallQualityView(this, null);
        callQualityView.setForceDarkMode(true);

        AbstractConfirmView.Observer observer = new AbstractConfirmView.Observer() {
            @Override
            public void onConfirmClick() {
                callQualityView.animationCloseConfirmView();
            }

            @Override
            public void onCancelClick() {
                callQualityView.animationCloseConfirmView();
            }

            @Override
            public void onDismissClick() {
                callQualityView.animationCloseConfirmView();
            }

            @Override
            public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                mRootView.removeView(callQualityView);
                setStatusBarColor();
                finish();
            }
        };
        callQualityView.setObserver(observer);

        CallQualityView.CallQualityObserver callQualityObserver = quality -> {
            onSendCallQualityClick(quality);
            callQualityView.animationCloseConfirmView();
        };
        callQualityView.setCallQualityObserver(callQualityObserver);

        mRootView.addView(callQualityView);
        callQualityView.bringToFront();
        callQualityView.show();

        Window window = getWindow();
        window.setNavigationBarColor(Design.POPUP_BACKGROUND_COLOR);
    }

    private void openSelectAudioSourceView() {
        if (DEBUG) {
            Log.d(LOG_TAG, "openSelectAudioSourceView");
        }

        if (mSelectAudioSourceView == null) {
            mSelectAudioSourceView = new SelectAudioSourceView(this, null);

            // Use same layout parameters in portrait/landscape modes
            @SuppressWarnings("deprecation")
            PercentRelativeLayout.LayoutParams layoutParams = new PercentRelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            mSelectAudioSourceView.setLayoutParams(layoutParams);
            mRootView.addView(mSelectAudioSourceView);

            SelectAudioSourceView.SelectAudioSourceListener selectAudioSourceListener = new SelectAudioSourceView.SelectAudioSourceListener() {
                @Override
                public void onSelectAudioSource(AudioDevice audioDevice) {
                    mSelectAudioSourceView.closeMenu();
                    CallAudioManager audioManager = CallService.getAudioManager();

                    if (audioManager != null) {
                        audioManager.setCommunicationDevice(audioDevice);
                    }
                }

                @Override
                public void onDismissSelectAudioSourceView() {
                    mSelectAudioSourceView.closeMenu();
                }

                @Override
                public void onCloseSelectAudioSourceViewFinish() {

                    mSelectAudioSourceView.setVisibility(View.GONE);
                }
            };

            mSelectAudioSourceView.setSelectAudioSourceListener(selectAudioSourceListener);
        }

        mSelectAudioSourceView.setVisibility(View.VISIBLE);
        mSelectAudioSourceView.bringToFront();

        CallAudioManager twinmeAudioManager = CallService.getAudioManager();

        if (twinmeAudioManager != null) {
            List<UIAudioSource> audioSources = new ArrayList<>();

            for (AudioDevice audioDevice : twinmeAudioManager.getAudioDevices()) {
                UIAudioSource uiAudioSource = new UIAudioSource(this, audioDevice);

                if (audioDevice == AudioDevice.BLUETOOTH) {
                    uiAudioSource.setName(twinmeAudioManager.getBluetoothDeviceName());
                }

                if (twinmeAudioManager.getSelectedAudioDevice() == audioDevice) {
                    uiAudioSource.setIsSelected(true);
                }

                audioSources.add(uiAudioSource);
            }
            mSelectAudioSourceView.setAudioSources(audioSources);
        }

        mSelectAudioSourceView.openMenu();
    }

    private void backPressed() {
        if (DEBUG) {
            Log.d(LOG_TAG, "backPressed");
        }

        if (mMode == null) {
            return;
        }

        switch (mMode) {
            case INCOMING_CALL:
            case INCOMING_VIDEO_BELL:
            case OUTGOING_CALL:
            case OUTGOING_VIDEO_CALL:
            case OUTGOING_VIDEO_BELL:
            case IN_VIDEO_BELL:
                terminateCall(TerminateReason.CANCEL, true);
                break;

            case ACCEPTED_INCOMING_CALL:
            case ACCEPTED_OUTGOING_CALL:
            case IN_CALL:
            case FALLBACK:
            case TERMINATED:
                break;
        }
    }

    private void bringAbstractConfirmViewToFront() {
        if (DEBUG) {
            Log.d(LOG_TAG, "bringAbstractConfirmViewToFront");
        }

        if (mRootView == null) {
            return;
        }

        for (int i = 0; i < mRootView.getChildCount(); i++) {
            View child = mRootView.getChildAt(i);

            if (child instanceof AbstractConfirmView) {
                child.bringToFront();
                return;
            }
        }
    }
}
