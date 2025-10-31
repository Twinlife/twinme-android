/*
 *  Copyright (c) 2020-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.spaces;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.AvatarView;

import java.util.Arrays;
import java.util.Date;

public class PreviewAppearanceViewHolder extends RecyclerView.ViewHolder {

    private static final float DESIGN_ITEM_BOTTOM_MARGIN = 12f;
    private static final float DESIGN_ITEM_SMALL_ROUND_CORNER_RADIUS = 8f;
    private static final float DESIGN_ITEM_LARGE_ROUND_CORNER_RADIUS = 38f;
    private static final float DESIGN_TIME_VIEW_HEIGHT = 50f;
    private static final float DESIGN_AVATAR_HEIGHT = 52f;
    private static final float DESIGN_MESSAGE_ITEM_TEXT_HEIGHT_PADDING = 10f;
    private static final float DESIGN_MESSAGE_ITEM_TEXT_WIDTH_PADDING = 32f;

    private static final int ITEM_BOTTOM_MARGIN;
    private static final float ITEM_SMALL_RADIUS;
    private static final float ITEM_LARGE_RADIUS;
    private static final int AVATAR_SIZE;
    private static final int TIME_VIEW_HEIGHT;
    private static final int MESSAGE_ITEM_TEXT_HEIGHT_PADDING;
    private static final int MESSAGE_ITEM_TEXT_WIDTH_PADDING;

    static {
        ITEM_BOTTOM_MARGIN = (int) (DESIGN_ITEM_BOTTOM_MARGIN * Design.HEIGHT_RATIO);
        ITEM_SMALL_RADIUS = DESIGN_ITEM_SMALL_ROUND_CORNER_RADIUS * Design.HEIGHT_RATIO;
        ITEM_LARGE_RADIUS = DESIGN_ITEM_LARGE_ROUND_CORNER_RADIUS * Design.HEIGHT_RATIO;
        TIME_VIEW_HEIGHT = (int) (DESIGN_TIME_VIEW_HEIGHT * Design.HEIGHT_RATIO);
        AVATAR_SIZE = (int) (DESIGN_AVATAR_HEIGHT * Design.HEIGHT_RATIO);
        MESSAGE_ITEM_TEXT_HEIGHT_PADDING = (int) (DESIGN_MESSAGE_ITEM_TEXT_HEIGHT_PADDING * Design.HEIGHT_RATIO);
        MESSAGE_ITEM_TEXT_WIDTH_PADDING = (int) (DESIGN_MESSAGE_ITEM_TEXT_WIDTH_PADDING * Design.WIDTH_RATIO);
    }

    private final ImageView mBackgroundView;
    private final TextView mTimeTextView;
    private final TextView mTextView;
    private final GradientDrawable mGradientDrawable;
    private final TextView mPeerTextView;
    private final GradientDrawable mPeerGradientDrawable;

    PreviewAppearanceViewHolder(@NonNull View view) {
        super(view);

        view.setBackgroundColor(Design.WHITE_COLOR);

        mBackgroundView = view.findViewById(R.id.preview_appearance_background_image_view);

        View timeView = view.findViewById(R.id.preview_appearance_time_view);
        ViewGroup.LayoutParams layoutParams = timeView.getLayoutParams();
        layoutParams.height = TIME_VIEW_HEIGHT;

        mTimeTextView = view.findViewById(R.id.preview_appearance_time_text);
        Design.updateTextFont(mTimeTextView, Design.FONT_MEDIUM26);
        mTimeTextView.setText(DateFormat.format("kk:mm", new Date()));

        View containerMessageView = view.findViewById(R.id.preview_appearance_message_item_layout_container);
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) containerMessageView.getLayoutParams();
        marginLayoutParams.bottomMargin = ITEM_BOTTOM_MARGIN;

        mTextView = view.findViewById(R.id.preview_appearance_message_item_text);
        mTextView.setPadding(MESSAGE_ITEM_TEXT_WIDTH_PADDING, MESSAGE_ITEM_TEXT_HEIGHT_PADDING, MESSAGE_ITEM_TEXT_WIDTH_PADDING, MESSAGE_ITEM_TEXT_HEIGHT_PADDING);
        Design.updateTextFont(mTextView, Design.FONT_REGULAR32);

        mGradientDrawable = new GradientDrawable();
        mGradientDrawable.mutate();
        mGradientDrawable.setColor(Design.getMainStyle());
        mGradientDrawable.setShape(GradientDrawable.RECTANGLE);
        mTextView.setBackground(mGradientDrawable);

        mPeerTextView = view.findViewById(R.id.preview_appearance_peer_message_item_text);
        Design.updateTextFont(mPeerTextView, Design.FONT_REGULAR32);
        mPeerTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mPeerTextView.setPadding(MESSAGE_ITEM_TEXT_WIDTH_PADDING, MESSAGE_ITEM_TEXT_HEIGHT_PADDING, MESSAGE_ITEM_TEXT_WIDTH_PADDING, MESSAGE_ITEM_TEXT_HEIGHT_PADDING);

        View peerContainerMessageView = view.findViewById(R.id.preview_appearance_peer_message_item_layout_container);
        marginLayoutParams = (ViewGroup.MarginLayoutParams) peerContainerMessageView.getLayoutParams();
        marginLayoutParams.bottomMargin = ITEM_BOTTOM_MARGIN;

        mPeerGradientDrawable = new GradientDrawable();
        mPeerGradientDrawable.mutate();
        mPeerGradientDrawable.setColor(Design.GREY_ITEM_COLOR);
        mPeerGradientDrawable.setShape(GradientDrawable.RECTANGLE);
        mPeerGradientDrawable.setStroke(Design.BORDER_WIDTH, Design.ITEM_BORDER_COLOR);
        mPeerTextView.setBackground(mPeerGradientDrawable);

        AvatarView avatarView = view.findViewById(R.id.preview_appearance_peer_message_item_avatar);

        layoutParams = avatarView.getLayoutParams();
        layoutParams.width = AVATAR_SIZE;
        layoutParams.height = AVATAR_SIZE;
        avatarView.setLayoutParams(layoutParams);

        Bitmap previewAvatarBitmap = BitmapFactory.decodeResource(view.getResources(), R.drawable.preview_avatar);
        avatarView.setImageBitmap(previewAvatarBitmap);
    }

    public void onBind(CustomAppearance customAppearance, Bitmap conversationBackground) {

        if (conversationBackground != null) {
            mBackgroundView.setImageBitmap(conversationBackground);
            mBackgroundView.setVisibility(View.VISIBLE);
        } else {
            itemView.setBackgroundColor(customAppearance.getConversationBackgroundColor());
            mBackgroundView.setVisibility(View.GONE);
        }

        mTimeTextView.setTextColor(customAppearance.getConversationBackgroundText());

        float[] peerRadii = new float[8];
        Arrays.fill(peerRadii, ITEM_LARGE_RADIUS);
        mPeerGradientDrawable.setCornerRadii(peerRadii);
        mPeerGradientDrawable.setColor(customAppearance.getPeerMessageBackgroundColor());
        mPeerGradientDrawable.setStroke(Design.BORDER_WIDTH, customAppearance.getPeerMessageBorderColor());

        float[] radii = new float[8];
        Arrays.fill(radii, ITEM_LARGE_RADIUS);
        radii[4] = ITEM_SMALL_RADIUS;
        radii[5] = ITEM_SMALL_RADIUS;
        mGradientDrawable.setCornerRadii(radii);
        mGradientDrawable.setColor(customAppearance.getMessageBackgroundColor());
        mGradientDrawable.setStroke(Design.BORDER_WIDTH, customAppearance.getMessageBorderColor());

        mPeerTextView.setTextColor(customAppearance.getPeerMessageTextColor());
        mTextView.setTextColor(customAppearance.getMessageTextColor());
    }

    public void onViewRecycled() {

    }
}
