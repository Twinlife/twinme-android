/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (fabrice.trescartes@twin.life)
 */

package org.twinlife.twinme.ui.contacts;

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

public class ShareContactAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "ShareContactAdapter";
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
    private final ShareContactActivity mListActivity;
    private final AbstractTwinmeService mService;
    private final int mItemHeight;
    private final List<UIContact> mUIContacts;
    private final List<ShareContactItem> mItems = new ArrayList<>();
    private final int mResource;

    private UUID mContactId;
    private Bitmap mShareContactAvatar;
    private String mShareContactName;

    private static final int TITLE = 0;
    private static final int SHARE_CONTACT = 1;
    private static final int CONTACT = 2;

    ShareContactAdapter(ShareContactActivity activity, AbstractTwinmeService service, int itemHeight,
                               List<UIContact> contacts,
                               @LayoutRes int resource, @IdRes int nameId, @IdRes int avatarId, @IdRes int tagId, @IdRes int tagTitleId, @IdRes int certifiedId, @IdRes int separatorId) {
        mListActivity = activity;
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

        loadItems();
    }

    public void updateShareContactInfo(String name, Bitmap avatar) {

        mShareContactName = name;
        mShareContactAvatar = avatar;

        notifyItemChanged(0);
    }

    public String getShareContactName() {

        return mShareContactName;
    }

    @Override
    public int getItemCount() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemCount");
        }

        return mItems.size();
    }

    @Override
    public long getItemId(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemId: position=" + position);
        }

        ShareContactItem item = mItems.get(position);

        if (item.getType() == ShareContactItem.ShareContactType.CONTACT && item.getContact() != null) {
            return item.getContact().getItemId();
        } else {
            return -1;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemViewType: " + position);
        }

        ShareContactItem item = mItems.get(position);

        switch (item.getType()) {
            case SHARE_CONTACT:
                return SHARE_CONTACT;

            case SECTION:
                    return TITLE;
            default:
                return CONTACT;
        }
    }

    public void updateContacts() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateContacts");
        }

        loadItems();
        notifyItemRangeChanged(0, mItems.size());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        ShareContactItem item = mItems.get(position);

        if (item.getType() == ShareContactItem.ShareContactType.SHARE_CONTACT) {
            ShareContactViewHolder shareContactViewHolder = (ShareContactViewHolder) viewHolder;
            shareContactViewHolder.onBind(mShareContactName, mShareContactAvatar);
        } else if (item.getType() == ShareContactItem.ShareContactType.SECTION) {
            SectionTitleViewHolder sectionTitleViewHolder = (SectionTitleViewHolder) viewHolder;
            sectionTitleViewHolder.onBind(mListActivity.getString(R.string.share_view_contact_list), true);
        } else {
            boolean hideSeparator = position - 1 == mUIContacts.size();
            UIContactViewHolder<UIContact> contactViewHolder = (UIContactViewHolder<UIContact>) viewHolder;
            contactViewHolder.itemView.setOnClickListener((v) -> mListActivity.onUIContactClick(item.getContact()));
            contactViewHolder.onBind(mListActivity, item.getContact(), hideSeparator);
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
        } else if (viewType == SHARE_CONTACT) {
            convertView = inflater.inflate(R.layout.share_contact_item, parent, false);
            return new ShareContactViewHolder(convertView);
        } else {
            convertView = inflater.inflate(mResource, parent, false);
            ViewGroup.LayoutParams layoutParams = convertView.getLayoutParams();
            layoutParams.height = mItemHeight;
            convertView.setLayoutParams(layoutParams);
            return new UIContactViewHolder<UIContact>(mService, convertView, mNameId, mAvatarId, mTagId, mTagTitleId, 0, 0, mCertifiedId, mSeparatorId, Design.FONT_REGULAR34);
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }
    }

    void setContacts(@NonNull List<Contact> contacts, UUID contactId) {

        TwinmeApplication twinmeApplication = mListActivity.getTwinmeApplication();

        mContactId = contactId;
        mUIContacts.clear();
        for (Contact contact : contacts) {
            if (contact.hasPeer() && contact.hasPrivatePeer()) {
                UIContact uiContact = create(twinmeApplication, contact, null);
                if (mContactId.equals(contact.getId())) {
                    mShareContactName = uiContact.getName();
                    mService.getImage(uiContact.getContact(), (Bitmap avatar) -> {
                        mShareContactAvatar = avatar;
                        notifyItemChanged(0);
                    });
                } else {
                    mUIContacts.add(uiContact);
                }
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

        if (mContactId.equals(contact.getId())) {
            mShareContactName = uiContact.getName();
            mShareContactAvatar = avatar;
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

    private void loadItems() {
        if (DEBUG) {
            Log.d(LOG_TAG, "loadItems");
        }

        mItems.clear();

        mItems.add(new ShareContactItem(ShareContactItem.ShareContactType.SHARE_CONTACT, null));
        mItems.add(new ShareContactItem(ShareContactItem.ShareContactType.SECTION, null));

        for (UIContact uiContact : mUIContacts) {
            mItems.add(new ShareContactItem(ShareContactItem.ShareContactType.CONTACT, uiContact));
        }
    }
}