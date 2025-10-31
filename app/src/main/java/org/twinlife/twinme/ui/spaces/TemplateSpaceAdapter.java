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

import java.util.List;

public class TemplateSpaceAdapter extends RecyclerView.Adapter<TemplateSpaceViewHolder> {
    private static final String LOG_TAG = "TemplateSpaceAdapter";
    private static final boolean DEBUG = false;

    @NonNull
    private final OnTemplateSpaceClickListener mOnTemplateSpaceClickListener;

    public interface OnTemplateSpaceClickListener {
        void onTemplateSpaceClick(int position);
    }

    private final TemplateSpaceActivity mTemplateActivity;
    private final List<UITemplateSpace> mUITemplateSpace;

    TemplateSpaceAdapter(TemplateSpaceActivity activity, List<UITemplateSpace> templates, @NonNull OnTemplateSpaceClickListener onTemplateSpaceClickListener) {

        mTemplateActivity = activity;
        mUITemplateSpace = templates;
        mOnTemplateSpaceClickListener = onTemplateSpaceClickListener;

        setHasStableIds(true);
    }

    @NonNull
    @Override
    public TemplateSpaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mTemplateActivity.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.template_space_activity_item, parent, false);

        TemplateSpaceViewHolder templateSpaceViewHolder = new TemplateSpaceViewHolder(convertView);

        convertView.setOnClickListener(v -> {
            int position = templateSpaceViewHolder.getBindingAdapterPosition();
            if (position >= 0) {
                mOnTemplateSpaceClickListener.onTemplateSpaceClick(position);
            }
        });

        return templateSpaceViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull TemplateSpaceViewHolder viewHolder, int position) {

        boolean hideSeparator = position + 1 == mUITemplateSpace.size();
        viewHolder.onBind(mUITemplateSpace.get(position), hideSeparator);
    }

    @Override
    public int getItemCount() {

        return mUITemplateSpace.size();
    }

    @Override
    public long getItemId(int position) {

        return mUITemplateSpace.get(position).getItemId();
    }
}
