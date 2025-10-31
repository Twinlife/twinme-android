/*
 *  Copyright (c) 2020-2022 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.calls;

import androidx.annotation.NonNull;

import org.twinlife.twinlife.ConversationService.CallDescriptor;
import org.twinlife.twinme.models.Originator;
import org.twinlife.twinme.ui.users.UIOriginator;

import java.util.ArrayList;
import java.util.List;

public class UICall {

    private static int sItemId = 0;

    private final long mItemId;

    @NonNull
    private final UIOriginator mUIContact;

    @NonNull
    private final List<CallDescriptor> mCallDescriptors;

    public UICall(@NonNull UIOriginator uiContact, @NonNull CallDescriptor callDescriptor) {

        mItemId = sItemId++;

        mUIContact = uiContact;

        mCallDescriptors = new ArrayList<>();
        mCallDescriptors.add(callDescriptor);
    }

    public long getItemId() {

        return mItemId;
    }

    public UIOriginator getUIContact() {

        return mUIContact;
    }

    public Originator getContact() {

        return mUIContact.getContact();
    }

    boolean isCertified() {

        return mUIContact.isCertified();
    }

    public void addCallDescriptor(CallDescriptor callDescriptor) {

        mCallDescriptors.add(0, callDescriptor);
    }

    public CallDescriptor getLastCallDescriptor() {

        return mCallDescriptors.get(0);
    }

    public List<CallDescriptor> getCallDescriptors() {

        return mCallDescriptors;
    }

    public int getCount() {

        return mCallDescriptors.size();
    }
}
