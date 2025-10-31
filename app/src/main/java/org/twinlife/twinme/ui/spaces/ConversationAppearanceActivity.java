/*
 *  Copyright (c) 2020-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 *   Romain Kolb (romain.kolb@skyrock.com)
 */

package org.twinlife.twinme.ui.spaces;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.percentlayout.widget.PercentRelativeLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ExportedImageId;
import org.twinlife.twinme.models.Space;
import org.twinlife.twinme.models.SpaceSettings;
import org.twinlife.twinme.services.SpaceAppearanceService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.skin.DisplayMode;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.profiles.MenuPhotoView;
import org.twinlife.twinme.utils.EditableView;
import org.twinlife.twinme.utils.UIMenuSelectAction;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ConversationAppearanceActivity extends AbstractTwinmeActivity implements SpaceAppearanceService.Observer {

    private static final String LOG_TAG = "ConversationAppeara...";
    private static final boolean DEBUG = false;

    private static final int REQUEST_COLOR_CODE = 1;

    private boolean mUIInitialized = false;

    private ConversationAppearanceAdapter mConversationAppearanceAdapter;
    private RecyclerView mRecyclerView;

    private Bitmap mConversationBackgroundLightBitmap;
    private File mConversationBackgroundLightFile;

    private Bitmap mConversationBackgroundDarkBitmap;
    private File mConversationBackgroundDarkFile;

    private boolean mUpdatedLightImage = false;
    private boolean mUpdatedDarkImage = false;
    private boolean mUpdatedConversationBackgroundLightColor = false;
    private boolean mUpdatedConversationBackgroundDarkColor = false;
    @Nullable
    private UUID mLightImageId;
    @Nullable
    private UUID mDarkImageId;

    private EditableView mEditableView;

    private boolean mOnlyConversationAppearance = false;
    private boolean mUpdateDefaultSettings = false;

    @Nullable
    private Menu mMenu;

    private boolean mCanSave = false;

    private int mSelectedPosition;
    private String mSelectedColor;
    private CustomAppearance mCustomAppearance;

    private Space mSpace;
    private SpaceAppearanceService mSpaceAppearanceService;

    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        Intent incomingIntent = getIntent();

        if (incomingIntent.hasExtra(Intents.INTENT_ONLY_CONVERSATION)) {
            mOnlyConversationAppearance = true;
        }

        if (incomingIntent.hasExtra(Intents.INTENT_DEFAULT_SPACE_SETTINGS)) {
            mUpdateDefaultSettings = true;
        }

        if (mUpdateDefaultSettings) {
            mCustomAppearance = new CustomAppearance(this, getTwinmeContext().getDefaultSpaceSettings());
            mLightImageId = mCustomAppearance.getConversationBackgroundImageId(DisplayMode.LIGHT);
            mDarkImageId = mCustomAppearance.getConversationBackgroundImageId(DisplayMode.DARK);
        } else {
            mCustomAppearance = new CustomAppearance();
            mCustomAppearance.setCurrentMode(this, Design.getDisplayMode(getTwinmeApplication().displayMode()));
        }

        initViews();

        mSpaceAppearanceService = new SpaceAppearanceService(this, getTwinmeContext(), this);

        if (mUpdateDefaultSettings) {
            if (mLightImageId != null) {
                mSpaceAppearanceService.getConversationImage(mLightImageId);
            }

            if (mDarkImageId != null) {
                mSpaceAppearanceService.getConversationImage(mDarkImageId);
            }
        }

        String value = incomingIntent.getStringExtra(Intents.INTENT_SPACE_ID);
        if (value != null) {
            mSpaceAppearanceService.getSpace(UUID.fromString(value));
        } else if (incomingIntent.hasExtra(Intents.INTENT_COLOR)) {
            mCustomAppearance.setCurrentMode(this, DisplayMode.LIGHT);
            mCustomAppearance.setMainColor(incomingIntent.getStringExtra(Intents.INTENT_COLOR));
            mCustomAppearance.setConversationBackgroundColor(incomingIntent.getIntExtra(CustomAppearance.PROPERTY_CONVERSATION_BACKGROUND_COLOR, 0));
            mCustomAppearance.setConversationBackgroundText(incomingIntent.getIntExtra(CustomAppearance.PROPERTY_CONVERSATION_BACKGROUND_TEXT, 0));
            mCustomAppearance.setMessageBackgroundColor(incomingIntent.getIntExtra(CustomAppearance.PROPERTY_MESSAGE_BACKGROUND_COLOR, 0));
            mCustomAppearance.setPeerMessageBackgroundColor(incomingIntent.getIntExtra(CustomAppearance.PROPERTY_PEER_MESSAGE_BACKGROUND_COLOR, 0));
            mCustomAppearance.setMessageBorderColor(incomingIntent.getIntExtra(CustomAppearance.PROPERTY_MESSAGE_BORDER_COLOR, 0));
            mCustomAppearance.setPeerMessageBorderColor(incomingIntent.getIntExtra(CustomAppearance.PROPERTY_PEER_MESSAGE_BORDER_COLOR, 0));
            mCustomAppearance.setMessageTextColor(incomingIntent.getIntExtra(CustomAppearance.PROPERTY_MESSAGE_TEXT_COLOR, 0));
            mCustomAppearance.setPeerMessageTextColor(incomingIntent.getIntExtra(CustomAppearance.PROPERTY_PEER_MESSAGE_TEXT_COLOR, 0));

            mCustomAppearance.setCurrentMode(this, DisplayMode.DARK);
            mCustomAppearance.setConversationBackgroundColor(incomingIntent.getIntExtra(CustomAppearance.PROPERTY_DARK_CONVERSATION_BACKGROUND_COLOR, 0));
            mCustomAppearance.setConversationBackgroundText(incomingIntent.getIntExtra(CustomAppearance.PROPERTY_DARK_CONVERSATION_BACKGROUND_TEXT, 0));
            mCustomAppearance.setMessageBackgroundColor(incomingIntent.getIntExtra(CustomAppearance.PROPERTY_DARK_MESSAGE_BACKGROUND_COLOR, 0));
            mCustomAppearance.setPeerMessageBackgroundColor(incomingIntent.getIntExtra(CustomAppearance.PROPERTY_DARK_PEER_MESSAGE_BACKGROUND_COLOR, 0));
            mCustomAppearance.setMessageBorderColor(incomingIntent.getIntExtra(CustomAppearance.PROPERTY_DARK_MESSAGE_BORDER_COLOR, 0));
            mCustomAppearance.setPeerMessageBorderColor(incomingIntent.getIntExtra(CustomAppearance.PROPERTY_DARK_PEER_MESSAGE_BORDER_COLOR, 0));
            mCustomAppearance.setMessageTextColor(incomingIntent.getIntExtra(CustomAppearance.PROPERTY_DARK_MESSAGE_TEXT_COLOR, 0));
            mCustomAppearance.setPeerMessageTextColor(incomingIntent.getIntExtra(CustomAppearance.PROPERTY_DARK_PEER_MESSAGE_TEXT_COLOR, 0));

            mCustomAppearance.setCurrentMode(this, DisplayMode.LIGHT);

            String conversationBackgroundLightPath = incomingIntent.getStringExtra(CustomAppearance.PROPERTY_CONVERSATION_BACKGROUND_IMAGE);
            if (conversationBackgroundLightPath != null) {
                mConversationBackgroundLightFile = new File(conversationBackgroundLightPath);
                mConversationBackgroundLightBitmap = BitmapFactory.decodeFile(conversationBackgroundLightPath);
            }

            String conversationBackgroundDarkPath = incomingIntent.getStringExtra(CustomAppearance.PROPERTY_DARK_CONVERSATION_BACKGROUND_IMAGE);
            if (conversationBackgroundDarkPath != null) {
                mConversationBackgroundDarkFile = new File(conversationBackgroundDarkPath);
                mConversationBackgroundDarkBitmap = BitmapFactory.decodeFile(conversationBackgroundDarkPath);
            }

            mConversationAppearanceAdapter.setConversationBackground(mConversationBackgroundLightBitmap);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onActivityResult requestCode=" + requestCode + " resultCode=" + resultCode + " data=" + data);
        }

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_COLOR_CODE) {
            if (data != null && data.hasExtra(Intents.INTENT_COLOR)) {
                String color = data.getStringExtra(Intents.INTENT_COLOR);

                if (mSelectedPosition == ConversationAppearanceAdapter.SPACE_COLOR_POSITION) {
                    updateMainColor(color);
                } else {
                    updateCustomAppearanceColor(color);
                }

                setUpdated();
            }
        } else if (mEditableView != null) {
            Uri image = mEditableView.onActivityResult(requestCode, resultCode, data);
            if (image != null) {
                updateSelectedImage();
            }
        }
    }

    @Override
    public void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        mSpaceAppearanceService.dispose();

        // Cleanup capture and cropped images.
        if (mEditableView != null) {
            if (mSpace != null) {
                mEditableView.onDestroy();
            }
        }

        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSaveInstanceState: outState=" + outState);
        }

        super.onSaveInstanceState(outState);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateOptionsMenu: menu=" + menu);
        }

        super.onCreateOptionsMenu(menu);

        mMenu = menu;

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.space_appearance_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.save_action);
        String title = menuItem.getTitle().toString();

        TextView titleView = (TextView) menuItem.getActionView();

        if (titleView != null) {
            titleView.setTypeface(Design.FONT_BOLD36.typeface);
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_BOLD36.size);
            titleView.setText(title.toLowerCase());
            titleView.setTextColor(Color.WHITE);
            titleView.setAlpha(0.5f);
            titleView.setPadding(0, 0, Design.TOOLBAR_TEXT_ITEM_PADDING, 0);
            titleView.setOnClickListener(view -> onSaveClick());
        }

        return true;
    }

    //
    // Implement SpaceAppearanceActivity.Observer methods
    //

    @Override
    public void onUpdateSpace(@NonNull Space space) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateSpace: space=" + space);
        }

        finish();
    }

    @Override
    public void onUpdateDefaultSpaceSettings(SpaceSettings spaceSettings) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateDefaultSpaceSettings: spaceSettings=" + spaceSettings);
        }

        finish();
    }

    @Override
    public void onGetSpace(@NonNull Space space, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetSpace: space=" + space);
        }

        mSpace = space;
        mCustomAppearance = new CustomAppearance(this, mSpace.getSpaceSettings());
        mConversationAppearanceAdapter.setCustomAppearance(mCustomAppearance);

        mLightImageId = mCustomAppearance.getConversationBackgroundImageId(DisplayMode.LIGHT);
        mDarkImageId = mCustomAppearance.getConversationBackgroundImageId(DisplayMode.DARK);

        if (mLightImageId != null) {
            mSpaceAppearanceService.getConversationImage(mLightImageId);
        }

        if (mDarkImageId != null) {
            mSpaceAppearanceService.getConversationImage(mDarkImageId);
        }

        notifyAppareanceListChanged();
    }

    @Override
    public void onGetSpaceNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetSpaceNotFound");
        }

        finish();
    }

    @Override
    public void onGetConversationImage(@NonNull ExportedImageId imageId, @NonNull Bitmap bitmap) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetConversationImage: imageId=" + imageId);
        }

        if (mLightImageId != null && mLightImageId.equals(imageId.getExportedId())) {
            mConversationBackgroundLightBitmap = bitmap;
        } else if (mDarkImageId != null && mDarkImageId.equals(imageId.getExportedId())) {
            mConversationBackgroundDarkBitmap = bitmap;
        }

        if (mCustomAppearance.getCurrentMode() == DisplayMode.LIGHT) {
            mConversationAppearanceAdapter.setConversationBackground(mConversationBackgroundLightBitmap);
        } else {
            mConversationAppearanceAdapter.setConversationBackground(mConversationBackgroundDarkBitmap);
        }

        notifyAppareanceListChanged();
    }

    public boolean isUpdateDefaultSettings() {
        if (DEBUG) {
            Log.d(LOG_TAG, "isUpdateDefaultSettings");
        }

        return mUpdateDefaultSettings;
    }

    //
    // Private methods
    //

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.conversation_appearance_activity);

        setStatusBarColor();
        setToolBar(R.id.conversation_appearance_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);

        setTitle(getString(R.string.application_appearance));
        setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        applyInsets(R.id.conversation_appearance_activity_layout, R.id.conversation_appearance_activity_tool_bar, R.id.conversation_appearance_activity_list_view, Design.TOOLBAR_COLOR, false);

        mEditableView = new EditableView(this);
        mEditableView.setEditableAvatar(false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        mRecyclerView = findViewById(R.id.conversation_appearance_activity_list_view);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemViewCacheSize(Design.ITEM_LIST_CACHE_SIZE);
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        mProgressBarView = findViewById(R.id.conversation_appearance_activity_progress_bar);

        ConversationAppearanceAdapter.OnAppearanceClickListener appearanceClickListener = new ConversationAppearanceAdapter.OnAppearanceClickListener() {

            @Override
            public void onColorClick(int position, String title, int color, int defaultColor) {
                mSelectedColor = title;
                mSelectedPosition = position;

                if (position == ConversationAppearanceAdapter.BACKGROUND_COLOR_POSITION) {
                    openMenuBackground();
                } else {
                    openMenuColor(title, color, defaultColor);
                }
            }

            @Override
            public void onResetAppearanceClick() {

                mCustomAppearance.resetToDefaultValues();
                notifyAppareanceListChanged();
                setUpdated();
            }
        };

        mConversationAppearanceAdapter = new ConversationAppearanceAdapter(this, appearanceClickListener, mCustomAppearance, null, mOnlyConversationAppearance);
        mRecyclerView.setAdapter(mConversationAppearanceAdapter);

        mUIInitialized = true;
    }

    private void notifyAppareanceListChanged() {
        if (DEBUG) {
            Log.d(LOG_TAG, "notifyAppareanceListChanged");
        }

        if (mUIInitialized) {
            mRecyclerView.invalidate();
            mConversationAppearanceAdapter.notifyDataSetChanged();
        }
    }

    private void setUpdated() {
        if (DEBUG) {
            Log.d(LOG_TAG, "setUpdated");
        }

        if (!mUIInitialized) {
            return;
        }

        mCanSave = true;
        if (mMenu != null) {
            MenuItem saveMenuItem = mMenu.findItem(R.id.save_action);
            saveMenuItem.getActionView().setAlpha(1.0f);
            saveMenuItem.setEnabled(true);
        }
    }

    private void onSaveClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSaveClick");
        }

        if (!mCanSave) {
            return;
        }

        if (mSpace != null || mUpdateDefaultSettings) {

            Bitmap updateLightBitmap = null;
            File updateLightFile = null;
            Bitmap updateDarkBitmap = null;
            File updateDarkFile = null;

            if (mUpdatedLightImage) {
                updateLightBitmap = mConversationBackgroundLightBitmap;
                updateLightFile = mConversationBackgroundLightFile;
            }

            if (mUpdatedDarkImage) {
                updateDarkBitmap = mConversationBackgroundDarkBitmap;
                updateDarkFile = mConversationBackgroundDarkFile;
            }

            mSpaceAppearanceService.updateSpace(mCustomAppearance.getSpaceSettings(), updateLightBitmap, updateLightFile, updateDarkBitmap, updateDarkFile, mUpdatedConversationBackgroundLightColor, mUpdatedConversationBackgroundDarkColor);
        } else {
            Intent data = new Intent();
            data.putExtra(Intents.INTENT_COLOR, mCustomAppearance.getSpaceSettings().getStyle());
            mCustomAppearance.setCurrentMode(this, DisplayMode.LIGHT);
            data.putExtra(CustomAppearance.PROPERTY_CONVERSATION_BACKGROUND_COLOR, mCustomAppearance.getConversationBackgroundColor());
            data.putExtra(CustomAppearance.PROPERTY_CONVERSATION_BACKGROUND_TEXT, mCustomAppearance.getConversationBackgroundText());
            data.putExtra(CustomAppearance.PROPERTY_MESSAGE_BACKGROUND_COLOR, mCustomAppearance.getMessageBackgroundColor());
            data.putExtra(CustomAppearance.PROPERTY_PEER_MESSAGE_BACKGROUND_COLOR, mCustomAppearance.getPeerMessageBackgroundColor());
            data.putExtra(CustomAppearance.PROPERTY_MESSAGE_BORDER_COLOR, mCustomAppearance.getMessageBorderColor());
            data.putExtra(CustomAppearance.PROPERTY_PEER_MESSAGE_BORDER_COLOR, mCustomAppearance.getPeerMessageBorderColor());
            data.putExtra(CustomAppearance.PROPERTY_MESSAGE_TEXT_COLOR, mCustomAppearance.getMessageTextColor());
            data.putExtra(CustomAppearance.PROPERTY_PEER_MESSAGE_TEXT_COLOR, mCustomAppearance.getPeerMessageTextColor());
            mCustomAppearance.setCurrentMode(this, DisplayMode.DARK);
            data.putExtra(CustomAppearance.PROPERTY_DARK_CONVERSATION_BACKGROUND_COLOR, mCustomAppearance.getConversationBackgroundColor());
            data.putExtra(CustomAppearance.PROPERTY_DARK_CONVERSATION_BACKGROUND_TEXT, mCustomAppearance.getConversationBackgroundText());
            data.putExtra(CustomAppearance.PROPERTY_DARK_MESSAGE_BACKGROUND_COLOR, mCustomAppearance.getMessageBackgroundColor());
            data.putExtra(CustomAppearance.PROPERTY_DARK_PEER_MESSAGE_BACKGROUND_COLOR, mCustomAppearance.getPeerMessageBackgroundColor());
            data.putExtra(CustomAppearance.PROPERTY_DARK_MESSAGE_BORDER_COLOR, mCustomAppearance.getMessageBorderColor());
            data.putExtra(CustomAppearance.PROPERTY_DARK_PEER_MESSAGE_BORDER_COLOR, mCustomAppearance.getConversationBackgroundColor());
            data.putExtra(CustomAppearance.PROPERTY_DARK_MESSAGE_TEXT_COLOR, mCustomAppearance.getConversationBackgroundColor());
            data.putExtra(CustomAppearance.PROPERTY_DARK_PEER_MESSAGE_TEXT_COLOR, mCustomAppearance.getPeerMessageTextColor());

            if (mConversationBackgroundLightFile != null) {
                data.putExtra(CustomAppearance.PROPERTY_CONVERSATION_BACKGROUND_IMAGE, mConversationBackgroundLightFile.getAbsolutePath());
            }

            if (mConversationBackgroundDarkFile != null) {
                data.putExtra(CustomAppearance.PROPERTY_DARK_CONVERSATION_BACKGROUND_IMAGE, mConversationBackgroundDarkFile.getAbsolutePath());
            }

            setResult(RESULT_OK, data);
            finish();
        }
    }

    private void updateMainColor(String color) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateMainColor");
        }

        mCustomAppearance.setMainColor(color);

        notifyAppareanceListChanged();
    }

    private void updateCustomAppearanceColor(String color) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateCustomAppearanceColor: color=" + color);
        }

        switch (mSelectedPosition) {
            case ConversationAppearanceAdapter.BACKGROUND_COLOR_POSITION:
                if (mCustomAppearance.getCurrentMode() == DisplayMode.DARK) {
                    mConversationBackgroundDarkFile = null;
                    mConversationBackgroundDarkBitmap = null;
                    mUpdatedConversationBackgroundDarkColor = true;
                } else {
                    mConversationBackgroundLightFile = null;
                    mConversationBackgroundLightBitmap = null;
                    mUpdatedConversationBackgroundLightColor = true;
                }
                mCustomAppearance.setConversationBackgroundColor(color);
                mConversationAppearanceAdapter.setConversationBackground(null);
                break;

            case ConversationAppearanceAdapter.BACKGROUND_TEXT_POSITION:
                mCustomAppearance.setConversationBackgroundText(color);
                break;

            case ConversationAppearanceAdapter.ITEM_BACKGROUND_COLOR_POSITION:
                mCustomAppearance.setMessageBackgroundColor(color);
                break;

            case ConversationAppearanceAdapter.PEER_ITEM_BACKGROUND_COLOR_POSITION:
                mCustomAppearance.setPeerMessageBackgroundColor(color);
                break;

            case ConversationAppearanceAdapter.ITEM_BORDER_COLOR_POSITION:
                mCustomAppearance.setMessageBorderColor(color);
                break;

            case ConversationAppearanceAdapter.PEER_ITEM_BORDER_COLOR_POSITION:
                mCustomAppearance.setPeerMessageBorderColor(color);
                break;

            case ConversationAppearanceAdapter.ITEM_TEXT_COLOR_POSITION:
                mCustomAppearance.setMessageTextColor(color);
                break;

            case ConversationAppearanceAdapter.PEER_ITEM_TEXT_COLOR_POSITION:
                mCustomAppearance.setPeerMessageTextColor(color);
                break;

            default:
                break;
        }

        notifyAppareanceListChanged();
    }

    private void updateSelectedImage() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateSelectedImage");
        }

        mEditableView.getSelectedImage((String path, Bitmap bitmap, Bitmap largeImage) -> {
            if (mCustomAppearance.getCurrentMode() == DisplayMode.LIGHT) {
                mUpdatedLightImage = true;
                mUpdatedConversationBackgroundLightColor = false;
                mConversationBackgroundLightBitmap = bitmap;
                mConversationBackgroundLightFile = new File(path);
            } else {
                mUpdatedDarkImage = true;
                mUpdatedConversationBackgroundDarkColor = false;
                mConversationBackgroundDarkBitmap = bitmap;
                mConversationBackgroundDarkFile = new File(path);
            }

            mConversationAppearanceAdapter.setConversationBackground(bitmap);

            setUpdated();
            notifyAppareanceListChanged();
        });
    }

    private void openMenuBackground() {
        if (DEBUG) {
            Log.d(LOG_TAG, "openMenuPhoto");
        }

        PercentRelativeLayout percentRelativeLayout = findViewById(R.id.conversation_appearance_activity_layout);

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
            public void onBackgroundColorClick() {
                percentRelativeLayout.removeView(menuPhotoView);
                openMenuColor(getString(R.string.space_appearance_activity_background_title), mCustomAppearance.getConversationBackgroundColor(), mCustomAppearance.getConversationBackgroundDefaultColor());
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
        actions.add(new UIMenuSelectAction(getString(R.string.application_color), R.drawable.color_icon));
        menuPhotoView.setActions(actions, this);
        menuPhotoView.openMenu(true);

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }

    private void openMenuColor(String title, int color, int defaultColor) {
        if (DEBUG) {
            Log.d(LOG_TAG, "openMenuColor");
        }

        PercentRelativeLayout percentRelativeLayout = findViewById(R.id.conversation_appearance_activity_layout);

        MenuSelectColorView menuSelectColorView = new MenuSelectColorView(this, null);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        menuSelectColorView.setLayoutParams(layoutParams);

        MenuSelectColorView.OnMenuColorListener onMenuColorListener = new MenuSelectColorView.OnMenuColorListener() {
            @Override
            public void onSelectedColor(String color) {

                menuSelectColorView.animationCloseMenu();
                if (mSelectedPosition == ConversationAppearanceAdapter.SPACE_COLOR_POSITION) {
                    updateMainColor(color);
                } else {
                    updateCustomAppearanceColor(color);
                }

                setUpdated();
            }

            @Override
            public void onResetColor() {

                onSelectedColor(null);
            }

            @Override
            public void onCloseMenu() {
                percentRelativeLayout.removeView(menuSelectColorView);
                setStatusBarColor();
            }
        };

        menuSelectColorView.setOnMenuColorListener(onMenuColorListener);
        menuSelectColorView.setAppearanceActivity(this);
        percentRelativeLayout.addView(menuSelectColorView);

        String hexColor = colorIntToHex(color);
        String hexDefaultColor = colorIntToHex(defaultColor);
        menuSelectColorView.openMenu(title, hexColor, hexDefaultColor);

        int statusBarColor = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(statusBarColor, Design.POPUP_BACKGROUND_COLOR);
    }

    private String colorIntToHex(int color) {
        return "#" + String.format("%08x",color).substring(2).toUpperCase();
    }

    private void selectDisplayMode(DisplayMode mode) {
        if (DEBUG) {
            Log.d(LOG_TAG, "selectDisplayMode mode=" + mode);
        }

        mCustomAppearance.setCurrentMode(this, mode);

        if (mode == DisplayMode.LIGHT) {
            mConversationAppearanceAdapter.setConversationBackground(mConversationBackgroundLightBitmap);
        } else {
            mConversationAppearanceAdapter.setConversationBackground(mConversationBackgroundDarkBitmap);
        }

        notifyAppareanceListChanged();
    }

    @Override
    public void updateColor() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateColor");
        }

        super.updateColor();

        setToolBar(R.id.conversation_appearance_activity_tool_bar);
        setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        setStatusBarColor();
        applyInsets(R.id.conversation_appearance_activity_layout, R.id.conversation_appearance_activity_tool_bar, R.id.conversation_appearance_activity_list_view, Design.TOOLBAR_COLOR, false);
        setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        mRecyclerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
    }
}
