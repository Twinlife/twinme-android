/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.contacts;

public class UIConversationNotificationsItem {

    public enum ConversationNotificationsItemType {
        NOTIFICATIONS_SECTION,
        DISPLAY_NOTIFICATIONS_REACTIONS,
        SILENT_MODE,
        SILENT_MODE_DURATION,
        PRIVACY_SECTION,
        DISCREET_MODE,
        DISCREET_MODE_INFO
    }

    private final ConversationNotificationsItemType mType;
    private final String mText;

    public UIConversationNotificationsItem(ConversationNotificationsItemType type, String text) {

        mType = type;
        mText = text;
    }

    public String geText() {

        return mText;
    }

    public ConversationNotificationsItemType getType() {

        return mType;
    }
}
