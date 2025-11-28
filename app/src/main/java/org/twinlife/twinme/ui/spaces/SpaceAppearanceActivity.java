/*
 *  Copyright (c) 2020-2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui.spaces;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

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
import org.twinlife.twinme.skin.DisplayMode;
import org.twinlife.twinme.ui.Intents;

import java.util.UUID;

public class SpaceAppearanceActivity extends AbstractSpaceActivity {
    private static final String LOG_TAG = "SpaceAppearanceActivity";
    private static final boolean DEBUG = false;

    private static final int REQUEST_COLOR_CODE = 1;

    private SpaceAppearanceAdapter mSpaceAppearanceAdapter;

    @Nullable
    private Space mSpace;
    @Nullable
    private CustomAppearance mCustomAppearance;
    private DisplayMode mDisplayMode;
    private SpaceService mSpaceService;

    private View mContentView;

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
        mDisplayMode = Design.getDisplayMode(getTwinmeApplication().displayMode());

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

                if (mCustomAppearance != null && mSpace != null) {
                    if (getTwinmeApplication().isCurrentSpace(mSpace.getId())) {
                        getTwinmeApplication().updateDisplayMode(Design.getDisplayMode(Integer.parseInt(mCustomAppearance.getSpaceSettings().getString(SpaceSettingProperty.PROPERTY_DISPLAY_MODE, DisplayMode.SYSTEM.ordinal() + ""))));
                        Design.setMainStyle(color);
                    }

                    mCustomAppearance.setMainColor(color);
                    mSpaceService.updateSpace(mSpace, mCustomAppearance.getSpaceSettings(), null, null);
                }

                updateColor();
            }
        }
    }

    public DisplayMode getDisplayMode() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getDisplayMode");
        }

        return mDisplayMode;
    }

    public int getMainColor() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getMainColor");
        }

        if (mCustomAppearance == null) {
            return Color.parseColor(Design.DEFAULT_COLOR);
        } else {
            return mCustomAppearance.getMainColor();
        }
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
        mCustomAppearance = new CustomAppearance(this, mSpace.getSpaceSettings());
        mSpaceAppearanceAdapter.setSpace(mSpace);
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

        if (mSpace == null || space.getId().equals(mSpace.getId())) {
            mSpace = space;
        }

        if (getTwinmeApplication().isCurrentSpace(mSpace.getId())) {
            getTwinmeApplication().updateDisplayMode(mDisplayMode);
        }

        updateColor();
        mSpaceAppearanceAdapter.notifyDataSetChanged();
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
            setToolBar(R.id.space_appearance_activity_tool_bar);
            mContentView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        }
    }

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.space_appearance_activity);

        setStatusBarColor();
        setToolBar(R.id.space_appearance_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);

        setTitle(getString(R.string.navigation_activity_settings));

        applyInsets(R.id.space_appearance_activity_content_view, R.id.space_appearance_activity_tool_bar, R.id.space_appearance_activity_list_view, Design.TOOLBAR_COLOR, false);

        mContentView = findViewById(R.id.space_appearance_activity_content_view);
        mContentView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        SpaceAppearanceAdapter.OnSpaceAppearanceClickListener onSpaceAppearanceClickListener = new SpaceAppearanceAdapter.OnSpaceAppearanceClickListener() {

            @Override
            public void onUpdateDisplayMode(DisplayMode displayMode) {
                mDisplayMode = displayMode;
                if (mCustomAppearance != null) {
                    mCustomAppearance.setCurrentMode(getApplicationContext(), mDisplayMode);
                }

                if (mSpace != null && getTwinmeApplication().isCurrentSpace(mSpace.getId())) {
                    getTwinmeApplication().updateDisplayMode(mDisplayMode);
                }

                saveSpaceSettings();
            }

            @Override
            public void onConversationAppearanceClick() {
                onConversationApppearanceClick();
            }

            @Override
            public void onUpdateMainColorClick() {

                if (mCustomAppearance != null) {
                    openMenuColor(mCustomAppearance.getMainColor(), Design.getMainStyle());
                }
            }
        };

        mSpaceAppearanceAdapter = new SpaceAppearanceAdapter(this, onSpaceAppearanceClickListener);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        RecyclerView settingsRecyclerView = findViewById(R.id.space_appearance_activity_list_view);
        settingsRecyclerView.setLayoutManager(linearLayoutManager);
        settingsRecyclerView.setAdapter(mSpaceAppearanceAdapter);
        settingsRecyclerView.setItemAnimator(null);
        settingsRecyclerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
    }

    private void updateSettings() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateSettings");
        }

        if (mSpace != null) {
            mDisplayMode = Design.getDisplayMode(Integer.parseInt(mSpace.getSpaceSettings().getString(SpaceSettingProperty.PROPERTY_DISPLAY_MODE, getTwinmeApplication().displayMode() + "")));
        }
    }

    private void saveSpaceSettings() {
        if (DEBUG) {
            Log.d(LOG_TAG, "saveSpaceSettings");
        }

        if (mSpace != null) {
            SpaceSettings spaceSettings = mSpace.getSpaceSettings();
            spaceSettings.setStyle(mSpace.getStyle());
            spaceSettings.setString(SpaceSettingProperty.PROPERTY_DISPLAY_MODE, mDisplayMode.ordinal() + "");
            mSpaceService.updateSpace(mSpace, spaceSettings, null, null);
        } else {
            Intent data = new Intent();
            setResult(RESULT_OK, data);
            finish();
        }
    }

    private void openMenuColor(int color, int defaultColor) {
        if (DEBUG) {
            Log.d(LOG_TAG, "openMenuColor");
        }

        ViewGroup viewGroup = findViewById(R.id.space_appearance_activity_content_view);

        MenuSelectColorView menuSelectColorView = new MenuSelectColorView(this, null);
        MenuSelectColorView.OnMenuColorListener onMenuColorListener = new MenuSelectColorView.OnMenuColorListener() {
            @Override
            public void onSelectedColor(String color) {

                menuSelectColorView.animationCloseMenu();

                if (mSpace != null && getTwinmeApplication().isCurrentSpace(mSpace.getId())) {
                    getTwinmeApplication().updateDisplayMode(mDisplayMode);
                    Design.setMainStyle(color);
                }

                if (mCustomAppearance != null) {
                    mCustomAppearance.setMainColor(color);
                    mSpaceService.updateSpace(mSpace, mCustomAppearance.getSpaceSettings(), null, null);
                }

                updateColor();
            }

            @Override
            public void onResetColor() {

                onSelectedColor(null);
            }

            @Override
            public void onCloseMenu() {
                viewGroup.removeView(menuSelectColorView);
                setStatusBarColor();
            }
        };

        menuSelectColorView.setOnMenuColorListener(onMenuColorListener);
        menuSelectColorView.setAppearanceActivity(this);
        viewGroup.addView(menuSelectColorView);

        String hexColor = "#" + Integer.toHexString(color);
        String hexDefaultColor = "#" + Integer.toHexString(defaultColor);
        menuSelectColorView.openMenu(getString(R.string.space_appearance_activity_theme), hexColor, hexDefaultColor);

        int statusBarColor = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(statusBarColor, Design.POPUP_BACKGROUND_COLOR);
    }

    private void onConversationApppearanceClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onConversationApppearanceClick");
        }

        if (mSpace != null) {
            Intent intent = new Intent();
            intent.putExtra(Intents.INTENT_SPACE_ID, mSpace.getId().toString());
            intent.putExtra(Intents.INTENT_ONLY_CONVERSATION, true);
            startActivity(ConversationAppearanceActivity.class, intent);
        }
    }
}