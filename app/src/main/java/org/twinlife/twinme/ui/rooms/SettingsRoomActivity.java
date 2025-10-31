/*
 *  Copyright (c) 2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.rooms;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.RoomConfig;
import org.twinlife.twinme.services.EditRoomService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.Intents;

import java.util.UUID;

public class SettingsRoomActivity extends AbstractTwinmeActivity implements EditRoomService.Observer {
    private static final String LOG_TAG = "SettingsRoomActivity";
    private static final boolean DEBUG = false;

    private boolean mUIInitialized = false;

    public static final int ALLOW_INVITATION_SWITCH = 0;
    public static final int ALLOW_INVITATION_AS_PERSONAL_CONTACT = 1;
    public static final int VOICE_CALL_SWITCH = 2;
    public static final int VIDEO_CALL_SWITCH = 3;

    private RoomConfig.ChatMode mChatMode;
    private RoomConfig.CallMode mCallMode;
    private RoomConfig.NotificationMode mNotificationMode;
    private RoomConfig.InvitationMode mInvitationMode;

    private Contact mRoom;
    private RoomConfig mRoomConfig;

    private EditRoomService mEditRoomService;

    private Menu mMenu;

    private boolean mCanSave = false;

    private SettingsRoomListAdapter mSettingsRoomListAdapter;

    private boolean mUIPostInitialized = false;
    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        mChatMode = RoomConfig.ChatMode.CHAT_CHANNEL;
        mCallMode = RoomConfig.CallMode.CALL_VIDEO;
        mNotificationMode = RoomConfig.NotificationMode.NOISY;
        mInvitationMode = RoomConfig.InvitationMode.INVITE_ADMIN;

        initViews();

        Intent intent = getIntent();
        UUID roomId = null;
        String value = intent.getStringExtra(Intents.INTENT_CONTACT_ID);
        if (value != null) {
            roomId = UUID.fromString(value);
        }
        if (roomId != null) {
            mEditRoomService = new EditRoomService(this, getTwinmeContext(), this, roomId);
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

        mMenu = menu;

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_room_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.save_action);
        String title = menuItem.getTitle().toString();

        TextView titleView = (TextView) menuItem.getActionView();

        if (titleView != null) {
            Design.updateTextFont(titleView, Design.FONT_BOLD36);
            titleView.setText(title.toLowerCase());
            titleView.setTextColor(Color.WHITE);
            titleView.setAlpha(0.5f);
            titleView.setPadding(0, 0, Design.TOOLBAR_TEXT_ITEM_PADDING, 0);
            titleView.setOnClickListener(view -> onSaveClick());
        }

        return true;
    }

    public void onSettingChangeValue(int switchTag, boolean value) {

        switch (switchTag) {
            case ALLOW_INVITATION_SWITCH:
                if (value) {
                    mInvitationMode = RoomConfig.InvitationMode.INVITE_PUBLIC;
                } else {
                    mInvitationMode = RoomConfig.InvitationMode.INVITE_ADMIN;
                }
                break;

            case VOICE_CALL_SWITCH:
                if (value) {
                    mCallMode = RoomConfig.CallMode.CALL_AUDIO;
                } else {
                    mCallMode = RoomConfig.CallMode.CALL_DISABLED;
                }
                break;

            case VIDEO_CALL_SWITCH:
                if (value) {
                    mCallMode = RoomConfig.CallMode.CALL_VIDEO;
                } else {
                    mCallMode = RoomConfig.CallMode.CALL_AUDIO;
                }
                break;

            default:
                break;
        }

        mSettingsRoomListAdapter.notifyDataSetChanged();
        setUpdated();
    }

    public RoomConfig.CallMode getCallMode() {

        return mCallMode;
    }

    public RoomConfig.ChatMode getChatMode() {

        return mChatMode;
    }

    public RoomConfig.InvitationMode getInvitationMode() {

        return mInvitationMode;
    }

    public RoomConfig.NotificationMode getNotificationMode() {

        return mNotificationMode;
    }

    //
    // EditRoomService.Observer methods
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
    public void onGetRoomConfig(@NonNull RoomConfig roomConfig) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetRoomConfig: " + roomConfig);
        }

        mRoomConfig = roomConfig;

        mInvitationMode = mRoomConfig.getInvitationMode();
        mCallMode = mRoomConfig.getCallMode();
        mChatMode = mRoomConfig.getChatMode();
        mNotificationMode = mRoomConfig.getNotificationMode();

        mSettingsRoomListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onGetRoomConfigNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetRoomConfigNotFound");
        }

        mRoomConfig = new RoomConfig(null, mChatMode, mCallMode, mNotificationMode, mInvitationMode, null);

        mSettingsRoomListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onGetContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContact");
        }

        mRoom = contact;
        mEditRoomService.getRoomConfig();
    }

    @Override
    public void onUpdateContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateContact contact=" + contact);
        }

        if (!contact.getId().equals(mRoom.getId())) {

            return;
        }

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
        setContentView(R.layout.settings_room_activity);

        setStatusBarColor();
        setToolBar(R.id.settings_room_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);
        setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        setTitle(getString(R.string.navigation_activity_settings));

        applyInsets(R.id.settings_room_activity_layout, R.id.settings_room_activity_tool_bar, R.id.settings_room_activity_list_view, Design.TOOLBAR_COLOR, false);

        SettingsRoomListAdapter.OnSettingsRoomClickListener onSettingsRoomClickListener = new SettingsRoomListAdapter.OnSettingsRoomClickListener() {

            @Override
            public void onSelectCallMode(RoomConfig.CallMode callMode) {
                mCallMode = callMode;
                mSettingsRoomListAdapter.notifyDataSetChanged();
                setUpdated();
            }

            @Override
            public void onSelectChatMode(RoomConfig.ChatMode chatMode) {
                mChatMode = chatMode;
                mSettingsRoomListAdapter.notifyDataSetChanged();
                setUpdated();
            }

            @Override
            public void onSelectNotificationMode(RoomConfig.NotificationMode notificationMode) {
                mNotificationMode = notificationMode;
                mSettingsRoomListAdapter.notifyDataSetChanged();
                setUpdated();
            }
        };

        mSettingsRoomListAdapter = new SettingsRoomListAdapter(this, onSettingsRoomClickListener);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        RecyclerView recyclerView = findViewById(R.id.settings_room_activity_list_view);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(mSettingsRoomListAdapter);
        recyclerView.setItemAnimator(null);
        recyclerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        mProgressBarView = findViewById(R.id.personalization_activity_progress_bar);

        mUIInitialized = true;
    }

    private void postInitViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "postInitViews");
        }

        mUIPostInitialized = true;
    }

    private void onSaveClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSaveClick");
        }

        if (!mCanSave) {
            return;
        }

        mRoomConfig.setCallMode(mCallMode);
        mRoomConfig.setInvitationMode(mInvitationMode);
        mRoomConfig.setChatMode(mChatMode);
        mRoomConfig.setNotificationMode(mNotificationMode);

        mEditRoomService.updateRoomConfig(mRoom, mRoomConfig);
    }

    private void setUpdated() {
        if (DEBUG) {
            Log.d(LOG_TAG, "setUpdated");
        }

        if (!mUIInitialized) {
            return;
        }

        if (mRoomConfig.getNotificationMode() == mNotificationMode && mRoomConfig.getCallMode() == mCallMode && mRoomConfig.getInvitationMode() == mInvitationMode && mRoomConfig.getChatMode() == mChatMode) {
            if (!mCanSave) {
                return;
            }
            mCanSave = false;
            if (mMenu != null) {
                MenuItem saveMenuItem = mMenu.findItem(R.id.save_action);
                saveMenuItem.getActionView().setAlpha(0.5f);
                saveMenuItem.setEnabled(false);
            }
        } else {
            if (mCanSave) {
                return;
            }
            mCanSave = true;
            if (mMenu != null) {
                MenuItem saveMenuItem = mMenu.findItem(R.id.save_action);
                saveMenuItem.getActionView().setAlpha(1.0f);
                saveMenuItem.setEnabled(true);
            }
        }
    }
}
