/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.utils;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.res.ResourcesCompat;
import androidx.percentlayout.widget.PercentRelativeLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.premiumServicesActivity.UIPremiumFeature;

public class OnboardingDetailView extends OnboardingConfirmView {
    private static final String LOG_TAG = "OnboardingDetailView";
    private static final boolean DEBUG = false;

    private static final int DESIGN_LIST_HORIZONTAL_MARGIN = 12;

    public OnboardingDetailView(Context context) {
        super(context);
    }

    public OnboardingDetailView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (DEBUG) {
            Log.d(LOG_TAG, "create");
        }

        try {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            mRootView = inflater.inflate(R.layout.onboarding_detail_view, null);
            addView(mRootView);

            initViews();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setPremiumFeature(UIPremiumFeature premiumFeature) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setPremiumFeature");
        }

        setTitle(premiumFeature.getTitle());
        setImage(ResourcesCompat.getDrawable(getResources(),  premiumFeature.getImageId(), null));
        setMessage(premiumFeature.getSubTitle());

        OnboardingDetailAdapter onboardingDetailAdapter = new OnboardingDetailAdapter(this, premiumFeature);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        RecyclerView recyclerView = getRootView().findViewById(R.id.onboarding_confirm_view_list_view);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(onboardingDetailAdapter);

        MarginLayoutParams marginLayoutParams = (MarginLayoutParams) recyclerView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_LIST_HORIZONTAL_MARGIN * Design.HEIGHT_RATIO);
    }

    @Override
    protected void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        super.initViews();
    }
}