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

import java.util.ArrayList;
import java.util.List;

public class AccountAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "AccountAdapter";
    private static final boolean DEBUG = false;

    @NonNull
    private final AccountActivity mAccountActivity;

    private final List<UIAccountItem> mItems = new ArrayList<>();

    private static final int TITLE = 0;
    private static final int SUBSECTION = 1;
    private static final int INFO = 2;

    AccountAdapter(@NonNull AccountActivity listActivity) {

        mAccountActivity = listActivity;
        setHasStableIds(false);
        loadItems();
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

        UIAccountItem item = mItems.get(position);
        switch (item.getType()) {
            case SECTION_ACCOUNT:
            case SECTION_BACKUP:
            case SECTION_CONVERSATIONS:
            case SECTION_TRANSFER:
                return TITLE;

            case LAST_BACKUP:
                return INFO;

            default:
                return SUBSECTION;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        int viewType = getItemViewType(position);
        UIAccountItem item = mItems.get(position);
        if (viewType == TITLE) {
            SectionTitleViewHolder sectionTitleViewHolder = (SectionTitleViewHolder) viewHolder;
            if (item.getType() == UIAccountItem.AccountItemType.SECTION_BACKUP) {
                sectionTitleViewHolder.onBind(item.getText(), false, mAccountActivity.getString(R.string.application_new), mAccountActivity::onBetaInfoClick);
            } else {
                sectionTitleViewHolder.onBind(item.getText(), false);
            }
        } else if (viewType == SUBSECTION) {
            SettingIconViewHolder settingIconViewHolder = (SettingIconViewHolder) viewHolder;
            settingIconViewHolder.itemView.setOnClickListener(view -> {
                if (item.getOnClickListener() != null) {
                    item.getOnClickListener().run();
                }
            });
            settingIconViewHolder.onBind(item.getText(), item.getTextColor(), item.getIcon(), item.getIconColor(), false);
        } else if (viewType == INFO) {
            InformationViewHolder informationViewHolder = (InformationViewHolder) viewHolder;
            if (mAccountActivity.getTwinmeApplication().getLastBackupDate() > 0) {
                String lastBackupDate = Utils.formatBackupInterval(mAccountActivity, mAccountActivity.getTwinmeApplication().getLastBackupDate() * 1000L, true);
                String message = String.format(mAccountActivity.getString(R.string.backup_view_last_backup), lastBackupDate);
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
            UIAccountItem item = mItems.get(position);
            if (viewType == TITLE) {
                SectionTitleViewHolder sectionTitleViewHolder = (SectionTitleViewHolder) viewHolder;
                if (item.getType() == UIAccountItem.AccountItemType.SECTION_BACKUP) {
                    sectionTitleViewHolder.onBind(item.getText(), false, mAccountActivity.getString(R.string.application_new), mAccountActivity::onBetaInfoClick);
                } else {
                    sectionTitleViewHolder.onBind(item.getText(), false);
                }
            }
            if (viewType == SUBSECTION) {
                SettingIconViewHolder settingIconViewHolder = (SettingIconViewHolder) viewHolder;
                settingIconViewHolder.itemView.setOnClickListener(view -> {
                    if (item.getOnClickListener() != null) {
                        item.getOnClickListener().run();
                    }
                });
                settingIconViewHolder.onBind(item.getText(), item.getTextColor(), item.getIcon(), item.getIconColor(), false);
            }
        }
    }

    private void loadItems() {
        if (DEBUG) {
            Log.d(LOG_TAG, "loadItems");
        }

        mItems.clear();
        mItems.add(new UIAccountItem(UIAccountItem.AccountItemType.SECTION_TRANSFER, mAccountActivity.getString(R.string.account_view_transfer_between_devices), -1, -1, -1, null));
        mItems.add(new UIAccountItem(UIAccountItem.AccountItemType.TRANSFER_FROM_DEVICE, mAccountActivity.getString(R.string.account_view_transfer_from_device), R.drawable.migration_my_device_icon, Design.FONT_COLOR_DEFAULT, Design.SHOW_ICON_COLOR, () -> mAccountActivity.onTransferClick(true)));
        mItems.add(new UIAccountItem(UIAccountItem.AccountItemType.TRANSFER_FROM_OTHER_DEVICE, mAccountActivity.getString(R.string.account_view_transfer_from_another_device), R.drawable.migration_another_device_icon, Design.FONT_COLOR_DEFAULT, Design.SHOW_ICON_COLOR,() -> mAccountActivity.onTransferClick(false)));
        mItems.add(new UIAccountItem(UIAccountItem.AccountItemType.SECTION_BACKUP, mAccountActivity.getString(R.string.account_view_backup_restore), -1, -1, -1, null));
        mItems.add(new UIAccountItem(UIAccountItem.AccountItemType.BACKUP, mAccountActivity.getString(R.string.account_view_backup), R.drawable.backup_icon, Design.FONT_COLOR_DEFAULT, Design.SHOW_ICON_COLOR, mAccountActivity::onBackupClick));
        mItems.add(new UIAccountItem(UIAccountItem.AccountItemType.RESTORE, mAccountActivity.getString(R.string.account_view_restore), R.drawable.restore_icon, Design.FONT_COLOR_DEFAULT, Design.SHOW_ICON_COLOR, mAccountActivity::onRestoreClick));
        mItems.add(new UIAccountItem(UIAccountItem.AccountItemType.VERIFY_BACKUP, mAccountActivity.getString(R.string.account_view_backup_verify), R.drawable.backup_verify_icon, Design.FONT_COLOR_DEFAULT, Design.SHOW_ICON_COLOR, mAccountActivity::onVerifyBackupClick));
        mItems.add(new UIAccountItem(UIAccountItem.AccountItemType.BACKUPS, mAccountActivity.getString(R.string.account_view_backup_list), R.drawable.backup_list_icon, Design.FONT_COLOR_DEFAULT, Design.SHOW_ICON_COLOR, mAccountActivity::onBackupsClick));
        mItems.add(new UIAccountItem(UIAccountItem.AccountItemType.LAST_BACKUP, "", -1, -1, -1, null));
        mItems.add(new UIAccountItem(UIAccountItem.AccountItemType.SECTION_CONVERSATIONS, mAccountActivity.getString(R.string.account_view_conversations_content_title), -1, -1, -1, null));
        mItems.add(new UIAccountItem(UIAccountItem.AccountItemType.EXPORT, mAccountActivity.getString(R.string.show_contact_view_export_contents), R.drawable.share_icon, Design.FONT_COLOR_DEFAULT, Design.SHOW_ICON_COLOR, mAccountActivity::onExportClick));
        mItems.add(new UIAccountItem(UIAccountItem.AccountItemType.CLEANUP, mAccountActivity.getString(R.string.show_contact_view_cleanup), R.drawable.cleanup_icon, Design.FONT_COLOR_DEFAULT, Design.SHOW_ICON_COLOR, mAccountActivity::onCleanupClick));
        mItems.add(new UIAccountItem(UIAccountItem.AccountItemType.SECTION_ACCOUNT, "", -1, -1, -1, null));
        mItems.add(new UIAccountItem(UIAccountItem.AccountItemType.DELETE, mAccountActivity.getString(R.string.deleted_account_view_delete), R.drawable.delete_icon, Design.DELETE_COLOR_RED, Design.DELETE_COLOR_RED, mAccountActivity::onDeleteAccountClick));
    }
}