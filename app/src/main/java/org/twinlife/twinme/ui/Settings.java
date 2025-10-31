/*
 *  Copyright (c) 2021-2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui;

import android.graphics.Color;

import androidx.annotation.NonNull;

import org.twinlife.twinlife.ConfigIdentifier;
import org.twinlife.twinlife.ConfigurationService;
import org.twinlife.twinlife.ConfigurationService.Configuration;
import org.twinlife.twinlife.DisplayCallsMode;
import org.twinlife.twinme.models.Profile;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.skin.DisplayMode;
import org.twinlife.twinme.skin.EmojiSize;
import org.twinlife.twinme.skin.FontSize;
import org.twinlife.twinme.utils.CommonUtils;

/**
 * Settings used by the application.
 * <p>
 * The user setting has a unique name, a unique UUID and is stored in the Android default preferences.
 * The user setting UUID is used by the account migration service to take into account the setting during an account migration.
 * The get/save/reset operations are handled by the ConfigurationService.
 */
public class Settings {
    public static final int MAX_CALL_GROUP_PARTICIPANTS = 8;

    private static final int DEFAULT_TIMEOUT_MESSAGE = 30;

    // Sound and vibration settings.
    public static final BooleanConfig soundEnabled = new BooleanConfig("settings_activity_sound_enabled", true, "4383A4B4-F091-4EB5-93E7-4C7A01E6A31D");
    public static final BooleanConfig audioVibration = new BooleanConfig("settings_activity_audio_has_vibration", true, "CA705D70-9029-4746-9719-274EA0F29F7C");
    public static final BooleanConfig videoVibration = new BooleanConfig("settings_activity_video_has_vibration", true, "8412B66C-19E6-4D86-ADBF-BFF0FDDA1C2D");
    public static final BooleanConfig notificationVibration = new BooleanConfig("settings_activity_notification_has_vibration", true, "73D907A5-BDD2-44E4-8FA5-78E170A84421");
    public static final BooleanConfig audioRingEnabled = new BooleanConfig("settings_activity_audio_call_has_ringtone", true, "EA25E83B-772E-456F-BF87-65745C80CCD1");
    public static final BooleanConfig videoRingEnabled = new BooleanConfig("settings_activity_video_call_has_ringtone", true, "BD58A3FF-5EFE-491D-8FDC-21F61C87CE0C");
    public static final BooleanConfig notificationRingEnabled = new BooleanConfig("settings_activity_notification_has_ringtone", true, "58F00122-5ED8-41CC-966A-572AA0B20B4A");
    public static final StringConfig audioCallRingtone = new StringConfig("settings_activity_audio_call_ringtone", null, "77D31CDE-8EBF-4796-AADA-97276B1AD79F");
    public static final StringConfig videoCallRingtone = new StringConfig("settings_activity_video_call_ringtone", null, "81BA3B79-DFAE-4DBA-827A-471D17F64CFF");
    public static final StringConfig notificatonRingtone = new StringConfig("settings_activity_notification_ringtone", null, "989CB652-F1AE-4863-BA02-E3D024BCAD7A");

    // Display settings.
    public static final ColorConfig mainStyle = new ColorConfig(Design.MAIN_COLOR_PREFERENCE, Design.DEFAULT_COLOR, "B2977B13-1899-4A41-9244-365B40ADBBB9");
    public static final IntConfig fontSize = new IntConfig("settings_activity_font_size", FontSize.SYSTEM.ordinal(), "8961B734-1D70-407B-A02B-0F673FB2F8BC");
    public static final IntConfig defaultTab = new IntConfig("settings_activity_default_tab", TwinmeApplication.DefaultTab.CONVERSATIONS.ordinal(), "AD11179C-1510-4F1A-A4C2-0F29DC989997");
    public static final IntConfig displayMode = new IntConfig("settings_activity_display_mode", DisplayMode.SYSTEM.ordinal(), "44CE232D-4BA3-4295-8B27-7BD9981AD555");
    public static final IntConfig hapticFeedbackMode = new IntConfig("settings_activity_haptic_feedback_mode", TwinmeApplication.HapticFeedbackMode.SYSTEM.ordinal(), "E9819421-CD71-4C3D-AB6A-0783F0FF4532");
    public static final BooleanConfig visualizationLink = new BooleanConfig("settings_activity_visualization_link", true, "4B143BC6-1590-4889-B46A-2B54BCf5DBA8");
    public static final IntConfig emojiSize = new IntConfig("settings_activity_emoji_size", EmojiSize.STANDARD.ordinal(), "5CDAfAE4-FFE8-4754-A178-4f8C5DC834E0");
    public static final IntConfig displayCallsMode = new IntConfig("settings_activity_display_calls_mode", DisplayCallsMode.MISSED.ordinal(), "FA50C4AC-C196-4F3F-BD68-3DE18D27F44E");

