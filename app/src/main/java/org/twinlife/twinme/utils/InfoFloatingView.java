/*
 *  Copyright (c) 2024-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.utils;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowInsets;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.percentlayout.widget.PercentRelativeLayout;

import com.airbnb.lottie.LottieAnimationView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

import java.util.ArrayList;
import java.util.List;

/** @noinspection deprecation*/
public class InfoFloatingView extends PercentRelativeLayout implements View.OnTouchListener {
    private static final String LOG_TAG = "InfoFloatingView";
    private static final boolean DEBUG = false;

    private static final float DESIGN_X_INSET = 40f;
    private static final float DESIGN_Y_INSET = 100f;
    private static final float DESIGN_DEFAULT_SIZE = 60f;
    private static final float DESIGN_IMAGE_SIZE = 36f;
    private static final float DESIGN_IMAGE_MARGIN = 12f;
    private static final float DESIGN_DEFAULT_ALPHA = 1f;
    private static final float DESIGN_EXTEND_ALPHA = 1f;
    private static final int ANIMATION_DURATION = 500;

    class GestureTap extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDoubleTap(@NonNull MotionEvent e) {

            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {

            if (mOnInfoClickListener != null) {
                mOnInfoClickListener.onInfoClick();
            }

            return true;
        }

        @Override
        public void onLongPress(@NonNull MotionEvent e) {
            super.onLongPress(e);

            if (mOnInfoClickListener != null) {
                mOnInfoClickListener.onInfoLongPress();
            }
        }
    }

    public interface OnInfoClickListener {
        void onInfoClick();

        void onInfoLongPress();
    }

    public interface Observer {

        void onHideInfoFloatingView();

        void onTouchInfoFloatingView();
    }

    private ImageView mImageView;
    private LottieAnimationView mAnimationView;
    private TextView mMessageView;
    private View mBackgroundView;

    private OnInfoClickListener mOnInfoClickListener;
    private Observer mObserver;

    private GestureDetector mTapDetector;

    private float mDeltaX;
    private float mDeltaY;

    @Nullable
    private AppStateInfo mAppStateInfo;
    @Nullable
    private UIAppInfo mUIAppInfo;

    private final Handler mHandler = new Handler();
    private final Runnable mHideFloatingView = this::checkFloatingView;
    private final Runnable mUpdateMessage = this::updateMessage;

    public InfoFloatingView(Context context) {

        super(context);
        if (DEBUG) {
            Log.d(LOG_TAG, "InfoFloatingView");
        }

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            View view = inflater.inflate(R.layout.info_floating_view, (ViewGroup) getParent());
            view.setLayoutParams(new PercentRelativeLayout.LayoutParams(PercentRelativeLayout.LayoutParams.MATCH_PARENT, PercentRelativeLayout.LayoutParams.MATCH_PARENT));
            addView(view);
        }
        setVisibility(INVISIBLE);
        setOnTouchListener(this);
        setBackgroundColor(Color.TRANSPARENT);

        mTapDetector = new GestureDetector(context, new GestureTap());

        mBackgroundView = findViewById(R.id.info_floating_view_background_view);

        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.mutate();
        gradientDrawable.setShape(GradientDrawable.OVAL);
        gradientDrawable.setCornerRadii(new float[]{0, 0, 0, 0, 0, 0, 0, 0});
        gradientDrawable.setColor(Color.BLACK);
        gradientDrawable.setStroke(3, Design.POPUP_BACKGROUND_COLOR);
        mBackgroundView.setBackground(gradientDrawable);

        mBackgroundView.setAlpha(DESIGN_DEFAULT_ALPHA);

        mImageView = findViewById(R.id.info_floating_view_image);

