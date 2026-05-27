/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  RecyclerView adapter for the GIF grid. Loads animated previews with Glide
 *  (the same image library the app already uses), which decodes and plays GIFs
 *  natively.
 */

package org.twinlife.twinme.ui.gifPickerActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.gif.GifItem;

import java.util.List;

public class GifAdapter extends RecyclerView.Adapter<GifAdapter.GifViewHolder> {

    public interface Listener {
        void onGifClick(@NonNull GifItem item);
    }

    @NonNull
    private final List<GifItem> mItems;
    @NonNull
    private final Listener mListener;

    public GifAdapter(@NonNull List<GifItem> items, @NonNull Listener listener) {
        mItems = items;
        mListener = listener;
    }

    @NonNull
    @Override
    public GifViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gif, parent, false);
        return new GifViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GifViewHolder holder, int position) {
        GifItem item = mItems.get(position);
        holder.bind(item, mListener);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public void onViewRecycled(@NonNull GifViewHolder holder) {
        super.onViewRecycled(holder);
        Glide.with(holder.mImageView).clear(holder.mImageView);
        holder.mImageView.setImageDrawable(null);
    }

    static class GifViewHolder extends RecyclerView.ViewHolder {

        @NonNull
        private final ImageView mImageView;

        GifViewHolder(@NonNull View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.gif_item_image);
        }

        void bind(@NonNull GifItem item, @NonNull Listener listener) {
            mImageView.setContentDescription(item.getDescription() != null ? item.getDescription() : "GIF");
            Glide.with(mImageView)
                    .load(item.getPreviewUrl())
                    .centerCrop()
                    .into(mImageView);
            itemView.setOnClickListener(v -> listener.onGifClick(item));
        }
    }
}
