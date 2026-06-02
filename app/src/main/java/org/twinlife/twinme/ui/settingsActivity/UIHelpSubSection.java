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
import org.twinlife.twinme.utils.Utils;

public class UIHelpSubSection extends UIHelpItem {

    public enum HelpSubSectionType {
        GETTING_STARTED,
        FAQ,
        BLOG,
        FEEDBACK,
        WELCOME,
        PROFILE,
        CERTIFIED_RELATION,
        QUALITY_OF_SERVICES,
        ACCOUNT_TRANSFER,
        ADDITIONAL_FUNCTIONS,
        SPACES,
        CLICK_TO_CALL,
        BACKUP,
        PROXY
    }

    private int mIcon;
    private final HelpSubSectionType mHelpSubSectionType;

    public UIHelpSubSection(Context context, HelpSubSectionType helpItemType) {

        mHelpSubSectionType = helpItemType;
        initInfo(context);
    }

    public int getIcon() {

        return mIcon;
    }

    public HelpSubSectionType getHelpSubSectionType() {

        return mHelpSubSectionType;
    }

    private void initInfo(Context context) {

        switch (mHelpSubSectionType) {
            case GETTING_STARTED:
                mTitle = context.getString(R.string.navigation_view_getting_started);
                mIcon = R.drawable.getting_started_icon;
                break;

            case FAQ:
                mTitle = context.getString(R.string.navigation_view_faq);
                mIcon = R.drawable.faq_icon;
                break;

            case BLOG:
                mTitle = context.getString(R.string.navigation_view_blog);
                mIcon = R.drawable.blog_icon;
                break;

            case FEEDBACK:
                mTitle = context.getString(R.string.navigation_view_feedback);
                mIcon = R.drawable.feedback_icon;
                break;

            case WELCOME:
                mTitle = Utils.capitalizeString(context.getString(R.string.settings_view_welcome_screen_category_title));
                mIcon = R.drawable.placeholder_logo;
                break;

            case PROFILE:
                mTitle = context.getString(R.string.application_profile);
                mIcon = R.drawable.profile_icon;
                break;

            case CERTIFIED_RELATION:
                mTitle = context.getString(R.string.authentified_relation_view_title);
                mIcon = R.drawable.certified_icon;
                break;

            case QUALITY_OF_SERVICES:
                mTitle = context.getString(R.string.about_view_quality_of_service);
                mIcon = R.drawable.quality_of_services_icon;
                break;

            case ACCOUNT_TRANSFER:
                mTitle = context.getString(R.string.account_view_transfer_between_devices);
                mIcon = R.drawable.migration_icon;
                break;

            case ADDITIONAL_FUNCTIONS:
                mTitle = context.getString(R.string.help_view_additional_features);
                mIcon = R.drawable.premium_services_icon;
                break;

            case SPACES:
                mTitle = context.getString(R.string.premium_services_view_space_title);
                mIcon = R.drawable.space_icon;
                break;

            case CLICK_TO_CALL:
                mTitle = context.getString(R.string.premium_services_view_click_to_call_title);
                mIcon = R.drawable.add_external_call;
                break;

            case BACKUP:
                mTitle = context.getString(R.string.account_view_backup_restore);
                mIcon = R.drawable.backup_restore_icon;
                break;

            case PROXY:
                mTitle = context.getString(R.string.proxy_view_title);
                mIcon = R.drawable.proxy_icon;
                break;

            default:
                mTitle = "";
                mIcon = 1;
                break;
        }
    }
}