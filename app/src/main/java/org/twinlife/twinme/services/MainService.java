/*
 *  Copyright (c) 2017-2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.services;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.twinlife.twinlife.AccountService;
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
    private static final int SUBSCRIBE_FEATURE = 1 << 2;
    private static final int SUBSCRIBE_FEATURE_DONE = 1 << 3;
    private static final int GET_SPACES = 1 << 4;
    private static final int GET_SPACES_DONE = 1 << 5;
    private static final int GET_PENDING_NOTIFICATIONS = 1 << 10;
    private static final int GET_PENDING_NOTIFICATIONS_DONE = 1 << 11;
    private static final int GET_SPACES_NOTIFICATIONS = 1 << 12;
    private static final int GET_SPACES_NOTIFICATIONS_DONE = 1 << 13;
    private static final int SET_LEVEL = 1 << 16;
    private static final int CREATE_LEVEL = 1 << 18;
    private static final int DELETE_LEVEL = 1 << 20;
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

    public interface Observer extends AbstractTwinmeService.Observer, SpaceListObserver {

        void onSignIn();

        void onSignOut();

        void onFatalError(@NonNull ErrorCode errorCode);

        void onSetCurrentSpace(@NonNull Space space);

        void onGetProfileNotFound();

        void onCreateSpace(@NonNull Space space);

        void onUpdateSpace(@NonNull Space space);

        void onDeleteSpace(@NonNull UUID spaceId);

        void onUpdateProfile(@NonNull Profile profile);

        void onUpdatePendingNotifications(boolean hasPendingNotification);

        void onGetSpacesNotifications(@NonNull Map<Space, NotificationStat> spacesNotifications);

        void onSubscribeSuccess();

        void onSubscribeFailed(@NonNull ErrorCode errorCode);

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

        @Override
        public void onUpdateProfile(long requestId, @NonNull Profile profile) {
            if (DEBUG) {
                Log.d(LOG_TAG, "TwinmeContextObserver.onUpdateProfile: requestId=" + requestId + " profile=" + profile);
            }

            MainService.this.onUpdateProfile(profile);
        }

        @Override
        public void onCreateSpace(long requestId, @NonNull Space space) {
            if (DEBUG) {
                Log.d(LOG_TAG, "TwinmeContextObserver.onUpdateSpace: requestId=" + requestId + " space=" + space);
            }

            MainService.this.onCreateSpace(space);
        }

        @Override
        public void onUpdateSpace(long requestId, @NonNull Space space) {
            if (DEBUG) {
                Log.d(LOG_TAG, "TwinmeContextObserver.onUpdateSpace: requestId=" + requestId + " space=" + space);
            }

            MainService.this.onUpdateSpace(space);
        }

        @Override
        public void onDeleteSpace(long requestId, @NonNull UUID spaceId) {
            if (DEBUG) {
                Log.d(LOG_TAG, "TwinmeContextObserver.onDeleteSpace: requestId=" + requestId + " spaceId=" + spaceId);
            }

            MainService.this.onDeleteSpace(spaceId);
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

    private class MainServiceAccountServiceObserver extends AccountService.DefaultServiceObserver {

        @Override
        public void onSubscribeUpdate(long requestId, @NonNull ErrorCode errorCode) {
            if (DEBUG) {
                Log.d(LOG_TAG, "MainServiceAccountServiceObserver.onSubscribeUpdate: requestId=" + requestId + " errorCode=" + errorCode);
            }

            if (getOperation(requestId) == null) {

                return;
            }

            MainService.this.onSubscribeUpdate(errorCode);
        }
    }

    @NonNull
    private final Observer mObserver;

    private int mState = 0;
    private UUID mSpaceId;
    private Space mSpace;

    private int mWork = 0;
    private String mProductId;
    private String mPurchaseToken;
    private String mPurchaseOrderId;

    private boolean mCreateLevel = false;

    private final MainServiceAccountServiceObserver mMainServiceAccountServiceObserver;

    public MainService(@NonNull AbstractTwinmeActivity activity, @NonNull TwinmeContext twinmeContext, @NonNull Observer observer) {
        super(LOG_TAG, activity, twinmeContext, observer);
        if (DEBUG) {
            Log.d(LOG_TAG, "MainService: activity=" + activity + " twinmeContext=" + twinmeContext + " observer=" + observer);
        }

        mObserver = observer;

        mMainServiceAccountServiceObserver = new MainServiceAccountServiceObserver();

        mTwinmeContextObserver = new TwinmeContextObserver();

        mTwinmeContext.setObserver(mTwinmeContextObserver);

        showProgressIndicator();
    }

    @Override
    protected void onTwinlifeReady() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onTwinlifeReady");
        }

        super.onTwinlifeReady();

        mTwinmeContext.getAccountService().addServiceObserver(mMainServiceAccountServiceObserver);
    }

    protected void onTwinlifeOnline() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onTwinlifeOnline");
        }

        if (mRestarted) {
            mRestarted = false;

            if ((mState & SUBSCRIBE_FEATURE) != 0 && (mState & SUBSCRIBE_FEATURE_DONE) == 0) {
                mState &= ~SUBSCRIBE_FEATURE;
            }
        }
    }

    public void dispose() {
        if (DEBUG) {
            Log.d(LOG_TAG, "dispose");
        }

        if (mTwinmeContext.hasTwinlife()) {
            mTwinmeContext.getAccountService().removeServiceObserver(mMainServiceAccountServiceObserver);
        }

        super.dispose();
    }

    public void getPendingNotifications() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getPendingNotifications");
        }

        mState &= ~(GET_PENDING_NOTIFICATIONS | GET_PENDING_NOTIFICATIONS_DONE);
        startOperation();
    }

    public void findSpacesNotifications() {
        if (DEBUG) {
            Log.d(LOG_TAG, "findSpacesNotifications");
        }

        long requestId = newOperation(GET_SPACES_NOTIFICATIONS);
        if (DEBUG) {
            Log.d(LOG_TAG, "findSpacesNotifications: requestId=" + requestId);
        }
        showProgressIndicator();

        mTwinmeContext.getNotificationStats((BaseService.ErrorCode errorCode, Map<Space, NotificationStat> stats) -> {
            runOnUiThread(() -> {
                if (stats != null) {
                    mObserver.onGetSpacesNotifications(stats);
                }
            });
            mState |= GET_SPACES_NOTIFICATIONS_DONE;
            onOperation();
        });
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

    public void createLevel(@NonNull String level) {
        if (DEBUG) {
            Log.d(LOG_TAG, "createLevel level=" + level);
        }

        if (!level.isEmpty()) {
            mCreateLevel = true;
            mTwinmeContext.createLevel(newOperation(CREATE_LEVEL), level);
        }
    }

    public void setLevel(@NonNull String level) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setLevel level=" + level);
        }

        if (!level.isEmpty()) {
            mTwinmeContext.setLevel(newOperation(SET_LEVEL), level);
        }
    }

    public void deleteLevel(@NonNull String level) {
        if (DEBUG) {
            Log.d(LOG_TAG, "deleteLevel level=" + level);
        }

        if (!level.isEmpty() && !"0".equals(level)) {
            mTwinmeContext.deleteLevel(newOperation(DELETE_LEVEL), level);
        }
    }

    public void subscribeFeature(@NonNull String productId, @NonNull String purchaseToken, @NonNull String purchaseOrderId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "subscribeFeature: " + productId + " purchaseToken = " + purchaseToken + " purchaseOrderId = " + purchaseOrderId);
        }

        mProductId = productId;
        mPurchaseToken = purchaseToken;
        mPurchaseOrderId = purchaseOrderId;

        mWork = SUBSCRIBE_FEATURE;
        mState &= ~(SUBSCRIBE_FEATURE | SUBSCRIBE_FEATURE_DONE);
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
        // Step 2: get spaces.
        //

        if ((mState & GET_SPACES) == 0) {
            mState |= GET_SPACES;

            mTwinmeContext.findSpaces((Space space) -> true, (ErrorCode errorCode, List<Space> spaces) -> {
                if (spaces != null) {
                    runOnGetSpaces(mObserver, spaces);
                }
                mState |= GET_SPACES_DONE;
                onOperation();
            });
            return;
        }
        if ((mState & GET_SPACES_DONE) == 0) {
            return;
        }

        //
        // Step 6
        //

        if ((mState & GET_PENDING_NOTIFICATIONS) == 0) {
            mState |= GET_PENDING_NOTIFICATIONS;

            if (DEBUG) {
                Log.d(LOG_TAG, "TwinmeContext.getPendingNotifications");
            }

            mTwinmeContext.getSpaceNotificationStats((BaseService.ErrorCode errorCode, NotificationStat stat) -> onGetSpaceNotificationStats(stat));
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
                    this::onGetTransferCall);
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
                if (!mSpace.isSecret()) {
                    mTwinmeContext.setDefaultSpace(mSpace);
                }
                return;
            }
            if ((mState & SET_SPACE_DONE) == 0) {
                return;
            }
        }

        if ((mWork & SUBSCRIBE_FEATURE) != 0) {

            if ((mState & SUBSCRIBE_FEATURE) == 0) {
                mState |= SUBSCRIBE_FEATURE;

                long requestId = newOperation(SUBSCRIBE_FEATURE);
                mTwinmeContext.getAccountService().subscribeFeature(requestId, AccountService.MerchantIdentification.MERCHANT_GOOGLE, mProductId, mPurchaseToken, mPurchaseOrderId);
                return;
            }
            if ((mState & SUBSCRIBE_FEATURE_DONE) == 0) {
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

    private void onCreateSpace(@NonNull Space space) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateSpace: space=" + space);
        }

        runOnUiThread(() -> mObserver.onCreateSpace(space));

        if (mCreateLevel) {
            mCreateLevel = false;
            setSpace(space.getId());
        }
    }

    private void onUpdateSpace(@NonNull Space space) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateSpace: space=" + space);
        }

        runOnUiThread(() -> mObserver.onUpdateSpace(space));
    }

    private void onDeleteSpace(@NonNull UUID spaceId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteSpace: spaceId=" + spaceId);
        }

        runOnUiThread(() -> mObserver.onDeleteSpace(spaceId));
    }

    private void onGetTransferCall(@NonNull List<CallReceiver> callReceivers) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetTransferCall: callReceivers=" + callReceivers);
        }

        mState |= GET_TRANSFER_CALL_DONE;

        if (!callReceivers.isEmpty()) {
            runOnUiThread(() -> mObserver.onGetTransferCall(callReceivers.get(0)));
        }
        onOperation();
    }

    private void onSetSpace(@NonNull Space space) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSetSpace: space=" + space);
        }

        mState |= SET_SPACE_DONE;

        onSetCurrentSpace(space);
        onOperation();
    }

    private void onGetSpaceNotificationStats(NotificationStat notificationStat) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetPendingNotifications: notificationStat=" + notificationStat);
        }

        mState |= GET_PENDING_NOTIFICATIONS_DONE;
        runOnUiThread(() -> mObserver.onUpdatePendingNotifications(notificationStat.getPendingCount() > 0));
        onOperation();
    }

    private void onUpdatePendingNotifications(boolean hasPendingNotifications) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdatePendingNotifications: hasPendingNotifications=" + hasPendingNotifications);
        }

        runOnUiThread(() -> mObserver.onUpdatePendingNotifications(hasPendingNotifications));
    }

    private void onSubscribeUpdate(@NonNull ErrorCode errorCode) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSubscribeUpdate: " + errorCode);
        }

        // When we are offline or failed to send the request, we must retry.
        if (errorCode == ErrorCode.TWINLIFE_OFFLINE) {
            mRestarted = true;

            return;
        }

        mState |= SUBSCRIBE_FEATURE_DONE;

        runOnUiThread(() -> {
            if (errorCode == ErrorCode.SUCCESS) {
                mObserver.onSubscribeSuccess();
            } else {
                mObserver.onSubscribeFailed(errorCode);
            }
        });
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
                mObserver.onGetProfileNotFound();
                return;
            }
        }

        if (operationId == SET_LEVEL && errorCode == ErrorCode.ITEM_NOT_FOUND) {
            return;
        }
        if (operationId == DELETE_LEVEL && errorCode == ErrorCode.ITEM_NOT_FOUND) {
            return;
        }

        super.onError(operationId, errorCode, errorParameter);
    }
}
