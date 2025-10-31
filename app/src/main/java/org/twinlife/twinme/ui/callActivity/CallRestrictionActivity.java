/*
 *  Copyright (c) 2024-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.callActivity;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.skin.DisplayMode;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;

public class CallRestrictionActivity extends AbstractTwinmeActivity {

    private static final String LOG_TAG = "CallRestrictionActivity";
    private static final boolean DEBUG = false;

    private static final int DESIGN_SHADOW_COLOR = Color.argb(51, 0, 0, 0);
    private static final int DESIGN_SHADOW_OFFSET = 6;
    private static final int DESIGN_SHADOW_RADIUS = 12;
    private static final int POPUP_RADIUS = 28;

    private static final int DESIGN_CONTENT_VIEW_TOP_MARGIN = 20;
    private static final int DESIGN_CONTENT_VIEW_WIDTH = 686;
    private static final float DESIGN_LOGO_WIDTH = 134;
    private static final float DESIGN_LOGO_HEIGHT = 68;
    private static final int DESIGN_TITLE_TOP_MARGIN = 40;
    private static final float DESIGN_IMAGE_HEIGHT = 560;
    private static final float DESIGN_BUTTON_MARGIN = 60;
    private static final int DESIGN_CLOSE_HEIGHT = 52;
    private static final int DESIGN_CLOSE_TOP_MARGIN = 44;
    private static final int DESIGN_CLOSE_RIGHT_MARGIN = 44;
    private static final float DESIGN_CONTAINER_PADDING = 12;
    private static final float DESIGN_DO_NOT_SHOW_HEIGHT = 100f;
    private static int CONTAINER_PADDING;
    private static int BUTTON_MARGIN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        initViews();
    }

    @Override
    public boolean canShowInfoFloatingView() {
        if (DEBUG) {
            Log.d(LOG_TAG, "canShowInfoFloatingView");
        }

        return false;
    }

    //
    // Private methods
    //

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        setStatusBarColor(Color.BLACK, Color.BLACK);
        setContentView(R.layout.call_restriction_activity);
        setBackgroundColor(Design.OVERLAY_VIEW_COLOR);

        View containerView = findViewById(R.id.call_restriction_activity_container_view);
        containerView.setPadding(CONTAINER_PADDING, CONTAINER_PADDING, CONTAINER_PADDING, CONTAINER_PADDING);

        View contentView = findViewById(R.id.call_restriction_activity_content_view);

        ViewGroup.LayoutParams layoutParams = contentView.getLayoutParams();
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) contentView.getLayoutParams();

        float radius = POPUP_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        layoutParams.width = (int) (DESIGN_CONTENT_VIEW_WIDTH * Design.WIDTH_RATIO);
        marginLayoutParams.topMargin = (int) (DESIGN_CONTENT_VIEW_TOP_MARGIN * Design.HEIGHT_RATIO);
        ShapeDrawable popupViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        popupViewBackground.getPaint().setColor(Design.POPUP_BACKGROUND_COLOR);
        popupViewBackground.getPaint().setShadowLayer(DESIGN_SHADOW_RADIUS, 0, DESIGN_SHADOW_OFFSET, DESIGN_SHADOW_COLOR);
        contentView.setBackground(popupViewBackground);

        TextView titleTextView = findViewById(R.id.call_restriction_activity_title);
        Design.updateTextFont(titleTextView, Design.FONT_MEDIUM36);
        titleTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) titleTextView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_TITLE_TOP_MARGIN * Design.HEIGHT_RATIO);

        ImageView imageView = findViewById(R.id.call_restriction_activity_image);

        layoutParams = imageView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_IMAGE_HEIGHT * Design.HEIGHT_RATIO);

        boolean darkMode = false;
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        int displayMode = getTwinmeApplication().displayMode();
        if ((currentNightMode == Configuration.UI_MODE_NIGHT_YES && displayMode == DisplayMode.SYSTEM.ordinal())  || displayMode == DisplayMode.DARK.ordinal()) {
            darkMode = true;
        }

        imageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), darkMode ? R.drawable.quality_service_step2_dark : R.drawable.quality_service_step2, null));

        ImageView logoView = findViewById(R.id.call_restriction_activity_logo);

        layoutParams = logoView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_LOGO_WIDTH * Design.WIDTH_RATIO);
        layoutParams.height = (int) (DESIGN_LOGO_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) logoView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_TITLE_TOP_MARGIN * Design.HEIGHT_RATIO);

        TextView messageTextView = findViewById(R.id.call_restriction_activity_message_view);
        Design.updateTextFont(messageTextView, Design.FONT_REGULAR34);
        messageTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        View settingsView = findViewById(R.id.call_restriction_activity_settings_view);
        settingsView.setOnClickListener(v -> onSettingsClick());

        radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable settingsViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        settingsViewBackground.getPaint().setColor(Design.getMainStyle());
        settingsView.setBackground(settingsViewBackground);

        layoutParams = settingsView.getLayoutParams();
        layoutParams.height = Design.BUTTON_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) settingsView.getLayoutParams();
        marginLayoutParams.topMargin = BUTTON_MARGIN;

        TextView settingsTextView = findViewById(R.id.call_restriction_activity_settings_text_view);
        Design.updateTextFont(settingsTextView, Design.FONT_MEDIUM34);
        settingsTextView.setTextColor(Color.WHITE);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) settingsTextView.getLayoutParams();
        marginLayoutParams.leftMargin = Design.BUTTON_MARGIN;
        marginLayoutParams.rightMargin = Design.BUTTON_MARGIN;

        View doNotShowView = findViewById(R.id.call_restriction_activity_do_not_show_view);
        doNotShowView.setOnClickListener(v -> onDoNotShowAgainClick());

        layoutParams = doNotShowView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_DO_NOT_SHOW_HEIGHT * Design.HEIGHT_RATIO);

        TextView doNotShowTextView = findViewById(R.id.call_restriction_activity_do_not_show_title_view);
        Design.updateTextFont(doNotShowTextView, Design.FONT_MEDIUM28);
        doNotShowTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        doNotShowTextView.setPaintFlags(doNotShowTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        View closeView = findViewById(R.id.call_restriction_activity_close_view);
        closeView.setOnClickListener(view -> onCloseClick());

        layoutParams = closeView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_CLOSE_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) closeView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_CLOSE_TOP_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_CLOSE_RIGHT_MARGIN * Design.WIDTH_RATIO);
    }

    private void onCloseClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCloseClick");
        }

        finish();
    }

    private void onSettingsClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onStartClick");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            startActivity(intent);
            finish();
        }
    }

    public void onDoNotShowAgainClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDoNotShowAgainClick");
        }

        getTwinmeApplication().setShowCallRestrictionMessage(false);
        finish();
    }

    @Override
    public void setupDesign() {
        if (DEBUG) {
            Log.d(LOG_TAG, "setupDesign");
        }

        CONTAINER_PADDING = (int) (DESIGN_CONTAINER_PADDING * Design.HEIGHT_RATIO);
        BUTTON_MARGIN = (int) (DESIGN_BUTTON_MARGIN * Design.HEIGHT_RATIO);
    }
}
