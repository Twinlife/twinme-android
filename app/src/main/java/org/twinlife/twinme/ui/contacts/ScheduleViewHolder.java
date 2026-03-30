/*
 *  Copyright (c) 2024-2026 twinlife SA.
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

    private static final int DESIGN_LEFT_MARGIN = 30;
    private static final int DESIGN_RIGHT_MARGIN = 76;
    private static final int DESIGN_DATE_MARGIN = 40;
    private static final int DESIGN_DATE_HEIGHT = 80;

    protected static final float DESIGN_ITEM_VIEW_HEIGHT = 120f;
    protected static final int ITEM_VIEW_HEIGHT;

    static {
        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
    }

    private final TextView mTitleView;
    private final View mDateView;
    private final TextView mDateTextView;
    private final View mTimeView;
    private final TextView mTimeTextView;

    public ScheduleViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = ITEM_VIEW_HEIGHT;
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Design.WHITE_COLOR);

        mTitleView = view.findViewById(R.id.contact_capabilities_activity_schedule_item_title_view);
        Design.updateTextFont(mTitleView, Design.FONT_REGULAR34);
        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mTitleView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_LEFT_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_RIGHT_MARGIN * Design.WIDTH_RATIO);

        mDateView = view.findViewById(R.id.contact_capabilities_activity_schedule_item_date_view);

        layoutParams = mDateView.getLayoutParams();
        layoutParams.width = Design.DATE_VIEW_WIDTH;
        layoutParams.height = (int) (DESIGN_DATE_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mDateView.getLayoutParams();
        marginLayoutParams.rightMargin = Design.DATE_VIEW_MARGIN;

        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable startDateViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        startDateViewBackground.getPaint().setColor(Design.DATE_BACKGROUND_COLOR);
        mDateView.setBackground(startDateViewBackground);

        mDateTextView = view.findViewById(R.id.contact_capabilities_activity_schedule_item_date_text_view);
        Design.updateTextFont(mDateTextView, Design.FONT_REGULAR32);
        mDateTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mDateTextView.getLayoutParams();
        marginLayoutParams.leftMargin = Design.DATE_VIEW_PADDING;
        marginLayoutParams.rightMargin = Design.DATE_VIEW_PADDING;

        mTimeView = view.findViewById(R.id.contact_capabilities_activity_schedule_item_time_view);

        layoutParams = mTimeView.getLayoutParams();
        layoutParams.width = Design.HOUR_VIEW_WIDTH;
        layoutParams.height = (int) (DESIGN_DATE_HEIGHT * Design.HEIGHT_RATIO);

        ShapeDrawable startHourViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        startHourViewBackground.getPaint().setColor(Design.DATE_BACKGROUND_COLOR);
        mTimeView.setBackground(startHourViewBackground);

        mTimeTextView = view.findViewById(R.id.contact_capabilities_activity_schedule_item_time_text_view);
        Design.updateTextFont(mTimeTextView, Design.FONT_REGULAR32);
        mTimeTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mTimeTextView.getLayoutParams();
        marginLayoutParams.leftMargin = Design.DATE_VIEW_PADDING;
        marginLayoutParams.rightMargin = Design.DATE_VIEW_PADDING;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mTimeView.getLayoutParams();
        marginLayoutParams.rightMargin = (int) (DESIGN_DATE_MARGIN * Design.WIDTH_RATIO);
    }

    public void resetMargins() {

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mTitleView.getLayoutParams();
        marginLayoutParams.leftMargin = 0;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mTimeView.getLayoutParams();
        marginLayoutParams.rightMargin = 0;
    }

    public void onBind(Context context, AbstractCapabilitiesActivity.ScheduleType scheduleType, Date scheduleDate, Time scheduleTime, Runnable dateRunnable, Runnable timeRunnable) {

        if (dateRunnable != null) {
            mDateView.setOnClickListener(v -> dateRunnable.run());
            mDateView.setVisibility(View.VISIBLE);
        } else {
            mDateView.setOnClickListener(null);
            mDateView.setVisibility(View.GONE);
        }

        if (timeRunnable != null) {
            mTimeView.setOnClickListener(v -> timeRunnable.run());
        } else {
            mTimeView.setOnClickListener(null);
        }

        if (scheduleType == AbstractCapabilitiesActivity.ScheduleType.START) {
            mTitleView.setText(context.getString(R.string.show_call_activity_settings_start));
        } else {
            mTitleView.setText(context.getString(R.string.show_call_activity_settings_end));
        }

        if (scheduleDate != null && scheduleTime != null) {
            final Calendar calendar = new DateTime(scheduleDate, scheduleTime).toCalendar(TimeZone.getDefault());

            String formatDate = "dd MMM yyyy";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(formatDate, Locale.getDefault());
            mDateTextView.setText(simpleDateFormat.format(calendar.getTime()));
        }

        if (scheduleTime != null) {
            mTimeTextView.setText(scheduleTime.toString());
        }

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