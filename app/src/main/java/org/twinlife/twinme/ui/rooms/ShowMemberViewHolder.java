/*
 *  Copyright (c) 2020 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.rooms;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.CircularImageDescriptor;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.CircularImageView;
import org.twinlife.twinme.utils.RoundedView;

public class ShowMemberViewHolder extends RecyclerView.ViewHolder {
    private static final String LOG_TAG = "ShowMemberViewHolder";
    private static final boolean DEBUG = false;

    private final CircularImageView mAvatarView;
    private final View mNoAvatarView;
    private final RoundedView mNoAvatarRoundedView;
    private final TextView mNoAvatarTextView;

    public ShowMemberViewHolder(@NonNull View view, int itemWidth, int itemHeight) {
        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = itemWidth;
        layoutParams.height = itemHeight;
        view.setLayoutParams(layoutParams);

        mAvatarView = view.findViewById(R.id.show_room_activity_room_member_item_avatar_view);

        mNoAvatarView = view.findViewById(R.id.show_room_activity_room_member_item_no_avatar_view);

        mNoAvatarRoundedView = view.findViewById(R.id.show_room_activity_room_member_item_no_avatar_rounded_view);

        mNoAvatarTextView = view.findViewById(R.id.show_room_activity_room_member_item_no_avatar_text_view);
        Design.updateTextFont(mNoAvatarTextView, Design.FONT_BOLD44);
        mNoAvatarTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
    }

    public void onBind(String name, Bitmap avatar, int memberCount) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBind: name=" + name + " avatar=" + avatar + " memberCount=" + memberCount);
        }

        if (name == null) {
            mNoAvatarView.setVisibility(View.VISIBLE);
            mAvatarView.setVisibility(View.GONE);

            String text = "+" + memberCount;
            mNoAvatarTextView.setText(text);
            mNoAvatarTextView.setTextColor(Color.WHITE);
            mNoAvatarRoundedView.setColor(Design.getMainStyle());
        } else if (avatar != null) {
            mNoAvatarView.setVisibility(View.GONE);
            mAvatarView.setVisibility(View.VISIBLE);
            mAvatarView.setImage(mAvatarView.getContext(), null,
                    new CircularImageDescriptor(avatar, 0.5f, 0.5f, 0.5f));
        } else {
            mNoAvatarView.setVisibility(View.VISIBLE);
            mAvatarView.setVisibility(View.GONE);
            mNoAvatarTextView.setText(name.substring(0, 1).toUpperCase());
            mNoAvatarTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
            mNoAvatarRoundedView.setColor(Design.BACKGROUND_COLOR_GREY);
        }
    }

    public void onViewRecycled() {

    }
}
