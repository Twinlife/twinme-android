/*
 *  Copyright (c) 2017-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.mainActivity;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.twinlife.twinlife.Notification;
import org.twinlife.twinlife.NotificationService;
import org.twinlife.twinlife.NotificationService.NotificationType;
import org.twinlife.twinlife.RepositoryObject;
import org.twinlife.twinme.TwinmeContext;
import org.twinlife.twinme.models.GroupMember;
import org.twinlife.twinme.models.Originator;
import org.twinlife.twinme.services.AbstractTwinmeService;

import java.util.ArrayList;
import java.util.List;

public class UINotification implements Comparable<UINotification> {

    private static int sUINotificationId = 0;

    private final long mUINotificationId;

    @NonNull
    private final List<Notification> mNotifications;

    private Bitmap mAvatar;
    @Nullable
    private final GroupMember mGroupMember;

    private boolean mIsCertified = false;

    public UINotification(Notification notification, @Nullable GroupMember groupMember, @Nullable Bitmap avatar) {

        mUINotificationId = sUINotificationId++;

        mNotifications = new ArrayList<>();
        mNotifications.add(notification);
        mAvatar = avatar;
        mGroupMember = groupMember;
    }

    public long getItemId() {

        return mUINotificationId;
    }

    NotificationType getNotificationType() {

        return getLastNotification().getNotificationType();
    }

    public String getName(boolean isRtl) {

        if (mGroupMember != null) {

            String name = mGroupMember.getName();

            if (getLastNotification().getNotificationType() == NotificationService.NotificationType.UPDATED_ANNOTATION && getLastNotification().getUser() != null) {
                name = getLastNotification().getUser().getName();
            }

            if (isRtl) {
                return name + " - " + mGroupMember.getGroup().getName();
            } else {
                return mGroupMember.getGroup().getName() + " - " + name;
            }
        } else {
            return getLastNotification().getSubject().getName();
        }
    }

    public void getAvatar(AbstractTwinmeService twinmeService, TwinmeContext.Consumer<Bitmap> uiConsumer) {

        if (mAvatar != null) {
            uiConsumer.accept(mAvatar);
            return;
        }

        RepositoryObject subject = getLastNotification().getSubject();
        if (mGroupMember != null && getLastNotification().getNotificationType() != org.twinlife.twinlife.NotificationService.NotificationType.NEW_CONTACT_INVITATION) {

            if (getLastNotification().getNotificationType() == NotificationService.NotificationType.UPDATED_ANNOTATION && getLastNotification().getUser() != null) {
                twinmeService.getImage(getLastNotification().getUser().getAvatarId(), (Bitmap avatar) -> {
                    mAvatar = avatar;
                    uiConsumer.accept(mAvatar);
                });
            } else {
                twinmeService.getGroupMemberImage(mGroupMember, (Bitmap avatar) -> {
                    mAvatar = avatar;
                    uiConsumer.accept(mAvatar);
                });
            }
        } else if (subject instanceof Originator) {
            twinmeService.getImage((Originator) subject, (Bitmap avatar) -> {
                mAvatar = avatar;
                uiConsumer.accept(mAvatar);
            });
        }
    }

    public long getTimestamp() {

        return getLastNotification().getTimestamp();
    }

    public boolean isAcknowledged() {

        return getLastNotification().isAcknowledged();
    }

    public boolean isCertified() {

        return mIsCertified;
    }

    public void setIsCertified(boolean isCertified) {

        mIsCertified = isCertified;
    }

    public void addNotification(Notification notification) {

        mNotifications.add(0, notification);
    }

    public Notification getLastNotification() {

        return mNotifications.get(0);
    }

    public List<Notification> getNotifications() {

        return mNotifications;
    }

    public int getCount() {

        return mNotifications.size();
    }

    public boolean sameNotification(@NonNull UINotification uiNotification) {

        Notification first = getLastNotification();
        Notification second = uiNotification.getLastNotification();
        return first.getSubject() == second.getSubject()
                && mGroupMember == uiNotification.mGroupMember
                && !isNotificationNotBeGrouped(first.getNotificationType())
                && first.getNotificationType() == second.getNotificationType();
    }

    private boolean isNotificationNotBeGrouped(NotificationType notificationType) {

        return notificationType == NotificationType.NEW_GROUP_INVITATION || notificationType == NotificationType.NEW_CONTACT_INVITATION || notificationType == NotificationType.NEW_GROUP_JOINED || notificationType == NotificationType.DELETED_GROUP;
    }

    //
    // Override Object methods
    //

    @Override
    @NonNull
    public String toString() {

        return "Item:\n" +
                " itemId: " + mUINotificationId + "\n" +
                " notification: " + mNotifications + "\n";
    }

    @Override
    public int compareTo(@NonNull UINotification second) {

        return getLastNotification().getTimestamp() < second.getTimestamp() ? 1 : -1;
    }
}
