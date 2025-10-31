/*
 *  Copyright (c) 2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.groups;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.ui.rooms.InformationViewHolder;
import org.twinlife.twinme.utils.SectionTitleViewHolder;

public class SettingsGroupAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "CapabilitiesAdapter";
    private static final boolean DEBUG = false;

    private final SettingsGroupActivity mActivity;

    private final static int ITEM_COUNT = 8;

    private static final int POSITION_ALLOW_INVITATION = 0;
    private static final int POSITION_ALLOW_INVITATION_INFORMATION = 1;
    private static final int POSITION_SECTION_ALLOW_MESSAGE = 2;
    private static final int POSITION_ALLOW_MESSAGE = 3;
    private static final int POSITION_ALLOW_MESSAGE_INFORMATION = 4;
    private static final int POSITION_SECTION_INVITE_MEMBER_AS_CONTACT = 5;
    private static final int POSITION_INVITE_MEMBER_AS_CONTACT = 6;
    private static final int POSITION_INVITE_MEMBER_AS_CONTACT_INFORMATION = 7;

    private static final int SWITCH = 1;
    private static final int SECTION = 2;
    private static final int INFO = 3;

    public SettingsGroupAdapter(SettingsGroupActivity activity) {

        mActivity = activity;
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

        if (position == POSITION_SECTION_ALLOW_MESSAGE || position == POSITION_SECTION_INVITE_MEMBER_AS_CONTACT) {
            return SECTION;
        } else if (position == POSITION_ALLOW_INVITATION_INFORMATION || position == POSITION_ALLOW_MESSAGE_INFORMATION || position == POSITION_INVITE_MEMBER_AS_CONTACT_INFORMATION) {
            return INFO;
        } else {
            return SWITCH;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        int viewType = getItemViewType(position);

        if (viewType == SWITCH) {
            SettingsGroupViewHolder settingsGroupViewHolder = (SettingsGroupViewHolder) viewHolder;

            boolean isSelected = false;
            String title = "";
            int switchTag = 0;

            if (position == POSITION_ALLOW_INVITATION) {
                title = mActivity.getString(R.string.create_group_activity_member_allow_invitation_title);
                switchTag = SettingsGroupActivity.ALLOW_INVITATION_SWITCH;
                isSelected = mActivity.allowInvitation();
            } else if (position == POSITION_ALLOW_MESSAGE) {
                title = mActivity.getString(R.string.create_group_activity_member_allow_post_title);
                switchTag = SettingsGroupActivity.ALLOW_MESSAGE_SWITCH;
                isSelected = mActivity.allowMessage();
            } else if (position == POSITION_INVITE_MEMBER_AS_CONTACT) {
                title = mActivity.getString(R.string.create_group_activity_member_allow_invite_member_as_contact_title);
                switchTag = SettingsGroupActivity.ALLOW_INVITE_MEMBER_AS_CONTACT_SWITCH;
                isSelected = mActivity.allowInviteMemberAsContact();
            }

            settingsGroupViewHolder.onBind(title, switchTag, isSelected);
        } else if (viewType == SECTION) {
            SectionTitleViewHolder sectionTitleViewHolder = (SectionTitleViewHolder) viewHolder;
            sectionTitleViewHolder.onBind("", false);
        } else if (viewType == INFO) {
            InformationViewHolder informationViewHolder = (InformationViewHolder) viewHolder;

            String text = "";
            if (position == POSITION_ALLOW_INVITATION_INFORMATION) {
                text = mActivity.getString(R.string.create_group_activity_member_allow_invitation_message);
            } else if (position == POSITION_ALLOW_MESSAGE_INFORMATION) {
                text = mActivity.getString(R.string.create_group_activity_member_allow_post_message);
            } else if (position == POSITION_INVITE_MEMBER_AS_CONTACT_INFORMATION) {
                text = mActivity.getString(R.string.create_group_activity_member_allow_invite_member_as_contact_message);
            }

            informationViewHolder.onBind(text, false);
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

        if (viewType == SECTION) {
            convertView = inflater.inflate(R.layout.section_title_item, parent, false);
            return new SectionTitleViewHolder(convertView);
        } else if (viewType == INFO) {
            convertView = inflater.inflate(R.layout.settings_room_activity_information_item, parent, false);
            return new InformationViewHolder(convertView);
        } else {
            convertView = inflater.inflate(R.layout.settings_group_activity_item, parent, false);
            return new SettingsGroupViewHolder(convertView, mActivity);
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }
    }
}