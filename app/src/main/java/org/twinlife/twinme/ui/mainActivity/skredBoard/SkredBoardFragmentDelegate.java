/*
 *  Copyright (c) 2017 twinlife SA & Telefun SAS.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Thibaud David (contact@thibauddavid.com)
 */

package org.twinlife.twinme.ui.mainActivity.skredBoard;

import androidx.annotation.NonNull;

public interface SkredBoardFragmentDelegate {

    void skredboardDidValidateCode(SkredBoardFragment.SkredboardMode mode, @NonNull String code);
}
