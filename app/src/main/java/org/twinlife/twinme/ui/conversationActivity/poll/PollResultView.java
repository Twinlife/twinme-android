/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (fabrice.trescartes@twin.life)
 */

package org.twinlife.twinme.ui.conversationActivity.poll;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.conversationActivity.ConversationActivity;
import org.twinlife.twinme.utils.AbstractBottomSheetView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PollResultView extends AbstractBottomSheetView {
    private static final String LOG_TAG = "BackupContentCon...";
    private static final boolean DEBUG = false;

    public PollResultView(Context context) {
        super(context);
    }

    public PollResultView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (DEBUG) {
            Log.d(LOG_TAG, "create");
        }

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.poll_result_view, this, true);
        initViews();
    }

    public void initWithResults(ConversationActivity conversationActivity, List<UIPollResult> pollResults) {
        if (DEBUG) {
            Log.d(LOG_TAG, "initWithResults: " + pollResults);
        }

        List<PollResultItem> items = getPollResultItems(pollResults);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        PollResultAdapter pollResultAdapter = new PollResultAdapter(conversationActivity, new ArrayList<>());
        RecyclerView recyclerView = findViewById(R.id.poll_result_view_list_view);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setBackgroundColor(Color.TRANSPARENT);
        recyclerView.setAdapter(pollResultAdapter);
        pollResultAdapter.setItems(items);
    }

    @Override
    protected void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        mOverlayView = findViewById(R.id.poll_result_view_overlay_view);
        mActionView = findViewById(R.id.poll_result_view_action_view);
        mSlideMarkView = findViewById(R.id.poll_result_view_slide_mark_view);
        mTitleView = findViewById(R.id.poll_result_view_title_view);

        super.initViews();

        Design.updateTextFont(mTitleView, Design.FONT_MEDIUM34);
    }

    @NonNull
    private static List<PollResultItem> getPollResultItems(List<UIPollResult> pollResults) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getPollResultItems");
        }

        pollResults.sort(Comparator.comparingInt(UIPollResult::getCount).reversed());

        List<PollResultItem> items = new ArrayList<>();

        for (UIPollResult pollResult : pollResults) {
            String title = pollResult.getChoice().label + " (" + pollResult.getCount() + ")";
            PollResultItem pollChoiceItem = new PollResultItem(PollResultItem.PollResultItemType.POLL_RESULT_CHOICE, title, null);
            items.add(pollChoiceItem);
            for (UIPollResult.UIPollResultVoter voter : pollResult.getPollResultVoters()) {
                if (voter.getName() != null) {
                    PollResultItem pollVoterItem = new PollResultItem(PollResultItem.PollResultItemType.POLL_RESULT_VOTER, voter.getName(), voter.getAvatar());
                    items.add(pollVoterItem);
                }
            }
        }
        return items;
    }
}