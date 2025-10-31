/*
 *  Copyright (c) 2020 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Auguste Hatton (Auguste.Hatton@twin.life)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.utils;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.res.Resources;
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
import androidx.recyclerview.widget.RecyclerView.ItemDecoration;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.calls.AddExternalCallViewHolder;
import org.twinlife.twinme.ui.calls.SectionCallViewHolder;
import org.twinlife.twinme.ui.contacts.AddInvitationCodeViewHolder;
import org.twinlife.twinme.ui.users.UIContactViewHolder;

public class SwipeItemTouchHelper extends ItemTouchHelper.Callback {
    private static final String LOG_TAG = "SwipeItemTouchHelper";
    private static final boolean DEBUG = false;

    private static final float DESIGN_BUTTON_WIDTH_PERCENT = 0.2186f;
    private static final float DESIGN_DELETE_ICON_HEIGHT_PERCENT = 0.95f;
    private static final float DESIGN_EDIT_ICON_HEIGHT_PERCENT = 0.94f;
    private static final float DESIGN_SHARE_ICON_HEIGHT_PERCENT = 0.77f;
    private static final float DESIGN_DELETE_ICON_ASPECT_RATIO = 0.8421f;
    private static final float DESIGN_EDIT_ICON_ASPECT_RATIO = 1;
    private static final int DESIGN_ICON_MARGIN_BOTTOM = 10;
    private static final long CLOSE_ANIMATION_DURATION = 200;

    private final SwipeButton mButtonLeft;
    private final SwipeButton mButtonRight;

    private final boolean mIsRTL;
    private final OnSwipeItemClickListener mOnSwipeItemClickListener;
    private boolean mSwipeBack = false;
    private ButtonsState mButtonShowedState = ButtonsState.GONE;
    private RectF mButtonInstance;
    private RecyclerView.ViewHolder mCurrentItemViewHolder;
    private ButtonSide mButtonFocused;
    private final ButtonType mRightButtonType;

    public SwipeItemTouchHelper(RecyclerView recyclerView, ButtonType leftButtonType, ButtonType rightButtonType, OnSwipeItemClickListener onSwipeItemClickListener) {
        if (DEBUG) {
            Log.d(LOG_TAG, "SwipeController: recyclerView=" + recyclerView + " leftButtonType=" + leftButtonType + " rightButtonType" + rightButtonType);
        }

        mOnSwipeItemClickListener = onSwipeItemClickListener;

        mIsRTL = CommonUtils.isLayoutDirectionRTL();

        if (!mIsRTL) {
            mButtonLeft = leftButtonType == null ? null : new SwipeButton(leftButtonType, ButtonSide.LEFT, mIsRTL);
            mButtonRight = rightButtonType == null ? null : new SwipeButton(rightButtonType, ButtonSide.RIGHT, mIsRTL);
        } else {
            mButtonRight = leftButtonType == null ? null : new SwipeButton(leftButtonType, ButtonSide.RIGHT, mIsRTL);
            mButtonLeft = rightButtonType == null ? null : new SwipeButton(rightButtonType, ButtonSide.LEFT, mIsRTL);
        }

        mRightButtonType = rightButtonType;

        recyclerView.addItemDecoration(new ItemDecoration() {
            @Override
            public void onDraw(@NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                if (DEBUG) {
                    Log.d(LOG_TAG, "onDraw: canvas=" + canvas + " parent=" + parent + "state" + state);
                }

                if (mCurrentItemViewHolder != null) {
                    drawButtons(canvas, mCurrentItemViewHolder);
                }
            }
        });
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getMovementFlags: recyclerView=" + recyclerView + " viewHolder=" + viewHolder);
        }

        if (viewHolder instanceof SectionCallViewHolder || viewHolder instanceof AddExternalCallViewHolder || viewHolder instanceof AddInvitationCodeViewHolder) {
            return 0;
        }

        int swipeFlag = (mButtonLeft == null ? 0 : ItemTouchHelper.RIGHT) | (mButtonRight == null ? 0 : ItemTouchHelper.LEFT);

        return makeMovementFlags(0, swipeFlag);
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
            mSwipeBack = mButtonShowedState != ButtonsState.GONE;
            return 0;
        }

        return super.convertToAbsoluteDirection(flags, layoutDirection);
    }

    @Override
    public void onChildDraw(@NonNull Canvas canvas, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onChildDraw: canvas=" + canvas + " recyclerView=" + recyclerView + " viewHolder=" + viewHolder + " dX=" + dX + " dY=" + dY + " actionState=" + actionState + " isCurrentlyActive=" + isCurrentlyActive);
        }

        int buttonWidth = (int) (viewHolder.itemView.getWidth() * DESIGN_BUTTON_WIDTH_PERCENT);

        if (mRightButtonType == ButtonType.DELETE_AND_SHARE && viewHolder instanceof UIContactViewHolder) {
            buttonWidth *= 2;
        }

        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            if (mButtonShowedState != ButtonsState.GONE) {
                if (mButtonShowedState == ButtonsState.LEFT_VISIBLE) {
                    dX = Math.max(dX, buttonWidth);
                } else if (mButtonShowedState == ButtonsState.RIGHT_VISIBLE) {
                    dX = Math.min(dX, -buttonWidth);
                }
                super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, false);
            } else {
                setOnOpenSwipeListener(canvas, recyclerView, viewHolder, dX, dY, actionState);
            }
        }

        if (mButtonShowedState == ButtonsState.GONE) {
            mButtonFocused = dX > 0 ? ButtonSide.LEFT : ButtonSide.RIGHT;
            super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, false);
        }

        mCurrentItemViewHolder = viewHolder;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setOnOpenSwipeListener(final Canvas canvas, final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder, final float dX, final float dY, final int actionState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setOnOpenSwipeListener: canvas=" + canvas + " recyclerView=" + recyclerView + " viewHolder=" + viewHolder + " dX=" + dX + " dY=" + dY + " actionState=" + actionState);
        }

        int buttonWidth = (int) (viewHolder.itemView.getWidth() * DESIGN_BUTTON_WIDTH_PERCENT);

        recyclerView.setOnTouchListener((view, event) -> {
            if (DEBUG) {
                Log.d(LOG_TAG, "onTouch: view=" + view + " event=" + event);
            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    mSwipeBack = true;
                    if (dX < -buttonWidth) {
                        mButtonShowedState = ButtonsState.RIGHT_VISIBLE;
                    } else if (dX > buttonWidth) {
                        mButtonShowedState = ButtonsState.LEFT_VISIBLE;
                    }

                    if (mButtonShowedState != ButtonsState.GONE) {
                        setOnCloseSwipeListener(canvas, recyclerView, viewHolder, dX, dY, actionState);
                        setItemsClickable(recyclerView, false);
                    }
                    break;
            }

            return false;
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setOnCloseSwipeListener(final Canvas canvas, final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder, final float dX, final float dY, final int actionState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setOnCloseSwipeListener: canvas=" + canvas + " recyclerView=" + recyclerView + " viewHolder=" + viewHolder + " dX=" + dX + " dY=" + dY + " actionState=" + actionState);
        }

        recyclerView.setOnTouchListener((view, event) -> {
            if (DEBUG) {
                Log.d(LOG_TAG, "onTouch: view=" + view + " event=" + event);
            }

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (mButtonInstance != null && !mButtonInstance.contains(mButtonInstance.centerX(), event.getY())) {
                    recyclerView.setOnTouchListener((v, event1) -> false);
                    animateClose(viewHolder.itemView);
                    mSwipeBack = false;
                    setItemsClickable(recyclerView, true);
                    mButtonShowedState = ButtonsState.GONE;
                    mCurrentItemViewHolder = null;
                }
            }

            if (event.getAction() == MotionEvent.ACTION_UP) {
                animateClose(viewHolder.itemView);
                recyclerView.setOnTouchListener((v, event2) -> false);
                setItemsClickable(recyclerView, true);
                mSwipeBack = false;

                int position = viewHolder.getBindingAdapterPosition();
                if (mButtonInstance != null && mButtonInstance.contains(event.getX(), event.getY()) && position >= 0) {
                    if (!mIsRTL) {
                        if (mButtonShowedState == ButtonsState.LEFT_VISIBLE) {
                            mOnSwipeItemClickListener.onLeftActionClick(position);
                        } else if (mButtonShowedState == ButtonsState.RIGHT_VISIBLE) {
                            if (mRightButtonType == ButtonType.DELETE_AND_SHARE && viewHolder instanceof UIContactViewHolder) {
                                int buttonWidth = (int) (viewHolder.itemView.getWidth() * DESIGN_BUTTON_WIDTH_PERCENT);
                                RectF firstButton = new RectF(mButtonInstance.left, mButtonInstance.top, mButtonInstance.right - buttonWidth, mButtonInstance.bottom);
                                RectF secondButton = new RectF(mButtonInstance.left - buttonWidth, mButtonInstance.top, mButtonInstance.right, mButtonInstance.bottom);
                                if (firstButton.contains(event.getX(), event.getY())) {
                                    mOnSwipeItemClickListener.onRightActionClick(viewHolder.getBindingAdapterPosition());
                                } else if (secondButton.contains(event.getX(), event.getY())) {
                                    mOnSwipeItemClickListener.onOtherActionClick(viewHolder.getBindingAdapterPosition());
                                }
                            } else {
                                mOnSwipeItemClickListener.onRightActionClick(viewHolder.getBindingAdapterPosition());
                            }
                        }
                    } else {
                        if (mButtonShowedState == ButtonsState.LEFT_VISIBLE) {
                            if (mRightButtonType == ButtonType.DELETE_AND_SHARE && viewHolder instanceof UIContactViewHolder) {
                                int buttonWidth = (int) (viewHolder.itemView.getWidth() * DESIGN_BUTTON_WIDTH_PERCENT);
                                RectF firstButton = new RectF(mButtonInstance.left, mButtonInstance.top, mButtonInstance.right - buttonWidth, mButtonInstance.bottom);
                                RectF secondButton = new RectF(mButtonInstance.left + buttonWidth, mButtonInstance.top, mButtonInstance.right, mButtonInstance.bottom);

                                if (firstButton.contains(event.getX(), event.getY())) {
                                    mOnSwipeItemClickListener.onOtherActionClick(viewHolder.getBindingAdapterPosition());
                                } else if (secondButton.contains(event.getX(), event.getY())) {
                                    mOnSwipeItemClickListener.onRightActionClick(viewHolder.getBindingAdapterPosition());
                                }
                            } else {
                                mOnSwipeItemClickListener.onRightActionClick(viewHolder.getBindingAdapterPosition());
                            }
                        } else if (mButtonShowedState == ButtonsState.RIGHT_VISIBLE) {
                            mOnSwipeItemClickListener.onLeftActionClick(viewHolder.getBindingAdapterPosition());
                        }
                    }
                }

                mButtonShowedState = ButtonsState.GONE;
                mCurrentItemViewHolder = null;
            }
            return false;
        });
    }

    private void setItemsClickable(RecyclerView recyclerView, boolean isClickable) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setItemsClickable: recyclerView=" + recyclerView + " isClickable=" + isClickable);
        }

        for (int i = 0; i < recyclerView.getChildCount(); ++i) {
            recyclerView.getChildAt(i).setClickable(isClickable);
            recyclerView.getChildAt(i).setLongClickable(isClickable);
        }
    }

    private void animateClose(View item) {
        if (DEBUG) {
            Log.d(LOG_TAG, "animateClose: item=" + item);
        }

        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(item, "translationX", 0);
        objectAnimator.setDuration(CLOSE_ANIMATION_DURATION);
        objectAnimator.start();
    }

    private void drawButtons(Canvas canvas, RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "drawButtons: canvas=" + canvas + " viewHolder=" + viewHolder);
        }

        View itemView = viewHolder.itemView;
        RectF buttonLeftCanvas = null, buttonRightCanvas = null;

        if (mButtonFocused == ButtonSide.LEFT && mButtonLeft != null) {
            if (mIsRTL && mRightButtonType == ButtonType.DELETE_AND_SHARE && viewHolder instanceof UIContactViewHolder) {
                buttonLeftCanvas = mButtonLeft.drawSwipeButtonWithDoubleAction(canvas, itemView);
            } else {
                buttonLeftCanvas = mButtonLeft.drawSwipeButton(canvas, itemView);
            }
        }

        if (mButtonFocused == ButtonSide.RIGHT && mButtonRight != null) {
            if (mRightButtonType == ButtonType.DELETE_AND_SHARE && viewHolder instanceof UIContactViewHolder) {
                buttonRightCanvas = mButtonRight.drawSwipeButtonWithDoubleAction(canvas, itemView);
            } else {
                buttonRightCanvas = mButtonRight.drawSwipeButton(canvas, itemView);
            }
        }

        mButtonInstance = null;
        if (mButtonShowedState == ButtonsState.LEFT_VISIBLE) {
            mButtonInstance = buttonLeftCanvas;
        } else if (mButtonShowedState == ButtonsState.RIGHT_VISIBLE) {
            mButtonInstance = buttonRightCanvas;
        }
    }

    private enum ButtonsState {
        GONE,
        LEFT_VISIBLE,
        RIGHT_VISIBLE
    }

    public enum ButtonType {
        DELETE,
        LOAD,
        EDIT,
        RESET,
        DELETE_AND_SHARE
    }

    private enum ButtonSide {
        LEFT,
        RIGHT
    }

    public interface OnSwipeItemClickListener {
        void onLeftActionClick(int adapterPosition);

        void onRightActionClick(int adapterPosition);

        void onOtherActionClick(int adapterPosition);
    }

    public static class SwipeButton {

        private final ButtonSide mSide;
        private int mColorLink;
        private int mStringLink;
        private int mDrawableLink;

        private final boolean mIsRTL;

        SwipeButton(ButtonType buttonType, ButtonSide buttonSide, boolean isRTL) {
            switch (buttonType) {
                case DELETE:
                    mColorLink = Design.DELETE_COLOR_RED;
                    mStringLink = R.string.conversation_activity_menu_item_view_delete_title;
                    mDrawableLink = R.drawable.action_delete;
                    break;

                case EDIT:
                    mColorLink = Design.EDIT_COLOR;
                    mStringLink = R.string.application_edit;
                    mDrawableLink = R.drawable.action_edit;
                    break;

                case RESET:
                    mColorLink = Design.DELETE_COLOR_RED;
                    mStringLink = R.string.main_activity_reset_conversation;
                    mDrawableLink = R.drawable.action_delete;
                    break;

                case DELETE_AND_SHARE:
                    mColorLink = Design.DELETE_COLOR_RED;
                    mStringLink = R.string.conversation_activity_menu_item_view_delete_title;
                    mDrawableLink = R.drawable.action_delete;
                    break;
            }

            mSide = buttonSide;
            mIsRTL = isRTL;
        }

        RectF drawSwipeButton(Canvas canvas, View itemView) {
            if (DEBUG) {
                Log.d(LOG_TAG, "drawSwipeButton: canvas=" + canvas + " itemView=" + itemView);
            }

            RectF background = new RectF(itemView.getLeft(), itemView.getTop(), itemView.getRight(), itemView.getBottom());
            Paint colorPaint = new Paint();
            colorPaint.setColor(mColorLink);
            canvas.drawRect(background, colorPaint);

            int buttonWidth = (int) (itemView.getWidth() * DESIGN_BUTTON_WIDTH_PERCENT);

            Resources resources = itemView.getContext().getResources();
            String text = (String) resources.getText(mStringLink);

            int leftAbscissa = mSide == ButtonSide.LEFT ? itemView.getLeft() : itemView.getRight() - buttonWidth;
            int rightAbscissa = mSide == ButtonSide.LEFT ? itemView.getLeft() + buttonWidth : itemView.getRight();

            RectF swipeButton = new RectF(leftAbscissa, itemView.getTop(), rightAbscissa, itemView.getBottom());
            canvas.drawRect(swipeButton, colorPaint);

            Paint textPaint = new Paint();
            textPaint.setColor(Color.WHITE);
            textPaint.setTypeface(Design.FONT_REGULAR24.typeface);
            textPaint.setTextSize(Design.FONT_REGULAR24.size);
            textPaint.setAntiAlias(true);
            float textWidth = textPaint.measureText(text);

            Drawable actionDrawable = ResourcesCompat.getDrawable(resources, mDrawableLink, null);
            float iconAspectRatio = DESIGN_DELETE_ICON_ASPECT_RATIO;
            float iconHeightPercent = DESIGN_DELETE_ICON_HEIGHT_PERCENT;

            if (mDrawableLink == R.drawable.action_edit) {
                iconAspectRatio = DESIGN_EDIT_ICON_ASPECT_RATIO;
                iconHeightPercent = DESIGN_EDIT_ICON_HEIGHT_PERCENT;
            }

            int actionDrawableHeight = (int) (iconHeightPercent * actionDrawable.getIntrinsicHeight());
            int actionDrawableWidth = (int) (actionDrawableHeight * iconAspectRatio);

            float combinedHeight = actionDrawableHeight + DESIGN_ICON_MARGIN_BOTTOM + textPaint.getTextSize();

            int actionDrawableTop = (int) (swipeButton.centerY() - (combinedHeight / 2));
            int actionDrawableBottom = actionDrawableTop + actionDrawableHeight;

            int actionDrawableLeft = (int) swipeButton.centerX() - (actionDrawableWidth / 2);
            int actionDrawableRight = (int) swipeButton.centerX() + (actionDrawableWidth / 2);

            canvas.drawText(text, swipeButton.centerX() - (textWidth / 2), swipeButton.centerY() + (combinedHeight / 2), textPaint);

            actionDrawable.setTint(Color.WHITE);
            actionDrawable.setBounds(actionDrawableLeft, actionDrawableTop, actionDrawableRight, actionDrawableBottom);
            actionDrawable.draw(canvas);

            return swipeButton;
        }

        RectF drawSwipeButtonWithDoubleAction(Canvas canvas, View itemView) {
            if (DEBUG) {
                Log.d(LOG_TAG, "drawSwipeButtonWithDoubleAction: canvas=" + canvas + " itemView=" + itemView);
            }

            RectF background = new RectF(itemView.getLeft(), itemView.getTop(), itemView.getRight(), itemView.getBottom());
            Paint colorPaint = new Paint();
            colorPaint.setColor(mColorLink);
            canvas.drawRect(background, colorPaint);

            int buttonWidth = (int) (itemView.getWidth() * DESIGN_BUTTON_WIDTH_PERCENT);

            Resources resources = itemView.getContext().getResources();
            String text = (String) resources.getText(mStringLink);

            int leftAbscissa = mSide == ButtonSide.LEFT ? itemView.getLeft() : itemView.getRight() - buttonWidth;
            int rightAbscissa = mSide == ButtonSide.LEFT ? itemView.getLeft() + buttonWidth : itemView.getRight();

            RectF swipeButton = new RectF(leftAbscissa - buttonWidth, itemView.getTop(), rightAbscissa, itemView.getBottom());
            RectF firstButton = new RectF(leftAbscissa - buttonWidth, itemView.getTop(), rightAbscissa - buttonWidth, itemView.getBottom());

            if (mIsRTL) {
                swipeButton = new RectF(leftAbscissa, itemView.getTop(), rightAbscissa + buttonWidth, itemView.getBottom());
                firstButton = new RectF(leftAbscissa + buttonWidth, itemView.getTop(), rightAbscissa + buttonWidth, itemView.getBottom());
            }

            colorPaint.setColor(Design.DELETE_COLOR_RED);
            canvas.drawRect(firstButton, colorPaint);

            Paint textPaint = new Paint();
            textPaint.setColor(Color.WHITE);
            textPaint.setTypeface(Design.FONT_REGULAR24.typeface);
            textPaint.setTextSize(Design.FONT_REGULAR24.size);
            textPaint.setAntiAlias(true);
            float textWidth = textPaint.measureText(text);

            Drawable deleteDrawable = ResourcesCompat.getDrawable(resources, mDrawableLink, null);
            float iconAspectRatio = DESIGN_DELETE_ICON_ASPECT_RATIO;
            float iconHeightPercent = DESIGN_DELETE_ICON_HEIGHT_PERCENT;

            int actionDrawableHeight = (int) (iconHeightPercent * deleteDrawable.getIntrinsicHeight());
            int actionDrawableWidth = (int) (actionDrawableHeight * iconAspectRatio);

            float combinedHeight = actionDrawableHeight + DESIGN_ICON_MARGIN_BOTTOM + textPaint.getTextSize();

            int actionDrawableTop = (int) (firstButton.centerY() - (combinedHeight / 2));
            int actionDrawableBottom = actionDrawableTop + actionDrawableHeight;

            int actionDrawableLeft = (int) firstButton.centerX() - (actionDrawableWidth / 2);
            int actionDrawableRight = (int) firstButton.centerX() + (actionDrawableWidth / 2);

            canvas.drawText(text, firstButton.centerX() - (textWidth / 2), firstButton.centerY() + (combinedHeight / 2), textPaint);

            deleteDrawable.setTint(Color.WHITE);
            deleteDrawable.setBounds(actionDrawableLeft, actionDrawableTop, actionDrawableRight, actionDrawableBottom);
            deleteDrawable.draw(canvas);

            text = (String) resources.getText(R.string.share_activity_title);
            textWidth = textPaint.measureText(text);

            RectF secondButton = new RectF(leftAbscissa, itemView.getTop(), rightAbscissa, itemView.getBottom());

            if (mIsRTL) {
                secondButton = new RectF(leftAbscissa, itemView.getTop(), rightAbscissa, itemView.getBottom());
            }

            colorPaint.setColor(Design.getMainStyle());
            canvas.drawRect(secondButton, colorPaint);

            Drawable actionDrawable = ResourcesCompat.getDrawable(resources, R.drawable.qrcode, null);
            iconAspectRatio = DESIGN_EDIT_ICON_ASPECT_RATIO;
            iconHeightPercent = DESIGN_SHARE_ICON_HEIGHT_PERCENT;

            actionDrawableHeight = (int) (iconHeightPercent * actionDrawable.getIntrinsicHeight());
            actionDrawableWidth = (int) (actionDrawableHeight * iconAspectRatio);

            combinedHeight = actionDrawableHeight + DESIGN_ICON_MARGIN_BOTTOM + textPaint.getTextSize();

            actionDrawableTop = (int) (secondButton.centerY() - (combinedHeight / 2));
            actionDrawableBottom = actionDrawableTop + actionDrawableHeight;

            actionDrawableLeft = (int) secondButton.centerX() - (actionDrawableWidth / 2);
            actionDrawableRight = (int) secondButton.centerX() + (actionDrawableWidth / 2);

            canvas.drawText(text, secondButton.centerX() - (textWidth / 2), secondButton.centerY() + (combinedHeight / 2), textPaint);

            actionDrawable.setTint(Color.WHITE);
            actionDrawable.setBounds(actionDrawableLeft, actionDrawableTop, actionDrawableRight, actionDrawableBottom);
            actionDrawable.draw(canvas);

            return swipeButton;
        }
    }
}