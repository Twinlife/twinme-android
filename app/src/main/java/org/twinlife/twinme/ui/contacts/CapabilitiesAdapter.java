/*
 *  Copyright (c) 2021-2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.contacts;

import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.models.Zoomable;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.conversationActivity.SelectValueViewHolder;
import org.twinlife.twinme.ui.premiumServicesActivity.UIPremiumFeature;
import org.twinlife.twinme.ui.rooms.InformationViewHolder;
import org.twinlife.twinme.utils.SectionTitleViewHolder;

public class CapabilitiesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "CapabilitiesAdapter";
    private static final boolean DEBUG = false;

    private final AbstractCapabilitiesActivity mCapabilitiesActivity;

    private static int ITEM_COUNT = 15;

    private static final int SECTION_PERMISSION = 0;
    private static final int POSITION_ALLOW_AUDIO_CALL = 1;
    private static final int POSITION_ALLOW_VIDEO_CALL = 2;

    private static int SECTION_CAMERA_CONTROL = 3;
    private static int POSITION_CAMERA_CONTROL_INFORMATION = 4;
    private static int POSITION_SELECT_CAMERA_CONTROL = 5;

    private static int SECTION_DISCREET_RELATION = 6;
    private static int POSITION_DISCREET_RELATION = 7;
    private static int POSITION_DISCREET_RELATION_INFORMATION = 8;

    private static int SECTION_ENABLE_SCHEDULE = 9;
    private static int POSITION_ENABLE_SCHEDULE_INFORMATION = 10;
    private static int POSITION_ENABLE_SCHEDULE = 11;

    private static int SECTION_ANSWERING_AUTOMATIC = 12;
    private static int POSITION_ALLOW_ANSWERING_AUTOMATIC = 13;
    private static int POSITION_INFO_ANSWERING_AUTOMATIC = 14;

    private static final int SWITCH = 1;
    private static final int SECTION = 2;
    private static final int INFO = 3;
    private static final int VALUE = 4;

    public CapabilitiesAdapter(AbstractCapabilitiesActivity listActivity) {

        mCapabilitiesActivity = listActivity;
        setHasStableIds(true);

        if (mCapabilitiesActivity.isGroup()) {
            ITEM_COUNT = 6;
            SECTION_ANSWERING_AUTOMATIC = -1;
            POSITION_ALLOW_ANSWERING_AUTOMATIC = -1;
            POSITION_INFO_ANSWERING_AUTOMATIC = -1;
            SECTION_DISCREET_RELATION = -1;
            POSITION_DISCREET_RELATION_INFORMATION = -1;
            POSITION_DISCREET_RELATION = -1;
            SECTION_CAMERA_CONTROL = -1;
            POSITION_CAMERA_CONTROL_INFORMATION = -1;
            POSITION_SELECT_CAMERA_CONTROL = -1;
            SECTION_ENABLE_SCHEDULE = 3;
            POSITION_ENABLE_SCHEDULE_INFORMATION = 4;
            POSITION_ENABLE_SCHEDULE = 5;
        }
    }

    @Override
    public int getItemCount() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemCount");
        }

        return ITEM_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemViewType: " + position);
        }

        if (position == SECTION_PERMISSION || position == SECTION_DISCREET_RELATION || position == SECTION_ENABLE_SCHEDULE || position == SECTION_ANSWERING_AUTOMATIC || position == SECTION_CAMERA_CONTROL) {
            return SECTION;
        } else if (position == POSITION_CAMERA_CONTROL_INFORMATION || position == POSITION_DISCREET_RELATION_INFORMATION || position == POSITION_ENABLE_SCHEDULE_INFORMATION || position == POSITION_INFO_ANSWERING_AUTOMATIC) {
            return INFO;
        } else if (position == POSITION_SELECT_CAMERA_CONTROL) {
            return VALUE;
        } else {
            return SWITCH;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        int viewType = getItemViewType(position);

        if (viewType == SWITCH) {
            CapabilityViewHolder capabilityViewHolder = (CapabilityViewHolder) viewHolder;

            boolean isSelected = false;
            String title = "";
            int switchTag = 0;
            boolean isEnabled = false;

            if (position == POSITION_ALLOW_AUDIO_CALL) {
                title = mCapabilitiesActivity.getString(R.string.contact_capabilities_activity_information_audio_call);
                switchTag = ContactCapabilitiesActivity.VOICE_CALL_SWITCH;
                isSelected = mCapabilitiesActivity.allowAudioCall();
            } else if (position == POSITION_ALLOW_VIDEO_CALL) {
                title = mCapabilitiesActivity.getString(R.string.contact_capabilities_activity_information_video_call);
                switchTag = ContactCapabilitiesActivity.VIDEO_CALL_SWITCH;
                isSelected = mCapabilitiesActivity.allowVideoCall();
            } else if (position == POSITION_DISCREET_RELATION) {
                title = mCapabilitiesActivity.getString(R.string.contact_capabilities_activity_discreet_relation);
                switchTag = ContactCapabilitiesActivity.SCHEDULE_SWITCH;
                isSelected = mCapabilitiesActivity.scheduleEnable();
            } else if (position == POSITION_ENABLE_SCHEDULE) {
                title = mCapabilitiesActivity.getString(R.string.show_call_activity_settings_limited);
                switchTag = ContactCapabilitiesActivity.SCHEDULE_SWITCH;
                isSelected = mCapabilitiesActivity.scheduleEnable();
            } else if (position == POSITION_ALLOW_ANSWERING_AUTOMATIC) {
                title = mCapabilitiesActivity.getString(R.string.contact_capabilities_activity_automatic_answering);
                switchTag = ContactCapabilitiesActivity.ANSWERING_AUTOMATIC_SWITCH;
                isSelected = mCapabilitiesActivity.allowAnsweringAutomatic();
            }

            capabilityViewHolder.itemView.setOnClickListener(view -> mCapabilitiesActivity.showPremiumFeatureAlert(UIPremiumFeature.FeatureType.PRIVACY));
            capabilityViewHolder.onBind(title, switchTag, isEnabled, isSelected);
        } else if (viewType == SECTION) {
            SectionTitleViewHolder sectionTitleViewHolder = (SectionTitleViewHolder) viewHolder;

            String title = "";
            boolean hideSeparator = false;
            boolean isNewFeature = false;
            if (position == SECTION_PERMISSION) {
                title = mCapabilitiesActivity.getString(R.string.settings_activity_authorization_title);
            } else if (position == SECTION_CAMERA_CONTROL) {
                title = mCapabilitiesActivity.getString(R.string.call_activity_camera_control);
                hideSeparator = true;
                isNewFeature = true;
            } else if (position == SECTION_DISCREET_RELATION) {
                title = mCapabilitiesActivity.getString(R.string.privacy_activity_title);
            } else if (position == SECTION_ENABLE_SCHEDULE) {
                title = mCapabilitiesActivity.getString(R.string.show_call_activity_schedule_call);
                hideSeparator = true;
            }

            sectionTitleViewHolder.onBind(title, hideSeparator, isNewFeature);
        } else if (viewType == INFO) {
            InformationViewHolder informationViewHolder = (InformationViewHolder) viewHolder;

            String text = "";
            boolean isSubTitle = false;
            if (position == POSITION_CAMERA_CONTROL_INFORMATION) {
                text = mCapabilitiesActivity.getString(R.string.contact_capabilities_activity_camera_control_information);
                isSubTitle = true;
            }  else if (position == POSITION_DISCREET_RELATION_INFORMATION) {
                text = mCapabilitiesActivity.getString(R.string.contact_capabilities_activity_information_discreet_relation);
            }  else if (position == POSITION_ENABLE_SCHEDULE_INFORMATION) {
                text = mCapabilitiesActivity.isGroup() ? mCapabilitiesActivity.getString(R.string.group_capabilities_activity_information_programmed_call) : mCapabilitiesActivity.getString(R.string.contact_capabilities_activity_information_programmed_call);
                isSubTitle = true;
            } else {
                SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
                spannableStringBuilder.append(mCapabilitiesActivity.getString(R.string.contact_capabilities_activity_automatic_answering_message));
                spannableStringBuilder.append("\n\n");
                spannableStringBuilder.append(mCapabilitiesActivity.getString(R.string.contact_capabilities_activity_automatic_answering_restriction));
                text = spannableStringBuilder.toString();
            }

            informationViewHolder.onBind(text, isSubTitle);
        }  else if (viewType == VALUE) {
            SelectValueViewHolder selectValueViewHolder = (SelectValueViewHolder) viewHolder;
            selectValueViewHolder.itemView.setOnClickListener(v -> mCapabilitiesActivity.onSelectControlCamera());

            String value;
            if (mCapabilitiesActivity.getZoomable() == Zoomable.NEVER) {
                value = mCapabilitiesActivity.getString(R.string.contact_capabilities_activity_camera_control_never);
            } else if (mCapabilitiesActivity.getZoomable() == Zoomable.ALLOW) {
                value = mCapabilitiesActivity.getString(R.string.contact_capabilities_activity_camera_control_allow);
            } else {
                value = mCapabilitiesActivity.getString(R.string.contact_capabilities_activity_camera_control_ask);
            }

            selectValueViewHolder.onBind(null, value, false, Design.WHITE_COLOR);
        }
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mCapabilitiesActivity.getLayoutInflater();
        View convertView;

        if (viewType == SECTION) {
            convertView = inflater.inflate(R.layout.section_title_item, parent, false);
            return new SectionTitleViewHolder(convertView);
        } else if (viewType == INFO) {
            convertView = inflater.inflate(R.layout.settings_room_activity_information_item, parent, false);
            return new InformationViewHolder(convertView);
        } else if (viewType == SWITCH) {
            convertView = inflater.inflate(R.layout.contact_capabilities_activity_item, parent, false);
            return new CapabilityViewHolder(convertView, mCapabilitiesActivity);
        } else {
            convertView = inflater.inflate(R.layout.select_value_item, parent, false);
            return new SelectValueViewHolder(convertView);
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }
    }
}