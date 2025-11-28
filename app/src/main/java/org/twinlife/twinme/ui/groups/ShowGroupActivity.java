/*
 *  Copyright (c) 2018-2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.groups;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinlife.ConversationService.GroupConversation;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Group;
import org.twinlife.twinme.models.GroupMember;
import org.twinlife.twinme.services.GroupService;
import org.twinlife.twinme.skin.CircularImageDescriptor;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.EditIdentityActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.LastCallsActivity;
import org.twinlife.twinme.ui.Settings;
import org.twinlife.twinme.ui.cleanupActivity.TypeCleanUpActivity;
import org.twinlife.twinme.ui.conversationActivity.ConversationActivity;
import org.twinlife.twinme.ui.conversationFilesActivity.ConversationFilesActivity;
import org.twinlife.twinme.ui.exportActivity.ExportActivity;
import org.twinlife.twinme.ui.premiumServicesActivity.PremiumFeatureConfirmView;
import org.twinlife.twinme.ui.premiumServicesActivity.UIPremiumFeature;
import org.twinlife.twinme.ui.users.UIContact;
import org.twinlife.twinme.utils.AbstractConfirmView;
import org.twinlife.twinme.utils.CircularImageView;
import org.twinlife.twinme.utils.RoundedView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Activity controller to display information about the group.
 */

public class ShowGroupActivity extends AbstractGroupActivity {
    private static final String LOG_TAG = "ShowGroupActivity";
    private static final boolean DEBUG = false;

    private static final int ADD_MEMBERS = 1;

    private static final int DESIGN_MEMBER_VIEW_TOP_MARGIN = 40;
    private static final int DESIGN_DESCRIPTION_VIEW_TOP_MARGIN = 180;
    private static int AVATAR_OVER_SIZE;
    private static int AVATAR_MAX_SIZE;


    private UUID mGroupId;
    private View mBackClickableView;
    private ImageView mAvatarView;
    private ImageView mNoAvatarView;
    private View mContentView;
    private TextView mNameView;
    private TextView mDescriptionView;
    private TextView mMemberTextView;
    private TextView mInviteTextView;
    private View mMemberListSummaryView;
    private ShowGroupMemberListAdapter mMemberListAdapter;
    private TextView mIdentityTextView;
    private CircularImageView mIdentityAvatarView;
    private View mChatClickableView;
    private View mAudioClickableView;
    private View mVideoClickableView;
    private TextView mConfigurationTitleView;
    private View mPermissionsView;
    private View mCallSettingsView;
    private View mFallbackView;
    private TextView mFallbackTextView;
    private View mScrollView;
    private RoundedView mRoundedChatView;
    private TextView mChatTextView;
    private RoundedView mRoundedVideoView;
    private TextView mVideoTextView;
    private RoundedView mRoundedAudioView;
    private TextView mAudioTextView;
    private TextView mIdentityTitleView;
    private TextView mPermissionsTextView;
    private ImageView mPermissionsImageView;
    private TextView mCallSettingsTextView;
    private ImageView mCallSettingsImageView;
    private TextView mLastCallsTitleView;
    private TextView mLastCallsTextView;
    private TextView mConversationsTitleView;
    private TextView mFilesTextView;
    private ImageView mFilesImageView;
    private TextView mExportTextView;
    private ImageView mExportImageView;
    private TextView mCleanTextView;
    private ImageView mCleanImageView;

    private boolean mUIInitialized = false;
    private boolean mUIPostInitialized = false;
    private GroupService mGroupService;
    @Nullable
    private Group mGroup;
    private GroupConversation mGroupConversation;
    private List<UIContact> mGroupMembers = new ArrayList<>();
    private Bitmap mGroupAvatar;
    private String mIdentityName;
    private Bitmap mIdentityAvatar;
    private boolean mDeletedGroup = false;
    private boolean mCanInvite = false;
    @Nullable
    private String mContactsToAdd;

    private float mAvatarLastSize = -1;
    private float mScrollPosition = -1;

