/*
 *  Copyright (c) 2023-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Romain Kolb (romain.kolb@skyrock.com)
 */

package org.twinlife.twinme.ui.conversationFilesActivity;

import android.graphics.Color;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinme.glide.Modes;
import org.twinlife.twinme.ui.baseItemActivity.ImageItem;
import org.twinlife.twinme.ui.baseItemActivity.Item;
import org.twinlife.twinme.ui.baseItemActivity.PeerImageItem;

public class FullscreenImageViewHolder extends RecyclerView.ViewHolder {

    private final ZoomableImageView mZoomableImageView;

    FullscreenImageViewHolder(@NonNull View view, @NonNull FullscreenMediaActivity fullscreenMediaActivity) {

        super(view);

        view.setBackgroundColor(Color.BLACK);

        mZoomableImageView = view.findViewById(R.id.fullscreen_media_activity_image_item_image_view);
        mZoomableImageView.setClickable(true);

        ZoomableImageView.OnZoomImageTouchListener onZoomImageTouchListener = new ZoomableImageView.OnZoomImageTouchListener() {
            @Override
            public void onScaleBegin() {
                fullscreenMediaActivity.onImageScaleStateChanged(true);
            }

            @Override
            public void onScaleEnd() {
                fullscreenMediaActivity.onImageScaleStateChanged(mZoomableImageView.getScale() != 1.0f);
            }

            @Override
            public void onTap() {
                fullscreenMediaActivity.onMediaClick();
            }
        };

        mZoomableImageView.setOnZoomImageTouchListener(onZoomImageTouchListener);
        mZoomableImageView.setVisibility(View.VISIBLE);
    }

    public void resetZoom() {

        mZoomableImageView.resetZoom();
    }

    public void onBind(@NonNull Item item) {

        final ConversationService.ImageDescriptor imageDescriptor;
        if (item.isPeerItem()) {
            final PeerImageItem peerImageItem = (PeerImageItem) item;
            imageDescriptor = peerImageItem.getImageDescriptor();
        } else {
            final ImageItem imageItem = (ImageItem) item;
            imageDescriptor = imageItem.getImageDescriptor();
        }

        Glide.with(mZoomableImageView)
                .load(imageDescriptor)
                .thumbnail(Glide.with(mZoomableImageView).load(imageDescriptor).apply(Modes.AS_THUMBNAIL))
                .centerInside()
                .into(mZoomableImageView);

        mZoomableImageView.resetZoom();
    }

    public void onViewRecycled() {

        mZoomableImageView.setImageBitmap(null);
        mZoomableImageView.resetZoom();
    }
}
