/*
 *  Copyright (c) 2023-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.spaces;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class OnboardingSpaceThirdPartViewHolder extends RecyclerView.ViewHolder {

    private static final float DESIGN_CREATE_BUTTON_MARGIN = 20;
    private static final float DESIGN_IMAGE_HEIGHT = 260;
    private static final float DESIGN_IMAGE_MARGIN = 30;
    private static final float DESIGN_DO_NOT_SHOW_HEIGHT = 140f;

    private static final int IMAGE_MARGIN;

    private static final int CREATE_BUTTON_MARGIN;

    static {
        IMAGE_MARGIN = (int) (DESIGN_IMAGE_MARGIN * Design.HEIGHT_RATIO);
        CREATE_BUTTON_MARGIN = (int) (DESIGN_CREATE_BUTTON_MARGIN * Design.HEIGHT_RATIO);
    }

    private final View mCreateSpaceView;
    private final TextView mCreateSpaceTextView;
    private final TextView mMessageTextView;
    private final View mDoNotShowView;

    OnboardingSpaceThirdPartViewHolder(OnboardingSpaceActivity onboardingSpaceActivity, @NonNull View view) {

        super(view);

        ImageView imageView = view.findViewById(R.id.onboarding_space_activity_third_part_item_image);

        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_IMAGE_HEIGHT * Design.HEIGHT_RATIO);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) imageView.getLayoutParams();
        marginLayoutParams.topMargin = IMAGE_MARGIN;
        marginLayoutParams.bottomMargin = IMAGE_MARGIN;

        mMessageTextView = view.findViewById(R.id.onboarding_space_activity_third_part_item_message_view);
        Design.updateTextFont(mMessageTextView, Design.FONT_MEDIUM32);
        mMessageTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mMessageTextView.setMovementMethod(new ScrollingMovementMethod());

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mMessageTextView.getLayoutParams();
        marginLayoutParams.leftMargin = Design.ONBOARDING_TEXT_MARGIN;
        marginLayoutParams.rightMargin = Design.ONBOARDING_TEXT_MARGIN;

        mCreateSpaceView = view.findViewById(R.id.onboarding_space_activity_third_part_item_create_space_view);
        mCreateSpaceView.setOnClickListener(v -> onboardingSpaceActivity.onCreateSpaceClick());

        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable createViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        createViewBackground.getPaint().setColor(Design.getMainStyle());
        mCreateSpaceView.setBackground(createViewBackground);

        layoutParams = mCreateSpaceView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = Design.BUTTON_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mCreateSpaceView.getLayoutParams();
        marginLayoutParams.topMargin = CREATE_BUTTON_MARGIN;

        mCreateSpaceTextView = view.findViewById(R.id.onboarding_space_activity_third_part_item_create_space_text_view);
        Design.updateTextFont(mCreateSpaceTextView, Design.FONT_BOLD36);
        mCreateSpaceTextView.setTextColor(Color.WHITE);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mCreateSpaceTextView.getLayoutParams();
        marginLayoutParams.leftMargin = Design.BUTTON_MARGIN;
        marginLayoutParams.rightMargin = Design.BUTTON_MARGIN;

        mDoNotShowView = view.findViewById(R.id.onboarding_space_activity_item_do_not_show_view);
        mDoNotShowView.setOnClickListener(v -> onboardingSpaceActivity.onDoNotShowAgainClick());

        layoutParams = mDoNotShowView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_DO_NOT_SHOW_HEIGHT * Design.HEIGHT_RATIO);

        TextView doNotShowTextView = view.findViewById(R.id.onboarding_space_activity_item_do_not_show_title_view);
        Design.updateTextFont(doNotShowTextView, Design.FONT_BOLD36);
        doNotShowTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
    }

    public void onBind(Context context, boolean fromSideMenu) {

        String message = context.getString(R.string.create_space_activity_onboarding_message_part_4) +
                "\n\n" +
                context.getString(R.string.create_space_activity_onboarding_message_part_5) +
                "\n\n" +
                context.getString(R.string.create_space_activity_onboarding_message_part_6) +
                "\n\n" +
                context.getString(R.string.create_space_activity_onboarding_message_part_7) +
                "\n\n" +
                context.getString(R.string.create_space_activity_onboarding_message_part_8);
        mMessageTextView.setText(message);

        ViewGroup.LayoutParams layoutParams = mDoNotShowView.getLayoutParams();

        if (fromSideMenu) {
            mCreateSpaceTextView.setText(itemView.getContext().getString(R.string.application_ok));
            mDoNotShowView.setVisibility(View.INVISIBLE);
            layoutParams.height = 1;
        } else {
            mCreateSpaceTextView.setText(itemView.getContext().getString(R.string.create_space_activity_title));
            mDoNotShowView.setVisibility(View.VISIBLE);
            layoutParams.height = (int) (DESIGN_DO_NOT_SHOW_HEIGHT * Design.HEIGHT_RATIO);
        }
    }

    public void onViewRecycled() {

    }
}
