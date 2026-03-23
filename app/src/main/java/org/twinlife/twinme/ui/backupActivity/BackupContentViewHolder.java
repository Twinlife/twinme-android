/*
 *  Copyright (c) 2024-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */


package org.twinlife.twinme.ui.backupActivity;

import android.annotation.SuppressLint;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class BackupContentViewHolder extends RecyclerView.ViewHolder {
    private static final String LOG_TAG = "BackupContentViewHolder";
    private static final boolean DEBUG = false;

    private static final float DESIGN_ITEM_VIEW_HEIGHT = 100;
    private static final float DESIGN_ICON_HEIGHT = 42;
    private static final float DESIGN_MARGIN = 32;
    private static final int ITEM_VIEW_HEIGHT;

    static {
        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
    }

    private final ImageView mIconView;
    private final TextView mTitleView;
    private final TextView mValueView;
    private final View mSeparatorView;

    public BackupContentViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = ITEM_VIEW_HEIGHT;
        view.setLayoutParams(layoutParams);

        mIconView = view.findViewById(R.id.create_backup_activity_content_item_image_view);

        layoutParams = mIconView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_ICON_HEIGHT * Design.HEIGHT_RATIO);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mIconView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_MARGIN * Design.WIDTH_RATIO);

        mTitleView = view.findViewById(R.id.create_backup_activity_content_item_title_view);
        mTitleView.setTypeface(Design.FONT_REGULAR34.typeface);
        mTitleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_REGULAR34.size);
        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mValueView = view.findViewById(R.id.create_backup_activity_content_item_value_view);
        mValueView.setTypeface(Design.FONT_REGULAR32.typeface);
        mValueView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_REGULAR32.size);
        mValueView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mValueView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_MARGIN * Design.WIDTH_RATIO);

        mSeparatorView = view.findViewById(R.id.create_backup_activity_content_item_separator_view);
        mSeparatorView.setBackgroundColor(Design.SEPARATOR_COLOR);
    }

    @SuppressLint("SetTextI18n")
    public void onBind(UIBackupContent backupContent, boolean hideSeparator) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBind: " + backupContent);
        }

        itemView.setBackgroundColor(Design.POPUP_BACKGROUND_COLOR);

        mIconView.setImageDrawable(ResourcesCompat.getDrawable(itemView.getResources(), backupContent.getIcon(itemView.getContext()), null));
        mIconView.setColorFilter(Design.SHOW_ICON_COLOR);

        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mTitleView.setText(backupContent.getTitle(itemView.getContext()));

        mValueView.setText("" + backupContent.getContentCount());

        if (hideSeparator) {
            mSeparatorView.setVisibility(View.GONE);
        } else {
            mSeparatorView.setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("SetTextI18n")
    public void onBind(UIRestoreItem uiRestoreItem, int backgroundColor, boolean hideSeparator) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBind: " + uiRestoreItem);
        }

        itemView.setBackgroundColor(backgroundColor);

        mIconView.setImageDrawable(ResourcesCompat.getDrawable(itemView.getResources(), uiRestoreItem.getIcon(), null));

        if (uiRestoreItem.getColor() != -1) {
            mIconView.setColorFilter(uiRestoreItem.getColor());
        } else {
            mIconView.setColorFilter(null);
        }

        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mTitleView.setText(uiRestoreItem.getText());

        if (uiRestoreItem.getValue() != -1) {
            mValueView.setText("" + uiRestoreItem.getValue());
        } else {
            mValueView.setText("");
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