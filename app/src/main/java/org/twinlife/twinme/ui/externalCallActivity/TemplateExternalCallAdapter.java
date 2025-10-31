/*
 *  Copyright (c) 2023 twinlife SA.
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

import java.util.List;

public class TemplateExternalCallAdapter extends RecyclerView.Adapter<TemplateExternalCallViewHolder> {
    private static final String LOG_TAG = "TemplateExternalCa...";
    private static final boolean DEBUG = false;

    @NonNull
    private final OnTemplateExternalCallClickListener mOnTemplateExternalCallClickListener;

    public interface OnTemplateExternalCallClickListener {
        void onTemplateClick(int position);
    }

    private final TemplateExternalCallActivity mTemplateActivity;
    private final List<UITemplateExternalCall> mUITemplateExternalCall;

    TemplateExternalCallAdapter(TemplateExternalCallActivity activity, List<UITemplateExternalCall> templates, @NonNull OnTemplateExternalCallClickListener onTemplateExternalCallClickListener) {

        mTemplateActivity = activity;
        mUITemplateExternalCall = templates;
        mOnTemplateExternalCallClickListener = onTemplateExternalCallClickListener;

        setHasStableIds(true);
    }

    @NonNull
    @Override
    public TemplateExternalCallViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mTemplateActivity.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.template_external_call_activity_item, parent, false);

        TemplateExternalCallViewHolder templateExternalCallViewHolder = new TemplateExternalCallViewHolder(convertView);

        convertView.setOnClickListener(v -> {
            int position = templateExternalCallViewHolder.getBindingAdapterPosition();
            if (position >= 0) {
                mOnTemplateExternalCallClickListener.onTemplateClick(position);
            }
        });

        return templateExternalCallViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull TemplateExternalCallViewHolder viewHolder, int position) {

        boolean hideSeparator = position + 1 == mUITemplateExternalCall.size();
        viewHolder.onBind(mUITemplateExternalCall.get(position), hideSeparator);
    }

    @Override
    public int getItemCount() {

        return mUITemplateExternalCall.size();
    }

    @Override
    public long getItemId(int position) {

        return mUITemplateExternalCall.get(position).getItemId();
    }
}