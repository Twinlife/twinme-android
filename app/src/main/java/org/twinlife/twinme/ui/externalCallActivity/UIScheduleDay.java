/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.externalCallActivity;

import androidx.annotation.NonNull;

import org.twinlife.twinme.models.schedule.WeeklyTimeRange.DayOfWeek;

public class UIScheduleDay {

    private final String mDay;
    private final DayOfWeek mDayOfWeek;
    private boolean mSelected;

    public UIScheduleDay(@NonNull String day, @NonNull DayOfWeek dayOfWeek, boolean isSelected) {

        mDay = day;
        mDayOfWeek = dayOfWeek;
        mSelected = isSelected;
    }

    @NonNull
    public String getDay() {
        return mDay;
    }

    @NonNull
    public DayOfWeek getDayOfWeek() {

        return mDayOfWeek;
    }

    public boolean isSelected() {

        return mSelected;
    }

    public void setSelected(boolean selected) {

        mSelected = selected;
    }
}