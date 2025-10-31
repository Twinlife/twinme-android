/*
 *  Copyright (c) 2019-2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui.spaces;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.ColorUtils;
import androidx.percentlayout.widget.PercentRelativeLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.util.Utils;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Originator;
import org.twinlife.twinme.models.Space;
import org.twinlife.twinme.services.SpaceService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.users.OnContactTouchListener;
import org.twinlife.twinme.ui.users.UIContact;
import org.twinlife.twinme.ui.users.UIContactListAdapter;
import org.twinlife.twinme.ui.users.UIMoveContact;
import org.twinlife.twinme.ui.users.UIMoveContactListAdapter;
import org.twinlife.twinme.utils.AbstractConfirmView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ContactsSpaceActivity extends AbstractSpaceActivity implements OnContactTouchListener.OnContactObserver {
    private static final String LOG_TAG = "ContactsSpaceActivity";
    private static final boolean DEBUG = false;

    protected static final float DESIGN_ITEM_VIEW_HEIGHT = 124f;
    protected static final float DESIGN_SELECTED_ITEM_VIEW_HEIGHT = 116f;
    protected static int ITEM_VIEW_HEIGHT;
    protected static int SELECTED_ITEM_VIEW_HEIGHT;

    private boolean mUIInitialized = false;
    private boolean mUIPostInitialized = false;
    private UIMoveContactListAdapter mUIContactListAdapter;
    private UIContactListAdapter mSelectedUIContactListAdapter;
    private View mActionSaveView;
    private View mClearSearchView;
    private View mContentSearchView;
    private EditText mSearchEditText;
    private RecyclerView mSelectedUIContactRecyclerView;
    private final List<UIMoveContact> mUIContacts = new ArrayList<>();
    private final List<UIContact> mUISelected = new ArrayList<>();
    private String mSelected;
    private SpaceService mSpaceService;

    private Space mSpace;
    private UUID mSpaceId;

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

        // Get the current selected contacts and setup the UI in onGetContacts when we know all of them.
        mSelected = intent.getStringExtra(Intents.INTENT_CONTACT_SELECTION);

        mSpaceId = Utils.UUIDFromString(intent.getStringExtra(Intents.INTENT_SPACE_ID));
        mSpaceService = new SpaceService(this, getTwinmeContext(), this, mSpaceId);

        initViews();

        mSpaceService.getAllContacts();
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

        mSpaceService.dispose();

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

    //
    // Implement SpaceService.Observer methods
    //

    @Override
    public void onGetContacts(@NonNull List<Contact> contacts) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContacts: contacts=" + contacts);
        }

        // Get the current selection of contacts (passed within the intent parameter).
        List<Contact> selected;
        if (mSelected != null) {
            selected = fromIntentString(contacts, mSelected);
        } else {
            selected = new ArrayList<>();
        }

        for (UIContact uiContact : mUISelected) {
            Contact contact = (Contact) uiContact.getContact();
            if (contact != null) {
                selected.add(contact);
            }
        }

        mUIContacts.clear();

        if (contacts.isEmpty()) {
            hideProgressIndicator();
            notifyContactListChanged();
            return;
        }

        AtomicInteger avatarCounter = new AtomicInteger(contacts.size());
        // Setup the list of contacts and mark those that are selected.
        for (Contact contact : contacts) {
            mSpaceService.getImage(contact, (Bitmap avatar) -> {
                UIMoveContact uiContact = mUIContactListAdapter.updateUIContact(contact, avatar);

                boolean isContactInUISelected = containsContact(mUISelected, contact);
                if (selected.contains(contact) && !isContactInUISelected) {
                    uiContact.setSelected(true);
                    mUISelected.add(uiContact);
                } else if (isContactInUISelected) {
                    uiContact.setSelected(true);
                }

                if (mSpace != null && contact.getSpace() != null && contact.getSpace().getId() == mSpace.getId()) {
                    uiContact.setSelected(true);
                    uiContact.setCanMove(false);
                } else if (mSpace == null) {
                    uiContact.setSelected(false);
                    uiContact.setCanMove(true);
                }

                if (avatarCounter.decrementAndGet() == 0) {
                    hideProgressIndicator();
                    notifyContactListChanged();
                }
            });
        }
    }

    @Override
    public void onGetSpace(@NonNull Space space, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetSpace: space=" + space);
        }

        if (space.getId().equals(mSpaceId)) {
            mSpace = space;
        }
    }

    @Override
    public void onGetSpaceNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetSpaceNotFound");
        }

        // Activity was called with a spaceId that is no longer valid.
        finish();
    }

    @Override
    public void onUpdateSpace(@NonNull Space space) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateSpace: space=" + space);
        }

        finish();
    }

    //
    // Override TwinmeActivityImpl methods
    //


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
            sb.append(contact.getId().toString());
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
    public static <E extends Originator> List<E> fromIntentString(List<E> allContacts, String selection) {
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
    // Private methods
    //

    @SuppressLint("WrongConstant")
    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.contacts_space_activity);

        setStatusBarColor();
        setToolBar(R.id.contacts_space_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);
        setTitle(getString(R.string.contacts_fragment_title));

        applyInsets(R.id.contacts_space_activity_layout, R.id.contacts_space_activity_tool_bar, R.id.contacts_space_activity_layout_save_view, Design.TOOLBAR_COLOR, false);

        mActionSaveView = findViewById(R.id.contacts_space_activity_layout_save_view);
        mActionSaveView.setBackgroundColor(Design.WHITE_COLOR);

        ViewGroup.LayoutParams layoutParams = mActionSaveView.getLayoutParams();
        layoutParams.height = SELECTED_ITEM_VIEW_HEIGHT;
        mActionSaveView.setLayoutParams(layoutParams);

        TextView saveView = findViewById(R.id.contacts_space_activity_add_title);
        saveView.setTypeface(Design.FONT_REGULAR36.typeface);
        saveView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_REGULAR36.size);
        saveView.setTextColor(Design.getMainStyle());
        saveView.setOnClickListener(v -> onMoveClicked());

        View saveClickableView = findViewById(R.id.contacts_space_activity_add_title_clickable_view);
        saveClickableView.setOnClickListener(v -> onMoveClicked());

        View searchView = findViewById(R.id.contacts_space_activity_search_view);
        searchView.setBackgroundColor(Design.TOOLBAR_COLOR);

        layoutParams = searchView.getLayoutParams();
        layoutParams.height = Design.SEARCH_VIEW_HEIGHT;

        mContentSearchView = findViewById(R.id.contacts_space_activity_search_content_view);
        mContentSearchView.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.grey_search_rounded, getTheme()));

        mClearSearchView = findViewById(R.id.contacts_space_activity_clear_image_view);
        mClearSearchView.setVisibility(View.GONE);
        mClearSearchView.setOnClickListener(v -> {
            mSearchEditText.setText("");
            mClearSearchView.setVisibility(View.GONE);
        });

        mSearchEditText = findViewById(R.id.contacts_space_activity_search_edit_text_view);
        mSearchEditText.setTypeface(Design.FONT_REGULAR34.typeface);
        mSearchEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_REGULAR34.size);
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
                mSpaceService.findContactsByName(s.toString());
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

        // List of contacts the user has.
        mUIContactListAdapter = new UIMoveContactListAdapter(this, mSpaceService, ITEM_VIEW_HEIGHT, mUIContacts,
                R.layout.contacts_space_activity_contact_item, R.id.contacts_space_activity_contact_item_name_view,
                R.id.contacts_space_activity_contact_item_avatar_view, R.id.contacts_space_activity_contact_item_certified_image_view, R.id.contacts_space_activity_contact_item_separator_view);
        LinearLayoutManager uiContactLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        RecyclerView uiContactRecyclerView = findViewById(R.id.contacts_space_activity_list_view);
        uiContactRecyclerView.setLayoutManager(uiContactLinearLayoutManager);
        uiContactRecyclerView.setAdapter(mUIContactListAdapter);
        uiContactRecyclerView.setItemViewCacheSize(Design.ITEM_LIST_CACHE_SIZE);
        uiContactRecyclerView.setItemAnimator(null);
        uiContactRecyclerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        OnContactTouchListener onContactTouchListener = new OnContactTouchListener(this, uiContactRecyclerView, this);
        uiContactRecyclerView.addOnItemTouchListener(onContactTouchListener);

        mSelectedUIContactListAdapter = new UIContactListAdapter(this, mSpaceService, SELECTED_ITEM_VIEW_HEIGHT, mUISelected,
                R.layout.add_group_member_selected_contact, 0, R.id.add_group_member_activity_contact_item_avatar_view, 0, 0, 0, 0);
        LinearLayoutManager selectedUIContactLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mSelectedUIContactRecyclerView = findViewById(R.id.contacts_space_activity_selected_list_view);
        mSelectedUIContactRecyclerView.setLayoutManager(selectedUIContactLinearLayoutManager);
        mSelectedUIContactRecyclerView.setAdapter(mSelectedUIContactListAdapter);
        mSelectedUIContactRecyclerView.setItemViewCacheSize(Design.ITEM_LIST_CACHE_SIZE);
        mSelectedUIContactRecyclerView.setItemAnimator(null);

        mProgressBarView = findViewById(R.id.contacts_space_activity_progress_bar);

        mUIInitialized = true;
    }

    private void onMoveClicked() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onMoveClicked");
        }

        mSpaceService.getSpaceImage(mSpace, (Bitmap avatar) -> {
            PercentRelativeLayout percentRelativeLayout = findViewById(R.id.contacts_space_activity_layout);

            SpaceActionConfirmView spaceActionConfirmView = new SpaceActionConfirmView(this, null);
            PercentRelativeLayout.LayoutParams layoutParams = new PercentRelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            spaceActionConfirmView.setLayoutParams(layoutParams);

            spaceActionConfirmView.setSpaceName(mSpace.getSpaceSettings().getName(), mSpace.getSpaceSettings().getStyle());
            spaceActionConfirmView.setAvatar(avatar, false);
            spaceActionConfirmView.setIconTintColor(Color.WHITE);
            spaceActionConfirmView.setIcon(R.drawable.move_contacts_icon);
            spaceActionConfirmView.setTitle(mSpace.getSpaceSettings().getName());
            spaceActionConfirmView.setMessage(getString(R.string.contact_space_activity_move_message));
            spaceActionConfirmView.setConfirmTitle(getString(R.string.contact_space_activity_move_title));
            spaceActionConfirmView.setCancelTitle(getString(R.string.application_cancel));

            AbstractConfirmView.Observer observer = new AbstractConfirmView.Observer() {
                @Override
                public void onConfirmClick() {
                    if (mSpace != null) {
                        List<Contact> contacts = new ArrayList<>();
                        for (UIContact uiContact : mUISelected) {
                            contacts.add((Contact) uiContact.getContact());
                        }
                        mSpaceService.moveContactsInSpace(contacts, mSpace);
                    } else {
                        Intent data = new Intent();
                        data.putExtra(Intents.INTENT_CONTACT_SELECTION, toIntentString(mSelectedUIContactListAdapter.getContacts()));
                        setResult(RESULT_OK, data);
                        finish();
                    }
                    spaceActionConfirmView.animationCloseConfirmView();
                }

                @Override
                public void onCancelClick() {
                    spaceActionConfirmView.animationCloseConfirmView();
                }

                @Override
                public void onDismissClick() {
                    spaceActionConfirmView.animationCloseConfirmView();
                }

                @Override
                public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                    percentRelativeLayout.removeView(spaceActionConfirmView);
                    setStatusBarColor();
                }
            };
            spaceActionConfirmView.setObserver(observer);

            percentRelativeLayout.addView(spaceActionConfirmView);
            spaceActionConfirmView.show();

            int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
            setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
        });
    }

    private void notifyContactListChanged() {
        if (DEBUG) {
            Log.d(LOG_TAG, "notifyContactListChanged");
        }

        if (mUIInitialized) {
            mUIContactListAdapter.notifyDataSetChanged();

            if (mUISelected.isEmpty()) {
                mActionSaveView.setVisibility(View.GONE);
            } else {
                mActionSaveView.setVisibility(View.VISIBLE);

                ViewGroup.LayoutParams layoutParams = mSelectedUIContactRecyclerView.getLayoutParams();
                layoutParams.height = SELECTED_ITEM_VIEW_HEIGHT;
                layoutParams.width = (mUISelected.size() + 1) * SELECTED_ITEM_VIEW_HEIGHT;
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

            final UIMoveContact contact = mUIContacts.get(position);
            if (!contact.canMove()) {
                return false;
            }

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
    public void updateColor() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateColor");
        }

        mSearchEditText.setTextColor(Design.EDIT_TEXT_TEXT_COLOR);
        mSearchEditText.setHintTextColor(Design.GREY_COLOR);

        mContentSearchView.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.grey_search_rounded, getTheme()));
    }

    @Override
    public void setupDesign() {
        if (DEBUG) {
            Log.d(LOG_TAG, "setupDesign");
        }

        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
        SELECTED_ITEM_VIEW_HEIGHT = (int) (DESIGN_SELECTED_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
    }
}