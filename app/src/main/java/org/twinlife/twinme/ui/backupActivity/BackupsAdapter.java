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

import java.util.List;

public class BackupsAdapter extends RecyclerView.Adapter<BackupViewHolder> {
    private static final String LOG_TAG = "BackupsAdapter";
    private static final boolean DEBUG = false;

    private final BackupsActivity mBackupsActivity;

    private List<UIBackupInfo> mBackupInfos;

    public BackupsAdapter(BackupsActivity backupsActivity, List<UIBackupInfo> backupInfos) {

        mBackupsActivity = backupsActivity;
        mBackupInfos = backupInfos;
        setHasStableIds(true);
    }

    public void setBackupInfos(List<UIBackupInfo> backupInfos) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setBackupInfos");
        }

        mBackupInfos = backupInfos;
        notifyItemRangeChanged(0, getItemCount());
    }

    @Override
    public int getItemCount() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemCount");
        }

        return mBackupInfos.size();
    }

    @Override
    public void onBindViewHolder(@NonNull BackupViewHolder backupViewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + backupViewHolder + " position=" + position);
        }

        UIBackupInfo backupInfo = mBackupInfos.get(position);
        backupViewHolder.onBind(backupInfo, position == mBackupInfos.size() - 1);
    }

    @Override
    @NonNull
    public BackupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = (LayoutInflater) mBackupsActivity.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View convertView = inflater.inflate(R.layout.backups_activity_item, parent, false);
        return new BackupViewHolder(convertView);
    }

    @Override
    public void onViewRecycled(@NonNull BackupViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }

    }
}