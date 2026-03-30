/*
 *  Copyright (c) 2024-2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.backupActivity;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class BackupWordViewHolder extends RecyclerView.ViewHolder {
    private static final String LOG_TAG = "BackupWordViewHolder";
    private static final boolean DEBUG = false;

    private static final float DESIGN_WORD_HEIGHT = 100f;
    private static final float DESIGN_CONTAINER_MARGIN = 16f;
    private static final float DESIGN_TEXT_HEIGHT = 80f;
    private static final float DESIGN_WORD_MARGIN = 14f;

    private final TextView mWordView;

    BackupWordViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int) (DESIGN_WORD_HEIGHT * Design.HEIGHT_RATIO);
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Color.TRANSPARENT);

        View containerView = view.findViewById(R.id.create_backup_activity_word_item_container_view);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) containerView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_CONTAINER_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_CONTAINER_MARGIN * Design.WIDTH_RATIO);

        float radius = DESIGN_WORD_HEIGHT * Design.HEIGHT_RATIO * 0.5f * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable containerViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        containerViewBackground.getPaint().setColor(Design.GREY_ITEM_COLOR);
        containerView.setBackground(containerViewBackground);

        mWordView = view.findViewById(R.id.create_backup_activity_word_item_text);
        mWordView.setTypeface(Design.FONT_MONOSPACE30.typeface);
        mWordView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_MONOSPACE30.size);
        mWordView.setTextColor(Design.FONT_COLOR_DEFAULT);

        layoutParams = mWordView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_TEXT_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mWordView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_WORD_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_WORD_MARGIN * Design.WIDTH_RATIO);
    }

    public void onBind(UIBackupWord backupWord) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBind: " + backupWord);
        }

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(String.valueOf(backupWord.getPosition() + 1));
        spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_GREY), 0, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        spannableStringBuilder.append(" ");
        if (backupWord.getPosition() < 9) {
            spannableStringBuilder.append(" ");
        }
        int startWord = spannableStringBuilder.length();
        spannableStringBuilder.append(backupWord.getWord());
        spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_DEFAULT), startWord, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        mWordView.setText(spannableStringBuilder);
    }
}
