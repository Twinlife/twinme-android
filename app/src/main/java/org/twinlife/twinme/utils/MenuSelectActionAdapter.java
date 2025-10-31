/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.utils;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.profiles.MenuIconViewHolder;

import java.util.List;

public class MenuSelectActionAdapter extends RecyclerView.Adapter<MenuIconViewHolder> {
    private static final String LOG_TAG = "MenuSelectActionAdapter";
    private static final boolean DEBUG = false;

    @NonNull
    private final OnMenuSelectActionClickListener mOnMenuSelectActionClickListener;

    public interface OnMenuSelectActionClickListener {
        void onMenuSelectActionClick(int position);
    }

    private final List<UIMenuSelectAction> mActions;

    private final AbstractTwinmeActivity mActivity;

    MenuSelectActionAdapter(AbstractTwinmeActivity activity, @NonNull List<UIMenuSelectAction> actions, @NonNull OnMenuSelectActionClickListener onMenuSelectActionClickListener) {

        mActivity = activity;
        mActions = actions;
        mOnMenuSelectActionClickListener = onMenuSelectActionClickListener;

        setHasStableIds(true);
    }

    @NonNull
    @Override
    public MenuIconViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mActivity.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.menu_icon_item, parent, false);

        MenuIconViewHolder menuIconViewHolder = new MenuIconViewHolder(convertView);

        convertView.setOnClickListener(v -> {
            int position = menuIconViewHolder.getBindingAdapterPosition();
            if (position >= 0) {
                mOnMenuSelectActionClickListener.onMenuSelectActionClick(position);
            }
        });

        return menuIconViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MenuIconViewHolder viewHolder, int position) {

        boolean hideSeparator = position + 1 == getItemCount();
        viewHolder.onBind(mActions.get(position), hideSeparator);
    }

    @Override
    public int getItemCount() {

        return mActions.size();
    }
}