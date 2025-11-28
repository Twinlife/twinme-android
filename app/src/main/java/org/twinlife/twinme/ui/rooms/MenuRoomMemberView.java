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
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
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
import org.twinlife.twinme.utils.CircularImageView;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class MenuRoomMemberView extends PercentRelativeLayout implements ViewTreeObserver.OnGlobalLayoutListener {
    private static final String LOG_TAG = "MenuRoomMemberView";
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
    private TextView mInviteTextView;
    private View mAdminView;
    private TextView mAdminTextView;
    private View mRemoveView;
    private TextView mNameView;
    private CircularImageView mAvatarView;

    private RoomMembersActivity mRoomMemberActivity;
    private Observer mObserver;
    private boolean isOpenAnimationEnded = false;
    private boolean isCloseAnimationEnded = false;
    private int mHeight = Design.DISPLAY_HEIGHT;

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
        inflater.inflate(R.layout.room_members_activity_menu_view, this, true);
        initViews();
    }

    public void openMenu(UIRoomMember uiRoomMember, boolean showAdminAction, boolean showInviteAction, boolean removeAdminAction) {
        if (DEBUG) {
            Log.d(LOG_TAG, "openMenu");
        }

        isOpenAnimationEnded = false;
        isCloseAnimationEnded = false;
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

    public void setObserver(Observer observer) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setObserver: " + observer);
        }

        mObserver = observer;
    }

    public void setRoomMemberActivity(RoomMembersActivity activty) {

        mRoomMemberActivity = activty;
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

        mActionView = findViewById(R.id.room_members_activity_menu_action_view);

        float radius = Design.ACTION_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, 0, 0, 0, 0};

        ShapeDrawable scrollIndicatorBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        scrollIndicatorBackground.getPaint().setColor(Design.POPUP_BACKGROUND_COLOR);
        mActionView.setBackground(scrollIndicatorBackground);

        View sliderMarkView = findViewById(R.id.room_members_activity_menu_slide_mark_view);

        ViewGroup.LayoutParams layoutParams = sliderMarkView.getLayoutParams();
        layoutParams.height = Design.SLIDE_MARK_HEIGHT;

        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.mutate();
        gradientDrawable.setColor(Color.rgb(244, 244, 244));
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        sliderMarkView.setBackground(gradientDrawable);

        float corner = ((float)Design.SLIDE_MARK_HEIGHT / 2) * Resources.getSystem().getDisplayMetrics().density;
        gradientDrawable.setCornerRadius(corner);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) sliderMarkView.getLayoutParams();
        marginLayoutParams.topMargin = Design.SLIDE_MARK_TOP_MARGIN;

        mAvatarView = findViewById(R.id.room_members_activity_menu_avatar_view);

        layoutParams = mAvatarView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_AVATAR_SIZE * Design.HEIGHT_RATIO);

        marginLayoutParams = (MarginLayoutParams) mAvatarView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_AVATAR_MARGIN * Design.HEIGHT_RATIO);

        mNameView = findViewById(R.id.room_members_activity_menu_name_view);
        Design.updateTextFont(mNameView, Design.FONT_MEDIUM34);
        mNameView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (MarginLayoutParams) mNameView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_TITLE_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_TITLE_BOTTOM_MARGIN * Design.HEIGHT_RATIO);

        mInviteView = findViewById(R.id.room_members_activity_menu_invite_view);
        mInviteView.setOnClickListener(v -> onInviteMemberClick());

        layoutParams = mInviteView.getLayoutParams();
        layoutParams.height = Design.SECTION_HEIGHT;

        ImageView inviteImageView = findViewById(R.id.room_members_activity_menu_invite_image_view);
        inviteImageView.setColorFilter(Design.BLACK_COLOR);

        mInviteTextView = findViewById(R.id.room_members_activity_menu_invite_text_view);
        Design.updateTextFont(mInviteTextView, Design.FONT_MEDIUM34);
        mInviteTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mAdminView = findViewById(R.id.room_members_activity_menu_admin_view);
        mAdminView.setOnClickListener(v -> onChangeAdminClick());

        layoutParams = mAdminView.getLayoutParams();
        layoutParams.height = Design.SECTION_HEIGHT;

        ImageView adminImageView = findViewById(R.id.room_members_activity_menu_admin_image_view);
        adminImageView.setColorFilter(Design.BLACK_COLOR);

        mAdminTextView = findViewById(R.id.room_members_activity_menu_admin_text_view);
        Design.updateTextFont(mAdminTextView, Design.FONT_MEDIUM34);
        mAdminTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mRemoveView = findViewById(R.id.room_members_activity_menu_remove_view);
        mRemoveView.setOnClickListener(v -> onRemoveMemberClick());

        layoutParams = mRemoveView.getLayoutParams();
        layoutParams.height = Design.SECTION_HEIGHT;

        TextView removeTextView = findViewById(R.id.room_members_activity_menu_remove_text_view);
        Design.updateTextFont(removeTextView, Design.FONT_MEDIUM34);
        removeTextView.setTextColor(Design.FONT_COLOR_RED);
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

    @Override
    public void onGlobalLayout() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGlobalLayout");
        }
    }

    private int getActionViewHeight() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getActionViewHeight");
        }

        int countAction = 0;
        if (mAdminView.getVisibility() == View.VISIBLE) {
            countAction++;
        }

        if (mInviteView.getVisibility() == View.VISIBLE) {
            countAction++;
        }

        if (mRemoveView.getVisibility() == View.VISIBLE) {
            countAction++;
        }

        int actionViewHeight = Design.SLIDE_MARK_TOP_MARGIN + Design.SLIDE_MARK_HEIGHT + Design.SECTION_HEIGHT * countAction + (int) ((DESIGN_TITLE_MARGIN + DESIGN_AVATAR_MARGIN + DESIGN_AVATAR_SIZE + DESIGN_TITLE_BOTTOM_MARGIN) * Design.HEIGHT_RATIO);

        int bottomInset = 0;
        View rootView = ((Activity) getContext()).getWindow().getDecorView();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            WindowInsets insets = rootView.getRootWindowInsets();
            if (insets != null) {
                bottomInset = insets.getInsets(WindowInsets.Type.systemBars()).bottom;
            }
        }

        mActionView.setPadding(0, 0, 0, bottomInset);
        return actionViewHeight + mNameView.getHeight() + bottomInset;
    }
}
