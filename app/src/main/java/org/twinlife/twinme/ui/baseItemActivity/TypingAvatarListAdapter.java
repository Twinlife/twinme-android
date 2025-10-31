/*
 *  Copyright (c) 2019-2020 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import android.view.View;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;

import org.twinlife.twinme.services.AbstractTwinmeService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.users.UIContact;
import org.twinlife.twinme.ui.users.UIContactListAdapter;
import org.twinlife.twinme.ui.users.UIContactViewHolder;

import java.util.List;

class TypingAvatarListAdapter extends UIContactListAdapter {

    TypingAvatarListAdapter(AbstractTwinmeActivity listActivity, AbstractTwinmeService service, int itemHeight, List<UIContact> contacts,
                            @LayoutRes int resource, @IdRes int nameId, @IdRes int avatarId) {

        super(listActivity, service, itemHeight, contacts, resource, nameId, avatarId, 0, 0, 0, 0);
    }

    /**
     * Create the UIContactViewHolder for the representation of a selected contact.
     *
     * @param convertView the view.
     * @return the contact view holder to display the contact.
     */
    public UIContactViewHolder<UIContact> createUIContactViewHolder(View convertView) {

        return new TypingAvatarViewHolder(mService, convertView, mNameId, mAvatarId, Design.FONT_REGULAR40);
    }
}