/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.externalCallActivity;

public abstract class UITemplateItem {

    public enum TemplateItemType {
        SECTION,
        TEMPLATE
    }

    private static int sItemId = 0;

    private final long mItemId;

    protected TemplateItemType mTemplateItemType;

    protected UITemplateItem() {

        mItemId = sItemId++;
    }

    public long getItemId() {

        return mItemId;
    }

    public TemplateItemType getTemplateItemType() {

        return mTemplateItemType;
    }
}
