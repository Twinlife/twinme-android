/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (fabrice.trescartes@twin.life)
 */

package org.twinlife.twinme.ui.conversationActivity.poll;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.baseItemActivity.BaseItemActivity;
import org.twinlife.twinme.ui.conversationActivity.AnnotationInfoViewHolder;
import org.twinlife.twinme.ui.conversationActivity.UIAnnotation;
import org.twinlife.twinme.utils.SectionTitleViewHolder;

import java.util.List;

public class PollResultAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "PollResultAdapter";
    private static final boolean DEBUG = false;

    private final BaseItemActivity mActivity;

    @NonNull
    private List<PollResultItem> mItems;

    PollResultAdapter(BaseItemActivity activity, @NonNull List<PollResultItem> items) {

        mActivity = activity;
        mItems = items;
        setHasStableIds(true);
    }

    public void setItems(List<PollResultItem>items) {

        mItems = items;
        notifyItemRangeChanged(0, mItems.size());
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mActivity.getLayoutInflater();
        View convertView;
        if (viewType == PollResultItem.PollResultItemType.POLL_RESULT_CHOICE.ordinal()) {
            convertView = inflater.inflate(R.layout.section_title_item, parent, false);
            return new SectionTitleViewHolder(convertView);
        }  else {
            convertView = inflater.inflate(R.layout.annotation_info_item, parent, false);
            return new AnnotationInfoViewHolder(convertView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {

        PollResultItem item = mItems.get(position);

        if (item.getPollResultItemType() == PollResultItem.PollResultItemType.POLL_RESULT_CHOICE) {
            SectionTitleViewHolder sectionTitleViewHolder = (SectionTitleViewHolder) viewHolder;
            sectionTitleViewHolder.onBind(item.getTitle(), Design.POPUP_BACKGROUND_COLOR, true);
        }  else if (item.getPollResultItemType() == PollResultItem.PollResultItemType.POLL_RESULT_VOTER) {
            boolean hideSeparator = false;
            if (position + 1 < mItems.size() && mItems.get(position + 1).getPollResultItemType() != PollResultItem.PollResultItemType.POLL_RESULT_CHOICE) {
                hideSeparator = true;
            }
            UIAnnotation uiAnnotation = new UIAnnotation(null, item.getTitle(), item.getAvatar(), -1, ConversationService.AnnotationType.POLL);
            AnnotationInfoViewHolder annotationInfoViewHolder = (AnnotationInfoViewHolder) viewHolder;
            annotationInfoViewHolder.onBind(mActivity, uiAnnotation, Design.POPUP_BACKGROUND_COLOR, hideSeparator);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemViewType: " + position);
        }

        return mItems.get(position).getPollResultItemType().ordinal();
    }

    @Override
    public int getItemCount() {

        return mItems.size();
    }

    @Override
    public long getItemId(int position) {

        return mItems.get(position).getItemId();
    }
}
