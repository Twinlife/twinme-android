/*
 *  Copyright (c) 2016-2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import android.view.View;
import android.view.ViewGroup.LayoutParams;

import org.twinlife.device.android.twinme.R;

class HeaderItemViewHolder extends BaseItemViewHolder {

    HeaderItemViewHolder(BaseItemActivity baseItemActivity, View view) {

        super(baseItemActivity, view, R.id.base_item_activity_header_item_overlay_view);

        LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = BaseItemActivity.HEADER_HEIGHT;
        view.setLayoutParams(layoutParams);
    }

    @Override
    void onBind(Item item) {
        super.onBind(item);

        if (isMenuOpen()) {
            getOverlayView().setVisibility(View.VISIBLE);
        } else {
            getOverlayView().setVisibility(View.INVISIBLE);
        }
    }
}