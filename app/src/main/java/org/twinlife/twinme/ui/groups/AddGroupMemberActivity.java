/*
 *  Copyright (c) 2018-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.groups;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinlife.ConversationService.GroupConversation;
import org.twinlife.twinlife.ConversationService.InvitationDescriptor;
import org.twinlife.twinlife.util.Utils;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Group;
import org.twinlife.twinme.models.GroupMember;
import org.twinlife.twinme.models.Originator;
import org.twinlife.twinme.models.Space;
import org.twinlife.twinme.services.GroupService;
import org.twinlife.twinme.services.GroupService.PendingInvitationsObserver;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.users.AddGroupMemberListAdapter;
import org.twinlife.twinme.ui.users.OnContactTouchListener;
import org.twinlife.twinme.ui.users.UIContact;
import org.twinlife.twinme.ui.users.UISelectableContact;
import org.twinlife.twinme.ui.users.UISelectableContactListAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Activity controller to select a list of contacts.
 */

public class AddGroupMemberActivity extends AbstractGroupActivity implements OnContactTouchListener.OnContactObserver, PendingInvitationsObserver {
    private static final String LOG_TAG = "AddGroupMemberActivity";
    private static final boolean DEBUG = false;

    private static final float DESIGN_SELECTED_BOTTOM_MARGIN = 40f;
    private static int SELECTED_BOTTOM_MARGIN;

    private boolean mUIInitialized = false;
    private boolean mUIPostInitialized = false;
    private UISelectableContactListAdapter mUIContactListAdapter;
    private AddGroupMemberListAdapter mSelectedUIContactListAdapter;
    private View mSearchView;
    private View mActionSaveView;
    private RecyclerView mSelectedUIContactRecyclerView;
    private RecyclerView mUIContactRecyclerView;
    private EditText mSearchEditText;
    private View mClearSearchView;
    private final List<UISelectableContact> mUIContacts = new ArrayList<>();
    private final List<UIContact> mUISelected = new ArrayList<>();
    private String mSelected;
    private GroupService mGroupService;
    @Nullable
    private Group mGroup;
    @NonNull
    private Set<UUID> mInvitedContacts = new HashSet<>();

    private boolean mFromCreateGroup = false;
    private boolean mAddInvitationList = false;

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

        mFromCreateGroup = intent.getBooleanExtra(Intents.INTENT_FROM_MENU, false);

        // Get the current selected contacts and setup the UI in onGetContacts when we know all of them.
        mSelected = intent.getStringExtra(Intents.INTENT_CONTACT_SELECTION);

        initViews();

        if (mFromCreateGroup) {
            mGroupService.getCurrentSpace();
        }

