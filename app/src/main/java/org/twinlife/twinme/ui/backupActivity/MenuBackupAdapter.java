/*
 *  Copyright (c) 2026 twinlife SA.
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
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.profiles.MenuIconViewHolder;
import org.twinlife.twinme.utils.UIMenuSelectAction;

public class MenuBackupAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "MenuSelectActionAdapter";
    private static final boolean DEBUG = false;

    @NonNull
    private final OnMenuBackupClickListener mOnMenuBackupClickListener;

    public interface OnMenuBackupClickListener {
        void onMenuBackupClick(int position);
    }

    private final AbstractTwinmeActivity mActivity;

    private static final int ITEM_COUNT = 3;

    private static final int POSITION_BACKUP_INFO = 0;
    private static final int POSITION_VERIFY_BACKUP = 1;

    private static final int INFO = 0;
    private static final int ACTION = 1;

    private final String mBackupName;

    MenuBackupAdapter(AbstractTwinmeActivity activity, @NonNull String backupName, @NonNull OnMenuBackupClickListener onMenuBackupClickListener) {

        mActivity = activity;
        mBackupName = backupName;
        mOnMenuBackupClickListener = onMenuBackupClickListener;

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
            return INFO;
        } else {
            return ACTION;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mActivity.getLayoutInflater();
        View convertView;

        if (viewType == INFO) {
            convertView = inflater.inflate(R.layout.restore_activity_backup_info_item, parent, false);
            return new BackupInfoViewHolder(convertView);
        } else {
            convertView = inflater.inflate(R.layout.menu_icon_item, parent, false);
            return new MenuIconViewHolder(convertView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {

        int viewType = getItemViewType(position);

        if (viewType == INFO) {
            BackupInfoViewHolder backupInfoViewHolder = (BackupInfoViewHolder) viewHolder;
            backupInfoViewHolder.onBind(mBackupName, Design.POPUP_BACKGROUND_COLOR);
        } else {
            MenuIconViewHolder menuIconViewHolder = (MenuIconViewHolder) viewHolder;

            menuIconViewHolder.itemView.setOnClickListener(v -> {
                if (position >= 0) {
                    mOnMenuBackupClickListener.onMenuBackupClick(position);
                }
            });
            boolean hideSeparator = position + 1 == getItemCount();
            UIMenuSelectAction action;
            if (position == POSITION_VERIFY_BACKUP) {
                action = new UIMenuSelectAction(mActivity.getString(R.string.application_backup_verify), R.drawable.backup_verify_icon);
            } else {
                action = new UIMenuSelectAction(mActivity.getString(R.string.application_backup_restore), R.drawable.restore_icon);
            }
            menuIconViewHolder.onBind(action, hideSeparator);
        }
    }
}