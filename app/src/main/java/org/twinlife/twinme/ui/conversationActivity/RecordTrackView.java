/*
 *  Copyright (c) 2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.conversationActivity;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class RecordTrackView extends View {

    private static final float DESIGN_LINE_SPACE = 6f;
    private static final float DESIGN_LINE_WIDTH = 4f;
    private static final double MAX_AMPLITUDE = 32767.0;

    private int mHeight = 0;
    private Paint mPaint;
    private Path mPath = new Path();

    private int mStartLine = 1;

    public RecordTrackView(Context context) {
        super(context);

        init();
    }

    public RecordTrackView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public RecordTrackView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    public int getStartLine() {

        return mStartLine;
    }

    public void resetTrack() {

        mStartLine = 1;
        mPath = new Path();
        invalidate();
    }

    public void drawLine(int amplitude) {

        float scaleFactor = (float) (mHeight / MAX_AMPLITUDE);
        int lineHeight = (int) (amplitude * scaleFactor);
        if (lineHeight == 0) {
            lineHeight = 1;
        }
        int startY = (mHeight - lineHeight) / 2;
        mPath.moveTo(mStartLine, startY);
        mPath.lineTo(mStartLine, startY + lineHeight);
        mStartLine += DESIGN_LINE_SPACE;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {

        mHeight = height;

        super.onSizeChanged(width, height, oldWidth, oldHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawPath(mPath, mPaint);
    }

    private void init() {

        setWillNotDraw(false);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(DESIGN_LINE_WIDTH);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setColor(Color.WHITE);
    }
}
