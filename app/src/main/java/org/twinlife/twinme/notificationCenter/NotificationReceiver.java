/*
 *  Copyright (c) 2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Romain Kolb (romain.kolb@skyrock.com)
 */

package org.twinlife.twinme.notificationCenter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.RemoteInput;

import org.twinlife.twinlife.BaseService;
import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinme.TwinmeContext;
import org.twinlife.twinme.services.PeerService;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.TwinmeApplicationImpl;
import org.twinlife.twinme.utils.Utils;

import java.util.UUID;

/**
 * Handles direct replies from message notifications. Also handles notification deletion.
 */
public class NotificationReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = "ReplyReceiver";
    private static final boolean DEBUG = false;

    public static final String KEY_TEXT_REPLY = "org.twinlife.twinme.text_reply";
    public static final String REPLY_ACTION = "org.twinlife.twinme.REPLY_ACTION";

    public static final String DELETE_ACTION = "org.twinlife.twinme.NOTIFICATION_DELETE_ACTION";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onReceive: context=" + context + " intent=" + intent);
        }

        TwinmeApplicationImpl twinmeApplication = TwinmeApplicationImpl.getInstance(context);

        if (twinmeApplication == null || twinmeApplication.getTwinmeContext() == null) {
            Log.e(LOG_TAG, "Application is not initialized, can't send reply");
            return;
        }

        if (REPLY_ACTION.equals(intent.getAction())) {
            sendReply(context, intent, twinmeApplication);
        } else if (DELETE_ACTION.equals(intent.getAction())) {
            handleNotificationDelete(intent, twinmeApplication);
        }
    }

    private void sendReply(@NonNull Context context, @NonNull Intent intent, @NonNull TwinmeApplicationImpl twinmeApplication) {
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        String contactId = intent.getStringExtra(Intents.INTENT_CONTACT_ID);
        UUID conversationId = (UUID) intent.getSerializableExtra(Intents.INTENT_CONVERSATION_ID);

        if (remoteInput == null || contactId == null || conversationId == null) {
            Log.e(LOG_TAG, "No remoteInput or contactId or conversationId in intent");
            return;
        }

        TwinmeContext twinmeContext = twinmeApplication.getTwinmeContext();

        if (twinmeContext == null) {
            Log.e(LOG_TAG, "twinmeContext not initialized, can't send reply");
            return;
        }

        CharSequence message = remoteInput.getCharSequence(KEY_TEXT_REPLY);

        if (message == null) {
            Log.e(LOG_TAG, "No message in remoteInput");
            twinmeContext.getNotificationCenter().acknowledgeReply(conversationId, null);
            return;
        }

        if (DEBUG) {
            Log.d(LOG_TAG, "Got message: " + message + " for: " + contactId);
        }

        UUID contactUUID = null;
        try {
            contactUUID = Utils.UUIDFromString(contactId.split("_")[1]);
        } catch (Exception e) {
            // Nothing to do, the null check below will fail.
        }

        if (contactUUID == null) {
            Log.e(LOG_TAG, "Invalid contactId: " + contactId);
            twinmeContext.getNotificationCenter().acknowledgeReply(conversationId, null);
            return;
        }

        // Start the foreground PeerService to ensure we have a connection and the message is sent right away.
        PeerService.startService(context, 0, System.currentTimeMillis());

        twinmeContext.getOriginator(contactUUID, (status, originator) -> {
            if (status != BaseService.ErrorCode.SUCCESS || originator == null) {
                Log.e(LOG_TAG, "Could not get originator with id=" + contactId);
                twinmeContext.getNotificationCenter().acknowledgeReply(conversationId, null);
                return;
            }

            ConversationService.Conversation conversation = twinmeContext.getConversationService().getConversation(originator);

            if (conversation == null) {
                Log.e(LOG_TAG, "Could not get conversation for originator=" + originator);
                twinmeContext.getNotificationCenter().acknowledgeReply(conversationId, null);
                return;
            }

            twinmeContext.pushMessage(0, conversation, null, null, message.toString(), twinmeApplication.messageCopyAllowed(), 0);

            UUID notificationId = Utils.UUIDFromString(intent.getStringExtra(Intents.INTENT_NOTIFICATION_ID));
            if (notificationId != null) {
                twinmeContext.getNotification(notificationId, (status1, notification) -> {
                    if (status1 == BaseService.ErrorCode.SUCCESS && notification != null) {
                        ConversationService.DescriptorId descriptorId = notification.getDescriptorId();

                        if (descriptorId != null) {
                            twinmeContext.markDescriptorRead(0, descriptorId);
                        }
                    }
                });
            }

            twinmeContext.getNotificationCenter().acknowledgeReply(conversationId, message.toString());
        });
    }

    private void handleNotificationDelete(@NonNull Intent intent, @NonNull TwinmeApplicationImpl twinmeApplication) {
        TwinmeContext twinmeContext = twinmeApplication.getTwinmeContext();

        if (twinmeContext == null) {
            Log.e(LOG_TAG, "twinmeContext not initialized, can't handle notification delete");
            return;
        }
        UUID conversationId = null;
        try {
            conversationId = (UUID) intent.getSerializableExtra(Intents.INTENT_CONVERSATION_ID);
        } catch (Exception ignored) {
        }

        if (conversationId == null) {
            Log.e(LOG_TAG, "Could not get conversation ID extra, value is: " + intent.getSerializableExtra(Intents.INTENT_CONVERSATION_ID));
            return;
        }

        ((NotificationCenterImpl) twinmeContext.getNotificationCenter()).onMessageNotificationDeleted(conversationId);
    }
}
