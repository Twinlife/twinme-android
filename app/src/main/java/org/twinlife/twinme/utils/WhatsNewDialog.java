/*
 *  Copyright (c) 2022-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Romain Kolb (romain.kolb@skyrock.com)
 */

package org.twinlife.twinme.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Handler;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.skin.DisplayMode;
import org.twinlife.twinme.ui.Settings;
import org.twinlife.twinme.utils.update.LastVersion;

import java.util.ArrayList;
import java.util.List;

public class WhatsNewDialog extends Dialog implements CustomProgressBarView.Observer {

    private static final float DESIGN_IMAGE_TOP_MARGIN = 60;
    private static final float DESIGN_IMAGE_WIDTH = 410;
    private static final float DESIGN_IMAGE_HEIGHT = 360;
    private static final int DESIGN_SLIDE_MARK_WIDTH = 90;
    private static final int DESIGN_TITLE_MARGIN = 60;
    private static final int DESIGN_MESSAGE_MARGIN = 30;
    private static final int DESIGN_TEXT_MARGIN = 52;
    private static final int DESIGN_CONFIRM_TOP_MARGIN = 60;
    private static final int DESIGN_CONFIRM_BOTTOM_MARGIN = 80;
    private static final int DESIGN_MIN_HEIGHT = 962;
    private static final int DESIGN_CUSTOM_PROGRESS_TOP_MARGIN = 30;
    private static final int DESIGN_CUSTOM_PROGRESS_HEIGHT = 8;
    private static final int DESIGN_CUSTOM_PROGRESS_MARGIN = 10;
    private static final int DESIGN_CANCEL_VIEW_HEIGHT = 120;

    private static final float DESIGN_ALERT_TOP = 100;

    private RelativeLayout mProgressContainerView;
    private ImageView mImageView;
    private TextView mMessageView;
    private TextView mConfirmTextView;

    private final List<UIWhatsNew> mUIWhatsNew = new ArrayList<>();

    private int mCurrentWhatsNew = -1;
    private final List<CustomProgressBarView> mCustomProgressBarView = new ArrayList<>();

    private Runnable mRunnable;
    private boolean mShowAllWhatsNew = false;
    private boolean mStopOnFocusChanged = false;

    private final boolean mCurrentVersion;
    private boolean mUpdateMode;

    public WhatsNewDialog(@NonNull Context context, boolean currentVersion) {

        super(context);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mCurrentVersion = currentVersion;
    }

    public WhatsNewDialog setup(@NonNull LastVersion lastVersion, Runnable runnable, boolean updateMode) {

        mRunnable = runnable;
        mUpdateMode = updateMode;

        setContentView(R.layout.whatsnew_dialog);

        Window window = getWindow();
        if (window != null) {
            window.setDimAmount(0.f);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            WindowManager.LayoutParams windowManager = getWindow().getAttributes();
            windowManager.y = (int) (DESIGN_ALERT_TOP * Design.WIDTH_RATIO);
            windowManager.gravity = Gravity.TOP;
            window.setAttributes(windowManager);
        }

        View overlayView = findViewById(R.id.whats_new_dialog_overlay_view);
        overlayView.setBackgroundColor(Design.OVERLAY_VIEW_COLOR);
        overlayView.setOnClickListener(view -> onCancelClick());

        RelativeLayout containerLayout = findViewById(R.id.whats_new_dialog_container);
        containerLayout.setOnClickListener(null);

        float radius = Design.ACTION_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, 0, 0, 0, 0};

        ShapeDrawable scrollIndicatorBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        scrollIndicatorBackground.getPaint().setColor(Design.POPUP_BACKGROUND_COLOR);
        containerLayout.setBackground(scrollIndicatorBackground);

        View slideMarkView = findViewById(R.id.whats_new_dialog_slide_mark_view);

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

