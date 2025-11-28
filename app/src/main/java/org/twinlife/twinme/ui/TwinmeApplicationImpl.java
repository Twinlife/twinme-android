/*
 *  Copyright (c) 2012-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Romain Kolb (romain.kolb@skyrock.com)
 */

package org.twinlife.twinme.ui;

import static org.twinlife.twinme.utils.Utils.getScaledAvatar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import org.twinlife.device.android.twinme.BuildConfig;
import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.BaseService;
import org.twinlife.twinlife.ConnectionStatus;
import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinlife.DisplayCallsMode;
import org.twinlife.twinlife.ImageId;
import org.twinlife.twinlife.JobService;
import org.twinlife.twinlife.Twincode;
import org.twinlife.twinlife.util.Logger;
import org.twinlife.twinme.NotificationCenter;
import org.twinlife.twinme.TwinmeContext;
import org.twinlife.twinme.calls.CallService;
import org.twinlife.twinme.configuration.Configuration;
import org.twinlife.twinme.glide.FileDescriptorLoader;
import org.twinlife.twinme.glide.MediaInfoImageLoader;
import org.twinlife.twinme.glide.MediaInfoVideoThumbnailLoader;
import org.twinlife.twinme.glide.TwinlifeImageLoader;
import org.twinlife.twinme.models.Profile;
import org.twinlife.twinme.models.Space;
import org.twinlife.twinme.models.SpaceSettings;
import org.twinlife.twinme.notificationCenter.NotificationCenterImpl;
import org.twinlife.twinme.services.AdminService;
import org.twinlife.twinme.services.PeerService;
import org.twinlife.twinme.skin.DisplayMode;
import org.twinlife.twinme.skin.EmojiSize;
import org.twinlife.twinme.skin.FontSize;
import org.twinlife.twinme.ui.spaces.SpaceSettingProperty;
import org.twinlife.twinme.utils.AppStateInfo;
import org.twinlife.twinme.utils.FileInfo;
import org.twinlife.twinme.utils.InCallInfo;
import org.twinlife.twinme.utils.TwinmeActivityImpl;
import org.twinlife.twinme.utils.coachmark.CoachMark;
import org.twinlife.twinme.utils.coachmark.CoachMarkManager;
import org.twinlife.twinme.utils.update.LastVersion;
import org.twinlife.twinme.utils.update.LastVersionAsyncTask;
import org.twinlife.twinme.utils.update.LastVersionImpl;

