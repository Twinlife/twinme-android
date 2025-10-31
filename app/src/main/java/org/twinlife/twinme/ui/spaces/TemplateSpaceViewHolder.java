/*
 *  Copyright (c) 2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.spaces;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.RoundedImageView;
import org.twinlife.twinme.utils.RoundedView;

import java.util.Arrays;

public class TemplateSpaceViewHolder extends RecyclerView.ViewHolder {
    private static final String LOG_TAG = "TemplateSpaceViewHolder";
    private static final boolean DEBUG = false;

    private static final float DESIGN_ITEM_VIEW_HEIGHT = 124;
    private static final int ITEM_VIEW_HEIGHT;
    private static final float DESIGN_ITEM_ROUND_CORNER_RADIUS_DP = 18;

    static {
        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
    }

    private static final int PROFILE_VIEW_COLOR = Color.argb(255, 143, 150, 164);

    private final RoundedImageView mAvatarView;
    private final TextView mSpaceView;
    private final TextView mNameView;
    private final TextView mDescriptionView;
    private final GradientDrawable mNoAvatarGradientDrawable;
    private final View mSeparatorView;
    private final View mNoAvatarView;
    private final RoundedView mColorView;

    TemplateSpaceViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = ITEM_VIEW_HEIGHT;
        view.setLayoutParams(layoutParams);

        mAvatarView = view.findViewById(R.id.template_space_activity_item_avatar_view);

        mSpaceView = view.findViewById(R.id.template_space_activity_item_name_view);
        mSpaceView.setTypeface(Design.FONT_MEDIUM34.typeface);
        mSpaceView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_MEDIUM34.size);
        mSpaceView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mNameView = view.findViewById(R.id.template_space_activity_item_no_avatar_text_view);
        mNameView.setTypeface(Design.FONT_BOLD44.typeface);
        mNameView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_BOLD44.size);
        mNameView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mNoAvatarView = view.findViewById(R.id.template_space_activity_item_no_avatar_view);

        mNoAvatarGradientDrawable = new GradientDrawable();
        mNoAvatarGradientDrawable.mutate();
        mNoAvatarGradientDrawable.setColor(Design.BACKGROUND_COLOR_GREY);
        mNoAvatarGradientDrawable.setShape(GradientDrawable.RECTANGLE);
        ViewCompat.setBackground(mNoAvatarView, mNoAvatarGradientDrawable);

        mDescriptionView = view.findViewById(R.id.template_space_activity_item_description_view);
        mDescriptionView.setTypeface(Design.FONT_MEDIUM32.typeface);
        mDescriptionView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_MEDIUM32.size);
        mDescriptionView.setTextColor(PROFILE_VIEW_COLOR);

        mColorView = view.findViewById(R.id.template_space_activity_item_color_view);

        mSeparatorView = view.findViewById(R.id.template_space_activity_item_current_separator_view);
        mSeparatorView.setBackgroundColor(Design.SEPARATOR_COLOR);
    }

    public void onBind(UITemplateSpace uiTemplateSpace, boolean hideSeparator) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBind: uiTemplateSpace=" + uiTemplateSpace);
        }

        itemView.setBackgroundColor(Design.WHITE_COLOR);
        mSpaceView.setTextColor(Design.FONT_COLOR_DEFAULT);

        float corner = DESIGN_ITEM_ROUND_CORNER_RADIUS_DP * Resources.getSystem().getDisplayMetrics().density;
        float[] radii = new float[8];
        Arrays.fill(radii, corner);
        if (uiTemplateSpace.getAvatarId() != -1) {
            mAvatarView.setVisibility(View.VISIBLE);
            mNoAvatarView.setVisibility(View.GONE);
            Bitmap bitmap = BitmapFactory.decodeResource(itemView.getResources(), uiTemplateSpace.getAvatarId());
            mAvatarView.setImageBitmap(bitmap, radii);
            mNameView.setVisibility(View.GONE);
        } else {
            mNoAvatarGradientDrawable.setCornerRadii(radii);
            mAvatarView.setVisibility(View.GONE);
            mNoAvatarView.setVisibility(View.VISIBLE);
            mNameView.setVisibility(View.VISIBLE);

            String name = uiTemplateSpace.getSpace();
            if (name != null && !name.isEmpty()) {
                mNameView.setText(name.substring(0, 1).toUpperCase());
            }

            mNameView.setTextColor(Design.getMainStyle());
        }

        mSpaceView.setText(uiTemplateSpace.getSpace());

        if (uiTemplateSpace.getProfile() != null) {
            mDescriptionView.setVisibility(View.VISIBLE);
            mDescriptionView.setText(uiTemplateSpace.getProfile());
        } else {
            mDescriptionView.setVisibility(View.GONE);
        }

        if (uiTemplateSpace.getColor() != null) {
            mColorView.setColor(Color.parseColor(uiTemplateSpace.getColor()));
        } else {
            mColorView.setColor(Design.getMainStyle());
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