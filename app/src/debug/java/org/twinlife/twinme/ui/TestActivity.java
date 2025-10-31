/*
 *  Copyright (c) 2018 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */
package org.twinlife.twinme.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinlife.Filter;
import org.twinlife.twinlife.RepositoryObject;
import org.twinlife.twinlife.util.EventMonitor;
import org.twinlife.twinme.TwinmeContext;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Group;
import org.twinlife.twinme.models.Profile;
import org.twinlife.twinme.services.ServiceAssertPoint;
import org.twinlife.twinme.services.TestService;

import java.util.List;
import java.util.UUID;

/**
 * Activity to perform various tests useful during the development.
 */
public class TestActivity extends AbstractTwinmeActivity implements TestService.Observer {
    private static final boolean DEBUG = true;
    private static final String LOG_TAG = "TestActivity";
    private TestService mTestService;
    private List<ConversationService.Conversation> mConversations;
    private TwinmeContext mTwinmeContext;
    private int mMsgId = 0;
    private EditText mMessageView;
    private boolean mTestingContacts = false;
    private boolean mTestingDeleteContacts = false;
    private int mContactCount = 0;
    private List<Contact> mContacts;
    private static final int TEST_CONTACTS_OPERATION = 0x1000;
    private static final int TEST_DELETE_CONTACTS_OPERATION = 0x2000;
    private static final int TEST_DELETE_ONE_CONTACT_OPERATION = 0x4000;
    private static final int TEST_DELETE_ACCOUNT = 0x8000;
    private UUID mInvitedGroup;
    private UUID mInvitedConversation;
    private String mGroupName;
    private long mDeleteAccountStartTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();

