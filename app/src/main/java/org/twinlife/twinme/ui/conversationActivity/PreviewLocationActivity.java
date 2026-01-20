/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Romain Kolb (romain.kolb@skyrock.com)
 */

package org.twinlife.twinme.ui.conversationActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ImageId;
import org.twinlife.twinlife.ImageService;
import org.twinlife.twinme.TwinmeContext;
import org.twinlife.twinme.skin.CircularImageDescriptor;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.utils.AbstractBottomSheetView;
import org.twinlife.twinme.utils.CircularImageView;
import org.twinlife.twinme.utils.DefaultConfirmView;
import org.twinlife.twinme.utils.RoundedView;

import java.util.Arrays;
import java.util.List;

public class PreviewLocationActivity extends AbstractPreviewActivity implements OnMapReadyCallback {
    private static final String LOG_TAG = "PreviewLocationActivity";
    private static final boolean DEBUG = false;

    private static final float DESIGN_MAP_TOP_MARGIN = 120f;
    private static final float DESIGN_MAP_BOTTOM_MARGIN = 60f;
    private static final float DESIGN_MAP_SIDE_MARGIN = 34f;
    private static final int DESIGN_ACTION_VIEW_RADIUS = 14;
    private static final float DESIGN_MARKER_VIEW_WIDTH = 120f;
    private static final float DESIGN_AVATAR_BORDER_INSET = 0f;
    private static final int DESIGN_BORDER_COLOR_WHITE = Color.argb(255, 255, 255, 255);
    private static final float DESIGN_AVATAR_BORDER_THICKNESS = 4f / 120f;
    private static final int DESIGN_ACTION_VERTICAL_MARGIN = 110;
    private static final int DESIGN_ACTION_HORIZONTAL_MARGIN = 20;
    private static final int DESIGN_ACTION_SIZE = 90;

    @Nullable
    private GoogleMap mGoogleMap;
    private Location mUserLocation;

    private CardView mCardView;
    private ImageView mMapTypeImageView;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private ImageId mImageId;

    private TwinmeContextObserver mObserver;
    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mAllowEphemeralMessage = intent.getBooleanExtra(Intents.INTENT_ALLOW_EPHEMERAL, false);
        mExpireTimeout = intent.getLongExtra(Intents.INTENT_EXPIRE_TIMEOUT, 0);
        mShareLocation = true;

        if (intent.hasExtra(Intents.INTENT_CONTACT_NAME)) {
            mContactName = intent.getStringExtra(Intents.INTENT_CONTACT_NAME);
        }

        if (intent.hasExtra(Intents.INTENT_AVATAR_ID)) {
            long avatarId = intent.getLongExtra(Intents.INTENT_AVATAR_ID, 0);
            mImageId = new ImageId(avatarId);
        }

        if (intent.hasExtra(Intents.INTENT_TEXT_MESSAGE)) {
            mInitMessage = intent.getStringExtra(Intents.INTENT_TEXT_MESSAGE);
        }

        mIsCertified = intent.getBooleanExtra(Intents.INTENT_IS_CERTIFIED, false);

        initViews();

        mObserver = new TwinmeContextObserver();
        getTwinmeContext().setObserver(mObserver);
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        super.onDestroy();

