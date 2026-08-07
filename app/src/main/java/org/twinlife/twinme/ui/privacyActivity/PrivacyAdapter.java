/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui.privacyActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ShareInvitationMode;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.conversationActivity.SelectValueViewHolder;
import org.twinlife.twinme.ui.rooms.InformationViewHolder;
import org.twinlife.twinme.ui.settingsActivity.SettingSwitchViewHolder;
import org.twinlife.twinme.utils.SectionTitleViewHolder;

import java.util.ArrayList;
import java.util.List;

public class PrivacyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "MessagesSettingsAdapter";
    private static final boolean DEBUG = false;

    @NonNull
    private final PrivacyActivity mPrivacyActivity;

    private final List<UIPrivacyItem> mPrivacyItems =  new ArrayList<>();

    private static final int TITLE = 0;
    private static final int CHECKBOX = 1;
    private static final int INFO = 2;
    private static final int VALUE = 3;

    PrivacyAdapter(@NonNull PrivacyActivity listActivity) {

        mPrivacyActivity = listActivity;
        setHasStableIds(false);
        loadItems();
    }

    public void updateShareInvitationMode() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateShareInvitationMode");
        }

        for (UIPrivacyItem item : mPrivacyItems) {
            if (item.getType() == UIPrivacyItem.PrivacyItemType.SHARE_INVITATION_MODE) {
                notifyItemChanged(mPrivacyItems.indexOf(item));
            }
        }
    }

    @Override
    public int getItemCount() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemCount");
        }

        return mPrivacyItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemViewType: " + position);
        }

        UIPrivacyItem item = mPrivacyItems.get(position);
        switch (item.getType()) {
            case SECTION:
                return TITLE;

            case ALLOW_SCREENSHOT:
            case LOCKSCREEN_ENABLE:
                return CHECKBOX;

            case SHARE_INVITATION_MODE:
                return VALUE;

            default:
                return INFO;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        int viewType = getItemViewType(position);
        UIPrivacyItem item = mPrivacyItems.get(position);

        if (viewType == INFO) {
            InformationViewHolder informationViewHolder = (InformationViewHolder) viewHolder;
            informationViewHolder.onBind(item.getText(), false);
        } else if (viewType == TITLE) {
            SectionTitleViewHolder sectionTitleViewHolder = (SectionTitleViewHolder) viewHolder;
            sectionTitleViewHolder.onBind(item.getText(), false);
        } else if (viewType == CHECKBOX) {
            SettingSwitchViewHolder settingsViewHolder = (SettingSwitchViewHolder) viewHolder;
            settingsViewHolder.itemView.setOnClickListener(v -> mPrivacyActivity.onPremiumFeatureClick());
            settingsViewHolder.onBind(item.getText(), false, false, null);
        } else if (viewType == VALUE) {
            SelectValueViewHolder selectValueViewHolder = (SelectValueViewHolder) viewHolder;
            selectValueViewHolder.itemView.setOnClickListener(v -> mPrivacyActivity.onSelectShareInvitationModeClick());

            String value = "";
            if (mPrivacyActivity.getTwinmeApplication().getShareInvitationMode() == ShareInvitationMode.NEVER) {
                value = mPrivacyActivity.getString(R.string.contact_capabilities_view_camera_control_never);
            } else if (mPrivacyActivity.getTwinmeApplication().getShareInvitationMode() == ShareInvitationMode.ASK) {
                value = mPrivacyActivity.getString(R.string.privacy_view_share_invitation_ask);
            } else if (mPrivacyActivity.getTwinmeApplication().getShareInvitationMode() == ShareInvitationMode.AUTOMATIC) {
                value = mPrivacyActivity.getString(R.string.contact_capabilities_view_camera_control_allow);
            }

            selectValueViewHolder.onBind(item.getText(), value, true, Design.WHITE_COLOR);
        }
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mPrivacyActivity.getLayoutInflater();
        View convertView;

        if (viewType == INFO) {
            convertView = inflater.inflate(R.layout.settings_room_activity_information_item, parent, false);
            return new InformationViewHolder(convertView);
        } else if (viewType == TITLE) {
            convertView = inflater.inflate(R.layout.section_title_item, parent, false);
            return new SectionTitleViewHolder(convertView);
        } else if (viewType == CHECKBOX) {
            convertView = inflater.inflate(R.layout.settings_activity_item_switch, parent, false);
            return new SettingSwitchViewHolder(convertView);
        } else {
            convertView = inflater.inflate(R.layout.select_value_item, parent, false);
            return new SelectValueViewHolder(convertView);
        }
    }

    private void loadItems() {
        if (DEBUG) {
            Log.d(LOG_TAG, "loadItems");
        }

        mPrivacyItems.clear();

        mPrivacyItems.add(new UIPrivacyItem(UIPrivacyItem.PrivacyItemType.SECTION, mPrivacyActivity.getString(R.string.settings_advanced_view_security_title)));
        mPrivacyItems.add(new UIPrivacyItem(UIPrivacyItem.PrivacyItemType.LOCKSCREEN_ENABLE, mPrivacyActivity.getString(R.string.privacy_view_lock_screen_title)));
        mPrivacyItems.add(new UIPrivacyItem(UIPrivacyItem.PrivacyItemType.INFO, mPrivacyActivity.getString(R.string.privacy_view_lock_screen_message)));

        mPrivacyItems.add(new UIPrivacyItem(UIPrivacyItem.PrivacyItemType.SECTION, mPrivacyActivity.getString(R.string.privacy_view_app_switcher)));
        mPrivacyItems.add(new UIPrivacyItem(UIPrivacyItem.PrivacyItemType.ALLOW_SCREENSHOT, mPrivacyActivity.getString(R.string.privacy_view_hide_last_screen_title)));
        mPrivacyItems.add(new UIPrivacyItem(UIPrivacyItem.PrivacyItemType.INFO, mPrivacyActivity.getString(R.string.privacy_view_hide_last_screen_message)));

        mPrivacyItems.add(new UIPrivacyItem(UIPrivacyItem.PrivacyItemType.SECTION, mPrivacyActivity.getString(R.string.privacy_view_share_invitation_title)));
        mPrivacyItems.add(new UIPrivacyItem(UIPrivacyItem.PrivacyItemType.SHARE_INVITATION_MODE, mPrivacyActivity.getString(R.string.privacy_view_share_invitation_setting)));
        mPrivacyItems.add(new UIPrivacyItem(UIPrivacyItem.PrivacyItemType.INFO, mPrivacyActivity.getString(R.string.privacy_view_share_invitation_info)));
    }
}
