/*
 *  Copyright (c) 2019-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui.mainActivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.MenuProvider;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Profile;
import org.twinlife.twinme.models.Space;
import org.twinlife.twinme.services.ContactsService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.skin.DisplayMode;
import org.twinlife.twinme.ui.AddContactActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.Settings;
import org.twinlife.twinme.ui.accountMigrationActivity.AccountMigrationScannerActivity;
import org.twinlife.twinme.ui.contacts.MenuAddContactView;
import org.twinlife.twinme.ui.profiles.AddProfileActivity;
import org.twinlife.twinme.ui.users.OnContactTouchListener;
import org.twinlife.twinme.ui.users.UIContact;
import org.twinlife.twinme.ui.users.UIContactListAdapter;
import org.twinlife.twinme.utils.UIMenuSelectAction;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ContactsFragment extends TabbarFragment implements OnContactTouchListener.OnContactObserver, ContactsService.Observer {
    private static final String LOG_TAG = "ContactsFragment";
    private static final boolean DEBUG = false;

    private static final float DESIGN_IMAGE_VIEW_HEIGHT = 480f;
    private static final float DESIGN_NO_RESULT_IMAGE_VIEW_HEIGHT = 260f;
    private static final float DESIGN_NO_RESULT_IMAGE_VIEW_WIDTH = 380f;

    private boolean mUIInitialized = false;

    private SearchView mSearchView;
    private RecyclerView mUIContactRecyclerView;
    private ImageView mNoContactImageView;
    private TextView mNoContactTextView;
    private UIContactListAdapter mUIContactListAdapter;
    private View mInviteContactView;
    private View mTransferView;
    private View mNoResultFoundView;
    private TextView mNoResultFoundTitleView;

    private final List<UIContact> mUIContacts = new ArrayList<>();

    private boolean mOnGetContactDone = false;
    private String mLastSearch = "";
    private ContactsService mContactsService;

    @Nullable
    private Menu mMenu;

    // Default constructor is required by Android for proper activity restoration.
    public ContactsFragment() {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateView: inflater=" + inflater + " container=" + container + " savedInstanceState=" + savedInstanceState);
        }

        View view = inflater.inflate(R.layout.contacts_fragment, container, false);
        initViews(view);

        addMenu();

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

        updateColor();
        updateFont();

        if (mSearchView != null && !mSearchView.isIconified()) {
            mLastSearch = mSearchView.getQuery().toString();
        } else {
            notifyContactsListChanged();
        }
    }

    @Override
    public void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        mContactsService.dispose();

        if (mSearchView != null) {
            mSearchView.setOnQueryTextListener(null);
        }

        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateOptionsMenu: menu=" + menu + " inflater=" + inflater);
        }

        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.contacts_menu, menu);

        mMenu = menu;

        MenuItem menuItem = menu.findItem(R.id.add_contact_action);

        ImageView imageView = (ImageView) menuItem.getActionView();

        if (imageView != null) {
            imageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.action_bar_add_contact, null));
            imageView.setPadding(Design.TOOLBAR_IMAGE_ITEM_PADDING, 0, Design.TOOLBAR_IMAGE_ITEM_PADDING, 0);
            imageView.setOnClickListener(view -> onAddContactClick());
            imageView.setContentDescription(getString(R.string.add_contact_activity_title));
        }

        MenuItem searchItem = menu.findItem(R.id.search_action);
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(@NonNull MenuItem menuItem) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(@NonNull MenuItem menuItem) {

                if (mUIContactListAdapter != null) {
                    mUIContactListAdapter.setAddContact(true);
                    mUIContactListAdapter.notifyDataSetChanged();
                }

                return true;
            }
        });

        if (mSearchView != null) {
            mSearchView.setOnQueryTextListener(null);
        }

        mSearchView = (SearchView) searchItem.getActionView();

        if (mSearchView != null) {
            mSearchView.setQueryHint(getString(R.string.application_search_hint));

            SearchView.OnQueryTextListener onQueryTextListener = new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    findContactByName(newText);
                    return false;
                }
            };

            mSearchView.setOnQueryTextListener(onQueryTextListener);
        }

        if (!mLastSearch.isEmpty()) {
            searchItem.expandActionView();
            mSearchView.setQuery(mLastSearch, false);
            mLastSearch = "";
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

        mOnGetContactDone = true;

        mUIContactListAdapter.setContacts(contacts);

        notifyContactsListChanged();
    }

    @Override
    public void onCreateContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateContact: contact=" + contact);
        }

        mUIContactListAdapter.updateUIContact(contact, avatar);

        notifyContactsListChanged();
    }

    @Override
    public void onGetContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContact: contact=" + contact);
        }
    }

    @Override
    public void onUpdateContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateContact: contact=" + contact);
        }

        mUIContactListAdapter.updateUIContact(contact, avatar);

        notifyContactsListChanged();
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

        mUIContactListAdapter.removeUIContact(contactId);

        notifyContactsListChanged();
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

        if (position >= 2 && (position - 2) < mUIContacts.size()) {

            InputMethodManager inputMethodManager = (InputMethodManager) mTwinmeActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null && mSearchView != null) {
                mSearchView.setFocusable(false);
                inputMethodManager.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
            }

            final UIContact uiContact = mUIContacts.get(position - 2);

            showContactActivity(uiContact.getContact());
            return true;
        }

        return false;
    }

    /**
     * A click action is made on add contact section.
     *
     * @param recyclerView the contact list that holds the contact.
     * @return True if the event was handled.
     */
    @Override
    public boolean onAddContactClick(RecyclerView recyclerView) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onAddContactClick recyclerView=" + recyclerView);
        }

        onAddContactClick();

        return true;
    }

    @Override
    public boolean onUIContactFling(RecyclerView recyclerView, int position, OnContactTouchListener.Direction direction) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUIContactFling position=" + position + " direction=" + direction);
        }

        return false;
    }

    //
    // Private methods
    //

    private void initViews(View view) {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews: view=" + view);
        }

        // Fragment was detached.
        if (mTwinmeActivity == null) {
            return;
        }

        LinearLayoutManager uiContactLinearLayoutManager = new LinearLayoutManager(mTwinmeActivity, RecyclerView.VERTICAL, false);
        mUIContactRecyclerView = view.findViewById(R.id.contacts_fragment_list_view);
        mUIContactRecyclerView.setLayoutManager(uiContactLinearLayoutManager);
        mUIContactRecyclerView.setItemViewCacheSize(Design.ITEM_LIST_CACHE_SIZE);
        mUIContactRecyclerView.setItemAnimator(null);
        OnContactTouchListener onContactTouchListener = new OnContactTouchListener(mTwinmeActivity, mUIContactRecyclerView, this);
        mUIContactRecyclerView.addOnItemTouchListener(onContactTouchListener);

        mNoContactImageView = view.findViewById(R.id.contacts_fragment_no_contact_image_view);

        ViewGroup.LayoutParams layoutParams = mNoContactImageView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_IMAGE_VIEW_HEIGHT * Design.HEIGHT_RATIO);

        mNoContactTextView = view.findViewById(R.id.contacts_fragment_no_contact_text_view);
        Design.updateTextFont(mNoContactTextView, Design.FONT_MEDIUM34);
        mNoContactTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mInviteContactView = view.findViewById(R.id.contacts_fragment_invite_contact_view);
        mInviteContactView.setOnClickListener(v -> onAddContactClick());

        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable saveViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        saveViewBackground.getPaint().setColor(Design.getMainStyle());
        mInviteContactView.setBackground(saveViewBackground);

        layoutParams = mInviteContactView.getLayoutParams();
        layoutParams.height = Design.BUTTON_HEIGHT;

        TextView inviteContactTextView = view.findViewById(R.id.contacts_fragment_invite_contact_title_view);
        Design.updateTextFont(inviteContactTextView, Design.FONT_MEDIUM34);
        inviteContactTextView.setTextColor(Color.WHITE);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) inviteContactTextView.getLayoutParams();
        marginLayoutParams.leftMargin = Design.BUTTON_MARGIN;
        marginLayoutParams.rightMargin = Design.BUTTON_MARGIN;

        // Setup the service after the view is initialized but before the adapter.
        mContactsService = new ContactsService(mTwinmeActivity, mTwinmeActivity.getTwinmeContext(), this);

        mUIContactListAdapter = new UIContactListAdapter(mTwinmeActivity, mContactsService, Design.ITEM_VIEW_HEIGHT, mUIContacts, R.layout.contacts_fragment_contact_item, R.id.contacts_fragment_contact_item_name_view, R.id.contacts_fragment_contact_item_avatar_view, R.id.contacts_fragment_contact_item_tag_view, R.id.contacts_fragment_contact_item_tag_title_view, R.id.contacts_fragment_contact_item_certified_image_view, R.id.contacts_fragment_contact_item_separator_view);
        mUIContactListAdapter.setAddContact(true);
        mUIContactRecyclerView.setAdapter(mUIContactListAdapter);

        mTransferView = view.findViewById(R.id.contacts_fragment_transfer_view);
        mTransferView.setOnClickListener(v -> onTransferClick());

        TextView transferTextView = view.findViewById(R.id.contacts_fragment_transfer_text_view);
        Design.updateTextFont(transferTextView, Design.FONT_REGULAR26);
        transferTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        transferTextView.setPaintFlags(transferTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        mNoResultFoundView = view.findViewById(R.id.contacts_fragment_no_result_found_view);

        ImageView noResultFoundImageView = view.findViewById(R.id.contacts_fragment_no_result_found_image_view);
        layoutParams = noResultFoundImageView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_NO_RESULT_IMAGE_VIEW_WIDTH * Design.WIDTH_RATIO);
        layoutParams.height = (int) (DESIGN_NO_RESULT_IMAGE_VIEW_HEIGHT * Design.HEIGHT_RATIO);

        mNoResultFoundTitleView = view.findViewById(R.id.contacts_fragment_no_result_found_title_view);
        Design.updateTextFont(mNoResultFoundTitleView, Design.FONT_MEDIUM34);
        mNoResultFoundTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mUIInitialized = true;
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
        if (profile != null) {

            InputMethodManager inputMethodManager = (InputMethodManager) mTwinmeActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null && mSearchView != null) {
                mSearchView.setFocusable(false);
                inputMethodManager.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
            }

            DrawerLayout drawerLayout = mTwinmeActivity.findViewById(R.id.main_activity_drawer_layout);

            MenuAddContactView menuAddContactView = new MenuAddContactView(mTwinmeActivity, null);
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            menuAddContactView.setLayoutParams(layoutParams);

            MenuAddContactView.Observer observer = new MenuAddContactView.Observer() {
                @Override
                public void onStartAddContactByScan() {

                    menuAddContactView.animationCloseMenu();

                    if (mTwinmeActivity == null) {
                        return;
                    }

                    Intent intent = new Intent();
                    intent.putExtra(Intents.INTENT_PROFILE_ID, profile.getId());
                    intent.putExtra(Intents.INTENT_INVITATION_MODE, AddContactActivity.InvitationMode.SCAN);
                    intent.setClass(mTwinmeActivity, AddContactActivity.class);
                    startActivity(intent);
                }

                @Override
                public void onStartAddContactByInvite() {

                    menuAddContactView.animationCloseMenu();

                    if (mTwinmeActivity == null) {
                        return;
                    }

                    Intent intent = new Intent();
                    intent.putExtra(Intents.INTENT_PROFILE_ID, profile.getId());
                    intent.putExtra(Intents.INTENT_INVITATION_MODE, AddContactActivity.InvitationMode.INVITE);
                    intent.setClass(mTwinmeActivity, AddContactActivity.class);
                    startActivity(intent);
                }

                @Override
                public void onCloseMenuSelectActionAnimationEnd() {

                    drawerLayout.removeView(menuAddContactView);
                    if (mTwinmeActivity != null) {
                        mTwinmeActivity.setStatusBarColor();
                    }
                }
            };

            menuAddContactView.setObserver(observer);

            drawerLayout.addView(menuAddContactView);

            List<UIMenuSelectAction> actions = new ArrayList<>();
            actions.add(new UIMenuSelectAction(getString(R.string.contacts_fragment_scan_contact_title), R.drawable.scan_code));
            actions.add(new UIMenuSelectAction(getString(R.string.contacts_fragment_invite_contact_title), R.drawable.qrcode));
            menuAddContactView.setActions(actions, mTwinmeActivity);
            menuAddContactView.openMenu(false);

            int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
            mTwinmeActivity.setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
        } else {
            Intent intent = new Intent();
            intent.putExtra(Intents.INTENT_FIRST_PROFILE, true);
            intent.putExtra(Intents.INTENT_FROM_CONTACT, true);
            startActivity(intent, AddProfileActivity.class);
        }
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

    private void notifyContactsListChanged() {
        if (DEBUG) {
            Log.d(LOG_TAG, "notifyContactListChanged");
        }

        if (mUIInitialized) {
            if (mSearchView != null && mSearchView.isIconified()) {
                mUIContactListAdapter.setAddContact(true);
            } else {
                mUIContactListAdapter.setAddContact(false);
            }

            mUIContactListAdapter.notifyDataSetChanged();

            if (mUIContacts.isEmpty() && mOnGetContactDone && mSearchView != null && mSearchView.isIconified()) {

                if (mMenu != null) {
                    MenuItem searchMenuItem = mMenu.findItem(R.id.search_action);
                    setEnabled(searchMenuItem, false);
                }

                mNoContactImageView.setVisibility(View.VISIBLE);
                mNoContactTextView.setVisibility(View.VISIBLE);
                mInviteContactView.setVisibility(View.VISIBLE);
                mTransferView.setVisibility(View.VISIBLE);
                mNoResultFoundView.setVisibility(View.GONE);
                mUIContactRecyclerView.setVisibility(View.GONE);
            } else if (mOnGetContactDone) {
                if (mMenu != null) {
                    MenuItem searchMenuItem = mMenu.findItem(R.id.search_action);
                    setEnabled(searchMenuItem, true);
                }
                mNoContactImageView.setVisibility(View.GONE);
                mNoContactTextView.setVisibility(View.GONE);
                mInviteContactView.setVisibility(View.GONE);
                mTransferView.setVisibility(View.GONE);
                mUIContactRecyclerView.setVisibility(View.VISIBLE);

                if (mUIContacts.isEmpty() && mSearchView != null && !mSearchView.isIconified()) {
                    mNoResultFoundView.setVisibility(View.VISIBLE);
                } else {
                    mNoResultFoundView.setVisibility(View.GONE);
                }
            }
        }
    }

    private void findContactByName(String text) {
        if (DEBUG) {
            Log.d(LOG_TAG, "findContactByName: " + text);
        }

        // This fragment is detached and has no activity: ignore the action.
        if (!isAdded() || mTwinmeActivity == null) {
            return;
        }

        if (mNoResultFoundTitleView != null) {
            mNoResultFoundTitleView.setText(String.format(getString(R.string.conversations_fragment_no_result_found), text));
        }

        mContactsService.findContactsByName(text);
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
                menuInflater.inflate(R.menu.contacts_menu, menu);

                mMenu = menu;

                MenuItem menuItem = menu.findItem(R.id.add_contact_action);

                ImageView imageView = (ImageView) menuItem.getActionView();

                if (imageView != null) {
                    imageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.action_bar_add_contact, null));
                    imageView.setPadding(Design.TOOLBAR_IMAGE_ITEM_PADDING, 0, Design.TOOLBAR_IMAGE_ITEM_PADDING, 0);
                    imageView.setOnClickListener(view -> onAddContactClick());
                    imageView.setContentDescription(getString(R.string.add_contact_activity_title));
                }

                MenuItem searchItem = menu.findItem(R.id.search_action);
                searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionExpand(@NonNull MenuItem menuItem) {
                        return true;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(@NonNull MenuItem menuItem) {

                        if (mUIContactListAdapter != null) {
                            mUIContactListAdapter.setAddContact(true);
                            mUIContactListAdapter.notifyDataSetChanged();
                        }

                        return true;
                    }
                });

                if (mSearchView != null) {
                    mSearchView.setOnQueryTextListener(null);
                }

                mSearchView = (SearchView) searchItem.getActionView();

                if (mSearchView != null) {
                    mSearchView.setQueryHint(getString(R.string.application_search_hint));

                    SearchView.OnQueryTextListener onQueryTextListener = new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String query) {
                            return false;
                        }

                        @Override
                        public boolean onQueryTextChange(String newText) {
                            findContactByName(newText);
                            return false;
                        }
                    };

                    mSearchView.setOnQueryTextListener(onQueryTextListener);
                }

                if (!mLastSearch.isEmpty()) {
                    searchItem.expandActionView();
                    mSearchView.setQuery(mLastSearch, false);
                    mLastSearch = "";
                }

                notifyContactsListChanged();
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                // Handle menu item clicks here
                return false;
            }

        }, getViewLifecycleOwner());
    }

    private void updateFont() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateFont");
        }

        Design.updateTextFont(mNoContactTextView, Design.FONT_MEDIUM34);
    }

    private void updateColor() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateColor");
        }

        // Don't try to access the activity or the resource if the fragment is detached.
        if (mTwinmeActivity == null) {
            return;
        }

        mNoContactTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        final Resources resources = mTwinmeActivity.getResources();

        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable saveViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        saveViewBackground.getPaint().setColor(Design.getMainStyle());
        mInviteContactView.setBackground(saveViewBackground);

        boolean darkMode = false;
        int currentNightMode = resources.getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        int displayMode = Settings.displayMode.getInt();
        if ((currentNightMode == Configuration.UI_MODE_NIGHT_YES && displayMode == DisplayMode.SYSTEM.ordinal())  || displayMode == DisplayMode.DARK.ordinal()) {
            darkMode = true;
        }

        mNoContactImageView.setImageDrawable(ResourcesCompat.getDrawable(resources, darkMode ? R.drawable.onboarding_step3_dark : R.drawable.onboarding_step3, null));
    }
}
