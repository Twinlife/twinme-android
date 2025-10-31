/*
 *  Copyright (c) 2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.conversationFilesActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.ui.baseItemActivity.Item;

public class FileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "FileAdapter";
    private static final boolean DEBUG = false;

    private final ConversationFilesActivity mConversationFilesActivity;

    private UIFileSection mFileSection;

    private enum TYPE {
        DOCUMENT,
        LINK
    }

    public FileAdapter(ConversationFilesActivity listActivity) {

        mConversationFilesActivity = listActivity;
        setHasStableIds(true);
    }

    public void setFileSection(UIFileSection fileSection) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setFileSection: " + fileSection);
        }

        mFileSection = fileSection;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {

        if (mConversationFilesActivity.getCustomTabType() == UICustomTab.CustomTabType.DOCUMENT) {
            return TYPE.DOCUMENT.ordinal();
        }

        return TYPE.LINK.ordinal();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mConversationFilesActivity.getLayoutInflater();

        if (viewType == TYPE.LINK.ordinal()) {
            View convertView = inflater.inflate(R.layout.conversation_files_activity_link_item, parent, false);
            LinkViewHolder linkViewHolder = new LinkViewHolder(convertView);
            convertView.setOnClickListener(v -> {
                int position = linkViewHolder.getBindingAdapterPosition();
                if (position >= 0) {
                    mConversationFilesActivity.onItemClick(mFileSection.getItems().get(position));
                }
            });

            return linkViewHolder;
        } else {
            View convertView = inflater.inflate(R.layout.conversation_files_activity_document_item, parent, false);
            DocumentViewHolder documentViewHolder = new DocumentViewHolder(convertView);
            convertView.setOnClickListener(v -> {
                int position = documentViewHolder.getBindingAdapterPosition();
                if (position >= 0) {
                    mConversationFilesActivity.onItemClick(mFileSection.getItems().get(position));
                }
            });

            return documentViewHolder;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        int viewType = getItemViewType(position);
        if (viewType == TYPE.LINK.ordinal()) {
            LinkViewHolder linkViewHolder = (LinkViewHolder) viewHolder;
            linkViewHolder.onBind(mFileSection.getItems().get(position), mConversationFilesActivity);
        } else {
            DocumentViewHolder documentViewHolder = (DocumentViewHolder) viewHolder;
            documentViewHolder.onBind(mFileSection.getItems().get(position), mConversationFilesActivity);
        }
    }

    @Override
    public long getItemId(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemId: position=" + position);
        }

        Item item = mFileSection.getItems().get(position);
        return item.getItemId();
    }

    @Override
    public int getItemCount() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemCount");
        }

        if (mFileSection == null) {
            return 0;
        }

        return mFileSection.getCount();
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }

        int position = viewHolder.getBindingAdapterPosition();
        if (position != -1) {
            int viewType = getItemViewType(position);
            if (viewType == TYPE.LINK.ordinal()) {
                LinkViewHolder linkViewHolder = (LinkViewHolder) viewHolder;
                linkViewHolder.onBind(mFileSection.getItems().get(position), mConversationFilesActivity);
            } else {
                DocumentViewHolder documentViewHolder = (DocumentViewHolder) viewHolder;
                documentViewHolder.onBind(mFileSection.getItems().get(position), mConversationFilesActivity);
            }
        }
    }
}