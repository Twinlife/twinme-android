/*
 *  Copyright (c) 2020-2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.groups;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.TwinmeApplication;
import org.twinlife.twinme.models.Originator;
import org.twinlife.twinme.services.AbstractTwinmeService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.rooms.ShowMemberViewHolder;
import org.twinlife.twinme.ui.users.UIContact;

import java.util.List;

public class ShowGroupMemberListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "ShowGroupMemberListA...";
    private static final boolean DEBUG = false;

    protected static final float DESIGN_ITEM_VIEW_HEIGHT = 120f;
    protected static final int ITEM_VIEW_HEIGHT;

    protected static final int MAX_ROOM_MEMBERS = 5;

    static {
        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
    }

    @NonNull
    private final AbstractTwinmeActivity mListActivity;
    @NonNull
    private final AbstractTwinmeService mService;
    private final List<UIContact> mUIMembers;

    private final int mMaxWidth;

    public ShowGroupMemberListAdapter(@NonNull AbstractTwinmeActivity listActivity, @NonNull AbstractTwinmeService service, List<UIContact> members, int maxWidth) {

        mListActivity = listActivity;
        mService = service;
        mUIMembers = members;
        mMaxWidth = maxWidth;
        setHasStableIds(true);
    }

    @Override
    public int getItemCount() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemCount");
        }

        if (mUIMembers.size() <= MAX_ROOM_MEMBERS) {
            return mUIMembers.size();
        }

        return MAX_ROOM_MEMBERS + 1;
    }

    public List<UIContact> getMembers() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getMembers");
        }

        return mUIMembers;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        ShowMemberViewHolder showMemberViewHolder = (ShowMemberViewHolder) viewHolder;

        if (position < MAX_ROOM_MEMBERS) {
            UIContact uiMember = mUIMembers.get(position);
            mService.getImage(uiMember.getContact(), (Bitmap avatar) -> {
                showMemberViewHolder.onBind(uiMember.getName(), avatar, mUIMembers.size());
            });
        } else {
            showMemberViewHolder.onBind(null, null, mUIMembers.size() - MAX_ROOM_MEMBERS);
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

        int width = Math.min(ITEM_VIEW_HEIGHT, Math.round((float) mMaxWidth / 6));
        ViewGroup.LayoutParams layoutParams = convertView.getLayoutParams();
        layoutParams.height = ITEM_VIEW_HEIGHT;
        layoutParams.width = width;

        convertView.setLayoutParams(layoutParams);
        return new ShowMemberViewHolder(convertView, width, ITEM_VIEW_HEIGHT);
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }
    }

    public UIContact create(TwinmeApplication application, Originator contact, Bitmap avatar) {

        return new UIContact(application, contact, avatar);
    }

    /**
     * Update the contact in the list.
     *
     * @param member the contact to update or add.
     */
    void updateUIMember(Originator member, Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateUIMember: contact=" + member);
        }

        UIContact uiMember = null;
        for (UIContact lUIContact : mUIMembers) {
            if (lUIContact.getContact().getId().equals(member.getId())) {
                uiMember = lUIContact;

                break;
            }
        }

        if (uiMember != null) {
            mUIMembers.remove(uiMember);

            uiMember.update(mListActivity.getTwinmeApplication(), member, avatar);
        } else {
            uiMember = create(mListActivity.getTwinmeApplication(), member, avatar);
        }

        // TBD Sort using id order when name are equals
        boolean added = false;
        int size = mUIMembers.size();
        for (int i = 0; i < size; i++) {
            String contactName1 = mUIMembers.get(i).getName();
            String contactName2 = uiMember.getName();
            if (contactName1 != null && contactName2 != null && contactName1.compareToIgnoreCase(contactName2) > 0) {
                mUIMembers.add(i, uiMember);
                added = true;
                break;
            }
        }

        if (!added) {
            mUIMembers.add(uiMember);
        }
    }

    void clearAllMembers() {
        if (DEBUG) {
            Log.d(LOG_TAG, "clearAllMembers");
        }

        mUIMembers.clear();
    }
}

