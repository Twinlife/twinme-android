/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.settingsActivity;

import android.content.Context;

import org.twinlife.device.android.twinme.R;

import java.util.List;

public class UIHelpSection extends UIHelpItem {

    public enum HelpSectionType {
        GENERAL,
        STANDARD_SERVICES,
        PREMIUM_SERVICES,
        ADVANCED_SERVICES
    }


    private final HelpSectionType mHelpSectionType;
    private final List<UIHelpSubSection> mItems = new java.util.ArrayList<>();

    public UIHelpSection(Context context, HelpSectionType helpSectionType) {

        mHelpSectionType = helpSectionType;
        initInfo(context);
    }

    public List<UIHelpSubSection> getItems() {

        return mItems;
    }

    private void initInfo(Context context) {

        switch (mHelpSectionType) {
            case GENERAL:
                mTitle = "";
                mItems.add(new UIHelpSubSection(context, UIHelpSubSection.HelpSubSectionType.FAQ));
                mItems.add(new UIHelpSubSection(context, UIHelpSubSection.HelpSubSectionType.BLOG));
                mItems.add(new UIHelpSubSection(context, UIHelpSubSection.HelpSubSectionType.FEEDBACK));
                break;

            case STANDARD_SERVICES:
                mTitle = context.getString(R.string.help_view_standard_services);
                mItems.add(new UIHelpSubSection(context, UIHelpSubSection.HelpSubSectionType.WELCOME));
                mItems.add(new UIHelpSubSection(context, UIHelpSubSection.HelpSubSectionType.PROFILE));
                mItems.add(new UIHelpSubSection(context, UIHelpSubSection.HelpSubSectionType.CERTIFIED_RELATION));
                mItems.add(new UIHelpSubSection(context, UIHelpSubSection.HelpSubSectionType.QUALITY_OF_SERVICES));
                mItems.add(new UIHelpSubSection(context, UIHelpSubSection.HelpSubSectionType.ACCOUNT_TRANSFER));
                break;

            case PREMIUM_SERVICES:
                mTitle = context.getString(R.string.about_view_premium_services);
                mItems.add(new UIHelpSubSection(context, UIHelpSubSection.HelpSubSectionType.ADDITIONAL_FUNCTIONS));
                mItems.add(new UIHelpSubSection(context, UIHelpSubSection.HelpSubSectionType.SPACES));
                mItems.add(new UIHelpSubSection(context, UIHelpSubSection.HelpSubSectionType.CLICK_TO_CALL));
                break;

            case ADVANCED_SERVICES:
                mTitle = context.getString(R.string.help_view_advanced_functions);
                mItems.add(new UIHelpSubSection(context, UIHelpSubSection.HelpSubSectionType.BACKUP));
                mItems.add(new UIHelpSubSection(context, UIHelpSubSection.HelpSubSectionType.PROXY));
                break;

            default:
                mTitle = "";
                break;
        }
    }
}

