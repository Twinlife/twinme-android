/*
 *  Copyright (c) 2018-2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.users;

import android.graphics.Bitmap;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

import org.twinlife.twinme.TwinmeApplication;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Originator;
import org.twinlife.twinme.services.AbstractTwinmeService;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;

import java.util.List;

/**
 * A UI list adapter to display a contact within the contact list.
 * <p>
 * This list adapter is used for the group creation and group edition.
 */
public class UIContactListAdapter extends UIOriginatorListAdapter<UIContact, Contact> {

    public UIContactListAdapter(@NonNull AbstractTwinmeActivity listActivity, @NonNull AbstractTwinmeService service, int itemHeight,
                                @NonNull List<UIContact> contacts, @LayoutRes int resource, @IdRes int nameId, @IdRes int avatarId,
                                @IdRes int tagId, @IdRes int tagTitleId, @IdRes int certifiedId, @IdRes int separatorId) {
        super(listActivity, service, itemHeight, contacts, resource, nameId, avatarId, tagId, tagTitleId, certifiedId, separatorId);
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
}
