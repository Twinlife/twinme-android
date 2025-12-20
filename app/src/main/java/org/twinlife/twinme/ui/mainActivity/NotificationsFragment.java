/*
 *  Copyright (c) 2020-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui.mainActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.MenuProvider;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ConversationService.DescriptorId;
import org.twinlife.twinlife.Notification;
import org.twinlife.twinlife.RepositoryObject;
import org.twinlife.twinme.TwinmeContext;
import org.twinlife.twinme.models.CertificationLevel;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Group;
import org.twinlife.twinme.models.GroupMember;
import org.twinlife.twinme.models.Originator;
import org.twinlife.twinme.models.Space;
import org.twinlife.twinme.services.NotificationService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AcceptInvitationActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.ShowContactActivity;
import org.twinlife.twinme.ui.contacts.DeleteConfirmView;
import org.twinlife.twinme.ui.conversationActivity.ConversationActivity;
import org.twinlife.twinme.ui.externalCallActivity.ShowExternalCallActivity;
import org.twinlife.twinme.ui.groups.AcceptGroupInvitationActivity;
import org.twinlife.twinme.ui.groups.ShowGroupActivity;
import org.twinlife.twinme.ui.rooms.ShowRoomActivity;
import org.twinlife.twinme.utils.AbstractBottomSheetView;
import org.twinlife.twinme.utils.SwipeItemTouchHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class NotificationsFragment extends TabbarFragment implements NotificationService.Observer {
    private static final String LOG_TAG = "NotificationsFragment";
    private static final boolean DEBUG = false;

    private static final int DESIGN_NO_NOTIFICATION_WIDTH = 620;
    private static final int DESIGN_NO_NOTIFICATION_HEIGHT = 252;

    private interface OnItemClickListener {
        void onItemClick(int position);
    }

    private class ItemListAdapter extends RecyclerView.Adapter<UINotificationViewHolder> {

        private final OnItemClickListener mOnItemClickListener;

        ItemListAdapter(OnItemClickListener onItemClickListener) {
            mOnItemClickListener = onItemClickListener;
            setHasStableIds(true);
        }

        @Override
        public int getItemCount() {
            if (DEBUG) {
                Log.d(LOG_TAG, "ItemListAdapter.getItemCount");
            }

            return mUINotifications.size();
        }

        @Override
        public long getItemId(int position) {
            if (DEBUG) {
                Log.d(LOG_TAG, "ItemListAdapter.getItemId: position=" + position);
            }

            return mUINotifications.get(position).getItemId();
        }

        @Override
        public void onBindViewHolder(@NonNull UINotificationViewHolder viewHolder, int position) {
            if (DEBUG) {
                Log.d(LOG_TAG, "ItemListAdapter.onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
            }

            UINotification uiNotification = mUINotifications.get(position);
            boolean hideSeparator = position + 1 == mUINotifications.size();
            viewHolder.onBind(mTwinmeActivity, uiNotification, hideSeparator);
        }

        @NonNull
        @Override
        public UINotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (DEBUG) {
                Log.d(LOG_TAG, "ItemListAdapter.onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
            }

            LayoutInflater inflater = getLayoutInflater();
            View convertView = inflater.inflate(R.layout.notifications_fragment_notification_item, parent, false);
            int itemHeight = Design.ITEM_VIEW_HEIGHT;
            ViewGroup.LayoutParams layoutParams = convertView.getLayoutParams();
            layoutParams.height = itemHeight;
            convertView.setLayoutParams(layoutParams);

            UINotificationViewHolder uiNotificationViewHolder = new UINotificationViewHolder(mTwinmeActivity, mNotificationService, convertView);
            convertView.setOnClickListener(v -> {
                int position = uiNotificationViewHolder.getBindingAdapterPosition();
                if (position >= 0) {
                    mOnItemClickListener.onItemClick(position);
                }
            });
            return uiNotificationViewHolder;
        }

        @Override
        public void onViewRecycled(@NonNull UINotificationViewHolder viewHolder) {
            if (DEBUG) {
                Log.d(LOG_TAG, "ItemListAdapter.onViewRecycled: viewHolder=" + viewHolder);
            }

            viewHolder.onViewRecycled();
        }

        @Override
        public void onViewAttachedToWindow(@NonNull UINotificationViewHolder viewHolder) {
            if (DEBUG) {
                Log.d(LOG_TAG, "ItemListAdapter.onViewAttachedToWindow: viewHolder=" + viewHolder);
            }

            int position = viewHolder.getBindingAdapterPosition();

            if (position >= 0 && position < mUINotifications.size()) {
                UINotification uiNotification = mUINotifications.get(position);
                Notification notification = uiNotification.getLastNotification();

                switch (notification.getNotificationType()) {
                    case DELETED_CONTACT:
                    case NEW_CONTACT:
                    case UPDATED_CONTACT:
                    case UPDATED_AVATAR_CONTACT:
                    case MISSED_AUDIO_CALL:
                    case MISSED_VIDEO_CALL:
                    case NEW_GROUP_JOINED:
                    case RESET_CONVERSATION:
                        if (!uiNotification.isAcknowledged()) {
                            for (int i = 0; i < uiNotification.getCount(); i++) {
                                mNotificationService.acknowledgeNotification(uiNotification.getNotifications().get(i));
                            }

                        }
                        break;

                    default:
                        break;
                }
            }
        }
    }

    private boolean mResetAllNotification = false;
    private boolean mUIInitialized = false;
    private boolean mNeedRefresh = false;

    private RecyclerView mNotificationsRecyclerView;
    private ImageView mNoNotificationImageView;
    private TextView mNoNotificationTitleView;
    private TextView mNoNotificationTextView;

    private final List<UINotification> mUINotifications = new ArrayList<>();
    private ItemListAdapter mItemListAdapter;
    @Nullable
    private Menu mMenu;

    private NotificationService mNotificationService;

    //
    // Override TwinmeActivityImpl methods
    //

    // Default constructor is required by Android for proper activity restoration.
    public NotificationsFragment() {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateView: inflater=" + inflater + " container=" + container + " savedInstanceState=" + savedInstanceState);
        }

        View view = inflater.inflate(R.layout.notifications_fragment, container, false);
        initViews(view);
        addMenu();

        // Setup the service after the view is initialized.
        mNotificationService = new NotificationService(mTwinmeActivity, mTwinmeActivity.getTwinmeContext(), this);

        return view;
    }

    //
    // Override Fragment methods
    //

    @Override
    public void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        super.onResume();
        if (mNeedRefresh) {
            mNeedRefresh = false;
            mNotificationService.getNotifications();
        }
    }

    @Override
    public void onPause() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPause");
        }

        mNeedRefresh = true;
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        mNotificationService.dispose();

        super.onDestroy();
    }

    //
    // Implement NotificationService.Observer methods
    //

    @Override
    public void showProgressIndicator() {

    }

    @Override
    public void hideProgressIndicator() {

    }

    @Override
    public void onSetCurrentSpace(@NonNull Space space) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSetCurrentSpace: space=" + space);
        }

        mNotificationsRecyclerView.scrollToPosition(0);
    }

    @Override
    public void onGetNotifications(@NonNull List<Notification> notifications,
                                   @NonNull Map<UUID, GroupMember> groupMembers) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetNotifications: notifications=" + notifications);
        }

        mUINotifications.clear();

        if (notifications.isEmpty()) {
            if (mMenu != null) {
                android.view.MenuItem resetMenuItem = mMenu.findItem(R.id.reset_notification_action);
                setEnabled(resetMenuItem, !mUINotifications.isEmpty());
            }

            notifyNotificationListChanged();
            return;
        }

        for (Notification notification : notifications) {
            GroupMember groupMember;
            if (notification.getSubject() instanceof Group) {
                DescriptorId descriptorId = notification.getDescriptorId();
                if (descriptorId != null) {
                    groupMember = groupMembers.get(descriptorId.twincodeOutboundId);
                } else {
                    groupMember = null;
                }
            } else {
                groupMember = null;
            }

            UINotification uiNotification = new UINotification(notification, groupMember, null);
            if (notification.getSubject() instanceof Contact) {
                Contact contact = (Contact) notification.getSubject();
                if (contact.getCertificationLevel() == CertificationLevel.LEVEL_4) {
                    uiNotification.setIsCertified(true);
                }
            }

            // No need to get the avatar here, it will be retrieved in UINotificationViewHolder.onBind()
            mUINotifications.add(uiNotification);
        }

        if (mMenu != null) {
            android.view.MenuItem resetMenuItem = mMenu.findItem(R.id.reset_notification_action);
            setEnabled(resetMenuItem, !mUINotifications.isEmpty());
        }

        notifyNotificationListChanged();
    }

    @Override
    public void onAddNotification(@NonNull Notification notification, @Nullable GroupMember groupMember) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onAddNotification: notification=" + notification);
        }

        UINotification uiNotification = new UINotification(notification, groupMember, null);
        if (notification.getSubject() instanceof Contact) {
            Contact contact = (Contact) notification.getSubject();
            if (contact.getCertificationLevel() == CertificationLevel.LEVEL_4) {
                uiNotification.setIsCertified(true);
            }
        }

        // No need to get the avatar here, it will be retrieved in UINotificationViewHolder.onBind()
        addItem(uiNotification);

        if (mMenu != null) {
            android.view.MenuItem resetMenuItem = mMenu.findItem(R.id.reset_notification_action);
            setEnabled(resetMenuItem, !mUINotifications.isEmpty());
        }

        notifyNotificationListChanged();
    }

    @Override
    public void onAcknowledgeNotification(@NonNull Notification notification) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onAcknowledgeNotification: notification=" + notification);
        }

        for (int i = 0; i < mUINotifications.size(); i++) {
            UINotification uiNotification = mUINotifications.get(i);
            if (uiNotification.getLastNotification().getId().equals(notification.getId())) {
                uiNotification.getNotifications().set(0, notification);
                break;
            }
        }
    }

    @Override
    public void onDeleteNotification(@NonNull UUID notificationId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteNotification: notificationId=" + notificationId);
        }

        for (int i = 0; i < mUINotifications.size(); i++) {
            UINotification uiNotification = mUINotifications.get(i);
            if (uiNotification.getLastNotification().getId().equals(notificationId)) {
                mUINotifications.remove(i);
                break;
            }
        }

        if (mResetAllNotification && mUINotifications.isEmpty()) {
            mResetAllNotification = false;
            hideProgressIndicator();

            if (mMenu != null) {
                android.view.MenuItem resetMenuItem = mMenu.findItem(R.id.reset_notification_action);
                setEnabled(resetMenuItem, false);
            }
        }

        notifyNotificationListChanged();
    }

    @Override
    public void onUpdatePendingNotifications(boolean hasPendingNotification) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdatePendingNotifications: hasPendingNotification=" + hasPendingNotification);
        }
    }

    //
    // Private methods
    //

    private void getAvatar(@NonNull Notification notification, @Nullable GroupMember groupMember, TwinmeContext.Consumer<Bitmap> uiConsumer) {

        Originator originator = (Originator) notification.getSubject();
        if (groupMember != null && notification.getNotificationType() != org.twinlife.twinlife.NotificationService.NotificationType.NEW_CONTACT_INVITATION) {

            mNotificationService.getGroupMemberImage(groupMember, uiConsumer);

        } else {
            mNotificationService.getImage(originator, uiConsumer);
        }
    }

    private void onItemClick(UINotification uiNotification) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onItemClick: uiNotification=" + uiNotification);
        }

        Notification notification = uiNotification.getLastNotification();
        if (!uiNotification.isAcknowledged()) {
            for (int i = 0; i < uiNotification.getCount(); i++) {
                mNotificationService.acknowledgeNotification(uiNotification.getNotifications().get(i));
            }
        }

        Intent intent = null;
        Class<?> clazz = null;
        final RepositoryObject subject = notification.getSubject();
        switch (notification.getNotificationType()) {
            case DELETED_CONTACT:
            case NEW_CONTACT:
            case UPDATED_CONTACT:
            case UPDATED_AVATAR_CONTACT:
            case MISSED_AUDIO_CALL:
            case MISSED_VIDEO_CALL:
                intent = new Intent();

                if (subject instanceof Originator) {
                    Originator originator = (Originator) subject;
                    if (originator.getType() == Originator.Type.CALL_RECEIVER) {
                        clazz = ShowExternalCallActivity.class;
                        intent.putExtra(Intents.INTENT_CALL_RECEIVER_ID, notification.getOriginatorId().toString());
                    } else if (originator.getType() == Originator.Type.CONTACT) {
                        clazz = ((Contact) originator).isTwinroom() ? ShowRoomActivity.class : ShowContactActivity.class;
                        intent.putExtra(Intents.INTENT_CONTACT_ID, notification.getOriginatorId().toString());
                    } else if (originator.getType() == Originator.Type.GROUP) {
                        clazz = ShowGroupActivity.class;
                        intent.putExtra(Intents.INTENT_GROUP_ID, notification.getOriginatorId().toString());
                    }
                }
                break;

            case RESET_CONVERSATION:
            case NEW_TEXT_MESSAGE:
            case NEW_IMAGE_MESSAGE:
            case NEW_AUDIO_MESSAGE:
            case NEW_VIDEO_MESSAGE:
            case NEW_FILE_MESSAGE:
            case UPDATED_ANNOTATION:
                intent = new Intent();
                clazz = ConversationActivity.class;
                if (subject instanceof Group) {
                    intent.putExtra(Intents.INTENT_GROUP_ID, notification.getOriginatorId().toString());
                } else {
                    intent.putExtra(Intents.INTENT_CONTACT_ID, notification.getOriginatorId().toString());
                }
                break;

            case NEW_GROUP_JOINED:
                intent = new Intent();
                clazz = ConversationActivity.class;
                intent.putExtra(Intents.INTENT_GROUP_ID, notification.getOriginatorId().toString());
                break;

            case NEW_GROUP_INVITATION:
                if (notification.getDescriptorId() != null) {
                    intent = new Intent();
                    clazz = AcceptGroupInvitationActivity.class;
                    intent.putExtra(Intents.INTENT_CONTACT_ID, notification.getOriginatorId().toString());
                    intent.putExtra(Intents.INTENT_INVITATION_ID, notification.getDescriptorId().toString());
                }
                break;
            case NEW_CONTACT_INVITATION:
                if (notification.getDescriptorId() != null) {
                    intent = new Intent();
                    clazz = AcceptInvitationActivity.class;
                    intent.putExtra(Intents.INTENT_DESCRIPTOR_ID, notification.getDescriptorId().toString());
                    if (subject instanceof Group) {
                        intent.putExtra(Intents.INTENT_GROUP_ID, notification.getOriginatorId().toString());
                    } else {
                        intent.putExtra(Intents.INTENT_CONTACT_ID, notification.getOriginatorId().toString());
                    }
                    intent.putExtra(Intents.INTENT_NOTIFICATION_ID, notification.getId().toString());
                }
                break;
        }
        if (intent != null && clazz != null) {
            startActivity(intent, clazz);

            if (mTwinmeActivity != null && notification.getNotificationType() == org.twinlife.twinlife.NotificationService.NotificationType.NEW_CONTACT_INVITATION || notification.getNotificationType() == org.twinlife.twinlife.NotificationService.NotificationType.NEW_GROUP_INVITATION) {
                mTwinmeActivity.overridePendingTransition(0, 0);
            }
        }
    }

    public void onDeleteItem(UINotification uiNotification) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteItem: uiNotification=" + uiNotification);
        }

        for (int i = 0; i < uiNotification.getNotifications().size(); i++) {
            mNotificationService.deleteNotification(uiNotification.getNotifications().get(i));
        }
    }

    private void initViews(View view) {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        OnItemClickListener onItemClickListener = position -> {
            UINotification uiNotification = mUINotifications.get(position);
            onItemClick(uiNotification);
        };

        mItemListAdapter = new ItemListAdapter(onItemClickListener);

        mNotificationsRecyclerView = view.findViewById(R.id.notifications_fragment_list_view);
        mNotificationsRecyclerView.setLayoutManager(new LinearLayoutManager(mTwinmeActivity, RecyclerView.VERTICAL, false));
        mNotificationsRecyclerView.setAdapter(mItemListAdapter);
        mNotificationsRecyclerView.setItemViewCacheSize(Design.ITEM_LIST_CACHE_SIZE);
        mNotificationsRecyclerView.setItemAnimator(null);

        SwipeItemTouchHelper.OnSwipeItemClickListener onSwipeItemClickListener = new SwipeItemTouchHelper.OnSwipeItemClickListener() {
            @Override
            public void onLeftActionClick(int adapterPosition) {

            }

            @Override
            public void onRightActionClick(int adapterPosition) {
                onDeleteItem(mUINotifications.get(adapterPosition));
            }

            @Override
            public void onOtherActionClick(int adapterPosition) {

            }
        };

        SwipeItemTouchHelper swipeItemTouchHelper = new SwipeItemTouchHelper(mNotificationsRecyclerView, null, SwipeItemTouchHelper.ButtonType.DELETE, onSwipeItemClickListener);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeItemTouchHelper);
        itemTouchHelper.attachToRecyclerView(mNotificationsRecyclerView);

        mNoNotificationImageView = view.findViewById(R.id.notifications_fragment_no_notification_image_view);

        ViewGroup.LayoutParams layoutParams = mNoNotificationImageView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_NO_NOTIFICATION_WIDTH * Design.WIDTH_RATIO);
        layoutParams.height = (int) (DESIGN_NO_NOTIFICATION_HEIGHT * Design.HEIGHT_RATIO);

        mNoNotificationTitleView = view.findViewById(R.id.notifications_fragment_no_notification_title_view);
        Design.updateTextFont(mNoNotificationTitleView, Design.FONT_MEDIUM34);
        mNoNotificationTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mNoNotificationTextView = view.findViewById(R.id.notifications_fragment_no_notification_text_view);
        Design.updateTextFont(mNoNotificationTextView, Design.FONT_MEDIUM28);
        mNoNotificationTextView.setTextColor(Design.FONT_COLOR_DESCRIPTION);

        mUIInitialized = true;

        if (!mUINotifications.isEmpty()) {
            notifyNotificationListChanged();
        }
    }

    private void onResetClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResetClick");
        }

        // This fragment is detached and has no activity: ignore the action.
        if (!isAdded() || mTwinmeActivity == null) {
            return;
        }

        if (mUINotifications.isEmpty()) {
            return;
        }

        DrawerLayout drawerLayout = mTwinmeActivity.findViewById(R.id.main_activity_drawer_layout);
        mNotificationService.getProfileImage(mTwinmeActivity.getProfile(), (Bitmap avatar) -> {
            DeleteConfirmView deleteConfirmView = new DeleteConfirmView(mTwinmeActivity, null);
            deleteConfirmView.setAvatar(avatar, false);

            String message = getString(R.string.application_operation_irreversible) + "\n\n"  + getString(R.string.notifications_fragment_reset);
            deleteConfirmView.setMessage(message);
            deleteConfirmView.setConfirmTitle(getString(R.string.notifications_fragment_reset_title));

            AbstractBottomSheetView.Observer observer = new AbstractBottomSheetView.Observer() {
                @Override
                public void onConfirmClick() {
                    resetNotifications();
                    deleteConfirmView.animationCloseConfirmView();
                }

                @Override
                public void onCancelClick() {
                    deleteConfirmView.animationCloseConfirmView();
                }

                @Override
                public void onDismissClick() {
                    deleteConfirmView.animationCloseConfirmView();
                }

                @Override
                public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                    drawerLayout.removeView(deleteConfirmView);
                    if (mTwinmeActivity != null) {
                        mTwinmeActivity.setStatusBarColor();
                    }
                }
            };
            deleteConfirmView.setObserver(observer);

            drawerLayout.addView(deleteConfirmView);
            deleteConfirmView.show();

            int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
            mTwinmeActivity.setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
        });
    }

    private void addItem(UINotification uiNotification) {
        if (DEBUG) {
            Log.d(LOG_TAG, "addItem: uiNotification=" + uiNotification);
        }

        for (int i = 0; i < mUINotifications.size(); i++) {
            if (mUINotifications.get(i).getTimestamp() > uiNotification.getTimestamp()) {
                continue;
            }
            mUINotifications.add(i, uiNotification);

            return;
        }
        mUINotifications.add(uiNotification);
    }

    private void resetNotifications() {
        if (DEBUG) {
            Log.d(LOG_TAG, "resetNotifications");
        }

        if (mMenu == null) {

            return;
        }
        showProgressIndicator();

        mResetAllNotification = true;

        MenuItem resetMenuItem = mMenu.findItem(R.id.reset_notification_action);
        setEnabled(resetMenuItem, false);

        for (int i = 0; i < mUINotifications.size(); i++) {
            UINotification uiNotification = mUINotifications.get(i);
            for (int j = 0; j < uiNotification.getNotifications().size(); j++) {
                mNotificationService.deleteNotification(uiNotification.getNotifications().get(j));
            }
        }
    }

    private void notifyNotificationListChanged() {
        if (DEBUG) {
            Log.d(LOG_TAG, "notifyNotificationListChanged");
        }

        if (mUIInitialized) {

            if (mUINotifications.size() > 1) {
                List<UINotification> uiNotifications = new ArrayList<>(mUINotifications);
                mUINotifications.clear();

                UINotification uiNotification = uiNotifications.get(0);

                for (int i = 1; i < uiNotifications.size(); i++) {
                    UINotification uiNotification2 = uiNotifications.get(i);
                    if (uiNotification.sameNotification(uiNotification2)) {
                        for (int index = uiNotification2.getCount() - 1; index >= 0; index--) {
                            uiNotification.addNotification(uiNotification2.getNotifications().get(index));
                        }
                    } else {
                        mUINotifications.add(uiNotification);
                        uiNotification = uiNotification2;
                    }

                    if (i + 1 == uiNotifications.size()) {
                        mUINotifications.add(uiNotification);
                    }
                }
            }

            mItemListAdapter.notifyDataSetChanged();

            if (mUINotifications.isEmpty()) {
                mNoNotificationImageView.setVisibility(View.VISIBLE);
                mNoNotificationTitleView.setVisibility(View.VISIBLE);
                mNoNotificationTextView.setVisibility(View.VISIBLE);
                mNotificationsRecyclerView.setVisibility(View.GONE);
                if (mMenu != null) {
                    android.view.MenuItem resetMenuItem = mMenu.findItem(R.id.reset_notification_action);
                    setEnabled(resetMenuItem, false);
                }
            } else {
                mNoNotificationImageView.setVisibility(View.GONE);
                mNoNotificationTitleView.setVisibility(View.GONE);
                mNoNotificationTextView.setVisibility(View.GONE);
                mNotificationsRecyclerView.setVisibility(View.VISIBLE);
                if (mMenu != null) {
                    android.view.MenuItem resetMenuItem = mMenu.findItem(R.id.reset_notification_action);
                    setEnabled(resetMenuItem, true);
                }
            }
        }
    }

    private void addMenu() {
        if (DEBUG) {
            Log.d(LOG_TAG, "addMenu");
        }

        if (mTwinmeActivity == null) {
            return;
        }

        mTwinmeActivity.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.notifications_menu, menu);

                mMenu = menu;

                MenuItem menuItem = menu.findItem(R.id.reset_notification_action);

                ImageView imageView = (ImageView) menuItem.getActionView();
                if (imageView != null) {
                    imageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.action_bar_delete, null));
                    imageView.setColorFilter(Color.WHITE);
                    imageView.setPadding(Design.TOOLBAR_IMAGE_ITEM_PADDING, 0, Design.TOOLBAR_IMAGE_ITEM_PADDING, 0);
                    imageView.setOnClickListener(view -> onResetClick());
                    imageView.setContentDescription(getString(R.string.notifications_fragment_reset_title));
                }

                setEnabled(menuItem, !mUINotifications.isEmpty());
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                // Handle menu item clicks here
                return false;
            }

        }, getViewLifecycleOwner());
    }
}
