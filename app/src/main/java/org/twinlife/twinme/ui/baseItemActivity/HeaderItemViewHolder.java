/*
 *  Copyright (c) 2016-2019 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import android.view.View;
import android.view.ViewGroup.LayoutParams;

class HeaderItemViewHolder extends BaseItemViewHolder {

    HeaderItemViewHolder(BaseItemActivity baseItemActivity, View view) {

        super(baseItemActivity, view);

        LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = BaseItemActivity.HEADER_HEIGHT;
        view.setLayoutParams(layoutParams);
    }
}