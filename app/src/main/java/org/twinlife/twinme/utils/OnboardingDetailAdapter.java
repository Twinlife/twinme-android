/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.utils;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.ui.premiumServicesActivity.UIPremiumFeature;

public class OnboardingDetailAdapter extends RecyclerView.Adapter<OnboardingDetailViewHolder> {
    private static final String LOG_TAG = "OnboardingDetailAdapter";
    private static final boolean DEBUG = false;

    private final UIPremiumFeature mPremiumFeature;

    private final OnboardingDetailView mOnboardingDetailView;

    OnboardingDetailAdapter(OnboardingDetailView onboardingDetailView, @NonNull UIPremiumFeature premiumFeature) {

        mOnboardingDetailView = onboardingDetailView;
        mPremiumFeature = premiumFeature;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public OnboardingDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = (LayoutInflater) mOnboardingDetailView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View convertView = inflater.inflate(R.layout.onboarding_detail_view_item, parent, false);

        return new OnboardingDetailViewHolder(convertView);
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingDetailViewHolder viewHolder, int position) {

        viewHolder.onBind(mPremiumFeature.getFeatureDetails().get(position), false);
    }

    @Override
    public int getItemCount() {

        return mPremiumFeature.getFeatureDetails().size();
    }
}