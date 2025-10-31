/*
 *  Copyright (c) 2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.users;

import android.content.Context;
import android.graphics.Color;

import org.twinlife.device.android.twinme.R;

public class UIContactTag {

    public enum ContactTag {
        PENDING,
        REVOKED
    }

    private final ContactTag mContactTag;
    private String mTitle;
    private int mBackgroundColor;
    private int mForegroundColor;

    public UIContactTag(Context context, ContactTag contactTag) {

        mContactTag = contactTag;

        initTagInfo(context);
    }

    public String getTitle() {

        return mTitle;
    }

    public int getBackgroundColor() {

        return mBackgroundColor;
    }

    public int getForegroundColor() {

        return mForegroundColor;
    }

    private void initTagInfo(Context context) {

        switch (mContactTag) {
            case PENDING:
                mTitle = context.getString(R.string.show_contact_activity_pending);
                mBackgroundColor = Color.argb(30, 255, 147, 0);
                mForegroundColor = Color.argb(255, 255, 147, 0);
                break;

            case REVOKED:
                mTitle = context.getString(R.string.show_contact_activity_revoked);
                mBackgroundColor = Color.argb(30, 253, 96, 93);
                mForegroundColor = Color.argb(255, 253, 96, 93);
                break;

            default:
                mTitle = "";
                mBackgroundColor = 0;
                mForegroundColor = 0;
                break;
        }
    }


    /*
    - (void)initTagInfo {

    switch (self.contactTag) {
        case ContactTagPending:
            self.title = TwinmeLocalizedString(@"show_contact_activity_pending", nil);
            self.backgroundColor = [UIColor colorWithRed:255./255. green:147./255. blue:0./255. alpha:0.12];
            self.foregroundColor = [UIColor colorWithRed:255./255. green:147./255. blue:0./255. alpha:1];
            break;

        case ContactTagRevoked:
            self.title = TwinmeLocalizedString(@"show_contact_activity_revoked", nil);
            self.backgroundColor = [UIColor colorWithRed:253./255. green:96./255. blue:93./255. alpha:0.12];
            self.foregroundColor = [UIColor colorWithRed:253./255. green:96./255. blue:93./255. alpha:1];
            break;

        default:
            break;
    }
}
     */
}
