/*
 *  Copyright (c) 2023-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.externalCallActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.ui.welcomeActivity.WelcomeViewHolder;

import java.util.List;

public class OnboardingExternalCallAdapter extends RecyclerView.Adapter<OnboardingExternalCallViewHolder> {

    private static final String LOG_TAG = "WelcomeAdapter";
    private static final boolean DEBUG = false;

    private final OnboardingExternalCallActivity mExternalCallActivity;
    private final List<UIOnboarding> mUIOnboarding;

    OnboardingExternalCallAdapter(OnboardingExternalCallActivity onboardingExternalCallActivity, List<UIOnboarding> uiOnboardings) {

        mExternalCallActivity = onboardingExternalCallActivity;
        mUIOnboarding = uiOnboardings;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public OnboardingExternalCallViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mExternalCallActivity.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.onboarding_external_call_activity_item, parent, false);
        return new OnboardingExternalCallViewHolder(mExternalCallActivity, convertView);
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingExternalCallViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        viewHolder.onBind(mUIOnboarding.get(position), mExternalCallActivity.isFromSideMenu());
    }

    @Override
    public int getItemCount() {

        return mUIOnboarding.size();
    }

    @Override
    public long getItemId(int position) {

        return mUIOnboarding.get(position).getItemId();
    }

    public void onViewRecycled(@NonNull WelcomeViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }

        viewHolder.onViewRecycled();
    }
}