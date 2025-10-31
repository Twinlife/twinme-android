/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.welcomeActivity;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.skin.DisplayMode;
import org.twinlife.twinme.ui.AbstractOnboardingActivity;
import org.twinlife.twinme.ui.Settings;
import org.twinlife.twinme.utils.DotsAdapter;

import java.util.ArrayList;
import java.util.List;

public class WelcomeHelpActivity extends AbstractOnboardingActivity {
    private static final String LOG_TAG = "WelcomeHelpActivity";
    private static final boolean DEBUG = false;

    private static final int MIN_CONTENT_VIEW_HEIGHT = 256;
    private static final int MIN_CONTENT_CELL_HEIGHT = 662;

    private static final int DESIGN_LOGO_HEIGHT = 68;

    private final List<UIWelcome> mUIWelcome = new ArrayList<>();

    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        super.onResume();
    }


    //
    // Private methods
    //


    @Override
    protected void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color,  ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.WHITE_COLOR));
        setContentView(R.layout.welcome_help_activity);
        setBackgroundColor(Color.TRANSPARENT);

        applyInsets(R.id.welcome_help_activity_layout, -1 , R.id.welcome_help_activity_action_view, Color.TRANSPARENT, true);

        mOverlayView = findViewById(R.id.welcome_help_activity_overlay_view);
        mActionView = findViewById(R.id.welcome_help_activity_action_view);
        View slideMarkView = findViewById(R.id.welcome_help_activity_slide_mark_view);

        mOverlayView.setBackgroundColor(Design.OVERLAY_VIEW_COLOR);
        mOverlayView.setAlpha(0);
        mOverlayView.setOnClickListener(v -> onDismissClick());

        mActionView.setY(Design.DISPLAY_HEIGHT);

        float radius = Design.ACTION_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, 0, 0, 0, 0};

        ShapeDrawable scrollIndicatorBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        scrollIndicatorBackground.getPaint().setColor(Design.POPUP_BACKGROUND_COLOR);
        mActionView.setBackground(scrollIndicatorBackground);

        ViewGroup.LayoutParams layoutParams = slideMarkView.getLayoutParams();
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

        ImageView logoImageView = findViewById(R.id.welcome_help_activity_logo);

        layoutParams = logoImageView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_LOGO_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) logoImageView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_ONBOARDING_TOP_MARGIN * Design.HEIGHT_RATIO);

        initWelcome();

        WelcomeAdapter welcomeAdapter = new WelcomeAdapter(this, mUIWelcome);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);

        mRecyclerView = findViewById(R.id.welcome_help_activity_recycler_view);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(welcomeAdapter);
        mRecyclerView.setItemAnimator(null);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mRecyclerView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_ONBOARDING_TOP_MARGIN * Design.HEIGHT_RATIO);

        SnapHelper pagerSnapHelper = new PagerSnapHelper();
        pagerSnapHelper.attachToRecyclerView(mRecyclerView);

        RecyclerView dotsRecyclerView = findViewById(R.id.welcome_help_activity_dots_view);
        mDotsAdapter = new DotsAdapter(mUIWelcome.size(), getLayoutInflater());

        LinearLayoutManager dotsLinearLayoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        dotsRecyclerView.setLayoutManager(dotsLinearLayoutManager);
        dotsRecyclerView.setAdapter(mDotsAdapter);
        dotsRecyclerView.setItemAnimator(null);

        setupWelcome();

        layoutParams = dotsRecyclerView.getLayoutParams();
        layoutParams.width = mUIWelcome.size() * Design.DOT_SIZE;
        dotsRecyclerView.setLayoutParams(layoutParams);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) dotsRecyclerView.getLayoutParams();
        marginLayoutParams.topMargin = Design.DOT_MARGIN;
        marginLayoutParams.bottomMargin = Design.DOT_MARGIN;

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    View centerView = pagerSnapHelper.findSnapView(linearLayoutManager);
                    if (centerView != null) {
                        mCurrentPosition = linearLayoutManager.getPosition(centerView);
                        mDotsAdapter.setCurrentPosition(mCurrentPosition);
                    }
                }
            }
        });

        ViewTreeObserver viewTreeObserver = mActionView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (DEBUG) {
                    Log.d(LOG_TAG, "onGlobalLayout");
                }

                ViewTreeObserver viewTreeObserver = mActionView.getViewTreeObserver();
                viewTreeObserver.removeOnGlobalLayoutListener(this);

                if (!mShowActionView) {
                    mActionView.postDelayed(() -> {
                        mRootHeight = mOverlayView.getHeight();
                        mActionHeight = mActionView.getHeight();

                        showOnboardingView();
                    }, Design.ANIMATION_VIEW_DURATION);
                }
            }
        });
    }

    private void onDismissClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDismissClick");
        }

        animationClose();
    }

    private void initWelcome() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initWelcome");
        }

        boolean darkMode = false;
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        int displayMode = Settings.displayMode.getInt();
        if ((currentNightMode == Configuration.UI_MODE_NIGHT_YES && displayMode == DisplayMode.SYSTEM.ordinal())  || displayMode == DisplayMode.DARK.ordinal()) {
            darkMode = true;
        }

        mUIWelcome.add(new UIWelcome(getString(R.string.welcome_activity_step1_message), darkMode ? R.drawable.onboarding_step1_dark : R.drawable.onboarding_step1));
        mUIWelcome.add(new UIWelcome(getString(R.string.welcome_activity_step2_message), darkMode ? R.drawable.onboarding_step2_dark : R.drawable.onboarding_step2));
        mUIWelcome.add(new UIWelcome(getString(R.string.welcome_activity_step3_message), darkMode ? R.drawable.onboarding_step3_dark : R.drawable.onboarding_step3));
    }

    private void setupWelcome() {
        if (DEBUG) {
            Log.d(LOG_TAG, "setupWelcome");
        }

        float maxRecyclerViewHeight = Design.DISPLAY_HEIGHT - (MIN_CONTENT_VIEW_HEIGHT * Design.HEIGHT_RATIO) - Design.DOT_SIZE;
        int textWidth = (int ) (Design.DISPLAY_WIDTH - (Design.ONBOARDING_TEXT_MARGIN * 2));

        float recyclerViewHeight = 0;
        float minContentCellHeight = (MIN_CONTENT_CELL_HEIGHT * Design.HEIGHT_RATIO);

        for (UIWelcome uiWelcome : mUIWelcome) {

            float height = uiWelcome.getMessageHeight(textWidth);
            float contentHeight = minContentCellHeight + height;

            if (contentHeight > recyclerViewHeight) {
                recyclerViewHeight = contentHeight;
            }
        }

        if (recyclerViewHeight > maxRecyclerViewHeight) {
            recyclerViewHeight = maxRecyclerViewHeight;
        }

        ViewGroup.LayoutParams layoutParams = mRecyclerView.getLayoutParams();
        layoutParams.height = (int) recyclerViewHeight;
    }
}
