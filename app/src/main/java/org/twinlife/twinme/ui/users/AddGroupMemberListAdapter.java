/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.users;

import android.graphics.Bitmap;

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

public class AddGroupMemberListAdapter extends UIOriginatorListAdapter<UIContact, Contact> {

    private Bitmap mCurrentAvatar;

    private int mOffset = 0;

    public AddGroupMemberListAdapter(@NonNull AbstractTwinmeActivity listActivity, @NonNull AbstractTwinmeService service, int itemHeight,
                                @NonNull List<UIContact> contacts, @LayoutRes int resource, @IdRes int nameId, @IdRes int avatarId,
                                @IdRes int tagId, @IdRes int tagTitleId, @IdRes int certifiedId, @IdRes int separatorId, boolean fromCreateGroup, Bitmap avatar) {
        super(listActivity, service, itemHeight, contacts, resource, nameId, avatarId, tagId, tagTitleId, certifiedId, separatorId);

        mCurrentAvatar = avatar;

        if (fromCreateGroup) {
            mOffset = 1;
        }
    }

    /**
     * Create a new UI element associated with the given originator.
     *
     * @param application the twinme application.
     * @param contact     the originator to link with the new UI element.
     * @return the new UI element representing the originator (contact, group member, invitation).
     */
    @Override
    public UIContact create(TwinmeApplication application, Originator contact, Bitmap avatar) {

        UIContact uiContact = new UIContact(application, contact, avatar);
        uiContact.updateContactTag(mListActivity);
        return uiContact;
    }

    public void setAvatar(Bitmap avatar) {

        mCurrentAvatar = avatar;
    }

    @Override
    public int getItemCount() {

        return mUIContacts == null ? mOffset : mUIContacts.size() + mOffset;
    }

    @Override
    public long getItemId(int position) {

        if (mOffset == 1 && position == 0) {
            return -1;
        }
        return mUIContacts.get(position - mOffset).getItemId();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {

        UIContactViewHolder uiContactViewHolder = (UIContactViewHolder) viewHolder;
        if (mOffset == 1 && position == 0) {
            uiContactViewHolder.onBind(mListActivity, mCurrentAvatar);
        } else {
            boolean hideSeparator = position == mUIContacts.size();
            uiContactViewHolder.onBind(mListActivity, mUIContacts.get(position - mOffset), hideSeparator);
        }
    }
}

