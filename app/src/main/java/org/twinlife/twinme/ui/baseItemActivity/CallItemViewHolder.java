/*
 *  Copyright (c) 2020-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ConversationService.CallDescriptor;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.AvatarView;
import org.twinlife.twinme.utils.CommonUtils;
import org.twinlife.twinme.utils.Utils;

import java.util.Arrays;
import java.util.List;

class CallItemViewHolder extends ItemViewHolder {

    private static final float DESIGN_AVATAR_VIEW_HEIGHT = 42f;
    private static final float DESIGN_TYPE_CALL_VIEW_HEIGHT = 30f;
    private static final float DESIGN_TYPE_CALL_VIEW_WIDTH = 30f;
    private static final float DESIGN_TYPE_CALL_VIEW_MARGIN_RIGHT = 22f;
    private static final int AVATAR_VIEW_HEIGHT;
    private static final int TYPE_CALL_VIEW_HEIGHT;
    private static final int TYPE_CALL_VIEW_WIDTH;
    private static final int TYPE_CALL_VIEW_MARGIN_RIGHT;

    static {
        AVATAR_VIEW_HEIGHT = (int) (DESIGN_AVATAR_VIEW_HEIGHT * Design.HEIGHT_RATIO);
        TYPE_CALL_VIEW_HEIGHT = (int) (DESIGN_TYPE_CALL_VIEW_HEIGHT * Design.HEIGHT_RATIO);
        TYPE_CALL_VIEW_WIDTH = (int) (DESIGN_TYPE_CALL_VIEW_WIDTH * Design.WIDTH_RATIO);
        TYPE_CALL_VIEW_MARGIN_RIGHT = (int) (DESIGN_TYPE_CALL_VIEW_MARGIN_RIGHT * Design.WIDTH_RATIO);
    }

    private final View mCallItemContainer;
    private final GradientDrawable mGradientDrawable;
    private final TextView mCallTypeView;
    private final TextView mCallDurationView;
    private final AvatarView mCallAvatarImageView;
    private final DeleteProgressView mDeleteView;

    private boolean mIsVideo;

    CallItemViewHolder(BaseItemActivity baseItemActivity, View view, boolean allowClick, boolean allowLongClick) {

        super(baseItemActivity, view,
                R.id.base_item_activity_call_item_container,
                R.id.base_item_activity_call_item_state_view,
                R.id.base_item_activity_call_item_state_avatar_view,
                R.id.base_item_activity_call_item_overlay_view,
                R.id.base_item_activity_call_item_selected_view,
                R.id.base_item_activity_call_item_selected_image_view);

        mCallItemContainer = view.findViewById(R.id.base_item_activity_call_item_view);
        mCallItemContainer.setPadding(FILE_ITEM_WIDTH_PADDING, FILE_ITEM_HEIGHT_PADDING, FILE_ITEM_WIDTH_PADDING, FILE_ITEM_HEIGHT_PADDING);
        mCallItemContainer.setClickable(false);

        mGradientDrawable = new GradientDrawable();
        mGradientDrawable.mutate();
        mGradientDrawable.setColor(Design.getMainStyle());
        mGradientDrawable.setShape(GradientDrawable.RECTANGLE);
        mCallItemContainer.setBackground(mGradientDrawable);

        mCallTypeView = view.findViewById(R.id.base_item_activity_call_item_type_call_view);
        Design.updateTextFont(mCallTypeView, Design.FONT_MEDIUM30);
        mCallTypeView.setTextColor(Color.WHITE);

        mCallDurationView = view.findViewById(R.id.base_item_activity_call_item_call_duration_view);
        Design.updateTextFont(mCallDurationView, Design.FONT_REGULAR30);
        mCallDurationView.setTextColor(Color.WHITE);

        mCallAvatarImageView = view.findViewById(R.id.base_item_activity_call_item_avatar_image_view);
        ViewGroup.LayoutParams layoutParams = mCallAvatarImageView.getLayoutParams();
        layoutParams.height = AVATAR_VIEW_HEIGHT;

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mCallAvatarImageView.getLayoutParams();

        if (CommonUtils.isLayoutDirectionRTL()) {
            marginLayoutParams.leftMargin = TYPE_CALL_VIEW_MARGIN_RIGHT;
            marginLayoutParams.setMarginStart(TYPE_CALL_VIEW_MARGIN_RIGHT);
        } else {
            marginLayoutParams.rightMargin = TYPE_CALL_VIEW_MARGIN_RIGHT;
            marginLayoutParams.setMarginEnd(TYPE_CALL_VIEW_MARGIN_RIGHT);
        }

        TextView callAgainView = view.findViewById(R.id.base_item_activity_call_item_call_again_title_view);
        Design.updateTextFont(callAgainView, Design.FONT_MEDIUM30);
        callAgainView.setTextColor(Color.WHITE);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) callAgainView.getLayoutParams();
        marginLayoutParams.rightMargin = TYPE_CALL_VIEW_MARGIN_RIGHT;

        ImageView callAgainImageView = view.findViewById(R.id.base_item_activity_call_item_call_again_view);
        layoutParams = callAgainImageView.getLayoutParams();
        layoutParams.width = TYPE_CALL_VIEW_WIDTH;
        layoutParams.height = TYPE_CALL_VIEW_HEIGHT;

        mDeleteView = view.findViewById(R.id.base_item_activity_call_item_delete_view);

        if (allowClick) {
            mCallItemContainer.setOnClickListener(v -> {
                if (getBaseItemActivity().isSelectItemMode()) {
                    onContainerClick();
                    return;
                }

                if (getItem().getState() != Item.ItemState.BOTH_DELETED) {
                    if (mIsVideo) {
                        baseItemActivity.videoCall();
                    } else {
                        baseItemActivity.audioCall();
                    }
                }
            });
        }

        if (allowLongClick) {
            mCallItemContainer.setOnLongClickListener(v -> {
                baseItemActivity.onItemLongPress(getItem());

                return true;
            });
        }
    }

    @Override
    void onBind(Item item) {

        if (!(item instanceof CallItem)) {
            return;
        }
        super.onBind(item);

        mGradientDrawable.setCornerRadii(getCornerRadii());

        final CallItem callItem = (CallItem) item;
        CallDescriptor callDescriptor = callItem.getCallDescriptor();
        mIsVideo = callDescriptor.isVideo();

        if (callDescriptor.isVideo()) {
            mCallTypeView.setText(getString(R.string.conversation_activity_video_call));
        } else {
            mCallTypeView.setText(getString(R.string.conversation_activity_audio_call));
        }

        getBaseItemActivity().getContactAvatar(null, (Bitmap avatar) -> {
            if (avatar != null) {
                mCallAvatarImageView.setImageBitmap(avatar);
            }

            if (callDescriptor.getTerminateReason() != null) {
                int duration = (int) callDescriptor.getDuration() / 1000;
                mCallDurationView.setText(Utils.formatInterval(duration, "mm:ss"));
            } else {
                mCallDurationView.setText("");
            }
        });
    }

    @Override
    void startDeletedAnimation() {

        if (isDeleteAnimationStarted()) {
            return;
        }

        setDeleteAnimationStarted(true);
        mDeleteView.setVisibility(View.VISIBLE);

        ViewGroup.MarginLayoutParams deleteLayoutParams = (ViewGroup.MarginLayoutParams) mDeleteView.getLayoutParams();
        deleteLayoutParams.width = mCallItemContainer.getWidth();
        deleteLayoutParams.height = mCallItemContainer.getHeight();
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
        return Arrays.asList(mCallItemContainer, getContainer());
    }
}
