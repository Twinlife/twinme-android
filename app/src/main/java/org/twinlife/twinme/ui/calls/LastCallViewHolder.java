/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.calls;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ConversationService.CallDescriptor;
import org.twinlife.twinme.models.Originator;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.users.UIOriginator;
import org.twinlife.twinme.utils.Utils;

public class LastCallViewHolder extends RecyclerView.ViewHolder {
    private static final String LOG_TAG = "LastCallViewHolder";
    private static final boolean DEBUG = false;

    private static final float DESIGN_DATE_WIDTH = 160f;
    private static final float DESIGN_TYPE_CALL_VIEW_HEIGHT = 28f;
    private static final float DESIGN_TYPE_CALL_VIEW_WIDTH = 28f;
    private static final float DESIGN_TYPE_CALL_VIEW_MARGIN_RIGHT = 14f;
    private static final float DESIGN_ITEM_VIEW_HEIGHT = 126f;
    private static final int ITEM_VIEW_HEIGHT;
    private static final int TYPE_CALL_VIEW_HEIGHT;
    private static final int TYPE_CALL_VIEW_WIDTH;
    private static final int TYPE_CALL_VIEW_MARGIN_RIGHT;
    private static final int TEXT_VIEW_COLOR = Color.argb(255, 119, 138, 159);

    static {
        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
        TYPE_CALL_VIEW_HEIGHT = (int) (DESIGN_TYPE_CALL_VIEW_HEIGHT * Design.HEIGHT_RATIO);
        TYPE_CALL_VIEW_WIDTH = (int) (DESIGN_TYPE_CALL_VIEW_WIDTH * Design.WIDTH_RATIO);
        TYPE_CALL_VIEW_MARGIN_RIGHT = (int) (DESIGN_TYPE_CALL_VIEW_MARGIN_RIGHT * Design.WIDTH_RATIO);
    }

    private final ImageView mTypeImageView;
    private final TextView mTypeView;
    private final TextView mDurationView;
    private final TextView mDateView;
    private final View mSeparatorView;

    LastCallViewHolder(View view) {
        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = ITEM_VIEW_HEIGHT;
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Design.WHITE_COLOR);

        View informationView = view.findViewById(R.id.last_calls_activity_item_information_view);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) informationView.getLayoutParams();
        marginLayoutParams.leftMargin = Design.NAME_TRAILING;

        mTypeImageView = view.findViewById(R.id.last_calls_activity_item_type_image_view);
        mTypeImageView.setColorFilter(Design.BLACK_COLOR);

        layoutParams = mTypeImageView.getLayoutParams();
        layoutParams.width = TYPE_CALL_VIEW_WIDTH;
        layoutParams.height = TYPE_CALL_VIEW_HEIGHT;

        mTypeView = view.findViewById(R.id.last_calls_activity_item_type_view);
        Design.updateTextFont(mTypeView, Design.FONT_MEDIUM28);
        mTypeView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mTypeView.getLayoutParams();
        marginLayoutParams.leftMargin = TYPE_CALL_VIEW_MARGIN_RIGHT;

        mDurationView = view.findViewById(R.id.last_calls_activity_item_duration_view);
        Design.updateTextFont(mDurationView, Design.FONT_MEDIUM34);
        mDurationView.setTextColor(TEXT_VIEW_COLOR);

        mDateView = view.findViewById(R.id.last_calls_activity_item_date_view);
        Design.updateTextFont(mDateView, Design.FONT_MEDIUM26);
        mDateView.setTextColor(TEXT_VIEW_COLOR);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mDateView.getLayoutParams();
        marginLayoutParams.rightMargin = Design.NAME_TRAILING;

        mDateView.setMaxWidth((int)(DESIGN_DATE_WIDTH * Design.WIDTH_RATIO));

        mSeparatorView = view.findViewById(R.id.last_calls_activity_item_separator_view);
        mSeparatorView.setBackgroundColor(Design.SEPARATOR_COLOR);
    }

    @SuppressLint("DefaultLocale")
    public void onBind(Context context, UICall uiCall, boolean hideSeparator) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBind: uiCall=" + uiCall);
        }

        CallDescriptor callDescriptor = uiCall.getLastCallDescriptor();
        UIOriginator uiOriginator = uiCall.getUIContact();
        if (callDescriptor.isVideo()) {
            mTypeImageView.setImageResource(R.drawable.history_video_call);
        } else {
            mTypeImageView.setImageResource(R.drawable.history_audio_call);
        }

        String callDuration = "";
        if (callDescriptor.getTerminateReason() != null && callDescriptor.getDuration() > 0) {
            int duration = (int) callDescriptor.getDuration() / 1000;
            callDuration = Utils.formatCallDuration(context, duration);
        }

        String callType = "";
        if (callDescriptor.isIncoming()) {
            if (uiOriginator != null && uiOriginator.getContact().getType() == Originator.Type.CALL_RECEIVER) {
                callType = context.getString(R.string.premium_services_activity_click_to_call_title);
            } else {
                callType = context.getString(R.string.calls_fragment_incoming_call);
            }
        } else {
            callType = context.getString(R.string.calls_fragment_outgoing_call);
        }

        mDurationView.setText(callDuration);
        mTypeView.setText(callType);

        if (!callDescriptor.isAccepted() && callDescriptor.isIncoming() && callDescriptor.getTerminateReason() != null) {
            mDurationView.setTextColor(Design.DELETE_COLOR_RED);
            mDurationView.setText(Utils.capitalizeString(context.getString(R.string.conversation_activity_call_missed)));
        } else {
            mDurationView.setTextColor(TEXT_VIEW_COLOR);
        }

        mDateView.setText(Utils.formatCallTimeInterval(mDateView.getContext(), callDescriptor.getCreatedTimestamp()));

        if (hideSeparator) {
            mSeparatorView.setVisibility(View.GONE);
        } else {
            mSeparatorView.setVisibility(View.VISIBLE);
        }

        updateColor();
        updateFont();
    }

    public void onViewRecycled() {

    }

    private void updateColor() {

        itemView.setBackgroundColor(Design.WHITE_COLOR);
        mSeparatorView.setBackgroundColor(Design.SEPARATOR_COLOR);
    }

    private void updateFont() {

        Design.updateTextFont(mDurationView, Design.FONT_MEDIUM28);
        Design.updateTextFont(mTypeView, Design.FONT_MEDIUM34);
        Design.updateTextFont(mDateView, Design.FONT_MEDIUM28);
    }
}
