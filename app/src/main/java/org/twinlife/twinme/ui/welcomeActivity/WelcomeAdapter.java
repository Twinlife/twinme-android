/*
 *  Copyright (c) 2019-2020 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.welcomeActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;

import java.util.List;

public class WelcomeAdapter extends RecyclerView.Adapter<WelcomeViewHolder> {

    private static final String LOG_TAG = "WelcomeAdapter";
    private static final boolean DEBUG = false;

    private final AbstractTwinmeActivity mWelcomeActivity;
    private final List<UIWelcome> mUIWelcomes;

    WelcomeAdapter(AbstractTwinmeActivity welcomeActivity, List<UIWelcome> uiWelcomes) {

        mWelcomeActivity = welcomeActivity;
        mUIWelcomes = uiWelcomes;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public WelcomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mWelcomeActivity.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.welcome_activity_item, parent, false);
        return new WelcomeViewHolder(convertView);
    }

    @Override
    public void onBindViewHolder(@NonNull WelcomeViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        viewHolder.onBind(mWelcomeActivity, mUIWelcomes.get(position));
    }

    @Override
    public int getItemCount() {

        return mUIWelcomes.size();
    }

    @Override
    public long getItemId(int position) {

        return mUIWelcomes.get(position).getItemId();
    }

    @Override
    public void onViewRecycled(@NonNull WelcomeViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }

        viewHolder.onViewRecycled();
    }
}
