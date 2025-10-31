/*
 *  Copyright (c) 2019-2020 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.welcomeActivity;

import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import org.twinlife.twinme.skin.Design;

public class UIWelcome {

    private static int sItemId = 0;

    private final long mItemId;

    private final String mText;

    private final int mImageId;

    UIWelcome(String text, int imageId) {
        mItemId = sItemId++;

        mText = text;
        mImageId = imageId;
    }

    public long getItemId() {

        return mItemId;
    }

    public String getText() {

        return mText;
    }

    int getImageId() {

        return mImageId;
    }

    public float getMessageHeight(int width) {

        if (mText == null) {
            return 0;
        }

        TextPaint textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(Design.FONT_MEDIUM32.size);
        textPaint.setTypeface(Design.FONT_MEDIUM32.typeface);

        Layout.Alignment alignment = Layout.Alignment.ALIGN_NORMAL;
        StaticLayout staticLayout = new StaticLayout(mText, textPaint, width, alignment, 1, 0, false);
        return staticLayout.getHeight();
    }
}
