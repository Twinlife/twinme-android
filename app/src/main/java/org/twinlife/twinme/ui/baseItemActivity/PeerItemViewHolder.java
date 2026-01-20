/*
 *  Copyright (c) 2018-2021 twinlife SA.
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
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.AvatarView;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

abstract class PeerItemViewHolder extends BaseItemViewHolder {

    private static final float DESIGN_ANNOTATION_VIEW_HEIGHT = 50f;

    private final AvatarView mAvatarView;
    private final RecyclerView mAnnotationView;
    private final AnnotationAdapter mAnnotationAdapter;

    private Timer mEphemeralTimer;

    PeerItemViewHolder(BaseItemActivity baseItemActivity, View view, int containerViewId, int avatarViewId, int overlayViewId, int selectedViewId, int selectedImageViewId) {

        super(baseItemActivity, view, containerViewId, overlayViewId, selectedViewId, selectedImageViewId);

        mAvatarView = view.findViewById(avatarViewId);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mAvatarView.getLayoutParams();
        layoutParams.height = BaseItemActivity.AVATAR_HEIGHT;
        mAvatarView.setLayoutParams(layoutParams);
        mAnnotationView = null;
        mAnnotationAdapter = null;
    }

    PeerItemViewHolder(BaseItemActivity baseItemActivity, View view, int containerViewId, int avatarViewId, int overlayViewId, int annotationViewId, int selectedViewId, int selectedImageViewId) {

        super(baseItemActivity, view, containerViewId, overlayViewId, selectedViewId, selectedImageViewId);

        mAvatarView = view.findViewById(avatarViewId);
        RelativeLayout.LayoutParams relativeLayoutParams = (RelativeLayout.LayoutParams) mAvatarView.getLayoutParams();
        relativeLayoutParams.height = BaseItemActivity.AVATAR_HEIGHT;
        mAvatarView.setLayoutParams(relativeLayoutParams);

        mAnnotationAdapter = new AnnotationAdapter(baseItemActivity, new ArrayList<>(), true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(baseItemActivity, RecyclerView.HORIZONTAL, false);
        mAnnotationView = view.findViewById(annotationViewId);
        mAnnotationView.setBackgroundColor(Color.TRANSPARENT);
        mAnnotationView.setLayoutManager(linearLayoutManager);
        mAnnotationView.setItemAnimator(null);
        mAnnotationView.setAdapter(mAnnotationAdapter);
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

        mAvatarView.setVisibility(View.GONE);
        if (item.getVisibleAvatar() && !getBaseItemActivity().isSelectItemMode()) {

            // Get a possible avatar image that depends on the peer twincode.
            getBaseItemActivity().getContactAvatar(item.getPeerTwincodeOutboundId(), (Bitmap avatar) -> {
                if (avatar != null) {
                    mAvatarView.setImageBitmap(avatar);
                }
                mAvatarView.setVisibility(View.VISIBLE);
            });
        }

        if (item.getState() == Item.ItemState.DELETED) {
            getBaseItemActivity().deleteItem(item.getDescriptorId());
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
        int topMargin;
        int bottomMargin;

        if ((item.getCorners() & Item.TOP_LARGE_MARGIN) == 0 || getBaseItemActivity().isGroupConversation()) {
            topMargin = ITEM_TOP_MARGIN1;
        } else {
            topMargin = ITEM_TOP_MARGIN2;
        }
        if (((item.getCorners() & Item.BOTTOM_LARGE_MARGIN) == 0) || item.isForwarded() || item.getLikeDescriptorAnnotations() != null) {
            bottomMargin = ITEM_BOTTOM_MARGIN1;
        } else {
            bottomMargin = ITEM_BOTTOM_MARGIN2;
        }
        if (topMargin != layoutParams.topMargin || bottomMargin != layoutParams.bottomMargin) {
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

        if (overlayLayoutParams.width != container.getWidth() || overlayLayoutParams.height != overlayHeight) {
            overlayLayoutParams.width = container.getWidth();
            overlayLayoutParams.height = overlayHeight;
            overlayView.setLayoutParams(overlayLayoutParams);
        }
    }

    @Override
    void onViewRecycled() {

        super.onViewRecycled();

        if (mEphemeralTimer != null) {
            mEphemeralTimer.cancel();
            mEphemeralTimer = null;
        }
    }

    void deleteEphemeralItem() {

        getBaseItemActivity().deleteItem(getItem().getDescriptorId());
    }
}
