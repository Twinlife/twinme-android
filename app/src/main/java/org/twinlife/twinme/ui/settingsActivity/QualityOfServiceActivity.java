/*
 *  Copyright (c) 2022-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.settingsActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import org.twinlife.device.android.twinme.BuildConfig;
import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.AndroidDeviceInfo;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractOnboardingActivity;
import org.twinlife.twinme.utils.DotsAdapter;

import java.util.ArrayList;
import java.util.List;

public class QualityOfServiceActivity extends AbstractOnboardingActivity {
    private static final String LOG_TAG = "QualityOfServiceA...";
    private static final boolean DEBUG = false;

    private static final int MIN_CONTENT_VIEW_HEIGHT = 148;
    private static final int MIN_CONTENT_CELL_HEIGHT = 798;

    private QualityOfServiceAdapter mQualityOfServicesAdapter;
    private final List<UIQuality> mUiQualities = new ArrayList<>();

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

        mQualityOfServicesAdapter.notifyItemChanged(1);
        mDotsAdapter.setCurrentPosition(mCurrentPosition);
    }

    //
    // Override Activity methods
    //

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSaveInstanceState: outState=" + outState);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onWindowFocusChanged: hasFocus=" + hasFocus);
        }
    }

    public void onPermissionsClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPermissionsClick");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final AndroidDeviceInfo androidDeviceInfo = new AndroidDeviceInfo(this);

            boolean postNotificationEnable = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                postNotificationEnable = checkPermissionsWithoutRequest(new Permission[]{Permission.POST_NOTIFICATIONS});
            }

            // Order of checks must be the same as in RestrictionView.updateView().
            Intent intent = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !postNotificationEnable) {
                if (!checkPermissions(new Permission[]{Permission.POST_NOTIFICATIONS})) {
                    intent = new Intent();
                    intent.setAction(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                    intent.putExtra("android.provider.extra.APP_PACKAGE", getPackageName());
                }
            } else if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                intent = new Intent();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    intent.setAction(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                    intent.putExtra("android.provider.extra.APP_PACKAGE", getPackageName());
                } else {
                    intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                    intent.putExtra("app_package", getPackageName());
                    intent.putExtra("app_uid", getApplicationInfo().uid);
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && androidDeviceInfo.isNetworkRestricted()) {
                intent = new Intent(Settings.ACTION_IGNORE_BACKGROUND_DATA_RESTRICTIONS_SETTINGS);
                intent.setData(Uri.parse("package:" + BuildConfig.APPLICATION_ID));
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && androidDeviceInfo.isBackgroundRestricted()) {
                intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + BuildConfig.APPLICATION_ID));
            } else  if (!androidDeviceInfo.isIgnoringBatteryOptimizations()) {
                intent = new Intent();
                intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            }

            if (intent != null) {
                startActivity(intent);
            }
        }
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
        
        setContentView(R.layout.quality_of_service_activity);
        setBackgroundColor(Color.TRANSPARENT);

        applyInsets(R.id.quality_of_service_activity_layout, -1 , R.id.quality_of_service_activity_action_view, Color.TRANSPARENT, true);

        mOverlayView = findViewById(R.id.quality_of_service_activity_overlay_view);
        mActionView = findViewById(R.id.quality_of_service_activity_action_view);
        View slideMarkView = findViewById(R.id.quality_of_service_activity_slide_mark_view);

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

        TextView titleTextView = findViewById(R.id.quality_of_service_activity_title);
        Design.updateTextFont(titleTextView, Design.FONT_MEDIUM36);
        titleTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) titleTextView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_ONBOARDING_TOP_MARGIN * Design.HEIGHT_RATIO);

        mUiQualities.add(new UIQuality(this, UIQuality.QualityOfServicesStep.ONE));
        mUiQualities.add(new UIQuality(this, UIQuality.QualityOfServicesStep.TWO));
        mUiQualities.add(new UIQuality(this, UIQuality.QualityOfServicesStep.THREE));

        mQualityOfServicesAdapter = new QualityOfServiceAdapter(this, mUiQualities);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);

        mRecyclerView = findViewById(R.id.quality_of_service_activity_recycler_view);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mQualityOfServicesAdapter);
        mRecyclerView.setItemAnimator(null);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mRecyclerView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_ONBOARDING_TOP_MARGIN * Design.HEIGHT_RATIO);

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
                        mQualityOfServicesAdapter.notifyDataSetChanged();
                    }
                }
            }
        });

        setupQualityOfServices(titleTextView.getLineHeight());

        RecyclerView dotsRecyclerView = findViewById(R.id.quality_of_service_activity_dots_view);
        mDotsAdapter = new DotsAdapter(mUiQualities.size(), getLayoutInflater());
        LinearLayoutManager dotsLinearLayoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        dotsRecyclerView.setLayoutManager(dotsLinearLayoutManager);
        dotsRecyclerView.setAdapter(mDotsAdapter);
        dotsRecyclerView.setItemAnimator(null);

        layoutParams = dotsRecyclerView.getLayoutParams();
        layoutParams.width = mUiQualities.size() * Design.DOT_SIZE;

        dotsRecyclerView.setLayoutParams(layoutParams);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) dotsRecyclerView.getLayoutParams();
        marginLayoutParams.topMargin = Design.DOT_MARGIN;
        marginLayoutParams.bottomMargin = Design.DOT_MARGIN;

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

    private void setupQualityOfServices(int lineHeight) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setupQualityOfServices");
        }

        float maxRecyclerViewHeight = Design.DISPLAY_HEIGHT - (MIN_CONTENT_VIEW_HEIGHT * Design.HEIGHT_RATIO) - lineHeight - Design.DOT_SIZE;
        int textWidth = (int ) (Design.DISPLAY_WIDTH - (Design.ONBOARDING_TEXT_MARGIN * 2));

        float recyclerViewHeight = 0;
        float minContentCellHeight = MIN_CONTENT_CELL_HEIGHT * Design.HEIGHT_RATIO;

        for (UIQuality uiQuality : mUiQualities) {

            float height = uiQuality.getMessageHeight(textWidth);
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

    private void onDismissClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDismissClick");
        }

        animationClose();
    }
}