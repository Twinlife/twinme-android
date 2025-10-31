/*
 *  Copyright (c) 2020-2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowInsets;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.percentlayout.widget.PercentRelativeLayout;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.calls.CallService;
import org.twinlife.twinme.calls.CallState;
import org.twinlife.twinme.skin.CircularImageDescriptor;
import org.twinlife.twinme.skin.Design;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

@SuppressWarnings("deprecation")
public class CallFloatingView extends PercentRelativeLayout implements View.OnTouchListener {

    private static final float DESIGN_INSET = 40f;

    class GestureTap extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDoubleTap(@NonNull MotionEvent e) {

            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {

            mOnInCallClickListener.onInCallClick();
            return true;
        }
    }

    private OnInCallClickListener mOnInCallClickListener;

    public interface OnInCallClickListener {
        void onInCallClick();
    }

    private CircularImageView mAvatarView;
    private PercentFrameLayout mRemoteRenderLayout;
    private View mBackgroundView;

    private GestureDetector mTapDetector;

    private InCallInfo mInCallInfo;

    private float mDeltaX;
    private float mDeltaY;

    public CallFloatingView(Context context) {

        super(context);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            View view = inflater.inflate(R.layout.call_floating_view, (ViewGroup) getParent());
            view.setLayoutParams(new PercentRelativeLayout.LayoutParams(PercentRelativeLayout.LayoutParams.MATCH_PARENT, PercentRelativeLayout.LayoutParams.MATCH_PARENT));
            addView(view);
        }

        setOnTouchListener(this);
        setBackgroundColor(Color.TRANSPARENT);

        mTapDetector = new GestureDetector(context, new GestureTap());

        mBackgroundView = findViewById(R.id.call_floating_view_background_view);

        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.mutate();
        gradientDrawable.setShape(GradientDrawable.OVAL);
        gradientDrawable.setCornerRadii(new float[]{0, 0, 0, 0, 0, 0, 0, 0});
        gradientDrawable.setColor(Design.BLACK_COLOR);
        mBackgroundView.setBackground(gradientDrawable);

        mAvatarView = findViewById(R.id.call_floating_view_avatar);

        mRemoteRenderLayout = findViewById(R.id.call_floating_view_remote_video_layout);
        mRemoteRenderLayout.setBackgroundColor(Color.TRANSPARENT);
        mRemoteRenderLayout.setClipChildren(true);
    }

    public CallFloatingView(Context context, AttributeSet attrs) {

        super(context, attrs);
    }

    public CallFloatingView(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs, defStyle);
    }

    public void setInCallInfo(InCallInfo inCallInfo) {

        mInCallInfo = inCallInfo;

        if (mInCallInfo.isVideo()) {
            mRemoteRenderLayout.setVisibility(VISIBLE);
            mAvatarView.setVisibility(GONE);
            connectVideo();
        } else {
            displayAvatar();
        }
    }

    public void setOnInCallClickListener(OnInCallClickListener onInCallClickListener) {

        mOnInCallClickListener = onInCallClickListener;
    }

    public void moveToTopRight() {

        Handler handler = new Handler();
        Runnable runnable = () -> moveToCornerUnit(new Point(1, -1), this, false);
        handler.postDelayed(runnable, 100);
    }

    private void connectVideo() {

        CallState callState = CallService.getState();
        if (callState == null) {

            return;
        }

        SurfaceViewRenderer remoteRenderer = callState.getRemoteRenderer();
        if (remoteRenderer != null) {
            if (remoteRenderer.getParent() != null) {
                ((ViewGroup) remoteRenderer.getParent()).removeView(remoteRenderer);
            }
            mRemoteRenderLayout.setPosition(0, 0, 100, 100);

            remoteRenderer.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            mRemoteRenderLayout.addView(remoteRenderer);

            remoteRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
            remoteRenderer.setEnableHardwareScaler(true);
            remoteRenderer.setMirror(false);
            remoteRenderer.setZOrderMediaOverlay(false);
            remoteRenderer.requestLayout();
        } else {
            displayAvatar();
        }
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {

        if (mInCallInfo != null && mInCallInfo.isVideo()) {
            Path path = new Path();
            path.addRoundRect(new RectF(0, 0, getWidth(), getHeight()), getHeight() * 0.5f, getHeight() * 0.5f, Path.Direction.CW);
            canvas.clipPath(path);
        }

        super.dispatchDraw(canvas);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {

        mTapDetector.onTouchEvent(event);

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                mDeltaX = view.getX() - event.getRawX();
                mDeltaY = view.getY() - event.getRawY();
                break;

            case MotionEvent.ACTION_MOVE:
                float newX = event.getRawX() + mDeltaX;
                float newY = event.getRawY() + mDeltaY;

                view.animate()
                        .x(newX)
                        .y(newY)
                        .setDuration(0)
                        .start();
                mRemoteRenderLayout.requestLayout();
                break;

            case MotionEvent.ACTION_UP:
                moveToClosestCornerAnimated(view, true);
                break;

            default:
                return false;
        }

        return true;
    }

    private void moveToClosestCornerAnimated(View view, @SuppressWarnings("SameParameterValue") boolean animated) {

        Point closestCornerUnit = closestCornerUnit(view);
        moveToCornerUnit(closestCornerUnit, view, animated);
    }

    private void moveToCornerUnit(Point unit, View view, boolean animated) {

        ViewParent parent = view.getParent();
        if (!(parent instanceof View)) {
            return;
        }

        int parentTopInset = 0;
        int parentBottomInset = 0;

        float screenWidth = Design.DISPLAY_WIDTH;
        float screenHeight = ((View) parent).getHeight();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            WindowInsets insets = ((View) parent).getRootWindowInsets();
            if (insets != null) {
                parentTopInset = insets.getInsets(WindowInsets.Type.systemBars()).top;
                parentBottomInset = insets.getInsets(WindowInsets.Type.systemBars()).bottom;
            }
        }

        float xCenter = screenWidth / 2;
        float yCenter = screenHeight / 2;

        float xWidth = screenWidth - view.getWidth() - (DESIGN_INSET * Design.WIDTH_RATIO * 2.0f);
        float yHeight = screenHeight - view.getHeight() - (DESIGN_INSET * Design.HEIGHT_RATIO * 2.0f);

        Point cornerPoint = new Point((int) (xCenter + (xWidth / 2.0f * unit.x)), (int) (yCenter + (yHeight / 2.0f * unit.y)));

        float xd = cornerPoint.x - (view.getX() + (view.getWidth() / 2.0f));
        float yd = cornerPoint.y - (view.getY() + (view.getHeight() / 2.0f));

        float directDistance = (float) Math.sqrt(xd * xd + yd * yd);
        int duration = (int) ((directDistance / 720.f) * 1000);

        if (!animated) {
            duration = 0;
        }

        float newX = cornerPoint.x - (view.getWidth() / 2.0f);
        float newY = cornerPoint.y - (view.getHeight() / 2.0f);

        if (newY > yCenter) {
            newY -= parentBottomInset;
        } else {
            newY += parentTopInset;
        }

        view.animate()
                .x(newX)
                .y(newY)
                .setDuration(duration)
                .start();
        mRemoteRenderLayout.requestLayout();
    }

    private Point closestCornerUnit(View view) {

        float screenWidth = Design.DISPLAY_WIDTH;
        float screenHeight = ((View) view.getParent()).getHeight();

        float xCenter = screenWidth / 2;
        float yCenter = screenHeight / 2;

        float xCenterDist = view.getX() + (view.getWidth() / 2.0f) - xCenter;
        float yCenterDist = view.getY() + (view.getHeight() / 2.0f) - yCenter;

        return new Point((int) (xCenterDist / Math.abs(xCenterDist)), (int) (yCenterDist / Math.abs(yCenterDist)));
    }

    private void displayAvatar() {

        mRemoteRenderLayout.setVisibility(GONE);
        mBackgroundView.setVisibility(VISIBLE);
        mAvatarView.setVisibility(VISIBLE);
        mAvatarView.setImage(getContext(), null,
                new CircularImageDescriptor(mInCallInfo.getContactAvatar(), 0.5f, 0.5f, 0.5f));
    }
}
