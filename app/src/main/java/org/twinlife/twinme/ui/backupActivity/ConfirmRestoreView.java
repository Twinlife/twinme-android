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
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.utils.AbstractBottomSheetView;

import java.util.List;

public class ConfirmRestoreView extends AbstractBottomSheetView {
    private static final String LOG_TAG = "ConfirmRestoreView";
    private static final boolean DEBUG = false;

    public ConfirmRestoreView(Context context) {
        super(context);
    }

    public ConfirmRestoreView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (DEBUG) {
            Log.d(LOG_TAG, "create");
        }

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.confirm_restore_view, this, true);
        initViews();
    }

    public void setBackupContent(AbstractTwinmeActivity activity, List<UIBackupContent> backupContents) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setBackupContent");
        }

        ConfirmBackupRestoreAdapter confirmBackupRestoreAdapter = new ConfirmBackupRestoreAdapter(activity, backupContents);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity, RecyclerView.VERTICAL, false);
        RecyclerView recyclerView = findViewById(R.id.confirm_restore_view_list_view);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(confirmBackupRestoreAdapter);
        recyclerView.setItemAnimator(null);
    }

    @Override
    protected void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        mOverlayView = findViewById(R.id.confirm_restore_view_overlay_view);
        mActionView = findViewById(R.id.confirm_restore_view_action_view);
        mSlideMarkView = findViewById(R.id.confirm_restore_view_slide_mark_view);
        mTitleView = findViewById(R.id.confirm_restore_view_title_view);
        mMessageView = findViewById(R.id.confirm_restore_view_message_view);
        mConfirmView = findViewById(R.id.confirm_restore_view_confirm_view);
        mConfirmTextView = findViewById(R.id.confirm_restore_view_confirm_text_view);
        mCancelView = findViewById(R.id.confirm_restore_view_cancel_view);
        mCancelTextView = findViewById(R.id.confirm_restore_view_cancel_text_view);

        super.initViews();

        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable confirmViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        confirmViewBackground.getPaint().setColor(Design.getMainStyle());
        mConfirmView.setBackground(confirmViewBackground);
    }
}