        mTwinmeContext = getTwinmeContext();
        mTestService = new TestService(this, mTwinmeContext, this);
    }

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        setContentView(R.layout.test_activity);
        final View backClickableView = findViewById(R.id.test_activity_back_clickable_view);
        backClickableView.setOnClickListener(v -> onBackClick());

        mMessageView = findViewById(R.id.test_message_text);
        final View broadcastButton = findViewById(R.id.test_broadcast_button);
        broadcastButton.setOnClickListener(v -> {
            mMsgId++;
            String message = mMessageView.getText().toString();
            TestActivity.this.broadcastMessage(message);
        });

        final View testContactsButton = findViewById(R.id.test_create_contact_button);
        testContactsButton.setOnClickListener(v -> TestActivity.this.onTestCreateContacts());

        final View testDeleteContactsButton = findViewById(R.id.test_delete_contact_button);
        testDeleteContactsButton.setOnClickListener(v -> {
            TestActivity.this.onTestDeleteContacts();
        });

        final View testCreateGroupButton = findViewById(R.id.test_create_group_button);
        testCreateGroupButton.setOnClickListener(v -> {
            TestActivity.this.onTestCreateGroup();
        });

        final View testDeleteAccountButton = findViewById(R.id.test_delete_account_button);
        testDeleteAccountButton.setOnClickListener(v -> {
            TestActivity.this.onTestDeleteAccount();
        });
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        mTestService.dispose();

        super.onDestroy();
    }

    private void broadcastMessage(String message) {
        if (mConversations != null) {
            for (ConversationService.Conversation conversation : mConversations) {
                getTwinmeContext().assertNotNull(ServiceAssertPoint.NULL_SUBJECT,  conversation.getContactId(), 117);

                long requestId = mTestService.newOperation(0x1000000);
                if (DEBUG) {
                    Log.d(LOG_TAG, "ConversationService.pushObject: requestId=" + requestId + " conversationId=" + conversation.getId() + " message=" + message);
                }
                mTwinmeContext.getConversationService().pushMessage(requestId, conversation, null, null, message, true, 0);
            }
        }
    }

    private void onTestDeleteAccount() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onTestDeleteAccount");
        }

        final long requestId = mTestService.newOperation(TEST_DELETE_ACCOUNT);

        EventMonitor.info(LOG_TAG, "Deleting account");
        mDeleteAccountStartTime = System.currentTimeMillis();
        mTwinmeContext.deleteAccount(requestId);
    }

    private void onTestCreateContacts() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onTestCreateContacts");
        }

        if (!mTestingContacts) {

            Filter<RepositoryObject> filter = new Filter<>(null);
            mTestingContacts = true;
            mTwinmeContext.findContacts(filter, (List<Contact> contacts) -> {
                mContactCount = contacts.size();
                mContacts = contacts;
                for (Contact contact : contacts) {
                    UUID peerId = contact.getPublicPeerTwincodeOutboundId();
                    if (peerId != null) {
                        for (int i = 0; i < 5; i++) {
                            EventMonitor.event("Add " + contact.getPeerName());
                            mTestService.addContact(peerId);
                        }
                        //mTwinmeContext.createContactPhase1(requestId, peerId, contact.getPeerName() + " F",
                        //        contact.getPeerAvatar());
                    }
                }
                mTestingContacts = false;
            });
        }
    }

    private void onTestDeleteContacts() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onTestDeleteContacts");
        }
        if (!mTestingDeleteContacts) {

            mTestingDeleteContacts = true;
            Filter<RepositoryObject> filter = new Filter<>(null);
            mTwinmeContext.findContacts(filter, (List<Contact> contacts) -> {
                mContactCount = contacts.size();
                mContacts = contacts;
                for (Contact contact : contacts) {
                    boolean foundConversation = false;

                    for (ConversationService.Conversation conversation : mConversations) {
                        if (conversation.getContactId().equals(contact.getId())) {
                            foundConversation = true;
                            break;
                        }
                    }
                    if (!foundConversation) {
                        EventMonitor.event("Delete " + contact.getPeerName());
                        long requestId2 = mTestService.newOperation(TEST_DELETE_ONE_CONTACT_OPERATION);
                        mTwinmeContext.deleteContact(requestId2, contact);
                    }
                }
                mTestingContacts = false;
            });
        }
    }

    @Override
    public void onGetConversations(@NonNull List<ConversationService.Conversation> conversations) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetConversations: conversations=" + conversations);
        }

        mConversations = conversations;
    }

    @Override
    public void onDeleteAccount() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteAccount");
        }

        EventMonitor.event("Delete account", mDeleteAccountStartTime);
    }

    public void showProgressIndicator() {

    }

    public void hideProgressIndicator() {

    }

    public void onConnect() {

    }

    public void onDisconnect() {

    }

    public void onUpdateDefaultProfile(@NonNull Profile profile) {

    }

    public void onGetDefaultProfileNotFound() {

    }

    public void onGetContacts(@NonNull List<Contact> contacts) {
        Log.e(LOG_TAG, "Getting contacts...");

        mContactCount = contacts.size();
        mContacts = contacts;
        if (mTestingContacts) {
            for (Contact contact : contacts) {
                UUID peerId = contact.getPublicPeerTwincodeOutboundId();
                if (peerId != null) {
                    for (int i = 0; i < 5; i++) {
                        EventMonitor.event("Add " + contact.getPeerName());
                        mTestService.addContact(peerId);
                    }
                    //mTwinmeContext.createContactPhase1(requestId, peerId, contact.getPeerName() + " F",
                    //        contact.getPeerAvatar());
                }
            }
            mTestingContacts = false;

        } else if (mTestingDeleteContacts) {
            for (Contact contact : contacts) {
                boolean foundConversation = false;

                for (ConversationService.Conversation conversation : mConversations) {
                    if (conversation.getContactId().equals(contact.getId())) {
                        foundConversation = true;
                        break;
                    }
                }
                if (!foundConversation) {
                    EventMonitor.event("Delete " + contact.getPeerName());
                    long requestId = mTestService.newOperation(TEST_DELETE_ONE_CONTACT_OPERATION);
                    mTwinmeContext.deleteContact(requestId, contact);
                }
            }
            mTestingContacts = false;
        }

    }

    public void onCreateContact(@NonNull Contact contact) {
        Log.e(LOG_TAG, "Contact created " + contact);
    }

    public void onUpdateContact(@NonNull Contact contact) {

    }

    public void onDeleteContact(@NonNull UUID contactId) {

    }

    private void onTestCreateGroup() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onTestCreateGroup");
        }
        UUID id = UUID.randomUUID();
        mGroupName = "Test group" + " " + id.toString().substring(0, 4);
        mTestService.createGroup(mGroupName);
    }

    @Override
    public void onGetOrCreateGroup(@NonNull ConversationService.GroupConversation group) {
        Log.e(LOG_TAG, "Created group " + group.toString().substring(1, 6));

        for (Contact contact : mContacts) {
            if (!contact.isGroup()) {
                ConversationService.Conversation conversation = mTwinmeContext.getConversationService().getConversation(contact);
                UUID cid = conversation.getId();
                long requestId = mTestService.newOperation(0x100000);
                EventMonitor.event("Invite " + cid.toString().substring(0, 6) + " in " + group.getTwincodeOutboundId().toString().substring(0, 6));
                mTwinmeContext.getConversationService().inviteGroup(requestId, conversation, group.getSubject(), mGroupName);
            }
        }
    }

    @Override
    public void onCreateMember(@NonNull UUID conversationId, @NonNull UUID groupId, @NonNull String name, @NonNull Group group) {
        /* EventMonitor.event("Join group " + groupId.toString().substring(0, 6) + " m=" + group.getTwincodeOutboundId().toString().substring(0, 6));
        long requestId = mTestService.newOperation(0x4000);
        mTwinmeContext.getConversationService().joinGroup(requestId, conversationId, groupId, group.getTwincodeInboundId(),
                group.getTwincodeOutboundId(), group.getId());*/
    }

    @Override
    public void onInviteGroupCall(@NonNull UUID conversationId, @NonNull UUID groupId, @NonNull String name) {
        EventMonitor.event("Invite received from " + conversationId.toString().substring(0, 6) + " for " + groupId.toString().substring(0, 6));

        mTestService.createGroupMember(conversationId, name, groupId);
    }

    @Override
    public void onJoinGroupCall(@NonNull UUID groupConversationId, @NonNull UUID groupId, @NonNull UUID memberId, boolean accepted) {
        EventMonitor.event("M " + memberId.toString().substring(0, 6) + " joined " + groupId.toString().substring(0, 6));

        long requestId = mTestService.newOperation(0x8000);
        // mTwinmeContext.getConversationService().pushMessage(requestId, groupConversationId, null, null,
        //        "Welcome " + memberId.toString().substring(0, 6) + " in " + groupId.toString().substring(0, 6), true, 0);
    }

    @Override
    public void onJoinGroup(@NonNull UUID groupConversationId, @NonNull UUID groupId) {
        EventMonitor.event("Member joined " + groupId.toString().substring(0, 6));

        long requestId = mTestService.newOperation(0x8000);
        // mTwinmeContext.getConversationService().pushMessage(requestId, groupConversationId, null, null,
        //        "Joined " + groupId.toString().substring(0, 6), true, 0);
    }

    @Override
    public void onInviteGroup(@NonNull UUID conversationId, @NonNull UUID groupId, @NonNull String name) {
        EventMonitor.event("Invitation sent for " + conversationId.toString().substring(0, 6) + " for group " + groupId.toString().substring(0, 6));

        // long requestId = mTestService.newOperation(0x8000);
        // mTwinmeContext.getConversationService().pushObject(requestId, conversationId, new Message("Invited!"));

    }

    public void onGetOrCreateConversation(@NonNull ConversationService.Conversation conversation) {

    }

    public void onResetConversation(@NonNull ConversationService.Conversation conversation) {

    }

    public void onDeleteConversation(@NonNull UUID conversationId) {

    }

    public void onUpdatePendingNotifications(boolean hasPendingNotification) {

    }

}
