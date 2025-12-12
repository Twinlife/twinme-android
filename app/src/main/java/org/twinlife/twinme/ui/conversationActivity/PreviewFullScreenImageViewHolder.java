/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Romain Kolb (romain.kolb@skyrock.com)
 */

package org.twinlife.twinme.ui.conversationActivity;

import android.graphics.Color;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.ui.conversationFilesActivity.ZoomableImageView;
import org.twinlife.twinme.utils.FileInfo;

public class PreviewFullScreenImageViewHolder extends RecyclerView.ViewHolder {
    private static final String LOG_TAG = "PreviewFullScre...";

    private final ZoomableImageView mZoomableImageView;

    PreviewFullScreenImageViewHolder(@NonNull View view, PreviewFileActivity previewFileActivity) {

        super(view);

        view.setBackgroundColor(Color.BLACK);

        mZoomableImageView = view.findViewById(R.id.preview_fullscreen_activity_image_item_image_view);
        mZoomableImageView.setClickable(true);

        ZoomableImageView.OnZoomImageTouchListener onZoomImageTouchListener = new ZoomableImageView.OnZoomImageTouchListener() {
            @Override
            public void onScaleBegin() {
                previewFileActivity.onImageScaleStateChanged(true);
            }

            @Override
            public void onScaleEnd() {
                previewFileActivity.onImageScaleStateChanged(false);
            }

            @Override
            public void onTap() {

            }
        };

        mZoomableImageView.setOnZoomImageTouchListener(onZoomImageTouchListener);
    }

    public void resetZoom() {

        mZoomableImageView.resetZoom();
    }

    public void onBind(FileInfo fileInfo, PreviewFileActivity previewFileActivity) {
        Glide.with(previewFileActivity)
                .load(fileInfo)
                .centerInside()
                .into(mZoomableImageView);
    }

    public void onViewRecycled() {

        mZoomableImageView.setImageBitmap(null);
        mZoomableImageView.resetZoom();

    }
}