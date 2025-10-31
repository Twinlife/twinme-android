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

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinme.TwinmeApplication;
import org.twinlife.twinme.models.Originator;
import org.twinlife.twinme.services.AbstractTwinmeService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.users.UIContact;
import org.twinlife.twinme.ui.users.UIContactViewHolder;
import org.twinlife.twinme.ui.users.UIInvitation;
import org.twinlife.twinme.ui.users.UIInvitationViewHolder;
import org.twinlife.twinme.utils.SectionTitleViewHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GroupMemberListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "GroupMemberListAdapter";
    private static final boolean DEBUG = false;

    public interface OnGroupMemberClickListener {

        void onAdminClick(UIContact uiAdmin);

        void onMemberClick(UIContact uiMember);

        void onInvitationClick(UIInvitation uiInvitation);
    }

    @IdRes
    private final int mNameId;
    @IdRes
    private final int mAvatarId;
    @IdRes
    private final int mSeparatorId;
    @NonNull
    private final GroupMemberActivity mListActivity;
    @NonNull
    private final AbstractTwinmeService mService;
    private final int mItemHeight;
    private UIContact mAdmin;
    private final List<UIContact> mUIMembers;
    private final List<UIInvitation> mUIInvitations;
    private final int mResource;
    private int mMinMemberPosition = -1;
    private int mMinInvitationPosition = -1;

    private final OnGroupMemberClickListener mOnGroupMemberClickListener;

    private static final int ADMIN_TITLE = 0;
    private static final int ADMIN = 1;
    private static final int MEMBERS_TITLE = 2;
    private static final int MEMBERS = 3;
    private static final int INVITATION_TITLE = 4;
    private static final int INVITATION = 5;

    GroupMemberListAdapter(@NonNull GroupMemberActivity listActivity, @NonNull AbstractTwinmeService service, int itemHeight, UIContact admin,
                           List<UIContact> members, List<UIInvitation> invitations,
                           @LayoutRes int resource, @IdRes int nameId, @IdRes int avatarId, @IdRes int separatorId, OnGroupMemberClickListener onGroupMemberClickListener) {

        mListActivity = listActivity;
        mService = service;
        mItemHeight = itemHeight;
        mAdmin = admin;
        mUIMembers = members;
        mUIInvitations = invitations;
        mResource = resource;
        setHasStableIds(true);
        mNameId = nameId;
        mAvatarId = avatarId;
        mSeparatorId = separatorId;

        mOnGroupMemberClickListener = onGroupMemberClickListener;
    }

    @Override
    public int getItemCount() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemCount");
        }

        int count = 2;

        if (!mUIMembers.isEmpty()) {
            count = count + mUIMembers.size() + 1;
        }

        if (!mUIInvitations.isEmpty()) {
            count = count + mUIInvitations.size() + 1;
        }

        return count;
    }

    @Override
    public int getItemViewType(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemViewType: " + position);
        }

        int positionMemberMin = -1;
        int positionMemberMax = -1;
        if (!mUIMembers.isEmpty()) {
            positionMemberMin = 3;
            positionMemberMax = mUIMembers.size() + 2;
        }

        int positionInvitationMin = -1;
        int positionInvitationMax = getItemCount() - 1;
        if (!mUIInvitations.isEmpty()) {
            if (!mUIMembers.isEmpty()) {
                positionInvitationMin = positionMemberMax + 2;
            } else {
                positionInvitationMin = 3;
            }
        }

        if (position == 0) {
            return ADMIN_TITLE;
        } else if (position == 1) {
            return ADMIN;
        } else if (position == 2 && !mUIMembers.isEmpty()) {
            mMinMemberPosition = position + 1;
            return MEMBERS_TITLE;
        } else if (position == positionInvitationMin - 1 && !mUIInvitations.isEmpty()) {
            mMinInvitationPosition = position + 1;
            return INVITATION_TITLE;
        } else if (position >= positionMemberMin && position <= positionMemberMax) {
            return MEMBERS;
        } else if (position >= positionInvitationMin && position <= positionInvitationMax) {
            return INVITATION;
        }

        return -1;
    }

    @Override
    public long getItemId(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemId: position=" + position);
        }

        int positionMemberMin = -1;
        int positionMemberMax = -1;
        if (!mUIMembers.isEmpty()) {
            positionMemberMin = 3;
            positionMemberMax = mUIMembers.size() + 2;
        }

        int positionInvitationMin = -1;
        int positionInvitationMax = getItemCount() - 1;
        if (!mUIInvitations.isEmpty()) {
            if (!mUIMembers.isEmpty()) {
                positionInvitationMin = positionMemberMax + 2;
            } else {
                positionInvitationMin = 3;
            }
        }

        if (position == 0) {
            return -1;
        } else if (position == 1) {
            if (mAdmin == null) {
                return -1;
            }
            return mAdmin.getItemId();
        } else if (position == 2 && !mUIMembers.isEmpty()) {
            return -1;
        } else if (position == positionInvitationMin - 1 && !mUIInvitations.isEmpty()) {
            return -1;
        } else if (position >= positionMemberMin && position <= positionMemberMax) {
            return mUIMembers.get(position - positionMemberMin).getItemId();
        } else if (position >= positionInvitationMin && position <= positionInvitationMax) {
            return mUIInvitations.get(position - positionInvitationMin).getItemId();
        }

        return -1;
    }

    @SuppressWarnings("unchecked")
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
            sectionTitleViewHolder.onBind(mListActivity.getString(R.string.group_member_activity_section_member), false);
        } else if (viewType == INVITATION_TITLE) {
            SectionTitleViewHolder sectionTitleViewHolder = (SectionTitleViewHolder) viewHolder;
            sectionTitleViewHolder.onBind(mListActivity.getString(R.string.group_member_activity_section_invitation), false);
        } else if (viewType == ADMIN) {
            UIContactViewHolder<UIContact> contactViewHolder = (UIContactViewHolder<UIContact>) viewHolder;
            if (mAdmin != null) {
                contactViewHolder.itemView.setOnClickListener(view -> mOnGroupMemberClickListener.onAdminClick(mAdmin));
            }
            contactViewHolder.onBind(mListActivity, mAdmin, true);
        } else if (viewType == MEMBERS) {
            boolean hideSeparator = mMinMemberPosition + mUIMembers.size() - 1 == position;
            UIContactViewHolder<UIContact> contactViewHolder = (UIContactViewHolder<UIContact>) viewHolder;
            UIContact uiMember = mUIMembers.get(position - mMinMemberPosition);
            contactViewHolder.itemView.setOnClickListener(view -> mOnGroupMemberClickListener.onMemberClick(uiMember));
            contactViewHolder.onBind(mListActivity, uiMember, hideSeparator);
        } else if (viewType == INVITATION) {
            boolean hideSeparator = mMinInvitationPosition + mUIInvitations.size() - 1 == position;
            UIInvitationViewHolder invitationViewHolder = (UIInvitationViewHolder) viewHolder;
            UIInvitation uiInvitation = mUIInvitations.get(invitationViewHolder.getBindingAdapterPosition() - mMinInvitationPosition);
            invitationViewHolder.itemView.setOnClickListener(view -> mOnGroupMemberClickListener.onInvitationClick(uiInvitation));
            invitationViewHolder.onBind(mListActivity, uiInvitation, hideSeparator);
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
                convertView = inflater.inflate(mResource, parent, false);
                ViewGroup.LayoutParams layoutParams = convertView.getLayoutParams();
                layoutParams.height = mItemHeight;
                convertView.setLayoutParams(layoutParams);
                return new UIContactViewHolder<UIContact>(mService, convertView, mNameId, mAvatarId, 0, 0, 0, 0, mSeparatorId, Design.FONT_REGULAR34);
            }
            case INVITATION: {
                convertView = inflater.inflate(mResource, parent, false);
                ViewGroup.LayoutParams layoutParams = convertView.getLayoutParams();
                layoutParams.height = mItemHeight;
                convertView.setLayoutParams(layoutParams);
                return new UIInvitationViewHolder(mService, convertView, mNameId, mAvatarId, mSeparatorId);
            }
            case ADMIN_TITLE:
            case MEMBERS_TITLE:
            case INVITATION_TITLE:
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

    void updateAdmin(Originator admin, Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateAdmin: admin=" + admin);
        }

        mAdmin = new UIContact(mListActivity.getTwinmeApplication(), admin, avatar);
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

    /**
     * Update the group in the list.
     *
     * @param group the group to update or add.
     */
    void updateUIInvitation(Originator group, ConversationService.InvitationDescriptor invitation, Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateUIGroup: group=" + group);
        }

        UIInvitation uiInvitation = null;
        for (UIInvitation lUIContact : mUIInvitations) {
            if (lUIContact.getContact().getId().equals(group.getId())) {
                uiInvitation = lUIContact;

                break;
            }
        }

        if (uiInvitation != null) {
            mUIInvitations.remove(uiInvitation);

            uiInvitation.update(mListActivity.getTwinmeApplication(), group, avatar);
        } else {
            uiInvitation = createInvitation(mListActivity.getTwinmeApplication(), group, avatar);
        }

        uiInvitation.setInvitation(invitation);

        // TBD Sort using id order when name are equals
        boolean added = false;
        int size = mUIInvitations.size();
        for (int i = 0; i < size; i++) {
            String contactName1 = mUIInvitations.get(i).getName();
            String contactName2 = uiInvitation.getName();
            if (contactName1 != null && contactName2 != null && contactName1.compareToIgnoreCase(contactName2) > 0) {
                mUIInvitations.add(i, uiInvitation);
                added = true;
                break;
            }
        }

        if (!added) {
            mUIInvitations.add(uiInvitation);
        }
    }

    public UIContact create(TwinmeApplication application, Originator contact, Bitmap avatar) {

        return new UIContact(application, contact, avatar);
    }

    public UIInvitation createInvitation(TwinmeApplication application, Originator contact, Bitmap avatar) {

        return new UIInvitation(application, contact, avatar);
    }

    @SuppressWarnings("UnusedReturnValue")
    boolean removeUIContact(UUID contactId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "removeUIContact: contactId=" + contactId);
        }

        for (UIContact item : mUIMembers) {
            if (item.getContact().getId().equals(contactId)) {
                mUIMembers.remove(item);

                return true;
            }
        }

        return false;
    }

    @SuppressWarnings("UnusedReturnValue")
    boolean removeUIInvitation(UUID invitationId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "removeUIInvitation: invitationId=" + invitationId);
        }

        for (UIInvitation item : mUIInvitations) {
            if (item.getContact().getId().equals(invitationId)) {
                mUIInvitations.remove(item);

                return true;
            }
        }

        return false;
    }

    /**
     * Get the list of contacts.
     *
     * @return the list of contacts.
     */
    @SuppressWarnings("unused")
    public List<Originator> getContacts() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getContacts");
        }

        final List<Originator> result = new ArrayList<>();

        for (UIContact item : mUIMembers) {
            result.add(item.getContact());
        }
        return result;
    }

}