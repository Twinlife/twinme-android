/*
 *  Copyright (c) 2020-2022 twinlife SA.
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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.BuildConfig;
import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.ui.Settings;
import org.twinlife.twinme.ui.rooms.InformationViewHolder;
import org.twinlife.twinme.utils.SectionTitleViewHolder;

public class MessagesSettingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "MessagesSettingsAdapter";
    private static final boolean DEBUG = false;

    @NonNull
    private final MessagesSettingsActivity mListActivity;

    private final int ITEM_COUNT;

    private static final int SECTION_NOTIFICATION = 0;
    private static final int SECTION_COPY = 4;
    private static final int SECTION_CALLS = 8;

    private final int SECTION_EPHEMERAL;
    private final int SECTION_CONTENT;
    private final int SECTION_LINK;

    private static final int POSITION_DISPLAY_NOTIFCATION_SENDER = 1;
    private static final int POSITION_DISPLAY_NOTIFCATION_CONTENT = 2;
    private static final int POSITION_DISPLAY_NOTIFCATION_LIKE = 3;
    private static final int POSITION_ALLOW_COPY_INFORMATION = 5;
    private static final int POSITION_ALLOW_COPY_TEXT = 6;
    private static final int POSITION_ALLOW_COPY_FILE = 7;
    private static final int POSITION_DISPLAY_CALLS_INFORMATION = 9;
    private static final int POSITION_DISPLAY_CALLS = 10;

    private final int POSITION_CONTENT_INFORMATION;
    private final int POSITION_CONTENT_IMAGE;
    private final int POSITION_CONTENT_VIDEO;
    private final int POSITION_LINK_PREVIEW_INFORMATION;
    private final int POSITION_LINK_PREVIEW;
    private final int POSITION_EPHEMERAL_INFORMATION;

    private static final int TITLE = 0;
    private static final int CHECKBOX = 1;
    private static final int INFO = 2;
    private static final int VALUE = 3;

    MessagesSettingsAdapter(@NonNull MessagesSettingsActivity listActivity) {

        mListActivity = listActivity;
        setHasStableIds(false);

        if (!BuildConfig.DISPLAY_PREMIUM_FEATURE) {
            SECTION_EPHEMERAL = -1;
            POSITION_EPHEMERAL_INFORMATION = -1;
            SECTION_CONTENT = 11;
            POSITION_CONTENT_INFORMATION = 12;
            POSITION_CONTENT_IMAGE = 13;
            POSITION_CONTENT_VIDEO = -1;
            SECTION_LINK = 14;
            POSITION_LINK_PREVIEW_INFORMATION = 15;
            POSITION_LINK_PREVIEW = 16;
            ITEM_COUNT = 17;
        } else {
            SECTION_EPHEMERAL = 11;
            POSITION_EPHEMERAL_INFORMATION = 12;
            SECTION_CONTENT = 14;
            POSITION_CONTENT_INFORMATION = 15;
            POSITION_CONTENT_IMAGE = 16;
            POSITION_CONTENT_VIDEO = -1;
            SECTION_LINK = 17;
            POSITION_LINK_PREVIEW_INFORMATION = 18;
            POSITION_LINK_PREVIEW = 19;
            ITEM_COUNT = 20;
        }
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

        if (position == POSITION_ALLOW_COPY_INFORMATION || position == POSITION_EPHEMERAL_INFORMATION || position == POSITION_CONTENT_INFORMATION || position == POSITION_LINK_PREVIEW_INFORMATION || position == POSITION_DISPLAY_CALLS_INFORMATION) {
            return INFO;
        } else if (position == SECTION_NOTIFICATION || position == SECTION_COPY || position == SECTION_EPHEMERAL || position == SECTION_CONTENT || position == SECTION_LINK || position == SECTION_CALLS) {
            return TITLE;
        } else if (position == POSITION_CONTENT_IMAGE || position == POSITION_CONTENT_VIDEO || position == POSITION_DISPLAY_CALLS) {
            return VALUE;
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

        if (viewType == INFO) {
            InformationViewHolder informationViewHolder = (InformationViewHolder) viewHolder;
            if (position == POSITION_ALLOW_COPY_INFORMATION) {
                informationViewHolder.onBind(mListActivity.getString(R.string.settings_activity_allow_copy_category_title), true);
            } else if (position == POSITION_DISPLAY_CALLS_INFORMATION) {
                informationViewHolder.onBind(mListActivity.getString(R.string.settings_activity_display_call_title), true);
            } else if (position == POSITION_CONTENT_INFORMATION) {
                informationViewHolder.onBind(mListActivity.getString(R.string.settings_activity_content_information), true);
            } else if (position == POSITION_LINK_PREVIEW_INFORMATION) {
                informationViewHolder.onBind(mListActivity.getString(R.string.conversation_settings_activity_link_preview_message), true);
            } else {
                informationViewHolder.onBind(mListActivity.getString(R.string.settings_activity_ephemeral_message), true);
            }
        } else if (viewType == TITLE) {
            SectionTitleViewHolder sectionTitleViewHolder = (SectionTitleViewHolder) viewHolder;
            String title = getSectionTitle(position);
            boolean hideSeparator = position != SECTION_NOTIFICATION;
            sectionTitleViewHolder.onBind(title, hideSeparator);
        } else if (viewType == CHECKBOX) {
            SettingSwitchViewHolder settingsViewHolder = (SettingSwitchViewHolder) viewHolder;

            UISetting<Boolean> uiSetting = null;

            if (position == POSITION_LINK_PREVIEW) {
                uiSetting = new UISetting<>(UISetting.TypeSetting.CHECKBOX, mListActivity.getString(R.string.conversation_settings_activity_link_preview), Settings.visualizationLink);
            } else {
                switch (position) {
                    case POSITION_DISPLAY_NOTIFCATION_SENDER:
                        uiSetting = new UISetting<>(UISetting.TypeSetting.CHECKBOX, mListActivity.getString(R.string.settings_activity_display_notification_sender_title), Settings.displayNotificationSender);
                        break;

                    case POSITION_DISPLAY_NOTIFCATION_CONTENT:
                        uiSetting = new UISetting<>(UISetting.TypeSetting.CHECKBOX, mListActivity.getString(R.string.settings_activity_display_notification_content_title), Settings.displayNotificationContent);
                        break;

                    case POSITION_DISPLAY_NOTIFCATION_LIKE:
                        uiSetting = new UISetting<>(UISetting.TypeSetting.CHECKBOX, mListActivity.getString(R.string.settings_activity_display_notification_like_title), Settings.displayNotificationLike);
                        break;

                    case POSITION_ALLOW_COPY_TEXT:
                        uiSetting = new UISetting<>(UISetting.TypeSetting.CHECKBOX, mListActivity.getString(R.string.settings_activity_allow_copy_text_title), Settings.messageCopyAllowed);
                        break;

                    case POSITION_ALLOW_COPY_FILE:
                        uiSetting = new UISetting<>(UISetting.TypeSetting.CHECKBOX, mListActivity.getString(R.string.settings_activity_allow_copy_file_title), Settings.fileCopyAllowed);
                        break;

                    default:
                        break;
                }
            }

            if (uiSetting != null) {
                settingsViewHolder.onBind(uiSetting, uiSetting.getBoolean());
            } else {
                settingsViewHolder.itemView.setOnClickListener(v -> mListActivity.onPremiumFeatureClick());
                settingsViewHolder.onBind(mListActivity.getString(R.string.settings_activity_ephemeral_title), false, false);
            }
        } else if (viewType == VALUE) {
            SettingValueViewHolder settingsViewHolder = (SettingValueViewHolder) viewHolder;
            UISetting<Integer> uiSetting;
            if (position == POSITION_CONTENT_IMAGE) {
                uiSetting = new UISetting<>(UISetting.TypeSetting.VALUE, mListActivity.getString(R.string.settings_activity_image_title), Settings.reduceSizeImage);
            } else if (position == POSITION_CONTENT_VIDEO) {
                uiSetting = new UISetting<>(UISetting.TypeSetting.VALUE, mListActivity.getString(R.string.show_contact_activity_video), Settings.reduceSizeVideo);
            } else {
                uiSetting = new UISetting<>(UISetting.TypeSetting.VALUE, "", Settings.displayCallsMode);
            }
            settingsViewHolder.onBind(uiSetting, true);
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
            return new SettingSwitchViewHolder(convertView, mListActivity);
        } else {
            convertView = inflater.inflate(R.layout.settings_activity_item_value, parent, false);
            return new SettingValueViewHolder(convertView, mListActivity);
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }

        int position = viewHolder.getBindingAdapterPosition();
        int viewType = getItemViewType(position);
        if (position != -1) {
            if (viewType == TITLE) {
                SectionTitleViewHolder sectionTitleViewHolder = (SectionTitleViewHolder) viewHolder;
                String title = getSectionTitle(position);
                boolean hideSeparator = position != SECTION_NOTIFICATION;
                sectionTitleViewHolder.onBind(title, hideSeparator);
            }
        }
    }

    private String getSectionTitle(int position) {

        String title = "";

        if (position == SECTION_NOTIFICATION) {
            title = mListActivity.getString(R.string.settings_activity_system_notifications_title);
        } else if (position == SECTION_COPY) {
            title = mListActivity.getString(R.string.settings_activity_permissions_title);
        } else if (position == SECTION_CALLS) {
            title = mListActivity.getString(R.string.calls_fragment_title);
        } else if (position == SECTION_EPHEMERAL) {
            title = mListActivity.getString(R.string.settings_activity_ephemeral_section_title);
        } else if (position == SECTION_CONTENT) {
            title = mListActivity.getString(R.string.settings_activity_content_title);
        } else if (position == SECTION_LINK) {
            title = mListActivity.getString(R.string.conversation_settings_activity_link_title);
        }

        return title;
    }
}