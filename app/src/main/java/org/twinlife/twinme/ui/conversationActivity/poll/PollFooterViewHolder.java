/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (fabrice.trescartes@twin.life)
 */

package org.twinlife.twinme.ui.conversationActivity.poll;

import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class PollFooterViewHolder extends RecyclerView.ViewHolder {
    private static final String LOG_TAG = "PollFooterViewHolder";
    private static final boolean DEBUG = false;

    public PollFooterViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = Design.ITEM_VIEW_HEIGHT;
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Color.TRANSPARENT);

        TextView addChoiceView = view.findViewById(R.id.create_poll_footer_item_add_response_text_view);
        Design.updateTextFont(addChoiceView, Design.FONT_MEDIUM34);
        addChoiceView.setTextColor(Design.FONT_COLOR_DEFAULT);
        String addChoice = String.format("+ %s",view.getContext().getString(R.string.poll_view_add_response));
        addChoiceView.setText(addChoice);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) addChoiceView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (CreatePollActivity.DESIGN_MARGIN * Design.WIDTH_RATIO);
    }

    public void onBind(boolean canAddChoice) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBind");
        }

        if (canAddChoice) {
            itemView.setVisibility(View.VISIBLE);
        } else {
            itemView.setVisibility(View.GONE);
        }
    }

    public void onViewRecycled() {

    }
}
