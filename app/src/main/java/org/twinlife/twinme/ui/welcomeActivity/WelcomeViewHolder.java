/*
 *  Copyright (c) 2019-2020 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.welcomeActivity;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class WelcomeViewHolder extends RecyclerView.ViewHolder {
    private static final String LOG_TAG = "WelcomeViewHolder";
    private static final boolean DEBUG = false;

    private final ImageView mImageView;
    private final TextView mTextView;

    WelcomeViewHolder(@NonNull View view) {

        super(view);

        mTextView = view.findViewById(R.id.welcome_activty_item_text_view);
        Design.updateTextFont(mTextView, Design.FONT_MEDIUM34);
        mTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mImageView = view.findViewById(R.id.welcome_activty_item_image_view);
    }

    public void onBind(Context context, UIWelcome uiWelcome) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBind: welcome=" + uiWelcome);
        }

        mTextView.setText(uiWelcome.getText());
        mImageView.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), uiWelcome.getImageId(), null));
    }

    public void onViewRecycled() {

    }
}
