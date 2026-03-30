/*
 *  Copyright (c) 2019-2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.calls;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;

import java.util.List;

public class LastCallsAdapter extends RecyclerView.Adapter<LastCallViewHolder> {
    private static final String LOG_TAG = "LastCallsAdapter";
    private static final boolean DEBUG = false;

    @NonNull
    private final OnLastCallClickListener mOnCallClickListener;

    @NonNull
    private final AbstractTwinmeActivity mListActivity;

    @NonNull
    private final List<UICall> mUICalls;

    public interface OnLastCallClickListener {

        void onCallClick(int position);
    }

    public LastCallsAdapter(@NonNull AbstractTwinmeActivity listActivity, @NonNull List<UICall> uiCalls, @NonNull OnLastCallClickListener onCallClickListener) {

        mOnCallClickListener = onCallClickListener;
        mListActivity = listActivity;
        mUICalls = uiCalls;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public LastCallViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mListActivity.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.last_calls_activity_item, parent, false);

        LastCallViewHolder callViewHolder = new LastCallViewHolder(convertView);
        convertView.setOnClickListener(v -> {
            int position = callViewHolder.getBindingAdapterPosition();
            if (position >= 0) {
                mOnCallClickListener.onCallClick(position);
            }
        });
        return callViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull LastCallViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        boolean hideSeparator = position + 1 == mUICalls.size();
        viewHolder.onBind(mListActivity, mUICalls.get(position), hideSeparator);
    }

    @Override
    public int getItemCount() {

        return mUICalls.size();
    }

    @Override
    public long getItemId(int position) {

        return mUICalls.get(position).getItemId();
    }

    @Override
    public void onViewRecycled(@NonNull LastCallViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }

        viewHolder.onViewRecycled();
    }
}
