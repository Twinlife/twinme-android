/*
 *  Copyright (c) 2023-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.contacts;

import android.os.Bundle;
import android.util.Log;

import org.twinlife.twinme.models.Capabilities;
import org.twinlife.twinme.models.Zoomable;
import org.twinlife.twinme.models.schedule.Date;
import org.twinlife.twinme.models.schedule.DateTime;
import org.twinlife.twinme.models.schedule.DateTimeRange;
import org.twinlife.twinme.models.schedule.Schedule;
import org.twinlife.twinme.models.schedule.Time;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;

import java.util.TimeZone;

public class AbstractCapabilitiesActivity extends AbstractTwinmeActivity {
    private static final String LOG_TAG = "AbstractCapabilities...";
    private static final boolean DEBUG = false;

    public enum CapabilitiesType {
        CONTACT,
        CALL_RECEIVER
    }

    public enum ScheduleType {
        START,
        END
    }

    public static final int VOICE_CALL_SWITCH = 1;
    public static final int VIDEO_CALL_SWITCH = 2;
    public static final int DISCREET_RELATION_SWITCH = 3;
    public static final int SCHEDULE_SWITCH = 4;
    public static final int ANSWERING_AUTOMATIC_SWITCH = 5;

    protected CapabilitiesAdapter mCapabilitiesAdapter;

    protected Capabilities mCapabilities;

    protected boolean mAllowAudioCall = true;
    protected boolean mAllowVideoCall = true;
    protected boolean mDiscreetRelation = false;
    protected boolean mScheduleEnable = false;
    protected Zoomable mZoomable = Zoomable.ASK;
    protected boolean mAllowAnsweringAutomatic = false;

    protected Date mScheduleStartDate;
    protected Time mScheduleStartTime;
    protected Date mScheduleEndDate;
    protected Time mScheduleEndTime;

    protected boolean mCanSave = false;

    protected boolean mUIInitialized = false;
    protected boolean mUIPostInitialized = false;

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
    }

    public boolean allowAudioCall() {

        return mAllowAudioCall;
    }

    public boolean allowVideoCall() {

        return mAllowVideoCall;
    }

    public boolean discreetRelation() {

        return mDiscreetRelation;
    }

    public boolean scheduleEnable() {

        return mScheduleEnable;
    }

    public Date getScheduleStartDate() {

        return mScheduleStartDate;
    }

    public Date getScheduleEndDate() {

        return mScheduleEndDate;
    }

    public Time getScheduleStartTime() {

        return mScheduleStartTime;
    }

    public Time getScheduleEndTime() {

        return mScheduleEndTime;
    }

    public boolean allowAnsweringAutomatic() {

        return mAllowAnsweringAutomatic;
    }

    public CapabilitiesType getCapabilitiesType() {

        return CapabilitiesType.CONTACT;
    }

    public Zoomable getZoomable() {

        return mZoomable;
    }

    public boolean isGroup() {

        return false;
    }

    //
    // Override Activity methods
    //

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onWindowFocusChanged: hasFocus=" + hasFocus);
        }

        if (hasFocus && mUIInitialized && !mUIPostInitialized) {
            postInitViews();
        }
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        super.onDestroy();
    }

    public void onSettingChangeValue(int switchTag, boolean value) {

        switch (switchTag) {
            case VOICE_CALL_SWITCH:
                mAllowAudioCall = value;
                break;

            case VIDEO_CALL_SWITCH:
                mAllowVideoCall = value;
                break;

            case DISCREET_RELATION_SWITCH:
                mDiscreetRelation = value;
                break;

            case SCHEDULE_SWITCH:
                mScheduleEnable = value;
                if (mScheduleEnable && mScheduleStartDate == null) {
                    initSchedule();
                }
                break;

            case ANSWERING_AUTOMATIC_SWITCH:
                mAllowAnsweringAutomatic = value;
                break;

            default:
                break;
        }

        mCapabilitiesAdapter.notifyDataSetChanged();
        setUpdated();
    }

    //
    // Private methods
    //

    protected void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }
    }

    private void postInitViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "postInitViews");
        }

        mUIPostInitialized = true;
    }

    protected void setUpdated() {
        if (DEBUG) {
            Log.d(LOG_TAG, "setUpdated");
        }
    }

    protected Schedule createSchedule(){
        DateTime start = new DateTime(mScheduleStartDate, mScheduleStartTime);
        DateTime end = new DateTime(mScheduleEndDate, mScheduleEndTime);
        Schedule schedule = new Schedule(TimeZone.getDefault(), new DateTimeRange(start, end));
        schedule.setEnabled(mScheduleEnable);
        return schedule;
    }

    protected void initSchedule() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initSchedule");
        }
    }

    protected void updateSchedule() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateSchedule");
        }
    }

    protected void onDateClick(ScheduleType scheduleType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDateClick");
        }
    }

    protected void onTimeClick(ScheduleType scheduleType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onTimeClick");
        }
    }

    protected void onSelectControlCamera() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSelectControlCamera");
        }
    }

    protected void showOnboardingView(boolean hideCancelAction) {
        if (DEBUG) {
            Log.d(LOG_TAG, "showOnboardingView");
        }
    }
}
