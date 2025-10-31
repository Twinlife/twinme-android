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

public class PreviewThumbnailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "PreviewThumbnailAdapter";
    private static final boolean DEBUG = false;
    
    private static final int PREVIEW_FILE = 0;
    private static final int ADD_FILE = 2;

    private final PreviewFileActivity mActivity;

    public interface OnPreviewThumbnailListener {
        void onAddFileClick();

        void onSelectFileClick(int position);
    }

    @NonNull
    private final OnPreviewThumbnailListener mOnPreviewThumbnailListener;

    @NonNull
    private final List<FileInfo> mFiles;

    PreviewThumbnailAdapter(PreviewFileActivity activity, @NonNull List<FileInfo> files, @NonNull OnPreviewThumbnailListener onPreviewThumbnailListener) {

        mActivity = activity;
        mFiles = files;
        mOnPreviewThumbnailListener = onPreviewThumbnailListener;
        setHasStableIds(false);
    }

    @Override
    public int getItemCount() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemCount");
        }

        return mFiles.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemViewType: " + position);
        }

        if (position == mFiles.size()) {
            return ADD_FILE;
        } else {
            return PREVIEW_FILE;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mActivity.getLayoutInflater();
        View convertView;

        if (viewType == ADD_FILE) {
            convertView = inflater.inflate(R.layout.preview_add_item, parent, false);
            PreviewAddViewHolder previewAddViewHolder = new PreviewAddViewHolder(convertView);
            convertView.setOnClickListener(v -> {
                mOnPreviewThumbnailListener.onAddFileClick();
            });
            return previewAddViewHolder;
        } else {
            convertView = inflater.inflate(R.layout.preview_media_item, parent, false);
            PreviewThumbnailViewHolder previewThumbnailViewHolder = new PreviewThumbnailViewHolder(convertView);
            convertView.setOnClickListener(v -> {
                int position = previewThumbnailViewHolder.getBindingAdapterPosition();
                if (position >= 0) {
                    mOnPreviewThumbnailListener.onSelectFileClick(position);
                }
            });
            return previewThumbnailViewHolder;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {

        int viewType = getItemViewType(position);

        if (position >= 0) {
            if (viewType == PREVIEW_FILE) {
                FileInfo fileInfo = mFiles.get(position);
                PreviewThumbnailViewHolder previewThumbnailViewHolder = (PreviewThumbnailViewHolder) viewHolder;
                previewThumbnailViewHolder.onBind(mActivity, fileInfo, position == mActivity.getCurrentPosition(), mFiles.size() > 1);
            }
        }
    }
}