        if (mObserver != null) {
            getTwinmeContext().removeObserver(mObserver);
        }
    }

    @Override
    protected void onPause() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPause");
        }

        super.onPause();
    }

    @Override
    protected void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        super.onResume();
    }

    @Override
    public void finish() {
        if (DEBUG) {
            Log.d(LOG_TAG, "finish");
        }

        super.finish();
    }

    @Override
    public void onRequestPermissions(@NonNull Permission[] grantedPermissions) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRequestPermissions grantedPermissions=" + Arrays.toString(grantedPermissions));
        }

        boolean accessLocationGranted = false;
        for (Permission grantedPermission : grantedPermissions) {
            switch (grantedPermission) {
                case ACCESS_FINE_LOCATION:
                case ACCESS_COARSE_LOCATION:
                    accessLocationGranted = true;
                    break;
            }
        }

        if (accessLocationGranted) {
            mCardView.setVisibility(View.VISIBLE);
            startFusedLocation();
        } else {
            showLocationSettings();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onMapReady: googleMap=" + googleMap);
        }

        mGoogleMap = googleMap;

        initLocation();
    }

    @Override
    public void send() {
        if (DEBUG) {
            Log.d(LOG_TAG, "send");
        }

        if (mUserLocation != null && mGoogleMap != null) {
            Intent data = new Intent();

            data.putExtra(Intents.INTENT_TEXT_MESSAGE, mEditText.getText().toString());
            data.putExtra(Intents.INTENT_ALLOW_COPY_FILE, mAllowCopy);
            data.putExtra(Intents.INTENT_ALLOW_COPY_TEXT, mAllowCopy);
            data.putExtra(Intents.INTENT_ALLOW_EPHEMERAL, mAllowEphemeralMessage);
            data.putExtra(Intents.INTENT_EXPIRE_TIMEOUT, mExpireTimeout);

            LatLngBounds latLngBounds = mGoogleMap.getProjection().getVisibleRegion().latLngBounds;
            double latitudeDelta = latLngBounds.northeast.latitude - latLngBounds.southwest.latitude;
            double longitudeDelta = latLngBounds.northeast.longitude - latLngBounds.southwest.longitude;

            data.putExtra(Intents.INTENT_LATITUDE_DELTA, latitudeDelta);
            data.putExtra(Intents.INTENT_LONGITUDE_DELTA, longitudeDelta);
            data.putExtra(Intents.INTENT_ALTITUDE, mUserLocation.getAltitude());
            data.putExtra(Intents.INTENT_LATITUDE, mUserLocation.getLatitude());
            data.putExtra(Intents.INTENT_LONGITUDE, mUserLocation.getLongitude());

            setResult(RESULT_OK, data);

            mUserLocation = null;
            mGoogleMap.clear();
            finish();
        }
    }

    //
    // Private methods
    //

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());

        setContentView(R.layout.preview_location_activity);

        setBackgroundColor(Color.BLACK);
        setStatusBarColor(Color.BLACK);
        showToolBar(false);
        showBackButton(true);

        applyInsets(R.id.preview_activity_layout, -1, R.id.preview_activity_content_send_view, Color.BLACK, false);

        super.initViews();

        mQualityView.setVisibility(View.GONE);

        mSendView.setAlpha(0.5f);

        float radius = DESIGN_ACTION_VIEW_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        mCardView = findViewById(R.id.preview_location_activity_card_view);
        mCardView.setRadius(radius);
        mCardView.setZ(2);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mCardView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_MAP_TOP_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_MAP_BOTTOM_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.leftMargin = (int) (DESIGN_MAP_SIDE_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_MAP_SIDE_MARGIN * Design.WIDTH_RATIO);

        MapView mapView = findViewById(R.id.preview_location_activity_map_view);
        mapView.onCreate(null);
        mapView.getMapAsync(this);

        if (mIsCertified) {
            mCertifiedImageView.setVisibility(View.VISIBLE);
        } else {
            mCertifiedImageView.setVisibility(View.GONE);
        }

        mNameView.setText(mContactName);

        View mapTypeView = findViewById(R.id.preview_location_activity_map_type_view);
        mapTypeView.setOnClickListener(v -> onMapTypeCLick());

        int actionSize = (int) (DESIGN_ACTION_SIZE * Design.HEIGHT_RATIO);
        int actionLeftMargin = (int) (Design.DISPLAY_WIDTH - (DESIGN_MAP_SIDE_MARGIN * Design.WIDTH_RATIO * 2) - actionSize - (DESIGN_ACTION_HORIZONTAL_MARGIN * Design.WIDTH_RATIO));

        ViewGroup.LayoutParams layoutParams = mapTypeView.getLayoutParams();
        layoutParams.width = actionSize;
        layoutParams.height = actionSize;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mapTypeView.getLayoutParams();
        marginLayoutParams.leftMargin = actionLeftMargin;

        RoundedView mapTypeRoundedView = findViewById(R.id.preview_location_activity_map_type_view_background_view);
        mapTypeRoundedView.setColor(Color.WHITE);

        mMapTypeImageView = findViewById(R.id.preview_location_activity_map_type_view_image_view);
        mMapTypeImageView.setPadding(6, 6, 6, 6);

        ViewTreeObserver viewTreeObserver = mCardView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewTreeObserver viewTreeObserver = mCardView.getViewTreeObserver();
                viewTreeObserver.removeOnGlobalLayoutListener(this);

                int actionMargin = (int) (mCardView.getHeight() - actionSize - (DESIGN_ACTION_VERTICAL_MARGIN * Design.HEIGHT_RATIO));
                ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mapTypeView.getLayoutParams();
                marginLayoutParams.topMargin = actionMargin;
                marginLayoutParams.leftMargin = actionLeftMargin;
                mapTypeView.setLayoutParams(marginLayoutParams);
            }
        });
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    protected void updateViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateViews");
        }
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

    @Override
    protected void onTwinlifeReady() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onTwinlifeReady");
        }

        if (mContactAvatar == null && mImageId != null) {
            mContactAvatar = getTwinmeContext().getImageService().getImage(mImageId, ImageService.Kind.THUMBNAIL);

            runOnUiThread(() -> {
                mAvatarView.setImage(this, null,
                        new CircularImageDescriptor(mContactAvatar, 0.5f, 0.5f, 0.5f));
            });
        }
    }

    private void initLocation() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initLocation");
        }

        if (mFusedLocationClient == null) {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

            mLocationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                    .setWaitForAccurateLocation(false)
                    .setMinUpdateIntervalMillis(LocationRequest.Builder.IMPLICIT_MIN_UPDATE_INTERVAL)
                    .setMinUpdateDistanceMeters(1)
                    .build();

            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {

                    List<Location> locations = locationResult.getLocations();
                    if (!locations.isEmpty()) {
                        mUserLocation = locations.get(0);
                        addLocation();
                        stopLocationUpdates();
                    }
                }
            };

            startFusedLocation();
        }

        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            showLocationSettings();
        } else {
            Permission[] permissions = new Permission[]{Permission.ACCESS_FINE_LOCATION, Permission.ACCESS_COARSE_LOCATION};
            if (checkPermissions(permissions)) {
                startFusedLocation();
            }
        }
    }

    private void startFusedLocation() {
        if (DEBUG) {
            Log.d(LOG_TAG, "startFusedLocation");
        }

        try {
            mFusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    mUserLocation = location;
                    addLocation();
                }
            });
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void stopLocationUpdates() {
        if (DEBUG) {
            Log.d(LOG_TAG, "stopLocationUpdates");
        }

        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    private void addLocation() {
        if (DEBUG) {
            Log.d(LOG_TAG, "addLocation");
        }

        if (mGoogleMap == null) {
            return;
        }

        try {
            LatLng latLng = new LatLng(mUserLocation.getLatitude(), mUserLocation.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("");
            markerOptions.draggable(false);

            this.getMarkerBitmapFromView((Bitmap avatar) -> {
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(avatar));
                markerOptions.visible(true);
            });

            mGoogleMap.clear();
            mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            mGoogleMap.addMarker(markerOptions);

            double latitudeDelta = 0.05;
            double longitudeDelta = 0.05;
            LatLngBounds.Builder displayBuilder = new LatLngBounds.Builder();
            displayBuilder.include(new LatLng(mUserLocation.getLatitude() - (latitudeDelta / 2), mUserLocation.getLongitude() - (longitudeDelta / 2)));
            displayBuilder.include(new LatLng(mUserLocation.getLatitude() + (latitudeDelta / 2), mUserLocation.getLongitude() + (longitudeDelta / 2)));

            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(displayBuilder.build(), 0));
            mSendView.setAlpha((float) 1.0);

        } catch (Throwable ex) {
            // An exception could be raised if the wrong Google play service is used.
            Log.w(LOG_TAG, "Error: " + ex.getMessage());
        }
    }

    private void getMarkerBitmapFromView(@NonNull TwinmeContext.Consumer<Bitmap> uiConsumer) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getMarkerBitmapFromView");
        }

        Bitmap avatar = getTwinmeApplication().getDefaultAvatar();
        if (getTwinmeApplication().getCurrentSpace().getProfile() != null && getTwinmeApplication().getCurrentSpace().getProfile().getAvatarId() != null) {
            avatar = getTwinmeContext().getImageService().getImage(getTwinmeApplication().getCurrentSpace().getProfile().getAvatarId(), ImageService.Kind.THUMBNAIL);
        }

        View customMarkerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.location_marker, null);
        customMarkerView.measure((int) (DESIGN_MARKER_VIEW_WIDTH * Design.WIDTH_RATIO), (int) (DESIGN_MARKER_VIEW_WIDTH * Design.HEIGHT_RATIO));
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());

        CircularImageView markerImageView = customMarkerView.findViewById(R.id.location_marker_avatar_view);
        markerImageView.setImage(this, Design.LIGHT_CIRCULAR_SHADOW_DESCRIPTOR,
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

        uiConsumer.accept(returnedBitmap);
    }

    private void showLocationSettings() {
        if (DEBUG) {
            Log.d(LOG_TAG, "showLocationSettings");
        }

        hideKeyboard();
        
        ViewGroup viewGroup = findViewById(R.id.preview_activity_layout);

        mCardView.setVisibility(View.GONE);
        DefaultConfirmView defaultConfirmView = new DefaultConfirmView(this, null);
        defaultConfirmView.setTitle(getString(R.string.application_location));
        defaultConfirmView.setMessage(getString(R.string.application_location_enabled));
        defaultConfirmView.setImage(null);
        defaultConfirmView.setConfirmTitle(getString(R.string.application_authorization_go_settings));

        AbstractBottomSheetView.Observer observer = new AbstractBottomSheetView.Observer() {
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
                viewGroup.removeView(defaultConfirmView);
                mCardView.setVisibility(View.VISIBLE);
                if (fromConfirmAction) {
                    Intent locationSettings = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(locationSettings);
                }
            }
        };
        defaultConfirmView.setObserver(observer);
        viewGroup.addView(defaultConfirmView);
        defaultConfirmView.show();
    }

}
