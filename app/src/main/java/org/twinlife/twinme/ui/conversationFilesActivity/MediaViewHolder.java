/*
 *  Copyright (c) 2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Romain Kolb (romain.kolb@skyrock.com)
 */

package org.twinlife.twinme.ui.conversationFilesActivity;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ConversationService.FileDescriptor;
import org.twinlife.twinme.glide.Modes;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.baseItemActivity.ImageItem;
import org.twinlife.twinme.ui.baseItemActivity.Item;
import org.twinlife.twinme.ui.baseItemActivity.PeerImageItem;
import org.twinlife.twinme.ui.baseItemActivity.PeerVideoItem;
import org.twinlife.twinme.ui.baseItemActivity.VideoItem;

public class MediaViewHolder extends RecyclerView.ViewHolder {

    private static final int DESIGN_IMAGE_MARGIN = 6;
    private static final int DESIGN_CHECKBOX_MARGIN = 12;
    private static final int DESIGN_CHECKBOX_HEIGHT = 44;

    private static final int IMAGE_MARGIN;
    private static final int CHECKBOX_MARGIN;
    private static final int CHECKBOX_HEIGHT;

    static {
        IMAGE_MARGIN = (int) (DESIGN_IMAGE_MARGIN * Design.WIDTH_RATIO);
        CHECKBOX_MARGIN = (int) (DESIGN_CHECKBOX_MARGIN * Design.WIDTH_RATIO);
        CHECKBOX_HEIGHT = (int) (DESIGN_CHECKBOX_HEIGHT * Design.HEIGHT_RATIO);
    }

    private final ImageView mImageView;

    private final View mSelectedView;
    private final ImageView mSelectedImageView;

    MediaViewHolder(@NonNull View view) {

        super(view);

        int mediaPerLine = 3;
        if (Design.DISPLAY_WIDTH > 320) {
            mediaPerLine = 4;
        }
        int size = Design.DISPLAY_WIDTH / mediaPerLine;

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = size;
        layoutParams.height = size;
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Color.TRANSPARENT);

        mImageView = view.findViewById(R.id.conversation_files_activity_media_item_image_view);
        mImageView.setClipToOutline(true);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mImageView.getLayoutParams();
        marginLayoutParams.leftMargin = IMAGE_MARGIN;
        marginLayoutParams.rightMargin = IMAGE_MARGIN;
        marginLayoutParams.topMargin = IMAGE_MARGIN;
        marginLayoutParams.bottomMargin = IMAGE_MARGIN;

        mSelectedView = view.findViewById(R.id.conversation_files_activity_media_item_selected_view);

        layoutParams = mSelectedView.getLayoutParams();
        layoutParams.height = CHECKBOX_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mSelectedView.getLayoutParams();
        marginLayoutParams.rightMargin = CHECKBOX_MARGIN;
        marginLayoutParams.bottomMargin = CHECKBOX_MARGIN;

        mSelectedImageView = view.findViewById(R.id.conversation_files_activity_media_item_selected_image_view);
        mSelectedImageView.setColorFilter(Design.getMainStyle());
    }

    public void onBind(Item item, ConversationFilesActivity conversationFilesActivity) {

        mImageView.setImageBitmap(null);

        FileDescriptor descriptor = getFileDescriptor(item);

        Glide.with(conversationFilesActivity)
                .asBitmap()
                .load(descriptor)
                .apply(Modes.AS_THUMBNAIL)
                .into(mImageView);

        if (conversationFilesActivity.isSelectMode()) {
            mSelectedView.setVisibility(View.VISIBLE);

            if (item.isSelected()) {
                mSelectedImageView.setVisibility(View.VISIBLE);
            } else {
                mSelectedImageView.setVisibility(View.INVISIBLE);
            }
        } else {
            mSelectedView.setVisibility(View.INVISIBLE);
        }
    }

    @NonNull
    private static FileDescriptor getFileDescriptor(Item item) {
        FileDescriptor descriptor;

        if (item.getType() == Item.ItemType.IMAGE || item.getType() == Item.ItemType.PEER_IMAGE) {
            if (item.isPeerItem()) {
                final PeerImageItem peerImageItem = (PeerImageItem) item;
                descriptor = peerImageItem.getImageDescriptor();
            } else {
                final ImageItem imageItem = (ImageItem) item;
                descriptor = imageItem.getImageDescriptor();
            }
        } else {
            if (item.isPeerItem()) {
                final PeerVideoItem peerVideoItem = (PeerVideoItem) item;
                descriptor = peerVideoItem.getVideoDescriptor();
            } else {
                final VideoItem videoItem = (VideoItem) item;
                descriptor = videoItem.getVideoDescriptor();
            }
        }
        return descriptor;
    }

    public void onViewRecycled() {

        mImageView.setImageBitmap(null);
    }
}
