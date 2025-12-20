/*
 *  Copyright (c) 2020-2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui.settingsActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.Settings;
import org.twinlife.twinme.utils.AbstractBottomSheetView;
import org.twinlife.twinme.utils.DefaultConfirmView;

public class SoundsSettingsActivity extends AbstractSettingsActivity {
    private static final String LOG_TAG = "SoundsSettingsActivity";
    private static final boolean DEBUG = false;

    private static final int REQUEST_SELECT_TONE = 2;
    private static final String SETTING_KEY = "settingKey";
    private static final String CHANNEL_NOTIF = "notification-10-messages";

    private SoundsSettingsAdapter mSoundsSettingsAdapter;

    private boolean mUIInitialized = false;
    private boolean mUIPostInitialized = false;
    private boolean mRefreshNeeded = false;

    enum SoundSetting {
        SOUND_NOTIFICATION,
        SOUND_AUDIO,
        SOUND_VIDEO
    }

    @Nullable
    private SoundSetting mSettingKind;

    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mSettingKind = (SoundSetting) savedInstanceState.getSerializable(SETTING_KEY);
        }

        initViews();
    }

    @Override
    protected void onPause() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPause");
        }

        super.onPause();
    }

    @Override
    protected void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        super.onResume();

        if (mUIPostInitialized && mRefreshNeeded) {
            mRefreshNeeded = false;
            mSoundsSettingsAdapter.notifyDataSetChanged();
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

        if (mSettingKind != null) {
            outState.putSerializable(SETTING_KEY, mSettingKind);
        }
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
    public void onSettingClick(UISetting<?> setting) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSettingClick");
        }

        if (setting.getTypeSetting() == UISetting.TypeSetting.SYSTEM) {
            onSystemPreferencesClick();
        } else if (setting.getTypeSetting() == UISetting.TypeSetting.SYSTEM_MESSAGE) {
            onSystemMessagePreferencesClick();
        } else if (setting.getTypeSetting() == UISetting.TypeSetting.RESET) {
            onResetPreferencesClick();
        }
    }

    @Override
    public void onRingToneClick(@NonNull UISetting<String> setting) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRingToneClick: setting=" + setting);
        }

        Uri defaultUri = null;
        String title = "";
        int ringToneType = RingtoneManager.TYPE_RINGTONE;
        if (setting.isSetting(Settings.notificatonRingtone)) {
            defaultUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.notification_ringtone);
            title = getString(R.string.settings_activity_chat_ringtone_title);
            ringToneType = RingtoneManager.TYPE_NOTIFICATION;
            mSettingKind = SoundSetting.SOUND_NOTIFICATION;

        } else if (setting.isSetting(Settings.audioCallRingtone)) {
            defaultUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.audio_call_ringtone);
            title = getString(R.string.settings_activity_audio_call_notification_ringtone_title);
            mSettingKind = SoundSetting.SOUND_AUDIO;

        } else if (setting.isSetting(Settings.videoCallRingtone)) {
            defaultUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.video_call_ringtone);
            title = getString(R.string.settings_activity_video_call_notification_ringtone_title);
            mSettingKind = SoundSetting.SOUND_VIDEO;
        }

        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, ringToneType);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, title);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, getExistingRingTone(setting));
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, defaultUri);
        startActivityForResult(intent, REQUEST_SELECT_TONE);
    }

    @Override
    public void onSettingChangeValue(@NonNull UISetting<Boolean> setting, boolean value) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSettingChangeValue: setting=" + setting + " value=" + value);
        }

        super.onSettingChangeValue(setting, value);

        // When we enable the ringtone notification, check that the ringtone to play is not
        // empty and set it to the Twinme default ringtone if this happens.
        Settings.StringConfig ringtoneKey = null;
        int defaultTone = 0;
        if (setting.isSetting(Settings.notificatonRingtone)) {
            ringtoneKey = Settings.notificatonRingtone; // "settings_activity_notification_ringtone";
            defaultTone = R.raw.notification_ringtone;

        } else if (setting.isSetting(Settings.audioCallRingtone)) {
            ringtoneKey = Settings.audioCallRingtone; // "settings_activity_audio_call_ringtone";
            defaultTone = R.raw.audio_call_ringtone;

        } else if (setting.isSetting(Settings.videoCallRingtone)) {
            ringtoneKey = Settings.videoCallRingtone; // "settings_activity_video_call_ringtone";
            defaultTone = R.raw.video_call_ringtone;
        }

        // If notifications are enabled, check the ringtone to make sure it is not empty.
        if (ringtoneKey != null) {
            final String ringtone = ringtoneKey.getString();
            if (value && (ringtone == null || ringtone.isEmpty())) {
                ringtoneKey.setString("android.resource://" + getPackageName() + "/" + defaultTone);
                ringtoneKey.save();
            }
        }

        if (mUIPostInitialized) {
            mSoundsSettingsAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onActivityResult: requestCode=" + requestCode + " resultCode=" + resultCode + " intent=" + intent);
        }

        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_OK && requestCode == REQUEST_SELECT_TONE && mSettingKind != null) {
            Uri uri = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);

            if (uri != null) {
                // Refuse a file:// because this is not allowed on SDK 24 and higher.
                // We will get the FileUriExposedException when the notification is created.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && uri.toString().startsWith("file://")) {
                    return;
                }

                Settings.StringConfig soundSetting;
                Settings.BooleanConfig enableSetting;
                switch (mSettingKind) {
                    case SOUND_NOTIFICATION:
                        soundSetting = Settings.notificatonRingtone;
                        enableSetting = Settings.notificationRingEnabled;
                        break;

                    case SOUND_AUDIO:
                        soundSetting = Settings.audioCallRingtone;
                        enableSetting = Settings.audioRingEnabled;
                        break;

                    case SOUND_VIDEO:
                        soundSetting = Settings.videoCallRingtone;
                        enableSetting = Settings.videoRingEnabled;
                        break;

                    default:
                        enableSetting = null;
                        soundSetting = null;
                        break;
                }

                // If the ringtone is changed to "Silent", check that the ringtone pref is disabled.
                // Otherwise, if the ringtone is changed, make sure the notification is turned on.
                if (enableSetting != null) {
                    soundSetting.setString(uri.toString());
                    final String value = soundSetting.getString();
                    final boolean enable = enableSetting.getBoolean();

                    // Keep the enable and "Silent" consistent.  If the boolean value is changed
                    // we will be called again and the checkbox will also be updated.
                    if (enable && (value == null || value.isEmpty())) {
                        enableSetting.setBoolean(false);

                    } else if (!enable && value != null && value.length() > 0) {
                        enableSetting.setBoolean(true);

                    }
                    soundSetting.save();
                }

                mRefreshNeeded = true;
            }
        }
        mSettingKind = null;
    }

    //
    // Private methods
    //

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        setContentView(R.layout.sounds_settings_activity);

        setStatusBarColor();
        setToolBar(R.id.sounds_settings_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);
        setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        setTitle(getString(R.string.notifications_fragment_title));
        applyInsets(R.id.sounds_settings_activity_layout, R.id.sounds_settings_activity_tool_bar, R.id.sounds_settings_activity_list_view, Design.TOOLBAR_COLOR, false);

        mSoundsSettingsAdapter = new SoundsSettingsAdapter(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        RecyclerView settingsRecyclerView = findViewById(R.id.sounds_settings_activity_list_view);
        settingsRecyclerView.setLayoutManager(linearLayoutManager);
        settingsRecyclerView.setAdapter(mSoundsSettingsAdapter);
        settingsRecyclerView.setItemAnimator(null);
        settingsRecyclerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        mProgressBarView = findViewById(R.id.sounds_settings_activity_progress_bar);

        mUIInitialized = true;
    }

    private void postInitViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "postInitViews");
        }

        mUIPostInitialized = true;
    }

    private void onResetPreferencesClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResetPreferencesClick");
        }

        ViewGroup viewGroup = findViewById(R.id.sounds_settings_activity_layout);

        DefaultConfirmView defaultConfirmView = new DefaultConfirmView(this, null);
        defaultConfirmView.setTitle(getString(R.string.settings_activity_reset_preferences_title));
        defaultConfirmView.setMessage(getString(R.string.settings_activity_reset_preferences_message));
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
                    getTwinmeApplication().resetPreferences();
                    finish();
                }
            }
        };
        defaultConfirmView.setObserver(observer);
        viewGroup.addView(defaultConfirmView);
        defaultConfirmView.show();

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }

    private void onSystemPreferencesClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSystemPreferencesClick");
        }

        try {
            Intent intent = new Intent();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.setAction(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra("android.provider.extra.APP_PACKAGE", getPackageName());
            } else {
                intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                intent.putExtra("app_package", getPackageName());
                intent.putExtra("app_uid", getApplicationInfo().uid);
            }

            // Use the Android startActivity because we want to handle the ActivityNotFoundException.
            startActivity(intent, null);

        } catch (ActivityNotFoundException exception) {
            // We have seen some Android that don't provide the ACTION_APP_NOTIFICATION_SETTINGS.
            Intent intent = new Intent();
            intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);

            // For this second try, use the Twinme startActivity to handle the error.
            startActivity(intent);
        }
    }

    private void onSystemMessagePreferencesClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSystemMessagePreferencesClick");
        }

        try {
            Intent intent = new Intent();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.setAction(android.provider.Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                intent.putExtra(android.provider.Settings.EXTRA_CHANNEL_ID, CHANNEL_NOTIF);
                intent.putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, getPackageName());
            }

            startActivity(intent);
        } catch (ActivityNotFoundException exception) {
            // We have seen some Android that don't provide the ACTION_APP_NOTIFICATION_SETTINGS.
            Intent intent = new Intent();
            intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);

            // For this second try, use the Twinme startActivity to handle the error.
            startActivity(intent);
        }
    }

    private Uri getExistingRingTone(@NonNull UISetting<String> setting) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getExistingRingTone: setting=" + setting);
        }

        final String value = setting.getString();
        Uri ringtoneUri = null;
        if (value != null) {
            ringtoneUri = Uri.parse(value);
        }

        Uri defaultUri;
        if (setting.isSetting(Settings.audioCallRingtone) || setting.isSetting(Settings.videoCallRingtone)) {
            defaultUri = android.provider.Settings.System.DEFAULT_RINGTONE_URI;
        } else {
            defaultUri = android.provider.Settings.System.DEFAULT_NOTIFICATION_URI;
        }

        if (ringtoneUri != null) {
            return ringtoneUri;
        } else {
            return defaultUri;
        }
    }
}
