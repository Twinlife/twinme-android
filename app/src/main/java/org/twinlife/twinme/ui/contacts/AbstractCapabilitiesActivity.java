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
import android.view.ViewGroup;

import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.ColorUtils;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.models.Capabilities;
import org.twinlife.twinme.models.Zoomable;
import org.twinlife.twinme.models.schedule.Date;
import org.twinlife.twinme.models.schedule.Time;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.TwinmeApplication;
import org.twinlife.twinme.ui.premiumServicesActivity.PremiumFeatureConfirmView;
import org.twinlife.twinme.ui.premiumServicesActivity.UIPremiumFeature;
import org.twinlife.twinme.utils.AbstractBottomSheetView;
import org.twinlife.twinme.utils.OnboardingConfirmView;

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

        AbstractBottomSheetView.Observer observer = new AbstractBottomSheetView.Observer() {
            @Override
            public void onConfirmClick() {
                premiumFeatureConfirmView.redirectStore();
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

    public void showOnboardingView(boolean hideCancelAction) {
        if (DEBUG) {
            Log.d(LOG_TAG, "showOnboardingView");
        }

        ViewGroup viewGroup = findViewById(R.id.capabilities_activity_layout);

        OnboardingConfirmView onboardingConfirmView = new OnboardingConfirmView(this, null);
        onboardingConfirmView.setImage(ResourcesCompat.getDrawable(getResources(), R.drawable.onboarding_control_camera, null));
        onboardingConfirmView.setTitle(getString(R.string.call_activity_camera_control_needs_help));
        onboardingConfirmView.setMessage(getString(R.string.contact_capabilities_activity_camera_control_onboarding));
        onboardingConfirmView.setConfirmTitle(getString(R.string.application_ok));
        onboardingConfirmView.setCancelTitle(getString(R.string.application_do_not_display));

        if (hideCancelAction) {
            onboardingConfirmView.hideCancelView();
        }

        AbstractBottomSheetView.Observer observer = new AbstractBottomSheetView.Observer() {
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
                    showPremiumFeatureAlert(UIPremiumFeature.FeatureType.CAMERA_CONTROL);
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

        if (!mUIInitialized) {
            return;
        }

        if (mCapabilities.hasAudio() == mAllowAudioCall && mCapabilities.hasVideo() == mAllowVideoCall) {
            if (!mCanSave) {
                return;
            }
            mCanSave = false;
        } else {
            if (mCanSave) {
                return;
            }
            mCanSave = true;
        }
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

        if (getTwinmeApplication().startOnboarding(TwinmeApplication.OnboardingType.REMOTE_CAMERA_SETTING)) {
            showOnboardingView(false);
        } else {
            showPremiumFeatureAlert(UIPremiumFeature.FeatureType.CAMERA_CONTROL);
        }
    }
}
