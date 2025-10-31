/*
 *  Copyright (c) 2015-2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Thibaud David (contact@thibauddavid.com)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Yannis Le Gal (Yannis.LeGal@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.utils;

import android.content.Context;

import org.twinlife.device.android.twinme.R;

public class Utils extends CommonUtils {
    public static String formatTimeout(Context context, long timeout) {

        long oneMinute = 60;
        long oneHour = oneMinute * 60;
        long oneDay = oneHour * 24;
        long oneWeek = oneDay * 7;
        long oneMonth = oneDay * 30;

        if (timeout == 0) {
            return context.getResources().getString(R.string.privacy_activity_lock_screen_timeout_instant);
        } else if (timeout < oneMinute) {
            return String.format(context.getResources().getString(R.string.application_timeout_seconds), timeout);
        } else if (timeout == oneMinute) {
            return context.getResources().getString(R.string.application_timeout_minute);
        } else if (timeout < oneHour) {
            long minutes = timeout / oneMinute;
            return String.format(context.getResources().getString(R.string.application_timeout_minutes), minutes);
        } else if (timeout == oneHour) {
            return context.getResources().getString(R.string.application_timeout_hour);
        } else if (timeout < oneDay) {
            long hours = timeout / oneHour;
            return String.format(context.getResources().getString(R.string.application_timeout_hours), hours);
        } else if (timeout == oneDay) {
            return context.getResources().getString(R.string.application_timeout_day);
        } else if (timeout == oneWeek) {
            return context.getResources().getString(R.string.application_timeout_week);
        } else if (timeout == oneMonth) {
            return context.getResources().getString(R.string.application_timeout_month);
        }

        return Long.toString(timeout);
    }
}