    // Message settings
    public static final IntConfig reduceSizeImage = new IntConfig("settings_activity_reduce_size_image", TwinmeApplication.SendImageSize.ORIGINAL.ordinal(), "85F98FDE-5C4E-11ED-9B6A-0242AC120002");
    public static final IntConfig reduceSizeVideo = new IntConfig("settings_activity_reduce_size_video", TwinmeApplication.SendVideoSize.ORIGINAL.ordinal(), "E476F52F-C863-4463-BAB4-B89C875E601F");

    // Notification settings
    public static final BooleanConfig displayNotificationSender = new BooleanConfig("settings_activity_display_notification_sender", true, "2BA7FFAC-7992-4828-B2F3-D27A6F5D9AAB");
    public static final BooleanConfig displayNotificationContent = new BooleanConfig("settings_activity_display_notification_content", true, "C3B015D9-01C9-40E8-8239-98084D4C2D3F");
    public static final BooleanConfig displayNotificationLike = new BooleanConfig("settings_activity_display_notification_like", true, "8A368FF8-6E37-4B82-8227-AAF9B916CDBE");

    // Behavior settings
    public static final BooleanConfig showWelcomeScreen = new BooleanConfig("settings_activity_show_welcome_screen", true, "04E86861-71B6-40A0-9BAB-9AE58CC2E765");
    public static final BooleanConfig messageCopyAllowed = new BooleanConfig("settings_activity_message_copy_allowed", true, "3FC4574E-79CD-4CD6-8FD4-AC541162C312");
    public static final BooleanConfig fileCopyAllowed = new BooleanConfig("settings_activity_file_copy_allowed", true, "1A3E0E6E-78FE-448B-A671-7C5B4BA6AC72");
    public static final StringConfig defaultDirectoryToSave = new StringConfig("settings_activity_default_directory_to_save_files", null, "81E0C7CF-4146-43E1-B2D6-BAAD324514A0");
    public static final StringConfig defaultDocumentIdToSave = new StringConfig("settings_activity_default_document_id_to_save_files", "", "46C01729-E871-4208-8094-1EBD3E036FFF");
    public static final StringConfig defaultUriAuthorityToSave = new StringConfig("settings_activity_default_uri_authority_to_save_files", null, "1E141E92-1DC7-4FCD-9CD8-9D14FCAD8596");
    public static final StringConfig defaultDirectoryToExport = new StringConfig("settings_activity_default_directory_to_export", null, "3AE62A72-6CFB-4F8E-BBFE-730A7AE3AFCA");
    public static final BooleanConfig showGroupCallAnimation = new BooleanConfig("call_activity_show_group_call_animation", true, "BB834EE6-3927-42E1-BC46-5663B2AB47DB");
    public static final IntConfig showClickToCallDescriptionCount = new IntConfig("create_external_call_activity_show_click_to_call_description", 0, "70D0949E-A10f-4156-8D87-EFF914C65962");
    public static final BooleanConfig showSpaceDescription = new BooleanConfig("space_fragment_show_space_description", true, "C873606B-CA82-4A11-8252-6AD70CD07D7F");
    public static final IntConfig profileUpdateMode = new IntConfig("edit_profile_activity_profile_update_mode", Profile.UpdateMode.DEFAULT.ordinal(), "959957DA-B8EE-4506-8A5E-A5006023E13D");

