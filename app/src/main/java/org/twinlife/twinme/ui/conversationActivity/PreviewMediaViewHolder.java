/*
 *  Copyright (c) 2025 twinlife SA.
 *
 *  All Rights Reserved.
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.conversationActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.FileInfo;
import org.twinlife.twinme.utils.Utils;

import java.util.Arrays;

public class PreviewMediaViewHolder extends RecyclerView.ViewHolder {
    private static final String LOG_TAG = "PreviewMediaViewHolder";
    private static final boolean DEBUG = false;

    private static final float DESIGN_ITEM_VIEW = 120f;
    private static final float DESIGN_CONTAINER_MARGIN = 10f;
    private static final float DESIGN_ICON_SIZE = 50f;

    private final View mOverlayView;
    private final ImageView mTrashView;
    private final ShapeableImageView mThumbnailView;

    public PreviewMediaViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = (int) (DESIGN_ITEM_VIEW * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_ITEM_VIEW * Design.HEIGHT_RATIO);
        view.setLayoutParams(layoutParams);

        view.setBackgroundColor(Color.TRANSPARENT);

        View containerView = view.findViewById(R.id.preview_media_item_container);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) containerView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_CONTAINER_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_CONTAINER_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.topMargin = (int) (DESIGN_CONTAINER_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_CONTAINER_MARGIN * Design.HEIGHT_RATIO);

        mThumbnailView = view.findViewById(R.id.preview_media_item_image);

        mOverlayView = view.findViewById(R.id.preview_media_item_overlay_view);

        mTrashView = view.findViewById(R.id.preview_media_item_trash);
        mTrashView.setVisibility(View.GONE);

        layoutParams = mTrashView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_ICON_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_ICON_SIZE * Design.HEIGHT_RATIO);
    }

    public void onBind(Context context, FileInfo mediaInfo, boolean isCurrentPreview, boolean canDelete) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBind: preview=" + mediaInfo);
        }

        float[] radii = new float[8];
        Arrays.fill(radii, Design.CONTAINER_RADIUS);

        if (mediaInfo.isImage()) {
            BitmapDrawable bitmap = null;
            if (mediaInfo.getPath() != null && mediaInfo.isFile()) {
                bitmap = Utils.getBitmapDrawable(context, mediaInfo.getPath(), (int) (DESIGN_ITEM_VIEW * Design.HEIGHT_RATIO), (int)(DESIGN_ITEM_VIEW * Design.HEIGHT_RATIO));
            }
            if (bitmap != null) {
                mThumbnailView.setImageDrawable(bitmap);
            } else {
                // Try loading the image from the Uri.
                try {
                    Bitmap mediaImage;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                        ImageDecoder.Source source = ImageDecoder.createSource(context.getContentResolver(), mediaInfo.getUri());
                        mediaImage = ImageDecoder.decodeBitmap(source);
                    } else {
                        mediaImage = MediaStore.Images.Media.getBitmap(context.getContentResolver(), mediaInfo.getUri());
                    }

                    bitmap = new BitmapDrawable(context.getResources(), mediaImage);
                    mThumbnailView.setImageBitmap(bitmap.getBitmap());

                } catch (Exception exception) {
                    Log.e(LOG_TAG, "Cannot load bitmap for " + context + ": " + exception);
                } catch (OutOfMemoryError error) {
                    Log.e(LOG_TAG, "Cannot load bitmap for " + context + ": " + error);
                }
            }
        } else if (mediaInfo.isVideo()) {
            if (mediaInfo.getPath() != null) {
                Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(mediaInfo.getPath(), MediaStore.Video.Thumbnails.MINI_KIND);
                if (bitmap != null) {
                    mThumbnailView.setImageBitmap(bitmap);
                }
            } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
                try {
                    MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                    mediaMetadataRetriever.setDataSource(context, mediaInfo.getUri());
                    Bitmap bitmap = mediaMetadataRetriever.getScaledFrameAtTime(1000, MediaMetadataRetriever.OPTION_NEXT_SYNC, 120, 120);
                    if (bitmap != null) {
                        mThumbnailView.setImageBitmap(bitmap);
                    }
                } catch (Exception exception) {
                    Log.e(LOG_TAG, "Cannot load bitmap for " + context + ": " + exception);
                }
            }
        }

        if (isCurrentPreview) {
            if (canDelete) {
                mTrashView.setVisibility(View.VISIBLE);
            } else {
                mTrashView.setVisibility(View.GONE);
            }

            mOverlayView.setVisibility(View.VISIBLE);
        } else {
            mTrashView.setVisibility(View.GONE);
            mOverlayView.setVisibility(View.GONE);
        }
    }

    public void onViewRecycled() {

    }
}