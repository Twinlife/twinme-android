/*
 *  Copyright (c) 2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.groups;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinme.models.Group;
import org.twinlife.twinme.models.GroupMember;
import org.twinlife.twinme.services.EditContactCapabilitiesService;
import org.twinlife.twinme.services.GroupService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.Intents;

import java.util.List;
import java.util.UUID;

public class SettingsGroupActivity extends AbstractGroupActivity implements EditContactCapabilitiesService.Observer {
    private static final String LOG_TAG = "SettingsGroupActivity";
    private static final boolean DEBUG = false;

    public static final int ALLOW_INVITATION_SWITCH = 0;
    public static final int ALLOW_MESSAGE_SWITCH = 1;
    public static final int ALLOW_INVITE_MEMBER_AS_CONTACT_SWITCH = 2;

    private UUID mGroupId;
    private Group mGroup;

    private SettingsGroupAdapter mSettingsGroupAdapter;
    private GroupService mGroupService;

    private boolean mAllowInvitation = true;
    private boolean mAllowPostMessage = true;
    private boolean mAllowInviteMemberAsContact = true;

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
        String value = intent.getStringExtra(Intents.INTENT_GROUP_ID);
        if (value != null) {
            mGroupId = UUID.fromString(value);
        }

        mAllowInvitation = intent.getBooleanExtra(Intents.INTENT_GROUP_ALLOW_INVITATION, true);
        mAllowPostMessage = intent.getBooleanExtra(Intents.INTENT_GROUP_ALLOW_MESSAGE, true);
        mAllowInviteMemberAsContact = intent.getBooleanExtra(Intents.INTENT_GROUP_INVITE_MEMBER_AS_CONTACT, true);

        initViews();

        mGroupService = new GroupService(this, getTwinmeContext(), this);
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

        if (mGroupId != null) {
            mGroupService.getGroup(mGroupId, false);
        }
    }

    @Override
    protected void onPause() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPause");
        }

        super.onPause();

        if (mGroup != null) {
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

            mGroupService.updateGroupPermissions(permissions);
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

    public boolean allowInvitation() {
        if (DEBUG) {
            Log.d(LOG_TAG, "allowInvitation");
        }

        return mAllowInvitation;
    }

    public boolean allowMessage() {
        if (DEBUG) {
            Log.d(LOG_TAG, "allowMessage");
        }

        return mAllowPostMessage;
    }

    public boolean allowInviteMemberAsContact() {
        if (DEBUG) {
            Log.d(LOG_TAG, "allowInviteMemberAsContact");
        }

        return mAllowInviteMemberAsContact;
    }

    public void onSettingChangeValue(int switchTag, boolean value) {

        switch (switchTag) {
            case ALLOW_INVITATION_SWITCH:
                mAllowInvitation = value;
                break;

            case ALLOW_MESSAGE_SWITCH:
                mAllowPostMessage = value;
                break;

            case ALLOW_INVITE_MEMBER_AS_CONTACT_SWITCH:
                mAllowInviteMemberAsContact = value;
                break;

            default:
                break;
        }

        if (mGroup == null) {
            updateSettings();
        }

        mSettingsGroupAdapter.notifyDataSetChanged();
    }

    //
    // Implement GroupService.Observer methods
    //

    @Override
    public void onGetGroup(@NonNull Group group, @NonNull List<GroupMember> groupMembers, @NonNull ConversationService.GroupConversation conversation) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetGroup group=" + group);
        }

        mGroup = group;
        ConversationService.GroupConversation  groupConversation = conversation;

        long joinPermissions = groupConversation.getJoinPermissions();
        mAllowInvitation = (joinPermissions & (1L << ConversationService.Permission.INVITE_MEMBER.ordinal())) != 0;
        mAllowInviteMemberAsContact = (joinPermissions & (1L << ConversationService.Permission.SEND_TWINCODE.ordinal())) != 0;
        mAllowPostMessage = (joinPermissions & (1L << ConversationService.Permission.SEND_MESSAGE.ordinal())) != 0;

        mSettingsGroupAdapter.notifyDataSetChanged();
    }

    @Override
    public void onUpdateGroup(@NonNull Group group, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateGroup group=" + group);
        }

        finish();
    }

    private  void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        setContentView(R.layout.settings_group_activity);

        setStatusBarColor();
        setToolBar(R.id.settings_group_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);
        setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        setTitle(getString(R.string.settings_activity_authorization_title));
        applyInsets(R.id.settings_group_activity_layout, R.id.settings_group_activity_tool_bar, R.id.settings_group_activity_list_view, Design.TOOLBAR_COLOR, false);

        mSettingsGroupAdapter = new SettingsGroupAdapter(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        RecyclerView settingsRecyclerView = findViewById(R.id.settings_group_activity_list_view);
        settingsRecyclerView.setLayoutManager(linearLayoutManager);
        settingsRecyclerView.setAdapter(mSettingsGroupAdapter);
        settingsRecyclerView.setItemAnimator(null);
        settingsRecyclerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        mProgressBarView = findViewById(R.id.settings_group_activity_progress_bar);
    }

    private void updateSettings() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateSettings");
        }

        Intent data = new Intent();
        data.putExtra(Intents.INTENT_GROUP_ALLOW_INVITATION, mAllowInvitation);
        data.putExtra(Intents.INTENT_GROUP_ALLOW_MESSAGE, mAllowPostMessage);
        data.putExtra(Intents.INTENT_GROUP_INVITE_MEMBER_AS_CONTACT, mAllowInviteMemberAsContact);
        setResult(RESULT_OK, data);
    }
}
