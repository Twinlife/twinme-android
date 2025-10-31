/*
 *  Copyright (c) 2020-2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Romain Kolb (romain.kolb@skyrock.com)
 */

package org.twinlife.twinme.ui.spaces;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;

import org.twinlife.twinme.models.SpaceSettings;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.skin.DisplayMode;

import java.util.UUID;

public class CustomAppearance {

    public static final String PROPERTY_CONVERSATION_BACKGROUND_COLOR = "ConversationBackgroundColor";
    public static final String PROPERTY_CONVERSATION_BACKGROUND_IMAGE = "ConversationBackgroundImage";
    public static final String PROPERTY_CONVERSATION_BACKGROUND_TEXT = "ConversationBackgroundText";
    public static final String PROPERTY_MESSAGE_BACKGROUND_COLOR = "MessageBackgroundColor";
    public static final String PROPERTY_PEER_MESSAGE_BACKGROUND_COLOR = "PeerMessageBackgroundColor";
    public static final String PROPERTY_MESSAGE_BORDER_COLOR = "MessageBorderColor";
    public static final String PROPERTY_PEER_MESSAGE_BORDER_COLOR = "PeerMessageBorderColor";
    public static final String PROPERTY_MESSAGE_TEXT_COLOR = "MessageTextColor";
    public static final String PROPERTY_PEER_MESSAGE_TEXT_COLOR = "PeerMessageTextColor";
    public static final String PROPERTY_DARK_CONVERSATION_BACKGROUND_COLOR = "DarkConversationBackgroundColor";
    public static final String PROPERTY_DARK_CONVERSATION_BACKGROUND_IMAGE = "DarkConversationBackgroundImage";
    public static final String PROPERTY_DARK_CONVERSATION_BACKGROUND_TEXT = "DarkConversationBackgroundText";
    public static final String PROPERTY_DARK_MESSAGE_BACKGROUND_COLOR = "DarkMessageBackgroundColor";
    public static final String PROPERTY_DARK_PEER_MESSAGE_BACKGROUND_COLOR = "DarkPeerMessageBackgroundColor";
    public static final String PROPERTY_DARK_MESSAGE_BORDER_COLOR = "DarkMessageBorderColor";
    public static final String PROPERTY_DARK_PEER_MESSAGE_BORDER_COLOR = "DarkPeerMessageBorderColor";
    public static final String PROPERTY_DARK_MESSAGE_TEXT_COLOR = "DarkMessageTextColor";
    public static final String PROPERTY_DARK_PEER_MESSAGE_TEXT_COLOR = "DarkPeerMessageTextColor";

    SpaceSettings mSpaceSettings;

    DisplayMode mDisplayMode;

    public CustomAppearance(Context context, SpaceSettings spaceSettings) {

        mSpaceSettings = spaceSettings;
        DisplayMode mode = Design.getDisplayMode(Integer.parseInt(mSpaceSettings.getString(SpaceSettingProperty.PROPERTY_DISPLAY_MODE, DisplayMode.SYSTEM.ordinal() + "")));
        setCurrentMode(context, mode);
    }

    public CustomAppearance() {

        mSpaceSettings = new SpaceSettings("Space");
        mDisplayMode = DisplayMode.LIGHT;
    }

    public SpaceSettings getSpaceSettings() {

        return mSpaceSettings;
    }

    public DisplayMode getCurrentMode() {

        return mDisplayMode;
    }

    public int getMainColor() {

        return Design.getDefaultColor(mSpaceSettings.getStyle());
    }

    public int getConversationBackgroundColor() {

        if (mDisplayMode == DisplayMode.LIGHT) {
            return mSpaceSettings.getColor(PROPERTY_CONVERSATION_BACKGROUND_COLOR, Design.CONVERSATION_BACKGROUND_COLOR);
        }

        return mSpaceSettings.getColor(PROPERTY_DARK_CONVERSATION_BACKGROUND_COLOR, Design.CONVERSATION_BACKGROUND_COLOR);
    }

    public int getConversationBackgroundDefaultColor() {

        return  Design.CONVERSATION_BACKGROUND_COLOR;
    }

