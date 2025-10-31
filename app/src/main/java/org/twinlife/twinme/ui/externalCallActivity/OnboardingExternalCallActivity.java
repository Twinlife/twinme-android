/*
 *  Copyright (c) 2023-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.externalCallActivity;

import android.content.Intent;
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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractOnboardingActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.TwinmeApplication;
import org.twinlife.twinme.utils.DotsAdapter;

import java.util.ArrayList;
import java.util.List;

public class OnboardingExternalCallActivity extends AbstractOnboardingActivity {
    private static final String LOG_TAG = "OnboardingExternal...";
    private static final boolean DEBUG = false;

    private static final int MIN_CONTENT_VIEW_HEIGHT = 148;
    private static final int MIN_CONTENT_CELL_FROM_SIDE_MENU_HEIGHT = 542;
    private static final int MIN_CONTENT_CELL_HEIGHT = 662;

    private final List<UIOnboarding> mUIOnboarding = new ArrayList<>();

    private boolean mFromSideMenu = false;

    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        mFromSideMenu = getIntent().getBooleanExtra(Intents.INTENT_FROM_SIDE_MENU, false);
        initOnboarding();

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        super.onResume();
    }

    public void onCreateExternalCallClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateExternalCallClick");
        }

        if (!mFromSideMenu) {
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        } else {
            animationClose();
        }
    }

    public void onDoNotShowAgainClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDoNotShowAgainClick");
        }

        getTwinmeApplication().setShowOnboardingType(TwinmeApplication.OnboardingType.EXTERNAL_CALL, false);

        finish();
    }

    public boolean isFromSideMenu() {
        if (DEBUG) {
            Log.d(LOG_TAG, "isFromSideMenu");
        }

        return mFromSideMenu;
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
        setContentView(R.layout.onboarding_external_call_activity);
        setBackgroundColor(Color.TRANSPARENT);

        applyInsets(R.id.onboarding_external_call_activity_layout, -1 , R.id.onboarding_external_call_activity_action_view, Color.TRANSPARENT, true);

        mOverlayView = findViewById(R.id.onboarding_external_call_activity_overlay_view);
        mActionView = findViewById(R.id.onboarding_external_call_activity_action_view);
        View slideMarkView = findViewById(R.id.onboarding_external_call_activity_slide_mark_view);

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

        TextView titleTextView = findViewById(R.id.onboarding_external_call_activity_title);
        Design.updateTextFont(titleTextView, Design.FONT_MEDIUM36);
        titleTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) titleTextView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_ONBOARDING_TOP_MARGIN * Design.HEIGHT_RATIO);

        OnboardingExternalCallAdapter onboardingExternalCallAdapter = new OnboardingExternalCallAdapter(this, mUIOnboarding);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);

        mRecyclerView = findViewById(R.id.onboarding_external_call_activity_recycler_view);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(onboardingExternalCallAdapter);
        mRecyclerView.setItemAnimator(null);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mRecyclerView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_ONBOARDING_TOP_MARGIN * Design.HEIGHT_RATIO);

        SnapHelper pagerSnapHelper = new PagerSnapHelper();
        pagerSnapHelper.attachToRecyclerView(mRecyclerView);

        RecyclerView dotsRecyclerView = findViewById(R.id.onboarding_external_call_activity_dots_view);
        mDotsAdapter = new DotsAdapter(mUIOnboarding.size(), getLayoutInflater());

        LinearLayoutManager dotsLinearLayoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        dotsRecyclerView.setLayoutManager(dotsLinearLayoutManager);
        dotsRecyclerView.setAdapter(mDotsAdapter);
        dotsRecyclerView.setItemAnimator(null);

        setupOnboarding(titleTextView.getLineHeight());

        layoutParams = dotsRecyclerView.getLayoutParams();
        layoutParams.width = mUIOnboarding.size() * Design.DOT_SIZE;
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

    private void initOnboarding() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initOnboarding");
        }

        mUIOnboarding.add(new UIOnboarding(this, UIOnboarding.OnboardingType.PART_ONE, true));
        mUIOnboarding.add(new UIOnboarding(this, UIOnboarding.OnboardingType.PART_TWO, true));
        mUIOnboarding.add(new UIOnboarding(this, UIOnboarding.OnboardingType.PART_THREE, true));
        mUIOnboarding.add(new UIOnboarding(this, UIOnboarding.OnboardingType.PART_FOUR, false));
    }

    private void setupOnboarding(int lineHeight) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setupOnboarding");
        }

        float maxRecyclerViewHeight = Design.DISPLAY_HEIGHT - (MIN_CONTENT_VIEW_HEIGHT * Design.HEIGHT_RATIO) - lineHeight - Design.DOT_SIZE;
        int textWidth = (int ) (Design.DISPLAY_WIDTH - (Design.ONBOARDING_TEXT_MARGIN * 2));

        float recyclerViewHeight = 0;
        float minContentCellHeight = mFromSideMenu ? (MIN_CONTENT_CELL_FROM_SIDE_MENU_HEIGHT * Design.HEIGHT_RATIO) : (MIN_CONTENT_CELL_HEIGHT * Design.HEIGHT_RATIO);

        for (UIOnboarding uiOnboarding : mUIOnboarding) {

            float height = uiOnboarding.getMessageHeight(textWidth);
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
