/*
 *  Copyright (c) 2023-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinme.skin.Design;

public class AnnotationViewHolder extends RecyclerView.ViewHolder {
    private static final String LOG_TAG = "AnnotationViewHolder";
    private static final boolean DEBUG = false;

    private static final float DESIGN_ITEM_VIEW_LARGE_WIDTH = 90f;
    private static final float DESIGN_ITEM_VIEW_WIDTH = 60f;
    private static final float DESIGN_ITEM_VIEW_HEIGHT = 50f;
    private static final float DESIGN_CONTAINER_LARGE_WIDTH = 82f;
    private static final float DESIGN_CONTAINER_WIDTH = 50f;
    private static final float DESIGN_ICON_FORWARDED_HEIGHT = 28f;
    private static final float DESIGN_ICON_HEIGHT = 44f;
    private static final float DESIGN_ICON_MARGIN = 8f;
    private static final int ITEM_VIEW_LARGE_WIDTH;
    private static final int ITEM_VIEW_WIDTH;
    private static final int ITEM_VIEW_HEIGHT;
    private static final int CONTAINER_LARGE_WIDTH;
    private static final int CONTAINER_WIDTH;
    private static final int ICON_FORWARDED_HEIGHT;
    private static final int ICON_HEIGHT;
    private static final int ICON_MARGIN;

    static {
        ITEM_VIEW_LARGE_WIDTH = (int) (DESIGN_ITEM_VIEW_LARGE_WIDTH * Design.WIDTH_RATIO);
        ITEM_VIEW_WIDTH = (int) (DESIGN_ITEM_VIEW_WIDTH * Design.WIDTH_RATIO);
        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
        CONTAINER_LARGE_WIDTH = (int) (DESIGN_CONTAINER_LARGE_WIDTH * Design.WIDTH_RATIO);
        CONTAINER_WIDTH = (int) (DESIGN_CONTAINER_WIDTH * Design.WIDTH_RATIO);
        ICON_FORWARDED_HEIGHT = (int) (DESIGN_ICON_FORWARDED_HEIGHT * Design.HEIGHT_RATIO);
        ICON_HEIGHT = (int) (DESIGN_ICON_HEIGHT * Design.HEIGHT_RATIO);
        ICON_MARGIN = (int) (DESIGN_ICON_MARGIN * Design.WIDTH_RATIO);
    }

    private final View mContainerView;
    private final ImageView mImageView;
    private final TextView mCountView;

    @Nullable
    private ConversationService.DescriptorId mDescriptorId;

    AnnotationViewHolder(BaseItemActivity baseItemActivity, @NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = ITEM_VIEW_WIDTH;
        layoutParams.height = ITEM_VIEW_HEIGHT;
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Color.TRANSPARENT);

        mContainerView = view.findViewById(R.id.base_item_activity_annotation_item_container_view);
        mContainerView.setOnClickListener(v -> baseItemActivity.onAnnotationClick(mDescriptorId));

        layoutParams = mContainerView.getLayoutParams();
        layoutParams.width = CONTAINER_WIDTH;
        layoutParams.height = ICON_HEIGHT;

        float radius = ICON_HEIGHT * 0.5f * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable containerViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        containerViewBackground.getPaint().setColor(Design.POPUP_BACKGROUND_COLOR);
        mContainerView.setBackground(containerViewBackground);

        mImageView = view.findViewById(R.id.base_item_activity_annotation_item_image_view);

        mCountView = view.findViewById(R.id.base_item_activity_annotation_item_count_view);
        Design.updateTextFont(mCountView, Design.FONT_MEDIUM24);
        mCountView.setTextColor(Design.FONT_COLOR_DEFAULT);
    }

    @SuppressLint("DefaultLocale")
    public void onBind(ConversationService.DescriptorId descriptorId, Drawable image, int count, int colorFilter, boolean isPeerItem) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBind: image=" + image + " count=" + count);
        }

        mDescriptorId = descriptorId;

        if (!isPeerItem) {
            RelativeLayout.LayoutParams relativeLayoutParams = (RelativeLayout.LayoutParams) mContainerView.getLayoutParams();
            relativeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
            relativeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        }

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mImageView.getLayoutParams();
        ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
        ViewGroup.LayoutParams containerLayoutParams = mContainerView.getLayoutParams();
        if (count > 1) {
            layoutParams.width = ITEM_VIEW_LARGE_WIDTH;
            itemView.setLayoutParams(layoutParams);

            containerLayoutParams.width = CONTAINER_LARGE_WIDTH;
            mContainerView.setLayoutParams(containerLayoutParams);

            mCountView.setVisibility(View.VISIBLE);
            mCountView.setText(String.format("%d",count));

            marginLayoutParams.leftMargin = ICON_MARGIN;
            marginLayoutParams.rightMargin = 0;
            marginLayoutParams.setMarginStart(ICON_MARGIN);
            marginLayoutParams.setMarginEnd(0);

            marginLayoutParams = (ViewGroup.MarginLayoutParams) mCountView.getLayoutParams();
            marginLayoutParams.rightMargin = ICON_MARGIN / 2;
            marginLayoutParams.setMarginEnd(ICON_MARGIN / 2);
        } else {
            layoutParams.width = ITEM_VIEW_WIDTH;
            itemView.setLayoutParams(layoutParams);

            containerLayoutParams.width = CONTAINER_WIDTH;
            mContainerView.setLayoutParams(containerLayoutParams);

            mCountView.setVisibility(View.GONE);
            marginLayoutParams.leftMargin = ICON_MARGIN;
            marginLayoutParams.rightMargin = ICON_MARGIN;
            marginLayoutParams.setMarginStart(ICON_MARGIN);
            marginLayoutParams.setMarginEnd(ICON_MARGIN);

            marginLayoutParams = (ViewGroup.MarginLayoutParams) mCountView.getLayoutParams();
            marginLayoutParams.rightMargin = 0;
            marginLayoutParams.setMarginEnd(0);
        }

        layoutParams = mImageView.getLayoutParams();
        layoutParams.height = ICON_HEIGHT;

        mImageView.setImageDrawable(image);
        mImageView.setColorFilter(colorFilter);
    }

    public void onBindAnnotationWithImage(Drawable image, boolean isPeerItem) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindAnnotationWithImage: image=" + image);
        }

        ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
        layoutParams.width = ITEM_VIEW_WIDTH;
        itemView.setLayoutParams(layoutParams);

        ViewGroup.LayoutParams containerLayoutParams = mContainerView.getLayoutParams();
        containerLayoutParams.width = CONTAINER_WIDTH;
        mContainerView.setLayoutParams(containerLayoutParams);

        int margin = (int) ((CONTAINER_WIDTH - ICON_FORWARDED_HEIGHT) * 0.5);
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mImageView.getLayoutParams();
        marginLayoutParams.leftMargin = margin;
        marginLayoutParams.rightMargin = margin;
        marginLayoutParams.setMarginStart(margin);
        marginLayoutParams.setMarginEnd(margin);

        layoutParams = mImageView.getLayoutParams();
        layoutParams.height = ICON_FORWARDED_HEIGHT;

        image.setColorFilter(Design.BLACK_COLOR, PorterDuff.Mode.SRC_IN);
        mImageView.setImageDrawable(image);

        mCountView.setVisibility(View.GONE);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mCountView.getLayoutParams();
        marginLayoutParams.rightMargin = 0;
        marginLayoutParams.setMarginEnd(0);
    }

    public void onViewRecycled() {
    }
}
