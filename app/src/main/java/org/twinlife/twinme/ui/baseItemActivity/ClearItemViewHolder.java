/*
 *  Copyright (c) 2022 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import android.view.View;
import android.widget.TextView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

import java.util.ArrayList;
import java.util.List;

public class ClearItemViewHolder extends ItemViewHolder {

    ClearItemViewHolder(BaseItemActivity baseItemActivity, View view, boolean allowLongClick) {

        super(baseItemActivity, view,
                R.id.base_item_activity_clear_item_container,
                R.id.base_item_activity_clear_item_state_view,
                R.id.base_item_activity_clear_item_state_avatar_view,
                R.id.base_item_activity_clear_item_overlay_view,
                R.id.base_item_activity_clear_item_selected_view,
                R.id.base_item_activity_clear_item_selected_image_view);

        TextView mResetView = view.findViewById(R.id.base_item_activity_clear_item_reset_view);
        Design.updateTextFont(mResetView, Design.FONT_ITALIC_28);
        mResetView.setTextColor(Design.DELETE_COLOR_RED);

        View clearContainer = view.findViewById(R.id.base_item_activity_clear_item_container);

        clearContainer.setOnClickListener(v -> {
            if (getBaseItemActivity().isSelectItemMode()) {
                onContainerClick();
            }
        });

        if (allowLongClick) {
            clearContainer.setOnLongClickListener(v -> {
                baseItemActivity.onItemLongPress(getItem());

                return true;
            });
        }
    }

    @Override
    void onBind(Item item) {

        super.onBind(item);
    }

    @Override
    void startDeletedAnimation() {

    }

    @Override
    void onViewRecycled() {

        super.onViewRecycled();
    }

    @Override
    List<View> clickableViews() {

        return new ArrayList<View>() {
            {
                add(getContainer());
            }
        };
    }
}
