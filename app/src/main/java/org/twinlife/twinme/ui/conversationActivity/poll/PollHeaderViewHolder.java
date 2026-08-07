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
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.SwitchView;

import java.util.Locale;

public class PollHeaderViewHolder extends RecyclerView.ViewHolder {
    private static final String LOG_TAG = "PollHeaderViewHolder";
    private static final boolean DEBUG = false;

    private static final int DESIGN_HEADER_HEIGHT = 240;
    private static final int DESIGN_EDIT_TEXT_MARGIN = 30;
    private static final int DESIGN_BOTTOM_MARGIN = 20;

    private final EditText mPollView;
    private final TextView mCounterView;
    private final SwitchView mAllowMultipleChoiceView;

    private final CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener;

    private boolean mBeginEdit = true;

    public PollHeaderViewHolder(@NonNull View view, CreatePollActivity createPollActivity) {

        super(view);

        view.setBackgroundColor(Color.TRANSPARENT);
        view.setMinimumHeight((int) (DESIGN_HEADER_HEIGHT * Design.HEIGHT_RATIO));

        mPollView = view.findViewById(R.id.create_poll_footer_header_question_view);
        Design.updateTextFont(mPollView, Design.FONT_REGULAR34);
        mPollView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mPollView.setHintTextColor(Design.GREY_COLOR);
        mPollView.setFilters(new InputFilter[]{new InputFilter.LengthFilter(CreatePollActivity.MAX_QUESTION_LENGTH)});
        mPollView.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {

                mCounterView.setText(String.format(Locale.getDefault(), "%d/%d", s.length(), CreatePollActivity.MAX_QUESTION_LENGTH));
                createPollActivity.setPollQuestion(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.toString().contains("\n")) {
                    mPollView.setText(s.toString().replace("\n", ""));
                    mPollView.setSelection(mPollView.getText().length());
                    createPollActivity.setPollQuestion(mPollView.getText().toString());
                    createPollActivity.selectFirstChoice();
                }
            }
        });

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mPollView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (CreatePollActivity.DESIGN_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (CreatePollActivity.DESIGN_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.topMargin = (int) (DESIGN_EDIT_TEXT_MARGIN * Design.HEIGHT_RATIO);

        mCounterView = view.findViewById(R.id.create_poll_footer_header_counter_view);
        Design.updateTextFont(mCounterView, Design.FONT_REGULAR28);
        mCounterView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mCounterView.getLayoutParams();
        marginLayoutParams.rightMargin = (int) (CreatePollActivity.DESIGN_MARGIN * Design.WIDTH_RATIO);

        mAllowMultipleChoiceView = view.findViewById(R.id.create_poll_footer_header_allow_multiple_switch_view);
        Design.updateTextFont(mAllowMultipleChoiceView, Design.FONT_MEDIUM34);
        mAllowMultipleChoiceView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mOnCheckedChangeListener = (compoundButton, value) -> createPollActivity.setAllowMultipleChoice(value);
        mAllowMultipleChoiceView.setOnCheckedChangeListener(mOnCheckedChangeListener);

        mAllowMultipleChoiceView.setPadding(0, 0, 0, (int) (DESIGN_BOTTOM_MARGIN * Design.HEIGHT_RATIO));

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mAllowMultipleChoiceView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (CreatePollActivity.DESIGN_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (CreatePollActivity.DESIGN_MARGIN * Design.WIDTH_RATIO);
    }

    public void onBind(String question, boolean allowMultipleChoice) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBind: question=" + question);
        }

        if (mBeginEdit) {
            mBeginEdit = false;

            mPollView.postDelayed(() -> {
                mPollView.requestFocus();

                InputMethodManager inputMethodManager = (InputMethodManager) itemView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputMethodManager != null) {
                    inputMethodManager.showSoftInput(mPollView, InputMethodManager.SHOW_IMPLICIT);
                }
            }, 300);
        }

        mPollView.setText(question);
        mCounterView.setText(String.format(Locale.getDefault(), "%d/%d", question.length(), CreatePollActivity.MAX_QUESTION_LENGTH));
        mAllowMultipleChoiceView.setOnCheckedChangeListener(null);
        mAllowMultipleChoiceView.setChecked(allowMultipleChoice);
        mAllowMultipleChoiceView.setOnCheckedChangeListener(mOnCheckedChangeListener);
    }

    public void onViewRecycled() {

    }
}
