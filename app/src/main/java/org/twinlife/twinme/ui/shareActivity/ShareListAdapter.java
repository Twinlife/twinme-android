/*
 *  Copyright (c) 2020-2022 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.shareActivity;

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
import org.twinlife.twinme.TwinmeApplication;
import org.twinlife.twinme.models.Originator;
import org.twinlife.twinme.services.AbstractTwinmeService;
import org.twinlife.twinme.ui.users.UIContact;
import org.twinlife.twinme.ui.users.UISelectableContact;
import org.twinlife.twinme.ui.users.UISelectableContactViewHolder;
import org.twinlife.twinme.utils.SectionTitleViewHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ShareListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "ShareListAdapter";
    private static final boolean DEBUG = false;

    @IdRes
    private final int mNameId;
    @IdRes
    private final int mAvatarId;
    @IdRes
    private final int mSeparatorId;
    @IdRes
    private final int mCertifiedId;
    private final ShareActivity mListActivity;
    private final AbstractTwinmeService mService;
    private final int mItemHeight;
    private final List<UISelectableContact> mUIContacts;
    private final List<UISelectableContact> mUIGroups;
    private final int mResource;
    private int mMinContactPosition = -1;
    private int mMinGroupPosition = -1;

    private static final int CONTACTS_TITLE = 0;
    private static final int CONTACTS = 1;
    private static final int GROUPS_TITLE = 2;
    private static final int GROUPS = 3;

    ShareListAdapter(ShareActivity listActivity, AbstractTwinmeService service, int itemHeight, List<UISelectableContact> contacts, List<UISelectableContact> groups,
                     @LayoutRes int resource, @IdRes int nameId, @IdRes int avatarId, @IdRes int certifiedId, @IdRes int separatorId) {
        mListActivity = listActivity;
        mService = service;
        mItemHeight = itemHeight;
        mUIContacts = contacts;
        mUIGroups = groups;
        mResource = resource;
        setHasStableIds(true);
        mNameId = nameId;
        mAvatarId = avatarId;
        mCertifiedId = certifiedId;
        mSeparatorId = separatorId;
    }

    @Override
    public int getItemCount() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemCount");
        }

        int count = 0;

        if (mUIContacts.size() > 0) {
            count = mUIContacts.size() + 1;
        }

        if (mUIGroups.size() > 0) {
            count = count + mUIGroups.size() + 1;
        }

        return count;
    }

    @Override
    public int getItemViewType(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemViewType: " + position);
        }

        int positionContactMin = -1;
        int positionContactMax = -1;
        if (mUIContacts.size() > 0) {
            positionContactMin = 1;
            positionContactMax = mUIContacts.size();
        }

        int positionGroupMin = -1;
        int positionGroupMax = getItemCount() - 1;
        if (mUIGroups.size() > 0) {
            positionGroupMin = positionContactMax + 2;
        }

        if (position == 0 && mUIContacts.size() > 0) {
            mMinContactPosition = position + 1;
            return CONTACTS_TITLE;
        } else if (position == positionGroupMin - 1 && mUIGroups.size() > 0) {
            mMinGroupPosition = position + 1;
            return GROUPS_TITLE;
        } else if (position >= positionContactMin && position <= positionContactMax) {
            return CONTACTS;
        } else if (position >= positionGroupMin && position <= positionGroupMax) {
            return GROUPS;
        }

        return -1;
    }

    @Override
    public long getItemId(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemId: position=" + position);
        }

        int positionContactMin = -1;
        int positionContactMax = -1;
        if (mUIContacts.size() > 0) {
            positionContactMin = 1;
            positionContactMax = mUIContacts.size();
        }

        int positionGroupMin = -1;
        int positionGroupMax = getItemCount() - 1;
        if (mUIGroups.size() > 0) {
            positionGroupMin = positionContactMax + 2;
        }

        if (position == 0 && mUIContacts.size() > 0) {
            return -1;
        } else if (position == positionGroupMin - 1 && mUIGroups.size() > 0) {
            return -1;
        } else if (position >= positionContactMin && position <= positionContactMax) {
            return mUIContacts.get(position - positionContactMin).getItemId();
        } else if (position >= positionGroupMin && position <= positionGroupMax) {
            return mUIGroups.get(position - positionGroupMin).getItemId();
        }

        return -1;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        int viewType = getItemViewType(position);

        if (viewType == CONTACTS_TITLE) {
            SectionTitleViewHolder sectionTitleViewHolder = (SectionTitleViewHolder) viewHolder;
            sectionTitleViewHolder.onBind(mListActivity.getString(R.string.share_activity_contact_list), false);
        } else if (viewType == GROUPS_TITLE) {
            SectionTitleViewHolder sectionTitleViewHolder = (SectionTitleViewHolder) viewHolder;
            sectionTitleViewHolder.onBind(mListActivity.getString(R.string.share_activity_group_list), false);
        } else if (viewType == CONTACTS && position >= mMinContactPosition && position - mMinContactPosition < mUIContacts.size()) {
            boolean hideSeparator = position - 1 == mUIContacts.size();
            ((UISelectableContactViewHolder) viewHolder).onBind(mListActivity, mUIContacts.get(position - mMinContactPosition), hideSeparator, true, false);
        } else if (viewType == GROUPS && position >= mMinGroupPosition && position - mMinGroupPosition < mUIGroups.size()) {
            boolean hideSeparator = position - 1 == mUIGroups.size();
            ((UISelectableContactViewHolder) viewHolder).onBind(mListActivity, mUIGroups.get(position - mMinGroupPosition), hideSeparator, true, false);
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

        if (viewType == CONTACTS_TITLE || viewType == GROUPS_TITLE) {
            convertView = inflater.inflate(R.layout.section_title_item, parent, false);
            return new SectionTitleViewHolder(convertView);
        } else {
            convertView = inflater.inflate(mResource, parent, false);
            ViewGroup.LayoutParams layoutParams = convertView.getLayoutParams();
            layoutParams.height = mItemHeight;
            convertView.setLayoutParams(layoutParams);
            return new UISelectableContactViewHolder(mService, convertView, mNameId, mAvatarId, mCertifiedId, mSeparatorId, true);
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }
    }

    /**
     * Update the contact in the list.
     *
     * @param contact the contact to update or add.
     */
    void updateUIContact(Originator contact, Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateUIContact: contact=" + contact);
        }

        UISelectableContact uiContact = null;
        for (UISelectableContact lUIContact : mUIContacts) {
            if (lUIContact.getContact().getId().equals(contact.getId())) {
                uiContact = lUIContact;

                break;
            }
        }

        if (uiContact != null) {
            mUIContacts.remove(uiContact);

            uiContact.update(mListActivity.getTwinmeApplication(), contact, avatar);
        } else {
            uiContact = new UISelectableContact(mListActivity.getTwinmeApplication(), contact, avatar);
        }

        // TBD Sort using id order when name are equals
        boolean added = false;
        int size = mUIContacts.size();
        for (int i = 0; i < size; i++) {
            String contactName1 = mUIContacts.get(i).getName();
            String contactName2 = uiContact.getName();
            if (contactName1 != null && contactName2 != null && contactName1.compareToIgnoreCase(contactName2) > 0) {
                mUIContacts.add(i, uiContact);
                added = true;
                break;
            }
        }

        if (!added) {
            mUIContacts.add(uiContact);
        }
    }

    /**
     * Update the group in the list.
     *
     * @param group the group to update or add.
     */
    void updateUIGroup(Originator group, Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateUIGroup: group=" + group);
        }

        UISelectableContact uiContact = null;
        for (UISelectableContact lUIContact : mUIGroups) {
            if (lUIContact.getContact().getId().equals(group.getId())) {
                uiContact = lUIContact;

                break;
            }
        }

        if (uiContact != null) {
            mUIContacts.remove(uiContact);

            uiContact.update(mListActivity.getTwinmeApplication(), group, avatar);
        } else {
            uiContact = new UISelectableContact(mListActivity.getTwinmeApplication(), group, avatar);
        }

        // TBD Sort using id order when name are equals
        boolean added = false;
        int size = mUIGroups.size();
        for (int i = 0; i < size; i++) {
            String contactName1 = mUIGroups.get(i).getName();
            String contactName2 = uiContact.getName();
            if (contactName1 != null && contactName2 != null && contactName1.compareToIgnoreCase(contactName2) > 0) {
                mUIGroups.add(i, uiContact);
                added = true;
                break;
            }
        }

        if (!added) {
            mUIGroups.add(uiContact);
        }
    }

    int getMinContactPosition() {

        return mMinContactPosition;
    }

    int getMinGroupPosition() {

        return mMinGroupPosition;
    }

    public UIContact create(TwinmeApplication application, Originator contact, Bitmap avatar) {

        UIContact uiContact = new UIContact(application, contact, avatar);
        uiContact.updateContactTag(mListActivity);
        return uiContact;
    }

    void removeUIContact(UUID contactId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "removeUIContact: contactId=" + contactId);
        }

        for (UIContact item : mUIContacts) {
            if (item.getContact().getId().equals(contactId)) {
                mUIContacts.remove(item);

                return;
            }
        }

    }

    /**
     * Get the list of contacts.
     *
     * @return the list of contacts.
     */
    public List<Originator> getContacts() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getContacts");
        }

        final List<Originator> result = new ArrayList<>();

        for (UIContact item : mUIContacts) {
            result.add(item.getContact());
        }
        return result;
    }
}