/*
 *  Copyright (c) 2018-2020 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.services;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.twinlife.twinlife.BaseService;
import org.twinlife.twinlife.BaseService.ErrorCode;
import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinlife.ConversationService.Conversation;
import org.twinlife.twinlife.ConversationService.GroupConversation;
import org.twinlife.twinlife.ConversationService.InvitationDescriptor;
import org.twinlife.twinlife.Filter;
import org.twinlife.twinlife.NotificationService.NotificationStat;
import org.twinlife.twinlife.RepositoryObject;
import org.twinlife.twinlife.TwincodeFactory;
import org.twinlife.twinlife.TwincodeOutboundService;
import org.twinlife.twinlife.TwincodeOutbound;
import org.twinlife.twinme.TwinmeContext;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Group;
import org.twinlife.twinme.models.Profile;
import org.twinlife.twinme.models.Space;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A test service used for test activity.
 */
public class TestService extends AbstractTwinmeService {
    private static final String LOG_TAG = "TestService";
    private static final boolean DEBUG = false;

    private static final int GET_DEFAULT_PROFILE = 1;
    private static final int GET_DEFAULT_PROFILE_DONE = 1 << 1;
    private static final int GET_CONTACTS = 1 << 2;
    private static final int GET_CONTACTS_DONE = 1 << 3;
    private static final int GET_CONVERSATIONS = 1 << 4;
    private static final int GET_CONVERSATIONS_DONE = 1 << 5;
    private static final int GET_PENDING_NOTIFICATIONS = 1 << 6;
    private static final int GET_PENDING_NOTIFICATIONS_DONE = 1 << 7;
    private static final int RESET_CONVERSATION = 1 << 8;
    private static final int CREATE_GROUP = 1 << 10;
    private static final int CREATE_GROUP_MEMBER = 1 << 11;
    private static final int GET_GROUP = 1 << 12;
    private static final int GET_GROUP_FOR_JOIN = 1 << 14;
    private static final int GET_TWINCODE = 1 << 15;
    private static final int GET_TWINCODE_DONE = 1 << 16;
    private static final int CREATE_CONTACT = 1 << 17;

    public interface Observer extends AbstractTwinmeService.Observer {

        void onUpdateDefaultProfile(@NonNull Profile profile);

        void onGetDefaultProfileNotFound();

        void onGetContacts(@NonNull List<Contact> contacts);

        void onCreateContact(@NonNull Contact contact);

        void onUpdateContact(@NonNull Contact contact);

        void onDeleteContact(@NonNull UUID contactId);

        void onGetConversations(@NonNull List<Conversation> conversations);

        void onDeleteAccount();

        void onGetOrCreateConversation(@NonNull Conversation conversation);

        void onResetConversation(@NonNull Conversation conversation);

        void onDeleteConversation(@NonNull UUID conversationId);

        void onUpdatePendingNotifications(boolean hasPendingNotification);

        void onGetOrCreateGroup(@NonNull ConversationService.GroupConversation conversation);

        void onInviteGroup(@NonNull UUID conversationId, @NonNull UUID groupId, @NonNull String name);

        void onJoinGroup(@NonNull UUID conversationId, @NonNull UUID groupId);

        void onInviteGroupCall(@NonNull UUID conversationId, @NonNull UUID groupId, @NonNull String name);

        void onJoinGroupCall(@NonNull UUID conversationId, @NonNull UUID groupId, @NonNull UUID memberId, boolean accepted);

        void onCreateMember(@NonNull UUID conversationId, @NonNull UUID groupId, @NonNull String name, @NonNull Group group);
    }

    private class TwinmeContextObserver extends AbstractTwinmeService.TwinmeContextObserver {

        @Override
        public void onCreateProfile(long requestId, @NonNull Profile profile) {
            if (DEBUG) {
                Log.d(LOG_TAG, "TwinmeContextObserver.onCreateProfile: requestId=" + requestId + " profile=" + profile);
            }

            runOnUiThread(() -> TestService.this.onCreateProfile(profile));
        }

        @Override
        public void onUpdateProfile(long requestId, @NonNull Profile profile) {
            if (DEBUG) {
                Log.d(LOG_TAG, "TwinmeContextObserver.onUpdateProfile: requestId=" + requestId + " profile=" + profile);
            }

            runOnUiThread(() -> TestService.this.onUpdateProfile(profile));
        }

