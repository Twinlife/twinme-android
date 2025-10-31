/*
 *  Copyright (c) 2020 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.newConversationActivity;

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
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Originator;
import org.twinlife.twinme.services.AbstractTwinmeService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.users.UIContact;
import org.twinlife.twinme.ui.users.UIContactViewHolder;
import org.twinlife.twinme.utils.SectionTitleViewHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class NewConversationListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "NewConversationListA...";
    private static final boolean DEBUG = false;

    @IdRes
    private final int mNameId;
    @IdRes
    private final int mAvatarId;
    @IdRes
    private final int mTagId;
    @IdRes
    private final int mTagTitleId;
    @IdRes
    private final int mCertifiedId;
    @IdRes
    private final int mSeparatorId;
    private final NewConversationActivity mListActivity;
    private final AbstractTwinmeService mService;
    private final int mItemHeight;
    private final List<UIContact> mUIContacts;
    private final int mResource;

    private static final int CREATE_GROUP = 0;
    private static final int CONTACTS_TITLE = 1;
    private static final int CONTACTS = 2;

    NewConversationListAdapter(NewConversationActivity listActivity, AbstractTwinmeService service, int itemHeight,
                               List<UIContact> contacts,
                               @LayoutRes int resource, @IdRes int nameId, @IdRes int avatarId, @IdRes int tagId, @IdRes int tagTitleId, @IdRes int certifiedId, @IdRes int separatorId) {
        mListActivity = listActivity;
        mService = service;
        mItemHeight = itemHeight;
        mUIContacts = contacts;
        mResource = resource;
        setHasStableIds(true);
        mNameId = nameId;
        mTagId = tagId;
        mTagTitleId = tagTitleId;
        mCertifiedId = certifiedId;
        mAvatarId = avatarId;
        mSeparatorId = separatorId;
    }

    @Override
    public int getItemCount() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemCount");
        }

        if (mUIContacts == null || mUIContacts.isEmpty()) {
            return 1;
        }

        return mUIContacts.size() + 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemViewType: " + position);
        }

        if (position == 0) {
            return CREATE_GROUP;
        } else if (position == 1) {
            return CONTACTS_TITLE;
        } else {
            return CONTACTS;
        }
    }

    @Override
    public long getItemId(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemId: position=" + position);
        }

        if (position == 0) {
            return -1;
        } else if (position == 1) {
            return -1;
        } else {
            return mUIContacts.get(position - 2).getItemId();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        if (position < 2) {
            return;
        }

        boolean hideSeparator = position - 1 == mUIContacts.size();
        UIContactViewHolder<UIContact> contactViewHolder = (UIContactViewHolder<UIContact>) viewHolder;
        contactViewHolder.onBind(mListActivity, mUIContacts.get(position - 2), hideSeparator);
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mListActivity.getLayoutInflater();
        View convertView;

        if (viewType == CREATE_GROUP) {
            convertView = inflater.inflate(R.layout.create_group_item, parent, false);
            return new CreateGroupViewHolder(mListActivity, convertView);
        } else if (viewType == CONTACTS_TITLE) {
            convertView = inflater.inflate(R.layout.section_title_item, parent, false);
            return new SectionTitleViewHolder(convertView);
        } else {
            convertView = inflater.inflate(mResource, parent, false);
            ViewGroup.LayoutParams layoutParams = convertView.getLayoutParams();
            layoutParams.height = mItemHeight;
            convertView.setLayoutParams(layoutParams);
            return new UIContactViewHolder<UIContact>(mService, convertView, mNameId, mAvatarId, mTagId, mTagTitleId, 0, mCertifiedId, mSeparatorId, Design.FONT_REGULAR34);
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }
    }

    void setContacts(@NonNull List<Contact> contacts) {

        TwinmeApplication twinmeApplication = mListActivity.getTwinmeApplication();

        mUIContacts.clear();
        for (Contact contact : contacts) {
            if (contact.hasPeer()) {
                mUIContacts.add(create(twinmeApplication, contact, null));
            }
        }

        Collections.sort(mUIContacts);
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

        UIContact uiContact = null;
        for (UIContact lUIContact : mUIContacts) {
            if (lUIContact.getContact().getId().equals(contact.getId())) {
                uiContact = lUIContact;

                break;
            }
        }

        if (uiContact != null) {
            mUIContacts.remove(uiContact);

            uiContact.update(mListActivity.getTwinmeApplication(), contact, avatar);
        } else {
            uiContact = create(mListActivity.getTwinmeApplication(), contact, avatar);
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