/*
 *  Copyright (c) 2018-2025 twinlife SA.
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
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.View;
import android.view.ViewGroup;
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
import org.twinlife.twinme.models.Invitation;
import org.twinlife.twinme.models.Space;
import org.twinlife.twinme.services.GroupService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractEditActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.profiles.MenuPhotoView;
import org.twinlife.twinme.utils.EditableView;
import org.twinlife.twinme.utils.RoundedView;
import org.twinlife.twinme.utils.UIMenuSelectAction;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Activity controller for the group creation.
 * <p>
 * The controller allows to choose:
 * <p>
 * - the group name,
 * - the group picture,
 * - the group settings (allow invitation, allow posts),
 * - a list of contacts to invite.
 * <p>
 * After the group is created and selected members invited, the user is redirected to the ShowGroupActivity.
 */

public class CreateGroupActivity extends AbstractEditActivity implements GroupService.Observer {
    private static final String LOG_TAG = "CreateGroupActivity";
    private static final boolean DEBUG = false;

    private static final int ADD_MEMBERS = 1;
    private static final int SETTINGS = 2;
    private static final String SELECTED_MEMBERS = "org.twinlife.device.android.twinme.CreateGroupActivity.SelectedMembers";

    private static final int DESIGN_MEMBER_VIEW_TOP_MARGIN = 40;

    private EditableView mEditableView;
    private ImageView mNoAvatarView;
    private TextView mInviteTextView;
    private TextView mNoMemberTextView;
    private TextView mTitleView;
    private ImageView mNoMemberImageView;
    private RecyclerView mMemberRecyclerView;
    private TextView mMemberTextView;
    private TextView mConfigurationTitleView;
    private TextView mPermissionsTextView;
    private TextView mSaveTextView;

