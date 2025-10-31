/*
 *  Copyright (c) 2022-2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.settingsActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.Settings;
import org.twinlife.twinme.skin.EmojiSize;
import org.twinlife.twinme.ui.spaces.ConversationAppearanceActivity;
import org.twinlife.twinme.ui.spaces.SpaceAppearanceActivity;

public class ConversationSettingsActivity extends AbstractSettingsActivity {
    private static final String LOG_TAG = "ConversationSettings...";
    private static final boolean DEBUG = false;

    private RecyclerView mRecyclerView;
    private ConversationSettingsAdapter mConversationSettingsAdapter;

    private boolean mUIInitialized = false;
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

        initViews();
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
    public void onSettingClick(@NonNull UISetting<?> setting) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSettingClick");
        }
    }

    @Override
    public void onSettingClick(Settings.IntConfig intConfig) {

    }

    @Override
    public void onRingToneClick(@NonNull UISetting<String> setting) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRingToneClick");
        }
    }

    @Override
    public void onSettingChangeValue(Settings.BooleanConfig booleanConfig, boolean value) {

    }

    //
    // Private methods
    //

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.conversation_settings_activity);

        setStatusBarColor();
        setToolBar(R.id.conversation_settings_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);
        setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        setTitle(getString(R.string.application_appearance));
        applyInsets(R.id.conversation_settings_activity_layout, R.id.conversation_settings_activity_tool_bar, R.id.conversation_settings_activity_list_view, Design.TOOLBAR_COLOR, false);

        ConversationSettingsAdapter.OnConversationSettingsClickListener onConversationSettingsClickListener = new ConversationSettingsAdapter.OnConversationSettingsClickListener() {
            @Override
            public void onUpdateEmojiSize(EmojiSize emojiSize) {
                getTwinmeApplication().updateEmojiFontSize(emojiSize);
                Design.setupFont(ConversationSettingsActivity.this, getTwinmeApplication());
                mConversationSettingsAdapter.updateColor();
            }

            @Override
            public void onColorsAndBackgroundClick() {
                onConversationAppearanceClick();
            }
        };

        mConversationSettingsAdapter = new ConversationSettingsAdapter(this, onConversationSettingsClickListener);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        mRecyclerView = findViewById(R.id.conversation_settings_activity_list_view);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mConversationSettingsAdapter);
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        mUIInitialized = true;
    }

    private void postInitViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "postInitViews");
        }

        mUIPostInitialized = true;
    }

    private void onConversationAppearanceClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onConversationAppearance");
        }

        Intent intent = new Intent();
        intent.putExtra(Intents.INTENT_ONLY_CONVERSATION, true);
        intent.putExtra(Intents.INTENT_DEFAULT_SPACE_SETTINGS, true);
        intent.setClass(this, ConversationAppearanceActivity.class);
        startActivity(intent);
    }

    @Override
    public void updateColor() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateColor");
        }

        super.updateColor();

        if (!mUIInitialized) {
            return;
        }

        setToolBar(R.id.conversation_settings_activity_tool_bar);
        setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        setStatusBarColor();
        applyInsets(R.id.conversation_settings_activity_layout, R.id.conversation_settings_activity_tool_bar, R.id.conversation_settings_activity_list_view, Design.TOOLBAR_COLOR, false);
        mRecyclerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        mConversationSettingsAdapter.updateColor();
    }
}