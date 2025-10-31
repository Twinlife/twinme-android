/*
 *  Copyright (c) 2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.settingsActivity;

import android.content.Context;
import android.content.res.Configuration;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.skin.DisplayMode;
import org.twinlife.twinme.ui.Settings;

public class UIQuality {

    public enum QualityOfServicesStep {
        ONE,
        TWO,
        THREE
    }

    private String mMessage;
    private int mImage;
    private final QualityOfServicesStep mQualityOfServicesStep;

    public UIQuality(Context context, QualityOfServicesStep qualityOfServicesStep) {

        mQualityOfServicesStep = qualityOfServicesStep;
        initQualityOfServicesInformation(context);
    }

    public String getMessage() {

        return mMessage;
    }

    public int getImage() {

        return mImage;
    }

    public boolean hideAction() {

        return mQualityOfServicesStep != QualityOfServicesStep.TWO;
    }

    private void initQualityOfServicesInformation(Context context) {

        boolean darkMode = false;
        int currentNightMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        int displayMode = Settings.displayMode.getInt();
        if ((currentNightMode == Configuration.UI_MODE_NIGHT_YES && displayMode == DisplayMode.SYSTEM.ordinal())  || displayMode == DisplayMode.DARK.ordinal()) {
            darkMode = true;
        }

        switch (mQualityOfServicesStep) {
            case ONE:
                mMessage = context.getString(R.string.quality_of_service_activity_step1_message);
                mImage = darkMode ? R.drawable.onboarding_step2_dark : R.drawable.onboarding_step2;
                break;

            case TWO:
                mMessage = context.getString(R.string.quality_of_service_activity_step2_message);
                mImage = darkMode ? R.drawable.quality_service_step2_dark : R.drawable.quality_service_step2;
                break;

            case THREE:
                mMessage = context.getString(R.string.quality_of_service_activity_step3_message);
                mImage = R.drawable.quality_service_step3;
                break;
        }
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
