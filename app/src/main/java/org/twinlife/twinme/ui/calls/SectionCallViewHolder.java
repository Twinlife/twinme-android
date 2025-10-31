/*
 *  Copyright (c) 2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.calls;

import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.Utils;

public class SectionCallViewHolder extends RecyclerView.ViewHolder {

    private static final float DESIGN_ITEM_VIEW_HEIGHT = 110f;
    private static final float DESIGN_RIGHT_VIEW_WIDTH = 280f;
    private static final float DESIGN_RIGHT_VIEW_HEIGHT = 80f;
    private static final int ITEM_VIEW_HEIGHT;

    static {
        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
    }

    private final TextView mSectionTitleView;
    protected final View mRightView;
    private final TextView mRightTitleView;
    private final View mSeparatorView;

    public SectionCallViewHolder(@NonNull View view, CallsAdapter.OnCallClickListener onCallClickListener) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = ITEM_VIEW_HEIGHT;
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        mSectionTitleView = view.findViewById(R.id.calls_fragment_section_call_item_title_view);
        Design.updateTextFont(mSectionTitleView, Design.FONT_BOLD26);
        mSectionTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mRightView = view.findViewById(R.id.calls_fragment_section_call_item_right_view);
        layoutParams = mRightView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_RIGHT_VIEW_WIDTH * Design.WIDTH_RATIO);
        layoutParams.height = (int) (DESIGN_RIGHT_VIEW_HEIGHT * Design.HEIGHT_RATIO);

        mRightView.setOnClickListener(v -> onCallClickListener.onDisplayAllExternalCallClick());

        mRightTitleView = view.findViewById(R.id.calls_fragment_section_call_item_right_title_view);
        mRightTitleView.setTypeface(Design.FONT_BOLD26.typeface);
        mRightTitleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_BOLD26.size);
        mRightTitleView.setTextColor(Design.getMainStyle());

        mSeparatorView = view.findViewById(R.id.calls_fragment_section_call_item_separator_view);
        mSeparatorView.setBackgroundColor(Design.SEPARATOR_COLOR);
    }

    public void onBind(String title, boolean showRightView, boolean hideSection) {

        ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
        if (!hideSection) {
            layoutParams.height = ITEM_VIEW_HEIGHT;
        } else {
            layoutParams.height = 0;
        }
        itemView.setLayoutParams(layoutParams);

        mSectionTitleView.setText(Utils.capitalizeString(title));

        if (showRightView) {
            mRightView.setVisibility(View.VISIBLE);
            mRightTitleView.setVisibility(View.VISIBLE);
        } else {
            mRightView.setVisibility(View.GONE);
            mRightTitleView.setVisibility(View.GONE);
        }

        updateFont();
        updateColor();
    }

    public void onViewRecycled() {

    }

    private void updateFont() {

        Design.updateTextFont(mSectionTitleView, Design.FONT_BOLD26);
        Design.updateTextFont(mRightTitleView, Design.FONT_BOLD26);
    }

    private void updateColor() {

        itemView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        mSectionTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mRightTitleView.setTextColor(Design.getMainStyle());
        mSeparatorView.setBackgroundColor(Design.SEPARATOR_COLOR);
    }
}
