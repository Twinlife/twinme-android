/*
 *  Copyright (c) 2018-2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui;

import android.app.AlertDialog;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinlife.DebugService;
import org.twinlife.twinlife.debug.DebugTwinlifeImpl;
import org.twinlife.twinme.TwinmeApplication;
import org.twinlife.twinme.TwinmeContext;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Group;
import org.twinlife.twinme.models.Originator;
import org.twinlife.twinme.services.ContactsService;
import org.twinlife.twinme.services.ServiceAssertPoint;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Activity to perform various debugging actions while the application is running.
 */

public class DebugActivity extends AbstractTwinmeActivity implements ContactsService.Observer {
    private static final String LOG_TAG = "DebugActivity";
    private static final boolean DEBUG = true;

    private ListView mLocales;
    private ContactsService mContactsService;
    private List<ConversationService.Conversation> mConversations;
    private TwinmeContext mTwinmeContext;
    private DebugService mDebugService;
    private final Map<UUID, Contact> mContacts = new HashMap<>();
    private final Map<UUID, Group> mGroups = new HashMap<>();
    // private TwinmeEngineImpl mTwinmeEngine;
    private int mNumber = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();

        mTwinmeContext = getTwinmeContext();
        mContactsService = new ContactsService(this, mTwinmeContext, this);
        mDebugService = DebugTwinlifeImpl.getDebugService();
    }

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }
        TwinmeApplication twinmeApp = (TwinmeApplication) getApplicationContext();
        TwinmeContext twinmeContext = twinmeApp.getTwinmeContext();

        // mTwinmeEngine = ((TwinmeApplicationImpl)twinmeApp).getTwinmeEngine();

        setContentView(R.layout.debug_activity);
        View backClickableView = findViewById(R.id.debug_activity_back_clickable_view);
        backClickableView.setOnClickListener(v -> onBackClick());

        final View reconnectView = findViewById(R.id.debug_reconnect_button);
        reconnectView.setOnClickListener(v -> {
            twinmeContext.disconnect();
            twinmeContext.connect();
        });

        final View disconnectView = findViewById(R.id.debug_disconnect_button);
        disconnectView.setOnClickListener(v -> twinmeContext.disconnect());

        final View sendMessagesView = findViewById(R.id.debug_send_messages_button);
        sendMessagesView.setOnClickListener(v -> sendAllConversations());

        final View deleteConversationsView = findViewById(R.id.debug_delete_conversations_button);
        deleteConversationsView.setOnClickListener(v -> deleteAllConversations());

        final View backupView = findViewById(R.id.debug_backup_button);
        backupView.setOnClickListener(v -> backupDatabase());

        final View restoreView = findViewById(R.id.debug_restore_button);
        restoreView.setOnClickListener(v -> restoreDatabase());
/*
        final View engineView = findViewById(R.id.debug_engine_button);
        engineView.setOnClickListener(v -> {
            engineTest();
        });
*/
        /*
        Set<UUID> list = twinmeContext.getAllTwincodes();
        Log.e(LOG_TAG, "Dump " + list.size() + " twincodes");
        for (UUID key : list) {
            Log.e(LOG_TAG, " " + key.toString());
        }
*/
        mLocales = findViewById(R.id.debug_select_locale_view);
        ArrayList<String> locales = new ArrayList<>();
        locales.add("ru");
        locales.add("fr");
        locales.add("en");
        locales.add("de");
        locales.add("ar");
        mLocales.setAdapter(new ArrayAdapter<>(DebugActivity.this, android.R.layout.simple_list_item_1, locales));
        mLocales.setOnItemClickListener((parent, view, position, id) -> {
            String name = ((TextView) view).getText().toString();
            final Locale ru = new Locale(name);
            Configuration newConfig = getBaseContext().getResources().getConfiguration();
            if (!newConfig.locale.equals(ru)) {
                newConfig.locale = ru;

                Locale.setDefault(ru);
                getBaseContext().getResources().updateConfiguration(newConfig, getResources().getDisplayMetrics());
            }
        });
    }

    private void deleteAllConversations() {
        if (mConversations != null) {
            for (ConversationService.Conversation conversation : mConversations) {
                getTwinmeContext().assertNotNull(ServiceAssertPoint.NULL_SUBJECT,  conversation.getContactId(), 144);

                long requestId = mContactsService.newOperation(0x1000000);
                if (DEBUG) {
                    Log.d(LOG_TAG, "ConversationService.deleteConversation: requestId=" + requestId + " conversationId=" + conversation.getId());
                }
                mTwinmeContext.getConversationService().deleteConversation(conversation.getSubject());
            }
            mConversations = null;
        }
        if (mGroups != null) {
            for (Group g : mGroups.values()) {
                long requestId = mContactsService.newOperation(0x10000000);
                // mTwinmeContext.getRepositoryService().deleteObject(requestId, g.getId(), g.getSchemaId());
            }
            mGroups.clear();
        }
    }

    private void sendAllConversations() {
        if (DEBUG) {
            Log.d(LOG_TAG, "sendAllConversations");
        }

        if (mConversations != null) {
            for (ConversationService.Conversation conversation : mConversations) {
                getTwinmeContext().assertNotNull(ServiceAssertPoint.NULL_SUBJECT,  conversation.getContactId(), 170);

                Originator c = conversation.isGroup() ? mGroups.get(conversation.getContactId()) : mContacts.get(conversation.getContactId());

                if (conversation.hasPermission(ConversationService.Permission.SEND_MESSAGE)) {
                    int msgId = mNumber;
                    for (int repeat = 0; repeat < 4; repeat++) {
                        long requestId = mContactsService.newOperation(0x1000000);
                        if (DEBUG) {
                            Log.d(LOG_TAG, "ConversationService.pushObject: requestId=" + requestId + " conversationId=" + conversation.getId());
                        }
                        Date d = new Date();
                        String msg;

                        msgId++;
                        if (c == null) {
                            msg = "4-Message " + msgId + " for unkown at " + d;
                        } else {
                            msg = "4-Message " + msgId + " for " + c.getName() + " at " + d;
                        }
                        mTwinmeContext.getConversationService().pushMessage(requestId, conversation, null,
                                null, msg, true, 0);
                    }
                }
            }
            mNumber += 100;
            mConversations = null;
        }
    }

    private void backupDatabase() {
        if (DEBUG) {
            Log.d(LOG_TAG, "backupDatabase");
        }

        mDebugService.backupDatabase();
    }

    private void restoreDatabase() {
        if (DEBUG) {
            Log.d(LOG_TAG, "restoreDatabase");
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.debug_restore_database_confirmation);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.application_yes, (dialog, id) -> {
            mDebugService.restoreDatabase();
            Log.e(LOG_TAG, "Stopping twinme because the database file was changed");
            finishAffinity();
        });
        builder.setNegativeButton(R.string.application_no, (dialog, id) -> {
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void engineTest() {

    }

    @Override
    public void onCreateContact(@NonNull Contact contact, @Nullable Bitmap avatar) {

    }

    @Override
    public void onUpdateContact(@NonNull Contact contact, @Nullable Bitmap avatar) {

    }

    @Override
    public void onDeleteContact(@NonNull UUID contactId) {

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
    public void onGetContacts(@NonNull List<Contact> contacts) {

        for (Contact c : contacts) {
            mContacts.put(c.getId(), c);
        }
    }
}
