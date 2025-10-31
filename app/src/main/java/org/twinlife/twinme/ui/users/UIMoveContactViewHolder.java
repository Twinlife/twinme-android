/*
 *  Copyright (c) 2019 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.users;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.IdRes;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.services.AbstractTwinmeService;
import org.twinlife.twinme.skin.Design;

public class UIMoveContactViewHolder extends UIContactViewHolder<UIMoveContact> {

    private static final int DESIGN_SELECTION_HEIGHT = 44;
    private static final float DESIGN_SELECTION_MARGIN_PERCENT = 0.0426f;

    private final ImageView mSelectedView;
    private final View mSelectionLayout;
    private boolean mShowSelection;

    private final TextView mSpaceView;

    private static final int SPACE_VIEW_COLOR = Color.argb(255, 143, 150, 164);

    UIMoveContactViewHolder(AbstractTwinmeService service, View view, @IdRes int nameId, @IdRes int avatarId, @IdRes int certifiedId, @IdRes int separatorId, boolean showSelection) {

        super(service, view, nameId, avatarId, 0, 0, 0, certifiedId, separatorId, Design.FONT_REGULAR32);

        mSelectedView = view.findViewById(R.id.contacts_space_activity_contact_selected_image);
        mSelectedView.setColorFilter(Design.getMainStyle());

        mSelectionLayout = view.findViewById(R.id.contacts_space_activity_contact_layout_image);

        ViewGroup.LayoutParams layoutParams = mSelectionLayout.getLayoutParams();
        layoutParams.height = (int) (DESIGN_SELECTION_HEIGHT * Design.HEIGHT_RATIO);

        mSpaceView = view.findViewById(R.id.contacts_space_activity_contact_item_space_view);
        mSpaceView.setTypeface(Design.FONT_REGULAR32.typeface);
        mSpaceView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_REGULAR32.size);
        mSpaceView.setTextColor(SPACE_VIEW_COLOR);

        mShowSelection = showSelection;
    }

    public void onBind(Context context, UIMoveContact uiContact,boolean hideSeparator, boolean showSelection) {

        super.onBind(context, uiContact, hideSeparator);

        itemView.setBackgroundColor(Design.WHITE_COLOR);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mNameView.getLayoutParams();
        marginLayoutParams.rightMargin = Design.NAME_TRAILING;
        marginLayoutParams.setMarginEnd(Design.NAME_TRAILING);

        mNameView.setTypeface(Design.FONT_MEDIUM34.typeface);
        mNameView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_MEDIUM34.size);

        float maxWidth = Design.DISPLAY_WIDTH - (Design.DISPLAY_WIDTH * (DESIGN_MARGIN_PERCENT + DESIGN_SELECTION_MARGIN_PERCENT)) - (DESIGN_AVATAR_HEIGHT * Design.HEIGHT_RATIO) - Design.NAME_TRAILING - (DESIGN_SELECTION_HEIGHT * Design.HEIGHT_RATIO);

        if (uiContact.isCertified()) {
            marginLayoutParams.rightMargin = (int) (DESIGN_CERTIFIED_MARGIN * Design.WIDTH_RATIO);
            marginLayoutParams.setMarginEnd((int) (DESIGN_CERTIFIED_MARGIN * Design.WIDTH_RATIO));

            maxWidth = Design.DISPLAY_WIDTH - (Design.DISPLAY_WIDTH * (DESIGN_MARGIN_PERCENT + DESIGN_SELECTION_MARGIN_PERCENT)) - (DESIGN_AVATAR_HEIGHT * Design.HEIGHT_RATIO) - Design.NAME_TRAILING - (DESIGN_CERTIFIED_MARGIN * Design.WIDTH_RATIO) - Design.CERTIFIED_HEIGHT - (DESIGN_SELECTION_HEIGHT * Design.HEIGHT_RATIO);
        }

        mNameView.setMaxWidth((int) maxWidth);

        if (uiContact.getContact().getSpace() != null) {
            mSpaceView.setText(uiContact.getContact().getSpace().getName());
        }

        mShowSelection = showSelection;

        if (mShowSelection) {
            mSelectionLayout.setVisibility(View.VISIBLE);
            if (uiContact.isSelected()) {
                mSelectedView.setVisibility(View.VISIBLE);
            } else {
                mSelectedView.setVisibility(View.INVISIBLE);
            }

            if (uiContact.canMove()) {
                mSelectedView.setAlpha(1f);
                getAvatarView().setAlpha(1f);
                getNameView().setAlpha(1f);
            } else {
                mSelectedView.setAlpha(0.5f);
                getAvatarView().setAlpha(0.5f);
                getNameView().setAlpha(0.5f);
            }

        } else {
            mSelectionLayout.setVisibility(View.GONE);
        }
    }
}
