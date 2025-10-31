/*
 *  Copyright (c) 2018-2020 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.groups;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinlife.ConversationService.GroupConversation;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Group;
import org.twinlife.twinme.models.GroupMember;
import org.twinlife.twinme.models.Invitation;
import org.twinlife.twinme.models.Space;
import org.twinlife.twinme.services.GroupService;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;

import java.util.List;
import java.util.UUID;

/**
 * Activity controller for the group creation.
 * <p>
 * The controller allows to choose:
 * <p>
 * - the group name,
 * - the group picture,
 * - a list of contacts to invite.
 * <p>
 * After the group is created and selected members invited, the user is redirected to the ShowGroupActivity.
 */

@SuppressLint("Registered")
public class AbstractGroupActivity extends AbstractTwinmeActivity implements GroupService.Observer {
    private static final String LOG_TAG = "AbstractGroupActivity";
    private static final boolean DEBUG = false;

    protected static final int DESIGN_HINT_COLOR = Color.parseColor("#bdbdbd");

    //
    // Override TwinmeActivityImpl methods
    //

    //
    // Override Activity methods
    //

    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    public void onGetGroup(@NonNull Group group, @NonNull List<GroupMember> groupMembers, @NonNull GroupConversation conversation) {

    }

    @Override
    public void onGetGroupNotFound() {

    }

    @Override
    public void onDeleteGroup(UUID groupId) {

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

    /**
     * The GroupService has finished the creation of the group, redirect to the show group activity.
     *
     * @param group        the group that was created.
     * @param conversation the group conversation.
     */
    @Override
    public void onCreateGroup(@NonNull Group group, @NonNull ConversationService.GroupConversation conversation) {

    }

    /**
     * Get the list of contacts and build the UI contact list selector.
     *
     * @param contacts the list of contacts.
     */
    @Override
    public void onGetContacts(@NonNull List<Contact> contacts) {

    }

    @Override
    public void onGetContact(@NonNull Contact contact, Bitmap avatar) {

    }

    @Override
    public void onUpdateContact(@NonNull Contact contact, Bitmap avatar) {

    }

    @Override
    public void onCreateInvitation(@NonNull Invitation invitation) {

    }

    @Override
    public void onGetCurrentSpace(@NonNull Space space) {

    }

    @Override
    public void onSetCurrentSpace(@NonNull Space space) {

    }

    @Override
    public void onGetSpace(@NonNull Space space, @Nullable Bitmap avatar) {

    }

    @Override
    public void onGetSpaceNotFound() {

    }

    @Override
    public void onErrorLimitReached() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onErrorLimitReached");
        }

        error(String.format(getString(R.string.application_group_limit_reached), ConversationService.MAX_GROUP_MEMBERS), this::hideProgressIndicator);
    }

    @Override
    public void onGetContactNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onErrorContactNotFound");
        }

        onError(null, getString(R.string.application_contact_not_found), this::finish);
    }
}
