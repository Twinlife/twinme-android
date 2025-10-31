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
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.IdRes;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.services.AbstractTwinmeService;
import org.twinlife.twinme.skin.Design;

public class UISelectableContactViewHolder extends UIContactViewHolder<UISelectableContact> {

    private static final int DESIGN_SELECTION_HEIGHT = 44;
    private static final float DESIGN_SELECTION_MARGIN_PERCENT = 0.0426f;

    private final ImageView mSelectedView;
    private final View mSelectionLayout;
    private boolean mShowSelection;

    public UISelectableContactViewHolder(AbstractTwinmeService service, View view, @IdRes int nameId, @IdRes int avatarId, @IdRes int certifiedId, @IdRes int separatorId, boolean showSelection) {

        super(service, view, nameId, avatarId, 0, 0, 0, certifiedId, separatorId, Design.FONT_REGULAR32);

        mSelectionLayout = view.findViewById(R.id.add_group_member_activity_contact_layout_image);

        ViewGroup.LayoutParams layoutParams = mSelectionLayout.getLayoutParams();
        layoutParams.height = (int) (DESIGN_SELECTION_HEIGHT * Design.HEIGHT_RATIO);

        mSelectedView = view.findViewById(R.id.add_group_member_activity_contact_selected_image);
        mSelectedView.setColorFilter(Design.getMainStyle());

        mShowSelection = showSelection;
    }

    public void onBind(Context context, UISelectableContact uiContact, boolean hideSeparator, boolean showSelection, @SuppressWarnings("unused") boolean showAdd) {

        super.onBind(context, uiContact, hideSeparator);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mNameView.getLayoutParams();
        marginLayoutParams.rightMargin = Design.NAME_TRAILING;
        marginLayoutParams.setMarginEnd(Design.NAME_TRAILING);

        float maxWidth = Design.DISPLAY_WIDTH - (Design.DISPLAY_WIDTH * (DESIGN_MARGIN_PERCENT + DESIGN_SELECTION_MARGIN_PERCENT)) - (DESIGN_AVATAR_HEIGHT * Design.HEIGHT_RATIO) - Design.NAME_TRAILING - (DESIGN_SELECTION_HEIGHT * Design.HEIGHT_RATIO);

        if (mCertifiedView != null) {
            if (uiContact.isCertified()) {
                mCertifiedView.setVisibility(View.VISIBLE);

                marginLayoutParams.rightMargin = (int) (DESIGN_CERTIFIED_MARGIN * Design.WIDTH_RATIO);
                marginLayoutParams.setMarginEnd((int) (DESIGN_CERTIFIED_MARGIN * Design.WIDTH_RATIO));

                maxWidth = Design.DISPLAY_WIDTH - (Design.DISPLAY_WIDTH * (DESIGN_MARGIN_PERCENT + DESIGN_SELECTION_MARGIN_PERCENT)) - (DESIGN_AVATAR_HEIGHT * Design.HEIGHT_RATIO) - Design.NAME_TRAILING - (DESIGN_CERTIFIED_MARGIN * Design.WIDTH_RATIO) - Design.CERTIFIED_HEIGHT - (DESIGN_SELECTION_HEIGHT * Design.HEIGHT_RATIO);
            } else {
                mCertifiedView.setVisibility(View.GONE);
            }
        }

        mNameView.setMaxWidth((int) maxWidth);

        itemView.setBackgroundColor(Design.WHITE_COLOR);

        mShowSelection = showSelection;

        if (mShowSelection) {
            mSelectionLayout.setVisibility(View.VISIBLE);
            if (uiContact.isSelected() || uiContact.isInvited()) {
                mSelectedView.setVisibility(View.VISIBLE);
            } else {
                mSelectedView.setVisibility(View.INVISIBLE);
            }

            if (uiContact.isInvited()) {
                mSelectedView.setAlpha(0.5f);
                getAvatarView().setAlpha(0.5f);
                getNameView().setAlpha(0.5f);
            } else {
                mSelectedView.setAlpha(1f);
                getAvatarView().setAlpha(1f);
                getNameView().setAlpha(1f);
            }

        } else {
            mSelectionLayout.setVisibility(View.GONE);
        }
    }
}
