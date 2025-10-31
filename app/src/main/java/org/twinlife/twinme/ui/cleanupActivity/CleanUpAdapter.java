/*
 *  Copyright (c) 2023-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.cleanupActivity;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.ui.exportActivity.ExportContentViewHolder;
import org.twinlife.twinme.ui.exportActivity.UIExport;
import org.twinlife.twinme.ui.rooms.InformationViewHolder;
import org.twinlife.twinme.utils.SectionTitleViewHolder;

import java.util.List;

public class CleanUpAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "CleanUpAdapter";
    private static final boolean DEBUG = false;

    public interface OnCleanupClickListener {

        void onContentClick(UIExport export);

        void onSelectExpiration();
    }

    private final CleanUpActivity mCleanupActivity;

    private final OnCleanupClickListener mOnCleanupClickListener;

    private static int ITEM_COUNT = 16;

    private static final int SECTION_STORAGE = 0;
    private static final int SECTION_CONTENT = 6;
    private static final int SECTION_EXPIRATION = 12;

    private static final int POSITION_STORAGE_CHART = 1;
    private static final int POSITION_CONTENT_INFORMATION = 7;
    private static final int POSITION_CONTENT_FILE = 8;
    private static final int POSITION_CONTENT_FILE_INFORMATION = 9;
    private static final int POSITION_CONTENT_MESSAGES = 10;
    private static final int POSITION_CONTENT_MESSAGES_INFORMATION = 11;
    private static final int POSITION_EXPIRATION_ALL = 13;
    private static int POSITION_EXPIRATION_CUSTOM = 14;
    private static int POSITION_CLEAN_ACTION = 15;

    private static final int TITLE = 0;
    private static final int INFO = 1;
    private static final int CONTENT = 2;
    private static final int ACTION = 3;
    private static final int STORAGE = 4;
    private static final int STORAGE_CHART = 5;
    private static final int VALUE = 6;
    private static final int SWITCH = 7;

    private List<UIExport> mExports;
    private List<UIStorage> mStorages;
    private UICleanUpExpiration mUICleanUpExpiration;
    private CleanupSwitchViewHolder.Observer mCleanupExpirationSwitchObserver;

    public CleanUpAdapter(CleanUpActivity listActivity, List<UIExport> exports, List<UIStorage> storages, UICleanUpExpiration cleanUpExpiration, OnCleanupClickListener onCleanupClickListener, CleanupSwitchViewHolder.Observer cleanupExpirationSwitchObserver) {

        mCleanupActivity = listActivity;
        mExports = exports;
        mStorages = storages;
        mUICleanUpExpiration = cleanUpExpiration;
        mOnCleanupClickListener = onCleanupClickListener;
        mCleanupExpirationSwitchObserver = cleanupExpirationSwitchObserver;

        if (mUICleanUpExpiration.getExpirationType() == UICleanUpExpiration.ExpirationType.ALL) {
            POSITION_EXPIRATION_CUSTOM = -1;
            POSITION_CLEAN_ACTION = 14;
            ITEM_COUNT = 15;
        } else {
            POSITION_EXPIRATION_CUSTOM = 14;
            POSITION_CLEAN_ACTION = 15;
            ITEM_COUNT = 16;
        }

        setHasStableIds(true);
    }

    public void updateData(List<UIExport> exports, List<UIStorage> storages) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setExports: " + exports);
        }

        mExports = exports;
        mStorages = storages;
        notifyDataSetChanged();
    }

    public void setCleanUpExpiration(UICleanUpExpiration cleanUpExpiration) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setCleanUpExpiration: " + cleanUpExpiration);
        }

        mUICleanUpExpiration = cleanUpExpiration;

        if (mUICleanUpExpiration.getExpirationType() == UICleanUpExpiration.ExpirationType.ALL) {
            POSITION_EXPIRATION_CUSTOM = -1;
            POSITION_CLEAN_ACTION = 14;
            ITEM_COUNT = 15;
        } else {
            POSITION_EXPIRATION_CUSTOM = 14;
            POSITION_CLEAN_ACTION = 15;
            ITEM_COUNT = 16;
        }

        notifyItemRangeChanged(POSITION_EXPIRATION_ALL, 2);
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

        if (position == SECTION_STORAGE || position == SECTION_CONTENT || position == SECTION_EXPIRATION) {
            return TITLE;
        } else if (position == POSITION_CONTENT_INFORMATION || position == POSITION_CONTENT_MESSAGES_INFORMATION || position == POSITION_CONTENT_FILE_INFORMATION) {
            return INFO;
        } else if (position == POSITION_STORAGE_CHART) {
            return STORAGE_CHART;
        } else if (position == POSITION_CLEAN_ACTION) {
            return ACTION;
        } else if (position == POSITION_EXPIRATION_ALL) {
            return SWITCH;
        } else if (position == POSITION_EXPIRATION_CUSTOM) {
            return VALUE;
        } else if (position == POSITION_CONTENT_FILE || position == POSITION_CONTENT_MESSAGES) {
            return CONTENT;
        } else {
            return STORAGE;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        int viewType = getItemViewType(position);

        if (viewType == TITLE) {
            SectionTitleViewHolder sectionTitleViewHolder = (SectionTitleViewHolder) viewHolder;
            if (position == SECTION_STORAGE) {
                sectionTitleViewHolder.onBind(mCleanupActivity.getString(R.string.cleanup_activity_storage_title), true);
            } else if (position == SECTION_CONTENT) {
                sectionTitleViewHolder.onBind(mCleanupActivity.getString(R.string.export_activity_content_title), true);
            } else {
                sectionTitleViewHolder.onBind(mCleanupActivity.getString(R.string.cleanup_activity_expiration), true);
            }
        } else if (viewType == INFO) {
            InformationViewHolder informationViewHolder = (InformationViewHolder) viewHolder;
            if (position == POSITION_CONTENT_INFORMATION) {
                informationViewHolder.onBind(mCleanupActivity.getString(R.string.cleanup_activity_select_content), true);
            } else if (position == POSITION_CONTENT_FILE_INFORMATION) {
                if (mCleanupActivity.isLocalCleanUpOnly()) {
                    informationViewHolder.onBind(mCleanupActivity.getString(R.string.cleanup_activity_medias_and_files_info), true);
                } else {
                    informationViewHolder.onBind(mCleanupActivity.getString(R.string.cleanup_activity_medias_and_files_info_both), true);
                }
            } else {
                informationViewHolder.onBind(mCleanupActivity.getString(R.string.cleanup_activity_messages_info), true);
            }
        } else if (viewType == CONTENT) {
            ExportContentViewHolder exportContentViewHolder = (ExportContentViewHolder) viewHolder;
            UIExport export;
            if (position == POSITION_CONTENT_FILE) {
                export = mExports.get(0);
            } else {
                export = mExports.get(1);
            }
            Drawable drawable = ResourcesCompat.getDrawable(mCleanupActivity.getResources(), export.getImage(), mCleanupActivity.getTheme());
            exportContentViewHolder.itemView.setOnClickListener(view -> mOnCleanupClickListener.onContentClick(export));
            exportContentViewHolder.onBind(export, drawable);
        } else if (viewType == STORAGE) {
            StorageViewHolder storageViewHolder = (StorageViewHolder) viewHolder;
            UIStorage storage = mStorages.get(position - POSITION_STORAGE_CHART - 1);
            storageViewHolder.onBind(storage);
        } else if (viewType == STORAGE_CHART) {
            StorageChartViewHolder storageChartViewHolder = (StorageChartViewHolder) viewHolder;
            storageChartViewHolder.onBind(mStorages);
        } else if (viewType == SWITCH) {
            CleanupSwitchViewHolder cleanupSwitchViewHolder = (CleanupSwitchViewHolder) viewHolder;
            cleanupSwitchViewHolder.onBind(mCleanupActivity.getString(R.string.cleanup_activity_all), mUICleanUpExpiration.getExpirationType() == UICleanUpExpiration.ExpirationType.ALL);
        } else if (viewType == VALUE) {
            ExpirationViewHolder expirationViewHolder = (ExpirationViewHolder) viewHolder;
            expirationViewHolder.itemView.setOnClickListener(view -> mOnCleanupClickListener.onSelectExpiration());
            expirationViewHolder.onBind(mUICleanUpExpiration.getTitle(mCleanupActivity), mUICleanUpExpiration.getValue(mCleanupActivity));
        } else if (viewType == ACTION) {
            CleanUpActionViewHolder cleanUpActionViewHolder = (CleanUpActionViewHolder) viewHolder;
            cleanUpActionViewHolder.onBind(mCleanupActivity.canCleanup());
        }
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mCleanupActivity.getLayoutInflater();
        View convertView;

        if (viewType == TITLE) {
            convertView = inflater.inflate(R.layout.section_title_item, parent, false);
            return new SectionTitleViewHolder(convertView);
        } else if (viewType == INFO) {
            convertView = inflater.inflate(R.layout.settings_room_activity_information_item, parent, false);
            return new InformationViewHolder(convertView);
        } else if (viewType == CONTENT) {
            convertView = inflater.inflate(R.layout.export_activity_content_item, parent, false);
            return new ExportContentViewHolder(convertView);
        } else if (viewType == STORAGE_CHART) {
            convertView = inflater.inflate(R.layout.cleanup_activity_storage_chart_item, parent, false);
            return new StorageChartViewHolder(convertView);
        } else if (viewType == VALUE) {
            convertView = inflater.inflate(R.layout.cleanup_activity_value_item, parent, false);
            return new ExpirationViewHolder(convertView);
        } else if (viewType == SWITCH) {
            convertView = inflater.inflate(R.layout.settings_activity_item_switch, parent, false);
            return new CleanupSwitchViewHolder(convertView, mCleanupExpirationSwitchObserver);
        } else if (viewType == ACTION) {
            convertView = inflater.inflate(R.layout.cleanup_activity_action_item, parent, false);
            return new CleanUpActionViewHolder(convertView, mCleanupActivity);
        } else {
            convertView = inflater.inflate(R.layout.cleanup_activity_storage_item, parent, false);
            return new StorageViewHolder(convertView);
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }

        int position = viewHolder.getBindingAdapterPosition();
        int viewType = getItemViewType(position);

        if (viewType == ACTION && position != -1) {
            CleanUpActionViewHolder cleanUpActionViewHolder = (CleanUpActionViewHolder) viewHolder;
            cleanUpActionViewHolder.onBind(mCleanupActivity.canCleanup());
        }
    }
}
