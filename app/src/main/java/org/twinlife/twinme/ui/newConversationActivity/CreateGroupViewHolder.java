/*
 *  Copyright (c) 2020 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.newConversationActivity;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class CreateGroupViewHolder extends RecyclerView.ViewHolder {

    CreateGroupViewHolder(@NonNull NewConversationActivity newConversationActivity, @NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = Design.ITEM_VIEW_HEIGHT;
        view.setLayoutParams(layoutParams);

        view.setBackgroundColor(Design.WHITE_COLOR);
        view.setOnClickListener(v -> newConversationActivity.onCreateGroupClick());

        TextView titleView = view.findViewById(R.id.create_group_item_title_view);
        Design.updateTextFont(titleView, Design.FONT_REGULAR34);
        titleView.setTextColor(Design.FONT_COLOR_DEFAULT);
    }

    public void onViewRecycled() {

    }
}
