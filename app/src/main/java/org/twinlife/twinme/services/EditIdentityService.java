/*
 *  Copyright (c) 2020-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 *   Romain Kolb (romain.kolb@skyrock.com)
 */

package org.twinlife.twinme.services;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.twinlife.twinlife.BaseService.ErrorCode;
import org.twinlife.twinlife.ImageId;
import org.twinlife.twinlife.ImageService;
import org.twinlife.twinme.TwinmeContext;
import org.twinlife.twinme.models.CallReceiver;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Group;
import org.twinlife.twinme.models.Profile;
import org.twinlife.twinme.models.Space;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;

import java.io.File;
import java.util.UUID;

public class EditIdentityService extends AbstractTwinmeService {
    private static final String LOG_TAG = "EditIdentityService";
    private static final boolean DEBUG = false;

    private static final int GET_PROFILE = 1;
    private static final int GET_PROFILE_DONE = 1 << 1;
    private static final int GET_CONTACT = 1 << 2;
    private static final int GET_CONTACT_DONE = 1 << 3;
    private static final int GET_GROUP = 1 << 4;
    private static final int GET_GROUP_DONE = 1 << 5;
    private static final int UPDATE_PROFILE = 1 << 6;
    private static final int UPDATE_PROFILE_DONE = 1 << 7;
    private static final int UPDATE_CONTACT = 1 << 8;
    private static final int UPDATE_CONTACT_DONE = 1 << 9;
    private static final int UPDATE_GROUP = 1 << 10;
    private static final int UPDATE_GROUP_DONE = 1 << 11;
    private static final int GET_IDENTITY_AVATAR = 1 << 14;
    private static final int GET_IDENTITY_AVATAR_DONE = 1 << 15;
    private static final int GET_SPACE = 1 << 16;
    private static final int GET_SPACE_DONE = 1 << 17;
    private static final int CREATE_PROFILE = 1 << 20;
    private static final int CREATE_PROFILE_DONE = 1 << 21;
    private static final int GET_CALL_RECEIVER = 1 << 23;
    private static final int GET_CALL_RECEIVER_DONE = 1 << 24;
    private static final int UPDATE_CALL_RECEIVER = 1 << 25;
    private static final int UPDATE_CALL_RECEIVER_DONE = 1 << 26;

    public interface Observer extends AbstractTwinmeService.Observer, ContactObserver, SpaceObserver {

        void onCreateProfile(@NonNull Profile profile);

        void onGetProfile(@NonNull Profile profile, @Nullable Bitmap avatar);

        void onGetProfileNotFound();

        void onUpdateSpace(@NonNull Space space);

        void onUpdateProfile(@NonNull Profile profile);

        void onUpdateIdentityAvatar(@NonNull Bitmap avatar);

        void onGetGroup(@NonNull Group group);

        void onGetGroupNotFound();

        void onUpdateGroup(@NonNull Group group);

        void onGetCallReceiver(@NonNull CallReceiver callReceiver);

        void onGetCallReceiverNotFound();

        void onUpdateCallReceiver(@NonNull CallReceiver callReceiver);
    }

    private class TwinmeContextObserver extends AbstractTwinmeService.TwinmeContextObserver {

        @Override
        public void onCreateProfile(long requestId, @NonNull Profile profile) {
            if (DEBUG) {
                Log.d(LOG_TAG, "TwinmeContextObserver.onCreateProfile: requestId=" + requestId + " profile=" + profile);
            }

            if (getOperation(requestId) == null) {

                return;
            }

            EditIdentityService.this.onCreateProfile(profile);
        }

        @Override
        public void onUpdateProfile(long requestId, @NonNull Profile profile) {
            if (DEBUG) {
                Log.d(LOG_TAG, "TwinmeContextObserver.onUpdateProfile: requestId=" + requestId + " profile=" + profile);
            }

            Integer operationId = getOperation(requestId);
            EditIdentityService.this.onUpdateProfile(operationId, profile);
        }

