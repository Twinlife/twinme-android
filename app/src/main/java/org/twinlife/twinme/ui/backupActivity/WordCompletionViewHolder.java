/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.backupActivity;

import android.graphics.Color;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class WordCompletionViewHolder  extends RecyclerView.ViewHolder {
    private static final String LOG_TAG = "WordCompletion...";
    private static final boolean DEBUG = false;

    private static final float DESIGN_WORD_HEIGHT = 80f;
    private static final float DESIGN_WORD_MARGIN = 14f;

    private final TextView mWordView;

    public WordCompletionViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int) (DESIGN_WORD_HEIGHT * Design.HEIGHT_RATIO);
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Color.TRANSPARENT);

        mWordView = view.findViewById(R.id.word_completion_item_text);
        mWordView.setTypeface(Design.FONT_MONOSPACE34.typeface);
        mWordView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_MONOSPACE34.size);
        mWordView.setTextColor(Design.FONT_COLOR_DEFAULT);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mWordView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_WORD_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_WORD_MARGIN * Design.WIDTH_RATIO);
    }

    public void onBind(String word, int backgroundColor) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBind: " + word + " backgroundColor:" + backgroundColor);
        }

        mWordView.setText(word);
        itemView.setBackgroundColor(backgroundColor);
    }
}
