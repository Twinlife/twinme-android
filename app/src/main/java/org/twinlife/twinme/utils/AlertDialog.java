/*
 *  Copyright (c) 2018-2020 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Yannis Le Gal (Yannis.LeGal@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.text.Spanned;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class AlertDialog extends Dialog {

    private static final int DESIGN_MESSAGE_COLOR = Color.argb(255, 178, 178, 178);

    private static final int DESIGN_SLIDE_MARK_WIDTH = 90;
    private static final int DESIGN_TITLE_MARGIN = 60;
    private static final int DESIGN_MESSAGE_MARGIN = 30;
    private static final int DESIGN_TEXT_MARGIN = 52;
    private static final int DESIGN_CONFIRM_TOP_MARGIN = 70;
    private static final int DESIGN_CONFIRM_BOTTOM_MARGIN = 80;

    private static final float DESIGN_ALERT_TOP = 100;
    private static final float DESIGN_CONTAINER_WIDTH = 686;
    private static final float DESIGN_BUTTON_WIDTH = 302;
    private static final float DESIGN_BUTTON_HEIGHT = 100;
    private static final float DESIGN_TITLE_TOP_MARGIN = 80;
    private static final float DESIGN_TEXT_TOP_MARGIN = 40;
    private static final float DESIGN_TEXT_BOTTOM_MARGIN = 40;
    private static final float DESIGN_BUTTON_BOTTOM_MARGIN = 17;
    private static final float DESIGN_LEFT_MARGIN = 22;
    private static final float DESIGN_RIGHT_MARGIN = 22;
    private static final float DESIGN_CLOSE_VIEW_SIZE = 52;
    private static final float DESIGN_CLOSE_VIEW_MARGIN = 18;
    private static final float DESIGN_CONTAINER_RADIUS = 6;

    private boolean mCancelClick = false;

    public AlertDialog(@NonNull Context context) {

        super(context);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    public AlertDialog setup(@NonNull String title, @NonNull Spanned message, @Nullable String leftButtonText,
                             @Nullable String rightButtonText, @Nullable Runnable leftRunnable, @Nullable Runnable rightRunnable) {

        setContentView(R.layout.alert_dialog);

        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            WindowManager.LayoutParams windowManager = getWindow().getAttributes();
            windowManager.y = (int) (DESIGN_ALERT_TOP * Design.WIDTH_RATIO);
            windowManager.gravity = Gravity.TOP;
            window.setAttributes(windowManager);
        }

        RelativeLayout containerLayout = findViewById(R.id.alert_dialog_container);

        float radius = Design.POPUP_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable popupViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        popupViewBackground.getPaint().setColor(Design.POPUP_BACKGROUND_COLOR);
        containerLayout.setBackground(popupViewBackground);

        ViewGroup.LayoutParams layoutParams = containerLayout.getLayoutParams();
        layoutParams.width = (int) (DESIGN_CONTAINER_WIDTH * Design.WIDTH_RATIO);
        containerLayout.setLayoutParams(layoutParams);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) containerLayout.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_ALERT_TOP * Design.HEIGHT_RATIO);

        View closeView = findViewById(R.id.alert_dialog_close_view);
        layoutParams = closeView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_CLOSE_VIEW_SIZE * Design.MIN_RATIO);
        layoutParams.height = (int) (DESIGN_CLOSE_VIEW_SIZE * Design.MIN_RATIO);
        closeView.setLayoutParams(layoutParams);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) closeView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_CLOSE_VIEW_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_CLOSE_VIEW_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginEnd((int) (DESIGN_CLOSE_VIEW_MARGIN * Design.WIDTH_RATIO));

        TextView titleView = findViewById(R.id.alert_dialog_title);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) titleView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_TITLE_TOP_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.leftMargin = (int) (DESIGN_LEFT_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_RIGHT_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_LEFT_MARGIN * Design.WIDTH_RATIO));
        marginLayoutParams.setMarginEnd((int) (DESIGN_RIGHT_MARGIN * Design.WIDTH_RATIO));

        titleView.setLayoutParams(marginLayoutParams);

        TextView messageView = findViewById(R.id.alert_dialog_message);
        marginLayoutParams = (ViewGroup.MarginLayoutParams) messageView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_TEXT_TOP_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_TEXT_BOTTOM_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.leftMargin = (int) (DESIGN_LEFT_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_RIGHT_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_LEFT_MARGIN * Design.WIDTH_RATIO));
        marginLayoutParams.setMarginEnd((int) (DESIGN_RIGHT_MARGIN * Design.WIDTH_RATIO));

        messageView.setLayoutParams(marginLayoutParams);

        Design.updateTextFont(titleView, Design.FONT_REGULAR34);
        titleView.setTextColor(Design.FONT_COLOR_DEFAULT);
        titleView.setText(title);

        Design.updateTextFont(messageView, Design.FONT_REGULAR28);
        messageView.setTextColor(DESIGN_MESSAGE_COLOR);
        messageView.setText(message);

        View leftButton = findViewById(R.id.alert_dialog_negative_button);
        View rightButton = findViewById(R.id.alert_dialog_positive_button);

        layoutParams = leftButton.getLayoutParams();
        layoutParams.width = (int) (DESIGN_BUTTON_WIDTH * Design.WIDTH_RATIO);
        layoutParams.height = (int) (DESIGN_BUTTON_HEIGHT * Design.HEIGHT_RATIO);
        leftButton.setLayoutParams(layoutParams);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) leftButton.getLayoutParams();
        marginLayoutParams.bottomMargin = (int) (DESIGN_BUTTON_BOTTOM_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.leftMargin = (int) (DESIGN_LEFT_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_LEFT_MARGIN * Design.WIDTH_RATIO));

        leftButton.setLayoutParams(marginLayoutParams);

        radius = DESIGN_CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable leftButtonBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        leftButtonBackground.getPaint().setColor(Design.BUTTON_RED_COLOR);
        leftButton.setBackground(leftButtonBackground);

        TextView leftTextView = findViewById(R.id.alert_dialog_negative_button_title);
        Design.updateTextFont(leftTextView, Design.FONT_BOLD28);
        leftTextView.setAllCaps(false);
        leftTextView.setText(leftButtonText);

        TextView rightTextView = findViewById(R.id.alert_dialog_positive_button_title);
        Design.updateTextFont(rightTextView, Design.FONT_BOLD28);
        rightTextView.setAllCaps(false);
        rightTextView.setText(rightButtonText);

        layoutParams = rightButton.getLayoutParams();
        layoutParams.width = (int) (DESIGN_BUTTON_WIDTH * Design.WIDTH_RATIO);
        layoutParams.height = (int) (DESIGN_BUTTON_HEIGHT * Design.HEIGHT_RATIO);
        rightButton.setLayoutParams(layoutParams);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) rightButton.getLayoutParams();
        marginLayoutParams.bottomMargin = (int) (DESIGN_BUTTON_BOTTOM_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_RIGHT_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginEnd((int) (DESIGN_RIGHT_MARGIN * Design.WIDTH_RATIO));

        rightButton.setLayoutParams(marginLayoutParams);

        radius = DESIGN_CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable rightButtonBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        rightButtonBackground.getPaint().setColor(Design.BLUE_NORMAL);
        rightButton.setBackground(rightButtonBackground);

        if (leftRunnable != null) {
            closeView.setOnClickListener(v -> {
                mCancelClick = true;
                leftRunnable.run();
            });
            leftButton.setOnClickListener(v -> leftRunnable.run());

            setOnCancelListener(dialog -> {
                mCancelClick = true;
                leftRunnable.run();
            });
        } else {
            closeView.setOnClickListener(v -> dismiss());
        }

        if (rightRunnable != null) {
            rightButton.setOnClickListener(v -> rightRunnable.run());
        }

        return this;
    }

    public AlertDialog setup(String title, Spanned message, String leftButtonText, Runnable leftRunnable) {

        setContentView(R.layout.alert_dialog_one_button);

        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            WindowManager.LayoutParams windowManager = getWindow().getAttributes();
            windowManager.y = (int) (DESIGN_ALERT_TOP * Design.WIDTH_RATIO);
            windowManager.gravity = Gravity.TOP;
            window.setAttributes(windowManager);
        }

        RelativeLayout containerLayout = findViewById(R.id.alert_dialog_container);

        float radius = Design.ACTION_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, 0, 0, 0, 0};

        ShapeDrawable scrollIndicatorBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        scrollIndicatorBackground.getPaint().setColor(Design.POPUP_BACKGROUND_COLOR);
        containerLayout.setBackground(scrollIndicatorBackground);

        View slideMarkView = findViewById(R.id.alert_dialog_slide_mark_view);

        ViewGroup.LayoutParams layoutParams = slideMarkView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_SLIDE_MARK_WIDTH * Design.WIDTH_RATIO);
        layoutParams.height = Design.SLIDE_MARK_HEIGHT;

        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.mutate();
        gradientDrawable.setColor(Color.rgb(244, 244, 244));
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        slideMarkView.setBackground(gradientDrawable);

        float corner = ((float)Design.SLIDE_MARK_HEIGHT / 2) * Resources.getSystem().getDisplayMetrics().density;
        gradientDrawable.setCornerRadius(corner);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) slideMarkView.getLayoutParams();
        marginLayoutParams.topMargin = Design.SLIDE_MARK_TOP_MARGIN;

        TextView titleView = findViewById(R.id.alert_dialog_title);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) titleView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_TITLE_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.leftMargin = (int) (DESIGN_TEXT_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_TEXT_MARGIN * Design.WIDTH_RATIO);
        titleView.setLayoutParams(marginLayoutParams);

        TextView messageView = findViewById(R.id.alert_dialog_message);
        marginLayoutParams = (ViewGroup.MarginLayoutParams) messageView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_MESSAGE_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.leftMargin = (int) (DESIGN_TEXT_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_TEXT_MARGIN * Design.WIDTH_RATIO);

        messageView.setLayoutParams(marginLayoutParams);

        Design.updateTextFont(titleView, Design.FONT_BOLD44);
        titleView.setTextColor(Design.FONT_COLOR_DEFAULT);
        titleView.setText(title);

        Design.updateTextFont(messageView, Design.FONT_MEDIUM40);
        messageView.setTextColor(Design.FONT_COLOR_GREY);
        messageView.setText(message);

        View leftButton = findViewById(R.id.alert_dialog_negative_button);

        radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable confirmViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        confirmViewBackground.getPaint().setColor(Design.getMainStyle());
        leftButton.setBackground(confirmViewBackground);

        layoutParams = leftButton.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = Design.BUTTON_HEIGHT;

        leftButton.setMinimumHeight(Design.BUTTON_HEIGHT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) leftButton.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_CONFIRM_TOP_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_CONFIRM_BOTTOM_MARGIN * Design.HEIGHT_RATIO);

        TextView leftTextView = findViewById(R.id.alert_dialog_negative_button_title);
        Design.updateTextFont(leftTextView, Design.FONT_BOLD36);
        leftTextView.setAllCaps(false);
        leftTextView.setText(leftButtonText);
        leftTextView.setTextColor(Color.WHITE);

        if (leftRunnable != null) {
            leftButton.setOnClickListener(v -> leftRunnable.run());
            setOnCancelListener(dialogInterface -> leftRunnable.run());
        }

        return this;
    }

    public boolean isCancelClick() {

        return mCancelClick;
    }
}
