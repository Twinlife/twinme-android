/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (fabrice.trescartes@twin.life)
 *   Romain Kolb (romain.kolb@skyrock.com)
 */

package org.twinlife.twinme.ui.conversationActivity.poll;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;

public class CreatePollActivity extends AbstractTwinmeActivity {
    private static final String LOG_TAG = "CreatePollActivity";
    private static final boolean DEBUG = false;

    protected static final int DESIGN_MARGIN = 34;

    protected static final int MAX_QUESTION_LENGTH = 128;
    protected static final int MAX_CHOICE_LENGTH = 32;
    protected static final int LIMIT_CHOICE =  10;

    private CreatePollAdapter mPollAdapter;
    private RecyclerView mRecyclerView;

    private MenuItem mMenuItemSave;

    private String mPollQuestion = "";
    private final List<UIPollChoice> mPollChoices = new ArrayList<>();
    private boolean mAllowMultipleChoice = true;

    private boolean mCanSavePoll = false;

    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        initViews();
    }

    @Override
    protected void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        super.onResume();

    }

    //
    // Override Activity methods
    //

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateOptionsMenu: menu=" + menu);
        }

        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.create_poll_menu, menu);

        mMenuItemSave = menu.findItem(R.id.add_action);

        TextView titleView = (TextView) mMenuItemSave.getActionView();
        String title = String.valueOf(mMenuItemSave.getTitle());

        if (titleView != null) {
            Design.updateTextFont(titleView, Design.FONT_BOLD36);
            titleView.setTextColor(Color.WHITE);
            titleView.setText(title);
            titleView.setAlpha(0.5f);
            titleView.setPadding(0, 0, Design.TOOLBAR_TEXT_ITEM_PADDING, 0);
            titleView.setOnClickListener(view -> onSavePollClick());
        }

        return true;
    }

    protected void addChoice() {
        if (DEBUG) {
            Log.d(LOG_TAG, "addChoice");
        }

        mRecyclerView.post(() -> {
            if (mPollChoices.size() >= LIMIT_CHOICE) {
                return;
            }
            mPollChoices.add(new UIPollChoice(mPollChoices.size(), ""));

            for (UIPollChoice uiPollChoice : mPollChoices) {
                uiPollChoice.setSelected(false);
            }

            mPollChoices.get(mPollChoices.size() - 1).setSelected(true);
            mPollAdapter.updatePollChoices();
        });
    }

    protected String getPollQuestion() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getPollQuestion");
        }

        return mPollQuestion;
    }

    protected void setPollQuestion(String question) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setPollQuestion: question=" + question);
        }

        mPollQuestion = question.trim();
        setUpdated();
        updateViews();
    }

    protected void updatePollChoice(UIPollChoice pollChoice, String choice) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateChoice: pollChoice=" + pollChoice);
        }

        for (UIPollChoice uiPollChoice : mPollChoices) {
            if (uiPollChoice.getPosition() == pollChoice.getPosition()) {
                uiPollChoice.setChoice(choice);
                break;
            }
        }

        setUpdated();
        updateViews();
        mPollAdapter.updateFooter();
    }

    protected void selectPollChoice(UIPollChoice pollChoice, boolean isSelected) {
        if (DEBUG) {
            Log.d(LOG_TAG, "selectPollChoice: pollChoice=" + pollChoice);
        }

        for (UIPollChoice uiPollChoice : mPollChoices) {
            if (uiPollChoice.getPosition() == pollChoice.getPosition()) {
                uiPollChoice.setSelected(isSelected);
            } else {
                uiPollChoice.setSelected(false);
            }
        }

        mPollAdapter.updatePollChoices();
    }

    protected void selectFirstChoice() {
        if (DEBUG) {
            Log.d(LOG_TAG, "selectFirstChoice");
        }

        for (UIPollChoice uiPollChoice : mPollChoices) {
            uiPollChoice.setSelected(false);
        }

        mPollChoices.get(0).setSelected(true);
        mPollAdapter.updatePollChoices();

        setUpdated();
        updateViews();
    }

    protected void selectNextPollChoice(UIPollChoice pollChoice) {
        if (DEBUG) {
            Log.d(LOG_TAG, "selectNextPollChoice: pollChoice=" + pollChoice.getPosition() +  " - " + mPollChoices.size());
        }

        UIPollChoice pollChoiceToUpdate = mPollChoices.get(pollChoice.getPosition());
        pollChoiceToUpdate.setSelected(false);

        if (pollChoice.getPosition() + 1 < mPollChoices.size()) {
            UIPollChoice nextPollChoice = mPollChoices.get(pollChoice.getPosition() + 1);
            nextPollChoice.setSelected(true);
        } else if (pollChoice.getPosition() + 1 == mPollChoices.size()) {
            addChoice();
        }

        mPollAdapter.updatePollChoices();

        setUpdated();
        updateViews();
    }

    protected void setAllowMultipleChoice(boolean allowMultipleChoice) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setAllowMultipleChoice: allowMultipleChoice=" + allowMultipleChoice);
        }

        mAllowMultipleChoice = allowMultipleChoice;
    }

    protected boolean allowMultipleChoice() {

        return mAllowMultipleChoice;
    }

    protected List<UIPollChoice> getPollChoices() {

        return mPollChoices;
    }

    protected int countValidChoices() {
        if (DEBUG) {
            Log.d(LOG_TAG, "countValidChoices");
        }

        int validChoices = 0;

        for (UIPollChoice pollChoice : mPollChoices) {
            if (!pollChoice.getChoice().trim().isEmpty()) {
                validChoices++;
            }
        }

        return validChoices;
    }

    //
    // Private methods
    //

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.create_poll_activity);

        setStatusBarColor();
        setToolBar(R.id.create_poll_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);
        setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        setTitle(getString(R.string.poll_view_title));
        applyInsets(R.id.create_poll_activity_layout, R.id.create_poll_activity_tool_bar, R.id.create_poll_activity_list_view, Design.TOOLBAR_COLOR, false);

        mPollChoices.add(new UIPollChoice(0, ""));
        mPollChoices.add(new UIPollChoice(1, ""));

        mPollAdapter = new CreatePollAdapter(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        mRecyclerView = findViewById(R.id.create_poll_activity_list_view);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mPollAdapter);
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
    }

    private void setUpdated() {
        if (DEBUG) {
            Log.d(LOG_TAG, "setUpdated");
        }

        mCanSavePoll = false;

        if (mPollQuestion.trim().isEmpty()) {
            return;
        }

        int validChoices = 0;

        for (UIPollChoice pollChoice : mPollChoices) {
            if (!pollChoice.getChoice().trim().isEmpty()) {
                validChoices++;
            }
        }

        if (validChoices < 2) {
            return;
        }

        mCanSavePoll = true;
    }

    private void updateViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateViews");
        }

        if (mMenuItemSave == null || mMenuItemSave.getActionView() == null) {
            return;
        }

        CommonUtils.setMenuItem(mMenuItemSave, mCanSavePoll, 0.5f, 1.0f);
    }

    private void onSavePollClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSavePollClick");
        }

        if (!mCanSavePoll) {
            setResult(RESULT_CANCELED, new Intent());
            finish();
            return;
        }

        List<ConversationService.PollDescriptor.Choice> choices = new ArrayList<>();

        for (UIPollChoice pollChoice : mPollChoices) {
            String label = pollChoice.getChoice().trim();
            int position = pollChoice.getPosition();

            if (label.isEmpty()) {
                continue;
            }

            choices.add(new ConversationService.PollDescriptor.Choice(position, label));
        }

        PollInfo pollInfo = new PollInfo(mAllowMultipleChoice, mPollQuestion, choices);
        Intent intent = new Intent();
        intent.putExtra(Intents.INTENT_POLL_INFO, pollInfo);
        setResult(RESULT_OK, intent);
        finish();
    }
}
