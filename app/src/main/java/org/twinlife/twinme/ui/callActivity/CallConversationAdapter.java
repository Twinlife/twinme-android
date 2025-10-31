/*
 *  Copyright (c) 2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.callActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.ui.baseItemActivity.Item;

import java.util.List;

public class CallConversationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "CallConversationAdapter";
    private static final boolean DEBUG = false;

    private final List<Item> mItems;
    private final CallActivity mCallActivity;

    public CallConversationAdapter(CallActivity callActivity, List<Item> items) {

        mCallActivity = callActivity;
        mItems = items;
        setHasStableIds(true);
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
            Log.d(LOG_TAG, "getItemViewType: position=" + position);
        }

        return getItem(position).getType().ordinal();
    }

    @Override
    public long getItemId(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemId: position=" + position);
        }

        return getItem(position).getItemId();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        Item item = getItem(position);

        if (item.getType() == Item.ItemType.NAME) {
            CallNameViewHolder callNameViewHolder = (CallNameViewHolder) viewHolder;
            callNameViewHolder.onBind(item);
        } else if (item.getType() == Item.ItemType.MESSAGE) {
            CallMessageViewHolder callMessageViewHolder = (CallMessageViewHolder) viewHolder;
            callMessageViewHolder.onBind(item);
        } else {
            CallPeerMessageViewHolder callPeerMessageViewHolder = (CallPeerMessageViewHolder) viewHolder;
            callPeerMessageViewHolder.onBind(item);
        }
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mCallActivity.getLayoutInflater();
        View convertView;

        if (Item.ItemType.values()[viewType] == Item.ItemType.NAME) {
            convertView = inflater.inflate(R.layout.call_activity_conversation_name_item, parent, false);

            return new CallNameViewHolder(convertView);
        } else if (Item.ItemType.values()[viewType] == Item.ItemType.MESSAGE) {
            convertView = inflater.inflate(R.layout.call_activity_conversation_message_item, parent, false);

            return new CallMessageViewHolder(mCallActivity, convertView);
        } else {
            convertView = inflater.inflate(R.layout.call_activity_conversation_peer_message_item, parent, false);

            return new CallPeerMessageViewHolder(mCallActivity, convertView);
        }
    }

    private Item getItem(int position) {

        return mItems.get(position);
    }

}