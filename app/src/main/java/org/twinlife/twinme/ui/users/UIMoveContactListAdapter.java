/*
 *  Copyright (c) 2019-2020 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.users;

import android.graphics.Bitmap;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.twinme.TwinmeApplication;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Originator;
import org.twinlife.twinme.services.AbstractTwinmeService;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;

import java.util.List;

public class UIMoveContactListAdapter extends UIOriginatorListAdapter<UIMoveContact, Contact> {
    private boolean mAllowSelection;

    public UIMoveContactListAdapter(AbstractTwinmeActivity listActivity, AbstractTwinmeService service, int itemHeight, List<UIMoveContact> contacts,
                                    @LayoutRes int resource, @IdRes int nameId, @IdRes int avatarId, @IdRes int certifiedId, @IdRes int separatorId) {
        super(listActivity, service, itemHeight, contacts, resource, nameId, avatarId, 0, 0, certifiedId, separatorId);

        mAllowSelection = true;
    }

    /**
     * Enable or disable the selection of a contact.  This operation must be called before rendering the list
     * and it applies to all items.  It cannot be defined by the constructor because it is set dynamically after
     * getting information about a group and the user's permissions.
     *
     * @param allow true to allow the selection of contacts.
     */
    public void setAllowSelection(boolean allow) {

        mAllowSelection = allow;
    }

    /**
     * Create a new UI element associated with the given originator.
     *
     * @param application the twinme application.
     * @param contact     the originator to link with the new UI element.
     * @return the new UI element representing the originator (contact, group member, invitation).
     */
    @Override
    public UIMoveContact create(TwinmeApplication application, Originator contact, Bitmap avatar) {

        return new UIMoveContact(application, contact, avatar);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {

        boolean hideSeparator = position + 1 == mUIContacts.size();
        ((UIMoveContactViewHolder) viewHolder).onBind(mListActivity, mUIContacts.get(position), hideSeparator, mAllowSelection);
    }

    /**
     * Create the UIContactViewHolder for the representation of a selected contact.
     *
     * @param convertView the view.
     * @return the contact view holder to display the contact.
     */
    public UIMoveContactViewHolder createUIContactViewHolder(View convertView) {

        return new UIMoveContactViewHolder(mService, convertView, mNameId, mAvatarId, mCertifiedId, mSeparatorId, mAllowSelection);
    }
}
