/*
 *  Copyright (c) 2023-2026twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.externalCallActivity;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.utils.SectionTitleViewHolder;

import java.util.List;

public class TemplateExternalCallAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "TemplateExternalCa...";
    private static final boolean DEBUG = false;

    @NonNull
    private final OnTemplateExternalCallClickListener mOnTemplateExternalCallClickListener;

    public interface OnTemplateExternalCallClickListener {
        void onTemplateClick(UITemplateExternalCall templateExternalCall);
    }

    private final TemplateExternalCallActivity mTemplateActivity;
    private final List<UITemplateItem> mUITemplateItems = new java.util.ArrayList<>();

    private static final int TITLE = 0;
    private static final int TEMPLATE = 1;

    TemplateExternalCallAdapter(TemplateExternalCallActivity activity,  @NonNull OnTemplateExternalCallClickListener onTemplateExternalCallClickListener) {

        mTemplateActivity = activity;
        mOnTemplateExternalCallClickListener = onTemplateExternalCallClickListener;

        initTemplates();
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mTemplateActivity.getLayoutInflater();

        if (viewType == TITLE) {
            View convertView = inflater.inflate(R.layout.section_title_item, parent, false);
            return new SectionTitleViewHolder(convertView);
        } else {
            View convertView = inflater.inflate(R.layout.template_external_call_activity_item, parent, false);
            TemplateExternalCallViewHolder templateExternalCallViewHolder = new TemplateExternalCallViewHolder(convertView);

            convertView.setOnClickListener(v -> {
                int position = templateExternalCallViewHolder.getBindingAdapterPosition();
                if (position >= 0 && mUITemplateItems.get(position).getTemplateItemType() == UITemplateItem.TemplateItemType.TEMPLATE) {
                    UITemplateExternalCall uiTemplateExternalCall = (UITemplateExternalCall) mUITemplateItems.get(position);
                    mOnTemplateExternalCallClickListener.onTemplateClick(uiTemplateExternalCall);
                }
            });

            return templateExternalCallViewHolder;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {

        UITemplateItem uiTemplateItem = mUITemplateItems.get(position);

        if (uiTemplateItem.getTemplateItemType() == UITemplateItem.TemplateItemType.SECTION) {
            SectionTitleViewHolder sectionTitleViewHolder = (SectionTitleViewHolder) viewHolder;

            UITemplateSection uiTemplateSection = (UITemplateSection) uiTemplateItem;
            String title = uiTemplateSection.getTitle();
            boolean hideSeparator = false;
            String badgeTitle = null;
            Runnable runnable = null;
            if (position == 0) {
                badgeTitle = mTemplateActivity.getString(R.string.application_new_feature);
                runnable = mTemplateActivity::showOnboardingView;
            }
            sectionTitleViewHolder.onBind(title, hideSeparator, badgeTitle, runnable);
        } else {
            TemplateExternalCallViewHolder templateExternalCallViewHolder = (TemplateExternalCallViewHolder) viewHolder;
            UITemplateExternalCall uiTemplateExternalCall = (UITemplateExternalCall) uiTemplateItem;
            boolean hideSeparator = false;
            if (position + 1 < mUITemplateItems.size()) {
                UITemplateItem nextItem = mUITemplateItems.get(position + 1);
                if (nextItem.getTemplateItemType() == UITemplateItem.TemplateItemType.SECTION) {
                    hideSeparator = true;
                }
            }
            templateExternalCallViewHolder.onBind(uiTemplateExternalCall, hideSeparator);
        }

    }

    @Override
    public int getItemCount() {

        return mUITemplateItems.size();
    }

    @Override
    public long getItemId(int position) {

        return mUITemplateItems.get(position).getItemId();
    }

    @Override
    public int getItemViewType(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemViewType: " + position);
        }

        UITemplateItem uiTemplateItem = mUITemplateItems.get(position);

        if (uiTemplateItem.getTemplateItemType() == UITemplateItem.TemplateItemType.SECTION) {
            return TITLE;
        } else {
            return TEMPLATE;
        }
    }

    public void updateProfileTemplate(String name, Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateTemplates");
        }

        for (UITemplateItem uiTemplateItem : mUITemplateItems) {
            if (uiTemplateItem.getTemplateItemType() == UITemplateItem.TemplateItemType.TEMPLATE) {
                UITemplateExternalCall uiTemplateExternalCall = (UITemplateExternalCall) uiTemplateItem;
                if (uiTemplateExternalCall.getTemplateType() == UITemplateExternalCall.TemplateType.PROFILE) {
                    uiTemplateExternalCall.setName(name);
                    uiTemplateExternalCall.setAvatar(avatar);
                    notifyItemChanged(mUITemplateItems.indexOf(uiTemplateExternalCall));
                    break;
                }
            }
        }
    }

    private void initTemplates() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initTemplates");
        }

        mUITemplateItems.clear();
        mUITemplateItems.add(new UITemplateSection(mTemplateActivity.getString(R.string.create_external_call_activity_conference_call_title)));
        mUITemplateItems.add(new UITemplateExternalCall(mTemplateActivity, UITemplateExternalCall.TemplateType.MEETING));
        mUITemplateItems.add(new UITemplateSection(mTemplateActivity.getString(R.string.create_external_call_activity_direct_call_title)));
        mUITemplateItems.add(new UITemplateExternalCall(mTemplateActivity, UITemplateExternalCall.TemplateType.HELP));
        mUITemplateItems.add(new UITemplateExternalCall(mTemplateActivity, UITemplateExternalCall.TemplateType.JOB));
        mUITemplateItems.add(new UITemplateExternalCall(mTemplateActivity, UITemplateExternalCall.TemplateType.CLASSIFIED_AD));
        mUITemplateItems.add(new UITemplateExternalCall(mTemplateActivity, UITemplateExternalCall.TemplateType.VIDEO_BELL));
        mUITemplateItems.add(new UITemplateExternalCall(mTemplateActivity, UITemplateExternalCall.TemplateType.PROFILE));
        mUITemplateItems.add(new UITemplateSection(mTemplateActivity.getString(R.string.application_default)));
        mUITemplateItems.add(new UITemplateExternalCall(mTemplateActivity, UITemplateExternalCall.TemplateType.OTHER));
    }
}