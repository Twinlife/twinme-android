/*
 *  Copyright (c) 2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.externalCallActivity;

import android.content.Context;

import androidx.annotation.Nullable;

import org.twinlife.device.android.twinme.R;

public class UITemplateExternalCall {

    public enum TemplateType {
        CLASSIFIED_AD,
        HELP,
        MEETING,
        VIDEO_BELL,
        OTHER
    }

    private final TemplateType mTemplateType;

    private static int sItemId = 0;

    private final long mItemId;
    @Nullable
    private String mName;
    private String mPlaceholder;
    private int mAvatarId;
    private String mAvatarUrl;

    private boolean mAllowVoiceCall;
    private boolean mAllowVideoCall;
    private boolean mAllowGroupCall;
    private boolean mEnableSchedule;

    public UITemplateExternalCall(Context context, TemplateType templateType) {

        mItemId = sItemId++;

        mTemplateType = templateType;
        initTemplateInformation(context);
    }

    public long getItemId() {

        return mItemId;
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

    public boolean hasSchedule() {

        return mEnableSchedule;
    }

    private void initTemplateInformation(Context context) {

        switch (mTemplateType) {
            case CLASSIFIED_AD:
                mName = context.getString(R.string.template_click_to_call_activity_template_classified_ad);
                mPlaceholder = context.getString(R.string.template_click_to_call_activity_template_classified_ad_placeholder);
                mAvatarId = R.drawable.click_to_call_sample_classified_ad;
                mAvatarUrl = "https://twin.me/download/click_to_call_sample_classified_ad.jpg";
                mEnableSchedule = false;
                mAllowVoiceCall = true;
                mAllowVideoCall = false;
                mAllowGroupCall = false;
                break;

            case HELP:
                mName = context.getString(R.string.template_click_to_call_activity_template_help);
                mPlaceholder = context.getString(R.string.template_click_to_call_activity_template_help_placeholder);
                mAvatarId = R.drawable.click_to_call_sample_help;
                mAvatarUrl = "https://twin.me/download/click_to_call_sample_help.jpg";
                mEnableSchedule = false;
                mAllowVoiceCall = true;
                mAllowVideoCall = false;
                mAllowGroupCall = false;
                break;

            case MEETING:
                mName = context.getString(R.string.template_click_to_call_activity_template_meeting);
                mPlaceholder = context.getString(R.string.template_click_to_call_activity_template_meeting_placeholder);
                mAvatarId = R.drawable.click_to_call_sample_meeting;
                mAvatarUrl = "https://twin.me/download/click_to_call_sample_meeting.jpg";
                mEnableSchedule = true;
                mAllowVoiceCall = true;
                mAllowVideoCall = true;
                mAllowGroupCall = true;
                break;

            case VIDEO_BELL:
                mName = context.getString(R.string.template_click_to_call_activity_template_video_bell);
                mPlaceholder = context.getString(R.string.template_click_to_call_activity_template_video_bell_placeholder);
                mAvatarId = R.drawable.click_to_call_sample_video_bell;
                mAvatarUrl = "https://twin.me/download/click_to_call_sample_video_bell.jpg";
                mEnableSchedule = false;
                mAllowVoiceCall = true;
                mAllowVideoCall = true;
                mAllowGroupCall = false;
                break;

            case OTHER:
                mName = context.getString(R.string.template_space_activity_template_other);
                mPlaceholder = context.getString(R.string.create_external_call_activity_placeholder);
                mAvatarId = -1;
                mAvatarUrl = null;
                mEnableSchedule = false;
                mAllowVoiceCall = true;
                mAllowVideoCall = true;
                mAllowGroupCall = false;
                break;

            default:
                break;
        }
    }


}
