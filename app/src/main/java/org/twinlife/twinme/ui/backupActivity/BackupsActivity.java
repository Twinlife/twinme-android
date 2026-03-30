/*
 *  Copyright (c) 2025-2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Romain Kolb (romain.kolb@skyrock.com)
 */

package org.twinlife.twinme.ui.backupActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.BackupInfo;
import org.twinlife.twinlife.BaseService;
import org.twinlife.twinme.services.BackupService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.TwinmeApplication;
import org.twinlife.twinme.utils.AbstractBottomSheetView;
import org.twinlife.twinme.utils.DefaultConfirmView;
import org.twinlife.twinme.utils.OnboardingConfirmView;

import java.util.ArrayList;
import java.util.List;

public class BackupsActivity extends AbstractTwinmeActivity {
    private static final String LOG_TAG = "BackupsActivity";
    private static final boolean DEBUG = false;

    private static final int DESIGN_NO_BACKUP_IMAGE_MARGIN = 100;
    private static final int DESIGN_NO_BACKUP_IMAGE_VIEW_WIDTH = 440;
    private static final int DESIGN_NO_BACKUP_IMAGE_VIEW_HEIGHT = 440;
    private static final int DESIGN_NO_BACKUP_TEXT_VERTICAL_MARGIN = 60;
    private static final int DESIGN_NO_BACKUP_TEXT_HORIZONTAL_MARGIN = 40;
    private static final int DESIGN_NEW_BACKUP_MARGIN = 80;
    private static final int DESIGN_NEW_BACKUP_TEXT_MARGIN = 20;

    private View mNoBackupsView;
    private TextView mNoBackupTextView;
    private View mStartBackupView;
    private RecyclerView mBackupRecyclerView;
    private BackupsAdapter mBackupsAdapter;
    private boolean mIsGetBackupsDone = false;

    @NonNull
    private final List<UIBackupInfo> mBackupInfos = new ArrayList<>();

    @Nullable
    private Menu mMenu;

    @Nullable
    private BackupService mService;
    private boolean mBound = false;

    @NonNull
    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BackupService.LocalBinder binder = (BackupService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;

            mService.getAllBackups((errorCode, backupInfos) -> {
                if (errorCode == BaseService.ErrorCode.SUCCESS && backupInfos != null && !isDestroyed()) {
                    mIsGetBackupsDone = true;
                    for (BackupInfo backupInfo : backupInfos) {
                        addBackupInfo(backupInfo);
                    }
                    runOnUiThread(BackupsActivity.this::updateViews);
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };

    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        initViews();

        bindService(new Intent(this, BackupService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        super.onResume();

        updateViews();
    }

    @Override
    public void onApplyInsetsFinish() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        super.onApplyInsetsFinish();

        if (mStartBackupView != null) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mStartBackupView.getLayoutParams();
            marginLayoutParams.bottomMargin = (int) (DESIGN_NEW_BACKUP_MARGIN * Design.HEIGHT_RATIO) + getBarBottomInset();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateOptionsMenu: menu=" + menu);
        }

        super.onCreateOptionsMenu(menu);

        mMenu = menu;

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.backups_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.invalid_backups_action);

        ImageView imageView = (ImageView) menuItem.getActionView();

        if (imageView != null) {
            imageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.action_bar_delete, null));
            imageView.setPadding(Design.TOOLBAR_IMAGE_ITEM_PADDING, 0, Design.TOOLBAR_IMAGE_ITEM_PADDING, 0);
            imageView.setOnClickListener(view -> onInvalidBackupClick());
        }

        updateViews();

