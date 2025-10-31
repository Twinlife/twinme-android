/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.conversationActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;

import java.util.List;

public class MenuActionConversationAdapter extends RecyclerView.Adapter<MenuActionConversationViewHolder> {
    private static final String LOG_TAG = "MenuActionConversa...";
    private static final boolean DEBUG = false;

    private static final int OFFSET_VISIBILITY_DELAY = 20;

    private final ConversationActivity mActivity;
    private final List<UIActionConversation> mActions;
    private final OnActionClickListener mOnActionClickListener;

    public interface OnActionClickListener {

        void onActionClick(UIActionConversation actionConversation);
    }

    MenuActionConversationAdapter(ConversationActivity activity, OnActionClickListener onActionClickListener, List<UIActionConversation> actions) {
        if (DEBUG) {
            Log.d(LOG_TAG, "init");
        }

        mActivity = activity;
        mOnActionClickListener = onActionClickListener;
        mActions = actions;
    }

    @NonNull
    @Override
    public MenuActionConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder=" + viewType);
        }

        LayoutInflater inflater = mActivity.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.menu_action_conversation_view_item, parent, false);

        MenuActionConversationViewHolder menuActionConversationViewHolder = new MenuActionConversationViewHolder(convertView);
        convertView.setOnClickListener(v -> {
            int position = menuActionConversationViewHolder.getBindingAdapterPosition();
            if (position >= 0) {
                UIActionConversation actionConversation = mActions.get(position);
                mOnActionClickListener.onActionClick(actionConversation);
            }
        });
        return menuActionConversationViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MenuActionConversationViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder=" + position);
        }

        UIActionConversation actionConversation = mActions.get(position);
        int delay = 100 + OFFSET_VISIBILITY_DELAY * (mActions.size() - position);
        viewHolder.onBind(mActivity, actionConversation, delay);
    }

    @Override
    public int getItemCount() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemCount");
        }

        return mActions.size();
    }
}
