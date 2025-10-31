/*
 *  Copyright (c) 2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.cleanupActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.percentlayout.widget.PercentRelativeLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Group;
import org.twinlife.twinme.services.ResetConversationService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.utils.AbstractConfirmView;

import java.util.UUID;

public class TypeCleanUpActivity extends AbstractTwinmeActivity implements ResetConversationService.Observer {
    private static final String LOG_TAG = "TypeCleanUpActivity";
    private static final boolean DEBUG = false;

    private boolean mUIInitialized = false;
    private boolean mUIPostInitialized = false;

    private String mContactId;
    private String mGroupId;
    private String mSpaceId;

    private Contact mContact;
    private Group mGroup;

    private ResetConversationService mResetConversationService;

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

        Intent intent = getIntent();
        mContactId = intent.getStringExtra(Intents.INTENT_CONTACT_ID);
        mGroupId = intent.getStringExtra(Intents.INTENT_GROUP_ID);
        mSpaceId = intent.getStringExtra(Intents.INTENT_SPACE_ID);

        mResetConversationService = new ResetConversationService(this, getTwinmeContext(), this);
    }

    @Override
    protected void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        super.onResume();

        // Get the contact and group after we resume so that we can refresh the information.
        if (mContactId != null) {
            mResetConversationService.getContact(UUID.fromString(mContactId));
        } else if (mGroupId != null) {
            mResetConversationService.getGroup(UUID.fromString(mGroupId));
        }
    }

    //
    // Override Activity methods
    //

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
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        super.onDestroy();

        mResetConversationService.dispose();
    }

    public boolean showResetConversation() {
        if (DEBUG) {
            Log.d(LOG_TAG, "showResetConversation");
        }

        return mContactId == null && mGroupId == null;
    }

    //
    // Override ResetConversationService.Observer methods
    //

    @Override
    public void onResetConversation(@NonNull ConversationService.Conversation conversation, @NonNull ConversationService.ClearMode clearMode) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResetConversation: conversation=" + conversation);
        }

        finish();
    }

    @Override
    public void onGetGroup(@NonNull Group group) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetGroup: " + group);
        }

        mGroup = group;
    }

    @Override
    public void onGetGroupNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetGroupNotFound");
        }

        finish();
    }

    @Override
    public void onGetContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContact: " + contact);
        }

        mContact = contact;
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
            Log.d(LOG_TAG, "onUpdateContact: " + contact);
        }
    }

    //
    // Private methods
    //

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.type_cleanup_activity);

        setStatusBarColor();
        setToolBar(R.id.type_cleanup_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);
        setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        setTitle(getString(R.string.show_contact_activity_cleanup));
        applyInsets(R.id.type_cleanup_activity_layout, R.id.type_cleanup_activity_tool_bar, R.id.type_cleanup_activity_list_view, Design.TOOLBAR_COLOR, false);

        TypeCleanUpAdapter.OnTypeCleanupClickListener onTypeCleanupClickListener = new TypeCleanUpAdapter.OnTypeCleanupClickListener() {
            @Override
            public void onLocalCleanUpClick() {
                onCleanUpClick(true);
            }

            @Override
            public void onBothCleanUpClick() {
                onCleanUpClick(false);
            }

            @Override
            public void onResetConversationClick() {
                onResetClick();
            }
        };

        TypeCleanUpAdapter typeCleanUpAdapter = new TypeCleanUpAdapter(this, onTypeCleanupClickListener);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        RecyclerView recyclerView = findViewById(R.id.type_cleanup_activity_list_view);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(typeCleanUpAdapter);
        recyclerView.setItemAnimator(null);
        recyclerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        mUIInitialized = true;
    }

    private void postInitViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "postInitViews");
        }

        mUIPostInitialized = true;
    }

    private void onCleanUpClick(boolean localCleanUpOnly) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCleanUpClick");
        }

        Intent intent = new Intent(this, CleanUpActivity.class);

        if (mContactId != null) {
            intent.putExtra(Intents.INTENT_CONTACT_ID, mContactId);
        } else if (mGroupId != null) {
            intent.putExtra(Intents.INTENT_GROUP_ID, mGroupId);
        } else if (mSpaceId != null) {
            intent.putExtra(Intents.INTENT_SPACE_ID, mSpaceId);
        }

        intent.putExtra(Intents.INTENT_LOCAL_CLEANUP, localCleanUpOnly);
        startActivity(intent);
    }

    private void onResetClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResetClick");
        }

        if (mContact != null) {
            mResetConversationService.getImage(mContact, this::openResetConversationConfirmView);
        } else if (mGroup != null) {
            mResetConversationService.getImage(mGroup, this::openResetConversationConfirmView);
        }
    }

    private void openResetConversationConfirmView(@Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "openResetConversationConfirmView: " + avatar);
        }

        Spanned message = Html.fromHtml(getString(R.string.main_activity_reset_conversation_message));
        if (mGroup != null) {
            if (mGroup.isOwner()) {
                message = Html.fromHtml(getString(R.string.main_activity_reset_group_conversation_admin_message));
            } else {
                message = Html.fromHtml(getString(R.string.main_activity_reset_group_conversation_message));
            }
        }

        PercentRelativeLayout percentRelativeLayout = findViewById(R.id.type_cleanup_activity_layout);

        ResetConversationConfirmView resetConversationConfirmView = new ResetConversationConfirmView(this, null);
        PercentRelativeLayout.LayoutParams layoutParams = new PercentRelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        resetConversationConfirmView.setLayoutParams(layoutParams);
        resetConversationConfirmView.setAvatar(avatar, avatar == null || avatar.equals(getTwinmeApplication().getDefaultGroupAvatar()));
        resetConversationConfirmView.setMessage(message.toString());

        AbstractConfirmView.Observer observer = new AbstractConfirmView.Observer() {
            @Override
            public void onConfirmClick() {
                mResetConversationService.resetConversation();
                resetConversationConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCancelClick() {
                resetConversationConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onDismissClick() {
                resetConversationConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                percentRelativeLayout.removeView(resetConversationConfirmView);
                setStatusBarColor();
            }
        };
        resetConversationConfirmView.setObserver(observer);

        percentRelativeLayout.addView(resetConversationConfirmView);
        resetConversationConfirmView.show();

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }
}
