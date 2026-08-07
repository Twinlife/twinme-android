/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.conversations;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.mainActivity.UIConversation;

public class MenuHeaderViewHolder extends RecyclerView.ViewHolder {

    private static final int DESIGN_AVATAR_MARGIN = 40;
    protected static final int DESIGN_AVATAR_SIZE = 148;
    protected static final int DESIGN_TITLE_VERTICAL_MARGIN = 40;
    protected static final int DESIGN_TITLE_HORIZONTAL_MARGIN = 24;

    private final TextView mTitleView;
    private final  ShapeableImageView mAvatarView;

    public MenuHeaderViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int) (MenuConversationShortcutView.DESIGN_HEADER_HEIGHT * Design.HEIGHT_RATIO);
        view.setLayoutParams(layoutParams);

        mAvatarView = view.findViewById(R.id.menu_header_item_avatar_view);
        layoutParams = mAvatarView.getLayoutParams();
        mAvatarView.setBackgroundColor(Design.WHITE_COLOR);
        layoutParams.width = (int) (DESIGN_AVATAR_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_AVATAR_SIZE * Design.HEIGHT_RATIO);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mAvatarView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_AVATAR_MARGIN * Design.HEIGHT_RATIO);

        mTitleView = view.findViewById(R.id.menu_header_item_title_view);
        Design.updateTextFont(mTitleView, Design.FONT_MEDIUM40);
        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mTitleView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_TITLE_VERTICAL_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.leftMargin = (int) (DESIGN_TITLE_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_TITLE_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);

    }

    public void onBind(UIConversation conversation) {

        mAvatarView.setImageBitmap(conversation.getAvatar());
        mTitleView.setText(conversation.getName());
    }

    public void onViewRecycled() {

    }
}
