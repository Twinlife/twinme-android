/*
 *  Copyright (c) 2020-2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui.settingsActivity;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.ui.Settings;
import org.twinlife.twinme.utils.SectionTitleViewHolder;

public class SoundsSettingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "SoundsSettingsAdapter";
    private static final boolean DEBUG = false;

    @NonNull
    private final SoundsSettingsActivity mListActivity;

    private static final int ITEM_COUNT;

    private static final int SECTION_NOTIFICATION_SOUND;
    private static final int SECTION_MESSAGES;
    private static final int SECTION_AUDIO_CALL;
    private static final int SECTION_VIDEO_CALL;

    private static final int TITLE = 0;
    private static final int SETTING = 1;
    private static final int CHECKBOX = 2;
    private static final int RINGTONE = 3;

    static {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            ITEM_COUNT = 13;
            SECTION_NOTIFICATION_SOUND = 0;
            SECTION_MESSAGES = 3;
            SECTION_AUDIO_CALL = 5;
            SECTION_VIDEO_CALL = 9;
        } else {
            ITEM_COUNT = 12;
            SECTION_NOTIFICATION_SOUND = -1;
            SECTION_MESSAGES = 0;
            SECTION_AUDIO_CALL = 4;
            SECTION_VIDEO_CALL = 8;
        }
    }

    SoundsSettingsAdapter(@NonNull SoundsSettingsActivity listActivity) {

        mListActivity = listActivity;
        setHasStableIds(true);
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

        if (position == SECTION_NOTIFICATION_SOUND || position == SECTION_MESSAGES || position == SECTION_AUDIO_CALL || position == SECTION_VIDEO_CALL) {
            return TITLE;
        } else if ((position == SECTION_NOTIFICATION_SOUND + 1 || position == SECTION_NOTIFICATION_SOUND + 2 || position == SECTION_MESSAGES + 1) && SECTION_NOTIFICATION_SOUND != -1) {
            return SETTING;
        } else if (position == SECTION_AUDIO_CALL - 1 || position == SECTION_VIDEO_CALL - 1 || position == ITEM_COUNT - 1) {
            return RINGTONE;
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
            String title;
            if (position == SECTION_NOTIFICATION_SOUND) {
                title = mListActivity.getString(R.string.settings_activity_application_notifications);
            } else if (position == SECTION_MESSAGES) {
                title = mListActivity.getString(R.string.settings_activity_chat_category_title);
            } else if (position == SECTION_AUDIO_CALL) {
                title = mListActivity.getString(R.string.settings_activity_audio_call_category_title);
            } else {
                title = mListActivity.getString(R.string.settings_activity_video_call_category_title);
            }
            sectionTitleViewHolder.onBind(title, false);
        } else if (viewType == SETTING) {
            SettingViewHolder settingsViewHolder = (SettingViewHolder) viewHolder;

            UISetting<?> uiSetting = null;

            if (position == SECTION_NOTIFICATION_SOUND + 1) {
                uiSetting = new UISetting<>(UISetting.TypeSetting.SYSTEM, mListActivity.getString(R.string.settings_activity_system_settings_title));
            } else if (position == SECTION_MESSAGES + 1) {
                uiSetting = new UISetting<>(UISetting.TypeSetting.SYSTEM_MESSAGE, mListActivity.getString(R.string.settings_activity_system_settings_title));
            } else if (position == SECTION_NOTIFICATION_SOUND + 2) {
                uiSetting = new UISetting<>(UISetting.TypeSetting.RESET, mListActivity.getString(R.string.settings_activity_reset_preferences_button_title));
            }

            if (uiSetting != null) {
                settingsViewHolder.onBind(uiSetting);
            }
        } else if (viewType == RINGTONE) {
            RingtoneViewHolder ringtoneViewHolder = (RingtoneViewHolder) viewHolder;

            UISetting<String> uiSetting = null;
            if (position == SECTION_MESSAGES + 3) {
                uiSetting = new UISetting<>(UISetting.TypeSetting.RINGTONE, mListActivity.getString(R.string.settings_activity_chat_ringtone_title), Settings.notificatonRingtone);
            } else if (position == SECTION_AUDIO_CALL + 3) {
                uiSetting = new UISetting<>(UISetting.TypeSetting.RINGTONE, mListActivity.getString(R.string.settings_activity_audio_call_notification_ringtone_title), Settings.audioCallRingtone);
            } else if (position == SECTION_VIDEO_CALL + 3) {
                uiSetting = new UISetting<>(UISetting.TypeSetting.RINGTONE, mListActivity.getString(R.string.settings_activity_video_call_notification_ringtone_title), Settings.videoCallRingtone);
            }

            if (uiSetting != null) {
                ringtoneViewHolder.onBind(uiSetting, getRingtoneName(uiSetting));
            }
        } else if (viewType == CHECKBOX) {
            SettingSwitchViewHolder settingsSwitchViewHolder = (SettingSwitchViewHolder) viewHolder;

            UISetting<Boolean> uiSetting = null;

            if (position == SECTION_MESSAGES + 1) {
                uiSetting = new UISetting<>(UISetting.TypeSetting.CHECKBOX, mListActivity.getString(R.string.settings_activity_chat_vibration_title), Settings.notificationVibration);
            } else if (position == SECTION_MESSAGES + 2) {
                uiSetting = new UISetting<>(UISetting.TypeSetting.CHECKBOX, mListActivity.getString(R.string.settings_activity_chat_title), Settings.notificationRingEnabled);
            } else if (position == SECTION_AUDIO_CALL + 1) {
                uiSetting = new UISetting<>(UISetting.TypeSetting.CHECKBOX, mListActivity.getString(R.string.settings_activity_audio_call_vibration_title), Settings.audioVibration);
            } else if (position == SECTION_AUDIO_CALL + 2) {
                uiSetting = new UISetting<>(UISetting.TypeSetting.CHECKBOX, mListActivity.getString(R.string.settings_activity_audio_call_notification_title), Settings.audioRingEnabled);
            } else if (position == SECTION_VIDEO_CALL + 1) {
                uiSetting = new UISetting<>(UISetting.TypeSetting.CHECKBOX, mListActivity.getString(R.string.settings_activity_video_call_vibration_title), Settings.videoVibration);
            } else if (position == SECTION_VIDEO_CALL + 2) {
                uiSetting = new UISetting<>(UISetting.TypeSetting.CHECKBOX, mListActivity.getString(R.string.settings_activity_video_call_notification_title), Settings.videoRingEnabled);
            }

            if (uiSetting != null) {
                boolean isSelected = uiSetting.getBoolean();
                settingsSwitchViewHolder.onBind(uiSetting, isSelected, true);
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

        if (viewType == TITLE) {
            convertView = inflater.inflate(R.layout.section_title_item, parent, false);
            return new SectionTitleViewHolder(convertView);
        } else if (viewType == CHECKBOX) {
            convertView = inflater.inflate(R.layout.settings_activity_item_switch, parent, false);
            return new SettingSwitchViewHolder(convertView, mListActivity);
        } else if (viewType == RINGTONE) {
            convertView = inflater.inflate(R.layout.settings_activity_item_ringtone, parent, false);
            return new RingtoneViewHolder(convertView, mListActivity);
        } else {
            convertView = inflater.inflate(R.layout.settings_activity_item, parent, false);
            return new SettingViewHolder(convertView, mListActivity);
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }
    }

    @NonNull
    private String getRingtoneName(@NonNull UISetting<String> setting) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getRingtoneName:  setting=" + setting);
        }

        final String value = setting.getString();
        Uri ringtoneUri = null;
        if (value != null) {
            ringtoneUri = Uri.parse(value);
        }

        boolean enable;
        boolean defaultSound = value != null && value.startsWith("android.resource://");
        if (setting.isSetting(Settings.notificatonRingtone)) {
            enable = Settings.notificationRingEnabled.getBoolean();
            if (!defaultSound) {
                defaultSound = ringtoneUri != null && ringtoneUri.equals(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI);
            }
        } else if (setting.isSetting(Settings.audioCallRingtone)) {
            enable = Settings.audioRingEnabled.getBoolean();
            if (!defaultSound) {
                defaultSound = ringtoneUri != null && ringtoneUri.equals(android.provider.Settings.System.DEFAULT_RINGTONE_URI);
            }
        } else if (setting.isSetting(Settings.videoCallRingtone)) {
            enable = Settings.videoRingEnabled.getBoolean();
            if (!defaultSound) {
                defaultSound = ringtoneUri != null && ringtoneUri.equals(android.provider.Settings.System.DEFAULT_RINGTONE_URI);
            }
        } else {
            return mListActivity.getString(R.string.settings_activity_default_sound);
        }

        if (!enable) {
            return mListActivity.getString(R.string.settings_activity_silent_sound);
        }

        if (defaultSound || value == null || value.isEmpty()) {
            return mListActivity.getString(R.string.settings_activity_default_sound);
        }

        final Ringtone ringtone = RingtoneManager.getRingtone(mListActivity, ringtoneUri);
        if (ringtone != null) {
            String ringtoneName = ringtone.getTitle(mListActivity);
            if (ringtoneName != null) {
                return ringtoneName;
            }
        }

        return mListActivity.getString(R.string.settings_activity_default_sound);
    }
}