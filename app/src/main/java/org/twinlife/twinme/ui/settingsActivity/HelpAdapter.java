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
import org.twinlife.twinme.utils.SectionTitleViewHolder;
import org.twinlife.twinme.utils.Utils;

public class HelpAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "HelpAdapter";
    private static final boolean DEBUG = false;

    private final HelpActivity mHelpActivity;

    private final static int ITEM_COUNT = 13;

    private static final int SECTION_INFO = 3;

    protected static final int POSITION_FAQ = 0;
    protected static final int POSITION_BLOG = 1;
    protected static final int POSITION_FEEDBACK = 2;
    protected static final int POSITION_WELCOME = 4;
    protected static final int POSITION_QUALITY = 5;
    protected static final int POSITION_PREMIUM = 6;
    protected static final int POSITION_SPACES = 7;
    protected static final int POSITION_PROFILE = 8;
    protected static final int POSITION_CLICK_TO_CALL = 9;
    protected static final int POSITION_CERTIFY_RELATION = 10;
    protected static final int POSITION_ACCOUNT_TRANSFER = 11;
    protected static final int POSITION_PROXY = 12;

    private static final int SECTION = 0;
    private static final int SUBSECTION = 1;

    HelpAdapter(HelpActivity helpActivity) {

        mHelpActivity = helpActivity;
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
    public int getItemViewType(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemViewType: " + position);
        }

        if (position == SECTION_INFO) {
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

        if (viewType == SECTION) {
            SectionTitleViewHolder sectionTitleViewHolder = (SectionTitleViewHolder) viewHolder;
            sectionTitleViewHolder.onBind(mHelpActivity.getString(R.string.about_activity_information), false);
        } else if (viewType == SUBSECTION) {
            SettingSectionViewHolder settingSectionViewHolder = (SettingSectionViewHolder) viewHolder;
            settingSectionViewHolder.itemView.setOnClickListener(view -> mHelpActivity.onSubSectionClick(position));
            settingSectionViewHolder.onBind(getSubSectionTitle(position), true);
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
            convertView = inflater.inflate(R.layout.settings_activity_item_section, parent, false);
            return new SettingSectionViewHolder(convertView);
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }

        int position = viewHolder.getBindingAdapterPosition();
        int viewType = getItemViewType(position);

        if (position != -1) {
            if (viewType == SECTION) {
                SectionTitleViewHolder sectionTitleViewHolder = (SectionTitleViewHolder) viewHolder;
                sectionTitleViewHolder.onBind(mHelpActivity.getString(R.string.about_activity_information), false);
            } else if (viewType == SUBSECTION) {
                SettingSectionViewHolder settingSectionViewHolder = (SettingSectionViewHolder) viewHolder;
                settingSectionViewHolder.itemView.setOnClickListener(view -> mHelpActivity.onSubSectionClick(position));
                settingSectionViewHolder.onBind(getSubSectionTitle(position), true);
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
        int viewType = getItemViewType(position);

        if (position != -1) {
            if (viewType == SECTION) {
                SectionTitleViewHolder sectionTitleViewHolder = (SectionTitleViewHolder) viewHolder;
                sectionTitleViewHolder.onBind(mHelpActivity.getString(R.string.about_activity_information), false);
            } else if (viewType == SUBSECTION) {
                SettingSectionViewHolder settingSectionViewHolder = (SettingSectionViewHolder) viewHolder;
                settingSectionViewHolder.itemView.setOnClickListener(view -> mHelpActivity.onSubSectionClick(position));
                settingSectionViewHolder.onBind(getSubSectionTitle(position), true);
            }
        }
    }

    private String getSubSectionTitle(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getSubSectionTitle: " + position);
        }

        String title = "";
        switch (position) {

            case POSITION_FAQ:
                title = mHelpActivity.getString(R.string.navigation_activity_faq);
                break;

            case POSITION_BLOG:
                title = mHelpActivity.getString(R.string.navigation_activity_blog);
                break;

            case POSITION_FEEDBACK:
                title = mHelpActivity.getString(R.string.navigation_activity_feedback);
                break;

            case POSITION_WELCOME:
                title = Utils.capitalizeString(mHelpActivity.getString(R.string.settings_activity_welcome_screen_category_title));
                break;

            case POSITION_QUALITY:
                title = mHelpActivity.getString(R.string.about_activity_quality_of_service);
                break;

            case POSITION_PREMIUM:
                title = mHelpActivity.getString(R.string.about_activity_premium_services);
                break;

            case POSITION_SPACES:
                title = mHelpActivity.getString(R.string.premium_services_activity_space_title);
                break;

            case POSITION_PROFILE:
                title = mHelpActivity.getString(R.string.application_profile);
                break;

            case POSITION_CLICK_TO_CALL:
                title = mHelpActivity.getString(R.string.premium_services_activity_click_to_call_title);
                break;

            case POSITION_CERTIFY_RELATION:
                title = mHelpActivity.getString(R.string.authentified_relation_activity_title);
                break;

            case POSITION_ACCOUNT_TRANSFER:
                title = mHelpActivity.getString(R.string.account_activity_transfer_between_devices);
                break;

            case POSITION_PROXY:
                title = mHelpActivity.getString(R.string.proxy_activity_title);
                break;

            default:
                break;
        }

        return title;
    }
}
