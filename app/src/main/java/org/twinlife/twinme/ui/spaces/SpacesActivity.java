/*
 *  Copyright (c) 2019-2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.spaces;

import static org.twinlife.twinme.ui.Intents.INTENT_SPACE_ID;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.ColorUtils;
import androidx.percentlayout.widget.PercentRelativeLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.NotificationService.NotificationStat;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Group;
import org.twinlife.twinme.models.Space;
import org.twinlife.twinme.services.SpaceService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.EditProfileActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.utils.AbstractConfirmView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class SpacesActivity extends AbstractSpaceActivity {
    private static final String LOG_TAG = "SpacesActivity";
    private static final boolean DEBUG = false;

    private static final float DESIGN_ITEM_VIEW_HEIGHT = 124;
    private static int ITEM_VIEW_HEIGHT;

    private static final int ITEM_LIST_CACHE_SIZE = 32;

    private boolean mUIInitialized = false;

    private View mContentSearchView;
    private EditText mSearchEditText;
    private View mClearSearchView;
    private SpacesAdapter mSpacesListAdapter;
    private RecyclerView mSpacesRecyclerView;

    private final List<UISpace> mUISpaces = new ArrayList<>();

    private SpaceService mSpaceService;

    private boolean mMoveContactMode = false;
    private boolean mMoveGroupMode = false;
    private boolean mSetCurrentSpaceMode = false;
    private Contact mContact;
    private Group mGroup;
    private Space mCurrentSpace;

    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        mSpaceService = new SpaceService(this, getTwinmeContext(), this, null);

        Intent intent = getIntent();

        String value = intent.getStringExtra(Intents.INTENT_CONTACT_ID);
        if (value != null) {
            UUID contactId = UUID.fromString(value);
            mSpaceService.getContact(contactId);
            mMoveContactMode = true;
        }

        value = intent.getStringExtra(Intents.INTENT_GROUP_ID);
        if (value != null) {
            UUID groupId = UUID.fromString(value);
            mSpaceService.getGroup(groupId);
            mMoveGroupMode = true;
        }

        mSetCurrentSpaceMode = intent.getBooleanExtra(Intents.INTENT_SET_CURRENT_SPACE_MODE, false);

        initViews();
    }

    @Override
    protected void onPause() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPause");
        }

        super.onPause();

        hideKeyboard();
    }

    @Override
    protected void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        super.onResume();

        mSpaceService.getSpaces();
    }

    private void hideKeyboard() {
        if (DEBUG) {
            Log.d(LOG_TAG, "hideKeyboard");
        }

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(mSearchEditText.getWindowToken(), 0);
        }
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
        hideKeyboard();

        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSaveInstanceState: outState=" + outState);
        }

        super.onSaveInstanceState(outState);
    }

    //
    // Implement SpaceService.Observer methods
    //

    @Override
    public void onGetSpaces(@NonNull List<Space> spaces) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetSpaces: spaces=" + spaces);
        }

        mUISpaces.clear();

        if (spaces.isEmpty()) {
            mSpaceService.findSpacesNotifications();
            return;
        }

        AtomicInteger avatarCounter = new AtomicInteger(spaces.size());

        for (Space space : spaces) {
            mSpaceService.getSpaceImage(space, (Bitmap avatar) ->
                    mSpaceService.getProfileImage(space.getProfile(), (Bitmap profileAvatar) -> {
                        mSpacesListAdapter.updateUISpace(space, avatar, profileAvatar);

                        if (avatarCounter.decrementAndGet() == 0) {
                            mSpaceService.findSpacesNotifications();
                        }
                    }));
        }
    }

    @Override
    public void onGetSpace(@NonNull Space space, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetSpace: space=" + space);
        }

        mCurrentSpace = space;
    }

    @Override
    public void onSetCurrentSpace(@NonNull Space space) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSetCurrentSpace: space=" + space);
        }

        finish();
    }

    @Override
    public void onGetSpacesNotifications(@NonNull Map<Space, NotificationStat> spacesNotifications) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetSpacesNotifications: space=" + spacesNotifications);
        }

        for (UISpace space : mUISpaces) {
            NotificationStat stat = spacesNotifications.get(space.getSpace());
            space.setHasNotification(stat != null && stat.getPendingCount() > 0);

            if (mContact != null && mContact.isSpace(space.getSpace())) {
                space.setIsCurrentSpace(true);
            } else if (mGroup != null && mGroup.isSpace(space.getSpace())) {
                space.setIsCurrentSpace(true);
            } else if (mContact == null && mGroup == null && mCurrentSpace != null && mCurrentSpace.getId() == space.getId()) {
                space.setIsCurrentSpace(true);
            }
        }

        if (mUIInitialized) {
            mSpacesRecyclerView.requestLayout();
            mSpacesListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onGetContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContact: contact=" + contact);
        }

        mContact = contact;
    }

    @Override
    public void onUpdateContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateContact: contact=" + contact);
        }

        finish();
    }

    @Override
    public void onGetGroup(@NonNull Group group, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetGroup");
        }

        mGroup = group;
    }

    @Override
    public void onUpdateGroup(@NonNull Group group) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateGroup: group=" + group);
        }

        finish();
    }

    public void onCreateSpaceClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateSpaceClick");
        }

        Intent intent = new Intent();
        intent.setClass(SpacesActivity.this, EditSpaceActivity.class);
        startActivity(intent);
    }

    //
    // Private methods
    //

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.spaces_activity);

        setStatusBarColor();
        setToolBar(R.id.spaces_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);

        setTitle(getString(R.string.spaces_activity_title));

        applyInsets(R.id.spaces_activity_layout, R.id.spaces_activity_tool_bar, R.id.spaces_activity_spaces_list_view, Design.TOOLBAR_COLOR, false);

        View searchView = findViewById(R.id.spaces_activity_search_view);
        searchView.setBackgroundColor(Design.TOOLBAR_COLOR);

        ViewGroup.LayoutParams layoutParams = searchView.getLayoutParams();
        layoutParams.height = Design.SEARCH_VIEW_HEIGHT;

        mContentSearchView = findViewById(R.id.spaces_activity_search_content_view);
        mContentSearchView.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.grey_search_rounded, getTheme()));

        mClearSearchView = findViewById(R.id.spaces_activity_clear_image_view);
        mClearSearchView.setVisibility(View.GONE);
        mClearSearchView.setOnClickListener(v -> {
            mSearchEditText.setText("");
            mClearSearchView.setVisibility(View.GONE);
        });

        mSearchEditText = findViewById(R.id.spaces_activity_search_edit_text_view);
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
                mSpaceService.findSpaceByName(s.toString());
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
                hideKeyboard();
                return true;
            }
            return false;
        });

        SpacesAdapter.OnSpaceClickListener onSpaceClickListener = this::onSpaceClick;

        mSpacesListAdapter = new SpacesAdapter(this, ITEM_VIEW_HEIGHT, mUISpaces, onSpaceClickListener, null);
        LinearLayoutManager uiSpacesLinearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        mSpacesRecyclerView = findViewById(R.id.spaces_activity_spaces_list_view);
        mSpacesRecyclerView.setLayoutManager(uiSpacesLinearLayoutManager);
        mSpacesRecyclerView.setAdapter(mSpacesListAdapter);
        mSpacesRecyclerView.setItemViewCacheSize(ITEM_LIST_CACHE_SIZE);
        mSpacesRecyclerView.setItemAnimator(null);
        mSpacesRecyclerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        mProgressBarView = findViewById(R.id.share_activity_progress_bar);

        mUIInitialized = true;
    }

    private void onSpaceClick(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSpaceClick: position=" + position);
        }

        if (position >= 0 && position < mUISpaces.size()) {
            Space space = mUISpaces.get(position).getSpace();
            if (mMoveContactMode) {
                if (!space.hasPermission(Space.Permission.MOVE_CONTACT)) {
                    showAlertMessageView(R.id.spaces_activity_layout, getString(R.string.deleted_account_activity_warning), getString(R.string.spaces_activity_permission_not_allowed), false, null);
                } else if (space.getProfile() == null) {

                    mSpaceService.getSpaceImage(space, (Bitmap avatar) -> {
                        PercentRelativeLayout percentRelativeLayout = findViewById(R.id.spaces_activity_layout);

                        SpaceActionConfirmView spaceActionConfirmView = new SpaceActionConfirmView(this, null);
                        PercentRelativeLayout.LayoutParams layoutParams = new PercentRelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT);
                        spaceActionConfirmView.setLayoutParams(layoutParams);

                        spaceActionConfirmView.setSpaceName(space.getSpaceSettings().getName(), space.getSpaceSettings().getStyle());
                        spaceActionConfirmView.setAvatar(avatar, false);
                        spaceActionConfirmView.setIcon(R.drawable.action_bar_add_contact);
                        spaceActionConfirmView.setTitle(getString(R.string.create_profile_activity_title));
                        spaceActionConfirmView.setMessage(getString(R.string.create_space_activity_contacts_no_profile));
                        spaceActionConfirmView.setConfirmTitle(getString(R.string.application_now));
                        spaceActionConfirmView.setCancelTitle(getString(R.string.application_later));

                        AbstractConfirmView.Observer observer = new AbstractConfirmView.Observer() {
                            @Override
                            public void onConfirmClick() {
                                onEditIdentityClick(space.getId());
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
                } else if (mContact.getSpace() != null && mContact.getSpace().getId() == space.getId()) {
                    showAlertMessageView(R.id.spaces_activity_layout, getString(R.string.deleted_account_activity_warning), getString(R.string.spaces_activity_move_contact_already_in_space), false, null);
                } else {
                    mSpaceService.getSpaceImage(space, (Bitmap avatar) -> {
                        PercentRelativeLayout percentRelativeLayout = findViewById(R.id.spaces_activity_layout);

                        SpaceActionConfirmView spaceActionConfirmView = new SpaceActionConfirmView(this, null);
                        PercentRelativeLayout.LayoutParams layoutParams = new PercentRelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT);
                        spaceActionConfirmView.setLayoutParams(layoutParams);

                        spaceActionConfirmView.setSpaceName(space.getSpaceSettings().getName(), space.getSpaceSettings().getStyle());
                        spaceActionConfirmView.setAvatar(avatar, false);
                        spaceActionConfirmView.setIcon(R.drawable.move_contacts_icon);
                        spaceActionConfirmView.setIconTintColor(Color.WHITE);
                        spaceActionConfirmView.setTitle(space.getSpaceSettings().getName());
                        spaceActionConfirmView.setMessage(getString(R.string.contact_space_activity_move_message));
                        spaceActionConfirmView.setConfirmTitle(getString(R.string.contact_space_activity_move_title));
                        spaceActionConfirmView.setCancelTitle(getString(R.string.application_cancel));

                        AbstractConfirmView.Observer observer = new AbstractConfirmView.Observer() {
                            @Override
                            public void onConfirmClick() {
                                mSpaceService.moveContactToSpace(space, mContact);
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
            } else if (mMoveGroupMode) {
                if (!space.hasPermission(Space.Permission.MOVE_GROUP)) {
                    showAlertMessageView(R.id.spaces_activity_layout, getString(R.string.deleted_account_activity_warning), getString(R.string.spaces_activity_permission_not_allowed), false, null);
                } else if (space.getProfile() == null) {

                    mSpaceService.getSpaceImage(space, (Bitmap avatar) -> {
                        PercentRelativeLayout percentRelativeLayout = findViewById(R.id.spaces_activity_layout);

                        SpaceActionConfirmView spaceActionConfirmView = new SpaceActionConfirmView(this, null);
                        PercentRelativeLayout.LayoutParams layoutParams = new PercentRelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT);
                        spaceActionConfirmView.setLayoutParams(layoutParams);

                        spaceActionConfirmView.setSpaceName(space.getSpaceSettings().getName(), space.getSpaceSettings().getStyle());
                        spaceActionConfirmView.setAvatar(avatar, false);
                        spaceActionConfirmView.setIcon(R.drawable.action_bar_add_contact);
                        spaceActionConfirmView.setTitle(getString(R.string.create_profile_activity_title));
                        spaceActionConfirmView.setMessage(getString(R.string.spaces_activity_move_group_no_profile));
                        spaceActionConfirmView.setConfirmTitle(getString(R.string.application_now));
                        spaceActionConfirmView.setCancelTitle(getString(R.string.application_later));

                        AbstractConfirmView.Observer observer = new AbstractConfirmView.Observer() {
                            @Override
                            public void onConfirmClick() {
                                onEditIdentityClick(space.getId());
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
                } else if (mGroup.getSpace() != null && mGroup.getSpace().getId() == space.getId()) {
                    showAlertMessageView(R.id.spaces_activity_layout, getString(R.string.deleted_account_activity_warning), getString(R.string.spaces_activity_move_group_already_in_space), false, null);
                } else {
                    mSpaceService.getSpaceImage(space, (Bitmap avatar) -> {
                        PercentRelativeLayout percentRelativeLayout = findViewById(R.id.spaces_activity_layout);

                        SpaceActionConfirmView spaceActionConfirmView = new SpaceActionConfirmView(this, null);
                        PercentRelativeLayout.LayoutParams layoutParams = new PercentRelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT);
                        spaceActionConfirmView.setLayoutParams(layoutParams);

                        spaceActionConfirmView.setSpaceName(space.getSpaceSettings().getName(), space.getSpaceSettings().getStyle());
                        spaceActionConfirmView.setAvatar(avatar, false);
                        spaceActionConfirmView.setIcon(R.drawable.move_contacts_icon);
                        spaceActionConfirmView.setIconTintColor(Color.WHITE);
                        spaceActionConfirmView.setTitle(space.getSpaceSettings().getName());
                        spaceActionConfirmView.setMessage(getString(R.string.spaces_activity_move_message));
                        spaceActionConfirmView.setConfirmTitle(getString(R.string.contact_space_activity_move_title));
                        spaceActionConfirmView.setCancelTitle(getString(R.string.application_cancel));

                        AbstractConfirmView.Observer observer = new AbstractConfirmView.Observer() {
                            @Override
                            public void onConfirmClick() {
                                mSpaceService.moveGroupToSpace(space, mGroup);
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
            } else if (mSetCurrentSpaceMode) {
                mSpaceService.setSpace(space);
            } else {
                Intent data = new Intent();
                data.putExtra(Intents.INTENT_SPACE_SELECTION, space.getId().toString());
                setResult(RESULT_OK, data);
                finish();
            }
        }

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

    private void onEditIdentityClick(UUID spaceId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onEditIdentityClick");
        }

        if (spaceId != null) {
            Intent intent = new Intent();
            intent.putExtra(INTENT_SPACE_ID, spaceId.toString());
            intent.setClass(this, EditProfileActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void setupDesign() {
        if (DEBUG) {
            Log.d(LOG_TAG, "setupDesign");
        }

        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
    }
}
