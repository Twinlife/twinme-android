/*
 *  Copyright (c) 2020-2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
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
import org.twinlife.twinme.ui.Settings;
import org.twinlife.twinme.ui.rooms.InformationViewHolder;
import org.twinlife.twinme.utils.SectionTitleViewHolder;

import java.util.ArrayList;
import java.util.List;

public class MessagesSettingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "MessagesSettingsAdapter";
    private static final boolean DEBUG = false;

    @NonNull
    private final MessagesSettingsActivity mListActivity;

    private final List<UIMessageSettingItem> mItems = new ArrayList<>();

    private static final int TITLE = 0;
    private static final int CHECKBOX = 1;
    private static final int INFO = 2;
    private static final int VALUE = 3;


    MessagesSettingsAdapter(@NonNull MessagesSettingsActivity listActivity) {

        mListActivity = listActivity;
        setHasStableIds(false);
        loadItems();
    }

    @Override
    public int getItemCount() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemCount");
        }

        return mItems.size();
    }

    public void updateMediaQuality() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateMediaQuality");
        }

        for (UIMessageSettingItem item : mItems) {
            if (item.getType() == UIMessageSettingItem.MessageSettingItemType.CONTENT_MEDIA) {
                notifyItemChanged(mItems.indexOf(item));
                break;
            }
        }
    }

    public void updateDisplayCalls() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateDisplayCalls");
        }

        for (UIMessageSettingItem item : mItems) {
            if (item.getType() == UIMessageSettingItem.MessageSettingItemType.DISPLAY_CALLS) {
                notifyItemChanged(mItems.indexOf(item));
                break;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemViewType: " + position);
        }

        UIMessageSettingItem item = mItems.get(position);
        switch (item.getType()) {
            case HEADER:
            case INFO:
                return INFO;

            case SECTION:
                return TITLE;

            case DISPLAY_CALLS:
            case CONTENT_MEDIA:
            case EPHEMERAL_DURATION:
                return VALUE;

            default:
                return CHECKBOX;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        int viewType = getItemViewType(position);
        UIMessageSettingItem item = mItems.get(position);
        if (viewType == INFO) {
            InformationViewHolder informationViewHolder = (InformationViewHolder) viewHolder;
            informationViewHolder.onBind(item.getText(), item.hideSeparator());
        } else if (viewType == TITLE) {
            SectionTitleViewHolder sectionTitleViewHolder = (SectionTitleViewHolder) viewHolder;
            sectionTitleViewHolder.onBind(item.getText(), item.hideSeparator());
        } else if (viewType == CHECKBOX) {
            SettingSwitchViewHolder settingsViewHolder = (SettingSwitchViewHolder) viewHolder;

            UISetting<Boolean> uiSetting = null;
            Settings.BooleanConfig booleanConfig = null;
            boolean useUISetting = false;
            boolean value = false;

            switch (item.getType()) {
                case NOTIFICATION_DISPLAY_SENDER:
                    booleanConfig = Settings.displayNotificationSender;
                    useUISetting = true;
                    break;

                case NOTIFICATION_DISPLAY_CONTENT:
                    booleanConfig = Settings.displayNotificationContent;
                    useUISetting = true;
                    break;

                case NOTIFICATION_DISPLAY_LIKE:
                    booleanConfig = Settings.displayNotificationLike;
                    useUISetting = true;
                    break;

                case ALLOW_COPY_TEXT:
                    booleanConfig = Settings.messageCopyAllowed;
                    value = mListActivity.messageCopyAllowed();
                    break;

                case ALLOW_COPY_FILE:
                    booleanConfig = Settings.fileCopyAllowed;
                    value = mListActivity.fileCopyAllowed();
                    break;

                case EPHEMERAL_ENABLE:
                    booleanConfig = Settings.ephemeralMessageAllowed;
                    value = mListActivity.isAllowEphemeral();
                    break;

                default:
                    break;
            }

            if (booleanConfig == null) {
                return;
            }

            if (useUISetting) {
                uiSetting = new UISetting<>(UISetting.TypeSetting.CHECKBOX, item.getText(), booleanConfig);

                UISetting<Boolean> finalUiSetting = uiSetting;
                CompoundButton.OnCheckedChangeListener onCheckedChangeListener = (buttonView, isChecked) -> mListActivity.onSettingChangeValue(finalUiSetting, isChecked);
                settingsViewHolder.onBind(uiSetting, uiSetting.getBoolean(), true, onCheckedChangeListener);
            } else {
                Settings.BooleanConfig finalBooleanConfig = booleanConfig;
                CompoundButton.OnCheckedChangeListener onCheckedChangeListener = (buttonView, isChecked) -> mListActivity.onSettingChangeValue(finalBooleanConfig, isChecked);
                settingsViewHolder.onBind(item.getText(), value, booleanConfig, onCheckedChangeListener);
            }
        } else if (viewType == VALUE) {
            SettingValueViewHolder settingValueViewHolder = (SettingValueViewHolder) viewHolder;
            if (item.getType() == UIMessageSettingItem.MessageSettingItemType.EPHEMERAL_DURATION) {
                Runnable runnable = () -> mListActivity.onSettingClick(Settings.ephemeralMessageExpireTimeout);
                settingValueViewHolder.onBind(item.getText(), mListActivity.getExpireTimeout(), mListActivity.isAllowEphemeral(), Settings.ephemeralMessageExpireTimeout, runnable);
            } else {
                UISetting<Integer> uiSetting;
                if (item.getType() == UIMessageSettingItem.MessageSettingItemType.CONTENT_MEDIA) {
                    uiSetting = new UISetting<>(UISetting.TypeSetting.VALUE, item.getText(), Settings.qualityMedia);
                } else {
                    uiSetting = new UISetting<>(UISetting.TypeSetting.VALUE, "", Settings.displayCallsMode);
                }
                Runnable runnable = () -> mListActivity.onSettingClick(uiSetting);
                settingValueViewHolder.onBind(uiSetting, true, runnable);
            }
        }
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mListActivity.getLayoutInflater();
        View convertView;

        if (viewType == INFO) {
            convertView = inflater.inflate(R.layout.settings_room_activity_information_item, parent, false);
            return new InformationViewHolder(convertView);
        } else if (viewType == TITLE) {
            convertView = inflater.inflate(R.layout.section_title_item, parent, false);
            return new SectionTitleViewHolder(convertView);
        } else if (viewType == CHECKBOX) {
            convertView = inflater.inflate(R.layout.settings_activity_item_switch, parent, false);
            return new SettingSwitchViewHolder(convertView);
        } else {
            convertView = inflater.inflate(R.layout.settings_activity_item_value, parent, false);
            return new SettingValueViewHolder(convertView);
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }

        int position = viewHolder.getBindingAdapterPosition();
        if (position >= 0 && position < mItems.size()) {
            int viewType = getItemViewType(position);
            if (viewType == TITLE) {
                SectionTitleViewHolder sectionTitleViewHolder = (SectionTitleViewHolder) viewHolder;
                UIMessageSettingItem item = mItems.get(position);
                sectionTitleViewHolder.onBind(item.getText(), item.hideSeparator());
            }
        }
    }

    public void loadItems() {
        if (DEBUG) {
            Log.d(LOG_TAG, "loadItems");
        }

        mItems.clear();

        mItems.add(new UIMessageSettingItem(UIMessageSettingItem.MessageSettingItemType.HEADER, mListActivity.getString(R.string.settings_view_default_value_message), false));
        mItems.add(new UIMessageSettingItem(UIMessageSettingItem.MessageSettingItemType.SECTION, mListActivity.getString(R.string.settings_view_system_notifications_title), false));
        mItems.add(new UIMessageSettingItem(UIMessageSettingItem.MessageSettingItemType.NOTIFICATION_DISPLAY_SENDER, mListActivity.getString(R.string.settings_view_display_notification_sender_title), false));
        mItems.add(new UIMessageSettingItem(UIMessageSettingItem.MessageSettingItemType.NOTIFICATION_DISPLAY_CONTENT, mListActivity.getString(R.string.settings_view_display_notification_content_title), false));
        mItems.add(new UIMessageSettingItem(UIMessageSettingItem.MessageSettingItemType.NOTIFICATION_DISPLAY_LIKE, mListActivity.getString(R.string.settings_view_display_notification_like_title), false));
        mItems.add(new UIMessageSettingItem(UIMessageSettingItem.MessageSettingItemType.SECTION, mListActivity.getString(R.string.settings_view_permissions_title), true));
        mItems.add(new UIMessageSettingItem(UIMessageSettingItem.MessageSettingItemType.INFO, mListActivity.getString(R.string.settings_view_allow_copy_category_title), true));
        mItems.add(new UIMessageSettingItem(UIMessageSettingItem.MessageSettingItemType.ALLOW_COPY_TEXT, mListActivity.getString(R.string.settings_view_allow_copy_text_title), false));
        mItems.add(new UIMessageSettingItem(UIMessageSettingItem.MessageSettingItemType.ALLOW_COPY_FILE, mListActivity.getString(R.string.settings_view_allow_copy_file_title), false));
        mItems.add(new UIMessageSettingItem(UIMessageSettingItem.MessageSettingItemType.SECTION, mListActivity.getString(R.string.calls_view_title), true));
        mItems.add(new UIMessageSettingItem(UIMessageSettingItem.MessageSettingItemType.INFO, mListActivity.getString(R.string.settings_view_display_call_title), true));
        mItems.add(new UIMessageSettingItem(UIMessageSettingItem.MessageSettingItemType.DISPLAY_CALLS, mListActivity.getString(R.string.settings_view_display_call_title), false));
        mItems.add(new UIMessageSettingItem(UIMessageSettingItem.MessageSettingItemType.SECTION, mListActivity.getString(R.string.settings_view_ephemeral_section_title), true));
        mItems.add(new UIMessageSettingItem(UIMessageSettingItem.MessageSettingItemType.INFO, mListActivity.getString(R.string.settings_view_ephemeral_message), true));
        mItems.add(new UIMessageSettingItem(UIMessageSettingItem.MessageSettingItemType.EPHEMERAL_ENABLE, mListActivity.getString(R.string.settings_view_ephemeral_title), false));

        if (mListActivity.isAllowEphemeral()) {
            mItems.add(new UIMessageSettingItem(UIMessageSettingItem.MessageSettingItemType.EPHEMERAL_DURATION, mListActivity.getString(R.string.application_timeout), false));
        }

        mItems.add(new UIMessageSettingItem(UIMessageSettingItem.MessageSettingItemType.SECTION, mListActivity.getString(R.string.settings_view_content_title), true));
        mItems.add(new UIMessageSettingItem(UIMessageSettingItem.MessageSettingItemType.INFO, mListActivity.getString(R.string.settings_view_content_information), true));
        mItems.add(new UIMessageSettingItem(UIMessageSettingItem.MessageSettingItemType.CONTENT_MEDIA,"", false));

        notifyItemRangeChanged(0, mItems.size());
    }
}
