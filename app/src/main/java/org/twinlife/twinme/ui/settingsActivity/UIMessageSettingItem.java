/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.settingsActivity;

public class UIMessageSettingItem {

    public enum MessageSettingItemType {
        HEADER,
        SECTION,
        INFO,
        NOTIFICATION_DISPLAY_SENDER,
        NOTIFICATION_DISPLAY_CONTENT,
        NOTIFICATION_DISPLAY_LIKE,
        ALLOW_COPY_TEXT,
        ALLOW_COPY_FILE,
        DISPLAY_CALLS,
        EPHEMERAL_ENABLE,
        EPHEMERAL_DURATION,
        CONTENT_MEDIA
    }

    private final MessageSettingItemType mType;
    private final String mText;
    private final boolean mBoolValue;

    public UIMessageSettingItem(MessageSettingItemType type, String text, boolean boolValue) {

        mType = type;
        mText = text;
        mBoolValue = boolValue;
    }

    public String getText() {

        return mText;
    }

    public MessageSettingItemType getType() {

        return mType;
    }

    public boolean hideSeparator() {

        return mBoolValue;
    }
}
