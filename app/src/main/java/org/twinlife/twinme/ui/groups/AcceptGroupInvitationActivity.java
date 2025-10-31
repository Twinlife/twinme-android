/*
 *  Copyright (c) 2018-2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.groups;

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
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

import com.google.android.material.imageview.ShapeableImageView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ConversationService.DescriptorId;
import org.twinlife.twinlife.ConversationService.GroupConversation;
import org.twinlife.twinlife.ConversationService.InvitationDescriptor;
import org.twinlife.twinlife.util.Utils;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Group;
import org.twinlife.twinme.models.Originator;
import org.twinlife.twinme.services.GroupInvitationService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.Intents;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Activity controller to accept or decline a group invitation.
 */

public class AcceptGroupInvitationActivity extends AbstractGroupActivity implements GroupInvitationService.Observer {
    private static final String LOG_TAG = "AcceptGroupInvitation";
    private static final boolean DEBUG = false;

    private static final int BULLET_COLOR = Color.rgb(213, 213, 213);
    private static final int DESIGN_AVATAR_MARGIN = 60;
    private static final int DESIGN_AVATAR_HEIGHT = 148;
    protected static final int DESIGN_ICON_VIEW_SIZE = 72;
    private static final int DESIGN_ICON_IMAGE_VIEW_HEIGHT = 36;
    protected static final int DESIGN_BULLET_VIEW_SIZE = 26;
    private static final int DESIGN_BULLET_VIEW_MARGIN = 20;
    protected static final int DESIGN_TITLE_MARGIN = 40;
    private static final int DESIGN_MESSAGE_MARGIN = 30;
    private static final int DESIGN_CONFIRM_MARGIN = 80;
    private static final int DESIGN_CONFIRM_VERTICAL_MARGIN = 10;
    private static final int DESIGN_CONFIRM_HORIZONTAL_MARGIN = 20;
    private static final int DESIGN_CANCEL_HEIGHT = 140;
    private static final int DESIGN_CANCEL_MARGIN = 80;

    private View mOverlayView;
    private View mActionView;
    private TextView mNameView;
    private TextView mMessageView;
    private ShapeableImageView mAvatarView;
    private View mIconView;
    private View mBulletView;
    private ImageView mInvitationStatusImageView;
    private ShapeableImageView mContactImageView;
    private View mConfirmView;
    private TextView mConfirmTextView;
    private View mCancelView;

    private int mRootHeight = 0;
    private int mActionHeight = 0;

    private boolean mShowActionView = false;
    private boolean isOpenAnimationEnded = false;
    private boolean isCloseAnimationEnded = false;

    private class AcceptListener implements OnClickListener {

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

