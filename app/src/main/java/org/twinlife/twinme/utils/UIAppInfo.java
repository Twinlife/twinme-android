/*
 *  Copyright (c) 2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.utils;

import android.content.Context;
import android.graphics.Color;

import androidx.annotation.Nullable;

import org.twinlife.device.android.twinme.R;

public class UIAppInfo {

    private final AppStateInfo.InfoFloatingViewType mInfoFloatingViewType;
    private String mTitle;
    private String mMessage;
    private int mImageId;
    private int mColor;

    @Nullable
    private String mProxy;

    public UIAppInfo(Context context, AppStateInfo.InfoFloatingViewType infoFloatingViewType) {

        mInfoFloatingViewType = infoFloatingViewType;
        initAppInfo(context);
    }

    public AppStateInfo.InfoFloatingViewType getInfoFloatingViewType() {

        return mInfoFloatingViewType;
    }

    public String getTitle() {

        return mTitle;
    }

    public String getMessage() {

        return mMessage;
    }

    public int getImageId() {

        return mImageId;
    }

    public int getColor() {

        return mColor;
    }

    public void setProxy(String proxy) {

        mProxy = proxy;
    }

    public String getProxy() {

        return mProxy;
    }

    private void initAppInfo(Context context) {

        switch (mInfoFloatingViewType) {
            case CONNECTED:
                mTitle = context.getString(R.string.application_connected);
                mMessage ="";
                mImageId = R.drawable.connected_icon;
                mColor = Color.TRANSPARENT;
                break;

            case CONNECTION_IN_PROGRESS:
                mTitle = context.getString(R.string.application_not_connected);
                mMessage = "";
                mImageId = R.drawable.no_network;
                mColor = Color.rgb(192, 124, 65);
                break;

            case NO_SERVICES:
                mTitle = context.getString(R.string.application_connection_status_no_services);
                mMessage = context.getString(R.string.application_connection_status_no_services_message);
                mImageId = R.drawable.no_access_services;
                mColor = Color.TRANSPARENT;
                break;

            case OFFLINE:
                mTitle = context.getString(R.string.application_connection_status_no_network);
                mMessage = context.getString(R.string.application_connection_status_no_network_message);
                mImageId = R.drawable.no_network;
                mColor = Color.TRANSPARENT;
                break;
        }
    }
}
