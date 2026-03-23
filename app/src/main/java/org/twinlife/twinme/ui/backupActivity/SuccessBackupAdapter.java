/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.backupActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.rooms.InformationViewHolder;
import org.twinlife.twinme.utils.SectionTitleViewHolder;

import java.util.ArrayList;
import java.util.List;

public class SuccessBackupAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "SuccessBackupAdapter";
    private static final boolean DEBUG = false;

    private final SuccessBackupActivity mSuccessBackupActivity;

    private static final int ITEM_COUNT = 6;

    private static final int POSITION_BACKUP_INFO = 0;
    private static final int POSITION_BACKUP_ACTION = 1;
    private static final int SECTION_SECURITY = 2;
    private static final int POSITION_SECURITY_INFO = 3;
    private static final int POSITION_COPY_WORDS = 5;

    private static final int BACKUP = 0;
    private static final int ACTION = 1;
    private static final int INFO = 2;
    private static final int SECTION = 3;
    private static final int WORDS = 4;

    private List<UIBackupWord> mBackupWords = new ArrayList<>();

    public SuccessBackupAdapter(SuccessBackupActivity successBackupActivity, List<UIBackupWord> backupWords) {

        mSuccessBackupActivity = successBackupActivity;
        mBackupWords = backupWords;
        setHasStableIds(true);
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

        if (position == POSITION_BACKUP_INFO) {
            return BACKUP;
        } else if (position == POSITION_BACKUP_ACTION || position == POSITION_COPY_WORDS) {
            return ACTION;
        } else if (position == SECTION_SECURITY) {
            return SECTION;
        } else if (position == POSITION_SECURITY_INFO) {
            return INFO;
        } else {
            return WORDS;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        int viewType = getItemViewType(position);

        if (viewType == BACKUP) {
            BackupInfoViewHolder backupInfoViewHolder = (BackupInfoViewHolder) viewHolder;
            backupInfoViewHolder.onBind(mSuccessBackupActivity.getBackupFileName(), Design.WHITE_COLOR);
        } else if (viewType == ACTION) {
            BackupActionViewHolder backupActionViewHolder = (BackupActionViewHolder) viewHolder;

            if (position == POSITION_BACKUP_ACTION) {
                String saveActionTitle = mSuccessBackupActivity.getString(R.string.application_save);
                int saveActionIcon = R.drawable.save_item;
                Runnable saveRunnable = mSuccessBackupActivity::onSaveFileClick;

                String shareActionTitle = mSuccessBackupActivity.getString(R.string.share_activity_title);
                int shareActionIcon = R.drawable.share_item;
                Runnable shareRunnable = mSuccessBackupActivity::onShareClick;

                backupActionViewHolder.onBind(saveActionIcon, shareActionIcon, saveActionTitle, shareActionTitle, saveRunnable, shareRunnable);
            } else {
                String copyActionTitle = mSuccessBackupActivity.getString(R.string.conversation_activity_menu_item_view_copy_title);
                int copyActionIcon = R.drawable.copy_item;
                Runnable copyRunnable = mSuccessBackupActivity::onCopyClick;
                backupActionViewHolder.onBind(copyActionIcon, -1, copyActionTitle, null, copyRunnable, null);
            }
        } else if (viewType == INFO) {
            InformationViewHolder informationViewHolder = (InformationViewHolder) viewHolder;
            informationViewHolder.onBind(mSuccessBackupActivity.getString(R.string.backup_activity_security_info), true);
        } else if (viewType == SECTION) {
            SectionTitleViewHolder sectionTitleViewHolder = (SectionTitleViewHolder) viewHolder;
            sectionTitleViewHolder.onBind(mSuccessBackupActivity.getString(R.string.backup_activity_security), true);
        } else if (viewType == WORDS) {
            BackupWordsViewHolder backupWordsViewHolder = (BackupWordsViewHolder) viewHolder;
            backupWordsViewHolder.onBind(mBackupWords);
        }
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mSuccessBackupActivity.getLayoutInflater();
        View convertView;

        if (viewType == SECTION) {
            convertView = inflater.inflate(R.layout.section_title_item, parent, false);
            return new SectionTitleViewHolder(convertView);
        } else if (viewType == INFO) {
            convertView = inflater.inflate(R.layout.settings_room_activity_information_item, parent, false);
            return new InformationViewHolder(convertView);
        } else if (viewType == WORDS) {
            convertView = inflater.inflate(R.layout.create_backup_activity_words_item, parent, false);
            return new BackupWordsViewHolder(mSuccessBackupActivity, convertView);
        } else if (viewType == ACTION) {
            convertView = inflater.inflate(R.layout.create_backup_activity_action_item, parent, false);
            return new BackupActionViewHolder(convertView);
        } else{
            convertView = inflater.inflate(R.layout.restore_activity_backup_info_item, parent, false);
            return new BackupInfoViewHolder(convertView);
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }

    }
}