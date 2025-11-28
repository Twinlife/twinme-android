/*
 *  Copyright (c) 2022 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.callActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.percentlayout.widget.PercentRelativeLayout;

import org.twinlife.twinme.calls.CallParticipant;
import org.twinlife.twinme.calls.CallStatus;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.PercentFrameLayout;

public abstract class AbstractCallParticipantView extends PercentRelativeLayout implements View.OnTouchListener {
    private static final String LOG_TAG = "AbstractCallParticip...";
    private static final boolean DEBUG = false;

    enum CallParticipantViewAspect {
        FIT,
        FULLSCREEN
    }

    enum CallParticipantViewMode {
        SMALL_LOCALE_VIDEO,
        SMALL_REMOTE_VIDEO,
        SPLIT_SCREEN
    }

    class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScaleBegin(@NonNull ScaleGestureDetector detector) {

            mZoomView.setVisibility(View.VISIBLE);
            mZoomView.bringToFront();

            PropertyValuesHolder propertyValuesHolderAlpha = PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1.0f);
            ObjectAnimator alphaViewAnimator = ObjectAnimator.ofPropertyValuesHolder(mZoomView, propertyValuesHolderAlpha);
            alphaViewAnimator.setDuration(300L);
            alphaViewAnimator.start();

            return true;
        }

        @Override
        public boolean onScale(@NonNull ScaleGestureDetector detector) {

            scaleVideo(detector.getScaleFactor(), false);
            return true;
        }

        @Override
        public void onScaleEnd(@NonNull ScaleGestureDetector detector) {
            super.onScaleEnd(detector);

            PropertyValuesHolder propertyValuesHolderAlpha = PropertyValuesHolder.ofFloat(View.ALPHA, 1.0f, 0.0f);
            ObjectAnimator alphaViewAnimator = ObjectAnimator.ofPropertyValuesHolder(mZoomView, propertyValuesHolderAlpha);
            alphaViewAnimator.setDuration(300L);
            alphaViewAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mZoomView.setVisibility(View.GONE);
                }
            });

            alphaViewAnimator.start();
        }
    }

    class GestureTap extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDoubleTap(@NonNull MotionEvent e) {

            if (mNumberParticipants == 2) {
                mInitInFitMode = false;
                mOnCallParticipantScaleListener.onSaveFitMode(mInitInFitMode);
            }

            mOnCallParticipantClickListener.onDoubleTap();
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {

            if (mNumberParticipants == 2) {
                mIsVideoInitialized = false;
            }

            mOnCallParticipantClickListener.onSimpleTap();
            return true;
        }
    }

    class SwipeGesture extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onFling(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {

            boolean isDownGesture = velocityY > 0;

            if (isDownGesture) {
                mOnCallParticipantClickListener.onSwipeDown();
            } else {
                mOnCallParticipantClickListener.onSwipeUp();
            }

            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }

    public interface OnCallParticipantClickListener {

        void onInfoTap();

        void onLocationTap();

        void onCancelTap();

        void onSimpleTap();

        void onDoubleTap();

        void onSwipeDown();

        void onSwipeUp();

        void onFullScreenTap();

        void onMinimizeTap();

        void onSwitchCameraTap();
    }

    public interface OnCallParticipantScaleListener {

        void onZoomChanged(float zoom);

        void onSaveFitMode(boolean fitMode);
    }

    protected static final int DESIGN_BACKGROUND_COLOR = Color.rgb(60, 60, 60);
    private static final int ANIMATON_DURATION = 500;
    private static final int DESIGN_MARGIN_PARTICIPANT = 34;
    protected static final int CONTAINER_RADIUS = 24;
    protected static final int SMALL_CONTAINER_RADIUS = 12;
    protected static final int DESIGN_MICRO_MUTE_HEIGHT = 52;
    protected static final int DESIGN_MICRO_MUTE_MARGIN = 10;
    protected static final int DESIGN_NAME_VIEW_MARGIN = 14;
    protected static final int DESIGN_NAME_VIEW_HEIGHT = 58;
    protected static final int DESIGN_NAME_TEXT_VIEW_MARGIN = 20;
    protected static final int DESIGN_THUMBNAIL_PARTICIPANT_WIDTH = 220;
    protected static final int DESIGN_THUMBNAIL_PARTICIPANT_HEIGHT = 280;
    protected static final int DESIGN_STROKE_WIDTH = 10;
    protected static final float DESIGN_ZOOM_VIEW_HEIGHT = 160f;
    protected static final float DESIGN_ZOOM_VIEW_MARGIN = 60f;
    protected static final float DESIGN_SWITCH_CAMERA_HEIGHT = 80f;
    protected static final float DESIGN_SWITCH_CAMERA_IMAGE_HEIGHT = 36f;

    protected static final int MARGIN_PARTICIPANT;
    protected static final int ICON_HEIGHT;
    protected static final int ICON_MARGIN;
    protected static final int NAME_VIEW_MARGIN;
    protected static final int NAME_VIEW_HEIGHT;
    protected static final int NAME_TEXT_VIEW_MARGIN;
    protected static final int THUMBNAIL_PARTICIPANT_WIDTH;
    protected static final int THUMBNAIL_PARTICIPANT_HEIGHT;
    protected static final int SWITCH_CAMERA_HEIGHT;
    protected static final int SWITCH_CAMERA_IMAGE_HEIGHT;

    static {
        MARGIN_PARTICIPANT = (int) (DESIGN_MARGIN_PARTICIPANT * Design.WIDTH_RATIO);
        ICON_HEIGHT = (int) (DESIGN_MICRO_MUTE_HEIGHT * Design.HEIGHT_RATIO);
        ICON_MARGIN = (int) (DESIGN_MICRO_MUTE_MARGIN * Design.HEIGHT_RATIO);
        NAME_VIEW_MARGIN = (int) (DESIGN_NAME_VIEW_MARGIN * Design.HEIGHT_RATIO);
        NAME_VIEW_HEIGHT = (int) (DESIGN_NAME_VIEW_HEIGHT * Design.HEIGHT_RATIO);
        NAME_TEXT_VIEW_MARGIN = (int) (DESIGN_NAME_TEXT_VIEW_MARGIN * Design.WIDTH_RATIO);
        THUMBNAIL_PARTICIPANT_WIDTH = (int) (DESIGN_THUMBNAIL_PARTICIPANT_WIDTH * Design.WIDTH_RATIO);
        THUMBNAIL_PARTICIPANT_HEIGHT = (int) (DESIGN_THUMBNAIL_PARTICIPANT_HEIGHT * Design.HEIGHT_RATIO);
        SWITCH_CAMERA_HEIGHT = (int) (DESIGN_SWITCH_CAMERA_HEIGHT * Design.HEIGHT_RATIO);
        SWITCH_CAMERA_IMAGE_HEIGHT = (int) (DESIGN_SWITCH_CAMERA_IMAGE_HEIGHT * Design.HEIGHT_RATIO);
    }

    protected ImageView mAvatarView;
    protected View mNoAvatarContainerView;
    protected View mBackgroundView;
    protected TextView mNoAvatarTextView;
    protected View mNoAvatarView;
    protected View mNameView;
    protected View mCancelView;
    protected TextView mNameTextView;
    protected PercentFrameLayout mRemoteRenderLayout;
    protected ImageView mMuteMicroView;
    protected ImageView mPauseView;
    protected ImageView mInfoView;
    protected View mSharedLocationView;
    protected View mOverlayView;
    protected View mZoomView;
    protected TextView mZoomTextView;
    protected View mSwitchCameraView;
    protected ImageView mSwitchCameraImageView;

    private GestureDetector mTapDetector;
    private GestureDetector mSwipeDetector;
    protected OnCallParticipantClickListener mOnCallParticipantClickListener;
    private ScaleGestureDetector mScaleDetector;
    protected OnCallParticipantScaleListener mOnCallParticipantScaleListener;

    protected int mParentViewWidth = 0;
    protected int mParentViewHeight = 0;
    protected int mNumberParticipants = 0;
    private int mPosition = 0;
    private float mX = 0;
    private float mY = 0;
    protected float mWidth = 0;
    protected float mHeight = 0;
    protected float mVideoZoom = 1.0f;
    protected float mPeerVideoZoom = 1.0f;
    protected CallStatus mCallStatus;
    protected boolean mIsVideoCall = false;
    protected boolean mIsCallReceiver = false;
    private boolean mIsMainParticipant = false;
    private boolean mIsDraggable = false;
    private boolean mMoved = false;
    protected boolean mHideName = false;
    protected boolean mIsLandscape = false;
    protected boolean mIsVideoInitialized = false;
    protected boolean mInitInFitMode = false;
    private float mDeltaX;
    private float mDeltaY;
    private Point mClosestCornerUnit = new Point();

    protected CallParticipantViewAspect mCallParticipantViewAspect;
    protected CallParticipantViewMode mCallParticipantViewMode;

    private float mMainParticipantWidth = 0;
    private float mMainParticipantHeight = 0;

    public AbstractCallParticipantView(Context context) {
        super(context);

        if (DEBUG) {
            Log.d(LOG_TAG, "create: " + context);
        }

        setOnTouchListener(this);
        mCallParticipantViewAspect = CallParticipantViewAspect.FIT;
        mTapDetector = new GestureDetector(context, new GestureTap());
        mSwipeDetector = new GestureDetector(context, new SwipeGesture());
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    public AbstractCallParticipantView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (DEBUG) {
            Log.d(LOG_TAG, "create: " + context + " attrs: " + attrs);
        }
    }

    public AbstractCallParticipantView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        if (DEBUG) {
            Log.d(LOG_TAG, "create: " + context + " attrs: " + attrs + " defStyle: " + defStyle);
        }
    }

    public void setOnCallParticipantClickListener(OnCallParticipantClickListener onCallParticipantClickListener) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setOnCallParticipantClickListener: " + onCallParticipantClickListener);
        }

        mOnCallParticipantClickListener = onCallParticipantClickListener;
    }

    public void setOnCallParticipantScaleListener(OnCallParticipantScaleListener onCallParticipantScaleListener) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setOnCallParticipantScaleListener: " + onCallParticipantScaleListener);
        }

        mOnCallParticipantScaleListener = onCallParticipantScaleListener;
    }

    public void setVideoZoom(float zoom) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setVideoZoom: " + zoom);
        }

        mVideoZoom = zoom;
    }

    public void setInitVideoInFitMode(boolean fitMode) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setInitVideoInFitMode: " + fitMode);
        }

        mInitInFitMode = fitMode;
    }

    public void updateCallParticipantViewAspect() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateCallParticipantViewAspect");
        }

        if (mCallParticipantViewAspect == CallParticipantViewAspect.FIT && (!isCameraMute() || isScreenSharing()))  {
            mCallParticipantViewAspect = CallParticipantViewAspect.FULLSCREEN;

            int x = MARGIN_PARTICIPANT;
            if (mIsLandscape) {
                x = 0;
            }

            animate().setDuration(ANIMATON_DURATION).x(x).y(0).setInterpolator(new AccelerateDecelerateInterpolator()).start();

            ViewGroup.LayoutParams layoutParams = getLayoutParams();

            if (mIsLandscape) {
                layoutParams.width =  mParentViewWidth;
                layoutParams.height = mParentViewHeight;
            } else {
                layoutParams.width =  mParentViewWidth - (MARGIN_PARTICIPANT * 2);
                layoutParams.height = mParentViewHeight;
            }
        } else {
            mCallParticipantViewAspect = CallParticipantViewAspect.FIT;

            animate().setDuration(ANIMATON_DURATION).x(mX).y(mY).setInterpolator(new AccelerateDecelerateInterpolator()).start();

            ViewGroup.LayoutParams layoutParams = getLayoutParams();
            layoutParams.width = (int) mWidth;
            layoutParams.height = (int) mHeight;
        }

        updateViews();
    }

    public CallParticipantViewAspect getCallParticipantViewAspect() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getCallParticipantViewAspect");
        }

        return mCallParticipantViewAspect;
    }

    public void setCallParticipantViewMode(CallParticipantViewMode callParticipantViewMode) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setCallParticipantViewMode");
        }

        if (mCallParticipantViewMode != callParticipantViewMode) {
            mCallParticipantViewMode = callParticipantViewMode;
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onTouch: " + view + " event: " + event);
        }

        mTapDetector.onTouchEvent(event);
        mSwipeDetector.onTouchEvent(event);
        if (!isCameraMute()) {
            mScaleDetector.onTouchEvent(event);
        }

        if (mIsDraggable) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mDeltaX = view.getX() - event.getRawX();
                    mDeltaY = view.getY() - event.getRawY();
                    break;

                case MotionEvent.ACTION_MOVE:
                    mMoved = true;
                    view.animate()
                            .x(event.getRawX() + mDeltaX)
                            .y(event.getRawY() + mDeltaY)
                            .setDuration(0)
                            .start();
                    break;

                case MotionEvent.ACTION_UP:
                    if (mMoved) {
                        mMoved = false;
                        moveToClosestCornerAnimated(true);
                    }
                    break;

                default:
                    return false;
            }
        }

        return true;
    }

    public void resetView() {
        if (DEBUG) {
            Log.d(LOG_TAG, "resetView");
        }

        if (isRemoteParticipant()) {
            mPeerVideoZoom = 1.0f;
            scaleVideo(1.0f, true);
        }
    }

    public void setPosition(boolean mainParticipant, int parentViewWidth, int parentViewHeight, int numberParticipants, int position, boolean hideName, CallStatus callStatus, boolean isVideoCall, boolean isCallReceiver, boolean isLandscape) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setPosition: " + mainParticipant + " parentViewWidth: " + parentViewWidth  + " parentViewHeight: " + parentViewHeight + " numberParticipants: " + numberParticipants + " position: " + position + " isLandscape: " + isLandscape);
        }

        boolean isOrientationChanged = mIsLandscape != isLandscape;

        mParentViewWidth = parentViewWidth;
        mParentViewHeight = parentViewHeight;

        mNumberParticipants = numberParticipants;
        mPosition = position;
        mCallStatus = callStatus;
        mIsVideoCall = isVideoCall;
        mIsCallReceiver = isCallReceiver;
        mIsLandscape = isLandscape;

        mMainParticipantWidth = getMainParticipantWidth();
        mMainParticipantHeight = getMainParticipantHeight();

        if (!CallStatus.isTerminated(mCallStatus)) {
            mIsMainParticipant = mainParticipant;
        }

        if (!isMainParticipant() && mNumberParticipants == 2) {
            bringToFront();
        }

        if (isMainParticipant()) {
            mWidth = mMainParticipantWidth;
            mHeight = mMainParticipantHeight;
            if (mIsLandscape) {
                if ((!isVideoCall || (!CallStatus.isActive(mCallStatus))) && mNumberParticipants == 2) {
                    mX = (parentViewWidth * 0.5f) - (mWidth * 0.5f);
                } else {
                    mX = 0;
                }
            } else {
                mX = MARGIN_PARTICIPANT;
            }

            if ((!mIsVideoCall || !CallStatus.isActive(mCallStatus)) && mNumberParticipants == 2) {
                mY = (mParentViewHeight * 0.5f) - (mHeight * 0.5f);
            } else {
                mY = 0;
            }
        } else {
            mWidth = getParticipantWidth();
            mHeight = getParticipantHeight();
            mX = getParticipantX();
            mY = getParticipantY();
        }

        if (!isVideoInFitMode() || mCallParticipantViewMode != CallParticipantViewMode.SMALL_LOCALE_VIDEO || mNumberParticipants > 2 ) {

            setX(mX);
            setY(mY);

            ViewGroup.LayoutParams layoutParams = getLayoutParams();
            layoutParams.width = (int) mWidth;
            layoutParams.height = (int) mHeight;
        }

        if (mNumberParticipants == 2 && CallStatus.isActive(mCallStatus) && mIsVideoCall && mCallParticipantViewMode != CallParticipantViewMode.SPLIT_SCREEN && !isMainParticipant()) {
            mIsDraggable = true;
        } else {
            mIsDraggable = false;
        }

        if (mIsDraggable && (mClosestCornerUnit.x != 0 || isOrientationChanged)) {
            if (mClosestCornerUnit.x == 0) {
                mClosestCornerUnit = closestCornerUnit();
            }
            moveToCornerUnit(mClosestCornerUnit, false);
        }

        if (hideName || (mNumberParticipants == 2 && !mIsCallReceiver)) {
            mNameView.setVisibility(GONE);
        } else {
            mNameView.setVisibility(VISIBLE);
        }
        mHideName = hideName;
        updateViews();
    }

    protected boolean isMainParticipant() {

        if (mIsVideoCall && mNumberParticipants == 2 && mCallParticipantViewMode != CallParticipantViewMode.SPLIT_SCREEN) {
            if (isRemoteParticipant() && mCallParticipantViewMode == CallParticipantViewMode.SMALL_LOCALE_VIDEO) {
                return true;
            } else if (!isRemoteParticipant() && mCallParticipantViewMode == CallParticipantViewMode.SMALL_REMOTE_VIDEO) {
                return true;
            }
            return false;
        } else if ((CallStatus.isTerminated(mCallStatus) || (mNumberParticipants == 2 && CallStatus.isActive(mCallStatus))) && isRemoteParticipant()) {
            return true;
        }

        return mIsMainParticipant;
    }

    private float getMainParticipantWidth() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getMainParticipantWidth");
        }

        if (mIsLandscape) {
            return getLandscapeMainParticipantWidth();
        }

        return getPortraitMainParticipantWidth();
    }

    private float getPortraitMainParticipantWidth() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getPortraitMainParticipantWidth");
        }

        if (mNumberParticipants != 2 && mNumberParticipants % 2 == 0) {
            return (mParentViewWidth - (MARGIN_PARTICIPANT * 3)) * 0.5f ;
        }

        return mParentViewWidth - (MARGIN_PARTICIPANT * 2);
    }

    private float getLandscapeMainParticipantWidth() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getLandscapeMainParticipantWidth");
        }

        if (mNumberParticipants == 2 && mIsVideoCall && CallStatus.isActive(mCallStatus) && mCallParticipantViewMode != CallParticipantViewMode.SPLIT_SCREEN) {
            return mParentViewWidth;
        } else if (mNumberParticipants == 2) {
            return (mParentViewWidth - MARGIN_PARTICIPANT) * 0.5f;
        } else if (mNumberParticipants == 3 || mNumberParticipants == 6) {
            return (mParentViewWidth - (MARGIN_PARTICIPANT * 2)) / 3.f;
        } else if (mNumberParticipants == 5) {
            return (mParentViewWidth - (MARGIN_PARTICIPANT * 2)) * 0.5f;
        } else if (mNumberParticipants == 4 || mNumberParticipants == 7 || mNumberParticipants == 8) {
            return (mParentViewWidth - (MARGIN_PARTICIPANT * 3)) * 0.25f;
        }

        return mParentViewWidth;
    }

    private float getMainParticipantHeight() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getMainParticipantHeight");
        }

        if (mIsLandscape) {
            return getLandscapeMainParticipantHeight();
        }

        return getPortraitMainParticipantHeight();
    }

    private float getPortraitMainParticipantHeight() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getPortraitMainParticipantHeight");
        }

        if (mNumberParticipants == 2 && mIsVideoCall && CallStatus.isActive(mCallStatus) && mCallParticipantViewMode != CallParticipantViewMode.SPLIT_SCREEN) {
            return mParentViewHeight;
        } else if (mNumberParticipants == 2 || mNumberParticipants == 4) {
            return (mParentViewHeight - MARGIN_PARTICIPANT) * 0.5f;
        } else if (mNumberParticipants == 3) {
            return (mParentViewHeight - MARGIN_PARTICIPANT) * 0.67f;
        } else if (mNumberParticipants == 5 || mNumberParticipants == 6) {
            return (mParentViewHeight - (MARGIN_PARTICIPANT * 3)) / 3f;
        }

        return (mParentViewHeight - (MARGIN_PARTICIPANT * 3)) / 4f;
    }

    private float getLandscapeMainParticipantHeight() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getLandscapeMainParticipantHeight");
        }

        if (mNumberParticipants == 6 ||mNumberParticipants == 8) {
            return (mParentViewHeight - MARGIN_PARTICIPANT) * 0.5f;
        }

        return mParentViewHeight;
    }

    private float getParticipantWidth() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getParticipantWidth");
        }

        if (mIsLandscape) {
            return getLandscapeParticipantWidth();
        }

        return getPortraitParticipantWidth();
    }

    private float getPortraitParticipantWidth() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getPortraitParticipantWidth");
        }

        if (mNumberParticipants == 2 && mIsVideoCall && CallStatus.isActive(mCallStatus) && mCallParticipantViewMode != CallParticipantViewMode.SPLIT_SCREEN) {
            return THUMBNAIL_PARTICIPANT_WIDTH;
        } else if (mNumberParticipants == 2) {
            return getMainParticipantWidth();
        }

        return (mParentViewWidth - (MARGIN_PARTICIPANT * 3)) * 0.5f;
    }

    private float getLandscapeParticipantWidth() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getLandscapeParticipantWidth");
        }

        if (mNumberParticipants == 2 && mIsVideoCall && CallStatus.isActive(mCallStatus) && mCallParticipantViewMode != CallParticipantViewMode.SPLIT_SCREEN) {
            return THUMBNAIL_PARTICIPANT_WIDTH;
        } else if (mNumberParticipants % 2 == 0 || mNumberParticipants == 3|| mNumberParticipants == 7) {
            return getMainParticipantWidth();
        } else {
            return (mParentViewWidth - mMainParticipantWidth - (MARGIN_PARTICIPANT * 2)) * 0.5f;
        }
    }

    private float getParticipantHeight() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getParticipantHeight");
        }

        if (mIsLandscape) {
            return getLandscapeParticipantHeight();
        }

        return getPortraitParticipantHeight();
    }

    private float getPortraitParticipantHeight() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getPortraitParticipantHeight");
        }

        if (mNumberParticipants == 2 && mIsVideoCall && CallStatus.isActive(mCallStatus) && mCallParticipantViewMode != CallParticipantViewMode.SPLIT_SCREEN) {
            return THUMBNAIL_PARTICIPANT_HEIGHT;
        } else if (mIsVideoCall && !CallStatus.isActive(mCallStatus) && mNumberParticipants == 2) {
            return mParentViewHeight;
        } else if (mNumberParticipants == 3) {
            return (mParentViewHeight - MARGIN_PARTICIPANT) * 0.33f;
        }

        return getMainParticipantHeight();
    }

    private float getLandscapeParticipantHeight() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getLandscapeParticipantHeight");
        }

        if (mNumberParticipants == 2 && mIsVideoCall && CallStatus.isActive(mCallStatus) && mCallParticipantViewMode != CallParticipantViewMode.SPLIT_SCREEN) {
            return THUMBNAIL_PARTICIPANT_HEIGHT;
        } else if (mIsVideoCall && !CallStatus.isActive(mCallStatus) && mNumberParticipants == 2) {
            return mParentViewHeight;
        } else if (mNumberParticipants > 4) {
            return (mParentViewHeight - MARGIN_PARTICIPANT) * 0.5f;
        }

        return getMainParticipantHeight();
    }

    private float getParticipantX() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getParticipantX");
        }

        if (mIsLandscape) {
            return getLandscapeParticipantX();
        }

        return getPortraitParticipantX();
    }

    private float getPortraitParticipantX() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getPortraitParticipantX");
        }

        if (mNumberParticipants == 2 && mIsVideoCall && CallStatus.isActive(mCallStatus) && mCallParticipantViewMode != CallParticipantViewMode.SPLIT_SCREEN) {
            return mParentViewWidth - (MARGIN_PARTICIPANT * 2) - mWidth;
        } else if (mNumberParticipants == 2) {
            return MARGIN_PARTICIPANT;
        } else if (mNumberParticipants % 2 != 0) {
            if (mPosition % 2 == 0) {
                return MARGIN_PARTICIPANT;
            } else {
                return mWidth + (MARGIN_PARTICIPANT * 2);
            }
        } else {
            if (mPosition % 2 == 0) {
                return mWidth + (MARGIN_PARTICIPANT * 2);
            } else {
                return MARGIN_PARTICIPANT;
            }
        }
    }

    private float getLandscapeParticipantX() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getLandscapeParticipantX");
        }

        if (mNumberParticipants == 2 && mIsVideoCall && CallStatus.isActive(mCallStatus) && mCallParticipantViewMode != CallParticipantViewMode.SPLIT_SCREEN) {
            return mParentViewWidth - (MARGIN_PARTICIPANT * 2) - mWidth;
        } else if (mNumberParticipants == 2) {
            return mMainParticipantWidth + MARGIN_PARTICIPANT;
        } else if (mNumberParticipants == 3) {
            if (mPosition == 2) {
                return mMainParticipantWidth + MARGIN_PARTICIPANT;
            } else {
                return mMainParticipantWidth + mWidth + (MARGIN_PARTICIPANT * 2);
            }
        } else if (mNumberParticipants == 4 || mNumberParticipants == 8) {
            return (mMainParticipantWidth * (mPosition - 1))  + (MARGIN_PARTICIPANT * (mPosition - 1));
        } else if (mNumberParticipants == 5) {
            if (mPosition == 2 || mPosition == 4) {
                return mMainParticipantWidth + MARGIN_PARTICIPANT;
            } else {
                return mMainParticipantWidth + mWidth + (MARGIN_PARTICIPANT * 2);
            }
        } else if (mNumberParticipants == 6) {
            if (mPosition == 2 || mPosition == 5) {
                return mMainParticipantWidth + MARGIN_PARTICIPANT;
            } else if (mPosition == 3 || mPosition == 6) {
                return (mMainParticipantWidth * 2) + (MARGIN_PARTICIPANT * 2);
            }
        } else if (mNumberParticipants == 7) {
            if (mPosition == 2 || mPosition == 5) {
                return mMainParticipantWidth + MARGIN_PARTICIPANT;
            } else if (mPosition == 3 || mPosition == 6) {
                return (mMainParticipantWidth * 2) + (MARGIN_PARTICIPANT * 2);
            } else {
                return (mMainParticipantWidth * 3) + (MARGIN_PARTICIPANT * 3);
            }
        }

        return 0;
    }

    private float getParticipantY() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getParticipantY");
        }

        if (mIsLandscape) {
            return getLandscapeParticipantY();
        }

        return getPortraitParticipantY();
    }

    private float getPortraitParticipantY() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getPortraitParticipantY");
        }

        if (mIsVideoCall && !CallStatus.isActive(mCallStatus) && mNumberParticipants == 2) {
            return 0;
        } else if (mNumberParticipants == 2 && mIsVideoCall && CallStatus.isActive(mCallStatus) && mCallParticipantViewMode != CallParticipantViewMode.SPLIT_SCREEN) {
            return MARGIN_PARTICIPANT;
        } else if (mNumberParticipants < 4) {
            return mMainParticipantHeight + MARGIN_PARTICIPANT;
        } else if (mNumberParticipants == 4) {
            if (mPosition > 2) {
                return mHeight + MARGIN_PARTICIPANT;
            }
        } else if (mNumberParticipants == 5) {
            if (mPosition < 4) {
                return mHeight + MARGIN_PARTICIPANT;
            } else {
                return (mHeight * 2) + (MARGIN_PARTICIPANT * 2);
            }
        } else if (mNumberParticipants == 6) {
            if (mPosition < 3) {
                return 0;
            } else if (mPosition < 5) {
                return mHeight + MARGIN_PARTICIPANT;
            } else {
                return (mHeight * 2) + (MARGIN_PARTICIPANT * 2);
            }
        } else if (mNumberParticipants == 7) {
            if (mPosition < 4) {
                return mHeight + MARGIN_PARTICIPANT;
            } else if (mPosition < 6) {
                return (mHeight * 2) + (MARGIN_PARTICIPANT * 2);
            } else {
                return (mHeight * 3) + (MARGIN_PARTICIPANT * 3);
            }
        } else if (mNumberParticipants == 8) {
            if (mPosition < 3) {
                return 0;
            } if (mPosition < 5) {
                return mHeight + MARGIN_PARTICIPANT;
            } else if (mPosition < 7) {
                return (mHeight * 2) + (MARGIN_PARTICIPANT * 2);
            } else {
                return (mHeight * 3) + (MARGIN_PARTICIPANT * 3);
            }
        }

        return 0;
    }

    private float getLandscapeParticipantY() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getLandscapeParticipantY");
        }

        if (mNumberParticipants < 5) {
            return 0;
        } else if (mNumberParticipants < 7) {
            if (mPosition > 3) {
                return mHeight + MARGIN_PARTICIPANT;
            }
        } else if (mNumberParticipants < 9) {
            if (mPosition > 4) {
                return mHeight + MARGIN_PARTICIPANT;
            }
        }

        return 0;
    }

    private void moveToClosestCornerAnimated(@SuppressWarnings("SameParameterValue") boolean animated) {
        if (DEBUG) {
            Log.d(LOG_TAG, "moveToClosestCornerAnimated: animated=" + animated);
        }

        mClosestCornerUnit = closestCornerUnit();
        moveToCornerUnit(mClosestCornerUnit, animated);
    }

    private void moveToCornerUnit(Point unit, boolean animated) {
        if (DEBUG) {
            Log.d(LOG_TAG, "moveToCornerUnit: unit= " + unit + " animated=" + animated);
        }

        float xCenter = mParentViewWidth * 0.5f;
        float yCenter = mParentViewHeight * 0.5f;

        float xWidth = mParentViewWidth - getWidth() - MARGIN_PARTICIPANT * 4.0f;
        float yHeight = mParentViewHeight - getHeight() - MARGIN_PARTICIPANT * 2.0f;

        if (mIsLandscape) {
            xWidth = mParentViewWidth - getWidth() - MARGIN_PARTICIPANT * 2.0f;
        }

        Point cornerPoint = new Point((int) (xCenter + (xWidth / 2.0f * unit.x)), (int) (yCenter + (yHeight / 2.0f * unit.y)));

        float xd = cornerPoint.x - (getX() + (getWidth() / 2.0f));
        float yd = cornerPoint.y - (getY() + (getHeight() / 2.0f));

        float directDistance = (float) Math.sqrt(xd * xd + yd * yd);
        int duration = (int) ((directDistance / Design.DISPLAY_HEIGHT) * 1000);

        if (!animated) {
            duration = 0;
        }

        float newX = cornerPoint.x - (getWidth() / 2.0f);
        float newY = cornerPoint.y - (getHeight() / 2.0f);

        animate()
                .x(newX)
                .y(newY)
                .setDuration(duration)
                .start();
    }

    private Point closestCornerUnit() {
        if (DEBUG) {
            Log.d(LOG_TAG, "closestCornerUnit");
        }

        float xCenter = mParentViewWidth * 0.5f;
        float yCenter = mParentViewHeight * 0.5f;

        float xCenterDist = getX() + (getWidth() / 2.0f) - xCenter;
        float yCenterDist = getY() + (getHeight() / 2.0f) - yCenter;

        return new Point((int) (xCenterDist / Math.abs(xCenterDist)), (int) (yCenterDist / Math.abs(yCenterDist)));
    }

    protected abstract void initViews();

    protected abstract void updateViews();

    protected abstract void connectVideo();

    protected abstract void scaleVideo(float scaleFactor, boolean reset);

    protected abstract void bringVideoToFront();

    protected abstract CallParticipant getCallParticipant();

    protected abstract String getName();

    protected abstract int getParticipantId();

    protected abstract boolean isRemoteParticipant();

    protected abstract boolean isVideoInFitMode();

    protected abstract boolean isCameraMute();

    protected abstract boolean isScreenSharing();

    protected abstract boolean isMessageSupported();
    
    protected abstract boolean isLocationSupported();

    protected abstract boolean isRemoteCameraControl();

    protected abstract boolean isRemoteCameraControlSupported();

    protected abstract void minZoom();
}