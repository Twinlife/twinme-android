/*
 *  Copyright (c) 2022 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.callActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.core.content.res.ResourcesCompat;
import androidx.percentlayout.widget.PercentRelativeLayout;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.audio.AudioDevice;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.RoundedView;

public class CallMenuView extends PercentRelativeLayout {
    private static final String LOG_TAG = "CallMenuView";
    private static final boolean DEBUG = false;

    enum CallMenuViewState {
        DEFAULT,
        EXTEND
    }

    interface CallMenuListener {
        void onCallPause();

        void onCertifyRelation();

        void onOpenConversation();

        void onSelectStreamingAudio();

        void onShareInvitation();

        void onMicroMute();

        void onCameraMute();

        void onControlCamera();

        void onSpeakerOn();

        void onSpeakerLongClick();

        void onHangup();

        void onMenuStateUpdated(CallMenuViewState state);
    }

    private class HangupListener implements OnClickListener {

        private boolean disabled = false;

        @Override
        public void onClick(View view) {
            if (DEBUG) {
                Log.d(LOG_TAG, "HangupListener.onClick: view=" + view);
            }

            if (disabled) {

                return;
            }
            disabled = true;

            onHangupClick();
        }
    }

    private static final int DESIGN_ACTION_VIEW_COLOR = Color.rgb(24, 24, 24);
    private static final int DESIGN_ACTION_VIEW_RADIUS = 14;

    private static final float DESIGN_MARGIN_ACTION_BUTTON = 18f;
    private static final float DESIGN_BUTTON_HEIGHT = 100f;
    private static final float DESIGN_ACTION_HEIGHT = 242f;
    private static final float DESIGN_CONTAINER_HEIGHT = 342f;
    private static final float DESIGN_ICON_PAUSE_CALL_HEIGHT = 32;
    private static final float DESIGN_ICON_RESUME_CALL_HEIGHT = 40;
    private static final int MARGIN_ACTION_BUTTON;
    private static final int BUTTON_HEIGHT;

    static {
        MARGIN_ACTION_BUTTON = (int) (DESIGN_MARGIN_ACTION_BUTTON * Design.WIDTH_RATIO);
        BUTTON_HEIGHT = (int) (DESIGN_BUTTON_HEIGHT * Design.HEIGHT_RATIO);
    }

    private View mCameraMuteView;
    private View mMicroMuteView;
    private View mSpeakerOnView;
    private View mConversationView;
    private View mInvitationView;
    private View mStreamingAudioView;
    private View mCertifyRelationView;
    private View mCameraControlView;
    private ImageView mMicroMuteImageView;
    private ImageView mCameraMuteImageView;
    private ImageView mSpeakerImageView;
    private ImageView mPauseImageView;
    private ImageView mCameraControlImageView;

    private CallMenuViewState mCallMenuViewState = CallMenuViewState.DEFAULT;

    private boolean mIsInCall = false;
    private boolean mIsAudioMuted = false;
    private boolean mIsSpeakerOn = false;
    private boolean mIsCameraMute = true;
    private boolean mIsVideoAllowed = true;
    private boolean mIsConversationAllowed = false;
    private boolean mIsStreamingAudioSupported = false;
    private boolean mIsShareInvitationAllowed = false;
    private boolean mIsInPause = false;
    private boolean mHideCertify = false;
    private boolean mIsCertifyRunning = false;
    private boolean mIsCameraControlAllowed = false;
    private boolean mIsRemoteCameraControl = false;

    private AudioDevice mAudioDevice;
    private boolean mIsHeadSetAvailable = false;

    private CallMenuListener mCallMenuListener;

    public CallMenuView(Context context) {

        super(context);
    }

    public CallMenuView(Context context, AttributeSet attrs) {

        super(context, attrs);

        try {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = inflater.inflate(R.layout.call_activity_menu_view, (ViewGroup) getParent());
            //noinspection deprecation
            view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            addView(view);

            initViews();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CallMenuView(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs, defStyle);
    }

    void setCallMenuViewState(CallMenuViewState state) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setCallMenuViewState: " + state);
        }

        mCallMenuViewState = state;
        mCallMenuListener.onMenuStateUpdated(mCallMenuViewState);
    }

    CallMenuViewState getCallMenuViewState() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getCallMenuViewState");
        }

        return mCallMenuViewState;
    }

    public void setIsInCall(boolean isInCall) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setIsInCall: " + isInCall);
        }

        mIsInCall = isInCall;
    }

    public void setIsAudioMuted(boolean isAudioMuted) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setIsAudioMuted: " + isAudioMuted);
        }

        mIsAudioMuted = isAudioMuted;
    }

    public void setIsInSpeakerOn(boolean isSpeakerOn) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setIsInSpeakerOn: " + isSpeakerOn);
        }

        mIsSpeakerOn = isSpeakerOn;
    }

    public void setIsCameraMuted(boolean isCameraMuted) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setIsCameraMuted: " + isCameraMuted);
        }

        mIsCameraMute = isCameraMuted;
    }

    public void setIsVideoAllowed(boolean isVideoAllowed) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setIsVideoAllowed: " + isVideoAllowed);
        }

        mIsVideoAllowed = isVideoAllowed;
    }

    public void setIsInPause(boolean isInPause) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setIsInPause: " + isInPause);
        }

        mIsInPause = isInPause;
    }

    public void setIsConversationAllowed(boolean isConversationAllowed) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setIsConversationAllowed: " + isConversationAllowed);
        }

        mIsConversationAllowed = isConversationAllowed;
    }

    public void setIsStreamingAudioSupported(boolean isStreamingAudioSupported) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setIsStreamingAudioSupported: " + isStreamingAudioSupported);
        }

        mIsStreamingAudioSupported = isStreamingAudioSupported;
    }

    public void setIsShareInvitationAllowed(boolean isShareInvitationAllowed) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setIsShareInvitationAllowed: " + isShareInvitationAllowed);
        }

        mIsShareInvitationAllowed = isShareInvitationAllowed;
    }

    public void setHideCertifyRelation(boolean hideCertifyRelation) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setHideCertifyRelation: " + hideCertifyRelation);
        }

        mHideCertify = hideCertifyRelation;
    }

    public void setIsCertifyRunning(boolean isCertifyRunning) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setIsCertifyRunning: " + isCertifyRunning);
        }

        mIsCertifyRunning = isCertifyRunning;
    }

    public void setIsCameraControlAllowed(boolean isCameraControlAllowed) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setIsCameraControlAllowed: " + isCameraControlAllowed);
        }

        mIsCameraControlAllowed = isCameraControlAllowed;
    }

    public void setIsRemoteCameraControl(boolean isRemoteCameraControl) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setIsRemoteCameraControl: " + isRemoteCameraControl);
        }

        mIsRemoteCameraControl = isRemoteCameraControl;
    }

    public void setAudioDevice(AudioDevice audioDevice, boolean isHeadSetAvailable) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setAudioDevice: " + audioDevice);
        }

        mAudioDevice = audioDevice;
        mIsHeadSetAvailable = isHeadSetAvailable;
    }

    void setCallMenuListener(CallMenuListener callMenuListener) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setCallMenuListener: " + callMenuListener);
        }

        mCallMenuListener = callMenuListener;
    }

    public void updateMenu() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateMenu");
        }

        if (mIsInCall) {
            mMicroMuteView.setAlpha(1.0f);
            mCameraMuteView.setAlpha(1.0f);
            mSpeakerOnView.setAlpha(1.0f);
            mConversationView.setAlpha(1.0f);
            mStreamingAudioView.setAlpha(1.0f);
            mInvitationView.setAlpha(1.0f);
            mCertifyRelationView.setAlpha(1.0f);
            mCameraControlView.setAlpha(1.0f);
        } else {
            mMicroMuteView.setAlpha(0.5f);
            mCameraMuteView.setAlpha(0.5f);
            mConversationView.setAlpha(0.5f);
            mStreamingAudioView.setAlpha(0.5f);
            mInvitationView.setAlpha(0.5f);
            mCertifyRelationView.setAlpha(0.5f);
            mCameraControlView.setAlpha(0.5f);
        }

        if (!mIsAudioMuted) {
            mMicroMuteImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.mute_action_call_off, null));
        } else {
            mMicroMuteImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.mute_action_call_on, null));
        }

        if (!mIsCameraMute) {
            mCameraMuteImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.video_mute_action_off, null));
        } else {
            mCameraMuteImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.video_mute_action_on, null));
        }

        if (!mIsHeadSetAvailable) {
            if (mIsSpeakerOn) {
                mSpeakerImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.loud_speaker_action_call_on, null));
            } else {
                mSpeakerImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.loud_speaker_action_call_off, null));
            }
        } else {
            updateSourceAudioIcon();
        }

        ViewGroup.LayoutParams layoutParams = mPauseImageView.getLayoutParams();
        if (mIsInPause) {
            mPauseImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.call_resume_icon, null));
            layoutParams.height = (int) (DESIGN_ICON_RESUME_CALL_HEIGHT * Design.HEIGHT_RATIO);
            layoutParams.width = (int) (DESIGN_ICON_RESUME_CALL_HEIGHT * Design.HEIGHT_RATIO);
        } else {
            mPauseImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.call_pause_icon, null));
            layoutParams.height = (int) (DESIGN_ICON_PAUSE_CALL_HEIGHT * Design.HEIGHT_RATIO);
            layoutParams.width = (int) (DESIGN_ICON_PAUSE_CALL_HEIGHT * Design.HEIGHT_RATIO);
        }

        if (mIsVideoAllowed && mIsInCall) {
            mCameraMuteView.setAlpha(1.0f);
        } else {
            mCameraMuteView.setAlpha(0.5f);
        }

        if (mIsCertifyRunning) {
            mCameraMuteView.setAlpha(0.5f);
            mConversationView.setAlpha(0.5f);
            mStreamingAudioView.setAlpha(0.5f);
            mInvitationView.setAlpha(0.5f);
            mCertifyRelationView.setAlpha(0.5f);
            mCameraControlView.setAlpha(0.5f);
        }

        if (mIsConversationAllowed) {
            mConversationView.setVisibility(View.VISIBLE);
        } else {
            mConversationView.setVisibility(View.INVISIBLE);
        }

        if (mIsStreamingAudioSupported) {
            mStreamingAudioView.setVisibility(View.VISIBLE);
        } else {
            mStreamingAudioView.setVisibility(View.INVISIBLE);
        }

        if (mIsShareInvitationAllowed) {
            mInvitationView.setVisibility(View.VISIBLE);
        } else {
            mInvitationView.setVisibility(View.GONE);
        }

        if (mHideCertify) {
            mCertifyRelationView.setVisibility(View.GONE);
        } else {
            mCertifyRelationView.setVisibility(View.VISIBLE);
        }

        if (mIsCameraControlAllowed) {
            mCameraControlView.setVisibility(View.VISIBLE);
            if (mIsRemoteCameraControl) {
                mCameraControlImageView.setColorFilter(Design.DELETE_COLOR_RED);
            } else {
                mCameraControlImageView.setColorFilter(Color.BLACK);
            }
        } else {
            mCameraControlView.setVisibility(View.INVISIBLE);
        }

        int viewPosition = 1;

        if (mIsStreamingAudioSupported) {
            int margin = BUTTON_HEIGHT * viewPosition + MARGIN_ACTION_BUTTON * viewPosition * 2;
            MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mStreamingAudioView.getLayoutParams();
            marginLayoutParams.leftMargin = margin;
            marginLayoutParams.setMarginStart(margin);
            viewPosition++;
        }

        if (mIsShareInvitationAllowed) {
            int margin = BUTTON_HEIGHT * viewPosition + MARGIN_ACTION_BUTTON * viewPosition * 2;
            MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mInvitationView.getLayoutParams();
            marginLayoutParams.leftMargin = margin;
            marginLayoutParams.setMarginStart(margin);
            viewPosition++;
        }

        if (!mHideCertify) {
            int margin = BUTTON_HEIGHT * viewPosition + MARGIN_ACTION_BUTTON * viewPosition * 2;
            MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mCertifyRelationView.getLayoutParams();
            marginLayoutParams.leftMargin = margin;
            marginLayoutParams.setMarginStart(margin);
            viewPosition++;
        }

        if (mIsCameraControlAllowed) {
            int margin = BUTTON_HEIGHT * viewPosition + MARGIN_ACTION_BUTTON * viewPosition * 2;
            MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mCameraControlView.getLayoutParams();
            marginLayoutParams.leftMargin = margin;
            marginLayoutParams.setMarginStart(margin);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        View containerView = findViewById(R.id.call_activity_container_view);
        ViewGroup.LayoutParams layoutParams = containerView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_CONTAINER_HEIGHT * Design.HEIGHT_RATIO);

        float radius = DESIGN_ACTION_VIEW_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, 0, 0, 0, 0};

        ShapeDrawable containerViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        containerViewBackground.getPaint().setColor(DESIGN_ACTION_VIEW_COLOR);
        containerView.setBackground(containerViewBackground);

        View slideMarkView = findViewById(R.id.call_activity_slide_mark_view);
        layoutParams = slideMarkView.getLayoutParams();
        layoutParams.height = Design.SLIDE_MARK_HEIGHT;

        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.mutate();
        gradientDrawable.setColor(Color.WHITE);
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        slideMarkView.setBackground(gradientDrawable);

        float corner = ((float)Design.SLIDE_MARK_HEIGHT / 2) * Resources.getSystem().getDisplayMetrics().density;
        gradientDrawable.setCornerRadius(corner);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) slideMarkView.getLayoutParams();
        marginLayoutParams.topMargin = Design.SLIDE_MARK_TOP_MARGIN;

        View actionView = findViewById(R.id.call_activity_action_view);

        layoutParams = actionView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_ACTION_HEIGHT * Design.HEIGHT_RATIO);
        layoutParams.width = (BUTTON_HEIGHT * 5) + MARGIN_ACTION_BUTTON * 8;

        mMicroMuteView = findViewById(R.id.call_activity_micro_mute_view);
        mMicroMuteView.setOnClickListener(v -> onMicroMuteClick());

        layoutParams = mMicroMuteView.getLayoutParams();
        layoutParams.height = BUTTON_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mMicroMuteView.getLayoutParams();
        marginLayoutParams.leftMargin = MARGIN_ACTION_BUTTON;
        marginLayoutParams.rightMargin = MARGIN_ACTION_BUTTON;
        marginLayoutParams.setMarginStart(MARGIN_ACTION_BUTTON);
        marginLayoutParams.setMarginEnd(MARGIN_ACTION_BUTTON);

        RoundedView microMuteBackgroundView = findViewById(R.id.call_activity_micro_mute_background_view);
        microMuteBackgroundView.setColor(Color.WHITE);

        mMicroMuteImageView = findViewById(R.id.call_activity_micro_mute_image_view);

        mCameraMuteView = findViewById(R.id.call_activity_camera_mute_view);
        mCameraMuteView.setOnClickListener(v -> onCameraMuteClick());

        layoutParams = mCameraMuteView.getLayoutParams();
        layoutParams.height = BUTTON_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mCameraMuteView.getLayoutParams();
        marginLayoutParams.leftMargin = MARGIN_ACTION_BUTTON;
        marginLayoutParams.rightMargin = MARGIN_ACTION_BUTTON;
        marginLayoutParams.setMarginStart(MARGIN_ACTION_BUTTON);
        marginLayoutParams.setMarginEnd(MARGIN_ACTION_BUTTON);

        RoundedView cameraMuteRoundedView = findViewById(R.id.call_activity_camera_mute_background_view);
        cameraMuteRoundedView.setColor(Color.WHITE);

        mCameraMuteImageView = findViewById(R.id.call_activity_camera_mute_image_view);

        mSpeakerOnView = findViewById(R.id.call_activity_speaker_on_view);
        mSpeakerOnView.setOnClickListener(v -> onSpeakerOnClick());
        mSpeakerOnView.setOnLongClickListener(view -> {
            onSpeakerOnLongClick();
            return true;
        });

        layoutParams = mSpeakerOnView.getLayoutParams();
        layoutParams.height = BUTTON_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mSpeakerOnView.getLayoutParams();
        marginLayoutParams.leftMargin = MARGIN_ACTION_BUTTON;
        marginLayoutParams.setMarginStart(MARGIN_ACTION_BUTTON);

        RoundedView speakerOnBackgroundView = findViewById(R.id.call_activity_speaker_on_background_view);
        speakerOnBackgroundView.setColor(Color.WHITE);

        mSpeakerImageView = findViewById(R.id.call_activity_speaker_on_image_view);
        mSpeakerImageView.setColorFilter(Color.BLACK);

        View pauseView = findViewById(R.id.call_activity_pause_view);
        pauseView.setOnClickListener(v -> onPauseClick());

        layoutParams = pauseView.getLayoutParams();
        layoutParams.height = BUTTON_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) pauseView.getLayoutParams();
        marginLayoutParams.leftMargin = MARGIN_ACTION_BUTTON;
        marginLayoutParams.rightMargin = MARGIN_ACTION_BUTTON;
        marginLayoutParams.setMarginStart(MARGIN_ACTION_BUTTON);
        marginLayoutParams.setMarginEnd(MARGIN_ACTION_BUTTON);


        RoundedView pauseBackgroundView = findViewById(R.id.call_activity_pause_background_view);
        pauseBackgroundView.setColor(Color.WHITE);

        mPauseImageView = findViewById(R.id.call_activity_pause_image_view);

        mConversationView = findViewById(R.id.call_activity_conversation_view);
        mConversationView.setOnClickListener(v -> onConversationClick());

        layoutParams = mConversationView.getLayoutParams();
        layoutParams.height = BUTTON_HEIGHT;

        RoundedView conversationBackgroundView = findViewById(R.id.call_activity_conversation_background_view);
        conversationBackgroundView.setColor(Color.WHITE);

        mStreamingAudioView = findViewById(R.id.call_activity_streaming_audio_view);
        mStreamingAudioView.setOnClickListener(v -> onStreamingAudioClick());

        layoutParams = mStreamingAudioView.getLayoutParams();
        layoutParams.height = BUTTON_HEIGHT;

        RoundedView streamingAudioBackgroundView = findViewById(R.id.call_activity_streaming_audion_background_view);
        streamingAudioBackgroundView.setColor(Color.WHITE);

        mInvitationView = findViewById(R.id.call_activity_invitation_view);
        mInvitationView.setOnClickListener(v -> onInvitationClick());

        layoutParams = mInvitationView.getLayoutParams();
        layoutParams.height = BUTTON_HEIGHT;

        RoundedView invitationBackgroundView = findViewById(R.id.call_activity_invitation_background_view);
        invitationBackgroundView.setColor(Color.WHITE);

        mCertifyRelationView = findViewById(R.id.call_activity_certify_view);
        mCertifyRelationView.setOnClickListener(v -> onCertifyRelationClick());

        layoutParams = mCertifyRelationView.getLayoutParams();
        layoutParams.height = BUTTON_HEIGHT;

        mCameraControlView = findViewById(R.id.call_activity_camera_control_view);
        mCameraControlView.setOnClickListener(v -> onCameraControlClick());

        layoutParams = mCameraControlView.getLayoutParams();
        layoutParams.height = BUTTON_HEIGHT;

        RoundedView cameraControlBackgroundView = findViewById(R.id.call_activity_camera_control_background_view);
        cameraControlBackgroundView.setColor(Color.WHITE);

        mCameraControlImageView = findViewById(R.id.call_activity_camera_control_image_view);
        mCameraControlImageView.setColorFilter(Color.BLACK);

        mCertifyRelationView = findViewById(R.id.call_activity_certify_view);
        mCertifyRelationView.setOnClickListener(v -> onCertifyRelationClick());

        layoutParams = mCertifyRelationView.getLayoutParams();
        layoutParams.height = BUTTON_HEIGHT;

        RoundedView certifyRelationBackgroundView = findViewById(R.id.call_activity_certify_background_view);
        certifyRelationBackgroundView.setColor(Color.WHITE);

        View hangupView = findViewById(R.id.call_activity_hangup_view);
        hangupView.setOnClickListener(new HangupListener());

        layoutParams = hangupView.getLayoutParams();
        layoutParams.height = BUTTON_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) hangupView.getLayoutParams();
        marginLayoutParams.rightMargin = MARGIN_ACTION_BUTTON;
        marginLayoutParams.setMarginEnd(MARGIN_ACTION_BUTTON);

        View hangupRoundedView = findViewById(R.id.call_activity_hangup_rounded_view);

        radius = BUTTON_HEIGHT * Resources.getSystem().getDisplayMetrics().density;
        outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable hangupViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        hangupViewBackground.getPaint().setColor(Design.BUTTON_RED_COLOR);
        hangupRoundedView.setBackground(hangupViewBackground);
    }

    private void onPauseClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPauseClick");
        }

        mCallMenuListener.onCallPause();
    }

    private void onConversationClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onInvitationClick");
        }

        if (mIsInCall && !mIsCertifyRunning) {
            mCallMenuListener.onOpenConversation();
        }
    }

    private void onStreamingAudioClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onStreamingAudioClick");
        }

        if (mIsInCall && !mIsCertifyRunning) {
            mCallMenuListener.onSelectStreamingAudio();
        }
    }


    private void onInvitationClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onInvitationClick");
        }

        if (mIsInCall && !mIsCertifyRunning) {
            mCallMenuListener.onShareInvitation();
        }
    }

    private void onMicroMuteClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onMicroMuteClick");
        }

        if (mIsInCall) {
            mCallMenuListener.onMicroMute();
        }
    }

    private void onCameraMuteClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCameraMuteClick");
        }

        if (mIsInCall && !mIsCertifyRunning) {
            mCallMenuListener.onCameraMute();
        }
    }

    private void onSpeakerOnClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSpeakerOnClick");
        }

        mCallMenuListener.onSpeakerOn();
    }

    private void onSpeakerOnLongClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSpeakerOnLongClick");
        }

        mCallMenuListener.onSpeakerLongClick();
    }

    private void onCameraControlClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCameraControlClick");
        }

        if (mIsInCall && !mIsCertifyRunning) {
            mCallMenuListener.onControlCamera();
        }
    }

    private void onHangupClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onHangupClick");
        }

        mCallMenuListener.onHangup();
    }

    private void onCertifyRelationClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCertifyRelationClick");
        }

        if (mIsInCall && !mIsCertifyRunning) {
            mCallMenuListener.onCertifyRelation();
        }
    }

    private void updateSourceAudioIcon() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateSourceAudioIcon");
        }

        if (mAudioDevice == null) {
            return;
        }

        switch (mAudioDevice) {
            case SPEAKER_PHONE :
                mSpeakerImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.loud_speaker_action_call_on, null));
                break;

            case WIRED_HEADSET :
                mSpeakerImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.audio_headphone_icon, null));
                break;

            case EARPIECE :
                mSpeakerImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.audio_phonespeaker_icon, null));
                break;

            case BLUETOOTH :
                mSpeakerImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.audio_bluetooth_icon, null));
                break;

            case NONE:
                mSpeakerImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.loud_speaker_action_call_off, null));
                break;
        }
    }
}
