/*
 *  Copyright (c) 2020-2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.newConversationActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Space;
import org.twinlife.twinme.services.ContactsService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.conversationActivity.ConversationActivity;
import org.twinlife.twinme.ui.groups.CreateGroupActivity;
import org.twinlife.twinme.ui.users.OnContactTouchListener;
import org.twinlife.twinme.ui.users.UIContact;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Activity controller called to share content (file, text)
 */

public class NewConversationActivity extends AbstractTwinmeActivity implements ContactsService.Observer, OnContactTouchListener.OnContactObserver {
    private static final String LOG_TAG = "NewConversationActivity";
    private static final boolean DEBUG = false;

    private static final int CREATE_GROUP = 1;

    private boolean mUIInitialized = false;
    private boolean mUIPostInitialized = false;
    private NewConversationListAdapter mNewConversationListAdapter;
    private EditText mSearchEditText;
    private View mClearSearchView;
    private final List<UIContact> mUIContacts = new ArrayList<>();

    private ContactsService mContactsService;

    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

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
    }

    //
    // Override Activity methods
    //

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        mContactsService.dispose();

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onActivityResult requestCode=" + requestCode + " resultCode=" + resultCode + " data=" + data);
        }

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CREATE_GROUP && resultCode == RESULT_OK) {
            finish();
        }
    }

    //
    // Implement ContactsService.Observer methods
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

        mNewConversationListAdapter.setContacts(contacts);

        if (mUIInitialized) {
            mNewConversationListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onCreateContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateContact: contact=" + contact);
        }

        mNewConversationListAdapter.updateUIContact(contact, avatar);

        if (mUIInitialized) {
            mNewConversationListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onUpdateContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateContact: contact=" + contact);
        }

        mNewConversationListAdapter.updateUIContact(contact, avatar);

        if (mUIInitialized) {
            mNewConversationListAdapter.notifyDataSetChanged();
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
    public void onDeleteContact(@NonNull UUID contactId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteContact: contactId=" + contactId);
        }

        mNewConversationListAdapter.removeUIContact(contactId);

        if (mUIInitialized) {
            mNewConversationListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onSetCurrentSpace(@NonNull Space space) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSetCurrentSpace: space=" + space);
        }

    }

    //
    // Implement OnContactTouchListener.OnContactObserver methods
    //

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

        if (position >= 2 && position - 2 < mUIContacts.size()) {
            UIContact uiContact = mUIContacts.get(position - 2);
            if (uiContact.getContact().hasPeer()) {
                startActivity(ConversationActivity.class, Intents.INTENT_CONTACT_ID, uiContact.getContact().getId());
                finish();
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean onUIContactFling(RecyclerView recyclerView, int position, OnContactTouchListener.Direction direction) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUIContactFling position=" + position + " direction=" + direction);
        }

        return false;
    }

    public void onCreateGroupClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateGroupClick");
        }

        Intent intent = new Intent();
        intent.setClass(this, CreateGroupActivity.class);
        startActivityForResult(intent, CREATE_GROUP);
    }

    //
    // Private methods
    //

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.new_conversation_activity);

        setStatusBarColor();
        setToolBar(R.id.new_conversation_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);
        setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        applyInsets(R.id.new_conversation_activity_layout, R.id.new_conversation_activity_tool_bar, R.id.new_conversation_activity_contact_list_view, Design.TOOLBAR_COLOR, false);

        setTitle(getString(R.string.conversations_fragment_title));

        View searchView = findViewById(R.id.new_conversation_activity_search_view);
        searchView.setBackgroundColor(Design.TOOLBAR_COLOR);

        ViewGroup.LayoutParams layoutParams = searchView.getLayoutParams();
        layoutParams.height = Design.SEARCH_VIEW_HEIGHT;

        mClearSearchView = findViewById(R.id.new_conversation_activity_clear_image_view);
        mClearSearchView.setVisibility(View.GONE);
        mClearSearchView.setOnClickListener(v -> {
            mSearchEditText.setText("");
            mClearSearchView.setVisibility(View.GONE);
        });

        mSearchEditText = findViewById(R.id.new_conversation_activity_search_edit_text_view);
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
                mContactsService.findContactsByName(s.toString());
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

        LinearLayoutManager uiContactLinearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        RecyclerView newConversationRecyclerView = findViewById(R.id.new_conversation_activity_contact_list_view);
        newConversationRecyclerView.setLayoutManager(uiContactLinearLayoutManager);
        newConversationRecyclerView.setItemViewCacheSize(Design.ITEM_LIST_CACHE_SIZE);
        newConversationRecyclerView.setItemAnimator(null);
        newConversationRecyclerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        OnContactTouchListener onTouchContactListener = new OnContactTouchListener(this, newConversationRecyclerView, this);
        newConversationRecyclerView.addOnItemTouchListener(onTouchContactListener);

        mProgressBarView = findViewById(R.id.share_activity_progress_bar);

        // Setup the service after the view is initialized but before the adapter!
        mContactsService = new ContactsService(this, getTwinmeContext(), this);

        mNewConversationListAdapter = new NewConversationListAdapter(this, mContactsService, Design.ITEM_VIEW_HEIGHT, mUIContacts, R.layout.contacts_fragment_contact_item, R.id.contacts_fragment_contact_item_name_view, R.id.contacts_fragment_contact_item_avatar_view, R.id.contacts_fragment_contact_item_tag_view, R.id.contacts_fragment_contact_item_tag_title_view, R.id.contacts_fragment_contact_item_certified_image_view, R.id.contacts_fragment_contact_item_separator_view);
        newConversationRecyclerView.setAdapter(mNewConversationListAdapter);

        mUIInitialized = true;
    }

    private void postInitViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "postInitViews");
        }

        mUIPostInitialized = true;
    }
}
