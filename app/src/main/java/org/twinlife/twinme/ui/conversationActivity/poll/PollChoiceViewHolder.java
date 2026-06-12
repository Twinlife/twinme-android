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

    private static final int DESIGN_TRACK_COLOR = Color.rgb(38, 209, 160);

    public static final float DESIGN_ITEM_HEIGHT = 80f;
    private static final float DESIGN_CHECK_MARGIN = 24f;
    private static final float DESIGN_SELECTED_SIZE = 44f;
    private static final float DESIGN_CHECK_SIZE = 24f;
    private static final float DESIGN_HORIZONTAL_MARGIN = 14f;
    private static final float DESIGN_AVATAR_SIZE = 30f;
    private static final float DESIGN_TRACK_TOP_MARGIN = 8f;
    private static final float DESIGN_TRACK_BOTTOM_MARGIN = 18f;
    private static final float DESIGN_TRACK_HEIGHT = 8f;

    private final TextView mChoiceView;
    private final TextView mCounterView;
    private final CircularImageView mAvatarOneImageView;
    private final CircularImageView mAvatarTwoImageView;
    private final ImageView mSelectedImageView;
    private final View mTrackView;
    private final View mResultTrackView;

    public PollChoiceViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        view.setMinimumHeight((int) (DESIGN_ITEM_HEIGHT * Design.HEIGHT_RATIO));
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Color.TRANSPARENT);

        View selectedView = view.findViewById(R.id.poll_choice_item_selected_view);

        layoutParams = selectedView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_SELECTED_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_SELECTED_SIZE * Design.HEIGHT_RATIO);
        selectedView.setLayoutParams(layoutParams);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) selectedView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_CHECK_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_CHECK_MARGIN * Design.HEIGHT_RATIO);
        selectedView.setLayoutParams(marginLayoutParams);

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

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mChoiceView.getLayoutParams();
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

        mTrackView = view.findViewById(R.id.poll_choice_item_track_view);
        layoutParams = mTrackView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_TRACK_HEIGHT * Design.HEIGHT_RATIO);
        mTrackView.setLayoutParams(layoutParams);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mTrackView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_TRACK_TOP_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_TRACK_BOTTOM_MARGIN * Design.HEIGHT_RATIO);
        mTrackView.setLayoutParams(marginLayoutParams);

        GradientDrawable trackDrawable = new GradientDrawable();
        trackDrawable.setShape(GradientDrawable.RECTANGLE);
        trackDrawable.setColor(Design.SEPARATOR_COLOR);
        trackDrawable.setCornerRadius(DESIGN_TRACK_HEIGHT * Design.HEIGHT_RATIO * 0.5f);
        mTrackView.setBackground(trackDrawable);

        mResultTrackView = view.findViewById(R.id.poll_choice_item_result_track_view);
        layoutParams = mResultTrackView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_TRACK_HEIGHT * Design.HEIGHT_RATIO);
        mResultTrackView.setLayoutParams(layoutParams);

        GradientDrawable resultTrackDrawable = new GradientDrawable();
        resultTrackDrawable.setShape(GradientDrawable.RECTANGLE);
        resultTrackDrawable.setColor(DESIGN_TRACK_COLOR);
        resultTrackDrawable.setCornerRadius(DESIGN_TRACK_HEIGHT * Design.HEIGHT_RATIO * 0.5f);
        mResultTrackView.setBackground(resultTrackDrawable);
    }

    public void onBind(@NonNull UIPollResult pollResult, int textColor, int maxResult) {
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

        mTrackView.post(() -> {
            ViewGroup.LayoutParams layoutParams = mResultTrackView.getLayoutParams();
            if (maxResult > 0) {
                layoutParams.width = (int) ((pollResult.getCount() / (float) maxResult) * mTrackView.getWidth());
            } else {
                layoutParams.width = 0;
            }
            mResultTrackView.setLayoutParams(layoutParams);
        });

        mChoiceView.setTextColor(textColor);
        mCounterView.setTextColor(textColor);
    }

    public void onViewRecycled() {

    }
}
