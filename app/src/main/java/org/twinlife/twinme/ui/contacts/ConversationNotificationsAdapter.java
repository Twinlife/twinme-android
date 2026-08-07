/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.contacts;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.conversationActivity.SelectValueViewHolder;
import org.twinlife.twinme.ui.premiumServicesActivity.UIPremiumFeature;
import org.twinlife.twinme.ui.rooms.InformationViewHolder;
import org.twinlife.twinme.utils.CommonUtils;
import org.twinlife.twinme.utils.SectionTitleViewHolder;

import java.util.ArrayList;
import java.util.List;

public class ConversationNotificationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "ConversationNotificationsAdapter";
    private static final boolean DEBUG = false;

    private final ConversationNotificationsActivity mNotificationsActivity;

    private final List<UIConversationNotificationsItem> mItems = new ArrayList<>();

    private static final int SWITCH = 1;
    private static final int SECTION = 2;
    private static final int INFO = 3;
    private static final int VALUE = 4;

    public ConversationNotificationsAdapter(ConversationNotificationsActivity activity) {

        mNotificationsActivity = activity;
        updateItems();
        setHasStableIds(true);
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

        UIConversationNotificationsItem item = mItems.get(position);
        switch (item.getType()) {
            case NOTIFICATIONS_SECTION:
            case PRIVACY_SECTION:
                return SECTION;

            case SILENT_MODE_DURATION:
                return VALUE;

            case DISCREET_MODE_INFO:
                return INFO;

            case DISCREET_MODE:
            case DISPLAY_NOTIFICATIONS_REACTIONS:
            case SILENT_MODE:
                return SWITCH;

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
        UIConversationNotificationsItem item = mItems.get(position);

        if (viewType == SWITCH) {
            CapabilityViewHolder capabilityViewHolder = (CapabilityViewHolder) viewHolder;

            boolean isSelected = false;
            boolean isEnabled = true;

            switch (item.getType()) {
                case DISPLAY_NOTIFICATIONS_REACTIONS:
                    isSelected = mNotificationsActivity.isNotificationsReactionsEnabled();
                    break;

                case SILENT_MODE:
                    isSelected = mNotificationsActivity.isSilentModeEnabled();
                    break;

                case DISCREET_MODE:
                    isSelected = mNotificationsActivity.isDiscreetModeEnabled();
                    break;

                default:
                    break;
            }

            CompoundButton.OnCheckedChangeListener onCheckedChangeListener = (compoundButton, value) -> mNotificationsActivity.onSettingChangeValue(item, value);
            capabilityViewHolder.onBind(item.geText(), item.getType().ordinal(), isEnabled, isSelected, onCheckedChangeListener);
        } else if (viewType == SECTION) {
            SectionTitleViewHolder sectionTitleViewHolder = (SectionTitleViewHolder) viewHolder;
            sectionTitleViewHolder.onBind(item.geText(), false, null, null);
        } else if (viewType == INFO) {
            InformationViewHolder informationViewHolder = (InformationViewHolder) viewHolder;
            informationViewHolder.onBind(item.geText(), false);
        }  else if (viewType == VALUE) {
            SelectValueViewHolder selectValueViewHolder = (SelectValueViewHolder) viewHolder;
            selectValueViewHolder.itemView.setOnClickListener(v -> mNotificationsActivity.onSelectSilentDuration());

            String value = "";
            if (mNotificationsActivity.getSilentModeExpiration() > 0) {
                value = String.format(mNotificationsActivity.getString(R.string.application_until), CommonUtils.formatItemInterval(mNotificationsActivity, mNotificationsActivity.getSilentModeExpiration()));
            } else if (mNotificationsActivity.getSilentModeExpiration() == -1) {
                value = mNotificationsActivity.getString(R.string.contact_capabilities_view_camera_control_allow);
            }

            selectValueViewHolder.onBind(item.geText(), value, false, Design.WHITE_COLOR);
        }
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mNotificationsActivity.getLayoutInflater();
        View convertView;

        if (viewType == SECTION) {
            convertView = inflater.inflate(R.layout.section_title_item, parent, false);
            return new SectionTitleViewHolder(convertView);
        } else if (viewType == INFO) {
            convertView = inflater.inflate(R.layout.settings_room_activity_information_item, parent, false);
            return new InformationViewHolder(convertView);
        } else if (viewType == SWITCH) {
            convertView = inflater.inflate(R.layout.contact_capabilities_activity_item, parent, false);
            return new CapabilityViewHolder(convertView);
        } else {
            convertView = inflater.inflate(R.layout.select_value_item, parent, false);
            return new SelectValueViewHolder(convertView);
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }
    }

    protected void updateItems() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateItems");
        }

        mItems.clear();

        mItems.add(new UIConversationNotificationsItem(UIConversationNotificationsItem.ConversationNotificationsItemType.NOTIFICATIONS_SECTION, mNotificationsActivity.getString(R.string.settings_view_system_notifications_title)));
        mItems.add(new UIConversationNotificationsItem(UIConversationNotificationsItem.ConversationNotificationsItemType.DISPLAY_NOTIFICATIONS_REACTIONS, mNotificationsActivity.getString(R.string.settings_view_message_reactions_notification)));
        mItems.add(new UIConversationNotificationsItem(UIConversationNotificationsItem.ConversationNotificationsItemType.SILENT_MODE, mNotificationsActivity.getString(R.string.settings_view_silent_mode)));

        if (mNotificationsActivity.isSilentModeEnabled()) {
            mItems.add(new UIConversationNotificationsItem(UIConversationNotificationsItem.ConversationNotificationsItemType.SILENT_MODE_DURATION, mNotificationsActivity.getString(R.string.settings_view_turn_off_notification_sounds)));
        }

        mItems.add(new UIConversationNotificationsItem(UIConversationNotificationsItem.ConversationNotificationsItemType.PRIVACY_SECTION, mNotificationsActivity.getString(R.string.privacy_view_title)));
        mItems.add(new UIConversationNotificationsItem(UIConversationNotificationsItem.ConversationNotificationsItemType.DISCREET_MODE, mNotificationsActivity.getString(R.string.contact_capabilities_view_discreet_relation)));
        mItems.add(new UIConversationNotificationsItem(UIConversationNotificationsItem.ConversationNotificationsItemType.DISCREET_MODE_INFO, mNotificationsActivity.getString(R.string.contact_capabilities_view_information_discreet_relation)));

        notifyItemRangeChanged(0, mItems.size());
    }
}