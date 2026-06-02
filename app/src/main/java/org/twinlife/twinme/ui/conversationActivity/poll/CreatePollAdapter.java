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

public class CreatePollAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "CreatePollAdapter";
    private static final boolean DEBUG = false;

    private final CreatePollActivity mCreatePollActivity;
    private RecyclerView mRecyclerView;

    private int ITEM_COUNT = 2;

    private static final int HEADER = 0;
    private static final int FOOTER = 1;
    private static final int CHOICE = 2;

    CreatePollAdapter(CreatePollActivity createPollActivity) {

        mCreatePollActivity = createPollActivity;
        setHasStableIds(false);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        mRecyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);

        mRecyclerView = null;
    }

    public void updatePollChoices() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updatePollChoices");
        }

        if (mRecyclerView != null) {
            mRecyclerView.post(() -> {
                if (mRecyclerView != null) {
                    notifyItemRangeChanged(1, mCreatePollActivity.getPollChoices().size());
                }
            });
        } else {
            notifyItemRangeChanged(1, mCreatePollActivity.getPollChoices().size());
        }
    }

    public void updateFooter() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateFooter");
        }

        if (mRecyclerView != null) {
            mRecyclerView.post(() -> {
                if (mRecyclerView != null) {
                    notifyItemChanged(ITEM_COUNT - 1);
                }
            });
        } else {
            notifyItemChanged(ITEM_COUNT - 1);
        }
    }

    @Override
    public int getItemCount() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemCount");
        }

        if (mCreatePollActivity.getPollChoices().size() < CreatePollActivity.LIMIT_CHOICE) {
            ITEM_COUNT =  mCreatePollActivity.getPollChoices().size() + 2;
        } else {
            ITEM_COUNT =  mCreatePollActivity.getPollChoices().size() + 1;
        }

        return ITEM_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemViewType: " + position);
        }

        if (position == 0) {
            return HEADER;
        } else if (position == ITEM_COUNT - 1  && mCreatePollActivity.getPollChoices().size() < CreatePollActivity.LIMIT_CHOICE) {
            return FOOTER;
        } else {
            return CHOICE;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        int viewType = getItemViewType(position);

        if (viewType == HEADER) {
            PollHeaderViewHolder headerViewHolder = (PollHeaderViewHolder) viewHolder;
            headerViewHolder.onBind(mCreatePollActivity.getPollQuestion(), mCreatePollActivity.allowMultipleChoice());
        } else if (viewType == CHOICE) {
            PollAddChoiceViewHolder addChoiceViewHolder = (PollAddChoiceViewHolder) viewHolder;
            UIPollChoice pollChoice = mCreatePollActivity.getPollChoices().get(position - 1);
            addChoiceViewHolder.onBind(pollChoice);
        } else if (viewType == FOOTER) {
            PollFooterViewHolder footerViewHolder = (PollFooterViewHolder) viewHolder;
            boolean canAddChoice = mCreatePollActivity.getPollChoices().size() < CreatePollActivity.LIMIT_CHOICE && mCreatePollActivity.countValidChoices() == mCreatePollActivity.getPollChoices().size();
            footerViewHolder.onBind(canAddChoice);
        }
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mCreatePollActivity.getLayoutInflater();
        View convertView;

        if (viewType == HEADER) {
            convertView = inflater.inflate(R.layout.create_poll_header_item, parent, false);
            return new PollHeaderViewHolder(convertView, mCreatePollActivity);
        } else if (viewType == FOOTER) {
            convertView = inflater.inflate(R.layout.create_poll_footer_item, parent, false);
            convertView.setOnClickListener(view -> mCreatePollActivity.addChoice());
            return new PollFooterViewHolder(convertView);
        } else {
            convertView = inflater.inflate(R.layout.create_poll_add_choice_item, parent, false);
            return new PollAddChoiceViewHolder(convertView, mCreatePollActivity);
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {

        if (viewHolder instanceof PollAddChoiceViewHolder) {
            PollAddChoiceViewHolder addChoiceViewHolder = (PollAddChoiceViewHolder) viewHolder;
            addChoiceViewHolder.onViewRecycled();
        }

    }
}