        String value = intent.getStringExtra(Intents.INTENT_GROUP_ID);
        if (value != null) {
            mGroupService.getGroup(UUID.fromString(value), false);
        }
    }

    @Override
    protected void onPause() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPause");
        }

        super.onPause();

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(mSearchEditText.getWindowToken(), 0);
        }
    }

    @Override
    protected void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        super.onResume();

        mGroupService.getContacts();
    }

    //
    // Override Activity methods
    //

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        mGroupService.dispose();

        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSaveInstanceState: outState=" + outState);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onWindowFocusChanged: hasFocus=" + hasFocus);
        }

        if (hasFocus && mUIInitialized && !mUIPostInitialized) {
            postInitViews();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateOptionsMenu: menu=" + menu);
        }

        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_group_member_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.add_action);

        TextView titleView = (TextView) menuItem.getActionView();
        String title = menuItem.getTitle().toString();

        if (titleView != null) {
            Design.updateTextFont(titleView, Design.FONT_BOLD36);
            titleView.setTextColor(Color.WHITE);
            titleView.setText(title);
            titleView.setPadding(0, 0, Design.TOOLBAR_TEXT_ITEM_PADDING, 0);
            titleView.setOnClickListener(view -> onSaveClicked());
        }

        return true;
    }

    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    public void onGetCurrentSpace(@NonNull Space space) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetCurrentSpace: " + space);
        }

        if (space.getProfile() != null && mFromCreateGroup) {
            mGroupService.getProfileImage(space.getProfile(), (Bitmap avatar) -> mSelectedUIContactListAdapter.setAvatar(avatar));
        }
    }

    /**
     * Get the list of contacts and build the UI contact list selector.
     *
     * @param contacts the list of contacts.
     */
    @Override
    public void onGetContacts(@NonNull List<Contact> contacts) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContacts contacts=" + contacts);
        }

        // Get the current selection of contacts (passed within the intent parameter).
        List<Contact> selected = new ArrayList<>();
        if (mSelected != null) {
            selected = fromIntentString(contacts, mSelected);
        }

        for (UIContact uiContact : mUISelected) {
            if (uiContact.getContact() instanceof Contact) {
                Contact contact = (Contact) uiContact.getContact();
                if (contact != null) {
                    selected.add(contact);
                }
            }
        }

        mUIContacts.clear();

        // Setup the list of contacts and mark those that are selected.
        for (Contact contact : contacts) {
            if (contact.hasPeer()) {

                UISelectableContact uiContact = mUIContactListAdapter.updateUIContact(contact, null);

                boolean isContactInUISelected = containsContact(mUISelected, contact);

                if (selected.contains(contact) && !isContactInUISelected) {
                    uiContact.setSelected(true);
                    mUISelected.add(uiContact);
                } else if (isContactInUISelected) {
                    uiContact.setSelected(true);
                }
                if (mInvitedContacts.contains(contact.getId())) {
                    uiContact.setInvited(true);
                }
            }
        }

        hideProgressIndicator();
        notifyContactListChanged();
    }

    /**
     * Serialize the list of contact ids into a simple string that can be exchanged between activities through intents.
     *
     * @param list list of contacts to serialize.
     * @return list of UUIDs separated by ','
     */
    public static <E extends Originator> String toIntentString(List<E> list) {
        if (DEBUG) {
            Log.d(LOG_TAG, "toIntentString list=" + list);
        }

        StringBuilder sb = new StringBuilder();
        for (E contact : list) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(contact.getId());
        }
        return sb.toString();
    }

    /**
     * Deserialize a list of contacts ids and return a list of contacts that are referenced by the id and
     * present in the allContacts list.
     *
     * @param allContacts all known contacts.
     * @param selection   list of contact UUIDs separated by ','
     * @return the contacts selected by selection.
     */
    @NonNull
    public static <E extends Originator> List<E> fromIntentString(@NonNull List<E> allContacts, @Nullable String selection) {
        List<E> result = new ArrayList<>();
        if (selection != null) {
            String[] items = selection.split(",");
            for (String s : items) {
                UUID contactId = Utils.UUIDFromString(s);
                if (contactId != null) {
                    for (E c : allContacts) {
                        if (contactId.equals(c.getId())) {
                            result.add(c);
                            break;
                        }
                    }
                }
            }
        }
        return result;
    }

    //
    // Implement GroupService.Observer methods
    //

    @Override
    public void onGetGroup(@NonNull Group group, @NonNull List<GroupMember> groupMembers, @NonNull GroupConversation conversation) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetGroup =" + group);
        }

        mGroup = group;

        GroupMember currentMember = group.getCurrentMember();

        AtomicInteger avatarCounter = new AtomicInteger(groupMembers.size());
        if (currentMember != null) {
            avatarCounter.addAndGet(1);
            mGroupService.getGroupMemberImage(currentMember, (Bitmap memberAvatar) -> {
                mSelectedUIContactListAdapter.updateUIContact(currentMember, memberAvatar);
                if (avatarCounter.decrementAndGet() == 0) {
                    notifyContactListChanged();
                }
            });
        }
        for (GroupMember member : groupMembers) {

            mGroupService.getGroupMemberImage(member, (Bitmap memberAvatar) -> {
                mSelectedUIContactListAdapter.updateUIContact(member, memberAvatar);

                if (avatarCounter.decrementAndGet() == 0) {
                    notifyContactListChanged();
                }
            });
        }

        notifyContactListChanged();
    }

    @Override
    public void onListPendingInvitations(@NonNull Map<UUID, InvitationDescriptor> list) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onListPendingInvitations");
        }

        mInvitedContacts = list.keySet();

        notifyContactListChanged();
    }

    //
    // Private methods
    //

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.add_group_member_activity);

        setStatusBarColor();
        setToolBar(R.id.add_group_member_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);
        setTitle(getString(R.string.add_group_member_activity_title));
        setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        applyInsets(R.id.add_group_member_activity_layout, R.id.add_group_member_activity_tool_bar, R.id.add_group_member_activity_list_view, Design.TOOLBAR_COLOR, false);

        TextView subTitleView = findViewById(R.id.toolbar_subtitle);
        if (subTitleView != null) {
            Design.updateTextFont(subTitleView, Design.FONT_REGULAR30);
            subTitleView.setTextColor(Color.WHITE);
        }

        mSearchView = findViewById(R.id.add_group_member_activity_search_view);
        mSearchView.setBackgroundColor(Design.TOOLBAR_COLOR);

        ViewGroup.LayoutParams layoutParams = mSearchView.getLayoutParams();
        layoutParams.height = Design.SEARCH_VIEW_HEIGHT;

        mClearSearchView = findViewById(R.id.add_group_member_activity_clear_image_view);
        mClearSearchView.setVisibility(View.GONE);
        mClearSearchView.setOnClickListener(v -> {
            mSearchEditText.setText("");
            mClearSearchView.setVisibility(View.GONE);
        });

        mSearchEditText = findViewById(R.id.add_group_member_activity_search_edit_text_view);
        Design.updateTextFont(mSearchEditText, Design.FONT_REGULAR34);
        mSearchEditText.setTextColor(Design.EDIT_TEXT_TEXT_COLOR);
        mSearchEditText.setHintTextColor(Design.GREY_COLOR);
        mSearchEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {

                if (s.length() > 0) {
                    mClearSearchView.setVisibility(View.VISIBLE);
                } else {
                    mClearSearchView.setVisibility(View.GONE);
                }
                mGroupService.findContactsByName(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        mSearchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                mSearchEditText.clearFocus();
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputMethodManager != null) {
                    inputMethodManager.hideSoftInputFromWindow(mSearchEditText.getWindowToken(), 0);
                }

                return true;
            }

            return false;
        });

        mActionSaveView = findViewById(R.id.add_group_member_activity_layout_save_view);
        mActionSaveView.setBackgroundColor(Design.WHITE_COLOR);

        layoutParams = mActionSaveView.getLayoutParams();
        layoutParams.height = Design.SELECTED_ITEM_VIEW_HEIGHT;
        mActionSaveView.setLayoutParams(layoutParams);

        // List of contacts the user has.
        LinearLayoutManager uiContactLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mUIContactRecyclerView = findViewById(R.id.add_group_member_activity_list_view);
        mUIContactRecyclerView.setLayoutManager(uiContactLinearLayoutManager);
        mUIContactRecyclerView.setItemViewCacheSize(Design.ITEM_LIST_CACHE_SIZE);
        mUIContactRecyclerView.setItemAnimator(null);
        OnContactTouchListener onContactTouchListener = new OnContactTouchListener(this, mUIContactRecyclerView, this);
        mUIContactRecyclerView.addOnItemTouchListener(onContactTouchListener);
        mUIContactRecyclerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        LinearLayoutManager selectedUIContactLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mSelectedUIContactRecyclerView = findViewById(R.id.add_group_member_activity_selected_list_view);
        mSelectedUIContactRecyclerView.setLayoutManager(selectedUIContactLinearLayoutManager);
        mSelectedUIContactRecyclerView.setItemViewCacheSize(Design.ITEM_LIST_CACHE_SIZE);
        mSelectedUIContactRecyclerView.setItemAnimator(null);

        mProgressBarView = findViewById(R.id.add_group_member_activity_progress_bar);

        // Setup the service after the view is initialized but before the adapters!
        mGroupService = new GroupService(this, getTwinmeContext(), this);
        mUIContactListAdapter = new UISelectableContactListAdapter(this, mGroupService, Design.ITEM_VIEW_HEIGHT, mUIContacts,
                R.layout.add_group_member_contact_item, R.id.add_group_member_activity_contact_item_name_view,
                R.id.add_group_member_activity_contact_item_avatar_view, R.id.add_group_member_activity_contact_item_certified_image_view, R.id.add_group_member_activity_contact_item_separator_view);
        mUIContactRecyclerView.setAdapter(mUIContactListAdapter);

        mSelectedUIContactListAdapter = new AddGroupMemberListAdapter(this, mGroupService, Design.SELECTED_ITEM_VIEW_HEIGHT, mUISelected,
                R.layout.add_group_member_selected_contact, 0, R.id.add_group_member_activity_contact_item_avatar_view, 0, 0, 0, 0, mFromCreateGroup, getTwinmeApplication().getDefaultAvatar());
        mSelectedUIContactRecyclerView.setAdapter(mSelectedUIContactListAdapter);

        mUIInitialized = true;
    }

    private void onSaveClicked() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSaveClicked");
        }

        Intent data = new Intent();
        data.putExtra(Intents.INTENT_CONTACT_SELECTION, toIntentString(mSelectedUIContactListAdapter.getContacts()));
        setResult(RESULT_OK, data);
        finish();
    }

    private void notifyContactListChanged() {
        if (DEBUG) {
            Log.d(LOG_TAG, "notifyContactListChanged");
        }

        if (mUIInitialized) {
            mUIContactListAdapter.notifyDataSetChanged();

            if (!mAddInvitationList && !mUIContacts.isEmpty() && !mInvitedContacts.isEmpty()) {
                mAddInvitationList = true;



                AtomicInteger avatarCounter = new AtomicInteger(mInvitedContacts.size());
                List<Originator> contacts = mUIContactListAdapter.getContacts();

                for (UUID contactId : mInvitedContacts) {
                    for (Originator originator : contacts) {
                        if (originator.getId().equals(contactId)) {
                            mGroupService.getImage(originator, (Bitmap avatar) -> {

                                mSelectedUIContactListAdapter.updateUIContact(originator, avatar);

                                if (avatarCounter.decrementAndGet() == 0) {
                                    notifyContactListChanged();
                                }
                            });
                            break;
                        }
                    }
                }
            }

            if (mUISelected.isEmpty() && !mFromCreateGroup) {
                mUIContactRecyclerView.requestLayout();
                mActionSaveView.setVisibility(View.GONE);
                hideSubTitle();
            } else {
                mActionSaveView.setVisibility(View.VISIBLE);

                ViewGroup.LayoutParams layoutParams = mSelectedUIContactRecyclerView.getLayoutParams();
                layoutParams.height = Design.SELECTED_ITEM_VIEW_HEIGHT;
                layoutParams.width = (mUISelected.size() + 1) * Design.SELECTED_ITEM_VIEW_HEIGHT;
                mSelectedUIContactRecyclerView.setLayoutParams(layoutParams);

                mSelectedUIContactRecyclerView.requestLayout();
                mSelectedUIContactListAdapter.notifyDataSetChanged();
                showSubTitle();

                int memberCount = mUISelected.size();
                if (mFromCreateGroup) {
                    memberCount++;
                }

                setSubTitle(memberCount + " / " + ConversationService.MAX_GROUP_MEMBERS);
            }
        }
    }

    private void postInitViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "postInitViews");
        }

        mUIPostInitialized = true;
    }

    /**
     * A click action is made on a contact: enable or disable the contact selection.
     *
     * @param recyclerView the contact list that holds the contact.
     * @param position     the contact position.
     * @return true if the click was handled.
     */
    @Override
    public boolean onUIContactClick(RecyclerView recyclerView, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUIContactClick position=" + position);
        }

        if (position >= 0 && position < mUIContacts.size()) {

            final UISelectableContact contact = mUIContacts.get(position);

            if (contact.isInvited()) {
                return false;
            }

            if (contact.isSelected()) {
                contact.setSelected(false);
                mUISelected.remove(contact);

            } else {
                boolean isMaxGroupMembers = false;
                if (mGroup != null) {
                    if (mUISelected.size() >= ConversationService.MAX_GROUP_MEMBERS) {
                        isMaxGroupMembers = true;
                    }
                } else if (mUISelected.size() + 1 >= ConversationService.MAX_GROUP_MEMBERS) {
                    isMaxGroupMembers = true;
                }

                if (isMaxGroupMembers) {
                    showAlertMessageView(R.id.add_group_member_activity_layout, getString(R.string.deleted_account_activity_warning), String.format(getString(R.string.application_group_limit_reached), ConversationService.MAX_GROUP_MEMBERS), false, null);
                    return false;
                }

                contact.setSelected(true);
                mUISelected.add(contact);
            }
            notifyContactListChanged();

            mSelectedUIContactRecyclerView.scrollToPosition(mUISelected.size() - 1);
            return true;

        } else {
            return false;
        }
    }

    @Override
    public boolean onUIContactFling(RecyclerView recyclerView, int position, OnContactTouchListener.Direction direction) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUIContactFling position=" + position + " direction=" + direction);
        }

        return false;
    }

    private boolean containsContact(List<UIContact> uiContacts, Contact contact) {

        boolean contains = false;
        for (UIContact uiContact : uiContacts) {

            if (uiContact.getId().equals(contact.getId())) {
                contains = true;
                break;
            }
        }

        return contains;
    }

    @Override
    public void setupDesign() {
        if (DEBUG) {
            Log.d(LOG_TAG, "setupDesign");
        }

        SELECTED_BOTTOM_MARGIN = (int) (DESIGN_SELECTED_BOTTOM_MARGIN * Design.HEIGHT_RATIO);
    }

    @Override
    public void updateFont() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateFont");
        }

        super.updateFont();

        if (!mUIInitialized) {
            return;
        }

        Design.updateTextFont(mSearchEditText, Design.FONT_REGULAR34);
    }

    @Override
    public void updateColor() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateColor");
        }

        super.updateColor();

        if (!mUIInitialized) {
            return;
        }

        mSearchEditText.setTextColor(Design.EDIT_TEXT_TEXT_COLOR);
        mSearchEditText.setHintTextColor(Design.GREY_COLOR);
        mSelectedUIContactListAdapter.notifyItemRangeChanged(0, mUISelected.size());
        mUIContactListAdapter.notifyItemRangeChanged(0, mUIContacts.size());
        mActionSaveView.setBackgroundColor(Design.WHITE_COLOR);
        mSearchView.setBackgroundColor(Design.TOOLBAR_COLOR);
        mUIContactRecyclerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        setStatusBarColor();
        setToolBar(R.id.add_group_member_activity_tool_bar);
        setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        applyInsets(R.id.add_group_member_activity_layout, R.id.add_group_member_activity_tool_bar, R.id.add_group_member_activity_list_view, Design.TOOLBAR_COLOR, false);
    }
}
