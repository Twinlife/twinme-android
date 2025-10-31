/*
 *  Copyright (c) 2022 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.settingsActivity;

import android.graphics.Paint;
import android.graphics.Rect;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.skin.EmojiSize;
import org.twinlife.twinme.skin.TextStyle;

public class EmojiSizeViewHolder extends RecyclerView.ViewHolder {

    private static final int EMOJI_CODE = 0x1F609;

    private static final float CHECK_VIEW_PERCENT_HEIGHT = 0.3667f;
    private static final float DESIGN_EMOJI_VIEW_MARGIN = 10f;
    private static final int EMOJI_VIEW_MARGIN;

    static {
        EMOJI_VIEW_MARGIN = (int) (DESIGN_EMOJI_VIEW_MARGIN * Design.HEIGHT_RATIO);
    }

    private final TextView mTitleView;
    private final TextView mEmojiView;
    private final ImageView mSelectedView;

    public EmojiSizeViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = Design.SECTION_HEIGHT;
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Design.WHITE_COLOR);

        mEmojiView = view.findViewById(R.id.conversation_settings_activity_emoji_size_item_emoji_view);
        mEmojiView.setText(new String(Character.toChars(EMOJI_CODE)));

        TextStyle textStyle = Design.getSampleEmojiFont(view.getContext(), EmojiSize.LARGE);
        layoutParams = mEmojiView.getLayoutParams();
        layoutParams.width = getEmojiWidth(textStyle);
        mEmojiView.setLayoutParams(layoutParams);

        mTitleView = view.findViewById(R.id.conversation_settings_activity_emoji_size_item_name_view);
        Design.updateTextFont(mTitleView, Design.FONT_REGULAR32);
        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        View contentSelectedView = view.findViewById(R.id.conversation_settings_activity_emoji_size_item_layout_image);
        layoutParams = contentSelectedView.getLayoutParams();
        layoutParams.height = (int) (Design.SECTION_HEIGHT * CHECK_VIEW_PERCENT_HEIGHT);
        contentSelectedView.setLayoutParams(layoutParams);

        mSelectedView = view.findViewById(R.id.conversation_settings_activity_emoji_size_item_selected_image);
        mSelectedView.setColorFilter(Design.getMainStyle());
    }

    public void onBind(String title, EmojiSize emojiSize, boolean isSelected) {

        TextStyle textStyle = Design.getSampleEmojiFont(mEmojiView.getContext(), emojiSize);

        ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
        layoutParams.height = getEmojiHeight(textStyle);
        itemView.setLayoutParams(layoutParams);

        mTitleView.setText(title);

        mEmojiView.setTypeface(textStyle.typeface);
        mEmojiView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textStyle.size);

        if (isSelected) {
            mSelectedView.setVisibility(View.VISIBLE);
        } else {
            mSelectedView.setVisibility(View.INVISIBLE);
        }

        updateFont();
        updateColor();
    }

    private void updateFont() {

        Design.updateTextFont(mTitleView, Design.FONT_REGULAR32);
    }

    private void updateColor() {

        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);
        itemView.setBackgroundColor(Design.WHITE_COLOR);
        mSelectedView.setColorFilter(Design.getMainStyle());
    }

    private int getEmojiHeight(TextStyle textStyle) {

        String string = new String(Character.toChars(EMOJI_CODE));
        Paint paint = new Paint();
        paint.setTextSize(textStyle.size);
        paint.setTypeface(textStyle.typeface);
        paint.setStyle(Paint.Style.FILL);

        Rect rect = new Rect();
        paint.getTextBounds(string, 0, string.length(), rect);

        int emojiHeight = rect.height() + (EMOJI_VIEW_MARGIN * 2);

        return Math.max(emojiHeight, Design.SECTION_HEIGHT);
    }

    private int getEmojiWidth(TextStyle textStyle) {

        String string = new String(Character.toChars(EMOJI_CODE));
        Paint paint = new Paint();
        paint.setTextSize(textStyle.size);
        paint.setTypeface(textStyle.typeface);
        paint.setStyle(Paint.Style.FILL);

        Rect rect = new Rect();
        paint.getTextBounds(string, 0, string.length(), rect);

        return rect.width();
    }
}