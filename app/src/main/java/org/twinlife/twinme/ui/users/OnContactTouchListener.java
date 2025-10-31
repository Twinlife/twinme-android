/*
 *  Copyright (c) 2018-2022 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */
package org.twinlife.twinme.ui.users;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.contacts.AddContactViewHolder;

public class OnContactTouchListener implements RecyclerView.OnItemTouchListener, GestureDetector.OnGestureListener {
    private static final String LOG_TAG = "OnContactTouchListener";
    private static final boolean DEBUG = false;

    protected final GestureDetector mGestureDetector;
    protected boolean mSingleTap;
    protected boolean mHorizontalScroll;
    protected boolean mVerticalScroll;
    protected final OnContactObserver mObserver;
    protected final RecyclerView mRecyclerView;

    public enum Direction {
        LEFT,
        TOP,
        BOTTOM,
        RIGHT
    }

    public interface OnContactObserver {
        /**
         * A click action is made on a contact.
         *
         * @param recyclerView the contact list that holds the contact.
         * @param position     the contact position.
         * @return True if the event was handled.
         */
        boolean onUIContactClick(RecyclerView recyclerView, int position);

        /**
         * A fling action is made on a contact.
         *
         * @param recyclerView the contact list that holds the contact.
         * @param position     the contact position.
         * @param direction    the fling direction.
         * @return True if the event was handled.
         */
        boolean onUIContactFling(RecyclerView recyclerView, int position, Direction direction);

        /**
         * A click action is made on add contact section.
         *
         * @param recyclerView the contact list that holds the contact.
         * @return True if the event was handled.
         */
        default boolean onAddContactClick(RecyclerView recyclerView) {
            return false;
        }
    }

    public OnContactTouchListener(AbstractTwinmeActivity listActivity, RecyclerView recyclerView, OnContactObserver observer) {
        if (DEBUG) {
            Log.d(LOG_TAG, "OnItemTouchListener");
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
            case MotionEvent.ACTION_UP:
                if (mSingleTap) {
                    reset();

                    View view = recyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
                    if (view != null) {
                        RecyclerView.ViewHolder viewHolder = recyclerView.findContainingViewHolder(view);
                        int position = recyclerView.getChildAdapterPosition(view);
                        if (viewHolder instanceof UIContactViewHolder) {
                            return mObserver.onUIContactClick(recyclerView, position);
                        } else if (viewHolder instanceof AddContactViewHolder) {
                            return mObserver.onAddContactClick(recyclerView);
                        }
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE:
                break;

            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_CANCEL:
                reset();
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
    public boolean onScroll(@Nullable MotionEvent motionEvent1, @NonNull MotionEvent motionEvent2, float distanceX, float distanceY) {
        if (DEBUG) {
            Log.d(LOG_TAG, "OnItemTouchListener.onScroll: motionEvent1=" + motionEvent1 + " motionEvent2=" + motionEvent2 + " distanceX=" + distanceX +
                    " distanceY=" + distanceY);
        }

        if (!mHorizontalScroll && !mVerticalScroll) {
            if (Math.abs(distanceX) > 2 * Math.abs(distanceY)) {
                mHorizontalScroll = true;
            } else {
                mVerticalScroll = true;
            }
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
            Log.d(LOG_TAG, "OnItemTouchListener.onFling: motionEvent1=" + motionEvent1 + " motionEvent2=" + motionEvent2 + " velocityX=" + velocityX +
                    " velocityY=" + velocityY);
        }

        Direction direction;
        if (Math.abs(velocityX) > Math.abs(velocityY)) {
            direction = velocityX > 0 ? Direction.RIGHT : Direction.LEFT;
        } else {
            direction = velocityY > 0 ? Direction.TOP : Direction.BOTTOM;
        }
        float x = motionEvent1.getX();
        float y = motionEvent1.getY();
        View view = mRecyclerView.findChildViewUnder(x, y);
        if (view != null) {
            RecyclerView.ViewHolder viewHolder = mRecyclerView.findContainingViewHolder(view);
            int position = mRecyclerView.getChildAdapterPosition(view);
            if (viewHolder instanceof UIContactViewHolder) {
                return mObserver.onUIContactFling(mRecyclerView, position, direction);
            }
        }
        return false;
    }

    protected void reset() {
        mSingleTap = false;
        mHorizontalScroll = false;
        mVerticalScroll = false;
    }
}
