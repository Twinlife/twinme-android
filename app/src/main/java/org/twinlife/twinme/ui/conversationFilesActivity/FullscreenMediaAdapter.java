/*
 *  Copyright (c) 2023-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Romain Kolb (romain.kolb@skyrock.com)
 */

package org.twinlife.twinme.ui.conversationFilesActivity;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.ui.baseItemActivity.Item;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FullscreenMediaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String LOG_TAG = "FullscreenMediaAdapter";
    private static final boolean DEBUG = false;

    private final FullscreenMediaActivity mFullscreenMediaActivity;
    private List<Item> mItems;

    private static final int IMAGE = 0;
    private static final int VIDEO = 1;

    @Nullable
    private FullscreenVideoViewHolder mCurrentFullscreenVideoViewHolder;

    // Keep a reference to the video players to make sure we stop them all on exit.
    @NonNull
    private final Set<FullscreenVideoViewHolder> mFullscreenVideoViewHolders = new HashSet<>();

    FullscreenMediaAdapter(FullscreenMediaActivity fullscreenMediaActivity, List<Item> items) {

        mFullscreenMediaActivity = fullscreenMediaActivity;
        mItems = items;
        setHasStableIds(false);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setItems(List<Item> items) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setItems: " + items);
        }

        mItems = items;
        notifyDataSetChanged();
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
    public long getItemId(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemId: position=" + position);
        }

        Item item = mItems.get(position);
        return item.getItemId();
    }

    @Override
    public int getItemViewType(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemViewType: " + position);
        }

        Item item = mItems.get(position);
        if (item.getType() == Item.ItemType.IMAGE || item.getType() == Item.ItemType.PEER_IMAGE) {
            return IMAGE;
        }

        return VIDEO;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        if (viewType == IMAGE) {
            LayoutInflater inflater = mFullscreenMediaActivity.getLayoutInflater();
            View convertView = inflater.inflate(R.layout.fullscreen_media_activity_image_item, parent, false);
            return new FullscreenImageViewHolder(convertView, mFullscreenMediaActivity);
        } else {
            LayoutInflater inflater = mFullscreenMediaActivity.getLayoutInflater();
            View convertView = inflater.inflate(R.layout.fullscreen_media_activity_video_item, parent, false);
            FullscreenVideoViewHolder fullscreenVideoViewHolder = new FullscreenVideoViewHolder(convertView, mFullscreenMediaActivity);
            mFullscreenVideoViewHolders.add(fullscreenVideoViewHolder);
            return fullscreenVideoViewHolder;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        int viewType = getItemViewType(position);

        if (viewType == IMAGE) {
            FullscreenImageViewHolder fullscreenImageViewHolder = (FullscreenImageViewHolder) viewHolder;
            Item item = mItems.get(position);
            fullscreenImageViewHolder.onBind(item);
        } else {
            FullscreenVideoViewHolder fullscreenVideoViewHolder = (FullscreenVideoViewHolder) viewHolder;
            Item item = mItems.get(position);
            fullscreenVideoViewHolder.onBind(item, mFullscreenMediaActivity, mFullscreenMediaActivity.getCurrentPosition() == position);
        }
    }

    @Override
    public int getItemCount() {

        return mItems.size();
    }

    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder viewHolder) {
        super.onViewAttachedToWindow(viewHolder);

        pausePlayer();

        if (viewHolder.getItemViewType() == VIDEO) {
            FullscreenVideoViewHolder fullscreenVideoViewHolder = (FullscreenVideoViewHolder) viewHolder;
            Item item = mItems.get(fullscreenVideoViewHolder.getBindingAdapterPosition());
            fullscreenVideoViewHolder.onBind(item, mFullscreenMediaActivity, true);
            mCurrentFullscreenVideoViewHolder = fullscreenVideoViewHolder;
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder viewHolder) {
        super.onViewDetachedFromWindow(viewHolder);

        if (viewHolder.getItemViewType() == VIDEO) {
            FullscreenVideoViewHolder fullscreenVideoViewHolder = (FullscreenVideoViewHolder) viewHolder;
            fullscreenVideoViewHolder.stopPlayer();
        } else {
            FullscreenImageViewHolder fullscreenImageViewHolder = (FullscreenImageViewHolder) viewHolder;
            fullscreenImageViewHolder.resetZoom();
        }
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);

        for (FullscreenVideoViewHolder videoViewHolder : mFullscreenVideoViewHolders) {
            videoViewHolder.stopPlayer();
        }
        mFullscreenVideoViewHolders.clear();
    }
}
