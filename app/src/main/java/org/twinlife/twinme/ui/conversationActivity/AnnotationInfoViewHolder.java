/*
 *  Copyright (c) 2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.conversationActivity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.CircularImageDescriptor;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.CircularImageView;

public class AnnotationInfoViewHolder extends RecyclerView.ViewHolder {
    private static final String LOG_TAG = "AnnotationInfo...";
    private static final boolean DEBUG = false;

    private final CircularImageView mAvatarView;
    private final TextView mNameView;
    private final ImageView mReactionView;
    private final View mSeparatorView;

    public AnnotationInfoViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = Design.SECTION_HEIGHT;
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Color.TRANSPARENT);

        mAvatarView = view.findViewById(R.id.annotation_info_item_avatar_view);

        mNameView = view.findViewById(R.id.annotation_info_item_name_view);
        Design.updateTextFont(mNameView, Design.FONT_REGULAR34);
        mNameView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mReactionView = view.findViewById(R.id.annotation_info_item_reaction_image_view);

        mSeparatorView = view.findViewById(R.id.annotation_info_item_separator_view);
        mSeparatorView.setBackgroundColor(Design.SEPARATOR_COLOR);
    }

    public void onBind(Context context, UIAnnotation annotation, int backgroundColor, boolean hideSeparator) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBind: annotation=" + annotation);
        }

        itemView.setBackgroundColor(backgroundColor);

        mAvatarView.setImage(context, null,
                new CircularImageDescriptor(annotation.getAvatar(), 0.5f, 0.5f, 0.5f));

        mNameView.setText(annotation.getName());

        Drawable drawable = ResourcesCompat.getDrawable(context.getResources(), annotation.getReaction().getImage(), null);
        mReactionView.setImageDrawable(drawable);
        mReactionView.setColorFilter(annotation.getReaction().getColorFilter());

        if (hideSeparator) {
            mSeparatorView.setVisibility(View.GONE);
        } else {
            mSeparatorView.setVisibility(View.VISIBLE);
        }
    }

    public void onViewRecycled() {

    }
}
