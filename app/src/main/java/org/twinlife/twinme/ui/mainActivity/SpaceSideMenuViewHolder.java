/*
 *  Copyright (c) 2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.mainActivity;

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
import org.twinlife.twinme.ui.spaces.UISpace;
import org.twinlife.twinme.utils.CommonUtils;
import org.twinlife.twinme.utils.RoundedImageView;

import java.util.Arrays;

public class SpaceSideMenuViewHolder extends RecyclerView.ViewHolder {
    private static final String LOG_TAG = "SpaceSideMenuViewHolder";
    private static final boolean DEBUG = false;

    private static final float DESIGN_ITEM_VIEW_HEIGHT = 160f;
    private static final int ITEM_VIEW_HEIGHT;
    private static final float DESIGN_ITEM_ROUND_CORNER_RADIUS_DP = 18f;

    static {
        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
    }

    private final RoundedImageView mAvatarView;
    private final View mSpaceView;
    private final TextView mNameView;
    private final TextView mSpaceNameView;
    private final View mCurrentSpaceView;
    private final ImageView mNotificationMarkView;
    private final GradientDrawable mGradientDrawable;
    private final GradientDrawable mSpaceGradientDrawable;

    SpaceSideMenuViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = ITEM_VIEW_HEIGHT;
        view.setLayoutParams(layoutParams);

        mAvatarView = view.findViewById(R.id.side_menu_space_item_avatar_view);

        mSpaceView = view.findViewById(R.id.side_menu_space_item_no_avatar_view);

        mSpaceGradientDrawable = new GradientDrawable();
        mSpaceGradientDrawable.mutate();
        mSpaceGradientDrawable.setColor(Design.BACKGROUND_COLOR_GREY);
        mSpaceGradientDrawable.setShape(GradientDrawable.RECTANGLE);
        ViewCompat.setBackground(mSpaceView, mSpaceGradientDrawable);

        float corner = DESIGN_ITEM_ROUND_CORNER_RADIUS_DP * Resources.getSystem().getDisplayMetrics().density;
        float[] radii = new float[8];
        Arrays.fill(radii, corner);
        mSpaceGradientDrawable.setCornerRadii(radii);

        mNameView = view.findViewById(R.id.side_menu_space_item_no_avatar_text_view);
        mNameView.setTypeface(Design.FONT_BOLD44.typeface);
        mNameView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_BOLD44.size);
        mNameView.setTextColor(Color.WHITE);

        mCurrentSpaceView = view.findViewById(R.id.side_menu_space_item_current_space_view);
        mCurrentSpaceView.setVisibility(View.INVISIBLE);

        mGradientDrawable = new GradientDrawable();
        mGradientDrawable.mutate();
        mGradientDrawable.setColor(Design.getMainStyle());
        mGradientDrawable.setShape(GradientDrawable.RECTANGLE);
        ViewCompat.setBackground(mCurrentSpaceView, mGradientDrawable);

        mNotificationMarkView = view.findViewById(R.id.side_menu_space_item_notitification_view);
        mNotificationMarkView.setVisibility(View.INVISIBLE);

        mSpaceNameView = view.findViewById(R.id.side_menu_space_item_space_name_text_view);
        mSpaceNameView.setTypeface(Design.FONT_REGULAR24.typeface);
        mSpaceNameView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_REGULAR24.size);
        mSpaceNameView.setTextColor(Design.FONT_COLOR_DEFAULT);
    }

    public void onBind(UISpace uiSpace) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBind: space=" + uiSpace);
        }

        float corner = DESIGN_ITEM_ROUND_CORNER_RADIUS_DP * Resources.getSystem().getDisplayMetrics().density;
        if (CommonUtils.isLayoutDirectionRTL()) {
            mGradientDrawable.setCornerRadii(new float[]{corner, corner, 0, 0, 0, 0, corner, corner});
        } else {
            mGradientDrawable.setCornerRadii(new float[]{0, 0, corner, corner, corner, corner, 0, 0});
        }

        mSpaceNameView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mSpaceNameView.setText(uiSpace.getNameSpace());

        if (uiSpace.getAvatarSpace() != null) {
            float[] radii = new float[18];
            Arrays.fill(radii, corner);
            mAvatarView.setImageBitmap(uiSpace.getAvatarSpace(), radii);
            mAvatarView.setVisibility(View.VISIBLE);
            mSpaceView.setVisibility(View.GONE);

            mGradientDrawable.setColor(Design.getDefaultColor(uiSpace.getSpaceSettings().getStyle()));
        } else {
            mAvatarView.setVisibility(View.GONE);
            mSpaceView.setVisibility(View.VISIBLE);
            String name = uiSpace.getNameSpace();
            if (name != null && !name.isEmpty()) {
                mNameView.setText(name.substring(0, 1).toUpperCase());
            }
            mSpaceGradientDrawable.setColor(Design.getDefaultColor(uiSpace.getSpaceSettings().getStyle()));
            mGradientDrawable.setColor(Design.getDefaultColor(uiSpace.getSpaceSettings().getStyle()));
        }

        if (uiSpace.isCurrentSpace()) {
            mCurrentSpaceView.setVisibility(View.VISIBLE);
        } else {
            mCurrentSpaceView.setVisibility(View.INVISIBLE);
        }

        if (uiSpace.hasNotification()) {
            mNotificationMarkView.setVisibility(View.VISIBLE);
        } else {
            mNotificationMarkView.setVisibility(View.INVISIBLE);
        }
    }

    public void onViewRecycled() {

    }
}