/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.utils;

public class UIMenuSelectAction {

    private final String mTitle;

    private final int mIcon;

    public UIMenuSelectAction(String title, int icon) {

        mTitle = title;
        mIcon = icon;
    }

    public String getTitle() {

        return mTitle;
    }

    public int getIcon() {

        return mIcon;
    }
}