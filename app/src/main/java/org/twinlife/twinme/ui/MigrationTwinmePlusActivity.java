/*
 *  Copyright (c) 2021-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.mainActivity.MainActivity;
import org.twinlife.twinme.ui.premiumServicesActivity.PremiumServicesActivity;

public class MigrationTwinmePlusActivity extends AbstractTwinmeActivity {
    private static final String LOG_TAG = "MigrationTwinmePlus...";
    private static final boolean DEBUG = false;

    private static final float DESIGN_UPGRADE_WIDTH = 384f;
    private static final float DESIGN_UPGRADE_HEIGHT = 100f;

    private static final int DESIGN_MIGRATE_COLOR = Color.rgb(119, 138, 159);

    private boolean mHasConversations;
    private boolean mUpgradeFromSplashscreen;
    private boolean mFromSideMenu;

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

        setContentView(R.layout.migration_twinme_plus_activity);

        setStatusBarColor(Design.WHITE_COLOR);

        View passView = findViewById(R.id.migration_twinme_plus_activity_pass_view);
        passView.setOnClickListener(v -> onPassClick());

        ImageView closeView = findViewById(R.id.migration_twinme_plus_activity_close_view);
        closeView.setColorFilter(Design.BLACK_COLOR);

        TextView migrateTextView = findViewById(R.id.migration_twinme_plus_activity_migration_text_view);
        Design.updateTextFont(migrateTextView, Design.FONT_BOLD36);
        migrateTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        TextView accountTextView = findViewById(R.id.migration_twinme_plus_activity_migration_data_view);
        Design.updateTextFont(accountTextView, Design.FONT_REGULAR32);
        accountTextView.setTextColor(DESIGN_MIGRATE_COLOR);

        View upgradeView = findViewById(R.id.migration_twinme_plus_activity_upgrade_view);
        upgradeView.setOnClickListener(v -> onUpgradeClick());

        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable upgradeViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        upgradeViewBackground.getPaint().setColor(Design.getMainStyle());
        upgradeView.setBackground(upgradeViewBackground);

        ViewGroup.LayoutParams layoutParams = upgradeView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_UPGRADE_WIDTH * Design.WIDTH_RATIO);
        layoutParams.height = (int) (DESIGN_UPGRADE_HEIGHT * Design.HEIGHT_RATIO);

        TextView upgradeTextView = findViewById(R.id.migration_twinme_plus_activity_upgrade_title_view);
        Design.updateTextFont(upgradeTextView, Design.FONT_BOLD36);
        upgradeTextView.setTextColor(Color.WHITE);

        View doNotShowView = findViewById(R.id.migration_twinme_plus_activity_do_not_show_view);
        doNotShowView.setOnClickListener(v -> onDoNotShowClick());

        TextView doNotShowTextView = findViewById(R.id.migration_twinme_plus_activity_do_not_show_title_view);
        Design.updateTextFont(doNotShowTextView, Design.FONT_REGULAR24);
        doNotShowTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        doNotShowTextView.setPaintFlags(doNotShowTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        if (mFromSideMenu) {
            doNotShowView.setVisibility(View.GONE);
        }
    }

    private void onPassClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPassClick");
        }

        if (mUpgradeFromSplashscreen) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Intents.INTENT_HAS_CONVERSATIONS, mHasConversations);
            startActivity(intent);
        }

        finish();
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

    private void onUpgradeClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpgradeClick");
        }

        PremiumServicesActivity.redirectStoreUpgrade(this);
    }
}