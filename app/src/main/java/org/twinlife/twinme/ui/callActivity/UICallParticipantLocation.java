/*
 *  Copyright (c) 2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.callActivity;

import android.graphics.Bitmap;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.Marker;

public class UICallParticipantLocation {

    private final int mParticipantId;
    private String mName;
    private Bitmap mAvatar;

    private double mLatitude;
    private double mLongitude;
    private double mMapLatitudeDelta;
    private double mMapLongitudeDelta;

    @Nullable
    private Marker mMarker;

    public UICallParticipantLocation(int participantId, @Nullable String name, @Nullable Bitmap avatar, double latitude, double longitude, double mapLatitudeDelta, double mapLongitudeDelta) {

        mParticipantId = participantId;
        mName = name;
        mAvatar = avatar;
        mLatitude = latitude;
        mLongitude = longitude;
        mMapLatitudeDelta = mapLatitudeDelta;
        mMapLongitudeDelta = mapLongitudeDelta;
    }

    public int getParticipantId() {

        return mParticipantId;
    }

    public String getName() {

        return mName;
    }

    public Bitmap getAvatar() {

        return mAvatar;
    }

    public double getLatitude() {

        return mLatitude;
    }

    public double getLongitude() {

        return mLongitude;
    }

    public double getMapLatitudeDelta() {

        return mMapLatitudeDelta;
    }

    public double getMapLongitudeDelta() {

        return mMapLongitudeDelta;
    }

    public Marker getMarker() {

        return mMarker;
    }

    public void setMarker(Marker marker) {

        mMarker = marker;
    }

    public void updateName(String name, Bitmap avatar) {

        mName = name;
        mAvatar = avatar;
    }

    public void updateLatitude(double latitude, double longitude) {

        mLatitude = latitude;
        mLongitude = longitude;
    }

    public void updateMapDelta(double latitudeMapDelta, double longitudeMapDelta) {

        mMapLatitudeDelta = latitudeMapDelta;
        mMapLongitudeDelta = longitudeMapDelta;
    }

    public boolean isLocaleLocation() {

        return mParticipantId == -1;
    }
}
