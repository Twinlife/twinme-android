/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (fabrice.trescartes@twin.life)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.conversationActivity.poll.PollChoiceAdapter;
import org.twinlife.twinme.ui.conversationActivity.poll.PollChoiceViewHolder;
import org.twinlife.twinme.ui.conversationActivity.poll.UIPollResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PeerPollItemViewHolder extends PeerItemViewHolder {

    private static final float DESIGN_RESULT_HEIGHT = 80f;
    private static final float DESIGN_HORIZONTAL_MARGIN = 10f;
    private static final float DESIGN_VERTICAL_MARGIN = 14f;

    private final View mPollContainer;
    private final TextView mQuestionView;
    private final PollChoiceAdapter mChoicesAdapter;
    private final RecyclerView mChoicesView;

    private final GradientDrawable mGradientDrawable;
    PeerPollItemViewHolder(BaseItemActivity baseItemActivity, View view) {

        super(baseItemActivity, view,
                R.id.base_item_activity_peer_poll_item_container,
                R.id.base_item_activity_peer_poll_item_avatar,
                R.id.base_item_activity_peer_poll_item_overlay_view,
                R.id.base_item_activity_peer_poll_item_annotation_view,
                R.id.base_item_activity_peer_poll_item_selected_view,
                R.id.base_item_activity_peer_poll_item_selected_image_view);

        mPollContainer = view.findViewById(R.id.base_item_activity_peer_poll_item_view);
        mPollContainer.setPadding(FILE_ITEM_WIDTH_PADDING, FILE_ITEM_HEIGHT_PADDING, FILE_ITEM_WIDTH_PADDING, 0);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mPollContainer.getLayoutParams();
        if (baseItemActivity.displayPeerItemAvatar()) {
            marginLayoutParams.setMarginStart(Design.PEER_CONTENT_CONVERSATION_MARGIN + Design.PEER_AVATAR_CONVERSATION_MARGIN + BaseItemActivity.AVATAR_HEIGHT);
        } else {
            marginLayoutParams.setMarginStart(Design.PEER_AVATAR_CONVERSATION_MARGIN);
        }
        mPollContainer.setLayoutParams(marginLayoutParams);

        mGradientDrawable = new GradientDrawable();
        mGradientDrawable.mutate();
        mGradientDrawable.setColor(Design.GREY_ITEM_COLOR);
        mGradientDrawable.setShape(GradientDrawable.RECTANGLE);
        mPollContainer.setBackground(mGradientDrawable);
        mGradientDrawable.setStroke(Design.BORDER_WIDTH, Color.TRANSPARENT);
        mPollContainer.setClickable(false);

        mPollContainer.setOnLongClickListener(v -> {
            baseItemActivity.onItemLongPress(getItem());
            return true;
        });

        mQuestionView = view.findViewById(R.id.base_item_activity_peer_poll_item_question_view);
        Design.updateTextFont(mQuestionView, Design.FONT_REGULAR34);
        mQuestionView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mQuestionView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_VERTICAL_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.leftMargin = (int) (DESIGN_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
        mQuestionView.setLayoutParams(marginLayoutParams);

        mChoicesAdapter = new PollChoiceAdapter(Design.FONT_COLOR_DEFAULT);

        mChoicesView = view.findViewById(R.id.base_item_activity_peer_poll_item_choices_view);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(baseItemActivity, LinearLayoutManager.VERTICAL, false);
        mChoicesView.setLayoutManager(linearLayoutManager);
        mChoicesView.setItemViewCacheSize(Design.ITEM_LIST_CACHE_SIZE);
        mChoicesView.setItemAnimator(null);
        mChoicesView.setAdapter(mChoicesAdapter);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mChoicesView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_VERTICAL_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.leftMargin = (int) (DESIGN_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
        mChoicesView.setLayoutParams(marginLayoutParams);

        View resultView = view.findViewById(R.id.base_item_activity_peer_poll_item_results_view);

        ViewGroup.LayoutParams layoutParams = resultView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_RESULT_HEIGHT * Design.HEIGHT_RATIO);
        resultView.setLayoutParams(layoutParams);

        resultView.setOnClickListener(v -> {
            ConversationService.PollDescriptor pollDescriptor = getPollItem().getPollDescriptor();
            if (pollDescriptor != null) {
                getBaseItemActivity().onPollResultClick(pollDescriptor);
            }
        });

        TextView resultTextView = resultView.findViewById(R.id.base_item_activity_peer_poll_item_results_text_view);
        Design.updateTextFont(resultTextView, Design.FONT_REGULAR30);
        resultTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) resultTextView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
        resultTextView.setLayoutParams(marginLayoutParams);
    }

    @Override
    void onBind(Item item) {

        if (!(item instanceof PeerPollItem)) {
            return;
        }
        super.onBind(item);

        mGradientDrawable.setCornerRadii(getCornerRadii());

        PeerPollItem peerPollItem = (PeerPollItem) item;

        ConversationService.PollDescriptor pollDescriptor = peerPollItem.getPollDescriptor();
        mQuestionView.setText(pollDescriptor.getQuestion());
        mChoicesAdapter.setChoices(getPollResults(pollDescriptor, peerPollItem.getVotes()));

        PollChoiceAdapter.OnChoiceClickListener onChoiceClickListener = choice -> {
            getBaseItemActivity().onSelectPollChoiceClick(pollDescriptor, choice, peerPollItem.getVotes());
        };
        mChoicesAdapter.setOnChoiceClickListener(onChoiceClickListener);

        ViewGroup.LayoutParams layoutParams = mChoicesView.getLayoutParams();
        layoutParams.height = pollDescriptor.getChoices().size() * (int) (PollChoiceViewHolder.DESIGN_ITEM_HEIGHT * Design.HEIGHT_RATIO);
        mChoicesView.setLayoutParams(layoutParams);

        if (!getBaseItemActivity().displayPeerItemAvatar()) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mPollContainer.getLayoutParams();
            int leftMargin = Design.PEER_AVATAR_CONVERSATION_MARGIN;

            if (getBaseItemActivity().isSelectItemMode()) {
                marginLayoutParams.setMarginStart(leftMargin + BaseItemViewHolder.CHECKBOX_MARGIN + BaseItemViewHolder.CHECKBOX_HEIGHT);
            } else {
                marginLayoutParams.setMarginStart(leftMargin);
            }

            mPollContainer.setLayoutParams(marginLayoutParams);
        }
    }

    @Override
    List<View> clickableViews() {

        return new ArrayList<View>() {
            {
                add(getContainer());
            }
        };
    }

    List<UIPollResult> getPollResults(ConversationService.PollDescriptor pollDescriptor, Map<UUID, List<ConversationService.PollDescriptor.Choice>> votes) {

        List<UIPollResult> results = new ArrayList<>();

        List<ConversationService.PollDescriptor.Choice> choices = pollDescriptor.getChoices();
        for (ConversationService.PollDescriptor.Choice choice : choices) {
            results.add(new UIPollResult(choice));
        }

        for (Map.Entry<UUID, List<ConversationService.PollDescriptor.Choice>> entry : votes.entrySet()) {
            UUID twincodeOutboundId = entry.getKey();
            List<ConversationService.PollDescriptor.Choice> userVotes = entry.getValue();
            for (ConversationService.PollDescriptor.Choice userVote : userVotes) {
                for (UIPollResult pollResult : results) {
                    if (pollResult.getChoice().equals(userVote)) {
                        pollResult.setCount(pollResult.getCount() + 1);
                        if (pollResult.getPollResultVoters().size() < 2) {
                            getBaseItemActivity().getPollAvatar(twincodeOutboundId, (Bitmap avatar) -> {
                                if (avatar != null) {
                                    UIPollResult.UIPollResultVoter voter = new UIPollResult.UIPollResultVoter(null, avatar);
                                    pollResult.addPollResultVoter(voter);
                                }
                            });
                        }

                        if (getBaseItemActivity().isUserVote(twincodeOutboundId)) {
                            pollResult.setSelected(true);
                        }
                    }
                }
            }
        }

        return results;
    }

    @Override
    void onViewRecycled() {

        super.onViewRecycled();
    }

    //
    // Private methods
    //

    private PeerPollItem getPollItem() {

        return (PeerPollItem) getItem();
    }
}
