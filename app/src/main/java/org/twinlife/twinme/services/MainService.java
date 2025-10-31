/*
 *  Copyright (c) 2017-2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.services;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.twinlife.twinlife.AssertPoint;
import org.twinlife.twinlife.BaseService;
import org.twinlife.twinlife.BaseService.ErrorCode;
import org.twinlife.twinlife.BuildConfig;
import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinlife.DisplayCallsMode;
import org.twinlife.twinlife.Filter;
import org.twinlife.twinlife.NotificationService.NotificationStat;
import org.twinlife.twinlife.RepositoryObject;
import org.twinlife.twinlife.TrustMethod;
import org.twinlife.twinlife.TwincodeURI;
import org.twinlife.twinlife.util.Utils;
import org.twinlife.twinme.TwinmeContext;
import org.twinlife.twinme.models.CallReceiver;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Profile;
import org.twinlife.twinme.models.Space;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainService extends AbstractTwinmeService {
    private static final String LOG_TAG = "MainService";
    private static final boolean DEBUG = false;

    private static final int GET_CURRENT_SPACE = 1;
    private static final int GET_CURRENT_SPACE_DONE = 1 << 1;
    private static final int GET_PROFILES = 1 << 2;
    private static final int GET_PROFILES_DONE = 1 << 3;
    private static final int UPDATE_SPACE = 1 << 4;
    private static final int UPDATE_SPACE_DONE = 1 << 5;
    private static final int GET_PENDING_NOTIFICATIONS = 1 << 10;
    private static final int GET_PENDING_NOTIFICATIONS_DONE = 1 << 11;
    private static final int GET_TRANSFER_CALL = 1 << 12;
    private static final int GET_TRANSFER_CALL_DONE = 1 << 13;
    private static final int GET_CONVERSATIONS = 1 << 14;
    private static final int GET_CONVERSATIONS_DONE = 1 << 15;
    private static final int GET_CONTACTS = 1 << 16;
    private static final int GET_CONTACTS_DONE = 1 << 17;
    private static final int SET_SPACE = 1 << 22;
    private static final int SET_SPACE_DONE = 1 << 23;
    private static final int GET_SPACE = 1 << 24;
    private static final int GET_SPACE_DONE = 1 << 25;

    public interface Observer extends AbstractTwinmeService.Observer {

        void onSignIn();

        void onSignOut();

        void onFatalError(@NonNull ErrorCode errorCode);

        void onSetCurrentSpace(@NonNull Space space);

        void onGetProfileNotFound();

        void onUpdateSpace(@NonNull Space space);

        void onUpdateProfile(@NonNull Profile profile);

        void onGetProfiles(@NonNull List<Profile> profiles);

        void onCreateProfile(@NonNull Profile profile);

        void onDeleteProfile(@NonNull UUID profileId);

        void onUpdatePendingNotifications(boolean hasPendingNotification);

        void onGetTransferCall(@NonNull CallReceiver callReceiver);

        void onDeleteTransferCall(@NonNull UUID callReceiverId);

        void onGetContacts(int nbContacts);

        void onGetConversations(int nbConversations);
    }

    private class TwinmeContextObserver extends AbstractTwinmeService.TwinmeContextObserver {

        @Override
        public void onSignIn() {
            if (DEBUG) {
                Log.d(LOG_TAG, "TwinmeContextObserver.onSignIn");
            }

            runOnUiThread(MainService.this::onSignIn);
        }

        @Override
        public void onSignOut() {
            if (DEBUG) {
                Log.d(LOG_TAG, "TwinmeContextObserver.onSignOut");
            }

            runOnUiThread(MainService.this::onSignOut);
        }

        @Override
        public void onSignInError(@NonNull ErrorCode errorCode) {
            if (DEBUG) {
                Log.d(LOG_TAG, "TwinmeContextObserver.onSignInError errorCode=" + errorCode);
            }

            runOnUiThread(() -> MainService.this.onFatalError(errorCode));
        }

        @Override
        public void onFatalError(BaseService.ErrorCode errorCode) {
            if (DEBUG) {
                Log.d(LOG_TAG, "TwinmeContextObserver.onFatalError: errorCode=" + errorCode);
            }

            runOnUiThread(() -> MainService.this.onFatalError(errorCode));
        }

        public void onCreateProfile(long requestId, @NonNull Profile profile) {
            if (DEBUG) {
                Log.d(LOG_TAG, "TwinmeContextObserver.onCreateProfile: requestId=" + requestId + " profile=" + profile);
            }

            MainService.this.onCreateProfile(profile);
            onOperation();
        }

        @Override
        public void onUpdateProfile(long requestId, @NonNull Profile profile) {
            if (DEBUG) {
                Log.d(LOG_TAG, "TwinmeContextObserver.onUpdateProfile: requestId=" + requestId + " profile=" + profile);
            }

            MainService.this.onUpdateProfile(profile);
        }

        @Override
        public void onDeleteProfile(long requestId, @NonNull UUID profileId) {
            if (DEBUG) {
                Log.d(LOG_TAG, "TwinmeContextObserver.onDeleteProfile: requestId=" + requestId + " groupId=" + profileId);
            }

            MainService.this.onDeleteProfile(profileId);
            onOperation();
        }

        @Override
        public void onUpdateSpace(long requestId, @NonNull Space space) {
            if (DEBUG) {
                Log.d(LOG_TAG, "TwinmeContextObserver.onUpdateSpace: requestId=" + requestId + " space=" + space);
            }

            MainService.this.onUpdateSpace(space);
        }

        @Override
        public void onUpdatePendingNotifications(long requestId, boolean hasPendingNotifications) {
            if (DEBUG) {
                Log.d(LOG_TAG, "TwinmeContextObserver.onUpdatePendingNotifications: requestId=" + requestId + " hasPendingNotifications=" + hasPendingNotifications);
            }

            MainService.this.onUpdatePendingNotifications(hasPendingNotifications);
        }

        @Override
        public void onSetCurrentSpace(long requestId, @NonNull Space space) {
            if (DEBUG) {
                Log.d(LOG_TAG, "TwinmeContextObserver.onSetCurrentSpace: requestId=" + requestId + " space=" + space);
            }

            MainService.this.onSetSpace(space);
            onOperation();
        }

        @Override
        public void onCreateCallReceiver(long requestId, @NonNull CallReceiver callReceiver) {
            if (DEBUG) {
                Log.d(LOG_TAG, "TwinmeContextObserver.onCreateCallReceiver: requestId=" + requestId + " callReceiver=" + callReceiver);
            }

            MainService.this.onCreateCallReceiver(callReceiver);
        }

        @Override
        public void onDeleteCallReceiver(long requestId, @NonNull UUID callReceiverId) {
            if (DEBUG) {
                Log.d(LOG_TAG, "TwinmeContextObserver.onDeleteCallReceiver: requestId=" + requestId + " callReceiverId=" + callReceiverId);
            }

            MainService.this.onDeleteCallReceiver(callReceiverId);
        }
    }

    @NonNull
    private final Observer mObserver;

    private int mState = 0;
    private int mWork = 0;
    private UUID mSpaceId;
    private Space mSpace;
    private Profile mProfile;

    public MainService(@NonNull AbstractTwinmeActivity activity, @NonNull TwinmeContext twinmeContext, @NonNull Observer observer) {
        super(LOG_TAG, activity, twinmeContext, observer);
        if (DEBUG) {
            Log.d(LOG_TAG, "MainService: activity=" + activity + " twinmeContext=" + twinmeContext + " observer=" + observer);
        }

        mObserver = observer;

        mTwinmeContextObserver = new TwinmeContextObserver();

        mTwinmeContext.setObserver(mTwinmeContextObserver);

        showProgressIndicator();
    }

    public void getPendingNotifications() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getPendingNotifications");
        }

        mState &= ~(GET_PENDING_NOTIFICATIONS | GET_PENDING_NOTIFICATIONS_DONE);
        startOperation();
    }

    public void getContacts() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getContacts");
        }

        mState &= ~(GET_CONTACTS | GET_CONTACTS_DONE);
        startOperation();
    }

    public void getConversations() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getConversations");
        }

        mState &= ~(GET_CONVERSATIONS | GET_CONVERSATIONS_DONE);
        startOperation();
    }

    public void setSpace(@NonNull UUID spaceId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setSpace: spaceId= " + spaceId);
        }

        mSpaceId = spaceId;
        mState &= ~(GET_SPACE | GET_SPACE_DONE);
        startOperation();
    }

    public void activeProfile(@NonNull Profile profile) {
        if (DEBUG) {
            Log.d(LOG_TAG, "activeProfile: profile=" + profile);
        }

        mProfile = profile;
        mWork = UPDATE_SPACE;
        mState &= ~(UPDATE_SPACE | UPDATE_SPACE_DONE);
        showProgressIndicator();
        startOperation();
    }

    public void verifyAuthenticateURI(@NonNull Uri uri, @NonNull TwinmeContext.ConsumerWithError<Contact> complete) {
        if (DEBUG) {
            Log.d(LOG_TAG, "verifyAuthenticateURI: uri=" + uri);
        }

        parseURI(uri, (ErrorCode errorCode, TwincodeURI twincodeURI) -> {
            if (errorCode != ErrorCode.SUCCESS) {
                complete.onGet(errorCode, null);
            } else {
                mTwinmeContext.verifyContact(twincodeURI, TrustMethod.LINK, (ErrorCode lErrorCode, Contact contact) -> runOnUiThread(() -> {
                    complete.onGet(lErrorCode, contact);
                }));
            }
        });
    }

    //
    // Private methods
    //

    @Override
    protected void onOperation() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onOperation");
        }

        if (BuildConfig.ENABLE_CHECKS && Utils.isMainThread()) {
            mTwinmeContext.assertion(ServiceAssertPoint.MAIN_THREAD, AssertPoint.create(getClass()).putMarker(312));
        }
        if (!mIsTwinlifeReady) {

            return;
        }

        //
        // Step 1: get the current space.
        //

        if ((mState & GET_CURRENT_SPACE) == 0) {
            mState |= GET_CURRENT_SPACE;

            mTwinmeContext.getCurrentSpace((ErrorCode errorCode, Space space) -> {
                mSpace = space;
                runOnUiThread(() -> {
                    if (space != null) {
                        mObserver.onSetCurrentSpace(space);
                    } else {
                        mObserver.onGetProfileNotFound();
                    }
                });
                mState |= GET_CURRENT_SPACE_DONE;
                onOperation();
            });
            return;
        }
        if ((mState & GET_CURRENT_SPACE_DONE) == 0) {
            return;
        }

        //
        // Step 2: get profiles.
        //

        if ((mState & GET_PROFILES) == 0) {
            mState |= GET_PROFILES;

            long requestId = newOperation(GET_PROFILES);
            if (DEBUG) {
                Log.d(LOG_TAG, "TwinmeContext.getProfiles: requestId=" + requestId);
            }
            mTwinmeContext.getProfiles(requestId, (List<Profile> list) -> {
                finishOperation(requestId);
                onGetProfiles(list);
                onOperation();
            });
            return;
        }
        if ((mState & GET_PROFILES_DONE) == 0) {
            return;
        }

        //
        // Step 3
        //

        if ((mState & GET_PENDING_NOTIFICATIONS) == 0) {
            mState |= GET_PENDING_NOTIFICATIONS;

            if (DEBUG) {
                Log.d(LOG_TAG, "TwinmeContext.getPendingNotifications");
            }

            mTwinmeContext.getSpaceNotificationStats((BaseService.ErrorCode errorCode, NotificationStat stat) -> {
                onGetSpaceNotificationStats(stat);
                onOperation();
            });
            return;
        }
        if ((mState & GET_PENDING_NOTIFICATIONS_DONE) == 0) {
            return;
        }

        //
        // Step 4
        //

        if ((mState & GET_TRANSFER_CALL) == 0) {
            mState |= GET_TRANSFER_CALL;

            Filter<RepositoryObject> filter = new Filter<RepositoryObject>(null) {
                @Override
                public boolean accept(@NonNull RepositoryObject object) {
                    if (!(object instanceof CallReceiver)) {
                        return false;
                    }

                    final CallReceiver callReceiver = (CallReceiver) object;
                    return callReceiver.isTransfer();
                }
            };
            if (DEBUG) {
                Log.d(LOG_TAG, "TwinmeContext.findCallReceivers: filter=" + filter);
            }
            mTwinmeContext.findCallReceivers(filter,
                    (List<CallReceiver> callReceivers) -> {
                        onGetTransferCall(callReceivers);
                        onOperation();
                    });
            return;
        }
        if ((mState & GET_TRANSFER_CALL_DONE) == 0) {
            return;
        }

        //
        // Step 5: get conversations.
        //

        if ((mState & GET_CONVERSATIONS) == 0 && mSpace != null) {
            mState |= GET_CONVERSATIONS;

            long requestId = newOperation(GET_CONVERSATIONS);
            if (DEBUG) {
                Log.d(LOG_TAG, "TwinmeContext.getContacts: requestId=" + requestId);
            }
            mTwinmeContext.findConversationDescriptors(new Filter<>(mSpace), DisplayCallsMode.ALL,
                    (Map<ConversationService.Conversation, ConversationService.Descriptor> conversations) -> {
                finishOperation(requestId);
                onGetConversations(conversations.size());
                onOperation();
            });
            return;
        }
        if ((mState & GET_CONVERSATIONS_DONE) == 0) {
            return;
        }


        //
        // Step 6: get contacts.
        //

        if ((mState & GET_CONTACTS) == 0) {
            mState |= GET_CONTACTS;

            long requestId = newOperation(GET_CONTACTS);
            if (DEBUG) {
                Log.d(LOG_TAG, "TwinmeContext.getContacts: requestId=" + requestId);
            }

            final Filter<RepositoryObject> filter = mTwinmeContext.createSpaceFilter();
            mTwinmeContext.findContacts(filter, (List<Contact> contacts) -> {
                finishOperation(requestId);
                onGetContacts(contacts);
                onOperation();
            });
            return;
        }
        if ((mState & GET_CONTACTS_DONE) == 0) {
            return;
        }

        if (mSpaceId != null) {
            if ((mState & GET_SPACE) == 0) {
                mState |= GET_SPACE;

                mTwinmeContext.getSpace(mSpaceId, (ErrorCode errorCode, Space space) -> {
                    mSpace = space;
                    mState |= GET_SPACE_DONE;
                    mState &= ~(SET_SPACE | SET_SPACE_DONE);
                    onOperation();
                });
                return;

            }
            if ((mState & GET_SPACE_DONE) == 0) {
                return;
            }
        }

        if (mSpace != null) {
            if ((mState & SET_SPACE) == 0) {
                mState |= SET_SPACE;

                long requestId = newOperation(SET_SPACE);
                if (DEBUG) {
                    Log.d(LOG_TAG, "TwinmeContext.setCurrentSpace: requestId=" + requestId);
                }
                mTwinmeContext.setCurrentSpace(requestId, mSpace);
                return;
            }
            if ((mState & SET_SPACE_DONE) == 0) {
                return;
            }
        }

        //
        // We must update the current profile.
        //
        if (mProfile != null && mSpace != null && (mWork & UPDATE_SPACE) != 0) {
            if ((mState & UPDATE_SPACE) == 0) {
                mState |= UPDATE_SPACE;

                long requestId = newOperation(UPDATE_SPACE);
                if (DEBUG) {
                    Log.d(LOG_TAG, "TwinmeContext.updateSpace: requestId=" + requestId + " space=" + mSpace + " profile=" + mProfile);
                }
                mTwinmeContext.updateSpace(requestId, mSpace, mProfile);
            }
            if ((mState & UPDATE_SPACE_DONE) == 0) {
                return;
            }
        }

        //
        // Last Step
        //

        hideProgressIndicator();
    }

    private void onSignIn() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSignIn");
        }

        mObserver.onSignIn();
    }

    private void onSignOut() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSignOut");
        }

        mState = 0;

        mObserver.onSignOut();
    }

    private void onFatalError(@NonNull ErrorCode errorCode) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSignInError errorCode=" + errorCode);
        }

        mState = 0;

        mObserver.onFatalError(errorCode);
    }

    private void onUpdateProfile(@NonNull Profile profile) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateProfile: profile=" + profile);
        }

        runOnUiThread(() -> mObserver.onUpdateProfile(profile));
    }

    private void onGetProfiles(@NonNull List<Profile> profiles) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetProfiles: profiles=" + profiles);
        }

        mState |= GET_PROFILES_DONE;

        runOnUiThread(() -> mObserver.onGetProfiles(profiles));
    }

    private void onCreateProfile(@NonNull Profile profile) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateProfile profile=" + profile);
        }

        runOnUiThread(() -> mObserver.onCreateProfile(profile));
    }

    private void onDeleteProfile(@NonNull UUID profileId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteProfile: profileId=" + profileId);
        }

        runOnUiThread(() -> mObserver.onDeleteProfile(profileId));
    }

    private void onGetTransferCall(@NonNull List<CallReceiver> callReceivers) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetTransferCall: callReceivers=" + callReceivers);
        }

        mState |= GET_TRANSFER_CALL_DONE;

        if (!callReceivers.isEmpty()) {
            runOnUiThread(() -> mObserver.onGetTransferCall(callReceivers.get(0)));
        }
    }

    private void onUpdateSpace(@NonNull Space space) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateSpace: space=" + space);
        }

        runOnUiThread(() -> mObserver.onUpdateSpace(space));
    }

    private void onSetSpace(@NonNull Space space) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSetSpace: space=" + space);
        }

        mState |= SET_SPACE_DONE;

        onSetCurrentSpace(space);
    }

    private void onGetSpaceNotificationStats(NotificationStat notificationStat) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetPendingNotifications: notificationStat=" + notificationStat);
        }

        mState |= GET_PENDING_NOTIFICATIONS_DONE;
        runOnUiThread(() -> mObserver.onUpdatePendingNotifications(notificationStat.getPendingCount() > 0));
    }

    private void onUpdatePendingNotifications(boolean hasPendingNotifications) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdatePendingNotifications: hasPendingNotifications=" + hasPendingNotifications);
        }

        runOnUiThread(() -> mObserver.onUpdatePendingNotifications(hasPendingNotifications));
    }

    private void onCreateCallReceiver(@NonNull CallReceiver callReceiver) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateCallReceiver: callReceiver=" + callReceiver);
        }

        if (callReceiver.isTransfer()) {
            runOnUiThread(() -> mObserver.onGetTransferCall(callReceiver));
        }
    }

    private void onDeleteCallReceiver(@NonNull UUID callReceiverId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteCallReceiver: callReceiverId=" + callReceiverId);
        }

        runOnUiThread(() -> mObserver.onDeleteTransferCall(callReceiverId));
    }

    private void onGetContacts(@NonNull List<Contact> contacts) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContacts: contacts=" + contacts);
        }

        mState |= GET_CONTACTS_DONE;

        runOnUiThread(() -> mObserver.onGetContacts(contacts.size()));
    }

    private void onGetConversations(int nbConversations) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetConversations: conversations=" + nbConversations);
        }

        mState |= GET_CONVERSATIONS_DONE;

        runOnUiThread(() -> mObserver.onGetConversations(nbConversations));
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

        if (operationId == GET_CURRENT_SPACE) {
            mState |= GET_CURRENT_SPACE_DONE;

            if (errorCode == ErrorCode.ITEM_NOT_FOUND) {

                return;
            }
        }

        super.onError(operationId, errorCode, errorParameter);
    }
}
