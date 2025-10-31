/*
 *  Copyright (c) 2018-2020 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Yannis Le Gal (Yannis.LeGal@twin.life)
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.View;

import org.twinlife.twinme.skin.Design;

public class DeleteProgressView extends View {

    private GradientDrawable mGradientDeleteDrawable;
    private final Rect mBounds = new Rect();
    private float mClipRatio = 0f;
    private DeleteProgressListener mDeleteProgressListener;

    interface DeleteProgressListener {

        void onAnimationDeletedEnded();
    }

    public DeleteProgressView(Context context) {

        super(context);

        init(null, 0);
    }

    public DeleteProgressView(Context context, AttributeSet attrs) {

        super(context, attrs);

        init(attrs, 0);
    }

    public DeleteProgressView(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs, defStyle);

        init(attrs, defStyle);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {

        getDrawingRect(mBounds);
        mGradientDeleteDrawable.setBounds(mBounds);

        super.onSizeChanged(width, height, oldWidth, oldHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        canvas.save();
        canvas.clipRect(mBounds.left, mBounds.top, mBounds.left + mBounds.width() * mClipRatio, mBounds.bottom);
        mGradientDeleteDrawable.draw(canvas);
        canvas.restore();
    }

    public void setCornerRadii(float[] radii) {

        mGradientDeleteDrawable.setCornerRadii(radii);
    }

    public void setOnDeleteProgressListener(DeleteProgressListener deleteProgressListener) {

        mDeleteProgressListener = deleteProgressListener;
    }

    public void startAnimation(int duration, float progress) {

        if (duration <= 0) {
            if (mDeleteProgressListener != null) {
                mDeleteProgressListener.onAnimationDeletedEnded();
            }
            return;
        }

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(progress, 1f);
        valueAnimator.setDuration(duration);
        valueAnimator.addUpdateListener(animation -> {
            mClipRatio = (float) animation.getAnimatedValue();
            invalidate();

            if (mClipRatio >= 1f) {
                if (mDeleteProgressListener != null) {
                    mDeleteProgressListener.onAnimationDeletedEnded();
                }
            }
        });

        valueAnimator.start();
    }

    //
    // Private methods
    //

    @SuppressWarnings("unused")
    private void init(AttributeSet attrs, int defStyle) {

        mGradientDeleteDrawable = new GradientDrawable();
        mGradientDeleteDrawable.mutate();
        mGradientDeleteDrawable.setColor(Design.BACKGROUND_COLOR_RED);
        mGradientDeleteDrawable.setShape(GradientDrawable.RECTANGLE);
    }

}