/*
 *  Copyright (c) 2017-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Romain Kolb (romain.kolb@skyrock.com)
 */

package org.twinlife.twinme.ui.mainActivity;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ConversationService.CallDescriptor;
import org.twinlife.twinlife.ConversationService.Descriptor;
import org.twinlife.twinlife.ConversationService.ObjectDescriptor;
import org.twinlife.twinme.models.Originator;
import org.twinlife.twinme.ui.users.UIContact;
import org.twinlife.twinme.utils.Utils;

import java.util.UUID;

public class UIConversation {
    private static int sItemId = 0;

    private final long mItemId;

    @NonNull
    private final UUID mConversationId;
    @NonNull
    private UIContact mUIContact;

    private Descriptor mLastDescriptor;
    @NonNull
    private String mDate;

    UIConversation(@NonNull UUID conversationId, @NonNull UIContact uiContact) {

        mItemId = sItemId++;

        mConversationId = conversationId;
        mUIContact = uiContact;
        mDate = "";
    }

    long getItemId() {

        return mItemId;
    }

    double getUsageScore() {

        return mUIContact.getUsageScore();
    }

    void setLastDescriptor(@Nullable Context context, @Nullable Descriptor descriptor) {

        mLastDescriptor = descriptor;

        if (mLastDescriptor != null) {
            mDate = Utils.formatTimeInterval(context, mLastDescriptor.getCreatedTimestamp());
        } else {
            mDate = "";
        }
    }

    void setUIContact(UIContact uiContact) {

        mUIContact = uiContact;
    }

    @Nullable
    public Descriptor getLastDescriptor() {

        return mLastDescriptor;
    }

    public UIContact getUIContact() {

        return mUIContact;
    }


    public String getLastMessage(Context context) {

        String lastMessage = "";

        if (mLastDescriptor != null) {

            switch (mLastDescriptor.getType()) {
                case OBJECT_DESCRIPTOR:
                    ObjectDescriptor objectDescriptor = (ObjectDescriptor) mLastDescriptor;
                    lastMessage = objectDescriptor.getMessage();
                    break;

                case IMAGE_DESCRIPTOR:
                    lastMessage = context.getResources().getString(R.string.notification_center_photo_message_received);
                    break;

                case AUDIO_DESCRIPTOR:
                    lastMessage = context.getResources().getString(R.string.notification_center_audio_message_received);
                    break;

                case VIDEO_DESCRIPTOR:
                    lastMessage = context.getResources().getString(R.string.notification_center_video_message_received);
                    break;

                case NAMED_FILE_DESCRIPTOR:
                    lastMessage = context.getResources().getString(R.string.notification_center_file_message_received);
                    break;

                case INVITATION_DESCRIPTOR:
                case TWINCODE_DESCRIPTOR:
                    lastMessage = context.getResources().getString(R.string.notification_center_group_invitation_received);
                    break;

                case GEOLOCATION_DESCRIPTOR:
                    lastMessage = context.getResources().getString(R.string.notification_center_geolocation_message_received);
                    break;

                case CLEAR_DESCRIPTOR:
                    lastMessage = context.getResources().getString(R.string.notification_center_cleanup_conversation);
                    break;

                case CALL_DESCRIPTOR:
                    CallDescriptor callDescriptor = (CallDescriptor) mLastDescriptor;

                    if (!callDescriptor.isAccepted() && callDescriptor.isIncoming() && callDescriptor.getTerminateReason() != null) {
                        lastMessage = context.getResources().getString(R.string.calls_fragment_missed_call);
                    } else if (callDescriptor.isIncoming()) {
                        lastMessage = context.getResources().getString(R.string.calls_fragment_incoming_call);
                    } else {
                        lastMessage = context.getResources().getString(R.string.calls_fragment_outgoing_call);
                    }
                    break;

                default:
                    break;
            }
        }

        return lastMessage;
    }

    @NonNull
    public String getMessageDate() {

        return mDate;
    }

    long getLastMessageDate() {

        if (mLastDescriptor != null) {
            return mLastDescriptor.getCreatedTimestamp();
        }

        return mUIContact.getLastMessageDate();
    }

    @NonNull
    public UUID getConversationId() {

        return mConversationId;
    }

    public String getName() {

        return mUIContact.getName();
    }

    public Bitmap getAvatar() {

        return mUIContact.getAvatar();
    }

    public Originator getContact() {

        return mUIContact.getContact();
    }

    void resetUIConversation() {

        mUIContact.setUIConversation(null);
    }

    @SuppressWarnings("unused")
    String getInformation() {

        return "";
    }

    boolean isLastDescriptorUnread() {

        return mLastDescriptor != null && mLastDescriptor.getReadTimestamp() == 0 && !isLocalDescriptor();
    }

    boolean isCertified() {

        return mUIContact.isCertified();
    }

    public boolean isLocalDescriptor() {

        return mLastDescriptor.getTwincodeOutboundId().equals(mUIContact.getContact().getTwincodeOutboundId());
    }
}
