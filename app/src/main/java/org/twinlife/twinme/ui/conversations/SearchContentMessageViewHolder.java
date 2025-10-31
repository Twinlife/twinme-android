/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.conversations;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.services.AbstractTwinmeService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.mainActivity.UIConversation;
import org.twinlife.twinme.utils.AvatarView;
import org.twinlife.twinme.utils.RoundedView;
import org.twinlife.twinme.utils.Utils;

import java.util.Arrays;

public class SearchContentMessageViewHolder extends RecyclerView.ViewHolder {

    private static final float DESIGN_ITEM_SMALL_ROUND_CORNER_RADIUS = 8f;
    private static final float DESIGN_ITEM_LARGE_ROUND_CORNER_RADIUS = 38f;
    private static final float DESIGN_AVATAR_MARGIN = 16f;
    private static final float DESIGN_AVATAR_HEIGHT = 26f;
    private static final float DESIGN_MESSAGE_MARGIN = 20f;
    private static final float DESIGN_MESSAGE_ITEM_TEXT_HEIGHT_PADDING = 10f;
    private static final float DESIGN_MESSAGE_ITEM_TEXT_WIDTH_PADDING = 32f;
    private static final float DESIGN_MESSAGE_MAX_WIDTH = 408f;

    private static final int MESSAGE_ITEM_TEXT_HEIGHT_PADDING;
    private static final int MESSAGE_ITEM_TEXT_WIDTH_PADDING;
    private static final float ITEM_SMALL_RADIUS;
    private static final float ITEM_LARGE_RADIUS;
    private static final int MESSAGE_MARGIN;
    private static final int MESSAGE_MAX_WIDTH;
    private static final int AVATAR_SIZE;
    private static final int AVATAR_MARGIN;

    static {
        ITEM_SMALL_RADIUS = DESIGN_ITEM_SMALL_ROUND_CORNER_RADIUS * Design.HEIGHT_RATIO;
        ITEM_LARGE_RADIUS = DESIGN_ITEM_LARGE_ROUND_CORNER_RADIUS * Design.HEIGHT_RATIO;
        MESSAGE_ITEM_TEXT_HEIGHT_PADDING = (int) (DESIGN_MESSAGE_ITEM_TEXT_HEIGHT_PADDING * Design.HEIGHT_RATIO);
        MESSAGE_ITEM_TEXT_WIDTH_PADDING = (int) (DESIGN_MESSAGE_ITEM_TEXT_WIDTH_PADDING * Design.WIDTH_RATIO);
        MESSAGE_MARGIN = (int) (DESIGN_MESSAGE_MARGIN * Design.WIDTH_RATIO);
        MESSAGE_MAX_WIDTH = (int) (DESIGN_MESSAGE_MAX_WIDTH * Design.WIDTH_RATIO);
        AVATAR_SIZE = (int) (DESIGN_AVATAR_HEIGHT * Design.HEIGHT_RATIO);
        AVATAR_MARGIN = (int) (DESIGN_AVATAR_MARGIN * Design.WIDTH_RATIO);
    }

    @NonNull
    private final AbstractTwinmeService mService;

    private final RoundedView mNoAvatarView;
    private final AvatarView mAvatarView;
    private final TextView mMessageView;
    private final TextView mNameView;
    private final TextView mTimeView;

    private final GradientDrawable mGradientDrawable;

    public SearchContentMessageViewHolder(@NonNull AbstractTwinmeService service, @NonNull View view) {

        super(view);

        mService = service;

        view.setBackgroundColor(Design.WHITE_COLOR);

        View avatarContainerView = view.findViewById(R.id.search_content_message_item_avatar_container);

        ViewGroup.LayoutParams layoutParams = avatarContainerView.getLayoutParams();
        layoutParams.width = AVATAR_SIZE;
        layoutParams.height = AVATAR_SIZE;

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) avatarContainerView.getLayoutParams();
        marginLayoutParams.leftMargin = AVATAR_MARGIN;
        marginLayoutParams.rightMargin = AVATAR_MARGIN;
        marginLayoutParams.setMarginStart(AVATAR_MARGIN);
        marginLayoutParams.setMarginEnd(AVATAR_MARGIN);

