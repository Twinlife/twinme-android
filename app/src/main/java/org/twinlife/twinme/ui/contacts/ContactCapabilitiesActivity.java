/*
 *  Copyright (c) 2021-2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Romain Kolb (romain.kolb@skyrock.com)
 */

package org.twinlife.twinme.ui.contacts;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.ColorUtils;
import androidx.percentlayout.widget.PercentRelativeLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.models.Capabilities;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Zoomable;
import org.twinlife.twinme.models.schedule.Date;
import org.twinlife.twinme.models.schedule.DateTimeRange;
import org.twinlife.twinme.models.schedule.Schedule;
import org.twinlife.twinme.models.schedule.Time;
import org.twinlife.twinme.services.EditContactCapabilitiesService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.TwinmeApplication;
import org.twinlife.twinme.ui.inAppSubscriptionActivity.InAppSubscriptionActivity;
import org.twinlife.twinme.ui.premiumServicesActivity.PremiumFeatureConfirmView;
import org.twinlife.twinme.ui.premiumServicesActivity.UIPremiumFeature;
import org.twinlife.twinme.ui.privacyActivity.UITimeout;
import org.twinlife.twinme.ui.settingsActivity.MenuSelectValueView;
import org.twinlife.twinme.utils.AbstractConfirmView;
import org.twinlife.twinme.utils.OnboardingConfirmView;

import java.util.Calendar;
import java.util.UUID;

public class ContactCapabilitiesActivity extends AbstractCapabilitiesActivity implements EditContactCapabilitiesService.Observer, MenuSelectValueView.Observer {
    private static final String LOG_TAG = "ContactCapabilitiesA...";
    private static final boolean DEBUG = false;

    private View mOverlayMenuView;
    private MenuSelectValueView mMenuSelectValueView;

    private Contact mContact;

    private EditContactCapabilitiesService mEditContactCapabiltiesService;

    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        mEditContactCapabiltiesService = new EditContactCapabilitiesService(this, getTwinmeContext(), this);

