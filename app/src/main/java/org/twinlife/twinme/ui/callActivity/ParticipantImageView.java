/*
 *  Copyright (c) 2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.callActivity;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;

public class ParticipantImageView extends androidx.appcompat.widget.AppCompatImageView {

    private static final float IMAGE_RADIUS = 24.0f;
    private Path mPath = new Path();
    private RectF mRect;

    public ParticipantImageView(Context context) {

        super(context);
    }

    public ParticipantImageView(Context context, AttributeSet attrs) {

        super(context, attrs);
    }

    public ParticipantImageView(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs, defStyle);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {

        super.onSizeChanged(width, height, oldWidth, oldHeight);

        mPath = new Path();
        mRect = new RectF(0, 0, width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (mRect != null) {
            mPath.addRoundRect(mRect, IMAGE_RADIUS, IMAGE_RADIUS, Path.Direction.CW);
            canvas.clipPath(mPath);
        }

        super.onDraw(canvas);
    }
}
