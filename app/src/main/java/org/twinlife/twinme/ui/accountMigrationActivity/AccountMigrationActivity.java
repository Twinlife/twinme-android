/*
 *  Copyright (c) 2020-2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.accountMigrationActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.AccountMigrationService.QueryInfo;
import org.twinlife.twinlife.AccountMigrationService.State;
import org.twinlife.twinlife.AccountMigrationService.Status;
import org.twinlife.twinlife.ConnectionStatus;
import org.twinlife.twinlife.util.Logger;
import org.twinlife.twinme.TwinmeContext;
import org.twinlife.twinme.services.AccountMigrationService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.SplashScreenActivity;
import org.twinlife.twinme.ui.TwinmeApplication;
import org.twinlife.twinme.utils.AbstractBottomSheetView;
import org.twinlife.twinme.utils.AlertMessageView;
import org.twinlife.twinme.utils.DefaultConfirmView;
import org.twinlife.twinme.utils.TwinmeImmersiveActivityImpl;

import java.util.UUID;

public class AccountMigrationActivity extends TwinmeImmersiveActivityImpl {
    private static final String LOG_TAG = "AccountMigrationAct...";
    private static final boolean DEBUG = false;

    private static final long CLOSE_ACTIVITY_TIMEOUT = 5 * 1000; // 5s

    private static final float DESIGN_GRADIENT_HEIGHT = 40;
    private static final float DESIGN_TITLE_TOP_MARGIN = 100;
    private static final float DESIGN_IMAGE_TOP_MARGIN = 40;
    private static final float DESIGN_IMAGE_BOTTOM_MARGIN = 20;
    private static final float DESIGN_IMAGE_HEIGHT = 320;
    private static final float DESIGN_CONTAINER_WIDTH = 640f;
    private static final float DESIGN_VERTICAL_MARGIN = 40f;
    private static final float DESIGN_CANCEL_MARGIN = 60f;
    private static final float DESIGN_DECLINE_HEIGHT = 120f;
    private static final float DESIGN_DECLINE_MARGIN = 30f;

    protected class CancelListener implements View.OnClickListener {

        private boolean disabled = false;

        @Override
        public void onClick(View view) {
            if (DEBUG) {
                Log.d(LOG_TAG, "CancelListener.onClick: view=" + view);
            }

            if (disabled) {

                return;
            }
            disabled = true;

            onCancelClick();
        }

        void enable() {

            disabled = false;
        }
    }

    protected class AcceptListener implements View.OnClickListener {

        private boolean disabled = true;

        @Override
        public void onClick(View view) {
            if (DEBUG) {
                Log.d(LOG_TAG, "AcceptListener.onClick: view=" + view);
            }

            if (disabled) {

                return;
            }
            disabled = true;

            onAcceptClick();
        }

        void enable() {

            disabled = false;
        }

        void disable() {

            disabled = true;
        }
    }

    private class MigrationServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent == null || intent.getExtras() == null) {

                return;
            }

            final String event = intent.getExtras().getString(AccountMigrationService.MIGRATION_SERVICE_EVENT);
            if (event == null) {
                return;

            }

            // Catch exception in case an external app succeeds in sending a message.
            try {
                if (DEBUG) {
                    Log.d(LOG_TAG, "Received event=" + event);
                }
                switch (event) {
                    case AccountMigrationService.MESSAGE_STATE:
                        AccountMigrationActivity.this.onMessageState(intent);
                        break;

                    case AccountMigrationService.MESSAGE_ERROR:
                        // Error occurred, CallService is stopping.
                        AccountMigrationActivity.this.onMessageError(intent);
                        break;

                    default:
                        Log.w(LOG_TAG, "Migration event " + event + " not handled");
                        break;
                }
            } catch (Exception exception) {
                if (Logger.WARN) {
                    Log.w(LOG_TAG, "Invalid message", exception);
                }
            }
        }
    }

    private View mCancelButton;
    private View mStartView;
    private View mDeclineView;
    private View mContentView;
    private View mContainerRecyclerView;
    private TextView mInfoView;
    private TextView mCancelTextView;
    private RecyclerView mMigrationRecyclerView;
    private DefaultConfirmView mMigrationCancelView;
    private AccountMigrationAdapter mMigrationAdapter;

    protected ProgressBar mProgressBarView;

    private MigrationServiceReceiver mMigrationReceiver;
    @Nullable
    private UUID mAccountMigrationPeerTwincodeId;
    private State mState = State.STARTING;
    private long mStartTime;
    private CancelListener mCancelListener;
    private AcceptListener mAcceptListener;
    private long mRemain;
    private long mSent;
    private long mReceived;
    private boolean mNeedRestart = false;
    private boolean mCanceled = false;
    private boolean mIsConnected = false;
    private boolean mIsAlertMessage = false;

    private UIMigrationStateItem mHeaderItem;

    private final Runnable terminateRunnable = this::terminateActivity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Intent intent = getIntent();
        UUID deviceMigrationId = (UUID) intent.getSerializableExtra(Intents.INTENT_ACCOUNT_MIGRATION_ID);
        if (deviceMigrationId == null) {
            deviceMigrationId = getTwinmeContext().getAccountMigrationService().getActiveDeviceMigrationId();
        }

        mAccountMigrationPeerTwincodeId = (UUID) intent.getSerializableExtra(Intents.INTENT_ACCOUNT_MIGRATION_TWINCODE);

        // Listen to the MigrationService messages.
        IntentFilter filter = new IntentFilter(Intents.INTENT_MIGRATION_SERVICE_MESSAGE);
        mMigrationReceiver = new MigrationServiceReceiver();

        // Register and avoid exporting the export receiver.
        ContextCompat.registerReceiver(getBaseContext(), mMigrationReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);

        mHeaderItem = new UIMigrationStateItem(this, UIMigrationStateItem.MigrationStateItemType.HEADER);
        initViews();

        intent = new Intent(this, AccountMigrationService.class);
        if (deviceMigrationId != null) {
            intent.putExtra(AccountMigrationService.PARAM_ACCOUNT_MIGRATION_ID, deviceMigrationId.toString());
            intent.setAction(AccountMigrationService.ACTION_OUTGOING_MIGRATION);
        } else {
            intent.setAction(AccountMigrationService.ACTION_STATE_MIGRATION);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    @Override
    protected void onPause() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPause");
        }

        super.onPause();

        if (mNeedRestart) {
            Log.e(LOG_TAG, "Restarting application after migration");
        }
    }

    @Override
    protected void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        super.onResume();

        if (!mIsConnected) {
            showNetworkDisconnect(R.string.account_view_migration_title, () -> {});
        }
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        unregisterReceiver(mMigrationReceiver);

        super.onDestroy();
    }

    @Override
    public void onConnectionStatusChange(@NonNull ConnectionStatus connectionStatus) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onConnect");
        }

        super.onConnectionStatusChange(connectionStatus);
    }

    private void onAcceptClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onAcceptClick");
        }

        mStartView.setVisibility(View.GONE);
        mDeclineView.setVisibility(View.GONE);
        updateRecyclerViewLayout();

        Intent intent = new Intent(this, AccountMigrationService.class);
        intent.setAction(AccountMigrationService.ACTION_START_MIGRATION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        mAcceptListener.enable();
    }

    private void onCancelConfirmedClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCancelConfirmedClick");
        }

        mStartView.setVisibility(View.GONE);
        mDeclineView.setVisibility(View.GONE);
        updateRecyclerViewLayout();

        Intent intent = new Intent(this, AccountMigrationService.class);
        intent.setAction(AccountMigrationService.ACTION_CANCEL_MIGRATION);
        startService(intent);
        mCanceled = true;
        mCancelListener.enable();
    }

    private void onCancelClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCancelClick");
        }

        // If the migration is canceled or terminated, we can stop if the cancel button is clicked.
        if (mState == State.TERMINATED || mState == State.CANCELED || mState == State.STOPPED) {

            finish();
            return;
        }

        // If the account migration terminated with an error, we must execute the ACTION_CANCEL_MIGRATION
        // to stop the service and delete the AccountMigration object with its twincode (if we don't do this
        // leaving and starting again the application will re-open the account migration activity).
        if (mState == State.ERROR) {
            onCancelConfirmedClick();
            return;
        }

        ViewGroup viewGroup = findViewById(R.id.account_migration_activity_layout);

        mMigrationCancelView = new DefaultConfirmView(this, null);
        mMigrationCancelView.setTitle(getString(org.twinlife.twinme.android.R.string.deleted_account_view_warning));
        mMigrationCancelView.setMessage(getString(R.string.account_migration_view_confirm_cancel_message));
        mMigrationCancelView.setConfirmColor(Design.DELETE_COLOR_RED);
        mMigrationCancelView.setConfirmTitle(getString(R.string.account_migration_view_stop));

        AbstractBottomSheetView.Observer observer = new AbstractBottomSheetView.Observer() {
            @Override
            public void onConfirmClick() {
                mMigrationCancelView.animationCloseConfirmView();
            }

            @Override
            public void onCancelClick() {
                mMigrationCancelView.animationCloseConfirmView();
            }

            @Override
            public void onDismissClick() {
                mMigrationCancelView.animationCloseConfirmView();
            }

            @Override
            public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                viewGroup.removeView(mMigrationCancelView);
                setStatusBarColor();

                if (fromConfirmAction) {
                    onCancelConfirmedClick();
                } else {
                    mCancelListener.enable();
                }
            }
        };

        mMigrationCancelView.setObserver(observer);
        viewGroup.addView(mMigrationCancelView);
        mMigrationCancelView.show();

        Window window = getWindow();
        window.setNavigationBarColor(Design.POPUP_BACKGROUND_COLOR);
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void onMessageState(@NonNull Intent intent) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onMessageState: intent=" + intent);
        }

        if (mNeedRestart) {
            return;
        }

        // Check for Twinlife server connectivity.
        final TwinmeContext twinmeContext = getTwinmeContext();
        if (twinmeContext.isConnected() != mIsConnected) {
            mIsConnected = twinmeContext.isConnected();
            if (mIsConnected) {
                onConnectionStatusChange(ConnectionStatus.CONNECTED);
            } else {
                onConnectionStatusChange(ConnectionStatus.NO_SERVICE);
            }
        }

        long startTime = intent.getLongExtra(AccountMigrationService.MIGRATION_START_TIME, 0);

        State state = (State) intent.getSerializableExtra(AccountMigrationService.MIGRATION_SERVICE_STATE);
        Status migrationStatus = (Status) intent.getSerializableExtra(AccountMigrationService.MIGRATION_STATUS);

        if (state != mState) {
            // Ignore the stopped state: we cannot proceed and must remain in the terminated/canceled state.
            if (state == State.STOPPED && mState != State.TERMINATED) {
                return;
            }

            mState = state;

            // If the CallService does not have a state, it means there is no migration in progress because it was finished.
            // It happens if the current activity is called with an intent that refers to a past incoming/outgoing migration.
            if (mState == null) {
                updateViews(migrationStatus);
                return;
            }

            if (mState == State.STOPPED || mState == State.TERMINATED || mState == State.CANCELED || mState == State.ERROR) {
                mNeedRestart = mState == State.STOPPED;
                updateViews(migrationStatus);
                return;
            }
        }

        if (mHeaderItem != null) {
            mHeaderItem.update(this, mState, migrationStatus);
            updateInfo();
        }

        mMigrationAdapter.updateItems(mState, migrationStatus);

        if (mStartTime == 0 && startTime != 0) {
            mStartTime = startTime;
            mStartView.setVisibility(View.GONE);
            mDeclineView.setVisibility(View.GONE);
            mCancelButton.setVisibility(View.VISIBLE);
            updateRecyclerViewLayout();
        }

        QueryInfo peerQueryStats = (QueryInfo) intent.getSerializableExtra(AccountMigrationService.MIGRATION_PEER_QUERY_INFO);
        QueryInfo localQueryStats = (QueryInfo) intent.getSerializableExtra(AccountMigrationService.MIGRATION_LOCAL_QUERY_INFO);
        if (migrationStatus == null) {
            return;
        }

        // Handle the accept button: disable it if we are disconnected and enable it once we are connected.
        if (!migrationStatus.isConnected()) {
            mAcceptListener.disable();
            mStartView.setAlpha(0.5f);
        } else if (mState == State.NEGOTIATE) {
            if (mAccountMigrationPeerTwincodeId != null) {
                onAcceptClick();
            } else if (mAcceptListener.disabled) {
                mAcceptListener.enable();
                mStartView.setAlpha(1.0f);
            }
        }

        // Check there is enough space on both devices.
        if (peerQueryStats != null && localQueryStats != null) {
            String message = null;

            if (peerQueryStats.getDatabaseFileSize() >= localQueryStats.getDatabaseAvailableSpace()) {
                message = getResources().getString(R.string.account_migration_view_not_enough_space_to_receive);
            }
            if (localQueryStats.getDatabaseFileSize() >= peerQueryStats.getDatabaseAvailableSpace()) {
                message = getResources().getString(R.string.account_migration_view_not_enough_space_to_upload);
            }
            if (peerQueryStats.getTotalFileSize() >= localQueryStats.getFilesystemAvailableSpace()) {
                message = getResources().getString(R.string.account_migration_view_not_enough_space_for_files);
            }
            if (localQueryStats.getTotalFileSize() >= peerQueryStats.getFilesystemAvailableSpace()) {
                message = getResources().getString(R.string.account_migration_view_not_enough_space_for_files);
            }

            if (message != null && !mIsAlertMessage) {
                mIsAlertMessage = true;

                ViewGroup viewGroup = findViewById(R.id.account_migration_activity_layout);

                AlertMessageView alertMessageView = new AlertMessageView(this, null);
                alertMessageView.setMessage(message);

                AlertMessageView.Observer observer = new AlertMessageView.Observer() {

                    @Override
                    public void onConfirmClick() {
                        alertMessageView.animationCloseConfirmView();
                    }

                    @Override
                    public void onDismissClick() {
                        alertMessageView.animationCloseConfirmView();
                    }

                    @Override
                    public void onCloseViewAnimationEnd() {
                        viewGroup.removeView(alertMessageView);
                        mIsAlertMessage = false;
                        setStatusBarColor();
                        onCancelConfirmedClick();
                    }
                };
                alertMessageView.setObserver(observer);

                viewGroup.addView(alertMessageView);
                alertMessageView.show();

                int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
                setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
            }
        }

        long sent = migrationStatus.getBytesSent();
        long sentRemain = migrationStatus.getEstimatedBytesRemainSend();
        long received = migrationStatus.getBytesReceived();

        if (sentRemain != mRemain) {
            mRemain = sentRemain;
        }
        if (sent != mSent) {
            mSent = sent;
        }
        if (received != mReceived) {
            mReceived = received;
        }
    }

    private void onMessageError(@NonNull Intent intent) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onMessageError: intent=" + intent);
        }

        terminateActivity();
    }

    //
    // Private methods
    //

    protected void setToolBar(int toolbarId) {

        Toolbar toolbar = findViewById(toolbarId);

        if (toolbar != null) {

            toolbar.setTitle("");
            toolbar.setBackgroundColor(Design.TOOLBAR_COLOR);

            setSupportActionBar(toolbar);

            TextView titleView = findViewById(R.id.toolbar_title);
            if (titleView != null) {
                Design.updateTextFont(titleView, Design.FONT_BOLD34);
                titleView.setTextColor(Color.WHITE);
            }

            TextView subTitleView = findViewById(R.id.toolbar_subtitle);
            if (subTitleView != null) {
                Design.updateTextFont(subTitleView, Design.FONT_REGULAR24);
                subTitleView.setTextColor(Color.WHITE);
                subTitleView.setVisibility(View.GONE);
            }
        }
    }

    public void applyInsets(int rootLayout, int toolBarLayout, int bottomLayout, int backgroundColor, boolean isFullScreen) {
        if (DEBUG) {
            Log.d(LOG_TAG, "applyInsets rootLayout=" + rootLayout + " bottomLayout=" + bottomLayout + " backgroundColor=" + backgroundColor);
        }

        View rootView = findViewById(rootLayout);

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                            | WindowInsetsCompat.Type.displayCutout()
                            | WindowInsetsCompat.Type.ime()
            );

            v.setBackgroundColor(backgroundColor);

            int topPadding = isFullScreen ? 0 : bars.top;
            int bottomPadding = bars.bottom;

            if (toolBarLayout != -1) {
                topPadding = 0;
                View toolBarView = findViewById(toolBarLayout);
                if (toolBarView != null) {
                    ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams)toolBarView.getLayoutParams();
                    marginLayoutParams.topMargin = bars.top;
                }
            }

            if (bottomLayout != -1) {
                bottomPadding = 0;
                View bottomView = findViewById(bottomLayout);
                if (bottomView != null) {
                    bottomView.setPadding(0, 0, 0, bars.bottom);
                }
            }

            v.setPadding(bars.left, topPadding, bars.right, bottomPadding);

            return WindowInsetsCompat.CONSUMED;
        });
    }

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        setActivityTheme(getTwinmeApplication());
        setContentView(R.layout.account_migration_activity);

        setStatusBarColor();
        showToolBar(true);
        setTitle(getString(R.string.account_view_migration_title));
        setBackgroundColor(Design.WHITE_COLOR);

        applyInsets(R.id.account_migration_activity_layout, -1, R.id.account_migration_activity_container_view, Design.TOOLBAR_COLOR, true);

        View containerView = findViewById(R.id.account_migration_activity_container_view);
        containerView.setBackgroundColor(Design.WHITE_COLOR);

        mContentView = findViewById(android.R.id.content).getRootView();

        TextView titleView = findViewById(R.id.account_migration_activity_title_view);
        Design.updateTextFont(titleView, Design.FONT_BOLD34);
        titleView.setTextColor(Design.BLACK_COLOR);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) titleView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_TITLE_TOP_MARGIN * Design.HEIGHT_RATIO);

        ImageView imageView = findViewById(R.id.account_migration_activity_image_view);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) imageView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_IMAGE_TOP_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_IMAGE_BOTTOM_MARGIN * Design.HEIGHT_RATIO);

        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_IMAGE_HEIGHT * Design.HEIGHT_RATIO);

        mInfoView = findViewById(R.id.account_migration_activity_info_view);
        Design.updateTextFont(mInfoView, Design.FONT_MEDIUM32);
        mInfoView.setTextColor(Design.BLACK_COLOR);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mInfoView.getLayoutParams();
        marginLayoutParams.leftMargin = Design.TEXT_MARGIN;
        marginLayoutParams.rightMargin = Design.TEXT_MARGIN;

        View containerStateView = findViewById(R.id.account_migration_activity_container_recycler_view);
        mContainerRecyclerView = containerStateView;
        containerStateView.setBackgroundColor(Design.WHITE_COLOR);

        ViewGroup.LayoutParams containerLayoutParams = containerStateView.getLayoutParams();
        containerLayoutParams.width = (int) (DESIGN_CONTAINER_WIDTH * Design.WIDTH_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) containerStateView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_VERTICAL_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_VERTICAL_MARGIN * Design.HEIGHT_RATIO);

        View fadeTopView = findViewById(R.id.account_migration_activity_recycler_header_view);

        layoutParams = fadeTopView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_GRADIENT_HEIGHT * Design.HEIGHT_RATIO);

        GradientDrawable topGradient = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Design.WHITE_COLOR, Color.TRANSPARENT});
        fadeTopView.setBackground(topGradient);

        View fadeBottomView = findViewById(R.id.account_migration_activity_recycler_footer_view);
        layoutParams = fadeBottomView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_GRADIENT_HEIGHT * Design.HEIGHT_RATIO);

        GradientDrawable bottomGradient = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.TRANSPARENT, Design.WHITE_COLOR});
        fadeBottomView.setBackground(bottomGradient);

        mMigrationAdapter = new AccountMigrationAdapter(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        mMigrationRecyclerView = findViewById(R.id.account_migration_activity_recycler_view);
        mMigrationRecyclerView.setLayoutManager(linearLayoutManager);
        mMigrationRecyclerView.setAdapter(mMigrationAdapter);
        mMigrationRecyclerView.setItemAnimator(null);
        mMigrationRecyclerView.setBackgroundColor(Color.TRANSPARENT);

        // Accept button cannot be selected until we are connected.
        mAcceptListener = new AcceptListener();
        mStartView = findViewById(R.id.account_migration_activity_accept_view);
        mStartView.setOnClickListener(mAcceptListener);
        mStartView.setAlpha(0.5f);

        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable migrateViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        migrateViewBackground.getPaint().setColor(Design.getMainStyle());
        mStartView.setBackground(migrateViewBackground);

        layoutParams = mStartView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = Design.BUTTON_HEIGHT;

        TextView startTextView = findViewById(R.id.account_migration_activity_accept_title_view);
        Design.updateTextFont(startTextView, Design.FONT_MEDIUM34);
        startTextView.setTextColor(Color.WHITE);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) startTextView.getLayoutParams();
        marginLayoutParams.leftMargin = Design.BUTTON_MARGIN;
        marginLayoutParams.rightMargin = Design.BUTTON_MARGIN;

        mDeclineView = findViewById(R.id.account_migration_activity_decline_view);

        layoutParams = mDeclineView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_DECLINE_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mDeclineView.getLayoutParams();
        marginLayoutParams.bottomMargin = (int) (DESIGN_DECLINE_MARGIN * Design.HEIGHT_RATIO);

        mCancelListener = new CancelListener();
        mDeclineView.setOnClickListener(mCancelListener);

        TextView declineTextView = findViewById(R.id.account_migration_activity_decline_title_view);
        Design.updateTextFont(declineTextView, Design.FONT_MEDIUM34);
        declineTextView.setTextColor(Color.RED);

        // Cancel button can be selected at any time.
        mCancelButton = findViewById(R.id.account_migration_activity_cancel_view);
        mCancelButton.setOnClickListener(v -> onCancelClick());

        ShapeDrawable cancelViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        cancelViewBackground.getPaint().setColor(Design.BLACK_COLOR);
        mCancelButton.setBackground(cancelViewBackground);

        layoutParams = mCancelButton.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = Design.BUTTON_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mCancelButton.getLayoutParams();
        marginLayoutParams.bottomMargin = (int) (DESIGN_CANCEL_MARGIN * Design.HEIGHT_RATIO);

        mCancelTextView = findViewById(R.id.account_migration_activity_cancel_title_view);
        Design.updateTextFont(mCancelTextView, Design.FONT_BOLD28);
        mCancelTextView.setTextColor(Design.WHITE_COLOR);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mCancelTextView.getLayoutParams();
        marginLayoutParams.leftMargin = Design.TEXT_MARGIN;
        marginLayoutParams.rightMargin = Design.TEXT_MARGIN;

        if (mAccountMigrationPeerTwincodeId != null) {
            mStartView.setVisibility(View.GONE);
            mDeclineView.setVisibility(View.GONE);
        }
        updateRecyclerViewLayout();

        mProgressBarView = findViewById(R.id.account_migration_activity_progress_bar);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                backPressed();
            }
        });
    }

    private void terminateActivity() {
        if (DEBUG) {
            Log.d(LOG_TAG, "terminateActivity");
        }

        // Make sure we redirect to the main screen only once.
        mMigrationRecyclerView.removeCallbacks(terminateRunnable);
        if (mAccountMigrationPeerTwincodeId != null) {
            setResult(mCanceled || mState == State.CANCELED ? Activity.RESULT_CANCELED : Activity.RESULT_OK);
            finish();

        } else {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            intent.setClass(this, SplashScreenActivity.class);
            startActivity(intent);

            finish();

            // Stop the twinme service to stop the application and force a restart.
            // The above startActivity() is necessary to tell the system the current activity is the SplashScreen.
            // Otherwise, it will restart the application with the AccountActivity.
            if (mNeedRestart) {
                TwinmeApplication twinmeApplication = (TwinmeApplication) getApplication();
                twinmeApplication.hideWelcomeScreen();
                twinmeApplication.stop();
            }
        }
    }

    private void updateViews(Status migrationStatus) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateViews");
        }

        if (mMigrationCancelView != null && (mState == State.CANCELED || mState == State.STOPPED || mState == State.TERMINATED || mState == State.ERROR)) {
            mMigrationCancelView.animationCloseConfirmView();
        }

        if (mState == null || mState == State.CANCELED) {
            mStartView.setVisibility(View.GONE);
            mDeclineView.setVisibility(View.GONE);
            if (mState == State.CANCELED) {
                mCancelButton.setVisibility(View.GONE);
                mCancelTextView.setText(getString(org.twinlife.twinme.android.R.string.application_cancel));
                mMigrationRecyclerView.postDelayed(terminateRunnable, CLOSE_ACTIVITY_TIMEOUT);
                mContentView.setOnClickListener(view -> terminateActivity());
            } else {
                mCancelButton.setVisibility(View.VISIBLE);
            }
            if (mCanceled) {
                terminateActivity();
            }
        } else if (mState == State.ERROR) {
            mStartView.setVisibility(View.GONE);
            mDeclineView.setVisibility(View.GONE);
            mCancelButton.setVisibility(View.VISIBLE);
        } else if (mState == State.STOPPED || mState == State.TERMINATED) {
            mStartView.setVisibility(View.GONE);
            mDeclineView.setVisibility(View.GONE);
            mCancelButton.setVisibility(View.GONE);
            if (mState == State.STOPPED) {
                mContentView.setOnClickListener(view -> terminateActivity());
                mMigrationRecyclerView.postDelayed(terminateRunnable, CLOSE_ACTIVITY_TIMEOUT);
            }
        }
        updateRecyclerViewLayout();
    }

    private void updateRecyclerViewLayout() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateRecyclerViewLayout");
        }

        final RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mContainerRecyclerView.getLayoutParams();
        if (mStartView.getVisibility() == View.VISIBLE) {
            layoutParams.addRule(RelativeLayout.ABOVE, R.id.account_migration_activity_accept_view);
        } else if (mCancelButton.getVisibility() == View.VISIBLE) {
            layoutParams.addRule(RelativeLayout.ABOVE, R.id.account_migration_activity_cancel_view);
        } else {
            layoutParams.removeRule(RelativeLayout.ABOVE);
        }
        mContainerRecyclerView.setLayoutParams(layoutParams);
    }

    private void updateInfo() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateInfo");
        }

        if (mHeaderItem == null) {
            return;
        }

        if (mHeaderItem.getInfo() != null && !mHeaderItem.getInfo().isEmpty()) {
            Design.updateTextFont(mInfoView, Design.FONT_MEDIUM32);
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
            spannableStringBuilder.append(mHeaderItem.getTitle());
            spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_DEFAULT), 0, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableStringBuilder.append("\n");
            int startSubTitle = spannableStringBuilder.length();
            spannableStringBuilder.append(mHeaderItem.getInfo());
            spannableStringBuilder.setSpan(new RelativeSizeSpan(0.8f), startSubTitle, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_GREY), startSubTitle, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            mInfoView.setText(spannableStringBuilder);
        } else {
            Design.updateTextFont(mInfoView, Design.FONT_MEDIUM36);
            mInfoView.setTextColor(Design.FONT_COLOR_DEFAULT);
            mInfoView.setText(mHeaderItem.getTitle());
        }
    }

    private void backPressed() {
        if (DEBUG) {
            Log.d(LOG_TAG, "backPressed");
        }

        // Accept the back if the migration is canceled or terminated.
        if (mState == State.CANCELED || mState == State.TERMINATED) {
            terminateActivity();

        } else {
            onCancelClick();
        }
    }
}
