/*
 *  Copyright (c) 2020-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.settingsActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.skin.DisplayMode;
import org.twinlife.twinme.skin.FontSize;
import org.twinlife.twinme.ui.TwinmeApplication;
import org.twinlife.twinme.ui.premiumServicesActivity.PremiumFeatureConfirmView;
import org.twinlife.twinme.ui.premiumServicesActivity.UIPremiumFeature;
import org.twinlife.twinme.utils.AbstractConfirmView;

public class PersonalizationActivity extends AbstractSettingsActivity {
    private static final String LOG_TAG = "PersonalizationActivity";
    private static final boolean DEBUG = false;

    private RecyclerView mPersonalizationRecyclerView;
    private PersonalizationListAdapter mPersonalizationListAdapter;

    private boolean mUIInitialized = false;
    private boolean mUIPostInitialized = false;

    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        initViews();
    }

    //
    // Override Activity methods
    //

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
    public void onSettingClick(@NonNull UISetting<?> setting) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSettingClick");
        }
    }

    @Override
    public void onRingToneClick(@NonNull UISetting<String> setting) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRingToneClick");
        }
    }

    @Override
    public void updateColor() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateColor");
        }

        Design.setupColor(this, getTwinmeApplication());
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

        Design.setMainStyle(color);

        mPersonalizationListAdapter.updateColor();
        updateColor();
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
            public void onUpdateConversationColor() {

                onConversationSettingsClick();
            }

            @Override
            public void onUpdateMainColor() {

                onColorClick();
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

        ViewGroup viewGroup = findViewById(R.id.personalization_activity_layout);

        MenuSelectColorView menuSelectColorView = new MenuSelectColorView(this, null);
        MenuSelectColorView.OnMenuColorListener onMenuColorListener = new MenuSelectColorView.OnMenuColorListener() {
            @Override
            public void onSelectedColor(String color) {

                menuSelectColorView.animationCloseMenu();
                selectedColor(color);
            }

            @Override
            public void onCustomColor() {
                viewGroup.removeView(menuSelectColorView);
                onPremiumFeatureClick();
            }

            @Override
            public void onResetColor() {

                menuSelectColorView.animationCloseMenu();
                selectedColor(null);
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

        menuSelectColorView.openMenu(getString(R.string.space_appearance_activity_theme), Design.getMainStyleString(), Design.DEFAULT_COLOR);

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }

    private void onPremiumFeatureClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPremiumFeatureClick");
        }

        ViewGroup viewGroup = findViewById(R.id.personalization_activity_layout);

        PremiumFeatureConfirmView premiumFeatureConfirmView = new PremiumFeatureConfirmView(this, null);
        premiumFeatureConfirmView.initWithPremiumFeature(new UIPremiumFeature(this, UIPremiumFeature.FeatureType.SPACES));

        AbstractConfirmView.Observer observer = new AbstractConfirmView.Observer() {
            @Override
            public void onConfirmClick() {
                premiumFeatureConfirmView.redirectStore();
            }

            @Override
            public void onCancelClick() {
                premiumFeatureConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onDismissClick() {
                premiumFeatureConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                viewGroup.removeView(premiumFeatureConfirmView);
                setStatusBarColor();
                onColorClick();
            }
        };
        premiumFeatureConfirmView.setObserver(observer);
        viewGroup.addView(premiumFeatureConfirmView);
        premiumFeatureConfirmView.show();

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }
}
