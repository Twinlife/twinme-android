/*
 *  Copyright (c) 2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.callActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;

import java.util.List;

public class SelectAudioSourceAdapter extends RecyclerView.Adapter<SelectAudioSourceViewHolder> {

    private final CallActivity mActivity;
    private List<UIAudioSource> mAudioSources;
    private final OnAudioSourceClickListener mOnAudioSourceClickListener;

    public interface OnAudioSourceClickListener {

        void onAudioSourceClick(UIAudioSource audioSource);
    }

    SelectAudioSourceAdapter(CallActivity activity, OnAudioSourceClickListener onAudioSourceClickListener, List<UIAudioSource> audioSources) {

        mActivity = activity;
        mOnAudioSourceClickListener = onAudioSourceClickListener;
        mAudioSources = audioSources;
    }

    public void setAudioSources(List<UIAudioSource> audioSources) {

        mAudioSources = audioSources;
        synchronized (this) {
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public SelectAudioSourceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = mActivity.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.call_activity_select_audio_item_view, parent, false);

        SelectAudioSourceViewHolder selectAudioSourceViewHolder = new SelectAudioSourceViewHolder(convertView);
        convertView.setOnClickListener(v -> {
            int position = selectAudioSourceViewHolder.getBindingAdapterPosition();
            if (position >= 0) {
                UIAudioSource audioSource = mAudioSources.get(position);
                mOnAudioSourceClickListener.onAudioSourceClick(audioSource);
            }
        });
        return selectAudioSourceViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull SelectAudioSourceViewHolder viewHolder, int position) {

        UIAudioSource audioSource = mAudioSources.get(position);
        boolean hideSeparator = position + 1 == mAudioSources.size();
        viewHolder.onBind(audioSource, hideSeparator);
    }

    @Override
    public int getItemCount() {

        return mAudioSources.size();
    }

    @Override
    public void onViewRecycled(@NonNull SelectAudioSourceViewHolder viewHolder) {

        viewHolder.onViewRecycled();
    }
}
