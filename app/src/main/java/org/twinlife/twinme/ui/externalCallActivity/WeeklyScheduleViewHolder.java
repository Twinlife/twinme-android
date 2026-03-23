/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.externalCallActivity;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

import java.util.List;

public class WeeklyScheduleViewHolder extends RecyclerView.ViewHolder {
    private static final String LOG_TAG = "WeeklySchedule...";
    private static final boolean DEBUG = false;

    private static final float DESIGN_MARGIN = 34;
    private static final float DESIGN_ITEM_VIEW_HEIGHT = 124;
    private static final int ITEM_VIEW_HEIGHT;

    static {
        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
    }

    private final RecyclerView mRecyclerView;
    private final WeeklyScheduleAdapter mWeeklyScheduleAdapter;

    WeeklyScheduleViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = ITEM_VIEW_HEIGHT;
        view.setLayoutParams(layoutParams);

        mWeeklyScheduleAdapter = new WeeklyScheduleAdapter();
        GridLayoutManager gridLayoutManager = new GridLayoutManager(view.getContext(), 7);

        mRecyclerView = view.findViewById(R.id.create_external_call_activity_weekly_schedule_list_view);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.setItemViewCacheSize(Design.ITEM_LIST_CACHE_SIZE);
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setAdapter(mWeeklyScheduleAdapter);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mRecyclerView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_MARGIN * Design.WIDTH_RATIO);
    }

    public void resetMargins() {

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mRecyclerView.getLayoutParams();
        marginLayoutParams.leftMargin = 0;
        marginLayoutParams.rightMargin = 0;
    }

    public void onBind(List<UIScheduleDay> days, WeeklyScheduleAdapter.OnDayClickListener onDayClickListener) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBind");
        }

        mWeeklyScheduleAdapter.setDays(days);
        mWeeklyScheduleAdapter.setOnDayClickListener(onDayClickListener);
    }

    public void onViewRecycled() {

    }
}