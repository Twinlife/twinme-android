/*
 *  Copyright (c) 2022 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.streamingAudioActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.conversationActivity.MusicItem;

import java.util.ArrayList;
import java.util.List;

public class StreamingAudioAdapter  extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "StreamingAudioAdapter";
    private static final boolean DEBUG = false;

    protected static final float DESIGN_ITEM_VIEW_HEIGHT = 252f;
    protected static final int ITEM_VIEW_HEIGHT;

    static {
        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
    }

    public interface OnSongClickListener {

        void onSongClick(int position);
    }

    @NonNull
    private final StreamingAudioActivity mListActivity;
    private List<MusicItem> mMusicItems = new ArrayList<>();
    private MusicItem mMusicItem;

    @NonNull
    private final OnSongClickListener mOnSongClickListener;

    StreamingAudioAdapter(@NonNull StreamingAudioActivity listActivity, @NonNull OnSongClickListener onSongClickListener) {

        mListActivity = listActivity;
        setHasStableIds(false);

        mOnSongClickListener = onSongClickListener;
    }

    void setMusicItems(List<MusicItem> items) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setMusicItems: items=" + items);
        }

        mMusicItems = items;
        notifyDataSetChanged();
    }

    public void setSelectedItem(MusicItem musicItem) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setSelectedItem: musicItem=" + musicItem);
        }

        mMusicItem = musicItem;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "StreamingAudioAdapter.getItemId: position=" + position);
        }

        return mMusicItems.get(position).getItemId();
    }

    @Override
    public int getItemCount() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemCount");
        }

        return mMusicItems.size();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        StreamingMusicViewHolder streamingMusicViewHolder = (StreamingMusicViewHolder) viewHolder;
        streamingMusicViewHolder.itemView.setOnClickListener(view -> mOnSongClickListener.onSongClick(position));
        MusicItem musicItem = mMusicItems.get(position);
        boolean isSelected = musicItem.equals(mMusicItem);
        streamingMusicViewHolder.onBind(musicItem, isSelected);
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mListActivity.getLayoutInflater();
        View convertView= inflater.inflate(R.layout.streaming_music_item, parent, false);
        ViewGroup.LayoutParams layoutParams = convertView.getLayoutParams();
        layoutParams.height = ITEM_VIEW_HEIGHT;
        convertView.setLayoutParams(layoutParams);

        return new StreamingMusicViewHolder(convertView);
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }

        int position = viewHolder.getBindingAdapterPosition();

        if (position != -1) {
            StreamingMusicViewHolder streamingMusicViewHolder = (StreamingMusicViewHolder) viewHolder;
            streamingMusicViewHolder.itemView.setOnClickListener(view -> mOnSongClickListener.onSongClick(position));
            MusicItem musicItem = mMusicItems.get(position);
            boolean isSelected = musicItem.equals(mMusicItem);
            streamingMusicViewHolder.onBind(musicItem,isSelected);
        }
    }
}