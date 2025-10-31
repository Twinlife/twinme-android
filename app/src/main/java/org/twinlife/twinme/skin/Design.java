/*
 *  Copyright (c) 2015-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Denis Campredon (Denis.Campredon@twinlife-systems.com)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.skin;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.ui.Settings;
import org.twinlife.twinme.ui.TwinmeApplication;
import org.twinlife.twinme.ui.spaces.UIColorSpace;
import org.twinlife.twinme.utils.CommonUtils;
import org.twinlife.twinme.utils.SwitchView;

import java.util.ArrayList;
import java.util.List;

public class Design {

    private static final float REFERENCE_HEIGHT = 1334f;
    private static final float REFERENCE_WIDTH = 750f;

    public static int DISPLAY_HEIGHT = -1;
    public static int DISPLAY_WIDTH = -1;

    private static int PREVIOUS_DISPLAY_HEIGHT = -1;
    private static int PREVIOUS_DISPLAY_WIDTH = -1;

    public static float MIN_RATIO = 0f;
    public static float HEIGHT_RATIO = 0f;
    public static float WIDTH_RATIO = 0f;
    public static final int ITEM_LIST_CACHE_SIZE = 32;

    public static final long ANIMATION_VIEW_DURATION = 300;

    //
    // Colors
    //

    public static final int BACKGROUND_COLOR_DEFAULT = Color.rgb(250, 252, 255);

    public static final int BACKGROUND_COLOR_RED = Color.rgb(255, 77, 85);

    public static final int BACKGROUND_COLOR_GREY = Color.rgb(239, 239, 239);

    public static final int DELETE_COLOR_RED = Color.rgb(253, 96, 93);
    public static final int EDIT_COLOR = Color.rgb(35, 42, 69);

    public static final int FONT_COLOR_RED = Color.rgb(255, 77, 85);
    public static final int FONT_COLOR_GREY = Color.rgb(178, 178, 178);
    public static final int FONT_COLOR_DESCRIPTION = Color.rgb(142, 142, 142);

    public static final int BLUE_NORMAL = Color.rgb(0, 174, 255);
    public static final int OVERLAY_VIEW_COLOR = Color.argb(127, 0, 0, 0);

    public static final int ITEM_BACKGROUND_COLOR = Color.rgb(120, 137, 159);
    public static final int ITEM_FONT_COLOR = Color.rgb(44, 44, 44);
    public static final int BACKGROUND_SPACE_AVATAR = Color.rgb(239, 239, 239);
    public static final int BOTTOM_GRADIENT_START_COLOR = Color.argb(0, 0, 0, 0);
    public static final int BOTTOM_GRADIENT_END_COLOR = Color.argb(255, 0, 0, 0);

    public static final int AUDIO_TRACK_COLOR = Color.rgb(51, 51, 51);
    public static final int PLACEHOLDER_COLOR = Color.rgb(162, 162, 162);
    public static final int CHAT_COLOR = Color.rgb(78, 229, 184);
    public static final int AUDIO_CALL_COLOR = Color.rgb(0, 174, 244);
    public static final int VIDEO_CALL_COLOR = Color.rgb(247, 114, 114);
    public static final int AVATAR_PLACEHOLDER_COLOR = Color.rgb(242, 243, 245);
    public static final int ZOOM_COLOR = Color.rgb(255, 161, 0);

    public static final int BACK_VIEW_COLOR = Color.argb(76, 0, 0, 0);

    public static final String MAIN_COLOR_PREFERENCE = "MAIN_COLOR";
    public static final String DEFAULT_COLOR = "#FB1C5B";

    public static GradientDescriptor GRADIENT_COLORS_GREEN;
    public static GradientDescriptor GRADIENT_COLORS_RED;
    public static CircularShadowDescriptor LIGHT_CIRCULAR_SHADOW_DESCRIPTOR;

    @SuppressWarnings({"WeakerAccess", "unused"})
    public static CircularShadowDescriptor DARK_CIRCULAR_SHADOW_DESCRIPTOR;

    //
    // Fonts
    //

    public static TextStyle FONT_REGULAR16;
    public static TextStyle FONT_REGULAR20;
    public static TextStyle FONT_REGULAR22;
    public static TextStyle FONT_REGULAR24;
    public static TextStyle FONT_REGULAR26;
    public static TextStyle FONT_REGULAR28;
    public static TextStyle FONT_REGULAR30;
    public static TextStyle FONT_REGULAR32;
    public static TextStyle FONT_REGULAR34;
    public static TextStyle FONT_REGULAR36;
    public static TextStyle FONT_REGULAR40;
    public static TextStyle FONT_REGULAR44;
    public static TextStyle FONT_REGULAR50;
    public static TextStyle FONT_REGULAR64;
    public static TextStyle FONT_REGULAR88;
    public static TextStyle FONT_MEDIUM16;
    public static TextStyle FONT_MEDIUM20;
    public static TextStyle FONT_MEDIUM24;
    public static TextStyle FONT_MEDIUM26;
    public static TextStyle FONT_MEDIUM28;
    public static TextStyle FONT_MEDIUM30;
    public static TextStyle FONT_MEDIUM32;
    public static TextStyle FONT_MEDIUM34;
    public static TextStyle FONT_MEDIUM36;
    public static TextStyle FONT_MEDIUM38;
    public static TextStyle FONT_MEDIUM40;
    public static TextStyle FONT_MEDIUM42;
    public static TextStyle FONT_MEDIUM44;
    public static TextStyle FONT_MEDIUM54;
    public static TextStyle FONT_ITALIC_28;
    public static TextStyle FONT_BOLD28;
    public static TextStyle FONT_BOLD26;
    public static TextStyle FONT_BOLD32;
    public static TextStyle FONT_BOLD34;
    public static TextStyle FONT_BOLD36;
    public static TextStyle FONT_BOLD44;
    public static TextStyle FONT_BOLD54;
    public static TextStyle FONT_BOLD68;
    public static TextStyle FONT_BOLD88;

    public static TextStyle FONT_EMOJI_EXTRA_EXTRA_LARGE;
    public static TextStyle FONT_EMOJI_EXTRA_LARGE;
    public static TextStyle FONT_EMOJI_LARGE;
    public static TextStyle FONT_EMOJI_MEDIUM;
    public static TextStyle FONT_EMOJI_SMALL;

    public static int TOOLBAR_COLOR;
    public static int LIGHT_GREY_BACKGROUND_COLOR;
    public static int GREY_BACKGROUND_COLOR;
    public static int LIGHT_BORDER_COLOR;
    public static int WHITE_COLOR;
    public static int BLACK_COLOR;
    public static int GREY_COLOR;
    public static int FONT_COLOR_DEFAULT;
    public static int BACKGROUND_COLOR_WHITE_OPACITY85;
    public static int BACKGROUND_COLOR_WHITE_OPACITY36;
    public static int CONVERSATION_BACKGROUND_COLOR;
    public static int CONVERSATION_OVERLAY_COLOR;

    public static int MENU_REACTION_BACKGROUND_COLOR;
    public static int EDIT_TEXT_CONVERSATION_BACKGROUND_COLOR;
    public static int BUTTON_RED_COLOR;
    public static int BUTTON_GREEN_COLOR;
    public static int ACTION_CALL_COLOR;
    public static int ACTION_IMAGE_CALL_COLOR;
    public static int EDIT_AVATAR_BACKGROUND_COLOR;
    public static int EDIT_AVATAR_IMAGE_COLOR;
    public static int ITEM_BORDER_COLOR;
    public static int EDIT_TEXT_BACKGROUND_COLOR;
    public static int AVATAR_DEFAULT_COLOR;
    public static int EDIT_TEXT_TEXT_COLOR;
    public static int SEPARATOR_COLOR;
    public static int POPUP_BACKGROUND_COLOR;
    public static int MIGRATION_BACKGROUND_COLOR;
    public static int GREY_ITEM_COLOR;
    public static int SPLASHSCREEN_IMAGE_COLOR;
    public static int TIME_COLOR;
    public static int PEER_AUDIO_TRACK_COLOR;
    public static int FORWARD_COMMENT_COLOR;
    public static int SHOW_ICON_COLOR;
    public static int CUSTOM_TAB_BACKGROUND_COLOR;
    public static int CLOSE_COLOR;
    public static int DATE_BACKGROUND_COLOR;
    public static int REPLY_FONT_COLOR;
    public static int REPLY_BACKGROUND_COLOR;
    public static int CONVERSATION_ICON_COLOR;
    public static int CUSTOM_TAB_GREY_COLOR;

    //
    // Standard button
    //

    private static final float DESIGN_BUTTON_WIDTH = 640f;
    private static final float DESIGN_BUTTON_HEIGHT = 82f;
    private static final float DESIGN_BUTTON_MARGIN = 44f;

    private static final float DESIGN_MENU_CANCEL_HEIGHT = 100f;
    private static final float DESIGN_MENU_CANCEL_MARGIN = 28f;
    private static final float DESIGN_MENU_ACTION_MARGIN = 22f;

    public static int BUTTON_WIDTH;
    public static int BUTTON_HEIGHT;
    public static int BUTTON_MARGIN;
    public static int MENU_CANCEL_HEIGHT;
    public static int MENU_CANCEL_MARGIN;
    public static int MENU_ACTION_MARGIN;

    //
    //  Common size and margin
    //

    private static final int DESIGN_SLIDE_MARK_HEIGHT = 12;
    private static final int DESIGN_SLIDE_MARK_TOP_MARGIN = 16;
    private static final float DESIGN_CONTENT_VIEW_INITIAL_POSITION = 664f;
    private static final float DESIGN_CONTENT_VIEW_MIN_Y = 64f;
    private static final float DESIGN_CONTENT_VIEW_FOCUS_Y = 260f;
    private static final int DESIGN_TWINCODE_VIEW_TOP_MARGIN = 66;
    private static final float DESIGN_TWINCODE_PADDING = 20f;
    private static final float DESIGN_TWINCODE_ICON_SIZE = 42f;
    private static final float DESIGN_TWINCODE_ICON_PADDING = 27f;
    private static final int DESIGN_AVATAR_MAX_WIDTH = 990;
    private static final int DESIGN_AVATAR_MAX_HEIGHT = 916;
    private static final int DESIGN_AVATAR_HEIGHT = 86;
    private static final int DESIGN_CERTIFIED_HEIGHT = 28;
    private static final int DESIGN_NAME_TRAILING = 38;
    private static final float DESIGN_DESCRIPTION_CONTENT_VIEW_HEIGHT = 162f;
    private static final int DESIGN_AVATAR_OVER_WIDTH = 120;
    private static final int DESIGN_SECTION_HEIGHT = 120;
    private static final int DESIGN_EDIT_CLICKABLE_VIEW_HEIGHT = 80;
    private static final int DESIGN_HEADER_VIEW_TOP_MARGIN = 56;
    private static final int DESIGN_TITLE_IDENTITY_TOP_MARGIN = 80;
    private static final float DESIGN_ACTION_VIEW_MIN_MARGIN = 90f;
    private static final int DESIGN_ACTION_CLICKABLE_VIEW_HEIGHT = 172;
    private static final int DESIGN_IDENTITY_VIEW_TOP_MARGIN = 14;
    private static final int DESIGN_ACTION_VIEW_TOP_MARGIN = 34;
    private static final float DESIGN_ITEM_VIEW_HEIGHT = 124f;
    private static final float DESIGN_SELECTED_ITEM_VIEW_HEIGHT = 116f;
    private static final float DESIGN_EXPORT_VIEW_HEIGHT = 120f;
    private static final int DESIGN_DATE_VIEW_WIDTH = 240;
    private static final int DESIGN_DATE_VIEW_MARGIN = 20;
    private static final int DESIGN_DATE_VIEW_PADDING = 10;
    private static final int DESIGN_HOUR_VIEW_WIDTH = 180;
    private static final int DESIGN_BACK_CLICKABLE_VIEW_HEIGHT = 80;
    private static final int DESIGN_BACK_CLICKABLE_VIEW_TOP_MARGIN = 85;
    private static final int DESIGN_BACK_CLICKABLE_VIEW_LEFT_MARGIN = 34;
    private static final float DESIGN_NEW_FEATURE_HEIGHT = 50;
    private static final float DESIGN_NEW_FEATURE_MARGIN = 20;
    private static final int DESIGN_NEW_FEATURE_PADDING = 15;
    private static final int DESIGN_DOT_SIZE = 40;
    private static final int DESIGN_DOT_MARGIN = 20;
    private static final int DESIGN_ONBOARDING_TEXT_MARGIN = 44;

    public static int SLIDE_MARK_HEIGHT;
    public static int SLIDE_MARK_TOP_MARGIN;
    public static float CONTENT_VIEW_INITIAL_POSITION;
    public static float CONTENT_VIEW_MIN_Y;
    public static float CONTENT_VIEW_FOCUS_Y;
    public static int TWINCODE_VIEW_TOP_MARGIN;
    public static int TWINCODE_PADDING;
    public static int TWINCODE_ICON_SIZE;
    public static int TWINCODE_ICON_PADDING;
    public static int AVATAR_OVER_WIDTH;
    public static int AVATAR_MAX_WIDTH;
    public static int AVATAR_MAX_HEIGHT;
    public static int AVATAR_HEIGHT;
    public static int NAME_TRAILING;
    public static int CERTIFIED_HEIGHT;
    public static float DESCRIPTION_CONTENT_VIEW_HEIGHT;
    public static int SECTION_HEIGHT;
    public static int EDIT_CLICKABLE_VIEW_HEIGHT;
    public static int HEADER_VIEW_TOP_MARGIN;
    public static int TITLE_IDENTITY_TOP_MARGIN;
    public static int ACTION_VIEW_MIN_MARGIN;
    public static int ACTION_CLICKABLE_VIEW_HEIGHT;
    public static int IDENTITY_VIEW_TOP_MARGIN;
    public static int ACTION_VIEW_TOP_MARGIN;
    public static int ITEM_VIEW_HEIGHT;
    public static int SELECTED_ITEM_VIEW_HEIGHT;
    public static int EXPORT_VIEW_HEIGHT;
    public static int DATE_VIEW_WIDTH;
    public static int DATE_VIEW_MARGIN;
    public static int DATE_VIEW_PADDING;
    public static int HOUR_VIEW_WIDTH;
    public static int BACK_CLICKABLE_VIEW_HEIGHT;
    public static int BACK_CLICKABLE_VIEW_TOP_MARGIN;
    public static int BACK_CLICKABLE_VIEW_LEFT_MARGIN;
    public static int DOT_SIZE;
    public static int DOT_MARGIN;
    public static int ONBOARDING_TEXT_MARGIN;
    public static int NEW_FEATURE_HEIGHT;
    public static int NEW_FEATURE_MARGIN;
    public static int NEW_FEATURE_PADDING;

    //
    // Search view
    //

    private static final float DESIGN_SEARCH_VIEW_HEIGHT = 108f;
    public static int SEARCH_VIEW_HEIGHT;

    //
    // Toolbar item
    //

    private static final int DESIGN_TOOLBAR_TEXT_ITEM_PADDING = 17;
    public static int TOOLBAR_TEXT_ITEM_PADDING;

    private static final int DESIGN_TOOLBAR_IMAGE_ITEM_PADDING = 25;
    public static int TOOLBAR_IMAGE_ITEM_PADDING;

    //
    // Peer item
    //

    public static final int BORDER_WIDTH = 2;

    //
    // Radius
    //

    public static final float POPUP_RADIUS = 14f;
    public static final float CONTAINER_RADIUS = 11f;
    public static final float ACTION_RADIUS = 40;

    public static float mCurrentFontScale = 0;
    public static int mCurrentNightMode = 0;

    public static void init(@NonNull Context context, @NonNull TwinmeApplication application) {

        // Twinme layout has REFERENCE_WIDTH < REFERENCE_HEIGHT, make sure the display has the same constraint
        // otherwise we get wrong scaling factors.
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();

        final DisplayManager displayManager = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
        final Display defaultDisplay = displayManager.getDisplay(Display.DEFAULT_DISPLAY);

        if (metrics.heightPixels > metrics.widthPixels) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                final WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                Rect bounds = windowManager.getCurrentWindowMetrics().getBounds();
                DISPLAY_WIDTH = bounds.width();
                DISPLAY_HEIGHT = bounds.height();
            } else {
                if (defaultDisplay != null) {
                    Point size = new Point();
                    defaultDisplay.getRealSize(size);
                    DISPLAY_WIDTH = size.x;
                    DISPLAY_HEIGHT = size.y;

                } else {
                    DISPLAY_HEIGHT = metrics.heightPixels;
                    DISPLAY_WIDTH = metrics.widthPixels;
                }
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                final WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                Rect bounds = windowManager.getCurrentWindowMetrics().getBounds();
                DISPLAY_WIDTH = bounds.height();
                DISPLAY_HEIGHT = bounds.width();
            } else {
                if (defaultDisplay != null) {
                    Point size = new Point();
                    defaultDisplay.getRealSize(size);
                    //noinspection SuspiciousNameCombination
                    DISPLAY_WIDTH = size.y;
                    //noinspection SuspiciousNameCombination
                    DISPLAY_HEIGHT = size.x;
                } else {
                    //noinspection SuspiciousNameCombination
                    DISPLAY_HEIGHT = metrics.widthPixels;
                    //noinspection SuspiciousNameCombination
                    DISPLAY_WIDTH = metrics.heightPixels;
                }
            }
        }

        if(DISPLAY_HEIGHT == PREVIOUS_DISPLAY_HEIGHT && DISPLAY_WIDTH == PREVIOUS_DISPLAY_WIDTH){
            // No window/screen size change => nothing to do.
            return;
        }

        PREVIOUS_DISPLAY_HEIGHT = DISPLAY_HEIGHT;
        PREVIOUS_DISPLAY_WIDTH = DISPLAY_WIDTH;

        if (REFERENCE_HEIGHT * DISPLAY_WIDTH < REFERENCE_WIDTH * DISPLAY_HEIGHT) {
            HEIGHT_RATIO = DISPLAY_WIDTH / REFERENCE_WIDTH;
        } else {
            HEIGHT_RATIO = DISPLAY_HEIGHT / REFERENCE_HEIGHT;
        }
        //noinspection SuspiciousNameCombination
        WIDTH_RATIO = HEIGHT_RATIO;

        MIN_RATIO = Math.min(HEIGHT_RATIO, WIDTH_RATIO);

        GRADIENT_COLORS_GREEN = new GradientDescriptor(Color.rgb(112, 226, 205), Color.rgb(61, 193, 158));
        GRADIENT_COLORS_RED = new GradientDescriptor(Color.rgb(255, 132, 142), Color.rgb(255, 77, 85));

        LIGHT_CIRCULAR_SHADOW_DESCRIPTOR = new CircularShadowDescriptor(R.drawable.light_circular_shadow, 0.5f, (3f + 188f * 0.5f) / 223f,
                188f * 0.5f / 223f);

        DARK_CIRCULAR_SHADOW_DESCRIPTOR = new CircularShadowDescriptor(R.drawable.dark_circular_shadow, 0.5f, (9f + 80f * 0.5f) / 122f,
                80f * 0.5f / 122f);

        TOOLBAR_TEXT_ITEM_PADDING = (int) (DESIGN_TOOLBAR_TEXT_ITEM_PADDING * Design.WIDTH_RATIO);
        TOOLBAR_IMAGE_ITEM_PADDING = (int) (DESIGN_TOOLBAR_IMAGE_ITEM_PADDING * Design.WIDTH_RATIO);

        BUTTON_WIDTH = (int) (DESIGN_BUTTON_WIDTH * WIDTH_RATIO);
        BUTTON_HEIGHT = (int) (DESIGN_BUTTON_HEIGHT * HEIGHT_RATIO);
        BUTTON_MARGIN  = (int) (DESIGN_BUTTON_MARGIN * WIDTH_RATIO);
        MENU_CANCEL_HEIGHT = (int) (DESIGN_MENU_CANCEL_HEIGHT * HEIGHT_RATIO);
        MENU_CANCEL_MARGIN = (int) (DESIGN_MENU_CANCEL_MARGIN * HEIGHT_RATIO);
        MENU_ACTION_MARGIN = (int) (DESIGN_MENU_ACTION_MARGIN * HEIGHT_RATIO);

        SLIDE_MARK_HEIGHT = (int) (DESIGN_SLIDE_MARK_HEIGHT * HEIGHT_RATIO);
        SLIDE_MARK_TOP_MARGIN = (int) (DESIGN_SLIDE_MARK_TOP_MARGIN * HEIGHT_RATIO);
        CONTENT_VIEW_INITIAL_POSITION = (int) (DESIGN_CONTENT_VIEW_INITIAL_POSITION * HEIGHT_RATIO);
        CONTENT_VIEW_MIN_Y = (int) (DESIGN_CONTENT_VIEW_MIN_Y * HEIGHT_RATIO);
        CONTENT_VIEW_FOCUS_Y = (int) (DESIGN_CONTENT_VIEW_FOCUS_Y * HEIGHT_RATIO);
        TWINCODE_VIEW_TOP_MARGIN = (int) (DESIGN_TWINCODE_VIEW_TOP_MARGIN * HEIGHT_RATIO);
        TWINCODE_PADDING = (int) (DESIGN_TWINCODE_PADDING * Design.WIDTH_RATIO);
        TWINCODE_ICON_SIZE = (int) (DESIGN_TWINCODE_ICON_SIZE * Design.HEIGHT_RATIO);
        TWINCODE_ICON_PADDING = (int) (DESIGN_TWINCODE_ICON_PADDING * Design.HEIGHT_RATIO);
        AVATAR_OVER_WIDTH = (int) (DESIGN_AVATAR_OVER_WIDTH * WIDTH_RATIO);
        AVATAR_MAX_WIDTH = (int) (DESIGN_AVATAR_MAX_WIDTH * WIDTH_RATIO);
        AVATAR_MAX_HEIGHT = (int) (DESIGN_AVATAR_MAX_HEIGHT * HEIGHT_RATIO);
        AVATAR_HEIGHT = (int) (DESIGN_AVATAR_HEIGHT * HEIGHT_RATIO);
        NAME_TRAILING = (int) (DESIGN_NAME_TRAILING * WIDTH_RATIO);
        CERTIFIED_HEIGHT = (int) (DESIGN_CERTIFIED_HEIGHT * HEIGHT_RATIO);
        DESCRIPTION_CONTENT_VIEW_HEIGHT = (int) (DESIGN_DESCRIPTION_CONTENT_VIEW_HEIGHT * HEIGHT_RATIO);
        SECTION_HEIGHT = (int) (DESIGN_SECTION_HEIGHT * HEIGHT_RATIO);
        EDIT_CLICKABLE_VIEW_HEIGHT = (int) (DESIGN_EDIT_CLICKABLE_VIEW_HEIGHT * HEIGHT_RATIO);
        HEADER_VIEW_TOP_MARGIN = (int) (DESIGN_HEADER_VIEW_TOP_MARGIN * HEIGHT_RATIO);
        TITLE_IDENTITY_TOP_MARGIN = (int) (DESIGN_TITLE_IDENTITY_TOP_MARGIN * HEIGHT_RATIO);
        ACTION_VIEW_MIN_MARGIN = (int) (DESIGN_ACTION_VIEW_MIN_MARGIN * HEIGHT_RATIO);
        ACTION_CLICKABLE_VIEW_HEIGHT = (int) (DESIGN_ACTION_CLICKABLE_VIEW_HEIGHT * HEIGHT_RATIO);
        IDENTITY_VIEW_TOP_MARGIN = (int) (DESIGN_IDENTITY_VIEW_TOP_MARGIN * HEIGHT_RATIO);
        ACTION_VIEW_TOP_MARGIN = (int) (DESIGN_ACTION_VIEW_TOP_MARGIN * HEIGHT_RATIO);
        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * HEIGHT_RATIO);
        SELECTED_ITEM_VIEW_HEIGHT = (int) (DESIGN_SELECTED_ITEM_VIEW_HEIGHT * HEIGHT_RATIO);
        EXPORT_VIEW_HEIGHT = (int) (DESIGN_EXPORT_VIEW_HEIGHT * HEIGHT_RATIO);
        DATE_VIEW_WIDTH = (int) (DESIGN_DATE_VIEW_WIDTH * WIDTH_RATIO);
        DATE_VIEW_MARGIN = (int) (DESIGN_DATE_VIEW_MARGIN * WIDTH_RATIO);
        DATE_VIEW_PADDING = (int) (DESIGN_DATE_VIEW_PADDING * WIDTH_RATIO);
        HOUR_VIEW_WIDTH = (int) (DESIGN_HOUR_VIEW_WIDTH * HEIGHT_RATIO);
        BACK_CLICKABLE_VIEW_HEIGHT = (int) (DESIGN_BACK_CLICKABLE_VIEW_HEIGHT * HEIGHT_RATIO);
        BACK_CLICKABLE_VIEW_TOP_MARGIN = (int) (DESIGN_BACK_CLICKABLE_VIEW_TOP_MARGIN * HEIGHT_RATIO);
        BACK_CLICKABLE_VIEW_LEFT_MARGIN= (int) (DESIGN_BACK_CLICKABLE_VIEW_LEFT_MARGIN * WIDTH_RATIO);
        DOT_SIZE = (int) (DESIGN_DOT_SIZE * HEIGHT_RATIO);
        DOT_MARGIN = (int) (DESIGN_DOT_MARGIN * HEIGHT_RATIO);
        ONBOARDING_TEXT_MARGIN = (int) (DESIGN_ONBOARDING_TEXT_MARGIN * WIDTH_RATIO);
        NEW_FEATURE_HEIGHT = (int) (DESIGN_NEW_FEATURE_HEIGHT * HEIGHT_RATIO);
        NEW_FEATURE_MARGIN = (int) (DESIGN_NEW_FEATURE_MARGIN * WIDTH_RATIO);
        NEW_FEATURE_PADDING = (int) (DESIGN_NEW_FEATURE_PADDING * WIDTH_RATIO);

        SEARCH_VIEW_HEIGHT = (int) (DESIGN_SEARCH_VIEW_HEIGHT * HEIGHT_RATIO);

        setupFont(context, application);
        setupColor(context, application);
    }

    public static void setupFont(Context context, TwinmeApplication application) {

        Typeface regularTypeface = Typeface.DEFAULT;
        Typeface boldTypeface = Typeface.DEFAULT_BOLD;
        Typeface italicTypeface = Typeface.defaultFromStyle(Typeface.ITALIC);

        Typeface mediumTypeface;
        try {
            mediumTypeface = Typeface.create("sans-serif-medium", Typeface.NORMAL);
        } catch (Resources.NotFoundException exception) {
            Log.e("Design", "setupFont exception=" + exception);
            mediumTypeface = Typeface.DEFAULT;
        }

        int fontSize = Settings.fontSize.getInt();

        int adjustFontSize = 0;
        float fontScale = 1f;

        if (fontSize == FontSize.SYSTEM.ordinal()) {
            fontScale = context.getResources().getConfiguration().fontScale;
        } else if (fontSize == FontSize.SMALL.ordinal()) {
            adjustFontSize = -2;
        } else if (fontSize == FontSize.LARGE.ordinal()) {
            adjustFontSize = 2;
        } else if (fontSize == FontSize.EXTRA_LARGE.ordinal()) {
            adjustFontSize = 4;
        }

        mCurrentFontScale = fontScale;

        FONT_REGULAR16 = new TextStyle(regularTypeface, (MIN_RATIO * 16 * fontScale) + adjustFontSize);
        FONT_REGULAR20 = new TextStyle(regularTypeface, (MIN_RATIO * 20 * fontScale) + adjustFontSize);
        FONT_REGULAR22 = new TextStyle(regularTypeface, (MIN_RATIO * 22 * fontScale) + adjustFontSize);
        FONT_REGULAR24 = new TextStyle(regularTypeface, (MIN_RATIO * 24 * fontScale) + adjustFontSize);
        FONT_REGULAR26 = new TextStyle(regularTypeface, (MIN_RATIO * 26 * fontScale) + adjustFontSize);
        FONT_REGULAR28 = new TextStyle(regularTypeface, (MIN_RATIO * 28 * fontScale) + adjustFontSize);
        FONT_REGULAR30 = new TextStyle(regularTypeface, (MIN_RATIO * 30 * fontScale) + adjustFontSize);
        FONT_REGULAR32 = new TextStyle(regularTypeface, (MIN_RATIO * 32 * fontScale) + adjustFontSize);
        FONT_REGULAR34 = new TextStyle(regularTypeface, (MIN_RATIO * 34 * fontScale) + adjustFontSize);
        FONT_REGULAR36 = new TextStyle(regularTypeface, (MIN_RATIO * 36 * fontScale) + adjustFontSize);
        FONT_REGULAR40 = new TextStyle(regularTypeface, (MIN_RATIO * 40 * fontScale) + adjustFontSize);
        FONT_REGULAR44 = new TextStyle(regularTypeface, (MIN_RATIO * 44 * fontScale) + adjustFontSize);
        FONT_REGULAR50 = new TextStyle(regularTypeface, (MIN_RATIO * 50 * fontScale) + adjustFontSize);
        FONT_REGULAR64 = new TextStyle(regularTypeface, (MIN_RATIO * 64 * fontScale) + adjustFontSize);
        FONT_REGULAR88 = new TextStyle(regularTypeface, (MIN_RATIO * 88 * fontScale) + adjustFontSize);

        FONT_MEDIUM16 = new TextStyle(mediumTypeface, (MIN_RATIO * 16 * fontScale) + adjustFontSize);
        FONT_MEDIUM20 = new TextStyle(mediumTypeface, (MIN_RATIO * 20 * fontScale) + adjustFontSize);
        FONT_MEDIUM24 = new TextStyle(mediumTypeface, (MIN_RATIO * 24 * fontScale) + adjustFontSize);
        FONT_MEDIUM26 = new TextStyle(mediumTypeface, (MIN_RATIO * 26 * fontScale) + adjustFontSize);
        FONT_MEDIUM28 = new TextStyle(mediumTypeface, (MIN_RATIO * 28 * fontScale) + adjustFontSize);
        FONT_MEDIUM30 = new TextStyle(mediumTypeface, (MIN_RATIO * 30 * fontScale) + adjustFontSize);
        FONT_MEDIUM32 = new TextStyle(mediumTypeface, (MIN_RATIO * 32 * fontScale) + adjustFontSize);
        FONT_MEDIUM34 = new TextStyle(mediumTypeface, (MIN_RATIO * 34 * fontScale) + adjustFontSize);
        FONT_MEDIUM36 = new TextStyle(mediumTypeface, (MIN_RATIO * 36 * fontScale) + adjustFontSize);
        FONT_MEDIUM38 = new TextStyle(mediumTypeface, (MIN_RATIO * 38 * fontScale) + adjustFontSize);
        FONT_MEDIUM40 = new TextStyle(mediumTypeface, (MIN_RATIO * 40 * fontScale) + adjustFontSize);
        FONT_MEDIUM42 = new TextStyle(mediumTypeface, (MIN_RATIO * 42 * fontScale) + adjustFontSize);
        FONT_MEDIUM44 = new TextStyle(mediumTypeface, (MIN_RATIO * 44 * fontScale) + adjustFontSize);
        FONT_MEDIUM54 = new TextStyle(mediumTypeface, (MIN_RATIO * 54 * fontScale) + adjustFontSize);

        FONT_ITALIC_28 = new TextStyle(italicTypeface, (MIN_RATIO * 28 * fontScale) + adjustFontSize);

        FONT_BOLD26 = new TextStyle(boldTypeface, (MIN_RATIO * 26 * fontScale) + adjustFontSize);
        FONT_BOLD28 = new TextStyle(boldTypeface, (MIN_RATIO * 28 * fontScale) + adjustFontSize);
        FONT_BOLD32 = new TextStyle(boldTypeface, (MIN_RATIO * 32 * fontScale) + adjustFontSize);
        FONT_BOLD34 = new TextStyle(boldTypeface, (MIN_RATIO * 34 * fontScale) + adjustFontSize);
        FONT_BOLD36 = new TextStyle(boldTypeface, (MIN_RATIO * 36 * fontScale) + adjustFontSize);
        FONT_BOLD44 = new TextStyle(boldTypeface, (MIN_RATIO * 44 * fontScale) + adjustFontSize);
        FONT_BOLD54 = new TextStyle(boldTypeface, (MIN_RATIO * 54 * fontScale) + adjustFontSize);
        FONT_BOLD68 = new TextStyle(boldTypeface, (MIN_RATIO * 68 * fontScale) + adjustFontSize);
        FONT_BOLD88 = new TextStyle(boldTypeface, (MIN_RATIO * 88 * fontScale) + adjustFontSize);

        if (application.emojiFontSize() == EmojiSize.SMALL.ordinal()) {
            FONT_EMOJI_EXTRA_EXTRA_LARGE = new TextStyle(boldTypeface, (MIN_RATIO * 100 * fontScale) + adjustFontSize);
            FONT_EMOJI_EXTRA_LARGE = new TextStyle(boldTypeface, (MIN_RATIO * 80 * fontScale) + adjustFontSize);
            FONT_EMOJI_LARGE = new TextStyle(boldTypeface, (MIN_RATIO * 60 * fontScale) + adjustFontSize);
            FONT_EMOJI_MEDIUM = new TextStyle(boldTypeface, (MIN_RATIO * 40 * fontScale) + adjustFontSize);
            FONT_EMOJI_SMALL = new TextStyle(boldTypeface, (MIN_RATIO * 32 * fontScale) + adjustFontSize);
        } else if (application.emojiFontSize() == EmojiSize.STANDARD.ordinal()) {
            FONT_EMOJI_EXTRA_EXTRA_LARGE = new TextStyle(boldTypeface, (MIN_RATIO * 120 * fontScale) + adjustFontSize);
            FONT_EMOJI_EXTRA_LARGE = new TextStyle(boldTypeface, (MIN_RATIO * 100 * fontScale) + adjustFontSize);
            FONT_EMOJI_LARGE = new TextStyle(boldTypeface, (MIN_RATIO * 80 * fontScale) + adjustFontSize);
            FONT_EMOJI_MEDIUM = new TextStyle(boldTypeface, (MIN_RATIO * 60 * fontScale) + adjustFontSize);
            FONT_EMOJI_SMALL = new TextStyle(boldTypeface, (MIN_RATIO * 40 * fontScale) + adjustFontSize);
        } else {
            FONT_EMOJI_EXTRA_EXTRA_LARGE = new TextStyle(boldTypeface, (MIN_RATIO * 140 * fontScale) + adjustFontSize);
            FONT_EMOJI_EXTRA_LARGE = new TextStyle(boldTypeface, (MIN_RATIO * 120 * fontScale) + adjustFontSize);
            FONT_EMOJI_LARGE = new TextStyle(boldTypeface, (MIN_RATIO * 100 * fontScale) + adjustFontSize);
            FONT_EMOJI_MEDIUM = new TextStyle(boldTypeface, (MIN_RATIO * 80 * fontScale) + adjustFontSize);
            FONT_EMOJI_SMALL = new TextStyle(boldTypeface, (MIN_RATIO * 60 * fontScale) + adjustFontSize);
        }
    }

    public static void setupColor(@NonNull Context context, @NonNull TwinmeApplication application) {

        int displayMode = application.displayMode();

        if (displayMode == DisplayMode.SYSTEM.ordinal()) {
            setupSystemColor(context);
        } else if (displayMode == DisplayMode.LIGHT.ordinal()) {
            setupLightColor();
        } else if (displayMode == DisplayMode.DARK.ordinal()) {
            setupDarkColor();
        }
    }

    public static void setTheme(@NonNull Activity activity, @NonNull TwinmeApplication application) {

        int displayMode = application.displayMode();

        if (displayMode == DisplayMode.SYSTEM.ordinal()) {
            activity.setTheme(R.style.TwinmeThemeWithNoActionBar);
        } else if (displayMode == DisplayMode.LIGHT.ordinal()) {
            activity.setTheme(R.style.TwinmeThemeLight);
        } else if (displayMode == DisplayMode.DARK.ordinal()) {
            activity.setTheme(R.style.TwinmeThemeDark);
        }
    }

    public static DisplayMode getDisplayMode(int mode) {

        if (mode == DisplayMode.SYSTEM.ordinal()) {
            return DisplayMode.SYSTEM;
        } else if (mode == DisplayMode.LIGHT.ordinal()) {
            return DisplayMode.LIGHT;
        } else {
            return DisplayMode.DARK;
        }
    }

    public static boolean isDarkMode(@NonNull Context context) {

        boolean darkMode = false;
        int currentNightMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        int displayMode = Settings.displayMode.getInt();
        if ((currentNightMode == Configuration.UI_MODE_NIGHT_YES && displayMode == DisplayMode.SYSTEM.ordinal()) || displayMode == DisplayMode.DARK.ordinal()) {
            darkMode = true;
        }

        return darkMode;
    }

    public static void updateValues(@NonNull Context context, @NonNull TwinmeApplication application) {

        int currentNightMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        boolean updateColor = false;
        if (Settings.displayMode.getInt() == DisplayMode.SYSTEM.ordinal() && currentNightMode != mCurrentNightMode) {
            updateColor = true;
        }

        boolean updateScaleFont = false;
        if (Settings.fontSize.getInt() == FontSize.SYSTEM.ordinal() && mCurrentFontScale != context.getResources().getConfiguration().fontScale) {
            updateScaleFont = true;
        }

        if (updateColor) {
            setupColor(context, application);
        }

        if (updateScaleFont) {
            setupFont(context, application);
        }
    }

    public static void updateTextFont(TextView textView, TextStyle textStyle) {

        textView.setTypeface(textStyle.typeface);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textStyle.size);
    }

    public static void updateTextFont(EditText editText, TextStyle textStyle) {

        editText.setTypeface(textStyle.typeface);
        editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textStyle.size);
    }

    public static void updateTextFont(SwitchView switchView, TextStyle textStyle) {

        switchView.setTypeface(textStyle.typeface);
        switchView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textStyle.size);
    }

    public static void updateTextFont(RadioButton radioButton, TextStyle textStyle) {

        radioButton.setTypeface(textStyle.typeface);
        radioButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, textStyle.size);
    }

    private static void setupSystemColor(@NonNull Context context) {

        int currentNightMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        mCurrentNightMode = currentNightMode;
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            TOOLBAR_COLOR = Color.parseColor("#000000");
            CONVERSATION_ICON_COLOR = Design.PLACEHOLDER_COLOR;
        } else {
            TOOLBAR_COLOR = getMainStyle();
            CONVERSATION_ICON_COLOR = getMainStyle();
        }

        LIGHT_GREY_BACKGROUND_COLOR = ResourcesCompat.getColor(context.getResources(), R.color.background_light_grey_color, context.getTheme());
        GREY_BACKGROUND_COLOR = ResourcesCompat.getColor(context.getResources(), R.color.background_grey_color, context.getTheme());
        WHITE_COLOR = ResourcesCompat.getColor(context.getResources(), R.color.white_color, context.getTheme());
        BLACK_COLOR = ResourcesCompat.getColor(context.getResources(), R.color.black_color, context.getTheme());
        GREY_COLOR = ResourcesCompat.getColor(context.getResources(), R.color.grey, context.getTheme());
        FONT_COLOR_DEFAULT = ResourcesCompat.getColor(context.getResources(), R.color.font_default_color, context.getTheme());
        BACKGROUND_COLOR_WHITE_OPACITY85 = ResourcesCompat.getColor(context.getResources(), R.color.background_color_white_opacity_85, context.getTheme());
        BACKGROUND_COLOR_WHITE_OPACITY36 = ResourcesCompat.getColor(context.getResources(), R.color.background_color_white_opacity_36, context.getTheme());
        LIGHT_BORDER_COLOR = ResourcesCompat.getColor(context.getResources(), R.color.line_border_light_grey, context.getTheme());
        BUTTON_RED_COLOR = ResourcesCompat.getColor(context.getResources(), R.color.red_normal, context.getTheme());
        BUTTON_GREEN_COLOR = ResourcesCompat.getColor(context.getResources(), R.color.green_normal, context.getTheme());
        ACTION_CALL_COLOR = ResourcesCompat.getColor(context.getResources(), R.color.action_call_color, context.getTheme());
        ACTION_IMAGE_CALL_COLOR = ResourcesCompat.getColor(context.getResources(), R.color.action_image_call_color, context.getTheme());
        EDIT_AVATAR_BACKGROUND_COLOR = ResourcesCompat.getColor(context.getResources(), R.color.edit_avatar_background_color, context.getTheme());
        EDIT_AVATAR_IMAGE_COLOR = ResourcesCompat.getColor(context.getResources(), R.color.edit_avatar_image_color, context.getTheme());
        CONVERSATION_BACKGROUND_COLOR = ResourcesCompat.getColor(context.getResources(), R.color.conversation_background_color, context.getTheme());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            CONVERSATION_OVERLAY_COLOR = ResourcesCompat.getColor(context.getResources(), R.color.conversation_overlay_color_android_31, context.getTheme());
        } else {
            CONVERSATION_OVERLAY_COLOR = ResourcesCompat.getColor(context.getResources(), R.color.conversation_overlay_color, context.getTheme());
        }
        MENU_REACTION_BACKGROUND_COLOR = ResourcesCompat.getColor(context.getResources(), R.color.menu_reaction_background_color, context.getTheme());
        EDIT_TEXT_CONVERSATION_BACKGROUND_COLOR = ResourcesCompat.getColor(context.getResources(), R.color.edit_text_conversation_background_color, context.getTheme());
        ITEM_BORDER_COLOR = ResourcesCompat.getColor(context.getResources(), R.color.item_border_color, context.getTheme());
        EDIT_TEXT_BACKGROUND_COLOR = ResourcesCompat.getColor(context.getResources(), R.color.edit_text_background_color, context.getTheme());
        AVATAR_DEFAULT_COLOR = ResourcesCompat.getColor(context.getResources(), R.color.avatar_default_color, context.getTheme());
        EDIT_TEXT_TEXT_COLOR = ResourcesCompat.getColor(context.getResources(), R.color.edit_text_color, context.getTheme());
        SEPARATOR_COLOR = ResourcesCompat.getColor(context.getResources(), R.color.separator_color, context.getTheme());
        POPUP_BACKGROUND_COLOR = ResourcesCompat.getColor(context.getResources(), R.color.popup_background_color, context.getTheme());
        MIGRATION_BACKGROUND_COLOR = ResourcesCompat.getColor(context.getResources(), R.color.migration_background_color, context.getTheme());
        GREY_ITEM_COLOR = ResourcesCompat.getColor(context.getResources(), R.color.grey_item_color, context.getTheme());
        SPLASHSCREEN_IMAGE_COLOR = ResourcesCompat.getColor(context.getResources(), R.color.grey_item_color, context.getTheme());
        TIME_COLOR = ResourcesCompat.getColor(context.getResources(), R.color.time_color, context.getTheme());
        PEER_AUDIO_TRACK_COLOR = ResourcesCompat.getColor(context.getResources(), R.color.peer_audio_track_color, context.getTheme());
        FORWARD_COMMENT_COLOR = ResourcesCompat.getColor(context.getResources(), R.color.forward_comment_color, context.getTheme());
        SHOW_ICON_COLOR = ResourcesCompat.getColor(context.getResources(), R.color.switch_track_color, context.getTheme());
        CLOSE_COLOR = ResourcesCompat.getColor(context.getResources(), R.color.close_color, context.getTheme());
        CUSTOM_TAB_BACKGROUND_COLOR = ResourcesCompat.getColor(context.getResources(), R.color.custom_tab_background_color, context.getTheme());
        DATE_BACKGROUND_COLOR = ResourcesCompat.getColor(context.getResources(), R.color.date_background_color, context.getTheme());
        REPLY_BACKGROUND_COLOR = ResourcesCompat.getColor(context.getResources(), R.color.reply_background_color, context.getTheme());
        REPLY_FONT_COLOR = ResourcesCompat.getColor(context.getResources(), R.color.reply_font_color, context.getTheme());
        CUSTOM_TAB_GREY_COLOR = ResourcesCompat.getColor(context.getResources(), R.color.custom_tab_grey_color, context.getTheme());
    }

    private static void setupLightColor() {

        TOOLBAR_COLOR = getMainStyle();
        CONVERSATION_ICON_COLOR = getMainStyle();
        LIGHT_GREY_BACKGROUND_COLOR = Color.parseColor("#f9f9f9");
        GREY_BACKGROUND_COLOR = Color.parseColor("#efefef");
        WHITE_COLOR = Color.parseColor("#ffffff");
        BLACK_COLOR = Color.parseColor("#000000");
        GREY_COLOR = Color.parseColor("#bdb9b9");
        FONT_COLOR_DEFAULT = Color.parseColor("#2c2c2c");
        BACKGROUND_COLOR_WHITE_OPACITY85 = Color.parseColor("#d9ffffff");
        BACKGROUND_COLOR_WHITE_OPACITY36 = Color.parseColor("#5cffffff");
        LIGHT_BORDER_COLOR = Color.parseColor("#f3f4f8");
        BUTTON_RED_COLOR = Color.parseColor("#fd605d");
        BUTTON_GREEN_COLOR = Color.parseColor("#52cc7a");
        ACTION_CALL_COLOR = Color.parseColor("#ffffff");
        ACTION_IMAGE_CALL_COLOR = Color.parseColor("#000000");
        EDIT_AVATAR_BACKGROUND_COLOR = Color.parseColor("#f3f3f3");
        EDIT_AVATAR_IMAGE_COLOR = Color.parseColor("#c8c8c8");
        CONVERSATION_BACKGROUND_COLOR = Color.parseColor("#efefef");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            CONVERSATION_OVERLAY_COLOR = Color.parseColor("#ccffffff");
        } else {
            CONVERSATION_OVERLAY_COLOR = Color.parseColor("#e6ffffff");
        }

        MENU_REACTION_BACKGROUND_COLOR = Color.parseColor("#000000");
        EDIT_TEXT_CONVERSATION_BACKGROUND_COLOR = Color.parseColor("#f8f8f8");
        ITEM_BORDER_COLOR = Color.parseColor("#dadada");
        EDIT_TEXT_BACKGROUND_COLOR = Color.parseColor("#4DD5D7E0");
        AVATAR_DEFAULT_COLOR = Color.parseColor("#f2f3f5");
        EDIT_TEXT_TEXT_COLOR = Color.parseColor("#2c2c2c");
        SEPARATOR_COLOR = Color.parseColor("#80c7c7cc");
        POPUP_BACKGROUND_COLOR = Color.parseColor("#ffffff");
        MIGRATION_BACKGROUND_COLOR = Color.parseColor("#f3f3f3");
        GREY_ITEM_COLOR = Color.parseColor("#f3f3f3");
        TIME_COLOR = Color.parseColor("#666E6E6E");
        PEER_AUDIO_TRACK_COLOR = Color.parseColor("#333333");
        FORWARD_COMMENT_COLOR = Color.parseColor("#FFFFFF");
        SHOW_ICON_COLOR = Color.parseColor("#778A9F");
        CLOSE_COLOR = Color.parseColor("#00FFFFFF");
        CUSTOM_TAB_BACKGROUND_COLOR = Color.parseColor("#ffffff");
        DATE_BACKGROUND_COLOR = Color.parseColor("#efefef");
        REPLY_BACKGROUND_COLOR = Color.parseColor("#e7e7e7");
        REPLY_FONT_COLOR = Color.parseColor("#797979");
        CUSTOM_TAB_GREY_COLOR = Color.parseColor("#efefef");
    }

    private static void setupDarkColor() {

        TOOLBAR_COLOR = Color.parseColor("#000000");
        CONVERSATION_ICON_COLOR = Design.PLACEHOLDER_COLOR;
        LIGHT_GREY_BACKGROUND_COLOR = Color.parseColor("#000000");
        GREY_BACKGROUND_COLOR = Color.parseColor("#000000");
        WHITE_COLOR = Color.parseColor("#000000");
        BLACK_COLOR = Color.parseColor("#ffffff");
        GREY_COLOR = Color.parseColor("#bdb9b9");
        FONT_COLOR_DEFAULT = Color.parseColor("#ffffff");
        BACKGROUND_COLOR_WHITE_OPACITY85 = Color.parseColor("#d9000000");
        BACKGROUND_COLOR_WHITE_OPACITY36 = Color.parseColor("#5c000000");
        LIGHT_BORDER_COLOR = Color.parseColor("#f3f4f8");
        BUTTON_RED_COLOR = Color.parseColor("#fd605d");
        BUTTON_GREEN_COLOR = Color.parseColor("#52cc7a");
        ACTION_CALL_COLOR = Color.parseColor("#181a1e");
        ACTION_IMAGE_CALL_COLOR = Color.parseColor("#788a9f");
        EDIT_AVATAR_BACKGROUND_COLOR = Color.parseColor("#f3f3f3");
        EDIT_AVATAR_IMAGE_COLOR = Color.parseColor("#c8c8c8");
        CONVERSATION_BACKGROUND_COLOR = Color.parseColor("#000000");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            CONVERSATION_OVERLAY_COLOR = Color.parseColor("#cc000000");
        } else {
            CONVERSATION_OVERLAY_COLOR = Color.parseColor("#e6000000");
        }

        MENU_REACTION_BACKGROUND_COLOR = Color.parseColor("#484848");
        EDIT_TEXT_CONVERSATION_BACKGROUND_COLOR = Color.parseColor("#000000");
        ITEM_BORDER_COLOR = Color.parseColor("#dadada");
        EDIT_TEXT_BACKGROUND_COLOR = Color.parseColor("#4DD5D7E0");
        AVATAR_DEFAULT_COLOR = Color.parseColor("#f2f3f5");
        EDIT_TEXT_TEXT_COLOR = Color.parseColor("#ffffff");
        SEPARATOR_COLOR = Color.parseColor("#80C7C7FF");
        POPUP_BACKGROUND_COLOR = Color.parseColor("#484848");
        MIGRATION_BACKGROUND_COLOR = Color.parseColor("#484848");
        GREY_ITEM_COLOR = Color.parseColor("#484848");
        TIME_COLOR = Color.parseColor("#CC6E6E6E");
        PEER_AUDIO_TRACK_COLOR = Color.parseColor("#C7C7C7");
        FORWARD_COMMENT_COLOR = Color.parseColor("#484848");
        SHOW_ICON_COLOR = Color.parseColor("#778A9F");
        CLOSE_COLOR = Color.parseColor("#FFFFFF");
        CUSTOM_TAB_BACKGROUND_COLOR = Color.parseColor("#181b22");
        DATE_BACKGROUND_COLOR = Color.parseColor("#484848");
        REPLY_BACKGROUND_COLOR = Color.parseColor("#646464");
        REPLY_FONT_COLOR = Color.parseColor("#c8c8c8");
        CUSTOM_TAB_GREY_COLOR = Color.parseColor("#484848");
    }

    public static List<UIColorSpace> spaceColors() {

        List<UIColorSpace> colors = new ArrayList<>();
        colors.add(new UIColorSpace(null));
        colors.add(new UIColorSpace("#4B90E2"));
        colors.add(new UIColorSpace("#F07675"));
        colors.add(new UIColorSpace("#9DEDB4"));
        colors.add(new UIColorSpace("#9DDBED"));
        colors.add(new UIColorSpace("#89AC8F"));
        colors.add(new UIColorSpace("#E99616"));
        colors.add(new UIColorSpace("#F0CB26"));
        colors.add(new UIColorSpace("#EBBDBF"));
        return colors;
    }

    public static TextStyle customRegularFont(float size) {

        return new TextStyle(Typeface.DEFAULT, size);
    }

    public static int getMainStyle() {

        return Settings.mainStyle.getColor();
    }

    public static String getMainStyleString() {

        return Settings.mainStyle.getString();
    }

    public static void setMainStyle(String color) {

        if (color != null) {
            Settings.mainStyle.setString(color).save();
        } else {
            Settings.mainStyle.setString(DEFAULT_COLOR).save();
        }
    }

    public static int getDefaultColor(@Nullable String style) {

        return CommonUtils.parseColor(style, Color.parseColor(DEFAULT_COLOR));
    }

    public static int getHeight(int height) {

        return (int) (height * Design.HEIGHT_RATIO);
    }

    public static int mixColors(int color1, int color2) {

        int red1 = Color.red(color1);
        int green1 = Color.green(color1);
        int blue1 = Color.blue(color1);

        int red2 = Color.red(color2);
        int green2 = Color.green(color2);
        int blue2 = Color.blue(color2);

        int mixRed = (red1 + red2) / 2;
        int mixGreen = (green1 + green2) / 2;
        int mixBlue = (blue1 + blue2) / 2;

        return Color.rgb(mixRed, mixGreen, mixBlue);
    }

    public static int getItemBackgroundColor() {

        if (getMainStyleString().equals(Design.DEFAULT_COLOR)) {

            return ITEM_BACKGROUND_COLOR;
        }

        return getMainStyle();
    }

    public static int getItemFontColor(Context context) {

        if (getMainStyleString().equals(Design.DEFAULT_COLOR)) {

            return ITEM_FONT_COLOR;
        }

        return Color.WHITE;
    }

    public static TextStyle getEmojiFont(int nbEmoji) {

        if (nbEmoji == 1) {
            return FONT_EMOJI_EXTRA_EXTRA_LARGE;
        } else if (nbEmoji == 2) {
            return FONT_EMOJI_EXTRA_LARGE;
        } else if (nbEmoji == 3) {
            return FONT_EMOJI_LARGE;
        } else if (nbEmoji == 4) {
            return FONT_EMOJI_MEDIUM;
        }

        return FONT_EMOJI_SMALL;
    }

    public static TextStyle getSampleEmojiFont(Context context, EmojiSize emojiSize) {

        int fontSize = Settings.fontSize.getInt();

        int adjustFontSize = 0;
        float fontScale = 1f;

        if (fontSize == FontSize.SYSTEM.ordinal()) {
            fontScale = context.getResources().getConfiguration().fontScale;
        } else if (fontSize == FontSize.SMALL.ordinal()) {
            adjustFontSize = -2;
        } else if (fontSize == FontSize.LARGE.ordinal()) {
            adjustFontSize = 2;
        } else if (fontSize == FontSize.EXTRA_LARGE.ordinal()) {
            adjustFontSize = 4;
        }

        Typeface boldTypeface = Typeface.DEFAULT_BOLD;

        if (emojiSize == EmojiSize.SMALL) {
            return new TextStyle(boldTypeface, (MIN_RATIO * 100 * fontScale) + adjustFontSize);
        } else if (emojiSize == EmojiSize.STANDARD) {
            return new TextStyle(boldTypeface, (MIN_RATIO * 120 * fontScale) + adjustFontSize);
        } else {
            return new TextStyle(boldTypeface, (MIN_RATIO * 140 * fontScale) + adjustFontSize);
        }
    }
}
