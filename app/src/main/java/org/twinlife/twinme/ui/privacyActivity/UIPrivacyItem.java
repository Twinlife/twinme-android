/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.privacyActivity;

public class UIPrivacyItem {

    public enum PrivacyItemType {
        LOCKSCREEN_ENABLE,
        LOCKSCREEN_TIMEOUT,
        ALLOW_SCREENSHOT,
        SHARE_INVITATION_MODE,
        SECTION,
        INFO
    }

    private final PrivacyItemType mType;
    private final String mText;

    public UIPrivacyItem(PrivacyItemType type, String text) {
        mType = type;
        mText = text;
    }

    public String getText() {

        return mText;
    }

    public PrivacyItemType getType() {

        return mType;
    }
}
