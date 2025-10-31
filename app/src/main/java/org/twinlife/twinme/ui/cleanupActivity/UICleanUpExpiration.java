/*
 *  Copyright (c) 2023-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.cleanupActivity;

import android.annotation.SuppressLint;
import android.content.Context;

import org.twinlife.device.android.twinme.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class UICleanUpExpiration {

    public enum ExpirationType {
        ALL,
        VALUE,
        DATE
    }

    public enum ExpirationPeriod {
        ONE_DAY,
        ONE_WEEK,
        ONE_MONTH,
        THREE_MONTHS,
        SIX_MONTHS,
        ONE_YEAR
    }

    private ExpirationType mExpirationType;
    private ExpirationPeriod mExpirationPeriod;
    private Date mExpirationDate;

    public UICleanUpExpiration(ExpirationType expirationType, ExpirationPeriod expirationPeriod) {

        mExpirationType = expirationType;
        mExpirationPeriod = expirationPeriod;
    }

    public UICleanUpExpiration(ExpirationType expirationType, Date date) {

        mExpirationType = expirationType;
        mExpirationDate = date;
    }

    public ExpirationType getExpirationType() {

        return mExpirationType;
    }

    public void setExpirationType(ExpirationType expirationType) {

        mExpirationType = expirationType;
    }

    public ExpirationPeriod getExpirationPeriod() {

        return mExpirationPeriod;
    }

    public void setExpirationPeriod(ExpirationPeriod expirationPeriod) {

        mExpirationPeriod = expirationPeriod;
    }

    public Date getExpirationDate() {

        return mExpirationDate;
    }

    public void setExpirationDate(Date date) {

        mExpirationDate = date;
    }

    public String getTitle(Context context) {

        String  title = "";
        switch (mExpirationType) {
            case ALL:
                title = context.getString(R.string.cleanup_activity_all);
                break;

            case DATE:
                title = context.getString(R.string.cleanup_activity_prior_to);
                break;

            case VALUE:
                title = context.getString(R.string.cleanup_activity_older_than);
                break;
        }

        return title;
    }

    @SuppressLint("StringFormatInvalid")
    public String getValue(Context context) {

        String  value = "";

        if (mExpirationType == ExpirationType.VALUE) {
            switch (mExpirationPeriod) {
                case ONE_DAY:
                    value = context.getString(R.string.application_timeout_day);
                    break;

                case ONE_WEEK:
                    value = context.getString(R.string.application_timeout_week);
                    break;

                case ONE_MONTH:
                    value = context.getString(R.string.application_timeout_month);
                    break;

                case THREE_MONTHS:
                    value = String.format(context.getString(R.string.cleanup_activity_month), 3);
                    break;

                case SIX_MONTHS:
                    value = String.format(context.getString(R.string.cleanup_activity_month), 6);
                    break;

                case ONE_YEAR:
                    value = context.getString(R.string.cleanup_activity_one_year);
                    break;

            }
        } else if (mExpirationType == ExpirationType.DATE) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
            value = simpleDateFormat.format(mExpirationDate);
        }

        return value;
    }

    public long getClearDate() {

        long clearDate = System.currentTimeMillis();
        if (mExpirationType == ExpirationType.VALUE) {
            Date now = new Date();
            Calendar calendar = Calendar.getInstance();
            Calendar date = Calendar.getInstance();
            date.setTimeInMillis(now.getTime());

            switch (mExpirationPeriod) {
                case ONE_DAY:
                    calendar.add(Calendar.DATE, -1);
                    break;

                case ONE_WEEK:
                    calendar.add(Calendar.DATE, -7);
                    break;

                case ONE_MONTH:
                    calendar.add(Calendar.MONTH, -1);
                    break;

                case THREE_MONTHS:
                    calendar.add(Calendar.MONTH, -3);
                    break;

                case SIX_MONTHS:
                    calendar.add(Calendar.MONTH, -6);
                    break;

                case ONE_YEAR:
                    calendar.add(Calendar.YEAR, -1);
                    break;

            }
            calendar.add(Calendar.DATE, -1);

            mExpirationDate = calendar.getTime();
            clearDate = mExpirationDate.getTime();
        } else if (mExpirationType == ExpirationType.DATE && mExpirationDate != null) {
            clearDate = mExpirationDate.getTime();
        }

        return clearDate;
    }
}
