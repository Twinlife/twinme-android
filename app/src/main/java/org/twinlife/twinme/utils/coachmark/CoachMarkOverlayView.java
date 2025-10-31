/*
 *  Copyright (c) 2022 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.utils.coachmark;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

import org.twinlife.twinme.skin.Design;

public class CoachMarkOverlayView extends View {

    private RectF mRectF;
    private float mRadius;
    private Paint mPaint;


    public CoachMarkOverlayView(Context context) {

        super(context);

        init();
    }

    public CoachMarkOverlayView(Context context, AttributeSet attrs) {

        super(context, attrs);

        init();
    }

    public CoachMarkOverlayView(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs, defStyle);

        init();
    }

    public void setClipRect(RectF rect, float radius) {

        mRectF = rect;
        mRadius = radius;
        invalidate();
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(@NonNull Canvas canvas) {

        super.onDraw(canvas);

        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.TRANSPARENT);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawPaint(mPaint);

        if (mRectF != null) {
            mPaint.setColor(Color.TRANSPARENT);
            mPaint.setStyle(Paint.Style.FILL);
            canvas.drawPaint(mPaint);

            Path clipPath = new Path();
            if (mRadius != 0) {
                //clipPath.addCircle(mRectF.left + ((mRectF.right - mRectF.left) / 2), mRectF.top + ((mRectF.bottom - mRectF.top) / 2), mRadius, Path.Direction.CCW);
                clipPath.addOval(mRectF,Path.Direction.CCW);
            } else {
                clipPath.addRect(mRectF, Path.Direction.CCW);
            }

            canvas.clipPath(clipPath, Region.Op.DIFFERENCE);

            mPaint.setColor(Design.OVERLAY_VIEW_COLOR);
            canvas.drawRect(0, 0.0f, Design.DISPLAY_WIDTH, Design.DISPLAY_HEIGHT, mPaint);
        }
    }

    //
    // Private Methods
    //

    private void init() {

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
    }
}