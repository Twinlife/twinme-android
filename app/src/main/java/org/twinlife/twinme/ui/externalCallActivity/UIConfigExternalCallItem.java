/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.externalCallActivity;

import android.content.Context;

import org.twinlife.twinme.ui.externalCallActivity.UIConfigExternalCall.ConfigExternalCallSettings;
import org.twinlife.device.android.twinme.R;

public class UIConfigExternalCallItem {

    private final ConfigExternalCallSettings mConfigExternalCallSettings;

    private String mTitle;

    public UIConfigExternalCallItem(Context context, ConfigExternalCallSettings configExternalCallSettings) {

        mConfigExternalCallSettings = configExternalCallSettings;
        setup(context);
    }

    public String getTitle() {

        return mTitle;
    }

    public ConfigExternalCallSettings getConfigExternalCallSettings() {

        return mConfigExternalCallSettings;
    }

    private void setup(Context context) {

        switch (mConfigExternalCallSettings) {
            case CALL_TYPE:
                mTitle = context.getString(R.string.create_external_call_activity_call_type);
                break;

            case PERMISSIONS:
                mTitle = context.getString(R.string.settings_activity_authorization_title);
                break;

            case EXPIRATION:
                mTitle = context.getString(R.string.create_external_call_activity_link_validity);
                break;

            case DELETE:
                mTitle = context.getString(R.string.create_external_call_activity_delete_link_setting);
                break;

            case NOTIFICATION:
                mTitle = context.getString(R.string.create_external_call_activity_notification_setting);
                break;

            default:
                mTitle = "";
                break;
        }
    }
}