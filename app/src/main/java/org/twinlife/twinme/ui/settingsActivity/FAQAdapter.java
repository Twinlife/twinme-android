/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.settingsActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.utils.SectionTitleViewHolder;
import org.twinlife.twinme.utils.faq.UIFAQCategory;
import org.twinlife.twinme.utils.faq.UIFAQItem;

import java.util.ArrayList;
import java.util.List;

public class FAQAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "FAQAdapter";
    private static final boolean DEBUG = false;

    private final FAQActivity mFAQActivity;

    private final List<UIFAQItem> mItems = new ArrayList<>();

    private static final int SECTION = 0;
    private static final int FAQ = 1;

    FAQAdapter(FAQActivity faqActivity, List<UIFAQCategory> uiFAQCategories) {

        mFAQActivity = faqActivity;
        loadItems(uiFAQCategories);
    }

    public void updateFAQCategories(List<UIFAQCategory> uiFAQCategories) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateFAQCategories");
        }

        loadItems(uiFAQCategories);
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

        UIFAQItem item = mItems.get(position);
        if (item.getType() == UIFAQItem.FAQItemType.CATEGORY) {
            return SECTION;
        } else {
            return FAQ;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        int viewType = getItemViewType(position);

        UIFAQItem item = mItems.get(position);
        if (viewType == SECTION) {
            SectionTitleViewHolder sectionTitleViewHolder = (SectionTitleViewHolder) viewHolder;
            sectionTitleViewHolder.onBind(item.getTitle(), false);
        } else if (viewType == FAQ) {
            FAQViewHolder faqViewHolder = (FAQViewHolder) viewHolder;
            faqViewHolder.itemView.setOnClickListener(view -> mFAQActivity.onFAQClick(item));
            faqViewHolder.onBind(item, false);
        }
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mFAQActivity.getLayoutInflater();
        View convertView;

        if (viewType == SECTION) {
            convertView = inflater.inflate(R.layout.section_title_item, parent, false);
            return new SectionTitleViewHolder(convertView);
        } else {
            convertView = inflater.inflate(R.layout.faq_activity_item, parent, false);
            return new FAQViewHolder(convertView);
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }

    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewDetachedFromWindow: viewHolder=" + viewHolder);
        }

        super.onViewDetachedFromWindow(viewHolder);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewAttachedToWindow: viewHolder=" + viewHolder);
        }

        super.onViewAttachedToWindow(viewHolder);
    }

    private void loadItems(List<UIFAQCategory> uiFAQCategories) {
        if (DEBUG) {
            Log.d(LOG_TAG, "loadItems");
        }

        List<UIFAQItem> newItems = new ArrayList<>();
        for (UIFAQCategory category : uiFAQCategories) {
            newItems.add(category);
            newItems.addAll(category.getArticles());
        }

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return mItems.size();
            }

            @Override
            public int getNewListSize() {
                return newItems.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                UIFAQItem oldItem = mItems.get(oldItemPosition);
                UIFAQItem newItem = newItems.get(newItemPosition);

                if (oldItem.getType() != newItem.getType()) {
                    return false;
                }

                if (oldItem.getArticleId() != newItem.getArticleId()) {
                    return false;
                }

                return oldItem.equals(newItem);
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {

                return areItemsTheSame(oldItemPosition, newItemPosition);
            }
        });

        mItems.clear();
        mItems.addAll(newItems);

        diffResult.dispatchUpdatesTo(this);
    }
}
