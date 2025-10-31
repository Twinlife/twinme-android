/*
 *  Copyright (c) 2023-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.conversationFilesActivity;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;

import java.util.List;

public class CustomTabAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "ConversationFilesAd...";
    private static final boolean DEBUG = false;

    private final AbstractTwinmeActivity mActivity;

    private final List<UICustomTab> mCustomTabs;

    private final OnCustomTabClickListener mOnCustomTabClickListener;

    private int mMainColor = Design.getMainStyle();
    private int mTextSelectedColor = Color.WHITE;

    public interface OnCustomTabClickListener {

        void onCustomTabClick(UICustomTab uiCustomTab);
    }

    public CustomTabAdapter(List<UICustomTab> customTabs, AbstractTwinmeActivity activity, OnCustomTabClickListener onCustomTabClickListener) {

        mCustomTabs = customTabs;
        mOnCustomTabClickListener = onCustomTabClickListener;
        mActivity = activity;
        setHasStableIds(false);
    }

    public void updateColor(int mainColor, int textSelectedColor) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: mainColor=" + mainColor + " textSelectedColor=" + textSelectedColor);
        }

        mMainColor = mainColor;
        mTextSelectedColor = textSelectedColor;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mActivity.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.conversation_files_activity_custom_tab_view_item, parent, false);

        CustomTabViewHolder customTabViewHolder = new CustomTabViewHolder(convertView);
        convertView.setOnClickListener(v -> {
            int position = customTabViewHolder.getBindingAdapterPosition();
            if (position >= 0) {
                UICustomTab uiCustomTab = mCustomTabs.get(position);
                mOnCustomTabClickListener.onCustomTabClick(uiCustomTab);
            }
        });
        return customTabViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        CustomTabViewHolder customTabViewHolder = (CustomTabViewHolder) viewHolder;
        customTabViewHolder.onBind(mCustomTabs.get(position), mMainColor, mTextSelectedColor);
    }

    @Override
    public int getItemCount() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemCount");
        }

        return mCustomTabs.size();
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }

    }

}
