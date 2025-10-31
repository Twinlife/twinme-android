/*
 *  Copyright (c) 2020 twinlife SA.
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
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;

import java.util.List;

public class ShowMemberListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "ShowMemberListAdapter";
    private static final boolean DEBUG = false;

    protected static final float DESIGN_ITEM_VIEW_HEIGHT = 120f;
    protected static final int ITEM_VIEW_HEIGHT;

    protected static final int MAX_ROOM_MEMBER = 5;

    static {
        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
    }

    @NonNull
    private final AbstractTwinmeActivity mListActivity;
    private final List<UIRoomMember> mUIRoomMembers;

    private int mMemberCount;

    public ShowMemberListAdapter(@NonNull AbstractTwinmeActivity listActivity, List<UIRoomMember> roomMembers, int memberCount) {

        mListActivity = listActivity;
        mUIRoomMembers = roomMembers;
        mMemberCount = memberCount;

        setHasStableIds(true);
    }

    public void setMemberCount(int memberCount) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setMemberCount");
        }

        mMemberCount = memberCount;
    }

    @Override
    public int getItemCount() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemCount");
        }

        if (mUIRoomMembers.size() <= MAX_ROOM_MEMBER) {
            return mUIRoomMembers.size();
        }

        return MAX_ROOM_MEMBER + 1;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        ShowMemberViewHolder showMemberViewHolder = (ShowMemberViewHolder) viewHolder;

        if (position < MAX_ROOM_MEMBER && position < mUIRoomMembers.size()) {
            UIRoomMember uiRoomMember = mUIRoomMembers.get(position);
            showMemberViewHolder.onBind(uiRoomMember.getName(), uiRoomMember.getAvatar(), mMemberCount);
        } else {
            showMemberViewHolder.onBind(null, null, mMemberCount - MAX_ROOM_MEMBER);
        }
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mListActivity.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.show_room_activity_room_member_item, parent, false);
        ViewGroup.LayoutParams layoutParams = convertView.getLayoutParams();
        layoutParams.height = ITEM_VIEW_HEIGHT;
        convertView.setLayoutParams(layoutParams);
        return new ShowMemberViewHolder(convertView, ITEM_VIEW_HEIGHT, ITEM_VIEW_HEIGHT);
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }
    }
}