        @Override
        public void onUpdateContact(long requestId, @NonNull Contact contact) {
            if (DEBUG) {
                Log.d(LOG_TAG, "TwinmeContextObserver.onUpdateContact: requestId=" + requestId + " contact=" + contact);
            }

            if (getOperation(requestId) == null) {

                return;
            }

            EditIdentityService.this.onUpdateContact(contact);
        }

        @Override
        public void onUpdateGroup(long requestId, @NonNull Group group) {
            if (DEBUG) {
                Log.d(LOG_TAG, "TwinmeContextObserver.onUpdateGroup: requestId=" + requestId + " group=" + group);
            }

            if (getOperation(requestId) == null) {

                return;
            }

            EditIdentityService.this.onUpdateGroup(group);
        }

        @Override
        public void onUpdateCallReceiver(long requestId, @NonNull CallReceiver callReceiver) {
            if (DEBUG) {
                Log.d(LOG_TAG, "TwinmeContextObserver.onUpdateCallReceiver: requestId=" + requestId + " callReceiver=" + callReceiver);
            }

            if (getOperation(requestId) == null) {

                return;
            }

            EditIdentityService.this.onUpdateCallReceiver(callReceiver);
        }

        @Override
        public void onSetCurrentSpace(long requestId, @NonNull Space space) {
            if (DEBUG) {
                Log.d(LOG_TAG, "TwinmeContextObserver.onSetCurrentSpace: requestId=" + requestId + " space=" + space);
            }

            finishOperation(requestId);

            EditIdentityService.this.onSetCurrentSpace(space);
        }

        @Override
        public void onUpdateSpace(long requestId, @NonNull Space space) {
            if (DEBUG) {
                Log.d(LOG_TAG, "TwinmeContextObserver.onUpdateSpace: requestId=" + requestId + " space=" + space);
            }

            EditIdentityService.this.onUpdateSpace(space);
        }
    }

    @Nullable
    private Observer mObserver;
    private int mState = 0;
    private int mWork = 0;
    private Space mCurrentSpace;
    @Nullable
    private UUID mSpaceId;
    @Nullable
    private UUID mProfileId;
    @Nullable
    private ImageId mAvatarId;
    @Nullable
    private UUID mContactId;
    @Nullable
    private UUID mGroupId;
    @Nullable
    private UUID mCallReceiverId;
    private Profile mProfile;
    private Contact mContact;
    private Group mGroup;
    private CallReceiver mCallReceiver;
    private Bitmap mAvatar;
    private File mAvatarFile;
    private String mName;
    private String mDescription;
    private Profile.UpdateMode mProfileUpdateMode;

    public EditIdentityService(@NonNull AbstractTwinmeActivity activity, @NonNull TwinmeContext twinmeContext, @NonNull Observer observer) {
        super(LOG_TAG, activity, twinmeContext, observer);
        if (DEBUG) {
            Log.d(LOG_TAG, "EditIdentityService: activity=" + activity + " twinmeContext=" + twinmeContext
                    + " observer=" + observer);
        }

        mObserver = observer;

        mTwinmeContextObserver = new TwinmeContextObserver();

        mTwinmeContext.setObserver(mTwinmeContextObserver);
    }

    public void dispose() {
        if (DEBUG) {
            Log.d(LOG_TAG, "dispose");
        }

        mObserver = null;
        super.dispose();
    }

    public void getProfile(@NonNull UUID profileId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getProfile: profileId=" + profileId);
        }

