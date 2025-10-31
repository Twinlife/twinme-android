/*
 *  Copyright (c) 2023-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.externalCallActivity;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class OnboardingExternalCallViewHolder extends RecyclerView.ViewHolder {

    private static final float DESIGN_CREATE_BUTTON_MARGIN = 20;
    private static final float DESIGN_IMAGE_HEIGHT = 320;
    private static final float DESIGN_IMAGE_MARGIN = 100;
    private static final float DESIGN_DO_NOT_SHOW_HEIGHT = 140f;

    private static final int IMAGE_MARGIN;

    private static final int CREATE_BUTTON_MARGIN;

    static {
        IMAGE_MARGIN = (int) (DESIGN_IMAGE_MARGIN * Design.HEIGHT_RATIO);
        CREATE_BUTTON_MARGIN = (int) (DESIGN_CREATE_BUTTON_MARGIN * Design.HEIGHT_RATIO);
    }

    private final ImageView mImageView;
    private final TextView mMessageTextView;
    private final View mCreateView;
    private final TextView mCreateTextView;
    private final View mDoNotShowView;

    OnboardingExternalCallViewHolder(OnboardingExternalCallActivity onboardingExternalCallActivity, @NonNull View view) {

        super(view);

        mImageView = view.findViewById(R.id.onboarding_external_call_activity_item_image);

        ViewGroup.LayoutParams layoutParams = mImageView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_IMAGE_HEIGHT * Design.HEIGHT_RATIO);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mImageView.getLayoutParams();
        marginLayoutParams.bottomMargin = IMAGE_MARGIN;

        mMessageTextView = view.findViewById(R.id.onboarding_external_call_activity_item_message);
        Design.updateTextFont(mMessageTextView, Design.FONT_MEDIUM32);
        mMessageTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mMessageTextView.setMovementMethod(new ScrollingMovementMethod());

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mMessageTextView.getLayoutParams();
        marginLayoutParams.leftMargin = Design.ONBOARDING_TEXT_MARGIN;
        marginLayoutParams.rightMargin = Design.ONBOARDING_TEXT_MARGIN;

        mCreateView = view.findViewById(R.id.onboarding_external_call_activity_item_create_view);
        mCreateView.setOnClickListener(v -> onboardingExternalCallActivity.onCreateExternalCallClick());

        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable createViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        createViewBackground.getPaint().setColor(Design.getMainStyle());
        ViewCompat.setBackground(mCreateView, createViewBackground);

        layoutParams = mCreateView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = Design.BUTTON_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mCreateView.getLayoutParams();
        marginLayoutParams.topMargin = CREATE_BUTTON_MARGIN;

        mCreateTextView = view.findViewById(R.id.onboarding_external_call_activity_item_create_text_view);
        Design.updateTextFont(mCreateTextView, Design.FONT_BOLD36);
        mCreateTextView.setTextColor(Color.WHITE);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mCreateTextView.getLayoutParams();
        marginLayoutParams.leftMargin = Design.BUTTON_MARGIN;
        marginLayoutParams.rightMargin = Design.BUTTON_MARGIN;

        mDoNotShowView = view.findViewById(R.id.onboarding_external_call_activity_item_do_not_show_view);
        mDoNotShowView.setOnClickListener(v -> onboardingExternalCallActivity.onDoNotShowAgainClick());

        layoutParams = mDoNotShowView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_DO_NOT_SHOW_HEIGHT * Design.HEIGHT_RATIO);

        TextView doNotShowTextView = view.findViewById(R.id.onboarding_external_call_activity_item_do_not_show_title_view);
        Design.updateTextFont(doNotShowTextView, Design.FONT_BOLD36);
        doNotShowTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
    }

    public void onBind(UIOnboarding uiOnboarding, boolean fromSideMenu) {

        Bitmap bitmap = BitmapFactory.decodeResource(itemView.getResources(), uiOnboarding.getImageId());
        mImageView.setImageBitmap(bitmap);
        mMessageTextView.setText(uiOnboarding.getMessage());

        ViewGroup.LayoutParams layoutParams = mDoNotShowView.getLayoutParams();

        if (uiOnboarding.hideAction()) {
            mCreateView.setVisibility(View.GONE);
            mDoNotShowView.setVisibility(View.GONE);
        } else {
            mCreateView.setVisibility(View.VISIBLE);
            if (fromSideMenu) {
                mCreateTextView.setText(itemView.getContext().getString(R.string.application_ok));
                mDoNotShowView.setVisibility(View.INVISIBLE);
                layoutParams.height = 0;
            } else {
                mCreateTextView.setText(itemView.getContext().getString(R.string.calls_fragment_create_link));
                mDoNotShowView.setVisibility(View.VISIBLE);
                layoutParams.height = (int) (DESIGN_DO_NOT_SHOW_HEIGHT * Design.HEIGHT_RATIO);
            }
        }
    }

    public void onViewRecycled() {

    }
}