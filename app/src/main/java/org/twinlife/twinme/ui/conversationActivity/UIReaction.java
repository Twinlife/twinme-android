/*
 *  Copyright (c) 2023-2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Romain Kolb (romain.kolb@skyrock.com)
 */

package org.twinlife.twinme.ui.conversationActivity;

import android.graphics.Color;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class UIReaction {

    public enum ReactionType {
        LIKE,
        UNLIKE,
        LOVE,
        CRY,
        HUNGER,
        SURPRISED,
        SCREAMING,
        FIRE;

        // Avoid cloning the values array multiple times per reaction.
        private static final ReactionType[] VALUES = values();
    }

    private ReactionType mReactionType;
    private int mImage;
    private int mColorFilter = Color.TRANSPARENT;

    public UIReaction(ReactionType reactionType, int image) {

        mReactionType = reactionType;
        mImage = image;
    }

    public UIReaction(int reaction) {

        if (reaction < 0 || reaction >= ReactionType.VALUES.length) {
            mImage = R.drawable.reaction_unknown;
            mColorFilter = Design.BLACK_COLOR;
        } else {
            initTypeAndImage(ReactionType.VALUES[reaction]);
        }
    }

    private void initTypeAndImage(ReactionType reactionType) {

        switch (reactionType) {
            case LIKE:
                mReactionType = ReactionType.LIKE;
                mImage = R.drawable.reaction_like;
                break;

            case UNLIKE:
                mReactionType = ReactionType.UNLIKE;
                mImage = R.drawable.reaction_unlike;
                break;

            case LOVE:
                mReactionType = ReactionType.LOVE;
                mImage = R.drawable.reaction_love;
                break;

            case CRY:
                mReactionType = ReactionType.CRY;
                mImage = R.drawable.reaction_cry;
                break;

            case HUNGER:
                mReactionType = ReactionType.HUNGER;
                mImage = R.drawable.reaction_hunger;
                break;

            case SURPRISED:
                mReactionType = ReactionType.SURPRISED;
                mImage = R.drawable.reaction_surprised;
                break;

            case SCREAMING:
                mReactionType = ReactionType.SCREAMING;
                mImage = R.drawable.reaction_screaming;
                break;

            case FIRE:
                mReactionType = ReactionType.FIRE;
                mImage = R.drawable.reaction_fire;
                break;

            default:
                mImage = R.drawable.reaction_unknown;
                mColorFilter = Design.BLACK_COLOR;
                break;
        }
    }

    public int getImage() {

        return mImage;
    }

    public int getColorFilter() {

        return mColorFilter;
    }

    public ReactionType getReactionType() {

        return mReactionType;
    }

    static public ReactionType getReactionTypeWithInt(int reactionType) {

        return ReactionType.VALUES[reactionType];
    }

    public static int getNotificationImageReactionWithReactionType(int reactionType) {

        if (reactionType < 0 || reactionType >= ReactionType.VALUES.length) {
            return R.drawable.reaction_unknown;
        }

        int image;

        switch (getReactionTypeWithInt(reactionType)) {
            case LIKE:
                image = R.drawable.notification_reaction_like;
                break;

            case UNLIKE:
                image = R.drawable.notification_reaction_unlike;
                break;

            case LOVE:
                image = R.drawable.notification_reaction_love;
                break;

            case CRY:
                image = R.drawable.notification_reaction_cry;
                break;

            case HUNGER:
                image = R.drawable.notification_reaction_hunger;
                break;

            case SURPRISED:
                image = R.drawable.notification_reaction_surprised;
                break;

            case SCREAMING:
                image = R.drawable.notification_reaction_screaming;
                break;

            case FIRE:
                image = R.drawable.notification_reaction_fire;
                break;

            default:
                image = R.drawable.reaction_unknown;
                break;
        }

        return image;
    }

    public static int getColorFilterReactionWithReactionType(int reactionType) {

        if (reactionType < 0 || reactionType >= ReactionType.values().length) {
           return Design.BLACK_COLOR;
        }

        return Color.TRANSPARENT;
    }
}
