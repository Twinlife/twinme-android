/*
 *  Copyright (c) 2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.conversationActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;

import java.util.List;

public class MenuItemAdapter extends RecyclerView.Adapter<MenuItemViewHolder> {

    private final AbstractTwinmeActivity mActivity;
    private List<UIMenuAction> mActions;
    private final OnActionClickListener mOnActionClickListener;

    public interface OnActionClickListener {

        void onActionClick(UIMenuAction menuAction);
    }

    MenuItemAdapter(AbstractTwinmeActivity activity, OnActionClickListener onActionClickListener, List<UIMenuAction> actions) {

        mActivity = activity;
        mOnActionClickListener = onActionClickListener;
        mActions = actions;
    }

    public void setActions(List<UIMenuAction> actions) {

        mActions = actions;
        synchronized (this) {
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public MenuItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = mActivity.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.menu_item_child, parent, false);

        MenuItemViewHolder menuItemViewHolder = new MenuItemViewHolder(convertView);
        convertView.setOnClickListener(v -> {
            int position = menuItemViewHolder.getBindingAdapterPosition();
            if (position >= 0) {
                UIMenuAction menuAction = mActions.get(position);
                mOnActionClickListener.onActionClick(menuAction);
            }
        });
        return menuItemViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MenuItemViewHolder viewHolder, int position) {

        UIMenuAction menuAction = mActions.get(position);
        boolean hideSeparator = position + 1 == mActions.size();
        int colorFilter = Design.BLACK_COLOR;
        if (menuAction.getActionType() == UIMenuAction.ActionType.DELETE) {
            colorFilter = Design.DELETE_COLOR_RED;
        }
        viewHolder.onBind(menuAction.getTitle(), ResourcesCompat.getDrawable(mActivity.getResources(), menuAction.getImage(), mActivity.getTheme()), colorFilter, menuAction.getEnabledAction(), hideSeparator);
    }

    @Override
    public int getItemCount() {

        return mActions.size();
    }

    @Override
    public void onViewRecycled(@NonNull MenuItemViewHolder viewHolder) {

        viewHolder.onViewRecycled();
    }
}
