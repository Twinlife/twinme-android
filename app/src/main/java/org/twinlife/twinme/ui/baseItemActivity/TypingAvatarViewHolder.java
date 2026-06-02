/*
 *  Copyright (c) 2019 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IdRes;

import org.twinlife.twinme.services.AbstractTwinmeService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.skin.TextStyle;
import org.twinlife.twinme.ui.users.UIContact;
import org.twinlife.twinme.ui.users.UIContactViewHolder;

class TypingAvatarViewHolder extends UIContactViewHolder<UIContact> {

    private static final int DESIGN_AVATAR_SIZE = 40;

    TypingAvatarViewHolder(AbstractTwinmeService service, View view, @IdRes int nameId, @IdRes int avatarId, @SuppressWarnings("unused") TextStyle font) {

        super(service, view, nameId, avatarId, 0, 0, 0, 0, 0, 0, Design.FONT_REGULAR32);
    }

    public void onBind(Context context, UIContact uiContact, boolean lightBackground) {

        super.onBind(context, uiContact, lightBackground);

        itemView.setBackgroundColor(Color.TRANSPARENT);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mAvatarView.getLayoutParams();
        marginLayoutParams.leftMargin = 0;

        ViewGroup.LayoutParams layoutParams = mAvatarView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_AVATAR_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_AVATAR_SIZE * Design.HEIGHT_RATIO);
    }
}
