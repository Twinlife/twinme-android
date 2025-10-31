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
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class OnboardingSpaceFirstPartViewHolder extends RecyclerView.ViewHolder {

    private static final int DESIGN_SHADOW_COLOR = Color.argb(51, 0, 0, 0);
    private static final int DESIGN_SHADOW_OFFSET = 6;
    private static final int DESIGN_SHADOW_RADIUS = 12;

    private static final float DESIGN_SAMPLE_VIEW_WIDTH = 420;
    private static final float DESIGN_SAMPLE_VIEW_HEIGHT = 120;
    private static final float DESIGN_SAMPLE_TOP_MARGIN = 60;
    private static final float DESIGN_SAMPLE_PADDING = 12;
    private static final float DESIGN_MESSAGE_TOP_MARGIN = 50;
    private static final float DESIGN_MESSAGE_BOTTOM_MARGIN = 30;
    private static final int SAMPLE_VIEW_WIDTH;
    private static final int SAMPLE_VIEW_HEIGHT;
    private static final int SAMPLE_TOP_MARGIN;
    private static final int SAMPLE_PADDING;
    private static final int MESSAGE_TOP_MARGIN;
    private static final int MESSAGE_BOTTOM_MARGIN;

    static {
        SAMPLE_VIEW_WIDTH = (int) (DESIGN_SAMPLE_VIEW_WIDTH * Design.WIDTH_RATIO);
        SAMPLE_VIEW_HEIGHT = (int) (DESIGN_SAMPLE_VIEW_HEIGHT * Design.HEIGHT_RATIO);
        SAMPLE_TOP_MARGIN = (int) (DESIGN_SAMPLE_TOP_MARGIN * Design.HEIGHT_RATIO);
        SAMPLE_PADDING = (int) (DESIGN_SAMPLE_PADDING * Design.HEIGHT_RATIO);
        MESSAGE_TOP_MARGIN = (int) (DESIGN_MESSAGE_TOP_MARGIN * Design.HEIGHT_RATIO);
        MESSAGE_BOTTOM_MARGIN = (int) (DESIGN_MESSAGE_BOTTOM_MARGIN * Design.HEIGHT_RATIO);
    }

    private final TextView mMessageTextView;
    
    OnboardingSpaceFirstPartViewHolder(Context context, @NonNull View view) {

        super(view);

        View friendsSpaceContainerView = view.findViewById(R.id.onboarding_space_activity_first_part_item_sample_space_friends_container_view);
        friendsSpaceContainerView.setPadding(SAMPLE_PADDING,SAMPLE_PADDING,SAMPLE_PADDING,SAMPLE_PADDING);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) friendsSpaceContainerView.getLayoutParams();
        marginLayoutParams.topMargin = SAMPLE_TOP_MARGIN;

        View friendsSpaceView = view.findViewById(R.id.onboarding_space_activity_first_part_item_sample_space_friends_view);
        ViewGroup.LayoutParams layoutParams = friendsSpaceView.getLayoutParams();
        layoutParams.width = SAMPLE_VIEW_WIDTH;
        layoutParams.height = SAMPLE_VIEW_HEIGHT;

        float radius = Design.POPUP_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable spaceViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        spaceViewBackground.getPaint().setColor(Design.POPUP_BACKGROUND_COLOR);
        spaceViewBackground.getPaint().setShadowLayer(DESIGN_SHADOW_RADIUS, 0, DESIGN_SHADOW_OFFSET, DESIGN_SHADOW_COLOR);
        friendsSpaceView.setBackground(spaceViewBackground);

        TextView friendsSpaceTextView = view.findViewById(R.id.onboarding_space_activity_first_part_item_sample_space_friends_text_view);
        Design.updateTextFont(friendsSpaceTextView, Design.FONT_MEDIUM34);
        friendsSpaceTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(context.getString(R.string.spaces_activity_sample_friends));
        spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_DEFAULT), 0, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.append("\n");
        int startName = spannableStringBuilder.length();
        spannableStringBuilder.append(context.getString(R.string.spaces_activity_sample_friends_name));
        spannableStringBuilder.setSpan(new RelativeSizeSpan(0.94f), startName, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_GREY), startName, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        friendsSpaceTextView.setText(spannableStringBuilder);

        View familySpaceContainerView = view.findViewById(R.id.onboarding_space_activity_first_part_item_sample_space_family_container_view);
        familySpaceContainerView.setPadding(SAMPLE_PADDING,SAMPLE_PADDING,SAMPLE_PADDING,SAMPLE_PADDING);

        View familySpaceView = view.findViewById(R.id.onboarding_space_activity_first_part_item_sample_space_family_view);

        layoutParams = familySpaceView.getLayoutParams();
        layoutParams.width = SAMPLE_VIEW_WIDTH;
        layoutParams.height = SAMPLE_VIEW_HEIGHT;

        familySpaceView.setBackground(spaceViewBackground);

        TextView familySpaceTextView = view.findViewById(R.id.onboarding_space_activity_first_part_item_sample_space_family_text_view);
        Design.updateTextFont(familySpaceTextView, Design.FONT_MEDIUM34);
        familySpaceTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(context.getString(R.string.spaces_activity_sample_family));
        spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_DEFAULT), 0, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.append("\n");
        startName = spannableStringBuilder.length();
        spannableStringBuilder.append(context.getString(R.string.spaces_activity_sample_family_name));
        spannableStringBuilder.setSpan(new RelativeSizeSpan(0.94f), startName, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_GREY), startName, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        familySpaceTextView.setText(spannableStringBuilder);

        View businessSpaceContainerView = view.findViewById(R.id.onboarding_space_activity_first_part_item_sample_space_business_container_view);
        businessSpaceContainerView.setPadding(SAMPLE_PADDING,SAMPLE_PADDING,SAMPLE_PADDING,SAMPLE_PADDING);

        View businessSpaceView = view.findViewById(R.id.onboarding_space_activity_first_part_item_sample_space_business_view);

        layoutParams = businessSpaceView.getLayoutParams();
        layoutParams.width = SAMPLE_VIEW_WIDTH;
        layoutParams.height = SAMPLE_VIEW_HEIGHT;

        businessSpaceView.setBackground(spaceViewBackground);

        TextView businessSpaceTextView = view.findViewById(R.id.onboarding_space_activity_first_part_item_sample_space_business_text_view);
        Design.updateTextFont(businessSpaceTextView, Design.FONT_MEDIUM34);
        businessSpaceTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(context.getString(R.string.spaces_activity_sample_business));
        spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_DEFAULT), 0, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.append("\n");
        startName = spannableStringBuilder.length();
        spannableStringBuilder.append(context.getString(R.string.spaces_activity_sample_business_name));
        spannableStringBuilder.setSpan(new RelativeSizeSpan(0.94f), startName, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_GREY), startName, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        businessSpaceTextView.setText(spannableStringBuilder);

        mMessageTextView = view.findViewById(R.id.onboarding_space_activity_first_part_item_message_view);
        Design.updateTextFont(mMessageTextView, Design.FONT_MEDIUM32);
        mMessageTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mMessageTextView.getLayoutParams();
        marginLayoutParams.topMargin = MESSAGE_TOP_MARGIN;
        marginLayoutParams.bottomMargin = MESSAGE_BOTTOM_MARGIN;
        marginLayoutParams.leftMargin = Design.ONBOARDING_TEXT_MARGIN;
        marginLayoutParams.rightMargin = Design.ONBOARDING_TEXT_MARGIN;
    }

    public void onBind(Context context) {

        String message = context.getString(R.string.spaces_activity_message);
        mMessageTextView.setText(message);
    }

    public void onViewRecycled() {

    }
}