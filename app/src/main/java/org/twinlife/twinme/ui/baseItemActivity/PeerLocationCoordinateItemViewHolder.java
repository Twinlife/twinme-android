/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.EphemeralView;
import org.twinlife.twinme.utils.RoundedImageView;

import java.util.Date;

public class PeerLocationCoordinateItemViewHolder extends PeerItemViewHolder {

    private static final float DESIGN_LOCATION_ICON_MARGIN = 14f;
    private static final float DESIGN_LOCATION_ICON_SIZE = 48f;
    private static final float DESIGN_MESSAGE_MARGIN = 12f;
    private static final float DESIGN_EPHEMERAL_SIZE = 28f;
    private static final float DESIGN_EPHEMERAL_RIGHT_MARGIN = 20f;
    private static final float DESIGN_EPHEMERAL_BOTTOM_MARGIN = 16f;

    private final View mLocationItemContainer;
    private final GradientDrawable mGradientDrawable;
    private final TextView mCoordinateTextView;
    private final TextView mMessageView;

    private final View mReplyView;
    private final TextView mReplyTextView;
    private final View mReplyToImageContentView;
    private final RoundedImageView mReplyImageView;
    private final GradientDrawable mReplyGradientDrawable;
    private final GradientDrawable mReplyToImageContentGradientDrawable;
    private final EphemeralView mEphemeralView;

    private CountDownTimer mTimer;

