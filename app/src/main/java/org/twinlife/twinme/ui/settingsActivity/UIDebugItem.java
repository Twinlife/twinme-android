/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.settingsActivity;

import org.twinlife.twinme.ui.TwinmeApplication;

public class UIDebugItem {

    public enum DebugItemType {
        SECTION,
        ONBOARDING,
        RESET
    }

    private final DebugItemType mType;
    private final String mText;

    private final TwinmeApplication.OnboardingType mOnboardingType;

    public UIDebugItem(DebugItemType debugItemType, TwinmeApplication.OnboardingType onboardingType, String text) {
        mType = debugItemType;
        mText = text;
        mOnboardingType = onboardingType;
    }

    public String getText() {

        return mText;
    }

    public DebugItemType getType() {

        return mType;
    }

    public TwinmeApplication.OnboardingType getOnboardingType() {

        return mOnboardingType;
    }
}
