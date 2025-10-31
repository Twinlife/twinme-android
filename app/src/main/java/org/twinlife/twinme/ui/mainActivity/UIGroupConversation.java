/*
 *  Copyright (c) 2018-2020 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui.mainActivity;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.Nullable;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinme.models.GroupMember;
import org.twinlife.twinme.ui.users.UIContact;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a group conversation.
 */
public class UIGroupConversation extends UIConversation {

    private List<GroupMember> mGroupMembers;
    private List<UUID> mGroupMemberTwincodeOutboundIds;
    private List<Bitmap> mGroupAvatars;

    private int mGroupMemberCount = 0;
    private ConversationService.GroupConversation.State mGroupConversationState;

    UIGroupConversation(UUID conversationId, UIContact uiContact, ConversationService.GroupConversation.State groupConversationState) {
        super(conversationId, uiContact);

        mGroupConversationState = groupConversationState;
    }

    /**
     * Set the list of group members to be resolved and displayed below the group name.
     *
     * @param uiMemberList a list of group member twincodes to resolve.
     */
    void setVisibleMembers(List<UUID> uiMemberList) {

        mGroupMemberTwincodeOutboundIds = uiMemberList;
        mGroupMembers = null;
        mGroupAvatars = new ArrayList<>();
    }

    void setGroupConversationState(ConversationService.GroupConversation.State state) {
        this.mGroupConversationState = state;
    }

    /**
     * Given a map of known group members, collect the group members that we want to display
     * and identify the group member twincodes for which a resolution is necessary.
     *
     * @param members               the map of known group members.
     * @param groupMemberTwincodeId the group member id that was just added or null.
     * @return the list of group member twincodes not found in the known map.
     */
    List<UUID> updateVisibleMembers(Map<UUID, GroupMember> members, @Nullable UUID groupMemberTwincodeId, Bitmap avatar) {
        List<UUID> result = new ArrayList<>();

        // This member was not found, remove it from our list.
        if (groupMemberTwincodeId != null && !members.containsKey(groupMemberTwincodeId)) {
            mGroupMemberTwincodeOutboundIds.remove(groupMemberTwincodeId);
        }

        // ConversationFragment.onGetGroups() is called multiple times with the same groupMember/avatar =>
        // Prevent duplicated avatars if the group doesn't have an image.
        // TODO: figure out whether this is normal.
        if (avatar != null && !mGroupAvatars.contains(avatar)) {
            mGroupAvatars.add(avatar);
        }

        mGroupMembers = new ArrayList<>();
        for (UUID member : mGroupMemberTwincodeOutboundIds) {
            if (members.containsKey(member)) {
                mGroupMembers.add(members.get(member));
            } else {
                result.add(member);
            }
        }
        return result;
    }

    public String getLastMessage(Context context) {

        if (mGroupConversationState == ConversationService.GroupConversation.State.CREATED) {
            return context.getString(R.string.conversation_activity_invitation_accepted);
        } else {
            return super.getLastMessage(context);
        }
    }

    /**
     * Get the group information to be displayed below the group name.
     *
     * @return the group information (holding the name of first group members).
     */
    String getInformation() {

        if (mGroupMembers == null || mGroupMembers.isEmpty()) {
            return "";
        } else {
            StringBuilder result = new StringBuilder();

            for (GroupMember member : mGroupMembers) {
                if (member.getName() != null) {
                    if (result.length() > 0) {
                        result.append(", ");
                    }
                    result.append(member.getName());
                }
            }
            return result.toString();
        }
    }

    int getGroupMemberCount() {

        return mGroupMemberCount;
    }

    void setGroupMemberCount(int groupMemberCount) {

        this.mGroupMemberCount = groupMemberCount;
    }

    List<Bitmap> getGroupAvatars() {

        return mGroupAvatars;
    }

    ConversationService.GroupConversation.State getGroupConversationState() {

        return mGroupConversationState;
    }
}
