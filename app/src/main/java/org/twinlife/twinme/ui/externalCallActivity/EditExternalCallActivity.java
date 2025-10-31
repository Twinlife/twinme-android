/*
 *  Copyright (c) 2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.externalCallActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.percentlayout.widget.PercentRelativeLayout;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.models.CallReceiver;
import org.twinlife.twinme.services.CallReceiverService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractEditActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.contacts.DeleteConfirmView;
import org.twinlife.twinme.utils.AbstractConfirmView;
import org.twinlife.twinme.utils.RoundedView;

import java.util.Locale;
import java.util.UUID;

public class EditExternalCallActivity extends AbstractEditActivity implements CallReceiverService.Observer {
    private static final String LOG_TAG = "EditExternalCallAc...";
    private static final boolean DEBUG = false;

    private class RemoveListener implements View.OnClickListener {

        private boolean disabled = true;

        @Override
        public void onClick(View view) {
            if (DEBUG) {
                Log.d(LOG_TAG, "RemoveListener.onClick: view=" + view);
            }

            if (disabled) {

                return;
            }
            disabled = true;

            onRemoveClick();
        }

        void enable() {

            disabled = false;
        }
    }

    private RemoveListener mRemoveListener;

    private TextView mTitleView;

    private boolean mUIInitialized = false;
    private String mName;
    private String mDescription;
    private boolean mUpdated = false;
    private Bitmap mAvatar;

    private CallReceiverService mCallReceiverService;
    private CallReceiver mCallReceiver;

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

        mCallReceiverService = new CallReceiverService(this, getTwinmeContext(), this);

        Intent intent = getIntent();
        String callReceiverId = intent.getStringExtra(Intents.INTENT_CALL_RECEIVER_ID);
        if (callReceiverId != null) {
            mCallReceiverService.getCallReceiver(UUID.fromString(callReceiverId));
        } else {
            finish();
        }

        showProgressIndicator();
    }

    //
    // Override Activity methods
    //

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        mCallReceiverService.dispose();

        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSaveInstanceState: outState=" + outState);
        }

        super.onSaveInstanceState(outState);
    }

    //
    // Implement CallReceiverService.Observer methods
    //

    @Override
    public void onGetCallReceiver(@Nullable CallReceiver callReceiver) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetCallReceiver: " + callReceiver);
        }

        mCallReceiver = callReceiver;

        setFullscreen();
        mRemoveListener.enable();
        mName = mCallReceiver.getName();

        if (mCallReceiver.getDescription() != null) {
            mDescription = mCallReceiver.getDescription();
        }

        mCallReceiverService.getImage(callReceiver, (Bitmap avatar) -> {
            mAvatar = avatar;

            if (mCallReceiver.getAvatarId() != null) {
                mCallReceiverService.getLargeAvatar(mCallReceiver.getAvatarId());
            }

            updateExternalCall();
        });
    }

    @Override
    public void onUpdateCallReceiver(@NonNull CallReceiver callReceiver) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateCallReceiver: " + callReceiver);
        }

        if (callReceiver.getId().equals(mCallReceiver.getId())) {
            finish();
        }
    }

    @Override
    public void onUpdateCallReceiverAvatar(@NonNull Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateCallReceiverAvatar: " + avatar);
        }

        mAvatar = avatar;
        updateExternalCall();
    }

    @Override
    public void onDeleteCallReceiver(@NonNull UUID callReceiverId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteCallReceiver: " + callReceiverId);
        }

        if (callReceiverId.equals(mCallReceiver.getId())) {
            finish();
        }
    }

    //
    // Private methods
    //

    @SuppressLint({"ClickableViewAccessibility"})
    @Override
    protected void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.edit_external_call_activity);

        showToolBar(true);
        showBackButton(true);

        setTitle(getString(R.string.application_profile));

        mAvatarView = findViewById(R.id.edit_external_call_activity_avatar_view);

        ViewGroup.LayoutParams layoutParams = mAvatarView.getLayoutParams();
        layoutParams.width = Design.AVATAR_MAX_WIDTH;
        layoutParams.height = Design.AVATAR_MAX_HEIGHT;

        View backClickableView = findViewById(R.id.edit_external_call_activity_back_clickable_view);
        GestureDetector backGestureDetector = new GestureDetector(this, new ViewTapGestureDetector(ACTION_BACK));
        backClickableView.setOnTouchListener((v, motionEvent) -> {
            backGestureDetector.onTouchEvent(motionEvent);
            touchContent(motionEvent);
            return true;
        });

        layoutParams = backClickableView.getLayoutParams();
        layoutParams.height = Design.BACK_CLICKABLE_VIEW_HEIGHT;

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) backClickableView.getLayoutParams();
        marginLayoutParams.leftMargin = Design.BACK_CLICKABLE_VIEW_LEFT_MARGIN;
        marginLayoutParams.topMargin = Design.BACK_CLICKABLE_VIEW_TOP_MARGIN;

        RoundedView backRoundedView = findViewById(R.id.edit_external_call_activity_back_rounded_view);
        backRoundedView.setColor(Design.BACK_VIEW_COLOR);

        mContentView = findViewById(R.id.edit_external_call_activity_content_view);
        mContentView.setY(Design.CONTENT_VIEW_INITIAL_POSITION);

        setBackground(mContentView);

        View slideMarkView = findViewById(R.id.edit_external_call_activity_slide_mark_view);
        layoutParams = slideMarkView.getLayoutParams();
        layoutParams.height = Design.SLIDE_MARK_HEIGHT;

        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.mutate();
        gradientDrawable.setColor(Color.rgb(244, 244, 244));
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        ViewCompat.setBackground(slideMarkView, gradientDrawable);

        float corner = ((float)Design.SLIDE_MARK_HEIGHT / 2) * Resources.getSystem().getDisplayMetrics().density;
        gradientDrawable.setCornerRadius(corner);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) slideMarkView.getLayoutParams();
        marginLayoutParams.topMargin = Design.SLIDE_MARK_TOP_MARGIN;

        mContentView.setOnTouchListener((v, motionEvent) -> touchContent(motionEvent));

        mTitleView = findViewById(R.id.edit_external_call_activity_title_view);
        Design.updateTextFont(mTitleView, Design.FONT_BOLD44);
        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        View headerView = findViewById(R.id.edit_external_call_activity_content_header_view);
        marginLayoutParams = (ViewGroup.MarginLayoutParams) headerView.getLayoutParams();
        marginLayoutParams.topMargin = Design.HEADER_VIEW_TOP_MARGIN;

        View nameContentView = findViewById(R.id.edit_external_call_activity_name_content_view);

        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable nameViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        nameViewBackground.getPaint().setColor(Design.EDIT_TEXT_BACKGROUND_COLOR);
        ViewCompat.setBackground(nameContentView, nameViewBackground);

        layoutParams = nameContentView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = Design.BUTTON_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) nameContentView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_NAME_TOP_MARGIN * Design.HEIGHT_RATIO);

        mNameView = findViewById(R.id.edit_external_call_activity_name_view);
        Design.updateTextFont(mNameView, Design.FONT_REGULAR28);
        mNameView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mNameView.setHintTextColor(Design.GREY_COLOR);
        mNameView.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_NAME_LENGTH)});
        mNameView.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {

                mCounterNameView.setText(String.format(Locale.getDefault(), "%d/%d", s.length(), MAX_NAME_LENGTH));
                setUpdated();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        GestureDetector nameGestureDetector = new GestureDetector(this, new ViewTapGestureDetector(ACTION_EDIT_NAME));
        mNameView.setOnTouchListener((v, motionEvent) -> {
            boolean result = nameGestureDetector.onTouchEvent(motionEvent);
            touchContent(motionEvent);
            return result;
        });

        mCounterNameView = findViewById(R.id.edit_external_call_activity_counter_name_view);
        Design.updateTextFont(mCounterNameView, Design.FONT_REGULAR26);
        mCounterNameView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mCounterNameView.setText(String.format(Locale.getDefault(), "0/%d", MAX_NAME_LENGTH));

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mCounterNameView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_COUNTER_TOP_MARGIN * Design.HEIGHT_RATIO);

        View descriptionContentView = findViewById(R.id.edit_external_call_activity_description_content_view);

        ShapeDrawable descriptionContentViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        descriptionContentViewBackground.getPaint().setColor(Design.EDIT_TEXT_BACKGROUND_COLOR);
        ViewCompat.setBackground(descriptionContentView, descriptionContentViewBackground);

        layoutParams = descriptionContentView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = (int) Design.DESCRIPTION_CONTENT_VIEW_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) descriptionContentView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_DESCRIPTION_TOP_MARGIN * Design.HEIGHT_RATIO);

        mDescriptionView = findViewById(R.id.edit_external_call_activity_description_view);
        Design.updateTextFont(mDescriptionView, Design.FONT_REGULAR28);
        mDescriptionView.setTextColor(Design.EDIT_TEXT_TEXT_COLOR);
        mDescriptionView.setHintTextColor(Design.GREY_COLOR);
        mDescriptionView.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_DESCRIPTION_LENGTH)});
        mDescriptionView.addTextChangedListener(new TextWatcher() {

            @SuppressLint("DefaultLocale")
            @Override
            public void afterTextChanged(Editable s) {

                mCounterDescriptionView.setText(String.format(Locale.getDefault(), "%d/%d", s.length(), MAX_DESCRIPTION_LENGTH));
                setUpdated();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        GestureDetector descriptionGestureDetector = new GestureDetector(this, new ViewTapGestureDetector(ACTION_EDIT_DESCRIPTION));
        mDescriptionView.setOnTouchListener((v, motionEvent) -> {
            boolean result = descriptionGestureDetector.onTouchEvent(motionEvent);
            touchContent(motionEvent);
            return result;
        });

        mCounterDescriptionView = findViewById(R.id.edit_external_call_activity_counter_description_view);
        Design.updateTextFont(mCounterDescriptionView, Design.FONT_REGULAR26);
        mCounterDescriptionView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mCounterDescriptionView.setText(String.format(Locale.getDefault(), "0/%d", MAX_DESCRIPTION_LENGTH));

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mCounterDescriptionView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_COUNTER_TOP_MARGIN * Design.HEIGHT_RATIO);

        mSaveClickableView = findViewById(R.id.edit_external_call_activity_save_view);
        mSaveClickableView.setOnClickListener(v -> onSaveClick());
        mSaveClickableView.setAlpha(0.5f);

        GestureDetector saveGestureDetector = new GestureDetector(this, new ViewTapGestureDetector(ACTION_SAVE));
        mSaveClickableView.setOnTouchListener((v, motionEvent) -> {
            saveGestureDetector.onTouchEvent(motionEvent);
            touchContent(motionEvent);
            return true;
        });

        ShapeDrawable saveViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        saveViewBackground.getPaint().setColor(Design.getMainStyle());
        ViewCompat.setBackground(mSaveClickableView, saveViewBackground);

        layoutParams = mSaveClickableView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = Design.BUTTON_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mSaveClickableView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_SAVE_TOP_MARGIN * Design.HEIGHT_RATIO);

        TextView saveTextView = findViewById(R.id.edit_external_call_activity_save_title_view);
        Design.updateTextFont(saveTextView, Design.FONT_BOLD28);
        saveTextView.setTextColor(Color.WHITE);

        View removeView = findViewById(R.id.edit_external_call_activity_remove_view);
        mRemoveListener = new RemoveListener();
        removeView.setOnClickListener(mRemoveListener);

        layoutParams = removeView.getLayoutParams();
        layoutParams.height = Design.BUTTON_HEIGHT;

        TextView removeTextView = findViewById(R.id.edit_external_call_activity_remove_label_view);
        Design.updateTextFont(removeTextView, Design.FONT_REGULAR34);
        removeTextView.setTextColor(Design.FONT_COLOR_RED);

        mProgressBarView = findViewById(R.id.edit_external_call_activity_progress_bar);

        mUIInitialized = true;
    }

    @Override
    protected void onSaveClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSaveClick");
        }

        if (mCallReceiverService == null || mCallReceiver == null) {

            return;
        }

        hideKeyboard();

        String callReceiverName = mNameView.getText().toString().trim();
        if (callReceiverName.isEmpty()) {
            callReceiverName = mName;
        }

        String callReceiverDescription = mDescriptionView.getText().toString().trim();
        if (callReceiverDescription.isEmpty()) {
            callReceiverDescription = mDescription;
        }

        boolean updated = !callReceiverName.equals(mName);
        updated = updated || (callReceiverDescription != null && !callReceiverDescription.equals(mDescription));

        if (updated) {
            mCallReceiverService.updateCallReceiver(mCallReceiver, callReceiverName, callReceiverDescription, mCallReceiver.getIdentityName(), mCallReceiver.getIdentityDescription(), null, null, null);
        }
    }

    private void onRemoveClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRemoveClick");
        }

        if (mCallReceiverService == null || mCallReceiver == null) {

            return;
        }

        PercentRelativeLayout percentRelativeLayout = findViewById(R.id.edit_external_call_activity_layout);

        DeleteConfirmView deleteConfirmView = new DeleteConfirmView(this, null);
        PercentRelativeLayout.LayoutParams layoutParams = new PercentRelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        deleteConfirmView.setLayoutParams(layoutParams);
        deleteConfirmView.setAvatar(mAvatar, false);

        String message;
        if (mCallReceiver.isTransfer()) {
            message = getString(R.string.transfert_call_activity_delete_message) + "\n\n"  + getString(R.string.transfert_call_activity_delete_confirm_message);
        } else {
            message = getString(R.string.edit_external_call_activity_delete_message) + "\n\n"  + getString(R.string.edit_external_call_activity_delete_confirm_message);
        }
        deleteConfirmView.setMessage(message);

        AbstractConfirmView.Observer observer = new AbstractConfirmView.Observer() {
            @Override
            public void onConfirmClick() {
                mCallReceiverService.deleteCallReceiver(mCallReceiver);
                deleteConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCancelClick() {
                mRemoveListener.enable();
                deleteConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onDismissClick() {
                mRemoveListener.enable();
                deleteConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                percentRelativeLayout.removeView(deleteConfirmView);
                setFullscreen();
            }
        };
        deleteConfirmView.setObserver(observer);

        percentRelativeLayout.addView(deleteConfirmView);
        deleteConfirmView.show();

        Window window = getWindow();
        window.setNavigationBarColor(Design.POPUP_BACKGROUND_COLOR);
    }

    private void updateExternalCall() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateExternalCall");
        }

        if (!mUIInitialized || mCallReceiver == null || mName == null) {

            return;
        }

        mAvatarView.setImageBitmap(mAvatar);
        mNameView.setHint(mName);

        if (mName.length() > MAX_NAME_LENGTH) {
            mName = mName.substring(0, MAX_NAME_LENGTH);
        }
        mTitleView.setText(mName);

        mCounterNameView.setText(String.format(Locale.getDefault(), "%d/%d", mName.length(), MAX_NAME_LENGTH));
        mCounterDescriptionView.setText(String.format(Locale.getDefault(), "%d/%d", mDescription == null ? 0 : mDescription.length(), MAX_DESCRIPTION_LENGTH));

        // If the nameView contains some text, this is a text entered by the user and the activity was restored.
        if (mNameView.getText().toString().isEmpty()) {
            mNameView.append(mName);
        } else {
            setUpdated();
        }

        // If the nameView contains some text, this is a text entered by the user and the activity was restored.
        if (mDescription != null && mDescriptionView.getText().toString().isEmpty()) {
            mDescriptionView.append(mDescription);
        } else {
            setUpdated();
        }

        mNameView.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {

                mCounterNameView.setText(String.format(Locale.getDefault(), "%d/%d", s.length(), MAX_NAME_LENGTH));

                if (!s.toString().isEmpty() && !s.toString().equals(mName)) {
                    setUpdated();
                } else {
                    mUpdated = false;
                    mSaveClickableView.setAlpha(0.5f);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        mDescriptionView.addTextChangedListener(new TextWatcher() {

            @SuppressLint("DefaultLocale")
            @Override
            public void afterTextChanged(Editable s) {

                mCounterDescriptionView.setText(String.format(Locale.getDefault(), "%d/%d", s.length(), MAX_DESCRIPTION_LENGTH));
                setUpdated();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        if (mAvatar != null) {
            mAvatarView.setImageBitmap(mAvatar);
        }
    }

    private void setUpdated() {
        if (DEBUG) {
            Log.d(LOG_TAG, "setUpdated");
        }

        if (mUpdated) {

            return;
        }
        mUpdated = true;

        mSaveClickableView.setAlpha(1.0f);
    }
}
