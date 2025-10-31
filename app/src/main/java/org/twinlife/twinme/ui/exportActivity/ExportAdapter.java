/*
 *  Copyright (c) 2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui.exportActivity;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.ui.Settings;
import org.twinlife.twinme.ui.rooms.InformationViewHolder;
import org.twinlife.twinme.ui.settingsActivity.UISetting;
import org.twinlife.twinme.utils.SectionTitleViewHolder;

import java.util.List;

public class ExportAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "ExportAdapter";
    private static final boolean DEBUG = false;

    public interface OnExportClickListener {

        void onExportContentClick(UIExport export);
    }

    private final ExportActivity mExportActivity;

    private final OnExportClickListener mOnExportClickListener;

    private static final int ITEM_COUNT;

    private static final int SECTION_CONTENT = 0;
    private static final int SECTION_LOCATION;
    private static final int SECTION_EXPORT;

    private static final int POSITION_CONTENT_INFORMATION = 1;
    private static final int POSITION_CONTENT_INFORMATION_FOOTER = 7;
    private static final int POSITION_LOCATION_INFORMATION;
    private static final int POSITION_LOCATION;
    private static final int POSITION_PROGRESS;

    private static final int TITLE = 0;
    private static final int INFO = 1;
    private static final int EXPORT_CONTENT = 2;
    private static final int EXPORT_ACTION = 3;
    private static final int EXPORT_PROGRESS = 4;
    private static final int EXPORT_LOCATION = 5;

    static {
        ITEM_COUNT = 9;
        SECTION_EXPORT = 8;
        SECTION_LOCATION = 11;
        POSITION_LOCATION_INFORMATION = 11;
        POSITION_LOCATION = 11;
        POSITION_PROGRESS = 8;
    }

    private List<UIExport> mExports;

    private boolean mIsExportInProgress = false;
    @Nullable
    private String mExportDirectory;
    private int mProgress = 0;

    ExportAdapter(ExportActivity listActivity, List<UIExport> exports, OnExportClickListener onExportClickListener) {

        mExportActivity = listActivity;
        mExports = exports;
        mOnExportClickListener = onExportClickListener;
        mExportDirectory = null;
        setHasStableIds(true);
    }

    public void setExportDirectory(@Nullable String exportDirectory) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setExportDirectory: " + exportDirectory);
        }

        mExportDirectory = exportDirectory;
        notifyItemChanged(POSITION_LOCATION);
    }

    @SuppressLint("NotifyDataSetChanged")
    void setExportProgress(int progress) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setExportProgress");
        }

        if (!mIsExportInProgress) {
            mIsExportInProgress = true;
            notifyDataSetChanged();
        }

        mProgress = progress;
        notifyItemChanged(POSITION_PROGRESS);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setExports(List<UIExport> exports) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setExports: " + exports);
        }

        mExports = exports;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemCount");
        }

        if (mIsExportInProgress) {
            return ITEM_COUNT + 1;
        }

        return ITEM_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemViewType: " + position);
        }

        if (position == SECTION_CONTENT || position == SECTION_LOCATION) {
            return TITLE;
        } else if (position == POSITION_CONTENT_INFORMATION || position == POSITION_CONTENT_INFORMATION_FOOTER || position == POSITION_LOCATION_INFORMATION) {
            return INFO;
        } else if (position == SECTION_EXPORT && mIsExportInProgress) {
            return EXPORT_PROGRESS;
        } else if (position == SECTION_EXPORT || position == SECTION_EXPORT + 1) {
            return EXPORT_ACTION;
        } else if (position == POSITION_LOCATION) {
            return EXPORT_LOCATION;
        } else {
            return EXPORT_CONTENT;
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
            if (position == SECTION_CONTENT) {
                sectionTitleViewHolder.onBind(mExportActivity.getString(R.string.export_activity_content_title), true);
            } else {
                sectionTitleViewHolder.onBind(mExportActivity.getString(R.string.export_activity_save_location), true);
            }
        } else if (viewType == INFO) {
            InformationViewHolder informationViewHolder = (InformationViewHolder) viewHolder;
            if (position == POSITION_CONTENT_INFORMATION) {
                informationViewHolder.onBind(mExportActivity.getString(R.string.export_activity_select_content), true);
            } else if (position == POSITION_CONTENT_INFORMATION_FOOTER) {
                informationViewHolder.onBind(mExportActivity.getExportInformation(), false);
            } else {
                informationViewHolder.onBind(mExportActivity.getString(R.string.export_activity_save_location_message), true);
            }
        } else if (viewType == EXPORT_CONTENT) {
            ExportContentViewHolder exportContentViewHolder = (ExportContentViewHolder) viewHolder;
            UIExport export = mExports.get(position - 2);
            Drawable drawable = ResourcesCompat.getDrawable(mExportActivity.getResources(), export.getImage(), mExportActivity.getTheme());
            exportContentViewHolder.itemView.setOnClickListener(view -> mOnExportClickListener.onExportContentClick(export));
            exportContentViewHolder.onBind(export, drawable);
        } else if (viewType == EXPORT_ACTION) {
            ExportActionViewHolder exportActionViewHolder = (ExportActionViewHolder) viewHolder;
            exportActionViewHolder.onBind(mExportActivity.canExport(), mExportActivity.isExportInProgress());
        } else if (viewType == EXPORT_PROGRESS) {
            ExportProgressViewHolder exportProgressViewHolder = (ExportProgressViewHolder) viewHolder;
            exportProgressViewHolder.onBind(mProgress, mExportActivity.getString(R.string.export_activity_do_not_leave_screen));
        } else if (viewType == EXPORT_LOCATION) {
            if (mExportDirectory == null) {
                mExportDirectory = mExportActivity.getString(R.string.settings_activity_default_directory_title);
            }
            ExportDirectoryViewHolder exportDirectoryViewHolder = (ExportDirectoryViewHolder) viewHolder;
            UISetting<String> uiSetting = new UISetting<>(UISetting.TypeSetting.DIRECTORY, mExportDirectory, Settings.defaultDirectoryToExport);
            // exportDirectoryViewHolder.itemView.setOnClickListener(view -> mOnExportClickListener.onSelectLocationClick());
            exportDirectoryViewHolder.onBind(uiSetting);
        }
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mExportActivity.getLayoutInflater();
        View convertView;

        if (viewType == TITLE) {
            convertView = inflater.inflate(R.layout.section_title_item, parent, false);
            return new SectionTitleViewHolder(convertView);
        } else if (viewType == INFO) {
            convertView = inflater.inflate(R.layout.settings_room_activity_information_item, parent, false);
            return new InformationViewHolder(convertView);
        } else if (viewType == EXPORT_CONTENT) {
            convertView = inflater.inflate(R.layout.export_activity_content_item, parent, false);
            return new ExportContentViewHolder(convertView);
        } else if (viewType == EXPORT_ACTION) {
            convertView = inflater.inflate(R.layout.export_activity_action_item, parent, false);
            return new ExportActionViewHolder(convertView, mExportActivity);
        } else if (viewType == EXPORT_LOCATION) {
            convertView = inflater.inflate(R.layout.export_activity_directory_item, parent, false);
            return new ExportDirectoryViewHolder(convertView, mExportActivity);
        } else {
            convertView = inflater.inflate(R.layout.export_activity_progress_item, parent, false);
            return new ExportProgressViewHolder(convertView);
        }
    }
}
