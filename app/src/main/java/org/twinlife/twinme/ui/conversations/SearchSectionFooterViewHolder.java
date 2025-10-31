/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.conversations;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class SearchSectionFooterViewHolder extends RecyclerView.ViewHolder {

    private static final float DESIGN_ITEM_VIEW_HEIGHT = 60f;
    private static final float DESIGN_SEPARATOR_HEIGHT = 4f;
    private static final float DESIGN_SEPARATOR_MARGIN = 24f;
    private static final int ITEM_VIEW_HEIGHT;
    private static final int SEPARATOR_HEIGHT;

    static {
        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
        SEPARATOR_HEIGHT = (int) (DESIGN_SEPARATOR_HEIGHT * Design.HEIGHT_RATIO);
    }

    private final View mSeparatorView;

    public SearchSectionFooterViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = ITEM_VIEW_HEIGHT;
        view.setLayoutParams(layoutParams);

        mSeparatorView = view.findViewById(R.id.search_section_footer_item_separator_view);
        mSeparatorView.setBackgroundColor(Design.SEPARATOR_COLOR);

        layoutParams = mSeparatorView.getLayoutParams();
        layoutParams.height = SEPARATOR_HEIGHT;

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mSeparatorView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_SEPARATOR_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_SEPARATOR_MARGIN * Design.WIDTH_RATIO);

        marginLayoutParams.setMarginStart((int) (DESIGN_SEPARATOR_MARGIN * Design.WIDTH_RATIO));
        marginLayoutParams.setMarginEnd((int) (DESIGN_SEPARATOR_MARGIN * Design.WIDTH_RATIO));
    }

    public void onBind() {

        mSeparatorView.setBackgroundColor(Design.SEPARATOR_COLOR);
    }

    public void onViewRecycled() {

    }
}
