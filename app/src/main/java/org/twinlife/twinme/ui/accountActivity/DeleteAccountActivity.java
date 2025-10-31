/*
 *  Copyright (c) 2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.accountActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.services.DeleteAccountService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;

public class DeleteAccountActivity extends AbstractTwinmeActivity implements DeleteAccountService.Observer {
    private static final String LOG_TAG = "DeleteAccountActivity";
    private static final boolean DEBUG = false;

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
        setTitle(getString(R.string.deleted_account_activity_delete));

        applyInsets(R.id.delete_account_activity_layout, R.id.delete_account_activity_tool_bar, R.id.delete_account_activity_content_view, Design.TOOLBAR_COLOR, false);

        View contentView = findViewById(R.id.delete_account_activity_content_view);
        contentView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        TextView accountTextView = findViewById(R.id.delete_account_activity_message_view);
        accountTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        Design.updateTextFont(accountTextView, Design.FONT_MEDIUM34);

        String accountText = getResources().getString(R.string.account_activity_message_first_part) +
                "\n\n" + getResources().getString(R.string.account_activity_message_second_part);
        accountTextView.setText(accountText);

        View deleteView = findViewById(R.id.delete_account_activity_delete_view);
        ViewGroup.LayoutParams layoutParams = deleteView.getLayoutParams();
        layoutParams.height = Design.BUTTON_HEIGHT;

        mDeleteListener = new DeleteListener();
        deleteView.setOnClickListener(mDeleteListener);

        TextView deleteTextView = findViewById(R.id.delete_account_activity_delete_label_view);
        deleteTextView.setTextColor(Color.RED);
        Design.updateTextFont(deleteTextView, Design.FONT_MEDIUM34);

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
}
