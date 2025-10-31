/*
 *  Copyright (c) 2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.externalCallActivity;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;

public class MenuCallCapabilitiesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "MenuCallCapabili...";
    private static final boolean DEBUG = false;

    private final MenuCallCapabilitiesView mMenuCallCapabilitiesView;

    private final static int ITEM_COUNT = 3;

    private static final int POSITION_ALLOW_AUDIO_CALL = 0;
    private static final int POSITION_ALLOW_VIDEO_CALL = 1;

    public MenuCallCapabilitiesAdapter(MenuCallCapabilitiesView menuCallCapabilitiesView) {

        mMenuCallCapabilitiesView = menuCallCapabilitiesView;
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
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        MenuCallCapabilitiesViewHolder capabilityViewHolder = (MenuCallCapabilitiesViewHolder) viewHolder;

        boolean isSelected = mMenuCallCapabilitiesView.isCapabilitiesOn(position + 1);
        String title;
        int switchTag = position + 1;
        boolean isEnabled = true;

        if (position == POSITION_ALLOW_AUDIO_CALL) {
            title = mMenuCallCapabilitiesView.getContext().getString(R.string.conversation_activity_audio_call);
        } else if (position == POSITION_ALLOW_VIDEO_CALL) {
            title = mMenuCallCapabilitiesView.getContext().getString(R.string.conversation_activity_video_call);
        } else {
            title = mMenuCallCapabilitiesView.getContext().getString(R.string.show_call_activity_settings_group_calls);
        }

        capabilityViewHolder.onBind(title, switchTag, isEnabled, isSelected);
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = (LayoutInflater) mMenuCallCapabilitiesView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View convertView = inflater.inflate(R.layout.menu_call_capabilities_item, parent, false);
        return new MenuCallCapabilitiesViewHolder(convertView, mMenuCallCapabilitiesView);
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }
    }
}