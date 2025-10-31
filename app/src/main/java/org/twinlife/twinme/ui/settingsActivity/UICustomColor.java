/*
 *  Copyright (c) 2020 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.settingsActivity;

public class UICustomColor {
    private static int sItemId = 0;

    private final long mItemId;

    private final String mColor;

    private boolean mSelected;

    public UICustomColor(String color) {
        mItemId = sItemId++;

        mColor = color;
        mSelected = false;
    }

    public String getColor() {

        return mColor;
    }

    public boolean isSelected() {

        return mSelected;
    }

    public void setSelected(boolean selected) {

        mSelected = selected;
    }
}