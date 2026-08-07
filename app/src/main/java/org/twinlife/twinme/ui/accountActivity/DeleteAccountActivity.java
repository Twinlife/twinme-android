/*
 *  Copyright (c) 2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.accountActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.graphics.text.LineBreaker;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.services.DeleteAccountService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;

public class DeleteAccountActivity extends AbstractTwinmeActivity implements DeleteAccountService.Observer {
    private static final String LOG_TAG = "DeleteAccountActivity";
    private static final boolean DEBUG = false;

    private static final int DESIGN_IMAGE_MARGIN = 100;
    private static final int DESIGN_MESSAGE_VERTICAL_MARGIN = 60;
    private static final int DESIGN_MESSAGE_HORIZONTAL_MARGIN = 40;
    private static final int DESIGN_DELETE_MARGIN = 20;
    private static final int DESIGN_CONFIRM_VERTICAL_MARGIN = 10;
    private static final int DESIGN_CONFIRM_HORIZONTAL_MARGIN = 20;
    private static final int DESIGN_CANCEL_HEIGHT = 140;
    private static final int DESIGN_CANCEL_MARGIN = 80;

    private static final int CONFIRM_DELETE_ACCOUNT = 3;

    private class DeleteListener implements View.OnClickListener {

        private boolean disabled = false;

        @Override
        public void onClick(View view) {
            if (DEBUG) {
                Log.d(LOG_TAG, "RemoveListener.onClick: view=" + view);
            }

            if (disabled) {

                return;
            }
            disabled = true;

            onDeleteAccountClick();
        }

        void enable() {

            disabled = false;
        }
    }

    private DeleteListener mDeleteListener;
    private DeleteAccountService mDeleteAccountService;

    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        initViews();

        mDeleteAccountService = new DeleteAccountService(this, getTwinmeContext(), this);
    }

    //
    // Override Activity methods
    //

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        mDeleteAccountService.dispose();

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        super.onResume();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onActivityResult requestCode=" + requestCode + " resultCode=" + resultCode + " data=" + data);
        }

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CONFIRM_DELETE_ACCOUNT && resultCode == RESULT_OK) {
            mDeleteAccountService.deleteAccount();
            getTwinmeApplication().restoreWelcomeScreen();
        } else {
            mDeleteListener.enable();
        }
    }

    //
    // Implement DeleteAccountService.Observer methods
    //

    @Override
    public void onDeleteAccount() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteAccount");
        }

        Intent intent = new Intent(this, DeletedAccountActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }

    //
    // Private methods
    //

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.delete_account_activity);

        setStatusBarColor();
        setToolBar(R.id.delete_account_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);
        setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        setTitle(getString(R.string.account_view_title));

        applyInsets(R.id.delete_account_activity_layout, R.id.delete_account_activity_tool_bar, R.id.delete_account_activity_content_view, Design.TOOLBAR_COLOR, false);

        View contentView = findViewById(R.id.delete_account_activity_content_view);
        contentView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        ImageView imageView = findViewById(R.id.delete_account_activity_image_view);
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) imageView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_IMAGE_MARGIN * Design.HEIGHT_RATIO);

        TextView accountTextView = findViewById(R.id.delete_account_activity_message_view);
        accountTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        Design.updateTextFont(accountTextView, Design.FONT_MEDIUM34);

        String accountText = getResources().getString(R.string.account_view_message_first_part) +
                "\n\n" + getResources().getString(R.string.account_view_message_second_part) +
                "\n\n" + getResources().getString(R.string.account_view_message_third_part);
        accountTextView.setText(accountText);
        accountTextView.setMovementMethod(new ScrollingMovementMethod());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            accountTextView.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
        }

        marginLayoutParams = (ViewGroup.MarginLayoutParams) accountTextView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_MESSAGE_VERTICAL_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.leftMargin = (int) (DESIGN_MESSAGE_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_MESSAGE_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);

        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        View deleteView = findViewById(R.id.delete_account_activity_delete_view);
        ShapeDrawable saveViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        saveViewBackground.getPaint().setColor(Design.DELETE_COLOR_RED);
        deleteView.setBackground(saveViewBackground);

        ViewGroup.LayoutParams layoutParams = deleteView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;

        deleteView.setMinimumHeight(Design.BUTTON_HEIGHT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) deleteView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_DELETE_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) ((DESIGN_CANCEL_HEIGHT) * Design.HEIGHT_RATIO);

        TextView deleteTextView = findViewById(R.id.delete_account_activity_delete_text_view);
        deleteTextView.setTextColor(Color.WHITE);
        Design.updateTextFont(deleteTextView, Design.FONT_BOLD36);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) deleteTextView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_CONFIRM_VERTICAL_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_CONFIRM_VERTICAL_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.leftMargin = (int) (DESIGN_CONFIRM_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_CONFIRM_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);

        mDeleteListener = new DeleteListener();
        deleteView.setOnClickListener(mDeleteListener);

        View cancelView = findViewById(R.id.delete_account_activity_cancel_view);
        cancelView.setOnClickListener(v -> onCancelClick());

        layoutParams = cancelView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_CANCEL_HEIGHT * Design.HEIGHT_RATIO);

        TextView cancelTextView = findViewById(R.id.delete_account_activity_cancel_text_view);
        cancelTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        Design.updateTextFont(cancelTextView, Design.FONT_BOLD36);

        mProgressBarView = findViewById(R.id.delete_account_activity_progress_bar);
    }

    private void onDeleteAccountClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteAccountClick");
        }

        Intent intent = new Intent();
        intent.setClass(this, DeleteAccountConfirmActivity.class);
        startActivityForResult(intent, CONFIRM_DELETE_ACCOUNT);
        overridePendingTransition(0, 0);
    }

    private void onCancelClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCancelClick");
        }

        finish();
    }
}
