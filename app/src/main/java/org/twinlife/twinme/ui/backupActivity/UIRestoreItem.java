/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.backupActivity;

import androidx.annotation.Nullable;

public class UIRestoreItem {

    public enum UIRestoreItemType {
        HEADER(1),
        WORDS(2),
        STATE(3),
        SECTION(4),
        CONTENT(5),
        INFO(6),
        FOOTER(7),
        UNKNOWN(8);

        private final int mValue;

        UIRestoreItemType(int value) {

            mValue = value;
        }

        public int getValue() {

            return mValue;
        }

        public static UIRestoreItemType fromType(int value) {

            for (UIRestoreItemType itemType : values()) {
                if (itemType.getValue() == value) {
                    return itemType;
                }
            }

            return UNKNOWN;
        }
    }

    private final UIRestoreItemType mType;
    @Nullable
    private final String mText;

    private final int mIcon;

    private final int mValue;

    private final int mColor;

    public UIRestoreItem(UIRestoreItemType type, @Nullable String text, int icon, int value, int color) {

        mType = type;
        mText = text;
        mIcon = icon;
        mValue = value;
        mColor = color;
    }

    public UIRestoreItemType getType() {

        return mType;
    }

    public String getText() {

        return mText;
    }

    public int getIcon() {

        return mIcon;
    }

    public int getValue() {

        return mValue;
    }

    public int getColor() {

        return mColor;
    }
}
