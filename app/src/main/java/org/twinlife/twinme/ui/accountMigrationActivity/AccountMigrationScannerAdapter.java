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

import java.util.ArrayList;
import java.util.List;

public class AccountMigrationScannerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "AccountMigrationScannerAdapter";
    private static final boolean DEBUG = false;

    @NonNull
    private final AccountMigrationScannerActivity mAccountMigrationScannerActivity;

    private final List<UIAccountMigrationItem> mItems =  new ArrayList<>();

    AccountMigrationScannerAdapter(@NonNull AccountMigrationScannerActivity accountMigrationScannerActivity) {

        mAccountMigrationScannerActivity = accountMigrationScannerActivity;
        setHasStableIds(false);
        loadItems();
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

        UIAccountMigrationItem item = mItems.get(position);
        AccountMigrationScannerViewHolder accountMigrationScannerViewHolder = (AccountMigrationScannerViewHolder) viewHolder;
        accountMigrationScannerViewHolder.onBind(item);
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mAccountMigrationScannerActivity.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.account_migration_scanner_item, parent, false);
        return new AccountMigrationScannerViewHolder(convertView);
    }

    private void loadItems() {
        if (DEBUG) {
            Log.d(LOG_TAG, "loadItems");
        }

        mItems.clear();

        mItems.add(new UIAccountMigrationItem(1, mAccountMigrationScannerActivity.getString(R.string.account_migration_scanner_view_step_1)));
        mItems.add(new UIAccountMigrationItem(2, mAccountMigrationScannerActivity.getString(R.string.account_migration_scanner_view_step_2)));
        mItems.add(new UIAccountMigrationItem(3, mAccountMigrationScannerActivity.getString(R.string.account_migration_scanner_view_step_3)));

        if (!mAccountMigrationScannerActivity.isFromCurrentDevice()) {
            mItems.add(new UIAccountMigrationItem(4, mAccountMigrationScannerActivity.getString(R.string.account_migration_scanner_view_step_4_my_device)));
            mItems.add(new UIAccountMigrationItem(5, mAccountMigrationScannerActivity.getString(R.string.account_migration_scanner_view_step_5_another_device)));
        } else {
            mItems.add(new UIAccountMigrationItem(4, mAccountMigrationScannerActivity.getString(R.string.account_migration_scanner_view_step_4_another_device)));
            mItems.add(new UIAccountMigrationItem(5, mAccountMigrationScannerActivity.getString(R.string.account_migration_scanner_view_step_5_my_device)));
        }
    }
}
