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
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.accountActivity.SettingIconViewHolder;
import org.twinlife.twinme.utils.SectionTitleViewHolder;

import java.util.List;

public class HelpAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "HelpAdapter";
    private static final boolean DEBUG = false;

    private final HelpActivity mHelpActivity;

    private static final int SECTION = 0;
    private static final int SUBSECTION = 1;

    private final List<UIHelpItem> mItems = new java.util.ArrayList<>();

    HelpAdapter(HelpActivity helpActivity) {

        mHelpActivity = helpActivity;
        initItems();
        setHasStableIds(false);
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

        UIHelpItem item = mItems.get(position);
        if (item instanceof UIHelpSection) {
            return SECTION;
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
        UIHelpItem item = mItems.get(position);
        if (viewType == SECTION) {
            SectionTitleViewHolder sectionTitleViewHolder = (SectionTitleViewHolder) viewHolder;
            sectionTitleViewHolder.onBind(item.getTitle(), false);
        } else if (viewType == SUBSECTION) {
            UIHelpSubSection subSection = (UIHelpSubSection) item;
            SettingIconViewHolder settingIconViewHolder = (SettingIconViewHolder) viewHolder;
            settingIconViewHolder.itemView.setOnClickListener(view -> mHelpActivity.onSubSectionClick(subSection.getHelpSubSectionType()));
            settingIconViewHolder.onBind(subSection.getTitle(), Design.FONT_COLOR_DEFAULT, subSection.getIcon(), Design.BLACK_COLOR, false);
        }
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mHelpActivity.getLayoutInflater();
        View convertView;

        if (viewType == SECTION) {
            convertView = inflater.inflate(R.layout.section_title_item, parent, false);
            return new SectionTitleViewHolder(convertView);
        } else {
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

        if (position != -1) {
            int viewType = getItemViewType(position);
            UIHelpItem item = mItems.get(position);
            if (viewType == SECTION) {
                SectionTitleViewHolder sectionTitleViewHolder = (SectionTitleViewHolder) viewHolder;
                sectionTitleViewHolder.onBind(item.getTitle(), false);
            } else if (viewType == SUBSECTION) {
                UIHelpSubSection subSection = (UIHelpSubSection) item;
                SettingIconViewHolder settingIconViewHolder = (SettingIconViewHolder) viewHolder;
                settingIconViewHolder.itemView.setOnClickListener(view -> mHelpActivity.onSubSectionClick(subSection.getHelpSubSectionType()));
                settingIconViewHolder.onBind(subSection.getTitle(), Design.FONT_COLOR_DEFAULT, subSection.getIcon(), Design.BLACK_COLOR, false);
            }
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

        int position = viewHolder.getBindingAdapterPosition();

        if (position != -1) {
            int viewType = getItemViewType(position);
            UIHelpItem item = mItems.get(position);
            if (viewType == SECTION) {
                SectionTitleViewHolder sectionTitleViewHolder = (SectionTitleViewHolder) viewHolder;
                sectionTitleViewHolder.onBind(item.getTitle(), false);
            } else if (viewType == SUBSECTION) {
                UIHelpSubSection subSection = (UIHelpSubSection) item;
                SettingIconViewHolder settingIconViewHolder = (SettingIconViewHolder) viewHolder;
                settingIconViewHolder.itemView.setOnClickListener(view -> mHelpActivity.onSubSectionClick(subSection.getHelpSubSectionType()));
                settingIconViewHolder.onBind(subSection.getTitle(), Design.FONT_COLOR_DEFAULT, subSection.getIcon(), Design.BLACK_COLOR, false);
            }
        }
    }

    private void initItems() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initItems");
        }

        mItems.clear();

        List<UIHelpSection> sections = new java.util.ArrayList<>();
        sections.add(new UIHelpSection(mHelpActivity, UIHelpSection.HelpSectionType.GENERAL));
        sections.add(new UIHelpSection(mHelpActivity, UIHelpSection.HelpSectionType.STANDARD_SERVICES));
        sections.add(new UIHelpSection(mHelpActivity, UIHelpSection.HelpSectionType.PREMIUM_SERVICES));
        sections.add(new UIHelpSection(mHelpActivity, UIHelpSection.HelpSectionType.ADVANCED_SERVICES));

        for (UIHelpSection section : sections) {
            if (section.getTitle() != null && !section.getTitle().isEmpty()) {
                mItems.add(section);
            }
            mItems.addAll(section.getItems());
        }

        notifyItemRangeChanged(0, mItems.size());
    }

}
