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
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

import java.util.Locale;

public class PollAddChoiceViewHolder extends RecyclerView.ViewHolder {
    private static final String LOG_TAG = "PollAddChoiceViewHolder";
    private static final boolean DEBUG = false;

    private static final int DESIGN_CHOICE_HEIGHT = 160;
    private static final int DESIGN_CONTAINER_HEIGHT = 140;
    private static final int DESIGN_VERTICAL_MARGIN = 20;

    private final View mContainerView;
    private final GradientDrawable mContainerBackground;
    private final TextView mChoicePositionView;
    private final EditText mEditChoiceView;
    private final TextView mCounterView;

    private UIPollChoice mPollChoice;

    public PollAddChoiceViewHolder(@NonNull View view, CreatePollActivity createPollActivity) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int) (DESIGN_CHOICE_HEIGHT * Design.HEIGHT_RATIO);
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Color.TRANSPARENT);

        mContainerView = view.findViewById(R.id.create_poll_add_choice_container_view);

        layoutParams = mContainerView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_CONTAINER_HEIGHT * Design.HEIGHT_RATIO);

        mContainerBackground = new GradientDrawable();
        mContainerBackground.setColor(Design.WHITE_COLOR);
        mContainerBackground.setStroke(4, Design.GREY_COLOR);
        mContainerBackground.setCornerRadius(Design.CONTAINER_RADIUS);
        mContainerView.setBackground(mContainerBackground);

        mContainerView.setOnClickListener(v -> setFocus());

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mContainerView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (CreatePollActivity.DESIGN_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (CreatePollActivity.DESIGN_MARGIN * Design.WIDTH_RATIO);

        mChoicePositionView = view.findViewById(R.id.create_poll_add_choice_title_view);
        Design.updateTextFont(mChoicePositionView, Design.FONT_REGULAR30);
        mChoicePositionView.setTextColor(Design.getMainStyle());

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mChoicePositionView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (CreatePollActivity.DESIGN_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.topMargin = (int) (DESIGN_VERTICAL_MARGIN * Design.HEIGHT_RATIO);

        mEditChoiceView = view.findViewById(R.id.create_poll_add_choice_edit_text);
        Design.updateTextFont(mEditChoiceView, Design.FONT_REGULAR34);
        mEditChoiceView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mEditChoiceView.setHintTextColor(Design.GREY_COLOR);
        mEditChoiceView.setFilters(new InputFilter[]{new InputFilter.LengthFilter(CreatePollActivity.MAX_QUESTION_LENGTH)});
        mEditChoiceView.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {

                mCounterView.setVisibility(View.VISIBLE);
                mContainerBackground.setStroke(4, Design.getMainStyle());

                mCounterView.setText(String.format(Locale.getDefault(), "%d/%d", s.length(), CreatePollActivity.MAX_CHOICE_LENGTH));

                if (mPollChoice != null && !mPollChoice.getChoice().equals(mEditChoiceView.getText().toString())) {
                    createPollActivity.updatePollChoice(mPollChoice, mEditChoiceView.getText().toString());
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        mEditChoiceView.setOnFocusChangeListener((view1, focused) -> {
            if (!focused) {
                mContainerBackground.setStroke(4, Design.GREY_COLOR);
            } else {
                mContainerBackground.setStroke(4, Design.getMainStyle());
            }

            if (mPollChoice != null) {
                createPollActivity.selectPollChoice(mPollChoice, focused);
            }
        });

        mEditChoiceView.setOnEditorActionListener((v, actionId, event) -> {

            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                if (mPollChoice != null) {
                    createPollActivity.selectNextPollChoice(mPollChoice);
                }

                return true;
            }

            return false;
        });

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mEditChoiceView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (CreatePollActivity.DESIGN_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (CreatePollActivity.DESIGN_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.topMargin = (int) (DESIGN_VERTICAL_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_VERTICAL_MARGIN * Design.HEIGHT_RATIO);

        mCounterView = view.findViewById(R.id.create_poll_add_choice_counter_view);
        Design.updateTextFont(mCounterView, Design.FONT_REGULAR30);
        mCounterView.setTextColor(Design.getMainStyle());

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mCounterView.getLayoutParams();
        marginLayoutParams.rightMargin = (int) (CreatePollActivity.DESIGN_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.topMargin = (int) (DESIGN_VERTICAL_MARGIN * Design.HEIGHT_RATIO);
    }

    public void onBind(UIPollChoice pollChoice) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBind: pollChoice=" + pollChoice);
        }

        mPollChoice = pollChoice;

        mChoicePositionView.setText(pollChoice.getChoicePosition(itemView.getContext()));
        mEditChoiceView.setText(pollChoice.getChoice());

        if (pollChoice.isSelected()) {
            mContainerBackground.setStroke(4, Design.getMainStyle());
            mCounterView.setVisibility(View.VISIBLE);
            setFocus();
        } else {
            mContainerBackground.setStroke(4, Design.GREY_COLOR);
            mCounterView.setVisibility(View.GONE);
        }

        mContainerView.setBackground(mContainerBackground);
    }

    public void onViewRecycled() {

    }

    private void setFocus() {


        mEditChoiceView.post(() -> {
            mEditChoiceView.requestFocus();

            InputMethodManager inputMethodManager = (InputMethodManager) itemView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(mEditChoiceView, InputMethodManager.SHOW_IMPLICIT);
        });
    }
}
