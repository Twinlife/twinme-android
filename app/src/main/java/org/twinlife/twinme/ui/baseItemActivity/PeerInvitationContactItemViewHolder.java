/*
 *  Copyright (c) 2020-2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.imageview.ShapeableImageView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

import java.util.ArrayList;
import java.util.List;

class PeerInvitationContactItemViewHolder extends PeerItemViewHolder {

    private static final float DESIGN_AVATAR_SIZE = 80f;
    private static final float DESIGN_LINE_HEIGHT = 4f;
    private static final float DESIGN_ICON_CONTAINER_SIZE = 60f;
    private static final float DESIGN_ICON_SIZE = 32f;
    private static final float DESIGN_AVATAR_MARGIN = 24f;
    private static final float DESIGN_AVATAR_TOP_MARGIN = 20f;
    private static final float DESIGN_MESSAGE_MARGIN = 20f;
    private static final float DESIGN_ACTION_HEIGHT = 60f;

    private static final int DESIGN_LINE_DASH_LONG_LENGTH = 8;
    private static final int DESIGN_LINE_DASH_SHORT_LENGTH = 4;
    private static final int DESIGN_LINE_DASH_SPACING = 6;
    private static final int DESIGN_LINE_DASH_WIDTH = 3;

    private final View mInvitationContainer;
    private final TextView mInvitationView;
    private final GradientDrawable mGradientDrawable;
    private final ShapeableImageView mLeftAvatarView;
    private final ShapeableImageView mRightAvatarView;

    PeerInvitationContactItemViewHolder(BaseItemActivity baseItemActivity, View view, boolean allowClick, boolean allowLongClick) {

        super(baseItemActivity, view,
                R.id.base_item_activity_peer_invitation_contact_item_layout_container,
                R.id.base_item_activity_peer_invitation_contact_item_avatar,
                R.id.base_item_activity_peer_invitation_contact_item_overlay_view,
                R.id.base_item_activity_peer_invitation_contact_item_selected_view,
                R.id.base_item_activity_peer_invitation_contact_item_selected_image_view);

        mInvitationContainer = view.findViewById(R.id.base_item_activity_peer_invitation_contact_item_view);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mInvitationContainer.getLayoutParams();
        if (baseItemActivity.displayPeerItemAvatar()) {
            marginLayoutParams.setMarginStart(Design.PEER_CONTENT_CONVERSATION_MARGIN + Design.PEER_AVATAR_CONVERSATION_MARGIN + BaseItemActivity.AVATAR_HEIGHT);
        } else {
            marginLayoutParams.setMarginStart(Design.PEER_AVATAR_CONVERSATION_MARGIN);
        }
        mInvitationContainer.setLayoutParams(marginLayoutParams);

        mGradientDrawable = new GradientDrawable();
        mGradientDrawable.mutate();
        mGradientDrawable.setColor(Design.GREY_ITEM_COLOR);
        mGradientDrawable.setShape(GradientDrawable.RECTANGLE);
        mInvitationContainer.setBackground(mGradientDrawable);
        mGradientDrawable.setStroke(Design.BORDER_WIDTH, Color.TRANSPARENT);
        mInvitationContainer.setClickable(false);

        View avatarContainerView = view.findViewById(R.id.base_item_activity_peer_invitation_contact_item_avatar_container_view);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) avatarContainerView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_AVATAR_TOP_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.leftMargin = (int) (DESIGN_AVATAR_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_AVATAR_MARGIN * Design.WIDTH_RATIO);
        avatarContainerView.setLayoutParams(marginLayoutParams);

        ColorStateList colorStateList = new ColorStateList(
                new int[][]{
                        new int[]{}
                },
                new int[]{
                        Design.GREY_COLOR
                }
        );

        mLeftAvatarView = view.findViewById(R.id.base_item_activity_peer_invitation_contact_item_left_avatar_view);
        mLeftAvatarView.setStrokeColor(colorStateList);

        ViewGroup.LayoutParams layoutParams = mLeftAvatarView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_AVATAR_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_AVATAR_SIZE * Design.HEIGHT_RATIO);
        mLeftAvatarView.setLayoutParams(layoutParams);

        mRightAvatarView = view.findViewById(R.id.base_item_activity_peer_invitation_contact_item_right_avatar_view);
        mRightAvatarView.setStrokeColor(colorStateList);

        layoutParams = mRightAvatarView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_AVATAR_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_AVATAR_SIZE * Design.HEIGHT_RATIO);
        mRightAvatarView.setLayoutParams(layoutParams);

        View lineLeftView = view.findViewById(R.id.base_item_activity_peer_invitation_contact_item_line_left_view);

        layoutParams = lineLeftView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_LINE_HEIGHT * Design.HEIGHT_RATIO);
        lineLeftView.setLayoutParams(layoutParams);

        View lineRightView = view.findViewById(R.id.base_item_activity_peer_invitation_contact_item_line_right_view);

        layoutParams = lineRightView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_LINE_HEIGHT * Design.HEIGHT_RATIO);
        lineRightView.setLayoutParams(layoutParams);

        float dp = Resources.getSystem().getDisplayMetrics().density;

        Paint dashPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dashPaint.setColor(Design.GREY_COLOR);
        dashPaint.setStyle(Paint.Style.STROKE);
        dashPaint.setStrokeWidth(dp * DESIGN_LINE_DASH_WIDTH);
        dashPaint.setStrokeCap(Paint.Cap.ROUND);
        dashPaint.setPathEffect(new DashPathEffect(new float[]{dp * DESIGN_LINE_DASH_LONG_LENGTH, dp * DESIGN_LINE_DASH_SPACING, dp * DESIGN_LINE_DASH_SHORT_LENGTH, dp * DESIGN_LINE_DASH_SPACING}, 0));
        ShapeDrawable lineLeftDrawable = new ShapeDrawable() {
            @Override
            public void draw(Canvas canvas) {
                float startY = getBounds().height() * 0.5f;
                canvas.drawLine(0, startY, getBounds().width(), startY, dashPaint);
            }
        };
        lineLeftView.setBackground(lineLeftDrawable);

        ShapeDrawable lineRightDrawable = new ShapeDrawable() {
            @Override
            public void draw(Canvas canvas) {
                float startY = getBounds().height() * 0.5f;
                canvas.drawLine(getBounds().width(), startY, 0, startY, dashPaint);
            }
        };
        lineRightView.setBackground(lineRightDrawable);

        View iconContainerView = view.findViewById(R.id.base_item_activity_peer_invitation_contact_item_icon_view);

        layoutParams = iconContainerView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_ICON_CONTAINER_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_ICON_CONTAINER_SIZE * Design.HEIGHT_RATIO);
        iconContainerView.setLayoutParams(layoutParams);

        ImageView iconView = view.findViewById(R.id.base_item_activity_peer_invitation_contact_item_icon_image_view);

        layoutParams = iconView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_ICON_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_ICON_SIZE * Design.HEIGHT_RATIO);
        iconView.setLayoutParams(layoutParams);

        iconView.setColorFilter(Design.GREY_COLOR);

        GradientDrawable iconContainerBackground = new GradientDrawable();
        iconContainerBackground.mutate();
        iconContainerBackground.setColor(Design.WHITE_COLOR);
        iconContainerBackground.setShape(GradientDrawable.OVAL);
        iconContainerBackground.setStroke(Design.BORDER_WIDTH, Design.GREY_COLOR);
        iconContainerView.setBackground(iconContainerBackground);

        mInvitationView = view.findViewById(R.id.base_item_activity_peer_invitation_contact_item_message_view);
        Design.updateTextFont(mInvitationView, Design.FONT_MEDIUM32);
        mInvitationView.setTextColor(getBaseItemActivity().getCustomAppearance().getPeerMessageTextColor());

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mInvitationView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_AVATAR_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_AVATAR_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.topMargin = (int) (DESIGN_MESSAGE_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_MESSAGE_MARGIN * Design.HEIGHT_RATIO);
        mInvitationView.setLayoutParams(marginLayoutParams);

        View actionView = view.findViewById(R.id.base_item_activity_peer_invitation_contact_item_action_view);

        layoutParams = actionView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_ACTION_HEIGHT * Design.HEIGHT_RATIO);
        actionView.setLayoutParams(layoutParams);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) actionView.getLayoutParams();
        marginLayoutParams.bottomMargin = (int) (DESIGN_MESSAGE_MARGIN * Design.HEIGHT_RATIO);

        GradientDrawable actionBackground = new GradientDrawable();
        actionBackground.mutate();
        actionBackground.setColor(Design.GREY_COLOR);
        actionBackground.setShape(GradientDrawable.RECTANGLE);
        actionBackground.setCornerRadius(DESIGN_ACTION_HEIGHT * Design.HEIGHT_RATIO * 0.5f);
        actionView.setBackground(actionBackground);

        TextView actionTextView = view.findViewById(R.id.base_item_activity_peer_invitation_contact_item_action_text_view);
        Design.updateTextFont(actionTextView, Design.FONT_MEDIUM34);
        actionTextView.setTextColor(Color.WHITE);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) actionTextView.getLayoutParams();
        marginLayoutParams.leftMargin = Design.TEXT_MARGIN;
        marginLayoutParams.rightMargin = Design.TEXT_MARGIN;

        if (allowClick) {
            mInvitationContainer.setOnClickListener(v -> {

                if (getBaseItemActivity().isSelectItemMode()) {
                    onContainerClick();
                    return;
                }

                PeerInvitationContactItem invitation = (PeerInvitationContactItem) getItem();
                invitation.onClickInvitation();
            });
        }
        if (allowLongClick) {
            mInvitationContainer.setOnLongClickListener(v -> {
                baseItemActivity.onItemLongPress(getItem());
                return true;
            });
        }
    }

    @Override
    void onBind(Item item) {

        if (!(item instanceof PeerInvitationContactItem)) {
            return;
        }
        super.onBind(item);

        PeerInvitationContactItem invitation = (PeerInvitationContactItem) item;

        mGradientDrawable.setCornerRadii(getCornerRadii());
        mGradientDrawable.setColor(getBaseItemActivity().getCustomAppearance().getPeerMessageBackgroundColor());
        if (getBaseItemActivity().getCustomAppearance().getPeerMessageBorderColor() != Color.TRANSPARENT) {
            mGradientDrawable.setStroke(Design.BORDER_WIDTH, getBaseItemActivity().getCustomAppearance().getPeerMessageBorderColor());
        }

        // Get a possible avatar image that depends on the peer twincode.
        Bitmap avatar = invitation.getAvatar();
        if (avatar != null) {
            mRightAvatarView.setImageBitmap(avatar);
        }

        mLeftAvatarView.setImageBitmap(getBaseItemActivity().getIdentityAvatar());

        mInvitationView.setText(String.format(getString(R.string.accept_invitation_view_message), invitation.getName()));

        if (isMenuOpen() && isSelectedItem(item.getDescriptorId())) {
            if (getBaseItemActivity().getCustomAppearance().getPeerMessageBackgroundColor() == Color.WHITE) {
                mGradientDrawable.setColor(Design.GREY_ITEM_COLOR);
            }
        }
        if (!getBaseItemActivity().displayPeerItemAvatar()) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mInvitationContainer.getLayoutParams();
            int leftMargin = Design.PEER_AVATAR_CONVERSATION_MARGIN;
            if (getBaseItemActivity().isSelectItemMode()) {
                marginLayoutParams.setMarginStart(leftMargin + BaseItemViewHolder.CHECKBOX_MARGIN + BaseItemViewHolder.CHECKBOX_HEIGHT);
            } else {
                marginLayoutParams.setMarginStart(leftMargin);
            }
            mInvitationContainer.setLayoutParams(marginLayoutParams);
        }
    }

    @Override
    List<View> clickableViews() {

        return new ArrayList<View>() {
            {
                add(getContainer());
            }
        };
    }

    @Override
    void onViewRecycled() {

        mInvitationView.setText(null);
    }
}
