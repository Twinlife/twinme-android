/*
 *  Copyright (c) 2016-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Yannis Le Gal (Yannis.LeGal@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Romain Kolb (romain.kolb@skyrock.com)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

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

class PeerImageItemViewHolder extends PeerItemViewHolder  {

    private static final float DESIGN_BOTTOM_VIEW_HEIGHT = 60f;
    private static final float DESIGN_EPHEMERAL_HEIGHT = 28f;
    private static final float DESIGN_EPHEMERAL_LEFT_MARGIN = 4f;
    private static final float DESIGN_EPHEMERAL_TOP_MARGIN = 4f;
    private static final float DESIGN_EPHEMERAL_BOTTOM_MARGIN = 4f;

    private final RoundedImageView mImageView;
    private final View mReplyView;
    private final TextView mReplyTextView;
    private final View mReplyToImageContentView;
    private final RoundedImageView mReplyImageView;
    private final GradientDrawable mReplyGradientDrawable;
    private final GradientDrawable mReplyToImageContentGradientDrawable;
    private final View mBottomView;
    private final GradientDrawable mBottomGradientDrawable;
    private final GradientDrawable mBackgroundGradientDrawable;
    private final TextView mProgressTextView;
    private final ProgressBar mProgressBar;
    private final EphemeralView mEphemeralView;

    private CountDownTimer mTimer;

    PeerImageItemViewHolder(BaseItemActivity baseItemActivity, View view, boolean allowClick, boolean allowLongClick) {

        super(baseItemActivity, view,
                R.id.base_item_activity_peer_image_item_layout_container,
                R.id.base_item_activity_peer_image_item_avatar,
                R.id.base_item_activity_peer_image_item_overlay_view,
                R.id.base_item_activity_peer_image_item_annotation_view,
                R.id.base_item_activity_peer_image_item_selected_view,
                R.id.base_item_activity_peer_image_item_selected_image_view);

        mImageView = view.findViewById(R.id.base_item_activity_peer_image_item_image_view);
        mImageView.setClickable(false);

        mBackgroundGradientDrawable = new GradientDrawable();
        mBackgroundGradientDrawable.mutate();
        mBackgroundGradientDrawable.setShape(GradientDrawable.RECTANGLE);
        mBackgroundGradientDrawable.setCornerRadii(getCornerRadii());
        mImageView.setBackground(mBackgroundGradientDrawable);

        mBottomView = view.findViewById(R.id.base_item_activity_peer_image_item_bottom_view);

        ViewGroup.LayoutParams layoutParams = mBottomView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_BOTTOM_VIEW_HEIGHT * Design.HEIGHT_RATIO);

        mBottomGradientDrawable = new GradientDrawable();
        mBottomGradientDrawable.mutate();
        mBottomGradientDrawable.setColors(new int[]{Design.BOTTOM_GRADIENT_START_COLOR, Design.BOTTOM_GRADIENT_END_COLOR});
        mBottomGradientDrawable.setShape(GradientDrawable.RECTANGLE);
        mBottomView.setBackground(mBottomGradientDrawable);

        mProgressBar = view.findViewById(R.id.base_item_activity_peer_image_item_progress_bar);
        mProgressBar.setProgressTintList(ColorStateList.valueOf(Design.getMainStyle()));
        mProgressBar.setProgressBackgroundTintList(ColorStateList.valueOf(Color.WHITE));

        mProgressTextView = view.findViewById(R.id.base_item_activity_peer_image_item_progress_text);
        Design.updateTextFont(mProgressTextView, Design.FONT_MEDIUM26);
        mProgressTextView.setTextColor(Color.WHITE);

        mReplyTextView = view.findViewById(R.id.base_item_activity_peer_image_item_reply_text);
        mReplyTextView.setPadding(MESSAGE_ITEM_TEXT_WIDTH_PADDING, MESSAGE_ITEM_TEXT_DEFAULT_PADDING, MESSAGE_ITEM_TEXT_WIDTH_PADDING, MESSAGE_ITEM_TEXT_DEFAULT_PADDING);
        mReplyTextView.setTypeface(getMessageFont().typeface);
        mReplyTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getMessageFont().size);
        mReplyTextView.setTextColor(Design.REPLY_FONT_COLOR);
        mReplyTextView.setMaxLines(3);
        mReplyTextView.setEllipsize(TextUtils.TruncateAt.END);

        mReplyView = view.findViewById(R.id.base_item_activity_peer_image_item_reply_view);

        mReplyView.setOnClickListener(v -> onReplyClick());

        mReplyGradientDrawable = new GradientDrawable();
        mReplyGradientDrawable.mutate();
        mReplyGradientDrawable.setColor(Design.REPLY_BACKGROUND_COLOR);
        mReplyGradientDrawable.setShape(GradientDrawable.RECTANGLE);
        mReplyView.setBackground(mReplyGradientDrawable);

        mReplyImageView = view.findViewById(R.id.base_item_activity_peer_image_item_reply_image_view);
        layoutParams = mReplyImageView.getLayoutParams();
        layoutParams.width = REPLY_IMAGE_ITEM_MAX_WIDTH;
        layoutParams.height = REPLY_IMAGE_ITEM_MAX_HEIGHT;

        View replyContainerImageView = view.findViewById(R.id.base_item_activity_peer_image_item_reply_container_image_view);
        replyContainerImageView.setPadding(REPLY_IMAGE_WIDTH_MARGIN, REPLY_IMAGE_HEIGHT_MARGIN, REPLY_IMAGE_WIDTH_MARGIN, REPLY_IMAGE_HEIGHT_MARGIN);

        mReplyToImageContentView = view.findViewById(R.id.base_item_activity_peer_image_item_reply_image_content_view);

        mReplyToImageContentView.setOnClickListener(v -> onReplyClick());

        mReplyToImageContentGradientDrawable = new GradientDrawable();
        mReplyToImageContentGradientDrawable.mutate();
        mReplyToImageContentGradientDrawable.setColor(Design.REPLY_BACKGROUND_COLOR);
        mReplyToImageContentGradientDrawable.setShape(GradientDrawable.RECTANGLE);
        mReplyToImageContentView.setBackground(mReplyToImageContentGradientDrawable);

        mEphemeralView = view.findViewById(R.id.base_item_activity_peer_image_item_ephemeral_view);
        mEphemeralView.setColor(Color.WHITE);

        layoutParams = mEphemeralView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_EPHEMERAL_HEIGHT * Design.HEIGHT_RATIO);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mEphemeralView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_EPHEMERAL_LEFT_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_EPHEMERAL_LEFT_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.topMargin = (int) (DESIGN_EPHEMERAL_TOP_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_EPHEMERAL_BOTTOM_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_EPHEMERAL_LEFT_MARGIN * Design.WIDTH_RATIO));
        marginLayoutParams.setMarginEnd((int) (DESIGN_EPHEMERAL_LEFT_MARGIN * Design.WIDTH_RATIO));

        if (allowClick) {
            mImageView.setOnClickListener(v -> {
                if (getBaseItemActivity().isSelectItemMode()) {
                    onContainerClick();
                    return;
                }

                PeerImageItem peerImageItem = getPeerImageItem();
                if (peerImageItem != null && peerImageItem.isAvailableItem()) {
                    if (peerImageItem.isClearLocalItem()) {
                        Toast.makeText(baseItemActivity, R.string.conversation_activity_local_cleanup, Toast.LENGTH_SHORT).show();
                    } else {
                        baseItemActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                        baseItemActivity.onMediaClick(peerImageItem.getDescriptorId());
                    }
                }
            });
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
    }

    @SuppressLint("DefaultLocale")
    @Override
    void onBind(Item item) {

        if (!(item instanceof PeerImageItem)) {
            return;
        }

        super.onBind(item);

        final PeerImageItem peerImageItem = (PeerImageItem) item;
        final ImageDescriptor imageDescriptor = peerImageItem.getImageDescriptor();

        setImage(mImageView, imageDescriptor);

        // Compute the corner radii only once!
        final float[] cornerRadii = getCornerRadii();

        float[] bottomRadii = getCornerRadii();
        bottomRadii[0] = 0;
        bottomRadii[1] = 0;
        bottomRadii[2] = 0;
        bottomRadii[3] = 0;
        mBottomGradientDrawable.setCornerRadii(bottomRadii);

        if (!item.isAvailableItem()) {
            mBottomView.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressTextView.setVisibility(View.VISIBLE);
            mEphemeralView.setVisibility(View.GONE);
            int progress = (int) (imageDescriptor.getEnd() * 100 / imageDescriptor.getLength());
            mProgressBar.setProgress(progress);
            String progessValue = progress + "%";
            mProgressTextView.setText(progessValue);
        } else {
            mBottomView.setVisibility(View.GONE);
        }

        mReplyGradientDrawable.setCornerRadii(cornerRadii);
        mReplyToImageContentGradientDrawable.setCornerRadii(cornerRadii);
        mBackgroundGradientDrawable.setCornerRadii(cornerRadii);

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
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_peer_image_item_reply_text);

                    ObjectDescriptor objectDescriptor = (ObjectDescriptor) replyToDescriptor;
                    mReplyTextView.setText(objectDescriptor.getMessage());
                    break;

                case IMAGE_DESCRIPTOR:
                    mReplyToImageContentView.setVisibility(View.VISIBLE);
                    mReplyImageView.setVisibility(View.VISIBLE);
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_peer_image_item_reply_container_image_view);

                    setReplyImage(mReplyImageView, (ImageDescriptor) replyToDescriptor);
                    break;

                case VIDEO_DESCRIPTOR:
                    mReplyToImageContentView.setVisibility(View.VISIBLE);
                    mReplyImageView.setVisibility(View.VISIBLE);
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_peer_image_item_reply_container_image_view);

                    setReplyImage(mReplyImageView, (VideoDescriptor) replyToDescriptor);
                    break;

                case AUDIO_DESCRIPTOR:
                    mReplyView.setVisibility(View.VISIBLE);
                    mReplyTextView.setVisibility(View.VISIBLE);
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_peer_image_item_reply_text);

                    mReplyTextView.setText(getString(R.string.conversation_activity_audio_message));
                    break;

                case NAMED_FILE_DESCRIPTOR:
                    mReplyView.setVisibility(View.VISIBLE);
                    mReplyTextView.setVisibility(View.VISIBLE);
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_peer_image_item_reply_text);

                    NamedFileDescriptor fileDescriptor = (NamedFileDescriptor) replyToDescriptor;
                    mReplyTextView.setText(fileDescriptor.getName());
                    break;
            }
        }

        if (mReplyView.getVisibility() == View.VISIBLE) {
            mBackgroundGradientDrawable.setColor(getBaseItemActivity().getCustomAppearance().getPeerMessageBackgroundColor());
        } else {
            mBackgroundGradientDrawable.setColor(Color.TRANSPARENT);
        }

        if (item.isEphemeralItem() && item.isAvailableItem()) {
            mBottomView.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
            mProgressTextView.setVisibility(View.GONE);
            mEphemeralView.setVisibility(View.VISIBLE);
            startEphemeralAnimation();
        }
    }

    @Override
    void onViewRecycled() {

        super.onViewRecycled();

        mImageView.setImageBitmap(null, null);
        mReplyImageView.setImageBitmap(null, null);

        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
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

    //
    // Private methods
    //

    @Nullable
    private PeerImageItem getPeerImageItem() {

        Item item = getItem();
        if (item instanceof PeerImageItem) {

            return (PeerImageItem) item;
        }

        return null;
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