    PeerLocationCoordinateItemViewHolder(BaseItemActivity baseItemActivity, View view, boolean allowClick, boolean allowLongClick) {

        super(baseItemActivity, view,
                R.id.base_item_activity_peer_location_item_layout_container,
                R.id.base_item_activity_peer_location_item_avatar,
                R.id.base_item_activity_peer_location_item_overlay_view,
                R.id.base_item_activity_peer_location_item_annotation_view,
                R.id.base_item_activity_peer_location_item_selected_view,
                R.id.base_item_activity_peer_location_item_selected_image_view);

        mLocationItemContainer = view.findViewById(R.id.base_item_activity_peer_location_item_coordinate_container);

        mLocationItemContainer.setPadding(FILE_ITEM_WIDTH_PADDING, FILE_ITEM_HEIGHT_PADDING, FILE_ITEM_WIDTH_PADDING, FILE_ITEM_HEIGHT_PADDING);
        mGradientDrawable = new GradientDrawable();
        mGradientDrawable.mutate();
        mGradientDrawable.setColor(Design.GREY_ITEM_COLOR);
        mGradientDrawable.setShape(GradientDrawable.RECTANGLE);
        mLocationItemContainer.setBackground(mGradientDrawable);
        mLocationItemContainer.setClickable(false);

        if (allowClick) {
            mLocationItemContainer.setOnClickListener(v -> onCoordinateClick());
        }

        if (allowLongClick) {
            mLocationItemContainer.setOnLongClickListener(v -> {
                baseItemActivity.onItemLongPress(getItem());
                return true;
            });
        }

        mCoordinateTextView = view.findViewById(R.id.base_item_activity_peer_location_item_coordinate_text_view);
        Design.updateTextFont(mCoordinateTextView, Design.FONT_MEDIUM28);
        mCoordinateTextView.setTextColor(getBaseItemActivity().getCustomAppearance().getPeerMessageTextColor());

        ImageView locationImageView = view.findViewById(R.id.base_item_activity_peer_location_item_coordinate_icon_view);
        locationImageView.setColorFilter(Design.BLACK_COLOR);

        ViewGroup.LayoutParams layoutParams = locationImageView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_LOCATION_ICON_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_LOCATION_ICON_SIZE * Design.HEIGHT_RATIO);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) locationImageView.getLayoutParams();
        marginLayoutParams.rightMargin = (int) (DESIGN_LOCATION_ICON_MARGIN * Design.WIDTH_RATIO);

        mMessageView = view.findViewById(R.id.base_item_activity_peer_location_item_coordinate_message_view);
        Design.updateTextFont(mMessageView, getBaseItemActivity().getMessageFont());
        mMessageView.setTextColor(getBaseItemActivity().getCustomAppearance().getPeerMessageTextColor());

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mMessageView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_MESSAGE_MARGIN * Design.HEIGHT_RATIO);

        mReplyTextView = view.findViewById(R.id.base_item_activity_peer_location_item_reply_text);
        mReplyTextView.setPadding(MESSAGE_ITEM_TEXT_WIDTH_PADDING, MESSAGE_ITEM_TEXT_DEFAULT_PADDING, MESSAGE_ITEM_TEXT_WIDTH_PADDING, MESSAGE_ITEM_TEXT_DEFAULT_PADDING);
        mReplyTextView.setTypeface(getBaseItemActivity().getMessageFont().typeface);
        mReplyTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getBaseItemActivity().getMessageFont().size);
        mReplyTextView.setTextColor(Design.REPLY_FONT_COLOR);
        mReplyTextView.setMaxLines(3);
        mReplyTextView.setEllipsize(TextUtils.TruncateAt.END);

        mReplyView = view.findViewById(R.id.base_item_activity_peer_location_item_reply_view);

        mReplyView.setOnClickListener(v -> onReplyClick());

        mReplyView.setOnLongClickListener(v -> {
            baseItemActivity.onItemLongPress(getItem());
            return true;
        });

        mReplyGradientDrawable = new GradientDrawable();
        mReplyGradientDrawable.mutate();
        mReplyGradientDrawable.setColor(Design.REPLY_BACKGROUND_COLOR);
        mReplyView.setBackground(mReplyGradientDrawable);

        mReplyImageView = view.findViewById(R.id.base_item_activity_peer_location_item_reply_image_view);
        layoutParams = mReplyImageView.getLayoutParams();
        layoutParams.width = REPLY_IMAGE_ITEM_MAX_WIDTH;
        layoutParams.height = REPLY_IMAGE_ITEM_MAX_HEIGHT;

        View replyContainerImageView = view.findViewById(R.id.base_item_activity_peer_location_item_reply_container_image_view);
        replyContainerImageView.setPadding(REPLY_IMAGE_WIDTH_MARGIN, REPLY_IMAGE_HEIGHT_MARGIN, REPLY_IMAGE_WIDTH_MARGIN, REPLY_IMAGE_HEIGHT_MARGIN);

        mReplyToImageContentView = view.findViewById(R.id.base_item_activity_peer_location_item_reply_image_content_view);

        mReplyToImageContentView.setOnClickListener(v -> onReplyClick());

        mReplyToImageContentView.setOnLongClickListener(v -> {
            baseItemActivity.onItemLongPress(getItem());
            return true;
        });

        mReplyToImageContentGradientDrawable = new GradientDrawable();
        mReplyToImageContentGradientDrawable.mutate();
        mReplyToImageContentGradientDrawable.setColor(Design.REPLY_BACKGROUND_COLOR);
        mReplyToImageContentGradientDrawable.setShape(GradientDrawable.RECTANGLE);
        mReplyToImageContentView.setBackground(mReplyToImageContentGradientDrawable);

        mEphemeralView = view.findViewById(R.id.base_item_activity_peer_location_item_ephemeral_view);
        mEphemeralView.setColor(Color.BLACK);

        layoutParams = mEphemeralView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_EPHEMERAL_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_EPHEMERAL_SIZE * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mEphemeralView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_EPHEMERAL_RIGHT_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.topMargin = (int) (DESIGN_EPHEMERAL_BOTTOM_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_EPHEMERAL_RIGHT_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_EPHEMERAL_BOTTOM_MARGIN * Design.HEIGHT_RATIO);
    }

    @Override
    void onBind(@NonNull Item item) {

        if (!(item instanceof PeerLocationItem)) {
            return;
        }
        super.onBind(item);

        final PeerLocationItem peerLocationItem = (PeerLocationItem) item;

        // Compute the corner radii only once!
        final float[] cornerRadii = getCornerRadii();
        mGradientDrawable.setCornerRadii(cornerRadii);
        mGradientDrawable.setColor(getBaseItemActivity().getCustomAppearance().getPeerMessageBackgroundColor());
        if (getBaseItemActivity().getCustomAppearance().getPeerMessageBorderColor() != Color.TRANSPARENT) {
            mGradientDrawable.setStroke(Design.BORDER_WIDTH, getBaseItemActivity().getCustomAppearance().getPeerMessageBorderColor());
        }

        String coordinate = peerLocationItem.getGeolocationDescriptor().getLatitude() + "\n" + peerLocationItem.getGeolocationDescriptor().getLongitude();
        mCoordinateTextView.setText(coordinate);

        if (getBaseItemActivity().getContactName() != null) {
            String errorMessage = String.format(getBaseItemActivity().getString(R.string.info_item_activity_location_map_error), getBaseItemActivity().getPeerName(item.getPeerTwincodeOutboundId()));
            mMessageView.setText(errorMessage);
        }

        mReplyGradientDrawable.setCornerRadii(cornerRadii);
        mReplyToImageContentGradientDrawable.setCornerRadii(cornerRadii);

        mReplyView.setVisibility(View.GONE);
        mReplyTextView.setVisibility(View.GONE);
        mReplyToImageContentView.setVisibility(View.GONE);
        mReplyImageView.setVisibility(View.GONE);

        final ConversationService.Descriptor replyToDescriptor = peerLocationItem.getReplyToDescriptor();
        if (replyToDescriptor != null) {

            RelativeLayout.LayoutParams relativeLayoutParams = (RelativeLayout.LayoutParams) mLocationItemContainer.getLayoutParams();

            switch (replyToDescriptor.getType()) {
                case OBJECT_DESCRIPTOR:
                    mReplyView.setVisibility(View.VISIBLE);
                    mReplyTextView.setVisibility(View.VISIBLE);
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_peer_location_item_reply_text);

                    ConversationService.ObjectDescriptor objectDescriptor = (ConversationService.ObjectDescriptor) replyToDescriptor;
                    mReplyTextView.setText(objectDescriptor.getMessage());
                    break;

                case IMAGE_DESCRIPTOR:
                    mReplyToImageContentView.setVisibility(View.VISIBLE);
                    mReplyImageView.setVisibility(View.VISIBLE);
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_peer_location_item_reply_container_image_view);

                    setReplyImage(mReplyImageView, (ConversationService.ImageDescriptor) replyToDescriptor);
                    break;

                case VIDEO_DESCRIPTOR:
                    mReplyToImageContentView.setVisibility(View.VISIBLE);
                    mReplyImageView.setVisibility(View.VISIBLE);
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_peer_location_item_reply_container_image_view);

                    setReplyImage(mReplyImageView, (ConversationService.VideoDescriptor) replyToDescriptor);
                    break;

                case AUDIO_DESCRIPTOR:
                    mReplyView.setVisibility(View.VISIBLE);
                    mReplyTextView.setVisibility(View.VISIBLE);
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_peer_location_item_reply_text);

                    mReplyTextView.setText(getString(R.string.conversation_activity_audio_message));
                    break;

                case GEOLOCATION_DESCRIPTOR:
                    mReplyView.setVisibility(View.VISIBLE);
                    mReplyTextView.setVisibility(View.VISIBLE);
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_peer_location_item_reply_text);

                    mReplyTextView.setText(getString(R.string.application_location));
                    break;

                case NAMED_FILE_DESCRIPTOR:
                    mReplyView.setVisibility(View.VISIBLE);
                    mReplyTextView.setVisibility(View.VISIBLE);
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_peer_location_item_reply_text);

                    ConversationService.NamedFileDescriptor fileDescriptor = (ConversationService.NamedFileDescriptor) replyToDescriptor;
                    mReplyTextView.setText(fileDescriptor.getName());
                    break;
            }
        }

        ViewGroup.LayoutParams overlayLayoutParams = getOverlayView().getLayoutParams();
        overlayLayoutParams.width = getContainer().getWidth();
        overlayLayoutParams.height = itemView.getHeight();
        getOverlayView().setLayoutParams(overlayLayoutParams);

        if (item.isEphemeralItem()) {
            mEphemeralView.setVisibility(View.VISIBLE);
            startEphemeralAnimation();
        } else {
            mEphemeralView.setVisibility(View.GONE);
        }
    }

    @Override
    void onViewRecycled() {

        super.onViewRecycled();

        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    //
    // Private methods
    //

    private PeerLocationItem getPeerLocationItem() {

        return (PeerLocationItem) getItem();
    }

    private void onCoordinateClick() {

        Uri coordinateUri = Uri.parse("geo:" + getPeerLocationItem().getGeolocationDescriptor().getLatitude() + "," + getPeerLocationItem().getGeolocationDescriptor().getLongitude());
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, coordinateUri);
        getBaseItemActivity().startActivity(mapIntent);
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
