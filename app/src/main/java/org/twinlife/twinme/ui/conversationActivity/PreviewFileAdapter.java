/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.conversationActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.utils.FileInfo;

import java.util.List;

public class PreviewFileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String LOG_TAG = "PreviewMediaAdapter";
    private static final boolean DEBUG = false;

    private final PreviewFileActivity mPreviewFileActivity;
    private final List<FileInfo> mFiles;

    private static final int IMAGE = 0;
    private static final int VIDEO = 1;
    private static final int FILE = 2;

    private PreviewFullScreenVideoViewHolder mCurrentFullscreenVideoViewHolder;

    PreviewFileAdapter(PreviewFileActivity previewFileActivity, List<FileInfo> files) {

        mPreviewFileActivity = previewFileActivity;
        mFiles = files;
        setHasStableIds(false);
    }

    public void stopPlayer() {
        if (DEBUG) {
            Log.d(LOG_TAG, "stopPlayer");
        }

        if (mCurrentFullscreenVideoViewHolder != null) {
            mCurrentFullscreenVideoViewHolder.stopPlayer();
        }
    }

    public void pausePlayer() {
        if (DEBUG) {
            Log.d(LOG_TAG, "pausePlayer");
        }

        if (mCurrentFullscreenVideoViewHolder != null) {
            mCurrentFullscreenVideoViewHolder.pausePlayer();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemViewType: " + position);
        }

        FileInfo fileInfo = mFiles.get(position);

        if (fileInfo.isImage()) {
            return IMAGE;
        } else if (fileInfo.isVideo()) {
            return VIDEO;
        }

        return FILE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        if (viewType == IMAGE) {
            LayoutInflater inflater = mPreviewFileActivity.getLayoutInflater();
            View convertView = inflater.inflate(R.layout.preview_fullscreen_image_item, parent, false);
            return new PreviewFullScreenImageViewHolder(convertView, mPreviewFileActivity);
        } else if (viewType == VIDEO) {
            LayoutInflater inflater = mPreviewFileActivity.getLayoutInflater();
            View convertView = inflater.inflate(R.layout.preview_fullscreen_video_item, parent, false);
            return new PreviewFullScreenVideoViewHolder(convertView, mPreviewFileActivity);
        } else {
            LayoutInflater inflater = mPreviewFileActivity.getLayoutInflater();
            View convertView = inflater.inflate(R.layout.preview_file_item, parent, false);
            return new PreviewFileViewHolder(convertView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        int viewType = getItemViewType(position);

        FileInfo fileInfo = mFiles.get(position);
        if (viewType == IMAGE) {
            PreviewFullScreenImageViewHolder previewFullScreenImageViewHolder = (PreviewFullScreenImageViewHolder) viewHolder;
            previewFullScreenImageViewHolder.onBind(fileInfo, mPreviewFileActivity);
        } else if (viewType == VIDEO) {
            PreviewFullScreenVideoViewHolder previewFullScreenVideoViewHolder = (PreviewFullScreenVideoViewHolder) viewHolder;
            previewFullScreenVideoViewHolder.onBind(fileInfo, mPreviewFileActivity, mPreviewFileActivity.getCurrentPosition() == position);
        } else {
            PreviewFileViewHolder previewFileViewHolder = (PreviewFileViewHolder) viewHolder;
            previewFileViewHolder.onBind(mPreviewFileActivity, fileInfo);
        }
    }

    @Override
    public int getItemCount() {

        return mFiles.size();
    }

    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder viewHolder) {
        super.onViewAttachedToWindow(viewHolder);

        if (viewHolder.getItemViewType() == VIDEO) {
            PreviewFullScreenVideoViewHolder previewFullScreenVideoViewHolder = (PreviewFullScreenVideoViewHolder) viewHolder;
            FileInfo fileInfo = (FileInfo) mFiles.get(previewFullScreenVideoViewHolder.getBindingAdapterPosition());
            previewFullScreenVideoViewHolder.onBind(fileInfo, mPreviewFileActivity, true);
            mCurrentFullscreenVideoViewHolder = previewFullScreenVideoViewHolder;
        } else {
            mCurrentFullscreenVideoViewHolder = null;
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder viewHolder) {
        super.onViewDetachedFromWindow(viewHolder);

        if (viewHolder.getItemViewType() == VIDEO) {
            PreviewFullScreenVideoViewHolder previewFullScreenVideoViewHolder = (PreviewFullScreenVideoViewHolder) viewHolder;
            previewFullScreenVideoViewHolder.stopPlayer();
        } else if (viewHolder.getItemViewType() == IMAGE) {
            PreviewFullScreenImageViewHolder previewFullScreenImageViewHolder = (PreviewFullScreenImageViewHolder) viewHolder;
            previewFullScreenImageViewHolder.resetZoom();
        }
    }
}
