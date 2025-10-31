/*
 *  Copyright (c) 2015-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Romain Kolb (romain.kolb@skyrock.com)
 */

package org.twinlife.twinme.notificationCenter;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.Person;
import androidx.core.app.RemoteInput;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.BaseService.AttributeNameValue;
import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinlife.ConversationService.Conversation;
import org.twinlife.twinlife.ConversationService.GroupMemberConversation;
import org.twinlife.twinlife.ConversationService.Descriptor;
import org.twinlife.twinlife.ConversationService.InvitationDescriptor;
import org.twinlife.twinlife.ConversationService.ObjectDescriptor;
import org.twinlife.twinlife.ConversationService.TwincodeDescriptor;
import org.twinlife.twinlife.ConversationService.UpdateType;
import org.twinlife.twinlife.Filter;
import org.twinlife.twinlife.ImageId;
import org.twinlife.twinlife.ImageService;
import org.twinlife.twinlife.Notification;
import org.twinlife.twinlife.NotificationService.NotificationType;
import org.twinlife.twinlife.Offer;
import org.twinlife.twinlife.TerminateReason;
import org.twinlife.twinlife.Twincode;
import org.twinlife.twinlife.TwincodeOutbound;
import org.twinlife.twinlife.util.Logger;
import org.twinlife.twinme.FeatureUtils;
import org.twinlife.twinme.NotificationCenter;
import org.twinlife.twinme.TwinmeContext;
import org.twinlife.twinme.models.AccountMigration;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Group;
import org.twinlife.twinme.models.GroupMember;
import org.twinlife.twinme.models.Invitation;
import org.twinlife.twinme.models.Originator;
import org.twinlife.twinme.services.AccountMigrationService;
import org.twinlife.twinme.calls.CallService;
import org.twinlife.twinme.services.PeerService;
import org.twinlife.twinme.calls.CallStatus;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.Settings;
import org.twinlife.twinme.ui.ShowContactActivity;
import org.twinlife.twinme.ui.TwinmeApplication;
import org.twinlife.twinme.ui.accountMigrationActivity.AccountMigrationActivity;
import org.twinlife.twinme.ui.callActivity.CallActivity;
import org.twinlife.twinme.ui.conversationActivity.ConversationActivity;
import org.twinlife.twinme.ui.conversationActivity.UIReaction;
import org.twinlife.twinme.ui.exportActivity.ExportActivity;
import org.twinlife.twinme.ui.groups.ShowGroupActivity;
import org.twinlife.twinme.ui.mainActivity.MainActivity;
import org.twinlife.twinme.utils.CommonUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class NotificationCenterImpl implements NotificationCenter {
    private static final String LOG_TAG = "NotificationCenterImpl";
    private static final boolean DEBUG = false;

    private static final int MAX_SHORTCUTS = 4;

    // Old notification channel used in Twinme 8.x and must now be removed on startup.
    private static final String CHANNEL_OLD_ID = "notification-id";

    // Old notification channel used in Twinme 9.x up to 14.1
    private static final String CHANNEL_AUDIO_V1 = "notification-11-audio";
    private static final String CHANNEL_VIDEO_V1 = "notification-12-video";

    // One notification channel by type of notification.
    // The number is used to sort the notification in a well defined order on the system preference.
    // Notification channels persist in the system: they must be explicitly removed when they are renamed or no longer used.
    // see https://developer.android.com/training/notify-user/channels
    private static final String CHANNEL_NOTIF = "notification-10-messages";
    private static final String CHANNEL_AUDIO = "notification-11-audio2";
    private static final String CHANNEL_VIDEO = "notification-12-video2";
    private static final String CHANNEL_MISSED_CALL = "notification-13-missed-call";
    private static final String CHANNEL_CONTACT = "notification-14-contact";
    private static final String CHANNEL_GROUP = "notification-15-group";

    // System notification channel (low priority).
    private static final String CHANNEL_SYSTEM = "notification-16-any";

    private static final String NOTIFICATION_CENTER_PREFERENCES = "NotificationCenter";
    private static final String NOTIFICATION_SEQUENCE = "notificationSequence";
    private static final String CHANNEL_BASE_ID = "channelBaseId";

    // Introduced in twinme 19.2 to solve the application sound URI issue.
    private static final String CHANNEL_BASE_IDS = "channelBaseIds";

    private static final String MESSAGE_NOTIFICATION_GROUP = "org.twinlife.twinme.MESSAGE_NOTIFICATION_GROUP";

    private static final int CHANNEL_COUNT = 7;

    private static final boolean SYSTEM_NOTIFICATION_ON_CONTACT_UPDATE = true;

    private static final String DESCRIPTOR_ID_EXTRA = "DESCRIPTOR_ID";

    private static class SystemNotification {

        UUID sessionId;
        final int id;

        SystemNotification(int id, UUID sessionId) {

            this.id = id;
            this.sessionId = sessionId;
        }
    }

    private static class NewMessageNotification extends SystemNotification {

        final AtomicInteger count = new AtomicInteger(0);

        NewMessageNotification(int id, UUID sessionId) {

            super(id, sessionId);
        }
    }

    private final TwinmeApplication mTwinmeApplication;
    private final TwinmeContext mTwinmeContext;
    private final Application mApplication;

    private final NotificationManagerCompat mNotificationManager;
    private final Badger mBadger;
    private final int[] mBaseIds;
    private int mNotificationId;
    private String mAudioChannel;
    private String mVideoChannel;
    private String mMissedCallChannel;
    private String mMessageChannel;
    private String mGroupChannel;
    private String mContactChannel;
    private String mDefaultChannel;
    @SuppressLint("UseSparseArrays")
    private final HashMap<UUID, SystemNotification> mConversationId2Notifications = new HashMap<>();
    private int mLastNotificationId;
    @NonNull
    private final SharedPreferences mSharedPreferences;
    @Nullable
    private android.app.Notification mPlaceholderCallNotification = null;

    public NotificationCenterImpl(@NonNull Application application, @NonNull TwinmeApplication twinmeApplication, @NonNull TwinmeContext twinmeContext) {
        if (DEBUG) {
            Log.d(LOG_TAG, "NotificationCenter: twinmeApplication=" + twinmeApplication + " twinmeContext=" + twinmeContext);
        }

        mTwinmeApplication = twinmeApplication;
        mTwinmeContext = twinmeContext;
        mApplication = application;
        mBaseIds = new int[CHANNEL_COUNT];

        // Make the system notification ID persistent.
        // Allocate them in sequences of 10 and save in the preference the last sequence allocated.
        // The first available ID when we restart is the last sequence number.
        mSharedPreferences = mApplication.getSharedPreferences(NOTIFICATION_CENTER_PREFERENCES, android.content.Context.MODE_PRIVATE);
        mLastNotificationId = mSharedPreferences.getInt(NOTIFICATION_SEQUENCE, 10);
        mNotificationId = mLastNotificationId;

        mNotificationManager = NotificationManagerCompat.from(mApplication);

        createNotificationChannels();

        mBadger = Badger.getBadger(mApplication, new ComponentName(mApplication.getPackageName(), MainActivity.class.getName()));
    }

    public void resetNotificationChannels() {
        if (DEBUG) {
            Log.d(LOG_TAG, "resetNotificationChannels");
        }

        deleteNotificationChannels();
        createNotificationChannels();
    }

    @Override
    public void onIncomingCall(@NonNull Originator contact, @Nullable Bitmap avatar, @NonNull UUID peerConnectionId, @NonNull Offer offer) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onIncomingCall: contact=" + contact + " peerConnectionId=" + peerConnectionId + " offer=" + offer);
        }

        CallService.startService(mApplication, contact, avatar, peerConnectionId, offer);
    }

    @Override
    public void onIncomingAccountMigration(@NonNull AccountMigration accountMigration, @NonNull UUID peerConnectionId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onIncomingDeviceMigration: deviceMigration=" + accountMigration + " peerConnectionId=" + peerConnectionId);
        }

        Intent serviceIntent = new Intent(mApplication, AccountMigrationService.class);
        serviceIntent.setPackage(mApplication.getPackageName());
        serviceIntent.putExtra(AccountMigrationService.PARAM_PEER_CONNECTION_ID, peerConnectionId.toString());
        serviceIntent.putExtra(AccountMigrationService.PARAM_ACCOUNT_MIGRATION_ID, accountMigration.getId().toString());

        serviceIntent.setAction(AccountMigrationService.ACTION_INCOMING_MIGRATION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mApplication.startForegroundService(serviceIntent);
        } else {
            mApplication.startService(serviceIntent);
        }
    }

    @Override
    public void onPopDescriptor(@NonNull Originator contact, @NonNull Conversation conversation, @NonNull UUID sessionId, @NonNull Descriptor descriptor) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPopDescriptor: contact=" + contact + " conversation=" + conversation + " descriptor=" + descriptor);
        }

        SpannableStringBuilder notificationMessage = null;
        NotificationType type = null;

        boolean displayNotificationSender = mTwinmeApplication.getDisplayNotificationSender() && !contact.getIdentityCapabilities().hasDiscreet();
        boolean displayNotificationContent = mTwinmeApplication.getDisplayNotificationContent() && !contact.getIdentityCapabilities().hasDiscreet();

        long timestamp = -1;

        switch (descriptor.getType()) {
            case OBJECT_DESCRIPTOR:
                ObjectDescriptor objectDescriptor = (ObjectDescriptor) descriptor;
                String message = objectDescriptor.getMessage();
                timestamp = objectDescriptor.getCreatedTimestamp();
                type = NotificationType.NEW_TEXT_MESSAGE;
                if (displayNotificationContent && descriptor.getExpireTimeout() <= 0) {
                    notificationMessage = org.twinlife.twinme.utils.Utils.formatText(message, 0);
                } else {
                    notificationMessage = new SpannableStringBuilder(mApplication.getString(R.string.notification_center_message_received));
                }
                break;

            case IMAGE_DESCRIPTOR:
                type = NotificationType.NEW_IMAGE_MESSAGE;
                break;

            case AUDIO_DESCRIPTOR:
                type = NotificationType.NEW_AUDIO_MESSAGE;
                break;

            case VIDEO_DESCRIPTOR:
                type = NotificationType.NEW_VIDEO_MESSAGE;
                break;

            case NAMED_FILE_DESCRIPTOR:
                type = NotificationType.NEW_FILE_MESSAGE;
                break;

            case INVITATION_DESCRIPTOR:
                InvitationDescriptor invitation = (InvitationDescriptor) descriptor;
                if (invitation.getStatus() == InvitationDescriptor.Status.PENDING) {
                    type = NotificationType.NEW_GROUP_INVITATION;
                    if (displayNotificationSender && displayNotificationContent) {
                        notificationMessage = new SpannableStringBuilder(mApplication.getString(R.string.notification_center_group_invitation));
                    } else if (displayNotificationSender) {
                        notificationMessage = new SpannableStringBuilder(mApplication.getString(R.string.notification_center_message_received));
                    } else if (displayNotificationContent) {
                        notificationMessage = new SpannableStringBuilder(mApplication.getString(R.string.notification_center_group_invitation_received));
                    } else {
                        notificationMessage = new SpannableStringBuilder(mApplication.getString(R.string.notification_center_message_received));
                    }
                }
                break;

            case TWINCODE_DESCRIPTOR:
                TwincodeDescriptor twincodeDescriptor = (TwincodeDescriptor) descriptor;
                if (twincodeDescriptor.getSchemaId().equals(Invitation.SCHEMA_ID)) {
                    type = NotificationType.NEW_CONTACT_INVITATION;
                    if (displayNotificationSender && displayNotificationContent) {
                        notificationMessage = new SpannableStringBuilder(mApplication.getString(R.string.notification_center_group_invitation));
                    } else if (displayNotificationSender) {
                        notificationMessage = new SpannableStringBuilder(mApplication.getString(R.string.notification_center_message_received));
                    } else if (displayNotificationContent) {
                        notificationMessage = new SpannableStringBuilder(mApplication.getString(R.string.notification_center_group_invitation_received));
                    } else {
                        notificationMessage = new SpannableStringBuilder(mApplication.getString(R.string.notification_center_message_received));
                    }
                }
                break;

            case CLEAR_DESCRIPTOR:
                type = NotificationType.RESET_CONVERSATION;
                break;

            default:
                break;
        }

        if (type == null) {

            return;
        }
        messageNotification(contact, sessionId, conversation, notificationMessage, timestamp, type, descriptor, null);
    }

    /**
     * Build and publish the list of direct share targets.
     * See <a href="https://developer.android.com/training/sharing/direct-share-targets">Provide Direct Share targets</a>
     *
     * @param descriptors all conversation/last descriptor pairs.
     *                    Only the first (i.e. more recent) few conversations are published as share targets (see MAX_SHORTCUTS).
     */
    public void setDynamicShortcuts(@Nullable Map<Conversation, Descriptor> descriptors) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setDynamicShortcuts: descriptors=" + descriptors);
        }

        if (!(mTwinmeApplication instanceof Application) || descriptors == null) {
            return;
        }

        if (!mTwinmeApplication.getDisplayNotificationSender() || mTwinmeApplication.screenLocked()) {
            ShortcutManagerCompat.removeAllDynamicShortcuts(mApplication.getApplicationContext());
            return;
        }

        Context context = (Application) mTwinmeApplication;

        // Sort conversations by last message date, newer first
        List<Map.Entry<ConversationService.Conversation, ConversationService.Descriptor>> sortedConversations = new ArrayList<>(descriptors.entrySet());
        Collections.sort(sortedConversations, (c1, c2) -> {
            ConversationService.Descriptor d1 = c1.getValue();
            ConversationService.Descriptor d2 = c2.getValue();

            if (d1 == null && d2 == null) {
                return 0;
            } else if (d1 == null) {
                return 1;
            } else if (d2 == null) {
                return -1;
            }

            return Long.compare(d2.getCreatedTimestamp(), d1.getCreatedTimestamp());
        });

        List<ShortcutInfoCompat> shortcuts = new ArrayList<>();

        for (Map.Entry<ConversationService.Conversation, ConversationService.Descriptor> entry : sortedConversations) {
            if (shortcuts.size() >= MAX_SHORTCUTS) {
                break;
            }

            ConversationService.Conversation conversation = entry.getKey();

            if (conversation.getSubject() instanceof Originator) {
                Originator originator = (Originator) conversation.getSubject();

                if (originator.getSpace() != null && originator.getSpace().isSecret() || originator.getIdentityCapabilities().hasDiscreet()) {
                    // Don't create shortcuts to secret/discreet contacts.
                    continue;
                }

                Bitmap avatar = null;
                if (originator.getAvatarId() != null) {
                    avatar = mTwinmeContext.getImageService().getImage(originator.getAvatarId(), ImageService.Kind.THUMBNAIL);
                }

                if (avatar == null && originator.isGroup()) {
                    avatar = mTwinmeApplication.getDefaultGroupAvatar();
                }

                ShortcutInfoCompat shortcutInfo = CommonUtils.buildShortcutInfo(context, originator, avatar, ConversationActivity.class, true);

                if (shortcutInfo != null) {
                    shortcuts.add(shortcutInfo);
                }
            }
        }

        ShortcutManagerCompat.setDynamicShortcuts(context, shortcuts);
    }

    @Override
    public void pushDynamicShortcut(@NonNull Originator originator, boolean incoming) {
        if (DEBUG) {
            Log.d(LOG_TAG, "pushDynamicShortcut: originator=" + originator);
        }
        ShortcutInfoCompat shortcutInfo = CommonUtils.buildShortcutInfo(mApplication, originator, getAvatar(originator), ConversationActivity.class, incoming);

        if (shortcutInfo != null) {
            ShortcutManagerCompat.pushDynamicShortcut(mApplication, shortcutInfo);
        }
    }

    @Override
    public void removeAllDynamicShortcuts() {
        if (DEBUG) {
            Log.d(LOG_TAG, "removeDynamicShortcuts");
        }

        ShortcutManagerCompat.removeAllDynamicShortcuts(mApplication);
    }

    @Override
    public void acknowledgeReply(@NonNull UUID conversationId, @Nullable String reply) {
        if (DEBUG) {
            Log.d(LOG_TAG, "acknowledgeReply: conversationId=" + conversationId + " reply=" + reply);
        }

        synchronized (this) {
            SystemNotification systemNotification = mConversationId2Notifications.get(conversationId);

            if (systemNotification != null) {

                for (StatusBarNotification statusBarNotification : mNotificationManager.getActiveNotifications()) {
                    if (statusBarNotification.getId() == systemNotification.id) {

                        if (reply == null) {
                            // Even if we have no reply we post the notification again to let Android know it can hide the notification.
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(mApplication, statusBarNotification.getNotification())
                                    .setSilent(true);
                            mNotificationManager.notify(systemNotification.id, builder.build());
                        } else {
                            NotificationCompat.MessagingStyle.Message message = new NotificationCompat.MessagingStyle.Message(reply, System.currentTimeMillis(), (Person) null);
                            updateMessageNotification(systemNotification.id, message, null, true);
                        }

                        return;
                    }
                }
            }
        }
    }

    @Override
    public void onUpdateDescriptor(@NonNull Originator contact, @NonNull Conversation conversation, @NonNull Descriptor descriptor, UpdateType updateType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateDescriptor: contact=" + contact + " conversation=" + conversation + " descriptor=" + descriptor + " updateType=" + updateType);
        }

        if (descriptor instanceof ConversationService.FileDescriptor && !((ConversationService.FileDescriptor) descriptor).isAvailable()) {
            // New file chunk received: don't post a new notification unless the transfer is finished.
            return;
        }

        if (updateType == UpdateType.CONTENT) {
            onPopDescriptor(contact, conversation, conversation.getPeerConnectionId(), descriptor);
        }
    }

    @Override
    public void onUpdateAnnotation(@NonNull Originator contact, @NonNull Conversation conversation, @NonNull Descriptor descriptor,
                            @NonNull TwincodeOutbound annotatingUser) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateAnnotation: contact=" + contact + " conversation=" + conversation + " descriptor=" + descriptor
                    + " annotatingUser=" + annotatingUser);
        }

        messageNotification(contact, null, conversation, new SpannableStringBuilder(mApplication.getString(R.string.notification_center_reaction_message_received)), -1,
                NotificationType.UPDATED_ANNOTATION, descriptor, annotatingUser);
    }

    @Override
    public void onJoinGroup(@NonNull Originator group, @NonNull Conversation conversation) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onJoinGroup: group=" + group);
        }

        messageNotification(group, null, conversation, null, -1, NotificationType.NEW_GROUP_JOINED, null, null);
    }

    @Override
    public void onLeaveGroup(@NonNull ConversationService.GroupConversation conversation) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onLeaveGroup: conversation=" + conversation);
        }

        if (conversation.getSubject() instanceof Group) {
            Group group = (Group) conversation.getSubject();
            ShortcutManagerCompat.removeLongLivedShortcuts(mApplication, Collections.singletonList(group.getType() + "_" + group.getId()));
        }
    }

    /**
     * Clean up notification cache and cancel summary notification if appropriate.
     * @param conversationId The ID of the conversation whose notification was deleted (either by swipe or "Clear all" button).
     */
    void onMessageNotificationDeleted(@NonNull UUID conversationId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onMessageNotificationDeleted: conversationId=" + conversationId);
        }

        synchronized (this) {
            mConversationId2Notifications.remove(conversationId);
            updateMessageSummary();
        }
    }


    private synchronized boolean updateMessageNotification(int notificationId, @NonNull NotificationCompat.MessagingStyle.Message message, @Nullable Person sender, boolean silent) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateMessageNotification: notificationId=" + notificationId + " message=" + message + " silent=" + silent);
        }

        String descriptorId = message.getExtras().getString(DESCRIPTOR_ID_EXTRA);

        for (StatusBarNotification statusBarNotification : mNotificationManager.getActiveNotifications()) {
            if (statusBarNotification.getId() == notificationId) {
                NotificationCompat.MessagingStyle messagingStyle = NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(statusBarNotification.getNotification());
                if (messagingStyle != null) {

                    List<NotificationCompat.MessagingStyle.Message> messages = messagingStyle.getMessages();

                    // If there is already a message with the same descriptor ID, the new message is actually and edit =>
                    // remove the original message and add the edited one.
                    NotificationCompat.MessagingStyle.Message originalMessage = null;
                    for (NotificationCompat.MessagingStyle.Message msg : messages) {
                        String dId = msg.getExtras().getString(DESCRIPTOR_ID_EXTRA);

                        if (descriptorId != null && descriptorId.equals(dId)) {
                            originalMessage = msg;
                        }
                    }

                    if (originalMessage != null) {
                        messages.remove(originalMessage);
                        Bundle extras = message.getExtras();
                        message = new NotificationCompat.MessagingStyle.Message(message.getText(), message.getTimestamp(), message.getPerson());
                        message.getExtras().putAll(extras);

                        // Don't ring/vibrate for edited messages
                        silent = true;
                    }

                    messagingStyle.addMessage(message);

                    try {
                        // In case messages arrived out of order, or the new message is an edit.
                        Collections.sort(messages, (m1, m2) -> Long.compare(m1.getTimestamp(), m2.getTimestamp()));
                    } catch (UnsupportedOperationException e) {
                        // Should not happen as messagingStyle.getMessages() should return an ArrayList.
                    }

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(mApplication, statusBarNotification.getNotification())
                            .setSilent(silent)
                            .setNumber(messages.size())
                            .setStyle(messagingStyle);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (sender != null && sender.getIcon() != null) {
                            builder.setLargeIcon(sender.getIcon().toIcon(mApplication));
                        }
                    }

                    mNotificationManager.notify(statusBarNotification.getId(), builder.build());
                    updateMessageSummary();
                    return true;
                }
            }
        }

        return false;
    }

    private synchronized void updateMessageSummary() {
        int nbMessages = 0;
        for (StatusBarNotification statusBarNotification : mNotificationManager.getActiveNotifications()) {
            NotificationCompat.MessagingStyle messagingStyle = NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(statusBarNotification.getNotification());

            if (messagingStyle != null) {
                for (NotificationCompat.MessagingStyle.Message message : messagingStyle.getMessages()) {
                    if (message.getPerson() != null && message.getPerson().getUri() != null) {
                        nbMessages++;
                    }
                }
            }
        }

        if (nbMessages == 0) {
            mNotificationManager.cancel(MESSAGE_SUMMARY_NOTIFICATION_ID);
        } else {
            SpannableStringBuilder summaryLabel = new SpannableStringBuilder(nbMessages + " " + mApplication.getString(R.string.notification_channel_message_title));

            NotificationCompat.Builder summaryBuilder = new NotificationCompat.Builder(mApplication, mMessageChannel)
                    .setSmallIcon(R.drawable.logo_small)
                    .setGroup(MESSAGE_NOTIFICATION_GROUP)
                    .setGroupSummary(true)
                    .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN)
                    .setContentTitle(summaryLabel)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setStyle(new NotificationCompat.InboxStyle()
                            .setSummaryText(summaryLabel));

            mNotificationManager.notify(MESSAGE_SUMMARY_NOTIFICATION_ID, summaryBuilder.build());
        }
    }

    private void messageNotification(@NonNull Originator contact, UUID sessionId, @NonNull Conversation conversation,
                                     SpannableStringBuilder notificationMessage, long timestamp,
                                     NotificationType notificationType, @Nullable Descriptor descriptor,
                                     @Nullable TwincodeOutbound annotatingUser) {
        if (DEBUG) {
            Log.d(LOG_TAG, "messageNotification: contact=" + contact + " conversation=" + conversation + " msg=" + notificationMessage + " type=" + notificationType);
        }

        String channelId = mMessageChannel;

        // For a group notification, we receive the message on the member conversationId and we must associate
        // the notification to the group conversationId so that onSetActiveConversation() can remove the notification.
        if (conversation instanceof GroupMemberConversation) {
            GroupMemberConversation groupMemberConversation = (GroupMemberConversation) conversation;
            conversation = groupMemberConversation.getGroupConversation();

            channelId = mGroupChannel;
        } else if (contact instanceof Group) {
            channelId = mGroupChannel;
        }

        String senderName;
        String groupName = null;
        Bitmap senderAvatar;
        Bitmap groupAvatar = null;
        if (contact instanceof GroupMember) {
            channelId = mGroupChannel;
            GroupMember groupMember = (GroupMember) contact;

            senderName = groupMember.getName();
            if (notificationType == NotificationType.UPDATED_ANNOTATION && annotatingUser != null) {
                senderName = annotatingUser.getName();
            }

            groupName = groupMember.getGroup().getName();

            if (notificationType == NotificationType.UPDATED_ANNOTATION && annotatingUser != null) {
                senderAvatar = getAvatarWithAvatarId(annotatingUser.getAvatarId());
            } else {
                senderAvatar = getAvatar(groupMember);
            }

            groupAvatar = getAvatar(groupMember.getGroup());
        } else {
            senderName = contact.getName();
            senderAvatar = getAvatar(contact);
        }

        int notificationId;
        NewMessageNotification newMessageNotification = null;
        synchronized (this) {
            SystemNotification notification = mConversationId2Notifications.get(conversation.getId());
            if (notification != null) {
                newMessageNotification = (NewMessageNotification) notification;
            }
        }
        boolean wasNotified = false;
        if (newMessageNotification == null) {
            notificationId = newNotificationId();
            newMessageNotification = new NewMessageNotification(notificationId, sessionId);
            synchronized (this) {
                mConversationId2Notifications.put(conversation.getId(), newMessageNotification);
            }
        } else {
            notificationId = newMessageNotification.id;
            if (sessionId != null && sessionId.equals(newMessageNotification.sessionId)) {
                wasNotified = true;
            } else if (sessionId != null) {
                newMessageNotification.sessionId = sessionId;
            }
        }
        int count;

        // System push notification that was already notified, we can ignore.
        if (wasNotified && notificationMessage == null) {
            return;
        }

        boolean displayNotificationSender = mTwinmeApplication.getDisplayNotificationSender() && !contact.getIdentityCapabilities().hasDiscreet();
        boolean displayNotificationContent = mTwinmeApplication.getDisplayNotificationContent() && !contact.getIdentityCapabilities().hasDiscreet();

        // Get a default message for some notifications.
        if (notificationMessage == null && notificationType != null) {
            switch (notificationType) {
                case NEW_TEXT_MESSAGE:
                    notificationMessage = new SpannableStringBuilder(mApplication.getString(R.string.notification_center_message_received));
                    break;

                case NEW_IMAGE_MESSAGE:
                    if (displayNotificationSender && displayNotificationContent) {
                        notificationMessage = new SpannableStringBuilder(mApplication.getString(R.string.notification_center_photo_message));
                    } else if (displayNotificationSender) {
                        notificationMessage = new SpannableStringBuilder(mApplication.getString(R.string.notification_center_message_received));
                    } else if (displayNotificationContent) {
                        notificationMessage = new SpannableStringBuilder(mApplication.getString(R.string.notification_center_photo_message_received));
                    } else {
                        notificationMessage = new SpannableStringBuilder(mApplication.getString(R.string.notification_center_message_received));
                    }
                    break;

                case NEW_AUDIO_MESSAGE:
                    if (displayNotificationSender && displayNotificationContent) {
                        notificationMessage = new SpannableStringBuilder(mApplication.getString(R.string.notification_center_audio_message));
                    } else if (displayNotificationSender) {
                        notificationMessage = new SpannableStringBuilder(mApplication.getString(R.string.notification_center_message_received));
                    } else if (displayNotificationContent) {
                        notificationMessage = new SpannableStringBuilder(mApplication.getString(R.string.notification_center_audio_message_received));
                    } else {
                        notificationMessage = new SpannableStringBuilder(mApplication.getString(R.string.notification_center_message_received));
                    }
                    break;

                case NEW_VIDEO_MESSAGE:
                    if (displayNotificationSender && displayNotificationContent) {
                        notificationMessage = new SpannableStringBuilder(mApplication.getString(R.string.notification_center_video_message));
                    } else if (displayNotificationSender) {
                        notificationMessage = new SpannableStringBuilder(mApplication.getString(R.string.notification_center_message_received));
                    } else if (displayNotificationContent) {
                        notificationMessage = new SpannableStringBuilder(mApplication.getString(R.string.notification_center_video_message_received));
                    } else {
                        notificationMessage = new SpannableStringBuilder(mApplication.getString(R.string.notification_center_message_received));
                    }
                    break;

                case NEW_FILE_MESSAGE:
                    if (displayNotificationSender && displayNotificationContent) {
                        notificationMessage = new SpannableStringBuilder(mApplication.getString(R.string.notification_center_file_message));
                    } else if (displayNotificationSender) {
                        notificationMessage = new SpannableStringBuilder(mApplication.getString(R.string.notification_center_message_received));
                    } else if (displayNotificationContent) {
                        notificationMessage = new SpannableStringBuilder(mApplication.getString(R.string.notification_center_file_message_received));
                    } else {
                        notificationMessage = new SpannableStringBuilder(mApplication.getString(R.string.notification_center_message_received));
                    }
                    break;

                case NEW_GROUP_INVITATION:
                case NEW_CONTACT_INVITATION:
                    if (displayNotificationSender && displayNotificationContent) {
                        notificationMessage = new SpannableStringBuilder(mApplication.getString(R.string.notification_center_group_invitation));
                    } else if (displayNotificationSender) {
                        notificationMessage = new SpannableStringBuilder(mApplication.getString(R.string.notification_center_message_received));
                    } else if (displayNotificationContent) {
                        notificationMessage = new SpannableStringBuilder(mApplication.getString(R.string.notification_center_group_invitation_received));
                    } else {
                        notificationMessage = new SpannableStringBuilder(mApplication.getString(R.string.notification_center_message_received));
                    }
                    break;

                case NEW_GROUP_JOINED:
                    notificationMessage = new SpannableStringBuilder(mApplication.getString(R.string.notification_center_join_group));
                    break;

                case RESET_CONVERSATION:
                    if (displayNotificationSender && displayNotificationContent) {
                        notificationMessage = new SpannableStringBuilder(mApplication.getString(R.string.notifications_fragment_item_cleanup_message));
                    } else if (displayNotificationSender) {
                        notificationMessage = new SpannableStringBuilder(mApplication.getString(R.string.notification_center_message_received));
                    } else if (displayNotificationContent) {
                        notificationMessage = new SpannableStringBuilder(mApplication.getString(R.string.notification_center_cleanup_conversation));
                    } else {
                        notificationMessage = new SpannableStringBuilder(mApplication.getString(R.string.notification_center_message_received));
                    }
                    break;

                case UPDATED_ANNOTATION:
                    if (displayNotificationSender && displayNotificationContent) {
                        notificationMessage = new SpannableStringBuilder(mApplication.getString(R.string.notification_center_reaction_message_received));
                    } else {
                        notificationMessage = new SpannableStringBuilder(mApplication.getString(R.string.notification_center_reaction_message));
                    }
                    break;

                case DELETED_GROUP:
                default:
                    // We don't have a message, we don't want these notifications.
                    return;
            }
        }

        // Increment only new notifications.
        if (!wasNotified) {
            count = newMessageNotification.count.getAndIncrement();
        } else {
            count = newMessageNotification.count.get();
        }

        if (notificationType == NotificationType.UPDATED_ANNOTATION && (descriptor != null && !descriptor.getTwincodeOutboundId().equals(contact.getTwincodeOutboundId()))) {
            return;
        }

        Notification notification = null;
        if (notificationType != null) {
            notification = mTwinmeContext.createNotification(notificationType, notificationId, contact,
                    descriptor == null ? null : descriptor.getDescriptorId(), annotatingUser);
        }

        NotificationCompat.MessagingStyle.Message message = null;

        if (notification != null && notificationType.isMessagingStyle()) {

            if (notificationType == NotificationType.UPDATED_ANNOTATION && !mTwinmeApplication.getDisplayNotificationLike()) {
                return;
            }

            if (notificationType == NotificationType.UPDATED_ANNOTATION) {
                String emoji = emojiFromAnnotationValue(UIReaction.ReactionType.values()[notification.getAnnotationValue()]);
                if (!emoji.isEmpty() && displayNotificationContent) {
                    notificationMessage = new SpannableStringBuilder(String.format(mApplication.getString(R.string.notification_center_reaction), emoji));
                }
            }

            Person sender = new Person.Builder()
                    .setKey(contact.getShortcutId())
                    .setIcon(CommonUtils.bitmapToAdaptiveIcon(displayNotificationSender ? senderAvatar : mTwinmeApplication.getAnonymousAvatar()))
                    .setName(displayNotificationSender && !TextUtils.isEmpty(senderName) ? senderName : mTwinmeApplication.getAnonymousName())
                    .build();

            message = new NotificationCompat.MessagingStyle.Message(notificationMessage, timestamp, sender);

            /*
            // We can also add images to the notification. It doesn't always work (e.g. if there are
            // multiple messages), and the image needs to be exposed through a ContentProvider
            // which may cause privacy issues.
            if (descriptor != null && descriptor.getType() == Descriptor.Type.IMAGE_DESCRIPTOR) {

                File path = new File(mTwinmeContext.getFilesDir(), ((ConversationService.ImageDescriptor) descriptor).getPath());
                try {
                    Uri uri = FileProvider.getUriForFile(mApplication, BuildConfig.FILE_PROVIDER, path);
                    message.setData("image/", uri);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "couldn't load image", e);
                }
            }*/

            if (descriptor != null) {
                message.getExtras().putString(DESCRIPTOR_ID_EXTRA, descriptor.getDescriptorId().toString());
            }

            if (updateMessageNotification(notificationId, message, sender, false)) {
                // There was an existing notification for this conversation and we updated it, nothing left to do.
                return;
            }
        }

        Intent conversationIntent = new Intent(mApplication, MainActivity.class);
        conversationIntent.putExtra(Intents.INTENT_SHOW_SPLASHSCREEN, false);
        if (notificationType == NotificationType.NEW_GROUP_INVITATION && descriptor != null) {
            conversationIntent.putExtra(Intents.INTENT_NEW_INVITATION, true);
            conversationIntent.putExtra(Intents.INTENT_INVITATION_ID, descriptor.getDescriptorId().toString());
        } else if (notificationType == NotificationType.NEW_CONTACT_INVITATION && descriptor != null) {
            conversationIntent.putExtra(Intents.INTENT_NEW_CONTACT_INVITATION, true);
            if (contact.isGroup()) {
                conversationIntent.putExtra(Intents.INTENT_GROUP_ID, contact.getId().toString());
            } else {
                conversationIntent.putExtra(Intents.INTENT_CONTACT_ID, contact.getId().toString());
            }
            if (notification != null) {
                conversationIntent.putExtra(Intents.INTENT_NOTIFICATION_ID, notification.getId().toString());
            }
        } else {
            conversationIntent.putExtra(Intents.INTENT_NEW_MESSAGE, true);
        }
        if (contact instanceof GroupMember) {
            GroupMember groupMember = (GroupMember) contact;
            Originator owner = groupMember.getGroup();

            if (owner instanceof Contact) {
                conversationIntent.putExtra(Intents.INTENT_CONTACT_ID, owner.getId().toString());
            } else {
                conversationIntent.putExtra(Intents.INTENT_GROUP_ID, owner.getId().toString());
            }
        } else if (contact.isGroup()) {
            // The NEW_GROUP_JOINED notification is received on the Group object so we have a group id.
            conversationIntent.putExtra(Intents.INTENT_GROUP_ID, contact.getId().toString());

        } else {
            conversationIntent.putExtra(Intents.INTENT_CONTACT_ID, contact.getId().toString());
        }
        PendingIntent conversationPendingIntent = createPendingIntent(notificationId, conversationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mApplication, channelId);
        if (displayNotificationSender) {
            notificationBuilder.setContentTitle(senderName);
            notificationBuilder.setLargeIcon(senderAvatar);
        }

        notificationBuilder.setContentIntent(conversationPendingIntent);
        notificationBuilder.setContentText(notificationMessage);
        notificationBuilder.setLights(Design.BLUE_NORMAL, 1000, 500);
        notificationBuilder.setSmallIcon(R.drawable.logo_small);
        notificationBuilder.setCategory(NotificationCompat.CATEGORY_MESSAGE);
        notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        notificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        notificationBuilder.setNumber(count);
        if (notificationType == NotificationType.NEW_GROUP_INVITATION) {
            notificationBuilder.setAutoCancel(true);
        }

        Uri ringtoneUri = mTwinmeApplication.getRingtone(TwinmeApplication.RingtoneType.NOTIFICATION_RINGTONE);
        if (ringtoneUri != null) {
            notificationBuilder.setSound(ringtoneUri);
        }

        if (mTwinmeApplication.getVibration(TwinmeApplication.RingtoneType.NOTIFICATION_RINGTONE)) {
            long[] pattern = {0L, 500L};
            notificationBuilder.setVibrate(pattern);
        }

        if (message != null) {
            boolean isGroup = contact.isGroup() || contact instanceof GroupMember;

            IconCompat youAvatar = null;

            if (displayNotificationSender && contact.getIdentityAvatarId() != null) {
                Bitmap avatar = mTwinmeContext.getImageService().getImage(contact.getIdentityAvatarId(), ImageService.Kind.THUMBNAIL);
                if (avatar != null) {
                    youAvatar = CommonUtils.bitmapToAdaptiveIcon(avatar);
                }
            }

            String youName = displayNotificationSender && !TextUtils.isEmpty(contact.getIdentityName()) ?
                    contact.getIdentityName() :
                    mApplication.getResources().getString(R.string.conversations_fragment_you);

            Person you = new Person.Builder()
                    .setName(youName)
                    .setIcon(youAvatar)
                    .build();

            NotificationCompat.MessagingStyle messagingStyle = new NotificationCompat.MessagingStyle(you).setGroupConversation(isGroup);
            messagingStyle.addMessage(message);

            if (isGroup && displayNotificationSender) {
                messagingStyle.setConversationTitle(groupName);
            }

            ShortcutInfoCompat shortcutInfo = null;
            if (displayNotificationSender) {
                shortcutInfo = CommonUtils.buildShortcutInfo(mApplication, contact, contact instanceof GroupMember ? groupAvatar : senderAvatar, ConversationActivity.class, true);
            }
            notificationBuilder.setShortcutInfo(shortcutInfo);

            notificationBuilder.setStyle(messagingStyle);
            notificationBuilder.setGroup(MESSAGE_NOTIFICATION_GROUP);
            notificationBuilder.setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN);

            Intent deleteIntent = new Intent(mApplication, NotificationReceiver.class)
                    .setAction(NotificationReceiver.DELETE_ACTION)
                    .putExtra(Intents.INTENT_CONVERSATION_ID, conversation.getId());
            notificationBuilder.setDeleteIntent(PendingIntent.getBroadcast(mApplication, 0, deleteIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // Android < 7 doesn't support notification groups (only the summary will be displayed),
                // nor direct replies.

                if (allowReply(contact, notificationType) && shortcutInfo != null) {
                    String replyLabel = mApplication.getResources().getString(R.string.conversation_activity_menu_item_view_reply_title);
                    RemoteInput remoteInput = new RemoteInput.Builder(NotificationReceiver.KEY_TEXT_REPLY)
                            .setLabel(replyLabel)
                            .build();

                    Intent messageReplyIntent = new Intent(mApplication, NotificationReceiver.class)
                            .setAction(NotificationReceiver.REPLY_ACTION)
                            .putExtra(Intents.INTENT_CONTACT_ID, shortcutInfo.getId())
                            .putExtra(Intents.INTENT_CONVERSATION_ID, conversation.getId());

                    if (notification != null) {
                        messageReplyIntent.putExtra(Intents.INTENT_NOTIFICATION_ID, notification.getId().toString());
                    }

                    int flags = PendingIntent.FLAG_UPDATE_CURRENT;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                        flags |= PendingIntent.FLAG_MUTABLE;
                    }

                    PendingIntent replyPendingIntent =
                            PendingIntent.getBroadcast(mApplication,
                                    (int) conversation.getDatabaseId().getId(), // hopefully we have less than 2 billion conversations in DB.
                                    messageReplyIntent,
                                    flags);

                    NotificationCompat.Action action =
                            new NotificationCompat.Action.Builder(android.R.drawable.ic_menu_send,
                                    replyLabel, replyPendingIntent)
                                    .addRemoteInput(remoteInput)
                                    .build();

                    notificationBuilder.addAction(action);
                }

            }
        }

        mNotificationManager.notify(notificationId, notificationBuilder.build());
        updateMessageSummary();
    }

    private boolean allowReply(@NonNull Originator contact, @NonNull NotificationType notificationType) {
        return (contact.getSpace() == null || !contact.getSpace().isSecret())
                && mTwinmeApplication.getDisplayNotificationSender()
                && !contact.getIdentityCapabilities().hasDiscreet()
                && !mTwinmeApplication.screenLocked()
                && notificationType == NotificationType.NEW_TEXT_MESSAGE;
    }


    @Override
    public void onNewContact(@NonNull Originator contact) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onNewContact: contact=" + contact);
        }

        String callerName = contact.getName();
        Bitmap callerAvatar = getAvatar(contact);

        int notificationId;
        notificationId = newNotificationId();

        String notificationMessage = mApplication.getString(R.string.notification_center_new_contact);

        Intent contactIntent = new Intent(mApplication, MainActivity.class);
        contactIntent.putExtra(Intents.INTENT_SHOW_SPLASHSCREEN, false);
        contactIntent.putExtra(Intents.INTENT_NEW_CONTACT, true);
        contactIntent.putExtra(Intents.INTENT_CONTACT_ID, contact.getId().toString());
        PendingIntent conversationPendingIntent = createPendingIntent(notificationId, contactIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mApplication, mContactChannel);
        notificationBuilder.setContentTitle(callerName);
        notificationBuilder.setContentIntent(conversationPendingIntent);
        notificationBuilder.setContentText(notificationMessage);
        notificationBuilder.setSmallIcon(R.drawable.logo_small);
        notificationBuilder.setLargeIcon(callerAvatar);
        notificationBuilder.setLights(Design.BLUE_NORMAL, 1000, 500);
        notificationBuilder.setCategory(NotificationCompat.CATEGORY_MESSAGE);
        notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        notificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        notificationBuilder.setAutoCancel(true);

        Uri ringtoneUri = mTwinmeApplication.getRingtone(TwinmeApplication.RingtoneType.NOTIFICATION_RINGTONE);
        if (ringtoneUri != null) {
            notificationBuilder.setSound(ringtoneUri);
        }

        if (mTwinmeApplication.getVibration(TwinmeApplication.RingtoneType.NOTIFICATION_RINGTONE)) {
            long[] pattern = {0L, 500L};
            notificationBuilder.setVibrate(pattern);
        }
        mNotificationManager.notify(notificationId, notificationBuilder.build());

        mTwinmeContext.createNotification(NotificationType.NEW_CONTACT, notificationId, contact, null, null);
    }

    @Override
    public void onUnbindContact(@NonNull Originator contact) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUnbindContact: contact=" + contact);
        }

        String callerName = contact.getName();
        Bitmap callerAvatar = getAvatar(contact);

        int notificationId;
        notificationId = newNotificationId();

        String notificationMessage = mApplication.getString(R.string.notification_center_deleted_contact);

        Intent conversationIntent = new Intent(mApplication, MainActivity.class);
        conversationIntent.putExtra(Intents.INTENT_SHOW_SPLASHSCREEN, false);
        conversationIntent.putExtra(Intents.INTENT_CONTACT_ID, contact.getId().toString());
        conversationIntent.putExtra(Intents.INTENT_NEW_CONTACT, true);
        PendingIntent conversationPendingIntent = createPendingIntent(notificationId, conversationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mApplication, mContactChannel);
        notificationBuilder.setContentTitle(callerName);
        notificationBuilder.setContentIntent(conversationPendingIntent);
        notificationBuilder.setContentText(notificationMessage);
        notificationBuilder.setSmallIcon(R.drawable.logo_small);
        notificationBuilder.setLargeIcon(callerAvatar);
        notificationBuilder.setLights(Design.BLUE_NORMAL, 1000, 500);
        notificationBuilder.setCategory(NotificationCompat.CATEGORY_MESSAGE);
        notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        notificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        notificationBuilder.setAutoCancel(true);

        Uri ringtoneUri = mTwinmeApplication.getRingtone(TwinmeApplication.RingtoneType.NOTIFICATION_RINGTONE);
        if (ringtoneUri != null) {
            notificationBuilder.setSound(ringtoneUri);
        }

        if (mTwinmeApplication.getVibration(TwinmeApplication.RingtoneType.NOTIFICATION_RINGTONE)) {
            long[] pattern = {0L, 500L};
            notificationBuilder.setVibrate(pattern);
        }
        mNotificationManager.notify(notificationId, notificationBuilder.build());

        mTwinmeContext.createNotification(NotificationType.DELETED_CONTACT, notificationId, contact, null, null);

        ShortcutManagerCompat.removeLongLivedShortcuts(mApplication, Collections.singletonList(Originator.Type.CONTACT + "_" + contact.getId()));
    }

    @Override
    public void onUpdateContact(@NonNull Originator contact, @NonNull List<AttributeNameValue> previousAttributes) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateContact: contact=" + contact + " updatedAttributes=" + previousAttributes);
        }

        String callerName = contact.getName();
        Bitmap callerAvatar = getAvatar(contact);

        NotificationType type;
        String notificationMessage;
        if (AttributeNameValue.getAttribute(previousAttributes, Twincode.NAME) != null) {
            type = NotificationType.UPDATED_CONTACT;
            notificationMessage = mApplication.getString(R.string.notification_center_updated_contact_name);

        } else if (AttributeNameValue.getAttribute(previousAttributes, Twincode.AVATAR_ID) != null) {
            type = NotificationType.UPDATED_AVATAR_CONTACT;
            notificationMessage = mApplication.getString(R.string.notification_center_updated_contact_avatar);

        } else {

            return;
        }
        int notificationId;

        // Twinme raises the system notification but Skred does not.
        if (SYSTEM_NOTIFICATION_ON_CONTACT_UPDATE) {
            notificationId = newNotificationId();

            Intent conversationIntent = new Intent(mApplication, MainActivity.class);
            conversationIntent.putExtra(Intents.INTENT_SHOW_SPLASHSCREEN, false);
            if (contact instanceof Group) {
                conversationIntent.putExtra(Intents.INTENT_GROUP_ID, contact.getId().toString());
            } else {
                conversationIntent.putExtra(Intents.INTENT_CONTACT_ID, contact.getId().toString());
            }
            conversationIntent.putExtra(Intents.INTENT_NEW_CONTACT, true);
            PendingIntent conversationPendingIntent = createPendingIntent(notificationId, conversationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mApplication, mContactChannel);
            notificationBuilder.setContentTitle(callerName);
            notificationBuilder.setContentIntent(conversationPendingIntent);
            notificationBuilder.setContentText(notificationMessage);
            notificationBuilder.setSmallIcon(R.drawable.logo_small);
            notificationBuilder.setLargeIcon(callerAvatar);
            notificationBuilder.setCategory(NotificationCompat.CATEGORY_MESSAGE);
            notificationBuilder.setPriority(NotificationCompat.PRIORITY_LOW);
            notificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            notificationBuilder.setAutoCancel(true);

            Uri ringtoneUri = mTwinmeApplication.getRingtone(TwinmeApplication.RingtoneType.NOTIFICATION_RINGTONE);
            if (ringtoneUri != null) {
                notificationBuilder.setSound(ringtoneUri);
            }

            if (mTwinmeApplication.getVibration(TwinmeApplication.RingtoneType.NOTIFICATION_RINGTONE)) {
                long[] pattern = {0L, 500L};
                notificationBuilder.setVibrate(pattern);
            }
            mNotificationManager.notify(notificationId, notificationBuilder.build());
        } else {
            notificationId = Notification.NO_NOTIFICATION_ID;
        }
        mTwinmeContext.createNotification(type, notificationId, contact, null, null);

    }

    @Override
    public void onSetActiveConversation(@NonNull Conversation conversation) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSetActiveConversation: conversation=" + conversation);
        }

        synchronized (this) {
            mConversationId2Notifications.remove(conversation.getId());
            if (mConversationId2Notifications.isEmpty()) {
                mNotificationManager.cancel(MESSAGE_SUMMARY_NOTIFICATION_ID);
            }
        }
    }

    @Override
    public void onAcknowledgeNotification(@NonNull Notification notification) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onAcknowledgeNotification: notification=" + notification);
        }

        final int notificationId = notification.getSystemNotificationId();
        if (notificationId != Notification.NO_NOTIFICATION_ID) {
            cancelOrCleanNotification(notificationId);
        }
    }

    /**
     * If the notification is a message notification (i.e. it has a MessagingStyle),
     * remove messages if they're not present in the
     * notification table. Otherwise cancel the notification.
     *
     * <p>Needed in case the sender sends several messages, then deletes one of them
     * while the notification is still present on the recipient's device.
     *
     * @param notificationId The ID of the notification cancel or clean.
     */
    private void cancelOrCleanNotification(int notificationId) {
        UUID tmpCID = null;
        SystemNotification systemNotification = null;

        // Find the cached notification data
        synchronized (this) {
            for (Map.Entry<UUID, SystemNotification> entry : mConversationId2Notifications.entrySet()) {
                if (entry.getValue().id == notificationId) {
                    tmpCID = entry.getKey();
                    systemNotification = entry.getValue();
                    break;
                }
            }
        }
        final UUID conversationId = tmpCID;

        if (conversationId == null || !(systemNotification instanceof NewMessageNotification)) {
            mNotificationManager.cancel(notificationId);
            return;
        }

        NewMessageNotification msgNotif = (NewMessageNotification) systemNotification;

        // Get all unread messages for this conversation (they all share the same system notification ID).
        Filter<Notification> filter = new Filter<Notification>(null) {
            @Override
            public boolean accept(@NonNull Notification notif) {
                return notif.getSystemNotificationId() == notificationId;
            }
        };

        mTwinmeContext.findNotifications(filter, -1, notifs -> {

            if (notifs.isEmpty()) {
                synchronized (this) {
                    mConversationId2Notifications.remove(conversationId);
                    mNotificationManager.cancel(notificationId);
                    updateMessageSummary();
                }
                return;
            }

            Set<ConversationService.DescriptorId> descriptorIds = new HashSet<>();
            for (Notification notif : notifs) {
                if (notif.getDescriptorId() != null) {
                    descriptorIds.add(notif.getDescriptorId());
                }
            }

            synchronized (this) {
                for (StatusBarNotification activeNotification : mNotificationManager.getActiveNotifications()) {
                    if (activeNotification.getId() == msgNotif.id) {
                        NotificationCompat.MessagingStyle messagingStyle = NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(activeNotification.getNotification());
                        if (messagingStyle != null) {
                            // MessagingStyle.getMessages() returns its actual Message ArrayList so we can simply remove the messages through the iterator.
                            Iterator<NotificationCompat.MessagingStyle.Message> msgIterator = messagingStyle.getMessages().iterator();

                            while (msgIterator.hasNext()) {
                                ConversationService.DescriptorId msgDescriptorId = ConversationService.DescriptorId.fromString(msgIterator.next().getExtras().getString(DESCRIPTOR_ID_EXTRA));

                                if (msgDescriptorId != null && !descriptorIds.contains(msgDescriptorId)) {
                                    msgIterator.remove();
                                }
                            }

                            android.app.Notification notif = new NotificationCompat.Builder(mApplication, activeNotification.getNotification())
                                    .setSilent(true)
                                    .setStyle(messagingStyle)
                                    .setNumber(messagingStyle.getMessages().size())
                                    .build();

                            mNotificationManager.notify(activeNotification.getId(), notif);
                            updateMessageSummary();
                            return;
                        }
                    }
                }
            }
        });
    }

    @Override
    public void cancelAll() {
        if (DEBUG) {
            Log.d(LOG_TAG, "cancelAll");
        }

        mNotificationManager.cancelAll();
        mBadger.setBadgeNumber(0);
        synchronized (this) {
            mConversationId2Notifications.clear();
        }
        mNotificationManager.cancel(MESSAGE_SUMMARY_NOTIFICATION_ID);
    }

    @Override
    public void updateApplicationBadgeNumber(int applicationBadgeNumber) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateApplicationBadgeNumber: applicationBadgeNumber=" + applicationBadgeNumber);
        }

        mBadger.setBadgeNumber(applicationBadgeNumber);
    }

    public android.app.Notification createOutgoingCallNotification(@NonNull Originator originator, @NonNull CallStatus callStatus, @Nullable UUID callId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "createOutgoingCallNotification callMode=" + callStatus);
        }

        NotificationCompat.Builder notificationBuilder = createCallNotification(callStatus, originator, null, callId);

        android.app.Notification notification = notificationBuilder.build();
        notification.flags |= NotificationCompat.FLAG_INSISTENT | NotificationCompat.FLAG_NO_CLEAR | NotificationCompat.FLAG_ONGOING_EVENT;

        PeerService.forceStop(mApplication.getApplicationContext());
        return notification;
    }

    private android.app.Notification createPlaceholderCallNotification(boolean video) {
        if (DEBUG) {
            Log.d(LOG_TAG, "createPlaceholderCallNotification: video=" + video);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mApplication, mDefaultChannel);
        notificationBuilder.setContentTitle(mApplication.getString(R.string.application_name));
        if (video) {
            notificationBuilder.setContentText(mApplication.getString(R.string.conversation_activity_video_call));
        } else {
            notificationBuilder.setContentText(mApplication.getString(R.string.conversation_activity_audio_call));
        }

        notificationBuilder.setSmallIcon(R.drawable.logo_small);
        notificationBuilder.setLights(Design.BLUE_NORMAL, 1000, 500);
        notificationBuilder.setCategory(NotificationCompat.CATEGORY_CALL);
        notificationBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
        notificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setOngoing(true);

        // Post the notification before calling startForeground().
        android.app.Notification notification = notificationBuilder.build();
        notification.flags |= NotificationCompat.FLAG_INSISTENT | NotificationCompat.FLAG_NO_CLEAR | NotificationCompat.FLAG_ONGOING_EVENT;
        PeerService.forceStop(mApplication.getApplicationContext());
        return notification;
    }

    @Override
    @NonNull
    public android.app.Notification getPlaceholderCallNotification() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getPlaceholderIncomingCallNotification");
        }

        synchronized (this) {
            if (mPlaceholderCallNotification == null) {
                mPlaceholderCallNotification = createPlaceholderCallNotification(false);
            }
        }
        return mPlaceholderCallNotification;
    }

    public android.app.Notification createIncomingCallNotification(@NonNull Originator originator, Bitmap avatar, @NonNull CallStatus callStatus, @Nullable UUID callId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "createIncomingCallNotification mode=" + callStatus);
        }

        NotificationCompat.Builder notificationBuilder = createCallNotification(callStatus, originator, avatar, callId);

        // Update the notification.
        android.app.Notification notification = notificationBuilder.build();
        notification.flags |= NotificationCompat.FLAG_INSISTENT | NotificationCompat.FLAG_NO_CLEAR | NotificationCompat.FLAG_ONGOING_EVENT;
        return notification;
    }

    public void cancel(int notificationId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "cancel: notificationId=" + notificationId);
        }

        cancelOrCleanNotification(notificationId);
    }

    @NonNull
    private PendingIntent createPendingIntent(int requestCode, @NonNull Intent intent, int flags) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        return PendingIntent.getActivity(mApplication, requestCode, intent, flags);
    }

    @NonNull
    private PendingIntent createServiceIntent(@NonNull Intent intent, int flags) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return PendingIntent.getForegroundService(mApplication, 0, intent, flags | PendingIntent.FLAG_IMMUTABLE);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return PendingIntent.getService(mApplication, 0, intent, flags | PendingIntent.FLAG_IMMUTABLE);
        } else {
            return PendingIntent.getService(mApplication, 0, intent, flags);
        }
    }

    @NonNull
    private NotificationCompat.Builder createCallNotification(@NonNull CallStatus mode, @NonNull Originator originator, @Nullable Bitmap avatar, @Nullable UUID callId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "createCallNotification: mode=" + mode + " originator=" + originator + " callId=" + callId);
        }

        String channelId;

        boolean video = mode.isVideo();
        if (CallStatus.isIncoming(mode) && !CallStatus.isAccepted(mode)) {
            channelId = video ? mVideoChannel : mAudioChannel;
        } else {
            channelId = mDefaultChannel;
        }
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mApplication, channelId);

        boolean discreet = originator.getIdentityCapabilities().hasDiscreet() && !channelId.equals(mDefaultChannel);

        String callerName;
        String callerShortcutId;

        if (discreet) {
            callerName = mApplication.getResources().getString(R.string.calls_fragment_incoming_call);
            callerShortcutId = null;
            avatar = mTwinmeApplication.getAnonymousAvatar();
        } else {
            callerName = originator.getType() == Originator.Type.GROUP_MEMBER ? ((GroupMember) originator).getGroup().getName() : originator.getName();
            callerShortcutId = originator.getType() == Originator.Type.GROUP_MEMBER ? ((GroupMember) originator).getGroup().getShortcutId() : originator.getShortcutId();
            if (avatar == null) {
                avatar = originator.getType() == Originator.Type.GROUP_MEMBER ? getAvatar(((GroupMember) originator).getGroup()) : getAvatar(originator);
            }
        }
        String calleeName = originator.getIdentityName();
        notificationBuilder.setContentTitle(callerName);

        if (calleeName == null) {
            if (video) {
                notificationBuilder.setContentText(mApplication.getString(R.string.conversation_activity_video_call));
            } else {
                notificationBuilder.setContentText(mApplication.getString(R.string.conversation_activity_audio_call));
            }
        } else {
            if (video) {
                notificationBuilder.setContentText(String.format(mApplication.getString(R.string.notification_center_video_call_to), calleeName));
            } else {
                notificationBuilder.setContentText(String.format(mApplication.getString(R.string.notification_center_audio_call_to), calleeName));
            }
        }

        Intent incomingCallIntent;
        incomingCallIntent = new Intent(mApplication, CallActivity.class);
        incomingCallIntent.putExtra(Intents.INTENT_CALL_MODE, mode);
        incomingCallIntent.putExtra(Intents.INTENT_CONTACT_ID, originator.getId().toString());

        if (originator.getType() == Originator.Type.GROUP_MEMBER) {
            incomingCallIntent.putExtra(Intents.INTENT_GROUP_ID, ((GroupMember) originator).getGroup().getId().toString());
        }

        if (callId != null) {
            incomingCallIntent.putExtra(CallService.CALL_ID, callId);
        }

        incomingCallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent incomingCallPendingIntent = createPendingIntent(0, incomingCallIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_CANCEL_CURRENT);
        notificationBuilder.setSmallIcon(R.drawable.logo_small);
        notificationBuilder.setLargeIcon(avatar);
        notificationBuilder.setLights(Design.BLUE_NORMAL, 1000, 500);
        notificationBuilder.setContentIntent(incomingCallPendingIntent);
        if (CallStatus.isIncoming(mode) && !CallStatus.isAccepted(mode)) {
            notificationBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
            notificationBuilder.setCategory(NotificationCompat.CATEGORY_CALL);

            // Start the full screen intent only for the incoming call.
            notificationBuilder.setFullScreenIntent(incomingCallPendingIntent, true);
        } else {
            notificationBuilder.setPriority(NotificationCompat.PRIORITY_MIN);
            notificationBuilder.setCategory(NotificationCompat.CATEGORY_SERVICE);
        }
        notificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        notificationBuilder.setOngoing(true);
        notificationBuilder.setAutoCancel(false);

        Person person = new Person.Builder()
                .setKey(callerShortcutId)
                .setIcon(CommonUtils.bitmapToAdaptiveIcon(avatar))
                .setName(TextUtils.isEmpty(callerName) ? mTwinmeApplication.getAnonymousName() : callerName)
                .build();

        notificationBuilder.addPerson(person);

        if (CallStatus.isIncoming(mode) && !CallStatus.isAccepted(mode)) {

            Intent cancelCallIntent = new Intent(mApplication, CallService.class);
            cancelCallIntent.setAction(CallService.ACTION_TERMINATE_CALL);
            cancelCallIntent.putExtra(CallService.PARAM_TERMINATE_REASON, TerminateReason.DECLINE);

            if (callId != null) {
                cancelCallIntent.putExtra(CallService.CALL_ID, callId);
            }

            int flags = PendingIntent.FLAG_ONE_SHOT;
            if (FeatureUtils.isTelecomSupported(mApplication.getApplicationContext())) {
                // When Telecom is used, the Decline button is disabled if FLAG_CANCEL_CURRENT is set, no idea why.
                // Notification behavior shouldn't be affected by this, as the CALL_ID extra is the only thing that changes between two cancelCallIntents.
                flags |= PendingIntent.FLAG_UPDATE_CURRENT;
            } else {
                flags |= PendingIntent.FLAG_CANCEL_CURRENT;
            }

            final PendingIntent cancelCallPendingIntent = createServiceIntent(cancelCallIntent, flags);

            Intent acceptCallIntent = createAcceptCallIntent(mode, originator, callId);
            PendingIntent acceptCallPendingIntent = createPendingIntent(0, acceptCallIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            notificationBuilder
                    .setStyle(NotificationCompat.CallStyle.
                            forIncomingCall(person, cancelCallPendingIntent, acceptCallPendingIntent)
                            .setIsVideo(video));
        }
        return notificationBuilder;
    }

    public Intent createAcceptCallIntent(@NonNull CallStatus mode, @NonNull Originator originator, @Nullable UUID callId) {

        Intent acceptCallIntent;
        acceptCallIntent = new Intent(mApplication, CallActivity.class);
        acceptCallIntent.putExtra(Intents.INTENT_CONTACT_ID, originator.getId().toString());
        acceptCallIntent.putExtra(Intents.INTENT_INCOMING_CALL, true);
        acceptCallIntent.putExtra(Intents.INTENT_CALL_MODE, mode);

        if (originator.getType() == Originator.Type.GROUP_MEMBER) {
            acceptCallIntent.putExtra(Intents.INTENT_GROUP_ID, ((GroupMember) originator).getGroup().getId().toString());
        }

        if (callId != null){
            acceptCallIntent.putExtra(CallService.CALL_ID, callId);
        }

        // Do not use extra - extra data are not used to compare
        // PendingIntent
        acceptCallIntent.setAction(Intents.INTENT_ACCEPTED);

        return acceptCallIntent;
    }

    public android.app.Notification createCallNotification(@NonNull CallStatus mode, @NonNull Originator originator, @Nullable UUID callId, boolean mute) {
        if (DEBUG) {
            Log.d(LOG_TAG, "createCallNotification mode=" + mode + " originator=" + originator
                    + " mute=" + mute);
        }

        NotificationCompat.Builder notificationBuilder = createCallNotification(mode, originator, null, callId);

        if (CallStatus.isActive(mode) || CallStatus.isAccepted(mode)) {
            Intent muteCall = new Intent(mApplication, CallService.class);
            muteCall.setAction(CallService.ACTION_AUDIO_MUTE);
            muteCall.putExtra(CallService.PARAM_AUDIO_MUTE, mute);

            final PendingIntent muteCallPendingIntent = createServiceIntent(muteCall, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Action muteAction;


            // Use material icons when applying notification styles, as they fit better with the default "hang up" icon.
            // NB: icons are not displayed on Android 7+ (API 24) when no notification style is applied.
            int micOffIcon = R.drawable.mic_off;
            int micOnIcon = R.drawable.mic;

            if (mute) {
                muteAction = new NotificationCompat.Action(micOffIcon, mApplication.getString(R.string.notification_center_mute), muteCallPendingIntent);
            } else {
                muteAction = new NotificationCompat.Action(micOnIcon, mApplication.getString(R.string.notification_center_unmute), muteCallPendingIntent);
            }

            Intent terminateCall = new Intent(mApplication, CallService.class);
            terminateCall.setAction(CallService.ACTION_TERMINATE_CALL);
            terminateCall.putExtra(CallService.PARAM_TERMINATE_REASON, TerminateReason.SUCCESS);

            if(callId != null){
                terminateCall.putExtra(CallService.CALL_ID, callId);
            }

            final PendingIntent terminateCallPendingIntent = createServiceIntent(terminateCall, PendingIntent.FLAG_UPDATE_CURRENT);

            if (originator.getType() == Originator.Type.GROUP_MEMBER) {
                originator = ((GroupMember) originator).getGroup();
            }

            Bitmap callerAvatar = getAvatar(originator);

            notificationBuilder.setShortcutInfo(CommonUtils.buildShortcutInfo(mApplication, originator, callerAvatar, ConversationActivity.class, true));

            Person person = new Person.Builder()
                    .setKey(originator.getShortcutId())
                    .setIcon(CommonUtils.bitmapToAdaptiveIcon(callerAvatar))
                    .setName(TextUtils.isEmpty(originator.getName()) ? mTwinmeApplication.getAnonymousName() :originator.getName())
                    .build();

            notificationBuilder.setStyle(
                    NotificationCompat.CallStyle.
                            forOngoingCall(person, terminateCallPendingIntent)
                            .setIsVideo(mode.isVideo()));

            notificationBuilder.addAction(muteAction);
        }

        // Update the notification.
        android.app.Notification notification = notificationBuilder.build();
        notification.flags |= NotificationCompat.FLAG_INSISTENT | NotificationCompat.FLAG_NO_CLEAR | NotificationCompat.FLAG_ONGOING_EVENT;
        return notification;
    }

    @Override
    public void startForegroundService(@NonNull Service service, boolean transferring) {
        if (DEBUG) {
            Log.d(LOG_TAG, "startForegroundService transferring: " + transferring);
        }

        // Create notification builder.
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mApplication, mDefaultChannel);

        notificationBuilder.setContentTitle(transferring ? mApplication.getString(R.string.notification_center_transfering_data) : mApplication.getString(R.string.application_checking_connection));
        notificationBuilder.setWhen(System.currentTimeMillis());
        notificationBuilder.setSmallIcon(R.drawable.logo_small);
        notificationBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
        notificationBuilder.setCategory(NotificationCompat.CATEGORY_SERVICE);
        notificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        // SCz: To display a data transfer progress bar, uncomment the following line.
        // builder.setProgress(1, 100, true);

        try {
            // Start foreground service with the notification.  The notification is removed when the service is stopped.
            service.startForeground(FOREGROUND_SERVICE_NOTIFICATION_ID, notificationBuilder.build());
        } catch (RuntimeException ex) {
            if (Logger.ERROR) {
                Log.e(LOG_TAG, "startForeground failed", ex);
            }
        }
    }

    public int startMigrationService(@NonNull Service service, boolean fullScreenActivity) {
        if (DEBUG) {
            Log.d(LOG_TAG, "startMigrationService fullScreenActivity=" + fullScreenActivity);
        }

        String channelId;

        channelId = mDefaultChannel;

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mApplication, channelId);

        Intent incomingCallIntent;

        incomingCallIntent = new Intent(mApplication, AccountMigrationActivity.class);
        incomingCallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent incomingCallPendingIntent = createPendingIntent(0, incomingCallIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        notificationBuilder.setSmallIcon(R.drawable.logo_small);
        notificationBuilder.setLights(Design.BLUE_NORMAL, 1000, 500);
        notificationBuilder.setContentIntent(incomingCallPendingIntent);

        // Start the full screen intent only for the incoming call.
        if (fullScreenActivity) {
            notificationBuilder.setFullScreenIntent(incomingCallPendingIntent, true);
        }

        notificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        notificationBuilder.setOngoing(true);
        notificationBuilder.setAutoCancel(true);

        // Create notification builder.

        notificationBuilder.setContentText(service.getString(R.string.account_activity_migration_title));
        notificationBuilder.setContentTitle(service.getString(R.string.account_activity_migration_title));
        notificationBuilder.setWhen(System.currentTimeMillis());
        notificationBuilder.setSmallIcon(R.drawable.logo_small);
        notificationBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
        notificationBuilder.setCategory(NotificationCompat.CATEGORY_SERVICE);
        notificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        // Start foreground service with the notification.  The notification is removed when the service is stopped.
        android.app.Notification notification = notificationBuilder.build();
        mNotificationManager.notify(ACCOUNT_MIGRATION_NOTIFICATION_ID, notification);
        service.startForeground(ACCOUNT_MIGRATION_NOTIFICATION_ID, notification);

        return ACCOUNT_MIGRATION_NOTIFICATION_ID;
    }

    @Override
    public int startExportService(@NonNull Service service, int progress) {
        if (DEBUG) {
            Log.d(LOG_TAG, "startExportService progress=" + progress);
        }

        String channelId;

        channelId = mDefaultChannel;

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mApplication, channelId);

        Intent incomingCallIntent;

        incomingCallIntent = new Intent(mApplication, ExportActivity.class);

        PendingIntent incomingCallPendingIntent = createPendingIntent(0, incomingCallIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        notificationBuilder.setSmallIcon(R.drawable.logo_small);
        notificationBuilder.setLights(Design.BLUE_NORMAL, 1000, 500);
        notificationBuilder.setContentIntent(incomingCallPendingIntent);

        notificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        notificationBuilder.setOngoing(true);
        notificationBuilder.setAutoCancel(true);

        // Create notification builder.

        notificationBuilder.setContentTitle("Export conversation");
        notificationBuilder.setWhen(System.currentTimeMillis());
        notificationBuilder.setSmallIcon(R.drawable.logo_small);
        notificationBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
        notificationBuilder.setCategory(NotificationCompat.CATEGORY_SERVICE);
        notificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        notificationBuilder.setProgress(100, progress, false);

        // Start foreground service with the notification.  The notification is removed when the service is stopped.
        android.app.Notification notification = notificationBuilder.build();
        mNotificationManager.notify(EXPORT_NOTIFICATION_ID, notification);
        service.startForeground(EXPORT_NOTIFICATION_ID, notification);

        return EXPORT_NOTIFICATION_ID;
    }

    /**
     * Check if the do-not-disturb is activated and we should not ring or vibrate the for the call.
     *
     * @return true if do-not-disturb is active.
     */
    public boolean isDoNotDisturb() {
        if (DEBUG) {
            Log.d(LOG_TAG, "isDoNotDisturb");
        }

        if (Build.VERSION.SDK_INT <= 23) {

            return false;
        }

        int filter = mNotificationManager.getCurrentInterruptionFilter();
        return filter != NotificationManager.INTERRUPTION_FILTER_ALL && filter != NotificationManager.INTERRUPTION_FILTER_UNKNOWN;
    }

    public boolean audioVibrate() {
        if (DEBUG) {
            Log.d(LOG_TAG, "audioVibrate");
        }

        return mTwinmeApplication.getVibration(TwinmeApplication.RingtoneType.AUDIO_RINGTONE);
    }

    public boolean videoVibrate() {
        if (DEBUG) {
            Log.d(LOG_TAG, "videoVibrate");
        }

        return mTwinmeApplication.getVibration(TwinmeApplication.RingtoneType.VIDEO_RINGTONE);
    }

    public Uri getRingtone(boolean video) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getRingtone video=" + video);
        }

        return mTwinmeApplication.getRingtone(video ? TwinmeApplication.RingtoneType.VIDEO_RINGTONE : TwinmeApplication.RingtoneType.AUDIO_RINGTONE);
    }

    //
    // Private Methods
    //

    @NonNull
    private Bitmap getAvatar(@NonNull Originator contact) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getAvatar");
        }

        if (contact.getAvatarId() == null || contact.getIdentityCapabilities().hasDiscreet()) {
            if (contact.isGroup()) {
                return mTwinmeApplication.getDefaultGroupAvatar();
            }
            return mTwinmeApplication.getDefaultAvatar();
        } else {
            return getAvatarWithAvatarId(contact.getAvatarId());
        }
    }

    @NonNull
    private Bitmap getAvatarWithAvatarId(@Nullable ImageId avatarId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getAvatarWithAvatarId");
        }

        Bitmap avatar = null;
        if (avatarId != null) {
            avatar = mTwinmeContext.getImageService().getImage(avatarId, ImageService.Kind.THUMBNAIL);
        }
        return avatar == null ? mTwinmeApplication.getDefaultAvatar() : avatar;
    }

    private int newNotificationId() {
        if (DEBUG) {
            Log.d(LOG_TAG, "newNotificationId");
        }

        int result;
        int needUpdate = 0;
        synchronized (this) {
            result = mNotificationId++;
            if (result >= mLastNotificationId) {
                mLastNotificationId = result + 10;
                needUpdate = mLastNotificationId;
            }
        }

        // Save the last notification Id every 10 allocation.
        if (needUpdate > 0) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putInt(NOTIFICATION_SEQUENCE, needUpdate);
            editor.apply();
        }
        return result;
    }

    public void missedCallNotification(Originator originator, boolean video) {
        if (DEBUG) {
            Log.d(LOG_TAG, "missedCallNotification: originator=" + originator + " video=" + video);
        }

        String callerName = originator.getName();
        Bitmap callerAvatar = getAvatar(originator);

        String calleeName = null;
        if (!originator.getIdentityCapabilities().hasDiscreet()) {
            calleeName = originator.getIdentityName();
        }


        Intent showContactIntent;

        if (originator instanceof Contact) {
            showContactIntent = new Intent(mApplication, ShowContactActivity.class);
            showContactIntent.putExtra(Intents.INTENT_CONTACT_ID, originator.getId().toString());
        } else {
            UUID groupId = originator instanceof GroupMember ? ((GroupMember) originator).getGroup().getId() : originator.getId();

            showContactIntent = new Intent(mApplication, ShowGroupActivity.class);
            showContactIntent.putExtra(Intents.INTENT_GROUP_ID, groupId.toString());
        }
        PendingIntent showContactPendingIntent = createPendingIntent(0, showContactIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mApplication, mMissedCallChannel);
        if (!originator.getIdentityCapabilities().hasDiscreet()) {
            notificationBuilder.setContentTitle(callerName);
        } else {
            notificationBuilder.setContentTitle(mApplication.getString(R.string.application_name));
        }

        notificationBuilder.setContentIntent(showContactPendingIntent);

        if (calleeName == null) {
            notificationBuilder.setContentText(mApplication.getString(R.string.calls_fragment_missed_call));
        } else {
            if (video) {
                notificationBuilder.setContentText(String.format(mApplication.getString(R.string.notification_center_missed_video_call_to), calleeName));
            } else {
                notificationBuilder.setContentText(String.format(mApplication.getString(R.string.notification_center_missed_audio_call_to), calleeName));
            }
        }

        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setSmallIcon(R.drawable.logo_small);
        notificationBuilder.setLargeIcon(callerAvatar);
        notificationBuilder.setLights(Design.BLUE_NORMAL, 1000, 500);
        notificationBuilder.setCategory(NotificationCompat.CATEGORY_CALL);
        notificationBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
        notificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        int notificationId = newNotificationId();
        mNotificationManager.notify(notificationId, notificationBuilder.build());

        NotificationType notificationType = video ? NotificationType.MISSED_VIDEO_CALL : NotificationType.MISSED_AUDIO_CALL;
        mTwinmeContext.createNotification(notificationType, notificationId, originator, null, null);
    }

    private void deleteNotificationChannels() {
        if (DEBUG) {
            Log.d(LOG_TAG, "deleteNotificationChannels");
        }

        if (mNotificationManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            List<NotificationChannel> list = mNotificationManager.getNotificationChannels();

            for (NotificationChannel channel : list) {
                try {
                    mNotificationManager.deleteNotificationChannel(channel.getId());
                } catch (SecurityException exception) {
                    if (Logger.WARN) {
                        Log.w(LOG_TAG, "delete notification channel failed", exception);
                    }
                }
            }

            // Increment the notification channel base id so that we get new notification channel names.
            for (int i = 0; i < mBaseIds.length; i++) {
                mBaseIds[i]++;
            }
        }
    }

    /**
     * Migrate the user settings from the Android channel settings to the twinme application.
     *
     * @param oldAudioChannel the old audio channel name.
     * @param oldVideoChannel the old video channel name.
     */
    private void migrateV1(@NonNull String oldAudioChannel, @NonNull String oldVideoChannel) {
        if (DEBUG) {
            Log.d(LOG_TAG, "migrateV1 oldAudioChannel=" + oldAudioChannel + " oldVideoChannel=" + oldVideoChannel);
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {

            return;
        }

        try {
            NotificationChannel channel = mNotificationManager.getNotificationChannel(oldAudioChannel);
            if (channel != null) {

                boolean vibrate = channel.shouldVibrate();
                Settings.audioVibration.setBoolean(vibrate).save();

                // If the user disabled the sound on this channel, the importance is set to none or low and we must not play any sound.
                Settings.audioRingEnabled.setBoolean(channel.getImportance() > NotificationManager.IMPORTANCE_LOW).save();

                Uri sound = channel.getSound();
                if (sound != null) {

                    Settings.audioCallRingtone.setString(sound.toString());
                }

                mNotificationManager.deleteNotificationChannel(oldAudioChannel);
            }
        } catch (Exception ignore) {

        }
        try {
            NotificationChannel channel = mNotificationManager.getNotificationChannel(oldVideoChannel);
            if (channel != null) {

                boolean vibrate = channel.shouldVibrate();
                Settings.videoVibration.setBoolean(vibrate).save();

                // If the user disabled the sound on this channel, the importance is set to none or low and we must not play any sound.
                Settings.videoRingEnabled.setBoolean(channel.getImportance() > NotificationManager.IMPORTANCE_LOW).save();

                Uri sound = channel.getSound();
                if (sound != null) {

                    Settings.videoCallRingtone.setString(sound.toString());
                }

                mNotificationManager.deleteNotificationChannel(oldVideoChannel);
            }
        } catch (Exception ignore) {
        }
    }

    /**
     * Check if the notification channel must be re-configured because it uses a sound URI that is based
     * on the resource Id instead of the resource name.
     *
     * @param soundPrefix the application resource prefix.
     * @param channelPrefix the channel base name.
     * @param channelIndex the channel index.
     * @return the new notification channel or null.
     */
    @TargetApi(Build.VERSION_CODES.O)
    @Nullable
    private NotificationChannel needFixSoundURI(@NonNull String soundPrefix, @NonNull String channelPrefix, int channelIndex) {
        if (DEBUG) {
            Log.d(LOG_TAG, "needFixSoundURI prefix=" + soundPrefix + " channelPrefix=" + channelPrefix + " channelIndex=" + channelIndex);
        }

        try {
            String channelName = channelPrefix + mBaseIds[channelIndex];
            NotificationChannel channel = mNotificationManager.getNotificationChannel(channelName);
            if (channel == null) {
                return null;
            }

            Uri uri = channel.getSound();
            if (uri == null) {
                return null;
            }

            // Check for an application resource URI.
            String s = uri.toString();
            if (!s.startsWith(soundPrefix)) {
                return null;
            }
            // And a named resource is correct.
            if (s.startsWith(soundPrefix + "/raw/")) {
                return null;
            }

            mBaseIds[channelIndex]++;
            String newChannelName = channelPrefix + mBaseIds[channelIndex];

            NotificationChannel copy = new NotificationChannel(newChannelName, channel.getName(), channel.getImportance());
            copy.setVibrationPattern(new long[]{0L, 500L});
            copy.enableVibration(channel.shouldVibrate());
            copy.enableLights(channel.shouldShowLights());
            copy.setLightColor(channel.getLightColor());

            // This notification is using an URI
            mNotificationManager.deleteNotificationChannel(channelName);
            return copy;

        } catch (Exception ex) {
            return null;
        }
    }

    private void createNotificationChannels() {
        if (DEBUG) {
            Log.d(LOG_TAG, "createNotificationChannels");
        }

        // Setup default notification channel names.
        mAudioChannel = CHANNEL_AUDIO;
        mVideoChannel = CHANNEL_VIDEO;
        mMessageChannel = CHANNEL_NOTIF;
        mGroupChannel = CHANNEL_GROUP;
        mContactChannel = CHANNEL_CONTACT;
        mMissedCallChannel = CHANNEL_MISSED_CALL;
        mDefaultChannel = CHANNEL_SYSTEM;

        if (mNotificationManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            String audioChannelV1 = CHANNEL_AUDIO_V1;
            String videoChannelV1 = CHANNEL_VIDEO_V1;

            // Setup new notification channel names when previous ones have been removed.
            String list = mSharedPreferences.getString(CHANNEL_BASE_IDS, "");
            String[] items = list.split(",");
            if (items.length <= 1) {
                int baseId = mSharedPreferences.getInt(CHANNEL_BASE_ID, 0);
                Arrays.fill(mBaseIds, baseId);
            } else {
                for (int i = 0; i < items.length; i++) {
                    try {
                        mBaseIds[i] = Integer.parseInt(items[i]);
                    } catch (NumberFormatException ignore) {
                        mBaseIds[i] = 0;
                    }
                }
            }
            mAudioChannel = CHANNEL_AUDIO + (mBaseIds[0] <= 0 ? "" : mBaseIds[0]);
            mVideoChannel = CHANNEL_VIDEO + (mBaseIds[1] <= 0 ? "" : mBaseIds[1]);
            mMessageChannel = CHANNEL_NOTIF + (mBaseIds[2] <= 0 ? "" : mBaseIds[2]);
            mGroupChannel = CHANNEL_GROUP + (mBaseIds[3] <= 0 ? "" : mBaseIds[3]);
            mContactChannel = CHANNEL_CONTACT + (mBaseIds[4] <= 0 ? "" : mBaseIds[4]);
            mMissedCallChannel = CHANNEL_MISSED_CALL + (mBaseIds[5] <= 0 ? "" : mBaseIds[5]);
            mDefaultChannel = CHANNEL_SYSTEM + (mBaseIds[6] <= 0 ? "" : mBaseIds[6]);
            audioChannelV1 = audioChannelV1 + (mBaseIds[0] <= 0 ? "" : mBaseIds[0]);
            videoChannelV1 = videoChannelV1 + (mBaseIds[0] <= 0 ? "" : mBaseIds[0]);

            // Remove the old channel so that it disappears from the system preference.
            mNotificationManager.deleteNotificationChannel(CHANNEL_OLD_ID);

            migrateV1(audioChannelV1, videoChannelV1);

            String packageName = mApplication.getPackageName();
            String prefix = "android.resource://" + packageName;
            Uri notificationRingtoneUri = Uri.parse(prefix + "/raw/notification_ringtone");

            // Video notification channel: urgent/high importance.
            NotificationChannel channelVideo = new NotificationChannel(mVideoChannel,
                    mApplication.getString(R.string.notification_channel_video_title),
                    NotificationManager.IMPORTANCE_HIGH);
            channelVideo.enableVibration(false);
            channelVideo.enableLights(true);
            channelVideo.setLightColor(Design.BLUE_NORMAL);
            channelVideo.setSound(null, null);
            channelVideo.setShowBadge(false);
            channelVideo.setLockscreenVisibility(android.app.Notification.VISIBILITY_PUBLIC);

            // Audio notification channel: urgent/high importance.
            NotificationChannel channelAudio = new NotificationChannel(mAudioChannel,
                    mApplication.getString(R.string.notification_channel_audio_title),
                    NotificationManager.IMPORTANCE_HIGH);
            channelAudio.enableLights(true);
            channelAudio.enableVibration(false);
            channelAudio.setLightColor(Design.BLUE_NORMAL);
            channelAudio.setSound(null, null);
            channelAudio.setShowBadge(false);
            channelAudio.setLockscreenVisibility(android.app.Notification.VISIBILITY_PUBLIC);

            AudioAttributes audioAttributesRingtone = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
                    .build();

            // Message notification channel: urgent/high importance.
            NotificationChannel channelNotif = needFixSoundURI(prefix, CHANNEL_NOTIF, 2);
            if (channelNotif == null) {
                channelNotif = new NotificationChannel(mMessageChannel,
                        mApplication.getString(R.string.notification_channel_message_title),
                        NotificationManager.IMPORTANCE_HIGH);
                channelNotif.setVibrationPattern(new long[]{0L, 500L});
                channelNotif.enableVibration(true);
                channelNotif.enableLights(true);
                channelNotif.setLightColor(Design.BLUE_NORMAL);
            }
            channelNotif.setSound(notificationRingtoneUri, audioAttributesRingtone);

            // Group message notification channel: urgent/high importance.
            NotificationChannel channelGroup = needFixSoundURI(prefix, CHANNEL_GROUP, 3);
            if (channelGroup == null) {
                channelGroup = new NotificationChannel(mGroupChannel,
                        mApplication.getString(R.string.notification_channel_group_title),
                        NotificationManager.IMPORTANCE_HIGH);
                channelGroup.setVibrationPattern(new long[]{0L, 500L});
                channelGroup.enableVibration(true);
                channelGroup.enableLights(true);
                channelGroup.setLightColor(Design.BLUE_NORMAL);
            }
            channelGroup.setSound(notificationRingtoneUri, audioAttributesRingtone);

            // Contact notification channel: urgent/high importance.
            NotificationChannel channelContact = needFixSoundURI(prefix, CHANNEL_CONTACT, 4);
            if (channelContact == null) {
                channelContact = new NotificationChannel(mContactChannel,
                        mApplication.getString(R.string.notification_channel_contact_title),
                        NotificationManager.IMPORTANCE_HIGH);
                channelContact.setVibrationPattern(new long[]{0L, 500L});
                channelContact.enableVibration(true);
                channelContact.enableLights(true);
                channelContact.setLightColor(Design.BLUE_NORMAL);
            }
            channelContact.setSound(notificationRingtoneUri, audioAttributesRingtone);

            // Missed audio/video call: default importance.
            NotificationChannel channelMissed = needFixSoundURI(prefix, CHANNEL_MISSED_CALL, 5);
            if (channelMissed == null) {
                channelMissed = new NotificationChannel(mMissedCallChannel,
                        mApplication.getString(R.string.notification_channel_missed_title),
                        NotificationManager.IMPORTANCE_DEFAULT);
                channelMissed.setVibrationPattern(new long[]{0L, 500L});
                channelMissed.enableLights(true);
                channelMissed.setLightColor(Design.BLUE_NORMAL);
            }
            channelMissed.setSound(notificationRingtoneUri, audioAttributesRingtone);

            // System notification channel: low importance (will not play any sound).
            NotificationChannel channelSystem = new NotificationChannel(mDefaultChannel,
                    mApplication.getString(R.string.notification_channel_system_title),
                    NotificationManager.IMPORTANCE_LOW);
            channelSystem.setShowBadge(false);

            mNotificationManager.createNotificationChannel(channelVideo);
            mNotificationManager.createNotificationChannel(channelAudio);
            mNotificationManager.createNotificationChannel(channelNotif);
            mNotificationManager.createNotificationChannel(channelMissed);
            mNotificationManager.createNotificationChannel(channelContact);
            mNotificationManager.createNotificationChannel(channelGroup);
            mNotificationManager.createNotificationChannel(channelSystem);

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mBaseIds.length; i++) {
                if (i > 0) {
                    sb.append(",");
                }
                sb.append(mBaseIds[i]);
            }
            String value = sb.toString();
            if (!value.equals(mSharedPreferences.getString(CHANNEL_BASE_IDS, ""))) {
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putString(CHANNEL_BASE_IDS, sb.toString());
                editor.apply();
            }
        }
    }

    private String emojiFromAnnotationValue(UIReaction.ReactionType reactionType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "emojiFromAnnotationValue: " + reactionType);
        }

        String emoji = "";
        switch (reactionType) {
            case LIKE:
                emoji = "\uD83D\uDC4D"; // 👍
                break;

            case UNLIKE:
                emoji = "\uD83D\uDC4E"; // 👎
                break;

            case LOVE:
                emoji = "\u2764\uFE0F"; // ❤️
                break;

            case CRY:
                emoji = "\uD83D\uDE22"; // 😢
                break;

            case SURPRISED:
                emoji = "\uD83D\uDE32"; // 😲
                break;

            case HUNGER:
                emoji = "\uD83D\uDE02"; // 😂
                break;

            case SCREAMING:
                emoji = "\uD83D\uDE31"; // 😱
                break;

            case FIRE:
                emoji = "\uD83D\uDD25"; // 🔥
                break;

            default:
                break;
        }

        return emoji;
    }
}
