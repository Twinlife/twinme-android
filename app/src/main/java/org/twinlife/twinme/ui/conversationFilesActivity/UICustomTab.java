/*
 *  Copyright (c) 2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.conversationFilesActivity;


import android.graphics.Paint;
import android.graphics.Rect;

import org.twinlife.twinme.skin.Design;

public class UICustomTab {

    private static final float DESIGN_TITLE_MARGIN = 22f;

    public enum CustomTabType {
        IMAGE,
        VIDEO,
        DOCUMENT,
        LINK,
        INVITE,
        SCAN,
        ALL,
        CONTACTS,
        GROUPS,
        MESSAGES,
        PERIOD,
        DATE
    }

    private final String mTitle;

    private final CustomTabType mCustomTabType;

    private boolean mSelected;

    private float mWidth = 0;

    public UICustomTab(String title, CustomTabType customTabType, boolean selected) {

        mTitle = title;
        mCustomTabType = customTabType;
        mSelected = selected;
    }

    public String getTitle() {

        return mTitle;
    }

    public CustomTabType getCustomTabType() {

        return mCustomTabType;
    }

    public boolean isSelected() {

        return mSelected;
    }

    public void setSelected(boolean selected) {

        mSelected = selected;
    }

    public float getWidth() {

        if (mWidth != 0) {
            return mWidth;
        }
        Paint paint = new Paint();
        paint.setTextSize(Design.FONT_REGULAR34.size);
        paint.setTypeface(Design.FONT_REGULAR34.typeface);
        paint.setStyle(Paint.Style.STROKE);

        float textWidth = paint.measureText(mTitle);
        if (textWidth == 0) {
            Rect rect = new Rect();
            paint.getTextBounds(mTitle, 0, mTitle.length(), rect);
            textWidth = rect.width();
        }

        mWidth = textWidth + (DESIGN_TITLE_MARGIN * Design.WIDTH_RATIO * 2);

        return mWidth;
    }

}
