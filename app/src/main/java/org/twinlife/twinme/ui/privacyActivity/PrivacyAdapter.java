/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui.privacyActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.ui.Settings;
import org.twinlife.twinme.ui.rooms.InformationViewHolder;
import org.twinlife.twinme.ui.settingsActivity.SettingSwitchViewHolder;
import org.twinlife.twinme.ui.settingsActivity.SettingValueViewHolder;
import org.twinlife.twinme.ui.settingsActivity.UISetting;
import org.twinlife.twinme.utils.SectionTitleViewHolder;

public class PrivacyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "MessagesSettingsAdapter";
    private static final boolean DEBUG = false;

    @NonNull
    private final PrivacyActivity mPrivacyActivity;

    private final static int ITEM_COUNT = 5;

    private static final int POSITION_LOCK_SCREEN = 0;
    private static final int POSITION_LOCK_SCREEN_INFORMATION = 1;
    private static final int SECTION_PREVENT_SCREENSHOT = 2;
    private static final int POSITION_PREVENT_SCREENSHOT = 3;
    private static final int POSITION_PREVENT_SCREENSHOT_INFORMATION = 4;

    private static final int TITLE = 0;
    private static final int CHECKBOX = 1;
    private static final int INFO = 2;

    PrivacyAdapter(@NonNull PrivacyActivity listActivity) {

        mPrivacyActivity = listActivity;
        setHasStableIds(false);
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

        if (position == POSITION_LOCK_SCREEN_INFORMATION || position == POSITION_PREVENT_SCREENSHOT_INFORMATION ) {
            return INFO;
        } else if (position == SECTION_PREVENT_SCREENSHOT ) {
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
            String message = "";
            if (position == POSITION_LOCK_SCREEN_INFORMATION) {
                message = mPrivacyActivity.getString(R.string.privacy_activity_lock_screen_message);
            } else if (position == POSITION_PREVENT_SCREENSHOT_INFORMATION) {
                message = mPrivacyActivity.getString(R.string.privacy_activity_hide_last_screen_message);
            }
            informationViewHolder.onBind(message, false);

        } else if (viewType == TITLE) {
            SectionTitleViewHolder sectionTitleViewHolder = (SectionTitleViewHolder) viewHolder;
            sectionTitleViewHolder.onBind("", false);
        } else if (viewType == CHECKBOX) {
            SettingSwitchViewHolder settingsViewHolder = (SettingSwitchViewHolder) viewHolder;
            settingsViewHolder.itemView.setOnClickListener(v -> mPrivacyActivity.onPremiumFeatureClick());

            String title = "";
            if (position == POSITION_LOCK_SCREEN) {
                title = mPrivacyActivity.getString(R.string.privacy_activity_lock_screen_title);
            } else if (position == POSITION_PREVENT_SCREENSHOT) {
                title = mPrivacyActivity.getString(R.string.privacy_activity_hide_last_screen_title);
            }

            settingsViewHolder.onBind(title, false, false);
        }
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mPrivacyActivity.getLayoutInflater();
        View convertView;

        if (viewType == INFO) {
            convertView = inflater.inflate(R.layout.settings_room_activity_information_item, parent, false);
            return new InformationViewHolder(convertView);
        } else if (viewType == TITLE) {
            convertView = inflater.inflate(R.layout.section_title_item, parent, false);
            return new SectionTitleViewHolder(convertView);
        } else {
            convertView = inflater.inflate(R.layout.settings_activity_item_switch, parent, false);
            return new SettingSwitchViewHolder(convertView, mPrivacyActivity);
        }
    }
}
