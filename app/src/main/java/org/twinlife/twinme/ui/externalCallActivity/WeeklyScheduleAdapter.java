/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.externalCallActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;

import java.util.ArrayList;
import java.util.List;

public class WeeklyScheduleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "WeeklyScheduleAdapter";
    private static final boolean DEBUG = false;

    public interface OnDayClickListener {
        void onSelectDay(UIScheduleDay scheduleDay);
    }

    private final List<UIScheduleDay> mDays = new ArrayList<>();

    private OnDayClickListener mOnDayClickListener;

    public WeeklyScheduleAdapter() {

        setHasStableIds(true);
    }

    public void setOnDayClickListener(OnDayClickListener onDayClickListener) {

        mOnDayClickListener = onDayClickListener;
    }

    public void setDays(List<UIScheduleDay> days) {

        mDays.clear();
        mDays.addAll(days);
        notifyItemRangeChanged(0, mDays.size());
    }

    @Override
    public int getItemCount() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemCount");
        }

        return mDays.size();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        UIScheduleDay uiScheduleDay = mDays.get(position);
        DayViewHolder dayViewHolder = (DayViewHolder) viewHolder;
        dayViewHolder.itemView.setOnClickListener(view -> {
            if (mOnDayClickListener != null) {
                mOnDayClickListener.onSelectDay(uiScheduleDay);
                dayViewHolder.onBind(uiScheduleDay);
            }
        });
        dayViewHolder.onBind(uiScheduleDay);
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View convertView = inflater.inflate(R.layout.create_external_call_activity_day_item, parent, false);
        return new DayViewHolder(convertView);
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }
    }
}