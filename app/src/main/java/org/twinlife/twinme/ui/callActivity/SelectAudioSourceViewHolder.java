/*
 *  Copyright (c) 2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.callActivity;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class SelectAudioSourceViewHolder extends RecyclerView.ViewHolder {
    private static final String LOG_TAG = "SelectAudioSou...";
    private static final boolean DEBUG = false;

    private static final float DESIGN_ITEM_VIEW_HEIGHT = 124;
    private static final float DESIGN_CHECK_HEIGHT = 48;
    private static final float DESIGN_MARGIN = 32;
    private static final int ITEM_VIEW_HEIGHT;

    static {
        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
    }

    private final ImageView mIconView;
    private final TextView mTitleView;
    private final ImageView mCheckImageView;
    private final View mSeparatorView;

    public SelectAudioSourceViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = ITEM_VIEW_HEIGHT;
        view.setLayoutParams(layoutParams);

        mIconView = view.findViewById(R.id.call_activity_select_audio_source_image_view);
        mIconView.setColorFilter(Design.SHOW_ICON_COLOR);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mIconView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_MARGIN * Design.WIDTH_RATIO);

        mTitleView = view.findViewById(R.id.call_activity_select_audio_source_name_view);
        Design.updateTextFont(mTitleView, Design.FONT_REGULAR34);
        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mCheckImageView = view.findViewById(R.id.call_activity_select_audio_source_check_view);

        layoutParams = mCheckImageView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_CHECK_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mCheckImageView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_MARGIN * Design.WIDTH_RATIO);

        mSeparatorView = view.findViewById(R.id.call_activity_select_audio_source_separator_view);
        mSeparatorView.setBackgroundColor(Design.SEPARATOR_COLOR);
    }

    public void onBind(UIAudioSource uiAudioSource, boolean hideSeparator) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBind: uiAudioSource=" + uiAudioSource);
        }

        mTitleView.setText(uiAudioSource.getName());
        mIconView.setImageDrawable(ResourcesCompat.getDrawable(itemView.getResources(), uiAudioSource.getIcon(), null));

        if (uiAudioSource.isSelected()) {
            mCheckImageView.setVisibility(View.VISIBLE);
        } else {
            mCheckImageView.setVisibility(View.GONE);
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
