/*
 *  Copyright (c) 2018-2020 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.users;

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
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.calls.SectionCallViewHolder;
import org.twinlife.twinme.ui.contacts.AddContactViewHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * A UI list adapter to display a contact within the contact list.
 * <p>
 * This list adapter is used for the group creation and group edition.
 */
public abstract class UIOriginatorListAdapter<E extends UIOriginator, C extends Originator> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "UIContactListAdapter";
    private static final boolean DEBUG = false;

    @IdRes
    protected final int mNameId;
    @IdRes
    protected final int mAvatarId;
    @IdRes
    protected final int mTagId;
    @IdRes
    protected final int mTagTitleId;
    @IdRes
    protected final int mCertifiedId;
    @IdRes
    final int mSeparatorId;
    final AbstractTwinmeActivity mListActivity;
    private final int mItemHeight;
    final List<E> mUIContacts;
    private final int mResource;
    protected final AbstractTwinmeService mService;

    protected boolean mAddContact = false;

    private static final int ADD_CONTACT = 0;
    private static final int TITLE = 1;
    private static final int CONTACT = 2;

    UIOriginatorListAdapter(@NonNull AbstractTwinmeActivity listActivity, @NonNull AbstractTwinmeService service, int itemHeight,
                            @NonNull List<E> contacts, @LayoutRes int resource, @IdRes int nameId, @IdRes int avatarId,
                            @IdRes int tagId, @IdRes int tagTitleId, @IdRes int certifiedId, @IdRes int separatorId) {
        mListActivity = listActivity;
        mService = service;
        mItemHeight = itemHeight;
        mUIContacts = contacts;
        mResource = resource;
        setHasStableIds(true);
        mNameId = nameId;
        mAvatarId = avatarId;
        mTagId = tagId;
        mTagTitleId = tagTitleId;
        mCertifiedId = certifiedId;
        mSeparatorId = separatorId;
    }

    @Override
    public int getItemCount() {
        if (DEBUG) {
            Log.d(LOG_TAG, "UIContactListAdapter.getItemCount");
        }


        if (mUIContacts == null) {
            return mAddContact ? 1 : 0;
        }

        if (mAddContact) {
            return !mUIContacts.isEmpty() ? mUIContacts.size() + 2 : 1;
        }

        return  !mUIContacts.isEmpty() ? mUIContacts.size() : 0;
    }

    @Override
    public long getItemId(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "UIContactListAdapter.getItemId: position=" + position);
        }

        if ((mAddContact && position < 2) || mUIContacts == null) {
            return -1;
        }

        if (mAddContact && mUIContacts.size() > position - 2) {
            return mUIContacts.get(position - 2).getItemId();
        }

        if (mUIContacts.size() > position) {
            return mUIContacts.get(position).getItemId();
        }

        return -1;
    }

    @Override
    public int getItemViewType(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemViewType: " + position);
        }

        if (!mAddContact) {
            return CONTACT;
        }

        if (position == 0) {
            return ADD_CONTACT;
        } else if (position == 1) {
            return TITLE;
        } else {
            return CONTACT;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "UIContactListAdapter.onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        int viewType = getItemViewType(position);

        if (viewType == CONTACT) {
            UIContactViewHolder contactViewHolder = (UIContactViewHolder) viewHolder;
            if (mAddContact) {
                position = position - 2;
            }
            boolean hideSeparator = position + 1 == mUIContacts.size();
            contactViewHolder.onBind(mListActivity, mUIContacts.get(position), hideSeparator);
        } else if (viewType == TITLE) {
            SectionCallViewHolder sectionCallViewHolder = (SectionCallViewHolder) viewHolder;
            sectionCallViewHolder.onBind(mListActivity.getString(R.string.contacts_fragment_title));
        } else if (viewType == ADD_CONTACT) {
            AddContactViewHolder addContactViewHolder = (AddContactViewHolder) viewHolder;
            //addContactViewHolder.itemView.setOnClickListener(view -> mOnCallClickListener.onAddExternalCallClick());
            addContactViewHolder.onBind(mListActivity.getString(R.string.main_activity_add_contact), mListActivity.getString(R.string.contacts_fragment_add_contact_subtitle));
        }
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "UIContactListAdapter.onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mListActivity.getLayoutInflater();

        if (viewType == CONTACT) {
            View convertView = inflater.inflate(mResource, parent, false);
            ViewGroup.LayoutParams layoutParams = convertView.getLayoutParams();
            if (mResource == R.layout.add_group_member_selected_contact) {
                layoutParams.width = mItemHeight;
            }
            layoutParams.height = mItemHeight;
            convertView.setLayoutParams(layoutParams);

            return createUIContactViewHolder(convertView);
        } else if (viewType == TITLE) {
            View convertView = inflater.inflate(R.layout.calls_fragment_section_call_item, parent, false);
            return new SectionCallViewHolder(convertView, null);
        } else {
            View convertView = inflater.inflate(R.layout.contact_fragment_add_contact_item, parent, false);
            return new AddContactViewHolder(convertView);
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "UIContactListAdapter.onViewRecycled: viewHolder=" + viewHolder);
        }

        int position = viewHolder.getBindingAdapterPosition();
        int viewType = getItemViewType(position);
        if (viewType == CONTACT && position != -1) {
            UIContactViewHolder uiContactViewHolder = (UIContactViewHolder)viewHolder;
            uiContactViewHolder.onViewRecycled();
        }

    }

    public void setAddContact(boolean addContact) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setAddContact" + addContact);
        }

        mAddContact = addContact;
    }

    /**
     * Create the UIContactViewHolder for the representation of a contact.
     *
     * @param convertView the view.
     * @return the contact view holder to display the contact.
     */
    public UIContactViewHolder<E> createUIContactViewHolder(View convertView) {

        return new UIContactViewHolder<>(mService, convertView, mNameId, mAvatarId, mTagId, mTagTitleId, 0, mCertifiedId, mSeparatorId, Design.FONT_REGULAR34);
    }

    /**
     * Set a new list of contacts.
     *
     * @param contacts the list of contacts to setup.
     */
    public void setContacts(List<C> contacts) {

        mUIContacts.clear();

        // Setup the list without avatar.
        final TwinmeApplication twinmeApplication = mListActivity.getTwinmeApplication();
        for (Originator contact : contacts) {
            mUIContacts.add(create(twinmeApplication, contact, null));
        }

        // Sort the list with the UIOriginator compareTo operation.
        Collections.sort(mUIContacts);
    }

    /**
     * Update the contact in the list.
     *
     * @param contact the contact to update or add.
     * @return the UI contact that was created.
     */
    public E updateUIContact(Originator contact, Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateUIContact: contact=" + contact);
        }

        E uiContact = null;
        for (E lUIContact : mUIContacts) {
            if (lUIContact.getContact().getId().equals(contact.getId())) {
                uiContact = lUIContact;

                break;
            }
        }

        if (uiContact != null) {
            mUIContacts.remove(uiContact);

            uiContact.update(mListActivity.getTwinmeApplication(), contact, avatar);

            if (uiContact instanceof UIContact) {
                ((UIContact) uiContact).updateContactTag(mListActivity);
            }
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
        return uiContact;
    }

    /**
     * Create a new UI element associated with the given originator.
     *
     * @param application the twinme application.
     * @param contact     the originator to link with the new UI element.
     * @return the new UI element representing the originator (contact, group member, invitation).
     */
    public abstract E create(TwinmeApplication application, Originator contact, Bitmap avatar);

    public boolean removeUIContact(@NonNull UUID contactId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "removeUIContact: contactId=" + contactId);
        }

        for (E item : mUIContacts) {
            if (item.getContact().getId().equals(contactId)) {
                mUIContacts.remove(item);
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
    public List<Originator> getContacts() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getContacts");
        }

        final List<Originator> result = new ArrayList<>();

        for (E item : mUIContacts) {
            result.add(item.getContact());
        }
        return result;
    }
}
