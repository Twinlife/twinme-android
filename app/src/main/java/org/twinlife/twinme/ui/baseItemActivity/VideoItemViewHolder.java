/*
 *  Copyright (c) 2018-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (fabrice.trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.percentlayout.widget.PercentRelativeLayout;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ConversationService.Descriptor;
import org.twinlife.twinlife.ConversationService.ImageDescriptor;
import org.twinlife.twinlife.ConversationService.NamedFileDescriptor;
import org.twinlife.twinlife.ConversationService.ObjectDescriptor;
import org.twinlife.twinlife.ConversationService.VideoDescriptor;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.EphemeralView;
import org.twinlife.twinme.utils.RoundedImageView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class VideoItemViewHolder extends ItemViewHolder {

    private static final float DESIGN_EPHEMERAL_HEIGHT = 28f;
    private static final float DESIGN_EPHEMERAL_LEFT_MARGIN = 4f;
    private static final float DESIGN_EPHEMERAL_TOP_MARGIN = 4f;
    private static final float DESIGN_EPHEMERAL_BOTTOM_MARGIN = 4f;
    private static final float DESIGN_BOTTOM_VIEW_HEIGHT = 60f;

    private final RoundedImageView mImageView;
    private final ImageView mPlayView;
    private final View mReplyView;
    private final TextView mReplyTextView;
    private final View mReplyToImageContentView;
    private final RoundedImageView mReplyImageView;
    private final GradientDrawable mReplyGradientDrawable;
    private final GradientDrawable mReplyToImageContentGradientDrawable;
    private final DeleteProgressView mDeleteView;
    private final View mBottomView;
    private final GradientDrawable mBottomGradientDrawable;
    private final EphemeralView mEphemeralView;

    private CountDownTimer mTimer;

    VideoItemViewHolder(BaseItemActivity baseItemActivity, View view, boolean allowClick, boolean allowLongClick) {

        super(baseItemActivity, view,
                R.id.base_item_activity_video_item_layout_container,
                R.id.base_item_activity_video_item_state_view,
                R.id.base_item_activity_video_item_state_avatar_view,
                R.id.base_item_activity_video_item_overlay_view,
                R.id.base_item_activity_video_item_annotation_view,
                R.id.base_item_activity_video_item_selected_view,
                R.id.base_item_activity_video_item_selected_image_view);

        mImageView = view.findViewById(R.id.base_item_activity_video_item_image_view);
        mImageView.setClickable(false);

        mPlayView = view.findViewById(R.id.base_item_activity_video_item_play_view);

        mReplyTextView = view.findViewById(R.id.base_item_activity_video_item_reply_text);
        mReplyTextView.setPadding(MESSAGE_ITEM_TEXT_WIDTH_PADDING, MESSAGE_ITEM_TEXT_DEFAULT_PADDING, MESSAGE_ITEM_TEXT_WIDTH_PADDING, MESSAGE_ITEM_TEXT_DEFAULT_PADDING);
        mReplyTextView.setTypeface(getMessageFont().typeface);
        mReplyTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getMessageFont().size);
        mReplyTextView.setTextColor(Design.REPLY_FONT_COLOR);
        mReplyTextView.setMaxLines(3);
        mReplyTextView.setEllipsize(TextUtils.TruncateAt.END);

        mReplyView = view.findViewById(R.id.base_item_activity_video_item_reply_view);

        mReplyView.setOnClickListener(v -> onReplyClick());

        mReplyView.setOnLongClickListener(v -> {
            baseItemActivity.onItemLongPress(getItem());
            return true;
        });

        mReplyGradientDrawable = new GradientDrawable();
        mReplyGradientDrawable.mutate();
        mReplyGradientDrawable.setColor(Design.REPLY_BACKGROUND_COLOR);
        mReplyGradientDrawable.setShape(GradientDrawable.RECTANGLE);
        mReplyView.setBackground(mReplyGradientDrawable);

        mReplyImageView = view.findViewById(R.id.base_item_activity_video_item_reply_image_view);
        ViewGroup.LayoutParams layoutParams = mReplyImageView.getLayoutParams();
        layoutParams.width = REPLY_IMAGE_ITEM_MAX_WIDTH;
        layoutParams.height = REPLY_IMAGE_ITEM_MAX_HEIGHT;

        View replyContainerImageView = view.findViewById(R.id.base_item_activity_video_item_reply_container_image_view);
        replyContainerImageView.setPadding(REPLY_IMAGE_WIDTH_MARGIN, REPLY_IMAGE_HEIGHT_MARGIN, REPLY_IMAGE_WIDTH_MARGIN, REPLY_IMAGE_HEIGHT_MARGIN);

        mReplyToImageContentView = view.findViewById(R.id.base_item_activity_video_item_reply_image_content_view);

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

        mDeleteView = view.findViewById(R.id.base_item_activity_video_item_delete_view);

        if (allowClick) {
            mImageView.setOnClickListener(v -> {

                if (getBaseItemActivity().isSelectItemMode()) {
                    onContainerClick();
                    return;
                }

                VideoItem videoItem = getVideoItem();
                if (videoItem != null) {
                    if (videoItem.isClearLocalItem()) {
                        Toast.makeText(baseItemActivity, R.string.conversation_activity_local_cleanup, Toast.LENGTH_SHORT).show();
                    } else {
                        baseItemActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                        baseItemActivity.onMediaClick(videoItem.getDescriptorId());
                    }
                }
            });
        }

        if (allowLongClick) {
            mImageView.setOnLongClickListener(v -> {
                baseItemActivity.onItemLongPress(getItem());
                return true;
            });
        }

        mBottomView = view.findViewById(R.id.base_item_activity_video_item_bottom_view);

        layoutParams = mBottomView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_BOTTOM_VIEW_HEIGHT * Design.HEIGHT_RATIO);

        mBottomGradientDrawable = new GradientDrawable();
        mBottomGradientDrawable.mutate();
        mBottomGradientDrawable.setColors(new int[]{Design.BOTTOM_GRADIENT_START_COLOR, Design.BOTTOM_GRADIENT_END_COLOR});
        mBottomGradientDrawable.setShape(GradientDrawable.RECTANGLE);
        mBottomView.setBackground(mBottomGradientDrawable);

        mEphemeralView = view.findViewById(R.id.base_item_activity_video_item_ephemeral_view);
        mEphemeralView.setColor(Color.WHITE);

        layoutParams = mEphemeralView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_EPHEMERAL_HEIGHT * Design.HEIGHT_RATIO);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mEphemeralView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_EPHEMERAL_LEFT_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_EPHEMERAL_LEFT_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.topMargin = (int) (DESIGN_EPHEMERAL_TOP_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_EPHEMERAL_BOTTOM_MARGIN * Design.HEIGHT_RATIO);
    }

    @Override
    public void onBind(Item item) {

        if (!(item instanceof VideoItem)) {
            return;
        }

        super.onBind(item);

        final VideoItem videoItem = (VideoItem) item;
        final VideoDescriptor videoDescriptor = videoItem.getVideoDescriptor();

        setImage(mImageView, videoDescriptor);

        // Compute the corner radii only once!
        final float[] cornerRadii = getCornerRadii();

        ViewGroup.MarginLayoutParams playViewLayoutParams = (ViewGroup.MarginLayoutParams) mPlayView.getLayoutParams();
        if (playViewLayoutParams.rightMargin != playViewLayoutParams.bottomMargin) {
            playViewLayoutParams.rightMargin = playViewLayoutParams.bottomMargin;
            mPlayView.setLayoutParams(playViewLayoutParams);
        }

        mReplyGradientDrawable.setCornerRadii(cornerRadii);
        mReplyToImageContentGradientDrawable.setCornerRadii(cornerRadii);

        mReplyView.setVisibility(View.GONE);
        mReplyTextView.setVisibility(View.GONE);
        mReplyToImageContentView.setVisibility(View.GONE);
        mReplyImageView.setVisibility(View.GONE);

        final Descriptor replyToDescriptor = item.getReplyToDescriptor();
        if (replyToDescriptor != null) {

            RelativeLayout.LayoutParams relativeLayoutParams = (RelativeLayout.LayoutParams) mImageView.getLayoutParams();

            switch (replyToDescriptor.getType()) {
                case OBJECT_DESCRIPTOR:
                    mReplyView.setVisibility(View.VISIBLE);
                    mReplyTextView.setVisibility(View.VISIBLE);
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_video_item_reply_text);

                    ObjectDescriptor objectDescriptor = (ObjectDescriptor) replyToDescriptor;
                    mReplyTextView.setText(objectDescriptor.getMessage());
                    break;

                case IMAGE_DESCRIPTOR:
                    mReplyToImageContentView.setVisibility(View.VISIBLE);
                    mReplyImageView.setVisibility(View.VISIBLE);
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_video_item_reply_container_image_view);

                    setReplyImage(mReplyImageView, (ImageDescriptor) replyToDescriptor);
                    break;

                case VIDEO_DESCRIPTOR:
                    mReplyToImageContentView.setVisibility(View.VISIBLE);
                    mReplyImageView.setVisibility(View.VISIBLE);
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_video_item_reply_container_image_view);

                    setReplyImage(mReplyImageView, (VideoDescriptor) replyToDescriptor);
                    break;

                case AUDIO_DESCRIPTOR:
                    mReplyView.setVisibility(View.VISIBLE);
                    mReplyTextView.setVisibility(View.VISIBLE);
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_video_item_reply_text);

                    mReplyTextView.setText(getString(R.string.conversation_activity_audio_message));
                    break;

                case GEOLOCATION_DESCRIPTOR:
                    mReplyView.setVisibility(View.VISIBLE);
                    mReplyTextView.setVisibility(View.VISIBLE);
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_video_item_reply_text);

                    mReplyTextView.setText(getBaseItemActivity().getResources().getString(R.string.application_location));
                    break;

                case NAMED_FILE_DESCRIPTOR:
                    mReplyView.setVisibility(View.VISIBLE);
                    mReplyTextView.setVisibility(View.VISIBLE);
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_video_item_reply_text);

                    NamedFileDescriptor fileDescriptor = (NamedFileDescriptor) replyToDescriptor;
                    mReplyTextView.setText(fileDescriptor.getName());
                    break;
            }
        }

        float[] bottomRadii = getCornerRadii();
        bottomRadii[0] = 0;
        bottomRadii[1] = 0;
        bottomRadii[2] = 0;
        bottomRadii[3] = 0;
        mBottomGradientDrawable.setCornerRadii(bottomRadii);

        if (item.isEphemeralItem()) {
            mBottomView.setVisibility(View.VISIBLE);
            startEphemeralAnimation();
        } else {
            mBottomView.setVisibility(View.GONE);
        }

        ViewGroup.LayoutParams overlayLayoutParams = getOverlayView().getLayoutParams();
        overlayLayoutParams.width = getContainer().getWidth();

        PercentRelativeLayout.LayoutParams layoutParams = (PercentRelativeLayout.LayoutParams) mImageView.getLayoutParams();

        if (getBaseItemActivity().isMenuOpen()) {
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

    @Override
    public void onViewRecycled() {

        super.onViewRecycled();

        mImageView.setImageBitmap(null, null);
        mReplyImageView.setImageBitmap(null, null);
        mDeleteView.setVisibility(View.GONE);
        mDeleteView.setOnDeleteProgressListener(null);
        setDeleteAnimationStarted(false);

        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    //
    // Private methods
    //

    @Nullable
    private VideoItem getVideoItem() {

        Item item = getItem();
        if (item instanceof VideoItem) {

            return (VideoItem) item;
        }

        return null;
    }

    @Override
    List<View> clickableViews() {

        return new ArrayList<View>() {
            {
                add(mImageView);
                add(getContainer());
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
        deleteLayoutParams.width = mImageView.getWidth();
        deleteLayoutParams.height = mImageView.getHeight();
        mDeleteView.setLayoutParams(deleteLayoutParams);
        mDeleteView.setCornerRadii(getCornerRadii());
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
