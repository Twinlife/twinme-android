/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.conversations;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.conversationActivity.MenuSwitchViewHolder;
import org.twinlife.twinme.ui.conversationActivity.SelectValueViewHolder;
import org.twinlife.twinme.ui.profiles.MenuIconViewHolder;
import org.twinlife.twinme.utils.CommonUtils;
import org.twinlife.twinme.utils.UIMenuSelectAction;

import java.util.ArrayList;
import java.util.List;

public class MenuConversationShortcutAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "MenuSelectActionAdapter";
    private static final boolean DEBUG = false;

    private final List<MenuConversationShortcutItem> mItems = new ArrayList<>();
    private final MenuConversationShortcutView mMenuConversationShortcutView;

    private static final int HEADER = 0;
    private static final int CHECKBOX = 1;
    private static final int VALUE = 2;
    private static final int ACTION = 3;

    MenuConversationShortcutAdapter(MenuConversationShortcutView menuConversationShortcutView) {
        if (DEBUG) {
            Log.d(LOG_TAG, "MenuConversationShortcutAdapter: menuConversationShorcutView=" + menuConversationShortcutView);
        }

        mMenuConversationShortcutView = menuConversationShortcutView;
        setHasStableIds(false);
        loadItems();
    }

    public void loadItems() {
        if (DEBUG) {
            Log.d(LOG_TAG, "loadItems");
        }

        mItems.clear();
        mItems.add(new MenuConversationShortcutItem(MenuConversationShortcutItem.ConversationShortcutItemType.HEADER,""));
        mItems.add(new MenuConversationShortcutItem(MenuConversationShortcutItem.ConversationShortcutItemType.NOTIFICATIONS_REACTIONS, mMenuConversationShortcutView.getContext().getString(R.string.settings_view_message_reactions_notification)));
        mItems.add(new MenuConversationShortcutItem(MenuConversationShortcutItem.ConversationShortcutItemType.SILENT_MODE, mMenuConversationShortcutView.getContext().getString(R.string.settings_view_silent_mode)));

        if (mMenuConversationShortcutView.isSilentModeEnabled()) {
            mItems.add(new MenuConversationShortcutItem(MenuConversationShortcutItem.ConversationShortcutItemType.SILENT_MODE_DURATION, mMenuConversationShortcutView.getContext().getString(R.string.settings_view_turn_off_notification_sounds)));
        }

        mItems.add(new MenuConversationShortcutItem(MenuConversationShortcutItem.ConversationShortcutItemType.RESET_CONVERSATION, mMenuConversationShortcutView.getContext().getString(R.string.main_view_reset_conversation)));

        notifyItemRangeChanged(0, mItems.size());
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = (LayoutInflater) mMenuConversationShortcutView.getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );

        if (viewType == HEADER) {
            View convertView = inflater.inflate(R.layout.menu_header_item, parent, false);
            return new MenuHeaderViewHolder(convertView);
        } else if (viewType == CHECKBOX) {
            View convertView = inflater.inflate(R.layout.menu_send_option_item, parent, false);
            return new MenuSwitchViewHolder(convertView);
        } else if (viewType == ACTION) {
            View convertView = inflater.inflate(R.layout.menu_icon_item, parent, false);
             return new MenuIconViewHolder(convertView);
        } else {
            View convertView = inflater.inflate(R.layout.select_value_item, parent, false);
            return new SelectValueViewHolder(convertView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        int viewType = getItemViewType(position);
        MenuConversationShortcutItem item = mItems.get(position);
        if (viewType == HEADER) {
            MenuHeaderViewHolder menuHeaderViewHolder = (MenuHeaderViewHolder) viewHolder;
            menuHeaderViewHolder.itemView.setOnClickListener(view -> mMenuConversationShortcutView.onHeaderClick());
            menuHeaderViewHolder.onBind(mMenuConversationShortcutView.getConversation());
        } else if (viewType == CHECKBOX) {
            MenuSwitchViewHolder menuSwitchViewHolder = (MenuSwitchViewHolder) viewHolder;
            int icon = item.getType() == MenuConversationShortcutItem.ConversationShortcutItemType.NOTIFICATIONS_REACTIONS ? R.drawable.notification_reaction_icon : R.drawable.notifications_icon;
            boolean isSelected = item.getType() == MenuConversationShortcutItem.ConversationShortcutItemType.NOTIFICATIONS_REACTIONS ? mMenuConversationShortcutView.isNotificationReactionEnabled() : mMenuConversationShortcutView.isSilentModeEnabled();
            CompoundButton.OnCheckedChangeListener onCheckedChangeListener = (compoundButton, value) -> mMenuConversationShortcutView.onSettingCheckedChange(item, value);
            menuSwitchViewHolder.onBind(item.geText(), icon, 0, isSelected, true, false, Design.POPUP_BACKGROUND_COLOR, false, onCheckedChangeListener);
        } else if (viewType == ACTION) {
            MenuIconViewHolder menuIconViewHolder = (MenuIconViewHolder) viewHolder;
            menuIconViewHolder.itemView.setOnClickListener(view -> mMenuConversationShortcutView.onResetConversationClick());
            UIMenuSelectAction action = new UIMenuSelectAction(item.geText(), R.drawable.toolbar_trash_grey);
            menuIconViewHolder.onBind(action, Design.DELETE_COLOR_RED, true);
        } else if (viewType == VALUE) {
            SelectValueViewHolder selectValueViewHolder = (SelectValueViewHolder) viewHolder;
            selectValueViewHolder.itemView.setOnClickListener(view -> mMenuConversationShortcutView.onSilentModeDurationClick());

            String value = "";
            if (mMenuConversationShortcutView.getSilentModeExpiration() > 0) {
                value = String.format(mMenuConversationShortcutView.getContext().getString(R.string.application_until), CommonUtils.formatItemInterval(mMenuConversationShortcutView.getContext(), mMenuConversationShortcutView.getSilentModeExpiration()));
            } else if (mMenuConversationShortcutView.getSilentModeExpiration() == -1) {
                value = mMenuConversationShortcutView.getContext().getString(R.string.contact_capabilities_view_camera_control_allow);
            }

            selectValueViewHolder.onBind(item.geText(), value, R.drawable.ephemeral_icon, true, Design.POPUP_BACKGROUND_COLOR);
        }
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

        MenuConversationShortcutItem item = mItems.get(position);

        switch (item.getType()) {
            case HEADER:
                return HEADER;

            case NOTIFICATIONS_REACTIONS:
            case SILENT_MODE:
                return CHECKBOX;

            case SILENT_MODE_DURATION:
                return VALUE;

            case RESET_CONVERSATION:
                return ACTION;

            default:
                return -1;
        }
    }
}