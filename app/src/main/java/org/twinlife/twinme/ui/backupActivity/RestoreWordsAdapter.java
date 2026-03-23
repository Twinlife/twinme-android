/*
 *  Copyright (c) 2024-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.backupActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;

import java.util.List;

public class RestoreWordsAdapter extends RecyclerView.Adapter<RestoreWordViewHolder> {
    private static final String LOG_TAG = "RestoreWordsAdapter";
    private static final boolean DEBUG = false;

    private final RestoreActivity mActivity;
    private List<UIBackupWord> mBackupWords;

    public RestoreWordsAdapter(RestoreActivity activity, List<UIBackupWord> backupWords) {

        mActivity = activity;
        mBackupWords = backupWords;
        setHasStableIds(true);
    }

    public void setBackupWords(List<UIBackupWord> backupWords) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setBackupWords: " + backupWords);
        }

        mBackupWords = backupWords;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemCount");
        }

        return mBackupWords.size();
    }

    @Override
    public void onBindViewHolder(@NonNull RestoreWordViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        UIBackupWord backupWord = mBackupWords.get(position);
        viewHolder.itemView.setOnClickListener(view -> mActivity.onWordClick(position));
        viewHolder.onBind(backupWord, position == mActivity.getCurrentWord());
    }

    @Override
    @NonNull
    public RestoreWordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mActivity.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.restore_activity_restore_word_item, parent, false);
        return new RestoreWordViewHolder(convertView);
    }
}