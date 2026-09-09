/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.settingsActivity;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class DiagnosticViewHolder extends RecyclerView.ViewHolder {
    private static final String LOG_TAG = "DiagnosticViewHolder";
    private static final boolean DEBUG = false;

    private static final float DESIGN_ITEM_VIEW_HEIGHT = 124;
    private static final float DESIGN_ICON_VIEW_SIZE = 42;
    private static final float DESIGN_STATE_VIEW_SIZE = 42;
    private static final float DESIGN_HORIZONTAL_MARGIN = 32;

    private static final int ITEM_VIEW_HEIGHT;

    static {
        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
    }

    private final ImageView mIconView;
    private final ImageView mStateView;
    private final TextView mTitleView;
    private final View mSeparatorView;

    public DiagnosticViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = ITEM_VIEW_HEIGHT;
        view.setLayoutParams(layoutParams);

        mIconView = view.findViewById(R.id.diagnostic_item_image_view);
        mIconView.setColorFilter(Design.BLACK_COLOR);

        layoutParams = mIconView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_ICON_VIEW_SIZE * Design.WIDTH_RATIO);
        layoutParams.height = (int) (DESIGN_ICON_VIEW_SIZE * Design.WIDTH_RATIO);
        mIconView.setLayoutParams(layoutParams);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mIconView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
        mIconView.setLayoutParams(marginLayoutParams);

        mTitleView = view.findViewById(R.id.diagnostic_item_title_view);
        Design.updateTextFont(mTitleView, Design.FONT_REGULAR34);
        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mTitleView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
        mTitleView.setLayoutParams(marginLayoutParams);

        mStateView = view.findViewById(R.id.diagnostic_item_state_view);

        layoutParams = mStateView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_STATE_VIEW_SIZE * Design.WIDTH_RATIO);
        layoutParams.height = (int) (DESIGN_STATE_VIEW_SIZE * Design.WIDTH_RATIO);
        mStateView.setLayoutParams(layoutParams);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mStateView.getLayoutParams();
        marginLayoutParams.rightMargin = (int) (DESIGN_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
        mStateView.setLayoutParams(marginLayoutParams);

        mSeparatorView = view.findViewById(R.id.diagnostic_item_separator_view);
        mSeparatorView.setBackgroundColor(Design.SEPARATOR_COLOR);
    }

    public void onBind(UIDiagnosticSubsection item, boolean hideSeparator) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBind: item=" + item);
        }

        itemView.setBackgroundColor(Design.WHITE_COLOR);

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(item.getTitle());
        spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_DEFAULT), 0, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        if (item.getSubtitle() != null) {
            spannableStringBuilder.append("\n");
            int startInfo = spannableStringBuilder.length();
            spannableStringBuilder.append(item.getSubtitle());
            spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_GREY), startInfo, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        mTitleView.setText(spannableStringBuilder);

        mIconView.setImageDrawable(ResourcesCompat.getDrawable(itemView.getResources(), item.getIcon(), null));
        mIconView.setColorFilter(Design.BLACK_COLOR);

        if (item.getState() == UIDiagnosticSubsection.DiagnosticState.OK) {
            mStateView.setImageDrawable(ResourcesCompat.getDrawable(itemView.getResources(), R.drawable.diagniostic_ok_icon, null));
            mStateView.setColorFilter(Color.GREEN);
        } else if (item.getState() == UIDiagnosticSubsection.DiagnosticState.KO) {
            mStateView.setImageDrawable(ResourcesCompat.getDrawable(itemView.getResources(), R.drawable.diagniostic_ko_icon, null));
            mStateView.setColorFilter(Color.RED);
        } else {
            mStateView.setImageDrawable(null);
        }

        if (hideSeparator) {
            mSeparatorView.setVisibility(View.GONE);
        } else {
            mSeparatorView.setVisibility(View.VISIBLE);
        }
    }

    public void onViewRecycled() {

    }
}