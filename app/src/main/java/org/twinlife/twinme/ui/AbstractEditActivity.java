/*
 *  Copyright (c) 2021-2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.twinlife.twinme.skin.Design;

public abstract class AbstractEditActivity extends AbstractTwinmeActivity {
    private static final String LOG_TAG = "AbstractEditActivity";
    private static final boolean DEBUG = false;

    protected final int ACTION_BACK = 0;
    protected final int ACTION_SAVE = 1;
    protected final int ACTION_EDIT_NAME = 2;
    protected final int ACTION_EDIT_DESCRIPTION = 3;
    protected final int ACTION_SETTINGS = 4;
    protected final int ACTION_START_DATE = 5;
    protected final int ACTION_START_TIME = 6;
    protected final int ACTION_END_DATE = 7;
    protected final int ACTION_END_TIME = 8;
    protected final int ACTION_ADD_MEMBERS = 9;
    protected final int ACTION_INVITE = 10;
    protected final int ACTION_CODE = 11;
    protected final int ACTION_REMOVE = 12;

    protected class ViewTapGestureDetector extends GestureDetector.SimpleOnGestureListener {

        private final int mAction;

        public ViewTapGestureDetector(int action) {
            mAction = action;
        }

        @Override
        public boolean onDoubleTap(@NonNull MotionEvent e) {
            return false;
        }

        @Override
        public void onLongPress(@NonNull MotionEvent e) {
        }

        @Override
        public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {

            switch (mAction) {
                case ACTION_BACK:
                    onBackClick();
                    break;

                case ACTION_SAVE:
                    onSaveClick();
                    break;

                case ACTION_EDIT_NAME:
                    onNameViewClick();
                    break;

                case ACTION_EDIT_DESCRIPTION:
                    onDescriptionViewClick();
                    break;

                case ACTION_SETTINGS:
                    onSettingsViewClick();
                    break;

                case ACTION_START_DATE:
                    onStartDateViewClick();
                    break;

                case ACTION_START_TIME:
                    onStartTimeViewClick();
                    break;

                case ACTION_END_DATE:
                    onEndDateViewClick();
                    break;

                case ACTION_END_TIME:
                    onEndTimeViewClick();
                    break;

                case ACTION_ADD_MEMBERS:
                    onAddMemberClick();
                    break;

                case ACTION_INVITE:
                    onInviteClick();
                    break;

                case ACTION_CODE:
                    onInvitationCodeClick();
                    break;

                case ACTION_REMOVE:
                    onRemoveClick();
                    break;

                default:
                    break;
            }
            return true;
        }

        @Override
        public boolean onDown(@NonNull MotionEvent e) {

            return true;
        }
    }

    public static final int MAX_NAME_LENGTH = 32;
    public static final int MAX_DESCRIPTION_LENGTH = 128;

    public static final float DESIGN_NAME_TOP_MARGIN = 40f;
    public static final float DESIGN_SAVE_TOP_MARGIN = 52f;
    public static final float DESIGN_DESCRIPTION_TOP_MARGIN = 44f;
    public static final float DESIGN_COUNTER_TOP_MARGIN = 2f;

    protected View mContentView;
    protected ImageView mAvatarView;
    protected EditText mNameView;
    protected EditText mDescriptionView;
    protected TextView mCounterNameView;
    protected TextView mCounterDescriptionView;
    protected View mSaveClickableView;

    private float mContentViewDY;


    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        setFullscreen();

        initViews();

        showProgressIndicator();
    }

    //
    // Override Activity methods
    //

    @Override
    protected void onPause() {

        super.onPause();

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(mNameView.getWindowToken(), 0);
        }
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSaveInstanceState: outState=" + outState);
        }

        super.onSaveInstanceState(outState);
    }

    protected void hideKeyboard() {
        if (DEBUG) {
            Log.d(LOG_TAG, "hideKeyboard");
        }

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(mNameView.getWindowToken(), 0);
            inputMethodManager.hideSoftInputFromWindow(mDescriptionView.getWindowToken(), 0);
        }
    }

    //
    // Private methods
    //

    protected abstract void initViews();

    protected abstract void onSaveClick();

    protected void onNameViewClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onNameViewClick");
        }

        boolean hasFocus = mNameView.hasFocus();
        mNameView.requestFocus();
        // Set the selection only the first time the name is clicked (otherwise the user cannot position the cursor within the text).
        if (!hasFocus) {
            mNameView.setSelection(mNameView.getText().length());
        }

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.showSoftInput(mNameView, InputMethodManager.SHOW_IMPLICIT);
            updateContentOffset();
        }
    }

    protected void onDescriptionViewClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDescriptionViewClick");
        }

        boolean hasFocus = mDescriptionView.hasFocus();
        mDescriptionView.requestFocus();
        // Set the selection only the first time the name is clicked (otherwise the user cannot position the cursor within the text).
        if (!hasFocus) {
            mDescriptionView.setSelection(mDescriptionView.getText().length());
        }

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.showSoftInput(mDescriptionView, InputMethodManager.SHOW_IMPLICIT);
            updateContentOffset();
        }
    }

    protected void onSettingsViewClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSettingsViewClick");
        }
    }

    protected void onStartDateViewClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onStartDateViewClick");
        }
    }

    protected void onStartTimeViewClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onStartTimeViewClick");
        }
    }

    protected void onEndDateViewClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onEndDateViewClick");
        }
    }

    protected void onEndTimeViewClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onEndTimeViewClick");
        }
    }

    protected void onAddMemberClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onAddMemberClick");
        }
    }

    protected void onInviteClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onInviteClick");
        }
    }

    protected void onInvitationCodeClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onInvitationCodeClick");
        }
    }

    protected void onRemoveClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRemoveClick");
        }
    }

    protected boolean touchContent(MotionEvent motionEvent) {
        if (DEBUG) {
            Log.d(LOG_TAG, "touchContent");
        }

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mContentViewDY = mContentView.getY() - motionEvent.getRawY();
                break;

            case MotionEvent.ACTION_MOVE:
                float newY = motionEvent.getRawY() + mContentViewDY;
                float diffY = mContentView.getY() - newY;

                if (newY > Design.CONTENT_VIEW_MIN_Y && newY < Design.CONTENT_VIEW_INITIAL_POSITION) {
                    mContentView.animate()
                            .y(motionEvent.getRawY() + mContentViewDY)
                            .setDuration(0)
                            .start();
                }

                float avatarViewWidth = mAvatarView.getWidth() - diffY;
                float avatarViewHeight = mAvatarView.getHeight() - diffY;

                if (avatarViewWidth < Design.DISPLAY_WIDTH) {
                    avatarViewWidth = Design.DISPLAY_WIDTH;
                } else if (avatarViewWidth > Design.AVATAR_MAX_WIDTH) {
                    avatarViewWidth = Design.AVATAR_MAX_WIDTH;
                }

                if (avatarViewHeight < (Design.AVATAR_MAX_HEIGHT - Design.AVATAR_OVER_WIDTH)) {
                    avatarViewHeight = Design.AVATAR_MAX_HEIGHT - Design.AVATAR_OVER_WIDTH;
                } else if (avatarViewHeight > Design.AVATAR_MAX_HEIGHT) {
                    avatarViewHeight = Design.AVATAR_MAX_HEIGHT;
                }

                ViewGroup.LayoutParams avatarLayoutParams = mAvatarView.getLayoutParams();
                avatarLayoutParams.width = (int) avatarViewWidth;
                avatarLayoutParams.height = (int) avatarViewHeight;
                mAvatarView.requestLayout();

                break;

            case MotionEvent.ACTION_UP:
                break;

            default:
                return false;
        }
        return true;
    }

    protected void updateContentOffset() {

        float contentY = Design.CONTENT_VIEW_FOCUS_Y;
        float diffY = mContentView.getY() - contentY;
        mContentView.animate()
                .y(contentY)
                .setDuration(0)
                .start();

        float avatarViewWidth = mAvatarView.getWidth() - diffY;
        float avatarViewHeight = mAvatarView.getHeight() - diffY;

        if (avatarViewWidth < Design.DISPLAY_WIDTH) {
            avatarViewWidth = Design.DISPLAY_WIDTH;
        } else if (avatarViewWidth > Design.AVATAR_MAX_WIDTH) {
            avatarViewWidth = Design.AVATAR_MAX_WIDTH;
        }

        if (avatarViewHeight < (Design.AVATAR_MAX_HEIGHT - Design.AVATAR_OVER_WIDTH)) {
            avatarViewHeight = Design.AVATAR_MAX_HEIGHT - Design.AVATAR_OVER_WIDTH;
        } else if (avatarViewHeight > Design.AVATAR_MAX_HEIGHT) {
            avatarViewHeight = Design.AVATAR_MAX_HEIGHT;
        }

        ViewGroup.LayoutParams avatarLayoutParams = mAvatarView.getLayoutParams();
        avatarLayoutParams.width = (int) avatarViewWidth;
        avatarLayoutParams.height = (int) avatarViewHeight;
        mAvatarView.requestLayout();
    }
}
