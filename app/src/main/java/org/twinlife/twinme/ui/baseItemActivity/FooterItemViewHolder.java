/*
 *  Copyright (c) 2016-2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import android.view.View;
import android.view.ViewGroup.LayoutParams;

class FooterItemViewHolder extends BaseItemViewHolder {

    FooterItemViewHolder(BaseItemActivity baseItemActivity, View view) {

        super(baseItemActivity, view);
    }

    @Override
    void onBind(Item item) {

        super.onBind(item);

        LayoutParams layoutParams = itemView.getLayoutParams();

        if (getBaseItemActivity().isReplyViewOpen()) {
            layoutParams.height = BaseItemActivity.FOOTER_HEIGHT;

        } else {
            layoutParams.height = 0;
        }

        itemView.setLayoutParams(layoutParams);
    }
}