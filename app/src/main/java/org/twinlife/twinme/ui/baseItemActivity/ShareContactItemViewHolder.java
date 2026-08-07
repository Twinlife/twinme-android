/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (fabrice.trescartes@twin.life)
 *   Romain Kolb (romain.kolb@skyrock.com)
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

import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.imageview.ShapeableImageView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

import java.util.ArrayList;
import java.util.List;

public class ShareContactItemViewHolder extends ItemViewHolder {

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

    private final View mShareContactContainer;
    private final GradientDrawable mGradientDrawable;
    private final ShapeableImageView mLeftAvatarView;
    private final ShapeableImageView mRightAvatarView;
    private final TextView mMessageView;
    private final DeleteProgressView mDeleteView;
    private final ImageView mStatusImageView;

    ShareContactItemViewHolder(BaseItemActivity baseItemActivity, View view) {

        super(baseItemActivity, view,
                R.id.base_item_activity_share_contact_item_container,
                R.id.base_item_activity_share_contact_item_state_view,
                R.id.base_item_activity_share_contact_item_state_avatar_view,
                R.id.base_item_activity_share_contact_item_overlay_view,
                R.id.base_item_activity_share_contact_item_annotation_view,
                R.id.base_item_activity_share_contact_item_selected_view,
                R.id.base_item_activity_share_contact_item_selected_image_view,
                R.id.base_item_activity_share_contact_item_error_image_view);


        mShareContactContainer = view.findViewById(R.id.base_item_activity_share_contact_item_view);
        mShareContactContainer.setPadding(FILE_ITEM_WIDTH_PADDING, FILE_ITEM_HEIGHT_PADDING, FILE_ITEM_WIDTH_PADDING, 0);
        mShareContactContainer.setClickable(false);

        mShareContactContainer.setOnLongClickListener(v -> {
            baseItemActivity.onItemLongPress(getItem());
            return true;
        });

        mGradientDrawable = new GradientDrawable();
        mGradientDrawable.mutate();
        mGradientDrawable.setColor(Design.getMainStyle());
        mGradientDrawable.setShape(GradientDrawable.RECTANGLE);
        mShareContactContainer.setBackground(mGradientDrawable);

        View avatarContainerView = view.findViewById(R.id.base_item_activity_share_contact_item_avatar_container_view);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) avatarContainerView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_AVATAR_TOP_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.leftMargin = (int) (DESIGN_AVATAR_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_AVATAR_MARGIN * Design.WIDTH_RATIO);
        avatarContainerView.setLayoutParams(marginLayoutParams);

        mLeftAvatarView = view.findViewById(R.id.base_item_activity_share_contact_item_left_avatar_view);

