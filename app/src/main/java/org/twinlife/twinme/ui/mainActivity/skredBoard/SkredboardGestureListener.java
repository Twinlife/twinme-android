/*
 *  Copyright (c) 2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.mainActivity.skredBoard;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class SkredboardGestureListener extends GestureDetector.SimpleOnGestureListener {

    private static final int SWIPE_DISTANCE = 50;
    private static final int SWIPE_VELOCITY = 60;

    private final OnSkredboardGestureListener mOnSkredboardGestureListener;

    public Context mContext;

    public interface OnSkredboardGestureListener {

        void onOpenSkredboard();

        void onCloseSkredboard();

        void onSingleTap();
    }

    public SkredboardGestureListener(Context context, OnSkredboardGestureListener onSkredboardGestureListener) {
        super();

        mContext = context;
        mOnSkredboardGestureListener = onSkredboardGestureListener;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {

        mOnSkredboardGestureListener.onSingleTap();

        return true;
    }

    @Override
    public boolean onDown(MotionEvent event) {

        return true;
    }

    @Override
    public boolean onFling(MotionEvent motionEvent1, MotionEvent motionEvent2, float velocityX, float velocityY) {

        float dY = motionEvent1.getY() - motionEvent2.getY();

        if (Math.abs(velocityY) >= SWIPE_VELOCITY && Math.abs(dY) >= SWIPE_DISTANCE) {
            if (dY > 0) {
                mOnSkredboardGestureListener.onCloseSkredboard();
            } else {
                mOnSkredboardGestureListener.onOpenSkredboard();
            }
        }
        return false;
    }
}