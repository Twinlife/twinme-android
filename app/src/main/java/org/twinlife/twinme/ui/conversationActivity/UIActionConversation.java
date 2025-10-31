/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.conversationActivity;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.skin.DisplayMode;
import org.twinlife.twinme.ui.Settings;
import org.twinlife.twinme.utils.Utils;

public class UIActionConversation {

    public enum ConversationActionType {
        FILE,
        GALLERY,
        MANAGE_CONVERSATION,
        MEDIAS_AND_FILES,
        PHOTO,
        RESET,
        VIDEO
    }

    private String mTitle;
    private int mIcon;
    private int mIconColor;
    private final ConversationActionType mConversationActionType;

    public UIActionConversation(Context context, ConversationActionType conversationActionType) {

        mConversationActionType = conversationActionType;
        initAction(context);
    }

    public String getTitle() {

        return mTitle;
    }

    public int getIcon() {

        return mIcon;
    }

    public int getIconColor() {

        return mIconColor;
    }

    public ConversationActionType getConversationActionType() {

        return mConversationActionType;
    }

    private void initAction(Context context) {

        boolean darkMode = false;
        int currentNightMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        int displayMode = Settings.displayMode.getInt();
        if ((currentNightMode == Configuration.UI_MODE_NIGHT_YES && displayMode == DisplayMode.SYSTEM.ordinal())  || displayMode == DisplayMode.DARK.ordinal()) {
            darkMode = true;
        }

        switch (mConversationActionType) {
            case FILE:
                mTitle = Utils.capitalizeString(context.getString(R.string.export_activity_files));
                mIcon = R.drawable.toolbar_file_grey;
                mIconColor = Color.rgb(200, 200, 200);
                break;

            case GALLERY:
                mTitle = context.getString(R.string.application_photo_gallery);
                mIcon = R.drawable.toolbar_picture_grey;
                mIconColor = Color.rgb(241, 154, 55);
                break;

            case MANAGE_CONVERSATION:
                mTitle = context.getString(R.string.conversation_activity_manage_conversation);
                mIcon = R.drawable.settings_icon;
                mIconColor = darkMode ? Color.rgb(230, 230, 230) : Color.rgb(110, 110, 110);
                break;

            case MEDIAS_AND_FILES:
                mTitle = context.getString(R.string.conversation_files_activity_title);
                mIcon = R.drawable.select_file;
                mIconColor = Color.rgb(78, 171, 241);
                break;

            case PHOTO:
                mTitle = context.getString(R.string.conversation_activity_photo_camera);
                mIcon = R.drawable.toolbar_camera_grey;
                mIconColor = Color.rgb(112, 212, 174);
                break;

            case RESET:
                mTitle = context.getString(R.string.main_activity_reset_conversation_title);
                mIcon = R.drawable.action_bar_delete;
                mIconColor = Design.DELETE_COLOR_RED;
                break;

            case VIDEO:
                mTitle = context.getString(R.string.conversation_activity_video_camera);
                mIcon = R.drawable.history_video_call;
                mIconColor = Color.rgb(179, 104, 216);
                break;



            default:
                break;
        }
    }
}