            onAcceptClick();
        }
    }

    private class DeclineListener implements OnClickListener {

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

    private InvitationDescriptor mInvitation;
    private boolean mUIInitialized = false;
    private Contact mContact;
    private Originator mGroup;
    private Bitmap mContactAvatar;
    private Bitmap mGroupAvatar;
    private boolean mAccepting = false;
    @Nullable
    private GroupInvitationService mGroupInvitationService;

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

        // From the invitation id, get the invitation and the conversation that received it.
        final Intent intent = getIntent();
        final UUID contactId = Utils.UUIDFromString(intent.getStringExtra(Intents.INTENT_CONTACT_ID));
        final DescriptorId invitationId = DescriptorId.fromString(intent.getStringExtra(Intents.INTENT_INVITATION_ID));
        if (contactId == null || invitationId == null) {
            finish();
            return;
        }

        initViews();
        setFullscreen();

        mGroupInvitationService = new GroupInvitationService(this, getTwinmeContext(), this, invitationId, contactId);
    }

    //
    // Override Activity methods
    //

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        if (mGroupInvitationService != null) {
            mGroupInvitationService.dispose();
        }

        super.onDestroy();
    }

    @Override
    public void finish() {
        if (DEBUG) {
            Log.d(LOG_TAG, "finish");
        }

        super.finish();

        if (mResumed && mGroup != null) {
            showContactActivity(mGroup);
        }

        overridePendingTransition(0, 0);
    }

    @Override
    public boolean canShowInfoFloatingView() {
        if (DEBUG) {
            Log.d(LOG_TAG, "canShowInfoFloatingView");
        }

        return false;
    }

    @Override
    public void onGetContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContact: contact=" + contact);
        }

        mContact = contact;
        mContactAvatar = avatar;
        updateViews();
    }

    @Override
    public void onGetInvitation(@NonNull InvitationDescriptor invitation, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetInvitation");
        }

        mInvitation = invitation;
        mGroupAvatar = avatar;
        updateViews();
    }

    @Override
    public void onAcceptedInvitation(@NonNull GroupConversation conversation, @NonNull InvitationDescriptor invitation) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onAcceptedInvitation");
        }

        mInvitation = invitation;
        mGroup = (Originator) conversation.getSubject();
        updateViews();
    }

    @Override
    public void onDeclinedInvitation(InvitationDescriptor invitation) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeclinedInvitation");
        }

        mInvitation = invitation;
        updateViews();
    }

    @Override
    public void onMoveGroup(@NonNull Group group) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onMoveGroup group=" + group);
        }

    }

    @Override
    public void onGroupJoined(@NonNull Group group, InvitationDescriptor invitation) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGroupJoined");
        }

        mInvitation = invitation;
        updateViews();
    }

    @Override
    public void onDeletedInvitation() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeletedInvitation");
        }

        mInvitation = null;
        updateViews();
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
        setContentView(R.layout.accept_group_invitation_activity);
        setBackgroundColor(Color.TRANSPARENT);

        mOverlayView = findViewById(R.id.accept_group_invitation_activity_overlay_view);
        mActionView = findViewById(R.id.accept_group_invitation_activity_action_view);
        View slideMarkView = findViewById(R.id.accept_group_invitation_activity_slide_mark_view);
        mAvatarView = findViewById(R.id.accept_group_invitation_activity_avatar_view);
        mContactImageView = findViewById(R.id.accept_group_invitation_activity_contact_avatar_view);
        mIconView = findViewById(R.id.accept_group_invitation_activity_icon_view);
        mInvitationStatusImageView = findViewById(R.id.accept_group_invitation_activity_icon_image_view);
        mBulletView = findViewById(R.id.accept_group_invitation_activity_bullet_view);
        mNameView = findViewById(R.id.accept_group_invitation_activity_title_view);
        mMessageView = findViewById(R.id.accept_group_invitation_activity_message_view);
        mConfirmView = findViewById(R.id.accept_group_invitation_activity_confirm_view);
        mConfirmTextView = findViewById(R.id.accept_group_invitation_activity_confirm_text_view);
        mCancelView = findViewById(R.id.accept_group_invitation_activity_cancel_view);
        TextView cancelTextView = findViewById(R.id.accept_group_invitation_activity_cancel_text_view);

        mOverlayView.setBackgroundColor(Design.OVERLAY_VIEW_COLOR);
        mOverlayView.setAlpha(0);
        mOverlayView.setOnClickListener(v -> onDismissClick());

        mActionView.setY(Design.DISPLAY_HEIGHT);

        float radius = Design.ACTION_RADIUS * Resources.getSystem().getDisplayMetrics().density;
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

        layoutParams = mAvatarView.getLayoutParams();
        mAvatarView.setBackgroundColor(Design.WHITE_COLOR);
        layoutParams.height = (int) (DESIGN_AVATAR_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mAvatarView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_AVATAR_MARGIN * Design.HEIGHT_RATIO);

        layoutParams = mContactImageView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_ICON_VIEW_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_ICON_VIEW_SIZE * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mContactImageView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (-DESIGN_BULLET_VIEW_MARGIN * Design.WIDTH_RATIO);

        layoutParams = mIconView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_ICON_VIEW_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_ICON_VIEW_SIZE * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mIconView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (-DESIGN_BULLET_VIEW_MARGIN * Design.WIDTH_RATIO);

        GradientDrawable iconBackgroundDrawable = new GradientDrawable();
        iconBackgroundDrawable.setColor(BULLET_COLOR);
        iconBackgroundDrawable.setCornerRadius((int) ((DESIGN_ICON_VIEW_SIZE * Design.HEIGHT_RATIO) * 0.5));
        iconBackgroundDrawable.setStroke(8, Color.WHITE);
        mIconView.setBackground(iconBackgroundDrawable);

        layoutParams = mInvitationStatusImageView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_ICON_IMAGE_VIEW_HEIGHT * Design.HEIGHT_RATIO);

        layoutParams = mBulletView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_BULLET_VIEW_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_BULLET_VIEW_SIZE * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mBulletView.getLayoutParams();
        marginLayoutParams.rightMargin = (int) (DESIGN_BULLET_VIEW_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.topMargin = (int) ((DESIGN_ICON_VIEW_SIZE - DESIGN_BULLET_VIEW_SIZE) * Design.HEIGHT_RATIO);

        GradientDrawable bulletBackgroundDrawable = new GradientDrawable();
        bulletBackgroundDrawable.setColor(BULLET_COLOR);
        bulletBackgroundDrawable.setCornerRadius((int) ((DESIGN_BULLET_VIEW_SIZE * Design.HEIGHT_RATIO) * 0.5));
        bulletBackgroundDrawable.setStroke(8, Color.WHITE);
        mBulletView.setBackground(bulletBackgroundDrawable);

        Design.updateTextFont(mNameView, Design.FONT_BOLD44);
        mNameView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mNameView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_TITLE_MARGIN * Design.HEIGHT_RATIO);

        Design.updateTextFont(mMessageView, Design.FONT_MEDIUM40);
        mMessageView.setTextColor(Design.FONT_COLOR_GREY);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mMessageView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_MESSAGE_MARGIN * Design.HEIGHT_RATIO);

        mConfirmView.setOnClickListener(v -> onAcceptClick());

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

        Design.updateTextFont(mConfirmTextView, Design.FONT_BOLD36);
        mConfirmTextView.setTextColor(Color.WHITE);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mConfirmTextView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_CONFIRM_VERTICAL_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_CONFIRM_VERTICAL_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.leftMargin = (int) (DESIGN_CONFIRM_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_CONFIRM_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);

        mCancelView.setOnClickListener(v -> onDeclineClick());

        layoutParams = mCancelView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_CANCEL_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mCancelView.getLayoutParams();
        marginLayoutParams.bottomMargin = (int) (DESIGN_CANCEL_MARGIN * Design.HEIGHT_RATIO);

        Design.updateTextFont(cancelTextView, Design.FONT_BOLD36);
        cancelTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mProgressBarView = findViewById(R.id.accept_group_invitation_activity_progress_bar);

        mUIInitialized = true;
    }

    private void onAcceptClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onAcceptClick");
        }

        if (mAccepting || mGroupInvitationService == null) {
            return;
        }

        if (mInvitation != null && mInvitation.getStatus().equals(InvitationDescriptor.Status.PENDING)) {
            mAccepting = true;

            // Disable accept/decline actions as a feedback of the action.
            mCancelView.setAlpha(0.5f);
            mConfirmView.setAlpha(0.5f);

            mGroupInvitationService.acceptInvitation();
        }
    }

    private void onDeclineClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeclineClick");
        }

        if (mAccepting || mGroupInvitationService == null) {

            return;
        }
        mAccepting = true;

        // Disable accept/decline actions as a feedback of the action.
        mCancelView.setAlpha(0.5f);
        mConfirmView.setAlpha(0.5f);

        mGroupInvitationService.declineInvitation();
    }

    private void onDismissClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCancelClick");
        }

        animationCloseInvitationView();
    }

    @Override
    protected void onBackClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBackClick");
        }

        finish();
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

    private void updateViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateViews");
        }

        if (!mUIInitialized) {

            return;
        }

        if (mInvitation != null) {
            mNameView.setText(mInvitation.getName());
        }

        if (mContact != null) {
            mMessageView.setText(String.format(getString(R.string.accept_group_invitation_activity_message), mContact.getName()));
        }

        if (mContactAvatar != null) {
            mContactImageView.setImageBitmap(mContactAvatar);
        }

        if (mGroupAvatar == null || mGroupAvatar == getTwinmeApplication().getDefaultGroupAvatar()) {
            mAvatarView.setImageBitmap(getTwinmeApplication().getDefaultGroupAvatar());
            mAvatarView.setBackgroundColor(Design.GREY_ITEM_COLOR);
        } else {
            mAvatarView.setImageBitmap(mGroupAvatar);
            mAvatarView.setBackgroundColor(Color.TRANSPARENT);
        }

        if (mInvitation != null && mContact != null) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mMessageView.getLayoutParams();
            marginLayoutParams.bottomMargin = 0;

            mConfirmView.setVisibility(View.VISIBLE);
            mCancelView.setVisibility(View.VISIBLE);
            mAvatarView.setVisibility(View.VISIBLE);
            mContactImageView.setVisibility(View.VISIBLE);
            mBulletView.setVisibility(View.VISIBLE);
            mIconView.setVisibility(View.VISIBLE);

            marginLayoutParams = (ViewGroup.MarginLayoutParams) mConfirmView.getLayoutParams();
            switch (mInvitation.getStatus()) {
                case PENDING:
                    mIconView.setVisibility(View.GONE);
                    mContactImageView.setVisibility(View.VISIBLE);
                    mCancelView.setVisibility(View.VISIBLE);
                    mCancelView.setAlpha(1);
                    mCancelView.setOnClickListener(new DeclineListener());
                    mConfirmView.setVisibility(View.VISIBLE);
                    mConfirmView.setAlpha(1);
                    mConfirmView.setOnClickListener(new AcceptListener());
                    marginLayoutParams.bottomMargin = 0;
                    break;

                case ACCEPTED:
                    mCancelView.setVisibility(View.GONE);
                    mConfirmView.setVisibility(View.VISIBLE);
                    mConfirmView.setAlpha(1);
                    mConfirmView.setOnClickListener(v -> onDismissClick());
                    marginLayoutParams.bottomMargin = (int) (DESIGN_CANCEL_MARGIN * Design.HEIGHT_RATIO);
                    mConfirmTextView.setText(getString(R.string.application_ok));
                    mContactImageView.setVisibility(View.GONE);
                    mIconView.setVisibility(View.VISIBLE);
                    mInvitationStatusImageView.setVisibility(View.VISIBLE);
                    mMessageView.setText(getString(R.string.conversation_activity_invitation_accepted));
                    mInvitationStatusImageView.setImageResource(R.drawable.invitation_state_accepted);
                    break;

                case JOINED:
                    mCancelView.setVisibility(View.GONE);
                    mConfirmView.setVisibility(View.VISIBLE);
                    mConfirmView.setAlpha(1);
                    mConfirmView.setOnClickListener(v -> onDismissClick());
                    marginLayoutParams.bottomMargin = (int) (DESIGN_CANCEL_MARGIN * Design.HEIGHT_RATIO);
                    mConfirmTextView.setText(getString(R.string.application_ok));
                    mContactImageView.setVisibility(View.GONE);
                    mIconView.setVisibility(View.VISIBLE);
                    mMessageView.setText(getString(R.string.conversation_activity_invitation_joined));
                    mInvitationStatusImageView.setImageResource(R.drawable.invitation_state_joined);
                    break;

                case WITHDRAWN:
                    mCancelView.setVisibility(View.GONE);
                    mConfirmView.setVisibility(View.VISIBLE);
                    mConfirmView.setAlpha(1);
                    mConfirmView.setOnClickListener(v -> onDismissClick());
                    marginLayoutParams.bottomMargin = (int) (DESIGN_CANCEL_MARGIN * Design.HEIGHT_RATIO);
                    mConfirmTextView.setText(getString(R.string.application_ok));
                    mContactImageView.setVisibility(View.GONE);
                    mIconView.setVisibility(View.VISIBLE);
                    mMessageView.setText(getString(R.string.accept_group_invitation_activity_deleted));
                    mInvitationStatusImageView.setImageResource(R.drawable.action_delete);
                    break;

                default:
                    mCancelView.setVisibility(View.GONE);
                    mConfirmView.setVisibility(View.VISIBLE);
                    mConfirmView.setAlpha(1);
                    mConfirmView.setOnClickListener(v -> onDismissClick());
                    marginLayoutParams.bottomMargin = (int) (DESIGN_CANCEL_MARGIN * Design.HEIGHT_RATIO);
                    mConfirmTextView.setText(getString(R.string.application_ok));
                    mContactImageView.setVisibility(View.GONE);
                    mIconView.setVisibility(View.VISIBLE);
                    mMessageView.setText(getString(R.string.conversation_activity_invitation_refused));
                    mInvitationStatusImageView.setImageResource(R.drawable.invitation_state_refused);
                    break;

            }
        } else {
            mConfirmView.setVisibility(View.GONE);
            mCancelView.setVisibility(View.GONE);
            mAvatarView.setVisibility(View.GONE);
            mContactImageView.setVisibility(View.GONE);
            mBulletView.setVisibility(View.GONE);
            mIconView.setVisibility(View.GONE);

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
        }, Design.ANIMATION_VIEW_DURATION);
    }
}
