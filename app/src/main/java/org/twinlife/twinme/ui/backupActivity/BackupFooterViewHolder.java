/*
 *  Copyright (c) 2024-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */


package org.twinlife.twinme.ui.backupActivity;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class BackupFooterViewHolder extends RecyclerView.ViewHolder {
    private static final String LOG_TAG = "BackupFooterViewHolder";
    private static final boolean DEBUG = false;

    private static final float DESIGN_ITEM_VIEW_HEIGHT = 180;
    private static final int ITEM_VIEW_HEIGHT;

    static {
        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
    }

    private final View mActionView;
    private final TextView mActionTextView;

    public BackupFooterViewHolder(@NonNull View view, @Nullable Runnable runnable) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = ITEM_VIEW_HEIGHT;
        view.setLayoutParams(layoutParams);
        mActionView = view.findViewById(R.id.create_backup_activity_footer_item_backup_view);

        if (runnable != null) {
            mActionView.setOnClickListener(v -> runnable.run());
        }

        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable backupViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        backupViewBackground.getPaint().setColor(Design.getMainStyle());
        ViewCompat.setBackground(mActionView, backupViewBackground);

        layoutParams = mActionView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = Design.BUTTON_HEIGHT;

        mActionTextView = view.findViewById(R.id.create_backup_activity_footer_item_backup_text_view);
        mActionTextView.setTypeface(Design.FONT_BOLD36.typeface);
        mActionTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_BOLD36.size);
        mActionTextView.setTextColor(Color.WHITE);
    }

    public void onBind(String title, boolean enable) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBind: " + title + " enable: " + enable);
        }

        mActionTextView.setText(title);

        if (enable) {
            mActionView.setAlpha(1.0f);
        } else {
            mActionView.setAlpha(0.5f);
        }
    }
}
