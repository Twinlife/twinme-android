/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.externalCallActivity;

public class UITemplateSection extends UITemplateItem {

    private final String mTitle;

    public UITemplateSection(String title) {
        super();

        mTemplateItemType = TemplateItemType.SECTION;
        mTitle = title;
    }

    public String getTitle() {

        return mTitle;
    }

}
