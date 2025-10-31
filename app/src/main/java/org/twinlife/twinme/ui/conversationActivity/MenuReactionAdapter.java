/*
 *  Copyright (c) 2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.conversationActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;

import java.util.List;

public class MenuReactionAdapter extends RecyclerView.Adapter<ReactionViewHolder> {

    private final AbstractTwinmeActivity mActivity;
    private List<UIReaction> mReactions;
    private final OnReactionClickListener mOnReactionClickListener;

    public interface OnReactionClickListener {

        void onReactionClick(UIReaction uiReaction);
    }

    MenuReactionAdapter(AbstractTwinmeActivity activity, OnReactionClickListener onReactionClickListener, List<UIReaction> reactions) {

        mActivity = activity;
        mOnReactionClickListener = onReactionClickListener;
        mReactions = reactions;
    }

    public void setReactions(List<UIReaction> reactions) {

        mReactions = reactions;
        synchronized (this) {
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public ReactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = mActivity.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.menu_reaction_child, parent, false);

        ReactionViewHolder reactionViewHolder = new ReactionViewHolder(convertView);
        convertView.setOnClickListener(v -> {
            int position = reactionViewHolder.getBindingAdapterPosition();
            if (position >= 0) {
                UIReaction uiReaction = mReactions.get(position);
                mOnReactionClickListener.onReactionClick(uiReaction);
            }
        });
        return reactionViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ReactionViewHolder viewHolder, int position) {

        UIReaction uiReaction = mReactions.get(position);
        viewHolder.onBind(ResourcesCompat.getDrawable(mActivity.getResources(), uiReaction.getImage(), mActivity.getTheme()));
    }

    @Override
    public int getItemCount() {

        return mReactions.size();
    }

    @Override
    public void onViewRecycled(@NonNull ReactionViewHolder viewHolder) {

        viewHolder.onViewRecycled();
    }
}
