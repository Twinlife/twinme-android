/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.backupActivity;

import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class RestoreStateViewHolder extends RecyclerView.ViewHolder {
    private static final String LOG_TAG = "RestoreStateViewHolder";
    private static final boolean DEBUG = false;

    private static final float DESIGN_PROGRESS_MARGIN = 20;
    private static final float DESIGN_TEXT_HORIZONTAL_MARGIN = 34;
    private static final float DESIGN_TEXT_VERTICAL_MARGIN = 29;

    private final ProgressBar mProgressBarView;
    private final TextView mMessageView;

    RestoreStateViewHolder(@NonNull View view) {

        super(view);

        view.setBackgroundColor(Color.TRANSPARENT);

        mProgressBarView = view.findViewById(R.id.restore_activity_restore_state_progress_bar);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mProgressBarView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_PROGRESS_MARGIN * Design.HEIGHT_RATIO);

        mMessageView = view.findViewById(R.id.restore_activity_restore_state_text_view);
        mMessageView.setTypeface(Design.FONT_MEDIUM34.typeface);
        mMessageView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_MEDIUM34.size);
        mMessageView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mMessageView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_TEXT_VERTICAL_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.leftMargin = (int) (DESIGN_TEXT_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin =(int) (DESIGN_TEXT_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
    }

    public void onBind(SpannableStringBuilder message, boolean inProgress) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBind: " + message);
        }

        if (inProgress) {
            mProgressBarView.setVisibility(View.VISIBLE);
        } else {
            mProgressBarView.setVisibility(View.GONE);
        }

        mMessageView.setText(message);
    }
}
