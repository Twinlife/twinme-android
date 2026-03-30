/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.CommonUtils;

public class InfoIconItemViewHolder extends BaseItemViewHolder {

    private static final float DESIGN_ITEM_VIEW_HEIGHT = 120f;
    private static final float DESIGN_ICON_SIZE = 36f;
    private static final float DESIGN_ICON_MARGIN = 34f;
    private static final int ITEM_VIEW_HEIGHT;

    static {
        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
    }

    private final TextView mTitleView;
    private final TextView mDateTextView;
    private final ImageView mIconView;

    InfoIconItemViewHolder(BaseItemActivity baseItemActivity, View view) {

        super(baseItemActivity, view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = ITEM_VIEW_HEIGHT;
        view.setLayoutParams(layoutParams);

        mTitleView = view.findViewById(R.id.info_icon_item_text_view);
        Design.updateTextFont(mTitleView, Design.FONT_REGULAR32);
        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mIconView = view.findViewById(R.id.info_icon_item_image_view);

        layoutParams = mIconView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_ICON_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_ICON_SIZE * Design.HEIGHT_RATIO);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mIconView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_ICON_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_ICON_MARGIN * Design.WIDTH_RATIO);

        mDateTextView = view.findViewById(R.id.info_icon_item_date_view);
        Design.updateTextFont(mDateTextView, Design.FONT_REGULAR32);
        mDateTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
    }

    @Override
    void onBind(Item item) {

        if (!(item instanceof InfoEphemeralItem) && !(item instanceof InfoDeleteItem)) {
            return;
        }

        if ((item instanceof InfoDeleteItem)) {
            InfoDeleteItem infoDeleteItem = (InfoDeleteItem) item;
            mIconView.setImageDrawable(ResourcesCompat.getDrawable(itemView.getResources(), R.drawable.toolbar_trash_grey, null));
            mIconView.setColorFilter(Design.DELETE_COLOR_RED);
            mTitleView.setText(itemView.getResources().getString(R.string.info_item_activity_deleted));

            long peerDeletedTimestamp = infoDeleteItem.getItem().getPeerDeletedTimestamp();
            if (peerDeletedTimestamp > 0) {
                mDateTextView.setText(CommonUtils.formatItemInterval(getBaseItemActivity(), peerDeletedTimestamp));
            }
        } else {
            InfoEphemeralItem infoEphemeralItem = (InfoEphemeralItem) item;
            mIconView.setImageDrawable(ResourcesCompat.getDrawable(itemView.getResources(), R.drawable.ephemeral_icon, null));
            mIconView.setColorFilter(Design.BLACK_COLOR);
            mTitleView.setText(itemView.getResources().getString(R.string.application_timeout));

            long timeInterval = infoEphemeralItem.getItem().getReadTimestamp() + infoEphemeralItem.getItem().getExpireTimeout();
            if (timeInterval > 0) {
                mDateTextView.setText(CommonUtils.formatItemInterval(getBaseItemActivity(), timeInterval));
            }
        }
    }
}
