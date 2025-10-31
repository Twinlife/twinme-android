/*
 *  Copyright (c) 2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.settingsActivity;

import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.AndroidDeviceInfo;
import org.twinlife.twinme.ui.TwinmeActivity;

import java.util.List;

public class QualityOfServiceAdapter extends RecyclerView.Adapter<QualityOfServiceViewHolder> {

    private static final String LOG_TAG = "QualityOfServiceAdapter";
    private static final boolean DEBUG = false;

    private final QualityOfServiceActivity mQualityOfServiceActivity;

    private final List<UIQuality> mUIQuality;

    QualityOfServiceAdapter(QualityOfServiceActivity qualityOfServiceActivity, List<UIQuality> uiQuality) {

        mQualityOfServiceActivity = qualityOfServiceActivity;
        mUIQuality = uiQuality;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public QualityOfServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mQualityOfServiceActivity.getLayoutInflater();

        View convertView = inflater.inflate(R.layout.quality_of_services_activity_item, parent, false);
        return new QualityOfServiceViewHolder(mQualityOfServiceActivity, convertView);
    }

    @Override
    public void onBindViewHolder(@NonNull QualityOfServiceViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        final AndroidDeviceInfo androidDeviceInfo = new AndroidDeviceInfo(mQualityOfServiceActivity);

        boolean postNotificationEnable = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            postNotificationEnable = mQualityOfServiceActivity.checkPermissionsWithoutRequest(new TwinmeActivity.Permission[]{TwinmeActivity.Permission.POST_NOTIFICATIONS});
        }

        final boolean notificationDisabled = !NotificationManagerCompat.from(mQualityOfServiceActivity).areNotificationsEnabled() || !postNotificationEnable;
        final boolean backgroundRestricted = androidDeviceInfo.isBackgroundRestricted();
        final boolean networkRestricted = androidDeviceInfo.isNetworkRestricted();
        final boolean lowUsage = !backgroundRestricted && androidDeviceInfo.isIgnoringBatteryOptimizations() && androidDeviceInfo.getAppStandbyBucket() > 30;

        boolean isRestriction = false;
        if (backgroundRestricted || lowUsage || notificationDisabled || networkRestricted) {
            isRestriction = true;
        }

        UIQuality uiQuality = mUIQuality.get(position);
        boolean hideAction = uiQuality.hideAction() || !isRestriction;

        viewHolder.onBind(uiQuality, hideAction);
    }

    @Override
    public int getItemCount() {

        return mUIQuality.size();
    }

    @Override
    public void onViewRecycled(@NonNull QualityOfServiceViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }
    }
}