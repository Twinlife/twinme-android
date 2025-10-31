/*
 *  Copyright (c) 2020 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.text.Spanned;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class OnboardingDialog extends Dialog {

    private static final float DESIGN_ALERT_TOP = 100;
    private static final float DESIGN_CONTAINER_WIDTH = 686;
    private static final float DESIGN_BUTTON_HEIGHT = 100;
    private static final float DESIGN_TEXT_TOP_MARGIN = 20;
    private static final float DESIGN_TEXT_BOTTOM_MARGIN = 40;
    private static final float DESIGN_BUTTON_BOTTOM_MARGIN = 17;
    private static final float DESIGN_LEFT_MARGIN = 22;
    private static final float DESIGN_RIGHT_MARGIN = 22;
    private static final float DESIGN_CLOSE_VIEW_SIZE = 52;
    private static final float DESIGN_CLOSE_VIEW_MARGIN = 18;
    private static final float DESIGN_IMAGE_TOP_MARGIN = 100;
    private static final float DESIGN_IMAGE_WIDTH = 604;
    private static final float DESIGN_IMAGE_HEIGHT = 500;
    private static final float DESIGN_CONTAINER_RADIUS = 6;

    public OnboardingDialog(@NonNull Context context) {

        super(context);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    public OnboardingDialog setup(Spanned message, Bitmap bitmap, String buttonText, Runnable runnable) {

        setContentView(R.layout.onboarding_dialog);

        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            WindowManager.LayoutParams windowManager = getWindow().getAttributes();
            windowManager.y = (int) (DESIGN_ALERT_TOP * Design.WIDTH_RATIO);
            windowManager.gravity = Gravity.TOP;
            window.setAttributes(windowManager);
        }

        RelativeLayout containerLayout = findViewById(R.id.onboarding_dialog_container);

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

        View closeView = findViewById(R.id.onboarding_dialog_close_view);
        layoutParams = closeView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_CLOSE_VIEW_SIZE * Design.MIN_RATIO);
        layoutParams.height = (int) (DESIGN_CLOSE_VIEW_SIZE * Design.MIN_RATIO);
        closeView.setLayoutParams(layoutParams);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) closeView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_CLOSE_VIEW_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_CLOSE_VIEW_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginEnd((int) (DESIGN_CLOSE_VIEW_MARGIN * Design.WIDTH_RATIO));

        closeView.setOnClickListener(v -> dismiss());

        ImageView imageView = findViewById(R.id.onboarding_dialog_image_view);
        layoutParams = imageView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_IMAGE_WIDTH * Design.WIDTH_RATIO);
        layoutParams.height = (int) (DESIGN_IMAGE_HEIGHT * Design.HEIGHT_RATIO);
        imageView.setLayoutParams(layoutParams);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) imageView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_IMAGE_TOP_MARGIN * Design.HEIGHT_RATIO);
        imageView.setLayoutParams(marginLayoutParams);

        imageView.setImageBitmap(bitmap);

        TextView messageView = findViewById(R.id.onboarding_dialog_message);
        marginLayoutParams = (ViewGroup.MarginLayoutParams) messageView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_TEXT_TOP_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_TEXT_BOTTOM_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.leftMargin = (int) (DESIGN_LEFT_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_RIGHT_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_LEFT_MARGIN * Design.WIDTH_RATIO));
        marginLayoutParams.setMarginEnd((int) (DESIGN_RIGHT_MARGIN * Design.WIDTH_RATIO));

        messageView.setLayoutParams(marginLayoutParams);

        Design.updateTextFont(messageView, Design.FONT_REGULAR32);
        messageView.setTextColor(Design.FONT_COLOR_DEFAULT);
        messageView.setText(message);

        View leftButton = findViewById(R.id.onboarding_dialog_negative_button);

        layoutParams = leftButton.getLayoutParams();
        layoutParams.height = (int) (DESIGN_BUTTON_HEIGHT * Design.HEIGHT_RATIO);

        leftButton.setLayoutParams(layoutParams);

        radius = DESIGN_CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable leftButtonBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        leftButtonBackground.getPaint().setColor(Design.BUTTON_RED_COLOR);
        leftButton.setBackground(leftButtonBackground);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) leftButton.getLayoutParams();
        marginLayoutParams.bottomMargin = (int) (DESIGN_BUTTON_BOTTOM_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.leftMargin = (int) (DESIGN_LEFT_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_RIGHT_MARGIN * Design.WIDTH_RATIO);

        leftButton.setLayoutParams(marginLayoutParams);

        TextView leftTextView = findViewById(R.id.onboarding_dialog_negative_button_title);
        Design.updateTextFont(leftTextView, Design.FONT_BOLD28);
        leftTextView.setAllCaps(false);
        leftTextView.setText(buttonText);

        leftButton.setOnClickListener(v -> runnable.run());

        return this;
    }
}
