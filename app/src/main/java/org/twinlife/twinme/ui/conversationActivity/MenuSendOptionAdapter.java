/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.conversationActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.utils.Utils;

public class MenuSendOptionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "MenuSendOptionAdapter";
    private static final boolean DEBUG = false;

    @NonNull
    private final AbstractTwinmeActivity mActivity;
    @NonNull
    private final MenuSendOptionView mMenuSendOptionView;

    private int ITEM_COUNT;

    private static final int POSITION_ALLOW_EPHEMERAL = 0;
    private int POSITION_TIMEOUT = 1;
    private int POSITION_ALLOW_COPY = 2;

    private static final int CHECKBOX = 0;
    private static final int VALUE = 1;

    private boolean mAllowCopy;
    private boolean mAllowEphemeral;
    private boolean mForceDarkMode = false;
    private int mTimeout;

    MenuSendOptionAdapter(@NonNull AbstractTwinmeActivity activity, @NonNull MenuSendOptionView menuSendOptionView, boolean allowCopy, boolean allowEphemeral, int timeout, boolean forceDarkMode) {

        mActivity = activity;
        mMenuSendOptionView = menuSendOptionView;
        mAllowCopy = allowCopy;
        mAllowEphemeral = allowEphemeral;
        mTimeout = timeout;
        mForceDarkMode = forceDarkMode;

        if (mAllowEphemeral) {
            ITEM_COUNT = 3;
            POSITION_TIMEOUT = 1;
            POSITION_ALLOW_COPY = 2;
        } else {
            ITEM_COUNT = 2;
            POSITION_TIMEOUT = -1;
            POSITION_ALLOW_COPY = 1;
        }

        setHasStableIds(false);
    }

    public void updateOptions(boolean allowCopy, boolean allowEphemeral, int timeout) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateOptions: " + allowCopy + " allowEphemeral: " + allowEphemeral + " timeout: " + timeout);
        }

        mAllowCopy = allowCopy;
        mAllowEphemeral = allowEphemeral;
        mTimeout = timeout;

        if (mAllowEphemeral) {
            ITEM_COUNT = 3;
            POSITION_TIMEOUT = 1;
            POSITION_ALLOW_COPY = 2;
        } else {
            ITEM_COUNT = 2;
            POSITION_TIMEOUT = -1;
            POSITION_ALLOW_COPY = 1;
        }

        notifyDataSetChanged();;
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

        if (position == POSITION_TIMEOUT) {
            return VALUE;
        } else {
            return CHECKBOX;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        int viewType = getItemViewType(position);

        if (viewType == CHECKBOX) {
            MenuSendOptionViewHolder menuSendOptionViewHolder = (MenuSendOptionViewHolder) viewHolder;

            boolean isOn = mAllowCopy;
            boolean hideSeparator = true;
            int tag = MenuSendOptionView.ALLOW_COPY_TAG;
            int icon = mAllowCopy ? R.drawable.send_option_copy_allowed_icon : R.drawable.send_option_copy_icon;
            String title = mActivity.getString(R.string.conversation_activity_send_menu_allow_copy);
            if (position == POSITION_ALLOW_EPHEMERAL) {
                isOn = mAllowEphemeral;
                tag = MenuSendOptionView.ALLOW_EPHEMERAL_TAG;
                icon = R.drawable.send_option_ephemeral_icon;
                title = mActivity.getString(R.string.settings_activity_ephemeral_title);
                hideSeparator = false;
            }

            int finalTag = tag;
            CompoundButton.OnCheckedChangeListener onCheckedChangeListener = (compoundButton, value) -> mMenuSendOptionView.onOptionChangeValue(finalTag, value);
            ((MenuSendOptionViewHolder) viewHolder).onBind(title, icon, tag, isOn, true, mForceDarkMode, Design.POPUP_BACKGROUND_COLOR, hideSeparator, onCheckedChangeListener);

        } else if (viewType == VALUE) {
            SelectValueViewHolder selectValueViewHolder = (SelectValueViewHolder) viewHolder;
            selectValueViewHolder.itemView.setOnClickListener(v -> mMenuSendOptionView.onAllowEphemeralClick());
            selectValueViewHolder.onBind(mActivity.getString(R.string.application_timeout), Utils.formatTimeout(mActivity, mTimeout), mForceDarkMode, Design.POPUP_BACKGROUND_COLOR);
        }
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mActivity.getLayoutInflater();
        View convertView;

        if (viewType == CHECKBOX) {
            convertView = inflater.inflate(R.layout.menu_send_option_item, parent, false);
            return new MenuSendOptionViewHolder(convertView);
        } else {
            convertView = inflater.inflate(R.layout.select_value_item, parent, false);
            return new SelectValueViewHolder(convertView);
        }
    }
}