        return true;
    }

    //
    // Private methods
    //

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.backups_activity);

        setStatusBarColor();
        setToolBar(R.id.backups_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);

        setTitle(getString(R.string.account_activity_backup_list));
        setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        applyInsets(R.id.backups_activity_layout, R.id.backups_activity_tool_bar, R.id.backups_activity_list_view, Design.TOOLBAR_COLOR, false);

        View backgroundView = findViewById(R.id.backups_activity_layout);
        backgroundView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        mBackupsAdapter = new BackupsAdapter(this, mBackupInfos);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        mBackupRecyclerView = findViewById(R.id.backups_activity_list_view);
        mBackupRecyclerView.setLayoutManager(linearLayoutManager);
        mBackupRecyclerView.setAdapter(mBackupsAdapter);
        mBackupRecyclerView.setItemAnimator(null);
        mBackupRecyclerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        mNoBackupsView = findViewById(R.id.backups_activity_no_backups_view);
        mNoBackupsView.setVisibility(View.VISIBLE);
        mNoBackupsView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        ImageView noBackupImageView = findViewById(R.id.backups_activity_no_backups_image_view);

        ViewGroup.LayoutParams layoutParams = noBackupImageView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_NO_BACKUP_IMAGE_VIEW_WIDTH * Design.WIDTH_RATIO);
        layoutParams.height = (int) (DESIGN_NO_BACKUP_IMAGE_VIEW_HEIGHT * Design.HEIGHT_RATIO);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) noBackupImageView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_NO_BACKUP_IMAGE_MARGIN * Design.HEIGHT_RATIO);

        mNoBackupTextView = findViewById(R.id.backups_activity_no_backups_message_view);
        Design.updateTextFont(mNoBackupTextView, Design.FONT_MEDIUM34);
        mNoBackupTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mNoBackupTextView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_NO_BACKUP_TEXT_VERTICAL_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.leftMargin = (int) (DESIGN_NO_BACKUP_TEXT_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_NO_BACKUP_TEXT_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);

        mStartBackupView = findViewById(R.id.backups_activity_new_backup_view);
        mStartBackupView.setOnClickListener(view -> startBackup());

        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable newBackupViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        newBackupViewBackground.getPaint().setColor(Design.getMainStyle());
        mStartBackupView.setBackground(newBackupViewBackground);

        layoutParams = mStartBackupView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = Design.BUTTON_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mStartBackupView.getLayoutParams();
        marginLayoutParams.bottomMargin = (int) (DESIGN_NEW_BACKUP_MARGIN * Design.HEIGHT_RATIO);

        TextView newBackupTextView = findViewById(R.id.backups_activity_new_backup_text_view);
        Design.updateTextFont(newBackupTextView, Design.FONT_BOLD36);
        newBackupTextView.setTextColor(Color.WHITE);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) newBackupTextView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_NEW_BACKUP_TEXT_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_NEW_BACKUP_TEXT_MARGIN * Design.WIDTH_RATIO);
    }

    private void updateViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateViews");
        }

        if (mBackupInfos.isEmpty()) {
            mNoBackupsView.setVisibility(View.VISIBLE);
            mBackupRecyclerView.setVisibility(View.GONE);

            if (!mIsGetBackupsDone) {
                mStartBackupView.setVisibility(View.GONE);
                mNoBackupTextView.setText(getString(R.string.application_processing_please_wait));
            } else {
                mStartBackupView.setVisibility(View.VISIBLE);
                mNoBackupTextView.setText(getString(R.string.backups_activity_no_backup_message));
            }

            if (mMenu != null) {
                MenuItem menuItem = mMenu.findItem(R.id.invalid_backups_action);
                menuItem.setEnabled(false);
                if (menuItem.getActionView() != null) {
                    menuItem.getActionView().setAlpha(0.5f);
                }
            }
        } else {
            mNoBackupsView.setVisibility(View.GONE);
            mBackupRecyclerView.setVisibility(View.VISIBLE);

            if (mMenu != null) {
                MenuItem menuItem = mMenu.findItem(R.id.invalid_backups_action);
                menuItem.setEnabled(true);
                if (menuItem.getActionView() != null) {
                    menuItem.getActionView().setAlpha(1.0f);
                }
            }
        }

        mBackupsAdapter.setBackupInfos(mBackupInfos);
    }

    private void addBackupInfo(BackupInfo backupInfo) {
        if (DEBUG) {
            Log.d(LOG_TAG, "addBackupInfo: " + backupInfo);
        }

        UIBackupInfo uiBackupInfo = new UIBackupInfo(backupInfo.id, backupInfo.creationDate);

        boolean added = false;
        int size = mBackupInfos.size();
        for (int i = 0; i < size; i++) {
            UIBackupInfo backup = mBackupInfos.get(i);
            if (uiBackupInfo.getDate() > backup.getDate()) {
                mBackupInfos.add(i, uiBackupInfo);
                added = true;
                break;
            }
        }

        if (!added) {
            mBackupInfos.add(uiBackupInfo);
        }
    }

    private void onInvalidBackupClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onInvalidBackupClick");
        }

        if (mBackupInfos.isEmpty()) {
            return;
        }

        ViewGroup viewGroup = findViewById(R.id.backups_activity_layout);

        DefaultConfirmView defaultConfirmView = new DefaultConfirmView(this, null);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        defaultConfirmView.setLayoutParams(layoutParams);
        defaultConfirmView.setTitle(getString(R.string.backups_activity_invalidate_backup));
        String message = getString(R.string.backups_activity_invalidate_backup_message_part_one)
                + "\n\n" + getString(R.string.backups_activity_invalidate_backup_message_part_two) ;
        defaultConfirmView.setMessage(message);
        defaultConfirmView.setImage(null);
        defaultConfirmView.setConfirmTitle(getString(R.string.application_delete));
        defaultConfirmView.setConfirmColor(Design.DELETE_COLOR_RED);
        defaultConfirmView.setCancelTitle(getString(R.string.application_cancel));

        AbstractBottomSheetView.Observer observer = new AbstractBottomSheetView.Observer() {
            @Override
            public void onConfirmClick() {
                defaultConfirmView.animationCloseConfirmView();

                invalidBackups();
            }

            @Override
            public void onCancelClick() {
                defaultConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onDismissClick() {
                defaultConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                viewGroup.removeView(defaultConfirmView);
                setStatusBarColor();

                if (fromConfirmAction) {
                    invalidBackups();
                }
            }
        };

        defaultConfirmView.setObserver(observer);
        viewGroup.addView(defaultConfirmView);
        defaultConfirmView.show();

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }

    private void invalidBackups() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onInvalidBackupClick");
        }

        if (mBackupInfos.isEmpty()) {
            return;
        }

        if (mService == null) {
            Log.e(LOG_TAG, "BackupService not bound, can't delete backups");
            return;
        }

        mService.deleteBackups((errorCode, ignored) -> {
            if (errorCode == BaseService.ErrorCode.SUCCESS && !isDestroyed()) {
                mBackupInfos.clear();
                getTwinmeApplication().clearLastBackupDate();
                runOnUiThread(this::updateViews);
            }
        });
    }

    private void startBackup() {
        if (DEBUG) {
            Log.d(LOG_TAG, "startBackup");
        }

        if (getTwinmeApplication().startOnboarding(TwinmeApplication.OnboardingType.BACKUP)) {
            ViewGroup viewGroup = findViewById(R.id.backups_activity_layout);

            OnboardingConfirmView onboardingConfirmView = new OnboardingConfirmView(this, null);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            onboardingConfirmView.setLayoutParams(layoutParams);

            String title = getString(R.string.account_activity_backup);
            String message = getString(R.string.backup_activity_onboarding);
            String action = getString(R.string.backup_activity_backup);

            onboardingConfirmView.setTitle(title);
            onboardingConfirmView.setImage(ResourcesCompat.getDrawable(getResources(), R.drawable.onboarding_backup, null));
            onboardingConfirmView.setMessage(message);
            onboardingConfirmView.setConfirmTitle(action);
            onboardingConfirmView.setCancelTitle(getString(R.string.application_do_not_display));

            AbstractBottomSheetView.Observer observer = new AbstractBottomSheetView.Observer() {
                @Override
                public void onConfirmClick() {
                    onboardingConfirmView.animationCloseConfirmView();
                    startBackupIntent();
                }

                @Override
                public void onCancelClick() {
                    onboardingConfirmView.animationCloseConfirmView();
                    getTwinmeApplication().setShowOnboardingType(TwinmeApplication.OnboardingType.BACKUP, false);
                    startBackupIntent();
                }

                @Override
                public void onDismissClick() {
                    onboardingConfirmView.animationCloseConfirmView();
                }

                @Override
                public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                    viewGroup.removeView(onboardingConfirmView);
                    setStatusBarColor();
                }
            };
            onboardingConfirmView.setObserver(observer);
            viewGroup.addView(onboardingConfirmView);
            onboardingConfirmView.show();

            int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
            setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
        } else {
            startBackupIntent();
        }
    }

    private void startBackupIntent() {
        if (DEBUG) {
            Log.d(LOG_TAG, "startBackupIntent");
        }

        Intent intent = new Intent();
        intent.setClass(this, BackupActivity.class);
        startActivity(intent);
        finish();
    }
}