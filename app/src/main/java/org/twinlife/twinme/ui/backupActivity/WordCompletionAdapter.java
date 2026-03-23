/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.backupActivity;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

import java.util.ArrayList;
import java.util.List;

public class WordCompletionAdapter extends RecyclerView.Adapter<WordCompletionViewHolder> {
    private static final String LOG_TAG = "WordCompletionAdapter";
    private static final boolean DEBUG = false;

    public interface OnWordCompletionClickListener {

        void onSelectWordClick(String word);
    }

    private final WordCompletionView mWordCompletionView;
    private final OnWordCompletionClickListener mOnWordCompletionClickListener;

    private List<String> mWordsSuggestions = new ArrayList<>();

    public WordCompletionAdapter(WordCompletionView wordCompletionView, OnWordCompletionClickListener onWordCompletionClickListener) {

        mWordCompletionView = wordCompletionView;
        mOnWordCompletionClickListener = onWordCompletionClickListener;

        setHasStableIds(true);
    }

    public void setWordsSuggestions(List<String> wordsSuggestions) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setWordsSuggestions: " + wordsSuggestions);
        }

        mWordsSuggestions = wordsSuggestions;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemCount");
        }

        return mWordsSuggestions.size();
    }

    @Override
    public void onBindViewHolder(@NonNull WordCompletionViewHolder wordCompletionViewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + wordCompletionViewHolder + " position=" + position);
        }

        String word = mWordsSuggestions.get(position);

        int backgroundColor = position % 2 == 0 ? Design.WHITE_COLOR : Design.LIGHT_GREY_BACKGROUND_COLOR;
        wordCompletionViewHolder.itemView.setOnClickListener(view -> mOnWordCompletionClickListener.onSelectWordClick(word));
        wordCompletionViewHolder.onBind(word, backgroundColor);
    }

    @Override
    @NonNull
    public WordCompletionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = (LayoutInflater) mWordCompletionView.getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View convertView = inflater.inflate(R.layout.word_completion_item, parent, false);
        return new WordCompletionViewHolder(convertView);
    }

    @Override
    public void onViewRecycled(@NonNull WordCompletionViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }

    }
}