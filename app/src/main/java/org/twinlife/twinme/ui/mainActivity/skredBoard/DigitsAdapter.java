/*
 *  Copyright (c) 2017-2022 twinlife SA & Telefun SAS.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Thibaud David (contact@thibauddavid.com)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui.mainActivity.skredBoard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

class DigitsAdapter extends RecyclerView.Adapter<DigitsAdapter.DigitViewHolder> {

    private static final int[] BACKGROUND_IDS = {
            R.drawable.digit_background_0,
            R.drawable.digit_background_1,
            R.drawable.digit_background_2,
            R.drawable.digit_background_3,
            R.drawable.digit_background_4,
            R.drawable.digit_background_5,
            R.drawable.digit_background_6,
            R.drawable.digit_background_7,
            R.drawable.digit_background_8,
            R.drawable.digit_background_9
    };

    private static final int[] BLANK_BACKGROUND_IDS = {
            R.drawable.digit_blank_background_0,
            R.drawable.digit_blank_background_1,
            R.drawable.digit_blank_background_2,
            R.drawable.digit_blank_background_3,
            R.drawable.digit_blank_background_4,
            R.drawable.digit_blank_background_5,
            R.drawable.digit_blank_background_6,
            R.drawable.digit_blank_background_7,
            R.drawable.digit_blank_background_8,
            R.drawable.digit_blank_background_9
    };

    private final int resId;
    @NonNull
    private final View parent;
    private DigitClickListener digitClickListener;
    private String code;

    @SuppressWarnings("SameParameterValue")
    DigitsAdapter(@LayoutRes int resource, @NonNull View parent) {

        this.code = "";
        this.resId = resource;
        this.parent = parent;
    }

    void setDigitClickListener(DigitClickListener digitClickListener) {

        this.digitClickListener = digitClickListener;
    }

    @Override
    @NonNull
    public DigitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        final View convertView = LayoutInflater.from(parent.getContext()).inflate(this.resId, parent, false);
        convertView.setOnClickListener(view -> digitClickListener.onDigitClicked((int) convertView.getTag()));

        return new DigitViewHolder(convertView);
    }

    @SuppressLint("NewApi")
    @Override
    public void onBindViewHolder(@NonNull DigitViewHolder holder, int position) {

        holder.itemView.setTag(position);

        RecyclerView.LayoutParams itemLayoutParam = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
        itemLayoutParam.width = (int) ((float) Design.DISPLAY_WIDTH / 5f);
        itemLayoutParam.height = (int) ((float) itemLayoutParam.width * 0.72f);

        TextView digitTextView = holder.itemView.findViewById(R.id.digitTextView);
        digitTextView.setTextColor(Color.WHITE);
        digitTextView.setText(String.valueOf(position));

        Context context = parent.getContext();
        int id;
        if (code.contains(String.valueOf(position))) {
            id = BACKGROUND_IDS[position];
        } else {
            id = BLANK_BACKGROUND_IDS[position];
        }

        digitTextView.setBackground(ResourcesCompat.getDrawable(context.getResources(), id, null));
    }

    @Override
    public int getItemCount() {

        return 10;
    }

    void codeDidUpdate(String code) {

        this.code = code;
        notifyDataSetChanged();
    }

    static class DigitViewHolder extends RecyclerView.ViewHolder {

        DigitViewHolder(View itemView) {

            super(itemView);
        }
    }
}
