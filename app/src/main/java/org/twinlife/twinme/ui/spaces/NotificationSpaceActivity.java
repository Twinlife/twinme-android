/*
 *  Copyright (c) 2022 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.spaces;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.models.Space;
import org.twinlife.twinme.models.SpaceSettings;
import org.twinlife.twinme.services.SpaceService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.Intents;

import java.util.UUID;

public class NotificationSpaceActivity extends AbstractSpaceActivity {
    private static final String LOG_TAG = "NotificationSpaceA...";
    private static final boolean DEBUG = false;

    private NotificationSpaceAdapter mNotificationSpaceAdapter;

    private boolean mAllowNotification;

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
        mAllowNotification = intent.getBooleanExtra(Intents.INTENT_ALLOW_NOTIFICATION, true);

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
        mNotificationSpaceAdapter.setSpace(mSpace);
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

        updateColor();
        mNotificationSpaceAdapter.notifyDataSetChanged();
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
            setToolBar(R.id.notification_space_activity_tool_bar);
        }
    }

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.notification_space_activity);

        setStatusBarColor();
        setToolBar(R.id.notification_space_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);

        applyInsets(R.id.notification_space_activity_content_view, R.id.notification_space_activity_tool_bar, R.id.notification_space_activity_list_view, Design.TOOLBAR_COLOR, false);

        View contentView = findViewById(R.id.notification_space_activity_content_view);
        contentView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        setTitle(getString(R.string.navigation_activity_settings));

        SettingSpaceViewHolder.Observer settingSpaceViewHolderObserver = this::onSettingsSpaceChangeValue;

        mNotificationSpaceAdapter = new NotificationSpaceAdapter(this, settingSpaceViewHolderObserver);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        RecyclerView settingsRecyclerView = findViewById(R.id.notification_space_activity_list_view);
        settingsRecyclerView.setLayoutManager(linearLayoutManager);
        settingsRecyclerView.setAdapter(mNotificationSpaceAdapter);
        settingsRecyclerView.setItemAnimator(null);
        settingsRecyclerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
    }

    private void updateSettings() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateSettings");
        }

        if (mSpace != null) {
            mAllowNotification = mSpace.getSpaceSettings().getBoolean(SpaceSettingProperty.PROPERTY_DISPLAY_NOTIFICATIONS, true);
        }
    }

    public void onSettingsSpaceChangeValue(String spaceSettingProperty, boolean value) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSettingsSpaceChangeValue = " + spaceSettingProperty + " value " + value);
        }

        if (spaceSettingProperty.equals(SpaceSettingProperty.PROPERTY_DISPLAY_NOTIFICATIONS)) {
            mAllowNotification = value;
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
            spaceSettings.setBoolean(SpaceSettingProperty.PROPERTY_DISPLAY_NOTIFICATIONS, mAllowNotification);
            mSpaceService.updateSpace(mSpace, spaceSettings, null, null);
        } else {
            Intent data = new Intent();
            data.putExtra(Intents.INTENT_ALLOW_NOTIFICATION, mAllowNotification);
            setResult(RESULT_OK, data);
            finish();
        }
    }
}