        Intent intent = getIntent();
        String contactId = intent.getStringExtra(Intents.INTENT_CONTACT_ID);
        if (contactId != null) {
            mEditContactCapabiltiesService.getContact(UUID.fromString(contactId));
        } else {
            finish();
        }
    }

    //
    // Override Activity methods
    //

    @Override
    protected void onPause() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPause");
        }

        if (mCanSave) {
            mCapabilities.setCapAudio(mAllowAudioCall);
            mCapabilities.setCapVideo(mAllowVideoCall);
            mCapabilities.setCapAutoAnswerCall(mAllowAnsweringAutomatic);
            mCapabilities.setCapDiscreet(mDiscreetRelation);
            mCapabilities.setZoomable(mZoomable);

            if (mScheduleEnable) {
                mCapabilities.setSchedule(createSchedule());
            } else if (mCapabilities.getSchedule() != null) {
                mCapabilities.updateSchedule(s -> s.setEnabled(false));
            }

            mEditContactCapabiltiesService.updateContact(mContact, mCapabilities, null);
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        mEditContactCapabiltiesService.dispose();

        super.onDestroy();
    }

    //
    // Implement EditContactCapabilitiesService.Observer methods
    //

    @Override
    public void onGetContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContact: contact=" + contact);
        }

        mContact = contact;

        final String identityCapabilities = contact.getIdentityCapabilities().toAttributeValue();
        mCapabilities = identityCapabilities == null ? new Capabilities() : new Capabilities(identityCapabilities);

        mAllowAudioCall = mCapabilities.hasAudio();
        mAllowVideoCall = mCapabilities.hasVideo();
        mAllowAnsweringAutomatic = mCapabilities.hasAutoAnswerCall();
        mZoomable = mCapabilities.getZoomable();
        mDiscreetRelation = mCapabilities.hasDiscreet();

        if (mCapabilities.getSchedule() != null) {
            Schedule schedule = mCapabilities.getSchedule();
            mScheduleEnable = schedule.isEnabled();
            if (!schedule.getTimeRanges().isEmpty()) {
                DateTimeRange dateTimeRange = (DateTimeRange) schedule.getTimeRanges().get(0);
                mScheduleStartDate = dateTimeRange.start.date;
                mScheduleStartTime = dateTimeRange.start.time;
                mScheduleEndDate = dateTimeRange.end.date;
                mScheduleEndTime = dateTimeRange.end.time;

                updateSchedule();
            }
        } else {
            mScheduleEnable = false;
        }

        if (mUIInitialized) {
            mCapabilitiesAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onGetContactNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContactNotFound");
        }

        finish();
    }

    @Override
    public void onUpdateContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateContact: contact=" + contact);
        }

        finish();
    }

    @Override
    protected void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        setContentView(R.layout.contact_capabilities_activity);

        setStatusBarColor();
        setToolBar(R.id.contact_capabilities_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);
        setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        setTitle(getString(R.string.contact_capabilities_activity_call_settings));
        applyInsets(R.id.capabilities_activity_layout, R.id.contact_capabilities_activity_tool_bar, R.id.contact_capabilities_activity_list_view, Design.TOOLBAR_COLOR, false);

        mCapabilitiesAdapter = new CapabilitiesAdapter(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        RecyclerView settingsRecyclerView = findViewById(R.id.contact_capabilities_activity_list_view);
        settingsRecyclerView.setLayoutManager(linearLayoutManager);
        settingsRecyclerView.setAdapter(mCapabilitiesAdapter);
        settingsRecyclerView.setItemAnimator(null);
        settingsRecyclerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        mOverlayMenuView = findViewById(R.id.contact_capabilities_activity_overlay_view);
        mOverlayMenuView.setBackgroundColor(Design.OVERLAY_VIEW_COLOR);
        mOverlayMenuView.setOnClickListener(view -> closeMenu());

        mMenuSelectValueView = findViewById(R.id.contact_capabilities_activity_menu_select_value_view);
        mMenuSelectValueView.setVisibility(View.INVISIBLE);
        mMenuSelectValueView.setObserver(this);
        mMenuSelectValueView.setActivity(this);

        mProgressBarView = findViewById(R.id.contact_capabilities_activity_progress_bar);

        mUIInitialized = true;
    }

    @Override
    protected void setUpdated() {
        if (DEBUG) {
            Log.d(LOG_TAG, "setUpdated");
        }

        if (!mUIInitialized) {
            return;
        }

        boolean scheduleUpdated;

        if (!mScheduleEnable) {
            scheduleUpdated = mCapabilities.getSchedule() != null && mCapabilities.getSchedule().isEnabled();
        } else {
            Schedule newSchedule = createSchedule();
            scheduleUpdated = mCapabilities.getSchedule() == null || !mCapabilities.getSchedule().equals(newSchedule);
        }

        mCanSave = mCapabilities.hasAudio() != mAllowAudioCall ||
                mCapabilities.hasVideo() != mAllowVideoCall ||
                mCapabilities.hasAutoAnswerCall() != mAllowAnsweringAutomatic ||
                mCapabilities.hasDiscreet() != mDiscreetRelation ||
                !mCapabilities.getZoomable().equals(mZoomable) ||
                scheduleUpdated;
    }

    @Override
    protected void initSchedule() {
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

    @Override
    protected void onDateClick(ScheduleType scheduleType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDateClick");
        }

        if (scheduleType == ScheduleType.START) {
            onStartDateViewClick();
        } else {
            onEndDateViewClick();
        }
    }

    @Override
    protected void onTimeClick(ScheduleType scheduleType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onTimeClick");
        }

        if (scheduleType == ScheduleType.START) {
            onStartTimeViewClick();
        } else {
            onEndTimeViewClick();
        }
    }

    @Override
    protected void onSelectControlCamera() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSelectControlCamera");
        }

        if (getTwinmeApplication().startOnboarding(TwinmeApplication.OnboardingType.REMOTE_CAMERA_SETTING)) {
            showOnboardingView(false);
        } else if (isFeatureSubscribed(org.twinlife.twinme.TwinmeApplication.Feature.GROUP_CALL)) {
            openSelectValueMenu();
        } else {
            showPremiumFeatureAlert(UIPremiumFeature.FeatureType.CAMERA_CONTROL);
        }
    }

    //
    // MenuSelectValueView.Observer
    //

    @Override
    public void onCloseMenuAnimationEnd() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCloseMenuAnimationEnd");
        }

        mMenuSelectValueView.setVisibility(View.INVISIBLE);
        mOverlayMenuView.setVisibility(View.INVISIBLE);
        setStatusBarColor();
    }

    @Override
    public void onSelectValue(int value) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSelectValue: " + value);
        }

        closeMenu();

        if (value == Zoomable.NEVER.ordinal()) {
            mZoomable = Zoomable.NEVER;
        } else if (value == Zoomable.ALLOW.ordinal()) {
            mZoomable = Zoomable.ALLOW;
        } else {
            mZoomable = Zoomable.ASK;
        }

        setUpdated();
        mCapabilitiesAdapter.notifyDataSetChanged();
    }

    @Override
    public void onSelectTimeout(UITimeout timeout) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSelectTimeout: " + timeout);
        }

    }

    private void onStartDateViewClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onStartDateViewClick");
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
            mCapabilitiesAdapter.notifyDataSetChanged();
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
            mCapabilitiesAdapter.notifyDataSetChanged();
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
            mCapabilitiesAdapter.notifyDataSetChanged();
        };

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, onDateSetListener, year, month, day);

        if (mScheduleStartDate != null) {
            calendar.set(mScheduleStartDate.year, mScheduleStartDate.month - 1, mScheduleStartDate.day);
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
            mCapabilitiesAdapter.notifyDataSetChanged();
        };

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, onTimeSetListener, hour, minute, true);
        timePickerDialog.show();
    }

    private void closeMenu() {
        if (DEBUG) {
            Log.d(LOG_TAG, "closeMenu");
        }

        mMenuSelectValueView.setVisibility(View.INVISIBLE);
        mOverlayMenuView.setVisibility(View.INVISIBLE);
        setStatusBarColor();
    }

    private void openSelectValueMenu() {
        if (DEBUG) {
            Log.d(LOG_TAG, "openSelectValueMenu");
        }

        if (mMenuSelectValueView.getVisibility() == View.INVISIBLE) {
            mMenuSelectValueView.setVisibility(View.VISIBLE);
            mOverlayMenuView.setVisibility(View.VISIBLE);
            mMenuSelectValueView.openMenu(MenuSelectValueView.MenuType.CAMERA_CONTROL);
            mMenuSelectValueView.setSelectedValue(mZoomable.ordinal());

            int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
            setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
        }
    }

    public void showPremiumFeatureAlert(UIPremiumFeature.FeatureType featureType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "showPremiumFeatureAlert");
        }

        ViewGroup viewGroup = findViewById(R.id.capabilities_activity_layout);

        PremiumFeatureConfirmView premiumFeatureConfirmView = new PremiumFeatureConfirmView(this, null);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        premiumFeatureConfirmView.setLayoutParams(layoutParams);
        premiumFeatureConfirmView.initWithPremiumFeature(new UIPremiumFeature(this, featureType));

        AbstractConfirmView.Observer observer = new AbstractConfirmView.Observer() {
            @Override
            public void onConfirmClick() {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), InAppSubscriptionActivity.class);
                startActivity(intent);
                premiumFeatureConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCancelClick() {
                premiumFeatureConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onDismissClick() {
                premiumFeatureConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                viewGroup.removeView(premiumFeatureConfirmView);
                setStatusBarColor();
            }
        };
        premiumFeatureConfirmView.setObserver(observer);

        viewGroup.addView(premiumFeatureConfirmView);
        premiumFeatureConfirmView.show();

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }

    @Override
    protected void showOnboardingView(boolean hideCancelAction) {
        if (DEBUG) {
            Log.d(LOG_TAG, "showOnboardingView");
        }

        ViewGroup viewGroup = findViewById(R.id.capabilities_activity_layout);

        OnboardingConfirmView onboardingConfirmView = new OnboardingConfirmView(this, null);
        PercentRelativeLayout.LayoutParams layoutParams = new PercentRelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        onboardingConfirmView.setLayoutParams(layoutParams);
        onboardingConfirmView.setImage(ResourcesCompat.getDrawable(getResources(), R.drawable.onboarding_control_camera, null));
        onboardingConfirmView.setTitle(getString(R.string.call_activity_camera_control_needs_help));
        onboardingConfirmView.setMessage(getString(R.string.contact_capabilities_activity_camera_control_onboarding));
        onboardingConfirmView.setConfirmTitle(getString(R.string.application_ok));
        onboardingConfirmView.setCancelTitle(getString(R.string.application_do_not_display));

        if (hideCancelAction) {
            onboardingConfirmView.hideCancelView();
        }

        AbstractConfirmView.Observer observer = new AbstractConfirmView.Observer() {
            @Override
            public void onConfirmClick() {
                onboardingConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCancelClick() {
                onboardingConfirmView.animationCloseConfirmView();
                getTwinmeApplication().setShowOnboardingType(TwinmeApplication.OnboardingType.REMOTE_CAMERA_SETTING, false);
            }

            @Override
            public void onDismissClick() {
                onboardingConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                viewGroup.removeView(onboardingConfirmView);

                if (fromConfirmAction && !hideCancelAction) {
                    openSelectValueMenu();
                }

                setStatusBarColor();
            }
        };
        onboardingConfirmView.setObserver(observer);
        viewGroup.addView(onboardingConfirmView);
        onboardingConfirmView.show();

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }
}
