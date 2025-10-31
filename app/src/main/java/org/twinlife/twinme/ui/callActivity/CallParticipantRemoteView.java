/*
 *  Copyright (c) 2022 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.callActivity;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.calls.CallParticipant;
import org.twinlife.twinme.calls.CallStatus;
import org.twinlife.twinme.models.Originator;
import org.twinlife.twinme.models.Zoomable;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.RoundedView;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

class CallParticipantRemoteView extends AbstractCallParticipantView  {

    private static final int ZOOM_MAX = 5;

    private static final int DESIGN_ACTION_LEFT_MARGIN = 16;
    private static final int DESIGN_ACTION_TOP_MARGIN = 16;
    private static final int DESIGN_ACTION_SIZE = 90;
    private static final int DESIGN_ACTION_IMAGE_SIZE = 45;
    private static final float DESIGN_CANCEL_VIEW_WIDTH = 74f;
    private static final float DESIGN_NO_AVATAR_HEIGHT = 180f;
    private static final int CANCEL_VIEW_WIDTH;
    private static final int ACTION_TOP_MARGIN;
    private static final int ACTION_LEFT_MARGIN;
    private static final int ACTION_SIZE;
    private static final int ACTION_IMAGE_SIZE;

    static {
        CANCEL_VIEW_WIDTH = (int) (DESIGN_CANCEL_VIEW_WIDTH * Design.WIDTH_RATIO);
        ACTION_TOP_MARGIN = (int) (DESIGN_ACTION_TOP_MARGIN * Design.WIDTH_RATIO);
        ACTION_LEFT_MARGIN = (int) (DESIGN_ACTION_LEFT_MARGIN * Design.WIDTH_RATIO);
        ACTION_SIZE = (int) (DESIGN_ACTION_SIZE * Design.HEIGHT_RATIO);
        ACTION_IMAGE_SIZE = (int) (DESIGN_ACTION_IMAGE_SIZE * Design.HEIGHT_RATIO);
    }

    private View mFullScreenView;
    private ImageView mFullScreenImageView;

    private CallParticipant mParticipant;
    private int mColor;

    private int mFitVideoWidth = 0;
    private int mFitVideoHeight = 0;
    private int mFillVideoWidth = 0;
    private int mFillVideoHeight = 0;

    private boolean mDeferredMinZoom = false;

    public CallParticipantRemoteView(Context context) {

        super(context);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            View view = inflater.inflate(R.layout.call_participant_view, (ViewGroup) getParent());
            addView(view);
        }

        initViews();
    }

    public CallParticipantRemoteView(Context context, AttributeSet attrs) {

        super(context, attrs);
    }

    public CallParticipantRemoteView(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs, defStyle);
    }

    public void setParticipant(CallParticipant participant) {

        mParticipant = participant;

        if (participant.isScreenSharing()) {
            mParticipant.setCallParticipantListener(p -> {
                if (mDeferredMinZoom) {
                    minZoom();
                }
            });
        }
    }

    public void setColor(int color) {

        mColor = color;
    }

    @Override
    protected CallParticipant getCallParticipant() {

        return mParticipant;
    }

    @Override
    protected String getName() {

        if (mCallStatus != null && CallStatus.isIncoming(mCallStatus)) {
            if (mParticipant.getCallConnection().getOriginator() != null && mParticipant.getCallConnection().getOriginator().getIdentityCapabilities().hasDiscreet()) {
                return getContext().getString(R.string.calls_fragment_incoming_call);
            }
        }

        return mParticipant.getName();
    }

    @Override
    protected int getParticipantId() {

        return mParticipant.getParticipantId();
    }

    @Override
    protected boolean isVideoInFitMode() {

        if (mFitVideoWidth == 0 && mFitVideoHeight == 0) {
            return false;
        }

        return mFitVideoWidth == getWidth() && mFitVideoHeight == getHeight();
    }

    private String getNoAvatarName() {

        if (mCallStatus != null && CallStatus.isIncoming(mCallStatus)) {
            if (mParticipant.getCallConnection().getOriginator() != null && mParticipant.getCallConnection().getOriginator().getIdentityCapabilities().hasDiscreet()) {
                String appName = getContext().getString(R.string.application_name);
                return appName.substring(0, 1).toUpperCase();
            }
        }

        String name = mParticipant.getName();
        if (name != null && !name.isEmpty()) {
            return name.substring(0, 1).toUpperCase();
        }

        return "";
    }

    private Bitmap getAvatar() {

        if (mCallStatus != null && CallStatus.isIncoming(mCallStatus)) {
            if (mParticipant.getCallConnection().getOriginator() != null && mParticipant.getCallConnection().getOriginator().getIdentityCapabilities().hasDiscreet()) {
                return BitmapFactory.decodeResource(getResources(), R.drawable.anonymous_avatar);
            }
        }

        return  mParticipant.getAvatar();
    }

    @Override
    public void minZoom() {

        if (mFillVideoWidth == 0 || mFillVideoHeight == 0 || mFitVideoWidth == 0 || mFitVideoHeight == 0) {
            if (!getVideoSize()) {
                mDeferredMinZoom = true;
                return;
            }
        }

        mDeferredMinZoom = false;

        mPeerVideoZoom = 1.0f;

        setX((mParentViewWidth * 0.5f) - (mFitVideoWidth * 0.5f));
        setY((mParentViewHeight * 0.5f) - (mFitVideoHeight * 0.5f));

        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.width = mFitVideoWidth;
        layoutParams.height = mFitVideoHeight;

        SurfaceViewRenderer remoteRenderer = mParticipant.getRemoteRenderer();
        if (remoteRenderer != null) {
            remoteRenderer.setScaleX(mPeerVideoZoom);
            remoteRenderer.setScaleY(mPeerVideoZoom);
        }

        mRemoteRenderLayout.requestLayout();
    }

    protected void scaleVideo(float scaleFactor, boolean reset) {

        if (mParticipant.getRemoteActiveCamera() > 0) {
            float zoomValue = mVideoZoom * scaleFactor;
            if (mVideoZoom < 15) {
                if (mVideoZoom < zoomValue) {
                    mVideoZoom += 1f;
                } else {
                    mVideoZoom -= 1f;
                }
            } else {
                mVideoZoom = zoomValue;
            }

            if (mVideoZoom >= 100) {
                mVideoZoom = 100.0f;
            } else if (mVideoZoom <= 1) {
                mVideoZoom = 1.0f;
            }

            mZoomTextView.setText(String.format("%.0f%%", mVideoZoom));
            mOnCallParticipantScaleListener.onZoomChanged(mVideoZoom);
            return;
        }

        if (mCallParticipantViewAspect == CallParticipantViewAspect.FULLSCREEN || (mNumberParticipants == 2 && mCallParticipantViewMode == CallParticipantViewMode.SMALL_LOCALE_VIDEO) || reset) {

            if (mFillVideoWidth == 0 || mFillVideoHeight == 0 || mFitVideoWidth == 0 || mFitVideoHeight == 0) {
                if (!getVideoSize()) {
                    return;
                }
            }

            float zoomValue = mPeerVideoZoom * scaleFactor;

            if (mPeerVideoZoom < zoomValue) {
                mPeerVideoZoom += 1f;
            } else {
                mPeerVideoZoom -= 1f;
            }

            if (mPeerVideoZoom >= ZOOM_MAX) {
                mPeerVideoZoom = ZOOM_MAX;
            } else if (mPeerVideoZoom <= 1) {
                mPeerVideoZoom = 1.0f;
            }

            int scaleWidth = (int) (getWidth() * mPeerVideoZoom);
            int scaleHeight = (int) (getHeight() * mPeerVideoZoom);

            if (mPeerVideoZoom == 1.0f) {
                scaleWidth = (int) (getWidth() * scaleFactor);
                scaleHeight = (int) (getHeight() * scaleFactor);
            }

            float maxWidth = mFillVideoWidth * ZOOM_MAX;
            float maxHeight = mFillVideoHeight * ZOOM_MAX;

            if (scaleWidth > mFillVideoWidth) {
                if (maxWidth < scaleWidth) {
                    mPeerVideoZoom = maxWidth / mFillVideoWidth;
                } else {
                    mPeerVideoZoom = scaleWidth / (float) mFillVideoWidth;
                }
                scaleWidth = mFillVideoWidth;
            } else if (scaleWidth < mFitVideoWidth) {
                mPeerVideoZoom = scaleWidth / (float) mFillVideoWidth;
                scaleWidth = mFitVideoWidth;
            }

            if (scaleHeight > mFillVideoHeight) {
                if (maxHeight < scaleHeight) {
                    mPeerVideoZoom = maxHeight /  mFillVideoHeight;
                } else {
                    mPeerVideoZoom = scaleHeight / (float) mFillVideoHeight;
                }
                scaleHeight = mFillVideoHeight;
            } else if (scaleHeight < mFitVideoHeight) {
                mPeerVideoZoom = scaleHeight /  (float) mFillVideoHeight;
                scaleHeight = mFitVideoHeight;
            }

            float zoomMin = (float) (mFitVideoHeight / mFillVideoHeight);
            if (mPeerVideoZoom >= ZOOM_MAX) {
                mPeerVideoZoom = ZOOM_MAX;
            } else if (mPeerVideoZoom <= zoomMin) {
                mPeerVideoZoom = zoomMin;
            }

            float zoomPercent = mPeerVideoZoom * 100;
            mZoomTextView.setText(String.format("%.0f%%", zoomPercent));

            if (reset) {
                scaleWidth = mFillVideoWidth;
                scaleHeight = mFillVideoHeight;
            }

            setX((mParentViewWidth * 0.5f) - (scaleWidth * 0.5f));
            setY((mParentViewHeight * 0.5f) - (scaleHeight * 0.5f));

            ViewGroup.LayoutParams layoutParams = getLayoutParams();
            layoutParams.width = scaleWidth;
            layoutParams.height = scaleHeight;

            SurfaceViewRenderer remoteRenderer = mParticipant.getRemoteRenderer();
            if (remoteRenderer != null) {
                remoteRenderer.setScaleX(mPeerVideoZoom > 1 ? mPeerVideoZoom : 1);
                remoteRenderer.setScaleY(mPeerVideoZoom > 1 ? mPeerVideoZoom : 1);
            }

            mRemoteRenderLayout.requestLayout();

            if (mNumberParticipants == 2) {
                if (reset) {
                    mInitInFitMode = false;
                } else {
                    mInitInFitMode = isVideoInFitMode();
                }
                mOnCallParticipantScaleListener.onSaveFitMode(mInitInFitMode);
            }
        }
    }

    protected void initViews() {

        setBackgroundColor(Color.TRANSPARENT);

        mBackgroundView = findViewById(R.id.call_participant_background_view);

        mAvatarView = findViewById(R.id.call_participant_avatar_layout);

        mNoAvatarContainerView = findViewById(R.id.call_participant_no_avatar_container_view);

        ViewGroup.LayoutParams layoutParams = mNoAvatarContainerView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_NO_AVATAR_HEIGHT * Design.HEIGHT_RATIO);

        mNoAvatarTextView = findViewById(R.id.call_participant_no_avatar_text_view);
        Design.updateTextFont(mNoAvatarTextView, Design.FONT_BOLD68);
        mNoAvatarTextView.setTextColor(Color.WHITE);

        mNoAvatarView = findViewById(R.id.call_participant_no_avatar_view);

        mNameView = findViewById(R.id.call_participant_name_view);

        layoutParams = mNameView.getLayoutParams();
        layoutParams.height = NAME_VIEW_HEIGHT;

        float radius = CONTAINER_RADIUS;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable nameViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        nameViewBackground.getPaint().setColor(Color.BLACK);
        mNameView.setBackground(nameViewBackground);

        MarginLayoutParams marginLayoutParams = (MarginLayoutParams) mNameView.getLayoutParams();
        marginLayoutParams.leftMargin = NAME_VIEW_MARGIN;
        marginLayoutParams.rightMargin = NAME_VIEW_MARGIN;
        marginLayoutParams.bottomMargin = NAME_VIEW_MARGIN;
        marginLayoutParams.setMarginStart(NAME_VIEW_MARGIN);
        marginLayoutParams.setMarginEnd(NAME_VIEW_MARGIN);

        mNameTextView = findViewById(R.id.call_participant_name_text_view);
        Design.updateTextFont(mNameTextView, Design.FONT_REGULAR28);
        mNameTextView.setTextColor(Color.WHITE);

        marginLayoutParams = (MarginLayoutParams) mNameTextView.getLayoutParams();
        marginLayoutParams.leftMargin = NAME_TEXT_VIEW_MARGIN;
        marginLayoutParams.rightMargin = NAME_TEXT_VIEW_MARGIN;
        marginLayoutParams.setMarginStart(NAME_TEXT_VIEW_MARGIN);
        marginLayoutParams.setMarginEnd(NAME_TEXT_VIEW_MARGIN);

        mRemoteRenderLayout = findViewById(R.id.call_participant_video_layout);
        mRemoteRenderLayout.setClipChildren(true);

        mMuteMicroView = findViewById(R.id.call_participant_audio_mute_layout);

        layoutParams = mMuteMicroView.getLayoutParams();
        layoutParams.height = ICON_HEIGHT;

        marginLayoutParams = (MarginLayoutParams) mMuteMicroView.getLayoutParams();
        marginLayoutParams.rightMargin = ICON_MARGIN;
        marginLayoutParams.topMargin = ICON_MARGIN;
        marginLayoutParams.setMarginEnd(ICON_MARGIN);

        mPauseView = findViewById(R.id.call_participant_pause_layout);

        layoutParams = mPauseView.getLayoutParams();
        layoutParams.height = ICON_HEIGHT;

        marginLayoutParams = (MarginLayoutParams) mPauseView.getLayoutParams();
        marginLayoutParams.rightMargin = ICON_MARGIN;
        marginLayoutParams.topMargin = ICON_MARGIN;
        marginLayoutParams.setMarginEnd(ICON_MARGIN);

        mInfoView = findViewById(R.id.call_participant_info_layout);
        mInfoView.setColorFilter(Design.DELETE_COLOR_RED);
        mInfoView.setOnClickListener(v -> mOnCallParticipantClickListener.onInfoTap());
        mInfoView.setVisibility(GONE);

        layoutParams = mInfoView.getLayoutParams();
        layoutParams.height = ICON_HEIGHT;

        marginLayoutParams = (MarginLayoutParams) mInfoView.getLayoutParams();
        marginLayoutParams.leftMargin = ICON_MARGIN;
        marginLayoutParams.topMargin = ICON_MARGIN;
        marginLayoutParams.setMarginStart(ICON_MARGIN);

        mSwitchCameraView = findViewById(R.id.call_participant_switch_camera_layout);
        mSwitchCameraView.setOnClickListener(v -> mOnCallParticipantClickListener.onSwitchCameraTap());

        layoutParams = mSwitchCameraView.getLayoutParams();
        layoutParams.height = SWITCH_CAMERA_HEIGHT;

        RoundedView switchCameraBackgroundView = findViewById(R.id.call_participant_switch_camera_background_view);
        switchCameraBackgroundView.setColor(Color.WHITE);

        layoutParams = switchCameraBackgroundView.getLayoutParams();
        layoutParams.height = ICON_HEIGHT;

        mSwitchCameraImageView = findViewById(R.id.call_participant_switch_camera_image_view);

        layoutParams = mSwitchCameraImageView.getLayoutParams();
        layoutParams.height = SWITCH_CAMERA_IMAGE_HEIGHT;

        mCancelView = findViewById(R.id.call_participant_cancel_view);
        mCancelView.setOnClickListener(v -> mOnCallParticipantClickListener.onCancelTap());

        layoutParams = mCancelView.getLayoutParams();
        layoutParams.height = NAME_VIEW_HEIGHT;
        layoutParams.width = CANCEL_VIEW_WIDTH;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mCancelView.getLayoutParams();
        marginLayoutParams.leftMargin = NAME_VIEW_MARGIN;
        marginLayoutParams.rightMargin = NAME_VIEW_MARGIN;
        marginLayoutParams.bottomMargin = NAME_VIEW_MARGIN;
        marginLayoutParams.setMarginStart(NAME_VIEW_MARGIN);
        marginLayoutParams.setMarginEnd(NAME_VIEW_MARGIN);

        View hangupRoundedView = findViewById(R.id.call_participant_cancel_rounded_view);

        radius = NAME_VIEW_HEIGHT * Resources.getSystem().getDisplayMetrics().density;
        outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable hangupViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        hangupViewBackground.getPaint().setColor(Design.BUTTON_RED_COLOR);
        hangupRoundedView.setBackground(hangupViewBackground);

        mOverlayView = findViewById(R.id.call_participant_overlay_view);
        mOverlayView.setBackgroundColor(Design.OVERLAY_VIEW_COLOR);

        mZoomView = findViewById(R.id.call_participant_zoom_view);
        mZoomView.setVisibility(View.GONE);

        layoutParams = mZoomView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_ZOOM_VIEW_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (MarginLayoutParams) mZoomView.getLayoutParams();
        marginLayoutParams.bottomMargin = (int) (DESIGN_ZOOM_VIEW_MARGIN * Design.HEIGHT_RATIO);

        RoundedView roundedView = findViewById(R.id.call_participant_zoom_rounded_view);
        roundedView.setColor(Color.BLACK);
        roundedView.setAlpha(0.5f);

        mZoomTextView = findViewById(R.id.call_participant_zoom_text_view);
        Design.updateTextFont(mZoomTextView, Design.FONT_BOLD44);
        mZoomTextView.setTextColor(Design.ZOOM_COLOR);

        mFullScreenView = findViewById(R.id.call_participant_fullscreen_view);
        mFullScreenView.setOnClickListener(v -> onFullScreenClick());

        layoutParams = mFullScreenView.getLayoutParams();
        layoutParams.width = ACTION_SIZE;
        layoutParams.height = ACTION_SIZE;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mFullScreenView.getLayoutParams();
        marginLayoutParams.leftMargin = ACTION_LEFT_MARGIN;
        marginLayoutParams.topMargin = ACTION_TOP_MARGIN;

        RoundedView fullScreenRoundedView = findViewById(R.id.call_participant_fullscreen_background_view);
        fullScreenRoundedView.setColor(Color.WHITE);

        mFullScreenImageView = findViewById(R.id.call_participant_map_fullscreen_image_view);

        layoutParams = mFullScreenImageView.getLayoutParams();
        layoutParams.width = ACTION_IMAGE_SIZE;
        layoutParams.height = ACTION_IMAGE_SIZE;
        mFullScreenImageView.setLayoutParams(layoutParams);
    }

    protected void updateViews() {

        final String name = getName();
        if (name != null) {
            mNameTextView.setText(name);
        }

        if (mHideName || (mNumberParticipants == 2 && !mIsCallReceiver) || name == null) {
            mNameView.setVisibility(GONE);
        } else {
            mNameView.setVisibility(VISIBLE);
        }

        if (CallStatus.isOutgoing(mCallStatus) && mIsVideoCall && mNumberParticipants == 2) {
            setVisibility(GONE);
        } else {
            setVisibility(VISIBLE);
        }

        if (mParticipant.isCameraMute() || (mIsVideoCall && !CallStatus.isActive(mCallStatus)) || CallStatus.isTerminated(mCallStatus)) {
            boolean callReceiver = mParticipant.getCallConnection().getOriginator() != null && mParticipant.getCallConnection().getOriginator().getType() == Originator.Type.CALL_RECEIVER;
            mRemoteRenderLayout.setVisibility(GONE);
            mAvatarView.setVisibility(VISIBLE);
            mSwitchCameraView.setVisibility(GONE);
            if (CallStatus.isIncoming(mCallStatus) && mParticipant.getGroupAvatar() != null) {
                // Incoming group call => display the group's avatar instead of the calling member's.
                mAvatarView.setImageBitmap(mParticipant.getGroupAvatar());
                mAvatarView.setVisibility(VISIBLE);
                mNoAvatarContainerView.setVisibility(GONE);
            } else if (getAvatar() != null && (!callReceiver || !CallStatus.isActive(mCallStatus))) {
                mAvatarView.setImageBitmap(getAvatar());
                mAvatarView.setVisibility(VISIBLE);
                mNoAvatarContainerView.setVisibility(GONE);
            } else {
                mAvatarView.setVisibility(GONE);
                mNoAvatarContainerView.setVisibility(VISIBLE);

                if (mColor != -1) {
                    GradientDrawable noAvatarGradientDrawable = new GradientDrawable();
                    noAvatarGradientDrawable.mutate();
                    noAvatarGradientDrawable.setShape(GradientDrawable.OVAL);
                    noAvatarGradientDrawable.setCornerRadii(new float[]{0, 0, 0, 0, 0, 0, 0, 0});
                    noAvatarGradientDrawable.setColor(mColor);
                    mNoAvatarView.setBackground(noAvatarGradientDrawable);
                }

                mNoAvatarTextView.setText(getNoAvatarName());
            }
        } else {
            mRemoteRenderLayout.setVisibility(VISIBLE);
            mAvatarView.setVisibility(GONE);
            mNoAvatarContainerView.setVisibility(GONE);
            connectVideo();

            if (CallStatus.isActive(mCallStatus) && mParticipant.getRemoteActiveCamera() > 0) {
                mSwitchCameraView.setVisibility(VISIBLE);
            } else {
                mSwitchCameraView.setVisibility(GONE);
            }
        }

        if (mParticipant.isAudioMute() && CallStatus.isActive(mParticipant.getCallConnection().getStatus())) {
            mMuteMicroView.setVisibility(VISIBLE);
        } else {
            mMuteMicroView.setVisibility(GONE);
        }

        if ((mCallStatus != null &&  mCallStatus.isOnHold()) || mParticipant.getStatus().isOnHold()) {
            mPauseView.setVisibility(VISIBLE);
            mOverlayView.setVisibility(VISIBLE);
        } else {
            mPauseView.setVisibility(GONE);
            mOverlayView.setVisibility(GONE);
        }

        Boolean groupSupported = mParticipant.isGroupSupported();
        if (groupSupported != null && !groupSupported && mNumberParticipants > 2 && CallStatus.isActive(mParticipant.getCallConnection().getStatus()))  {
            mInfoView.setVisibility(VISIBLE);
        } else {
            mInfoView.setVisibility(GONE);
        }

        if (getCallParticipant().isScreenSharing() && CallStatus.isActive(mParticipant.getCallConnection().getStatus())) {
            mFullScreenView.setVisibility(VISIBLE);
        } else {
            mFullScreenView.setVisibility(GONE);
        }

        if (mCallParticipantViewAspect == CallParticipantViewAspect.FULLSCREEN) {
            mFullScreenImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.minimize_icon, null));
        } else {
            mFullScreenImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.fullscreen_icon, null));
        }

        MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mNameView.getLayoutParams();
        if (mNumberParticipants > 2 && CallStatus.isOutgoing(mParticipant.getCallConnection().getStatus())) {
            mCancelView.setVisibility(VISIBLE);
            marginLayoutParams.leftMargin = (NAME_VIEW_MARGIN * 2) + CANCEL_VIEW_WIDTH;
            marginLayoutParams.setMarginStart((NAME_VIEW_MARGIN * 2) + CANCEL_VIEW_WIDTH);
        } else {
            mCancelView.setVisibility(GONE);
            marginLayoutParams.leftMargin = NAME_VIEW_MARGIN;
            marginLayoutParams.setMarginStart(NAME_VIEW_MARGIN);
        }

        marginLayoutParams = (MarginLayoutParams) mSwitchCameraView.getLayoutParams();
        int marginBottom = marginLayoutParams.bottomMargin;
        if (mNameView.getVisibility() == VISIBLE && mSwitchCameraView.getVisibility() == VISIBLE) {
            if (mNameView.getX() < (mSwitchCameraView.getX() * 2 + mSwitchCameraView.getWidth())) {
                marginLayoutParams.bottomMargin = SWITCH_CAMERA_HEIGHT;
            } else {
                marginLayoutParams.bottomMargin = 0;
            }
        } else {
            marginLayoutParams.bottomMargin = 0;
        }

        if (marginBottom != marginLayoutParams.bottomMargin) {
            mSwitchCameraView.setLayoutParams(marginLayoutParams);
            mSwitchCameraView.requestLayout();
        }

        if (mInitInFitMode && mNumberParticipants == 2 && mIsVideoCall && !mIsVideoInitialized && CallStatus.isActive(mCallStatus)) {
            if (mFitVideoWidth == 0 || mFitVideoHeight == 0) {
                getVideoSize();
            }

            setX((mParentViewWidth * 0.5f) - (mFitVideoWidth * 0.5f));
            setY((mParentViewHeight * 0.5f) - (mFitVideoHeight * 0.5f));

            ViewGroup.LayoutParams layoutParams = getLayoutParams();
            layoutParams.width = mFitVideoWidth;
            layoutParams.height = mFitVideoHeight;

            mRemoteRenderLayout.requestLayout();
        }

        if (mInitInFitMode && isVideoInFitMode() && mNumberParticipants == 2) {
            mIsVideoInitialized = true;
        }
    }

    protected void connectVideo() {

        if (mParticipant != null) {
            getVideoSize();

            if (mDeferredMinZoom) {
                minZoom();
            }

            SurfaceViewRenderer remoteRenderer = mParticipant.getRemoteRenderer();
            if (remoteRenderer != null) {
                if (remoteRenderer.getParent() != null) {
                    ((ViewGroup) remoteRenderer.getParent()).removeView(remoteRenderer);
                }
                mRemoteRenderLayout.setPosition(0, 0, 100, 100);

                remoteRenderer.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
                remoteRenderer.setClipToOutline(true);
                mRemoteRenderLayout.addView(remoteRenderer);

                remoteRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
                remoteRenderer.setEnableHardwareScaler(true);

                remoteRenderer.setMirror(false);
                remoteRenderer.requestLayout();
            }

            bringVideoToFront();
        }
    }

    @Override
    protected void bringVideoToFront() {

        if (mParticipant != null) {
            SurfaceViewRenderer remoteRenderer = mParticipant.getRemoteRenderer();
            if (remoteRenderer != null) {
                if (((mNumberParticipants == 2 && !isMainParticipant()) || mCallParticipantViewAspect == CallParticipantViewAspect.FULLSCREEN) && mIsVideoCall) {
                    remoteRenderer.setZOrderMediaOverlay(true);
                } else {
                    remoteRenderer.setZOrderMediaOverlay(false);
                }
            }
        }
    }

    @Override
    protected boolean isRemoteParticipant() {

        return true;
    }

    @Override
    protected boolean isCameraMute() {

        return mParticipant.isCameraMute();
    }

    @Override
    protected boolean isScreenSharing() {

        return mParticipant.isScreenSharing();
    }

    @Override
    protected boolean isRemoteCameraControl() {

        return mParticipant.getRemoteActiveCamera() > 0 || mParticipant.getCallConnection().isRemoteControlGranted();
    }

    @Override
    protected boolean isMessageSupported() {

        if (mParticipant.isMessageSupported() != null) {
            return Boolean.TRUE.equals(mParticipant.isMessageSupported());
        }

        return false;
    }

    @Override
    protected boolean isRemoteCameraControlSupported() {

        return mParticipant.isZoomable() != Zoomable.NEVER;
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {

        if (mParticipant != null) {
            boolean drawBorder = false;
            int radius = CONTAINER_RADIUS;
            if (mNumberParticipants == 2 && mCallParticipantViewMode == CallParticipantViewMode.SMALL_REMOTE_VIDEO && CallStatus.isActive(mCallStatus)) {
                drawBorder = true;
                radius = SMALL_CONTAINER_RADIUS;
            }

            Path path = new Path();
            path.addRoundRect(new RectF(0, 0, getWidth(), getHeight()), radius, radius, Path.Direction.CW);
            canvas.clipPath(path);

            super.dispatchDraw(canvas);

            if (drawBorder) {
                mRemoteRenderLayout.setPosition(1, 1, 98, 98);
                Paint paint = new Paint();
                paint.setColor(DESIGN_BACKGROUND_COLOR);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeCap(Paint.Cap.ROUND);
                paint.setStrokeJoin(Paint.Join.ROUND);
                float strokeWidth =  0.05f * mWidth;
                if (strokeWidth > DESIGN_STROKE_WIDTH) {
                    strokeWidth = DESIGN_STROKE_WIDTH;
                }
                paint.setStrokeWidth(strokeWidth);
                paint.setAntiAlias(true);
                canvas.drawPath(path, paint);
            } else {
                mRemoteRenderLayout.setPosition(0, 0, 100, 100);
            }

            float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
            ShapeDrawable backgroundViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
            backgroundViewBackground.getPaint().setColor(DESIGN_BACKGROUND_COLOR);
            mBackgroundView.setBackground(backgroundViewBackground);

            mRemoteRenderLayout.requestLayout();
        }
    }

    private boolean getVideoSize() {

        if (getCallParticipant().getVideoWidth() > 0 && getCallParticipant().getVideoHeight() > 0) {
            mFillVideoWidth = Design.DISPLAY_WIDTH - (MARGIN_PARTICIPANT * 2);
            mFillVideoHeight = mParentViewHeight;
            if (mIsLandscape) {
                mFillVideoWidth =  mParentViewWidth;
            }

            if (mIsLandscape) {
                mFitVideoWidth = (getCallParticipant().getVideoWidth() * mFillVideoHeight) / getCallParticipant().getVideoHeight();
                mFitVideoHeight = mFillVideoHeight;
            } else {
                mFitVideoWidth = mFillVideoWidth;
                mFitVideoHeight = (getCallParticipant().getVideoHeight() * mFillVideoWidth) / getCallParticipant().getVideoWidth();
            }

            return true;
        }

        return false;
    }

    private void onFullScreenClick() {

        if (mCallParticipantViewAspect == CallParticipantViewAspect.FULLSCREEN) {
            mOnCallParticipantClickListener.onMinimizeTap();
        } else {
            mOnCallParticipantClickListener.onFullScreenTap();
        }
    }
}