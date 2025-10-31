/*
 *  Copyright (c) 2022-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.callActivity;

import android.content.Context;
import android.content.Intent;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.util.Utils;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.services.CallParticipantService;
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

public class AddCallParticipantActivity extends AbstractTwinmeActivity implements OnContactTouchListener.OnContactObserver, CallParticipantService.Observer {
    private static final String LOG_TAG = "AddCallParticipant...";
    private static final boolean DEBUG = false;

    private static final float DESIGN_ITEM_VIEW_HEIGHT = 120f;
    private static final float DESIGN_SELECTED_BOTTOM_MARGIN = 40f;
    private static int SELECTED_BOTTOM_MARGIN;
    private static int ITEM_VIEW_HEIGHT;

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
    private final List<UUID> mParticipantsUUID = new ArrayList<>();

    private CallParticipantService mCallParticipantService;


    private int mMaxMemberCount = 0;

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

        mMaxMemberCount = intent.getIntExtra(Intents.INTENT_MAX_NUMBER_COUNT, 0);
        String selectedParticipants = intent.getStringExtra(Intents.INTENT_CONTACT_SELECTION);

        if (selectedParticipants != null) {
            String[] list = selectedParticipants.split(",");
            for (String item : list) {
                UUID participantPeerTwincodeOutboundId = Utils.UUIDFromString(item);
                mParticipantsUUID.add(participantPeerTwincodeOutboundId);
            }
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

        mCallParticipantService.getContacts();
    }

    //
    // Override Activity methods
    //

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        mCallParticipantService.dispose();

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
        inflater.inflate(R.menu.add_call_participant_menu, menu);

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

        mUIContacts.clear();

        // Setup the list of contacts and mark those that are selected.
        for (Contact contact : contacts) {
            if (contact.hasPeer()) {

                UISelectableContact uiContact = mUIContactListAdapter.updateUIContact(contact, null);

                boolean isContactInUISelected = containsContact(mUISelected, contact);
                if (mParticipantsUUID.contains(contact.getPeerTwincodeOutboundId()) && !isContactInUISelected) {
                    uiContact.setSelected(true);
                    uiContact.setInvited(true);
                    mUISelected.add(uiContact);
                } else if (isContactInUISelected) {
                    uiContact.setSelected(true);
                }
            }
        }

        hideProgressIndicator();
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
        setContentView(R.layout.add_call_participant_activity);

        setStatusBarColor();
        setToolBar(R.id.add_call_participant_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);
        setTitle(getString(R.string.add_call_participant_activity_title));
        setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        View searchView = findViewById(R.id.add_call_participant_activity_search_view);
        searchView.setBackgroundColor(Design.TOOLBAR_COLOR);

        ViewGroup.LayoutParams layoutParams = searchView.getLayoutParams();
        layoutParams.height = Design.SEARCH_VIEW_HEIGHT;

        mClearSearchView = findViewById(R.id.add_call_participant_activity_clear_image_view);
        mClearSearchView.setVisibility(View.GONE);
        mClearSearchView.setOnClickListener(v -> {
            mSearchEditText.setText("");
            mClearSearchView.setVisibility(View.GONE);
        });

        mSearchEditText = findViewById(R.id.add_call_participant_activity_search_edit_text_view);
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
                mCallParticipantService.findContactsByName(s.toString());
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

        mActionSaveView = findViewById(R.id.add_call_participant_activity_layout_save_view);
        mActionSaveView.setBackgroundColor(Design.WHITE_COLOR);

        layoutParams = mActionSaveView.getLayoutParams();
        layoutParams.height = Design.SELECTED_ITEM_VIEW_HEIGHT;
        mActionSaveView.setLayoutParams(layoutParams);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mActionSaveView.getLayoutParams();
        marginLayoutParams.bottomMargin = SELECTED_BOTTOM_MARGIN;

        // List of contacts the user has.
        LinearLayoutManager uiContactLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mUIContactRecyclerView = findViewById(R.id.add_call_participant_activity_list_view);
        mUIContactRecyclerView.setLayoutManager(uiContactLinearLayoutManager);
        mUIContactRecyclerView.setItemViewCacheSize(Design.ITEM_LIST_CACHE_SIZE);
        mUIContactRecyclerView.setItemAnimator(null);
        OnContactTouchListener onContactTouchListener = new OnContactTouchListener(this, mUIContactRecyclerView, this);
        mUIContactRecyclerView.addOnItemTouchListener(onContactTouchListener);

        LinearLayoutManager selectedUIContactLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mSelectedUIContactRecyclerView = findViewById(R.id.add_call_participant_activity_selected_list_view);
        mSelectedUIContactRecyclerView.setLayoutManager(selectedUIContactLinearLayoutManager);
        mSelectedUIContactRecyclerView.setItemViewCacheSize(Design.ITEM_LIST_CACHE_SIZE);
        mSelectedUIContactRecyclerView.setItemAnimator(null);

        mProgressBarView = findViewById(R.id.add_call_participant_activity_progress_bar);

        // Setup the service after the view is initialized but before the adapters!
        mCallParticipantService = new CallParticipantService(this, getTwinmeContext(), this);
        mUIContactListAdapter = new UISelectableContactListAdapter(this, mCallParticipantService, ITEM_VIEW_HEIGHT, mUIContacts,
                R.layout.add_group_member_contact_item, R.id.add_group_member_activity_contact_item_name_view,
                R.id.add_group_member_activity_contact_item_avatar_view,  R.id.add_group_member_activity_contact_item_certified_image_view, R.id.add_group_member_activity_contact_item_separator_view);
        mUIContactRecyclerView.setAdapter(mUIContactListAdapter);

        mSelectedUIContactListAdapter = new UIContactListAdapter(this, mCallParticipantService, Design.SELECTED_ITEM_VIEW_HEIGHT, mUISelected,
                R.layout.add_group_member_selected_contact, 0, R.id.add_group_member_activity_contact_item_avatar_view, 0, 0, 0, 0);
        mSelectedUIContactRecyclerView.setAdapter(mSelectedUIContactListAdapter);

        mUIInitialized = true;
    }

    private void onSaveClicked() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSaveClicked");
        }

        StringBuilder stringBuilder = new StringBuilder();
        List<UUID> contactsId = getNewSelectedContacts();
        for (UUID contactId : contactsId) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append(",");
            }
            stringBuilder.append(contactId.toString());
        }

        Intent data = new Intent();
        data.putExtra(Intents.INTENT_CONTACT_SELECTION, stringBuilder.toString());
        setResult(RESULT_OK, data);
        finish();
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
                List<UUID> contactsId = getNewSelectedContacts();
                int countParticipants = mParticipantsUUID.size() + contactsId.size() + 1;

                if (countParticipants >= mMaxMemberCount && mMaxMemberCount != 0) {
                    showAlertMessageView(R.id.add_call_participant_activity_layout, getString(R.string.deleted_account_activity_warning), String.format(getString(R.string.call_activity_max_participant_message), mMaxMemberCount), false, null);

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

    private List<UUID>getNewSelectedContacts() {

        List<UUID> selectedContacts = new ArrayList<>();
        for (UIContact uiContact : mUISelected) {
            Contact contact = (Contact) uiContact.getContact();
            if (!mParticipantsUUID.contains(contact.getPeerTwincodeOutboundId())) {
                selectedContacts.add(contact.getId());
            }
        }

        return selectedContacts;
    }

    @Override
    public void setupDesign() {
        if (DEBUG) {
            Log.d(LOG_TAG, "setupDesign");
        }

        SELECTED_BOTTOM_MARGIN = (int) (DESIGN_SELECTED_BOTTOM_MARGIN * Design.HEIGHT_RATIO);
        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
    }
}
