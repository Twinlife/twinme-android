/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.settingsActivity;

import android.content.Context;

import androidx.annotation.Nullable;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.utils.Utils;

public class UIDiagnosticSubsection extends UIDiagnosticItem {

    public enum DiagnosticSubSectionType {
        CONNECTION,
        PERMISSION_MICRO,
        PERMISSION_CAMERA,
        PERMISSION_NOTIFICATION,
        PERMISSION_LOCATION,
        PERMISSION_BACKGROUND,
        PUSH,
        SECURE_STORAGE
    }

    public enum DiagnosticState {
        OK,
        KO,
        UNKNOWN
    }

    private int mIcon;
    private final DiagnosticSubSectionType mType;
    private DiagnosticState mState;

    @Nullable
    private String mSubtitle;

    public UIDiagnosticSubsection(Context context, DiagnosticSubSectionType type) {

        mType = type;
        mState = DiagnosticState.UNKNOWN;
        initInfo(context);
    }

    public int getIcon() {

        return mIcon;
    }

    public DiagnosticSubSectionType getType() {

        return mType;
    }

    public DiagnosticState getState() {

        return mState;
    }

    public void setState(DiagnosticState state) {

        mState = state;
    }
    @Nullable
    public String getSubtitle() {

        return mSubtitle;
    }

    public void setSubtitle(String subtitle) {

        mSubtitle = subtitle;
    }

    private void initInfo(Context context) {

        switch (mType) {
            case CONNECTION:
                mTitle = context.getString(R.string.application_connected);
                mIcon = R.drawable.connected_icon;
                break;

            case PERMISSION_MICRO:
                mTitle = context.getString(R.string.call_view_tag_microphone);
                mIcon = R.drawable.toolbar_microphone_grey;
                break;

            case PERMISSION_CAMERA:
                mTitle = context.getString(R.string.call_view_tag_camera);
                mIcon = R.drawable.toolbar_camera_grey;
                break;

            case PERMISSION_NOTIFICATION:
                mTitle = context.getString(R.string.notifications_view_title);
                mIcon = R.drawable.notifications_icon;
                break;

            case PERMISSION_LOCATION:
                mTitle = context.getString(R.string.application_location);
                mIcon = R.drawable.call_location_icon;
                break;

            case PERMISSION_BACKGROUND:
                mTitle = Utils.capitalizeString(context.getString(R.string.diagnostics_view_background_execution));
                mIcon = R.drawable.background_data_icon;
                break;

            case PUSH:
                mTitle = context.getString(R.string.diagnostics_view_push_token);
                mIcon = R.drawable.notifications_icon;
                break;

            case SECURE_STORAGE:
                mTitle = context.getString(R.string.diagnostics_view_secure_storage);
                mIcon = R.drawable.send_option_copy_icon;
                break;

            default:
                mTitle = "";
                mIcon = -1;
                break;
        }
    }
}