        @Override
        public void onCreateContact(long requestId, @NonNull Contact contact) {
            if (DEBUG) {
                Log.d(LOG_TAG, "TwinmeContextObserver.onCreateContact: requestId=" + requestId + " contact=" + contact);
            }

            runOnUiThread(() -> TestService.this.onCreateContact(contact));
        }

        @Override
        public void onUpdateContact(long requestId, @NonNull Contact contact) {
            if (DEBUG) {
                Log.d(LOG_TAG, "TwinmeContextObserver.onUpdateContact: requestId=" + requestId + " contact=" + contact);
            }

            runOnUiThread(() -> TestService.this.onUpdateContact(contact));
        }

        @Override
        public void onDeleteContact(long requestId, @NonNull UUID contactId) {
            if (DEBUG) {
                Log.d(LOG_TAG, "TwinmeContextObserver.onDeleteContact: requestId=" + requestId + " contactId=" + contactId);
            }

            runOnUiThread(() -> TestService.this.onDeleteContact(contactId));
        }

        @Override
        public void onDeleteAccount(long requestId) {
            if (DEBUG) {
                Log.d(LOG_TAG, "TwinmeContextObserver.onDeleteAccount: requestId=" + requestId);
            }

            runOnUiThread(TestService.this::onDeleteAccount);
        }

        @Override
        public void onCreateGroup(long requestId, @NonNull Group group, @NonNull GroupConversation conversation) {
            if (DEBUG) {
                Log.d(LOG_TAG, "TwinmeContextObserver.onCreateGroup: requestId=" + requestId + " group=" + group);
            }
            Integer operationId;
            synchronized (mRequestIds) {
                operationId = mRequestIds.remove(requestId);
            }
            if (operationId != null) {
                runOnUiThread(() -> TestService.this.onCreateGroup(requestId, operationId, group, conversation));
            }
        }

        @Override
        public void onUpdatePendingNotifications(long requestId, boolean hasPendingNotifications) {
            if (DEBUG) {
                Log.d(LOG_TAG, "TwinmeContextObserver.onUpdatePendingNotifications: requestId=" + requestId + " hasPendingNotifications=" + hasPendingNotifications);
            }

            runOnUiThread(() -> TestService.this.onUpdatePendingNotifications(hasPendingNotifications));
        }
    }

    private class ConversationServiceObserver extends ConversationService.DefaultServiceObserver {

        @Override
        public void onCreateConversation(@NonNull Conversation conversation) {
            if (DEBUG) {
                Log.d(LOG_TAG, "ConversationServiceObserver.onCreateConversation: conversation=" + conversation);
            }

            runOnUiThread(() -> TestService.this.onGetOrCreateConversation(conversation));
        }

        @Override
        public void onResetConversation(@NonNull Conversation conversation, @NonNull ConversationService.ClearMode clearMode) {
            if (DEBUG) {
                Log.d(LOG_TAG, "ConversationServiceObserver.onResetConversation: conversation=" + conversation);
            }

            runOnUiThread(() -> TestService.this.onResetConversation(conversation));
        }


        @Override
        public void onInviteGroupRequest(long requestId, @NonNull Conversation conversation, @NonNull InvitationDescriptor invitation) {
            if (DEBUG) {
                Log.d(LOG_TAG, "ConversationServiceObserver.onInviteGroupResponse: requestId=" + requestId + " conversation=" + conversation);
            }

            runOnUiThread(() -> TestService.this.onInviteGroupCall(requestId, conversation, invitation));
        }

        @Override
        public void onJoinGroupRequest(long requestId, @NonNull GroupConversation conversation, @Nullable InvitationDescriptor invitation, @NonNull UUID memberId) {
            if (DEBUG) {
                Log.d(LOG_TAG, "ConversationServiceObserver.onJoinGroupRequest: requestId=" + requestId + " conversation=" + conversation);
            }

            runOnUiThread(() -> TestService.this.onJoinGroupCall(requestId, conversation, invitation));
        }

