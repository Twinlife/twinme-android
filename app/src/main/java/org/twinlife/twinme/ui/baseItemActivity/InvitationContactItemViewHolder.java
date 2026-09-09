/*
 *  Copyright (c) 2018-2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.imageview.ShapeableImageView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

import java.util.ArrayList;
import java.util.List;

class InvitationContactItemViewHolder extends ItemViewHolder {

    private static final float DESIGN_AVATAR_SIZE = 80f;
    private static final float DESIGN_LINE_HEIGHT = 4f;
    private static final float DESIGN_ICON_CONTAINER_SIZE = 60f;
    private static final float DESIGN_ICON_SIZE = 32f;
    private static final float DESIGN_AVATAR_MARGIN = 24f;
    private static final float DESIGN_AVATAR_TOP_MARGIN = 20f;
    private static final float DESIGN_MESSAGE_MARGIN = 20f;

    private static final int DESIGN_LINE_DASH_LONG_LENGTH = 8;
    private static final int DESIGN_LINE_DASH_SHORT_LENGTH = 4;
    private static final int DESIGN_LINE_DASH_SPACING = 6;
    private static final int DESIGN_LINE_DASH_WIDTH = 3;

    private final TextView mInvitationView;
    private final GradientDrawable mGradientDrawable;
    private final ShapeableImageView mLeftAvatarView;
    private final ShapeableImageView mRightAvatarView;
    private final DeleteProgressView mDeleteView;
    private final View mInvitationContainer;

    InvitationContactItemViewHolder(BaseItemActivity baseItemActivity, View view, boolean allowClick, boolean allowLongClick) {

        super(baseItemActivity, view,
                R.id.base_item_activity_invitation_contact_item_layout_container,
                R.id.base_item_activity_invitation_contact_item_state_view,
                R.id.base_item_activity_invitation_contact_item_state_avatar_view,
                R.id.base_item_activity_invitation_contact_item_overlay_view,
                R.id.base_item_activity_invitation_contact_item_selected_view,
                R.id.base_item_activity_invitation_contact_item_selected_image_view,
                R.id.base_item_activity_invitation_contact_item_error_image_view);

        mInvitationContainer = view.findViewById(R.id.base_item_activity_invitation_contact_item_view);

        mInvitationContainer.setOnClickListener(v -> {
            if (getBaseItemActivity().isSelectItemMode()) {
                onContainerClick();
            }
        });

        mGradientDrawable = new GradientDrawable();
        mGradientDrawable.mutate();
        mGradientDrawable.setColor(Design.getMainStyle());
        mGradientDrawable.setShape(GradientDrawable.RECTANGLE);
        mInvitationContainer.setBackground(mGradientDrawable);
        mInvitationContainer.setClickable(false);

        View avatarContainerView = view.findViewById(R.id.base_item_activity_invitation_contact_item_avatar_container_view);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) avatarContainerView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_AVATAR_TOP_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.leftMargin = (int) (DESIGN_AVATAR_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_AVATAR_MARGIN * Design.WIDTH_RATIO);
        avatarContainerView.setLayoutParams(marginLayoutParams);

        mLeftAvatarView = view.findViewById(R.id.base_item_activity_invitation_contact_item_left_avatar_view);

        ViewGroup.LayoutParams layoutParams = mLeftAvatarView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_AVATAR_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_AVATAR_SIZE * Design.HEIGHT_RATIO);
        mLeftAvatarView.setLayoutParams(layoutParams);

        mRightAvatarView = view.findViewById(R.id.base_item_activity_invitation_contact_item_right_avatar_view);
        layoutParams = mRightAvatarView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_AVATAR_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_AVATAR_SIZE * Design.HEIGHT_RATIO);
        mRightAvatarView.setLayoutParams(layoutParams);

        View lineLeftView = view.findViewById(R.id.base_item_activity_invitation_contact_item_line_left_view);

        layoutParams = lineLeftView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_LINE_HEIGHT * Design.HEIGHT_RATIO);
        lineLeftView.setLayoutParams(layoutParams);

        View lineRightView = view.findViewById(R.id.base_item_activity_invitation_contact_item_line_right_view);

        layoutParams = lineRightView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_LINE_HEIGHT * Design.HEIGHT_RATIO);
        lineRightView.setLayoutParams(layoutParams);

        float dp = Resources.getSystem().getDisplayMetrics().density;

        Paint dashPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dashPaint.setColor(Color.WHITE);
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

        View iconContainerView = view.findViewById(R.id.base_item_activity_invitation_contact_item_icon_view);

        layoutParams = iconContainerView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_ICON_CONTAINER_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_ICON_CONTAINER_SIZE * Design.HEIGHT_RATIO);
        iconContainerView.setLayoutParams(layoutParams);

        ImageView iconView = view.findViewById(R.id.base_item_activity_invitation_contact_item_icon_image_view);

        layoutParams = iconView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_ICON_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_ICON_SIZE * Design.HEIGHT_RATIO);
        iconView.setLayoutParams(layoutParams);
        iconView.setColorFilter(Design.GREY_COLOR);

        GradientDrawable iconContainerBackground = new GradientDrawable();
        iconContainerBackground.mutate();
        iconContainerBackground.setColor(Design.GREY_ITEM_COLOR);
        iconContainerBackground.setShape(GradientDrawable.OVAL);
        iconContainerBackground.setStroke(Design.BORDER_WIDTH, Color.WHITE);
        iconContainerView.setBackground(iconContainerBackground);

        mInvitationView = view.findViewById(R.id.base_item_activity_invitation_contact_item_message_view);
        Design.updateTextFont(mInvitationView, Design.FONT_MEDIUM32);
        mInvitationView.setTextColor(Color.WHITE);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mInvitationView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_AVATAR_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_AVATAR_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.topMargin = (int) (DESIGN_MESSAGE_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_MESSAGE_MARGIN * Design.HEIGHT_RATIO);
        mInvitationView.setLayoutParams(marginLayoutParams);

        mDeleteView = view.findViewById(R.id.base_item_activity_invitation_contact_item_delete_view);

        if (allowLongClick) {
            mInvitationContainer.setOnLongClickListener(v -> {
                baseItemActivity.onItemLongPress(getItem());
                return true;
            });
        }
    }

    @Override
    void onBind(Item item) {

        if (!(item instanceof InvitationContactItem)) {
            return;
        }
        super.onBind(item);

        InvitationContactItem invitation = (InvitationContactItem) item;

        mGradientDrawable.setCornerRadii(getCornerRadii());

        // Get a possible avatar image that depends on the peer twincode.
        Bitmap avatar = invitation.getAvatar();
        if (avatar != null) {
            mRightAvatarView.setImageBitmap(avatar);
        }

        mLeftAvatarView.setImageBitmap(getBaseItemActivity().getIdentityAvatar());

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();

        // Be careful that name is retrieve asynchronously and can be null.
        final String name = invitation.getName();
        if (name != null) {
            spannableStringBuilder.append(name);
        }
        spannableStringBuilder.setSpan(new ForegroundColorSpan(Color.WHITE), 0, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        spannableStringBuilder.append("\n");
        int startSubTitle = spannableStringBuilder.length();
        spannableStringBuilder.append(getString(R.string.conversation_view_invitation_pending));
        spannableStringBuilder.setSpan(new RelativeSizeSpan(0.9f), startSubTitle, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.setSpan(new ForegroundColorSpan(Color.WHITE), startSubTitle, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mInvitationView.setText(spannableStringBuilder);
    }

    @Override
    void onViewRecycled() {

        super.onViewRecycled();

        mInvitationView.setText(null);
        mDeleteView.setVisibility(View.GONE);
        mDeleteView.setOnDeleteProgressListener(null);
        setDeleteAnimationStarted(false);
    }

    @Override
    void startDeletedAnimation() {

        if (isDeleteAnimationStarted()) {
            return;
        }

        setDeleteAnimationStarted(true);
        mDeleteView.setVisibility(View.VISIBLE);

        ViewGroup.MarginLayoutParams deleteLayoutParams = (ViewGroup.MarginLayoutParams) mDeleteView.getLayoutParams();
        deleteLayoutParams.width = mInvitationContainer.getWidth();
        deleteLayoutParams.height = mInvitationContainer.getHeight();
        mDeleteView.setLayoutParams(deleteLayoutParams);
        mDeleteView.setCornerRadii(getCornerRadii());
        mDeleteView.setOnDeleteProgressListener(() -> deleteItem(getItem()));

        float progress = 0;
        int animationDuration = DESIGN_DELETE_ANIMATION_DURATION;
        if (getItem().getDeleteProgress() > 0) {
            progress = getItem().getDeleteProgress() / 100.0f;
            animationDuration = (int) (BaseItemViewHolder.DESIGN_DELETE_ANIMATION_DURATION - ((getItem().getDeleteProgress() * BaseItemViewHolder.DESIGN_DELETE_ANIMATION_DURATION) / 100.0));
        }

        mDeleteView.startAnimation(animationDuration, progress);
    }

    @Override
    List<View> clickableViews() {

        return new ArrayList<View>() {
            {
                add(mInvitationContainer);
                add(getContainer());
            }
        };
    }
}
