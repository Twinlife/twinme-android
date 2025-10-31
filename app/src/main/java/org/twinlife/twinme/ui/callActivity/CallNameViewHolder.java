/*
 *  Copyright (c) 2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.callActivity;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.baseItemActivity.Item;
import org.twinlife.twinme.ui.baseItemActivity.NameItem;

public class CallNameViewHolder extends RecyclerView.ViewHolder {

    private static final float DESIGN_NAME_TOP_MARGIN = 4f;
    private static final float DESIGN_NAME_LEFT_MARGIN = 20f;

    private final TextView mTextView;

    CallNameViewHolder(View view) {

        super(view);

        mTextView = view.findViewById(R.id.call_activity_conversation_name_item_text);
        Design.updateTextFont(mTextView, Design.FONT_REGULAR28);
        mTextView.setTextColor(Color.WHITE);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mTextView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_NAME_TOP_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_NAME_TOP_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.leftMargin = (int) (DESIGN_NAME_LEFT_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_NAME_LEFT_MARGIN * Design.WIDTH_RATIO));
    }

    public void onBind(Item item) {

        mTextView.setText(((NameItem) item).getName());
    }
}