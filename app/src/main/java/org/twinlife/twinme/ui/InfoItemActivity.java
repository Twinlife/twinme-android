/*
 *  Copyright (c) 2019-2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 */

package org.twinlife.twinme.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.BaseService;
import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinlife.ConversationService.Descriptor;
import org.twinlife.twinlife.ConversationService.DescriptorId;
import org.twinlife.twinlife.ConversationService.DescriptorAnnotation;
import org.twinlife.twinlife.TwincodeOutbound;
import org.twinlife.twinlife.util.Utils;
import org.twinlife.twinme.TwinmeContext;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Group;
import org.twinlife.twinme.models.GroupMember;
import org.twinlife.twinme.models.Originator;
import org.twinlife.twinme.services.InfoItemService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.skin.TextStyle;
import org.twinlife.twinme.ui.baseItemActivity.AudioItem;
import org.twinlife.twinme.ui.baseItemActivity.BaseItemActivity;
import org.twinlife.twinme.ui.baseItemActivity.CallItem;
import org.twinlife.twinme.ui.baseItemActivity.ClearItem;
import org.twinlife.twinme.ui.baseItemActivity.FileItem;
import org.twinlife.twinme.ui.baseItemActivity.ImageItem;
import org.twinlife.twinme.ui.baseItemActivity.InfoCopyItem;
import org.twinlife.twinme.ui.baseItemActivity.InfoDateItem;
import org.twinlife.twinme.ui.baseItemActivity.InfoFileItem;
import org.twinlife.twinme.ui.baseItemActivity.InfoItemListAdapter;
import org.twinlife.twinme.ui.baseItemActivity.InvitationContactItem;
import org.twinlife.twinme.ui.baseItemActivity.InvitationItem;
import org.twinlife.twinme.ui.baseItemActivity.Item;
import org.twinlife.twinme.ui.baseItemActivity.MessageItem;
import org.twinlife.twinme.ui.baseItemActivity.PeerAudioItem;
import org.twinlife.twinme.ui.baseItemActivity.PeerCallItem;
import org.twinlife.twinme.ui.baseItemActivity.PeerClearItem;
import org.twinlife.twinme.ui.baseItemActivity.PeerFileItem;
import org.twinlife.twinme.ui.baseItemActivity.PeerImageItem;
import org.twinlife.twinme.ui.baseItemActivity.PeerInvitationContactItem;
import org.twinlife.twinme.ui.baseItemActivity.PeerInvitationItem;
import org.twinlife.twinme.ui.baseItemActivity.PeerMessageItem;
import org.twinlife.twinme.ui.baseItemActivity.PeerVideoItem;
import org.twinlife.twinme.ui.baseItemActivity.TimeItem;
import org.twinlife.twinme.ui.baseItemActivity.VideoItem;
import org.twinlife.twinme.ui.conversationActivity.UIAnnotation;
import org.twinlife.twinme.ui.conversationActivity.UIReaction;
import org.twinlife.twinme.utils.async.Loader;
import org.twinlife.twinme.utils.async.LoaderListener;
import org.twinlife.twinme.utils.async.Manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class InfoItemActivity extends BaseItemActivity implements InfoItemService.Observer, BaseItemActivity.InvitationItemObserver, LoaderListener<Item> {
    private static final String LOG_TAG = "InfoItemActivity";
    private static final boolean DEBUG = false;

    private UUID mGroupId;
    private Bitmap mAvatar;
    private Item mItem;
    private InfoItemListAdapter mInfoItemListAdapter;
    private InfoItemService mInfoItemService;
    private Contact mContact;
    private Group mGroup;
    private Bitmap mContactAvatar;
    private String mResetConversationName;
    @Nullable
    private Manager<Item> mAsyncItemLoader;
    private boolean mIsPeerItem;
    private boolean mCanUpdateCopy = true;

    private Bitmap mIdentityAvatar;
    private final Map<UUID, Originator> mGroupMembers = new HashMap<>();

    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        UUID contactId = Utils.UUIDFromString(intent.getStringExtra(Intents.INTENT_CONTACT_ID));
        mGroupId = Utils.UUIDFromString(intent.getStringExtra(Intents.INTENT_GROUP_ID));
        DescriptorId descriptorId = DescriptorId.fromString(intent.getStringExtra(Intents.INTENT_DESCRIPTOR_ID));
        mResetConversationName = intent.getStringExtra(Intents.INTENT_RESET_CONVERSATION_NAME);
        mIsPeerItem = intent.getBooleanExtra(Intents.INTENT_IS_PEER_ITEM, false);
        if ((contactId == null && mGroupId == null) || descriptorId == null) {
            finish();
            return;
        }

        initViews();

        mInfoItemService = new InfoItemService(this, getTwinmeContext(), this, contactId, mGroupId, descriptorId);
    }

    public void updateDescriptor(boolean allowCopy) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateDescriptor updateDescriptor=" + allowCopy);
        }

        mItem.setCopyAllowed(allowCopy);
        mInfoItemListAdapter.notifyDataSetChanged();
        mInfoItemService.updateDescriptor(mItem.getDescriptorId(), allowCopy);
    }

    //
    // Implement InfoService.Observer methods
    //

    @Override
    public void onGetDescriptor(@Nullable Descriptor descriptor, @Nullable Map<TwincodeOutbound, DescriptorAnnotation> annotations) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetDescriptor descriptor=" + descriptor);
        }

        if (descriptor != null) {
            switch (descriptor.getType()) {
                case OBJECT_DESCRIPTOR:
                    if (mIsPeerItem) {
                        mItem = new PeerMessageItem((ConversationService.ObjectDescriptor) descriptor, descriptor.getReplyToDescriptor());
                    } else {
                        mItem = new MessageItem((ConversationService.ObjectDescriptor) descriptor, descriptor.getReplyToDescriptor());
                    }
                    break;

                case NAMED_FILE_DESCRIPTOR:
                    if (mIsPeerItem) {
                        mItem = new PeerFileItem((ConversationService.NamedFileDescriptor) descriptor, descriptor.getReplyToDescriptor());
                    } else {
                        mItem = new FileItem((ConversationService.NamedFileDescriptor) descriptor, descriptor.getReplyToDescriptor());
                    }
                    break;

                case IMAGE_DESCRIPTOR:
                    if (mIsPeerItem) {
                        mItem = new PeerImageItem((ConversationService.ImageDescriptor) descriptor, descriptor.getReplyToDescriptor());
                    } else {
                        mItem = new ImageItem((ConversationService.ImageDescriptor) descriptor, descriptor.getReplyToDescriptor());
                    }
                    break;

                case AUDIO_DESCRIPTOR:
                    if (mIsPeerItem) {
                        mItem = new PeerAudioItem((ConversationService.AudioDescriptor) descriptor, descriptor.getReplyToDescriptor());
                    } else {
                        mItem = new AudioItem((ConversationService.AudioDescriptor) descriptor, descriptor.getReplyToDescriptor());
                    }
                    break;

                case VIDEO_DESCRIPTOR:
                    if (mIsPeerItem) {
                        mItem = new PeerVideoItem((ConversationService.VideoDescriptor) descriptor, descriptor.getReplyToDescriptor());
                    } else {
                        mItem = new VideoItem((ConversationService.VideoDescriptor) descriptor, descriptor.getReplyToDescriptor());
                    }
                    break;

                case INVITATION_DESCRIPTOR:
                    if (mIsPeerItem) {
                        mItem = new PeerInvitationItem(this, this, (ConversationService.InvitationDescriptor) descriptor);
                    } else {
                        mItem = new InvitationItem(this, this, (ConversationService.InvitationDescriptor) descriptor);
                    }
                    break;

                case CALL_DESCRIPTOR:
                    if (mIsPeerItem) {
                        mItem = new PeerCallItem((ConversationService.CallDescriptor) descriptor);
                    } else {
                        mItem = new CallItem((ConversationService.CallDescriptor) descriptor);
                    }
                    break;

                case TWINCODE_DESCRIPTOR:
                    if (mIsPeerItem) {
                        mItem = new PeerInvitationContactItem(this, this, (ConversationService.TwincodeDescriptor) descriptor);
                    } else {
                        mItem = new InvitationContactItem(this, this, (ConversationService.TwincodeDescriptor) descriptor);
                    }
                    break;

                case CLEAR_DESCRIPTOR:
                    if (mIsPeerItem) {
                        mItem = new PeerClearItem((ConversationService.ClearDescriptor) descriptor);
                        ((PeerClearItem)mItem).setName(mResetConversationName);
                    } else {
                        mItem = new ClearItem((ConversationService.ClearDescriptor) descriptor);
                    }
                    break;

            }
            updateViews();
            updateAnnotations();
        } else {
            finish();
        }
    }

    @Override
    public void onGetContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContact contact=" + contact);
        }

        mContact = contact;
        mContactAvatar = avatar;

        if (contact.getPeerTwincodeOutbound() != null && !contact.getPeerTwincodeOutbound().isSigned()) {
            mCanUpdateCopy = false;
        }

        mInfoItemService.getIdentityImage(contact, (Bitmap identityAvatar) -> {
            mIdentityAvatar = identityAvatar;

            if (mIdentityAvatar == null) {
                mIdentityAvatar = getTwinmeApplication().getAnonymousAvatar();
            }

            if (contact.hasPeer()) {
                mAvatar = avatar == null ? getDefaultAvatar() : avatar;
            } else {
                mAvatar = getAnonymousAvatar();
            }

            updateViews();
            updateAnnotations();
        });
    }

    @Override
    public void onUpdateContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateContact contact=" + contact);
        }

        if (contact.hasPeer()) {
            mAvatar = avatar == null ? getDefaultAvatar() : avatar;
        } else {
            mAvatar = getAnonymousAvatar();
        }

        updateViews();
    }

    @Override
    public void onGetContactNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContactNotFound");
        }

        mAvatar = getAnonymousAvatar();

        updateViews();
    }

    @Override
    public void onGetGroup(@NonNull Group group, @Nullable Bitmap avatar) {

        mGroup = group;

        if (avatar != null) {
            mAvatar = avatar;
        } else {
            mAvatar = getTwinmeApplication().getDefaultGroupAvatar();
        }

        updateViews();
    }

    @Override
    public void onGetGroupNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetGroupNotFound");
        }

        mAvatar = getTwinmeApplication().getDefaultGroupAvatar();

        updateViews();
    }

    //
    // Implement LoaderListener methods
    //

    @Override
    public void onLoaded(@NonNull List<Item> list) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onLoaded");
        }

        mInfoItemListAdapter.notifyDataSetChanged();
    }

    //
    // Override Activity methods
    //

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        if (mAsyncItemLoader != null) {
            mAsyncItemLoader.stop();
        }
        if (mInfoItemService != null) {
            mInfoItemService.dispose();
        }

        super.onDestroy();
    }

    //
    // Override BaseItemActivity methods
    //


    @Override
    public void onUpdateDescriptor(@NonNull ConversationService.Descriptor descriptor, ConversationService.UpdateType updateType) {


        mInfoItemListAdapter.notifyDataSetChanged();
    }

    @Override
    public void getContactAvatar(@Nullable UUID peerTwincodeOutboundId, TwinmeContext.Consumer<Bitmap> avatarConsumer) {

        if (peerTwincodeOutboundId == null) {
            avatarConsumer.accept(mContactAvatar);
            return;
        }

        Originator member = mGroupMembers.get(peerTwincodeOutboundId);
        if (member != null) {
            mInfoItemService.getImage(member, avatarConsumer);
        } else {
            avatarConsumer.accept(mContactAvatar);
        }
    }

    @Override
    public void onGetGroup(@NonNull Group group, @NonNull List<GroupMember> groupMembers,
                           @NonNull ConversationService.GroupConversation conversation, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetGroup: group=" + group);
        }

        mContactAvatar = avatar;
        mInfoItemService.getIdentityImage(group, (Bitmap identityAvatar) -> {
            mIdentityAvatar = identityAvatar;

            if (mIdentityAvatar == null) {
                mIdentityAvatar = getTwinmeApplication().getAnonymousAvatar();
            }
            mGroup = group;
            for (GroupMember member : groupMembers) {
                mGroupMembers.put(member.getPeerTwincodeOutboundId(), member);
            }

            if (avatar != null) {
                mAvatar = avatar;
            } else {
                mAvatar = getTwinmeApplication().getDefaultGroupAvatar();
            }

            updateViews();
        });
    }

    @Override
    public void onGetGroupMembers(@NonNull List<GroupMember> groupMembers) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetGroupMembers groupMembers=" + groupMembers);
        }

        for (GroupMember member : groupMembers) {
            mGroupMembers.put(member.getPeerTwincodeOutboundId(), member);
        }

        updateAnnotations();
    }


    @Nullable
    @Override
    public Contact getContact() {

        return mContact;
    }

    @Nullable
    @Override
    public Group getGroup() {

        return mGroup;
    }

    @Override
    public boolean isGroupConversation() {

        return mGroupId != null;
    }

    @Override
    public void markDescriptorRead(@NonNull DescriptorId descriptorId) {

    }

    @Override
    public boolean isPeerTyping() {

        return false;
    }

    @Override
    public boolean isSelectItemMode() {

        return false;
    }

    @Override
    @Nullable
    public List<Originator> getTypingOriginators() {

        return null;
    }

    @Override
    @Nullable
    public List<Bitmap> getTypingOriginatorsImages() {

        return null;
    }

    @Override
    public void deleteItem(@NonNull DescriptorId descriptorId) {

    }

    @Override
    public void addLoadableItem(@NonNull final Loader<Item> item) {
        if (DEBUG) {
            Log.d(LOG_TAG, "addLoadableItem: item=" + item);
        }

        if (mAsyncItemLoader == null) {
            mAsyncItemLoader = new Manager<>(this, getTwinmeContext(), this);
        }
        mAsyncItemLoader.addItem(item);
    }

    public boolean isSelectedItem(@NonNull DescriptorId descriptorId) {

        return false;
    }

    @Override
    public void onItemLongPress(@Nullable Item item) {

    }

    @Override
    public void onReplyClick(@NonNull DescriptorId descriptorId) {

    }

    @Override
    public void onMediaClick(@NonNull DescriptorId descriptorId) {

    }

    @Override
    public void onItemClick(Item item) {

    }

    @Override
    public void onAnnotationClick(@Nullable DescriptorId descriptorId) {

    }

    @Override
    public boolean isMenuOpen() {

        return false;
    }

    @Override
    public void closeMenu() {

    }

    @Override
    public boolean isReplyViewOpen() {

        return false;
    }

    @Override
    public void audioCall() {

    }

    @Override
    public void videoCall() {

    }

    @Override
    public Bitmap getThumbnail(@NonNull org.twinlife.twinlife.ConversationService.FileDescriptor descriptor) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getThumbnail");
        }

        return getTwinmeContext().getConversationService().getDescriptorThumbnail(descriptor);
    }

    @Override
    @NonNull
    public TextStyle getMessageFont() {

        return Design.FONT_REGULAR32;
    }

    //
    // Private methods
    //

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.info_item_activity);

        setStatusBarColor();
        setToolBar(R.id.info_item_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);
        setTitle(getString(R.string.conversation_activity_menu_item_view_info_title));
        applyInsets(R.id.info_item_activity_layout, R.id.info_item_activity_tool_bar, R.id.info_item_activity_item_info_list_view, Design.TOOLBAR_COLOR, false);

    }

    private void updateViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateViews");
        }

        if (mItem != null) {
            List<Item> items = new ArrayList<>();
            items.add(new TimeItem(mItem.getTimestamp()));
            items.add(mItem);

            if (mItem.getType() != Item.ItemType.CALL && mItem.getType() != Item.ItemType.PEER_CALL) {
                items.add(new InfoDateItem(InfoDateItem.InfoDateItemType.SENT, mItem, mAvatar));
                items.add(new InfoDateItem(InfoDateItem.InfoDateItemType.RECEIVED, mItem, mAvatar));
                items.add(new InfoDateItem(InfoDateItem.InfoDateItemType.SEEN, mItem, mAvatar));

                if (mItem.getPeerDeletedTimestamp() > 0) {
                    items.add(new InfoDateItem(InfoDateItem.InfoDateItemType.DELETED, mItem, mAvatar));
                }

                if (mItem.isEdited()) {
                    items.add(new InfoDateItem(InfoDateItem.InfoDateItemType.UPDATED, mItem, mAvatar));
                }

                if (mItem.isEphemeralItem() && mItem.getReadTimestamp() > 0) {
                    items.add(new InfoDateItem(InfoDateItem.InfoDateItemType.EPHEMERAL, mItem, mAvatar));
                }
            }

            switch (mItem.getType()) {
                case MESSAGE:
                case PEER_MESSAGE:
                    items.add(new InfoCopyItem(mItem));
                    break;
                case IMAGE:
                case PEER_IMAGE:
                case VIDEO:
                case PEER_VIDEO:
                case AUDIO:
                case PEER_AUDIO:
                case FILE:
                case PEER_FILE:
                    items.add(new InfoCopyItem(mItem));
                    items.add(new InfoFileItem(mItem));
                    break;
                case CALL:
                case PEER_CALL:
                    items.add(new InfoFileItem(mItem));
                    break;
                default:
                    break;
            }

            mInfoItemListAdapter = new InfoItemListAdapter(this, items, mItem, mCanUpdateCopy);
            LinearLayoutManager uiLinearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
            RecyclerView infoItemListView = findViewById(R.id.info_item_activity_item_info_list_view);
            infoItemListView.setLayoutManager(uiLinearLayoutManager);
            infoItemListView.setAdapter(mInfoItemListAdapter);
            infoItemListView.scrollToPosition(items.size() - 1);
            infoItemListView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        }
    }

    private void updateAnnotations() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateAnnotations");
        }

        if (mItem == null || mItem.getDescriptorId() == null) {
            return;
        }

        mInfoItemService.listAnnotations(mItem.getDescriptorId(), (BaseService.ErrorCode errorCode, Map<TwincodeOutbound, org.twinlife.twinlife.ConversationService.DescriptorAnnotation> annotations) -> {
            // This lambda is run by mTwinlifeExecutor, so we can call the blocking getImage() variant.

            if (annotations == null) {
                return;
            }

            List<UIAnnotation> uiAnnotations = new ArrayList<>();

            for (Map.Entry<TwincodeOutbound, DescriptorAnnotation> annotation : annotations.entrySet()) {
                org.twinlife.twinlife.ConversationService.DescriptorAnnotation descriptorAnnotation = annotation.getValue();

                if (descriptorAnnotation.getType() == ConversationService.AnnotationType.LIKE) {
                    String name = annotation.getKey().getName();
                    Bitmap avatar = mInfoItemService.getTwincodeImage(annotation.getKey());
                    UIReaction uiReaction = new UIReaction(descriptorAnnotation.getValue());

                    if (name != null && avatar != null) {
                        UIAnnotation uiAnnotation = new UIAnnotation(uiReaction, name, avatar);
                        uiAnnotations.add(uiAnnotation);
                    }
                }
            }

            runOnUiThread(() -> mInfoItemListAdapter.setAnnotations(uiAnnotations));
        });
    }
}
