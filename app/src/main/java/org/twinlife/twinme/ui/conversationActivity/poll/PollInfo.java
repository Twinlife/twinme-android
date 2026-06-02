/*
 *  Copyright (c) 2026 twinlife SA.
 *
 *  All Rights Reserved.
 *
 *  Contributors:
 *   Romain Kolb (romain.kolb@skyrock.com)
 */

package org.twinlife.twinme.ui.conversationActivity.poll;

import androidx.annotation.NonNull;

import org.twinlife.twinlife.ConversationService;

import java.io.Serializable;
import java.util.List;

public class PollInfo implements Serializable {
    public final boolean multipleAnswersAllowed;
    @NonNull
    public final String question;
    @NonNull
    public final List<ConversationService.PollDescriptor.Choice> choices;

    public PollInfo(boolean multipleAnswersAllowed, @NonNull String question, @NonNull List<ConversationService.PollDescriptor.Choice> choices) {
        this.multipleAnswersAllowed = multipleAnswersAllowed;
        this.question = question;
        this.choices = choices;
    }

    @NonNull
    @Override
    public String toString() {
        return "PollInfo{" +
                "multipleAnswersAllowed=" + multipleAnswersAllowed +
                ", question='" + question + '\'' +
                ", choices=" + choices +
                '}';
    }
}
