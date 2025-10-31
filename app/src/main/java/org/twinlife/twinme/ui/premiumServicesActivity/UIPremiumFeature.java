/*
 *  Copyright (c) 2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.premiumServicesActivity;

import android.content.Context;
import android.content.res.Configuration;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.DisplayMode;
import org.twinlife.twinme.ui.Settings;

import java.util.ArrayList;
import java.util.List;

public class UIPremiumFeature {

    public enum FeatureType {
        CLICK_TO_CALL,
        CONVERSATION,
        GROUP_CALL,
        SPACES,
        STREAMING,
        TRANSFER_CALL,
        CAMERA_CONTROL
    }

    private final FeatureType mFeatureType;

    private final List<UIPremiumFeatureDetail> mFeatureDetails;

    private String mTitle;
    private String mSubTitle;

    private int mImageId;

    public UIPremiumFeature(Context context, FeatureType featureType) {

        mFeatureType = featureType;
        mFeatureDetails = new ArrayList<>();
        initFeatureDetails(context);
    }

    public String getTitle() {

        return mTitle;
    }

    public String getSubTitle() {

        return mSubTitle;
    }

    public int getImageId() {

        return mImageId;
    }

    public FeatureType getFeatureType() {

        return mFeatureType;
    }

    public List<UIPremiumFeatureDetail> getFeatureDetails() {

        return mFeatureDetails;
    }

    private void initFeatureDetails(Context context) {

        boolean darkMode = false;
        int currentNightMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        int displayMode = Settings.displayMode.getInt();
        if ((currentNightMode == Configuration.UI_MODE_NIGHT_YES && displayMode == DisplayMode.SYSTEM.ordinal())  || displayMode == DisplayMode.DARK.ordinal()) {
            darkMode = true;
        }

        switch (mFeatureType) {
            case CLICK_TO_CALL:
                mTitle = context.getString(R.string.premium_services_activity_click_to_call_title);
                mSubTitle = context.getString(R.string.premium_services_activity_click_to_call_subtitle);
                mImageId = darkMode ? R.drawable.premium_feature_click_to_call_dark : R.drawable.premium_feature_click_to_call;
                mFeatureDetails.add(new UIPremiumFeatureDetail(context.getString(R.string.premium_services_activity_click_to_call_description_1), darkMode ? R.drawable.premium_click_to_call_dark_icon_1 : R.drawable.premium_click_to_call_icon_1));
                mFeatureDetails.add(new UIPremiumFeatureDetail(context.getString(R.string.premium_services_activity_click_to_call_description_2), darkMode ? R.drawable.premium_click_to_call_dark_icon_2 : R.drawable.premium_click_to_call_icon_2));
                mFeatureDetails.add(new UIPremiumFeatureDetail(context.getString(R.string.premium_services_activity_click_to_call_description_3), darkMode ? R.drawable.premium_click_to_call_dark_icon_3 : R.drawable.premium_click_to_call_icon_3));
                mFeatureDetails.add(new UIPremiumFeatureDetail(context.getString(R.string.premium_services_activity_click_to_call_description_4), darkMode ? R.drawable.premium_click_to_call_dark_icon_4 : R.drawable.premium_click_to_call_icon_4));
                break;

            case CONVERSATION:
                mTitle = context.getString(R.string.premium_services_activity_conversation_title);
                mSubTitle = context.getString(R.string.premium_services_activity_conversation_subtitle);
                mImageId = darkMode ? R.drawable.premium_feature_conversation_dark : R.drawable.premium_feature_conversation;
                mFeatureDetails.add(new UIPremiumFeatureDetail(context.getString(R.string.premium_services_activity_conversation_description_1), darkMode ? R.drawable.premium_conversation_dark_icon_1 : R.drawable.premium_conversation_icon_1));
                mFeatureDetails.add(new UIPremiumFeatureDetail(context.getString(R.string.premium_services_activity_conversation_description_2), darkMode ? R.drawable.premium_conversation_dark_icon_2 : R.drawable.premium_conversation_icon_2));
                mFeatureDetails.add(new UIPremiumFeatureDetail(context.getString(R.string.premium_services_activity_conversation_description_3), darkMode ? R.drawable.premium_conversation_dark_icon_3 : R.drawable.premium_conversation_icon_3));
                mFeatureDetails.add(new UIPremiumFeatureDetail(context.getString(R.string.premium_services_activity_conversation_description_4), darkMode ? R.drawable.premium_conversation_dark_icon_4 : R.drawable.premium_conversation_icon_4));
                break;

            case GROUP_CALL:
                mTitle = context.getString(R.string.premium_services_activity_group_call_title);
                mSubTitle = context.getString(R.string.premium_services_activity_group_call_subtitle);
                mImageId = darkMode ? R.drawable.premium_feature_group_call_dark : R.drawable.premium_feature_group_call;
                mFeatureDetails.add(new UIPremiumFeatureDetail(context.getString(R.string.premium_services_activity_group_call_description_1), darkMode ? R.drawable.premium_group_call_dark_icon_1 : R.drawable.premium_group_call_icon_1));
                mFeatureDetails.add(new UIPremiumFeatureDetail(context.getString(R.string.premium_services_activity_group_call_description_2), darkMode ? R.drawable.premium_group_call_dark_icon_2 : R.drawable.premium_group_call_icon_2));
                mFeatureDetails.add(new UIPremiumFeatureDetail(context.getString(R.string.premium_services_activity_group_call_description_3), darkMode ? R.drawable.premium_group_call_dark_icon_3 : R.drawable.premium_group_call_icon_3));
                mFeatureDetails.add(new UIPremiumFeatureDetail(context.getString(R.string.premium_services_activity_group_call_description_4), darkMode ? R.drawable.premium_privacy_dark_icon_1 : R.drawable.premium_privacy_icon_1));
                break;

            case SPACES:
                mTitle = context.getString(R.string.premium_services_activity_space_title);
                mSubTitle = context.getString(R.string.premium_services_activity_space_subtitle);
                mImageId = darkMode ? R.drawable.premium_feature_space_dark : R.drawable.premium_feature_space;
                mFeatureDetails.add(new UIPremiumFeatureDetail(context.getString(R.string.premium_services_activity_space_description_1), darkMode ? R.drawable.premium_space_dark_icon_1 : R.drawable.premium_space_icon_1));
                mFeatureDetails.add(new UIPremiumFeatureDetail(context.getString(R.string.premium_services_activity_space_description_2), darkMode ? R.drawable.premium_space_dark_icon_2 : R.drawable.premium_space_icon_2));
                mFeatureDetails.add(new UIPremiumFeatureDetail(context.getString(R.string.premium_services_activity_space_description_3), darkMode ? R.drawable.premium_space_dark_icon_3 : R.drawable.premium_space_icon_3));
                mFeatureDetails.add(new UIPremiumFeatureDetail(context.getString(R.string.premium_services_activity_space_description_4), darkMode ? R.drawable.premium_space_dark_icon_4 : R.drawable.premium_space_icon_4));
                break;

            case STREAMING:
                mTitle = context.getString(R.string.premium_services_activity_streaming_title);
                mSubTitle = context.getString(R.string.premium_services_activity_streaming_subtitle);
                mImageId = darkMode ? R.drawable.premium_feature_streaming_dark : R.drawable.premium_feature_streaming;
                mFeatureDetails.add(new UIPremiumFeatureDetail(context.getString(R.string.premium_services_activity_streaming_description_1), darkMode ? R.drawable.premium_streaming_dark_icon_1 : R.drawable.premium_streaming_icon_1));
                mFeatureDetails.add(new UIPremiumFeatureDetail(context.getString(R.string.premium_services_activity_streaming_description_2), darkMode ? R.drawable.premium_streaming_dark_icon_2 : R.drawable.premium_streaming_icon_2));
                mFeatureDetails.add(new UIPremiumFeatureDetail(context.getString(R.string.premium_services_activity_streaming_description_3), darkMode ? R.drawable.premium_streaming_dark_icon_3 : R.drawable.premium_streaming_icon_3));
                mFeatureDetails.add(new UIPremiumFeatureDetail(context.getString(R.string.premium_services_activity_streaming_description_4), darkMode ? R.drawable.premium_streaming_dark_icon_4 : R.drawable.premium_streaming_icon_4));
                break;

            case TRANSFER_CALL:
                mTitle = context.getString(R.string.premium_services_activity_transfert_title);
                mSubTitle = context.getString(R.string.premium_services_activity_transfert_subtitle);
                mImageId = R.drawable.premium_feature_transfert_call;
                mFeatureDetails.add(new UIPremiumFeatureDetail(context.getString(R.string.premium_services_activity_transfert_description_1), darkMode ? R.drawable.premium_transfert_call_dark_icon_1 : R.drawable.premium_transfert_call_icon_1));
                mFeatureDetails.add(new UIPremiumFeatureDetail(context.getString(R.string.premium_services_activity_transfert_description_2), darkMode ? R.drawable.premium_transfert_call_dark_icon_2 : R.drawable.premium_transfert_call_icon_2));
                mFeatureDetails.add(new UIPremiumFeatureDetail(context.getString(R.string.premium_services_activity_transfert_description_3), darkMode ? R.drawable.premium_transfert_call_dark_icon_3 : R.drawable.premium_transfert_call_icon_3));
                mFeatureDetails.add(new UIPremiumFeatureDetail(context.getString(R.string.premium_services_activity_transfert_description_4), darkMode ? R.drawable.premium_transfert_call_dark_icon_4 : R.drawable.premium_transfert_call_icon_4));
                break;

            case CAMERA_CONTROL:
                mTitle = context.getString(R.string.premium_services_activity_camera_control_title);
                mSubTitle = context.getString(R.string.premium_services_activity_camera_control_subtitle);
                mImageId = R.drawable.onboarding_control_camera;
                mFeatureDetails.add(new UIPremiumFeatureDetail(context.getString(R.string.premium_services_activity_camera_control_description_1), darkMode ? R.drawable.premium_camera_control_dark_icon_1 : R.drawable.premium_camera_control_icon_1));
                mFeatureDetails.add(new UIPremiumFeatureDetail(context.getString(R.string.premium_services_activity_camera_control_description_2), R.drawable.premium_camera_control_icon_2));
                mFeatureDetails.add(new UIPremiumFeatureDetail(context.getString(R.string.premium_services_activity_camera_control_description_3), R.drawable.premium_camera_control_icon_3));
                mFeatureDetails.add(new UIPremiumFeatureDetail(context.getString(R.string.premium_services_activity_camera_control_description_4), R.drawable.premium_camera_control_icon_4));
                break;
        }
    }
}
