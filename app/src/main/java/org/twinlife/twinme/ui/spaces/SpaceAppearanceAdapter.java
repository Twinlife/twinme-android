/*
 *  Copyright (c) 2020-2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Romain Kolb (romain.kolb@skyrock.com)
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
import org.twinlife.twinme.skin.DisplayMode;
import org.twinlife.twinme.ui.rooms.InformationViewHolder;
import org.twinlife.twinme.ui.settingsActivity.DisplayModeViewHolder;
import org.twinlife.twinme.ui.settingsActivity.SettingSectionViewHolder;
import org.twinlife.twinme.utils.SectionTitleViewHolder;

public class SpaceAppearanceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "SpaceAppearanceAdapter";
    private static final boolean DEBUG = false;

    public interface OnSpaceAppearanceClickListener {

        void onUpdateDisplayMode(DisplayMode displayMode);

        void onConversationAppearanceClick();

        void onUpdateMainColorClick();
    }

    @NonNull
    private final SpaceAppearanceActivity mListActivity;

    private final static int ITEM_COUNT = 6;

    private static final int SECTION_INFO = 0;
    private static final int SECTION_MODE = 1;
    private static final int SECTION_APPEARANCE = 3;

    private static final int POSITION_DISPLAY_MODE = 2;
    private static final int POSITION_APPEARANCE_MAIN_COLOR = 4;

    private static final int TITLE = 0;
    private static final int INFO = 1;
    private static final int DISPLAY_MODE = 2;
    private static final int COLOR = 3;
    private static final int SUBSECTION = 4;

    @Nullable
    private Space mSpace;

    private final OnSpaceAppearanceClickListener mOnSpaceAppearanceClickListener;

    public SpaceAppearanceAdapter(@NonNull SpaceAppearanceActivity listActivity, OnSpaceAppearanceClickListener onSpaceAppearanceClickListener) {

        mListActivity = listActivity;
        mOnSpaceAppearanceClickListener = onSpaceAppearanceClickListener;
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

        if (position == SECTION_INFO) {
            return INFO;
        } else if (position == SECTION_MODE || position == SECTION_APPEARANCE) {
            return TITLE;
        } else if (position == POSITION_DISPLAY_MODE) {
            return DISPLAY_MODE;
        } else if (position == POSITION_APPEARANCE_MAIN_COLOR) {
            return COLOR;
        } else {
            return SUBSECTION;
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
                case SECTION_MODE:
                    sectionTitleViewHolder.onBind(mListActivity.getString(R.string.personalization_activity_mode), false);
                    break;

                case SECTION_APPEARANCE:
                    sectionTitleViewHolder.onBind(mListActivity.getString(R.string.application_appearance), false);
                    break;

                default:
                    break;
            }
        } else if (viewType == DISPLAY_MODE) {
            DisplayModeViewHolder displayModeViewHolder = (DisplayModeViewHolder) viewHolder;
            int displayMode = mListActivity.getDisplayMode().ordinal();
            displayModeViewHolder.onBind(displayMode, mListActivity.getMainColor());
        } else if (viewType == SUBSECTION) {
            SettingSectionViewHolder settingSectionViewHolder = (SettingSectionViewHolder) viewHolder;
            settingSectionViewHolder.itemView.setOnClickListener(view -> mOnSpaceAppearanceClickListener.onConversationAppearanceClick());
            settingSectionViewHolder.onBind(mListActivity.getString(R.string.conversations_fragment_title), true);
        } else if (viewType == COLOR) {
            AppearanceColorViewHolder appearanceColorViewHolder = (AppearanceColorViewHolder) viewHolder;
            appearanceColorViewHolder.itemView.setOnClickListener(view -> mOnSpaceAppearanceClickListener.onUpdateMainColorClick());
            appearanceColorViewHolder.onBind(mListActivity.getMainColor(), mListActivity.getString(R.string.space_appearance_activity_theme), null, false);
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
        } else if (viewType == DISPLAY_MODE) {
            convertView = inflater.inflate(R.layout.personalization_activity_mode_item, parent, false);
            DisplayModeViewHolder.Observer observer = mOnSpaceAppearanceClickListener::onUpdateDisplayMode;
            return new DisplayModeViewHolder(convertView, observer);
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