        /*
                @Override
                public void onInviteGroupUpdate(long requestId, @NonNull Conversation conversation, @NonNull InvitationDescriptor invitation) {
                    if (DEBUG) {
                        Log.d(LOG_TAG, "ConversationServiceObserver.onInviteGroupResponse: requestId=" + requestId + " conversation=" + conversation);
                    }
                     synchronized (mRequestIds) {
                        if (mRequestIds.remove(requestId) == null) {

                            Log.e(LOG_TAG, "onInviteGroupResponse is ignored");
                            return;
                        }
                    }

                    mActivity.runOnUiThread(() -> TestService.this.onInviteGroup(requestId, conversation, invitation));
                }
        */
        @Override
        public void onJoinGroupResponse(long requestId, @NonNull GroupConversation conversation, @Nullable InvitationDescriptor invitation) {
            if (DEBUG) {
                Log.d(LOG_TAG, "ConversationServiceObserver.onInviteGroupResponse: requestId=" + requestId + " conversation=" + conversation);
            }
            /* synchronized (mRequestIds) {
                if (mRequestIds.remove(requestId) == null) {

                    Log.e(LOG_TAG, "onJoinGroupResponse is ignored");
                    return;
                }
            }*/

            /* mActivity.runOnUiThread(() -> TestService.this.onJoinGroup(requestId, conversation, invitation)); */
        }

        @Override
        public void onError(long requestId, ErrorCode errorCode, String errorParameter) {
            if (DEBUG) {
                Log.d(LOG_TAG, "ConversationServiceObserver.onError: requestId=" + requestId + " errorCode=" + errorCode + " errorParameter=" + errorParameter);
            }

            Integer operationId;
            synchronized (mRequestIds) {
                operationId = mRequestIds.remove(requestId);
                if (operationId == null) {

                    return;
                }
            }

            runOnUiThread(() -> {
                TestService.this.onError(operationId, errorCode, errorParameter);
                onOperation();
            });
        }
    }

    private int mState = 0;
    private String mGroupName;

    private final ConversationServiceObserver mConversationServiceObserver;
    private final Map<UUID, UUID> mGroupConversations;
    private final Map<UUID, Group> mGroups = new HashMap<>();

    @NonNull
    private final Observer mObserver;

    @Nullable
    private Profile mProfile;
    private final List<UUID> mAddContacts = new ArrayList<>();
    @Nullable
    private UUID mAddContact;
    private Space mSpace;

    static final class GroupInviteMember {
        UUID groupId;
        UUID conversationId;
        String name;
        long requestId;

        GroupInviteMember(long requestId, UUID conversationId, UUID groupId, String name) {
            this.requestId = requestId;
            this.conversationId = conversationId;
            this.groupId = groupId;
            this.name = name;
        }
    }

    HashMap<Long, GroupInviteMember> mPendingJoins;

    public TestService(@NonNull AbstractTwinmeActivity activity, @NonNull TwinmeContext twinmeContext, @NonNull Observer observer) {
        super(LOG_TAG, activity, twinmeContext, observer);
        if (DEBUG) {
            Log.d(LOG_TAG, "TestService: activity=" + activity + " twinmeContext=" + twinmeContext + " observer=" + observer);
        }

        mObserver = observer;
        mPendingJoins = new HashMap<>();
        mGroupConversations = new HashMap<>();

        mTwinmeContextObserver = new TwinmeContextObserver();
        mConversationServiceObserver = new ConversationServiceObserver();

        mTwinmeContext.setObserver(mTwinmeContextObserver);
    }

