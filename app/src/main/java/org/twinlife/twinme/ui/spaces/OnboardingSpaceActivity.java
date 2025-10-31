/*
 *  Copyright (c) 2023-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.spaces;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
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
import org.twinlife.twinme.utils.DotsAdapter;

public class OnboardingSpaceActivity extends AbstractOnboardingActivity {

    private static final String LOG_TAG = "OnboardingSpaceActivity";
    private static final boolean DEBUG = false;

    private static final int MIN_CONTENT_VIEW_HEIGHT = 148;
    private static final int MIN_CONTENT_FIRST_PART = 524;
    private static final int MIN_CONTENT_SECOND_PART = 380;
    private static final int MIN_CONTENT_THIRD_PART = 562;
    private static final int MIN_CONTENT_FROM_SIDE_MENU_THIRD_PART = 442;

    private OnboardingSpaceAdapter mOnboardingSpaceAdapter;

    private boolean mShowFirstPart = true;
    private boolean mFromSideMenu = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        mFromSideMenu = getIntent().getBooleanExtra(Intents.INTENT_FROM_SIDE_MENU, false);
        mShowFirstPart = getIntent().getBooleanExtra(Intents.INTENT_SHOW_FIRST_PART_ONBOARDING, true);

        super.onCreate(savedInstanceState);
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
        setContentView(R.layout.onboarding_space_activity);
        setBackgroundColor(Color.TRANSPARENT);

        applyInsets(R.id.onboarding_space_activity_layout, -1 , R.id.onboarding_space_activity_action_view, Color.TRANSPARENT, true);

        mOverlayView = findViewById(R.id.onboarding_space_activity_overlay_view);
        mActionView = findViewById(R.id.onboarding_space_activity_action_view);
        View slideMarkView = findViewById(R.id.onboarding_space_activity_slide_mark_view);

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

        TextView titleTextView = findViewById(R.id.onboarding_space_activity_title);
        Design.updateTextFont(titleTextView, Design.FONT_MEDIUM36);
        titleTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) titleTextView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_ONBOARDING_TOP_MARGIN * Design.HEIGHT_RATIO);

        mOnboardingSpaceAdapter = new OnboardingSpaceAdapter(this, mFromSideMenu);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);

        mRecyclerView = findViewById(R.id.onboarding_space_activity_recycler_view);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mOnboardingSpaceAdapter);
        mRecyclerView.setItemAnimator(null);

        SnapHelper pagerSnapHelper = new PagerSnapHelper();
        pagerSnapHelper.attachToRecyclerView(mRecyclerView);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    View centerView = pagerSnapHelper.findSnapView(linearLayoutManager);
                    if (centerView != null) {
                        mCurrentPosition = linearLayoutManager.getPosition(centerView);
                        mDotsAdapter.setCurrentPosition(mCurrentPosition);
                        linearLayoutManager.requestLayout();
                        mRecyclerView.requestLayout();
                        mOnboardingSpaceAdapter.notifyDataSetChanged();
                    }
                }
            }
        });

        int dotsCount = 2;
        if (mShowFirstPart) {
            dotsCount = 3;
        }

        RecyclerView dotsRecyclerView = findViewById(R.id.onboarding_space_activity_dots_view);
        mDotsAdapter = new DotsAdapter(dotsCount, getLayoutInflater());
        LinearLayoutManager dotsLinearLayoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        dotsRecyclerView.setLayoutManager(dotsLinearLayoutManager);
        dotsRecyclerView.setAdapter(mDotsAdapter);
        dotsRecyclerView.setItemAnimator(null);

        layoutParams = dotsRecyclerView.getLayoutParams();
        layoutParams.width = dotsCount * Design.DOT_SIZE;

        dotsRecyclerView.setLayoutParams(layoutParams);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) dotsRecyclerView.getLayoutParams();
        marginLayoutParams.topMargin = Design.DOT_MARGIN;
        marginLayoutParams.bottomMargin = Design.DOT_MARGIN;

        setupOnboarding(titleTextView.getLineHeight());

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

    private void setupOnboarding(int lineHeight) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setupOnboarding");
        }

        float maxRecyclerViewHeight = Design.DISPLAY_HEIGHT - (MIN_CONTENT_VIEW_HEIGHT * Design.HEIGHT_RATIO) - lineHeight - Design.DOT_SIZE;

        float recyclerViewHeight = 0;

        float firstPartContentHeight = MIN_CONTENT_FIRST_PART * Design.HEIGHT_RATIO;
        float secondPartContentHeight = MIN_CONTENT_SECOND_PART * Design.HEIGHT_RATIO;
        float thirdPartContentHeight = mFromSideMenu ? MIN_CONTENT_FROM_SIDE_MENU_THIRD_PART * Design.HEIGHT_RATIO : MIN_CONTENT_THIRD_PART * Design.HEIGHT_RATIO;

        String messageFirstPart = getString(R.string.spaces_activity_message);

        String messageSecondPart = getString(R.string.create_space_activity_onboarding_message_part_1) +
                "\n\n" +
                getString(R.string.create_space_activity_onboarding_message_part_2) +
                "\n\n" +
                getString(R.string.create_space_activity_onboarding_message_part_3);

        String messageThirdPart = getString(R.string.create_space_activity_onboarding_message_part_4) +
                "\n\n" +
                getString(R.string.create_space_activity_onboarding_message_part_5) +
                "\n\n" +
                getString(R.string.create_space_activity_onboarding_message_part_6) +
                "\n\n" +
                getString(R.string.create_space_activity_onboarding_message_part_7) +
                "\n\n" +
                getString(R.string.create_space_activity_onboarding_message_part_8);

        firstPartContentHeight += getMessageHeight(messageFirstPart);
        secondPartContentHeight += getMessageHeight(messageSecondPart);
        thirdPartContentHeight += getMessageHeight(messageThirdPart);

        recyclerViewHeight = Math.max(firstPartContentHeight, secondPartContentHeight);
        recyclerViewHeight = Math.max(recyclerViewHeight, thirdPartContentHeight);

        if (recyclerViewHeight > maxRecyclerViewHeight) {
            recyclerViewHeight = maxRecyclerViewHeight;
        }

        ViewGroup.LayoutParams layoutParams = mRecyclerView.getLayoutParams();
        layoutParams.height = (int) recyclerViewHeight;
    }

    private void onDismissClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDismissClick");
        }

        animationClose();
    }

    public void onDoNotShowAgainClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDoNotShowAgainClick");
        }

    }

    public void onCreateSpaceClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateSpaceClick");
        }

        if (mFromSideMenu) {
            animationClose();
        } else {
            finish();
        }
    }

    public float getMessageHeight(String message) {

        if (message == null) {
            return 0;
        }

        TextPaint textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(Design.FONT_MEDIUM32.size);
        textPaint.setTypeface(Design.FONT_MEDIUM32.typeface);

        int textWidth = (int) (Design.DISPLAY_WIDTH - (Design.ONBOARDING_TEXT_MARGIN * 2));
        Layout.Alignment alignment = Layout.Alignment.ALIGN_NORMAL;
        StaticLayout staticLayout = new StaticLayout(message, textPaint, textWidth, alignment, 1, 0, false);
        return staticLayout.getHeight();
    }
}
