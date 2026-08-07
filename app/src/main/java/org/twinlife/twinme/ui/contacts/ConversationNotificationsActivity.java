/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.contacts;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.models.Capabilities;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Group;
import org.twinlife.twinme.services.EditContactCapabilitiesService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.conversations.MenuConversationShortcutView;
import org.twinlife.twinme.ui.premiumServicesActivity.PremiumFeatureConfirmView;
import org.twinlife.twinme.ui.premiumServicesActivity.UIPremiumFeature;
import org.twinlife.twinme.ui.privacyActivity.UITimeout;
import org.twinlife.twinme.ui.settingsActivity.MenuSelectValueView;
import org.twinlife.twinme.utils.AbstractBottomSheetView;

import java.util.UUID;

public class ConversationNotificationsActivity extends AbstractTwinmeActivity implements EditContactCapabilitiesService.Observer {
    private static final String LOG_TAG = "ConversationNotificationsActivity";
    private static final boolean DEBUG = false;

    @Nullable
    private Contact mContact;
    @Nullable
    private Group mGroup;
    private boolean mNotificationsReactionsEnabled;
    private boolean mSilentModeEnabled;
    private long mSilentExpirationTimestamp = -1;
    private boolean mDiscreetModeEnabled;
    private boolean mUpdated = false;

    @Nullable
    private Capabilities mCapabilities;

    private boolean mUIInitialized = false;

    private ConversationNotificationsAdapter mConversationNotificationsAdapter;

    private EditContactCapabilitiesService mEditCapabiltiesService;

    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        mEditCapabiltiesService = new EditContactCapabilitiesService(this, getTwinmeContext(), this);

        Intent intent = getIntent();
        String contactId = intent.getStringExtra(Intents.INTENT_CONTACT_ID);
        String groupId = intent.getStringExtra(Intents.INTENT_GROUP_ID);
        if (contactId != null) {
            mEditCapabiltiesService.getContact(UUID.fromString(contactId));
        } else if (groupId != null) {
            mEditCapabiltiesService.getGroup(UUID.fromString(groupId));
        } else {
            finish();
        }