    private boolean mInitScrollView = false;

    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        mGroupService = new GroupService(this, getTwinmeContext(), this);

        setFullscreen();

        Intent intent = getIntent();
        String value = intent.getStringExtra(Intents.INTENT_GROUP_ID);
        if (value != null) {
            mGroupId = UUID.fromString(value);
        }

        initViews();
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

        if (mDeletedGroup) {
            finish();
        } else {
            if (mGroupId != null) {
                mGroupService.getGroup(mGroupId, true);
            }
        }
    }

    //
    // Override Activity methods
    //

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        mGroupService.dispose();

        super.onDestroy();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onWindowFocusChanged: hasFocus=" + hasFocus);
        }

        if (hasFocus && mUIInitialized && !mUIPostInitialized) {
            postInitViews();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onActivityResult requestCode=" + requestCode + " resultCode=" + resultCode + " data=" + data);
        }

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_MEMBERS) {
            // Remember the contacts to invite, get a fresh list and send the invitation in onGetContacts.
            mContactsToAdd = data != null ? data.getStringExtra(Intents.INTENT_CONTACT_SELECTION) : null;
            if (mContactsToAdd != null) {
                mGroupService.getContacts();
            }
        }
    }

    //
    // Implement ShowGroupService.Observer methods
    //

    @Override
    public void onGetGroup(@NonNull Group group, @NonNull List<GroupMember> groupMembers, @NonNull GroupConversation conversation) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetGroup group=" + group);
        }

        mGroup = group;
        mGroupConversation = conversation;
        if (group.hasPeer()) {
            mMemberTextView.setVisibility(View.VISIBLE);
            mMemberListSummaryView.setVisibility(View.VISIBLE);
            mIdentityName = group.getIdentityName();
            if (mIdentityName == null) {
                mIdentityName = getTwinmeApplication().getAnonymousName();
            }
            mCanInvite = conversation.hasPermission(ConversationService.Permission.INVITE_MEMBER) && conversation.getState() == GroupConversation.State.JOINED;
        } else {
            mFallbackView.setVisibility(View.VISIBLE);
            mBackClickableView.setVisibility(View.GONE);
            mContentView.setVisibility(View.GONE);
            mScrollView.setVisibility(View.GONE);
            mAvatarView.setVisibility(View.GONE);

            ViewGroup.LayoutParams avatarLayoutParams = mAvatarView.getLayoutParams();
            avatarLayoutParams.width = Design.DISPLAY_WIDTH;
            //noinspection SuspiciousNameCombination
            avatarLayoutParams.height = Design.DISPLAY_WIDTH;
            mAvatarView.requestLayout();

            setStatusBarColor();
            showToolBar(true);
            showBackButton(true);

            mGroupAvatar = getTwinmeApplication().getDefaultGroupAvatar();
            mIdentityName = getTwinmeApplication().getAnonymousName();
            mIdentityAvatar = getTwinmeApplication().getAnonymousAvatar();
            mCanInvite = false;
        }

        if (group.hasPeer()) {
            mGroupService.getIdentityImage(group, (Bitmap identityAvatar) -> {
                mIdentityAvatar = identityAvatar;
                if (mIdentityAvatar == null) {
                    mIdentityAvatar = getTwinmeApplication().getAnonymousAvatar();
                }

                if (mGroupAvatar == null) {
                    mGroupService.getImage(group, (Bitmap groupAvatar) -> {
                        mGroupAvatar = groupAvatar;
                        onGetGroupMembers(group, groupMembers);
                    });
                } else {
                    onGetGroupMembers(group, groupMembers);
                }
            });
        } else {
            onGetGroupMembers(group, groupMembers);
        }
    }

    private void onGetGroupMembers(@NonNull Group group, @NonNull List<GroupMember> groupMembers) {
        GroupMember currentMember = group.getCurrentMember();
        mGroupService.getGroupMemberImage(currentMember, (Bitmap currentMemberAvatar) -> {
            mMemberListAdapter.updateUIMember(currentMember, currentMemberAvatar);

            if (!groupMembers.isEmpty()) {
                AtomicInteger avatarCounter = new AtomicInteger(groupMembers.size());
                for (GroupMember member : groupMembers) {
                    mGroupService.getGroupMemberImage(member, (Bitmap memberAvatar) -> {
                        mMemberListAdapter.updateUIMember(member, memberAvatar);

                        if (avatarCounter.decrementAndGet() == 0) {
                            mGroupMembers = mMemberListAdapter.getMembers();
                            mMemberListAdapter.notifyDataSetChanged();
                            updateGroup();
                        }
                    });
                }
            } else {
                mGroupMembers = mMemberListAdapter.getMembers();
                mMemberListAdapter.notifyDataSetChanged();
                updateGroup();
            }
        });
    }

    @Override
    public void onGetContacts(@NonNull List<Contact> contacts) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContacts contacts=" + contacts);
        }

        // getContacts was triggered after selecting, send invitation.
        if (mContactsToAdd != null) {
            List<Contact> contactsToInvite = AddGroupMemberActivity.fromIntentString(contacts, mContactsToAdd);
            mGroupService.inviteContacts(contactsToInvite);
            mContactsToAdd = null;
        }
    }

    @Override
    public void onGetGroupNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onErrorGroupNotFound");
        }

        mFallbackView.setVisibility(View.VISIBLE);
        mBackClickableView.setVisibility(View.GONE);
    }

    @Override
    public void onUpdateGroup(@NonNull Group group, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateGroup group=" + group);
        }

        // updateGroup() operation terminates after a leave action.
        mGroup = group;
        if (group.isLeaving()) {
            if (mResumed) {
                finish();
                return;
            }
        }

        mGroupAvatar = avatar;
        mIdentityName = group.getIdentityName();
        if (mIdentityName == null) {
            mIdentityName = getTwinmeApplication().getAnonymousName();
        }
        mGroupService.getIdentityImage(group, (Bitmap identityAvatar) -> {
            mIdentityAvatar = identityAvatar;
            if (mIdentityAvatar == null) {
                mIdentityAvatar = getTwinmeApplication().getAnonymousAvatar();
            }

            updateGroup();
        });
    }

    @Override
    public void onDeleteGroup(UUID groupId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteGroup groupId=" + groupId);
        }

        if (!groupId.equals(mGroupId)) {

            return;
        }

        mDeletedGroup = true;

        if (mResumed) {
            finish();
        }
    }

    @Override
    public void onLeaveGroup(@NonNull Group group, @NonNull UUID memberTwincodeId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onLeaveGroup group=" + group + " memberTwincodeId=" + memberTwincodeId);
        }

        if (mGroup != null && mGroup.getMemberTwincodeOutboundId().equals(memberTwincodeId)) {
            finish();
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
        setContentView(R.layout.show_group_activity);

        setToolBar(R.id.show_group_activity_tool_bar);
        showToolBar(false);
        showBackButton(true);
        setTitle(getString(R.string.show_group_activity_title));

        applyInsets(R.id.show_group_activity_layout, -1, -1, Design.WHITE_COLOR, true);

        mFallbackView = findViewById(R.id.show_group_activity_fallback_view);
        mFallbackTextView = findViewById(R.id.show_group_activity_fallback_message_view);

        mAvatarView = findViewById(R.id.show_group_activity_avatar_view);

        ViewGroup.LayoutParams layoutParams = mAvatarView.getLayoutParams();
        layoutParams.width = AVATAR_MAX_SIZE - AVATAR_OVER_SIZE;
        layoutParams.height = AVATAR_MAX_SIZE - AVATAR_OVER_SIZE;

        mNoAvatarView = findViewById(R.id.show_group_activity_no_avatar_view);
        mNoAvatarView.setVisibility(View.GONE);

        mBackClickableView = findViewById(R.id.show_group_activity_back_clickable_view);
        mBackClickableView.setOnClickListener(view -> onBackClick());

        layoutParams = mBackClickableView.getLayoutParams();
        layoutParams.height = Design.BACK_CLICKABLE_VIEW_HEIGHT;

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mBackClickableView.getLayoutParams();
        marginLayoutParams.leftMargin = Design.BACK_CLICKABLE_VIEW_LEFT_MARGIN;
        marginLayoutParams.topMargin = Design.BACK_CLICKABLE_VIEW_TOP_MARGIN;

        RoundedView backRoundedView = findViewById(R.id.show_group_activity_back_rounded_view);
        backRoundedView.setColor(Design.BACK_VIEW_COLOR);

        mContentView = findViewById(R.id.show_group_activity_content_view);
        mContentView.setY(AVATAR_MAX_SIZE - Design.ACTION_VIEW_MIN_MARGIN);

        setBackground(mContentView);

        mScrollView = findViewById(R.id.show_group_activity_content_scroll_view);
        ViewTreeObserver viewTreeObserver = mScrollView.getViewTreeObserver();
        viewTreeObserver.addOnScrollChangedListener(() -> {
            if (mScrollPosition == -1) {
                mScrollPosition = AVATAR_OVER_SIZE;
            }

            float delta = mScrollPosition - mScrollView.getScrollY();
            updateAvatarSize(delta);
            mScrollPosition = mScrollView.getScrollY();
        });

        View slideMarkView = findViewById(R.id.show_group_activity_slide_mark_view);
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

        mNameView = findViewById(R.id.show_group_activity_name_view);
        mDescriptionView = findViewById(R.id.show_group_activity_description_view);

        View headerView = findViewById(R.id.show_group_activity_content_header_view);
        marginLayoutParams = (ViewGroup.MarginLayoutParams) headerView.getLayoutParams();
        marginLayoutParams.topMargin = Design.HEADER_VIEW_TOP_MARGIN;

        View editClickableView = findViewById(R.id.show_group_activity_edit_clickable_view);
        editClickableView.setOnClickListener(view -> onEditClick());

        layoutParams = editClickableView.getLayoutParams();
        layoutParams.height = Design.EDIT_CLICKABLE_VIEW_HEIGHT;

        ImageView editImageView = findViewById(R.id.show_group_activity_edit_image_view);
        editImageView.setColorFilter(Design.getMainStyle());

        View actionView = findViewById(R.id.show_group_activity_action_view);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) actionView.getLayoutParams();
        marginLayoutParams.topMargin = Design.ACTION_VIEW_TOP_MARGIN;

        mChatClickableView = findViewById(R.id.show_group_activity_chat_clickable_view);
        mChatClickableView.setOnClickListener(view -> onChatClick());

        layoutParams = mChatClickableView.getLayoutParams();
        layoutParams.height = Design.ACTION_CLICKABLE_VIEW_HEIGHT;

        mRoundedChatView = findViewById(R.id.show_group_activity_chat_rounded_view);
        mChatTextView = findViewById(R.id.show_group_activity_chat_text_view);

        mVideoClickableView = findViewById(R.id.show_group_activity_video_clickable_view);
        mVideoClickableView.setOnClickListener(view -> onVideoClick());

        layoutParams = mVideoClickableView.getLayoutParams();
        layoutParams.height = Design.ACTION_CLICKABLE_VIEW_HEIGHT;

        mRoundedVideoView = findViewById(R.id.show_group_activity_video_rounded_view);
        mVideoTextView = findViewById(R.id.show_group_activity_video_text_view);

        mAudioClickableView = findViewById(R.id.show_group_activity_audio_clickable_view);
        mAudioClickableView.setOnClickListener(view -> onAudioClick());

        layoutParams = mAudioClickableView.getLayoutParams();
        layoutParams.height = Design.ACTION_CLICKABLE_VIEW_HEIGHT;

        mRoundedAudioView = findViewById(R.id.show_group_activity_audio_rounded_view);
        mAudioTextView = findViewById(R.id.show_group_activity_audio_text_view);

        mMemberTextView = findViewById(R.id.show_group_activity_members_title_view);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mMemberTextView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_MEMBER_VIEW_TOP_MARGIN * Design.HEIGHT_RATIO);

        mInviteTextView = findViewById(R.id.show_group_activity_invite_title_view);
        mInviteTextView.setText(" + " + getResources().getText(R.string.add_group_member_activity_add));

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mInviteTextView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_MEMBER_VIEW_TOP_MARGIN * Design.HEIGHT_RATIO);

        mInviteTextView.setOnClickListener(view -> onInviteMemberClick());

        mMemberListSummaryView = findViewById(R.id.show_group_activity_list_member_layout_view);
        layoutParams = mMemberListSummaryView.getLayoutParams();
        layoutParams.height = Design.ITEM_VIEW_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mMemberListSummaryView.getLayoutParams();
        marginLayoutParams.topMargin = Design.IDENTITY_VIEW_TOP_MARGIN;

        LinearLayoutManager selectedUIContactLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView memberRecyclerView = findViewById(R.id.show_group_activity_list_member_view);
        memberRecyclerView.setLayoutManager(selectedUIContactLinearLayoutManager);
        memberRecyclerView.setItemViewCacheSize(Design.ITEM_LIST_CACHE_SIZE);
        memberRecyclerView.setItemAnimator(null);
        memberRecyclerView.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                onListMembersClick();
            }
            return false;
        });

        mMemberListAdapter = new ShowGroupMemberListAdapter(this, mGroupService, mGroupMembers, Design.DISPLAY_WIDTH);
        memberRecyclerView.setAdapter(mMemberListAdapter);

        mIdentityTitleView = findViewById(R.id.show_group_activity_identity_title_view);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mIdentityTitleView.getLayoutParams();
        marginLayoutParams.topMargin = Design.TITLE_IDENTITY_TOP_MARGIN;

        View identityView = findViewById(R.id.show_group_activity_edit_identity_layout_view);
        layoutParams = identityView.getLayoutParams();
        layoutParams.height = Design.ITEM_VIEW_HEIGHT;
        identityView.setOnClickListener(view -> onEditIdentityClick());

        marginLayoutParams = (ViewGroup.MarginLayoutParams) identityView.getLayoutParams();
        marginLayoutParams.topMargin = Design.IDENTITY_VIEW_TOP_MARGIN;

        mIdentityTextView = findViewById(R.id.show_group_activity_identity_text_view);

        mIdentityAvatarView = findViewById(R.id.show_group_activity_edit_identity_avatar_view);

        mConfigurationTitleView = findViewById(R.id.show_group_activity_configuration_title_view);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mConfigurationTitleView.getLayoutParams();
        marginLayoutParams.topMargin = Design.TITLE_IDENTITY_TOP_MARGIN;

        mPermissionsView = findViewById(R.id.show_group_activity_permissions_view);
        layoutParams = mPermissionsView.getLayoutParams();
        layoutParams.height = Design.ITEM_VIEW_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mPermissionsView.getLayoutParams();
        marginLayoutParams.topMargin = Design.IDENTITY_VIEW_TOP_MARGIN;

        mPermissionsView.setOnClickListener(view -> onPermissionsClick());

        mPermissionsTextView = findViewById(R.id.show_group_activity_permissions_text_view);
        mPermissionsImageView = findViewById(R.id.show_group_activity_permissions_image_view);

        mCallSettingsView = findViewById(R.id.show_group_activity_call_settings_view);
        layoutParams = mCallSettingsView.getLayoutParams();
        layoutParams.height = Design.ITEM_VIEW_HEIGHT;

        mCallSettingsView.setOnClickListener(view -> onCallSettingsClick());

        mCallSettingsTextView = findViewById(R.id.show_group_activity_call_settings_text_view);
        mCallSettingsImageView = findViewById(R.id.show_group_activity_call_settings_image_view);

        mLastCallsTitleView = findViewById(R.id.show_group_activity_last_calls_title_view);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mLastCallsTitleView.getLayoutParams();
        marginLayoutParams.topMargin = Design.TITLE_IDENTITY_TOP_MARGIN;

        View lastCallsView = findViewById(R.id.show_group_activity_last_calls_view);
        layoutParams = lastCallsView.getLayoutParams();
        layoutParams.height = Design.SECTION_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) lastCallsView.getLayoutParams();
        marginLayoutParams.topMargin = Design.IDENTITY_VIEW_TOP_MARGIN;

        lastCallsView.setOnClickListener(view -> onLastCallsClick());

        mLastCallsTextView = findViewById(R.id.show_group_activity_last_calls_text_view);

        mConversationsTitleView = findViewById(R.id.show_group_activity_conversations_title_view);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mConversationsTitleView.getLayoutParams();
        marginLayoutParams.topMargin = Design.TITLE_IDENTITY_TOP_MARGIN;

        View filesView = findViewById(R.id.show_group_activity_files_view);
        layoutParams = filesView.getLayoutParams();
        layoutParams.height = Design.ITEM_VIEW_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) filesView.getLayoutParams();
        marginLayoutParams.topMargin = Design.IDENTITY_VIEW_TOP_MARGIN;

        filesView.setOnClickListener(view -> onConversationFilesClick());

        mFilesTextView = findViewById(R.id.show_group_activity_files_text_view);
        mFilesImageView = findViewById(R.id.show_group_activity_files_image_view);

        View exportView = findViewById(R.id.show_group_activity_export_view);
        layoutParams = exportView.getLayoutParams();
        layoutParams.height = Design.ITEM_VIEW_HEIGHT;

        exportView.setOnClickListener(view -> onExportClick());

        mExportTextView = findViewById(R.id.show_group_activity_export_text_view);
        mExportImageView = findViewById(R.id.show_group_activity_export_image_view);

        View cleanView = findViewById(R.id.show_group_activity_clean_view);
        layoutParams = cleanView.getLayoutParams();
        layoutParams.height = Design.ITEM_VIEW_HEIGHT;

        cleanView.setOnClickListener(view -> onCleanClick());

        mCleanTextView = findViewById(R.id.show_group_activity_clean_text_view);
        mCleanImageView = findViewById(R.id.show_group_activity_clean_image_view);

        mProgressBarView = findViewById(R.id.show_group_activity_progress_bar);

        mUIInitialized = true;

        updateFont();
        updateColor();
    }

    private void postInitViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "postInitViews");
        }

        mUIPostInitialized = true;
    }

    private void updateGroup() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateGroup");
        }

        if (mUIInitialized && mResumed && mGroup != null) {

            mNameView.setText(mGroup.getName());

            if (mGroup.getDescription() != null && !mGroup.getDescription().isEmpty()) {
                mDescriptionView.setText(mGroup.getDescription());
                mDescriptionView.setVisibility(View.VISIBLE);
            } else {
                mDescriptionView.setVisibility(View.GONE);
            }

            if (mGroup.getAvatarId() != null) {
                mNoAvatarView.setVisibility(View.GONE);
                if (mGroupAvatar != null) {
                    mAvatarView.setImageBitmap(mGroupAvatar);
                } else {
                    mAvatarView.setImageBitmap(getTwinmeApplication().getDefaultGroupAvatar());
                }
            } else {
                mNoAvatarView.setVisibility(View.VISIBLE);
                mAvatarView.setBackgroundColor(Design.AVATAR_PLACEHOLDER_COLOR);
            }

            mIdentityTextView.setText(mGroup.getIdentityName());
            mIdentityAvatarView.setImage(this, null, new CircularImageDescriptor(mIdentityAvatar, 0.5f, 0.5f, 0.5f));

            if (mCanInvite) {
                mInviteTextView.setAlpha(1.0f);
            } else {
                mInviteTextView.setAlpha(0.5f);
            }

            if (mGroup.isOwner()) {
                mConfigurationTitleView.setVisibility(View.VISIBLE);
                mPermissionsView.setVisibility(View.VISIBLE);
                mCallSettingsView.setVisibility(View.VISIBLE);
            } else {
                mConfigurationTitleView.setVisibility(View.GONE);
                mPermissionsView.setVisibility(View.GONE);
                mCallSettingsView.setVisibility(View.GONE);
            }

            ViewTreeObserver contentViewTreeObserver = mContentView.getViewTreeObserver();
            contentViewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    ViewTreeObserver viewTreeObserver = mContentView.getViewTreeObserver();
                    viewTreeObserver.removeOnGlobalLayoutListener(this);

                    View cleanView = findViewById(R.id.show_group_activity_clean_view);

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

            updateInCall();
        }
    }

    private void onChatClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onChatClick");
        }

        if (mGroupId != null && mGroup != null && mGroupMembers.size() > 1) {
            startActivity(ConversationActivity.class, Intents.INTENT_GROUP_ID, mGroupId);
        }
    }

    private void onVideoClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onVideoClick");
        }

        if (mGroup == null || mGroupId == null) {
            return;
        }

        showPremiumFeatureView();
    }

    private void onAudioClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onAudioClick");
        }

        if (mGroup == null || mGroupId == null) {
            return;
        }

        showPremiumFeatureView();
    }

    private void onListMembersClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewMemberClick");
        }

        if (mGroupConversation != null && mGroupConversation.getState() == GroupConversation.State.JOINED) {
            startActivity(GroupMemberActivity.class, Intents.INTENT_GROUP_ID, mGroupId);
        }
    }

    private void onInviteMemberClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onInviteMemberClick");
        }

        if (mGroupConversation != null && mGroupConversation.getState() == GroupConversation.State.JOINED) {
            if (mCanInvite) {
                Intent intent = new Intent();
                intent.putExtra(Intents.INTENT_GROUP_ID, mGroupId.toString());
                intent.setClass(this, AddGroupMemberActivity.class);
                startActivityForResult(intent, ADD_MEMBERS);
            } else {
                showAlertMessageView(R.id.show_group_activity_layout, getString(R.string.deleted_account_activity_warning), getString(R.string.group_member_activity_admin_not_authorize), true, null);
            }
        }
    }

    private void onEditIdentityClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onEditIdentityClick");
        }

        startActivity(EditIdentityActivity.class, Intents.INTENT_GROUP_ID, mGroupId);
    }

    private void onPermissionsClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPermissionsClick");
        }

        startActivity(SettingsGroupActivity.class, Intents.INTENT_GROUP_ID, mGroupId);
    }

    private void onCallSettingsClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCallSettingsClick");
        }

        startActivity(GroupCapabilitiesActivity.class, Intents.INTENT_GROUP_ID, mGroupId);
    }

    private void onLastCallsClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onLastCallsClick");
        }

        if (mGroup != null && mGroupId != null) {
            startActivity(LastCallsActivity.class, Intents.INTENT_GROUP_ID, mGroupId);
        }
    }

    private void onExportClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onExportClick");
        }

        if (mGroup != null) {
            startActivity(ExportActivity.class, Intents.INTENT_GROUP_ID, mGroupId);
        }
    }

    private void onCleanClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCleanClick");
        }

        if (mGroup != null) {
            startActivity(TypeCleanUpActivity.class, Intents.INTENT_GROUP_ID, mGroupId);
        }
    }

    private void onConversationFilesClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onConversationFilesClick");
        }

        if (mGroup != null) {
            startActivity(ConversationFilesActivity.class, Intents.INTENT_GROUP_ID, mGroupId);
        }
    }

    private void onEditClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onEditClick");
        }

        if (mGroup != null) {
            startActivity(EditGroupActivity.class, Intents.INTENT_GROUP_ID, mGroupId);
        }
    }

    private void showPremiumFeatureView() {
        if (DEBUG) {
            Log.d(LOG_TAG, "showPremiumFeatureView");
        }

        ViewGroup viewGroup = findViewById(R.id.show_group_activity_layout);

        PremiumFeatureConfirmView premiumFeatureConfirmView = new PremiumFeatureConfirmView(this, null);
        premiumFeatureConfirmView.initWithPremiumFeature(new UIPremiumFeature(this, UIPremiumFeature.FeatureType.GROUP_CALL));

        AbstractConfirmView.Observer observer = new AbstractConfirmView.Observer() {
            @Override
            public void onConfirmClick() {
                premiumFeatureConfirmView.redirectStore();
            }

            @Override
            public void onCancelClick() {
                premiumFeatureConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onDismissClick() {
                premiumFeatureConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                viewGroup.removeView(premiumFeatureConfirmView);
                setFullscreen();
            }
        };
        premiumFeatureConfirmView.setObserver(observer);
        viewGroup.addView(premiumFeatureConfirmView);
        premiumFeatureConfirmView.show();

        Window window = getWindow();
        window.setNavigationBarColor(Design.POPUP_BACKGROUND_COLOR);
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
        Design.updateTextFont(mMemberTextView, Design.FONT_BOLD26);
        Design.updateTextFont(mInviteTextView, Design.FONT_BOLD28);
        Design.updateTextFont(mIdentityTitleView, Design.FONT_BOLD26);
        Design.updateTextFont(mIdentityTextView, Design.FONT_REGULAR34);
        Design.updateTextFont(mConfigurationTitleView, Design.FONT_BOLD26);
        Design.updateTextFont(mPermissionsTextView, Design.FONT_REGULAR34);
        Design.updateTextFont(mCallSettingsTextView, Design.FONT_REGULAR34);
        Design.updateTextFont(mLastCallsTitleView, Design.FONT_BOLD26);
        Design.updateTextFont(mLastCallsTextView, Design.FONT_REGULAR34);
        Design.updateTextFont(mConversationsTitleView, Design.FONT_BOLD26);
        Design.updateTextFont(mFilesTextView, Design.FONT_REGULAR34);
        Design.updateTextFont(mExportTextView, Design.FONT_REGULAR34);
        Design.updateTextFont(mCleanTextView, Design.FONT_REGULAR34);
        Design.updateTextFont(mFallbackTextView, Design.FONT_MEDIUM34);
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
        mMemberTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mInviteTextView.setTextColor(Design.getMainStyle());
        mIdentityTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mIdentityTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mConfigurationTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mPermissionsTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mPermissionsImageView.setColorFilter(Design.SHOW_ICON_COLOR);
        mCallSettingsTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mCallSettingsImageView.setColorFilter(Design.SHOW_ICON_COLOR);
        mLastCallsTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mLastCallsTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mConversationsTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mFilesTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mFilesImageView.setColorFilter(Design.SHOW_ICON_COLOR);
        mExportTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mExportImageView.setColorFilter(Design.SHOW_ICON_COLOR);
        mCleanTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mCleanImageView.setColorFilter(Design.SHOW_ICON_COLOR);
        mFallbackTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
    }

    public void updateInCall() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateInCall");
        }

        if (getTwinmeApplication().inCallInfo() != null || (mGroup != null && (!mGroup.getCapabilities().hasAudio() || mGroupMembers.size() == 1 || mGroupMembers.size() > Settings.MAX_CALL_GROUP_PARTICIPANTS))) {
            mAudioClickableView.setAlpha(0.5f);
        } else {
            mAudioClickableView.setAlpha(1f);
        }

        if (getTwinmeApplication().inCallInfo() != null || (mGroup != null && (!mGroup.getCapabilities().hasVideo() || mGroupMembers.size() == 1 || mGroupMembers.size() > Settings.MAX_CALL_GROUP_PARTICIPANTS))) {
            mVideoClickableView.setAlpha(0.5f);
        } else {
            mVideoClickableView.setAlpha(1f);
        }

        if (mGroup != null && mGroupMembers.size() == 1) {
            mChatClickableView.setAlpha(0.5f);
        } else {
            mChatClickableView.setAlpha(1f);
        }
    }

    @Override
    public void setupDesign() {
        if (DEBUG) {
            Log.d(LOG_TAG, "setupDesign");
        }

        AVATAR_OVER_SIZE = Design.AVATAR_OVER_WIDTH;
        AVATAR_MAX_SIZE = Design.DISPLAY_WIDTH + (Design.AVATAR_OVER_WIDTH * 2);
    }
}
