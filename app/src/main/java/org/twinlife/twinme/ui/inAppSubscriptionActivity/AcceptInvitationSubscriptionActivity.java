/*
 *  Copyright (c) 2023-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.inAppSubscriptionActivity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.BaseService.ErrorCode;
import org.twinlife.twinlife.TwincodeOutbound;
import org.twinlife.twinlife.TwincodeURI;
import org.twinlife.twinlife.util.Logger;
import org.twinlife.twinme.models.Profile;
import org.twinlife.twinme.models.Space;
import org.twinlife.twinme.services.InvitationSubscriptionService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.Intents;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AcceptInvitationSubscriptionActivity extends AbstractTwinmeActivity implements InvitationSubscriptionService.Observer {
    private static final String LOG_TAG = "AcceptInvitationSub...";
    private static final boolean DEBUG = false;

    private static final long ANIMATION_DURATION = 300;
    private static final int ACTION_RADIUS = 40;
    private static final int DESIGN_IMAGE_WIDTH = 400;
    private static final int DESIGN_IMAGE_HEIGHT = 240;
    protected static final int DESIGN_TITLE_MARGIN = 40;
    private static final int DESIGN_MESSAGE_MARGIN = 30;
    private static final int DESIGN_CONFIRM_MARGIN = 60;
    private static final int DESIGN_CONFIRM_VERTICAL_MARGIN = 10;
    private static final int DESIGN_CONFIRM_HORIZONTAL_MARGIN = 20;
    private static final int DESIGN_CANCEL_HEIGHT = 140;
    private static final int DESIGN_CANCEL_MARGIN = 80;

    private View mOverlayView;
    private View mActionView;
    private TextView mNameView;
    private TextView mMessageView;
    private ImageView mSubscriptionImageView;
    private View mConfirmView;
    private View mCancelView;

    private int mRootHeight = 0;
    private int mActionHeight = 0;

    private boolean mShowActionView = false;
    private boolean isOpenAnimationEnded = false;
    private boolean isCloseAnimationEnded = false;

    private class AcceptListener implements View.OnClickListener {

        private boolean disabled = false;

        @Override
        public void onClick(View view) {
            if (DEBUG) {
                Log.d(LOG_TAG, "AcceptListener.onClick: view=" + view);
            }

            if (disabled) {

                return;
            }
            disabled = true;

            onAccept();
        }
    }

    private class DeclineListener implements View.OnClickListener {

        private boolean disabled = false;

        @Override
        public void onClick(View view) {
            if (DEBUG) {
                Log.d(LOG_TAG, "DeclineListener.onClick: view=" + view);
            }

            if (disabled) {

                return;
            }
            disabled = true;

            onDeclineClick();
        }
    }

    private boolean mUIInitialized = false;
    private boolean mHasTwincode = false;

    private String mContactName;
    private String mContactDescription;
    private Bitmap mContactAvatar;

    private Profile mProfile;
    private TwincodeURI mTwincodeURI;

    @Nullable
    private InvitationSubscriptionService mInvitationSubscriptionService;
    //
    // Override TwinlifeActivityImpl methods
    //

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        if (android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        Intent intent = getIntent();
        Uri uri = null;
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            uri = intent.getData();
        } else {
            String invitationLink = intent.getStringExtra(Intents.INTENT_INVITATION_LINK);
            if (invitationLink != null) {
                uri = Uri.parse(invitationLink);
            }
        }
        if (uri == null) {
            finish();
            return;
        }

        initViews();
        setFullscreen();

        mInvitationSubscriptionService = new InvitationSubscriptionService(this, getTwinmeContext(), uri, this);
    }

    //
    // Override Activity methods
    //

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        if (mInvitationSubscriptionService != null) {
            mInvitationSubscriptionService.dispose();
        }
        super.onDestroy();
    }

    @Override
    public void finish() {
        if (DEBUG) {
            Log.d(LOG_TAG, "finish");
        }

        super.finish();
        overridePendingTransition(0, 0);
    }

    //
    // Implement InAppSubscriptionService.Observer methods
    //

    @Override
    public void onGetSpace(@NonNull Space space, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetSpace: space=" + space);
        }

        mProfile = space.getProfile();

        updateViews();
    }

    @Override
    public void onGetSpaceNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetSpaceNotFound");
        }

        mProfile = null;
        updateViews();
    }

    @Override
    public void onGetTwincode(@NonNull TwincodeOutbound twincodeOutbound, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetTwincode: twincodeOutbound=" + twincodeOutbound + " avatar=" + avatar);
        }

        mHasTwincode = true;
        mContactName = twincodeOutbound.getName();
        mContactDescription = twincodeOutbound.getDescription();
        mContactAvatar = avatar != null ? avatar : getDefaultAvatar();

        updateViews();
    }

    @Override
    public void onGetTwincodeNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetTwincodeNotFound");
        }

        error(getString(R.string.accept_invitation_activity_incorrect_contact_information), this::finish);
    }

    @Override
    public void onParseTwincodeURI(@NonNull ErrorCode errorCode, @Nullable TwincodeURI uri) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onParseTwincodeURI errorCode=" + errorCode + " uri=" + uri);
        }

        // @todo Handle errors and report an accurate message:
        // ErrorCode.BAD_REQUEST: link is not well formed or not one of our link
        // ErrorCode.FEATURE_NOT_IMPLEMENTED: link does not target our application or domain.
        // ErrorCode.ITEM_NOT_FOUND: link targets the application but it is not compatible with the version.
        // TwincodeURI.Kind == Kind.AccountMigration => redirect to account migration
        // TwincodeURI.Kind == Kind.Call|Kind.Transfer => forbidden
        mTwincodeURI = uri;
        if (errorCode != ErrorCode.SUCCESS || mTwincodeURI == null
                || mTwincodeURI.twincodeId == null || mTwincodeURI.twincodeOptions == null) {
            error(getString(R.string.accept_invitation_activity_incorrect_contact_information), this::finish);
        }
    }

    @Override
    public void onSubscribeSuccess() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSubscribeSuccess");
        }

        Toast toast = Toast.makeText(this, R.string.in_app_subscription_activity_invitation_code_success, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 0, 0);
        toast.show();

        saveTwincodeAttributes();
        animationCloseInvitationView();
    }

    @Override
    public void onSubscribeFailed(@NonNull ErrorCode errorCode) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSubscribeFailed errorCode=" + errorCode);
        }

        String errorMessage;
        if (errorCode == ErrorCode.EXPIRED) {
            errorMessage = getString(R.string.in_app_subscription_activity_expire_code);
        } else if (errorCode == ErrorCode.LIMIT_REACHED) {
            errorMessage = getString(R.string.in_app_subscription_activity_used_code);
        } else {
            errorMessage = getString(R.string.in_app_subscription_activity_invalid_code);
        }
        error(errorMessage, this::animationCloseInvitationView);
    }

    public void animationOpenInvitationView() {
        if (DEBUG) {
            Log.d(LOG_TAG, "animationOpenInvitationView");
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
        objectAnimator.setDuration(ANIMATION_DURATION);
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

    public void animationCloseInvitationView() {
        if (DEBUG) {
            Log.d(LOG_TAG, "animationCloseInvitationView");
        }

        if (isCloseAnimationEnded) {
            return;
        }

        int startValue = mRootHeight - mActionHeight;
        int endValue = mRootHeight;

        List<Animator> animators = new ArrayList<>();

        PropertyValuesHolder propertyValuesHolder = PropertyValuesHolder.ofFloat(View.Y, startValue, endValue);

        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(mActionView, propertyValuesHolder);
        objectAnimator.setDuration(ANIMATION_DURATION);
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
                finish();
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animator) {

            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animator) {

            }
        });
    }

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color,  ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.WHITE_COLOR));
        setContentView(R.layout.accept_invitation_subscription_activity);
        setBackgroundColor(Color.TRANSPARENT);

        mOverlayView = findViewById(R.id.accept_invitation_subscription_activity_overlay_view);
        mActionView = findViewById(R.id.accept_invitation_subscription_activity_action_view);
        View slideMarkView = findViewById(R.id.accept_invitation_subscription_activity_slide_mark_view);
        mSubscriptionImageView = findViewById(R.id.accept_invitation_subscription_activity_image_view);
        mNameView = findViewById(R.id.accept_invitation_subscription_activity_title_view);
        mMessageView = findViewById(R.id.accept_invitation_subscription_activity_message_view);
        mConfirmView = findViewById(R.id.accept_invitation_subscription_activity_confirm_view);
        TextView confirmTextView = findViewById(R.id.accept_invitation_subscription_activity_confirm_text_view);
        mCancelView = findViewById(R.id.accept_invitation_subscription_activity_cancel_view);
        TextView cancelTextView = findViewById(R.id.accept_invitation_subscription_activity_cancel_text_view);

        mOverlayView.setBackgroundColor(Design.OVERLAY_VIEW_COLOR);
        mOverlayView.setAlpha(0);
        mOverlayView.setOnClickListener(v -> onDeclineClick());

        mActionView.setY(Design.DISPLAY_HEIGHT);

        float radius = ACTION_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, 0, 0, 0, 0};

        ShapeDrawable scrollIndicatorBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        scrollIndicatorBackground.getPaint().setColor(Design.POPUP_BACKGROUND_COLOR);
        mActionView.setBackground(scrollIndicatorBackground);

        ViewGroup.LayoutParams layoutParams = slideMarkView.getLayoutParams();
        layoutParams.height = Design.SLIDE_MARK_HEIGHT;

        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.mutate();
        gradientDrawable.setColor(Color.rgb(244, 244, 244));
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        slideMarkView.setBackground(gradientDrawable);

        float corner = ((float)Design.SLIDE_MARK_HEIGHT / 2) * Resources.getSystem().getDisplayMetrics().density;
        gradientDrawable.setCornerRadius(corner);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) slideMarkView.getLayoutParams();
        marginLayoutParams.topMargin = Design.SLIDE_MARK_TOP_MARGIN;

        layoutParams = mSubscriptionImageView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_IMAGE_WIDTH * Design.WIDTH_RATIO);
        layoutParams.height = (int) (DESIGN_IMAGE_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mSubscriptionImageView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_TITLE_MARGIN * Design.HEIGHT_RATIO);

        mNameView.setTypeface(Design.FONT_BOLD44.typeface);
        mNameView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_BOLD44.size);
        mNameView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mNameView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_TITLE_MARGIN * Design.HEIGHT_RATIO);

        mMessageView.setTypeface(Design.FONT_MEDIUM40.typeface);
        mMessageView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_MEDIUM40.size);
        mMessageView.setTextColor(Design.FONT_COLOR_GREY);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mMessageView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_MESSAGE_MARGIN * Design.HEIGHT_RATIO);

        mConfirmView.setOnClickListener(v -> onAccept());

        radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable saveViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        saveViewBackground.getPaint().setColor(Design.getMainStyle());
        mConfirmView.setBackground(saveViewBackground);

        layoutParams = mConfirmView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;

        mConfirmView.setMinimumHeight(Design.BUTTON_HEIGHT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mConfirmView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_CONFIRM_MARGIN * Design.HEIGHT_RATIO);

        confirmTextView.setTypeface(Design.FONT_BOLD36.typeface);
        confirmTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_BOLD36.size);
        confirmTextView.setTextColor(Color.WHITE);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) confirmTextView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_CONFIRM_VERTICAL_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_CONFIRM_VERTICAL_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.leftMargin = (int) (DESIGN_CONFIRM_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_CONFIRM_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);

        mCancelView.setOnClickListener(v -> onDeclineClick());

        layoutParams = mCancelView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_CANCEL_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mCancelView.getLayoutParams();
        marginLayoutParams.bottomMargin = (int) (DESIGN_CANCEL_MARGIN * Design.HEIGHT_RATIO);

        cancelTextView.setTypeface(Design.FONT_BOLD36.typeface);
        cancelTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_BOLD36.size);
        cancelTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mProgressBarView = findViewById(R.id.accept_invitation_subscription_activity_progress_bar);

        mUIInitialized = true;
    }

    private void updateViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateViews");
        }

        if (!mUIInitialized) {

            return;
        }

        if (mContactAvatar != null) {
            mSubscriptionImageView.setImageBitmap(mContactAvatar);
        }

        if (mContactName != null) {
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
            spannableStringBuilder.append(mContactName);
            spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_DEFAULT), 0, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (mContactDescription != null && !mContactDescription.isEmpty()) {
                spannableStringBuilder.append("\n");
                int startInfo = spannableStringBuilder.length();
                spannableStringBuilder.append(mContactDescription);
                spannableStringBuilder.setSpan(new RelativeSizeSpan(0.68f), startInfo, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_GREY), startInfo, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            mNameView.setText(spannableStringBuilder);

            mMessageView.setText(String.format(getString(R.string.in_app_subscription_activity_accept_invitation), mContactName));
        }

        if (mHasTwincode) {
            mCancelView.setVisibility(View.VISIBLE);
            mConfirmView.setVisibility(View.VISIBLE);
            mSubscriptionImageView.setVisibility(View.VISIBLE);

            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mMessageView.getLayoutParams();
            marginLayoutParams.bottomMargin = 0;

            mCancelView.setAlpha(1);
            mCancelView.setOnClickListener(new DeclineListener());
            mConfirmView.setAlpha(1);
            mConfirmView.setOnClickListener(new AcceptListener());

            if (mShowActionView) {
                mActionView.postDelayed(() -> {
                    mRootHeight = mOverlayView.getHeight();
                    mActionHeight = mActionView.getHeight();

                    mActionView.setY(mRootHeight - mActionHeight);
                    mActionView.invalidate();
                }, ANIMATION_DURATION);
            }
        } else {
            mCancelView.setVisibility(View.GONE);
            mConfirmView.setVisibility(View.GONE);
            mSubscriptionImageView.setVisibility(View.GONE);

            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mMessageView.getLayoutParams();
            marginLayoutParams.bottomMargin = (int) (DESIGN_CONFIRM_MARGIN * Design.HEIGHT_RATIO);

            String message = getString(R.string.accept_invitation_activity_being_transferred) + "\n" + getString(R.string.accept_invitation_activity_check_connection);
            mMessageView.setText(message);
        }

        mActionView.postDelayed(() -> {
            mRootHeight = mOverlayView.getHeight();
            mActionHeight = mActionView.getHeight();

            if (!mShowActionView) {
                showAcceptView();
            }
        }, ANIMATION_DURATION);
    }

    private void onAccept() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onAccept");
        }

        if (mInvitationSubscriptionService != null && mTwincodeURI != null && mTwincodeURI.twincodeId != null
                && mProfile.getTwincodeOutboundId() != null && mTwincodeURI.twincodeOptions != null) {
            mInvitationSubscriptionService.subscribeFeature(mTwincodeURI.twincodeId.toString(), mTwincodeURI.twincodeOptions,
                    mProfile.getTwincodeOutboundId().toString());
        }
    }

    private void onDeclineClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeclineClick");
        }

        animationCloseInvitationView();
    }

    @Override
    protected void onBackClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBackClick");
        }

        animationCloseInvitationView();
    }

    private void showAcceptView() {
        if (DEBUG) {
            Log.d(LOG_TAG, "showAcceptView");
        }

        mShowActionView = true;
        isOpenAnimationEnded = false;
        isCloseAnimationEnded = false;

        mActionView.setY(Design.DISPLAY_HEIGHT);
        mActionView.invalidate();
        animationOpenInvitationView();
    }

    private void saveTwincodeAttributes() {
        if (DEBUG) {
            Log.d(LOG_TAG, "saveTwincodeAttributes");
        }

        File filesDir = getTwinmeContext().getFilesDir();
        if (filesDir == null || mTwincodeURI == null || mTwincodeURI.twincodeId == null) {
            finish();
            return;
        }

        final String twincodeId = mTwincodeURI.twincodeId.toString();
        getTwinmeApplication().setInvitationSubscriptionTwincode(twincodeId);

        File directory = new File(getFilesDir(), "subscription");
        if (!directory.isDirectory()) {
            if (!directory.mkdirs() || !directory.isDirectory()) {
                return;
            }
        }

        File file = new File(directory, twincodeId + ".jpg");
        try {
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();
        } catch (IOException exception) {
            return;
        }

        try (FileOutputStream fileOutputStream = new FileOutputStream(file.getAbsolutePath())) {
            mContactAvatar.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            getTwinmeApplication().setInvitationSubscriptionImage(file.getAbsolutePath());

        } catch (Exception e) {
            if (Logger.ERROR) {
                Log.e(LOG_TAG, "Exception: ", e);
            }
        }
    }
}
