/*
 *  Copyright (c) 2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.utils;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.percentlayout.widget.PercentRelativeLayout;

import com.google.android.material.imageview.ShapeableImageView;

import org.twinlife.twinme.skin.Design;

import java.util.ArrayList;
import java.util.List;

public class AbstractConfirmView extends PercentRelativeLayout {
    private static final String LOG_TAG = "AbstractConfirmView";
    private static final boolean DEBUG = false;

    public interface Observer {

        void onConfirmClick();

        void onCancelClick();

        void onDismissClick();

        void onCloseViewAnimationEnd(boolean fromConfirmAction);
    }

    protected static final int DESIGN_AVATAR_MARGIN = 60;
    protected static final int DESIGN_AVATAR_HEIGHT = 148;
    protected static final int DESIGN_ICON_VIEW_SIZE = 72;
    private static final int DESIGN_ICON_IMAGE_VIEW_HEIGHT = 32;
    protected static final int DESIGN_BULLET_VIEW_SIZE = 26;
    private static final int DESIGN_BULLET_VIEW_MARGIN = 20;
    protected static final int DESIGN_TITLE_MARGIN = 60;
    protected static final int DESIGN_MESSAGE_MARGIN = 30;
    protected static final int DESIGN_CONFIRM_MARGIN = 80;
    private static final int DESIGN_CONFIRM_VERTICAL_MARGIN = 10;
    private static final int DESIGN_CONFIRM_HORIZONTAL_MARGIN = 20;
    protected static final int DESIGN_CANCEL_HEIGHT = 140;
    protected static final int DESIGN_CANCEL_MARGIN = 20;

    protected View mOverlayView;
    protected View mActionView;
    protected View mSlideMarkView;
    protected TextView mTitleView;
    protected TextView mMessageView;
    protected ShapeableImageView mAvatarView;
    protected View mIconView;
    protected ImageView mIconImageView;
    protected View mBulletView;
    protected View mConfirmView;
    protected TextView mConfirmTextView;
    protected View mCancelView;
    protected TextView mCancelTextView;

    private int mRootHeight = 0;
    private int mActionHeight = 0;

    private boolean isOpenAnimationEnded = false;
    private boolean isCloseAnimationEnded = false;

    protected boolean mDefaultGroupAvatar = false;
    private Bitmap mAvatar;
    private int mIcon = -1;
    private int mIconColor = Color.TRANSPARENT;
    private String mTitle;
    private SpannableStringBuilder mSpannableTitle;
    private String mMessage;
    private String mConfirmTitle;
    private String mCancelTitle;

    private boolean mIsConfirmAction = false;
    private boolean mForceDarkMode = false;

    @Nullable
    private Observer mObserver;

    public AbstractConfirmView(Context context) {
        super(context);
    }

    public AbstractConfirmView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setObserver(Observer observer) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setObserver: " + observer);
        }

        mObserver = observer;
    }

    public void setConfirmTitle(String title) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setConfirmTitle: " + title);
        }

        mConfirmTitle = title;
    }

    public void setCancelTitle(String title) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setCancelTitle: " + title);
        }

        mCancelTitle = title;
    }

    public void setAvatar(Bitmap avatar, boolean isDefaultGroupAvatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setAvatar: " + avatar);
        }

        mAvatar = avatar;
        mDefaultGroupAvatar = isDefaultGroupAvatar;
    }

    public void setTitle(String title) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setTitle: " + title);
        }

        mTitle = title;
    }

    public void setSpannableTitle(SpannableStringBuilder title) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setSpannableTitle: " + title);
        }

        mSpannableTitle = title;
    }

    public void setMessage(String message) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setMessage: " + message);
        }

        mMessage = message;
    }

    public void setIcon(int icon) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setIcon: " + icon);
        }

        mIcon = icon;
    }

    public void setIconTintColor(int color) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setIconTintColor: " + color);
        }

        mIconColor = color;
    }

    public void setForceDarkMode(boolean forceDarkMode) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setForceDarkMode: " + forceDarkMode);
        }

        mForceDarkMode = forceDarkMode;
    }

    public void show() {
        if (DEBUG) {
            Log.d(LOG_TAG, "show");
        }

        updateViews();

        ViewTreeObserver viewTreeObserver = mActionView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewTreeObserver viewTreeObserver = mActionView.getViewTreeObserver();
                viewTreeObserver.removeOnGlobalLayoutListener(this);

                mRootHeight = mOverlayView.getHeight();
                mActionHeight = mActionView.getHeight();

                showConfirmView();
            }
        });
    }

    public void animationOpenConfirmView() {
        if (DEBUG) {
            Log.d(LOG_TAG, "animationOpenConfirmView");
        }

        if (isOpenAnimationEnded) {
            return;
        }

        mOverlayView.setAlpha(1.0f);

        int startValue = mRootHeight;
        int endValue = mRootHeight - mActionHeight;

        List<Animator> animators = new ArrayList<>();

        PropertyValuesHolder propertyValuesHolder = PropertyValuesHolder.ofFloat(View.Y, startValue, endValue);

        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(mActionView, propertyValuesHolder);
        objectAnimator.setDuration(Design.ANIMATION_VIEW_DURATION);
        animators.add(objectAnimator);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animators);
        animatorSet.start();
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animator) {

            }

            @Override
            public void onAnimationEnd(@NonNull Animator animator) {

                if (mRootHeight != mOverlayView.getHeight()) {
                    mRootHeight = mOverlayView.getHeight();
                    mActionView.setY(mRootHeight - mActionHeight);
                }
                isOpenAnimationEnded = true;
                onFinishOpenAnimation();
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animator) {

            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animator) {

            }
        });
    }

    public void animationCloseConfirmView() {
        if (DEBUG) {
            Log.d(LOG_TAG, "animationCloseConfirmView");
        }

        if (isCloseAnimationEnded) {
            return;
        }

        int startValue = mRootHeight - mActionHeight;
        int endValue = mRootHeight;

        List<Animator> animators = new ArrayList<>();

        PropertyValuesHolder propertyValuesHolder = PropertyValuesHolder.ofFloat(View.Y, startValue, endValue);

        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(mActionView, propertyValuesHolder);
        objectAnimator.setDuration(Design.ANIMATION_VIEW_DURATION);
        animators.add(objectAnimator);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animators);
        animatorSet.start();
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animator) {

            }

            @Override
            public void onAnimationEnd(@NonNull Animator animator) {

                isCloseAnimationEnded = true;
                mOverlayView.setAlpha(0f);

                if (mObserver != null) {
                    mObserver.onCloseViewAnimationEnd(mIsConfirmAction);
                }
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animator) {

            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animator) {

            }
        });
    }

    protected void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        mOverlayView.setBackgroundColor(Design.OVERLAY_VIEW_COLOR);
        mOverlayView.setAlpha(0);
        mOverlayView.setOnClickListener(v -> onDismissClick());

        View rootView = ((Activity) getContext()).getWindow().getDecorView();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            WindowInsets insets = rootView.getRootWindowInsets();
            if (insets != null) {
                int bottomInset = insets.getInsets(WindowInsets.Type.systemBars()).bottom;
                mActionView.setPadding(0, 0, 0, bottomInset);
            }
        }

        mActionView.setY(Design.DISPLAY_HEIGHT);

        float radius = Design.ACTION_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, 0, 0, 0, 0};

        ShapeDrawable scrollIndicatorBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        scrollIndicatorBackground.getPaint().setColor(Design.POPUP_BACKGROUND_COLOR);
        mActionView.setBackground(scrollIndicatorBackground);

        ViewGroup.LayoutParams layoutParams = mSlideMarkView.getLayoutParams();
        layoutParams.height = Design.SLIDE_MARK_HEIGHT;

        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.mutate();
        gradientDrawable.setColor(Color.rgb(244, 244, 244));
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        mSlideMarkView.setBackground(gradientDrawable);

        float corner = ((float)Design.SLIDE_MARK_HEIGHT / 2) * Resources.getSystem().getDisplayMetrics().density;
        gradientDrawable.setCornerRadius(corner);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mSlideMarkView.getLayoutParams();
        marginLayoutParams.topMargin = Design.SLIDE_MARK_TOP_MARGIN;

        if (mAvatarView != null) {
            layoutParams = mAvatarView.getLayoutParams();
            mAvatarView.setBackgroundColor(Design.WHITE_COLOR);
            layoutParams.height = (int) (DESIGN_AVATAR_HEIGHT * Design.HEIGHT_RATIO);

            marginLayoutParams = (ViewGroup.MarginLayoutParams) mAvatarView.getLayoutParams();
            marginLayoutParams.topMargin = (int) (DESIGN_AVATAR_MARGIN * Design.HEIGHT_RATIO);
        }

        if (mIconView != null) {
            layoutParams = mIconView.getLayoutParams();
            layoutParams.width = (int) (DESIGN_ICON_VIEW_SIZE * Design.HEIGHT_RATIO);
            layoutParams.height = (int) (DESIGN_ICON_VIEW_SIZE * Design.HEIGHT_RATIO);

            marginLayoutParams = (ViewGroup.MarginLayoutParams) mIconView.getLayoutParams();
            marginLayoutParams.leftMargin = (int) (-DESIGN_BULLET_VIEW_MARGIN * Design.WIDTH_RATIO);

            GradientDrawable iconBackgroundDrawable = new GradientDrawable();
            iconBackgroundDrawable.setColor(Design.DELETE_COLOR_RED);
            iconBackgroundDrawable.setCornerRadius((int) ((DESIGN_ICON_VIEW_SIZE * Design.HEIGHT_RATIO) * 0.5));
            iconBackgroundDrawable.setStroke(8, Color.WHITE);
            mIconView.setBackground(iconBackgroundDrawable);
        }

        if (mIconImageView != null) {
            mIconImageView.setColorFilter(Color.WHITE);

            layoutParams = mIconImageView.getLayoutParams();
            layoutParams.height = (int) (DESIGN_ICON_IMAGE_VIEW_HEIGHT * Design.HEIGHT_RATIO);
        }

        if (mBulletView != null) {
            layoutParams = mBulletView.getLayoutParams();
            layoutParams.width = (int) (DESIGN_BULLET_VIEW_SIZE * Design.HEIGHT_RATIO);
            layoutParams.height = (int) (DESIGN_BULLET_VIEW_SIZE * Design.HEIGHT_RATIO);

            marginLayoutParams = (ViewGroup.MarginLayoutParams) mBulletView.getLayoutParams();
            marginLayoutParams.rightMargin = (int) (DESIGN_BULLET_VIEW_MARGIN * Design.WIDTH_RATIO);
            marginLayoutParams.topMargin = (int) ((DESIGN_ICON_VIEW_SIZE - DESIGN_BULLET_VIEW_SIZE) * Design.HEIGHT_RATIO);

            GradientDrawable bulletBackgroundDrawable = new GradientDrawable();
            bulletBackgroundDrawable.setColor(Design.DELETE_COLOR_RED);
            bulletBackgroundDrawable.setCornerRadius((int) ((DESIGN_BULLET_VIEW_SIZE * Design.HEIGHT_RATIO) * 0.5));
            bulletBackgroundDrawable.setStroke(8, Color.WHITE);
            mBulletView.setBackground(bulletBackgroundDrawable);
        }

        if (mTitleView != null) {
            Design.updateTextFont(mTitleView, Design.FONT_BOLD44);
            mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

            marginLayoutParams = (ViewGroup.MarginLayoutParams) mTitleView.getLayoutParams();
            marginLayoutParams.topMargin = (int) (DESIGN_TITLE_MARGIN * Design.HEIGHT_RATIO);
        }

        Design.updateTextFont(mMessageView, Design.FONT_MEDIUM40);
        mMessageView.setTextColor(Design.FONT_COLOR_GREY);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mMessageView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_MESSAGE_MARGIN * Design.HEIGHT_RATIO);

        mConfirmView.setOnClickListener(v -> onConfirmClick());

        radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable saveViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        saveViewBackground.getPaint().setColor(Design.DELETE_COLOR_RED);
        mConfirmView.setBackground(saveViewBackground);

        layoutParams = mConfirmView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;

        mConfirmView.setMinimumHeight(Design.BUTTON_HEIGHT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mConfirmView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_CONFIRM_MARGIN * Design.HEIGHT_RATIO);

        Design.updateTextFont(mConfirmTextView, Design.FONT_BOLD36);
        mConfirmTextView.setTextColor(Color.WHITE);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mConfirmTextView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_CONFIRM_VERTICAL_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_CONFIRM_VERTICAL_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.leftMargin = (int) (DESIGN_CONFIRM_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_CONFIRM_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);

        if (mCancelView != null) {
            mCancelView.setOnClickListener(v -> onCancelClick());

            layoutParams = mCancelView.getLayoutParams();
            layoutParams.height = (int) (DESIGN_CANCEL_HEIGHT * Design.HEIGHT_RATIO);

            marginLayoutParams = (ViewGroup.MarginLayoutParams) mCancelView.getLayoutParams();
            marginLayoutParams.bottomMargin = (int) (DESIGN_CANCEL_MARGIN * Design.HEIGHT_RATIO);

            Design.updateTextFont(mCancelTextView, Design.FONT_BOLD36);
            mCancelTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        }
    }

    private void updateViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateViews");
        }

        if (mIcon != -1 && mIconImageView != null) {
            mIconImageView.setColorFilter(mIconColor);
            mIconImageView.setImageResource(mIcon);
        }

        if (mAvatar != null && mAvatarView != null) {
            mAvatarView.setImageBitmap(mAvatar);
            mAvatarView.setVisibility(VISIBLE);

            if (mDefaultGroupAvatar) {
                mAvatarView.setColorFilter(Color.WHITE);
                mAvatarView.setBackgroundColor(Color.parseColor(Design.DEFAULT_COLOR));
            }
        }

        if (mTitle != null && mTitleView != null) {
            mTitleView.setText(mTitle);
        }

        if (mSpannableTitle != null && mTitleView != null) {
            mTitleView.setText(mSpannableTitle);
        }

        if (mMessage != null && mMessageView != null) {
            mMessageView.setText(mMessage);
        }

        if (mConfirmTitle != null && mConfirmTextView != null) {
            mConfirmTextView.setText(mConfirmTitle);
        }

        if (mCancelTitle != null && mCancelTextView != null) {
            mCancelTextView.setText(mCancelTitle);
        }
    }

    private void onConfirmClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onConfirmClick");
        }

        if (mObserver != null) {
            mIsConfirmAction = true;
            mObserver.onConfirmClick();
        }
    }

    private void onCancelClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCancelClick");
        }

        if (mObserver != null) {
            mObserver.onCancelClick();
        }
    }

    private void onDismissClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDismissClick");
        }

        if (mObserver != null) {
            mObserver.onDismissClick();
        }
    }

    public void showConfirmView() {
        if (DEBUG) {
            Log.d(LOG_TAG, "showConfirmView");
        }

        isOpenAnimationEnded = false;
        isCloseAnimationEnded = false;

        float radius = Design.ACTION_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, 0, 0, 0, 0};
        ShapeDrawable scrollIndicatorBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));

        if (mForceDarkMode) {
            scrollIndicatorBackground.getPaint().setColor(Color.rgb(72,72,72));
            if (mTitleView != null && mCancelTextView != null) {
                mTitleView.setTextColor(Color.WHITE);
                mCancelTextView.setTextColor(Color.WHITE);
                mMessageView.setTextColor(Color.WHITE);
            }
        } else {
            scrollIndicatorBackground.getPaint().setColor(Design.POPUP_BACKGROUND_COLOR);
            if (mTitleView != null && mCancelTextView != null) {
                mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);
                mCancelTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
                mMessageView.setTextColor(Design.FONT_COLOR_GREY);
            }
        }

        mActionView.setBackground(scrollIndicatorBackground);

        mActionView.setY(Design.DISPLAY_HEIGHT);
        mActionView.invalidate();
        animationOpenConfirmView();
    }

    protected void onFinishOpenAnimation() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onFinishOpenAnimation");
        }
    }
}
