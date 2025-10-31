/*
 *  Copyright (c) 2024-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.settingsActivity;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class QualityOfServiceViewHolder extends RecyclerView.ViewHolder {

    private static final int DESIGN_CONTENT_HEIGHT = 798;
    private static final float DESIGN_BUTTON_MARGIN = 20;
    private static final float DESIGN_BUTTON_SIDE_MARGIN = 40;
    private static final float DESIGN_IMAGE_HEIGHT = 560;
    private static final float DESIGN_IMAGE_MARGIN = 30;
    private static final float DESIGN_SETTINGS_TITLE_MARGIN = 16;
    private static final float DESIGN_SETTINGS_TITLE_SIDE_MARGIN = 30;

    private static final int IMAGE_MARGIN;

    private static final int BUTTON_MARGIN;
    private static final int BUTTON_SIDE_MARGIN;
    private static final int SETTINGS_TITLE_MARGIN;
    private static final int SETTINGS_TITLE_SIDE_MARGIN;

    static {
        IMAGE_MARGIN = (int) (DESIGN_IMAGE_MARGIN * Design.HEIGHT_RATIO);
        BUTTON_MARGIN = (int) (DESIGN_BUTTON_MARGIN * Design.HEIGHT_RATIO);
        BUTTON_SIDE_MARGIN = (int) (DESIGN_BUTTON_SIDE_MARGIN * Design.WIDTH_RATIO);
        SETTINGS_TITLE_MARGIN = (int) (DESIGN_SETTINGS_TITLE_MARGIN * Design.HEIGHT_RATIO);
        SETTINGS_TITLE_SIDE_MARGIN = (int) (DESIGN_SETTINGS_TITLE_SIDE_MARGIN * Design.WIDTH_RATIO);
    }

    private final ImageView mImageView;
    private final View mSettingsView;
    private final TextView mMessageTextView;

    QualityOfServiceViewHolder(QualityOfServiceActivity qualityOfServiceActivity, @NonNull View view) {

        super(view);

        mImageView = view.findViewById(R.id.quality_of_services_activity_item_image);

        ViewGroup.LayoutParams layoutParams = mImageView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_IMAGE_HEIGHT * Design.HEIGHT_RATIO);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mImageView.getLayoutParams();
        marginLayoutParams.topMargin = IMAGE_MARGIN;
        marginLayoutParams.bottomMargin = IMAGE_MARGIN;

        mMessageTextView = view.findViewById(R.id.quality_of_services_activity_item_message_view);
        Design.updateTextFont(mMessageTextView, Design.FONT_MEDIUM32);
        mMessageTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mMessageTextView.setMovementMethod(new ScrollingMovementMethod());

        float messageMaxHeight = Design.DISPLAY_HEIGHT - (DESIGN_CONTENT_HEIGHT * Design.HEIGHT_RATIO);
        mMessageTextView.setMaxHeight((int) messageMaxHeight);

        mSettingsView = view.findViewById(R.id.quality_of_services_activity_item_settings_view);
        mSettingsView.setOnClickListener(v -> qualityOfServiceActivity.onPermissionsClick());

        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable createViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        createViewBackground.getPaint().setColor(Design.getMainStyle());
        mSettingsView.setBackground(createViewBackground);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mSettingsView.getLayoutParams();
        marginLayoutParams.topMargin = BUTTON_MARGIN;
        marginLayoutParams.bottomMargin = BUTTON_MARGIN;
        marginLayoutParams.leftMargin = BUTTON_SIDE_MARGIN;
        marginLayoutParams.rightMargin = BUTTON_SIDE_MARGIN;

        TextView settingsTextView = view.findViewById(R.id.quality_of_services_activity_item_settings_text_view);
        Design.updateTextFont(settingsTextView, Design.FONT_MEDIUM34);
        settingsTextView.setTextColor(Color.WHITE);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) settingsTextView.getLayoutParams();
        marginLayoutParams.leftMargin = SETTINGS_TITLE_SIDE_MARGIN;
        marginLayoutParams.rightMargin = SETTINGS_TITLE_SIDE_MARGIN;
        marginLayoutParams.topMargin = SETTINGS_TITLE_MARGIN;
        marginLayoutParams.bottomMargin = SETTINGS_TITLE_MARGIN;
    }

    public void onBind(UIQuality uiQuality, boolean hideAction) {

        mMessageTextView.setText(uiQuality.getMessage());

        Bitmap bitmap = BitmapFactory.decodeResource(itemView.getResources(), uiQuality.getImage());
        mImageView.setImageBitmap(bitmap);

        if (hideAction) {
            mSettingsView.setVisibility(View.GONE);
        } else {
            mSettingsView.setVisibility(View.VISIBLE);
        }
    }

    public void onViewRecycled() {

    }
}
