/*
 *  Copyright (c) 2017-2020 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.users;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.services.AbstractTwinmeService;
import org.twinlife.twinme.skin.Design;

public class UIInvitationViewHolder extends UIContactViewHolder<UIContact> {

    private final TextView mInvitationStatus;

    public UIInvitationViewHolder(@NonNull AbstractTwinmeService service, View view, @IdRes int nameId, @IdRes int avatarId, @IdRes int separatorId) {
        super(service, view, nameId, avatarId, 0, 0, 0, 0, separatorId, Design.FONT_REGULAR34);

        mInvitationStatus = view.findViewById(R.id.group_member_activity_member_item_invitation_status_text);
        Design.updateTextFont(mInvitationStatus, Design.FONT_REGULAR24);
        mInvitationStatus.setTextColor(Design.FONT_COLOR_DEFAULT);
    }

    public void onBind(Context context, UIContact uiContact, boolean hideSeparator) {

        super.onBind(context, uiContact, hideSeparator);

        UIInvitation invitation = (UIInvitation) uiContact;
        mInvitationStatus.setVisibility(View.VISIBLE);
        if (invitation.peerFailure()) {
            mInvitationStatus.setText(context.getString(R.string.conversation_activity_invitation_failed));

        } else {
            switch (invitation.getStatus()) {
                case PENDING:
                    mInvitationStatus.setText(context.getString(R.string.conversation_activity_invitation_pending));
                    break;

                case ACCEPTED:
                    mInvitationStatus.setText(context.getString(R.string.conversation_activity_invitation_accepted));
                    break;

                case JOINED:
                    mInvitationStatus.setText(context.getString(R.string.conversation_activity_invitation_joined));
                    break;

                case REFUSED:
                case WITHDRAWN:
                    mInvitationStatus.setText(context.getString(R.string.conversation_activity_invitation_refused));
                    break;
            }
        }
    }
}
