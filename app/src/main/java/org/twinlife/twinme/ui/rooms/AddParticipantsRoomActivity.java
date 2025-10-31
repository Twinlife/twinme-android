/*
 *  Copyright (c) 2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.rooms;

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
import org.twinlife.twinlife.TwincodeURI;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.services.InvitationRoomService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.users.OnContactTouchListener;
import org.twinlife.twinme.ui.users.UIContact;
import org.twinlife.twinme.ui.users.UIContactListAdapter;
import org.twinlife.twinme.ui.users.UISelectableContact;
import org.twinlife.twinme.ui.users.UISelectableContactListAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AddParticipantsRoomActivity extends AbstractTwinmeActivity implements OnContactTouchListener.OnContactObserver, InvitationRoomService.Observer {
    private static final String LOG_TAG = "AddParticipantsRoom...";
    private static final boolean DEBUG = false;

    protected static final float DESIGN_ITEM_VIEW_HEIGHT = 120f;
    private static final float DESIGN_SELECTED_BOTTOM_MARGIN = 40f;
    private static int SELECTED_BOTTOM_MARGIN;
    protected static int ITEM_VIEW_HEIGHT;

    private boolean mUIInitialized = false;
    private boolean mUIPostInitialized = false;
    private UISelectableContactListAdapter mUIContactListAdapter;
    private UIContactListAdapter mSelectedUIContactListAdapter;
    private View mActionSaveView;
    private RecyclerView mSelectedUIContactRecyclerView;
    private RecyclerView mUIContactRecyclerView;
    private EditText mSearchEditText;
    private View mClearSearchView;
    private final List<UISelectableContact> mUIContacts = new ArrayList<>();
    private final List<UIContact> mUISelected = new ArrayList<>();
    @Nullable
    private Contact mRoom;
    private UUID mRoomId;

    private InvitationRoomService mInvitationRoomService;

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
        String value = intent.getStringExtra(Intents.INTENT_CONTACT_ID);
        if (value != null) {
            mRoomId = UUID.fromString(value);
        } else {
            finish();
        }

        initViews();
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

        mInvitationRoomService.getContacts();
    }

    //
    // Override Activity methods
    //

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        mInvitationRoomService.dispose();

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
        inflater.inflate(R.menu.invitation_room_menu, menu);

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

    @Override
    public void onGetContacts(@NonNull List<Contact> contacts) {

        mUIContacts.clear();

        // Setup the list of contacts and mark those that are selected.
        for (Contact contact : contacts) {
            if (contact.hasPeer()) {
                UISelectableContact uiContact = mUIContactListAdapter.updateUIContact(contact, null);
                if (containsContact(mUISelected, contact)) {
                    uiContact.setSelected(true);
                }
            }
        }

        hideProgressIndicator();
        notifyContactListChanged();
    }

    @Override
    public void onGetContact(@NonNull Contact contact, @Nullable Bitmap avatar) {

        if (contact.getId().equals(mRoomId)) {
            mRoom = contact;
        }
    }

    @Override
    public void onGetContactNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContactNotFound");
        }
    }

    @Override
    public void onDeleteContact(@NonNull UUID contactId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteContact");
        }
    }

    @Override
    public void onGetTwincodeURI(@NonNull TwincodeURI uri) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetTwincodeURI uri=" + uri);
        }
    }

    @Override
    public void onUpdateImage(@NonNull Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateImage");
        }
    }

    @Override
    public void onUpdateContact(@NonNull Contact contact, @Nullable Bitmap avatar) {

    }

    @Override
    public void onSendTwincodeToContacts() {

        finish();
    }

    //
    // Private methods
    //

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.add_participants_room_activity);

        setStatusBarColor();
        setToolBar(R.id.add_participants_room_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);
        setTitle(getString(R.string.contacts_fragment_invite_contact_title));
        setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        applyInsets(R.id.add_participants_room_activity_layout, R.id.add_participants_room_activity_tool_bar, R.id.add_participants_room_activity_list_view, Design.TOOLBAR_COLOR, false);

        View searchView = findViewById(R.id.add_participants_room_activity_search_view);
        searchView.setBackgroundColor(Design.TOOLBAR_COLOR);

        ViewGroup.LayoutParams layoutParams = searchView.getLayoutParams();
        layoutParams.height = Design.SEARCH_VIEW_HEIGHT;

        mClearSearchView = findViewById(R.id.add_participants_room_activity_clear_image_view);
        mClearSearchView.setVisibility(View.GONE);
        mClearSearchView.setOnClickListener(v -> {
            mSearchEditText.setText("");
            mClearSearchView.setVisibility(View.GONE);
        });

        mSearchEditText = findViewById(R.id.add_participants_room_activity_search_edit_text_view);
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

                mInvitationRoomService.findContactsByName(s.toString());
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

        mActionSaveView = findViewById(R.id.add_participants_room_activity_layout_save_view);
        mActionSaveView.setBackgroundColor(Design.WHITE_COLOR);

        layoutParams = mActionSaveView.getLayoutParams();
        layoutParams.height = Design.SELECTED_ITEM_VIEW_HEIGHT;
        mActionSaveView.setLayoutParams(layoutParams);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mActionSaveView.getLayoutParams();
        marginLayoutParams.bottomMargin = SELECTED_BOTTOM_MARGIN;

        // List of contacts the user has.
        LinearLayoutManager uiContactLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mUIContactRecyclerView = findViewById(R.id.add_participants_room_activity_list_view);
        mUIContactRecyclerView.setLayoutManager(uiContactLinearLayoutManager);
        mUIContactRecyclerView.setItemViewCacheSize(Design.ITEM_LIST_CACHE_SIZE);
        mUIContactRecyclerView.setItemAnimator(null);
        OnContactTouchListener onContactTouchListener = new OnContactTouchListener(this, mUIContactRecyclerView, this);
        mUIContactRecyclerView.addOnItemTouchListener(onContactTouchListener);
        mUIContactRecyclerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        LinearLayoutManager selectedUIContactLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mSelectedUIContactRecyclerView = findViewById(R.id.add_participants_room_activity_selected_list_view);
        mSelectedUIContactRecyclerView.setLayoutManager(selectedUIContactLinearLayoutManager);
        mSelectedUIContactRecyclerView.setItemViewCacheSize(Design.ITEM_LIST_CACHE_SIZE);
        mSelectedUIContactRecyclerView.setItemAnimator(null);

        mInvitationRoomService = new InvitationRoomService(this, getTwinmeContext(), this, mRoomId);

        mUIContactListAdapter = new UISelectableContactListAdapter(this, mInvitationRoomService, ITEM_VIEW_HEIGHT, mUIContacts,
                R.layout.add_group_member_contact_item, R.id.add_group_member_activity_contact_item_name_view,
                R.id.add_group_member_activity_contact_item_avatar_view, R.id.add_group_member_activity_contact_item_certified_image_view, R.id.add_group_member_activity_contact_item_separator_view);
        mUIContactRecyclerView.setAdapter(mUIContactListAdapter);

        mSelectedUIContactListAdapter = new UIContactListAdapter(this, mInvitationRoomService, Design.SELECTED_ITEM_VIEW_HEIGHT, mUISelected,
                R.layout.add_group_member_selected_contact, 0, R.id.add_group_member_activity_contact_item_avatar_view, 0, 0, 0, 0);
        mSelectedUIContactRecyclerView.setAdapter(mSelectedUIContactListAdapter);

        mProgressBarView = findViewById(R.id.add_group_member_activity_progress_bar);

        mUIInitialized = true;
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

            if (contact.isSelected()) {
                contact.setSelected(false);
                mUISelected.remove(contact);
            } else {
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


    private void onSaveClicked() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSaveClicked");
        }

        List<Contact> contactsToInvite = new ArrayList<>();
        for (UIContact uiContact : mUISelected) {
            contactsToInvite.add((Contact) uiContact.getContact());
        }

        mInvitationRoomService.inviteContactToRoom(contactsToInvite, mRoom);
    }

    private void notifyContactListChanged() {
        if (DEBUG) {
            Log.d(LOG_TAG, "notifyContactListChanged");
        }

        if (mUIInitialized) {
            mUIContactListAdapter.notifyDataSetChanged();

            if (mUISelected.isEmpty()) {
                mUIContactRecyclerView.requestLayout();
                mActionSaveView.setVisibility(View.GONE);
            } else {
                mActionSaveView.setVisibility(View.VISIBLE);

                ViewGroup.LayoutParams layoutParams = mSelectedUIContactRecyclerView.getLayoutParams();
                layoutParams.height = Design.SELECTED_ITEM_VIEW_HEIGHT;
                layoutParams.width = (mUISelected.size() + 1) * Design.SELECTED_ITEM_VIEW_HEIGHT;
                mSelectedUIContactRecyclerView.setLayoutParams(layoutParams);

                mSelectedUIContactRecyclerView.requestLayout();
                mSelectedUIContactListAdapter.notifyDataSetChanged();
            }
        }
    }

    private void postInitViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "postInitViews");
        }

        mUIPostInitialized = true;
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

        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
        SELECTED_BOTTOM_MARGIN = (int) (DESIGN_SELECTED_BOTTOM_MARGIN * Design.HEIGHT_RATIO);
    }
}