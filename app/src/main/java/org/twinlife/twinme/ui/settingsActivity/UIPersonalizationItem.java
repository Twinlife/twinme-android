/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.settingsActivity;

import androidx.annotation.Nullable;

public class UIPersonalizationItem {

    public enum PersonalizationType {
        HEADER,
        TAB_SECTION,
        TAB,
        TAB_INFO,
        DISPLAY_SECTION,
        DISPLAY_MODE,
        APPEARANCE_SECTION,
        THEME,
        CONVERSATION_APPEARANCE,
        FONT_SECTION,
        FONT_SYSTEM,
        FONT_SMALL,
        FONT_MEDIUM,
        FONT_LARGE,
        SOUND_VIBRATION_SECTION,
        HAPTIC_FEEDBACK,
        SOUND_EFFECTS
    }

    private final PersonalizationType mType;

    @Nullable
    private final String mTitle;
    @Nullable
    private final String mSubtitle;

    public UIPersonalizationItem(PersonalizationType type, @Nullable String title, @Nullable String subtitle) {

        mType = type;
        mTitle = title;
        mSubtitle = subtitle;
    }

    @Nullable
    public String getTitle() {

        return mTitle;
    }

    @Nullable
    public String getSubtitle() {

        return mSubtitle;
    }

    public PersonalizationType getType() {

        return mType;
    }
}
