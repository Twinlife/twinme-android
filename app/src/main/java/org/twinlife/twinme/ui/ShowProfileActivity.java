/*
 *  Copyright (c) 2022-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.percentlayout.widget.PercentRelativeLayout;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.models.CallReceiver;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Group;
import org.twinlife.twinme.models.Profile;
import org.twinlife.twinme.models.Space;
import org.twinlife.twinme.services.EditIdentityService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.contacts.MenuAddContactView;
import org.twinlife.twinme.utils.EditableView;
import org.twinlife.twinme.utils.OnboardingDialog;
import org.twinlife.twinme.utils.RoundedView;
import org.twinlife.twinme.utils.UIMenuSelectAction;
import org.twinlife.twinme.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class ShowProfileActivity extends AbstractTwinmeActivity implements EditIdentityService.Observer {
    private static final String LOG_TAG = "ShowProfileActivity";
    private static final boolean DEBUG = false;

    private final int ACTION_BACK = 0;
    private final int ACTION_SAVE = 1;
    private final int ACTION_EDIT_NAME = 2;
    private final int ACTION_EDIT_DESCRIPTION = 3;
    private final int ACTION_EDIT_AVATAR = 4;
    private final int ACTION_EDIT_PROFILE = 5;
    private final int ACTION_TWINCODE = 6;

    class ViewTapGestureDetector extends GestureDetector.SimpleOnGestureListener {

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

                case ACTION_EDIT_AVATAR:
                    onAvatarClick();
                    break;

                case ACTION_EDIT_PROFILE:
                    onEditProfileClick();
                    break;

                case ACTION_TWINCODE:
                    onTwincodeClick();
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

    private static final String SHOW_ONBOARDING = "showOnboarding";

    private static final int MAX_NAME_LENGTH = 32;
    private static final int MAX_DESCRIPTION_LENGTH = 128;

    private static final int DESIGN_ROUNDED_VIEW_TOP_MARGIN = 85;
    private static final int DESIGN_TWINCODE_VIEW_TOP_MARGIN = 66;
    private static final int DESIGN_MESSAGE_VIEW_TOP_MARGIN = 60;
    private static final float DESIGN_NAME_TOP_MARGIN = 40f;
    private static final float DESIGN_SAVE_TOP_MARGIN = 52f;
    private static final float DESIGN_DESCRIPTION_TOP_MARGIN = 44f;
    private static final float DESIGN_COUNTER_TOP_MARGIN = 2f;

    private EditableView mEditableView;
    private View mContentView;
    private View mTwincodeView;
    private View mAddContactView;
    private TextView mMessageView;
    private View mEditView;
    private TextView mDescriptionTextView;
    private ImageView mAvatarView;
    private ImageView mNoAvatarView;
    private TextView mTitleView;
    private View mNameContentView;
    private EditText mNameView;
    private View mDescriptionContentView;
    private EditText mDescriptionView;
    private TextView mCounterNameView;
    private TextView mCounterDescriptionView;
    private View mSaveClickableView;

    private boolean mUIInitialized = false;
    private boolean mShowOnboarding = false;

    @Nullable
    private Profile mProfile;

    @Nullable
    private String mName;
    private String mDescription;
    private boolean mDisableUpdated = false;
    private boolean mUpdated = false;
    private Bitmap mAvatar;
    private Bitmap mUpdatedProfileAvatar;
    private Bitmap mUpdatedProfileLargeAvatar;
    private File mUpdatedProfileAvatarFile;
    private ProgressBar mProgressBarView;

    private EditIdentityService mEditIdentityService;

    private float mContentViewDY;

    private boolean mCreateProfile = false;

    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        mEditIdentityService = new EditIdentityService(this, getTwinmeContext(), this);

        initViews();

        if (savedInstanceState != null && mEditableView != null) {
            mShowOnboarding = savedInstanceState.getBoolean(SHOW_ONBOARDING);
            mEditableView.onCreate(savedInstanceState);
            updateSelectedImage();
        }

        updateIdentity();

        Intent intent = getIntent();
        UUID profileId = Utils.UUIDFromString(intent.getStringExtra(Intents.INTENT_PROFILE_ID));

        if (profileId != null) {
            mEditIdentityService.getProfile(profileId);
        } else {
            mCreateProfile = true;
        }

        showProgressIndicator();
    }

    //
    // Override Fragment methods
    //

    @Override
    public void onPause() {

        super.onPause();

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(mNameView.getWindowToken(), 0);
        }
    }

    @Override
    public void onDestroy() {
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
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSaveInstanceState: outState=" + outState);
        }

        super.onSaveInstanceState(outState);

        if (mEditableView != null) {
            mEditableView.onSaveInstanceState(outState);
        }

        outState.putBoolean(SHOW_ONBOARDING, mShowOnboarding);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
    public void onRequestPermissions(@NonNull TwinmeActivity.Permission[] grantedPermissions) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRequestPermissions grantedPermissions=" + Arrays.toString(grantedPermissions));
        }

        if (!mEditableView.onRequestPermissions(grantedPermissions)) {
            message(getString(R.string.application_denied_permissions), 0L, new TwinmeActivity.DefaultMessageCallback(R.string.application_ok) {
            });
        }
    }

    //
    // Implement EditIdentityService.Observer methods
    //

    @Override
    public void showProgressIndicator() {
        if (DEBUG) {
            Log.d(LOG_TAG, "showProgressIndicator");
        }

        if (mProgressBarView != null && mProgressBarView.getVisibility() != View.VISIBLE) {
            mProgressBarView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void hideProgressIndicator() {
        if (DEBUG) {
            Log.d(LOG_TAG, "hideProgressIndicator");
        }

        if (mProgressBarView != null && mProgressBarView.getVisibility() == View.VISIBLE) {
            mProgressBarView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreateProfile(@NonNull Profile profile) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateProfile: profile=" + profile);
        }

        mProfile = profile;

        if (mUIInitialized) {
            mNameView.setText("");
            mDescriptionView.setText("");
            mProgressBarView.setVisibility(View.GONE);
        }

        mEditIdentityService.getProfileImage(mProfile, (Bitmap avatar) -> {
            mAvatar = avatar;
            updateIdentity();

            finish();
        });
    }

    @Override
    public void onGetProfile(@NonNull Profile profile, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetProfile: profile=" + profile);
        }

        mProfile = profile;
        mAvatar = avatar;

        if (mUIInitialized) {
            mDisableUpdated = true;
            mNameView.setText("");
            mDescriptionView.setText("");
            mDisableUpdated = false;
        }

        setFullscreen();
        updateIdentity();
    }

    @Override
    public void onGetProfileNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetProfileNotFound");
        }

        if (mCreateProfile && !mShowOnboarding) {
            mShowOnboarding = true;

            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.onboarding_profile);
            DialogInterface.OnCancelListener dialogCancelListener = dialog -> {
            };
            OnboardingDialog onboardingDialog = new OnboardingDialog(this);
            onboardingDialog.setOnCancelListener(dialogCancelListener);
            onboardingDialog.setup(Html.fromHtml(getString(R.string.create_profile_activity_onboarding_message)), bitmap,
                    getString(R.string.application_ok),
                    onboardingDialog::dismiss
            );
            onboardingDialog.show();
        }
    }

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

        if (mUIInitialized) {
            mNameView.setText("");
            mDescriptionView.setText("");
        }

        mEditIdentityService.getProfileImage(mProfile, (Bitmap avatar) -> {
            mAvatar = avatar;
            updateIdentity();
        });
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

        if (mProfile != null && profile.getId().equals(mProfile.getId())) {
            mProfile = profile;

            if (mUIInitialized) {
                mNameView.setText("");
                mDescriptionView.setText("");
            }

            updateIdentity();
        }
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
            mUpdatedProfileAvatar = bitmap;
            mUpdatedProfileLargeAvatar = largeImage;
            mUpdatedProfileAvatarFile = new File(path);
            mUpdated = true;
            mNoAvatarView.setVisibility(View.GONE);
            updateIdentity();
        });
    }

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.show_profile_activity);

        setTitle(getString(R.string.application_name));
        showToolBar(false);
        showBackButton(true);
        setBackgroundColor(Design.WHITE_COLOR);

        applyInsets(R.id.show_profile_activity_layout, -1, -1, Design.WHITE_COLOR, true);

        mEditableView = new EditableView(this);

        mAvatarView = findViewById(R.id.show_profile_activity_avatar_view);
        mAvatarView.setBackgroundColor(Design.AVATAR_PLACEHOLDER_COLOR);

        GestureDetector avatarGestureDetector = new GestureDetector(this, new ViewTapGestureDetector(ACTION_EDIT_AVATAR));
        mAvatarView.setOnTouchListener((v, event) -> avatarGestureDetector.onTouchEvent(event));

        ViewGroup.LayoutParams layoutParams = mAvatarView.getLayoutParams();
        layoutParams.width = Design.AVATAR_MAX_WIDTH;
        layoutParams.height = Design.AVATAR_MAX_HEIGHT;

        mNoAvatarView = findViewById(R.id.show_profile_activity_no_avatar_view);
        mNoAvatarView.setVisibility(View.VISIBLE);

        mNoAvatarView.setOnClickListener(v -> mEditableView.onClick());

        mAddContactView = findViewById(R.id.show_profile_activity_add_contact_view);
        mAddContactView.setOnClickListener(v -> onAddContactClick());

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mAddContactView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_ROUNDED_VIEW_TOP_MARGIN * Design.HEIGHT_RATIO);

        RoundedView addContactRoundedView = findViewById(R.id.show_profile_activity_add_contact_rounded_view);
        addContactRoundedView.setColor(Color.argb(76, 0, 0, 0));

        mContentView = findViewById(R.id.show_profile_activity_content_view);
        mContentView.setY(Design.CONTENT_VIEW_INITIAL_POSITION);

        setBackground(mContentView);

        View slideMarkView = findViewById(R.id.show_profile_activity_slide_mark_view);
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

        View backClickableView = findViewById(R.id.show_profile_activity_back_clickable_view);
        GestureDetector backGestureDetector = new GestureDetector(this, new ViewTapGestureDetector(ACTION_BACK));
        backClickableView.setOnTouchListener((v, motionEvent) -> {
            backGestureDetector.onTouchEvent(motionEvent);
            touchContent(motionEvent);
            return true;
        });

        layoutParams = backClickableView.getLayoutParams();
        layoutParams.height = Design.BACK_CLICKABLE_VIEW_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) backClickableView.getLayoutParams();
        marginLayoutParams.leftMargin = Design.BACK_CLICKABLE_VIEW_LEFT_MARGIN;
        marginLayoutParams.topMargin = Design.BACK_CLICKABLE_VIEW_TOP_MARGIN;

        RoundedView backRoundedView = findViewById(R.id.show_profile_activity_back_rounded_view);
        backRoundedView.setColor(Design.BACK_VIEW_COLOR);

        mContentView.setOnTouchListener((v, motionEvent) -> touchContent(motionEvent));

        mTitleView = findViewById(R.id.show_profile_activity_title_view);
        Design.updateTextFont(mTitleView, Design.FONT_BOLD44);
        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        View headerView = findViewById(R.id.show_profile_activity_content_header_view);
        marginLayoutParams = (ViewGroup.MarginLayoutParams) headerView.getLayoutParams();
        marginLayoutParams.topMargin = Design.HEADER_VIEW_TOP_MARGIN;

        mNameContentView = findViewById(R.id.show_profile_activity_name_content_view);

        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable nameViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        nameViewBackground.getPaint().setColor(Design.EDIT_TEXT_BACKGROUND_COLOR);
        mNameContentView.setBackground(nameViewBackground);

        layoutParams = mNameContentView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = Design.BUTTON_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mNameContentView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_NAME_TOP_MARGIN * Design.HEIGHT_RATIO);

        mNameView = findViewById(R.id.show_profile_activity_name_view);
        Design.updateTextFont(mNameView, Design.FONT_REGULAR28);
        mNameView.setTextColor(Design.EDIT_TEXT_TEXT_COLOR);
        mNameView.setHintTextColor(Design.GREY_COLOR);
        mNameView.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_NAME_LENGTH)});
        mNameView.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {

                mCounterNameView.setText(String.format(Locale.getDefault(), "%d/%d", s.length(), MAX_NAME_LENGTH));

                if (!s.toString().isEmpty() && !s.toString().equals(mName)) {
                    setUpdated();

                } else if (mUpdatedProfileAvatar == null) {
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

        mCounterNameView = findViewById(R.id.show_profile_activity_counter_name_view);
        Design.updateTextFont(mCounterNameView, Design.FONT_REGULAR26);
        mCounterNameView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mCounterNameView.setText("0/" + MAX_NAME_LENGTH);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mCounterNameView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_COUNTER_TOP_MARGIN * Design.HEIGHT_RATIO);

        mDescriptionContentView = findViewById(R.id.show_profile_activity_description_content_view);

        ShapeDrawable descriptionContentViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        descriptionContentViewBackground.getPaint().setColor(Design.EDIT_TEXT_BACKGROUND_COLOR);
        mDescriptionContentView.setBackground(descriptionContentViewBackground);

        layoutParams = mDescriptionContentView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = (int) Design.DESCRIPTION_CONTENT_VIEW_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mDescriptionContentView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_DESCRIPTION_TOP_MARGIN * Design.HEIGHT_RATIO);

        mDescriptionView = findViewById(R.id.show_profile_activity_description_view);
        Design.updateTextFont(mDescriptionView, Design.FONT_REGULAR28);
        mDescriptionView.setTextColor(Design.EDIT_TEXT_TEXT_COLOR);
        mDescriptionView.setHintTextColor(Design.GREY_COLOR);
        mDescriptionView.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_DESCRIPTION_LENGTH)});
        mDescriptionView.addTextChangedListener(new TextWatcher() {

            @SuppressLint("DefaultLocale")
            @Override
            public void afterTextChanged(Editable s) {

                mCounterDescriptionView.setText(String.format(Locale.getDefault(), "%d/%d", s.length(), MAX_NAME_LENGTH));
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
            descriptionGestureDetector.onTouchEvent(motionEvent);
            touchContent(motionEvent);
            return true;
        });

        mCounterDescriptionView = findViewById(R.id.show_profile_activity_counter_description_view);
        Design.updateTextFont(mCounterDescriptionView, Design.FONT_REGULAR26);
        mCounterDescriptionView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mCounterDescriptionView.setText("0/" + MAX_DESCRIPTION_LENGTH);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mCounterDescriptionView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_COUNTER_TOP_MARGIN * Design.HEIGHT_RATIO);

        mSaveClickableView = findViewById(R.id.show_profile_activity_save_view);
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

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mSaveClickableView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_SAVE_TOP_MARGIN * Design.HEIGHT_RATIO);

        TextView saveTextView = findViewById(R.id.show_profile_activity_save_title_view);
        Design.updateTextFont(saveTextView, Design.FONT_BOLD28);
        saveTextView.setTextColor(Color.WHITE);

        mEditView = findViewById(R.id.show_profile_activity_edit_clickable_view);
        GestureDetector editGestureDetector = new GestureDetector(this, new ViewTapGestureDetector(ACTION_EDIT_PROFILE));
        mEditView.setOnTouchListener((v, motionEvent) -> {
            editGestureDetector.onTouchEvent(motionEvent);
            touchContent(motionEvent);
            return true;
        });

        layoutParams = mEditView.getLayoutParams();
        layoutParams.height = Design.EDIT_CLICKABLE_VIEW_HEIGHT;

        ImageView editImageView = findViewById(R.id.show_profile_activity_edit_image_view);
        editImageView.setColorFilter(Design.getMainStyle());

        mTwincodeView = findViewById(R.id.show_profile_activity_twincode_view);
        GestureDetector twincodeGestureDetector = new GestureDetector(this, new ViewTapGestureDetector(ACTION_TWINCODE));
        mTwincodeView.setOnTouchListener((v, motionEvent) -> {
            twincodeGestureDetector.onTouchEvent(motionEvent);
            touchContent(motionEvent);
            return true;
        });

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mTwincodeView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_TWINCODE_VIEW_TOP_MARGIN * Design.HEIGHT_RATIO);

        ShapeDrawable twincodeViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        twincodeViewBackground.getPaint().setColor(Design.getMainStyle());
        mTwincodeView.setBackground(twincodeViewBackground);

        ImageView twincodeIconView = findViewById(R.id.show_profile_activity_twincode_icon_view);
        marginLayoutParams = (ViewGroup.MarginLayoutParams) twincodeIconView.getLayoutParams();
        marginLayoutParams.leftMargin = Design.TWINCODE_PADDING;
        marginLayoutParams.topMargin = Design.TWINCODE_ICON_PADDING;
        marginLayoutParams.bottomMargin = Design.TWINCODE_ICON_PADDING;
        marginLayoutParams.setMarginStart(Design.TWINCODE_PADDING);

        layoutParams = twincodeIconView.getLayoutParams();
        layoutParams.height = Design.TWINCODE_ICON_SIZE;
        layoutParams.width = Design.TWINCODE_ICON_SIZE;

        TextView twincodeTextView = findViewById(R.id.show_profile_activity_twincode_title_view);
        Design.updateTextFont(twincodeTextView, Design.FONT_REGULAR28);
        twincodeTextView.setTextColor(Color.WHITE);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) twincodeTextView.getLayoutParams();
        marginLayoutParams.leftMargin = Design.TWINCODE_PADDING;
        marginLayoutParams.rightMargin = Design.TWINCODE_PADDING;
        marginLayoutParams.setMarginStart(Design.TWINCODE_PADDING);
        marginLayoutParams.setMarginEnd(Design.TWINCODE_PADDING);

        mDescriptionTextView = findViewById(R.id.show_profile_activity_description_text_view);
        Design.updateTextFont(mDescriptionTextView, Design.FONT_MEDIUM34);
        mDescriptionTextView.setTextColor(Design.FONT_COLOR_DESCRIPTION);

        mMessageView = findViewById(R.id.show_profile_activity_activity_message_view);
        Design.updateTextFont(mMessageView, Design.FONT_REGULAR26);
        mMessageView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mMessageView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_MESSAGE_VIEW_TOP_MARGIN * Design.HEIGHT_RATIO);

        mProgressBarView = findViewById(R.id.show_profile_activity_progress_bar);

        mUIInitialized = true;
    }

    private void onAvatarClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onAvatarClick");
        }

        if (mProfile != null) {
            return;
        }

        mEditableView.onClick();
    }

    private void onSaveClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSaveClick");
        }

        String name = mNameView.getText().toString().trim();
        if (name.isEmpty() || mUpdatedProfileAvatar == null) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.onboarding_profile);
            DialogInterface.OnCancelListener dialogCancelListener = dialog -> {
            };
            OnboardingDialog onboardingDialog = new OnboardingDialog(this);
            onboardingDialog.setOnCancelListener(dialogCancelListener);
            onboardingDialog.setup(Html.fromHtml(getString(R.string.create_profile_activity_incomplete_profile_message)), bitmap,
                    getString(R.string.application_ok),
                    onboardingDialog::dismiss
            );
            onboardingDialog.show();
            return;
        }

        mSaveClickableView.setAlpha(0.5f);

        String updatedIdentityName = mNameView.getText().toString().trim();
        if (updatedIdentityName.isEmpty() && mName != null) {
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
            mTitleView.setText(mName);
            mDescriptionTextView.setText(mDescription);

            mNameContentView.setVisibility(View.GONE);
            mDescriptionContentView.setVisibility(View.GONE);
            mNameView.setVisibility(View.GONE);
            mDescriptionView.setVisibility(View.GONE);
            mCounterNameView.setVisibility(View.GONE);
            mCounterDescriptionView.setVisibility(View.GONE);
            mSaveClickableView.setVisibility(View.GONE);
            mAddContactView.setVisibility(View.VISIBLE);
            mDescriptionTextView.setVisibility(View.VISIBLE);
            mMessageView.setVisibility(View.VISIBLE);
            mTwincodeView.setVisibility(View.VISIBLE);
            mEditView.setVisibility(View.VISIBLE);
        } else {
            if (mName == null || !mName.isEmpty()) {
                mNameView.setHint(getResources().getString(R.string.application_name_hint));
            }

            mNameContentView.setVisibility(View.VISIBLE);
            mDescriptionContentView.setVisibility(View.VISIBLE);
            mNameView.setVisibility(View.VISIBLE);
            mDescriptionView.setVisibility(View.VISIBLE);
            mCounterNameView.setVisibility(View.VISIBLE);
            mCounterDescriptionView.setVisibility(View.VISIBLE);
            mSaveClickableView.setVisibility(View.VISIBLE);
            mAddContactView.setVisibility(View.GONE);
            mDescriptionTextView.setVisibility(View.GONE);
            mMessageView.setVisibility(View.GONE);
            mTwincodeView.setVisibility(View.GONE);
            mEditView.setVisibility(View.GONE);

            setUpdated();
        }

        if (mAvatar != null) {
            mAvatarView.setImageBitmap(mUpdatedProfileLargeAvatar != null ? mUpdatedProfileLargeAvatar : mAvatar);
            mAvatarView.setBackgroundColor(Color.TRANSPARENT);
            mNoAvatarView.setVisibility(View.GONE);
        } else {
            mEditIdentityService.getProfileImage(mProfile, (Bitmap avatar) -> {
                mAvatar = avatar;
                if (mAvatar != null || mUpdatedProfileLargeAvatar != null) {
                    mAvatarView.setImageBitmap(mUpdatedProfileLargeAvatar != null ? mUpdatedProfileLargeAvatar : mAvatar);
                    mAvatarView.setBackgroundColor(Color.TRANSPARENT);
                    mNoAvatarView.setVisibility(View.GONE);
                    if (mUpdatedProfileLargeAvatar != null) {
                        setUpdated();
                    }
                } else {
                    mAvatarView.setBackgroundColor(Design.AVATAR_PLACEHOLDER_COLOR);
                    mNoAvatarView.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private void onAddContactClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onAddContactClick");
        }

        if (mProfile != null) {

            PercentRelativeLayout percentRelativeLayout = findViewById(R.id.show_profile_activity_layout);

            MenuAddContactView menuAddContactView = new MenuAddContactView(this, null);
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            menuAddContactView.setLayoutParams(layoutParams);

            MenuAddContactView.Observer observer = new MenuAddContactView.Observer() {
                @Override
                public void onStartAddContactByScan() {

                    menuAddContactView.animationCloseMenu();

                    Intent intent = new Intent();
                    intent.putExtra(Intents.INTENT_PROFILE_ID, mProfile.getId());
                    intent.putExtra(Intents.INTENT_INVITATION_MODE, AddContactActivity.InvitationMode.SCAN);
                    intent.setClass(getBaseContext(), AddContactActivity.class);
                    startActivity(intent);
                }

                @Override
                public void onStartAddContactByInvite() {

                    menuAddContactView.animationCloseMenu();

                    Intent intent = new Intent();
                    intent.putExtra(Intents.INTENT_PROFILE_ID, mProfile.getId());
                    intent.putExtra(Intents.INTENT_INVITATION_MODE, AddContactActivity.InvitationMode.INVITE);
                    intent.setClass(getBaseContext(), AddContactActivity.class);
                    startActivity(intent);
                }

                @Override
                public void onCloseMenuSelectActionAnimationEnd() {

                    percentRelativeLayout.removeView(menuAddContactView);
                    setFullscreen();
                }
            };

            menuAddContactView.setObserver(observer);

            percentRelativeLayout.addView(menuAddContactView);

            List<UIMenuSelectAction> actions = new ArrayList<>();
            actions.add(new UIMenuSelectAction(getString(R.string.contacts_fragment_scan_contact_title), R.drawable.scan_code));
            actions.add(new UIMenuSelectAction(getString(R.string.contacts_fragment_invite_contact_title), R.drawable.qrcode));
            menuAddContactView.setActions(actions, this);
            menuAddContactView.openMenu(false);

            Window window = getWindow();
            window.setNavigationBarColor(Design.POPUP_BACKGROUND_COLOR);
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

        boolean hasFocus = mNameView.hasFocus();
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

    protected void onEditProfileClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onEditProfileClick");
        }

        if (mProfile != null) {
            startActivity(EditProfileActivity.class, Intents.INTENT_PROFILE_ID, mProfile.getId());
        }
    }

    protected void onTwincodeClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onTwincodeClick");
        }

        if (mProfile != null) {
            Intent intent = new Intent(this, AddContactActivity.class);
            intent.putExtra(Intents.INTENT_PROFILE_ID, mProfile.getId().toString());
            intent.putExtra(Intents.INTENT_INVITATION_MODE, AddContactActivity.InvitationMode.INVITE_ONLY);
            startActivity(intent);
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
