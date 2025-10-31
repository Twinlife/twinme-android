/*
 *  Copyright (c) 2023-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.conversationFilesActivity;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.format.Formatter;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.baseItemActivity.FileItem;
import org.twinlife.twinme.ui.baseItemActivity.Item;
import org.twinlife.twinme.ui.baseItemActivity.PeerFileItem;

import java.io.File;

public class DocumentViewHolder extends RecyclerView.ViewHolder {

    private static final int DESIGN_PLACEHOLDER_COLOR = Color.rgb(229, 229, 229);

    private static final int DESIGN_DOCUMENT_VIEW_RADIUS = 6;

    private static final float DESIGN_ITEM_VIEW_HEIGHT = 252f;
    private static final int ITEM_VIEW_HEIGHT;

    static {
        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
    }

    private final TextView mTitleView;
    private final ImageView mIconView;

    private final View mSelectedView;
    private final ImageView mSelectedImageView;

    DocumentViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = ITEM_VIEW_HEIGHT;
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Color.TRANSPARENT);

        mTitleView = view.findViewById(R.id.conversation_files_activity_document_item_title_view);
        Design.updateTextFont(mTitleView, Design.FONT_MEDIUM34);
        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        View containerView = view.findViewById(R.id.conversation_files_activity_document_item_type_view);

        float radius = DESIGN_DOCUMENT_VIEW_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable containerViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        containerViewBackground.getPaint().setColor(DESIGN_PLACEHOLDER_COLOR);
        containerView.setBackground(containerViewBackground);

        mIconView = view.findViewById(R.id.conversation_files_activity_document_item_image_view);

        mSelectedView = view.findViewById(R.id.conversation_files_activity_document_item_selected_view);

        mSelectedImageView = view.findViewById(R.id.conversation_files_activity_document_item_selected_image_view);
        mSelectedImageView.setColorFilter(Design.getMainStyle());
    }

    public void onBind(Item item, ConversationFilesActivity conversationFilesActivity) {

        ConversationService.NamedFileDescriptor namedFileDescriptor;
        if (item.isPeerItem()) {
            final PeerFileItem peerFileItem = (PeerFileItem) item;
            namedFileDescriptor = peerFileItem.getNamedFileDescriptor();
        } else {
            final FileItem fileItem = (FileItem) item;
            namedFileDescriptor = fileItem.getNamedFileDescriptor();
        }

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(namedFileDescriptor.getName());
        spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_DEFAULT), 0, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.append("\n");

        String size = Formatter.formatFileSize(conversationFilesActivity, namedFileDescriptor.getLength());

        int startInfo = spannableStringBuilder.length();
        spannableStringBuilder.append(size);
        spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_GREY), startInfo, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.setSpan(new RelativeSizeSpan(0.7f), startInfo, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mTitleView.setText(spannableStringBuilder);

        String path = namedFileDescriptor.getPath();
        File file = new File(conversationFilesActivity.getTwinmeContext().getFilesDir(), path);
        mIconView.setImageResource(FileItem.getFileIcon(file.getPath()));

        if (conversationFilesActivity.isSelectMode()) {
            mSelectedView.setVisibility(View.VISIBLE);

            if (item.isSelected()) {
                mSelectedImageView.setVisibility(View.VISIBLE);
            } else {
                mSelectedImageView.setVisibility(View.INVISIBLE);
            }
        } else {
            mSelectedView.setVisibility(View.INVISIBLE);
        }
    }

    public void onViewRecycled() {

    }
}
