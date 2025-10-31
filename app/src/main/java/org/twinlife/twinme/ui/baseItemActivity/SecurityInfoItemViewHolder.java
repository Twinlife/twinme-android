/*
 *  Copyright (c) 2017-2024 twinlife SA & Telefun SAS.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Florian Fossa (ffossa@skyrock.com)
 *   Fabrice Trescartes (fabrice.trescartes@twin.life)
 *   Romain Kolb (romain.kolb@skyrock.com)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.skin.TextStyle;

public class SecurityInfoItemViewHolder extends BaseItemViewHolder {

    private final TextView mTextView;
    private final ViewGroup.MarginLayoutParams mMarginLayoutParams;
    private final GradientDrawable mGradientDrawable;

    SecurityInfoItemViewHolder(BaseItemActivity baseItemActivity, View view) {

        super(baseItemActivity, view, R.id.base_item_activity_security_item_overlay_view);

        mTextView = view.findViewById(R.id.base_item_activity_header_item_text);
        mTextView.setPadding(MESSAGE_ITEM_TEXT_WIDTH_PADDING, MESSAGE_ITEM_TEXT_DEFAULT_PADDING, MESSAGE_ITEM_TEXT_WIDTH_PADDING, MESSAGE_ITEM_TEXT_DEFAULT_PADDING);

        TextStyle textStyle = Design.FONT_REGULAR30;
        mTextView.setTypeface(textStyle.typeface);
        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textStyle.size);
        mTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mMarginLayoutParams = (ViewGroup.MarginLayoutParams) mTextView.getLayoutParams();

        mGradientDrawable = new GradientDrawable();
        mGradientDrawable.mutate();
        mGradientDrawable.setColor(Design.POPUP_BACKGROUND_COLOR);
        mGradientDrawable.setShape(GradientDrawable.RECTANGLE);
        ViewCompat.setBackground(mTextView, mGradientDrawable);
    }

    @Override
    public void onBind(Item item) {

        mGradientDrawable.setCornerRadii(getCornerRadii());
        Drawable drawable = ResourcesCompat.getDrawable(getBaseItemActivity().getResources(), R.drawable.lock, null);
        if (drawable != null) {
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), Math.round(drawable.getIntrinsicHeight()));
            ImageSpan is = new ImageSpan(drawable);

            String name = getBaseItemActivity().getContactName();
            if (name != null) {
                SpannableString spannableString = new SpannableString(getBaseItemActivity().getString(R.string.conversation_activity_info, name));
                spannableString.setSpan(is, 0, 5, 0);
                mTextView.setText(spannableString);
            }
        }

        if ((Item.TOP_RIGHT) == 0) {
            mMarginLayoutParams.topMargin = ITEM_TOP_MARGIN1;
        } else {
            mMarginLayoutParams.topMargin = ITEM_TOP_MARGIN2;
        }
        if ((Item.BOTTOM_RIGHT) == 0) {
            mMarginLayoutParams.bottomMargin = ITEM_BOTTOM_MARGIN1;
        } else {
            mMarginLayoutParams.bottomMargin = ITEM_BOTTOM_MARGIN2;
        }
        mTextView.setLayoutParams(mMarginLayoutParams);

        ViewGroup.LayoutParams overlayLayoutParams = getOverlayView().getLayoutParams();
        overlayLayoutParams.height = itemView.getHeight();
        getOverlayView().setLayoutParams(overlayLayoutParams);

        if (getBaseItemActivity().isMenuOpen()) {
            getOverlayView().setVisibility(View.VISIBLE);
        } else {
            getOverlayView().setVisibility(View.INVISIBLE);
        }
    }
}