    // Twinme+ settings
    public static final BooleanConfig privacyActivityScreenLock = new BooleanConfig("privacy_activity_screen_lock", false, "D3372EA5-1CB2-4365-92E5-5780B1F982FD");
    public static final IntConfig privacyScreenLockTimeout = new IntConfig("privacy_activity_timeout_screen_lock", 0, "24223BA2-822B-4867-B826-AE5430D88A4A");
    public static final BooleanConfig privacyHideLastScreen = new BooleanConfig("privacy_activity_hide_last_screen", false, "579627C5-87B5-403B-A58D-61977DFDD53A");
    public static final BooleanConfig ephemeralMessageAllowed = new BooleanConfig("settings_activity_ephemeral_message_allowed", false, "7837F336-8422-11EC-A8A3-0242AC120002");
    public static final IntConfig ephemeralMessageExpireTimeout = new IntConfig("settings_activity_ephemeral_message_expire_timeout", DEFAULT_TIMEOUT_MESSAGE, "585BA89F-86F3-48e0-A07C-C924C50f7C6D");

    // Internal settings (they are not transfered by account migration).
    public static final InternalLongConfig firstInstallation = new InternalLongConfig("settings_activity_first_first_installation", 0);
    public static final InternalLongConfig firstShowUpgradeScreen = new InternalLongConfig("settings_activity_first_show_upgrade_screen", 0);
    public static final InternalLongConfig lastShowUpgradeScreen = new InternalLongConfig("settings_activity_last_show_upgrade_screen", 0);
    public static final InternalBooleanConfig canShowUpgradeScreen = new InternalBooleanConfig("settings_activity_can_show_upgrade_screen", false);
    public static final InternalBooleanConfig showConnectedMessage = new InternalBooleanConfig("settings_activity_show_connected_message", false);
    public static final InternalBooleanConfig showCallRestrictionMessage = new InternalBooleanConfig("settings_activity_show_call_restriction_message", true);
    public static final InternalBooleanConfig showCertifiedRelationOnboarding = new InternalBooleanConfig("settings_activity_show_certified_relation_onboarding", true);
    public static final InternalBooleanConfig showSpaceOnboarding = new InternalBooleanConfig("settings_activity_show_space_onboarding", true);
    public static final InternalBooleanConfig showProfileOnboarding = new InternalBooleanConfig("settings_activity_show_profile_onboarding", true);
    public static final InternalBooleanConfig showExternalCallOnboarding = new InternalBooleanConfig("settings_activity_show_sexternal_call_onboarding", true);
    public static final InternalBooleanConfig showTransferOnboarding = new InternalBooleanConfig("settings_activity_show_transfer_call_onboarding", true);
    public static final InternalBooleanConfig showEnterMiniCodeOnboarding = new InternalBooleanConfig("settings_activity_show_enter_mini_code_onboarding", true);
    public static final InternalBooleanConfig showMiniCodeOnboarding = new InternalBooleanConfig("settings_activity_show_mini_code_onboarding", true);
    public static final InternalBooleanConfig showRemoteCameraOnboarding = new InternalBooleanConfig("settings_activity_show_remote_camera_onboarding", true);
    public static final InternalBooleanConfig showRemoteCameraSettingOnboarding = new InternalBooleanConfig("settings_activity_show_remote_camera_setting_onboarding", true);
    public static final InternalBooleanConfig showTransferCallOnboarding = new InternalBooleanConfig("settings_activity_show_transfer_call_onboarding", true);
    public static final InternalBooleanConfig showProxyOnboarding = new InternalBooleanConfig("settings_activity_show_proxy_onboarding", true);

    public static final InternalBooleanConfig showWarningEditMessage = new InternalBooleanConfig("settings_activity_show_warning_edit_message", true);
    public static final InternalLongConfig lastShowEnableNotificationScreen = new InternalLongConfig("settings_activity_last_show_enable_notifications_screen", 0);

    // Call
    public static final BooleanConfig videoCallInFitMode = new BooleanConfig("call_activity_video_call_in_fit_mode", false, "D36D6D8A-2DFF-11ED-A261-0242AC120002");
    public static final IntConfig callQualityCount = new IntConfig("call_activity_call_quality_count", 0, "DDD83ED6-3335-11ED-A261-0242AC120002");
    public static final LongConfig callQualityLastDate = new LongConfig("call_activity_call_quality_last_date", 0, "B57863E8-3336-11ED-A261-0242AC120002");

