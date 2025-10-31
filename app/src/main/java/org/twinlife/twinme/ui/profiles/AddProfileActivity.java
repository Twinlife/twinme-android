/*
 *  Copyright (c) 2020-2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui.profiles;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.ColorUtils;
import androidx.percentlayout.widget.PercentRelativeLayout;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.models.Profile;
import org.twinlife.twinme.models.Space;
import org.twinlife.twinme.services.CreateProfileService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.skin.DisplayMode;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.AcceptInvitationActivity;
import org.twinlife.twinme.ui.AddContactActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.inAppSubscriptionActivity.InvitationSubscriptionActivity;
import org.twinlife.twinme.ui.Settings;
import org.twinlife.twinme.utils.AbstractConfirmView;
import org.twinlife.twinme.utils.AvatarView;
import org.twinlife.twinme.utils.DefaultConfirmView;
import org.twinlife.twinme.utils.EditableView;
import org.twinlife.twinme.utils.OnboardingConfirmView;
import org.twinlife.twinme.utils.UIMenuSelectAction;
import org.twinlife.twinme.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class AddProfileActivity extends AbstractTwinmeActivity implements CreateProfileService.Observer {
    private static final String LOG_TAG = "AddProfileActivity";
    private static final boolean DEBUG = false;

    private static final String SHOW_ONBOARDING = "showOnboarding";
    private static final String PROFILE_ID = "profileId";

    public static final int MAX_NAME_LENGTH = 32;

    public static final float DESIGN_AVATAR_TOP_MARGIN = 40f;
    public static final float DESIGN_AVATAR_HEIGHT = 280f;
    public static final float DESIGN_EDIT_AVATAR_HEIGHT = 80f;
    public static final float DESIGN_NAME_TOP_MARGIN = 40f;
    public static final float DESIGN_SAVE_TOP_MARGIN = 20f;
    public static final float DESIGN_COUNTER_TOP_MARGIN = 10f;
    public static final float DESIGN_MESSAGE_TOP_MARGIN = 40f;

    private View mContainerView;
    private AvatarView mAvatarView;
    private View mNameContentView;
    private EditText mNameView;
    private TextView mCounterNameView;
    private View mSaveClickableView;
    private TextView mSaveTextView;
    private TextView mMessageView;

    private EditableView mEditableView;
    private ImageView mNoAvatarView;

    private boolean mUIInitialized = false;
    private boolean mUpdated = false;
    private boolean mCreateProfile = false;
    private boolean mFirstProfile = false;
    private boolean mFromContact = false;
    private boolean mFromSubscription = false;
    private boolean mShowOnboarding = false;
    private String mNameSpace;
    @Nullable
    private UUID mProfileId;

    private Bitmap mUpdatedProfileAvatar;
    private Bitmap mUpdatedProfileLargeAvatar;
    private File mUpdatedProfileFile;

    private CreateProfileService mAddProfileService;

    private String mLastLevelName;
    @Nullable
    private String mInvitationLink;

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
        mFirstProfile = intent.getBooleanExtra(Intents.INTENT_FIRST_PROFILE, false);
        mFromContact = intent.getBooleanExtra(Intents.INTENT_FROM_CONTACT, false);
        mFromSubscription = intent.getBooleanExtra(Intents.INTENT_FROM_SUBSCRIPTION, false);
        mNameSpace = intent.getStringExtra(Intents.INTENT_SPACE_NAME);
        mInvitationLink = intent.getStringExtra(Intents.INTENT_INVITATION_LINK);

        setFullscreen();

        if (intent.hasExtra(Intents.INTENT_LAST_LEVEL_NAME)) {
            mLastLevelName = intent.getStringExtra(Intents.INTENT_LAST_LEVEL_NAME);
        }

        setFullscreen();
        
        initViews();

        if (savedInstanceState != null) {
            // Restore saved profileId created after user answers POST_NOTIFICATIONS.
            mProfileId = Utils.UUIDFromString(savedInstanceState.getString(PROFILE_ID));
            if (mEditableView != null) {
                mShowOnboarding = savedInstanceState.getBoolean(SHOW_ONBOARDING);
                mEditableView.onCreate(savedInstanceState);
                updateSelectedImage();
            }
        }

        mAddProfileService = new CreateProfileService(this, getTwinmeContext(), this);
    }

    //
    // Override Activity methods
    //

    @Override
    public void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        super.onResume();

        if (!mShowOnboarding) {
            mShowOnboarding = true;

            showOnboarding(false);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onActivityResult requestCode=" + requestCode + " resultCode=" + resultCode + " data=" + data);
        }

        super.onActivityResult(requestCode, resultCode, data);

        if (mEditableView != null) {
            Uri image = mEditableView.onActivityResult(requestCode, resultCode, data);
            if (image != null) {
                updateSelectedImage();
            }
        }
    }

    @Override
    public void onPause() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPause");
        }

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

        if (mLastLevelName != null && !mLastLevelName.isEmpty()) {
            mAddProfileService.setLevel(mLastLevelName);
        }

        mAddProfileService.dispose();

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

        // Save the profile Id when it was created for Android 13 while we ask for the POST_NOTIFICATIONS permission.
        if (mProfileId != null) {
            outState.putString(PROFILE_ID, mProfileId.toString());
        }
        outState.putBoolean(SHOW_ONBOARDING, mShowOnboarding);
    }

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
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateOptionsMenu: menu=" + menu);
        }

        super.onCreateOptionsMenu(menu);

        if (mFirstProfile) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.onboarding_menu, menu);

            MenuItem menuItem = menu.findItem(R.id.info_action);
            ImageView imageView = (ImageView) menuItem.getActionView();

            if (imageView != null) {
                imageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.onboarding_info_icon, null));
                imageView.setColorFilter(Color.WHITE);
                imageView.setPadding(Design.TOOLBAR_IMAGE_ITEM_PADDING, 0, Design.TOOLBAR_IMAGE_ITEM_PADDING, 0);
                imageView.setOnClickListener(view -> onOnboardingClick());
            }
        }

        return true;
    }

    //
    // Implement CreateProfileService.Observer methods
    //

    @Override
    public void onCreateSpace(@NonNull Space space) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateSpace: " + space);
        }
    }

    @Override
    public void onCreateProfile(@NonNull Profile profile) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateProfile: " + profile);
        }

        mLastLevelName = null;
        mCreateProfile = false;
        mProfileId = profile.getId();

        finishCreateProfile();
    }

    //
    // Private methods
    //

    private void finishCreateProfile() {
        if (DEBUG) {
            Log.d(LOG_TAG, "finishCreateProfile: " + mProfileId);
        }

        if (mFirstProfile) {
            if (mInvitationLink != null) {
                Intent intent = new Intent(this, AcceptInvitationActivity.class);
                intent.putExtra(Intents.INTENT_INVITATION_LINK, mInvitationLink);
                startActivity(intent);
            } else if (mFromContact && mProfileId != null) {
                Intent intent = new Intent(this, AddContactActivity.class);
                intent.putExtra(Intents.INTENT_INVITATION_MODE, AddContactActivity.InvitationMode.INVITE);
                intent.putExtra(Intents.INTENT_PROFILE_ID, mProfileId.toString());
                startActivity(intent);
            } else if (mFromSubscription) {
                Intent intent = new Intent();
                intent.setClass(this, InvitationSubscriptionActivity.class);
                startActivity(intent);
            }
        }

        finish();
    }

    @SuppressLint({"ClickableViewAccessibility"})
    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.add_profile_activity);

        setStatusBarColor();
        setToolBar(R.id.add_profile_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);

        setBackgroundColor(Design.WHITE_COLOR);

        setTitle(getString(R.string.application_profile));

        applyInsets(R.id.add_profile_activity_layout, R.id.add_profile_activity_tool_bar, R.id.add_profile_activity_container_view, Design.TOOLBAR_COLOR, false);

        mContainerView = findViewById(R.id.add_profile_activity_container_view);
        mContainerView.setBackgroundColor(Design.WHITE_COLOR);

        mEditableView = new EditableView(this);

        View contentAvatarView = findViewById(R.id.add_profile_activity_avatar_content_view);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) contentAvatarView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_AVATAR_TOP_MARGIN * Design.HEIGHT_RATIO);

        mAvatarView = findViewById(R.id.add_profile_activity_avatar_view);

        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(Design.AVATAR_PLACEHOLDER_COLOR);
        gradientDrawable.setCornerRadius((DESIGN_AVATAR_HEIGHT * Design.HEIGHT_RATIO) * 0.5f);
        mAvatarView.setBackground(gradientDrawable);

        mAvatarView.setOnClickListener(v -> openMenuPhoto());

        ViewGroup.LayoutParams layoutParams = mAvatarView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_AVATAR_HEIGHT * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_AVATAR_HEIGHT * Design.HEIGHT_RATIO);

        mNoAvatarView = findViewById(R.id.add_profile_activity_no_avatar_view);
        mNoAvatarView.setVisibility(View.VISIBLE);

        layoutParams = mNoAvatarView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_EDIT_AVATAR_HEIGHT * Design.HEIGHT_RATIO);

        mNoAvatarView.setOnClickListener(v -> openMenuPhoto());

        mNameContentView = findViewById(R.id.add_profile_activity_name_content_view);

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

        mNameView = findViewById(R.id.add_profile_activity_name_view);
        Design.updateTextFont(mNameView, Design.FONT_REGULAR28);
        mNameView.setTextColor(Design.EDIT_TEXT_TEXT_COLOR);
        mNameView.setHintTextColor(Design.GREY_COLOR);
        mNameView.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_NAME_LENGTH)});
        mNameView.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {

                mCounterNameView.setText(String.format(Locale.getDefault(), "%d/%d", s.length(), MAX_NAME_LENGTH));
                updateProfile();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        mNameView.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (!mNameView.getText().toString().isEmpty() && mUpdatedProfileAvatar == null) {
                    openMenuPhoto();
                }
            }
            return false;
        });

        mCounterNameView = findViewById(R.id.add_profile_activity_counter_name_view);
        Design.updateTextFont(mCounterNameView, Design.FONT_REGULAR26);
        mCounterNameView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mCounterNameView.setText(String.format(Locale.getDefault(), "0/%d", MAX_NAME_LENGTH));

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mCounterNameView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_COUNTER_TOP_MARGIN * Design.HEIGHT_RATIO);

        mSaveClickableView = findViewById(R.id.add_profile_activity_save_view);
        mSaveClickableView.setAlpha(0.5f);
        mSaveClickableView.setOnClickListener(v -> onSaveClick());

        layoutParams = mSaveClickableView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = Design.BUTTON_HEIGHT;

        ShapeDrawable saveViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        saveViewBackground.getPaint().setColor(Design.getMainStyle());
        mSaveClickableView.setBackground(saveViewBackground);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mSaveClickableView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_SAVE_TOP_MARGIN * Design.HEIGHT_RATIO);

        mSaveTextView = findViewById(R.id.add_profile_activity_save_title_view);
        Design.updateTextFont(mSaveTextView, Design.FONT_BOLD28);
        mSaveTextView.setTextColor(Color.WHITE);

        mMessageView = findViewById(R.id.add_profile_activity_message_view);
        Design.updateTextFont(mMessageView, Design.FONT_REGULAR32);
        mMessageView.setTextColor(Design.FONT_COLOR_DEFAULT);

        layoutParams = mMessageView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mMessageView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_MESSAGE_TOP_MARGIN * Design.HEIGHT_RATIO);

        mProgressBarView = findViewById(R.id.add_profile_activity_progress_bar);

        mUIInitialized = true;
    }

    private void updateSelectedImage() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateSelectedImage");
        }

        mEditableView.getSelectedImage((String path, Bitmap bitmap, Bitmap largeImage) -> {
            mUpdatedProfileAvatar = bitmap;
            mUpdatedProfileLargeAvatar = largeImage;
            mUpdatedProfileFile = new File(path);
            mUpdated = true;
            mNoAvatarView.setVisibility(View.GONE);
            updateProfile();
        });
    }

    private void onSaveClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSaveClick");
        }

        if (mCreateProfile) {

            return;
        }

        hideKeyboard();

        String name = mNameView.getText().toString().trim();
        if (name.isEmpty()) {
            showOnboarding(true);
            return;
        } else if (mUpdatedProfileAvatar == null) {
            openMenuPhoto();
            return;
        }

        mCreateProfile = true;

        if (mNameSpace != null) {
            mAddProfileService.createProfile(mNameSpace, name, null, mUpdatedProfileAvatar, mUpdatedProfileFile, true);
        } else {
            String nameSpace =  getString(R.string.space_appearance_activity_general_title);
            mAddProfileService.createProfile(nameSpace, name, null, mUpdatedProfileAvatar, mUpdatedProfileFile, false);
        }
    }

    private void updateProfile() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateProfile");
        }

        if (!mUIInitialized) {

            return;
        }

        mNameView.setHint(getString(R.string.application_name_hint));

        Bitmap avatar;
        if (mUpdatedProfileLargeAvatar != null) {
            avatar = mUpdatedProfileLargeAvatar;
            setUpdated();
            mAvatarView.setImageBitmap(avatar);
            mNoAvatarView.setVisibility(View.GONE);
        } else {
            mNoAvatarView.setVisibility(View.VISIBLE);
        }
    }

    private void openMenuPhoto() {
        if (DEBUG) {
            Log.d(LOG_TAG, "openMenuPhoto");
        }

        hideKeyboard();

        PercentRelativeLayout percentRelativeLayout = findViewById(R.id.add_profile_activity_layout);

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

                setStatusBarColor();
            }
        };

        menuPhotoView.setObserver(observer);
        percentRelativeLayout.addView(menuPhotoView);

        List<UIMenuSelectAction> actions = new ArrayList<>();
        actions.add(new UIMenuSelectAction(getString(R.string.application_camera), R.drawable.grey_camera));
        actions.add(new UIMenuSelectAction(getString(R.string.application_photo_gallery), R.drawable.from_gallery));
        menuPhotoView.setActions(actions, this);
        menuPhotoView.openMenu(false);

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }

    private void hideKeyboard() {
        if (DEBUG) {
            Log.d(LOG_TAG, "hideKeyboard");
        }

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(mNameView.getWindowToken(), 0);
        }
    }

    private void onOnboardingClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onOnboardingClick");
        }

        showInfo();
    }

    private void setUpdated() {
        if (DEBUG) {
            Log.d(LOG_TAG, "setUpdated mUpdated:" + mUpdated);
        }

        String name = mNameView.getText().toString().trim();
        mUpdated = !name.isEmpty() && mUpdatedProfileAvatar != null;

        if (mUpdated) {
            mSaveClickableView.setAlpha(1.0f);
        } else {
            mSaveClickableView.setAlpha(0.5f);
        }
    }

    private void showOnboarding(boolean incompleteProfile) {
        if (DEBUG) {
            Log.d(LOG_TAG, "showOnboarding");
        }

        PercentRelativeLayout percentRelativeLayout = findViewById(R.id.add_profile_activity_layout);

        DefaultConfirmView defaultConfirmView = new DefaultConfirmView(this, null);
        PercentRelativeLayout.LayoutParams layoutParams = new PercentRelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        defaultConfirmView.setLayoutParams(layoutParams);
        defaultConfirmView.hideTitleView();
        defaultConfirmView.hideCancelView();

        boolean darkMode = false;
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        int displayMode = Settings.displayMode.getInt();
        if ((currentNightMode == Configuration.UI_MODE_NIGHT_YES && displayMode == DisplayMode.SYSTEM.ordinal())  || displayMode == DisplayMode.DARK.ordinal()) {
            darkMode = true;
        }

        defaultConfirmView.setImage(ResourcesCompat.getDrawable(getResources(), darkMode ? R.drawable.onboarding_add_profile_dark : R.drawable.onboarding_add_profile, null));

        defaultConfirmView.setMessage(getString(R.string.create_profile_activity_incomplete_profile_message));

        if (incompleteProfile) {
            defaultConfirmView.setConfirmTitle(getString(R.string.application_ok));
        } else {
            defaultConfirmView.setConfirmTitle(getString(R.string.profile_fragment_create_profile));
        }

        AbstractConfirmView.Observer observer = new AbstractConfirmView.Observer() {
            @Override
            public void onConfirmClick() {
                defaultConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCancelClick() {
                defaultConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onDismissClick() {
                defaultConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                percentRelativeLayout.removeView(defaultConfirmView);
                setStatusBarColor();

                mNameView.requestFocus();
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(mNameView, InputMethodManager.SHOW_IMPLICIT);
            }
        };
        defaultConfirmView.setObserver(observer);
        percentRelativeLayout.addView(defaultConfirmView);
        defaultConfirmView.show();

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }

    private void showInfo() {
        if (DEBUG) {
            Log.d(LOG_TAG, "showInfo");
        }

        hideKeyboard();

        PercentRelativeLayout percentRelativeLayout = findViewById(R.id.add_profile_activity_layout);

        OnboardingConfirmView onboardingConfirmView = new OnboardingConfirmView(this, null);
        PercentRelativeLayout.LayoutParams layoutParams = new PercentRelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        onboardingConfirmView.setLayoutParams(layoutParams);

        onboardingConfirmView.setTitle(getString(R.string.application_profile));

        boolean darkMode = false;
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        int displayMode = Settings.displayMode.getInt();
        if ((currentNightMode == Configuration.UI_MODE_NIGHT_YES && displayMode == DisplayMode.SYSTEM.ordinal())  || displayMode == DisplayMode.DARK.ordinal()) {
            darkMode = true;
        }

        onboardingConfirmView.setImage(ResourcesCompat.getDrawable(getResources(), darkMode ? R.drawable.onboarding_add_profile_dark : R.drawable.onboarding_add_profile, null));

        String message = getString(R.string.create_profile_activity_onboarding_message_part_1) +
                "\n\n" +
                getString(R.string.create_profile_activity_onboarding_message_part_2) +
                "\n\n" +
                getString(R.string.create_profile_activity_onboarding_message_part_3) +
                "\n\n" +
                getString(R.string.create_profile_activity_onboarding_message_part_4);

        onboardingConfirmView.setMessage(message);
        onboardingConfirmView.setConfirmTitle(getString(R.string.application_ok));
        onboardingConfirmView.hideCancelView();

        AbstractConfirmView.Observer observer = new AbstractConfirmView.Observer() {
            @Override
            public void onConfirmClick() {
                onboardingConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCancelClick() {
                onboardingConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onDismissClick() {
                onboardingConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                percentRelativeLayout.removeView(onboardingConfirmView);
                setStatusBarColor();
            }
        };
        onboardingConfirmView.setObserver(observer);
        percentRelativeLayout.addView(onboardingConfirmView);
        onboardingConfirmView.show();

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }

    @Override
    public void updateColor() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateColor");
        }

        super.updateColor();

        setToolBar(R.id.add_profile_activity_tool_bar);
        applyInsets(R.id.add_profile_activity_layout, R.id.add_profile_activity_tool_bar, R.id.add_profile_activity_container_view, Design.TOOLBAR_COLOR, false);

        mContainerView.setBackgroundColor(Design.WHITE_COLOR);

        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(Design.AVATAR_PLACEHOLDER_COLOR);
        gradientDrawable.setCornerRadius((DESIGN_AVATAR_HEIGHT * Design.HEIGHT_RATIO) * 0.5f);
        mAvatarView.setBackground(gradientDrawable);

        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable nameViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        nameViewBackground.getPaint().setColor(Design.EDIT_TEXT_BACKGROUND_COLOR);
        mNameContentView.setBackground(nameViewBackground);

        mNameView.setTextColor(Design.EDIT_TEXT_TEXT_COLOR);
        mNameView.setHintTextColor(Design.GREY_COLOR);
        mCounterNameView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mSaveTextView.setTextColor(Color.WHITE);
        mMessageView.setTextColor(Design.FONT_COLOR_DEFAULT);
    }

    @Override
    public void updateFont() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateFont");
        }
        super.updateFont();

        Design.updateTextFont(mNameView, Design.FONT_REGULAR28);
        Design.updateTextFont(mCounterNameView, Design.FONT_REGULAR26);
        Design.updateTextFont(mSaveTextView, Design.FONT_BOLD28);
        Design.updateTextFont(mMessageView, Design.FONT_REGULAR32);
    }
}