import java.io.File;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class TwinmeApplicationImpl extends org.twinlife.twinme.TwinmeApplicationImpl implements TwinmeApplication, JobService.Observer {
    private static final String LOG_TAG = "TwinmeApplicationImpl";
    private static final boolean DEBUG = false;

    private static final long CALL_QUALITY_MIN_DURATION = 5 * 60;
    private static final long CALL_QUALITY_ASK_FREQUENCY = 10;
    private static final long CALL_QUALITY_INTERVAL_DATE = 10 * 60 * 60 * 24;

    private static final int SHOW_CLICK_TO_CALL_DESCRIPTION_MAX = 5;

    private Bitmap mAnonymousAvatar;
    private Bitmap mDefaultAvatar;
    private Bitmap mDefaultGroupAvatar;
    private NotificationCenterImpl mNotificationCenter;

    private State mState = State.STARTING;

    private volatile InCallInfo mInCallInfo;
    @Nullable
    private volatile AppStateInfo mAppInfo;

    private AdminService mAdminService;
    private JobService mJobService;
    private CoachMarkManager mCoachMarkManager;
    private boolean mShowConnectedMessage = true;
    private Date mAppBackgroundDate;
    private boolean mIsInBackground = true;
    private WeakReference<TwinmeActivityImpl> mCurrentActivity;

    private boolean mShowLockScreen = false;

    private static WeakReference<TwinmeApplicationImpl> sInstance;

    /**
     * Get the TwinmeApplication instance from the context.
     * <p>
     * We have found that the Activity.getApplication() sometimes does not return the expected TwinmeApplication instance.
     * We are having ClassCastException when we try to convert it to a TwinmeApplication class desipite a correct
     * setup in the Android manifest.
     *
     * @param context the context.
     * @return the Twinme application instance.
     */
    public static TwinmeApplicationImpl getInstance(@Nullable Context context) {

        for (int retry = 0; retry < 10; retry++) {
            if (context instanceof TwinmeApplicationImpl) {
                return (TwinmeApplicationImpl) context;
            }

            if (sInstance != null) {
                TwinmeApplicationImpl app = sInstance.get();
                if (app != null) {
                    return app;
                }
            }

            if (context != null) {
                Context appContext = context.getApplicationContext();
                if (appContext instanceof TwinmeApplicationImpl) {
                    return (TwinmeApplicationImpl) appContext;
                }
            }

            // Try to wait to let Android initialize the application (not sure it will help...).
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {

            }
        }
        return null;
    }

    //
    // Implement TwinmeApplication interface
    //

    @Override
    @NonNull
    public String getAnonymousName() {

        return getString(R.string.application_unknown_name);
    }

    @Override
    @NonNull
    public Bitmap getAnonymousAvatar() {

        if (mAnonymousAvatar == null) {
            mAnonymousAvatar = getScaledAvatar(getResources(), R.drawable.anonymous_avatar);
        }
        return mAnonymousAvatar;
    }

    @Override
    @NonNull
    public Bitmap getDefaultAvatar() {

        if (mDefaultAvatar == null) {
            mDefaultAvatar = getScaledAvatar(getResources(), R.drawable.default_avatar);
        }
        return mDefaultAvatar;
    }

    @Override
    @NonNull
    public Bitmap getDefaultGroupAvatar() {

        if (mDefaultGroupAvatar == null) {
            mDefaultGroupAvatar = getScaledAvatar(getResources(), R.drawable.anonymous_group_avatar);
        }
        return mDefaultGroupAvatar;
    }

    @Override
    public void setDefaultProfile(@Nullable Profile profile) {

    }

    @Override
    @NonNull
    public NotificationCenter newNotificationCenter(@NonNull TwinmeContext twinmeContext) {

        mNotificationCenter = new NotificationCenterImpl(this, this, twinmeContext);
        return mNotificationCenter;
    }

    @Override
    public void resetPreferences() {
        if (DEBUG) {
            Log.d(LOG_TAG, "resetPreferences");
        }

        Settings.soundEnabled.reset();
        Settings.audioVibration.reset();
        Settings.videoVibration.reset();
        Settings.notificationVibration.reset();
        Settings.audioRingEnabled.reset();
        Settings.videoRingEnabled.reset();
        Settings.notificationRingEnabled.reset();
        Settings.notificatonRingtone.reset();
        Settings.audioCallRingtone.reset();
        Settings.videoCallRingtone.reset().save();
        mNotificationCenter.resetNotificationChannels();
    }

    public static int errorToMessageId(BaseService.ErrorCode errorCode) {

        int message;
        switch (errorCode) {
            case BAD_REQUEST:
            case FEATURE_NOT_IMPLEMENTED:
            case SERVER_ERROR:
            case LIBRARY_ERROR:
                message = R.string.fatal_error_activity_error_code_message;
                break;

            case FEATURE_NOT_SUPPORTED_BY_PEER:
                message = R.string.conversation_activity_feature_not_supported_by_peer;
                break;

            case WRONG_LIBRARY_CONFIGURATION:
            case LIBRARY_TOO_OLD:
                message = R.string.application_wrong_configuration;
                break;

            case SERVICE_UNAVAILABLE:
            case TWINLIFE_OFFLINE:
                message = R.string.application_not_connected;
                break;

            case WEBRTC_ERROR:
                message = R.string.application_webrtc_failure;
                break;

            case NO_STORAGE_SPACE:
                message = R.string.application_error_no_storage_space;
                break;

            case FILE_NOT_FOUND:
                message = R.string.application_error_file_not_found;
                break;

            case FILE_NOT_SUPPORTED:
                message = R.string.application_error_media_not_supported;
                break;

            case DATABASE_ERROR:
                message = R.string.application_database_error;
                break;

            case ACCOUNT_DELETED:
                message = R.string.application_account_deleted;
                break;

            default:
                message = R.string.application_operation_failure;
                break;
        }

        return message;
    }

    @Override
    public void checkLastVersion(LastVersion mLastVersion) {
        LastVersionAsyncTask lastVersionAsyncTask = new LastVersionAsyncTask(this, mLastVersion, BuildConfig.CHECK_VERSION_URL);
        lastVersionAsyncTask.execute();
    }

    @Override
    @NonNull
    public String errorToString(BaseService.ErrorCode errorCode) {

        int message;
        switch (errorCode) {
            case BAD_REQUEST:
                message = R.string.application_operation_failure;
                break;

            case FEATURE_NOT_IMPLEMENTED:
                message = R.string.application_operation_failure;
                break;

            case FEATURE_NOT_SUPPORTED_BY_PEER:
                message = R.string.application_operation_failure;
                break;

            case ITEM_NOT_FOUND:
                message = R.string.application_operation_failure;
                break;

            case FILE_NOT_FOUND:
                message = R.string.application_error_file_not_found;
                break;

            case FILE_NOT_SUPPORTED:
                message = R.string.application_error_media_not_supported;
                break;

            case LIBRARY_ERROR:
                message = R.string.application_operation_failure;
                break;

            case LIBRARY_TOO_OLD:
                message = R.string.application_operation_failure;
                break;

            case SERVER_ERROR:
                message = R.string.application_operation_failure;
                break;

            case SERVICE_UNAVAILABLE:
                message = R.string.application_no_service;
                break;

            case TWINLIFE_OFFLINE:
                final ConnectionStatus connectionStatus = getTwinmeContext().getConnectionStatus();
                if (connectionStatus == ConnectionStatus.NO_INTERNET) {
                    message = R.string.application_no_network_connectivity;
                } else if (connectionStatus == ConnectionStatus.CONNECTING) {
                    message = R.string.application_not_connected;
                } else {
                    message = R.string.application_not_signed_in;
                }
                break;

            case WEBRTC_ERROR:
                message = R.string.application_webrtc_failure;
                break;

            case NO_STORAGE_SPACE:
                message = R.string.application_error_no_storage_space;
                break;

            case DATABASE_ERROR:
                message = R.string.application_database_error;
                break;

            case TIMEOUT_ERROR:
                message = R.string.application_server_timeout;
                break;

            case NOT_AUTHORIZED_OPERATION:
                message = R.string.application_not_authorized_operation;
                break;

            default:
                message = R.string.application_operation_failure;
                break;
        }

        return getString(message);
    }

    @Override
    public void onError(@NonNull final Activity activity, BaseService.ErrorCode errorCode, @Nullable String message, @Nullable Runnable errorCallback) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onError: activity=" + activity + " errorCode=" + errorCode + " message=" + message + " errorCallback=" + errorCallback);
        }

        if (message == null) {
            message = errorToString(errorCode);
        }

        if (errorCallback == null) {
            errorCallback = activity::finish;
        }
        ((TwinmeActivity) activity).error(message, errorCallback);
    }

    @Override
    public boolean showWelcomeScreen() {
        if (DEBUG) {
            Log.d(LOG_TAG, "showWelcomeScreen");
        }

        return Settings.showWelcomeScreen.getBoolean();
    }

    @SuppressLint("ApplySharedPref")
    @Override
    public void hideWelcomeScreen() {
        if (DEBUG) {
            Log.d(LOG_TAG, "hideWelcomeScreen");
        }

        Settings.showWelcomeScreen.setBoolean(false).save();
    }

    @Override
    public void restoreWelcomeScreen() {
        if (DEBUG) {
            Log.d(LOG_TAG, "restoreWelcomeScreen");
        }

        Settings.showWelcomeScreen.setBoolean(true).save();
    }

    @Override
    public void setFirstInstallation() {
        if (DEBUG) {
            Log.d(LOG_TAG, "setFirstInstallation");
        }

        if (Settings.firstInstallation.getLong() == 0) {
            Settings.firstInstallation.setLong(new Date().getTime() / 1000).save();
        }
    }

    @Override
    public boolean showUpgradeScreen() {
        if (DEBUG) {
            Log.d(LOG_TAG, "showUpgradeScreen");
        }

        if (isFeatureSubscribed(org.twinlife.twinme.TwinmeApplication.Feature.GROUP_CALL) || !canShowUpgradeScreen()) {
            return false;
        }

        long oneDay = 60 * 60 * 24;
        long threeDay = 3 * oneDay;
        long fifteenDay = 15 * oneDay;
        long oneWeek = 7 * oneDay;

        long timeInterval = new Date().getTime() / 1000;

        long firstInstallation = Settings.firstInstallation.getLong();
        long firstShowUpgradeScreen = Settings.firstShowUpgradeScreen.getLong();
        if (firstInstallation > 0 && firstShowUpgradeScreen == 0) {
            long diffTimeSinceFirstInstallation = timeInterval - firstInstallation;
            if (diffTimeSinceFirstInstallation < oneDay) {
                return false;
            }
        }

        if (firstShowUpgradeScreen == 0) {
            Settings.firstShowUpgradeScreen.setLong(timeInterval);
            Settings.lastShowUpgradeScreen.setLong(timeInterval).save();

            return true;
        }

        boolean showScreen = false;

        long lastShowUpgradeScreen = Settings.lastShowUpgradeScreen.getLong();
        long diffTimeSinceFirstShow = timeInterval - firstShowUpgradeScreen;
        long diffTimeSinceLastShow = timeInterval - lastShowUpgradeScreen;

        if (diffTimeSinceFirstShow < oneWeek && diffTimeSinceLastShow > threeDay) {
            showScreen = true;
        }  else if (diffTimeSinceLastShow > fifteenDay) {
            showScreen = true;
        }

        if (showScreen) {
            Settings.lastShowUpgradeScreen.setLong(timeInterval).save();
        }

        return showScreen;
    }
    @Override
    public boolean canShowUpgradeScreen() {
        if (DEBUG) {
            Log.d(LOG_TAG, "canShowUpgradeScreen");
        }

        return Settings.canShowUpgradeScreen.getBoolean();
    }

    @Override
    public void setCanShowUpgradeScreen() {
        if (DEBUG) {
            Log.d(LOG_TAG, "setCanShowUpgradeScreen");
        }

        Settings.canShowUpgradeScreen.setBoolean(true).save();
    }

    @Override
    public int fontSize() {
        if (DEBUG) {
            Log.d(LOG_TAG, "fontSize");
        }

        return Settings.fontSize.getInt();
    }

    @Override
    public void updateFontSize(FontSize fontSize) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateFontSize: fontSize=" + fontSize);
        }

        Settings.fontSize.setInt(fontSize.ordinal()).save();
    }

    @Override
    public int emojiFontSize() {
        if (DEBUG) {
            Log.d(LOG_TAG, "emojiFontSize");
        }

        return Settings.emojiSize.getInt();
    }

    @Override
    public void updateEmojiFontSize(EmojiSize emojiSize) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateEmojiFontSize: emojiSize=" + emojiSize);
        }

        Settings.emojiSize.setInt(emojiSize.ordinal()).save();
    }

    @Override
    public boolean visualizationLink() {
        if (DEBUG) {
            Log.d(LOG_TAG, "visualizationLink");
        }

        return Settings.visualizationLink.getBoolean();
    }

    @Override
    public int defaultTab() {
        if (DEBUG) {
            Log.d(LOG_TAG, "defaultTab");
        }

        return Settings.defaultTab.getInt();
    }

    @Override
    public void updateDefaultTab(DefaultTab defaultTab) {

        Settings.defaultTab.setInt(defaultTab.ordinal()).save();
    }

    @Override
    public void updateDisplayMode(DisplayMode displayMode) {

        Settings.displayMode.setInt(displayMode.ordinal()).save();
    }

    @Override
    public int hapticFeedbackMode() {
        if (DEBUG) {
            Log.d(LOG_TAG, "hapticFeedbackMode");
        }

        return Settings.hapticFeedbackMode.getInt();
    }

    @Override
    public void updateHapticFeedbackMode(HapticFeedbackMode hapticFeedbackMode) {

        Settings.hapticFeedbackMode.setInt(hapticFeedbackMode.ordinal()).save();
    }

    @Override
    public int displayMode() {
        if (DEBUG) {
            Log.d(LOG_TAG, "displayMode");
        }

        if (mAdminService != null && mAdminService.getCurrentSpace() != null && !mAdminService.getCurrentSpace().getSpaceSettings().getBoolean(SpaceSettingProperty.PROPERTY_DEFAULT_APPEARANCE_SETTINGS, true)) {
            return  Integer.parseInt(mAdminService.getCurrentSpace().getSpaceSettings().getString(SpaceSettingProperty.PROPERTY_DISPLAY_MODE, Settings.displayMode.getInt() + ""));
        } else if (getTwinmeContext() != null) {
            return  Integer.parseInt(getTwinmeContext().getDefaultSpaceSettings().getString(SpaceSettingProperty.PROPERTY_DISPLAY_MODE, Settings.displayMode.getInt() + ""));
        }

        return Settings.displayMode.getInt();
    }

    @Override
    public boolean messageCopyAllowed() {
        if (DEBUG) {
            Log.d(LOG_TAG, "messageCopyAllowed");
        }

        Space space = mAdminService.getCurrentSpace();
        if (space != null) {
            return space.messageCopyAllowed();
        }

        return Settings.messageCopyAllowed.getBoolean();
    }

    @Override
    public boolean fileCopyAllowed() {
        if (DEBUG) {
            Log.d(LOG_TAG, "fileCopyAllowed");
        }

        Space space = mAdminService.getCurrentSpace();
        if (space != null) {
            return space.fileCopyAllowed();
        }

        return Settings.fileCopyAllowed.getBoolean();
    }

    @Override
    public boolean imageCopyAllowed() {
        if (DEBUG) {
            Log.d(LOG_TAG, "imageCopyAllowed");
        }

        return fileCopyAllowed();
    }

    @Override
    public boolean audioCopyAllowed() {
        if (DEBUG) {
            Log.d(LOG_TAG, "audioCopyAllowed");
        }

        return fileCopyAllowed();
    }

    @Override
    public boolean videoCopyAllowed() {
        if (DEBUG) {
            Log.d(LOG_TAG, "videoCopyAllowed");
        }

        return fileCopyAllowed();
    }

    @Override
    public DisplayCallsMode displayCallsMode() {

        return DisplayCallsMode.fromInteger(Settings.displayCallsMode.getInt());
    }

    @Override
    public void setDisplayCallsMode(DisplayCallsMode displayCallsMode) {

        Settings.displayCallsMode.setInt(DisplayCallsMode.toInteger(displayCallsMode)).save();
    }

    @Override
    public int updateProfileMode() {

        return Settings.profileUpdateMode.getInt();
    }

    @Override
    public void setUpdateProfileMode(Profile.UpdateMode updateProfileMode) {

        Settings.profileUpdateMode.setInt(updateProfileMode.ordinal()).save();
    }

    @Override
    public String defaultDirectoryToSaveFiles() {
        if (DEBUG) {
            Log.d(LOG_TAG, "defaultDirectoryToSaveFiles");
        }

        String userDefaultDir = Settings.defaultDirectoryToSave.getString();
        File defaultDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (userDefaultDir == null) {
            return defaultDir.getPath();
        }

        if (userDefaultDir.startsWith("/")) {
            File userDir = new File(userDefaultDir);
            if (userDir.exists()) {
                return userDefaultDir;
            }
            return defaultDir.getPath();
        }

        File userDir = new File(defaultDir.getParentFile(), userDefaultDir);
        if (userDir.exists()) {
            return userDir.getPath();
        }
        return defaultDir.getPath();
    }

    @Override
    public String defaultDirectoryToExportFiles() {
        if (DEBUG) {
            Log.d(LOG_TAG, "defaultDirectoryToExportFiles");
        }

        String userDefaultDir = Settings.defaultDirectoryToExport.getString();
        File defaultDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (userDefaultDir == null) {
            return defaultDir.getPath();
        }

        if (userDefaultDir.startsWith("/")) {
            File userDir = new File(userDefaultDir);
            if (userDir.exists()) {
                return userDefaultDir;
            }
            return defaultDir.getPath();
        }

        File userDir = new File(defaultDir.getParentFile(), userDefaultDir);
        if (userDir.exists()) {
            return userDir.getPath();
        }
        return defaultDir.getPath();
    }

    @Override
    public Uri defaultUriToSaveFiles() {
        if (DEBUG) {
            Log.d(LOG_TAG, "defaultUriToSaveFiles");
        }

        String documentId = Settings.defaultDocumentIdToSave.getString();
        if (documentId.isEmpty()) {
            return MediaStore.Files.getContentUri("external");
        }

        String authority = Settings.defaultUriAuthorityToSave.getString();
        if (authority == null) {
            authority = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
        }

        return DocumentsContract.buildDocumentUri(authority, documentId);
    }

    @Override
    public int sendImageSize() {

        return Settings.reduceSizeImage.getInt();
    }

    @Override
    public void setSendImageSize(SendImageSize sendImageSize) {

        Settings.reduceSizeImage.setInt(sendImageSize.ordinal()).save();
    }

    @Override
    public int sendVideoSize() {

        return Settings.reduceSizeVideo.getInt();
    }

    @Override
    public void setSendVideoSize(SendVideoSize sendVideoSize) {

        Settings.reduceSizeVideo.setInt(sendVideoSize.ordinal()).save();
    }

    public State getState() {

        return mState;
    }

    public void setReady() {

        mState = State.READY;
    }

    @Override
    public InCallInfo inCallInfo() {

        return mInCallInfo;
    }

    @Override
    public void setInCallInfo(InCallInfo inCallInfo) {

        mInCallInfo = inCallInfo;
    }

    @Override
    public boolean screenLocked() {
        if (DEBUG) {
            Log.d(LOG_TAG, "screenLocked");
        }

        return Settings.privacyActivityScreenLock.getBoolean();
    }

    @Override
    public boolean isVideoInFitMode() {
        if (DEBUG) {
            Log.d(LOG_TAG, "isVideoInFitMode");
        }

        return Settings.videoCallInFitMode.getBoolean();
    }

    @Override
    public void setVideoInFitMode(boolean fitMode) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setVideoInFitMode: fitMode=" + fitMode);
        }

        Settings.videoCallInFitMode.setBoolean(fitMode).save();
    }

    @Override
    public boolean askCallQualityWithCallDuration(long duration) {
        if (DEBUG) {
            Log.d(LOG_TAG, "askCallQualityWithCallDuration");
        }

        if (duration > CALL_QUALITY_MIN_DURATION) {
            int callCount = Settings.callQualityCount.getInt() + 1;
            boolean askCallQuality = false;
            long timeInterval = new Date().getTime() / 1000;
            if (Settings.callQualityLastDate.getLong() == 0) {
                askCallQuality = true;
            } else {
                long callQualityLastDate = Settings.callQualityLastDate.getLong();
                long diffTimeSinceLastDate = timeInterval - callQualityLastDate;
                if (diffTimeSinceLastDate > CALL_QUALITY_INTERVAL_DATE || callCount >= CALL_QUALITY_ASK_FREQUENCY) {
                    askCallQuality = true;
                }
            }

            if (askCallQuality) {
                callCount = 0;
                Settings.callQualityLastDate.setLong(timeInterval).save();
            }

            Settings.callQualityCount.setInt(callCount).save();

            return askCallQuality;
        }

        return false;
    }

    //
    //  App Info Management
    //

    @Override
    @Nullable
    public AppStateInfo appInfo() {
        if (DEBUG) {
            Log.d(LOG_TAG, "appInfo");
        }

        return mAppInfo;
    }

    @Override
    public void setAppInfo(@Nullable AppStateInfo appInfo) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setAppInfo: " + appInfo);
        }

        mAppInfo = appInfo;
    }

    @Override
    public void setScreenLocked(boolean lock) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setScreenLocked: lock=" + lock);
        }

        Settings.privacyActivityScreenLock.setBoolean(lock).save();
    }

    @Override
    public int screenLockTimeout() {
        if (DEBUG) {
            Log.d(LOG_TAG, "screenLockTimeout");
        }

        return Settings.privacyScreenLockTimeout.getInt();
    }

    @Override
    public void updateScreenLockTimeout(int time) {

        Settings.privacyScreenLockTimeout.setInt(time).save();
    }

    @Override
    public boolean lastScreenHidden() {
        if (DEBUG) {
            Log.d(LOG_TAG, "lastScreenHidden");
        }

        return Settings.privacyHideLastScreen.getBoolean();
    }

    @Override
    public void setLastScreenHidden(boolean hide) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setLastScreenHidden: hide=" + hide);
        }

        Settings.privacyHideLastScreen.setBoolean(hide).save();
    }

    @Override
    public void setAppBackgroundDate(Date date) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setAppBackgroundDate: date=" + date);
        }

        mAppBackgroundDate = date;
    }

    @Override
    public boolean showLockScreen() {
        if (DEBUG) {
            Log.d(LOG_TAG, "showLockScreen");
        }

        return mShowLockScreen;
    }

    @Override
    public void unlockScreen() {
        if (DEBUG) {
            Log.d(LOG_TAG, "unlockScreen");
        }

        mShowLockScreen = false;
        mAppBackgroundDate = null;
    }

    //
    //  Ephemeral Message
    //

    @Override
    public int ephemeralExpireTimeout(){
        if (DEBUG) {
            Log.d(LOG_TAG, "ephemeralExpireTimeout");
        }

        return Settings.ephemeralMessageExpireTimeout.getInt();
    }

    public void onConnectionStatusChange(@NonNull ConnectionStatus connectionStatus) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onConnectionStatusChange " + connectionStatus);
        }

        final TwinmeActivityImpl activity;
        synchronized (this) {
            if (mCurrentActivity == null) {
                activity = null;
            } else {
                activity = mCurrentActivity.get();
            }
        }
        if (activity != null) {
            activity.runOnUiThread(() -> activity.onConnectionStatusChange(connectionStatus));
        }
    }

    @Override
    public boolean showConnectedMessage() {
        if (DEBUG) {
            Log.d(LOG_TAG, "showConnectedMessage");
        }

        return mShowConnectedMessage;
    }

    @Override
    public void setShowConnectedMessage(boolean show) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setShowConnectedMessage: show=" + show);
        }

        mShowConnectedMessage = show;
    }

    //
    // Override TwinmeApplicationImpl methods
    //

    @Override
    public void onCreate() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate");
        }

        sInstance = new WeakReference<>(this);

        mAnonymousAvatar = getScaledAvatar(getResources(), R.drawable.anonymous_avatar);
        mDefaultAvatar = getScaledAvatar(getResources(), R.drawable.default_avatar);
        mDefaultGroupAvatar = getScaledAvatar(getResources(), R.drawable.anonymous_group_avatar);

        try (InputStream is = getResources().openRawResource(R.raw.tool)) {
            setTwinlifeConfiguration(new Configuration(), is);
        } catch (Exception ex) {
            if (Logger.DEBUG) {
                Log.e(LOG_TAG, "Error in configuration", ex);
            }
            setTwinlifeConfiguration(new Configuration(), null);
        }

        // should be done after initialization
        super.onCreate();

        if (org.twinlife.twinlife.BuildConfig.ENABLE_CHECKS) {
            StrictMode.enableDefaults();
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedClosableObjects()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedRegistrationObjects()
                    .penaltyLog()
                    .build());
        }

        TwinmeContext twinmeContext = getTwinmeContext();
        if (twinmeContext == null) {
            return;
        }

        Settings.init(twinmeContext.getConfigurationService());

        // Create the default space settings based on the user's current settings.
        SpaceSettings defaultSettings = new SpaceSettings(getResources().getString(R.string.space_appearance_activity_general_title));
        defaultSettings.setMessageCopyAllowed(Settings.messageCopyAllowed.getBoolean());
        defaultSettings.setFileCopyAllowed(Settings.fileCopyAllowed.getBoolean());
        twinmeContext.setDefaultSpaceSettings(defaultSettings, getResources().getString(R.string.application_default));

        // Setup so that the 'description' and 'capabilities' attributes are copied from the Profile
        // when a new relation is created.
        twinmeContext.registerSharedTwincodeAttribute(Twincode.DESCRIPTION, Twincode.DESCRIPTION);
        twinmeContext.registerSharedTwincodeAttribute(Twincode.CAPABILITIES, Twincode.CAPABILITIES);
        mAdminService = new AdminService(twinmeContext, this);
        mJobService = twinmeContext.getJobService();
        mJobService.setObserver(this);
        Glide.get(this).getRegistry()
                .append(ImageId.class, Bitmap.class, TwinlifeImageLoader.Factory.create(twinmeContext))
                .append(ConversationService.FileDescriptor.class, InputStream.class, FileDescriptorLoader.Factory.create(twinmeContext))
                .append(FileInfo.class, InputStream.class, MediaInfoImageLoader.Factory.create())
                .append(FileInfo.class, Bitmap.class, MediaInfoVideoThumbnailLoader.Factory.create(this));

        mCoachMarkManager = new CoachMarkManager();
    }

    @Override
    public void onEnterBackground() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onEnterBackground");
        }

        mIsInBackground = true;
        mAppBackgroundDate = new Date();
        boolean isIdle = mJobService.isIdle();
        if (!isIdle && !CallService.isRunning()) {
            PeerService.startService(this, 0, System.currentTimeMillis());
        }
    }

    @Override
    public void onEnterForeground() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onEnterForeground");
        }

        mIsInBackground = false;

        // We are now in foreground: we can stop the peer service since we don't need it anymore.
        PeerService.forceStop(this);

        if (!screenLocked()) {
            mShowLockScreen = false;
            return;
        }

        if (mAppBackgroundDate != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(mAppBackgroundDate);
            calendar.add(Calendar.SECOND, screenLockTimeout());

            Date lockDate = calendar.getTime();
            mShowLockScreen = lockDate.before(new Date());
        } else {
            mShowLockScreen = true;
        }
    }

    @Override
    public void onBackgroundNetworkStart() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBackgroundNetworkStart");
        }

        final boolean isIdle = mJobService.isIdle();
        if (!isIdle && !CallService.isRunning()) {
            // Delay by 1s the possible start of the PeerService: it is best if we are started
            // as a result of a Firebase Push message because the application will have higher priority.
            // BUT, we cannot wait for the Firebase Push to be received.
            mJobService.schedule(this::startPeerService, 1000);
        }
    }

    private void startPeerService() {
        if (DEBUG) {
            Log.d(LOG_TAG, "startPeerService");
        }

        final boolean isIdle = mJobService.isIdle();
        if (!isIdle && !CallService.isRunning()) {
            PeerService.startService(this, 0, System.currentTimeMillis());
        }
    }

    @Override
    public void onBackgroundNetworkStop() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBackgroundNetworkStop");
        }

        // Wait 1s before asking the service to stop because sometimes a new P2P connection
        // is started 100 to 500ms after and we won't be able to start it again.
        mJobService.schedule(this::stopPeerService, 1000);
    }

    private void stopPeerService() {
        if (DEBUG) {
            Log.d(LOG_TAG, "stopPeerService");
        }

        final boolean isIdle = mJobService.isIdle();
        if (isIdle) {
            PeerService.forceStop(this);
        }
    }

    @Override
    public void onActivePeers(int count) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onActivePeers count=" + count);
        }
    }

    //
    // Implement TwinmeApplication Methods
    //

    @Override
    @Nullable
    public Uri getRingtone(RingtoneType ringtoneType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getRingtone: ringtoneType=" + ringtoneType);
        }

        if (!Settings.soundEnabled.getBoolean()) {
            return null;
        }
        boolean hasRingtone = false;
        switch (ringtoneType) {
            case AUDIO_RINGTONE:
                hasRingtone = Settings.audioRingEnabled.getBoolean();
                break;

            case VIDEO_RINGTONE:
                hasRingtone = Settings.videoRingEnabled.getBoolean();
                break;

            case NOTIFICATION_RINGTONE:
                hasRingtone = Settings.notificationRingEnabled.getBoolean();
                break;
        }

        if (!hasRingtone) {

            return null;
        }

        String ringtone = null;
        switch (ringtoneType) {
            case AUDIO_RINGTONE:
                ringtone = Settings.audioCallRingtone.getString();
                break;

            case VIDEO_RINGTONE:
                ringtone = Settings.videoCallRingtone.getString();
                break;

            case NOTIFICATION_RINGTONE:
                ringtone = Settings.notificatonRingtone.getString();
                break;
        }

        Uri ringtoneUri = null;
        if (ringtone != null) {
            ringtoneUri = Uri.parse(ringtone);

            // Refuse a file:// because this is not allowed on SDK 24 and higher.
            // We will get the FileUriExposedException when the notification is created.
            // This concerns only Android 7 and Android 7.1 since Android 8 and above use notification channels.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && ringtoneUri.toString().startsWith("file://")) {
                ringtoneUri = null;
            }
        }
        switch (ringtoneType) {
            case AUDIO_RINGTONE:
                if (ringtoneUri == null || ringtoneUri.equals(android.provider.Settings.System.DEFAULT_RINGTONE_URI)) {
                    ringtoneUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.audio_call_ringtone);
                }
                break;

            case VIDEO_RINGTONE:
                if (ringtoneUri == null || ringtoneUri.equals(android.provider.Settings.System.DEFAULT_RINGTONE_URI)) {
                    ringtoneUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.video_call_ringtone);
                }
                break;

            case NOTIFICATION_RINGTONE:
                if (ringtoneUri == null || ringtoneUri.equals(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)) {
                    ringtoneUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.notification_ringtone);
                }
                break;
        }

        Uri mediaUri = null;
        if (ringtoneUri != null && android.provider.Settings.AUTHORITY.equals(ringtoneUri.getAuthority())) {
            try (Cursor cursor = getContentResolver().query(ringtoneUri, new String[]{android.provider.Settings.NameValueTable.VALUE}, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    String value = cursor.getString(0);
                    mediaUri = Uri.parse(value);
                }
            } catch (Exception exception) {
                mediaUri = null;
            }
        } else {
            mediaUri = ringtoneUri;
        }

        return mediaUri;
    }

    @Override
    public boolean getVibration(RingtoneType ringtoneType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getVibration: ringtoneType=" + ringtoneType);
        }

        if (!Settings.soundEnabled.getBoolean()) {
            return false;
        }
        boolean vibration = true;
        switch (ringtoneType) {
            case AUDIO_RINGTONE:
                vibration = Settings.audioVibration.getBoolean();
                break;

            case VIDEO_RINGTONE:
                vibration = Settings.videoVibration.getBoolean();
                break;

            case NOTIFICATION_RINGTONE:
                vibration = Settings.notificationVibration.getBoolean();
                break;
        }
        return vibration;
    }

    @Override
    public boolean getDisplayNotificationSender() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getDisplayNotificationSender");
        }

        return Settings.displayNotificationSender.getBoolean();
    }

    @Override
    public boolean getDisplayNotificationContent() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getDisplayNotificationContent");
        }

        return Settings.displayNotificationContent.getBoolean();
    }

    @Override
    public boolean getDisplayNotificationLike() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getDisplayNotificationLike");
        }

        return Settings.displayNotificationLike.getBoolean();
    }

    @NonNull
    @Override
    public String getApplicationName() {
        return getString(R.string.application_name);
    }

    @Override
    @Nullable
    public LastVersion loadLastVersion(){
        return LastVersionImpl.load(this);
    }

    @Override
    @Nullable
    public synchronized LastVersion getLastVersion() {
        LastVersion lastVersion = mAdminService.getLastVersion();
        if (lastVersion == null) {
            lastVersion = loadLastVersion();
            mAdminService.setLastVersion(lastVersion);
        }

        return lastVersion;
    }

    @Override
    public boolean hasNewVersion() {

        LastVersion lastVersion = getLastVersion();
        return lastVersion != null && lastVersion.hasNewVersion();
    }

    @Override
    public boolean showWhatsNew() {

        long oneDay = 60 * 60 * 24;
        long period = 20 * oneDay;

        long timeInterval = new Date().getTime() / 1000;

        long firstInstallation = Settings.firstInstallation.getLong();
        if (firstInstallation > 0) {
            long diffTimeSinceFirstInstallation = timeInterval - firstInstallation;
            if (diffTimeSinceFirstInstallation < period) {
                return false;
            }
        }

        LastVersion lastVersion = getLastVersion();
        return lastVersion != null && lastVersion.isVersionUpdated();
    }

    @Override
    public boolean showEnableNotificationScreen() {

        long oneDay = 60 * 60 * 24;

        long timeInterval = new Date().getTime() / 1000;
        boolean showScreen = false;

        long lastShowEnableNotificationScreen = Settings.lastShowEnableNotificationScreen.getLong();
        long diffTimeSinceLastShow = timeInterval - lastShowEnableNotificationScreen;

        if (diffTimeSinceLastShow > oneDay) {
            showScreen = true;
        }

        if (showScreen) {
            Settings.lastShowEnableNotificationScreen.setLong(timeInterval).save();
        }

        return showScreen;
    }

    //
    // Invitation subscription
    //
    @Override
    public String getInvitationSubscriptionImage() {

        return Settings.premiumSubscriptionInvitationImage.getString();
    }

    @Override
    public void setInvitationSubscriptionImage(String image) {

        Settings.premiumSubscriptionInvitationImage.setString(image).save();
    }

    @Override
    public String getInvitationSubscriptionTwincode() {

        return Settings.premiumSubscriptionInvitationTwincode.getString();
    }

    @Override
    public void setInvitationSubscriptionTwincode(String twincode) {

        Settings.premiumSubscriptionInvitationTwincode.setString(twincode).save();
    }

    //
    // Coach Mark
    //

    @Override
    public boolean showCoachMark() {

        return mCoachMarkManager.showCoachMark();
    }

    @Override
    public void setShowCoachMark(boolean showCoachMark) {

        mCoachMarkManager.setShowCoachMark(showCoachMark);
    }

    @Override
    public boolean showCoachMark(CoachMark.CoachMarkTag coachMarkTag) {

        return mCoachMarkManager.showCoachMark(coachMarkTag);
    }

    @Override
    public void hideCoachMark(CoachMark.CoachMarkTag coachMarkTag) {

        mCoachMarkManager.hideCoachMark(coachMarkTag);
    }

    //
    // current space
    //
    @Override
    public boolean isCurrentSpace(UUID spaceId) {

        return mAdminService.getCurrentSpace() != null && mAdminService.getCurrentSpace().getId().equals(spaceId);
    }

    @Override
    public boolean hasCurrentSpace() {

        return mAdminService.getCurrentSpace() != null;
    }

    @Override
    public Space getCurrentSpace() {

        return mAdminService.getCurrentSpace();
    }

    //
    // background
    //
    @Override
    public boolean isInBackground() {

        return mIsInBackground;
    }

    //
    // Group call animation
    //

    @Override
    public boolean showGroupCallAnimation() {
        if (DEBUG) {
            Log.d(LOG_TAG, "showGroupCallAnimation");
        }

        return Settings.showGroupCallAnimation.getBoolean();
    }

    @SuppressLint("ApplySharedPref")
    @Override
    public void hideGroupCallAnimation() {
        if (DEBUG) {
            Log.d(LOG_TAG, "hideGroupCallAnimation");
        }

        Settings.showGroupCallAnimation.setBoolean(false).save();
    }

    //
    // Click to call description
    //
    @Override
    public boolean showClickToCallDescription() {
        if (DEBUG) {
            Log.d(LOG_TAG, "showClickToCallDescription");
        }

        int showClickToCallDescriptionCount = Settings.showClickToCallDescriptionCount.getInt();
        if (showClickToCallDescriptionCount < SHOW_CLICK_TO_CALL_DESCRIPTION_MAX) {
            Settings.showClickToCallDescriptionCount.setInt(showClickToCallDescriptionCount + 1).save();
            return true;
        }

        return false;
    }

    //
    // Space description
    //
    @Override
    public boolean showSpaceDescription() {
        if (DEBUG) {
            Log.d(LOG_TAG, "showSpaceDescription");
        }

        return Settings.showSpaceDescription.getBoolean();
    }

    @SuppressLint("ApplySharedPref")
    @Override
    public void hideSpaceDescription() {
        if (DEBUG) {
            Log.d(LOG_TAG, "hideSpaceDescription");
        }

        Settings.showSpaceDescription.setBoolean(false).save();
    }

    //
    // Telecom
    //

    @Override
    public boolean isTelecomEnable() {
        if (DEBUG) {
            Log.d(LOG_TAG, "isTelecomEnable");
        }

        return Settings.useTelecom.getBoolean();
    }

    @Override
    public void setUseTelecom(boolean enable) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setUseTelecom");
        }

        Settings.useTelecom.setBoolean(enable).save();
    }

    //
    // Onboarding
    //

    @Override
    public boolean startCallRestrictionMessage() {

        return Settings.showCallRestrictionMessage.getBoolean();
    }

    @Override
    public void setShowCallRestrictionMessage(boolean show) {

        Settings.showCallRestrictionMessage.setBoolean(show).save();
    }

    @Override
    public boolean startOnboarding(OnboardingType onboardingType) {

        switch (onboardingType) {
            case CERTIFIED_RELATION:
                return Settings.showCertifiedRelationOnboarding.getBoolean();

            case SPACE:
                return Settings.showSpaceOnboarding.getBoolean();

            case PROFILE:
                return Settings.showProfileOnboarding.getBoolean();

            case EXTERNAL_CALL:
                return Settings.showExternalCallOnboarding.getBoolean();

            case TRANSFER:
                return Settings.showTransferOnboarding.getBoolean();

            case ENTER_MINI_CODE:
                return Settings.showEnterMiniCodeOnboarding.getBoolean();

            case MINI_CODE:
                return Settings.showMiniCodeOnboarding.getBoolean();

            case REMOTE_CAMERA:
                return Settings.showRemoteCameraOnboarding.getBoolean();

            case REMOTE_CAMERA_SETTING:
                return Settings.showRemoteCameraSettingOnboarding.getBoolean();

            case TRANSFER_CALL:
                return Settings.showTransferCallOnboarding.getBoolean();

            case PROXY:
                return Settings.showProxyOnboarding.getBoolean();

            default:
                return false;
        }
    }

    @Override
    public void setShowOnboardingType(OnboardingType onboardingType, boolean state) {

        switch (onboardingType) {
            case CERTIFIED_RELATION:
                Settings.showCertifiedRelationOnboarding.setBoolean(state).save();
                break;

            case SPACE:
                Settings.showSpaceOnboarding.setBoolean(state).save();
                break;

            case PROFILE:
                Settings.showProfileOnboarding.setBoolean(state).save();
                break;

            case EXTERNAL_CALL:
                Settings.showExternalCallOnboarding.setBoolean(state).save();
                break;

            case TRANSFER:
                Settings.showTransferOnboarding.setBoolean(state).save();
                break;

            case ENTER_MINI_CODE:
                Settings.showEnterMiniCodeOnboarding.setBoolean(state).save();
                break;

            case MINI_CODE:
                Settings.showMiniCodeOnboarding.setBoolean(state).save();
                break;

            case REMOTE_CAMERA:
                Settings.showRemoteCameraOnboarding.setBoolean(state).save();
                break;

            case REMOTE_CAMERA_SETTING:
                Settings.showRemoteCameraSettingOnboarding.setBoolean(state).save();
                break;

            case TRANSFER_CALL:
                Settings.showTransferCallOnboarding.setBoolean(state).save();
                break;

            case PROXY:
                Settings.showProxyOnboarding.setBoolean(state).save();
                break;

            default:
                break;
        }
    }

    @Override
    public void resetOnboarding() {

        Settings.showCertifiedRelationOnboarding.setBoolean(true).save();
        Settings.showSpaceOnboarding.setBoolean(true).save();
        Settings.showProfileOnboarding.setBoolean(true).save();
        Settings.showExternalCallOnboarding.setBoolean(true).save();
        Settings.showTransferOnboarding.setBoolean(true).save();
        Settings.showEnterMiniCodeOnboarding.setBoolean(true).save();
        Settings.showMiniCodeOnboarding.setBoolean(true).save();
        Settings.showRemoteCameraOnboarding.setBoolean(true).save();
        Settings.showRemoteCameraSettingOnboarding.setBoolean(true).save();
        Settings.showTransferCallOnboarding.setBoolean(true).save();
        Settings.showProxyOnboarding.setBoolean(true).save();
    }

    @Override
    public boolean startWarningEditMessage() {

        return Settings.showWarningEditMessage.getBoolean();
    }

    @Override
    public void setShowWarningEditMessage(boolean show) {

        Settings.showWarningEditMessage.setBoolean(show).save();
    }

    public void setCurrentActivity(@NonNull TwinmeActivityImpl activity) {

        synchronized (this) {
            mCurrentActivity = new WeakReference<>(activity);
        }
    }
}
