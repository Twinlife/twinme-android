/*
 *  Copyright (c) 2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.conversationFilesActivity;

import android.content.Context;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.ui.baseItemActivity.Item;
import org.twinlife.twinme.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UIFileSection {

    private static int sItemId = 0;

    private final long mItemId;

    private final String mPeriod;

    private String mTitle;

    private final List<Item> mItems = new ArrayList<>();

    public UIFileSection(String period) {

        mItemId = sItemId++;

        mPeriod = period;
    }

    public long getItemId() {

        return mItemId;
    }

    public String getPeriod() {

        return mPeriod;
    }

    public String getTitle(Context context) {

        if (mTitle != null) {
            return mTitle;
        }

        if (mItems.size() == 0) {
            return "";
        }

        Item item = mItems.get(0);

        Calendar itemDay = Calendar.getInstance();
        Calendar today = Calendar.getInstance();
        itemDay.setTimeInMillis(item.getCreatedTimestamp());

        if (itemDay.get(Calendar.YEAR) == today.get(Calendar.YEAR) && itemDay.get(Calendar.MONTH) == today.get(Calendar.MONTH)) {
            return context.getString(R.string.conversation_files_activity_month);
        }

        final SimpleDateFormat simpleDateFormat;
        if (itemDay.get(Calendar.YEAR) == today.get(Calendar.YEAR)) {
            simpleDateFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
        } else {
            simpleDateFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        }

        mTitle = Utils.capitalizeString(simpleDateFormat.format(new Date(item.getCreatedTimestamp())));

        return mTitle;
    }

    public int getCount() {

        return mItems.size();
    }

    public List<Item> getItems() {

        return mItems;
    }

    public void addItem(Item item) {

        mItems.add(item);
    }
}