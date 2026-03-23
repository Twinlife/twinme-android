/*
 *  Copyright (c) 2024-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.backupActivity;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class RestoreWordViewHolder  extends RecyclerView.ViewHolder {
    private static final String LOG_TAG = "RestoreWordViewHolder";
    private static final boolean DEBUG = false;

    private static final float DESIGN_WORD_HEIGHT = 100f;
    private static final float DESIGN_CONTAINER_MARGIN = 34f;
    private static final float DESIGN_TEXT_HEIGHT = 80f;
    private static final float DESIGN_WORD_MARGIN = 14f;
    private static final float DESIGN_CHECK_SIZE = 36f;

    private final View mContainerView;
    private final TextView mWordView;
    private final ImageView mCheckImageView;

    RestoreWordViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int) (DESIGN_WORD_HEIGHT * Design.HEIGHT_RATIO);
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Color.TRANSPARENT);

        mContainerView = view.findViewById(R.id.restore_activity_restore_word_item_container_view);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mContainerView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_CONTAINER_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_CONTAINER_MARGIN * Design.WIDTH_RATIO);

        GradientDrawable containerViewBackground = new GradientDrawable();
        containerViewBackground.setColor(Design.GREY_ITEM_COLOR);
        containerViewBackground.setCornerRadius((int) (DESIGN_WORD_HEIGHT * Design.HEIGHT_RATIO * 0.5f * Resources.getSystem().getDisplayMetrics().density));
        mContainerView.setBackground(containerViewBackground);

        mWordView = view.findViewById(R.id.restore_activity_restore_word_item_text);
        mWordView.setTypeface(Design.FONT_MONOSPACE30.typeface);
        mWordView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_MONOSPACE30.size);
        mWordView.setTextColor(Design.FONT_COLOR_DEFAULT);

        layoutParams = mWordView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_TEXT_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mWordView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_WORD_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_WORD_MARGIN * Design.WIDTH_RATIO * 2) + (int) (DESIGN_CHECK_SIZE * Design.HEIGHT_RATIO);

        mCheckImageView = view.findViewById(R.id.restore_activity_restore_word_check_image);
        mCheckImageView.setColorFilter(Design.getMainStyle());
        mCheckImageView.setVisibility(View.GONE);

        layoutParams = mCheckImageView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_CHECK_SIZE * Design.WIDTH_RATIO);
        layoutParams.height = (int) (DESIGN_CHECK_SIZE * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mCheckImageView.getLayoutParams();
        marginLayoutParams.rightMargin = (int) (DESIGN_WORD_MARGIN * Design.WIDTH_RATIO);
    }

    public void onBind(UIBackupWord backupWord, boolean currentWord) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBind: " + backupWord);
        }

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(String.valueOf(backupWord.getPosition() + 1));
        spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_GREY), 0, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        if (backupWord.getWord() != null) {
            spannableStringBuilder.append(" ");
            if (backupWord.getPosition() < 9) {
                spannableStringBuilder.append(" ");
            }
            int startWord = spannableStringBuilder.length();
            spannableStringBuilder.append(backupWord.getWord());
            spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_DEFAULT), startWord, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        mWordView.setText(spannableStringBuilder);

        GradientDrawable containerViewBackground = new GradientDrawable();
        containerViewBackground.setColor(Design.GREY_ITEM_COLOR);
        containerViewBackground.setCornerRadius((int) (DESIGN_WORD_HEIGHT * Design.HEIGHT_RATIO * 0.5f * Resources.getSystem().getDisplayMetrics().density));

        if (currentWord) {
            containerViewBackground.setStroke(2, Design.FONT_COLOR_GREY);
        }
        mContainerView.setBackground(containerViewBackground);

        if (backupWord.getWord() != null) {
            mCheckImageView.setVisibility(View.VISIBLE);
        } else {
            mCheckImageView.setVisibility(View.GONE);
        }
    }
}
