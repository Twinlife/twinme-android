/*
 *  Copyright (c) 2021-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
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
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.GestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.models.CallReceiver;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Group;
import org.twinlife.twinme.models.Profile;
import org.twinlife.twinme.models.Space;
import org.twinlife.twinme.services.EditIdentityService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.profiles.MenuPhotoView;
import org.twinlife.twinme.ui.profiles.MenuPropagatingProfileView;
import org.twinlife.twinme.ui.settingsActivity.MenuSelectValueView;
import org.twinlife.twinme.utils.EditableView;
import org.twinlife.twinme.utils.RoundedView;
import org.twinlife.twinme.utils.UIMenuSelectAction;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class EditProfileActivity extends AbstractEditActivity implements EditIdentityService.Observer, MenuSelectValueView.Observer {
    private static final String LOG_TAG = "EditProfileActivity";
    private static final boolean DEBUG = false;

    protected static final float DESIGN_PROPAGATE_VIEW_HEIGHT = 100f;
    protected static final float DESIGN_PROPAGATE_VIEW_TOP_MARGIN = 50f;
    protected static final float DESIGN_PROPAGATE_VIEW_BOTTOM_MARGIN = 30f;

    private EditableView mEditableView;

    private TextView mTitleView;
    private View mPropagateView;
    private TextView mPropagateTextView;
    private TextView mSaveTextView;

    private View mOverlayMenuView;
    private MenuSelectValueView mMenuSelectValueView;

    private boolean mUIInitialized = false;

    @Nullable
    private Profile mProfile;
    @Nullable
    private String mName;
    @Nullable
    private String mDescription;
    private boolean mDisableUpdated = false;
    private boolean mUpdated = false;
    private Bitmap mAvatar;
    private Bitmap mUpdatedProfileAvatar;
    private Bitmap mUpdatedProfileLargeAvatar;
    private File mUpdatedProfileAvatarFile;

    private EditIdentityService mEditIdentityService;

    private boolean mSaveProfile = true;


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

        UUID profileId = org.twinlife.twinlife.util.Utils.UUIDFromString(intent.getStringExtra(Intents.INTENT_PROFILE_ID));
        if (profileId == null) {

            finish();
            return;
        }

        initViews();

        if (savedInstanceState != null && mEditableView != null) {
            mEditableView.onCreate(savedInstanceState);
            updateSelectedImage();
        }

        mEditIdentityService = new EditIdentityService(this, getTwinmeContext(), this);
        mEditIdentityService.getProfile(profileId);

        updateIdentity();

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

    }

    @Override
    public void onCreateProfile(@NonNull Profile profile) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateProfile: profile=" + profile);
        }
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
    public void onGetContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContact: contact=" + contact);
        }
    }

    @Override
    public void onGetContactNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContactNotFound");
        }
    }

    @Override
    public void onGetGroup(@NonNull Group group) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetGroup: group=" + group);
        }
    }

    @Override
    public void onGetGroupNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetGroupNotFound");
        }
    }

    @Override
    public void onUpdateProfile(@NonNull Profile profile) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateProfile: profile=" + profile);
        }

        finish();
    }

    @Override
    public void onUpdateIdentityAvatar(@NonNull Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateAvatarProfile: avatar=" + avatar);
        }

        mAvatar = avatar;
        updateIdentity();
    }

    @Override
    public void onUpdateContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateContact: contact=" + contact);
        }
    }

    @Override
    public void onUpdateGroup(@NonNull Group group) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateGroup: group=" + group);
        }
    }

    @Override
    public void onUpdateCallReceiver(@NonNull CallReceiver callReceiver) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateCallReceiver: callReceiver=" + callReceiver);
        }
    }

    @Override
    public void onGetCallReceiver(@NonNull CallReceiver callReceiver) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetCallReceiver: callReceiver=" + callReceiver);
        }
    }

    @Override
    public void onGetCallReceiverNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetCallReceiverNotFound");
        }
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

    //MenuSelectValueView.Observer

    @Override
    public void onCloseMenuAnimationEnd() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCloseMenuAnimationEnd");
        }

        mMenuSelectValueView.setVisibility(View.INVISIBLE);
        mOverlayMenuView.setVisibility(View.INVISIBLE);

        Window window = getWindow();
        window.setNavigationBarColor(Design.WHITE_COLOR);
    }

    @Override
    public void onSelectValue(int value) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSelectValue: " + value);
        }

        closeMenuSelectValue();
        Settings.profileUpdateMode.setInt(value).save();
        updateProfileUpdateMode();
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
        setContentView(R.layout.edit_profile_activity);

        setTitle(getString(R.string.application_name));
        showToolBar(false);
        showBackButton(true);
        setBackgroundColor(Design.WHITE_COLOR);

        mEditableView = new EditableView(this);

        mAvatarView = findViewById(R.id.edit_profile_activity_avatar_view);

        mAvatarView.setOnClickListener(v -> openMenuPhoto());

        ViewGroup.LayoutParams layoutParams = mAvatarView.getLayoutParams();
        layoutParams.width = Design.AVATAR_MAX_WIDTH;
        layoutParams.height = Design.AVATAR_MAX_HEIGHT;

        View backClickableView = findViewById(R.id.edit_profile_activity_back_clickable_view);
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

        RoundedView backRoundedView = findViewById(R.id.edit_profile_activity_back_rounded_view);
        backRoundedView.setColor(Design.BACK_VIEW_COLOR);

        mContentView = findViewById(R.id.edit_profile_activity_content_view);
        mContentView.setY(Design.CONTENT_VIEW_INITIAL_POSITION);

        setBackground(mContentView);

        View slideMarkView = findViewById(R.id.edit_profile_activity_slide_mark_view);
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

        mTitleView = findViewById(R.id.edit_profile_activity_title_view);
        Design.updateTextFont(mTitleView, Design.FONT_BOLD44);
        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        View headerView = findViewById(R.id.edit_profile_activity_content_header_view);
        marginLayoutParams = (ViewGroup.MarginLayoutParams) headerView.getLayoutParams();
        marginLayoutParams.topMargin = Design.HEADER_VIEW_TOP_MARGIN;

        View nameContentView = findViewById(R.id.edit_profile_activity_name_content_view);

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

        mNameView = findViewById(R.id.edit_profile_activity_name_view);
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

        mCounterNameView = findViewById(R.id.edit_profile_activity_counter_name_view);
        Design.updateTextFont(mCounterNameView, Design.FONT_REGULAR26);
        mCounterNameView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mCounterNameView.setText(String.format(Locale.getDefault(), "0/%d", MAX_NAME_LENGTH));

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mCounterNameView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_COUNTER_TOP_MARGIN * Design.HEIGHT_RATIO);

        View descriptionContentView = findViewById(R.id.edit_profile_activity_description_content_view);

        ShapeDrawable descriptionContentViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        descriptionContentViewBackground.getPaint().setColor(Design.EDIT_TEXT_BACKGROUND_COLOR);
        descriptionContentView.setBackground(descriptionContentViewBackground);

        layoutParams = descriptionContentView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = (int) Design.DESCRIPTION_CONTENT_VIEW_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) descriptionContentView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_DESCRIPTION_TOP_MARGIN * Design.HEIGHT_RATIO);

        mDescriptionView = findViewById(R.id.edit_profile_activity_description_view);
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

        mCounterDescriptionView = findViewById(R.id.edit_profile_activity_counter_description_view);
        Design.updateTextFont(mCounterDescriptionView, Design.FONT_REGULAR26);
        mCounterDescriptionView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mCounterDescriptionView.setText(String.format(Locale.getDefault(), "0/%d", MAX_DESCRIPTION_LENGTH));

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mCounterDescriptionView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_COUNTER_TOP_MARGIN * Design.HEIGHT_RATIO);

        mPropagateView = findViewById(R.id.edit_profile_activity_propagate_view);
        mPropagateView.setOnClickListener(v -> onPropagateClick());

        layoutParams = mPropagateView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = (int) (DESIGN_PROPAGATE_VIEW_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mPropagateView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_PROPAGATE_VIEW_TOP_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_PROPAGATE_VIEW_BOTTOM_MARGIN * Design.HEIGHT_RATIO);

        mPropagateTextView = findViewById(R.id.edit_profile_activity_propagate_text_view);
        Design.updateTextFont(mPropagateTextView, Design.FONT_REGULAR32);
        mPropagateTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mSaveClickableView = findViewById(R.id.edit_profile_activity_save_view);
        mSaveClickableView.setOnClickListener(v -> onSaveClick());
        mSaveClickableView.setAlpha(0.5f);

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

        mSaveTextView = findViewById(R.id.edit_profile_activity_save_title_view);
        Design.updateTextFont(mSaveTextView, Design.FONT_BOLD28);
        mSaveTextView.setTextColor(Color.WHITE);

        mOverlayMenuView = findViewById(R.id.edit_profile_activity_overlay_view);
        mOverlayMenuView.setBackgroundColor(Design.OVERLAY_VIEW_COLOR);
        mOverlayMenuView.setOnClickListener(view -> closeMenu());

        mMenuSelectValueView = findViewById(R.id.edit_profile_activity_menu_select_value_view);
        mMenuSelectValueView.setVisibility(View.INVISIBLE);
        mMenuSelectValueView.setObserver(this);
        mMenuSelectValueView.setActivity(this);

        mProgressBarView = findViewById(R.id.edit_profile_activity_progress_bar);

        mUIInitialized = true;
    }

    private void onPropagateClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPropagateClick");
        }

        hideKeyboard();

        openMenuSelectValue();
    }

    @Override
    protected void onSaveClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSaveClick");
        }

        // Make sure we have a valid profile and default name.
        if (mProfile == null || mName == null || !mSaveProfile) {
            finish();
            return;
        }

        hideKeyboard();

        openMenuPropagatingProfileView();
    }

    private void saveProfile() {
        if (DEBUG) {
            Log.d(LOG_TAG, "saveProfile");
        }

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
                mSaveProfile = false;
                mEditIdentityService.updateProfile(mProfile, updatedIdentityName, updatedIdentityDescription, avatar, mUpdatedProfileAvatarFile);
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

            if (mName.length() > MAX_NAME_LENGTH) {
                mName = mName.substring(0, MAX_NAME_LENGTH);
            }
            mTitleView.setText(getString(R.string.edit_profile_activity_editing_profile));

            if (mProfile.getDescription() != null) {
                mDescription = mProfile.getDescription();
            } else {
                mDescription = "";
            }

            mPropagateView.setVisibility(View.VISIBLE);

            updateProfileUpdateMode();
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mSaveClickableView.getLayoutParams();
            marginLayoutParams.topMargin = 0;
        } else {
            mTitleView.setText(getString(R.string.application_profile));
            mPropagateView.setVisibility(View.GONE);
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mSaveClickableView.getLayoutParams();
            marginLayoutParams.topMargin = (int) (DESIGN_PROPAGATE_VIEW_TOP_MARGIN * Design.HEIGHT_RATIO);
        }

        if (mName == null || !mName.isEmpty()) {
            mNameView.setHint(getResources().getString(R.string.application_name_hint));
        }

        // If the nameView contains some text, this is a text entered by the user and the activity was restored.
        if (mNameView.getText().toString().isEmpty()) {
            // Set the name but temporarily disable the setUpdate() that is triggered by the text listener.
            mDisableUpdated = true;
            mNameView.setText(mName);
            mDisableUpdated = false;
        }

        // If the descriptionView contains some text, this is a text entered by the user and the activity was restored.
        if (mDescriptionView.getText().toString().isEmpty()) {
            // Set the name but temporarily disable the setUpdate() that is triggered by the text listener.
            mDisableUpdated = true;
            mDescriptionView.setText(mDescription);
            mDisableUpdated = false;
        }

        Bitmap avatar;
        if (mUpdatedProfileLargeAvatar != null) {
            avatar = mUpdatedProfileLargeAvatar;
            setUpdated();
        } else {
            avatar = mAvatar;
        }

        if (avatar != null) {
            mAvatarView.setImageBitmap(avatar);
        }
    }

    private void updateProfileUpdateMode() {
        if (DEBUG) {
            Log.d(LOG_TAG, "setUpdated");
        }

        String value;
        if (getTwinmeApplication().updateProfileMode() == Profile.UpdateMode.NONE.ordinal()) {
            value = getString(R.string.edit_profile_activity_propagating_no_contact);
        } else if (getTwinmeApplication().updateProfileMode() == Profile.UpdateMode.DEFAULT.ordinal()) {
            value = getString(R.string.edit_profile_activity_propagating_except_contacts);
        } else {
            value = getString(R.string.edit_profile_activity_propagating_all_contacts);
        }

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(getString(R.string.edit_profile_activity_propagating_profile));
        spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_DEFAULT), 0, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        spannableStringBuilder.append("\n");
        int startSubTitle = spannableStringBuilder.length();
        spannableStringBuilder.append(value);
        spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_GREY), startSubTitle, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        mPropagateTextView.setText(spannableStringBuilder);
    }

    private void setUpdated() {
        if (DEBUG) {
            Log.d(LOG_TAG, "setUpdated");
        }

        if (mDisableUpdated) {

            return;
        }

        if (mProfile == null) {
            String name = mNameView.getText().toString().trim();
            if (!name.isEmpty() && mUpdatedProfileAvatar != null) {
                mUpdated = true;
            } else {
                mUpdated = false;
            }
        } else {
            String updatedIdentityName = mNameView.getText().toString().trim();
            if (updatedIdentityName.isEmpty()) {
                updatedIdentityName = mName;
            }

            String updatedIdentityDescription = mDescriptionView.getText().toString().trim();

            boolean updated = !updatedIdentityName.equals(mName);
            updated = updated || !updatedIdentityDescription.equals(mDescription);
            updated = updated || mUpdatedProfileAvatar != null;

            mUpdated = updated;
        }

        if (mUpdated) {
            mSaveClickableView.setAlpha(1.0f);
        } else {
            mSaveClickableView.setAlpha(0.5f);
        }
    }

    private void openMenuSelectValue() {
        if (DEBUG) {
            Log.d(LOG_TAG, "openMenuSelectValue");
        }

        if (mMenuSelectValueView.getVisibility() == View.INVISIBLE) {
            mMenuSelectValueView.setVisibility(View.VISIBLE);
            mOverlayMenuView.setVisibility(View.VISIBLE);
            mMenuSelectValueView.openMenu(MenuSelectValueView.MenuType.PROFILE_UPDATE_MODE);

            Window window = getWindow();
            window.setNavigationBarColor(Design.POPUP_BACKGROUND_COLOR);
        }
    }

    private void openMenuPropagatingProfileView() {
        if (DEBUG) {
            Log.d(LOG_TAG, "openMenuPropagatingProfileView");
        }

        ViewGroup viewGroup = findViewById(R.id.edit_profile_activity_layout);

        MenuPropagatingProfileView menuPropagatingProfileView = new MenuPropagatingProfileView(this, null);

        MenuPropagatingProfileView.Observer observer = new MenuPropagatingProfileView.Observer() {
            @Override
            public void onCloseMenuAnimationEnd() {

                menuPropagatingProfileView.animationCloseMenu();
                saveProfile();

                Window window = getWindow();
                window.setNavigationBarColor(Design.WHITE_COLOR);
            }

            @Override
            public void onSelectValue(int value) {

                menuPropagatingProfileView.animationCloseMenu();

                if (value == Profile.UpdateMode.NONE.ordinal()) {
                    getTwinmeApplication().setUpdateProfileMode(Profile.UpdateMode.NONE);
                } else if (value == Profile.UpdateMode.DEFAULT.ordinal()) {
                    getTwinmeApplication().setUpdateProfileMode(Profile.UpdateMode.DEFAULT);
                } else if (value == Profile.UpdateMode.ALL.ordinal()) {
                    getTwinmeApplication().setUpdateProfileMode(Profile.UpdateMode.ALL);
                }
                saveProfile();

                Window window = getWindow();
                window.setNavigationBarColor(Design.WHITE_COLOR);
            }
        };

        menuPropagatingProfileView.setObserver(observer);
        viewGroup.addView(menuPropagatingProfileView);

        menuPropagatingProfileView.openMenu(getTwinmeApplication().updateProfileMode());

        Window window = getWindow();
        window.setNavigationBarColor(Design.POPUP_BACKGROUND_COLOR);
    }

    private void closeMenu() {
        if (DEBUG) {
            Log.d(LOG_TAG, "closeMenu");
        }

        if (mMenuSelectValueView.getVisibility() == View.VISIBLE) {
            closeMenuSelectValue();
        }
    }

    private void closeMenuSelectValue() {
        if (DEBUG) {
            Log.d(LOG_TAG, "closeMenuSelectValue");
        }

        mMenuSelectValueView.animationCloseMenu();
    }

    private void openMenuPhoto() {
        if (DEBUG) {
            Log.d(LOG_TAG, "openMenuPhoto");
        }

        hideKeyboard();

        ViewGroup viewGroup = findViewById(R.id.edit_profile_activity_layout);

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
        menuPhotoView.openMenu(false);

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
        Design.updateTextFont(mPropagateTextView, Design.FONT_REGULAR32);
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
        mPropagateTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mSaveTextView.setTextColor(Color.WHITE);
    }
}
