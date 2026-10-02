/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.accountMigrationActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.AccountMigrationService;
import org.twinlife.twinme.models.AccountMigration;

import java.util.ArrayList;
import java.util.List;

public class AccountMigrationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "AccountMigrationAdapter";
    private static final boolean DEBUG = false;

    @NonNull
    private final AccountMigrationActivity mAccountMigrationActivity;

    private final List<UIMigrationStateItem> mItems =  new ArrayList<>();

    private static final int HEADER = 0;
    private static final int STATE = 1;

    AccountMigrationAdapter(@NonNull AccountMigrationActivity accountMigrationActivity) {

        mAccountMigrationActivity = accountMigrationActivity;
        setHasStableIds(false);
        loadItems();
    }

    public void updateItems(AccountMigrationService.State state, AccountMigrationService.Status status) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateItems: " + state + " status: " + status);
        }

        for (UIMigrationStateItem migrationStateItem : mItems) {
            migrationStateItem.update(mAccountMigrationActivity, state, status);
        }

        notifyItemRangeChanged(0, mItems.size());
    }

    @Override
    public int getItemViewType(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemViewType: " + position);
        }

        UIMigrationStateItem item = mItems.get(position);
        if (item.getType() == UIMigrationStateItem.MigrationStateItemType.HEADER) {
            return HEADER;
        }
        return STATE;
    }

    @Override
    public int getItemCount() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemCount");
        }

        return mItems.size();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        UIMigrationStateItem item = mItems.get(position);
        if (item.getType() == UIMigrationStateItem.MigrationStateItemType.HEADER && viewHolder instanceof MigrationHeaderViewHolder) {
            MigrationHeaderViewHolder migrationHeaderViewHolder = (MigrationHeaderViewHolder) viewHolder;
            migrationHeaderViewHolder.onBind(item);
        } else if (viewHolder instanceof MigrationStateViewHolder) {
            MigrationStateViewHolder migrationStateViewHolder = (MigrationStateViewHolder) viewHolder;

            boolean previousStateDone = false;
            if (position > 0) {
                UIMigrationStateItem prevItem = mItems.get(position - 1);
                previousStateDone = prevItem.getState() == UIMigrationStateItem.MigrationStateItemState.DONE;
            }

            migrationStateViewHolder.onBind(item, previousStateDone);
        }
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mAccountMigrationActivity.getLayoutInflater();

        View convertView;
        if (viewType == HEADER) {
            convertView = inflater.inflate(R.layout.account_migration_header_item, parent, false);
            return new MigrationHeaderViewHolder(convertView);
        } else {
            convertView = inflater.inflate(R.layout.account_migration_state_item, parent, false);
            return new MigrationStateViewHolder(convertView);
        }
    }

    private void loadItems() {
        if (DEBUG) {
            Log.d(LOG_TAG, "loadItems");
        }

        mItems.clear();
        mItems.add(new UIMigrationStateItem(mAccountMigrationActivity, UIMigrationStateItem.MigrationStateItemType.INIT));
        mItems.add(new UIMigrationStateItem(mAccountMigrationActivity, UIMigrationStateItem.MigrationStateItemType.TRANSFER));
        mItems.add(new UIMigrationStateItem(mAccountMigrationActivity, UIMigrationStateItem.MigrationStateItemType.SETTINGS));
        mItems.add(new UIMigrationStateItem(mAccountMigrationActivity, UIMigrationStateItem.MigrationStateItemType.DATABASE));
        mItems.add(new UIMigrationStateItem(mAccountMigrationActivity, UIMigrationStateItem.MigrationStateItemType.ACCOUNT));
        mItems.add(new UIMigrationStateItem(mAccountMigrationActivity, UIMigrationStateItem.MigrationStateItemType.TERMINATED));
    }
}
