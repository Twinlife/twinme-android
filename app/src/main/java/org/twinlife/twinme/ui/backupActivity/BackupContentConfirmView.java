/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.backupActivity;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.services.BackupService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.AbstractBottomSheetView;
import org.twinlife.twinme.utils.BackupStats;

public class BackupContentConfirmView extends AbstractBottomSheetView {
    private static final String LOG_TAG = "BackupContentCon...";
    private static final boolean DEBUG = false;

    public BackupContentConfirmView(Context context) {
        super(context);
    }

    public BackupContentConfirmView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (DEBUG) {
            Log.d(LOG_TAG, "create");
        }

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.backup_content_confirm_view, this, true);
        initViews();
    }

    public void initStats(BackupStats backupStats) {
        if (DEBUG) {
            Log.d(LOG_TAG, "initStats: " + backupStats);
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        BackupContentAdapter backupContentAdapter = new BackupContentAdapter(this);
        RecyclerView recyclerView = findViewById(R.id.backup_content_confirm_view_content_view);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setBackgroundColor(Color.TRANSPARENT);
        recyclerView.setAdapter(backupContentAdapter);

        backupContentAdapter.updateWithBackupStats(backupStats);
    }

    public void initRestoreReport(BackupService.RestoreReport restoreReport, boolean isLastBackup) {
        if (DEBUG) {
            Log.d(LOG_TAG, "initStats: restoreReport=" + restoreReport);
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        BackupContentAdapter backupContentAdapter = new BackupContentAdapter(this);
        RecyclerView recyclerView = findViewById(R.id.backup_content_confirm_view_content_view);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setBackgroundColor(Color.TRANSPARENT);
        recyclerView.setAdapter(backupContentAdapter);

        if (!restoreReport.isRestoreUpToDate()) {
            String diffMessage = String.format("%s\n\n%s", getContext().getString(R.string.backup_view_verify_not_up_to_date), getContext().getString(R.string.backup_view_content_diff_message));
            if (isLastBackup) {
                mMessageView.setText(diffMessage);
            } else {
                mMessageView.setText(String.format("%s\n%s", getContext().getString(R.string.restore_view_more_recent_backup), diffMessage));
            }
        } else {
            if (isLastBackup) {
                mMessageView.setText(getContext().getString(R.string.restore_view_confirm));
            } else {
                mMessageView.setText(String.format("%s\n\n%s", getContext().getString(R.string.restore_view_more_recent_backup), getContext().getString(R.string.restore_view_confirm)));
            }
        }

        backupContentAdapter.updateWithRestoreReport(restoreReport);
    }

    @Override
    protected void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        mOverlayView = findViewById(R.id.backup_content_confirm_view_overlay_view);
        mActionView = findViewById(R.id.backup_content_confirm_view_action_view);
        mSlideMarkView = findViewById(R.id.backup_content_confirm_view_slide_mark_view);
        mTitleView = findViewById(R.id.backup_content_confirm_view_title_view);
        mMessageView = findViewById(R.id.backup_content_confirm_view_message_view);
        mConfirmView = findViewById(R.id.backup_content_confirm_view_confirm_view);
        mConfirmTextView = findViewById(R.id.backup_content_confirm_view_confirm_text_view);
        mCancelView = findViewById(R.id.backup_content_confirm_view_cancel_view);
        mCancelTextView = findViewById(R.id.backup_content_confirm_view_cancel_text_view);

        super.initViews();

        mMessageView.setTypeface(Design.FONT_MEDIUM34.typeface);
        mMessageView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_MEDIUM34.size);

        MarginLayoutParams marginLayoutParams = (MarginLayoutParams) mMessageView.getLayoutParams();
        marginLayoutParams.bottomMargin = (int) (DESIGN_MESSAGE_MARGIN * Design.HEIGHT_RATIO);

        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable confirmViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        confirmViewBackground.getPaint().setColor(Design.getMainStyle());
        mConfirmView.setBackground(confirmViewBackground);
    }
}
