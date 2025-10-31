/*
 *  Copyright (c) 2020-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Auguste Hatton (Auguste.Hatton@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui.mainActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.models.Profile;
import org.twinlife.twinme.models.Space;
import org.twinlife.twinme.services.ProfileService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.skin.DisplayMode;
import org.twinlife.twinme.ui.AddContactActivity;
import org.twinlife.twinme.ui.EditProfileActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.Settings;
import org.twinlife.twinme.ui.accountMigrationActivity.AccountMigrationScannerActivity;
import org.twinlife.twinme.ui.contacts.MenuAddContactView;
import org.twinlife.twinme.ui.profiles.AddProfileActivity;
import org.twinlife.twinme.utils.RoundedView;
import org.twinlife.twinme.utils.UIMenuSelectAction;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends TabbarFragment implements ProfileService.Observer {
    private static final String LOG_TAG = "ProfileFragment";
    private static final boolean DEBUG = false;

    private static final float DESIGN_NO_PROFILE_IMAGE_VIEW_HEIGHT = 480f;
    private static final int DESIGN_ROUNDED_VIEW_TOP_MARGIN = 85;
    private static final int DESIGN_TWINCODE_VIEW_TOP_MARGIN = 66;
    private static final int DESIGN_MESSAGE_VIEW_TOP_MARGIN = 60;
    private static final int DESIGN_AVATAR_OVER_SIZE = 120;
    private static final int AVATAR_OVER_SIZE = (int) (DESIGN_AVATAR_OVER_SIZE * Design.WIDTH_RATIO);
    private static final int AVATAR_MAX_SIZE = Design.DISPLAY_WIDTH + (AVATAR_OVER_SIZE * 2);

    private View mContentView;
    private View mTwincodeView;
    private View mAddContactView;
    private View mSideMenuView;
    private TextView mMessageView;
    private View mEditView;
    private TextView mDescriptionTextView;
    private ImageView mAvatarView;
    private TextView mTitleView;

    private ImageView mNoProfileImageView;
    private TextView mNoProfileTextView;
    private View mCreateProfileView;
    private View mTransferView;
    private NestedScrollView mScrollView;
    private ProgressBar mProgressBarView;

    private boolean mUIInitialized = false;

    @Nullable
    private Profile mProfile;
    @Nullable
    private Bitmap mAvatar;
    private ProfileService mProfileService;

    private float mAvatarLastSize = -1;
    private float mScrollPosition = -1;

    private boolean mInitScrollView = false;

    public ProfileFragment() {
        if (DEBUG) {
            Log.d(LOG_TAG, "ProfileFragment");
        }

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateView: inflater=" + inflater + " container=" + container + " savedInstanceState=" + savedInstanceState);
        }

        View view = inflater.inflate(R.layout.profile_fragment, container, false);
        initViews(view);

        mProfileService = new ProfileService(mTwinmeActivity, mTwinmeActivity.getTwinmeContext(), this);

        // Try to get the profile from the main activity, if it is available this avoids a screen
        // flickering because we display the default profile fragment in edit mode and switch to
        // normal mode as soon as the ProfileService has called the onGetSpace() observer.
        // The flickering can still occur if the main activity does not yet have the current profile.
        mProfile = mTwinmeActivity.getProfile();

        updateIdentity();

        showProgressIndicator();

        return view;
    }

    //
    // Override Fragment methods
    //

    @Override
    public void onPause() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPause");
        }

        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        mProfileService.dispose();

        super.onDestroy();
    }

    @Override
    public void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        super.onResume();

        updateColor();

        if (mScrollView != null && !mInitScrollView && mTwinmeActivity != null) {
            mInitScrollView = true;

            Rect rectangle = new Rect();
            mTwinmeActivity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rectangle);
            int contentHeight = mContentView.getHeight();
            if (contentHeight < rectangle.height()) {
                contentHeight = rectangle.height();
            }

            ViewGroup.LayoutParams layoutParams = mContentView.getLayoutParams();
            layoutParams.height = contentHeight + AVATAR_MAX_SIZE;

            mScrollView.post(() -> mScrollView.scrollBy(0, AVATAR_OVER_SIZE));
        }
    }

    //
    // Implement ProfileService.Observer methods
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

        mAvatar = null;
        mProfile = space.getProfile();

        updateIdentity();
    }

    @Override
    public void onGetProfileNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetProfileNotFound");
        }

        updateIdentity();
    }

    @Override
    public void onUpdateProfile(@NonNull Profile profile) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateProfile: profile=" + profile);
        }

        mAvatar = null;
        mProfile = profile;

        updateIdentity();
    }

    @Override
    public void onGetProfileAvatar(@NonNull Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetProfileAvatar: avatar=" + avatar);
        }

        mAvatar = avatar;

        updateIdentity();
    }

    //
    // Private methods
    //

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    private void initViews(View view) {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        mNoProfileImageView = view.findViewById(R.id.profile_fragment_no_profile_image_view);

        ViewGroup.LayoutParams layoutParams = mNoProfileImageView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_NO_PROFILE_IMAGE_VIEW_HEIGHT * Design.HEIGHT_RATIO);

        mNoProfileTextView = view.findViewById(R.id.profile_fragment_no_profile_text_view);
        Design.updateTextFont(mNoProfileTextView, Design.FONT_MEDIUM34);
        mNoProfileTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mCreateProfileView = view.findViewById(R.id.profile_fragment_create_profile_view);
        mCreateProfileView.setOnClickListener(v -> onCreateProfileClick());

        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable saveViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        saveViewBackground.getPaint().setColor(Design.getMainStyle());
        mCreateProfileView.setBackground(saveViewBackground);

        layoutParams = mCreateProfileView.getLayoutParams();
        layoutParams.height = Design.BUTTON_HEIGHT;

        TextView createProfileTextView = view.findViewById(R.id.profile_fragment_create_profile_title_view);
        Design.updateTextFont(createProfileTextView, Design.FONT_MEDIUM34);
        createProfileTextView.setTextColor(Color.WHITE);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) createProfileTextView.getLayoutParams();
        marginLayoutParams.leftMargin = Design.BUTTON_MARGIN;
        marginLayoutParams.rightMargin = Design.BUTTON_MARGIN;

        mTransferView = view.findViewById(R.id.profile_fragment_transfer_view);
        mTransferView.setOnClickListener(v -> onTransferClick());

        TextView transferTextView = view.findViewById(R.id.profile_fragment_transfer_text_view);
        Design.updateTextFont(transferTextView, Design.FONT_REGULAR26);
        transferTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        transferTextView.setPaintFlags(transferTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        mAvatarView = view.findViewById(R.id.profile_fragment_avatar_view);
        mAvatarView.setBackgroundColor(Design.AVATAR_PLACEHOLDER_COLOR);

        layoutParams = mAvatarView.getLayoutParams();
        layoutParams.width = AVATAR_MAX_SIZE - AVATAR_OVER_SIZE;
        layoutParams.height = AVATAR_MAX_SIZE - AVATAR_OVER_SIZE;

        mSideMenuView = view.findViewById(R.id.profile_fragment_side_menu_view);
        mSideMenuView.setOnClickListener(v -> onSideMenuClick());

        marginLayoutParams  = (ViewGroup.MarginLayoutParams) mSideMenuView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_ROUNDED_VIEW_TOP_MARGIN * Design.HEIGHT_RATIO);

        RoundedView sideMenuRoundedView = view.findViewById(R.id.profile_fragment_side_menu_rounded_view);
        sideMenuRoundedView.setColor(Color.argb(76, 0, 0, 0));

        mAddContactView = view.findViewById(R.id.profile_fragment_add_contact_view);
        mAddContactView.setOnClickListener(v -> onAddContactClick());

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mAddContactView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_ROUNDED_VIEW_TOP_MARGIN * Design.HEIGHT_RATIO);

        RoundedView addContactRoundedView = view.findViewById(R.id.profile_fragment_add_contact_rounded_view);
        addContactRoundedView.setColor(Color.argb(76, 0, 0, 0));

        layoutParams = mSideMenuView.getLayoutParams();
        layoutParams.height = Design.EDIT_CLICKABLE_VIEW_HEIGHT;

        mContentView = view.findViewById(R.id.profile_fragment_content_view);
        mContentView.setY(AVATAR_MAX_SIZE - Design.ACTION_VIEW_MIN_MARGIN);

        mScrollView = view.findViewById(R.id.profile_fragment_content_scroll_view);

        mScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener)
                (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                    if (mScrollPosition == -1) {
                        mScrollPosition = AVATAR_OVER_SIZE;
                    }

                    float delta = mScrollPosition - scrollY;
                    ProfileFragment.this.updateAvatarSize(delta);
                    mScrollPosition = scrollY;
                });

        mScrollView.setOnTouchListener((v, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                Rect sideMenuRect = new Rect(mSideMenuView.getLeft(), mSideMenuView.getTop(), mSideMenuView.getRight(), mSideMenuView.getBottom());
                Rect avatarRect = new Rect(mAvatarView.getLeft(), mAvatarView.getTop(), mAvatarView.getRight(), mAvatarView.getBottom());
                int contentY = (int) (mContentView.getY() - mScrollView.getScrollY());
                Rect contentRect = new Rect(0, contentY, Design.DISPLAY_WIDTH, Design.DISPLAY_HEIGHT);
                Rect inviteRect = new Rect(mAddContactView.getLeft(), mAddContactView.getTop(), mAddContactView.getRight(), mAddContactView.getBottom());
                boolean isInSideMenuRect = sideMenuRect.contains((int) motionEvent.getX(), (int) motionEvent.getY());
                boolean isInAvatarRect = avatarRect.contains((int) motionEvent.getX(), (int) motionEvent.getY());
                boolean isInContentRect = contentRect.contains((int) motionEvent.getX(), (int) motionEvent.getY());
                boolean isInInviteRect = inviteRect.contains((int) motionEvent.getX(), (int) motionEvent.getY());
                if (isInSideMenuRect && !isInContentRect) {
                    onSideMenuClick();
                } else if (isInInviteRect && !isInContentRect) {
                    onAddContactClick();
                }
            }
            return false;
        });

        View slideMarkView = view.findViewById(R.id.profile_fragment_slide_mark_view);
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

        mTitleView = view.findViewById(R.id.profile_fragment_title_view);
        Design.updateTextFont(mTitleView, Design.FONT_BOLD44);
        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        View headerView = view.findViewById(R.id.profile_fragment_content_header_view);
        marginLayoutParams = (ViewGroup.MarginLayoutParams) headerView.getLayoutParams();
        marginLayoutParams.topMargin = Design.HEADER_VIEW_TOP_MARGIN;

        mEditView = view.findViewById(R.id.profile_fragment_edit_clickable_view);
        mEditView.setOnClickListener(view12 -> onEditProfileClick());

        layoutParams = mEditView.getLayoutParams();
        layoutParams.height = Design.EDIT_CLICKABLE_VIEW_HEIGHT;

        ImageView editImageView = view.findViewById(R.id.profile_fragment_edit_image_view);
        editImageView.setColorFilter(Design.getMainStyle());

        mTwincodeView = view.findViewById(R.id.profile_fragment_twincode_view);
        mTwincodeView.setOnClickListener(view13 -> onTwincodeClick());

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mTwincodeView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_TWINCODE_VIEW_TOP_MARGIN * Design.HEIGHT_RATIO);

        ShapeDrawable twincodeViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        twincodeViewBackground.getPaint().setColor(Design.getMainStyle());
        mTwincodeView.setBackground(twincodeViewBackground);

        ImageView twincodeIconView = view.findViewById(R.id.profile_fragment_twincode_icon_view);
        marginLayoutParams = (ViewGroup.MarginLayoutParams) twincodeIconView.getLayoutParams();
        marginLayoutParams.leftMargin = Design.TWINCODE_PADDING;
        marginLayoutParams.setMarginStart(Design.TWINCODE_PADDING);

        marginLayoutParams.topMargin = Design.TWINCODE_ICON_PADDING;
        marginLayoutParams.bottomMargin = Design.TWINCODE_ICON_PADDING;

        layoutParams = twincodeIconView.getLayoutParams();
        layoutParams.height = Design.TWINCODE_ICON_SIZE;
        layoutParams.width = Design.TWINCODE_ICON_SIZE;

        TextView twincodeTextView = view.findViewById(R.id.profile_fragment_twincode_title_view);
        Design.updateTextFont(twincodeTextView, Design.FONT_REGULAR28);
        twincodeTextView.setTextColor(Color.WHITE);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) twincodeTextView.getLayoutParams();
        marginLayoutParams.leftMargin = Design.TWINCODE_PADDING;
        marginLayoutParams.rightMargin = Design.TWINCODE_PADDING;
        marginLayoutParams.setMarginStart(Design.TWINCODE_PADDING);
        marginLayoutParams.setMarginEnd(Design.TWINCODE_PADDING);

        mDescriptionTextView = view.findViewById(R.id.profile_fragment_description_text_view);
        Design.updateTextFont(mDescriptionTextView, Design.FONT_MEDIUM34);
        mDescriptionTextView.setTextColor(Design.FONT_COLOR_DESCRIPTION);

        mMessageView = view.findViewById(R.id.profile_fragment_activity_message_view);
        Design.updateTextFont(mMessageView, Design.FONT_REGULAR26);
        mMessageView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mMessageView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_MESSAGE_VIEW_TOP_MARGIN * Design.HEIGHT_RATIO);

        mProgressBarView = view.findViewById(R.id.profile_fragment_progress_bar);

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
            mSideMenuView.setVisibility(View.VISIBLE);
            mScrollView.setVisibility(View.VISIBLE);
            mAvatarView.setVisibility(View.VISIBLE);

            mNoProfileImageView.setVisibility(View.GONE);
            mNoProfileTextView.setVisibility(View.GONE);
            mCreateProfileView.setVisibility(View.GONE);
            mTransferView.setVisibility(View.GONE);
        } else {
            mAddContactView.setVisibility(View.GONE);
            mDescriptionTextView.setVisibility(View.GONE);
            mMessageView.setVisibility(View.GONE);
            mTwincodeView.setVisibility(View.GONE);
            mEditView.setVisibility(View.GONE);
            mSideMenuView.setVisibility(View.GONE);
            mScrollView.setVisibility(View.GONE);
            mAvatarView.setVisibility(View.GONE);

            mNoProfileImageView.setVisibility(View.VISIBLE);
            mNoProfileTextView.setVisibility(View.VISIBLE);
            mCreateProfileView.setVisibility(View.VISIBLE);
            mTransferView.setVisibility(View.VISIBLE);
        }

        if (mAvatar == null && mProfile != null) {
            mProfileService.getProfileImage(mProfile, (Bitmap avatar) -> {
                mAvatar = avatar;
                if (avatar != null) {
                    mAvatarView.setImageBitmap(avatar);
                } else {
                    mAvatarView.setBackgroundColor(Design.AVATAR_PLACEHOLDER_COLOR);
                }
            });
        } else {
            Bitmap avatar = mAvatar;

            if (avatar != null) {
                mAvatarView.setImageBitmap(avatar);
            } else {
                mAvatarView.setBackgroundColor(Design.AVATAR_PLACEHOLDER_COLOR);
            }
        }
    }

    private void onAddContactClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onAddContactClick");
        }

        if (mProfile != null) {
            DrawerLayout drawerLayout = mTwinmeActivity.findViewById(R.id.main_activity_drawer_layout);

            MenuAddContactView menuAddContactView = new MenuAddContactView(mTwinmeActivity, null);
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            menuAddContactView.setLayoutParams(layoutParams);
            menuAddContactView.setPadding(0, 0, 0, mTwinmeActivity.getBarBottomInset());

            MenuAddContactView.Observer observer = new MenuAddContactView.Observer() {
                @Override
                public void onStartAddContactByScan() {

                    menuAddContactView.animationCloseMenu();

                    if (mTwinmeActivity == null) {
                        return;
                    }

                    Intent intent = new Intent();
                    intent.putExtra(Intents.INTENT_PROFILE_ID, mProfile.getId());
                    intent.putExtra(Intents.INTENT_INVITATION_MODE, AddContactActivity.InvitationMode.SCAN);
                    intent.setClass(mTwinmeActivity, AddContactActivity.class);
                    startActivity(intent);
                }

                @Override
                public void onStartAddContactByInvite() {

                    menuAddContactView.animationCloseMenu();

                    if (mTwinmeActivity == null) {
                        return;
                    }

                    Intent intent = new Intent();
                    intent.putExtra(Intents.INTENT_PROFILE_ID, mProfile.getId());
                    intent.putExtra(Intents.INTENT_INVITATION_MODE, AddContactActivity.InvitationMode.INVITE);
                    intent.setClass(mTwinmeActivity, AddContactActivity.class);
                    startActivity(intent);
                }

                @Override
                public void onCloseMenuSelectActionAnimationEnd() {

                    drawerLayout.removeView(menuAddContactView);
                }
            };

            menuAddContactView.setObserver(observer);

            drawerLayout.addView(menuAddContactView);

            List<UIMenuSelectAction> actions = new ArrayList<>();
            actions.add(new UIMenuSelectAction(getString(R.string.contacts_fragment_scan_contact_title), R.drawable.scan_code));
            actions.add(new UIMenuSelectAction(getString(R.string.contacts_fragment_invite_contact_title), R.drawable.qrcode));
            menuAddContactView.setActions(actions, mTwinmeActivity);
            menuAddContactView.openMenu(false);

        }
    }

    private void onTransferClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onTransferClick");
        }

        // This fragment is detached and has no activity: ignore the action.
        if (!isAdded() || mTwinmeActivity == null) {
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(Intents.INTENT_MIGRATION_FROM_CURRENT_DEVICE, false);
        intent.setClass(mTwinmeActivity, AccountMigrationScannerActivity.class);
        startActivity(intent);
    }

    private void onSideMenuClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSideMenuClick");
        }

        if (mTwinmeActivity != null) {
            onBackClick();
            mTwinmeActivity.openSideMenu();
        }
    }

    private void onBackClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBackClick");
        }

    }

    private void onCreateProfileClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateProfileClick");
        }

        Intent intent = new Intent();
        intent.putExtra(Intents.INTENT_FIRST_PROFILE, true);
        startActivity(intent, AddProfileActivity.class);
    }

    protected void onEditProfileClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onEditProfileClick");
        }

        if (mProfile != null) {
            Intent intent = new Intent();
            intent.putExtra(Intents.INTENT_PROFILE_ID, mProfile.getId().toString());
            startActivity(intent, EditProfileActivity.class);
        }
    }

    protected void onTwincodeClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onTwincodeClick");
        }

        if (mProfile != null) {
            Intent intent = new Intent();
            intent.putExtra(Intents.INTENT_PROFILE_ID, mProfile.getId().toString());
            intent.putExtra(Intents.INTENT_INVITATION_MODE, AddContactActivity.InvitationMode.INVITE_ONLY);
            startActivity(intent, AddContactActivity.class);
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

    private void updateColor() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateColor");
        }

        // Don't try to access the activity or the resource if the fragment is detached.
        if (mTwinmeActivity == null) {
            return;
        }

        final Resources resources = mTwinmeActivity.getResources();

        boolean darkMode = false;
        int currentNightMode = resources.getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        int displayMode = Settings.displayMode.getInt();
        if ((currentNightMode == Configuration.UI_MODE_NIGHT_YES && displayMode == DisplayMode.SYSTEM.ordinal())  || displayMode == DisplayMode.DARK.ordinal()) {
            darkMode = true;
        }

        mNoProfileImageView.setImageDrawable(ResourcesCompat.getDrawable(resources, darkMode ? R.drawable.onboarding_step3_dark : R.drawable.onboarding_step3, null));
    }
}
