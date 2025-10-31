/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.utils;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;

public class DotsAdapter extends RecyclerView.Adapter<DotsViewHolder> {
    private static final String LOG_TAG = "DotsAdapter";
    private static final boolean DEBUG = false;

    private int mCurrentPosition = 0;
    private final int mDotsCount;

    private final LayoutInflater mLayoutInflater;

    public DotsAdapter(int dotsCount, LayoutInflater layoutInflater) {

        mDotsCount = dotsCount;
        mLayoutInflater = layoutInflater;
    }

    public void setCurrentPosition(int currentPosition) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setCurrentPosition: currentPosition=" + currentPosition);
        }

        int oldPosition = mCurrentPosition;
        mCurrentPosition = currentPosition;

        notifyItemChanged(mCurrentPosition);
        notifyItemChanged(oldPosition);
    }

    @NonNull
    @Override
    public DotsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        View convertView = mLayoutInflater.inflate(R.layout.dot_item, parent, false);
        return new DotsViewHolder(convertView);
    }

    @Override
    public void onBindViewHolder(@NonNull DotsViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        viewHolder.onBind(position == mCurrentPosition);
    }

    @Override
    public int getItemCount() {

        return mDotsCount;
    }

    @Override
    public void onViewRecycled(@NonNull DotsViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }
    }
}