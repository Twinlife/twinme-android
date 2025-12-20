/*
 *  Copyright (c) 2018-2021 twinlife SA.
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
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinlife.ConversationService.GroupConversation;
import org.twinlife.twinlife.ConversationService.InvitationDescriptor;
import org.twinlife.twinlife.TwincodeOutbound;
import org.twinlife.twinlife.util.Utils;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Group;
import org.twinlife.twinme.models.GroupMember;
import org.twinlife.twinme.models.InvitedGroupMember;
import org.twinlife.twinme.services.GroupService;
import org.twinlife.twinme.services.GroupService.PendingInvitationsObserver;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.contacts.DeleteConfirmView;
import org.twinlife.twinme.ui.groups.GroupMemberListAdapter.OnGroupMemberClickListener;
import org.twinlife.twinme.ui.users.UIContact;
import org.twinlife.twinme.ui.users.UIInvitation;
import org.twinlife.twinme.utils.AbstractBottomSheetView;
import org.twinlife.twinme.utils.DefaultConfirmView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Activity controller to display the group members and allow to remove them if the user has the permission.
 */

public class GroupMemberActivity extends AbstractGroupActivity implements PendingInvitationsObserver, MenuGroupMemberView.Observer {
    private static final String LOG_TAG = "GroupMemberActivity";
    private static final boolean DEBUG = false;

    private static final int ADD_MEMBERS = 1;

    @Nullable
    private UUID mGroupId;

    private View mFallbackView;

    private boolean mUIInitialized = false;
    @Nullable
    private Group mGroup;
    private final List<UIContact> mUIMembers = new ArrayList<>();
    private final List<UIInvitation> mUIInvitations = new ArrayList<>();
    @Nullable
    private Map<UUID, InvitationDescriptor> mInvitedContacts;
    @Nullable
    private Map<UUID, Contact> mContactList;
    private boolean mDeletedGroup = false;
    private boolean mCanInvite = false;
    private boolean mCanRemove = false;
    private boolean mCanInviteMemberAsContact = false;
    private GroupMember mCurrentMember;
    private GroupMember mAdminMember;
    private String mIdentityName;
    private Bitmap mIdentityAvatar;
    private GroupMemberListAdapter mGroupMemberListAdapter;
    private RecyclerView mUIMemberRecyclerView;
    private MenuGroupMemberView mMenuGroupMemberView;
    private View mOverlayView;
    private GroupService mGroupService;
    private String mContactsToAdd;
    @Nullable
    private Menu mMenu;

    @Nullable
    private UIContact mSelectedContact;

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
        mGroupId = Utils.UUIDFromString(intent.getStringExtra(Intents.INTENT_GROUP_ID));

