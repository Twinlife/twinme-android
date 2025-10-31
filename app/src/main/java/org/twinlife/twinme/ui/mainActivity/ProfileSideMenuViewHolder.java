/*
 *  Copyright (c) 2021-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.mainActivity;

import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.profiles.UIProfile;
import org.twinlife.twinme.utils.AvatarView;
import org.twinlife.twinme.utils.CommonUtils;

public class ProfileSideMenuViewHolder extends RecyclerView.ViewHolder {
    private static final String LOG_TAG = "SpaceSideMenuViewHolder";
    private static final boolean DEBUG = false;

    private static final float DESIGN_ITEM_VIEW_HEIGHT = 160f;
    private static final int ITEM_VIEW_HEIGHT;
    private static final float DESIGN_ITEM_ROUND_CORNER_RADIUS_DP = 18f;

    static {
        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
    }

    private final AvatarView mAvatarView;
    private final TextView mNameView;
    private final View mActiveProfileView;
    private final GradientDrawable mGradientDrawable;

    ProfileSideMenuViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = ITEM_VIEW_HEIGHT;
        view.setLayoutParams(layoutParams);

        mAvatarView = view.findViewById(R.id.side_menu_profile_item_avatar_view);

        mActiveProfileView = view.findViewById(R.id.side_menu_profile_item_active_profile_view);
        mActiveProfileView.setVisibility(View.INVISIBLE);

        mGradientDrawable = new GradientDrawable();
        mGradientDrawable.mutate();
        mGradientDrawable.setColor(Design.BACKGROUND_COLOR_BLUE);
        mGradientDrawable.setShape(GradientDrawable.RECTANGLE);
        mActiveProfileView.setBackground(mGradientDrawable);

        mNameView = view.findViewById(R.id.side_menu_profile_item_name_text_view);
        Design.updateTextFont(mNameView, Design.FONT_REGULAR24);
        mNameView.setTextColor(Design.FONT_COLOR_DEFAULT);
    }

    public void onBind(UIProfile uiProfile, boolean isActiveProfile) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBind: profile=" + uiProfile);
        }

        float corner = DESIGN_ITEM_ROUND_CORNER_RADIUS_DP * Resources.getSystem().getDisplayMetrics().density;
        if (CommonUtils.isLayoutDirectionRTL()) {
            mGradientDrawable.setCornerRadii(new float[]{corner, corner, 0, 0, 0, 0, corner, corner});
        } else {
            mGradientDrawable.setCornerRadii(new float[]{0, 0, corner, corner, corner, corner, 0, 0});
        }

        mNameView.setText(uiProfile.getName());

        mAvatarView.setImageBitmap(uiProfile.getAvatar());
        mAvatarView.setVisibility(View.VISIBLE);

        mGradientDrawable.setColor(Design.getMainStyle());

        if (isActiveProfile) {
            mActiveProfileView.setVisibility(View.VISIBLE);
        } else {
            mActiveProfileView.setVisibility(View.INVISIBLE);
        }
    }

    public void onViewRecycled() {

    }
}