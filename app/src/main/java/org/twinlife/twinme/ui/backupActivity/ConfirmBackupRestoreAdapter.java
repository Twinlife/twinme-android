/*
 *  Copyright (c) 2025 twinlife SA.
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

public class ConfirmBackupRestoreAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "ConfirmBackup...";
    private static final boolean DEBUG = false;

    private final AbstractTwinmeActivity mActivity;
    private final List<UIBackupContent> mBackupContents;

    public ConfirmBackupRestoreAdapter(AbstractTwinmeActivity activity, List<UIBackupContent> backupContents) {

        mActivity = activity;
        mBackupContents = backupContents;
        setHasStableIds(true);
    }

    @Override
    public int getItemCount() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemCount");
        }

        return mBackupContents.size();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        BackupContentViewHolder backupContentViewHolder = (BackupContentViewHolder) viewHolder;
        if (position == 0) {
            backupContentViewHolder.onBind(mBackupContents.get(0), false);
        } else {
            backupContentViewHolder.onBind(mBackupContents.get(1), false);
        }
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mActivity.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.create_backup_activity_content_item, parent, false);
        return new BackupContentViewHolder(convertView);
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }

    }
}