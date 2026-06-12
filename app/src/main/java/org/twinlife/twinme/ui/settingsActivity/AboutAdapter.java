/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.settingsActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.BuildConfig;
import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.ui.rooms.InformationViewHolder;
import org.twinlife.twinme.utils.SectionTitleViewHolder;

import java.util.List;

public class AboutAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "AboutAdapter";
    private static final boolean DEBUG = false;

    @NonNull
    private final AboutActivity mActivity;

    private static final int SECTION = 0;
    private static final int VALUE = 1;
    private static final int UPDATE = 2;
    private static final int SUBSECTION = 3;
    private static final int INFO = 4;
    private static final int ABOUT = 5;

    private final List<UIAboutItem> mItems = new java.util.ArrayList<>();

    AboutAdapter(@NonNull AboutActivity aboutActivity) {

        mActivity = aboutActivity;
        initItems();
        setHasStableIds(false);
    }

    @Override
    public int getItemCount() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemCount");
        }

        return mItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemViewType: " + position);
        }

        UIAboutItem item = mItems.get(position);

        switch (item.getType()) {
            case LEGAL_SECTION:
            case OPEN_SOURCE_SECTION:
                return SECTION;

            case CURRENT_VERSION:
                return VALUE;

            case TERMS_OF_USE:
            case PRIVACY_POLICY:
            case SOURCE_CODE:
            case OPEN_SOURCE_LICENSES:
                return SUBSECTION;

            case UPDATE_AVAILABLE:
                return UPDATE;

            case SOURCE_CODE_INFO:
                return INFO;

            case HEADER:
            case COPYRIGHT:
                return ABOUT;

            default:
                return -1;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        int viewType = getItemViewType(position);

        UIAboutItem item = mItems.get(position);
        if (viewType == SECTION) {
            SectionTitleViewHolder sectionTitleViewHolder = (SectionTitleViewHolder) viewHolder;
            sectionTitleViewHolder.onBind(item.geText(), false);
        } else if (viewType == VALUE) {
            SettingValueViewHolder settingValueViewHolder = (SettingValueViewHolder) viewHolder;
            settingValueViewHolder.itemView.setOnClickListener(view -> mActivity.onAboutItemClick(item.getType()));
            if (mActivity.getTwinmeApplication().getLastVersion() != null) {
                settingValueViewHolder.onBind(item.geText(), BuildConfig.VERSION_NAME);
            } else {
                settingValueViewHolder.onBind(item.geText(), "");
            }
        } else if (viewType == SUBSECTION) {
            SettingSectionViewHolder settingSectionViewHolder = (SettingSectionViewHolder) viewHolder;
            settingSectionViewHolder.itemView.setOnClickListener(view -> mActivity.onAboutItemClick(item.getType()));
            settingSectionViewHolder.onBind(item.geText(), false);
        } else if (viewType == UPDATE) {
            UpdateAvailableViewHolder updateAvailableViewHolder = (UpdateAvailableViewHolder) viewHolder;
            updateAvailableViewHolder.itemView.setOnClickListener(view -> mActivity.onAboutItemClick(item.getType()));
            if (mActivity.getTwinmeApplication().getLastVersion() != null) {
                updateAvailableViewHolder.onBind(mActivity.getTwinmeApplication().getLastVersion().getVersionNumber());
            } else {
                updateAvailableViewHolder.onBind("");
            }
        } else if (viewType == INFO) {
            InformationViewHolder informationViewHolder = (InformationViewHolder) viewHolder;
            informationViewHolder.onBind(item.geText(), false);
        } else if (viewType == ABOUT) {
            AboutItemViewHolder aboutItemViewHolder = (AboutItemViewHolder) viewHolder;
            aboutItemViewHolder.onBind(item.getType() == UIAboutItem.AboutItemType.COPYRIGHT);
        }
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mActivity.getLayoutInflater();
        View convertView;

        if (viewType == SECTION) {
            convertView = inflater.inflate(R.layout.section_title_item, parent, false);
            return new SectionTitleViewHolder(convertView);
        } else if (viewType == SUBSECTION) {
            convertView = inflater.inflate(R.layout.settings_activity_item_section, parent, false);
            return new SettingSectionViewHolder(convertView);
        } else if (viewType == VALUE) {
            convertView = inflater.inflate(R.layout.settings_activity_item_value, parent, false);
            return new SettingValueViewHolder(convertView);
        } else if (viewType == ABOUT) {
            convertView = inflater.inflate(R.layout.about_activity_item, parent, false);
            return new AboutItemViewHolder(convertView);
        } else if (viewType == UPDATE) {
            convertView = inflater.inflate(R.layout.update_available_item, parent, false);
            return new UpdateAvailableViewHolder(convertView);
        } else if (viewType == INFO) {
            convertView = inflater.inflate(R.layout.settings_room_activity_information_item, parent, false);
            return new InformationViewHolder(convertView);
        } else {
            convertView = inflater.inflate(R.layout.section_title_item, parent, false);
            return new SectionTitleViewHolder(convertView);
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }
    }

    private void initItems() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initItems");
        }

        mItems.clear();

        mItems.add(new UIAboutItem(UIAboutItem.AboutItemType.HEADER, mActivity.getString(R.string.about_view_message)));
        mItems.add(new UIAboutItem(UIAboutItem.AboutItemType.CURRENT_VERSION, mActivity.getString(R.string.about_view_version)));

        if (mActivity.getTwinmeApplication().getLastVersion() != null && mActivity.getTwinmeApplication().getLastVersion().hasNewVersion()) {
            mItems.add(new UIAboutItem(UIAboutItem.AboutItemType.UPDATE_AVAILABLE, mActivity.getString(R.string.update_app_view_update_available)));
        }

        mItems.add(new UIAboutItem(UIAboutItem.AboutItemType.LEGAL_SECTION, mActivity.getString(R.string.about_view_legal)));
        mItems.add(new UIAboutItem(UIAboutItem.AboutItemType.TERMS_OF_USE, mActivity.getString(R.string.about_view_terms_of_use)));
        mItems.add(new UIAboutItem(UIAboutItem.AboutItemType.PRIVACY_POLICY, mActivity.getString(R.string.about_view_privacy_policy)));
        mItems.add(new UIAboutItem(UIAboutItem.AboutItemType.OPEN_SOURCE_SECTION, mActivity.getString(R.string.about_view_open_source)));
        mItems.add(new UIAboutItem(UIAboutItem.AboutItemType.SOURCE_CODE, mActivity.getString(R.string.about_view_application_code)));
        mItems.add(new UIAboutItem(UIAboutItem.AboutItemType.SOURCE_CODE_INFO, mActivity.getString(R.string.about_view_open_source_information) + "\n\n"));
        mItems.add(new UIAboutItem(UIAboutItem.AboutItemType.OPEN_SOURCE_LICENSES, mActivity.getString(R.string.about_view_open_sources_licences)));
        mItems.add(new UIAboutItem(UIAboutItem.AboutItemType.COPYRIGHT, mActivity.getString(R.string.about_view_copyright)));

        notifyItemRangeChanged(0, mItems.size());
    }
}