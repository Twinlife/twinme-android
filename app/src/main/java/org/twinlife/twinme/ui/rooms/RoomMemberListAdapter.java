/*
 *  Copyright (c) 2020-2021 twinlife SA.
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
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.SectionTitleViewHolder;

import java.util.List;

public class RoomMemberListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "RoomMemberListAdapter";
    private static final boolean DEBUG = false;

    protected static final float DESIGN_ITEM_VIEW_HEIGHT = 120f;
    protected static final int ITEM_VIEW_HEIGHT;

    static {
        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
    }

    public interface OnRoomMemberClickListener {

        void onMemberClick(UIRoomMember uiMember);
    }

    @NonNull
    private final RoomMembersActivity mListActivity;
    private final List<UIRoomMember> mUIAdmins;
    private final List<UIRoomMember> mUIMembers;
    private int mMinMemberPosition = -1;

    @Nullable
    private final OnRoomMemberClickListener mOnRoomMemberClickListener;

    private static final int ADMIN_TITLE = 0;
    private static final int ADMIN = 1;
    private static final int MEMBERS_TITLE = 2;
    private static final int MEMBERS = 3;

    RoomMemberListAdapter(@NonNull RoomMembersActivity listActivity, List<UIRoomMember> admins,
                          List<UIRoomMember> members, @Nullable OnRoomMemberClickListener onRoomMemberClickListener) {

        mListActivity = listActivity;
        mUIAdmins = admins;
        mUIMembers = members;
        setHasStableIds(true);

        mOnRoomMemberClickListener = onRoomMemberClickListener;
    }

    @Override
    public int getItemCount() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemCount");
        }

        int count = 0;

        if (!mUIAdmins.isEmpty()) {
            count = count + mUIAdmins.size() + 1;
        }

        if (!mUIMembers.isEmpty()) {
            count = count + mUIMembers.size() + 1;
        }

        return count;
    }

    @Override
    public int getItemViewType(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemViewType: " + position);
        }

        int adminCount = mUIAdmins.size();
        int positionMemberMin = -1;
        int positionMemberMax = -1;
        if (!mUIMembers.isEmpty()) {
            positionMemberMin = adminCount + 2;
            positionMemberMax = mUIAdmins.size() + mUIMembers.size() + 1;
        }

        if (position == 0) {
            return ADMIN_TITLE;
        } else if (position <= adminCount) {
            return ADMIN;
        } else if (position == adminCount + 1 && !mUIMembers.isEmpty()) {
            mMinMemberPosition = position + 1;
            return MEMBERS_TITLE;
        } else if (position >= positionMemberMin && position <= positionMemberMax) {
            return MEMBERS;
        }

        return -1;
    }

    @Override
    public long getItemId(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemId: position=" + position);
        }

        int adminCount = mUIAdmins.size();
        int positionMemberMin = -1;
        int positionMemberMax = -1;
        if (!mUIMembers.isEmpty()) {
            positionMemberMin = adminCount + 2;
            positionMemberMax = mUIAdmins.size() + mUIMembers.size() + 1;
        }

        if (position == 0) {
            return -1;
        } else if (position <= adminCount) {
            if (adminCount == 0) {
                return -1;
            }
            return mUIAdmins.get(position - 1).getItemId();
        } else if (position == adminCount + 1 && !mUIMembers.isEmpty()) {
            return -1;
        } else if (position >= positionMemberMin && position <= positionMemberMax) {
            return mUIMembers.get(position - positionMemberMin).getItemId();
        }

        return -1;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        int viewType = getItemViewType(position);

        if (viewType == ADMIN_TITLE) {
            SectionTitleViewHolder sectionTitleViewHolder = (SectionTitleViewHolder) viewHolder;
            sectionTitleViewHolder.onBind(mListActivity.getString(R.string.group_member_activity_section_administrator), false);
        } else if (viewType == MEMBERS_TITLE) {
            SectionTitleViewHolder sectionTitleViewHolder = (SectionTitleViewHolder) viewHolder;
            sectionTitleViewHolder.onBind(mListActivity.getString(R.string.room_members_activity_participants_title), false);
        } else if (viewType == ADMIN) {
            boolean hideSeparator = mUIAdmins.size() == position;
            RoomMemberViewHolder roomMemberViewHolder = (RoomMemberViewHolder) viewHolder;
            UIRoomMember uiAdmin = mUIAdmins.get(position - 1);
            if (mOnRoomMemberClickListener != null) {
                roomMemberViewHolder.itemView.setOnClickListener(view -> mOnRoomMemberClickListener.onMemberClick(uiAdmin));
            }
            roomMemberViewHolder.onBind(mListActivity, uiAdmin, hideSeparator);
        } else if (viewType == MEMBERS) {
            boolean hideSeparator = mMinMemberPosition + mUIMembers.size() - 1 == position;
            RoomMemberViewHolder roomMemberViewHolder = (RoomMemberViewHolder) viewHolder;
            UIRoomMember uiMember = mUIMembers.get(position - mMinMemberPosition);
            if (mOnRoomMemberClickListener != null) {
                roomMemberViewHolder.itemView.setOnClickListener(view -> mOnRoomMemberClickListener.onMemberClick(uiMember));
            }
            roomMemberViewHolder.onBind(mListActivity, uiMember, hideSeparator);
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

        switch (viewType) {
            case ADMIN:
            case MEMBERS: {
                convertView = inflater.inflate(R.layout.room_members_activity_item, parent, false);
                ViewGroup.LayoutParams layoutParams = convertView.getLayoutParams();
                layoutParams.height = ITEM_VIEW_HEIGHT;
                convertView.setLayoutParams(layoutParams);
                return new RoomMemberViewHolder(convertView);
            }
            case ADMIN_TITLE:
            case MEMBERS_TITLE:
            default: {
                convertView = inflater.inflate(R.layout.section_title_item, parent, false);
                return new SectionTitleViewHolder(convertView);
            }
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }
    }
}