/*
 *  Copyright (c) 2022 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;

public class PreviewItemListAdapter extends RecyclerView.Adapter<BaseItemViewHolder> {
    private static final String LOG_TAG = "PreviewItemListAdapter";
    private static final boolean DEBUG = false;

    private final BaseItemActivity mBaseItemActivity;
    private final Item mItem;

    public PreviewItemListAdapter(BaseItemActivity baseItemActivity, Item item) {

        mBaseItemActivity = baseItemActivity;
        mItem = item;
        setHasStableIds(true);
    }

    @Override
    public int getItemCount() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemCount");
        }

        return 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemViewType: position=" + position);
        }

        return mItem.getType().ordinal();
    }

    @Override
    public long getItemId(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemId: position=" + position);
        }

        return mItem.getItemId();
    }

    @Override
    public void onBindViewHolder(@NonNull BaseItemViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        viewHolder.onBind(mItem);
    }

    @Override
    @NonNull
    public BaseItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mBaseItemActivity.getLayoutInflater();
        View convertView;
        switch (Item.ItemType.values()[viewType]) {
            case MESSAGE:
                convertView = inflater.inflate(R.layout.base_item_activity_message_item, parent, false);

                return new MessageItemViewHolder(mBaseItemActivity, convertView);

            case PEER_MESSAGE:
                convertView = inflater.inflate(R.layout.base_item_activity_peer_message_item, parent, false);

                return new PeerMessageItemViewHolder(mBaseItemActivity, convertView);

            case IMAGE:
                convertView = inflater.inflate(R.layout.base_item_activity_image_item, parent, false);

                return new ImageItemViewHolder(mBaseItemActivity, convertView, false, false);

            case PEER_IMAGE:
                convertView = inflater.inflate(R.layout.base_item_activity_peer_image_item, parent, false);

                return new PeerImageItemViewHolder(mBaseItemActivity, convertView, false, false);

            case AUDIO:
                convertView = inflater.inflate(R.layout.base_item_activity_audio_item, parent, false);

                return new AudioItemViewHolder(mBaseItemActivity, convertView, null);

            case PEER_AUDIO:
                convertView = inflater.inflate(R.layout.base_item_activity_peer_audio_item, parent, false);

                return new PeerAudioItemViewHolder(mBaseItemActivity, convertView, null);

            case VIDEO:
                convertView = inflater.inflate(R.layout.base_item_activity_video_item, parent, false);

                return new VideoItemViewHolder(mBaseItemActivity, convertView, false, false);

            case PEER_VIDEO:
                convertView = inflater.inflate(R.layout.base_item_activity_peer_video_item, parent, false);

                return new PeerVideoItemViewHolder(mBaseItemActivity, convertView, false, false);

            case FILE:
                convertView = inflater.inflate(R.layout.base_item_activity_file_item, parent, false);
                return new FileItemViewHolder(mBaseItemActivity, convertView, false, false);

            case PEER_FILE:
                convertView = inflater.inflate(R.layout.base_item_activity_peer_file_item, parent, false);
                return new PeerFileItemViewHolder(mBaseItemActivity, convertView, false, false);

            default:
                convertView = inflater.inflate(R.layout.base_item_activity_item_default, parent, false);

                return new DefaultItemViewHolder(mBaseItemActivity, convertView);
        }
    }

    @Override
    public void onViewAttachedToWindow(@NonNull BaseItemViewHolder viewHolder) {

        super.onViewAttachedToWindow(viewHolder);

        viewHolder.onViewAttachedToWindow();
    }

    @Override
    public void onViewRecycled(@NonNull BaseItemViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }

        viewHolder.onViewRecycled();
    }
}