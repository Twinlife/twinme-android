/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.settingsActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.TwinmeApplication;
import org.twinlife.twinme.ui.spaces.ResetSettingsViewHolder;
import org.twinlife.twinme.utils.SectionTitleViewHolder;

public class DebugSettingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "SettingsAdvancedAdapter";
    private static final boolean DEBUG = false;

    @NonNull
    private final DebugSettingsActivity mActivity;

    private final int ITEM_COUNT;
    private static final int TITLE = 0;
    private static final int CHECKBOX = 1;
    private static final int RESET = 2;

    DebugSettingsAdapter(@NonNull DebugSettingsActivity listActivity) {

        mActivity = listActivity;
        ITEM_COUNT = TwinmeApplication.OnboardingType.values().length + 2;
        setHasStableIds(false);
    }

    @Override
    public int getItemCount() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemCount");
        }

        return ITEM_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemViewType: " + position);
        }

        if (position == 0) {
            return TITLE;
        } else if (position == ITEM_COUNT - 1) {
            return RESET;
        } else {
            return CHECKBOX;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        int viewType = getItemViewType(position);

        if (viewType == TITLE) {
            SectionTitleViewHolder sectionTitleViewHolder = (SectionTitleViewHolder) viewHolder;
            sectionTitleViewHolder.onBind(mActivity.getString(R.string.application_do_not_display), false);
        } else if (viewType == CHECKBOX) {
            SettingsAdvancedViewHolder settingsViewHolder = (SettingsAdvancedViewHolder) viewHolder;
            CompoundButton.OnCheckedChangeListener onCheckedChangeListener = (compoundButton, value) -> mActivity.getTwinmeApplication().setShowOnboardingType(TwinmeApplication.OnboardingType.values()[position - 1], value);
            settingsViewHolder.onBind(getOnboardingTitle(position - 1), isStartOnboarding(position - 1), true, onCheckedChangeListener );
        } else if (viewType == RESET) {
            ResetSettingsViewHolder resetSettingsViewHolder = (ResetSettingsViewHolder) viewHolder;
            resetSettingsViewHolder.itemView.setOnClickListener(view -> onResetSettings());
        }
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mActivity.getLayoutInflater();
        View convertView;

        if (viewType == TITLE) {
            convertView = inflater.inflate(R.layout.section_title_item, parent, false);
            return new SectionTitleViewHolder(convertView);
        } else if (viewType == CHECKBOX) {
            convertView = inflater.inflate(R.layout.settings_advanced_item, parent, false);
            return new SettingsAdvancedViewHolder(convertView);
        } else {
            convertView = inflater.inflate(R.layout.space_appearance_activity_reset_appearance_item, parent, false);
            ViewGroup.LayoutParams layoutParams = convertView.getLayoutParams();
            layoutParams.height = Design.ITEM_VIEW_HEIGHT;
            convertView.setLayoutParams(layoutParams);
            return new ResetSettingsViewHolder(convertView);
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }
    }

    private String getOnboardingTitle(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getOnboardingTitle: " + position);
        }

        switch (TwinmeApplication.OnboardingType.values()[position]) {
            case CERTIFIED_RELATION:
                return mActivity.getString(R.string.authentified_relation_activity_title);

            case EXTERNAL_CALL:
                return mActivity.getString(R.string.premium_services_activity_click_to_call_title);

            case PROFILE:
                return mActivity.getString(R.string.application_profile);

            case SPACE:
                return mActivity.getString(R.string.premium_services_activity_space_title);

            case TRANSFER:
                return mActivity.getString(R.string.account_activity_transfer_between_devices);

            case TRANSFER_CALL:
                return mActivity.getString(R.string.premium_services_activity_transfert_title);

            case ENTER_MINI_CODE:
                return mActivity.getString(R.string.enter_invitation_code_activity_enter_code);

            case MINI_CODE:
                return mActivity.getString(R.string.invitation_code_activity_create_code);

            case REMOTE_CAMERA:
                return mActivity.getString(R.string.call_activity_camera_control);

            case REMOTE_CAMERA_SETTING:
                return mActivity.getString(R.string.call_activity_camera_control) + " - " + mActivity.getString(R.string.navigation_activity_settings);

            case PROXY:
                return mActivity.getString(R.string.proxy_activity_title);

            default:
                return "";
        }
    }

    private boolean isStartOnboarding(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "isStartOnboarding: " + position);
        }

        return mActivity.getTwinmeApplication().startOnboarding(TwinmeApplication.OnboardingType.values()[position]);
    }

    private void onResetSettings() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResetSettings");
        }

        mActivity.getTwinmeApplication().resetOnboarding();
        notifyItemRangeChanged(1, ITEM_COUNT - 2);
    }
}