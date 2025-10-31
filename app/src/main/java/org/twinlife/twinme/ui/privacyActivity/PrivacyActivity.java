/*
 *  Copyright (c) 2021-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.privacyActivity;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.graphics.ColorUtils;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.settingsActivity.MenuSelectValueView;
import org.twinlife.twinme.utils.SwitchView;

public class PrivacyActivity extends AbstractTwinmeActivity implements MenuSelectValueView.Observer {
    private static final String LOG_TAG = "PrivacyActivity";
    private static final boolean DEBUG = false;

    private static final float DESIGN_TEXT_INFORMATION_TOP_MARGIN = 17f;
    private static final float DESIGN_SECTION_TOP_MARGIN = 80f;
    private static int SECTION_VIEW_HEIGHT;
    private static int TEXT_INFORMATION_TOP_MARGIN;
    private static int SECTION_TOP_MARGIN;

    private SwitchView mScreenLockSwitchView;
    private SwitchView mHideLastScreenSwitchView;
    private View mTimeoutScreenLockView;
    private TextView mTimeoutScreenTextView;

    private MenuSelectValueView mMenuTimeoutView;
    private View mOverlayView;

    private boolean mUIInitialized = false;

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


    //implements MenuSelectValueView.Observer

    @Override
    public void onCloseMenuAnimationEnd() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCloseMenuAnimationEnd");
        }

        closeMenu();
    }

    @Override
    public void onSelectValue(int value) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSelectValue: " + value);
        }

    }

    @Override
    public void onSelectTimeout(UITimeout timeout) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSelectTimeout: " + timeout);
        }

        mTimeoutScreenTextView.setText(timeout.getText());
        getTwinmeApplication().updateScreenLockTimeout(timeout.getDelay());
        closeMenu();
    }

    //
    // Private methods
    //

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.privacy_activity);

        View contentView = findViewById(R.id.privacy_activity_content_view);
        contentView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        setStatusBarColor();
        setToolBar(R.id.privacy_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);

        setTitle(getString(R.string.privacy_activity_title));
        applyInsets(R.id.privacy_activity_content_view, R.id.privacy_activity_tool_bar,R.id.privacy_activity_screen_container_view, Design.TOOLBAR_COLOR, false);

        View containerView = findViewById(R.id.privacy_activity_screen_container_view);
        containerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        View screenLockView = findViewById(R.id.privacy_activity_screen_lock_view);
        ViewGroup.LayoutParams sectionLayoutParams = screenLockView.getLayoutParams();
        sectionLayoutParams.height = SECTION_VIEW_HEIGHT;
        screenLockView.setLayoutParams(sectionLayoutParams);

        mScreenLockSwitchView = findViewById(R.id.privacy_activity_screen_lock_checkbox);
        mScreenLockSwitchView.setOnCheckedChangeListener((v, isChecked) -> setUpdated());
        Design.updateTextFont(mScreenLockSwitchView, Design.FONT_REGULAR34);
        mScreenLockSwitchView.setChecked(getTwinmeApplication().screenLocked());
        mScreenLockSwitchView.setTextColor(Design.FONT_COLOR_DEFAULT);

        TextView screenLockInformationView = findViewById(R.id.privacy_activity_screen_lock_information_text_view);
        Design.updateTextFont(screenLockInformationView, Design.FONT_REGULAR28);
        screenLockInformationView.setTextColor(Design.FONT_COLOR_GREY);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) screenLockInformationView.getLayoutParams();
        marginLayoutParams.topMargin = TEXT_INFORMATION_TOP_MARGIN;
        marginLayoutParams.bottomMargin = SECTION_TOP_MARGIN;

        if (!isDeviceSecure()) {
            mScreenLockSwitchView.setEnabled(false);
            mScreenLockSwitchView.setAlpha(0.5f);
            screenLockInformationView.setAlpha(0.5f);

            View unavailableScreenLockView = findViewById(R.id.privacy_activity_screen_lock_unavailable);
            unavailableScreenLockView.setVisibility(View.VISIBLE);
            unavailableScreenLockView.setOnClickListener(view -> {
                showAlertMessageView(R.id.privacy_activity_content_view, getString(R.string.deleted_account_activity_warning), getString(R.string.lock_screen_activity_passcode_not_set), false, null);
            });
        }

        mTimeoutScreenLockView = findViewById(R.id.privacy_activity_timeout_screen_lock_view);
        sectionLayoutParams = mTimeoutScreenLockView.getLayoutParams();
        sectionLayoutParams.height = SECTION_VIEW_HEIGHT;
        mTimeoutScreenLockView.setLayoutParams(sectionLayoutParams);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mTimeoutScreenLockView.getLayoutParams();
        marginLayoutParams.bottomMargin = SECTION_TOP_MARGIN;

        mTimeoutScreenLockView.setOnClickListener(view -> onScreenLockTimeoutClick());

        TextView timeoutScreenLockTitleView = findViewById(R.id.privacy_activity_timeout_screen_lock_title_view);
        timeoutScreenLockTitleView.setTypeface(Design.FONT_REGULAR34.typeface);
        timeoutScreenLockTitleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_REGULAR34.size);
        timeoutScreenLockTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mTimeoutScreenTextView = findViewById(R.id.privacy_activity_timeout_screen_lock_value_view);
        mTimeoutScreenTextView.setTypeface(Design.FONT_REGULAR34.typeface);
        mTimeoutScreenTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_REGULAR34.size);
        mTimeoutScreenTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        View hideLastScreenView = findViewById(R.id.privacy_activity_hide_last_screen_view);
        sectionLayoutParams = hideLastScreenView.getLayoutParams();
        sectionLayoutParams.height = SECTION_VIEW_HEIGHT;
        hideLastScreenView.setLayoutParams(sectionLayoutParams);

        mHideLastScreenSwitchView = findViewById(R.id.privacy_activity_hide_last_screen_checkbox);
        mHideLastScreenSwitchView.setOnCheckedChangeListener((v, isChecked) -> setUpdated());
        Design.updateTextFont(mHideLastScreenSwitchView, Design.FONT_REGULAR34);
        mHideLastScreenSwitchView.setChecked(getTwinmeApplication().lastScreenHidden());
        mHideLastScreenSwitchView.setTextColor(Design.FONT_COLOR_DEFAULT);

        TextView hideLastScreenInformationView = findViewById(R.id.privacy_activity_hide_last_screen_information_text_view);
        Design.updateTextFont(hideLastScreenInformationView, Design.FONT_REGULAR28);
        hideLastScreenInformationView.setTextColor(Design.FONT_COLOR_GREY);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) hideLastScreenInformationView.getLayoutParams();
        marginLayoutParams.topMargin = TEXT_INFORMATION_TOP_MARGIN;

        if (getTwinmeApplication().screenLocked()) {
            mTimeoutScreenLockView.setVisibility(View.VISIBLE);
        } else {
            mTimeoutScreenLockView.setVisibility(View.GONE);
        }

        mTimeoutScreenTextView.setText(UITimeout.getDelay(this, getTwinmeApplication().screenLockTimeout()));

        mOverlayView = findViewById(R.id.privacy_activity_overlay_view);
        mOverlayView.setBackgroundColor(Design.OVERLAY_VIEW_COLOR);
        mOverlayView.setOnClickListener(view -> closeMenu());

        mMenuTimeoutView = findViewById(R.id.privacy_activity__menu_select_value_view);
        mMenuTimeoutView.setVisibility(View.INVISIBLE);
        mMenuTimeoutView.setObserver(this);
        mMenuTimeoutView.setActivity(this);

        mUIInitialized = true;
    }

    private void setUpdated() {
        if (DEBUG) {
            Log.d(LOG_TAG, "setUpdated");
        }

        if (!mUIInitialized) {
            return;
        }

        getTwinmeApplication().setScreenLocked(mScreenLockSwitchView.isChecked());
        getTwinmeApplication().setLastScreenHidden(mHideLastScreenSwitchView.isChecked());

        if (mScreenLockSwitchView.isChecked()) {
            mTimeoutScreenLockView.setVisibility(View.VISIBLE);
        } else {
            mTimeoutScreenLockView.setVisibility(View.GONE);
        }
    }

    private void onScreenLockTimeoutClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onScreenLockTimeoutClick");
        }

        openMenu();
    }

    private void openMenu() {
        if (DEBUG) {
            Log.d(LOG_TAG, "openMenu");
        }

        if (mMenuTimeoutView.getVisibility() == View.INVISIBLE) {
            mMenuTimeoutView.setVisibility(View.VISIBLE);
            mOverlayView.setVisibility(View.VISIBLE);
            mMenuTimeoutView.openMenu(MenuSelectValueView.MenuType.LOCKSCREEN);
            int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
            setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
        }
    }

    private boolean isDeviceSecure() {
        if (DEBUG) {
            Log.d(LOG_TAG, "isDeviceSecure");
        }

        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if (keyguardManager != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                return keyguardManager.isDeviceSecure();
            }
            return keyguardManager.isKeyguardSecure();
        }

        return false;
    }

    private void closeMenu() {
        if (DEBUG) {
            Log.d(LOG_TAG, "closeMenu");
        }

        mMenuTimeoutView.setVisibility(View.INVISIBLE);
        mOverlayView.setVisibility(View.INVISIBLE);
        setStatusBarColor();
    }

    @Override
    public void setupDesign() {
        if (DEBUG) {
            Log.d(LOG_TAG, "setupDesign");
        }

        SECTION_VIEW_HEIGHT = Design.SECTION_HEIGHT;
        TEXT_INFORMATION_TOP_MARGIN = (int) (DESIGN_TEXT_INFORMATION_TOP_MARGIN * Design.HEIGHT_RATIO);
        SECTION_TOP_MARGIN = (int) (DESIGN_SECTION_TOP_MARGIN * Design.HEIGHT_RATIO);
    }
}
