/*
 *  Copyright (c) 2019-2020 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.spaces;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.twinme.ui.AbstractTwinmeActivity;

public class OnColorSpaceTouchListener implements RecyclerView.OnItemTouchListener, GestureDetector.OnGestureListener {
    private static final String LOG_TAG = "OnColorSpaceTouchLis...";
    private static final boolean DEBUG = false;

    private final GestureDetector mGestureDetector;
    private boolean mSingleTap;
    private final OnColorObserver mObserver;
    private final RecyclerView mRecyclerView;

    public interface OnColorObserver {
        /**
         * A click action is made on a color.
         *
         * @param recyclerView the contact list that holds the contact.
         * @param position     the contact position.
         * @return True if the event was handled.
         */
        boolean onUIColorSpaceClick(RecyclerView recyclerView, int position);
    }

    OnColorSpaceTouchListener(AbstractTwinmeActivity listActivity, RecyclerView recyclerView, OnColorObserver observer) {
        if (DEBUG) {
            Log.d(LOG_TAG, "OnColorSpaceTouchListener");
        }

        mObserver = observer;
        mRecyclerView = recyclerView;
        mGestureDetector = new GestureDetector(listActivity, this);
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {
        if (DEBUG) {
            Log.d(LOG_TAG, "OnItemTouchListener.onInterceptTouchEvent: recyclerView=" + recyclerView + " motionEvent=" + motionEvent);
        }

        mGestureDetector.onTouchEvent(motionEvent);

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_CANCEL:
                reset();
                break;

            case MotionEvent.ACTION_UP:
                if (mSingleTap) {
                    reset();

                    View view = recyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
                    if (view != null) {
                        RecyclerView.ViewHolder viewHolder = recyclerView.findContainingViewHolder(view);
                        int position = recyclerView.getChildAdapterPosition(view);
                        if (viewHolder instanceof ColorSpaceViewHolder) {
                            return mObserver.onUIColorSpaceClick(recyclerView, position);
                        }
                    }
                }
                break;
        }

        return false;
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        if (DEBUG) {
            Log.d(LOG_TAG, "OnItemTouchListener.onRequestDisallowInterceptTouchEvent: disallowIntercept=" + disallowIntercept);
        }
    }

    @Override
    public void onTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {
        if (DEBUG) {
            Log.d(LOG_TAG, "OnItemTouchListener.onInterceptTouchEvent: recyclerView=" + recyclerView + " motionEvent=" + motionEvent);
        }
    }

    @Override
    public boolean onDown(@NonNull MotionEvent motionEvent) {
        if (DEBUG) {
            Log.d(LOG_TAG, "OnItemTouchListener.onDown: motionEvent=" + motionEvent);
        }

        return false;
    }

    @Override
    public void onShowPress(@NonNull MotionEvent motionEvent) {
        if (DEBUG) {
            Log.d(LOG_TAG, "OnItemTouchListener.onShowPress: motionEvent=" + motionEvent);
        }
    }

    @Override
    public boolean onSingleTapUp(@NonNull MotionEvent motionEvent) {
        if (DEBUG) {
            Log.d(LOG_TAG, "OnItemTouchListener.onSingleTapUp: motionEvent=" + motionEvent);
        }

        mSingleTap = true;

        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent1, @NonNull MotionEvent motionEvent2, float distanceX, float distanceY) {
        if (DEBUG) {
            Log.d(LOG_TAG, "OnItemTouchListener.onScroll: motionEvent1=" + motionEvent1 + " motionEvent2=" + motionEvent2 +
                    " distanceX=" + distanceX + " distanceY=" + distanceY);
        }

        return false;
    }

    @Override
    public void onLongPress(@NonNull MotionEvent motionEvent) {
        if (DEBUG) {
            Log.d(LOG_TAG, "OnItemTouchListener.onLongPress: motionEvent=" + motionEvent);
        }
    }

    @Override
    public boolean onFling(MotionEvent motionEvent1, @NonNull MotionEvent motionEvent2, float velocityX, float velocityY) {
        if (DEBUG) {
            Log.d(LOG_TAG, "OnItemTouchListener.onFling: motionEvent1=" + motionEvent1 + " motionEvent2=" + motionEvent2 +
                    " velocityX=" + velocityX + " velocityY=" + velocityY);
        }

        return false;
    }

    private void reset() {

        mSingleTap = false;
    }
}
