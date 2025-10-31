/*
 *  Copyright (c) 2018-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Yannis Le Gal (Yannis.LeGal@twin.life)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ConversationService.Descriptor;
import org.twinlife.twinlife.ConversationService.ImageDescriptor;
import org.twinlife.twinlife.ConversationService.NamedFileDescriptor;
import org.twinlife.twinlife.ConversationService.ObjectDescriptor;
import org.twinlife.twinlife.ConversationService.VideoDescriptor;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.conversationActivity.NamedFileProvider;
import org.twinlife.twinme.utils.EphemeralView;
import org.twinlife.twinme.utils.RoundedImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class PeerFileItemViewHolder extends PeerItemViewHolder {

    private static final float DESIGN_EPHEMERAL_HEIGHT = 28f;
    private static final float DESIGN_EPHEMERAL_LEFT_MARGIN = 4f;
    private static final float DESIGN_EPHEMERAL_TOP_MARGIN = 4f;
    private static final float DESIGN_EPHEMERAL_BOTTOM_MARGIN = 4f;
    private static final float DESIGN_PROGRESS_VIEW_HEIGHT = 60f;

    private final View mFileItemContainer;
    private final TextView mFilenameView;
    private final GradientDrawable mGradientDrawable;
    private final ImageView mIconView;
    private final TextView mFileSizeView;
    private final View mReplyView;
    private final TextView mReplyTextView;
    private final View mReplyToImageContentView;
    private final RoundedImageView mReplyImageView;
    private final GradientDrawable mReplyGradientDrawable;
    private final GradientDrawable mReplyToImageContentGradientDrawable;
    private final View mProgressView;
    private final TextView mProgressTextView;
    private final ProgressBar mProgressBar;
    private final EphemeralView mEphemeralView;

    private CountDownTimer mTimer;

    private String mFilePath;

    PeerFileItemViewHolder(BaseItemActivity baseItemActivity, View view, boolean allowClick, boolean allowLongClick) {

        super(baseItemActivity, view,
                R.id.base_item_activity_peer_file_item_container,
                R.id.base_item_activity_peer_file_item_avatar,
                R.id.base_item_activity_peer_file_item_overlay_view,
                R.id.base_item_activity_peer_file_item_annotation_view,
                R.id.base_item_activity_peer_file_item_selected_view,
                R.id.base_item_activity_peer_file_item_selected_image_view);

        mFileItemContainer = view.findViewById(R.id.base_item_activity_peer_file_item_view);
        mFileItemContainer.setPadding(FILE_ITEM_WIDTH_PADDING, FILE_ITEM_HEIGHT_PADDING, FILE_ITEM_WIDTH_PADDING, FILE_ITEM_HEIGHT_PADDING);
        mGradientDrawable = new GradientDrawable();
        mGradientDrawable.mutate();
        mGradientDrawable.setColor(Design.GREY_ITEM_COLOR);
        mGradientDrawable.setShape(GradientDrawable.RECTANGLE);
        mFileItemContainer.setBackground(mGradientDrawable);
        mFileItemContainer.setClickable(false);

        mFilenameView = view.findViewById(R.id.base_item_activity_peer_file_name_view);
        Design.updateTextFont(mFilenameView, Design.FONT_REGULAR32);
        mFilenameView.setTextColor(getBaseItemActivity().getCustomAppearance().getPeerMessageTextColor());

        mFileSizeView = view.findViewById(R.id.base_item_activity_peer_file_size_view);
        Design.updateTextFont(mFileSizeView, Design.FONT_REGULAR28);
        mFileSizeView.setTextColor(getBaseItemActivity().getCustomAppearance().getPeerMessageTextColor());

        mIconView = view.findViewById(R.id.base_item_activity_peer_file_item_image_view);

        mProgressView = view.findViewById(R.id.base_item_activity_peer_file_progress_view);

        ViewGroup.LayoutParams layoutParams = mProgressView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_PROGRESS_VIEW_HEIGHT * Design.HEIGHT_RATIO);

        mProgressBar = view.findViewById(R.id.base_item_activity_peer_file_item_progress_bar);
        mProgressBar.setProgressTintList(ColorStateList.valueOf(Design.getMainStyle()));
        mProgressBar.setProgressBackgroundTintList(ColorStateList.valueOf(Color.WHITE));

        mProgressTextView = view.findViewById(R.id.base_item_activity_peer_file_item_progress_text);
        Design.updateTextFont(mProgressTextView, Design.FONT_MEDIUM26);
        mProgressTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mReplyTextView = view.findViewById(R.id.base_item_activity_peer_file_item_reply_text);
        mReplyTextView.setPadding(MESSAGE_ITEM_TEXT_WIDTH_PADDING, MESSAGE_ITEM_TEXT_DEFAULT_PADDING, MESSAGE_ITEM_TEXT_WIDTH_PADDING, MESSAGE_ITEM_TEXT_DEFAULT_PADDING);
        mReplyTextView.setTypeface(getMessageFont().typeface);
        mReplyTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getMessageFont().size);
        mReplyTextView.setTextColor(Design.REPLY_FONT_COLOR);
        mReplyTextView.setMaxLines(3);
        mReplyTextView.setEllipsize(TextUtils.TruncateAt.END);

        mReplyView = view.findViewById(R.id.base_item_activity_peer_file_item_reply_view);

        mReplyView.setOnClickListener(v -> onReplyClick());

        mReplyGradientDrawable = new GradientDrawable();
        mReplyGradientDrawable.mutate();
        mReplyGradientDrawable.setColor(Design.REPLY_BACKGROUND_COLOR);
        mReplyGradientDrawable.setShape(GradientDrawable.RECTANGLE);
        mReplyView.setBackground(mReplyGradientDrawable);

        mReplyImageView = view.findViewById(R.id.base_item_activity_peer_file_item_reply_image_view);
        layoutParams = mReplyImageView.getLayoutParams();
        layoutParams.width = REPLY_IMAGE_ITEM_MAX_WIDTH;
        layoutParams.height = REPLY_IMAGE_ITEM_MAX_HEIGHT;

        View replyContainerImageView = view.findViewById(R.id.base_item_activity_peer_file_item_reply_container_image_view);
        replyContainerImageView.setPadding(REPLY_IMAGE_WIDTH_MARGIN, REPLY_IMAGE_HEIGHT_MARGIN, REPLY_IMAGE_WIDTH_MARGIN, REPLY_IMAGE_HEIGHT_MARGIN);

        mReplyToImageContentView = view.findViewById(R.id.base_item_activity_peer_file_item_reply_image_content_view);

        mReplyToImageContentView.setOnClickListener(v -> onReplyClick());

        mReplyToImageContentGradientDrawable = new GradientDrawable();
        mReplyToImageContentGradientDrawable.mutate();
        mReplyToImageContentGradientDrawable.setColor(Design.REPLY_BACKGROUND_COLOR);
        mReplyToImageContentGradientDrawable.setShape(GradientDrawable.RECTANGLE);
        mReplyToImageContentView.setBackground(mReplyToImageContentGradientDrawable);

        mEphemeralView = view.findViewById(R.id.base_item_activity_peer_file_item_ephemeral_view);
        mEphemeralView.setColor(Design.BLACK_COLOR);

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
            mFileItemContainer.setOnClickListener(v -> {

                if (getBaseItemActivity().isSelectItemMode()) {
                    onContainerClick();
                    return;
                }

                if (getPeerFileItem().isAvailableItem()) {
                    try {
                        if (getPeerFileItem().needsUpdateReadTimestamp()) {
                            baseItemActivity.markDescriptorRead(getPeerFileItem().getDescriptorId());
                        }
                        Uri uri = NamedFileProvider.getInstance().getUriForFile(baseItemActivity, new File(mFilePath), getPeerFileItem().getNamedFileDescriptor().getName());
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        baseItemActivity.startActivity(intent);

                    } catch (Exception exception) {
                        Log.e("FileItemViewHolder", "FileItemViewHolder() exception=" + exception);
                    }
                }
            });
        }
        if (allowLongClick) {
            mFileItemContainer.setOnLongClickListener(v -> {
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

    @Override
    void onBind(Item item) {

        if (!(item instanceof PeerFileItem)) {
            return;
        }
        super.onBind(item);

        // Compute the corner radii only once!
        final float[] cornerRadii = getCornerRadii();

        mGradientDrawable.setCornerRadii(cornerRadii);
        mGradientDrawable.setColor(getBaseItemActivity().getCustomAppearance().getPeerMessageBackgroundColor());
        if (getBaseItemActivity().getCustomAppearance().getPeerMessageBorderColor() != Color.TRANSPARENT) {
            mGradientDrawable.setStroke(Design.BORDER_WIDTH, getBaseItemActivity().getCustomAppearance().getPeerMessageBorderColor());
        }

        final PeerFileItem peerFileItem = (PeerFileItem) item;
        NamedFileDescriptor namedFileDescriptor = peerFileItem.getNamedFileDescriptor();
        mFilenameView.setText(namedFileDescriptor.getName());

        mFilePath = getPath(namedFileDescriptor);

        mFileSizeView.setText(Formatter.formatFileSize(getBaseItemActivity(), namedFileDescriptor.getLength()));

        mIconView.setImageResource(FileItem.getFileIcon(mFilePath));

        if (!item.isAvailableItem()) {
            mProgressView.setVisibility(View.VISIBLE);
            mFileSizeView.setVisibility(View.GONE);
            int progress = (int) (namedFileDescriptor.getEnd() * 100 / namedFileDescriptor.getLength());
            mProgressBar.setProgress(progress);
            String progessValue = progress + "%";
            mProgressTextView.setText(progessValue);
        } else {
            mProgressView.setVisibility(View.GONE);
            mFileSizeView.setVisibility(View.VISIBLE);
        }

        mReplyGradientDrawable.setCornerRadii(cornerRadii);
        mReplyToImageContentGradientDrawable.setCornerRadii(cornerRadii);

        mReplyView.setVisibility(View.GONE);
        mReplyTextView.setVisibility(View.GONE);
        mReplyToImageContentView.setVisibility(View.GONE);
        mReplyImageView.setVisibility(View.GONE);

        final Descriptor replyToDescriptor = item.getReplyToDescriptor();
        if (replyToDescriptor != null) {

            RelativeLayout.LayoutParams relativeLayoutParams = (RelativeLayout.LayoutParams) mFileItemContainer.getLayoutParams();

            switch (replyToDescriptor.getType()) {
                case OBJECT_DESCRIPTOR:
                    mReplyView.setVisibility(View.VISIBLE);
                    mReplyTextView.setVisibility(View.VISIBLE);
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_peer_file_item_reply_text);

                    ObjectDescriptor objectDescriptor = (ObjectDescriptor) replyToDescriptor;
                    mReplyTextView.setText(objectDescriptor.getMessage());
                    break;

                case IMAGE_DESCRIPTOR:
                    mReplyToImageContentView.setVisibility(View.VISIBLE);
                    mReplyImageView.setVisibility(View.VISIBLE);
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_peer_file_item_reply_container_image_view);

                    setReplyImage(mReplyImageView, (ImageDescriptor) replyToDescriptor);
                    break;

                case VIDEO_DESCRIPTOR:
                    mReplyToImageContentView.setVisibility(View.VISIBLE);
                    mReplyImageView.setVisibility(View.VISIBLE);
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_peer_file_item_reply_container_image_view);

                    setReplyImage(mReplyImageView, (VideoDescriptor) replyToDescriptor);
                    break;

                case AUDIO_DESCRIPTOR:
                    mReplyView.setVisibility(View.VISIBLE);
                    mReplyTextView.setVisibility(View.VISIBLE);
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_peer_file_item_reply_text);

                    mReplyTextView.setText(getString(R.string.conversation_activity_audio_message));
                    break;

                case NAMED_FILE_DESCRIPTOR:
                    mReplyView.setVisibility(View.VISIBLE);
                    mReplyTextView.setVisibility(View.VISIBLE);
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_peer_file_item_reply_text);

                    NamedFileDescriptor fileDescriptor = (NamedFileDescriptor) replyToDescriptor;
                    mReplyTextView.setText(fileDescriptor.getName());
                    break;
            }
        }

        if (item.isEphemeralItem() && item.isAvailableItem()) {
            mEphemeralView.setVisibility(View.VISIBLE);
            startEphemeralAnimation();
        } else {
            mEphemeralView.setVisibility(View.GONE);
        }
    }

    @Override
    List<View> clickableViews() {

        return new ArrayList<View>() {
            {
                add(mFileItemContainer);
                add(getContainer());
            }
        };
    }

    @Override
    void onViewRecycled() {

        super.onViewRecycled();

        mReplyImageView.setImageBitmap(null, null);

        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    //
    // Private methods
    //

    private PeerFileItem getPeerFileItem() {

        return (PeerFileItem) getItem();
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
