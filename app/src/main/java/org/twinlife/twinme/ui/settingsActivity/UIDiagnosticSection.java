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

public class UIDiagnosticSection extends UIDiagnosticItem {

    public enum DiagnosticSectionType {
        CONNECTION,
        PERMISSIONS,
        NOTIFICATIONS,
        KEYSTORE
    }

    private final DiagnosticSectionType mType;
    private final List<UIDiagnosticSubsection> mItems = new java.util.ArrayList<>();

    public UIDiagnosticSection(Context context, DiagnosticSectionType type) {

        mType = type;
        initInfo(context);
    }

    public List<UIDiagnosticSubsection> getItems() {

        return mItems;
    }

    private void initInfo(Context context) {

        switch (mType) {
            case CONNECTION:
                mTitle = context.getString(R.string.settings_advanced_view_status_connection_title);
                mItems.add(new UIDiagnosticSubsection(context, UIDiagnosticSubsection.DiagnosticSubSectionType.CONNECTION));
                break;

            case PERMISSIONS:
                mTitle = context.getString(R.string.settings_view_authorization_title);
                mItems.add(new UIDiagnosticSubsection(context, UIDiagnosticSubsection.DiagnosticSubSectionType.PERMISSION_NOTIFICATION));
                mItems.add(new UIDiagnosticSubsection(context, UIDiagnosticSubsection.DiagnosticSubSectionType.PERMISSION_MICRO));
                mItems.add(new UIDiagnosticSubsection(context, UIDiagnosticSubsection.DiagnosticSubSectionType.PERMISSION_CAMERA));
                mItems.add(new UIDiagnosticSubsection(context, UIDiagnosticSubsection.DiagnosticSubSectionType.PERMISSION_LOCATION));
                mItems.add(new UIDiagnosticSubsection(context, UIDiagnosticSubsection.DiagnosticSubSectionType.PERMISSION_BACKGROUND));
                break;

            case NOTIFICATIONS:
                mTitle = context.getString(R.string.notifications_view_title);
                mItems.add(new UIDiagnosticSubsection(context, UIDiagnosticSubsection.DiagnosticSubSectionType.PUSH));
                break;

            case KEYSTORE:
                mTitle = context.getString(R.string.diagnostics_view_keystore);
                mItems.add(new UIDiagnosticSubsection(context, UIDiagnosticSubsection.DiagnosticSubSectionType.SECURE_STORAGE));
                break;

            default:
                mTitle = "";
                break;
        }
    }
}

