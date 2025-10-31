/*
 *  Copyright (c) 2022 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.spaces;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.models.Space;
import org.twinlife.twinme.ui.rooms.InformationViewHolder;
import org.twinlife.twinme.utils.SectionTitleViewHolder;

public class MessageSettingsSpaceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "MessageSettingsS...";
    private static final boolean DEBUG = false;

    @NonNull
    private final MessageSettingsSpaceActivity mListActivity;

    private final static int ITEM_COUNT;

    private static final int SECTION_INFO = 0;
    private static final int SECTION_COPY = 1;
    private static final int SECTION_EPHEMERAL = 5;

    private static final int POSITION_ALLOW_COPY_INFORMATION = 2;
    private static final int POSITION_ALLOW_COPY_TEXT = 3;
    private static final int POSITION_ALLOW_COPY_FILE = 4;
    private static final int POSITION_EPHEMERAL_INFORMATION = 6;
    private static final int POSITION_ALLOW_EPHEMERAL = 7;
    private static final int POSITION_TIMEOUT_EPHEMERAL = 8;

    private static final int TITLE = 0;
    private static final int CHECKBOX = 1;
    private static final int INFO = 2;
    private static final int VALUE = 3;

    static {
        ITEM_COUNT = 9;
    }

    @Nullable
    private Space mSpace;

    private final SettingSpaceViewHolder.Observer mSettingSpaceViewHolderObserver;

    private final SettingValueSpaceViewHolder.Observer mSettingValueSpaceViewHolderObserver;

    MessageSettingsSpaceAdapter(@NonNull MessageSettingsSpaceActivity listActivity, SettingSpaceViewHolder.Observer settingSpaceViewHolderObserver, SettingValueSpaceViewHolder.Observer settingValueSpaceViewHolderObserver) {

        mListActivity = listActivity;
        mSettingSpaceViewHolderObserver = settingSpaceViewHolderObserver;
        mSettingValueSpaceViewHolderObserver = settingValueSpaceViewHolderObserver;
        setHasStableIds(true);
    }

    public void setSpace(Space space) {

        mSpace = space;
        notifyDataSetChanged();
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

        if (position == SECTION_INFO || position == POSITION_ALLOW_COPY_INFORMATION || position == POSITION_EPHEMERAL_INFORMATION) {
            return INFO;
        } else if (position == SECTION_COPY || position == SECTION_EPHEMERAL) {
            return TITLE;
        } else if (position == POSITION_TIMEOUT_EPHEMERAL) {
            return VALUE;
        } else {
            return CHECKBOX;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        int viewType = getItemViewType(position);

        if (viewType == INFO) {
            InformationViewHolder informationViewHolder = (InformationViewHolder) viewHolder;
            switch (position) {
                case SECTION_INFO:
                    informationViewHolder.onBind(mListActivity.getString(R.string.settings_space_activity_default_value_message), false);
                    break;

                case POSITION_ALLOW_COPY_INFORMATION:
                    informationViewHolder.onBind(mListActivity.getString(R.string.settings_activity_allow_copy_category_title), true);
                    break;

                case POSITION_EPHEMERAL_INFORMATION:
                    informationViewHolder.onBind(mListActivity.getString(R.string.settings_activity_ephemeral_message), true);
                    break;

                default:
                    break;
            }
        } else if (viewType == TITLE) {
            SectionTitleViewHolder sectionTitleViewHolder = (SectionTitleViewHolder) viewHolder;
            switch (position) {
                case SECTION_COPY:
                    sectionTitleViewHolder.onBind(mListActivity.getString(R.string.settings_activity_permissions_title), true);
                    break;

                case SECTION_EPHEMERAL:
                    sectionTitleViewHolder.onBind(mListActivity.getString(R.string.settings_activity_ephemeral_section_title), true);
                    break;

                default:
                    break;
            }
        } else if (viewType == CHECKBOX) {
            SettingSpaceViewHolder settingsViewHolder = (SettingSpaceViewHolder) viewHolder;

            String title = "";
            String spaceSettingProperty = "";
            boolean value = false;

            switch (position) {
                case POSITION_ALLOW_COPY_TEXT:
                    title = mListActivity.getString(R.string.settings_activity_allow_copy_text_title);
                    spaceSettingProperty = SpaceSettingProperty.PROPERTY_ALLOW_COPY_TEXT;
                    value = mSpace != null && mSpace.getSpaceSettings().messageCopyAllowed();
                    break;

                case POSITION_ALLOW_COPY_FILE:
                    title = mListActivity.getString(R.string.settings_activity_allow_copy_file_title);
                    spaceSettingProperty = SpaceSettingProperty.PROPERTY_ALLOW_COPY_FILE;
                    value = mSpace != null && mSpace.getSpaceSettings().fileCopyAllowed();
                    break;

                case POSITION_ALLOW_EPHEMERAL:
                    title = mListActivity.getString(R.string.settings_activity_ephemeral_title);
                    spaceSettingProperty = SpaceSettingProperty.PROPERTY_ALLOW_EPHEMERAL_MESSAGE;
                    value = mSpace != null && mSpace.getSpaceSettings().getBoolean(SpaceSettingProperty.PROPERTY_ALLOW_EPHEMERAL_MESSAGE, false);
                    break;
            }
            settingsViewHolder.onBind(spaceSettingProperty, title, value, true);
        } else if (viewType == VALUE) {
            SettingValueSpaceViewHolder settingValueViewHolder = (SettingValueSpaceViewHolder) viewHolder;
            boolean allowEphemeral = mSpace != null && mSpace.getSpaceSettings().getBoolean(SpaceSettingProperty.PROPERTY_ALLOW_EPHEMERAL_MESSAGE, false);
            long expireTimeout = mSpace != null ? Long.parseLong(mSpace.getSpaceSettings().getString(SpaceSettingProperty.PROPERTY_TIMEOUT_EPHEMERAL_MESSAGE, SpaceSettingProperty.DEFAULT_TIMEOUT_MESSAGE + "")) : SpaceSettingProperty.DEFAULT_TIMEOUT_MESSAGE;
            settingValueViewHolder.onBind(SpaceSettingProperty.PROPERTY_TIMEOUT_EPHEMERAL_MESSAGE, mListActivity.getString(R.string.application_timeout), expireTimeout, allowEphemeral);
        }
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mListActivity.getLayoutInflater();
        View convertView;

        if (viewType == INFO) {
            convertView = inflater.inflate(R.layout.settings_room_activity_information_item, parent, false);
            return new InformationViewHolder(convertView);
        } else if (viewType == TITLE) {
            convertView = inflater.inflate(R.layout.section_title_item, parent, false);
            return new SectionTitleViewHolder(convertView);
        } else if (viewType == CHECKBOX) {
            convertView = inflater.inflate(R.layout.settings_activity_item_switch, parent, false);
            return new SettingSpaceViewHolder(convertView, mSettingSpaceViewHolderObserver);
        } else {
            convertView = inflater.inflate(R.layout.setting_activity_item_value, parent, false);
            return new SettingValueSpaceViewHolder(convertView, mSettingValueSpaceViewHolderObserver);
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }
    }
}
