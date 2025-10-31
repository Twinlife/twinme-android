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

class DigitsAdapter extends RecyclerView.Adapter {

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
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        final View convertView = LayoutInflater.from(parent.getContext()).inflate(this.resId, parent, false);
        convertView.setOnClickListener(view -> digitClickListener.onDigitClicked((int) convertView.getTag()));

        return new DigitViewHolder(convertView);
    }

    @SuppressLint("NewApi")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

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
            id = context.getResources().getIdentifier("digit_background_" + position, "drawable", context.getPackageName());
        } else {
            id = context.getResources().getIdentifier("digit_blank_background_" + position, "drawable", context.getPackageName());
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

    private static class DigitViewHolder extends RecyclerView.ViewHolder {

        DigitViewHolder(View itemView) {

            super(itemView);
        }
    }
}
