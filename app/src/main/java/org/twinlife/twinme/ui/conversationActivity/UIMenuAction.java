/*
 *  Copyright (c) 2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.conversationActivity;

public class UIMenuAction {

    public enum ActionType {
        COPY,
        INFO,
        DELETE,
        FORWARD,
        REPLY,
        SAVE,
        SHARE,
        SELECT_MORE,
        EDIT
    }

    private final String mTitle;
    private final int mImage;
    private final ActionType mActionType;
    private final boolean mEnabledAction;

    public UIMenuAction(String title, int image, ActionType actionType, boolean enabledAction) {

        mTitle = title;
        mImage = image;
        mActionType = actionType;
        mEnabledAction = enabledAction;
    }

    public String getTitle() {

        return mTitle;
    }

    public int getImage() {

        return mImage;
    }

    public ActionType getActionType() {

        return mActionType;
    }

    public boolean getEnabledAction() {

        return mEnabledAction;
    }
}

