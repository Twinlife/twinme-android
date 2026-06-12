/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.settingsActivity;

public class UIAboutItem {

    public enum AboutItemType {
        HEADER,
        CURRENT_VERSION,
        UPDATE_AVAILABLE,
        LEGAL_SECTION,
        TERMS_OF_USE,
        PRIVACY_POLICY,
        OPEN_SOURCE_SECTION,
        SOURCE_CODE,
        SOURCE_CODE_INFO,
        OPEN_SOURCE_LICENSES,
        COPYRIGHT
    }

    private final AboutItemType mType;
    private final String mText;

    public UIAboutItem(AboutItemType type, String text) {
        mType = type;
        mText = text;
    }

    public String geText() {

        return mText;
    }

    public AboutItemType getType() {

        return mType;
    }
}
