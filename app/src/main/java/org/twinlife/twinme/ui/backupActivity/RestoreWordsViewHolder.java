/*
 *  Copyright (c) 2024-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.backupActivity;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.contacts.PasteEditText;

import java.util.ArrayList;
import java.util.List;

public class RestoreWordsViewHolder  extends RecyclerView.ViewHolder {
    private static final String LOG_TAG = "RestoreWordsViewHolder";
    private static final boolean DEBUG = false;

    private static class CustomGridLayoutManager extends GridLayoutManager {

        public CustomGridLayoutManager(Context context, int lineCount) {
            super(context, lineCount);
        }


        @Override
        public boolean canScrollVertically() {
            return false;
        }
    }

    private static final float DESIGN_CONTAINER_HEIGHT= 80;
    private static final float DESIGN_WORDS_COMPLETION_HEIGHT= 240;
    private static final float DESIGN_EDIT_TEXT_MARGIN = 24;
    private static final float DESIGN_HORIZONTAL_MARGIN = 34;
    private static final float DESIGN_VERTICAL_MARGIN = 20;
    private static final float DESIGN_PASTE_VIEW_HEIGHT = 120;
    private static final float DESIGN_PASTE_ICON_SIZE = 50;
    private static final float DESIGN_PASTE_MARGIN = 20;

    private final RestoreWordsAdapter mRestoreWordsAdapter;
    private final WordCompletionView mWordCompletionView;
    private final TextView mPositionView;
    private final PasteEditText mEditText;
    private final RestoreActivity mRestoreActivity;

    public RestoreWordsViewHolder(RestoreActivity activity, @NonNull View view) {

        super(view);

        mRestoreActivity = activity;

        TextView messageView = view.findViewById(R.id.restore_activity_words_item_message_view);
        messageView.setTypeface(Design.FONT_REGULAR32.typeface);
        messageView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_REGULAR32.size);
        messageView.setTextColor(Design.FONT_COLOR_DEFAULT);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) messageView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.topMargin = (int) (DESIGN_VERTICAL_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_VERTICAL_MARGIN * Design.HEIGHT_RATIO);

        View containerSearchView = view.findViewById(R.id.restore_activity_words_item_container_view);

        ViewGroup.LayoutParams layoutParams = containerSearchView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_CONTAINER_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) containerSearchView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.topMargin = (int) (DESIGN_VERTICAL_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_VERTICAL_MARGIN * Design.WIDTH_RATIO);

        GradientDrawable containerViewBackground = new GradientDrawable();
        containerViewBackground.setColor(Design.GREY_ITEM_COLOR);
        containerViewBackground.setStroke(2, Design.FONT_COLOR_GREY);
        containerViewBackground.setCornerRadius((int) (DESIGN_CONTAINER_HEIGHT * Design.HEIGHT_RATIO * 0.5f * Resources.getSystem().getDisplayMetrics().density));
        containerSearchView.setBackground(containerViewBackground);

        mPositionView = view.findViewById(R.id.restore_activity_words_item_position_view);
        mPositionView.setTypeface(Design.FONT_MONOSPACE34.typeface);
        mPositionView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_MONOSPACE34.size);
        mPositionView.setTextColor(Design.FONT_COLOR_GREY);
        mPositionView.setVisibility(View.INVISIBLE);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mPositionView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_EDIT_TEXT_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_EDIT_TEXT_MARGIN * Design.WIDTH_RATIO);

        InputFilter inputFilter = (charSequence, i, i1, spanned, i2, i3) -> {
            try {
                char c = charSequence.charAt(0);
                if (Character.isLetter(c)) {
                    return "" + Character.toUpperCase(c);
                } else {
                    return "";
                }
            } catch (Exception e) {
            }

            return null;
        };

        mEditText = view.findViewById(R.id.restore_activity_words_item_edit_text);
        mEditText.setTypeface(Design.FONT_MONOSPACE34.typeface);
        mEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_MONOSPACE34.size);
        mEditText.setTextColor(Design.FONT_COLOR_DEFAULT);
        mEditText.setHintTextColor(Design.FONT_COLOR_GREY);
        mEditText.setHint(mRestoreActivity.getString(R.string.application_search_hint).toUpperCase());
        mEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        mEditText.setFilters(new InputFilter[]{inputFilter});

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mEditText.getLayoutParams();
        marginLayoutParams.rightMargin = (int) (DESIGN_EDIT_TEXT_MARGIN * Design.WIDTH_RATIO);

        mEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focus) {
                if (focus) {
                    mRestoreActivity.initCurrentWord();
                    mRestoreWordsAdapter.notifyItemChanged(0, activity.getBackupWords().size());
                    mPositionView.setText(String.valueOf(activity.getCurrentWord() + 1));
                    mPositionView.setVisibility(View.VISIBLE);
                    mRestoreActivity.updateScroll(true);
                } else {
                    mRestoreActivity.updateScroll(true);
                }
            }
        });

        mEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (!s.toString().isEmpty()) {
                    List<String> words = mRestoreActivity.searchWords(s.toString());
                    if (words.isEmpty()) {
                        mWordCompletionView.setVisibility(View.GONE);
                        mWordCompletionView.setWordsSuggestions(new ArrayList<>());
                        mRestoreActivity.updateScroll(true);
                    } else {
                        mWordCompletionView.setVisibility(View.VISIBLE);
                        mWordCompletionView.setWordsSuggestions(words);
                        mRestoreActivity.updateScroll(false);
                    }
                } else {
                    mWordCompletionView.setVisibility(View.GONE);
                    mWordCompletionView.setWordsSuggestions(new ArrayList<>());
                    mRestoreActivity.updateScroll(true);
                }
            }
        });

        mEditText.setPasteObserver(this::onPasteText);

        mRestoreWordsAdapter = new RestoreWordsAdapter(mRestoreActivity, new ArrayList<>());

        CustomGridLayoutManager gridLayoutManager = new CustomGridLayoutManager(mRestoreActivity, 2);

        RecyclerView recyclerView = view.findViewById(R.id.restore_activity_words_item_list_view);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(mRestoreWordsAdapter);
        recyclerView.setItemAnimator(null);

        mWordCompletionView = view.findViewById(R.id.restore_activity_words_item_word_completion_view);
        mWordCompletionView.setVisibility(View.GONE);

        layoutParams = mWordCompletionView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_WORDS_COMPLETION_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mWordCompletionView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);

        View pasteView = view.findViewById(R.id.restore_activity_words_item_paste_view);
        pasteView.setOnClickListener(v -> mRestoreActivity.onPasteWordsClick());

        layoutParams = pasteView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_PASTE_VIEW_HEIGHT * Design.HEIGHT_RATIO);

        ImageView copyImageView = view.findViewById(R.id.restore_activity_words_item_paste_image_view);
        copyImageView.setColorFilter(Design.BLACK_COLOR);

        layoutParams = copyImageView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_PASTE_ICON_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_PASTE_ICON_SIZE * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) copyImageView.getLayoutParams();
        marginLayoutParams.rightMargin = (int) (DESIGN_PASTE_MARGIN * Design.WIDTH_RATIO);

        TextView copyTextView = view.findViewById(R.id.restore_activity_words_item_paste_text_view);
        copyTextView.setTypeface(Design.FONT_MEDIUM30.typeface);
        copyTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_MEDIUM30.size);
        copyTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
    }

    public void onBind(List<UIBackupWord> backupWords, int position, boolean openKeyboard) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBind: " + backupWords);
        }

        if (position != -1) {
            mPositionView.setText(String.valueOf(position + 1));
            mPositionView.setVisibility(View.VISIBLE);
        } else {
            mPositionView.setVisibility(View.INVISIBLE);
        }

        mRestoreWordsAdapter.setBackupWords(backupWords);

        mWordCompletionView.setVisibility(View.GONE);
        mRestoreActivity.updateScroll(true);
        mEditText.setText("");

        if (openKeyboard) {
            mEditText.requestFocus();
            mEditText.post(() -> {
                InputMethodManager inputMethodManager = (InputMethodManager) itemView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputMethodManager != null) {
                    inputMethodManager.showSoftInput(mEditText, InputMethodManager.SHOW_FORCED);
                }
            });
        }
    }

    private void onPasteText() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPasteText");
        }

        mRestoreActivity.onPasteWordsClick();
    }
}
