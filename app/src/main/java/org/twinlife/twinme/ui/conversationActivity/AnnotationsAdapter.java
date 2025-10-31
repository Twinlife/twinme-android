/*
 *  Copyright (c) 2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.conversationActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.baseItemActivity.BaseItemActivity;

import java.util.List;

public class AnnotationsAdapter extends RecyclerView.Adapter<AnnotationInfoViewHolder> {
    private static final String LOG_TAG = "AnnotationsAdapter";
    private static final boolean DEBUG = false;

    private final BaseItemActivity mActivity;

    @NonNull
    private List<UIAnnotation> mUIAnnotations;

    AnnotationsAdapter(BaseItemActivity activity, @NonNull List<UIAnnotation> annotations) {

        mActivity = activity;
        mUIAnnotations = annotations;
        setHasStableIds(true);
    }

    public void setAnnotations(List<UIAnnotation>annotations) {

        mUIAnnotations = annotations;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AnnotationInfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mActivity.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.annotation_info_item, parent, false);

        return new AnnotationInfoViewHolder(convertView);
    }

    @Override
    public void onBindViewHolder(@NonNull AnnotationInfoViewHolder viewHolder, int position) {

        UIAnnotation uiAnnotation = mUIAnnotations.get(position);
        boolean hideSeparator = position + 1 == mUIAnnotations.size();
        viewHolder.onBind(mActivity, uiAnnotation, Design.POPUP_BACKGROUND_COLOR, hideSeparator);
    }

    @Override
    public int getItemCount() {

        return mUIAnnotations.size();
    }

    @Override
    public long getItemId(int position) {

        return mUIAnnotations.get(position).getItemId();
    }
}
