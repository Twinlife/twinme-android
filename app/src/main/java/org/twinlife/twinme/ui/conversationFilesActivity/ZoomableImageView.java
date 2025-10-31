/*
 *  Copyright (c) 2017-2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Thibaud David (contact@thibauddavid.com)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.conversationFilesActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;

public class ZoomableImageView extends AppCompatImageView {

    public interface OnZoomImageTouchListener {

        void onScaleBegin();

        void onScaleEnd();

        void onTap();
    }

    private Matrix matrix;

    // We can be in one of these 3 states
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;

    // Remember some things for zooming
    private final PointF last = new PointF();
    private final PointF start = new PointF();
    private float[] m;

    private int viewWidth;
    private int viewHeight;
    private float mScale = 1f;
    private float origWidth;
    private float origHeight;
    private int oldMeasuredHeight;

    private ScaleGestureDetector mScaleDetector;
    private GestureDetector mDoubleTapDetector;

    private OnZoomImageTouchListener mOnZoomImageTouchListener;

    public ZoomableImageView(Context context) {

        super(context);

        sharedConstructing(context);
    }

    public ZoomableImageView(Context context, AttributeSet attrs) {

        super(context, attrs);

        sharedConstructing(context);
    }

    public void setOnZoomImageTouchListener(OnZoomImageTouchListener onZoomImageTouchListener) {

        mOnZoomImageTouchListener = onZoomImageTouchListener;
    }

    public float getScale() {

        return mScale;
    }

    public void resetZoom() {

        mScale = 1f;

        Drawable drawable = getDrawable();
        if (drawable == null || drawable.getIntrinsicWidth() == 0 || drawable.getIntrinsicHeight() == 0) {

            return;
        }
        int bitmapWidth = drawable.getIntrinsicWidth();
        int bitmapHeight = drawable.getIntrinsicHeight();

        float scaleX = (float) viewWidth / (float) bitmapWidth;
        float scaleY = (float) viewHeight / (float) bitmapHeight;
        // Fit to screen.
        float scale = Math.min(scaleX, scaleY);
        matrix.setScale(scale, scale);

        // Center the image
        float margeX = ((float) viewWidth - (scale * (float) bitmapWidth)) / 2f;
        float margeY = ((float) viewHeight - (scale * (float) bitmapHeight)) / 2f;

        matrix.postTranslate(margeX, margeY);

        origWidth = viewWidth - 2 * margeX;
        origHeight = viewHeight - 2 * margeY;
        setImageMatrix(matrix);

        fixTrans();
    }

    private void stopInterceptEvent() {

        getParent().requestDisallowInterceptTouchEvent(true);
    }

    private void startInterceptEvent() {

        getParent().requestDisallowInterceptTouchEvent(false);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void sharedConstructing(Context context) {

        super.setClickable(true);

        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        mDoubleTapDetector = new GestureDetector(context, new DoubleTapListener());
        matrix = new Matrix();
        m = new float[9];
        setImageMatrix(matrix);
        setScaleType(ScaleType.MATRIX);

        setOnTouchListener((View v, MotionEvent event) -> {
            mScaleDetector.onTouchEvent(event);
            mDoubleTapDetector.onTouchEvent(event);
            PointF curr = new PointF(event.getX(), event.getY());

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    last.set(curr);
                    start.set(last);
                    mode = DRAG;
                    stopInterceptEvent();
                    break;

                case MotionEvent.ACTION_MOVE:

                    if (mode == DRAG) {
                        float deltaX = curr.x - last.x;
                        float deltaY = curr.y - last.y;
                        float fixTransX = getFixDragTrans(deltaX, viewWidth, origWidth * mScale);
                        float fixTransY = getFixDragTrans(deltaY, viewHeight, origHeight * mScale);
                        matrix.postTranslate(fixTransX, fixTransY);
                        fixTrans();
                        last.set(curr.x, curr.y);

                        float transX = m[Matrix.MTRANS_X];

                        if ((int) (getFixTrans(transX, viewWidth, origWidth * mScale) + fixTransX) == 0)
                            startInterceptEvent();
                        else stopInterceptEvent();
                    }
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                    mode = NONE;
                    if (mOnZoomImageTouchListener != null) {
                        mOnZoomImageTouchListener.onScaleEnd();
                    }
                    startInterceptEvent();
                    break;
            }

            setImageMatrix(matrix);
            invalidate();

            return true; // indicate event was handled
        });
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScaleBegin(@NonNull ScaleGestureDetector detector) {

            getParent().requestDisallowInterceptTouchEvent(true);
            mode = ZOOM;
            if (mOnZoomImageTouchListener != null) {
                mOnZoomImageTouchListener.onScaleBegin();
            }
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            float scaleFactor = detector.getScaleFactor();
            float origScale = mScale;
            mScale *= scaleFactor;
            float minScale = 1f;
            float maxScale = 3f;
            if (mScale > maxScale) {
                mScale = maxScale;
                scaleFactor = maxScale / origScale;
            } else if (mScale < minScale) {
                mScale = minScale;
                scaleFactor = minScale / origScale;
            }

            if (origWidth * mScale <= viewWidth || origHeight * mScale <= viewHeight) {
                matrix.postScale(scaleFactor, scaleFactor, (float) (viewWidth / 2), (float) (viewHeight / 2));
            } else {
                matrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
            }

            fixTrans();
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {

            getParent().requestDisallowInterceptTouchEvent(false);
            mOnZoomImageTouchListener.onScaleEnd();
        }
    }

    private class DoubleTapListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDoubleTap(@NonNull MotionEvent motionEvent) {

            if (mScale == 1f) {
                mScale = 2;
                matrix.postScale(mScale, mScale, (float) (viewWidth / 2), (float) (viewHeight / 2));
                fixTrans();
            } else {
                resetZoom();
            }

            return true;
        }

        @Override
        public boolean onSingleTapUp(@NonNull MotionEvent e) {

            if (mOnZoomImageTouchListener != null) {
                mOnZoomImageTouchListener.onTap();
            }

            return true;
        }
    }

    private void fixTrans() {

        matrix.getValues(m);
        float transX = m[Matrix.MTRANS_X];
        float transY = m[Matrix.MTRANS_Y];

        float fixTransX = getFixTrans(transX, viewWidth, origWidth * mScale);
        float fixTransY = getFixTrans(transY, viewHeight, origHeight * mScale);

        if (fixTransX != 0 || fixTransY != 0) matrix.postTranslate(fixTransX, fixTransY);
    }

    private float getFixTrans(float trans, float viewSize, float contentSize) {

        float minTrans, maxTrans;

        if (contentSize <= viewSize) {
            minTrans = 0;
            maxTrans = viewSize - contentSize;
        } else {
            minTrans = viewSize - contentSize;
            maxTrans = 0;
        }

        if (trans < minTrans) {

            return -trans + minTrans;
        }
        if (trans > maxTrans) {

            return -trans + maxTrans;
        }

        return 0;
    }

    private float getFixDragTrans(float delta, float viewSize, float contentSize) {

        if (contentSize <= viewSize) {
            return 0;
        }

        return delta;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        viewHeight = MeasureSpec.getSize(heightMeasureSpec);

        //
        // Rescales image on rotation
        //
        if (oldMeasuredHeight == viewWidth && oldMeasuredHeight == viewHeight || viewWidth == 0 || viewHeight == 0) {

            return;
        }

        oldMeasuredHeight = viewHeight;

        if (mScale == 1) {
            // Fit to screen.
            float scale;

            Drawable drawable = getDrawable();
            if (drawable == null || drawable.getIntrinsicWidth() == 0 || drawable.getIntrinsicHeight() == 0) {

                return;
            }
            int bmWidth = drawable.getIntrinsicWidth();
            int bmHeight = drawable.getIntrinsicHeight();

            float scaleX = (float) viewWidth / (float) bmWidth;
            float scaleY = (float) viewHeight / (float) bmHeight;
            scale = Math.min(scaleX, scaleY);
            matrix.setScale(scale, scale);

            // Center the image
            float redundantYSpace = (float) viewHeight - (scale * (float) bmHeight);
            float redundantXSpace = (float) viewWidth - (scale * (float) bmWidth);
            redundantYSpace /= (float) 2;
            redundantXSpace /= (float) 2;

            matrix.postTranslate(redundantXSpace, redundantYSpace);

            origWidth = viewWidth - 2 * redundantXSpace;
            origHeight = viewHeight - 2 * redundantYSpace;
            setImageMatrix(matrix);
        }

        fixTrans();
    }
}
