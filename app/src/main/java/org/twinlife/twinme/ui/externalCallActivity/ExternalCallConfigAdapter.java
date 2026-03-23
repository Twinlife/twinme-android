/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.externalCallActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.models.LinkValidity;
import org.twinlife.twinme.models.schedule.Date;
import org.twinlife.twinme.models.schedule.Time;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.contacts.AbstractCapabilitiesActivity;
import org.twinlife.twinme.ui.contacts.ScheduleViewHolder;
import org.twinlife.twinme.ui.conversationActivity.SelectValueViewHolder;
import org.twinlife.twinme.ui.rooms.InformationViewHolder;
import org.twinlife.twinme.ui.settingsActivity.SettingSwitchViewHolder;
import org.twinlife.twinme.utils.SectionTitleViewHolder;

public class ExternalCallConfigAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "ExternalCallConfigA...";
    private static final boolean DEBUG = false;

    public interface OnExternalCallConfigClickListener {
        void onExternalCallConfigClick(UIConfigExternalCallItem configExternalCall);

        void onDateViewClick(UIConfigExternalCallItem configExternalCall);

        void onTimeViewClick(UIConfigExternalCallItem configExternalCall);

        void onSelectDayClick(UIScheduleDay scheduleDay);

        void onSwitchValueChanged(UIConfigExternalCallItem configExternalCall, boolean value);
    }

    private final AbstractTwinmeActivity mActivity;
    private UIConfigExternalCall mConfigExternalCall;

    private final OnExternalCallConfigClickListener mOnExternalCallConfigClickListener;

    private static final int TITLE = 0;
    private static final int VALUE = 1;
    private static final int SCHEDULE = 2;
    private static final int WEEKLY_SCHEDULE = 3;
    private static final int SETTING = 4;
    private static final int INFO = 5;

    public ExternalCallConfigAdapter(AbstractTwinmeActivity activity, UIConfigExternalCall configExternalCall, OnExternalCallConfigClickListener onExternalCallConfigClickListener) {

        mActivity = activity;
        mOnExternalCallConfigClickListener = onExternalCallConfigClickListener;
        mConfigExternalCall = configExternalCall;
        setHasStableIds(true);
    }

    public void updateConfigItems(UIConfigExternalCall configExternalCall) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateConfigItems");
        }

        mConfigExternalCall = configExternalCall;
        notifyItemRangeChanged(0, mConfigExternalCall.getConfigItems().size());
    }

    @Override
    public int getItemCount() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemCount");
        }

        return 1 + mConfigExternalCall.getConfigItems().size();
    }

    @Override
    public int getItemViewType(int position) {

        if (position == 0) {
            return TITLE;
        }

        UIConfigExternalCallItem configItem = mConfigExternalCall.getConfigItems().get(position - 1);
        switch (configItem.getConfigExternalCallSettings()) {
            case CALL_TYPE:
            case PERMISSIONS:
            case EXPIRATION:
                return VALUE;

            case SCHEDULE_START:
            case SCHEDULE_END:
                return SCHEDULE;

            case SCHEDULE_RECURRENT:
                return WEEKLY_SCHEDULE;

            case NOTIFICATION:
                return SETTING;

            case DELETE:
            default:
                return INFO;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        int viewType = getItemViewType(position);
        if (viewType == TITLE) {
            SectionTitleViewHolder sectionTitleViewHolder = (SectionTitleViewHolder) viewHolder;

            if (mConfigExternalCall.isCreateExternalCallMode()) {
                sectionTitleViewHolder.resetMargins();
            }

            sectionTitleViewHolder.onBind(mActivity.getString(R.string.create_external_call_activity_call_configuration), Design.WHITE_COLOR, false);
        } else {
            UIConfigExternalCallItem configItem = mConfigExternalCall.getConfigItems().get(position - 1);
            if (viewType == VALUE) {
                SelectValueViewHolder selectValueViewHolder = (SelectValueViewHolder) viewHolder;

                String title = configItem.getTitle();
                String value = "";

                if (configItem.getConfigExternalCallSettings() == UIConfigExternalCall.ConfigExternalCallSettings.CALL_TYPE) {
                    value = mConfigExternalCall.getCallType();
                } else if (configItem.getConfigExternalCallSettings() == UIConfigExternalCall.ConfigExternalCallSettings.PERMISSIONS) {
                    value = mConfigExternalCall.getCallCapabilities();
                } else if (configItem.getConfigExternalCallSettings() == UIConfigExternalCall.ConfigExternalCallSettings.EXPIRATION) {
                    value = mConfigExternalCall.getExpiration();
                }

                selectValueViewHolder.itemView.setOnClickListener(view -> mOnExternalCallConfigClickListener.onExternalCallConfigClick(configItem));

                if (mConfigExternalCall.isCreateExternalCallMode()) {
                    selectValueViewHolder.resetMargins();
                }

                selectValueViewHolder.onBind(title, value, false, Design.WHITE_COLOR);
            } else if (viewType == SCHEDULE) {
                ScheduleViewHolder scheduleViewHolder = (ScheduleViewHolder) viewHolder;
                Date date;
                Time time;
                Runnable dateRunnable = null;
                if (mConfigExternalCall.getLinkValidity() == LinkValidity.SINGLE_USE) {
                    dateRunnable = () -> mOnExternalCallConfigClickListener.onDateViewClick(configItem);
                }
                Runnable timeRunnable = () -> mOnExternalCallConfigClickListener.onTimeViewClick(configItem);
                AbstractCapabilitiesActivity.ScheduleType scheduleType;
                if (configItem.getConfigExternalCallSettings() == UIConfigExternalCall.ConfigExternalCallSettings.SCHEDULE_START ) {
                    date = mConfigExternalCall.getScheduleStartDate();
                    time = mConfigExternalCall.getScheduleStartTime();
                    scheduleType = AbstractCapabilitiesActivity.ScheduleType.START;
                } else {
                    date = mConfigExternalCall.getScheduleEndDate();
                    time = mConfigExternalCall.getScheduleEndTime();
                    scheduleType = AbstractCapabilitiesActivity.ScheduleType.END;
                }

                if (mConfigExternalCall.isCreateExternalCallMode()) {
                    scheduleViewHolder.resetMargins();
                }

                scheduleViewHolder.onBind(mActivity, scheduleType, date, time, dateRunnable, timeRunnable);
            } else if (viewType == WEEKLY_SCHEDULE) {
                WeeklyScheduleViewHolder weeklyScheduleViewHolder = (WeeklyScheduleViewHolder) viewHolder;

                if (mConfigExternalCall.isCreateExternalCallMode()) {
                    weeklyScheduleViewHolder.resetMargins();
                }

                WeeklyScheduleAdapter.OnDayClickListener onDayClickListener = mOnExternalCallConfigClickListener::onSelectDayClick;
                weeklyScheduleViewHolder.onBind(mConfigExternalCall.getScheduleRecurrentDays(), onDayClickListener);
            } else if (viewType == SETTING) {
                SettingSwitchViewHolder settingSwitchViewHolder = (SettingSwitchViewHolder) viewHolder;
                if (mConfigExternalCall.isCreateExternalCallMode()) {
                    settingSwitchViewHolder.resetMargins();
                }

                boolean isSelected = false;
                if (configItem.getConfigExternalCallSettings() == UIConfigExternalCall.ConfigExternalCallSettings.NOTIFICATION) {
                    isSelected = mConfigExternalCall.notificationJoinCall();
                } else if (configItem.getConfigExternalCallSettings() == UIConfigExternalCall.ConfigExternalCallSettings.DELETE) {
                    isSelected = mConfigExternalCall.deleteLink();
                }

                CompoundButton.OnCheckedChangeListener onCheckedChangeListener = (compoundButton, b) -> mOnExternalCallConfigClickListener.onSwitchValueChanged(configItem, b);
                settingSwitchViewHolder.onBind(configItem.getTitle(), isSelected, true, onCheckedChangeListener);
            } else {
                InformationViewHolder informationViewHolder = (InformationViewHolder) viewHolder;

                if (mConfigExternalCall.isCreateExternalCallMode()) {
                    informationViewHolder.resetMargins();
                }

                informationViewHolder.onBind(mActivity.getString(R.string.create_external_call_activity_delete_link_setting), true, Design.WHITE_COLOR, Design.FONT_COLOR_DEFAULT, Design.FONT_REGULAR30);
            }
        }
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (viewType == TITLE) {
            View convertView = inflater.inflate(R.layout.section_title_item, parent, false);
            return new SectionTitleViewHolder(convertView);
        } else if (viewType == SCHEDULE) {
            View convertView = inflater.inflate(R.layout.contact_capabilities_activity_schedule_item, parent, false);
            return new ScheduleViewHolder(convertView);
        } else if (viewType == WEEKLY_SCHEDULE) {
            View convertView = inflater.inflate(R.layout.create_external_call_activity_weekly_schedule_item, parent, false);
            return new WeeklyScheduleViewHolder(convertView);
        } else if (viewType == VALUE) {
            View convertView = inflater.inflate(R.layout.select_value_item, parent, false);
            return new SelectValueViewHolder(convertView);
        }  else if (viewType == SETTING) {
            View convertView = inflater.inflate(R.layout.settings_activity_item_switch, parent, false);
            return new SettingSwitchViewHolder(convertView);
        } else {
            View convertView = inflater.inflate(R.layout.settings_room_activity_information_item, parent, false);
            return new InformationViewHolder(convertView);
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }
    }
}