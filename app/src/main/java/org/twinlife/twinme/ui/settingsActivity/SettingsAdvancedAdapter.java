/*
 *  Copyright (c) 2025-2026 twinlife SA.
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
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.libwebsockets.ErrorCategory;
import org.twinlife.device.android.twinme.BuildConfig;
import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.PeerConnectionService;
import org.twinlife.twinlife.ProxyDescriptor;
import org.twinlife.twinme.FeatureUtils;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.Settings;
import org.twinlife.twinme.ui.conversationActivity.SelectValueViewHolder;
import org.twinlife.twinme.ui.rooms.InformationViewHolder;
import org.twinlife.twinme.utils.SectionTitleViewHolder;

import java.util.ArrayList;
import java.util.List;

public class SettingsAdvancedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "SettingsAdvancedAdapter";
    private static final boolean DEBUG = false;

    @NonNull
    private final SettingsAdvancedActivity mActivity;

    private final List<UIAdvancedSettingItem> mItems = new ArrayList<>();

    private static final int TITLE = 0;
    private static final int INFO = 1;
    private static final int STATUS = 2;
    private static final int CHECKBOX_SETTINGS = 3;
    private static final int CHECKBOX_ADVANCED_SETTINGS = 4;
    private static final int SUBSECTION = 5;
    private static final int PROXY = 6;
    private static final int VALUE = 7;

    private List<ProxyDescriptor> mProxies;

    SettingsAdvancedAdapter(@NonNull SettingsAdvancedActivity listActivity, List<ProxyDescriptor> proxies) {

        mActivity = listActivity;
        mProxies = proxies;
        setHasStableIds(false);

        loadItems();
    }

    @Override
    public int getItemCount() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemCount");
        }

        return mItems.size();
    }

    public void updateProxies(List<ProxyDescriptor> proxies) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemCount");
        }

        mProxies = proxies;
        notifyItemRangeChanged(0, getItemCount());
    }

    public void updateConnexionStatus() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateConnexionStatus");
        }

        for (UIAdvancedSettingItem item : mItems) {
            if (item.getType() == UIAdvancedSettingItem.AdvancedSettingItemType.CONNEXION_STATUS) {
                notifyItemChanged(mItems.indexOf(item));
                break;
            }
        }
    }

    public void updateSecurityLevel() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateSecurityLevel");
        }

        for (UIAdvancedSettingItem item : mItems) {
            if (item.getType() == UIAdvancedSettingItem.AdvancedSettingItemType.SECURITY_LEVEL || item.getType() == UIAdvancedSettingItem.AdvancedSettingItemType.LINK_PREVIEW || item.getType() == UIAdvancedSettingItem.AdvancedSettingItemType.MAP_PREVIEW) {
                notifyItemChanged(mItems.indexOf(item));
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemViewType: " + position);
        }

        UIAdvancedSettingItem item = mItems.get(position);

        switch (item.getType()) {
            case CONNEXION_INFO:
            case PROXY_INFO:
            case TELECOM_INFO:
            case SECURITY_INFO:
            case CONVERSATION_INFO:
                return INFO;

            case CONNEXION_SECTION:
            case PROXY_SECTION:
            case TELECOM_SECTION:
            case SECURITY_SECTION:
            case CONVERSATION_SECTION:
            case DEBUG_SECTION:
                return TITLE;

            case CONNEXION_STATUS:
                return STATUS;

            case PROXY_ENABLE:
            case TELECOM_ENABLE:
                return CHECKBOX_ADVANCED_SETTINGS;

            case LINK_PREVIEW:
            case MAP_PREVIEW:
                return CHECKBOX_SETTINGS;

            case PROXY_ADD:
            case DEVELOPER_SETTINGS:
                return SUBSECTION;

            case SECURITY_LEVEL:
                return VALUE;

            default:
                return PROXY;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        int viewType = getItemViewType(position);
        UIAdvancedSettingItem item = mItems.get(position);

        if (viewType == INFO) {
            InformationViewHolder informationViewHolder = (InformationViewHolder) viewHolder;
            informationViewHolder.onBind(item.getText(), true);
        } else if (viewType == TITLE) {
            SectionTitleViewHolder sectionTitleViewHolder = (SectionTitleViewHolder) viewHolder;
            sectionTitleViewHolder.onBind(item.getText(), true);
        } else if (viewType == STATUS) {
            ConnexionStatusViewHolder connexionStatusViewHolder = (ConnexionStatusViewHolder) viewHolder;
            connexionStatusViewHolder.onBind(mActivity.getAppInfo());
        } else if (viewType == CHECKBOX_ADVANCED_SETTINGS) {
            SettingsAdvancedViewHolder settingsViewHolder = (SettingsAdvancedViewHolder) viewHolder;
            boolean isSelected;
            CompoundButton.OnCheckedChangeListener onCheckedChangeListener;
            if (item.getType() == UIAdvancedSettingItem.AdvancedSettingItemType.TELECOM_ENABLE) {
                onCheckedChangeListener = (compoundButton, value) -> mActivity.onTelecomSettingChangeValue(value);
                isSelected = mActivity.isTelecomEnable();
            } else {
                onCheckedChangeListener = (compoundButton, value) -> mActivity.onProxySettingChangeValue(value);
                isSelected = mActivity.isProxyEnable();
            }
            settingsViewHolder.onBind(item.getText(), isSelected, true, onCheckedChangeListener);
        } else if (viewType == CHECKBOX_SETTINGS) {
            SettingSwitchViewHolder settingsViewHolder = (SettingSwitchViewHolder) viewHolder;

            UISetting<Boolean> uiSetting;

            if (item.getType() == UIAdvancedSettingItem.AdvancedSettingItemType.MAP_PREVIEW) {
                uiSetting = new UISetting<>(UISetting.TypeSetting.CHECKBOX, mActivity.getString(R.string.settings_view_show_maps), mActivity.getString(R.string.settings_view_show_location_on_map), Settings.visualizationMap);
            } else {
                uiSetting = new UISetting<>(UISetting.TypeSetting.CHECKBOX, mActivity.getString(R.string.conversation_settings_view_link_title), mActivity.getString(R.string.conversation_settings_view_link_preview_message), Settings.visualizationLink);
            }

            if (PeerConnectionService.IceTransportMode.RELAY != mActivity.getTwinmeApplication().getIceTransportMode()) {
                CompoundButton.OnCheckedChangeListener onCheckedChangeListener = (buttonView, isChecked) -> mActivity.onSettingChangeValue(uiSetting, isChecked);
                settingsViewHolder.itemView.setOnClickListener(null);
                settingsViewHolder.onBind(uiSetting, uiSetting.getBoolean(), true, onCheckedChangeListener);
            } else {
                settingsViewHolder.itemView.setOnClickListener(view -> mActivity.onSettingsClick());
                settingsViewHolder.onBind(uiSetting, uiSetting.getBoolean(), false, null);
            }
        } else if (viewType == SUBSECTION) {
            SettingSectionViewHolder settingSectionViewHolder = (SettingSectionViewHolder) viewHolder;
            if (item.getType() == UIAdvancedSettingItem.AdvancedSettingItemType.PROXY_ADD) {
                settingSectionViewHolder.itemView.setOnClickListener(view -> mActivity.onAddProxyClick());
                settingSectionViewHolder.onBind(mActivity.getString(R.string.proxy_view_add), true);
            } else {
                settingSectionViewHolder.itemView.setOnClickListener(view -> mActivity.onDevelopersSettingsClick());
                settingSectionViewHolder.onBind(mActivity.getString(R.string.settings_advanced_view_developer_settings), true);
            }
            settingSectionViewHolder.onBind(item.getText(), false);
        } else if (viewType == PROXY) {
            ProxyViewHolder proxyViewHolder = (ProxyViewHolder) viewHolder;
            proxyViewHolder.itemView.setOnClickListener(view -> mActivity.onProxyClick(item.getProxyPosition()));
            ProxyDescriptor proxyDescriptor = mProxies.get(item.getProxyPosition());
            boolean hasError = proxyDescriptor.getLastError() == null || proxyDescriptor.getLastError() != ErrorCategory.ERR_NONE;
            proxyViewHolder.onBind(proxyDescriptor.getDescriptor(), hasError, false);
        } else if (viewType == VALUE) {
            SelectValueViewHolder selectValueViewHolder = (SelectValueViewHolder) viewHolder;

            String value;
            if (PeerConnectionService.IceTransportMode.ALL == mActivity.getTwinmeApplication().getIceTransportMode()) {
                value = mActivity.getString(R.string.settings_advanced_view_security_optimized);
            } else if (PeerConnectionService.IceTransportMode.TURNS == mActivity.getTwinmeApplication().getIceTransportMode()) {
                value = mActivity.getString(R.string.settings_advanced_view_security_advanced);
            } else {
                value = mActivity.getString(R.string.settings_advanced_view_security_expert);
            }

            selectValueViewHolder.itemView.setOnClickListener(view -> mActivity.onSecurityLevelClick());
            selectValueViewHolder.onBind(item.getText(), value, false, Design.WHITE_COLOR);
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

        if (viewType == INFO) {
            convertView = inflater.inflate(R.layout.settings_room_activity_information_item, parent, false);
            return new InformationViewHolder(convertView);
        } else if (viewType == TITLE) {
            convertView = inflater.inflate(R.layout.section_title_item, parent, false);
            return new SectionTitleViewHolder(convertView);
        } else if (viewType == SUBSECTION) {
            convertView = inflater.inflate(R.layout.settings_activity_item_section, parent, false);
            return new SettingSectionViewHolder(convertView);
        } else if (viewType == STATUS) {
            convertView = inflater.inflate(R.layout.connexion_status_item, parent, false);
            return new ConnexionStatusViewHolder(convertView);
        } else if (viewType == CHECKBOX_SETTINGS) {
            convertView = inflater.inflate(R.layout.settings_activity_item_switch, parent, false);
            return new SettingSwitchViewHolder(convertView);
        } else if (viewType == CHECKBOX_ADVANCED_SETTINGS) {
            convertView = inflater.inflate(R.layout.settings_advanced_item, parent, false);
            return new SettingsAdvancedViewHolder(convertView);
        } else if (viewType == VALUE) {
            convertView = inflater.inflate(R.layout.select_value_item, parent, false);
            return new SelectValueViewHolder(convertView);
        } else {
            convertView = inflater.inflate(R.layout.proxy_item, parent, false);
            return new ProxyViewHolder(convertView);
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }
    }

    private void loadItems() {
        if (DEBUG) {
            Log.d(LOG_TAG, "loadItems");
        }

        mItems.add(new UIAdvancedSettingItem(UIAdvancedSettingItem.AdvancedSettingItemType.CONNEXION_SECTION, mActivity.getString(R.string.settings_advanced_view_status_connection_title), -1));
        mItems.add(new UIAdvancedSettingItem(UIAdvancedSettingItem.AdvancedSettingItemType.CONNEXION_INFO, mActivity.getString(R.string.settings_advanced_view_status_connection_message), -1));
        mItems.add(new UIAdvancedSettingItem(UIAdvancedSettingItem.AdvancedSettingItemType.CONNEXION_STATUS, "", -1));
        mItems.add(new UIAdvancedSettingItem(UIAdvancedSettingItem.AdvancedSettingItemType.PROXY_SECTION, mActivity.getString(R.string.proxy_view_title), -1));
        mItems.add(new UIAdvancedSettingItem(UIAdvancedSettingItem.AdvancedSettingItemType.PROXY_INFO, mProxies.isEmpty() ? mActivity.getString(R.string.proxy_view_information) : mActivity.getString(R.string.proxy_view_list_information), -1));

        int proxyPosition = 0;
        for (ProxyDescriptor proxyDescriptor : mProxies) {
            mItems.add(new UIAdvancedSettingItem(UIAdvancedSettingItem.AdvancedSettingItemType.PROXY, proxyDescriptor.getDescriptor(), proxyPosition));
            proxyPosition++;
        }

        mItems.add(new UIAdvancedSettingItem(UIAdvancedSettingItem.AdvancedSettingItemType.PROXY_ADD, mActivity.getString(R.string.proxy_view_add), -1));

        if (FeatureUtils.isTelecomSupported(mActivity)) {
            mItems.add(new UIAdvancedSettingItem(UIAdvancedSettingItem.AdvancedSettingItemType.TELECOM_SECTION, mActivity.getString(R.string.settings_advanced_view_telecom), -1));
            mItems.add(new UIAdvancedSettingItem(UIAdvancedSettingItem.AdvancedSettingItemType.TELECOM_INFO, mActivity.getString(R.string.settings_advanced_view_telecom_information), -1));
            mItems.add(new UIAdvancedSettingItem(UIAdvancedSettingItem.AdvancedSettingItemType.TELECOM_ENABLE, mActivity.getString(R.string.settings_advanced_view_telecom_enable), -1));
        }

        mItems.add(new UIAdvancedSettingItem(UIAdvancedSettingItem.AdvancedSettingItemType.SECURITY_SECTION, mActivity.getString(R.string.settings_advanced_view_security_title), -1));
        mItems.add(new UIAdvancedSettingItem(UIAdvancedSettingItem.AdvancedSettingItemType.SECURITY_INFO, mActivity.getString(R.string.settings_advanced_view_security_info), -1));
        mItems.add(new UIAdvancedSettingItem(UIAdvancedSettingItem.AdvancedSettingItemType.SECURITY_LEVEL, mActivity.getString(R.string.settings_advanced_view_security_level_title), -1));

        mItems.add(new UIAdvancedSettingItem(UIAdvancedSettingItem.AdvancedSettingItemType.CONVERSATION_SECTION, mActivity.getString(R.string.conversations_view_title), -1));
        mItems.add(new UIAdvancedSettingItem(UIAdvancedSettingItem.AdvancedSettingItemType.CONVERSATION_INFO, mActivity.getString(R.string.settings_advanced_view_conversation_info), -1));
        mItems.add(new UIAdvancedSettingItem(UIAdvancedSettingItem.AdvancedSettingItemType.LINK_PREVIEW, mActivity.getString(R.string.conversation_settings_view_link_preview_message), -1));
        mItems.add(new UIAdvancedSettingItem(UIAdvancedSettingItem.AdvancedSettingItemType.MAP_PREVIEW, mActivity.getString(R.string.settings_view_show_maps), -1));

        if (BuildConfig.DEBUG) {
            mItems.add(new UIAdvancedSettingItem(UIAdvancedSettingItem.AdvancedSettingItemType.DEBUG_SECTION, mActivity.getString(R.string.settings_advanced_view_debug), -1));
            mItems.add(new UIAdvancedSettingItem(UIAdvancedSettingItem.AdvancedSettingItemType.DEVELOPER_SETTINGS, mActivity.getString(R.string.settings_advanced_view_developer_settings), -1));
        }
    }
}