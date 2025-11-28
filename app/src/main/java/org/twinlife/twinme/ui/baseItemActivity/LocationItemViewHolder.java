/*
 *  Copyright (c) 2019-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.percentlayout.widget.PercentRelativeLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinlife.ConversationService.GeolocationDescriptor;
import org.twinlife.twinlife.ConversationService.ImageDescriptor;
import org.twinlife.twinlife.ConversationService.NamedFileDescriptor;
import org.twinlife.twinlife.ConversationService.ObjectDescriptor;
import org.twinlife.twinlife.ConversationService.VideoDescriptor;
import org.twinlife.twinme.TwinmeContext;
import org.twinlife.twinme.skin.CircularImageDescriptor;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.LocationActivity;
import org.twinlife.twinme.utils.CircularImageView;
import org.twinlife.twinme.utils.CommonUtils;
import org.twinlife.twinme.utils.EphemeralView;
import org.twinlife.twinme.utils.RoundedFrameLayout;
import org.twinlife.twinme.utils.RoundedImageView;
import org.twinlife.twinme.utils.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class LocationItemViewHolder extends ItemViewHolder implements OnMapReadyCallback, GoogleMap.SnapshotReadyCallback {

    private static final float DESIGN_MARKER_VIEW_WIDTH = 120f;
    private static final float DESIGN_AVATAR_BORDER_INSET = 0f;
    private static final int DESIGN_BORDER_COLOR_WHITE = Color.argb(255, 255, 255, 255);
    private static final float DESIGN_AVATAR_BORDER_THICKNESS = 4f / 120f;

    private static final float DESIGN_EPHEMERAL_SIZE = 28f;
    private static final float DESIGN_EPHEMERAL_RIGHT_MARGIN = 20f;
    private static final float DESIGN_EPHEMERAL_BOTTOM_MARGIN = 16f;

    private final View mReplyView;
    private final TextView mReplyTextView;
    private final View mReplyToImageContentView;
    private final RoundedImageView mReplyImageView;
    private final GradientDrawable mReplyGradientDrawable;
    private final GradientDrawable mReplyToImageContentGradientDrawable;
    private final MapView mMapView;
    private final RoundedFrameLayout mLocationItemContainer;
    private final DeleteProgressView mDeleteView;
    private final RoundedImageView mImageView;
    private final EphemeralView mEphemeralView;

    private CountDownTimer mTimer;

    private final boolean mAllowLongClick;

    private GoogleMap mGoogleMap;

    LocationItemViewHolder(BaseItemActivity baseItemActivity, View view, boolean allowClick, boolean allowLongClick) {

        super(baseItemActivity, view,
                R.id.base_item_activity_location_item_layout_container,
                R.id.base_item_activity_location_item_state_view,
                R.id.base_item_activity_location_item_state_avatar_view,
                R.id.base_item_activity_location_item_overlay_view,
                R.id.base_item_activity_location_item_annotation_view,
                R.id.base_item_activity_location_item_selected_view,
                R.id.base_item_activity_location_item_selected_image_view);

        mLocationItemContainer = view.findViewById(R.id.base_item_activity_location_item_map_container);

        mMapView = view.findViewById(R.id.base_item_activity_location_item_map_view);
        mMapView.onCreate(null);

        mImageView = view.findViewById(R.id.base_item_activity_location_item_image_view);

        mReplyTextView = view.findViewById(R.id.base_item_activity_location_item_reply_text);
        mReplyTextView.setPadding(MESSAGE_ITEM_TEXT_WIDTH_PADDING, MESSAGE_ITEM_TEXT_DEFAULT_PADDING, MESSAGE_ITEM_TEXT_WIDTH_PADDING, MESSAGE_ITEM_TEXT_DEFAULT_PADDING);
        mReplyTextView.setTypeface(getMessageFont().typeface);
        mReplyTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getMessageFont().size);
        mReplyTextView.setTextColor(Design.REPLY_FONT_COLOR);
        mReplyTextView.setMaxLines(3);
        mReplyTextView.setEllipsize(TextUtils.TruncateAt.END);

        mReplyView = view.findViewById(R.id.base_item_activity_location_item_reply_view);

        mReplyView.setOnClickListener(v -> onReplyClick());

        mReplyGradientDrawable = new GradientDrawable();
        mReplyGradientDrawable.mutate();
        mReplyGradientDrawable.setColor(Design.REPLY_BACKGROUND_COLOR);
        mReplyGradientDrawable.setShape(GradientDrawable.RECTANGLE);
        mReplyView.setBackground(mReplyGradientDrawable);

        mReplyImageView = view.findViewById(R.id.base_item_activity_location_item_reply_image_view);
        ViewGroup.LayoutParams layoutParams = mReplyImageView.getLayoutParams();
        layoutParams.width = REPLY_IMAGE_ITEM_MAX_WIDTH;
        layoutParams.height = REPLY_IMAGE_ITEM_MAX_HEIGHT;

        View replyContainerImageView = view.findViewById(R.id.base_item_activity_location_item_reply_container_image_view);
        replyContainerImageView.setPadding(REPLY_IMAGE_WIDTH_MARGIN, REPLY_IMAGE_HEIGHT_MARGIN, REPLY_IMAGE_WIDTH_MARGIN, REPLY_IMAGE_HEIGHT_MARGIN);

        mReplyToImageContentView = view.findViewById(R.id.base_item_activity_location_item_reply_image_content_view);

        mReplyToImageContentView.setOnClickListener(v -> onReplyClick());

        mReplyToImageContentGradientDrawable = new GradientDrawable();
        mReplyToImageContentGradientDrawable.mutate();
        mReplyToImageContentGradientDrawable.setColor(Design.REPLY_BACKGROUND_COLOR);
        mReplyToImageContentGradientDrawable.setShape(GradientDrawable.RECTANGLE);
        mReplyToImageContentView.setBackground(mReplyToImageContentGradientDrawable);

        mDeleteView = view.findViewById(R.id.base_item_activity_location_item_delete_view);

        if (allowClick) {
            mImageView.setOnClickListener(v -> onMapClick());
            mMapView.setOnClickListener(v -> onMapClick());
        }
        if (allowLongClick) {
            mImageView.setOnLongClickListener(v -> {
                baseItemActivity.onItemLongPress(getItem());
                return true;
            });

            mReplyView.setOnLongClickListener(v -> {
                baseItemActivity.onItemLongPress(getItem());
                return true;
            });

            mReplyToImageContentView.setOnLongClickListener(v -> {
                baseItemActivity.onItemLongPress(getItem());
                return true;
            });
        }

        mAllowLongClick = allowClick;

        mEphemeralView = view.findViewById(R.id.base_item_activity_location_item_ephemeral_view);
        mEphemeralView.setColor(Color.BLACK);

        layoutParams = mEphemeralView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_EPHEMERAL_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_EPHEMERAL_SIZE * Design.HEIGHT_RATIO);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mEphemeralView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_EPHEMERAL_RIGHT_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.topMargin = (int) (DESIGN_EPHEMERAL_BOTTOM_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_EPHEMERAL_RIGHT_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_EPHEMERAL_BOTTOM_MARGIN * Design.HEIGHT_RATIO);
    }

    @Override
    void onBind(@NonNull Item item) {

        if (!(item instanceof LocationItem)) {
            return;
        }
        super.onBind(item);

        // Compute the corner radii only once!
        final float[] cornerRadii = getCornerRadii();
        final LocationItem locationItem = (LocationItem) item;
        final GeolocationDescriptor geolocationDescriptor = locationItem.getGeolocationDescriptor();

        @SuppressWarnings("deprecation")
        PercentRelativeLayout.LayoutParams layoutParams = (PercentRelativeLayout.LayoutParams) mLocationItemContainer.getLayoutParams();

        if ((locationItem.getCorners() & Item.TOP_RIGHT) == 0) {
            layoutParams.topMargin = ITEM_TOP_MARGIN1;
        } else {
            layoutParams.topMargin = ITEM_TOP_MARGIN2;
        }
        if ((locationItem.getCorners() & Item.BOTTOM_RIGHT) == 0) {
            layoutParams.bottomMargin = ITEM_BOTTOM_MARGIN1;
        } else {
            layoutParams.bottomMargin = ITEM_BOTTOM_MARGIN2;
        }

        mLocationItemContainer.setLayoutParams(layoutParams);
        mImageView.setLayoutParams(layoutParams);
        mLocationItemContainer.setCornerRadii(cornerRadii);

        mReplyGradientDrawable.setCornerRadii(cornerRadii);
        mReplyToImageContentGradientDrawable.setCornerRadii(cornerRadii);

        mReplyView.setVisibility(View.GONE);
        mReplyTextView.setVisibility(View.GONE);
        mReplyToImageContentView.setVisibility(View.GONE);
        mReplyImageView.setVisibility(View.GONE);

        final ConversationService.Descriptor replyToDescriptor = locationItem.getReplyToDescriptor();
        if (replyToDescriptor != null) {

            RelativeLayout.LayoutParams relativeLayoutParams = (RelativeLayout.LayoutParams) mImageView.getLayoutParams();

            switch (replyToDescriptor.getType()) {
                case OBJECT_DESCRIPTOR:
                    mReplyView.setVisibility(View.VISIBLE);
                    mReplyTextView.setVisibility(View.VISIBLE);
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_location_item_reply_text);

                    ObjectDescriptor objectDescriptor = (ObjectDescriptor) replyToDescriptor;
                    mReplyTextView.setText(objectDescriptor.getMessage());
                    break;

                case IMAGE_DESCRIPTOR:
                    mReplyToImageContentView.setVisibility(View.VISIBLE);
                    mReplyImageView.setVisibility(View.VISIBLE);
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_location_item_reply_container_image_view);

                    setReplyImage(mReplyImageView, (ImageDescriptor) replyToDescriptor);
                    break;

                case VIDEO_DESCRIPTOR:
                    mReplyToImageContentView.setVisibility(View.VISIBLE);
                    mReplyImageView.setVisibility(View.VISIBLE);
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_location_item_reply_container_image_view);

                    setReplyImage(mReplyImageView, (VideoDescriptor) replyToDescriptor);
                    break;

                case AUDIO_DESCRIPTOR:
                    mReplyView.setVisibility(View.VISIBLE);
                    mReplyTextView.setVisibility(View.VISIBLE);
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_location_item_reply_text);

                    mReplyTextView.setText(getString(R.string.conversation_activity_audio_message));
                    break;

                case GEOLOCATION_DESCRIPTOR:
                    mReplyView.setVisibility(View.VISIBLE);
                    mReplyTextView.setVisibility(View.VISIBLE);
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_location_item_reply_text);

                    mReplyTextView.setText(getString(R.string.application_location));
                    break;

                case NAMED_FILE_DESCRIPTOR:
                    mReplyView.setVisibility(View.VISIBLE);
                    mReplyTextView.setVisibility(View.VISIBLE);
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_location_item_reply_text);

                    NamedFileDescriptor fileDescriptor = (NamedFileDescriptor) replyToDescriptor;
                    mReplyTextView.setText(fileDescriptor.getName());
                    break;
            }
        }

        ViewGroup.LayoutParams overlayLayoutParams = getOverlayView().getLayoutParams();
        overlayLayoutParams.width = getContainer().getWidth();
        overlayLayoutParams.height = itemView.getHeight();
        getOverlayView().setLayoutParams(overlayLayoutParams);

        final BitmapDrawable bitmapDrawable;
        if (geolocationDescriptor.isValidLocalMap()) {
            bitmapDrawable = Utils.getBitmapDrawable(getBaseItemActivity(), geolocationDescriptor.getLocalMapPath(), LOCATION_ITEM_MAX_WIDTH, LOCATION_ITEM_MAX_HEIGHT);
        } else {
            bitmapDrawable = null;
        }

        if (bitmapDrawable != null) {
            mImageView.setImageBitmap(bitmapDrawable.getBitmap(), cornerRadii);
            mMapView.setVisibility(View.GONE);
            mImageView.setVisibility(View.VISIBLE);
        } else {
            if (mGoogleMap == null) {
                mMapView.getMapAsync(this);
            }
            mMapView.setVisibility(View.VISIBLE);
            mImageView.setVisibility(View.INVISIBLE);
        }

        if (item.isEphemeralItem()) {
            mEphemeralView.setVisibility(View.VISIBLE);
            startEphemeralAnimation();
        } else {
            mEphemeralView.setVisibility(View.GONE);
        }

        if (getBaseItemActivity().isMenuOpen()) {
            getOverlayView().setVisibility(View.VISIBLE);
            if (getBaseItemActivity().isSelectedItem(getItem().getDescriptorId())) {
                itemView.setBackgroundColor(Design.BACKGROUND_COLOR_WHITE_OPACITY85);
                getOverlayView().setVisibility(View.INVISIBLE);
            }
        } else {
            getOverlayView().setVisibility(View.INVISIBLE);
            itemView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    @Override
    void onViewRecycled() {

        super.onViewRecycled();

        if (mGoogleMap != null) {
            mGoogleMap.clear();
            mGoogleMap = null;
        }

        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        mImageView.setImageBitmap(null, null);
        mReplyImageView.setImageBitmap(null, null);
        mDeleteView.setVisibility(View.GONE);
        mDeleteView.setOnDeleteProgressListener(null);
        setDeleteAnimationStarted(false);
    }

    @Override
    List<View> clickableViews() {

        return new ArrayList<View>() {
            {
                add(mMapView);
                add(mImageView);
            }
        };
    }

    @Override
    void startDeletedAnimation() {

        if (isDeleteAnimationStarted()) {
            return;
        }

        setDeleteAnimationStarted(true);
        mDeleteView.setVisibility(View.VISIBLE);

        ViewGroup.MarginLayoutParams deleteLayoutParams = (ViewGroup.MarginLayoutParams) mDeleteView.getLayoutParams();
        deleteLayoutParams.width = mLocationItemContainer.getWidth();
        deleteLayoutParams.height = mLocationItemContainer.getHeight();
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) mLocationItemContainer.getLayoutParams();
        deleteLayoutParams.topMargin = layoutParams.topMargin;
        deleteLayoutParams.bottomMargin = layoutParams.bottomMargin;
        mDeleteView.setLayoutParams(deleteLayoutParams);
        mDeleteView.setCornerRadii(getCornerRadii());
        mDeleteView.setX(mLocationItemContainer.getX());
        mDeleteView.setOnDeleteProgressListener(() -> deleteItem(getItem()));

        float progress = 0;
        int animationDuration = DESIGN_DELETE_ANIMATION_DURATION;
        final Item item = getItem();
        if (item.getDeleteProgress() > 0) {
            progress = item.getDeleteProgress() / 100.0f;
            animationDuration = (int) (BaseItemViewHolder.DESIGN_DELETE_ANIMATION_DURATION - ((item.getDeleteProgress() * BaseItemViewHolder.DESIGN_DELETE_ANIMATION_DURATION) / 100.0));
        }

        mDeleteView.startAnimation(animationDuration, progress);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        GeolocationDescriptor geolocationDescriptor = getLocationItem().getGeolocationDescriptor();
        LatLng userLocation = new LatLng(geolocationDescriptor.getLatitude(), geolocationDescriptor.getLongitude());

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(userLocation);
        markerOptions.title("");
        try {
            this.getMarkerBitmapFromView((Bitmap avatar) -> markerOptions.icon(BitmapDescriptorFactory.fromBitmap(avatar)));
        } catch (Exception e) {
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker());
        }
        markerOptions.draggable(false);
        markerOptions.visible(true);

        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mGoogleMap.getUiSettings().setMapToolbarEnabled(false);
        mGoogleMap.addMarker(markerOptions);
        mGoogleMap.setOnMapLoadedCallback(this::saveMapSnapshot);

        double latitudeDelta = geolocationDescriptor.getMapLatitudeDelta();
        double longitudeDelta = geolocationDescriptor.getMapLongitudeDelta();
        LatLngBounds.Builder displayBuilder = new LatLngBounds.Builder();
        displayBuilder.include(new LatLng(userLocation.latitude - (latitudeDelta / 2), userLocation.longitude - (longitudeDelta / 2)));
        displayBuilder.include(new LatLng(userLocation.latitude + (latitudeDelta / 2), userLocation.longitude + (longitudeDelta / 2)));

        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(displayBuilder.build(), 0));

        if (mAllowLongClick) {
            mGoogleMap.setOnMapLongClickListener(v -> getBaseItemActivity().onItemLongPress(getItem()));
        }
    }

    @Override
    public void onSnapshotReady(Bitmap bitmap) {

        GeolocationDescriptor geolocationDescriptor = getLocationItem().getGeolocationDescriptor();

        File tmpFile = CommonUtils.saveBitmap(bitmap);
        if (tmpFile != null) {
            getBaseItemActivity().saveGeolocationMap(Uri.fromFile(tmpFile), geolocationDescriptor.getDescriptorId());
        }
    }

    //
    // Private methods
    //

    private LocationItem getLocationItem() {

        return (LocationItem) getItem();
    }

    private void getMarkerBitmapFromView(TwinmeContext.Consumer<Bitmap> avatarConsumer) {

        getBaseItemActivity().getMapAvatar(null, (Bitmap avatar) -> {
            if (avatar == null) {
                avatar = getBaseItemActivity().getTwinmeApplication().getDefaultAvatar();
            }

            View customMarkerView = ((LayoutInflater) getBaseItemActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.location_marker, null);
            customMarkerView.measure((int) (DESIGN_MARKER_VIEW_WIDTH * Design.WIDTH_RATIO), (int) (DESIGN_MARKER_VIEW_WIDTH * Design.HEIGHT_RATIO));
            customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());

            CircularImageView markerImageView = customMarkerView.findViewById(R.id.location_marker_avatar_view);
            markerImageView.setImage(getBaseItemActivity(), Design.LIGHT_CIRCULAR_SHADOW_DESCRIPTOR,
                    new CircularImageDescriptor(avatar, 0.5f, 0.5f, Design.LIGHT_CIRCULAR_SHADOW_DESCRIPTOR.imageWithShadowRadius, DESIGN_BORDER_COLOR_WHITE, DESIGN_AVATAR_BORDER_THICKNESS, DESIGN_AVATAR_BORDER_INSET));
            customMarkerView.buildDrawingCache();

            Bitmap bitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
            Drawable drawable = customMarkerView.getBackground();
            if (drawable != null) {
                drawable.draw(canvas);
            }
            customMarkerView.draw(canvas);
            avatarConsumer.accept(bitmap);
        });
    }

    private void saveMapSnapshot() {

        if (mGoogleMap != null) {
            mGoogleMap.snapshot(this);
        }
    }

    private void onMapClick() {

        final BaseItemActivity activity = getBaseItemActivity();
        if (activity.isSelectItemMode()) {
            onContainerClick();
            return;
        }

        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        Intent intent = new Intent(activity, LocationActivity.class);
        intent.putExtra(Intents.INTENT_DESCRIPTOR_ID, getLocationItem().getDescriptorId().toString());

        activity.getMapAvatar(null, (Bitmap avatar) -> {
            if (avatar == null) {
                avatar = activity.getTwinmeApplication().getDefaultAvatar();
            }

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            avatar.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            intent.putExtra(Intents.INTENT_AVATAR_BYTES, byteArray);
            intent.putExtra(Intents.INTENT_PROFILE_NAME, activity.getIdentityName());
            activity.startActivity(intent);
        });

    }

    private void startEphemeralAnimation() {

        if (mTimer == null && getItem().getState() == Item.ItemState.READ) {
            mTimer = new CountDownTimer(getItem().getExpireTimeout(), 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    Date now = new Date();
                    float timeSinceRead = (now.getTime() - getItem().getReadTimestamp());
                    float percent = (float) (1.0 - (timeSinceRead / getItem().getExpireTimeout()));
                    if (percent < 0) {
                        percent = 0;
                    } else if (percent > 1) {
                        percent = 1;
                    }
                    mEphemeralView.updateWithProgress(percent);
                }

                @Override
                public void onFinish() {

                }
            };
            mTimer.start();
        } else {
            mEphemeralView.updateWithProgress(1);
        }
    }
}
