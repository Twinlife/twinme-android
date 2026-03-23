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
import org.twinlife.twinme.ui.AbstractTwinmeActivity;

import java.util.List;

public class BackupWordsAdapter extends RecyclerView.Adapter<BackupWordViewHolder> {
    private static final String LOG_TAG = "BackupWordsAdapter";
    private static final boolean DEBUG = false;

    private final AbstractTwinmeActivity mActivity;
    private List<UIBackupWord> mBackupWords;

    public BackupWordsAdapter(AbstractTwinmeActivity activity, List<UIBackupWord> backupWords) {

        mActivity = activity;
        mBackupWords = backupWords;
        setHasStableIds(true);
    }

    public void setBackupWords(List<UIBackupWord> backupWords) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setBackupWords: " + backupWords);
        }

        mBackupWords = backupWords;
        notifyItemRangeChanged(0, backupWords.size());
    }

    @Override
    public int getItemCount() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemCount");
        }

        return mBackupWords.size();
    }

    @Override
    public void onBindViewHolder(@NonNull BackupWordViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        UIBackupWord backupWord = mBackupWords.get(position);
        viewHolder.onBind(backupWord);
    }

    @Override
    @NonNull
    public BackupWordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mActivity.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.create_backup_activity_word_item, parent, false);
        return new BackupWordViewHolder(convertView);
    }
}