/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (fabrice.trescartes@twin.life)
 */

package org.twinlife.twinme.ui.contacts;

import android.graphics.Bitmap;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.CircularImageDescriptor;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.CircularImageView;

public class ShareContactViewHolder extends RecyclerView.ViewHolder {

    private static final float DESIGN_AVATAR_SIZE = 86f;
    private static final float DESIGN_HORIZONTAL_MARGIN = 20f;
    private static final float DESIGN_VERTICAL_MARGIN = 74f;

    private static final int AVATAR_SIZE;
    private static final int HORIZONTAL_MARGIN;
    private static final int VERTICAL_MARGIN;

    static {
        AVATAR_SIZE = (int) (DESIGN_AVATAR_SIZE * Design.HEIGHT_RATIO);
        HORIZONTAL_MARGIN = (int) (DESIGN_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
        VERTICAL_MARGIN = (int) (DESIGN_VERTICAL_MARGIN * Design.HEIGHT_RATIO);
    }

    private final CircularImageView mAvatarView;
    private final TextView mTextView;

    public ShareContactViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = Design.ITEM_VIEW_HEIGHT;

        view.setBackgroundColor(Design.WHITE_COLOR);

        mAvatarView = view.findViewById(R.id.share_contact_item_avatar_view);

        layoutParams = mAvatarView.getLayoutParams();
        layoutParams.width = AVATAR_SIZE;
        layoutParams.height = AVATAR_SIZE;

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mAvatarView.getLayoutParams();
        marginLayoutParams.topMargin = VERTICAL_MARGIN;
        marginLayoutParams.bottomMargin = VERTICAL_MARGIN;
        marginLayoutParams.leftMargin = HORIZONTAL_MARGIN;
        marginLayoutParams.rightMargin = HORIZONTAL_MARGIN;

        mTextView = view.findViewById(R.id.share_contact_item_name_view);
        Design.updateTextFont(mTextView, Design.FONT_MEDIUM32);
        mTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mTextView.getLayoutParams();
        marginLayoutParams.topMargin = VERTICAL_MARGIN;
        marginLayoutParams.bottomMargin = VERTICAL_MARGIN;
        marginLayoutParams.rightMargin = HORIZONTAL_MARGIN;
    }

    public void onBind(@Nullable String name, @Nullable Bitmap avatar) {

        if (avatar != null) {
            mAvatarView.setImage(itemView.getContext(), null,
                    new CircularImageDescriptor(avatar, 0.5f, 0.5f, 0.5f));
        }

        if (name != null) {
            String title = String.format(itemView.getContext().getString(R.string.share_contact_view_share_title), name);
            String message = String.format(itemView.getContext().getString(R.string.share_contact_view_share_info), name);

            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
            spannableStringBuilder.append(title);
            spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_DEFAULT), 0, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableStringBuilder.append("\n");
            int startInfo = spannableStringBuilder.length();
            spannableStringBuilder.append(message);
            spannableStringBuilder.setSpan(new RelativeSizeSpan(0.87f), startInfo, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_GREY), startInfo, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            mTextView.setText(spannableStringBuilder);
        }

        updateColor();
    }

    public void onViewRecycled() {

    }

    private void updateColor() {

        itemView.setBackgroundColor(Design.WHITE_COLOR);
    }
}
