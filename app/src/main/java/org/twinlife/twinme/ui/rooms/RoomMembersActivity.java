/*
 *  Copyright (c) 2020-2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.rooms;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.TwincodeOutbound;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.services.RoomMemberService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.utils.AbstractBottomSheetView;
import org.twinlife.twinme.utils.DefaultConfirmView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RoomMembersActivity extends AbstractTwinmeActivity implements RoomMemberService.Observer, MenuRoomMemberView.Observer {
    private static final String LOG_TAG = "RoomMembersActivity";
    private static final boolean DEBUG = false;

    protected static final float DESIGN_ITEM_VIEW_HEIGHT = 120f;
    protected static int ITEM_VIEW_HEIGHT;

    private UUID mRoomId;

    @Nullable
    private Contact mRoom;
    private RoomMemberListAdapter mRoomMemberListAdapter;
    private RecyclerView mUIMemberRecyclerView;
    private MenuRoomMemberView mMenuRoomMemberView;
    private View mOverlayView;

    private final List<UIRoomMember> mRoomAdmins = new ArrayList<>();
    private final List<UIRoomMember> mRoomMembers = new ArrayList<>();
    private UIRoomMember mSelectedMember;

    private RoomMemberService mRoomMemberService;

    private boolean mUIInitialized = false;

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
        }

        // Start with an empty contact list.
        initViews();
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        mRoomMemberService.dispose();

        super.onDestroy();
    }

    //
    // Menu management
    //

    private void openMenu(UIRoomMember uiRoomMember) {
        if (DEBUG) {
            Log.d(LOG_TAG, "openMenu");
        }

        if (mRoom == null) {
            return;
        }

        boolean showAdminAction = mRoom.getCapabilities().hasAdmin();
        boolean showInviteAction = true;
        boolean removeAdminAction = false;

        for (UIRoomMember uiMember : mRoomAdmins) {
            if (uiMember.getTwincodeOutbound().getId().equals(uiRoomMember.getId())) {
                removeAdminAction = true;
                break;
            }
        }

        if (uiRoomMember.getTwincodeOutbound().getId() == mRoom.getIdentityTwincodeOutbound().getId()) {
            showInviteAction = false;
        }

        if (!showAdminAction && !showInviteAction) {
            return;
        }

        if (mMenuRoomMemberView.getVisibility() == View.INVISIBLE) {
            mSelectedMember = uiRoomMember;
            mMenuRoomMemberView.setVisibility(View.VISIBLE);
            mOverlayView.setVisibility(View.VISIBLE);
            mMenuRoomMemberView.openMenu(uiRoomMember, showAdminAction, showInviteAction, removeAdminAction);
            openMenuColor();
        }
    }

    public void closeMenu() {
        if (DEBUG) {
            Log.d(LOG_TAG, "closeMenu");
        }

        mMenuRoomMemberView.setVisibility(View.INVISIBLE);
        mOverlayView.setVisibility(View.INVISIBLE);
        setStatusBarColor();

        mSelectedMember = null;
    }

    public void onChangeAdminClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onChangeAdminClick");
        }

        changeAdmin(mSelectedMember);

        closeMenu();
    }

    public void onRemoveAdminClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRemoveAdminClick");
        }

        removeAdmin(mSelectedMember);

        closeMenu();
    }

    public void onRemoveMemberClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRemoveMemberClick");
        }

        onRemoveClick(mSelectedMember);
        closeMenu();
    }

    public void onInviteMemberClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onInviteMemberClick");
        }

        onInviteClick(mSelectedMember);
        closeMenu();
    }

    //
    // RoomMemberService.Observer methods
    //

    @Override
    public void onGetContactNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContactNotFound");
        }

        finish();
    }

    @Override
    public void onDeleteContact(@NonNull UUID roomId) {

        if (mRoom != null && mRoom.getId() == roomId) {
            finish();
        }
    }

    @Override
    public void onGetContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContactNotFound");
        }

        mRoom = contact;
    }

    @Override
    public void onUpdateContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateContact contact=" + contact);
        }

        if (!contact.getId().equals(mRoomId)) {

            return;
        }

        mRoom = contact;
    }

    @Override
    public void onGetRoomAdmins(@NonNull List<TwincodeOutbound> admins) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetRoomAdmins: admins=" + admins);
        }

        for (TwincodeOutbound twincodeOutbound : admins) {
            UIRoomMember uiRoomAdmin = new UIRoomMember(twincodeOutbound, null);
            mRoomAdmins.add(uiRoomAdmin);
        }

        notifyMemberListChanged();
    }

    @Override
    public void onGetRoomMembers(@NonNull List<TwincodeOutbound> members) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetRoomMembers: members=" + members);
        }

        for (TwincodeOutbound twincodeOutbound : members) {
            UIRoomMember uiRoomMember = new UIRoomMember(twincodeOutbound, null);
            mRoomMembers.add(uiRoomMember);
        }

        notifyMemberListChanged();
    }

    @Override
    public void onGetRoomAdminAvatar(@NonNull TwincodeOutbound twincodeOutbound, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetRoomAdminAvatar: twincodeOutbound=" + twincodeOutbound + " avatar=" + avatar);
        }

        for (UIRoomMember uiRoomAdmin : mRoomAdmins) {
            if (twincodeOutbound.getId().equals(uiRoomAdmin.getTwincodeOutbound().getId())) {
                uiRoomAdmin.setAvatar(avatar);
                break;
            }
        }

        notifyMemberListChanged();
    }

    @Override
    public void onGetRoomMemberAvatar(@NonNull TwincodeOutbound twincodeOutbound, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetRoomMemberAvatar: twincodeOutbound=" + twincodeOutbound + " avatar=" + avatar);
        }

        for (UIRoomMember uiRoomMember : mRoomMembers) {
            if (twincodeOutbound.getId().equals(uiRoomMember.getTwincodeOutbound().getId())) {
                uiRoomMember.setAvatar(avatar);
                break;
            }
        }

        notifyMemberListChanged();
    }

    @Override
    public void onSetAdministrator(@NonNull UUID adminId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSetAdministrator: adminId=" + adminId);
        }

        for (UIRoomMember uiRoomMember : mRoomMembers) {
            if (uiRoomMember.getTwincodeOutbound().getId().equals(adminId)) {
                mRoomAdmins.add(uiRoomMember);
                mRoomMembers.remove(uiRoomMember);
                break;
            }
        }

        notifyMemberListChanged();
    }

    @Override
    public void onRemoveAdministrator(@NonNull UUID adminId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRemoveAdministrator: adminId=" + adminId);
        }

        for (UIRoomMember uiRoomMember : mRoomAdmins) {
            if (uiRoomMember.getTwincodeOutbound().getId().equals(adminId)) {
                mRoomMembers.add(uiRoomMember);
                mRoomAdmins.remove(uiRoomMember);
                break;
            }
        }

        notifyMemberListChanged();
    }

    @Override
    public void onRemoveMember(@NonNull UUID memberId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRemoveMember: memberId=" + memberId);
        }

        for (UIRoomMember uiRoomMember : mRoomMembers) {
            if (uiRoomMember.getTwincodeOutbound().getId().equals(memberId)) {
                mRoomMembers.remove(uiRoomMember);
                break;
            }
        }

        notifyMemberListChanged();
    }

    //
    // MenuRoomMemberView.Observer
    //

    @Override
    public void onCloseMenuAnimationEnd() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCloseMenuAnimationEnd");
        }

        mMenuRoomMemberView.setVisibility(View.INVISIBLE);
        mOverlayView.setVisibility(View.INVISIBLE);
    }

    //
    // Private methods
    //

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.room_members_activity);

        setStatusBarColor();
        setToolBar(R.id.room_member_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);

        setTitle(getString(R.string.room_members_activity_participants_title));
        setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        applyInsets(R.id.room_member_activity_layout, R.id.room_member_activity_tool_bar, R.id.room_member_activity_member_list_view, Design.TOOLBAR_COLOR, false);

        RoomMemberListAdapter.OnRoomMemberClickListener onRoomMemberClickListener = this::openMenu;

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        mUIMemberRecyclerView = findViewById(R.id.room_member_activity_member_list_view);
        mUIMemberRecyclerView.setLayoutManager(layoutManager);
        mUIMemberRecyclerView.setItemViewCacheSize(Design.ITEM_LIST_CACHE_SIZE);
        mUIMemberRecyclerView.setItemAnimator(null);
        mUIMemberRecyclerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        mOverlayView = findViewById(R.id.room_member_activity_overlay_view);
        mOverlayView.setBackgroundColor(Design.OVERLAY_VIEW_COLOR);
        mOverlayView.setOnClickListener(view -> closeMenu());

        mMenuRoomMemberView = findViewById(R.id.room_member_activity_menu_view);
        mMenuRoomMemberView.setVisibility(View.INVISIBLE);
        mMenuRoomMemberView.setObserver(this);
        mMenuRoomMemberView.setRoomMemberActivity(this);

        mProgressBarView = findViewById(R.id.room_member_activity_progress_bar);

        // Setup the service after the view is initialized but before the adapter!
        mRoomMemberService = new RoomMemberService(this, getTwinmeContext(), this, mRoomId);
        mRoomMemberListAdapter = new RoomMemberListAdapter(this, mRoomAdmins, mRoomMembers, onRoomMemberClickListener);
        mUIMemberRecyclerView.setAdapter(mRoomMemberListAdapter);

        mUIMemberRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (layoutManager.findLastVisibleItemPosition() == mRoomMembers.size() + 2) {
                        mRoomMemberService.nextMembers();
                    }
                }
            }
        });

        mUIInitialized = true;
    }

    private void notifyMemberListChanged() {
        if (DEBUG) {
            Log.d(LOG_TAG, "notifyMemberListChanged");
        }

        if (mUIInitialized) {
            mUIMemberRecyclerView.requestLayout();
            mRoomMemberListAdapter.notifyDataSetChanged();
        }
    }

    private void onRemoveClick(UIRoomMember roomMember) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRemoveClick");
        }

        boolean isAdmin = false;

        for (UIRoomMember uiRoomMember : mRoomAdmins) {
            if (uiRoomMember.getTwincodeOutbound().getId().equals(roomMember.getId())) {
                isAdmin = true;
                break;
            }
        }

        if (isAdmin && mRoomAdmins.size() == 1) {
            showAlertMessageView(R.id.room_member_activity_layout, getString(R.string.deleted_account_activity_warning), getString(R.string.room_members_activity_only_admin_message), false, null);
        } else {
            ViewGroup viewGroup = findViewById(R.id.room_member_activity_layout);

            DefaultConfirmView defaultConfirmView = new DefaultConfirmView(this, null);
            defaultConfirmView.setTitle(getString(R.string.deleted_account_activity_warning));
            defaultConfirmView.setMessage(getString(R.string.application_delete_message));
            defaultConfirmView.setImage(null);
            defaultConfirmView.setConfirmColor(Design.DELETE_COLOR_RED);
            defaultConfirmView.setConfirmTitle(getString(R.string.application_delete));

            AbstractBottomSheetView.Observer observer = new AbstractBottomSheetView.Observer() {
                @Override
                public void onConfirmClick() {
                    defaultConfirmView.animationCloseConfirmView();
                }

                @Override
                public void onCancelClick() {
                    defaultConfirmView.animationCloseConfirmView();
                }

                @Override
                public void onDismissClick() {
                    defaultConfirmView.animationCloseConfirmView();
                }

                @Override
                public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                    viewGroup.removeView(defaultConfirmView);
                    setStatusBarColor();

                    if (fromConfirmAction) {
                        onConfirmedRemoveClick(roomMember);
                    }
                }
            };
            defaultConfirmView.setObserver(observer);
            viewGroup.addView(defaultConfirmView);
            defaultConfirmView.show();

            int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
            setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
        }
    }

    private void onConfirmedRemoveClick(UIRoomMember roomMember) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onConfirmedRemoveClick");
        }

        mRoomMemberService.removeMember(roomMember.getId());
    }

    private void changeAdmin(UIRoomMember roomMember) {

        ViewGroup viewGroup = findViewById(R.id.room_member_activity_layout);

        DefaultConfirmView defaultConfirmView = new DefaultConfirmView(this, null);
        defaultConfirmView.setTitle(getString(R.string.deleted_account_activity_warning));
        defaultConfirmView.setMessage(getString(R.string.room_members_activity_change_admin_title));
        defaultConfirmView.setImage(null);
        defaultConfirmView.setConfirmTitle(getString(R.string.application_confirm));

        AbstractBottomSheetView.Observer observer = new AbstractBottomSheetView.Observer() {
            @Override
            public void onConfirmClick() {
                defaultConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCancelClick() {
                defaultConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onDismissClick() {
                defaultConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                viewGroup.removeView(defaultConfirmView);
                setStatusBarColor();

                if (fromConfirmAction) {
                    mRoomMemberService.setRoomAdministrator(roomMember.getId());
                }
            }
        };
        defaultConfirmView.setObserver(observer);
        viewGroup.addView(defaultConfirmView);
        defaultConfirmView.show();

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }

    private void removeAdmin(UIRoomMember roomMember) {
        if (DEBUG) {
            Log.d(LOG_TAG, "removeAdmin: roomMember=" + roomMember);
        }

        if (mRoomAdmins.size() == 1) {
            showAlertMessageView(R.id.room_member_activity_layout, getString(R.string.deleted_account_activity_warning), getString(R.string.room_members_activity_only_admin_message), false, null);
        } else {
            ViewGroup viewGroup = findViewById(R.id.room_member_activity_layout);

            DefaultConfirmView defaultConfirmView = new DefaultConfirmView(this, null);
            defaultConfirmView.setTitle(getString(R.string.deleted_account_activity_warning));
            defaultConfirmView.setMessage(getString(R.string.room_members_activity_remove_admin_title));
            defaultConfirmView.setImage(null);
            defaultConfirmView.setConfirmTitle(getString(R.string.application_confirm));

            AbstractBottomSheetView.Observer observer = new AbstractBottomSheetView.Observer() {
                @Override
                public void onConfirmClick() {
                    defaultConfirmView.animationCloseConfirmView();
                }

                @Override
                public void onCancelClick() {
                    defaultConfirmView.animationCloseConfirmView();
                }

                @Override
                public void onDismissClick() {
                    defaultConfirmView.animationCloseConfirmView();
                }

                @Override
                public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                    viewGroup.removeView(defaultConfirmView);
                    setStatusBarColor();

                    if (fromConfirmAction) {
                        mRoomMemberService.removeRoomAdministrator(roomMember.getId());
                    }
                }
            };
            defaultConfirmView.setObserver(observer);
            viewGroup.addView(defaultConfirmView);
            defaultConfirmView.show();

            int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
            setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
        }
    }

    private void onInviteClick(UIRoomMember roomMember) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onInviteClick");
        }

        ViewGroup viewGroup = findViewById(R.id.room_member_activity_layout);

        DefaultConfirmView defaultConfirmView = new DefaultConfirmView(this, null);
        defaultConfirmView.setTitle(getString(R.string.group_member_activity_invitation_title));
        defaultConfirmView.setMessage(String.format(getString(R.string.group_member_activity_invitation_message), roomMember.getName()));
        defaultConfirmView.setImage(null);
        defaultConfirmView.setConfirmTitle(getString(R.string.application_ok));

        AbstractBottomSheetView.Observer observer = new AbstractBottomSheetView.Observer() {
            @Override
            public void onConfirmClick() {
                defaultConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCancelClick() {
                defaultConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onDismissClick() {
                defaultConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                viewGroup.removeView(defaultConfirmView);
                setStatusBarColor();

                if (fromConfirmAction) {
                    mRoomMemberService.inviteMember(roomMember.getId());
                }
            }
        };
        defaultConfirmView.setObserver(observer);
        viewGroup.addView(defaultConfirmView);
        defaultConfirmView.show();

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }

    @Override
    public void setupDesign() {
        if (DEBUG) {
            Log.d(LOG_TAG, "setupDesign");
        }

        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
    }
}
