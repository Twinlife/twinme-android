/*
 *  Copyright (c) 2023-2024 twinlife SA.
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
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.format.DateFormat;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.models.CallReceiver;
import org.twinlife.twinme.models.Capabilities;
import org.twinlife.twinme.models.schedule.Date;
import org.twinlife.twinme.models.schedule.DateTime;
import org.twinlife.twinme.models.schedule.DateTimeRange;
import org.twinlife.twinme.models.schedule.Schedule;
import org.twinlife.twinme.models.schedule.Time;
import org.twinlife.twinme.services.CallReceiverService;
import org.twinlife.twinme.skin.CircularImageDescriptor;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.EditIdentityActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.LastCallsActivity;
import org.twinlife.twinme.utils.CircularImageView;
import org.twinlife.twinme.utils.RoundedView;
import org.twinlife.twinme.utils.SwitchView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

public class ShowExternalCallActivity extends AbstractTwinmeActivity implements CallReceiverService.Observer, MenuCallCapabilitiesView.Observer {
    private static final String LOG_TAG = "ShowExternalCall...";
    private static final boolean DEBUG = false;

    private static final int DESIGN_MESSAGE_VIEW_TOP_MARGIN = 60;
    private static int AVATAR_OVER_SIZE;
    private static int AVATAR_MAX_SIZE;

    private View mContentView;
    private View mTwincodeView;
    private TextView mMessageView;
    private View mEditView;
    private TextView mDescriptionTextView;
    private ImageView mAvatarView;
    private TextView mTitleView;
    private TextView mIdentityTextView;
    private CircularImageView mIdentityAvatarView;
    private TextView mSettingsTextView;
    private SwitchView mLimitedSwitchView;
    private View mStartView;
    private TextView mStartDateTextView;
    private TextView mStartTimeTextView;
    private View mEndView;
    private TextView mEndDateTextView;
    private TextView mEndTimeTextView;

    private View mOverlayMenuView;
    private MenuCallCapabilitiesView mMenuCapabilitiesView;

    private ScrollView mScrollView;

    private boolean mUIInitialized = false;

    @Nullable
    private CallReceiver mCallReceiver;

    private Bitmap mAvatar;
    private Bitmap mIdentityAvatar;

    private CallReceiverService mCallReceiverService;

    private boolean mInitScrollView = false;
    private float mAvatarLastSize = -1;
    private float mScrollPosition = -1;

    private Date mScheduleStartDate;
    private Time mScheduleStartTime;
    private Date mScheduleEndDate;
    private Time mScheduleEndTime;

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
    public void onGetCallReceiver(@Nullable CallReceiver callReceiver) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetCallReceiver: " + callReceiver);
        }

        mCallReceiver = callReceiver;

        if (mCallReceiverService == null) {
            mIdentityAvatar = getTwinmeApplication().getAnonymousAvatar();
            updateExternalCall();
            return;
        }

        mCallReceiverService.getIdentityImage(callReceiver, (Bitmap identityAvatar) -> {
            mIdentityAvatar = identityAvatar;
            if (mCallReceiver != null && mCallReceiver.getAvatarId() != null) {
                mCallReceiverService.getLargeAvatar(mCallReceiver.getAvatarId());
            }

            updateExternalCall();
        });
    }

    @Override
    public void onUpdateCallReceiver(@NonNull CallReceiver callReceiver) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateCallReceiver: " + callReceiver);
        }

        if (mCallReceiver != null && callReceiver.getId().equals(mCallReceiver.getId())) {
            onGetCallReceiver(callReceiver);
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

        mMenuCapabilitiesView.setVisibility(View.INVISIBLE);
        mOverlayMenuView.setVisibility(View.INVISIBLE);

        Window window = getWindow();
        window.setNavigationBarColor(Design.WHITE_COLOR);

        saveCallCapabilities();
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
        ViewCompat.setBackground(slideMarkView, gradientDrawable);

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

        mTwincodeView = findViewById(R.id.show_external_call_activity_twincode_view);
        mTwincodeView.setOnClickListener(view -> onTwincodeClick());

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mTwincodeView.getLayoutParams();
        marginLayoutParams.topMargin = Design.TWINCODE_VIEW_TOP_MARGIN;

        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable twincodeViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        twincodeViewBackground.getPaint().setColor(Design.getMainStyle());
        ViewCompat.setBackground(mTwincodeView, twincodeViewBackground);

        ImageView twincodeIconView = findViewById(R.id.show_external_call_activity_twincode_icon_view);
        marginLayoutParams = (ViewGroup.MarginLayoutParams) twincodeIconView.getLayoutParams();
        marginLayoutParams.leftMargin = Design.TWINCODE_PADDING;
        marginLayoutParams.topMargin = Design.TWINCODE_ICON_PADDING;
        marginLayoutParams.bottomMargin = Design.TWINCODE_ICON_PADDING;
        marginLayoutParams.setMarginStart(Design.TWINCODE_PADDING);

        layoutParams = twincodeIconView.getLayoutParams();
        layoutParams.height = Design.TWINCODE_ICON_SIZE;
        layoutParams.width = Design.TWINCODE_ICON_SIZE;

        TextView twincodeTextView = findViewById(R.id.show_external_call_activity_twincode_title_view);
        Design.updateTextFont(twincodeTextView, Design.FONT_REGULAR28);
        twincodeTextView.setTextColor(Color.WHITE);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) twincodeTextView.getLayoutParams();
        marginLayoutParams.leftMargin = Design.TWINCODE_PADDING;
        marginLayoutParams.rightMargin = Design.TWINCODE_PADDING;
        marginLayoutParams.setMarginStart(Design.TWINCODE_PADDING);
        marginLayoutParams.setMarginEnd(Design.TWINCODE_PADDING);

        mDescriptionTextView = findViewById(R.id.show_external_call_activity_description_text_view);
        Design.updateTextFont(mDescriptionTextView, Design.FONT_MEDIUM34);
        mDescriptionTextView.setTextColor(Design.FONT_COLOR_DESCRIPTION);

        mMessageView = findViewById(R.id.show_external_call_activity_message_view);
        Design.updateTextFont(mMessageView, Design.FONT_REGULAR26);
        mMessageView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mMessageView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_MESSAGE_VIEW_TOP_MARGIN * Design.HEIGHT_RATIO);

        View identityView = findViewById(R.id.show_external_call_activity_identity_view);
        identityView.setOnClickListener(view -> onEditIdentityClick());

        layoutParams = identityView.getLayoutParams();
        layoutParams.height = Design.SECTION_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) identityView.getLayoutParams();
        marginLayoutParams.topMargin = Design.IDENTITY_VIEW_TOP_MARGIN;

        TextView identityTitleView = findViewById(R.id.show_external_call_activity_identity_title_view);
        Design.updateTextFont(identityTitleView, Design.FONT_BOLD26);
        identityTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) identityTitleView.getLayoutParams();
        marginLayoutParams.topMargin = Design.TITLE_IDENTITY_TOP_MARGIN;

        mIdentityTextView = findViewById(R.id.show_external_call_activity_identity_text_view);
        Design.updateTextFont(mIdentityTextView, Design.FONT_REGULAR34);
        mIdentityTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mIdentityAvatarView = findViewById(R.id.show_external_call_activity_identity_avatar_view);

        TextView settingsTitleView = findViewById(R.id.show_external_call_activity_settings_title_view);
        Design.updateTextFont(settingsTitleView, Design.FONT_BOLD26);
        settingsTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) settingsTitleView.getLayoutParams();
        marginLayoutParams.topMargin = Design.TITLE_IDENTITY_TOP_MARGIN;

        View settingsView = findViewById(R.id.show_external_call_activity_settings_view);
        settingsView.setOnClickListener(view -> onSettingsClick());

        layoutParams = settingsView.getLayoutParams();
        layoutParams.height = Design.SECTION_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) settingsView.getLayoutParams();
        marginLayoutParams.topMargin = Design.IDENTITY_VIEW_TOP_MARGIN;

        mSettingsTextView = findViewById(R.id.show_external_call_activity_settings_text_view);
        Design.updateTextFont(mSettingsTextView, Design.FONT_REGULAR34);
        mSettingsTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        View limitedView = findViewById(R.id.show_external_call_activity_limited_view);

        layoutParams = limitedView.getLayoutParams();
        layoutParams.height = Design.SECTION_HEIGHT;

        mLimitedSwitchView = findViewById(R.id.show_external_call_activity_limited_checkbox);
        Design.updateTextFont(mLimitedSwitchView, Design.FONT_REGULAR34);
        mLimitedSwitchView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mLimitedSwitchView.setOnCheckedChangeListener((buttonView, isChecked) -> saveLimited());

        mStartView = findViewById(R.id.show_external_call_activity_start_view);

        layoutParams = mStartView.getLayoutParams();
        layoutParams.height = Design.SECTION_HEIGHT;

        TextView startTextView = findViewById(R.id.show_external_call_activity_start_text_view);
        Design.updateTextFont(startTextView, Design.FONT_REGULAR34);
        startTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        View startDateView = findViewById(R.id.show_external_call_activity_start_date_view);
        startDateView.setOnClickListener(v -> onStartDateClick());

        layoutParams = startDateView.getLayoutParams();
        layoutParams.width = Design.DATE_VIEW_WIDTH;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) startDateView.getLayoutParams();
        marginLayoutParams.rightMargin = Design.DATE_VIEW_MARGIN;

        ShapeDrawable startDateViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        startDateViewBackground.getPaint().setColor(Design.DATE_BACKGROUND_COLOR);
        ViewCompat.setBackground(startDateView, startDateViewBackground);

        mStartDateTextView = findViewById(R.id.show_external_call_activity_start_date_text_view);
        Design.updateTextFont(mStartDateTextView, Design.FONT_REGULAR32);
        mStartDateTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mStartDateTextView.getLayoutParams();
        marginLayoutParams.leftMargin = Design.DATE_VIEW_PADDING;
        marginLayoutParams.rightMargin = Design.DATE_VIEW_PADDING;

        View startHourView = findViewById(R.id.show_external_call_activity_start_hour_view);
        startHourView.setOnClickListener(v -> onStartHourClick());

        layoutParams = startHourView.getLayoutParams();
        layoutParams.width = Design.HOUR_VIEW_WIDTH;

        ShapeDrawable startHourViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        startHourViewBackground.getPaint().setColor(Design.DATE_BACKGROUND_COLOR);
        ViewCompat.setBackground(startHourView, startHourViewBackground);

        mStartTimeTextView = findViewById(R.id.show_external_call_activity_start_hour_text_view);
        Design.updateTextFont(mStartTimeTextView, Design.FONT_REGULAR32);
        mStartTimeTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mStartTimeTextView.getLayoutParams();
        marginLayoutParams.leftMargin = Design.DATE_VIEW_PADDING;
        marginLayoutParams.rightMargin = Design.DATE_VIEW_PADDING;

        mEndView = findViewById(R.id.show_external_call_activity_end_view);

        layoutParams = mEndView.getLayoutParams();
        layoutParams.height = Design.SECTION_HEIGHT;

        TextView endTextView = findViewById(R.id.show_external_call_activity_end_text_view);
        Design.updateTextFont(endTextView, Design.FONT_REGULAR34);
        endTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        View endDateView = findViewById(R.id.show_external_call_activity_end_date_view);
        endDateView.setOnClickListener(v -> onEndDateClick());

        layoutParams = endDateView.getLayoutParams();
        layoutParams.width = Design.DATE_VIEW_WIDTH;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) endDateView.getLayoutParams();
        marginLayoutParams.rightMargin = Design.DATE_VIEW_MARGIN;

        ShapeDrawable endDateViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        endDateViewBackground.getPaint().setColor(Design.DATE_BACKGROUND_COLOR);
        ViewCompat.setBackground(endDateView, endDateViewBackground);

        mEndDateTextView = findViewById(R.id.show_external_call_activity_end_date_text_view);
        Design.updateTextFont(mEndDateTextView, Design.FONT_REGULAR32);
        mEndDateTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mEndDateTextView.getLayoutParams();
        marginLayoutParams.leftMargin = Design.DATE_VIEW_PADDING;
        marginLayoutParams.rightMargin = Design.DATE_VIEW_PADDING;

        View endHourView = findViewById(R.id.show_external_call_activity_end_hour_view);
        endHourView.setOnClickListener(v -> onEndHourClick());

        layoutParams = endHourView.getLayoutParams();
        layoutParams.width = Design.HOUR_VIEW_WIDTH;

        ShapeDrawable endHourViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        endHourViewBackground.getPaint().setColor(Design.DATE_BACKGROUND_COLOR);
        ViewCompat.setBackground(endHourView, endHourViewBackground);

        mEndTimeTextView = findViewById(R.id.show_external_call_activity_end_hour_text_view);
        Design.updateTextFont(mEndTimeTextView, Design.FONT_REGULAR32);
        mEndTimeTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mEndTimeTextView.getLayoutParams();
        marginLayoutParams.leftMargin = Design.DATE_VIEW_PADDING;
        marginLayoutParams.rightMargin = Design.DATE_VIEW_PADDING;

        TextView lastCallsTitleView = findViewById(R.id.show_external_call_activity_last_calls_title_view);
        Design.updateTextFont(lastCallsTitleView, Design.FONT_BOLD26);
        lastCallsTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) lastCallsTitleView.getLayoutParams();
        marginLayoutParams.topMargin = Design.TITLE_IDENTITY_TOP_MARGIN;

        View lastCallsView = findViewById(R.id.show_external_call_activity_last_calls_view);
        layoutParams = lastCallsView.getLayoutParams();
        layoutParams.height = Design.SECTION_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) lastCallsView.getLayoutParams();
        marginLayoutParams.topMargin = Design.IDENTITY_VIEW_TOP_MARGIN;

        lastCallsView.setOnClickListener(view -> onLastCallsClick());

        TextView lastCallsTextView = findViewById(R.id.show_external_call_activity_last_calls_text_view);
        Design.updateTextFont(lastCallsTextView, Design.FONT_REGULAR34);
        lastCallsTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mOverlayMenuView = findViewById(R.id.show_external_call_activity_overlay_view);
        mOverlayMenuView.setBackgroundColor(Design.OVERLAY_VIEW_COLOR);
        mOverlayMenuView.setOnClickListener(view -> closeMenuCapabilites());

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
        mMessageView.setVisibility(View.VISIBLE);
        mTwincodeView.setVisibility(View.VISIBLE);
        mEditView.setVisibility(View.VISIBLE);

        mIdentityTextView.setText(mCallReceiver.getIdentityName());
        mIdentityAvatarView.setImage(this, null, new CircularImageDescriptor(mIdentityAvatar, 0.5f, 0.5f, 0.5f));

        if (mAvatar == null) {
            mCallReceiverService.getImage(mCallReceiver, (Bitmap avatar) -> {
                mAvatar = avatar;

                if (mAvatar != null) {
                    mAvatarView.setImageBitmap(mAvatar);
                } else {
                    mAvatarView.setBackgroundColor(Design.AVATAR_PLACEHOLDER_COLOR);
                }

                updateCallCapabilities();
            });
        } else {
            mAvatarView.setImageBitmap(mAvatar);
            updateCallCapabilities();
        }
    }

    private void updateCallCapabilities() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateCallCapabilities");
        }

        if (mCallReceiver == null) {
            return;
        }
        Capabilities capabilities = mCallReceiver.getCapabilities();

        SpannableStringBuilder spannableCapabilitesStringBuilder = new SpannableStringBuilder();

        if (capabilities.hasAudio()) {
            spannableCapabilitesStringBuilder.append(getString(R.string.show_contact_activity_audio));
        }

        if (capabilities.hasVideo()) {
            if (!spannableCapabilitesStringBuilder.toString().isEmpty()) {
                spannableCapabilitesStringBuilder.append(", ");
            }
            spannableCapabilitesStringBuilder.append(getString(R.string.show_contact_activity_video));
        }

        if (capabilities.hasGroupCall()) {
            if (!spannableCapabilitesStringBuilder.toString().isEmpty()) {
                spannableCapabilitesStringBuilder.append(", ");
            }
            spannableCapabilitesStringBuilder.append(getString(R.string.show_group_activity_title));
        }

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(getString(R.string.show_call_activity_settings_call));
        spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_DEFAULT), 0, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        if (!spannableCapabilitesStringBuilder.toString().isEmpty()) {
            spannableStringBuilder.append("\n");
            int startSubTitle = spannableStringBuilder.length();
            spannableStringBuilder.append(spannableCapabilitesStringBuilder.toString());
            spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_GREY), startSubTitle, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        mSettingsTextView.setText(spannableStringBuilder);

        final Schedule schedule = capabilities.getSchedule();
        if (schedule != null) {
            if (schedule.isEnabled()) {
                mStartView.setVisibility(View.VISIBLE);
                mEndView.setVisibility(View.VISIBLE);
            } else {
                mStartView.setVisibility(View.GONE);
                mEndView.setVisibility(View.GONE);
            }

            if (!schedule.getTimeRanges().isEmpty()) {
                DateTimeRange dateTimeRange = (DateTimeRange) schedule.getTimeRanges().get(0);
                mScheduleStartDate = dateTimeRange.start.date;
                mScheduleStartTime = dateTimeRange.start.time;
                mScheduleEndDate = dateTimeRange.end.date;
                mScheduleEndTime = dateTimeRange.end.time;

                updateSchedule(false);
            } else {
                mStartDateTextView.setText("");
                mStartTimeTextView.setText("");
                mEndDateTextView.setText("");
                mEndTimeTextView.setText("");
            }
            mLimitedSwitchView.setChecked(schedule.isEnabled());
        } else {
            mLimitedSwitchView.setChecked(false);
            mStartView.setVisibility(View.GONE);
            mEndView.setVisibility(View.GONE);
        }
    }

    private void initSchedule() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initSchedule");
        }

        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(new java.util.Date(System.currentTimeMillis()));

        calendar.add(Calendar.HOUR, 1);
        calendar.set(Calendar.MINUTE, 0);
        mScheduleStartDate = Date.from(calendar);
        mScheduleStartTime = Time.from(calendar);

        calendar.add(Calendar.HOUR, 1);
        mScheduleEndDate = Date.from(calendar);
        mScheduleEndTime = Time.from(calendar);
    }

    private void updateSchedule(boolean save) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateSchedule");
        }

        final Calendar startCalendar = new DateTime(mScheduleStartDate, mScheduleStartTime).toCalendar(TimeZone.getDefault());

        final Calendar endCalendar = new DateTime(mScheduleEndDate, mScheduleEndTime).toCalendar(TimeZone.getDefault());

        String formatDate = "dd MMM yyyy";
        String formatTime;
        if (DateFormat.is24HourFormat(this)) {
            formatTime = "HH:mm";
        } else {
            formatTime = "hh:mm a";
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(formatDate, Locale.getDefault());
        mStartDateTextView.setText(simpleDateFormat.format(startCalendar.getTime()));
        mEndDateTextView.setText(simpleDateFormat.format(endCalendar.getTime()));

        SimpleDateFormat simpleTimeFormat = new SimpleDateFormat(formatTime, Locale.getDefault());
        mStartTimeTextView.setText(simpleTimeFormat.format(startCalendar.getTime()));
        mEndTimeTextView.setText(simpleTimeFormat.format(endCalendar.getTime()));

        if (save) {
            saveCallSchedule();
        }
    }

    private void onStartDateClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onStartDateClick");
        }

        final Calendar calendar = Calendar.getInstance();
        int day;
        int month;
        int year;
        if (mScheduleStartDate != null) {
            day = mScheduleStartDate.day;
            month = mScheduleStartDate.month - 1;
            year = mScheduleStartDate.year;
        } else {
            day = calendar.get(Calendar.DAY_OF_MONTH);
            month = calendar.get(Calendar.MONTH);
            year = calendar.get(Calendar.YEAR);
        }

        DatePickerDialog.OnDateSetListener onDateSetListener = (datePicker, y, m, d) -> {
            mScheduleStartDate = new Date(y, m+1, d);
            updateSchedule(true);
        };

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, onDateSetListener, year, month, day);
        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        datePickerDialog.show();
    }

    private void onStartHourClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onStartHourClick");
        }

        int hour;
        int minute;
        if (mScheduleStartTime != null) {
            hour = mScheduleStartTime.hour;
            minute = mScheduleStartTime.minute;
        } else {
            final Calendar calendar = Calendar.getInstance();
            hour = calendar.get(Calendar.HOUR_OF_DAY);
            minute = calendar.get(Calendar.MINUTE);
        }

        TimePickerDialog.OnTimeSetListener onTimeSetListener = (view, h, m) -> {
            mScheduleStartTime = new Time(h, m);
            updateSchedule(true);
        };

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, onTimeSetListener, hour, minute, true);
        timePickerDialog.show();
    }

    private void onEndDateClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onEndDateClick");
        }

        final Calendar calendar = Calendar.getInstance();
        int day;
        int month;
        int year;
        if (mScheduleEndDate != null) {
            day = mScheduleEndDate.day;
            month = mScheduleEndDate.month - 1;
            year = mScheduleEndDate.year;
        } else {
            day = calendar.get(Calendar.DAY_OF_MONTH);
            month = calendar.get(Calendar.MONTH);
            year = calendar.get(Calendar.YEAR);
        }

        DatePickerDialog.OnDateSetListener onDateSetListener = (datePicker, y, m, d) -> {
            mScheduleEndDate = new Date(y, m+1, d);
            updateSchedule(true);
        };

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, onDateSetListener, year, month, day);

        if (mScheduleStartDate != null) {
            calendar.set(mScheduleStartDate.year, mScheduleStartDate.month - 1, mScheduleStartDate.day);
            datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        }

        datePickerDialog.show();
    }

    private void onEndHourClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onEndHourClick");
        }

        int hour;
        int minute;
        if (mScheduleEndTime != null) {
            hour = mScheduleEndTime.hour;
            minute = mScheduleEndTime.minute;
        } else {
            final Calendar calendar = Calendar.getInstance();
            hour = calendar.get(Calendar.HOUR_OF_DAY);
            minute = calendar.get(Calendar.MINUTE);
        }

        TimePickerDialog.OnTimeSetListener onTimeSetListener = (view, h, m) -> {
            mScheduleEndTime = new Time(h, m);
            updateSchedule(true);
        };

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, onTimeSetListener, hour, minute, true);
        timePickerDialog.show();
    }

    protected void onEditExternalCallClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onEditExternalCallClick");
        }

        startActivity(EditExternalCallActivity.class, Intents.INTENT_CALL_RECEIVER_ID, mCallReceiver.getId());
    }

    protected void onTwincodeClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onTwincodeClick");
        }

        startActivity(InvitationExternalCallActivity.class, Intents.INTENT_CALL_RECEIVER_ID, mCallReceiver.getId());
    }

    protected void onEditIdentityClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onEditIdentityClick");
        }

        startActivity(EditIdentityActivity.class, Intents.INTENT_CALL_RECEIVER_ID, mCallReceiver.getId());
    }

    protected void onSettingsClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSettingsClick");
        }

        openMenuCapabilities();
    }

    protected void onLastCallsClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onLastCallsClick");
        }

        if (mCallReceiver != null) {
            startActivity(LastCallsActivity.class, Intents.INTENT_CALL_RECEIVER_ID, mCallReceiver.getId());
        }
    }

    private void saveLimited() {
        if (DEBUG) {
            Log.d(LOG_TAG, "saveLimited");
        }

        if (mCallReceiver != null) {
            Capabilities capabilities = mCallReceiver.getCapabilities();

            if (capabilities.getSchedule() == null) {
                initSchedule();
            }

            saveCallSchedule();
        }
    }

    private void saveCallCapabilities() {
        if (DEBUG) {
            Log.d(LOG_TAG, "saveCallCapabilities");
        }

        if (mCallReceiver == null) {
            return;
        }
        Capabilities capabilities = mCallReceiver.getCapabilities();
        boolean allowAudioCall = mMenuCapabilitiesView.isCapabilitiesOn(MenuCallCapabilitiesView.VOICE_CALL_SWITCH);
        boolean allowVideoCall = mMenuCapabilitiesView.isCapabilitiesOn(MenuCallCapabilitiesView.VIDEO_CALL_SWITCH);
        boolean allowGroupCall = mMenuCapabilitiesView.isCapabilitiesOn(MenuCallCapabilitiesView.GROUP_CALL_SWITCH);

        if (capabilities.hasAudio() == allowAudioCall && capabilities.hasVideo() == allowVideoCall && capabilities.hasGroupCall() == allowGroupCall) {
            return;
        }

        capabilities.setCapAudio(allowAudioCall);
        capabilities.setCapVideo(allowVideoCall);
        capabilities.setCapGroupCall(allowGroupCall);
        mCallReceiverService.updateCallReceiver(mCallReceiver, mCallReceiver.getName(), null, mCallReceiver.getIdentityName(), null, null, null, capabilities);
    }

    private void saveCallSchedule() {
        if (DEBUG) {
            Log.d(LOG_TAG, "saveCallSchedule");
        }

        if (mCallReceiver == null) {
            return;
        }
        Capabilities capabilities = mCallReceiver.getCapabilities();
        DateTime startDateTime = new DateTime(mScheduleStartDate, mScheduleStartTime);
        DateTime startEndTime = new DateTime(mScheduleEndDate, mScheduleEndTime);
        DateTimeRange dateTimeRange = new DateTimeRange(startDateTime, startEndTime);
        Schedule schedule = new Schedule(TimeZone.getDefault(), dateTimeRange);
        schedule.setEnabled(mLimitedSwitchView.isChecked());
        capabilities.setSchedule(schedule);
        mCallReceiverService.updateCallReceiver(mCallReceiver, mCallReceiver.getName(), null, mCallReceiver.getIdentityName(), null, null, null, capabilities);
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

    private void closeMenuCapabilites() {
        if (DEBUG) {
            Log.d(LOG_TAG, "closeMenuCapabilites");
        }

        mMenuCapabilitiesView.animationCloseMenu();
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

    @Override
    public void setupDesign() {
        if (DEBUG) {
            Log.d(LOG_TAG, "openMenuCapabilities");
        }

        AVATAR_OVER_SIZE = (int) (Design.AVATAR_OVER_WIDTH * Design.WIDTH_RATIO);
        AVATAR_MAX_SIZE = Design.DISPLAY_WIDTH + (AVATAR_OVER_SIZE * 2);
    }
}
