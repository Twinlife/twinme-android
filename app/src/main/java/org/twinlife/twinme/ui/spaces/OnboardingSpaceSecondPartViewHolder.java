/*
 *  Copyright (c) 2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.spaces;

import android.content.Context;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class OnboardingSpaceSecondPartViewHolder extends RecyclerView.ViewHolder {

    private static final float DESIGN_IMAGE_HEIGHT = 320;
    private static final float DESIGN_IMAGE_MARGIN = 30;

    private static final int IMAGE_MARGIN;

    static {
        IMAGE_MARGIN = (int) (DESIGN_IMAGE_MARGIN * Design.HEIGHT_RATIO);
    }

    private final TextView mMessageTextView;

    OnboardingSpaceSecondPartViewHolder(@NonNull View view) {

        super(view);

        ImageView imageView = view.findViewById(R.id.onboarding_space_activity_second_part_item_image);

        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_IMAGE_HEIGHT * Design.HEIGHT_RATIO);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) imageView.getLayoutParams();
        marginLayoutParams.topMargin = IMAGE_MARGIN;
        marginLayoutParams.bottomMargin = IMAGE_MARGIN;

        mMessageTextView = view.findViewById(R.id.onboarding_space_activity_second_part_item_message_view);
        Design.updateTextFont(mMessageTextView, Design.FONT_MEDIUM32);
        mMessageTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mMessageTextView.setMovementMethod(new ScrollingMovementMethod());

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mMessageTextView.getLayoutParams();
        marginLayoutParams.leftMargin = Design.ONBOARDING_TEXT_MARGIN;
        marginLayoutParams.rightMargin = Design.ONBOARDING_TEXT_MARGIN;
    }

    public void onBind(Context context) {

        String message = context.getString(R.string.create_space_activity_onboarding_message_part_1) +
                "\n\n" +
                context.getString(R.string.create_space_activity_onboarding_message_part_2) +
                "\n\n" +
                context.getString(R.string.create_space_activity_onboarding_message_part_3);
        mMessageTextView.setText(message);
    }

    public void onViewRecycled() {

    }
}
