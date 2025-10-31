/*
 *  Copyright (c) 2022-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.premiumServicesActivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.mainActivity.MainActivity;
import org.twinlife.twinme.utils.DotsAdapter;

import java.util.ArrayList;
import java.util.List;

public class PremiumServicesActivity extends AbstractTwinmeActivity {
    private static final String LOG_TAG = "PremiumServicesActivity";
    private static final boolean DEBUG = false;

    private static final float DESIGN_UPDATE_MARGIN = 56f;
    private static final float DESIGN_UPDATE_HEIGHT = 82f;
    private static final float DESIGN_DO_NOT_SHOW_HEIGHT = 68f;
    private static final float DESIGN_DO_NOT_SHOW_MARGIN = 16f;

    private static final float DESIGN_BOTTOM_HEIGHT = 188f;

    private static final float DESIGN_CLOSE_SIZE = 100f;
    private static final float DESIGN_CLOSE_MARGIN = 40f;

    private int mCurrentPosition = 0;
    private final List<UIPremiumFeature> mUIPremiumFeature = new ArrayList<>();

    private DotsAdapter mDotsAdapter;

    private boolean mHasConversations;
    private boolean mUpgradeFromSplashscreen;
    private boolean mFromSideMenu;

    public static void redirectStoreUpgrade(@NonNull Context context) {

        redirectStore(context, context.getString(R.string.twinme_plus_app_id));
    }

    public static void redirectStore(@NonNull Context context) {

        redirectStore(context, context.getString(R.string.twinme_app_id));
    }

    public static void redirectStore(@NonNull Context context, @NonNull String appId) {

        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appId)));
        } catch (android.content.ActivityNotFoundException anfe) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appId)));
        }
    }

    public static void redirectToPremiumServices(@NonNull Context context) {

        Intent intent = new Intent(context, PremiumServicesActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        mHasConversations = getIntent().getBooleanExtra(Intents.INTENT_HAS_CONVERSATIONS, false);
        mUpgradeFromSplashscreen = getIntent().getBooleanExtra(Intents.INTENT_UPGRADE_FROM_SPLASHSCREEN, false);
        mFromSideMenu = getIntent().getBooleanExtra(Intents.INTENT_FROM_SIDE_MENU, false);

        initViews();
    }

    @Override
    protected void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        super.onResume();
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

    //
    // Private methods
    //

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        setContentView(R.layout.premium_services_activity);

        setStatusBarColor(Color.BLACK);

        Window window = getWindow();
        window.setNavigationBarColor(Color.BLACK);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        applyInsets(R.id.premium_services_activity_layout, R.id.premium_services_activity_list_view , -1, Color.BLACK, true);

        initFeatures();

        PremiumFeatureAdapter premiumFeatureAdapter = new PremiumFeatureAdapter(this, mUIPremiumFeature);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);

        RecyclerView premiumFeatureRecyclerView = findViewById(R.id.premium_services_activity_list_view);
        premiumFeatureRecyclerView.setLayoutManager(linearLayoutManager);
        premiumFeatureRecyclerView.setAdapter(premiumFeatureAdapter);
        premiumFeatureRecyclerView.setItemAnimator(null);

        SnapHelper pagerSnapHelper = new PagerSnapHelper();
        pagerSnapHelper.attachToRecyclerView(premiumFeatureRecyclerView);

        premiumFeatureRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

        RecyclerView dotsRecyclerView = findViewById(R.id.premium_services_activity_dots_view);
        mDotsAdapter = new DotsAdapter(mUIPremiumFeature.size(), getLayoutInflater());
        LinearLayoutManager dotsLinearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        dotsRecyclerView.setLayoutManager(dotsLinearLayoutManager);
        dotsRecyclerView.setAdapter(mDotsAdapter);
        dotsRecyclerView.setItemAnimator(null);

        ViewGroup.LayoutParams layoutParams = dotsRecyclerView.getLayoutParams();
        layoutParams.width = Design.DOT_SIZE;
        layoutParams.height = mUIPremiumFeature.size() * Design.DOT_SIZE;
        dotsRecyclerView.setLayoutParams(layoutParams);

        View bottomView = findViewById(R.id.premium_services_activity_bottom_view);
        layoutParams = bottomView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_BOTTOM_HEIGHT * Design.HEIGHT_RATIO);

        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.mutate();
        gradientDrawable.setColors(new int[]{Design.BOTTOM_GRADIENT_START_COLOR, Design.BOTTOM_GRADIENT_END_COLOR});
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        bottomView.setBackground(gradientDrawable);

        View updateView = findViewById(R.id.premium_services_activity_update_view);
        updateView.setOnClickListener(v -> onUpdateClick());

        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable updateViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        updateViewBackground.getPaint().setColor(Design.getMainStyle());
        updateView.setBackground(updateViewBackground);

        layoutParams = updateView.getLayoutParams();
        layoutParams.width = (int) (Design.DISPLAY_WIDTH - (DESIGN_UPDATE_MARGIN * Design.HEIGHT_RATIO * 2));
        layoutParams.height = (int) (DESIGN_UPDATE_HEIGHT * Design.HEIGHT_RATIO);

        TextView updateTextView = findViewById(R.id.premium_services_activity_update_title_view);
        Design.updateTextFont(updateTextView, Design.FONT_BOLD36);
        updateTextView.setTextColor(Color.WHITE);

        View doNotShowView = findViewById(R.id.premium_services_activity_do_not_show_view);
        doNotShowView.setOnClickListener(v -> onDoNotShowClick());

        if (mFromSideMenu) {
            doNotShowView.setVisibility(View.GONE);
        }

        layoutParams = doNotShowView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_DO_NOT_SHOW_HEIGHT * Design.HEIGHT_RATIO);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) doNotShowView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_DO_NOT_SHOW_MARGIN * Design.HEIGHT_RATIO);

        TextView doNotShowTextView = findViewById(R.id.premium_services_activity_do_not_show_title_view);
        Design.updateTextFont(doNotShowTextView, Design.FONT_REGULAR24);
        doNotShowTextView.setTextColor(Color.WHITE);
        doNotShowTextView.setPaintFlags(doNotShowTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        View closeView = findViewById(R.id.premium_services_activity_close_view);
        closeView.setOnClickListener(v -> onCloseClick());

        layoutParams = closeView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_CLOSE_SIZE * Design.WIDTH_RATIO);
        layoutParams.height = (int) (DESIGN_CLOSE_SIZE * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) closeView.getLayoutParams();
        marginLayoutParams.rightMargin = (int) (DESIGN_CLOSE_MARGIN * Design.WIDTH_RATIO);

        ImageView closeImageView = findViewById(R.id.premium_services_activity_close_image_view);
        closeImageView.setColorFilter(Design.CLOSE_COLOR);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (DEBUG) {
                    Log.d(LOG_TAG, "handleOnBackPressed");
                }

                backPressed();
            }
        });
    }

    private void onUpdateClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateClick");
        }

        PremiumServicesActivity.redirectStoreUpgrade(this);
    }

    private void onDoNotShowClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDoNotShowClick");
        }

        if (mUpgradeFromSplashscreen) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Intents.INTENT_HAS_CONVERSATIONS, mHasConversations);
            startActivity(intent);
        }

        getTwinmeApplication().setDoNotShowUpgradeScreen();

        finish();
    }

    private void onCloseClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCloseClick");
        }

        if (mUpgradeFromSplashscreen) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Intents.INTENT_HAS_CONVERSATIONS, mHasConversations);
            startActivity(intent);
        }

        finish();
    }

    private void initFeatures() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initFeatures");
        }

        mUIPremiumFeature.clear();
        mUIPremiumFeature.add(new UIPremiumFeature(this, UIPremiumFeature.FeatureType.PRIVACY));
        mUIPremiumFeature.add(new UIPremiumFeature(this, UIPremiumFeature.FeatureType.SPACES));
        mUIPremiumFeature.add(new UIPremiumFeature(this, UIPremiumFeature.FeatureType.GROUP_CALL));
        mUIPremiumFeature.add(new UIPremiumFeature(this, UIPremiumFeature.FeatureType.STREAMING));
        mUIPremiumFeature.add(new UIPremiumFeature(this, UIPremiumFeature.FeatureType.TRANSFER_CALL));
        mUIPremiumFeature.add(new UIPremiumFeature(this, UIPremiumFeature.FeatureType.CLICK_TO_CALL));
        mUIPremiumFeature.add(new UIPremiumFeature(this, UIPremiumFeature.FeatureType.CONVERSATION));
        mUIPremiumFeature.add(new UIPremiumFeature(this, UIPremiumFeature.FeatureType.CAMERA_CONTROL));
    }

    private void backPressed() {
        if (DEBUG) {
            Log.d(LOG_TAG, "backPressed");
        }

        if (mUpgradeFromSplashscreen) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Intents.INTENT_HAS_CONVERSATIONS, mHasConversations);
            startActivity(intent);
        }

        finish();
    }
}