    public UUID getConversationBackgroundImageId() {

        if (mDisplayMode == DisplayMode.LIGHT) {
            return mSpaceSettings.getUUID(PROPERTY_CONVERSATION_BACKGROUND_IMAGE);
        }

        return mSpaceSettings.getUUID(PROPERTY_DARK_CONVERSATION_BACKGROUND_IMAGE);
    }

    public UUID getConversationBackgroundImageId(DisplayMode mode) {

        if (mode == DisplayMode.LIGHT) {
            return mSpaceSettings.getUUID(PROPERTY_CONVERSATION_BACKGROUND_IMAGE);
        }

        return mSpaceSettings.getUUID(PROPERTY_DARK_CONVERSATION_BACKGROUND_IMAGE);
    }

    public int getConversationBackgroundText() {

        if (mDisplayMode == DisplayMode.LIGHT) {
            return mSpaceSettings.getColor(PROPERTY_CONVERSATION_BACKGROUND_TEXT, Design.TIME_COLOR);
        }

        return mSpaceSettings.getColor(PROPERTY_DARK_CONVERSATION_BACKGROUND_TEXT, Design.TIME_COLOR);
    }

    public int getConversationBackgroundTextDefaultColor() {

        return Design.TIME_COLOR;
    }

    public int getMessageBackgroundColor() {

        if (mDisplayMode == DisplayMode.LIGHT) {
            return mSpaceSettings.getColor(PROPERTY_MESSAGE_BACKGROUND_COLOR, Design.getItemBackgroundColor());
        }

        return mSpaceSettings.getColor(PROPERTY_DARK_MESSAGE_BACKGROUND_COLOR, Design.getItemBackgroundColor());
    }

    public int getMessageBackgroundDefaultColor() {

        return Design.getItemBackgroundColor();
    }

    public int getPeerMessageBackgroundColor() {

        if (mDisplayMode == DisplayMode.LIGHT) {
            return mSpaceSettings.getColor(PROPERTY_PEER_MESSAGE_BACKGROUND_COLOR,  Color.WHITE);
        }

        return mSpaceSettings.getColor(PROPERTY_DARK_PEER_MESSAGE_BACKGROUND_COLOR, Color.parseColor("#484848"));
    }

    public int getPeerMessageBackgroundDefaultColor() {

        if (mDisplayMode == DisplayMode.LIGHT) {
            return Color.WHITE;
        }

        return Color.parseColor("#484848");
    }

    public int getMessageBorderColor() {

        if (mDisplayMode == DisplayMode.LIGHT) {
            return mSpaceSettings.getColor(PROPERTY_MESSAGE_BORDER_COLOR, Color.TRANSPARENT);
        }

        return mSpaceSettings.getColor(PROPERTY_DARK_MESSAGE_BORDER_COLOR, Color.TRANSPARENT);
    }

    public int getMessageBorderDefaultColor() {

        return Color.TRANSPARENT;
    }

    public int getPeerMessageBorderColor() {

        if (mDisplayMode == DisplayMode.LIGHT) {
            return mSpaceSettings.getColor(PROPERTY_PEER_MESSAGE_BORDER_COLOR, Color.TRANSPARENT);
        }

        return mSpaceSettings.getColor(PROPERTY_DARK_PEER_MESSAGE_BORDER_COLOR, Color.TRANSPARENT);
    }

    public int getPeerMessageBorderDefaultColor() {

        return Color.TRANSPARENT;
    }

    public int getMessageTextColor() {

        if (mDisplayMode == DisplayMode.LIGHT) {
            return mSpaceSettings.getColor(PROPERTY_MESSAGE_TEXT_COLOR, Color.WHITE);
        }

        return mSpaceSettings.getColor(PROPERTY_DARK_MESSAGE_TEXT_COLOR, Color.WHITE);
    }

    public int getMessageTextDefaultColor() {

        return  Color.WHITE;
    }

