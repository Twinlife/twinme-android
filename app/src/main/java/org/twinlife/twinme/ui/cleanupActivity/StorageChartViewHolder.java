/*
 *  Copyright (c) 2023-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.cleanupActivity;

import android.content.res.Resources;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

import java.util.List;

public class StorageChartViewHolder extends RecyclerView.ViewHolder {

    private static final float DESIGN_VIEW_HEIGHT = 190f;

    private final View mStorageView;
    private final View mStorageUsedView;
    private final View mStorageAppView;
    private final TextView mTitleView;
    private final TextView mValueView;

    public StorageChartViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int) (DESIGN_VIEW_HEIGHT * Design.HEIGHT_RATIO);
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Design.WHITE_COLOR);

        mStorageView = view.findViewById(R.id.cleanup_activity_storage_chart_item_chart_view);

        mStorageUsedView = view.findViewById(R.id.cleanup_activity_storage_chart_item_used_view);

        mStorageAppView = view.findViewById(R.id.cleanup_activity_storage_chart_item_app_view);

        mTitleView = view.findViewById(R.id.cleanup_activity_storage_chart_item_title_view);
        Design.updateTextFont(mTitleView, Design.FONT_REGULAR32);
        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mValueView = view.findViewById(R.id.cleanup_activity_storage_chart_item_value_view);
        Design.updateTextFont(mValueView, Design.FONT_REGULAR32);
        mValueView.setTextColor(Design.FONT_COLOR_DEFAULT);
    }

    public void onBind(List<UIStorage>storages) {

        UIStorage totalStorage = null;
        UIStorage usedStorage = null;
        UIStorage appStorage = null;

        for (UIStorage storage : storages) {
            if (storage.getStorageType() == UIStorage.StorageType.USED) {
                usedStorage = storage;
            } else if (storage.getStorageType() == UIStorage.StorageType.APP) {
                appStorage = storage;
            } else if (storage.getStorageType() == UIStorage.StorageType.TOTAL) {
                totalStorage = storage;
            }
        }

        if (usedStorage != null && appStorage != null && totalStorage != null) {

            mTitleView.setText(totalStorage.getTitle(itemView.getContext()));
            mValueView.setText(totalStorage.getSize(itemView.getContext()));

            float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
            float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
            ShapeDrawable storageViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
            storageViewBackground.getPaint().setColor(totalStorage.getBackgroundColor());
            mStorageView.setBackground(storageViewBackground);

            float storageViewWidth = Design.DISPLAY_WIDTH - (0.0467f * Design.DISPLAY_WIDTH * 2);
            float storageUsedViewWidth = 0;
            float storageAppViewWidth = 0;

            if (usedStorage.getSize() > 0) {
                storageUsedViewWidth = storageViewWidth * ((float) usedStorage.getSize() / (float) totalStorage.getSize());
            }

            if (appStorage.getSize() > 0) {
                storageAppViewWidth = storageViewWidth * ((float) appStorage.getSize() / (float) totalStorage.getSize());
            }

            ViewGroup.LayoutParams layoutParams = mStorageUsedView.getLayoutParams();
            layoutParams.width = (int) storageUsedViewWidth;

            layoutParams = mStorageAppView.getLayoutParams();
            layoutParams.width = (int) storageAppViewWidth + (int) storageUsedViewWidth;

            outerRadii = new float[]{radius, radius, 0, 0, 0, 0, radius, radius};
            ShapeDrawable usedViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
            usedViewBackground.getPaint().setColor(usedStorage.getBackgroundColor());
            mStorageUsedView.setBackground(usedViewBackground);

            ShapeDrawable appViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
            appViewBackground.getPaint().setColor(appStorage.getBackgroundColor());
            mStorageAppView.setBackground(appViewBackground);
        }

        updateColor();
    }

    private void updateColor() {

        itemView.setBackgroundColor(Design.WHITE_COLOR);
    }
}
