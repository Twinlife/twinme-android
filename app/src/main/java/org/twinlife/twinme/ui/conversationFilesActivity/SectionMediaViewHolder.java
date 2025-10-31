/*
 *  Copyright (c) 2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.conversationFilesActivity;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class SectionMediaViewHolder extends RecyclerView.ViewHolder {

    private static final int DESIGN_TITLE_HEIGHT = 80;

    private final TextView mTextView;

    private final MediaAdapter mMediaAdapter;

    public SectionMediaViewHolder(@NonNull View view, ConversationFilesActivity conversationFilesActivity) {

        super(view);

        view.setBackgroundColor(Design.WHITE_COLOR);

        mTextView = view.findViewById(R.id.conversation_files_activity_section_media_title_view);
        Design.updateTextFont(mTextView, Design.FONT_BOLD26);
        mTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        ViewGroup.LayoutParams layoutParams = mTextView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_TITLE_HEIGHT * Design.HEIGHT_RATIO);

        mMediaAdapter = new MediaAdapter(conversationFilesActivity);

        int mediaPerLine = 3;
        if (Design.DISPLAY_WIDTH > 320) {
            mediaPerLine = 4;
        }
        GridLayoutManager gridLayoutManager = new GridLayoutManager(conversationFilesActivity, mediaPerLine);
        RecyclerView recyclerView = view.findViewById(R.id.conversation_files_activity_section_media_list_view);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(mMediaAdapter);
        recyclerView.setItemAnimator(null);
    }

    public void onBind(UIFileSection fileSection) {

        mTextView.setText(fileSection.getTitle(mTextView.getContext()));
        mMediaAdapter.setFileSection(fileSection);
    }

}