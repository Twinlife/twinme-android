/*
 *  Copyright (c) 2022-2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Romain Kolb (romain.kolb@skyrock.com)
 */

package org.twinlife.twinme.ui.spaces;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.models.Space;
import org.twinlife.twinme.models.SpaceSettings;
import org.twinlife.twinme.services.SpaceService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.skin.DisplayMode;
import org.twinlife.twinme.ui.Intents;

import java.util.UUID;

public class SettingsSpaceActivity extends AbstractSpaceActivity {
    private static final String LOG_TAG = "SettingsSpaceActivity";
    private static final boolean DEBUG = false;

    private SettingsSpaceAdapter mSettingsSpaceAdapter;

    private Space mSpace;
    private SpaceService mSpaceService;

    private boolean mDefaultAppearanceSettings = true;
    private boolean mDefaultMessageSettings = true;
    private boolean mDefaultNotificationSettings = true;

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
        mSettingsSpaceAdapter.setSpace(mSpace);
        updateSettings();
        updateColor();
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

        if (space.getId().equals(mSpace.getId())) {
            mSpace = space;
        }

        if (getTwinmeApplication().isCurrentSpace(mSpace.getId())) {
            SpaceSettings spaceSettings = mSpace.getSpaceSettings();
            if (spaceSettings.getBoolean(SpaceSettingProperty.PROPERTY_DEFAULT_APPEARANCE_SETTINGS, true)) {
                spaceSettings = getTwinmeContext().getDefaultSpaceSettings();
            }

            getTwinmeApplication().updateDisplayMode(Design.getDisplayMode(Integer.parseInt(spaceSettings.getString(SpaceSettingProperty.PROPERTY_DISPLAY_MODE, DisplayMode.SYSTEM.ordinal() + ""))));
            Design.setMainStyle(spaceSettings.getStyle());
        }

        updateColor();
        mSettingsSpaceAdapter.notifyDataSetChanged();
    }

    @Override
    public void updateColor() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateColor");
        }

        if (mSpace != null && getTwinmeApplication().isCurrentSpace(mSpace.getId())) {
            Design.setupColor(this, getTwinmeApplication());
            Design.setTheme(this, getTwinmeApplication());
            setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
            setStatusBarColor();
            setToolBar(R.id.settings_space_activity_tool_bar);
            applyInsets(R.id.settings_space_activity_content_view, R.id.settings_space_activity_tool_bar, R.id.settings_space_activity_list_view, Design.TOOLBAR_COLOR, false);
        }
    }

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.settings_space_activity);

        setStatusBarColor();
        setToolBar(R.id.settings_space_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);

        setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        setTitle(getString(R.string.navigation_activity_settings));

        applyInsets(R.id.settings_space_activity_content_view, R.id.settings_space_activity_tool_bar, R.id.settings_space_activity_list_view, Design.TOOLBAR_COLOR, false);

        SettingsSpaceAdapter.OnSettingsSpaceClickListener onSettingsSpaceClickListener = new SettingsSpaceAdapter.OnSettingsSpaceClickListener() {
            @Override
            public void onSettingsAppearanceClick() {

                onApppearanceSettingsClick();
            }

            @Override
            public void onSettingsMessageClick() {

                onMessageSettingsClick();
            }

            @Override
            public void onSettingsNotificationClick() {

                onNotificationSettingsClick();
            }
        };

        SettingSpaceViewHolder.Observer settingSpaceViewHolderObserver = this::onSettingsSpaceChangeValue;

        mSettingsSpaceAdapter = new SettingsSpaceAdapter(this, onSettingsSpaceClickListener, settingSpaceViewHolderObserver);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        RecyclerView settingsRecyclerView = findViewById(R.id.settings_space_activity_list_view);
        settingsRecyclerView.setLayoutManager(linearLayoutManager);
        settingsRecyclerView.setAdapter(mSettingsSpaceAdapter);
        settingsRecyclerView.setItemAnimator(null);
        settingsRecyclerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
    }

    private void updateSettings() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateSettings");
        }

        if (mSpace != null) {
            mDefaultAppearanceSettings = mSpace.getSpaceSettings().getBoolean(SpaceSettingProperty.PROPERTY_DEFAULT_APPEARANCE_SETTINGS, true);
            mDefaultMessageSettings = mSpace.getSpaceSettings().getBoolean(SpaceSettingProperty.PROPERTY_DEFAULT_MESSAGE_SETTINGS, true);
            mDefaultNotificationSettings = mSpace.getSpaceSettings().getBoolean(SpaceSettingProperty.PROPERTY_DEFAULT_NOTIFICATION_SETTINGS, true);
        }
    }

    public void onSettingsSpaceChangeValue(String spaceSettingProperty, boolean value) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSettingsSpaceChangeValue = " + spaceSettingProperty + " value " + value);
        }

        switch (spaceSettingProperty) {
            case SpaceSettingProperty.PROPERTY_DEFAULT_APPEARANCE_SETTINGS:
                mDefaultAppearanceSettings = value;
                break;
            case SpaceSettingProperty.PROPERTY_DEFAULT_MESSAGE_SETTINGS:
                mDefaultMessageSettings = value;
                break;
            case SpaceSettingProperty.PROPERTY_DEFAULT_NOTIFICATION_SETTINGS:
                mDefaultNotificationSettings = value;
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
            spaceSettings.setBoolean(SpaceSettingProperty.PROPERTY_DEFAULT_APPEARANCE_SETTINGS, mDefaultAppearanceSettings);
            spaceSettings.setBoolean(SpaceSettingProperty.PROPERTY_DEFAULT_MESSAGE_SETTINGS, mDefaultMessageSettings);
            spaceSettings.setBoolean(SpaceSettingProperty.PROPERTY_DEFAULT_NOTIFICATION_SETTINGS, mDefaultNotificationSettings);
            mSpaceService.updateSpace(mSpace, spaceSettings, null, null);
        } else {
            Intent data = new Intent();
            setResult(RESULT_OK, data);
            finish();
        }
    }

    private void onMessageSettingsClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onMessageSettingsClick");
        }

        if (!mDefaultMessageSettings) {
            Intent intent = new Intent();
            intent.putExtra(Intents.INTENT_SPACE_ID, mSpace.getId().toString());
            intent.setClass(this, MessageSettingsSpaceActivity.class);
            startActivity(intent);
        }
    }

    private void onApppearanceSettingsClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onApppearanceSettingsClick");
        }

        if (!mDefaultAppearanceSettings) {
            Intent intent = new Intent();
            intent.putExtra(Intents.INTENT_SPACE_ID, mSpace.getId().toString());
            intent.setClass(this, SpaceAppearanceActivity.class);
            startActivity(intent);
        }
    }

    private void onNotificationSettingsClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onNotificationSettingsClick");
        }

        if (!mDefaultNotificationSettings) {
            Intent intent = new Intent();
            intent.putExtra(Intents.INTENT_SPACE_ID, mSpace.getId().toString());
            intent.setClass(this, NotificationSpaceActivity.class);
            startActivity(intent);
        }
    }
}
