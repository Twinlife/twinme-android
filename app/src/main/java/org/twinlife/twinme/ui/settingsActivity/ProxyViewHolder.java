/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.settingsActivity;

import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class ProxyViewHolder extends RecyclerView.ViewHolder {

    private final TextView mProxyView;
    private final ImageView mErrorImageView;
    private final View mSeparatorView;

    public ProxyViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = Design.SECTION_HEIGHT;
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Design.WHITE_COLOR);

        mProxyView = view.findViewById(R.id.proxy_item_name_view);
        mProxyView.setTypeface(Design.FONT_REGULAR32.typeface);
        mProxyView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_REGULAR32.size);
        mProxyView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mErrorImageView = view.findViewById(R.id.proxy_item_error_view);

        mSeparatorView  = view.findViewById(R.id.proxy_item_separator_view);
        mSeparatorView.setBackgroundColor(Design.SEPARATOR_COLOR);
    }

    public void onBind(String proxy, boolean hasError, boolean hideSeparator) {

        mProxyView.setText(proxy);

        if (!hasError) {
            mErrorImageView.setVisibility(View.VISIBLE);
        } else {
            mErrorImageView.setVisibility(View.INVISIBLE);
        }

        if (hideSeparator) {
            mSeparatorView.setVisibility(View.GONE);
        } else {
            mSeparatorView.setVisibility(View.VISIBLE);
        }

        updateFont();
        updateColor();
    }

    private void updateFont() {

        mProxyView.setTypeface(Design.FONT_REGULAR32.typeface);
        mProxyView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_REGULAR32.size);
    }

    private void updateColor() {

        mProxyView.setTextColor(Design.FONT_COLOR_DEFAULT);
        itemView.setBackgroundColor(Design.WHITE_COLOR);
    }
}