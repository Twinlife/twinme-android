/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.backupActivity;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.services.BackupService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.rooms.InformationViewHolder;
import org.twinlife.twinme.utils.BackupStats;
import org.twinlife.twinme.utils.SectionTitleViewHolder;

import java.util.ArrayList;
import java.util.List;

public class BackupContentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "ConfirmBackup...";
    private static final boolean DEBUG = false;

    private final BackupContentConfirmView mBackupContentConfirmView;
    private final List<UIRestoreItem> mUIRestoreItems = new ArrayList<>();

    public BackupContentAdapter(BackupContentConfirmView backupContentConfirmView) {

        mBackupContentConfirmView = backupContentConfirmView;
        setHasStableIds(true);
    }

    public void updateWithBackupStats(BackupStats backupStats) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateBackupStats : " + backupStats);
        }

        mUIRestoreItems.clear();
        mUIRestoreItems.add(new UIRestoreItem(UIRestoreItem.UIRestoreItemType.CONTENT, mBackupContentConfirmView.getContext().getString(R.string.contacts_fragment_title), R.drawable.contacts_icon, backupStats.contacts, Design.BLACK_COLOR));
        notifyItemRangeChanged(0, mUIRestoreItems.size());
    }

    public void updateWithRestoreReport(BackupService.RestoreReport restoreReport) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateWithRestoreReport : " + restoreReport);
        }

        mUIRestoreItems.clear();

        if (!restoreReport.isRestoreUpToDate()) {

            if (!restoreReport.profiles.isStatsUpToDate()) {
                mUIRestoreItems.add(new UIRestoreItem(UIRestoreItem.UIRestoreItemType.SECTION, mBackupContentConfirmView.getContext().getString(R.string.application_profile), -1, -1, -1));
                mUIRestoreItems.add(new UIRestoreItem(UIRestoreItem.UIRestoreItemType.CONTENT, mBackupContentConfirmView.getContext().getString(R.string.restore_activity_content_profile_reset), R.drawable.generate_code, -1, Design.BLACK_COLOR));
                mUIRestoreItems.add(new UIRestoreItem(UIRestoreItem.UIRestoreItemType.INFO, mBackupContentConfirmView.getContext().getString(R.string.restore_activity_content_profile_reset_message), -1, -1, -1));
            }

            if (!restoreReport.contacts.isStatsUpToDate()) {
                mUIRestoreItems.add(new UIRestoreItem(UIRestoreItem.UIRestoreItemType.SECTION, mBackupContentConfirmView.getContext().getString(R.string.contacts_fragment_title), -1, -1, -1));

                if (restoreReport.contacts.added != 0) {
                    mUIRestoreItems.add(new UIRestoreItem(UIRestoreItem.UIRestoreItemType.CONTENT, mBackupContentConfirmView.getContext().getString(R.string.backup_activity_content_diff_added), R.drawable.contacts_icon, restoreReport.contacts.added, Design.BLACK_COLOR));
                }

                if (restoreReport.contacts.modified != 0) {
                    mUIRestoreItems.add(new UIRestoreItem(UIRestoreItem.UIRestoreItemType.CONTENT, mBackupContentConfirmView.getContext().getString(R.string.backup_activity_content_diff_updated), R.drawable.action_edit, restoreReport.contacts.modified, Design.BLACK_COLOR));
                }

                if (restoreReport.contacts.deleted != 0) {
                    mUIRestoreItems.add(new UIRestoreItem(UIRestoreItem.UIRestoreItemType.CONTENT, mBackupContentConfirmView.getContext().getString(R.string.backup_activity_content_diff_deleted), R.drawable.delete_item, restoreReport.contacts.deleted, -1));
                    mUIRestoreItems.add(new UIRestoreItem(UIRestoreItem.UIRestoreItemType.INFO, mBackupContentConfirmView.getContext().getString(R.string.restore_activity_content_contact_deleted_message), -1, -1, -1));
                }
            }
        } else {
            mUIRestoreItems.add(new UIRestoreItem(UIRestoreItem.UIRestoreItemType.CONTENT, mBackupContentConfirmView.getContext().getString(R.string.contacts_fragment_title), R.drawable.contacts_icon, restoreReport.contacts.upToDate, Design.BLACK_COLOR));
        }

        notifyItemRangeChanged(0, mUIRestoreItems.size());
    }

    @Override
    public int getItemCount() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemCount");
        }

        return mUIRestoreItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemViewType: " + position);
        }

        return mUIRestoreItems.get(position).getType().getValue();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        UIRestoreItem restoreItem = mUIRestoreItems.get(position);
        switch (restoreItem.getType()) {
            case SECTION: {
                SectionTitleViewHolder sectionTitleViewHolder = (SectionTitleViewHolder) viewHolder;
                sectionTitleViewHolder.onBind(restoreItem.getText(), Design.POPUP_BACKGROUND_COLOR, false);
                break;
            }

            case CONTENT: {
                BackupContentViewHolder backupContentViewHolder = (BackupContentViewHolder) viewHolder;
                backupContentViewHolder.onBind(restoreItem, Design.POPUP_BACKGROUND_COLOR, false);
                break;
            }

            case INFO: {
                InformationViewHolder informationViewHolder = (InformationViewHolder) viewHolder;
                informationViewHolder.onBind(restoreItem.getText(), false, Design.POPUP_BACKGROUND_COLOR);
                break;
            }

            default:
                break;
        }
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = (LayoutInflater) mBackupContentConfirmView.getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View convertView;

        UIRestoreItem.UIRestoreItemType type = UIRestoreItem.UIRestoreItemType.fromType(viewType);

        switch (type) {
            case SECTION:
                convertView = inflater.inflate(R.layout.section_title_item, parent, false);
                return new SectionTitleViewHolder(convertView);

            case INFO:
                convertView = inflater.inflate(R.layout.settings_room_activity_information_item, parent, false);
                return new InformationViewHolder(convertView);

            default:
                convertView = inflater.inflate(R.layout.create_backup_activity_content_item, parent, false);
                return new BackupContentViewHolder(convertView);
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }

    }
}