        TextView titleView = findViewById(R.id.whats_new_dialog_title);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) titleView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_TITLE_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.leftMargin = (int) (DESIGN_TEXT_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_TEXT_MARGIN * Design.WIDTH_RATIO);
        titleView.setLayoutParams(marginLayoutParams);

        mProgressContainerView = findViewById(R.id.whats_new_dialog_progress_container_view);
        mProgressContainerView.setVisibility(View.GONE);

        layoutParams = mProgressContainerView.getLayoutParams();
        layoutParams.width = (int) (Design.DISPLAY_WIDTH - (DESIGN_TEXT_MARGIN * 2 * Design.WIDTH_RATIO));
        layoutParams.height = (int) (DESIGN_CUSTOM_PROGRESS_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mProgressContainerView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_TEXT_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_TEXT_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.topMargin = (int) (DESIGN_CUSTOM_PROGRESS_TOP_MARGIN * Design.HEIGHT_RATIO);

        mMessageView = findViewById(R.id.whats_new_dialog_message);
        marginLayoutParams = (ViewGroup.MarginLayoutParams) mMessageView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_MESSAGE_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.leftMargin = (int) (DESIGN_TEXT_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_TEXT_MARGIN * Design.WIDTH_RATIO);

        mMessageView.setLayoutParams(marginLayoutParams);

        Design.updateTextFont(titleView, Design.FONT_BOLD44);
        titleView.setTextColor(Design.FONT_COLOR_DEFAULT);
        titleView.setText(lastVersion.getVersionNumber());

        Design.updateTextFont(mMessageView, Design.FONT_MEDIUM34);
        mMessageView.setTextColor(Design.FONT_COLOR_GREY);
        mMessageView.setMovementMethod(new ScrollingMovementMethod());
        float messageMaxHeight = Design.DISPLAY_HEIGHT - (DESIGN_MIN_HEIGHT * Design.HEIGHT_RATIO);
        mMessageView.setMaxHeight((int) messageMaxHeight);

        mImageView = findViewById(R.id.whats_new_dialog_image);
        mImageView.setOnClickListener(view -> onImageClick());

        layoutParams = mImageView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_IMAGE_WIDTH * Design.WIDTH_RATIO);
        layoutParams.height = (int) (DESIGN_IMAGE_HEIGHT * Design.HEIGHT_RATIO);
        mImageView.setLayoutParams(layoutParams);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mImageView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_IMAGE_TOP_MARGIN * Design.HEIGHT_RATIO);
        mImageView.setLayoutParams(marginLayoutParams);

        View confirmButton = findViewById(R.id.whats_new_dialog_negative_button);

        radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable confirmViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        confirmViewBackground.getPaint().setColor(Design.getMainStyle());
        confirmButton.setBackground(confirmViewBackground);

        layoutParams = confirmButton.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = Design.BUTTON_HEIGHT;

        confirmButton.setMinimumHeight(Design.BUTTON_HEIGHT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) confirmButton.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_CONFIRM_TOP_MARGIN * Design.HEIGHT_RATIO);

        mConfirmTextView = findViewById(R.id.whats_new_dialog_negative_button_title);
        Design.updateTextFont(mConfirmTextView, Design.FONT_BOLD36);
        mConfirmTextView.setAllCaps(false);
        mConfirmTextView.setTextColor(Color.WHITE);

        confirmButton.setOnClickListener(v -> onConfirmClick());
        setOnCancelListener(dialogInterface -> onCancelClick());

        View cancelView = findViewById(R.id.whats_new_dialog_cancel_view);
        cancelView.setOnClickListener(v -> onCancelClick());

        layoutParams = cancelView.getLayoutParams();
        layoutParams.width = Design.DISPLAY_WIDTH;
        layoutParams.height = (int) (DESIGN_CANCEL_VIEW_HEIGHT * Design.HEIGHT_RATIO);

        TextView cancelTextView = findViewById(R.id.whats_new_dialog_cancel_text_view);
        Design.updateTextFont(cancelTextView, Design.FONT_BOLD36);
        cancelTextView.setAllCaps(false);
        cancelTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        if (!mUpdateMode) {
            cancelView.setVisibility(View.GONE);
            marginLayoutParams = (ViewGroup.MarginLayoutParams) confirmButton.getLayoutParams();
            marginLayoutParams.bottomMargin = (int) (DESIGN_CONFIRM_BOTTOM_MARGIN * Design.HEIGHT_RATIO);
        } else {
            marginLayoutParams = (ViewGroup.MarginLayoutParams) cancelView.getLayoutParams();
            marginLayoutParams.bottomMargin = (int) (DESIGN_CONFIRM_BOTTOM_MARGIN * Design.HEIGHT_RATIO);
        }

        initLastVersion(lastVersion);

        return this;
    }

    @Override
    protected void onStop() {
        super.onStop();

        for (CustomProgressBarView customProgressBarView : mCustomProgressBarView) {
            if (customProgressBarView != null) {
                customProgressBarView.stopAnimation();
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (!hasFocus) {
            mStopOnFocusChanged = true;
            for (CustomProgressBarView customProgressBarView : mCustomProgressBarView) {
                if (customProgressBarView != null) {
                    customProgressBarView.stopAnimation();
                }
            }
        } else if (mCurrentWhatsNew != -1 && mStopOnFocusChanged) {
            nextWhatsNew();
        }
    }

    //
    // Implement CustomProgressBarView.Observer methods
    //

    @Override
    public void onCustomProgressBarEndAnimation() {

        nextWhatsNew();
    }

    private void onImageClick() {

        if (mUIWhatsNew.size() > 1) {

            if (mCurrentWhatsNew < mCustomProgressBarView.size()) {
                CustomProgressBarView customProgressBarView = mCustomProgressBarView.get(mCurrentWhatsNew);
                if (customProgressBarView != null) {
                    customProgressBarView.stopAnimation();
                }
            }

            nextWhatsNew();
        }
    }

    private void onConfirmClick() {

        if (mShowAllWhatsNew) {
            mRunnable.run();
        } else {
            if (mCurrentWhatsNew < mCustomProgressBarView.size()) {
                CustomProgressBarView customProgressBarView = mCustomProgressBarView.get(mCurrentWhatsNew);
                if (customProgressBarView != null) {
                    customProgressBarView.stopAnimation();
                }
            }

            nextWhatsNew();
        }
    }

    private void onCancelClick() {

        if (mShowAllWhatsNew) {
            cancel();
        } else {
            if (mCurrentWhatsNew < mCustomProgressBarView.size()) {
                CustomProgressBarView customProgressBarView = mCustomProgressBarView.get(mCurrentWhatsNew);
                if (customProgressBarView != null) {
                    customProgressBarView.stopAnimation();
                }
            }

            nextWhatsNew();
        }
    }

    private void initLastVersion(LastVersion lastVersion) {

        List<String> messages;

        if (mCurrentVersion || lastVersion.isMajorVersionWithUpdate(mUpdateMode)) {
            messages = lastVersion.getListMajorChanges();
        } else {
            messages = lastVersion.getListMinorChanges();
        }

        if (messages == null) {
            mShowAllWhatsNew = true;
            Handler handler = new Handler();
            handler.postDelayed(mRunnable, 100);
            return;
        }

        for (String message : messages) {
            UIWhatsNew uiWhatsNew = new UIWhatsNew(message, null);
            mUIWhatsNew.add(uiWhatsNew);
        }

        boolean darkMode = false;
        int currentNightMode = getContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        int displayMode = Settings.displayMode.getInt();
        if ((currentNightMode == Configuration.UI_MODE_NIGHT_YES && displayMode == DisplayMode.SYSTEM.ordinal())  || displayMode == DisplayMode.DARK.ordinal()) {
            darkMode = true;
        }

        List<String> images;

        if (darkMode && !lastVersion.getImagesDark().isEmpty()) {
            images = lastVersion.getImagesDark();
        } else {
            images = lastVersion.getImages();
        }

        if (images != null && !images.isEmpty() && images.size() == mUIWhatsNew.size()) {

            for (int index = 0; index <  images.size(); index++) {
                UIWhatsNew uiWhatsNew = mUIWhatsNew.get(index);
                uiWhatsNew.setImage(images.get(index));
            }

        } else {
            mImageView.setVisibility(View.GONE);
        }

        updateViews();
    }

    private void updateViews() {

        int textWidth = (int) (Design.DISPLAY_WIDTH - (DESIGN_TEXT_MARGIN * 2 * Design.WIDTH_RATIO));

        ViewGroup.LayoutParams layoutParams = mMessageView.getLayoutParams();

        if (mUIWhatsNew.size() == 1) {
            mShowAllWhatsNew = true;

            UIWhatsNew uiWhatsNew = mUIWhatsNew.get(0);
            if (uiWhatsNew.getImage() != null) {
                Glide.with(getContext())
                        .load(uiWhatsNew.getImage())
                        .centerInside()
                        .into(mImageView);
            } else {
                Bitmap placeholderBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.splash_screen_logo);
                mImageView.setImageBitmap(placeholderBitmap);
            }

            mMessageView.setText(uiWhatsNew.getMessage());

            mProgressContainerView.setVisibility(View.GONE);

            mConfirmTextView.setText(mUpdateMode ? getContext().getString(R.string.update_app_activity_update_title) : getContext().getString(R.string.application_ok));

            layoutParams.height = getMessageHeight(uiWhatsNew.getMessage(), textWidth);
        } else {
            boolean darkMode = false;
            int currentNightMode = getContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            int displayMode = Settings.displayMode.getInt();
            if ((currentNightMode == Configuration.UI_MODE_NIGHT_YES && displayMode == DisplayMode.SYSTEM.ordinal())  || displayMode == DisplayMode.DARK.ordinal()) {
                darkMode = true;
            }

            mProgressContainerView.setVisibility(View.VISIBLE);
            mConfirmTextView.setText(getContext().getString(R.string.welcome_activity_next));

            int customBarMargin = (int) (DESIGN_CUSTOM_PROGRESS_MARGIN * Design.WIDTH_RATIO);
            int customBarProgressWidth = (textWidth - ((mUIWhatsNew.size() - 1) * customBarMargin)) / mUIWhatsNew.size();
            int customBarProgressHeight = (int) (DESIGN_CUSTOM_PROGRESS_HEIGHT * Design.HEIGHT_RATIO);

            int textMaxHeight = 0;
            int index = 0;

            for (UIWhatsNew whatsNew : mUIWhatsNew) {

                int textHeight = getMessageHeight(whatsNew.getMessage(), textWidth);
                if (textHeight > textMaxHeight) {
                    textMaxHeight = textHeight;
                }

                int x = index == 0 ? 0 : index * customBarProgressWidth + (customBarMargin * index);

                ViewGroup.LayoutParams customBarLayoutParams = new ViewGroup.LayoutParams(customBarProgressWidth, customBarProgressHeight);

                CustomProgressBarView customProgressBarView = new CustomProgressBarView(getContext(), null);
                customProgressBarView.setObserver(this);
                customProgressBarView.setDarkMode(darkMode);
                customProgressBarView.setLayoutParams(customBarLayoutParams);
                customProgressBarView.setX(x);
                customProgressBarView.initProgressBar(customBarProgressWidth, customBarProgressHeight);

                mCustomProgressBarView.add(customProgressBarView);
                mProgressContainerView.addView(customProgressBarView);
                index++;
            }

            layoutParams.height = textMaxHeight;

            nextWhatsNew();
        }
    }

    private int getMessageHeight(String message, int textWidth) {

        TextPaint textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(Design.FONT_MEDIUM34.size);
        textPaint.setTypeface(Design.FONT_MEDIUM34.typeface);

        Layout.Alignment alignment = Layout.Alignment.ALIGN_NORMAL;
        StaticLayout staticLayout = new StaticLayout(message, textPaint, textWidth, alignment, 1, 0, false);
        return staticLayout.getHeight();
    }

    private void nextWhatsNew() {

        mCurrentWhatsNew++;

        // Make sure we have a valid context before proceeding.
        Context context = getContext();
        if (context instanceof ContextWrapper) {
            context = ((ContextWrapper) context).getBaseContext();
        }
        if (!(context instanceof Activity) || ((Activity ) context).isDestroyed()) {
            return;
        }

        if (mCurrentWhatsNew == mUIWhatsNew.size()) {
            mCurrentWhatsNew = 0;

            for (CustomProgressBarView customProgressBarView : mCustomProgressBarView) {
                if (customProgressBarView != null) {
                    customProgressBarView.resetAnimation();
                }
            }
        }

        if (mCurrentWhatsNew < mUIWhatsNew.size()) {
            UIWhatsNew uiWhatsNew = mUIWhatsNew.get(mCurrentWhatsNew);

            if (uiWhatsNew.getImage() != null) {
                Glide.with(context)
                        .load(uiWhatsNew.getImage())
                        .centerInside()
                        .into(mImageView);
            } else {
                Bitmap placeholderBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.splash_screen_logo);
                mImageView.setImageBitmap(placeholderBitmap);
            }

            mMessageView.setText(uiWhatsNew.getMessage());

            if (mCurrentWhatsNew < mCustomProgressBarView.size()) {
                CustomProgressBarView customProgressBarView = mCustomProgressBarView.get(mCurrentWhatsNew);
                if (customProgressBarView != null) {
                    customProgressBarView.startAnimation();
                }
            }
        }

        if (mCurrentWhatsNew + 1 == mUIWhatsNew.size()) {
            mConfirmTextView.setText(mUpdateMode ? getContext().getString(R.string.update_app_activity_update_title) : getContext().getString(R.string.application_ok));
            mShowAllWhatsNew = true;
        }
    }
}
