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

import org.twinlife.twinme.utils.AudioTrackView;

public class RecordTrackView extends View {

    private static final float DESIGN_LINE_SPACE = 6f;
    private static final float DESIGN_LINE_WIDTH = 4f;
    private static final double MAX_AMPLITUDE = 32767.0;

    private int mHeight = 0;
    private Paint mPaint;
    private Path mPath = new Path();

    private int mStartLine = 0;

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

        int lineWidth = (int) (AudioTrackView.AUDIO_TRACK_LINE_WIDTH * getContext().getResources().getDisplayMetrics().density);
        mStartLine = (int) (lineWidth * 0.5f);
        mPath = new Path();
        invalidate();
    }

    public void drawLine(int amplitude) {

        int lineSpace = (int) (AudioTrackView.AUDIO_TRACK_LINE_SPACE * getContext().getResources().getDisplayMetrics().density);
        int lineWidth = (int) (AudioTrackView.AUDIO_TRACK_LINE_WIDTH * getContext().getResources().getDisplayMetrics().density);

        float scaleFactor = (float) ((mHeight - lineWidth) / MAX_AMPLITUDE);

        if (mStartLine == 0) {
            mStartLine = (int) (lineWidth * 0.5f);
        }

        int lineHeight = (int) (amplitude * scaleFactor);
        if (lineHeight == 0) {
            lineHeight = 1;
        }
        int startY = (mHeight - lineHeight) / 2;
        mPath.moveTo(mStartLine, startY);
        mPath.lineTo(mStartLine, startY + lineHeight);
        mStartLine += lineSpace;
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

        int lineWidth = (int) (AudioTrackView.AUDIO_TRACK_LINE_WIDTH * getContext().getResources().getDisplayMetrics().density);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(lineWidth);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setColor(Color.WHITE);
    }
}
