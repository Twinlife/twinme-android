/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.settingsActivity;

public class UIAdvancedSettingItem {

    public enum AdvancedSettingItemType {
        CONNEXION_SECTION,
        CONNEXION_INFO,
        CONNEXION_STATUS,
        PROXY_SECTION,
        PROXY_INFO,
        PROXY_ENABLE,
        PROXY_ADD,
        PROXY,
        TELECOM_SECTION,
        TELECOM_INFO,
        TELECOM_ENABLE,
        SECURITY_SECTION,
        SECURITY_INFO,
        SECURITY_LEVEL,
        CONVERSATION_SECTION,
        CONVERSATION_INFO,
        LINK_PREVIEW,
        DEBUG_SECTION,
        DEVELOPER_SETTINGS
    }

    private final AdvancedSettingItemType mType;
    private final String mText;

    private final int mProxyPosition;

    public UIAdvancedSettingItem(AdvancedSettingItemType type, String text, int proxyPosition) {

        mType = type;
        mText = text;
        mProxyPosition = proxyPosition;
    }

    public String getText() {

        return mText;
    }

    public AdvancedSettingItemType getType() {

        return mType;
    }

    public int getProxyPosition() {

        return mProxyPosition;
    }
}
