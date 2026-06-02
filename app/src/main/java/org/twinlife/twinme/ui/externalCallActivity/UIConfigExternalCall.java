/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.externalCallActivity;

import android.content.Context;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.models.Capabilities;
import org.twinlife.twinme.models.LinkValidity;
import org.twinlife.twinme.models.schedule.Date;
import org.twinlife.twinme.models.schedule.DateTimeRange;
import org.twinlife.twinme.models.schedule.Schedule;
import org.twinlife.twinme.models.schedule.Time;
import org.twinlife.twinme.models.schedule.WeeklyTimeRange;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class UIConfigExternalCall {

    public static final String PROPERTY_CALL_RECEIVER_UPDATE_COUNTER  = "CallReceiverUpdateCounter";

    public enum ConfigExternalCallSettings {
        CALL_TYPE,
        PERMISSIONS,
        EXPIRATION,
        SCHEDULE_START,
        SCHEDULE_END,
        SCHEDULE_RECURRENT,
        DELETE,
        NOTIFICATION
    }

    public enum ConfigExternalCallTypeCall {
        CALL_DIRECT,
        CALL_CONFERENCE
    }

    private ConfigExternalCallTypeCall mConfigCallType = ConfigExternalCallTypeCall.CALL_DIRECT;
    private LinkValidity mLinkValidity = LinkValidity.SINGLE_USE;

    private final List<UIConfigExternalCallItem> mConfigItems = new ArrayList<>();

    private Date mScheduleStartDate;
    private Time mScheduleStartTime;
    private Date mScheduleEndDate;
    private Time mScheduleEndTime;

    private final List<UIScheduleDay> mScheduleRecurrentDays = new ArrayList<>();

    private boolean mAllowVoiceCall = true;
    private boolean mAllowVideoCall = true;
    private boolean mAllowGroupCall = false;

    private boolean mDeleteLinkSetting = true;
    private boolean mNotificationJoinCallSetting = true;

    private final boolean mCreateExternalCallMode;

    private final Context mContext;

    public UIConfigExternalCall(Context context, boolean createExternalCallMode) {
        mContext = context;
        mCreateExternalCallMode = createExternalCallMode;
    }

    public void initWithTemplate(UITemplateExternalCall templateExternalCall) {

        mAllowVoiceCall = templateExternalCall.voiceCallAllowed();
        mAllowVideoCall = templateExternalCall.videoCallAllowed();
        mAllowGroupCall  = templateExternalCall.groupCallAllowed();
        mLinkValidity = templateExternalCall.getLinkValidity();
        mConfigCallType = templateExternalCall.getTypeCall();

        setupDays();

        if (mLinkValidity == LinkValidity.SINGLE_USE && mScheduleStartDate == null) {
            initSchedule();
        } else if (mLinkValidity == LinkValidity.PERIODIC) {
            mScheduleStartTime = templateExternalCall.getScheduleStartTime();
            mScheduleEndTime = templateExternalCall.getScheduleEndTime();

            if (templateExternalCall.getScheduleDays() != null) {
                for (WeeklyTimeRange.DayOfWeek dayOfWeek : templateExternalCall.getScheduleDays()) {
                    for (UIScheduleDay scheduleDay : mScheduleRecurrentDays) {
                        if (scheduleDay.getDayOfWeek() == dayOfWeek) {
                            scheduleDay.setSelected(true);
                            break;
                        }
                    }
                }
            }
        }

        updateConfigItems();
    }

    public void initDefault() {

        setupDays();
        updateConfigItems();
    }

    public void initWithCapabilities(Capabilities capabilities, boolean isConference) {

        mAllowVoiceCall = capabilities.hasAudio();
        mAllowVideoCall = capabilities.hasVideo();
        mAllowGroupCall = capabilities.hasGroupCall();
        mLinkValidity = capabilities.getLinkValidity();
        mNotificationJoinCallSetting = capabilities.hasNotifyJoin();
        mConfigCallType = isConference ? ConfigExternalCallTypeCall.CALL_CONFERENCE : ConfigExternalCallTypeCall.CALL_DIRECT;

        setupDays();

        if (capabilities.getSchedule() != null) {
            Schedule schedule = capabilities.getSchedule();

            if (!schedule.getTimeRanges().isEmpty()) {
                if (schedule.getTimeRanges().get(0) instanceof DateTimeRange) {
                    DateTimeRange dateTimeRange = (DateTimeRange) schedule.getTimeRanges().get(0);
                    setScheduleStartDate(dateTimeRange.start.date);
                    setScheduleStartTime(dateTimeRange.start.time);
                    setScheduleEndDate(dateTimeRange.end.date);
                    setScheduleEndTime(dateTimeRange.end.time);

                    if (schedule.isEnabled()) {
                        setLinkValidity(LinkValidity.SINGLE_USE);
                    } else {
                        setLinkValidity(LinkValidity.PERMANENT);
                    }
                } else if (schedule.getTimeRanges().get(0) instanceof WeeklyTimeRange) {
                    WeeklyTimeRange weeklyTimeRange = (WeeklyTimeRange) schedule.getTimeRanges().get(0);
                    setScheduleStartTime(weeklyTimeRange.start);
                    setScheduleEndTime(weeklyTimeRange.end);
                    for (WeeklyTimeRange.DayOfWeek dayOfWeek : weeklyTimeRange.days) {
                        for (UIScheduleDay scheduleDay : mScheduleRecurrentDays) {
                            if (scheduleDay.getDayOfWeek() == dayOfWeek) {
                                scheduleDay.setSelected(true);
                                break;
                            }
                        }
                    }

                    if (schedule.isEnabled()) {
                        setLinkValidity(LinkValidity.PERIODIC);
                    } else {
                        setLinkValidity(LinkValidity.PERMANENT);
                    }
                }
            }
        }

        updateConfigItems();
    }

    public boolean isCreateExternalCallMode() {

        return mCreateExternalCallMode;
    }

    public void setAllowVoiceCall(boolean allowVoiceCall) {

        mAllowVoiceCall = allowVoiceCall;
    }

    public boolean allowVoiceCall() {

        return mAllowVoiceCall;
    }

    public void setAllowVideoCall(boolean allowVideoCall) {

        mAllowVideoCall = allowVideoCall;
    }

    public boolean allowVideoCall() {

        return mAllowVideoCall;
    }

    public void setAllowGroupCall(boolean allowGroupCall) {

        mAllowGroupCall = allowGroupCall;
    }

    public boolean allowGroupCall() {

        return mAllowGroupCall;
    }

    public void setDeleteLinkSetting(boolean deleteLinkSetting) {

        mDeleteLinkSetting = deleteLinkSetting;
    }

    public boolean deleteLink() {

        return mDeleteLinkSetting;
    }

    public void setNotificationJoinSetting(boolean notificationJoinSetting) {

        mNotificationJoinCallSetting = notificationJoinSetting;
    }

    public boolean notificationJoinCall() {

        return mNotificationJoinCallSetting;
    }


    public void setConfigCallType(ConfigExternalCallTypeCall configCallType) {

        mConfigCallType = configCallType;
        updateConfigItems();
    }

    public ConfigExternalCallTypeCall getConfigCallType() {

        return mConfigCallType;
    }

    public void setLinkValidity(LinkValidity linkValidity) {

        mLinkValidity = linkValidity;
        if (mLinkValidity != LinkValidity.PERMANENT && mScheduleStartDate == null) {
            initSchedule();
        }
        updateConfigItems();
    }

    public LinkValidity getLinkValidity() {

        return mLinkValidity;
    }

    public void setScheduleStartDate(Date scheduleStartDate) {

        mScheduleStartDate = scheduleStartDate;
        updateSchedule();
    }

    public Date getScheduleStartDate() {

        return mScheduleStartDate;
    }

    public void setScheduleEndDate(Date scheduleEndDate) {

        mScheduleEndDate = scheduleEndDate;
        updateSchedule();
    }

    public Date getScheduleEndDate() {

        return mScheduleEndDate;
    }

    public void setScheduleStartTime(Time scheduleStartTime) {

        mScheduleStartTime = scheduleStartTime;
        updateSchedule();
    }

    public Time getScheduleStartTime() {

        return mScheduleStartTime;
    }

    public void setScheduleEndTime(Time scheduleEndTime) {

        mScheduleEndTime = scheduleEndTime;
        updateSchedule();
    }

    public Time getScheduleEndTime() {

        return mScheduleEndTime;
    }

    public String getCallType() {

        if (mConfigCallType == ConfigExternalCallTypeCall.CALL_DIRECT) {
            return mContext.getString(R.string.create_external_call_view_direct_call_short_title);
        } else {
            return mContext.getString(R.string.create_external_call_view_conference_call_title);
        }
    }

    public String getExpiration() {

        if (mLinkValidity == LinkValidity.PERMANENT) {
            return mContext.getString(R.string.create_external_call_view_continuous_link_title);
        } else if (mLinkValidity == LinkValidity.SINGLE_USE) {
            return mContext.getString(R.string.create_external_call_view_unique_link_title);
        } else {
            return mContext.getString(R.string.create_external_call_view_recurrent_link_title);
        }
    }

    public String getCallCapabilities() {

        StringBuilder message = new StringBuilder();

        if (mAllowVoiceCall) {
            message.append(mContext.getString(R.string.show_contact_view_audio));
        }

        if (mAllowVideoCall) {
            if (!message.toString().isEmpty()) {
                message.append(", ");
            }
            message.append(mContext.getString(R.string.show_contact_view_video));
        }

        if (mAllowGroupCall) {
            if (!message.toString().isEmpty()) {
                message.append(", ");
            }
            message.append(mContext.getString(R.string.show_group_view_title));
        }

        return message.toString();
    }

    public List<UIConfigExternalCallItem> getConfigItems() {

        return mConfigItems;
    }

    public void updateConfigItems() {

        mConfigItems.clear();

        if (mCreateExternalCallMode) {
            mConfigItems.add(new UIConfigExternalCallItem(mContext, ConfigExternalCallSettings.CALL_TYPE));
        }

        mConfigItems.add(new UIConfigExternalCallItem(mContext, ConfigExternalCallSettings.PERMISSIONS));
        mConfigItems.add(new UIConfigExternalCallItem(mContext, ConfigExternalCallSettings.EXPIRATION));

        if (mLinkValidity != LinkValidity.PERMANENT) {
            mConfigItems.add(new UIConfigExternalCallItem(mContext, ConfigExternalCallSettings.SCHEDULE_START));
            mConfigItems.add(new UIConfigExternalCallItem(mContext, ConfigExternalCallSettings.SCHEDULE_END));

            if (mLinkValidity == LinkValidity.PERIODIC) {
                mConfigItems.add(new UIConfigExternalCallItem(mContext, ConfigExternalCallSettings.SCHEDULE_RECURRENT));
            } else {
                mConfigItems.add(new UIConfigExternalCallItem(mContext, ConfigExternalCallSettings.DELETE));
            }
        }

        if (mConfigCallType == ConfigExternalCallTypeCall.CALL_CONFERENCE) {
            mConfigItems.add(new UIConfigExternalCallItem(mContext, ConfigExternalCallSettings.NOTIFICATION));
        }
    }

    public List<UIScheduleDay> getScheduleRecurrentDays() {

        return mScheduleRecurrentDays;
    }

    public void updateDaySelected(WeeklyTimeRange.DayOfWeek dayOfWeek, boolean selected) {

        for (UIScheduleDay scheduleDay : mScheduleRecurrentDays) {
            if (scheduleDay.getDayOfWeek() == dayOfWeek) {
                scheduleDay.setSelected(selected);
                break;
            }
        }
    }

    public List<WeeklyTimeRange.DayOfWeek> getSelectedDaysOfWeek() {

        List<WeeklyTimeRange.DayOfWeek> selectedDays = new ArrayList<>();

        for (UIScheduleDay scheduleDay : mScheduleRecurrentDays) {
            if (scheduleDay.isSelected()) {
                selectedDays.add(scheduleDay.getDayOfWeek());
            }
        }

        return selectedDays;
    }

    private void initSchedule() {

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

    private void updateSchedule() {
        
        if (mScheduleStartDate == null || mScheduleStartTime == null || mScheduleEndDate == null || mScheduleEndTime == null) {
            return;
        }

        if (mScheduleStartDate.compareTo(mScheduleEndDate) > 0 || (mScheduleStartDate.compareTo(mScheduleEndDate) == 0 && mScheduleStartTime.compareTo(mScheduleEndTime) >= 0)) {
            final Calendar calendar = Calendar.getInstance();
            calendar.setTime(new java.util.Date(System.currentTimeMillis()));

            calendar.set(Calendar.YEAR, mScheduleStartDate.year);
            calendar.set(Calendar.MONTH, mScheduleStartDate.month - 1);
            calendar.set(Calendar.DAY_OF_MONTH, mScheduleStartDate.day);
            calendar.set(Calendar.HOUR_OF_DAY, mScheduleStartTime.hour);
            calendar.set(Calendar.MINUTE, mScheduleStartTime.minute);

            calendar.add(Calendar.HOUR, 1);

            mScheduleEndDate = Date.from(calendar);
            mScheduleEndTime = Time.from(calendar);
        }
    }

    private void setupDays() {

        DateFormatSymbols dateFormatSymbols = new DateFormatSymbols(Locale.getDefault());
        String[] weekDays = dateFormatSymbols.getShortWeekdays();

        if (weekDays.length != 8) {
            return;
        }

        mScheduleRecurrentDays.add(new UIScheduleDay(weekDays[2].substring(0, 1).toUpperCase(Locale.getDefault()), WeeklyTimeRange.DayOfWeek.MONDAY, false));
        mScheduleRecurrentDays.add(new UIScheduleDay(weekDays[3].substring(0, 1).toUpperCase(Locale.getDefault()), WeeklyTimeRange.DayOfWeek.TUESDAY, false));
        mScheduleRecurrentDays.add(new UIScheduleDay(weekDays[4].substring(0, 1).toUpperCase(Locale.getDefault()), WeeklyTimeRange.DayOfWeek.WEDNESDAY, false));
        mScheduleRecurrentDays.add(new UIScheduleDay(weekDays[5].substring(0, 1).toUpperCase(Locale.getDefault()), WeeklyTimeRange.DayOfWeek.THURSDAY, false));
        mScheduleRecurrentDays.add(new UIScheduleDay(weekDays[6].substring(0, 1).toUpperCase(Locale.getDefault()), WeeklyTimeRange.DayOfWeek.FRIDAY, false));
        mScheduleRecurrentDays.add(new UIScheduleDay(weekDays[7].substring(0, 1).toUpperCase(Locale.getDefault()), WeeklyTimeRange.DayOfWeek.SATURDAY, false));
        mScheduleRecurrentDays.add(new UIScheduleDay(weekDays[1].substring(0, 1).toUpperCase(Locale.getDefault()), WeeklyTimeRange.DayOfWeek.SUNDAY, false));
    }
}
