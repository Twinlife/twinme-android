/*
 *  Copyright (c) 2020-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.settingsActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.percentlayout.widget.PercentRelativeLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.models.SpaceSettings;
import org.twinlife.twinme.services.SpaceSettingsService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.Settings;
import org.twinlife.twinme.skin.DisplayMode;
import org.twinlife.twinme.skin.FontSize;
import org.twinlife.twinme.ui.TwinmeApplication;
import org.twinlife.twinme.ui.spaces.MenuSelectColorView;
import org.twinlife.twinme.ui.spaces.SpaceSettingProperty;

public class PersonalizationActivity extends AbstractSettingsActivity implements SpaceSettingsService.Observer {
    private static final String LOG_TAG = "PersonalizationActivity";
    private static final boolean DEBUG = false;

    private RecyclerView mPersonalizationRecyclerView;
    private PersonalizationListAdapter mPersonalizationListAdapter;

    private boolean mUIInitialized = false;
    private boolean mUIPostInitialized = false;

    private SpaceSettingsService mSpaceSettingsService;
    private SpaceSettings mDefaultSpaceSettings;

    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        mDefaultSpaceSettings = getTwinmeContext().getDefaultSpaceSettings();

        initViews();

        mSpaceSettingsService = new SpaceSettingsService(this, getTwinmeContext(), this);
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

        mSpaceSettingsService.dispose();
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

    public DisplayMode getDisplayMode() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getDisplayMode");
        }

        return  Design.getDisplayMode(Integer.parseInt(mDefaultSpaceSettings.getString(SpaceSettingProperty.PROPERTY_DISPLAY_MODE, DisplayMode.SYSTEM.ordinal() + "")));
    }

    public int getMainColor() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getMainColor");
        }

        return Design.getDefaultColor(mDefaultSpaceSettings.getStyle());
    }

    //
    // Implement SpaceService.Observer methods
    //

    @Override
    public void onUpdateDefaultSpaceSettings(SpaceSettings spaceSettings) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateDefaultSpaceSettings: spaceSettings=" + spaceSettings);
        }

        mDefaultSpaceSettings = spaceSettings;
        Design.setupColor(PersonalizationActivity.this, getTwinmeApplication());
        updateColor();
        mPersonalizationListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onSettingClick(@NonNull UISetting<?> setting) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSettingClick");
        }
    }

    @Override
    public void onSettingClick(Settings.IntConfig intConfig) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSettingClick: intConfig= " + intConfig);
        }
    }

    @Override
    public void onRingToneClick(@NonNull UISetting<String> setting) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRingToneClick");
        }
    }

    @Override
    public void onSettingChangeValue(Settings.BooleanConfig booleanConfig, boolean value) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSettingChangeValue: booleanConfig=" + booleanConfig + " value=" + value);
        }
    }

    @Override
    public void updateColor() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateColor");
        }

        Design.setTheme(this, getTwinmeApplication());
        setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        setStatusBarColor();
        setToolBar(R.id.personalization_activity_tool_bar);
        applyInsets(R.id.personalization_activity_layout, R.id.personalization_activity_tool_bar, R.id.personalization_activity_list_view, Design.TOOLBAR_COLOR, false);

        mPersonalizationRecyclerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
    }

    public void selectedColor(String color) {
        if (DEBUG) {
            Log.d(LOG_TAG, "selectedColor color=" + color);
        }

        if (color == null) {
            color = Design.DEFAULT_COLOR;
        }

        mDefaultSpaceSettings.setStyle(color);

        if (!getTwinmeApplication().hasCurrentSpace() || getTwinmeApplication().getCurrentSpace().getSpaceSettings().getBoolean(SpaceSettingProperty.PROPERTY_DEFAULT_APPEARANCE_SETTINGS, true)) {
            Design.setMainStyle(color);
        }

        saveDefaultSpaceSettings();
        mPersonalizationListAdapter.updateColor();
        updateColor();
        mPersonalizationListAdapter.notifyDataSetChanged();
    }

    //
    // Private methods
    //

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.personalization_activity);

        setStatusBarColor();
        setToolBar(R.id.personalization_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);
        setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        setTitle(getString(R.string.application_appearance));
        applyInsets(R.id.personalization_activity_layout, R.id.personalization_activity_tool_bar, R.id.personalization_activity_list_view, Design.TOOLBAR_COLOR, false);

        PersonalizationListAdapter.OnPersonalizationClickListener onPersonalizationClickListener = new PersonalizationListAdapter.OnPersonalizationClickListener() {

            @Override
            public void onUpdateDisplayMode(DisplayMode displayMode) {

                getTwinmeApplication().updateDisplayMode(displayMode);
                mDefaultSpaceSettings.setString(SpaceSettingProperty.PROPERTY_DISPLAY_MODE, displayMode.ordinal() + "");
                saveDefaultSpaceSettings();

                Design.setupColor(PersonalizationActivity.this, getTwinmeApplication());
                updateColor();
                mPersonalizationListAdapter.updateColor();
            }

            @Override
            public void onUpdateFontSize(FontSize fontSize) {

                getTwinmeApplication().updateFontSize(fontSize);
                Design.setupFont(PersonalizationActivity.this, getTwinmeApplication());
                updateColor();
                mPersonalizationListAdapter.updateColor();
            }

            @Override
            public void onUpdateHapticFeedback(TwinmeApplication.HapticFeedbackMode hapticFeedbackMode) {

                getTwinmeApplication().updateHapticFeedbackMode(hapticFeedbackMode);

                hapticFeedback();
                mPersonalizationListAdapter.updateColor();
            }

            @Override
            public void onUpdateMainColor() {

                onColorClick();
            }

            @Override
            public void onUpdateConversationColor() {

                onConversationSettingsClick();
            }
        };

        mPersonalizationListAdapter = new PersonalizationListAdapter(this, onPersonalizationClickListener);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        mPersonalizationRecyclerView = findViewById(R.id.personalization_activity_list_view);
        mPersonalizationRecyclerView.setLayoutManager(linearLayoutManager);
        mPersonalizationRecyclerView.setAdapter(mPersonalizationListAdapter);
        mPersonalizationRecyclerView.setItemAnimator(null);
        mPersonalizationRecyclerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        mProgressBarView = findViewById(R.id.personalization_activity_progress_bar);

        mUIInitialized = true;
    }

    private void postInitViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "postInitViews");
        }

        mUIPostInitialized = true;
    }

    private void saveDefaultSpaceSettings() {
        if (DEBUG) {
            Log.d(LOG_TAG, "saveDefaultSpaceSettings");
        }

        mSpaceSettingsService.updateDefaultSpaceSettings(mDefaultSpaceSettings);
    }

    private void onConversationSettingsClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onConversationSettingsClick");
        }

        startActivity(ConversationSettingsActivity.class);
    }

    private void onColorClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onColorClick");
        }

        PercentRelativeLayout percentRelativeLayout = findViewById(R.id.personalization_activity_layout);

        MenuSelectColorView menuSelectColorView = new MenuSelectColorView(this, null);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        menuSelectColorView.setLayoutParams(layoutParams);

        MenuSelectColorView.OnMenuColorListener onMenuColorListener = new MenuSelectColorView.OnMenuColorListener() {
            @Override
            public void onSelectedColor(String color) {

                menuSelectColorView.animationCloseMenu();
                selectedColor(color);
            }

            @Override
            public void onResetColor() {

                menuSelectColorView.animationCloseMenu();
                selectedColor(null);
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

        menuSelectColorView.openMenu(getString(R.string.space_appearance_activity_theme), Design.getMainStyleString(), Design.DEFAULT_COLOR);

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }
}
