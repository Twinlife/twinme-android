/*
 *  Copyright (c) 2021-2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;

import org.twinlife.twinme.skin.Design;

public abstract class AbstractEditActivity extends AbstractTwinmeActivity {
    private static final String LOG_TAG = "AbstractEditActivity";
    private static final boolean DEBUG = false;

    protected static int AVATAR_OVER_SIZE;
    protected static int AVATAR_MAX_SIZE;

    public static final int MAX_NAME_LENGTH = 32;
    public static final int MAX_DESCRIPTION_LENGTH = 128;

    public static final float DESIGN_NAME_TOP_MARGIN = 40f;
    public static final float DESIGN_SAVE_TOP_MARGIN = 52f;
    public static final float DESIGN_DESCRIPTION_TOP_MARGIN = 44f;
    public static final float DESIGN_COUNTER_TOP_MARGIN = 2f;

    protected NestedScrollView mScrollView;
    protected View mContentView;
    protected ImageView mAvatarView;
    protected EditText mNameView;
    protected EditText mDescriptionView;
    protected TextView mCounterNameView;
    protected TextView mCounterDescriptionView;
    protected View mSaveClickableView;

    protected boolean mInitScrollView = false;
    protected float mAvatarLastSize = -1;
    protected float mScrollPosition = -1;

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
    protected void onResume() {

        super.onResume();

        if (mScrollView != null && !mInitScrollView) {
            mInitScrollView = true;
            Rect rectangle = new Rect();
            getWindow().getDecorView().getWindowVisibleDisplayFrame(rectangle);
            int contentHeight = mContentView.getHeight();
            if (contentHeight < rectangle.height()) {
                contentHeight = rectangle.height();
            }

            ViewGroup.LayoutParams layoutParams = mContentView.getLayoutParams();
            layoutParams.height = contentHeight + AVATAR_OVER_SIZE;
            mScrollView.post(() -> mScrollView.scrollBy(0, AVATAR_OVER_SIZE));
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

        mNameView.clearFocus();
        mDescriptionView.clearFocus();

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(mNameView.getWindowToken(), 0);
            inputMethodManager.hideSoftInputFromWindow(mDescriptionView.getWindowToken(), 0);
        }
    }

    protected void updateContentHeight() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateContentHeight");
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

    protected void updateAvatarSize(float deltaY) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateAvatarSize: " + deltaY);
        }

        if (mAvatarLastSize == -1) {
            mAvatarLastSize = AVATAR_MAX_SIZE - AVATAR_OVER_SIZE;
        }

        float avatarViewSize = mAvatarLastSize + deltaY;

        if (avatarViewSize < Design.DISPLAY_WIDTH) {
            avatarViewSize = Design.DISPLAY_WIDTH;
        } else if (avatarViewSize > AVATAR_MAX_SIZE) {
            avatarViewSize = AVATAR_MAX_SIZE;
        }

        if (avatarViewSize != mAvatarLastSize) {
            ViewGroup.LayoutParams avatarLayoutParams = mAvatarView.getLayoutParams();
            avatarLayoutParams.width = (int) avatarViewSize;
            avatarLayoutParams.height = (int) avatarViewSize;
            mAvatarView.requestLayout();

            mAvatarLastSize = avatarViewSize;
        }
    }

    @Override
    public void setupDesign() {
        if (DEBUG) {
            Log.d(LOG_TAG, "openMenuCapabilities");
        }

        AVATAR_OVER_SIZE = (int) (Design.AVATAR_OVER_WIDTH * Design.WIDTH_RATIO);
        AVATAR_MAX_SIZE = Design.DISPLAY_WIDTH + (AVATAR_OVER_SIZE * 2);
    }
}
