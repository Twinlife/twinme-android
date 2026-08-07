/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.accountActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class UIAccountItem {

    public enum AccountItemType {
        SECTION_TRANSFER,
        TRANSFER_FROM_DEVICE,
        TRANSFER_FROM_OTHER_DEVICE,
        TRANSFER_FROM_TWINME,
        SECTION_BACKUP,
        BACKUP,
        RESTORE,
        VERIFY_BACKUP,
        BACKUPS,
        LAST_BACKUP,
        SECTION_CONVERSATIONS,
        EXPORT,
        CLEANUP,
        SECTION_ACCOUNT,
        DELETE
    }

    private final AccountItemType mType;

    @NonNull
    private final String mText;
    private final int mTextColor;
    private final int mIcon;
    private final int mIconColor;

    @Nullable
    private final Runnable mOnClickListener;

    public UIAccountItem(AccountItemType accountItemType, @NonNull String text, int icon, int textColor, int iconColor, @Nullable Runnable onClickListener) {

        mType = accountItemType;
        mText = text;
        mIcon = icon;
        mTextColor = textColor;
        mIconColor = iconColor;
        mOnClickListener = onClickListener;
    }

    @NonNull
    public String getText() {

        return mText;
    }

    public AccountItemType getType() {

        return mType;
    }

    public int getIcon() {

        return mIcon;
    }

    public int getTextColor() {

        return mTextColor;
    }

    public int getIconColor() {

        return mIconColor;
    }

    @Nullable
    public Runnable getOnClickListener() {

        return mOnClickListener;
    }
}