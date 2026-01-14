/*
 *  Copyright (c) 2019-2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.models.Originator;
import org.twinlife.twinme.services.AbstractTwinmeService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.users.UIContact;
import org.twinlife.twinme.utils.RoundedView;

import java.util.ArrayList;
import java.util.List;

class TypingItemViewHolder extends BaseItemViewHolder {
    private static final String LOG_TAG = "TypingItemViewHolder";
    private static final boolean DEBUG = false;

    private final RoundedView mLeftBubbleView;
    private final RoundedView mMiddleBubbleView;
    private final RoundedView mRightBubbleView;

    private final TypingAvatarListAdapter mUIContactListAdapter;
    private final RecyclerView mMembersRecyclerView;

    private static final long ANIMATION_DURATION = 200;

    private static final float DESIGN_ITEM_VIEW_HEIGHT = 70f;
    private static final int ITEM_VIEW_HEIGHT;

    static {
        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
    }

    private final List<UIContact> mUIContacts = new ArrayList<>();

    TypingItemViewHolder(BaseItemActivity baseItemActivity, View view) {

        super(baseItemActivity, view, R.id.base_item_activity_typing_item_overlay_view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = TYPING_ITEM_HEIGHT;
        view.setLayoutParams(layoutParams);

        int bubbleColor = Design.getMainStyle();
        mLeftBubbleView = view.findViewById(R.id.base_item_activity_typing_item_left_bubble);
        mLeftBubbleView.setColor(bubbleColor);

        mMiddleBubbleView = view.findViewById(R.id.base_item_activity_typing_item_middle_bubble);
        mMiddleBubbleView.setColor(bubbleColor);

        mRightBubbleView = view.findViewById(R.id.base_item_activity_typing_item_right_bubble);
        mRightBubbleView.setColor(bubbleColor);

        AbstractTwinmeService service = new AbstractTwinmeService("TypingService", baseItemActivity, baseItemActivity.getTwinmeContext(), null);

        LinearLayoutManager contactLinearLayoutManager = new LinearLayoutManager(baseItemActivity, LinearLayoutManager.HORIZONTAL, false);
        mUIContactListAdapter = new TypingAvatarListAdapter(baseItemActivity, service, ITEM_VIEW_HEIGHT, mUIContacts,
                R.layout.conversation_activity_typing_contact, 0, R.id.conversation_activity_typing_contact_avatar_view);
        mMembersRecyclerView = view.findViewById(R.id.base_item_activity_typing_item_members_recycler_view);
        mMembersRecyclerView.setLayoutManager(contactLinearLayoutManager);
        mMembersRecyclerView.setAdapter(mUIContactListAdapter);
        mMembersRecyclerView.setItemAnimator(null);

        animationBubble();
    }

    @Override
    void onBind(Item item) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBind");
        }

        if (!(item instanceof TypingItem)) {
            return;
        }
        super.onBind(item);

        setOriginators(getBaseItemActivity().getTypingOriginators(), getBaseItemActivity().getTypingOriginatorsImages());

        if (getBaseItemActivity().isMenuOpen()) {
            getOverlayView().setVisibility(View.VISIBLE);
        } else {
            getOverlayView().setVisibility(View.INVISIBLE);
        }
    }

    private void setOriginators(@Nullable List<Originator> originators, @Nullable List<Bitmap> originatorsImages) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setOriginators");
        }

        mUIContacts.clear();

        if (originators != null) {
            BaseItemActivity baseItemActivity = getBaseItemActivity();
            int size = originators.size();
            for (int i = 0; i < size; i++) {
                Originator originator = originators.get(i);
                Bitmap avatar = baseItemActivity.getDefaultAvatar();
                if (originatorsImages != null && originatorsImages.size() > i) {
                    avatar = originatorsImages.get(i);
                }
                UIContact uiContact = new UIContact(baseItemActivity.getTwinmeApplication(), originator, avatar);
                mUIContacts.add(uiContact);
            }
        }
        if (mUIContacts.size() > 5) {
            ViewGroup.LayoutParams layoutParams = mMembersRecyclerView.getLayoutParams();
            layoutParams.height = ITEM_VIEW_HEIGHT;
            layoutParams.width = 5 * ITEM_VIEW_HEIGHT;
            mMembersRecyclerView.setLayoutParams(layoutParams);
            mMembersRecyclerView.requestLayout();
        } else {
            ViewGroup.LayoutParams layoutParams = mMembersRecyclerView.getLayoutParams();
            layoutParams.height = ITEM_VIEW_HEIGHT;
            layoutParams.width = (mUIContacts.size()) * ITEM_VIEW_HEIGHT;
            mMembersRecyclerView.setLayoutParams(layoutParams);
            mMembersRecyclerView.requestLayout();
        }

        mUIContactListAdapter.notifyDataSetChanged();
    }

    private void animationBubble() {
        if (DEBUG) {
            Log.d(LOG_TAG, "animationBubble");
        }

        PropertyValuesHolder propertyValuesHolderX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0f, 0.5f);
        PropertyValuesHolder propertyValuesHolderY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0f, 0.5f);

        ObjectAnimator scaleLeftViewAnimator = ObjectAnimator.ofPropertyValuesHolder(mLeftBubbleView, propertyValuesHolderX, propertyValuesHolderY);
        scaleLeftViewAnimator.setRepeatMode(ValueAnimator.REVERSE);
        scaleLeftViewAnimator.setRepeatCount(1);

        ObjectAnimator scaleMiddleFromLeftAnimator = ObjectAnimator.ofPropertyValuesHolder(mMiddleBubbleView, propertyValuesHolderX, propertyValuesHolderY);
        scaleMiddleFromLeftAnimator.setRepeatMode(ValueAnimator.REVERSE);
        scaleMiddleFromLeftAnimator.setRepeatCount(1);

        ObjectAnimator scaleMiddleFromRightAnimator = ObjectAnimator.ofPropertyValuesHolder(mMiddleBubbleView, propertyValuesHolderX, propertyValuesHolderY);
        scaleMiddleFromRightAnimator.setRepeatMode(ValueAnimator.REVERSE);
        scaleMiddleFromRightAnimator.setRepeatCount(1);

        ObjectAnimator scaleRightAnimator = ObjectAnimator.ofPropertyValuesHolder(mRightBubbleView, propertyValuesHolderX, propertyValuesHolderY);
        scaleRightAnimator.setRepeatMode(ValueAnimator.REVERSE);
        scaleRightAnimator.setRepeatCount(1);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(scaleLeftViewAnimator, scaleMiddleFromLeftAnimator, scaleRightAnimator, scaleMiddleFromRightAnimator);
        animatorSet.setDuration(ANIMATION_DURATION);
        animatorSet.start();

        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animator) {
            }

            @Override
            public void onAnimationEnd(@NonNull Animator animator) {
                animatorSet.start();
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animator) {
            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animator) {
            }
        });
    }
}
