/*
 *  Copyright (c) 2021-2022 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class ReplyItemTouchHelper extends ItemTouchHelper.Callback {
    private static final String LOG_TAG = "ReplyItemTouchHelper";
    private static final boolean DEBUG = false;

    private enum ButtonSide {
        LEFT,
        RIGHT
    }

    private static final float DESIGN_BUTTON_WIDTH = 116f;
    private static final float DESIGN_ICON_HEIGHT = 36f;

    private ButtonSide mSide;
    private final Drawable mReplyIcon;

    private final OnSwipeItemReplyListener mOnSwipeItemReplyListener;
    private boolean mSwipeBack = false;
    private RecyclerView.ViewHolder mCurrentItemViewHolder;
    private View mView;

    private float mDx = 0f;

    public interface OnSwipeItemReplyListener {
        void onReplySwipe(int adapterPosition);
    }

    public ReplyItemTouchHelper(RecyclerView recyclerView, OnSwipeItemReplyListener onSwipeItemReplyListener) {
        if (DEBUG) {
            Log.d(LOG_TAG, "SwipeController: recyclerView=" + recyclerView);
        }

        mReplyIcon = ResourcesCompat.getDrawable(recyclerView.getResources(), R.drawable.reply_swipe_icon, null);
        mOnSwipeItemReplyListener = onSwipeItemReplyListener;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getMovementFlags: recyclerView=" + recyclerView + " viewHolder=" + viewHolder);
        }

        mView = viewHolder.itemView;

        BaseItemViewHolder itemViewHolder = (BaseItemViewHolder) viewHolder;
        if (itemViewHolder.getItem() == null || !itemViewHolder.getItem().canReply() || itemViewHolder.getItem().getState() == Item.ItemState.DELETED || itemViewHolder.getBaseItemActivity().isMenuOpen()) {
            return 0;
        }

        if (itemViewHolder.getItem().isPeerItem()) {
            mSide = ButtonSide.LEFT;
            return makeMovementFlags(0, ItemTouchHelper.RIGHT);
        } else {
            mSide = ButtonSide.RIGHT;
            return makeMovementFlags(0, ItemTouchHelper.LEFT);
        }
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onMove: recyclerView=" + recyclerView + " viewHolder=" + viewHolder + " target=" + target);
        }

        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSwiped: viewHolder=" + viewHolder + " direction=" + direction);
        }

    }

    @Override
    public int convertToAbsoluteDirection(int flags, int layoutDirection) {
        if (DEBUG) {
            Log.d(LOG_TAG, "convertToAbsoluteDirection: flags=" + flags + " layoutDirection=" + layoutDirection);
        }

        if (mSwipeBack) {
            mSwipeBack = false;
            return 0;
        }

        return super.convertToAbsoluteDirection(flags, layoutDirection);
    }

    @Override
    public void onChildDraw(@NonNull Canvas canvas, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            setOnOpenSwipeListener(recyclerView, viewHolder);
        }

        if (Math.abs(mView.getTranslationX()) < (DESIGN_BUTTON_WIDTH * Design.WIDTH_RATIO) || dX < mDx) {
            super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            mDx = dX;
        }

        mCurrentItemViewHolder = viewHolder;
        drawReplyButton(canvas);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setOnOpenSwipeListener(final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setOnOpenSwipeListener: recyclerView=" + recyclerView + " viewHolder=" + viewHolder);
        }

        recyclerView.setOnTouchListener((view, event) -> {
            if (DEBUG) {
                Log.d(LOG_TAG, "onTouch: view=" + view + " event=" + event);
            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    mSwipeBack = true;
                    break;
            }

            if (mSwipeBack) {
                if (Math.abs(mView.getTranslationX()) >= (DESIGN_BUTTON_WIDTH * Design.WIDTH_RATIO)) {
                    int position = mCurrentItemViewHolder.getBindingAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        mOnSwipeItemReplyListener.onReplySwipe(position);
                    }
                }
            }
            return false;
        });
    }

    private void drawReplyButton(Canvas canvas) {

        float translationX = Math.abs(mView.getTranslationX());
        float progress = translationX / (DESIGN_BUTTON_WIDTH * Design.WIDTH_RATIO);

        if (progress > 1) {
            progress = 1;
        }

        int alpha = (int) Math.min(255.0, 255.0 * progress);
        mReplyIcon.setAlpha(alpha);

        RectF background = new RectF(mView.getLeft(), mView.getTop(), mView.getRight(), mView.getBottom());
        Paint paint = new Paint();
        paint.setColor(Color.TRANSPARENT);
        canvas.drawRect(background, paint);

        int buttonWidth = (int) (DESIGN_BUTTON_WIDTH * Design.WIDTH_RATIO);
        int leftAbscissa = mSide == ButtonSide.LEFT ? mView.getLeft() : mView.getRight() - buttonWidth;
        int rightAbscissa = mSide == ButtonSide.LEFT ? mView.getLeft() + buttonWidth : mView.getRight();

        RectF swipeButton = new RectF(leftAbscissa, mView.getTop(), rightAbscissa, mView.getBottom());
        canvas.drawRect(swipeButton, paint);

        int replyDrawableSize = (int) (DESIGN_ICON_HEIGHT * Design.HEIGHT_RATIO * progress);
        int replyDrawableTop = (int) (swipeButton.centerY() - (replyDrawableSize * 0.5));
        int replyDrawableBottom = replyDrawableTop + replyDrawableSize;

        int replyDrawableLeft = (int) (swipeButton.centerX() - (replyDrawableSize * 0.5));
        int replyDrawableRight = (int) (swipeButton.centerX() + (replyDrawableSize * 0.5));

        mReplyIcon.setBounds(replyDrawableLeft, replyDrawableTop, replyDrawableRight, replyDrawableBottom);
        mReplyIcon.draw(canvas);
    }
}