    // Update
    public static final StringConfig lastInformedVersion = new StringConfig("settings_activity_last_informed_version", null, "AEf8EfAE-40BC-11ED-B878-0242AC120002");

    // Subscription
    public static final StringConfig premiumSubscriptionInvitationTwincode = new StringConfig("in_app_subscription_activity_invitation_twincode", null, "22CA1D8D-FE44-4D94-B352-3977935FD44B");
    public static final StringConfig premiumSubscriptionInvitationImage = new StringConfig("in_app_subscription_activity_invitation_image", null, "3FAA6089-253C-4541-A9C7-3EA7D245F926");

    // Coach Mark
    public static final LongConfig lastShowCoachMarkConversationEphemeral = new LongConfig("last_show_coach_mark_conversation_ephemeral", 0, "314464E8-228B-4D0F-A1CF-43EEC8BCA45A");
    public static final LongConfig lastShowCoachMarkAddParticipantToCall = new LongConfig("last_show_coach_mark_add_participant_to_call", 0, "826A7CF6-11E3-42DD-BC53-22265FD82573");
    public static final LongConfig lastShowCoachMarkPrivacy = new LongConfig("last_show_coach_mark_privacy", 0, "8600F6A8-BFA6-4748-BCD4-3FA2B999A916");
    public static final LongConfig lastShowCoachMarkContactCapabilities = new LongConfig("last_show_coach_mark_contact_capabilities", 0, "C9BACD10-5584-4CAA-9D9B-E51A300DDFD0");
    public static final IntConfig countShowCoachMarkConversationEphemeral = new IntConfig("count_show_coach_mark_conversation_ephemeral", 0, "06EAC225-E7E0-4D07-8C6F-EF166006FE3C");
    public static final IntConfig countShowCoachMarkAddParticipantToCall = new IntConfig("count_show_coach_mark_add_participant_to_call", 0, "D3F0B2DC-14A8-4A1B-A231-F77894FA8155");
    public static final IntConfig countShowCoachMarkPrivacy = new IntConfig("count_show_coach_mark_privacy", 0, "20C49211-2465-4553-865E-9D203F402857");
    public static final IntConfig countShowCoachMarkContactCapabilities = new IntConfig("count_show_coach_mark_contact_capabilities", 0, "B9C54866-4FDC-4779-AB76-61547E1ADB2B");
    public static final BooleanConfig showCoachMark = new BooleanConfig("help_activity_show_coach_mark", true, "2088C0ED-A8E7-421B-A687-D4FCFCA4F571");

    private static Configuration sConfiguration;

    /**
     * Initialize the settings (must be called once during application startup).
     *
     * @param configurationService the configuration service.
     */
    public static void init(@NonNull ConfigurationService configurationService) {

        sConfiguration = configurationService.getConfiguration("");
    }

    /**
     * Configuration parameter which is assigned a unique UUID taken into account by the account migration service.
     *
     * @param <T> the parameter type.
     */
    public static class Config<T> extends ConfigIdentifier {
        protected final T mDefault;
        protected T mValue;

        Config(String name, T defaultValue, String uuid, Class<T> clazz) {
            super("", name, uuid, clazz);
            mDefault = defaultValue;
        }

        public Config<T> reset() {

            if (sConfiguration != null) {
                sConfiguration.removeConfig(this);
            }
            mValue = null;
            return this;
        }

        public void save() {

            if (sConfiguration != null) {
                sConfiguration.save();
            }
        }
    }

    public static class StringConfig extends Config<String> {

        StringConfig(String name, String defaultValue, String uuid) {
            super(name, defaultValue, uuid, String.class);
        }

        public String getString() {

            if (mValue != null) {

                return mValue;
            }
            if (sConfiguration == null) {

                return mDefault;
            }
            mValue = sConfiguration.getStringConfig(this, mDefault);
            return mValue;
        }

        public StringConfig setString(String value) {

            mValue = value;
            if (sConfiguration != null) {
                sConfiguration.setStringConfig(this, mValue);
            }
            return this;
        }
    }

    public static class BooleanConfig extends Config<Boolean> {

