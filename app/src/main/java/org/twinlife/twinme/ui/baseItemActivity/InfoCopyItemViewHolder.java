/*
 *  Copyright (c) 2019-2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

class InfoCopyItemViewHolder extends BaseItemViewHolder {

    private static final float DESIGN_ITEM_VIEW_HEIGHT = 120f;
    private static final int ITEM_VIEW_HEIGHT;

    static {
        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
    }

    private final TextView mTextView;
    private final ImageView mCopyAllowedImageView;

    InfoCopyItemViewHolder(BaseItemActivity baseItemActivity, View view) {

        super(baseItemActivity, view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = ITEM_VIEW_HEIGHT;
        view.setLayoutParams(layoutParams);

        mTextView = view.findViewById(R.id.info_copy_item_text_view);
        Design.updateTextFont(mTextView, Design.FONT_REGULAR32);

        mCopyAllowedImageView = view.findViewById(R.id.info_copy_item_image_view);
    }

    @Override
    void onBind(Item item) {

        if (!(item instanceof InfoCopyItem)) {
            return;
        }

        InfoCopyItem infoCopyItem = (InfoCopyItem) item;
        if (infoCopyItem.getItem().isClearLocalItem() || !infoCopyItem.getItem().getCopyAllowed()) {
            mTextView.setText(getString(R.string.info_item_activity_may_not_be_copied));
            mCopyAllowedImageView.setBackgroundResource(R.drawable.not_allowed_copy);
        } else {
            mTextView.setText(getString(R.string.info_item_activity_may_be_copied));
            mCopyAllowedImageView.setBackgroundResource(R.drawable.allowed_copy);
        }
    }
}
