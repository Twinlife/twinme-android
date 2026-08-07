/*
 *  Copyright (c) 2014-2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.ColorUtils;

import com.google.android.material.imageview.ShapeableImageView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ErrorCode;
import org.twinlife.twinlife.ConversationService.DescriptorId;
import org.twinlife.twinlife.Notification;
import org.twinlife.twinlife.TrustMethod;
import org.twinlife.twinlife.TwincodeOutbound;
import org.twinlife.twinlife.TwincodeURI;
import org.twinlife.twinlife.util.Utils;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Profile;
import org.twinlife.twinme.models.Space;
import org.twinlife.twinme.services.AcceptInvitationService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.skin.DisplayMode;
import org.twinlife.twinme.ui.profiles.AddProfileActivity;
import org.twinlife.twinme.utils.AbstractBottomSheetView;
import org.twinlife.twinme.utils.DefaultConfirmView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AcceptInvitationActivity extends AbstractTwinmeActivity implements AcceptInvitationService.Observer {
    private static final String LOG_TAG = "AcceptInvitationActi...";
    private static final boolean DEBUG = false;

    private static final int BULLET_COLOR = Color.rgb(213, 213, 213);
    private static final int DESIGN_AVATAR_MARGIN = 60;
    private static final int DESIGN_AVATAR_SIZE = 148;
    private static final int DESIGN_ICON_VIEW_SIZE = 72;
    private static final int DESIGN_ICON_IMAGE_VIEW_SIZE = 42;
    private static final int DESIGN_BULLET_VIEW_MARGIN = 20;
    private static final int DESIGN_TITLE_MARGIN = 40;
    private static final int DESIGN_NAME_MARGIN = 6;
    private static final int DESIGN_MESSAGE_MARGIN = 50;
    private static final int DESIGN_CONFIRM_MARGIN = 60;
    private static final int DESIGN_CONFIRM_VERTICAL_MARGIN = 10;
    private static final int DESIGN_CONFIRM_HORIZONTAL_MARGIN = 20;
    private static final int DESIGN_CANCEL_HEIGHT = 140;
    private static final int DESIGN_CANCEL_MARGIN = 80;

    private static final int DESIGN_LINE_DASH_LONG_LENGTH = 8;
    private static final int DESIGN_LINE_DASH_SHORT_LENGTH = 4;
    private static final int DESIGN_LINE_DASH_SPACING = 6;
    private static final int DESIGN_LINE_DASH_WIDTH = 3;

    private View mOverlayView;
    private View mActionView;
    private TextView mLeftNameView;
    private TextView mRightNameView;
    private TextView mMessageView;
    private ShapeableImageView mLeftAvatarView;
    private ShapeableImageView mRightAvatarView;
    private View mIconView;
    private View mConfirmView;
    private TextView mConfirmTextView;
    private View mCancelView;
    private TextView mCancelTextView;

    private int mRootHeight = 0;
    private int mActionHeight = 0;

    private boolean mShowActionView = false;
    private boolean isOpenAnimationEnded = false;
    private boolean isCloseAnimationEnded = false;

    private static final long CONTACT_CHECK_DELAY = 60 * 1000L; // 60s

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

            onAccept();
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

    private boolean mUIInitialized = false;
    private boolean mHasTwincode = false;
    private boolean mHasExistingContact = false;
    private String mContactName;
    private Bitmap mContactAvatar;
    private Profile mProfile;
    private Contact mContact;
    private String mFromName;

    @Nullable
    private AcceptInvitationService mAcceptInvitationService;

    private DescriptorId mDescriptorId;
    private Notification mNotification;

    private Space mSpace;
    private Space mInitialSpace;
    @Nullable
    private TwincodeURI mTwincodeURI;

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
        TrustMethod trustMethod;
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            uri = intent.getData();
            trustMethod = (TrustMethod) intent.getSerializableExtra(Intents.INTENT_TRUST_METHOD);
        } else if (intent.hasExtra(Intents.INTENT_INVITATION_LINK)) {
            uri = Uri.parse(intent.getStringExtra(Intents.INTENT_INVITATION_LINK));
            trustMethod = TrustMethod.LINK;
        } else {
            mDescriptorId = DescriptorId.fromString(intent.getStringExtra(Intents.INTENT_DESCRIPTOR_ID));
            trustMethod = TrustMethod.PEER;
        }

        initViews();
        setFullscreen();

        UUID groupId = Utils.UUIDFromString(intent.getStringExtra(Intents.INTENT_GROUP_ID));
        UUID contactId = Utils.UUIDFromString(intent.getStringExtra(Intents.INTENT_CONTACT_ID));
        UUID notificationId = Utils.UUIDFromString(intent.getStringExtra(Intents.INTENT_NOTIFICATION_ID));
        if (intent.hasExtra(Intents.INTENT_CONTACT_NAME)) {
            mFromName = intent.getStringExtra(Intents.INTENT_CONTACT_NAME);
        }

        mAcceptInvitationService = new AcceptInvitationService(this, getTwinmeContext(), this, uri,
                mDescriptorId, groupId, contactId, notificationId, trustMethod);
    }

    //
    // Override Activity methods
    //

    @Override
    protected void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        if (mAcceptInvitationService != null) {
            mAcceptInvitationService.dispose();
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

    @Override
    public boolean canShowInfoFloatingView() {
        if (DEBUG) {
            Log.d(LOG_TAG, "canShowInfoFloatingView");
        }

        return false;
    }

    //
    // Implement AcceptInvitationService.Observer methods
    //

    @Override
    public void onGetSpace(@NonNull Space space, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetDefaultProfile: space=" + space);
        }

        mSpace = space;

        if (mInitialSpace == null) {
            mInitialSpace = space;
        }

        mProfile = space.getProfile();

        updateViews();
    }

    @Override
    public void onGetSpaceNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetSpaceNotFound");
        }

        mSpace = null;
        mProfile = null;
        updateViews();
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
        if (uri != null) {
            if (uri.kind != TwincodeURI.Kind.Invitation) {

                String message = getString(R.string.accept_invitation_view_incorrect_contact_information);
                if (uri.kind == TwincodeURI.Kind.Call) {
                    message = getString(R.string.add_contact_view_scan_message_call_link);
                } else if (uri.kind == TwincodeURI.Kind.AccountMigration) {
                    message = getString(R.string.add_contact_view_scan_message_migration_link);
                } else if (uri.kind == TwincodeURI.Kind.Transfer) {
                    message = getString(R.string.add_contact_view_scan_message_transfer_link);
                }

                error(message, this::finish);
                return;
            }
        }

        if (errorCode != ErrorCode.SUCCESS) {
            String message = getString(R.string.accept_invitation_view_incorrect_contact_information);
            if (errorCode == ErrorCode.BAD_REQUEST) {
                message = getString(R.string.add_contact_view_scan_error_incorrect_link);
            } else if (errorCode == ErrorCode.FEATURE_NOT_IMPLEMENTED) {
                message = getString(R.string.add_contact_view_scan_error_not_managed_link);
            } else if (errorCode == ErrorCode.ITEM_NOT_FOUND) {
                message = getString(R.string.add_contact_view_scan_error_corrupt_link);
            } else if (errorCode == ErrorCode.EXPIRED) {
                message = getString(R.string.add_contact_view_scan_error_expired_link);
            }

            error(message, this::finish);
            return;
        }
        mTwincodeURI = uri;
        updateViews();

    }

    @Override
    public void onGetTwincode(@NonNull TwincodeOutbound twincodeOutbound, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetTwincode: twincodeOutbound=" + twincodeOutbound + " avatar=" + avatar);
        }

        mHasTwincode = true;
        mContactName = twincodeOutbound.getName();
        mContactAvatar = avatar != null ? avatar : getDefaultAvatar();

        updateViews();
    }

    @Override
    public void onGetTwincodeNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetTwincodeNotFound");
        }

        error(getString(R.string.add_contact_view_scan_error_revoked_link), this::finish);
    }

    @Override
    public void onGetTwincodeExpired() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetTwincodeExpired");
        }

        error(getString(R.string.add_contact_view_scan_error_revoked_link), this::finish);
    }

    @Override
    public void onLocalTwincode() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onLocalTwincode");
        }

        error(getString(R.string.accept_invitation_view_local_twincode), this::finish);
    }

    @Override
    public void onExistingContacts(@NonNull List<Contact> list) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onExistingContacts");
        }

        final long now = System.currentTimeMillis();
        for (Contact existingContact : list) {
            if (existingContact.getCreationDate() + CONTACT_CHECK_DELAY > now) {
                onCreateContact(existingContact);
                return;
            }
        }

        if (!list.isEmpty()) {
            mHasExistingContact = true;
        }

        updateViews();
    }

    @Override
    public void onCreateContact(@NonNull Contact contact) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateContact: contact=" + contact);
        }

        mContact = contact;
        if (mDescriptorId != null) {
            mAcceptInvitationService.deleteDescriptor(mDescriptorId);
        }
        if (mNotification != null) {
            mAcceptInvitationService.deleteNotification(mNotification);
        } else {
            showContactActivity(contact);
            animationCloseInvitationView();
        }
    }

    @Override
    public void onGetNotification(@NonNull Notification notification) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetNotification: notification=" + notification);
        }

        mNotification = notification;
    }

    @Override
    public void onDeleteDescriptor(@NonNull DescriptorId descriptorId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteDescriptor: descriptorId=" + descriptorId);
        }

        if (mNotification != null) {
            mAcceptInvitationService.deleteNotification(mNotification);
        } else {
            if (mContact != null) {
                showContactActivity(mContact);
            }
            animationCloseInvitationView();
        }
    }

    @Override
    public void onDeleteNotification(@NonNull UUID notificationId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteNotification: notificationId=" + notificationId);
        }

        mNotification = null;
        if (mContact != null) {
            showContactActivity(mContact);
        }
        animationCloseInvitationView();
    }

    @Override
    public void onSetCurrentSpace(@NonNull Space space) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSetCurrentSpace: space=" + space);
        }

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
        setContentView(R.layout.accept_invitation_activity);
        setBackgroundColor(Color.TRANSPARENT);

        mOverlayView = findViewById(R.id.accept_invitation_activity_overlay_view);
        mActionView = findViewById(R.id.accept_invitation_activity_action_view);
        View slideMarkView = findViewById(R.id.accept_invitation_activity_slide_mark_view);

        View identityView = findViewById(R.id.accept_invitation_activity_identity_view);
        View leftIdentityView = findViewById(R.id.accept_invitation_activity_identity_left_view);
        View rightIdentityView = findViewById(R.id.accept_invitation_activity_identity_right_view);
        View middleIdentityView = findViewById(R.id.accept_invitation_activity_identity_middle_view);
        mLeftAvatarView = findViewById(R.id.accept_invitation_activity_left_avatar_view);

        View leftLineContainerView = findViewById(R.id.accept_invitation_activity_left_line_container_view);
        View leftLineView = findViewById(R.id.accept_invitation_activity_left_line_view);
        mLeftNameView = findViewById(R.id.accept_invitation_activity_left_name_view);

        View rightLineContainerView = findViewById(R.id.accept_invitation_activity_right_line_container_view);
        View rightLineView = findViewById(R.id.accept_invitation_activity_right_line_view);
        mRightAvatarView = findViewById(R.id.accept_invitation_activity_right_avatar_view);
        mRightNameView = findViewById(R.id.accept_invitation_activity_right_name_view);

        mIconView = findViewById(R.id.accept_invitation_activity_icon_view);
        ImageView iconImageView = findViewById(R.id.accept_invitation_activity_icon_image_view);
        mMessageView = findViewById(R.id.accept_invitation_activity_message_view);
        mConfirmView = findViewById(R.id.accept_invitation_activity_confirm_view);
        mConfirmTextView = findViewById(R.id.accept_invitation_activity_confirm_text_view);
        mCancelView = findViewById(R.id.accept_invitation_activity_cancel_view);
        mCancelTextView = findViewById(R.id.accept_invitation_activity_cancel_text_view);

        mOverlayView.setBackgroundColor(Design.OVERLAY_VIEW_COLOR);
        mOverlayView.setAlpha(0);
        mOverlayView.setOnClickListener(v -> onOverlayClick());

        mActionView.setY(Design.DISPLAY_HEIGHT);

        float radius = Design.ACTION_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, 0, 0, 0, 0};

        ShapeDrawable scrollIndicatorBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        scrollIndicatorBackground.getPaint().setColor(Design.POPUP_BACKGROUND_COLOR);
        mActionView.setBackground(scrollIndicatorBackground);

        ViewGroup.LayoutParams layoutParams = slideMarkView.getLayoutParams();
        layoutParams.width = Design.SLIDE_MARK_WIDTH;
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

        marginLayoutParams = (ViewGroup.MarginLayoutParams) identityView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_AVATAR_MARGIN * Design.HEIGHT_RATIO);

        layoutParams = leftIdentityView.getLayoutParams();
        layoutParams.width = (int) (Design.DISPLAY_WIDTH * 0.5f);

        layoutParams = middleIdentityView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_AVATAR_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_AVATAR_SIZE * Design.HEIGHT_RATIO);

        layoutParams = leftLineContainerView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_AVATAR_SIZE * Design.HEIGHT_RATIO);

        layoutParams = rightLineContainerView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_AVATAR_SIZE * Design.HEIGHT_RATIO);

        layoutParams = mLeftAvatarView.getLayoutParams();
        mLeftAvatarView.setBackgroundColor(Design.WHITE_COLOR);
        layoutParams.width = (int) (DESIGN_AVATAR_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_AVATAR_SIZE * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) leftIdentityView.getLayoutParams();
        marginLayoutParams.leftMargin = - (int) (DESIGN_ICON_VIEW_SIZE * Design.HEIGHT_RATIO * 0.5);

        Design.updateTextFont(mLeftNameView, Design.FONT_BOLD36);
        mLeftNameView.setTextColor(Design.FONT_COLOR_DEFAULT);

        int nameMargin = (int) (DESIGN_NAME_MARGIN * Design.WIDTH_RATIO) + (int) (DESIGN_ICON_VIEW_SIZE * Design.HEIGHT_RATIO * 0.5);
        marginLayoutParams = (ViewGroup.MarginLayoutParams) mLeftNameView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_TITLE_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.leftMargin = nameMargin;
        marginLayoutParams.rightMargin = nameMargin;

        float dp = Resources.getSystem().getDisplayMetrics().density;

        Paint dashPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dashPaint.setColor(BULLET_COLOR);
        dashPaint.setStyle(Paint.Style.STROKE);
        dashPaint.setStrokeWidth(dp * DESIGN_LINE_DASH_WIDTH);
        dashPaint.setStrokeCap(Paint.Cap.ROUND);
        dashPaint.setPathEffect(new DashPathEffect(new float[]{dp * DESIGN_LINE_DASH_LONG_LENGTH, dp * DESIGN_LINE_DASH_SPACING, dp * DESIGN_LINE_DASH_SHORT_LENGTH, dp * DESIGN_LINE_DASH_SPACING}, 0));
        ShapeDrawable lineLeftDrawable = new ShapeDrawable() {
            @Override
            public void draw(Canvas canvas) {
                float startY = getBounds().height() * 0.5f;
                canvas.drawLine(0, startY, getBounds().width(), startY, dashPaint);
            }
        };
        leftLineView.setBackground(lineLeftDrawable);

        ShapeDrawable lineRightDrawable = new ShapeDrawable() {
            @Override
            public void draw(Canvas canvas) {
                float startY = getBounds().height() * 0.5f;
                canvas.drawLine(getBounds().width(), startY, 0, startY, dashPaint);
            }
        };
        rightLineView.setBackground(lineRightDrawable);

        layoutParams = rightIdentityView.getLayoutParams();
        layoutParams.width = (int) (Design.DISPLAY_WIDTH * 0.5f);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) rightIdentityView.getLayoutParams();
        marginLayoutParams.rightMargin = - (int) (DESIGN_ICON_VIEW_SIZE * Design.HEIGHT_RATIO * 0.5);

        layoutParams = mRightAvatarView.getLayoutParams();
        mRightAvatarView.setBackgroundColor(Design.WHITE_COLOR);
        layoutParams.width = (int) (DESIGN_AVATAR_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_AVATAR_SIZE * Design.HEIGHT_RATIO);

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

        iconImageView.setColorFilter(Color.WHITE);

        layoutParams = iconImageView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_ICON_IMAGE_VIEW_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_ICON_IMAGE_VIEW_SIZE * Design.HEIGHT_RATIO);

        layoutParams = mRightNameView.getLayoutParams();
        layoutParams.width = (int) (Design.DISPLAY_WIDTH * 0.5f);

        Design.updateTextFont(mRightNameView, Design.FONT_BOLD36);
        mRightNameView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mRightNameView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_TITLE_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.leftMargin = nameMargin;
        marginLayoutParams.rightMargin = nameMargin;

        Design.updateTextFont(mMessageView, Design.FONT_MEDIUM40);
        mMessageView.setTextColor(Design.FONT_COLOR_GREY);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mMessageView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_MESSAGE_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.leftMargin = Design.TEXT_MARGIN;
        marginLayoutParams.rightMargin = Design.TEXT_MARGIN;

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

        Design.updateTextFont(mCancelTextView, Design.FONT_BOLD36);
        mCancelTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mProgressBarView = findViewById(R.id.accept_invitation_activity_progress_bar);

        mUIInitialized = true;
    }

    private void onAccept() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onAccept");
        }

        if (mProfile == null) {

            ViewGroup viewGroup = findViewById(R.id.accept_invitation_activity_layout);

            DefaultConfirmView defaultConfirmView = new DefaultConfirmView(this, null);
            defaultConfirmView.setTitle(getString(R.string.profile_view_add_profile));
            defaultConfirmView.setMessage(getString(R.string.application_add_contact_no_profile));

            boolean darkMode = false;
            int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            int displayMode = Settings.displayMode.getInt();
            if ((currentNightMode == Configuration.UI_MODE_NIGHT_YES && displayMode == DisplayMode.SYSTEM.ordinal())  || displayMode == DisplayMode.DARK.ordinal()) {
                darkMode = true;
            }

            defaultConfirmView.setImage(ResourcesCompat.getDrawable(getResources(), darkMode ? R.drawable.onboarding_add_profile_dark : R.drawable.onboarding_add_profile, null));
            defaultConfirmView.setConfirmTitle(getString(R.string.profile_view_create_profile));

            AbstractBottomSheetView.Observer observer = new AbstractBottomSheetView.Observer() {
                @Override
                public void onConfirmClick() {
                    defaultConfirmView.animationCloseConfirmView();
                }

                @Override
                public void onCancelClick() {
                    defaultConfirmView.animationCloseConfirmView();
                }

                @Override
                public void onDismissClick() {
                    defaultConfirmView.animationCloseConfirmView();
                }

                @Override
                public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                    viewGroup.removeView(defaultConfirmView);
                    setFullscreen();

                    if (fromConfirmAction) {
                        Intent intent = new Intent(getApplicationContext(), AddProfileActivity.class);
                        intent.putExtra(Intents.INTENT_FIRST_PROFILE, true);
                        if (mTwincodeURI != null) {
                            intent.putExtra(Intents.INTENT_INVITATION_LINK, mTwincodeURI.uri);
                        }
                        startActivity(intent);
                        finish();
                    } else {
                        onDeclineClick();
                    }
                }
            };
            defaultConfirmView.setObserver(observer);
            viewGroup.addView(defaultConfirmView);
            defaultConfirmView.show();

            Window window = getWindow();
            window.setNavigationBarColor(Design.POPUP_BACKGROUND_COLOR);
        } else {
            mAcceptInvitationService.createContact(mProfile, mSpace);
        }
    }

    private void onDeclineClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeclineClick");
        }

        if (mDescriptorId != null && mAcceptInvitationService != null) {
            mAcceptInvitationService.deleteDescriptor(mDescriptorId);
        } else {
            animationCloseInvitationView();
        }
    }

    private void onOverlayClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onOverlayClick");
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

    private void updateViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateViews");
        }

        if (!mUIInitialized) {

            return;
        }

        if (mProfile != null && mAcceptInvitationService != null) {
            mLeftNameView.setText(mProfile.getName());

            mAcceptInvitationService.getImage(mProfile.getAvatarId(), (Bitmap avatar) -> {
                mLeftAvatarView.setImageBitmap(avatar);
            });
        }

        if (mContactAvatar != null) {
            mRightAvatarView.setImageBitmap(mContactAvatar);
        }

        if (mContactName != null) {
            mRightNameView.setText(mContactName);

            if (mHasExistingContact) {
                mMessageView.setText(getString(R.string.accept_invitation_view_existing_contact_message));
            } else {
                if (mFromName != null) {
                    String message = String.format(getString(R.string.share_contact_accept_view_message), mFromName, mContactName);
                    message += "\n";
                    message += String.format(getString(R.string.share_contact_accept_view_invite_message), mContactName);
                    mMessageView.setText(message);
                } else {
                    mMessageView.setText(String.format(getString(R.string.accept_invitation_view_message), mContactName));
                }
            }
        }

        if (mHasTwincode) {
            mCancelView.setVisibility(View.VISIBLE);
            mConfirmView.setVisibility(View.VISIBLE);
            mLeftAvatarView.setVisibility(View.VISIBLE);
            mRightAvatarView.setVisibility(View.VISIBLE);
            mIconView.setVisibility(View.VISIBLE);

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
                }, Design.ANIMATION_VIEW_DURATION);
            }
        } else {
            mCancelView.setVisibility(View.GONE);
            mConfirmView.setVisibility(View.GONE);
            mLeftAvatarView.setVisibility(View.GONE);
            mRightAvatarView.setVisibility(View.GONE);
            mIconView.setVisibility(View.GONE);

            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mMessageView.getLayoutParams();
            marginLayoutParams.bottomMargin = (int) (DESIGN_CONFIRM_MARGIN * Design.HEIGHT_RATIO);

            String message = getString(R.string.accept_invitation_view_being_transferred) + "\n" + getString(R.string.accept_invitation_view_check_connection);
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

    @Override
    public void updateFont() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateFont");
        }

        Design.updateTextFont(mLeftNameView, Design.FONT_BOLD36);
        Design.updateTextFont(mRightNameView, Design.FONT_BOLD36);
        Design.updateTextFont(mMessageView, Design.FONT_MEDIUM40);
        Design.updateTextFont(mConfirmTextView, Design.FONT_BOLD36);
        Design.updateTextFont(mCancelTextView, Design.FONT_BOLD36);
    }

    @Override
    public void updateColor() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateViews");
        }

        super.updateColor();

        mLeftNameView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mRightNameView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mMessageView.setTextColor(Design.FONT_COLOR_GREY);
        mCancelTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        float radius = Design.ACTION_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, 0, 0, 0, 0};

        ShapeDrawable scrollIndicatorBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        scrollIndicatorBackground.getPaint().setColor(Design.POPUP_BACKGROUND_COLOR);
        mActionView.setBackground(scrollIndicatorBackground);
    }
}