        BooleanConfig(String name, boolean defaultValue, String uuid) {
            super(name, defaultValue, uuid, Boolean.class);
        }

        public boolean getBoolean() {

            if (mValue != null) {

                return mValue;
            }
            if (sConfiguration == null) {

                return mDefault;
            }
            mValue = sConfiguration.getBoolean(getParameterName(), mDefault);
            return mValue;
        }

        public BooleanConfig setBoolean(boolean value) {

            mValue = value;
            if (sConfiguration != null) {
                sConfiguration.setBoolean(getParameterName(), mValue);
            }
            return this;
        }
    }

    public static class IntConfig extends Config<Integer> {

        IntConfig(String name, int defaultValue, String uuid) {
            super(name, defaultValue, uuid, Integer.class);
        }

        public int getInt() {

            if (mValue != null) {

                return mValue;
            }
            if (sConfiguration == null) {

                return mDefault;
            }
            mValue = sConfiguration.getInt(getParameterName(), mDefault);
            return mValue;
        }

        public IntConfig setInt(int value) {

            mValue = value;
            sConfiguration.setInt(getParameterName(), mValue);
            return this;
        }
    }

    public static class LongConfig extends Config<Long> {

        LongConfig(String name, long defaultValue, String uuid) {
            super(name, defaultValue, uuid, Long.class);
        }

        public long getLong() {

            if (mValue != null) {

                return mValue;
            }
            if (sConfiguration == null) {

                return mDefault;
            }
            mValue = sConfiguration.getLong(getParameterName(), mDefault);
            return mValue;
        }

        public LongConfig setLong(long value) {

            mValue = value;
            sConfiguration.setLong(getParameterName(), mValue);
            return this;
        }
    }

    public static class ColorConfig extends StringConfig {

        private Integer mValue;
        private final int mDefaultColor;

        ColorConfig(String name, String defaultValue, String uuid) {
            super(name, defaultValue, uuid);

            mDefaultColor = Color.parseColor(defaultValue);
        }

        public int getColor() {

            if (mValue != null) {

                return mValue;
            }

            String colorString = getString();
            mValue = CommonUtils.parseColor(colorString, mDefaultColor);
            return mValue;
        }

        @Override
        public StringConfig setString(String value) {
            mValue = CommonUtils.parseColor(value, mDefaultColor);
            return super.setString(value);
        }
    }

    /**
     * Configuration parameter which is not migrated by the account migration service.
     *
     * @param <T> the parameter type.
     */
    public static class InternalConfig<T> {
        protected final String mName;
        protected final T mDefault;
        protected T mValue;

        InternalConfig(String name, T defaultValue) {
            mName = name;
            mDefault = defaultValue;
        }

        public InternalConfig<T> reset() {

            if (sConfiguration != null) {
                sConfiguration.remove(mName);
            }
            mValue = null;
            return this;
        }

        public void save() {

            if (sConfiguration != null) {
                sConfiguration.save();
            }
        }
    }

    public static class InternalBooleanConfig extends InternalConfig<Boolean> {
        InternalBooleanConfig(String name, boolean defaultValue) {
            super(name, defaultValue);
        }

        public boolean getBoolean() {

            if (mValue != null) {

                return mValue;
            }
            if (sConfiguration == null) {

                return mDefault;
            }
            mValue = sConfiguration.getBoolean(mName, mDefault);
            return mValue;
        }

        InternalBooleanConfig setBoolean(boolean value) {
            mValue = value;
            if (sConfiguration != null) {
                sConfiguration.setBoolean(mName, mValue);
            }
            return this;
        }
    }

    public static class InternalLongConfig extends InternalConfig<Long> {
        InternalLongConfig(String name, long defaultValue) {
            super(name, defaultValue);
        }

        public long getLong() {

            if (mValue != null) {

                return mValue;
            }
            if (sConfiguration == null) {

                return mDefault;
            }
            mValue = sConfiguration.getLong(mName, mDefault);
            return mValue;
        }

        InternalLongConfig setLong(long value) {
            mValue = value;
            if (sConfiguration != null) {
                sConfiguration.setLong(mName, mValue);
            }
            return this;
        }
    }

}
