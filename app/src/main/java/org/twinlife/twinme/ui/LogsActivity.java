/*
 *  Copyright (c) 2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class LogsActivity extends AbstractTwinmeActivity {
    private static final String LOG_TAG = "LogsActivity";
    private static final boolean DEBUG = false;

    private static final float DESIGN_LOGS_MARGIN = 20f;

    private View mContainerView;
    private TextView mLogsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        initViews();
    }

    @Override
    protected void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        super.onResume();
    }

    //
    // Private methods
    //

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.logs_activity);

        setStatusBarColor();
        setToolBar(R.id.logs_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);

        setTitle(getString(R.string.feedback_activity_logs));
        setBackgroundColor(Design.WHITE_COLOR);

        applyInsets(R.id.logs_activity_layout, R.id.logs_activity_tool_bar, R.id.logs_activity_logs_view, Design.TOOLBAR_COLOR, false);

        mContainerView = findViewById(R.id.logs_activity_logs_container);
        mContainerView.setBackgroundColor(Design.WHITE_COLOR);

        mLogsTextView = findViewById(R.id.logs_activity_logs_view);
        mLogsTextView.setTypeface(Typeface.MONOSPACE);
        mLogsTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        mLogsTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mLogsTextView.setMovementMethod(new ScrollingMovementMethod());
        mLogsTextView.setText(getTwinmeContext().getManagementService().getLogReport());

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mLogsTextView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_LOGS_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_LOGS_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.topMargin = (int) (DESIGN_LOGS_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_LOGS_MARGIN * Design.HEIGHT_RATIO);
    }

    @Override
    public void updateColor() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateColor");
        }

        super.updateColor();

        setToolBar(R.id.logs_activity_tool_bar);
        setBackgroundColor(Design.WHITE_COLOR);
        setStatusBarColor();

        applyInsets(R.id.logs_activity_layout, R.id.logs_activity_tool_bar, R.id.logs_activity_logs_view, Design.TOOLBAR_COLOR, false);
        mContainerView.setBackgroundColor(Design.WHITE_COLOR);
        mLogsTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
    }
}
