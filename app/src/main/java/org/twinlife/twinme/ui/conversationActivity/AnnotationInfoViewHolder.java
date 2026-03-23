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
import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinme.skin.CircularImageDescriptor;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.CircularImageView;
import org.twinlife.twinme.utils.CommonUtils;

public class AnnotationInfoViewHolder extends RecyclerView.ViewHolder {
    private static final String LOG_TAG = "AnnotationInfo...";
    private static final boolean DEBUG = false;

    private static final int DATE_COLOR = Color.argb(255, 119, 138, 159);

    private static final float DESIGN_AVATAR_SIZE = 84;
    private static final float DESIGN_REACTION_WIDTH = 46;
    private static final float DESIGN_REACTION_HEIGHT = 56;
    private static final float DESIGN_DATE_WIDTH = 200;
    private static final float DESIGN_HORIZONTAL_MARGIN = 32;

    private final CircularImageView mAvatarView;
    private final TextView mNameView;
    private final ImageView mReactionView;
    private final TextView mDateTextView;
    private final View mSeparatorView;

    public AnnotationInfoViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = Design.SECTION_HEIGHT;
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Color.TRANSPARENT);

        mAvatarView = view.findViewById(R.id.annotation_info_item_avatar_view);

        layoutParams = mAvatarView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_AVATAR_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_AVATAR_SIZE * Design.HEIGHT_RATIO);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mAvatarView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);

        mNameView = view.findViewById(R.id.annotation_info_item_name_view);
        Design.updateTextFont(mNameView, Design.FONT_REGULAR30);
        mNameView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mNameView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);

        mDateTextView = view.findViewById(R.id.annotation_info_item_date_reaction_view);
        Design.updateTextFont(mDateTextView, Design.FONT_MEDIUM28);
        mDateTextView.setTextColor(DATE_COLOR);

        layoutParams = mDateTextView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_DATE_WIDTH * Design.WIDTH_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mDateTextView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);

        mReactionView = view.findViewById(R.id.annotation_info_item_reaction_image_view);

        layoutParams = mReactionView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_REACTION_WIDTH * Design.WIDTH_RATIO);
        layoutParams.height = (int) (DESIGN_REACTION_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mReactionView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);

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

        if (annotation.getAnnotationType() == ConversationService.AnnotationType.LIKE && annotation.getReaction() != null) {
            mReactionView.setVisibility(View.VISIBLE);
            mDateTextView.setVisibility(View.GONE);
            Drawable drawable = ResourcesCompat.getDrawable(context.getResources(), annotation.getReaction().getImage(), null);
            mReactionView.setImageDrawable(drawable);
            mReactionView.setColorFilter(annotation.getReaction().getColorFilter());
        } else {
            mReactionView.setVisibility(View.GONE);
            mDateTextView.setVisibility(View.VISIBLE);
            
            long timestamp = annotation.getTimestamp();
            if (timestamp > 0) {
                mDateTextView.setText(CommonUtils.formatItemInterval(itemView.getContext(), timestamp));
            } else {
                mDateTextView.setText("-");
            }
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
