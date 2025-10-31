/*
 *  Copyright (c) 2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.contacts;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.models.schedule.Date;
import org.twinlife.twinme.models.schedule.DateTime;
import org.twinlife.twinme.models.schedule.Time;
import org.twinlife.twinme.skin.Design;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class ScheduleViewHolder extends RecyclerView.ViewHolder {
    protected static final float DESIGN_ITEM_VIEW_HEIGHT = 120f;
    protected static final int ITEM_VIEW_HEIGHT;

    static {
        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
    }

    private final TextView mTitleView;
    private final TextView mDateTextView;
    private final TextView mTimeTextView;

    private AbstractCapabilitiesActivity.ScheduleType mScheduleType;

    public ScheduleViewHolder(@NonNull View view, AbstractCapabilitiesActivity activity) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = ITEM_VIEW_HEIGHT;
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Design.WHITE_COLOR);

        mTitleView = view.findViewById(R.id.contact_capabilities_activity_schedule_item_title_view);
        Design.updateTextFont(mTitleView, Design.FONT_REGULAR34);
        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        View dateView = view.findViewById(R.id.contact_capabilities_activity_schedule_item_date_view);
        dateView.setOnClickListener(v -> activity.onDateClick(mScheduleType));

        layoutParams = dateView.getLayoutParams();
        layoutParams.width = Design.DATE_VIEW_WIDTH;

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) dateView.getLayoutParams();
        marginLayoutParams.rightMargin = Design.DATE_VIEW_MARGIN;

        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable startDateViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        startDateViewBackground.getPaint().setColor(Design.DATE_BACKGROUND_COLOR);
        dateView.setBackground(startDateViewBackground);

        mDateTextView = view.findViewById(R.id.contact_capabilities_activity_schedule_item_date_text_view);
        Design.updateTextFont(mDateTextView, Design.FONT_REGULAR32);
        mDateTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mDateTextView.getLayoutParams();
        marginLayoutParams.leftMargin = Design.DATE_VIEW_PADDING;
        marginLayoutParams.rightMargin = Design.DATE_VIEW_PADDING;

        View timeView = view.findViewById(R.id.contact_capabilities_activity_schedule_item_time_view);
        timeView.setOnClickListener(v -> activity.onTimeClick(mScheduleType));

        layoutParams = timeView.getLayoutParams();
        layoutParams.width = Design.HOUR_VIEW_WIDTH;

        ShapeDrawable startHourViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        startHourViewBackground.getPaint().setColor(Design.DATE_BACKGROUND_COLOR);
        timeView.setBackground(startHourViewBackground);

        mTimeTextView = view.findViewById(R.id.contact_capabilities_activity_schedule_item_time_text_view);
        Design.updateTextFont(mTimeTextView, Design.FONT_REGULAR32);
        mTimeTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mTimeTextView.getLayoutParams();
        marginLayoutParams.leftMargin = Design.DATE_VIEW_PADDING;
        marginLayoutParams.rightMargin = Design.DATE_VIEW_PADDING;
    }

    public void onBind(Context context, AbstractCapabilitiesActivity.ScheduleType scheduleType, Date scheduleDate, Time scheduleTime) {

        mScheduleType = scheduleType;

        if (mScheduleType == AbstractCapabilitiesActivity.ScheduleType.START) {
            mTitleView.setText(context.getString(R.string.show_call_activity_settings_start));
        } else {
            mTitleView.setText(context.getString(R.string.show_call_activity_settings_end));
        }

        final Calendar calendar = new DateTime(scheduleDate, scheduleTime).toCalendar(TimeZone.getDefault());

        String formatDate = "dd MMM yyyy";
        String formatTime;
        if (DateFormat.is24HourFormat(context)) {
            formatTime = "kk:mm";
        } else {
            formatTime = "hh:mm a";
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(formatDate, Locale.getDefault());
        mDateTextView.setText(simpleDateFormat.format(calendar.getTime()));

        SimpleDateFormat simpleTimeFormat = new SimpleDateFormat(formatTime, Locale.getDefault());
        mTimeTextView.setText(simpleTimeFormat.format(calendar.getTime()));

        updateFont();
        updateColor();
    }

    public void onViewRecycled() {

    }

    private void updateFont() {

    }

    private void updateColor() {

        itemView.setBackgroundColor(Design.WHITE_COLOR);
    }
}