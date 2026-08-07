/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.accountMigrationActivity;

public class UIAccountMigrationItem {

    private final String mText;
    private final int mPosition;

    public UIAccountMigrationItem(int position, String text) {

        mPosition = position;
        mText = text;
    }

    public int getPosition() {

        return mPosition;
    }

    public String getText() {

        return mText;
    }
}