        initViews();
    }

    //
    // Override Activity methods
    //

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        mEditCapabiltiesService.dispose();

        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (DEBUG) {
            Log.d(LOG_TAG, "onPause");
        }

        if (!mUpdated) {
            return;
        }

        if (mContact != null && mCapabilities != null) {
            mCapabilities.setCapDiscreet(mDiscreetModeEnabled);
            mEditCapabiltiesService.updateContact(mContact, mCapabilities, null);

            mContact.putBoolean(MenuConversationShortcutView.PROPERTY_CONVERSATION_NOTIFICATION_REACTION, mNotificationsReactionsEnabled, getTwinmeContext());
            mContact.putBoolean(MenuConversationShortcutView.PROPERTY_CONVERSATION_SILENT_MODE, mSilentModeEnabled, getTwinmeContext());
            mContact.putLong(MenuConversationShortcutView.PROPERTY_CONVERSATION_SILENT_MODE_EXPIRATION, mSilentExpirationTimestamp, getTwinmeContext());
        } else if (mGroup != null) {
            mGroup.putBoolean(MenuConversationShortcutView.PROPERTY_CONVERSATION_NOTIFICATION_REACTION, mNotificationsReactionsEnabled, getTwinmeContext());
            mGroup.putBoolean(MenuConversationShortcutView.PROPERTY_CONVERSATION_SILENT_MODE, true, getTwinmeContext());
            mGroup.putLong(MenuConversationShortcutView.PROPERTY_CONVERSATION_SILENT_MODE_EXPIRATION, mSilentExpirationTimestamp, getTwinmeContext());
        }
    }

    @Override
    public void onGetContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContact: contact=" + contact);
        }

        mContact = contact;

        mSilentModeEnabled = contact.getBoolean(MenuConversationShortcutView.PROPERTY_CONVERSATION_SILENT_MODE, false);
        mNotificationsReactionsEnabled = contact.getBoolean(MenuConversationShortcutView.PROPERTY_CONVERSATION_NOTIFICATION_REACTION, true);
        mSilentExpirationTimestamp = contact.getLong(MenuConversationShortcutView.PROPERTY_CONVERSATION_SILENT_MODE_EXPIRATION, 0);

        long currentTimeMillis = System.currentTimeMillis() / 1000;
        if (mSilentExpirationTimestamp > 0 && mSilentExpirationTimestamp < currentTimeMillis) {
            mSilentModeEnabled = false;
        }

        final String capabilities = contact.getIdentityCapabilities().toAttributeValue();
        mCapabilities = capabilities == null ? new Capabilities() : new Capabilities(capabilities);

        mDiscreetModeEnabled = mCapabilities.hasDiscreet();

        if (mUIInitialized) {
            mConversationNotificationsAdapter.updateItems();
        }
    }

    @Override
    public void onGetContactNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContactNotFound");
        }

        finish();
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
            Log.d(LOG_TAG, "onGetGroup: group=" + group);
        }

        mGroup = group;

        mSilentModeEnabled = group.getBoolean(MenuConversationShortcutView.PROPERTY_CONVERSATION_SILENT_MODE, false);
        mNotificationsReactionsEnabled = group.getBoolean(MenuConversationShortcutView.PROPERTY_CONVERSATION_NOTIFICATION_REACTION, true);
        mSilentExpirationTimestamp = group.getLong(MenuConversationShortcutView.PROPERTY_CONVERSATION_SILENT_MODE_EXPIRATION, 0);

        long currentTimeMillis = System.currentTimeMillis() / 1000;
        if (mSilentExpirationTimestamp > 0 && mSilentExpirationTimestamp < currentTimeMillis) {
            mSilentModeEnabled = false;
        }

        if (mUIInitialized) {
            mConversationNotificationsAdapter.updateItems();
        }
    }

    @Override
    public void onGetGroupNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetGroupNotFound");
        }

        finish();
    }

    protected void onSelectSilentDuration() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSelectSilentDuration");
        }

        showSilentModeDuration();
    }

    protected boolean isNotificationsReactionsEnabled() {
        if (DEBUG) {
            Log.d(LOG_TAG, "isNotificationsReactionsEnabled");
        }

        return mNotificationsReactionsEnabled;
    }

    protected boolean isSilentModeEnabled() {
        if (DEBUG) {
            Log.d(LOG_TAG, "isSilentModeEnabled");
        }

        return mSilentModeEnabled;
    }

    protected long getSilentModeExpiration() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getSilentModeExpiration");
        }

        return mSilentExpirationTimestamp != -1 ? mSilentExpirationTimestamp * 1000 : mSilentExpirationTimestamp;
    }

    protected boolean isDiscreetModeEnabled() {
        if (DEBUG) {
            Log.d(LOG_TAG, "isDiscreetModeEnabled");
        }

        return mDiscreetModeEnabled;
    }

    protected void onSettingChangeValue(UIConversationNotificationsItem item, boolean value) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSettingChangeValue: item=" + item + " value=" + value);
        }

        switch (item.getType()) {
            case DISPLAY_NOTIFICATIONS_REACTIONS:
                mNotificationsReactionsEnabled = value;
                break;

            case SILENT_MODE:
                mSilentModeEnabled = value;

                if (mSilentModeEnabled) {
                    showSilentModeDuration();
                }
                break;

            case DISCREET_MODE:
                mDiscreetModeEnabled = value;
                break;

            default:
                break;
        }

        mUpdated = true;
        mConversationNotificationsAdapter.updateItems();
    }

    public void showPremiumFeatureAlert(UIPremiumFeature.FeatureType featureType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "showPremiumFeatureAlert");
        }

        ViewGroup viewGroup = findViewById(R.id.conversation_notifications_activity_layout);

        PremiumFeatureConfirmView premiumFeatureConfirmView = new PremiumFeatureConfirmView(this, null);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        premiumFeatureConfirmView.setLayoutParams(layoutParams);
        premiumFeatureConfirmView.initWithPremiumFeature(new UIPremiumFeature(this, featureType));

        AbstractBottomSheetView.Observer observer = new AbstractBottomSheetView.Observer() {
            @Override
            public void onConfirmClick() {
                premiumFeatureConfirmView.redirectStore();
            }

            @Override
            public void onCancelClick() {
                premiumFeatureConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onDismissClick() {
                premiumFeatureConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                viewGroup.removeView(premiumFeatureConfirmView);
                setStatusBarColor();
            }
        };
        premiumFeatureConfirmView.setObserver(observer);

        viewGroup.addView(premiumFeatureConfirmView);
        premiumFeatureConfirmView.show();

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        setContentView(R.layout.conversation_notifications_activity);

        setStatusBarColor();
        setToolBar(R.id.conversation_notifications_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);
        setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        setTitle(getString(R.string.notifications_view_title));
        applyInsets(R.id.conversation_notifications_activity_layout, R.id.conversation_notifications_activity_tool_bar, R.id.conversation_notifications_activity_list_view, Design.TOOLBAR_COLOR, false);

        mConversationNotificationsAdapter = new ConversationNotificationsAdapter(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        RecyclerView recyclerView = findViewById(R.id.conversation_notifications_activity_list_view);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(mConversationNotificationsAdapter);
        recyclerView.setItemAnimator(null);
        recyclerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        mProgressBarView = findViewById(R.id.conversation_notifications_activity_progress_bar);

        mUIInitialized = true;
    }

    private void showSilentModeDuration() {
        if (DEBUG) {
            Log.d(LOG_TAG, "showSilentModeDuration");
        }

        ViewGroup viewGroup = findViewById(R.id.conversation_notifications_activity_layout);

        MenuSelectValueView menuSelectValueView = new MenuSelectValueView(this, null);
        menuSelectValueView.setActivity(this);
        menuSelectValueView.setObserver(new MenuSelectValueView.Observer() {
            @Override
            public void onCloseMenuAnimationEnd() {
                viewGroup.removeView(menuSelectValueView);
                setStatusBarColor();
            }

            @Override
            public void onSelectValue(int value) {

            }

            @Override
            public void onSelectTimeout(UITimeout timeout) {

                long expiration;
                if (timeout.getDelay() > 0) {
                    long expirationDate = System.currentTimeMillis() + (timeout.getDelay() * 1000L);
                    expiration = expirationDate / 1000;
                } else {
                    expiration = timeout.getDelay();
                }

                mSilentExpirationTimestamp = expiration;

                mUpdated = true;
                menuSelectValueView.animationCloseMenu();
                mConversationNotificationsAdapter.updateItems();
            }
        });

        viewGroup.addView(menuSelectValueView);
        menuSelectValueView.openMenu(MenuSelectValueView.MenuType.SILENT_MODE_DURATION, 0);

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }
}
