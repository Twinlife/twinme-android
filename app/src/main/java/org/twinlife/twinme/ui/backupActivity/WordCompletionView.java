/*
 *  Copyright (c) 2024-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.backupActivity;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;

import androidx.percentlayout.widget.PercentRelativeLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

import java.util.List;

public class WordCompletionView extends PercentRelativeLayout {
    private static final String LOG_TAG = "WordCompletionView";
    private static final boolean DEBUG = false;

    private RestoreActivity mActivity;
    private WordCompletionAdapter mWordCompletionAdapter;

    public WordCompletionView(Context context) {
        super(context);
    }

    public WordCompletionView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (DEBUG) {
            Log.d(LOG_TAG, "create");
        }

        mActivity = (RestoreActivity) context;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.word_completion_view, this, true);
        initViews();
    }

    public void setWordsSuggestions(List<String> suggestions) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setWordsSuggestions: " + suggestions);
        }

        mWordCompletionAdapter.setWordsSuggestions(suggestions);
    }

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        MaterialCardView cardView = findViewById(R.id.word_completion_card_view);
        cardView.setRadius(Design.CONTAINER_RADIUS);
        cardView.setStrokeWidth(2);
        cardView.setStrokeColor(Design.FONT_COLOR_GREY);
        cardView.setBackgroundColor(Design.WHITE_COLOR);

        WordCompletionAdapter.OnWordCompletionClickListener onWordCompletionClickListener = (word) -> mActivity.selectWordSuggestions(word);

        mWordCompletionAdapter = new WordCompletionAdapter(this,onWordCompletionClickListener);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        RecyclerView recyclerView = findViewById(R.id.word_completion_list_view);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(mWordCompletionAdapter);
        recyclerView.setItemAnimator(null);
    }
}
