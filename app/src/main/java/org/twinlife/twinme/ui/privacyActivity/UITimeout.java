/*
 *  Copyright (c) 2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.privacyActivity;

import android.content.Context;

import org.twinlife.device.android.twinme.R;

public class UITimeout {

    private final String mText;
    private final int mDelay;

    public UITimeout(String text, int delay) {

        mText = text;
        mDelay = delay;
    }

    public String getText() {

        return mText;
    }

    public int getDelay() {

        return mDelay;
    }

     public static String getDelay(Context context, int delay) {

        if (delay == 0) {
            return context.getResources().getString(org.twinlife.device.android.twinme.R.string.privacy_activity_lock_screen_timeout_instant);
        } else if (delay == 10) {
            return String.format(context.getResources().getString(R.string.application_timeout_seconds), 10);
        } else if (delay == 30) {
            return String.format(context.getResources().getString(R.string.application_timeout_seconds), 30);
        } else if (delay == 60) {
            return context.getResources().getString(org.twinlife.device.android.twinme.R.string.application_timeout_minute);
        } else if (delay == 5 * 60) {
            return String.format(context.getResources().getString(R.string.application_timeout_minutes), 5);
        } else if (delay == 15 * 60) {
            return String.format(context.getResources().getString(R.string.application_timeout_minutes), 15);
        } else if (delay == 30 * 60) {
            return String.format(context.getResources().getString(R.string.application_timeout_minutes), 30);
        } else if (delay == 60 * 60) {
            return context.getResources().getString(org.twinlife.device.android.twinme.R.string.application_timeout_hour);
        } else if (delay == 4 * 60 * 60) {
            return String.format(context.getResources().getString(R.string.application_timeout_hours), 4);
        } else if (delay == 24 * 60 * 60) {
            return context.getResources().getString(org.twinlife.device.android.twinme.R.string.application_timeout_day);
        } else if (delay == 7 * 24 * 60 * 60) {
            return context.getResources().getString(org.twinlife.device.android.twinme.R.string.application_timeout_week);
        } else if (delay == 30 * 24 * 60 * 60) {
            return context.getResources().getString(org.twinlife.device.android.twinme.R.string.application_timeout_month);
        } else {
            return "";
        }
    }
}
