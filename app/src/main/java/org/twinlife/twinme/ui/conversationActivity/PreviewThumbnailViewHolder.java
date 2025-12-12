/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Romain Kolb (romain.kolb@skyrock.com)
 */

package org.twinlife.twinme.ui.conversationActivity;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.FileInfo;

public class PreviewThumbnailViewHolder extends RecyclerView.ViewHolder {
    private static final String LOG_TAG = "PreviewThumbnailView...";
    private static final boolean DEBUG = false;

    private static final float DESIGN_ITEM_VIEW = 120f;
    private static final float DESIGN_CONTAINER_MARGIN = 10f;
    private static final float DESIGN_ICON_SIZE = 60f;
    private static final float DESIGN_TRASH_SIZE = 50f;

    private final View mOverlayView;
    private final ImageView mIconView;
    private final ImageView mTrashView;
    private final ShapeableImageView mThumbnailView;

    public PreviewThumbnailViewHolder(@NonNull View view) {

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

        mIconView = view.findViewById(R.id.preview_media_item_icon);
        mIconView.setVisibility(View.GONE);

        layoutParams = mIconView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_ICON_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_ICON_SIZE * Design.HEIGHT_RATIO);

        mOverlayView = view.findViewById(R.id.preview_media_item_overlay_view);

        mTrashView = view.findViewById(R.id.preview_media_item_trash);
        mTrashView.setVisibility(View.GONE);

        layoutParams = mTrashView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_TRASH_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_TRASH_SIZE * Design.HEIGHT_RATIO);
    }

    public void onBind(Context context, FileInfo fileInfo, boolean isCurrentPreview, boolean canDelete) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBind: preview=" + fileInfo);
        }

        if (fileInfo.isImage() || fileInfo.isVideo()) {
            mThumbnailView.setBackgroundColor(Color.TRANSPARENT);
            mIconView.setVisibility(View.INVISIBLE);
            Glide.with(context)
                    .asBitmap()
                    .load(fileInfo)
                    .centerInside()
                    .into(mThumbnailView);
        } else {
            mThumbnailView.setBackgroundColor(Color.BLACK);
            mIconView.setVisibility(View.VISIBLE);



            int icon = R.drawable.file_grey;

            String fileName = fileInfo.getFilename();

            if (fileName != null) {
                if (fileName.endsWith(".doc") || fileName.endsWith(".docx")) {
                    icon = R.drawable.file_word;
                } else if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx")) {
                    icon = R.drawable.file_excel;
                } else if (fileName.endsWith(".ppt") || fileName.endsWith(".pptx")) {
                    icon = R.drawable.file_powerpoint;
                } else if (fileName.endsWith(".pdf")) {
                    icon = R.drawable.file_pdf;
                }
            }
            mIconView.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), icon, context.getTheme()));
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

    public void onBind(Context context, UIPreviewFile previewFile, boolean isCurrentPreview, boolean canDelete) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBind: preview=" + previewFile);
        }

        mThumbnailView.setBackgroundColor(Color.BLACK);
        mIconView.setVisibility(View.VISIBLE);
        mIconView.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), previewFile.getIcon(), context.getTheme()));

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