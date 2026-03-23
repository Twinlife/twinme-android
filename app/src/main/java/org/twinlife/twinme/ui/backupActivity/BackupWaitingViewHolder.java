/*
 *  Copyright (c) 2024-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.backupActivity;

import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class BackupWaitingViewHolder extends RecyclerView.ViewHolder {
    private static final String LOG_TAG = "BackupWaitingViewHolder";
    private static final boolean DEBUG = false;

    private static final float DESIGN_LOADER_MARGIN = 20;
    private static final float DESIGN_MESSAGE_MARGIN = 20;

    public BackupWaitingViewHolder(@NonNull View view) {

        super(view);

        ProgressBar progressBar = view.findViewById(R.id.create_backup_activity_waiting_item_progress_bar);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) progressBar.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_LOADER_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_LOADER_MARGIN * Design.HEIGHT_RATIO);

        TextView messageView = view.findViewById(R.id.create_backup_activity_waiting_item_message_view);
        messageView.setTypeface(Design.FONT_REGULAR32.typeface);
        messageView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_REGULAR32.size);
        messageView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) messageView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_MESSAGE_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_MESSAGE_MARGIN * Design.WIDTH_RATIO);
    }

    public void onBind() {

    }
}
