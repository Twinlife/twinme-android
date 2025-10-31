/*
 *  Copyright (c) 2022-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.streamingAudioActivity;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.conversationActivity.MusicItem;

public class StreamingMusicViewHolder extends RecyclerView.ViewHolder {

    private static final int DESIGN_PLACEHOLDER_COLOR = Color.rgb(229, 229, 229);

    private static final int DESIGN_COVER_VIEW_RADIUS = 6;

    private static final float DESIGN_ITEM_VIEW_HEIGHT = 252f;
    private static final int ITEM_VIEW_HEIGHT;

    static {
        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
    }

    private final TextView mTitleView;
    private final ImageView mImageView;

    private final ImageView mPlaceholderCoverView;

    private final ImageView mSelectedView;

    StreamingMusicViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = ITEM_VIEW_HEIGHT;
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Color.TRANSPARENT);

        mTitleView = view.findViewById(R.id.streaming_music_item_title_view);
        Design.updateTextFont(mTitleView, Design.FONT_MEDIUM34);
        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        View coverContainerView = view.findViewById(R.id.streaming_music_item_cover_container_view);

        float radius = DESIGN_COVER_VIEW_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable coverViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        coverViewBackground.getPaint().setColor(DESIGN_PLACEHOLDER_COLOR);
        coverContainerView.setBackground(coverViewBackground);

        mImageView = view.findViewById(R.id.streaming_music_item_cover_view);
        mImageView.setClipToOutline(true);

        mPlaceholderCoverView = view.findViewById(R.id.streaming_music_item_placeholder_cover_view);

        mSelectedView = view.findViewById(R.id.streaming_music_item_selected_image);
        mSelectedView.setColorFilter(Design.getMainStyle());
    }

    public void onBind(MusicItem musicItem, boolean isSelected) {

        if (musicItem.getMediaMetaData().artwork != null) {
            mImageView.setImageBitmap(musicItem.getMediaMetaData().artwork);
            mPlaceholderCoverView.setVisibility(View.GONE);
        } else {
            mImageView.setImageBitmap(null);
            mPlaceholderCoverView.setVisibility(View.VISIBLE);
        }

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(musicItem.getMediaMetaData().title);
        spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_DEFAULT), 0, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        if (musicItem.getMediaMetaData().artist != null) {
            spannableStringBuilder.append("\n");
            int startInfo = spannableStringBuilder.length();
            spannableStringBuilder.append(musicItem.getMediaMetaData().artist);
            spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_GREY), startInfo, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableStringBuilder.setSpan(new RelativeSizeSpan(0.7f), startInfo, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (musicItem.getMediaMetaData().album != null) {
            spannableStringBuilder.append("\n");
            int startInfo = spannableStringBuilder.length();
            spannableStringBuilder.append(musicItem.getMediaMetaData().album);
            spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_DEFAULT), startInfo, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableStringBuilder.setSpan(new RelativeSizeSpan(0.7f), startInfo, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        mTitleView.setText(spannableStringBuilder);

        if (isSelected) {
            mSelectedView.setVisibility(View.VISIBLE);
        } else {
            mSelectedView.setVisibility(View.INVISIBLE);
        }
    }

    public void onViewRecycled() {

        mImageView.setImageBitmap(null);
    }
}