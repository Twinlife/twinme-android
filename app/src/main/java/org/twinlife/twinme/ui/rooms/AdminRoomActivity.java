/*
 *  Copyright (c) 2020-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.rooms;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.percentlayout.widget.PercentRelativeLayout;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.RoomConfig;
import org.twinlife.twinme.services.EditRoomService;
import org.twinlife.twinme.skin.CircularImageDescriptor;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.profiles.MenuPhotoView;
import org.twinlife.twinme.utils.AbstractConfirmView;
import org.twinlife.twinme.utils.CircularImageView;
import org.twinlife.twinme.utils.CommonUtils;
import org.twinlife.twinme.utils.DefaultConfirmView;
import org.twinlife.twinme.utils.EditableView;
import org.twinlife.twinme.utils.RoundedView;
import org.twinlife.twinme.utils.UIMenuSelectAction;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class AdminRoomActivity extends AbstractTwinmeActivity implements EditRoomService.Observer {
    private static final String LOG_TAG = "AdminRoomActivity";
    private static final boolean DEBUG = false;

    protected static final int DESIGN_HINT_COLOR = Color.parseColor("#bdbdbd");

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

    private static final float DESIGN_CONFIGURATION_TITLE_TOP = 80f;
    private static final float DESIGN_CONFIGURATION_TITLE_LEFT = 34f;
    private static final float DESIGN_CONFIGURATION_TITLE_BOTTOM = 14f;
    private static final float DESIGN_DESCRIPTION_MARGIN = 10f;
    private static int CONFIGURATION_TITLE_TOP;
    private static int CONFIGURATION_TITLE_BOTTOM;
    private static int CONFIGURATION_TITLE_LEFT;
    private static int SECTION_VIEW_HEIGHT;
    private static int DESCRIPTION_MARGIN;

    private UUID mRoomId;

    private EditableView mEditableView;
    private RoundedView mAvatarBackgroundView;
    private ImageView mAvatarCameraView;
    private CircularImageView mAvatarView;
    private EditText mNameView;
    private View mContentWelcomeView;
    private EditText mWelcomeView;
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
    @Nullable
    private Menu mMenu;
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

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateOptionsMenu: menu=" + menu);
        }

        super.onCreateOptionsMenu(menu);

        mMenu = menu;

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.admin_group_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.save_action);
        String title = menuItem.getTitle().toString();

        TextView titleView = (TextView) menuItem.getActionView();

        if (titleView != null) {
            Design.updateTextFont(titleView, Design.FONT_BOLD36);
            titleView.setText(title.toLowerCase());
            titleView.setTextColor(Color.WHITE);
            titleView.setAlpha(0.5f);
            titleView.setPadding(0, 0, Design.TOOLBAR_TEXT_ITEM_PADDING, 0);
            titleView.setOnClickListener(view -> onSaveClick());
        }

        return true;
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

        if (mRoomWelcome != null) {
            mWelcomeView.setHint(mRoomWelcome);
        }

        if (mRoomWelcome != null && mWelcomeView.getText().toString().isEmpty()) {
            mWelcomeView.append(mRoomWelcome);
        } else {
            setUpdated();
        }

        mWelcomeView.addTextChangedListener(new TextWatcher() {

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
            Log.d(LOG_TAG, "onGetContactNotFound");
        }

        mRoom = contact;

        if (mRoom.hasPeer()) {
            mRoomAvatar = avatar;
            if (mRoomAvatar == null) {
                mRoomAvatar = getDefaultAvatar();
            }
            mRoomName = contact.getName();
            if (mRoomName == null) {
                mRoomName = getAnonymousName();
            }

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

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.admin_room_activity);

        View contentView = findViewById(R.id.admin_room_activity_content_view);
        contentView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        setStatusBarColor();
        setToolBar(R.id.admin_room_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);

        setTitle(getString(R.string.application_edit));

        mEditableView = new EditableView(this);

        View infoView = findViewById(R.id.admin_room_activity_info_view);
        ViewGroup.LayoutParams layoutParams = infoView.getLayoutParams();
        layoutParams.height = SECTION_VIEW_HEIGHT;

        mAvatarBackgroundView = findViewById(R.id.admin_room_activity_avatar_background_view);
        mAvatarBackgroundView.setColor(Design.EDIT_AVATAR_BACKGROUND_COLOR);

        mAvatarCameraView = findViewById(R.id.admin_room_activity_avatar_camera_view);
        mAvatarCameraView.setColorFilter(Design.EDIT_AVATAR_IMAGE_COLOR);

        mAvatarView = findViewById(R.id.admin_room_activity_avatar_view);
        mAvatarView.setVisibility(View.GONE);

        View avatarSelectableView = findViewById(R.id.admin_room_activity_avatar_selectable_view);
        avatarSelectableView.setOnClickListener(view -> openMenuPhoto());

        mNameView = findViewById(R.id.admin_room_activity_name_view);
        Design.updateTextFont(mNameView, Design.FONT_REGULAR32);
        mNameView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mNameView.setHintTextColor(DESIGN_HINT_COLOR);
        mNameView.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                updateRoom();
            }
            return false;
        });
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

        mContentWelcomeView = findViewById(R.id.admin_room_activity_welcome_view);
        ViewGroup.LayoutParams sectionLayoutParams = mContentWelcomeView.getLayoutParams();
        sectionLayoutParams.height = SECTION_VIEW_HEIGHT;
        mContentWelcomeView.setLayoutParams(sectionLayoutParams);

        mWelcomeView = findViewById(R.id.admin_room_activity_welcome_text_view);
        Design.updateTextFont(mWelcomeView, Design.FONT_REGULAR32);
        mWelcomeView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mWelcomeView.setHintTextColor(DESIGN_HINT_COLOR);

        mWelcomeView.addTextChangedListener(new TextWatcher() {

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

        TextView configurationTitleTextView = findViewById(R.id.admin_room_activity_configuration_title);
        Design.updateTextFont(configurationTitleTextView, Design.FONT_BOLD28);
        configurationTitleTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        ViewGroup.MarginLayoutParams titleLayoutParams = (ViewGroup.MarginLayoutParams) configurationTitleTextView.getLayoutParams();
        titleLayoutParams.topMargin = CONFIGURATION_TITLE_TOP;
        if (CommonUtils.isLayoutDirectionRTL()) {
            titleLayoutParams.rightMargin = CONFIGURATION_TITLE_LEFT;
        } else {
            titleLayoutParams.leftMargin = CONFIGURATION_TITLE_LEFT;
        }
        titleLayoutParams.bottomMargin = CONFIGURATION_TITLE_BOTTOM;
        configurationTitleTextView.setLayoutParams(titleLayoutParams);

        View inviteView = findViewById(R.id.admin_room_activity_invite_view);
        sectionLayoutParams = inviteView.getLayoutParams();
        sectionLayoutParams.height = SECTION_VIEW_HEIGHT;
        inviteView.setLayoutParams(sectionLayoutParams);

        TextView inviteTextView = findViewById(R.id.admin_room_activity_invite_text);
        Design.updateTextFont(inviteTextView, Design.FONT_REGULAR32);
        inviteTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        inviteTextView.setOnClickListener(v -> onInviteClick());

        View settingsView = findViewById(R.id.admin_room_activity_settings_view);
        sectionLayoutParams = settingsView.getLayoutParams();
        sectionLayoutParams.height = SECTION_VIEW_HEIGHT;
        settingsView.setLayoutParams(sectionLayoutParams);

        TextView settingsTextView = findViewById(R.id.admin_room_activity_settings_text);
        Design.updateTextFont(settingsTextView, Design.FONT_REGULAR32);
        settingsTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        settingsView.setOnClickListener(v -> onSettingsClick());

        View removeView = findViewById(R.id.admin_room_activity_remove_view);
        layoutParams = removeView.getLayoutParams();
        layoutParams.height = SECTION_VIEW_HEIGHT;

        mRemoveListener = new RemoveListener();
        removeView.setOnClickListener(mRemoveListener);

        TextView removeLabelView = findViewById(R.id.admin_room_activity_remove_label_view);
        Design.updateTextFont(removeLabelView, Design.FONT_REGULAR34);
        removeLabelView.setTextColor(Design.FONT_COLOR_RED);

        mProgressBarView = findViewById(R.id.admin_group_activity_progress_bar);

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
        mRoomWelcome = mWelcomeView.getText().toString();

        int welcomeHeight = mWelcomeView.getLineCount() * mWelcomeView.getLineHeight() + DESCRIPTION_MARGIN * 2;
        ViewGroup.LayoutParams sectionLayoutParams = mContentWelcomeView.getLayoutParams();
        sectionLayoutParams.height = Math.max(welcomeHeight, SECTION_VIEW_HEIGHT);
        mContentWelcomeView.setLayoutParams(sectionLayoutParams);

        boolean updatedRoomConfig = false;

        if (mRoomConfig != null && !mRoomWelcome.equals(mRoomConfig.getWelcome())) {
            updatedRoomConfig = true;
        }

        if (mRoomName.equals(mRoom.getName()) && mUpdatedRoomAvatar == null && !updatedRoomConfig) {
            if (!mCanSave) {
                return;
            }
            mCanSave = false;
            if (mMenu != null) {
                MenuItem saveMenuItem = mMenu.findItem(R.id.save_action);
                saveMenuItem.getActionView().setAlpha(0.5f);
                saveMenuItem.setEnabled(false);
            }
        } else {
            if (mCanSave) {
                return;
            }
            mCanSave = true;
            if (mMenu != null) {
                MenuItem saveMenuItem = mMenu.findItem(R.id.save_action);
                saveMenuItem.getActionView().setAlpha(1.0f);
                saveMenuItem.setEnabled(true);
            }
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

    private void onSaveClick() {
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
        String updatedWelcome = mWelcomeView.getText().toString();
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
            mAvatarBackgroundView.setVisibility(View.GONE);
            mAvatarCameraView.setVisibility(View.GONE);
            mAvatarView.setVisibility(View.VISIBLE);
            mAvatarView.setImage(this, null, new CircularImageDescriptor(avatar, 0.5f, 0.5f, 0.5f));
        }
    }

    private void onInviteClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onInviteClick");
        }

        if (mRoom.getPublicPeerTwincodeOutboundId() != null) {
            Intent intent = new Intent(this, InvitationRoomActivity.class);
            intent.putExtra(Intents.INTENT_CONTACT_ID, mRoom.getId().toString());
            intent.putExtra(Intents.INTENT_ROOM_NAME, mRoom.getName());
            startActivity(intent);
        }
    }

    private void onSettingsClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSettingsClick");
        }

        if (mRoom != null) {
            startActivity(SettingsRoomActivity.class, Intents.INTENT_CONTACT_ID, mRoom.getId());
        }
    }

    private void onRemoveClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRemoveClick");
        }

        PercentRelativeLayout percentRelativeLayout = findViewById(R.id.admin_room_activity_content_view);

        DefaultConfirmView defaultConfirmView = new DefaultConfirmView(this, null);
        PercentRelativeLayout.LayoutParams layoutParams = new PercentRelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        defaultConfirmView.setLayoutParams(layoutParams);
        defaultConfirmView.setTitle(getString(R.string.deleted_account_activity_warning));
        defaultConfirmView.setMessage(getString(R.string.application_delete_message));
        defaultConfirmView.setImage(null);
        defaultConfirmView.setConfirmColor(Design.DELETE_COLOR_RED);
        defaultConfirmView.setConfirmTitle(getString(R.string.application_delete));

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

                if (fromConfirmAction) {
                    mEditRoomService.deleteRoom(mRoom);
                } else {
                    mRemoveListener.enable();
                }
            }
        };
        defaultConfirmView.setObserver(observer);
        percentRelativeLayout.addView(defaultConfirmView);
        defaultConfirmView.show();

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }

    private void openMenuPhoto() {
        if (DEBUG) {
            Log.d(LOG_TAG, "openMenuPhoto");
        }

        hideKeyboard();

        PercentRelativeLayout percentRelativeLayout = findViewById(R.id.admin_room_activity_content_view);

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
        }
    }

    @Override
    public void setupDesign() {
        if (DEBUG) {
            Log.d(LOG_TAG, "setupDesign");
        }

        CONFIGURATION_TITLE_TOP = (int) (DESIGN_CONFIGURATION_TITLE_TOP * Design.HEIGHT_RATIO);
        CONFIGURATION_TITLE_BOTTOM = (int) (DESIGN_CONFIGURATION_TITLE_BOTTOM * Design.HEIGHT_RATIO);
        CONFIGURATION_TITLE_LEFT = (int) (DESIGN_CONFIGURATION_TITLE_LEFT * Design.WIDTH_RATIO);
        SECTION_VIEW_HEIGHT = Design.SECTION_HEIGHT;
        DESCRIPTION_MARGIN = (int) (DESIGN_DESCRIPTION_MARGIN * Design.HEIGHT_RATIO);
    }
}
