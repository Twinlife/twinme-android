/*
 *  Copyright (c) 2020-2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Auguste Hatton (Auguste.Hatton@twin.life)
 */

package org.twinlife.twinme.ui.profiles;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.CircularImageDescriptor;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.CircularImageView;

public class UIProfileViewHolder extends RecyclerView.ViewHolder {
    private static final String LOG_TAG = "UIProfileViewHolder";
    private static final boolean DEBUG = false;

    private static final float DESIGN_ITEM_VIEW_HEIGHT = 126f;
    private static final int ITEM_VIEW_HEIGHT;

    static {
        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
    }

    private static final int PROFILE_VIEW_COLOR = Color.argb(255, 179, 179, 179);

    private final CircularImageView mAvatarView;
    private final TextView mProfileView;
    private final View mSeparatorView;

    UIProfileViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = ITEM_VIEW_HEIGHT;
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Design.WHITE_COLOR);

        mAvatarView = view.findViewById(R.id.profile_fragment_profile_item_avatar_view);

        mProfileView = view.findViewById(R.id.profile_fragment_profile_item_name_view);
        Design.updateTextFont(mProfileView, Design.FONT_REGULAR34);
        mProfileView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mSeparatorView = view.findViewById(R.id.profile_fragment_profile_item_separator_view);
        mSeparatorView.setBackgroundColor(Design.SEPARATOR_COLOR);
    }

    public void onBind(Context context, UIProfile uiProfile, boolean isActiveProfile, boolean hideSeparator) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBind: profile=" + uiProfile + " isActiveProfile=" + isActiveProfile);
        }

        if (uiProfile.getName() != null) {
            SpannableStringBuilder spannableProfile = new SpannableStringBuilder();
            spannableProfile.append(uiProfile.getName());
            spannableProfile.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_DEFAULT), 0, spannableProfile.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            if (isActiveProfile) {
                spannableProfile.append("\n");
                int startActiveTitle = spannableProfile.length();
                spannableProfile.append(context.getString(R.string.profile_fragment_default_profile_title));
                spannableProfile.setSpan(new ForegroundColorSpan(PROFILE_VIEW_COLOR), startActiveTitle, spannableProfile.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannableProfile.setSpan(new RelativeSizeSpan(0.8f), startActiveTitle, spannableProfile.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            mProfileView.setText(spannableProfile);
        }

        mAvatarView.setImage(context, null,
                new CircularImageDescriptor(uiProfile.getAvatar(), 0.5f, 0.5f, 0.5f));

        if (hideSeparator) {
            mSeparatorView.setVisibility(View.GONE);
        } else {
            mSeparatorView.setVisibility(View.VISIBLE);
        }

        updateFont();
        updateColor();
    }

    public void onViewRecycled() {

    }

    private void updateFont() {

        Design.updateTextFont(mProfileView, Design.FONT_REGULAR34);
    }

    private void updateColor() {

        itemView.setBackgroundColor(Design.WHITE_COLOR);
        mProfileView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mSeparatorView.setBackgroundColor(Design.SEPARATOR_COLOR);
    }
}