        mWork |= GET_SPACE | GET_PROFILE | GET_IDENTITY_AVATAR;
        mState &= ~(GET_SPACE | GET_SPACE_DONE | GET_PROFILE | GET_PROFILE_DONE | GET_IDENTITY_AVATAR | GET_IDENTITY_AVATAR_DONE);
        mProfileId = profileId;
        mSpaceId = null;
        showProgressIndicator();
        startOperation();
    }

    public void updateProfile(@NonNull Profile profile, @NonNull String name, @Nullable String descriptionProfile, @NonNull Bitmap avatar, @Nullable File avatarFile) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateProfile: profile=" + profile + " name=" + name + " descriptionProfile=" + descriptionProfile + " avatar=" + avatar + " avatarFile=" + avatarFile);
        }

        mWork |= UPDATE_PROFILE;
        mState &= ~(UPDATE_PROFILE | UPDATE_PROFILE_DONE);
        mProfile = profile;
        mName = name;
        mDescription = descriptionProfile;
        mAvatar = avatar;
        mAvatarFile = avatarFile;

        if (mTwinmeApplication.updateProfileMode() == Profile.UpdateMode.NONE.ordinal()) {
            mProfileUpdateMode = Profile.UpdateMode.NONE;
        } else if (mTwinmeApplication.updateProfileMode() == Profile.UpdateMode.ALL.ordinal()) {
            mProfileUpdateMode = Profile.UpdateMode.ALL;
        } else {
            mProfileUpdateMode = Profile.UpdateMode.DEFAULT;
        }

        showProgressIndicator();
        startOperation();
    }

    public void getContact(@NonNull UUID contactId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getContact: contactId=" + contactId);
        }

        mWork |= GET_CONTACT | GET_IDENTITY_AVATAR;
        mState &= ~(GET_CONTACT | GET_CONTACT_DONE | GET_IDENTITY_AVATAR | GET_IDENTITY_AVATAR_DONE);
        mContactId = contactId;
        showProgressIndicator();
        startOperation();
    }

    /**
     * <p>
     * Find a {@link CallReceiver} by its DB ID.
     * <p>
     * Once found, the CallReceiver will be made available through the
     * {@link CallReceiverService.Observer#onGetCallReceiver(CallReceiver)} callback. If no CallReceiver was found,
     * the callback will be called with a null argument.
     *
     * @param callReceiverId the ID of the CallReceiver.
     */
    public void getCallReceiver(@NonNull UUID callReceiverId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getCallReceiver: callReceiverId=" + callReceiverId);
        }

        mCallReceiverId = callReceiverId;

        mWork |= GET_CALL_RECEIVER | GET_IDENTITY_AVATAR;
        mState &= ~(GET_CALL_RECEIVER | GET_CALL_RECEIVER_DONE | GET_IDENTITY_AVATAR | GET_IDENTITY_AVATAR_DONE);

        startOperation();
    }

    public void updateContact(@NonNull Contact contact, @NonNull String name, @Nullable String description, @NonNull Bitmap avatar, @Nullable File avatarFile) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateContact: contact=" + contact + " name=" + name + " description=" + description + " avatar=" + avatar + " avatarFile=" + avatarFile);
        }

        mWork |= UPDATE_CONTACT;
        mState &= ~(UPDATE_CONTACT | UPDATE_CONTACT_DONE);
        mContact = contact;
        mName = name;
        mDescription = description;
        mAvatar = avatar;
        mAvatarFile = avatarFile;
        showProgressIndicator();
        startOperation();
    }

    public void getGroup(@NonNull UUID groupId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getGroup: groupId=" + groupId);
        }

        mWork |= GET_GROUP | GET_IDENTITY_AVATAR;
        mState &= ~(GET_GROUP | GET_GROUP_DONE | GET_IDENTITY_AVATAR | GET_IDENTITY_AVATAR_DONE);
        mGroupId = groupId;
        showProgressIndicator();
        startOperation();
    }

    public void updateGroup(@NonNull Group group, @NonNull String name, @NonNull Bitmap avatar, @Nullable File avatarFile) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateGroup: group=" + group + " name=" + name + " avatar=" + avatar);
        }

        mWork |= UPDATE_GROUP;
        mState &= ~(UPDATE_GROUP | UPDATE_GROUP_DONE);
        mGroup = group;
        mName = name;
        mAvatar = avatar;
        mAvatarFile = avatarFile;
        showProgressIndicator();
        startOperation();
    }

    /**
     * <p>
     * Update a {@link CallReceiver}.
     * <p>
     * Apart from the CallReceiver itself and its name, all parameters are optional.
     * null parameters will be ignored and the current value will be kept.
     *
     * @param callReceiver The CallReceiver to update.
     * @param identityName The name of the call receiver's twincodeOutbound.
     * @param description  The description of the call receiver's twincodeOutbound.
     * @param avatar       The thumbnail of the call receiver's avatar.
     * @param avatarFile   The actual call receiver's avatar. Mandatory if avatar is not null.
     */
    public void updateCallReceiver(@NonNull CallReceiver callReceiver, @Nullable String identityName, @Nullable String description, @Nullable Bitmap avatar, @Nullable File avatarFile) {
        if (DEBUG) {
            Log.d(LOG_TAG, "deleteCallReceiver: callReceiver=");
        }

        mCallReceiver = callReceiver;
        mName = identityName;
        mDescription = description;
        mAvatar = avatar;
        mAvatarFile = avatarFile;

        mWork |= UPDATE_CALL_RECEIVER;
        mState &= ~(UPDATE_CALL_RECEIVER | UPDATE_CALL_RECEIVER_DONE);

        startOperation();
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
        // Step 1: get the current space.
        //

        if ((mWork & GET_SPACE) != 0) {
            if ((mState & GET_SPACE) == 0) {
                mState |= GET_SPACE;

                if (mSpaceId == null) {
                    mTwinmeContext.getCurrentSpace(this::onGetSpace);

                } else {
                    mTwinmeContext.getSpace(mSpaceId, this::onGetSpace);
                }
                return;
            }
            if ((mState & GET_SPACE_DONE) == 0) {
                return;
            }
        }

        // We must get the profile.
        if ((mWork & GET_PROFILE) != 0 && mProfileId != null) {
            if ((mState & GET_PROFILE) == 0) {
                mState |= GET_PROFILE;

                mTwinmeContext.getProfile(mProfileId, this::onGetProfile);
                return;
            }
            if ((mState & GET_PROFILE_DONE) == 0) {
                return;
            }
        }

        //
        // Get the profile normal image if we can.
        //
        if (mAvatarId != null && (mWork & GET_IDENTITY_AVATAR) != 0) {
            if ((mState & GET_IDENTITY_AVATAR) == 0) {
                mState |= GET_IDENTITY_AVATAR;

                mTwinmeContext.getImageService().getImageFromServer(mAvatarId, ImageService.Kind.NORMAL, (ErrorCode errorCode, Bitmap image) -> {
                    mState |= GET_IDENTITY_AVATAR_DONE;
                    if (image != null) {
                        mAvatar = image;
                        runOnUiThread(() -> {
                            if (mObserver != null) {
                                mObserver.onUpdateIdentityAvatar(image);
                            }
                        });
                    }
                    onOperation();
                });
                return;
            }
            if ((mState & GET_IDENTITY_AVATAR_DONE) == 0) {
                return;
            }
        }

        // We must update profile
        if ((mWork & UPDATE_PROFILE) != 0) {
            if ((mState & UPDATE_PROFILE) == 0) {
                mState |= UPDATE_PROFILE;
                long requestId = newOperation(UPDATE_PROFILE);
                mTwinmeContext.updateProfile(requestId, mProfile, mProfileUpdateMode,
                        mName, mAvatar, mAvatarFile, mDescription, mProfile.getIdentitiyCapabilities());
                return;
            }
            if ((mState & UPDATE_PROFILE_DONE) == 0) {
                return;
            }
        }

        // We must get the contact.
        if ((mWork & GET_CONTACT) != 0 && mContactId != null) {
            if ((mState & GET_CONTACT) == 0) {
                mState |= GET_CONTACT;

                mTwinmeContext.getContact(mContactId, this::onGetContact);
                return;
            }
            if ((mState & GET_CONTACT_DONE) == 0) {
                return;
            }
        }

        // We must update identity for a contact
        if ((mWork & UPDATE_CONTACT) != 0 && mContact.getName() != null) {
            if ((mState & UPDATE_CONTACT) == 0) {
                mState |= UPDATE_CONTACT;

                long requestId = newOperation(UPDATE_CONTACT);

                mTwinmeContext.updateContactIdentity(requestId, mContact, mName, mAvatar, mAvatarFile, mDescription, mContact.getIdentityCapabilities(), mContact.getPrivateCapabilities());
                return;
            }
            if ((mState & UPDATE_CONTACT_DONE) == 0) {
                return;
            }
        }

        // We must get the group.
        if ((mWork & GET_GROUP) != 0 && mGroupId != null) {
            if ((mState & GET_GROUP) == 0) {
                mState |= GET_GROUP;

                mTwinmeContext.getGroup(mGroupId, this::onGetGroup);
                return;
            }
            if ((mState & GET_GROUP_DONE) == 0) {
                return;
            }
        }

        // We must update the user's profile in the group.
        if ((mWork & UPDATE_GROUP) != 0) {
            if ((mState & UPDATE_GROUP) == 0) {
                mState |= UPDATE_GROUP;
                long requestId = newOperation(UPDATE_GROUP);
                mTwinmeContext.updateGroupProfile(requestId, mGroup, mName, mAvatar, mAvatarFile);
                return;
            }
            if ((mState & UPDATE_GROUP_DONE) == 0) {
                return;
            }
        }

        //
        // Get a call receiver by ID
        //

        if (mCallReceiverId != null && (mWork & GET_CALL_RECEIVER) != 0) {
            if ((mState & GET_CALL_RECEIVER) == 0) {
                mState |= GET_CALL_RECEIVER;

                mTwinmeContext.getCallReceiver(mCallReceiverId, this::onGetCallReceiver);
                return;
            }

            if ((mState & GET_CALL_RECEIVER_DONE) == 0) {
                return;
            }
        }

        //
        // Update a call receiver
        //

        if (mCallReceiver != null && (mWork & UPDATE_CALL_RECEIVER) != 0) {
            if ((mState & UPDATE_CALL_RECEIVER) == 0) {
                mState |= UPDATE_CALL_RECEIVER;

                long requestId = newOperation(UPDATE_CALL_RECEIVER);
                if (DEBUG) {
                    Log.d(LOG_TAG, "mTwinmeContext.updateCallReceiver: requestId=" + requestId);
                }

                String callReceiverName = mCallReceiver.getName();
                String callReceiverDescription = mCallReceiver.getDescription();

                if (mCallReceiver.isTransfer()) {
                    callReceiverName = mName;
                    callReceiverDescription = mDescription;
                }
                mTwinmeContext.updateCallReceiver(requestId, mCallReceiver, callReceiverName, callReceiverDescription, mName, mDescription, mAvatar, mAvatarFile, null);
                return;
            }

            if ((mState & UPDATE_CALL_RECEIVER_DONE) == 0) {
                return;
            }
        }

        //
        // We must create a profile for the current space.
        //
        if (mCurrentSpace != null && (mWork & CREATE_PROFILE) != 0) {

            if ((mState & CREATE_PROFILE) == 0) {
                mState |= CREATE_PROFILE;

                long requestId = newOperation(CREATE_PROFILE);
                if (DEBUG) {
                    Log.d(LOG_TAG, "TwinmeContext.createProfile: requestId=" + requestId + " name=" + mName + " avatar=" + mAvatar);
                }

                mTwinmeContext.createProfile(requestId, mName, mAvatar, mAvatarFile, mDescription, null, mCurrentSpace);
                return;
            }
            if ((mState & CREATE_PROFILE_DONE) == 0) {
                return;
            }
        }

        //
        // Get a call receiver by ID
        //

        if (mCallReceiverId != null && (mWork & GET_CALL_RECEIVER) != 0) {
            if ((mState & GET_CALL_RECEIVER) == 0) {
                mState |= GET_CALL_RECEIVER;

                mTwinmeContext.getCallReceiver(mCallReceiverId, this::onGetCallReceiver);
                return;
            }

            if ((mState & GET_CALL_RECEIVER_DONE) == 0) {
                return;
            }
        }

        //
        // Last Step
        //

        hideProgressIndicator();
    }

    private void onGetSpace(@NonNull ErrorCode errorCode, @Nullable Space space) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetSpace: space=" + space);
        }

        mState |= GET_SPACE_DONE;
        if (space != null) {
            runOnGetSpace(mObserver, space, null);

            final Profile profile = space.getProfile();
            if (profile == null) {
                runOnUiThread(() -> {
                    if (mObserver != null) {
                        mObserver.onGetProfileNotFound();
                    }
                });

            } else if (mProfileId != null && mProfileId.equals(profile.getId())) {
                // Trigger the onGetProfile() because this is what we are editing.
                mState |= GET_PROFILE;
                onGetProfile(ErrorCode.SUCCESS, profile);
            } else if (mProfileId == null) {
                mProfileId = profile.getId();
                onGetProfile(ErrorCode.SUCCESS, profile);
            }
        }
        onOperation();
    }

    private void onUpdateProfile(@Nullable Integer operationId, @NonNull Profile profile) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateProfile: profile=" + profile);
        }

        if (operationId != null) {
            mState |= UPDATE_PROFILE_DONE;
        }

        if (mProfileId != null && mProfileId.equals(profile.getId())) {
            final ImageId avatarId = profile.getAvatarId();
            if (mAvatarId == null || !mAvatarId.equals(avatarId)) {
                mAvatarId = avatarId;
                mState &= ~(GET_IDENTITY_AVATAR | GET_IDENTITY_AVATAR_DONE);
            }
            runOnUiThread(() -> {
                if (mObserver != null) {
                    mObserver.onUpdateProfile(profile);
                }
            });
        }
        onOperation();
    }

    private void onGetProfile(@NonNull ErrorCode errorCode, @Nullable Profile profile) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetProfile: profile=" + profile);
        }

        mState |= GET_PROFILE_DONE;
        if (profile != null) {

            mProfileId = profile.getId();
            final ImageId avatarId = profile.getAvatarId();
            if (mAvatarId == null || !mAvatarId.equals(avatarId)) {
                mAvatarId = avatarId;
                mState &= ~(GET_IDENTITY_AVATAR | GET_IDENTITY_AVATAR_DONE);
            }
            final Bitmap avatar = getImage(avatarId);
            mAvatar = avatar;
            runOnUiThread(() -> {
                if (mObserver != null) {
                    mObserver.onGetProfile(profile, avatar);
                }
            });
        } else if (errorCode == ErrorCode.ITEM_NOT_FOUND) {
            runOnUiThread(() -> {
                if (mObserver != null) {
                    mObserver.onGetProfileNotFound();
                }
            });
        } else {
            onError(GET_PROFILE, errorCode, null);
        }
    }

    private void onUpdateContact(@NonNull Contact contact) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateContact: contact=" + contact);
        }

        mState |= UPDATE_CONTACT_DONE;
        runOnUpdateContact(mObserver, contact, null);
        onOperation();
    }

    private void onGetContact(@NonNull ErrorCode errorCode, @Nullable Contact contact) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContact: contact=" + contact);
        }

        mState |= GET_CONTACT_DONE;
        if (contact != null) {
            mAvatarId = contact.getIdentityAvatarId();
            runOnGetContact(mObserver, contact, null);
        } else if (errorCode == ErrorCode.ITEM_NOT_FOUND) {
            runOnGetContactNotFound(mObserver);
        } else {
            onError(GET_CONTACT, errorCode, null);
        }
        onOperation();
    }

    private void onUpdateGroup(@NonNull Group group) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateGroup: group=" + group);
        }

        mState |= UPDATE_GROUP_DONE;
        runOnUiThread(() -> {
            if (mObserver != null) {
                mObserver.onUpdateGroup(group);
            }
        });
        onOperation();
    }

    private void onGetGroup(@NonNull ErrorCode errorCode, @Nullable Group group) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetGroup: group=" + group);
        }

        mState |= GET_GROUP_DONE;
        if (group != null) {
            mAvatarId = group.getIdentityAvatarId();
            runOnUiThread(() -> {
                if (mObserver != null) {
                    mObserver.onGetGroup(group);
                }
            });
        } else if (errorCode == ErrorCode.ITEM_NOT_FOUND) {
            runOnUiThread(() -> {
                if (mObserver != null) {
                    mObserver.onGetGroupNotFound();
                }
            });
        } else {
            onError(GET_GROUP, errorCode, null);
        }
        onOperation();
    }

    private void onUpdateSpace(@NonNull Space space) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateSpace: space=" + space);
        }
        runOnUiThread(() -> {
            if (mObserver != null) {
                mObserver.onUpdateSpace(space);
            }
        });
    }

    private void onGetCallReceiver(@NonNull ErrorCode errorCode, @Nullable CallReceiver callReceiver) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetCallReceiver callReceiver=" + callReceiver);
        }

        mState |= GET_CALL_RECEIVER_DONE;

        if (callReceiver != null) {
            mAvatarId = callReceiver.getIdentityAvatarId();
        }
        runOnUiThread(() -> {
            if (mObserver != null) {
                if (callReceiver != null) {
                    mObserver.onGetCallReceiver(callReceiver);
                } else {
                    mObserver.onGetCallReceiverNotFound();
                }

            }
        });
        onOperation();
    }

    private void onUpdateCallReceiver(@NonNull CallReceiver callReceiver) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateCallReceiver callReceiver=" + callReceiver);
        }

        mState |= UPDATE_CALL_RECEIVER_DONE;

        runOnUiThread(() -> {
            if (mObserver != null) {
                mObserver.onUpdateCallReceiver(callReceiver);
            }
        });
        onOperation();
    }

    private void onCreateProfile(@NonNull Profile profile) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateProfile profile=" + profile);
        }

        mState |= CREATE_PROFILE_DONE;
        runOnUiThread(() -> {
            if (mObserver != null) {
                mObserver.onCreateProfile(profile);
            }
        });
        onOperation();
    }

    protected void onSetCurrentSpace(@NonNull Space space) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSetCurrentSpace: space=" + space);
        }

        mCurrentSpace = space;
        super.onSetCurrentSpace(space);
        onOperation();
    }

    protected void onError(int operationId, ErrorCode errorCode, @Nullable String errorParameter) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onError: operationId=" + operationId + " errorCode=" + errorCode + " errorParameter=" + errorParameter);
        }

        if (errorCode == ErrorCode.ITEM_NOT_FOUND) {
            switch (operationId) {

                case UPDATE_PROFILE:
                    mState |= UPDATE_PROFILE_DONE;
                    runOnUiThread(() -> {
                        if (mObserver != null) {
                            mObserver.onGetProfileNotFound();
                        }
                    });

                    return;

                case UPDATE_CONTACT:
                    mState |= UPDATE_CONTACT_DONE;
                    runOnUiThread(() -> {
                        if (mObserver != null) {
                            mObserver.onGetContactNotFound();
                        }
                    });

                    return;

                case UPDATE_GROUP:
                    mState |= UPDATE_GROUP_DONE;
                    runOnUiThread(() -> {
                        if (mObserver != null) {
                            mObserver.onGetGroupNotFound();
                        }
                    });

                    return;

                case UPDATE_CALL_RECEIVER:
                    mState |= UPDATE_CALL_RECEIVER_DONE;
                    runOnUiThread(() -> {
                        if (mObserver != null) {
                            mObserver.onGetCallReceiverNotFound();
                        }
                    });

                    return;

                default:
                    break;
            }
        }

        super.onError(operationId, errorCode, errorParameter);
    }
}