        MarginLayoutParams marginLayoutParams = (MarginLayoutParams) mImageView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_IMAGE_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_IMAGE_MARGIN * Design.WIDTH_RATIO));

        marginLayoutParams.rightMargin = (int) (DESIGN_IMAGE_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginEnd((int) (DESIGN_IMAGE_MARGIN * Design.WIDTH_RATIO));

        mAnimationView = findViewById(R.id.info_floating_view_animation_view);
        mAnimationView.setVisibility(INVISIBLE);

        marginLayoutParams = (MarginLayoutParams) mAnimationView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_IMAGE_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_IMAGE_MARGIN * Design.WIDTH_RATIO));

        marginLayoutParams.rightMargin = (int) (DESIGN_IMAGE_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginEnd((int) (DESIGN_IMAGE_MARGIN * Design.WIDTH_RATIO));

        mMessageView = findViewById(R.id.info_floating_view_text);
        Design.updateTextFont(mMessageView, Design.FONT_MEDIUM32);
        mMessageView.setTextColor(Color.WHITE);

        marginLayoutParams = (MarginLayoutParams) mMessageView.getLayoutParams();
        marginLayoutParams.rightMargin = (int) (DESIGN_IMAGE_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginEnd((int) (DESIGN_IMAGE_MARGIN * Design.WIDTH_RATIO));

        mHandler.postDelayed(mHideFloatingView, 1000);
    }

    public InfoFloatingView(Context context, AttributeSet attrs) {

        super(context, attrs);
    }

    public InfoFloatingView(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs, defStyle);
    }

    @NonNull
    public AppStateInfo.InfoFloatingViewState getState() {

        return mAppStateInfo == null ? AppStateInfo.InfoFloatingViewState.DEFAULT : mAppStateInfo.getState();
    }

    public void setAppInfo(@NonNull AppStateInfo appStateInfo) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setAppInfo state=" + appStateInfo);
        }

        mAppStateInfo = appStateInfo;

        boolean needsUpdate = mUIAppInfo == null || mUIAppInfo.getInfoFloatingViewType() != appStateInfo.getType();
        mUIAppInfo = new UIAppInfo(getContext(), mAppStateInfo.getType());

        if (needsUpdate) {
            updateInfo();
        }

        mHandler.removeCallbacks(mHideFloatingView);
        mHandler.postDelayed(mHideFloatingView, 1000);
    }

    public void setOnInfoClickListener(OnInfoClickListener onInfoClickListener) {

        mOnInfoClickListener = onInfoClickListener;
    }

    public void setObserver(Observer observer) {

        mObserver = observer;
    }

    public void tapAction() {

        float width;
        float x;
        float alpha;

        float labelX = (DESIGN_IMAGE_MARGIN * Design.HEIGHT_RATIO * 2) + (DESIGN_IMAGE_SIZE * Design.HEIGHT_RATIO);

        resetUpdateMessage();

        if (getState() == AppStateInfo.InfoFloatingViewState.DEFAULT) {
            mMessageView.setVisibility(GONE);

            float defaultX = DESIGN_X_INSET * Design.WIDTH_RATIO;
            if ((int) getX() != (int) defaultX) {
                defaultX = Design.DISPLAY_WIDTH - (DESIGN_X_INSET * Design.WIDTH_RATIO) - (DESIGN_DEFAULT_SIZE * Design.HEIGHT_RATIO);
            }

            x = defaultX;
            width = DESIGN_DEFAULT_SIZE * Design.HEIGHT_RATIO;

            alpha = DESIGN_DEFAULT_ALPHA;
        } else {
            mMessageView.setVisibility(VISIBLE);

            startUpdateMessage();

            float maxWidth = Design.DISPLAY_WIDTH - (DESIGN_X_INSET * Design.WIDTH_RATIO * 2);
            float newWidth = labelX + getMessageWidth() + (DESIGN_IMAGE_MARGIN * Design.HEIGHT_RATIO);

            float extendX = DESIGN_X_INSET * Design.WIDTH_RATIO;
            if ((int) getX() != (int) extendX && newWidth < maxWidth) {
                extendX = Design.DISPLAY_WIDTH - (DESIGN_X_INSET * Design.WIDTH_RATIO) - newWidth;
            }

            width = Math.min(newWidth, maxWidth);
            x = extendX;
            alpha = DESIGN_EXTEND_ALPHA;
        }

        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.mutate();
        gradientDrawable.setColor(Color.BLACK);
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setStroke(3, Design.POPUP_BACKGROUND_COLOR);
        mBackgroundView.setBackground(gradientDrawable);

        float corner = ((float)mBackgroundView.getHeight() / 2) * Resources.getSystem().getDisplayMetrics().density;
        gradientDrawable.setCornerRadius(corner);

        ValueAnimator widthAnimator = ValueAnimator
                .ofInt(getMeasuredWidth(), (int)width)
                .setDuration(ANIMATION_DURATION);

        widthAnimator.addUpdateListener(animation -> {
            getLayoutParams().width = (Integer) animation.getAnimatedValue();
            requestLayout();
        });

        PropertyValuesHolder propertyValuesHolderX = PropertyValuesHolder.ofFloat(View.X, getX(), x);
        PropertyValuesHolder propertyValuesHolderAlpha = PropertyValuesHolder.ofFloat(View.ALPHA, mBackgroundView.getAlpha(), alpha);

        ObjectAnimator xAnimator = ObjectAnimator.ofPropertyValuesHolder(this, propertyValuesHolderX);
        ObjectAnimator alphaAnimator = ObjectAnimator.ofPropertyValuesHolder(mBackgroundView, propertyValuesHolderAlpha);

        List<Animator> animators = new ArrayList<>();
        animators.add(xAnimator);
        animators.add(widthAnimator);
        animators.add(alphaAnimator);

        AnimatorSet animationSet = new AnimatorSet();
        animationSet.setInterpolator(new DecelerateInterpolator());
        animationSet.playTogether(animators);
        animationSet.start();
    }

    public void moveToTopRight() {

        setVisibility(VISIBLE);
        moveToCornerUnit(new Point(1, -1), this, false);
    }

    public void hideView() {
        if (DEBUG) {
            Log.d(LOG_TAG, "hideView state=" + mAppStateInfo);
        }

        mHandler.removeCallbacks(mHideFloatingView);

        resetUpdateMessage();

        ViewGroup viewParent = (ViewGroup) getParent();
        if (viewParent != null) {
            viewParent.removeView(this);
        }

        setObserver(null);
        setOnInfoClickListener(null);
        mAppStateInfo = null;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {

        Path path = new Path();
        path.addRoundRect(new RectF(0, 0, getWidth(), getHeight()), getHeight() * 0.5f, getHeight() * 0.5f, Path.Direction.CW);
        canvas.clipPath(path);


        super.dispatchDraw(canvas);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {

        if (mObserver != null) {
            mObserver.onTouchInfoFloatingView();
        }

        mTapDetector.onTouchEvent(event);

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                mDeltaX = view.getX() - event.getRawX();
                mDeltaY = view.getY() - event.getRawY();
                break;

            case MotionEvent.ACTION_MOVE:

                if (getState() == AppStateInfo.InfoFloatingViewState.EXTEND) {
                    return false;
                }

                float newX = event.getRawX() + mDeltaX;
                float newY = event.getRawY() + mDeltaY;

                view.animate()
                        .x(newX)
                        .y(newY)
                        .setDuration(0)
                        .start();
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

        if (getState() == AppStateInfo.InfoFloatingViewState.EXTEND) {
            return;
        }

        Point closestCornerUnit = closestCornerUnit(view);
        moveToCornerUnit(closestCornerUnit, view, animated);
    }

    private void moveToCornerUnit(Point unit, View view, boolean animated) {
        if (DEBUG) {
            Log.d(LOG_TAG, "moveToCornerUnit unit=" + unit);
        }

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

        float xWidth = screenWidth - view.getWidth() - (DESIGN_X_INSET * Design.WIDTH_RATIO * 2.0f);
        float yHeight = screenHeight - view.getHeight() - (DESIGN_Y_INSET * Design.HEIGHT_RATIO * 2.0f);

        Point cornerPoint = new Point((int) (xCenter + (xWidth / 2.0f * unit.x)), (int) (yCenter + (yHeight / 2.0f * unit.y)));

        float xd = cornerPoint.x - (view.getX() + (view.getWidth() / 2.0f));
        float yd = cornerPoint.y - (view.getY() + (view.getHeight() / 2.0f));

        float directDistance = (float) Math.sqrt(xd * xd + yd * yd);
        int duration = (int) ((directDistance / 720.f));

        if (!animated) {
            duration = 0;
        }

        float newX = cornerPoint.x - (view.getWidth() / 2.0f);
        float newY = cornerPoint.y - (view.getHeight() / 2.0f );

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

    private void startUpdateMessage() {

        if (mUIAppInfo != null && !mUIAppInfo.getMessage().isEmpty()) {
            mMessageView.setText(mUIAppInfo.getMessage());
        }

        updateMessage();
    }

    private void resetUpdateMessage() {

        mHandler.removeCallbacks(mUpdateMessage);
    }

    private void updateInfo() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateInfo state=" + mAppStateInfo);
        }

        if (mUIAppInfo == null || mAppStateInfo == null) {
            return;
        }
        mMessageView.setText(mUIAppInfo.getTitle());
        mImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), mUIAppInfo.getImageId(), null));
        mImageView.setColorFilter(mUIAppInfo.getColor());

        if (getState() == AppStateInfo.InfoFloatingViewState.EXTEND) {
            updateFrame();
        }

        if (mAppStateInfo.getType() != AppStateInfo.InfoFloatingViewType.CONNECTED) {
            if (mAnimationView.isAnimating()) {
                mAnimationView.cancelAnimation();
            }
            mAnimationView.setVisibility(INVISIBLE);
            mImageView.setVisibility(VISIBLE);
        } else {
            if (!mAnimationView.isAnimating()) {
                mAnimationView.playAnimation();
            }
            mAnimationView.setVisibility(VISIBLE);
            mImageView.setVisibility(INVISIBLE);
        }
    }

    private void updateMessage() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateMessage state=" + mAppStateInfo);
        }

        if (getState() == AppStateInfo.InfoFloatingViewState.EXTEND && (mUIAppInfo != null && !mUIAppInfo.getMessage().isEmpty())) {

            if (mMessageView.getText().equals(mUIAppInfo.getTitle())) {
                mMessageView.setText(mUIAppInfo.getMessage());
            } else {
                mMessageView.setText(mUIAppInfo.getTitle());
            }

            updateFrame();
        }
        mHandler.postDelayed(mUpdateMessage, 5000);
    }

    private void checkFloatingView() {
        if (DEBUG) {
            Log.d(LOG_TAG, "checkFloatingView state=" + mAppStateInfo);
        }

        if (mAppStateInfo != null && mObserver != null) {
            if (!mAppStateInfo.displayInfoFloatingView()) {
                mObserver.onHideInfoFloatingView();
            } else {
                mHandler.postDelayed(mHideFloatingView, 1000);
            }
        }
    }

    private void updateFrame() {

        float width;
        float x;
        float alpha;

        float labelX = (DESIGN_IMAGE_MARGIN * Design.HEIGHT_RATIO * 2) + (DESIGN_IMAGE_SIZE * Design.HEIGHT_RATIO);
        float maxWidth = Design.DISPLAY_WIDTH - (DESIGN_X_INSET * Design.WIDTH_RATIO * 2);
        float newWidth = labelX + getMessageWidth() + (DESIGN_IMAGE_MARGIN * Design.HEIGHT_RATIO);

        float extendX = DESIGN_X_INSET * Design.WIDTH_RATIO;
        if ((int) getX() != (int) extendX && newWidth < maxWidth) {
            extendX = Design.DISPLAY_WIDTH - (DESIGN_X_INSET * Design.WIDTH_RATIO) - newWidth;
        }

        width = Math.min(newWidth, maxWidth);
        x = extendX;
        alpha = DESIGN_EXTEND_ALPHA;

        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.mutate();
        gradientDrawable.setColor(Color.BLACK);
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setStroke(3, Design.POPUP_BACKGROUND_COLOR);
        mBackgroundView.setBackground(gradientDrawable);

        float corner = ((float)mBackgroundView.getHeight() / 2) * Resources.getSystem().getDisplayMetrics().density;
        gradientDrawable.setCornerRadius(corner);

        ValueAnimator widthAnimator = ValueAnimator
                .ofInt(getMeasuredWidth(), (int)width)
                .setDuration(ANIMATION_DURATION);

        widthAnimator.addUpdateListener(animation -> {
            getLayoutParams().width = (Integer) animation.getAnimatedValue();
            requestLayout();
        });

        PropertyValuesHolder propertyValuesHolderX = PropertyValuesHolder.ofFloat(View.X, getX(), x);
        PropertyValuesHolder propertyValuesHolderAlpha = PropertyValuesHolder.ofFloat(View.ALPHA, mBackgroundView.getAlpha(), alpha);

        ObjectAnimator xAnimator = ObjectAnimator.ofPropertyValuesHolder(this, propertyValuesHolderX);
        ObjectAnimator alphaAnimator = ObjectAnimator.ofPropertyValuesHolder(mBackgroundView, propertyValuesHolderAlpha);

        List<Animator> animators = new ArrayList<>();
        animators.add(xAnimator);
        animators.add(widthAnimator);
        animators.add(alphaAnimator);

        AnimatorSet animationSet = new AnimatorSet();
        animationSet.setInterpolator(new DecelerateInterpolator());
        animationSet.playTogether(animators);
        animationSet.start();
    }

    private float getMessageWidth() {

        Paint paint = new Paint();
        paint.setTextSize(Design.FONT_MEDIUM32.size);
        paint.setTypeface(Design.FONT_MEDIUM32.typeface);
        paint.setStyle(Paint.Style.STROKE);

        String message = mMessageView.getText().toString();

        float textWidth = paint.measureText(message);
        if (textWidth == 0) {
            Rect rect = new Rect();
            paint.getTextBounds(message, 0, message.length(), rect);
            textWidth = rect.width();
        }

        return textWidth;
    }
}