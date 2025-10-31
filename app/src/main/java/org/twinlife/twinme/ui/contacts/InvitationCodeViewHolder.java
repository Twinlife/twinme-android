/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.contacts;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class InvitationCodeViewHolder extends RecyclerView.ViewHolder {

    private static final float DESIGN_ITEM_VIEW_SIZE = 124f;
    private static final int ITEM_VIEW_SIZE;

    static {
        ITEM_VIEW_SIZE = (int) (DESIGN_ITEM_VIEW_SIZE * Design.HEIGHT_RATIO);
    }

    private final TextView mCodeView;
    private final TextView mExpirationView;
    private final View mSeparatorView;

    public InvitationCodeViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = ITEM_VIEW_SIZE;
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Design.WHITE_COLOR);

        mCodeView = view.findViewById(R.id.invitation_code_activity_code_item_code_view);
        Design.updateTextFont(mCodeView, Design.FONT_MEDIUM38);
        mCodeView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mExpirationView = view.findViewById(R.id.invitation_code_activity_code_item_expiration_view);
        Design.updateTextFont(mExpirationView, Design.FONT_MEDIUM32);
        mExpirationView.setTextColor(Design.FONT_COLOR_GREY);

        mSeparatorView = view.findViewById(R.id.invitation_code_activity_code_item_separator_view);
        mSeparatorView.setBackgroundColor(Design.SEPARATOR_COLOR);
    }

    public void onBind(UIInvitationCode invitationCode, boolean hideSeparator) {

        if (invitationCode.hasExpired()) {
            mCodeView.setTextColor(Design.FONT_COLOR_GREY);
        } else {
            mCodeView.setTextColor(Design.FONT_COLOR_DEFAULT);
        }

        mCodeView.setText(invitationCode.getCode());

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());
        String expirationDate = simpleDateFormat.format(new Date(invitationCode.getExpirationDate() * 1000L));

        String message = itemView.getContext().getString(R.string.invitation_code_activity_expiration) + " " + expirationDate;
        mExpirationView.setText(message);
        if (hideSeparator) {
            mSeparatorView.setVisibility(View.GONE);
        } else {
            mSeparatorView.setVisibility(View.VISIBLE);
        }
        updateColor();
    }

    public void onViewRecycled() {

        updateColor();
    }

    private void updateColor() {

        itemView.setBackgroundColor(Design.WHITE_COLOR);
    }
}
