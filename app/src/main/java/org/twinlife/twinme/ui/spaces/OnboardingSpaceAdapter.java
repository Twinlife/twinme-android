/*
 *  Copyright (c) 2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.spaces;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;

public class OnboardingSpaceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String LOG_TAG = "OnboardingSpaceAdapter";
    private static final boolean DEBUG = false;

    private static final int ONBOARDING_FIRST_PART = 0;
    private static final int ONBOARDING_SECOND_PART = 1;

    private final boolean mShowFirstPart;

    private final OnboardingSpaceActivity mOnboardingSpaceActivity;

    OnboardingSpaceAdapter(OnboardingSpaceActivity onboardingSpaceActivity, boolean showFirstPart) {

        mOnboardingSpaceActivity = onboardingSpaceActivity;
        mShowFirstPart = showFirstPart;
        setHasStableIds(true);
    }

    @Override
    public int getItemViewType(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemViewType: " + position);
        }

        if (mShowFirstPart) {
            return position;
        } else {
            return position + 1;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mOnboardingSpaceActivity.getLayoutInflater();

        if (viewType == ONBOARDING_FIRST_PART) {
            View convertView = inflater.inflate(R.layout.onboarding_space_activity_first_part_item, parent, false);
            return new OnboardingSpaceFirstPartViewHolder(mOnboardingSpaceActivity, convertView);
        } else if (viewType == ONBOARDING_SECOND_PART) {
            View convertView = inflater.inflate(R.layout.onboarding_space_activity_second_part_item, parent, false);
            return new OnboardingSpaceSecondPartViewHolder(convertView);
        } else {
            View convertView = inflater.inflate(R.layout.onboarding_space_activity_third_part_item, parent, false);
            return new OnboardingSpaceThirdPartViewHolder(mOnboardingSpaceActivity, convertView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        if (!mShowFirstPart) {
            position += 1;
        }

        if (position == 0) {
            OnboardingSpaceFirstPartViewHolder onboardingSpaceFirstPartViewHolder = (OnboardingSpaceFirstPartViewHolder) viewHolder;
            onboardingSpaceFirstPartViewHolder.onBind(mOnboardingSpaceActivity);
        } else if (position == 1) {
            OnboardingSpaceSecondPartViewHolder onboardingSpaceSecondPartViewHolder = (OnboardingSpaceSecondPartViewHolder) viewHolder;
            onboardingSpaceSecondPartViewHolder.onBind(mOnboardingSpaceActivity);
        } else {
            OnboardingSpaceThirdPartViewHolder onboardingSpaceThirdPartViewHolder = (OnboardingSpaceThirdPartViewHolder) viewHolder;
            onboardingSpaceThirdPartViewHolder.onBind(mOnboardingSpaceActivity, mOnboardingSpaceActivity.isFromSideMenu());
        }
    }

    @Override
    public int getItemCount() {

        if (mShowFirstPart) {
            return 3;
        }
        return 2;
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }
    }
}
