/*
 *  Copyright (c) 2024-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.backupActivity;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;

import java.util.ArrayList;
import java.util.List;

public class BackupWordsViewHolder extends RecyclerView.ViewHolder {
    private static final String LOG_TAG = "BackupWordsViewHolder";
    private static final boolean DEBUG = false;

    private static final float DESIGN_MARGIN = 30;

    private final BackupWordsAdapter mBackupWordsAdapter;

    public BackupWordsViewHolder(AbstractTwinmeActivity activity, @NonNull View view) {

        super(view);

        view.setBackgroundColor(Design.WHITE_COLOR);

        mBackupWordsAdapter = new BackupWordsAdapter(activity, new ArrayList<>());

        GridLayoutManager gridLayoutManager = new GridLayoutManager(activity, 2);
        RecyclerView recyclerView = view.findViewById(R.id.create_backup_activity_words_item_list_view);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(mBackupWordsAdapter);
        recyclerView.setItemAnimator(null);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) recyclerView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_MARGIN * Design.HEIGHT_RATIO);
    }

    public void onBind(@Nullable List<UIBackupWord> backupWords) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBind: " + backupWords);
        }

        if (backupWords != null) {
            mBackupWordsAdapter.setBackupWords(backupWords);
        }
    }
}
