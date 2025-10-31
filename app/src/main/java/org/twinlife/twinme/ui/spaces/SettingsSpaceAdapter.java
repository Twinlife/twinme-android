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
import org.twinlife.twinme.ui.settingsActivity.SettingSectionViewHolder;
import org.twinlife.twinme.utils.SectionTitleViewHolder;

public class SettingsSpaceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "SettingsSpaceAdapter";
    private static final boolean DEBUG = false;

    public interface OnSettingsSpaceClickListener {

        void onSettingsAppearanceClick();

        void onSettingsMessageClick();

        void onSettingsNotificationClick();
    }

    @NonNull
    private final SettingsSpaceActivity mListActivity;

    private final static int ITEM_COUNT_WITHOUT_PERMISSIONS = 10;
    private final static int ITEM_COUNT = 17;

    private static final int SECTION_INFO = 0;
    private static final int SECTION_APPEARANCE = 1;
    private static final int SECTION_MESSAGE = 4;
    private static final int SECTION_NOTIFICATION = 7;
    private static final int SECTION_PERMISSIONS = 10;

    private static final int POSITION_APPEARANCE_DEFAULT = 2;
    private static final int POSITION_APPEARANCE_SETTINGS = 3;
    private static final int POSITION_MESSAGE_DEFAULT = 5;
    private static final int POSITION_MESSAGE_SETTINGS = 6;
    private static final int POSITION_NOTIFICATION_DEFAULT = 8;
    private static final int POSITION_NOTIFICATION_SETTINGS = 9;
    private static final int POSITION_PERMISSION_SHARE_SPACE_CARD = 11;
    private static final int POSITION_PERMISSION_CREATE_CONTACT = 12;
    private static final int POSITION_PERMISSION_MOVE_CONTACT = 13;
    private static final int POSITION_PERMISSION_CREATE_GROUP = 14;
    private static final int POSITION_PERMISSION_MOVE_GROUP = 15;
    private static final int POSITION_PERMISSION_UPDATE_IDENTITY = 16;

    private static final int TITLE = 0;
    private static final int CHECKBOX = 1;
    private static final int INFO = 2;
    private static final int SUBSECTION = 3;

    private boolean mDisplaySpacePermissions = false;

    @Nullable
    private Space mSpace;

    private final OnSettingsSpaceClickListener mOnSettingsSpaceClickListener;

    private final SettingSpaceViewHolder.Observer mSettingSpaceViewHolderObserver;

    public SettingsSpaceAdapter(@NonNull SettingsSpaceActivity listActivity, OnSettingsSpaceClickListener onSettingsSpaceClickListener, SettingSpaceViewHolder.Observer settingSpaceViewHolderObserver) {

        mListActivity = listActivity;
        mOnSettingsSpaceClickListener = onSettingsSpaceClickListener;
        mSettingSpaceViewHolderObserver = settingSpaceViewHolderObserver;
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

        if (mDisplaySpacePermissions) {
            return ITEM_COUNT;
        }
        return ITEM_COUNT_WITHOUT_PERMISSIONS;
    }

    @Override
    public int getItemViewType(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemViewType: " + position);
        }

        if (position == SECTION_INFO) {
            return INFO;
        } else if (position == SECTION_NOTIFICATION || position == SECTION_MESSAGE || position == SECTION_APPEARANCE || position == SECTION_PERMISSIONS) {
            return TITLE;
        } else if (position == POSITION_MESSAGE_SETTINGS || position == POSITION_APPEARANCE_SETTINGS || position == POSITION_NOTIFICATION_SETTINGS) {
            return SUBSECTION;
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
            informationViewHolder.onBind(mListActivity.getString(R.string.settings_space_activity_header_message), false);
        } else if (viewType == TITLE) {
            SectionTitleViewHolder sectionTitleViewHolder = (SectionTitleViewHolder) viewHolder;
            switch (position) {
                case SECTION_NOTIFICATION:
                    sectionTitleViewHolder.onBind(mListActivity.getString(R.string.notifications_fragment_title), false);
                    break;

                case SECTION_MESSAGE:
                    sectionTitleViewHolder.onBind(mListActivity.getString(R.string.settings_activity_chat_category_title), true);
                    break;

                case SECTION_APPEARANCE:
                    sectionTitleViewHolder.onBind(mListActivity.getString(R.string.application_appearance), false);
                    break;

                case SECTION_PERMISSIONS:
                    sectionTitleViewHolder.onBind(mListActivity.getString(R.string.settings_activity_permissions_title), false);
                    break;

                default:
                    break;
            }
        } else if (viewType == SUBSECTION) {
            SettingSectionViewHolder settingSectionViewHolder = (SettingSectionViewHolder) viewHolder;
            boolean value = false;
            if (position == POSITION_MESSAGE_SETTINGS) {
                settingSectionViewHolder.itemView.setOnClickListener(view -> mOnSettingsSpaceClickListener.onSettingsMessageClick());
                value = mSpace != null && mSpace.getSpaceSettings().getBoolean(SpaceSettingProperty.PROPERTY_DEFAULT_MESSAGE_SETTINGS, true);
            } else if (position == POSITION_APPEARANCE_SETTINGS) {
                settingSectionViewHolder.itemView.setOnClickListener(view -> mOnSettingsSpaceClickListener.onSettingsAppearanceClick());
                value = mSpace != null && mSpace.getSpaceSettings().getBoolean(SpaceSettingProperty.PROPERTY_DEFAULT_APPEARANCE_SETTINGS, true);
            } else if (position == POSITION_NOTIFICATION_SETTINGS) {
                settingSectionViewHolder.itemView.setOnClickListener(view -> mOnSettingsSpaceClickListener.onSettingsNotificationClick());
                value = mSpace != null && mSpace.getSpaceSettings().getBoolean(SpaceSettingProperty.PROPERTY_DEFAULT_NOTIFICATION_SETTINGS, true);
            }
            settingSectionViewHolder.onBind(mListActivity.getString(R.string.settings_space_activity_default_value_title), !value);
        } else if (viewType == CHECKBOX) {
            SettingSpaceViewHolder settingsViewHolder = (SettingSpaceViewHolder) viewHolder;

            String spaceSettingProperty = null;
            String title = null;
            boolean value = false;

            switch (position) {
                case POSITION_APPEARANCE_DEFAULT:
                    spaceSettingProperty = SpaceSettingProperty.PROPERTY_DEFAULT_APPEARANCE_SETTINGS;
                    title = mListActivity.getString(R.string.navigation_activity_application_settings);
                    value = mSpace != null && mSpace.getSpaceSettings().getBoolean(SpaceSettingProperty.PROPERTY_DEFAULT_APPEARANCE_SETTINGS, true);
                    break;

                case POSITION_MESSAGE_DEFAULT:
                    spaceSettingProperty = SpaceSettingProperty.PROPERTY_DEFAULT_MESSAGE_SETTINGS;
                    title = mListActivity.getString(R.string.navigation_activity_application_settings);
                    value = mSpace != null && mSpace.getSpaceSettings().getBoolean(SpaceSettingProperty.PROPERTY_DEFAULT_MESSAGE_SETTINGS, true);
                    break;

                case POSITION_NOTIFICATION_DEFAULT:
                    spaceSettingProperty = SpaceSettingProperty.PROPERTY_DEFAULT_NOTIFICATION_SETTINGS;
                    title = mListActivity.getString(R.string.navigation_activity_application_settings);
                    value = mSpace != null && mSpace.getSpaceSettings().getBoolean(SpaceSettingProperty.PROPERTY_DEFAULT_NOTIFICATION_SETTINGS, true);
                    break;

                case POSITION_PERMISSION_SHARE_SPACE_CARD:
                    spaceSettingProperty = SpaceSettingProperty.PROPERTY_PERMISSION_SHARE_SPACE_CARD;
                    title = mListActivity.getString(R.string.settings_space_activity_permission_share_space_card);
                    value = mSpace != null && mSpace.hasPermission(Space.Permission.SHARE_SPACE_CARD);
                    break;

                case POSITION_PERMISSION_CREATE_CONTACT:
                    spaceSettingProperty = SpaceSettingProperty.PROPERTY_PERMISSION_CREATE_CONTACT;
                    title = mListActivity.getString(R.string.settings_space_activity_permission_create_contact);
                    value = mSpace != null && mSpace.hasPermission(Space.Permission.CREATE_CONTACT);
                    break;

                case POSITION_PERMISSION_MOVE_CONTACT:
                    spaceSettingProperty = SpaceSettingProperty.PROPERTY_PERMISSION_MOVE_CONTACT;
                    title = mListActivity.getString(R.string.settings_space_activity_permission_move_contact);
                    value = mSpace != null && mSpace.hasPermission(Space.Permission.MOVE_CONTACT);
                    break;

                case POSITION_PERMISSION_CREATE_GROUP:
                    spaceSettingProperty = SpaceSettingProperty.PROPERTY_PERMISSION_CREATE_GROUP;
                    title = mListActivity.getString(R.string.settings_space_activity_permission_create_group);
                    value = mSpace != null && mSpace.hasPermission(Space.Permission.CREATE_GROUP);
                    break;

                case POSITION_PERMISSION_MOVE_GROUP:
                    spaceSettingProperty = SpaceSettingProperty.PROPERTY_PERMISSION_MOVE_GROUP;
                    title = mListActivity.getString(R.string.settings_space_activity_permission_move_group);
                    value = mSpace != null && mSpace.hasPermission(Space.Permission.MOVE_GROUP);
                    break;

                case POSITION_PERMISSION_UPDATE_IDENTITY:
                    spaceSettingProperty = SpaceSettingProperty.PROPERTY_PERMISSION_UPDATE_IDENTITY;
                    title = mListActivity.getString(R.string.settings_space_activity_permission_update_identity);
                    value = mSpace != null && mSpace.hasPermission(Space.Permission.UPDATE_IDENTITY);
                    break;

                default:
                    break;
            }

            settingsViewHolder.onBind(spaceSettingProperty, title, value, true);
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
        } else if (viewType == SUBSECTION) {
            convertView = inflater.inflate(R.layout.settings_activity_item_section, parent, false);
            return new SettingSectionViewHolder(convertView);
        } else {
            convertView = inflater.inflate(R.layout.space_appearance_activity_appearance_color_item, parent, false);
            return new AppearanceColorViewHolder(convertView);
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }
    }
}