        mNoAvatarView = view.findViewById(R.id.search_content_message_item_no_avatar);
        mNoAvatarView.setColor(Design.GREY_ITEM_COLOR);

        mAvatarView = view.findViewById(R.id.search_content_message_item_avatar);

        mMessageView = view.findViewById(R.id.search_content_message_item_message);
        mMessageView.setPadding(MESSAGE_ITEM_TEXT_WIDTH_PADDING, MESSAGE_ITEM_TEXT_HEIGHT_PADDING, MESSAGE_ITEM_TEXT_WIDTH_PADDING, MESSAGE_ITEM_TEXT_HEIGHT_PADDING);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mMessageView.getLayoutParams();
        marginLayoutParams.topMargin = MESSAGE_MARGIN;
        marginLayoutParams.bottomMargin = MESSAGE_MARGIN;

        mGradientDrawable = new GradientDrawable();
        mGradientDrawable.mutate();
        mGradientDrawable.setColor(Design.GREY_ITEM_COLOR);
        mGradientDrawable.setShape(GradientDrawable.RECTANGLE);
        mMessageView.setBackground(mGradientDrawable);

        Design.updateTextFont(mMessageView, Design.FONT_REGULAR32);
        mMessageView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mMessageView.setMaxWidth(MESSAGE_MAX_WIDTH);

        View infoView = view.findViewById(R.id.search_content_message_info);
        marginLayoutParams = (ViewGroup.MarginLayoutParams) infoView.getLayoutParams();
        marginLayoutParams.rightMargin = AVATAR_MARGIN;
        marginLayoutParams.setMarginEnd(MESSAGE_MARGIN);

        mNameView = view.findViewById(R.id.search_content_message_item_contact);
        Design.updateTextFont(mNameView, Design.FONT_MEDIUM28);
        mNameView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mTimeView = view.findViewById(R.id.search_content_message_item_time);
        Design.updateTextFont(mTimeView, Design.FONT_REGULAR28);
        mTimeView.setTextColor(Design.FONT_COLOR_GREY);
    }

    public void onBind(Context context, UIConversation uiConversation, String searchContent) {

        if (uiConversation.getContact().getAvatarId() == null) {
            mAvatarView.setImageBitmap(uiConversation.getAvatar());
            mNoAvatarView.setVisibility(View.VISIBLE);
        } else {
            mNoAvatarView.setVisibility(View.GONE);
            mService.getImageOrDefaultAvatar(uiConversation.getUIContact().getContact(), mAvatarView::setImageBitmap);
        }

        mNameView.setText(uiConversation.getName());
        mTimeView.setText(uiConversation.getMessageDate());

        float[] radii = new float[8];
        Arrays.fill(radii, ITEM_LARGE_RADIUS);
        radii[0] = ITEM_SMALL_RADIUS;
        radii[1] = ITEM_SMALL_RADIUS;
        mGradientDrawable.setCornerRadii(radii);

        if (uiConversation.isLocalDescriptor()) {
            mGradientDrawable.setColor(Design.getMainStyle());
            mMessageView.setTextColor(Color.WHITE);
        } else {
            mGradientDrawable.setColor(Design.GREY_ITEM_COLOR);
            mMessageView.setTextColor(Design.FONT_COLOR_DEFAULT);
        }

        String message = uiConversation.getLastMessage(context);
        message = message.replaceAll("(?i)" + searchContent, "~" + searchContent + "~");

        mMessageView.setText(Utils.formatText(message, (int) Design.FONT_REGULAR34.size));
    }

    public void onViewRecycled() {

    }
}