/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.externalCallActivity;

import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.RoundedView;

public class DayViewHolder extends RecyclerView.ViewHolder {
    private static final String LOG_TAG = "DayViewHolder";
    private static final boolean DEBUG = false;

    private static final float DESIGN_PADDING= 4;

    private static final int PADDING;

    static {
        PADDING = (int) (DESIGN_PADDING * Design.HEIGHT_RATIO);
    }

    private final RoundedView mRoundedView;
    private final TextView mDayTextView;

    DayViewHolder(@NonNull View view) {

        super(view);

        View containerView = view.findViewById(R.id.create_external_call_activity_day_item_container_view);
        containerView.setPadding(PADDING, PADDING, PADDING, PADDING);

        mRoundedView = view.findViewById(R.id.create_external_call_activity_day_item_rounded_view);
        mRoundedView.setColor(Design.GREY_ITEM_COLOR);

        mDayTextView = view.findViewById(R.id.create_external_call_activity_day_item_day_view);
        Design.updateTextFont(mDayTextView, Design.FONT_BOLD44);
        mDayTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
    }

    public void onBind(UIScheduleDay uiScheduleDay) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBind: uiScheduleDay=" + uiScheduleDay);
        }

        mDayTextView.setText(uiScheduleDay.getDay());
        mRoundedView.invalidate();
        if (uiScheduleDay.isSelected()) {
            mRoundedView.setColor(Design.getMainStyle());
            mDayTextView.setTextColor(Color.WHITE);
        } else {
            mRoundedView.setColor(Design.GREY_ITEM_COLOR);
            mDayTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        }
    }

    public void onViewRecycled() {

    }
}
