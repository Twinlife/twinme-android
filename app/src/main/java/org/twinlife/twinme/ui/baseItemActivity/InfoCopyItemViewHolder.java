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

import androidx.core.content.res.ResourcesCompat;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

class InfoCopyItemViewHolder extends BaseItemViewHolder {

    private static final float DESIGN_ITEM_VIEW_HEIGHT = 120f;
    private static final float DESIGN_ICON_MARGIN = 34f;
    private static final float DESIGN_ICON_SIZE = 36f;

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

        mCopyAllowedImageView = view.findViewById(R.id.info_copy_item_image_view);
        mCopyAllowedImageView.setColorFilter(Design.BLACK_COLOR);

        layoutParams = mCopyAllowedImageView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_ICON_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_ICON_SIZE * Design.HEIGHT_RATIO);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mCopyAllowedImageView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_ICON_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_ICON_MARGIN * Design.WIDTH_RATIO);

        mTextView = view.findViewById(R.id.info_copy_item_text_view);
        Design.updateTextFont(mTextView, Design.FONT_REGULAR32);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mTextView.getLayoutParams();
        marginLayoutParams.rightMargin = (int) (DESIGN_ICON_MARGIN * Design.WIDTH_RATIO);
    }

    @Override
    void onBind(Item item) {

        if (!(item instanceof InfoCopyItem)) {
            return;
        }

        InfoCopyItem infoCopyItem = (InfoCopyItem) item;
        if (infoCopyItem.getItem().isClearLocalItem() || !infoCopyItem.getItem().getCopyAllowed()) {
            mTextView.setText(getString(R.string.info_item_view_may_not_be_copied));
            mCopyAllowedImageView.setImageDrawable(ResourcesCompat.getDrawable(itemView.getResources(), R.drawable.not_allowed_copy, null));
        } else {
            mTextView.setText(getString(R.string.info_item_view_may_be_copied));
            mCopyAllowedImageView.setImageDrawable(ResourcesCompat.getDrawable(itemView.getResources(), R.drawable.allowed_copy, null));
        }
    }
}
