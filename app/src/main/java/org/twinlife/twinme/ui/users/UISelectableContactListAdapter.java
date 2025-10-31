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
import android.view.View;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.twinme.TwinmeApplication;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Group;
import org.twinlife.twinme.models.Originator;
import org.twinlife.twinme.services.AbstractTwinmeService;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;

import java.util.List;

/**
 * A UI list adapter to display a contact within the contact list that allows selecting contacts.
 * <p>
 * This list adapter is used for the group creation and group edition.
 */
public class UISelectableContactListAdapter extends UIOriginatorListAdapter<UISelectableContact, Contact> {

    boolean mAllowSelection;
    private boolean mAllowInviteMemberAsContact;

    private Group mGroup;

    public UISelectableContactListAdapter(AbstractTwinmeActivity listActivity, AbstractTwinmeService service, int itemHeight, List<UISelectableContact> contacts,
                                          @LayoutRes int resource, @IdRes int nameId, @IdRes int avatarId, @IdRes int certifiedId, @IdRes int separatorId) {
        super(listActivity, service, itemHeight, contacts, resource, nameId, avatarId, 0, 0, certifiedId, separatorId);

        mAllowSelection = true;
        mAllowInviteMemberAsContact = false;
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

    public void setAllowInviteMemberAsContact(boolean allow) {

        mAllowInviteMemberAsContact = allow;
    }

    public void setGroup(Group group) {

        mGroup = group;
    }

    /**
     * Create a new UI element associated with the given originator.
     *
     * @param application the twinme application.
     * @param contact     the originator to link with the new UI element.
     * @return the new UI element representing the originator (contact, group member, invitation).
     */
    @Override
    public UISelectableContact create(TwinmeApplication application, Originator contact, Bitmap avatar) {

        return new UISelectableContact(application, contact, avatar);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {

        UIOriginator contact = mUIContacts.get(position);

        boolean allowAdd = false;

        if (mGroup != null && mAllowInviteMemberAsContact && contact.getContact().getTwincodeOutboundId() != mGroup.getCurrentMember().getTwincodeOutboundId()) {
            allowAdd = true;
        }

        boolean hideSeparator = position + 1 == mUIContacts.size();
        ((UISelectableContactViewHolder) viewHolder).onBind(mListActivity, mUIContacts.get(position), hideSeparator, mAllowSelection, allowAdd);
    }

    /**
     * Create the UIContactViewHolder for the representation of a selected contact.
     *
     * @param convertView the view.
     * @return the contact view holder to display the contact.
     */
    public UIContactViewHolder<UISelectableContact> createUIContactViewHolder(View convertView) {

        return new UISelectableContactViewHolder(mService, convertView, mNameId, mAvatarId, mCertifiedId, mSeparatorId, mAllowSelection);
    }
}
