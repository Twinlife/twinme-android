/*
 *  Copyright (c) 2022 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.utils.coachmark;

import org.twinlife.twinme.ui.Settings;

import java.util.Date;

public class CoachMarkManager {

    private static final int COACH_MARK_MAX_SHOW = 3;
    private static final int COACH_MARK_INTERVAL_DATE = 60 * 60 * 24;

    public boolean showCoachMark() {

        return Settings.showCoachMark.getBoolean();
    }

    public void setShowCoachMark(boolean showCoachMark) {

        Settings.showCoachMark.setBoolean(showCoachMark).save();

        if (showCoachMark) {
            resetCoachMark();
        } else {
            hideAllCoachMark();
        }
    }

    public boolean showCoachMark(CoachMark.CoachMarkTag coachMarkTag) {

        return false;
        /*if (!showCoachMark()) {
            return false;
        }

        long lastShowDate = lastShowCoachMarkDate(coachMarkTag);
        int showCount = countShowCoachMark(coachMarkTag);

        if (lastShowDate == 0) {
            setLastShowCoachMark(coachMarkTag);
            setCountShowCoachMark(coachMarkTag, showCount + 1);
            return true;
        } else {
            long timeInterval = new Date().getTime() / 1000;
            long diffTimeSinceLastDate = timeInterval - lastShowDate;

            if (diffTimeSinceLastDate > COACH_MARK_INTERVAL_DATE || showCount > COACH_MARK_MAX_SHOW) {
                setLastShowCoachMark(coachMarkTag);
                setCountShowCoachMark(coachMarkTag, showCount + 1);
                return true;
            }
        }

        return false;*/
    }

    private long lastShowCoachMarkDate(CoachMark.CoachMarkTag coachMarkTag) {

        if (coachMarkTag == CoachMark.CoachMarkTag.CONVERSATION_EPHEMERAL) {
            return Settings.lastShowCoachMarkConversationEphemeral.getLong();
        } else if (coachMarkTag == CoachMark.CoachMarkTag.ADD_PARTICIPANT_TO_CALL) {
            return Settings.lastShowCoachMarkAddParticipantToCall.getLong();
        } else if (coachMarkTag == CoachMark.CoachMarkTag.CONTACT_CAPABILITIES) {
            return Settings.lastShowCoachMarkContactCapabilities.getLong();
        } else if (coachMarkTag == CoachMark.CoachMarkTag.PRIVACY) {
            return Settings.lastShowCoachMarkPrivacy.getLong();
        }

        return 0;
    }

    private void setLastShowCoachMark(CoachMark.CoachMarkTag coachMarkTag) {

        long timeInterval = new Date().getTime() / 1000;

        if (coachMarkTag == CoachMark.CoachMarkTag.CONVERSATION_EPHEMERAL) {
            Settings.lastShowCoachMarkConversationEphemeral.setLong(timeInterval).save();
        } else if (coachMarkTag == CoachMark.CoachMarkTag.ADD_PARTICIPANT_TO_CALL) {
            Settings.lastShowCoachMarkAddParticipantToCall.setLong(timeInterval).save();
        } else if (coachMarkTag == CoachMark.CoachMarkTag.CONTACT_CAPABILITIES) {
            Settings.lastShowCoachMarkContactCapabilities.setLong(timeInterval).save();
        } else if (coachMarkTag == CoachMark.CoachMarkTag.PRIVACY) {
            Settings.lastShowCoachMarkPrivacy.setLong(timeInterval).save();
        }
    }

    private int countShowCoachMark(CoachMark.CoachMarkTag coachMarkTag) {

        if (coachMarkTag == CoachMark.CoachMarkTag.CONVERSATION_EPHEMERAL) {
            return Settings.countShowCoachMarkConversationEphemeral.getInt();
        } else if (coachMarkTag == CoachMark.CoachMarkTag.ADD_PARTICIPANT_TO_CALL) {
            return Settings.countShowCoachMarkAddParticipantToCall.getInt();
        } else if (coachMarkTag == CoachMark.CoachMarkTag.CONTACT_CAPABILITIES) {
            return Settings.countShowCoachMarkContactCapabilities.getInt();
        } else if (coachMarkTag == CoachMark.CoachMarkTag.PRIVACY) {
            return Settings.countShowCoachMarkPrivacy.getInt();
        }

        return 0;
    }

    private void setCountShowCoachMark(CoachMark.CoachMarkTag coachMarkTag, int count) {

        if (coachMarkTag == CoachMark.CoachMarkTag.CONVERSATION_EPHEMERAL) {
            Settings.countShowCoachMarkConversationEphemeral.setInt(count).save();
        } else if (coachMarkTag == CoachMark.CoachMarkTag.ADD_PARTICIPANT_TO_CALL) {
            Settings.countShowCoachMarkAddParticipantToCall.setInt(count).save();
        } else if (coachMarkTag == CoachMark.CoachMarkTag.CONTACT_CAPABILITIES) {
            Settings.countShowCoachMarkContactCapabilities.setInt(count).save();
        } else if (coachMarkTag == CoachMark.CoachMarkTag.PRIVACY) {
            Settings.countShowCoachMarkPrivacy.setInt(count).save();
        }

        int countShowCoachMark = countShowCoachMark(CoachMark.CoachMarkTag.CONTACT_CAPABILITIES);

        if (countShowCoachMark >= COACH_MARK_MAX_SHOW) {
            countShowCoachMark = countShowCoachMark(CoachMark.CoachMarkTag.PRIVACY);
            if (countShowCoachMark >= COACH_MARK_MAX_SHOW) {
                countShowCoachMark = countShowCoachMark(CoachMark.CoachMarkTag.CONVERSATION_EPHEMERAL);
                if (countShowCoachMark >= COACH_MARK_MAX_SHOW) {
                    countShowCoachMark = countShowCoachMark(CoachMark.CoachMarkTag.ADD_PARTICIPANT_TO_CALL);
                    if (countShowCoachMark >= COACH_MARK_MAX_SHOW) {
                        setShowCoachMark(false);
                    }
                }
            }
        }
    }

    public void resetCoachMark() {

        Settings.lastShowCoachMarkConversationEphemeral.setLong(0).save();
        Settings.lastShowCoachMarkAddParticipantToCall.setLong(0).save();
        Settings.lastShowCoachMarkContactCapabilities.setLong(0).save();
        Settings.lastShowCoachMarkPrivacy.setLong(0).save();

        Settings.countShowCoachMarkConversationEphemeral.setInt(0).save();
        Settings.countShowCoachMarkAddParticipantToCall.setInt(0).save();
        Settings.countShowCoachMarkContactCapabilities.setInt(0).save();
        Settings.countShowCoachMarkPrivacy.setInt(0).save();
    }

    public void hideAllCoachMark() {

        Settings.countShowCoachMarkConversationEphemeral.setInt(COACH_MARK_MAX_SHOW).save();
        Settings.countShowCoachMarkAddParticipantToCall.setInt(COACH_MARK_MAX_SHOW).save();
        Settings.countShowCoachMarkContactCapabilities.setInt(COACH_MARK_MAX_SHOW).save();
        Settings.countShowCoachMarkPrivacy.setInt(COACH_MARK_MAX_SHOW).save();
    }

    public void hideCoachMark(CoachMark.CoachMarkTag coachMarkTag) {

        if (coachMarkTag == CoachMark.CoachMarkTag.CONVERSATION_EPHEMERAL) {
            Settings.countShowCoachMarkConversationEphemeral.setInt(COACH_MARK_MAX_SHOW).save();
        } else if (coachMarkTag == CoachMark.CoachMarkTag.ADD_PARTICIPANT_TO_CALL) {
            Settings.countShowCoachMarkAddParticipantToCall.setInt(COACH_MARK_MAX_SHOW).save();
        } else if (coachMarkTag == CoachMark.CoachMarkTag.CONTACT_CAPABILITIES) {
            Settings.countShowCoachMarkContactCapabilities.setInt(COACH_MARK_MAX_SHOW).save();
        } else if (coachMarkTag == CoachMark.CoachMarkTag.PRIVACY) {
            Settings.countShowCoachMarkPrivacy.setInt(COACH_MARK_MAX_SHOW).save();
        }
    }
}
