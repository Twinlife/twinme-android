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

import java.util.List;

public class ConversationFilesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "ConversationFilesAd...";
    private static final boolean DEBUG = false;

    private enum TYPE {
        MEDIA,
        FILE
    }

    private final ConversationFilesActivity mConversationFilesActivity;

    private List<UIFileSection> mFileSections;

    public ConversationFilesAdapter(ConversationFilesActivity listActivity, List<UIFileSection> fileSections) {

        mConversationFilesActivity = listActivity;
        mFileSections = fileSections;
        setHasStableIds(true);
    }

    public void setFileSections(List<UIFileSection> fileSections) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setFileSections: " + fileSections);
        }

        mFileSections = fileSections;
        notifyDataSetChanged();
    }

    public void updateItemInFileSection(UIFileSection fileSection) {

        int index = -1;
        for (UIFileSection fs : mFileSections) {
            index++;

            if (fs.equals(fileSection)) {
                break;
            }
        }

        if (index != -1 ) {
            notifyItemChanged(index);
        }
    }

    @Override
    public int getItemViewType(int position) {

        if (mConversationFilesActivity.getCustomTabType() == UICustomTab.CustomTabType.IMAGE || mConversationFilesActivity.getCustomTabType() == UICustomTab.CustomTabType.VIDEO) {
            return TYPE.MEDIA.ordinal();
        }

        return TYPE.FILE.ordinal();
    }

    @Override
    public long getItemId(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemId: position=" + position);
        }

        return mFileSections.get(position).getItemId();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mConversationFilesActivity.getLayoutInflater();

        if (viewType == TYPE.MEDIA.ordinal()) {
            View convertView = inflater.inflate(R.layout.conversation_files_activity_section_media_item, parent, false);
            return new SectionMediaViewHolder(convertView, mConversationFilesActivity);
        } else {
            View convertView = inflater.inflate(R.layout.conversation_files_activity_section_file_item, parent, false);
            return new SectionFileViewHolder(convertView, mConversationFilesActivity);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        if (position == mFileSections.size() - 1) {
            mConversationFilesActivity.loadMoreDescriptors();
        }

        int viewType = getItemViewType(position);

        if (viewType == TYPE.MEDIA.ordinal()) {
            SectionMediaViewHolder sectionMediaViewHolder = (SectionMediaViewHolder) viewHolder;
            sectionMediaViewHolder.onBind(mFileSections.get(position));
        } else {
            SectionFileViewHolder sectionFileViewHolder = (SectionFileViewHolder) viewHolder;
            sectionFileViewHolder.onBind(mFileSections.get(position));
        }
    }

    @Override
    public int getItemCount() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemCount");
        }

        return mFileSections.size();
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }

        int position = viewHolder.getBindingAdapterPosition();
        int viewType = getItemViewType(position);
        if (position != -1) {
            if (viewType == TYPE.MEDIA.ordinal()) {
                SectionMediaViewHolder sectionMediaViewHolder = (SectionMediaViewHolder) viewHolder;
                sectionMediaViewHolder.onBind(mFileSections.get(position));
            } else {
                SectionFileViewHolder sectionFileViewHolder = (SectionFileViewHolder) viewHolder;
                sectionFileViewHolder.onBind(mFileSections.get(position));
            }
        }
    }
}