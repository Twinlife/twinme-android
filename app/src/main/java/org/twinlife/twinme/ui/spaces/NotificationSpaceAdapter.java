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

public class NotificationSpaceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "NotificationSpaceA...";
    private static final boolean DEBUG = false;

    @NonNull
    private final NotificationSpaceActivity mListActivity;

    private final static int ITEM_COUNT = 4;

    private static final int SECTION_INFO = 0;
    private static final int SECTION_NOTIFICATION = 1;

    private static final int POSITION_ALLOW_NOTIFICATIONS_INFO = 3;

    private static final int TITLE = 0;
    private static final int CHECKBOX = 1;
    private static final int INFO = 2;

    @Nullable
    private Space mSpace;

    private final SettingSpaceViewHolder.Observer mSettingSpaceViewHolderObserver;

    public NotificationSpaceAdapter(@NonNull NotificationSpaceActivity listActivity, SettingSpaceViewHolder.Observer settingSpaceViewHolderObserver) {

        mListActivity = listActivity;
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

        return ITEM_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemViewType: " + position);
        }

        if (position == SECTION_INFO || position == POSITION_ALLOW_NOTIFICATIONS_INFO) {
            return INFO;
        } else if (position == SECTION_NOTIFICATION) {
            return TITLE;
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
                    informationViewHolder.onBind(mListActivity.getString(R.string.settings_space_activity_header_message), false);
                    break;

                case POSITION_ALLOW_NOTIFICATIONS_INFO:
                    informationViewHolder.onBind(mListActivity.getString(R.string.settings_space_activity_allow_notifications_message), true);
                    break;
                default:
                    break;
            }
        } else if (viewType == TITLE) {
            SectionTitleViewHolder sectionTitleViewHolder = (SectionTitleViewHolder) viewHolder;
            sectionTitleViewHolder.onBind(mListActivity.getString(R.string.notifications_fragment_title), false);
        } else if (viewType == CHECKBOX) {
            SettingSpaceViewHolder settingsViewHolder = (SettingSpaceViewHolder) viewHolder;
            String spaceSettingProperty = SpaceSettingProperty.PROPERTY_DISPLAY_NOTIFICATIONS;;
            String title = mListActivity.getString(R.string.settings_space_activity_allow_notifications);
            boolean value = mSpace != null && mSpace.getSpaceSettings().getBoolean(SpaceSettingProperty.PROPERTY_DISPLAY_NOTIFICATIONS, true);

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
        } else {
            convertView = inflater.inflate(R.layout.settings_activity_item_switch, parent, false);
            return new SettingSpaceViewHolder(convertView, mSettingSpaceViewHolderObserver);
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }
    }
}