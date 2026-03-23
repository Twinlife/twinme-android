/*
 *  Copyright (c) 2024-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.backupActivity;

import android.content.res.Resources;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class BackupInfoViewHolder extends RecyclerView.ViewHolder {
    private static final String LOG_TAG = "BackupInfoViewHolder";
    private static final boolean DEBUG = false;

    private static final float DESIGN_ITEM_VIEW_HEIGHT = 190;
    private static final float DESIGN_CONTAINER_HEIGHT = 150;
    private static final float DESIGN_ICON_VIEW_HEIGHT = 122;
    private static final float DESIGN_ICON_HEIGHT = 54;
    private static final float DESIGN_ICON_MARGIN = 20;
    private static final float DESIGN_MARGIN = 34;
    private static final int ITEM_VIEW_HEIGHT;

    static {
        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
    }

    private final TextView mInfoView;

    public BackupInfoViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = ITEM_VIEW_HEIGHT;
        view.setLayoutParams(layoutParams);

        View containerView = view.findViewById(R.id.restore_activity_backup_info_container_view);

        layoutParams = containerView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_CONTAINER_HEIGHT * Design.HEIGHT_RATIO);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) containerView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_MARGIN * Design.WIDTH_RATIO);

        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable containerViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        containerViewBackground.getPaint().setColor(Design.GREY_ITEM_COLOR);
        ViewCompat.setBackground(containerView, containerViewBackground);

        View iconView = view.findViewById(R.id.restore_activity_backup_info_icon_view);

        layoutParams = iconView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_ICON_VIEW_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) iconView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_ICON_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_ICON_MARGIN * Design.WIDTH_RATIO);

        ShapeDrawable iconViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        iconViewBackground.getPaint().setColor(Design.WHITE_COLOR);
        ViewCompat.setBackground(iconView, iconViewBackground);

        ImageView iconImageView = view.findViewById(R.id.restore_activity_backup_info_image_view);

        layoutParams = iconImageView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_ICON_HEIGHT * Design.HEIGHT_RATIO);

        layoutParams = iconView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_ICON_VIEW_HEIGHT * Design.HEIGHT_RATIO);

        mInfoView = view.findViewById(R.id.restore_activity_backup_info_text_view);
        mInfoView.setTypeface(Design.FONT_MEDIUM34.typeface);
        mInfoView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_MEDIUM34.size);
        mInfoView.setTextColor(Design.FONT_COLOR_DEFAULT);
    }

    public void onBind(String fileName, int backgroundColor) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBind: " + fileName);
        }

        mInfoView.setText(fileName);
        itemView.setBackgroundColor(backgroundColor);
    }
}
