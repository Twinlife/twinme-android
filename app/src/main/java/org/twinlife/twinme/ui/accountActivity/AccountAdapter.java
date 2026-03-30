/*
 *  Copyright (c) 2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.accountActivity;

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
import org.twinlife.twinme.utils.Utils;

public class AccountAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "AccountAdapter";
    private static final boolean DEBUG = false;

    @NonNull
    private final AccountActivity mAccountActivity;

    private static final int ITEM_COUNT = 14;

    private static final int SECTION_TRANSFER = 0;
    private static final int SECTION_BACKUP = 3;
    private static final int SECTION_CONVERSATIONS = 9;
    private static final int SECTION_DELETE_ACCOUNT = 12;

    private static final int POSITION_TRANSFER_FROM_CURRENT_DEVICE = 1;
    private static final int POSITION_TRANSFER_FROM_ANOTHER_DEVICE = 2;
    private static final int POSITION_BACKUP = 4;
    private static final int POSITION_RESTORE = 5;
    private static final int POSITION_VERIFY_BACKUP = 6;
    private static final int POSITION_BACKUS = 7;
    private static final int POSITION_LAST_BACKUP = 8;
    private static final int POSITION_EXPORT_CONVERSATIONS = 10;
    private static final int POSITION_CLEANUP = 11;

    private static final int TITLE = 0;
    private static final int SUBSECTION = 1;
    private static final int INFO = 2;

    AccountAdapter(@NonNull AccountActivity listActivity) {

        mAccountActivity = listActivity;
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

        if (position == SECTION_TRANSFER || position == SECTION_BACKUP || position == SECTION_CONVERSATIONS  || position == SECTION_DELETE_ACCOUNT) {
            return TITLE;
        } else if (position == POSITION_LAST_BACKUP) {
            return INFO;
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

        if (viewType == TITLE) {
            SectionTitleViewHolder sectionTitleViewHolder = (SectionTitleViewHolder) viewHolder;
            String title = getSectionTitle(position);

            if (position == SECTION_BACKUP) {
                sectionTitleViewHolder.onBind(title, false, mAccountActivity.getString(R.string.application_beta), mAccountActivity::onBetaInfoClick);
            } else {
                sectionTitleViewHolder.onBind(title, false);
            }

        } else if (viewType == SUBSECTION) {
            SettingIconViewHolder settingIconViewHolder = (SettingIconViewHolder) viewHolder;
            String title;
            int textColor = Design.FONT_COLOR_DEFAULT;
            int iconId;
            int iconColor = Design.SHOW_ICON_COLOR;
            if (position == POSITION_TRANSFER_FROM_CURRENT_DEVICE) {
                title = mAccountActivity.getString(R.string.account_activity_transfer_from_device);
                iconId = R.drawable.migration_my_device_icon;
                settingIconViewHolder.itemView.setOnClickListener(view -> mAccountActivity.onTransferClick(true));
            } else if (position == POSITION_TRANSFER_FROM_ANOTHER_DEVICE) {
                title = mAccountActivity.getString(R.string.account_activity_transfer_from_another_device);
                iconId = R.drawable.migration_another_device_icon;
                settingIconViewHolder.itemView.setOnClickListener(view -> mAccountActivity.onTransferClick(false));
            } else if (position == POSITION_BACKUP) {
                title = mAccountActivity.getString(R.string.account_activity_backup);
                iconId = R.drawable.backup_icon;
                settingIconViewHolder.itemView.setOnClickListener(view -> mAccountActivity.onBackupClick());
            } else if (position == POSITION_RESTORE) {
                title = mAccountActivity.getString(R.string.account_activity_restore);
                iconId = R.drawable.restore_icon;
                settingIconViewHolder.itemView.setOnClickListener(view -> mAccountActivity.onRestoreClick());
            } else if (position == POSITION_VERIFY_BACKUP) {
                title = mAccountActivity.getString(R.string.account_activity_backup_verify);
                iconId = R.drawable.backup_verify_icon;
                settingIconViewHolder.itemView.setOnClickListener(view -> mAccountActivity.onVerifyBackupClick());
            } else if (position == POSITION_BACKUS) {
                title = mAccountActivity.getString(R.string.account_activity_backup_list);
                iconId = R.drawable.backup_list_icon;
                settingIconViewHolder.itemView.setOnClickListener(view -> mAccountActivity.onBackupsClick());
            } else if (position == POSITION_EXPORT_CONVERSATIONS) {
                title = mAccountActivity.getString(R.string.show_contact_activity_export_contents);
                iconId = R.drawable.share_icon;
                settingIconViewHolder.itemView.setOnClickListener(view -> mAccountActivity.onExportClick());
            } else if (position == POSITION_CLEANUP) {
                title = mAccountActivity.getString(R.string.show_contact_activity_cleanup);
                iconId = R.drawable.cleanup_icon;
                settingIconViewHolder.itemView.setOnClickListener(view -> mAccountActivity.onCleanupClick());
            } else {
                title = mAccountActivity.getString(R.string.deleted_account_activity_delete);
                textColor = Design.DELETE_COLOR_RED;
                iconId = R.drawable.delete_icon;
                iconColor = Design.DELETE_COLOR_RED;
                settingIconViewHolder.itemView.setOnClickListener(view -> mAccountActivity.onDeleteAccountClick());
            }
            settingIconViewHolder.onBind(title, textColor, iconId, iconColor, false);
        } else if (viewType == INFO) {
            InformationViewHolder informationViewHolder = (InformationViewHolder) viewHolder;
            if (mAccountActivity.getTwinmeApplication().getLastBackupDate() > 0) {
                String lastBackupDate = Utils.formatBackupInterval(mAccountActivity, mAccountActivity.getTwinmeApplication().getLastBackupDate() * 1000L, true);
                String message = String.format(mAccountActivity.getString(R.string.backup_activity_last_backup), lastBackupDate);
                informationViewHolder.onBind(message, false);
            } else {
                informationViewHolder.onBind("", false);
            }
        }
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mAccountActivity.getLayoutInflater();
        View convertView;

        if (viewType == TITLE) {
            convertView = inflater.inflate(R.layout.section_title_item, parent, false);
            return new SectionTitleViewHolder(convertView);
        } else if (viewType == INFO) {
            convertView = inflater.inflate(R.layout.settings_room_activity_information_item, parent, false);
            return new InformationViewHolder(convertView);
        } else  {
            convertView = inflater.inflate(R.layout.setting_icon_item, parent, false);
            return new SettingIconViewHolder(convertView);
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }

        int position = viewHolder.getBindingAdapterPosition();
        int viewType = getItemViewType(position);
        if (position != -1) {
            if (viewType == TITLE) {
                SectionTitleViewHolder sectionTitleViewHolder = (SectionTitleViewHolder) viewHolder;
                String title = getSectionTitle(position);
                sectionTitleViewHolder.onBind(title, false);
            }
            if (viewType == SUBSECTION) {
                SettingIconViewHolder settingIconViewHolder = (SettingIconViewHolder) viewHolder;
                String title;
                int textColor = Design.FONT_COLOR_DEFAULT;
                int iconId;
                int iconColor = Design.SHOW_ICON_COLOR;
                if (position == POSITION_TRANSFER_FROM_CURRENT_DEVICE) {
                    title = mAccountActivity.getString(R.string.account_activity_transfer_from_device);
                    iconId = R.drawable.migration_my_device_icon;
                    settingIconViewHolder.itemView.setOnClickListener(view -> mAccountActivity.onTransferClick(true));
                } else if (position == POSITION_TRANSFER_FROM_ANOTHER_DEVICE) {
                    title = mAccountActivity.getString(R.string.account_activity_transfer_from_another_device);
                    iconId = R.drawable.migration_another_device_icon;
                    settingIconViewHolder.itemView.setOnClickListener(view -> mAccountActivity.onTransferClick(false));
                } else if (position == POSITION_BACKUP) {
                    title = mAccountActivity.getString(R.string.account_activity_backup);
                    iconId = R.drawable.backup_icon;
                    settingIconViewHolder.itemView.setOnClickListener(view -> mAccountActivity.onBackupClick());
                } else if (position == POSITION_RESTORE) {
                    title = mAccountActivity.getString(R.string.account_activity_restore);
                    iconId = R.drawable.restore_icon;
                    settingIconViewHolder.itemView.setOnClickListener(view -> mAccountActivity.onRestoreClick());
                } else if (position == POSITION_EXPORT_CONVERSATIONS) {
                    title = mAccountActivity.getString(R.string.show_contact_activity_export_contents);
                    iconId = R.drawable.share_icon;
                    settingIconViewHolder.itemView.setOnClickListener(view -> mAccountActivity.onExportClick());
                } else if (position == POSITION_CLEANUP) {
                    title = mAccountActivity.getString(R.string.show_contact_activity_cleanup);
                    iconId = R.drawable.cleanup_icon;
                    settingIconViewHolder.itemView.setOnClickListener(view -> mAccountActivity.onCleanupClick());
                } else {
                    title = mAccountActivity.getString(R.string.deleted_account_activity_delete);
                    textColor = Design.DELETE_COLOR_RED;
                    iconId = R.drawable.delete_icon;
                    iconColor = Design.DELETE_COLOR_RED;
                    settingIconViewHolder.itemView.setOnClickListener(view -> mAccountActivity.onDeleteAccountClick());
                }
                settingIconViewHolder.onBind(title, textColor, iconId, iconColor, false);
            }
        }
    }

    private String getSectionTitle(int position) {

        String title = "";

        if (position == SECTION_TRANSFER) {
            title = mAccountActivity.getString(R.string.account_activity_transfer_between_devices);
        } else if (position == SECTION_BACKUP) {
            title = mAccountActivity.getString(R.string.account_activity_backup_restore);
        } else if (position == SECTION_CONVERSATIONS) {
            title = mAccountActivity.getString(R.string.account_activity_conversations_content_title);
        }

        return title;
    }
}