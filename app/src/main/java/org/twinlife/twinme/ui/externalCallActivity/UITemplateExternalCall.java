/*
 *  Copyright (c) 2023-2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.externalCallActivity;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.Nullable;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.models.LinkValidity;
import org.twinlife.twinme.models.schedule.Time;
import org.twinlife.twinme.models.schedule.WeeklyTimeRange;
import org.twinlife.twinme.ui.externalCallActivity.UIConfigExternalCall.ConfigExternalCallTypeCall;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UITemplateExternalCall extends UITemplateItem {

    public enum TemplateType {
        CLASSIFIED_AD,
        HELP,
        JOB,
        MEETING,
        VIDEO_BELL,
        PROFILE,
        OTHER
    }

    private final TemplateType mTemplateType;

    @Nullable
    private String mName;
    private String mPlaceholder;
    @Nullable
    private String mMessage;
    private int mAvatarId;

    @Nullable
    private Bitmap mAvatar;

    private String mAvatarUrl;

    private boolean mAllowVoiceCall;
    private boolean mAllowVideoCall;
    private boolean mAllowGroupCall;
    private ConfigExternalCallTypeCall mTypeCall;
    private LinkValidity mLinkValidity;

    @Nullable
    private Time mScheduleStartTime;
    @Nullable
    private Time mScheduleEndTime;
    @Nullable
    private List<WeeklyTimeRange.DayOfWeek> mScheduleDays;

    public UITemplateExternalCall(Context context, TemplateType templateType) {
        super();

        mTemplateItemType = TemplateItemType.TEMPLATE;
        mTemplateType = templateType;
        initTemplateInformation(context);
    }

    public TemplateType getTemplateType() {

        return mTemplateType;
    }

    public String getName() {

        return mName;
    }

    public String getPlaceholder() {

        return mPlaceholder;
    }

    public String getMessage() {

        return mMessage;
    }

    public int getAvatarId() {

        return mAvatarId;
    }

    public String getAvatarUrl() {

        return mAvatarUrl;
    }

    public boolean voiceCallAllowed() {

        return mAllowVoiceCall;
    }

    public boolean videoCallAllowed() {

        return mAllowVideoCall;
    }

    public boolean groupCallAllowed() {

        return mAllowGroupCall;
    }

    @Nullable
    public Bitmap getAvatar() {

        return mAvatar;
    }

    public void setName(String name) {

        mName = name;
    }

    public void setAvatar(Bitmap avatar) {

        mAvatar = avatar;
    }

    public ConfigExternalCallTypeCall getTypeCall() {

        return mTypeCall;
    }

    public LinkValidity getLinkValidity() {

        return mLinkValidity;
    }

    public Time getScheduleStartTime() {

        return mScheduleStartTime;
    }

    public Time getScheduleEndTime() {

        return mScheduleEndTime;
    }

    public List<WeeklyTimeRange.DayOfWeek> getScheduleDays() {

        return mScheduleDays;
    }

    private void initTemplateInformation(Context context) {

        switch (mTemplateType) {
            case CLASSIFIED_AD:
                mName = context.getString(R.string.template_click_to_call_view_template_classified_ad);
                mPlaceholder = context.getString(R.string.template_click_to_call_view_template_classified_ad_placeholder);
                mMessage = context.getString(R.string.template_click_to_call_view_template_classified_ad_description);
                mAvatarId = R.drawable.click_to_call_sample_classified_ad;
                mAvatarUrl = "https://twin.me/download/click_to_call_sample_classified_ad_2026.jpg";
                mTypeCall = ConfigExternalCallTypeCall.CALL_DIRECT;
                mLinkValidity = LinkValidity.PERMANENT;
                mAllowVoiceCall = true;
                mAllowVideoCall = false;
                mAllowGroupCall = false;
                mScheduleStartTime = null;
                mScheduleEndTime = null;
                mScheduleDays = null;
                break;

            case HELP:
                mName = context.getString(R.string.template_click_to_call_view_template_help);
                mPlaceholder = context.getString(R.string.template_click_to_call_view_template_help_placeholder);
                mMessage = context.getString(R.string.template_click_to_call_view_template_help_description);
                mAvatarId = R.drawable.click_to_call_sample_help;
                mAvatarUrl = "https://twin.me/download/click_to_call_sample_help_2026.jpg";
                mTypeCall = ConfigExternalCallTypeCall.CALL_DIRECT;
                mLinkValidity = LinkValidity.PERIODIC;
                mAllowVoiceCall = true;
                mAllowVideoCall = false;
                mAllowGroupCall = false;
                mScheduleStartTime = Time.from("10:00");
                mScheduleEndTime = Time.from("18:00");
                mScheduleDays = new ArrayList<>(Arrays.asList(
                        WeeklyTimeRange.DayOfWeek.MONDAY,
                        WeeklyTimeRange.DayOfWeek.TUESDAY,
                        WeeklyTimeRange.DayOfWeek.WEDNESDAY,
                        WeeklyTimeRange.DayOfWeek.THURSDAY,
                        WeeklyTimeRange.DayOfWeek.FRIDAY
                ));
                break;

            case JOB:
                mName = context.getString(R.string.template_click_to_call_view_template_job);
                mPlaceholder = context.getString(R.string.template_click_to_call_view_template_job_placeholder);
                mMessage = context.getString(R.string.template_click_to_call_view_template_help_description);
                mAvatarId = R.drawable.click_to_call_sample_job;
                mAvatarUrl = "https://twin.me/download/click_to_call_sample_job_2026.jpg";
                mTypeCall = ConfigExternalCallTypeCall.CALL_DIRECT;
                mLinkValidity = LinkValidity.PERMANENT;
                mAllowVoiceCall = true;
                mAllowVideoCall = false;
                mAllowGroupCall = false;
                mScheduleStartTime = null;
                mScheduleEndTime = null;
                mScheduleDays = null;

                break;

            case MEETING:
                mName = context.getString(R.string.template_click_to_call_view_template_meeting);
                mPlaceholder = context.getString(R.string.template_click_to_call_view_template_meeting_placeholder);
                mMessage = context.getString(R.string.template_click_to_call_view_template_meeting_description);
                mAvatarId = R.drawable.click_to_call_sample_meeting;
                mAvatarUrl = "https://twin.me/download/click_to_call_sample_meeting_2026.jpg";
                mTypeCall = ConfigExternalCallTypeCall.CALL_CONFERENCE;
                mLinkValidity = LinkValidity.SINGLE_USE;
                mAllowVoiceCall = true;
                mAllowVideoCall = true;
                mAllowGroupCall = true;
                mScheduleStartTime = null;
                mScheduleEndTime = null;
                mScheduleDays = null;
                break;

            case VIDEO_BELL:
                mName = context.getString(R.string.template_click_to_call_view_template_video_bell);
                mPlaceholder = context.getString(R.string.template_click_to_call_view_template_video_bell_placeholder);
                mMessage = context.getString(R.string.template_click_to_call_view_template_video_bell_description);
                mAvatarId = R.drawable.click_to_call_sample_video_bell;
                mAvatarUrl = "https://twin.me/download/click_to_call_sample_video_bell.jpg";
                mTypeCall = ConfigExternalCallTypeCall.CALL_DIRECT;
                mLinkValidity = LinkValidity.PERMANENT;
                mAllowVoiceCall = true;
                mAllowVideoCall = true;
                mAllowGroupCall = false;
                mScheduleStartTime = null;
                mScheduleEndTime = null;
                mScheduleDays = null;
                break;

            case PROFILE:
                mName = context.getString(R.string.premium_services_view_click_to_call_title);
                mPlaceholder = context.getString(R.string.create_external_call_view_placeholder);
                mMessage = context.getString(R.string.template_click_to_call_view_template_profile_description);
                mAvatarId = -1;
                mAvatarUrl = null;
                mTypeCall = ConfigExternalCallTypeCall.CALL_DIRECT;
                mLinkValidity = LinkValidity.PERMANENT;
                mAllowVoiceCall = true;
                mAllowVideoCall = true;
                mAllowGroupCall = false;
                mScheduleStartTime = null;
                mScheduleEndTime = null;
                mScheduleDays = null;
                break;

            case OTHER:
                mName = context.getString(R.string.premium_services_view_click_to_call_title);
                mPlaceholder = context.getString(R.string.create_external_call_view_placeholder);
                mMessage = context.getString(R.string.template_click_to_call_view_template_default_description);
                mAvatarId = -1;
                mAvatarUrl = null;
                mTypeCall = ConfigExternalCallTypeCall.CALL_DIRECT;
                mLinkValidity = LinkValidity.PERMANENT;
                mAllowVoiceCall = true;
                mAllowVideoCall = true;
                mAllowGroupCall = false;
                mScheduleStartTime = null;
                mScheduleEndTime = null;
                mScheduleDays = null;
                break;

            default:
                break;
        }
    }
}
