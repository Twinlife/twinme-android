/*
 *  Copyright (c) 2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.callActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.net.Uri;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;
import androidx.percentlayout.widget.PercentRelativeLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.twinlife.device.android.twinme.BuildConfig;
import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinlife.util.Logger;
import org.twinlife.twinme.calls.CallParticipant;
import org.twinlife.twinme.skin.CircularImageDescriptor;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.AbstractConfirmView;
import org.twinlife.twinme.utils.CircularImageView;
import org.twinlife.twinme.utils.DefaultConfirmView;
import org.twinlife.twinme.utils.RoundedView;

import java.util.ArrayList;
import java.util.List;

public class CallMapView extends PercentRelativeLayout implements OnMapReadyCallback {
    private static final String LOG_TAG = "CallMapView";
    private static final boolean DEBUG = false;

    public interface CallMapListener {
        void onCloseMap();

        void onFullScreenMap(boolean isFullScreen);

        void onStopShareLocation();

        void onStartShareLocation(double mapLatitudeDelta, double mapLongitudeDelta);
    }

    private static final int DESIGN_ACTION_VIEW_COLOR = Color.rgb(60, 60, 60);
    private static final int DESIGN_ACTION_VIEW_RADIUS = 14;

    private static final int DESIGN_SIDE_MARGIN = 34;
    private static final int DESIGN_CLOSE_HEIGHT = 52;
    private static final int DESIGN_CLOSE_TOP_MARGIN = 24;
    private static final int DESIGN_CLOSE_RIGHT_MARGIN = 12;
    private static final int DESIGN_ACTION_RIGHT_MARGIN = 20;
    private static final int DESIGN_ACTION_BOTTOM_MARGIN = 20;
    private static final int DESIGN_SHARE_BOTTOM_MARGIN = 130;
    private static final int DESIGN_ACTION_SIZE = 90;
    private static final int DESIGN_ACTION_IMAGE_SIZE = 45;

    private static final float DESIGN_MARKER_VIEW_WIDTH = 120f;
    private static final float DESIGN_AVATAR_BORDER_INSET = 0f;
    private static final int DESIGN_BORDER_COLOR_WHITE = Color.argb(255, 255, 255, 255);
    private static final float DESIGN_AVATAR_BORDER_THICKNESS = 4f / 120f;

    private CallMapListener mCallMapListener;

    private MapView mMapView;
    private CardView mCardView;
    private View mCloseView;
    private View mShareView;
    private ImageView mShareImageView;
    private View mResetView;
    private View mFullScreenView;
    private ImageView mFullScreenImageView;
    private View mMapTypeView;
    private ImageView mMapTypeImageView;

    @Nullable
    private GoogleMap mGoogleMap;

    private boolean mIsLocationShared = false;
    private boolean mCanShareLocation = false;
    private boolean mCanShareBackgroundLocation = false;
    private boolean mShowBackgroundLocationAlert = false;
    private boolean mCanShareExactLocation = false;
    private boolean mShowExactLocationAlert = false;
    private boolean mDeferredShareLocation = false;
    private boolean mIsFullScreen = false;
    private boolean mMoveMapAutomatically = true;
    private String mName;
    private Bitmap mAvatar;

    private int mInitialHeight = 0;

    private final List<UICallParticipantLocation> mLocations = new ArrayList<>();

    public CallMapView(Context context) {

        super(context);
    }

    public CallMapView(Context context, AttributeSet attrs) {

        super(context, attrs);

        try {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = inflater.inflate(R.layout.call_activity_map_view, (ViewGroup) getParent());
            //noinspection deprecation
            view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            addView(view);
            initViews();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CallMapView(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs, defStyle);
    }

    public void setCallMapListener(CallMapListener callMapListener) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setCallMapListener: " + callMapListener);
        }

        mCallMapListener = callMapListener;
    }

    public void setName(String name) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setName=" + name);
        }

        mName = name;
    }

    public void setAvatar(Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setAvatar=" + avatar);
        }

        mAvatar = avatar;
    }

    public void resumeMap() {
        if (DEBUG) {
            Log.d(LOG_TAG, "resumeMap");
        }

        if (mMapView != null) {
            mMapView.onResume();
        }
    }

    public void startMap() {
        if (DEBUG) {
            Log.d(LOG_TAG, "startMap");
        }

        if (mMapView != null) {
            mMapView.onStart();
        }
    }

    public void stopMap() {
        if (DEBUG) {
            Log.d(LOG_TAG, "stopMap");
        }

        if (mMapView != null) {
            mMapView.onStop();
        }
    }

    public  void pauseMap() {
        if (DEBUG) {
            Log.d(LOG_TAG, "pauseMap");
        }

        if (mMapView != null) {
            mMapView.onPause();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onMapReady: googleMap=" + googleMap);
        }

        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        mGoogleMap.setOnCameraMoveStartedListener(reason -> {
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                mMoveMapAutomatically = false;
            }
        });

        if (mDeferredShareLocation) {
            mDeferredShareLocation = false;

            // GoogleMap API can raise various exceptions.
            // Set the isLocationShared only when everything succeeded.
            try {
                LatLngBounds latLngBounds = mGoogleMap.getProjection().getVisibleRegion().latLngBounds;
                double latitudeDelta = latLngBounds.northeast.latitude - latLngBounds.southwest.latitude;
                double longitudeDelta = latLngBounds.northeast.longitude - latLngBounds.southwest.longitude;
                mCallMapListener.onStartShareLocation(latitudeDelta, longitudeDelta);

                mShareImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.share_location_icon, null));
                mShareView.setContentDescription(getContext().getString(R.string.call_activity_location_stop));
                mIsLocationShared = true;
                Toast.makeText(getContext(), getContext().getString(R.string.call_activity_location_share_message), Toast.LENGTH_SHORT).show();
            } catch (Exception ex) {
                if (Logger.ERROR) {
                    Log.e(LOG_TAG, "Exception in onShareClick", ex);
                }
            }
        }

        updateMap();
    }

    public void setLocationShared(boolean isLocationShared) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setLocationShared: isLocationShared=" + isLocationShared);
        }

        mIsLocationShared = isLocationShared;

        initMapView();
    }

    public void setCanShareLocation(boolean canShareLocation) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setCanShareLocation: canShareLocation=" + canShareLocation);
        }

        mCanShareLocation = canShareLocation;

        if (mCanShareLocation) {
            mShareView.setAlpha(1.0f);
        } else {
            mShareView.setAlpha(0.5f);
        }
    }

    public boolean canShareLocation() {

        return mCanShareLocation;
    }

    public void setCanShareBackgroundLocation(boolean canShareBackgroundLocation, CallActivity callActivity) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setCanShareBackgroundLocation: canShareBackgroundLocation=" + canShareBackgroundLocation);
        }

        mCanShareBackgroundLocation = canShareBackgroundLocation;

        if (mCanShareLocation && !mCanShareBackgroundLocation && !mShowBackgroundLocationAlert) {
            mShowBackgroundLocationAlert = true;

            PercentRelativeLayout percentRelativeLayout = callActivity.findViewById(R.id.call_activity_view);

            mCardView.setVisibility(View.GONE);
            DefaultConfirmView defaultConfirmView = new DefaultConfirmView(callActivity, null);
            PercentRelativeLayout.LayoutParams layoutParams = new PercentRelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            defaultConfirmView.setLayoutParams(layoutParams);
            defaultConfirmView.setForceDarkMode(true);
            defaultConfirmView.setTitle(callActivity.getString(R.string.application_authorization_go_settings));
            defaultConfirmView.setMessage(callActivity.getString(R.string.call_activity_location_background_warning));
            defaultConfirmView.setImage(null);
            defaultConfirmView.setConfirmTitle(callActivity.getString(R.string.application_yes));

            AbstractConfirmView.Observer observer = new AbstractConfirmView.Observer() {
                @Override
                public void onConfirmClick() {
                    defaultConfirmView.animationCloseConfirmView();
                }

                @Override
                public void onCancelClick() {
                    defaultConfirmView.animationCloseConfirmView();
                }

                @Override
                public void onDismissClick() {
                    defaultConfirmView.animationCloseConfirmView();
                }

                @Override
                public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                    percentRelativeLayout.removeView(defaultConfirmView);
                    mCardView.setVisibility(View.VISIBLE);
                    if (fromConfirmAction) {
                        openAppSettings();
                    }
                }
            };
            defaultConfirmView.setObserver(observer);
            percentRelativeLayout.addView(defaultConfirmView);
            defaultConfirmView.show();
        }
    }

    public void setCanShareExactLocation(boolean canShareExactLocation, CallActivity callActivity) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setCanShareExactLocation: canShareExactLocation" + canShareExactLocation);
        }

        mCanShareExactLocation = canShareExactLocation;

        if (mCanShareLocation && !mCanShareExactLocation && !mShowExactLocationAlert && !mShowBackgroundLocationAlert) {
            mShowExactLocationAlert = true;

            PercentRelativeLayout percentRelativeLayout = callActivity.findViewById(R.id.call_activity_view);

            mCardView.setVisibility(View.GONE);
            DefaultConfirmView defaultConfirmView = new DefaultConfirmView(callActivity, null);
            PercentRelativeLayout.LayoutParams layoutParams = new PercentRelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            defaultConfirmView.setLayoutParams(layoutParams);
            defaultConfirmView.setForceDarkMode(true);
            defaultConfirmView.setTitle(callActivity.getString(R.string.application_authorization_go_settings));
            defaultConfirmView.setMessage(callActivity.getString(R.string.call_activity_location_exact_warning));
            defaultConfirmView.setImage(null);
            defaultConfirmView.setConfirmTitle(callActivity.getString(R.string.application_yes));

            AbstractConfirmView.Observer observer = new AbstractConfirmView.Observer() {
                @Override
                public void onConfirmClick() {
                    defaultConfirmView.animationCloseConfirmView();
                }

                @Override
                public void onCancelClick() {
                    defaultConfirmView.animationCloseConfirmView();
                }

                @Override
                public void onDismissClick() {
                    defaultConfirmView.animationCloseConfirmView();
                }

                @Override
                public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                    percentRelativeLayout.removeView(defaultConfirmView);
                    mCardView.setVisibility(View.VISIBLE);
                    if (fromConfirmAction) {
                        openAppSettings();
                    }
                }
            };
            defaultConfirmView.setObserver(observer);
            percentRelativeLayout.addView(defaultConfirmView);
            defaultConfirmView.show();
        }
    }

    public void zoomToParticipant(int participantId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "zoomToParticipant: participantId=" + participantId);
        }

        if (mGoogleMap == null) {
            return;
        }
        for (UICallParticipantLocation uiCallParticipantLocation : mLocations) {
            if (uiCallParticipantLocation.getParticipantId() == participantId) {
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                double latitudeDelta = uiCallParticipantLocation.getMapLatitudeDelta();
                double longitudeDelta = uiCallParticipantLocation.getMapLongitudeDelta();
                builder.include(new LatLng(uiCallParticipantLocation.getLatitude() - (latitudeDelta / 2.0), uiCallParticipantLocation.getLongitude() - (longitudeDelta / 2.0)));
                builder.include(new LatLng(uiCallParticipantLocation.getLatitude() + (latitudeDelta / 2.0), uiCallParticipantLocation.getLongitude() + (longitudeDelta / 2.0)));
                try {
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), Math.max((int) (DESIGN_MARKER_VIEW_WIDTH * Design.WIDTH_RATIO), (int) (DESIGN_MARKER_VIEW_WIDTH * Design.HEIGHT_RATIO))));
                } catch (Exception ex) {
                    if (Logger.ERROR) {
                        Log.e(LOG_TAG, "Exception in zoomToParticipant", ex);
                    }
                }
                return;
            }
        }
    }

    public void updateLocaleLocation(double latitude, double longitude) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateLocaleLocation: latitude=" + latitude + " longitude=" + longitude);
        }

        addLocation(-1, mName, mAvatar, latitude, longitude,0.005, 0.005);
    }

    public void updateLocation(CallParticipant callParticipant, ConversationService.GeolocationDescriptor geolocationDescriptor) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateLocation: callParticipant=" + callParticipant + " geolocationDescriptor=" + geolocationDescriptor);
        }

        addLocation(callParticipant.getParticipantId(), callParticipant.getName(), callParticipant.getAvatar(), geolocationDescriptor.getLatitude(), geolocationDescriptor.getLongitude(), geolocationDescriptor.getMapLatitudeDelta(), geolocationDescriptor.getMapLongitudeDelta());
    }

    public void addLocation(int participantId, String name, Bitmap avatar, double latitude, double longitude, double mapLatitudeDelta, double mapLongitudeDelta) {
        if (DEBUG) {
            Log.d(LOG_TAG, "addLocation: participantId=" + participantId + " name=" + name + " avatar=" + avatar + " latitude=" + latitude + " longitude=" + longitude);
        }

        boolean added = false;

        for (UICallParticipantLocation uiCallParticipantLocation : mLocations) {

            if (uiCallParticipantLocation.getParticipantId() == participantId) {
                added = true;
                uiCallParticipantLocation.updateName(name, avatar);
                uiCallParticipantLocation.updateLatitude(latitude, longitude);
                uiCallParticipantLocation.updateMapDelta(mapLatitudeDelta, mapLongitudeDelta);
                break;
            }
        }

        if (!added) {
            UICallParticipantLocation uiCallParticipantLocation = new UICallParticipantLocation(participantId, name, avatar, latitude, longitude, mapLatitudeDelta, mapLongitudeDelta);
            mLocations.add(uiCallParticipantLocation);
        }

        updateMap();
    }

    public void deleteLocation(int participantId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "addLocation: participantId=" + participantId);
        }

        for (UICallParticipantLocation uiCallParticipantLocation : mLocations) {
            if (uiCallParticipantLocation.getParticipantId() == participantId) {
                if (uiCallParticipantLocation.getMarker() != null) {
                    uiCallParticipantLocation.getMarker().remove();
                }

                mLocations.remove(uiCallParticipantLocation);
                break;
            }
        }
    }

    private void initMapView() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initMapView");
        }

        if (mIsLocationShared) {
            mShareImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.share_location_icon, null));
            mShareView.setContentDescription(getContext().getString(R.string.call_activity_location_stop));
        } else {
            mShareImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.call_location_icon, null));
            mShareView.setContentDescription(getContext().getString(R.string.call_activity_location_share));
        }

        if (mCanShareLocation) {
            mShareView.setAlpha(1.0f);
        } else {
            mShareView.setAlpha(0.5f);
        }

        updateMap();
    }

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        View containerView = findViewById(R.id.call_activity_map_container_view);

        float radius = DESIGN_ACTION_VIEW_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable containerViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        containerViewBackground.getPaint().setColor(DESIGN_ACTION_VIEW_COLOR);
        ViewCompat.setBackground(containerView, containerViewBackground);

        mCardView = findViewById(R.id.call_activity_card_view);
        mCardView.setRadius(radius);

        mCloseView = findViewById(R.id.call_activity_map_close_view);
        mCloseView.setOnClickListener(view -> onCloseClick());

        ViewGroup.LayoutParams layoutParams = mCloseView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_CLOSE_HEIGHT * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_CLOSE_HEIGHT * Design.HEIGHT_RATIO);

        MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mCloseView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_CLOSE_TOP_MARGIN * Design.HEIGHT_RATIO);

        float leftMargin = (Design.DISPLAY_WIDTH - (DESIGN_SIDE_MARGIN * Design.WIDTH_RATIO * 2) - layoutParams.width - (DESIGN_CLOSE_RIGHT_MARGIN * Design.WIDTH_RATIO));
        marginLayoutParams.leftMargin = (int) leftMargin;

        mMapView = findViewById(R.id.call_activity_map_view);
        mMapView.onCreate(null);
        mMapView.getMapAsync(this);

        mShareView = findViewById(R.id.call_activity_map_share_view);
        mShareView.setOnClickListener(v -> onShareClick());

        int actionSize = (int) (DESIGN_ACTION_SIZE * Design.HEIGHT_RATIO);
        layoutParams = mShareView.getLayoutParams();
        layoutParams.width = actionSize;
        layoutParams.height = actionSize;

        float leftActionMargin = (Design.DISPLAY_WIDTH - (DESIGN_SIDE_MARGIN * Design.WIDTH_RATIO * 2) - actionSize - (DESIGN_ACTION_RIGHT_MARGIN * Design.WIDTH_RATIO));

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mShareView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) leftActionMargin;

        RoundedView shareRoundedView = findViewById(R.id.call_activity_map_share_view_background_view);
        shareRoundedView.setColor(Color.WHITE);

        mShareImageView = findViewById(R.id.call_activity_map_share_view_image_view);

        int actionImageSize = (int) (DESIGN_ACTION_IMAGE_SIZE * Design.HEIGHT_RATIO);

        layoutParams = mShareImageView.getLayoutParams();
        layoutParams.width = actionImageSize;
        layoutParams.height = actionImageSize;
        mShareImageView.setLayoutParams(layoutParams);

        mResetView = findViewById(R.id.call_activity_map_reset_view);
        mResetView.setOnClickListener(v -> onResetMapClick());

        layoutParams = mResetView.getLayoutParams();
        layoutParams.width = actionSize;
        layoutParams.height = actionSize;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mResetView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) leftActionMargin;

        RoundedView resetRoundedView = findViewById(R.id.call_activity_map_reset_view_background_view);
        resetRoundedView.setColor(Color.WHITE);

        ImageView resetImageView = findViewById(R.id.call_activity_map_reset_view_image_view);

        layoutParams = resetImageView.getLayoutParams();
        layoutParams.width = actionImageSize;
        layoutParams.height = actionImageSize;
        resetImageView.setLayoutParams(layoutParams);

        mFullScreenView = findViewById(R.id.call_activity_map_fullscreen_view);
        mFullScreenView.setOnClickListener(v -> onFullScreenClick());

        layoutParams = mFullScreenView.getLayoutParams();
        layoutParams.width = actionSize;
        layoutParams.height = actionSize;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mFullScreenView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) leftActionMargin;

        RoundedView fullScreenRoundedView = findViewById(R.id.call_activity_map_fullscreen_view_background_view);
        fullScreenRoundedView.setColor(Color.WHITE);

        mFullScreenImageView = findViewById(R.id.call_activity_map_fullscreen_view_image_view);

        layoutParams = mFullScreenImageView.getLayoutParams();
        layoutParams.width = actionImageSize;
        layoutParams.height = actionImageSize;
        mFullScreenImageView.setLayoutParams(layoutParams);

        mMapTypeView = findViewById(R.id.call_activity_map_type_view);
        mMapTypeView.setOnClickListener(v -> onMapTypeCLick());

        layoutParams = mMapTypeView.getLayoutParams();
        layoutParams.width = actionSize;
        layoutParams.height = actionSize;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mMapTypeView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) leftActionMargin;

        RoundedView mapTypeRoundedView = findViewById(R.id.call_activity_map_type_view_background_view);
        mapTypeRoundedView.setColor(Color.WHITE);

        mMapTypeImageView = findViewById(R.id.call_activity_map_type_view_image_view);
        mMapTypeImageView.setPadding(6, 6, 6, 6);

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressLint("NewApi")
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);

                mInitialHeight = getHeight();

                MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mMapTypeView.getLayoutParams();
                marginLayoutParams.topMargin = mInitialHeight - (int) ((DESIGN_SHARE_BOTTOM_MARGIN + DESIGN_ACTION_BOTTOM_MARGIN + DESIGN_ACTION_SIZE) * Design.HEIGHT_RATIO);
                mMapTypeView.setLayoutParams(marginLayoutParams);

                marginLayoutParams = (ViewGroup.MarginLayoutParams) mShareView.getLayoutParams();
                marginLayoutParams.topMargin = mInitialHeight - (int) ((DESIGN_SHARE_BOTTOM_MARGIN + DESIGN_ACTION_BOTTOM_MARGIN * 2 + DESIGN_ACTION_SIZE * 2) * Design.HEIGHT_RATIO);
                mShareView.setLayoutParams(marginLayoutParams);

                marginLayoutParams = (ViewGroup.MarginLayoutParams) mFullScreenView.getLayoutParams();
                marginLayoutParams.topMargin = mInitialHeight - (int) ((DESIGN_SHARE_BOTTOM_MARGIN + DESIGN_ACTION_BOTTOM_MARGIN * 3 + DESIGN_ACTION_SIZE * 3) * Design.HEIGHT_RATIO);
                mFullScreenView.setLayoutParams(marginLayoutParams);

                marginLayoutParams = (ViewGroup.MarginLayoutParams) mResetView.getLayoutParams();
                marginLayoutParams.topMargin = mInitialHeight - (int) ((DESIGN_SHARE_BOTTOM_MARGIN + DESIGN_ACTION_BOTTOM_MARGIN * 4 + DESIGN_ACTION_SIZE * 4) * Design.HEIGHT_RATIO);
                mResetView.setLayoutParams(marginLayoutParams);
            }
        });
    }

    private void onCloseClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCloseClick");
        }

        mCallMapListener.onCloseMap();
    }

    private void onMapTypeCLick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onMapTypeCLick");
        }

        if (mGoogleMap == null) {
            return;
        }

        if (mGoogleMap.getMapType() == GoogleMap.MAP_TYPE_SATELLITE) {
            mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            mMapTypeImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.satellite_icon, null));
        } else {
            mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            mMapTypeImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.map_icon, null));
        }
    }

    private void onResetMapClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResetMapClick");
        }

        if (mGoogleMap == null || mLocations.isEmpty()) {
            return;
        }

        mMoveMapAutomatically = true;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (UICallParticipantLocation uiCallParticipantLocation : mLocations) {
            LatLng latLng = new LatLng(uiCallParticipantLocation.getLatitude(), uiCallParticipantLocation.getLongitude());
            builder.include(latLng);
        }

        // Catch every exception possibly raised by Google API.
        try {
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), Math.max((int) (DESIGN_MARKER_VIEW_WIDTH * Design.WIDTH_RATIO), (int) (DESIGN_MARKER_VIEW_WIDTH * Design.HEIGHT_RATIO))));
        } catch (Exception ex) {
            if (Logger.ERROR) {
                Log.e(LOG_TAG, "Exception in onResetMapClick", ex);
            }
        }
    }

    private void onFullScreenClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onFullScreenClick");
        }

        if (mIsFullScreen) {
            mIsFullScreen = false;
            mCloseView.setVisibility(VISIBLE);
            mFullScreenImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.fullscreen_icon, null));

            int actionSize = (int) (DESIGN_ACTION_SIZE * Design.HEIGHT_RATIO);
            float leftActionMargin = (Design.DISPLAY_WIDTH -  (DESIGN_SIDE_MARGIN * Design.WIDTH_RATIO * 2) - actionSize - (DESIGN_ACTION_RIGHT_MARGIN * Design.WIDTH_RATIO));

            MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mShareView.getLayoutParams();
            marginLayoutParams.topMargin = mInitialHeight - (int) ((DESIGN_SHARE_BOTTOM_MARGIN + DESIGN_ACTION_BOTTOM_MARGIN + DESIGN_ACTION_SIZE) * Design.HEIGHT_RATIO);
            marginLayoutParams.leftMargin = (int) leftActionMargin;
            mShareView.setLayoutParams(marginLayoutParams);

            marginLayoutParams = (ViewGroup.MarginLayoutParams) mFullScreenView.getLayoutParams();
            marginLayoutParams.topMargin = mInitialHeight - (int) ((DESIGN_SHARE_BOTTOM_MARGIN + DESIGN_ACTION_BOTTOM_MARGIN * 2 + DESIGN_ACTION_SIZE * 2) * Design.HEIGHT_RATIO);
            marginLayoutParams.leftMargin = (int) leftActionMargin;
            mFullScreenView.setLayoutParams(marginLayoutParams);

            marginLayoutParams = (ViewGroup.MarginLayoutParams) mResetView.getLayoutParams();
            marginLayoutParams.topMargin = mInitialHeight - (int) ((DESIGN_SHARE_BOTTOM_MARGIN + DESIGN_ACTION_BOTTOM_MARGIN * 3 + DESIGN_ACTION_SIZE * 3) * Design.HEIGHT_RATIO);
            marginLayoutParams.leftMargin = (int) leftActionMargin;
            mResetView.setLayoutParams(marginLayoutParams);
        } else {
            mIsFullScreen = true;
            mCloseView.setVisibility(GONE);
            mFullScreenImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.minimize_icon, null));

            int actionSize = (int) (DESIGN_ACTION_SIZE * Design.HEIGHT_RATIO);
            float leftActionMargin = (Design.DISPLAY_WIDTH - actionSize - (DESIGN_ACTION_RIGHT_MARGIN * Design.WIDTH_RATIO));

            MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mShareView.getLayoutParams();
            marginLayoutParams.topMargin = getRootView().getHeight() - (int) ((DESIGN_SHARE_BOTTOM_MARGIN + DESIGN_ACTION_BOTTOM_MARGIN + DESIGN_ACTION_SIZE) * Design.HEIGHT_RATIO);
            marginLayoutParams.leftMargin = (int) leftActionMargin;
            mShareView.setLayoutParams(marginLayoutParams);

            marginLayoutParams = (ViewGroup.MarginLayoutParams) mFullScreenView.getLayoutParams();
            marginLayoutParams.topMargin = getRootView().getHeight() - (int) ((DESIGN_SHARE_BOTTOM_MARGIN + DESIGN_ACTION_BOTTOM_MARGIN * 2 + DESIGN_ACTION_SIZE * 2) * Design.HEIGHT_RATIO);
            marginLayoutParams.leftMargin = (int) leftActionMargin;
            mFullScreenView.setLayoutParams(marginLayoutParams);

            marginLayoutParams = (ViewGroup.MarginLayoutParams) mResetView.getLayoutParams();
            marginLayoutParams.topMargin = getRootView().getHeight() - (int) ((DESIGN_SHARE_BOTTOM_MARGIN + DESIGN_ACTION_BOTTOM_MARGIN * 3 + DESIGN_ACTION_SIZE * 3) * Design.HEIGHT_RATIO);
            marginLayoutParams.leftMargin = (int) leftActionMargin;
            mResetView.setLayoutParams(marginLayoutParams);
        }

        mCallMapListener.onFullScreenMap(mIsFullScreen);
    }

    public void onShareClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onShareClick");
        }

        if (!mCanShareLocation) {
            return;
        }

        onResetMapClick();

        if (mIsLocationShared) {
            mIsLocationShared = false;
            mCallMapListener.onStopShareLocation();
            mShareImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.call_location_icon, null));
            mShareView.setContentDescription(getContext().getString(R.string.call_activity_location_share));
        } else if (mGoogleMap != null) {

            // GoogleMap API can raise various exceptions.
            // Set the isLocationShared only when everything succeeded.
            try {
                LatLngBounds latLngBounds = mGoogleMap.getProjection().getVisibleRegion().latLngBounds;
                double latitudeDelta = latLngBounds.northeast.latitude - latLngBounds.southwest.latitude;
                double longitudeDelta = latLngBounds.northeast.longitude - latLngBounds.southwest.longitude;
                mCallMapListener.onStartShareLocation(latitudeDelta, longitudeDelta);

                mShareImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.share_location_icon, null));
                mShareView.setContentDescription(getContext().getString(R.string.call_activity_location_stop));
                mIsLocationShared = true;
            } catch (Exception ex) {
                if (Logger.ERROR) {
                    Log.e(LOG_TAG, "Exception in onShareClick", ex);
                }
            }
        } else {
            mDeferredShareLocation = true;
        }
    }

    private void openAppSettings() {
        if (DEBUG) {
            Log.d(LOG_TAG, "openAppSettings");
        }

        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + BuildConfig.APPLICATION_ID));
        getContext().startActivity(intent);
    }

    private void updateMap() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateMap");
        }

        if (mGoogleMap == null) {
            return;
        }

        boolean addMarker = false;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (UICallParticipantLocation uiCallParticipantLocation : mLocations) {

            LatLng latLng = new LatLng(uiCallParticipantLocation.getLatitude(), uiCallParticipantLocation.getLongitude());

            if (uiCallParticipantLocation.getMarker() != null) {
                Marker marker = uiCallParticipantLocation.getMarker();
                marker.setPosition(latLng);
            } else if (addMarker(uiCallParticipantLocation)) {
                addMarker = true;
                mMoveMapAutomatically = true;
            }

            if (mLocations.size() > 1) {
                builder.include(latLng);
            } else {
                double latitudeDelta = uiCallParticipantLocation.getMapLatitudeDelta();
                double longitudeDelta = uiCallParticipantLocation.getMapLongitudeDelta();
                builder.include(new LatLng(uiCallParticipantLocation.getLatitude() - (latitudeDelta / 2.0), uiCallParticipantLocation.getLongitude() - (longitudeDelta / 2.0)));
                builder.include(new LatLng(uiCallParticipantLocation.getLatitude() + (latitudeDelta / 2.0), uiCallParticipantLocation.getLongitude() + (longitudeDelta / 2.0)));
            }
        }

        if (!mLocations.isEmpty() && (addMarker || mMoveMapAutomatically)) {
            try {
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), Math.max((int) (DESIGN_MARKER_VIEW_WIDTH * Design.WIDTH_RATIO), (int) (DESIGN_MARKER_VIEW_WIDTH * Design.HEIGHT_RATIO))));
            } catch (Exception ex) {
                if (Logger.ERROR) {
                    Log.e(LOG_TAG, "Exception in updateMap", ex);
                }
            }
        }
    }

    private boolean addMarker(UICallParticipantLocation callParticipantLocation) {
        if (DEBUG) {
            Log.d(LOG_TAG, "addMarker");
        }

        if (mGoogleMap == null) {
            return false;
        }
        try {
            LatLng latLng = new LatLng(callParticipantLocation.getLatitude(), callParticipantLocation.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("");
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(this.getMarkerBitmapFromView(callParticipantLocation)));
            markerOptions.draggable(false);
            markerOptions.visible(true);

            Marker marker = mGoogleMap.addMarker(markerOptions);
            if (marker != null) {
                callParticipantLocation.setMarker(marker);
            }

            return true;
        } catch (Throwable ex) {
            // An exception could be raised if the wrong Google play service is used.
            Log.w(LOG_TAG, "Error: " + ex.getMessage());

            return false;
        }
    }

    private Bitmap getMarkerBitmapFromView(UICallParticipantLocation callParticipantLocation) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getMarkerBitmapFromView");
        }

        View customMarkerView = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.location_marker, null);
        customMarkerView.measure((int) (DESIGN_MARKER_VIEW_WIDTH * Design.WIDTH_RATIO), (int) (DESIGN_MARKER_VIEW_WIDTH * Design.HEIGHT_RATIO));
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());

        CircularImageView markerImageView = customMarkerView.findViewById(R.id.location_marker_avatar_view);

        Bitmap avatar = callParticipantLocation.getAvatar();

        markerImageView.setImage(getContext(), Design.LIGHT_CIRCULAR_SHADOW_DESCRIPTOR,
                new CircularImageDescriptor(avatar, 0.5f, 0.5f, Design.LIGHT_CIRCULAR_SHADOW_DESCRIPTOR.imageWithShadowRadius, DESIGN_BORDER_COLOR_WHITE, DESIGN_AVATAR_BORDER_THICKNESS, DESIGN_AVATAR_BORDER_INSET));
        customMarkerView.buildDrawingCache();

        Bitmap returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = customMarkerView.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        customMarkerView.draw(canvas);
        return returnedBitmap;
    }
}