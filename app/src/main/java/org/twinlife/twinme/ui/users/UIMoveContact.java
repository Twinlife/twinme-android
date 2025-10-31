/*
 *  Copyright (c) 2019-2020 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.users;

import android.graphics.Bitmap;

import org.twinlife.twinme.TwinmeApplication;
import org.twinlife.twinme.models.Originator;

public class UIMoveContact extends UISelectableContact {

    private boolean mCanMove;

    UIMoveContact(TwinmeApplication twinmeApplication, Originator contact, Bitmap avatar) {
        super(twinmeApplication, contact, avatar);

        mCanMove = true;
    }

    public boolean canMove() {

        return mCanMove;
    }

    public void setCanMove(boolean canMove) {

        mCanMove = canMove;
    }
}
