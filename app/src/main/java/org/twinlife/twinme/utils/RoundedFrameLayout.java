/*
 *  Copyright (c) 2019 twinlife SA.
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
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.core.view.ViewCompat;

import java.util.Arrays;

public class RoundedFrameLayout extends FrameLayout {

    private int mWidth = 0;
    private int mHeight = 0;
    private Path mPath;
    private float[] mCornerRadii;

    public RoundedFrameLayout(Context context) {

        super(context);

        init();
    }

    public RoundedFrameLayout(Context context, AttributeSet attrs) {

        super(context, attrs);

        init();
    }

    public RoundedFrameLayout(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs, defStyle);

        init();
    }

    @Override
    public void draw(Canvas canvas) {

        canvas.save();
        mPath.addRoundRect(new RectF(0, 0, mWidth, mHeight), mCornerRadii, Path.Direction.CW);
        canvas.clipPath(mPath);
        super.draw(canvas);
        canvas.restore();
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {

        super.onSizeChanged(width, height, oldWidth, oldHeight);

        mWidth = width;
        mHeight = height;

    }

    public void setCornerRadii(float[] radii) {

        mCornerRadii = radii;
        invalidate();
    }

    private void init() {

        GradientDrawable mGradientDrawable = new GradientDrawable();
        mGradientDrawable.mutate();
        mGradientDrawable.setColor(Color.TRANSPARENT);
        mGradientDrawable.setShape(GradientDrawable.RECTANGLE);
        ViewCompat.setBackground(this, mGradientDrawable);

        mPath = new Path();

        mWidth = getWidth();
        mHeight = getHeight();

        float[] radii = new float[8];
        Arrays.fill(radii, 8f);
        mCornerRadii = radii;
    }
}
