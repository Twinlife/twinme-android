/*
 *  Copyright (c) 2020 twinlife SA.
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
import androidx.recyclerview.widget.RecyclerView.Adapter;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;

import java.util.List;

public class ColorSpaceAdapter extends Adapter<ColorSpaceViewHolder> {
    private static final String LOG_TAG = "ColorSpaceAdapter";
    private static final boolean DEBUG = false;

    public interface OnColorClickListener {

        void onUpdateColor(UIColorSpace color);

        void onEnterCustomColor();
    }

    private final AbstractTwinmeActivity mListActivity;
    private final List<UIColorSpace> mUIColor;
    private final int mColorWidth;
    private final OnColorClickListener mOnColorClickListener;

    private String mDefaultColor;
    private String mSelectedColor;
    private boolean mEnterColorEnable;

    ColorSpaceAdapter(AbstractTwinmeActivity listActivity, List<UIColorSpace> colors, OnColorClickListener onColorClickListener, int colorWidth) {
        mListActivity = listActivity;
        mUIColor = colors;
        mOnColorClickListener = onColorClickListener;
        mColorWidth = colorWidth;
        setHasStableIds(true);
    }

    public void setDefaultColor(String color) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setDefaultColor: color=" + color);
        }

        mDefaultColor = color;
    }

    public void setSelectedColor(String color) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setSelectedColor: color=" + color);
        }

        mSelectedColor = color;
        notifyDataSetChanged();
    }

    public void setEnterColorEnable(boolean enterColorEnable) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setEnterColorEnable: " + enterColorEnable);
        }

        mEnterColorEnable = enterColorEnable;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ColorSpaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mListActivity.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.create_space_activity_color_item, parent, false);
        return new ColorSpaceViewHolder(convertView, mColorWidth);
    }

    @Override
    public void onBindViewHolder(@NonNull ColorSpaceViewHolder viewHolder, int position) {

        if (position == mUIColor.size()) {
            viewHolder.onBindEditStyle(mEnterColorEnable);
            viewHolder.itemView.setOnClickListener(view -> mOnColorClickListener.onEnterCustomColor());
        } else {
            UIColorSpace customColor = mUIColor.get(position);
            boolean isSelected = false;

            if (!mEnterColorEnable) {
                if (customColor.getStringColor() != null) {
                    isSelected = customColor.getStringColor().equals(mSelectedColor);
                } else if (mSelectedColor != null) {
                    isSelected = mSelectedColor.equals(mDefaultColor);
                }
            }

            customColor.setSelected(isSelected);
            viewHolder.onBind(customColor);

            if (mOnColorClickListener != null) {
                viewHolder.itemView.setOnClickListener(view -> mOnColorClickListener.onUpdateColor(customColor));
            }
        }
    }

    @Override
    public int getItemCount() {

        return mUIColor.size() + 1;
    }
}
