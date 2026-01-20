/*
 *  Copyright (c) 2019-2022 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui.spaces;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.models.Space;
import org.twinlife.twinme.models.SpaceSettings;
import org.twinlife.twinme.services.SpaceService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.privacyActivity.UITimeout;
import org.twinlife.twinme.ui.settingsActivity.MenuSelectValueView;

import java.util.UUID;

public class MessageSettingsSpaceActivity extends AbstractSpaceActivity implements MenuSelectValueView.Observer {
    private static final String LOG_TAG = "MessageSettingsSpac...";
    private static final boolean DEBUG = false;

    private View mOverlayView;
    private MenuSelectValueView mMenuTimeoutView;

    private MessageSettingsSpaceAdapter mMessageSettingsSpaceAdapter;

    private boolean mAllowCopyText;
    private boolean mAllowCopyFile;
    private boolean mAllowEphemeral;
    private long mExpireTimeout;

    private Space mSpace;
    private SpaceService mSpaceService;

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

        mAllowCopyText = intent.getBooleanExtra(Intents.INTENT_ALLOW_COPY_TEXT, true);
        mAllowCopyFile = intent.getBooleanExtra(Intents.INTENT_ALLOW_COPY_FILE, true);
        mAllowEphemeral = intent.getBooleanExtra(Intents.INTENT_ALLOW_EPHEMERAL, false);
        mExpireTimeout = intent.getLongExtra(Intents.INTENT_EXPIRE_TIMEOUT, 0);

        initViews();

        UUID spaceId = org.twinlife.twinlife.util.Utils.UUIDFromString(intent.getStringExtra(Intents.INTENT_SPACE_ID));
        if (spaceId != null) {
            mSpaceService = new SpaceService(this, getTwinmeContext(), this, spaceId);
        } else {
            // We are selecting some settings for a new space: the mSpace instance will always be null.
            mSpaceService = new SpaceService(this, getTwinmeContext(), this);
        }
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
    }

    //
    // Override Activity methods
    //

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        super.onDestroy();

        mSpaceService.dispose();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSaveInstanceState: outState=" + outState);
        }

        super.onSaveInstanceState(outState);
    }

    //
    // Implement SpaceService.Observer methods
    //

    @Override
    public void onGetSpace(@NonNull Space space, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetSpace: space=" + space);
        }

        mSpace = space;
        mMessageSettingsSpaceAdapter.setSpace(mSpace);
        updateSettings();
    }

    @Override
    public void onGetSpaceNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetSpaceNotFound");
        }

        // Activity was called with a spaceId that is no longer valid.
        finish();
    }

    @Override
    public void onUpdateSpace(@NonNull Space space) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateSpace: space=" + space);
        }

        mSpace = space;
        mMessageSettingsSpaceAdapter.setSpace(mSpace);
    }

    //
    // Implement MenuSelectValueView.Observer methods
    //

    @Override
    public void onCloseMenuAnimationEnd() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCloseMenuAnimationEnd");
        }

        closeTimeout();
    }

    @Override
    public void onSelectValue(int value) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSelectValue: value=" + value);
        }
    }

    @Override
    public void onSelectTimeout(UITimeout timeout) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSelectTimeout: timeout=" + timeout);
        }

        mExpireTimeout = timeout.getDelay();
        closeTimeout();
        saveSpaceSettings();

    }

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.message_settings_space_activity);

        setStatusBarColor();
        setToolBar(R.id.message_settings_space_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);

        setTitle(getString(R.string.settings_activity_chat_category_title));

        applyInsets(R.id.message_settings_space_activity_content_view, R.id.message_settings_space_activity_tool_bar, R.id.message_settings_space_activity_list_view, Design.TOOLBAR_COLOR, false);

        View contentView = findViewById(R.id.message_settings_space_activity_content_view);
        contentView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        
        SettingSpaceViewHolder.Observer settingSpaceViewHolderObserver = this::onSettingsSpaceChangeValue;

        SettingValueSpaceViewHolder.Observer settingValueSpaceViewHolderObserver = spaceSettingProperty -> onTimeoutClick();

        mMessageSettingsSpaceAdapter = new MessageSettingsSpaceAdapter(this, settingSpaceViewHolderObserver, settingValueSpaceViewHolderObserver);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        RecyclerView settingsRecyclerView = findViewById(R.id.message_settings_space_activity_list_view);
        settingsRecyclerView.setLayoutManager(linearLayoutManager);
        settingsRecyclerView.setAdapter(mMessageSettingsSpaceAdapter);
        settingsRecyclerView.setItemAnimator(null);
        settingsRecyclerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        mMenuTimeoutView = findViewById(R.id.message_settings_space_activity_menu_timeout_view);
        mMenuTimeoutView.setVisibility(View.INVISIBLE);
        mMenuTimeoutView.setObserver(this);
        mMenuTimeoutView.setActivity(this);

        mOverlayView = findViewById(R.id.message_settings_space_activity_overlay_view);
        mOverlayView.setBackgroundColor(Design.OVERLAY_VIEW_COLOR);
        mOverlayView.setOnClickListener(view -> closeTimeout());
    }

    private void updateSettings() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateSettings");
        }

        if (mSpace != null) {
            mAllowCopyText = mSpace.getSpaceSettings().messageCopyAllowed();
            mAllowCopyFile = mSpace.getSpaceSettings().fileCopyAllowed();
            mAllowEphemeral = mSpace.getSpaceSettings().getBoolean(SpaceSettingProperty.PROPERTY_ALLOW_EPHEMERAL_MESSAGE, false);
            mExpireTimeout = Long.parseLong(mSpace.getSpaceSettings().getString(SpaceSettingProperty.PROPERTY_TIMEOUT_EPHEMERAL_MESSAGE, SpaceSettingProperty.DEFAULT_TIMEOUT_MESSAGE + ""));
        }
    }

    public void onSettingsSpaceChangeValue(String spaceSettingProperty, boolean value) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSettingsSpaceChangeValue = " + spaceSettingProperty + " value " + value);
        }

        switch (spaceSettingProperty) {
            case SpaceSettingProperty.PROPERTY_ALLOW_COPY_TEXT:
                mAllowCopyText = value;
                break;
            case SpaceSettingProperty.PROPERTY_ALLOW_COPY_FILE:
                mAllowCopyFile = value;
                break;
            case SpaceSettingProperty.PROPERTY_ALLOW_EPHEMERAL_MESSAGE:
                mAllowEphemeral = value;
                break;
        }

        saveSpaceSettings();
    }

    private void saveSpaceSettings() {
        if (DEBUG) {
            Log.d(LOG_TAG, "saveSpaceSettings");
        }

        if (mSpace != null) {
            SpaceSettings spaceSettings = mSpace.getSpaceSettings();
            spaceSettings.setStyle(mSpace.getStyle());
            spaceSettings.setFileCopyAllowed(mAllowCopyFile);
            spaceSettings.setMessageCopyAllowed(mAllowCopyText);
            spaceSettings.setBoolean(SpaceSettingProperty.PROPERTY_ALLOW_EPHEMERAL_MESSAGE, mAllowEphemeral);
            spaceSettings.setString(SpaceSettingProperty.PROPERTY_TIMEOUT_EPHEMERAL_MESSAGE, mExpireTimeout + "");
            mSpaceService.updateSpace(mSpace, spaceSettings, null, null);
        } else {
            Intent data = new Intent();
            data.putExtra(Intents.INTENT_ALLOW_COPY_TEXT, mAllowCopyFile);
            data.putExtra(Intents.INTENT_ALLOW_COPY_FILE, mAllowCopyFile);
            data.putExtra(Intents.INTENT_ALLOW_EPHEMERAL, mAllowEphemeral);
            data.putExtra(Intents.INTENT_EXPIRE_TIMEOUT, mExpireTimeout);
            setResult(RESULT_OK, data);
            finish();
        }
    }

    private void onTimeoutClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onTimeoutClick");
        }

        if (mMenuTimeoutView.getVisibility() != View.VISIBLE) {
            mOverlayView.setVisibility(View.VISIBLE);
            mMenuTimeoutView.setVisibility(View.VISIBLE);
            mMenuTimeoutView.setSelectedValue((int) mExpireTimeout);
            mMenuTimeoutView.openMenu(MenuSelectValueView.MenuType.EPHEMERAL_MESSAGE, -1);
            int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
            setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
        }
    }

    private void closeTimeout() {
        if (DEBUG) {
            Log.d(LOG_TAG, "closeTimeout");
        }

        mOverlayView.setVisibility(View.INVISIBLE);
        mMenuTimeoutView.setVisibility(View.INVISIBLE);
        setStatusBarColor();
    }
}
