/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.accountMigrationActivity;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class MigrationStateViewHolder extends RecyclerView.ViewHolder {


    private static final int DESIGN_STATE_COLOR = Color.argb(255, 0, 255, 204);
    private static final int DESIGN_BORDER_COLOR = Color.argb(255, 219, 219, 219);
    private static final int DESIGN_BORDER_WIDTH = 8;

    private static final int DESIGN_MINIMUM_HEIGHT = 100;

    private static final float DESIGN_HORIZONTAL_MARGIN = 12f;
    private static final float DESIGN_VERTICAL_MARGIN = 10f;
    private static final float DESIGN_JOIN_MARGIN = 8f;

    private static final float DESIGN_ROUNDED_SIZE = 30f;
    private static final float DESIGN_CHECK_SIZE = 20f;

    private final ImageView mCheckImageView;
    private final TextView mMessageView;

    private final GradientDrawable mRoundedBackgroundDrawable;

    private final View mJoinTopView;
    private final View mJoinBottomView;

    public MigrationStateViewHolder(@NonNull View view) {

        super(view);

        view.setMinimumHeight((int) (DESIGN_MINIMUM_HEIGHT * Design.HEIGHT_RATIO));
        view.setBackgroundColor(Color.TRANSPARENT);

        View roundedBackgroundView  = view.findViewById(R.id.account_migration_state_item_rounded_view);

        ViewGroup.LayoutParams layoutParams = roundedBackgroundView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_ROUNDED_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_ROUNDED_SIZE * Design.HEIGHT_RATIO);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) roundedBackgroundView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.topMargin = (int) (DESIGN_VERTICAL_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_VERTICAL_MARGIN * Design.HEIGHT_RATIO);

        mRoundedBackgroundDrawable = new GradientDrawable();
        mRoundedBackgroundDrawable.setColor(Design.WHITE_COLOR);
        mRoundedBackgroundDrawable.setStroke(DESIGN_BORDER_WIDTH, DESIGN_BORDER_COLOR);
        mRoundedBackgroundDrawable.setCornerRadius((DESIGN_ROUNDED_SIZE * Design.HEIGHT_RATIO) / 2f);
        roundedBackgroundView.setBackground(mRoundedBackgroundDrawable);

        mJoinTopView = view.findViewById(R.id.account_migration_state_item_join_top_view);

        layoutParams = mJoinTopView.getLayoutParams();
        layoutParams.width = DESIGN_BORDER_WIDTH;

        GradientDrawable joinTopDrawable = new GradientDrawable();
        joinTopDrawable.setShape(GradientDrawable.RECTANGLE);
        joinTopDrawable.setColor(DESIGN_BORDER_COLOR);
        joinTopDrawable.setCornerRadii(new float[]{
                0f, 0f,
                0f, 0f,
                DESIGN_BORDER_WIDTH * 0.5f, DESIGN_BORDER_WIDTH * 0.5f,
                DESIGN_BORDER_WIDTH * 0.5f, DESIGN_BORDER_WIDTH * 0.5f,
        });
        mJoinTopView.setBackground(joinTopDrawable);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mJoinTopView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_HORIZONTAL_MARGIN * Design.WIDTH_RATIO) + (int) (DESIGN_ROUNDED_SIZE * 0.5 * Design.HEIGHT_RATIO) - (int) (DESIGN_BORDER_WIDTH * 0.5);
        marginLayoutParams.bottomMargin =  (int) (DESIGN_JOIN_MARGIN * Design.HEIGHT_RATIO);

        mJoinBottomView = view.findViewById(R.id.account_migration_state_item_join_bottom_view);

        GradientDrawable joinBottomDrawable = new GradientDrawable();
        joinBottomDrawable.setShape(GradientDrawable.RECTANGLE);
        joinBottomDrawable.setColor(DESIGN_BORDER_COLOR);
        joinBottomDrawable.setCornerRadii(new float[]{
                DESIGN_BORDER_WIDTH * 0.5f, DESIGN_BORDER_WIDTH * 0.5f,
                DESIGN_BORDER_WIDTH * 0.5f, DESIGN_BORDER_WIDTH * 0.5f,
                0f, 0f,
                0f, 0f
        });
        mJoinBottomView.setBackground(joinBottomDrawable);

        layoutParams = mJoinBottomView.getLayoutParams();
        layoutParams.width = DESIGN_BORDER_WIDTH;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mJoinBottomView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_HORIZONTAL_MARGIN * Design.WIDTH_RATIO) + (int) (DESIGN_ROUNDED_SIZE * 0.5 * Design.HEIGHT_RATIO) - (int) (DESIGN_BORDER_WIDTH * 0.5);
        marginLayoutParams.topMargin = (int) (DESIGN_JOIN_MARGIN * Design.HEIGHT_RATIO);

        mCheckImageView = view.findViewById(R.id.account_migration_state_item_check_view);
        mCheckImageView.setColorFilter(Color.WHITE);

        layoutParams = mCheckImageView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_CHECK_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_CHECK_SIZE * Design.HEIGHT_RATIO);

        mMessageView = view.findViewById(R.id.account_migration_state_item_text_view);
        Design.updateTextFont(mMessageView, Design.FONT_REGULAR32);
        mMessageView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mMessageView.getLayoutParams();
        marginLayoutParams.rightMargin = (int) (DESIGN_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
    }

    public void onBind(UIMigrationStateItem item, boolean previousStateDone) {

        SpannableStringBuilder attributed = new SpannableStringBuilder();

        int titleStart = attributed.length();
        attributed.append(item.getTitle());
        attributed.setSpan(
                new ForegroundColorSpan(Design.FONT_COLOR_DEFAULT),
                titleStart,
                attributed.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        String info = item.getInfo();
        if (info != null && !info.isEmpty()) {
            attributed.append("\n");
            int infoStart = attributed.length();
            Spanned attributedInfo = item.getAttributedInfo();
            attributed.append(attributedInfo != null ? attributedInfo : info);
            attributed.setSpan(
                    new ForegroundColorSpan(Design.FONT_COLOR_GREY),
                    infoStart,
                    attributed.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }

        mMessageView.setText(attributed);
        Design.updateTextFont(mMessageView, Design.FONT_MEDIUM32);

        if (item.getType() == UIMigrationStateItem.MigrationStateItemType.INIT) {
            mJoinTopView.setVisibility(View.INVISIBLE);
            mJoinBottomView.setVisibility(View.VISIBLE);
        } else if (item.getType() == UIMigrationStateItem.MigrationStateItemType.TERMINATED) {
            mJoinTopView.setVisibility(View.VISIBLE);
            mJoinBottomView.setVisibility(View.INVISIBLE);
        } else {
            mJoinTopView.setVisibility(View.VISIBLE);
            mJoinBottomView.setVisibility(View.VISIBLE);
        }

        switch (item.getState()) {
            case PENDING:
                mCheckImageView.setVisibility(View.GONE);
                mRoundedBackgroundDrawable.setColor(Color.TRANSPARENT);
                mRoundedBackgroundDrawable.setStroke(DESIGN_BORDER_WIDTH, DESIGN_BORDER_COLOR);
                break;

            case IN_PROGRESS:
                mCheckImageView.setVisibility(View.GONE);
                mRoundedBackgroundDrawable.setStroke(0, Color.TRANSPARENT);
                mRoundedBackgroundDrawable.setColor(DESIGN_STATE_COLOR);
                break;

            case DONE:
                mCheckImageView.setVisibility(View.VISIBLE);
                mRoundedBackgroundDrawable.setColor(Design.getMainStyle());
                mRoundedBackgroundDrawable.setStroke(0, Color.TRANSPARENT);
                break;
        }

        mMessageView.post(() -> {
            int totalHeight = (int) ((itemView.getHeight() - (DESIGN_ROUNDED_SIZE * Design.HEIGHT_RATIO)) * 0.5f);
            totalHeight -= (int) (DESIGN_JOIN_MARGIN * Design.HEIGHT_RATIO);
            ViewGroup.LayoutParams layoutParams = mJoinTopView.getLayoutParams();
            layoutParams.height = totalHeight;
            mJoinTopView.setLayoutParams(layoutParams);

            layoutParams = mJoinBottomView.getLayoutParams();
            layoutParams.height = totalHeight;
            mJoinBottomView.setLayoutParams(layoutParams);
        });

        updateFont();
        updateColor();
    }

    private void updateFont() {

        Design.updateTextFont(mMessageView, Design.FONT_MEDIUM32);
    }

    private void updateColor() {

        mMessageView.setTextColor(Design.FONT_COLOR_DEFAULT);
    }

}
