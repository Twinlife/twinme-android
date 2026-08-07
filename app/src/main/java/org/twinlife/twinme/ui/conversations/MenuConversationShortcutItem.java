/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.conversations;

public class MenuConversationShortcutItem {

    public enum ConversationShortcutItemType {
        HEADER,
        NOTIFICATIONS_REACTIONS,
        SILENT_MODE,
        SILENT_MODE_DURATION,
        RESET_CONVERSATION
    }

    private final ConversationShortcutItemType mType;

    private final String mText;

    public MenuConversationShortcutItem(ConversationShortcutItemType type, String text) {

        mType = type;
        mText = text;
    }

    public String geText() {

        return mText;
    }

    public ConversationShortcutItemType getType() {

        return mType;
    }
}