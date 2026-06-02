/*
 *  Copyright (c) 2023-2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.externalCallActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.calls.CallStatus;
import org.twinlife.twinme.models.CallReceiver;
import org.twinlife.twinme.models.Capabilities;
import org.twinlife.twinme.models.LinkValidity;
import org.twinlife.twinme.models.schedule.Date;
import org.twinlife.twinme.models.schedule.DateTime;
import org.twinlife.twinme.models.schedule.DateTimeRange;
import org.twinlife.twinme.models.schedule.Schedule;
import org.twinlife.twinme.models.schedule.Time;
import org.twinlife.twinme.models.schedule.WeeklyTimeRange;
import org.twinlife.twinme.services.CallReceiverService;
import org.twinlife.twinme.skin.CircularImageDescriptor;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.EditIdentityActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.LastCallsActivity;
import org.twinlife.twinme.ui.privacyActivity.UITimeout;
import org.twinlife.twinme.ui.callActivity.CallActivity;
import org.twinlife.twinme.ui.settingsActivity.MenuSelectValueView;
import org.twinlife.twinme.utils.CircularImageView;
import org.twinlife.twinme.utils.RoundedView;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

public class ShowExternalCallActivity extends AbstractTwinmeActivity implements CallReceiverService.Observer, MenuCallCapabilitiesView.Observer {
    private static final String LOG_TAG = "ShowExternalCall...";
    private static final boolean DEBUG = false;

    protected static final float DESIGN_ACTION_ITEM_VIEW = 128f;
    protected static final float DESIGN_ACTION_ITEM_MARGIN = 104f;

    private static int AVATAR_OVER_SIZE;
    private static int AVATAR_MAX_SIZE;

    private View mContentView;
    private View mEditView;
    private TextView mDescriptionTextView;
    private ImageView mAvatarView;
    private TextView mTitleView;
    private TextView mIdentityTextView;
    private CircularImageView mIdentityAvatarView;
    private RecyclerView mSettingsRecyclerView;

    private View mOverlayMenuView;
    private MenuCallCapabilitiesView mMenuCapabilitiesView;

    private ScrollView mScrollView;
    private View mAudioClickableView;
    private View mVideoClickableView;
    private View mShareClickableView;
    private RoundedView mRoundedShareView;
    private TextView mShareTextView;
    private RoundedView mRoundedVideoView;
    private TextView mVideoTextView;
    private RoundedView mRoundedAudioView;
    private TextView mAudioTextView;
    private TextView mIdentityTitleView;
    private View mIdentityView;

    private View mLastCallsView;
    private TextView mLastCallsTitleView;
    private TextView mLastCallsTextView;
    private ExternalCallConfigAdapter mExternalCallConfigAdapter;

    private boolean mUIInitialized = false;

    @Nullable
    private CallReceiver mCallReceiver;

    private Bitmap mAvatar;
    private Bitmap mIdentityAvatar;

    private CallReceiverService mCallReceiverService;

    private boolean mInitScrollView = false;
    private float mAvatarLastSize = -1;
    private float mScrollPosition = -1;

    private UIConfigExternalCall mConfigExternalCall;

    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        setFullscreen();

        initViews();

        mCallReceiverService = new CallReceiverService(this, getTwinmeContext(), this);

        Intent intent = getIntent();
        String callReceiverId = intent.getStringExtra(Intents.INTENT_CALL_RECEIVER_ID);
        if (callReceiverId != null) {
            mCallReceiverService.getCallReceiver(UUID.fromString(callReceiverId));
        } else {
            finish();
        }

        showProgressIndicator();
    }

    //
    // Override Activity methods
    //


    @Override
    public void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        if (mScrollView != null && !mInitScrollView) {
            mInitScrollView = true;
            Rect rectangle = new Rect();
            getWindow().getDecorView().getWindowVisibleDisplayFrame(rectangle);
            int contentHeight = mContentView.getHeight();
            if (contentHeight < rectangle.height()) {
                contentHeight = rectangle.height();
            }

            ViewGroup.LayoutParams layoutParams = mContentView.getLayoutParams();
            layoutParams.height = contentHeight + AVATAR_MAX_SIZE;

            mScrollView.post(() -> mScrollView.scrollBy(0, AVATAR_OVER_SIZE));

            updateContentHeight();
        }

        super.onResume();
    }

    @Override
    public void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        mCallReceiverService.dispose();

        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSaveInstanceState: outState=" + outState);
        }

        super.onSaveInstanceState(outState);
    }

    //
    // Implement CallReceiverService.Observer methods
    //

    @Override
    public void onGetCallReceiver(@Nullable CallReceiver callReceiver, @Nullable Bitmap image) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetCallReceiver: " + callReceiver);
        }

        mCallReceiver = callReceiver;
        mAvatar = image;
        updateExternalCall();
    }

    @Override
    public void onGetCallReceiverNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetCallReceiverNotFound");
        }

        // @todo report a message that the call receiver is invalid.
        finish();
    }

    @Override
    public void onUpdateCallReceiver(@NonNull CallReceiver callReceiver) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateCallReceiver: " + callReceiver);
        }

        if (mCallReceiver != null && callReceiver.getId().equals(mCallReceiver.getId())) {
            onGetCallReceiver(callReceiver, null);
        }
    }

    @Override
    public void onUpdateCallReceiverAvatar(@NonNull Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateCallReceiverAvatar: " + avatar);
        }

        mAvatar = avatar;
        updateExternalCall();
    }

    @Override
    public void onDeleteCallReceiver(@NonNull UUID callReceiverId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteCallReceiver: " + callReceiverId);
        }

        if (mCallReceiver != null && callReceiverId.equals(mCallReceiver.getId())) {
            finish();
        }
    }

    //MenuCallCapabilitiesView.Observer

    @Override
    public void onCloseMenuAnimationEnd() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCloseMenuAnimationEnd");
        }

        mConfigExternalCall.setAllowVoiceCall(mMenuCapabilitiesView.isCapabilitiesOn(MenuCallCapabilitiesView.VOICE_CALL_SWITCH));
        mConfigExternalCall.setAllowVideoCall(mMenuCapabilitiesView.isCapabilitiesOn(MenuCallCapabilitiesView.VIDEO_CALL_SWITCH));
        mConfigExternalCall.setAllowGroupCall(mMenuCapabilitiesView.isCapabilitiesOn(MenuCallCapabilitiesView.GROUP_CALL_SWITCH));

        mMenuCapabilitiesView.setVisibility(View.INVISIBLE);
        mOverlayMenuView.setVisibility(View.INVISIBLE);

        Window window = getWindow();
        window.setNavigationBarColor(Design.WHITE_COLOR);

        saveCapabilities();
    }

    //
    // Private methods
    //

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.show_external_call_activity);

        setTitle(getString(R.string.application_name));
        showToolBar(false);
        showBackButton(true);
        setBackgroundColor(Design.WHITE_COLOR);
        
        mAvatarView = findViewById(R.id.show_external_call_activity_avatar_view);
        mAvatarView.setBackgroundColor(Design.AVATAR_PLACEHOLDER_COLOR);

        ViewGroup.LayoutParams layoutParams = mAvatarView.getLayoutParams();
        layoutParams.width = AVATAR_MAX_SIZE - AVATAR_OVER_SIZE;
        layoutParams.height = AVATAR_MAX_SIZE - AVATAR_OVER_SIZE;

        mContentView = findViewById(R.id.show_external_call_activity_content_view);
        mContentView.setY(AVATAR_MAX_SIZE - Design.ACTION_VIEW_MIN_MARGIN);

        setBackground(mContentView);

        mScrollView = findViewById(R.id.show_external_call_activity_scroll_view);
        ViewTreeObserver viewTreeObserver = mScrollView.getViewTreeObserver();
        viewTreeObserver.addOnScrollChangedListener(() -> {
            if (mScrollPosition == -1) {
                mScrollPosition = AVATAR_OVER_SIZE;
            }

            float delta = mScrollPosition - mScrollView.getScrollY();
            updateAvatarSize(delta);
            mScrollPosition = mScrollView.getScrollY();
        });

        View slideMarkView = findViewById(R.id.show_external_call_activity_slide_mark_view);
        layoutParams = slideMarkView.getLayoutParams();
        layoutParams.height = Design.SLIDE_MARK_HEIGHT;

        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.mutate();
        gradientDrawable.setColor(Color.rgb(244, 244, 244));
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        slideMarkView.setBackground(gradientDrawable);

        float corner = ((float)Design.SLIDE_MARK_HEIGHT / 2) * Resources.getSystem().getDisplayMetrics().density;
        gradientDrawable.setCornerRadius(corner);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) slideMarkView.getLayoutParams();
        marginLayoutParams.topMargin =Design.SLIDE_MARK_TOP_MARGIN;

        View backClickableView = findViewById(R.id.show_external_call_activity_back_clickable_view);
        backClickableView.setOnClickListener(view -> onBackClick());

        layoutParams = backClickableView.getLayoutParams();
        layoutParams.height = Design.BACK_CLICKABLE_VIEW_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) backClickableView.getLayoutParams();
        marginLayoutParams.leftMargin = Design.BACK_CLICKABLE_VIEW_LEFT_MARGIN;
        marginLayoutParams.topMargin = Design.BACK_CLICKABLE_VIEW_TOP_MARGIN;

        RoundedView backRoundedView = findViewById(R.id.show_external_call_activity_back_rounded_view);
        backRoundedView.setColor(Design.BACK_VIEW_COLOR);

        mTitleView = findViewById(R.id.show_external_call_activity_title_view);
        Design.updateTextFont(mTitleView, Design.FONT_BOLD44);
        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        View headerView = findViewById(R.id.show_external_call_activity_content_header_view);
        marginLayoutParams = (ViewGroup.MarginLayoutParams) headerView.getLayoutParams();
        marginLayoutParams.topMargin = Design.HEADER_VIEW_TOP_MARGIN;

        mEditView = findViewById(R.id.show_external_call_activity_edit_clickable_view);
        mEditView.setOnClickListener(view -> onEditExternalCallClick());

        layoutParams = mEditView.getLayoutParams();
        layoutParams.height = Design.EDIT_CLICKABLE_VIEW_HEIGHT;

        ImageView editImageView = findViewById(R.id.show_external_call_activity_edit_image_view);
        editImageView.setColorFilter(Design.getMainStyle());

        View actionView = findViewById(R.id.show_external_call_activity_action_view);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) actionView.getLayoutParams();
        marginLayoutParams.topMargin = Design.ACTION_VIEW_TOP_MARGIN;

        mShareClickableView = findViewById(R.id.show_external_call_activity_share_clickable_view);
        mShareClickableView.setOnClickListener(view -> onTwincodeClick());

        layoutParams = mShareClickableView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_ACTION_ITEM_VIEW * Design.WIDTH_RATIO);
        layoutParams.height = Design.ACTION_CLICKABLE_VIEW_HEIGHT;

        mRoundedShareView = findViewById(R.id.show_external_call_activity_share_rounded_view);
        mShareTextView = findViewById(R.id.show_external_call_activity_share_text_view);

        mVideoClickableView = findViewById(R.id.show_external_call_activity_video_clickable_view);
        mVideoClickableView.setOnClickListener(view -> onVideoClick());

        layoutParams = mVideoClickableView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_ACTION_ITEM_VIEW * Design.WIDTH_RATIO);
        layoutParams.height = Design.ACTION_CLICKABLE_VIEW_HEIGHT;

        mRoundedVideoView = findViewById(R.id.show_external_call_activity_video_rounded_view);
        mVideoTextView = findViewById(R.id.show_external_call_activity_video_text_view);

        mAudioClickableView = findViewById(R.id.show_external_call_activity_audio_clickable_view);
        mAudioClickableView.setOnClickListener(view -> onAudioClick());

        layoutParams = mAudioClickableView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_ACTION_ITEM_VIEW * Design.WIDTH_RATIO);
        layoutParams.height = Design.ACTION_CLICKABLE_VIEW_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mVideoClickableView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_ACTION_ITEM_MARGIN * Design.WIDTH_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mAudioClickableView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_ACTION_ITEM_MARGIN * Design.WIDTH_RATIO);

        mRoundedAudioView = findViewById(R.id.show_external_call_activity_audio_rounded_view);
        mRoundedAudioView.setColor(Design.AUDIO_CALL_COLOR);

        mAudioTextView = findViewById(R.id.show_external_call_activity_audio_text_view);

        mDescriptionTextView = findViewById(R.id.show_external_call_activity_description_text_view);
        Design.updateTextFont(mDescriptionTextView, Design.FONT_MEDIUM34);
        mDescriptionTextView.setTextColor(Design.FONT_COLOR_DESCRIPTION);

        mIdentityView = findViewById(R.id.show_external_call_activity_identity_view);
        mIdentityView.setOnClickListener(view -> onEditIdentityClick());

        layoutParams = mIdentityView.getLayoutParams();
        layoutParams.height = Design.SECTION_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mIdentityView.getLayoutParams();
        marginLayoutParams.topMargin = Design.IDENTITY_VIEW_TOP_MARGIN;

        mIdentityTitleView = findViewById(R.id.show_external_call_activity_identity_title_view);
        Design.updateTextFont(mIdentityTitleView, Design.FONT_BOLD26);
        mIdentityTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mIdentityTitleView.getLayoutParams();
        marginLayoutParams.topMargin = Design.TITLE_IDENTITY_TOP_MARGIN;

        mIdentityTextView = findViewById(R.id.show_external_call_activity_identity_text_view);
        Design.updateTextFont(mIdentityTextView, Design.FONT_REGULAR34);
        mIdentityTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mIdentityAvatarView = findViewById(R.id.show_external_call_activity_identity_avatar_view);

        mSettingsRecyclerView = findViewById(R.id.show_external_call_activity_settings_view);

        mConfigExternalCall = new UIConfigExternalCall(this, false);

        ExternalCallConfigAdapter.OnExternalCallConfigClickListener onExternalCallConfigClickListener = new ExternalCallConfigAdapter.OnExternalCallConfigClickListener() {
            @Override
            public void onExternalCallConfigClick(UIConfigExternalCallItem configExternalCall) {

                if (configExternalCall.getConfigExternalCallSettings() == UIConfigExternalCall.ConfigExternalCallSettings.PERMISSIONS) {
                    openMenuCapabilities();
                } else if (configExternalCall.getConfigExternalCallSettings() == UIConfigExternalCall.ConfigExternalCallSettings.CALL_TYPE) {
                    openMenuSelectValue(MenuSelectValueView.MenuType.EXTERNAL_CALL_TYPE, mConfigExternalCall.getConfigCallType().ordinal());
                } else if (configExternalCall.getConfigExternalCallSettings() == UIConfigExternalCall.ConfigExternalCallSettings.EXPIRATION) {
                    openMenuSelectValue(MenuSelectValueView.MenuType.EXTERNAL_CALL_EXPIRATION, mConfigExternalCall.getLinkValidity().ordinal());
                }
            }

            @Override
            public void onDateViewClick(UIConfigExternalCallItem configExternalCall) {

                if (configExternalCall.getConfigExternalCallSettings() == UIConfigExternalCall.ConfigExternalCallSettings.SCHEDULE_START) {
                    onStartDateViewClick();
                } else if (configExternalCall.getConfigExternalCallSettings() == UIConfigExternalCall.ConfigExternalCallSettings.SCHEDULE_END) {
                    onEndDateViewClick();
                }
            }

            @Override
            public void onTimeViewClick(UIConfigExternalCallItem configExternalCall) {

                if (configExternalCall.getConfigExternalCallSettings() == UIConfigExternalCall.ConfigExternalCallSettings.SCHEDULE_START) {
                    onStartTimeViewClick();
                } else if (configExternalCall.getConfigExternalCallSettings() == UIConfigExternalCall.ConfigExternalCallSettings.SCHEDULE_END) {
                    onEndTimeViewClick();
                }
            }

            @Override
            public void onSelectDayClick(UIScheduleDay scheduleDay) {

                onSelectScheduleDayClick(scheduleDay);
            }

            @Override
            public void onSwitchValueChanged(UIConfigExternalCallItem configExternalCall, boolean value) {

                if (configExternalCall.getConfigExternalCallSettings() == UIConfigExternalCall.ConfigExternalCallSettings.DELETE) {
                    mConfigExternalCall.setDeleteLinkSetting(value);
                } if (configExternalCall.getConfigExternalCallSettings() == UIConfigExternalCall.ConfigExternalCallSettings.NOTIFICATION) {
                    mConfigExternalCall.setNotificationJoinSetting(value);
                }

                saveCapabilities();
            }
        };

        mExternalCallConfigAdapter = new ExternalCallConfigAdapter(this, mConfigExternalCall, onExternalCallConfigClickListener);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };

        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);

        mSettingsRecyclerView.setLayoutManager(linearLayoutManager);
        mSettingsRecyclerView.setAdapter(mExternalCallConfigAdapter);
        mSettingsRecyclerView.setItemAnimator(null);

        mLastCallsTitleView = findViewById(R.id.show_external_call_activity_last_calls_title_view);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mLastCallsTitleView.getLayoutParams();
        marginLayoutParams.topMargin = Design.TITLE_IDENTITY_TOP_MARGIN;

        mLastCallsView = findViewById(R.id.show_external_call_activity_last_calls_view);
        layoutParams = mLastCallsView.getLayoutParams();
        layoutParams.height = Design.SECTION_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mLastCallsView.getLayoutParams();
        marginLayoutParams.topMargin = Design.IDENTITY_VIEW_TOP_MARGIN;

        mLastCallsView.setOnClickListener(view -> onLastCallsClick());

        mLastCallsTextView = findViewById(R.id.show_external_call_activity_last_calls_text_view);
        Design.updateTextFont(mLastCallsTextView, Design.FONT_BOLD26);
        mLastCallsTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mLastCallsTextView.getLayoutParams();
        marginLayoutParams.topMargin = Design.TITLE_IDENTITY_TOP_MARGIN;

        mOverlayMenuView = findViewById(R.id.show_external_call_activity_overlay_view);
        mOverlayMenuView.setBackgroundColor(Design.OVERLAY_VIEW_COLOR);
        mOverlayMenuView.setOnClickListener(view -> closeMenuCapabilities());

        mMenuCapabilitiesView = findViewById(R.id.show_external_call_activity_menu_call_capabilities_view);
        mMenuCapabilitiesView.setVisibility(View.INVISIBLE);
        mMenuCapabilitiesView.setObserver(this);
        mMenuCapabilitiesView.setActivity(this);

        mProgressBarView = findViewById(R.id.show_external_call_activity_progress_bar);

        mUIInitialized = true;
    }

    private void updateExternalCall() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateExternalCall");
        }

        if (!mUIInitialized || mCallReceiver == null) {

            return;
        }

        String name = mCallReceiver.getName();
        String description = mCallReceiver.getDescription();
        mTitleView.setText(name);
        mDescriptionTextView.setText(description);
        mDescriptionTextView.setVisibility(View.VISIBLE);
        mEditView.setVisibility(View.VISIBLE);

        mConfigExternalCall.initWithCapabilities(mCallReceiver.getCapabilities(), mCallReceiver.isConference());

        if (!mCallReceiver.isConference()) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mShareClickableView.getLayoutParams();
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
            mShareClickableView.setLayoutParams(layoutParams);

            mAudioClickableView.setVisibility(View.GONE);
            mVideoClickableView.setVisibility(View.GONE);
            mIdentityTitleView.setVisibility(View.GONE);
            mIdentityView.setVisibility(View.GONE);
        } else {
            mIdentityTextView.setText(mCallReceiver.getIdentityName());
            mCallReceiverService.getOrganizerAvatar(mCallReceiver, (Bitmap avatar) -> {
                mIdentityAvatar = avatar;
                mIdentityAvatarView.setImage(this, null, new CircularImageDescriptor(mIdentityAvatar, 0.5f, 0.5f, 0.5f));
            });
        }

        if (mAvatar == null) {
            mCallReceiverService.getImage(mCallReceiver, (Bitmap avatar) -> {
                mAvatar = avatar;

                if (mAvatar != null) {
                    mAvatarView.setImageBitmap(mAvatar);
                } else {
                    mAvatarView.setBackgroundColor(Design.AVATAR_PLACEHOLDER_COLOR);
                }

                mExternalCallConfigAdapter.updateConfigItems(mConfigExternalCall);
            });
        } else {
            mAvatarView.setImageBitmap(mAvatar);
            mExternalCallConfigAdapter.updateConfigItems(mConfigExternalCall);
        }

        updateContentHeight();
    }

    private void onStartDateViewClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onStartDateViewClick");
        }

        final Calendar calendar = Calendar.getInstance();
        int day;
        int month;
        int year;
        if (mConfigExternalCall.getScheduleStartDate() != null) {
            day = mConfigExternalCall.getScheduleStartDate().day;
            month = mConfigExternalCall.getScheduleStartDate().month - 1;
            year = mConfigExternalCall.getScheduleStartDate().year;
        } else {
            day = calendar.get(Calendar.DAY_OF_MONTH);
            month = calendar.get(Calendar.MONTH);
            year = calendar.get(Calendar.YEAR);
        }

        DatePickerDialog.OnDateSetListener onDateSetListener = (datePicker, y, m, d) -> {
            mConfigExternalCall.setScheduleStartDate(new Date(y, m+1, d));
            saveCapabilities();
        };

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, onDateSetListener, year, month, day);
        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        datePickerDialog.show();
    }

    private void onStartTimeViewClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onStartTimeViewClick");
        }

        int hour;
        int minute;
        if (mConfigExternalCall.getScheduleStartTime() != null) {
            hour = mConfigExternalCall.getScheduleStartTime().hour;
            minute = mConfigExternalCall.getScheduleStartTime().minute;
        } else {
            final Calendar calendar = Calendar.getInstance();
            hour = calendar.get(Calendar.HOUR_OF_DAY);
            minute = calendar.get(Calendar.MINUTE);
        }

        TimePickerDialog.OnTimeSetListener onTimeSetListener = (view, h, m) -> {
            mConfigExternalCall.setScheduleStartTime(new Time(h, m));
            saveCapabilities();
        };

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, onTimeSetListener, hour, minute, true);
        timePickerDialog.show();
    }

    private void onEndDateViewClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onEndDateViewClick");
        }

        final Calendar calendar = Calendar.getInstance();
        int day;
        int month;
        int year;
        if (mConfigExternalCall.getScheduleEndDate() != null) {
            day = mConfigExternalCall.getScheduleEndDate().day;
            month = mConfigExternalCall.getScheduleEndDate().month - 1;
            year = mConfigExternalCall.getScheduleEndDate().year;
        } else {
            day = calendar.get(Calendar.DAY_OF_MONTH);
            month = calendar.get(Calendar.MONTH);
            year = calendar.get(Calendar.YEAR);
        }

        DatePickerDialog.OnDateSetListener onDateSetListener = (datePicker, y, m, d) -> {
            mConfigExternalCall.setScheduleEndDate(new Date(y, m+1, d));
            saveCapabilities();
        };

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, onDateSetListener, year, month, day);

        if (mConfigExternalCall.getScheduleStartDate() != null) {
            calendar.set(mConfigExternalCall.getScheduleStartDate().year, mConfigExternalCall.getScheduleStartDate().month - 1, mConfigExternalCall.getScheduleStartDate().day);
            datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        }

        datePickerDialog.show();
    }

    private void onEndTimeViewClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onEndTimeViewClick");
        }

        int hour;
        int minute;
        if (mConfigExternalCall.getScheduleEndTime() != null) {
            hour = mConfigExternalCall.getScheduleEndTime() .hour;
            minute = mConfigExternalCall.getScheduleEndTime() .minute;
        } else {
            final Calendar calendar = Calendar.getInstance();
            hour = calendar.get(Calendar.HOUR_OF_DAY);
            minute = calendar.get(Calendar.MINUTE);
        }

        TimePickerDialog.OnTimeSetListener onTimeSetListener = (view, h, m) -> {
            mConfigExternalCall.setScheduleEndTime(new Time(h, m));
            saveCapabilities();
        };

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, onTimeSetListener, hour, minute, true);
        timePickerDialog.show();
    }

    private void onSelectScheduleDayClick(UIScheduleDay scheduleDay) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSelectScheduleDayClick: scheduleDay=" + scheduleDay);
        }

        scheduleDay.setSelected(!scheduleDay.isSelected());
        mConfigExternalCall.updateDaySelected(scheduleDay.getDayOfWeek(), scheduleDay.isSelected());
        mExternalCallConfigAdapter.updateConfigItems(mConfigExternalCall);
        saveCapabilities();
    }

    protected void onEditExternalCallClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onEditExternalCallClick");
        }

        if (mCallReceiver != null) {
            startActivity(EditExternalCallActivity.class, Intents.INTENT_CALL_RECEIVER_ID, mCallReceiver.getId());
        }
    }

    protected void onTwincodeClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onTwincodeClick");
        }

        if (mCallReceiver != null) {
            startActivity(InvitationExternalCallActivity.class, Intents.INTENT_CALL_RECEIVER_ID, mCallReceiver.getId());
        }
    }

    protected void onAudioClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onAudioClick");
        }

        if (mCallReceiver == null) {
            return;
        }

        if (getTwinmeApplication().inCallInfo() == null && mCallReceiver.getCapabilities().hasAudio() && !hasSchedule()) {
            Intent intent = new Intent();
            intent.putExtra(Intents.INTENT_CONTACT_ID, mCallReceiver.getId().toString());
            intent.putExtra(Intents.INTENT_CALL_MODE, CallStatus.OUTGOING_CALL);

            startActivity(CallActivity.class, intent);
        } else if (!mCallReceiver.getCapabilities().hasAudio()) {
            Toast.makeText(this, R.string.application_not_authorized_operation_by_your_contact, Toast.LENGTH_SHORT).show();
        } else if (hasSchedule()) {
            showSchedule();
        }
    }

    protected void onVideoClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onVideoClick");
        }

        if (mCallReceiver == null) {
            return;
        }

        if (getTwinmeApplication().inCallInfo() == null && mCallReceiver.getCapabilities().hasVideo() && !hasSchedule()) {
            Intent intent = new Intent();
            intent.putExtra(Intents.INTENT_CONTACT_ID, mCallReceiver.getId().toString());
            intent.putExtra(Intents.INTENT_CALL_MODE, CallStatus.OUTGOING_VIDEO_CALL);

            startActivity(CallActivity.class, intent);
        } else if (!mCallReceiver.getCapabilities().hasVideo()) {
            Toast.makeText(this, R.string.application_not_authorized_operation_by_your_contact, Toast.LENGTH_SHORT).show();
        } else if (hasSchedule()) {
            showSchedule();
        }
    }

    protected void onEditIdentityClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onEditIdentityClick");
        }

        if (mCallReceiver != null) {
            startActivity(EditIdentityActivity.class, Intents.INTENT_CALL_RECEIVER_ID, mCallReceiver.getId());
        }
    }

    protected void onLastCallsClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onLastCallsClick");
        }

        if (mCallReceiver != null) {
            startActivity(LastCallsActivity.class, Intents.INTENT_CALL_RECEIVER_ID, mCallReceiver.getId());
        }
    }

    private void saveCapabilities() {
        if (DEBUG) {
            Log.d(LOG_TAG, "saveCapabilities");
        }

        if (mCallReceiver == null) {
            return;
        }
        Capabilities capabilities = mCallReceiver.getCapabilities();

        capabilities.setCapAudio(mConfigExternalCall.allowVoiceCall());
        capabilities.setCapVideo(mConfigExternalCall.allowVideoCall());
        capabilities.setCapGroupCall(mConfigExternalCall.allowGroupCall());
        capabilities.setCapNotifyJoin(mConfigExternalCall.notificationJoinCall());
        capabilities.setLinkValidity(mConfigExternalCall.getLinkValidity());

        if (mConfigExternalCall.getLinkValidity() == LinkValidity.SINGLE_USE && mConfigExternalCall.getScheduleStartDate() != null) {
            DateTime startDateTime = new DateTime(mConfigExternalCall.getScheduleStartDate(), mConfigExternalCall.getScheduleStartTime());
            DateTime startEndTime = new DateTime(mConfigExternalCall.getScheduleEndDate(), mConfigExternalCall.getScheduleEndTime());
            DateTimeRange dateTimeRange = new DateTimeRange(startDateTime, startEndTime);
            Schedule schedule = new Schedule(TimeZone.getDefault(), dateTimeRange);
            schedule.setEnabled(true);
            capabilities.setSchedule(schedule);
        } else if (mConfigExternalCall.getLinkValidity() == LinkValidity.PERIODIC) {
            WeeklyTimeRange weeklyTimeRange = new WeeklyTimeRange(mConfigExternalCall.getSelectedDaysOfWeek(), mConfigExternalCall.getScheduleStartTime(), mConfigExternalCall.getScheduleEndTime());
            Schedule schedule = new Schedule(TimeZone.getDefault(), weeklyTimeRange);
            schedule.setEnabled(true);
            capabilities.setSchedule(schedule);
        } else if (capabilities.getSchedule() != null) {
            capabilities.setSchedule(null);
        }

        mCallReceiver.putLong(UIConfigExternalCall.PROPERTY_CALL_RECEIVER_UPDATE_COUNTER, mCallReceiver.getLong(UIConfigExternalCall.PROPERTY_CALL_RECEIVER_UPDATE_COUNTER, 0) + 1, getTwinmeContext());
        mCallReceiverService.updateCallReceiver(mCallReceiver, capabilities);
        updateContentHeight();
    }

    private void openMenuCapabilities() {
        if (DEBUG) {
            Log.d(LOG_TAG, "openMenuCapabilities");
        }

        if (mCallReceiver != null && mMenuCapabilitiesView.getVisibility() == View.INVISIBLE) {
            mMenuCapabilitiesView.setVisibility(View.VISIBLE);
            mOverlayMenuView.setVisibility(View.VISIBLE);
            mMenuCapabilitiesView.openMenu(mCallReceiver.getCapabilities());

            Window window = getWindow();
            window.setNavigationBarColor(Design.POPUP_BACKGROUND_COLOR);
        }
    }

    private void closeMenuCapabilities() {
        if (DEBUG) {
            Log.d(LOG_TAG, "closeMenuCapabilities");
        }

        mMenuCapabilitiesView.animationCloseMenu();
    }

    private void openMenuSelectValue(MenuSelectValueView.MenuType menuType, int selectedValue) {
        if (DEBUG) {
            Log.d(LOG_TAG, "openMenuSelectValue");
        }

        ViewGroup viewGroup = findViewById(R.id.show_external_call_activity_layout);

        MenuSelectValueView menuSelectValueView = new MenuSelectValueView(this, null);

        menuSelectValueView.setActivity(this);
        menuSelectValueView.setObserver(new MenuSelectValueView.Observer() {
            @Override
            public void onCloseMenuAnimationEnd() {

                viewGroup.removeView(menuSelectValueView);

                Window window = getWindow();
                window.setNavigationBarColor(Design.WHITE_COLOR);
            }

            @Override
            public void onSelectValue(int value) {

                menuSelectValueView.animationCloseMenu();

                if (menuType == MenuSelectValueView.MenuType.EXTERNAL_CALL_TYPE) {
                    mConfigExternalCall.setConfigCallType(UIConfigExternalCall.ConfigExternalCallTypeCall.values()[value]);
                } else if (menuType == MenuSelectValueView.MenuType.EXTERNAL_CALL_EXPIRATION) {
                    mConfigExternalCall.setLinkValidity(LinkValidity.values()[value]);
                }

                mExternalCallConfigAdapter.updateConfigItems(mConfigExternalCall);
                saveCapabilities();
            }

            @Override
            public void onSelectTimeout(UITimeout timeout) {

            }
        });

        viewGroup.addView(menuSelectValueView);
        menuSelectValueView.openMenu(menuType, selectedValue);

        Window window = getWindow();
        window.setNavigationBarColor(Design.POPUP_BACKGROUND_COLOR);
    }

    private void updateAvatarSize(float deltaY) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateAvatarSize: " + deltaY);
        }

        if (mAvatarLastSize == -1) {
            mAvatarLastSize = AVATAR_MAX_SIZE - AVATAR_OVER_SIZE;
        }

        float avatarViewSize = mAvatarLastSize + deltaY;

        if (avatarViewSize < Design.DISPLAY_WIDTH) {
            avatarViewSize = Design.DISPLAY_WIDTH;
        } else if (avatarViewSize > AVATAR_MAX_SIZE) {
            avatarViewSize = AVATAR_MAX_SIZE;
        }

        if (avatarViewSize != mAvatarLastSize) {
            ViewGroup.LayoutParams avatarLayoutParams = mAvatarView.getLayoutParams();
            avatarLayoutParams.width = (int) avatarViewSize;
            avatarLayoutParams.height = (int) avatarViewSize;
            mAvatarView.requestLayout();

            mAvatarLastSize = avatarViewSize;
        }
    }

    private void updateContentHeight() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateContentHeight");
        }

        mSettingsRecyclerView.post(() -> {
            mSettingsRecyclerView.requestLayout();
            ViewGroup.LayoutParams layoutParams = mContentView.getLayoutParams();
            layoutParams.height = (int) (mLastCallsView.getY() + mLastCallsView.getHeight() + AVATAR_MAX_SIZE);
            mContentView.setLayoutParams(layoutParams);
        });
    }

    private boolean hasSchedule() {
        if (DEBUG) {
            Log.d(LOG_TAG, "hasSchedule");
        }

        if (mCallReceiver != null && mCallReceiver.getCapabilities().getSchedule() != null && mCallReceiver.getCapabilities().getSchedule().isEnabled()) {
            return !mCallReceiver.getCapabilities().getSchedule().isNowInRange();
        }

        return false;
    }

    private void showSchedule() {
        if (DEBUG) {
            Log.d(LOG_TAG, "showSchedule");
        }

        String message = "";
        if (mCallReceiver != null && mCallReceiver.getCapabilities().getSchedule() != null) {
            Schedule schedule = mCallReceiver.getCapabilities().getSchedule();
            if (schedule != null && !schedule.getTimeRanges().isEmpty()) {

                if (schedule.getTimeRanges().get(0) instanceof WeeklyTimeRange) {
                    WeeklyTimeRange weeklyTimeRange = (WeeklyTimeRange) schedule.getTimeRanges().get(0);
                    Time scheduleStartTime = weeklyTimeRange.start;
                    Time scheduleEndTime = weeklyTimeRange.end;
                    StringBuilder messageStringBuilder = new StringBuilder();
                    messageStringBuilder.append(getString(R.string.show_call_view_settings_start));
                    messageStringBuilder.append(" : ");
                    messageStringBuilder.append(scheduleStartTime);
                    messageStringBuilder.append("\n");
                    messageStringBuilder.append(getString(R.string.show_call_view_settings_end));
                    messageStringBuilder.append(" : ");
                    messageStringBuilder.append(scheduleEndTime);
                    messageStringBuilder.append("\n\n");

                    DateFormatSymbols dateFormatSymbols = new DateFormatSymbols(Locale.getDefault());
                    String[] weekDays = dateFormatSymbols.getWeekdays();

                    for (WeeklyTimeRange.DayOfWeek dayOfWeek : weeklyTimeRange.days) {
                        String dayString = "";
                        switch (dayOfWeek) {
                            case MONDAY:
                                dayString = weekDays[2];
                                break;
                            case TUESDAY:
                                dayString = weekDays[3];
                                break;
                            case WEDNESDAY:
                                dayString = weekDays[4];
                                break;
                            case THURSDAY:
                                dayString = weekDays[5];
                                break;
                            case FRIDAY:
                                dayString = weekDays[6];
                                break;
                            case SATURDAY:
                                dayString = weekDays[7];
                                break;
                            case SUNDAY:
                                dayString = weekDays[1];
                                break;
                        }

                        if (!dayString.isEmpty()) {
                            messageStringBuilder.append(dayString);
                            messageStringBuilder.append("\n");
                        }

                        message = messageStringBuilder.toString();
                    }
                } else {
                    DateTimeRange dateTimeRange = (DateTimeRange) schedule.getTimeRanges().get(0);
                    DateTime start = dateTimeRange.start;
                    DateTime end = dateTimeRange.end;

                    if (start.date.equals(end.date)) {
                        message = String.format(getString(R.string.show_call_view_schedule_from_to), start.formatDate(), start.formatTime(this), end.formatTime(this));
                    } else {
                        message = String.format("%1$s %2$s", start.formatDateTime(this), end.formatDateTime(this));
                    }
                }
            } else {
                message = getString(R.string.show_call_view_schedule_message);
            }

            showAlertMessageView(R.id.show_external_call_activity_layout, getString(R.string.show_call_view_schedule_call), message, true, null);
        }
    }

    public void updateInCall() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateInCall");
        }

        if (getTwinmeApplication().inCallInfo() != null || (mCallReceiver != null && (!mCallReceiver.getCapabilities().hasAudio() || !mCallReceiver.hasPrivatePeer())) || hasSchedule()) {
            mAudioClickableView.setAlpha(0.5f);
        } else {
            mAudioClickableView.setAlpha(1f);
        }

        if (getTwinmeApplication().inCallInfo() != null || (mCallReceiver != null && (!mCallReceiver.getCapabilities().hasVideo() || !mCallReceiver.hasPrivatePeer())) || hasSchedule()) {
            mVideoClickableView.setAlpha(0.5f);
        } else {
            mVideoClickableView.setAlpha(1f);
        }
    }

    @Override
    public void updateFont() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateFont");
        }

        super.updateFont();

        if (!mUIInitialized) {
            return;
        }

        Design.updateTextFont(mTitleView, Design.FONT_BOLD44);
        Design.updateTextFont(mDescriptionTextView, Design.FONT_REGULAR32);
        Design.updateTextFont(mShareTextView, Design.FONT_REGULAR28);
        Design.updateTextFont(mVideoTextView, Design.FONT_REGULAR28);
        Design.updateTextFont(mAudioTextView, Design.FONT_REGULAR28);
        Design.updateTextFont(mIdentityTitleView, Design.FONT_BOLD26);
        Design.updateTextFont(mIdentityTextView, Design.FONT_REGULAR34);
        Design.updateTextFont(mLastCallsTitleView, Design.FONT_BOLD26);
        Design.updateTextFont(mLastCallsTextView, Design.FONT_REGULAR34);
    }

    @Override
    public void updateColor() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateColor");
        }

        super.updateColor();

        if (!mUIInitialized) {
            return;
        }

        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mDescriptionTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mRoundedShareView.setColor(Design.GREY_COLOR);
        mShareTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mRoundedVideoView.setColor(Design.VIDEO_CALL_COLOR);
        mVideoTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mRoundedAudioView.setColor(Design.AUDIO_CALL_COLOR);
        mAudioTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mIdentityTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mIdentityTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mLastCallsTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mLastCallsTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
    }

    @Override
    public void setupDesign() {
        if (DEBUG) {
            Log.d(LOG_TAG, "setupDesign");
        }

        AVATAR_OVER_SIZE = (int) (Design.AVATAR_OVER_WIDTH * Design.WIDTH_RATIO);
        AVATAR_MAX_SIZE = Design.DISPLAY_WIDTH + (AVATAR_OVER_SIZE * 2);
    }
}
