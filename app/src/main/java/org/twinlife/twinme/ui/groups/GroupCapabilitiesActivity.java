/*
 *  Copyright (c) 2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.groups;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinme.models.Capabilities;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Group;
import org.twinlife.twinme.models.GroupMember;
import org.twinlife.twinme.models.Invitation;
import org.twinlife.twinme.models.Space;
import org.twinlife.twinme.models.schedule.Date;
import org.twinlife.twinme.models.schedule.DateTimeRange;
import org.twinlife.twinme.models.schedule.Schedule;
import org.twinlife.twinme.models.schedule.Time;
import org.twinlife.twinme.services.GroupService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.contacts.AbstractCapabilitiesActivity;
import org.twinlife.twinme.ui.contacts.CapabilitiesAdapter;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class GroupCapabilitiesActivity extends AbstractCapabilitiesActivity implements GroupService.Observer {
    private static final String LOG_TAG = "GroupCapabilitiesAc...";
    private static final boolean DEBUG = false;

    private UUID mGroupId;
    private Group mGroup;

    private GroupService mGroupService;

    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String value = intent.getStringExtra(Intents.INTENT_GROUP_ID);
        if (value != null) {
            mGroupId = UUID.fromString(value);
        } else {
            finish();
        }

        initViews();

        mGroupService = new GroupService(this, getTwinmeContext(), this);
    }

    //
    // Override Activity methods
    //

    @Override
    protected void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        super.onResume();

        if (mGroupId != null) {
            mGroupService.getGroup(mGroupId, false);
        }
    }

    @Override
    protected void onPause() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPause");
        }

        if (mCanSave) {
            mCapabilities.setCapAudio(mAllowAudioCall);
            mCapabilities.setCapVideo(mAllowVideoCall);

            if (mScheduleEnable) {
                mCapabilities.setSchedule(createSchedule());
            } else if (mCapabilities.getSchedule() != null) {
                mCapabilities.updateSchedule(s -> s.setEnabled(false));
            }

            mGroupService.updateGroup(mGroup.getName(), null, null, null, null, mCapabilities);
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        mGroupService.dispose();

        super.onDestroy();
    }

    public boolean isGroup() {

        return true;
    }

    //
    // Implement GroupService.Observer methods
    //

    @Override
    public void onCreateGroup(@NonNull Group group, @NonNull ConversationService.GroupConversation conversation) {

    }

    @Override
    public void onGetGroup(@NonNull Group group, @NonNull List<GroupMember> groupMembers, @NonNull ConversationService.GroupConversation conversation) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetGroup group=" + group);
        }

        mGroup = group;

        final String capabilities = group.getCapabilities().toAttributeValue();
        mCapabilities = capabilities == null ? new Capabilities() : new Capabilities(capabilities);

        mAllowAudioCall = mCapabilities.hasAudio();
        mAllowVideoCall = mCapabilities.hasVideo();

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
    }

    @Override
    public void onUpdateGroup(@NonNull Group group, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateGroup group=" + group);
        }

        finish();
    }

    @Override
    public void onGetContact(@NonNull Contact contact, @Nullable Bitmap avatar) {

    }

    @Override
    public void onGetContactNotFound() {

    }

    @Override
    public void onUpdateContact(@NonNull Contact contact, @Nullable Bitmap avatar) {

    }

    @Override
    public void onGetContacts(@NonNull List<Contact> contacts) {

    }

    @Override
    public void onInviteGroup(@NonNull ConversationService.Conversation conversation, @NonNull ConversationService.InvitationDescriptor invitationDescriptor) {

    }

    @Override
    public void onLeaveGroup(@NonNull Group group, @NonNull UUID memberTwincodeId) {

    }

    @Override
    public void onDeleteGroup(UUID groupId) {

    }

    @Override
    public void onCreateInvitation(@NonNull Invitation invitation) {

    }

    @Override
    public void onGetCurrentSpace(@NonNull Space space) {

    }

    @Override
    public void onGetGroupNotFound() {

    }

    @Override
    public void onErrorLimitReached() {

    }

    @Override
    protected void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        setContentView(R.layout.group_capabilities_activity);

        setStatusBarColor();
        setToolBar(R.id.group_capabilities_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);
        setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        setTitle(getString(R.string.contact_capabilities_activity_call_settings));

        applyInsets(R.id.capabilities_activity_layout, R.id.group_capabilities_activity_tool_bar, R.id.group_capabilities_activity_list_view, Design.TOOLBAR_COLOR, false);

        mCapabilitiesAdapter = new CapabilitiesAdapter(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        RecyclerView settingsRecyclerView = findViewById(R.id.group_capabilities_activity_list_view);
        settingsRecyclerView.setLayoutManager(linearLayoutManager);
        settingsRecyclerView.setAdapter(mCapabilitiesAdapter);
        settingsRecyclerView.setItemAnimator(null);
        settingsRecyclerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        mProgressBarView = findViewById(R.id.group_capabilities_activity_progress_bar);

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
}
