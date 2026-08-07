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
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class AccountMigrationScannerViewHolder extends RecyclerView.ViewHolder {

    private static final float DESIGN_HORIZONTAL_MARGIN = 12f;
    private static final float DESIGN_VERTICAL_MARGIN = 10f;

    private static final float DESIGN_ROUNDED_SIZE = 70f;

    private final TextView mStepView;
    private final TextView mMessageView;

    public AccountMigrationScannerViewHolder(@NonNull View view) {

        super(view);

        view.setBackgroundColor(Color.TRANSPARENT);

        View roundedBackgroundView = view.findViewById(R.id.account_migration_scanner_item_rounded_view);

        ViewGroup.LayoutParams layoutParams = roundedBackgroundView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_ROUNDED_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_ROUNDED_SIZE * Design.HEIGHT_RATIO);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) roundedBackgroundView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.topMargin = (int) (DESIGN_VERTICAL_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_VERTICAL_MARGIN * Design.HEIGHT_RATIO);

        GradientDrawable roundedBackgroundDrawable = new GradientDrawable();
        roundedBackgroundDrawable.setColor(Design.getMainStyle());
        roundedBackgroundDrawable.setAlpha(122);
        roundedBackgroundDrawable.setCornerRadius((DESIGN_ROUNDED_SIZE * Design.HEIGHT_RATIO) / 2f);
        roundedBackgroundView.setBackground(roundedBackgroundDrawable);

        mStepView = view.findViewById(R.id.account_migration_scanner_item_step_view);
        Design.updateTextFont(mStepView, Design.FONT_BOLD36);
        mStepView.setTextColor(Color.WHITE);

        mMessageView = view.findViewById(R.id.account_migration_scanner_item_text_view);
        Design.updateTextFont(mMessageView, Design.FONT_REGULAR32);
        mMessageView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mMessageView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
    }

    public void onBind(UIAccountMigrationItem item) {

        mStepView.setText(String.format("%s",item.getPosition() + ""));
        mMessageView.setText(item.getText());
        updateFont();
        updateColor();
    }

    private void updateFont() {

        Design.updateTextFont(mStepView, Design.FONT_BOLD36);
        Design.updateTextFont(mMessageView, Design.FONT_REGULAR32);
    }

    private void updateColor() {

        mMessageView.setTextColor(Design.FONT_COLOR_DEFAULT);
    }
}
