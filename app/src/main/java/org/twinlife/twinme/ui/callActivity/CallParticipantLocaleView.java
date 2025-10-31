/*
 *  Copyright (c) 2022 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.callActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.calls.CallParticipant;
import org.twinlife.twinme.calls.CallService;
import org.twinlife.twinme.calls.CallState;
import org.twinlife.twinme.calls.CallStatus;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.RoundedView;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

class CallParticipantLocaleView extends AbstractCallParticipantView {

    private String mName;
    private Bitmap mAvatar;

    private boolean mIsCameraMute = true;
    private boolean mIsMicroMute = false;

    public CallParticipantLocaleView(Context context) {

        super(context);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            View view = inflater.inflate(R.layout.call_participant_view, (ViewGroup) getParent());
            addView(view);
        }

        initViews();
    }

    public CallParticipantLocaleView(Context context, AttributeSet attrs) {

        super(context, attrs);
    }

    public CallParticipantLocaleView(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs, defStyle);
    }

    protected String getName() {

        return mName;
    }

    public Bitmap getAvatar() {

        return mAvatar;
    }

    protected int getParticipantId() {

        return -1;
    }

    @Override
    protected boolean isVideoInFitMode() {

        return false;
    }

    @Override
    public void minZoom() {

    }

    protected void scaleVideo(float scaleFactor, boolean reset) {

        if (mCallParticipantViewAspect == CallParticipantViewAspect.FULLSCREEN || (mNumberParticipants == 2 && mCallParticipantViewMode == CallParticipantViewMode.SMALL_REMOTE_VIDEO) || reset) {
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
        }
    }

    public void setName(String name) {

        mName = name;
    }

    public void setCameraMute(boolean isCameraMute) {

        mIsCameraMute = isCameraMute;
    }

    public void setMicroMute(boolean isMicroMute) {

        mIsMicroMute = isMicroMute;
    }

    public void setAvatar(Bitmap avatar) {

        mAvatar = avatar;
    }

    protected void initViews() {

        setBackgroundColor(Color.TRANSPARENT);

        mBackgroundView = findViewById(R.id.call_participant_background_view);

        mAvatarView = findViewById(R.id.call_participant_avatar_layout);

        mNameView = findViewById(R.id.call_participant_name_view);

        ViewGroup.LayoutParams layoutParams = mNameView.getLayoutParams();
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
        mInfoView.setVisibility(GONE);

        layoutParams = mInfoView.getLayoutParams();
        layoutParams.height = ICON_HEIGHT;

        marginLayoutParams = (MarginLayoutParams) mInfoView.getLayoutParams();
        marginLayoutParams.leftMargin = ICON_MARGIN;
        marginLayoutParams.topMargin = ICON_MARGIN;
        marginLayoutParams.setMarginStart(ICON_MARGIN);

        mInfoView.setOnClickListener(v -> mOnCallParticipantClickListener.onInfoTap());

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
    }

    protected void updateViews() {

        if (!(mCallStatus != null && mCallStatus.isOnHold()) &&
                ((CallStatus.isOutgoing(mCallStatus) || (CallStatus.isActive(mCallStatus))) && mIsVideoCall && mNumberParticipants == 2)
                || mNumberParticipants > 2) {
            setVisibility(VISIBLE);
        } else {
            setVisibility(GONE);
        }

        mNameTextView.setText(getName());

        if (mIsCameraMute || CallStatus.isTerminated(mCallStatus)) {
            mRemoteRenderLayout.setVisibility(GONE);
            mAvatarView.setVisibility(VISIBLE);
            mAvatarView.setImageBitmap(mAvatar);
            mSwitchCameraView.setVisibility(GONE);
        } else {
            mRemoteRenderLayout.setVisibility(VISIBLE);
            mAvatarView.setVisibility(GONE);
            mSwitchCameraView.setVisibility(VISIBLE);
            connectVideo();
        }

        if (mIsMicroMute) {
            mMuteMicroView.setVisibility(VISIBLE);
        } else {
            mMuteMicroView.setVisibility(GONE);
        }

        if (mCallStatus != null && mCallStatus.isOnHold()) {
            mPauseView.setVisibility(VISIBLE);
            mOverlayView.setVisibility(VISIBLE);
        } else {
            mPauseView.setVisibility(GONE);
            mOverlayView.setVisibility(GONE);
        }

        MarginLayoutParams marginLayoutParams = (MarginLayoutParams) mSwitchCameraView.getLayoutParams();
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
    }

    @Override
    protected CallParticipant getCallParticipant() {

        return null;
    }

    protected void connectVideo() {

        final CallState callState = CallService.getState();
        if (callState == null) {
            return;
        }

        SurfaceViewRenderer localRenderer = callState.getLocalRenderer();
        if (localRenderer != null) {
            localRenderer.setMirror(callState.isFrontCamera());
            ViewParent parent = localRenderer.getParent();
            if (parent != mRemoteRenderLayout) {
                if (parent != null) {
                    ((ViewGroup) parent).removeView(localRenderer);
                }

                if (mNumberParticipants == 2 && mCallParticipantViewMode == CallParticipantViewMode.SMALL_LOCALE_VIDEO && CallStatus.isActive(mCallStatus)) {

                    float widthPercent = ((mWidth - (DESIGN_STROKE_WIDTH * 2)) / mWidth) * 100;
                    float heightPercent = ((mHeight - (DESIGN_STROKE_WIDTH * 2)) / mHeight) * 100;

                    float xPercent = (100 - widthPercent) * 0.5f;
                    float yPercent = (100 - heightPercent) * 0.5f;

                    mRemoteRenderLayout.setPosition((int) xPercent, (int) yPercent, (int) widthPercent, (int) heightPercent);
                } else {
                    mRemoteRenderLayout.setPosition(0, 0, 100, 100);
                }

                localRenderer.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
                mRemoteRenderLayout.addView(localRenderer);

                localRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);

                localRenderer.setEnableHardwareScaler(true);
                localRenderer.setMirror(callState.isFrontCamera());

                bringVideoToFront();

                localRenderer.requestLayout();

                Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(() -> localRenderer.setBackgroundColor(Color.TRANSPARENT), 500);
            } else {
                bringVideoToFront();
            }
        }
    }

    @Override
    protected void bringVideoToFront() {

        final CallState callState = CallService.getState();
        if (callState == null) {
            return;
        }

        SurfaceViewRenderer remoteRenderer = callState.getLocalRenderer();
        if (remoteRenderer != null) {
            if (((mNumberParticipants == 2 && !isMainParticipant()) || mCallParticipantViewAspect == CallParticipantViewAspect.FULLSCREEN) && mIsVideoCall) {
                remoteRenderer.setZOrderMediaOverlay(true);
            } else {
                remoteRenderer.setZOrderMediaOverlay(false);
            }
        }
    }

    @Override
    protected boolean isRemoteParticipant() {

        return false;
    }

    @Override
    protected boolean isScreenSharing() {

        return false;
    }

    @Override
    protected boolean isRemoteCameraControl() {

        return false;
    }

    @Override
    protected boolean isCameraMute() {

        return mIsCameraMute;
    }

    @Override
    protected boolean isMessageSupported() {

        return false;
    }

    @Override
    protected boolean isRemoteCameraControlSupported() {

        return false;
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {

        boolean drawBorder = false;
        int radius = CONTAINER_RADIUS;
        if (mNumberParticipants == 2 && mCallParticipantViewMode == CallParticipantViewMode.SMALL_LOCALE_VIDEO && CallStatus.isActive(mCallStatus)) {
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