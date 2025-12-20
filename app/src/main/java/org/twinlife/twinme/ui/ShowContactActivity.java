/*
 *  Copyright (c) 2015-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.util.Utils;
import org.twinlife.twinme.calls.CallStatus;
import org.twinlife.twinme.models.CertificationLevel;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.schedule.DateTime;
import org.twinlife.twinme.models.schedule.DateTimeRange;
import org.twinlife.twinme.models.schedule.Schedule;
import org.twinlife.twinme.services.ShowContactService;
import org.twinlife.twinme.skin.CircularImageDescriptor;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.skin.DisplayMode;
import org.twinlife.twinme.ui.callActivity.CallActivity;
import org.twinlife.twinme.ui.cleanupActivity.TypeCleanUpActivity;
import org.twinlife.twinme.ui.contacts.AuthentifiedRelationActivity;
import org.twinlife.twinme.ui.contacts.ContactCapabilitiesActivity;
import org.twinlife.twinme.ui.contacts.MenuCertifyView;
import org.twinlife.twinme.ui.conversationActivity.ConversationActivity;
import org.twinlife.twinme.ui.conversationFilesActivity.ConversationFilesActivity;
import org.twinlife.twinme.ui.exportActivity.ExportActivity;
import org.twinlife.twinme.utils.AbstractBottomSheetView;
import org.twinlife.twinme.utils.CircularImageView;
import org.twinlife.twinme.utils.OnboardingConfirmView;
import org.twinlife.twinme.utils.RoundedView;
import org.twinlife.twinme.utils.UIMenuSelectAction;
import org.twinlife.twinme.utils.coachmark.CoachMark;
import org.twinlife.twinme.utils.coachmark.CoachMarkView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ShowContactActivity extends AbstractTwinmeActivity implements ShowContactService.Observer {
    private static final String LOG_TAG = "ShowContactActivity";
    private static final boolean DEBUG = false;

    private static final int COACH_MARK_DELAY = 500;

    private static final float DESIGN_NAME_WIDTH_PERCENT = 0.6667f;
    private static final int DESIGN_CERTIFIED_MARGIN = 20;
    private static final float DESIGN_CERTIFIED_HEIGHT = 34;

    private static int AVATAR_OVER_SIZE;
    private static int AVATAR_MAX_SIZE;

    @Nullable
    private UUID mContactId;

    private View mBackClickableView;
    private ImageView mAvatarView;
    private View mContentView;
    private View mActionView;
    private TextView mNameView;
    private View mCertifiedHeaderView;
    private TextView mDescriptionView;
    private TextView mIdentityTextView;
    private CircularImageView mIdentityAvatarView;
    private View mFallbackView;
    private View mAudioClickableView;
    private View mVideoClickableView;
    private View mIdentityView;
    private View mSettingsView;
    private View mCertifiedView;
    private ImageView mCertifiedImageView;
    private TextView mCertifiedTextView;
    private RoundedView mRoundedChatView;
    private TextView mChatTextView;
    private RoundedView mRoundedVideoView;
    private TextView mVideoTextView;
    private RoundedView mRoundedAudioView;
    private TextView mAudioTextView;
    private TextView mIdentityTitleView;
    private TextView mSettingsTitleView;
    private TextView mSettingsTextView;
    private TextView mLastCallsTitleView;
    private TextView mLastCallsTextView;
    private TextView mConversationsTitleView;
    private TextView mFilesTextView;
    private ImageView mFilesImageView;
    private ImageView mExportImageView;
    private TextView mExportTextView;
    private ImageView mCleanImageView;
    private TextView mCleanTextView;
    private TextView mFallbackTextView;
    private TextView mRemoveTextView;

    private CoachMarkView mCoachMarkView;

    private ScrollView mScrollView;

    private boolean mUIInitialized = false;
    @Nullable
    private Contact mContact;
    private boolean mDeletedContact = false;
    private String mContactName;
    private String mContactDescription;
    private Bitmap mContactAvatar;
    private String mIdentityName;
    private Bitmap mIdentityAvatar;

    private float mAvatarLastSize = -1;
    private float mScrollPosition = -1;

    private boolean mInitScrollView = false;

    @Nullable
    private ShowContactService mShowContactService;

    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        initViews();

        mContactId = Utils.UUIDFromString(intent.getStringExtra(Intents.INTENT_CONTACT_ID));
        if (mContactId == null) {

            finish();
            return;
        }

        mShowContactService = new ShowContactService(this, getTwinmeContext(), this, mContactId);
    }

    @Override
    protected void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        super.onResume();

        if (mScrollView != null && !mInitScrollView) {
            mInitScrollView = true;
            mScrollView.post(() -> mScrollView.scrollBy(0, AVATAR_OVER_SIZE));
        }

        if (mDeletedContact) {
            finish();
        } else {
            updateContact();
        }

        showCoachMark();
    }

    //
    // Override Activity methods
    //

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        if (mShowContactService != null) {
            mShowContactService.dispose();
        }

        super.onDestroy();
    }

    //
    // Implement ShowContactService.Observer methods
    //

    @Override
    public void onGetContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContact contact=" + contact);
        }

        mContact = contact;
        if (mContact.hasPeer()) {
            setFullscreen();
            mContactName = contact.getName();
            mContactAvatar = avatar;
            if (mContactAvatar == null) {
                mContactAvatar = getDefaultAvatar();
            }
            mIdentityName = contact.getIdentityName();
            if (mIdentityName == null) {
                mIdentityName = getAnonymousName();
            }

            if (mContact.getDescription() != null && !mContact.getDescription().isEmpty()) {
                mContactDescription = mContact.getDescription();
            } else {
                mContactDescription = mContact.getPeerDescription();
            }


            if (mShowContactService == null) {
                mIdentityAvatar = getAnonymousAvatar();
            } else {
                mShowContactService.getIdentityImage(mContact, (Bitmap identityAvatar) -> {
                    mIdentityAvatar = identityAvatar;
                    updateContact();
                });
                return;
            }
        } else {
            mFallbackView.setVisibility(View.VISIBLE);
            mBackClickableView.setVisibility(View.GONE);
            mContentView.setVisibility(View.GONE);
            mScrollView.setVisibility(View.GONE);

            ViewGroup.LayoutParams avatarLayoutParams = mAvatarView.getLayoutParams();
            avatarLayoutParams.width = Design.DISPLAY_WIDTH;
            //noinspection SuspiciousNameCombination
            avatarLayoutParams.height = Design.DISPLAY_WIDTH;
            mAvatarView.requestLayout();

            setStatusBarColor();
            showToolBar(true);
            showBackButton(true);
            applyInsets(R.id.show_contact_activity_layout, R.id.show_contact_activity_tool_bar, R.id.show_contact_activity_fallback_view, Design.TOOLBAR_COLOR, true);

            setTitle(mContact.getName());

            mContactName = contact.getName();
            mContactAvatar = getAnonymousAvatar();
            mIdentityName = getAnonymousName();
            mIdentityAvatar = getAnonymousAvatar();
        }

        updateContact();
    }

    @Override
    public void onGetContactNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContactNotFound");
        }

        mFallbackView.setVisibility(View.VISIBLE);
        mBackClickableView.setVisibility(View.GONE);
        mContentView.setVisibility(View.GONE);
        mScrollView.setVisibility(View.GONE);

        ViewGroup.LayoutParams avatarLayoutParams = mAvatarView.getLayoutParams();
        avatarLayoutParams.width = Design.DISPLAY_WIDTH;
        //noinspection SuspiciousNameCombination
        avatarLayoutParams.height = Design.DISPLAY_WIDTH;
        mAvatarView.requestLayout();

        setStatusBarColor();
        showToolBar(true);
        showBackButton(true);
        applyInsets(R.id.show_contact_activity_layout, R.id.show_contact_activity_tool_bar, R.id.show_contact_activity_fallback_view, Design.TOOLBAR_COLOR, true);
    }

    @Override
    public void onUpdateImage(@NonNull Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateImage avatar=" + avatar);
        }

        mContactAvatar = avatar;
        mAvatarView.setImageBitmap(avatar);
    }

    @Override
    public void onUpdateContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateContact contact=" + contact);
        }

        if (!contact.getId().equals(mContactId)) {

            return;
        }

        mContact = contact;
        if (contact.hasPeer()) {
            mContactName = contact.getName();
            mContactAvatar = avatar;
            if (mContactAvatar == null) {
                mContactAvatar = getDefaultAvatar();
            }
            mIdentityName = contact.getIdentityName();
            if (mIdentityName == null) {
                mIdentityName = getAnonymousName();
            }

            if (mContact.getDescription() != null && !mContact.getDescription().isEmpty()) {
                mContactDescription = mContact.getDescription();
            } else {
                mContactDescription = mContact.getPeerDescription();
            }

            if (mShowContactService == null) {
                mIdentityAvatar = getAnonymousAvatar();
            } else {
                mShowContactService.getIdentityImage(mContact, (Bitmap identityAvatar) -> {
                    mIdentityAvatar = identityAvatar;
                    updateContact();
                });
                return;
            }
        } else {
            mFallbackView.setVisibility(View.VISIBLE);
            mBackClickableView.setVisibility(View.GONE);
            mContentView.setVisibility(View.GONE);
            mScrollView.setVisibility(View.GONE);

            ViewGroup.LayoutParams avatarLayoutParams = mAvatarView.getLayoutParams();
            avatarLayoutParams.width = Design.DISPLAY_WIDTH;
            //noinspection SuspiciousNameCombination
            avatarLayoutParams.height = Design.DISPLAY_WIDTH;
            mAvatarView.requestLayout();

            setStatusBarColor();
            showToolBar(true);
            showBackButton(true);
            applyInsets(R.id.show_contact_activity_layout, R.id.show_contact_activity_tool_bar, R.id.show_contact_activity_fallback_view, Design.TOOLBAR_COLOR, true);

            mContactName = contact.getName();
            mContactAvatar = getAnonymousAvatar();
            mIdentityName = getAnonymousName();
            mIdentityAvatar = getAnonymousAvatar();
        }

        updateContact();
    }

    @Override
    public void onDeleteContact(@NonNull UUID contactId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteContact contactId=" + contactId);
        }

        if (!contactId.equals(mContactId)) {

            return;
        }

        mDeletedContact = true;

        if (mResumed) {
            finish();
        }
    }

    //
    // Private methods
    //
    @SuppressLint({"ClickableViewAccessibility"})
    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.show_contact_activity);

        setToolBar(R.id.show_contact_activity_tool_bar);
        setTitle(getString(R.string.application_name));
        showToolBar(false);
        showBackButton(true);
        setBackgroundColor(Design.WHITE_COLOR);

        applyInsets(R.id.show_contact_activity_layout, -1, -1, Design.WHITE_COLOR, true);

        mAvatarView = findViewById(R.id.show_contact_activity_avatar_view);

        ViewGroup.LayoutParams layoutParams = mAvatarView.getLayoutParams();
        layoutParams.width = AVATAR_MAX_SIZE - AVATAR_OVER_SIZE;
        layoutParams.height = AVATAR_MAX_SIZE - AVATAR_OVER_SIZE;

        mBackClickableView = findViewById(R.id.show_contact_activity_back_clickable_view);
        mBackClickableView.setOnClickListener(view -> onBackClick());

        layoutParams = mBackClickableView.getLayoutParams();
        layoutParams.height = Design.BACK_CLICKABLE_VIEW_HEIGHT;

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mBackClickableView.getLayoutParams();
        marginLayoutParams.leftMargin = Design.BACK_CLICKABLE_VIEW_LEFT_MARGIN;
        marginLayoutParams.topMargin = Design.BACK_CLICKABLE_VIEW_TOP_MARGIN;

        RoundedView backRoundedView = findViewById(R.id.show_contact_activity_back_rounded_view);
        backRoundedView.setColor(Design.BACK_VIEW_COLOR);

        mContentView = findViewById(R.id.show_contact_activity_content_view);
        mContentView.setY(AVATAR_MAX_SIZE - Design.ACTION_VIEW_MIN_MARGIN);

        setBackground(mContentView);

        View slideMarkView = findViewById(R.id.show_contact_activity_slide_mark_view);
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

        mScrollView = findViewById(R.id.show_contact_activity_content_scroll_view);
        ViewTreeObserver viewTreeObserver = mScrollView.getViewTreeObserver();
        viewTreeObserver.addOnScrollChangedListener(() -> {
            if (mScrollPosition == -1) {
                mScrollPosition = AVATAR_OVER_SIZE;
            }

            float delta = mScrollPosition - mScrollView.getScrollY();
            updateAvatarSize(delta);
            mScrollPosition = mScrollView.getScrollY();
        });

        mNameView = findViewById(R.id.show_contact_activity_name_view);

        mCertifiedHeaderView = findViewById(R.id.show_contact_activity_certified_header_image_view);

        layoutParams = mCertifiedHeaderView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_CERTIFIED_HEIGHT * Design.HEIGHT_RATIO);

        View headerView = findViewById(R.id.show_contact_activity_content_header_view);
        marginLayoutParams = (ViewGroup.MarginLayoutParams) headerView.getLayoutParams();
        marginLayoutParams.topMargin = Design.HEADER_VIEW_TOP_MARGIN;

        View editClickableView = findViewById(R.id.show_contact_activity_edit_clickable_view);
        editClickableView.setOnClickListener(view -> onEditClick());

        layoutParams = editClickableView.getLayoutParams();
        layoutParams.height = Design.EDIT_CLICKABLE_VIEW_HEIGHT;

        ImageView editImageView = findViewById(R.id.show_contact_activity_edit_image_view);
        editImageView.setColorFilter(Design.getMainStyle());

        mDescriptionView = findViewById(R.id.show_contact_activity_description_view);

        mActionView = findViewById(R.id.show_contact_activity_action_view);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mActionView.getLayoutParams();
        marginLayoutParams.topMargin = Design.ACTION_VIEW_TOP_MARGIN;

        View chatClickableView = findViewById(R.id.show_contact_activity_chat_clickable_view);
        chatClickableView.setOnClickListener(view -> onChatClick());

        layoutParams = chatClickableView.getLayoutParams();
        layoutParams.height = Design.ACTION_CLICKABLE_VIEW_HEIGHT;

        mRoundedChatView = findViewById(R.id.show_contact_activity_chat_rounded_view);
        mChatTextView = findViewById(R.id.show_contact_activity_chat_text_view);

        mVideoClickableView = findViewById(R.id.show_contact_activity_video_clickable_view);
        mVideoClickableView.setOnClickListener(view -> onVideoClick(false));

        layoutParams = mVideoClickableView.getLayoutParams();
        layoutParams.height = Design.ACTION_CLICKABLE_VIEW_HEIGHT;

        mRoundedVideoView = findViewById(R.id.show_contact_activity_video_rounded_view);
        mVideoTextView = findViewById(R.id.show_contact_activity_video_text_view);

        mAudioClickableView = findViewById(R.id.show_contact_activity_audio_clickable_view);
        mAudioClickableView.setOnClickListener(view -> onAudioClick());

        layoutParams = mAudioClickableView.getLayoutParams();
        layoutParams.height = Design.ACTION_CLICKABLE_VIEW_HEIGHT;

        mRoundedAudioView = findViewById(R.id.show_contact_activity_audio_rounded_view);
        mRoundedAudioView.setColor(Design.AUDIO_CALL_COLOR);

        mAudioTextView = findViewById(R.id.show_contact_activity_audio_text_view);

        mIdentityView = findViewById(R.id.show_contact_activity_identity_view);
        mIdentityView.setOnClickListener(view -> onEditIdentityClick());

        layoutParams = mIdentityView.getLayoutParams();
        layoutParams.height = Design.SECTION_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mIdentityView.getLayoutParams();
        marginLayoutParams.topMargin = Design.IDENTITY_VIEW_TOP_MARGIN;

        mIdentityTitleView = findViewById(R.id.show_contact_activity_identity_title_view);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mIdentityTitleView.getLayoutParams();
        marginLayoutParams.topMargin = Design.TITLE_IDENTITY_TOP_MARGIN;

        mIdentityTextView = findViewById(R.id.show_contact_activity_identity_text_view);

        mIdentityAvatarView = findViewById(R.id.show_contact_activity_identity_avatar_view);

        mCertifiedView = findViewById(R.id.show_contact_activity_certified_view);
        mCertifiedView.setOnClickListener(view -> onCertifiedViewClick());

        layoutParams = mCertifiedView.getLayoutParams();
        layoutParams.height = Design.SECTION_HEIGHT;

        mCertifiedTextView = findViewById(R.id.show_contact_activity_certified_text_view);

        mCertifiedImageView = findViewById(R.id.show_contact_activity_certified_image_view);

        mSettingsTitleView = findViewById(R.id.show_contact_activity_settings_title_view);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mSettingsTitleView.getLayoutParams();
        marginLayoutParams.topMargin = Design.TITLE_IDENTITY_TOP_MARGIN;

        mSettingsView = findViewById(R.id.show_contact_activity_settings_view);
        mSettingsView.setOnClickListener(view -> onSettingsClick());

        layoutParams = mSettingsView.getLayoutParams();
        layoutParams.height = Design.SECTION_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mSettingsView.getLayoutParams();
        marginLayoutParams.topMargin = Design.IDENTITY_VIEW_TOP_MARGIN;

        mSettingsTextView = findViewById(R.id.show_contact_activity_settings_text_view);

        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        TextView newFeatureTitleView = findViewById(R.id.show_contact_activity_settings_new_feature_title);
        Design.updateTextFont(newFeatureTitleView, Design.FONT_MEDIUM30);
        newFeatureTitleView.setTextColor(Color.WHITE);
        newFeatureTitleView.setPadding(Design.NEW_FEATURE_PADDING, 0, Design.NEW_FEATURE_PADDING, 0);
        newFeatureTitleView.setOnClickListener(view -> showControlCameraOnboarding());

        layoutParams = newFeatureTitleView.getLayoutParams();
        layoutParams.height = Design.NEW_FEATURE_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) newFeatureTitleView.getLayoutParams();
        marginLayoutParams.topMargin = - (int) (Design.NEW_FEATURE_HEIGHT * 0.5f);
        marginLayoutParams.leftMargin = - Design.NEW_FEATURE_MARGIN;
        marginLayoutParams.rightMargin = - Design.NEW_FEATURE_MARGIN;

        ShapeDrawable newFeatureTitleViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        newFeatureTitleViewBackground.getPaint().setColor(Design.getMainStyle());
        newFeatureTitleView.setBackground(newFeatureTitleViewBackground);

        mLastCallsTitleView = findViewById(R.id.show_contact_activity_last_calls_title_view);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mLastCallsTitleView.getLayoutParams();
        marginLayoutParams.topMargin = Design.TITLE_IDENTITY_TOP_MARGIN;

        View lastCallsView = findViewById(R.id.show_contact_activity_last_calls_view);
        layoutParams = lastCallsView.getLayoutParams();
        layoutParams.height = Design.SECTION_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) lastCallsView.getLayoutParams();
        marginLayoutParams.topMargin = Design.IDENTITY_VIEW_TOP_MARGIN;

        lastCallsView.setOnClickListener(view -> onLastCallsClick());

        mLastCallsTextView = findViewById(R.id.show_contact_activity_last_calls_text_view);

        mConversationsTitleView = findViewById(R.id.show_contact_activity_conversations_title_view);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mConversationsTitleView.getLayoutParams();
        marginLayoutParams.topMargin = Design.TITLE_IDENTITY_TOP_MARGIN;

        View filesView = findViewById(R.id.show_contact_activity_files_view);
        layoutParams = filesView.getLayoutParams();
        layoutParams.height = Design.SECTION_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) filesView.getLayoutParams();
        marginLayoutParams.topMargin = Design.IDENTITY_VIEW_TOP_MARGIN;

        filesView.setOnClickListener(view -> onConversationFilesClick());

        mFilesTextView = findViewById(R.id.show_contact_activity_files_text_view);

        mFilesImageView = findViewById(R.id.show_contact_activity_files_image_view);

        View exportView = findViewById(R.id.show_contact_activity_export_view);
        layoutParams = exportView.getLayoutParams();
        layoutParams.height = Design.SECTION_HEIGHT;

        exportView.setOnClickListener(view -> onExportClick());

        mExportTextView = findViewById(R.id.show_contact_activity_export_text_view);

        mExportImageView = findViewById(R.id.show_contact_activity_export_image_view);

        View cleanView = findViewById(R.id.show_contact_activity_clean_view);
        layoutParams = cleanView.getLayoutParams();
        layoutParams.height = Design.SECTION_HEIGHT;

        cleanView.setOnClickListener(view -> onCleanClick());

        ViewTreeObserver contentViewTreeObserver = mContentView.getViewTreeObserver();
        contentViewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewTreeObserver viewTreeObserver = mContentView.getViewTreeObserver();
                viewTreeObserver.removeOnGlobalLayoutListener(this);

                Rect rectangle = new Rect();
                getWindow().getDecorView().getWindowVisibleDisplayFrame(rectangle);
                int contentHeight = (int) (cleanView.getHeight() + cleanView.getY());
                if (contentHeight < rectangle.height()) {
                    contentHeight = rectangle.height();
                }

                ViewGroup.LayoutParams layoutParams = mContentView.getLayoutParams();
                layoutParams.height = contentHeight + AVATAR_MAX_SIZE;
                mContentView.requestLayout();
            }
        });

        mCleanTextView = findViewById(R.id.show_contact_activity_clean_text_view);

        mCleanImageView = findViewById(R.id.show_contact_activity_clean_image_view);

        mFallbackView = findViewById(R.id.show_contact_activity_fallback_view);
        mFallbackView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        mFallbackTextView = findViewById(R.id.show_contact_activity_fallback_message_view);

        View removeView = findViewById(R.id.show_contact_activity_remove_view);
        removeView.setOnClickListener(v -> onRemoveClick());

        mRemoveTextView = findViewById(R.id.show_contact_activity_remove_text_view);

        mCoachMarkView = findViewById(R.id.show_contact_activity_coach_mark_view);
        CoachMarkView.OnCoachMarkViewListener onCoachMarkViewListener = new CoachMarkView.OnCoachMarkViewListener() {
            @Override
            public void onCloseCoachMark() {

                mCoachMarkView.setVisibility(View.GONE);
            }

            @Override
            public void onTapCoachMarkFeature() {

                mCoachMarkView.setVisibility(View.GONE);
                getTwinmeApplication().hideCoachMark(CoachMark.CoachMarkTag.CONTACT_CAPABILITIES);
                onSettingsClick();
            }

            @Override
            public void onLongPressCoachMarkFeature() {

            }
        };

        mCoachMarkView.setOnCoachMarkViewListener(onCoachMarkViewListener);

        mUIInitialized = true;

        updateFont();
        updateColor();
    }

    private void onEditClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onEditClick");
        }

        if (mContact != null && mContactId != null) {
            startActivity(EditContactActivity.class, Intents.INTENT_CONTACT_ID, mContactId);
        }
    }

    private void onChatClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onChatClick");
        }

        if (mContactId != null) {
            startActivity(ConversationActivity.class, Intents.INTENT_CONTACT_ID, mContactId);
        }
    }

    private void onVideoClick(boolean startCertifyCall) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onVideoClick");
        }

        if (mContact == null || mContactId == null) {
            return;
        }

        if (!mContact.hasPrivatePeer()) {
            Toast.makeText(this, R.string.show_contact_activity_pending_message, Toast.LENGTH_SHORT).show();
            return;
        }

        if (getTwinmeApplication().inCallInfo() == null && mContact.getCapabilities().hasVideo() && !hasSchedule()) {
            Intent intent = new Intent();
            intent.putExtra(Intents.INTENT_CERTIFY_BY_VIDEO_CALL, startCertifyCall);
            intent.putExtra(Intents.INTENT_CONTACT_ID, mContactId.toString());
            intent.putExtra(Intents.INTENT_CALL_MODE, CallStatus.OUTGOING_VIDEO_CALL);

            startActivity(CallActivity.class, intent);
        } else if (!mContact.getCapabilities().hasVideo()) {
            Toast.makeText(this, R.string.application_not_authorized_operation_by_your_contact, Toast.LENGTH_SHORT).show();
        } else if (hasSchedule()) {
            showSchedule();
        }
    }

    private void onAudioClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onAudioClick");
        }

        if (mContact == null || mContactId == null) {
            return;
        }

        if (!mContact.hasPrivatePeer()) {
            Toast.makeText(this, R.string.show_contact_activity_pending_message, Toast.LENGTH_SHORT).show();
            return;
        }

        if (getTwinmeApplication().inCallInfo() == null && mContact.getCapabilities().hasAudio() && !hasSchedule()) {
            Intent intent = new Intent();
            intent.putExtra(Intents.INTENT_CONTACT_ID, mContactId.toString());
            intent.putExtra(Intents.INTENT_CALL_MODE, CallStatus.OUTGOING_CALL);

            startActivity(CallActivity.class, intent);
        } else if (!mContact.getCapabilities().hasAudio()) {
            Toast.makeText(this, R.string.application_not_authorized_operation_by_your_contact, Toast.LENGTH_SHORT).show();
        } else if (hasSchedule()) {
            showSchedule();
        }
    }

    private void onEditIdentityClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onEditIdentityClick");
        }

        if (mContact != null && mContactId != null) {

            if (mContact.hasPrivatePeer()) {
                startActivity(EditIdentityActivity.class, Intents.INTENT_CONTACT_ID, mContactId);
            } else {
                Toast.makeText(this, R.string.show_contact_activity_pending_message, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void onCertifiedViewClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCertifiedViewClick");
        }

        if (mContact != null && mContactId != null) {
            if (mContact.hasPrivatePeer()) {
                if (mContact.getCertificationLevel() == CertificationLevel.LEVEL_4) {
                    Intent intent = new Intent(this, AuthentifiedRelationActivity.class);
                    intent.putExtra(Intents.INTENT_CONTACT_ID, mContactId.toString());
                    intent.putExtra(Intents.INTENT_START_SCAN, false);
                    intent.putExtra(Intents.INTENT_SHOW_ONBOARDING, true);
                    startActivity(intent);
                } else {
                    openMenuCertify();
                }
            } else {
                Toast.makeText(this, R.string.show_contact_activity_pending_message, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void onLastCallsClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onLastCallsClick");
        }

        if (mContact != null && mContactId != null) {
            startActivity(LastCallsActivity.class, Intents.INTENT_CONTACT_ID, mContactId);
        }
    }

    private void onRemoveClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRemoveClick");
        }

        if (mContact != null && mShowContactService != null) {
            mShowContactService.deleteContact(mContact);
        }
    }

    private void onSettingsClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSettingsClick");
        }

        if (mContactId != null) {
            startActivity(ContactCapabilitiesActivity.class, Intents.INTENT_CONTACT_ID, mContactId);
        }
    }

    private void onExportClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onExportClick");
        }

        if (mContact != null && mContactId != null) {
            startActivity(ExportActivity.class, Intents.INTENT_CONTACT_ID, mContactId);
        }
    }

    private void onCleanClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCleanClick");
        }

        if (mContact != null && mContactId != null) {
            startActivity(TypeCleanUpActivity.class, Intents.INTENT_CONTACT_ID, mContactId);
        }
    }

    private void onConversationFilesClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onConversationFilesClick");
        }

        if (mContact != null && mContactId != null) {
            startActivity(ConversationFilesActivity.class, Intents.INTENT_CONTACT_ID, mContactId);
        }
    }

    private void updateContact() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateContact");
        }

        if (mUIInitialized && mResumed && mContact != null) {
            mAvatarView.setImageBitmap(mContactAvatar);
            mIdentityTextView.setText(mIdentityName);
            mNameView.setText(mContactName);

            if (mContactDescription != null && !mContactDescription.isEmpty()) {
                mDescriptionView.setText(mContactDescription);
                mDescriptionView.setVisibility(View.VISIBLE);
            } else {
                mDescriptionView.setVisibility(View.GONE);
            }

            mIdentityAvatarView.setImage(this, null, new CircularImageDescriptor(mIdentityAvatar, 0.5f, 0.5f, 0.5f));

            if (getTwinmeApplication().inCallInfo() != null || !mContact.getCapabilities().hasAudio() || !mContact.hasPrivatePeer() || hasSchedule()) {
                mAudioClickableView.setAlpha(0.5f);
            } else {
                mAudioClickableView.setAlpha(1f);
            }

            if (getTwinmeApplication().inCallInfo() != null || !mContact.getCapabilities().hasVideo() || !mContact.hasPrivatePeer() || hasSchedule()) {
                mVideoClickableView.setAlpha(0.5f);
            } else {
                mVideoClickableView.setAlpha(1f);
            }

            if (!mContact.hasPrivatePeer()) {
                mIdentityView.setAlpha(0.5f);
            } else {
                mIdentityView.setAlpha(1f);
            }

            if (mContact.getCertificationLevel() == CertificationLevel.LEVEL_0) {
                mCertifiedView.setVisibility(View.GONE);
                mCertifiedHeaderView.setVisibility(View.GONE);
                mNameView.setMaxWidth((int) (Design.DISPLAY_WIDTH * DESIGN_NAME_WIDTH_PERCENT));

                ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mCertifiedHeaderView.getLayoutParams();
                marginLayoutParams.leftMargin = 0;
                marginLayoutParams.setMarginStart(0);
            } else {
                mCertifiedView.setVisibility(View.VISIBLE);
                if (mContact.getCertificationLevel() == CertificationLevel.LEVEL_4) {
                    mCertifiedTextView.setText(getString(R.string.authentified_relation_activity_title));
                    mCertifiedImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.authentified_relation_icon, null));
                    mCertifiedImageView.setColorFilter(Color.TRANSPARENT);
                    mCertifiedHeaderView.setVisibility(View.VISIBLE);
                    mNameView.setMaxWidth((int) ((Design.DISPLAY_WIDTH * DESIGN_NAME_WIDTH_PERCENT) - (DESIGN_CERTIFIED_MARGIN * Design.WIDTH_RATIO) - (DESIGN_CERTIFIED_HEIGHT * Design.HEIGHT_RATIO)));
                    ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mCertifiedHeaderView.getLayoutParams();
                    marginLayoutParams.leftMargin = (int) (DESIGN_CERTIFIED_MARGIN * Design.WIDTH_RATIO);
                    marginLayoutParams.setMarginStart((int) (DESIGN_CERTIFIED_MARGIN * Design.WIDTH_RATIO));
                } else {
                    mCertifiedTextView.setText(getString(R.string.authentified_relation_activity_to_be_certified_title));
                    mCertifiedImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.authentified_relation_grey_icon, null));
                    mCertifiedHeaderView.setVisibility(View.GONE);
                    mNameView.setMaxWidth((int) (Design.DISPLAY_WIDTH * DESIGN_NAME_WIDTH_PERCENT));
                    ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mCertifiedHeaderView.getLayoutParams();
                    marginLayoutParams.leftMargin = 0;
                    marginLayoutParams.setMarginStart(0);
                }
            }
        }
    }

    private void openMenuCertify() {
        if (DEBUG) {
            Log.d(LOG_TAG, "openMenuCertify");
        }

        ViewGroup viewGroup = findViewById(R.id.show_contact_activity_layout);

        MenuCertifyView menuCertifyView = new MenuCertifyView(this, null);

        MenuCertifyView.Observer observer = new MenuCertifyView.Observer() {
            @Override
            public void onStartCertifyByScan() {

                menuCertifyView.animationCloseMenu();

                final CertificationLevel level = mContact.getCertificationLevel();
                Intent intent = new Intent(getApplicationContext(), AuthentifiedRelationActivity.class);
                intent.putExtra(Intents.INTENT_CONTACT_ID, mContactId.toString());
                intent.putExtra(Intents.INTENT_START_SCAN, level == CertificationLevel.LEVEL_2
                    || (level == CertificationLevel.LEVEL_1 && mContact.getPublicPeerTwincodeOutboundId() == null));
                intent.putExtra(Intents.INTENT_SHOW_ONBOARDING, level == CertificationLevel.LEVEL_4);
                startActivity(intent);
            }

            @Override
            public void onStartCertifyByVideoCall() {

                menuCertifyView.animationCloseMenu();

                if (mContact == null || mContactId == null) {
                    return;
                }

                if (!mContact.getCapabilities().hasVideo()) {
                    Toast.makeText(getApplicationContext(), String.format(getString(R.string.authentified_relation_activity_certify_by_video_call_missing_capability), mContactName), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (getTwinmeApplication().startOnboarding(TwinmeApplication.OnboardingType.CERTIFIED_RELATION)) {
                    showCertifyOnboarding();
                } else {
                    onVideoClick(true);
                }
            }

            @Override
            public void onCloseMenuSelectActionAnimationEnd() {

                viewGroup.removeView(menuCertifyView);

                Window window = getWindow();
                window.setNavigationBarColor(Design.WHITE_COLOR);
            }
        };

        menuCertifyView.setObserver(observer);
        viewGroup.addView(menuCertifyView);

        List<UIMenuSelectAction> actions = new ArrayList<>();
        actions.add(new UIMenuSelectAction(getString(R.string.authentified_relation_activity_certify_by_scan), R.drawable.certify_by_scan_icon));
        actions.add(new UIMenuSelectAction(getString(R.string.authentified_relation_activity_certify_by_video_call), R.drawable.video_call));
        menuCertifyView.setActions(actions, this);
        menuCertifyView.openMenu(false);

        Window window = getWindow();
        window.setNavigationBarColor(Design.POPUP_BACKGROUND_COLOR);
    }

    private void showCoachMark() {
        if (DEBUG) {
            Log.d(LOG_TAG, "showCoachMark");
        }

        if (getTwinmeApplication().showCoachMark(CoachMark.CoachMarkTag.CONTACT_CAPABILITIES)) {
            mCoachMarkView.postDelayed(() -> {
                mCoachMarkView.setVisibility(View.VISIBLE);
                CoachMark coachMark = new CoachMark(getString(R.string.show_contact_activity_settings_coach_mark), CoachMark.CoachMarkTag.CONTACT_CAPABILITIES, true, true, new Point(0, (int) mContentView.getY() + (int) mSettingsView.getY()),mSettingsView.getWidth(), mSettingsView.getHeight(), 0);
                mCoachMarkView.openCoachMark(coachMark);
            }, COACH_MARK_DELAY);
        }
    }

    private boolean hasSchedule() {
        if (DEBUG) {
            Log.d(LOG_TAG, "hasSchedule");
        }

        if (mContact != null && mContact.getCapabilities().getSchedule() != null && mContact.getCapabilities().getSchedule().isEnabled()) {
            return !mContact.getCapabilities().getSchedule().isNowInRange();
        }

        return false;
    }

    private void showSchedule() {
        if (DEBUG) {
            Log.d(LOG_TAG, "showSchedule");
        }

        String message;

        if (mContact != null && mContact.getCapabilities().getSchedule() != null) {
            Schedule schedule = mContact.getCapabilities().getSchedule();
            if (schedule != null && !schedule.getTimeRanges().isEmpty()) {
                DateTimeRange dateTimeRange = (DateTimeRange) schedule.getTimeRanges().get(0);
                DateTime start = dateTimeRange.start;
                DateTime end = dateTimeRange.end;

                if (start.date.equals(end.date)) {
                    message = String.format(getString(R.string.show_call_activity_schedule_from_to), start.formatDate(), start.formatTime(this), end.formatTime(this));
                } else {
                    message = String.format("%1$s %2$s", start.formatDateTime(this), end.formatDateTime(this));
                }
            } else {
                message = getString(R.string.show_call_activity_schedule_message);
            }

            showAlertMessageView(R.id.show_contact_activity_layout, getString(R.string.show_call_activity_schedule_call), message, true, null);
        }
    }

    public void updateInCall() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateInCall");
        }

        if (getTwinmeApplication().inCallInfo() != null || (mContact != null && (!mContact.getCapabilities().hasAudio() || !mContact.hasPrivatePeer())) || hasSchedule()) {
            mAudioClickableView.setAlpha(0.5f);
        } else {
            mAudioClickableView.setAlpha(1f);
        }

        if (getTwinmeApplication().inCallInfo() != null || (mContact != null && (!mContact.getCapabilities().hasVideo() || !mContact.hasPrivatePeer())) || hasSchedule()) {
            mVideoClickableView.setAlpha(0.5f);
        } else {
            mVideoClickableView.setAlpha(1f);
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

    private void showCertifyOnboarding() {
        if (DEBUG) {
            Log.d(LOG_TAG, "showCertifyOnboarding");
        }

        ViewGroup viewGroup = findViewById(R.id.show_contact_activity_layout);

        OnboardingConfirmView onboardingConfirmView = new OnboardingConfirmView(this, null);

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(getString(R.string.authentified_relation_activity_to_be_certified_title));
        spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_DEFAULT), 0, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.append("\n\n");
        int startSubTitle = spannableStringBuilder.length();
        spannableStringBuilder.append(getString(R.string.authentified_relation_activity_onboarding_subtitle));
        spannableStringBuilder.setSpan(new RelativeSizeSpan(0.94f), startSubTitle, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_GREY), startSubTitle, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        onboardingConfirmView.setSpannableTitle(spannableStringBuilder);

        boolean darkMode = false;
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        int displayMode = getTwinmeApplication().displayMode();
        if ((currentNightMode == Configuration.UI_MODE_NIGHT_YES && displayMode == DisplayMode.SYSTEM.ordinal())  || displayMode == DisplayMode.DARK.ordinal()) {
            darkMode = true;
        }

        onboardingConfirmView.setImage(ResourcesCompat.getDrawable(getResources(), darkMode ? R.drawable.onboarding_authentified_relation_dark : R.drawable.onboarding_authentified_relation, null));

        String message = getString(R.string.authentified_relation_activity_onboarding_message) + "\n\n" + getString(R.string.call_activity_certify_onboarding_message);

        onboardingConfirmView.setMessage(message);
        onboardingConfirmView.setConfirmTitle(getString(R.string.authentified_relation_activity_start));
        onboardingConfirmView.setCancelTitle(getString(R.string.application_do_not_display));

        AbstractBottomSheetView.Observer observer = new AbstractBottomSheetView.Observer() {
            @Override
            public void onConfirmClick() {
                onboardingConfirmView.animationCloseConfirmView();
                onVideoClick(true);
            }

            @Override
            public void onCancelClick() {
                getTwinmeApplication().setShowOnboardingType(TwinmeApplication.OnboardingType.CERTIFIED_RELATION, false);
                onVideoClick(true);
                onboardingConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onDismissClick() {
                onboardingConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                viewGroup.removeView(onboardingConfirmView);
                setFullscreen();
            }
        };

        onboardingConfirmView.setObserver(observer);
        viewGroup.addView(onboardingConfirmView);
        onboardingConfirmView.show();

        Window window = getWindow();
        window.setNavigationBarColor(Design.POPUP_BACKGROUND_COLOR);
    }

    public void showControlCameraOnboarding() {
        if (DEBUG) {
            Log.d(LOG_TAG, "showControlCameraOnboarding");
        }

        ViewGroup viewGroup = findViewById(R.id.show_contact_activity_layout);

        OnboardingConfirmView onboardingConfirmView = new OnboardingConfirmView(this, null);
        onboardingConfirmView.setImage(ResourcesCompat.getDrawable(getResources(), R.drawable.onboarding_control_camera, null));
        onboardingConfirmView.setTitle(getString(R.string.call_activity_camera_control_needs_help));
        onboardingConfirmView.setMessage(getString(R.string.contact_capabilities_activity_camera_control_onboarding));
        onboardingConfirmView.setConfirmTitle(getString(R.string.application_ok));
        onboardingConfirmView.hideCancelView();

        AbstractBottomSheetView.Observer observer = new AbstractBottomSheetView.Observer() {
            @Override
            public void onConfirmClick() {
                onboardingConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCancelClick() {
                onboardingConfirmView.animationCloseConfirmView();
                getTwinmeApplication().setShowOnboardingType(TwinmeApplication.OnboardingType.REMOTE_CAMERA_SETTING, false);
            }

            @Override
            public void onDismissClick() {
                onboardingConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                viewGroup.removeView(onboardingConfirmView);

                setFullscreen();
            }
        };

        onboardingConfirmView.setObserver(observer);
        viewGroup.addView(onboardingConfirmView);
        onboardingConfirmView.show();

        Window window = getWindow();
        window.setNavigationBarColor(Design.POPUP_BACKGROUND_COLOR);
    }

    @Override
    public void setupDesign() {
        if (DEBUG) {
            Log.d(LOG_TAG, "setupDesign");
        }

        AVATAR_OVER_SIZE = (int) (Design.AVATAR_OVER_WIDTH * Design.WIDTH_RATIO);
        AVATAR_MAX_SIZE = Design.DISPLAY_WIDTH + (AVATAR_OVER_SIZE * 2);
    }

    @Override
    public void updateFont() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateFont");
        }

        super.updateFont();

        if (!mUIInitialized) {
            return;
        }

        Design.updateTextFont(mNameView, Design.FONT_BOLD44);
        Design.updateTextFont(mDescriptionView, Design.FONT_REGULAR32);
        Design.updateTextFont(mChatTextView, Design.FONT_REGULAR28);
        Design.updateTextFont(mVideoTextView, Design.FONT_REGULAR28);
        Design.updateTextFont(mAudioTextView, Design.FONT_REGULAR28);
        Design.updateTextFont(mIdentityTitleView, Design.FONT_BOLD26);
        Design.updateTextFont(mIdentityTextView, Design.FONT_REGULAR34);
        Design.updateTextFont(mCertifiedTextView, Design.FONT_REGULAR34);
        Design.updateTextFont(mSettingsTitleView, Design.FONT_BOLD26);
        Design.updateTextFont(mSettingsTextView, Design.FONT_REGULAR34);
        Design.updateTextFont(mLastCallsTitleView, Design.FONT_BOLD26);
        Design.updateTextFont(mLastCallsTextView, Design.FONT_REGULAR34);
        Design.updateTextFont(mConversationsTitleView, Design.FONT_BOLD26);
        Design.updateTextFont(mFilesTextView, Design.FONT_REGULAR34);
        Design.updateTextFont(mExportTextView, Design.FONT_REGULAR34);
        Design.updateTextFont(mCleanTextView, Design.FONT_REGULAR34);
        Design.updateTextFont(mFallbackTextView, Design.FONT_REGULAR34);
        Design.updateTextFont(mRemoveTextView, Design.FONT_REGULAR34);
    }

    @Override
    public void updateColor() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateColor");
        }

        super.updateColor();

        if (!mUIInitialized) {
            return;
        }

        mNameView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mDescriptionView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mRoundedChatView.setColor(Design.CHAT_COLOR);
        mChatTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mRoundedVideoView.setColor(Design.VIDEO_CALL_COLOR);
        mVideoTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mRoundedAudioView.setColor(Design.AUDIO_CALL_COLOR);
        mAudioTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mIdentityTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mIdentityTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mCertifiedTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mCertifiedImageView.setColorFilter(Design.SHOW_ICON_COLOR);
        mSettingsTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mSettingsTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mLastCallsTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mLastCallsTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mConversationsTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mFilesTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mFilesImageView.setColorFilter(Design.SHOW_ICON_COLOR);
        mExportTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mExportImageView.setColorFilter(Design.SHOW_ICON_COLOR);
        mCleanTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mCleanImageView.setColorFilter(Design.SHOW_ICON_COLOR);
        mFallbackView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        mFallbackTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mRemoveTextView.setTextColor(Color.RED);
    }
}
