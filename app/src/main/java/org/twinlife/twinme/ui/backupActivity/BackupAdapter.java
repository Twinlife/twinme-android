/*
 *  Copyright (c) 2024-2025 twinlife SA.
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
import org.twinlife.twinme.ui.rooms.InformationViewHolder;
import org.twinlife.twinme.utils.SectionTitleViewHolder;

import java.util.ArrayList;
import java.util.List;

public class BackupAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "BackupAdapter";
    private static final boolean DEBUG = false;

    private final BackupActivity mBackupActivity;

    private static final int ITEM_COUNT = 6;

    private static final int SECTION_SECURITY = 0;
    private static final int POSITION_SECURITY_INFO = 1;
    private static final int POSITION_WORDS = 2;
    private static final int POSITION_ACTION = 3;
    private static final int POSITION_CONFIRM = 4;
    private static final int POSITION_WAIT = -1;

    private static final int INFO = 0;
    private static final int SECTION = 1;
    private static final int WORDS = 2;
    private static final int ACTION = 3;
    private static final int CONFIRM = 4;
    private static final int WAIT = 5;
    private static final int FOOTER = 6;

    private List<UIBackupWord> mBackupWords = new ArrayList<>();
    private final OnCreateBackupClickListener mOnCreateBackupClickListener;

    public interface OnCreateBackupClickListener {

        void onCreateBackupClick();
    }

    public BackupAdapter(BackupActivity backupActivity, OnCreateBackupClickListener onCreateBackupClickListener) {

        mBackupActivity = backupActivity;
        mOnCreateBackupClickListener = onCreateBackupClickListener;
        setHasStableIds(true);
    }

    public void updateWords(List<UIBackupWord> backupWords) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateWords");
        }

        mBackupWords = backupWords;
        notifyItemChanged(POSITION_WORDS);
    }

    public void updateConfirmBackup() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateConfirmBackup");
        }

        notifyItemChanged(POSITION_CONFIRM);
        notifyItemChanged(ITEM_COUNT - 1);
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

        if (position == SECTION_SECURITY) {
            return SECTION;
        } else if (position == POSITION_SECURITY_INFO) {
            return INFO;
        } else if (position == POSITION_WORDS) {
            return WORDS;
        } else if (position == POSITION_ACTION) {
            return ACTION;
        } else if (position == POSITION_CONFIRM) {
            return CONFIRM;
        } else if (position == POSITION_WAIT) {
            return WAIT;
        } else {
            return FOOTER;
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
            informationViewHolder.onBind(mBackupActivity.getString(R.string.backup_activity_security_info), true);
        } else if (viewType == SECTION) {
            SectionTitleViewHolder sectionTitleViewHolder = (SectionTitleViewHolder) viewHolder;
            sectionTitleViewHolder.onBind(mBackupActivity.getString(R.string.backup_activity_security), true);
        } else if (viewType == WORDS) {
            BackupWordsViewHolder backupWordsViewHolder = (BackupWordsViewHolder) viewHolder;
            backupWordsViewHolder.onBind(mBackupWords);
        } else if (viewType == ACTION) {
            BackupActionViewHolder backupActionViewHolder = (BackupActionViewHolder) viewHolder;

            String copyActionTitle = mBackupActivity.getString(R.string.conversation_activity_menu_item_view_copy_title);
            int copyActionIcon = R.drawable.copy_item;
            Runnable copyRunnable = mBackupActivity::onCopyWordsClick;

            String generateActionTitle = mBackupActivity.getString(R.string.backup_activity_generate_word);
            int generateActionIcon = R.drawable.generate_icon;
            Runnable generateRunnable = mBackupActivity::onGenerateWordsClick;

            backupActionViewHolder.onBind(copyActionIcon, generateActionIcon, copyActionTitle, generateActionTitle, copyRunnable, generateRunnable);
        } else if (viewType == CONFIRM) {
            BackupConfirmViewHolder backupConfirmViewHolder = (BackupConfirmViewHolder) viewHolder;
            backupConfirmViewHolder.onBind(mBackupActivity.isConfirmBackup());
        } else if (viewType == FOOTER) {
            BackupFooterViewHolder backupFooterViewHolder = (BackupFooterViewHolder) viewHolder;
            backupFooterViewHolder.onBind(mBackupActivity.getString(R.string.backup_activity_backup), mBackupActivity.isConfirmBackup());
        }
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mBackupActivity.getLayoutInflater();
        View convertView;

        if (viewType == SECTION) {
            convertView = inflater.inflate(R.layout.section_title_item, parent, false);
            return new SectionTitleViewHolder(convertView);
        } else if (viewType == INFO) {
            convertView = inflater.inflate(R.layout.settings_room_activity_information_item, parent, false);
            return new InformationViewHolder(convertView);
        } else if (viewType == WORDS) {
            convertView = inflater.inflate(R.layout.create_backup_activity_words_item, parent, false);
            return new BackupWordsViewHolder(mBackupActivity, convertView);
        } else if (viewType == ACTION) {
            convertView = inflater.inflate(R.layout.create_backup_activity_action_item, parent, false);
            return new BackupActionViewHolder(convertView);
        } else if (viewType == CONFIRM) {
            convertView = inflater.inflate(R.layout.create_backup_activity_confirm_item, parent, false);
            convertView.setOnClickListener(view -> mBackupActivity.onConfirmBackupClick());
            return new BackupConfirmViewHolder(convertView);
        } else if (viewType == WAIT) {
            convertView = inflater.inflate(R.layout.create_backup_activity_waiting_item, parent, false);
            return new BackupWaitingViewHolder(convertView);
        } else {
            convertView = inflater.inflate(R.layout.create_backup_activity_footer_item, parent, false);
            return new BackupFooterViewHolder(convertView, mOnCreateBackupClickListener::onCreateBackupClick);
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }

    }
}