/*
 *  Copyright (c) 2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.cleanupActivity;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.RoundedView;

public class StorageViewHolder extends RecyclerView.ViewHolder {

    private static final float DESIGN_VIEW_HEIGHT = 50f;

    private final TextView mTitleView;
    private final TextView mValueView;
    private final RoundedView mColorView;

    public StorageViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int) (DESIGN_VIEW_HEIGHT * Design.HEIGHT_RATIO);
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Design.WHITE_COLOR);

        mTitleView = view.findViewById(R.id.cleanup_activity_storage_item_title_view);
        Design.updateTextFont(mTitleView, Design.FONT_REGULAR32);
        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mValueView = view.findViewById(R.id.cleanup_activity_storage_item_value_view);
        Design.updateTextFont(mValueView, Design.FONT_REGULAR32);
        mValueView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mColorView = view.findViewById(R.id.cleanup_activity_storage_item_rounded_view);
    }

    public void onBind(UIStorage storage) {

        mTitleView.setText(storage.getTitle(itemView.getContext()));
        mValueView.setText(storage.getSize(itemView.getContext()));

        mColorView.setBorder(1, storage.getBorderColor());
        mColorView.setColor(storage.getBackgroundColor());

        updateColor();
    }

    private void updateColor() {

        itemView.setBackgroundColor(Design.WHITE_COLOR);
    }
}
