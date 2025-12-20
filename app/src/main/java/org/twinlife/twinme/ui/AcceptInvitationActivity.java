/*
 *  Copyright (c) 2014-2025 twinlife SA.
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
import org.twinlife.twinlife.BaseService.ErrorCode;
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
import org.twinlife.twinme.ui.inAppSubscriptionActivity.AcceptInvitationSubscriptionActivity;
import org.twinlife.twinme.ui.profiles.AddProfileActivity;
import org.twinlife.twinme.ui.spaces.SpacesActivity;
import org.twinlife.twinme.utils.AbstractBottomSheetView;
import org.twinlife.twinme.utils.CommonUtils;
import org.twinlife.twinme.utils.DefaultConfirmView;
import org.twinlife.twinme.utils.RoundedImageView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class AcceptInvitationActivity extends AbstractTwinmeActivity implements AcceptInvitationService.Observer {
    private static final String LOG_TAG = "AcceptInvitationActi...";
    private static final boolean DEBUG = false;

    private static final float DESIGN_SPACE_ROUND_CORNER_RADIUS_DP = 14f;
    private static final int BULLET_COLOR = Color.rgb(213, 213, 213);

    private static final int DESIGN_AVATAR_MARGIN = 60;
    private static final int DESIGN_AVATAR_HEIGHT = 148;
    protected static final int DESIGN_ICON_VIEW_SIZE = 72;
    private static final int DESIGN_ICON_IMAGE_VIEW_HEIGHT = 42;
    protected static final int DESIGN_BULLET_VIEW_SIZE = 26;
    private static final int DESIGN_BULLET_VIEW_MARGIN = 20;
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
    private ShapeableImageView mAvatarView;
    private View mIconView;
    private View mBulletView;
    private View mConfirmView;
    private TextView mConfirmTextView;
    private View mCancelView;
    private TextView mCancelTextView;

    private TextView mSpaceNameView;
    private TextView mProfileView;
    private TextView mSpaceTitleView;
    private View mSpaceView;
    private View mNoSpaceAvatarView;
    private TextView mNoSpaceAvatarTextView;
    private GradientDrawable mNoSpaceAvatarGradientDrawable;
    private RoundedImageView mSpaceAvatarView;

    private int mRootHeight = 0;
    private int mActionHeight = 0;

    private boolean mShowActionView = false;
    private boolean isOpenAnimationEnded = false;
    private boolean isCloseAnimationEnded = false;

    private static final int MOVE_TO_SPACE = 1;
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

    private volatile boolean mCreateProfileOnResume = false;

    private boolean mUIInitialized = false;
    private boolean mHasTwincode = false;
    private boolean mHasExistingContact = false;
    private String mContactName;
    private String mContactDescription;
    private Bitmap mContactAvatar;
    private Profile mProfile;
    private Contact mContact;

    @Nullable
    private AcceptInvitationService mAcceptInvitationService;

    private DescriptorId mDescriptorId;
    private Notification mNotification;

    private Space mSpace;
    private Space mInitialSpace;
    @Nullable
    private TwincodeURI mTwincodeURI;
    @Nullable
    private TrustMethod mTrustMethod = null;

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
            mTrustMethod = (TrustMethod) intent.getSerializableExtra(Intents.INTENT_TRUST_METHOD);
        } else if (intent.hasExtra(Intents.INTENT_INVITATION_LINK)) {
            uri = Uri.parse(intent.getStringExtra(Intents.INTENT_INVITATION_LINK));
            mTrustMethod = TrustMethod.LINK;
        } else {
            mDescriptorId = DescriptorId.fromString(intent.getStringExtra(Intents.INTENT_DESCRIPTOR_ID));
            mTrustMethod = TrustMethod.PEER;
        }

        initViews();
        setFullscreen();

        UUID groupId = Utils.UUIDFromString(intent.getStringExtra(Intents.INTENT_GROUP_ID));
        UUID contactId = Utils.UUIDFromString(intent.getStringExtra(Intents.INTENT_CONTACT_ID));
        UUID notificationId = Utils.UUIDFromString(intent.getStringExtra(Intents.INTENT_NOTIFICATION_ID));

        mAcceptInvitationService = new AcceptInvitationService(this, getTwinmeContext(), this, uri,
                mDescriptorId, groupId, contactId, notificationId, mTrustMethod);
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

        if (mCreateProfileOnResume) {
            mCreateProfileOnResume = false;

            Intent intent = new Intent();
            intent.setClass(AcceptInvitationActivity.this, AddProfileActivity.class);
            startActivity(intent);
        }
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onActivityResult requestCode=" + requestCode + " resultCode=" + resultCode + " data=" + data);
        }

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MOVE_TO_SPACE) {
            String value = data != null ? data.getStringExtra(Intents.INTENT_SPACE_SELECTION) : null;
            if (value != null && mAcceptInvitationService != null) {
                mAcceptInvitationService.getSpace(UUID.fromString(value));
            }
        }
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
    public void onParseTwincodeURI(@NonNull ErrorCode errorCode, @Nullable TwincodeURI twincodeURI) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onParseTwincodeURI errorCode=" + errorCode + " twincodeURI=" + twincodeURI);
        }

        // @todo Handle errors and report an accurate message:
        // ErrorCode.BAD_REQUEST: link is not well formed or not one of our link
        // ErrorCode.FEATURE_NOT_IMPLEMENTED: link does not target our application or domain.
        // ErrorCode.ITEM_NOT_FOUND: link targets the application but it is not compatible with the version.
        // TwincodeURI.Kind == Kind.AccountMigration => redirect to account migration
        // TwincodeURI.Kind == Kind.Call|Kind.Transfer => forbidden
        if (twincodeURI != null) {
            if (twincodeURI.kind != TwincodeURI.Kind.Invitation) {

                String message = getString(R.string.accept_invitation_activity_incorrect_contact_information);
                if (twincodeURI.kind == TwincodeURI.Kind.Call) {
                    message = getString(R.string.add_contact_activity_scan_message_call_link);
                } else if (twincodeURI.kind == TwincodeURI.Kind.AccountMigration) {
                    message = getString(R.string.add_contact_activity_scan_message_migration_link);
                } else if (twincodeURI.kind == TwincodeURI.Kind.Transfer) {
                    message = getString(R.string.add_contact_activity_scan_message_transfer_link);
                }

                error(message, this::finish);
                return;
            }
            if (twincodeURI.twincodeOptions != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(twincodeURI.uri));
                intent.putExtra(Intents.INTENT_TRUST_METHOD, mTrustMethod != null ? mTrustMethod : TrustMethod.QR_CODE);

                intent.setClass(this, AcceptInvitationSubscriptionActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                return;
            }
        }

        if (errorCode != ErrorCode.SUCCESS) {
            String message = getString(R.string.accept_invitation_activity_incorrect_contact_information);
            if (errorCode == ErrorCode.BAD_REQUEST) {
                message = getString(R.string.add_contact_activity_scan_error_incorect_link);
            } else if (errorCode == ErrorCode.FEATURE_NOT_IMPLEMENTED) {
                message = getString(R.string.add_contact_activity_scan_error_not_managed_link);
            } else if (errorCode == ErrorCode.ITEM_NOT_FOUND) {
                message = getString(R.string.add_contact_activity_scan_error_corrupt_link);
            }

            error(message, this::finish);
            return;
        }
        mTwincodeURI = twincodeURI;
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

        error(getString(R.string.add_contact_activity_scan_error_revoked_link), this::finish);
    }

    @Override
    public void onLocalTwincode() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onLocalTwincode");
        }

        error(getString(R.string.accept_invitation_activity_local_twincode), this::finish);
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
        if (mDescriptorId != null && mAcceptInvitationService != null) {
            mAcceptInvitationService.deleteDescriptor(mDescriptorId);
        }
        if (mNotification != null && mAcceptInvitationService != null) {
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

        if (mNotification != null && mAcceptInvitationService != null) {
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
        mAvatarView = findViewById(R.id.accept_invitation_activity_avatar_view);
        mIconView = findViewById(R.id.accept_invitation_activity_icon_view);
        ImageView iconImageView = findViewById(R.id.accept_invitation_activity_icon_image_view);
        mBulletView = findViewById(R.id.accept_invitation_activity_bullet_view);
        mNameView = findViewById(R.id.accept_invitation_activity_title_view);
        mMessageView = findViewById(R.id.accept_invitation_activity_message_view);
        mConfirmView = findViewById(R.id.accept_invitation_activity_confirm_view);
        mConfirmTextView = findViewById(R.id.accept_invitation_activity_confirm_text_view);
        mCancelView = findViewById(R.id.accept_invitation_activity_cancel_view);
        mCancelTextView = findViewById(R.id.accept_invitation_activity_cancel_text_view);

        mOverlayView.setBackgroundColor(Design.OVERLAY_VIEW_COLOR);
        mOverlayView.setAlpha(0);
        mOverlayView.setOnClickListener(v -> onDeclineClick());

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

        mSpaceTitleView = findViewById(R.id.accept_invitation_activity_space_title_view);
        mSpaceTitleView.setTypeface(Design.FONT_BOLD26.typeface);
        mSpaceTitleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_BOLD26.size);
        mSpaceTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mSpaceTitleView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_CONFIRM_MARGIN * Design.HEIGHT_RATIO);

        mSpaceView = findViewById(R.id.accept_invitation_activity_space_view);
        mSpaceView.setOnClickListener(view -> onSpaceClick());

        layoutParams = mSpaceView.getLayoutParams();
        layoutParams.height = Design.SECTION_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mSpaceView.getLayoutParams();
        marginLayoutParams.topMargin = Design.IDENTITY_VIEW_TOP_MARGIN;

        mSpaceAvatarView = findViewById(R.id.accept_invitation_activity_space_avatar_view);

        mNoSpaceAvatarView = findViewById(R.id.accept_invitation_activity_no_space_avatar_view);

        mNoSpaceAvatarGradientDrawable = new GradientDrawable();
        mNoSpaceAvatarGradientDrawable.mutate();
        mNoSpaceAvatarGradientDrawable.setColor(Design.BACKGROUND_COLOR_GREY);
        mNoSpaceAvatarGradientDrawable.setShape(GradientDrawable.RECTANGLE);
        mNoSpaceAvatarView.setBackground(mNoSpaceAvatarGradientDrawable);

        mNoSpaceAvatarTextView = findViewById(R.id.accept_invitation_activity_no_space_avatar_text_view);
        mNoSpaceAvatarTextView.setTypeface(Design.FONT_BOLD44.typeface);
        mNoSpaceAvatarTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_BOLD44.size);
        mNoSpaceAvatarTextView.setTextColor(Color.WHITE);

        mSpaceNameView = findViewById(R.id.accept_invitation_activity_space_name_view);
        mSpaceNameView.setTypeface(Design.FONT_MEDIUM34.typeface);
        mSpaceNameView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_MEDIUM34.size);
        mSpaceNameView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mProfileView = findViewById(R.id.accept_invitation_activity_profile_name_view);
        mProfileView.setTypeface(Design.FONT_MEDIUM32.typeface);
        mProfileView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_MEDIUM32.size);
        mProfileView.setTextColor(Design.FONT_COLOR_GREY);

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
            defaultConfirmView.setTitle(getString(R.string.profile_fragment_add_profile));
            defaultConfirmView.setMessage(getString(R.string.application_add_contact_no_profile));

            boolean darkMode = false;
            int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            int displayMode = Settings.displayMode.getInt();
            if ((currentNightMode == Configuration.UI_MODE_NIGHT_YES && displayMode == DisplayMode.SYSTEM.ordinal())  || displayMode == DisplayMode.DARK.ordinal()) {
                darkMode = true;
            }

            defaultConfirmView.setImage(ResourcesCompat.getDrawable(getResources(), darkMode ? R.drawable.onboarding_add_profile_dark : R.drawable.onboarding_add_profile, null));
            defaultConfirmView.setConfirmTitle(getString(R.string.profile_fragment_create_profile));

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
        } else if (mAcceptInvitationService != null) {
            mAcceptInvitationService.createContact(mProfile, mSpace);
        }
    }

    private void onDeclineClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeclineClick");
        }

        if (mInitialSpace != null && mSpace != null && !mInitialSpace.getId().equals(mSpace.getId()) && mAcceptInvitationService != null) {
            mAcceptInvitationService.setCurrentSpace(mInitialSpace);
        }

        if (mDescriptorId != null && mAcceptInvitationService != null) {
            mAcceptInvitationService.deleteDescriptor(mDescriptorId);
        } else {
            animationCloseInvitationView();
        }
    }

    private void onSpaceClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSwitchSpaceClick");
        }

        Intent intent = new Intent();
        intent.putExtra(Intents.INTENT_PICKER_MODE, true);
        intent.setClass(this, SpacesActivity.class);

        startActivityForResult(intent, MOVE_TO_SPACE);
    }

    @Override
    protected void onBackClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBackClick");
        }

        if (mInitialSpace != null && mSpace != null && !mInitialSpace.getId().equals(mSpace.getId()) && mAcceptInvitationService != null) {
            mAcceptInvitationService.setCurrentSpace(mInitialSpace);
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

        if (mContactAvatar != null) {
            mAvatarView.setImageBitmap(mContactAvatar);
        }

        if (mContactName != null) {
            mNameView.setText(mContactName);

            if (mHasExistingContact) {
                mMessageView.setText(getString(R.string.accept_invitation_activity_existing_contact_message));
            } else {
                mMessageView.setText(String.format(getString(R.string.accept_invitation_activity_message), mContactName));
            }

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
        }

        if (mHasTwincode) {
            mCancelView.setVisibility(View.VISIBLE);
            mConfirmView.setVisibility(View.VISIBLE);
            mAvatarView.setVisibility(View.VISIBLE);
            mBulletView.setVisibility(View.VISIBLE);
            mIconView.setVisibility(View.VISIBLE);
            mSpaceTitleView.setVisibility(View.VISIBLE);
            mSpaceView.setVisibility(View.VISIBLE);

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
            mAvatarView.setVisibility(View.GONE);
            mBulletView.setVisibility(View.GONE);
            mIconView.setVisibility(View.GONE);
            mSpaceTitleView.setVisibility(View.GONE);
            mSpaceView.setVisibility(View.GONE);

            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mMessageView.getLayoutParams();
            marginLayoutParams.bottomMargin = (int) (DESIGN_CONFIRM_MARGIN * Design.HEIGHT_RATIO);

            String message = getString(R.string.accept_invitation_activity_being_transferred) + "\n" + getString(R.string.accept_invitation_activity_check_connection);
            mMessageView.setText(message);
        }

        if (mSpace != null && mAcceptInvitationService != null) {
            mSpaceTitleView.setVisibility(View.VISIBLE);
            mSpaceView.setVisibility(View.VISIBLE);
            mSpaceNameView.setText(mSpace.getName());
            if (mProfile != null) {
                mProfileView.setText(mProfile.getName());
            }

            float corner = DESIGN_SPACE_ROUND_CORNER_RADIUS_DP * Resources.getSystem().getDisplayMetrics().density;
            float[] radii = new float[8];
            Arrays.fill(radii, corner);

            if (mSpace.hasSpaceAvatar()) {
                mSpaceAvatarView.setVisibility(View.VISIBLE);
                mNoSpaceAvatarView.setVisibility(View.GONE);
                mNoSpaceAvatarTextView.setVisibility(View.GONE);
                mAcceptInvitationService.getSpaceImage(mSpace, (Bitmap avatar) -> mSpaceAvatarView.setImageBitmap(avatar, radii));
            } else {
                mNoSpaceAvatarGradientDrawable.setCornerRadii(radii);
                mSpaceAvatarView.setVisibility(View.GONE);
                mNoSpaceAvatarView.setVisibility(View.VISIBLE);
                mNoSpaceAvatarTextView.setVisibility(View.VISIBLE);

                String name = mSpace.getName();
                if (!name.isEmpty()) {
                    mNoSpaceAvatarTextView.setText(name.substring(0, 1).toUpperCase());
                }
            }
            mNoSpaceAvatarGradientDrawable.setColor(CommonUtils.parseColor(mSpace.getStyle(), Design.BACKGROUND_COLOR_DEFAULT));
        } else {
            mSpaceTitleView.setVisibility(View.GONE);
            mSpaceView.setVisibility(View.GONE);
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

        Design.updateTextFont(mNameView, Design.FONT_BOLD44);

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

        mNameView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mMessageView.setTextColor(Design.FONT_COLOR_GREY);
        mCancelTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        float radius = Design.ACTION_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, 0, 0, 0, 0};

        ShapeDrawable scrollIndicatorBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        scrollIndicatorBackground.getPaint().setColor(Design.POPUP_BACKGROUND_COLOR);
        mActionView.setBackground(scrollIndicatorBackground);
    }
}
