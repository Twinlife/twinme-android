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
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Group;
import org.twinlife.twinme.models.GroupMember;
import org.twinlife.twinme.models.Invitation;
import org.twinlife.twinme.models.Space;
import org.twinlife.twinme.models.schedule.DateTime;
import org.twinlife.twinme.models.schedule.DateTimeRange;
import org.twinlife.twinme.models.schedule.Schedule;
import org.twinlife.twinme.services.GroupService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.contacts.AbstractCapabilitiesActivity;
import org.twinlife.twinme.ui.contacts.CapabilitiesAdapter;

import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

public class GroupCapabilitiesActivity extends AbstractCapabilitiesActivity implements GroupService.Observer {
    private static final String LOG_TAG = "GroupCapabilitiesAc...";
    private static final boolean DEBUG = false;

    private UUID mGroupId;
    private Group mGroup;

    private GroupService mGroupService;

    private boolean mUIInitialized = false;
    private final boolean mUIPostInitialized = false;

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
        } else {
            finish();
        }

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

        if (mCanSave || mScheduleEnable) {
            mCapabilities.setCapAudio(mAllowAudioCall);
            mCapabilities.setCapVideo(mAllowVideoCall);

            if (mScheduleEnable) {
                DateTime start = new DateTime(mScheduleStartDate, mScheduleStartTime);
                DateTime end = new DateTime(mScheduleEndDate, mScheduleEndTime);
                Schedule schedule = new Schedule(false, TimeZone.getDefault(), Collections.singletonList(new DateTimeRange(start, end)));
                mCapabilities.setSchedule(schedule);
            }

            mGroupService.updateGroup(null, null, null, null, null, mCapabilities);
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        mGroupService.dispose();

        super.onDestroy();
    }

    public boolean isGroup() {

        return true;
    }

    //
    // Implement GroupService.Observer methods
    //

    @Override
    public void onCreateGroup(@NonNull Group group, @NonNull ConversationService.GroupConversation conversation) {

    }

    @Override
    public void onGetGroup(@NonNull Group group, @NonNull List<GroupMember> groupMembers, @NonNull ConversationService.GroupConversation conversation) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetGroup group=" + group);
        }

        mGroup = group;
    }

    @Override
    public void onUpdateGroup(@NonNull Group group, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateGroup group=" + group);
        }

        finish();
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

    @Override
    public void onGetContacts(@NonNull List<Contact> contacts) {

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

    }

    @Override
    protected void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        setContentView(R.layout.group_capabilities_activity);

        setStatusBarColor();
        setToolBar(R.id.group_capabilities_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);
        setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        setTitle(getString(R.string.contact_capabilities_activity_call_settings));

        applyInsets(R.id.capabilities_activity_layout, R.id.group_capabilities_activity_tool_bar, R.id.group_capabilities_activity_list_view, Design.TOOLBAR_COLOR, false);

        mCapabilitiesAdapter = new CapabilitiesAdapter(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        RecyclerView settingsRecyclerView = findViewById(R.id.group_capabilities_activity_list_view);
        settingsRecyclerView.setLayoutManager(linearLayoutManager);
        settingsRecyclerView.setAdapter(mCapabilitiesAdapter);
        settingsRecyclerView.setItemAnimator(null);
        settingsRecyclerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        mProgressBarView = findViewById(R.id.group_capabilities_activity_progress_bar);

        mUIInitialized = true;
    }

}
