/*
 *  Copyright (c) 2020-2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.groups;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.percentlayout.widget.PercentRelativeLayout;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.CircularImageDescriptor;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.users.UIContact;
import org.twinlife.twinme.utils.CircularImageView;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class MenuGroupMemberView extends PercentRelativeLayout {
    private static final String LOG_TAG = "MenuGroupMemberView";
    private static final boolean DEBUG = false;

    public interface Observer {

        void onCloseMenuAnimationEnd();
    }

    private static final int DESIGN_AVATAR_MARGIN = 40;
    private static final int DESIGN_AVATAR_SIZE = 100;
    private static final int DESIGN_TITLE_MARGIN = 20;
    private static final int DESIGN_TITLE_BOTTOM_MARGIN = 60;

    private View mActionView;
    private View mInviteView;
    private View mRemoveView;
    private TextView mNameView;
    private CircularImageView mAvatarView;
    private TextView mInviteTextView;

    private GroupMemberActivity mGroupMemberActivity;
    private Observer mObserver;

    private boolean isOpenAnimationEnded = false;
    private boolean isCloseAnimationEnded = false;

    private boolean mCanInvite = false;
    private boolean mCanRemove = false;
    private int mHeight = Design.DISPLAY_HEIGHT;

    public MenuGroupMemberView(Context context) {
        super(context);
    }

    public MenuGroupMemberView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (DEBUG) {
            Log.d(LOG_TAG, "MenuGroupMemberView");
        }

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            View view = inflater.inflate(R.layout.group_member_activity_menu_view, (ViewGroup) getParent());
            view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            addView(view);
        }
        initViews();
    }

    public void setObserver(Observer observer) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setObserver: " + observer);
        }

        mObserver = observer;
    }

    public void openMenu(UIContact uiContact, boolean canInvite, boolean canRemove) {
        if (DEBUG) {
            Log.d(LOG_TAG, "openMenu");
        }

        isOpenAnimationEnded = false;
        isCloseAnimationEnded = false;

        mCanInvite = canInvite;
        mCanRemove = canRemove;

        if (canInvite) {
            mInviteView.setAlpha(1.0f);
        } else {
            mInviteView.setAlpha(0.5f);
        }

        if (canRemove) {
            mRemoveView.setAlpha(1.0f);
        } else {
            mRemoveView.setAlpha(0.5f);
        }

        mInviteTextView.setText(mGroupMemberActivity.getString(R.string.group_member_activity_invite_personnal_relation));

        mAvatarView.setImage(mGroupMemberActivity, null,
                new CircularImageDescriptor(uiContact.getAvatar(), 0.5f, 0.5f, 0.5f));
        mNameView.setText(uiContact.getName());

        ViewGroup.LayoutParams layoutParams = mActionView.getLayoutParams();
        layoutParams.height = getActionViewHeight();
        mActionView.setLayoutParams(layoutParams);
        mActionView.setY(Design.DISPLAY_HEIGHT);
        mActionView.invalidate();

        animationOpenMenu();
    }

    public void animationOpenMenu() {
        if (DEBUG) {
            Log.d(LOG_TAG, "animationOpenMenu");
        }

        if (isOpenAnimationEnded) {
            return;
        }

        int startValue = mHeight;
        int endValue = mHeight - getActionViewHeight();

        PropertyValuesHolder propertyValuesHolder = PropertyValuesHolder.ofFloat(View.Y, startValue, endValue);

        List<Animator> animators = new ArrayList<>();
        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(mActionView, propertyValuesHolder);
        objectAnimator.setDuration(Design.ANIMATION_VIEW_DURATION);
        animators.add(objectAnimator);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(animators);
        animatorSet.start();
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animator) {

            }

            @Override
            public void onAnimationEnd(@NonNull Animator animator) {

                isOpenAnimationEnded = true;
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animator) {

            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animator) {

            }
        });
    }

    public void animationCloseMenu() {
        if (DEBUG) {
            Log.d(LOG_TAG, "animationCloseMenu");
        }

        if (isCloseAnimationEnded) {
            return;
        }

        int startValue = mHeight - getActionViewHeight();
        int endValue = mHeight;

        PropertyValuesHolder propertyValuesHolder = PropertyValuesHolder.ofFloat(View.Y, startValue, endValue);

        List<Animator> animators = new ArrayList<>();
        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(mActionView, propertyValuesHolder);
        objectAnimator.setDuration(Design.ANIMATION_VIEW_DURATION);
        animators.add(objectAnimator);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(animators);
        animatorSet.start();
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animator) {

            }

            @Override
            public void onAnimationEnd(@NonNull Animator animator) {

                isCloseAnimationEnded = true;
                mObserver.onCloseMenuAnimationEnd();
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animator) {

            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animator) {

            }
        });
    }

    public void setGroupMemberActivity(GroupMemberActivity activty) {

        mGroupMemberActivity = activty;
    }

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressLint("NewApi")
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);

                mHeight = getHeight();
            }
        });

        mActionView = findViewById(R.id.group_member_activity_menu_action_view);

        float radius = Design.ACTION_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, 0, 0, 0, 0};

        ShapeDrawable scrollIndicatorBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        scrollIndicatorBackground.getPaint().setColor(Design.POPUP_BACKGROUND_COLOR);
        mActionView.setBackground(scrollIndicatorBackground);

        mAvatarView = findViewById(R.id.group_member_activity_menu_avatar_view);

        ViewGroup.LayoutParams layoutParams = mAvatarView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_AVATAR_SIZE * Design.HEIGHT_RATIO);

        MarginLayoutParams marginLayoutParams = (MarginLayoutParams) mAvatarView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_AVATAR_MARGIN * Design.HEIGHT_RATIO);

        mNameView = findViewById(R.id.group_member_activity_menu_name_view);
        Design.updateTextFont(mNameView, Design.FONT_MEDIUM34);
        mNameView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (MarginLayoutParams) mNameView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_TITLE_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_TITLE_BOTTOM_MARGIN * Design.HEIGHT_RATIO);

        mInviteView = findViewById(R.id.group_member_activity_menu_invite_view);
        mInviteView.setOnClickListener(v -> onInviteMemberClick());

        layoutParams = mInviteView.getLayoutParams();
        layoutParams.height = Design.SECTION_HEIGHT;

        ImageView inviteImageView = findViewById(R.id.group_member_activity_menu_invite_image_view);
        inviteImageView.setColorFilter(Design.BLACK_COLOR);

        mInviteTextView = findViewById(R.id.group_member_activity_menu_invite_text_view);
        Design.updateTextFont(mInviteTextView, Design.FONT_MEDIUM34);
        mInviteTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mRemoveView = findViewById(R.id.group_member_activity_menu_remove_view);
        mRemoveView.setOnClickListener(v -> onRemoveMemberClick());

        layoutParams = mRemoveView.getLayoutParams();
        layoutParams.height = Design.SECTION_HEIGHT;

        TextView removeTextView = findViewById(R.id.group_member_activity_menu_remove_text_view);
        Design.updateTextFont(removeTextView, Design.FONT_MEDIUM34);
        removeTextView.setTextColor(Design.FONT_COLOR_RED);
    }

    private void onInviteMemberClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onInviteMemberClick");
        }

        mGroupMemberActivity.onInviteMemberClick(mCanInvite);
    }

    private void onRemoveMemberClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRemoveMemberClick");
        }

        mGroupMemberActivity.onRemoveMemberClick(mCanRemove);
    }

    private int getActionViewHeight() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getActionViewHeight");
        }

        int actionViewHeight = Design.SECTION_HEIGHT * 2 + (int) ((DESIGN_TITLE_MARGIN + DESIGN_AVATAR_MARGIN + DESIGN_AVATAR_SIZE + DESIGN_TITLE_BOTTOM_MARGIN) * Design.HEIGHT_RATIO);

        int bottomInset = 0;
        View rootView = ((Activity) getContext()).getWindow().getDecorView();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            WindowInsets insets = rootView.getRootWindowInsets();
            if (insets != null) {
                bottomInset = insets.getInsets(WindowInsets.Type.systemBars()).bottom;
            }
        }

        return actionViewHeight + mNameView.getHeight() + bottomInset;
    }
}
