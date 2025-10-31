/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.conversationActivity;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.format.Formatter;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.FileInfo;

public class PreviewFileViewHolder extends RecyclerView.ViewHolder {
    private static final String LOG_TAG = "PreviewFileViewHolder";
    private static final boolean DEBUG = false;

    private static final float DESIGN_ICON_MARGIN = 100f;
    private static final float DESIGN_TITLE_MARGIN = 34f;
    private static final float DESIGN_TITLE_TOP_MARGIN = 40f;
    private static final float DESIGN_ICON_SIZE = 200f;

    private final ImageView mIconView;
    private final TextView mTitleView;

    public PreviewFileViewHolder(@NonNull View view) {

        super(view);

        view.setBackgroundColor(Color.TRANSPARENT);

        mIconView = view.findViewById(R.id.preview_file_item_icon);

        float margin = (float) ((Design.DISPLAY_HEIGHT * 0.5) - ((DESIGN_ICON_SIZE + DESIGN_ICON_MARGIN) * Design.HEIGHT_RATIO));
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mIconView.getLayoutParams();
        marginLayoutParams.topMargin = (int) margin;

        ViewGroup.LayoutParams layoutParams = mIconView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_ICON_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_ICON_SIZE * Design.HEIGHT_RATIO);

        mTitleView = view.findViewById(R.id.preview_file_item_title);
        Design.updateTextFont(mTitleView, Design.FONT_MEDIUM38);
        mTitleView.setTextColor(Color.WHITE);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mTitleView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_TITLE_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_TITLE_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.topMargin = (int) (DESIGN_TITLE_TOP_MARGIN * Design.HEIGHT_RATIO);
    }

    public void onBind(Context context, FileInfo fileInfo) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBind: preview=" + fileInfo);
        }

        if (fileInfo.getFilename() != null) {
            String fileName = fileInfo.getFilename();

            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
            spannableStringBuilder.append(fileInfo.getFilename());
            spannableStringBuilder.setSpan(new ForegroundColorSpan(Color.WHITE), 0, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            if (fileInfo.getSize() != null) {
                spannableStringBuilder.append("\n");
                int startInfo = spannableStringBuilder.length();
                spannableStringBuilder.append(Formatter.formatFileSize(context, Integer.parseInt(fileInfo.getSize())));
                spannableStringBuilder.setSpan(new RelativeSizeSpan(0.8f), startInfo, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            mTitleView.setText(spannableStringBuilder);

            int icon = R.drawable.file_grey;

            if (fileInfo.getFilename() != null) {
                if (fileName.endsWith(".doc") || fileName.endsWith(".docx")) {
                    icon = R.drawable.file_word;
                } else if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx")) {
                    icon = R.drawable.file_excel;
                } else if (fileName.endsWith(".ppt") || fileName.endsWith(".pptx")) {
                    icon = R.drawable.file_powerpoint;
                } else if (fileName.endsWith(".pdf")) {
                    icon = R.drawable.file_pdf;
                }

                mIconView.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), icon, context.getTheme()));
            }
        }
    }

    public void onViewRecycled() {

    }
}
