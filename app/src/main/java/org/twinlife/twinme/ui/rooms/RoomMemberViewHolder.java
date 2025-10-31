/*
 *  Copyright (c) 2020 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.rooms;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.CircularImageDescriptor;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.CircularImageView;

public class RoomMemberViewHolder extends RecyclerView.ViewHolder {
    private static final String LOG_TAG = "RoomMemberViewHolder";
    private static final boolean DEBUG = false;

    private static final float DESIGN_ITEM_VIEW_HEIGHT = 120f;
    private static final int ITEM_VIEW_HEIGHT;

    static {
        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
    }

    private final CircularImageView mAvatarView;
    private final TextView mNameView;
    private final View mSeparatorView;

    RoomMemberViewHolder(View view) {
        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = ITEM_VIEW_HEIGHT;
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Design.WHITE_COLOR);

        mAvatarView = view.findViewById(R.id.room_member_activity_item_avatar_view);

        mNameView = view.findViewById(R.id.room_member_activity_item_name_view);
        Design.updateTextFont(mNameView, Design.FONT_REGULAR34);
        mNameView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mSeparatorView = view.findViewById(R.id.room_member_activity_item_separator_view);
        mSeparatorView.setBackgroundColor(Design.SEPARATOR_COLOR);
    }

    public void onBind(Context context, UIRoomMember uiRoomMember, boolean hideSeparator) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBind: uiRoomMember=" + uiRoomMember);
        }

        if (uiRoomMember != null) {
            mNameView.setText(uiRoomMember.getName());
            mAvatarView.setImage(context, null,
                    new CircularImageDescriptor(uiRoomMember.getAvatar(), 0.5f, 0.5f, 0.5f));
        }

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

        Design.updateTextFont(mNameView, Design.FONT_REGULAR34);
    }
}
