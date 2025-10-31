/*
 *  Copyright (c) 2020-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.groups;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.percentlayout.widget.PercentRelativeLayout;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinlife.util.Utils;
import org.twinlife.twinme.models.Group;
import org.twinlife.twinme.models.GroupMember;
import org.twinlife.twinme.services.GroupService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractEditActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.contacts.DeleteConfirmView;
import org.twinlife.twinme.ui.profiles.MenuPhotoView;
import org.twinlife.twinme.utils.AbstractConfirmView;
import org.twinlife.twinme.utils.EditableView;
import org.twinlife.twinme.utils.RoundedView;
import org.twinlife.twinme.utils.UIMenuSelectAction;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class EditGroupActivity extends AbstractGroupActivity {
    private static final String LOG_TAG = "EditGroupActivity";
    private static final boolean DEBUG = false;

    private class RemoveListener implements OnClickListener {

        // Disabled by default until we call enable() when the group instance is retrieved.
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

    protected final int ACTION_BACK = 0;
    protected final int ACTION_SAVE = 1;
    protected final int ACTION_EDIT_NAME = 2;
    protected final int ACTION_EDIT_DESCRIPTION = 3;

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

    protected static final float DESIGN_NAME_TOP_MARGIN = 40f;

    private EditableView mEditableView;
    private View mContentView;
    private ImageView mAvatarView;
    private ImageView mNoAvatarView;
    private ImageView mEditAvatarView;
    private EditText mNameView;
    private EditText mDescriptionView;
    private TextView mCounterNameView;
    private TextView mCounterDescriptionView;
    private View mSaveClickableView;
    private TextView mRemoveLabelView;
    private TextView mTitleView;
    private TextView mSaveTextView;

    @Nullable
    private UUID mGroupId;
    private Bitmap mUpdatedGroupAvatar;
    private File mUpdatedGroupAvatarFile;

    private RemoveListener mRemoveListener;

    private boolean mUIInitialized = false;
    @Nullable
    private Group mGroup;
    private String mGroupName;
    private String mGroupDescription;
    private Bitmap mGroupAvatar;
    private boolean mUpdated = false;
    private boolean mDisableUpdated = false;
    private boolean mHasClearedName = false;

    private GroupService mGroupService;

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

        Intent intent = getIntent();
        mGroupId = Utils.UUIDFromString(intent.getStringExtra(Intents.INTENT_GROUP_ID));

        initViews();

        mGroupService = new GroupService(this, getTwinmeContext(), this);

        if (savedInstanceState != null) {
            if (mEditableView != null) {
                mEditableView.onCreate(savedInstanceState);
                updateSelectedImage();
            }
            updateGroup();
        }
    }

    //
    // Override Activity methods
    //

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        mGroupService.dispose();

        // Cleanup capture and cropped images.
        if (mEditableView != null) {
            mEditableView.onDestroy();
        }

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        super.onResume();

        if (mGroupId != null && mGroup == null) {
            mGroupService.getGroup(mGroupId, true);
        }
    }

    @Override
    protected void onPause() {

        super.onPause();

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(mNameView.getWindowToken(), 0);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSaveInstanceState: outState=" + outState);
        }

        super.onSaveInstanceState(outState);

        if (mEditableView != null) {
            mEditableView.onSaveInstanceState(outState);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onActivityResult requestCode=" + requestCode + " resultCode=" + resultCode + " data=" + data);
        }

        super.onActivityResult(requestCode, resultCode, data);

        if (mEditableView != null) {
            mEditableView.onActivityResult(requestCode, resultCode, data);

            if (resultCode == Activity.RESULT_OK) {
                updateSelectedImage();
            }
        }
    }

    @Override
    public void onRequestPermissions(@NonNull Permission[] grantedPermissions) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRequestPermissions grantedPermissions=" + Arrays.toString(grantedPermissions));
        }

        if (!mEditableView.onRequestPermissions(grantedPermissions)) {
            message(getString(R.string.application_denied_permissions), 0L, new DefaultMessageCallback(R.string.application_ok) {
            });
        }
    }

    @Override
    public void onGetGroup(@NonNull Group group, @NonNull List<GroupMember> groupMembers, @NonNull ConversationService.GroupConversation conversation) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetGroup group=" + group);
        }

        mGroup = group;

        mRemoveListener.enable();
        if (mGroup.hasPeer()) {
            mGroupName = group.getName();
            mGroupDescription = group.getDescription();
            if (mGroupAvatar == null) {
                mGroupService.getImage(group, (Bitmap avatar) -> {
                    mGroupAvatar = avatar;
                    updateGroup();
                });
                return;
            }
        }

        updateGroup();
    }

    @Override
    public void onUpdateGroup(@NonNull Group group, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateGroup group=" + group);
        }

        if (mUpdated) {
            finish();
        } else {
            mGroup = group;

            if (mGroup.hasPeer()) {
                mGroupAvatar = avatar;
                mGroupName = group.getName();
                mGroupDescription = group.getDescription();
            }

            updateGroup();
        }

    }

    @Override
    public void onLeaveGroup(@NonNull Group group, @NonNull UUID memberTwincodeId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onLeaveGroup group=" + group + " memberTwincodeId=" + memberTwincodeId);
        }

        if (mGroup != null && mGroup.getMemberTwincodeOutboundId().equals(memberTwincodeId)) {
            finish();
        }
    }

    //
    // Private methods
    //

    @SuppressLint("ClickableViewAccessibility")
    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.edit_group_activity);

        setTitle(getString(R.string.application_name));
        showToolBar(false);
        showBackButton(true);
        setBackgroundColor(Design.WHITE_COLOR);

        mAvatarView = findViewById(R.id.edit_group_activity_avatar_view);

        mEditAvatarView = findViewById(R.id.edit_group_activity_edit_avatar_view);
        mEditAvatarView.setVisibility(View.GONE);

        mNoAvatarView = findViewById(R.id.edit_group_activity_no_avatar_view);
        mNoAvatarView.setVisibility(View.GONE);

        mAvatarView.setOnClickListener(view -> openMenuPhoto());

        mEditableView = new EditableView(this);

        ViewGroup.LayoutParams layoutParams = mAvatarView.getLayoutParams();
        layoutParams.width = Design.AVATAR_MAX_WIDTH;
        layoutParams.height = Design.AVATAR_MAX_HEIGHT;

        View backClickableView = findViewById(R.id.edit_group_activity_back_clickable_view);
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

        RoundedView backRoundedView = findViewById(R.id.edit_group_activity_back_rounded_view);
        backRoundedView.setColor(Design.BACK_VIEW_COLOR);

        mContentView = findViewById(R.id.edit_group_activity_content_view);
        mContentView.setY(Design.CONTENT_VIEW_INITIAL_POSITION);

        setBackground(mContentView);

        View slideMarkView = findViewById(R.id.edit_group_activity_slide_mark_view);
        layoutParams = slideMarkView.getLayoutParams();
        layoutParams.height = Design.SLIDE_MARK_HEIGHT;

        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.mutate();
        gradientDrawable.setColor(Color.rgb(244, 244, 244));
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        slideMarkView.setBackground(gradientDrawable);

        float corner = ((float)Design.SLIDE_MARK_HEIGHT / 2) * Resources.getSystem().getDisplayMetrics().density;
        gradientDrawable.setCornerRadius(corner);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) slideMarkView.getLayoutParams();
        marginLayoutParams.topMargin = Design.SLIDE_MARK_TOP_MARGIN;

        mContentView.setOnTouchListener((v, motionEvent) -> touchContent(motionEvent));

        mTitleView = findViewById(R.id.edit_group_activity_title_view);
        Design.updateTextFont(mTitleView, Design.FONT_BOLD44);
        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        View headerView = findViewById(R.id.edit_group_activity_content_header_view);
        marginLayoutParams = (ViewGroup.MarginLayoutParams) headerView.getLayoutParams();
        marginLayoutParams.topMargin = Design.HEADER_VIEW_TOP_MARGIN;

        View nameContentView = findViewById(R.id.edit_group_activity_name_content_view);

        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable nameViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        nameViewBackground.getPaint().setColor(Design.EDIT_TEXT_BACKGROUND_COLOR);
        nameContentView.setBackground(nameViewBackground);

        layoutParams = nameContentView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = Design.BUTTON_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) nameContentView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_NAME_TOP_MARGIN * Design.HEIGHT_RATIO);

        mNameView = findViewById(R.id.edit_group_activity_name_view);
        Design.updateTextFont(mNameView, Design.FONT_REGULAR28);
        mNameView.setTextColor(Design.EDIT_TEXT_TEXT_COLOR);
        mNameView.setHintTextColor(Design.GREY_COLOR);
        mNameView.setFilters(new InputFilter[]{new InputFilter.LengthFilter(AbstractEditActivity.MAX_NAME_LENGTH)});
        mNameView.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {

                mCounterNameView.setText(String.format(Locale.getDefault(), "%d/%d", s.length(), AbstractEditActivity.MAX_NAME_LENGTH));
                if (!s.toString().isEmpty() && !s.toString().equals(mGroupName)) {
                    setUpdated();
                } else if (s.toString().isEmpty() && !mHasClearedName && mGroup != null  && mGroup.getPeerTwincodeOutbound() != null && !mGroupName.equals(mGroup.getPeerTwincodeOutbound().getName())) {
                    mHasClearedName = true;
                    String peerName = mGroup.getPeerTwincodeOutbound().getName();
                    if (peerName != null) {
                        mNameView.setText(peerName);
                        mNameView.setSelection(peerName.length());
                    }
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

        GestureDetector nameGestureDetector = new GestureDetector(this, new ViewTapGestureDetector(ACTION_EDIT_NAME));
        mNameView.setOnTouchListener((v, motionEvent) -> {
            boolean result = nameGestureDetector.onTouchEvent(motionEvent);
            touchContent(motionEvent);
            return result;
        });

        mCounterNameView = findViewById(R.id.edit_group_activity_counter_name_view);
        Design.updateTextFont(mCounterNameView, Design.FONT_REGULAR26);
        mCounterNameView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mCounterNameView.setText(String.format(Locale.getDefault(), "0/%d", AbstractEditActivity.MAX_NAME_LENGTH));

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mCounterNameView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (AbstractEditActivity.DESIGN_COUNTER_TOP_MARGIN * Design.HEIGHT_RATIO);

        View descriptionContentView = findViewById(R.id.edit_group_activity_description_content_view);

        ShapeDrawable descriptionContentViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        descriptionContentViewBackground.getPaint().setColor(Design.EDIT_TEXT_BACKGROUND_COLOR);
        descriptionContentView.setBackground(descriptionContentViewBackground);

        layoutParams = descriptionContentView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = (int) Design.DESCRIPTION_CONTENT_VIEW_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) descriptionContentView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (AbstractEditActivity.DESIGN_DESCRIPTION_TOP_MARGIN * Design.HEIGHT_RATIO);

        mDescriptionView = findViewById(R.id.edit_group_activity_description_view);
        Design.updateTextFont(mDescriptionView, Design.FONT_REGULAR28);
        mDescriptionView.setTextColor(Design.EDIT_TEXT_TEXT_COLOR);
        mDescriptionView.setHintTextColor(Design.GREY_COLOR);
        mDescriptionView.setFilters(new InputFilter[]{new InputFilter.LengthFilter(AbstractEditActivity.MAX_DESCRIPTION_LENGTH)});
        mDescriptionView.addTextChangedListener(new TextWatcher() {

            @SuppressLint("DefaultLocale")
            @Override
            public void afterTextChanged(Editable s) {

                mCounterDescriptionView.setText(String.format(Locale.getDefault(), "%d/%d", s.length(), AbstractEditActivity.MAX_DESCRIPTION_LENGTH));
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

        mCounterDescriptionView = findViewById(R.id.edit_group_activity_counter_description_view);
        Design.updateTextFont(mCounterDescriptionView, Design.FONT_REGULAR26);
        mCounterDescriptionView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mCounterDescriptionView.setText(String.format(Locale.getDefault(), "0/%d", AbstractEditActivity.MAX_DESCRIPTION_LENGTH));

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mCounterDescriptionView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (AbstractEditActivity.DESIGN_COUNTER_TOP_MARGIN * Design.HEIGHT_RATIO);

        mSaveClickableView = findViewById(R.id.edit_group_activity_save_view);
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
        mSaveClickableView.setBackground(saveViewBackground);

        layoutParams = mSaveClickableView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = Design.BUTTON_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mSaveClickableView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_NAME_TOP_MARGIN * Design.HEIGHT_RATIO);

        mSaveTextView = findViewById(R.id.edit_group_activity_save_title_view);
        Design.updateTextFont(mSaveTextView, Design.FONT_BOLD28);
        mSaveTextView.setTextColor(Color.WHITE);

        View removeView = findViewById(R.id.edit_group_activity_remove_view);
        mRemoveListener = new RemoveListener();
        removeView.setOnClickListener(mRemoveListener);

        layoutParams = removeView.getLayoutParams();
        layoutParams.height = Design.BUTTON_HEIGHT;

        mRemoveLabelView = findViewById(R.id.edit_group_activity_remove_label_view);
        Design.updateTextFont(mRemoveLabelView, Design.FONT_REGULAR34);
        mRemoveLabelView.setTextColor(Design.FONT_COLOR_RED);

        mProgressBarView = findViewById(R.id.edit_group_activity_progress_bar);

        mUIInitialized = true;
    }

    private void onSaveClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSaveClick");
        }

        // The Save button can be clicked while we have not received the group yet!
        if (mGroup == null) {

            return;
        }

        hideKeyboard();

        mSaveClickableView.setAlpha(0.5f);

        String updatedName = mNameView.getText().toString().trim();
        if (updatedName.isEmpty()) {
            updatedName = mNameView.getHint().toString();
        }

        String updatedDescription = mDescriptionView.getText().toString().trim();

        if (!updatedName.equals(mGroup.getName()) || !updatedDescription.equals(mGroup.getDescription()) || mUpdatedGroupAvatar != null) {
            if (mGroup.isOwner()) {
                mGroupService.updateGroup(updatedName, updatedDescription, mUpdatedGroupAvatar, mUpdatedGroupAvatarFile);
            } else {
                mGroupService.updateGroup(updatedName, updatedDescription);
            }
        } else {
            finish();
        }
    }

    private void onRemoveClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRemoveClick");
        }

        if (mGroup == null) {

            return;
        }

        hideKeyboard();

        PercentRelativeLayout percentRelativeLayout = findViewById(R.id.edit_group_activity_layout);

        DeleteConfirmView deleteConfirmView = new DeleteConfirmView(this, null);
        PercentRelativeLayout.LayoutParams layoutParams = new PercentRelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        deleteConfirmView.setLayoutParams(layoutParams);
        deleteConfirmView.setConfirmTitle(getString(R.string.application_confirm));
        deleteConfirmView.setAvatar(mGroupAvatar, mGroupAvatar == null || mGroupAvatar.equals(getTwinmeApplication().getDefaultGroupAvatar()));
        String message = getString(R.string.show_group_activity_leave_message) + "\n\n"  + getString(R.string.show_group_activity_leave_confirm_message);

        if (mGroup.isOwner()){
            message = getString(R.string.show_group_activity_remove_message) + "\n\n"  + getString(R.string.show_group_activity_remove_confirm_message);
        }
        deleteConfirmView.setMessage(message);

        AbstractConfirmView.Observer observer = new AbstractConfirmView.Observer() {
            @Override
            public void onConfirmClick() {
                mGroupService.leaveGroup(mGroup.getMemberTwincodeOutboundId());
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

    private void updateGroup() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateContact");
        }

        if (!mUIInitialized || mGroup == null || mDisableUpdated) {

            return;
        }

        if (mGroup.getAvatarId() != null || mUpdatedGroupAvatar != null) {
            if (mUpdatedGroupAvatar != null) {
                mAvatarView.setImageBitmap(mUpdatedGroupAvatar);
            } else {
                mAvatarView.setImageBitmap(mGroupAvatar);
            }
            mNoAvatarView.setVisibility(View.GONE);
        } else {
            mNoAvatarView.setVisibility(View.VISIBLE);
            mAvatarView.setBackgroundColor(Design.AVATAR_PLACEHOLDER_COLOR);
        }

        mNameView.setHint(mGroupName);

        // If the nameView contains some text, this is a text entered by the user and the activity was restored.
        if (mNameView.getText().toString().isEmpty()) {
            mDisableUpdated = true;
            mNameView.append(mGroupName);
            mDisableUpdated = false;
        } else {
            setUpdated();
        }

        // If the descriptionView contains some text, this is a text entered by the user and the activity was restored.
        if (mDescriptionView.getText().toString().isEmpty()) {
            mDisableUpdated = true;
            mDescriptionView.setText(mGroupDescription);
            mDisableUpdated = false;
        } else {
            setUpdated();
        }

        if (mGroup.isOwner()) {
            mEditAvatarView.setVisibility(View.VISIBLE);
            mRemoveLabelView.setText(getString(R.string.application_remove));
        } else {
            mEditAvatarView.setVisibility(View.GONE);
            mRemoveLabelView.setText(getString(R.string.show_group_activity_leave));
        }
    }

    private void updateSelectedImage() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateSelectedImage");
        }

        mEditableView.getSelectedImage((String path, Bitmap bitmap, Bitmap largeImage) -> {
            mUpdatedGroupAvatar = bitmap;
            mUpdatedGroupAvatarFile = new File(path);

            mAvatarView.setImageBitmap(mUpdatedGroupAvatar);
            mAvatarView.setBackgroundColor(Color.TRANSPARENT);

            setUpdated();
        });
    }

    private void onNameViewClick() {
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

    private void onDescriptionViewClick() {
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

    private void setUpdated() {
        if (DEBUG) {
            Log.d(LOG_TAG, "setUpdated");
        }

        if (mDisableUpdated) {

            return;
        }

        if (mUpdated) {

            return;
        }
        mUpdated = true;

        mSaveClickableView.setAlpha(1.0f);
    }

    private void openMenuPhoto() {
        if (DEBUG) {
            Log.d(LOG_TAG, "openMenuPhoto");
        }

        if (mGroup == null || !mGroup.isOwner()) {
            return;
        }

        hideKeyboard();

        PercentRelativeLayout percentRelativeLayout = findViewById(R.id.edit_group_activity_layout);

        MenuPhotoView menuPhotoView = new MenuPhotoView(this, null);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        menuPhotoView.setLayoutParams(layoutParams);

        MenuPhotoView.Observer observer = new MenuPhotoView.Observer() {
            @Override
            public void onCameraClick() {

                menuPhotoView.animationCloseMenu();
                mEditableView.onCameraClick();
            }

            @Override
            public void onPhotoGalleryClick() {

                menuPhotoView.animationCloseMenu();
                mEditableView.onGalleryClick();
            }

            @Override
            public void onCloseMenuSelectActionAnimationEnd() {

                percentRelativeLayout.removeView(menuPhotoView);

                Window window = getWindow();
                window.setNavigationBarColor(Design.WHITE_COLOR);
            }
        };

        menuPhotoView.setObserver(observer);
        percentRelativeLayout.addView(menuPhotoView);

        List<UIMenuSelectAction> actions = new ArrayList<>();
        actions.add(new UIMenuSelectAction(getString(R.string.application_camera), R.drawable.grey_camera));
        actions.add(new UIMenuSelectAction(getString(R.string.application_photo_gallery), R.drawable.from_gallery));
        menuPhotoView.setActions(actions, this);
        menuPhotoView.openMenu(true);

        Window window = getWindow();
        window.setNavigationBarColor(Design.POPUP_BACKGROUND_COLOR);
    }

    private void hideKeyboard() {
        if (DEBUG) {
            Log.d(LOG_TAG, "hideKeyboard");
        }

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(mNameView.getWindowToken(), 0);
            inputMethodManager.hideSoftInputFromWindow(mDescriptionView.getWindowToken(), 0);
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

                if (newY > Design.CONTENT_VIEW_MIN_Y && newY < (Design.CONTENT_VIEW_INITIAL_POSITION)) {
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

    @Override
    public void updateFont() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateFont");
        }

        super.updateFont();

        if (!mUIInitialized) {
            return;
        }

        Design.updateTextFont(mTitleView, Design.FONT_BOLD44);
        Design.updateTextFont(mNameView, Design.FONT_REGULAR28);
        Design.updateTextFont(mCounterNameView, Design.FONT_REGULAR26);
        Design.updateTextFont(mDescriptionView, Design.FONT_REGULAR28);
        Design.updateTextFont(mCounterDescriptionView, Design.FONT_REGULAR26);
        Design.updateTextFont(mSaveTextView, Design.FONT_BOLD28);
        Design.updateTextFont(mRemoveLabelView, Design.FONT_REGULAR34);
    }

    @Override
    public void updateColor() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateColor");
        }

        super.updateColor();

        if (!mUIInitialized) {
            return;
        }

        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mNameView.setTextColor(Design.EDIT_TEXT_TEXT_COLOR);
        mNameView.setHintTextColor(Design.GREY_COLOR);
        mCounterNameView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mDescriptionView.setTextColor(Design.EDIT_TEXT_TEXT_COLOR);
        mDescriptionView.setHintTextColor(Design.GREY_COLOR);
        mCounterDescriptionView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mSaveTextView.setTextColor(Color.WHITE);
        mRemoveLabelView.setTextColor(Design.FONT_COLOR_RED);
    }
}