    private ShowGroupMemberListAdapter mMemberListAdapter;
    private boolean mCanCreate = false;
    private boolean mCreateClicked = false;
    private boolean mUIInitialized = false;
    private boolean mUIPostInitialized = false;
    private boolean mGroupCreated = false;
    private String mName;
    private Bitmap mAvatar;
    private File mAvatarFile;
    private boolean mAllowInvitation = true;
    private boolean mAllowPostMessage = true;
    private boolean mAllowInviteMemberAsContact = true;
    @Nullable
    private List<Contact> mContacts;
    private GroupService mGroupService;
    private String mSelectedMembers;

    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            if (mEditableView != null) {
                mEditableView.onCreate(savedInstanceState);
                updateSelectedImage();
            }
            mSelectedMembers = savedInstanceState.getString(SELECTED_MEMBERS);
            updateViews();
        }
        if (mSelectedMembers == null) {
            mSelectedMembers = "";
        }

        mGroupService = new GroupService(this, getTwinmeContext(), this);

        initViews();
    }

    @Override
    protected void onPause() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPause");
        }

        super.onPause();

        hideKeyboard();
    }

    @Override
    protected void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        super.onResume();
        mGroupService.getContacts();
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

        // Cleanup capture and cropped images.
        if (mEditableView != null) {
            mEditableView.onDestroy();
        }

        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSaveInstanceState: outState=" + outState);
        }

        super.onSaveInstanceState(outState);

        if (mEditableView != null) {
            mEditableView.onSaveInstanceState(outState);
        }
        outState.putString(SELECTED_MEMBERS, mSelectedMembers);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onActivityResult requestCode=" + requestCode + " resultCode=" + resultCode + " data=" + data);
        }

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_MEMBERS) {
            String selection = data != null ? data.getStringExtra(Intents.INTENT_CONTACT_SELECTION) : null;
            if (selection != null) {
                // Keep the selected members as a String because when the activity is restored, we don't know the contacts.
                mSelectedMembers = selection;
                updateViews();
                updateMembers();
            }
        } else if (requestCode == SETTINGS) {
            mAllowInvitation = data == null || data.getBooleanExtra(Intents.INTENT_GROUP_ALLOW_INVITATION, true);
            mAllowPostMessage = data == null || data.getBooleanExtra(Intents.INTENT_GROUP_ALLOW_MESSAGE, true);
            mAllowInviteMemberAsContact = data == null || data.getBooleanExtra(Intents.INTENT_GROUP_INVITE_MEMBER_AS_CONTACT, true);
        } else {
            if (mEditableView != null) {
                Uri image = mEditableView.onActivityResult(requestCode, resultCode, data);
                if (image != null) {
                    updateSelectedImage();
                }
            }
        }
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

    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    public void onRequestPermissions(@NonNull Permission[] grantedPermissions) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRequestPermissions grantedPermissions=" + Arrays.toString(grantedPermissions));
        }

        if (!mEditableView.onRequestPermissions(grantedPermissions)) {
            message(getString(R.string.application_denied_permissions), 0L, new DefaultMessageCallback(R.string.application_ok) {
            });
        }
    }

    /**
     * The GroupService has finished the creation of the group, redirect to the show group activity.
     *
     * @param group        the group that was created.
     * @param conversation the group conversation.
     */
    @Override
    public void onCreateGroup(@NonNull Group group, @NonNull GroupConversation conversation) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateGroup group=" + group + " conversation=" + conversation);
        }

        if (mGroupCreated) {
            return;
        }

        mGroupCreated = true;

        Intent data = new Intent();
        setResult(RESULT_OK, data);

        final Intent intent = new Intent(this, ShowGroupActivity.class);
        intent.putExtra(Intents.INTENT_GROUP_ID, group.getId().toString());
        startActivity(intent);

        finish();
    }

    @Override
    public void onGetGroup(@NonNull Group group, @NonNull List<GroupMember> groupMembers, @NonNull GroupConversation conversation) {

    }

    @Override
    public void onUpdateGroup(@NonNull Group group, @Nullable Bitmap avatar) {

    }

    @Override
    public void onInviteGroup(@NonNull ConversationService.Conversation conversation, @NonNull ConversationService.InvitationDescriptor invitationDescriptor) {

    }

    @Override
    public void onLeaveGroup(@NonNull Group group, @NonNull UUID memberTwincodeId) {

    }

    @Override
    public void onDeleteGroup(UUID groupId) {

    }

    @Override
    public void onCreateInvitation(@NonNull Invitation invitation) {

    }

    @Override
    public void onGetCurrentSpace(@NonNull Space space) {

    }

    @Override
    public void onGetGroupNotFound() {

    }

    @Override
    public void onErrorLimitReached() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onErrorLimitReached");
        }

        error(String.format(getString(R.string.application_group_limit_reached), ConversationService.MAX_GROUP_MEMBERS), () -> {
            hideProgressIndicator();
            finish();
        });
    }

    @Override
    public void onGetContact(@NonNull Contact contact, @Nullable Bitmap avatar) {

    }

    @Override
    public void onGetContactNotFound() {

    }

    @Override
    public void onUpdateContact(@NonNull Contact contact, @Nullable Bitmap avatar) {

    }

    /**
     * Get the list of contacts.
     *
     * @param contacts the list of contacts.
     */
    @Override
    public void onGetContacts(@NonNull List<Contact> contacts) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContacts contacts=" + contacts);
        }

        mContacts = contacts;
        updateMembers();
    }

    //
    // Private methods
    //

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.create_group_activity);

        showToolBar(true);
        showBackButton(true);

        setTitle(getString(R.string.application_profile));

        mEditableView = new EditableView(this);

        mAvatarView = findViewById(R.id.create_group_activity_avatar_view);
        mAvatarView.setBackgroundColor(Design.AVATAR_PLACEHOLDER_COLOR);

        mAvatarView.setOnClickListener(v -> openMenuPhoto());

        ViewGroup.LayoutParams layoutParams = mAvatarView.getLayoutParams();
        layoutParams.width = Design.AVATAR_MAX_WIDTH;
        layoutParams.height = Design.AVATAR_MAX_HEIGHT;

        mNoAvatarView = findViewById(R.id.create_group_activity_no_avatar_view);
        mNoAvatarView.setVisibility(View.VISIBLE);

        mNoAvatarView.setOnClickListener(v -> openMenuPhoto());

        View backClickableView = findViewById(R.id.create_group_activity_back_clickable_view);
        GestureDetector backGestureDetector = new GestureDetector(this, new ViewTapGestureDetector(ACTION_BACK));
        backClickableView.setOnTouchListener((v, motionEvent) -> {
            backGestureDetector.onTouchEvent(motionEvent);
            touchContent(motionEvent);
            return true;
        });

        layoutParams = backClickableView.getLayoutParams();
        layoutParams.height = Design.BACK_CLICKABLE_VIEW_HEIGHT;

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) backClickableView.getLayoutParams();
        marginLayoutParams.leftMargin = Design.BACK_CLICKABLE_VIEW_LEFT_MARGIN;
        marginLayoutParams.topMargin = Design.BACK_CLICKABLE_VIEW_TOP_MARGIN;

        RoundedView backRoundedView = findViewById(R.id.create_group_activity_back_rounded_view);
        backRoundedView.setColor(Design.BACK_VIEW_COLOR);

        mContentView = findViewById(R.id.create_group_activity_content_view);
        mContentView.setY(Design.CONTENT_VIEW_INITIAL_POSITION);

        setBackground(mContentView);

        View slideMarkView = findViewById(R.id.create_group_activity_slide_mark_view);
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

        mContentView.setOnTouchListener((v, motionEvent) -> touchContent(motionEvent));

        mTitleView = findViewById(R.id.create_group_activity_title_view);
        Design.updateTextFont(mTitleView, Design.FONT_BOLD44);
        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        View headerView = findViewById(R.id.create_group_activity_content_header_view);
        marginLayoutParams = (ViewGroup.MarginLayoutParams) headerView.getLayoutParams();
        marginLayoutParams.topMargin = Design.HEADER_VIEW_TOP_MARGIN;

        View nameContentView = findViewById(R.id.create_group_activity_name_content_view);

        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable nameViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        nameViewBackground.getPaint().setColor(Design.EDIT_TEXT_BACKGROUND_COLOR);
        nameContentView.setBackground(nameViewBackground);

        layoutParams = nameContentView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = Design.BUTTON_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) nameContentView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_NAME_TOP_MARGIN * Design.HEIGHT_RATIO);

        mNameView = findViewById(R.id.create_group_activity_name_view);
        Design.updateTextFont(mNameView, Design.FONT_REGULAR28);
        mNameView.setTextColor(Design.EDIT_TEXT_TEXT_COLOR);
        mNameView.setHintTextColor(Design.GREY_COLOR);
        mNameView.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_NAME_LENGTH)});
        mNameView.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {

                mCounterNameView.setText(String.format(Locale.getDefault(), "%d/%d", s.length(), MAX_NAME_LENGTH));
                updateGroup();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        GestureDetector nameGestureDetector = new GestureDetector(this, new ViewTapGestureDetector(ACTION_EDIT_NAME));
        mNameView.setOnTouchListener((v, motionEvent) -> {
            boolean result = nameGestureDetector.onTouchEvent(motionEvent);
            touchContent(motionEvent);
            return result;
        });

        mCounterNameView = findViewById(R.id.create_group_activity_counter_name_view);
        Design.updateTextFont(mCounterNameView, Design.FONT_REGULAR26);
        mCounterNameView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mCounterNameView.setText(String.format(Locale.getDefault(), "0/%d", MAX_NAME_LENGTH));

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mCounterNameView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_COUNTER_TOP_MARGIN * Design.HEIGHT_RATIO);

        View descriptionContentView = findViewById(R.id.create_group_activity_description_content_view);

        ShapeDrawable descriptionContentViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        descriptionContentViewBackground.getPaint().setColor(Design.EDIT_TEXT_BACKGROUND_COLOR);
        descriptionContentView.setBackground(descriptionContentViewBackground);

        layoutParams = descriptionContentView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = (int) Design.DESCRIPTION_CONTENT_VIEW_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) descriptionContentView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_DESCRIPTION_TOP_MARGIN * Design.HEIGHT_RATIO);

        mDescriptionView = findViewById(R.id.create_group_activity_description_view);
        Design.updateTextFont(mDescriptionView, Design.FONT_REGULAR28);
        mDescriptionView.setTextColor(Design.EDIT_TEXT_TEXT_COLOR);
        mDescriptionView.setHintTextColor(Design.GREY_COLOR);
        mDescriptionView.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_DESCRIPTION_LENGTH)});
        mDescriptionView.addTextChangedListener(new TextWatcher() {

            @SuppressLint("DefaultLocale")
            @Override
            public void afterTextChanged(Editable s) {

                mCounterDescriptionView.setText(String.format(Locale.getDefault(), "%d/%d", s.length(), MAX_DESCRIPTION_LENGTH));
                updateGroup();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        GestureDetector descriptionGestureDetector = new GestureDetector(this, new ViewTapGestureDetector(ACTION_EDIT_DESCRIPTION));
        mDescriptionView.setOnTouchListener((v, motionEvent) -> {
            boolean result = descriptionGestureDetector.onTouchEvent(motionEvent);
            touchContent(motionEvent);
            return result;
        });

        mCounterDescriptionView = findViewById(R.id.create_group_activity_counter_description_view);
        Design.updateTextFont(mCounterDescriptionView, Design.FONT_REGULAR26);
        mCounterDescriptionView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mCounterDescriptionView.setText(String.format(Locale.getDefault(), "0/%d", MAX_DESCRIPTION_LENGTH));

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mCounterDescriptionView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_COUNTER_TOP_MARGIN * Design.HEIGHT_RATIO);

        mMemberTextView = findViewById(R.id.create_group_activity_members_title_view);
        Design.updateTextFont(mMemberTextView, Design.FONT_BOLD26);
        mMemberTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mMemberTextView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_MEMBER_VIEW_TOP_MARGIN * Design.HEIGHT_RATIO);

        mInviteTextView = findViewById(R.id.create_group_activity_invite_title_view);
        Design.updateTextFont(mInviteTextView, Design.FONT_BOLD28);
        mInviteTextView.setTextColor(Design.getMainStyle());
        mInviteTextView.setText(" + " + getResources().getText(R.string.add_group_member_activity_add));

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mInviteTextView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_MEMBER_VIEW_TOP_MARGIN * Design.HEIGHT_RATIO);

        mInviteTextView.setOnClickListener(view -> onAddMemberClick());

        mNoMemberTextView = findViewById(R.id.create_group_activity_no_member_text_view);
        Design.updateTextFont(mNoMemberTextView, Design.FONT_REGULAR34);
        mNoMemberTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mNoMemberImageView = findViewById(R.id.create_group_activity_no_member_accessory_view);
        mNoMemberImageView.setColorFilter(Design.SHOW_ICON_COLOR);

        View memberView = findViewById(R.id.create_group_activity_list_member_layout_view);
        layoutParams = memberView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = Design.ITEM_VIEW_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) memberView.getLayoutParams();
        marginLayoutParams.topMargin = Design.IDENTITY_VIEW_TOP_MARGIN;

        LinearLayoutManager selectedUIContactLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mMemberRecyclerView = findViewById(R.id.create_group_activity_list_member_view);
        mMemberRecyclerView.setLayoutManager(selectedUIContactLinearLayoutManager);
        mMemberRecyclerView.setItemViewCacheSize(Design.ITEM_LIST_CACHE_SIZE);
        mMemberRecyclerView.setItemAnimator(null);

        mMemberListAdapter = new ShowGroupMemberListAdapter(this, mGroupService, new ArrayList<>(), Design.BUTTON_WIDTH);
        mMemberRecyclerView.setAdapter(mMemberListAdapter);

        GestureDetector memberGestureDetector = new GestureDetector(this, new ViewTapGestureDetector(ACTION_ADD_MEMBERS));
        memberView.setOnTouchListener((v, motionEvent) -> {
            memberGestureDetector.onTouchEvent(motionEvent);
            touchContent(motionEvent);
            return true;
        });

        GestureDetector memberListGestureDetector = new GestureDetector(this, new ViewTapGestureDetector(ACTION_ADD_MEMBERS));
        mMemberRecyclerView.setOnTouchListener((v, motionEvent) -> {
            memberListGestureDetector.onTouchEvent(motionEvent);
            touchContent(motionEvent);
            return true;
        });

        mConfigurationTitleView = findViewById(R.id.create_group_activity_configuration_title_view);
        Design.updateTextFont(mConfigurationTitleView, Design.FONT_BOLD26);
        mConfigurationTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mConfigurationTitleView.getLayoutParams();
        marginLayoutParams.topMargin = Design.TITLE_IDENTITY_TOP_MARGIN;

        View permissionView = findViewById(R.id.create_group_activity_permissions_view);
        layoutParams = permissionView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = Design.ITEM_VIEW_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) permissionView.getLayoutParams();
        marginLayoutParams.topMargin = Design.IDENTITY_VIEW_TOP_MARGIN;

        permissionView.setOnClickListener(view -> onSettingsViewClick());

        GestureDetector permissionGestureDetector = new GestureDetector(this, new ViewTapGestureDetector(ACTION_SETTINGS));
        permissionView.setOnTouchListener((v, motionEvent) -> {
            permissionGestureDetector.onTouchEvent(motionEvent);
            touchContent(motionEvent);
            return true;
        });

        mPermissionsTextView = findViewById(R.id.create_group_activity_permissions_text_view);
        Design.updateTextFont(mPermissionsTextView, Design.FONT_REGULAR34);
        mPermissionsTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        ImageView permissionsImageView = findViewById(R.id.create_group_activity_permissions_image_view);
        permissionsImageView.setColorFilter(Design.SHOW_ICON_COLOR);

        mSaveClickableView = findViewById(R.id.create_group_activity_save_view);
        mSaveClickableView.setAlpha(0.5f);
        mSaveClickableView.setOnClickListener(v -> onSaveClick());

        GestureDetector saveGestureDetector = new GestureDetector(this, new ViewTapGestureDetector(ACTION_SAVE));
        mSaveClickableView.setOnTouchListener((v, motionEvent) -> {
            saveGestureDetector.onTouchEvent(motionEvent);
            touchContent(motionEvent);
            return true;
        });

        layoutParams = mSaveClickableView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = Design.BUTTON_HEIGHT;

        ShapeDrawable saveViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        saveViewBackground.getPaint().setColor(Design.getMainStyle());
        mSaveClickableView.setBackground(saveViewBackground);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mSaveClickableView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_SAVE_TOP_MARGIN * Design.HEIGHT_RATIO);

        mSaveTextView = findViewById(R.id.create_group_activity_save_title_view);
        Design.updateTextFont(mSaveTextView, Design.FONT_BOLD28);
        mSaveTextView.setTextColor(Color.WHITE);

        mProgressBarView = findViewById(R.id.create_group_activity_progress_bar);

        updateViews();

        mUIInitialized = true;
    }

    @Override
    protected void onSaveClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSaveClick");
        }

        if (!mCanCreate || mContacts == null) {
            return;
        }

        if (mCreateClicked) {
            return;
        }

        hideKeyboard();

        mCreateClicked = true;
        mName = mNameView.getText().toString().trim();
        String groupDescription = mDescriptionView.getText().toString().trim();

        long permissions = ~0;
        permissions &= ~(1L << ConversationService.Permission.UPDATE_MEMBER.ordinal());
        permissions &= ~(1L << ConversationService.Permission.REMOVE_MEMBER.ordinal());
        permissions &= ~(1L << ConversationService.Permission.RESET_CONVERSATION.ordinal());
        if (!mAllowInvitation) {
            permissions &= ~(1L << ConversationService.Permission.INVITE_MEMBER.ordinal());
        }
        if (!mAllowPostMessage) {
            permissions &= ~(1L << ConversationService.Permission.SEND_MESSAGE.ordinal());
            permissions &= ~(1L << ConversationService.Permission.SEND_AUDIO.ordinal());
            permissions &= ~(1L << ConversationService.Permission.SEND_VIDEO.ordinal());
            permissions &= ~(1L << ConversationService.Permission.SEND_IMAGE.ordinal());
            permissions &= ~(1L << ConversationService.Permission.SEND_FILE.ordinal());
        }
        if (!mAllowInviteMemberAsContact) {
            permissions &= ~(1L << ConversationService.Permission.SEND_TWINCODE.ordinal());
        }
        List<Contact> selectedMembers = AddGroupMemberActivity.fromIntentString(mContacts, mSelectedMembers);
        mGroupService.createGroup(mName, groupDescription, mAvatar, mAvatarFile, selectedMembers, permissions);
    }

    private void updateSelectedImage() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateSelectedImage");
        }

        mEditableView.getSelectedImage((String path, Bitmap bitmap, Bitmap largeImage) -> {
            mAvatar = bitmap;
            mAvatarFile = new File(path);
            updateViews();
        });
    }

    private void setUpdated() {
        if (DEBUG) {
            Log.d(LOG_TAG, "setUpdated");
        }
        mName = mNameView.getText().toString().trim();

        if (mName.isEmpty()) {
            if (!mCanCreate) {
                return;
            }
            mCanCreate = false;
            mSaveClickableView.setAlpha(0.5f);
        } else {
            if (mCanCreate) {
                return;
            }
            mCanCreate = true;
            mSaveClickableView.setAlpha(1f);
        }
    }

    private void postInitViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "postInitViews");
        }

        mUIPostInitialized = true;
    }

    private void updateViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateViews");
        }

        if (mAvatar != null) {
            mAvatarView.setVisibility(View.VISIBLE);
            mAvatarView.setImageBitmap(mAvatar);
        }

        mName = mNameView.getText().toString().trim();

        if (mSelectedMembers == null || mSelectedMembers.isEmpty()) {
            mMemberRecyclerView.setVisibility(View.GONE);
            mInviteTextView.setVisibility(View.GONE);
            mNoMemberTextView.setVisibility(View.VISIBLE);
            mNoMemberImageView.setVisibility(View.VISIBLE);
        } else {
            mMemberRecyclerView.setVisibility(View.VISIBLE);
            mInviteTextView.setVisibility(View.VISIBLE);
            mNoMemberTextView.setVisibility(View.GONE);
            mNoMemberImageView.setVisibility(View.GONE);
        }

        updateMembers();
        updateGroup();
    }

    @Override
    protected void onAddMemberClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onAddMemberClick");
        }

        Intent intent = new Intent(this, AddGroupMemberActivity.class);

        intent.putExtra(Intents.INTENT_CONTACT_SELECTION, mSelectedMembers);
        intent.putExtra(Intents.INTENT_FROM_MENU, true);

        startActivityForResult(intent, ADD_MEMBERS);
    }

    @Override
    protected void onSettingsViewClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSettingsViewClick");
        }

        Intent intent = new Intent(this, SettingsGroupActivity.class);
        intent.putExtra(Intents.INTENT_GROUP_ALLOW_INVITATION, mAllowInvitation);
        intent.putExtra(Intents.INTENT_GROUP_ALLOW_MESSAGE, mAllowPostMessage);
        intent.putExtra(Intents.INTENT_GROUP_INVITE_MEMBER_AS_CONTACT, mAllowInviteMemberAsContact);
        startActivityForResult(intent, SETTINGS);
    }

    private void updateGroup() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateGroup");
        }

        if (!mUIInitialized) {

            return;
        }

        mNameView.setHint(getString(R.string.application_name_hint));

        Bitmap avatar;
        if (mAvatar != null) {
            avatar = mAvatar;
            mAvatarView.setImageBitmap(avatar);
            mNoAvatarView.setVisibility(View.GONE);
        } else {
            mAvatarView.setBackgroundColor(Design.AVATAR_PLACEHOLDER_COLOR);
            mNoAvatarView.setVisibility(View.VISIBLE);
        }

        setUpdated();
    }

    private void updateMembers() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateMembers");
        }

        mMemberListAdapter.clearAllMembers();

        if (mContacts == null) {
            return;
        }

        final List<Contact> selectedMembers = AddGroupMemberActivity.fromIntentString(mContacts, mSelectedMembers);
        AtomicInteger avatarCounter = new AtomicInteger(selectedMembers.size());
        for (Contact contact : selectedMembers) {
            mGroupService.getImage(contact, (Bitmap memberAvatar) -> {
                mMemberListAdapter.updateUIMember(contact, memberAvatar);
                if (avatarCounter.decrementAndGet() == 0) {
                    mMemberListAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    private void openMenuPhoto() {
        if (DEBUG) {
            Log.d(LOG_TAG, "openMenuPhoto");
        }

        hideKeyboard();

        ViewGroup viewGroup = findViewById(R.id.create_group_activity_layout);

        MenuPhotoView menuPhotoView = new MenuPhotoView(this, null);
        MenuPhotoView.Observer observer = new MenuPhotoView.Observer() {
            @Override
            public void onCameraClick() {

                menuPhotoView.animationCloseMenu();
                mEditableView.onCameraClick();
            }

            @Override
            public void onPhotoGalleryClick() {

                menuPhotoView.animationCloseMenu();
                mEditableView.onGalleryClick();
            }

            @Override
            public void onCloseMenuSelectActionAnimationEnd() {

                viewGroup.removeView(menuPhotoView);

                Window window = getWindow();
                window.setNavigationBarColor(Design.WHITE_COLOR);
            }
        };

        menuPhotoView.setObserver(observer);
        viewGroup.addView(menuPhotoView);

        List<UIMenuSelectAction> actions = new ArrayList<>();
        actions.add(new UIMenuSelectAction(getString(R.string.application_camera), R.drawable.grey_camera));
        actions.add(new UIMenuSelectAction(getString(R.string.application_photo_gallery), R.drawable.from_gallery));
        menuPhotoView.setActions(actions, this);
        menuPhotoView.openMenu(true);

        Window window = getWindow();
        window.setNavigationBarColor(Design.POPUP_BACKGROUND_COLOR);
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

        Design.updateTextFont(mTitleView, Design.FONT_BOLD44);
        Design.updateTextFont(mNameView, Design.FONT_REGULAR28);
        Design.updateTextFont(mCounterNameView, Design.FONT_REGULAR26);
        Design.updateTextFont(mDescriptionView, Design.FONT_REGULAR28);
        Design.updateTextFont(mCounterDescriptionView, Design.FONT_REGULAR26);
        Design.updateTextFont(mMemberTextView, Design.FONT_BOLD26);
        Design.updateTextFont(mInviteTextView, Design.FONT_BOLD28);
        Design.updateTextFont(mNoMemberTextView, Design.FONT_REGULAR34);
        Design.updateTextFont(mConfigurationTitleView, Design.FONT_BOLD26);
        Design.updateTextFont(mPermissionsTextView, Design.FONT_REGULAR34);
        Design.updateTextFont(mSaveTextView, Design.FONT_BOLD28);
    }

    @Override
    public void updateColor() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateColor");
        }

        super.updateColor();

        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mNameView.setTextColor(Design.EDIT_TEXT_TEXT_COLOR);
        mNameView.setHintTextColor(Design.GREY_COLOR);
        mCounterNameView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mDescriptionView.setTextColor(Design.EDIT_TEXT_TEXT_COLOR);
        mDescriptionView.setHintTextColor(Design.GREY_COLOR);
        mCounterDescriptionView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mMemberTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mInviteTextView.setTextColor(Design.getMainStyle());
        mNoMemberTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mConfigurationTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mPermissionsTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
    }
}
