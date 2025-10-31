/*
 *  Copyright (c) 2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.rooms;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.models.RoomConfig;
import org.twinlife.twinme.ui.settingsActivity.PersonalizationViewHolder;
import org.twinlife.twinme.utils.SectionTitleViewHolder;

public class SettingsRoomListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "SettingsRoomListAdapter";
    private static final boolean DEBUG = false;

    public interface OnSettingsRoomClickListener {

        void onSelectCallMode(RoomConfig.CallMode callMode);

        void onSelectChatMode(RoomConfig.ChatMode chatMode);

        void onSelectNotificationMode(RoomConfig.NotificationMode notificationMode);
    }

    private final SettingsRoomActivity mListActivity;

    private final OnSettingsRoomClickListener mOnSettingsClickListener;

    private final static int ITEM_COUNT = 14;

    private static final int SECTION_PARTCIPANTS = 0;
    private static final int SECTION_CHAT = 2;
    private static final int SECTION_CALLS = 7;
    private static final int SECTION_NOTIFICATIONS = 10;

    private static final int POSITION_ALLOW_INVITATION = 1;

    private static final int POSITION_CHAT_CHANNEL = 3;
    private static final int POSITION_CHAT_FEEDBACK = 4;
    private static final int POSITION_CHAT_FORUM = 5;
    private static final int POSITION_CHAT_INFORMATION = 6;

    private static final int POSITION_VOICE_CALL = 8;
    private static final int POSITION_VIDEO_CALL = 9;

    private static final int POSITION_NOTIFICATIONS_INFORM = 11;
    private static final int POSITION_NOTIFICATIONS_NOISY = 12;
    private static final int POSITION_NOTIFICATIONS_QUIET = 13;

    private static final int TITLE = 0;
    private static final int SWITCH = 1;
    private static final int SELECT = 2;
    private static final int INFORMATION = 3;

    SettingsRoomListAdapter(SettingsRoomActivity listActivity, OnSettingsRoomClickListener onSettingsRoomClickListener) {

        mListActivity = listActivity;
        mOnSettingsClickListener = onSettingsRoomClickListener;
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

        if (position == SECTION_PARTCIPANTS || position == SECTION_CHAT || position == SECTION_CALLS || position == SECTION_NOTIFICATIONS) {
            return TITLE;
        } else if (position == POSITION_ALLOW_INVITATION || position == POSITION_VOICE_CALL || position == POSITION_VIDEO_CALL) {
            return SWITCH;
        } else if (position == POSITION_CHAT_INFORMATION) {
            return INFORMATION;
        } else {
            return SELECT;
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
            switch (position) {
                case SECTION_PARTCIPANTS:
                    sectionTitleViewHolder.onBind(mListActivity.getString(R.string.room_members_activity_participants_title), false);
                    break;

                case SECTION_CHAT:
                    sectionTitleViewHolder.onBind(mListActivity.getString(R.string.conversations_fragment_title), false);
                    break;

                case SECTION_CALLS:
                    sectionTitleViewHolder.onBind(mListActivity.getString(R.string.calls_fragment_title), false);
                    break;

                case SECTION_NOTIFICATIONS:
                    sectionTitleViewHolder.onBind(mListActivity.getString(R.string.notifications_fragment_title), false);
                    break;

                default:
                    break;
            }
        } else if (viewType == SWITCH) {
            RoomSettingsViewHolder settingsViewHolder = (RoomSettingsViewHolder) viewHolder;

            boolean isSelected = false;
            String title = "";
            int switchTag = 0;

            int invitationMode = mListActivity.getInvitationMode().ordinal();
            int callMode = mListActivity.getCallMode().ordinal();

            if (position == POSITION_ALLOW_INVITATION) {
                title = mListActivity.getString(R.string.settings_room_activity_allow_invite_contact);
                switchTag = SettingsRoomActivity.ALLOW_INVITATION_SWITCH;
                isSelected = invitationMode != RoomConfig.InvitationMode.INVITE_ADMIN.ordinal();
            } else if (position == POSITION_VOICE_CALL) {
                title = mListActivity.getString(R.string.conversation_activity_audio_call);
                switchTag = SettingsRoomActivity.VOICE_CALL_SWITCH;
                isSelected = callMode == RoomConfig.CallMode.CALL_VIDEO.ordinal() || callMode == RoomConfig.CallMode.CALL_AUDIO.ordinal();
            } else if (position == POSITION_VIDEO_CALL) {
                title = mListActivity.getString(R.string.conversation_activity_video_call);
                switchTag = SettingsRoomActivity.VIDEO_CALL_SWITCH;
                isSelected = callMode == RoomConfig.CallMode.CALL_VIDEO.ordinal();
            }

            settingsViewHolder.onBind(title, switchTag, isSelected);
        } else if (viewType == SELECT) {
            PersonalizationViewHolder personalizationViewHolder = (PersonalizationViewHolder) viewHolder;

            boolean isSelected = false;
            String title = "";

            int chatMode = mListActivity.getChatMode().ordinal();
            int notificationMode = mListActivity.getNotificationMode().ordinal();

            switch (position) {
                case POSITION_CHAT_CHANNEL:
                    title = mListActivity.getString(R.string.settings_room_activity_room_type_channel);
                    personalizationViewHolder.itemView.setOnClickListener(view -> mOnSettingsClickListener.onSelectChatMode(RoomConfig.ChatMode.CHAT_CHANNEL));
                    isSelected = chatMode == RoomConfig.ChatMode.CHAT_CHANNEL.ordinal();
                    break;

                case POSITION_CHAT_FEEDBACK:
                    title = mListActivity.getString(R.string.settings_room_activity_room_type_feedback);
                    personalizationViewHolder.itemView.setOnClickListener(view -> mOnSettingsClickListener.onSelectChatMode(RoomConfig.ChatMode.CHAT_FEEDBACK));
                    isSelected = chatMode == RoomConfig.ChatMode.CHAT_FEEDBACK.ordinal();
                    break;

                case POSITION_CHAT_FORUM:
                    title = mListActivity.getString(R.string.settings_room_activity_room_type_forum);
                    personalizationViewHolder.itemView.setOnClickListener(view -> mOnSettingsClickListener.onSelectChatMode(RoomConfig.ChatMode.CHAT_PUBLIC));
                    isSelected = chatMode == RoomConfig.ChatMode.CHAT_PUBLIC.ordinal();
                    break;

                case POSITION_NOTIFICATIONS_INFORM:
                    title = mListActivity.getString(R.string.settings_room_activity_conference_notifications_inform);
                    personalizationViewHolder.itemView.setOnClickListener(view -> mOnSettingsClickListener.onSelectNotificationMode(RoomConfig.NotificationMode.INFORM));
                    isSelected = notificationMode == RoomConfig.NotificationMode.INFORM.ordinal();
                    break;

                case POSITION_NOTIFICATIONS_QUIET:
                    title = mListActivity.getString(R.string.settings_room_activity_conference_notifications_quiet);
                    personalizationViewHolder.itemView.setOnClickListener(view -> mOnSettingsClickListener.onSelectNotificationMode(RoomConfig.NotificationMode.QUIET));
                    isSelected = notificationMode == RoomConfig.NotificationMode.QUIET.ordinal();
                    break;

                case POSITION_NOTIFICATIONS_NOISY:
                    title = mListActivity.getString(R.string.settings_room_activity_conference_notifications_noisy);
                    personalizationViewHolder.itemView.setOnClickListener(view -> mOnSettingsClickListener.onSelectNotificationMode(RoomConfig.NotificationMode.NOISY));
                    isSelected = notificationMode == RoomConfig.NotificationMode.NOISY.ordinal();
                    break;
            }

            personalizationViewHolder.onBind(title, isSelected);
        } else {
            InformationViewHolder informationViewHolder = (InformationViewHolder) viewHolder;

            String information = "";
            if (position == POSITION_CHAT_INFORMATION) {
                int chatMode = mListActivity.getChatMode().ordinal();
                if (chatMode == RoomConfig.ChatMode.CHAT_CHANNEL.ordinal()) {
                    information = mListActivity.getString(R.string.settings_room_activity_room_type_channel_information);
                } else if (chatMode == RoomConfig.ChatMode.CHAT_FEEDBACK.ordinal()) {
                    information = mListActivity.getString(R.string.settings_room_activity_room_type_feedback_information);
                } else if (chatMode == RoomConfig.ChatMode.CHAT_PUBLIC.ordinal()) {
                    information = mListActivity.getString(R.string.settings_room_activity_room_type_forum_information);
                }
            }
            informationViewHolder.onBind(information, false);
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
        } else if (viewType == SWITCH) {
            convertView = inflater.inflate(R.layout.settings_room_activity_item, parent, false);
            return new RoomSettingsViewHolder(convertView, mListActivity);
        } else if (viewType == SELECT) {
            convertView = inflater.inflate(R.layout.personalization_activity_item, parent, false);
            return new PersonalizationViewHolder(convertView);
        } else {
            convertView = inflater.inflate(R.layout.settings_room_activity_information_item, parent, false);
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