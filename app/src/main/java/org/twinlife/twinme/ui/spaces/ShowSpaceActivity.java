/*
 *  Copyright (c) 2020-2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.spaces;

import static org.twinlife.twinme.ui.Intents.INTENT_PROFILE_ID;
import static org.twinlife.twinme.ui.Intents.INTENT_SPACE_ID;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;
import androidx.percentlayout.widget.PercentRelativeLayout;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.models.Profile;
import org.twinlife.twinme.models.Space;
import org.twinlife.twinme.models.SpaceSettings;
import org.twinlife.twinme.services.ShowSpaceService;
import org.twinlife.twinme.skin.CircularImageDescriptor;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.AddContactActivity;
import org.twinlife.twinme.ui.EditProfileActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.ShowProfileActivity;
import org.twinlife.twinme.ui.cleanupActivity.TypeCleanUpActivity;
import org.twinlife.twinme.ui.exportActivity.ExportActivity;
import org.twinlife.twinme.ui.privacyActivity.UITimeout;
import org.twinlife.twinme.ui.settingsActivity.MenuSelectValueView;
import org.twinlife.twinme.utils.AbstractConfirmView;
import org.twinlife.twinme.utils.CircularImageView;
import org.twinlife.twinme.utils.RoundedView;
import org.twinlife.twinme.utils.SwitchView;

import java.util.UUID;

/**
 * Activity controller to display information about the group.
 */

public class ShowSpaceActivity extends AbstractTwinmeActivity implements ShowSpaceService.Observer, MenuSelectValueView.Observer {
    private static final String LOG_TAG = "ShowSpaceActivity";
    private static final boolean DEBUG = false;
    
    private static final int DESIGN_DESCRIPTION_COLOR = Color.rgb(140, 159, 175);
    private static final float DESIGN_PROFILE_AVATAR_BORDER_THICKNESS = 4f / 216f;

    private static final int DESIGN_PROFILE_AVATAR_HEIGHT = 216;
    private static final int DESIGN_SLIDE_MARK_TOP_MARGIN = 16;
    private static final int DESIGN_HEADER_VIEW_TOP_MARGIN = 56;
    private static final int DESIGN_TILE_VIEW_TOP_MARGIN = 80;
    private static final int DESIGN_SLIDE_MARK_HEIGHT = 12;
    private static final int DESIGN_ADD_PROFILE_PADDING = 20;
    private static final int DESIGN_ADD_PROFILE_HEIGHT = 56;
    private static final int DESIGN_EDIT_CLICKABLE_VIEW_HEIGHT = 80;
    private static final int DESIGN_SECTION_HEIGHT = 120;
    private static final int DESIGN_IDENTITY_VIEW_TOP_MARGIN = 108;
    private static final int DESIGN_SEPARATOR_VIEW_TOP_MARGIN = 36;
    private static final int DESIGN_SECTION_VIEW_TOP_MARGIN = 14;
    private static final int DESIGN_AVATAR_OVER_SIZE = 120;
    private static final float DESIGN_ACTION_VIEW_MIN_MARGIN = 90f;
    private static int AVATAR_OVER_SIZE;
    private static int AVATAR_MAX_SIZE;
    private static int ACTION_VIEW_MIN_MARGIN;

    private UUID mSpaceId;
    private ImageView mAvatarView;
    private View mContentView;
    private View mHeaderView;
    private TextView mNameView;
    private TextView mDescriptionView;
    private TextView mIdentityTextView;
    private ImageView mTwincodeIconView;
    private TextView mTwincodeTextView;
    private CircularImageView mIdentityAvatarView;
    private View mContactsView;
    private View mSettingsView;
    private View mSecretView;
    private SwitchView mSecretSwitchView;

    private ScrollView mScrollView;

    private MenuSelectValueView mMenuSelectValueView;
    private View mOverlayMenuView;

    private boolean mUIInitialized = false;
    private boolean mUIPostInitialized = false;
    private boolean mInitScrollView = false;

    private ShowSpaceService mSpaceService;

    @Nullable
    private Space mSpace;

    private Bitmap mSpaceAvatar;
    private String mIdentityName;
    private Bitmap mIdentityAvatar;
    
    private float mAvatarLastSize = -1;
    private float mScrollPosition = -1;

    private boolean mSpaceDeleted = false;

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
        String value = intent.getStringExtra(Intents.INTENT_SPACE_ID);
        if (value != null) {
            mSpaceId = UUID.fromString(value);
        }

        initViews();

