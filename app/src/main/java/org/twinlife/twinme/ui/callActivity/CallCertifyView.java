/*
 *  Copyright (c) 2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.callActivity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.percentlayout.widget.PercentRelativeLayout;

import com.airbnb.lottie.LottieAnimationView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.calls.keycheck.WordCheckChallenge;
import org.twinlife.twinme.skin.CircularImageDescriptor;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.CircularImageView;
import org.twinlife.twinme.utils.CommonUtils;
import org.twinlife.twinme.utils.RoundedView;

import java.util.ArrayList;
import java.util.List;

public class CallCertifyView extends PercentRelativeLayout  {
    private static final String LOG_TAG = "CallCertifyView";
    private static final boolean DEBUG = false;

    private static final int DESIGN_CANCEL_COLOR = Color.rgb(253, 96, 83);
    private static final int DESIGN_CONFIRM_COLOR = Color.rgb(78, 229, 184);
    protected static final float DESIGN_BACKGROUND_MARGIN = 342f;

    public interface CallCertifyListener {

        void onCancelWord();

        void onConfirmWord();

        void onCertifyViewFinish();

        void onCertifyViewSingleTap();
    }

    private static final int DESIGN_NAME_TOP_MARGIN = 18;
    private static final int DESIGN_NAME_BOTTOM_MARGIN = 30;
    private static final int DESIGN_TITLE_BOTTOM_MARGIN = 20;
    private static final int DESIGN_MESSAGE_WORD_MARGIN = 80;
    private static final int DESIGN_PROGRESS_MARGIN = 40;
    private static final int DESIGN_ACTION_SIZE = 120;
    private static final int DESIGN_ACTION_MARGIN = 60;
    private static final int DESIGN_ACTION_BOTTOM_MARGIN = 234;
    private static final int DESIGN_ACTION_BOTTOM_LANDSCAPE_MARGIN = 80;
    private static final int DESIGN_SUCCESS_BOTTOM_MARGIN = 400;
    private static final int DESIGN_AVATAR_SIZE = 180;
    private static final int DESIGN_ICON_SIZE = 42;
    private static final int DESIGN_ICON_MARGIN = 20;
    private static final int DESIGN_BULLET_SIZE = 26;
    private static final int DESIGN_BULLET_BOTTOM_MARGIN = 40;
    private static final int DESIGN_BULLET_SIDE_MARGIN = 16;

    private CallCertifyListener mCallCertifyListener;

    private View mCancelView;
    private View mConfirmView;
    private TextView mTitleView;
    private TextView mMessageView;
    private TextSwitcher mWordView;
    private View mCertifiedView;
    private TextView mNameView;
    private TextView mProgressView;
    private CircularImageView mAvatarView;
    private TextView mSuccessMessageView;
    private LottieAnimationView mAnimationView;
    private View mBulletsView;
    private RoundedView mBulletTwoView;
    private RoundedView mBulletThreeView;
    private RoundedView mBulletFourView;
    private RoundedView mBulletFiveView;

    private WordCheckChallenge mWordCheckChallenge;
    private String mName;

    public CallCertifyView(Context context) {

        super(context);
    }

    public CallCertifyView(Context context, AttributeSet attrs) {

        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.call_activity_certify_view, this, true);
        initViews();
    }

    public CallCertifyView(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs, defStyle);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        MarginLayoutParams marginLayoutParams = (MarginLayoutParams) mCancelView.getLayoutParams();
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            marginLayoutParams.bottomMargin = (int) (DESIGN_ACTION_BOTTOM_LANDSCAPE_MARGIN * Design.HEIGHT_RATIO);
        } else {
            marginLayoutParams.bottomMargin = (int) (DESIGN_ACTION_BOTTOM_MARGIN * Design.HEIGHT_RATIO);
        }
    }

    public void setCallCertifyListener(CallCertifyListener callCertifyListener) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setCallCertifyListener: " + callCertifyListener);
        }

        mCallCertifyListener = callCertifyListener;
    }

    public void setName(String name) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setName=" + name);
        }

        if (name != null) {
            mName = name;
            mNameView.setText(mName);
            mMessageView.setText(String.format(getResources().getString(R.string.call_activity_repeat_word), mName));
            mSuccessMessageView.setText(String.format(getResources().getString(R.string.authentified_relation_activity_certified_message), mName));
        }
    }

    public void setAvatar(Context context, Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setAvatar=" + avatar);
        }

        if (avatar != null) {
            mAvatarView.setImage(context, null,
                    new CircularImageDescriptor(avatar, 0.5f, 0.5f, 0.5f));
        }
    }

    @SuppressLint("DefaultLocale")
    public void setCurrentWord(WordCheckChallenge wordCheckChallenge) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setCurrentWord=" + wordCheckChallenge);
        }

        if (wordCheckChallenge == null) {
            return;
        }

        if (mWordCheckChallenge == null || mWordCheckChallenge.index != wordCheckChallenge.index) {
            mWordCheckChallenge = wordCheckChallenge;
            mWordView.setText(mWordCheckChallenge.word.toUpperCase());
            mProgressView.setText(String.format("%d / 5", wordCheckChallenge.index + 1));
            updateBulletsView(mWordCheckChallenge.index);
        }

        if (mWordCheckChallenge.checker) {
            mTitleView.setText(getResources().getString(R.string.call_activity_confirm_word_title));
            mMessageView.setText(String.format(getResources().getString(R.string.call_activity_confirm_word), mName));
            mCancelView.setVisibility(VISIBLE);
            mConfirmView.setVisibility(VISIBLE);
        } else {
            mTitleView.setText(getResources().getString(R.string.call_activity_repeat_word_title));
            mMessageView.setText(String.format(getResources().getString(R.string.call_activity_repeat_word), mName));
            mCancelView.setVisibility(INVISIBLE);
            mConfirmView.setVisibility(INVISIBLE);
        }
    }

    public void certifyRelationSuccess() {
        if (DEBUG) {
            Log.d(LOG_TAG, "certifyRelationSuccess");
        }

        mCertifiedView.setVisibility(VISIBLE);
        mCancelView.setVisibility(GONE);
        mConfirmView.setVisibility(GONE);
        mWordView.setVisibility(GONE);
        mTitleView.setVisibility(GONE);
        mMessageView.setVisibility(GONE);
        mProgressView.setVisibility(GONE);
        mBulletsView.setVisibility(GONE);

        startSuccessAnimation();

        Handler handler = new Handler();
        Runnable runnable = () -> mCallCertifyListener.onCertifyViewFinish();
        handler.postDelayed(runnable, 5000);
    }

    public void certifyRelationFailed() {
        if (DEBUG) {
            Log.d(LOG_TAG, "certifyRelationFailed");
        }

        mCancelView.setVisibility(GONE);
        mConfirmView.setVisibility(GONE);
        mTitleView.setVisibility(GONE);
        mMessageView.setText(String.format(getResources().getString(R.string.call_activity_certify_error_message), mName));
        mWordCheckChallenge = null;
        resetBulletsView();
        Handler handler = new Handler();
        Runnable runnable = () -> mCallCertifyListener.onCertifyViewFinish();
        handler.postDelayed(runnable, 5000);
    }

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        View rootView = findViewById(R.id.call_activity_certify_view_root);

        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.mutate();
        gradientDrawable.setColors(new int[]{Design.BOTTOM_GRADIENT_START_COLOR, Design.BOTTOM_GRADIENT_END_COLOR});
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        rootView.setBackground(gradientDrawable);

        View backgroundView = findViewById(R.id.call_activity_certify_view_background);
        backgroundView.setOnClickListener(v -> mCallCertifyListener.onCertifyViewSingleTap());

        MarginLayoutParams marginLayoutParams = (MarginLayoutParams) backgroundView.getLayoutParams();
        marginLayoutParams.bottomMargin = (int) (DESIGN_BACKGROUND_MARGIN * Design.HEIGHT_RATIO);

        mTitleView = findViewById(R.id.call_activity_certify_view_title);
        Design.updateTextFont(mTitleView, Design.FONT_MEDIUM44);
        mTitleView.setTextColor(Color.WHITE);

        marginLayoutParams = (MarginLayoutParams) mTitleView.getLayoutParams();
        marginLayoutParams.bottomMargin = (int) (DESIGN_TITLE_BOTTOM_MARGIN * Design.HEIGHT_RATIO);

        mMessageView = findViewById(R.id.call_activity_certify_view_message);
        Design.updateTextFont(mMessageView, Design.FONT_MEDIUM38);
        mMessageView.setTextColor(Color.argb(180, 255, 255, 255));

        mWordView = findViewById(R.id.call_activity_certify_view_word);
        mWordView.setFactory(() -> {
            TextView textView = new TextView(getContext());
            Design.updateTextFont(textView, Design.FONT_BOLD88);
            textView.setTextColor(Color.WHITE);
            textView.setGravity(Gravity.CENTER);
            return textView;
        });

        Animation in = AnimationUtils.loadAnimation(getContext(), android.R.anim.slide_in_left);
        Animation out = AnimationUtils.loadAnimation(getContext(), android.R.anim.slide_out_right);
        mWordView.setInAnimation(in);
        mWordView.setOutAnimation(out);

        marginLayoutParams = (MarginLayoutParams) mWordView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_MESSAGE_WORD_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_MESSAGE_WORD_MARGIN * Design.HEIGHT_RATIO);

        mBulletsView = findViewById(R.id.call_activity_certify_view_bullets_view);

        ViewGroup.LayoutParams layoutParams = mBulletsView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_BULLET_SIZE * Design.HEIGHT_RATIO);

        marginLayoutParams = (MarginLayoutParams) mBulletsView.getLayoutParams();
        marginLayoutParams.bottomMargin = (int) (DESIGN_BULLET_BOTTOM_MARGIN * Design.HEIGHT_RATIO);

        RoundedView bulletOneView = findViewById(R.id.call_activity_certify_view_bullet_one_view);
        bulletOneView.setBorder(4, Color.WHITE);
        bulletOneView.setColor(Color.WHITE);

        layoutParams = bulletOneView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_BULLET_SIZE * Design.HEIGHT_RATIO);

        marginLayoutParams = (MarginLayoutParams) bulletOneView.getLayoutParams();
        if (CommonUtils.isLayoutDirectionRTL()) {
            marginLayoutParams.leftMargin = (int) (DESIGN_BULLET_SIDE_MARGIN * Design.WIDTH_RATIO);
        } else {
            marginLayoutParams.rightMargin = (int) (DESIGN_BULLET_SIDE_MARGIN * Design.WIDTH_RATIO);
        }

        mBulletTwoView = findViewById(R.id.call_activity_certify_view_bullet_two_view);
        mBulletTwoView.setBorder(4, Color.argb(122, 255, 255, 255));
        mBulletTwoView.setColor(Color.TRANSPARENT);

        layoutParams = mBulletTwoView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_BULLET_SIZE * Design.HEIGHT_RATIO);

        marginLayoutParams = (MarginLayoutParams) mBulletTwoView.getLayoutParams();
        if (CommonUtils.isLayoutDirectionRTL()) {
            marginLayoutParams.leftMargin = (int) (DESIGN_BULLET_SIDE_MARGIN * Design.WIDTH_RATIO);
        } else {
            marginLayoutParams.rightMargin = (int) (DESIGN_BULLET_SIDE_MARGIN * Design.WIDTH_RATIO);
        }

        mBulletThreeView = findViewById(R.id.call_activity_certify_view_bullet_three_view);
        mBulletThreeView.setBorder(4, Color.argb(122, 255, 255, 255));
        mBulletThreeView.setColor(Color.TRANSPARENT);

        layoutParams = mBulletThreeView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_BULLET_SIZE * Design.HEIGHT_RATIO);

        mBulletFourView = findViewById(R.id.call_activity_certify_view_bullet_four_view);
        mBulletFourView.setBorder(4, Color.argb(122, 255, 255, 255));
        mBulletFourView.setColor(Color.TRANSPARENT);

        layoutParams = mBulletFourView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_BULLET_SIZE * Design.HEIGHT_RATIO);

        marginLayoutParams = (MarginLayoutParams) mBulletFourView.getLayoutParams();
        if (CommonUtils.isLayoutDirectionRTL()) {
            marginLayoutParams.rightMargin = (int) (DESIGN_BULLET_SIDE_MARGIN * Design.WIDTH_RATIO);
        } else {
            marginLayoutParams.leftMargin = (int) (DESIGN_BULLET_SIDE_MARGIN * Design.WIDTH_RATIO);
        }

        mBulletFiveView = findViewById(R.id.call_activity_certify_view_bullet_five_view);
        mBulletFiveView.setBorder(4, Color.argb(122, 255, 255, 255));
        mBulletFiveView.setColor(Color.TRANSPARENT);

        layoutParams = mBulletFiveView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_BULLET_SIZE * Design.HEIGHT_RATIO);

        marginLayoutParams = (MarginLayoutParams) mBulletFiveView.getLayoutParams();
        if (CommonUtils.isLayoutDirectionRTL()) {
            marginLayoutParams.rightMargin = (int) (DESIGN_BULLET_SIDE_MARGIN * Design.WIDTH_RATIO);
        } else {
            marginLayoutParams.leftMargin = (int) (DESIGN_BULLET_SIDE_MARGIN * Design.WIDTH_RATIO);
        }

        mAnimationView = findViewById(R.id.call_activity_certify_view_animation_view);
        mAnimationView.setVisibility(INVISIBLE);

        layoutParams = mAnimationView.getLayoutParams();
        layoutParams.width = Design.DISPLAY_WIDTH;
        layoutParams.height = (int) (Design.DISPLAY_HEIGHT * 0.5);

        mAnimationView.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animation) {
                mAnimationView.setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationEnd(@NonNull Animator animation) {

                PropertyValuesHolder propertyValuesHolderAlpha = PropertyValuesHolder.ofFloat(View.ALPHA, 1, 0);
                ObjectAnimator alphaAnimator = ObjectAnimator.ofPropertyValuesHolder(mAnimationView, propertyValuesHolderAlpha);

                List<Animator> animators = new ArrayList<>();
                animators.add(alphaAnimator);

                AnimatorSet animationSet = new AnimatorSet();
                animationSet.setDuration(2000);
                animationSet.setInterpolator(new DecelerateInterpolator());
                animationSet.playTogether(animators);
                animationSet.start();
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animation) {

            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animation) {

            }
        });

        mProgressView = findViewById(R.id.call_activity_certify_view_progress);
        Design.updateTextFont(mProgressView, Design.FONT_BOLD44);
        mProgressView.setTextColor(Color.WHITE);
        mProgressView.setVisibility(GONE);

        layoutParams = mProgressView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_ACTION_SIZE * Design.HEIGHT_RATIO);

        marginLayoutParams = (MarginLayoutParams) mProgressView.getLayoutParams();
        marginLayoutParams.bottomMargin = (int) (DESIGN_PROGRESS_MARGIN * Design.HEIGHT_RATIO);

        mCancelView = findViewById(R.id.call_activity_certify_view_decline_view);
        mCancelView.setOnClickListener(view -> onCancelClick());

        layoutParams = mCancelView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_ACTION_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_ACTION_SIZE * Design.HEIGHT_RATIO);

        marginLayoutParams = (MarginLayoutParams) mCancelView.getLayoutParams();
        if (CommonUtils.isLayoutDirectionRTL()) {
            marginLayoutParams.leftMargin = (int) (DESIGN_ACTION_MARGIN * Design.WIDTH_RATIO);
        } else {
            marginLayoutParams.rightMargin = (int) (DESIGN_ACTION_MARGIN * Design.WIDTH_RATIO);
        }

        int orientation = getContext().getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            marginLayoutParams.bottomMargin = (int) (DESIGN_ACTION_BOTTOM_LANDSCAPE_MARGIN * Design.HEIGHT_RATIO);
        } else {
            marginLayoutParams.bottomMargin = (int) (DESIGN_ACTION_BOTTOM_MARGIN * Design.HEIGHT_RATIO);
        }

        RoundedView cancelRoundedView = findViewById(R.id.call_activity_certify_decline_background_view);
        cancelRoundedView.setColor(DESIGN_CANCEL_COLOR);

        mConfirmView = findViewById(R.id.call_activity_certify_confirm_view);
        mConfirmView.setOnClickListener(view -> onConfirmClick());

        layoutParams = mConfirmView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_ACTION_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_ACTION_SIZE * Design.HEIGHT_RATIO);

        RoundedView confirmRoundedView = findViewById(R.id.call_activity_certify_confirm_background_view);
        confirmRoundedView.setColor(DESIGN_CONFIRM_COLOR);

        mCertifiedView = findViewById(R.id.call_activity_certify_view_success_view);

        marginLayoutParams = (MarginLayoutParams) mCertifiedView.getLayoutParams();
        marginLayoutParams.bottomMargin = (int) (DESIGN_SUCCESS_BOTTOM_MARGIN * Design.HEIGHT_RATIO);

        mAvatarView = findViewById(R.id.call_activity_certify_view_avatar_view);

        layoutParams = mAvatarView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_AVATAR_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_AVATAR_SIZE * Design.HEIGHT_RATIO);

        mNameView = findViewById(R.id.call_activity_certify_view_name_view);
        Design.updateTextFont(mNameView, Design.FONT_MEDIUM34);
        mNameView.setTextColor(Color.WHITE);

        marginLayoutParams = (MarginLayoutParams) mNameView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_NAME_TOP_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_NAME_BOTTOM_MARGIN * Design.HEIGHT_RATIO);

        ImageView iconCertifiedView = findViewById(R.id.call_activity_certify_view_certified_image_view);

        layoutParams = iconCertifiedView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_ICON_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_ICON_SIZE * Design.HEIGHT_RATIO);

        marginLayoutParams = (MarginLayoutParams) iconCertifiedView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_ICON_MARGIN * Design.WIDTH_RATIO);

        mSuccessMessageView = findViewById(R.id.call_activity_certify_view_certified_message_view);
        Design.updateTextFont(mSuccessMessageView, Design.FONT_REGULAR32);
        mSuccessMessageView.setTextColor(Color.WHITE);
    }

    private void onCancelClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCancelClick");
        }

        mCallCertifyListener.onCancelWord();
        mCallCertifyListener.onCertifyViewFinish();
        mWordCheckChallenge = null;
        resetBulletsView();
    }

    private void onConfirmClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onConfirmClick");
        }

        mCallCertifyListener.onConfirmWord();
    }

    private void startSuccessAnimation() {
        if (DEBUG) {
            Log.d(LOG_TAG, "startSuccessAnimation");
        }

        mAnimationView.setVisibility(VISIBLE);
        if (!mAnimationView.isAnimating()) {
            mAnimationView.playAnimation();
        }
    }

    private void updateBulletsView(int index) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateBulletsView");
        }

        if (index > 0) {
            mBulletTwoView.setBorder(4, Color.WHITE);
            mBulletTwoView.setColor(Color.WHITE);
        }

        if (index > 1) {
            mBulletThreeView.setBorder(4, Color.WHITE);
            mBulletThreeView.setColor(Color.WHITE);
        }

        if (index > 2) {
            mBulletFourView.setBorder(4, Color.WHITE);
            mBulletFourView.setColor(Color.WHITE);
        }

        if (index > 3) {
            mBulletFiveView.setBorder(4, Color.WHITE);
            mBulletFiveView.setColor(Color.WHITE);
        }
    }

    private void resetBulletsView() {
        if (DEBUG) {
            Log.d(LOG_TAG, "resetBulletsView");
        }

        mBulletTwoView.setBorder(4, Color.argb(122, 255, 255, 255));
        mBulletTwoView.setColor(Color.TRANSPARENT);

        mBulletThreeView.setBorder(4, Color.argb(122, 255, 255, 255));
        mBulletThreeView.setColor(Color.TRANSPARENT);

        mBulletFourView.setBorder(4, Color.argb(122, 255, 255, 255));
        mBulletFourView.setColor(Color.TRANSPARENT);

        mBulletFiveView.setBorder(4, Color.argb(122, 255, 255, 255));
        mBulletFiveView.setColor(Color.TRANSPARENT);
    }
}