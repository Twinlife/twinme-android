/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.profiles;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.models.Profile;
import org.twinlife.twinme.ui.settingsActivity.PersonalizationViewHolder;

public class MenuPropagatingAdapter extends RecyclerView.Adapter<PersonalizationViewHolder> {
    private static final String LOG_TAG = "MenuPropagatingAdapter";
    private static final boolean DEBUG = false;

    public interface OnMenuPropagatingClickListener {

        void onSelectUpdateMode(Profile.UpdateMode updateMode);
    }

    private final MenuPropagatingProfileView mMenuPropagatingProfileView;

    private final OnMenuPropagatingClickListener mOnMenuPropagatingClickListener;

    private static final int ITEM_COUNT = 3;

    public MenuPropagatingAdapter(MenuPropagatingProfileView menuPropagatingProfileView, OnMenuPropagatingClickListener onMenuPropagatingClickListener) {

        mMenuPropagatingProfileView = menuPropagatingProfileView;
        mOnMenuPropagatingClickListener = onMenuPropagatingClickListener;
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
    public void onBindViewHolder(@NonNull PersonalizationViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        String title;
        boolean isSelected;
        if (position == 0) {
            title = mMenuPropagatingProfileView.getContext().getString(R.string.edit_profile_activity_propagating_no_contact);
            isSelected = mMenuPropagatingProfileView.getUpdateMode() == Profile.UpdateMode.NONE;
            viewHolder.itemView.setOnClickListener(view -> {
                mOnMenuPropagatingClickListener.onSelectUpdateMode(Profile.UpdateMode.NONE);
                notifyItemRangeChanged(0, ITEM_COUNT);
            });
        } else if (position == 1) {
            title = mMenuPropagatingProfileView.getContext().getString(R.string.edit_profile_activity_propagating_except_contacts);
            isSelected = mMenuPropagatingProfileView.getUpdateMode() == Profile.UpdateMode.DEFAULT;
            viewHolder.itemView.setOnClickListener(view -> {
                mOnMenuPropagatingClickListener.onSelectUpdateMode(Profile.UpdateMode.DEFAULT);
                notifyItemRangeChanged(0, ITEM_COUNT);
            });
        } else {
            title = mMenuPropagatingProfileView.getContext().getString(R.string.edit_profile_activity_propagating_all_contacts);
            isSelected = mMenuPropagatingProfileView.getUpdateMode() == Profile.UpdateMode.ALL;
            viewHolder.itemView.setOnClickListener(view -> {
                mOnMenuPropagatingClickListener.onSelectUpdateMode(Profile.UpdateMode.ALL);
                notifyItemRangeChanged(0, ITEM_COUNT);
            });
        }


        viewHolder.onBind(title, isSelected);
    }

    @Override
    @NonNull
    public PersonalizationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = (LayoutInflater) mMenuPropagatingProfileView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View convertView = inflater.inflate(R.layout.personalization_activity_item, parent, false);
        return new PersonalizationViewHolder(convertView);
    }

    @Override
    public void onViewRecycled(@NonNull PersonalizationViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }

    }
}


