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
import org.twinlife.twinme.utils.SectionTitleViewHolder;

public class AccountAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "AccountAdapter";
    private static final boolean DEBUG = false;

    @NonNull
    private final AccountActivity mListActivity;

    private static final int ITEM_COUNT = 8;

    private static final int SECTION_TRANSFER = 0;
    private static final int SECTION_CONVERSATIONS = 3;
    private static final int SECTION_DELETE_ACCOUNT = 6;

    private static final int POSITION_TRANSFER_FROM_CURRENT_DEVICE = 1;
    private static final int POSITION_TRANSFER_FROM_ANOTHER_DEVICE = 2;
    private static final int POSITION_EXPORT_CONVERSATIONS = 4;
    private static final int POSITION_CLEANUP = 5;

    private static final int TITLE = 0;
    private static final int SUBSECTION = 1;

    AccountAdapter(@NonNull AccountActivity listActivity) {

        mListActivity = listActivity;
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

        if (position == SECTION_TRANSFER || position == SECTION_CONVERSATIONS || position == SECTION_DELETE_ACCOUNT) {
            return TITLE;
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
            sectionTitleViewHolder.onBind(title, false);
        } else if (viewType == SUBSECTION) {
            SettingIconViewHolder settingIconViewHolder = (SettingIconViewHolder) viewHolder;
            String title;
            int textColor = Design.FONT_COLOR_DEFAULT;
            int iconId;
            int iconColor = Design.SHOW_ICON_COLOR;
            if (position == POSITION_TRANSFER_FROM_CURRENT_DEVICE) {
                title = mListActivity.getString(R.string.account_activity_transfer_from_device);
                iconId = R.drawable.migration_my_device_icon;
                settingIconViewHolder.itemView.setOnClickListener(view -> mListActivity.onTransferClick(true));
            } else if (position == POSITION_TRANSFER_FROM_ANOTHER_DEVICE) {
                title = mListActivity.getString(R.string.account_activity_transfer_from_another_device);
                iconId = R.drawable.migration_another_device_icon;
                settingIconViewHolder.itemView.setOnClickListener(view -> mListActivity.onTransferClick(false));
            } else if (position == POSITION_EXPORT_CONVERSATIONS) {
                title = mListActivity.getString(R.string.show_contact_activity_export_contents);
                iconId = R.drawable.share_icon;
                settingIconViewHolder.itemView.setOnClickListener(view -> mListActivity.onExportClick());
            } else if (position == POSITION_CLEANUP) {
                title = mListActivity.getString(R.string.show_contact_activity_cleanup);
                iconId = R.drawable.cleanup_icon;
                settingIconViewHolder.itemView.setOnClickListener(view -> mListActivity.onCleanupClick());
            } else {
                title = mListActivity.getString(R.string.deleted_account_activity_delete);
                textColor = Design.DELETE_COLOR_RED;
                iconId = R.drawable.delete_icon;
                iconColor = Design.DELETE_COLOR_RED;
                settingIconViewHolder.itemView.setOnClickListener(view -> mListActivity.onDeleteAccountClick());
            }
            settingIconViewHolder.onBind(title, textColor, iconId, iconColor, false);
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

        if (viewType == TITLE) {
            convertView = inflater.inflate(R.layout.section_title_item, parent, false);
            return new SectionTitleViewHolder(convertView);
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
                    title = mListActivity.getString(R.string.account_activity_transfer_from_device);
                    iconId = R.drawable.migration_my_device_icon;
                    settingIconViewHolder.itemView.setOnClickListener(view -> mListActivity.onTransferClick(true));
                } else if (position == POSITION_TRANSFER_FROM_ANOTHER_DEVICE) {
                    title = mListActivity.getString(R.string.account_activity_transfer_from_another_device);
                    iconId = R.drawable.migration_another_device_icon;
                    settingIconViewHolder.itemView.setOnClickListener(view -> mListActivity.onTransferClick(false));
                } else if (position == POSITION_EXPORT_CONVERSATIONS) {
                    title = mListActivity.getString(R.string.show_contact_activity_export_contents);
                    iconId = R.drawable.share_icon;
                    settingIconViewHolder.itemView.setOnClickListener(view -> mListActivity.onExportClick());
                } else if (position == POSITION_CLEANUP) {
                    title = mListActivity.getString(R.string.show_contact_activity_cleanup);
                    iconId = R.drawable.cleanup_icon;
                    settingIconViewHolder.itemView.setOnClickListener(view -> mListActivity.onCleanupClick());
                } else {
                    title = mListActivity.getString(R.string.deleted_account_activity_delete);
                    textColor = Design.DELETE_COLOR_RED;
                    iconId = R.drawable.delete_icon;
                    iconColor = Design.DELETE_COLOR_RED;
                    settingIconViewHolder.itemView.setOnClickListener(view -> mListActivity.onDeleteAccountClick());
                }
                settingIconViewHolder.onBind(title, textColor, iconId, iconColor, false);
            }
        }
    }

    private String getSectionTitle(int position) {

        String title = "";

        if (position == SECTION_TRANSFER) {
            title = mListActivity.getString(R.string.account_activity_transfer_between_devices);
        } else if (position == SECTION_CONVERSATIONS) {
            title = mListActivity.getString(R.string.account_activity_conversations_content_title);
        }

        return title;
    }
}