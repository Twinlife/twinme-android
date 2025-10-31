/*
 *  Copyright (c) 2020-2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.rooms;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.percentlayout.widget.PercentRelativeLayout;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.CircularImageDescriptor;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.CircularImageView;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class MenuRoomMemberView extends PercentRelativeLayout implements ViewTreeObserver.OnGlobalLayoutListener {
    private static final String LOG_TAG = "MenuRoomMemberView";
    private static final boolean DEBUG = false;

    private static final long ANIMATION_DURATION = 100;
    private static final int ACTION_COLOR = Color.argb(255, 0, 122, 255);

    private static final int DESIGN_AVATAR_HEIGHT = 100;
    private static final int DESIGN_AVATAR_TOP_MARGIN = 40;
    private static final int DESIGN_NAME_TOP_MARGIN = 20;
    private static final int DESIGN_NAME_BOTTOM_MARGIN = 26;
    private static final int DESIGN_ACTION_HEIGHT = 120;

    private View mActionView;
    private View mCancelView;
    private View mInviteView;
    private TextView mInviteTextView;
    private View mAdminView;
    private TextView mAdminTextView;
    private View mRemoveView;
    private TextView mNameView;
    private CircularImageView mAvatarView;

    private RoomMembersActivity mRoomMemberActivity;

    private final List<View> mAnimationList = new ArrayList<>();

    private boolean mIsAnimationEnded = false;

    private boolean mRemoveAdmin = false;

    public MenuRoomMemberView(Context context) {
        super(context);
    }

    public MenuRoomMemberView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (DEBUG) {
            Log.d(LOG_TAG, "MenuRoomMemberView");
        }

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            View view = inflater.inflate(R.layout.room_members_activity_menu_view, (ViewGroup) getParent());
            view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            addView(view);
        }
        initViews();
    }

    public void openMenu(UIRoomMember uiRoomMember, boolean showAdminAction, boolean showInviteAction, boolean removeAdminAction) {
        if (DEBUG) {
            Log.d(LOG_TAG, "openMenu");
        }

        mRemoveAdmin = removeAdminAction;

        if (showAdminAction) {
            mAdminView.setVisibility(VISIBLE);
            mRemoveView.setVisibility(VISIBLE);

            if (mRemoveAdmin) {
                mAdminTextView.setText(mRoomMemberActivity.getString(R.string.room_members_activity_remove_admin_title));
            } else {
                mAdminTextView.setText(mRoomMemberActivity.getString(R.string.room_members_activity_change_admin_title));
            }
        } else {
            mAdminView.setVisibility(GONE);
            mRemoveView.setVisibility(GONE);
        }

        if (showInviteAction) {
            mInviteView.setVisibility(VISIBLE);
        } else {
            mInviteView.setVisibility(GONE);
        }

        mAvatarView.setImage(mRoomMemberActivity, null,
                new CircularImageDescriptor(uiRoomMember.getAvatar(), 0.5f, 0.5f, 0.5f));
        mNameView.setText(uiRoomMember.getName());
        mInviteTextView.setText(mRoomMemberActivity.getString(R.string.group_member_activity_invite_personnal_relation));

        mIsAnimationEnded = false;

        mActionView.setAlpha((float) 0.0);
        mCancelView.setAlpha((float) 0.0);

        mAnimationList.clear();

        mAnimationList.add(mCancelView);
        mAnimationList.add(mActionView);

        animationMenu();
    }

    public void animationMenu() {
        if (DEBUG) {
            Log.d(LOG_TAG, "animationMenu");
        }

        if (mIsAnimationEnded) {
            return;
        }

        PropertyValuesHolder propertyValuesHolderAlpha = PropertyValuesHolder.ofFloat(View.ALPHA, 0.0f, 1.0f);

        List<Animator> animators = new ArrayList<>();

        for (View view : mAnimationList) {
            ObjectAnimator alphaViewAnimator = ObjectAnimator.ofPropertyValuesHolder(view, propertyValuesHolderAlpha);
            alphaViewAnimator.setDuration(ANIMATION_DURATION);
            animators.add(alphaViewAnimator);
        }

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(animators);
        animatorSet.start();
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animator) {

            }

            @Override
            public void onAnimationEnd(@NonNull Animator animator) {

                mIsAnimationEnded = true;
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animator) {

            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animator) {

            }
        });
    }

    public void setRoomMemberActivity(RoomMembersActivity activty) {

        mRoomMemberActivity = activty;
    }

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        mActionView = findViewById(R.id.room_members_activity_menu_action_view);

        MarginLayoutParams marginLayoutParams = (MarginLayoutParams) mActionView.getLayoutParams();
        marginLayoutParams.bottomMargin = Design.MENU_ACTION_MARGIN;

        mAvatarView = findViewById(R.id.room_members_activity_menu_avatar_view);
        ViewGroup.LayoutParams layoutParams = mAvatarView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_AVATAR_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (MarginLayoutParams) mAvatarView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_AVATAR_TOP_MARGIN * Design.HEIGHT_RATIO);

        mNameView = findViewById(R.id.room_members_activity_menu_name_view);
        Design.updateTextFont(mNameView, Design.FONT_REGULAR34);
        mNameView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (MarginLayoutParams) mNameView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_NAME_TOP_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_NAME_BOTTOM_MARGIN * Design.HEIGHT_RATIO);

        mInviteView = findViewById(R.id.room_members_activity_menu_invite_view);
        mInviteView.setOnClickListener(v -> onInviteMemberClick());

        layoutParams = mInviteView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_ACTION_HEIGHT * Design.HEIGHT_RATIO);

        mInviteTextView = findViewById(R.id.room_members_activity_menu_invite_text_view);
        Design.updateTextFont(mInviteTextView, Design.FONT_BOLD34);
        mInviteTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mAdminView = findViewById(R.id.room_members_activity_menu_admin_view);
        mAdminView.setOnClickListener(v -> onChangeAdminClick());

        layoutParams = mAdminView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_ACTION_HEIGHT * Design.HEIGHT_RATIO);

        mAdminTextView = findViewById(R.id.room_members_activity_menu_admin_text_view);
        Design.updateTextFont(mAdminTextView, Design.FONT_BOLD34);
        mAdminTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mRemoveView = findViewById(R.id.room_members_activity_menu_remove_view);
        mRemoveView.setOnClickListener(v -> onRemoveMemberClick());

        layoutParams = mRemoveView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_ACTION_HEIGHT * Design.HEIGHT_RATIO);

        TextView removeTextView = findViewById(R.id.room_members_activity_menu_remove_text_view);
        Design.updateTextFont(removeTextView, Design.FONT_BOLD34);
        removeTextView.setTextColor(Design.FONT_COLOR_RED);

        mCancelView = findViewById(R.id.room_members_activity_menu_cancel_view);
        mCancelView.setOnClickListener(v -> onCloseMenuClick());

        layoutParams = mCancelView.getLayoutParams();
        layoutParams.height = Design.MENU_CANCEL_HEIGHT;

        marginLayoutParams = (MarginLayoutParams) mCancelView.getLayoutParams();
        marginLayoutParams.bottomMargin = Design.MENU_CANCEL_MARGIN;

        TextView cancelTextView = findViewById(R.id.room_members_activity_menu_cancel_text_view);
        Design.updateTextFont(cancelTextView, Design.FONT_BOLD34);
        cancelTextView.setTextColor(ACTION_COLOR);
    }

    private void onChangeAdminClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onChangeAdminClick");
        }

        if (mRemoveAdmin) {
            mRoomMemberActivity.onRemoveAdminClick();
        } else {
            mRoomMemberActivity.onChangeAdminClick();
        }
    }

    private void onRemoveMemberClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRemoveMemberClick");
        }

        mRoomMemberActivity.onRemoveMemberClick();
    }

    private void onInviteMemberClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onInviteMemberClick");
        }

        mRoomMemberActivity.onInviteMemberClick();
    }

    private void onCloseMenuClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCloseMenuClick");
        }

        mRoomMemberActivity.closeMenu();
    }

    @Override
    public void onGlobalLayout() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGlobalLayout");
        }
    }
}
