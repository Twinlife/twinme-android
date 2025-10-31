/*
 *  Copyright (c) 2019-2020 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.calls;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ConversationService.CallDescriptor;
import org.twinlife.twinme.models.Originator;
import org.twinlife.twinme.services.AbstractTwinmeService;
import org.twinlife.twinme.skin.CircularImageDescriptor;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.users.UIOriginator;
import org.twinlife.twinme.utils.CircularImageView;
import org.twinlife.twinme.utils.CommonUtils;
import org.twinlife.twinme.utils.RoundedView;
import org.twinlife.twinme.utils.Utils;

public class CallViewHolder extends RecyclerView.ViewHolder {
    private static final String LOG_TAG = "CallViewHolder";
    private static final boolean DEBUG = false;

    private static final float DESIGN_TYPE_CALL_VIEW_HEIGHT = 28f;
    private static final float DESIGN_TYPE_CALL_VIEW_WIDTH = 18f;
    private static final float DESIGN_TYPE_CALL_VIEW_MARGIN_RIGHT = 14f;
    private static final float DESIGN_ITEM_VIEW_HEIGHT = 126f;
    private static final int DESIGN_AVATAR_HEIGHT = 86;
    private static final float DESIGN_NAME_MARGIN_PERCENT = 0.392f;
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

    private final RoundedView mNoAvatarView;
    private final CircularImageView mAvatarView;
    private final TextView mNameView;
    private final ImageView mTypeImageView;
    private final TextView mTypeView;
    private final TextView mDateView;
    private final View mCertifiedView;
    private final View mSeparatorView;
    private final AbstractTwinmeService mService;

    CallViewHolder(@NonNull AbstractTwinmeService service, View view) {
        super(view);

        mService = service;

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = ITEM_VIEW_HEIGHT;
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Design.WHITE_COLOR);

        mNoAvatarView = view.findViewById(R.id.calls_fragment_call_item_no_avatar_view);
        mNoAvatarView.setColor(Design.GREY_ITEM_COLOR);

        mAvatarView = view.findViewById(R.id.calls_fragment_call_item_avatar_view);

        mNameView = view.findViewById(R.id.calls_fragment_call_item_name_view);
        Design.updateTextFont(mNameView, Design.FONT_MEDIUM34);
        mNameView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mTypeImageView = view.findViewById(R.id.calls_fragment_call_item_type_image_view);
        layoutParams = mTypeImageView.getLayoutParams();
        layoutParams.width = TYPE_CALL_VIEW_WIDTH;
        layoutParams.height = TYPE_CALL_VIEW_HEIGHT;

        boolean isRTL = CommonUtils.isLayoutDirectionRTL();
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mTypeImageView.getLayoutParams();
        if (isRTL) {
            marginLayoutParams.leftMargin = TYPE_CALL_VIEW_MARGIN_RIGHT;
            marginLayoutParams.setMarginStart(TYPE_CALL_VIEW_MARGIN_RIGHT);
        } else {
            marginLayoutParams.rightMargin = TYPE_CALL_VIEW_MARGIN_RIGHT;
            marginLayoutParams.setMarginEnd(TYPE_CALL_VIEW_MARGIN_RIGHT);
        }

        mTypeView = view.findViewById(R.id.calls_fragment_call_item_type_name_view);
        Design.updateTextFont(mTypeView, Design.FONT_MEDIUM28);
        mTypeView.setTextColor(TEXT_VIEW_COLOR);

        mDateView = view.findViewById(R.id.calls_fragment_call_item_date_view);
        Design.updateTextFont(mDateView, Design.FONT_MEDIUM26);
        mDateView.setTextColor(TEXT_VIEW_COLOR);

        mCertifiedView = view.findViewById(R.id.calls_fragment_call_item_certified_image_view);

        layoutParams = mCertifiedView.getLayoutParams();
        layoutParams.height = Design.CERTIFIED_HEIGHT;

        mSeparatorView = view.findViewById(R.id.calls_fragment_call_item_separator_view);
        mSeparatorView.setBackgroundColor(Design.SEPARATOR_COLOR);
    }

    @SuppressLint("DefaultLocale")
    public void onBind(Context context, UICall uiCall, boolean hideSeparator) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBind: uiCall=" + uiCall);
        }

        CallDescriptor callDescriptor = uiCall.getLastCallDescriptor();
        UIOriginator uiOriginator = uiCall.getUIContact();

        if (uiOriginator != null) {
            mService.getImage(uiOriginator.getContact(), (Bitmap avatar) -> {
                if (uiCall.getCount() > 1) {
                    mNameView.setText(String.format("%s (%d)", uiOriginator.getName(), uiCall.getCount()));
                } else {
                    mNameView.setText(uiOriginator.getName());
                }

                if (uiOriginator.getContact().getAvatarId() == null && uiOriginator.getContact().isGroup()) {
                    mNoAvatarView.setVisibility(View.VISIBLE);
                } else {
                    mNoAvatarView.setVisibility(View.GONE);
                }
                mAvatarView.setImage(context, null,
                        new CircularImageDescriptor(avatar, 0.5f, 0.5f, 0.5f));
            });
        }

        if (uiCall.isCertified()) {
            mCertifiedView.setVisibility(View.VISIBLE);
            float maxWidth = Design.DISPLAY_WIDTH - (Design.DISPLAY_WIDTH * DESIGN_NAME_MARGIN_PERCENT) - (DESIGN_AVATAR_HEIGHT * Design.HEIGHT_RATIO) - Design.NAME_TRAILING - Design.CERTIFIED_HEIGHT;
            mNameView.setMaxWidth((int) maxWidth);
        } else {
            mCertifiedView.setVisibility(View.GONE);
            float maxWidth = Design.DISPLAY_WIDTH - (Design.DISPLAY_WIDTH * DESIGN_NAME_MARGIN_PERCENT) - (DESIGN_AVATAR_HEIGHT * Design.HEIGHT_RATIO);
            mNameView.setMaxWidth((int) maxWidth);
        }

        if (callDescriptor.isVideo()) {
            mTypeImageView.setImageResource(R.drawable.history_video_call);
        } else {
            mTypeImageView.setImageResource(R.drawable.history_audio_call);
        }

        if (callDescriptor.isIncoming()) {
            if (uiOriginator != null && uiOriginator.getContact().getType() == Originator.Type.CALL_RECEIVER) {
                mTypeView.setText(context.getString(R.string.premium_services_activity_click_to_call_title));
            } else {
                mTypeView.setText(context.getString(R.string.calls_fragment_incoming_call));
            }

        } else {
            mTypeView.setText(context.getString(R.string.calls_fragment_outgoing_call));
        }

        if (!callDescriptor.isAccepted() && callDescriptor.isIncoming() && callDescriptor.getTerminateReason() != null) {
            mNameView.setTextColor(Design.DELETE_COLOR_RED);
            mTypeView.setText(context.getString(R.string.calls_fragment_missed_call));
        } else {
            mNameView.setTextColor(Design.FONT_COLOR_DEFAULT);
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

        mAvatarView.dispose();
    }

    private void updateColor() {

        itemView.setBackgroundColor(Design.WHITE_COLOR);
        mSeparatorView.setBackgroundColor(Design.SEPARATOR_COLOR);
    }

    private void updateFont() {

        Design.updateTextFont(mNameView, Design.FONT_MEDIUM34);
        Design.updateTextFont(mTypeView, Design.FONT_MEDIUM28);
        Design.updateTextFont(mDateView, Design.FONT_MEDIUM26);
    }
}