        mSpaceService = new ShowSpaceService(this, getTwinmeContext(), this, mSpaceId, false);
    }

    @Override
    protected void onPause() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPause");
        }

        super.onPause();
    }

    @Override
    protected void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

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
            layoutParams.height = contentHeight + AVATAR_MAX_SIZE;

            mScrollView.post(() -> mScrollView.scrollBy(0, AVATAR_OVER_SIZE));
        }

        if (mSpaceDeleted) {
            finish();
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

        mSpaceService.dispose();

        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSaveInstanceState: outState=" + outState);
        }

        super.onSaveInstanceState(outState);
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
    // Implement ShowSpaceService.Observer methods
    //

    @Override
    public void onGetSpace(Space space, Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetSpace space=" + space);
        }

        mSpace = space;
        mSpaceAvatar = avatar;

        if (space.getProfile() != null) {
            mIdentityName = space.getProfile().getName();
        } else {
            mIdentityName = getTwinmeApplication().getAnonymousName();
        }

        mSpaceService.getProfileImage(space.getProfile(), (Bitmap identityAvatar) -> {
            mIdentityAvatar = (identityAvatar != null) ?
                    identityAvatar :
                    getTwinmeApplication().getAnonymousAvatar();

            updateSpace();
        });
    }

    @Override
    public void onGetSpaceNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetSpaceNotFound");
        }

        finish();
    }


    @Override
    public void onUpdateSpace(Space space, Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateSpace: space=" + space + " avatar=" + avatar);
        }

        mSpace = space;
        mSpaceAvatar = avatar;
        if (space.getProfile() != null) {
            mIdentityName = space.getProfile().getName();
        } else {
            mIdentityName = getTwinmeApplication().getAnonymousName();
        }

        mSpaceService.getProfileImage(space.getProfile(), (Bitmap identityAvatar) -> {
            mIdentityAvatar = (identityAvatar != null) ?
                    identityAvatar :
                    getTwinmeApplication().getAnonymousAvatar();

            updateSpace();
        });
    }

    @Override
    public void onDeleteSpace(UUID spaceId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteSpace spaceId=" + spaceId);
        }

        if (!spaceId.equals(mSpaceId)) {

            return;
        }

        mSpaceDeleted = true;

        if (mResumed) {
            finish();
        }
    }

    @Override
    public void onUpdateProfile(Profile profile) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateProfile profile=" + profile);
        }

        if (profile.getSpace() != null && profile.getSpace().getId().equals(mSpaceId)) {
            mIdentityName = profile.getName();
            mSpaceService.getProfileImage(profile, (Bitmap identityAvatar) -> {
                mIdentityAvatar = (identityAvatar != null) ?
                        identityAvatar :
                        getTwinmeApplication().getAnonymousAvatar();

                updateSpace();
            });
        }
    }

    //MenuSelectValueView.Observer

    @Override
    public void onCloseMenuAnimationEnd() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCloseMenuAnimationEnd");
        }

        mMenuSelectValueView.setVisibility(View.INVISIBLE);
        mOverlayMenuView.setVisibility(View.INVISIBLE);
        setStatusBarColor();
    }

    @Override
    public void onSelectValue(int value) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSelectValue: " + value);
        }

        closeMenuSelectVaue();

        Intent intent = new Intent();
        if (value == 0) {
            intent.putExtra(INTENT_SPACE_ID, mSpaceId.toString());
            intent.setClass(this, EditSpaceActivity.class);
        } else {
            intent.putExtra(INTENT_PROFILE_ID, mSpace.getProfile().getId().toString());
            intent.setClass(this, EditProfileActivity.class);
        }
        startActivity(intent);
    }

    @Override
    public void onSelectTimeout(UITimeout timeout) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSelectTimeout: " + timeout);
        }
    }

    //
    // Private methods
    //

    @SuppressLint({"ClickableViewAccessibility"})
    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.show_space_activity);

        showToolBar(false);
        showBackButton(true);

        setTitle(getString(R.string.show_contact_activity_space));

        applyInsets(R.id.show_space_activity_layout, -1, -1, Design.WHITE_COLOR, true);

        View backClickableView = findViewById(R.id.show_space_activity_back_clickable_view);
        backClickableView.setOnClickListener(view -> onBackClick());

        ViewGroup.LayoutParams layoutParams = backClickableView.getLayoutParams();
        layoutParams.height = Design.BACK_CLICKABLE_VIEW_HEIGHT;

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) backClickableView.getLayoutParams();
        marginLayoutParams.leftMargin = Design.BACK_CLICKABLE_VIEW_LEFT_MARGIN;
        marginLayoutParams.topMargin = Design.BACK_CLICKABLE_VIEW_TOP_MARGIN;

        RoundedView backRoundedView = findViewById(R.id.show_space_activity_back_rounded_view);
        backRoundedView.setColor(Design.BACK_VIEW_COLOR);

        mAvatarView = findViewById(R.id.show_space_activity_avatar_view);

        layoutParams = mAvatarView.getLayoutParams();
        layoutParams.width = AVATAR_MAX_SIZE - AVATAR_OVER_SIZE;
        layoutParams.height = AVATAR_MAX_SIZE - AVATAR_OVER_SIZE;

        mContentView = findViewById(R.id.show_space_activity_content_view);
        mContentView.setY(AVATAR_MAX_SIZE - ACTION_VIEW_MIN_MARGIN);

        setBackground(mContentView);

        mScrollView = findViewById(R.id.show_space_activity_content_scroll_view);
        ViewTreeObserver viewTreeObserver = mScrollView.getViewTreeObserver();
        viewTreeObserver.addOnScrollChangedListener(() -> {
            if (mScrollPosition == -1) {
                mScrollPosition = AVATAR_OVER_SIZE;
            }

            float delta = mScrollPosition - mScrollView.getScrollY();
            updateAvatarSize(delta);
            mScrollPosition = mScrollView.getScrollY();
        });

        View slideMarkView = findViewById(R.id.show_space_activity_slide_mark_view);
        layoutParams = slideMarkView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_SLIDE_MARK_HEIGHT * Design.HEIGHT_RATIO);

        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.mutate();
        gradientDrawable.setColor(Color.rgb(244, 244, 244));
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        ViewCompat.setBackground(slideMarkView, gradientDrawable);

        float corner = ((DESIGN_SLIDE_MARK_HEIGHT * Design.HEIGHT_RATIO) / 2) * Resources.getSystem().getDisplayMetrics().density;
        gradientDrawable.setCornerRadius(corner);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) slideMarkView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_SLIDE_MARK_TOP_MARGIN * Design.HEIGHT_RATIO);

        mNameView = findViewById(R.id.show_space_activity_name_view);
        mNameView.setTypeface(Design.FONT_BOLD44.typeface);
        mNameView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_BOLD44.size);
        mNameView.setTextColor(DESIGN_DESCRIPTION_COLOR);

        mHeaderView = findViewById(R.id.show_space_activity_content_header_view);
        marginLayoutParams = (ViewGroup.MarginLayoutParams) mHeaderView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_IDENTITY_VIEW_TOP_MARGIN * Design.HEIGHT_RATIO);

        mDescriptionView = findViewById(R.id.show_space_activity_description_view);
        marginLayoutParams = (ViewGroup.MarginLayoutParams) mDescriptionView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_SEPARATOR_VIEW_TOP_MARGIN * Design.HEIGHT_RATIO);

        mDescriptionView.setTypeface(Design.FONT_REGULAR32.typeface);
        mDescriptionView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_REGULAR32.size);
        mDescriptionView.setTextColor(Design.FONT_COLOR_DEFAULT);

        View editClickableView = findViewById(R.id.show_space_activity_edit_clickable_view);
        editClickableView.setClickable(false);
        editClickableView.setOnClickListener(view -> onEditClick());

        layoutParams = editClickableView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_EDIT_CLICKABLE_VIEW_HEIGHT * Design.HEIGHT_RATIO);

        ImageView editImageView = findViewById(R.id.show_space_activity_edit_image_view);
        editImageView.setColorFilter(Design.getMainStyle());

        View twincodeView = findViewById(R.id.show_space_activity_twincode_view);
        twincodeView.setOnClickListener(view -> onTwincodeClick());

        marginLayoutParams = (ViewGroup.MarginLayoutParams) twincodeView.getLayoutParams();
        marginLayoutParams.topMargin = Design.TWINCODE_VIEW_TOP_MARGIN;

        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable twincodeViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        twincodeViewBackground.getPaint().setColor(Design.getMainStyle());
        ViewCompat.setBackground(twincodeView, twincodeViewBackground);

        mTwincodeIconView = findViewById(R.id.show_space_activity_twincode_icon_view);
        marginLayoutParams = (ViewGroup.MarginLayoutParams) mTwincodeIconView.getLayoutParams();
        marginLayoutParams.leftMargin = Design.TWINCODE_PADDING;
        marginLayoutParams.topMargin = Design.TWINCODE_ICON_PADDING;
        marginLayoutParams.bottomMargin = Design.TWINCODE_ICON_PADDING;

        layoutParams = mTwincodeIconView.getLayoutParams();
        layoutParams.height = Design.TWINCODE_ICON_SIZE;
        layoutParams.width = Design.TWINCODE_ICON_SIZE;

        mTwincodeTextView = findViewById(R.id.show_space_activity_twincode_title_view);
        mTwincodeTextView.setTypeface(Design.FONT_REGULAR28.typeface);
        mTwincodeTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_REGULAR28.size);
        mTwincodeTextView.setTextColor(Color.WHITE);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mTwincodeTextView.getLayoutParams();
        marginLayoutParams.leftMargin = Design.TWINCODE_PADDING;
        marginLayoutParams.rightMargin = Design.TWINCODE_PADDING;

        TextView settingsTitleView = findViewById(R.id.show_space_activity_settings_title_view);
        settingsTitleView.setTypeface(Design.FONT_BOLD26.typeface);
        settingsTitleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_BOLD26.size);
        settingsTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) settingsTitleView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_TILE_VIEW_TOP_MARGIN * Design.HEIGHT_RATIO);

        mSettingsView = findViewById(R.id.show_space_activity_settings_layout_view);
        layoutParams = mSettingsView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_SECTION_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mSettingsView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_SECTION_VIEW_TOP_MARGIN * Design.HEIGHT_RATIO);

        mSettingsView.setOnClickListener(view -> onSettingsClick());

        ImageView settingsImageView = findViewById(R.id.show_space_activity_settings_image_view);
        settingsImageView.setColorFilter(Design.SHOW_ICON_COLOR);

        TextView settingsText = findViewById(R.id.show_space_activity_settings_text);
        settingsText.setTypeface(Design.FONT_REGULAR32.typeface);
        settingsText.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_REGULAR32.size);
        settingsText.setTextColor(Design.FONT_COLOR_DEFAULT);

        mContactsView = findViewById(R.id.show_space_activity_list_contacts_layout_view);
        layoutParams = mContactsView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_SECTION_HEIGHT * Design.HEIGHT_RATIO);

        mContactsView.setOnClickListener(view -> onListContactsClick());

        TextView contactsText = findViewById(R.id.show_space_activity_contacts_text);
        contactsText.setTypeface(Design.FONT_REGULAR32.typeface);
        contactsText.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_REGULAR32.size);
        contactsText.setTextColor(Design.FONT_COLOR_DEFAULT);

        mIdentityTextView = findViewById(R.id.show_space_activity_identity_text_view);
        mIdentityTextView.setTypeface(Design.FONT_BOLD44.typeface);
        mIdentityTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_BOLD44.size);
        mIdentityTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mIdentityAvatarView = findViewById(R.id.show_space_activity_edit_identity_avatar_view);
        mIdentityAvatarView.setOnClickListener(view -> onProfileAvatarClick());

        layoutParams = mIdentityAvatarView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_PROFILE_AVATAR_HEIGHT * Design.HEIGHT_RATIO);

        float identityAvatarInitialPosition = AVATAR_MAX_SIZE - ACTION_VIEW_MIN_MARGIN;
        identityAvatarInitialPosition = (float) (identityAvatarInitialPosition - (DESIGN_PROFILE_AVATAR_HEIGHT * Design.HEIGHT_RATIO * 0.5));
        mIdentityAvatarView.setY(identityAvatarInitialPosition);

        mSecretView = findViewById(R.id.show_space_activity_secret_view);
        layoutParams = mSecretView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_SECTION_HEIGHT * Design.HEIGHT_RATIO);

        mSecretView.setOnLongClickListener(v -> {
            showAlertMessageView(R.id.show_space_activity_layout, getString(R.string.settings_space_activity_secret_title), getString(R.string.settings_space_activity_secret_message), true, null);
            return true;
        });

        ImageView secretImageView = findViewById(R.id.show_space_activity_secret_image_view);
        secretImageView.setColorFilter(Design.SHOW_ICON_COLOR);

        mSecretSwitchView = findViewById(R.id.show_space_activity_secret_checkbox);
        mSecretSwitchView.setTypeface(Design.FONT_REGULAR32.typeface);
        mSecretSwitchView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_REGULAR32.size);
        mSecretSwitchView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mSecretSwitchView.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Save only when the user changes (not if setChecked() is called).
            if (buttonView.isPressed()) {
                saveSpace();
            }
        });

        TextView conversationsTitleView = findViewById(R.id.show_space_activity_conversations_title_view);
        conversationsTitleView.setTypeface(Design.FONT_BOLD26.typeface);
        conversationsTitleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_BOLD26.size);
        conversationsTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) conversationsTitleView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_TILE_VIEW_TOP_MARGIN * Design.HEIGHT_RATIO);

        TextView exportTextView = findViewById(R.id.show_space_activity_export_text_view);
        exportTextView.setTypeface(Design.FONT_REGULAR34.typeface);
        exportTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_REGULAR34.size);
        exportTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        View exportView = findViewById(R.id.show_space_activity_export_view);
        layoutParams = exportView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_SECTION_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) exportView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_SECTION_VIEW_TOP_MARGIN * Design.HEIGHT_RATIO);

        exportView.setOnClickListener(view -> onExportClick());

        ImageView exportImageView = findViewById(R.id.show_space_activity_export_image_view);
        exportImageView.setColorFilter(Design.SHOW_ICON_COLOR);

        View cleanView = findViewById(R.id.show_space_activity_clean_view);
        layoutParams = cleanView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_SECTION_HEIGHT * Design.HEIGHT_RATIO);

        cleanView.setOnClickListener(view -> onCleanClick());

        ViewTreeObserver cleanViewTreeObserver = cleanView.getViewTreeObserver();
        cleanViewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewTreeObserver viewTreeObserver = cleanView.getViewTreeObserver();
                viewTreeObserver.removeGlobalOnLayoutListener(this);

                Rect rectangle = new Rect();
                getWindow().getDecorView().getWindowVisibleDisplayFrame(rectangle);
                int contentHeight = (int) (cleanView.getHeight() + cleanView.getY());
                if (contentHeight < rectangle.height()) {
                    contentHeight = rectangle.height();
                }

                ViewGroup.LayoutParams layoutParams = mContentView.getLayoutParams();
                layoutParams.height = contentHeight + AVATAR_MAX_SIZE;
            }
        });

        TextView cleanTextView = findViewById(R.id.show_space_activity_clean_text_view);
        cleanTextView.setTypeface(Design.FONT_REGULAR34.typeface);
        cleanTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_REGULAR34.size);
        cleanTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        ImageView cleanImageView = findViewById(R.id.show_space_activity_clean_image_view);
        cleanImageView.setColorFilter(Design.SHOW_ICON_COLOR);

        mOverlayMenuView = findViewById(R.id.show_space_activity_overlay_view);
        mOverlayMenuView.setBackgroundColor(Design.OVERLAY_VIEW_COLOR);
        mOverlayMenuView.setOnClickListener(view -> closeMenuSelectVaue());

        mMenuSelectValueView = findViewById(R.id.show_space_activity_menu_select_value_view);
        mMenuSelectValueView.setVisibility(View.INVISIBLE);
        mMenuSelectValueView.setObserver(this);
        mMenuSelectValueView.setActivity(this);

        mProgressBarView = findViewById(R.id.show_space_activity_progress_bar);

        mUIInitialized = true;
    }

    private void postInitViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "postInitViews");
        }

        mUIPostInitialized = true;
    }

    private void updateSpace() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateSpace");
        }

        if (mUIInitialized) {
            if (mSpace != null) {
                mSecretView.setVisibility(View.VISIBLE);
                mNameView.setText(mSpace.getName());

                if (mSpace.getDescription() != null && !mSpace.getDescription().isEmpty()) {
                    mDescriptionView.setText(mSpace.getDescription());
                    mDescriptionView.setVisibility(View.VISIBLE);
                } else {
                    mDescriptionView.setVisibility(View.GONE);
                }

                if (mSpaceAvatar != null) {
                    mAvatarView.setBackgroundColor(Color.TRANSPARENT);
                    mAvatarView.setImageBitmap(mSpaceAvatar);
                } else {
                    mAvatarView.setBackgroundColor(Design.getMainStyle());
                    mAvatarView.setImageResource(R.drawable.skred_logo_3d);
                }

                mIdentityAvatarView.setImage(this, null, new CircularImageDescriptor(mIdentityAvatar, 0.5f, 0.5f, 0.5f, Design.WHITE_COLOR, DESIGN_PROFILE_AVATAR_BORDER_THICKNESS, 0));

                if (mSpace.getProfile() != null) {
                    mSettingsView.setAlpha(1.0f);
                    mIdentityTextView.setText(mIdentityName);
                    mIdentityAvatarView.setVisibility(View.VISIBLE);
                    mNameView.setVisibility(View.VISIBLE);
                    mTwincodeIconView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.qrcode, null));
                    mTwincodeTextView.setText(getString(R.string.profile_fragment_twincode_title));

                    ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mHeaderView.getLayoutParams();
                    marginLayoutParams.topMargin = (int) (DESIGN_IDENTITY_VIEW_TOP_MARGIN * Design.HEIGHT_RATIO);

                    ViewGroup.LayoutParams layoutParams = mTwincodeIconView.getLayoutParams();
                    layoutParams.height = Design.TWINCODE_ICON_SIZE;
                    layoutParams.width = Design.TWINCODE_ICON_SIZE;

                    marginLayoutParams = (ViewGroup.MarginLayoutParams) mTwincodeIconView.getLayoutParams();
                    marginLayoutParams.topMargin = Design.TWINCODE_ICON_PADDING;
                    marginLayoutParams.bottomMargin = Design.TWINCODE_ICON_PADDING;
                } else {
                    mSettingsView.setAlpha(0.5f);
                    mIdentityTextView.setText(mSpace.getName());
                    mIdentityAvatarView.setVisibility(View.GONE);
                    mNameView.setVisibility(View.GONE);
                    mTwincodeIconView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.action_bar_add_contact, null));
                    mTwincodeTextView.setText(getString(R.string.profile_fragment_add_profile));

                    ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mHeaderView.getLayoutParams();
                    marginLayoutParams.topMargin = (int) (DESIGN_HEADER_VIEW_TOP_MARGIN * Design.HEIGHT_RATIO);

                    ViewGroup.LayoutParams layoutParams = mTwincodeIconView.getLayoutParams();
                    layoutParams.height =  (int) (DESIGN_ADD_PROFILE_HEIGHT * Design.HEIGHT_RATIO);
                    layoutParams.width =  (int) (DESIGN_ADD_PROFILE_HEIGHT * Design.HEIGHT_RATIO);

                    marginLayoutParams = (ViewGroup.MarginLayoutParams) mTwincodeIconView.getLayoutParams();
                    marginLayoutParams.topMargin = (int) (DESIGN_ADD_PROFILE_PADDING * Design.HEIGHT_RATIO);
                    marginLayoutParams.bottomMargin = (int) (DESIGN_ADD_PROFILE_PADDING * Design.HEIGHT_RATIO);
                }

                if (mSpace.getProfile() != null && mSpaceService.numberSpaces(true) > 1) {
                    mContactsView.setAlpha(1.0f);
                } else {
                    mContactsView.setAlpha(0.5f);
                }

                mSecretSwitchView.setChecked(mSpace.getSpaceSettings().isSecret());

                boolean canUpdateSecretSpace = true;
                if ((mSpaceService.numberSpaces(false) > 1 && !mSpace.getSpaceSettings().isSecret()) || mSpace.getSpaceSettings().isSecret()) {
                    mSecretView.setAlpha(1.0f);
                    mSecretSwitchView.setEnabled(true);
                    mSecretView.setOnClickListener(null);
                } else {
                    mSecretView.setAlpha(0.5f);
                    mSecretSwitchView.setEnabled(false);
                    mSecretView.setOnClickListener(view -> onSecretSpaceClick());
                    canUpdateSecretSpace = false;
                }

                if (canUpdateSecretSpace) {
                    if (!mSpace.getSpaceSettings().isSecret()) {
                        mSecretSwitchView.setEnabled(false);
                        mSecretView.setOnClickListener(v -> onSecretSpaceClick());
                        mSecretSwitchView.setClickable(false);
                    } else {
                        mSecretSwitchView.setEnabled(true);
                        mSecretView.setOnClickListener(null);
                        mSecretSwitchView.setClickable(true);
                    }
                } else {
                    mSecretView.setOnClickListener(v -> onSecretSpaceClick());
                }
            } else {
                mSecretView.setVisibility(View.GONE);
            }
        }
    }

    private void saveSpace() {
        if (DEBUG) {
            Log.d(LOG_TAG, "saveSpace");
        }

        if (mSpace != null) {
            if (mSecretSwitchView.isChecked() && getTwinmeContext().isDefaultSpace(mSpace)) {
                if(!mSpaceService.updateDefaultSpace(mSpace)){
                    return;
                }
            }

            SpaceSettings spaceSettings = mSpace.getSpaceSettings();
            if (mSecretSwitchView.isChecked() != spaceSettings.isSecret()) {
                spaceSettings.setSecret(mSecretSwitchView.isChecked());
                spaceSettings.setBoolean(SpaceSettingProperty.PROPERTY_DISPLAY_NOTIFICATIONS, !mSecretSwitchView.isChecked());
                mSpaceService.updateSpace(mSpace, spaceSettings);
            }
        }
    }

    private void onListContactsClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onListContactsClick");
        }

        if (mSpace != null && mSpace.getProfile() != null && mSpaceService.numberSpaces(true) <= 1) {
            showAlertMessageView(R.id.show_space_activity_layout, getString(R.string.deleted_account_activity_warning), getString(R.string.show_space_fragment_move_message), true, null);
        } else if (mSpace != null && mSpace.getProfile() == null) {
            PercentRelativeLayout percentRelativeLayout = findViewById(R.id.show_space_activity_layout);

            SpaceActionConfirmView spaceActionConfirmView = new SpaceActionConfirmView(this, null);
            PercentRelativeLayout.LayoutParams layoutParams = new PercentRelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            spaceActionConfirmView.setLayoutParams(layoutParams);

            spaceActionConfirmView.setSpaceName(mSpace.getSpaceSettings().getName(), mSpace.getSpaceSettings().getStyle());
            spaceActionConfirmView.setAvatar(mSpaceAvatar, false);
            spaceActionConfirmView.setIcon(R.drawable.action_bar_add_contact);
            spaceActionConfirmView.setTitle(getString(R.string.create_profile_activity_title));
            spaceActionConfirmView.setMessage(getString(R.string.create_space_activity_contacts_no_profile));
            spaceActionConfirmView.setConfirmTitle(getString(R.string.application_now));
            spaceActionConfirmView.setCancelTitle(getString(R.string.application_later));

            AbstractConfirmView.Observer observer = new AbstractConfirmView.Observer() {
                @Override
                public void onConfirmClick() {
                    onEditIdentityClick();
                    spaceActionConfirmView.animationCloseConfirmView();
                }

                @Override
                public void onCancelClick() {
                    spaceActionConfirmView.animationCloseConfirmView();
                }

                @Override
                public void onDismissClick() {
                    spaceActionConfirmView.animationCloseConfirmView();
                }

                @Override
                public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                    percentRelativeLayout.removeView(spaceActionConfirmView);
                    setFullscreen();
                }
            };
            spaceActionConfirmView.setObserver(observer);

            percentRelativeLayout.addView(spaceActionConfirmView);
            spaceActionConfirmView.show();

            Window window = getWindow();
            window.setNavigationBarColor(Design.POPUP_BACKGROUND_COLOR);

        } else {
            Intent intent = new Intent();
            intent.putExtra(INTENT_SPACE_ID, mSpaceId.toString());
            intent.setClass(this, ContactsSpaceActivity.class);
            startActivity(intent);
        }
    }

    private void onEditIdentityClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onEditIdentityClick");
        }

        if (mSpace != null) {
            Intent intent = new Intent();
            if (mSpace.getProfile() == null) {
                intent.putExtra(INTENT_SPACE_ID, mSpace.getId().toString());
                intent.setClass(this, EditProfileActivity.class);
            } else {
                intent.putExtra(INTENT_PROFILE_ID, mSpace.getProfile().getId().toString());
                intent.setClass(this, ShowProfileActivity.class);
            }
            startActivity(intent);
        }
    }

    private void onEditClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onEditClick");
        }

        if (mSpace != null && mSpace.getProfile() == null) {
            Intent intent = new Intent();
            intent.putExtra(INTENT_SPACE_ID, mSpaceId.toString());
            intent.setClass(this, EditSpaceActivity.class);
            startActivity(intent);
        } else if (mSpace != null && mSpace.getProfile() != null) {
            openMenuSelectVaue();
        }
    }

    private void onProfileAvatarClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onProfileAvatarClick");
        }

        if (mSpace != null && mSpace.getProfile() != null) {
            Intent intent = new Intent();
            intent.putExtra(INTENT_PROFILE_ID, mSpace.getProfile().getId().toString());
            intent.setClass(this, ShowProfileActivity.class);
            startActivity(intent);
        }
    }

    private void onTwincodeClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onEditClick");
        }

        if (mSpace != null) {
            Intent intent = new Intent();
            if (mSpace.getProfile() == null) {
                intent.putExtra(INTENT_SPACE_ID, mSpace.getId().toString());
                intent.setClass(this, EditProfileActivity.class);
            } else {
                intent.setClass(this, AddContactActivity.class);
                intent.putExtra(Intents.INTENT_PROFILE_ID, mSpace.getProfile().getId().toString());
                intent.putExtra(Intents.INTENT_INVITATION_MODE, AddContactActivity.InvitationMode.INVITE);
            }
            startActivity(intent);
        }
    }

    private void onSettingsClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSettingsClick");
        }

        Intent intent = new Intent();
        intent.putExtra(Intents.INTENT_SPACE_ID, mSpaceId.toString());
        intent.setClass(this, SettingsSpaceActivity.class);
        startActivity(intent);
    }

    private void onExportClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onExportClick");
        }

        Intent intent = new Intent();
        intent.putExtra(Intents.INTENT_SPACE_ID, mSpaceId.toString());
        intent.setClass(this, ExportActivity.class);
        startActivity(intent);
    }

    private void onCleanClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCleanClick");
        }

        Intent intent = new Intent();
        intent.putExtra(Intents.INTENT_SPACE_ID, mSpaceId.toString());
        intent.setClass(this, TypeCleanUpActivity.class);
        startActivity(intent);
    }

    private void openMenuSelectVaue() {
        if (DEBUG) {
            Log.d(LOG_TAG, "openMenuSelectVaue");
        }

        if (mMenuSelectValueView.getVisibility() == View.INVISIBLE) {
            mMenuSelectValueView.setVisibility(View.VISIBLE);
            mOverlayMenuView.setVisibility(View.VISIBLE);
            mMenuSelectValueView.openMenu(MenuSelectValueView.MenuType.EDIT_SPACE);
        }
    }

    private void closeMenuSelectVaue() {
        if (DEBUG) {
            Log.d(LOG_TAG, "closeMenuSelectVaue");
        }

        mMenuSelectValueView.setVisibility(View.INVISIBLE);
        mOverlayMenuView.setVisibility(View.INVISIBLE);
    }

    private void onSecretSpaceClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSecretSpaceClick");
        }

        if (mSpaceService.numberSpaces(false) == 1) {
            showAlertMessageView(R.id.show_space_activity_layout, getString(R.string.settings_space_activity_secret_title), getString(R.string.show_space_fragment_secret_disabled_message), true, null);
            return;
        }

        PercentRelativeLayout percentRelativeLayout = findViewById(R.id.show_space_activity_layout);

        SpaceActionConfirmView spaceActionConfirmView = new SpaceActionConfirmView(this, null);
        PercentRelativeLayout.LayoutParams layoutParams = new PercentRelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        spaceActionConfirmView.setLayoutParams(layoutParams);

        spaceActionConfirmView.setSpaceName(mSpace.getSpaceSettings().getName(), mSpace.getSpaceSettings().getStyle());
        spaceActionConfirmView.setAvatar(mSpaceAvatar, false);

        String message = getString(R.string.show_space_fragment_secret_message) + "\n\n"  + getString(R.string.show_space_fragment_secret_message_confirm);
        spaceActionConfirmView.setTitle(getString(R.string.application_are_you_sure));
        spaceActionConfirmView.setMessage(message);
        spaceActionConfirmView.setConfirmTitle(getString(R.string.show_space_fragment_secret_confirm));
        AbstractConfirmView.Observer observer = new AbstractConfirmView.Observer() {
            @Override
            public void onConfirmClick() {
                mSecretSwitchView.setChecked(true);
                saveSpace();
                spaceActionConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCancelClick() {
                spaceActionConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onDismissClick() {
                spaceActionConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                percentRelativeLayout.removeView(spaceActionConfirmView);
                setFullscreen();
            }
        };
        spaceActionConfirmView.setObserver(observer);

        percentRelativeLayout.addView(spaceActionConfirmView);
        spaceActionConfirmView.show();

        Window window = getWindow();
        window.setNavigationBarColor(Design.POPUP_BACKGROUND_COLOR);
    }

    private void updateAvatarSize(float deltaY) {
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
            Log.d(LOG_TAG, "setupDesign");
        }

        AVATAR_OVER_SIZE = (int) (DESIGN_AVATAR_OVER_SIZE * Design.WIDTH_RATIO);
        AVATAR_MAX_SIZE = Design.DISPLAY_WIDTH + (AVATAR_OVER_SIZE * 2);
        ACTION_VIEW_MIN_MARGIN = (int) (Design.HEIGHT_RATIO * DESIGN_ACTION_VIEW_MIN_MARGIN);
    }
}
