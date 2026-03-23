/*
 *  Copyright (c) 2024-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.backupActivity;

import androidx.annotation.Nullable;

public class UIBackupWord {

    @Nullable
    private String mWord;

    private final int mPosition;

    public UIBackupWord(@Nullable String word, int position) {

        mWord = word;
        mPosition = position;
    }

    public String getWord() {

        return mWord;
    }

    public void setWord(String word) {

        mWord = word;
    }

    public int getPosition() {

        return mPosition;
    }
}
