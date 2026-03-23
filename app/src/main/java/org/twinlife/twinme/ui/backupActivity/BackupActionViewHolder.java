/*
 *  Copyright (c) 2024-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.backupActivity;

import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class BackupActionViewHolder extends RecyclerView.ViewHolder {
    private static final String LOG_TAG = "BackupActionViewHolder";
    private static final boolean DEBUG = false;

    private static final float DESIGN_ITEM_VIEW_HEIGHT = 100;
    private static final float DESIGN_ICON_SIZE = 50;
    private static final float DESIGN_MARGIN = 20;
    private static final int ITEM_VIEW_HEIGHT;
    private static final int ICON_SIZE;

    static {
        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
        ICON_SIZE = (int) (DESIGN_ICON_SIZE * Design.HEIGHT_RATIO);
    }

    private final View mLeftView;
    private final ImageView mLeftIconView;
    private final TextView mLeftTextView;

    private final View mRightView;
    private final ImageView mRightIconView;
    private final TextView mRightTextView;

    public BackupActionViewHolder(@NonNull View view) {

        super(view);

        view.setBackgroundColor(Design.WHITE_COLOR);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = ITEM_VIEW_HEIGHT;
        view.setLayoutParams(layoutParams);

        mLeftView = view.findViewById(R.id.backup_activity_action_item_left_view);
        layoutParams = mLeftView.getLayoutParams();
        layoutParams.width = (int) (Design.DISPLAY_WIDTH * 0.5f);

        mLeftIconView = view.findViewById(R.id.backup_activity_action_item_left_image_view);
        mLeftIconView.setColorFilter(Design.BLACK_COLOR);
        layoutParams = mLeftIconView.getLayoutParams();
        layoutParams.width = ICON_SIZE;
        layoutParams.height = ICON_SIZE;

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mLeftIconView.getLayoutParams();
        marginLayoutParams.rightMargin = (int) (DESIGN_MARGIN * Design.WIDTH_RATIO);

        mLeftTextView = view.findViewById(R.id.backup_activity_action_item_left_text_view);
        mLeftTextView.setTypeface(Design.FONT_MEDIUM30.typeface);
        mLeftTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_MEDIUM30.size);
        mLeftTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mRightView = view.findViewById(R.id.backup_activity_action_item_right_view);

        layoutParams = mRightView.getLayoutParams();
        layoutParams.width = (int) (Design.DISPLAY_WIDTH * 0.5f);

        mRightIconView = view.findViewById(R.id.backup_activity_action_item_right_image_view);
        mRightIconView.setColorFilter(Design.BLACK_COLOR);

        layoutParams = mRightIconView.getLayoutParams();
        layoutParams.width = ICON_SIZE;
        layoutParams.height = ICON_SIZE;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mRightIconView.getLayoutParams();
        marginLayoutParams.rightMargin = (int) (DESIGN_MARGIN * Design.WIDTH_RATIO);

        mRightTextView = view.findViewById(R.id.backup_activity_action_item_right_text_view);
        mRightTextView.setTypeface(Design.FONT_MEDIUM30.typeface);
        mRightTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_MEDIUM30.size);
        mRightTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
    }

    public void onBind(int leftIcon, int rightIcon, String leftTitle, @Nullable String rightTitle, Runnable leftRunnable, @Nullable Runnable rightRunnable) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBind");
        }

        mLeftIconView.setImageDrawable(ResourcesCompat.getDrawable(itemView.getResources(), leftIcon, null));
        mLeftTextView.setText(leftTitle);
        mLeftView.setOnClickListener(view1 -> leftRunnable.run());

        ViewGroup.LayoutParams layoutParams = mLeftView.getLayoutParams();
        if (rightRunnable != null) {
            mRightIconView.setImageDrawable(ResourcesCompat.getDrawable(itemView.getResources(), rightIcon, null));
            mRightTextView.setText(rightTitle);
            mRightView.setOnClickListener(view1 -> rightRunnable.run());
            mRightView.setVisibility(View.VISIBLE);
            layoutParams.width = (int) (Design.DISPLAY_WIDTH * 0.5f);
        } else {
            layoutParams.width = Design.DISPLAY_WIDTH;
            mRightView.setVisibility(View.GONE);
        }

        updateColor();
        updateFont();
    }

    private void updateColor() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateColor");
        }

        mLeftIconView.setColorFilter(Design.BLACK_COLOR);
        mLeftTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mRightIconView.setColorFilter(Design.BLACK_COLOR);
        mRightTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
    }

    private void updateFont() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateFont");
        }

        mLeftTextView.setTypeface(Design.FONT_MEDIUM30.typeface);
        mLeftTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_MEDIUM30.size);

        mRightTextView.setTypeface(Design.FONT_MEDIUM30.typeface);
        mRightTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_MEDIUM30.size);
    }
}
