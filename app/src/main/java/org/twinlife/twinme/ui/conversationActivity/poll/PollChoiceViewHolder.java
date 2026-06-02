/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (fabrice.trescartes@twin.life)
 */

package org.twinlife.twinme.ui.conversationActivity.poll;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.CircularImageDescriptor;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.CircularImageView;

public class PollChoiceViewHolder extends RecyclerView.ViewHolder {
    private static final String LOG_TAG = "PollChoiceViewHolder";
    private static final boolean DEBUG = false;

    public static final float DESIGN_ITEM_HEIGHT = 80f;
    private static final float DESIGN_SELECTED_SIZE = 44f;
    private static final float DESIGN_CHECK_SIZE = 24f;
    private static final float DESIGN_HORIZONTAL_MARGIN = 14f;
    private static final float DESIGN_AVATAR_SIZE = 30f;
    private static final float DESIGN_SEPARATOR_HEIGHT = 4f;

    private final TextView mChoiceView;
    private final TextView mCounterView;
    private final CircularImageView mAvatarOneImageView;
    private final CircularImageView mAvatarTwoImageView;
    private final ImageView mSelectedImageView;
    private final GradientDrawable mSeparatorDrawable;

    public PollChoiceViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int) (DESIGN_ITEM_HEIGHT * Design.HEIGHT_RATIO);
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Color.TRANSPARENT);

        View selectedView = view.findViewById(R.id.poll_choice_item_selected_view);

        layoutParams = selectedView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_SELECTED_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_SELECTED_SIZE * Design.HEIGHT_RATIO);
        selectedView.setLayoutParams(layoutParams);

        GradientDrawable selectDrawable = new GradientDrawable();
        selectDrawable.setShape(GradientDrawable.OVAL);
        selectDrawable.setColor(Design.GREY_ITEM_COLOR);
        selectedView.setBackground(selectDrawable);

        mSelectedImageView = view.findViewById(R.id.poll_choice_item_selected_image);
        mSelectedImageView.setColorFilter(Color.BLACK);

        layoutParams = mSelectedImageView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_CHECK_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_CHECK_SIZE * Design.HEIGHT_RATIO);
        mSelectedImageView.setLayoutParams(layoutParams);

        mChoiceView = view.findViewById(R.id.poll_choice_item_choice_view);
        Design.updateTextFont(mChoiceView, Design.FONT_MEDIUM32);
        mChoiceView.setTextColor(Design.FONT_COLOR_DEFAULT);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mChoiceView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);

        mCounterView = view.findViewById(R.id.poll_choice_item_counter_view);
        Design.updateTextFont(mCounterView, Design.FONT_MEDIUM32);
        mCounterView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mCounterView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);

        mAvatarOneImageView = view.findViewById(R.id.poll_choice_item_avatar_one_view);

        layoutParams = mAvatarOneImageView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_AVATAR_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_AVATAR_SIZE * Design.HEIGHT_RATIO);
        mAvatarOneImageView.setLayoutParams(layoutParams);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mAvatarOneImageView.getLayoutParams();
        marginLayoutParams.rightMargin = (int) -(DESIGN_AVATAR_SIZE * Design.HEIGHT_RATIO * 0.5f);

        mAvatarTwoImageView = view.findViewById(R.id.poll_choice_item_avatar_two_view);
        layoutParams = mAvatarTwoImageView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_AVATAR_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_AVATAR_SIZE * Design.HEIGHT_RATIO);
        mAvatarTwoImageView.setLayoutParams(layoutParams);

        View separatorView = view.findViewById(R.id.poll_choice_item_separator_view);
        layoutParams = separatorView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_SEPARATOR_HEIGHT * Design.HEIGHT_RATIO);
        separatorView.setLayoutParams(layoutParams);

        mSeparatorDrawable = new GradientDrawable();
        mSeparatorDrawable.setShape(GradientDrawable.RECTANGLE);
        mSeparatorDrawable.setColor(Design.FONT_COLOR_DEFAULT);
        mSeparatorDrawable.setCornerRadius(DESIGN_SEPARATOR_HEIGHT * Design.HEIGHT_RATIO * 0.5f);
        separatorView.setBackground(mSeparatorDrawable);
    }

    public void onBind(@NonNull UIPollResult pollResult, int textColor) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBind");
        }

        mChoiceView.setText(pollResult.getChoice().label);

        if (pollResult.isSelected()) {
            mSelectedImageView.setVisibility(View.VISIBLE);
        } else {
            mSelectedImageView.setVisibility(View.INVISIBLE);
        }

        mCounterView.setText(String.valueOf(pollResult.getCount()));

        mAvatarOneImageView.setVisibility(View.INVISIBLE);
        mAvatarTwoImageView.setVisibility(View.INVISIBLE);

        if (pollResult.getPollResultVoters().size() > 1) {
            Bitmap avatar = pollResult.getPollResultVoters().get(1).getAvatar();
            if (avatar != null) {
                mAvatarOneImageView.setVisibility(View.VISIBLE);
                mAvatarOneImageView.setImage(mAvatarOneImageView.getContext(), null,
                        new CircularImageDescriptor(avatar, 0.5f, 0.5f, 0.5f));
            }
        }

        if (!pollResult.getPollResultVoters().isEmpty()) {
            Bitmap avatar = pollResult.getPollResultVoters().get(0).getAvatar();
            if (avatar != null) {
                mAvatarTwoImageView.setVisibility(View.VISIBLE);
                mAvatarTwoImageView.setImage(mAvatarTwoImageView.getContext(), null,
                        new CircularImageDescriptor(avatar, 0.5f, 0.5f, 0.5f));
            }
        }

        mChoiceView.setTextColor(textColor);
        mCounterView.setTextColor(textColor);
        mSeparatorDrawable.setColor(textColor);
    }

    public void onViewRecycled() {

    }
}
