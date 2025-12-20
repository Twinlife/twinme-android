/*
 *  Copyright (c) 2019-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 *   Auguste Hatton (Auguste.Hatton@twin.life)
 *   Romain Kolb (romain.kolb@skyrock.com)
 */

package org.twinlife.twinme.ui.mainActivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.MenuProvider;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.BuildConfig;
import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.AndroidDeviceInfo;
import org.twinlife.twinlife.BaseService.ErrorCode;
import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinlife.ConversationService.ClearMode;
import org.twinlife.twinlife.ConversationService.Conversation;
import org.twinlife.twinlife.ConversationService.Descriptor;
import org.twinlife.twinlife.ConversationService.DescriptorId;
import org.twinlife.twinlife.DisplayCallsMode;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Group;
import org.twinlife.twinme.models.GroupMember;
import org.twinlife.twinme.models.Originator;
import org.twinlife.twinme.models.Profile;
import org.twinlife.twinme.models.Space;
import org.twinlife.twinme.services.ChatService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.skin.DisplayMode;
import org.twinlife.twinme.ui.AddContactActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.Settings;
import org.twinlife.twinme.ui.TwinmeActivity;
import org.twinlife.twinme.ui.TwinmeApplication;
import org.twinlife.twinme.ui.accountMigrationActivity.AccountMigrationScannerActivity;
import org.twinlife.twinme.ui.cleanupActivity.ResetConversationConfirmView;
import org.twinlife.twinme.ui.conversationActivity.ConversationActivity;
import org.twinlife.twinme.ui.conversationFilesActivity.CustomTabView;
import org.twinlife.twinme.ui.conversationFilesActivity.UICustomTab;
import org.twinlife.twinme.ui.conversations.ConversationsSearchAdapter;
import org.twinlife.twinme.ui.newConversationActivity.NewConversationActivity;
import org.twinlife.twinme.ui.profiles.AddProfileActivity;
import org.twinlife.twinme.ui.settingsActivity.QualityOfServiceActivity;
import org.twinlife.twinme.ui.users.UIContact;
import org.twinlife.twinme.utils.AbstractBottomSheetView;
import org.twinlife.twinme.utils.CommonUtils;
import org.twinlife.twinme.utils.SwipeItemTouchHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ConversationsFragment extends TabbarFragment implements ChatService.Observer, ViewTreeObserver.OnGlobalLayoutListener, CustomTabView.Observer {
    private static final String LOG_TAG = "ConversationsFragment";
    private static final boolean DEBUG = false;

    public enum SearchFilter {
        ALL,
        CONTACTS,
        GROUPS,
        MESSAGES
    }

    public static final int MIN_RESULTS_VISIBLE = 3;

    private static final float DESIGN_RESTRICTION_VIEW_HEIGHT = 300f;
    private static final float DESIGN_IMAGE_VIEW_HEIGHT = 480f;
    private static final float DESIGN_NO_CONVERSATION_IMAGE_VIEW_HEIGHT = 588f;
    private static final float DESIGN_NO_RESULT_IMAGE_VIEW_HEIGHT = 260f;
    private static final float DESIGN_NO_RESULT_IMAGE_VIEW_WIDTH = 380f;
    private static final float DESIGN_CELL_MARGIN_LINE = 2;
    private static final float DESIGN_CELL_NAME_MARGIN = 16;
    private static final float DESIGN_TAB_HEIGHT = 150;

    private RecyclerView mUIConversationRecyclerView;
    private ImageView mNoConversationImageView;
    private TextView mNoConversationTextView;
    private View mInviteContactView;
    private View mTransferView;
    private View mStartConversationView;
    private RestrictionView mRestrictionView;
    private ImageView mNewChatImageView;
    private SearchView mMessagesSearchView;

    private CustomTabView mCustomTabView;
    private View mNoResultFoundView;
    private TextView mNoResultFoundTitleView;
    private RecyclerView mSearchRecyclerView;
    private ConversationsSearchAdapter mConversationsSearchAdapter;

    private TextView mConversationsTitleView;
    private RadioGroup mConversationsRadioGroup;
    private RadioButton mAllRadioButton;
    private RadioButton mGroupsRadioButton;
    private UIConversationListAdapter mUIConversationListAdapter;

    private final Map<UUID, UIContact> mUIContacts = new HashMap<>();
    private final Map<UUID, UIContact> mUIGroups = new HashMap<>();
    private final List<UIConversation> mUIConversations = new ArrayList<>();
    private final ArrayList<UIConversation> mFilteredConversations = new ArrayList<>();
    private final Map<UUID, GroupMember> mGroupMembers = new HashMap<>();
    private final Map<UUID, UIGroupConversation> mMembersToGroupConversations = new HashMap<>();
    private final Map<UUID, UIConversation> mUIConversationsMap = new HashMap<>();

    private final ArrayList<UIConversation> mSearchContacts = new ArrayList<>();
    private final ArrayList<UIGroupConversation> mSearchGroups = new ArrayList<>();
    private final ArrayList<UIConversation> mSearchConversations = new ArrayList<>();

    private boolean mUIInitialized = false;

    private boolean mOnGetConversationsIsDone = false;
    private boolean mOnlyGroups;
    private String mLastSearch = "";
    private int mItemHeight = 0;
    private int mItemNameMargin = 0;
    private boolean mShowAllContacts = false;
    private boolean mShowAllGroups = false;
    private SearchFilter mSearchFilter = SearchFilter.ALL;

    private ChatService mChatService;

    @Nullable
    private Menu mMenu;

    public interface OnConversationClickListener {

        void onConversationClick(int position);
    }

    public interface OnConversationLongClickListener {

        void onConversationLongClick(int position);
    }

    private class UIConversationListAdapter extends RecyclerView.Adapter<UIConversationViewHolder> {

        private final OnConversationClickListener mOnConversationClickListener;
        private final OnConversationLongClickListener mOnConversationLongClickListener;

        UIConversationListAdapter(OnConversationClickListener onConversationClickListener, OnConversationLongClickListener onConversationLongClickListener) {

            mOnConversationClickListener = onConversationClickListener;
            mOnConversationLongClickListener = onConversationLongClickListener;
            setHasStableIds(true);
        }

        @Override
        public int getItemCount() {
            if (DEBUG) {
                Log.d(LOG_TAG, "UIConversationListAdapter.getItemCount");
            }

            return mFilteredConversations.size();
        }

        @Override
        public long getItemId(int position) {
            if (DEBUG) {
                Log.d(LOG_TAG, "UIConversationListAdapter.getItemId: position=" + position);
            }

            return mFilteredConversations.get(position).getItemId();
        }

        @Override
        public void onBindViewHolder(@NonNull UIConversationViewHolder viewHolder, int position) {
            if (DEBUG) {
                Log.d(LOG_TAG, "UIConversationListAdapter.onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
            }

            if (mTwinmeActivity != null) {
                boolean hideSeparator = position + 1 == mFilteredConversations.size();
                viewHolder.onBind(mTwinmeActivity, mFilteredConversations.get(position), hideSeparator);
            }
        }

        @Override
        @NonNull
        public UIConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (DEBUG) {
                Log.d(LOG_TAG, "UIConversationListAdapter.onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
            }

            LayoutInflater inflater = getLayoutInflater();
            View convertView = inflater.inflate(R.layout.conversations_fragment_conversation_item, parent, false);
            ViewGroup.LayoutParams layoutParams = convertView.getLayoutParams();
            layoutParams.height = getItemHeight();
            convertView.setLayoutParams(layoutParams);

            UIConversationViewHolder uiConversationViewHolder = new UIConversationViewHolder(mChatService, convertView, mItemNameMargin);
            convertView.setOnClickListener(v -> {
                int position = uiConversationViewHolder.getBindingAdapterPosition();
                if (position >= 0) {
                    mOnConversationClickListener.onConversationClick(position);
                }
            });
            convertView.setOnLongClickListener(v -> {
                int position = uiConversationViewHolder.getBindingAdapterPosition();
                if (position >= 0) {
                    mOnConversationLongClickListener.onConversationLongClick(position);
                }
                return false;
            });
            return uiConversationViewHolder;
        }

        @Override
        public void onViewRecycled(@NonNull UIConversationViewHolder viewHolder) {
            if (DEBUG) {
                Log.d(LOG_TAG, "UIConversationListAdapter.onViewRecycled: viewHolder=" + viewHolder);
            }

            viewHolder.onViewRecycled();
        }
    }

    // Default constructor is required by Android for proper activity restoration.
    public ConversationsFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateView: inflater=" + inflater + " container=" + container + " savedInstanceState=" + savedInstanceState);
        }

        View view = inflater.inflate(R.layout.conversations_fragment, container, false);

        initViews(view);
        addMenu();

        return view;
    }

    //
    // Override Fragment methods
    //

    @Override
    public void onAttach(@NonNull Context context) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onAttach");
        }

        super.onAttach(context);

        mOnlyGroups = mTwinmeActivity != null && mTwinmeActivity.getOnlyGroups();
    }

    @Override
    public void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        super.onResume();

        updateColor();
        updateFont();
        updateRestrictionView();

        if (mMessagesSearchView != null && !mMessagesSearchView.isIconified()) {
            mLastSearch = mMessagesSearchView.getQuery().toString();
            reloadSearchResult();
        } else {
            notifyConversationListChanged();
        }

        if (mConversationsRadioGroup != null) {
            mConversationsRadioGroup.invalidate();
            mAllRadioButton.invalidate();
            mGroupsRadioButton.invalidate();
        }
    }

    @Override
    public void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        super.onDestroy();

        if (mTwinmeActivity != null) {
            mTwinmeActivity.setOnlyGroups(mOnlyGroups);
        }

        if (mConversationsRadioGroup != null) {
            mConversationsRadioGroup.setOnCheckedChangeListener(null);
        }

        if (mNewChatImageView != null) {
            mNewChatImageView.setOnClickListener(null);
        }

        if (mMessagesSearchView != null) {
            mMessagesSearchView.setOnQueryTextListener(null);
        }

        mChatService.dispose();
    }

    @Override
    public void onGlobalLayout() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGlobalLayout");
        }

        int maxWidth = mAllRadioButton.getWidth();

        if (mGroupsRadioButton.getWidth() > maxWidth) {
            maxWidth = mGroupsRadioButton.getWidth();
        }

        Paint paint = new Paint();
        paint.setTypeface(Design.FONT_REGULAR32.typeface);
        paint.setTextSize(Design.FONT_REGULAR32.size);
        float textHeight = Math.abs(paint.getFontMetrics().ascent - paint.getFontMetrics().descent);

        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        float padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, displayMetrics);

        float defaultHeight = textHeight + padding;

        mAllRadioButton.setWidth(maxWidth);
        mGroupsRadioButton.setWidth(maxWidth);
        mAllRadioButton.setHeight((int)defaultHeight);
        mGroupsRadioButton.setHeight((int)defaultHeight);

        final ViewTreeObserver viewTreeObserver = mGroupsRadioButton.getViewTreeObserver();
        if (viewTreeObserver != null && viewTreeObserver.isAlive()) {
            viewTreeObserver.removeOnGlobalLayoutListener(this);
        }
    }

    public boolean isShowAllContacts() {

        return mShowAllContacts;
    }

    public boolean isShowAllGroups() {

        return mShowAllGroups;
    }

    //
    // Implement ChatService.Observer methods
    //

    @Override
    public void showProgressIndicator() {

    }

    @Override
    public void hideProgressIndicator() {

    }

    @Override
    public void onGetContacts(@NonNull List<Contact> contacts) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContacts: contacts=" + contacts);
        }

        // Fragment was detached.
        if (mTwinmeActivity == null) {
            return;
        }
        mUIContacts.clear();

        TwinmeApplication twinmeApplication = mTwinmeActivity.getTwinmeApplication();
        for (Contact contact : contacts) {
            UIContact uiContact = new UIContact(twinmeApplication, contact, null);
            mUIContacts.put(contact.getId(), uiContact);
        }
    }

    @Override
    public void onGetContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContact: contact=" + contact);
        }
    }

    @Override
    public void onGetContactNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContactNotFound");
        }
    }

    @Override
    public void onUpdateGroup(@NonNull Group group) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateGroup: group=" + group);
        }

        mChatService.getImage(group, (Bitmap avatar) -> {
            // Fragment was detached.
            if (mTwinmeActivity == null) {
                return;
            }
            UIContact uiContact = new UIContact(mTwinmeActivity.getTwinmeApplication(), group, avatar);

            for (UIConversation uiConversation : mUIConversations) {
                if (uiConversation.getContact().isGroup() && uiConversation.getContact().getId() == group.getId()) {
                    uiConversation.setUIContact(uiContact);
                    notifyConversationListChanged();
                    break;
                }
            }

            mUIGroups.put(group.getId(), uiContact);
        });
    }

    @Override
    public void onDeleteGroup(@NonNull UUID groupId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteGroup: groupId=" + groupId);
        }

        mUIGroups.remove(groupId);

        for (UIConversation uiConversation : mUIConversations) {
            if (uiConversation.getContact().getId() == groupId) {
                uiConversation.resetUIConversation();
                mUIConversations.remove(uiConversation);
                notifyConversationListChanged();
                break;
            }
        }
    }

    @Override
    public void onCreateContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateContact: contact=" + contact);
        }

        // Fragment was detached.
        if (mTwinmeActivity == null) {
            return;
        }
        UIContact uiContact = new UIContact(mTwinmeActivity.getTwinmeApplication(), contact, avatar);
        mUIContacts.put(contact.getId(), uiContact);
    }

    @Override
    public void onUpdateContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateContact: contact=" + contact);
        }

        // Fragment was detached.
        if (mTwinmeActivity == null) {
            return;
        }
        UIContact uiContact = new UIContact(mTwinmeActivity.getTwinmeApplication(), contact, avatar);

        for (UIConversation uiConversation : mUIConversations) {
            if (uiConversation.getContact().getId() == contact.getId()) {
                uiConversation.setUIContact(uiContact);
                notifyConversationListChanged();
                break;
            }
        }

        mUIContacts.put(contact.getId(), uiContact);
    }


    @Override
    public void onDeleteContact(@NonNull UUID contactId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteContact: contactId=" + contactId);
        }

        mUIContacts.remove(contactId);

        for (UIConversation uiConversation : mUIConversations) {
            if (uiConversation.getContact().getId() == contactId) {
                uiConversation.resetUIConversation();
                mUIConversations.remove(uiConversation);
                notifyConversationListChanged();
                break;
            }
        }
    }

    @Override
    public void onGetGroups(@NonNull List<Group> groups) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetGroups: groups=" + groups);
        }

        // Fragment was detached.
        if (mTwinmeActivity == null) {
            return;
        }
        mUIGroups.clear();

        if (groups.isEmpty()) {
            return;
        }

        TwinmeApplication twinmeApplication = mTwinmeActivity.getTwinmeApplication();
        for (Group group : groups) {
            UIContact uiContact = new UIContact(twinmeApplication, group, null);
            mUIGroups.put(group.getId(), uiContact);

            mChatService.getImage(group, uiContact::setAvatar);
        }
    }

    @Override
    public void onCreateGroup(@NonNull Group group, @NonNull ConversationService.GroupConversation conversation) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateGroup: group=" + group + " conversation=" + conversation);
        }

        mChatService.getImage(group, (Bitmap avatar) -> {
            // Fragment was detached.
            if (mTwinmeActivity == null) {
                return;
            }

            UIContact uiContact = new UIContact(mTwinmeActivity.getTwinmeApplication(), group, avatar);
            mUIGroups.put(group.getId(), uiContact);

            onGetOrCreateConversation(conversation);

            updateConversations(false);
        });
    }

    @Override
    public void onJoinGroup(@NonNull ConversationService.GroupConversation conversation, @Nullable UUID memberId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onJoinGroup: conversation=" + conversation + " memberId=" + memberId);
        }

        if (conversation.getState() != ConversationService.GroupConversation.State.JOINED) {

            return;
        }

        UIConversation uiConversation = mUIConversationsMap.get(conversation.getId());
        if (uiConversation instanceof UIGroupConversation) {
            // Refresh the UIGroupConversation object to display the new member.
            updateGroupConversation(conversation, (UIGroupConversation) uiConversation);
        } else {
            // Add the GroupConversation because we joined the group and it is now in the JOINED state and visible.
            onGetOrCreateConversation(conversation);
        }

        updateConversations(false);
    }

    @Override
    public void onLeaveGroup(@NonNull ConversationService.GroupConversation conversation, @Nullable UUID memberId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onLeaveGroup: conversation=" + conversation + " memberId=" + memberId);
        }

        if (conversation.getState() != ConversationService.GroupConversation.State.JOINED) {

            return;
        }

        UIConversation uiConversation = mUIConversationsMap.get(conversation.getId());
        if (uiConversation instanceof UIGroupConversation) {
            // Refresh the UIGroupConversation object to display the new member.
            updateGroupConversation(conversation, (UIGroupConversation) uiConversation);
        }

        updateConversations(false);
    }

    @Override
    public void onGetGroupMember(@Nullable UUID groupMemberTwincodeId, @Nullable GroupMember member) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetGroupMember: member=" + member);
        }

        if (member != null) {
            mGroupMembers.put(member.getPeerTwincodeOutboundId(), member);
        }
        UIGroupConversation conversation = mMembersToGroupConversations.get(groupMemberTwincodeId);
        if (conversation != null) {
            mChatService.getGroupMemberImage(member, (Bitmap avatar) -> {
                conversation.updateVisibleMembers(mGroupMembers, groupMemberTwincodeId, avatar);
                int indexConversation = -1;
                for (UIConversation lUIConversation : mUIConversations) {
                    indexConversation++;
                    if (lUIConversation.getConversationId().equals(conversation.getConversationId())) {
                        break;
                    }
                }

                if (indexConversation != -1) {
                    mUIConversations.set(indexConversation, conversation);
                }

                indexConversation = -1;
                for (int i = 0; i < mFilteredConversations.size(); i++) {
                    UIConversation uiConversation = mFilteredConversations.get(i);
                    if (uiConversation.getConversationId().equals(conversation.getConversationId())) {
                        indexConversation = i;
                        break;
                    }
                }

                if (indexConversation != -1) {
                    mUIConversationListAdapter.notifyItemChanged(indexConversation);
                }
            });
        }
    }

    @Override
    public void onGetConversations(@NonNull Map<Conversation, Descriptor> conversations) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetConversations: conversations=" + conversations);
        }

        mOnGetConversationsIsDone = true;

        mUIConversations.clear();

        for (Map.Entry<Conversation, Descriptor> item : conversations.entrySet()) {
            final Conversation conversation = item.getKey();
            final Descriptor lastDescriptor = item.getValue();

            if (conversation.isActive()) {
                final Map<UUID, UIContact> contacts = conversation.isGroup() ? mUIGroups : mUIContacts;
                final UIContact uiContact = contacts.get(conversation.getContactId());
                if (uiContact != null) {
                    UIConversation uiConversation = uiContact.getUIConversation();
                    if (uiConversation == null || !uiConversation.getConversationId().equals(conversation.getId())) {
                        if (!conversation.isGroup()) {
                            uiConversation = new UIConversation(conversation.getId(), uiContact);
                        } else {
                            ConversationService.GroupConversation group = (ConversationService.GroupConversation) conversation;

                            uiConversation = new UIGroupConversation(conversation.getId(), uiContact, group.getState());
                            updateGroupConversation(group, (UIGroupConversation) uiConversation);
                        }
                        uiContact.setUIConversation(uiConversation);
                    }
                    uiConversation.setLastDescriptor(getContext(), lastDescriptor);
                    updateUIConversation(uiConversation);
                }
            }
        }

        updateConversations(false);
        notifyConversationListChanged();
    }

    @Override
    public void onFindConversationsByName(@NonNull Map<Conversation, Descriptor> conversations) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onFindConversationsByName: conversations=" + conversations);
        }

        for (Map.Entry<Conversation, Descriptor> item : conversations.entrySet()) {
            Conversation conversation = item.getKey();
            final Map<UUID, UIContact> contacts = conversation.isGroup() ? mUIGroups : mUIContacts;
            UIContact uiContact = contacts.get(conversation.getContactId());
            if (conversation.isActive() && uiContact != null) {
                UIConversation uiConversation = new UIConversation(conversation.getId(), uiContact);
                if (conversation.isGroup()) {
                    ConversationService.GroupConversation group = (ConversationService.GroupConversation) conversation;
                    UIGroupConversation uiGroupConversation = new UIGroupConversation(conversation.getId(), uiContact, group.getState());
                    List<ConversationService.GroupMemberConversation> members = group.getGroupMembers(ConversationService.MemberFilter.JOINED_MEMBERS);
                    uiGroupConversation.setGroupMemberCount(members.size());
                    mSearchGroups.add(uiGroupConversation);
                } else {
                    mSearchContacts.add(uiConversation);
                }
            }
        }

        if (mSearchContacts.size() <= MIN_RESULTS_VISIBLE) {
            mShowAllContacts = true;
        }

        if (mSearchGroups.size() <= MIN_RESULTS_VISIBLE) {
            mShowAllGroups = true;
        }

        if (mMessagesSearchView != null) {
            mChatService.searchDescriptorsByContent(mMessagesSearchView.getQuery().toString(), true);
        }
    }

    @Override
    public void onSearchDescriptors(@NonNull List<Pair<Conversation, Descriptor>> descriptors) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSearchDescriptors: descriptors=" + descriptors);
        }

        for (Pair<Conversation, Descriptor> pair : descriptors) {
            Conversation conversation = pair.first;
            final Map<UUID, UIContact> contacts = conversation.isGroup() ? mUIGroups : mUIContacts;
            UIContact uiContact = contacts.get(conversation.getContactId());
            if (conversation.isActive() && uiContact != null) {
                UIConversation uiConversation = new UIConversation(conversation.getId(), uiContact);
                if (conversation.isGroup()) {
                    ConversationService.GroupConversation group = (ConversationService.GroupConversation) conversation;
                    UIGroupConversation uiGroupConversation = new UIGroupConversation(conversation.getId(), uiContact, group.getState());
                    List<ConversationService.GroupMemberConversation> members = group.getGroupMembers(ConversationService.MemberFilter.JOINED_MEMBERS);
                    uiGroupConversation.setGroupMemberCount(members.size());
                    uiGroupConversation.setLastDescriptor(mTwinmeActivity, pair.second);
                    mSearchConversations.add(uiGroupConversation);
                } else {
                    uiConversation.setLastDescriptor(mTwinmeActivity, pair.second);
                    mSearchConversations.add(uiConversation);
                }
            }
        }

        reloadSearchResult();

        if (!mChatService.isGetDescriptorsDone() && mMessagesSearchView != null && !mMessagesSearchView.getQuery().toString().isEmpty()) {
            mChatService.searchDescriptorsByContent(mMessagesSearchView.getQuery().toString(), false);
        }
    }

    @Override
    public void onGetOrCreateConversation(@NonNull ConversationService.Conversation conversation) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetOrCreateConversation: conversation=" + conversation);
        }

        updateConversation(conversation, false);
        notifyConversationListChanged();
    }

    private void updateGroupConversation(ConversationService.GroupConversation conversation, UIGroupConversation uiConversation) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateGroupConversation: conversation=" + conversation);
        }

        List<ConversationService.GroupMemberConversation> members = conversation.getGroupMembers(ConversationService.MemberFilter.JOINED_MEMBERS);
        // Prepare a list of group members to be displayed below the group conversation name.
        List<UUID> uiMemberList = new ArrayList<>();
        for (int i = 0; i < 5 && i < members.size(); i++) {
            ConversationService.GroupMemberConversation member = members.get(i);
            uiMemberList.add(member.getMemberTwincodeOutboundId());
            mMembersToGroupConversations.put(member.getMemberTwincodeOutboundId(), uiConversation);
        }
        uiConversation.setVisibleMembers(uiMemberList);
        uiConversation.setGroupConversationState(conversation.getState());
        uiConversation.setGroupMemberCount(members.size());

        List<UUID> unknownMembers = uiConversation.updateVisibleMembers(mGroupMembers, null, null);
        mChatService.getGroupMembers((Group) uiConversation.getContact(), unknownMembers);
    }

    @Override
    public void onResetConversation(@NonNull ConversationService.Conversation conversation, @NonNull ClearMode clearMode) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResetConversation: conversation=" + conversation + " clearMode=" + clearMode);
        }

        if (clearMode == ClearMode.CLEAR_MEDIA) {
            return;
        }

        UIConversation uiConversation = mUIConversationsMap.remove(conversation.getId());
        if (uiConversation != null) {
            if (conversation.isGroup()) {
                uiConversation.setLastDescriptor(null, null);
            } else {
                uiConversation.resetUIConversation();
                mUIConversations.remove(uiConversation);
            }
            notifyConversationListChanged();
        }
    }

    @Override
    public void onDeleteConversation(@NonNull UUID conversationId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteConversation: conversationId=" + conversationId);
        }

        UIConversation uiConversation = mUIConversationsMap.remove(conversationId);
        if (uiConversation != null) {
            uiConversation.resetUIConversation();
            mUIConversations.remove(uiConversation);

            notifyConversationListChanged();
        }
    }

    @Override
    public void onPushDescriptor(@NonNull ConversationService.Descriptor descriptor, @NonNull ConversationService.Conversation conversation) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPushDescriptor: descriptor=" + descriptor + "conversation=" + conversation);
        }

        if (descriptor.getType() == ConversationService.Descriptor.Type.TRANSIENT_OBJECT_DESCRIPTOR) {
            return;
        }

        if (conversation instanceof ConversationService.GroupMemberConversation) {
            conversation = ((ConversationService.GroupMemberConversation) conversation).getGroupConversation();
        }
        UIConversation uiConversation = mUIConversationsMap.get(conversation.getId());

        // If the conversation is not known, create it because we sent a first message.
        if (uiConversation == null) {
            updateConversation(conversation, true);
            notifyConversationListChanged();
        } else {
            if (uiConversation.getLastDescriptor() == null || (uiConversation.getLastDescriptor() != null && uiConversation.getLastDescriptor().getCreatedTimestamp() < descriptor.getCreatedTimestamp())) {
                uiConversation.setLastDescriptor(getContext(), descriptor);
                updateUIConversation(uiConversation);
                notifyConversationListChanged();
            }
        }
    }

    @Override
    public void onPopDescriptor(@NonNull ConversationService.Descriptor descriptor, @NonNull ConversationService.Conversation conversation) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPopDescriptor: descriptor=" + descriptor + "conversation=" + conversation);
        }

        if (descriptor.getType() == ConversationService.Descriptor.Type.TRANSIENT_OBJECT_DESCRIPTOR) {
            return;
        }

        if (conversation instanceof ConversationService.GroupMemberConversation) {
            conversation = ((ConversationService.GroupMemberConversation) conversation).getGroupConversation();
        }
        UIConversation uiConversation = mUIConversationsMap.get(conversation.getId());

        // If the conversation is not known, create it because we sent a first message.
        if (uiConversation == null) {
            updateConversation(conversation, true);
            if (mMessagesSearchView != null && mMessagesSearchView.getQuery().toString().isEmpty()) {
                notifyConversationListChanged();
            }
        } else {
            if (uiConversation.getLastDescriptor() == null || (uiConversation.getLastDescriptor() != null && uiConversation.getLastDescriptor().getCreatedTimestamp() < descriptor.getCreatedTimestamp())) {
                uiConversation.setLastDescriptor(getContext(), descriptor);
                updateUIConversation(uiConversation);
                if (mMessagesSearchView != null && mMessagesSearchView.getQuery().toString().isEmpty()) {
                    notifyConversationListChanged();
                } else {
                    updateConversations(false);
                }
            }
        }
    }

    @Override
    public void onUpdateDescriptor(@NonNull ConversationService.Descriptor descriptor, @NonNull ConversationService.Conversation conversation) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateDescriptor: descriptor=" + descriptor + "conversation=" + conversation);
        }

        if (descriptor.getType() == ConversationService.Descriptor.Type.TRANSIENT_OBJECT_DESCRIPTOR) {
            return;
        }

        if (conversation instanceof ConversationService.GroupMemberConversation) {
            conversation = ((ConversationService.GroupMemberConversation) conversation).getGroupConversation();
        }

        UIConversation uiConversation = mUIConversationsMap.get(conversation.getId());
        if (uiConversation != null) {
            if (uiConversation.getLastDescriptor() != null && uiConversation.getLastDescriptor().getDescriptorId().equals(descriptor.getDescriptorId())) {
                uiConversation.setLastDescriptor(getContext(), descriptor);
                updateUIConversation(uiConversation);
                if (mMessagesSearchView != null && mMessagesSearchView.getQuery().toString().isEmpty()) {
                    notifyConversationListChanged();
                } else {
                    updateConversations(false);
                }
            }
        }
    }

    @Override
    public void onDeleteDescriptors(@NonNull Set<DescriptorId> descriptorIdSet, @NonNull Conversation conversation) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteDescriptors: descriptorIdSet=" + descriptorIdSet.size() + "conversation=" + conversation);
        }

        if (conversation instanceof ConversationService.GroupMemberConversation) {
            conversation = ((ConversationService.GroupMemberConversation) conversation).getGroupConversation();
        }

        UIConversation uiConversation = mUIConversationsMap.get(conversation.getId());
        if (uiConversation != null) {

            Descriptor descriptor = uiConversation.getLastDescriptor();
            if (descriptor != null && descriptorIdSet.remove(descriptor.getDescriptorId())) {
                mChatService.getLastDescriptor(conversation, (ErrorCode errorCode, Descriptor lastDescriptor) -> {
                    uiConversation.setLastDescriptor(getContext(), lastDescriptor);
                    updateUIConversation(uiConversation);
                    if (mMessagesSearchView != null && mMessagesSearchView.getQuery().toString().isEmpty()) {
                        notifyConversationListChanged();
                    } else {
                        updateConversations(false);
                    }
                });
            }
        }
    }

    @Override
    public void onSetCurrentSpace(@NonNull Space space) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSetCurrentSpace: space=" + space);
        }

        mOnlyGroups = false;
        mConversationsRadioGroup.check(R.id.conversations_tool_bar_all_radio);
        updateColor();
    }

    void onUIConversationLongPress(UIConversation uiConversation) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUIConversationLongPress: uiConversation=" + uiConversation);
        }

        // This fragment is detached and has no activity: ignore the action.
        if (!isAdded() || mTwinmeActivity == null) {
            return;
        }

        mTwinmeActivity.hapticFeedback();
        showContactActivity(uiConversation.getContact());
    }

    //
    // Implement CustomTabView.Observer methods
    //

    @Override
    public void onSelectCustomTab(UICustomTab customTab) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSelectCustomTab: customTab=" + customTab);
        }

        if (mTwinmeActivity == null) {
            return;
        }

        mTwinmeActivity.hapticFeedback();

        if (customTab.getCustomTabType() == UICustomTab.CustomTabType.ALL) {
            mSearchFilter = SearchFilter.ALL;
        } else if (customTab.getCustomTabType() == UICustomTab.CustomTabType.CONTACTS) {
            mSearchFilter = SearchFilter.CONTACTS;
        } else if (customTab.getCustomTabType() == UICustomTab.CustomTabType.GROUPS) {
            mSearchFilter = SearchFilter.GROUPS;
        } else {
            mSearchFilter = SearchFilter.MESSAGES;
        }

        reloadSearchResult();
    }

    private void initViews(View view) {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews: view=" + view);
        }

        // Fragment was detached.
        if (mTwinmeActivity == null) {
            return;
        }

        mConversationsTitleView = mTwinmeActivity.findViewById(R.id.conversations_tool_bar_title);
        Design.updateTextFont(mConversationsTitleView, Design.FONT_BOLD34);
        mConversationsTitleView.setTextColor(Color.WHITE);
        mConversationsTitleView.setText(getString(R.string.conversations_fragment_title));

        mConversationsRadioGroup = mTwinmeActivity.findViewById(R.id.conversations_tool_bar_radio_group);

        mAllRadioButton = mTwinmeActivity.findViewById(R.id.conversations_tool_bar_all_radio);
        Design.updateTextFont(mAllRadioButton, Design.FONT_REGULAR32);

        ColorStateList colorStateList = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{}
                },
                new int[]{
                        Design.getMainStyle(),
                        Color.WHITE
                }
        );

        mAllRadioButton.setTextColor(colorStateList);

        mGroupsRadioButton = mTwinmeActivity.findViewById(R.id.conversations_tool_bar_groups_radio);
        Design.updateTextFont(mGroupsRadioButton, Design.FONT_REGULAR32);
        mGroupsRadioButton.setTextColor(colorStateList);

        final Resources resources = mTwinmeActivity.getResources();
        if (CommonUtils.isLayoutDirectionRTL()) {
            mAllRadioButton.setBackground(ResourcesCompat.getDrawable(resources, R.drawable.segmented_control_right, mTwinmeActivity.getTheme()));
            mGroupsRadioButton.setBackground(ResourcesCompat.getDrawable(resources, R.drawable.segmented_control_left, mTwinmeActivity.getTheme()));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mAllRadioButton.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
            mGroupsRadioButton.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
        }

        mGroupsRadioButton.getViewTreeObserver().addOnGlobalLayoutListener(this);

        mConversationsRadioGroup.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            mUIConversationRecyclerView.smoothScrollToPosition(0);
            if (checkedId == R.id.conversations_tool_bar_all_radio) {
                mOnlyGroups = false;
            } else if (checkedId == R.id.conversations_tool_bar_groups_radio) {
                mOnlyGroups = true;
            }

            updateConversations(true);
        });

        OnConversationClickListener onConversationClickListener = position -> {
            UIConversation uiConversation = mFilteredConversations.get(position);
            onUIConversationClick(uiConversation, false);
        };

        OnConversationLongClickListener onConversationLongClickListener = position -> {
            UIConversation uiConversation = mFilteredConversations.get(position);
            onUIConversationLongPress(uiConversation);
        };

        // Setup the service after the view is initialized but before the adapter.
        final DisplayCallsMode callsMode = mTwinmeActivity.getTwinmeApplication().displayCallsMode();
        mChatService = new ChatService(mTwinmeActivity, mTwinmeActivity.getTwinmeContext(), callsMode, this);

        mUIConversationListAdapter = new UIConversationListAdapter(onConversationClickListener, onConversationLongClickListener);

        LinearLayoutManager uiConversationLinearLayoutManager = new LinearLayoutManager(mTwinmeActivity, RecyclerView.VERTICAL, false);
        mUIConversationRecyclerView = view.findViewById(R.id.conversations_fragment_list_view);
        mUIConversationRecyclerView.setLayoutManager(uiConversationLinearLayoutManager);
        mUIConversationRecyclerView.setAdapter(mUIConversationListAdapter);
        mUIConversationRecyclerView.setItemViewCacheSize(Design.ITEM_LIST_CACHE_SIZE);
        mUIConversationRecyclerView.setItemAnimator(null);

        SwipeItemTouchHelper.OnSwipeItemClickListener onSwipeItemClickListener = new SwipeItemTouchHelper.OnSwipeItemClickListener() {

            @Override
            public void onLeftActionClick(int adapterPosition) {

            }

            @Override
            public void onRightActionClick(int adapterPosition) {

                onUIConversationResetClick(mFilteredConversations.get(adapterPosition));
            }

            @Override
            public void onOtherActionClick(int adapterPosition) {

            }
        };
        SwipeItemTouchHelper swipeItemTouchHelper = new SwipeItemTouchHelper(mUIConversationRecyclerView, null, SwipeItemTouchHelper.ButtonType.RESET, onSwipeItemClickListener);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeItemTouchHelper);
        itemTouchHelper.attachToRecyclerView(mUIConversationRecyclerView);

        mNoConversationImageView = view.findViewById(R.id.conversations_fragment_no_conversation_image_view);

        ViewGroup.LayoutParams layoutParams = mNoConversationImageView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_IMAGE_VIEW_HEIGHT * Design.HEIGHT_RATIO);

        mNoConversationTextView = view.findViewById(R.id.conversations_fragment_no_conversation_text_view);
        Design.updateTextFont(mNoConversationTextView, Design.FONT_MEDIUM34);
        mNoConversationTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mInviteContactView = view.findViewById(R.id.conversations_fragment_invite_contact_view);
        mInviteContactView.setOnClickListener(v -> onAddContactClick());

        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable saveViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        saveViewBackground.getPaint().setColor(Design.getMainStyle());
        mInviteContactView.setBackground(saveViewBackground);

        layoutParams = mInviteContactView.getLayoutParams();
        layoutParams.height = Design.BUTTON_HEIGHT;

        TextView inviteContactTextView = view.findViewById(R.id.conversations_fragment_invite_contact_title_view);
        Design.updateTextFont(inviteContactTextView, Design.FONT_MEDIUM34);
        inviteContactTextView.setTextColor(Color.WHITE);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) inviteContactTextView.getLayoutParams();
        marginLayoutParams.leftMargin = Design.BUTTON_MARGIN;
        marginLayoutParams.rightMargin = Design.BUTTON_MARGIN;

        mTransferView = view.findViewById(R.id.conversations_fragment_transfer_view);
        mTransferView.setOnClickListener(v -> onTransferClick());

        TextView transferTextView = view.findViewById(R.id.conversations_fragment_transfer_text_view);
        Design.updateTextFont(transferTextView, Design.FONT_REGULAR26);
        transferTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        transferTextView.setPaintFlags(transferTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        mStartConversationView = view.findViewById(R.id.conversations_fragment_start_conversation_view);
        mStartConversationView.setOnClickListener(v -> onAddGroupClick());

        ShapeDrawable startViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        startViewBackground.getPaint().setColor(Design.getMainStyle());
        mStartConversationView.setBackground(startViewBackground);

        layoutParams = mStartConversationView.getLayoutParams();
        layoutParams.height = Design.BUTTON_HEIGHT;

        TextView startConversationTextView = view.findViewById(R.id.conversations_fragment_start_conversation_titl_view);
        Design.updateTextFont(startConversationTextView, Design.FONT_MEDIUM34);
        startConversationTextView.setTextColor(Color.WHITE);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) startConversationTextView.getLayoutParams();
        marginLayoutParams.leftMargin = Design.BUTTON_MARGIN;
        marginLayoutParams.rightMargin = Design.BUTTON_MARGIN;

        mRestrictionView = view.findViewById(R.id.conversations_fragment_restriction_view);
        mRestrictionView.setOnClickListener(v -> onRestrictionClick());

        RestrictionView.Observer observer = this::startQualityOfServices;
        mRestrictionView.setObserver(observer);

        layoutParams = mRestrictionView.getLayoutParams();
        layoutParams.width = Design.DISPLAY_WIDTH;
        layoutParams.height = (int) (DESIGN_RESTRICTION_VIEW_HEIGHT * Design.HEIGHT_RATIO);

        mCustomTabView = view.findViewById(R.id.conversations_fragment_search_tab_view);

        layoutParams = mCustomTabView.getLayoutParams();
        layoutParams.height = (int ) (DESIGN_TAB_HEIGHT * Design.HEIGHT_RATIO);

        initTabs();

        mNoResultFoundView = view.findViewById(R.id.conversations_fragment_no_result_found_view);

        ImageView noResultFoundImageView = view.findViewById(R.id.conversations_fragment_no_result_found_image_view);
        layoutParams = noResultFoundImageView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_NO_RESULT_IMAGE_VIEW_WIDTH * Design.WIDTH_RATIO);
        layoutParams.height = (int) (DESIGN_NO_RESULT_IMAGE_VIEW_HEIGHT * Design.HEIGHT_RATIO);

        mNoResultFoundTitleView = view.findViewById(R.id.conversations_fragment_no_result_found_title_view);
        Design.updateTextFont(mNoResultFoundTitleView, Design.FONT_MEDIUM34);
        mNoResultFoundTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        ConversationsSearchAdapter.OnSearchClickListener onSearchClickListener = new ConversationsSearchAdapter.OnSearchClickListener() {
            @Override
            public void onConversationClick(UIConversation uiConversation) {

                onUIConversationClick(uiConversation, true);
            }

            @Override
            public void onShowAllContactClick() {

                mShowAllContacts = true;
                reloadSearchResult();
            }

            @Override
            public void onShowAllGroupClick() {

                mShowAllGroups = true;
                reloadSearchResult();
            }

            @Override
            public void onCurrentListChanged() {
                // mSearchRecyclerView.scrollToPosition(0) does not scroll if the first element is already partially visible.
                RecyclerView.LayoutManager layoutManager = mSearchRecyclerView.getLayoutManager();
                if (layoutManager instanceof LinearLayoutManager) {
                    ((LinearLayoutManager) layoutManager).scrollToPositionWithOffset(0, 0);
                }
            }
        };

        mConversationsSearchAdapter = new ConversationsSearchAdapter(this, mChatService, onSearchClickListener);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mTwinmeActivity, RecyclerView.VERTICAL, false);
        mSearchRecyclerView = view.findViewById(R.id.conversations_fragment_search_result_view);
        mSearchRecyclerView.setAdapter(mConversationsSearchAdapter);
        mSearchRecyclerView.setLayoutManager(linearLayoutManager);
        mSearchRecyclerView.setBackgroundColor(Design.WHITE_COLOR);

        mUIInitialized = true;
    }

    private void onAddGroupClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onAddGroupClick");
        }

        // This fragment is detached and has no activity: ignore the action.
        if (!isAdded()) {
            return;
        }

        if (mTwinmeActivity != null && mTwinmeActivity.getProfile() != null && !mUIContacts.isEmpty()) {
            Intent intent = new Intent(getContext(), NewConversationActivity.class);
            startActivity(intent);
        }
    }

    private void onAddContactClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onAddContactClick");
        }

        // This fragment is detached and has no activity: ignore the action.
        if (!isAdded() || mTwinmeActivity == null) {
            return;
        }

        Profile profile = mTwinmeActivity.getProfile();
        Intent intent;
        if (profile != null) {
            intent = new Intent(getContext(), AddContactActivity.class);
            intent.putExtra(Intents.INTENT_PROFILE_ID, profile.getId().toString());
            intent.putExtra(Intents.INTENT_INVITATION_MODE, AddContactActivity.InvitationMode.INVITE);
        } else {
            intent = new Intent(getContext(), AddProfileActivity.class);
            intent.putExtra(Intents.INTENT_FIRST_PROFILE, true);
            intent.putExtra(Intents.INTENT_FROM_CONTACT, true);
        }
        startActivity(intent);
    }

    private void onTransferClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onTransferClick");
        }

        // This fragment is detached and has no activity: ignore the action.
        if (!isAdded() || mTwinmeActivity == null) {
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(Intents.INTENT_MIGRATION_FROM_CURRENT_DEVICE, false);
        intent.setClass(mTwinmeActivity, AccountMigrationScannerActivity.class);
        startActivity(intent);
    }

    private void onRestrictionClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRestrictionClick");
        }

        // This fragment is detached and has no activity: ignore the action.
        if (!isAdded()) {
            return;
        }

        if (mTwinmeActivity != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                final AndroidDeviceInfo androidDeviceInfo = new AndroidDeviceInfo(mTwinmeActivity);

                boolean postNotificationEnable = true;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    postNotificationEnable = mTwinmeActivity.checkPermissionsWithoutRequest(new TwinmeActivity.Permission[]{TwinmeActivity.Permission.POST_NOTIFICATIONS});
                }

                // Order of checks must be the same as in RestrictionView.updateView().
                Intent intent = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !postNotificationEnable) {
                    if (!mTwinmeActivity.checkPermissions(new TwinmeActivity.Permission[]{TwinmeActivity.Permission.POST_NOTIFICATIONS})) {
                        intent = new Intent();
                        intent.setAction(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                        intent.putExtra("android.provider.extra.APP_PACKAGE", mTwinmeActivity.getPackageName());
                    }
                } else if (!NotificationManagerCompat.from(mTwinmeActivity).areNotificationsEnabled()) {
                    intent = new Intent();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        intent.setAction(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                        intent.putExtra("android.provider.extra.APP_PACKAGE", mTwinmeActivity.getPackageName());
                    } else {
                        intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                        intent.putExtra("app_package", mTwinmeActivity.getPackageName());
                        intent.putExtra("app_uid", mTwinmeActivity.getApplicationInfo().uid);
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && androidDeviceInfo.isNetworkRestricted()) {
                    intent = new Intent(android.provider.Settings.ACTION_IGNORE_BACKGROUND_DATA_RESTRICTIONS_SETTINGS);
                    intent.setData(Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && androidDeviceInfo.isBackgroundRestricted()) {
                    intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                } else if (!androidDeviceInfo.isIgnoringBatteryOptimizations()) {
                    intent = new Intent();
                    intent.setAction(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                }

                if (intent != null) {
                    startActivity(intent);
                }
            }
        }
    }

    private void startQualityOfServices() {
        if (DEBUG) {
            Log.d(LOG_TAG, "startQualityOfServices");
        }

        // This fragment is detached and has no activity: ignore the action.
        if (!isAdded()) {
            return;
        }

        if (mTwinmeActivity != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setClass(mTwinmeActivity, QualityOfServiceActivity.class);
            startActivity(intent);
            mTwinmeActivity.overridePendingTransition(0, 0);
        }

    }


    private void notifyConversationListChanged() {
        if (DEBUG) {
            Log.d(LOG_TAG, "notifyConversationListChanged");
        }

        if (mUIInitialized && mTwinmeActivity != null) {
            updateConversations(false);

            boolean darkMode = false;
            final Resources resources = mTwinmeActivity.getResources();
            int currentNightMode = resources.getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            int displayMode = Settings.displayMode.getInt();
            if ((currentNightMode == Configuration.UI_MODE_NIGHT_YES && displayMode == DisplayMode.SYSTEM.ordinal())  || displayMode == DisplayMode.DARK.ordinal()) {
                darkMode = true;
            }

            if (mUIContacts.isEmpty() && mUIConversations.isEmpty() && mOnGetConversationsIsDone) {
                if (mMenu != null) {
                    MenuItem newChatMenuItem = mMenu.findItem(R.id.new_chat_action);
                    setEnabled(newChatMenuItem, false);

                    MenuItem searchMenuItem = mMenu.findItem(R.id.search_action);
                    setEnabled(searchMenuItem, false);
                }
                mConversationsTitleView.setVisibility(View.VISIBLE);
                mConversationsRadioGroup.setVisibility(View.GONE);
                mNoConversationImageView.setVisibility(View.VISIBLE);
                mNoConversationTextView.setVisibility(View.VISIBLE);
                mInviteContactView.setVisibility(View.VISIBLE);
                mTransferView.setVisibility(View.VISIBLE);

                mStartConversationView.setVisibility(View.GONE);
                mUIConversationRecyclerView.setVisibility(View.GONE);
                mNoConversationImageView.setImageDrawable(ResourcesCompat.getDrawable(resources, darkMode ? R.drawable.onboarding_step3_dark : R.drawable.onboarding_step3, null));
                ViewGroup.LayoutParams layoutParams = mNoConversationImageView.getLayoutParams();
                layoutParams.height = (int) (DESIGN_IMAGE_VIEW_HEIGHT * Design.HEIGHT_RATIO);

                mNoConversationTextView.setText(getString(R.string.add_contact_activity_onboarding_message));
            } else if (mUIConversations.isEmpty() && mOnGetConversationsIsDone) {
                if (mMenu != null) {
                    MenuItem newChatMenuItem = mMenu.findItem(R.id.new_chat_action);
                    setEnabled(newChatMenuItem, true);

                    MenuItem searchMenuItem = mMenu.findItem(R.id.search_action);
                    setEnabled(searchMenuItem, false);
                }
                mConversationsTitleView.setVisibility(View.VISIBLE);
                mConversationsRadioGroup.setVisibility(View.GONE);
                mNoConversationImageView.setVisibility(View.VISIBLE);
                mNoConversationTextView.setVisibility(View.VISIBLE);
                mUIConversationRecyclerView.setVisibility(View.GONE);
                mInviteContactView.setVisibility(View.GONE);
                mTransferView.setVisibility(View.GONE);
                mStartConversationView.setVisibility(View.VISIBLE);
                mNoConversationImageView.setImageDrawable(ResourcesCompat.getDrawable(resources, darkMode ? R.drawable.onboarding_step2_dark : R.drawable.onboarding_step2, null));
                ViewGroup.LayoutParams layoutParams = mNoConversationImageView.getLayoutParams();
                layoutParams.height = (int) (DESIGN_NO_CONVERSATION_IMAGE_VIEW_HEIGHT * Design.HEIGHT_RATIO);

                mNoConversationTextView.setText(getString(R.string.conversations_fragment_no_conversation_message));
            } else if (mOnGetConversationsIsDone) {
                if (mMenu != null) {
                    MenuItem newChatMenuItem = mMenu.findItem(R.id.new_chat_action);
                    setEnabled(newChatMenuItem, true);

                    MenuItem searchMenuItem = mMenu.findItem(R.id.search_action);
                    setEnabled(searchMenuItem, true);
                }
                mConversationsTitleView.setVisibility(View.GONE);
                mConversationsRadioGroup.setVisibility(View.VISIBLE);
                mNoConversationImageView.setVisibility(View.GONE);
                mNoConversationTextView.setVisibility(View.GONE);
                mInviteContactView.setVisibility(View.GONE);
                mTransferView.setVisibility(View.GONE);
                mStartConversationView.setVisibility(View.GONE);
                mUIConversationRecyclerView.setVisibility(View.VISIBLE);
            }
        }
    }

    private void searchDescriptor(String content) {
        if (DEBUG) {
            Log.d(LOG_TAG, "searchDescriptor: " + content);
        }

        mSearchConversations.clear();
        mSearchContacts.clear();
        mSearchGroups.clear();

        mShowAllContacts = false;
        mShowAllGroups = false;

        if (!content.isEmpty()) {
            mChatService.findConversationsByName(content);
        } else {
            reloadSearchResult();
        }
    }

    private void reloadSearchResult() {
        if (DEBUG) {
            Log.d(LOG_TAG, "reloadSearchResult");
        }

        if (!mUIInitialized) {
            return;
        }

        String searchContent = "";

        if (mMessagesSearchView != null && mMessagesSearchView.getQuery() != null && !mMessagesSearchView.getQuery().toString().isEmpty()) {
            searchContent = mMessagesSearchView.getQuery().toString();
            showSearchView();
            } else {
            hideSearchView();
        }

        mConversationsSearchAdapter.updateSearchParam(mSearchFilter, searchContent, mSearchContacts, mSearchGroups, mSearchConversations);
    }

    private void showSearchView() {
        boolean hasResults = hasSearchResults();
        mUIConversationRecyclerView.setVisibility(View.GONE);
        mCustomTabView.setVisibility(View.VISIBLE);
        mSearchRecyclerView.setVisibility(hasResults ? View.VISIBLE : View.GONE);
        mNoResultFoundView.setVisibility(hasResults ? View.GONE : View.VISIBLE);
    }

    private boolean hasSearchResults() {
        switch (mSearchFilter) {
            case ALL:
                return !mSearchContacts.isEmpty() || !mSearchGroups.isEmpty() || !mSearchConversations.isEmpty();
            case CONTACTS:
                return !mSearchContacts.isEmpty();
            case GROUPS:
                return !mSearchGroups.isEmpty();
            case MESSAGES:
                return !mSearchConversations.isEmpty();
            default:
                return false;
        }
    }

    private void hideSearchView() {
        mUIConversationRecyclerView.setVisibility(View.VISIBLE);
        mSearchRecyclerView.setVisibility(View.GONE);
        mCustomTabView.setVisibility(View.GONE);
        mNoResultFoundView.setVisibility(View.GONE);
        // Reset tab view to make the ALL tab active.
        initTabs();
    }

    private void updateConversation(ConversationService.Conversation conversation, boolean refreshConversations) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateConversation: conversation=" + conversation);
        }

        final Map<UUID, UIContact> contacts = conversation.isGroup() ? mUIGroups : mUIContacts;
        UIContact uiContact = contacts.get(conversation.getContactId());
        if (uiContact != null) {
            UIConversation uiConversation = uiContact.getUIConversation();
            if (uiConversation == null || !uiConversation.getConversationId().equals(conversation.getId())) {
                if (!conversation.isGroup()) {
                    uiConversation = new UIConversation(conversation.getId(), uiContact);
                } else {
                    ConversationService.GroupConversation group = (ConversationService.GroupConversation) conversation;

                    uiConversation = new UIGroupConversation(conversation.getId(), uiContact, group.getState());
                    updateGroupConversation(group, (UIGroupConversation) uiConversation);
                }
                uiContact.setUIConversation(uiConversation);
            }

            final UIConversation lUIConversation = uiConversation;
            mChatService.getLastDescriptor(conversation, (ErrorCode errorCode, Descriptor lastDescriptor) -> {
                lUIConversation.setLastDescriptor(getContext(), lastDescriptor);
                updateUIConversation(lUIConversation);

                if (refreshConversations) {
                    notifyConversationListChanged();
                }
            });
        }
    }

    private void updateUIConversation(UIConversation uiConversation) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateUIConversation: uiConversation=" + uiConversation);
        }

        UIConversation lUIConversation = mUIConversationsMap.get(uiConversation.getConversationId());
        if (lUIConversation != null) {
            mUIConversations.remove(lUIConversation);
        }

        boolean added = false;
        int size = mUIConversations.size();

        // Put the conversation at the top if it has the highest usage score.
        long lastMessageDate = uiConversation.getLastMessageDate();
        double score = uiConversation.getUsageScore();

        // Put the conversation according to the highest access time.
        for (int i = 0; i < size; i++) {
            UIConversation c = mUIConversations.get(i);
            if (lastMessageDate > c.getLastMessageDate()
                    || (lastMessageDate == 0 && c.getLastMessageDate() == 0 && score > c.getUsageScore())
                    || (lastMessageDate == 0 && c.getLastMessageDate() == 0 && score == c.getUsageScore() && mUIConversations.get(i).getName().compareToIgnoreCase(uiConversation.getName()) > 0)) {
                mUIConversations.add(i, uiConversation);
                added = true;
                break;
            }
        }

        if (!added) {
            mUIConversations.add(uiConversation);
        }
        mUIConversationsMap.put(uiConversation.getConversationId(), uiConversation);
    }

    private void onUIConversationClick(UIConversation uiConversation, boolean isSearchResult) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUIConversationClick: uiConversation=" + uiConversation);
        }

        // This fragment is detached and has no activity: ignore the action.
        if (!isAdded()) {
            return;
        }

        InputMethodManager inputMethodManager = (InputMethodManager) mTwinmeActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null && mMessagesSearchView != null) {
            mMessagesSearchView.setFocusable(false);
            inputMethodManager.hideSoftInputFromWindow(mMessagesSearchView.getWindowToken(), 0);
        }

        if (uiConversation.getContact().isGroup()) {
            UIGroupConversation groupConversation = (UIGroupConversation) uiConversation;
            if (groupConversation.getGroupMemberCount() == 0) {
                onUIConversationLongPress(uiConversation);
                return;
            }
        }

        Intent intent = new Intent(getContext(), ConversationActivity.class);

        if (uiConversation.getContact().isGroup()) {
            intent.putExtra(Intents.INTENT_GROUP_ID, uiConversation.getContact().getId().toString());
        } else {
            intent.putExtra(Intents.INTENT_CONTACT_ID, uiConversation.getContact().getId().toString());
        }

        if (isSearchResult && uiConversation.getLastDescriptor() != null) {
            intent.putExtra(Intents.INTENT_DESCRIPTOR_ID, uiConversation.getLastDescriptor().getDescriptorId().toString());
        }

        startActivity(intent);
    }

    private void onUIConversationResetClick(final UIConversation uiConversation) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUIConversationResetClick: uiConversation=" + uiConversation);
        }

        // This fragment is detached and has no activity: ignore the action.
        if (!isAdded() || mTwinmeActivity == null) {
            return;
        }

        mChatService.getImage(uiConversation.getContact(), (Bitmap avatar) -> {

            // While we fetched the image, this fragment was detached and has no activity: ignore the action.
            if (!isAdded() || mTwinmeActivity == null) {
                return;
            }

            Spanned message = Html.fromHtml(getString(R.string.main_activity_reset_conversation_message));
            if (uiConversation.getContact().isGroup()) {
                Group group = (Group) uiConversation.getContact();
                if (group.isOwner()) {
                    message = Html.fromHtml(getString(R.string.main_activity_reset_group_conversation_admin_message));
                } else {
                    message = Html.fromHtml(getString(R.string.main_activity_reset_group_conversation_message));
                }
            }
            DrawerLayout drawerLayout = mTwinmeActivity.findViewById(R.id.main_activity_drawer_layout);

            ResetConversationConfirmView resetConversationConfirmView = new ResetConversationConfirmView(mTwinmeActivity, null);
            resetConversationConfirmView.setAvatar(avatar, avatar == null || avatar.equals(mTwinmeActivity.getTwinmeApplication().getDefaultGroupAvatar()));
            resetConversationConfirmView.setMessage(message.toString());

            AbstractBottomSheetView.Observer observer = new AbstractBottomSheetView.Observer() {
                @Override
                public void onConfirmClick() {
                    mChatService.resetConversation(uiConversation.getContact());
                    resetConversationConfirmView.animationCloseConfirmView();
                }

                @Override
                public void onCancelClick() {
                    resetConversationConfirmView.animationCloseConfirmView();
                }

                @Override
                public void onDismissClick() {
                    resetConversationConfirmView.animationCloseConfirmView();
                }

                @Override
                public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                    drawerLayout.removeView(resetConversationConfirmView);
                    if (mTwinmeActivity != null) {
                        mTwinmeActivity.setStatusBarColor();
                    }
                }
            };
            resetConversationConfirmView.setObserver(observer);

            drawerLayout.addView(resetConversationConfirmView);
            resetConversationConfirmView.show();

            int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
            mTwinmeActivity.setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
        });
    }

    private void updateConversations(boolean scrollToTop) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateConversations");
        }

        if (!mUIInitialized) {
            return;
        }

        mFilteredConversations.clear();

        for (UIConversation uiConversation : mUIConversations) {
            Originator originator = uiConversation.getContact();

            if (!mOnlyGroups || originator.isGroup()) {
                mFilteredConversations.add(uiConversation);
            }
        }

        if (mMessagesSearchView != null && mMessagesSearchView.getQuery().toString().isEmpty()) {
            updateRestrictionView();
        }

        if (scrollToTop && !mFilteredConversations.isEmpty()) {
            mUIConversationRecyclerView.scrollToPosition(0);
        }

        mUIConversationListAdapter.notifyDataSetChanged();
    }

    private void updateRestrictionView() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateRestrictionView");
        }

        if (mTwinmeActivity != null) {
            final AndroidDeviceInfo androidDeviceInfo = new AndroidDeviceInfo(mTwinmeActivity);

            boolean postNotificationEnable = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                postNotificationEnable = mTwinmeActivity.checkPermissionsWithoutRequest(new TwinmeActivity.Permission[]{TwinmeActivity.Permission.POST_NOTIFICATIONS});
            }

            final boolean notificationDisabled = !NotificationManagerCompat.from(mTwinmeActivity).areNotificationsEnabled() || !postNotificationEnable;
            final boolean backgroundRestricted = androidDeviceInfo.isBackgroundRestricted();
            final boolean networkRestricted = androidDeviceInfo.isNetworkRestricted();
            final boolean lowUsage = !backgroundRestricted && androidDeviceInfo.isIgnoringBatteryOptimizations() && androidDeviceInfo.getAppStandbyBucket() > 30;

            if ((backgroundRestricted || lowUsage || notificationDisabled || networkRestricted) && !mUIConversations.isEmpty()) {
                mRestrictionView.setVisibility(View.VISIBLE);
                mRestrictionView.updateView(backgroundRestricted, networkRestricted, lowUsage, notificationDisabled);
            } else {
                mRestrictionView.setVisibility(View.GONE);
            }
        }
    }

    private void searchMessage(String text) {
        if (DEBUG) {
            Log.d(LOG_TAG, "searchMessage");
        }

        // This fragment is detached and has no activity: ignore the action.
        if (!isAdded() || mTwinmeActivity == null) {
            return;
        }

        if (mNoResultFoundTitleView != null) {
            mNoResultFoundTitleView.setText(String.format(getString(R.string.conversations_fragment_no_result_found), text));
        }
        
        searchDescriptor(text);
    }

    private void updateFont() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateFont");
        }

        Design.updateTextFont(mNoConversationTextView, Design.FONT_MEDIUM34);
        Design.updateTextFont(mAllRadioButton, Design.FONT_REGULAR32);
        Design.updateTextFont(mGroupsRadioButton, Design.FONT_REGULAR32);
    }

    private void updateColor() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateColor");
        }

        // Don't try to access the activity or the resource if the fragment is detached.
        if (mTwinmeActivity == null) {
            return;
        }

        mNoConversationTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        ColorStateList colorStateList = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{}
                },
                new int[]{
                        Design.getMainStyle(),
                        Color.WHITE
                });

        mAllRadioButton.setTextColor(colorStateList);
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
                menuInflater.inflate(R.menu.conversations_menu, menu);

                mMenu = menu;

                MenuItem menuItem = menu.findItem(R.id.new_chat_action);

                if (mNewChatImageView != null) {
                    // see CallsFragment.onCreateOptionsMenu()
                    mNewChatImageView.setOnClickListener(null);
                }

                mNewChatImageView = (ImageView) menuItem.getActionView();

                if (mNewChatImageView != null) {
                    mNewChatImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.action_bar_new_chat, null));
                    mNewChatImageView.setPadding(Design.TOOLBAR_IMAGE_ITEM_PADDING, 0, Design.TOOLBAR_IMAGE_ITEM_PADDING, 0);
                    mNewChatImageView.setOnClickListener(view -> onAddGroupClick());
                    mNewChatImageView.setContentDescription(getString(R.string.conversations_fragment_title));
                }

                MenuItem searchItem = menu.findItem(R.id.search_action);

                if (mUIInitialized && mTwinmeActivity != null) {

                    if (mUIContacts.isEmpty() && mUIConversations.isEmpty() && mOnGetConversationsIsDone) {
                        setEnabled(searchItem, false);
                    } else if (mUIConversations.isEmpty() && mOnGetConversationsIsDone) {
                        setEnabled(searchItem, false);
                    } else if (mOnGetConversationsIsDone) {
                        setEnabled(searchItem, true);
                    }
                }

                if (mMessagesSearchView != null) {
                    mMessagesSearchView.setOnQueryTextListener(null);
                }

                mMessagesSearchView = (SearchView) searchItem.getActionView();

                if (mMessagesSearchView != null) {

                    mMessagesSearchView.setQueryHint(getString(R.string.application_search_hint));

                    SearchView.OnQueryTextListener onQueryTextListener = new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String query) {
                            return false;
                        }

                        @Override
                        public boolean onQueryTextChange(String newText) {

                            searchMessage(newText);

                            return false;
                        }
                    };

                    mMessagesSearchView.setOnQueryTextListener(onQueryTextListener);

                    if (!mLastSearch.isEmpty()) {
                        searchItem.expandActionView();
                        mMessagesSearchView.setQuery(mLastSearch, false);
                        mLastSearch = "";
                    }
                }
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                // Handle menu item clicks here
                return false;
            }

        }, getViewLifecycleOwner());
    }

    private void initTabs() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initTabs");
        }

        List<UICustomTab> customTabs = new ArrayList<>();
        customTabs.add(new UICustomTab(getString(R.string.calls_fragment_all_call_segmented_control), UICustomTab.CustomTabType.ALL, true));
        customTabs.add(new UICustomTab(getString(R.string.contacts_fragment_title), UICustomTab.CustomTabType.CONTACTS, false));
        customTabs.add(new UICustomTab(getString(R.string.share_activity_group_list), UICustomTab.CustomTabType.GROUPS, false));
        customTabs.add(new UICustomTab(getString(R.string.settings_activity_chat_category_title), UICustomTab.CustomTabType.MESSAGES, false));

        mCustomTabView.initTabs(customTabs, this);
        mCustomTabView.updateColor(Design.WHITE_COLOR, Design.getMainStyle(), Color.WHITE, Design.GREY_ITEM_COLOR);
    }

    private int getItemHeight() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemHeight");
        }

        if (mItemHeight == 0) {
            Paint paint = new Paint();
            paint.setTypeface(Design.FONT_MEDIUM34.typeface);
            paint.setTextSize(Design.FONT_MEDIUM34.size);

            float nameHeight = Math.abs(paint.getFontMetrics().ascent - paint.getFontMetrics().descent);
            paint.setTypeface(Design.FONT_REGULAR30.typeface);
            paint.setTextSize(Design.FONT_REGULAR30.size);

            float messageHeight = Math.abs(paint.getFontMetrics().ascent - paint.getFontMetrics().descent);

            float textHeight = nameHeight + messageHeight * 2 + (DESIGN_CELL_MARGIN_LINE * Design.HEIGHT_RATIO);
            float avatarHeight = Design.AVATAR_HEIGHT;
            float minHeight = Design.ITEM_VIEW_HEIGHT;

            float contentHeight = avatarHeight;
            if (textHeight > avatarHeight) {
                contentHeight = textHeight;
                mItemNameMargin = (int) ((minHeight - avatarHeight) * 0.5);
            } else {
                mItemNameMargin = (int) (DESIGN_CELL_NAME_MARGIN * Design.HEIGHT_RATIO);
            }

            mItemHeight = (int) (mItemNameMargin * 2 + contentHeight);
        }

        return mItemHeight;
    }
}
