/*
 *  Copyright (c) 2015-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui;

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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.TwinmeContext;
import org.twinlife.twinme.models.CallReceiver;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Group;
import org.twinlife.twinme.models.Profile;
import org.twinlife.twinme.models.Space;
import org.twinlife.twinme.services.EditIdentityService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.profiles.MenuPhotoView;
import org.twinlife.twinme.utils.EditableView;
import org.twinlife.twinme.utils.RoundedView;
import org.twinlife.twinme.utils.UIMenuSelectAction;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class EditIdentityActivity extends AbstractEditActivity implements EditIdentityService.Observer {
    private static final String LOG_TAG = "EditIdentityActivity";
    private static final boolean DEBUG = false;

    private TextView mTitleView;
    private EditableView mEditableView;
    private View mDescriptionContentView;
    private TextView mSaveTextView;

    private boolean mUIInitialized = false;
    private Profile mProfile;
    private Contact mContact;
    private Group mGroup;
    private CallReceiver mCallReceiver;
    private String mName;
    private String mDescription;
    private boolean mDisableUpdated = false;
    private boolean mUpdated = false;
    private Bitmap mAvatar;
    private Bitmap mUpdatedProfileAvatar;
    private Bitmap mUpdatedProfileLargeAvatar;
    private File mUpdatedProfileAvatarFile;

    private EditIdentityService mEditIdentityService;

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

        if (savedInstanceState != null && mEditableView != null) {
            mEditableView.onCreate(savedInstanceState);
            updateSelectedImage();
        }

        updateIdentity();

        mEditIdentityService = new EditIdentityService(this, getTwinmeContext(), this);

        Intent intent = getIntent();
        String profileId = intent.getStringExtra(Intents.INTENT_PROFILE_ID);
        String contactId = intent.getStringExtra(Intents.INTENT_CONTACT_ID);
        String receiverId = intent.getStringExtra(Intents.INTENT_CALL_RECEIVER_ID);
        String groupId = intent.getStringExtra(Intents.INTENT_GROUP_ID);

        if (profileId != null) {
            mEditIdentityService.getProfile(UUID.fromString(profileId));
        } else if (contactId != null) {
            mEditIdentityService.getContact(UUID.fromString(contactId));
        } else if (groupId != null) {
            mEditIdentityService.getGroup(UUID.fromString(groupId));
        } else if (receiverId != null) {
            mEditIdentityService.getCallReceiver(UUID.fromString(receiverId));
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

        mEditIdentityService.dispose();

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
    // Implement EditIdentityService.Observer methods
    //


    @Override
    public void onGetSpace(@NonNull Space space, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetSpace: space=" + space);
        }

    }

    @Override
    public void onUpdateSpace(@NonNull Space space) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateSpace: space=" + space);
        }

        mProfile = space.getProfile();

        updateIdentity();

    }

    @Override
    public void onCreateProfile(@NonNull Profile profile) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateProfile: profile=" + profile);
        }

        mProfile = profile;

        updateIdentity();
    }

    @Override
    public void onGetProfile(@NonNull Profile profile, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetProfile: profile=" + profile);
        }

        mProfile = profile;
        mAvatar = avatar;

        updateIdentity();
    }

    @Override
    public void onGetProfileNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetProfileNotFound");
        }

        finish();
    }

    @Override
    public void onUpdateIdentityAvatar(@NonNull Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateIdentityAvatar: avatar=" + avatar);
        }

        mAvatar = avatar;

        updateIdentity();
    }

    @Override
    public void onGetContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContact: contact=" + contact);
        }

        mContact = contact;
        updateIdentity();
    }

    @Override
    public void onGetContactNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContactNotFound");
        }

        finish();
    }

    @Override
    public void onGetGroup(@NonNull Group group) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetGroup: group=" + group);
        }

        mGroup = group;
        updateIdentity();
    }

    @Override
    public void onGetGroupNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetGroupNotFound");
        }

        finish();
    }

    @Override
    public void onUpdateProfile(@NonNull Profile profile) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateProfile: profile=" + profile);
        }

        finish();
    }

    @Override
    public void onUpdateContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateContact: contact=" + contact);
        }

        finish();
    }

    @Override
    public void onUpdateGroup(@NonNull Group group) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateGroup: group=" + group);
        }

        finish();
    }

    @Override
    public void onGetCallReceiver(@NonNull CallReceiver callReceiver) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetCallReceiver: callReceiver=" + callReceiver);
        }

        mCallReceiver = callReceiver;
        updateIdentity();
    }

    @Override
    public void onGetCallReceiverNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetCallReceiverNotFound");
        }

        finish();
    }

    @Override
    public void onUpdateCallReceiver(@NonNull CallReceiver callReceiver) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateCallReceiver: callReceiver=" + callReceiver);
        }

        finish();
    }

    //
    // Private methods
    //

    private void updateSelectedImage() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateSelectedImage");
        }

        mEditableView.getSelectedImage((String path, Bitmap bitmap, Bitmap largeImage) -> {
            mUpdatedProfileAvatarFile = new File(path);
            mUpdatedProfileAvatar = bitmap;
            mUpdatedProfileLargeAvatar = largeImage;
            updateIdentity();
        });
    }

    @SuppressLint({"ClickableViewAccessibility"})
    @Override
    protected void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.edit_identity_activity);

        showToolBar(true);
        showBackButton(true);

        setTitle(getString(R.string.application_profile));

        mEditableView = new EditableView(this);

        mAvatarView = findViewById(R.id.edit_identity_activity_avatar_view);
        mAvatarView.setOnClickListener(v -> openMenuPhoto());

        ViewGroup.LayoutParams layoutParams = mAvatarView.getLayoutParams();
        layoutParams.width = Design.AVATAR_MAX_WIDTH;
        layoutParams.height = Design.AVATAR_MAX_HEIGHT;

        View backClickableView = findViewById(R.id.edit_identity_activity_back_clickable_view);
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

        RoundedView backRoundedView = findViewById(R.id.edit_identity_activity_back_rounded_view);
        backRoundedView.setColor(Design.BACK_VIEW_COLOR);

        mContentView = findViewById(R.id.edit_identity_activity_content_view);
        mContentView.setY(Design.CONTENT_VIEW_INITIAL_POSITION);

        setBackground(mContentView);

        View slideMarkView = findViewById(R.id.edit_identity_activity_slide_mark_view);
        layoutParams = slideMarkView.getLayoutParams();
        layoutParams.height = Design.SLIDE_MARK_HEIGHT;

        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.mutate();
        gradientDrawable.setColor(Color.rgb(244, 244, 244));
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        slideMarkView.setBackground(gradientDrawable);

        float corner = ((float) Design.SLIDE_MARK_HEIGHT / 2) * Resources.getSystem().getDisplayMetrics().density;
        gradientDrawable.setCornerRadius(corner);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) slideMarkView.getLayoutParams();
        marginLayoutParams.topMargin = Design.SLIDE_MARK_TOP_MARGIN;

        mContentView.setOnTouchListener((v, motionEvent) -> touchContent(motionEvent));

        mTitleView = findViewById(R.id.edit_identity_activity_title_view);
        Design.updateTextFont(mTitleView, Design.FONT_BOLD44);
        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        View headerView = findViewById(R.id.edit_identity_activity_content_header_view);
        marginLayoutParams = (ViewGroup.MarginLayoutParams) headerView.getLayoutParams();
        marginLayoutParams.topMargin = Design.HEADER_VIEW_TOP_MARGIN;

        View nameContentView = findViewById(R.id.edit_identity_activity_name_content_view);

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

        mNameView = findViewById(R.id.edit_identity_activity_name_view);
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

        mCounterNameView = findViewById(R.id.edit_identity_activity_counter_name_view);
        Design.updateTextFont(mCounterNameView, Design.FONT_REGULAR26);
        mCounterNameView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mCounterNameView.setText(String.format(Locale.getDefault(), "0/%d", MAX_NAME_LENGTH));

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mCounterNameView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_COUNTER_TOP_MARGIN * Design.HEIGHT_RATIO);

        mDescriptionContentView = findViewById(R.id.edit_identity_activity_description_content_view);

        ShapeDrawable descriptionContentViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        descriptionContentViewBackground.getPaint().setColor(Design.EDIT_TEXT_BACKGROUND_COLOR);
        mDescriptionContentView.setBackground(descriptionContentViewBackground);

        layoutParams = mDescriptionContentView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = (int) Design.DESCRIPTION_CONTENT_VIEW_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mDescriptionContentView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_DESCRIPTION_TOP_MARGIN * Design.HEIGHT_RATIO);

        mDescriptionView = findViewById(R.id.edit_identity_activity_description_view);
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

        mCounterDescriptionView = findViewById(R.id.edit_identity_activity_counter_description_view);
        Design.updateTextFont(mCounterDescriptionView, Design.FONT_REGULAR26);
        mCounterDescriptionView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mCounterDescriptionView.setText(String.format(Locale.getDefault(), "0/%d", MAX_DESCRIPTION_LENGTH));

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mCounterDescriptionView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_COUNTER_TOP_MARGIN * Design.HEIGHT_RATIO);

        mSaveClickableView = findViewById(R.id.edit_identity_activity_save_view);
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
        marginLayoutParams.topMargin = (int) (DESIGN_SAVE_TOP_MARGIN * Design.HEIGHT_RATIO);

        mSaveTextView = findViewById(R.id.edit_identity_activity_save_title_view);
        Design.updateTextFont(mSaveTextView, Design.FONT_BOLD28);
        mSaveTextView.setTextColor(Color.WHITE);

        mProgressBarView = findViewById(R.id.edit_identity_activity_progress_bar);

        mUIInitialized = true;
    }

    @Override
    protected void onSaveClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSaveClick");
        }

        hideKeyboard();
        String updatedIdentityName = mNameView.getText().toString().trim();
        if (updatedIdentityName.isEmpty()) {
            updatedIdentityName = mName;
        }

        String updatedIdentityDescription = mDescriptionView.getText().toString().trim();

        boolean updated = !updatedIdentityName.equals(mName);
        updated = updated || !updatedIdentityDescription.equals(mDescription);
        updated = updated || mUpdatedProfileAvatar != null;

        if (updated) {
            Bitmap avatar = mUpdatedProfileAvatar;
            if (avatar == null) {
                avatar = mAvatar;
            }
            if (mProfile != null) {
                mEditIdentityService.updateProfile(mProfile, updatedIdentityName, updatedIdentityDescription, avatar, mUpdatedProfileAvatarFile);
            } else if (mContact != null) {
                mEditIdentityService.updateContact(mContact, updatedIdentityName, updatedIdentityDescription, avatar, mUpdatedProfileAvatarFile);
            } else if (mGroup != null) {
                mEditIdentityService.updateGroup(mGroup, updatedIdentityName, avatar, mUpdatedProfileAvatarFile);
            } else if (mCallReceiver != null) {
                mEditIdentityService.updateCallReceiver(mCallReceiver, updatedIdentityName, updatedIdentityDescription, avatar, mUpdatedProfileAvatarFile);
            }
        }
    }

    private void updateIdentity() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateIdentity");
        }

        if (!mUIInitialized) {

            return;
        }

        if (mProfile != null) {
            mName = mProfile.getName();
            mDescription = mProfile.getDescription();
        } else if (mContact != null) {
            mName = mContact.getIdentityName();
            mDescription = mContact.getIdentityDescription();
            if (mName == null) {
                mName = getTwinmeApplication().getAnonymousName();
            }
        } else if (mGroup != null) {
            mName = mGroup.getIdentityName();
            mDescriptionContentView.setVisibility(View.GONE);
            mCounterDescriptionView.setVisibility(View.GONE);
        } else if (mCallReceiver != null) {
            mName = mCallReceiver.getIdentityName();
            mDescription = mCallReceiver.getIdentityDescription();
            if (mName == null) {
                mName = getTwinmeApplication().getAnonymousName();
            }
        }

        if (mName == null || !mName.isEmpty()) {
            mNameView.setHint(getResources().getString(R.string.application_name_hint));
        }

        if (mName != null && mName.length() > MAX_NAME_LENGTH) {
            mName = mName.substring(0, MAX_NAME_LENGTH);
        }

        mTitleView.setText(mName);

        // If the nameView contains some text, this is a text entered by the user and the activity was restored.
        if (mNameView.getText().toString().isEmpty()) {
            // Set the name but temporarily disable the setUpdate() that is triggered by the text listener.
            mDisableUpdated = true;
            mNameView.setText(mName);
            mDisableUpdated = false;
        } else {
            setUpdated();
        }

        // If the descriptionView contains some text, this is a text entered by the user and the activity was restored.
        if (mDescriptionView.getText().toString().isEmpty()) {
            // Set the name but temporarily disable the setUpdate() that is triggered by the text listener.
            mDisableUpdated = true;
            mDescriptionView.setText(mDescription);
            mDisableUpdated = false;
        } else {
            setUpdated();
        }

        TwinmeContext.Consumer<Bitmap> avatarConsumer = (Bitmap avatar) -> {
            mAvatar = avatar;

            if (mUpdatedProfileLargeAvatar != null) {
                avatar = mUpdatedProfileLargeAvatar;
                setUpdated();
            } else if (mUpdatedProfileAvatar != null) {
                avatar = mUpdatedProfileAvatar;
                setUpdated();
            }
            if (avatar != null) {
                mAvatarView.setImageBitmap(avatar);
            }
        };

        if (mAvatar != null) {
            avatarConsumer.accept(mAvatar);
        } else {
            if (mProfile != null) {
                mEditIdentityService.getProfileImage(mProfile, avatarConsumer);
            } else if (mContact != null) {
                mEditIdentityService.getIdentityImage(mContact, avatarConsumer);
            } else if (mGroup != null) {
                mEditIdentityService.getIdentityImage(mGroup, avatarConsumer);
            } else if (mCallReceiver != null) {
                mEditIdentityService.getIdentityImage(mCallReceiver, avatarConsumer);
            }
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

        hideKeyboard();

        ViewGroup viewGroup = findViewById(R.id.edit_identity_activity_layout);

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
        mNameView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mNameView.setHintTextColor(Design.GREY_COLOR);
        mCounterNameView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mDescriptionView.setTextColor(Design.EDIT_TEXT_TEXT_COLOR);
        mDescriptionView.setHintTextColor(Design.GREY_COLOR);
        mCounterDescriptionView.setTextColor(Design.FONT_COLOR_DEFAULT);
    }
}
