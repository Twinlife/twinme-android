/*
 *  Copyright (c) 2018 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.users;

import android.graphics.Bitmap;

import org.twinlife.twinme.TwinmeApplication;
import org.twinlife.twinme.models.Originator;

/**
 * A UIContact that can be selected: it adds a boolean state that tells the ViewHolder whether the contact is selected or not.
 */

public class UISelectableContact extends UIContact {

    private boolean mSelected;
    private boolean mInvited;

    public UISelectableContact(TwinmeApplication twinmeApplication, Originator contact, Bitmap avatar) {
        super(twinmeApplication, contact, avatar);

        mSelected = false;
        mInvited = false;
    }

    public boolean isSelected() {

        return mSelected;
    }

    public void setSelected(boolean selected) {

        mSelected = selected;
    }

    public boolean isInvited() {

        return mInvited;
    }

    public void setInvited(boolean invited) {

        mInvited = invited;
    }
}
