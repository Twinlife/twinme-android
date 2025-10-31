/*
 *  Copyright (c) 2019-2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.spaces;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
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
import org.twinlife.twinme.utils.CommonUtils;
import org.twinlife.twinme.utils.RoundedImageView;
import org.twinlife.twinme.utils.RoundedView;

import java.util.Arrays;

public class SpaceViewHolder extends RecyclerView.ViewHolder {
    private static final String LOG_TAG = "SpaceViewHolder";
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
    private final ImageView mCurrentSpaceImageView;
    private final ImageView mNotificationMarkView;
    private final View mCurrentSpaceView;
    private final GradientDrawable mGradientDrawable;
    private final GradientDrawable mNoAvatarGradientDrawable;
    private final View mSeparatorView;
    private final View mNoAvatarView;
    private final RoundedView mColorView;

    SpaceViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = ITEM_VIEW_HEIGHT;
        view.setLayoutParams(layoutParams);

        mAvatarView = view.findViewById(R.id.spaces_activity_space_item_avatar_view);

        mSpaceView = view.findViewById(R.id.spaces_activity_space_item_space_name_view);
        mSpaceView.setTypeface(Design.FONT_MEDIUM34.typeface);
        mSpaceView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_MEDIUM34.size);
        mSpaceView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mNameView = view.findViewById(R.id.spaces_activity_space_item_no_avatar_text_view);
        mNameView.setTypeface(Design.FONT_BOLD44.typeface);
        mNameView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_BOLD44.size);
        mNameView.setTextColor(Color.WHITE);

        mNoAvatarView = view.findViewById(R.id.spaces_activity_space_item_no_avatar_view);

        mNoAvatarGradientDrawable = new GradientDrawable();
        mNoAvatarGradientDrawable.mutate();
        mNoAvatarGradientDrawable.setColor(Design.BACKGROUND_COLOR_GREY);
        mNoAvatarGradientDrawable.setShape(GradientDrawable.RECTANGLE);
        ViewCompat.setBackground(mNoAvatarView, mNoAvatarGradientDrawable);

        mDescriptionView = view.findViewById(R.id.spaces_activity_space_item_description_name_view);
        mDescriptionView.setTypeface(Design.FONT_MEDIUM32.typeface);
        mDescriptionView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_MEDIUM32.size);
        mDescriptionView.setTextColor(PROFILE_VIEW_COLOR);

        mCurrentSpaceImageView = view.findViewById(R.id.spaces_activity_space_item_current_space_image_view);

        mNotificationMarkView = view.findViewById(R.id.spaces_activity_space_item_notitification_view);
        mNotificationMarkView.setVisibility(View.INVISIBLE);

        mCurrentSpaceView = view.findViewById(R.id.spaces_activity_space_item_current_space_view);
        mCurrentSpaceView.setVisibility(View.INVISIBLE);

        mColorView = view.findViewById(R.id.spaces_activity_space_item_color_view);

        mGradientDrawable = new GradientDrawable();
        mGradientDrawable.mutate();
        mGradientDrawable.setColor(Design.BACKGROUND_SPACE_AVATAR);
        mGradientDrawable.setShape(GradientDrawable.RECTANGLE);
        ViewCompat.setBackground(mCurrentSpaceView, mGradientDrawable);

        mSeparatorView = view.findViewById(R.id.spaces_activity_space_item_current_separator_view);
        mSeparatorView.setBackgroundColor(Design.SEPARATOR_COLOR);
    }

    public void onBind(UISpace uiSpace, boolean hideSeparator) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBind: space=" + uiSpace);
        }

        itemView.setBackgroundColor(Design.WHITE_COLOR);
        mSpaceView.setTextColor(Design.FONT_COLOR_DEFAULT);

        float corner = DESIGN_ITEM_ROUND_CORNER_RADIUS_DP * Resources.getSystem().getDisplayMetrics().density;
        if (CommonUtils.isLayoutDirectionRTL()) {
            mGradientDrawable.setCornerRadii(new float[]{corner, corner, 0, 0, 0, 0, corner, corner});
        } else {
            mGradientDrawable.setCornerRadii(new float[]{0, 0, corner, corner, corner, corner, 0, 0});
        }

        float[] radii = new float[8];
        Arrays.fill(radii, corner);
        if (uiSpace.getAvatarSpace() != null) {
            mAvatarView.setVisibility(View.VISIBLE);
            mNoAvatarView.setVisibility(View.GONE);
            mAvatarView.setImageBitmap(uiSpace.getAvatarSpace(), radii);
            mNameView.setVisibility(View.GONE);
        } else {
            mNoAvatarGradientDrawable.setCornerRadii(radii);
            mAvatarView.setVisibility(View.GONE);
            mNoAvatarView.setVisibility(View.VISIBLE);
            mNameView.setVisibility(View.VISIBLE);

            String name = uiSpace.getNameSpace();
            if (name != null && !name.isEmpty()) {
                mNameView.setText(name.substring(0, 1).toUpperCase());
            }
            mNoAvatarGradientDrawable.setColor(Design.getDefaultColor(uiSpace.getSpaceSettings().getStyle()));
        }

        mSpaceView.setText(uiSpace.getNameSpace());

        if (uiSpace.hasProfile()) {
            mDescriptionView.setVisibility(View.VISIBLE);
            mDescriptionView.setText(uiSpace.getNameProfile());
        } else {
            mDescriptionView.setVisibility(View.GONE);
        }

        mGradientDrawable.setColor(Design.getDefaultColor(uiSpace.getSpaceSettings().getStyle()));
        mColorView.setColor(Design.getDefaultColor(uiSpace.getSpaceSettings().getStyle()));
        mCurrentSpaceImageView.setColorFilter(Design.getDefaultColor(uiSpace.getSpaceSettings().getStyle()));

        if (uiSpace.hasNotification()) {
            mNotificationMarkView.setVisibility(View.VISIBLE);
        } else {
            mNotificationMarkView.setVisibility(View.INVISIBLE);
        }

        if (uiSpace.isCurrentSpace()) {
            mCurrentSpaceView.setVisibility(View.VISIBLE);
            mCurrentSpaceImageView.setVisibility(View.VISIBLE);
            mColorView.setVisibility(View.INVISIBLE);
        } else {
            mCurrentSpaceView.setVisibility(View.INVISIBLE);
            mCurrentSpaceImageView.setVisibility(View.INVISIBLE);
            mColorView.setVisibility(View.INVISIBLE);
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