    public int getPeerMessageTextColor() {

        if (mDisplayMode == DisplayMode.LIGHT) {
            return mSpaceSettings.getColor(PROPERTY_PEER_MESSAGE_TEXT_COLOR, Color.BLACK);
        }

        return mSpaceSettings.getColor(PROPERTY_DARK_PEER_MESSAGE_TEXT_COLOR, Color.parseColor("#ffffff"));
    }

    public int getPeerMessageTextDefaultColor() {

        if (mDisplayMode == DisplayMode.LIGHT) {
            return Color.BLACK;
        }

        return Color.parseColor("#ffffff");
    }

    public void setCurrentMode(Context context, DisplayMode mode) {

        if (mode == DisplayMode.SYSTEM) {
            int currentNightMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
                mode = DisplayMode.DARK;
            } else {
                mode = DisplayMode.LIGHT;
            }
        }

        mDisplayMode = mode;
    }

    public void setMainColor(String color) {

        if (color == null) {
            color = Design.DEFAULT_COLOR;
        }
        mSpaceSettings.setStyle(color);
    }

    public void setConversationBackgroundColor(String hexColor) {

        if (hexColor == null) {
            if (mDisplayMode == DisplayMode.LIGHT) {
                mSpaceSettings.remove(PROPERTY_CONVERSATION_BACKGROUND_COLOR);
            } else {
                mSpaceSettings.remove(PROPERTY_DARK_CONVERSATION_BACKGROUND_COLOR);
            }
            return;
        }

        int color = Color.parseColor(hexColor);

        if (mDisplayMode == DisplayMode.LIGHT) {
            mSpaceSettings.setColor(PROPERTY_CONVERSATION_BACKGROUND_COLOR, color);
        } else {
            mSpaceSettings.setColor(PROPERTY_DARK_CONVERSATION_BACKGROUND_COLOR, color);
        }
    }

    public void setConversationBackgroundColor(int color) {

        if (mDisplayMode == DisplayMode.LIGHT) {
            mSpaceSettings.setColor(PROPERTY_CONVERSATION_BACKGROUND_COLOR, color);
        } else {
            mSpaceSettings.setColor(PROPERTY_DARK_CONVERSATION_BACKGROUND_COLOR, color);
        }
    }

    public void setConversationBackgroundText(String hexColor) {

        if (hexColor == null) {
            if (mDisplayMode == DisplayMode.LIGHT) {
                mSpaceSettings.remove(PROPERTY_CONVERSATION_BACKGROUND_TEXT);
            } else {
                mSpaceSettings.remove(PROPERTY_DARK_CONVERSATION_BACKGROUND_TEXT);
            }
            return;
        }

        int color = Color.parseColor(hexColor);

        if (mDisplayMode == DisplayMode.LIGHT) {
            mSpaceSettings.setColor(PROPERTY_CONVERSATION_BACKGROUND_TEXT, color);
        } else {
            mSpaceSettings.setColor(PROPERTY_DARK_CONVERSATION_BACKGROUND_TEXT, color);
        }
    }

    public void setConversationBackgroundText(int color) {

        if (mDisplayMode == DisplayMode.LIGHT) {
            mSpaceSettings.setColor(PROPERTY_CONVERSATION_BACKGROUND_TEXT, color);
        } else {
            mSpaceSettings.setColor(PROPERTY_DARK_CONVERSATION_BACKGROUND_TEXT, color);
        }
    }

    public void setMessageBackgroundColor(String hexColor) {

        if (hexColor == null) {
            if (mDisplayMode == DisplayMode.LIGHT) {
                mSpaceSettings.remove(PROPERTY_MESSAGE_BACKGROUND_COLOR);
            } else {
                mSpaceSettings.remove(PROPERTY_DARK_MESSAGE_BACKGROUND_COLOR);
            }
            return;
        }

        int color = Color.parseColor(hexColor);

        if (mDisplayMode == DisplayMode.LIGHT) {
            mSpaceSettings.setColor(PROPERTY_MESSAGE_BACKGROUND_COLOR, color);
        } else {
            mSpaceSettings.setColor(PROPERTY_DARK_MESSAGE_BACKGROUND_COLOR, color);
        }
    }

    public void setMessageBackgroundColor(int color) {

        if (mDisplayMode == DisplayMode.LIGHT) {
            mSpaceSettings.setColor(PROPERTY_MESSAGE_BACKGROUND_COLOR, color);
        } else {
            mSpaceSettings.setColor(PROPERTY_DARK_MESSAGE_BACKGROUND_COLOR, color);
        }
    }

    public void setPeerMessageBackgroundColor(String hexColor) {

        if (hexColor == null) {
            if (mDisplayMode == DisplayMode.LIGHT) {
                mSpaceSettings.remove(PROPERTY_PEER_MESSAGE_BACKGROUND_COLOR);
            } else {
                mSpaceSettings.remove(PROPERTY_DARK_PEER_MESSAGE_BACKGROUND_COLOR);
            }
            return;
        }

        int color = Color.parseColor(hexColor);

        if (mDisplayMode == DisplayMode.LIGHT) {
            mSpaceSettings.setColor(PROPERTY_PEER_MESSAGE_BACKGROUND_COLOR, color);
        } else {
            mSpaceSettings.setColor(PROPERTY_DARK_PEER_MESSAGE_BACKGROUND_COLOR, color);
        }
    }

    public void setPeerMessageBackgroundColor(int color) {

        if (mDisplayMode == DisplayMode.LIGHT) {
            mSpaceSettings.setColor(PROPERTY_PEER_MESSAGE_BACKGROUND_COLOR, color);
        } else {
            mSpaceSettings.setColor(PROPERTY_DARK_PEER_MESSAGE_BACKGROUND_COLOR, color);
        }
    }

    public void setMessageBorderColor(String hexColor) {

        if (hexColor == null) {
            if (mDisplayMode == DisplayMode.LIGHT) {
                mSpaceSettings.remove(PROPERTY_MESSAGE_BORDER_COLOR);
            } else {
                mSpaceSettings.remove(PROPERTY_DARK_MESSAGE_BORDER_COLOR);
            }
            return;
        }

        int color = Color.parseColor(hexColor);

        if (mDisplayMode == DisplayMode.LIGHT) {
            mSpaceSettings.setColor(PROPERTY_MESSAGE_BORDER_COLOR, color);
        } else {
            mSpaceSettings.setColor(PROPERTY_DARK_MESSAGE_BORDER_COLOR, color);
        }
    }

    public void setMessageBorderColor(int color) {

        if (mDisplayMode == DisplayMode.LIGHT) {
            mSpaceSettings.setColor(PROPERTY_MESSAGE_BORDER_COLOR, color);
        } else {
            mSpaceSettings.setColor(PROPERTY_DARK_MESSAGE_BORDER_COLOR, color);
        }
    }

    public void setPeerMessageBorderColor(String hexColor) {

        if (hexColor == null) {
            if (mDisplayMode == DisplayMode.LIGHT) {
                mSpaceSettings.remove(PROPERTY_PEER_MESSAGE_BORDER_COLOR);
            } else {
                mSpaceSettings.remove(PROPERTY_DARK_PEER_MESSAGE_BORDER_COLOR);
            }
            return;
        }

        int color = Color.parseColor(hexColor);

        if (mDisplayMode == DisplayMode.LIGHT) {
            mSpaceSettings.setColor(PROPERTY_PEER_MESSAGE_BORDER_COLOR, color);
        } else {
            mSpaceSettings.setColor(PROPERTY_DARK_PEER_MESSAGE_BORDER_COLOR, color);
        }
    }

    public void setPeerMessageBorderColor(int color) {

        if (mDisplayMode == DisplayMode.LIGHT) {
            mSpaceSettings.setColor(PROPERTY_PEER_MESSAGE_BORDER_COLOR, color);
        } else {
            mSpaceSettings.setColor(PROPERTY_DARK_PEER_MESSAGE_BORDER_COLOR, color);
        }
    }

    public void setMessageTextColor(String hexColor) {

        if (hexColor == null) {
            if (mDisplayMode == DisplayMode.LIGHT) {
                mSpaceSettings.remove(PROPERTY_MESSAGE_TEXT_COLOR);
            } else {
                mSpaceSettings.remove(PROPERTY_DARK_MESSAGE_TEXT_COLOR);
            }
            return;
        }

        int color = Color.parseColor(hexColor);

        if (mDisplayMode == DisplayMode.LIGHT) {
            mSpaceSettings.setColor(PROPERTY_MESSAGE_TEXT_COLOR, color);
        } else {
            mSpaceSettings.setColor(PROPERTY_DARK_MESSAGE_TEXT_COLOR, color);
        }
    }

    public void setMessageTextColor(int color) {

        if (mDisplayMode == DisplayMode.LIGHT) {
            mSpaceSettings.setColor(PROPERTY_MESSAGE_TEXT_COLOR, color);
        } else {
            mSpaceSettings.setColor(PROPERTY_DARK_MESSAGE_TEXT_COLOR, color);
        }
    }

    public void setPeerMessageTextColor(String hexColor) {

        if (hexColor == null) {
            if (mDisplayMode == DisplayMode.LIGHT) {
                mSpaceSettings.remove(PROPERTY_PEER_MESSAGE_TEXT_COLOR);
            } else {
                mSpaceSettings.remove(PROPERTY_DARK_PEER_MESSAGE_TEXT_COLOR);
            }
            return;
        }

        int color = Color.parseColor(hexColor);
        if (mDisplayMode == DisplayMode.LIGHT) {
            mSpaceSettings.setColor(PROPERTY_PEER_MESSAGE_TEXT_COLOR, color);
        } else {
            mSpaceSettings.setColor(PROPERTY_DARK_PEER_MESSAGE_TEXT_COLOR, color);
        }
    }

    public void setPeerMessageTextColor(int color) {

        if (mDisplayMode == DisplayMode.LIGHT) {
            mSpaceSettings.setColor(PROPERTY_PEER_MESSAGE_TEXT_COLOR, color);
        } else {
            mSpaceSettings.setColor(PROPERTY_DARK_PEER_MESSAGE_TEXT_COLOR, color);
        }
    }

    public void resetToDefaultValues() {

        setMainColor(Design.DEFAULT_COLOR);
        mSpaceSettings.remove(PROPERTY_CONVERSATION_BACKGROUND_COLOR);
        mSpaceSettings.remove(PROPERTY_CONVERSATION_BACKGROUND_IMAGE);
        mSpaceSettings.remove(PROPERTY_CONVERSATION_BACKGROUND_TEXT);
        mSpaceSettings.remove(PROPERTY_MESSAGE_BACKGROUND_COLOR);
        mSpaceSettings.remove(PROPERTY_PEER_MESSAGE_BACKGROUND_COLOR);
        mSpaceSettings.remove(PROPERTY_MESSAGE_BORDER_COLOR);
        mSpaceSettings.remove(PROPERTY_PEER_MESSAGE_BORDER_COLOR);
        mSpaceSettings.remove(PROPERTY_MESSAGE_TEXT_COLOR);
        mSpaceSettings.remove(PROPERTY_PEER_MESSAGE_TEXT_COLOR);
        mSpaceSettings.remove(PROPERTY_DARK_CONVERSATION_BACKGROUND_COLOR);
        mSpaceSettings.remove(PROPERTY_DARK_CONVERSATION_BACKGROUND_IMAGE);
        mSpaceSettings.remove(PROPERTY_DARK_CONVERSATION_BACKGROUND_TEXT);
        mSpaceSettings.remove(PROPERTY_DARK_MESSAGE_BACKGROUND_COLOR);
        mSpaceSettings.remove(PROPERTY_DARK_PEER_MESSAGE_BACKGROUND_COLOR);
        mSpaceSettings.remove(PROPERTY_DARK_MESSAGE_BORDER_COLOR);
        mSpaceSettings.remove(PROPERTY_DARK_PEER_MESSAGE_BORDER_COLOR);
        mSpaceSettings.remove(PROPERTY_DARK_MESSAGE_TEXT_COLOR);
        mSpaceSettings.remove(PROPERTY_DARK_PEER_MESSAGE_TEXT_COLOR);
    }
}
