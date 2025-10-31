/*
 *  Copyright (c) 2018-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.AvatarView;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

abstract class ItemViewHolder extends BaseItemViewHolder {

    private static final float DESIGN_ANNOTATION_VIEW_HEIGHT = 50f;

    private final ImageView mStateView;
    private final AvatarView mStateAvatarView;
    private final RecyclerView mAnnotationView;
    private final AnnotationAdapter mAnnotationAdapter;
    private boolean mDeleteAnimationStarted;
    private Timer mEphemeralTimer;

    ItemViewHolder(BaseItemActivity baseItemActivity, View view, int containerViewId, int stateViewId, int stateAvatarViewId, int overlayViewId, int selectedViewId, int selectedImageViewId) {

        super(baseItemActivity, view, containerViewId, overlayViewId, selectedViewId, selectedImageViewId);

        mStateView = view.findViewById(stateViewId);
        mStateAvatarView = view.findViewById(stateAvatarViewId);
        mAnnotationView = null;
        mAnnotationAdapter = null;
        mDeleteAnimationStarted = false;
    }

    ItemViewHolder(BaseItemActivity baseItemActivity, View view, int containerViewId, int stateViewId, int stateAvatarViewId, int overlayViewId, int annotationViewId, int selectedViewId, int selectedImageViewId) {

        super(baseItemActivity, view, containerViewId, overlayViewId, selectedViewId, selectedImageViewId);

        mStateView = view.findViewById(stateViewId);
        mStateAvatarView = view.findViewById(stateAvatarViewId);

        mAnnotationAdapter = new AnnotationAdapter(baseItemActivity, new ArrayList<>(), false);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(baseItemActivity, RecyclerView.HORIZONTAL, false);
        mAnnotationView = view.findViewById(annotationViewId);
        mAnnotationView.setBackgroundColor(Color.TRANSPARENT);
        mAnnotationView.setLayoutManager(linearLayoutManager);
        mAnnotationView.setItemAnimator(null);
        mAnnotationView.setAdapter(mAnnotationAdapter);

        mDeleteAnimationStarted = false;
    }

    public int getAnnotationViewHeight() {

        if (mAnnotationView == null || mAnnotationView.getVisibility() != View.VISIBLE) {
            return 0;
        } else {
            return (int) (DESIGN_ANNOTATION_VIEW_HEIGHT * Design.HEIGHT_RATIO);
        }
    }

    @Override
    void onBind(Item item) {

        super.onBind(item);
        
        switch (item.getState()) {
            case DEFAULT:
                mStateView.clearAnimation();
                mStateView.setVisibility(View.GONE);
                mStateAvatarView.setVisibility(View.GONE);
                break;

            case SENDING:
                updateCornerWithMask(~Item.BOTTOM_RIGHT);
                mStateView.clearAnimation();
                mStateView.setBackgroundResource(R.drawable.sending_state);
                mStateView.setVisibility(View.VISIBLE);
                mStateAvatarView.setVisibility(View.GONE);
                break;

            case RECEIVED:
                updateCornerWithMask(~Item.BOTTOM_RIGHT);
                mStateView.setBackgroundResource(R.drawable.received_state);
                mStateView.setVisibility(View.VISIBLE);
                mStateAvatarView.setVisibility(View.GONE);
                break;

            case READ:
            case PEER_DELETED:
                updateCornerWithMask(~Item.BOTTOM_RIGHT);
                mStateView.setVisibility(View.GONE);
                getBaseItemActivity().getContactAvatar(null, (Bitmap avatar) -> {
                    if (avatar != null) {
                        if (avatar.equals(getBaseItemActivity().getTwinmeApplication().getDefaultGroupAvatar())) {
                            mStateAvatarView.setImageBitmap(avatar, Design.GREY_ITEM_COLOR, 0, Design.GREY_ITEM_COLOR);
                        } else {
                            mStateAvatarView.setImageBitmap(avatar);
                        }
                        mStateAvatarView.setVisibility(View.VISIBLE);
                    }
                });
                break;

            case NOT_SENT:
                updateCornerWithMask(~Item.BOTTOM_RIGHT);
                mStateView.setBackgroundResource(R.drawable.not_sent_state);
                mStateView.setVisibility(View.VISIBLE);
                mStateAvatarView.setVisibility(View.GONE);
                break;

            case DELETED:
                updateCornerWithMask(~Item.BOTTOM_RIGHT);
                mStateView.setBackgroundResource(R.drawable.deleted_state);
                mStateView.setVisibility(View.VISIBLE);
                mStateAvatarView.setVisibility(View.GONE);
                break;

            case BOTH_DELETED:
                updateCornerWithMask(~Item.BOTTOM_RIGHT);
                mStateView.setBackgroundResource(R.drawable.deleted_state);
                mStateView.setVisibility(View.VISIBLE);

                if (getItem().getDeleteProgress() == 0) {
                    getItem().startDeleteItem();
                }
                startDeletedAnimation();
                break;
        }

        if (item.getType() == Item.ItemType.CALL) {
            mStateView.clearAnimation();
            mStateView.setVisibility(View.GONE);
            mStateAvatarView.setVisibility(View.GONE);
        }

        if (item.getState() == Item.ItemState.READ && item.isEphemeralItem()) {
            Date now = new Date();
            long timeInterval = item.getReadTimestamp() + item.getExpireTimeout() - now.getTime();
            if (timeInterval > 0) {
                if (mEphemeralTimer == null) {
                    mEphemeralTimer = new Timer();
                    mEphemeralTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            deleteEphemeralItem();
                        }
                    }, timeInterval);
                }
            }
        }

        if (mAnnotationView != null) {
            if (item.isForwarded() || item.isEdited() || (item.getLikeDescriptorAnnotations() != null && !item.getLikeDescriptorAnnotations().isEmpty())) {
                mAnnotationAdapter.setAnnotations(item.getLikeDescriptorAnnotations(), item.getDescriptorId());
                mAnnotationAdapter.setIsForwarded(item.isForwarded());
                mAnnotationAdapter.setIsUpdated(item.isEdited());
                mAnnotationView.setVisibility(View.VISIBLE);
            } else {
                mAnnotationView.setVisibility(View.GONE);
            }
        }

        final View container = getContainer();

        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) container.getLayoutParams();
        int corners = item.getCorners();
        int topMargin;
        int bottomMargin;

        if ((corners & Item.TOP_RIGHT) == 0) {
            topMargin = ITEM_TOP_MARGIN1;
        } else {
            topMargin = ITEM_TOP_MARGIN2;
        }
        if (((corners & Item.BOTTOM_RIGHT) == 0) || item.isForwarded() || item.isEdited() || item.getLikeDescriptorAnnotations() != null) {
            bottomMargin = ITEM_BOTTOM_MARGIN1;
        } else {
            bottomMargin = ITEM_BOTTOM_MARGIN2;
        }

        if (layoutParams.topMargin != topMargin || layoutParams.bottomMargin != bottomMargin) {
            layoutParams.topMargin = topMargin;
            layoutParams.bottomMargin = bottomMargin;
            container.setLayoutParams(layoutParams);
        }

        View overlayView = getOverlayView();
        ViewGroup.LayoutParams overlayLayoutParams = overlayView.getLayoutParams();
        int overlayHeight;

        if (isMenuOpen()) {
            overlayHeight = container.getHeight() + getAnnotationViewHeight() + layoutParams.topMargin + layoutParams.bottomMargin;
            overlayView.setVisibility(View.VISIBLE);
            if (isSelectedItem(item.getDescriptorId())) {
                itemView.setBackgroundColor(Design.BACKGROUND_COLOR_WHITE_OPACITY85);
                overlayView.setVisibility(View.INVISIBLE);
            }
        } else {
            overlayHeight = OVERLAY_DEFAULT_HEIGHT;
            overlayView.setVisibility(View.INVISIBLE);
            itemView.setBackgroundColor(Color.TRANSPARENT);
        }
        if (container.getWidth() != overlayLayoutParams.width || overlayHeight != overlayLayoutParams.height) {
            overlayLayoutParams.width = container.getWidth();
            overlayLayoutParams.height = overlayHeight;
            overlayView.setLayoutParams(overlayLayoutParams);
        }
    }

    @Override
    void onViewRecycled() {

        super.onViewRecycled();
    }

    boolean isDeleteAnimationStarted() {

        return mDeleteAnimationStarted;
    }

    void setDeleteAnimationStarted(boolean deleteAnimationStarted) {

        mDeleteAnimationStarted = deleteAnimationStarted;
    }

    void deleteEphemeralItem() {

        getBaseItemActivity().deleteItem(getItem().getDescriptorId());
    }

    abstract void startDeletedAnimation();
}
