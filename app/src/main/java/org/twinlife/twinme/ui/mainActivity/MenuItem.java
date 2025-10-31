/*
 *  Copyright (c) 2020-2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.mainActivity;

public class MenuItem {

    public enum MenuItemAction {

        NO_ACTION, HELP, ABOUT_TWINME, ACCOUNT, SIGN_OUT, PERSONALIZATION, SOUND_SETTINGS, PRIVACY, TRANSFER_CALL, SETTINGS_ADVANCED, MESSAGE_SETTINGS, PROFILE, UPGRADE
    }

    public enum MenuItemLevel {

        LEVEL0, LEVEL1, LEVEL2
    }

    public static final int TYPE_LEVEL0 = 0;
    public static final int TYPE_LEVEL1 = 1;
    public static final int TYPE_LEVEL2 = 2;

    private final MenuItemLevel mLevel;
    private final int mText;
    private final MenuItemAction mAction;

    MenuItem(MenuItemLevel level, int text, MenuItemAction action) {

        mLevel = level;
        mText = text;
        mAction = action;
    }

    public MenuItemLevel getLevel() {

        return mLevel;
    }

    public int getText() {

        return mText;
    }

    public MenuItemAction getAction() {

        return mAction;
    }
}
