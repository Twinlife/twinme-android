/*
 *  Copyright (c) 2023-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.externalCallActivity;

import android.content.Context;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import androidx.annotation.Nullable;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class UIOnboarding {

    public enum OnboardingType {
        PART_ONE,
        PART_TWO,
        PART_THREE,
        PART_FOUR
    }

    private final OnboardingType mOnboardingType;

    private static int sItemId = 0;

    private final long mItemId;
    @Nullable
    private String mMessage;
    private int mImageId;

    private final boolean mHideAction;

    public UIOnboarding(Context context, OnboardingType onboardingType, boolean hideAction) {

        mItemId = sItemId++;

        mOnboardingType = onboardingType;
        mHideAction = hideAction;

        initOnboardingInformation(context);
    }

    public long getItemId() {

        return mItemId;
    }

    public OnboardingType getOnboardingType() {

        return mOnboardingType;
    }

    public String getMessage() {

        return mMessage;
    }

    public int getImageId() {

        return mImageId;
    }

    public boolean hideAction() {

        return mHideAction;
    }

    private void initOnboardingInformation(Context context) {

        StringBuilder stringBuilder = new StringBuilder();

        switch (mOnboardingType) {
            case PART_ONE:
                stringBuilder.append(context.getString(R.string.create_external_call_activity_onboarding_part_1_message_1));
                stringBuilder.append("\n\n");
                stringBuilder.append(context.getString(R.string.create_external_call_activity_onboarding_part_1_message_2));
                stringBuilder.append("\n\n");
                stringBuilder.append("    • ");
                stringBuilder.append(context.getString(R.string.create_external_call_activity_onboarding_part_1_message_3));
                stringBuilder.append("\n");
                stringBuilder.append("    • ");
                stringBuilder.append(context.getString(R.string.create_external_call_activity_onboarding_part_1_message_4));
                stringBuilder.append("\n");
                stringBuilder.append("    • ");
                stringBuilder.append(context.getString(R.string.create_external_call_activity_onboarding_part_1_message_5));
                stringBuilder.append("\n");
                stringBuilder.append("    • ");
                stringBuilder.append(context.getString(R.string.create_external_call_activity_onboarding_part_1_message_6));
                mImageId = R.drawable.onboarding_click_to_call;
                break;

            case PART_TWO:
                stringBuilder.append(context.getString(R.string.create_external_call_activity_onboarding_part_2_message_1));
                stringBuilder.append("\n\n");
                stringBuilder.append("    • ");
                stringBuilder.append(context.getString(R.string.create_external_call_activity_onboarding_part_2_message_2));
                stringBuilder.append("\n");
                stringBuilder.append("    • ");
                stringBuilder.append(context.getString(R.string.create_external_call_activity_onboarding_part_2_message_3));
                stringBuilder.append("\n");
                stringBuilder.append("    • ");
                stringBuilder.append(context.getString(R.string.create_external_call_activity_onboarding_part_2_message_4));
                mImageId = R.drawable.onboarding_click_to_call_2;
                break;

            case PART_THREE:
                stringBuilder.append(context.getString(R.string.create_external_call_activity_onboarding_part_3_message_1));
                stringBuilder.append("\n\n");
                stringBuilder.append("    • ");
                stringBuilder.append(context.getString(R.string.create_external_call_activity_onboarding_part_3_message_2));
                stringBuilder.append("\n");
                stringBuilder.append("    • ");
                stringBuilder.append(context.getString(R.string.create_external_call_activity_onboarding_part_3_message_3));
                stringBuilder.append("\n");
                stringBuilder.append("    • ");
                stringBuilder.append(context.getString(R.string.create_external_call_activity_onboarding_part_3_message_4));
                stringBuilder.append("\n");
                stringBuilder.append("    • ");
                stringBuilder.append(context.getString(R.string.create_external_call_activity_onboarding_part_3_message_5));
                mImageId = R.drawable.onboarding_click_to_call_3;
                break;

            case PART_FOUR:
                stringBuilder.append(context.getString(R.string.create_external_call_activity_onboarding_part_4_message_1));
                stringBuilder.append("\n\n");
                stringBuilder.append("    • ");
                stringBuilder.append(context.getString(R.string.create_external_call_activity_onboarding_part_4_message_2));
                stringBuilder.append("\n");
                stringBuilder.append("    • ");
                stringBuilder.append(context.getString(R.string.create_external_call_activity_onboarding_part_4_message_3));
                stringBuilder.append("\n\n");
                stringBuilder.append(context.getString(R.string.create_external_call_activity_onboarding_part_4_message_4));
                mImageId = R.drawable.onboarding_click_to_call_4;
                break;

            default:
                break;
        }

        mMessage = stringBuilder.toString();
    }

    public float getMessageHeight(int width) {

        if (mMessage == null) {
            return 0;
        }

        TextPaint textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(Design.FONT_MEDIUM32.size);
        textPaint.setTypeface(Design.FONT_MEDIUM32.typeface);

        Layout.Alignment alignment = Layout.Alignment.ALIGN_NORMAL;
        StaticLayout staticLayout = new StaticLayout(mMessage, textPaint, width, alignment, 1, 0, false);
        return staticLayout.getHeight();
    }
}