/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (fabrice.trescartes@twin.life)
 */

package org.twinlife.twinme.ui.conversationActivity.poll;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ConversationService;

import java.util.ArrayList;
import java.util.List;

public class PollChoiceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "PollChoiceAdapter";
    private static final boolean DEBUG = false;

    private final int mTextColor;
    public interface OnChoiceClickListener {
        void onSelectChoice(ConversationService.PollDescriptor.Choice choice);
    }

    private final List<UIPollResult> mResults = new ArrayList<>();

    private OnChoiceClickListener mOnChoiceClickListener;

    public PollChoiceAdapter(int textColor) {

        setHasStableIds(true);
        mTextColor = textColor;
    }

    public void setOnChoiceClickListener(OnChoiceClickListener onChoiceClickListener) {

        mOnChoiceClickListener = onChoiceClickListener;
    }

    public void setChoices(List<UIPollResult> results) {

        mResults.clear();
        mResults.addAll(results);
        notifyItemRangeChanged(0, mResults.size());
    }

    @Override
    public int getItemCount() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemCount");
        }

        return mResults.size();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        UIPollResult pollResult = mResults.get(position);
        PollChoiceViewHolder pollChoiceViewHolder = (PollChoiceViewHolder) viewHolder;
        pollChoiceViewHolder.itemView.setOnClickListener(view -> {
            if (mOnChoiceClickListener != null) {
                mOnChoiceClickListener.onSelectChoice(pollResult.getChoice());
                pollChoiceViewHolder.onBind(pollResult, mTextColor);
            }
        });
        pollChoiceViewHolder.onBind(pollResult, mTextColor);
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View convertView = inflater.inflate(R.layout.poll_choice_item, parent, false);
        return new PollChoiceViewHolder(convertView);
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }
    }
}