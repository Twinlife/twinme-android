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
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.libwebsockets.ErrorCategory;
import org.twinlife.device.android.twinme.BuildConfig;
import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ProxyDescriptor;
import org.twinlife.twinme.FeatureUtils;
import org.twinlife.twinme.ui.rooms.InformationViewHolder;
import org.twinlife.twinme.utils.SectionTitleViewHolder;

import java.util.List;

public class SettingsAdvancedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "SettingsAdvancedAdapter";
    private static final boolean DEBUG = false;

    @NonNull
    private final SettingsAdvancedActivity mActivity;

    private int ITEM_COUNT = 7;

    private static final int SECTION_CONNEXION = 0;
    private static final int SECTION_PROXY = 3;
    private int SECTION_TELECOM = -1;
    private int SECTION_DEBUG = -1;
    private static final int POSITION_CONNEXION_INFO = 1;
    private static final int POSITION_CONNEXION_STATUS = 2;
    private static final int POSITION_PROXY_INFO = 4;
    private static final int POSITION_PROXY_ENABLE = 5;
    private static int POSITION_PROXY_ADD = 6;

    private int POSITION_TELECOM_INFO = -1;
    private int POSITION_TELECOM_ENABLE = -1;

    private int POSITION_DEVELOPER_SETTINGS = -1;

    private static final int TITLE = 0;
    private static final int INFO = 1;
    private static final int STATUS = 2;
    private static final int CHECKBOX = 3;
    private static final int SUBSECTION = 4;
    private static final int PROXY = 5;

    private List<ProxyDescriptor> mProxies;

    SettingsAdvancedAdapter(@NonNull SettingsAdvancedActivity listActivity, List<ProxyDescriptor> proxies) {

        mActivity = listActivity;
        mProxies = proxies;
        setHasStableIds(false);

        if (BuildConfig.DEBUG) {
            ITEM_COUNT = 9;
        }

        if (FeatureUtils.isTelecomSupported(mActivity)) {
            ITEM_COUNT += 3;
            SECTION_TELECOM = 7;
            POSITION_TELECOM_INFO = 8;
            POSITION_TELECOM_ENABLE = 9;
        }
    }

    @Override
    public int getItemCount() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemCount");
        }

        int count = ITEM_COUNT + mProxies.size();
        if (BuildConfig.DEBUG) {
            SECTION_DEBUG = count - 2;
            POSITION_DEVELOPER_SETTINGS = count - 1;
        } else {
            SECTION_DEBUG = -1;
            POSITION_DEVELOPER_SETTINGS = -1;
        }

        POSITION_PROXY_ADD = POSITION_PROXY_ENABLE + mProxies.size() + 1;

        if (SECTION_TELECOM != -1) {
            SECTION_TELECOM = POSITION_PROXY_ADD + 1;
            POSITION_TELECOM_INFO = SECTION_TELECOM + 1;
            POSITION_TELECOM_ENABLE = POSITION_TELECOM_INFO + 1;
        }

        return count;
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

        notifyItemChanged(POSITION_CONNEXION_STATUS);
    }

    @Override
    public int getItemViewType(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemViewType: " + position);
        }

        if (position == POSITION_CONNEXION_INFO || position == POSITION_PROXY_INFO || position == POSITION_TELECOM_INFO) {
            return INFO;
        } else if (position == SECTION_CONNEXION || position == SECTION_PROXY || position == SECTION_TELECOM || position == SECTION_DEBUG) {
            return TITLE;
        } else if (position == POSITION_CONNEXION_STATUS) {
            return STATUS;
        } else if (position == POSITION_PROXY_ENABLE ||position == POSITION_TELECOM_ENABLE) {
            return CHECKBOX;
        } else if (position == POSITION_PROXY_ADD || position == POSITION_DEVELOPER_SETTINGS) {
            return SUBSECTION;
        } else {
            return PROXY;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        int viewType = getItemViewType(position);

        if (viewType == INFO) {
            InformationViewHolder informationViewHolder = (InformationViewHolder) viewHolder;
            if (position == POSITION_CONNEXION_INFO) {
                informationViewHolder.onBind(mActivity.getString(R.string.settings_advanced_activity_status_connection_message), true);
            } else if (position == POSITION_PROXY_INFO) {
                if (mProxies.isEmpty()) {
                    informationViewHolder.onBind(mActivity.getString(R.string.proxy_activity_information), true);
                } else {
                    informationViewHolder.onBind(mActivity.getString(R.string.proxy_activity_list_information), true);
                }
            }  else if (position == POSITION_TELECOM_INFO) {
                informationViewHolder.onBind(mActivity.getString(R.string.settings_advanced_activity_telecom_information), true);
            }
        } else if (viewType == TITLE) {
            SectionTitleViewHolder sectionTitleViewHolder = (SectionTitleViewHolder) viewHolder;
            String title = getSectionTitle(position);
            sectionTitleViewHolder.onBind(title, true);
        } else if (viewType == STATUS) {
            ConnexionStatusViewHolder connexionStatusViewHolder = (ConnexionStatusViewHolder) viewHolder;
            connexionStatusViewHolder.onBind(mActivity.getAppInfo());
        } else if (viewType == CHECKBOX) {
            SettingsAdvancedViewHolder settingsViewHolder = (SettingsAdvancedViewHolder) viewHolder;

            if (position == POSITION_TELECOM_ENABLE) {
                CompoundButton.OnCheckedChangeListener onCheckedChangeListener = (compoundButton, value) -> mActivity.onTelecomSettingChangeValue(value);
                settingsViewHolder.onBind(mActivity.getString(R.string.settings_advanced_activity_telecom_enable), mActivity.isTelecomEnable(), true, onCheckedChangeListener);
            } else {
                CompoundButton.OnCheckedChangeListener onCheckedChangeListener = (compoundButton, value) -> mActivity.onProxySettingChangeValue(value);
                settingsViewHolder.onBind(mActivity.getString(R.string.proxy_activity_enable), mActivity.isProxyEnable(), !mProxies.isEmpty(), onCheckedChangeListener);
            }
        } else if (viewType == SUBSECTION) {
            SettingSectionViewHolder settingSectionViewHolder = (SettingSectionViewHolder) viewHolder;
            if (position == POSITION_PROXY_ADD) {
                settingSectionViewHolder.itemView.setOnClickListener(view -> mActivity.onAddProxyClick());
                settingSectionViewHolder.onBind(mActivity.getString(R.string.proxy_activity_add), true);
            } else {
                settingSectionViewHolder.itemView.setOnClickListener(view -> mActivity.onDevelopersSettingsClick());
                settingSectionViewHolder.onBind(mActivity.getString(R.string.settings_advanced_activity_developer_settings), true);
            }
        } else if (viewType == PROXY) {
            ProxyViewHolder proxyViewHolder = (ProxyViewHolder) viewHolder;
            int proxyPosition = position - POSITION_PROXY_ENABLE - 1;
            proxyViewHolder.itemView.setOnClickListener(view -> mActivity.onProxyClick(proxyPosition));
            ProxyDescriptor proxyDescriptor = mProxies.get(proxyPosition);
            boolean hasError = proxyDescriptor.getLastError() == null || proxyDescriptor.getLastError() != ErrorCategory.ERR_NONE;
            proxyViewHolder.onBind(proxyDescriptor.getDescriptor(), hasError, false);
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
        } else if (viewType == CHECKBOX) {
            convertView = inflater.inflate(R.layout.settings_advanced_item, parent, false);
            return new SettingsAdvancedViewHolder(convertView);
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

    private String getSectionTitle(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getSectionTitle: " + position);
        }

        if (position == SECTION_CONNEXION) {
            return mActivity.getString(R.string.settings_advanced_activity_status_connection_title);
        } else if (position == SECTION_PROXY) {
            return mActivity.getString(R.string.proxy_activity_title);
        } else if (position == SECTION_TELECOM) {
            return mActivity.getString(R.string.settings_advanced_activity_telecom);
        } else if (position == SECTION_DEBUG) {
            return mActivity.getString(R.string.settings_advanced_activity_debug);
        }

        return "";
    }
}