/*
 *  Copyright (c) 2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.profiles;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.ColorUtils;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.skin.DisplayMode;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.Settings;

public class OnboardingProfileActivity extends AbstractTwinmeActivity {

    private static final String LOG_TAG = "OnboardingProfileA...";
    private static final boolean DEBUG = false;

    private static final int DESIGN_SHADOW_COLOR = Color.argb(51, 0, 0, 0);
    private static final int DESIGN_SHADOW_OFFSET = 6;
    private static final int DESIGN_SHADOW_RADIUS = 12;
    private static final int POPUP_RADIUS = 28;

    private static final int DESIGN_CONTENT_VIEW_TOP_MARGIN = 20;
    private static final int DESIGN_CONTENT_VIEW_BOTTOM_MARGIN = 80;
    private static final int DESIGN_CONTENT_VIEW_WIDTH = 686;
    private static final int DESIGN_TITLE_TOP_MARGIN = 100;
    private static final float DESIGN_IMAGE_HEIGHT = 340;
    private static final float DESIGN_IMAGE_MARGIN = 50;
    private static final int DESIGN_CONTENT_HEIGHT = 738;
    private static final float DESIGN_MORE_TEXT_VIEW_HEIGHT = 80;
    private static final float DESIGN_MORE_TEXT_VIEW_MARGIN = 20;
    private static final float DESIGN_CREATE_BUTTON_BOTTOM_MARGIN = 40;
    private static final int DESIGN_CLOSE_HEIGHT = 52;
    private static final int DESIGN_CLOSE_TOP_MARGIN = 24;
    private static final int DESIGN_CLOSE_RIGHT_MARGIN = 12;
    private static final int DESIGN_FULLSCREEN_CLOSE_TOP_MARGIN = 50;
    private static final int DESIGN_FULLSCREEN_CLOSE_RIGHT_MARGIN = 44;
    private static final float DESIGN_CONTAINER_PADDING = 12;
    private static int CONTAINER_PADDING;
    private static int IMAGE_MARGIN;
    private static int MORE_TEXT_VIEW_MARGIN;
    private static int CREATE_BUTTON_BOTTOM_MARGIN;

    private TextView mMessageTextView;
    private View mShowMoreTextView;

    private boolean mFromSideMenu = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        mFromSideMenu = getIntent().getBooleanExtra(Intents.INTENT_FROM_SIDE_MENU, false);

        initViews();
    }

    //
    // Private methods
    //

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        if (!mFromSideMenu) {
            int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.AVATAR_PLACEHOLDER_COLOR);
            setStatusBarColor(color,  ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.WHITE_COLOR));
            setContentView(R.layout.onboarding_profile_activity);
            setBackgroundColor(Design.OVERLAY_VIEW_COLOR);
        } else {
            Design.setTheme(this, getTwinmeApplication());
            setContentView(R.layout.onboarding_profile_activity_full_screen);
            setStatusBarColor(Design.WHITE_COLOR);
        }

        View containerView = findViewById(R.id.onboarding_profile_activity_container_view);
        containerView.setPadding(CONTAINER_PADDING, CONTAINER_PADDING, CONTAINER_PADDING, CONTAINER_PADDING);

        View contentView = findViewById(R.id.onboarding_profile_activity_content_view);

        ViewGroup.LayoutParams layoutParams = contentView.getLayoutParams();
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) contentView.getLayoutParams();

        float radius = POPUP_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        if (!mFromSideMenu) {
            layoutParams.width = (int) (DESIGN_CONTENT_VIEW_WIDTH * Design.WIDTH_RATIO);
            marginLayoutParams.topMargin = (int) (DESIGN_CONTENT_VIEW_TOP_MARGIN * Design.HEIGHT_RATIO);
            ShapeDrawable popupViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
            popupViewBackground.getPaint().setColor(Design.POPUP_BACKGROUND_COLOR);
            popupViewBackground.getPaint().setShadowLayer(DESIGN_SHADOW_RADIUS, 0, DESIGN_SHADOW_OFFSET, DESIGN_SHADOW_COLOR);
            contentView.setBackground(popupViewBackground);
        }

        TextView titleTextView = findViewById(R.id.onboarding_profile_activity_title);
        Design.updateTextFont(titleTextView, Design.FONT_MEDIUM36);
        titleTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        if (mFromSideMenu) {
            marginLayoutParams = (ViewGroup.MarginLayoutParams) titleTextView.getLayoutParams();
            marginLayoutParams.topMargin = (int) (DESIGN_TITLE_TOP_MARGIN * Design.HEIGHT_RATIO);
        }

        ImageView imageView = findViewById(R.id.onboarding_profile_activity_image);

        layoutParams = imageView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_IMAGE_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) imageView.getLayoutParams();
        marginLayoutParams.topMargin = IMAGE_MARGIN;
        marginLayoutParams.bottomMargin = IMAGE_MARGIN;

        boolean darkMode = false;
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        int displayMode = Settings.displayMode.getInt();
        if ((currentNightMode == Configuration.UI_MODE_NIGHT_YES && displayMode == DisplayMode.SYSTEM.ordinal())  || displayMode == DisplayMode.DARK.ordinal()) {
            darkMode = true;
        }

        imageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), darkMode ? R.drawable.onboarding_add_profile_dark : R.drawable.onboarding_add_profile, null));

        mMessageTextView = findViewById(R.id.onboarding_profile_activity_message_view);
        Design.updateTextFont(mMessageTextView, Design.FONT_REGULAR34);mMessageTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mMessageTextView.setMovementMethod(new ScrollingMovementMethod());

        float messageMaxHeight = Design.DISPLAY_HEIGHT - ((DESIGN_CONTENT_VIEW_TOP_MARGIN + DESIGN_CONTENT_VIEW_BOTTOM_MARGIN + DESIGN_CONTENT_HEIGHT) * Design.HEIGHT_RATIO);
        mMessageTextView.setMaxHeight((int) messageMaxHeight);

        mShowMoreTextView = findViewById(R.id.onboarding_profile_activity_more_text_view);
        mShowMoreTextView.setVisibility(View.GONE);

        String message = getString(R.string.create_profile_activity_onboarding_message_part_1) +
                "\n\n" +
                getString(R.string.create_profile_activity_onboarding_message_part_2) +
                "\n\n" +
                getString(R.string.create_profile_activity_onboarding_message_part_3) +
                "\n\n" +
                getString(R.string.create_profile_activity_onboarding_message_part_4);

        mMessageTextView.setText(message);

        layoutParams = mShowMoreTextView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_MORE_TEXT_VIEW_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mShowMoreTextView.getLayoutParams();
        marginLayoutParams.topMargin = MORE_TEXT_VIEW_MARGIN;

        mShowMoreTextView.setOnClickListener(v -> onShowMoreTextClick());

        ImageView showMoreTextImageView = findViewById(R.id.onboarding_profile_activity_more_text_image);
        showMoreTextImageView.setColorFilter(Design.BLACK_COLOR);

        View createProfileView = findViewById(R.id.onboarding_profile_activity_create_profile_view);
        createProfileView.setOnClickListener(v -> onCreateProfileClick());

        if (mFromSideMenu) {
            createProfileView.setVisibility(View.GONE);
        }

        radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable createViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        createViewBackground.getPaint().setColor(Design.getMainStyle());
        createProfileView.setBackground(createViewBackground);

        layoutParams = createProfileView.getLayoutParams();
        layoutParams.height = Design.BUTTON_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) createProfileView.getLayoutParams();
        marginLayoutParams.topMargin = MORE_TEXT_VIEW_MARGIN;
        marginLayoutParams.bottomMargin = CREATE_BUTTON_BOTTOM_MARGIN;

        TextView createProfileTextView = findViewById(R.id.onboarding_profile_activity_create_space_text_view);
        Design.updateTextFont(createProfileTextView, Design.FONT_MEDIUM34);
        createProfileTextView.setTextColor(Color.WHITE);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) createProfileTextView.getLayoutParams();
        marginLayoutParams.leftMargin = Design.BUTTON_MARGIN;
        marginLayoutParams.rightMargin = Design.BUTTON_MARGIN;

        View closeView = findViewById(R.id.onboarding_profile_activity_close_view);
        closeView.setOnClickListener(view -> finish());

        layoutParams = closeView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_CLOSE_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) closeView.getLayoutParams();
        if (mFromSideMenu) {
            marginLayoutParams.topMargin = (int) (DESIGN_FULLSCREEN_CLOSE_TOP_MARGIN * Design.HEIGHT_RATIO);
            marginLayoutParams.rightMargin = (int) (DESIGN_FULLSCREEN_CLOSE_RIGHT_MARGIN * Design.WIDTH_RATIO);
        } else {
            marginLayoutParams.topMargin = (int) (DESIGN_CLOSE_TOP_MARGIN * Design.HEIGHT_RATIO);
            marginLayoutParams.rightMargin = (int) (DESIGN_CLOSE_RIGHT_MARGIN * Design.WIDTH_RATIO);
        }
    }

    private void onCreateProfileClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateProfileClick");
        }

        finish();
    }

    private void onShowMoreTextClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onShowMoreTextClick");
        }

        mShowMoreTextView.setVisibility(View.GONE);

        String message = getString(R.string.create_profile_activity_onboarding_message_part_1) +
                "\n\n" +
                getString(R.string.create_profile_activity_onboarding_message_part_2) +
                "\n\n" +
                getString(R.string.create_profile_activity_onboarding_message_part_3) +
                "\n\n" +
                getString(R.string.create_profile_activity_onboarding_message_part_4);

        mMessageTextView.setText(message);
    }

    @Override
    public void setupDesign() {
        if (DEBUG) {
            Log.d(LOG_TAG, "setupDesign");
        }

        CONTAINER_PADDING = (int) (DESIGN_CONTAINER_PADDING * Design.HEIGHT_RATIO);
        IMAGE_MARGIN = (int) (DESIGN_IMAGE_MARGIN * Design.HEIGHT_RATIO);
        MORE_TEXT_VIEW_MARGIN = (int) (DESIGN_MORE_TEXT_VIEW_MARGIN * Design.HEIGHT_RATIO);
        CREATE_BUTTON_BOTTOM_MARGIN = (int) (DESIGN_CREATE_BUTTON_BOTTOM_MARGIN * Design.HEIGHT_RATIO);
    }
}
