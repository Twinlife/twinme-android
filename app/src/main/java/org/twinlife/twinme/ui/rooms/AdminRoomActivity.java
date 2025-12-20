/*
 *  Copyright (c) 2020-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.rooms;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.RoomConfig;
import org.twinlife.twinme.services.EditRoomService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractEditActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.contacts.DeleteConfirmView;
import org.twinlife.twinme.ui.profiles.MenuPhotoView;
import org.twinlife.twinme.utils.AbstractBottomSheetView;
import org.twinlife.twinme.utils.EditableView;
import org.twinlife.twinme.utils.RoundedView;
import org.twinlife.twinme.utils.UIMenuSelectAction;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class AdminRoomActivity extends AbstractEditActivity implements EditRoomService.Observer {
    private static final String LOG_TAG = "AdminRoomActivity";
    private static final boolean DEBUG = false;

    private static final int DESIGN_SETTINGS_TOP_MARGIN = 14;

    private class RemoveListener implements View.OnClickListener {

        private boolean disabled = false;

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


    private UUID mRoomId;
    private EditableView mEditableView;
    private ImageView mNoAvatarView;
    private boolean mCanSave = false;
    private boolean mSaveClicked = false;
    private boolean mUIInitialized = false;
    private boolean mUIPostInitialized = false;

    private String mRoomName;
    private Bitmap mRoomAvatar;
    private Bitmap mUpdatedRoomAvatar;
    private File mUpdatedRoomAvatarFile;
    private String mRoomWelcome;
    private EditRoomService mEditRoomService;

    private RemoveListener mRemoveListener;

    private Contact mRoom;
    private RoomConfig mRoomConfig;

    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String value = intent.getStringExtra(Intents.INTENT_CONTACT_ID);
        if (value != null) {
            mRoomId = UUID.fromString(value);
        }

        initViews();

        if (savedInstanceState != null) {
            if (mEditableView != null) {
                mEditableView.onCreate(savedInstanceState);
                updateSelectedImage();
            }
            updateRoom();
        }

        mEditRoomService = new EditRoomService(this, getTwinmeContext(), this, mRoomId);
    }

    @Override
    protected void onPause() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPause");
        }

        super.onPause();

        hideKeyboard();
    }

    //
    // Override Activity methods
    //

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        mEditRoomService.dispose();

        // Cleanup capture and cropped images.
        if (mEditableView != null) {
            mEditableView.onDestroy();
        }

        super.onDestroy();
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
    public void onWindowFocusChanged(boolean hasFocus) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onWindowFocusChanged: hasFocus=" + hasFocus);
        }

        if (hasFocus && mUIInitialized && !mUIPostInitialized) {
            postInitViews();
        }
    }

    //
    // Override TwinmeActivityImpl methods
    //

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

    //
    // EditRoomService.Observer methods
    //

    @Override
    public void onGetContactNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContactNotFound");
        }

        finish();
    }

    @Override
    public void onDeleteContact(@NonNull UUID roomId) {

        if (mRoom != null && mRoom.getId() == roomId) {
            finish();
        }
    }

    @Override
    public void onGetRoomConfig(@NonNull RoomConfig roomConfig) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetRoomConfig: " + roomConfig);
        }

        mRoomConfig = roomConfig;
        mRoomWelcome = mRoomConfig.getWelcome();

        if (mRoomWelcome != null && !mRoomWelcome.isEmpty()) {
            mDescriptionView.setHint(mRoomWelcome);
        }

        if (mRoomWelcome != null && mDescriptionView.getText().toString().isEmpty()) {
            mDescriptionView.append(mRoomWelcome);
        } else {
            setUpdated();
        }
    }

    @Override
    public void onGetRoomConfigNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetRoomConfigNotFound");
        }
    }

    @Override
    public void onGetContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContact");
        }

        mRoom = contact;

        if (mRoom.hasPeer()) {
            mRoomAvatar = avatar;
            if (mRoomAvatar == null) {
                mRoomAvatar = getDefaultAvatar();
            }

            mRoomName = contact.getName();
            mEditRoomService.getRoomConfig();

            updateRoom();
        } else {
            finish();
        }
    }

    @Override
    public void onUpdateContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateContact contact=" + contact);
        }

        if (!contact.getId().equals(mRoomId)) {

            return;
        }

        finish();
    }

    //
    // Private methods
    //

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.admin_room_activity);

        showToolBar(true);
        showBackButton(true);

        setTitle(getString(R.string.application_edit));

        mEditableView = new EditableView(this);

        mAvatarView = findViewById(R.id.admin_room_activity_avatar_view);
        mAvatarView.setBackgroundColor(Design.AVATAR_PLACEHOLDER_COLOR);

        mAvatarView.setOnClickListener(v -> openMenuPhoto());

        ViewGroup.LayoutParams layoutParams = mAvatarView.getLayoutParams();
        layoutParams.width = Design.AVATAR_MAX_WIDTH;
        layoutParams.height = Design.AVATAR_MAX_HEIGHT;

        mNoAvatarView = findViewById(R.id.admin_room_activity_no_avatar_view);
        mNoAvatarView.setVisibility(View.VISIBLE);

        mNoAvatarView.setOnClickListener(v -> openMenuPhoto());

        View backClickableView = findViewById(R.id.admin_room_activity_back_clickable_view);
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

        RoundedView backRoundedView = findViewById(R.id.admin_room_activity_back_rounded_view);
        backRoundedView.setColor(Design.BACK_VIEW_COLOR);

        mContentView = findViewById(R.id.admin_room_activity_content_view);
        mContentView.setY(Design.CONTENT_VIEW_INITIAL_POSITION);

        setBackground(mContentView);

        View slideMarkView = findViewById(R.id.admin_room_activity_slide_mark_view);
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

        TextView titleView = findViewById(R.id.admin_room_activity_title_view);
        Design.updateTextFont(titleView, Design.FONT_BOLD44);
        titleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        View headerView = findViewById(R.id.admin_room_activity_content_header_view);
        marginLayoutParams = (ViewGroup.MarginLayoutParams) headerView.getLayoutParams();
        marginLayoutParams.topMargin = Design.HEADER_VIEW_TOP_MARGIN;

        View nameContentView = findViewById(R.id.admin_room_activity_name_content_view);

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

        mNameView = findViewById(R.id.admin_room_activity_name_view);
        Design.updateTextFont(mNameView, Design.FONT_REGULAR28);
        mNameView.setTextColor(Design.EDIT_TEXT_TEXT_COLOR);
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

        mCounterNameView = findViewById(R.id.admin_room_activity_counter_name_view);
        Design.updateTextFont(mCounterNameView, Design.FONT_REGULAR26);
        mCounterNameView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mCounterNameView.setText(String.format(Locale.getDefault(), "0/%d", MAX_NAME_LENGTH));

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mCounterNameView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_COUNTER_TOP_MARGIN * Design.HEIGHT_RATIO);

        View descriptionContentView = findViewById(R.id.admin_room_activity_description_content_view);

        ShapeDrawable descriptionContentViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        descriptionContentViewBackground.getPaint().setColor(Design.EDIT_TEXT_BACKGROUND_COLOR);
        descriptionContentView.setBackground(descriptionContentViewBackground);

        layoutParams = descriptionContentView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = (int) Design.DESCRIPTION_CONTENT_VIEW_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) descriptionContentView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_DESCRIPTION_TOP_MARGIN * Design.HEIGHT_RATIO);

        mDescriptionView = findViewById(R.id.admin_room_activity_description_view);
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

        mCounterDescriptionView = findViewById(R.id.admin_room_activity_counter_description_view);
        Design.updateTextFont(mCounterDescriptionView, Design.FONT_REGULAR26);
        mCounterDescriptionView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mCounterDescriptionView.setText(String.format(Locale.getDefault(), "0/%d", MAX_DESCRIPTION_LENGTH));

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mCounterDescriptionView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_COUNTER_TOP_MARGIN * Design.HEIGHT_RATIO);

        TextView configurationTitleView = findViewById(R.id.admin_room_activity_configuration_title_view);
        Design.updateTextFont(configurationTitleView, Design.FONT_BOLD26);
        configurationTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) configurationTitleView.getLayoutParams();
        marginLayoutParams.topMargin = Design.TITLE_IDENTITY_TOP_MARGIN;

        View settingsView = findViewById(R.id.admin_room_activity_settings_view);
        layoutParams = settingsView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = Design.ITEM_VIEW_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) settingsView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_SETTINGS_TOP_MARGIN * Design.HEIGHT_RATIO);

        settingsView.setOnClickListener(view -> onSettingsViewClick());

        GestureDetector permissionGestureDetector = new GestureDetector(this, new ViewTapGestureDetector(ACTION_SETTINGS));
        settingsView.setOnTouchListener((v, motionEvent) -> {
            permissionGestureDetector.onTouchEvent(motionEvent);
            touchContent(motionEvent);
            return true;
        });

        TextView settingsTextView = findViewById(R.id.admin_room_activity_settings_text_view);
        Design.updateTextFont(settingsTextView, Design.FONT_REGULAR34);
        settingsTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        ImageView settingsImageView = findViewById(R.id.admin_room_activity_settings_image_view);
        settingsImageView.setColorFilter(Design.SHOW_ICON_COLOR);

        View inviteView = findViewById(R.id.admin_room_activity_invite_view);
        layoutParams = inviteView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = Design.ITEM_VIEW_HEIGHT;

        inviteView.setOnClickListener(view -> onInviteClick());

        GestureDetector inviteViewGestureDetector = new GestureDetector(this, new ViewTapGestureDetector(ACTION_INVITE));
        inviteView.setOnTouchListener((v, motionEvent) -> {
            inviteViewGestureDetector.onTouchEvent(motionEvent);
            touchContent(motionEvent);
            return true;
        });

        TextView inviteTextView = findViewById(R.id.admin_room_activity_invite_text_view);
        Design.updateTextFont(inviteTextView, Design.FONT_REGULAR34);
        inviteTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        ImageView inviteImageView = findViewById(R.id.admin_room_activity_invite_image_view);
        inviteImageView.setColorFilter(Design.SHOW_ICON_COLOR);

        View roomCodeView = findViewById(R.id.admin_room_activity_code_view);
        layoutParams = roomCodeView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = Design.ITEM_VIEW_HEIGHT;

        roomCodeView.setOnClickListener(view -> onRoomCodeClick());

        GestureDetector roomCodeViewGestureDetector = new GestureDetector(this, new ViewTapGestureDetector(ACTION_CODE));
        roomCodeView.setOnTouchListener((v, motionEvent) -> {
            roomCodeViewGestureDetector.onTouchEvent(motionEvent);
            touchContent(motionEvent);
            return true;
        });

        TextView roomCodeTextView = findViewById(R.id.admin_room_activity_code_text_view);
        Design.updateTextFont(roomCodeTextView, Design.FONT_REGULAR34);
        roomCodeTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        ImageView roomCodeImageView = findViewById(R.id.admin_room_activity_code_image_view);
        roomCodeImageView.setColorFilter(Design.SHOW_ICON_COLOR);

        mSaveClickableView = findViewById(R.id.admin_room_activity_save_view);
        mSaveClickableView.setAlpha(0.5f);
        mSaveClickableView.setOnClickListener(v -> onSaveClick());

        GestureDetector saveGestureDetector = new GestureDetector(this, new ViewTapGestureDetector(ACTION_SAVE));
        mSaveClickableView.setOnTouchListener((v, motionEvent) -> {
            saveGestureDetector.onTouchEvent(motionEvent);
            touchContent(motionEvent);
            return true;
        });

        layoutParams = mSaveClickableView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = Design.BUTTON_HEIGHT;

        ShapeDrawable saveViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        saveViewBackground.getPaint().setColor(Design.getMainStyle());
        mSaveClickableView.setBackground(saveViewBackground);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mSaveClickableView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_SAVE_TOP_MARGIN * Design.HEIGHT_RATIO);

        TextView saveTextView = findViewById(R.id.admin_room_activity_save_title_view);
        Design.updateTextFont(saveTextView, Design.FONT_BOLD28);
        saveTextView.setTextColor(Color.WHITE);

        View removeView = findViewById(R.id.admin_room_activity_remove_view);
        layoutParams = removeView.getLayoutParams();
        layoutParams.height = Design.BUTTON_HEIGHT;

        mRemoveListener = new RemoveListener();
        removeView.setOnClickListener(mRemoveListener);

        TextView removeTextView = findViewById(R.id.admin_room__activity_remove_text_view);
        Design.updateTextFont(removeTextView, Design.FONT_REGULAR34);
        removeTextView.setTextColor(Design.FONT_COLOR_RED);

        mProgressBarView = findViewById(R.id.admin_room_activity_progress_bar);

        mUIInitialized = true;
    }

    private void updateSelectedImage() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateSelectedImage");
        }

        mEditableView.getSelectedImage((String path, Bitmap bitmap, Bitmap largeImage) -> {
            mUpdatedRoomAvatarFile = new File(path);
            mUpdatedRoomAvatar = bitmap;
            updateRoom();
            setUpdated();
        });
    }

    private void setUpdated() {
        if (DEBUG) {
            Log.d(LOG_TAG, "setUpdated");
        }

        if (!mUIInitialized || mRoom == null) {

            return;
        }

        mRoomName = mNameView.getText().toString().trim();
        mRoomWelcome = mDescriptionView.getText().toString();

        boolean updatedRoomConfig = mRoomConfig != null && !mRoomWelcome.equals(mRoomConfig.getWelcome());

        if (mRoomName.equals(mRoom.getName()) && mUpdatedRoomAvatar == null && !updatedRoomConfig) {
            if (!mCanSave) {
                return;
            }
            mCanSave = false;
            mSaveClickableView.setAlpha(0.5f);
        } else {
            if (mCanSave) {
                return;
            }
            mCanSave = true;
            mSaveClickableView.setAlpha(1.0f);
        }
    }

    private void postInitViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "postInitViews");
        }

        mUIPostInitialized = true;
    }

    //
    // Implement GroupService.Observer methods
    //

    @Override
    protected void onSaveClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSaveClick");
        }

        if (!mCanSave) {
            return;
        }

        if (mSaveClicked) {
            return;
        }

        hideKeyboard();

        mSaveClicked = true;
        String updatedName = mNameView.getText().toString().trim();
        String updatedWelcome = mDescriptionView.getText().toString();
        if (updatedName.isEmpty()) {
            updatedName = mNameView.getHint().toString();
        }

        mEditRoomService.updateRoom(mRoom, updatedName, mUpdatedRoomAvatar, mUpdatedRoomAvatarFile, updatedWelcome);
    }

    private void updateRoom() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateContact");
        }

        if (!mUIInitialized || mRoom == null) {

            return;
        }

        mNameView.setHint(mRoomName);

        // If the nameView contains some text, this is a text entered by the user and the activity was restored.
        if (mNameView.getText().toString().isEmpty()) {
            mNameView.append(mRoomName);
        } else {
            setUpdated();
        }

        mNameView.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {

                setUpdated();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        Bitmap avatar;
        if (mUpdatedRoomAvatar != null) {
            avatar = mUpdatedRoomAvatar;
            setUpdated();
        } else {
            avatar = mRoomAvatar;
        }

        if (avatar != null) {
            mAvatarView.setImageBitmap(avatar);
            mNoAvatarView.setVisibility(View.GONE);
        } else {
            mAvatarView.setBackgroundColor(Design.AVATAR_PLACEHOLDER_COLOR);
            mNoAvatarView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onSettingsViewClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSettingsViewClick");
        }

        if (mRoom != null) {
            startActivity(SettingsRoomActivity.class, Intents.INTENT_CONTACT_ID, mRoom.getId());
        }
    }

    @Override
    protected void onInviteClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onInviteClick");
        }

        if (mRoom.getPublicPeerTwincodeOutboundId() != null) {
            Intent intent = new Intent(this, AddParticipantsRoomActivity.class);
            intent.putExtra(Intents.INTENT_CONTACT_ID, mRoomId.toString());
            startActivity(intent);
        }
    }

    @Override
    protected void onInvitationCodeClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onInvitationCodeClick");
        }

        if (mRoom.getPublicPeerTwincodeOutboundId() != null) {
            Intent intent = new Intent(this, InvitationRoomActivity.class);
            intent.putExtra(Intents.INTENT_CONTACT_ID, mRoom.getId().toString());
            intent.putExtra(Intents.INTENT_ROOM_NAME, mRoom.getName());
            startActivity(intent);
        }
    }

    @Override
    protected void onRemoveClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRemoveClick");
        }

        ViewGroup viewGroup = findViewById(R.id.admin_room_activity_layout);

        DeleteConfirmView deleteConfirmView = new DeleteConfirmView(this, null);
        deleteConfirmView.setTitle(mRoomName);
        deleteConfirmView.setConfirmTitle(getString(R.string.application_confirm));
        deleteConfirmView.setAvatar(mRoomAvatar, mRoomAvatar == null || mRoomAvatar.equals(getTwinmeApplication().getDefaultGroupAvatar()));
        String message = getString(R.string.application_delete_message);
        deleteConfirmView.setMessage(message);

        AbstractBottomSheetView.Observer observer = new AbstractBottomSheetView.Observer() {
            @Override
            public void onConfirmClick() {
                deleteConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCancelClick() {
                deleteConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onDismissClick() {
                deleteConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                viewGroup.removeView(deleteConfirmView);
                setStatusBarColor();

                if (fromConfirmAction) {
                    mEditRoomService.deleteRoom(mRoom);
                } else {
                    mRemoveListener.enable();
                }
            }
        };
        deleteConfirmView.setObserver(observer);
        viewGroup.addView(deleteConfirmView);
        deleteConfirmView.show();

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }

    private void onRoomCodeClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRoomCodeClick");
        }

        if (mRoom != null) {
            startActivity(SettingsRoomActivity.class, Intents.INTENT_CONTACT_ID, mRoom.getId());
        }
    }

    private void openMenuPhoto() {
        if (DEBUG) {
            Log.d(LOG_TAG, "openMenuPhoto");
        }

        hideKeyboard();

        ViewGroup viewGroup = findViewById(R.id.admin_room_activity_layout);

        MenuPhotoView menuPhotoView = new MenuPhotoView(this, null);

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

                viewGroup.removeView(menuPhotoView);

                Window window = getWindow();
                window.setNavigationBarColor(Design.WHITE_COLOR);
            }
        };

        menuPhotoView.setObserver(observer);
        viewGroup.addView(menuPhotoView);

        List<UIMenuSelectAction> actions = new ArrayList<>();
        actions.add(new UIMenuSelectAction(getString(R.string.application_camera), R.drawable.grey_camera));
        actions.add(new UIMenuSelectAction(getString(R.string.application_photo_gallery), R.drawable.from_gallery));
        menuPhotoView.setActions(actions, this);
        menuPhotoView.openMenu(true);

        Window window = getWindow();
        window.setNavigationBarColor(Design.POPUP_BACKGROUND_COLOR);
    }
}
