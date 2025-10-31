/*
 *  Copyright (c) 2019 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.core.content.res.ResourcesCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.BaseService;
import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinlife.ConversationService.GeolocationDescriptor;
import org.twinlife.twinme.skin.CircularImageDescriptor;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.CircularImageView;
import org.twinlife.twinme.utils.RoundedView;

public class LocationActivity extends AbstractTwinmeActivity implements OnMapReadyCallback {
    private static final String LOG_TAG = "LocationActivity";
    private static final boolean DEBUG = false;

    private static final float DESIGN_MARKER_VIEW_WIDTH = 120f;
    private static final float DESIGN_AVATAR_BORDER_INSET = 0f;
    private static final int DESIGN_BORDER_COLOR_WHITE = Color.argb(255, 255, 255, 255);
    private static final float DESIGN_AVATAR_BORDER_THICKNESS = 4f / 120f;
    private static final int DESIGN_ACTION_TOP_MARGIN = 60;
    private static final int DESIGN_ACTION_BOTTOM_MARGIN = 110;
    private static final int DESIGN_ACTION_HORIZONTAL_MARGIN = 34;
    private static final int DESIGN_ACTION_SIZE = 90;
    private static final int DESIGN_ACTION_IMAGE_SIZE = 30;

    private MapView mMapView;
    private ImageView mMapTypeImageView;

    private GoogleMap mGoogleMap;
    private Bitmap mAvatar;
    private String mContactName;

    private GeolocationDescriptor mGeolocationDescriptor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String value = intent.getStringExtra(Intents.INTENT_PROFILE_NAME);
        if (value != null) {
            mContactName = value;
        }

        byte[] byteArray = getIntent().getByteArrayExtra(Intents.INTENT_AVATAR_BYTES);
        if (byteArray != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            if (bitmap != null) {
                mAvatar = bitmap;
            }
        }

        ConversationService.DescriptorId id = ConversationService.DescriptorId.fromString(intent.getStringExtra(Intents.INTENT_DESCRIPTOR_ID));

        if (id != null) {
            final ConversationService service = getTwinmeContext().getConversationService();
            service.getGeolocation(id, (BaseService.ErrorCode errorCode, GeolocationDescriptor geolocationDescriptor) ->
                    new Handler(getMainLooper()).post(() -> {
                        if (errorCode != BaseService.ErrorCode.SUCCESS || geolocationDescriptor == null) {
                            if (DEBUG) {
                                Log.w(LOG_TAG, "Error getting geolocation descriptor with id " + id + ", error: " + errorCode);
                            }
                        }
                        mGeolocationDescriptor = geolocationDescriptor;
                        initViews();
                    }));
        } else {
            initViews();
        }
    }

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        setContentView(R.layout.location_activity);

        applyInsets(R.id.location_activity_layout, -1, -1, Design.WHITE_COLOR, true);

        mMapView = findViewById(R.id.location_activity_map_view);
        mMapView.onCreate(null);
        mMapView.getMapAsync(this);

        View closeView = findViewById(R.id.location_activity_close_view);
        closeView.setOnClickListener(v -> finish());

        int actionSize = (int) (DESIGN_ACTION_SIZE * Design.HEIGHT_RATIO);
        int actionImageSize = (int) (DESIGN_ACTION_IMAGE_SIZE * Design.HEIGHT_RATIO);
        int actionTopMargin = (int) (DESIGN_ACTION_TOP_MARGIN * Design.HEIGHT_RATIO);
        int actionBottomMargin = (int) (DESIGN_ACTION_BOTTOM_MARGIN * Design.HEIGHT_RATIO);
        int actionRightMargin = (int) (DESIGN_ACTION_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);

        ViewGroup.LayoutParams layoutParams = closeView.getLayoutParams();
        layoutParams.width = actionSize;
        layoutParams.height = actionSize;

        ImageView closeImageView = findViewById(R.id.location_activity_close_view_image_view);
        closeImageView.setColorFilter(Color.BLACK);

        layoutParams = closeImageView.getLayoutParams();
        layoutParams.width = actionImageSize;
        layoutParams.height = actionImageSize;

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) closeView.getLayoutParams();
        marginLayoutParams.topMargin = actionTopMargin;
        marginLayoutParams.rightMargin = actionRightMargin;

        RoundedView closeRoundedView = findViewById(R.id.location_activity_close_view_background_view);
        closeRoundedView.setColor(Color.WHITE);

        View mapTypeView = findViewById(R.id.location_activity_map_type_view);
        mapTypeView.setOnClickListener(v -> onMapTypeCLick());

        layoutParams = mapTypeView.getLayoutParams();
        layoutParams.width = actionSize;
        layoutParams.height = actionSize;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mapTypeView.getLayoutParams();
        marginLayoutParams.bottomMargin = actionBottomMargin;
        marginLayoutParams.rightMargin = actionRightMargin;

        RoundedView mapTypeRoundedView = findViewById(R.id.location_activity_map_type_view_background_view);
        mapTypeRoundedView.setColor(Color.WHITE);

        mMapTypeImageView = findViewById(R.id.location_activity_map_type_view_image_view);
        mMapTypeImageView.setPadding(6, 6, 6, 6);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        super.onResume();

        if (mMapView != null) {
            mMapView.onResume();
        }
    }

    @Override
    protected void onStart() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onStart");
        }

        super.onStart();

        if (mMapView != null) {
            mMapView.onStart();
        }
    }

    @Override
    protected void onStop() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onStop");
        }

        super.onStop();

        if (mMapView != null) {
            mMapView.onStop();
        }
    }

    @Override
    protected void onPause() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPause");
        }

        super.onPause();

        if (mMapView != null) {
            mMapView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        super.onDestroy();

        if (mMapView != null) {
            mMapView.onDestroy();
        }
    }

    @Override
    public void onLowMemory() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onLowMemory");
        }

        super.onLowMemory();

        mMapView.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onMapReady: googleMap=" + googleMap);
        }

        mGoogleMap = googleMap;

        addLocation();
    }

    @Override
    public void finish() {
        if (DEBUG) {
            Log.d(LOG_TAG, "finish");
        }

        super.finish();
    }

    private void addLocation() {
        if (DEBUG) {
            Log.d(LOG_TAG, "addLocation");
        }

        if (mGeolocationDescriptor != null) {
            LatLng userLocation = new LatLng(mGeolocationDescriptor.getLatitude(), mGeolocationDescriptor.getLongitude());

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(userLocation);
            markerOptions.title(mContactName);
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(this.getMarkerBitmapFromView()));
            markerOptions.draggable(false);
            markerOptions.visible(true);

            mGoogleMap.addMarker(markerOptions);

            double latitudeDelta = mGeolocationDescriptor.getMapLatitudeDelta();
            double longitudeDelta = mGeolocationDescriptor.getMapLongitudeDelta();
            LatLngBounds.Builder displayBuilder = new LatLngBounds.Builder();
            displayBuilder.include(new LatLng(userLocation.latitude - (latitudeDelta / 2.0), userLocation.longitude - (longitudeDelta / 2.0)));
            displayBuilder.include(new LatLng(userLocation.latitude + (latitudeDelta / 2.0), userLocation.longitude + (longitudeDelta / 2.0)));

            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(displayBuilder.build(), Design.DISPLAY_WIDTH, Design.DISPLAY_HEIGHT, 0));
        }
    }


    private Bitmap getMarkerBitmapFromView() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getMarkerBitmapFromView");
        }

        View customMarkerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.location_marker, null);
        customMarkerView.measure((int) (DESIGN_MARKER_VIEW_WIDTH * Design.WIDTH_RATIO), (int) (DESIGN_MARKER_VIEW_WIDTH * Design.HEIGHT_RATIO));
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());

        CircularImageView markerImageView = customMarkerView.findViewById(R.id.location_marker_avatar_view);
        markerImageView.setImage(this, Design.LIGHT_CIRCULAR_SHADOW_DESCRIPTOR,
                new CircularImageDescriptor(mAvatar, 0.5f, 0.5f, Design.LIGHT_CIRCULAR_SHADOW_DESCRIPTOR.imageWithShadowRadius, DESIGN_BORDER_COLOR_WHITE, DESIGN_AVATAR_BORDER_THICKNESS, DESIGN_AVATAR_BORDER_INSET));
        customMarkerView.buildDrawingCache();

        Bitmap bitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = customMarkerView.getBackground();
        if (drawable != null) {
            drawable.draw(canvas);
        }
        customMarkerView.draw(canvas);
        return bitmap;
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
}
