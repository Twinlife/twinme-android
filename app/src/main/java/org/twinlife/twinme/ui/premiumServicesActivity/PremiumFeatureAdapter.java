/*
 *  Copyright (c) 2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.premiumServicesActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;

import java.util.List;

public class PremiumFeatureAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String LOG_TAG = "PremiumFeatureAdapter";
    private static final boolean DEBUG = false;

    private final AbstractTwinmeActivity mPremiumActivity;
    private final List<UIPremiumFeature> mUIPremiumFeatures;

    private static final int FEATURE = 0;
    private static final int FOOTER = 1;

    PremiumFeatureAdapter(AbstractTwinmeActivity premiumActivity, List<UIPremiumFeature> uiPremiumFeatures) {

        mPremiumActivity = premiumActivity;
        mUIPremiumFeatures = uiPremiumFeatures;
        setHasStableIds(false);
    }

    @Override
    public int getItemViewType(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemViewType: " + position);
        }

        if (position < mUIPremiumFeatures.size()) {
            return FEATURE;
        }

        return FOOTER;
    }

        @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        if (viewType == FEATURE) {
            LayoutInflater inflater = mPremiumActivity.getLayoutInflater();
            View convertView = inflater.inflate(R.layout.premium_services_activity_item, parent, false);
            boolean showBorder = true;
            if (mUIPremiumFeatures.size() == 1) {
                showBorder = false;
            }
            return new PremiumFeatureViewHolder(convertView, showBorder);
        } else {
            LayoutInflater inflater = mPremiumActivity.getLayoutInflater();
            View convertView = inflater.inflate(R.layout.premium_services_activity_footer_item, parent, false);
            return new PremiumFeatureFooterViewHolder(convertView);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        int viewType = getItemViewType(position);

        if (viewType == FEATURE) {
            PremiumFeatureViewHolder premiumFeatureViewHolder = (PremiumFeatureViewHolder) viewHolder;
            UIPremiumFeature premiumFeature = mUIPremiumFeatures.get(position);
            premiumFeatureViewHolder.onBind(mPremiumActivity, premiumFeature);
        }
    }

    @Override
    public int getItemCount() {

        if (mUIPremiumFeatures.size() == 1) {
            return 1;
        }

        return mUIPremiumFeatures.size() + 1;
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }

        int position = viewHolder.getBindingAdapterPosition();
        int viewType = getItemViewType(position);

        if (viewType == FEATURE && position != -1) {
            PremiumFeatureViewHolder premiumFeatureViewHolder = (PremiumFeatureViewHolder) viewHolder;
            UIPremiumFeature premiumFeature = mUIPremiumFeatures.get(position);
            premiumFeatureViewHolder.onBind(mPremiumActivity, premiumFeature);
        }
    }
}