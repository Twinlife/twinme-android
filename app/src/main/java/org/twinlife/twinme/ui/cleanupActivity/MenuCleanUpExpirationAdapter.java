/*
 *  Copyright (c) 2023-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.cleanupActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;

import java.util.List;

public class MenuCleanUpExpirationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "MenuCleanUpExpira...";
    private static final boolean DEBUG = false;

    public interface OnMenuExpirationClickListener {

        void onSelectDate();

        void onSelectPeriod(UICleanUpExpiration.ExpirationPeriod expirationPeriod);
    }

    private final MenuCleanUpExpirationView mMenuCleanUpExpirationView;

    private final OnMenuExpirationClickListener mOnMenuExpirationClickListener;

    private static final int DATE = 0;
    private static final int PERIOD = 1;

    private final List<UICleanUpExpiration> mExpirationsPeriod;

    private boolean mPeriodSelect = true;

    MenuCleanUpExpirationAdapter(MenuCleanUpExpirationView menuCleanUpExpirationView, List<UICleanUpExpiration> expirationsPeriod, OnMenuExpirationClickListener onMenuExpirationClickListener) {

        mMenuCleanUpExpirationView = menuCleanUpExpirationView;
        mExpirationsPeriod = expirationsPeriod;
        mOnMenuExpirationClickListener = onMenuExpirationClickListener;
        setHasStableIds(true);
    }

    public void setPeriodSelect(boolean periodSelect) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setPeriodSelect");
        }

        mPeriodSelect = periodSelect;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemCount");
        }

        if (!mPeriodSelect) {
            return 1;
        }

        return mExpirationsPeriod.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemViewType: " + position);
        }

        if (!mPeriodSelect) {
            return DATE;
        } else {
            return PERIOD;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        int viewType = getItemViewType(position);

        if (viewType == DATE) {
            ExpirationDateViewHolder expirationDateViewHolder = (ExpirationDateViewHolder) viewHolder;
            expirationDateViewHolder.itemView.setOnClickListener(view -> mOnMenuExpirationClickListener.onSelectDate());
            expirationDateViewHolder.onBind(mMenuCleanUpExpirationView.getCleanupExpiration().getValue(mMenuCleanUpExpirationView.getContext()));
        } else if (viewType == PERIOD) {
            ExpirationPeriodViewHolder expirationPeriodViewHolder = (ExpirationPeriodViewHolder) viewHolder;
            UICleanUpExpiration cleanUpExpiration = mExpirationsPeriod.get(position);
            expirationPeriodViewHolder.itemView.setOnClickListener(view -> mOnMenuExpirationClickListener.onSelectPeriod(cleanUpExpiration.getExpirationPeriod()));
            expirationPeriodViewHolder.onBind(cleanUpExpiration, true, mMenuCleanUpExpirationView.getCleanupExpiration().getExpirationPeriod() == cleanUpExpiration.getExpirationPeriod(), position == mExpirationsPeriod.size() - 1);
        }
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder (@NonNull ViewGroup parent, int viewType){
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mMenuCleanUpExpirationView.getLocalCleanUpActivity().getLayoutInflater();
        View convertView;

        if (viewType == DATE) {
            convertView = inflater.inflate(R.layout.menu_expiration_date_item, parent, false);
            return new ExpirationDateViewHolder(convertView);
        } else {
            convertView = inflater.inflate(R.layout.menu_expiration_period_item, parent, false);
            return new ExpirationPeriodViewHolder(convertView);
        }
    }
}