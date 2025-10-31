/*
 *  Copyright (c) 2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.cleanupActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.rooms.InformationViewHolder;
import org.twinlife.twinme.ui.settingsActivity.SettingSectionViewHolder;

public class TypeCleanUpAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "TypeCleanUpAdapter";
    private static final boolean DEBUG = false;

    public interface OnTypeCleanupClickListener {

        void onLocalCleanUpClick();

        void onBothCleanUpClick();

        void onResetConversationClick();
    }

    private final TypeCleanUpActivity mCleanupActivity;

    private final OnTypeCleanupClickListener mOnTypeCleanupClickListener;

    private static final int ITEM_COUNT = 6;

    private static final int POSITION_LOCAL_CLEANUP = 0;
    private static final int POSITION_LOCAL_CLEANUP_INFORMATION = 1;
    private static final int POSITION_BOTH_CLEANUP = 2;
    private static final int POSITION_BOTH_CLEANUP_INFORMATION = 3;
    private static final int POSITION_RESET_CONVERSATION = 4;
    private static final int POSITION_RESET_CONVERSATION_INFORMATION = 5;

    private static final int INFO = 0;
    private static final int SUBSECTION = 1;

    public TypeCleanUpAdapter(TypeCleanUpActivity listActivity, OnTypeCleanupClickListener onTypeCleanupClickListener) {

        mCleanupActivity = listActivity;
        mOnTypeCleanupClickListener = onTypeCleanupClickListener;
        setHasStableIds(true);
    }

    @Override
    public int getItemCount() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemCount");
        }

        if (mCleanupActivity.showResetConversation()) {
            return ITEM_COUNT - 2;
        }

        return ITEM_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemViewType: " + position);
        }

        if (position == POSITION_BOTH_CLEANUP || position == POSITION_LOCAL_CLEANUP || position == POSITION_RESET_CONVERSATION) {
            return SUBSECTION;
        } else {
            return INFO;
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
            if (position == POSITION_LOCAL_CLEANUP_INFORMATION) {
                informationViewHolder.onBind(mCleanupActivity.getString(R.string.cleanup_activity_info), true);
            } else if (position == POSITION_BOTH_CLEANUP_INFORMATION) {
                informationViewHolder.onBind(mCleanupActivity.getString(R.string.cleanup_activity_info_both), true);
            } else if (position == POSITION_RESET_CONVERSATION_INFORMATION) {
                informationViewHolder.onBind(mCleanupActivity.getString(R.string.cleanup_activity_reset_conversation_message), true);
            }
        } else if (viewType == SUBSECTION) {
            SettingSectionViewHolder settingSectionViewHolder = (SettingSectionViewHolder) viewHolder;
            if (position == POSITION_LOCAL_CLEANUP) {
                settingSectionViewHolder.itemView.setOnClickListener(view -> mOnTypeCleanupClickListener.onLocalCleanUpClick());
                settingSectionViewHolder.onBind(mCleanupActivity.getString(R.string.cleanup_activity_local_cleanup), false);
            } else if (position == POSITION_BOTH_CLEANUP) {
                settingSectionViewHolder.itemView.setOnClickListener(view -> mOnTypeCleanupClickListener.onBothCleanUpClick());
                settingSectionViewHolder.onBind(mCleanupActivity.getString(R.string.cleanup_activity_both_clean), false);
            } else if (position == POSITION_RESET_CONVERSATION) {
                settingSectionViewHolder.itemView.setOnClickListener(view -> mOnTypeCleanupClickListener.onResetConversationClick());
                settingSectionViewHolder.onBind(mCleanupActivity.getString(R.string.main_activity_reset_conversation_title), Design.DELETE_COLOR_RED, true);
            }
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

        if (viewType == INFO) {
            convertView = inflater.inflate(R.layout.settings_room_activity_information_item, parent, false);
            return new InformationViewHolder(convertView);
        } else {
            convertView = inflater.inflate(R.layout.settings_activity_item_section, parent, false);
            return new SettingSectionViewHolder(convertView);
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }

    }
}