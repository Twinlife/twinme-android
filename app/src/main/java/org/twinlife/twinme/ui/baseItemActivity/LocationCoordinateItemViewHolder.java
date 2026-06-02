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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LocationCoordinateItemViewHolder extends ItemViewHolder {

    private static final float DESIGN_LOCATION_ICON_MARGIN = 14f;
    private static final float DESIGN_LOCATION_ICON_SIZE = 48f;
    private static final float DESIGN_SHOW_ICON_SIZE = 30f;
    private static final float DESIGN_MESSAGE_MARGIN = 12f;
    private static final float DESIGN_EPHEMERAL_SIZE = 28f;
    private static final float DESIGN_EPHEMERAL_RIGHT_MARGIN = 20f;
    private static final float DESIGN_EPHEMERAL_BOTTOM_MARGIN = 16f;

    private final View mLocationItemContainer;
    private final GradientDrawable mGradientDrawable;
    private final TextView mCoordinateTextView;
    private final View mReplyView;
    private final TextView mReplyTextView;
    private final View mReplyToImageContentView;
    private final RoundedImageView mReplyImageView;
    private final GradientDrawable mReplyGradientDrawable;
    private final GradientDrawable mReplyToImageContentGradientDrawable;
    private final DeleteProgressView mDeleteView;
    private final EphemeralView mEphemeralView;

    private CountDownTimer mTimer;

    LocationCoordinateItemViewHolder(BaseItemActivity baseItemActivity, View view, boolean allowClick, boolean allowLongClick) {

        super(baseItemActivity, view,
                R.id.base_item_activity_location_item_container,
                R.id.base_item_activity_location_item_state_view,
                R.id.base_item_activity_location_item_state_avatar_view,
                R.id.base_item_activity_location_item_overlay_view,
                R.id.base_item_activity_location_item_annotation_view,
                R.id.base_item_activity_location_item_selected_view,
                R.id.base_item_activity_location_item_selected_image_view,
                R.id.base_item_activity_location_item_coordinate_error_image_view);

        mLocationItemContainer = view.findViewById(R.id.base_item_activity_location_item_coordinate_container);

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

        mCoordinateTextView = view.findViewById(R.id.base_item_activity_location_item_coordinate_text_view);
        Design.updateTextFont(mCoordinateTextView, Design.FONT_MEDIUM28);
        mCoordinateTextView.setTextColor(getBaseItemActivity().getCustomAppearance().getMessageTextColor());

        ImageView locationImageView = view.findViewById(R.id.base_item_activity_location_item_coordinate_icon_view);
        locationImageView.setColorFilter(getBaseItemActivity().getCustomAppearance().getMessageTextColor());

        ViewGroup.LayoutParams layoutParams = locationImageView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_LOCATION_ICON_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_LOCATION_ICON_SIZE * Design.HEIGHT_RATIO);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) locationImageView.getLayoutParams();
        marginLayoutParams.rightMargin = (int) (DESIGN_LOCATION_ICON_MARGIN * Design.WIDTH_RATIO);

        TextView messageView = view.findViewById(R.id.base_item_activity_location_item_coordinate_message_view);
        Design.updateTextFont(messageView, getBaseItemActivity().getMessageFont());
        messageView.setTextColor(getBaseItemActivity().getCustomAppearance().getMessageTextColor());

        marginLayoutParams = (ViewGroup.MarginLayoutParams) messageView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_MESSAGE_MARGIN * Design.HEIGHT_RATIO);

        ImageView showImageView = view.findViewById(R.id.base_item_activity_location_item_coordinate_show_view);
        layoutParams = showImageView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_SHOW_ICON_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_SHOW_ICON_SIZE * Design.HEIGHT_RATIO);
        showImageView.setColorFilter(getBaseItemActivity().getCustomAppearance().getMessageTextColor());

        mReplyTextView = view.findViewById(R.id.base_item_activity_location_item_reply_text);
        mReplyTextView.setPadding(MESSAGE_ITEM_TEXT_WIDTH_PADDING, MESSAGE_ITEM_TEXT_DEFAULT_PADDING, MESSAGE_ITEM_TEXT_WIDTH_PADDING, MESSAGE_ITEM_TEXT_DEFAULT_PADDING);
        mReplyTextView.setTypeface(getBaseItemActivity().getMessageFont().typeface);
        mReplyTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getBaseItemActivity().getMessageFont().size);
        mReplyTextView.setTextColor(Design.REPLY_FONT_COLOR);
        mReplyTextView.setMaxLines(3);
        mReplyTextView.setEllipsize(TextUtils.TruncateAt.END);

        mReplyView = view.findViewById(R.id.base_item_activity_location_item_reply_view);

        mReplyView.setOnClickListener(v -> onReplyClick());

        mReplyView.setOnLongClickListener(v -> {
            baseItemActivity.onItemLongPress(getItem());
            return true;
        });

        mReplyGradientDrawable = new GradientDrawable();
        mReplyGradientDrawable.mutate();
        mReplyGradientDrawable.setColor(Design.REPLY_BACKGROUND_COLOR);
        mReplyView.setBackground(mReplyGradientDrawable);

        mReplyImageView = view.findViewById(R.id.base_item_activity_location_item_reply_image_view);
        layoutParams = mReplyImageView.getLayoutParams();
        layoutParams.width = REPLY_IMAGE_ITEM_MAX_WIDTH;
        layoutParams.height = REPLY_IMAGE_ITEM_MAX_HEIGHT;

        View replyContainerImageView = view.findViewById(R.id.base_item_activity_location_item_reply_container_image_view);
        replyContainerImageView.setPadding(REPLY_IMAGE_WIDTH_MARGIN, REPLY_IMAGE_HEIGHT_MARGIN, REPLY_IMAGE_WIDTH_MARGIN, REPLY_IMAGE_HEIGHT_MARGIN);

        mReplyToImageContentView = view.findViewById(R.id.base_item_activity_location_item_reply_image_content_view);

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

        mDeleteView = view.findViewById(R.id.base_item_activity_location_item_delete_view);

        mEphemeralView = view.findViewById(R.id.base_item_activity_location_item_ephemeral_view);
        mEphemeralView.setColor(getBaseItemActivity().getCustomAppearance().getMessageTextColor());

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

        if (!(item instanceof LocationItem)) {
            return;
        }
        super.onBind(item);

        // Compute the corner radii only once!
        final float[] cornerRadii = getCornerRadii();

        mGradientDrawable.setCornerRadii(cornerRadii);
        mGradientDrawable.setColor(getBaseItemActivity().getCustomAppearance().getMessageBackgroundColor());
        if (getBaseItemActivity().getCustomAppearance().getMessageBorderColor() != Color.TRANSPARENT) {
            mGradientDrawable.setStroke(Design.BORDER_WIDTH, getBaseItemActivity().getCustomAppearance().getMessageBorderColor());
        }

        String coordinate = getLocationItem().getGeolocationDescriptor().getLatitude() + "\n" + getLocationItem().getGeolocationDescriptor().getLongitude();
        mCoordinateTextView.setText(coordinate);

        mReplyGradientDrawable.setCornerRadii(cornerRadii);
        mReplyToImageContentGradientDrawable.setCornerRadii(cornerRadii);

        mReplyView.setVisibility(View.GONE);
        mReplyTextView.setVisibility(View.GONE);
        mReplyToImageContentView.setVisibility(View.GONE);
        mReplyImageView.setVisibility(View.GONE);

        final ConversationService.Descriptor replyToDescriptor = item.getReplyToDescriptor();
        if (replyToDescriptor != null) {

            RelativeLayout.LayoutParams relativeLayoutParams = (RelativeLayout.LayoutParams) mLocationItemContainer.getLayoutParams();

            switch (item.getReplyToDescriptor().getType()) {
                case OBJECT_DESCRIPTOR:
                    mReplyView.setVisibility(View.VISIBLE);
                    mReplyTextView.setVisibility(View.VISIBLE);
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_file_item_reply_text);

                    ConversationService.ObjectDescriptor objectDescriptor = (ConversationService.ObjectDescriptor) replyToDescriptor;
                    mReplyTextView.setText(objectDescriptor.getMessage());
                    break;

                case IMAGE_DESCRIPTOR:
                    mReplyToImageContentView.setVisibility(View.VISIBLE);
                    mReplyImageView.setVisibility(View.VISIBLE);
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_file_item_reply_container_image_view);

                    setReplyImage(mReplyImageView, (ConversationService.ImageDescriptor) replyToDescriptor);
                    break;

                case VIDEO_DESCRIPTOR:
                    mReplyToImageContentView.setVisibility(View.VISIBLE);
                    mReplyImageView.setVisibility(View.VISIBLE);
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_file_item_reply_container_image_view);

                    setReplyImage(mReplyImageView, (ConversationService.VideoDescriptor) replyToDescriptor);
                    break;

                case AUDIO_DESCRIPTOR:
                    mReplyView.setVisibility(View.VISIBLE);
                    mReplyTextView.setVisibility(View.VISIBLE);
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_file_item_reply_text);

                    mReplyTextView.setText(getString(R.string.conversation_view_audio_message));
                    break;

                case GEOLOCATION_DESCRIPTOR:
                    mReplyView.setVisibility(View.VISIBLE);
                    mReplyTextView.setVisibility(View.VISIBLE);
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_file_item_reply_text);

                    mReplyTextView.setText(getBaseItemActivity().getResources().getString(R.string.application_location));
                    break;

                case NAMED_FILE_DESCRIPTOR:
                    mReplyView.setVisibility(View.VISIBLE);
                    mReplyTextView.setVisibility(View.VISIBLE);
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_file_item_reply_text);

                    ConversationService.NamedFileDescriptor fileDescriptor = (ConversationService.NamedFileDescriptor) replyToDescriptor;
                    mReplyTextView.setText(fileDescriptor.getName());
                    break;
            }
        }

        if (item.isEphemeralItem()) {
            mEphemeralView.setVisibility(View.VISIBLE);
            startEphemeralAnimation();
        } else {
            mEphemeralView.setVisibility(View.GONE);
        }

        ViewGroup.LayoutParams overlayLayoutParams = getOverlayView().getLayoutParams();
        overlayLayoutParams.width = getContainer().getWidth();
        if (getBaseItemActivity().isMenuOpen()) {
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) getContainer().getLayoutParams();
            overlayLayoutParams.height = getContainer().getHeight() + layoutParams.topMargin + layoutParams.bottomMargin;
            getOverlayView().setVisibility(View.VISIBLE);
            if (getBaseItemActivity().isSelectedItem(getItem().getDescriptorId())) {
                itemView.setBackgroundColor(Design.BACKGROUND_COLOR_WHITE_OPACITY85);
                getOverlayView().setVisibility(View.INVISIBLE);
            }
        } else {
            overlayLayoutParams.height = OVERLAY_DEFAULT_HEIGHT;
            getOverlayView().setVisibility(View.INVISIBLE);
            itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        getOverlayView().setLayoutParams(overlayLayoutParams);
    }

    //
    // Private methods
    //

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
        mDeleteView.setLayoutParams(deleteLayoutParams);
        mDeleteView.setCornerRadii(getCornerRadii());
        mDeleteView.setOnDeleteProgressListener(() -> deleteItem(getItem()));

        float progress = 0;
        int animationDuration = DESIGN_DELETE_ANIMATION_DURATION;
        if (getItem().getDeleteProgress() > 0) {
            progress = getItem().getDeleteProgress() / 100.0f;
            animationDuration = (int) (BaseItemViewHolder.DESIGN_DELETE_ANIMATION_DURATION - ((getItem().getDeleteProgress() * BaseItemViewHolder.DESIGN_DELETE_ANIMATION_DURATION) / 100.0));
        }

        mDeleteView.startAnimation(animationDuration, progress);
    }

    @Override
    void onViewRecycled() {

        super.onViewRecycled();

        mReplyImageView.setImageBitmap(null, null);
        mDeleteView.setVisibility(View.GONE);
        mDeleteView.setOnDeleteProgressListener(null);
        setDeleteAnimationStarted(false);
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    private LocationItem getLocationItem() {

        return (LocationItem) getItem();
    }

    @Override
    List<View> clickableViews() {

        return new ArrayList<View>() {
            {
                add(getContainer());
                add(mLocationItemContainer);
            }
        };
    }

    private void onCoordinateClick() {

        if (getBaseItemActivity().isSelectItemMode()) {
            onContainerClick();
            return;
        }

        Uri coordinateUri = Uri.parse("geo:" + getLocationItem().getGeolocationDescriptor().getLatitude() + "," + getLocationItem().getGeolocationDescriptor().getLongitude());
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
