/*
 *  Copyright (c) 2022-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.models.CallReceiver;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Group;
import org.twinlife.twinme.models.Profile;
import org.twinlife.twinme.models.Space;
import org.twinlife.twinme.services.EditIdentityService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.contacts.MenuAddContactView;
import org.twinlife.twinme.utils.RoundedView;
import org.twinlife.twinme.utils.UIMenuSelectAction;
import org.twinlife.twinme.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ShowProfileActivity extends AbstractTwinmeActivity implements EditIdentityService.Observer {
    private static final String LOG_TAG = "ShowProfileActivity";
    private static final boolean DEBUG = false;

    private static final String SHOW_ONBOARDING = "showOnboarding";

    private static final int DESIGN_ROUNDED_VIEW_TOP_MARGIN = 85;
    private static final int DESIGN_MESSAGE_VIEW_TOP_MARGIN = 60;
    private static final float DESIGN_ACTION_VIEW_MIN_MARGIN = 90f;
    private static int AVATAR_OVER_SIZE;
    private static int AVATAR_MAX_SIZE;
    private static int ACTION_VIEW_MIN_MARGIN;

    private View mContentView;
    private View mTwincodeView;
    private View mAddContactView;
    private TextView mMessageView;
    private View mEditView;
    private TextView mDescriptionTextView;
    private ImageView mAvatarView;
    private TextView mTitleView;

    private ScrollView mScrollView;

    private boolean mUIInitialized = false;
    private boolean mShowOnboarding = false;

    private boolean mInitScrollView = false;

    @Nullable
    private Profile mProfile;

    private Bitmap mAvatar;
    private ProgressBar mProgressBarView;

    private EditIdentityService mEditIdentityService;

    private float mAvatarLastSize = -1;
    private float mScrollPosition = -1;

    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        mEditIdentityService = new EditIdentityService(this, getTwinmeContext(), this);

        initViews();

        if (savedInstanceState != null) {
            mShowOnboarding = savedInstanceState.getBoolean(SHOW_ONBOARDING);
        }

        updateIdentity();

        Intent intent = getIntent();
        UUID profileId = Utils.UUIDFromString(intent.getStringExtra(Intents.INTENT_PROFILE_ID));
        UUID spaceId = Utils.UUIDFromString(intent.getStringExtra(Intents.INTENT_SPACE_ID));

        if (profileId != null) {
            mEditIdentityService.getProfile(profileId);
        } else if (spaceId != null) {
            mEditIdentityService.getSpace(spaceId);
        }

        showProgressIndicator();
    }

    //
    // Override Fragment methods
    //

    @Override
    public void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        mEditIdentityService.dispose();

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        if (mScrollView != null && !mInitScrollView) {
            mInitScrollView = true;
            Rect rectangle = new Rect();
            getWindow().getDecorView().getWindowVisibleDisplayFrame(rectangle);
            int contentHeight = mContentView.getHeight();
            if (contentHeight < rectangle.height()) {
                contentHeight = rectangle.height();
            }

            ViewGroup.LayoutParams layoutParams = mContentView.getLayoutParams();
            layoutParams.height = contentHeight + AVATAR_MAX_SIZE;

            mScrollView.post(() -> mScrollView.scrollBy(0, AVATAR_OVER_SIZE));
        }

        super.onResume();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSaveInstanceState: outState=" + outState);
        }

        super.onSaveInstanceState(outState);
        outState.putBoolean(SHOW_ONBOARDING, mShowOnboarding);
    }

    //
    // Implement EditIdentityService.Observer methods
    //

    @Override
    public void showProgressIndicator() {
        if (DEBUG) {
            Log.d(LOG_TAG, "showProgressIndicator");
        }

        if (mProgressBarView != null && mProgressBarView.getVisibility() != View.VISIBLE) {
            mProgressBarView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void hideProgressIndicator() {
        if (DEBUG) {
            Log.d(LOG_TAG, "hideProgressIndicator");
        }

        if (mProgressBarView != null && mProgressBarView.getVisibility() == View.VISIBLE) {
            mProgressBarView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onGetSpace(@NonNull Space space, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetSpace: space=" + space);
        }

        setFullscreen();
        // updateIdentity();
    }

    @Override
    public void onUpdateSpace(@NonNull Space space) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateSpace: space=" + space);
        }

    }

    @Override
    public void onCreateProfile(@NonNull Profile profile) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateProfile: profile=" + profile);
        }
    }

    @Override
    public void onGetProfile(@NonNull Profile profile, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetProfile: profile=" + profile);
        }

        mProfile = profile;
        mAvatar = avatar;
        setFullscreen();
        updateIdentity();
    }

    @Override
    public void onGetProfileNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetProfileNotFound");
        }

    }

    @Override
    public void onGetContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContact: contact=" + contact);
        }
    }

    @Override
    public void onGetContactNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContactNotFound");
        }
    }

    @Override
    public void onGetGroup(@NonNull Group group) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetGroup: group=" + group);
        }
    }

    @Override
    public void onGetGroupNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetGroupNotFound");
        }
    }

    @Override
    public void onUpdateProfile(@NonNull Profile profile) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateProfile: profile=" + profile);
        }

        if (mProfile != null && profile.getId().equals(mProfile.getId())) {
            mProfile = profile;
            updateIdentity();
        }
    }

    @Override
    public void onUpdateIdentityAvatar(@NonNull Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateIdentityAvatar: avatar=" + avatar);
        }

        mAvatar = avatar;

        updateIdentity();
    }

    @Override
    public void onUpdateContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateContact: contact=" + contact);
        }
    }

    @Override
    public void onUpdateGroup(@NonNull Group group) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateGroup: group=" + group);
        }

    }

    @Override
    public void onUpdateCallReceiver(@NonNull CallReceiver callReceiver) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateCallReceiver: callReceiver=" + callReceiver);
        }
    }

    @Override
    public void onGetCallReceiver(@NonNull CallReceiver callReceiver) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetCallReceiver: callReceiver=" + callReceiver);
        }
    }

    @Override
    public void onGetCallReceiverNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetCallReceiverNotFound");
        }
    }

    //
    // Private methods
    //

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.show_profile_activity);

        setTitle(getString(R.string.application_name));
        showToolBar(false);
        showBackButton(true);
        setBackgroundColor(Design.WHITE_COLOR);

        applyInsets(R.id.show_profile_activity_layout, -1, -1, Design.WHITE_COLOR, true);

        mAvatarView = findViewById(R.id.show_profile_activity_avatar_view);
        mAvatarView.setBackgroundColor(Design.AVATAR_PLACEHOLDER_COLOR);

        ViewGroup.LayoutParams layoutParams = mAvatarView.getLayoutParams();
        layoutParams.width = AVATAR_MAX_SIZE - AVATAR_OVER_SIZE;
        layoutParams.height = AVATAR_MAX_SIZE - AVATAR_OVER_SIZE;

        mAddContactView = findViewById(R.id.show_profile_activity_add_contact_view);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mAddContactView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_ROUNDED_VIEW_TOP_MARGIN * Design.HEIGHT_RATIO);

        RoundedView addContactRoundedView = findViewById(R.id.show_profile_activity_add_contact_rounded_view);
        addContactRoundedView.setColor(Color.argb(76, 0, 0, 0));

        mContentView = findViewById(R.id.show_profile_activity_content_view);
        mContentView.setY(AVATAR_MAX_SIZE - ACTION_VIEW_MIN_MARGIN);

        setBackground(mContentView);

        mScrollView = findViewById(R.id.show_profile_activity_content_scroll_view);
        ViewTreeObserver viewTreeObserver = mScrollView.getViewTreeObserver();
        viewTreeObserver.addOnScrollChangedListener(() -> {
            if (mScrollPosition == -1) {
                mScrollPosition = AVATAR_OVER_SIZE;
            }

            float delta = mScrollPosition - mScrollView.getScrollY();
            updateAvatarSize(delta);
            mScrollPosition = mScrollView.getScrollY();
        });

        mScrollView.setOnTouchListener((v, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                int contentY = (int) (mContentView.getY() - mScrollView.getScrollY());
                Rect contentRect = new Rect(0, contentY, Design.DISPLAY_WIDTH, Design.DISPLAY_HEIGHT);
                Rect inviteRect = new Rect(mAddContactView.getLeft(), mAddContactView.getTop(), mAddContactView.getRight(), mAddContactView.getBottom());
                boolean isInContentRect = contentRect.contains((int) motionEvent.getX(), (int) motionEvent.getY());
                boolean isInInviteRect = inviteRect.contains((int) motionEvent.getX(), (int) motionEvent.getY());
                if (isInInviteRect && !isInContentRect) {
                    onAddContactClick();
                }
            }
            return false;
        });

        View slideMarkView = findViewById(R.id.show_profile_activity_slide_mark_view);
        layoutParams = slideMarkView.getLayoutParams();
        layoutParams.height = Design.SLIDE_MARK_HEIGHT;

        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.mutate();
        gradientDrawable.setColor(Color.rgb(244, 244, 244));
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        slideMarkView.setBackground(gradientDrawable);

        float corner = ((float)Design.SLIDE_MARK_HEIGHT / 2) * Resources.getSystem().getDisplayMetrics().density;
        gradientDrawable.setCornerRadius(corner);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) slideMarkView.getLayoutParams();
        marginLayoutParams.topMargin = Design.SLIDE_MARK_TOP_MARGIN;

        View backClickableView = findViewById(R.id.show_profile_activity_back_clickable_view);
        backClickableView.setOnClickListener(view -> onBackClick());

        layoutParams = backClickableView.getLayoutParams();
        layoutParams.height = Design.BACK_CLICKABLE_VIEW_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) backClickableView.getLayoutParams();
        marginLayoutParams.leftMargin = Design.BACK_CLICKABLE_VIEW_LEFT_MARGIN;
        marginLayoutParams.topMargin = Design.BACK_CLICKABLE_VIEW_TOP_MARGIN;

        RoundedView backRoundedView = findViewById(R.id.show_profile_activity_back_rounded_view);
        backRoundedView.setColor(Design.BACK_VIEW_COLOR);

        mTitleView = findViewById(R.id.show_profile_activity_title_view);
        Design.updateTextFont(mTitleView, Design.FONT_BOLD44);
        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        View headerView = findViewById(R.id.show_profile_activity_content_header_view);
        marginLayoutParams = (ViewGroup.MarginLayoutParams) headerView.getLayoutParams();
        marginLayoutParams.topMargin = Design.HEADER_VIEW_TOP_MARGIN;

        mEditView = findViewById(R.id.show_profile_activity_edit_clickable_view);
        mEditView.setOnClickListener(view -> onEditProfileClick());

        layoutParams = mEditView.getLayoutParams();
        layoutParams.height = Design.EDIT_CLICKABLE_VIEW_HEIGHT;

        ImageView editImageView = findViewById(R.id.show_profile_activity_edit_image_view);
        editImageView.setColorFilter(Design.getMainStyle());

        mTwincodeView = findViewById(R.id.show_profile_activity_twincode_view);
        mTwincodeView.setOnClickListener(view -> onTwincodeClick());

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mTwincodeView.getLayoutParams();
        marginLayoutParams.topMargin = Design.TWINCODE_VIEW_TOP_MARGIN;

        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable twincodeViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        twincodeViewBackground.getPaint().setColor(Design.getMainStyle());
        mTwincodeView.setBackground(twincodeViewBackground);

        ImageView twincodeIconView = findViewById(R.id.show_profile_activity_twincode_icon_view);
        marginLayoutParams = (ViewGroup.MarginLayoutParams) twincodeIconView.getLayoutParams();
        marginLayoutParams.leftMargin = Design.TWINCODE_PADDING;
        marginLayoutParams.topMargin = Design.TWINCODE_ICON_PADDING;
        marginLayoutParams.bottomMargin = Design.TWINCODE_ICON_PADDING;
        marginLayoutParams.setMarginStart(Design.TWINCODE_PADDING);

        layoutParams = twincodeIconView.getLayoutParams();
        layoutParams.height = Design.TWINCODE_ICON_SIZE;
        layoutParams.width = Design.TWINCODE_ICON_SIZE;

        TextView twincodeTextView = findViewById(R.id.show_profile_activity_twincode_title_view);
        Design.updateTextFont(twincodeTextView, Design.FONT_REGULAR28);
        twincodeTextView.setTextColor(Color.WHITE);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) twincodeTextView.getLayoutParams();
        marginLayoutParams.leftMargin = Design.TWINCODE_PADDING;
        marginLayoutParams.rightMargin = Design.TWINCODE_PADDING;

        marginLayoutParams.setMarginStart(Design.TWINCODE_PADDING);
        marginLayoutParams.setMarginEnd(Design.TWINCODE_PADDING);

        mDescriptionTextView = findViewById(R.id.show_profile_activity_description_text_view);
        Design.updateTextFont(mDescriptionTextView, Design.FONT_MEDIUM34);
        mDescriptionTextView.setTextColor(Design.FONT_COLOR_DESCRIPTION);

        mMessageView = findViewById(R.id.show_profile_activity_activity_message_view);
        Design.updateTextFont(mMessageView, Design.FONT_REGULAR26);
        mMessageView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mMessageView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_MESSAGE_VIEW_TOP_MARGIN * Design.HEIGHT_RATIO);

        mProgressBarView = findViewById(R.id.show_profile_activity_progress_bar);

        mUIInitialized = true;
    }

    private void updateIdentity() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateIdentity");
        }

        if (!mUIInitialized) {

            return;
        }

        if (mProfile != null) {
            mTitleView.setText(mProfile.getName());
            mDescriptionTextView.setText(mProfile.getDescription());
            mAddContactView.setVisibility(View.VISIBLE);
            mDescriptionTextView.setVisibility(View.VISIBLE);
            mMessageView.setVisibility(View.VISIBLE);
            mTwincodeView.setVisibility(View.VISIBLE);
            mEditView.setVisibility(View.VISIBLE);
        } else {
            mAddContactView.setVisibility(View.GONE);
            mDescriptionTextView.setVisibility(View.GONE);
            mMessageView.setVisibility(View.GONE);
            mTwincodeView.setVisibility(View.GONE);
            mEditView.setVisibility(View.GONE);
        }

        if (mAvatar != null) {
            mAvatarView.setImageBitmap(mAvatar);
            mAvatarView.setBackgroundColor(Color.TRANSPARENT);
        } else {
            mEditIdentityService.getProfileImage(mProfile, (Bitmap avatar) -> {
                mAvatar = avatar;
                if (mAvatar != null) {
                    mAvatarView.setImageBitmap(mAvatar);
                    mAvatarView.setBackgroundColor(Color.TRANSPARENT);
                } else {
                    mAvatarView.setBackgroundColor(Design.AVATAR_PLACEHOLDER_COLOR);
                }
            });
        }
    }

    private void onAddContactClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onAddContactClick");
        }

        if (mProfile != null) {

            ViewGroup viewGroup = findViewById(R.id.show_profile_activity_layout);

            MenuAddContactView menuAddContactView = new MenuAddContactView(this, null);
            MenuAddContactView.Observer observer = new MenuAddContactView.Observer() {
                @Override
                public void onStartAddContactByScan() {

                    menuAddContactView.animationCloseMenu();

                    Intent intent = new Intent();
                    intent.putExtra(Intents.INTENT_PROFILE_ID, mProfile.getId());
                    intent.putExtra(Intents.INTENT_INVITATION_MODE, AddContactActivity.InvitationMode.SCAN);
                    intent.setClass(getBaseContext(), AddContactActivity.class);
                    startActivity(intent);
                }

                @Override
                public void onStartAddContactByInvite() {

                    menuAddContactView.animationCloseMenu();

                    Intent intent = new Intent();
                    intent.putExtra(Intents.INTENT_PROFILE_ID, mProfile.getId());
                    intent.putExtra(Intents.INTENT_INVITATION_MODE, AddContactActivity.InvitationMode.INVITE);
                    intent.setClass(getBaseContext(), AddContactActivity.class);
                    startActivity(intent);
                }

                @Override
                public void onCloseMenuSelectActionAnimationEnd() {

                    viewGroup.removeView(menuAddContactView);
                    setFullscreen();
                }
            };

            menuAddContactView.setObserver(observer);

            viewGroup.addView(menuAddContactView);

            List<UIMenuSelectAction> actions = new ArrayList<>();
            actions.add(new UIMenuSelectAction(getString(R.string.contacts_fragment_scan_contact_title), R.drawable.scan_code));
            actions.add(new UIMenuSelectAction(getString(R.string.contacts_fragment_invite_contact_title), R.drawable.qrcode));
            menuAddContactView.setActions(actions, this);
            menuAddContactView.openMenu(false);

            Window window = getWindow();
            window.setNavigationBarColor(Design.POPUP_BACKGROUND_COLOR);
        }
    }

    protected void onEditProfileClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onEditProfileClick");
        }

        if (mProfile != null) {
            startActivity(EditProfileActivity.class, Intents.INTENT_PROFILE_ID, mProfile.getId());
        }
    }

    protected void onTwincodeClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onTwincodeClick");
        }

        if (mProfile != null) {
            Intent intent = new Intent(this, AddContactActivity.class);
            intent.putExtra(Intents.INTENT_PROFILE_ID, mProfile.getId().toString());
            intent.putExtra(Intents.INTENT_INVITATION_MODE, AddContactActivity.InvitationMode.INVITE_ONLY);
            startActivity(intent);
        }
    }

    private void updateAvatarSize(float deltaY) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateAvatarSize: " + deltaY);
        }

        if (mAvatarLastSize == -1) {
            mAvatarLastSize = AVATAR_MAX_SIZE - AVATAR_OVER_SIZE;
        }

        float avatarViewSize = mAvatarLastSize + deltaY;
        if (avatarViewSize < Design.DISPLAY_WIDTH) {
            avatarViewSize = Design.DISPLAY_WIDTH;
        } else if (avatarViewSize > AVATAR_MAX_SIZE) {
            avatarViewSize = AVATAR_MAX_SIZE;
        }

        if (avatarViewSize != mAvatarLastSize) {
            ViewGroup.LayoutParams avatarLayoutParams = mAvatarView.getLayoutParams();
            avatarLayoutParams.width = (int) avatarViewSize;
            avatarLayoutParams.height = (int) avatarViewSize;
            mAvatarView.requestLayout();

            mAvatarLastSize = avatarViewSize;
        }
    }

    @Override
    public void setupDesign() {
        if (DEBUG) {
            Log.d(LOG_TAG, "setupDesign");
        }

        AVATAR_OVER_SIZE = Design.AVATAR_OVER_WIDTH;
        AVATAR_MAX_SIZE = Design.DISPLAY_WIDTH + (AVATAR_OVER_SIZE * 2);
        ACTION_VIEW_MIN_MARGIN = (int) (Design.HEIGHT_RATIO * DESIGN_ACTION_VIEW_MIN_MARGIN);
    }
}