        ViewGroup.LayoutParams layoutParams = mLeftAvatarView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_AVATAR_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_AVATAR_SIZE * Design.HEIGHT_RATIO);
        mLeftAvatarView.setLayoutParams(layoutParams);

        mRightAvatarView = view.findViewById(R.id.base_item_activity_share_contact_item_right_avatar_view);
        layoutParams = mRightAvatarView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_AVATAR_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_AVATAR_SIZE * Design.HEIGHT_RATIO);
        mRightAvatarView.setLayoutParams(layoutParams);

        View lineLeftView = view.findViewById(R.id.base_item_activity_share_contact_item_line_left_view);

        layoutParams = lineLeftView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_LINE_HEIGHT * Design.HEIGHT_RATIO);
        lineLeftView.setLayoutParams(layoutParams);

        View lineRightView = view.findViewById(R.id.base_item_activity_share_contact_item_line_right_view);

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

        View iconContainerView = view.findViewById(R.id.base_item_activity_share_contact_item_icon_view);

        layoutParams = iconContainerView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_ICON_CONTAINER_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_ICON_CONTAINER_SIZE * Design.HEIGHT_RATIO);
        iconContainerView.setLayoutParams(layoutParams);

        ImageView iconView = view.findViewById(R.id.base_item_activity_share_contact_item_icon_image_view);

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

        mStatusImageView = view.findViewById(R.id.base_item_activity_share_contact_item_status_image_view);
        mStatusImageView.setPadding(Design.BORDER_WIDTH, Design.BORDER_WIDTH, Design.BORDER_WIDTH, Design.BORDER_WIDTH);
        mStatusImageView.setVisibility(View.GONE);

        mMessageView = view.findViewById(R.id.base_item_activity_share_contact_item_message_view);
        Design.updateTextFont(mMessageView, Design.FONT_MEDIUM32);
        mMessageView.setTextColor(getBaseItemActivity().getCustomAppearance().getMessageTextColor());

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mMessageView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_AVATAR_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_AVATAR_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.topMargin = (int) (DESIGN_MESSAGE_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_MESSAGE_MARGIN * Design.HEIGHT_RATIO);
        mMessageView.setLayoutParams(marginLayoutParams);

        mDeleteView = view.findViewById(R.id.base_item_activity_share_contact_item_delete_view);
    }

    @Override
    void onBind(Item item) {

        if (!(item instanceof ShareContactItem)) {
            return;
        }
        super.onBind(item);

        // Compute the corner radii only once!
        final float[] cornerRadii = getCornerRadii();

        mGradientDrawable.setCornerRadii(cornerRadii);
        mGradientDrawable.setColor(getBaseItemActivity().getCustomAppearance().getMessageBackgroundColor());
        if (getBaseItemActivity().getCustomAppearance().getMessageBorderColor() != Color.TRANSPARENT) {
            mGradientDrawable.setStroke(Design.BORDER_WIDTH, getBaseItemActivity().getCustomAppearance().getMessageBorderColor());
        }
        
        ShareContactItem shareContactItem = getShareContactItem();

        if (shareContactItem.getContactShareDescriptor() != null) {

            String message = "";
            switch (shareContactItem.getContactShareDescriptor().getStatus()) {
                case PENDING:
                    message = itemView.getContext().getString(R.string.conversation_view_invitation_pending);
                    mStatusImageView.setVisibility(View.GONE);
                    break;

                case ACCEPTED:
                    message = String.format(getBaseItemActivity().getString(R.string.conversation_view_share_contact_item_pending_acceptance), shareContactItem.getContactShareDescriptor().getName());
                    mStatusImageView.setVisibility(View.VISIBLE);
                    mStatusImageView.setImageDrawable(ResourcesCompat.getDrawable(itemView.getResources(), R.drawable.sending_state, null));
                    break;

                case JOINED:
                    message = String.format(getBaseItemActivity().getString(R.string.conversation_view_invitation_item_accepted_invitation), shareContactItem.getContactShareDescriptor().getName());
                    mStatusImageView.setVisibility(View.VISIBLE);
                    mStatusImageView.setImageDrawable(ResourcesCompat.getDrawable(itemView.getResources(), R.drawable.invitation_state_joined, null));
                    break;

                case REFUSED:
                    message = String.format(getBaseItemActivity().getString(R.string.conversation_view_share_contact_item_decline_connection_by_peer), getBaseItemActivity().getContact().getName());
                    mStatusImageView.setVisibility(View.VISIBLE);
                    mStatusImageView.setImageDrawable(ResourcesCompat.getDrawable(itemView.getResources(), R.drawable.invitation_state_refused, null));
                    break;

                default:
                    break;
            }

            if (getBaseItemActivity().getContact() != null) {
                SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
                spannableStringBuilder.append(String.format(itemView.getContext().getString(R.string.conversation_view_share_contact_item_local_message), shareContactItem.getContactName(), getBaseItemActivity().getContact().getName()));
                spannableStringBuilder.setSpan(new ForegroundColorSpan(getBaseItemActivity().getCustomAppearance().getMessageTextColor()), 0, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                spannableStringBuilder.append("\n");
                int startSubTitle = spannableStringBuilder.length();
                spannableStringBuilder.append(message);
                spannableStringBuilder.setSpan(new RelativeSizeSpan(0.9f), startSubTitle, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannableStringBuilder.setSpan(new ForegroundColorSpan(getBaseItemActivity().getCustomAppearance().getMessageTextColor()), startSubTitle, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                mMessageView.setText(spannableStringBuilder);
            }

            getBaseItemActivity().getShareContactAvatar(shareContactItem.getContactShareDescriptor(), (Bitmap avatar) -> {
                if (avatar != null) {
                    mLeftAvatarView.setImageBitmap(avatar);
                } else {
                    mLeftAvatarView.setImageBitmap(getBaseItemActivity().getDefaultAvatar());
                }
            });
        } else if (shareContactItem.getTwincodeDescriptor() != null) {
            if (getBaseItemActivity().getContact() != null) {
                mMessageView.setText(String.format(itemView.getContext().getString(R.string.conversation_view_share_contact_item_local_message), shareContactItem.getName(), getBaseItemActivity().getContact().getName()));
            }

            mLeftAvatarView.setImageBitmap(shareContactItem.getAvatar());
        }

        mRightAvatarView.setImageBitmap(getBaseItemActivity().getContactAvatar());
    }

    @Override
    void startDeletedAnimation() {

        if (isDeleteAnimationStarted()) {
            return;
        }

        setDeleteAnimationStarted(true);
        mDeleteView.setVisibility(View.VISIBLE);

        ViewGroup.MarginLayoutParams deleteLayoutParams = (ViewGroup.MarginLayoutParams) mDeleteView.getLayoutParams();
        deleteLayoutParams.width = mShareContactContainer.getWidth();
        deleteLayoutParams.height = mShareContactContainer.getHeight();
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
    void onViewRecycled() {

        super.onViewRecycled();

        mDeleteView.setVisibility(View.GONE);
        mDeleteView.setOnDeleteProgressListener(null);
        setDeleteAnimationStarted(false);
    }

    @Override
    List<View> clickableViews() {

        return new ArrayList<View>() {
            {
                add(getContainer());
            }
        };
    }

    //
    // Private methods
    //

    private ShareContactItem getShareContactItem() {

        return (ShareContactItem) getItem();
    }
}