    public void addContact(@NonNull UUID peerId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "addContact peer=" + peerId);
        }

        if (mAddContact == null) {
            mAddContact = peerId;
            mState &= ~(GET_TWINCODE | GET_TWINCODE_DONE);
        } else {
            mAddContacts.add(peerId);
        }
        onOperation();
    }

    public void createGroup(String name) {
        long requestId = newOperation(CREATE_GROUP);


        mGroupName = name;
        mTwinmeContext.createGroup(requestId, name, null, null, null);
    }

    public void createGroupMember(@NonNull UUID conversationId, String name, @NonNull UUID groupId) {
        long requestId = newOperation(CREATE_GROUP_MEMBER);

        mPendingJoins.put(requestId, new GroupInviteMember(requestId, conversationId, groupId, name));
        mTwinmeContext.createGroup(requestId, name, null, null, null);
    }

    private void onCreateGroup(long requestId, long operationId, @NonNull Group group, @NonNull GroupConversation conversation) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateGroup: group=" + group);
        }

        if (operationId == CREATE_GROUP) {
            if (DEBUG) {
                Log.d(LOG_TAG, "onInviteGroupResponse: groupId=" + conversation);
            }
            mGroupConversations.put(conversation.getTwincodeOutboundId(), conversation.getId());
            if (operationId == GET_GROUP) {
                mObserver.onGetOrCreateGroup(conversation);
            } else if (operationId == GET_GROUP_FOR_JOIN) {
                mObserver.onJoinGroup(conversation.getId(), conversation.getContactId());
            }
        } else if (operationId == CREATE_GROUP_MEMBER) {
            GroupInviteMember member = mPendingJoins.get(requestId);
            if (member != null) {
                mPendingJoins.remove(requestId);
                mGroups.put(group.getGroupTwincodeOutboundId(), group);
                mObserver.onCreateMember(member.conversationId, member.groupId, member.name, group);
            }
        }
    }

    @Override
    public void dispose() {
        if (DEBUG) {
            Log.d(LOG_TAG, "dispose");
        }

        if (mTwinmeContext.hasTwinlife()) {
            mTwinmeContext.getConversationService().removeServiceObserver(mConversationServiceObserver);
        }

        super.dispose();
    }

    //
    // Private methods
    //

    @Override
    protected void onOperation() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onOperation");
        }

        if (!mIsTwinlifeReady) {

            return;
        }

        //
        // Step 1
        //

        boolean completedStep1 = true;

        if ((mState & GET_DEFAULT_PROFILE) == 0) {
            mState |= GET_DEFAULT_PROFILE;
            long requestId = newOperation(GET_DEFAULT_PROFILE);
            if (DEBUG) {
                Log.d(LOG_TAG, "TwinmeContext.getDefaultProfile: requestId=" + requestId);
            }
            mTwinmeContext.getCurrentSpace((ErrorCode errorCode, Space space) -> {
                mSpace = space;
                onGetDefaultProfile(space.getProfile());
                onOperation();
            });

            mObserver.showProgressIndicator();
        }
        if ((mState & GET_DEFAULT_PROFILE_DONE) == 0) {
            completedStep1 = false;
        }

        if (!completedStep1) {

            return;
        }

        //
        // Step 2
        //

        boolean completedStep2 = true;

        if ((mState & GET_CONTACTS) == 0) {
            mState |= GET_CONTACTS;
            Filter<RepositoryObject> filter = new Filter<>(null);
            if (DEBUG) {
                Log.d(LOG_TAG, "TwinmeContext.findContacts: filter=" + filter);
            }
            mTwinmeContext.findContacts(filter, (List<Contact> contacts) -> {
                runOnUiThread(() -> {
                    mState |= GET_CONTACTS_DONE;
                    mObserver.onGetContacts(contacts);
                    onOperation();
                });
            });

            mObserver.showProgressIndicator();
        }
        if ((mState & GET_CONTACTS_DONE) == 0) {
            completedStep2 = false;
        }

        if (!completedStep2) {

            return;
        }

        //
        // Step 3
        //

        boolean completedStep3 = true;
        if ((mState & GET_CONVERSATIONS) == 0) {
            mState |= GET_CONVERSATIONS;
            completedStep3 = false;

            Filter filter = new Filter(mSpace);
            mTwinmeContext.findConversations(filter, (List<Conversation> conversations) -> {
                runOnUiThread(() -> {
                    onGetConversations(conversations);
                    onOperation();
                });
            });

            mObserver.showProgressIndicator();
        }
        if ((mState & GET_CONVERSATIONS_DONE) == 0) {
            completedStep3 = false;
        }

        if (!completedStep3) {

            return;
        }

        //
        // Step 4
        //

        boolean completedStep4 = true;
        if ((mState & GET_PENDING_NOTIFICATIONS) == 0) {
            mState |= GET_PENDING_NOTIFICATIONS;
            completedStep4 = false;

            if (DEBUG) {
                Log.d(LOG_TAG, "TwinmeContext.getPendingNotifications");
            }
            mTwinmeContext.getSpaceNotificationStats((BaseService.ErrorCode errorCode, NotificationStat stat) -> {
                onGetSpaceNotificationStats(stat);
                onOperation();
            });

            mObserver.showProgressIndicator();
        }
        if ((mState & GET_PENDING_NOTIFICATIONS_DONE) == 0) {
            completedStep4 = false;
        }

        if (!completedStep4) {

            return;
        }

        if (mAddContact != null) {
            boolean completedStep = true;

            if ((mState & GET_TWINCODE) == 0) {
                mState |= GET_TWINCODE;
                completedStep = false;

                if (DEBUG) {
                    Log.d(LOG_TAG, "TwincodeOutboundService.getTwincode: twincodeOutboundId=" + mAddContact);
                }
                mTwinmeContext.getTwincodeOutboundService().getTwincode(mAddContact, TwincodeOutboundService.REFRESH_PERIOD, (ErrorCode status, TwincodeOutbound twincodeOutbound) -> {
                    runOnUiThread(() -> {
                        onGetTwincodeOutbound(status, twincodeOutbound);
                        onOperation();
                    });
                });
            }
            if ((mState & GET_TWINCODE_DONE) == 0) {
                completedStep = false;
            }
            if (!completedStep) {

                return;
            }
        }

        //
        // Last Step
        //

        mObserver.hideProgressIndicator();
    }

    @Override
    protected void onTwinlifeReady() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onTwinlifeReady");
        }

        mTwinmeContext.getConversationService().addServiceObserver(mConversationServiceObserver);
        super.onTwinlifeReady();
    }

    private void onGetDefaultProfile(@NonNull Profile profile) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetDefaultProfile: profile=" + profile);
        }

        if ((mState & GET_DEFAULT_PROFILE_DONE) != 0) {

            return;
        }
        mState |= GET_DEFAULT_PROFILE_DONE;

        mProfile = profile;
        mObserver.onUpdateDefaultProfile(profile);
    }

    private void onCreateProfile(@NonNull Profile profile) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateProfile: profile=" + profile);
        }

        if (mTwinmeContext.isCurrentProfile(profile)) {
            mObserver.onUpdateDefaultProfile(profile);
        }
    }

    private void onUpdateProfile(@NonNull Profile profile) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateProfile: profile=" + profile);
        }

        if (mTwinmeContext.isCurrentProfile(profile)) {
            mObserver.onUpdateDefaultProfile(profile);
        }
    }

    private void onCreateContact(@NonNull Contact contact) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateContact: contact=" + contact);
        }

        mObserver.onCreateContact(contact);
    }

    private void onUpdateContact(@NonNull Contact contact) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateContact: contact=" + contact);
        }

        mObserver.onUpdateContact(contact);
    }

    private void onDeleteContact(@NonNull UUID contactId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteContact: contactId=" + contactId);
        }

        mObserver.onDeleteContact(contactId);
    }

    private void onDeleteAccount() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteAccount");
        }

        mObserver.onDeleteAccount();
    }

    private void onCreateTwincode(long operationId, long requestId, @NonNull TwincodeFactory twincodeFactory) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateTwincode: twincodeFactory=" + twincodeFactory);
        }
        /* GroupInviteMember member = mPendingJoins.get(requestId);
        if (member != null) {
            mPendingJoins.remove(requestId);
            mBaseObserver.onCreateMember(member.conversationId, member.groupId, member.name, twincodeFactory);
        } else if (operationId == CREATE_GROUP) {
            requestId = newOperation(GET_GROUP);
            mTwinmeContext.getConversationService().getOrCreateGroup(requestId, twincodeFactory.getTwincodeOutboundId(),
                    twincodeFactory.getTwincodeInboundId(), twincodeFactory.getTwincodeOutboundId());
            // mBaseObserver.onCreateTwincode(twincodeFactory);
        }*/
    }

    private void onGetTwincodeOutbound(@NonNull ErrorCode status, @Nullable TwincodeOutbound twincodeOutbound) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetTwincodeOutbound: twincodeOutbound=" + twincodeOutbound);
        }

        if (status != ErrorCode.SUCCESS || twincodeOutbound == null) {

            onError(GET_TWINCODE, status, mAddContact.toString());
            return;
        }

        if ((mState & GET_TWINCODE_DONE) != 0) {

            return;
        }
        mState |= GET_TWINCODE_DONE;

        mTwinmeContext.assertEqual(ServiceAssertPoint.INVALID_TWINCODE, twincodeOutbound.getId(), mAddContact);

        long requestId = newOperation(CREATE_CONTACT);

        mTwinmeContext.createContactPhase1(requestId, twincodeOutbound, null, mProfile, null);

        if (mAddContacts.isEmpty()) {
            mAddContact = null;
        } else {
            mAddContact = mAddContacts.remove(0);
            mState &= ~(GET_TWINCODE);
            mState &= ~(GET_TWINCODE_DONE);
        }
    }

    private void onGetConversations(@NonNull List<Conversation> conversations) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetConversations: conversations=" + conversations);
        }

        if ((mState & GET_CONVERSATIONS_DONE) != 0) {

            return;
        }
        mState |= GET_CONVERSATIONS_DONE;

        mObserver.onGetConversations(conversations);
    }

    private void onDeleteConversation(@NonNull UUID conversationId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteConversation: conversationId=" + conversationId);
        }

        mObserver.onDeleteConversation(conversationId);
    }

    private void onGetOrCreateConversation(@NonNull Conversation conversation) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetOrCreateConversation: conversation=" + conversation);
        }

        if (conversation.getContactId() == null) {

            return;
        }

        mObserver.onGetOrCreateConversation(conversation);
    }

    private void onResetConversation(@NonNull Conversation conversation) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResetConversation: conversation=" + conversation);
        }

        mObserver.onResetConversation(conversation);
    }

    private void onGetSpaceNotificationStats(NotificationStat notificationStat) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetPendingNotifications: notificationStat=" + notificationStat);
        }

        if ((mState & GET_PENDING_NOTIFICATIONS_DONE) != 0) {

            return;
        }
        mState |= GET_PENDING_NOTIFICATIONS_DONE;

        mObserver.onUpdatePendingNotifications(notificationStat.getPendingCount() > 0);
    }

    private void onUpdatePendingNotifications(boolean hasPendingNotifications) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdatePendingNotifications: hasPendingNotifications=" + hasPendingNotifications);
        }

        mObserver.onUpdatePendingNotifications(hasPendingNotifications);
    }

    private void onInviteGroupCall(long requestId, @NonNull Conversation conversation, @NonNull InvitationDescriptor invitation) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onInviteGroupRequest: groupId=" + invitation.getGroupTwincodeId() + " name=" + invitation.getName());
        }

        mObserver.onInviteGroupCall(conversation.getId(), invitation.getGroupTwincodeId(), invitation.getName());
    }

    private void onJoinGroupCall(long requestId, @NonNull Conversation conversation, @NonNull InvitationDescriptor invitation) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onJoinGroupRequest: groupId=" + conversation + " memberId=" + invitation);
        }
        Log.e(LOG_TAG, "onJoinGroupRequest: groupId=" + conversation + " memberId=" + invitation);

        UUID c = mGroupConversations.get(invitation.getGroupTwincodeId());
        if (c != null) {
            // mTwinmeContext.getConversationService().addMember(c, memberId);
            mObserver.onJoinGroupCall(c, invitation.getGroupTwincodeId(), invitation.getMemberTwincodeId(), true);
        }
    }

    private void onInviteGroup(long requestId, @NonNull Conversation conversation, @NonNull InvitationDescriptor invitation) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onInviteGroupResponse: groupId=" + invitation.getGroupTwincodeId() + " name=" + invitation.getName());
        }

        mObserver.onInviteGroup(conversation.getId(), invitation.getGroupTwincodeId(), invitation.getName());
    }

    @Override
    protected void onError(int operationId, ErrorCode errorCode, @Nullable String errorParameter) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onError: operationId=" + operationId + " errorCode=" + errorCode + " errorParameter=" + errorParameter);
        }

        // Wait for reconnection
        if (errorCode == ErrorCode.TWINLIFE_OFFLINE) {
            mRestarted = true;

            return;
        }

        if (operationId == GET_DEFAULT_PROFILE) {
            mState |= GET_DEFAULT_PROFILE_DONE;

            mObserver.hideProgressIndicator();

            if (errorCode == ErrorCode.ITEM_NOT_FOUND) {
                mObserver.onGetDefaultProfileNotFound();

                return;
            }
        }
        if (operationId == GET_TWINCODE && errorCode == ErrorCode.ITEM_NOT_FOUND) {

            if (mAddContacts.isEmpty()) {
                mAddContact = null;
                mState |= GET_TWINCODE_DONE;
            } else {
                mAddContact = mAddContacts.remove(0);
                mState &= ~(GET_TWINCODE);
                mState &= ~(GET_TWINCODE_DONE);
            }
            return;
        }


        // mTwinmeContext.sendProblemReport(LOG_TAG, "onError:" + "\n" +
        //        " operationId=" + operationId + "\n" +
        //        " errorCode=" + errorCode + "\n" +
        //        " errorParameter=" + errorParameter + "\n");
    }
}