        // Start with an empty contact list.
        initViews();
    }

    @Override
    protected void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        super.onResume();
        if (mDeletedGroup) {
            finish();
        } else {
            if (mGroupId != null) {
                mGroupService.getGroup(mGroupId, false);
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
    // Implement GroupService.Observer methods
    //

    @Override
    public void onGetGroup(@NonNull Group group, @NonNull List<GroupMember> groupMembers, @NonNull GroupConversation conversation) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetGroup group=" + group);
        }

        mGroup = group;

        if (mGroup.hasPeer()) {
            mIdentityName = group.getIdentityName();
            if (mIdentityName == null) {
                mIdentityName = getTwinmeApplication().getAnonymousName();
            }

            // If the user has permissions to invite other users, display the add member button.
            mCanInvite = conversation.hasPermission(ConversationService.Permission.INVITE_MEMBER);
            mCanRemove = conversation.hasPermission(ConversationService.Permission.REMOVE_MEMBER);
            mCanInviteMemberAsContact = conversation.hasPermission(ConversationService.Permission.SEND_TWINCODE);
        } else {
            mFallbackView.setVisibility(View.VISIBLE);

            mIdentityName = getTwinmeApplication().getAnonymousName();
            mIdentityAvatar = getTwinmeApplication().getAnonymousAvatar();
            mCanInvite = false;
            mCanRemove = false;
            mCanInviteMemberAsContact = false;
        }

        if (mGroup.hasPeer()) {
            // We're in the group => get our avatar first.
            mGroupService.getIdentityImage(group, (Bitmap identityAvatar) -> {
                mIdentityAvatar = identityAvatar;
                if (mIdentityAvatar == null) {
                    mIdentityAvatar = getTwinmeApplication().getAnonymousAvatar();
                }
                onGetGroupMembers(group, groupMembers);
            });
        } else {
            onGetGroupMembers(group, groupMembers);
        }
    }

    private void onGetGroupMembers(@NonNull Group group, @NonNull List<GroupMember> groupMembers) {
        mAdminMember = null;
        mUIMembers.clear();

        mCurrentMember = group.getCurrentMember();
        if (group.isOwner()) {
            mAdminMember = mCurrentMember;
            mGroupMemberListAdapter.updateAdmin(mCurrentMember, mIdentityAvatar);
        } else {
            mGroupMemberListAdapter.updateUIMember(mCurrentMember, mIdentityAvatar);
        }

        if (groupMembers.isEmpty()) {
            updateMembers();
            return;
        }

        final UUID adminTwincode = group.getCreatedByMemberTwincodeOutboundId();
        AtomicInteger avatarCounter = new AtomicInteger(groupMembers.size());

        for (GroupMember member : groupMembers) {
            mGroupService.getGroupMemberImage(member, (Bitmap memberAvatar) -> {
                if (adminTwincode != null && adminTwincode.equals(member.getPeerTwincodeOutboundId())) {
                    mAdminMember = member;
                    mGroupMemberListAdapter.updateAdmin(mAdminMember, memberAvatar);
                } else {
                    mGroupMemberListAdapter.updateUIMember(member, memberAvatar);
                }

                if (avatarCounter.decrementAndGet() == 0) {
                    updateMembers();
                }
            });
        }
    }

    @Override
    public void onListPendingInvitations(@NonNull Map<UUID, InvitationDescriptor> list) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onListPendingInvitations");
        }

        mInvitedContacts = list;

        if (!mInvitedContacts.isEmpty() && mContactList == null) {
            mGroupService.getContacts();
        }
    }

    @Override
    public void onGetGroupNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onErrorGroupNotFound");
        }

        mFallbackView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onUpdateGroup(@NonNull Group group, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateGroup group=" + group);
        }

        if (!group.getId().equals(mGroupId)) {

            return;
        }

        mGroup = group;
        if (group.isLeaving()) {
            mDeletedGroup = true;

            if (mResumed) {
                finish();
                return;
            }
        }

        mIdentityName = group.getIdentityName();
        if (mIdentityName == null) {
            mIdentityName = getTwinmeApplication().getAnonymousName();
        }
        mGroupService.getIdentityImage(group, (Bitmap identityAvatar) -> {
            mIdentityAvatar = identityAvatar;

            if (mIdentityAvatar == null) {
                mIdentityAvatar = getTwinmeApplication().getAnonymousAvatar();
            }
            mCurrentMember = group.getCurrentMember();
            if (group.isOwner()) {
                mAdminMember = mCurrentMember;
            } else {
                mGroupMemberListAdapter.updateUIMember(mCurrentMember, mIdentityAvatar);
            }
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

        // The group can be deleted while the application is in background.
        mDeletedGroup = true;
        if (mResumed) {
            finish();
        }
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

        mContactList = new HashMap<>();
        for (Contact contact : contacts) {
            mContactList.put(contact.getId(), contact);
        }

        updateMembers();
    }

    @Override
    public void onInviteGroup(@NonNull ConversationService.Conversation conversation, @NonNull InvitationDescriptor invitationDescriptor) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onInviteGroup conversation=" + conversation + "invitationDescriptor" + invitationDescriptor);
        }

        mInvitedContacts.put(conversation.getContactId(), invitationDescriptor);
    }

    @Override
    public void onLeaveGroup(@NonNull Group group, @NonNull UUID memberTwincodeId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onLeaveGroup group=" + group);
        }

        mGroupMemberListAdapter.removeUIContact(memberTwincodeId);
        updateMembers();

        // If the current member leaves the group, the group object is marked as isLeaving and deleted at the very end.
        if (group.isLeaving()) {
            mDeletedGroup = true;
            if (mResumed) {
                finish();
            }
        }
    }

    //
    // Menu management
    //

    private void openMenu(UIContact uiContact, boolean canInvite, boolean canRemove) {
        if (DEBUG) {
            Log.d(LOG_TAG, "openMenu");
        }

        if (mMenuGroupMemberView.getVisibility() == View.INVISIBLE) {
            mSelectedContact = uiContact;
            mMenuGroupMemberView.setVisibility(View.VISIBLE);
            mOverlayView.setVisibility(View.VISIBLE);
            mMenuGroupMemberView.openMenu(uiContact, canInvite, canRemove);
            int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
            setStatusBarColor(color, Design.WHITE_COLOR);
        }
    }

    public void closeMenu(boolean updateStatusBar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "closeMenu");
        }

        mMenuGroupMemberView.animationCloseMenu();
        if (updateStatusBar) {
            setStatusBarColor();
        }
        mSelectedContact = null;
    }

    public void onInviteMemberClick(boolean canInvite) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onInviteMemberClick");
        }

        if (mSelectedContact != null) {
            if (canInvite) {
                inviteMember(mSelectedContact);
            } else {
                showAlertMessageView(R.id.group_member_activity_layout, getString(R.string.deleted_account_activity_warning), getString(R.string.group_member_activity_admin_not_authorize), false, null);
            }
        }

        closeMenu(true);
    }

    public void onRemoveMemberClick(boolean canRemove) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRemoveMemberClick");
        }

        if (mSelectedContact != null) {
            if (canRemove) {
                onRemoveClick(mSelectedContact);

                closeMenu(false);
            } else {
                showAlertMessageView(R.id.group_member_activity_layout, getString(R.string.deleted_account_activity_warning), getString(R.string.group_member_activity_admin_not_authorize), false, null);
            }
        }


    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateOptionsMenu: menu=" + menu);
        }

        super.onCreateOptionsMenu(menu);

        mMenu = menu;

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.group_member_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.add_member_action);

        ImageView imageView = (ImageView) menuItem.getActionView();

        if (imageView != null) {
            imageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.action_bar_add_contact, null));
            imageView.setPadding(Design.TOOLBAR_IMAGE_ITEM_PADDING, 0, Design.TOOLBAR_IMAGE_ITEM_PADDING, 0);
            imageView.setOnClickListener(view -> onAddMemberClick());

            if (mCanInvite) {
                imageView.setAlpha(1.0f);
            } else {
                imageView.setAlpha(0.5f);
            }
        }

        return true;
    }

    //MenuGroupMemberView.Observer

    @Override
    public void onCloseMenuAnimationEnd() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCloseMenuAnimationEnd");
        }

        mMenuGroupMemberView.setVisibility(View.INVISIBLE);
        mOverlayView.setVisibility(View.INVISIBLE);
    }

    //
    // Private methods
    //

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.group_member_activity);

        setStatusBarColor();
        setToolBar(R.id.group_member_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);

        setTitle(getString(R.string.group_member_activity_title));
        setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        applyInsets(R.id.group_member_activity_layout, R.id.group_member_activity_tool_bar, R.id.group_member_activity_member_list_view, Design.TOOLBAR_COLOR, false);

        mFallbackView = findViewById(R.id.group_member_activity_fallback_view);

        OnGroupMemberClickListener onGroupMemberClickListener = new OnGroupMemberClickListener() {

            @Override
            public void onAdminClick(UIContact uiAdmin) {

                boolean canInvite = false;
                if (mGroup != null && mCanInviteMemberAsContact && uiAdmin.getContact().getPeerTwincodeOutboundId() != mGroup.getMemberTwincodeOutboundId()) {
                    canInvite = true;
                }
                openMenu(uiAdmin, canInvite, mCanRemove);
            }

            @Override
            public void onMemberClick(UIContact uiMember) {

                boolean canInvite = false;
                if (mGroup != null && mCanInviteMemberAsContact && uiMember.getContact().getPeerTwincodeOutboundId() != mGroup.getMemberTwincodeOutboundId()) {
                    canInvite = true;
                }
                openMenu(uiMember, canInvite, mCanRemove);
            }

            @Override
            public void onInvitationClick(UIInvitation uiInvitation) {

                openMenu(uiInvitation, false, true);
            }
        };

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        mUIMemberRecyclerView = findViewById(R.id.group_member_activity_member_list_view);
        mUIMemberRecyclerView.setLayoutManager(layoutManager);
        mUIMemberRecyclerView.setItemViewCacheSize(Design.ITEM_LIST_CACHE_SIZE);
        mUIMemberRecyclerView.setItemAnimator(null);
        mUIMemberRecyclerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        mOverlayView = findViewById(R.id.group_member_activity_overlay_view);
        mOverlayView.setBackgroundColor(Design.OVERLAY_VIEW_COLOR);
        mOverlayView.setOnClickListener(view -> closeMenu(true));

        mMenuGroupMemberView = findViewById(R.id.group_member_activity_menu_view);
        mMenuGroupMemberView.setVisibility(View.INVISIBLE);
        mMenuGroupMemberView.setObserver(this);
        mMenuGroupMemberView.setGroupMemberActivity(this);

        mProgressBarView = findViewById(R.id.group_member_activity_progress_bar);

        // Setup the service after the view is initialized but before the adapter!
        mGroupService = new GroupService(this, getTwinmeContext(), this);
        mGroupMemberListAdapter = new GroupMemberListAdapter(this, mGroupService, Design.ITEM_VIEW_HEIGHT, null, mUIMembers, mUIInvitations, R.layout.group_member_activity_member_item, R.id.group_member_activity_member_item_name_view,
                R.id.group_member_activity_member_item_avatar_view, R.id.group_member_activity_member_item_separator_view, onGroupMemberClickListener);
        mUIMemberRecyclerView.setAdapter(mGroupMemberListAdapter);

        mUIInitialized = true;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void notifyMemberListChanged() {
        if (DEBUG) {
            Log.d(LOG_TAG, "notifyContactListChanged");
        }

        if (mUIInitialized) {
            mUIMemberRecyclerView.requestLayout();
            mGroupMemberListAdapter.notifyDataSetChanged();
        }
    }

    private void onRemoveClick(@NonNull UIContact uiContact) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRemoveClick");
        }

        ViewGroup viewGroup = findViewById(R.id.group_member_activity_layout);

        DeleteConfirmView deleteConfirmView = new DeleteConfirmView(this, null);
        deleteConfirmView.setAvatar(uiContact.getAvatar(), false);
        deleteConfirmView.setMessage(getString(R.string.group_member_activity_remove_message));

        AbstractBottomSheetView.Observer observer = new AbstractBottomSheetView.Observer() {
            @Override
            public void onConfirmClick() {
                onConfirmedRemoveClick(uiContact);
                deleteConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCancelClick() {
                deleteConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onDismissClick() {
                deleteConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                viewGroup.removeView(deleteConfirmView);
                setStatusBarColor();
            }
        };
        deleteConfirmView.setObserver(observer);
        viewGroup.addView(deleteConfirmView);
        deleteConfirmView.show();

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }

    private void onConfirmedRemoveClick(@NonNull UIContact uiContact) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onConfirmedRemoveClick");
        }

        if (uiContact instanceof UIInvitation) {
            UIInvitation uiInvitation = (UIInvitation) uiContact;
            mGroupService.withdrawInvitation(uiInvitation.getInvitationDescriptor());
            mGroupMemberListAdapter.removeUIInvitation(uiInvitation.getId());
            updateMembers();
        } else if (uiContact.getContact() != null && uiContact.getContact().getPeerTwincodeOutboundId() != null) {
            mGroupService.leaveGroup(uiContact.getContact().getPeerTwincodeOutboundId());
        }
    }

    private void onAddMemberClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onConfirmedRemoveClick");
        }

        if (!mCanInvite) {
            showAlertMessageView(R.id.group_member_activity_layout, getString(R.string.deleted_account_activity_warning), getString(R.string.group_member_activity_admin_not_authorize), false, null);
        } else if (mGroupId != null) {
            Intent intent = new Intent();
            intent.setClass(this, AddGroupMemberActivity.class);
            intent.putExtra(Intents.INTENT_GROUP_ID, mGroupId.toString());
            startActivityForResult(intent, ADD_MEMBERS);
        }
    }

    private void updateMembers() {
        if (mMenu != null) {
            MenuItem addMemberMenuItem = mMenu.findItem(R.id.add_member_action);
            if (mCanInvite) {
                addMemberMenuItem.getActionView().setAlpha(1.0f);
            } else {
                addMemberMenuItem.getActionView().setAlpha(0.5f);
            }
        }

        mUIInvitations.clear();
        if (mInvitedContacts == null || mContactList == null || mInvitedContacts.isEmpty()) {
            notifyMemberListChanged();
            return;
        }

        // Create a InvitedGroupMember for each pending invitation.
        AtomicInteger avatarCounter = new AtomicInteger(mInvitedContacts.size());

        for (Map.Entry<UUID, InvitationDescriptor> invitedContact : mInvitedContacts.entrySet()) {
            final Contact contact = mContactList.get(invitedContact.getKey());
            if (contact != null) {
                InvitationDescriptor invitation = invitedContact.getValue();
                final InvitedGroupMember member = new InvitedGroupMember(contact, invitation.getDescriptorId());
                mGroupService.getImage(member, (Bitmap avatar) -> {
                    mGroupMemberListAdapter.updateUIInvitation(member, invitation, avatar);

                    if (avatarCounter.decrementAndGet() == 0) {
                        notifyMemberListChanged();
                    }
                });
            }
        }
    }

    private void inviteMember(@NonNull UIContact uiContact) {

        ViewGroup viewGroup = findViewById(R.id.group_member_activity_layout);

        DefaultConfirmView defaultConfirmView = new DefaultConfirmView(this, null);
        defaultConfirmView.setAvatar(uiContact.getAvatar(), false);
        defaultConfirmView.setTitle(uiContact.getName());
        defaultConfirmView.setMessage(String.format(getString(R.string.group_member_activity_invitation_message), uiContact.getName()));
        defaultConfirmView.setConfirmTitle(getString(R.string.add_contact_activity_invite));

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
                setStatusBarColor();

                if (fromConfirmAction) {
                    TwincodeOutbound twincodeOutbound = uiContact.getContact().getPeerTwincodeOutbound();
                    if (mGroup != null && twincodeOutbound != null) {
                        GroupMember groupMember = new GroupMember(mGroup, twincodeOutbound);
                        mGroupService.createInvitation(groupMember);
                    }
                }
            }
        };
        defaultConfirmView.setObserver(observer);
        viewGroup.addView(defaultConfirmView);
        defaultConfirmView.show();

        Window window = getWindow();
        window.setNavigationBarColor(Design.POPUP_BACKGROUND_COLOR);
    }
}
