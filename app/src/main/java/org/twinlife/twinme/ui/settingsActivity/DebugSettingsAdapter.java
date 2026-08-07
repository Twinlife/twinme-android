/*
 *  Copyright (c) 2025-2026 twinlife SA.
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

import java.util.ArrayList;
import java.util.List;

public class DebugSettingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "SettingsAdvancedAdapter";
    private static final boolean DEBUG = false;

    @NonNull
    private final DebugSettingsActivity mActivity;

    private static final int TITLE = 0;
    private static final int CHECKBOX = 1;
    private static final int RESET = 2;

    private final List<UIDebugItem> mItems = new ArrayList<>();

    DebugSettingsAdapter(@NonNull DebugSettingsActivity listActivity) {

        mActivity = listActivity;
        setHasStableIds(false);
        initItems();
    }

    @Override
    public int getItemCount() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemCount");
        }

        return mItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemViewType: " + position);
        }

        UIDebugItem item = mItems.get(position);

        switch (item.getType()) {
            case SECTION:
                return TITLE;

            case ONBOARDING:
                return CHECKBOX;

            case RESET:
                return RESET;

            default:
                return -1;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        int viewType = getItemViewType(position);
        UIDebugItem item = mItems.get(position);
        if (viewType == TITLE) {
            SectionTitleViewHolder sectionTitleViewHolder = (SectionTitleViewHolder) viewHolder;
            sectionTitleViewHolder.onBind(item.getText(), false);
        } else if (viewType == CHECKBOX) {
            SettingsAdvancedViewHolder settingsViewHolder = (SettingsAdvancedViewHolder) viewHolder;
            CompoundButton.OnCheckedChangeListener onCheckedChangeListener = (compoundButton, value) -> mActivity.getTwinmeApplication().setShowOnboardingType(item.getOnboardingType(), value);
            settingsViewHolder.onBind(item.getText(), mActivity.getTwinmeApplication().startOnboarding(item.getOnboardingType()), true, onCheckedChangeListener );
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


    private void onResetSettings() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResetSettings");
        }

        mActivity.getTwinmeApplication().resetOnboarding();
        notifyItemRangeChanged(1, mItems.size() - 1);
    }

    private void initItems() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initItems");
        }

        mItems.clear();
        mItems.add(new UIDebugItem(UIDebugItem.DebugItemType.SECTION, null, mActivity.getString(R.string.application_do_not_display)));
        mItems.add(new UIDebugItem(UIDebugItem.DebugItemType.ONBOARDING, TwinmeApplication.OnboardingType.CERTIFIED_RELATION, mActivity.getString(R.string.authentified_relation_view_title)));
        mItems.add(new UIDebugItem(UIDebugItem.DebugItemType.ONBOARDING, TwinmeApplication.OnboardingType.EXTERNAL_CALL, mActivity.getString(R.string.premium_services_view_click_to_call_title)));
        mItems.add(new UIDebugItem(UIDebugItem.DebugItemType.ONBOARDING, TwinmeApplication.OnboardingType.PROFILE, mActivity.getString(R.string.application_profile)));
        mItems.add(new UIDebugItem(UIDebugItem.DebugItemType.ONBOARDING, TwinmeApplication.OnboardingType.SPACE, mActivity.getString(R.string.premium_services_view_space_title)));
        mItems.add(new UIDebugItem(UIDebugItem.DebugItemType.ONBOARDING, TwinmeApplication.OnboardingType.TRANSFER, mActivity.getString(R.string.account_view_transfer_between_devices)));
        mItems.add(new UIDebugItem(UIDebugItem.DebugItemType.ONBOARDING, TwinmeApplication.OnboardingType.ENTER_MINI_CODE, mActivity.getString(R.string.enter_invitation_code_view_enter_code)));
        mItems.add(new UIDebugItem(UIDebugItem.DebugItemType.ONBOARDING, TwinmeApplication.OnboardingType.MINI_CODE, mActivity.getString(R.string.invitation_code_view_create_code)));
        mItems.add(new UIDebugItem(UIDebugItem.DebugItemType.ONBOARDING, TwinmeApplication.OnboardingType.REMOTE_CAMERA, mActivity.getString(R.string.call_view_camera_control)));
        mItems.add(new UIDebugItem(UIDebugItem.DebugItemType.ONBOARDING, TwinmeApplication.OnboardingType.REMOTE_CAMERA_SETTING, mActivity.getString(R.string.call_view_camera_control) + " - " + mActivity.getString(R.string.navigation_view_settings)));
        mItems.add(new UIDebugItem(UIDebugItem.DebugItemType.ONBOARDING, TwinmeApplication.OnboardingType.TRANSFER_CALL, mActivity.getString(R.string.premium_services_view_transfert_title)));
        mItems.add(new UIDebugItem(UIDebugItem.DebugItemType.ONBOARDING, TwinmeApplication.OnboardingType.PROXY, mActivity.getString(R.string.proxy_view_title)));
        mItems.add(new UIDebugItem(UIDebugItem.DebugItemType.ONBOARDING, TwinmeApplication.OnboardingType.BACKUP, mActivity.getString(R.string.account_view_backup)));
        mItems.add(new UIDebugItem(UIDebugItem.DebugItemType.ONBOARDING, TwinmeApplication.OnboardingType.RESTORE, mActivity.getString(R.string.account_view_restore)));
        mItems.add(new UIDebugItem(UIDebugItem.DebugItemType.ONBOARDING, TwinmeApplication.OnboardingType.VERIFY_BACKUP, mActivity.getString(R.string.account_view_backup_verify)));
        mItems.add(new UIDebugItem(UIDebugItem.DebugItemType.ONBOARDING, TwinmeApplication.OnboardingType.SHARE_CONTACT, mActivity.getString(R.string.privacy_view_share_invitation_title)));
        mItems.add(new UIDebugItem(UIDebugItem.DebugItemType.RESET, null, mActivity.getString(R.string.settings_view_reset_preferences_title)));

        notifyItemRangeChanged(0, mItems.size());
    }
}